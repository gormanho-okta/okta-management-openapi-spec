package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.RequestBody;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.okta.sdk.OpenApiSpec.getOperations;

public class RenameRequestBodyProcessor extends RenameProcessor {
    public RenameRequestBodyProcessor(Object parameters) {
        super(parameters);
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        Map<String, Operation> operations = getOperations(spec)
                .collect(Collectors.toMap(Operation::getOperationId, Function.identity()));
        getRenames().forEach(rename -> {
            Operation operation = operations.get(rename.get("operation"));
            if (operation != null) {
                RequestBody requestBody = operation.getRequestBody();
                if (requestBody != null) {
                    requestBody.addExtension("x-codegen-request-body-name", rename.get("to"));
                }
            }
        });
    }
}
