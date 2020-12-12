package org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ArtifactoryContextThreadBinder;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.ui.rest.model.admin.configuration.repository.BasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.DockerTypeSpecificConfigModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_DOCKER_REMOTE_RETRIEVAL_CACHE_PERIOD;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Author: andreik
 */
@Test
public class RepoConfigDescriptorBuilderTest extends ArtifactoryHomeBoundTest {
    @Mock
    private ArtifactoryContext artifactoryContext;
    @Mock
    private CentralConfigService centralConfigService;
    @Mock
    private MutableCentralConfigDescriptor mutableCentralConfigDescriptor;
    @InjectMocks
    private RepoConfigDescriptorBuilder repoConfigDescriptorBuilder;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryContextThreadBinder.bind(artifactoryContext);
        when(artifactoryContext.getCentralConfig()).thenReturn(centralConfigService);
        when(centralConfigService.getMutableDescriptor()).thenReturn(mutableCentralConfigDescriptor);
    }

    @AfterMethod
    private void cleanup() {
        ArtifactoryContextThreadBinder.unbind();
    }

    @Test
    public void testBuildRemoteDescriptorWithCustomDockerRetrievalCachePeriod() {
        DockerTypeSpecificConfigModel dockerTypeSpecificConfigModel = new DockerTypeSpecificConfigModel();
        final RemoteRepositoryConfigModel model = prepareRemoteModel(dockerTypeSpecificConfigModel);

        final RemoteRepoDescriptor remoteDescriptor = repoConfigDescriptorBuilder.buildRemoteDescriptor(model);

        assertEquals(remoteDescriptor.getRetrievalCachePeriodSecs(), DEFAULT_DOCKER_REMOTE_RETRIEVAL_CACHE_PERIOD);
    }

    private void fillBasic(BasicRepositoryConfigModel basic) {
        basic.setPublicDescription("");
        basic.setInternalDescription("");
        basic.setIncludesPattern("");
        basic.setExcludesPattern("");
    }

    private RemoteRepositoryConfigModel prepareRemoteModel
            (DockerTypeSpecificConfigModel dockerTypeSpecificConfigModel) {

        final RemoteRepositoryConfigModel model = new RemoteRepositoryConfigModel();
        final RemoteAdvancedRepositoryConfigModel advanced = new RemoteAdvancedRepositoryConfigModel();
        final GeneralRepositoryConfigModel general = new GeneralRepositoryConfigModel();
        final RemoteBasicRepositoryConfigModel basic = new RemoteBasicRepositoryConfigModel();
        fillBasic(basic);
        general.setRepoKey("repo");
        model.setTypeSpecific(dockerTypeSpecificConfigModel);
        model.setAdvanced(advanced);
        model.setGeneral(general);
        model.setBasic(basic);
        return model;
    }
}