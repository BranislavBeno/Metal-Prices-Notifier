package com.serverless.lambda.metal.prices.api;

import com.serverless.lambda.metal.prices.domain.MetalRates;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import okhttp3.Headers;
import okhttp3.HttpUrl;
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
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        String url = mockWebServer.url("/").toString();
        webClient = new MetalExchangeWebClient(url, BASE, SYMBOLS, ACCESS_KEY);
    }

    @Test
    void testSuccessfulResponse() throws InterruptedException {
        mockWebServer.enqueue(
                new MockResponse(200, new Headers(new String[] {"Content-Type", "application/json"}), validResponse));

        MetalRates metalRates = webClient.fetchMetalRates();

        assertValidData(metalRates);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        HttpUrl url = recordedRequest.getUrl();
        assertThat(url.pathSegments()).containsExactly("latest", "");
        assertThat(url.encodedQuery())
                .isEqualTo("base=base%3DUSD&symbols=symbols%3DLME-ALU,LME-XCU,LME-LEAD&access_key=private-token");
    }

    @Test
    void testIncompleteResponse() {
        Assertions.assertThrows(IllegalStateException.class, this::mockIncompleteResponse);
    }

    @Test
    void testFailingResponse() {
        Assertions.assertThrows(IllegalStateException.class, this::mockWrongResponse);
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

    private void mockIncompleteResponse() {
        String response = """
                {
                  "success": true,
                  "rates": {
                    "LME-ALU": 10.573385811699,
                    "LME-XCU": 3.256136987247
                  }
                }""";

        mockWebServer.enqueue(
                new MockResponse(500, new Headers(new String[] {"Content-Type", "application/json"}), response));
        webClient.fetchMetalRates();
    }

    private void mockWrongResponse() {
        mockWebServer.enqueue(new MockResponse(500, new Headers(new String[] {}), "System failure!"));
        webClient.fetchMetalRates();
    }
}
