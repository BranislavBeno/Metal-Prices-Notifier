package com.serverless.lambda.metal.prices.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.serverless.lambda.metal.prices.api.MetalExchangeWebClient;
import com.serverless.lambda.metal.prices.service.MetalExchangeService;

@Configuration
public class MetalExchangeConfiguration {

   @Bean
   public MetalExchangeWebClient metalExchangeWebClient(@Autowired SsmParamsProvider paramsProvider) {
      return new MetalExchangeWebClient(paramsProvider);
   }

   @Bean
   public MetalExchangeService metalExchangeService(@Autowired MetalExchangeWebClient webClient) {
      return new MetalExchangeService(webClient);
   }
}
