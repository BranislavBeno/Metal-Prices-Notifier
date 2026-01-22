package com.serverless.lambda.cdk.stack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.CronOptions;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.ManagedPolicy;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.LogGroupProps;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

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

        Function function = getFunction(inputParams);
        IRole role = function.getRole();
        if (role != null) {
            role.addManagedPolicy(ManagedPolicy.fromAwsManagedPolicyName("AmazonSSMReadOnlyAccess"));
        }

        PolicyStatement allowSendingEmails = getAllowSendingEmails();
        function.addToRolePolicy(allowSendingEmails);

        Rule rule = getRule(inputParams);
        rule.addTarget(LambdaFunction.Builder.create(function).build());
    }

    private @NotNull Rule getRule(StackInputParams inputParams) {
        return Rule.Builder.create(this, "MetalPricesEventBridgeRule")
                .ruleName(inputParams.stackName() + "-rule")
                .schedule(Schedule.cron(CronOptions.builder()
                        .minute("0")
                        .hour("5")
                        .month("*")
                        .weekDay("MON-FRI")
                        .year("*")
                        .build()))
                .build();
    }

    private @NotNull Function getFunction(StackInputParams inputParams) {
        String suffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("-yyyy-MM-dd-HH-mm-ss"));
        var logGroup = new LogGroup(
                this,
                "MetalPricesLambdaLogGroup",
                LogGroupProps.builder()
                        .logGroupName(inputParams.stackName() + suffix)
                        .retention(RetentionDays.ONE_WEEK)
                        .build());

        return Function.Builder.create(this, "MetalPricesLambdaFunction")
                .functionName(inputParams.stackName())
                .runtime(Runtime.JAVA_25)
                .code(software.amazon.awscdk.services.lambda.Code.fromAsset(
                        "../metal-prices-lambda/target/metal-prices-lambda-1.0.0-aws.jar"))
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .architecture(Architecture.ARM_64)
                .memorySize(512)
                .timeout(Duration.seconds(15))
                .logGroup(logGroup)
                .build();
    }

    private @NotNull PolicyStatement getAllowSendingEmails() {
        return PolicyStatement.Builder.create()
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
    }

    private @NotNull String setUpResource(String resourceArn) {
        return resourceArn.formatted(environment.getRegion(), environment.getAccount());
    }
}
