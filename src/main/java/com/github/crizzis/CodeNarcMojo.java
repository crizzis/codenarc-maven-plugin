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
import org.apache.maven.model.FileSet;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.apache.maven.plugins.annotations.LifecyclePhase.SITE;
import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

/**
 * Create a CodeNarc report, and optionally fail the build if specified quality criteria are not met
 */
@Getter
@Mojo(name = "codenarc", defaultPhase = SITE, requiresDependencyResolution = COMPILE)
@Execute(phase = SITE, goal = "codenarc")
public class CodeNarcMojo extends AbstractMavenReport {

    /**
     * Location where the generated HTML report will be created
     */
    @Parameter(property = "codenarc.outputDirectory", defaultValue = "${project.reporting.outputDirectory}")
    private File outputDirectory;

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
     * A collection of Ant-style file patterns specifying the files to analyze
     */
    @Parameter(property = "codenrc.includes")
    private List<String> includes = List.of("**/*.groovy");

    /**
     * A collection of Ant-style file patterns specifying the files to exclude from analysis. Takes precedence over {@link CodeNarcMojo#includes}
     */
    @Parameter(property = "codenarc.exclude")
    private List<String> excludes;

    /**
     * The CodeNarc default rulesets to use.
     *
     * Defaults to <a href="https://codenarc.github.io/CodeNarc/codenarc-rules-basic.html">rulesets/basic.xml</a>,
     * <a href="https://codenarc.github.io/CodeNarc/codenarc-rules-exceptions.html">rulesets/exceptions.xml</a>,
     * <a href="https://codenarc.github.io/CodeNarc/codenarc-rules-imports.html">rulesets/imports.xml</a>
     */
    @Parameter(property = "codenarc.defaultRulesetFiles")
    private List<String> defaultRuleSets = List.of("rulesets/basic.xml", "rulesets/exceptions.xml", "rulesets/imports.xml");

    /**
     * A list of custom CodeNarc ruleset files to use
     */
    @Parameter(property = "codenarc.additionalRulesets")
    private List<File> additionalRuleSets;

    /**
     * The filesets containing source files to be analyzed.
     *
     * Defaults to the source configuration of <a href="https://groovy.github.io/GMavenPlus/">gmavenplus-plugin</a> if configured as part of the build;
     * otherwise, uses {@code ${project.basedir}/src/main/groovy/**&#47;*.groovy} as the default
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

    protected void executeReport(Locale locale) throws MavenReportException {

    }

    public String getOutputName() {
        return "codenarc";
    }

    public String getName(Locale locale) {
        return ResourceBundle.getBundle("codenarc", locale).getString("report.codenarc.name");
    }

    public String getDescription(Locale locale) {
        return ResourceBundle.getBundle("codenarc", locale).getString("report.codenarc.description");
    }
}
