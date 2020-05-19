package com.github.crizzis.codenarc.report;

import org.apache.maven.doxia.sink.Sink;

import java.util.List;
import java.util.function.Consumer;

public interface TableRenderer<T> {

    default void renderTable(Sink sink, Consumer<Consumer<T>> callback) {
        sink.table();
        renderHeader(sink);
        callback.accept(element -> renderRow(sink, element));
        sink.table_();
    }

    default void renderTable(Sink sink, Iterable<T> elements) {
        renderTable(sink, elements::forEach);
    }

    default void renderHeader(Sink sink) {
        sink.tableRow();
        getHeaders().forEach(header -> {
            sink.tableHeaderCell();
            sink.text(header);
            sink.tableHeaderCell_();
        });
        sink.tableRow_();
    }

    List<String> getHeaders();

    default void renderRow(Sink sink, T element) {
        sink.tableRow();
        for (int i = 0; i < getColumnCount(); ++i) {
            sink.tableCell();
            renderCell(sink, element, i);
            sink.tableCell_();
        }
        sink.tableRow_();
    }

    void renderCell(Sink sink, T element, int index);

    default int getColumnCount() {
        return getHeaders().size();
    }
}
