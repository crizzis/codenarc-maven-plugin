package com.github.crizzis.codenarc.report;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.sink.Sink;
import org.codenarc.results.Results;

import java.util.List;
import java.util.Locale;

class PackageSummaryTableRenderer implements TableRenderer<Results> {

    private static final String DEFAULT_PACKAGE_NAME = "(default package)";
    private static final int PRIORITY_ONE = 1;
    private static final int PRIORITY_TWO = 2;
    private static final int PRIORITY_THREE = 3;

    @Getter
    private final List<String> headers;

    PackageSummaryTableRenderer(Locale locale) {
        this.headers = List.of("Package", "Files with Violations", "Total Violations",
                "Priority 1 Violations", "Priority 2 Violations", "Priority 3 Violations");
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
        return StringUtils.isBlank(element.getPath()) ? DEFAULT_PACKAGE_NAME : element.getPath().replaceAll("/", ".");
    }
}
