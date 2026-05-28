/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.sdidata.DataEntryLimitsUtil;
import java.util.Collection;
import sapphire.SapphireException;
import sapphire.action.AddArrayContent;
import sapphire.action.BaseSDCRules;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_ArrayZone
extends BaseSDCRules
implements AddArrayContent {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    static final String COL_APPARENTDATATYPE = "_apparentdatatype";

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits("arrayparamitem", sdiData, actionProps);
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits("arrayparamitem", sdiData, actionProps);
    }

    private String getDelimitedString(Collection data, String delimiter, String wrapper) {
        String delimitedstr = "";
        for (Object value : data) {
            delimitedstr = delimitedstr + delimiter + wrapper + value + wrapper;
        }
        return delimitedstr.substring(1);
    }
}

