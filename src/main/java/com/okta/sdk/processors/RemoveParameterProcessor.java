package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.okta.sdk.OpenApiExtensions.REMOVE_PARAMETER;

public class RemoveParameterProcessor implements Processor {
    Set<String> parameters;

    @SuppressWarnings("unchecked")
    public RemoveParameterProcessor(Object parameters) {
        this.parameters = new HashSet<>((List<String>) parameters);
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        spec.addExtension(REMOVE_PARAMETER, parameters);
    }
}
