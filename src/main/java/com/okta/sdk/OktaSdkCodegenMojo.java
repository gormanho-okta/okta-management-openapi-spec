package com.okta.sdk;

import io.swagger.codegen.v3.CodegenConstants;
import io.swagger.codegen.v3.config.CodegenConfigurator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class OktaSdkCodegenMojo extends AbstractMojo {
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
            new OktaSdkGenerator(errataFile)
                    .opts(CodegenConfigurator
                            .fromFile(configFile.getAbsolutePath())
                            .setInputSpecURL("management.yaml")
                            .setLang(language)
                            .setModelPackage(modelPackage)
                            .setTemplateDir(templateDirectory.getAbsolutePath())
                            .setOutputDir(output.getAbsolutePath())
                            .addSystemProperty(CodegenConstants.MODEL_TESTS, "false")
                            .addSystemProperty(CodegenConstants.MODEL_DOCS, "false")
                            .addSystemProperty(CodegenConstants.API_TESTS, "false")
                            .addSystemProperty(CodegenConstants.API_DOCS, "false")
                            .toClientOptInput())
                    .generate();
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Code generation failed.");
        }
    }
}
