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

public class GetApiKeyServiceTest extends ApiKeyBaseTest {

    @Test(dataProvider = "provideIdAndUserNameForTest")
    public void getApiKeyTest(String id, String userName) {
        //setup
        TokenKeyValue tokenKeyValue = mock(TokenKeyValue.class);
        when(tokenKeyValue.getToken()).thenReturn(TOKEN);
        when(apiKeyManager.getToken(userName)).thenReturn(tokenKeyValue);
        when(authorizationService.isAnonymous()).thenReturn(false);
        when(authorizationService.currentUsername()).thenReturn(userName);
        ArtifactoryRestRequest artifactoryRestRequest = mock(ArtifactoryRestRequest.class);
        RestResponse restResponse = spy(ArtifactoryRestResponse.class);
        when(artifactoryRestRequest.getPathParamByKey("id")).thenReturn(id);

        //when
        getRestService().execute(artifactoryRestRequest, restResponse);

        //than
        verify(authorizationService, times(1)).isAnonymous();
        verify(authorizationService, times(1)).currentUsername();
        verify(apiKeyManager, times(1)).getToken(userName);
        verify(artifactoryRestRequest, times(1)).getPathParamByKey("id");
        assertApiKeyResponse(restResponse);
    }

    @Override
    protected RestService getRestService() {
        GetApiKeyService getApiKeyService = new GetApiKeyService();
        getApiKeyService.authorizationService = authorizationService;
        getApiKeyService.apiKeyManager = apiKeyManager;
        return getApiKeyService;
    }
}
