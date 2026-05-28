/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class COCRule
extends BaseBioBankRule {
    public static final int HINT_SAME_RC = 0;
    public static final int HINT_SAME_STUDY = 1;
    public static final int HINT_UNKNOWN = 2;

    public COCRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    @Override
    public String getRuleId() {
        return "COCRule";
    }

    public String processRule(List<String> samplefamilies, int hint, boolean forceUpdate) throws SapphireException {
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        String returnValue = "";
        if (samplefamilies.size() > 0) {
            if (hint == 0) {
                String samplefamilyid = samplefamilies.get(0);
                String restclassid = this.getColumnValue("LV_SampleFamily", samplefamilyid, "restrictclassid");
                if (StringUtil.getLen(restclassid) > 0L) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_SampleFamily");
                    props.setProperty("keyid1", OpalUtil.toDelimitedString(samplefamilies, ";"));
                    props.setProperty("cocflag", this.getColumnValue("LV_RestClass", restclassid, "cocflag", "N"));
                    props.setProperty("restrictionsflag", this.getColumnValue("LV_RestClass", restclassid, "restrictionsflag", "N"));
                    props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                }
            } else if (hint == 1) {
                String samplefamilyid = samplefamilies.get(0);
                String studyid = this.getColumnValue("LV_SampleFamily", samplefamilyid, "sstudyid");
                if (StringUtil.getLen(studyid) > 0L) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_SampleFamily");
                    props.setProperty("keyid1", OpalUtil.toDelimitedString(samplefamilies, ";"));
                    props.setProperty("cocflag", this.getColumnValue("Study", studyid, "conservativecocflag", "N"));
                    props.setProperty("restrictionsflag", this.getColumnValue("Study", studyid, "conservativerestrictionsflag", "N"));
                    props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                }
            } else if (hint == 2) {
                HashSet<String> set = new HashSet<String>();
                set.addAll(samplefamilies);
                DataSet ds = new DataSet();
                ds.addColumn("samplefamilyid", 0);
                ds.addColumn("cocflag", 0);
                ds.addColumn("restrictionsflag", 0);
                for (String samplefamilyid : set) {
                    int row;
                    if (samplefamilyid == null || samplefamilyid.length() <= 0) continue;
                    String restclassid = this.getColumnValue("LV_SampleFamily", samplefamilyid, "restrictclassid");
                    if (StringUtil.getLen(restclassid) > 0L) {
                        int row2 = ds.addRow();
                        ds.setValue(row2, "samplefamilyid", samplefamilyid);
                        ds.setValue(row2, "cocflag", this.getColumnValue("LV_RestClass", restclassid, "cocflag", "N"));
                        ds.setValue(row2, "restrictionsflag", this.getColumnValue("LV_RestClass", restclassid, "restrictionsflag", "N"));
                        continue;
                    }
                    String studyid = this.getColumnValue("LV_SampleFamily", samplefamilyid, "sstudyid");
                    if (StringUtil.getLen(studyid) > 0L) {
                        row = ds.addRow();
                        ds.setValue(row, "samplefamilyid", samplefamilyid);
                        ds.setValue(row, "cocflag", this.getColumnValue("Study", studyid, "conservativecocflag", "N"));
                        ds.setValue(row, "restrictionsflag", this.getColumnValue("Study", studyid, "conservativerestrictionsflag", "N"));
                        continue;
                    }
                    row = ds.addRow();
                    ds.setValue(row, "samplefamilyid", samplefamilyid);
                    ds.setValue(row, "cocflag", "Y");
                    ds.setValue(row, "restrictionsflag", "Y");
                }
                if (ds.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_SampleFamily");
                    props.setProperty("keyid1", ds.getColumnValues("samplefamilyid", ";"));
                    props.setProperty("cocflag", ds.getColumnValues("cocflag", ";"));
                    props.setProperty("restrictionsflag", ds.getColumnValues("restrictionsflag", ";"));
                    props.setProperty("propsmatch", "Y");
                    props.setProperty("__sdcruleconfirm", forceUpdate ? "Y" : "N");
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                }
            }
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
        return returnValue;
    }
}

