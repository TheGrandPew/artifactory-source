package org.artifactory.repo.remote.interceptor;

import org.artifactory.fs.RepoResource;
import org.artifactory.request.RepoRequests;

import static org.artifactory.repo.remote.interceptor.ExpiryAction.DONT_EXPIRE_STOP;
import static org.artifactory.repo.remote.interceptor.ExpiryAction.EXPIRE;

public class LastModifiedExpiryRemoteInterceptor implements RemoteRepoInterceptor {

    /**
     * Default implementation for a time based check comparing the remote and cached resources Last Modified timestamp.
     * Note this class is not a Spring bean, as we would like not to traverse over it unless called upon.
     *
     * @param cachedResource The resource retrieved from the remote's cache repository
     * @param remoteResource The resource potentially resolved from the HEAD/GET request
     */
    @Override
    public ExpiryAction shouldExpireCachedResource(RepoResource cachedResource,
            RepoResource remoteResource) {
        boolean lastModifiedIsNewerOnRemote =
                cachedResource.isExpired() && remoteResource.getLastModified() > cachedResource.getLastModified();
        RepoRequests.logToContext(
                "Found expired cached resource but remote is newer = %s. Cached resource: %s, Remote resource: %s",
                lastModifiedIsNewerOnRemote, cachedResource.getLastModified(), remoteResource.getLastModified());
        if (lastModifiedIsNewerOnRemote) {
            return EXPIRE;
        }
        return DONT_EXPIRE_STOP;
    }
}
