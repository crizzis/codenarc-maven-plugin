package com.github.crizzis;

import com.github.crizzis.util.Phrasify;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codenarc.CodeNarcRunner;
import org.codenarc.results.Results;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(Phrasify.class)
class CodeNarcVerifyMojoTest {

    public static final String PROJECT_NAME = "example_project";
    private CodeNarcRunnerFactory codeNarcRunnerFactory = mock(CodeNarcRunnerFactory.class);
    private CodeNarcRunner codeNarcRunner = mock(CodeNarcRunner.class);
    private CodeNarcXmlParser codeNarcXmlParser = mock(CodeNarcXmlParser.class);
    private GroovyCompilerPluginIntegration compilerPluginIntegration = mock(GroovyCompilerPluginIntegration.class);
    private CodeNarcVerifyMojo mojo = initializeMojo();

    private ArgumentCaptor<CodeNarcConfig> config = ArgumentCaptor.forClass(CodeNarcConfig.class);

    @BeforeEach
    void setup() {
        when(codeNarcRunnerFactory.newCodeNarcRunner(config.capture())).thenReturn(codeNarcRunner);
        when(compilerPluginIntegration.getSources(any(), any())).thenReturn(Optional.empty());
        when(compilerPluginIntegration.getTestSources(any(), any())).thenReturn(Optional.empty());
        mockViolations(0, 0, 0);
    }

    @Test
    void execute_shouldNotRun_whenSkipIsTrue() throws MojoFailureException, MojoExecutionException {
        //given
        mojo.setSkip(true);

        //when
        mojo.execute();

        //then
        verify(codeNarcRunner, never()).execute();
    }

    @Test
    void execute_shouldRelyOnPreExistingAnalysis_whenIgnoreExistingReportIsFalse() throws Exception {
        //given
        File xmlOutput = new File(resource("sample/CodeNarc.xml").toURI());
        Results results = resultsWithViolationCounts(3, 0, 0);
        doReturn(results).when(codeNarcXmlParser).parse(xmlOutput);
        mojo.setIgnoreExistingReport(false);
        mojo.setXmlOutputDirectory(xmlOutput.getParentFile());
        mojo.setMaxPriority1Violations(2);

        //when, then
        assertThrows(MojoFailureException.class, () -> mojo.execute());
    }

    @Test
    void execute_shouldPerformAnalysis_whenIgnoreExistingReportIsTrue() throws Exception {
        //given
        mockViolations(3, 0, 0);
        File xmlOutput = new File(resource("sample/CodeNarc.xml").toURI());
        mojo.setXmlOutputDirectory(xmlOutput.getParentFile());
        mojo.setIgnoreExistingReport(true);

        //when, then
        assertThrows(MojoFailureException.class, () -> mojo.execute());
    }

    @Test
    void execute_shouldPerformAnalysis_whenIgnoreExistingReportIsFalseButFileNotExists() {
        //given
        mockViolations(3, 0, 0);
        mojo.setXmlOutputDirectory(new File("nonexistent"));
        mojo.setIgnoreExistingReport(false);

        //when, then
        assertThrows(MojoFailureException.class, () -> mojo.execute());
    }

    @Test
    void execute_shouldNotFailExecution_whenPriorityViolationsIgnored() {
        //given
        mockViolations(1, 1, 1);
        mojo.setMaxPriority1Violations(-1);
        mojo.setMaxPriority2Violations(-1);
        mojo.setMaxPriority3Violations(-1);

        //when, then
        assertDoesNotThrow(() -> mojo.execute());
    }

    @ParameterizedTest
    @MethodSource
    void execute_shouldNotFailExecution_whenNumberOfViolationsBelowThreshold(
            int maxPriority1Violations, int maxPriority2Violations, int maxPriority3Violations,
            int actualPriority1Violations, int actualPriority2Violations, int actualPriority3Violations) {
        //given
        mockViolations(actualPriority1Violations, actualPriority2Violations, actualPriority3Violations);
        mojo.setMaxPriority1Violations(maxPriority1Violations);
        mojo.setMaxPriority2Violations(maxPriority2Violations);
        mojo.setMaxPriority3Violations(maxPriority3Violations);

        //when, then
        assertDoesNotThrow(() -> mojo.execute());
    }

    static Stream<Arguments> execute_shouldNotFailExecution_whenNumberOfViolationsBelowThreshold() {
        return Stream.of(
                arguments(0, 0, 0, 0, 0, 0),
                arguments(0, -1, -1, 0, 0, 0),
                arguments(3, -1, -1, 2, 4, 4),
                arguments(-1, 3, -1, 4, 2, 4),
                arguments(-1, -1, 3, 4, 4, 2),
                arguments(2, 4, 3, 0, 0, 0),
                arguments(2, 4, 3, 1, 3, 2),
                arguments(2, 4, 3, 2, 4, 3)
        );
    }

    @ParameterizedTest
    @MethodSource
    void execute_shouldFailExecution_whenNumberOfViolationsAboveThreshold(
            int maxPriority1Violations, int maxPriority2Violations, int maxPriority3Violations,
            int actualPriority1Violations, int actualPriority2Violations, int actualPriority3Violations) {
        //given
        mockViolations(actualPriority1Violations, actualPriority2Violations, actualPriority3Violations);
        mojo.setMaxPriority1Violations(maxPriority1Violations);
        mojo.setMaxPriority2Violations(maxPriority2Violations);
        mojo.setMaxPriority3Violations(maxPriority3Violations);

        //when, then
        assertThrows(MojoFailureException.class, () -> mojo.execute());
    }

    static Stream<Arguments> execute_shouldFailExecution_whenNumberOfViolationsAboveThreshold() {
        return Stream.of(
                arguments(0, -1, -1, 1, 0, 0),
                arguments(3, -1, -1, 5, 4, 4),
                arguments(-1, 3, -1, 4, 5, 4),
                arguments(-1, -1, 3, 4, 4, 4),
                arguments(2, 4, 3, 3, 5, 4)
        );
    }

    @Test
    void execute_shouldForwardAllErrorsFromCodeNarcRunner() {
        //given
        RuntimeException codeNarcException = new RuntimeException();
        when(codeNarcRunner.execute()).thenThrow(codeNarcException);

        //when, then
        MojoExecutionException executionException = assertThrows(MojoExecutionException.class, () -> mojo.execute());
        assertEquals(executionException.getCause(), codeNarcException);
    }

    @Test
    void execute_shouldConfigureProjectTitleCorrectly() throws MojoFailureException, MojoExecutionException {
        //when
        mojo.execute();

        //then
        assertEquals(config.getValue().getProjectName(), PROJECT_NAME);
    }

    @Test
    void execute_shouldConfigureOutputDirectoryCorrectly() throws MojoFailureException, MojoExecutionException {
        //given
        mojo.setXmlOutputDirectory(new File("output"));

        //when
        mojo.execute();

        //then
        assertEquals(config.getValue().getOutputFile(), new File("output/CodeNarc.xml"));
    }

    @ParameterizedTest
    @MethodSource("sourceConfigurationTestCases")
    void execute_shouldConfigureSourcesCorrectly_whenSourcesProvided(
            List<FileSet> sources, List<String> excludes, List<FileSet> expected) throws MojoFailureException, MojoExecutionException {
        //given
        mojo.setIncludeMain(true);
        mojo.setSources(sources.toArray(new FileSet[0]));
        mojo.setExcludes(excludes);

        //when
        mojo.execute();
        List<FileSet> actual = config.getValue().getFileSets();

        //then
        assertThat(actual, equalToFileSets(expected));
    }

    @ParameterizedTest
    @MethodSource("sourceConfigurationTestCases")
    void execute_shouldConfigureTestSourcesCorrectly_whenTestSourcesProvided(
            List<FileSet> sources, List<String> excludes, List<FileSet> expected) throws MojoFailureException, MojoExecutionException {
        //given
        mojo.setIncludeTests(true);
        mojo.setTestSources(sources.toArray(new FileSet[0]));
        mojo.setExcludes(excludes);

        //when
        mojo.execute();
        List<FileSet> actual = config.getValue().getFileSets();

        //then
        assertThat(actual, equalToFileSets(expected));
    }

    static Stream<Arguments> sourceConfigurationTestCases() {
        return Stream.of(
                arguments(
                        List.of(fileSet("src/main/groovy-sources", emptyList(), emptyList())),
                        emptyList(),
                        List.of(fileSet("src/main/groovy-sources", List.of("**/*.groovy"), emptyList()))
                ),
                arguments(
                        List.of(fileSet("src/main/groovy-sources", emptyList(), emptyList())),
                        List.of("**/*IT.groovy"),
                        List.of(fileSet("src/main/groovy-sources", List.of("**/*.groovy"), List.of("**/*IT.groovy")))
                ),
                arguments(
                        List.of(
                                fileSet("src/main/groovy-sources", emptyList(), emptyList()),
                                fileSet("src/main/groovy-sources-2", List.of("**/*.groovy-source"), List.of("**/*IT.groovy"))
                        ),
                        List.of("**/*IT.groovy"),
                        List.of(
                                fileSet("src/main/groovy-sources", List.of("**/*.groovy"), List.of("**/*IT.groovy")),
                                fileSet("src/main/groovy-sources-2", List.of("**/*.groovy-source", "**/*.groovy"), List.of("**/*IT.groovy"))
                        )
                ),
                arguments(
                        List.of(fileSet("src/main/groovy-sources", List.of("**/*.groovy-source"), List.of("**/*IT.groovy"))),
                        List.of("**/*IT2.groovy"),
                        List.of(fileSet("src/main/groovy-sources", List.of("**/*.groovy-source", "**/*.groovy"), List.of("**/*IT.groovy", "**/*IT2.groovy")))
                )
        );
    }

    @Test
    void execute_shouldConfigureSourcesCorrectly_whenSourcesResolvedFromGroovyCompilerPlugin() throws IOException, MojoFailureException, MojoExecutionException {
        //given
        reset(compilerPluginIntegration);
        when(compilerPluginIntegration.getSources(any(), any())).thenReturn(Optional.of(new FileSet[] {
                fileSet("src/groovy", emptyList(), List.of("**/*IT.groovy"))
        }));
        when(compilerPluginIntegration.getTestSources(any(), any())).thenReturn(Optional.of(new FileSet[] {
                fileSet("src/groovy-test", emptyList(), List.of("**/*IT.groovy"))
        }));
        mojo.setIncludeTests(true);
        mojo.setIncludeMain(true);

        //when
        mojo.execute();

        //then
        assertThat(config.getValue().getFileSets(), equalToFileSets(List.of(
                fileSet("src/groovy", List.of("**/*.groovy"), List.of("**/*IT.groovy")),
                fileSet("src/groovy-test", List.of("**/*.groovy"), List.of("**/*IT.groovy"))
        )));
    }

    @ParameterizedTest
    @MethodSource
    void execute_shouldConfigureSourcesCorrectly_whenSourceDefaultsAndIncludeFlagConfigs(
            boolean includeMain, boolean includeTests, List<FileSet> expected) throws MojoFailureException, MojoExecutionException {
        //given
        mojo.setIncludeMain(includeMain);
        mojo.setIncludeTests(includeTests);

        //when
        mojo.execute();
        List<FileSet> actual = config.getValue().getFileSets();

        //then
        assertThat(actual, equalToFileSets(expected));
    }

    static Stream<Arguments> execute_shouldConfigureSourcesCorrectly_whenSourceDefaultsAndIncludeFlagConfigs() {
        return Stream.of(
                arguments(false, false, emptyList()),
                arguments(true, false, List.of(fileSet("src/main/groovy", List.of("**/*.groovy"), emptyList()))),
                arguments(false, true, List.of(fileSet("src/test/groovy", List.of("**/*.groovy"), emptyList()))),
                arguments(true, true, List.of(
                        fileSet("src/main/groovy", List.of("**/*.groovy"), emptyList()),
                        fileSet("src/test/groovy", List.of("**/*.groovy"), emptyList())))
        );
    }

    @Test
    void execute_shouldUseDefultRuleSets_whenNoCustomization() throws MojoFailureException, MojoExecutionException {
        //when
        mojo.execute();
        List<String> ruleSets = config.getValue().getRuleSets();

        //then
        assertThat(ruleSets, containsInAnyOrder("rulesets/basic.xml", "rulesets/exceptions.xml", "rulesets/imports.xml"));
    }

    @Test
    void execute_shouldConfigureRuleSetsCorrectly_whenRuleSetSettingsCustomized() throws MojoFailureException, MojoExecutionException {
        //given
        File firstCustomFile = new File("custom/ruleset1.xml");
        File secondCustomFile = new File("custom/ruleset2.xml");
        mojo.setDefaultRuleSets(List.of("rulesets/exceptions.xml"));
        mojo.setAdditionalRuleSets(new File[] {
                firstCustomFile,
                secondCustomFile
        });

        //when
        mojo.execute();
        List<String> ruleSets = config.getValue().getRuleSets();

        //then
        assertThat(ruleSets, containsInAnyOrder(
                "rulesets/exceptions.xml",
                new File("").toURI() + "custom/ruleset1.xml",
                new File("").toURI() + "custom/ruleset2.xml"));

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void execute_shouldOnlyGenerateXmlReportWhenRequested(boolean shouldGenerateXmlReport) throws MojoFailureException, MojoExecutionException {
        //given
        mojo.setXmlOutput(shouldGenerateXmlReport);

        //when
        mojo.execute();

        //then
        assertEquals(config.getValue().isGenerateXmlReport(), shouldGenerateXmlReport);
    }

    private CodeNarcVerifyMojo initializeMojo() {
        CodeNarcVerifyMojo mojo = new CodeNarcVerifyMojo(codeNarcRunnerFactory, codeNarcXmlParser, List.of(compilerPluginIntegration));
        mojo.setIgnoreExistingReport(true);
        mojo.setXmlOutput(true);
        MavenProject project = new MavenProject();
        project.setName(PROJECT_NAME);
        mojo.setProject(project);
        return mojo;
    }

    private static URL resource(String name) {
        return CodeNarcVerifyMojo.class.getClassLoader().getResource(name);
    }

    private static FileSet fileSet(String directory, List<String> includes, List<String> excludes) {
        FileSet fileSet = new FileSet();
        fileSet.setDirectory(directory);
        fileSet.setIncludes(includes);
        fileSet.setExcludes(excludes);
        return fileSet;
    }

    private void mockViolations(int priorityOne, int priorityTwo, int priorityThree) {
        reset(codeNarcRunner);
        Results results = resultsWithViolationCounts(priorityOne, priorityTwo, priorityThree);
        when(codeNarcRunner.execute()).thenReturn(results);
    }

    private Results resultsWithViolationCounts(int priorityOne, int priorityTwo, int priorityThree) {
        Results results = mock(Results.class);
        when(results.getNumberOfViolationsWithPriority(1, true)).thenReturn(priorityOne);
        when(results.getNumberOfViolationsWithPriority(2, true)).thenReturn(priorityTwo);
        when(results.getNumberOfViolationsWithPriority(3, true)).thenReturn(priorityThree);
        return results;
    }

    private Matcher<Iterable<? extends FileSet>> equalToFileSets(List<FileSet> expected) {
        return expected.isEmpty() ? emptyIterable() : contains(expected.stream().map(this::equalToFileSet).collect(Collectors.toList()));
    }

    private Matcher<FileSet> equalToFileSet(FileSet expected) {
        return Matchers.allOf(
                hasProperty("directory", equalTo(expected.getDirectory())),
                hasProperty("includes", equalTo(expected.getIncludes())),
                hasProperty("excludes", equalTo(expected.getExcludes()))
        );
    }
}