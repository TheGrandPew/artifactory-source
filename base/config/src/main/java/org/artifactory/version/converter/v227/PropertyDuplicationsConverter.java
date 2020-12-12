package org.artifactory.version.converter.v227;

import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PropertyDuplicationsConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(PropertyDuplicationsConverter.class);
    static final String duplicatedMark = "duplicated";
    static final String separator = "_";
    // format is: <original name>_duplicated_<unique suffix>
    private static final String duplicatedPropertyNameFormat = "%s" + separator + duplicatedMark + separator + "%s";

    @Override
    public void convert(Document doc) {
        log.info("Converting descriptor using {}", getClass().getSimpleName());
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        for (Element propertySet : getChildren(rootElement, "propertySets", namespace)) {
            Set<String> propertiesSeen = new HashSet<>();
            List<Element> properties = getChildren(propertySet, "properties", namespace);
            for (Element property : properties) {
                appendAsDuplicatedPropertyIfDuplicated(property, namespace, propertiesSeen);
                Set<String> predefinedValuesSeen = new HashSet<>();
                Iterator<Element> predefinedValuesIterator =
                        getChildrenIterator(property, "predefinedValues", namespace);
                while (predefinedValuesIterator.hasNext()) {
                    Element predefinedValue = predefinedValuesIterator.next();
                    removeValueIfDuplicated(namespace, predefinedValuesSeen, predefinedValuesIterator,
                            predefinedValue, property.getChildText("name", namespace));
                }
            }
        }
    }

    public void removeValueIfDuplicated(Namespace namespace, Set<String> predefinedValuesSeen,
            Iterator<Element> predefinedValuesIterator, Element predefinedValue, String propertyNameString) {
        Element value = predefinedValue.getChild("value", namespace);
        if (value != null) {
            String textValue = value.getText();
            if (predefinedValuesSeen.contains(textValue)) {
                log.warn("preDefinedValue {} of property {} is duplicated while preDefinedValues must be unique. " +
                        "Removed redundant duplication", textValue, propertyNameString);
                predefinedValuesIterator.remove();
            } else {
                predefinedValuesSeen.add(textValue);
            }
        }
    }

    public void appendAsDuplicatedPropertyIfDuplicated(Element property, Namespace namespace,
            Set<String> propertiesSeen) {
        Element propertyName = property.getChild("name", namespace);
        String propertyNameString = propertyName.getText();
        if (propertiesSeen.contains(propertyNameString)) {
            String uniqueId = UUID.randomUUID().toString();
            String newName = String.format(duplicatedPropertyNameFormat, propertyNameString, uniqueId);
            propertyName.setText(newName);
            log.warn("Property {} is duplicated while name must be unique. Changed duplicated name to {}",
                    propertyNameString, newName);
        } else {
            propertiesSeen.add(propertyNameString);
        }
    }

    private static List<Element> getChildren(Element element, String name, Namespace namespace) {
        Element propertySets = element.getChild(name, namespace);
        return propertySets == null ? Collections.emptyList() : propertySets.getChildren();
    }

    private static Iterator<Element> getChildrenIterator(Element element, String name, Namespace namespace) {
        return getChildren(element, name, namespace).iterator();
    }
}
