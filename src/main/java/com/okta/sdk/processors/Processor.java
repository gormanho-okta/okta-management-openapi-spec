package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

public interface Processor {
    static Processor create(String type, Object parameters) {
        switch (type) {
            case "hideBaseMember":
                return new HideBaseMemberProcessor(parameters);
            case "removeParameter":
                return new RemoveParameterProcessor(parameters);
            case "renameApi":
                return new RenameApiProcessor(parameters);
            case "renameModel":
                return new RenameModelProcessor(parameters);
            case "renameRequestBody":
                return new RenameRequestBodyProcessor(parameters);
            case "setOperationTags":
                return new SetOperationTagsProcessor(parameters);
        }
        throw new RuntimeException("Unsupported processor type: " + type);
    }

    void process(OpenAPI spec, CodegenConfig config);
}
