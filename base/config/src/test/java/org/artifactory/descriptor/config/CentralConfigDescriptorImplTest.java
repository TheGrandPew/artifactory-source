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

package org.artifactory.descriptor.config;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.artifactory.descriptor.backup.BackupDescriptor;
import org.artifactory.descriptor.property.PropertySet;
import org.artifactory.descriptor.replication.LocalReplicationDescriptor;
import org.artifactory.descriptor.replication.RemoteReplicationDescriptor;
import org.artifactory.descriptor.repo.*;
import org.artifactory.descriptor.security.SecurityDescriptor;
import org.artifactory.util.RepoLayoutUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Tests the MutableCentralConfigDescriptorImpl class.
 *
 * @author Yossi Shaul
 */
@Test
public class CentralConfigDescriptorImplTest {
    private CentralConfigDescriptorImpl cc;

    @BeforeMethod
    public void initCentralConfig() {
        cc = CentralConfigTestUtils.initCentralConfig();
    }

    public void defaultsTest() {
        CentralConfigDescriptorImpl cc = new CentralConfigDescriptorImpl();
        assertNotNull(cc.getLocalRepositoriesMap(), "Local repos map should not be null");
        assertNotNull(cc.getRemoteRepositoriesMap(), "Remote repos map should not be null");
        assertNotNull(cc.getVirtualRepositoriesMap(), "Virtual repos map should not be null");
        assertNotNull(cc.getBackups(), "Backups list should not be null");
        assertNotNull(cc.getProxies(), "Proxies list should not be null");
        assertNotNull(cc.getPropertySets(), "Property sets list should not be null");
        assertNull(cc.getIndexer(), "Indexer should not be null");
        assertNotNull(cc.getSecurity(), "Security should not be null");
        assertNull(cc.getServerName(), "Server name should not be null");
        assertNotNull(cc.getDateFormat(), "Date format should not be null");
        assertTrue(cc.getFileUploadMaxSizeMb() > 50,
                "Default max file upload size should be bigger than 50mb");
        assertFalse(cc.isOfflineMode(), "Offline mode should be false by default");
        assertNotNull(cc.getRepoLayouts(), "Repo layouts list should not be null");
        assertNotNull(cc.getRemoteReplications(), "Remote replication list should not be null");
        assertNotNull(cc.getLocalReplications(), "Local replication list should not be null");
    }

    public void uniqueKeyExistence() {
        assertFalse(cc.isKeyAvailable("local1"));
        assertFalse(cc.isRepositoryExists("backup2"));
        assertFalse(cc.isRepositoryExists("proxy2"));

        assertTrue(cc.isKeyAvailable("proxy22"));
        cc.setSecurity(new SecurityDescriptor());
        assertTrue(cc.isKeyAvailable("ldap1"));

        assertTrue(cc.isKeyAvailable("repo")); // Global virtual repo has been deprecated

        assertTrue(cc.isPropertySetExists("set2"));

        assertFalse(cc.isKeyAvailable("layout1"));
        assertTrue(cc.isRepoLayoutExists("layout2"));
    }

    public void repositoriesExistence() {
        assertEquals(cc.getLocalRepositoriesMap().size(), 2, "Local repos count mismatch");
        assertEquals(cc.getRemoteRepositoriesMap().size(), 1, "Remote repos count mismatch");
        assertEquals(cc.getVirtualRepositoriesMap().size(), 1,
                "Virtual repos count mismatch");
        assertTrue(cc.isRepositoryExists("local1"));
        assertTrue(cc.isRepositoryExists("local2"));
        assertTrue(cc.isRepositoryExists("remote1"));
        assertTrue(cc.isRepositoryExists("virtual1"));
    }

    public void proxyExistence() {
        assertEquals(cc.getProxies().size(), 2, "Proxies count mismatch");
        assertTrue(cc.isProxyExists("proxy1"));
        assertTrue(cc.isProxyExists("proxy2"));
    }

    public void removeProxy() {
        assertTrue(cc.isProxyExists("proxy2"));
        ProxyDescriptor removedProxy = cc.removeProxy("proxy2");
        assertEquals(removedProxy.getKey(), "proxy2");
        assertEquals(cc.getProxies().size(), 1, "Only one proxy expected");
        assertFalse(cc.isProxyExists("proxy2"));
    }

    public void removeReferencedProxy() {
        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setKey("referencedProxy");
        cc.addProxy(proxy, false);

        HttpRepoDescriptor remoteRepo =
                (HttpRepoDescriptor) cc.getRemoteRepositoriesMap().get("remote1");
        remoteRepo.setProxy(proxy);

        assertNotNull(remoteRepo.getProxy(), "Just checking ...");

        LocalReplicationDescriptor localReplication = cc.getLocalReplication("local1");
        localReplication.setProxy(proxy);

        cc.removeProxy("referencedProxy");

        assertNull(remoteRepo.getProxy(), "Proxy should have been removed from the remote repo");
        assertNull(localReplication.getProxy(), "Proxy should have been removed from the local replication.");
    }

    public void defaultProxyChanged() {
        ProxyDescriptor proxy = new ProxyDescriptor();
        proxy.setKey("defaultProxy");
        proxy.setDefaultProxy(true);
        cc.addProxy(proxy, false);
        addDefaultProxyToRemoteRepositories(proxy);
        LocalReplicationDescriptor localReplication = cc.getLocalReplication("local1");
        localReplication.setProxy(proxy);

        ProxyDescriptor newDefaultProxy = new ProxyDescriptor();
        newDefaultProxy.setKey("newDefaultProxy");
        cc.addProxy(newDefaultProxy, false);
        newDefaultProxy.setDefaultProxy(true);
        cc.proxyChanged(newDefaultProxy, true);
        HttpRepoDescriptor remoteRepo = (HttpRepoDescriptor) cc.getRemoteRepositoriesMap().get("remote1");

        assertNotNull(remoteRepo.getProxy(), "Remote repo should have a proxy");
        assertEquals(remoteRepo.getProxy().getKey(), "newDefaultProxy", "Proxy name does not match");

        assertNotNull(localReplication.getProxy(), "Local replication should have a proxy");
        assertEquals(localReplication.getProxy().getKey(), "newDefaultProxy", "Proxy name does not match");

        assertFalse(proxy.isDefaultProxy(), "Original proxy should now be false");

        proxy.setDefaultProxy(true);
        cc.proxyChanged(proxy, false);

        assertNotNull(remoteRepo.getProxy(), "Remote repo should have a proxy");
        assertEquals(remoteRepo.getProxy().getKey(), "newDefaultProxy", "Proxy name does not match");

        assertNotNull(localReplication.getProxy(), "Remote repo should have a proxy");
        assertEquals(localReplication.getProxy().getKey(), "newDefaultProxy", "Proxy name does not match");
    }

    public void backupExistence() {
        assertEquals(cc.getBackups().size(), 3, "Backups count mismatch");
        assertTrue(cc.isBackupExists("backup1"));
        assertTrue(cc.isBackupExists("backup2"));
        assertTrue(cc.isBackupExists("backup3"));

    }

    public void removeBackup() {
        assertTrue(cc.isBackupExists("backup2"));
        BackupDescriptor removedBackup = cc.removeBackup("backup2");
        assertEquals(removedBackup.getKey(), "backup2");
        assertEquals(cc.getBackups().size(), 2, "Only 2 backups expected");
        assertFalse(cc.isBackupExists("backup2"));
    }

    public void excludeNewRepoFromBackup() {
        LocalRepoDescriptor newLocal = new LocalRepoDescriptor();
        newLocal.setKey("newLocal");
        cc.addLocalRepository(newLocal);
        cc.conditionallyAddToBackups(newLocal);
        RemoteRepoDescriptor newRemote = new HttpRepoDescriptor();
        newRemote.setKey("newRemote");
        cc.addRemoteRepository(newRemote);

        assertTrue(cc.isBackupExists("backup1"));
        BackupDescriptor backup1 = Iterables.tryFind(cc.getBackups(), new Predicate<BackupDescriptor>() {
            @Override
            public boolean apply(BackupDescriptor input) {
                return "backup1".equals(input.getKey());
            }
        }).orNull();
        assertNotNull(backup1, "Should find backup1");
        assertEquals(backup1.getExcludedRepositories().size(), 0);

        assertTrue(cc.isBackupExists("backup3"));
        BackupDescriptor backup3 = Iterables.tryFind(cc.getBackups(), new Predicate<BackupDescriptor>() {
            @Override
            public boolean apply(BackupDescriptor input) {
                return "backup3".equals(input.getKey());
            }
        }).orNull();
        assertNotNull(backup3, "Should find backup3");

        List<RealRepoDescriptor> excludedRepositories = backup3.getExcludedRepositories();
        assertEquals(excludedRepositories.size(), 2);
        assertTrue(excludedRepositories.contains(newLocal));
        assertTrue(excludedRepositories.contains(newRemote));
    }

    public void propertySetExistence() {
        assertEquals(cc.getPropertySets().size(), 2, "The config should contain 2 property sets");
        assertTrue(cc.isPropertySetExists("set1"), "The config should contain the property set: 'set1'");
        assertTrue(cc.isPropertySetExists("set2"), "The config should contain the property set: 'set2'");
    }

    public void removePropertySet() {
        PropertySet setToRemove = new PropertySet();
        String setName = "toRemove";
        setToRemove.setName(setName);
        cc.addPropertySet(setToRemove);

        LocalRepoDescriptor localDescriptor = cc.getLocalRepositoriesMap().get("local1");
        localDescriptor.addPropertySet(setToRemove);

        RemoteRepoDescriptor remoteDescriptor = cc.getRemoteRepositoriesMap().get("remote1");
        remoteDescriptor.addPropertySet(setToRemove);

        //Sanity check
        assertNotNull(localDescriptor.getPropertySet(setName), "The descriptor should contain the added " +
                "property set.");

        assertNotNull(cc.removePropertySet(setName), "The property set should have been removed from the central " +
                "config descriptor.");

        assertNull(localDescriptor.getPropertySet(setName), "The property set should have been removed from the " +
                "local repo descriptor.");

        assertNull(remoteDescriptor.getPropertySet(setName),
                "The property set should have been removed from the " +
                        "remote repo descriptor.");
    }

    public void repoLayoutExistence() {
        assertEquals(cc.getRepoLayouts().size(), 2, "The config should contain 2 repository layouts");
        assertTrue(cc.isRepoLayoutExists("layout1"), "The config should contain the repo layout: 'layout1'");
        assertTrue(cc.isRepoLayoutExists("layout2"), "The config should contain the repo layout: 'layout2'");
    }

    public void removeRepoLayout() {
        RepoLayout layoutToRemove = new RepoLayout();
        String name = "toRemove";
        layoutToRemove.setName(name);
        cc.addRepoLayout(layoutToRemove);

        LocalRepoDescriptor localDescriptor = cc.getLocalRepositoriesMap().get("local1");
        localDescriptor.setRepoLayout(layoutToRemove);

        RemoteRepoDescriptor remoteDescriptor = cc.getRemoteRepositoriesMap().get("remote1");
        remoteDescriptor.setRepoLayout(layoutToRemove);

        VirtualRepoDescriptor virtualDescriptor = cc.getVirtualRepositoriesMap().get("virtual1");
        virtualDescriptor.setRepoLayout(layoutToRemove);

        assertNotNull(localDescriptor.getRepoLayout(), "The descriptor should contain the added repo layout");

        assertNotNull(cc.removeRepoLayout(name), "The repo layout should have been removed from the central " +
                "config descriptor.");

        assertEquals(localDescriptor.getRepoLayout(), RepoLayoutUtils.MAVEN_2_DEFAULT,
                "The repo layout should have defaulted.");

        assertEquals(remoteDescriptor.getRepoLayout(), RepoLayoutUtils.MAVEN_2_DEFAULT,
                "The repo layout should have defaulted.");

        assertNull(virtualDescriptor.getRepoLayout(), "The repo layout should have defaulted to null.");
    }

    public void replicationsExistence() {
        assertNotNull(cc.getRemoteReplication("remote1"));
        assertNotNull(cc.getRemoteReplication("remote2"));
        assertNotNull(cc.getLocalReplication("local1"));
        assertNotNull(cc.getLocalReplication("local2"));
    }

    public void removeReplicationUpdates() {
        RemoteReplicationDescriptor remoteReplication = cc.getRemoteReplication("remote2");
        assertNotNull(remoteReplication, "Expected to find second remote replication.");
        cc.removeRemoteReplication(remoteReplication);
        assertNull(cc.getRemoteReplication("remote2"), "Second remote replication should have been removed.");

        LocalReplicationDescriptor localReplication = cc.getLocalReplication("local2");
        assertNotNull(localReplication, "Expected to find second local replication.");
        cc.removeLocalReplication(localReplication);
        assertNull(cc.getLocalReplication("local2"), "Second local replication should have been removed.");
    }

    private void addDefaultProxyToRemoteRepositories(ProxyDescriptor proxyDescriptor) {
        Map<String, RemoteRepoDescriptor> descriptorOrderedMap = cc.getRemoteRepositoriesMap();
        for (RemoteRepoDescriptor descriptor : descriptorOrderedMap.values()) {
            if (descriptor instanceof HttpRepoDescriptor) {
                HttpRepoDescriptor httpRepoDescriptor = (HttpRepoDescriptor) descriptor;
                httpRepoDescriptor.setProxy(proxyDescriptor);
            }
        }
    }
}
