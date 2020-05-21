package io.github.crizzis.codenarc.report;

import java.util.Locale;
import java.util.ResourceBundle;

public interface Localizable {
    default ResourceBundle getCodeNarcMessages(Locale locale) {
        return ResourceBundle.getBundle("codenarc-messages", locale);
    }
}
