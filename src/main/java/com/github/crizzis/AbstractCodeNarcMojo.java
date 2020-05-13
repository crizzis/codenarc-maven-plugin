package com.github.crizzis;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
public class AbstractCodeNarcMojo extends FailableMavenReport {

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
     * {@link AbstractCodeNarcMojo#sources}/{@link AbstractCodeNarcMojo#testSources} provided
     */
    @Parameter(property = "codenarc.includes", defaultValue = "**/*.groovy")
    private List<String> includes = List.of("**/*.groovy");

    /**
     * A collection of Ant-style file patterns specifying the files to exclude from analysis.
     * Takes precedence over {@link CodeNarcVerifyMojo#includes}.
     * The patterns defined here are appended to the exclusion rules of any custom
     * {@link AbstractCodeNarcMojo#sources}/{@link AbstractCodeNarcMojo#testSources} provided
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
     * @parameter property="codenarc.defaultRulesets"
     */
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

    protected File getXmlOutputFile() {
        return new File(getXmlOutputDirectory(), "codenarc.xml");
    }

    @Override
    protected void doExecuteReport(Locale locale) throws MavenReportException {
        throw new MavenReportException("Report generation is not supported");
    }

    @Override
    public boolean canGenerateReport() {
        return false;
    }
}
