package com.github.crizzis;

import com.github.crizzis.util.Phrasify;
import org.codenarc.results.DirectoryResults;
import org.codenarc.results.FileResults;
import org.codenarc.results.Results;
import org.codenarc.rule.Rule;
import org.codenarc.rule.StubRule;
import org.codenarc.rule.Violation;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import static com.github.crizzis.util.CodeNarcResultsMatcher.equalTo;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayNameGeneration(Phrasify.class)
class CodeNarcXmlParserTest {

    private CodeNarcXmlParser parser = new CodeNarcXmlParser();

    @ParameterizedTest(name = "should build correct results for {0}")
    @MethodSource
    void shouldBuildCorrectResults_whenValidXmlInput(String fileName, Results expected) throws Exception {
        //when
        Results results = parser.parse(new File(resource(fileName).toURI()));

        //then
        assertThat(results, equalTo(expected));
    }

    static Stream<Arguments> shouldBuildCorrectResults_whenValidXmlInput() {
        return Stream.of(
                arguments("sample/codenarc-empty.xml", emptyResults()),
                arguments("sample/codenarc-default-package-single-file.xml", singleFileResults())
        );
    }

    private static Results emptyResults() {
        return root(emptyList());
    }

    private static Results singleFileResults() {
        return root(List.of(
                directory("", 1, List.of(
                        file("SampleApplication.groovy", List.of(
                                violation(18,
                                        "private TestConsumer testConsumer",
                                        "The field testConsumer is not used within the class SampleApplication",
                                        rule("UnusedPrivateField", 2))
                        ))
                ))));
    }

    private static Results root(List<Results> children) {
        return directory(null, 0, children);
    }

    private static Results directory(String path, int numberOfFiles, List<Results> children) {
        DirectoryResults result = new DirectoryResults(path, numberOfFiles);
        children.forEach(result::addChild);
        return result;
    }

    private static Results file(String name, List<Violation> violations) {
        return new FileResults(name, violations);
    }

    private static Violation violation(int lineNumber, String sourceLine, String message, Rule rule) {
        Violation result = new Violation();
        result.setLineNumber(lineNumber);
        result.setMessage(message);
        result.setSourceLine(sourceLine);
        result.setRule(rule);
        return result;
    }

    private static Rule rule(String name, int priority) {
        StubRule stubRule = new StubRule(priority);
        stubRule.setName(name);
        return stubRule;
    }

    private static URL resource(String name) {
        return CodeNarcXmlParserTest.class.getClassLoader().getResource(name);
    }
}