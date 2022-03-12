package com.okta.sdk.generators;

import io.swagger.codegen.v3.CodegenType;
import io.swagger.codegen.v3.generators.java.AbstractJavaCodegen;

import java.io.File;

public class JavaCodeGen extends AbstractJavaCodegen {
    @Override
    public String getDefaultTemplateDir() {
        return "OktaJava";
    }

    @Override
    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    @Override
    public String getName() {
        return "JavaCodeGen";
    }

    @Override
    public String getHelp() {
        return "Java Code Generator";
    }

    @Override
    public String modelFileFolder() {
        return outputFolder + "/" + modelPackage().replace('.', File.separatorChar);
    }
}
