package com.serverless.lambda.cdk.util;

import com.serverless.lambda.cdk.construct.ApplicationEnvironment;
import software.amazon.awscdk.Environment;

public class CdkUtil {

    private CdkUtil() {}

    public static Environment makeEnv(String account, String region) {
        return Environment.builder().account(account).region(region).build();
    }

    public static String createStackName(String stack, ApplicationEnvironment appEnvironment) {
        return "%s-%s-%s".formatted(appEnvironment.applicationName(), stack, appEnvironment.environmentName());
    }
}
