package org.artifactory.version.converter.v227;

import org.artifactory.version.converter.v160.AddonsLayoutConverterTestBase;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.artifactory.version.converter.v227.PropertyDuplicationsConverter.duplicatedMark;
import static org.artifactory.version.converter.v227.PropertyDuplicationsConverter.separator;
import static org.testng.Assert.assertEquals;

public class PropertyDuplicationsConverterTest extends AddonsLayoutConverterTestBase {

    @Test
    public void propertySetDuplicationsAreRemoved() throws Exception {
        Document document = convertXml(
                "/config/test/config.2.2.7_property_set_with_duplications.xml",
                new PropertyDuplicationsConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();

        List<Element> propertySets = rootElement.getChild("propertySets", namespace).getChildren();
        assertEquals(propertySets.size(), 2);

        // check the simple property set, which has no issues in the first place
        checkNoWorriesPropertySet(namespace, propertySets);

        // check the property set which had the duplications
        checkDuplicationsPropertySet(namespace, propertySets);
    }

    private void checkDuplicationsPropertySet(Namespace namespace, List<Element> propertySets) {
        Element duplicationsPropertySet = propertySets.get(1);
        assertEquals(duplicationsPropertySet.getChildText("name", namespace),
                "duplications_property_set");
        assertEquals(duplicationsPropertySet.getChildText("visible", namespace), "true");
        List<Element> duplicationsProperties = duplicationsPropertySet.getChild("properties",
                namespace).getChildren();
        assertEquals(duplicationsProperties.size(), 4);

        // check the property that was duplicated
        Element propertyDuplicationProperty0 = duplicationsProperties.get(0);
        assertEquals(propertyDuplicationProperty0.getChildText("name", namespace),
                "property_duplication");
        List<Element> propertyDuplicationValues0 = propertyDuplicationProperty0.getChild("predefinedValues",
                namespace).getChildren();
        assertEquals(propertyDuplicationValues0.size(), 3);
        assertEquals(propertyDuplicationValues0.get(0).getChildText("value", namespace), "11");
        assertEquals(propertyDuplicationValues0.get(1).getChildText("value", namespace), "22");
        assertEquals(propertyDuplicationValues0.get(2).getChildText("value", namespace), "33");

        Element propertyDuplicationProperty1 = duplicationsProperties.get(1);
        String propName = propertyDuplicationProperty1.getChildText("name", namespace);
        assertEquals(propName.substring(0, propName.indexOf(separator + duplicatedMark)), "property_duplication");
        List<Element> propertyDuplicationValues = propertyDuplicationProperty1.getChild("predefinedValues",
                namespace).getChildren();
        assertEquals(propertyDuplicationValues.size(), 1);
        assertEquals(propertyDuplicationValues.get(0).getChildText("value", namespace), "20");

        // check the property that had duplicated predefined values
        Element predefinedValuesDuplicationProperty = duplicationsProperties.get(2);
        assertEquals(predefinedValuesDuplicationProperty.getChildText("name", namespace),
                "predefined_values_duplication");
        List<Element> valuesDuplicationValues = predefinedValuesDuplicationProperty.getChild("predefinedValues",
                namespace).getChildren();
        assertEquals(valuesDuplicationValues.size(), 3);
        assertEquals(valuesDuplicationValues.get(0).getChildText("value", namespace),
                "duplicated_value");
        assertEquals(valuesDuplicationValues.get(1).getChildText("value", namespace), "value2");
        assertEquals(valuesDuplicationValues.get(2).getChildText("value", namespace), "value3");

        Element propertyDuplicationProperty2 = duplicationsProperties.get(3);
        propName = propertyDuplicationProperty2.getChildText("name", namespace);
        assertEquals(propName.substring(0, propName.indexOf(separator + duplicatedMark)), "property_duplication");
        List<Element> propertyDuplicationValues2 = propertyDuplicationProperty1.getChild("predefinedValues",
                namespace).getChildren();
        assertEquals(propertyDuplicationValues2.size(), 1);
        assertEquals(propertyDuplicationValues2.get(0).getChildText("value", namespace), "20");
    }

    private void checkNoWorriesPropertySet(Namespace namespace, List<Element> propertySets) {
        Element noWorriesPropertySet = propertySets.get(0);
        assertEquals(noWorriesPropertySet.getChildText("name", namespace), "no_worries_property_set");
        assertEquals(noWorriesPropertySet.getChildText("visible", namespace), "true");
        List<Element> noWorriesProperties = noWorriesPropertySet.getChild("properties", namespace).getChildren();
        assertEquals(noWorriesProperties.size(), 1);
        Element noWorriesProperty = noWorriesProperties.get(0);
        assertEquals(noWorriesProperty.getChildText("name", namespace), "no_worries_property");
        List<Element> noWorriesValues = noWorriesProperty.getChild("predefinedValues", namespace).getChildren();
        assertEquals(noWorriesValues.size(), 3);
        assertEquals(noWorriesValues.get(0).getChildText("value", namespace), "1");
        assertEquals(noWorriesValues.get(1).getChildText("value", namespace), "2");
        assertEquals(noWorriesValues.get(2).getChildText("value", namespace), "3");
    }
}