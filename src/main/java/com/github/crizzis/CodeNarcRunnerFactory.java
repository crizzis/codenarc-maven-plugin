package com.github.crizzis;

import org.codenarc.CodeNarcRunner;

import javax.inject.Named;
import javax.inject.Singleton;

@Named
@Singleton
public class CodeNarcRunnerFactory {

    public CodeNarcRunner newCodeNarcRunner(CodeNarcConfig config) {
        CodeNarcRunner runner = new CodeNarcRunner();
        // setSourceAnalyzer
        // setResultsProcessor
        // setReportWriters
        // setRulesetFiles
        return runner;
    }

}
