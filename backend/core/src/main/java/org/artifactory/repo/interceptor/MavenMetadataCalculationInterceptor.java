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

package org.artifactory.repo.interceptor;

import lombok.NonNull;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.maven.MavenMetadataService;
import org.artifactory.api.maven.MavenMetadataWorkItem;
import org.artifactory.api.properties.PropertiesService;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.descriptor.repo.RepoType;
import org.artifactory.descriptor.repo.SnapshotVersionBehavior;
import org.artifactory.mime.MavenNaming;
import org.artifactory.repo.LocalRepo;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.interceptor.storage.StorageInterceptorAdapter;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.util.RepoLayoutUtils;
import org.artifactory.util.RepoPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Interceptor which handles maven metadata calculation upon creation and removal
 *
 * @author Noam Tenne
 */
public class MavenMetadataCalculationInterceptor extends StorageInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(MavenMetadataCalculationInterceptor.class);

    private MavenMetadataService mavenMetadataService;
    private InternalRepositoryService repoService;

    @NonNull
    public MavenMetadataService getMavenMetadataService() {
        return mavenMetadataService;
    }

    @Autowired
    public void setMavenMetadataService(@NonNull MavenMetadataService mavenMetadataService) {
        this.mavenMetadataService = mavenMetadataService;
    }

    @NonNull
    public InternalRepositoryService getRepoService() {
        return repoService;
    }

    @Autowired
    public void setRepoService(@NonNull InternalRepositoryService repoService) {
        this.repoService = repoService;
    }

    /**
     * If the newly created item is a pom file, this method will calculate the maven metadata of it's parent folder
     *
     * @param fsItem       Newly created item
     * @param statusHolder StatusHolder
     */
    @Override
    public void afterCreate(VfsItem fsItem, MutableStatusHolder statusHolder, InterceptorCreateContext ctx) {
        if (shouldRecalculateOnCreate(fsItem)) {
            if (MavenNaming.isUniqueSnapshot(fsItem.getPath()) ||
                    (isPomFile(fsItem) && MavenNaming.isSnapshot(fsItem.getPath()))) {
                // unique snapshots require instant metadata calculation since it is used to calculate future snapshots
                // this is also done for any kind of file to support classifier snapshot version introduced in Maven 3
                // we also instantly calculate if non-unique pom is deployed for simplicity
                mavenMetadataService
                        .calculateMavenMetadata(buildMavenMetadataWorkItem(fsItem, fsItem.getRepoPath().getParent()));
            }

            if (isPomFile(fsItem)) {
                // for pom files we need to trigger metadata calculation on the grandparent non-recursively -
                // potential new version and snapshot.
                // this can be done asynchronously since it doesn't require instant update
                mavenMetadataService.calculateMavenMetadataAsync(
                        buildMavenMetadataWorkItem(fsItem, RepoPathUtils.getAncestor(fsItem.getRepoPath(), 2)));
            }
        }
    }

    @Override
    public void afterDelete(VfsItem fsItem, MutableStatusHolder statusHolder, DeleteContext ctx) {
        if (ctx.isCalculateMavenMetadata() && isMavenSupportingRepo(getLocalRepo(fsItem))) {
            // calculate maven metadata on the parent path
            RepoPath parent = ctx.getRepoPath().getParent();
            if (parent != null && !parent.isRoot()) {
                if (!ConstantValues.mvnMetadataCalculationSkipDeleteEvent.getBoolean()) {
                    mavenMetadataService.calculateMavenMetadataAsync(buildMavenMetadataWorkItem(fsItem, parent));
                }
                if (isPomFile(fsItem) &&
                        !ConstantValues.mvnMetadataCalculationSkipDeleteEvent.getBoolean()) { // ISSUE RTFACT - 10257
                    // also calculate maven metadata on the artifactId node (grandparent of the file)
                    mavenMetadataService
                            .calculateMavenMetadataAsync(buildMavenMetadataWorkItem(fsItem, parent.getParent()));
                }
            }
        }
    }

    @NonNull
    private MavenMetadataWorkItem buildMavenMetadataWorkItem(@NonNull VfsItem fsItem, @NonNull RepoPath repoPath) {
        return new MavenMetadataWorkItem(repoPath, false, isMavenPlugin(fsItem));
    }

    private boolean shouldRecalculateOnCreate(VfsItem fsItem) {
        if (!fsItem.isFile()) {
            return false;
        }

        LocalRepo localRepo = getLocalRepo(fsItem);
        if (!isMavenSupportingRepo(localRepo)) {
            return false;
        }

        // it's a local non-cache repository, check the snapshot behavior
        MavenArtifactInfo moduleInfo = MavenArtifactInfo.fromRepoPath(fsItem.getRepoPath());
        if (!moduleInfo.isValid()) {
            return false;
        }

        return !MavenNaming.isSnapshot(fsItem.getPath()) ||
                !SnapshotVersionBehavior.DEPLOYER.equals(localRepo.getMavenSnapshotVersionBehavior());
    }

    private LocalRepo getLocalRepo(VfsItem fsItem) {
        return repoService.localRepositoryByKey(fsItem.getRepoKey());
    }

    private boolean isMavenSupportingRepo(LocalRepo localRepo) {
        if (localRepo == null) {
            return false;
        }
        RepoLayout repoLayout = localRepo.getDescriptor().getRepoLayout();
        RepoType type = localRepo.getDescriptor().getType();
        // Do not calculate maven metadata if type == null or type doesn't belong to the maven group (Maven, Ivy, Gradle) or repoLayout not equals MAVEN_2_DEFAULT
        if (type != null && !(type.isMavenGroup() || RepoLayoutUtils.MAVEN_2_DEFAULT.equals(repoLayout))) {
            log.debug("Skipping maven metadata calculation since repoType '{}' doesn't belong to neither Maven, Ivy, Gradle" +
                    " repositories types.", localRepo.getKey());
            return false;
        }
        return true;
    }

    private boolean isPomFile(VfsItem fsItem) {
        return MavenNaming.isPom(fsItem.getRepoPath().getPath());
    }

    private boolean isMavenPlugin(VfsItem fsItem) {
        Set<String> propertyValues = fsItem.getProperties().get(PropertiesService.MAVEN_PLUGIN_PROPERTY_NAME);
        if (propertyValues == null) {
            return false;
        } else {
            return Boolean.parseBoolean(propertyValues.stream().findFirst().orElse("false"));
        }
    }
}
