package com.github.crizzis;

import com.google.inject.internal.util.Iterables;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.codenarc.results.DirectoryResults;
import org.codenarc.results.FileResults;
import org.codenarc.results.Results;
import org.codenarc.rule.StubRule;
import org.codenarc.rule.Violation;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.*;

@SuppressWarnings("unchecked")
class CodeNarcXmlEventConsumer {

    public static final String PATH_SEPARATOR = "/";
    private Deque<Object> currentContext = new ArrayDeque<>();
    private DirectoryResults lastDirectory;

    Set<String> getIgnoredTags() {
        return Set.of("CodeNarc", "Report", "Rules", "SourceDirectory", "Rule", "Description");
    }

    Set<String> getSkippedTags() {
        return Set.of("Report", "Rules", "Project");
    }

    void consumeCharacters(Characters characters) {
        peekAs(TextHolder.class).setValue(characters.getData());
    }

    void consumeStartReport(StartElement startElement) {}

    void consumeStartProject(StartElement startElement) {}

    void consumeStartPackageSummary(StartElement packageSummary) {
        currentContext.push(new DirectoryResults(null, getTotalFiles(packageSummary)));
    }

    void consumeStartPackage(StartElement packageElement) {
        currentContext.push(new DirectoryResults(getPath(packageElement), getTotalFiles(packageElement)));
    }

    void consumeStartFile(StartElement file) {
        currentContext.push(new FileResultsHolder(getName(file), new ArrayList<Violation>()));
    }

    void consumeStartViolation(StartElement violationElement) {
        Violation violation = new Violation();
        violation.setLineNumber(getLineNumber(violationElement));
        StubRule rule = new StubRule(getPriority(violationElement));
        rule.setName(getRuleName(violationElement));
        violation.setRule(rule);
        currentContext.push(violation);
    }

    void consumeStartSourceLine(StartElement sourceLine) {
        currentContext.push(new SourceLineHolder());
    }

    void consumeStartMessage(StartElement message) {
        currentContext.push(new MessageHolder());
    }

    void consumeEndReport(EndElement endElement) {}

    void consumeEndProject(EndElement endElement) {}

    void consumeEndPackageSummary(EndElement endElement) {}

    void consumeEndPackage(EndElement endElement) {
        DirectoryResults child = popAs(DirectoryResults.class);
        DirectoryResults parent = peekAs(DirectoryResults.class);
        embedChildInParent(child, parent);
    }

    private void embedChildInParent(DirectoryResults child, DirectoryResults parent) {
        if (child.getPath().isEmpty()) {
            parent.addChild(child);
            return;
        }
        DirectoryResults previous = null;
        while (parent != null) {
            previous = parent;
            parent = findAntecedentForPath(parent, child.getPath());
        }
        if (previous != null) {
            previous.addChild(child);
        }
    }

    private DirectoryResults findAntecedentForPath(DirectoryResults parent, String path) {
        ListIterator<Results> childIterator = parent.getChildren().listIterator(parent.getChildren().size());
        while (childIterator.hasPrevious()) {
            Results previous = childIterator.previous();
            if (!previous.isFile() && isAntecedentForPath(previous, path)) {
                return (DirectoryResults) previous;
            }
        }
        return null;
    }

    private boolean isAntecedentForPath(Results directory, String path) {
        return directory.getPath().isEmpty() || path.startsWith(directory.getPath() + PATH_SEPARATOR);
    }

    void consumeEndFile(EndElement endElement) {
        FileResultsHolder child = popAs(FileResultsHolder.class);
        peekAs(DirectoryResults.class).addChild(child.toFileResults());
    }

    void consumeEndViolation(EndElement endElement) {
        Violation child = popAs(Violation.class);
        peekAs(FileResultsHolder.class).getViolations().add(child);
    }

    void consumeEndSourceLine(EndElement endElement) {
        SourceLineHolder child = popAs(SourceLineHolder.class);
        peekAs(Violation.class).setSourceLine(child.getValue());
    }

    void consumeEndMessage(EndElement endElement) {
        MessageHolder child = popAs(MessageHolder.class);
        peekAs(Violation.class).setMessage(child.getValue());
    }

    private Integer getTotalFiles(StartElement startElement) {
        return Integer.valueOf(startElement.getAttributeByName(new QName("totalFiles")).getValue());
    }

    private String getPath(StartElement startElement) {
        return startElement.getAttributeByName(new QName("path")).getValue();
    }

    private String getName(StartElement startElement) {
        return startElement.getAttributeByName(new QName("name")).getValue();
    }

    private String getRuleName(StartElement startElement) {
        return startElement.getAttributeByName(new QName("ruleName")).getValue();
    }

    private Integer getPriority(StartElement startElement) {
        return Integer.valueOf(startElement.getAttributeByName(new QName("priority")).getValue());
    }

    private Integer getLineNumber(StartElement startElement) {
        return Integer.valueOf(startElement.getAttributeByName(new QName("lineNumber")).getValue());
    }

    private <T> T popAs(Class<T> expectedClass) {
        return (T) currentContext.pop();
    }

    private <T> T peekAs(Class<T> expectedClass) {
        return (T) currentContext.peek();
    }

    Results getResults() {
        if (currentContext.size() > 1) {
            throw new IllegalStateException("Malformed XML report input, " + currentContext + " left to parse");
        }
        return (Results) Iterables.getOnlyElement(currentContext);
    }

    private interface TextHolder {
        void setValue(String value);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class SourceLineHolder implements TextHolder {
        private String value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class MessageHolder implements TextHolder {
        private String value;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class FileResultsHolder { //needed because FileResults does not allow for appending new violations
        private String name;
        private List<Violation> violations = new ArrayList<>();

        public Results toFileResults() {
            return new FileResults(name, violations);
        }
    }
}
