package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;
import java.util.Map;

import static com.okta.sdk.OpenApiExtensions.OVERRIDE_PROPERTY_TYPE;

public class OverridePropertyTypeProcessor implements Processor {
    private final List<Map<String, String>> overrides;

    public OverridePropertyTypeProcessor(Object parameters) {
        this.overrides = (List<Map<String, String>>) parameters;
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        spec.addExtension(OVERRIDE_PROPERTY_TYPE, overrides);
    }
}
