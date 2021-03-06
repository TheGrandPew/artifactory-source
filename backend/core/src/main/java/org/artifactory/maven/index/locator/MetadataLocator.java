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
/*
 * Additional contributors:
 *    JFrog Ltd.
 */

package org.artifactory.maven.index.locator;

import org.apache.maven.index.locator.Locator;

import java.io.File;

/**
 * @author yoavl
 */
public class MetadataLocator implements Locator {

    @Override
    public File locate(File source) {
        //We never locate metadata
        //return new File(source.getParentFile().getParentFile() - may throw NPE, "maven-metadata.xml") {
        return new File("", "maven-metadata.xml") {
            @Override
            public boolean exists() {
                return false;
            }
        };
    }
}