package com.github.crizzis;

import com.github.crizzis.util.MavenProjectTest;
import com.github.crizzis.util.Phrasify;
import com.github.crizzis.util.ProjectRoot;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Tag;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration-test")
@DisplayNameGeneration(Phrasify.class)
class VerifyGoalIntegrationTest {

    private static final DocumentBuilderFactory XML_FACTORY = DocumentBuilderFactory.newDefaultInstance();

    private DocumentBuilder xmlBuilder = XML_FACTORY.newDocumentBuilder();

    VerifyGoalIntegrationTest() throws ParserConfigurationException {
    }

    @MavenProjectTest("/projects/verify-minimal-config-no-violations")
    void verifyMinimalConfig_shouldSucceed(Verifier verifier) throws VerificationException {
        //when
        verifier.executeGoal("verify");

        //then
        verifier.assertFilePresent("target/codenarc.xml");
        verifier.verifyTextInLog("CodeNarc completed: (p1=0; p2=0; p3=0)");
        verifier.verifyErrorFreeLog();
    }

    @MavenProjectTest("/projects/verify-minimal-config-violations-above-threshold")
    void verifyMinimalConfig_shouldFail_whenViolationsAboveThreshold(Verifier verifier, @ProjectRoot File projectRoot) throws VerificationException, IOException, SAXException {
        //when, then
        VerificationException exception = assertThrows(VerificationException.class, () -> verifier.executeGoal("verify"));
        assertThat(exception.getMessage(), startsWith("Exit code was non-zero"));
        verifier.verifyTextInLog("CodeNarc completed: (p1=0; p2=5; p3=4)");
        verifier.verifyTextInLog("totalPriority2Violations exceeded threshold of 3 errors with 5");

        verifier.assertFilePresent("target/codenarc.xml");
        Document codeNarcReport = parseCodeNarcXml(projectRoot);
        assertThat(codeNarcReport, hasXPath("/CodeNarc/Project/SourceDirectory", equalTo("src/main/groovy")));
    }

    private Document parseCodeNarcXml(@ProjectRoot File projectRoot) throws SAXException, IOException {
        return xmlBuilder.parse(new File(projectRoot, "target/codenarc.xml"));
    }

}
