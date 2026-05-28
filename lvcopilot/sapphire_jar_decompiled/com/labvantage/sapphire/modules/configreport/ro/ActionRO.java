/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.ro;

import com.labvantage.sapphire.services.SapphireConnection;
import sapphire.SapphireException;
import sapphire.ext.BaseSDCRO;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ActionRO
extends BaseSDCRO {
    public static final String SEPARATOR = "|!|";

    public void initialize(SapphireConnection connection) throws SapphireException {
        super.initialize("Action", connection);
    }

    public String getActionId() {
        return this.getKeyid1();
    }

    public String getActionVersionId() {
        String version = this.getKeyid2();
        if (version == null || version.length() == 0 || version.equals("*")) {
            return "1";
        }
        return version;
    }

    public String getActionDesc() {
        return this.getDescription();
    }

    public String getVersionStatus() {
        String colVal = this.getPrimaryValue("versionstatus");
        String versionStatus = "";
        versionStatus = "A".equals(colVal) ? "Active" : "Provisional";
        return versionStatus;
    }

    public String getActionType() {
        String colVal = this.getPrimaryValue("actiontype");
        if ("C".equals(colVal)) {
            return "Core";
        }
        if ("S".equals(colVal)) {
            return "System";
        }
        if ("U".equals(colVal)) {
            return "User";
        }
        return "";
    }

    public String getObjectName() {
        return this.getPrimaryValue("objectname");
    }

    public DataSet getActionProperties() {
        DataSet ds = this.getDataSet("actionproperty");
        DataSet ret = new DataSet();
        ret.setColidCaseSensitive(true);
        ret.addColumn("Property", 0);
        ret.addColumn("Type", 0);
        ret.addColumn("Mode", 0);
        ret.addColumn("Title", 0);
        ret.addColumn("Help", 0);
        ret.addColumn("Default Value", 0);
        ret.addColumnValues("Property", 0, ds.getColumnValues("propertyid", SEPARATOR), SEPARATOR);
        ret.addColumnValues("Type", 0, ds.getColumnValues("propertytype", SEPARATOR), SEPARATOR);
        String[] modePropsVals = StringUtil.split(ds.getColumnValues("propertytypeflag", SEPARATOR), SEPARATOR);
        for (int i = 0; i < modePropsVals.length; ++i) {
            String mode = "";
            mode = "I".equals(modePropsVals[i]) ? "Input" : ("O".equals(modePropsVals[i]) ? "Output" : "Both");
            ret.setString(i, "Mode", mode);
        }
        ret.addColumnValues("Title", 0, ds.getColumnValues("propertytitle", SEPARATOR), SEPARATOR);
        ret.addColumnValues("Help", 0, ds.getColumnValues("propertyhelp", SEPARATOR), SEPARATOR);
        ret.addColumnValues("Default Value", 0, ds.getColumnValues("defaultvalue", SEPARATOR), SEPARATOR);
        return ret;
    }
}

