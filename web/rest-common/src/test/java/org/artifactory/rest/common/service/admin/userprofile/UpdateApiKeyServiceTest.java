package org.artifactory.rest.common.service.admin.userprofile;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.ArtifactoryRestResponse;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.props.auth.model.TokenKeyValue;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.mock;

public class UpdateApiKeyServiceTest extends ApiKeyBaseTest {

    @Test(dataProvider = "provideIdAndUserNameForTest")
    public void updateApiKeyTest(String id, String userName) {
        //setup
        TokenKeyValue tokenKeyValue = mock(TokenKeyValue.class);
        when(tokenKeyValue.getToken()).thenReturn(TOKEN);
        when(apiKeyManager.refreshToken(userName)).thenReturn(tokenKeyValue);
        when(authorizationService.isAnonymous()).thenReturn(false);
        when(authorizationService.currentUsername()).thenReturn(userName);
        when(authorizationService.isApiKeyAuthentication()).thenReturn(false);
        ArtifactoryRestRequest artifactoryRestRequest = mock(ArtifactoryRestRequest.class);
        RestResponse restResponse = spy(ArtifactoryRestResponse.class);
        when(artifactoryRestRequest.getPathParamByKey("id")).thenReturn(id);
        //when
        getRestService().execute(artifactoryRestRequest, restResponse);

        //then
        verify(authorizationService, times(1)).isAnonymous();
        verify(authorizationService, times(1)).currentUsername();
        verify(authorizationService, times(1)).isApiKeyAuthentication();
        verify(apiKeyManager, times(1)).refreshToken(userName);
        verify(artifactoryRestRequest, times(1)).getPathParamByKey("id");
        assertApiKeyResponse(restResponse);
    }

    @Override
    protected RestService getRestService() {
        UpdateApiKeyService updateApiKeyService = new UpdateApiKeyService();
        updateApiKeyService.authorizationService = authorizationService;
        updateApiKeyService.apiKeyManager = apiKeyManager;
        return updateApiKeyService;
    }
}
