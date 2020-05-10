package com.github.crizzis;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codenarc.CodeNarcRunner;
import org.codenarc.results.Results;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

/**
 * Perform CodeNarc analysis, and optionally fail the build if specified quality criteria are not met
 */
@Getter
@Setter
@Mojo(name = "verify", defaultPhase = VERIFY, requiresDependencyResolution = TEST)
public class CodeNarcVerifyMojo extends AbstractCodeNarcMojo {

    private static final int PRIORITY_ONE = 1;
    private static final int PRIORITY_TWO = 2;
    private static final int PRIORITY_THREE = 3;

    private final CodeNarcRunnerFactory codeNarcRunnerFactory;
    private final CodeNarcXmlParser codeNarcXmlParser;
    private Collection<GroovyCompilerPluginIntegration> compilerIntegrations;

    @Inject
    public CodeNarcVerifyMojo(
            CodeNarcRunnerFactory codeNarcRunnerFactory,
            CodeNarcXmlParser codeNarcXmlParser,
            Collection<GroovyCompilerPluginIntegration> compilerIntegrations) {
        this.codeNarcRunnerFactory = codeNarcRunnerFactory;
        this.codeNarcXmlParser = codeNarcXmlParser;
        this.compilerIntegrations = compilerIntegrations;
    }

    /**
     * Whether to ignore any pre-existing CodeNarc XML report.
     * Setting this property to {@code true} is intended for a scenario in which generating the XML report
     * and failing the build (if violations have been found) needs to be split into separate executions of this goal
     */
    @Parameter(property = "codenarc.ignoreExistingReport", defaultValue = "true")
    private boolean ignoreExistingReport;

    /**
     * Turn XML output on and off. This will effectively disable generating the HTML report as well.
     * Use this if you want to fail the build without generating any output files
     */
    @Parameter(defaultValue = "true", property = "codenarc.xmlOutput", required = true)
    boolean xmlOutput;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession session;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!shouldRun()) {
            return;
        }
        Results results = obtainCodeNarcResults();
        verifyViolationsBelowThreshold(results);
    }

    private Results obtainCodeNarcResults() throws MojoExecutionException {
        if (!isIgnoreExistingReport() && getXmlOutputFile().exists()) {
            return reconstructFromExistingReport();
        } else {
            return tryExecuteCheck(obtainCodeNarcRunner());
        }
    }

    private Results reconstructFromExistingReport() throws MojoExecutionException {
        File outputFile = getXmlOutputFile();
        try {
            return codeNarcXmlParser.reconstruct(outputFile);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Could not read: %s", outputFile.getAbsolutePath()), e);
        }
    }

    private CodeNarcRunner obtainCodeNarcRunner() {
        CodeNarcConfig config = CodeNarcConfig.builder()
                .outputFile(getXmlOutputFile())
                .fileSets(resolveFileSets())
                .ruleSets(resolveRuleSets())
                .generateXmlReport(isXmlOutput())
                .build();
        return codeNarcRunnerFactory.newCodeNarcRunner(config);
    }

    private List<String> resolveRuleSets() {
        return Stream.concat(safeToStream(getDefaultRuleSets()), safeToStream(getAdditionalRuleSets())
                .map(File::toURI)
                .map(Object::toString)).collect(Collectors.toList());
    }

    private <T> Stream<T> safeToStream(Collection<T> collection) {
        return Optional.ofNullable(collection)
                .stream()
                .flatMap(Collection::stream);
    }

    private List<FileSet> resolveFileSets() {
        return SourceResolver.builder()
                .sources(getSources())
                .testSources(getTestSources())
                .pluginIntegrations(getCompilerIntegrations())
                .project(project)
                .session(session)
                .includeMain(isIncludeMain())
                .includeTests(isIncludeTests())
                .includes(getIncludes())
                .excludes(getExcludes())
                .resolveFileSets();
    }

    private boolean shouldRun() {
        if (isSkip()) {
            getLog().info("Plugin execution skipped");
            return false;
        }
        return true;
    }

    private void verifyViolationsBelowThreshold(Results results) throws MojoFailureException {
        verifyViolationsBelowThreshold(results, PRIORITY_ONE, getMaxPriority1Violations());
        verifyViolationsBelowThreshold(results, PRIORITY_TWO, getMaxPriority2Violations());
        verifyViolationsBelowThreshold(results, PRIORITY_THREE, getMaxPriority3Violations());
    }

    private void verifyViolationsBelowThreshold(Results results, int priority, int threshold) throws MojoFailureException {
        int actualViolations = results.getNumberOfViolationsWithPriority(priority, true);
        if (threshold >= 0 && actualViolations > threshold) {
            throw new MojoFailureException(String.format("totalPriority%dViolations exceeded threshold of %d errors with %d", priority, threshold, actualViolations));
        }
    }

    private Results tryExecuteCheck(CodeNarcRunner runner) throws MojoExecutionException {
        try {
            return runner.execute();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute CodeNarc analysis", e);
        }
    }
}
