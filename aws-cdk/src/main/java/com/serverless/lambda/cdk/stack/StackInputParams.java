package com.serverless.lambda.cdk.stack;

import software.amazon.awscdk.Environment;

public record StackInputParams(Environment environment, String stackName) {}
