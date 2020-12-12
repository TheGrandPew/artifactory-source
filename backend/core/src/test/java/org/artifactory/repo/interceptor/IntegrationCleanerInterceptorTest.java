package org.artifactory.repo.interceptor;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.common.MoveMultiStatusHolder;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.LocalCacheRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoRepoPath;
import org.artifactory.repo.cleanup.InternalIntegrationCleanupService;
import org.artifactory.repo.interceptor.storage.StorageInterceptorsImpl;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.service.mover.CopyRepoPathService;
import org.artifactory.repo.service.mover.MoveRepoPathService;
import org.artifactory.repo.service.mover.MoverConfig;
import org.artifactory.sapi.fs.VfsFile;
import org.artifactory.sapi.interceptor.StorageInterceptor;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.artifactory.storage.fs.MutableVfsFile;
import org.artifactory.storage.fs.session.StorageSession;
import org.artifactory.storage.fs.session.StorageSessionHolder;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test covers that repo added to cleanup process after moving/copying any file into it
 */
public class IntegrationCleanerInterceptorTest extends ArtifactoryHomeBoundTest {

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private AddonsManager addonsManager;

    @Mock
    private ArtifactoryApplicationContext artifactoryContext;

    @Mock
    private InternalIntegrationCleanupService cleanupService;

    @Mock
    private LocalCacheRepo dbLocalRepo1;

    @Mock
    private LocalCacheRepo dbLocalRepo2;

    @Mock
    private VfsFile vfsFile;

    @Mock
    private MutableVfsFile mutableVfsFile;

    @Mock
    private RepoPath repoPath;

    @Mock
    private InternalRepositoryService repositoryService;

    @Mock
    private Properties properties;

    @Mock
    private StorageSession storageSession;

    @Mock
    private LocalRepoDescriptor repoDescriptor;

    @Mock
    private ModuleInfo moduleInfo;

    @Spy
    @InjectMocks
    private CopyRepoPathService copyRepoPathService;

    @Spy
    @InjectMocks
    private MoveRepoPathService moveRepoPathService;

    @Spy
    @InjectMocks
    private IntegrationCleanerInterceptor integrationCleanerInterceptor;

    @Spy
    @InjectMocks
    private StorageInterceptorsImpl storageInterceptors;

    private static final RepoPath FROM_REPO_PATH = new RepoPathImpl("key1", "path.file.2", false);
    private static final RepoPath TO_REPO_PATH = new RepoPathImpl("key2", "path.file.1", false);

    private Map<String, StorageInterceptor> interceptors;
    private RepoRepoPath repoRepoPath;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        StorageSessionHolder.setSession(storageSession);

        repoRepoPath = new RepoRepoPath(dbLocalRepo2, repoPath);
        interceptors = new HashMap<>();
        interceptors.put(IntegrationCleanerInterceptor.class.getName(), integrationCleanerInterceptor);

        when(authorizationService.canDeploy(repoPath)).thenReturn(true);
        when(repoDescriptor.getMaxUniqueSnapshots()).thenReturn(1);
        when(moduleInfo.isValid()).thenReturn(true);
        when(moduleInfo.isIntegration()).thenReturn(true);
        when(mutableVfsFile.getRepoPath()).thenReturn(repoPath);
        when(mutableVfsFile.getRepoKey()).thenReturn(TO_REPO_PATH.getRepoKey());
        when(repositoryService.localRepoDescriptorByKey(TO_REPO_PATH.getRepoKey())).thenReturn(repoDescriptor);
        when(repositoryService.getItemModuleInfo(repoPath)).thenReturn(moduleInfo);
        when(dbLocalRepo2.accepts(repoPath)).thenReturn(true);
        when(dbLocalRepo2.createOrGetFile(repoPath)).thenReturn(mutableVfsFile);

        when(artifactoryContext.beanForType(AddonsManager.class)).thenReturn(addonsManager);
        when(artifactoryContext.beansForType(StorageInterceptor.class)).thenReturn(interceptors);
        when(artifactoryContext.beanForType(StorageInterceptors.class)).thenReturn(storageInterceptors);
        when(artifactoryContext.beanForType(InternalRepositoryService.class)).thenReturn(repositoryService);
        when(artifactoryContext.getAuthorizationService()).thenReturn(authorizationService);

        storageInterceptors.setApplicationContext(artifactoryContext);
        storageInterceptors.init();
    }

    @AfterMethod
    public void afterMethod() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void testCopyFileAndCleanUpSnapshots() {
        when(vfsFile.getProperties()).thenReturn(properties);

        copyRepoPathService
                .handleMoveOrCopy(new MoveMultiStatusHolder(), createMoveConfig(true), vfsFile, repoRepoPath);

        verify(cleanupService, atLeastOnce()).addItemToCache(repoPath);
    }

    @Test
    public void testMoveFileAndCleanUpSnapshots() {
        when(authorizationService.canDelete(repoPath)).thenReturn(true);
        when(repositoryService.localOrCachedRepositoryByKey(FROM_REPO_PATH.getRepoKey())).thenReturn(dbLocalRepo1);
        when(vfsFile.getRepoKey()).thenReturn(FROM_REPO_PATH.getRepoKey());
        when(vfsFile.getRepoPath()).thenReturn(repoPath);
        when(mutableVfsFile.getProperties()).thenReturn(properties);
        when(dbLocalRepo1.getMutableFsItem(repoPath)).thenReturn(mutableVfsFile);

        moveRepoPathService
                .handleMoveOrCopy(new MoveMultiStatusHolder(), createMoveConfig(false), vfsFile, repoRepoPath);

        verify(cleanupService, atLeastOnce()).addItemToCache(repoPath);
    }

    private MoverConfig createMoveConfig(boolean copy) {
        return new MoverConfig(FROM_REPO_PATH, TO_REPO_PATH, copy, false, false, false, properties,
                false, true, false, false, true, false);
    }
}