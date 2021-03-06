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

package org.artifactory.maven.snapshot;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.maven.MavenModelUtils;
import org.artifactory.mime.MavenNaming;
import org.artifactory.storage.fs.tree.ItemNode;

/**
 * @author Gidi Shabat
 */
@SuppressWarnings("UnusedDeclaration")
public class TimestampSnapshotComparator implements SnapshotComparator {

    private static final TimestampSnapshotComparator instance = new TimestampSnapshotComparator();

    /**
     * The more specific, strings only version comparator
     */

    public static TimestampSnapshotComparator get() {
        return instance;
    }

    @Override
    public int compare(ItemNode o1, ItemNode o2) {
        String snapshot1 = MavenNaming.getUniqueSnapshotVersionTimestamp(o1.getName());
        String snapshot2 = MavenNaming.getUniqueSnapshotVersionTimestamp(o2.getName());
        long time1 = MavenModelUtils.uniqueSnapshotTimestampToDate(snapshot1).getTime();
        long time2 = MavenModelUtils.uniqueSnapshotTimestampToDate(snapshot2).getTime();
        long dif = time1 - time2;
        return dif > 0 ? 1 : dif < 0 ? -1 : 0;
    }

    @Override
    public int compare(Snapshot o1, Snapshot o2) {
        long time1 = MavenModelUtils.uniqueSnapshotTimestampToDate(o1.getTimestamp()).getTime();
        long time2 = MavenModelUtils.uniqueSnapshotTimestampToDate(o2.getTimestamp()).getTime();
        long dif = time1 - time2;
        return dif > 0 ? 1 : dif < 0 ? -1 : 0;
    }

    @Override
    public int compare(SnapshotVersion o1, SnapshotVersion o2) {
        // Prefer the "updated" field instead of relying on the "value" field -
        // this one is less prone to formatting issues.
        String o1Timestamp = o1.getUpdated();
        String o2Timestamp = o2.getUpdated();
        if (StringUtils.isBlank(o1Timestamp) || StringUtils.isBlank(o2Timestamp)) {
            // Fallback to using the version if updated is missing
            o1Timestamp = o1.getVersion().substring(o1.getVersion().indexOf('-') + 1, o1.getVersion().lastIndexOf('-'));
            o2Timestamp = o2.getVersion().substring(o2.getVersion().indexOf('-') + 1, o2.getVersion().lastIndexOf('-'));
        }
        long time1 = MavenModelUtils.uniqueSnapshotTimestampToDate(o1Timestamp).getTime();
        long time2 = MavenModelUtils.uniqueSnapshotTimestampToDate(o2Timestamp).getTime();
        long dif = time1 - time2;
        return dif > 0 ? 1 : dif < 0 ? -1 : 0;
    }

    @Override
    public int compare(ModuleInfo o1, ModuleInfo o2) {
        String version1 = o1.getFileIntegrationRevision();
        String timestamp1 = version1.substring(0, version1.lastIndexOf('-'));
        String version2 = o2.getFileIntegrationRevision();
        String timestamp2 = version2.substring(0, version2.lastIndexOf('-'));
        long time1 = MavenModelUtils.uniqueSnapshotTimestampToDate(timestamp1).getTime();
        long time2 = MavenModelUtils.uniqueSnapshotTimestampToDate(timestamp2).getTime();
        long dif = time1 - time2;
        return dif > 0 ? 1 : dif < 0 ? -1 : 0;

    }
}
