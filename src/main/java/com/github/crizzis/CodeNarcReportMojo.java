package com.github.crizzis;

import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.MavenReportException;

import java.io.File;
import java.util.Locale;

import static org.apache.maven.plugins.annotations.LifecyclePhase.SITE;

/**
 * Create a CodeNarc report (implies the execution of the {@code verify} goal)
 */
@Mojo(name = "codenarc", defaultPhase = SITE)
@Execute(goal = "spotbugs:verify")
public class CodeNarcReportMojo extends AbstractCodeNarcMojo {

    /**
     * Location where the generated HTML report will be created
     */
    @Parameter(property = "codenarc.outputDirectory", defaultValue = "${project.reporting.outputDirectory}")
    private File outputDirectory;

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
    protected void doExecuteReport(Locale locale) throws MavenReportException {
        throw new MavenReportException("Report generation is not supported");
    }

}
