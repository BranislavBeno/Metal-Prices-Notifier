package com.serverless.lambda.metal.prices.service;

public enum MetalType {
    LME_ALU("Aluminium"),
    LME_XCU("Copper"),
    LME_LEAD("Lead");

    private final String label;

    MetalType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
