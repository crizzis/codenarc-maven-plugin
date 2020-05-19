package com.github.crizzis.codenarc.parser;

import com.github.crizzis.codenarc.util.Phrasify;
import org.codenarc.results.Results;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.DefaultLocale;

import java.io.File;
import java.net.URL;
import java.util.stream.Stream;

import static com.github.crizzis.codenarc.ResultsSamples.*;
import static com.github.crizzis.codenarc.util.CodeNarcResultsMatcher.equalToResults;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DefaultLocale("en-US")
@DisplayNameGeneration(Phrasify.class)
class CodeNarcXmlParserTest {

    private CodeNarcXmlParser parser = new CodeNarcXmlParser();

    @ParameterizedTest(name = "should build correct results for {0}")
    @MethodSource
    void shouldBuildCorrectResults_whenValidXmlInput(String fileName, Results expected) throws Exception {
        //when
        Results results = parser.parse(new File(resource(fileName).toURI())).getResults();

        //then
        MatcherAssert.assertThat(results, equalToResults(expected));
    }

    static Stream<Arguments> shouldBuildCorrectResults_whenValidXmlInput() {
        return Stream.of(
                arguments("sample/codenarc-empty.xml", emptyResults()),
                arguments("sample/codenarc-default-package-single-file.xml", defaultPackageSingleFileResults()),
                arguments("sample/codenarc-default-package-multiple-files.xml", defaultPackageMultipleFilesResults()),
                arguments("sample/codenarc-regular-package-multiple-files.xml", regularPackageMultipleFilesResults()),
                arguments("sample/codenarc-multiple-packages.xml", multiplePackagesResults()),
                arguments("sample/codenarc-multiple-sources.xml", multipleSourcesResults())
        );
    }

    @Test
    void shouldReadCorrectTimestampSourcesProjectTitleAndCodeNarcVersion_whenValidXmlInput() throws Exception {
        //when
        CodeNarcAnalysis analysis = parser.parse(new File(resource("sample/codenarc-multiple-sources.xml").toURI()));

        //then
        assertThat(analysis.getReportTimestamp(), equalTo("Jan 03, 2020, 10:28:35 AM"));
        assertThat(analysis.getCodeNarcVersion(), equalTo("0.27.0"));
        assertThat(analysis.getProjectTitle(), equalTo("sample-mail-receiver"));
        assertThat(analysis.getSourceDirectories(), contains("src/main/groovy", "src/test/groovy"));
    }

    private static URL resource(String name) {
        return CodeNarcXmlParserTest.class.getClassLoader().getResource(name);
    }
}