package com.github.crizzis.codenarc;

import org.apache.maven.doxia.sink.Sink;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * CodeNarc report generator. Takes an instance of {@link CodeNarcAnalysis} as input
 */
@Named
@Singleton
public class CodeNarcReportGenerator {

    public void generate(CodeNarcAnalysis input, Sink sink, Locale locale) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private ResourceBundle getCodeNarcMessages(Locale locale) {
        return ResourceBundle.getBundle("codenarc-messages", locale);
    }
}
