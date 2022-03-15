package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.okta.sdk.OpenApiExtensions.RENAME;

public class RenameProcessor implements Processor {
    private Map<String, Object> parameters;

    @SuppressWarnings("unchecked")
    public RenameProcessor(Object parameters) {
        this.parameters = (Map<String, Object>) parameters;
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        renameModels(spec, config, parameters.get("models"));
        renameRequestBodies(spec, parameters.get("requestBodies"));
    }

    private void renameModels(OpenAPI spec, CodegenConfig config, Object renames) {
        Map<String, Schema> schemas = spec.getComponents().getSchemas();
        forEachRename(renames, rename -> {
            for (String schemaName : schemas.keySet()) {
                String modelName = config.toModelName(schemaName);
                if (modelName.equals(rename.get("from"))) {
                    schemas.get(schemaName).addExtension(RENAME, rename.get("to"));
                }
            }
        });
    }

    private void renameRequestBodies(OpenAPI spec, Object renames) {
        Map<String, Operation> operations = spec.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .collect(Collectors.toMap(Operation::getOperationId, Function.identity()));
        forEachRename(renames, rename -> {
            Operation operation = operations.get(rename.get("operation"));
            if (operation != null) {
                RequestBody requestBody = operation.getRequestBody();
                if (requestBody != null) {
                    requestBody.addExtension("x-codegen-request-body-name", rename.get("to"));
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void forEachRename(Object renames, Consumer<Map<String, String>> rename) {
        if (renames != null) {
            ((List<Map<String, String>>) renames).forEach(rename);
        }
    }
}
