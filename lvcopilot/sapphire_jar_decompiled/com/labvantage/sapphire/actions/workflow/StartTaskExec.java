/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.sapphire.modules.workflow.TaskDef;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class StartTaskExec
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        TaskDef taskDef = TaskDef.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), properties.getProperty("taskdefid"), properties.getProperty("taskdefversionid"), properties.getProperty("taskdefvariantid"));
        PropertyList taskexec = new PropertyList();
        taskexec.setProperty("variables", taskDef.getTaskdef().getCollection("variables"));
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "LV_TaskExec");
        actionProps.setProperty("taskexecdesc", taskDef.getTaskdef().getProperty("longtitle", taskDef.getTaskdef().getProperty("shorttitle", taskDef.getTaskdefid())));
        actionProps.setProperty("taskdefid", taskDef.getTaskdefid());
        actionProps.setProperty("taskdefversionid", taskDef.getTaskdefversionid());
        actionProps.setProperty("taskdefvariantid", taskDef.getTaskdefvariantid());
        actionProps.setProperty("taskexec", taskexec.toXMLString());
        actionProps.setProperty("startdt", "n");
        actionProps.setProperty("execstatus", "A");
        actionProps.setProperty("workflowexecid", properties.getProperty("workflowexecid"));
        actionProps.setProperty("taskdefitemid", properties.getProperty("taskdefitemid"));
        actionProps.setProperty("trainingoverriddenflag", properties.getProperty("trainingoverriddenflag"));
        if (!this.connectionInfo.getSysuserId().equals("(system)")) {
            actionProps.setProperty("assignedanalyst", this.connectionInfo.getSysuserId());
        }
        actionProps.setProperty("connectionid", this.connectionInfo.getConnectionId());
        this.getActionProcessor().processAction("AddSDI", "1", actionProps);
        properties.setProperty("taskexecid", actionProps.getProperty("newkeyid1"));
    }
}

