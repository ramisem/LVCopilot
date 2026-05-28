/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.FormUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.AddSDI;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.actions.workitem.AddSDIWorkItem;
import com.labvantage.sapphire.admin.ddt.RuleUtil;
import com.labvantage.sapphire.admin.ddt.rules.AmbiguousSubjectRule;
import com.labvantage.sapphire.admin.ddt.rules.BaseBioBankRule;
import com.labvantage.sapphire.admin.ddt.rules.COCRule;
import com.labvantage.sapphire.admin.ddt.rules.GLPRule;
import com.labvantage.sapphire.admin.ddt.rules.HipaaRule;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.action.BaseSDCRules;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_SampleFamily
extends BaseSDCRules {
    protected String LABVANTAGE_CVS_ID = "$Revision: 89527 $";
    public static final String SDCID = "LV_SampleFamily";
    Map<String, String> eventDefLabelMap = new HashMap<String, String>();
    Map<String, String> isProtocolCache = new HashMap<String, String>();
    Map<String, Map<String, String>> participantCache = new HashMap<String, Map<String, String>>();
    Map<String, Object> eventCache = new HashMap<String, Object>();
    private HashMap sampleFamilyStudyMap = new HashMap();
    private HashMap restClassCOCFlagMap;
    private HashMap restClassDRFlagMap;
    private HashMap studyCOCFlagMap;
    private HashMap studyDRFlagMap;

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        if (this.connectionInfo.hasModule("SMS")) {
            primary.addColumn("cocflag", 0);
            primary.addColumn("restrictionsflag", 0);
            for (int i = 0; i < primary.size(); ++i) {
                String restclassid = primary.getValue(i, "restrictclassid");
                if (StringUtil.getLen(restclassid) > 0L) {
                    primary.setValue(i, "cocflag", this.getResctrictionClassCOCFlag(restclassid));
                    primary.setValue(i, "restrictionsflag", this.getResctrictionClassDRFlag(restclassid));
                } else {
                    String studyid = primary.getValue(i, "sstudyid");
                    if (StringUtil.getLen(studyid) > 0L) {
                        primary.setValue(i, "cocflag", this.getStudyCOCFlag(studyid));
                        primary.setValue(i, "restrictionsflag", this.getStudyDRFlag(studyid));
                    } else {
                        primary.setValue(i, "cocflag", "Y");
                        primary.setValue(i, "restrictionsflag", "Y");
                    }
                }
                String eventdefid = primary.getString(i, "eventdefid", "");
                if (eventdefid.length() <= 0) continue;
                primary.setString(i, "clinicalevent", this.getEventDefLabel(eventdefid));
            }
        }
        this.processKitUsage(primary, true);
    }

    private String getEventDefLabel(String eventdefid) {
        DataSet ds;
        if (!this.eventDefLabelMap.containsKey(eventdefid) && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_eventdefid, eventdeflabel, parenteventdefid, (select p.eventdeflabel from s_eventdef p where p.s_eventdefid = s_eventdef.parenteventdefid) parenteventdeflabel from s_eventdef where s_eventdefid = ?", (Object[])new String[]{eventdefid})) != null && ds.size() > 0) {
            String eventdeflabel = ds.getString(0, "eventdeflabel", "");
            String parenteventdeflabel = ds.getString(0, "parenteventdeflabel", "");
            if (parenteventdeflabel.length() > 0) {
                this.eventDefLabelMap.put(eventdefid, parenteventdeflabel + " > " + eventdeflabel);
            } else {
                this.eventDefLabelMap.put(eventdefid, eventdeflabel);
            }
        }
        return this.eventDefLabelMap.get(eventdefid);
    }

    private void processKitUsage(DataSet primary, boolean add) throws ActionException {
        if (primary.isValidColumn("kittrackitem")) {
            HashSet<String> usedKitSet = new HashSet<String>();
            for (int i = 0; i < primary.size(); ++i) {
                String kittrackitem = null;
                if (add) {
                    kittrackitem = primary.getString(i, "kittrackitem");
                } else if (this.hasPrimaryValueChanged(primary, i, "kittrackitem")) {
                    kittrackitem = primary.getString(i, "kittrackitem");
                }
                if (!OpalUtil.isNotEmpty(kittrackitem)) continue;
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitemid, trackitemstatus from trackitem where trackitemid = " + safeSQL.addVar(kittrackitem) + " or trackitemlabel = " + safeSQL.addVar(kittrackitem), safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    if ("Valid".equals(ds.getString(0, "trackitemstatus")) || "Shipped".equals(ds.getString(0, "trackitemstatus"))) {
                        usedKitSet.add(ds.getString(0, "trackitemid"));
                    }
                    primary.setString(i, "kittrackitem", ds.getString(0, "trackitemid"));
                    continue;
                }
                primary.setString(i, "kittrackitem", "(null)");
            }
            if (usedKitSet.size() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "TrackItemSDC");
                props.setProperty("keyid1", OpalUtil.toDelimitedString(usedKitSet, ";"));
                props.setProperty("trackitemstatus", "Used");
                props.setProperty("__sdcruleignore", "Y");
                this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
            }
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
        boolean isStudyClinical = "Y".equalsIgnoreCase(this.getQueryProcessor().getPreparedSqlDataSet("select s.clinicalflag from s_study s, s_samplefamily sf where sf.sstudyid = s.s_studyid and sf.s_samplefamilyid = ?", (Object[])new String[]{primary.getString(0, "s_samplefamilyid")}).getString(0, "clinicalflag", "N"));
        if (!isStudyClinical) {
            this.addSubjectDocuments(primary);
            if (!"Y".equals(actionProps.getProperty("__ignoreparticipant"))) {
                this.checkAndAddParticipant(primary);
            }
        } else {
            this.addBlankDocuments(primary);
            this.addBlankDocumentsFromEventDef(primary);
        }
        if (this.connectionInfo.hasModule("SMS")) {
            this.checkHipaaRule(primary, forceUpdate);
            this.checkAmbiguousSubjectRule(primary);
        }
    }

    private void addBlankDocumentsFromEventDef(DataSet primary) {
        String sampleFamilyIds = "";
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.getValue(i, "participanteventid", "").length() <= 0 || this.getOldPrimaryValue(primary, i, "participanteventid").equals(primary.getValue(i, "participanteventid", ""))) continue;
            sampleFamilyIds = sampleFamilyIds.length() > 0 ? sampleFamilyIds + ";" + primary.getValue(i, "s_samplefamilyid", "") : primary.getValue(i, "s_samplefamilyid", "");
        }
        if (sampleFamilyIds.length() > 0) {
            DataSet formds = BusinessRulesUtil.findFormFromEventDefUsingSampleId(sampleFamilyIds, this.getQueryProcessor());
            DataSet formsDataSet = new DataSet();
            if (formds != null && formds.size() > 0) {
                for (int i = 0; i < formds.size(); ++i) {
                    String ruledef;
                    String rule = formds.getValue(i, "formrule", "");
                    if (!rule.toLowerCase().contains("sample") || !rule.contains(":") || !(ruledef = formds.getValue(i, "formrule").split(":")[1].trim()).toLowerCase().contains("allocation")) continue;
                    formsDataSet.copyRow(formds, i, 1);
                }
            }
            if (formsDataSet.size() > 0) {
                try {
                    FormUtil.addBlankDocument(formsDataSet, this.getActionProcessor(), SDCID, "s_samplefamilyid");
                }
                catch (ActionException e) {
                    this.logger.error("Unable to add Blank Document to Sample " + formsDataSet.getColumnValues("s_sampleid", ";") + ". Exception raised is: " + e.getMessage(), e);
                }
            }
        }
    }

    private void checkAndAddParticipant(DataSet primary) throws SapphireException {
        boolean participantStudySubjectOnly = "Study-Subject".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("participantcreationrule"));
        HashSet<String> subjectModifiedSet = new HashSet<String>();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (participantStudySubjectOnly) {
                if (!this.hasPrimaryValueChanged(primary, i, "sstudyid") && !this.hasPrimaryValueChanged(primary, i, "subjectid") && !this.hasPrimaryValueChanged(primary, i, "externalsubject")) continue;
                subjectModifiedSet.add(primary.getString(i, "s_samplefamilyid"));
                continue;
            }
            if (!this.hasPrimaryValueChanged(primary, i, "sstudyid") && !this.hasPrimaryValueChanged(primary, i, "studysiteid") && !this.hasPrimaryValueChanged(primary, i, "subjectid") && !this.hasPrimaryValueChanged(primary, i, "externalsubject")) continue;
            subjectModifiedSet.add(primary.getString(i, "s_samplefamilyid"));
        }
        if (subjectModifiedSet.size() > 0) {
            DataSet ds;
            DataSet addParticipantList = new DataSet();
            StringBuilder sql = new StringBuilder();
            sql.append("select s_samplefamily.s_samplefamilyid, s_samplefamily.sstudyid, s_samplefamily.studysiteid, s_samplefamily.subjectid, s_samplefamily.externalsubject,");
            sql.append(" s_samplefamily.participantid sfparticipantid, s_studysite.clinicalprotocolid, s_studysite.clinicalprotocolversionid, s_studysite.clinicalprotocolrevision");
            if (this.getConnectionProcessor().isOra()) {
                if (participantStudySubjectOnly) {
                    sql.append(" ,(select p.s_participantid from s_participant p where p.sstudyid = s_samplefamily.sstudyid and p.subjectid = s_samplefamily.subjectid and rownum = 1) participantid");
                } else {
                    sql.append(" ,(select p.s_participantid from s_participant p where p.sstudyid = s_samplefamily.sstudyid and p.studysiteid = s_samplefamily.studysiteid and p.subjectid = s_samplefamily.subjectid and rownum = 1) participantid");
                }
            } else if (participantStudySubjectOnly) {
                sql.append(" ,(select top(1) p.s_participantid from s_participant p where p.sstudyid = s_samplefamily.sstudyid and p.subjectid = s_samplefamily.subjectid) participantid");
            } else {
                sql.append(" ,(select top(1) p.s_participantid from s_participant p where p.sstudyid = s_samplefamily.sstudyid and p.subjectid = s_samplefamily.subjectid and p.studysiteid = s_samplefamily.studysiteid) participantid");
            }
            sql.append(" from s_samplefamily left outer join s_studysite on s_studysite.s_studysiteid = s_samplefamily.studysiteid");
            if (subjectModifiedSet.size() > 1000) {
                String rsetid = this.getDAMProcessor().createRSet(SDCID, OpalUtil.toDelimitedString(subjectModifiedSet, ";"), null, null);
                sql.append(" where s_samplefamilyid in (select r.keyid1 from rsetitems r where r.rsetid = ?)");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append(" where s_samplefamilyid in (").append(safeSQL.addIn(subjectModifiedSet)).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (ds != null) {
                int i;
                DataSet addParticipantDS = new DataSet();
                HashSet<String> editParticipantSet = new HashSet<String>();
                DataSet editParticipantDS = new DataSet();
                DataSet editFamilyDS = new DataSet();
                HashMap<String, String> filter = new HashMap<String, String>();
                for (i = 0; i < ds.size(); ++i) {
                    int row;
                    String samplefamilyid = ds.getString(i, "s_samplefamilyid");
                    String studyid = ds.getString(i, "sstudyid", "");
                    String siteid = ds.getString(i, "studysiteid", "");
                    String subjectid = ds.getString(i, "subjectid", "");
                    String externalsubject = ds.getString(i, "externalsubject", "");
                    String participantID = ds.getString(i, "participantid", "");
                    String existingparticipantid = ds.getString(i, "sfparticipantid", "");
                    if (participantStudySubjectOnly) {
                        if (studyid.length() > 0 && subjectid.length() > 0) {
                            if (participantID.length() == 0) {
                                int row2 = addParticipantList.addRow();
                                addParticipantList.setString(row2, "samplefamilyid", samplefamilyid);
                                addParticipantList.setString(row2, "studyid", studyid);
                                addParticipantList.setString(row2, "siteid", siteid);
                                addParticipantList.setString(row2, "subjectid", subjectid);
                                filter.clear();
                                filter.put("studyid", studyid);
                                filter.put("subjectid", subjectid);
                                row2 = addParticipantDS.findRow(filter);
                                if (row2 != -1) continue;
                                row2 = addParticipantDS.addRow();
                                addParticipantDS.setString(row2, "studyid", studyid);
                                addParticipantDS.setString(row2, "siteid", siteid);
                                addParticipantDS.setString(row2, "subjectid", subjectid);
                                addParticipantDS.setString(row2, "externalsubject", externalsubject);
                                addParticipantDS.setString(row2, "clinicalprotocolid", ds.getString(i, "clinicalprotocolid", ""));
                                addParticipantDS.setString(row2, "clinicalprotocolversionid", ds.getString(i, "clinicalprotocolversionid", ""));
                                addParticipantDS.setString(row2, "clinicalprotocolrevision", ds.getString(i, "clinicalprotocolrevision", ""));
                                continue;
                            }
                            if (!existingparticipantid.equals(participantID)) {
                                int row3 = editFamilyDS.addRow();
                                editFamilyDS.setString(row3, "samplefamilyid", samplefamilyid);
                                editFamilyDS.setString(row3, "participantid", participantID);
                                continue;
                            }
                            String oldExternalSubject = this.getOldPrimaryValue(primary, i, "externalsubject");
                            if (externalsubject.equals(oldExternalSubject) || !editParticipantSet.add(participantID)) continue;
                            int row4 = editParticipantDS.addRow();
                            editParticipantDS.setString(row4, "s_participantid", participantID);
                            editParticipantDS.setString(row4, "externalparticipantid", externalsubject);
                            continue;
                        }
                        filter.clear();
                        filter.put("s_samplefamilyid", samplefamilyid);
                        int prow = primary.findRow(filter);
                        if (prow == -1) continue;
                        String primaryparticipantid = primary.getString(prow, "participantid", "");
                        if (existingparticipantid.length() <= 0 || existingparticipantid.equals(primaryparticipantid)) continue;
                        row = editFamilyDS.addRow();
                        editFamilyDS.setString(row, "samplefamilyid", samplefamilyid);
                        editFamilyDS.setString(row, "participantid", "");
                        continue;
                    }
                    if (studyid.length() > 0 && siteid.length() > 0 && subjectid.length() > 0) {
                        if (participantID.length() == 0) {
                            int row5 = addParticipantList.addRow();
                            addParticipantList.setString(row5, "samplefamilyid", samplefamilyid);
                            addParticipantList.setString(row5, "studyid", studyid);
                            addParticipantList.setString(row5, "siteid", siteid);
                            addParticipantList.setString(row5, "subjectid", subjectid);
                            filter.clear();
                            filter.put("studyid", studyid);
                            filter.put("siteid", siteid);
                            filter.put("subjectid", subjectid);
                            row5 = addParticipantDS.findRow(filter);
                            if (row5 != -1) continue;
                            row5 = addParticipantDS.addRow();
                            addParticipantDS.setString(row5, "studyid", studyid);
                            addParticipantDS.setString(row5, "siteid", siteid);
                            addParticipantDS.setString(row5, "subjectid", subjectid);
                            addParticipantDS.setString(row5, "externalsubject", externalsubject);
                            addParticipantDS.setString(row5, "clinicalprotocolid", ds.getString(i, "clinicalprotocolid", ""));
                            addParticipantDS.setString(row5, "clinicalprotocolversionid", ds.getString(i, "clinicalprotocolversionid", ""));
                            addParticipantDS.setString(row5, "clinicalprotocolrevision", ds.getString(i, "clinicalprotocolrevision", ""));
                            continue;
                        }
                        if (!existingparticipantid.equals(participantID)) {
                            int row6 = editFamilyDS.addRow();
                            editFamilyDS.setString(row6, "samplefamilyid", samplefamilyid);
                            editFamilyDS.setString(row6, "participantid", participantID);
                            continue;
                        }
                        String oldExternalSubject = this.getOldPrimaryValue(primary, i, "externalsubject");
                        if (externalsubject.equals(oldExternalSubject) || !editParticipantSet.add(participantID)) continue;
                        int row7 = editParticipantDS.addRow();
                        editParticipantDS.setString(row7, "s_participantid", participantID);
                        editParticipantDS.setString(row7, "externalparticipantid", externalsubject);
                        continue;
                    }
                    filter.clear();
                    filter.put("s_samplefamilyid", samplefamilyid);
                    int prow = primary.findRow(filter);
                    if (prow == -1) continue;
                    String primaryparticipantid = primary.getString(prow, "participantid", "");
                    if (existingparticipantid.length() <= 0 || existingparticipantid.equals(primaryparticipantid)) continue;
                    row = editFamilyDS.addRow();
                    editFamilyDS.setString(row, "samplefamilyid", samplefamilyid);
                    editFamilyDS.setString(row, "participantid", "");
                }
                if (editParticipantDS.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_Participant");
                    props.setProperty("keyid1", editParticipantDS.getColumnValues("s_participantid", ";"));
                    props.setProperty("externalparticipantid", editParticipantDS.getColumnValues("externalparticipantid", ";"));
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
                if (addParticipantList.size() > 0) {
                    this.createParticipants(addParticipantDS);
                    for (i = 0; i < addParticipantList.size(); ++i) {
                        int findrow;
                        filter.clear();
                        filter.put("studyid", addParticipantList.getString(i, "studyid"));
                        filter.put("subjectid", addParticipantList.getString(i, "subjectid"));
                        if (!participantStudySubjectOnly) {
                            filter.put("siteid", addParticipantList.getString(i, "siteid"));
                        }
                        if ((findrow = addParticipantDS.findRow(filter)) == -1) continue;
                        int row = editFamilyDS.addRow();
                        editFamilyDS.setString(row, "samplefamilyid", addParticipantList.getString(i, "samplefamilyid"));
                        editFamilyDS.setString(row, "participantid", addParticipantDS.getString(findrow, "participantid"));
                    }
                }
                if (editFamilyDS.size() > 0) {
                    PropertyList editProps = new PropertyList();
                    editProps.setProperty("sdcid", SDCID);
                    editProps.setProperty("keyid1", editFamilyDS.getColumnValues("samplefamilyid", ";"));
                    editProps.setProperty("participantid", editFamilyDS.getColumnValues("participantid", ";"));
                    editProps.setProperty("__participanteditflag", "Y");
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), editProps);
                }
            }
        }
    }

    private void createParticipants(DataSet addParticipantDS) throws SapphireException {
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_Participant");
        props.setProperty("copies", String.valueOf(addParticipantDS.size()));
        props.setProperty("sstudyid", addParticipantDS.getColumnValues("studyid", ";"));
        props.setProperty("studysiteid", addParticipantDS.getColumnValues("siteid", ";"));
        props.setProperty("subjectid", addParticipantDS.getColumnValues("subjectid", ";"));
        props.setProperty("externalparticipantid", addParticipantDS.getColumnValues("externalsubject", ";"));
        props.setProperty("clinicalprotocolid", addParticipantDS.getColumnValues("clinicalprotocolid", ";"));
        props.setProperty("clinicalprotocolversionid", addParticipantDS.getColumnValues("clinicalprotocolversionid", ";"));
        props.setProperty("clinicalprotocolrevision", addParticipantDS.getColumnValues("clinicalprotocolrevision", ";"));
        props.setProperty("participantstatus", "Enrolled");
        props.setProperty("enrolldt", "n");
        this.getActionProcessor().processAction("AddSDI", "1", props);
        addParticipantDS.addColumnValues("participantid", 0, props.getProperty("newkeyid1", ""), ";");
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__participanteditflag"))) {
            return;
        }
        if ("Y".equals(actionProps.getProperty("__studyswitchedit"))) {
            return;
        }
        long start = System.currentTimeMillis();
        Trace.logInfo("START: LV_SampleFamily.preEdit() [" + start + "]");
        DataSet primary = sdiData.getDataset("primary");
        ArrayList<String> eventDefList = new ArrayList<String>();
        if (actionProps.containsKey("_eventdefid")) {
            eventDefList.addAll(OpalUtil.toList(actionProps.getProperty("_eventdefid"), ";"));
        }
        HashMap<String, String> sampleFamilyEventDefMap = new HashMap<String, String>();
        if (actionProps.containsKey("_eventdefid")) {
            DataSet eventds;
            if (primary.size() > 1000) {
                String rsetid = this.getDAMProcessor().createRSet(SDCID, primary.getColumnValues("s_samplefamilyid", ";"), null, null);
                eventds = this.getQueryProcessor().getPreparedSqlDataSet("select s_samplefamily.s_samplefamilyid, s_samplefamily.participanteventid, s_participantevent.eventdefid from s_samplefamily left outer join s_participantevent on s_participantevent.s_participanteventid = s_samplefamily.participanteventid where s_samplefamily.participanteventid is not null and s_samplefamily.s_samplefamilyid in (select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "select s_samplefamily.s_samplefamilyid, s_samplefamily.participanteventid, s_participantevent.eventdefid from s_samplefamily left outer join s_participantevent on s_participantevent.s_participanteventid = s_samplefamily.participanteventid where s_samplefamily.participanteventid is not null and s_samplefamily.s_samplefamilyid in (" + safeSQL.addIn(primary.getColumnValues("s_samplefamilyid", "','")) + ")";
                eventds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
            if (eventds != null) {
                for (int i = 0; i < eventds.size(); ++i) {
                    sampleFamilyEventDefMap.put(eventds.getString(i, "s_samplefamilyid"), eventds.getString(i, "eventdefid", ""));
                }
            }
        }
        if (actionProps.containsKey("_clinicalevent")) {
            primary.addColumnValues("clinicalevent", 0, actionProps.getProperty("_clinicalevent"), ";");
        }
        boolean participantStudySubjectOnly = "Study-Subject".equals(this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom").getProperty("participantcreationrule"));
        HashSet<String> participantSet = new HashSet<String>();
        DataSet editParticipantDS = new DataSet();
        boolean isSMSAdmin = this.connectionInfo.hasRole("SMS Admin");
        for (int i = 0; i < primary.size(); ++i) {
            if (!isSMSAdmin && this.hasPrimaryValueChanged(primary, i, "sstudyid")) {
                throw new SapphireException("Invalid Study Update", "VALIDATION", this.getTranslationProcessor().translate("Invalid attempt to update study on Sample Family"));
            }
            String samplefamilyid = primary.getString(i, "s_samplefamilyid");
            String studyid = primary.getString(i, "sstudyid", this.getOldPrimaryValue(primary, i, "sstudyid")).trim();
            boolean isProtocolStudy = this.isProtocolStudy(studyid);
            if (!isProtocolStudy) continue;
            boolean participantInDifferentProtocolRevision = false;
            String participantClinicalProtocolRevision = "";
            if (this.hasPrimaryValueChanged(primary, i, "externalsubject") || this.hasPrimaryValueChanged(primary, i, "studysiteid")) {
                String[] cohorts = StringUtil.split(actionProps.getProperty("_cpcohortid"), ";");
                String externalsubject = primary.getString(i, "externalsubject", "").trim();
                if (externalsubject.length() == 0) {
                    primary.setString(i, "subjectid", "");
                    primary.setString(i, "participantid", "");
                    primary.setString(i, "participanteventid", "");
                    primary.setString(i, "eventdefid", "");
                    primary.setString(i, "clinicalevent", "");
                    primary.setString(i, "specimendefid", "");
                } else {
                    String participantid;
                    String studysiteid = primary.getString(i, "studysiteid", this.getOldPrimaryValue(primary, i, "studysiteid"));
                    String cpcohortid = "";
                    if (cohorts.length > i) {
                        cpcohortid = cohorts[i];
                    }
                    if (cpcohortid.length() > 0) {
                        Map<String, String> participantMap = this.getParticipant(participantStudySubjectOnly, studyid, studysiteid, externalsubject, cpcohortid);
                        String participantid2 = participantMap.get("participantid");
                        primary.setString(i, "subjectid", participantMap.get("subjectid"));
                        primary.setString(i, "participantid", participantid2);
                        if (participantMap.containsKey("clinicalprotocolrevision")) {
                            participantInDifferentProtocolRevision = true;
                            participantClinicalProtocolRevision = participantMap.get("clinicalprotocolrevision");
                            primary.setString(i, "clinicalprotocolrevision", participantClinicalProtocolRevision);
                        }
                    } else if (!this.hasPrimaryValueChanged(primary, i, "participantid")) {
                        participantid = this.getOldPrimaryValue(primary, i, "participantid");
                        if (participantSet.add(participantid)) {
                            int row = editParticipantDS.addRow();
                            editParticipantDS.setString(row, "s_participantid", participantid);
                            editParticipantDS.setString(row, "externalparticipantid", externalsubject);
                        }
                    } else {
                        participantid = primary.getString(i, "participantid", "");
                        if (participantid.length() > 0) {
                            String siteprotocolrevision;
                            Map<String, String> participantData = this.getParticipantDataSet(participantid);
                            String participantprotocolrevision = participantData.containsKey("participantprotocolrevision") ? participantData.get("participantprotocolrevision") : "";
                            String string = siteprotocolrevision = participantData.containsKey("siteprotocolrevision") ? participantData.get("siteprotocolrevision") : "";
                            if (!siteprotocolrevision.equals(participantprotocolrevision)) {
                                participantInDifferentProtocolRevision = true;
                                participantClinicalProtocolRevision = participantprotocolrevision;
                                primary.setString(i, "clinicalprotocolrevision", participantClinicalProtocolRevision);
                            }
                        }
                    }
                }
            }
            if (eventDefList.size() > i) {
                String participantid = primary.getString(i, "participantid", this.getOldPrimaryValue(primary, i, "participantid"));
                String eventdefid = (String)eventDefList.get(i);
                if (OpalUtil.isEmpty(eventdefid) || OpalUtil.isEmpty(participantid)) {
                    primary.setString(i, "participanteventid", "");
                    primary.setString(i, "eventdefid", "");
                    primary.setString(i, "clinicalevent", "");
                    continue;
                }
                String existingEventDefId = (String)sampleFamilyEventDefMap.get(samplefamilyid);
                if (!OpalUtil.isEmpty(existingEventDefId) && eventdefid.equals(existingEventDefId)) continue;
                if (participantInDifferentProtocolRevision) {
                    String sql = "SELECT s_eventdef.s_eventdefid FROM s_eventdef, s_participant WHERE s_eventdef.clinicalprotocolid = s_participant.clinicalprotocolid      AND s_eventdef.clinicalprotocolrevision = s_participant.clinicalprotocolrevision      AND s_participant.s_participantid = ?      AND s_eventdef.s_eventdefid = ?";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{participantid, eventdefid});
                    if (ds == null || ds.size() == 0) {
                        throw new SapphireException("Invalid Event", "VALIDATION", this.getTranslationProcessor().translate("Event does not exist in Participant's Protocol Revision") + " (" + participantClinicalProtocolRevision + ")");
                    }
                }
                Map<String, String> eventMap = this.getParticipantEventMap(participantid, eventdefid);
                String participanteventid = eventMap.get("s_participanteventid");
                primary.setString(i, "participanteventid", participanteventid);
                primary.setString(i, "eventdefid", eventdefid);
                String parenteventlabel = eventMap.get("parenteventlabel");
                String eventlabel = eventMap.get("eventlabel");
                primary.setString(i, "clinicalevent", OpalUtil.isNotEmpty(parenteventlabel) ? parenteventlabel + " > " + eventlabel : eventlabel);
                if (!OpalUtil.isNotEmpty(participanteventid)) continue;
                this.handleSampleFamilyParticipantEventChange(participanteventid, primary, i, actionProps);
                continue;
            }
            if (!this.hasPrimaryValueChanged(primary, i, "participanteventid")) continue;
            String participanteventid = primary.getString(i, "participanteventid", "");
            if (OpalUtil.isNotEmpty(participanteventid)) {
                if (participantInDifferentProtocolRevision) {
                    String participantid = primary.getString(i, "participantid", "");
                    String sql = "select pe.s_participanteventid from s_participantevent pe where pe.eventdefid in (select ed.s_eventdefid from s_eventdef ed, s_participant p where ed.clinicalprotocolid = p.clinicalprotocolid      and ed.clinicalprotocolrevision = p.clinicalprotocolrevision      and ed.cohortid = p.cpcohortid      and p.s_participantid = ?) and pe.s_participanteventid = ?";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{participantid, participanteventid});
                    if (ds == null || ds.size() == 0) {
                        throw new SapphireException("Invalid Event", "VALIDATION", this.getTranslationProcessor().translate("Event does not exist in Participant's Protocol Revision") + " (" + participantClinicalProtocolRevision + ")");
                    }
                }
                this.handleSampleFamilyParticipantEventChange(participanteventid, primary, i, actionProps);
                continue;
            }
            primary.setString(i, "eventdefid", "");
            primary.setString(i, "sampletypeid", "");
            primary.setString(i, "specimendefid", "");
        }
        if (editParticipantDS.size() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Participant");
            props.setProperty("keyid1", editParticipantDS.getColumnValues("s_participantid", ";"));
            props.setProperty("externalparticipantid", editParticipantDS.getColumnValues("externalparticipantid", ";"));
            this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
        }
        this.processKitUsage(primary, true);
        Trace.logInfo("END: LV_SampleFamily.preEdit() [" + start + "]. Took " + (System.currentTimeMillis() - start) + "ms.");
    }

    private Map<String, String> getParticipantDataSet(String participantid) {
        String key = "participant_" + participantid;
        if (!this.participantCache.containsKey(key)) {
            HashMap<String, String> map = new HashMap<String, String>();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, clinicalprotocolrevision participantprotocolrevision, (select ss.clinicalprotocolrevision from s_studysite ss where ss.s_studysiteid = s_participant.studysiteid) siteprotocolrevision from s_participant where s_participant.s_participantid = ?", (Object[])new String[]{participantid});
            if (ds != null && ds.size() > 0) {
                map.put("participantid", participantid);
                map.put("participantprotocolrevision", ds.getString(0, "participantprotocolrevision", ""));
                map.put("siteprotocolrevision", ds.getString(0, "siteprotocolrevision", ""));
            }
            this.participantCache.put(key, map);
        }
        return this.participantCache.get(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        if ("Y".equals(actionProps.getProperty("__participanteditflag"))) {
            return;
        }
        if ("Y".equals(actionProps.getProperty("__studyswitchedit"))) {
            return;
        }
        if (!"Y".equals(actionProps.getProperty("__samplefamilyaddflag"))) {
            String rsetid;
            long start = System.currentTimeMillis();
            Trace.logInfo("START: LV_SampleFamily.postEdit() [" + start + "]");
            DataSet primary = sdiData.getDataset("primary");
            boolean isStudyClinical = "Y".equalsIgnoreCase(this.getQueryProcessor().getPreparedSqlDataSet("select s.clinicalflag from s_study s, s_samplefamily sf where sf.sstudyid = s.s_studyid and sf.s_samplefamilyid = ?", (Object[])new String[]{primary.getString(0, "s_samplefamilyid")}).getString(0, "clinicalflag", "N"));
            if (!isStudyClinical) {
                this.addSubjectDocuments(primary);
                this.checkAndAddParticipant(primary);
            } else {
                this.addBlankDocuments(primary);
                this.addBlankDocumentsFromEventDef(primary);
            }
            boolean forceUpdate = "Y".equals(actionProps.getProperty("__sdcruleconfirm"));
            if (this.connectionInfo.hasModule("SMS")) {
                this.checkHipaaRule(primary, "Y".equals(actionProps.getProperty("__sdcruleconfirm")));
                this.checkGLPRule(primary, "Y".equals(actionProps.getProperty("__sdcruleconfirm")));
                this.checkCOCRule(primary, false, forceUpdate);
                this.checkAmbiguousSubjectRule(primary);
            }
            HashMap<String, String> qtyMap = new HashMap<String, String>();
            for (int i = 0; i < primary.size(); ++i) {
                if (this.hasPrimaryValueChanged(primary, i, "initialvolume")) {
                    if (primary.getValue(i, "initialvolume").equals(this.getOldPrimaryValue(primary, i, "initialvolume"))) continue;
                    qtyMap.put(primary.getValue(i, "s_samplefamilyid"), this.getOldPrimaryValue(primary, i, "initialvolume"));
                    continue;
                }
                if (!this.hasPrimaryValueChanged(primary, i, "initialmass") || primary.getValue(i, "initialmass").equals(this.getOldPrimaryValue(primary, i, "initialmass"))) continue;
                qtyMap.put(primary.getValue(i, "s_samplefamilyid"), this.getOldPrimaryValue(primary, i, "initialmass"));
            }
            if (qtyMap.size() > 0) {
                DataSet ds;
                StringBuilder sql = new StringBuilder();
                SafeSQL safeSQL = new SafeSQL();
                sql.append("select sf.s_samplefamilyid, t.trackitemid, t.qtycurrent, t.qtyunits, sf.initialvolume, sf.initialvolumeunits, sf.initialmass, sf.initialmassunits");
                sql.append(" from trackitem t, s_samplefamily sf, s_sample s");
                sql.append(" where s.samplefamilyid = sf.s_samplefamilyid");
                sql.append(" and t.linksdcid = 'Sample'");
                sql.append(" and t.linkkeyid1 = s.s_sampleid");
                sql.append(" and s.s_sampleid not in (select sm.destsampleid from s_samplemap sm)");
                if (qtyMap.size() < 750) {
                    sql.append(" and sf.s_samplefamilyid in ( ").append(safeSQL.addIn(qtyMap.keySet())).append(" )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                } else {
                    rsetid = this.getDAMProcessor().createRSet(SDCID, OpalUtil.toDelimitedString(qtyMap.keySet(), ";"), null, null);
                    sql.append(" and sf.s_samplefamilyid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                if (ds != null) {
                    DataSet dsdata = new DataSet();
                    for (int i = 0; i < ds.size(); ++i) {
                        String qtycurrent;
                        String samplefamilyid = ds.getString(i, "s_samplefamilyid");
                        String vol = ds.getValue(i, "initialvolume", "0");
                        String unit = ds.getValue(i, "initialvolumeunits", "");
                        double volume = ds.getDouble(i, "initialvolume", 0.0);
                        if (volume <= 0.0) {
                            vol = ds.getValue(i, "initialmass", "0");
                            unit = ds.getValue(i, "initialmassunits", "");
                        }
                        if ("0".equals(vol) || (qtycurrent = ds.getValue(i, "qtycurrent", "")).length() != 0 && !qtycurrent.equals(qtyMap.get(samplefamilyid))) continue;
                        int row = dsdata.addRow();
                        dsdata.setString(row, "trackitemid", ds.getValue(i, "trackitemid"));
                        dsdata.setString(row, "qtycurrent", vol);
                        dsdata.setString(row, "qtyunits", unit);
                    }
                    if (dsdata.size() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "TrackItemSDC");
                        props.setProperty("keyid1", dsdata.getColumnValues("trackitemid", ";"));
                        props.setProperty("qtycurrent", dsdata.getColumnValues("qtycurrent", ";"));
                        props.setProperty("qtyunits", dsdata.getColumnValues("qtyunits", ";"));
                        props.setProperty("__sdcruleignore", "Y");
                        props.setProperty("auditreason", "Quantity changed in sample family");
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                }
            }
            ArrayList<String> updateList = new ArrayList<String>();
            HashMap<String, String> updateStudyMap = new HashMap<String, String>();
            for (int i = 0; i < primary.size(); ++i) {
                String sstudyid;
                String samplefamilyid = primary.getString(i, "s_samplefamilyid");
                if (this.hasPrimaryValueChanged(primary, i, "studysiteid")) {
                    updateList.add(samplefamilyid);
                }
                if (!this.hasPrimaryValueChanged(primary, i, "sstudyid") || (sstudyid = primary.getString(i, "sstudyid", "")).length() <= 0) continue;
                updateStudyMap.put(samplefamilyid, primary.getString(i, "sstudyid"));
            }
            if (updateList.size() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = null;
                StringBuilder sql = new StringBuilder();
                sql.append("select sf.s_samplefamilyid, ss.clinicalprotocolid siteprotocolid, ss.clinicalprotocolrevision siteprotocolrevision,");
                sql.append("  (select cp.s_clinicalprotocolversionid from s_clinicalprotocol cp where cp.sstudyid = ss.sstudyid and cp.s_clinicalprotocolrevision = ss.clinicalprotocolrevision and cp.versionstatus = 'C') siteprotocolversionid,");
                sql.append("  p.CLINICALPROTOCOLID participantprotocolid, p.CLINICALPROTOCOLREVISION participantprotocolrevision,");
                sql.append("  (select cp.s_clinicalprotocolversionid from s_clinicalprotocol cp where cp.sstudyid = p.sstudyid and cp.s_clinicalprotocolrevision = p.clinicalprotocolrevision and cp.versionstatus = 'C') participantprotocolversion,");
                sql.append("  sf.CLINICALPROTOCOLID familyprotocolid, sf.CLINICALPROTOCOLVERSIONID familyprotocolversionid, sf.CLINICALPROTOCOLREVISION familyprotocolrevision");
                sql.append(" from s_samplefamily sf left outer join s_participant p on p.S_PARTICIPANTID = sf.PARTICIPANTID, s_studysite ss, s_study s");
                sql.append("  where ss.sstudyid = sf.sstudyid");
                sql.append("  and ss.s_studysiteid = sf.studysiteid");
                sql.append("  and s.s_studyid = sf.sstudyid");
                sql.append("  and s.clinicalflag = 'Y'");
                if (updateList.size() < 1000) {
                    sql.append("  and sf.s_samplefamilyid in (").append(safeSQL.addIn(updateList)).append(")");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                } else {
                    String rsetid2 = this.getDAMProcessor().createRSet(SDCID, OpalUtil.toDelimitedString(updateList, ";"), null, null);
                    try {
                        sql.append("  and sf.s_samplefamilyid in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid2)).append(")");
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    }
                    finally {
                        if (StringUtil.getLen(rsetid2) > 0L) {
                            this.getDAMProcessor().clearRSet(rsetid2);
                        }
                    }
                }
                if (ds != null && ds.size() > 0) {
                    DataSet updateds = new DataSet();
                    for (int i = 0; i < ds.size(); ++i) {
                        int row;
                        String familyprotocolid = ds.getString(i, "familyprotocolid", "");
                        String familyprotocolversionid = ds.getString(i, "familyprotocolversionid", "");
                        String familyprotocolrevision = ds.getString(i, "familyprotocolrevision", "");
                        String participantprotocolid = ds.getString(i, "participantprotocolid", "");
                        String participantprotocolversion = ds.getString(i, "participantprotocolversion", "");
                        String participantprotocolrevision = ds.getString(i, "participantprotocolrevision", "");
                        String siteprotocolid = ds.getString(i, "siteprotocolid", "");
                        String siteprotocolversionid = ds.getString(i, "siteprotocolversionid", "");
                        String siteprotocolrevision = ds.getString(i, "siteprotocolrevision", "");
                        if (participantprotocolid.length() > 0) {
                            if (familyprotocolid.equals(participantprotocolid) && familyprotocolversionid.equals(participantprotocolversion) && familyprotocolrevision.equals(participantprotocolrevision)) continue;
                            row = updateds.addRow();
                            updateds.setString(row, "s_samplefamilyid", ds.getString(i, "s_samplefamilyid"));
                            updateds.setString(row, "clinicalprotocolid", participantprotocolid);
                            updateds.setString(row, "clinicalprotocolversionid", participantprotocolversion);
                            updateds.setString(row, "clinicalprotocolrevision", participantprotocolrevision);
                            continue;
                        }
                        if (siteprotocolid.length() <= 0 || familyprotocolid.equals(siteprotocolid) && familyprotocolversionid.equals(siteprotocolversionid) && familyprotocolrevision.equals(siteprotocolrevision)) continue;
                        row = updateds.addRow();
                        updateds.setString(row, "s_samplefamilyid", ds.getString(i, "s_samplefamilyid"));
                        updateds.setString(row, "clinicalprotocolid", siteprotocolid);
                        updateds.setString(row, "clinicalprotocolversionid", siteprotocolversionid);
                        updateds.setString(row, "clinicalprotocolrevision", siteprotocolrevision);
                    }
                    if (updateds.size() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", SDCID);
                        props.setProperty("keyid1", updateds.getColumnValues("s_samplefamilyid", ";"));
                        props.setProperty("clinicalprotocolid", updateds.getColumnValues("clinicalprotocolid", ";"));
                        props.setProperty("clinicalprotocolversionid", updateds.getColumnValues("clinicalprotocolversionid", ";"));
                        props.setProperty("clinicalprotocolrevision", updateds.getColumnValues("clinicalprotocolrevision", ";"));
                        props.setProperty("__sdcruleignore", "Y");
                        props.setProperty("auditreason", "Edited site on Sample Family");
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                }
            }
            if (updateStudyMap.size() > 0) {
                DataSet ds;
                String sql;
                if (updateStudyMap.size() > 1000) {
                    rsetid = this.getDAMProcessor().createRSet("Study", OpalUtil.toDelimitedString(updateStudyMap.keySet(), ";"), null, null);
                    sql = "select s_sampleid, samplefamilyid, sstudyid from s_sample where samplefamilyid in (select r.keyid1 from rsetitems r where r.rsetid = ?)";
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                } else {
                    SafeSQL safeSQL = new SafeSQL();
                    sql = "select s_sampleid, samplefamilyid, sstudyid from s_sample where samplefamilyid in (" + safeSQL.addIn(updateStudyMap.keySet()) + ")";
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                }
                if (OpalUtil.isNotEmpty(ds)) {
                    for (int i = 0; i < ds.size(); ++i) {
                        ds.setString(i, "sstudyid", (String)updateStudyMap.get(ds.getString(i, "samplefamilyid")));
                    }
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", ds.getColumnValues("s_sampleid", ";"));
                    props.setProperty("sstudyid", ds.getColumnValues("sstudyid", ";"));
                    props.setProperty("__studyswitchedit", "Y");
                    props.setProperty("auditreason", "Edited Study on Sample");
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
            }
            Trace.logInfo("END: LV_SampleFamily.postEdit() [" + start + "]. Took " + (System.currentTimeMillis() - start) + "ms.");
        }
    }

    private void handleSampleFamilyParticipantEventChange(String participanteventid, DataSet primary, int i, PropertyList actionProps) throws ActionException {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s.s_sampleid, sf.s_samplefamilyid, s.sampletypeid, t.containertypeid from s_sample s, s_samplefamily sf, trackitem t where sf.s_samplefamilyid = ? and s.samplefamilyid = sf.s_samplefamilyid and t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid order by s.createdt desc", (Object[])new String[]{primary.getString(i, "s_samplefamilyid")});
        if (ds != null && ds.size() > 0) {
            String sampletypeid = ds.getString(0, "sampletypeid", "");
            String containertypeid = ds.getString(0, "containertypeid", "");
            if (OpalUtil.isNotEmpty(sampletypeid)) {
                if (this.connectionInfo.isOracle()) {
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select pe.eventdefid, (select s_specimendefid from s_eventdefstspecimendef sd where sd.s_eventdefid = pe.eventdefid and sd.s_sampletypeid = ? and sd.specimentype = ? and rownum = 1) specimendefid from s_participantevent pe where pe.s_participanteventid = ?", (Object[])new String[]{sampletypeid, containertypeid, participanteventid});
                } else if (this.connectionInfo.isSqlServer()) {
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select pe.eventdefid, (select top(1) s_specimendefid from s_eventdefstspecimendef sd where sd.s_eventdefid = pe.eventdefid and sd.s_sampletypeid = ? and sd.specimentype = ?) specimendefid from s_participantevent pe where pe.s_participanteventid = ?", (Object[])new String[]{sampletypeid, containertypeid, participanteventid});
                }
                if (ds != null && ds.size() > 0) {
                    DataSet wids;
                    String eventdefid = ds.getString(0, "eventdefid", "");
                    String specimendefid = ds.getString(0, "specimendefid", "");
                    primary.setString(i, "eventdefid", eventdefid);
                    primary.setString(i, "sampletypeid", sampletypeid);
                    primary.setString(i, "specimendefid", specimendefid);
                    if (OpalUtil.isNotEmpty(eventdefid) && OpalUtil.isNotEmpty(sampletypeid) && OpalUtil.isNotEmpty(specimendefid) && (wids = this.getQueryProcessor().getPreparedSqlDataSet("select workitemid, workitemversionid, applyonaddflag, assigneddepartmentid, embedchildsampleplanid, embedchildsampleplanversionid from s_eventdefstspecimendefwi where s_eventdefid = ? and s_sampletypeid = ? and s_specimendefid = ? order by usersequence", (Object[])new String[]{eventdefid, sampletypeid, specimendefid})) != null) {
                        for (int index = 0; index < wids.size(); ++index) {
                            DataSet sfds;
                            String workitemid = wids.getString(index, "workitemid", "");
                            String workitemversionid = wids.getString(index, "workitemversionid", "");
                            if (!OpalUtil.isNotEmpty(workitemid) || (sfds = this.getQueryProcessor().getPreparedSqlDataSet("select s.s_sampleid, s.sstudyid from s_sample s where not exists( select sm.destsampleid from s_samplemap sm where sm.destsampleid = s.s_sampleid ) and s.samplefamilyid = ?", (Object[])new String[]{primary.getString(i, "s_samplefamilyid", "")})) == null) continue;
                            String rootsampleid = sfds.getString(0, "s_sampleid", "");
                            String sourcesstudyid = sfds.getString(0, "sstudyid", "");
                            if (!OpalUtil.isNotEmpty(rootsampleid)) continue;
                            PropertyList props = new PropertyList();
                            props.setProperty("sdcid", "Sample");
                            props.setProperty("keyid1", rootsampleid);
                            props.setProperty("workitemid", workitemid);
                            props.setProperty("workitemversionid", workitemversionid);
                            props.setProperty("applyworkitem", wids.getString(index, "applyonaddflag", "N"));
                            props.setProperty("embedchildsampleplanid", wids.getString(index, "embedchildsampleplanid", ""));
                            props.setProperty("embedchildsampleplanversionid", wids.getString(index, "embedchildsampleplanversionid", ""));
                            props.setProperty("s_assigneddepartment", wids.getString(index, "assigneddepartmentid", ""));
                            props.setProperty("sourcesstudyid", sourcesstudyid);
                            props.setProperty("auditactivity", actionProps.getProperty("auditactivity"));
                            props.setProperty("auditreason", actionProps.getProperty("auditreason"));
                            props.setProperty("auditsignedflag", actionProps.getProperty("auditsignedflag"));
                            this.getActionProcessor().processActionClass(AddSDIWorkItem.class.getName(), props);
                        }
                    }
                }
            }
        }
    }

    private boolean isProtocolStudy(String studyid) {
        String key = "isprotocol_" + studyid;
        if (!this.isProtocolCache.containsKey(key)) {
            this.isProtocolCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "clinicalflag", "s_studyid = ?", new String[]{studyid}));
        }
        return "Y".equals(this.isProtocolCache.get(key));
    }

    private Map<String, String> getParticipant(boolean participantStudySubjectOnly, String studyid, String studysiteid, String externalsubject, String cpcohortid) throws SapphireException {
        String key;
        String string = key = participantStudySubjectOnly ? studyid + externalsubject : studysiteid + externalsubject;
        if (!this.participantCache.containsKey(key)) {
            HashMap<String, String> map = new HashMap<String, String>();
            DataSet ds = participantStudySubjectOnly ? this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, externalparticipantid, subjectid, studysiteid, clinicalprotocolrevision, (select ss.clinicalprotocolrevision from s_studysite ss where ss.s_studysiteid=s_participant.studysiteid) siteprotocolrevision from s_participant where sstudyid = ? and externalparticipantid = ?", new Object[]{studyid, externalsubject}) : this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, externalparticipantid, subjectid, studysiteid, clinicalprotocolrevision, (select ss.clinicalprotocolrevision from s_studysite ss where ss.s_studysiteid=s_participant.studysiteid) siteprotocolrevision from s_participant where studysiteid = ? and externalparticipantid = ?", new Object[]{studysiteid, externalsubject});
            if (ds != null && ds.size() > 0) {
                map.put("subjectid", ds.getString(0, "subjectid", ""));
                map.put("participantid", ds.getString(0, "s_participantid", ""));
                String clinicalprotocolrevision = ds.getString(0, "clinicalprotocolrevision", "");
                String siteprotocolrevision = ds.getString(0, "siteprotocolrevision", "");
                if (!clinicalprotocolrevision.equals(siteprotocolrevision)) {
                    map.put("clinicalprotocolrevision", clinicalprotocolrevision);
                }
            } else {
                if (StringUtil.getLen(cpcohortid) > 0L) {
                    String clinicalprotocolid = "";
                    String clinicalprotocolversionid = "";
                    String clinicalprotocolrevision = "";
                    if (OpalUtil.isNotEmpty(studysiteid) && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select sstudyid, clinicalprotocolid, clinicalprotocolversionid, clinicalprotocolrevision from s_studysite where s_studysiteid = ?", new Object[]{studysiteid})) != null && ds.size() > 0) {
                        clinicalprotocolid = ds.getString(0, "clinicalprotocolid", "");
                        clinicalprotocolversionid = ds.getString(0, "clinicalprotocolversionid", "");
                        clinicalprotocolrevision = ds.getString(0, "clinicalprotocolrevision", "");
                    }
                    boolean addNewParticipant = false;
                    if (participantStudySubjectOnly) {
                        addNewParticipant = true;
                    } else if (OpalUtil.isNotEmpty(studysiteid)) {
                        addNewParticipant = true;
                    }
                    if (addNewParticipant) {
                        PropertyList policy = null;
                        try {
                            policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
                        }
                        catch (SapphireException e) {
                            e.printStackTrace();
                        }
                        boolean autoCreateSubject = "Y".equals(policy != null ? policy.getProperty("autocreatesubject", "Y") : "Y");
                        if (!autoCreateSubject) {
                            throw new SapphireException(this.getTranslationProcessor().translate("Auto Create Subject Failure"), "VALIDATION", this.getTranslationProcessor().translate("Auto creation of Subject is not allowed in BioBanking Policy"));
                        }
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_Subject");
                        props.setProperty("copies", "1");
                        props.setProperty("activeflag", "Y");
                        props.setProperty("__sdcruleconfirm", "Y");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        String subjectid = props.getProperty("newkeyid1");
                        props.clear();
                        props.setProperty("sdcid", "LV_Participant");
                        props.setProperty("copies", "1");
                        props.setProperty("sstudyid", studyid);
                        props.setProperty("studysiteid", studysiteid);
                        props.setProperty("clinicalprotocolid", clinicalprotocolid);
                        props.setProperty("clinicalprotocolversionid", clinicalprotocolversionid);
                        props.setProperty("clinicalprotocolrevision", clinicalprotocolrevision);
                        props.setProperty("cpcohortid", cpcohortid);
                        props.setProperty("subjectid", subjectid);
                        props.setProperty("externalparticipantid", externalsubject);
                        props.setProperty("participantstatus", "Enrolled");
                        props.setProperty("enrolldt", "n");
                        props.setProperty("activeflag", "Y");
                        props.setProperty("__sdcruleconfirm", "Y");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        map.put("subjectid", subjectid);
                        map.put("participantid", props.getProperty("newkeyid1"));
                    }
                }
                if (!map.containsKey("subjectid")) {
                    map.put("subjectid", "");
                    map.put("participantid", "");
                }
            }
            this.participantCache.put(key, map);
        }
        return this.participantCache.get(key);
    }

    private Map<String, String> getParticipantEventMap(String participantid, String eventdefid) throws SapphireException {
        String key = "__participantevent_" + participantid + eventdefid;
        if (!this.eventCache.containsKey(key)) {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("BioBankingPolicy", "Sapphire Custom");
            String addEventPolicy = policy != null ? policy.getProperty("addeventpolicy", "Always add new event") : "Always add new event";
            HashMap<String, String> map = new HashMap<String, String>();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select pe.s_participanteventid, pe.participantid, pe.sstudysiteid, pe.eventdefid, pe.eventlabel, pe.parentparticipanteventid, ed.allowmultipleflag, (select ppe.eventdefid from s_participantevent ppe where ppe.s_participanteventid = pe.parentparticipanteventid) parenteventdefid, (select ppe.eventlabel from s_participantevent ppe where ppe.s_participanteventid = pe.parentparticipanteventid) parenteventlabel from s_participantevent pe, s_eventdef ed where pe.participantid = ? and pe.eventdefid = ? and ed.s_eventdefid = pe.eventdefid", (Object[])new String[]{participantid, eventdefid});
            if (ds != null && ds.size() > 0) {
                if ("Y".equals(ds.getString(0, "allowmultipleflag"))) {
                    if ("Update existing event".equals(addEventPolicy)) {
                        map.put("s_participanteventid", ds.getString(0, "s_participanteventid"));
                        map.put("eventlabel", ds.getString(0, "eventlabel"));
                        map.put("parenteventlabel", ds.getString(0, "parenteventlabel", ""));
                    } else {
                        this.addNewParticipantEvent(participantid, eventdefid, map);
                    }
                } else {
                    if ("Always add new event".equals(addEventPolicy)) {
                        String parenteventlabel = ds.getString(0, "parenteventlabel", "");
                        String eventlabel = ds.getString(0, "eventlabel", "");
                        if (OpalUtil.isNotEmpty(parenteventlabel)) {
                            eventlabel = parenteventlabel + " / " + eventlabel;
                        }
                        throw new SapphireException(this.getTranslationProcessor().translate("Multiple Event Error"), "VALIDATION", this.getTranslationProcessor().translate("Event already exist for the Participant and does not allow multiple instances") + "<br><br>" + this.getTranslationProcessor().translate("Participant: ") + participantid + "<br>" + this.getTranslationProcessor().translate("Event: ") + eventlabel);
                    }
                    map.put("s_participanteventid", ds.getString(0, "s_participanteventid"));
                    map.put("eventlabel", ds.getString(0, "eventlabel"));
                    map.put("parenteventlabel", ds.getString(0, "parenteventlabel", ""));
                }
            } else {
                this.addNewParticipantEvent(participantid, eventdefid, map);
            }
            this.eventCache.put(key, map);
        }
        return this.eventCache.containsKey(key) ? (Map)this.eventCache.get(key) : new HashMap<String, String>();
    }

    private void addNewParticipantEvent(String participantid, String eventdefid, Map<String, String> map) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select ed.s_eventdefid, ed.eventdeflabel, ed.clinicalprotocolid, ed.eventdeftype, ped.eventdeflabel parenteventdeflabel, ped.s_eventdefid parenteventdefid, (select pe.s_participanteventid from s_participantevent pe where pe.participantid = ? and pe.eventdefid = ped.s_eventdefid) parentparticipanteventid from s_eventdef ed left outer join s_eventdef ped on ped.s_eventdefid = ed.parenteventdefid where ed.s_eventdefid = ?", new Object[]{participantid, eventdefid});
        if (ds != null && ds.size() > 0) {
            PropertyList props = new PropertyList();
            String eventdeftype = ds.getString(0, "eventdeftype", "Visit");
            if ("Timepoint".equals(eventdeftype)) {
                String parentparticipanteventid = ds.getString(0, "parentparticipanteventid", "");
                if (StringUtil.getLen(parentparticipanteventid) == 0L) {
                    try {
                        props.setProperty("sdcid", "LV_ParticipantEvent");
                        props.setProperty("copies", "1");
                        props.setProperty("participantid", participantid);
                        props.setProperty("eventdt", "n");
                        props.setProperty("eventstatus", "Completed");
                        props.setProperty("sstudysiteid", this.getParticipantStudySite(participantid));
                        props.setProperty("eventdefid", ds.getString(0, "parenteventdefid", ""));
                        props.setProperty("eventlabel", ds.getString(0, "parenteventdeflabel", ""));
                        props.setProperty("activeflag", "Y");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        parentparticipanteventid = props.getProperty("newkeyid1");
                    }
                    catch (ActionException e) {
                        e.printStackTrace(System.out);
                    }
                }
                if (StringUtil.getLen(parentparticipanteventid) > 0L) {
                    try {
                        props.setProperty("sdcid", "LV_ParticipantEvent");
                        props.setProperty("copies", "1");
                        props.setProperty("participantid", participantid);
                        props.setProperty("eventdt", "n");
                        props.setProperty("eventstatus", "Completed");
                        props.setProperty("sstudysiteid", this.getParticipantStudySite(participantid));
                        props.setProperty("eventdefid", eventdefid);
                        props.setProperty("eventlabel", ds.getValue(0, "eventdeflabel", ""));
                        props.setProperty("parentparticipanteventid", parentparticipanteventid);
                        props.setProperty("activeflag", "Y");
                        this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                        map.put("s_participanteventid", props.getProperty("newkeyid1"));
                        map.put("eventlabel", props.getProperty("eventlabel"));
                        map.put("parenteventlabel", ds.getString(0, "parenteventdeflabel", ""));
                    }
                    catch (ActionException e) {
                        e.printStackTrace(System.out);
                    }
                }
            } else {
                try {
                    props.setProperty("sdcid", "LV_ParticipantEvent");
                    props.setProperty("copies", "1");
                    props.setProperty("participantid", participantid);
                    props.setProperty("eventdt", "n");
                    props.setProperty("eventstatus", "Completed");
                    props.setProperty("sstudysiteid", this.getParticipantStudySite(participantid));
                    props.setProperty("eventdefid", eventdefid);
                    props.setProperty("eventlabel", ds.getValue(0, "eventdeflabel", ""));
                    props.setProperty("activeflag", "Y");
                    this.getActionProcessor().processActionClass(AddSDI.class.getName(), props);
                    map.put("s_participanteventid", props.getProperty("newkeyid1"));
                    map.put("eventlabel", props.getProperty("eventlabel"));
                    map.put("parenteventlabel", ds.getString(0, "parenteventdeflabel", ""));
                }
                catch (ActionException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }

    private String getParticipantStudySite(String participantid) {
        String key = "__participant_studysite_" + participantid;
        if (!this.eventCache.containsKey(key)) {
            this.eventCache.put(key, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "studysiteid", "s_participantid = ?", new String[]{participantid}));
        }
        return (String)this.eventCache.get(key);
    }

    public void addBlankDocuments(DataSet primary) {
        DataSet ds;
        String sampleFamilyIds = "";
        for (int i = 0; i < primary.size(); ++i) {
            if (primary.getValue(i, "clinicalprotocolid", "").length() <= 0 || primary.getValue(i, "clinicalprotocolrevision", "").length() <= 0 || primary.getValue(i, "clinicalprotocolversionid", "").length() <= 0 || this.getOldPrimaryValue(primary, i, "clinicalprotocolid").equals(primary.getValue(i, "clinicalprotocolid", "")) && !this.getOldPrimaryValue(primary, i, "clinicalprotocolrevision").equals(primary.getValue(i, "clinicalprotocolrevision", "")) && !this.getOldPrimaryValue(primary, i, "clinicalprotocolversionid").equals(primary.getValue(i, "clinicalprotocolversionid", ""))) continue;
            sampleFamilyIds = sampleFamilyIds.length() > 0 ? sampleFamilyIds + ";" + primary.getValue(i, "s_samplefamilyid", "") : primary.getValue(i, "s_samplefamilyid", "");
        }
        if (sampleFamilyIds.length() > 0 && (ds = BusinessRulesUtil.findFormFromProtocolUsingSF(sampleFamilyIds, this.getQueryProcessor())).size() > 0) {
            DataSet familyformsds = null;
            HashMap<String, String> filter = new HashMap<String, String>();
            StringBuilder sql = new StringBuilder();
            sql.append("select sd.keyid1 s_samplefamilyid, d.documentid, d.formid, d.formversionid");
            sql.append(" from document d, sdidocument sd");
            sql.append(" where d.documentid = sd.documentid");
            sql.append(" and sd.sdcid = 'LV_SampleFamily'");
            sql.append(" and sd.keyid1 in ([])");
            try {
                familyformsds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), SDCID, sql.toString(), sampleFamilyIds);
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
            DataSet formsDataSet = new DataSet();
            for (int i = 0; i < ds.size(); ++i) {
                String ruledef;
                if (!ds.getValue(i, "formrule", "").toLowerCase().contains("sample") || !ds.getValue(i, "formrule").contains(":") || !(ruledef = ds.getValue(i, "formrule").split(":")[1].trim()).toLowerCase().contains("allocation")) continue;
                if (familyformsds != null && familyformsds.size() > 0) {
                    filter.clear();
                    filter.put("s_samplefamilyid", ds.getString(i, "s_samplefamilyid", ""));
                    filter.put("formid", ds.getString(i, "formid", ""));
                    filter.put("formversionid", ds.getString(i, "formversionid", ""));
                    if (familyformsds.findRow(filter) != -1) continue;
                    formsDataSet.copyRow(ds, i, 1);
                    continue;
                }
                formsDataSet.copyRow(ds, i, 1);
            }
            try {
                FormUtil.addBlankDocument(formsDataSet, this.getActionProcessor(), SDCID, "s_samplefamilyid");
            }
            catch (ActionException e) {
                this.logger.error("Unable to add Blank Document to Sample " + formsDataSet.getColumnValues("s_sampleid", ";") + ". Exception raised is: " + e.getMessage(), e);
            }
        }
    }

    private void checkHipaaRule(DataSet primary, boolean forceUpdate) {
        ArrayList<String> subjects = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            String subjectid;
            if (!this.hasPrimaryValueChanged(primary, i, "sstudyid") && !this.hasPrimaryValueChanged(primary, i, "subjectid") || (subjectid = primary.getValue(i, "subjectid")).length() <= 0) continue;
            subjects.add(subjectid);
        }
        if (subjects.size() > 0) {
            try {
                new HipaaRule(this.database, this.connectionInfo).processRule(subjects, forceUpdate);
            }
            catch (SapphireException e) {
                this.setError("HIPAA Rule", "CONFIRM", e.getMessage());
            }
        }
    }

    private void checkGLPRule(DataSet primary, boolean forceUpdate) throws SapphireException {
        if (BaseBioBankRule.isRuleActive("GLP Rule", this.getConfigurationProcessor())) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "sstudyid") && !this.hasPrimaryValueChanged(primary, i, "conditionalapprovalflag")) continue;
                list.add(primary.getString(i, "s_samplefamilyid"));
            }
            if (list.size() > 0) {
                GLPRule rule = new GLPRule(this.database, this.connectionInfo);
                rule.processSampleFamilyGLPRule(OpalUtil.toDelimitedString(list, ";"), forceUpdate);
            }
        }
    }

    private void checkCOCRule(DataSet primary, boolean add, boolean forceUpdate) throws SapphireException {
        ArrayList<String> families = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            if (!add && !this.hasPrimaryValueChanged(primary, i, "restrictclassid")) continue;
            String samplefamilyid = primary.getString(i, "s_samplefamilyid");
            families.add(samplefamilyid);
        }
        if (families.size() > 0) {
            new COCRule(this.database, this.connectionInfo).processRule(families, 2, forceUpdate);
        }
    }

    private void checkAmbiguousSubjectRule(DataSet primary) throws SapphireException {
        if (BaseBioBankRule.isRuleActive("Ambiguous Subject Rule", this.getConfigurationProcessor())) {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < primary.size(); ++i) {
                if (!this.hasPrimaryValueChanged(primary, i, "studysiteid") && !this.hasPrimaryValueChanged(primary, i, "externalsubject") && !this.hasPrimaryValueChanged(primary, i, "subjectid")) continue;
                list.add(primary.getString(i, "s_samplefamilyid"));
            }
            if (list.size() > 0) {
                try {
                    new AmbiguousSubjectRule(this.database, this.connectionInfo).processRule(list);
                }
                catch (SapphireException e) {
                    this.setError("Ambiguous Subject Rule", "VALIDATION", e.getMessage());
                }
            }
        }
    }

    public static String createNewSampleFamily(ActionProcessor ap, String studyid, boolean forceUpdate) throws ActionException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", SDCID);
        props.put("sstudyid", studyid);
        props.put("copies", "1");
        props.put("__sdcruleconfirm", forceUpdate ? "Y" : "N");
        ap.processAction("AddSDI", "1", props);
        return props.get("newkeyid1").toString();
    }

    public static String createNewSampleFamilies(ActionProcessor ap, String copies, String studyid, boolean forceUpdate) throws ActionException {
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", SDCID);
        props.put("sstudyid", studyid);
        props.put("copies", copies);
        props.put("__sdcruleconfirm", forceUpdate ? "Y" : "N");
        ap.processAction("AddSDI", "1", props);
        return props.get("newkeyid1").toString();
    }

    private String getSampleFamilyStudy(String samplefamilyid) throws SapphireException {
        if (!this.sampleFamilyStudyMap.containsKey(samplefamilyid)) {
            this.sampleFamilyStudyMap.put(samplefamilyid, LV_SampleFamily.getStudyid(this.database, samplefamilyid));
        }
        return (String)this.sampleFamilyStudyMap.get(samplefamilyid);
    }

    public static String getStudyid(DBAccess database, String samplefamilyid) throws SapphireException {
        String studyid = "";
        String sql = "SELECT sstudyid FROM s_samplefamily WHERE s_samplefamilyid=?";
        database.createPreparedResultSet(sql, new Object[]{samplefamilyid});
        if (database.getNext()) {
            studyid = database.getString("sstudyid");
        }
        database.closeResultSet();
        return studyid;
    }

    public static boolean isApproved(DBAccess database, String samplefamilyid) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT count(*) ");
        sql.append(" FROM s_samplefamily ");
        sql.append(" WHERE s_samplefamilyid=? ");
        sql.append(" AND approvedby is not null ");
        sql.append(" AND approveddt is not null");
        return database.getPreparedCount(sql.toString(), new Object[]{samplefamilyid}) > 0;
    }

    public static ArrayList getSampleList(DBAccess database, String samplefamilyid) throws SapphireException {
        ArrayList<String> samples = new ArrayList<String>();
        database.createPreparedResultSet("SELECT s_sampleid FROM s_sample WHERE samplefamilyid=?", new Object[]{samplefamilyid});
        while (database.getNext()) {
            samples.add(database.getString("s_sampleid"));
        }
        database.closeResultSet();
        return samples;
    }

    public static String getSubject(DBAccess database, String samplefamilyid) throws SapphireException {
        String subjectid = "";
        database.createPreparedResultSet("SELECT subjectid FROM s_samplefamily WHERE s_samplefamilyid=?", new Object[]{samplefamilyid});
        if (database.getNext()) {
            subjectid = database.getString("subjectid");
        }
        database.closeResultSet();
        return subjectid;
    }

    public static DataSet getSubjectDetails(DBAccess database, String samplefamilyid) throws SapphireException {
        String sql = "SELECT ( select s_studysiteid from s_studysite where s_studysiteid = studysiteid ) studysiteid, externalsubject, subjectid FROM s_samplefamily WHERE s_samplefamilyid=?";
        database.createPreparedResultSet(sql, new Object[]{samplefamilyid});
        DataSet ds = new DataSet(database.getResultSet());
        database.closeResultSet();
        return ds;
    }

    public static String getExternalSubject(DBAccess database, String samplefamilyid) throws SapphireException {
        String externalSubjectId = "";
        database.createPreparedResultSet("SELECT externalsubject FROM s_samplefamily WHERE s_samplefamilyid=?", new Object[]{samplefamilyid});
        if (database.getNext()) {
            externalSubjectId = database.getString("externalsubject");
        }
        database.closeResultSet();
        return externalSubjectId;
    }

    public static String getRestClass(DBAccess database, String samplefamilyid) throws SapphireException {
        String rcid = "";
        database.createPreparedResultSet("SELECT restrictclassid FROM s_samplefamily WHERE s_samplefamilyid=?", new Object[]{samplefamilyid});
        if (database.getNext()) {
            rcid = database.getString("restrictclassid");
        }
        database.closeResultSet();
        return rcid;
    }

    public static void setCOCDR(ConnectionInfo connectionInfo, List samplefamilies, boolean cocflag, boolean drflag, boolean forceUpdate) throws ActionException {
        String coc = cocflag ? "Y" : "N";
        String dr = drflag ? "Y" : "N";
        ActionProcessor ap = new ActionProcessor(connectionInfo.getConnectionId());
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", SDCID);
        props.put("keyid1", RuleUtil.getStringList(samplefamilies, ";"));
        props.put("cocflag", coc);
        props.put("restrictionsflag", dr);
        props.put("__sdcruleconfirm", forceUpdate ? "Y" : "N");
        ap.processAction("EditSDI", "1", props);
    }

    private void addSubjectDocuments(DataSet primary) throws SapphireException {
        StringBuilder sql = new StringBuilder();
        ArrayList<String> familylist = new ArrayList<String>();
        for (int i = 0; i < primary.size(); ++i) {
            if (!this.hasPrimaryValueChanged(primary, i, "subjectid") || primary.getString(i, "subjectid", "").length() <= 0) continue;
            familylist.add(primary.getString(i, "s_samplefamilyid"));
        }
        if (familylist.size() > 0) {
            sql.append("select s_samplefamilyid, subjectid, sstudyid, studysiteid from s_samplefamily where s_samplefamilyid in ([])");
            DataSet ds = OpalUtil.getSQLDataSet(this.getQueryProcessor(), this.getDAMProcessor(), SDCID, sql.toString(), OpalUtil.toDelimitedString(familylist, ";"));
            if (ds != null && ds.size() > 0) {
                ds.sort("sstudyid");
                ArrayList<DataSet> studydslist = ds.getGroupedDataSets("sstudyid");
                for (DataSet o : studydslist) {
                    DataSet dataset = o;
                    if (dataset == null || dataset.size() <= 0) continue;
                    String sstudyid = dataset.getString(0, "sstudyid");
                    SafeSQL safeSQL = new SafeSQL();
                    sql.setLength(0);
                    sql.append("select formid, formversionid");
                    sql.append(" from sdiformrule");
                    sql.append(" where sdcid = 'Study'");
                    sql.append(" and keyid1 = ").append(safeSQL.addVar(sstudyid));
                    sql.append(" and formrule = 'Subject: Association'");
                    sql.append(" order by sdiformrule.usersequence");
                    DataSet dsform = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (dsform == null || dsform.size() <= 0) continue;
                    PropertyList props = new PropertyList();
                    for (int i = 0; i < dsform.size(); ++i) {
                        String formid = dsform.getString(i, "formid");
                        String formversionid = dsform.getValue(i, "formversionid");
                        for (int j = 0; j < dataset.size(); ++j) {
                            String subjectid = dataset.getString(j, "subjectid");
                            String studysiteid = dataset.getString(j, "studysiteid");
                            sql.setLength(0);
                            safeSQL.reset();
                            sql.append("select s.keyid1, d.formid, d.formversionid");
                            sql.append("  from sdidocument s, document d");
                            sql.append("  where s.documentid = d.documentid");
                            sql.append("  and s.documentversionid = d.documentversionid");
                            sql.append("  and s.sdcid = 'LV_Subject'");
                            sql.append("  and s.keyid1 = ").append(safeSQL.addVar(subjectid));
                            sql.append("  and d.formid = ").append(safeSQL.addVar(formid));
                            if (this.database.checkPreparedExists(sql.toString(), safeSQL.getValues())) continue;
                            props.clear();
                            props.setProperty("formid", formid);
                            props.setProperty("formversionid", formversionid);
                            props.setProperty("studyid", sstudyid);
                            props.setProperty("siteid", studysiteid);
                            props.setProperty("subjectid", subjectid);
                            this.getActionProcessor().processAction("AddDocument", "1", props);
                            String documentid = props.getProperty("documentid");
                            String documentversionid = props.getProperty("documentversionid");
                            props.clear();
                            props.setProperty("sdcid", "LV_Subject");
                            props.setProperty("keyid1", subjectid);
                            props.setProperty("documentid", documentid);
                            props.setProperty("documentversionid", documentversionid);
                            this.getActionProcessor().processAction("AddSDIDocument", "1", props);
                        }
                    }
                }
            }
        }
    }

    private String getResctrictionClassCOCFlag(String restrictionclassid) {
        if (this.restClassCOCFlagMap == null) {
            this.restClassCOCFlagMap = new HashMap();
        }
        if (!this.restClassCOCFlagMap.containsKey(restrictionclassid)) {
            String flag = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_restrictclass", "cocflag", "s_restrictclassid=?", new String[]{restrictionclassid});
            if (StringUtil.getLen(flag) == 0L) {
                flag = "N";
            }
            this.restClassCOCFlagMap.put(restrictionclassid, flag);
        }
        return (String)this.restClassCOCFlagMap.get(restrictionclassid);
    }

    private String getResctrictionClassDRFlag(String restrictionclassid) {
        if (this.restClassDRFlagMap == null) {
            this.restClassDRFlagMap = new HashMap();
        }
        if (!this.restClassDRFlagMap.containsKey(restrictionclassid)) {
            String flag = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_restrictclass", "restrictionsflag", "s_restrictclassid=?", new String[]{restrictionclassid});
            if (StringUtil.getLen(flag) == 0L) {
                flag = "N";
            }
            this.restClassDRFlagMap.put(restrictionclassid, flag);
        }
        return (String)this.restClassDRFlagMap.get(restrictionclassid);
    }

    private String getStudyCOCFlag(String studyid) {
        if (this.studyCOCFlagMap == null) {
            this.studyCOCFlagMap = new HashMap();
        }
        if (!this.studyCOCFlagMap.containsKey(studyid)) {
            String flag = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "conservativecocflag", "s_studyid=?", new String[]{studyid});
            if (StringUtil.getLen(flag) == 0L) {
                flag = "N";
            }
            this.studyCOCFlagMap.put(studyid, flag);
        }
        return (String)this.studyCOCFlagMap.get(studyid);
    }

    private String getStudyDRFlag(String studyid) {
        if (this.studyDRFlagMap == null) {
            this.studyDRFlagMap = new HashMap();
        }
        if (!this.studyDRFlagMap.containsKey(studyid)) {
            String flag = OpalUtil.getColumnValue(this.getQueryProcessor(), "s_study", "conservativerestrictionsflag", "s_studyid=?", new String[]{studyid});
            if (StringUtil.getLen(flag) == 0L) {
                flag = "N";
            }
            this.studyDRFlagMap.put(studyid, flag);
        }
        return (String)this.studyDRFlagMap.get(studyid);
    }
}

