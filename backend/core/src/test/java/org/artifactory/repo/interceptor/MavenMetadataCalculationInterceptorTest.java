package org.artifactory.repo.interceptor;

import lombok.NonNull;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.maven.MavenMetadataService;
import org.artifactory.api.maven.MavenMetadataWorkItem;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.fs.FileInfo;
import org.artifactory.md.Properties;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.model.xstream.fs.FileInfoImpl;
import org.artifactory.model.xstream.fs.PropertiesImpl;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.db.DbLocalRepo;
import org.artifactory.repo.db.DbStoringRepoMixin;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.storage.db.fs.model.DbMutableFile;
import org.artifactory.storage.fs.MutableVfsFile;
import org.artifactory.storage.fs.repo.StoringRepo;
import org.artifactory.storage.fs.service.PropertiesService;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * @author Yevdo Abramov
 * Created on 01/10/2020
 */
public class MavenMetadataCalculationInterceptorTest extends ArtifactoryHomeBoundTest {

    @Mock
    private MavenMetadataService mavenMetadataService;
    @Mock
    private InternalRepositoryService repoService;
    @Mock
    private ArtifactoryContext artifactoryContext;
    @Mock
    private PropertiesService propertiesService;

    private MavenMetadataCalculationInterceptor interceptor;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        interceptor = new MavenMetadataCalculationInterceptor();
        interceptor.setMavenMetadataService(mavenMetadataService);
        interceptor.setRepoService(repoService);
        doReturn(propertiesService).when(artifactoryContext).beanForType(PropertiesService.class);
    }

    @AfterMethod
    public void tearDown() {
        ArtifactoryContextThreadBinder.unbind();
        verifyNoMoreInteractions(mavenMetadataService, repoService, propertiesService);
    }

    @Test(testName = "test afterCreate with mavenPlugin indicator true or false")
    public void testAfterCreateForMavenPluginIndicator() {
        // given
        MutableStatusHolder statusHolder = mock(MutableStatusHolder.class);
        InterceptorCreateContext context = mock(InterceptorCreateContext.class);
        VfsItem fsFile =
                mockFsFile("org/codehaus/mojo/maven-buildnumber-plugin/0.9.4/maven-buildnumber-plugin-0.9.4.pom");

        Properties properties = new PropertiesImpl();
        when(propertiesService.getProperties(fsFile.getInfo())).thenReturn(properties);

        // when
        interceptor.afterCreate(fsFile, statusHolder, context);

        // then
        verify(repoService).localRepositoryByKey(fsFile.getRepoKey());
        verify(propertiesService).getProperties(fsFile.getInfo());
        verify(mavenMetadataService).calculateMavenMetadataAsync(argThat(workItem -> !workItem.isMavenPlugin()));

        // given
        properties.put(org.artifactory.api.properties.PropertiesService.MAVEN_PLUGIN_PROPERTY_NAME, "true");
        Mockito.clearInvocations(repoService, mavenMetadataService, propertiesService);

        // when
        interceptor.afterCreate(fsFile, statusHolder, context);

        // then
        verify(repoService).localRepositoryByKey(fsFile.getRepoKey());
        verify(propertiesService).getProperties(fsFile.getInfo());
        verify(mavenMetadataService).calculateMavenMetadataAsync(argThat(MavenMetadataWorkItem::isMavenPlugin));
    }

    @Test(testName = "test afterDelete with mavenPlugin indicator true or false")
    public void testAfterDeleteForMavenPluginIndicator() {
        // given
        MutableStatusHolder statusHolder = mock(MutableStatusHolder.class);
        VfsItem fsFile =
                mockFsFile("org/codehaus/mojo/maven-buildnumber-plugin/0.9.4/maven-buildnumber-plugin-0.9.4.pom");

        DeleteContext context = new DeleteContext(fsFile.getRepoPath()).calculateMavenMetadata();
        Properties properties = new PropertiesImpl();
        when(propertiesService.getProperties(fsFile.getInfo())).thenReturn(properties);

        // when
        interceptor.afterDelete(fsFile, statusHolder, context);

        // then
        verify(repoService).localRepositoryByKey(fsFile.getRepoKey());
        verify(propertiesService, times(2)).getProperties(fsFile.getInfo());
        verify(mavenMetadataService, times(2))
                .calculateMavenMetadataAsync(argThat(workItem -> !workItem.isMavenPlugin()));

        // given
        properties.put(org.artifactory.api.properties.PropertiesService.MAVEN_PLUGIN_PROPERTY_NAME, "true");
        Mockito.clearInvocations(repoService, mavenMetadataService, propertiesService);

        // when
        interceptor.afterDelete(fsFile, statusHolder, context);

        // then
        verify(repoService).localRepositoryByKey(fsFile.getRepoKey());
        verify(propertiesService, times(2)).getProperties(fsFile.getInfo());
        verify(mavenMetadataService, times(2))
                .calculateMavenMetadataAsync(argThat(MavenMetadataWorkItem::isMavenPlugin));
    }

    @NonNull
    private MutableVfsFile mockFsFile(@NonNull String path) {
        LocalRepoDescriptor localRepoDescriptor = new LocalRepoDescriptor();// mock(LocalRepoDescriptor.class);
        LocalRepo localRepo = new DbLocalRepo(localRepoDescriptor, repoService, null);
        localRepoDescriptor.setType(RepoType.Maven);
        StoringRepo storingRepo = new DbStoringRepoMixin(localRepoDescriptor, null);
        FileInfo fileInfo = new FileInfoImpl(new RepoPathImpl("local_repo", path, false));
        MutableVfsFile file = new DbMutableFile(storingRepo, 1, fileInfo);
        when(repoService.localRepositoryByKey("local_repo")).thenReturn(localRepo);
        return file;
    }
}
