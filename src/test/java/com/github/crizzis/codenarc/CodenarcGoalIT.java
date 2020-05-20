package com.github.crizzis.codenarc;

import com.github.crizzis.codenarc.util.MavenProjectTest;
import com.github.crizzis.codenarc.util.Phrasify;
import com.github.crizzis.codenarc.util.ProjectRoot;
import org.apache.maven.it.Verifier;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junitpioneer.jupiter.DefaultLocale;
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

@DefaultLocale("en-US")
@DisplayNameGeneration(Phrasify.class)
public class CodenarcGoalIT {

    private static final DocumentBuilderFactory XML_FACTORY = DocumentBuilderFactory.newDefaultInstance();

    private DocumentBuilder xmlBuilder = XML_FACTORY.newDocumentBuilder();

    public CodenarcGoalIT() throws ParserConfigurationException {
    }

    @MavenProjectTest("/projects/codenarc-minimal-config")
    void codenarcMinimalConfig_shouldSucceed(Verifier verifier, @ProjectRoot File projectRoot) throws Exception {
        //when
        verifier.executeGoal("site");

        //then
        verifier.verifyErrorFreeLog();
        verifier.assertFilePresent("target/CodeNarc.xml");
        verifier.assertFilePresent("target/site/codenarc.html");
        Document codenarcHtml = parseXml(projectRoot, "target/site/codenarc.html");
        assertThat(codenarcHtml, hasSection(1, "CodeNarc Report"));
        assertThat(codenarcHtml, hasSection(2, "Summary"));
        assertThat(codenarcHtml, hasSection(3, "Package Summary"));
        assertThat(codenarcHtml, hasSection(4, "Files"));
        assertThat(codenarcHtml, hasFileViolation("com/example/TestClassWithSomeViolations.groovy", "BrokenNullCheck"));
        assertThat(codenarcHtml, hasFileViolation("com/example/TestClassWithSomeViolations.groovy", "DuplicateMapKey"));
        assertThat(codenarcHtml, hasFileViolation("com/example/nestedpackage/TestClassWithSomeMoreViolations.groovy", "DeadCode"));
        assertThat(codenarcHtml, hasFileViolation("com/example/nestedpackage/TestClassWithSomeMoreViolations.groovy", "EmptyIfStatement"));
    }

    @MavenProjectTest("/projects/codenarc-custom-config")
    void codenarcCustomConfig_shouldSucceed(Verifier verifier, @ProjectRoot File projectRoot) throws Exception {
        //when
        verifier.executeGoal("codenarc:codenarc");

        //then
        verifier.verifyErrorFreeLog();
        verifier.assertFilePresent("target/reports/CodeNarc.xml");
        verifier.assertFilePresent("target/site/codenarc.html");
        Document codenarcHtml = parseXml(projectRoot, "target/site/codenarc.html");
        assertThat(codenarcHtml, hasSourceDirectory("src/main/additional"));
        assertThat(codenarcHtml, hasFileViolation("com/example/TestClassIncluded.groovy", "ClassEndsWithBlankLine"));
        assertThat(codenarcHtml, hasFileViolation("com/example/TestClassWithSomeViolationsIncluded.groovy", "SpaceAroundMapEntryColon"));
        assertThat(codenarcHtml, not(hasFileViolation("com/example/nestedpackage/TestClassWithSomeMoreViolationsExcluded.groovy", "DeadCode")));
    }

    private Matcher<Node> hasSourceDirectory(String sourceDirectoryPath) {
        return hasXPath("//h3[text() = 'Source Directory: ']/i[text() = '" + sourceDirectoryPath + "']");
    }

    private Matcher<Node> hasSection(int sectionIndex, String caption) {
        return hasXPath("//div[@id='contentBox']/*[@class='section'][" + sectionIndex + "]/h2/text()", equalTo(caption));
    }

    private Matcher<Node> hasFileViolation(final String fileName, final String violation) {
        return hasXPath("//h4[text() = '" + fileName + "']/following-sibling::table[1]/tr/td[text() = '" + violation + "']");
    }

    private Document parseXml(File parent, String path) throws SAXException, IOException {
        return xmlBuilder.parse(new File(parent, path));
    }
}
