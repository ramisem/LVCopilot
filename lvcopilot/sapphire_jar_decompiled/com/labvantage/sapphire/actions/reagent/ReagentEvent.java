/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.reagent;

import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ReagentEvent
extends BaseAction
implements sapphire.action.ReagentEvent {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String eventType = properties.getProperty("eventtype");
        String keyId1 = properties.getProperty("keyid1");
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("source", "System");
        props.put("description", "Event Notification:" + eventType);
        props.put("role", "ReagentManager");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select reagentlotid, contentflag from reagentlot where reagentlotid = " + safeSQL.addVar(keyId1);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            String contentflag = ds.getValue(0, "contentflag", "R");
            String body = "";
            if ("R".equals(contentflag)) {
                if ("ExpiryWarningNotification".equalsIgnoreCase(eventType)) {
                    String expiryDate = properties.getProperty("expirydate");
                    body = "Consumable Lot " + keyId1 + " will expire on " + expiryDate;
                } else if ("ExpiryReorderNotification".equalsIgnoreCase(eventType)) {
                    String reorderDate = properties.getProperty("reorderdate");
                    body = "Consumable Lot  " + keyId1 + " reorder date is " + reorderDate;
                } else if ("ExpiryNotification".equalsIgnoreCase(eventType)) {
                    String expiryDate = properties.getProperty("expirydate");
                    String totalQty = properties.getProperty("totalquantity");
                    body = "Consumable Lot " + keyId1 + " has expired on " + expiryDate;
                    if (totalQty.length() > 0) {
                        body = body + " and the amount of consumable remaining is " + totalQty;
                    }
                } else if ("ReorderNotification".equalsIgnoreCase(eventType)) {
                    String totalQty = properties.getProperty("totalquantity");
                    body = "Consumable Lot " + keyId1 + " is below the threshold quantity";
                    if (totalQty.length() > 0) {
                        body = body + " and the amount of consumable remaining is " + totalQty;
                    }
                } else {
                    body = "Received the following event:" + eventType;
                }
            } else if ("K".equals(contentflag)) {
                if ("ExpiryWarningNotification".equalsIgnoreCase(eventType)) {
                    String expiryDate = properties.getProperty("expirydate");
                    body = "Kit Lot " + keyId1 + " will expire on " + expiryDate;
                } else if ("ExpiryReorderNotification".equalsIgnoreCase(eventType)) {
                    String reorderDate = properties.getProperty("reorderdate");
                    body = "Kit Lot  " + keyId1 + " reorder date is " + reorderDate;
                } else if ("ExpiryNotification".equalsIgnoreCase(eventType)) {
                    String expiryDate = properties.getProperty("expirydate");
                    String totalQty = properties.getProperty("totalquantity");
                    body = "Kit Lot " + keyId1 + " has expired on " + expiryDate;
                    if (totalQty.length() > 0) {
                        body = body + " and the amount of Kit remaining is " + totalQty;
                    }
                } else if ("ReorderNotification".equalsIgnoreCase(eventType)) {
                    String totalQty = properties.getProperty("totalquantity");
                    body = "Kit Lot " + keyId1 + " is below the threshold quantity";
                    if (totalQty.length() > 0) {
                        body = body + " and the amount of kit remaining is " + totalQty;
                    }
                } else {
                    body = "Received the following event:" + eventType;
                }
            }
            props.put("body", body);
            props.put("actionid", "SendBulletin");
            this.getActionProcessor().processAction("AddToDoListEntry", "1", props);
        }
    }
}

