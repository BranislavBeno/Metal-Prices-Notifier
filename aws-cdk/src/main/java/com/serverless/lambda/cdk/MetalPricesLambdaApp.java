package com.serverless.lambda.cdk;

import com.serverless.lambda.cdk.construct.ApplicationEnvironment;
import com.serverless.lambda.cdk.stack.MetalPricesLambdaStack;
import com.serverless.lambda.cdk.stack.StackInputParams;
import com.serverless.lambda.cdk.util.CdkUtil;
import com.serverless.lambda.cdk.util.Validations;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;

public class MetalPricesLambdaApp {

    public static void main(final String[] args) {
        App app = new App();

        String accountId = Validations.requireNonEmpty(app, "accountId");
        String region = Validations.requireNonEmpty(app, "region");
        String environmentName = Validations.requireNonEmpty(app, "environmentName");
        String applicationName = Validations.requireNonEmpty(app, "applicationName");

        Environment environment = CdkUtil.makeEnv(accountId, region);

        var appEnvironment = new ApplicationEnvironment(applicationName, environmentName);
        String stackName = CdkUtil.createStackName("lambda", appEnvironment);

        new MetalPricesLambdaStack(app, "MetalPricesLambdaStack", new StackInputParams(environment, stackName));

        app.synth();
    }
}
