package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.okta.sdk.OpenApiExtensions.HIDE_BASE_MEMBER;

public class HideBaseMemberProcessor implements Processor {
    private final Map<String, Set<String>> models = new HashMap<>();

    public HideBaseMemberProcessor(Object parameters) {
        for (Map<String, Object> parameter : (List<Map<String, Object>>) parameters) {
            @SuppressWarnings("unchecked")
            List<String> properties = (List<String>) parameter.get("properties");
            models.put((String) parameter.get("model"), new HashSet<>(properties));
        }
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        Map<String, Schema> schemas = spec.getComponents().getSchemas();
        models.forEach((modelName, properties) -> {
            Schema model = schemas.get(modelName);
            model.addExtension(HIDE_BASE_MEMBER, properties);
        });
    }
}
