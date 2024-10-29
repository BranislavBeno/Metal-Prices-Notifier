package com.serverless.lambda.cdk;

import software.amazon.awscdk.App;
import software.amazon.awscdk.StackProps;

public class MetalPricesLambdaApp {

    public static void main(final String[] args) {
        App app = new App();

        new MetalPricesLambdaStack(
                app, "MetalPricesLambdaStack", StackProps.builder().build());

        app.synth();
    }
}
