/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.stability;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.stability.task.GridTask;
import com.labvantage.sapphire.stability.task.GridTaskStatus;
import com.labvantage.sapphire.stability.task.HasDetails;
import com.labvantage.sapphire.stability.task.PullAmount;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;

public class TaskTypeList
extends ArrayList {
    private Map typeStore = new HashMap();

    public void addTaskType(String propertyTreeid, String objectName) throws Exception {
        TaskType task = new TaskType();
        GridTask gridTask = null;
        try {
            gridTask = (GridTask)Class.forName(objectName).newInstance();
            task.instanceGridTask = gridTask;
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (task.instanceGridTask != null) {
            task.objectName = objectName;
            task.propertyTreeid = propertyTreeid;
            try {
                task.instancePullAmount = (PullAmount)((Object)task.instanceGridTask);
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                task.instanceHasDetails = (HasDetails)((Object)task.instanceGridTask);
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                task.instanceGridTaskStatus = (GridTaskStatus)((Object)task.instanceGridTask);
            }
            catch (Exception exception) {
                // empty catch block
            }
            task.propertyTreeid = propertyTreeid;
            this.typeStore.put(propertyTreeid, task);
            this.add(propertyTreeid);
        }
    }

    public void initTask() throws Exception {
        for (String propertyTreeid : this) {
            TaskType task = this.getTaskType(propertyTreeid);
            GridTask gridTask = null;
            try {
                gridTask = (GridTask)Class.forName(task.objectName).newInstance();
                task.instanceGridTask = gridTask;
            }
            catch (Exception e) {
                Trace.logError("Error creating task for :" + task.objectName + ": " + e.getMessage());
            }
            if (task.instanceGridTask == null) continue;
            try {
                task.instancePullAmount = (PullAmount)((Object)task.instanceGridTask);
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                task.instanceHasDetails = (HasDetails)((Object)task.instanceGridTask);
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                task.instanceGridTaskStatus = (GridTaskStatus)((Object)task.instanceGridTask);
            }
            catch (Exception exception) {}
        }
    }

    public boolean isInstanceGridTaskNull() {
        for (String propertyTreeid : this) {
            try {
                TaskType task = this.getTaskType(propertyTreeid);
                if (task != null && task.instanceGridTask != null) continue;
                return true;
            }
            catch (Exception e) {
                return true;
            }
        }
        return false;
    }

    public void setShow(String propertyTreeid, boolean show) throws SapphireException {
        this.getTaskType(propertyTreeid).show = show;
    }

    public void setExcluded(String propertyTreeid, boolean excluded) throws SapphireException {
        this.getTaskType(propertyTreeid).excluded = excluded;
    }

    public void setShowTitle(String propertyTreeid, boolean showTitle) throws SapphireException {
        this.getTaskType(propertyTreeid).showTitle = showTitle;
    }

    public void setDetailLevel(String propertyTreeid, String detailLevel) throws SapphireException {
        TaskType tasktype = this.getTaskType(propertyTreeid);
        if (detailLevel == null || detailLevel.length() == 0) {
            tasktype.detailLevel = tasktype.instanceGridTask.getDetailLevels()[0];
        } else {
            String[] levels = tasktype.instanceGridTask.getDetailLevels();
            if (Arrays.asList(levels).contains(detailLevel)) {
                tasktype.detailLevel = detailLevel;
            } else {
                tasktype.detailLevel = tasktype.instanceGridTask.getDetailLevels()[0];
            }
        }
    }

    public String getObjectName(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).objectName;
    }

    public GridTask getGridTask(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).instanceGridTask;
    }

    public GridTaskStatus getGridTaskStatus(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).instanceGridTaskStatus;
    }

    public PullAmount getPullAmount(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).instancePullAmount;
    }

    public HasDetails getHasDetails(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).instanceHasDetails;
    }

    public boolean isPullAmount(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).instancePullAmount != null;
    }

    public boolean getShow(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).show;
    }

    public boolean isExcluded(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).excluded;
    }

    public boolean getShowTitle(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).showTitle;
    }

    public String getDetailLevel(String propertyTreeid) throws SapphireException {
        return this.getTaskType(propertyTreeid).detailLevel;
    }

    private TaskType getTaskType(String propertyTreeid) throws SapphireException {
        TaskType taskType = (TaskType)this.typeStore.get(propertyTreeid);
        if (taskType == null) {
            throw new SapphireException("Unrecognized tasktype: " + propertyTreeid);
        }
        return taskType;
    }

    class TaskType
    implements Serializable {
        private String objectName;
        private transient GridTask instanceGridTask;
        private transient GridTaskStatus instanceGridTaskStatus = null;
        private transient PullAmount instancePullAmount = null;
        private transient HasDetails instanceHasDetails = null;
        private boolean excluded = false;
        private boolean show = true;
        private boolean showTitle = true;
        private String detailLevel = "";
        public String propertyTreeid;

        TaskType() {
        }
    }
}

