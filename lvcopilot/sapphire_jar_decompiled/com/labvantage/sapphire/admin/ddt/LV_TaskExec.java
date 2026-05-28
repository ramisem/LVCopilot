/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.modules.workflow.WorkflowManager;
import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.xml.PropertyList;

public class LV_TaskExec
extends BaseSDCRules {
    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        WorkflowManager.updateTaskQueue(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, "fromtaskexecid = NULL", "fromtaskexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{rsetid}, true);
        this.database.executePreparedUpdate("UPDATE taskexecitem SET fromtaskexecid = NULL WHERE fromtaskexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{rsetid});
        this.database.executePreparedUpdate("UPDATE taskexecitem SET totaskexecid = NULL WHERE totaskexecid IN (SELECT keyid1 FROM rsetitems WHERE rsetid = ?)", new Object[]{rsetid});
    }
}

