package com.okta.sdk;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.helper.ConditionalHelpers;
import com.github.jknack.handlebars.helper.StringHelpers;
import io.swagger.codegen.v3.CodegenModel;
import io.swagger.codegen.v3.CodegenOperation;
import io.swagger.codegen.v3.CodegenParameter;
import io.swagger.codegen.v3.CodegenProperty;
import io.swagger.codegen.v3.generators.DefaultCodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.okta.sdk.OpenApiExtensions.GENERATE_LIST_MODELS;
import static com.okta.sdk.OpenApiExtensions.HIDE_BASE_MEMBER;
import static com.okta.sdk.OpenApiExtensions.OVERRIDE_PARAMETER_TYPE;
import static com.okta.sdk.OpenApiExtensions.OVERRIDE_PROPERTY_TYPE;
import static com.okta.sdk.OpenApiExtensions.REMOVE_PARAMETER;
import static com.okta.sdk.OpenApiExtensions.RENAME_API;
import static com.okta.sdk.OpenApiExtensions.RENAME_MODEL;
import static com.okta.sdk.OpenApiExtensions.RENAME_PARAMETER;
import static com.okta.sdk.OpenApiExtensions.SET_GLOBAL_OPTIONS;
import static com.okta.sdk.OpenApiExtensions.TOP_LEVEL_RESOURCES;
import static com.okta.sdk.OpenApiSpec.getArrayItemSchema;
import static com.okta.sdk.OpenApiSpec.getOperations;
import static com.okta.sdk.OpenApiSpec.isItemArray;

@Aspect
@SuppressWarnings("unchecked")
public class ProcessingCodegenAspect {
    @Around("execution(void addHandlebarHelpers(Handlebars)) && args(handlebars)")
    public void addHandlebarHelpers(ProceedingJoinPoint joinPoint, Handlebars handlebars) throws Throwable {
        joinPoint.proceed();
        handlebars.registerHelpers(ConditionalHelpers.class);
        handlebars.registerHelpers(StringHelpers.class);
    }

    @Around("execution(CodegenModel fromModel(String, Schema, Map<String, Schema>)) && args(name, model, allDefinitions)")
    public CodegenModel fromModel(ProceedingJoinPoint joinPoint, String name, Schema model, Map<String, Schema> allDefinitions) throws Throwable {
        CodegenModel codegenModel = (CodegenModel) joinPoint.proceed(new Object[]{name, model, allDefinitions});
        if (model.getExtensions() != null && model.getExtensions().containsKey("x-baseType")) {
            String baseType = (String) model.getExtensions().get("x-baseType");
            DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();
            codegenModel.vendorExtensions.put("baseType", codegen.toModelName(baseType));
        }
        return codegenModel;
    }

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

    @Around("execution(void io.swagger.codegen.v3.CodegenConfig+.postProcessModelProperty(CodegenModel, CodegenProperty)) && args(model, property)")
    public void postProcessModelProperty(ProceedingJoinPoint joinPoint, CodegenModel model, CodegenProperty property) {
        Map<String, Object> extensions = model.getVendorExtensions();
        if (extensions.containsKey(HIDE_BASE_MEMBER)) {
            Set<String> properties = (Set<String>) extensions.get(HIDE_BASE_MEMBER);
            if (properties.contains(property.getName())) {
                property.getVendorExtensions().put(HIDE_BASE_MEMBER, true);
            }
        }

        DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();
        List<Map<String, String>> overrides = getSpecExtension(codegen.getOpenAPI(), OVERRIDE_PROPERTY_TYPE, Collections.emptyList());
        for (Map<String, String> override : overrides) {
            if (override.containsKey("model")
                    && override.containsKey("property")
                    && model.getName().equals(override.get("model"))
                    && property.getName().equals(override.get("property"))) {
                property.datatypeWithEnum = override.get("type");
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
        getOperations(spec)
                .map(Operation::getParameters)
                .filter(Objects::nonNull)
                .forEach(parameters -> parameters.removeIf(parameter ->
                        !parameter.getRequired() && remove.contains(parameter.getName())));
    }

    @Around("execution(String toVarName(String)) && args(name)")
    public String toVarName(ProceedingJoinPoint joinPoint, String name) throws Throwable {
        String varName = (String) joinPoint.proceed(new Object[]{name});
        DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();
        Map<String, String> renames = getSpecExtension(codegen.getOpenAPI(), RENAME_PARAMETER, Collections.emptyMap());
        if (renames.containsKey(varName)) {
            varName = renames.get(varName);
        }
        return varName;
    }

    @Around("execution(String toBooleanGetter(String)) && args(name)")
    public String toBooleanGetter(ProceedingJoinPoint joinPoint, String name) throws Throwable {
        DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();
        String prefix = getSpecGlobalOption(codegen.getOpenAPI(), "booleanOperationPrefix", null);
        return prefix == null
                ? (String) joinPoint.proceed(new Object[]{name})
                : prefix + codegen.getterAndSetterCapitalize(name);
    }

    @Around("execution(Map<String, Object> postProcessOperations(Map<String, Object>)) && args(objs)")
    public Map<String, Object> postProcessOperations(ProceedingJoinPoint joinPoint, Map<String, Object> objs) throws Throwable {
        Map<String, Object> result = (Map<String, Object>) joinPoint.proceed(new Object[]{objs});
        Map<String, Object> operations = (Map<String, Object>) result.get("operations");
        operations.put("operation", ((List<CodegenOperation>) operations.get("operation"))
                .stream()
                .sorted(Comparator.comparing(CodegenOperation::getPath)
                        .thenComparing(CodegenOperation::getHttpMethod))
                .collect(Collectors.toList()));
        return result;
    }

    @Around("execution(void fixUpParentAndInterfaces(CodegenModel, Map<String, CodegenModel>)) && args(codegenModel, allModels)")
    public void fixUpParentAndInterfaces(ProceedingJoinPoint joinPoint, CodegenModel codegenModel, Map<String, CodegenModel> allModels) throws Throwable {
        DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();
        if (getSpecGlobalOption(codegen.getOpenAPI(), "resolvePropertyConflictsForJava", true)) {
            joinPoint.proceed(new Object[]{codegenModel, allModels});
        }
    }

    @Around("execution(String getTypeDeclaration(Schema)) && args(propertySchema)")
    public String getTypeDeclaration(ProceedingJoinPoint joinPoint, Schema propertySchema) throws Throwable {
        DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();

        List<Map<String, String>> overrides = getSpecExtension(codegen.getOpenAPI(), OVERRIDE_PROPERTY_TYPE, Collections.emptyList());
        for (Map<String, String> override : overrides) {
            if (override.containsKey("format")) {
                String format = override.get("format");
                if (format.equals(propertySchema.getFormat())) {
                    return override.get("type");
                }
            }
        }

        if (getSpecExtension(codegen.getOpenAPI(), GENERATE_LIST_MODELS, false) && isItemArray(propertySchema)) {
            Schema itemSchema = getArrayItemSchema(propertySchema);
            String itemType = (String) joinPoint.proceed(new Object[]{itemSchema});
            Set<String> topLevelResources = getSpecExtension(codegen.getOpenAPI(), TOP_LEVEL_RESOURCES, Collections.emptySet());
            if (!codegen.languageSpecificPrimitives().contains(itemType) && topLevelResources.contains(itemType)) {
                return itemType + "List";
            }
        }

        return (String) joinPoint.proceed(new Object[]{propertySchema});
    }

    @Around("execution(CodegenOperation fromOperation(String, String, Operation, Map<String, Schema>, OpenAPI)) && args(path, httpMethod, operation, definitions, openAPI)")
    public CodegenOperation fromOperation(ProceedingJoinPoint joinPoint, String path, String httpMethod, Operation operation, Map<String, Schema> definitions, OpenAPI openAPI) throws Throwable {
        CodegenOperation co = (CodegenOperation) joinPoint.proceed(new Object[]{path, httpMethod, operation, definitions, openAPI});

        if (co.getAllParams().parallelStream().anyMatch(param -> !param.required)) {
            co.vendorExtensions.put("hasOptional", true);

            List<CodegenParameter> nonOptionalParams = co.getAllParams().stream()
                    .filter(param -> param.required)
                    .map(CodegenParameter::copy)
                    .collect(Collectors.toList());
            if (!nonOptionalParams.isEmpty()) {
                co.vendorExtensions.put("nonOptionalParams", nonOptionalParams);
            }

            if (co.bodyParam != null && !co.bodyParam.required) {
                co.vendorExtensions.put("optionalBody", true);
            }
        }

        return co;
    }

    @Around("execution(CodegenParameter fromRequestBody(RequestBody, String, Schema, Map<String, Schema>, Set<String>)) && args(body, name, schema, schemas, imports)")
    public CodegenParameter fromRequestBody(ProceedingJoinPoint joinPoint, RequestBody body, String name, Schema schema, Map<String, Schema> schemas, Set<String> imports) throws Throwable {
        CodegenParameter codegenParameter = (CodegenParameter) joinPoint.proceed(new Object[]{body, name, schema, schemas, imports});

        DefaultCodegenConfig codegen = (DefaultCodegenConfig) joinPoint.getTarget();
        List<Map<String, String>> overrides = getSpecExtension(codegen.getOpenAPI(), OVERRIDE_PARAMETER_TYPE, Collections.emptyList());
        for (Map<String, String> override : overrides) {
            if (override.containsKey("format")) {
                String format = override.get("format");
                if (format.equals(schema.getFormat())) {
                    codegenParameter.baseType = override.get("type");
                    codegenParameter.dataType = codegenParameter.baseType;
                }
            }
        }

        return codegenParameter;
    }

    private <T> T getSpecExtension(OpenAPI spec, String name, T defaultValue) {
        if (spec != null && spec.getExtensions() != null && spec.getExtensions().containsKey(name)) {
            return (T) spec.getExtensions().get(name);
        }
        return defaultValue;
    }

    private <T> T getSpecGlobalOption(OpenAPI spec, String name, T defaultValue) {
        return (T) getSpecExtension(spec, SET_GLOBAL_OPTIONS, Collections.emptyMap()).getOrDefault(name, defaultValue);
    }
}
