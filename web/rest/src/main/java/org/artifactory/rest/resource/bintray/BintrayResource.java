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

package org.artifactory.rest.resource.bintray;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.docker.DockerAddon;
import org.artifactory.api.bintray.BintrayService;
import org.artifactory.api.bintray.docker.BintrayDockerPushRequest;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.rest.constant.BintrayRestConstants;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.rest.common.util.BintrayRestHelper;
import org.artifactory.rest.exception.AuthorizationRestException;
import org.artifactory.rest.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * A resource for Bintray actions
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(BintrayRestConstants.PATH_ROOT)
@RolesAllowed({AuthorizationService.ROLE_ADMIN, AuthorizationService.ROLE_USER})
public class BintrayResource {

    @Autowired
    private InternalRepositoryService repositoryService;

    @Autowired
    private BintrayService bintrayService;

    @Autowired
    private AddonsManager addonsManager;

    @Context
    private HttpServletRequest request;

    /**
     * Pushes a version to Bintray according to the file paths that are specified in the JSON file, if no paths are
     * specified pushes the entire directory tree that resides under the folder containing the json file
     *
     * @param descriptor    full path to the Json spec file containing version and package info and the list of files
     * @param gpgPassphrase (optional) the Passphrase to use in conjunction with the key stored in Bintray to
     *                      sign the version
     * @return result of the operation
     */
    @POST
    @Path("push")
    @Produces({BintrayRestConstants.MT_BINTRAY_PUSH_RESPONSE, MediaType.APPLICATION_JSON})
    public Response pushVersionToBintrayAccordingToSpec(
            @QueryParam("descriptor") String descriptor,
            @QueryParam("gpgSign") Boolean gpgSignOverride,
            @QueryParam("gpgPassphrase") String gpgPassphrase) {

        BasicStatusHolder status = new BasicStatusHolder();
        if (!BintrayRestHelper.isPushToBintrayAllowed(status, null)) {
            throw new AuthorizationRestException(status.getLastError().getMessage());
        }
        FileInfo jsonFile = repositoryService.getFileInfo(InternalRepoPathFactory.create(descriptor));
        status.merge(bintrayService.pushVersionFilesAccordingToSpec(jsonFile, gpgSignOverride, gpgPassphrase));
        return BintrayRestHelper.createAggregatedResponse(status, " version according to spec at: " + descriptor, false);
    }

    @POST
    @Path("docker/push/{repoKey}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response pushDockerTag(@PathParam("repoKey") String repoKey, BintrayDockerPushRequest request) throws IOException {
        BasicStatusHolder status = new BasicStatusHolder();
        if (!BintrayRestHelper.isPushToBintrayAllowed(status, null)) {
            throw new AuthorizationRestException(status.getLastError().getMessage());
        }

        if (StringUtils.isBlank(request.dockerRepository) || StringUtils.isBlank(request.dockerTagName)) {
            throw new BadRequestException("You must provide dockerRepository and dockerTagName");
        }

        if (StringUtils.isBlank(request.bintraySubject) || StringUtils.isBlank(request.bintrayRepo)) {
            throw new BadRequestException("You must provide bintraySubject and bintrayRepo");
        }
        try {
            DockerAddon dockerAddon = addonsManager.addonByType(DockerAddon.class);
            dockerAddon.pushTagToBintray(repoKey, request, null);
            return request.async ?
                    Response.status(HttpStatus.SC_ACCEPTED).entity("Docker tag pushing to Bintray accepted.").build()
                    : Response.ok("Docker tag successfully pushed to Bintray").build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }
}
