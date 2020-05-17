package com.github.crizzis;

import com.github.crizzis.util.MavenProjectTest;
import com.github.crizzis.util.Phrasify;
import com.github.crizzis.util.ProjectRoot;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(Phrasify.class)
class VerifyGoalIntegrationIT {

    private static final DocumentBuilderFactory XML_FACTORY = DocumentBuilderFactory.newDefaultInstance();

    private DocumentBuilder xmlBuilder = XML_FACTORY.newDocumentBuilder();

    VerifyGoalIntegrationIT() throws ParserConfigurationException {
    }

    @MavenProjectTest("/projects/verify-minimal-config-no-violations")
    void verifyMinimalConfig_shouldSucceed(Verifier verifier, @ProjectRoot File projectRoot) throws Exception {
        //when
        verifier.executeGoal("verify");

        //then
        verifier.assertFilePresent("target/CodeNarc.xml");
        Document codeNarcReport = parseXml(projectRoot, "target/CodeNarc.xml");
        assertThat(codeNarcReport, hasProjectName("verify-minimal-config-no-violations"));
        verifier.verifyTextInLog("CodeNarc completed: (p1=0; p2=0; p3=0)");
        verifier.verifyErrorFreeLog();
    }

    @MavenProjectTest("/projects/verify-minimal-config-violations-above-threshold")
    void verifyMinimalConfig_shouldFail_whenViolationsAboveThreshold(Verifier verifier, @ProjectRoot File projectRoot)
            throws Exception {
        //when, then
        VerificationException exception = assertThrows(VerificationException.class, () -> verifier.executeGoal("verify"));
        assertThat(exception.getMessage(), startsWith("Exit code was non-zero"));
        verifier.verifyTextInLog("CodeNarc completed: (p1=0; p2=5; p3=0)");
        verifier.verifyTextInLog("totalPriority2Violations exceeded threshold of 3 errors with 5");

        verifier.assertFilePresent("target/CodeNarc.xml");
        Document codeNarcReport = parseXml(projectRoot, "target/CodeNarc.xml");
        assertThat(codeNarcReport, includesSourceDirectory(1, "src/main/groovy"));
    }

    @MavenProjectTest("/projects/verify-custom-config")
    void verifyCustomConfig_shouldRespectCustomSettings(Verifier verifier, @ProjectRoot File projectRoot)
            throws Exception {
        //when
        verifier.executeGoal("codenarc:verify");

        //then
        verifier.verifyTextInLog("Using rule sets: rulesets/formatting.xml");
        verifier.verifyTextInLog("custom.xml");
        verifier.verifyTextInLog("CodeNarc completed: (p1=0; p2=6; p3=10)");

        verifier.assertFilePresent("target/reports/CodeNarc.xml");
        Document codeNarcReport = parseXml(projectRoot, "target/reports/CodeNarc.xml");
        assertThat(codeNarcReport, includesSourceDirectory(1, "src/main/groovy"));
        assertThat(codeNarcReport, includesSourceDirectory(2, "src/main/additional"));
        assertThat(codeNarcReport, includesSourceDirectory(3, "src/test/groovy"));
        assertThat(codeNarcReport, includesFile("TestClassIncluded.groovy"));
        assertThat(codeNarcReport, not(includesFile("TestClassWithSomeMoreViolationsExcluded.groovy")));
    }

    @MavenProjectTest("/projects/verify-preexisting-report")
    void verifyPreexistingReport_shouldConsumePreexistingReportCorrectly(Verifier verifier, @ProjectRoot File projectRoot)
            throws Exception {
        //when
        verifier.executeGoal("codenarc:verify@generate-report");

        //then
        verifier.verifyTextInLog("CodeNarc completed: (p1=0; p2=5; p3=0)");
        verifier.assertFilePresent("target/CodeNarc.xml");

        //when, then
        verifier.resetStreams();
        verifier.setAutoclean(false);
        assertThrows(VerificationException.class, () -> verifier.executeGoal("codenarc:verify@consume-report"));
        verifier.verifyTextInLog("Parsing completed: (p1=0; p2=5; p3=0)");
        verifier.verifyTextInLog("totalPriority2Violations exceeded threshold of 3 errors with 5");
    }

    private Matcher<Node> includesSourceDirectory(int index, String sourceDirectory) {
        return hasXPath("/CodeNarc/Project/SourceDirectory[" + index + "]", equalTo(sourceDirectory));
    }

    private Matcher<Node> hasProjectName(String projectName) {
        return hasXPath("/CodeNarc/Project/@title", equalTo(projectName));
    }

    private Matcher<Node> includesFile(String filename) {
        return hasXPath("/CodeNarc/Package/File[@name='" + filename + "']");
    }

    private Document parseXml(@ProjectRoot File projectRoot, String path) throws SAXException, IOException {
        return xmlBuilder.parse(new File(projectRoot, path));
    }

}
