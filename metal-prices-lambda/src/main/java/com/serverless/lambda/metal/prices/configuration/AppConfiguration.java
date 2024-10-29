package com.serverless.lambda.metal.prices.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

@Configuration
public class AppConfiguration {

    @Bean
    public SsmParamsProvider ssmParamsProvider() {
        return new SsmParamsProvider(Region.EU_CENTRAL_1);
    }
}
