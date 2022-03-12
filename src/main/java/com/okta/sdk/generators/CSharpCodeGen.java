package com.okta.sdk.generators;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.generators.dotnet.CSharpClientCodegen;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Map;
import java.util.Set;

import static com.okta.sdk.OktaSdkExtensions.HIDE_BASE_MEMBER;
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

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        super.postProcessModelProperty(model, property);
        Map<String, Object> extensions = model.getVendorExtensions();
        if (extensions.containsKey(HIDE_BASE_MEMBER)) {
            Set<String> properties = (Set<String>) extensions.get(HIDE_BASE_MEMBER);
            if (properties.contains(property.getName())) {
                property.getVendorExtensions().put(HIDE_BASE_MEMBER, true);
            }
        }
    }
}
