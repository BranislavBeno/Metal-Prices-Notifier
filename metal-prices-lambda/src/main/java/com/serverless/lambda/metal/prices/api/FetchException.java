package com.serverless.lambda.metal.prices.api;

public class FetchException extends RuntimeException {

    public FetchException(String message) {
        super(message);
    }
}
