/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.ejb.SessionBean
 */
package com.labvantage.sapphire.ejb;

import com.labvantage.sapphire.ejb.BaseManager;
import com.labvantage.sapphire.ejb.InstallManagement;
import com.labvantage.sapphire.ejb.ManagerException;
import com.labvantage.sapphire.services.ActionService;
import java.util.HashMap;
import javax.ejb.SessionBean;
import sapphire.error.ErrorHandler;
import sapphire.xml.PropertyList;

public class InstallManagerBean
extends BaseManager
implements SessionBean,
InstallManagement {
    public InstallManagerBean() {
        this.logName = "InstallManager";
    }

    @Override
    public HashMap processCommand(HashMap commandProps) throws ManagerException {
        String command = (String)commandProps.get("command");
        if (command != null && command.length() > 0) {
            if (command.equalsIgnoreCase("ProcessActionClass")) {
                try {
                    this.startMethod("processActionClass", (String)commandProps.get("connectionid"));
                    this.sapphireConnection.getConnection().setAutoCommit(false);
                    ActionService actionService = new ActionService(this.sapphireConnection);
                    PropertyList actionProps = new PropertyList(commandProps);
                    actionService.processActionClass((String)commandProps.get("actionclass"), actionProps, new ErrorHandler());
                    commandProps.putAll(actionProps);
                    this.sapphireConnection.getConnection().setAutoCommit(true);
                    this.endMethod("processActionClass");
                }
                catch (Exception e) {
                    throw new ManagerException("Failed to process action. Reason: " + e.getMessage(), e);
                }
            }
        } else {
            throw new ManagerException("No command specified");
        }
        return commandProps;
    }
}

