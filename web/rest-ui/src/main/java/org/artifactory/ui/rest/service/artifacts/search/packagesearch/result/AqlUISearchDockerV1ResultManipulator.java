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

package org.artifactory.ui.rest.service.artifacts.search.packagesearch.result;

import org.artifactory.ui.rest.model.artifacts.search.packagesearch.criteria.PackageSearchCriteria;
import org.artifactory.ui.rest.model.artifacts.search.packagesearch.result.PackageSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adds a docker image field as the search only returns tags and image prop is set on the _index_images.json file
 *
 * @author Dan Feldman
 */
public class AqlUISearchDockerV1ResultManipulator implements AqlUISearchResultManipulator {
    private static final Logger log = LoggerFactory.getLogger(AqlUISearchDockerV1ResultManipulator.class);

    @Override
    public void manipulate(PackageSearchResult result) {
        if (!result.getExtraFields().keySet().contains(PackageSearchCriteria.dockerV1Image.name())) {
            String tagName = result.getExtraFields().get(PackageSearchCriteria.dockerV1Tag.name()).iterator().next();
            Matcher v1ImageMatcher = Pattern.compile(".*repositories/(.*)/" + tagName + "/tag.json")
                    .matcher(result.getRelativePath());
            if (v1ImageMatcher.matches()) {
                String image = v1ImageMatcher.group(1);
                log.debug("Docker V1 result manipulator adding field image to result extra fields - " +
                        "artifact path: '{}', tag: '{}', image: '{}'", result.getRepoPath().toPath(), tagName, image);
                result.getExtraFieldsMap().put(PackageSearchCriteria.dockerV1Image.name(), image);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass();
    }
}