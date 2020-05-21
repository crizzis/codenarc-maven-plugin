package io.github.crizzis.codenarc;

import io.github.crizzis.codenarc.parser.CodeNarcAnalysis;
import io.github.crizzis.codenarc.parser.CodeNarcXmlParser;
import io.github.crizzis.codenarc.report.CodeNarcReportGenerator;
import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import javax.inject.Inject;
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
@Execute(goal = "verify")
public class CodeNarcReportMojo extends AbstractMavenReport {

    private final CodeNarcXmlParser xmlParser;
    private final CodeNarcReportGenerator reportGenerator;

    @Inject
    public CodeNarcReportMojo(CodeNarcXmlParser xmlParser, CodeNarcReportGenerator reportGenerator) {
        this.xmlParser = xmlParser;
        this.reportGenerator = reportGenerator;
    }

    /**
     * Location where the generated XML report will be created
     */
    @Parameter(property="codenarc.xmlOutputDirectory", defaultValue = "${project.build.directory}")
    private File xmlOutputDirectory;

    /**
     * Set this to "true" to bypass CodeNarc report generation entirely
     */
    @Parameter(property="codenarc.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public boolean canGenerateReport() {
        File xmlOutputFile = getXmlOutputFile();
        if (isSkip()) {
            getLog().info("Plugin execution skipped");
            return false;
        }
        if (!xmlOutputFile.exists() || !xmlOutputFile.canRead()) {
            getLog().info(String.format("CodeNarc report %s not found - skipping HTML report generation", xmlOutputFile.getPath()));
            return false;
        }
        return true;
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        CodeNarcAnalysis analysis = parseAnalysis();
        generateReport(analysis, locale);
    }

    private void generateReport(CodeNarcAnalysis analysis, Locale locale) {
        getLog().info("Report locale set to " + locale);
        reportGenerator.generate(analysis, getSink(), locale);
    }

    @Override
    public String getOutputName() {
        return "codenarc";
    }

    @Override
    public String getName(Locale locale) {
        return getCodeNarcMessages(locale).getString("report.codenarc.name");
    }

    @Override
    public String getDescription(Locale locale) {
        return getCodeNarcMessages(locale).getString("report.codenarc.description");
    }

    private ResourceBundle getCodeNarcMessages(Locale locale) {
        return ResourceBundle.getBundle("codenarc-messages", locale);
    }

    private CodeNarcAnalysis parseAnalysis() throws MavenReportException {
        File outputFile = getXmlOutputFile();
        try {
            getLog().info("CodeNarc report XML found, parsing");
            CodeNarcAnalysis analysis = xmlParser.parse(outputFile);
            getLog().info("Parsing completed");
            return analysis;
        } catch (CodeNarcXmlParser.XmlParserException e) {
            throw new MavenReportException(String.format("Could not parse: %s", outputFile.getAbsolutePath()), e);
        }
    }

    private File getXmlOutputFile() {
        return new File(getXmlOutputDirectory(), "CodeNarc.xml");
    }
}
