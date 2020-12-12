package org.artifactory.ui.rest.service.admin.configuration.repositories.util.builder;

import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.test.ArtifactoryHomeBoundTest;
import org.artifactory.ui.rest.model.admin.configuration.repository.typespecific.DockerTypeSpecificConfigModel;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author AndreiK.
 */
public class RepoConfigModelBuilderTest extends ArtifactoryHomeBoundTest {

    @Test(description = "Tests repository stored RetrievalCachePeriod, UI gets this for the Advanced -> Cache section" +
            "and displays this on RetrievalCachePeriod field")
    public void testPopulateRemoteRepositoryConfigValuesToModel() {
        RepoConfigModelBuilder repoConfigModelBuilder = new RepoConfigModelBuilder();
        HttpRepoDescriptor httpRepoDescriptor = new HttpRepoDescriptor();
        httpRepoDescriptor.setRetrievalCachePeriodSecs(500L);

        DockerTypeSpecificConfigModel remoteTypeSpecific = (DockerTypeSpecificConfigModel) repoConfigModelBuilder
                .createRemoteTypeSpecific(RepoType.Docker, httpRepoDescriptor);

        assertEquals((long) remoteTypeSpecific.getRetrievalCachePeriodSecs(), 500L);
    }
}