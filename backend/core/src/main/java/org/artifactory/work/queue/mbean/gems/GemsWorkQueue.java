package org.artifactory.work.queue.mbean.gems;

import org.artifactory.work.queue.mbean.WorkQueueMBean;

/**
 * @author barh
 * Gems compact index related job queue.
 */
public class GemsWorkQueue implements GemsWorkQueueMBean {

    private WorkQueueMBean workQueueMBean;

    public GemsWorkQueue(WorkQueueMBean workQueueMBean) {
        this.workQueueMBean = workQueueMBean;
    }

    @Override
    public int getQueueSize() {
        return workQueueMBean.getQueueSize();
    }

    @Override
    public int getNumberOfWorkers() {
        return workQueueMBean.getNumberOfWorkers();
    }

    @Override
    public int getMaxNumberOfWorkers() {
        return workQueueMBean.getMaxNumberOfWorkers();
    }

    @Override
    public String getName() {
        return workQueueMBean.getName();
    }
}
