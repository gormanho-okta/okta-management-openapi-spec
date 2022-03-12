package com.okta.sdk;

import io.swagger.codegen.v3.ClientOptInput;
import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class OktaSdkCodeGenMojo extends AbstractMojo {
    @Parameter(name = "configFile")
    private File configFile;

    @Parameter(name = "language", required = true)
    private String language;

    @Parameter(name = "modelPackage")
    private String modelPackage;

    @Parameter(name = "templateDirectory", required = true)
    private File templateDirectory;

    @Parameter(name = "output", required = true)
    private File output;

    @Parameter(name = "errataFile")
    private File errataFile;

    public void execute() throws MojoExecutionException {
        try {
            CodegenConfigurator configurator = CodegenConfigurator.fromFile(configFile.getAbsolutePath());
            configurator.setInputSpecURL("management.yaml");
            configurator.setLang(language);
            configurator.setModelPackage(modelPackage);
            configurator.setTemplateDir(templateDirectory.getAbsolutePath());
            configurator.setOutputDir(output.getAbsolutePath());
            System.setProperty(CodegenConstants.MODEL_TESTS, "false");
            System.setProperty(CodegenConstants.MODEL_DOCS, "false");
            System.setProperty(CodegenConstants.API_TESTS, "false");
            System.setProperty(CodegenConstants.API_DOCS, "false");
            ClientOptInput input = configurator.toClientOptInput();
            new OktaSdkGenerator(errataFile).opts(input).generate();
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Code generation failed.");
        }
    }
}
