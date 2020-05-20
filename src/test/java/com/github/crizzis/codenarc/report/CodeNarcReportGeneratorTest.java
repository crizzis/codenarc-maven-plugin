package com.github.crizzis.codenarc.report;

import com.github.crizzis.codenarc.CodeNarcVerifyMojo;
import com.github.crizzis.codenarc.parser.CodeNarcAnalysis;
import com.github.crizzis.codenarc.util.Phrasify;
import com.github.crizzis.codenarc.util.SinkMock;
import org.apache.commons.io.IOUtils;
import org.codenarc.results.Results;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static com.github.crizzis.codenarc.ResultsSamples.*;
import static com.github.crizzis.codenarc.report.ReportElements.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayNameGeneration(Phrasify.class)
class CodeNarcReportGeneratorTest {



    private static final DocumentBuilderFactory XML_FACTORY = DocumentBuilderFactory.newDefaultInstance();

    private DocumentBuilder xmlBuilder = XML_FACTORY.newDocumentBuilder();
    private CodeNarcReportGenerator generator = new CodeNarcReportGenerator();
    private SinkMock sinkMock = new SinkMock();

    CodeNarcReportGeneratorTest() throws ParserConfigurationException {
    }

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
    void shouldProduceCorrectLocalizedCaptions_whenNonDefaultLocaleSpecified() throws Exception {
        //when
        generator.generate(
                defaultAnalysis(List.of("src/main/groovy", "src/test/groovy"), multipleSourcesResults()),
                sinkMock.initialize(),
                GERMAN);
        Document actual = xmlBuilder.parse(IOUtils.toInputStream(sinkMock.terminate(), UTF_8));

        //then
        assertThat(actual, hasTitleCorrectlyLocalized());
        assertThat(actual, hasGeneralInfoSectionCorrectlyLocalized());
        assertThat(actual, hasSummarySectionCorrectlyLocalized());
        assertThat(actual, hasPackageSummarySectionCorrectlyLocalized());
        assertThat(actual, hasFilesSectionCorrectlyLocalized());
    }

    private Matcher<Document> hasFilesSectionCorrectlyLocalized() {
        return describedAs("Report with correctly localized Files section", allOf(
                hasPath(FILES_TITLE, containsString("DE Files")),
                hasPath(FILES_SOURCE_DIRECTORY_CAPTION, containsString("DE Source Directory")),
                hasPath(FILES_RULE_NAME_HEADER, equalToCompressingWhiteSpace("DE Rule Name")),
                hasPath(FILES_PRIORITY_HEADER, equalToCompressingWhiteSpace("DE Priority")),
                hasPath(FILES_LINE_HEADER, equalToCompressingWhiteSpace("DE Line")),
                hasPath(FILES_SOURCE_LINE_MESSAGE_HEADER, equalToCompressingWhiteSpace("DE Source Line / Message"))
        ));
    }

    private Matcher<Document> hasPackageSummarySectionCorrectlyLocalized() {
        return describedAs("Report with correctly localized Package Summary section",allOf(
                hasPath(PACKAGE_SUMMARY_TITLE, equalToCompressingWhiteSpace("DE Package Summary")),
                hasPath(PACKAGE_SUMMARY_SOURCE_DIRECTORY_CAPTION, containsString("DE Source Directory")),
                hasPath(PACKAGE_SUMMARY_PACKAGE_HEADER, equalToCompressingWhiteSpace("DE Package")),
                hasPath(PACKAGE_SUMMARY_FILES_WITH_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Files with Violations")),
                hasPath(PACKAGE_SUMMARY_TOTAL_VIOLATION_HEADER, equalToCompressingWhiteSpace("DE Total Violations")),
                hasPath(PACKAGE_SUMMARY_PRIORITY_ONE_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Priority 1 Violations")),
                hasPath(PACKAGE_SUMMARY_PRIORITY_TWO_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Priority 2 Violations")),
                hasPath(PACKAGE_SUMMARY_PRIORITY_THREE_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Priority 3 Violations"))
        ));
    }

    private Matcher<Document> hasSummarySectionCorrectlyLocalized() {
        return describedAs("Report with correctly localized Summary section",allOf(
                hasPath(SUMMARY_SECTION_TITLE, equalToCompressingWhiteSpace("DE Summary")),
                hasPath(SUMMARY_TOTAL_FILES_HEADER, equalToCompressingWhiteSpace("DE Total Files")),
                hasPath(SUMMARY_FILES_WITH_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Files with Violations")),
                hasPath(SUMMARY_TOTAL_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Total Violations")),
                hasPath(SUMMARY_PRIORITY_ONE_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Priority 1 Violations")),
                hasPath(SUMMARY_PRIORITY_TWO_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Priority 2 Violations")),
                hasPath(SUMMARY_PRIORITY_THREE_VIOLATIONS_HEADER, equalToCompressingWhiteSpace("DE Priority 3 Violations"))
        ));
    }

    private Matcher<Document> hasGeneralInfoSectionCorrectlyLocalized() {
        return describedAs("Report with correctly localized General Info section",allOf(
                hasPath(GENERAL_INFO_SECTION_TITLE, equalToCompressingWhiteSpace("DE CodeNarc Report")),
                hasPath(DESCRIPTION, equalToCompressingWhiteSpace("DE The following document contains the results of CodeNarc analysis")),
                hasPath(VERSION_CAPTION, containsString("DE CodeNarc Version")),
                hasPath(VERSION_CAPTION, containsString("DE CodeNarc Version")),
                hasPath(GENERATION_TIME, containsString("DE Report Time"))
        ));
    }

    private Matcher<Node> hasTitleCorrectlyLocalized() {
        return describedAs("Report with correctly localized title",
                hasPath(TITLE, equalToCompressingWhiteSpace("DE CodeNarc Report")));
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

    private Matcher<Node> hasPath(ReportElements reportElement, Matcher<String> elementMatcher) {
        return hasXPath(reportElement.path(), elementMatcher);
    }
}