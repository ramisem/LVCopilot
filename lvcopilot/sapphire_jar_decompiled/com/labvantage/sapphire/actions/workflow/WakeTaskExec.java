/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.workflow;

import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class WakeTaskExec
extends BaseAction
implements WorkflowManagerConstants {
    public static final String ID = "WakeTaskExec";
    public static final String VERSIONID = "1";
    public static final String PROPERTY_TASKEXECID = "taskexecid";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String taskexecid = properties.getProperty(PROPERTY_TASKEXECID);
        this.database.createPreparedResultSet("SELECT taskdefid, taskdefversionid, taskdefvariantid, execstatus FROM taskexec WHERE taskexecid = ?", new Object[]{taskexecid});
        if (this.database.getNext() && this.database.getValue("execstatus").equals("W")) {
            PropertyList editProps = new PropertyList();
            editProps.setProperty("sdcid", "LV_TaskExec");
            editProps.setProperty("keyid1", taskexecid);
            editProps.setProperty("execstatus", "P");
            this.getActionProcessor().processAction("EditSDI", VERSIONID, editProps);
        }
    }
}

