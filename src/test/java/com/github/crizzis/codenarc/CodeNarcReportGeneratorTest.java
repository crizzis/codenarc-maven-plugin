package com.github.crizzis.codenarc;

import com.github.crizzis.codenarc.parser.CodeNarcAnalysis;
import com.github.crizzis.codenarc.report.CodeNarcReportGenerator;
import com.github.crizzis.codenarc.util.Phrasify;
import com.github.crizzis.codenarc.util.SinkMock;
import org.codenarc.results.Results;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static com.github.crizzis.codenarc.ResultsSamples.*;
import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayNameGeneration(Phrasify.class)
class CodeNarcReportGeneratorTest {

    private CodeNarcReportGenerator generator = new CodeNarcReportGenerator();
    private SinkMock sinkMock = new SinkMock();

    @ParameterizedTest(name = "should generate correct report for {2}")
    @MethodSource
    void shouldGenerateCorrectReport_whenValidAnalysis(CodeNarcAnalysis input, String expected, String description) {
        //when
        generator.generate(input, sinkMock.initialize(), ENGLISH);
        String actual = sinkMock.terminate();

        //then
        Diff reportDiff = DiffBuilder.compare(expected)
                .withTest(Input.fromString(actual))
                .ignoreWhitespace()
                .checkForSimilar()
                .build();
        assertThat(reportDiff.toString(), !reportDiff.hasDifferences());
    }

    static Stream<Arguments> shouldGenerateCorrectReport_whenValidAnalysis() throws IOException, URISyntaxException {
        return Stream.of(
                arguments(
                        defaultAnalysis(emptyResults()),
                        textFile("sample/report-empty.xml"),
                        "empty results"),
                arguments(
                        defaultAnalysis(multiplePackagesResults()),
                        textFile("sample/report-multiple-packages.xml"),
                        "results with multiple packages"),
                arguments(
                        defaultAnalysis(List.of("src/main/groovy", "src/test/groovy"), multipleSourcesResults()),
                        textFile("sample/report-multiple-sources.xml"),
                        "results with multiple source directories")
        );
    }

    @Test
    void shouldProduceCorrectLocalizedCaptions_whenDifferentLocalesSpecified() {

    }

    private static CodeNarcAnalysis defaultAnalysis(Results results) {
        return defaultAnalysis(emptyList(), results);
    }

    private static CodeNarcAnalysis defaultAnalysis(List<String> sourceDirectories,
                                             Results results) {
        return analysis("sample-mail-receiver", "0.27.0", "Jan 03, 2020, 10:28:35 AM", sourceDirectories, results);
    }

    private static CodeNarcAnalysis analysis(String title,
                                      String version,
                                      String timestamp,
                                      List<String> sourceDirectories,
                                      Results results) {
        CodeNarcAnalysis analysis = new CodeNarcAnalysis();
        analysis.setProjectTitle(title);
        analysis.setCodeNarcVersion(version);
        analysis.setReportTimestamp(timestamp);
        analysis.setSourceDirectories(sourceDirectories);
        analysis.setResults(results);
        return analysis;
    }

    private static String textFile(String resourceName) throws URISyntaxException, IOException {
        return Files.readString(Paths.get(resource(resourceName).toURI()));
    }

    private static URL resource(String name) {
        return CodeNarcVerifyMojo.class.getClassLoader().getResource(name);
    }
}