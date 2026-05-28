/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.system;

import com.labvantage.sapphire.pageelements.PropertyHandler;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import sapphire.SapphireException;

public class ToDoListAdminPropertyHandler
extends PropertyHandler {
    @Override
    public void processProperties(HashMap props) throws SapphireException {
        String mode = (String)props.get("dothis");
        String todolistid = (String)props.get("todolistid");
        try {
            AutomationService automation = new AutomationService(this.sapphireConnection);
            if ("delete".equalsIgnoreCase(mode)) {
                automation.deleteToDoListEntry(todolistid);
            } else if ("resend".equalsIgnoreCase(mode)) {
                automation.resendToDoListEntry(todolistid);
            }
        }
        catch (ServiceException e) {
            throw new SapphireException("Failed to " + mode + " todolistid '" + todolistid + "'", e);
        }
    }
}

