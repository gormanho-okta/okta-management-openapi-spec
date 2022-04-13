package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;

public interface Processor {
    static Processor create(String type, Object parameters) {
        switch (type) {
            case "generateListModels":
                return new GenerateListModelsProcessor();
            case "hideBaseMember":
                return new HideBaseMemberProcessor(parameters);
            case "overrideParameterType":
                return new OverrideParameterTypeProcessor(parameters);
            case "overridePropertyType":
                return new OverridePropertyTypeProcessor(parameters);
            case "removeParameter":
                return new RemoveParameterProcessor(parameters);
            case "renameApi":
                return new RenameApiProcessor(parameters);
            case "renameModel":
                return new RenameModelProcessor(parameters);
            case "renameModelFile":
                return new RenameModelFileProcessor(parameters);
            case "renameParameter":
                return new RenameParameterProcessor(parameters);
            case "renameRequestBody":
                return new RenameRequestBodyProcessor(parameters);
            case "setGlobalOptions":
                return new SetGlobalOptionsProcessor(parameters);
            case "setOperationTags":
                return new SetOperationTagsProcessor(parameters);
        }
        throw new RuntimeException("Unsupported processor type: " + type);
    }

    void process(OpenAPI spec, CodegenConfig config);
}
