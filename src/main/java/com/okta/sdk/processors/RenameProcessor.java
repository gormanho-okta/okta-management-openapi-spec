package com.okta.sdk.processors;

import io.swagger.codegen.v3.CodegenConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.okta.sdk.OktaSdkExtensions.RENAME;

public class RenameProcessor implements Processor {
    private Map<String, String> renames = new HashMap<>();

    public RenameProcessor(List<Map<String, Object>> parameters) {
        for (Map<String, Object> parameter : parameters) {
            String from = (String) parameter.get("from");
            if (renames.containsKey(from)) {
                throw new RuntimeException("Trying to rename schema more than once: " + from);
            }
            renames.put(from, (String) parameter.get("to"));
        }
    }

    @Override
    public void process(OpenAPI spec, CodegenConfig config) {
        Map<String, Schema> schemas = spec.getComponents().getSchemas();
        renames.forEach((from, to) -> {
            for (String schemaName : schemas.keySet()) {
                String modelName = config.toModelName(schemaName);
                if (modelName.equals(from)) {
                    schemas.get(schemaName).addExtension(RENAME, to);
                }
            }
        });
    }
}
