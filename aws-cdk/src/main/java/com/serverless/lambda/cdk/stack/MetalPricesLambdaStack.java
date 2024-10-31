package com.serverless.lambda.cdk.stack;

import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupProps;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MetalPricesLambdaStack extends Stack {

    private final Environment environment;

    public MetalPricesLambdaStack(Construct scope, String id, StackInputParams inputParams) {
        super(
                scope,
                id,
                StackProps.builder()
                        .stackName(inputParams.stackName())
                        .env(inputParams.environment())
                        .build());

        this.environment = inputParams.environment();

        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("-yyyy-MM-dd-HH-mm-ss"));
        var logGroup = new LogGroup(
                this,
                "MetalPricesLambdaLogGroup",
                LogGroupProps.builder()
                        .logGroupName(inputParams.stackName() + suffix)
                        .retention(RetentionDays.ONE_WEEK)
                        .build());

        var function = Function.Builder.create(this, "MetalPricesLambdaFunction")
                .functionName(inputParams.stackName())
                .runtime(Runtime.JAVA_21)
                .code(software.amazon.awscdk.services.lambda.Code.fromAsset(
                        "../metal-prices-lambda/target/metal-prices-lambda-1.0.0-aws.jar"))
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .architecture(Architecture.X86_64)
                .memorySize(512)
                .timeout(Duration.seconds(15))
                .logGroup(logGroup)
                .build();

        IManagedPolicy ssmReadOnlyAccess = ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMReadOnlyAccess");
        IRole role = function.getRole();
        if (role != null) {
            role.addManagedPolicy(ssmReadOnlyAccess);
        }

        PolicyStatement allowSendingEmails = PolicyStatement.Builder.create()
                .sid("AllowSendingEmails")
                .effect(Effect.ALLOW)
                .resources(List.of(
                        setUpResource("arn:aws:ses:%s:%s:configuration-set/my-first-configuration-set"),
                        setUpResource("arn:aws:ses:%s:%s:identity/hugo.rad@gmail.com"),
                        setUpResource("arn:aws:ses:%s:%s:identity/brano.beno@gmail.com"),
                        setUpResource("arn:aws:ses:%s:%s:identity/ivana.chvojkova@gmail.com"),
                        setUpResource("arn:aws:ses:%s:%s:identity/b-l-s.click")))
                .actions(List.of("ses:SendEmail", "ses:SendRawEmail"))
                .build();
        function.addToRolePolicy(allowSendingEmails);
    }

    private @NotNull String setUpResource(String resourceArn) {
        return resourceArn.formatted(environment.getRegion(), environment.getAccount());
    }
}
