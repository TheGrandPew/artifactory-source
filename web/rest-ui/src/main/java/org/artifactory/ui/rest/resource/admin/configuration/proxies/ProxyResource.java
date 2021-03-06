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

package org.artifactory.ui.rest.resource.admin.configuration.proxies;

import org.artifactory.api.security.AuthorizationService;
import org.artifactory.rest.common.model.proxies.ProxiesModel;
import org.artifactory.rest.common.resource.BaseResource;
import org.artifactory.ui.rest.model.admin.configuration.proxy.Proxy;
import org.artifactory.ui.rest.service.admin.configuration.ConfigServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Chen Keinan
 */
@Path("proxies")
@RolesAllowed({AuthorizationService.ROLE_ADMIN})
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProxyResource extends BaseResource {

    @Autowired
    protected ConfigServiceFactory configServiceFactory;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProxies(Proxy proxy)
            throws Exception {
        return runService(configServiceFactory.createProxiesService(), proxy);
    }


    @PUT
    @Path("crud{id:(/[^/]+?)?}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProxies(Proxy proxy)
            throws Exception {
        return runService(configServiceFactory.updateProxiesService(), proxy);
    }

    @GET
    @Path("crud{id:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProxies()
            throws Exception {
        return runService(configServiceFactory.getProxiesService());
    }

    @Path("deleteProxies")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProxies(ProxiesModel proxiesModel)
            throws Exception {
        return runService(configServiceFactory.deleteProxiesService(), proxiesModel);
    }

    @GET
    @Path("proxyKeys")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProxyKeys() {
        return runService(configServiceFactory.getProxyKeysService());
    }
}
