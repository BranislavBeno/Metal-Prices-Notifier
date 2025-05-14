package com.serverless.lambda.metal.prices.api;

import com.serverless.lambda.metal.prices.domain.MetalRates;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MetalExchangeWebClientTest implements WithAssertions {

    private static final String BASE = "base=USD";
    private static final String SYMBOLS = "symbols=LME-ALU,LME-XCU,LME-LEAD";
    private static final String ACCESS_KEY = "private-token";
    private static final BigDecimal EXPECTED_ALUMINIUM = new BigDecimal("10.573385811699");
    private static final BigDecimal EXPECTED_COPPER = new BigDecimal("3.256136987247");
    private static final BigDecimal EXPECTED_LEAD = new BigDecimal("14.319008911883");
    private static String validResponse;

    static {
        try {
            validResponse = new String(Objects.requireNonNull(MetalExchangeWebClientTest.class
                            .getClassLoader()
                            .getResourceAsStream("payload/response.json"))
                    .readAllBytes());
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(MetalExchangeWebClientTest.class);
            logger.error(e.getMessage());
        }
    }

    private MockWebServer mockWebServer;
    private MetalExchangeWebClient webClient;

    @BeforeEach
    void setup() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        String url = mockWebServer.url("/").toString();
        this.webClient = new MetalExchangeWebClient(url, BASE, SYMBOLS, ACCESS_KEY);
    }

    @Test
    void testSuccessfulResponse() throws InterruptedException {
        MockResponse mockResponse =
                new MockResponse().addHeader("Content-Type", "application/json").setBody(validResponse);
        this.mockWebServer.enqueue(mockResponse);

        MetalRates metalRates = webClient.fetchMetalRates();

        assertValidData(metalRates);

        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        assertThat(recordedRequest.getPath())
                .isEqualTo(
                        "/latest/?base=base%3DUSD&symbols=symbols%3DLME-ALU,LME-XCU,LME-LEAD&access_key=private-token");
    }

    @Test
    void testIncompleteSuccessfulResponse() {
        String response =
                """
                {
                  "success": true,
                  "rates": {
                    "LME-ALU": 10.573385811699,
                    "LME-XCU": 3.256136987247
                  }
                }""";

        this.mockWebServer.enqueue(new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(response));

        MetalRates metalRates = webClient.fetchMetalRates();

        assertIncompleteValidData(metalRates);
    }

    @Test
    void testFailingResponse() {
        Assertions.assertThrows(RuntimeException.class, this::fetchMockedData);
    }

    private void assertValidData(MetalRates rates) {
        assertThat(rates).isNotNull();
        assertThat(rates.success()).isTrue();
        assertThat(rates.rates().aluminum()).isEqualTo(EXPECTED_ALUMINIUM);
        assertThat(rates.rates().copper()).isEqualTo(EXPECTED_COPPER);
        assertThat(rates.rates().lead()).isEqualTo(EXPECTED_LEAD);
        assertThat(rates.currency()).isEqualTo("USD");
        assertThat(rates.date()).isBeforeOrEqualTo(LocalDate.now());
    }

    private void assertIncompleteValidData(MetalRates rates) {
        assertThat(rates).isNotNull();
        assertThat(rates.success()).isTrue();
        assertThat(rates.rates().aluminum()).isEqualTo(EXPECTED_ALUMINIUM);
        assertThat(rates.rates().copper()).isEqualTo(EXPECTED_COPPER);
        assertThat(rates.rates().lead()).isNull();
        assertThat(rates.currency()).isNull();
        assertThat(rates.date()).isNull();
    }

    private void fetchMockedData() {
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("System failure!"));
        webClient.fetchMetalRates();
    }
}
