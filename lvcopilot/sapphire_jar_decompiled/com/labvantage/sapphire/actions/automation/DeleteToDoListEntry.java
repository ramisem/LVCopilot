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

public class DeleteToDoListEntry
extends BaseAction
implements sapphire.action.DeleteToDoListEntry {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        try {
            AutomationService ac = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            ac.deleteToDoListEntry(properties.getProperty("todolistid"));
        }
        catch (ServiceException e) {
            throw new SapphireException("AUTOMATION_SERVICE_FAILED", "Failed to delete todolist entry: " + properties.getProperty("todolistid"), e);
        }
    }
}

