package com.serverless.lambda.metal.prices.configuration;

import com.serverless.lambda.metal.prices.mail.EmailSender;
import com.serverless.lambda.metal.prices.service.MetalExchangeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.ISpringTemplateEngine;

import java.util.function.Supplier;

@Configuration
public class FunctionConfiguration {

    @Bean
    public Supplier<Void> sendMail(@Autowired ISpringTemplateEngine emailTemplateEngine,
                                   @Autowired MetalExchangeService exchangeService,
                                   @Value("${custom.mail.recipients}") String[] recipients,
                                   @Value("${custom.mail.sender}") String sender,
                                   @Value("${custom.metal.api.base}") String base) {
        new EmailSender(sender, recipients, base, emailTemplateEngine, exchangeService).sendMail();

        return () -> null;
    }
}
