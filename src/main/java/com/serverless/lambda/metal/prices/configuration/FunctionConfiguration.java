package com.serverless.lambda.metal.prices.configuration;

import com.serverless.lambda.metal.prices.mail.EmailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.ISpringTemplateEngine;

import java.util.function.Supplier;

@Configuration
public class FunctionConfiguration {

    @Value("${custom.mail.recipients}")
    private String[] recipients;
    @Value("${custom.mail.sender}")
    private String sender;

    @Bean
    public Supplier<Void> sendMail(@Autowired ISpringTemplateEngine emailTemplateEngine) {
        new EmailSender(sender, recipients, emailTemplateEngine).sendMail();

        return () -> null;
    }
}
