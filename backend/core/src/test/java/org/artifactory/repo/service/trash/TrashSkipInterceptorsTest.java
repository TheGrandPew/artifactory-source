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

package org.artifactory.repo.service.trash;

import com.google.common.collect.Maps;
import junit.framework.AssertionFailedError;
import org.artifactory.common.MutableStatusHolder;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.repo.interceptor.storage.StorageInterceptorsImpl;
import org.artifactory.repo.trash.TrashService;
import org.artifactory.sapi.fs.VfsItem;
import org.artifactory.sapi.interceptor.StorageInterceptor;
import org.artifactory.sapi.interceptor.context.DeleteContext;
import org.artifactory.sapi.interceptor.context.InterceptorCreateContext;
import org.artifactory.sapi.interceptor.context.InterceptorMoveCopyContext;
import org.artifactory.spring.ArtifactoryApplicationContext;
import org.testng.annotations.Test;

import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * Test that storage interceptors are not triggered on the trash
 *
 * @author Shay Yaakov
 */
public class TrashSkipInterceptorsTest {

    @Test
    public void testInterceptors() throws Exception {
        StorageInterceptor root = createMock(StorageInterceptor.class);
        StorageInterceptor interceptor = prepareMockInterceptor(true);

        Map<String, StorageInterceptor> interceptorsHashMap = Maps.newHashMap();
        interceptorsHashMap.put("root", root);
        interceptorsHashMap.put("skipper", interceptor);

        ArtifactoryApplicationContext context = createMock(ArtifactoryApplicationContext.class);
        expect(context.beansForType(StorageInterceptor.class)).andReturn(interceptorsHashMap);
        expect(context.getBean("root")).andReturn(root);

        VfsItem vfsItem = createMock(VfsItem.class);
        RepoPath repoPath = RepoPathFactory.create(TrashService.TRASH_KEY, "some/path");
        expect(vfsItem.getRepoPath()).andReturn(repoPath).anyTimes();
        replay(root, interceptor, context, vfsItem);
        StorageInterceptorsImpl interceptors = new StorageInterceptorsImpl();
        interceptors.setApplicationContext(context);
        interceptors.setBeanName("root");
        interceptors.init();
        interceptors.beforeCreate(vfsItem, null);
        interceptors.afterCreate(vfsItem, null, new InterceptorCreateContext());
        interceptors.beforeDelete(vfsItem, null, false);
        interceptors.afterDelete(vfsItem, null, new DeleteContext(vfsItem.getRepoPath()));
        interceptors.beforeCopy(null, repoPath, null, null);
        interceptors.afterCopy(null, vfsItem, null, null, null);
        interceptors.beforeMove(vfsItem, null, null, null);
        interceptors.beforePropertyCreate(vfsItem, null, null);
        interceptors.afterPropertyCreate(vfsItem, null, null);
        interceptors.beforePropertyDelete(vfsItem, null, null);
        interceptors.afterPropertyDelete(vfsItem, null, null);
        verify(root, interceptor, context, vfsItem);
    }


    private StorageInterceptor prepareMockInterceptor(boolean failOnCreate) {
        StorageInterceptor interceptor = createMock(StorageInterceptor.class);
        interceptor.beforeCreate(anyObject(VfsItem.class), anyObject(MutableStatusHolder.class));
        failOnMethodCall();
        interceptor.afterCreate(anyObject(VfsItem.class), anyObject(MutableStatusHolder.class),
                anyObject(InterceptorCreateContext.class));
        if (failOnCreate) {
            failOnMethodCall();
        } else {
            expectLastCall();
        }
        interceptor.beforeDelete(anyObject(VfsItem.class), anyObject(MutableStatusHolder.class), eq(false));
        failOnMethodCall();
        interceptor.afterDelete(anyObject(VfsItem.class), anyObject(MutableStatusHolder.class), anyObject(DeleteContext.class));
        failOnMethodCall();
        interceptor.beforeCopy(anyObject(), anyObject(), anyObject(), anyObject());
        failOnMethodCall();
        interceptor.afterCopy(anyObject(), anyObject(), anyObject(), anyObject(), anyObject(InterceptorMoveCopyContext.class));
        failOnMethodCall();
        interceptor.beforeMove(anyObject(), anyObject(), anyObject(), anyObject());
        failOnMethodCall();
        interceptor.afterMove(anyObject(), anyObject(), anyObject(), anyObject(), anyObject(InterceptorMoveCopyContext.class));
        failOnMethodCall();
        interceptor.beforePropertyCreate(anyObject(), anyObject(), anyObject());
        failOnMethodCall();
        interceptor.afterPropertyCreate(anyObject(), anyObject(), anyObject());
        failOnMethodCall();
        interceptor.beforePropertyDelete(anyObject(), anyObject(), anyObject());
        failOnMethodCall();
        interceptor.afterPropertyDelete(anyObject(), anyObject(), anyObject());
        failOnMethodCall();

        return interceptor;
    }

    private void failOnMethodCall() {
        expectLastCall().andThrow(new AssertionFailedError("Interceptor should not get called on trash")).anyTimes();
    }
}