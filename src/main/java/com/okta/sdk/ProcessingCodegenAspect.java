package com.okta.sdk;

import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.okta.sdk.OpenApiExtensions.HIDE_BASE_MEMBER;
import static com.okta.sdk.OpenApiExtensions.REMOVE_PARAMETER;
import static com.okta.sdk.OpenApiExtensions.RENAME_API;
import static com.okta.sdk.OpenApiExtensions.RENAME_MODEL;

@Aspect
@SuppressWarnings("unchecked")
public class ProcessingCodegenAspect {
    @Around("execution(String io.swagger.codegen.v3.CodegenConfig+.toModelName(String)) && args(name)")
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

    @After("execution(void io.swagger.codegen.v3.CodegenConfig+.postProcessModelProperty(CodegenModel, CodegenProperty)) && args(model, property)")
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        Map<String, Object> extensions = model.getVendorExtensions();
        if (extensions.containsKey(HIDE_BASE_MEMBER)) {
            Set<String> properties = (Set<String>) extensions.get(HIDE_BASE_MEMBER);
            if (properties.contains(property.getName())) {
                property.getVendorExtensions().put(HIDE_BASE_MEMBER, true);
            }
        }
    }

    @Around("execution(String io.swagger.codegen.v3.CodegenConfig+.toApiName(String)) && args(name)")
    public String toApiName(ProceedingJoinPoint joinPoint, String name) throws Throwable {
        String apiName = (String) joinPoint.proceed(new Object[]{name});
        DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();
        Map<String, String> renames = getSpecExtension(codegen.getOpenAPI(), RENAME_API, Collections.emptyMap());
        if (renames.containsKey(apiName)) {
            apiName = renames.get(apiName);
        }
        return apiName;
    }

    @After("execution(void preprocessOpenAPI(OpenAPI)) && args(spec)")
    public void preprocessOpenAPI(OpenAPI spec) {
        Set<String> remove = getSpecExtension(spec, REMOVE_PARAMETER, Collections.emptySet());
        spec.getPaths().values().stream()
                .flatMap(pathItem -> pathItem.readOperations().stream())
                .map(Operation::getParameters)
                .filter(Objects::nonNull)
                .forEach(parameters -> parameters.removeIf(parameter ->
                        !parameter.getRequired() && remove.contains(parameter.getName())));
    }

    private <T> T getSpecExtension(OpenAPI spec, String name, T defaultValue) {
        if (spec != null && spec.getExtensions() != null && spec.getExtensions().containsKey(name)) {
            return (T) spec.getExtensions().get(name);
        }
        return defaultValue;
    }
}
