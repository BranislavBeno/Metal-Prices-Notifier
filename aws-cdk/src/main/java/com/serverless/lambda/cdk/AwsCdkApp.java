package com.serverless.lambda.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class AwsCdkApp {

    public static void main(final String[] args) {
        App app = new App();

        new AwsCdkStack(app, "AwsCdkStack", StackProps.builder().build());

        app.synth();
    }
}
