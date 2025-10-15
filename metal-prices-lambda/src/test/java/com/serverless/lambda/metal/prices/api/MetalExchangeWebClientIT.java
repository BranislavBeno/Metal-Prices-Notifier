package com.serverless.lambda.metal.prices.api;

import com.serverless.lambda.metal.prices.domain.MetalRates;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
@DisabledIfSystemProperty(named = "os.arch", matches = "aarch64", disabledReason = "No ARM64 support")
class MetalExchangeWebClientIT implements WithAssertions {

    private static final MockServerContainer MOCK_SERVER =
            new MockServerContainer(DockerImageName.parse("mockserver/mockserver"));
    private static final String URL;
    private static final String BASE = "base=USD";
    private static final String SYMBOLS = "symbols=LME-ALU,LME-XCU,LME-LEAD";
    private static final String ACCESS_KEY = "private-token";
    private static MetalExchangeWebClient webClient;

    static {
        MOCK_SERVER.start();
        URL = "http://%s:%s/api".formatted(MOCK_SERVER.getHost(), MOCK_SERVER.getServerPort());
    }

    @BeforeAll
    static void setUpAll() {
        webClient = new MetalExchangeWebClient(URL, BASE, SYMBOLS, ACCESS_KEY);
    }

    @Test
    void testFetchData() throws IOException {
        String json = readResourceFile();
        try (var mockServerClient = new MockServerClient(MOCK_SERVER.getHost(), MOCK_SERVER.getServerPort())) {
            mockResponse(mockServerClient, json);

            assertThat(webClient).isNotNull();
            MetalRates metalRates = webClient.fetchMetalRates();

            assertThat(metalRates).isNotNull();
            assertThat(metalRates.success()).isTrue();
            assertThat(metalRates.rates().aluminum()).isEqualTo(new BigDecimal("10.573385811699"));
            assertThat(metalRates.rates().copper()).isEqualTo(new BigDecimal("3.256136987247"));
            assertThat(metalRates.rates().lead()).isEqualTo(new BigDecimal("14.319008911883"));
            assertThat(metalRates.currency()).isEqualTo("USD");
            assertThat(metalRates.date()).isBeforeOrEqualTo(LocalDate.now());
        }
    }

    private void mockResponse(MockServerClient mockClient, String json) {
        mockClient
                .when(HttpRequest.request().withMethod("GET"))
                .respond(new HttpResponse()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json));
    }

    private String readResourceFile() throws IOException {
        File epicsFile = new ClassPathResource("/payload/response.json").getFile();
        return FileUtils.readFileToString(epicsFile, StandardCharsets.UTF_8);
    }
}
