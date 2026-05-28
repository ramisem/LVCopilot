/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.admin.ddt.RuleUtil;
import com.labvantage.sapphire.admin.ddt.rules.HipaaRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.StringHolder;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import com.labvantage.sapphire.util.format.DateFormatter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class LV_Subject
extends BaseSDCRules {
    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.logger.info("Begin");
        DataSet primary = sdiData.getDataset("primary");
        boolean forceupdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        if (this.connectionInfo.hasModule("SMS")) {
            this.checkHipaaRule(primary, forceupdate);
        }
    }

    private void checkHipaaRule(DataSet primary, boolean forceUpdate) {
        HipaaRule rule = new HipaaRule(this.database, this.connectionInfo);
        ArrayList<String> subjects = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "birthdt")) continue;
            String subjectid = primary.getString(i, "s_subjectid");
            subjects.add(subjectid);
        }
        try {
            rule.processRule(subjects, forceUpdate);
        }
        catch (SapphireException e) {
            this.setError(rule.getClass().getName(), "VALIDATION", "The study is Hipaa so day/month information cannot be stored.");
        }
    }

    public static ArrayList<String> getStudyList(DBAccess database, String subjectid) throws SapphireException {
        ArrayList<String> studies = new ArrayList<String>();
        String sql = "SELECT distinct sstudyid FROM s_samplefamily WHERE subjectid=?";
        database.createPreparedResultSet(sql, new Object[]{subjectid});
        while (database.getNext()) {
            studies.add(database.getString("sstudyid"));
        }
        database.closeResultSet();
        return studies;
    }

    public static boolean hasDayMonth(DBAccess database, String subjectid) throws SapphireException {
        boolean hasDayMonth = false;
        Timestamp birthdt = null;
        GregorianCalendar date = new GregorianCalendar();
        String sql = "SELECT birthdt FROM s_subject WHERE s_subjectid=?";
        database.createPreparedResultSet(sql, new Object[]{subjectid});
        if (database.getNext() && (birthdt = database.getTimestamp("birthdt")) != null) {
            date.setTimeInMillis(birthdt.getTime());
            if (date.get(5) != 1 || date.get(2) != 0) {
                hasDayMonth = true;
            }
        }
        database.closeResultSet();
        return hasDayMonth;
    }

    public static boolean hasDayMonthBlank(DBAccess database, String subjectid) throws SapphireException {
        boolean hasDayMonthBlank = false;
        Timestamp birthdt = null;
        String sql = "SELECT birthdt FROM s_subject WHERE s_subjectid=?";
        database.createPreparedResultSet(sql, new Object[]{subjectid});
        if (database.getNext() && (birthdt = database.getTimestamp("birthdt")) == null) {
            hasDayMonthBlank = true;
        }
        database.closeResultSet();
        return hasDayMonthBlank;
    }

    public static void clearDayMonth(ConnectionInfo connectionInfo, List subjectlist, boolean forceUpdate, DAMProcessor dmp) throws ActionException {
        ActionProcessor ap = new ActionProcessor(connectionInfo.getConnectionId());
        QueryProcessor qp = new QueryProcessor(connectionInfo.getConnectionId());
        DataSet ds = null;
        Calendar date = null;
        StringBuffer datelist = new StringBuffer();
        StringHolder rsetholder = new StringHolder();
        dmp.createRSet("LV_Subject", RuleUtil.getStringList(subjectlist, ";"), null, null, rsetholder);
        String rsetid = rsetholder.value;
        StringBuffer sqlStmt = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlStmt.append("SELECT s.s_subjectid, s.birthdt FROM s_subject s, rsetitems r ").append(" WHERE r.sdcid = 'LV_Subject' AND r.keyid1 = s.s_subjectid and r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" order by s.s_subjectid");
        ds = qp.getPreparedSqlDataSet(sqlStmt.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                date = ds.getCalendar(i, "birthdt");
                if (date == null) {
                    date = Calendar.getInstance();
                }
                date.set(5, 1);
                date.set(2, 0);
                datelist.append(DateFormatter.formatDateTime(date)).append(";");
            }
        }
        datelist.setLength(datelist.length() - 1);
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", "LV_Subject");
        props.put("keyid1", ds.getColumnValues("s_subjectid", ";"));
        props.put("birthdt", datelist.toString());
        props.put("propsmatch", "Y");
        props.put("__sdcruleconfirm", forceUpdate ? "Y" : "N");
        ap.processAction("EditSDI", "1", props);
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        if (BusinessRulesUtil.isSubjecttDependentSampleFamilyExists(rsetid, this.database)) {
            this.throwError("DependentSample", "VALIDATION", this.getTranslationProcessor().translate("Some LV_Subject(s) are found to be associated with samplefamily(s)!"));
        }
    }
}

