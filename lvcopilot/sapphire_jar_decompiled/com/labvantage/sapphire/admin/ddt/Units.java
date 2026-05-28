/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.util.UnitsUtil;
import java.util.Arrays;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Units
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 59969 $";

    @Override
    public void postAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.resetCache(actionProps.getProperty("unitsid", actionProps.getProperty("keyid1")));
    }

    @Override
    public void postEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.resetCache(actionProps.getProperty("unitsid", actionProps.getProperty("keyid1")));
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) {
        this.resetCache(actionProps.getProperty("unitsid", actionProps.getProperty("keyid1")));
    }

    private void resetCache(String unitsIdList) {
        HashSet<String> set = new HashSet<String>(Arrays.asList(StringUtil.split(unitsIdList, ";")));
        for (String unitsid : set) {
            UnitsUtil.updateUnitConversationCache(this.getDatabaseid(), this.getQueryProcessor(), unitsid);
            AutomationService.broadcastServerCommand(this.connectionInfo.getDatabaseId(), "UpdateUnitConversionCache", unitsid);
        }
    }
}

