package com.github.crizzis.codenarc;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.FileSet;

import java.util.Optional;

/**
 * Defines an entry point for integration with Groovy compiler plugins.
 *
 * Implementations of this interface should scan the project build configuration
 * in search of a specific Groovy compiler plugin and, if found, extract compile sources configuration
 * from the configuration of that plugin. The obtained compile sources configuration, if found, will then be used
 * if no custom {@code sources}/{@code testSources} configuration has been specified for the {@code verify} goal
 */
public interface GroovyCompilerPluginIntegration {

    Optional<FileSet[]> getSources(Build build, MavenSession mavenSession);

    Optional<FileSet[]> getTestSources(Build build, MavenSession mavenSession);
}
