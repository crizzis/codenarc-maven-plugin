package com.github.crizzis.codenarc;

import com.github.crizzis.codenarc.integration.GroovyCompilerPluginIntegration;
import com.google.inject.internal.util.Iterables;
import com.google.inject.internal.util.Lists;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.project.MavenProject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * Resolves file sets used for CodeNarc analysis based on the plugin's configuration
 */
@RequiredArgsConstructor
@Builder
class FileSetResolver {

    private static final String DEFAULT_TEST_FILE_SET = "src/test/groovy";
    private static final String DEFAULT_MAIN_FILE_SET = "src/main/groovy";

    private final AnalysisScopeConfig scopeConfig;
    private final Collection<GroovyCompilerPluginIntegration> pluginIntegrations;
    private final MavenProject project;
    private final MavenSession session;

    List<FileSet> resolveFileSets() {
        return Lists.newArrayList(Iterables.concat(
                scopeConfig.isIncludeMain() ? filterFileSets(obtainEffectiveMainSources()) : emptyList(),
                scopeConfig.isIncludeTests() ? filterFileSets(obtainEffectiveTestSources()) : emptyList()
        ));
    }

    private FileSet[] obtainEffectiveTestSources() {
        return obtainEffectiveSourcesFromIntegration(integration -> integration.getTestSources(project.getBuild(), session))
                .orElseGet(() -> obtainEffectiveSourcesFromConfig(scopeConfig.getTestSources(), DEFAULT_TEST_FILE_SET));
    }

    private Optional<FileSet[]> obtainEffectiveSourcesFromIntegration(Function<GroovyCompilerPluginIntegration, Optional<FileSet[]>> getter) {
        return pluginIntegrations.stream()
                .map(getter)
                .flatMap(Optional::stream)
                .findAny();
    }

    private FileSet[] obtainEffectiveMainSources() {
        return obtainEffectiveSourcesFromIntegration(integration -> integration.getSources(project.getBuild(), session))
                .orElseGet(() -> obtainEffectiveSourcesFromConfig(scopeConfig.getSources(), DEFAULT_MAIN_FILE_SET));
    }

    private FileSet[] obtainEffectiveSourcesFromConfig(FileSet[] declaredSources, String defaultDirectory) {
        if (!isEmpty(declaredSources)) {
            return declaredSources;
        }
        return getFileSet(defaultDirectory);
    }

    private FileSet[] getFileSet(String directory) {
        FileSet defaultFileSet = new FileSet();
        defaultFileSet.setDirectory(directory);
        return new FileSet[] {defaultFileSet};
    }

    private List<FileSet> filterFileSets(FileSet[] fileSets) {
        return Arrays.stream(fileSets)
                .map(this::getFilteredFileSetCopy)
                .collect(Collectors.toList());
    }

    private FileSet getFilteredFileSetCopy(FileSet source) {
        FileSet copy = source.clone();
        copy.setIncludes(concatDistinct(copy.getIncludes(), scopeConfig.getIncludes()));
        copy.setExcludes(concatDistinct(copy.getExcludes(), scopeConfig.getExcludes()));
        return copy;
    }

    private List<String> concatDistinct(List<String> left, List<String> right) {
        return Stream.of(left, right)
                .filter(Objects::nonNull)
                .filter(Predicate.not(Collection::isEmpty))
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused")
    static class FileSetResolverBuilder {

        public List<FileSet> resolveFileSets() {
            return build().resolveFileSets();
        }
    }
}
