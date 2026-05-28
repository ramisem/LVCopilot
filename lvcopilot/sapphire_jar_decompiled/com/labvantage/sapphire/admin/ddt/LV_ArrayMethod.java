/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.sdidata.DataEntryLimitsUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_ArrayMethod
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$";

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits("arraymethodparamitem", sdiData, actionProps);
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataEntryLimitsUtil limitsUtil = new DataEntryLimitsUtil(this.getSDCProcessor(), this.getTranslationProcessor(), this.database, this.logger, this.connectionInfo);
        limitsUtil.manipulateLimits("arraymethodparamitem", sdiData, actionProps);
    }
}

