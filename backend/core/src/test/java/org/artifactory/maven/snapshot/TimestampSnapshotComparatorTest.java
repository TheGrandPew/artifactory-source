package org.artifactory.maven.snapshot;

import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author uriahl
 */
@Test
public class TimestampSnapshotComparatorTest {

    private final TimestampSnapshotComparator timestampSnapshotComparator = new TimestampSnapshotComparator();

    public void testTimestampComparatorVersions() {
        SnapshotVersion newerSnapshotVersion = new SnapshotVersion();
        newerSnapshotVersion.setUpdated("20170510085558");
        newerSnapshotVersion.setVersion("1.9.2.3-XWT-20170510.085558-2");
        newerSnapshotVersion.setExtension("pom");
        SnapshotVersion olderSnapshotVersion = new SnapshotVersion();
        olderSnapshotVersion.setUpdated("20170510084024");
        olderSnapshotVersion.setVersion("1.9.2.3-XWT-20170510.084024-1");
        olderSnapshotVersion.setExtension("pom");
        assertEquals(timestampSnapshotComparator.compare(newerSnapshotVersion, olderSnapshotVersion), 1,
                "20170510085558 should be > 20170510084024");
    }

    public void testTimestampSnapshotComparatorWithDottedUpdated() {
        SnapshotVersion newerSnapshotVersion = new SnapshotVersion();
        // Doesn't happen in real life but test to make sure we don't fail with formatting errors
        newerSnapshotVersion.setUpdated("20170510.085558");
        newerSnapshotVersion.setVersion("1.9.2.3-XWT-20170510.085558-2");
        newerSnapshotVersion.setExtension("pom");
        SnapshotVersion olderSnapshotVersion = new SnapshotVersion();
        olderSnapshotVersion.setUpdated("20170510.084024");
        olderSnapshotVersion.setVersion("1.9.2.3-XWT-20170510.084024-1");
        olderSnapshotVersion.setExtension("pom");
        assertEquals(timestampSnapshotComparator.compare(newerSnapshotVersion, olderSnapshotVersion), 1,
                "20170510085558 should be > 20170510084024");
    }

    public void testFallbackToVersionWhenUpdatedIsMissing() {
        SnapshotVersion newerSnapshotVersion = new SnapshotVersion();
        newerSnapshotVersion.setUpdated("");
        newerSnapshotVersion.setVersion("1.9.2.3-20170510.085558-2");
        newerSnapshotVersion.setExtension("pom");
        SnapshotVersion olderSnapshotVersion = new SnapshotVersion();
        olderSnapshotVersion.setUpdated(null);
        olderSnapshotVersion.setVersion("1.9.2.3-20170510.084024-1");
        olderSnapshotVersion.setExtension("pom");
        assertEquals(timestampSnapshotComparator.compare(newerSnapshotVersion, olderSnapshotVersion), 1,
                "20170510085558 should be > 20170510084024");
    }
}