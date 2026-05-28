/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class SetTaskExecIncomplete
extends BaseAction
implements WorkflowManagerConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.database.executePreparedUpdate("UPDATE taskexec SET execstatus = ? WHERE taskexecid = ?", new Object[]{"I", properties.getProperty("taskexecid")});
    }
}

