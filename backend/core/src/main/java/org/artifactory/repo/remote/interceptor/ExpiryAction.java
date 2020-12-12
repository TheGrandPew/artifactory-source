package org.artifactory.repo.remote.interceptor;


/**
 * Expiry Actions used to determine cached resource download logic. Used by:
 * {@link RemoteRepoInterceptor#shouldExpireCachedResource}
 * and
 * {@link org.artifactory.repo.RemoteRepoBase#downloadAndSave}
 *
 * @author andreik
 */
public enum ExpiryAction {
    EXPIRE,
    DONT_EXPIRE_STOP,
    DONT_EXPIRE_CONTINUE_NEXT;
}
