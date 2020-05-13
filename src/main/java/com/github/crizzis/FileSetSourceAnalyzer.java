package com.github.crizzis;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.maven.model.FileSet;
import org.codenarc.analyzer.AbstractSourceAnalyzer;
import org.codenarc.analyzer.FilesystemSourceAnalyzer;
import org.codenarc.results.DirectoryResults;
import org.codenarc.results.Results;
import org.codenarc.ruleset.RuleSet;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A composite-style analyzer that supports multiple {@link FileSet}s
 * by using multiple {@link FilesystemSourceAnalyzer}s
 */
@RequiredArgsConstructor
public class FileSetSourceAnalyzer extends AbstractSourceAnalyzer {

    @NonNull
    private final List<FileSet> fileSets;

    @Override
    @SuppressWarnings("unchecked")
    public Results analyze(RuleSet ruleSet) {
        return fileSets.stream()
                .map(fileSet -> analyze(fileSet, ruleSet))
                .collect(Collector.of(DirectoryResults::new, DirectoryResults::addChild, (left, right) -> {
                    ((List<Results>) right.getChildren()).forEach(left::addChild);
                    return left;
                }));
    }

    private Results analyze(FileSet fileSet, RuleSet ruleSet) {
        FilesystemSourceAnalyzer analyzer = new FilesystemSourceAnalyzer();
        analyzer.setBaseDirectory(fileSet.getDirectory());
        analyzer.setIncludes(String.join(",", fileSet.getIncludes()));
        analyzer.setExcludes(String.join(",", fileSet.getIncludes()));
        return (Results) analyzer.analyze(ruleSet).getChildren().get(0);
    }

    @Override
    public List<String> getSourceDirectories() {
        return fileSets.stream()
                .map(FileSet::getDirectory)
                .collect(Collectors.toList());
    }
}
