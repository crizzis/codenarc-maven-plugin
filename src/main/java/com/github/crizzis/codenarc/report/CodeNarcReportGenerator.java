package com.github.crizzis.codenarc.report;

import com.github.crizzis.codenarc.parser.CodeNarcAnalysis;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.sink.Sink;
import org.codenarc.results.DirectoryResults;
import org.codenarc.results.Results;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static com.github.crizzis.codenarc.report.ResultWalker.*;

/**
 * CodeNarc report generator. Takes an instance of {@link CodeNarcAnalysis} as input
 */
@Named
@Singleton
public class CodeNarcReportGenerator {

    private static final int MAX_PRIORITY = 3;

    private final ResultWalker resultWalker = new ResultWalker();

    public void generate(CodeNarcAnalysis input, Sink sink, Locale locale) {
        sink.head();
        generateHead(input, sink, locale);
        sink.head_();
        sink.body();
        generateBody(input, sink, locale);
        sink.body_();
    }

    private void generateHead(CodeNarcAnalysis input, Sink sink, Locale locale) {
        sink.title();
        sink.text("CodeNarc Report");
        sink.title_();
    }

    private void generateBody(CodeNarcAnalysis input, Sink sink, Locale locale) {
        generateHeading(input, sink, locale);
        generateSummary(input, sink, locale);
        if (hasViolations(input)) {
            generatePackageSummary(input, sink, locale);
            generateFileViolations(input, sink, locale);
        }
    }

    private void generateHeading(CodeNarcAnalysis input, Sink sink, Locale locale) {
        sink.section1();
        printSectionTitle(sink, "CodeNarc Report");
        printParagraph(sink, "The following document contains the results of CodeNarc analysis");
        printKeyValue(sink, "CodeNarc Version", input.getCodeNarcVersion());
        printKeyValue(sink, "Report time", input.getReportTimestamp());
        sink.section1_();
    }

    private void generateSummary(CodeNarcAnalysis input, Sink sink, Locale locale) {
        sink.section1();
        printSectionTitle(sink, "Summary");
        printTable(sink, new SummaryTableRenderer(locale), List.of((DirectoryResults) input.getResults()));
        sink.section1_();
    }

    private void generatePackageSummary(CodeNarcAnalysis input, Sink sink, Locale locale) {
        sink.section1();
        printSectionTitle(sink, "Package Summary");
        Iterator<String> sourceRootDirectories = input.getSourceDirectories().iterator();
        input.getResults().getChildren().forEach(sourceRoot -> {
            if (sourceRootDirectories.hasNext()) {
                printSourceDirectoryTitle(sink, sourceRootDirectories.next());
            }
            printResultTable(sink, new PackageSummaryTableRenderer(locale), (Results) sourceRoot, DIRECTORIES_WITH_FILES);
        });
        sink.section1_();
    }

    private void generateFileViolations(CodeNarcAnalysis input, Sink sink, Locale locale) {
        sink.section1();
        printSectionTitle(sink, "Files");
        final Iterator<String> sourceRootDirectories = input.getSourceDirectories().iterator();
        final CurrentPackageContext context = new CurrentPackageContext();
        resultWalker.walk(input.getResults(), DIRECTORIES_WITH_FILES.or(FILES).or(SOURCE_ROOTS), results -> {
            if (results.isFile()) {
                printSubSubSectionTitle(sink, toFilePath(context.getCurrentPackage(), results));
                printTable(sink, new ViolationTableRenderer(locale), results.getViolations());
            } else {
                if (isSourceRoot(results) && sourceRootDirectories.hasNext()) {
                    printSourceDirectoryTitle(sink, sourceRootDirectories.next());
                }
                context.setCurrentPackage(results);
            }
        });
        sink.section1_();
    }

    private void printSectionTitle(Sink sink, String title) {
        sink.sectionTitle1();
        sink.text(title);
        sink.sectionTitle1_();
    }

    private void printSourceDirectoryTitle(Sink sink, String sourceDirectory) {
        sink.sectionTitle2();
        sink.text("Source Directory" + ": ");
        sink.italic();
        sink.text(sourceDirectory);
        sink.italic_();
        sink.sectionTitle2_();
    }

    private void printSubSubSectionTitle(Sink sink, String title) {
        sink.sectionTitle3();
        sink.text(title);
        sink.sectionTitle3_();
    }

    private void printKeyValue(Sink sink, String key, String value) {
        sink.paragraph();
        sink.text(key + ": ");
        sink.italic();
        sink.text(value);
        sink.italic_();
        sink.paragraph_();
    }

    private void printParagraph(Sink sink, String paragraphText) {
        sink.paragraph();
        sink.text(paragraphText);
        sink.paragraph_();
    }

    private <T> void printTable(Sink sink, TableRenderer<T> renderer, List<T> elements) {
        renderer.renderTable(sink, elements);
    }

    private void printResultTable(Sink sink, TableRenderer<Results> renderer, Results root, Predicate<Results> include) {
        renderer.renderTable(sink, renderingCallback -> resultWalker.walk(root, include, renderingCallback));
    }

    private boolean hasViolations(CodeNarcAnalysis input) {
        return input.getResults().getNumberOfFilesWithViolations(MAX_PRIORITY, true) > 0;
    }

    private String toFilePath(Results directory, Results file) {
        return (StringUtils.isBlank(directory.getPath()) ? "" : directory.getPath() + "/") + file.getPath();
    }

    private boolean isSourceRoot(Results results) {
        return results.getPath().isBlank();
    }

    private ResourceBundle getCodeNarcMessages(Locale locale) {
        return ResourceBundle.getBundle("codenarc-messages", locale);
    }

    @Getter
    @Setter
    private static class CurrentPackageContext {

        private Results currentPackage;
    }
}
