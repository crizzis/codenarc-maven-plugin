package com.github.crizzis;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.FileSet;

import java.util.Optional;

public interface GroovyCompilerPluginIntegration {

    Optional<FileSet[]> getSources(Build build, MavenSession mavenSession);

    Optional<FileSet[]> getTestSources(Build build, MavenSession mavenSession);
}
