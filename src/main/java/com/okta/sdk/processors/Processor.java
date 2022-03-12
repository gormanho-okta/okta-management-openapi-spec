package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;
import java.util.Map;

public interface Processor {
    static Processor create(String type, List<Map<String, String>> parameters) {
        switch (type.toLowerCase()) {
            case "rename":
                return new RenameProcessor(parameters);
        }
        throw new RuntimeException("Unsupported processor type: " + type);
    }

    void process(OpenAPI spec, CodegenConfig config);
}
