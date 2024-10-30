package com.serverless.lambda.cdk;

import java.util.List;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

public class MetalPricesLambdaStack extends Stack {

    public MetalPricesLambdaStack(
            Construct scope, String id, StackProps props, List<PolicyStatement> policyStatements) {
        super(scope, id, props);

        var function = Function.Builder.create(this, "metal-prices-lambda")
                .runtime(Runtime.JAVA_21)
                .code(software.amazon.awscdk.services.lambda.Code.fromAsset(
                        "../metal-prices-lambda/target/metal-prices-lambda-1.0.0-aws.jar"))
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .architecture(Architecture.X86_64)
                .logRetention(RetentionDays.ONE_WEEK)
                .build();

        policyStatements.forEach(function::addToRolePolicy);
    }
}
