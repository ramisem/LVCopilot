/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.admin.ddt.LV_TaskDef;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TaskInputTimer
extends BaseAction
implements WorkflowManagerConstants {
    public static final String PROPERTY_TASKQUEUEID = "taskqueueid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String taskqueueid = properties.getProperty(PROPERTY_TASKQUEUEID);
        String taskdefid = properties.getProperty("taskdefid");
        String taskdefversionid = properties.getProperty("taskdefversionid");
        String taskdefvariantid = properties.getProperty("taskdefvariantid");
        boolean autoexec = properties.getProperty("autoexec").equals("Y");
        String where = "taskqueueid IN ( '" + StringUtil.replaceAll(taskqueueid, ";", "','") + "' ) AND    queuestatus = '" + (autoexec ? "Atimer" : "Wtimer") + "'";
        if (autoexec) {
            int updates = WorkflowManager.updateTaskQueue(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, "queuestatus = 'A'", where, null, false);
            if (updates > 0) {
                ActionProcessor actionProcessor = this.getActionProcessor();
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("actionid", "ProcessAutoExecTask");
                actionProps.setProperty("actionversionid", "1");
                actionProps.setProperty("duedate", "Y");
                actionProps.setProperty(PROPERTY_TASKQUEUEID, taskqueueid);
                actionProcessor.processAction("AddToDoListEntry", "1", actionProps);
                this.logger.debug(updates + " taskqueue timer events completed for task " + LV_TaskDef.getText(taskdefid, taskdefversionid, taskdefvariantid) + " and sent for auto execution");
            }
        } else {
            int updates = WorkflowManager.updateTaskQueue(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, "queuestatus = 'W'", where, null, true);
            if (updates > 0) {
                this.logger.debug(updates + " taskqueue timer events completed for task " + LV_TaskDef.getText(taskdefid, taskdefversionid, taskdefvariantid));
            }
        }
    }
}

