package com.pm.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.Protocol;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.msk.CfnCluster;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class LocalStack extends Stack {
    private final Vpc vpc;
    private final Cluster ecsCluster;

    // Constructor: Initializes the stack with the given application scope, ID, and properties
    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);
        this.vpc = createVpc();
        DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", "auth_service_db");

        DatabaseInstance patientServiceDb = createDatabase("PatientServiceDB", "patient_service_db");

        CfnHealthCheck authServiceHealthCheck = createHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");

        CfnHealthCheck patientServiceHealthCheck = createHealthCheck(patientServiceDb, "PatientServiceDBHealthCheck");

        CfnCluster mskCluster = createMskCluster();

        this.ecsCluster = createEcsCluster();

        FargateService authService = createFargateService("AuthService",
                "auth-service",
                List.of(4005),
                authServiceDb,
                Map.of("JWT_SECRET", "p+1VZ3j1K2bG5YX5jFjR3NcYZ5c8B7gG+u2Z5VnP3lE="));

        authService.getNode().addDependency(authServiceHealthCheck);
        authService.getNode().addDependency(authServiceDb);

        FargateService billingService = createFargateService("BillingService",
                "billing-service",
                List.of(4001, 9001),
                null,
                null);

        FargateService analyticsService = createFargateService("AnalyticsService",
                "analytics-service",
                List.of(4002),
                null,
                null);

        analyticsService.getNode().addDependency(mskCluster);

        FargateService patientService = createFargateService("PatientService",
                "patient-service",
                List.of(4000),
                patientServiceDb,
                Map.of("BILLING_SERVICE_ADDRESS","host.docker.internal",
                        "BILLING_SERVICE_GRPC_PORT", "9001"));

        patientService.getNode().addDependency(patientServiceDb);
        patientService.getNode().addDependency(patientServiceHealthCheck);
        patientService.getNode().addDependency(billingService);
        patientService.getNode().addDependency(mskCluster);

        createApiGateWayService();
    }

    public Vpc createVpc() {
        return Vpc.Builder.create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
                .build();
    }

    public DatabaseInstance createDatabase(String id, String databaseName) {
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine
                        .postgres(PostgresInstanceEngineProps
                        .builder()
                        .version(PostgresEngineVersion.VER_17_2)
                        .build()))
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin_user"))
                .databaseName(databaseName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();
    }

    private CfnHealthCheck createHealthCheck(DatabaseInstance db, String id) {
        return CfnHealthCheck.Builder
                .create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort()))
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30)
                        .failureThreshold(3)
                        .build())
                .build();
    }

    private CfnCluster createMskCluster() {
        return CfnCluster.Builder
                .create(this, "MskCluster")
                .clusterName("kafka-cluster")
                .kafkaVersion("2.8.0")
                .numberOfBrokerNodes(1)
                .brokerNodeGroupInfo(CfnCluster.BrokerNodeGroupInfoProperty.builder()
                        .instanceType("kafka.m5.xlarge")
                        .clientSubnets(vpc.getPrivateSubnets().stream().map(ISubnet::getSubnetId).toList())
                        .brokerAzDistribution("DEFAULT")
                        .build())
                .build();
    }

    private Cluster createEcsCluster() {
        return Cluster.Builder
                .create(this, "PatientManagementCluster")
                .vpc(vpc)
                .defaultCloudMapNamespace(CloudMapNamespaceOptions.builder().name("patient-management.local").build())
                .build();
    }

    private FargateService createFargateService(String id,
                                                String imageName,
                                                List<Integer> ports,
                                                DatabaseInstance db,
                                                Map<String, String> additionalEnvVars) {
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, id + "Task")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions.Builder containerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry(imageName))
                .portMappings(ports.stream().map(port -> PortMapping.builder()
                        .containerPort(port)
                        .hostPort(port).protocol(Protocol.TCP)
                        .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                .logGroup(LogGroup.Builder.create(this, id + "LogGroup")
                                        .logGroupName("/ecs/" + imageName)
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .retention(RetentionDays.ONE_DAY)
                                        .build())
                                .streamPrefix(imageName)
                                .build()));

        Map<String, String> envVars = new HashMap<>();
        envVars.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "localhost.localstack.cloud:4510, localhost.localstack.cloud:4511, localhost.localstack.cloud:4512");

        if (additionalEnvVars != null) {
            envVars.putAll(additionalEnvVars);
        }

        if (db != null) {
            envVars.put("SPRING_DATASOURCE_URL", "jdbc:postgresql://%s:%s/%s-db"
                    .formatted(db.getDbInstanceEndpointAddress(), db.getDbInstanceEndpointPort(), imageName));
            envVars.put("SPRING_DATASOURCE_USERNAME", "admin_user");
            envVars.put("SPRING_DATASOURCE_PASSWORD", db.getSecret().secretValueFromJson("password").toString());
            envVars.put("SPRING_JPA_HIBERNATE_DDL_AUTO", "update");
            envVars.put("SPRING_SQL_INIT_MODE", "always");
            envVars.put("SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT", "60000");
        }

        containerDefinitionOptions.environment(envVars);

        taskDefinition.addContainer(imageName + "Container", containerDefinitionOptions.build());

        return FargateService.Builder.create(this, id)
                .cluster(ecsCluster)
                .taskDefinition(taskDefinition)
                .assignPublicIp(false)
                .serviceName(imageName)
                .build();
    }

    private void createApiGateWayService() {
        FargateTaskDefinition taskDefinition = FargateTaskDefinition.Builder.create(this, "ApiGatewayTaskDefinition")
                .cpu(256)
                .memoryLimitMiB(512)
                .build();

        ContainerDefinitionOptions containerDefinitionOptions = ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromRegistry("api-gateway"))
                .environment(Map.of("SPRING_PROFILES_ACTIVE", "prod", "AUTH_SERVICE_URL", "http://host.docker.internal:4005"))
                .portMappings(Stream.of(4004).map(port -> PortMapping.builder()
                                .containerPort(port)
                                .hostPort(port).protocol(Protocol.TCP)
                                .build())
                        .toList())
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .logGroup(LogGroup.Builder.create(this, "ApiGatewayLogGroup")
                                .logGroupName("/ecs/api-gateway")
                                .removalPolicy(RemovalPolicy.DESTROY)
                                .retention(RetentionDays.ONE_DAY)
                                .build())
                        .streamPrefix("api-gateway")
                        .build()))
                .build();

        taskDefinition.addContainer("ApiGatewayContainer", containerDefinitionOptions);

        ApplicationLoadBalancedFargateService apiService =
                ApplicationLoadBalancedFargateService.Builder.create(this, "ApiGatewayService")
                        .cluster(ecsCluster)
                        .serviceName("api-gateway")
                        .taskDefinition(taskDefinition)
                        .desiredCount(1)
                        .build();
    }

    public static void main(String[] args) {

        // Create an instance of "App" with an output directory for generated assets
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        // Define stack properties, setting a BootstraplessSynthesizer (avoids needing AWS bootstrapping)
        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer())
                .build();

        // Instantiate the "LocalStack" with the app context, an ID, and the properties
        new LocalStack(app, "LocalStack", props);

        // Synthesize the app, generating the necessary CloudFormation templates
        app.synth();

        System.out.println("App synthesising in progress...");
    }
}
