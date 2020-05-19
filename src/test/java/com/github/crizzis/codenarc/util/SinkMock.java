package com.github.crizzis.codenarc.util;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.doxia.sink.Sink;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SinkMock implements InvocationHandler {

    private static final String INDENT_STRING = "    ";
    private static final String END_TAG_SUFFIX = "_";

    private StringBuilder result;

    @Getter
    private boolean flushed;

    @Getter
    private boolean closed;

    private int indent;

    public Sink initialize() {
        result = new StringBuilder();
        appendStartTag("report");
        return (Sink) Proxy.newProxyInstance(SinkMock.class.getClassLoader(), new Class[] { Sink.class }, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        if (isFlush(method)) {
            flushed = true;
        } else if (isClose(method)) {
            closed = true;
        } if (isText(method)) {
            appendElement(currentIndent(), args[0]);
        } else if (isEndTag(method)) {
            appendEndTag(method.getName());
        } else {
            appendStartTag(method.getName());
        }
        return null;
    }

    private void appendStartTag(String methodName) {
        appendTag(methodName, increasedIndent(), false);
    }

    private void appendEndTag(String methodName) {
        appendTag(methodName, decreasedIndent(), true);
    }

    private boolean isClose(Method method) {
        return method.getName().equals("close");
    }

    private boolean isFlush(Method method) {
        return method.getName().equals("flush");
    }

    private boolean isText(Method method) {
        return method.getName().equals("text");
    }

    private void appendTag(String methodName, String indent, boolean end) {
        appendElement(indent, getTag(methodName, end));
    }

    private void appendElement(String prefix, Object element) {
        result.append(prefix)
                .append(element)
                .append("\n");
    }

    private String getTag(String methodName, boolean end) {
        return "<" + (end ? "/" : "") + StringUtils.stripEnd(methodName, END_TAG_SUFFIX) + ">";
    }

    private String decreasedIndent() {
        return StringUtils.repeat(INDENT_STRING, (--indent));
    }

    private String currentIndent() {
        return StringUtils.repeat(INDENT_STRING, indent);
    }

    private String increasedIndent() {
        return StringUtils.repeat(INDENT_STRING, (indent++));
    }

    private boolean isEndTag(Method method) {
        return method.getName().endsWith(END_TAG_SUFFIX);
    }

    public String terminate() {
        appendEndTag("report_");
        String report = result.toString();
        result = new StringBuilder();
        return report;
    }
}
