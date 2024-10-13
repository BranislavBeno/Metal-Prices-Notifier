package com.serverless.lambda.metal.prices.mail;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.ISpringTemplateEngine;

import java.time.LocalDate;
import java.util.Locale;

@SpringBootTest
class EmailSenderTest implements WithAssertions {

    private static Context ctx;
    @Autowired
    private ISpringTemplateEngine emailTemplateEngine;

    @BeforeAll
    static void setUp() {
        ctx = new Context(Locale.US);
        ctx.setVariable("name", "Hugo");
        ctx.setVariable("reportDate", LocalDate.now());
    }

    @Test
    void testHtmlBody() {
        String body = emailTemplateEngine.process("email-template.html", ctx);
        assertThat(body).isNotEmpty();
    }
}