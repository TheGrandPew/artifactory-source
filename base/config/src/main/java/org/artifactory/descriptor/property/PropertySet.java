/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.descriptor.property;

import org.artifactory.descriptor.Descriptor;
import org.artifactory.util.AlreadyExistsException;
import org.jfrog.common.config.diff.DiffKey;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A collection of tag definitions, which will be eventually instantiated as properties.
 *
 * @author Yoav Landman
 */
@XmlType(name = "PropertySetType", propOrder = {"name", "visible", "properties"}, namespace = Descriptor.NS)
@GenerateDiffFunction
public class PropertySet implements Descriptor {

    public static final String ARTIFACTORY_RESERVED_PROP_SET = "artifactory";
    @XmlID
    @XmlElement(required = true)
    @DiffKey
    private String name;

    @XmlElement(defaultValue = "true")
    private boolean visible = true;

    @XmlElementWrapper(name = "properties")
    @XmlElement(name = "property", required = false)
    private List<Property> properties = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = Optional.ofNullable(properties).orElseGet(ArrayList::new);
    }

    public Property getProperty(String propertyName) {
        for (Property property : properties) {
            if (property.getName().equals(propertyName)) {
                return property;
            }
        }

        return null;
    }

    public boolean isPropertyExists(String propertyName) {
        return getProperty(propertyName) != null;
    }

    public void addProperty(Property property) {
        String propertyName = property.getName();
        if (isPropertyExists(propertyName)) {
            throw new AlreadyExistsException("Property " + property + " already exists");
        }
        properties.add(property);
    }

    public Property removeProperty(String propertyName) {
        Property property = getProperty(propertyName);
        if (property == null) {
            return null;
        }

        //Remove the property from the property list
        properties.remove(property);

        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PropertySet)) {
            return false;
        }

        PropertySet set = (PropertySet) o;

        return name != null ? name.equals(set.name) : set.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
