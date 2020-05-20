package com.github.crizzis.codenarc.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.xml.parsers.DocumentBuilderFactory;

@UtilityClass
public class MinimalDocumentBuilderFactory {

    @SneakyThrows
    public static DocumentBuilderFactory newMinimalInstance() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        factory.setFeature("http://xml.org/sax/features/namespaces", false);
        factory.setFeature("http://xml.org/sax/features/validation", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory;
    }
}
