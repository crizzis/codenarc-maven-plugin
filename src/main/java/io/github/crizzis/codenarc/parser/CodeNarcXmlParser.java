package io.github.crizzis.codenarc.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import static java.util.Map.entry;
import static javax.xml.stream.XMLStreamConstants.*;

/**
 *  CodeNarc XML report parser. This class converts XML CodeNarc output files into
 *  instances of {@link org.codenarc.results.Results}
 */
@Named
@Singleton
public class CodeNarcXmlParser {

    private static final XMLInputFactory FACTORY = XMLInputFactory.newInstance();

    public CodeNarcAnalysis parse(File xmlReport) throws XmlParserException {
        try (InputStream xmlInput = new FileInputStream(xmlReport)) {
            XMLEventReader xmlEventReader = FACTORY.createFilteredReader(FACTORY.createXMLEventReader(xmlInput),
                    NodesAttributesAndCharactersOnly.filter());
            CodeNarcXmlEventProcessor processor = new CodeNarcXmlEventProcessor(xmlEventReader);
            processor.process();
            return processor.getAnalysis();
        } catch (IOException | XMLStreamException | ClassCastException
                | IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            throw new XmlParserException(e);
        }
    }

    @RequiredArgsConstructor
    @Getter
    private static class CodeNarcXmlEventProcessor {

        private final XMLEventReader eventReader;

        private final CodeNarcXmlEventConsumer consumer = new CodeNarcXmlEventConsumer();

        private final Map<String, Consumer<EndElement>> END_ELEMENT_CONSUMER_METHODS = Map.ofEntries(
                entry("CodeNarc", consumer::consumeEndCodeNarc),
                entry("Report", consumer::consumeEndReport),
                entry("Project", consumer::consumeEndProject),
                entry("SourceDirectory", consumer::consumeEndSourceDirectory),
                entry("PackageSummary", consumer::consumeEndPackageSummary),
                entry("Package", consumer::consumeEndPackage),
                entry("File", consumer::consumeEndFile),
                entry("Violation", consumer::consumeEndViolation),
                entry("SourceLine", consumer::consumeEndSourceLine),
                entry("Message", consumer::consumeEndMessage)
        );

        private final Map<String, Consumer<StartElement>> START_ELEMENT_CONSUMER_METHODS = Map.ofEntries(
                entry("CodeNarc", consumer::consumeStartCodeNarc),
                entry("Report", consumer::consumeStartReport),
                entry("Project", consumer::consumeStartProject),
                entry("SourceDirectory", consumer::consumeStartSourceDirectory),
                entry("PackageSummary", consumer::consumeStartPackageSummary),
                entry("Package", consumer::consumeStartPackage),
                entry("File", consumer::consumeStartFile),
                entry("Violation", consumer::consumeStartViolation),
                entry("SourceLine", consumer::consumeStartSourceLine),
                entry("Message", consumer::consumeStartMessage)
        );

        void process() throws XMLStreamException {
            while (eventReader.hasNext()) {
                XMLEvent xmlEvent = eventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    handleStartElement(eventReader, xmlEvent.asStartElement());
                } else if (xmlEvent.isEndElement()) {
                    handleEndElement(xmlEvent.asEndElement());
                } else if (xmlEvent.isCharacters()) {
                    handleCharacters(xmlEvent.asCharacters());
                }
            }
        }

        private void handleCharacters(Characters characters) {
            consumer.consumeCharacters(characters);
        }

        private void handleEndElement(EndElement endElement) {
            String name = endElement.getName().getLocalPart();
            END_ELEMENT_CONSUMER_METHODS.getOrDefault(name, element -> verifyIgnoredTag(name)).accept(endElement);
        }

        private void handleStartElement(XMLEventReader eventReader, StartElement startElement) throws XMLStreamException {
            String name = startElement.getName().getLocalPart();
            if (consumer.getSkippedTags().contains(name)) {
                skipUntilEnd(eventReader, name);
            }
            START_ELEMENT_CONSUMER_METHODS.getOrDefault(name, element -> verifyIgnoredTag(name)).accept(startElement);
        }

        private void skipUntilEnd(XMLEventReader eventReader, String name) throws XMLStreamException {
            XMLEvent xmlEvent;
            do {
                xmlEvent = eventReader.nextEvent();
            } while (!xmlEvent.isEndElement() || !xmlEvent.asEndElement().getName().getLocalPart().equals(name));
        }

        private void verifyIgnoredTag(String name) {
            if (!consumer.getIgnoredTags().contains(name)) {
                throw new IllegalArgumentException("Unrecognized tag " + name);
            }
        }

        public CodeNarcAnalysis getAnalysis() {
            return consumer.getAnalysis();
        }
    }

    private static class NodesAttributesAndCharactersOnly implements EventFilter {

        private static final List<Integer> IGNORED_EVENTS = List.of(START_DOCUMENT, END_DOCUMENT, ENTITY_DECLARATION,
                ENTITY_REFERENCE, COMMENT, PROCESSING_INSTRUCTION, NAMESPACE, SPACE, NOTATION_DECLARATION);

        public static EventFilter filter() {
            return new NodesAttributesAndCharactersOnly();
        }

        @Override
        public boolean accept(XMLEvent event) {
            if (event.isCharacters()) {
                return !event.asCharacters().isWhiteSpace();
            }
            return !IGNORED_EVENTS.contains(event.getEventType());
        }
    }

    public static class XmlParserException extends Exception {

        public XmlParserException(Throwable cause) {
            super(cause);
        }
    }
}
