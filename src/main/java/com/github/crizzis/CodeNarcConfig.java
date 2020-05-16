package com.github.crizzis;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.maven.model.FileSet;

import java.io.File;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@Builder
public class CodeNarcConfig {

    private final String projectName;
    private final List<FileSet> fileSets;
    private final File outputFile;
    private final boolean generateXmlReport;
    private final List<String> ruleSets;
}
