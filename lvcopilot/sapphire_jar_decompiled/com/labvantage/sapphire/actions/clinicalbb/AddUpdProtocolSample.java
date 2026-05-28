/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AddUpdProtocolSample
extends BaseAction
implements sapphire.action.AddUpdProtocolSample {
    static final String LABVANTAGE_CVS_ID = "$Revision: 67311 $";
    private ArrayList nonUpdatableCols = new ArrayList();
    Map<String, String> participantRevisionCache = new HashMap<String, String>();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String siteId;
        long startime = System.currentTimeMillis();
        this.logger.info("processing  AddUpdProtocolSample action...");
        this.nonUpdatableCols = this.getNonUpdatableCols();
        TranslationProcessor tp = this.getTranslationProcessor();
        this.validateInputParams(properties, tp);
        DataSet inputParamsDS = this.createDataSetFromInputParams(properties);
        String uniqueIdentifier = "";
        uniqueIdentifier = properties.getProperty("sampleid") != null && properties.getProperty("sampleid").length() > 0 ? "keyid1" : "aliasid";
        if (properties.getProperty("kittrackitemid").length() == 0 && properties.getProperty("kitid").length() > 0) {
            this.findTrackItemForaKit(inputParamsDS, properties.getProperty("kitid"));
        }
        if ((siteId = properties.getProperty("visitedsiteid")).length() == 0) {
            if (StringUtil.getLen(properties.getProperty("visiteddepartmentid")) > 0L && StringUtil.getLen(properties.getProperty("studyid")) > 0L) {
                this.getSiteIdForGivenDepartment(inputParamsDS);
            } else if (StringUtil.getLen(properties.getProperty("visitedsitename")) > 0L && StringUtil.getLen(properties.getProperty("studyid")) > 0L) {
                this.getSiteIdForGivenSiteDesc(inputParamsDS);
            } else {
                this.getSiteIdFromParticipant(inputParamsDS, tp);
            }
        }
        if (properties.getProperty("sampleid").length() == 0 && inputParamsDS.getColumnValues("studysiteid", ";").length() == 0) {
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Missing Required Arguments:  (Sample ID or Study ID , Site ID and External Sample ID(sample alias))"));
        }
        DataSet cp4SiteDS = new DataSet();
        cp4SiteDS.addColumn("studysiteid", 0);
        cp4SiteDS.addColumn("clinicalprotocolid", 0);
        cp4SiteDS.addColumn("clinicalprotocolversionid", 0);
        cp4SiteDS.addColumn("clinicalprotocolrevision", 0);
        cp4SiteDS = this.getCPFromSiteID(inputParamsDS.getColumnValues("studysiteid", ";"), cp4SiteDS);
        DataSet subjectNEPDS = new DataSet();
        subjectNEPDS.addColumn("participantid", 0);
        subjectNEPDS.addColumn("subjectid", 0);
        subjectNEPDS.addColumn("externalparticipantid", 0);
        subjectNEPDS = this.getSubFromParticipant(inputParamsDS.getColumnValues("participantid", ";"), subjectNEPDS);
        String studyId = inputParamsDS.getColumnValues("sstudyid", ";");
        String studyCode = inputParamsDS.getColumnValues("studycode", ";");
        if (StringUtil.getLen(studyId) == 0L && StringUtil.getLen(studyCode) > 0L) {
            this.getStudyIdByAlias(inputParamsDS, studyCode);
        }
        DataSet addSampleDS = new DataSet();
        this.addSampleColumnsToDS(addSampleDS, inputParamsDS);
        DataSet editSampleDS = new DataSet();
        this.addSampleColumnsToDS(editSampleDS, inputParamsDS);
        DataSet editSFDS = new DataSet();
        this.addSFColumnsToDS(editSFDS, inputParamsDS);
        DataSet addDS4SampleAlias = new DataSet();
        this.addSampleAliasColumnsToDS(addDS4SampleAlias);
        DataSet editDS4NewSampleSF = new DataSet();
        this.addSFColumnsToDS(editDS4NewSampleSF, inputParamsDS);
        this.findSample(inputParamsDS, properties, addSampleDS, editSampleDS, editSFDS, addDS4SampleAlias, editDS4NewSampleSF, tp, cp4SiteDS, subjectNEPDS);
        String sampleListToReturn = "";
        if (addSampleDS.size() > 0) {
            int i;
            sampleListToReturn = this.ifNull(sampleListToReturn, this.addSample(addSampleDS, addDS4SampleAlias));
            String sampleFamilyList = this.findSampleFamilyForSamples(sampleListToReturn);
            String[] sampleFamilyArr = StringUtil.split(sampleFamilyList, ";");
            for (i = 0; i < editDS4NewSampleSF.size(); ++i) {
                editDS4NewSampleSF.setValue(i, "keyid1", sampleFamilyArr[i]);
            }
            if (editDS4NewSampleSF.size() > 0) {
                for (i = 0; i < editDS4NewSampleSF.size(); ++i) {
                    editSFDS.copyRow(editDS4NewSampleSF, i, 1);
                }
            }
        }
        if (editSampleDS.size() > 0) {
            this.editSample(editSampleDS);
        }
        if (editSFDS.size() > 0) {
            DataSet sampleds;
            this.editSampleFamily(editSFDS);
            List<String> sampleList = OpalUtil.toList(sampleListToReturn, ";");
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select s.s_sampleid, s.samplefamilyid, s.sampletypeid sampletypeid, pe.eventdefid,");
            if (this.connectionInfo.isOracle()) {
                sql.append(" (select s_specimendefid from s_eventdefstspecimendef sd where sd.s_eventdefid = pe.eventdefid and sd.s_sampletypeid = s.sampletypeid and sd.specimentype = t.containertypeid and rownum = 1) specimendefid,");
            } else {
                sql.append(" (select top(1) s_specimendefid from s_eventdefstspecimendef sd where sd.s_eventdefid = pe.eventdefid and sd.s_sampletypeid = s.sampletypeid and sd.specimentype = t.containertypeid) specimendefid,");
            }
            sql.append(" sf.eventdefid familyeventdefid, sf.sampletypeid familysampletypeid, sf.specimendefid familyspecimendefid");
            sql.append(" from s_sample s, s_samplefamily sf, trackitem t, s_participantevent pe");
            sql.append(" where sf.s_samplefamilyid = s.samplefamilyid");
            sql.append(" and t.linksdcid = 'Sample' and t.linkkeyid1 = s.s_sampleid");
            sql.append(" and pe.s_participanteventid = sf.participanteventid");
            if (sampleList.size() > 1000) {
                String rsetid = this.getDAMProcessor().createRSet("Sample", sampleListToReturn, null, null);
                sql.append(" and s.s_sampleid in ( select r.keyid1 from rsetitems r where r.rsetid = ? )");
                sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                this.getDAMProcessor().clearRSet(rsetid);
            } else {
                sql.append(" and s.s_sampleid in ( ").append(safeSQL.addIn(sampleList)).append(" )");
                sampleds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            if (sampleds != null && sampleds.size() > 0) {
                DataSet familyds = new DataSet();
                HashSet<String> familySet = new HashSet<String>();
                for (int i = 0; i < sampleds.size(); ++i) {
                    String samplefamilyid = sampleds.getString(i, "samplefamilyid", "");
                    if (familySet.contains(samplefamilyid)) continue;
                    familySet.add(samplefamilyid);
                    String sampletypeid = sampleds.getString(i, "sampletypeid", "");
                    String eventdefid = sampleds.getString(i, "eventdefid", "");
                    String specimendefid = sampleds.getString(i, "specimendefid", "");
                    String familysampletypeid = sampleds.getString(i, "familysampletypeid", "");
                    String familyeventdefid = sampleds.getString(i, "familyeventdefid", "");
                    String familyspecimendefid = sampleds.getString(i, "familyspecimendefid", "");
                    boolean editfamily = false;
                    if (sampletypeid.length() > 0 && familysampletypeid.length() == 0) {
                        editfamily = true;
                    } else if (eventdefid.length() > 0 && familyeventdefid.length() == 0) {
                        editfamily = true;
                    } else if (specimendefid.length() > 0 && familyspecimendefid.length() == 0) {
                        editfamily = true;
                    }
                    if (!editfamily) continue;
                    int row = familyds.addRow();
                    familyds.setString(row, "samplefamilyid", samplefamilyid);
                    familyds.setString(row, "sampletypeid", sampletypeid);
                    familyds.setString(row, "eventdefid", eventdefid);
                    familyds.setString(row, "specimendefid", specimendefid);
                }
                if (familyds.size() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_SampleFamily");
                    props.setProperty("keyid1", familyds.getColumnValues("samplefamilyid", ";"));
                    props.setProperty("sampletypeid", familyds.getColumnValues("sampletypeid", ";"));
                    props.setProperty("eventdefid", familyds.getColumnValues("eventdefid", ";"));
                    props.setProperty("specimendefid", familyds.getColumnValues("specimendefid", ";"));
                    props.setProperty("__participanteditflag", "Y");
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
            }
        }
        if (inputParamsDS.getColumnValues("kittrackitem", ";").length() > 0) {
            if (inputParamsDS.getColumnValues("kittrackitem", ";").indexOf(";") != -1 && inputParamsDS.getColumnValues("kittrackitem", ";").split(";").length > 0) {
                this.updateTrackitemStatus(inputParamsDS.getColumnValues("kittrackitem", ";"));
            } else if (inputParamsDS.getColumnValues("kittrackitem", ";").indexOf(";") == -1) {
                this.updateTrackitemStatus(inputParamsDS.getColumnValues("kittrackitem", ";"));
            }
        }
        for (int i = 0; i < inputParamsDS.size(); ++i) {
            HashMap<String, String> map = new HashMap<String, String>();
            String[] identifierArr = uniqueIdentifier.split(",");
            for (int j = 0; j < identifierArr.length; ++j) {
                map.put(identifierArr[j], inputParamsDS.getValue(i, identifierArr[j]));
            }
            int index = addSampleDS.findRow(map);
            if (index != -1) {
                inputParamsDS.setValue(i, "s_sampleid", addSampleDS.getValue(index, "keyid1"));
                continue;
            }
            int ri = editSampleDS.findRow(map);
            if (ri == -1) continue;
            inputParamsDS.setValue(i, "s_sampleid", editSampleDS.getValue(ri, "keyid1"));
        }
        properties.setProperty("newsampleid", inputParamsDS.getColumnValues("s_sampleid", ";"));
        properties.setProperty("newsamplefamilyid", this.findSampleFamilyForSamples(inputParamsDS.getColumnValues("s_sampleid", ";")));
        double took = (double)(System.currentTimeMillis() - startime) / 1000.0;
        this.logger.info("Total time to process AddUpdProtocolSample action : " + took);
    }

    private DataSet getSubFromParticipant(String participantid, DataSet subjectDS) throws SapphireException {
        String rsetid = "";
        try {
            SafeSQL safeSQL = new SafeSQL();
            String sql = " SELECT subjectid, s_participantid, externalparticipantid FROM s_participant where";
            if (StringUtil.split(participantid, ";").length <= 750) {
                sql = sql + " s_participantid in (" + safeSQL.addIn(participantid, ";") + ")";
            } else {
                rsetid = this.getDAMProcessor().createRSet("LV_Participant", participantid, null, null);
                sql = sql + " s_participantid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = " + safeSQL.addVar(rsetid) + " )";
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            for (int i = 0; i < ds.size(); ++i) {
                int row = subjectDS.addRow();
                subjectDS.setValue(row, "participantid", ds.getString(i, "s_participantid", ""));
                subjectDS.setValue(row, "subjectid", ds.getString(i, "subjectid", ""));
                subjectDS.setValue(row, "externalparticipantid", ds.getString(i, "externalparticipantid", ""));
            }
        }
        catch (SapphireException e) {
            this.logger.error("error in retrieving subjectid from participant ", e);
            throw e;
        }
        finally {
            if (OpalUtil.isNotEmpty(rsetid)) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
        return subjectDS;
    }

    private String findSampleFamilyForSamples(String sampleList) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT samplefamilyid FROM s_sample WHERE s_sampleid IN (" + safeSQL.addIn(sampleList, ";") + ")", safeSQL.getValues());
        return ds != null ? ds.getColumnValues("samplefamilyid", ";") : "";
    }

    private String ifNull(String str, String val) {
        if (str.indexOf(val) == -1) {
            str = str != null && str.length() > 0 ? str + ";" + val : val;
        }
        return str;
    }

    private String addSample(DataSet addSampleDS, DataSet addSampleAliasColumnsToDS) throws SapphireException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "Sample");
        for (int i = 0; i < addSampleDS.getColumnCount(); ++i) {
            String colid = addSampleDS.getColumnId(i);
            if (addSampleDS.getColumnValues(colid, ";").length() <= 0) continue;
            actionProps.setProperty(colid, addSampleDS.getColumnValues(colid, ";"));
        }
        this.logger.info("calling addsdi for all applicable samples " + actionProps);
        this.getActionProcessor().processAction("AddSDI", "1", actionProps);
        String sampleIds = actionProps.getProperty("newkeyid1");
        String[] arrSampleIds = StringUtil.split(sampleIds, ";");
        for (int i = 0; i < addSampleDS.size(); ++i) {
            addSampleDS.setValue(i, "keyid1", arrSampleIds[i]);
        }
        this.logger.info("adding externalsampleid to SDIAlias for Sample ");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Sample");
        props.setProperty("keyid1", sampleIds);
        props.setProperty("aliasid", addSampleAliasColumnsToDS.getColumnValues("aliasid", ";"));
        props.setProperty("aliastype", "External");
        this.getActionProcessor().processAction("AddSDIAlias", "1", props);
        return sampleIds;
    }

    private void editSample(DataSet editDataSet) throws ActionException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "Sample");
        for (int i = 0; i < editDataSet.getColumnCount(); ++i) {
            String colid = editDataSet.getColumnId(i);
            if (editDataSet.getColumnValues(colid, ";").length() <= 0) continue;
            if (editDataSet.getColumnValues(colid, ";").indexOf(";") != -1 && editDataSet.getColumnValues(colid, ";").split(";").length > 0) {
                actionProps.setProperty(colid, editDataSet.getColumnValues(colid, ";"));
                continue;
            }
            if (editDataSet.getColumnValues(colid, ";").indexOf(";") != -1) continue;
            actionProps.setProperty(colid, editDataSet.getColumnValues(colid, ";"));
        }
        if (actionProps.getProperty("keyid1").length() > 0) {
            this.logger.info("calling editsdi for all applicable samples " + actionProps);
            this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        }
    }

    private void editSampleFamily(DataSet editDataSet) throws ActionException {
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "LV_SampleFamily");
        for (int i = 0; i < editDataSet.getColumnCount(); ++i) {
            String colid = editDataSet.getColumnId(i);
            if (colid.indexOf("samplefamily_") != -1) {
                actionProps.setProperty("keyid1", editDataSet.getColumnValues(colid, ";"));
                continue;
            }
            if (editDataSet.getColumnValues(colid, ";").length() <= 0) continue;
            if (editDataSet.getColumnValues(colid, ";").indexOf(";") != -1 && editDataSet.getColumnValues(colid, ";").split(";").length > 0) {
                actionProps.setProperty(colid, editDataSet.getColumnValues(colid, ";"));
                continue;
            }
            if (editDataSet.getColumnValues(colid, ";").indexOf(";") != -1) continue;
            actionProps.setProperty(colid, editDataSet.getColumnValues(colid, ";"));
        }
        if (actionProps.getProperty("keyid1").length() > 0) {
            this.logger.info("calling editsdi for all applicable samples families " + actionProps);
            this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        }
    }

    private void getStudyIdByAlias(DataSet inputParamsDS, String studyCode) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        String rsetid = "";
        sql.append("SELECT s_studyid, studyalias FROM s_study WHERE ");
        if (StringUtil.split(studyCode, ";").length <= 750) {
            sql.append(" studyalias in (").append(safeSQL.addIn(studyCode, ";")).append(")");
        } else {
            rsetid = this.getDAMProcessor().createRSet("LV_StudySite", studyCode, null, null);
            sql = sql.append(" studyalias in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                for (int k = 0; k < inputParamsDS.size(); ++k) {
                    if (!inputParamsDS.getValue(k, "studycode", "").equals(ds.getString(i, "studyalias"))) continue;
                    inputParamsDS.setValue(k, "sstudyid", ds.getString(i, "s_studyid"));
                }
            }
        }
        if (OpalUtil.isNotEmpty(rsetid)) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    private void getSiteIdFromParticipant(DataSet inputParamsDS, TranslationProcessor tp) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        if (inputParamsDS.getColumnValues("participantid", ";").length() > 0) {
            String rsetid = "";
            String participantids = inputParamsDS.getColumnValues("participantid", ";");
            sql.append("SELECT studysiteid, s_participantid FROM s_participant p WHERE ");
            if (StringUtil.split(participantids, ";").length <= 750) {
                sql.append(" s_participantid in (").append(safeSQL.addIn(participantids, ";")).append(")");
            } else {
                rsetid = this.getDAMProcessor().createRSet("LV_Participant", participantids, null, null);
                sql.append(" s_participantid in ( select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            for (int i = 0; i < ds.size(); ++i) {
                if (ds.getString(i, "studysiteid", "").length() == 0) {
                    this.logger.info("No enrolling site found for the participant " + ds.getString(i, "s_participantid"));
                    continue;
                }
                for (int k = 0; k < inputParamsDS.size(); ++k) {
                    if (!inputParamsDS.getValue(k, "participantid", "").equals(ds.getString(i, "s_participantid"))) continue;
                    if (inputParamsDS.getValue(k, "studysiteid").length() == 0) {
                        inputParamsDS.setValue(k, "studysiteid", ds.getString(i, "studysiteid", ""));
                        continue;
                    }
                    throw new SapphireException("PROCESSACTION_FAILED", tp.translate("participant") + " " + ds.getString(i, "s_participantid") + " " + tp.translate("returned more than one site"));
                }
            }
            if (OpalUtil.isNotEmpty(rsetid)) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        } else if (inputParamsDS.getColumnValues("participanteventid", ";").length() > 0) {
            String rsetid = "";
            String participantEvents = inputParamsDS.getColumnValues("participanteventid", ";");
            sql.append(" p.s_participantid = pe.participantid and ");
            if (StringUtil.split(participantEvents, ";").length <= 750) {
                sql.append(" pe.s_participanteventid in (").append(safeSQL.addIn(participantEvents, ";")).append(")");
            } else {
                rsetid = this.getDAMProcessor().createRSet("LV_ParticipantEvent", participantEvents, null, null);
                sql.append(" pe.s_participanteventid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            }
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    if (ds.getString(i, "s_studysiteid", "").length() == 0) {
                        this.logger.info("No enrolling site found for the participantevent " + ds.getString(i, "s_participanteventid"));
                        continue;
                    }
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("participanteventid", ds.getString(i, "s_participanteventid", ""));
                    int rowIndex = inputParamsDS.findRow(map, 0);
                    if (rowIndex != -1 && inputParamsDS.getValue(rowIndex, "studysiteid").length() == 0) {
                        inputParamsDS.setValue(rowIndex, "studysiteid", ds.getString(i, "s_studysiteid", ""));
                    } else if (inputParamsDS.getValue(rowIndex, "studysiteid").length() > 0) {
                        throw new SapphireException("PROCESSACTION_FAILED", tp.translate("participant event") + " " + ds.getString(i, "s_participanteventid", "") + " " + tp.translate("returned more than one site"));
                    }
                    while (rowIndex < inputParamsDS.size()) {
                        if ((rowIndex = inputParamsDS.findRow(map, rowIndex)) != -1 && inputParamsDS.getValue(rowIndex, "studysiteid").length() == 0) {
                            inputParamsDS.setValue(rowIndex, "studysiteid", ds.getString(i, "s_studysiteid", ""));
                            continue;
                        }
                        if (inputParamsDS.getValue(rowIndex, "studysiteid").length() <= 0) continue;
                        throw new SapphireException("PROCESSACTION_FAILED", tp.translate("participant event") + " " + ds.getString(i, "s_participanteventid", "") + " " + tp.translate("returned more than one site"));
                    }
                }
            }
            if (OpalUtil.isNotEmpty(rsetid)) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }

    private void getSiteIdForGivenDepartment(DataSet inputParamsDS) throws SapphireException {
        inputParamsDS.sort("studyid");
        ArrayList<DataSet> dsAddSDIPropList = inputParamsDS.getGroupedDataSets("studyid");
        SafeSQL safeSQL = new SafeSQL();
        Iterator<DataSet> iterator = dsAddSDIPropList.iterator();
        while (iterator.hasNext()) {
            DataSet aDsAddSDIPropList;
            DataSet groupDataSet = aDsAddSDIPropList = iterator.next();
            String studyid = groupDataSet.getValue(0, "sstudyid");
            String rsetid = "";
            try {
                safeSQL.reset();
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT s_studysiteid, departmentid, sstudyid FROM s_studysite WHERE ");
                String departments = groupDataSet.getColumnValues("sitedepartment", ";");
                if (StringUtil.split(departments, ";").length <= 750) {
                    sql.append(" departmentid in (").append(safeSQL.addIn(departments, ";"));
                } else {
                    rsetid = this.getDAMProcessor().createRSet("LV_StudySite", departments, null, null);
                    sql.append(" departmentid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
                }
                sql.append(") AND sstudyid = ").append(safeSQL.addVar(studyid));
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds == null) continue;
                for (int i = 0; i < ds.size(); ++i) {
                    for (int k = 0; k < inputParamsDS.size(); ++k) {
                        if (!inputParamsDS.getValue(k, "sstudyid", "").equals(ds.getString(i, "sstudyid", "")) || !inputParamsDS.getValue(k, "sitedepartment", "").equals(ds.getString(i, "departmentid", ""))) continue;
                        inputParamsDS.setValue(k, "studysiteid", ds.getString(i, "s_studysiteid", ""));
                    }
                }
            }
            catch (SapphireException e) {
                this.logger.error("getSiteIdForGivenDepartment error", e);
                throw e;
            }
            finally {
                if (!OpalUtil.isNotEmpty(rsetid)) continue;
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }

    private void getSiteIdForGivenSiteDesc(DataSet inputParamsDS) throws SapphireException {
        inputParamsDS.sort("studyid");
        ArrayList<DataSet> dsAddSDIPropList = inputParamsDS.getGroupedDataSets("studyid");
        SafeSQL safeSQL = new SafeSQL();
        Iterator<DataSet> iterator = dsAddSDIPropList.iterator();
        while (iterator.hasNext()) {
            DataSet aDsAddSDIPropList;
            DataSet groupDataSet = aDsAddSDIPropList = iterator.next();
            String studyid = groupDataSet.getValue(0, "sstudyid");
            String sitedesc = groupDataSet.getColumnValues("sitedesc", ";");
            String rsetid = "";
            try {
                safeSQL.reset();
                StringBuilder sql = new StringBuilder();
                sql.append("SELECT s_studysiteid, studysitedesc, sstudyid FROM s_studysite WHERE ");
                if (StringUtil.split(sitedesc, ";").length <= 750) {
                    sql.append(" studysitedesc in (").append(safeSQL.addIn(sitedesc, ";"));
                } else {
                    rsetid = this.getDAMProcessor().createRSet("LV_StudySite", sitedesc, null, null);
                    sql.append(" studysitedesc in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
                }
                sql.append(") AND sstudyid = ").append(safeSQL.addVar(studyid));
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds == null) continue;
                for (int i = 0; i < ds.size(); ++i) {
                    for (int k = 0; k < inputParamsDS.size(); ++k) {
                        if (!inputParamsDS.getValue(k, "sitedesc", "").equals(ds.getString(i, "studysitedesc")) || !inputParamsDS.getValue(k, "sstudyid", "").equals(ds.getString(i, "sstudyid")) || inputParamsDS.getValue(k, "studysiteid").length() != 0) continue;
                        inputParamsDS.setValue(k, "studysiteid", ds.getString(i, "s_studysiteid", ""));
                    }
                }
            }
            catch (SapphireException e) {
                this.logger.error("getSiteIdForGivenSiteDesc error", e);
                throw e;
            }
            finally {
                if (!OpalUtil.isNotEmpty(rsetid)) continue;
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
    }

    private void addSampleAliasColumnsToDS(DataSet addDS4SampleAlias) {
        addDS4SampleAlias.addColumn("aliasid", 0);
    }

    private void addSFColumnsToDS(DataSet addDataSet, DataSet inputParamsDS) {
        for (int i = 0; i < inputParamsDS.getColumnCount(); ++i) {
            String colid = inputParamsDS.getColumnId(i);
            if (colid.contains("samplefamily_")) {
                colid = colid.split("samplefamily_")[1];
            }
            if (this.nonUpdatableCols.contains(colid) || colid.equals("s_sampleid") || colid.equals("sampletypeid")) continue;
            addDataSet.addColumn(colid, 0);
        }
        addDataSet.addColumn("clinicalprotocolversionid", 0);
        addDataSet.addColumn("clinicalprotocolrevision", 0);
        addDataSet.addColumn("clinicalprotocolid", 0);
        addDataSet.addColumn("kittrackitem", 0);
        addDataSet.addColumn("participantid", 0);
        addDataSet.addColumn("participanteventid", 0);
        addDataSet.addColumn("clinicalevent", 0);
        addDataSet.addColumn("externalsubject", 0);
        addDataSet.addColumn("subjectid", 0);
        addDataSet.addColumn("restrictclassid", 0);
        addDataSet.addColumn("collectiondt", 0);
        addDataSet.addColumn("studysiteid", 0);
        addDataSet.addColumn("sstudyid", 0);
        addDataSet.addColumn("keyid1", 0);
    }

    private void addSampleColumnsToDS(DataSet addDataSet, DataSet inputParamsDS) {
        for (int i = 0; i < inputParamsDS.getColumnCount(); ++i) {
            String colid = inputParamsDS.getColumnId(i);
            if (colid.contains("sample_")) {
                colid = colid.split("sample_")[1];
            }
            if (this.nonUpdatableCols.contains(colid) || colid.equals("s_sampleid")) continue;
            addDataSet.addColumn(colid, 0);
        }
        addDataSet.addColumn("sstudyid", 0);
        addDataSet.addColumn("sampletypeid", 0);
        addDataSet.addColumn("specimentype", 0);
        addDataSet.addColumn("auditreason", 0);
        addDataSet.addColumn("auditactivity", 0);
        addDataSet.addColumn("auditsignedflag", 0);
        addDataSet.addColumn("keyid1", 0);
    }

    public void findSample(DataSet inputParamsDS, PropertyList properties, DataSet addSampleDS, DataSet editSampleDS, DataSet editSFDS, DataSet addDS4SampleAlias, DataSet editDS4NewSampleSF, TranslationProcessor tp, DataSet cp4SiteDS, DataSet subjectNEPDS) throws SapphireException {
        if (properties.getProperty("sampleid") != null && properties.getProperty("sampleid").length() > 0) {
            this.logger.info("Use sampleid to get the existing data in database...");
            this.generateDSToEditSampleNSF(inputParamsDS, properties, editSampleDS, editSFDS, tp, cp4SiteDS, subjectNEPDS);
        } else if (properties.getProperty("externalsampleid") != null && properties.getProperty("externalsampleid").length() > 0) {
            this.logger.info("Use samplealias to get the existing data in database...");
            DataSet sampleDS = this.findSampleFromSampleAlias(inputParamsDS, properties);
            this.generateAddNEditDataSet(inputParamsDS, sampleDS, addSampleDS, editSampleDS, editSFDS, addDS4SampleAlias, cp4SiteDS, editDS4NewSampleSF, subjectNEPDS, tp);
        } else {
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Missing Required Arguments:  (Sample ID or Study ID , Site ID and External Sample ID(sample alias))"));
        }
    }

    private void generateAddNEditDataSet(DataSet inputParamsDS, DataSet sampleDS, DataSet addSampleDS, DataSet editSampleDS, DataSet editSFDS, DataSet addDS4SampleAlias, DataSet cp4SiteDS, DataSet editDS4NewSampleSF, DataSet subjectDS, TranslationProcessor tp) throws SapphireException {
        int addDataSetIndex = 0;
        int editDataSetIndex = 0;
        for (int i = 0; i < inputParamsDS.size(); ++i) {
            String inputAlias = inputParamsDS.getValue(i, "aliasid");
            String siteId = inputParamsDS.getValue(i, "studysiteid");
            HashMap<String, String> findMap = new HashMap<String, String>();
            findMap.put("aliasid", inputAlias);
            findMap.put("studysiteid", siteId);
            int index = sampleDS.findRow(findMap);
            if (index == -1) {
                this.addRowToAddSampleDataSet(inputParamsDS, i, addSampleDS, addDS4SampleAlias, addDataSetIndex);
                this.addRowtoEditSFDataSet(inputParamsDS, i, editDS4NewSampleSF, addDataSetIndex, cp4SiteDS, subjectDS, tp);
                ++addDataSetIndex;
                continue;
            }
            this.updateSampleIfReqd(sampleDS, index, inputParamsDS, i, editSampleDS, editDataSetIndex);
            this.editSampleFamily(inputParamsDS, sampleDS, index, sampleDS.getValue(index, "s_samplefamilyid"), i, inputParamsDS.getValue(i, "trackitemid"), editSFDS, cp4SiteDS, editDataSetIndex, subjectDS, tp);
            ++editDataSetIndex;
        }
    }

    private void addRowtoEditSFDataSet(DataSet inputParamsDS, int dsRowIndex, DataSet editDS4NewSampleSF, int addDataSetIndex, DataSet cp4SiteDS, DataSet subjectDS, TranslationProcessor tp) throws SapphireException {
        editDS4NewSampleSF.addRow();
        for (int i = 0; i < inputParamsDS.getColumnCount(); ++i) {
            String colidInEditDS = "";
            String colid = inputParamsDS.getColumnId(i);
            colidInEditDS = colid.contains("samplefamily_") ? colid.split("samplefamily_")[1] : colid;
            if (this.nonUpdatableCols.contains(colid) || colid.equals("s_sampleid") || colid.equals("sampletypeid")) continue;
            editDS4NewSampleSF.setValue(addDataSetIndex, colidInEditDS, inputParamsDS.getValue(dsRowIndex, colid));
        }
        int rowIndex = cp4SiteDS.findRow("studysiteid", inputParamsDS.getValue(dsRowIndex, "studysiteid"));
        String cpid = cp4SiteDS.getValue(rowIndex, "clinicalprotocolid");
        String cpversionid = cp4SiteDS.getValue(rowIndex, "clinicalprotocolversionid");
        String cprevision = cp4SiteDS.getValue(rowIndex, "clinicalprotocolrevision");
        String rcid = inputParamsDS.getValue(dsRowIndex, "restrictclassid", "");
        if (rcid.length() > 0 && this.isValidRC4Study(rcid, inputParamsDS.getValue(dsRowIndex, "sstudyid", ""))) {
            editDS4NewSampleSF.setValue(addDataSetIndex, "restrictclassid", rcid);
        } else if (rcid.length() > 0 && !this.isValidRC4Study(rcid, inputParamsDS.getValue(dsRowIndex, "sstudyid", ""))) {
            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Following Restriction Class is not a valid Active Restriction Class for the study. <br>RestrictionClassId: ") + rcid);
        }
        editDS4NewSampleSF.setValue(addDataSetIndex, "clinicalprotocolid", cpid);
        editDS4NewSampleSF.setValue(addDataSetIndex, "clinicalprotocolversionid", cpversionid);
        editDS4NewSampleSF.setValue(addDataSetIndex, "clinicalprotocolrevision", cprevision);
        int rowIndex4MatchingPart = subjectDS.findRow("participantid", inputParamsDS.getValue(dsRowIndex, "participantid"));
        String subjectid = subjectDS.getValue(rowIndex4MatchingPart, "subjectid");
        editDS4NewSampleSF.setValue(addDataSetIndex, "subjectid", subjectid);
        editDS4NewSampleSF.setValue(addDataSetIndex, "externalsubject", subjectDS.getValue(rowIndex4MatchingPart, "externalparticipantid"));
        String[] cols = new String[]{"sstudyid", "studysiteid", "collectiondt", "clinicalevent", "participantid", "participanteventid"};
        String[] dscols = new String[]{"sstudyid", "studysiteid", "collectiondt", "clinicalevent", "participantid", "participanteventid"};
        for (int j = 0; j < cols.length; ++j) {
            editDS4NewSampleSF.setValue(addDataSetIndex, cols[j], inputParamsDS.getValue(dsRowIndex, dscols[j], ""));
        }
    }

    private void addRowToAddSampleDataSet(DataSet inputParamsDS, int rowIndex, DataSet addSampleDS, DataSet addDS4SampleAlias, int addDataSetIndex) {
        addSampleDS.addRow();
        for (int i = 0; i < inputParamsDS.getColumnCount(); ++i) {
            String colidInSampleDS = "";
            String colid = inputParamsDS.getColumnId(i);
            colidInSampleDS = colid.contains("sample_") ? colid.split("sample_")[1] : colid;
            if (this.nonUpdatableCols.contains(colid) || colid.equals("s_sampleid")) continue;
            addSampleDS.setValue(addDataSetIndex, colidInSampleDS, inputParamsDS.getValue(rowIndex, colid));
        }
        addSampleDS.setValue(addDataSetIndex, "sstudyid", inputParamsDS.getValue(rowIndex, "sstudyid"));
        addSampleDS.setValue(addDataSetIndex, "sampletypeid", inputParamsDS.getValue(rowIndex, "sampletypeid"));
        addSampleDS.setValue(addDataSetIndex, "specimentype", inputParamsDS.getValue(rowIndex, "specimentype"));
        addSampleDS.setValue(addDataSetIndex, "auditreason", inputParamsDS.getValue(rowIndex, "auditreason"));
        addSampleDS.setValue(addDataSetIndex, "auditactivity", inputParamsDS.getValue(rowIndex, "auditactivity"));
        addSampleDS.setValue(addDataSetIndex, "auditsignedflag", inputParamsDS.getValue(rowIndex, "auditsignedflag"));
        addDS4SampleAlias.addRow();
        addDS4SampleAlias.setValue(addDataSetIndex, "aliasid", inputParamsDS.getValue(rowIndex, "aliasid"));
    }

    private DataSet findSampleFromSampleAlias(DataSet inputParamsDS, PropertyList properties) throws SapphireException {
        DataSet sampleDS = new DataSet();
        ArrayList cols2BSelected = this.getExtraColsList(properties, "Sample", "sample_");
        ArrayList sfCols2BSelected = this.getExtraColsList(properties, "LV_SampleFamily", "samplefamily_");
        inputParamsDS.sort("studysiteid");
        ArrayList<DataSet> groupedInputDS = inputParamsDS.getGroupedDataSets("studysiteid");
        SafeSQL safeSQL = new SafeSQL();
        for (DataSet groupedInputD : groupedInputDS) {
            String rsetid = "";
            String siteid = groupedInputD.getValue(0, "studysiteid");
            String sampleAlias = groupedInputD.getColumnValues("aliasid", ";");
            safeSQL.reset();
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT s.s_sampleid, s.samplefamilyid, s.sampletypeid, s.sstudyid, s.specimentype, ");
            sql.append(" s_samplefamily.clinicalprotocolid, s_samplefamily.clinicalprotocolversionid, s_samplefamily.clinicalprotocolrevision, ").append("s_samplefamily.restrictclassid, s_samplefamily.studysiteid, s_samplefamily.kittrackitem, s_samplefamily.participanteventid, ").append("sdialias.aliasid ");
            for (Object aCols2BSelected : cols2BSelected) {
                sql.append(", s.").append(aCols2BSelected);
            }
            for (Object aSfCols2BSelected : sfCols2BSelected) {
                sql.append(", s_samplefamily.").append(aSfCols2BSelected);
            }
            sql.append(" FROM s_sample s LEFT OUTER JOIN sdialias ON sdialias.sdcid='Sample' AND sdialias.keyid1 = s.s_sampleid ");
            sql.append(" LEFT OUTER JOIN s_samplefamily ON s_samplefamily.s_samplefamilyid  = s.samplefamilyid WHERE ");
            if (StringUtil.split(sampleAlias, ";").length <= 750) {
                sql.append(" sdialias.aliasid in (").append(safeSQL.addIn(inputParamsDS.getColumnValues("aliasid", "', '"))).append(") ");
            } else {
                rsetid = this.getDAMProcessor().createRSet("Sample", sampleAlias, null, null);
                sql.append(" sdialias.aliasid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
            }
            sql.append(" AND s_samplefamily.studysiteid = ").append(safeSQL.addVar(siteid)).append(" AND sdialias.aliastype = 'External'");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    sampleDS.copyRow(ds, i, 1);
                }
            }
            if (!OpalUtil.isNotEmpty(rsetid)) continue;
            this.getDAMProcessor().clearRSet(rsetid);
        }
        return sampleDS;
    }

    private void generateDSToEditSampleNSF(DataSet inputParamsDS, PropertyList properties, DataSet editSampleDS, DataSet editSFDS, TranslationProcessor tp, DataSet cp4SiteDS, DataSet subjectNEPDS) throws SapphireException {
        String sampleid = inputParamsDS.getColumnValues("s_sampleid", ";");
        String rsetid = "";
        ArrayList cols2BSelected = this.getExtraColsList(properties, "Sample", "sample_");
        ArrayList sfCols2BSelected = this.getExtraColsList(properties, "LV_SampleFamily", "samplefamily_");
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT s.s_sampleid, s.samplefamilyid, s.sampletypeid, s.sstudyid, s.specimentype, ");
        sql.append(" s_samplefamily.clinicalprotocolid, s_samplefamily.clinicalprotocolversionid, s_samplefamily.clinicalprotocolrevision, ").append("s_samplefamily.restrictclassid, s_samplefamily.studysiteid, s_samplefamily.kittrackitem, s_samplefamily.participanteventid ");
        for (Object aCols2BSelected : cols2BSelected) {
            sql.append(", s.").append(aCols2BSelected);
        }
        for (Object aSfCols2BSelected : sfCols2BSelected) {
            sql.append(", s_samplefamily.").append(aSfCols2BSelected);
        }
        sql.append(" FROM s_sample s LEFT OUTER JOIN sdialias ON sdialias.sdcid='Sample' AND sdialias.keyid1 = s.s_sampleid ");
        sql.append(" LEFT OUTER JOIN s_samplefamily ON s_samplefamily.s_samplefamilyid  = s.samplefamilyid ");
        if (StringUtil.split(sampleid, ";").length <= 750) {
            sql.append(" WHERE s.s_sampleid in (").append(safeSQL.addIn(sampleid, ";"));
        } else {
            rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
            sql.append(" WHERE s.s_sampleid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = ").append(safeSQL.addVar(rsetid));
        }
        sql.append(") order by s.s_sampleid");
        DataSet sampleDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (StringUtil.getLen(rsetid) > 0L) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        if (sampleDS == null || sampleDS.size() == 0) {
            throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Sampleid supplied to the action is invalid"));
        }
        this.generateEditDS(sampleDS, inputParamsDS, tp, editSampleDS, editSFDS, cp4SiteDS, subjectNEPDS);
    }

    private void generateEditDS(DataSet sampleDataInDB, DataSet inputParamsDS, TranslationProcessor tp, DataSet editSampleDS, DataSet editSFDS, DataSet cp4SiteDS, DataSet subjectNEPDS) throws SapphireException {
        for (int i = 0; i < sampleDataInDB.size(); ++i) {
            int index = inputParamsDS.findRow("s_sampleid", sampleDataInDB.getValue(i, "s_sampleid", ""));
            this.updateSampleIfReqd(sampleDataInDB, i, inputParamsDS, index, editSampleDS, i);
            this.editSampleFamily(inputParamsDS, sampleDataInDB, i, sampleDataInDB.getValue(i, "s_samplefamilyid"), index, inputParamsDS.getValue(i, "trackitemid"), editSFDS, cp4SiteDS, i, subjectNEPDS, tp);
        }
    }

    private void updateSampleIfReqd(DataSet sampleDatainDB, int sampleDataIndex, DataSet inputds, int inputDSIndex, DataSet editDataSet, int editDataSetIndex) throws SapphireException {
        String sampleTypeId = inputds.getValue(inputDSIndex, "sampletypeid", "");
        String studyId = inputds.getValue(inputDSIndex, "sstudyid", "");
        String specimenType = inputds.getValue(inputDSIndex, "specimentype", "");
        editDataSet.addRow();
        editDataSet.setValue(editDataSetIndex, "sampletypeid", sampleTypeId);
        editDataSet.setValue(editDataSetIndex, "sstudyid", studyId);
        editDataSet.setValue(editDataSetIndex, "specimentype", specimenType);
        editDataSet.setValue(editDataSetIndex, "auditreason", inputds.getValue(inputDSIndex, "auditreason", ""));
        editDataSet.setValue(editDataSetIndex, "auditactivity", inputds.getValue(inputDSIndex, "auditactivity", ""));
        editDataSet.setValue(editDataSetIndex, "auditsignedflag", inputds.getValue(inputDSIndex, "auditsignedflag", ""));
        editDataSet.setValue(editDataSetIndex, "keyid1", sampleDatainDB.getValue(sampleDataIndex, "s_sampleid", ""));
        if (inputds.getValue(inputDSIndex, "aliasid", "").length() > 0) {
            editDataSet.setValue(editDataSetIndex, "aliasid", inputds.getValue(inputDSIndex, "aliasid", ""));
        }
    }

    private DataSet getCPFromSiteID(String siteId, DataSet cp4SiteDS) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String rsetid = "";
        String sql = "SELECT ss.s_studysiteid, cp.s_clinicalprotocolid, cp.s_clinicalprotocolversionid, cp.s_clinicalprotocolrevision  FROM s_studysite ss, s_clinicalprotocol cp WHERE ";
        if (StringUtil.split(siteId, ";").length <= 750) {
            sql = sql + " ss.s_studysiteid in (" + safeSQL.addIn(siteId, ";") + ") ";
        } else {
            rsetid = this.getDAMProcessor().createRSet("LV_StudySite", siteId, null, null);
            sql = sql + " ss.s_studysiteid in (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = " + safeSQL.addVar(rsetid) + ")";
        }
        sql = sql + " and ss.clinicalprotocolid = cp.s_clinicalprotocolid  and cp.versionstatus = 'C'  and ss.clinicalprotocolrevision = cp.s_clinicalprotocolrevision";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (int i = 0; i < ds.size(); ++i) {
            int row = cp4SiteDS.addRow();
            cp4SiteDS.setValue(row, "studysiteid", ds.getString(i, "s_studysiteid", ""));
            cp4SiteDS.setValue(row, "clinicalprotocolid", ds.getString(i, "s_clinicalprotocolid", ""));
            cp4SiteDS.setValue(row, "clinicalprotocolrevision", ds.getString(i, "s_clinicalprotocolrevision", ""));
            cp4SiteDS.setValue(row, "clinicalprotocolversionid", ds.getString(i, "s_clinicalprotocolversionid", ""));
        }
        if (OpalUtil.isNotEmpty(rsetid)) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
        return cp4SiteDS;
    }

    private void updateTrackitemStatus(String trackitemid) throws SapphireException {
        if (trackitemid != null && trackitemid.length() > 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "TrackItemSDC");
            actionProps.setProperty("keyid1", trackitemid);
            actionProps.setProperty("trackitemstatus", "Used");
            this.getActionProcessor().processAction("EditSDI", "1", actionProps);
        }
    }

    private void findTrackItemForaKit(DataSet inputParamsDS, String kitid) throws SapphireException {
        String rsetid = "";
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT trackitemid, trackitemlabel FROM trackitem WHERE ";
        if (StringUtil.split(kitid, ";").length <= 750) {
            sql = sql + " trackitemlabel IN (" + safeSQL.addIn(kitid, ";") + ")";
        } else {
            rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", kitid, null, null);
            sql = sql + " trackitemlabel IN (select rsetitems.keyid1 from rsetitems where rsetitems.rsetid = " + safeSQL.addVar(rsetid) + ")";
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                for (int j = 0; j < inputParamsDS.size(); ++j) {
                    if (!ds.getValue(i, "trackitemlabel", "").equals(inputParamsDS.getValue(j, "kitid"))) continue;
                    inputParamsDS.setValue(j, "kittrackitem", ds.getValue(i, "trackitemid", ""));
                }
            }
        }
        if (OpalUtil.isNotEmpty(rsetid)) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    private ArrayList getNonUpdatableCols() {
        this.nonUpdatableCols.add("moddt");
        this.nonUpdatableCols.add("modby");
        this.nonUpdatableCols.add("modtool");
        this.nonUpdatableCols.add("createdt");
        this.nonUpdatableCols.add("createby");
        this.nonUpdatableCols.add("createtool");
        this.nonUpdatableCols.add("auditsequence");
        this.nonUpdatableCols.add("templateflag");
        return this.nonUpdatableCols;
    }

    private void editSampleFamily(DataSet ds, DataSet sampleDatainDB, int sampleDSIndex, String samplefamilyid, int dsRowIndex, String trackitemid, DataSet editSFDS, DataSet cp4SiteDS, int editDataSetIndex, DataSet subjectNEPDS, TranslationProcessor tp) throws SapphireException {
        boolean dataChanged = this.hasDataChanged(sampleDatainDB, sampleDSIndex, ds, dsRowIndex, trackitemid, cp4SiteDS);
        if (dataChanged) {
            String participantProtocolRevision;
            editSFDS.addRow();
            for (int col = 0; col < ds.getColumnCount(); ++col) {
                String colid = ds.getColumnId(col);
                String colidInEditDS = "";
                colidInEditDS = colid.indexOf("samplefamily_") != -1 ? colid.split("samplefamily_")[1] : colid;
                editSFDS.setValue(editDataSetIndex, colidInEditDS, ds.getValue(dsRowIndex, colid, ""));
            }
            editSFDS.setValue(editDataSetIndex, "kittrackitem", trackitemid);
            int rowIndex = cp4SiteDS.findRow("studysiteid", ds.getValue(dsRowIndex, "studysiteid"));
            String cpid = cp4SiteDS.getValue(rowIndex, "clinicalprotocolid");
            String cpversionid = cp4SiteDS.getValue(rowIndex, "clinicalprotocolversionid");
            String cprevision = cp4SiteDS.getValue(rowIndex, "clinicalprotocolrevision");
            String rcid = ds.getValue(dsRowIndex, "restrictclassid", "");
            if (rcid.length() > 0 && this.isValidRC4Study(rcid, ds.getValue(dsRowIndex, "sstudyid", ""))) {
                editSFDS.setValue(editDataSetIndex, "restrictclassid", rcid);
            } else if (rcid.length() > 0 && !this.isValidRC4Study(rcid, ds.getValue(dsRowIndex, "sstudyid", ""))) {
                throw new SapphireException("PROCESSACTION_FAILED", tp.translate("Following Restriction Class is not a valid Active Restriction Class for the study. <br>RestrictionClassId: ") + rcid);
            }
            String participantid = ds.getValue(dsRowIndex, "participantid");
            if (OpalUtil.isNotEmpty(participantid) && OpalUtil.isNotEmpty(participantProtocolRevision = this.getParticipantProtocolRevision(participantid))) {
                cprevision = participantProtocolRevision;
            }
            editSFDS.setValue(editDataSetIndex, "clinicalprotocolid", cpid);
            editSFDS.setValue(editDataSetIndex, "clinicalprotocolversionid", cpversionid);
            editSFDS.setValue(editDataSetIndex, "clinicalprotocolrevision", cprevision);
            int rowIndex4MatchingPart = subjectNEPDS.findRow("participantid", participantid);
            String subjectid = subjectNEPDS.getValue(rowIndex4MatchingPart, "subjectid");
            String externalparticipantid = subjectNEPDS.getValue(rowIndex4MatchingPart, "externalparticipantid");
            editSFDS.setValue(editDataSetIndex, "subjectid", subjectid);
            editSFDS.setValue(editDataSetIndex, "externalsubject", externalparticipantid);
            String[] cols = new String[]{"sstudyid", "studysiteid", "collectiondt", "clinicalevent", "participantid", "participanteventid"};
            String[] dscols = new String[]{"sstudyid", "studysiteid", "collectiondt", "clinicalevent", "participantid", "participanteventid"};
            for (int j = 0; j < cols.length; ++j) {
                if (sampleDatainDB.getValue(sampleDSIndex, cols[j]).length() != 0) continue;
                editSFDS.setValue(editDataSetIndex, cols[j], ds.getValue(dsRowIndex, dscols[j], ""));
            }
            editSFDS.setValue(editDataSetIndex, "keyid1", sampleDatainDB.getValue(sampleDSIndex, "samplefamilyid"));
        }
    }

    private String getParticipantProtocolRevision(String participantid) {
        String key = "participant_" + participantid;
        if (!this.participantRevisionCache.containsKey(key)) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select clinicalprotocolrevision from s_participant where s_participantid = ?", (Object[])new String[]{participantid});
            this.participantRevisionCache.put(key, ds != null && ds.size() > 0 ? ds.getString(0, "clinicalprotocolrevision", "") : "");
        }
        return this.participantRevisionCache.get(key);
    }

    private boolean hasDataChanged(DataSet sampleDatainDB, int sampleDSIndex, DataSet ds, int dsRowIndex, String trackitemid, DataSet cp4SiteDS) {
        boolean dataChanged = false;
        for (int i = 0; i < ds.getColumnCount(); ++i) {
            String inputval;
            String colid = ds.getColumnId(i);
            if (colid.indexOf("samplefamily_") != -1) {
                colid = colid.split("samplefamily_")[1];
            }
            String val = ds.getValue(dsRowIndex, colid);
            if (sampleDatainDB.getColumnType(colid) == -1 || (inputval = sampleDatainDB.getValue(sampleDSIndex, colid)).equals(val)) continue;
            dataChanged = true;
            break;
        }
        if (trackitemid.length() > 0 && !trackitemid.equals(sampleDatainDB.getValue(sampleDSIndex, "kittrackitem", ""))) {
            dataChanged = true;
        }
        int rowIndex = cp4SiteDS.findRow("studysiteid", ds.getValue(dsRowIndex, "studysiteid"));
        String cpid = cp4SiteDS.getValue(rowIndex, "clinicalprotocolid");
        String cpversionid = cp4SiteDS.getValue(rowIndex, "clinicalprotocolversionid");
        String cprevision = cp4SiteDS.getValue(rowIndex, "clinicalprotocolrevision");
        String rcid = ds.getValue(dsRowIndex, "restrictclassid", "");
        if (rcid != null && rcid.length() > 0 && !sampleDatainDB.getValue(sampleDSIndex, "restrictclassid", "").equals(rcid)) {
            dataChanged = true;
        }
        if (cpid != null && cpid.length() > 0 && !sampleDatainDB.getValue(sampleDSIndex, "clinicalprotocolid", "").equals(cpid)) {
            dataChanged = true;
        }
        if (cpversionid != null && cpversionid.length() > 0 && !sampleDatainDB.getValue(sampleDSIndex, "clinicalprotocolversionid", "").equals(cpversionid)) {
            dataChanged = true;
        }
        if (cprevision != null && cprevision.length() > 0 && !sampleDatainDB.getValue(sampleDSIndex, "clinicalprotocolrevision", "").equals(cprevision)) {
            dataChanged = true;
        }
        return dataChanged;
    }

    private void validateInputParams(PropertyList properties, TranslationProcessor tp) throws SapphireException {
        int maxlength = this.findMaxLength(properties);
        for (Object o : properties.keySet()) {
            String param = (String)o;
            String value = properties.getProperty(param);
            int length = StringUtil.split(value, ";").length;
            if (maxlength <= length || length <= 1) continue;
            throw new SapphireException("INVALID_PARAMETERS", tp.translate("Invalid Input:") + param + " " + tp.translate("Valid Input: All data fields, barring those with only 1 value, should have the same number of semicolon-seperated-values."));
        }
    }

    private int findMaxLength(PropertyList properties) {
        int maxlength = 0;
        for (Object o : properties.keySet()) {
            String param = (String)o;
            String value = properties.getProperty(param);
            int l = StringUtil.split(value, ";").length;
            if (l <= maxlength) continue;
            maxlength = l;
        }
        return maxlength;
    }

    private DataSet createDataSetFromInputParams(PropertyList properties) {
        String collectiondate = properties.getProperty("collectiondate", "");
        DataSet ds = new DataSet();
        ds.addColumnValues("s_sampleid", 0, properties.getProperty("sampleid", ""), ";");
        ds.addColumnValues("aliasid", 0, properties.getProperty("externalsampleid", ""), ";");
        ds.addColumnValues("sstudyid", 0, properties.getProperty("studyid", ""), ";");
        ds.addColumnValues("studysiteid", 0, properties.getProperty("visitedsiteid", ""), ";");
        ds.addColumnValues("participantid", 0, properties.getProperty("participantid", ""), ";");
        ds.addColumnValues("externalparticipantid", 0, properties.getProperty("externalparticipantid", ""), ";");
        ds.addColumnValues("participanteventid", 0, properties.getProperty("eventid", ""), ";");
        ds.addColumnValues("clinicalevent", 0, properties.getProperty("eventlabel", ""), ";");
        ds.addColumnValues("collectiondt", 0, collectiondate, ";");
        ds.addColumnValues("sampletypeid", 0, properties.getProperty("sampletypeid", ""), ";");
        ds.addColumnValues("sitedepartment", 0, properties.getProperty("visiteddepartmentid", ""), ";");
        ds.addColumnValues("sitedesc", 0, properties.getProperty("visitedsitename", ""), ";");
        ds.addColumnValues("auditreason", 0, properties.getProperty("auditreason", ""), ";");
        ds.addColumnValues("auditactivity", 0, properties.getProperty("auditactivity", ""), ";");
        ds.addColumnValues("auditsignedflag", 0, properties.getProperty("auditsignedflag", "N"), ";");
        ds.addColumnValues("studycode", 0, properties.getProperty("studycode", ""), ";");
        ds.addColumnValues("kitid", 0, properties.getProperty("kitid", ""), ";");
        ds.addColumnValues("kittrackitem", 0, properties.getProperty("kittrackitemid", ""), ";");
        ds.addColumnValues("specimentype", 0, properties.getProperty("specimentype", ""), ";");
        ds.addColumnValues("restrictclassid", 0, properties.getProperty("restrictclassid", ""), ";");
        ds.padColumns();
        ds = this.addToDataSet("LV_SampleFamily", "samplefamily_", ds, properties);
        ds = this.addToDataSet("Sample", "sample_", ds, properties);
        return ds;
    }

    private DataSet addToDataSet(String sdcid, String colPrefix, DataSet ds, PropertyList properties) {
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyListCollection columns = sdcProcessor.getColumns(sdcid);
        int rowCount = ds.getRowCount();
        for (int col = 0; col < columns.size(); ++col) {
            PropertyList column = columns.getPropertyList(col);
            String colid = column.getProperty("columnid").toLowerCase();
            if (!properties.containsKey(colPrefix + colid) || this.nonUpdatableCols.contains(colid)) continue;
            String userProp = properties.getProperty(colPrefix + colid, "");
            if (!userProp.contains(";")) {
                StringUtil.repeat(userProp, rowCount, ";");
            }
            ds.addColumnValues(colPrefix + colid, 0, userProp, ";");
        }
        return ds;
    }

    private ArrayList getExtraColsList(PropertyList properties, String sdcid, String colPrefix) {
        ArrayList<String> cols = new ArrayList<String>();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyListCollection columns = sdcProcessor.getColumns(sdcid);
        for (int col = 0; col < columns.size(); ++col) {
            PropertyList column = columns.getPropertyList(col);
            String colid = column.getProperty("columnid").toLowerCase();
            if (!properties.containsKey(colPrefix + colid) || this.nonUpdatableCols.contains(colid)) continue;
            cols.add(colid);
        }
        return cols;
    }

    private boolean isValidRC4Study(String rcid, String studyid) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT s_restrictclassid FROM s_restrictclass WHERE sstudyid = " + safeSQL.addVar(studyid) + " AND activeflag = 'Y' AND s_restrictclassid =" + safeSQL.addVar(rcid);
        return this.database.checkPreparedExists(sql, safeSQL.getValues());
    }

    static class UserMessages {
        static final String MISSINGREQARGS = "Missing Required Arguments:  (Sample ID or Study ID , Site ID and External Sample ID(sample alias))";
        static final String INVALIDINPUT = "Invalid Input:";
        static final String MULTIPARAMINPUT = "Valid Input: All data fields, barring those with only 1 value, should have the same number of semicolon-seperated-values.";
        static final String MORETHANONESITE = "returned more than one site";
        static final String INVALIDRCID = "Following Restriction Class is not a valid Active Restriction Class for the study. <br>RestrictionClassId: ";
        static final String IMVALIDSAMPLE = "Sampleid supplied to the action is invalid";

        UserMessages() {
        }
    }
}

