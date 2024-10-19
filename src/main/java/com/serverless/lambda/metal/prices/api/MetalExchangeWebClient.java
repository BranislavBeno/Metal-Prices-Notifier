package com.serverless.lambda.metal.prices.api;

import com.serverless.lambda.metal.prices.domain.MetalRates;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

public class MetalExchangeWebClient {

    private final WebClient webClient;

    public MetalExchangeWebClient(String url, String accessKey) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(2))
                                .addHandlerLast(new WriteTimeoutHandler(2)));

        String fullUrl = url + accessKey;
        this.webClient = WebClient.builder()
                .baseUrl(fullUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public MetalRates fetchMetalRates() {
        return webClient.get()
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ClientApiUtils::propagateFetchingError)
                .onStatus(HttpStatusCode::is5xxServerError, ClientApiUtils::propagateServerError)
                .bodyToMono(MetalRates.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200)))
                .block();
    }
}
