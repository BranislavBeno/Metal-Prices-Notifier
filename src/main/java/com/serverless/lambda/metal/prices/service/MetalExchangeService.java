package com.serverless.lambda.metal.prices.service;

import com.serverless.lambda.metal.prices.api.MetalExchangeWebClient;
import com.serverless.lambda.metal.prices.domain.MetalRates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.EnumMap;
import java.util.Map;

public record MetalExchangeService(MetalExchangeWebClient webClient) {

    private static final Logger LOG = LoggerFactory.getLogger(MetalExchangeService.class);
    private static final BigDecimal OUNCE_PER_TONNE = new BigDecimal("0.0000311034768");

    public Map<MetalType, BigDecimal> getMetalRates() {
        MetalRates rates = webClient.fetchMetalRates();
        LOG.info("Metal rates were successfully fetched on {}.", rates.date());

        Map<MetalType, BigDecimal> prices = new EnumMap<>(MetalType.class);
        prices.put(MetalType.LME_ALU, computePrice(rates.rates().aluminum()));
        prices.put(MetalType.LME_XCU, computePrice(rates.rates().copper()));
        prices.put(MetalType.LME_LEAD, computePrice(rates.rates().lead()));

        return prices;
    }

    private BigDecimal computePrice(BigDecimal input) {
        if (input == null) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(1).divide(input.multiply(OUNCE_PER_TONNE), MathContext.DECIMAL32);
    }
}
