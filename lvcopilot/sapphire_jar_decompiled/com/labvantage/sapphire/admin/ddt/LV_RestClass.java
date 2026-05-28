/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.admin.ddt.rules.ActiveRCRule;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.admin.ddt.rules.COCRule;
import com.labvantage.sapphire.admin.ddt.rules.ConsCOCRule;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_RestClass
extends BaseSDCRules {
    protected String LABVANTAGE_CVS_ID = "$Revision: 53880 $";
    public static final String SDC = "LV_RestClass";

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (this.connectionInfo.hasModule("SMS")) {
            this.checkConsCOCRule(primary);
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        if (this.connectionInfo.hasModule("SMS")) {
            this.checkConsCOCRule(primary);
            this.checkCOCRule(primary, forceUpdate);
            this.checkActiveRCRule(primary);
        }
    }

    private void checkConsCOCRule(DataSet primary) throws SapphireException {
        ConsCOCRule rule = new ConsCOCRule(this.database, this.connectionInfo);
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "activeflag") && !this.hasPrimaryValueChanged(primary, i, "cocflag") && !this.hasPrimaryValueChanged(primary, i, "restrictionsflag")) continue;
            String studyid = primary.isValidColumn("sstudyid") ? primary.getString(i, "sstudyid") : this.getOldPrimaryValue(primary, i, "sstudyid");
            rule.processRule(studyid);
        }
    }

    private void checkCOCRule(DataSet primary, boolean forceUpdate) {
        COCRule rule = new COCRule(this.database, this.connectionInfo);
        try {
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "cocflag") && !this.hasPrimaryValueChanged(primary, i, "restrictionsflag")) continue;
                String restclassid = primary.getString(i, "s_restrictclassid");
                ArrayList familyList = LV_RestClass.getFamilyList(this.database, restclassid);
                rule.processRule(familyList, 0, forceUpdate);
            }
        }
        catch (SapphireException se) {
            this.setError(rule.getClass().getName(), "VALIDATION", se.getMessage());
        }
    }

    private void checkActiveRCRule(DataSet primary) throws SapphireException {
        if (BaseBioBankRule.isRuleActive("Active RC Rule", this.getConfigurationProcessor())) {
            ActiveRCRule rule = new ActiveRCRule(this.database, this.connectionInfo);
            StringBuffer sql = new StringBuffer();
            for (int i = 0; i < primary.size(); ++i) {
                String restclassid;
                if (!this.hasPrimaryValueChanged(primary, i, "activeflag") || !primary.getValue(i, "activeflag").equals("N") || StringUtil.getLen(restclassid = primary.getString(i, "s_restrictclassid", "")) <= 0L) continue;
                SafeSQL safeSQL = new SafeSQL();
                sql.setLength(0);
                sql.append("SELECT s.s_sampleid, s.storagestatus, s.sstudyid");
                sql.append(" FROM s_sample s, s_samplefamily sf");
                sql.append(" WHERE s.samplefamilyid = sf.s_samplefamilyid");
                sql.append(" AND sf.restrictclassid=").append(safeSQL.addVar(restclassid));
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds == null) continue;
                try {
                    for (int row = 0; row < ds.size(); ++row) {
                        rule.processRule(ds.getValue(row, "s_sampleid"), ds.getValue(row, "storagestatus"), ds.getValue(row, "sstudyid"));
                    }
                    continue;
                }
                catch (SapphireException se) {
                    this.setError(rule.getClass().getName(), "VALIDATION", se.getMessage());
                }
            }
        }
    }

    public static ArrayList getFamilyList(DBAccess database, String restclassid) throws SapphireException {
        ArrayList<String> families = new ArrayList<String>();
        String sql = "SELECT s_samplefamilyid FROM s_samplefamily WHERE restrictclassid=?";
        database.createPreparedResultSet(sql, new Object[]{restclassid});
        while (database.getNext()) {
            families.add(database.getString("s_samplefamilyid"));
        }
        database.closeResultSet();
        return families;
    }

    public static boolean getCOC(DBAccess database, String restclassid) throws SapphireException {
        boolean coc = false;
        String sql = "SELECT cocflag FROM s_restrictclass WHERE s_restrictclassid=?";
        database.createPreparedResultSet(sql, new Object[]{restclassid});
        if (database.getNext()) {
            String s = database.getString("cocflag");
            coc = s != null && s.equals("Y");
        }
        database.closeResultSet();
        return coc;
    }

    public static boolean getDR(DBAccess database, String restclassid) throws SapphireException {
        boolean coc = false;
        String sql = "SELECT restrictionsflag FROM s_restrictclass WHERE s_restrictclassid=?";
        database.createPreparedResultSet(sql, new Object[]{restclassid});
        if (database.getNext()) {
            String s = database.getString("restrictionsflag");
            coc = s != null && s.equals("Y");
        }
        database.closeResultSet();
        return coc;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (BusinessRulesUtil.checkIfSFRefersRC(rsetid, this.database)) {
            this.throwError("SampleFamily Has Restriction Class", "VALIDATION", this.getTranslationProcessor().translate("SampleFamily refers to the selected Restriction Class id. So, it cannot be deleted."));
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

