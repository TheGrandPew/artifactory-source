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

package org.artifactory.aql.api.domain.sensitive;

import com.google.common.collect.Lists;
import org.artifactory.aql.api.internal.AqlApiDynamicFieldsDomains;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlPhysicalFieldEnum;
import org.artifactory.aql.result.rows.AqlProperty;

import java.util.ArrayList;

/**
 * @author Gidi Shabat
 */
public class AqlApiProperty extends AqlBase<AqlApiProperty, AqlProperty> {

    public AqlApiProperty() {
        super(AqlProperty.class, true);
    }

    public AqlApiProperty(boolean useDefaultResults) {
        super(AqlProperty.class, useDefaultResults);
    }

    public static AqlApiProperty create() {
        return new AqlApiProperty();
    }

    public static AqlApiProperty createWithEmptyResults() {
        return new AqlApiProperty(false);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiProperty> propertyItemId() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.propertyItemId, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiProperty> propertyId() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator<>(AqlPhysicalFieldEnum.propertyId, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiProperty> key() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.propertyKey, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiComparator<AqlApiProperty> value() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties);
        return new AqlApiDynamicFieldsDomains.AqlApiComparator(AqlPhysicalFieldEnum.propertyValue, subDomains);
    }

    public static AqlBase.PropertyCriteriaClause<AqlApiProperty> property(String key, AqlComparatorEnum comparator,
                                                                          String value) {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties);
        return new AqlBase.PropertyCriteriaClause(key, comparator, value, subDomains);
    }

    public static AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains<AqlApiProperty> item() {
        ArrayList<AqlDomainEnum> subDomains = Lists.newArrayList(AqlDomainEnum.properties, AqlDomainEnum.items);
        return new AqlApiDynamicFieldsDomains.AqlApiItemDynamicFieldsDomains(subDomains);
    }
}
