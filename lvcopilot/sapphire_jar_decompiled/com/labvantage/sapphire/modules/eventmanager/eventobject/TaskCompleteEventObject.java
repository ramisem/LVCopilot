/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.BaseEventObject;
import com.labvantage.sapphire.modules.eventmanager.eventobject.SDIEventItem;
import sapphire.util.SDIList;

public class TaskCompleteEventObject
extends BaseEventObject {
    public static final String EVENT_COMPLETETASK = "CompleteTask";

    public TaskCompleteEventObject(String taskdefid, String taskdefversionid, String taskdefvariantid, String outputid, String taskexecid, SDIList taskExecItems) {
        super(EVENT_COMPLETETASK, new Object[]{"", "", "", taskdefid, taskdefversionid, taskdefvariantid, outputid, taskexecid, taskExecItems});
    }

    public String getTaskdefid() {
        return (String)this.getEventData()[3];
    }

    public String getTaskdefversionid() {
        return (String)this.getEventData()[4];
    }

    public String getTaskdefvariantid() {
        return (String)this.getEventData()[5];
    }

    public String getOutputid() {
        return (String)this.getEventData()[6];
    }

    public String getTaskexecid() {
        return (String)this.getEventData()[7];
    }

    public SDIList getTaskExecItems() {
        return (SDIList)this.getEventData()[8];
    }

    @Override
    public int addEventItems() {
        SDIList taskExecItems = this.getTaskExecItems();
        for (int i = 0; i < taskExecItems.size(); ++i) {
            this.addEventItem(new SDIEventItem(taskExecItems.getSdcid(), taskExecItems.getKeyid1(i), taskExecItems.getKeyid2(i), taskExecItems.getKeyid3(i)));
        }
        this.setEventItemType(1);
        return taskExecItems.size();
    }
}

