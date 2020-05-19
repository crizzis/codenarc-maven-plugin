package com.github.crizzis.codenarc;

import lombok.experimental.UtilityClass;
import org.codenarc.results.DirectoryResults;
import org.codenarc.results.FileResults;
import org.codenarc.results.Results;
import org.codenarc.rule.Rule;
import org.codenarc.rule.StubRule;
import org.codenarc.rule.Violation;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

@UtilityClass
public final class ResultsSamples {

    public static Results emptyResults() {
        return root(0, emptyList());
    }

    public static Results defaultPackageSingleFileResults() {
        return root(1, List.of(
                directory("", 1, List.of(
                        sampleApplicationFile()
                ))));
    }

    public static Results defaultPackageMultipleFilesResults() {
        return root(2, List.of(
                directory("", 2, List.of(
                        sampleMessageITFile(),
                        outgoingMessageITFile()
                ))));
    }

    public static Results regularPackageMultipleFilesResults() {
        return root(2, List.of(
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

    public static Results multiplePackagesResults() {
        return root(4, List.of(
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

    public static Results multipleSourcesResults() {
        return root(2, List.of(
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

    private static Results root(int numberOfFiles, List<Results> children) {
        return directory(null, numberOfFiles, children);
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

}
