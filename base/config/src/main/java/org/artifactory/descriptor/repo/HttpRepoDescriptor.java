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

package org.artifactory.descriptor.repo;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.Descriptor;
import org.jfrog.common.config.diff.DiffReference;
import org.jfrog.common.config.diff.GenerateDiffFunction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Fred Simon
 */
@XmlType(name = "RemoteRepoType",
        propOrder = {"username", "password", "allowAnyHostAuth", "socketTimeoutMillis", "enableCookieManagement",
                "enableTokenAuthentication", "localAddress", "proxy", "queryParams", "propagateQueryParams", "clientTlsCertificate"},
        namespace = Descriptor.NS)
@GenerateDiffFunction
public class HttpRepoDescriptor extends RemoteRepoDescriptor {

    private String username;

    private String password;

    private boolean allowAnyHostAuth;

    @XmlElement(defaultValue = "15000", required = false)
    private int socketTimeoutMillis = 15000;//Default socket timeout

    private boolean enableCookieManagement;

    private boolean enableTokenAuthentication;

    private String localAddress;
    @XmlIDREF
    @XmlElement(name = "proxyRef")
    @DiffReference
    private ProxyDescriptor proxy;

    private String queryParams;

    private boolean propagateQueryParams;

    @XmlElement(name = "clientTlsCertificate", required = false)
    private String clientTlsCertificate;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public boolean isAllowAnyHostAuth() {
        return allowAnyHostAuth;
    }

    public void setAllowAnyHostAuth(boolean allowAnyHostAuth) {
        this.allowAnyHostAuth = allowAnyHostAuth;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public ProxyDescriptor getProxy() {
        return proxy;
    }

    public void setProxy(ProxyDescriptor proxy) {
        this.proxy = proxy;
    }

    public String getQueryParams() {
        return StringUtils.removeStart(queryParams, "?");
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public boolean isEnableCookieManagement() {
        return enableCookieManagement;
    }

    public void setEnableCookieManagement(boolean enableCookieManagement) {
        this.enableCookieManagement = enableCookieManagement;
    }

    public boolean isEnableTokenAuthentication() {
        return enableTokenAuthentication;
    }

    public void setEnableTokenAuthentication(boolean enableTokenAuthentication) {
        this.enableTokenAuthentication = enableTokenAuthentication;
    }

    public boolean isPropagateQueryParams() {
        return propagateQueryParams;
    }

    public void setPropagateQueryParams(boolean propagateQueryParams) {
        this.propagateQueryParams = propagateQueryParams;
    }

    public String getClientTlsCertificate() {
        return clientTlsCertificate;
    }

    public void setClientTlsCertificate(String clientTlsCertificate) {
        this.clientTlsCertificate = clientTlsCertificate;
    }
}