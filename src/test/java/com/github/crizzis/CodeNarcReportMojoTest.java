package com.github.crizzis;

import com.github.crizzis.util.Phrasify;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(Phrasify.class)
class CodeNarcReportMojoTest {

    @Test
    void executeReport_shouldNotRun_whenSkipIsTrue() {

    }

    @Test
    void executeReport_shouldNotRun_whenCodeNarcReportFileNotExists() {

    }

    @Test
    void executeReport_shouldForwardErrorsFromReportGenerator() {

    }

    @Test
    void getDescription_shouldLoadDescriptionFromBundle() {

    }

    @Test
    void getName_shouldLoadNameFromBundle() {

    }

    @Test
    void getOutputName_shouldReturnPluginName() {

    }
}