/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdidata;

import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;

public class DeleteDataSet
extends BaseSDIDataAction
implements sapphire.action.DeleteDataSet {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean applylock = properties.getProperty("applylock").equals("Y");
        String rsetid = "";
        try {
            rsetid = applylock ? this.getAllDSRSet(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), applylock) : BaseSDIDataAction.createRSet(properties.getProperty("sdcid"), properties.getProperty("keyid1"), properties.getProperty("keyid2"), properties.getProperty("keyid3"), this.database, this.connectionInfo, applylock);
            this.deleteSDIData(properties, rsetid, true, true, true, true, "sdidata");
        }
        finally {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }
}

