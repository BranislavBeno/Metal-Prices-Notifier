package com.serverless.lambda.metal.prices.domain;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Rates(@JsonProperty("LME-ALU") BigDecimal aluminum, @JsonProperty("LME-XCU") BigDecimal copper,
      @JsonProperty("LME-LEAD") BigDecimal lead) {
}
