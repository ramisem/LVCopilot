/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.clinicalbb;

import com.labvantage.opal.util.OpalUtil;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AddProtocolTest
extends BaseAction
implements sapphire.action.AddProtocolTest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 76593 $";
    Map<String, String> sampleStudyCache = new HashMap<String, String>();

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        this.logger.info("processing  AddProtocolTest action...");
        long startime = System.currentTimeMillis();
        String auditreason = properties.getProperty("auditreason");
        String auditactivity = properties.getProperty("auditactivity", "");
        String auditsignedflag = properties.getProperty("auditsignedflag", "N");
        DataSet SDIWorkItemDS = new DataSet();
        SDIWorkItemDS.addColumn("keyid1", 0);
        SDIWorkItemDS.addColumn("s_assaytypeid", 0);
        SDIWorkItemDS.addColumn("workitemid", 0);
        SDIWorkItemDS.addColumn("workitemversionid", 0);
        TranslationProcessor tp = this.getTranslationProcessor();
        String sampleIds = properties.getProperty("sampleid");
        if (sampleIds.length() == 0) {
            throw new SapphireException("INVALID_PARAMETER", tp.translate("Missing Required Arguments:  Sample ID"));
        }
        HashMap<String, DataSet> datasetHM = this.findWIsNSamplesForGivenSample(sampleIds);
        DataSet wids = datasetHM.get("wids");
        DataSet sampleds = datasetHM.get("sampleDS");
        String receivedSamples = sampleds.getColumnValues("sampleinput", ";");
        String[] sampleIdArr = StringUtil.split(properties.getProperty("sampleid"), ";");
        for (int i = 0; i < sampleIdArr.length; ++i) {
            if (!receivedSamples.contains(sampleIdArr[i])) continue;
            int rowIndex = sampleds.findRow("s_sampleid", sampleIdArr[i]);
            String specimenType = sampleds.getValue(rowIndex, "specimentype", "");
            int sampleOrder = i + 1;
            SDIWorkItemDS = specimenType.length() > 0 && !specimenType.equals("null") ? this.findSpecTypeTest(SDIWorkItemDS, sampleIdArr[i], wids, sampleOrder, specimenType, sampleds) : this.findGlobalTests(SDIWorkItemDS, sampleIdArr[i], wids, sampleOrder);
        }
        this.addTests(SDIWorkItemDS, auditreason, auditactivity, auditsignedflag, tp);
        this.logger.info("Total time to process AddProtocolTest action : " + (double)(System.currentTimeMillis() - startime) / 1000.0);
    }

    private void addTests(DataSet sdiWorkItemDS, String auditreason, String auditactivity, String auditsignedflag, TranslationProcessor tp) throws SapphireException {
        if ((sdiWorkItemDS = this.checkIfSampleHasWI(sdiWorkItemDS)).size() > 0) {
            PropertyList plWorkItemProps = new PropertyList();
            plWorkItemProps.setProperty("sdcid", "Sample");
            plWorkItemProps.setProperty("workitemid", sdiWorkItemDS.getColumnValues("workitemid", ";"));
            plWorkItemProps.setProperty("workitemversionid", sdiWorkItemDS.getColumnValues("workitemversionid", ";"));
            plWorkItemProps.setProperty("keyid1", sdiWorkItemDS.getColumnValues("keyid1", ";"));
            plWorkItemProps.setProperty("keyid2", "");
            plWorkItemProps.setProperty("keyid3", "");
            plWorkItemProps.setProperty("auditreason", auditreason);
            plWorkItemProps.setProperty("auditactivity", auditactivity);
            plWorkItemProps.setProperty("auditsignedflag", auditsignedflag);
            plWorkItemProps.setProperty("propsmatch", "Y");
            plWorkItemProps.setProperty("s_assaytypeid", sdiWorkItemDS.getColumnValues("s_assaytypeid", ";"));
            plWorkItemProps.setProperty("sourcesstudyid", sdiWorkItemDS.getColumnValues("sourcesstudyid", ";"));
            try {
                this.getActionProcessor().processAction("AddSDIWorkItem", "1", plWorkItemProps);
            }
            catch (Exception e) {
                throw new SapphireException("DB_ACTION_FAILED", tp.translate("Unable to Add Workitems to the following Samples:") + " " + sdiWorkItemDS.getColumnValues("keyid1", ";"), e);
            }
        }
    }

    private DataSet checkIfSampleHasWI(DataSet sdiWorkItemDS) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT keyid1, workitemid, workitemversionid FROM sdiworkitem WHERE keyid1 in (" + safeSQL.addIn(sdiWorkItemDS.getColumnValues("keyid1", "','")) + ")";
        DataSet sampleWIDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (int i = 0; i < sdiWorkItemDS.size(); ++i) {
            for (int j = 0; j < sampleWIDS.size(); ++j) {
                if (!sampleWIDS.getValue(j, "keyid1").equals(sdiWorkItemDS.getValue(i, "keyid1")) || !sampleWIDS.getValue(j, "workitemid").equals(sdiWorkItemDS.getValue(i, "workitemid")) || !sampleWIDS.getValue(j, "workitemversionid").equals(sdiWorkItemDS.getValue(i, "workitemversionid"))) continue;
                sdiWorkItemDS.deleteRow(i);
            }
        }
        return sdiWorkItemDS;
    }

    private DataSet findSpecTypeTest(DataSet sdiWorkItemDS, String sampleid, DataSet wids, int overallIndex, String specimenType, DataSet sampleds) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("s_sampleid", sampleid);
        filterMap.put("specimentype", specimenType);
        DataSet filteredWIDS = wids.getFilteredDataSet(filterMap);
        HashMap<String, String> filterMap4Samples = new HashMap<String, String>();
        filterMap4Samples.put("sampleinput", sampleid);
        filterMap4Samples.put("specimentype", specimenType);
        DataSet filteredSampleDS = sampleds.getFilteredDataSet(filterMap4Samples);
        int sampleIndex = filteredSampleDS.findRow("s_sampleid", sampleid) + 1;
        for (int i = 0; i < filteredWIDS.size(); ++i) {
            String arrivalOrder = filteredWIDS.getValue(i, "arrivalorder", "");
            if (arrivalOrder.length() == 0 || arrivalOrder.equalsIgnoreCase("All")) {
                sdiWorkItemDS = this.addRowToDS(sdiWorkItemDS, filteredWIDS, sampleid, i);
                continue;
            }
            if (!arrivalOrder.equals(String.valueOf(sampleIndex))) continue;
            sdiWorkItemDS = this.addRowToDS(sdiWorkItemDS, filteredWIDS, sampleid, i);
        }
        sdiWorkItemDS = this.findGlobalTests(sdiWorkItemDS, sampleid, wids, overallIndex);
        return sdiWorkItemDS;
    }

    private DataSet findGlobalTests(DataSet sdiWorkItemDS, String sampleid, DataSet wids, int sampleIndex) {
        HashMap<String, String> filterMap = new HashMap<String, String>();
        filterMap.put("s_sampleid", sampleid);
        if ("ORA".equals(this.connectionInfo.getDbms())) {
            filterMap.put("specimentype", null);
        } else {
            filterMap.put("specimentype", "");
        }
        DataSet filteredWIDS = wids.getFilteredDataSet(filterMap);
        for (int i = 0; i < filteredWIDS.size(); ++i) {
            String arrivalOrder = filteredWIDS.getValue(i, "arrivalorder", "");
            if (arrivalOrder.length() == 0 || arrivalOrder.equalsIgnoreCase("All")) {
                sdiWorkItemDS = this.addRowToDS(sdiWorkItemDS, filteredWIDS, sampleid, i);
                continue;
            }
            if (!arrivalOrder.equals(String.valueOf(sampleIndex))) continue;
            sdiWorkItemDS = this.addRowToDS(sdiWorkItemDS, filteredWIDS, sampleid, i);
        }
        return sdiWorkItemDS;
    }

    private DataSet addRowToDS(DataSet sdiWorkItemDS, DataSet filteredDS, String sampleid, int datsetIndex) {
        if (StringUtil.getLen(filteredDS.getValue(datsetIndex, "workitemid", "")) > 0L) {
            int row = sdiWorkItemDS.addRow();
            sdiWorkItemDS.setValue(row, "keyid1", sampleid);
            sdiWorkItemDS.setValue(row, "s_assaytypeid", filteredDS.getValue(datsetIndex, "s_assaytypeid", ""));
            sdiWorkItemDS.setValue(row, "workitemid", filteredDS.getValue(datsetIndex, "workitemid", ""));
            sdiWorkItemDS.setValue(row, "workitemversionid", filteredDS.getValue(datsetIndex, "workitemversionid", "1"));
            sdiWorkItemDS.setValue(row, "sourcesstudyid", this.getSampleStudy(sampleid));
        }
        return sdiWorkItemDS;
    }

    private String getSampleStudy(String sampleid) {
        if (!this.sampleStudyCache.containsKey(sampleid)) {
            this.sampleStudyCache.put(sampleid, OpalUtil.getColumnValue(this.getQueryProcessor(), "s_sample", "sstudyid", "s_sampleid = ?", new String[]{sampleid}));
        }
        return this.sampleStudyCache.get(sampleid);
    }

    private HashMap<String, DataSet> findWIsNSamplesForGivenSample(String sampleIds) throws SapphireException {
        String[] sampleIdArr = StringUtil.split(sampleIds, ";");
        HashMap<String, DataSet> dataSetHM = new HashMap<String, DataSet>();
        try {
            StringBuilder sql4WIs = new StringBuilder();
            StringBuilder sql4WIFromED = new StringBuilder();
            StringBuilder sql4SampleDetails = new StringBuilder();
            sql4WIFromED = sql4WIFromED.append("SELECT distinct edstat.s_mapid, ").append(" edstat.workitemid AS workitemid   , ").append(" edstat.workitemversionid, ").append(" edstat.s_assaytypeid, ").append(" edstat.arrivalorder , ").append(" edstat.specimentype, s.s_sampleid  ").append(" FROM s_eventdefstatmap edstat, ").append(" s_eventdefsampletype edst, ").append(" s_eventdef ed, ").append(" s_participantevent pe, ").append(" s_samplefamily sf, ").append(" s_cpassaytypesampletype cpatst, s_sample s ").append(" WHERE sf.s_samplefamilyid = s.samplefamilyid AND ").append(" sf.participanteventid = pe.s_participanteventid AND ").append(" pe.eventdefid = ed.s_eventdefid AND ").append(" ed.s_eventdefid = edst.s_eventdefid AND ").append(" edst.s_sampletypeid = s.sampletypeid AND ").append(" edst.s_sampletypeid = edstat.s_sampletypeid AND ").append(" edst.s_eventdefid = edstat.s_eventdefid ").append(" AND edstat.workitemid IS NOT NULL");
            sql4WIs = sql4WIs.append("SELECT distinct edstat.s_mapid, ").append(" cpatst.workitemid AS workitemid   , ").append(" cpatst.workitemversionid, ").append(" edstat.s_assaytypeid, ").append(" edstat.arrivalorder , ").append(" edstat.specimentype, s.s_sampleid  ").append(" FROM s_eventdefstatmap edstat, ").append(" s_eventdefsampletype edst, ").append(" s_eventdef ed, ").append(" s_participantevent pe, ").append(" s_samplefamily sf, ").append(" s_cpassaytypesampletype cpatst, s_sample s ").append(" WHERE sf.s_samplefamilyid = s.samplefamilyid AND ").append(" sf.participanteventid = pe.s_participanteventid AND ").append(" pe.eventdefid = ed.s_eventdefid AND ").append(" ed.s_eventdefid = edst.s_eventdefid AND ").append(" edst.s_sampletypeid = s.sampletypeid AND ").append(" edst.s_sampletypeid = edstat.s_sampletypeid AND ").append(" edst.s_eventdefid = edstat.s_eventdefid ").append(" AND ed.clinicalprotocolid        = cpatst.s_clinicalprotocolid ").append(" AND ed.clinicalprotocolrevision  = cpatst.s_clinicalprotocolrevision ").append(" AND ed.clinicalprotocolversionid = cpatst.s_clinicalprotocolversionid ").append(" AND edstat.s_assaytypeid         = cpatst.s_assaytypeid ").append(" AND edstat.s_sampletypeid        = cpatst.s_sampletypeid ").append(" AND (edstat.workitemid  IS NULL OR edstat.workitemid = '') AND edstat.defaultworkitemflag = 'Y'");
            sql4SampleDetails.append("SELECT distinct s1.s_sampleid sampleinput, s.s_sampleid, s.specimentype, s.receiveddt ");
            sql4SampleDetails.append(" FROM s_sample s, s_sample s1 , s_samplefamily sf, s_samplefamily sf1 ");
            if (sampleIdArr.length > 50) {
                SafeSQL safeSQL = new SafeSQL();
                String rsetid = this.getDAMProcessor().createRSet("Sample", sampleIds, null, null);
                sql4WIs.append(" AND s.s_sampleid ");
                sql4WIs.append(" in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                sql4WIFromED.append(" AND s.s_sampleid ");
                sql4WIFromED.append(" in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                sql4WIs.append(" UNION ALL ").append(sql4WIFromED.toString().replaceAll("s_cpassaytypesampletype cpatst,", ""));
                dataSetHM.put("wids", this.getQueryProcessor().getPreparedSqlDataSet(sql4WIs.toString(), safeSQL.getValues()));
                safeSQL.reset();
                sql4SampleDetails.append(" WHERE s1.s_sampleid ");
                sql4SampleDetails.append(" in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                sql4SampleDetails.append(" AND s.sampletypeid  = s1.sampletypeid ");
                sql4SampleDetails.append(" AND s.specimentype  = s1.specimentype ");
                sql4SampleDetails.append(" AND s.samplefamilyid  = sf.s_samplefamilyid ");
                sql4SampleDetails.append(" AND s1.samplefamilyid  = sf1.s_samplefamilyid ");
                sql4SampleDetails.append(" AND sf1.participanteventid = sf.participanteventid ");
                sql4SampleDetails.append(" AND s.samplestatus = s1.samplestatus AND s.samplestatus = 'Received' ").append(" GROUP BY  s1.s_sampleid, s.s_sampleid, s.specimentype, s.receiveddt ").append(" ORDER BY s.s_sampleid, s.receiveddt");
                dataSetHM.put("sampleDS", this.getQueryProcessor().getPreparedSqlDataSet(sql4SampleDetails.toString(), safeSQL.getValues()));
                if (OpalUtil.isNotEmpty(rsetid)) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql4WIs.append(" AND s.s_sampleid ");
                sql4WIs.append(" in ( ").append(safeSQL.addIn(sampleIds, ";")).append(" )");
                sql4WIFromED.append(" AND s.s_sampleid ");
                sql4WIFromED.append(" in ( ").append(safeSQL.addIn(sampleIds, ";")).append(" )");
                sql4WIs.append(" UNION ALL ").append(sql4WIFromED.toString().replaceAll("s_cpassaytypesampletype cpatst,", ""));
                dataSetHM.put("wids", this.getQueryProcessor().getPreparedSqlDataSet(sql4WIs.toString(), safeSQL.getValues()));
                safeSQL.reset();
                sql4SampleDetails.append(" WHERE s1.s_sampleid in ( ").append(safeSQL.addIn(sampleIds, ";")).append(" )");
                sql4SampleDetails.append(" AND s.sampletypeid  = s1.sampletypeid ");
                sql4SampleDetails.append(" AND s.specimentype  = s1.specimentype ");
                sql4SampleDetails.append(" AND s.samplefamilyid  = sf.s_samplefamilyid ");
                sql4SampleDetails.append(" AND s1.samplefamilyid  = sf1.s_samplefamilyid ");
                sql4SampleDetails.append(" AND sf1.participanteventid = sf.participanteventid ");
                sql4SampleDetails.append(" AND s.samplestatus = s1.samplestatus AND s.samplestatus = 'Received' ").append(" GROUP BY s1.s_sampleid, s.s_sampleid, s.specimentype, s.receiveddt ").append(" ORDER BY s.s_sampleid, s.receiveddt");
                dataSetHM.put("sampleDS", this.getQueryProcessor().getPreparedSqlDataSet(sql4SampleDetails.toString(), safeSQL.getValues()));
            }
        }
        catch (SapphireException e) {
            this.logger.error("Error in retrieving specimen def id using sql");
            throw e;
        }
        return dataSetHM;
    }

    static class UserMessages {
        static final String MISSINGREQARGS = "Missing Required Arguments:  Sample ID";

        UserMessages() {
        }
    }
}

