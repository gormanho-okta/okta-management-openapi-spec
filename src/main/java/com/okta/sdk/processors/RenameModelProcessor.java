package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;

import static com.okta.sdk.OpenApiExtensions.RENAME_MODEL;

public class RenameModelProcessor extends RenameProcessor {
    public RenameModelProcessor(Object parameters) {
        super(parameters);
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        Map<String, Schema> schemas = spec.getComponents().getSchemas();
        forEachRename(rename -> {
            for (String schemaName : schemas.keySet()) {
                String modelName = config.toModelName(schemaName);
                if (modelName.equals(rename.get("from"))) {
                    schemas.get(schemaName).addExtension(RENAME_MODEL, rename.get("to"));
                }
            }
        });
    }
}
