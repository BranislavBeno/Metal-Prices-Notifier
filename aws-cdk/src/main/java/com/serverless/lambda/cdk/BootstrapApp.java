package com.serverless.lambda.cdk;

import com.serverless.lambda.cdk.util.CdkUtil;
import com.serverless.lambda.cdk.util.Validations;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

public class BootstrapApp {

    public static void main(final String[] args) {
        var app = new App();

        String region = Validations.requireNonEmpty(app, "region");
        String accountId = Validations.requireNonEmpty(app, "accountId");

        Environment awsEnvironment = CdkUtil.makeEnv(accountId, region);

        new Stack(app, "Bootstrap", StackProps.builder().env(awsEnvironment).build());

        app.synth();
    }
}
