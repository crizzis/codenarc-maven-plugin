/*
 * Copyright 2020 Krzysztof Sierszeń
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
package com.github.crizzis;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codenarc.CodeNarcRunner;
import org.codenarc.results.Results;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
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
public class CodeNarcVerifyMojo extends AbstractMojo implements AnalysisScopeConfig {

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
     * Location where the generated XML report will be created
     */
    @Parameter(property="codenarc.xmlOutputDirectory", defaultValue = "${project.build.directory}")
    private File xmlOutputDirectory;

    /**
     * Set this to "true" to bypass CodeNarc verification entirely
     */
    @Parameter(property="codenarc.skip", defaultValue = "false")
    private boolean skip;

    /**
     * A collection of Ant-style file patterns specifying the files to analyze.
     * The patterns defined here are appended to the inclusion rules of any custom
     * {@link CodeNarcVerifyMojo#sources}/{@link CodeNarcVerifyMojo#testSources} provided
     */
    @Parameter(property = "codenarc.includes", defaultValue = "**/*.groovy")
    private List<String> includes = List.of("**/*.groovy");

    /**
     * A collection of Ant-style file patterns specifying the files to exclude from analysis.
     * Takes precedence over {@link CodeNarcVerifyMojo#includes}.
     * The patterns defined here are appended to the exclusion rules of any custom
     * {@link CodeNarcVerifyMojo#sources}/{@link CodeNarcVerifyMojo#testSources} provided
     */
    @Parameter(property = "codenarc.excludes")
    private List<String> excludes;

    /**
     * The CodeNarc default rulesets to use.
     *
     * Defaults to <a href="https://codenarc.github.io/CodeNarc/codenarc-rules-basic.html">rulesets/basic.xml</a>,
     * <a href="https://codenarc.github.io/CodeNarc/codenarc-rules-exceptions.html">rulesets/exceptions.xml</a>,
     * <a href="https://codenarc.github.io/CodeNarc/codenarc-rules-imports.html">rulesets/imports.xml</a>
     *
     */
    @Parameter(property = "codenarc.defaultRuleSets",
            defaultValue = "rulesets/basic.xml,rulesets/exceptions.xml,rulesets/imports.xml")
    private List<String> defaultRuleSets = List.of("rulesets/basic.xml", "rulesets/exceptions.xml", "rulesets/imports.xml");

    /**
     * A list of custom CodeNarc ruleset files to use
     */
    @Parameter(property = "codenarc.additionalRulesets")
    private File[] additionalRuleSets = new File[0];

    /**
     * The filesets containing source files to be analyzed.
     *
     * Defaults to the source configuration of <a href="https://groovy.github.io/GMavenPlus/">gmavenplus-plugin</a> if configured as part of the build;
     * otherwise, uses {@code ${project.basedir}/src/main/groovy/**&#47;*.groovy} as the default.
     */
    @Parameter(property = "codenarc.sources")
    private FileSet[] sources;

    /**
     * The filesets containing source files to be analyzed.
     *
     * Defaults to the test source configuration of <a href="https://groovy.github.io/GMavenPlus/">gmavenplus-plugin</a> if configured as part of the build;
     * otherwise, uses {@code ${project.basedir}/src/test/groovy/**&#47;*.groovy} as the default
     */
    @Parameter(property = "codenarc.testSources")
    private FileSet[] testSources;

    /**
     * Whether to include test classes in the analysis
     */
    @Parameter(property = "codenarc.includeTests", defaultValue = "false")
    private boolean includeTests;

    /**
     * Whether to include main classes in the analysis
     */
    @Parameter(property = "codenarc.includeMain", defaultValue = "true")
    private boolean includeMain;

    /**
     * Maximum number of priority 1 violations allowed before failing the build
     * (the default value of -1 means the build will not fail even if violations occur)
     */
    @Parameter(property = "codenarc.maxPriority1Violations", defaultValue = "-1")
    private int maxPriority1Violations;

    /**
     * Maximum number of priority 2 violations allowed before failing the build
     * (the default value of -1 means the build will not fail even if violations occur)
     */
    @Parameter(property = "codenarc.maxPriority2Violations", defaultValue = "-1")
    private int maxPriority2Violations;

    /**
     * Maximum number of priority 3 violations allowed before failing the build
     * (the default value of -1 means the build will not fail even if violations occur)
     */
    @Parameter(property = "codenarc.maxPriority3Violations", defaultValue = "-1")
    private int maxPriority3Violations;

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
            getLog().info("Existing CodeNarc report XML found, parsing");
            Results results = codeNarcXmlParser.parse(outputFile);
            getLog().info(String.format("Parsing completed: (p1=%d; p2=%d; p3=%d)",
                    results.getNumberOfViolationsWithPriority(PRIORITY_ONE, true),
                    results.getNumberOfViolationsWithPriority(PRIORITY_TWO, true),
                    results.getNumberOfViolationsWithPriority(PRIORITY_THREE, true)));
            return results;
        } catch (CodeNarcXmlParser.XmlParserException e) {
            throw new MojoExecutionException(String.format("Could not parse: %s", outputFile.getAbsolutePath()), e);
        }
    }

    private CodeNarcRunner obtainCodeNarcRunner() throws MojoExecutionException {
        CodeNarcConfig config = CodeNarcConfig.builder()
                .projectName(project.getName())
                .outputFile(getXmlOutputFile())
                .fileSets(resolveFileSets())
                .ruleSets(resolveRuleSets())
                .generateXmlReport(isXmlOutput())
                .build();
        return codeNarcRunnerFactory.newCodeNarcRunner(config);
    }

    private File getXmlOutputFile() {
        return new File(getXmlOutputDirectory(), "CodeNarc.xml");
    }

    private List<String> resolveRuleSets() throws MojoExecutionException {
        List<String> rulesets = Stream.concat(
                safeToStream(getDefaultRuleSets()),
                Arrays.stream(getAdditionalRuleSetUrls()).map(Object::toString))
                .collect(Collectors.toList());
        getLog().info("Using rule sets: " + String.join(", ", rulesets));
        return rulesets;
    }

    private URL[] getAdditionalRuleSetUrls() throws MojoExecutionException {
        try {
            return FileUtils.toURLs(getAdditionalRuleSets());
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Cannot convert files: %s to URLs", getAdditionalRuleSets()));
        }
    }

    private <T> Stream<T> safeToStream(Collection<T> collection) {
        return Optional.ofNullable(collection)
                .stream()
                .flatMap(Collection::stream);
    }

    private List<FileSet> resolveFileSets() {
        List<FileSet> fileSets = FileSetResolver.builder()
                .project(project)
                .session(session)
                .pluginIntegrations(getCompilerIntegrations())
                .scopeConfig(this)
                .resolveFileSets();
        getLog().info("Resolved filesets: ");
        fileSets.forEach(fileSet -> getLog().info(fileSet.toString()));
        return fileSets;
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
            getLog().info("Executing CodeNarc analysis");
            return runner.execute();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to execute CodeNarc analysis", e);
        }
    }
}
