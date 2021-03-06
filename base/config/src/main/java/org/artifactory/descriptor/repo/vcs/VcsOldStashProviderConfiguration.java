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

package org.artifactory.descriptor.repo.vcs;

/**
 * Vcs git provider for BitBucket before version 5.1.0
 * Use BitBucket archive plugin to download archive
 * @link https://www.jfrog.com/jira/browse/RTFACT-14817
 *
 * @author Michael Pasternak
 */
public class VcsOldStashProviderConfiguration extends VcsProviderConfiguration {

    public VcsOldStashProviderConfiguration() {
        super(
                "Stash prior 5.1",
                "scm/",
                "plugins/servlet/archive/projects/{0}/repos/{1}?at={2}&format={3}"
        );
    }
}