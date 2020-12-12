package org.artifactory.repo.remote.interceptor;

import org.apache.http.Header;
import org.artifactory.fs.RepoResource;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.resource.ExpiredRepoResource;
import org.artifactory.resource.FileResource;
import org.artifactory.resource.RemoteRepoResource;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class LastModifiedExpiryRemoteInterceptorTest extends ArtifactoryHomeBoundTest {

    private final String REPO = "docker-remote-cache";
    private final String PATH = "manifest.json";
    private final RepoPath repoPath = RepoPathFactory.create(REPO, PATH);

    @Test(dataProvider = "lastModifiedAndExpiryProvider")
    public void testShouldExpireCachedResource(ExpiryAction expectedExpiryAction, long cachedLastModified,
            long remoteLastModified) {
        RepoResource remoteResource = new RemoteRepoResource(repoPath, remoteLastModified, "etag-1", 1337,
                new HashSet<>(), new Header[] {});
        FileInfoImpl fileInfo = new FileInfoImpl(repoPath);
        fileInfo.setLastModified(cachedLastModified);
        RepoResource cachedResource = new ExpiredRepoResource(new FileResource(fileInfo));
        ExpiryAction expiryAction = new LastModifiedExpiryRemoteInterceptor()
                .shouldExpireCachedResource(cachedResource, remoteResource);
        assertEquals(expectedExpiryAction, expiryAction);
    }

    @DataProvider
    public Object[][] lastModifiedAndExpiryProvider() {
        return new Object[][] {
                // expectedExpiryAction, cachedLastModified, remoteLastModified
                {ExpiryAction.EXPIRE, 1598190863595L, 1598190863596L},
                {ExpiryAction.DONT_EXPIRE_STOP, 1598190863595L, 1598190863595L},
                {ExpiryAction.DONT_EXPIRE_STOP, 1598190863596L, 1598190863595L}
        };
    }

    @Test
    public void testShouldNotExpireUnexpiredResource() {
        RepoResource remoteResource = new RemoteRepoResource(repoPath, 1598190863595L, "etag-1", 1337,
                new HashSet<>(), new Header[] {});
        RepoResource cachedResource = new FileResource(new FileInfoImpl(repoPath));
        ExpiryAction expiryAction = new LastModifiedExpiryRemoteInterceptor()
                .shouldExpireCachedResource(cachedResource, remoteResource);
        assertEquals(ExpiryAction.DONT_EXPIRE_STOP, expiryAction);
    }
}