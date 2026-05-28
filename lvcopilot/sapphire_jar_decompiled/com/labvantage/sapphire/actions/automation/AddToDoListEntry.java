/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.automation;

import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class AddToDoListEntry
extends BaseAction
implements sapphire.action.AddToDoListEntry {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            String processassysuserid = properties.getProperty("processassysuserid");
            String todolistid = ac.addToDoListEntry(properties.getProperty("requestorid"), properties.getProperty("doactionid", properties.getProperty("actionid")), properties.getProperty("actionversionid"), properties, properties.getProperty("duedate"), properties.getProperty("delete", "Y").substring(0, 1).toUpperCase().equals("Y"), "(system)".equalsIgnoreCase(processassysuserid) ? "" : processassysuserid, properties.getProperty("processname"), properties.getProperty("groupname"), properties.getProperty("grouperrorrule", "S"));
            properties.setProperty("todolistid", todolistid);
        }
        catch (ServiceException e) {
            throw new SapphireException("AUTOMATION_SERVICE_FAILED", "Failed to add new TODOLIST entry", e);
        }
    }
}

