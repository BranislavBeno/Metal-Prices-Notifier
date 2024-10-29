package com.serverless.lambda.metal.prices.api;

import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

public class ClientApiUtils {

    public static final String SERVER_ERROR_OCCURRED = "Server error occurred";

    private ClientApiUtils() {}

    static Mono<Throwable> propagateServerError(ClientResponse response) {
        return response.bodyToMono(Throwable.class);
    }

    static Mono<Throwable> propagateFetchingError(ClientResponse response) {
        Mono<String> errorMsg = response.bodyToMono(String.class);
        return errorMsg.flatMap(msg -> Mono.error(new FetchException(SERVER_ERROR_OCCURRED)));
    }
}
