/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.admin.ddt.rules.ConnectByLoopRule;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class LV_Tissue
extends BaseSDCRules {
    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        ConnectByLoopRule rule = new ConnectByLoopRule(this.database, this.connectionInfo);
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "parenttissueid")) continue;
            rule.processRule("s_tissue", primary.getString(i, "s_tissueid"), primary.getString(i, "parenttissueid"), "s_tissueid", "parenttissueid");
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public void preCMTImport(SDIData sdiData, PropertyList actionProps, boolean isAddSDI) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.getValue(i, "parenttissueid").length() <= 0) continue;
            String fullname = primary.getValue(i, "__fullname");
            String parentfullname = fullname.substring(0, fullname.lastIndexOf(":"));
            String parentdesc = parentfullname.contains(":") ? parentfullname.substring(parentfullname.lastIndexOf(":") + 1) : parentfullname;
            this.database.createPreparedResultSet("SELECT s_tissueid FROM s_tissue WHERE tissuedesc=? AND " + (this.database.isOracle() ? "LV_QUERY." : "dbo.LV_QUERY_") + "GetTissueFullName( s_tissueid )=?", new Object[]{parentdesc, parentfullname});
            String matchingParentid = "";
            if (this.database.getNext()) {
                matchingParentid = this.database.getValue("s_tissueid");
            }
            if (matchingParentid.length() > 0) {
                primary.setValue(i, "parenttissueid", matchingParentid);
                continue;
            }
            primary.setValue(i, "parenttissueid", "");
        }
    }
}

