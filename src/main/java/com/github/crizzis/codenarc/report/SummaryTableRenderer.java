package com.github.crizzis.codenarc.report;

import lombok.Getter;
import org.apache.maven.doxia.sink.Sink;
import org.codenarc.results.DirectoryResults;

import java.util.List;
import java.util.Locale;

class SummaryTableRenderer implements TableRenderer<DirectoryResults> {

    private static final int PRIORITY_ONE = 1;
    private static final int PRIORITY_TWO = 2;
    private static final int PRIORITY_THREE = 3;

    @Getter
    private final List<String> headers;

    SummaryTableRenderer(Locale locale) {
        this.headers = List.of("Total Files", "Files with Violations", "Total Violations",
                "Priority 1 Violations", "Priority 2 Violations", "Priority 3 Violations");
    }

    @Override
    public void renderCell(Sink sink, DirectoryResults element, int index) {
        sink.text(String.valueOf(getValue(element, index)));
    }

    public int getValue(DirectoryResults element, int index) {
        switch (index) {
            case 0:
                return element.getTotalNumberOfFiles(false);
            case 1:
                return element.getNumberOfFilesWithViolations(PRIORITY_THREE);
            case 2:
                return (element.getNumberOfViolationsWithPriority(PRIORITY_ONE)
                        + element.getNumberOfViolationsWithPriority(PRIORITY_TWO)
                        + element.getNumberOfViolationsWithPriority(PRIORITY_THREE));
            case 3:
                return element.getNumberOfViolationsWithPriority(PRIORITY_ONE);
            case 4:
                return element.getNumberOfViolationsWithPriority(PRIORITY_TWO);
            case 5:
                return element.getNumberOfViolationsWithPriority(PRIORITY_THREE);
            default:
                throw new IllegalArgumentException("Invalid table cell index");
        }
    }
}
