/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class SDIWorkItem
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void postEditAttribute(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet dsAttribute = sdiData.getDataset("attribute");
        DataSet dsUpdate = new DataSet();
        for (int i = 0; i < dsAttribute.getRowCount(); ++i) {
            if (!this.hasSDIAttributeValueChanged(dsAttribute, i, "mandatoryflag") && !this.hasSDIAttributeValueChanged(dsAttribute, i, "datevalue") && !this.hasSDIAttributeValueChanged(dsAttribute, i, "textvalue") && !this.hasSDIAttributeValueChanged(dsAttribute, i, "clobvalue") && !this.hasPrimaryValueChanged(dsAttribute, i, "numericvalue")) continue;
            dsUpdate.copyRow(dsAttribute, i, 1);
        }
        if (dsUpdate.getRowCount() > 0) {
            OpalUtil.updateAttributeSourceStatus("SDIWorkItem", dsUpdate, this.database, this.getActionProcessor());
        }
    }

    @Override
    public void postAddAttribute(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet dsAttribute = sdiData.getDataset("attribute");
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("mandatoryflag", "Y");
        DataSet dsMandatoryAttribute = dsAttribute.getFilteredDataSet(filter);
        if (dsMandatoryAttribute.getRowCount() > 0) {
            OpalUtil.updateAttributeSourceStatus("SDIWorkItem", dsMandatoryAttribute, this.database, this.getActionProcessor());
        }
    }

    @Override
    public void postDeleteAttribute(String rsetid, PropertyList actionProps) throws SapphireException {
        DataSet dsAttribute = this.getQueryProcessor().getPreparedSqlDataSet("select s.* from sdiattribute s, rsetitems r  where s.sdcid = r.sdcid and s.keyid1 = r.keyid1 and r.rsetid = ? and r.sdcid = 'SDIWorkItem'", new Object[]{rsetid});
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("mandatoryflag", "Y");
        DataSet dsMandatoryAttribute = dsAttribute.getFilteredDataSet(filter);
        if (dsMandatoryAttribute.getRowCount() > 0) {
            OpalUtil.updateAttributeSourceStatus("SDIWorkItem", dsMandatoryAttribute, this.database, this.getActionProcessor());
        }
    }

    @Override
    public boolean requiresBeforeEditSDIAttributeImage() {
        return true;
    }
}

