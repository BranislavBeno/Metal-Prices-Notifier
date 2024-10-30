package com.serverless.lambda.cdk;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.IManagedPolicy;
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

import java.util.List;

public class MetalPricesLambdaStack extends Stack {

    public static final String FUNCTION_NAME = "metal-prices-lambda";

    public MetalPricesLambdaStack(
            Construct scope, String id, StackProps props, List<PolicyStatement> policyStatements) {
        super(scope, id, props);

        var logGroup = new LogGroup(
                this,
                "MetalPricesLambdaLogGroup",
                LogGroupProps.builder()
                        .logGroupName("log-group-" + FUNCTION_NAME)
                        .retention(RetentionDays.ONE_WEEK)
                        .build());

        var function = Function.Builder.create(this, "MetalPricesLambdaFunction")
                .functionName(FUNCTION_NAME)
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
        policyStatements.forEach(function::addToRolePolicy);
    }
}
