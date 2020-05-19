package com.github.crizzis.codenarc.parser;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.codenarc.results.Results;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class CodeNarcAnalysis {

    private String codeNarcVersion;
    private String projectTitle;
    private LocalDateTime reportTimestamp;
    private Results results;
    private List<String> sourceDirectories = new ArrayList<>();

    public void addSourceDirectory(String sourceDirectory) {
        sourceDirectories.add(sourceDirectory);
    }
}
