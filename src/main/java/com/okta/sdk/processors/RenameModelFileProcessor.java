package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.stream.Collectors;

import static com.okta.sdk.OpenApiExtensions.RENAME_MODEL_FILE;

public class RenameModelFileProcessor extends RenameProcessor {
    public RenameModelFileProcessor(Object parameters) {
        super(parameters);
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        spec.addExtension(RENAME_MODEL_FILE, getRenames().
                collect(Collectors.toMap(r -> r.get("from"), r -> r.get("to"))));
    }
}
