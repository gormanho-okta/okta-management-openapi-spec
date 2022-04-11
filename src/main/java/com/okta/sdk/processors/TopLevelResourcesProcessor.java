package com.okta.sdk.processors;

import com.okta.sdk.OpenApiSpec;
import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.okta.sdk.OpenApiExtensions.TOP_LEVEL_RESOURCES;
import static com.okta.sdk.OpenApiSpec.getContentSchema;
import static com.okta.sdk.OpenApiSpec.getOperations;
import static com.okta.sdk.OpenApiSpec.refToSimpleName;

public class TopLevelResourcesProcessor implements Processor {
    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        Set<String> topLevelResources = new HashSet<>();

        getOperations(spec)
                .map(OpenApiSpec::getRequestBodyType)
                .filter(Objects::nonNull)
                .forEach(topLevelResources::add);

        getOperations(spec)
                .flatMap(operation -> operation.getResponses().entrySet().stream())
                .filter(entry -> "200".equals(entry.getKey()) && entry.getValue().getContent() != null)
                .map(entry -> getContentSchema(entry.getValue().getContent()))
                .filter(schema -> schema instanceof ArraySchema)
                .forEach(schema -> topLevelResources.add(refToSimpleName(((ArraySchema) schema).getItems().get$ref())));

        spec.getComponents().getSchemas().forEach((name, model) -> {
            String parent = getParentModelRef(model);
            if (parent != null) {
                parent = refToSimpleName(parent);
                if (topLevelResources.contains(parent)) {
                    topLevelResources.add(name);
                }
            }
        });

        Map<String, Schema> schemas = spec.getComponents().getSchemas();
        topLevelResources.forEach(resource -> schemas.get(resource).addExtension("top-level", true));
        spec.addExtension(TOP_LEVEL_RESOURCES, topLevelResources);
    }

    private String getParentModelRef(Schema model) {
        if (model instanceof ComposedSchema) {
            // Assumes the first entry is the parent.
            ComposedSchema composed = (ComposedSchema) model;
            if (composed.getAllOf() != null && !composed.getAllOf().isEmpty()) {
                return composed.getAllOf().get(0).get$ref();
            }
        }
        return null;
    }
}
