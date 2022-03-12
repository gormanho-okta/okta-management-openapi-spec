package com.okta.sdk.generators;

import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.generators.dotnet.CSharpClientCodegen;
import io.swagger.v3.oas.models.media.Schema;

import static com.okta.sdk.OktaSdkExtensions.RENAME;

public class CSharpCodeGen extends CSharpClientCodegen {
    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "CSharpCodeGen";
    }

    @Override
    public String getHelp() {
        return "C# Code Generator";
    }

    @Override
    public String toModelName(String name) {
        String modelName = super.toModelName(name);
        if (openAPI != null
                && openAPI.getComponents() != null
                && openAPI.getComponents().getSchemas() != null) {
            Schema schema = openAPI.getComponents().getSchemas().get(name);
            if (schema != null && schema.getExtensions() != null) {
                String renameTo = (String) schema.getExtensions().get(RENAME);
                if (renameTo != null) {
                    return renameTo;
                }
            }
        }
        return modelName;
    }
}
