package com.serverless.lambda.metal.prices.service;

import com.serverless.lambda.metal.prices.api.MetalExchangeWebClient;
import com.serverless.lambda.metal.prices.domain.MetalRates;
import com.serverless.lambda.metal.prices.domain.Rates;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MetalExchangeServiceTest {

    @Mock
    private MetalExchangeWebClient webClient;

    @Mock
    private MetalRates metalRates;

    @Mock
    private Rates rates;

    @InjectMocks
    private MetalExchangeService service;

    @Test
    void testMetalRatesGetting() {
        Mockito.when(webClient.fetchMetalRates()).thenReturn(metalRates);
        Mockito.when(metalRates.rates()).thenReturn(rates);
        Mockito.when(rates.aluminum()).thenReturn(BigDecimal.valueOf(1));
        Mockito.when(rates.copper()).thenReturn(BigDecimal.valueOf(2));
        Mockito.when(rates.lead()).thenReturn(null);
        service.getMetalRates();
        Mockito.verify(webClient).fetchMetalRates();
    }
}
