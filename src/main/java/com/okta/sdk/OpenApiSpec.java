package com.okta.sdk;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.util.stream.Stream;

public abstract class OpenApiSpec {
    public static Stream<Operation> getOperations(OpenAPI spec) {
        return spec.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream());
    }
}
