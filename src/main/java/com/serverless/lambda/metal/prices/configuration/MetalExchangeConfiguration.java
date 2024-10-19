package com.serverless.lambda.metal.prices.configuration;

import com.serverless.lambda.metal.prices.api.MetalExchangeWebClient;
import com.serverless.lambda.metal.prices.service.MetalExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetalExchangeConfiguration {

    @Bean
    public MetalExchangeWebClient metalExchangeWebClient(@Value("${custom.metal.api.url}") String url,
                                                         @Value("${custom.metal.api.access-key}") String accessKey) {
        return new MetalExchangeWebClient(url, accessKey);
    }

    @Bean
    public MetalExchangeService metalDataService(@Autowired MetalExchangeWebClient webClient) {
        return new MetalExchangeService(webClient);
    }
}
