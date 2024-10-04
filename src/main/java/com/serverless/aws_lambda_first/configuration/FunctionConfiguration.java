package com.serverless.aws_lambda_first.configuration;

import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FunctionConfiguration {

   private static final Logger LOGGER = LoggerFactory.getLogger(FunctionConfiguration.class);

   @Bean
   public Function<String, String> reverse() {
      LOGGER.info("Invoked function 'reverse'.");
      return s -> new StringBuilder(s).reverse().toString();
   }
}
