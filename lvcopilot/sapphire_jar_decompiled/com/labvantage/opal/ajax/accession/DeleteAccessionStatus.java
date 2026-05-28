/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.ajax.accession;

import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class DeleteAccessionStatus
extends BaseAction {
    @Override
    public void processAction(PropertyList actionProps) {
        String accessionid = actionProps.getProperty("accessionid");
        if (accessionid.length() > 0) {
            try {
                this.database.executePreparedUpdate("DELETE FROM PROFILEPROPERTY WHERE PROFILEID = 'System' AND SYSUSERID = ? AND PROPERTYID = ?", new Object[]{this.getConnectionProcessor().getSapphireConnection().getSysuserId(), "ACTIONPROGRESS-" + accessionid});
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        }
    }
}

