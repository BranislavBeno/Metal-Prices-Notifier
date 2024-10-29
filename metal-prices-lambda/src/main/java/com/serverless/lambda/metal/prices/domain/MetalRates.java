package com.serverless.lambda.metal.prices.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record MetalRates(
        boolean success,
        int timestamp,
        LocalDate date,
        @JsonProperty("base") String currency,
        String unit,
        Rates rates) {}
