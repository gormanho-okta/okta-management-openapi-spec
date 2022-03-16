package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SetOperationTagsProcessor implements Processor {
    private final Map<String, Object> parameters;

    public SetOperationTagsProcessor(Object parameters) {
        this.parameters = (Map<String, Object>) parameters;
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        spec.getPaths().values().stream()
                .flatMap(path -> path.readOperations().stream())
                .filter(operation -> operation.getOperationId().matches((String) parameters.get("name")))
                .forEach(operation -> operation.setTags((List<String>) parameters.get("tags")));
    }
}
