/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class CancelTaskExec
extends BaseAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        WorkflowManager.cancelTask(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, properties.getProperty("taskexecid"), null, null, properties.getProperty("auditactivity"), properties.getProperty("auditreason"), properties.getProperty("auditsignedflag").equals("Y"), properties.getProperty("delete", "N").equals("Y"), properties.getProperty("canceloption", "Y"));
    }
}

