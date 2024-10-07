package com.serverless.aws_lambda_first.configuration;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.serverless.aws_lambda_first.mail.EmailSender;

@Configuration
public class FunctionConfiguration {

   @Value("${custom.mail.recipient}")
   private String recipient;
   @Value("${custom.mail.sender}")
   private String sender;

   @Bean
   public Supplier<Void> sendMail() {
      new EmailSender(sender, recipient).sendMail();

      return () -> null;
   }
}
