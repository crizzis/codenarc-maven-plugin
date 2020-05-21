package io.github.crizzis.codenarc.report;

import org.codenarc.results.Results;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Performs a DFS traversal of CodeNarc results
 */
@SuppressWarnings("unchecked")
class ResultWalker {

    static final Predicate<Results> FILES = Results::isFile;
    static final Predicate<Results> DIRECTORIES = Predicate.not(FILES);
    static final Predicate<Results> SOURCE_ROOTS = DIRECTORIES.and(results -> Objects.nonNull(results.getPath()) && results.getPath().isBlank());
    static final Predicate<Results> DIRECTORIES_WITH_FILES = results -> results.getChildren().stream().anyMatch(FILES);

    private final Deque<Results> toVisit = new ArrayDeque<>();

    void walk(Results results, Predicate<Results> filter, Consumer<Results> action) {
        if (filter.test(results)) {
            action.accept(results);
        }
        results.getChildren().stream()
                .sorted(Comparator.comparing(Results::isFile).reversed())
                .forEachOrdered(child -> walk((Results) child, filter, action));
    }
}
