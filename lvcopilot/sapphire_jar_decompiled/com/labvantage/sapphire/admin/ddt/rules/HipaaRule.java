/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.rules.BaseRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.format.DateFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class HipaaRule
extends BaseRule {
    public HipaaRule() {
    }

    public HipaaRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    public String getRuleId() {
        return "HipaaRule";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public void processRule(List subjects, boolean forceUpdate) throws SapphireException {
        start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        if (subjects.size() > 0) {
            subjectds = null;
            sql = new StringBuilder();
            safeSQL = new SafeSQL();
            if (subjects.size() > 999) {
                rsetid = null;
                try {
                    rsetid = this.getDAMProcessor().createRSet("LV_Subject", OpalUtil.toDelimitedString(subjects, ";"), null, null);
                    if (StringUtil.getLen(rsetid) <= 0L) ** GOTO lbl48
                    sql.append("select s.s_subjectid, s.birthdt");
                    sql.append(" from s_subject s, s_samplefamily sf, s_study st");
                    sql.append(" where s.s_subjectid = sf.subjectid");
                    sql.append(" and s.birthdt is not null");
                    sql.append(" and st.s_studyid = sf.sstudyid");
                    sql.append(" and st.hipaaflag = 'Y'");
                    sql.append(" and s.s_subjectid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addIn(rsetid)).append(" )");
                    subjectds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                }
                finally {
                    if (StringUtil.getLen(rsetid) > 0L) {
                        this.getDAMProcessor().clearRSet(rsetid);
                    }
                }
            } else {
                sql.append("select s.s_subjectid, s.birthdt");
                sql.append(" from s_subject s, s_samplefamily sf, s_study st");
                sql.append(" where s.s_subjectid = sf.subjectid");
                sql.append(" and s.birthdt is not null");
                sql.append(" and st.s_studyid = sf.sstudyid");
                sql.append(" and st.hipaaflag = 'Y'");
                sql.append(" and s.s_subjectid in ( ").append(safeSQL.addIn(OpalUtil.toDelimitedString(subjects, "','"))).append(" )");
                subjectds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
lbl48:
            // 3 sources

            this.processHIPAARule(subjectds, forceUpdate);
        }
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    public void processRule(String studyid, boolean forceUpdate) throws SapphireException {
        long start = System.currentTimeMillis();
        Trace.logInfo("START: " + this.getRuleId());
        this.processHIPAARule(this.getQueryProcessor().getPreparedSqlDataSet("select s.s_subjectid, s.birthdt from s_subject s, s_samplefamily sf, s_study st where sf.sstudyid = ? and s.s_subjectid = sf.subjectid and s.birthdt is not null and st.s_studyid = sf.sstudyid and st.hipaaflag = 'Y'", (Object[])new String[]{studyid}), forceUpdate);
        Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private void processHIPAARule(DataSet ds, boolean forceUpdate) throws SapphireException {
        if (ds != null && ds.size() > 0) {
            HashMap<String, Calendar> subjects = new HashMap<String, Calendar>();
            for (int i = 0; i < ds.size(); ++i) {
                Calendar cal = ds.getCalendar(i, "birthdt");
                if (cal.get(5) == 1 && cal.get(2) == 0) continue;
                subjects.put(ds.getString(i, "s_subjectid"), cal);
            }
            if (subjects.size() > 0) {
                if (forceUpdate) {
                    DataSet dsdata = new DataSet();
                    dsdata.addColumn("subjectid", 0);
                    dsdata.addColumn("birthdt", 0);
                    for (String subjectid : subjects.keySet()) {
                        Calendar cal = (Calendar)subjects.get(subjectid);
                        cal.set(5, 1);
                        cal.set(2, 0);
                        int row = dsdata.addRow();
                        dsdata.setString(row, "subjectid", subjectid);
                        dsdata.setString(row, "birthdt", DateFormatter.formatDateTime(cal));
                    }
                    if (dsdata.size() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_Subject");
                        props.setProperty("keyid1", dsdata.getColumnValues("subjectid", ";"));
                        props.setProperty("birthdt", dsdata.getColumnValues("birthdt", ";"));
                        props.setProperty("propsmatch", "Y");
                        props.setProperty("auditreason", "DOB cleared by HIPAA Rule");
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                } else {
                    throw new SapphireException(this.getTranslationProcessor().translate(String.valueOf(subjects.size())) + " " + this.getTranslationProcessor().translate("subjects will loose their Day and Month information from birth date. Continue?"));
                }
            }
        }
    }
}

