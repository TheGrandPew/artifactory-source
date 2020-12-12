package org.artifactory.version.converter.v228;

import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * @author AndreiK.
 */
@Test
public class DockerRemoteRepositoryCacheRetrievalConverterTest extends XmlConverterTest {

    @Test
    public void convert() throws Exception {
        Document document = convertXml("/config/test/config.2.2.8_docker_retrieval_period.xml",
                new DockerRemoteRepositoryCacheRetrievalConverter());
        Element rootElement = document.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        List<Element> remoteRepositories = rootElement.getChild("remoteRepositories", namespace).getChildren();
        for (Element remoteRepo : remoteRepositories) {
            String repoKey = remoteRepo.getChild("key", namespace).getText();
            String periodSecs = remoteRepo.getChild("retrievalCachePeriodSecs", namespace).getText();
            if (repoKey.equals("bintray-docker-remote")) {
                assertEquals(periodSecs, "7200");
            }
            if (repoKey.equals("docker-remote-https")) {
                assertEquals(periodSecs, "21600");
            }
            if (repoKey.equals("docker-remote-http")) {
                assertEquals(periodSecs, "21600");
            }
            if (repoKey.equals("high")) {
                assertEquals(periodSecs, "99999");
            }
            if (repoKey.equals("low")) {
                assertEquals(periodSecs, "500");
            }
            if (repoKey.equals("debian-remote")) {
                assertEquals(periodSecs, "7200");
            }
            if (repoKey.equals("maven-central")) {
                assertEquals(periodSecs, "7200");
            }
        }
    }
}
