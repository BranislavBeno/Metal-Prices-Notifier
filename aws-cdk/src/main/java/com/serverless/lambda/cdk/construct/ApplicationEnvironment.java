package com.serverless.lambda.cdk.construct;

import software.amazon.awscdk.Tags;
import software.constructs.IConstruct;

public record ApplicationEnvironment(String applicationName, String environmentName) {

    /**
     * Strips non-alphanumeric characters from a String since some AWS resources don't cope with them when using them in
     * resource names.
     */
    private String sanitize(String fullName) {
        return fullName.replaceAll("[^a-zA-Z0-9-]", "");
    }

    @Override
    public String toString() {
        return sanitize(applicationName + "-" + environmentName);
    }

    public String prefix(String string) {
        return this + "-" + string;
    }

    public String prefix(String string, int characterLimit) {
        String name = this + "-" + string;
        return name.length() <= characterLimit ? name : name.substring(name.length() - characterLimit);
    }

    public void tag(IConstruct construct) {
        Tags.of(construct).add("application", applicationName);
        Tags.of(construct).add("environment", environmentName);
    }
}
