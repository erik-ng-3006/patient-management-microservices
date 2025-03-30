package com.pm.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.*;

public class LocalStack extends Stack {
    private final Vpc vpc;

    // Constructor: Initializes the stack with the given application scope, ID, and properties
    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);
        this.vpc = createVpc();
        DatabaseInstance authServiceDb = createDatabase("AuthServiceDB", "auth_service_db");

        DatabaseInstance patientServiceDb = createDatabase("PatientServiceDB", "patient_service_db");
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
