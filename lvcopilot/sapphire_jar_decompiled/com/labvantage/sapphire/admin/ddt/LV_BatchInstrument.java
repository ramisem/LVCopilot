/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class LV_BatchInstrument
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String SDCID = "LV_BatchInstrument";
    public static final String TABLEID = "s_batchinstrument";
    public static final String COLUMN_KEYID1 = "s_batchid";
    public static final String COLUMN_KEYID2 = "s_batchinstrumentid";

    @Override
    public void postAddKey(DataSet primary, PropertyList actionProps) {
        if (!"Y".equalsIgnoreCase(actionProps.getProperty("overrideautokey"))) {
            this.generateKeys(primary);
        }
    }

    private void generateKeys(DataSet primary) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String now = sdf.format(cal.getTime());
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String keyid1 = primary.getValue(i, COLUMN_KEYID1, "");
            String keyid2 = primary.getValue(i, COLUMN_KEYID2, "");
            int sequence = this.getSequenceProcessor().getSequence(SDCID, keyid1);
            if (keyid2.length() != 0) continue;
            String id = "BI-" + now + "-" + sequence;
            primary.setString(i, COLUMN_KEYID2, id);
        }
    }
}

