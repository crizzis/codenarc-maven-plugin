package com.github.crizzis;

import org.codenarc.results.Results;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

@Named
@Singleton
public class CodeNarcXmlParser {

    public Results reconstruct(File xmlReport) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
