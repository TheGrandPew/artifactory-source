package org.artifactory.maven;

import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.api.maven.MavenMetadataService;
import org.artifactory.api.maven.MavenMetadataWorkItem;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.model.common.RepoPathImpl;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.db.DbLocalRepo;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.spring.InternalArtifactoryContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Test
public class MavenMetadataServiceImplTest {

    @Mock
    private InternalArtifactoryContext artifactoryContext;
    @Mock
    private InternalRepositoryService repositoryService;
    @Mock
    private MavenMetadataService mavenMetadataService;

    private MavenMetadataServiceImpl service;

    @BeforeTest
    void setup() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        service = spy(new MavenMetadataServiceImpl());
        service.setRepoService(repositoryService);

        doReturn(mavenMetadataService).when(artifactoryContext).beanForType(MavenMetadataService.class);
    }

    @AfterTest
    void tearDown() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void testCalculateMavenPluginMetadata() {
        // given
        RepoPath repoPath = new RepoPathImpl("local_repo",
                "org/codehaus/mojo/maven-buildnumber-plugin/0.9.4/maven-buildnumber-plugin-0.9.4.pom", false);
        MavenMetadataWorkItem workItem = new MavenMetadataWorkItem(repoPath, false, true);
        LocalRepoDescriptor localRepoDescriptor = new LocalRepoDescriptor();// mock(LocalRepoDescriptor.class);
        LocalRepo localRepo = new DbLocalRepo(localRepoDescriptor, repositoryService, null);
        localRepoDescriptor.setType(RepoType.Maven);

        when(repositoryService.localRepositoryByKey(repoPath.getRepoKey())).thenReturn(localRepo);

        MavenMetadataCalculator mavenMetadataCalculator = mock(MavenMetadataCalculator.class);
        when(service.getMavenMetadataCalculator(workItem)).thenReturn(mavenMetadataCalculator);

        // when
        service.calculateMavenMetadata(workItem);

        // then
        verify(repositoryService).localRepositoryByKey(repoPath.getRepoKey());
        verify(service).getMavenMetadataCalculator(workItem);
        verify(mavenMetadataCalculator).calculate();
        verify(mavenMetadataService).calculateMavenPluginsMetadataAsync(any());

        // given
        clearInvocations(repositoryService, service, mavenMetadataCalculator, mavenMetadataService);
        workItem = new MavenMetadataWorkItem(repoPath, false, false);
        when(service.getMavenMetadataCalculator(workItem)).thenReturn(mavenMetadataCalculator);

        // when
        service.calculateMavenMetadata(workItem);

        // then
        verify(repositoryService).localRepositoryByKey(repoPath.getRepoKey());
        verify(service).getMavenMetadataCalculator(workItem);
        verify(mavenMetadataCalculator).calculate();
        verifyNoMoreInteractions(mavenMetadataService);
    }
}
