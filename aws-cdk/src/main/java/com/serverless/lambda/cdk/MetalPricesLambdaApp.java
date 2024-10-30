package com.serverless.lambda.cdk;

import com.serverless.lambda.cdk.construct.ApplicationEnvironment;
import com.serverless.lambda.cdk.util.CdkUtil;
import com.serverless.lambda.cdk.util.Validations;
import java.util.List;
import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;

public class MetalPricesLambdaApp {

    public static void main(final String[] args) {
        App app = new App();

        String accountId = Validations.requireNonEmpty(app, "accountId");
        String region = Validations.requireNonEmpty(app, "region");
        String environmentName = Validations.requireNonEmpty(app, "environmentName");
        String applicationName = Validations.requireNonEmpty(app, "applicationName");

        Environment awsEnvironment = CdkUtil.makeEnv(accountId, region);

        var appEnvironment = new ApplicationEnvironment(applicationName, environmentName);

        String lambdaStackName = CdkUtil.createStackName("lambda", appEnvironment);

        PolicyStatement allowSendingEmails = PolicyStatement.Builder.create()
                .sid("AllowSendingEmails")
                .effect(Effect.ALLOW)
                .resources(List.of(
                        "arn:aws:ses:%s:%s:configuration-set/my-first-configuration-set".formatted(region, accountId),
                        "arn:aws:ses:%s:%s:identity/hugo.rad@gmail.com".formatted(region, accountId),
                        "arn:aws:ses:%s:%s:identity/b-l-s.click".formatted(region, accountId)))
                .actions(List.of("ses:SendEmail", "ses:SendRawEmail"))
                .build();

        new MetalPricesLambdaStack(
                app,
                "MetalPricesLambdaStack",
                StackProps.builder()
                        .stackName(lambdaStackName)
                        .env(awsEnvironment)
                        .build(),
                List.of(allowSendingEmails));

        app.synth();
    }
}
