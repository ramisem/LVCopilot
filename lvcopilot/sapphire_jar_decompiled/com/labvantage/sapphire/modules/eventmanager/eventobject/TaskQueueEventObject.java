/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.EventItem;
import sapphire.util.DataSet;

public class TaskQueueEventObject
extends BaseEventObject {
    public static final String EVENT_TASKQUEUECHANGED = "TaskQueueChanged";
    public static final String MODE_ADD = "add";
    public static final String MODE_UPDATE = "update";
    public static final String MODE_DELETE = "delete";

    public TaskQueueEventObject(DataSet taskqueueitems, String mode) {
        super(EVENT_TASKQUEUECHANGED, new Object[]{taskqueueitems, mode});
    }

    @Override
    public int addEventItems() {
        DataSet taskqueueitems = (DataSet)this.getEventData()[0];
        for (int i = 0; i < taskqueueitems.size(); ++i) {
            this.addEventItem(new EventItem(taskqueueitems.getValue(i, "taskqueueid")));
        }
        return taskqueueitems.size();
    }

    public String getTaskdefid() {
        return ((DataSet)this.getEventData()[0]).getColumnValues("taskdefid", ";");
    }

    public String getTaskdefid(int index) {
        return ((DataSet)this.getEventData()[0]).getValue(index, "taskdefid");
    }

    public String getTaskdefversionid() {
        return ((DataSet)this.getEventData()[0]).getColumnValues("taskdefversionid", ";");
    }

    public String getTaskdefversionid(int index) {
        return ((DataSet)this.getEventData()[0]).getValue(index, "taskdefversionid");
    }

    public String getTaskdefvariantid() {
        return ((DataSet)this.getEventData()[0]).getColumnValues("taskdefvariantid", ";");
    }

    public String getTaskdefvariantid(int index) {
        return ((DataSet)this.getEventData()[0]).getValue(index, "taskdefvariantid");
    }

    public String getWorkflowdefid() {
        return ((DataSet)this.getEventData()[0]).getColumnValues("workflowdefid", ";");
    }

    public String getWorkflowdefid(int index) {
        return ((DataSet)this.getEventData()[0]).getValue(index, "workflowdefid");
    }

    public String getWorkflowdefversionid() {
        return ((DataSet)this.getEventData()[0]).getColumnValues("workflowdefversionid", ";");
    }

    public String getWorkflowdefversionid(int index) {
        return ((DataSet)this.getEventData()[0]).getValue(index, "workflowdefversionid");
    }

    public String getWorkflowdefvariantid() {
        return ((DataSet)this.getEventData()[0]).getColumnValues("workflowdefvariantid", ";");
    }

    public String getWorkflowdefvariantid(int index) {
        return ((DataSet)this.getEventData()[0]).getValue(index, "workflowdefvariantid");
    }

    public String getMode() {
        return (String)this.getEventData()[1];
    }
}

