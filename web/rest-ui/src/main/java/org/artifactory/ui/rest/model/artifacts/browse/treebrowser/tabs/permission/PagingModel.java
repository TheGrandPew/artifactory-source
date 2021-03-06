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

package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class PagingModel extends BaseModel {

    PagingModel() {
    }

    private String totalItems;

    private List<? extends RestModel> pagingData;

    public PagingModel(
            long numOfPages, List<? extends RestModel> pagingData) {
        this.totalItems = Long.toString(numOfPages);
        this.pagingData = pagingData;
    }

    public String getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(String totalItems) {
        this.totalItems = totalItems;
    }

    public List<? extends RestModel> getPagingData() {
        return pagingData;

    }

    public void setPagingData(List<PrincipalEffectivePermissions> pagingData) {
        this.pagingData = pagingData;
    }
}
