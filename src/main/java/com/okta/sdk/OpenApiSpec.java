package com.okta.sdk;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;

import java.util.stream.Stream;

public abstract class OpenApiSpec {
    public static Object getExtension(Schema schema, String name) {
        if (schema.getExtensions() != null) {
            return schema.getExtensions().get(name);
        }
        return null;
    }

    public static String refToSimpleName(String ref) {
        return ref == null ? null : ref.substring(ref.lastIndexOf("/") + 1);
    }

    public static Stream<Operation> getOperations(OpenAPI spec) {
        return spec.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream());
    }

    public static String getRequestBodyType(Operation operation) {
        if (operation.getRequestBody() != null) {
            Schema schema = getContentSchema(operation.getRequestBody().getContent());
            return schema == null ? null : refToSimpleName(schema.get$ref());
        }
        return null;
    }

    public static Schema getContentSchema(Content content) {
        if (content != null) {
            MediaType mediaType = content.get("application/json");
            if (mediaType != null) {
                return mediaType.getSchema();
            }
        }
        return null;
    }

    public static Schema getSuccessResponseSchema(Operation operation) {
        if (operation != null
                && operation.getResponses() != null
                && operation.getResponses().get("200") != null) {
            return getContentSchema(operation.getResponses().get("200").getContent());
        }
        return null;
    }

    public static Schema getArrayItemSchema(Schema schema) {
        return ((ArraySchema) schema).getItems();
    }

    public static boolean isItemArray(Schema schema) {
        return schema instanceof ArraySchema && getArrayItemSchema(schema) != null;
    }

    public static String getArrayItemRef(Schema schema) {
        if (schema instanceof ArraySchema && ((ArraySchema) schema).getItems() != null) {
            return ((ArraySchema) schema).getItems().get$ref();
        }
        return null;
    }
}
