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

package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.watchers;

import com.google.common.collect.Lists;
import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.watch.ArtifactWatchAddon;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.fs.WatchersInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.watchers.WatchersArtifactInfo;
import org.artifactory.ui.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetWatchersService implements RestService {

    @Autowired
    private AuthorizationService authService;

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepoPath repoPath = RequestUtils.getPathFromRequest(request);
        if(repoService.isVirtualRepoExist(repoPath.getRepoKey())){
            repoPath = repoService.getVirtualItemInfo(repoPath).getRepoPath();
        }
        boolean canManage = authService.canManage(repoPath);
        if (canManage) {
            // get all watchers related to repo
            List<RestModel> watchersData = getWatchersData(repoPath);
            // update response with data
            response.iModelList(watchersData);
        }
    }

    private List<RestModel> getWatchersData(RepoPath repoPath) {
       ArtifactWatchAddon artifactWatchAddon = ContextHelper.get().beanForType(AddonsManager.class).addonByType(
               ArtifactWatchAddon.class);
        List<RestModel> actions = Lists.newArrayList();
        Map<RepoPath, WatchersInfo> allWatchers = artifactWatchAddon.getAllWatchers(repoPath);
       if (allWatchers != null && !allWatchers.isEmpty()) {
           Set<Map.Entry<RepoPath, WatchersInfo>> allEntries = allWatchers.entrySet();
           for (Map.Entry<RepoPath, WatchersInfo> entry : allEntries) {
               RepoPath watchedPath = entry.getKey();
               WatchersInfo watchers = entry.getValue();
               actions.addAll(watchers.getWatchers().stream().map(
                       watcher -> new WatchersArtifactInfo(watcher.getUsername(), watcher.getWatchingSinceTime(),
                               watchedPath, "RemoveWatcher")).collect(Collectors.toList()));
           }
       }
       return actions;
   }
}
