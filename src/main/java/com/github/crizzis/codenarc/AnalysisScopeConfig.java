package com.github.crizzis.codenarc;

import org.apache.maven.model.FileSet;

import java.util.List;

public interface AnalysisScopeConfig {

    List<String> getIncludes();

    List<String> getExcludes();

    FileSet[] getSources();

    FileSet[] getTestSources();

    boolean isIncludeTests();

    boolean isIncludeMain();
}
