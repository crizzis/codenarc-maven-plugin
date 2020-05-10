package com.github.crizzis;

import lombok.experimental.Delegate;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenMultiPageReport;
import org.apache.maven.reporting.MavenReportException;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This base class is needed as {@link org.apache.maven.reporting.AbstractMavenReport}
 * hides the {@link org.apache.maven.plugin.MojoFailureException} exception declaration
 */
public class FailableMavenReport extends AbstractMojo implements MavenMultiPageReport {

    @Delegate(excludes = Mojo.class)
    private final AbstractMavenReport delegate = new AbstractMavenReport() {

        @Override
        public String getOutputName() {
            return "codenarc";
        }

        @Override
        public String getName(Locale locale) {
            return ResourceBundle.getBundle("codenarc", locale).getString("report.codenarc.name");
        }

        @Override
        public String getDescription(Locale locale) {
            return ResourceBundle.getBundle("codenarc", locale).getString("report.codenarc.description");
        }

        @Override
        protected void executeReport(Locale locale) throws MavenReportException {
            FailableMavenReport.this.doExecuteReport(locale);
        }
    };

    protected void doExecuteReport(Locale locale) throws MavenReportException {

    }


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        delegate.execute();
    }
}
