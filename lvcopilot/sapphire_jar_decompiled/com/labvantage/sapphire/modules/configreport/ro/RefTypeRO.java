/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;

public class RefTypeRO
extends BaseSDCRO {
    public void initialize(SapphireConnection connection) throws SapphireException {
        super.initialize("RefType", connection);
    }

    public String getRefTypeId() {
        return this.getKeyid1();
    }

    public String getRefTypeDesc() {
        return this.getDescription();
    }

    public String getType() {
        String colValue = this.getPrimaryValue("typeflag");
        if ("C".equals(colValue)) {
            return "Core";
        }
        if ("S".equals(colValue)) {
            return "System";
        }
        return "User";
    }

    public DataSet getRefTypeValues() {
        DataSet ds = this.getDataSet("refvalue");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Reference Value", 0);
        ret.addColumn("Description", 0);
        ret.addColumn("Display Value", 0);
        ret.addColumnValues("Reference Value", 0, ds.getColumnValues("refvalueid", ";"), ";");
        ret.addColumnValues("Description", 0, ds.getColumnValues("refvaluedesc", ";"), ";");
        ret.addColumnValues("Display Value", 0, ds.getColumnValues("refdisplayvalue", ";"), ";");
        return ret;
    }

    public DataSet getRefTypeCategories() {
        DataSet ds = this.getCategories();
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Category ID", 0);
        ret.addColumnValues("Category ID", 0, ds.getColumnValues("categoryid", ";"), ";");
        return ret;
    }
}

