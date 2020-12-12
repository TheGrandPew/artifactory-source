package org.artifactory.rest.common.service;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ha.HaCommonAddon;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.jackson.JacksonFactory;
import org.artifactory.descriptor.delegation.ContentSynchronisation;
import org.artifactory.descriptor.repo.HttpRepoDescriptor;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.features.matrix.SmartRepoVersionFeatures;
import org.artifactory.repo.HttpRepositoryConfigurationImpl;
import org.codehaus.jackson.JsonGenerator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Dan Feldman
 * @author Avishay Halpren
 */
public class ResearchServiceImplTest {

    @Mock
    SmartRepoVersionFeatures versionFeatures;
    @Mock
    CentralConfigService configService;
    @Mock
    AddonsManager addonsManager;

    private ResearchServiceImpl researchService;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        researchService = new ResearchServiceImpl();
        researchService.setSmartRepoVersionFeatures(versionFeatures);
        researchService.setConfigService(configService);
        researchService.setAddonsManager(addonsManager);
    }

    @Test
    public void syncPropsDisabledWhenNoContentSyncAndNoPropSync() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(false);
        descriptor.setContentSynchronisation(null);
        assertFalse(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void syncPropsDisabledWhenContentSyncPresentAndDisabledAndNoPropSync() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(false);
        ContentSynchronisation contentSynchronisation = new ContentSynchronisation();
        contentSynchronisation.setEnabled(false);
        descriptor.setContentSynchronisation(contentSynchronisation);
        assertFalse(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void syncPropsDisabledWhenContentSyncEnabledAndContentPropSyncDisabledAndPropSyncDisabled() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(false);
        ContentSynchronisation contentSynchronisation = new ContentSynchronisation();
        contentSynchronisation.setEnabled(true);
        contentSynchronisation.getProperties().setEnabled(false);
        descriptor.setContentSynchronisation(contentSynchronisation);
        assertFalse(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void syncPropsEnabledWhenNoContentSyncAndPropSyncEnabled() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(true);
        descriptor.setContentSynchronisation(null);
        assertTrue(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void syncPropsEnabledWhenContentSyncEnabledAndContentPropSyncEnabledAndPropSyncDisabled() {
        HttpRepoDescriptor descriptor = new HttpRepoDescriptor();
        descriptor.setSynchronizeProperties(false);
        ContentSynchronisation contentSynchronisation = new ContentSynchronisation();
        contentSynchronisation.setEnabled(true);
        contentSynchronisation.getProperties().setEnabled(true);
        descriptor.setContentSynchronisation(contentSynchronisation);
        assertTrue(researchService.isRepoConfiguredToSyncProperties(descriptor));
    }

    @Test
    public void isRealRepoOrAllowedVirtual_allowedVirtualRepos() throws IOException {
        for (RepoType repoType : ResearchServiceImpl.ALLOWED_VIRTUAL_REPO_TYPES) {
            try (CloseableHttpClient client = getClient(repoType.getType())) {
                String repoKey = repoType.getType() + "-virtual";
                URI uri = URI.create("http://admin:password1@localhost:18082/artifactory/" + repoKey + "/" +
                        "helm/elasticsearch/elasticsearch-7.6.0.tgz");
                configureAddonsManager();

                boolean result = researchService.isAllowedRepo(client, uri, repoKey);
                assertTrue(result);
            }
        }
    }

    @Test
    public void isRealRepoOrAllowedVirtual_notAllowedVirtualRepos() throws IOException {
        Set<RepoType> notAllowedRepoTypes = Stream.of(RepoType.values())
                .filter(repoType -> !ResearchServiceImpl.ALLOWED_VIRTUAL_REPO_TYPES.contains(repoType))
                .collect(Collectors.toSet());
        for (RepoType repoType : notAllowedRepoTypes) {
            try (CloseableHttpClient client = getClient(repoType.getType())) {
                String repoKey = repoType.getType() + "-virtual";
                URI uri = URI.create("http://admin:password1@localhost:18082/artifactory/" + repoKey + "/" +
                        "helm/elasticsearch/elasticsearch-7.6.0.tgz");
                configureAddonsManager();

                boolean result = researchService.isAllowedRepo(client, uri, repoKey);
                assertFalse(result);
            }
        }
    }

    @Test
    public void isRealRepoOrAllowedVirtual_noSuchRepoType() throws IOException {
        String packageType = "noSuchPackageType";
        try (CloseableHttpClient client = getClient(packageType)) {
            String repoKey = packageType + "-virtual";
            URI uri = URI.create("http://admin:password1@localhost:18082/artifactory/" + repoKey + "/" +
                    "helm/elasticsearch/elasticsearch-7.6.0.tgz");
            configureAddonsManager();

            boolean result = researchService.isAllowedRepo(client, uri, repoKey);
            assertFalse(result);
        }
    }

    @Test(dataProvider = "provideUrls")
    public void checkProduceVersionUrl(String inputUrl, String outputUrl) {
        URI uri = URI.create(inputUrl);
        String result = researchService.produceVersionUrl(uri);
        Assert.assertEquals(result, outputUrl);
    }

    @DataProvider
    private Object[][] provideUrls() {
        return new Object[][]{
                {"http://server:8081/artifactory/repoName/", "http://server:8081/artifactory/api/system/version"},
                {"http://serverName/artifactory/repoName/", "http://serverName/artifactory/api/system/version"},
                {"https://server/artifactory/repoName/", "https://server/artifactory/api/system/version"},
                {"https://server/artifactory/api/npm/npm-remote/", "https://server/artifactory/api/system/version"},
                {"http://my.server.com/api/docker/docker-remote/", "http://my.server.com/api/system/version"},
                {"https://my.server.com/api/docker/docker-remote/", "https://my.server.com/api/system/version"},
                {"https://serverName/routed/docker-remote/", "https://serverName/routed/api/system/version"},
                {"https://serverName/artifactory2/docker-remote/", "https://serverName/artifactory2/api/system/version"},
                {"https://serverName/docker-remote/", "https://serverName/api/system/version"},
        };
    }

    @Nonnull
    private CloseableHttpClient getClient(String packageType) throws IOException {
        CloseableHttpClient stubClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse response = getResponse(packageType);
        when(stubClient.execute(any())).thenReturn(response);
        return stubClient;
    }

    private void configureAddonsManager() {
        HaCommonAddon addon = mock(HaCommonAddon.class);
        when(addon.getHostId()).thenReturn("host123");
        when(addonsManager.addonByType(HaCommonAddon.class)).thenReturn(addon);
    }

    @Nonnull
    private CloseableHttpResponse getResponse(String packageType) throws IOException {
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        InputStream repoConfigInputStream = getVirtualRepoConfigAsInputStream(packageType);
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContent()).thenReturn(repoConfigInputStream);
        when(response.getEntity()).thenReturn(httpEntity);
        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getStatusLine()).thenReturn(statusLine);
        return response;
    }

    @Nonnull
    private InputStream getVirtualRepoConfigAsInputStream(String packageType) throws IOException {
        HttpRepositoryConfigurationImpl httpRepositoryConfiguration = new HttpRepositoryConfigurationImpl();
        httpRepositoryConfiguration.setType("virtual");
        httpRepositoryConfiguration.setPackageType(packageType);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JsonGenerator jasonGenerator = JacksonFactory.createJsonGenerator(outputStream);
        jasonGenerator.writeObject(httpRepositoryConfiguration);
        byte[] bytes = outputStream.toByteArray();
        return new ByteArrayInputStream(bytes);
    }
}