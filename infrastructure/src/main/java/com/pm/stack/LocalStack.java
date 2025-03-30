package com.pm.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.Vpc;

public class LocalStack extends Stack {
    private final Vpc vpc;


    // Constructor: Initializes the stack with the given application scope, ID, and properties
    public LocalStack(final App scope, final String id, final StackProps props) {
        super(scope, id, props);
        this.vpc = createVpc();
    }

    public Vpc createVpc() {
        return Vpc.Builder.create(this, "PatientManagementVPC")
                .vpcName("PatientManagementVPC")
                .maxAzs(2)
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
