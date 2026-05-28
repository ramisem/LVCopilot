/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eventmanager;

import com.labvantage.sapphire.modules.eventmanager.NotifyManager;
import com.labvantage.sapphire.modules.eventmanager.gwt.shared.NotificationConstants;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.servlet.NotificationController;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class Notify
extends BaseAction
implements NotificationConstants {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String type = properties.getProperty("type");
        NotificationController.logDebug("NOTIFY ACTION: " + (type.equals("E") ? properties.getProperty("eventtypeid") + " event fired for " + properties.getProperty("elementtype") + " (" + properties.getProperty("elementid") + ")" : NotifyManager.getNotifyTypeText(type) + " notification"));
        properties.setProperty("__notificationid", String.valueOf(this.getSequenceProcessor().getSequence("Notification", "mainkey")));
        properties.setProperty("__createby", this.connectionInfo.getSysuserId());
        properties.setProperty("__connection", this.connectionInfo.getConnectionId());
        properties.setProperty("__databaseid", this.connectionInfo.getDatabaseId());
        if (properties.getProperty("listenersonly", "N").equalsIgnoreCase("Y")) {
            boolean listenersOut = NotifyManager.notifyListeners(properties);
            properties.setProperty("listenerscontinue", listenersOut ? "Y" : "N");
        } else if (type.equals("A") || type.equals("T") || type.equals("E")) {
            NotificationController.logDebug("NOTIFY ACTION - type = " + type);
            NotifyManager.notify(properties);
            NotificationController.logDebug("NOTIFY ACTION - About to broadcast");
            AutomationService.broadcastServerCommand(this.connectionInfo.getDatabaseId(), "Notification", properties.toXMLString());
        } else {
            NotifyManager.notify(properties);
        }
    }
}

