package com.github.crizzis.codenarc.report;

import lombok.Getter;
import org.apache.maven.doxia.sink.Sink;
import org.codenarc.rule.Violation;

import java.util.List;
import java.util.Locale;

class ViolationTableRenderer implements TableRenderer<Violation> {

    @Getter
    private final List<String> headers;

    ViolationTableRenderer(Locale locale) {
        this.headers = List.of("Rule Name", "Priority", "Line", "Source Line / Message");
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
