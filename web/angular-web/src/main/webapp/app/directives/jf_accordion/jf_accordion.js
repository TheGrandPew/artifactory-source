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
class jfAccordionController {

    constructor($state, $rootScope,ArtifactoryState,$timeout) {
        this.state = $state;
        this.currentAccordion = '';
        this.artifactoryState = ArtifactoryState;
        this.$timeout = $timeout;
        this.openItemByCurrentState();
        $rootScope.$on('$stateChangeSuccess', () => this.openItemByCurrentState());
    }

    openItemByCurrentState() {
        let item = _.find(this.items, (item) => {
            return this.isItemActive(item);
        });
        if (item) item.isOpen = true;
    }

    saveState() {

        this.artifactoryState.setState('saveAdminState',true);
/*
// ACTUAL SAVING OF LAST ADMIN STATE MOVED TO artifactory.states.mudule.js (TO PREVENT SAVING WRONG STATE WHEN CONFIRMATION MODAL IS PRESENTED)
        this.$timeout(()=>{
            this.artifactoryState.setState('lastAdminState',this.state.current);
            this.artifactoryState.setState('lastAdminStateParams', this.state.params);
        });
*/

    }

    isItemActive(item) {
        // (Adam) - don't use $state.includes, because it goes by the route hierarchy 
        let result = _.includes(this.state.current.name, item.state);
        // (Adam) - if item includes paramsParams, match this params with the current state
        if (result && item.stateParams) {
            let relevantParams = _.pick(this.state.params, Object.keys(item.stateParams));
            result = angular.equals(relevantParams, item.stateParams);
        }
        return result;
    }

}

export function jfAccordion() {

    return {
        restrict: 'EA',
        scope: {items: '='},
        controller: jfAccordionController,
        controllerAs: 'jfAccordion',
        templateUrl: 'directives/jf_accordion/jf_accordion.html',
        bindToController: true
    };
}

