/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager.eventobject;

import com.labvantage.sapphire.modules.eventmanager.eventobject.TaskCompleteEventObject;
import sapphire.util.SDIList;

public class WorkflowCompleteEventObject
extends TaskCompleteEventObject {
    public static final String EVENT_COMPLETEWORKFLOW = "CompleteWorkflow";

    public WorkflowCompleteEventObject(String workflowdefid, String workflowdefversionid, String workflowdefvariantid, String taskdefid, String taskdefversionid, String taskdefvariantid, String outputid, String taskexecid, SDIList taskExecItems) {
        super(taskdefid, taskdefversionid, taskdefvariantid, outputid, taskexecid, taskExecItems);
        this.setEventid(EVENT_COMPLETEWORKFLOW);
        this.setEventData(0, workflowdefid);
        this.setEventData(1, workflowdefversionid);
        this.setEventData(2, workflowdefvariantid);
    }

    public String getWorkflowdefid() {
        return (String)this.getEventData()[0];
    }

    public String getWorkflowdefversionid() {
        return (String)this.getEventData()[1];
    }

    public String getWorkflowdefvariantid() {
        return (String)this.getEventData()[2];
    }
}

