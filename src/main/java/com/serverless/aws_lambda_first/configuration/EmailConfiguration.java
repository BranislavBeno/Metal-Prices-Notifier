package com.serverless.aws_lambda_first.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring6.ISpringTemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class EmailConfiguration {

   @Bean
   public ISpringTemplateEngine emailTemplateEngine() {
      SpringTemplateEngine result = new SpringTemplateEngine();
      result.addDialect(new Java8TimeDialect());
      result.addTemplateResolver(htmlTemplateResolver());

      return result;
   }

   @Bean
   public ITemplateResolver htmlTemplateResolver() {
      ClassLoaderTemplateResolver result = new ClassLoaderTemplateResolver();
      result.setPrefix("templates/");
      result.setSuffix(".html");
      result.setTemplateMode(TemplateMode.HTML);
      result.setCharacterEncoding("UTF-8");

      return result;
   }
}
