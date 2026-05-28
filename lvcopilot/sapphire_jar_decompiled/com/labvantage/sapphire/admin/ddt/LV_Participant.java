/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.FormUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_Participant
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 94240 $";
    public static final String SDCID = "LV_Participant";
    Map<String, String> cprevisionmap = new HashMap<String, String>();
    private Map<String, String> studyCache = new HashMap<String, String>();

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select p.s_participantid, sf.s_samplefamilyid from s_samplefamily sf, s_participant p, rsetitems r where sf.participantid = p.s_participantid and p.s_participantid = r.keyid1 and r.rsetid = ?", (Object[])new String[]{rsetid});
        if (ds != null && ds.size() > 0) {
            this.throwError("DependentSample", "VALIDATION", this.getTranslationProcessor().translate("Some Participant(s) are found to be associated with samplefamily(s)!"));
        } else {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT distinct pe.s_participanteventid FROM s_participantevent pe, rsetitems r WHERE r.rsetid = ? AND pe.participantid = r.keyid1 AND r.sdcid='LV_Participant'", (Object[])new String[]{rsetid});
            if (ds != null && ds.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_ParticipantEvent");
                props.setProperty("keyid1", ds.getColumnValues("s_participanteventid", ";"));
                this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
            }
        }
    }

    @Override
    public void preAdd(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        boolean participantStudySubjectOnly = "Study-Subject".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("participantcreationrule"));
        DataSet primary = sdidata.getDataset("primary");
        this.validateSubjectEnrollment(primary, participantStudySubjectOnly);
        this.validateAmbigousSubjectid(primary, participantStudySubjectOnly);
        String sql = "SELECT clinicalflag FROM s_study where s_studyid = ?";
        for (int i = 0; i < primary.size(); ++i) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{primary.getString(i, "sstudyid")});
            if (ds.getRowCount() <= 0 || "N".equals(ds.getString(0, "clinicalflag"))) continue;
            if (OpalUtil.isEmpty(primary.getString(i, "clinicalprotocolid", ""))) {
                primary.setString(i, "clinicalprotocolid", primary.getString(i, "sstudyid"));
            }
            if (!OpalUtil.isNotEmpty(primary.getString(i, "studysiteid")) || !OpalUtil.isEmpty(primary.getString(i, "clinicalprotocolrevision", ""))) continue;
            primary.setString(i, "clinicalprotocolrevision", this.getStudySiteProtocolRevision(primary.getString(i, "studysiteid")));
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        DataSet ds = BusinessRulesUtil.findFormFromProtocolUsingParticipantId(primary.getColumnValues("s_participantid", ";"), this.getQueryProcessor());
        this.addBlankDocuments(ds, "add");
        String sql = "SELECT  clinicalflag FROM s_study WHERE s_studyid = ?";
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String studyid = primary.getString(i, "sstudyid");
            DataSet studyInfo = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{studyid});
            if (studyInfo == null || studyInfo.getRowCount() <= 0) continue;
            if ("N".equals(studyInfo.getString(0, "clinicalflag"))) {
                DataSet consentQuestions = this.getConsentQuestionsFromStudy(studyid);
                this.addConsentQuestionsFromStudy(primary.getString(i, "s_participantid"), consentQuestions, "1");
                continue;
            }
            String newRevision = primary.getString(i, "clinicalprotocolrevision");
            this.addConsentQuestionsFromCP(primary.getString(i, "s_participantid"), primary.getString(i, "clinicalprotocolid"), "", newRevision, primary.getString(i, "clinicalprotocolrevision"), "1");
        }
    }

    @Override
    public void preEdit(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        boolean fixdatamode = "Y".equals(actionProps.getProperty("fixdatamode", "N"));
        boolean participantStudySubjectOnly = "Study-Subject".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("participantcreationrule"));
        DataSet primary = sdidata.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String sstudyid;
            if (this.hasPrimaryValueChanged(primary, i, "externalparticipantid")) {
                sstudyid = this.getOldPrimaryValue(primary, i, "sstudyid");
                String subjectid = this.getOldPrimaryValue(primary, i, "subjectid");
                String externalparticipantid = primary.getString(i, "externalparticipantid");
                if (participantStudySubjectOnly) {
                    if (this.database.getPreparedCount("select count(s_participantid) from s_participant where sstudyid=? and subjectid=? and externalparticipantid=?", new String[]{sstudyid, subjectid, externalparticipantid}) <= 0) continue;
                    this.throwError(this.getTranslationProcessor().translate("External Participant Update Error"), "VALIDATION", this.getTranslationProcessor().translate("Participant already exists with same externalparticipantid") + " (" + this.getTranslationProcessor().translate("Study") + ": " + sstudyid + ", " + this.getTranslationProcessor().translate("Subject") + ": " + subjectid + ", " + this.getTranslationProcessor().translate("External Participant") + ": " + externalparticipantid + ")");
                    continue;
                }
                String studysiteid = this.getOldPrimaryValue(primary, i, "studysiteid");
                if (this.database.getPreparedCount("select count(s_participantid) from s_participant where sstudyid=? and subjectid=? and studysiteid=? and externalparticipantid=?", new String[]{sstudyid, subjectid, studysiteid, externalparticipantid}) <= 0) continue;
                this.throwError(this.getTranslationProcessor().translate("External Participant ID") + " " + this.getTranslationProcessor().translate("Update Error"), "VALIDATION", this.getTranslationProcessor().translate("Participant") + " " + this.getTranslationProcessor().translate("already exists") + " (" + this.getTranslationProcessor().translate("Study") + ": " + sstudyid + ", " + this.getTranslationProcessor().translate("Subject") + ": " + subjectid + ", " + this.getTranslationProcessor().translate("Site") + ": " + OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "studysitedesc", "s_studysiteid=?", new String[]{studysiteid}) + ", " + this.getTranslationProcessor().translate("External Participant ID") + ": " + externalparticipantid + ")");
                continue;
            }
            if (this.hasPrimaryValueChanged(primary, i, "sstudyid")) {
                if (fixdatamode) continue;
                this.throwError(this.getTranslationProcessor().translate("Participant") + " " + this.getTranslationProcessor().translate("Update Error"), "VALIDATION", this.getTranslationProcessor().translate("Not allowed to update Study in a Participant"));
                continue;
            }
            if (!this.hasPrimaryValueChanged(primary, i, "studysiteid") || fixdatamode || !this.isStudyProtocolDriven(sstudyid = this.getOldPrimaryValue(primary, i, "sstudyid"))) continue;
            this.throwError(this.getTranslationProcessor().translate("Participant") + " " + this.getTranslationProcessor().translate("Update Error"), "VALIDATION", this.getTranslationProcessor().translate("Not allowed to update Site in a Participant under Protocol Study"));
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        boolean fixdatamode = "Y".equals(actionProps.getProperty("fixdatamode", "N"));
        boolean updatesampledata = "Y".equals(actionProps.getProperty("updatesampledata", "N")) && this.getConnectionInfo().hasRole("SMS Admin");
        DataSet primary = sdiData.getDataset("primary");
        String participantIds = "";
        String upRevisionParticipants = "";
        HashMap<String[], DataSet> participantSampleFamilyMap = new HashMap<String[], DataSet>();
        HashMap<Object, DataSet> participantSampleMap = new HashMap<Object, DataSet>();
        for (int i = 0; i < primary.size(); ++i) {
            String[] participantid = primary.getString(i, "s_participantid");
            if (fixdatamode && updatesampledata && (this.hasPrimaryValueChanged(primary, i, "sstudyid") || this.hasPrimaryValueChanged(primary, i, "studysiteid"))) {
                DataSet sampleDataSet;
                if (!participantSampleFamilyMap.containsKey(participantid)) {
                    participantSampleFamilyMap.put(participantid, this.getQueryProcessor().getPreparedSqlDataSet("select s_samplefamilyid, sstudyid, studysiteid from s_samplefamily where participantid = ?", (Object[])new String[]{participantid}));
                    participantSampleMap.put(participantid, this.getQueryProcessor().getPreparedSqlDataSet("select s_sample.s_sampleid, s_sample.sstudyid from s_sample, s_samplefamily where s_sample.samplefamilyid = s_samplefamily.s_samplefamilyid and s_samplefamily.participantid = ?", (Object[])new String[]{participantid}));
                }
                String string = primary.getString(i, "sstudyid", "");
                String participantStudySiteid = primary.getString(i, "studysiteid", "");
                DataSet sampleFamilyDataSet = (DataSet)participantSampleFamilyMap.get(participantid);
                if (OpalUtil.isNotEmpty(sampleFamilyDataSet)) {
                    sampleFamilyDataSet.setValue(-1, "sstudyid", string);
                    sampleFamilyDataSet.setValue(-1, "studysiteid", participantStudySiteid);
                }
                if (OpalUtil.isNotEmpty(sampleDataSet = (DataSet)participantSampleMap.get(participantid))) {
                    sampleDataSet.setValue(-1, "sstudyid", string);
                }
            }
            if (this.hasPrimaryValueChanged(primary, i, "participantstatus")) {
                participantIds = participantIds.length() > 0 ? participantIds + ";" + primary.getValue(i, "s_participantid", ";") : primary.getValue(i, "s_participantid", ";");
            }
            if (!this.hasPrimaryValueChanged(primary, i, "clinicalprotocolrevision")) continue;
            String string = primary.getString(i, "s_participantid");
            String sql = "SELECT count(*) FROM s_participantconsent WHERE s_participantid  = ?";
            int count = this.getQueryProcessor().getPreparedCount(sql, new Object[]{string});
            String newconsentinstance = "" + (count + 1);
            String oldRevision = this.getOldPrimaryValue(primary, i, "clinicalprotocolrevision");
            String newRevision = primary.getString(i, "clinicalprotocolrevision");
            this.addConsentQuestionsFromCP(string, primary.getString(i, "clinicalprotocolid"), primary.getString(i, "clinicalprotocolverionid"), oldRevision, newRevision, newconsentinstance);
        }
        if (participantIds.length() > 0) {
            DataSet ds = BusinessRulesUtil.findFormFromProtocolUsingParticipantId(participantIds, this.getQueryProcessor());
            this.addBlankDocuments(ds, "edit");
        }
        this.resetExternalParticipantIdInSampleFamily(primary);
        if (upRevisionParticipants.length() > 0) {
            String[] particiantsToUprevision = StringUtil.split(upRevisionParticipants, ";");
            for (String aParticiantsToUprevision : particiantsToUprevision) {
                this.uprevision(aParticiantsToUprevision);
            }
        }
        if (fixdatamode && updatesampledata) {
            DataSet familyDataSet = new DataSet();
            for (Map.Entry entry : participantSampleFamilyMap.entrySet()) {
                DataSet ds = (DataSet)entry.getValue();
                if (ds == null) continue;
                for (int i = 0; i < ds.size(); ++i) {
                    familyDataSet.copyRow(ds, i, 1);
                }
            }
            DataSet sampleDataSet = new DataSet();
            for (Map.Entry entry : participantSampleMap.entrySet()) {
                DataSet ds = (DataSet)entry.getValue();
                if (ds == null) continue;
                for (int i = 0; i < ds.size(); ++i) {
                    sampleDataSet.copyRow(ds, i, 1);
                }
            }
            if (familyDataSet.size() > 0) {
                PropertyList propertyList = new PropertyList();
                propertyList.setProperty("sdcid", "LV_SampleFamily");
                propertyList.setProperty("keyid1", familyDataSet.getColumnValues("s_samplefamilyid", ";"));
                propertyList.setProperty("sstudyid", familyDataSet.getColumnValues("sstudyid", ";"));
                propertyList.setProperty("studysiteid", familyDataSet.getColumnValues("studysiteid", ";"));
                propertyList.setProperty("__participanteditflag", "Y");
                propertyList.setProperty("auditreason", "Participant updated");
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), propertyList);
            }
            if (sampleDataSet.size() > 0) {
                PropertyList propertyList = new PropertyList();
                propertyList.setProperty("sdcid", "Sample");
                propertyList.setProperty("keyid1", sampleDataSet.getColumnValues("s_sampleid", ";"));
                propertyList.setProperty("sstudyid", familyDataSet.getColumnValues("sstudyid", ";"));
                propertyList.setProperty("__samplePreEditRuleIgnore", "Y");
                propertyList.setProperty("__samplePostEditRuleIgnore", "Y");
                propertyList.setProperty("auditreason", "Participant updated");
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), propertyList);
            }
        }
    }

    private String getStudySiteProtocolRevision(String studysiteid) {
        if (!this.cprevisionmap.containsKey(studysiteid)) {
            this.cprevisionmap.put(studysiteid, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "clinicalprotocolrevision", "s_studysiteid = ?", new String[]{studysiteid}));
        }
        return this.cprevisionmap.get(studysiteid);
    }

    private void validateSubjectEnrollment(DataSet primary, boolean participantStudySubjectOnly) throws SapphireException {
        int invalidStudyCount;
        List<String> studyList = OpalUtil.toUniqueList(primary.getColumnValues("sstudyid", ";"), ";");
        if (studyList.size() > 1000) {
            String rsetid = this.getDAMProcessor().createRSet("Study", OpalUtil.toDelimitedString(studyList, ";"), null, null);
            invalidStudyCount = this.database.getPreparedCount("select count(s_studyid) from s_study where s_studyid in (select r.keyid1 from rsetitems r where r.rsetid = ?) and studystatus in ('Completed', 'Cancelled')", new String[]{rsetid});
            this.getDAMProcessor().clearRSet(rsetid);
        } else {
            SafeSQL safeSQL = new SafeSQL();
            invalidStudyCount = this.database.getPreparedCount("select count(s_studyid) from s_study where s_studyid in (" + safeSQL.addIn(studyList) + ") and studystatus in ('Completed', 'Cancelled')", safeSQL.getValues());
        }
        if (invalidStudyCount > 0 && !"Y".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("allowcompletedstudyenroll"))) {
            throw new SapphireException(this.getTranslationProcessor().translate("Invalid Study Status"), this.getTranslationProcessor().translate("Not allowed to enroll Participant in Cancelled or Completed study"));
        }
        if (participantStudySubjectOnly) {
            for (int i = 0; i < primary.size(); ++i) {
                String subjectId;
                String sstudyid = primary.getString(i, "sstudyid");
                if (!this.database.checkPreparedExists("select s_participantid from s_participant where sstudyid=? and subjectid=?", new String[]{sstudyid, subjectId = primary.getValue(i, "subjectid", "")})) continue;
                this.throwError(this.getTranslationProcessor().translate("Subject") + " " + this.getTranslationProcessor().translate("Enrollment"), "VALIDATION", this.getTranslationProcessor().translate("Subject") + " " + this.getTranslationProcessor().translate("already enrolled") + " (" + this.getTranslationProcessor().translate("Subject") + ": " + subjectId + ", " + this.getTranslationProcessor().translate("Study") + ": " + sstudyid);
            }
        } else {
            for (int i = 0; i < primary.size(); ++i) {
                String studySiteId = primary.getValue(i, "studysiteid", "");
                String subjectId = primary.getValue(i, "subjectid", "");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT p.s_participantid, ss.studysitedesc FROM s_participant p, s_studysite ss WHERE p.studysiteid = ? AND p.subjectid = ? AND p.studysiteid= ss.s_studysiteid", (Object[])new String[]{studySiteId, subjectId});
                if (!OpalUtil.isNotEmpty(ds)) continue;
                this.throwError(this.getTranslationProcessor().translate("Subject") + " " + this.getTranslationProcessor().translate("Enrollment"), "VALIDATION", this.getTranslationProcessor().translate("Subject") + " " + this.getTranslationProcessor().translate("already enrolled") + "  (" + this.getTranslationProcessor().translate("Site") + ": " + ds.getValue(0, "studysitedesc", "") + ")");
            }
        }
    }

    private void resetExternalParticipantIdInSampleFamily(DataSet primary) throws SapphireException {
        DataSet editFamilyDS = new DataSet();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "externalparticipantid")) continue;
            String participantId = primary.getValue(i, "s_participantid");
            String externalParticipantId = primary.getValue(i, "externalparticipantid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_samplefamilyid from s_samplefamily where participantid = ? and (externalsubject != ? or externalsubject is null)", (Object[])new String[]{participantId, externalParticipantId});
            for (int dsrow = 0; dsrow < ds.size(); ++dsrow) {
                int row = editFamilyDS.addRow();
                editFamilyDS.setString(row, "s_samplefamilyid", ds.getString(dsrow, "s_samplefamilyid"));
                editFamilyDS.setString(row, "externalsubject", externalParticipantId);
            }
        }
        if (editFamilyDS.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_SampleFamily");
            props.setProperty("keyid1", editFamilyDS.getColumnValues("s_samplefamilyid", ";"));
            props.setProperty("externalsubject", editFamilyDS.getColumnValues("externalsubject", ";"));
            props.setProperty("__participanteditflag", "Y");
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
    }

    private void uprevision(String participantID) {
    }

    private void addBlankDocuments(DataSet ds, String mode) throws SapphireException {
        if (ds.size() > 0) {
            DataSet formsDataSet = new DataSet();
            for (int i = 0; i < ds.size(); ++i) {
                if (!ds.getValue(i, "formrule").toLowerCase().contains("participant") || !ds.getValue(i, "formrule").contains(":")) continue;
                String ruledef = ds.getValue(i, "formrule").split(":")[1].trim();
                if (mode.equalsIgnoreCase("add") && ruledef.toLowerCase().contains("creation")) {
                    formsDataSet.copyRow(ds, i, 1);
                    continue;
                }
                if (ds.getValue(i, "participantstatus").equalsIgnoreCase("complete") && ruledef.toLowerCase().contains("completion")) {
                    formsDataSet.copyRow(ds, i, 1);
                    continue;
                }
                if (ds.getValue(i, "participantstatus").equalsIgnoreCase("Enrolled") && ruledef.toLowerCase().contains("enrollment")) {
                    formsDataSet.copyRow(ds, i, 1);
                    continue;
                }
                if (!ds.getValue(i, "participantstatus").equalsIgnoreCase("Withdrawn") || !ruledef.toLowerCase().contains("withdrawl")) continue;
                formsDataSet.copyRow(ds, i, 1);
            }
            try {
                FormUtil.addBlankDocument(formsDataSet, this.getActionProcessor(), SDCID, "s_participantid");
            }
            catch (ActionException e) {
                this.logger.error("Unable to add Blank Document to Sample " + formsDataSet.getColumnValues("s_sampleid", ";") + ". Exception raised is: " + e.getMessage());
                throw new SapphireException(e);
            }
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    private DataSet getConsentQuestionsFromStudy(String studyid) {
        return this.getQueryProcessor().getPreparedSqlDataSet("SELECT s_questionid, questiontext, answergranted, answereditorstyleid, consenttype, usersequence FROM s_studyquestion WHERE s_studyid = ?", new Object[]{studyid});
    }

    private DataSet getConsentQuestionsFromProtocol(String participantid) {
        String sql = "SELECT  p.s_participantid, p.cpcohortid, cp.s_clinicalprotocolversionid, cpq.reconsentflag, cpq.s_questionid, cpq.questiontext, cpq.answergranted, cpq.answereditorstyleid, cpq.consenttype, cpq.usersequence \nFROM s_clinicalprotocolquestion cpq, s_participant p, s_clinicalprotocol cp\nWHERE p.clinicalprotocolid = cp.s_clinicalprotocolid and\np.clinicalprotocolrevision = cp.s_clinicalprotocolrevision and\ncpq.s_clinicalprotocolid = cp.s_clinicalprotocolid and\ncpq.s_clinicalprotocolrevision = cp.s_clinicalprotocolrevision and\ncpq.s_clinicalprotocolversionid = cp.s_clinicalprotocolversionid and\ncp.versionstatus = 'C' and\n( cpq.cpcohortid is null or cpq.cpcohortid = p.cpcohortid )\nand p.s_participantid = ?";
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{participantid});
    }

    private void addConsentQuestionsFromCP(String participantid, String clinicalprotocolid, String versionid, String previousrevision, String revision, String consentinstance) throws SapphireException {
        DataSet consentQuestions = this.getConsentQuestionsFromProtocol(participantid);
        boolean reconsentsIncluded = false;
        if (consentQuestions != null && consentQuestions.getRowCount() > 0) {
            int i;
            for (int i2 = 0; i2 < consentQuestions.getRowCount(); ++i2) {
                if (!"N".equals(consentQuestions.getString(i2, "reconsentflag", "N"))) continue;
                consentQuestions.setString(i2, "reconsentflag", "N");
            }
            String reconsentflag = consentQuestions.getColumnValues("reconsentflag", ";");
            DataSet itemsToReconsent = null;
            if (previousrevision.length() > 0 && reconsentflag.contains("N")) {
                String sql = "select questionid, answer from s_participantconsentitem where s_participantid = ? and answerstatus != 'P' and s_participantconsentid in\n(select s_participantconsentid from s_participantconsent where clinicalprotocolrevision = ? and s_participantid = ? )";
                itemsToReconsent = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{participantid, previousrevision, participantid});
                if (itemsToReconsent != null && itemsToReconsent.getRowCount() > 0) {
                    for (i = 0; i < itemsToReconsent.getRowCount(); ++i) {
                        int match = consentQuestions.findRow("s_questionid", itemsToReconsent.getString(i, "questionid"));
                        if (match <= -1 || !"N".equals(consentQuestions.getString(match, "reconsentflag", "N"))) continue;
                        consentQuestions.setString(match, "answer", itemsToReconsent.getString(i, "answer"));
                        reconsentsIncluded = true;
                    }
                }
            }
            PropertyList addProps = new PropertyList();
            addProps.setProperty("sdcid", "LV_ParticipantConsent");
            addProps.setProperty("keyid1", participantid);
            addProps.setProperty("keyid2", consentinstance);
            addProps.setProperty("consentinstancestatus", "P");
            addProps.setProperty("clinicalprotocolid", clinicalprotocolid);
            addProps.setProperty("clinicalprotocolversionid", versionid);
            addProps.setProperty("clinicalprotocolrevision", revision);
            this.getActionProcessor().processAction("AddSDI", "1", addProps);
            DataSet addDetails = new DataSet();
            addDetails.addColumnValues("keyid1", 0, participantid, ";");
            addDetails.addColumnValues("keyid2", 0, consentinstance, ";");
            addDetails.addColumnValues("questionid", 0, consentQuestions.getColumnValues("s_questionid", ";"), ";");
            addDetails.addColumnValues("questiontext", 0, consentQuestions.getColumnValues("questiontext", ";"), ";");
            addDetails.addColumnValues("answergranted", 0, consentQuestions.getColumnValues("answergranted", ";"), ";");
            addDetails.addColumnValues("answereditorstyleid", 0, consentQuestions.getColumnValues("answereditorstyleid", ";"), ";");
            addDetails.addColumnValues("consenttype", 0, consentQuestions.getColumnValues("consenttype", ";"), ";");
            addDetails.addColumnValues("usersequence", 0, consentQuestions.getColumnValues("usersequence", ";"), ";");
            addDetails.addColumnValues("answerstatus", 0, "P", ";");
            if (reconsentsIncluded) {
                addDetails.addColumnValues("answer", 0, consentQuestions.getColumnValues("answer", ";"), ";");
            }
            addDetails.padColumn("keyid1");
            addDetails.padColumn("keyid2");
            addDetails.padColumn("answerstatus");
            PropertyList detailProps = new PropertyList();
            detailProps.setProperty("sdcid", "LV_ParticipantConsent");
            detailProps.setProperty("linkid", "consent responses");
            for (i = 0; i < addDetails.getRowCount(); ++i) {
                detailProps.setProperty("keyid1", addDetails.getString(i, "keyid1", ""));
                detailProps.setProperty("keyid2", addDetails.getString(i, "keyid2", ""));
                detailProps.setProperty("questionid", addDetails.getString(i, "questionid", ""));
                detailProps.setProperty("questiontext", addDetails.getString(i, "questiontext", ""));
                detailProps.setProperty("answergranted", addDetails.getString(i, "answergranted", ""));
                detailProps.setProperty("answereditorstyleid", addDetails.getString(i, "answereditorstyleid", ""));
                detailProps.setProperty("consenttype", addDetails.getString(i, "consenttype", ""));
                detailProps.setProperty("answerstatus", addDetails.getString(i, "answerstatus", ""));
                detailProps.setProperty("usersequence", addDetails.getString(i, "usersequence", ""));
                if (reconsentsIncluded) {
                    detailProps.setProperty("answer", addDetails.getString(i, "answer", ""));
                }
                this.getActionProcessor().processAction("AddSDIDetail", "1", detailProps);
            }
        }
    }

    private void addConsentQuestionsFromStudy(String participantid, DataSet consentQuestions, String consentinstance) throws SapphireException {
        if (consentQuestions != null && consentQuestions.getRowCount() > 0) {
            PropertyList addProps = new PropertyList();
            addProps.setProperty("sdcid", "LV_ParticipantConsent");
            addProps.setProperty("keyid1", participantid);
            addProps.setProperty("keyid2", consentinstance);
            addProps.setProperty("consentinstancestatus", "P");
            this.getActionProcessor().processAction("AddSDI", "1", addProps);
            DataSet addDetails = new DataSet();
            addDetails.addColumnValues("keyid1", 0, participantid, ";");
            addDetails.addColumnValues("keyid2", 0, consentinstance, ";");
            addDetails.addColumnValues("questionid", 0, consentQuestions.getColumnValues("s_questionid", ";"), ";");
            addDetails.addColumnValues("questiontext", 0, consentQuestions.getColumnValues("questiontext", ";"), ";");
            addDetails.addColumnValues("answergranted", 0, consentQuestions.getColumnValues("answergranted", ";"), ";");
            addDetails.addColumnValues("answereditorstyleid", 0, consentQuestions.getColumnValues("answereditorstyleid", ";"), ";");
            addDetails.addColumnValues("consenttype", 0, consentQuestions.getColumnValues("consenttype", ";"), ";");
            addDetails.addColumnValues("usersequence", 0, consentQuestions.getColumnValues("usersequence", ";"), ";");
            addDetails.addColumnValues("answerstatus", 0, "P", ";");
            addDetails.padColumn("keyid1");
            addDetails.padColumn("keyid2");
            addDetails.padColumn("answerstatus");
            PropertyList detailProps = new PropertyList();
            detailProps.setProperty("sdcid", "LV_ParticipantConsent");
            detailProps.setProperty("linkid", "consent responses");
            for (int i = 0; i < addDetails.getRowCount(); ++i) {
                detailProps.setProperty("keyid1", addDetails.getString(i, "keyid1", ""));
                detailProps.setProperty("keyid2", addDetails.getString(i, "keyid2", ""));
                detailProps.setProperty("questionid", addDetails.getString(i, "questionid", ""));
                detailProps.setProperty("questiontext", addDetails.getString(i, "questiontext", ""));
                detailProps.setProperty("answergranted", addDetails.getString(i, "answergranted", ""));
                detailProps.setProperty("answereditorstyleid", addDetails.getString(i, "answereditorstyleid", ""));
                detailProps.setProperty("consenttype", addDetails.getString(i, "consenttype", ""));
                detailProps.setProperty("answerstatus", addDetails.getString(i, "answerstatus", ""));
                detailProps.setProperty("usersequence", addDetails.getString(i, "usersequence", ""));
                this.getActionProcessor().processAction("AddSDIDetail", "1", detailProps);
            }
        }
    }

    private boolean isStudyProtocolDriven(String sstudyid) {
        if (!this.studyCache.containsKey(sstudyid)) {
            this.studyCache.put(sstudyid, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "clinicalflag", "s_studyid=?", new String[]{sstudyid}));
        }
        return "Y".equalsIgnoreCase(this.studyCache.get(sstudyid));
    }

    private void validateAmbigousSubjectid(DataSet primary, boolean participantStudySubjectOnly) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            DataSet ds;
            String subjectid = primary.getValue(i, "subjectid", "");
            String externalparticipantid = primary.getValue(i, "externalparticipantid", "");
            String sstudyid = primary.getValue(i, "sstudyid", "");
            String studysiteid = primary.getValue(i, "studysiteid", "");
            if (StringUtil.getLen(externalparticipantid) <= 0L) continue;
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select distinct( subjectid ) subjectid");
            sql.append(" from s_participant");
            sql.append(" where sstudyid = ").append(safeSQL.addVar(sstudyid));
            sql.append(" and externalparticipantid = ").append(safeSQL.addVar(externalparticipantid));
            sql.append(" and subjectid is not null");
            if (!participantStudySubjectOnly && StringUtil.getLen(studysiteid) > 0L) {
                sql.append(" and studysiteid = ").append(safeSQL.addVar(studysiteid));
            }
            if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) == null || ds.size() <= 0) continue;
            this.throwError(this.getTranslationProcessor().translate("External Participant ID") + " " + this.getTranslationProcessor().translate("Update Error"), "VALIDATION", this.getTranslationProcessor().translate("Multiple subjects found for combination of this Study, Clinical Site and External Participant ID ") + " (" + this.getTranslationProcessor().translate("Study") + ": " + sstudyid + ", " + this.getTranslationProcessor().translate("Site") + ": " + OpalUtil.getColumnValue(this.getQueryProcessor(), "s_studysite", "studysitedesc", "s_studysiteid=?", new String[]{studysiteid}) + ", " + this.getTranslationProcessor().translate("External Participant ID") + ": " + externalparticipantid + ")");
        }
    }
}

