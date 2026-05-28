/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.ajax.util;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SetActionProgressStatus
extends BaseAction {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList actionProps) throws SapphireException {
        String actionprogressid = actionProps.getProperty("actionprogressid").trim();
        String message = actionProps.getProperty("message").trim();
        if (!actionprogressid.isEmpty()) {
            SetActionProgressStatus setActionProgressStatus = this;
            synchronized (setActionProgressStatus) {
                try {
                    String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                    String propertyid = "ACTIONPROGRESS-" + actionprogressid;
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT PROPERTYVALUE FROM PROFILEPROPERTY WHERE PROFILEID = ? and SYSUSERID = ? and PROPERTYID = ?", (Object[])new String[]{"System", sysuserid, propertyid});
                    if (ds != null && ds.size() > 0) {
                        String propertyvalue = ds.getString(0, "propertyvalue", "");
                        if (!propertyvalue.startsWith("||COMPLETED||") && !propertyvalue.startsWith("||ERROR||")) {
                            this.database.executePreparedUpdate("UPDATE PROFILEPROPERTY SET PROPERTYVALUE = ? WHERE PROFILEID = ? AND PROPERTYID = ? AND SYSUSERID = ?", new String[]{message, "System", propertyid, sysuserid});
                        }
                    } else {
                        this.database.executePreparedUpdate("INSERT INTO PROFILEPROPERTY (PROFILEID, PROPERTYID, SYSUSERID, PROPERTYVALUE) VALUES ('System', ?, ?, ?)", new Object[]{propertyid, sysuserid, message});
                    }
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

