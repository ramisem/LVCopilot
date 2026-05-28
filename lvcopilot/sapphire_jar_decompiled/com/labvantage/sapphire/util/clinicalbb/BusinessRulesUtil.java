/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.clinicalbb;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class BusinessRulesUtil {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 102999 $";

    public static boolean isParticipantDependentSampleFamilyExists(String rsetid, DBAccess database) throws SapphireException {
        StringBuilder checkQry = new StringBuilder();
        checkQry.append("select p.s_participantid, sf.s_samplefamilyid ").append("from s_samplefamily sf, s_participant p, rsetitems r ").append("where ").append("sf.participantid = p.s_participantid ").append("and p.s_participantid = r.keyid1 ").append("and r.rsetid = ?");
        database.createPreparedResultSet(checkQry.toString(), new Object[]{rsetid});
        return database.getNext();
    }

    public static boolean isStudySiteDependentParticipantExists(String rsetid, DBAccess database) throws SapphireException {
        String findSql = "SELECT  distinct p.studysiteid FROM s_participant p, rsetitems r WHERE r.rsetid = ? AND  p.studysiteid = r.keyid1 AND r.sdcid='LV_StudySite'";
        database.createPreparedResultSet(findSql, new Object[]{rsetid});
        return database.getNext();
    }

    public static boolean isSubjectDependentParticipantExists(String rsetid, DBAccess database) throws SapphireException {
        String findSql = "select s_participant.subjectid from s_participant,rsetitems where rsetitems.rsetid = ? and s_participant.subjectid=rsetitems.keyid1 and rsetitems.sdcid='LV_Subject'";
        database.createPreparedResultSet(findSql, new Object[]{rsetid});
        return database.getNext();
    }

    public static boolean checkIfEventRefSampleFamily(String participantEventId, DBAccess database) throws SapphireException {
        boolean ref2SampleFamily = false;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT s_samplefamilyid FROM s_samplefamily WHERE participanteventid in (" + safeSQL.addIn(participantEventId, "','") + ") ";
        try {
            database.createPreparedResultSet("samplefamily", sql, safeSQL.getValues());
            ref2SampleFamily = database.getNext("samplefamily");
        }
        catch (SapphireException e) {
            throw e;
        }
        finally {
            database.closeResultSet("samplefamily");
        }
        return ref2SampleFamily;
    }

    public static boolean checkIfEventHasTimepoints(String participantEventId, DBAccess database) throws SapphireException {
        boolean hasChildEvents = false;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT s_participanteventid FROM s_participantevent WHERE parentparticipanteventId in (" + safeSQL.addIn(participantEventId, "','") + ")";
        try {
            database.createPreparedResultSet("timepoints", sql, safeSQL.getValues());
            hasChildEvents = database.getNext("timepoints");
        }
        catch (SapphireException e) {
            throw e;
        }
        finally {
            database.closeResultSet("timepoints");
        }
        return hasChildEvents;
    }

    public static boolean isStudyActive(String rsetid, DBAccess database) throws SapphireException {
        boolean isActive = false;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT activeflag from s_study WHERE s_studyid IN ( SELECT keyid1 FROM rsetitems ");
        sql.append(" WHERE rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
        try {
            database.createPreparedResultSet("study", sql.toString(), safeSQL.getValues());
            while (database.getNext("study")) {
                isActive = "Y".equalsIgnoreCase(BusinessRulesUtil.ifNull(database.getString("study", "activeflag")));
            }
        }
        catch (SapphireException e) {
            Logger.logStackTrace(e);
            throw e;
        }
        finally {
            database.closeResultSet("study");
        }
        return isActive;
    }

    public static boolean isEvntDefDependentParticipantEvntExists(String rsetid, DBAccess database) throws SapphireException {
        String findSql = "SELECT  distinct pe.eventdefid FROM s_participantevent pe, rsetitems r WHERE r.rsetid = ? AND  pe.eventdefid = r.keyid1 AND r.sdcid='LV_EventDef'";
        database.createPreparedResultSet(findSql, new Object[]{rsetid});
        return database.getNext();
    }

    public static boolean checkIfSFRefersRC(String rsetid, DBAccess database) throws SapphireException {
        String findSql = "SELECT  distinct sf.restrictclassid FROM s_samplefamily sf, rsetitems r WHERE r.rsetid = ? AND  sf.restrictclassid = r.keyid1 AND r.sdcid='LV_RestClass'";
        database.createPreparedResultSet(findSql, new Object[]{rsetid});
        return database.getNext();
    }

    public static void deleteEvents(String rsetid, DBAccess database, ActionProcessor ap) throws SapphireException {
        String findSql = "SELECT  distinct pe.s_participanteventid  FROM s_participantevent pe, rsetitems r  WHERE r.rsetid = ? AND  pe.participantid = r.keyid1 AND  r.sdcid='LV_Participant'";
        database.createPreparedResultSet(findSql, new Object[]{rsetid});
        DataSet ds = new DataSet(database.getResultSet());
        if (ds.size() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_ParticipantEvent");
            actionProps.setProperty("keyid1", ds.getColumnValues("s_participanteventid", ";"));
            ap.processAction("DeleteSdi", "1", actionProps);
        }
    }

    public static boolean isSubjecttDependentSampleFamilyExists(String rsetid, DBAccess database) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select s.s_subjectid, sf.subjectid ").append("from s_samplefamily sf, s_subject s, rsetitems r ").append("where ").append("sf.subjectid = s.s_subjectid ").append("and s.s_subjectid = r.keyid1 ").append("and r.rsetid = ?");
        database.createPreparedResultSet(sql.toString(), new Object[]{rsetid});
        return database.getNext();
    }

    public static void deleteParticipants(String rsetid, DBAccess database, ActionProcessor ap) throws SapphireException {
        String findSql = "SELECT distinct p.s_participantid  FROM s_participant p, rsetitems r  WHERE r.rsetid = ? AND  p.subjectid = r.keyid1 AND  r.sdcid='LV_Subject'";
        database.createPreparedResultSet(findSql, new Object[]{rsetid});
        DataSet ds = new DataSet(database.getResultSet());
        if (ds.size() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_Participant");
            actionProps.setProperty("keyid1", ds.getColumnValues("s_participantid", ";"));
            ap.processAction("DeleteSdi", "1", actionProps);
        }
    }

    public static void updateConsentQuestions(String cohortids, String protocolId, String protocolRevision, DBAccess database) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer updateSql = new StringBuffer("UPDATE s_clinicalprotocolquestion set  cpcohortid=NULL");
        updateSql.append(" WHERE cpcohortid in (").append(safeSQL.addIn(cohortids, ";")).append(")");
        updateSql.append(" AND s_clinicalprotocolid=").append(safeSQL.addVar(protocolId));
        updateSql.append(" AND s_clinicalprotocolrevision=").append(safeSQL.addVar(protocolRevision));
        database.executePreparedUpdate(updateSql.toString(), safeSQL.getValues());
    }

    public static boolean cohortHasParticipants(String cohortids, String protocolId, String protocolRevision, DBAccess database) throws SapphireException {
        StringBuffer findSql = new StringBuffer("SELECT s_participantid FROM s_participant");
        SafeSQL safeSQL = new SafeSQL();
        findSql.append(" WHERE cpcohortid in (").append(safeSQL.addIn(cohortids, ";")).append(")");
        findSql.append(" AND clinicalprotocolid=").append(safeSQL.addVar(protocolId));
        findSql.append(" AND clinicalprotocolrevision=").append(safeSQL.addVar(protocolRevision));
        database.createPreparedResultSet(findSql.toString(), safeSQL.getValues());
        return database.getNext();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static DataSet findFormFromProtocolUsingEvent(String participantEventId, QueryProcessor qp) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = null;
        sql.append("select fr.formid, fr.formversionid, fr.forminstance,");
        sql.append(" (select max(form.formversionid) from form where form.formid = fr.formid) defaultformversionid, fr.formrule,");
        sql.append(" p.sstudyid, p.studysiteid, p.s_participantid, p.participantstatus, pe.eventstatus, pe.s_participanteventid, pe.parentparticipanteventid,");
        sql.append(" p.subjectid, p.clinicalprotocolrevision");
        sql.append(" from sdiformrule fr, s_clinicalprotocol cp , s_participant p, s_participantevent pe");
        sql.append(" where fr.sdcid = 'LV_ClinicalProtocol'");
        sql.append(" and cp.s_clinicalprotocolid = p.clinicalprotocolid");
        sql.append(" and cp.s_clinicalprotocolrevision = p.clinicalprotocolrevision");
        sql.append(" and cp.versionstatus = 'C'");
        sql.append(" and pe.participantid = p.s_participantid");
        sql.append(" and fr.keyid1 = cp.s_clinicalprotocolid");
        sql.append(" and fr.keyid2 = cp.s_clinicalprotocolversionid");
        sql.append(" and fr.keyid3 = cp.s_clinicalprotocolrevision");
        if (StringUtil.split(participantEventId, ";").length > 1000) {
            DAMProcessor damProcessor = new DAMProcessor(qp.getConnectionid());
            String rsetid = null;
            try {
                rsetid = damProcessor.createRSet("LV_ParticipantEvent", participantEventId, null, null);
                sql.append(" and pe.s_participanteventid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                sql.append(" order by fr.usersequence");
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
            finally {
                if (rsetid != null && rsetid.length() > 0) {
                    damProcessor.clearRSet(rsetid);
                }
            }
        } else {
            sql.append(" and pe.s_participanteventid in (").append(safeSQL.addIn(participantEventId, ";")).append(")");
            sql.append(" order by fr.usersequence");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        return ds;
    }

    public static DataSet findFormFromProtocolUsingParticipantId(String participantId, QueryProcessor qp) {
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select fr.formid, fr.formversionid, fr.forminstance,");
        sql.append(" (select max(form.formversionid) from form where form.formid = fr.formid) defaultformversionid, fr.formrule,");
        sql.append(" p.sstudyid, p.studysiteid, p.s_participantid, p.participantstatus, p.cpcohortid");
        sql.append(" from sdiformrule fr, s_clinicalprotocol cp, s_participant p");
        sql.append(" where fr.sdcid = 'LV_ClinicalProtocol'");
        sql.append(" and cp.s_clinicalprotocolid = p.clinicalprotocolid");
        sql.append(" and cp.s_clinicalprotocolrevision = p.clinicalprotocolrevision");
        sql.append(" and cp.versionstatus = 'C'");
        sql.append(" and p.s_participantid in (").append(safeSQL.addIn(participantId, ";")).append(")");
        sql.append(" and fr.keyid1 = cp.s_clinicalprotocolid");
        sql.append(" and fr.keyid2 = cp.s_clinicalprotocolversionid");
        sql.append(" and fr.keyid3 = cp.s_clinicalprotocolrevision");
        sql.append(" order by fr.usersequence");
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    public static DataSet findFormFromProtocolUsingSampleId(String sampleId, QueryProcessor qp) {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("select distinct fr.formid, fr.formversionid, fr.forminstance ,  ");
        sql.append("( select max(form.formversionid) from form where form.formid = fr.formid ) defaultformversionid, ");
        sql.append("fr.formrule , ");
        sql.append(" sf.sstudyid, sf.studysiteid, s.s_sampleid, s.samplestatus ");
        sql.append("from sdiformrule fr, s_clinicalprotocol cp , s_samplefamily sf, s_sample s, s_study st ");
        sql.append("where sdcid = 'LV_ClinicalProtocol' and ");
        sql.append(" cp.s_clinicalprotocolid = sf.clinicalprotocolid AND ");
        sql.append(" cp.s_clinicalprotocolrevision = sf.clinicalprotocolrevision AND ");
        sql.append(" cp.s_clinicalprotocolrevision = sf.clinicalprotocolrevision AND ");
        sql.append(" sf.s_samplefamilyid = s.samplefamilyid AND ");
        sql.append(" cp.versionstatus = 'C' AND ");
        sql.append("s.s_sampleid in  (").append(safeSQL.addIn(sampleId, ";")).append(") and ");
        sql.append("keyid1 = cp.s_clinicalprotocolid and ");
        sql.append("keyid2 = cp.s_clinicalprotocolversionid and ");
        sql.append("keyid3 = cp.s_clinicalprotocolrevision ");
        return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static DataSet findFormFromProtocolUsingSF(String sampleFamilyId, QueryProcessor qp) {
        StringBuilder sql = new StringBuilder();
        DataSet ds = null;
        SafeSQL safeSQL = new SafeSQL();
        if (StringUtil.split(sampleFamilyId, ";").length > 750) {
            DAMProcessor damProcessor = new DAMProcessor(qp.getConnectionid());
            String rsetid = null;
            try {
                rsetid = damProcessor.createRSet("LV_SampleFamily", sampleFamilyId, null, null);
                sql.append("select fr.formid, fr.formversionid, fr.forminstance,");
                sql.append(" (select max(form.formversionid) from form where form.formid = fr.formid ) defaultformversionid, fr.formrule,");
                sql.append(" sf.sstudyid, sf.studysiteid, sf.s_samplefamilyid,");
                sql.append(" sf.participantid, sf.participanteventid, sf.clinicalprotocolrevision, sf.subjectid");
                sql.append(" from sdiformrule fr, s_clinicalprotocol cp , s_samplefamily sf");
                sql.append(" where sdcid = 'LV_ClinicalProtocol'");
                sql.append(" and cp.s_clinicalprotocolid = sf.clinicalprotocolid");
                sql.append(" and cp.s_clinicalprotocolrevision = sf.clinicalprotocolrevision");
                sql.append(" and cp.s_clinicalprotocolrevision = sf.clinicalprotocolrevision");
                sql.append(" and cp.versionstatus = 'C'");
                sql.append(" and sf.s_samplefamilyid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                sql.append(" and keyid1 = cp.s_clinicalprotocolid");
                sql.append(" and keyid2 = cp.s_clinicalprotocolversionid");
                sql.append(" and keyid3 = cp.s_clinicalprotocolrevision");
                sql.append(" order by fr.usersequence");
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            catch (SapphireException e) {
                ds = new DataSet();
                e.printStackTrace();
            }
            finally {
                if (rsetid != null && rsetid.length() > 0) {
                    damProcessor.clearRSet(rsetid);
                }
            }
        } else {
            sql.append("select fr.formid, fr.formversionid, fr.forminstance,");
            sql.append(" (select max(form.formversionid) from form where form.formid = fr.formid ) defaultformversionid, fr.formrule,");
            sql.append(" sf.sstudyid, sf.studysiteid, sf.s_samplefamilyid,");
            sql.append(" sf.participantid, sf.participanteventid, sf.clinicalprotocolrevision, sf.subjectid");
            sql.append(" from sdiformrule fr, s_clinicalprotocol cp , s_samplefamily sf");
            sql.append(" where sdcid = 'LV_ClinicalProtocol'");
            sql.append(" and cp.s_clinicalprotocolid = sf.clinicalprotocolid");
            sql.append(" and cp.s_clinicalprotocolrevision = sf.clinicalprotocolrevision");
            sql.append(" and cp.s_clinicalprotocolrevision = sf.clinicalprotocolrevision");
            sql.append(" and cp.versionstatus = 'C'");
            sql.append(" and sf.s_samplefamilyid in  (").append(safeSQL.addIn(sampleFamilyId, ";")).append(")");
            sql.append(" and keyid1 = cp.s_clinicalprotocolid");
            sql.append(" and keyid2 = cp.s_clinicalprotocolversionid");
            sql.append(" and keyid3 = cp.s_clinicalprotocolrevision");
            sql.append(" order by fr.usersequence");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        return ds;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static DataSet findFormFromEventDefUsingSampleId(String sampleFamilyId, QueryProcessor qp) {
        StringBuilder sql = new StringBuilder();
        DataSet ds = null;
        SafeSQL safeSQL = new SafeSQL();
        if (StringUtil.split(sampleFamilyId, ";").length > 750) {
            DAMProcessor damProcessor = new DAMProcessor(qp.getConnectionid());
            String rsetid = null;
            try {
                rsetid = damProcessor.createRSet("LV_SampleFamily", sampleFamilyId, null, null);
                sql.append("select fr.formid, fr.formversionid, fr.forminstance,");
                sql.append(" (select max(form.formversionid) from form where form.formid = fr.formid) defaultformversionid, fr.formrule,");
                sql.append(" sf.sstudyid, sf.studysiteid, sf.s_samplefamilyid,");
                sql.append(" sf.participantid, sf.participanteventid, sf.subjectid , sf.clinicalprotocolrevision");
                sql.append(" from sdiformrule fr, s_samplefamily sf, s_participantevent pe, s_eventdef ed");
                sql.append(" where fr.sdcid = 'LV_EventDef'");
                sql.append(" and sf.s_samplefamilyid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                sql.append(" and sf.participanteventid = pe.s_participanteventid");
                sql.append(" and pe.eventdefid = ed.s_eventdefid");
                sql.append(" and fr.keyid1 = ed.s_eventdefid");
                sql.append(" order by fr.usersequence");
                ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            catch (SapphireException e) {
                ds = new DataSet();
                e.printStackTrace();
            }
            finally {
                if (rsetid != null && rsetid.length() > 0) {
                    damProcessor.clearRSet(rsetid);
                }
            }
        } else {
            sql.append("select fr.formid, fr.formversionid, fr.forminstance,");
            sql.append(" (select max(form.formversionid) from form where form.formid = fr.formid) defaultformversionid, fr.formrule,");
            sql.append(" sf.sstudyid, sf.studysiteid, sf.s_samplefamilyid,");
            sql.append(" sf.participantid, sf.participanteventid, sf.subjectid , sf.clinicalprotocolrevision");
            sql.append(" from sdiformrule fr, s_samplefamily sf, s_participantevent pe, s_eventdef ed");
            sql.append(" where fr.sdcid = 'LV_EventDef'");
            sql.append(" and sf.s_samplefamilyid in (").append(safeSQL.addIn(sampleFamilyId, ";")).append(")");
            sql.append(" and sf.participanteventid = pe.s_participanteventid");
            sql.append(" and pe.eventdefid = ed.s_eventdefid");
            sql.append(" and fr.keyid1 = ed.s_eventdefid");
            sql.append(" order by fr.usersequence");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        return ds;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static DataSet getClinicalFlagForStudy(DAMProcessor dProcessor, QueryProcessor qp, List studyList) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("select s.s_studyid, s.clinicalflag");
        SafeSQL safeSQL = new SafeSQL();
        if (studyList.size() <= 750) {
            if (studyList.size() <= 0) return new DataSet();
            sql.append(" from s_study s");
            sql.append(" where s.s_studyid in ( ").append(safeSQL.addIn(studyList)).append(" )");
            return qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        String rsetid = dProcessor.createRSet("Study", OpalUtil.toDelimitedString(studyList, ";"), null, null);
        if (StringUtil.getLen(rsetid) <= 0L) throw new SapphireException("Unable to create RSET for " + OpalUtil.toDelimitedString(studyList, ";"));
        sql.append(" from s_study s, rsetitems r");
        sql.append(" where s.s_studyid = r.keyid1");
        sql.append(" and r.sdcid = 'Study'");
        sql.append(" and r.rsetid = ").append(safeSQL.addVar(rsetid));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        dProcessor.clearRSet(rsetid);
        return ds;
    }

    public static DataSet getClinicalFlagFromSampleFamily(DAMProcessor dp, QueryProcessor qp, List sampleFamilyList) throws SapphireException {
        DataSet ds;
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (sampleFamilyList.size() > 750) {
            String rsetid = dp.createRSet("LV_SampleFamily", OpalUtil.toDelimitedString(sampleFamilyList, ";"), null, null);
            sql.append("select s.s_studyid, s.clinicalflag");
            sql.append(" from s_study s, s_samplefamily sf");
            sql.append(" where sf.s_samplefamilyid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            sql.append(" and sf.sstudyid = s.s_studyid");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            dp.clearRSet(rsetid);
        } else {
            sql.append("select s.s_studyid, s.clinicalflag");
            sql.append(" from s_study s, s_samplefamily sf");
            sql.append(" where sf.s_samplefamilyid in ( ").append(safeSQL.addIn(sampleFamilyList)).append(" )");
            sql.append(" and sf.sstudyid = s.s_studyid");
            ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        return ds;
    }

    public static void getStudyFromCode(DataSet inputParamsDS, String studyCode, DBAccess database, TranslationProcessor tp, DAMProcessor damProcessor) throws SapphireException {
        String rsetid = "";
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT s_studyid, studyalias FROM s_study ");
        SafeSQL safeSQL = new SafeSQL();
        if (studyCode.split(";").length <= 750) {
            sql.append(" WHERE studyalias in (").append(safeSQL.addIn(studyCode, ";")).append(")");
        } else {
            rsetid = damProcessor.createRSet("Study", studyCode, null, null);
            sql.append("  studyalias in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
        }
        database.createPreparedResultSet("study", sql.toString(), safeSQL.getValues());
        while (database.getNext("study")) {
            for (int k = 0; k < inputParamsDS.size(); ++k) {
                if (!inputParamsDS.getValue(k, "studyalias", "").equals(database.getString("study", "studyalias"))) continue;
                inputParamsDS.setValue(k, "sstudyid", database.getString("study", "s_studyid"));
            }
        }
        if (StringUtil.getLen(rsetid) > 0L) {
            damProcessor.clearRSet(rsetid);
        }
    }

    public static void getSiteIdForGivenDepartment(DataSet inputParamsDS, String studyField, DBAccess database, TranslationProcessor tp, DAMProcessor damProcessor) throws SapphireException {
        String rsetid = "";
        inputParamsDS.sort(studyField);
        ArrayList<DataSet> dsAddSDIPropList = inputParamsDS.getGroupedDataSets(studyField);
        for (int i = 0; i < dsAddSDIPropList.size(); ++i) {
            DataSet groupDataSet = dsAddSDIPropList.get(i);
            String studyid = groupDataSet.getValue(0, "sstudyid");
            String departmentIds = groupDataSet.getColumnValues("sitedepartment", ";");
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT s_studysiteid, departmentid, sstudyid FROM s_studysite ");
                SafeSQL safeSQL = new SafeSQL();
                if (departmentIds.split(";").length <= 750) {
                    sql.append("WHERE departmentid in (").append(safeSQL.addIn(groupDataSet.getColumnValues("sitedepartment", ";"), ";")).append(") ");
                } else {
                    rsetid = damProcessor.createRSet("LV_StudySite", departmentIds, null, null);
                    sql.append(" WHERE departmentid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                }
                sql.append(" AND sstudyid = ").append(safeSQL.addVar(studyid)).append(" ");
                database.createPreparedResultSet("studysite", sql.toString(), safeSQL.getValues());
                while (database.getNext("studysite")) {
                    for (int k = 0; k < inputParamsDS.size(); ++k) {
                        if (!inputParamsDS.getValue(k, "sstudyid", "").equals(database.getString("studysite", "sstudyid")) || !inputParamsDS.getValue(k, "sitedepartment", "").equals(database.getString("studysite", "departmentid"))) continue;
                        inputParamsDS.setValue(k, "studysiteid", database.getString("studysite", "s_studysiteid"));
                    }
                }
                continue;
            }
            catch (SapphireException e) {
                Logger.logError("getSiteIdForGivenDepartment error", e);
                throw e;
            }
            finally {
                database.closeResultSet("studysite");
                if (StringUtil.getLen(rsetid) > 0L) {
                    damProcessor.clearRSet(rsetid);
                }
            }
        }
    }

    public static boolean isColumnNullInDataSet(DataSet inputParamsDS, String column) {
        boolean isNull = false;
        for (int i = 0; i < inputParamsDS.size(); ++i) {
            if (inputParamsDS.getValue(i, column, "").length() != 0) continue;
            isNull = true;
            break;
        }
        return isNull;
    }

    public static void getSiteIdForGivenSiteDesc(DataSet inputParamsDS, String studyColumn, DBAccess database, TranslationProcessor tp, DAMProcessor damProcessor) throws SapphireException {
        String rsetid = "";
        inputParamsDS.sort(studyColumn);
        ArrayList<DataSet> dsAddSDIPropList = inputParamsDS.getGroupedDataSets(studyColumn);
        for (int i = 0; i < dsAddSDIPropList.size(); ++i) {
            DataSet groupDataSet = dsAddSDIPropList.get(i);
            String studyid = groupDataSet.getValue(0, "sstudyid");
            String sitedesc = groupDataSet.getColumnValues("sitedesc", ";");
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT s_studysiteid, studysitedesc, sstudyid FROM s_studysite ");
                SafeSQL safeSQL = new SafeSQL();
                if (sitedesc.split(";").length <= 750) {
                    sql.append("WHERE studysitedesc in (").append(safeSQL.addIn(groupDataSet.getColumnValues("sitedesc", ";"), ";")).append(") ");
                } else {
                    rsetid = damProcessor.createRSet("LV_StudySite", sitedesc, null, null);
                    sql.append(" WHERE studysitedesc in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                }
                sql.append(" AND sstudyid = ").append(safeSQL.addVar(studyid)).append(" ");
                database.createPreparedResultSet("studysite", sql.toString(), safeSQL.getValues());
                while (database.getNext("studysite")) {
                    for (int k = 0; k < inputParamsDS.size(); ++k) {
                        if (!inputParamsDS.getValue(k, "sitedesc", "").equals(database.getString("studysite", "studysitedesc")) || !inputParamsDS.getValue(k, "sstudyid", "").equals(database.getString("studysite", "sstudyid"))) continue;
                        inputParamsDS.setValue(k, "studysiteid", database.getString("studysite", "s_studysiteid"));
                    }
                }
                continue;
            }
            catch (SapphireException e) {
                Logger.logError("getSiteIdForGivenSiteDesc error", e);
                throw e;
            }
            finally {
                database.closeResultSet("studysite");
                if (StringUtil.getLen(rsetid) > 0L) {
                    damProcessor.clearRSet(rsetid);
                }
            }
        }
    }

    public static DataSet getCPFromSiteID(String siteId, DataSet cp4SiteDS, DBAccess database, DAMProcessor damProcessor) throws SapphireException {
        String rsetid = "";
        String sql = "SELECT ss.s_studysiteid, cp.s_clinicalprotocolid, cp.s_clinicalprotocolversionid, cp.s_clinicalprotocolrevision  FROM s_studysite ss, s_clinicalprotocol cp WHERE ";
        SafeSQL safeSQL = new SafeSQL();
        if (siteId.split(";").length <= 750) {
            sql = sql + "   ss.s_studysiteid in (" + safeSQL.addIn(siteId, ";") + ") ";
        } else {
            rsetid = damProcessor.createRSet("LV_StudySite", siteId, null, null);
            sql = sql + " ss.s_studysiteid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = " + safeSQL.addVar(rsetid) + " )";
        }
        sql = sql + " and ss.clinicalprotocolid = cp.s_clinicalprotocolid  and cp.versionstatus = 'C'  and ss.clinicalprotocolrevision = cp.s_clinicalprotocolrevision";
        try {
            database.createPreparedResultSet("rc", sql, safeSQL.getValues());
            int counter = 0;
            while (database.getNext("rc")) {
                cp4SiteDS.addRow();
                cp4SiteDS.setValue(counter, "studysiteid", BusinessRulesUtil.ifNull(database.getString("rc", "s_studysiteid")));
                cp4SiteDS.setValue(counter, "clinicalprotocolid", BusinessRulesUtil.ifNull(database.getString("rc", "s_clinicalprotocolid")));
                cp4SiteDS.setValue(counter, "clinicalprotocolrevision", BusinessRulesUtil.ifNull(database.getString("rc", "s_clinicalprotocolrevision")));
                String cpversionid = BusinessRulesUtil.ifNull(database.getString("rc", "s_clinicalprotocolversionid"));
                cp4SiteDS.setValue(counter, "clinicalprotocolversionid", cpversionid);
                ++counter;
            }
        }
        catch (SapphireException e) {
            throw new SapphireException(e);
        }
        finally {
            database.closeResultSet("rc");
            if (StringUtil.getLen(rsetid) > 0L) {
                damProcessor.clearRSet(rsetid);
            }
        }
        return cp4SiteDS;
    }

    public static boolean checkDupEvtLbls(String tableColId, String tablColVal, String eventLabel, DBAccess database) throws SapphireException {
        boolean duplicateLabel = false;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT count(1) FROM s_participantevent WHERE " + tableColId + " in (" + safeSQL.addIn(tablColVal, "','") + ")  and  eventlabel = " + safeSQL.addVar(eventLabel);
        if (database.getPreparedCount(sql, safeSQL.getValues()) > 0) {
            duplicateLabel = true;
        }
        return duplicateLabel;
    }

    public static String ifNull(String str) {
        if (str == null) {
            str = "";
        }
        return str;
    }

    public static String checkIfSiteOrPartRefCPRev(String cpRevisions, String cpIds, DAMProcessor damProcessor, QueryProcessor qp) throws SapphireException {
        String error = "";
        boolean siteRefRev = false;
        DataSet cpDS = new DataSet();
        cpDS.addColumn("s_clinicalprotocolid", 0);
        cpDS.addColumn("s_clinicalprotocolrevision", 0);
        String[] cpIdArr = cpIds.split(";");
        String[] cpRevArr = cpRevisions.split(";");
        for (int i = 0; i < cpIdArr.length; ++i) {
            cpDS.addRow();
            cpDS.setValue(i, "s_clinicalprotocolid", cpIdArr[i]);
            cpDS.setValue(i, "s_clinicalprotocolrevision", cpRevArr[i]);
        }
        cpDS.sort("s_clinicalprotocolid");
        ArrayList<DataSet> groupedDSAL = cpDS.getGroupedDataSets("s_clinicalprotocolid");
        for (int i = 0; i < groupedDSAL.size(); ++i) {
            DataSet groupDataSet = groupedDSAL.get(i);
            String cpid = groupDataSet.getValue(0, "s_clinicalprotocolid");
            String cprevisions = groupDataSet.getColumnValues("s_clinicalprotocolrevision", ";");
            String rsetid = "";
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT count(*) FROM s_studysite WHERE ");
            sql.append("clinicalprotocolid = ").append(safeSQL.addVar(cpid)).append(" AND ");
            if (cprevisions.split(";").length <= 750) {
                sql.append("clinicalprotocolrevision in (").append(safeSQL.addIn(cprevisions, ";")).append(")");
            } else {
                rsetid = damProcessor.createRSet("LV_StudySite", cprevisions, null, null);
                sql.append("clinicalprotocolrevision  in (");
                sql.append(" select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            }
            if (qp.getPreparedCount(sql.toString(), safeSQL.getValues()) > 0) {
                siteRefRev = true;
                error = "sites";
                break;
            }
            if (siteRefRev) continue;
            sql.setLength(0);
            safeSQL.reset();
            sql.append("SELECT count(*) FROM s_participant WHERE ");
            sql.append("clinicalprotocolid = ").append(safeSQL.addVar(cpid)).append(" AND ");
            if (cprevisions.split(";").length <= 750) {
                sql.append("clinicalprotocolrevision in (").append(safeSQL.addIn(cprevisions, ";")).append(")");
            } else {
                rsetid = damProcessor.createRSet("LV_Participant", cprevisions, null, null);
                sql.append("clinicalprotocolrevision  in (");
                sql.append(" select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            }
            if (qp.getPreparedCount(sql.toString(), safeSQL.getValues()) <= 0) continue;
            siteRefRev = true;
            error = "participant";
            break;
        }
        return error;
    }

    public static DataSet getVersionCountForSelRev(String cpRevisions, String cpIds, DAMProcessor damProcessor, QueryProcessor qp) throws SapphireException {
        DataSet ds = new DataSet();
        ds.addColumn("s_clinicalprotocolid", 0);
        ds.addColumn("s_clinicalprotocolrevision", 0);
        ds.addColumn("versioncount", 1);
        DataSet cpDS = new DataSet();
        cpDS.addColumn("s_clinicalprotocolid", 0);
        cpDS.addColumn("s_clinicalprotocolrevision", 0);
        String[] cpIdArr = cpIds.split(";");
        String[] cpRevArr = cpRevisions.split(";");
        for (int i = 0; i < cpIdArr.length; ++i) {
            cpDS.addRow();
            cpDS.setValue(i, "s_clinicalprotocolid", cpIdArr[i]);
            cpDS.setValue(i, "s_clinicalprotocolrevision", cpRevArr[i]);
        }
        cpDS.sort("s_clinicalprotocolid");
        ArrayList<DataSet> groupedDSAL = cpDS.getGroupedDataSets("s_clinicalprotocolid");
        for (int i = 0; i < groupedDSAL.size(); ++i) {
            DataSet groupDataSet = groupedDSAL.get(i);
            String cpid = groupDataSet.getValue(0, "s_clinicalprotocolid");
            String cprevisions = groupDataSet.getColumnValues("s_clinicalprotocolrevision", ";");
            String rsetid = "";
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT s_clinicalprotocolid, s_clinicalprotocolversionid, s_clinicalprotocolrevision FROM s_clinicalprotocol WHERE ");
            sql.append("s_clinicalprotocolid = ").append(safeSQL.addVar(cpid)).append(" AND ");
            if (cprevisions.split(";").length <= 750) {
                sql.append(" s_clinicalprotocolrevision in (").append(safeSQL.addIn(cprevisions, ";")).append(")");
            } else {
                rsetid = damProcessor.createRSet("LV_StudySite", cprevisions, null, null);
                sql.append(" s_clinicalprotocolrevision  in (");
                sql.append(" select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            }
            DataSet sqlds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            sqlds.sort("s_clinicalprotocoLrevision");
            ArrayList<DataSet> dsAL = sqlds.getGroupedDataSets("s_clinicalprotocoLrevision");
            for (int j = 0; j < dsAL.size(); ++j) {
                int rowcount = ds.size();
                ds.addRow();
                ds.setValue(rowcount, "s_clinicalprotocolid", cpid);
                ds.setValue(rowcount, "s_clinicalprotocolrevision", dsAL.get(j).getValue(0, "s_clinicalprotocolrevision"));
                ds.setValue(rowcount, "versioncount", String.valueOf(dsAL.get(j).size()));
            }
        }
        return ds;
    }

    public static DataSet removeDuplicateRows(DataSet inputParamsDS) {
        HashSet set = new HashSet();
        DataSet ds = new DataSet();
        for (int i = 0; i < inputParamsDS.size(); ++i) {
            if (!set.add(inputParamsDS.get(i))) continue;
            ds.copyRow(inputParamsDS, i, 1);
        }
        return ds;
    }
}

