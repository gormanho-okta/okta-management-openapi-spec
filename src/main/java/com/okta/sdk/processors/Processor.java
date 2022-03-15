package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;
import java.util.Map;

public interface Processor {
    static Processor create(String type, Object parameters) {
        switch (type) {
            case "rename":
                return new RenameProcessor(parameters);
            case "hideBaseMember":
                return new HideBaseMemberProcessor(parameters);
        }
        throw new RuntimeException("Unsupported processor type: " + type);
    }

    void process(OpenAPI spec, CodegenConfig config);
}
