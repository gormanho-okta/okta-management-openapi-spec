package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.Map;

import static com.okta.sdk.OpenApiExtensions.SET_BOOLEAN_OPERATION_PREFIX;

public class SetBooleanOperationPrefixProcessor implements Processor {
    private final Map<String, Object> parameters;

    public SetBooleanOperationPrefixProcessor(Object parameters) {
        this.parameters = (Map<String, Object>) parameters;
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        spec.addExtension(SET_BOOLEAN_OPERATION_PREFIX, parameters.get("prefix"));
    }
}
