package com.github.crizzis.util;

import org.junit.jupiter.api.DisplayNameGenerator;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.DisplayNameGenerator.parameterTypesAsString;

public class Phrasify implements DisplayNameGenerator {

    private final Standard wrapped = new Standard();

    @Override
    public String generateDisplayNameForClass(Class<?> testClass) {
        return wrapped.generateDisplayNameForClass(testClass);
    }

    @Override
    public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
        return phrasify(wrapped.generateDisplayNameForNestedClass(nestedClass));
    }

    @Override
    public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
        String displayName = phrasify(testMethod.getName());
        if (hasParameters(testMethod)) {
            displayName += ' ' + parameterTypesAsString(testMethod);
        }
        return displayName;
    }

    private boolean hasParameters(Method testMethod) {
        return testMethod.getParameterCount() > 0;
    }

    private String phrasify(String input) {
        return input.replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2")
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("_", " ")
                .toLowerCase();
    }
}
