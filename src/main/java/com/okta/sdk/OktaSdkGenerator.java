package com.okta.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.sdk.processors.Processor;
import com.okta.sdk.processors.ProcessorSpec;
import com.okta.sdk.processors.TopLevelResourcesProcessor;
import io.swagger.codegen.v3.DefaultGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OktaSdkGenerator extends DefaultGenerator {
    private List<Processor> processors = new ArrayList<>();

    OktaSdkGenerator(File configFile) throws IOException {
        processors.add(new TopLevelResourcesProcessor());
        if (configFile != null) {
            InputStream stream = new FileInputStream(configFile);
            new ObjectMapper()
                    .readValue(stream, new TypeReference<List<ProcessorSpec>>() {})
                    .stream()
                    .map(spec -> Processor.create(spec.type, spec.parameters))
                    .forEach(processors::add);
        }
    }

    @Override
    public List<File> generate() {
        preprocessSpec();
        return super.generate();
    }

    private void preprocessSpec() {
        for (Processor processor : processors) {
            processor.process(openAPI, config);
        }
    }
}
