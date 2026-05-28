/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_SDMSCollector
extends BaseSDCRules {
    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if (!actionProps.getProperty("skipconfighash").equals("Y")) {
            String[] cols = new String[]{"internalflag", "storagepathlocal", "storagemodeflag", "corepoolsize", "mindiskspace"};
            boolean triggerRebootCheck = false;
            for (int i = 0; i < cols.length; ++i) {
                triggerRebootCheck |= actionProps.containsKey(cols[i]);
            }
            if (triggerRebootCheck) {
                DataSet primary = sdiData.getDataset("primary");
                PropertyList checkRebootProps = new PropertyList();
                checkRebootProps.setProperty("collectorid", primary.getColumnValues("sdmscollectorid", ";"));
                this.getActionProcessor().processAction("CheckSDMSRebootFlag", "1", checkRebootProps);
            }
        }
    }
}

