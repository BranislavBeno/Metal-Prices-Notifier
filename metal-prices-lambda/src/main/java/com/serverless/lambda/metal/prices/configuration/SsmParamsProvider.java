package com.serverless.lambda.metal.prices.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterType;

public final class SsmParamsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SsmParamsProvider.class);

    private final Region region;
    private final String[] recipients;
    private final String url;
    private final String base;
    private final String symbols;
    private final String accessKey;

    public SsmParamsProvider(Region region) {
        SsmClient ssmClient = SsmClient.builder().region(region).build();

        this.region = region;
        this.recipients = getParamList(ssmClient);
        this.url = getParamValue(ssmClient, "metal-prices.metals.api.url");
        this.base = getParamValue(ssmClient, "metal-prices.metals.api.base");
        this.symbols = getParamValue(ssmClient, "metal-prices.metals.api.symbols");
        this.accessKey = getParamValue(ssmClient, "metal-prices.metals.api.access-key");
    }

    private static String getParamValue(SsmClient client, String paramName) {
        return getParam(client, paramName).value();
    }

    private static String[] getParamList(SsmClient client) {
        Parameter parameter = getParam(client, "metal-prices.mail.recipients");
        ParameterType type = parameter.type();

        return type.equals(ParameterType.STRING_LIST) ? parameter.value().split(",", 0) : new String[] {};
    }

    private static Parameter getParam(SsmClient client, String paramName) {
        GetParameterRequest parameterRequest = GetParameterRequest.builder()
                .name(paramName)
                .withDecryption(true)
                .build();
        GetParameterResponse response = client.getParameter(parameterRequest);
        LOGGER.info(
                "SSM Parameter {} was read with result: '{}'",
                paramName,
                response.parameter().value());

        return response.parameter();
    }

    public Region getRegion() {
        return region;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public String getUrl() {
        return url;
    }

    public String getBase() {
        return base;
    }

    public String getSymbols() {
        return symbols;
    }

    public String getAccessKey() {
        return accessKey;
    }
}
