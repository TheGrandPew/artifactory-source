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

package org.artifactory.model;

import org.artifactory.fs.WatcherInfo;
import org.artifactory.repo.RepoPath;

/**
 * Simple data objects that holds the {@link org.artifactory.repo.RepoPath} in addition to the
 * {@link org.artifactory.fs.WatcherInfo}.
 *
 * @author Yossi Shaul
 */
public class WatcherRepoPathInfo {

    private final RepoPath repoPath;
    private final WatcherInfo info;

    public WatcherRepoPathInfo(RepoPath repoPath, WatcherInfo info) {
        this.repoPath = repoPath;
        this.info = info;
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public WatcherInfo getInfo() {
        return info;
    }
}
