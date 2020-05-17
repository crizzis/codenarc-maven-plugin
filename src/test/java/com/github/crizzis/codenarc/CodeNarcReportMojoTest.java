package com.github.crizzis.codenarc;

import com.github.crizzis.codenarc.util.Phrasify;
import org.apache.maven.reporting.MavenReportException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@DisplayNameGeneration(Phrasify.class)
class CodeNarcReportMojoTest {

    private CodeNarcReportGenerator generator = mock(CodeNarcReportGenerator.class);
    private CodeNarcXmlParser xmlParser = mock(CodeNarcXmlParser.class);
    private CodeNarcReportMojo mojo = initializeMojo();

    private CodeNarcReportMojo initializeMojo() {
        return new CodeNarcReportMojo(xmlParser, generator);
    }

    @ParameterizedTest
    @MethodSource
    void getDescription_shouldLoadDescriptionFromBundle(Locale locale, String expected) {
        //when, then
        assertEquals(mojo.getDescription(locale), expected);
    }

    static Stream<Arguments> getDescription_shouldLoadDescriptionFromBundle() {
        return Stream.of(
                arguments(new Locale("en"), "Generates a source code report with the CodeNarc Library"),
                arguments(new Locale("de"), "DE Generates a source code report with the CodeNarc Library")
        );
    }

    @ParameterizedTest
    @MethodSource
    void getName_shouldLoadNameFromBundle(Locale locale, String expected) {
        //when, then
        assertEquals(mojo.getName(locale), expected);
    }

    static Stream<Arguments> getName_shouldLoadNameFromBundle() {
        return Stream.of(
                arguments(new Locale("en"), "CodeNarc Report"),
                arguments(new Locale("de"), "DE CodeNarc Report")
        );
    }

    @Test
    void getOutputName_shouldReturnPluginName() {
        //when, then
        assertEquals(mojo.getOutputName(), "codenarc");
    }

    @Test
    void canGenerateReport_shouldReturnFalse_whenSkipIsTrue() throws Exception {
        //given
        File xmlOutput = new File(resource("sample/CodeNarc.xml").toURI());
        mojo.setXmlOutputDirectory(xmlOutput.getParentFile());
        mojo.setSkip(true);

        //when, then
        assertFalse(mojo.canGenerateReport());
    }

    @Test
    void canGenerateReport_shouldReturnTrue_whenSkipIsFalseAndFileExists() throws Exception {
        //given
        File xmlOutput = new File(resource("sample/CodeNarc.xml").toURI());
        mojo.setXmlOutputDirectory(xmlOutput.getParentFile());
        mojo.setSkip(false);

        //when, then
        assertTrue(mojo.canGenerateReport());
    }

    @Test
    void canGenerateReport_shouldReturnFalse_whenCodeNarcReportFileNotExists() {
        //given
        mojo.setXmlOutputDirectory(new File("nonexistent.xml"));

        //when, then
        assertFalse(mojo.canGenerateReport());
    }

    @Test
    void executeReport_shouldGenerateReport_whenConfiguredCorrectly() throws Exception {
        //given
        CodeNarcAnalysis analysis = new CodeNarcAnalysis();
        Locale locale = new Locale("en-US");
        mojo.setXmlOutputDirectory(new File("output"));
        when(xmlParser.parse(new File("output/CodeNarc.xml"))).thenReturn(analysis);

        //when
        mojo.executeReport(locale);

        //then
        verify(generator).generate(eq(analysis), any(), eq(locale));
    }

    @Test
    void executeReport_shouldReportError_whenXmlReportParsingError() throws Exception {
        //given
        mojo.setXmlOutputDirectory(new File("output"));
        when(xmlParser.parse(new File("output/CodeNarc.xml"))).thenThrow(CodeNarcXmlParser.XmlParserException.class);

        //when, then
        MavenReportException thrown = assertThrows(MavenReportException.class,
                () -> mojo.executeReport(new Locale("en-US")));
        assertThat(thrown.getMessage(), startsWith("Could not parse: "));
        assertThat(thrown.getCause(), instanceOf(CodeNarcXmlParser.XmlParserException.class));
    }

    private static URL resource(String name) {
        return CodeNarcVerifyMojo.class.getClassLoader().getResource(name);
    }
}