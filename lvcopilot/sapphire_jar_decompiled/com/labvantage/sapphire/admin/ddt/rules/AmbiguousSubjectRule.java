/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt.rules;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class AmbiguousSubjectRule
extends BaseBioBankRule {
    public AmbiguousSubjectRule(DBAccess database, ConnectionInfo connectionInfo) {
        super(database, connectionInfo);
    }

    @Override
    public String getRuleId() {
        return "Ambiguous Subject Rule";
    }

    public void processRule(List samplefamilylist) throws SapphireException {
        if (this.isRuleActive()) {
            DataSet ds;
            long start = System.currentTimeMillis();
            Trace.logInfo("START: " + this.getRuleId());
            StringBuilder sql = new StringBuilder();
            sql.append("select s_samplefamilyid, subjectid, externalsubject, sstudyid, studysiteid");
            sql.append(" from s_samplefamily");
            SafeSQL safeSQL = new SafeSQL();
            if (samplefamilylist.size() > 750) {
                String rsetid = this.getDAMProcessor().createRSet("LV_SampleFamily", OpalUtil.toDelimitedString(samplefamilylist, ";"), null, null);
                sql.append(" where s_samplefamilyid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                sql.append(" where s_samplefamilyid in ( ").append(safeSQL.addIn(OpalUtil.toDelimitedString(samplefamilylist, "','"))).append(" )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null) {
                HashSet<String> set = new HashSet<String>();
                boolean participantStudySubjectOnly = "Study-Subject".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("participantcreationrule"));
                for (int i = 0; i < ds.size(); ++i) {
                    String key;
                    String subjectid = ds.getValue(i, "subjectid");
                    String externalsubject = ds.getValue(i, "externalsubject");
                    String sstudyid = ds.getValue(i, "sstudyid");
                    String studysiteid = ds.getValue(i, "studysiteid");
                    if (StringUtil.getLen(subjectid) <= 0L || StringUtil.getLen(externalsubject) <= 0L || StringUtil.getLen(sstudyid) <= 0L || !set.add(key = sstudyid + subjectid + externalsubject + studysiteid)) continue;
                    this.checkAmbigousSubjectid(participantStudySubjectOnly, sstudyid, studysiteid, externalsubject, subjectid);
                }
            }
            Trace.logInfo("END: " + this.getRuleId() + ". Took " + (System.currentTimeMillis() - start) + "ms.");
        }
    }

    private void checkAmbigousSubjectid(boolean participantStudySubjectOnly, String studyid, String siteid, String patientid, String subjectid) throws SapphireException {
        DataSet ds;
        boolean hasSite = !participantStudySubjectOnly && StringUtil.getLen(siteid) > 0L;
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct( externalsubject ) externalsubject");
        sql.append(" from s_samplefamily");
        sql.append(" where sstudyid = ").append(safeSQL.addVar(studyid));
        sql.append(" and subjectid = ").append(safeSQL.addVar(subjectid));
        sql.append(" and externalsubject is not null");
        if (hasSite) {
            sql.append(" and studysiteid = ").append(safeSQL.addVar(siteid));
        }
        if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.size() > 1) {
            Map<String, String> tokenMap = this.getTokenMap(studyid, siteid, patientid, subjectid, hasSite);
            String message = this.getTranslationProcessor().translate("Sample records found for same Subject collected under different External Participant ID in same Study");
            message = message + "<br>";
            message = message + "<br>" + this.getTranslationProcessor().translate("Study: [studyid]", tokenMap);
            message = message + "<br>" + this.getTranslationProcessor().translate("Subject: [subjectid]", tokenMap);
            if (hasSite) {
                message = message + "<br>" + this.getTranslationProcessor().translate("Site: [sitedesc] ([siteid])", tokenMap);
            }
            message = message + "<br>" + this.getTranslationProcessor().translate("External Participant ID") + ": " + ds.getColumnValues("externalsubject", ", ");
            throw new SapphireException(message);
        }
        safeSQL.reset();
        sql.setLength(0);
        sql.append("select distinct( subjectid ) subjectid");
        sql.append(" from s_samplefamily");
        sql.append(" where sstudyid = ").append(safeSQL.addVar(studyid));
        sql.append(" and externalsubject = ").append(safeSQL.addVar(patientid));
        sql.append(" and subjectid is not null");
        if (hasSite) {
            sql.append(" and studysiteid = ").append(safeSQL.addVar(siteid));
        }
        if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && ds.size() > 1) {
            Map<String, String> tokenMap = this.getTokenMap(studyid, siteid, patientid, subjectid, hasSite);
            String message = this.getTranslationProcessor().translate("Sample records found collected under same External Participant ID used with multiple Subjects");
            message = message + "<br>";
            message = message + "<br>" + this.getTranslationProcessor().translate("Study: [studyid]", tokenMap);
            message = message + "<br>" + this.getTranslationProcessor().translate("External Participant ID: [patientid]", tokenMap);
            if (hasSite) {
                message = message + "<br>" + this.getTranslationProcessor().translate("Site: [sitedesc] ([siteid])", tokenMap);
            }
            message = message + "<br>" + this.getTranslationProcessor().translate("Subject") + ": " + ds.getColumnValues("subjectid", ", ");
            throw new SapphireException(message);
        }
    }

    private Map<String, String> getTokenMap(String studyid, String siteid, String patientid, String subjectid, boolean hasSite) {
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        tokenMap.put("studyid", studyid);
        tokenMap.put("subjectid", subjectid);
        tokenMap.put("patientid", patientid);
        if (hasSite) {
            tokenMap.put("siteid", siteid);
            tokenMap.put("sitedesc", OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "studysitedesc", "s_studysiteid = ?", new String[]{siteid}));
        }
        return tokenMap;
    }
}

