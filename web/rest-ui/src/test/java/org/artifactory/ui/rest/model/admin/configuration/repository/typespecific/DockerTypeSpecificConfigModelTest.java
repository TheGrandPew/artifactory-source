package org.artifactory.ui.rest.model.admin.configuration.repository.typespecific;

import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.testng.annotations.Test;

import static org.artifactory.repo.config.RepoConfigDefaultValues.DEFAULT_DOCKER_REMOTE_RETRIEVAL_CACHE_PERIOD;
import static org.testng.Assert.assertEquals;

/**
 * @author AndreiK.
 */
@Test
public class DockerTypeSpecificConfigModelTest extends ArtifactoryHomeBoundTest {

    @Test(description = "Tests the RetrievalCachePeriod default value for Docker remote repos. The setter and getter " +
            "are used for serializing the RetrievalCachePeriod to the 'defaultvalues' request under Docker: model")
    public void testValidateRemoteTypeSpecificRetrievalCachePeriod() {
        DockerTypeSpecificConfigModel dockerTypeSpecificConfigModel = new DockerTypeSpecificConfigModel();
        dockerTypeSpecificConfigModel.setRetrievalCachePeriodSecs(null);

        dockerTypeSpecificConfigModel.validateRemoteTypeSpecific();

        assertEquals((long) dockerTypeSpecificConfigModel.getRetrievalCachePeriodSecs(),
                DEFAULT_DOCKER_REMOTE_RETRIEVAL_CACHE_PERIOD);
    }
}