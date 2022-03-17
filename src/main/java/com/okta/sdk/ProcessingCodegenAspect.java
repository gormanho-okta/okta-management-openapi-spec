package com.okta.sdk;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.oas.models.media.Schema;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Map;
import java.util.Set;

import static com.okta.sdk.OpenApiExtensions.HIDE_BASE_MEMBER;
import static com.okta.sdk.OpenApiExtensions.RENAME_MODEL;

@Aspect
public class ProcessingCodegenAspect {
    @Around("call(String io.swagger.codegen.v3.CodegenConfig+.toModelName(String)) && args(name)")
    public String toModelName(ProceedingJoinPoint joinPoint, String name) throws Throwable {
        String modelName = (String) joinPoint.proceed(new Object[]{name});
        DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();
        if (codegen.getOpenAPI() != null
                && codegen.getOpenAPI().getComponents() != null
                && codegen.getOpenAPI().getComponents().getSchemas() != null) {
            Schema schema = codegen.getOpenAPI().getComponents().getSchemas().get(name);
            if (schema != null && schema.getExtensions() != null) {
                String rename = (String) schema.getExtensions().get(RENAME_MODEL);
                if (rename != null) {
                    modelName = rename;
                }
            }
        }
        return modelName;
    }

    @After("call(void io.swagger.codegen.v3.CodegenConfig+.postProcessModelProperty(CodegenModel, CodegenProperty)) && args(model, property)")
    public void postProcessModelProperty(JoinPoint joinPoint, CodegenModel model, CodegenProperty property) {
        Map<String, Object> extensions = model.getVendorExtensions();
        if (extensions.containsKey(HIDE_BASE_MEMBER)) {
            Set<String> properties = (Set<String>) extensions.get(HIDE_BASE_MEMBER);
            if (properties.contains(property.getName())) {
                property.getVendorExtensions().put(HIDE_BASE_MEMBER, true);
            }
        }
    }
}
