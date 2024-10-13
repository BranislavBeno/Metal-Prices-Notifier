package com.serverless.lambda.metal.prices.configuration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FunctionConfigurationTest {

    @Value("${custom.mail.recipients}")
    private String[] recipients;

    @Test
    void getRecipients() {
        Assertions.assertThat(recipients).containsExactly("brano.beno@gmail.com", "hugo.rad@gmail.com");
    }
}