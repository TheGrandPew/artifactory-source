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

package org.artifactory.ui.rest.service.admin.configuration.servers;

import org.artifactory.addon.AddonsManager;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.util.AolUtils;
import org.artifactory.rest.exception.ForbiddenWebAppException;
import org.artifactory.servers.ServerModel;
import org.artifactory.servers.ServersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Get details on all Artifactory servers.
 * Supported for all types of licenses and for single or multiple instances.
 *
 * @author Rotem Kfir
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetServersStatusService implements RestService {

    @Autowired
    private ServersService serversService;
    @Autowired
    private AddonsManager addonsManager;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {

        AolUtils.assertNotAol("GetServersStatus");
        if (addonsManager.getArtifactoryRunningMode().isOss()) {
            throw new ForbiddenWebAppException("Function is not supported for Artifactory OSS");
        }

        List<ServerModel> allArtifactoryServers = serversService.getAllArtifactoryServers();
        ServersStatusResponse serversResponse = new ServersStatusResponse();
        serversResponse.setServers(allArtifactoryServers);
        response.iModel(serversResponse);
    }
}

