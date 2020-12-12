package org.artifactory.version.converter.v228;

import org.apache.commons.lang.StringUtils;
import org.artifactory.version.converter.XmlConverter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.artifactory.descriptor.repo.RepoType.Docker;

/**
 * This converter will update remote repos set to proxy Dockerhub with a unique value defined under {@link
 * org.artifactory.repo.config.RepoConfigDefaultValues#DEFAULT_DOCKER_REMOTE_RETRIEVAL_CACHE_PERIOD}. The conditions
 * for the conversions are:
 *
 * 1. It is pointing to Dockerhub
 * 2. The default global value is defined (lower/higher value will not be changed, a warning message will be printed):
 * {@link org.artifactory.repo.config.RepoConfigDefaultValues#DEFAULT_RETRIEVAL_CACHE_PERIOD}
 * 3. There are no authentication details set for the remote (e.g. a payed Docker account)
 *
 * @author AndreiK.
 */
public class DockerRemoteRepositoryCacheRetrievalConverter implements XmlConverter {

    private static final Logger log = LoggerFactory.getLogger(DockerRemoteRepositoryCacheRetrievalConverter.class);

    @Override
    public void convert(Document doc) {
        log.info("Starting Dockerhub remote repos' default cache retrieval period conversion");
        Element rootElement = doc.getRootElement();
        Namespace namespace = rootElement.getNamespace();
        Element remoteRepositories = rootElement.getChild("remoteRepositories", namespace);
        if (remoteRepositories != null) {
            for (Element remoteRepo : remoteRepositories.getChildren()) {
                Element pkgTypeElement = remoteRepo.getChild("type", namespace);
                if (pkgTypeElement != null) {
                    String pkgType = pkgTypeElement.getText();
                    if (StringUtils.equals(pkgType, Docker.getType()) && isDockerHubRepo(namespace, remoteRepo)) {
                        updateIfNeeded(namespace, remoteRepo);
                    }
                }
            }
        }
        log.info("Finished Dockerhub remote repos' default cache retrieval period conversion");
    }

    private void updateIfNeeded(Namespace namespace, Element remoteRepo) {
        Element periodSecs = remoteRepo.getChild("retrievalCachePeriodSecs", namespace);
        if (periodSecs != null) {
            String periodSecsText = periodSecs.getText();
            boolean isNotPeriodSecsBlank = StringUtils.isNotBlank(periodSecsText);
            String repoKey = remoteRepo.getChild("key", namespace).getText();
            if (isNotPeriodSecsBlank && StringUtils.equals(periodSecsText, "7200")) {
                periodSecs.setText("21600");
                log.debug("Changing previous default value for Docker repo: {}", repoKey);
            } else if (isNotPeriodSecsBlank && Long.parseLong(periodSecsText) < 21600L) {
                log.warn("'{}' Dockerhub repository's retrieval cache period was manually configured with lower value" +
                        " than the recommended '21600' value and has not been updated. Skipping conversion.", repoKey);
            }
        }
    }

    private boolean isDockerHubRepo(Namespace namespace, Element remoteRepo) {
        Element urlElement = remoteRepo.getChild("url", namespace);
        if (urlElement == null) {
            return false;
        }
        String url = urlElement.getText();
        return StringUtils.isNotBlank(url) && StringUtils.containsIgnoreCase(url, "registry-1.docker.io");
    }
}
