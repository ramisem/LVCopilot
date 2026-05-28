/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class DeleteDataItemLimit
extends BaseSDIDataAction
implements sapphire.action.DeleteDataItemLimit {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean applylock = properties.getProperty("applylock").equals("Y");
        String rsetid = "";
        try {
            rsetid = this.getAllDSRSet(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), applylock);
            this.deleteSDIData(properties, rsetid, false, false, true, false, "sdidataitemlimits");
        }
        finally {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }
}

