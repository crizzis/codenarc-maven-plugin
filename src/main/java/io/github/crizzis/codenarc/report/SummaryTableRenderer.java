package io.github.crizzis.codenarc.report;

import lombok.Getter;
import org.apache.maven.doxia.sink.Sink;
import org.codenarc.results.DirectoryResults;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

class SummaryTableRenderer implements TableRenderer<DirectoryResults>, Localizable {

    private static final int PRIORITY_ONE = 1;
    private static final int PRIORITY_TWO = 2;
    private static final int PRIORITY_THREE = 3;

    @Getter
    private final List<String> headers;

    SummaryTableRenderer(Locale locale) {
        ResourceBundle messages = getCodeNarcMessages(locale);
        this.headers = List.of(
                messages.getString("report.codenarc.total_files"),
                messages.getString("report.codenarc.files_with_violations"),
                messages.getString("report.codenarc.total_violations"),
                messages.getString("report.codenarc.priority_one_violations"),
                messages.getString("report.codenarc.priority_two_violations"),
                messages.getString("report.codenarc.priority_three_violations"));
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
