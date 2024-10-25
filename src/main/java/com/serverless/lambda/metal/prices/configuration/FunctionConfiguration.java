package com.serverless.lambda.metal.prices.configuration;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import com.serverless.lambda.metal.prices.mail.EmailSender;
import com.serverless.lambda.metal.prices.service.MetalExchangeService;

@Configuration
public class FunctionConfiguration {

   @Bean
   public Supplier<Void> sendMail(@Autowired SsmParamsProvider paramsProvider,
                                  @Autowired ISpringTemplateEngine emailTemplateEngine,
                                  @Autowired MetalExchangeService exchangeService,
                                  @Value("${custom.mail.sender}") String sender) {
      new EmailSender(sender, paramsProvider, emailTemplateEngine, exchangeService).sendMail();

      return () -> null;
   }
}
