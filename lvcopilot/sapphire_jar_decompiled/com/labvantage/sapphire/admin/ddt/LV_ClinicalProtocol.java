/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_ClinicalProtocol
extends BaseSDCRules {
    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet cohortds = sdiData.getDataset("s_cpcohort");
        if (OpalUtil.isNotEmpty(cohortds)) {
            for (int i = 0; i < cohortds.size(); ++i) {
                String cohortid = cohortds.getString(i, "s_cpcohortid", "");
                cohortds.setString(i, "s_cpcohortid", cohortid.trim());
            }
        }
    }
}

