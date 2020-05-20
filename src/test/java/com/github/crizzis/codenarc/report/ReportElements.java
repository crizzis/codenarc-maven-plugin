package com.github.crizzis.codenarc.report;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReportElements {
    TITLE("//head/title"),
    GENERAL_INFO_SECTION_TITLE("//body/section1[1]/sectionTitle1/text()"),
    DESCRIPTION("//body/section1[1]/paragraph[1]/text()"),
    VERSION_CAPTION("//body/section1[1]/paragraph[2]/text()"),
    GENERATION_TIME("//body/section1[1]/paragraph[3]/text()"),

    SUMMARY_SECTION_TITLE("//body/section1[2]/sectionTitle1/text()"),
    SUMMARY_TOTAL_FILES_HEADER("//body/section1[2]/table//tableHeaderCell[1]"),
    SUMMARY_FILES_WITH_VIOLATIONS_HEADER("//body/section1[2]/table//tableHeaderCell[2]"),
    SUMMARY_TOTAL_VIOLATIONS_HEADER("//body/section1[2]/table//tableHeaderCell[3]"),
    SUMMARY_PRIORITY_ONE_VIOLATIONS_HEADER("//body/section1[2]/table//tableHeaderCell[4]"),
    SUMMARY_PRIORITY_TWO_VIOLATIONS_HEADER("//body/section1[2]/table//tableHeaderCell[5]"),
    SUMMARY_PRIORITY_THREE_VIOLATIONS_HEADER("//body/section1[2]/table//tableHeaderCell[6]"),
    PACKAGE_SUMMARY_TITLE("//body/section1[3]/sectionTitle1/text()"),
    PACKAGE_SUMMARY_SOURCE_DIRECTORY_CAPTION("//body/section1[3]/sectionTitle2/text()"),
    PACKAGE_SUMMARY_PACKAGE_HEADER("//body/section1[3]/table//tableHeaderCell[1]/text()"),
    PACKAGE_SUMMARY_FILES_WITH_VIOLATIONS_HEADER("//body/section1[3]/table//tableHeaderCell[2]/text()"),
    PACKAGE_SUMMARY_TOTAL_VIOLATION_HEADER("//body/section1[3]/table//tableHeaderCell[3]/text()"),
    PACKAGE_SUMMARY_PRIORITY_ONE_VIOLATIONS_HEADER("//body/section1[3]/table//tableHeaderCell[4]/text()"),
    PACKAGE_SUMMARY_PRIORITY_TWO_VIOLATIONS_HEADER("//body/section1[3]/table//tableHeaderCell[5]/text()"),
    PACKAGE_SUMMARY_PRIORITY_THREE_VIOLATIONS_HEADER("//body/section1[3]/table//tableHeaderCell[6]/text()"),
    FILES_TITLE("//body/section1[4]/sectionTitle1/text()"),
    FILES_SOURCE_DIRECTORY_CAPTION("//body/section1[4]/sectionTitle2/text()"),
    FILES_RULE_NAME_HEADER("//body/section1[4]/table//tableHeaderCell[1]/text()"),
    FILES_PRIORITY_HEADER("//body/section1[4]/table//tableHeaderCell[2]/text()"),
    FILES_LINE_HEADER("//body/section1[4]/table//tableHeaderCell[3]/text()"),
    FILES_SOURCE_LINE_MESSAGE_HEADER("//body/section1[4]/table//tableHeaderCell[4]/text()");

    private final String xpath;

    public String path() {
        return xpath;
    }
}
