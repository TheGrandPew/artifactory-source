package org.artifactory.ui.rest.service.admin.configuration.repositories.util.validator;

import org.artifactory.addon.AddonsManager;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.test.ArtifactoryHomeStub;
import org.artifactory.ui.rest.model.admin.configuration.repository.GeneralRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteAdvancedRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteBasicRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteCacheRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.remote.RemoteRepositoryConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.DockerTypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.GenericTypeSpecificConfigModel;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.TypeSpecificConfigModel;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.validation.constraints.NotNull;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_DOCKER_REMOTE_RETRIEVAL_CACHE_PERIOD;
import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_RETRIEVAL_CACHE_PERIOD;
import static org.testng.Assert.assertEquals;

/**
 * @author AndreiK.
 */
@Test
public class RepoConfigValidatorTest {

    @Mock
    CentralConfigService centralConfigServiceMock;
    @Mock
    AddonsManager addonsManagerMock;
    RepoConfigValidator validator;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ArtifactoryHome.bind(new ArtifactoryHomeStub());
        validator = new RepoConfigValidator(centralConfigServiceMock, addonsManagerMock);
    }

    @Test(dataProvider = "provideCachePeriodAndRepoType", description = "Actually sets the value when changed in the UI" +
            "note the cast to DockerTypeSpecificConfigModel - this replicates the UI originated model")
    public void testRemoteRepoValidationOfRetrievalCachePeriodSecsOfCacheModel(Long cachePeriod, boolean isDockerModel,
            Long expected) {
        TypeSpecificConfigModel typeSpecificConfigModel;
        if (isDockerModel) {
            typeSpecificConfigModel = new DockerTypeSpecificConfigModel();
            ((DockerTypeSpecificConfigModel) typeSpecificConfigModel).setRetrievalCachePeriodSecs(cachePeriod);
        } else {
            typeSpecificConfigModel = new GenericTypeSpecificConfigModel();
        }
        RemoteAdvancedRepositoryConfigModel remoteAdvancedRepositoryConfigModel =
                getAdvancedRepoConfigModelAndSetRetrievalCachePeriod(cachePeriod);
        RemoteRepositoryConfigModel model = getRemoteRepositoryConfigModel(remoteAdvancedRepositoryConfigModel,
                typeSpecificConfigModel);
        RemoteCacheRepositoryConfigModel cache = model.getAdvanced().getCache();

        validator.setRetrievalCachePeriodSecsOfCacheModel(model, cache);

        assertEquals((long) cache.getRetrievalCachePeriodSecs(), (long) expected);
    }

    @DataProvider
    private Object[][] provideCachePeriodAndRepoType() {
        return new Object[][]{
                {null, true, DEFAULT_DOCKER_REMOTE_RETRIEVAL_CACHE_PERIOD},
                {null, false, DEFAULT_RETRIEVAL_CACHE_PERIOD},
                {36000L, true, 36000L},
                {36000L, false, 36000L},
        };
    }

    @NotNull
    private RemoteAdvancedRepositoryConfigModel getAdvancedRepoConfigModelAndSetRetrievalCachePeriod(Long cachePeriod) {
        RemoteAdvancedRepositoryConfigModel remoteAdvancedRepositoryConfigModel =
                new RemoteAdvancedRepositoryConfigModel();
        RemoteCacheRepositoryConfigModel remoteCacheRepositoryConfigModel = new RemoteCacheRepositoryConfigModel();
        remoteAdvancedRepositoryConfigModel.setCache(remoteCacheRepositoryConfigModel);
        remoteCacheRepositoryConfigModel.setRetrievalCachePeriodSecs(cachePeriod);
        return remoteAdvancedRepositoryConfigModel;
    }

    @NotNull
    private RemoteRepositoryConfigModel getRemoteRepositoryConfigModel(
            RemoteAdvancedRepositoryConfigModel remoteAdvancedRepositoryConfigModel,
            TypeSpecificConfigModel typeSpecificConfigModel) {
        RemoteRepositoryConfigModel remoteRepositoryConfigModel = new RemoteRepositoryConfigModel();
        remoteRepositoryConfigModel.setBasic(new RemoteBasicRepositoryConfigModel());
        remoteRepositoryConfigModel.setGeneral(new GeneralRepositoryConfigModel());
        remoteRepositoryConfigModel.setAdvanced(remoteAdvancedRepositoryConfigModel);
        remoteRepositoryConfigModel.setTypeSpecific(typeSpecificConfigModel);
        return remoteRepositoryConfigModel;
    }
}