package com.okta.sdk.processors;

import com.okta.sdk.OpenApiSpec;
import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.okta.sdk.OpenApiExtensions.GENERATE_LIST_MODELS;
import static com.okta.sdk.OpenApiExtensions.TOP_LEVEL_RESOURCES;
import static com.okta.sdk.OpenApiSpec.getExtension;
import static com.okta.sdk.OpenApiSpec.getOperations;

public class GenerateListModelsProcessor implements Processor {
    private Set<String> languageSpecificPrimitives;
    private Set<String> topLevelResources;

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        languageSpecificPrimitives = config.languageSpecificPrimitives();
        topLevelResources = (Set<String>) spec.getExtensions().get(TOP_LEVEL_RESOURCES);

        List<Schema> properties = getOperations(spec)
                .map(OpenApiSpec::getSuccessResponseSchema)
                .collect(Collectors.toList());
        Map<String, Schema> listSchemas = processListsFromProperties(spec, properties);

        spec.getComponents().getSchemas().entrySet().stream()
                .filter(entry -> isTopLevelResourceName(entry.getKey()))
                .map(Map.Entry::getValue)
                .filter(model -> model != null && model.getProperties() != null)
                .forEach(model -> listSchemas.putAll(processListsFromProperties(spec, model.getProperties().values(), model)));

        listSchemas.forEach((key, value) -> spec.getComponents().addSchemas(key, value));
        spec.addExtension(GENERATE_LIST_MODELS, true);
    }

    private Map<String, Schema> processListsFromProperties(OpenAPI spec, Collection<Schema> properties) {
        return processListsFromProperties(spec, properties, null);
    }

    private Map<String, Schema> processListsFromProperties(OpenAPI spec, Collection<Schema> properties, Schema baseModel) {
        Map<String, Schema> result = new LinkedHashMap<>();
        properties.stream()
                .filter(OpenApiSpec::isItemArray)
                .map(OpenApiSpec::getArrayItemRef)
                .map(OpenApiSpec::refToSimpleName)
                .filter(this::isListResourceName)
                .map(name -> createListSchema(spec, name, baseModel))
                .forEach(schema -> result.put(schema.getName(), schema));
        return result;
    }

    private boolean isTopLevelResourceName(String name) {
        return topLevelResources.contains(name);
    }

    private boolean isListResourceName(String name) {
        return !languageSpecificPrimitives.contains(name) && isTopLevelResourceName(name);
    }

    private ObjectSchema createListSchema(OpenAPI spec, String baseName, Schema baseSchema) {
        ObjectSchema schema = new ObjectSchema();
        schema.setName(baseName + "List");
        schema.setType(schema.getName());
        schema.setDescription("Collection List for " + baseName);
        schema.addExtension("x-isResourceList", true);
        schema.addExtension("x-baseType", baseName);

        if (baseSchema == null) {
            baseSchema = spec.getComponents().getSchemas().get(baseName);
        }

        Object tags = getExtension(baseSchema, "x-okta-tags");
        if (tags != null) {
            schema.addExtension("x-okta-tags", tags);
        }

        return schema;
    }
}
