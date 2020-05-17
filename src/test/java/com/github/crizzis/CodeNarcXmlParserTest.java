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
import java.util.ArrayList;
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
                arguments("sample/codenarc-default-package-single-file.xml", defaultPackageSingleFileResults()),
                arguments("sample/codenarc-default-package-multiple-files.xml", defaultPackageMultipleFilesResults()),
                arguments("sample/codenarc-regular-package-multiple-files.xml", regularPackageMultipleFilesResults()),
                arguments("sample/codenarc-multiple-packages.xml", multiplePackagesResults()),
                arguments("sample/codenarc-multiple-sources.xml", multipleSourcesResults())
        );
    }

    private static Results emptyResults() {
        return root(emptyList());
    }

    private static Results defaultPackageSingleFileResults() {
        return root(List.of(
                directory("", 1, List.of(
                        sampleApplicationFile()
                ))));
    }

    private static Results defaultPackageMultipleFilesResults() {
        return root(List.of(
                directory("", 2, List.of(
                        sampleMessageITFile(),
                        outgoingMessageITFile()
                ))));
    }

    private static Results regularPackageMultipleFilesResults() {
        return root(List.of(
                nestedDirectory(List.of("",
                        "com",
                        "com/example",
                        "com/example/nestedone",
                        "com/example/nestedone/nestedtwo",
                        "com/example/nestedone/nestedtwo/nestedthree",
                        "com/example/nestedone/nestedtwo/nestedthree/nestedfour",
                        "com/example/nestedone/nestedtwo/nestedthree/nestedfour/nestedfive",
                        "com/example/nestedone/nestedtwo/nestedthree/nestedfour/nestedfive/packageone"), 2, List.of(
                        sampleMessageITFile(),
                        outgoingMessageITFile()
                ))));
    }

    private static Results multiplePackagesResults() {
        return root(List.of(
                nestedDirectory(List.of("", "com", "com/example"), 4, List.of(
                        sampleApplicationFile(),
                        directory("com/example/packageone", 2, List.of(
                                sampleMessageITFile(),
                                outgoingMessageITFile()
                        )),
                        directory("com/example/packageonetwo", 1, List.of(
                                messageStoreTestFile()
                        ))
                ))));
    }

    private static Results multipleSourcesResults() {
        return root(List.of(
                nestedDirectory(List.of("",
                        "com",
                        "com/example"), 1, List.of(
                        sampleApplicationFile()
                )),
                nestedDirectory(List.of("",
                        "com",
                        "com/example",
                        "com/example/packageone",
                        "com/example/packageone/packagetwo"), 1, List.of(
                        outgoingMessageITFile()
                ))));
    }

    private static Results sampleApplicationFile() {
        return file("SampleApplication.groovy", List.of(
                violation(18,
                        "private TestConsumer testConsumer",
                        "The field testConsumer is not used within the class SampleApplication",
                        rule("UnusedPrivateField", 2))
        ));
    }

    private static Results sampleMessageITFile() {
        return file("SampleMessageIT.groovy", List.of(
                violation(15,
                        "import static javax.mail.Flags.Flag.SEEN",
                        "Static imports should appear before normal imports",
                        rule("MisorderedStaticImports", 3)),
                violation(16,
                        "import static javax.mail.Message.RecipientType.TO",
                        "Static imports should appear before normal imports",
                        rule("MisorderedStaticImports", 3)),
                violation(17,
                        "import static com.example.Definition.SAMPLE_DEFINITION",
                        "Static imports should appear before normal imports",
                        rule("MisorderedStaticImports", 3)),
                violation(22,
                        "private TestProcessor processor",
                        "The field processor is not used within the class SampleMessageIT",
                        rule("UnusedPrivateField", 2))
        ));
    }

    private static Results outgoingMessageITFile() {
        return file("OutgoingMessageIT.groovy", List.of(
                violation(6,
                        "import static com.example.Samples.SAMPLE_CODE",
                        "Static imports should appear before normal imports",
                        rule("MisorderedStaticImports", 3)),
                violation(13,
                        "import com.example.MessageStatus",
                        "The [com.example.MessageStatus] import is never referenced",
                        rule("UnusedImport", 3)),
                violation(16,
                        "import static com.example.MessageStatus.SENT",
                        "Static imports should appear before normal imports",
                        rule("MisorderedStaticImports", 3))
        ));
    }

    private static Results messageStoreTestFile() {
        return file("MessageStoreTest.groovy", List.of(
                violation(6,
                        "import static com.example.Direction.INCOMING",
                        "Static imports should appear before normal imports",
                        rule("MisorderedStaticImports", 3))
        ));
    }

    private static Results root(List<Results> children) {
        return directory(null, 0, children);
    }

    private static DirectoryResults directory(String path, int numberOfFiles, List<Results> children) {
        DirectoryResults result = new DirectoryResults(path, numberOfFiles);
        children.forEach(result::addChild);
        return result;
    }

    private static Results nestedDirectory(List<String> paths, int numberOfFiles, List<Results> children) {
        DirectoryResults rootDirectory = directory(paths.get(0), numberOfFiles, new ArrayList<>());
        DirectoryResults leafDirectory = rootDirectory;
        for (var path : paths.subList(1, paths.size())) {
            DirectoryResults next = directory(path, numberOfFiles, new ArrayList<>());
            leafDirectory.addChild(next);
            leafDirectory = next;
        }
        children.forEach(leafDirectory::addChild);
        return rootDirectory;
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