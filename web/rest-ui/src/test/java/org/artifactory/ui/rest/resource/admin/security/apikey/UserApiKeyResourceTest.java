/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2018 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.ui.rest.resource.admin.security.apikey;

import org.apache.http.HttpHeaders;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.security.UserInfo;
import org.artifactory.security.props.auth.ApiKeyManager;
import org.artifactory.security.props.auth.model.TokenKeyValue;
import org.artifactory.ui.rest.service.admin.security.SecurityServiceFactory;
import org.artifactory.ui.rest.service.admin.security.user.userprofile.UserProfileHelperService;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.lang.reflect.Field;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Bar Haim
 */
public class UserApiKeyResourceTest {

    private static final String ADMIN = "admin";
    private static final String TEST_USER = "test-user";

    @Mock
    private AuthorizationService authorizationService;

    @Mock
    private ApiKeyManager apiKeyManager;

    @Mock
    SecurityServiceFactory securityService;

    @Mock
    UserProfileHelperService userProfileHelperService;

    @BeforeMethod
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @DataProvider
    private static Object[][] provideIdAndUserNameForTestIsExist() {
        return new Object[][] {
                {ADMIN, ADMIN, 200},
                {TEST_USER, ADMIN, 200},
                {TEST_USER, TEST_USER, 200},
                {"", TEST_USER, 200},
                {ADMIN, TEST_USER, 400},
                {"not-exists-user", ADMIN, 404}
        };
    }

    @Test
    public void testIsAllowApiKeyAccessUserAuthenticatedNoAuthHeader() throws Exception {
        UserInfo userInfo = mock(UserInfo.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        UserApiKeyResource userApiKeyResource = new UserApiKeyResource(securityService,
                userProfileHelperService, authorizationService);
        Field requestField = UserApiKeyResource.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(userApiKeyResource,httpServletRequest);
        when(authorizationService.requireProfileUnlock()).thenReturn(false);
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        when(userProfileHelperService.loadUserInfo()).thenReturn(userInfo);
        when(userInfo.isAnonymous()).thenReturn(false);
        assertTrue(isBlank(userApiKeyResource.isAllowApiKeyAccess()));
    }

    @Test
    public void testIsAllowApiKeyAccessAuthenticateWithPassword() throws Exception {
        UserInfo userInfo = mock(UserInfo.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic YWRtaW46cGFzc3dvcmQ=");
        UserApiKeyResource userApiKeyResource = new UserApiKeyResource(securityService,
                userProfileHelperService, authorizationService);
        when(userProfileHelperService.authenticate(userInfo,"password")).thenReturn(true);
        Field requestField = UserApiKeyResource.class.getDeclaredField("request");
        requestField.setAccessible(true);
        requestField.set(userApiKeyResource,httpServletRequest);
        when(authorizationService.requireProfileUnlock()).thenReturn(true);
        when(userProfileHelperService.loadUserInfo()).thenReturn(userInfo);
        when(userInfo.getUsername()).thenReturn("admin");
        when(userInfo.isAnonymous()).thenReturn(false);
        assertTrue(isBlank(userApiKeyResource.isAllowApiKeyAccess()));
    }

    @Test(dataProvider = "provideIdAndUserNameForTestIsExist")
    public void testIsExist(String id, String userName, int expectedStatusCode) {
        when(apiKeyManager.getToken((TEST_USER))).thenReturn(mock(TokenKeyValue.class));
        when(apiKeyManager.getToken((ADMIN))).thenReturn(mock(TokenKeyValue.class));
        when(authorizationService.isAnonymous()).thenReturn(false);
        when(authorizationService.currentUsername()).thenReturn(userName);
        when(authorizationService.isAdmin()).thenReturn(userName.equals(ADMIN));
        UserApiKeyResource userApiKeyResource = new UserApiKeyResource(securityService,
                userProfileHelperService, authorizationService);
        userApiKeyResource.apiKeyManager = apiKeyManager;
        Response response = userApiKeyResource.isExist(id);
        assertEquals(response.getStatus(), expectedStatusCode);
    }
}