package com.serverless.lambda.metal.prices.domain;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MetalRates(boolean success, int timestamp, LocalDate date, @JsonProperty("base") String currency,
      String unit, Rates rates) {
}
