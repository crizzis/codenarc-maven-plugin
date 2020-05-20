package com.github.crizzis.codenarc.report;

import lombok.Getter;
import org.apache.maven.doxia.sink.Sink;
import org.codenarc.rule.Violation;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

class ViolationTableRenderer implements TableRenderer<Violation>, Localizable {

    @Getter
    private final List<String> headers;

    ViolationTableRenderer(Locale locale) {
        ResourceBundle messages = getCodeNarcMessages(locale);
        this.headers = List.of(
                messages.getString("report.codenarc.rule_name"),
                messages.getString("report.codenarc.priority"),
                messages.getString("report.codenarc.line"),
                messages.getString("report.codenarc.source_line_message"));
    }

    @Override
    public void renderCell(Sink sink, Violation element, int index) {
        switch (index) {
            case 0:
                sink.text(element.getRule().getName());
                break;
            case 1:
                sink.text(String.valueOf(element.getRule().getPriority()));
                break;
            case 2:
                sink.text(String.valueOf(element.getLineNumber()));
                break;
            case 3:
                sink.paragraph();
                sink.italic();
                sink.text(element.getSourceLine());
                sink.italic_();
                sink.paragraph_();
                sink.paragraph();
                sink.text(element.getMessage());
                sink.paragraph_();
                break;
        }
    }
}
