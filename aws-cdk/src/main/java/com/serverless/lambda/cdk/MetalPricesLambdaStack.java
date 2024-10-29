package com.serverless.lambda.cdk;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.lambda.Architecture;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

public class MetalPricesLambdaStack extends Stack {

    public MetalPricesLambdaStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Function.Builder.create(this, "metal-prices-lambda")
                .runtime(Runtime.JAVA_21)
                .code(software.amazon.awscdk.services.lambda.Code.fromAsset(
                        "../metal-prices-lambda/target/metal-prices-lambda-1.0.0-aws.jar"))
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .architecture(Architecture.X86_64)
                .build();
    }
}
