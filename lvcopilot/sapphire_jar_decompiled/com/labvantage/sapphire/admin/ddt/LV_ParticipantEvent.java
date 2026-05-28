/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.FormUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_ParticipantEvent
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    public static final String SDCID = "LV_ParticipantEvent";
    HashMap participantmap = new HashMap();

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String partEventIds = "";
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "eventstatus")) continue;
            partEventIds = partEventIds.length() > 0 ? partEventIds + ";" + primary.getValue(i, "s_participanteventid", ";") : primary.getValue(i, "s_participanteventid", ";");
        }
        if (partEventIds.length() > 0) {
            DataSet ds = BusinessRulesUtil.findFormFromProtocolUsingEvent(partEventIds, this.getQueryProcessor());
            this.addBlankDocumentsToEvent(ds, "edit");
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.addBlankDocuments(primary);
        DataSet ds = BusinessRulesUtil.findFormFromProtocolUsingEvent(primary.getColumnValues("s_participanteventid", ";"), this.getQueryProcessor());
        this.addBlankDocumentsToEvent(ds, "add");
    }

    private void addBlankDocumentsToEvent(DataSet ds, String mode) throws SapphireException {
        if (ds.size() > 0) {
            DataSet formsDataSet = new DataSet();
            for (int i = 0; i < ds.size(); ++i) {
                String rule = ds.getValue(i, "formrule", "");
                if (ds.getValue(i, "parentparticipanteventid", "").length() != 0 || rule.toLowerCase().indexOf("visit") == -1 || rule.indexOf(":") == -1) continue;
                String ruledef = rule.split(":")[1].trim();
                if (mode.equalsIgnoreCase("add") && ruledef.toLowerCase().equalsIgnoreCase("creation")) {
                    formsDataSet.copyRow(ds, i, 1);
                    continue;
                }
                if (ds.getValue(i, "eventstatus").equalsIgnoreCase("completed") && ruledef.toLowerCase().indexOf("completion") != -1) {
                    formsDataSet.copyRow(ds, i, 1);
                    continue;
                }
                if (!ds.getValue(i, "eventstatus").equalsIgnoreCase("missed") || ruledef.toLowerCase().indexOf("missed") == -1) continue;
                formsDataSet.copyRow(ds, i, 1);
            }
            try {
                FormUtil.addBlankDocument(formsDataSet, this.getActionProcessor(), SDCID, "s_participanteventid");
            }
            catch (ActionException e) {
                this.logger.error("Unable to add Blank Document to Sample " + formsDataSet.getColumnValues("s_sampleid", ";") + ". Exception raised is: " + e.getMessage());
                throw new SapphireException(e);
            }
        }
    }

    private void addBlankDocuments(DataSet primary) throws SapphireException {
        for (int i = 0; i < primary.size(); ++i) {
            String participantid = primary.getValue(i, "participantid");
            String parentparticipanteventid = primary.getValue(i, "parentparticipanteventid");
            PropertyList participantinfo = this.getParticipantInfo(participantid);
            String studyid = participantinfo.getProperty("sstudyid");
            String studysiteid = participantinfo.getProperty("studysiteid");
            StringBuilder sql = new StringBuilder();
            String eventdefid = primary.getValue(i, "eventdefid", "");
            if (eventdefid.length() <= 0) continue;
            SafeSQL safeSQL = new SafeSQL();
            sql.setLength(0);
            sql.append("select distinct fr.formid, fr.formversionid, fr.forminstance,");
            sql.append(" (select max(form.formversionid) from form where form.formid = fr.formid ) defaultformversionid, fr.formrule,");
            sql.append(" p.sstudyid, p.studysiteid, p.s_participantid,");
            sql.append(" pe.s_participanteventid, pe.parentparticipanteventid, p.subjectid, p.clinicalprotocolrevision, fr.usersequence");
            sql.append(" from sdiformrule fr, s_participantevent pe, s_participant p");
            sql.append(" where fr.sdcid = 'LV_EventDef'");
            sql.append(" and fr.keyid1 = pe.eventdefid AND pe.s_participanteventid = ").append(safeSQL.addVar(primary.getValue(i, "s_participanteventid")));
            sql.append(" and pe.participantid = p.s_participantid ");
            sql.append(" order by fr.usersequence");
            DataSet formsds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (formsds.size() <= 0) continue;
            DataSet formsDataSet = new DataSet();
            for (int j = 0; j < formsds.size(); ++j) {
                String ruledef;
                String rule = formsds.getValue(j, "formrule", "");
                if (parentparticipanteventid.length() != 0 || !rule.toLowerCase().contains("visit") || !rule.contains(":") || !(ruledef = rule.split(":")[1].trim()).toLowerCase().equalsIgnoreCase("creation")) continue;
                formsDataSet.copyRow(formsds, j, 1);
            }
            try {
                FormUtil.addBlankDocument(formsDataSet, this.getActionProcessor(), SDCID, primary.getValue(i, "s_participanteventid"), studyid, studysiteid);
                continue;
            }
            catch (ActionException e) {
                this.logger.error("Unable to add Blank Document to Sample " + formsDataSet.getColumnValues("s_sampleid", ";") + ". Exception raised is: " + e.getMessage());
                throw new SapphireException(e);
            }
        }
    }

    public PropertyList getParticipantInfo(String participantid) {
        if (!this.participantmap.containsKey(participantid)) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select s_participantid, sstudyid, studysiteid, clinicalprotocolid, clinicalprotocolversionid,");
            sql.append(" clinicalprotocolrevision, cpcohortid, subjectid, externalparticipantid from s_participant");
            sql.append(" where s_participantid = ").append(safeSQL.addVar(participantid));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                PropertyList list = new PropertyList();
                list.setProperty("s_participantid", ds.getValue(0, "s_participantid"));
                list.setProperty("sstudyid", ds.getValue(0, "sstudyid"));
                list.setProperty("studysiteid", ds.getValue(0, "studysiteid"));
                list.setProperty("clinicalprotocolid", ds.getValue(0, "clinicalprotocolid"));
                list.setProperty("clinicalprotocolversionid", ds.getValue(0, "clinicalprotocolversionid", "1"));
                list.setProperty("clinicalprotocolrevision", ds.getValue(0, "clinicalprotocolrevision"));
                list.setProperty("cpcohortid", ds.getValue(0, "cpcohortid"));
                list.setProperty("subjectid", ds.getValue(0, "subjectid"));
                list.setProperty("externalparticipantid", ds.getValue(0, "externalparticipantid"));
                this.participantmap.put(participantid, list);
            } else {
                this.participantmap.put(participantid, new PropertyList());
            }
        }
        return (PropertyList)this.participantmap.get(participantid);
    }

    @Override
    public void preEdit(SDIData sdidata, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdidata.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String participantId;
            if (this.hasPrimaryValueChanged(primary, i, "eventlabel")) {
                throw new SapphireException("VALIDATION", "", this.getTranslationProcessor().translate("Event Label can only be modified at Event definition"));
            }
            String siteId = this.getOldPrimaryValue(primary, i, "sstudysiteid");
            if (!OpalUtil.isEmpty(siteId) || !OpalUtil.isNotEmpty(participantId = this.getOldPrimaryValue(primary, i, "participantid")) || !OpalUtil.isNotEmpty(siteId = this.getSiteId(participantId))) continue;
            primary.setString(i, "sstudysiteid", siteId);
        }
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        TranslationProcessor tp = this.getTranslationProcessor();
        String participantEventId = StringUtil.replaceAll(actionProps.getProperty("keyid1", ""), ";", "','");
        boolean eventHasSamples = BusinessRulesUtil.checkIfEventRefSampleFamily(participantEventId, this.database);
        if (eventHasSamples) {
            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Event can not be deleted. SampleFamily is referencing the event."));
        }
    }

    private String getSiteId(String participantid) throws SapphireException {
        String siteId = "";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select studysiteid from s_participant where s_participantid = ?", (Object[])new String[]{participantid});
        if (ds != null && ds.size() > 0) {
            siteId = ds.getString(0, "studysiteid");
        }
        return siteId;
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

