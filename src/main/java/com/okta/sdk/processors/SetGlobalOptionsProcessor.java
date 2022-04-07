package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.Map;

import static com.okta.sdk.OpenApiExtensions.SET_GLOBAL_OPTIONS;

public class SetGlobalOptionsProcessor implements Processor {
    private final Map<String, Object> parameters;

    public SetGlobalOptionsProcessor(Object parameters) {
        this.parameters = (Map<String, Object>) parameters;
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        spec.addExtension(SET_GLOBAL_OPTIONS, parameters);
    }
}
