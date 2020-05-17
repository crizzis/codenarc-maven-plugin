package com.github.crizzis.codenarc;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.apache.maven.plugins.annotations.LifecyclePhase.SITE;

/**
 * Create a CodeNarc report (implies the execution of the {@code verify} goal)
 */
@Getter
@Setter
@Mojo(name = "codenarc", defaultPhase = SITE)
@Execute(goal = "spotbugs:verify")
public class CodeNarcReportMojo extends AbstractMavenReport {

    /**
     * Location where the generated XML report will be created
     */
    @Parameter(property="codenarc.xmlOutputDirectory", defaultValue = "${project.build.directory}")
    private File xmlOutputDirectory;

    /**
     * Set this to "true" to bypass CodeNarc verification entirely
     */
    @Parameter(property="codenarc.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public boolean canGenerateReport() {
        File xmlOutputFile = getXmlOutputFile();
        if (!xmlOutputFile.exists() || !xmlOutputFile.canRead()) {
            getLog().info(String.format("CodeNarc report %s not found - skipping HTML report generation", xmlOutputFile.getPath()));
            return false;
        }
        return true;
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        throw new MavenReportException("Report generation is not supported");
    }

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

    private File getXmlOutputFile() {
        return new File(getXmlOutputDirectory(), "CodeNarc.xml");
    }
}
