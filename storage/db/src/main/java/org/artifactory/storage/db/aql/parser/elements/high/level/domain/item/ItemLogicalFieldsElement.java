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

package org.artifactory.storage.db.aql.parser.elements.high.level.domain.item;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlLogicalFieldEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.DomainProviderElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.RealFieldElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.*;
import static org.artifactory.storage.db.aql.parser.AqlParser.*;

/**
 * @author Gidi Shabat
 *         Acepts the items fields and its sub domain
 */
public class ItemLogicalFieldsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        AqlLogicalFieldEnum[] fields = items.getLogicalFields();
        for (AqlLogicalFieldEnum field : fields) {
            list.add(new RealFieldElement(field.getSignature(), items));
        }
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(archives.signature), dot, archiveLogicalFields));
        list.add(forward(new InternalNameElement(artifacts.signature), dot, buildArtifactLogicalFields));
        list.add(forward(new InternalNameElement(dependencies.signature), dot, buildDependenciesLogicalFields));
        list.add(forward(new InternalNameElement(statistics.signature), dot, statisticsLogicalFields));
        list.add(forward(new InternalNameElement(properties.signature), dot, propertiesLogicalFields));
        list.add(forward(new InternalNameElement(releaseBundleFiles.signature), dot, releaseBundleFilesLogicalFields));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return items;
    }
}
