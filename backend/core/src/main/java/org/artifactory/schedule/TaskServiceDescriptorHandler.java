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

package org.artifactory.schedule;

import org.artifactory.descriptor.TaskDescriptor;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

/**
 * User: freds
 * Date: 7/10/11
 * Time: 5:12 PM
 */
public interface TaskServiceDescriptorHandler<T extends TaskDescriptor> {
    String jobName();

    List<T> getNewDescriptors();

    List<T> getOldDescriptors();

    /**
     * @return A predicate that accepts all the tasks of types handled by this descriptor handler. This is used to
     * reschedule tasks by the task manager.
     */
    Predicate<Task> getAllPredicate();

    Predicate<Task> getPredicate(@Nonnull T descriptor);

    void activate(@Nonnull T descriptor, boolean manual);

    T findOldFromNew(@Nonnull T newDescriptor);
}
