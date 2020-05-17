package com.github.crizzis.codenarc.util;

import lombok.RequiredArgsConstructor;
import org.codenarc.results.Results;
import org.codenarc.rule.Rule;
import org.codenarc.rule.Violation;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

import static lombok.AccessLevel.PRIVATE;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor(access = PRIVATE)
public class CodeNarcResultsMatcher extends BaseMatcher<Results> {

    private final Results expected;
    private Results deepMismatchExpected;
    private Results deepMismatchActual;

    public static CodeNarcResultsMatcher equalToResults(Results expected) {
        return new CodeNarcResultsMatcher(expected);
    }

    @Override
    public boolean matches(Object actual) {
        return areEqual((Results) actual, expected);
    }

    private boolean areEqual(Results actual, Results expected) {
        boolean actualIsFile = actual.isFile();
        boolean expectedIsFile = expected.isFile();
        if (actualIsFile != expectedIsFile) {
            recordMismatch(actual, expected);
            return false;
        }
        if (actualIsFile) {
            return areFilesEqual(actual, expected);
        }
        if (!areListsEqual(actual.getChildren(), expected.getChildren(), this::areEqual)) {
            recordMismatch(actual, expected); //only needed when children lists differ in size
            return false;
        }
        return true;
    }

    private boolean areFilesEqual(Results actual, Results expected) {
        if (!actual.getPath().equals(expected.getPath())) {
            recordMismatch(actual, expected);
            return false;
        }
        if (!areListsEqual(actual.getViolations(), expected.getViolations(), violationComparator())) {
            recordMismatch(actual, expected);
            return false;
        }
        return true;
    }

    private Comparator<Violation> violationComparator() {
        return Comparator
                .comparing(Violation::getLineNumber)
                .thenComparing(Violation::getMessage)
                .thenComparing(Violation::getSourceLine)
                .thenComparing(Violation::getRule, ruleComparator());
    }

    private Comparator<Rule> ruleComparator() {
        return Comparator.comparing(Rule::getName)
                .thenComparingInt(Rule::getPriority)
                .thenComparingInt(Rule::getCompilerPhase);
    }

    private <T> boolean areListsEqual(List<T> actual, List<T> expected, Comparator<T> comparator) {
        return areListsEqual(actual, expected,
                (BiPredicate<T, T>) (actualElement, expectedElement) -> comparator.compare(actualElement, expectedElement) == 0);
    }

    private <T> boolean areListsEqual(List<T> actual, List<T> expected, BiPredicate<T, T> comparisonPredicate) {
        Iterator<T> actualIterator = actual.iterator();
        Iterator<T> expectedIterator = expected.iterator();
        while (actualIterator.hasNext() && expectedIterator.hasNext()) {
            if (!comparisonPredicate.test(actualIterator.next(), expectedIterator.next())) {
                return false;
            }
        }
        return !actualIterator.hasNext() && !expectedIterator.hasNext();
    }

    private void recordMismatch(Results actual, Results expected) {
        if (Objects.isNull(deepMismatchExpected) || Objects.isNull(deepMismatchActual)) {
            this.deepMismatchExpected = expected;
            this.deepMismatchActual = actual;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("Results equal to ").appendValue(expected);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendValue(deepMismatchActual)
                .appendText(" does not match ")
                .appendValue(deepMismatchExpected);
    }
}
