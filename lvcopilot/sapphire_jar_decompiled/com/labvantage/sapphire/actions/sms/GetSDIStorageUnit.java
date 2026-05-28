/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sms;

import com.labvantage.sapphire.SDI;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class GetSDIStorageUnit
extends BaseAction
implements sapphire.action.GetSDIStorageUnit {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String keyid3;
        String keyid2;
        String keyid1;
        String storageUnit = "";
        String sdcid = properties.getProperty("sdcid");
        SDI sdi = new SDI(sdcid, keyid1 = properties.getProperty("keyid1"), keyid2 = properties.getProperty("keyid2"), keyid3 = properties.getProperty("keyid3"));
        if (sdi.isValid()) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select StorageUnitId from storageunit where linksdcid = ? and linkkeyid1 = ? and (linkkeyid2 = ? or linkkeyid2 is null) and (linkkeyid3 = ? or linkkeyid3 is null)", (Object[])new String[]{sdi.getSdcid(), sdi.getKeyid1(), sdi.getKeyid2(), sdi.getKeyid3()});
            if (ds != null && ds.size() > 0) {
                if (ds.getRowCount() > 1) {
                    throw new SapphireException("INVALID_SDI", "Specified sdi returned multiple StorageUnit.");
                }
                storageUnit = ds.getValue(0, "storageunitid");
            }
        } else {
            throw new SapphireException("INVALID_SDI", "Invalid input sdi: " + sdi);
        }
        properties.setProperty("storageunitid", storageUnit);
    }
}

