package com.github.crizzis;

import org.codenarc.CodeNarcRunner;
import org.codenarc.report.XmlReportWriter;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.List;

/**
 * Configures a {@link org.codenarc.CodeNarcRunner} used for running the {@code verify} goal
 */
@Named
@Singleton
public class CodeNarcRunnerFactory {

    public CodeNarcRunner newCodeNarcRunner(CodeNarcConfig config) {
        CodeNarcRunner runner = new CodeNarcRunner();
        runner.setRuleSetFiles(joinedRuleSets(config));
        if (config.isGenerateXmlReport()) {
            configureXmlOutput(config, runner);
        }
        runner.setSourceAnalyzer(new FileSetSourceAnalyzer(config.getFileSets()));
        return runner;
    }

    private void configureXmlOutput(CodeNarcConfig config, CodeNarcRunner runner) {
        XmlReportWriter xmlReportWriter = new XmlReportWriter();
        xmlReportWriter.setTitle(config.getProjectName());
        xmlReportWriter.setOutputFile(config.getOutputFile().getAbsolutePath());
        runner.setReportWriters(List.of(xmlReportWriter));
    }

    private String joinedRuleSets(CodeNarcConfig config) {
        return String.join(",", config.getRuleSets());
    }

}
