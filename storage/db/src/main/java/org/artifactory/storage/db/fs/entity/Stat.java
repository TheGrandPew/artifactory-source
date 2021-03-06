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

package org.artifactory.storage.db.fs.entity;

import com.google.common.base.Strings;

/**
 * Represents a record in the stats table.
 *
 * @author Yossi Shaul
 */
public class Stat {
    private final long nodeId;

    private final long downloadCount;
    private final long lastDownloaded;
    private final String lastDownloadedBy;

    private  long remoteDownloadCount;
    private  long remoteLastDownloaded;
    private  String remoteLastDownloadedBy;

    private String origin;
    private String path;

    public Stat(long nodeId, long downloadCount, long lastDownloaded, String lastDownloadedBy) {
        this.nodeId = nodeId;

        this.downloadCount = downloadCount;
        this.lastDownloadedBy = lastDownloadedBy;
        this.lastDownloaded = lastDownloaded;

        this.remoteDownloadCount = 0;
        this.remoteLastDownloaded = 0;
        this.remoteLastDownloadedBy = null;

        this.origin = null;
        this.path = null;
    }

    public Stat(long nodeId, long downloadCount, long lastDownloaded, String lastDownloadedBy, long remoteDownloadCount,
            long remoteLastDownloaded, String remoteLastDownloadedBy) {
        this.nodeId = nodeId;
        this.downloadCount = downloadCount;
        this.lastDownloaded = lastDownloaded;
        this.lastDownloadedBy = lastDownloadedBy;
        this.remoteDownloadCount = remoteDownloadCount;
        this.remoteLastDownloaded = remoteLastDownloaded;
        this.remoteLastDownloadedBy = remoteLastDownloadedBy;

        this.origin = null;
        this.path = null;
    }

    public Stat(long nodeId, long downloadCount, long lastDownloaded, String lastDownloadedBy, long remoteDownloadCount,
            long remoteLastDownloaded, String remoteLastDownloadedBy, String origin, String path) {
        this.nodeId = nodeId;
        this.downloadCount = downloadCount;
        this.lastDownloaded = lastDownloaded;
        this.lastDownloadedBy = lastDownloadedBy;
        this.remoteDownloadCount = remoteDownloadCount;
        this.remoteLastDownloaded = remoteLastDownloaded;
        this.remoteLastDownloadedBy = remoteLastDownloadedBy;

        this.origin = origin;
        this.path = path;
    }

    public long getNodeId() {
        return nodeId;
    }

    public long getLocalDownloadCount() {
        return downloadCount;
    }

    public String getLocalLastDownloadedBy() {
        return lastDownloadedBy;
    }

    public long getLocalLastDownloaded() {
        return lastDownloaded;
    }

    public boolean isLocal() {
        return !Strings.isNullOrEmpty(lastDownloadedBy);
    }

    public boolean isRemote() {
        return !Strings.isNullOrEmpty(origin);
    }

    public long getRemoteDownloadCount() {
        return remoteDownloadCount;
    }

    public long getRemoteLastDownloaded() {
        return remoteLastDownloaded;
    }

    public String getRemoteLastDownloadedBy() {
        return remoteLastDownloadedBy;
    }

    public String getOrigin() {
        return origin;
    }

    public void setRemoteDownloadCount(long remoteDownloadCount) {
        this.remoteDownloadCount = remoteDownloadCount;
    }

    public void setRemoteLastDownloaded(long remoteLastDownloaded) {
        this.remoteLastDownloaded = remoteLastDownloaded;
    }

    public void setRemoteLastDownloadedBy(String remoteLastDownloadedBy) {
        this.remoteLastDownloadedBy = remoteLastDownloadedBy;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        StringBuilder sb =  new StringBuilder();
        sb.append("{");

        sb.append("downloadCount: ");
        sb.append(downloadCount);

        sb.append(", lastDownloaded: ");
        sb.append(lastDownloaded);

        sb.append(", lastDownloadedBy: ");
        sb.append(lastDownloadedBy);

        sb.append(", remoteDownloadCount: ");
        sb.append(remoteDownloadCount);

        sb.append(", remoteLastDownloaded: ");
        sb.append(remoteLastDownloaded);

        sb.append(", remoteLastDownloadedBy: ");
        sb.append(remoteLastDownloadedBy);

        sb.append(", path: ");
        sb.append(path);

        sb.append("}");

        return sb.toString();
    }
}
