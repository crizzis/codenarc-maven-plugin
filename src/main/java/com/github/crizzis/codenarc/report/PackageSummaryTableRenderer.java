package com.github.crizzis.codenarc.report;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.sink.Sink;
import org.codenarc.results.Results;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

class PackageSummaryTableRenderer implements TableRenderer<Results>, Localizable {

    private static final int PRIORITY_ONE = 1;
    private static final int PRIORITY_TWO = 2;
    private static final int PRIORITY_THREE = 3;

    private final String defaultPackageName;

    @Getter
    private final List<String> headers;

    PackageSummaryTableRenderer(Locale locale) {
        ResourceBundle messages = getCodeNarcMessages(locale);
        this.defaultPackageName = messages.getString("report.codenarc.default_package");
        this.headers = List.of(
                messages.getString("report.codenarc.package"),
                messages.getString("report.codenarc.files_with_violations"),
                messages.getString("report.codenarc.total_violations"),
                messages.getString("report.codenarc.priority_one_violations"),
                messages.getString("report.codenarc.priority_two_violations"),
                messages.getString("report.codenarc.priority_three_violations"));
    }

    @Override
    public void renderCell(Sink sink, Results element, int index) {
        switch (index) {
            case 0:
                sink.text(getPackageName(element));
                break;
            case 1:
                sink.text(String.valueOf(element.getNumberOfFilesWithViolations(PRIORITY_THREE, false)));
                break;
            case 2:
                sink.text(String.valueOf(element.getNumberOfViolationsWithPriority(PRIORITY_ONE, false)
                        + element.getNumberOfViolationsWithPriority(PRIORITY_TWO, false)
                        + element.getNumberOfViolationsWithPriority(PRIORITY_THREE, false)));
                break;
            case 3:
                sink.text(String.valueOf(element.getNumberOfViolationsWithPriority(PRIORITY_ONE, false)));
                break;
            case 4:
                sink.text(String.valueOf(element.getNumberOfViolationsWithPriority(PRIORITY_TWO, false)));
                break;
            case 5:
                sink.text(String.valueOf(element.getNumberOfViolationsWithPriority(PRIORITY_THREE, false)));
                break;
        }
    }

    private String getPackageName(Results element) {
        return StringUtils.isBlank(element.getPath()) ? defaultPackageName : element.getPath().replaceAll("/", ".");
    }
}
