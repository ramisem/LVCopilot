/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import com.labvantage.sapphire.modules.empower.ProcessLog;
import com.labvantage.sapphire.modules.empower.QCBatchDetails;
import com.labvantage.sapphire.util.format.NumericFormatter;
import com.labvantage.sapphire.webservices.messages.EmpowerMessage;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.ConnectionInfo;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class EmpowerUploadProcessor {
    public static final String DELIMITER = ";";
    private ActionProcessor actionProcessor;
    private QueryProcessor queryProcessor;
    private ConnectionProcessor connectionProcessor;
    private EmpowerPolicyDef empowerPolicyDef;
    private QCBatchDetails qcBatchDetails;
    private DataSet sampleSet;
    private String analyst;
    private PropertyList QCBatch_EditSDI_PL;
    private DataSet QCBatch_AddDataSet_DS;
    private DataSet QCBatch_EditDataSet_DS;
    private DataSet QCBatch_AddDataItem_DS;
    private DataSet QCBatch_EditDataItem_DS;
    private DataSet QCBatch_EnterDataItem_DS;
    private DataSet Sample_AddSDI_DS;
    private DataSet Sample_EditSDI_DS;
    private DataSet Sample_AddDataSet_DS;
    private DataSet Sample_AddDataItem_DS;
    private DataSet Sample_EditDataSet_DS;
    private DataSet Sample_EnterDataItem_DS;
    private DataSet Sample_AddReplicate_DS;
    private DataSet Sample_EditDataItem_DS;
    private DataSet Param_AddSDI_DS;
    private String empowerProject;
    private String empowerDatabase;
    private String sampleSetName;
    private DataSet paramListCurrentVersion;
    private DBAccess database;
    private ProcessLog processLog;
    private ConnectionInfo connectionInfo;

    public EmpowerUploadProcessor(EmpowerPolicyDef def, DBAccess database, ActionProcessor actionProcessor, QueryProcessor queryProcessor, ConnectionProcessor connectionProcessor) throws SapphireException {
        this.connectionInfo = connectionProcessor.getConnectionInfo(connectionProcessor.getConnectionid());
        this.empowerPolicyDef = def;
        this.actionProcessor = actionProcessor;
        this.queryProcessor = queryProcessor;
        this.connectionProcessor = connectionProcessor;
        this.QCBatch_EditSDI_PL = new PropertyList();
        this.QCBatch_AddDataSet_DS = new DataSet(this.connectionInfo);
        this.QCBatch_EditDataSet_DS = new DataSet(this.connectionInfo);
        this.QCBatch_AddDataItem_DS = new DataSet(this.connectionInfo);
        this.QCBatch_EditDataItem_DS = new DataSet(this.connectionInfo);
        this.QCBatch_EnterDataItem_DS = new DataSet(this.connectionInfo);
        this.Sample_AddSDI_DS = new DataSet(this.connectionInfo);
        this.Sample_EditSDI_DS = new DataSet(this.connectionInfo);
        this.Sample_AddDataSet_DS = new DataSet(this.connectionInfo);
        this.Sample_EditDataSet_DS = new DataSet(this.connectionInfo);
        this.Sample_AddDataItem_DS = new DataSet(this.connectionInfo);
        this.Sample_EditDataItem_DS = new DataSet(this.connectionInfo);
        this.Sample_EnterDataItem_DS = new DataSet(this.connectionInfo);
        this.Sample_AddReplicate_DS = new DataSet(this.connectionInfo);
        this.Param_AddSDI_DS = new DataSet(this.connectionInfo);
        this.database = database;
        this.processLog = new ProcessLog();
    }

    public String getLog() {
        return this.processLog.getLog();
    }

    public String getResponse() {
        return this.processLog.getResponse();
    }

    public void processMessage(EmpowerMessage msg) throws SapphireException {
        this.analyst = msg.getProperty("impersonationuser");
        M18NUtil currentUserLocale = new M18NUtil(this.connectionProcessor.getConnectionInfo(this.connectionProcessor.getConnectionid()));
        DataSet sampleSet = msg.getBatchData().toDataSet();
        if (sampleSet == null || sampleSet.getRowCount() == 0) {
            throw new SapphireException("SampleSet should contain one row of data.");
        }
        sampleSet.setM18NUtil(currentUserLocale);
        DataSet sampleSetLine = msg.getSampleData().toDataSet();
        if (sampleSetLine == null || sampleSet.getRowCount() == 0) {
            throw new SapphireException("SampleSetLine should contain atleast one row of data.");
        }
        sampleSetLine.setM18NUtil(currentUserLocale);
        DataSet results = msg.getResultData().toDataSet();
        if (results == null || results.getRowCount() == 0) {
            throw new SapphireException("Results should contain atleast one row of data.");
        }
        results.setM18NUtil(currentUserLocale);
        DataSet peaks = msg.getPeakData().toDataSet();
        if (peaks == null || peaks.getRowCount() == 0) {
            throw new SapphireException("Peaks should contain atleast one row of data.");
        }
        peaks.setM18NUtil(currentUserLocale);
        this.processUploadData(this.analyst, msg.getEmpowerProject(), msg.getEmpowerDatabase(), sampleSet, sampleSetLine, results, peaks);
    }

    public void processUploadData(String analyst, String projectname, String databasename, DataSet sampleSet, DataSet sampleSetLines, DataSet results, DataSet peaks) throws SapphireException {
        this.analyst = analyst;
        String qcBatchColumnName = this.empowerPolicyDef.getEmpowerCoreMapping("empowerqcbatchid");
        String qcbatchid = sampleSet.getString(0, qcBatchColumnName);
        if (qcbatchid.length() == 0) {
            throw new SapphireException("qcbatchid not specified in the SampleSet.");
        }
        this.processLog.println("EmpowerUpload: Identified the qcbatch specified in the SampleSet.\n");
        this.qcBatchDetails = new QCBatchDetails(this.connectionInfo, this.queryProcessor, qcbatchid);
        QCBatchDetails initialBatchDetails = new QCBatchDetails(this.connectionInfo, this.queryProcessor, qcbatchid);
        this.paramListCurrentVersion = this.populateInitialList(this.qcBatchDetails.getSampleDataSets());
        this.processSampleSet(qcbatchid, projectname, databasename, sampleSet);
        this.processSampleSetLines(sampleSetLines);
        this.processResults(results);
        peaks = this.sanitizePeaks(peaks);
        this.processPeaks(peaks);
        try {
            this.runActions(false);
            String updatedBatchStatus = this.postSaveStatusUpdates(qcbatchid);
            if (updatedBatchStatus.equals("Completed") || updatedBatchStatus.equalsIgnoreCase("DataEntered")) {
                PropertyList editProps = new PropertyList();
                editProps.setProperty("sdcid", "QCBatch");
                editProps.setProperty("keyid1", qcbatchid);
                editProps.setProperty("blockflag", "N");
                String externalReference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName);
                editProps.setProperty("externalreference", externalReference);
                this.actionProcessor.processAction("EditSDI", "1", editProps);
            }
        }
        catch (SapphireException e) {
            throw new SapphireException("Processing upload request failed:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        this.renderDiffQCBatchDetails(qcbatchid, initialBatchDetails);
    }

    private DataSet populateInitialList(DataSet sdidata) throws SapphireException {
        DataSet currentVersionInfo = new DataSet(this.connectionInfo);
        currentVersionInfo.addColumn("paramlistid", 0);
        currentVersionInfo.addColumn("paramlistversionid", 0);
        currentVersionInfo.addColumn("variantid", 0);
        if (sdidata == null || sdidata.getRowCount() == 0) {
            return currentVersionInfo;
        }
        String paramlistidList = sdidata.getColumnValues("paramlistid", DELIMITER);
        String paramlistVariantList = sdidata.getColumnValues("variantid", DELIMITER);
        String[] paramListId = StringUtil.split(paramlistidList, DELIMITER);
        String[] variantId = StringUtil.split(paramlistVariantList, DELIMITER);
        String sql = "SELECT paramlistversionid FROM paramlist WHERE paramlistid=? and variantid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (paramlistversionid as integer) desc";
        for (int i = 0; i < paramListId.length; ++i) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("paramlistid", paramListId[i]);
            filter.put("variantid", variantId[i]);
            if (currentVersionInfo.findRow(filter) != -1) continue;
            this.database.createPreparedResultSet("CurrentVersion", sql, new Object[]{paramListId[i], variantId[i]});
            if (!this.database.getNext("CurrentVersion")) continue;
            String paramlistversionid = this.database.getString("CurrentVersion", "paramlistversionid");
            int currRow = currentVersionInfo.addRow();
            currentVersionInfo.setString(currRow, "paramlistid", paramListId[i]);
            currentVersionInfo.setString(currRow, "variantid", variantId[i]);
            currentVersionInfo.setString(currRow, "paramlistversionid", paramlistversionid);
        }
        return currentVersionInfo;
    }

    private DataSet updateParamListCurrentVersion(DataSet paramListCurrentVersion, String paramlistid, String variantid) throws SapphireException {
        String sql = "SELECT paramlistversionid FROM paramlist WHERE paramlistid=? and variantid=? and ( versionstatus='P' or versionstatus='C' ) order by versionstatus, cast (paramlistversionid as integer) desc";
        this.database.createPreparedResultSet("NewCurrentVersion", sql, new Object[]{paramlistid, variantid});
        if (this.database.getNext("NewCurrentVersion")) {
            String paramlistversionid = this.database.getString("CurrentVersion", "paramlistversionid");
            int currRow = paramListCurrentVersion.addRow();
            paramListCurrentVersion.setString(currRow, "paramlistid", paramlistid);
            paramListCurrentVersion.setString(currRow, "variantid", variantid);
            paramListCurrentVersion.setString(currRow, "paramlistversionid", paramlistversionid);
        }
        return paramListCurrentVersion;
    }

    public void processSampleSet(String qcBatchId, String empowerProject, String empowerDatabase, DataSet sampleSet) throws SapphireException {
        this.sampleSet = sampleSet;
        this.empowerProject = empowerProject;
        this.empowerDatabase = empowerDatabase;
        this.sampleSetName = sampleSet.getString(0, this.empowerPolicyDef.getTranslate("SampleSetName"), "");
        DataSet sampleSetRules = this.empowerPolicyDef.getUploadSampleSetRules();
        String externalreference = QCBatchDetails.getExternalReference(empowerProject, empowerDatabase, this.sampleSetName);
        this.QCBatch_EditSDI_PL.setProperty("externalreference", externalreference);
        for (int i = 0; i < sampleSetRules.getRowCount(); ++i) {
            String ruleType = sampleSetRules.getString(i, "ruletype");
            String empowerColumn = sampleSetRules.getString(i, "empowercolumn");
            empowerColumn = this.empowerPolicyDef.getTranslate(empowerColumn);
            String empowerColumnValue = sampleSet.getValue(0, empowerColumn);
            if (ruleType.equals("QCBatch")) {
                this.mapQCBatchSDIColumn(qcBatchId, sampleSetRules, i, empowerColumnValue);
                continue;
            }
            if (ruleType.equals("DataSet")) {
                this.mapQCBatchDataSetColumn(qcBatchId, sampleSetRules, i, empowerColumnValue);
                continue;
            }
            if (!ruleType.equals("DataItem")) continue;
            this.mapQCBatchDataItemResult(qcBatchId, sampleSetRules, i, empowerColumnValue);
        }
        this.runQCBatchActions();
        this.processLog.println("Processed SampleSet DataSet.");
    }

    private void runQCBatchActions() throws SapphireException {
        String qcbatchid = this.sampleSet.getString(0, this.empowerPolicyDef.getEmpowerCoreMapping("empowerqcbatchid"), "");
        this.process_QCBatch_EditSDI_PL(qcbatchid);
        this.process_QCBatch_AddDataSet_DS(qcbatchid);
        this.process_Param_AddSDI_DS(false);
        this.process_QCBatch_AddDataItem_DS(qcbatchid);
        this.process_QCBatch_EditDataSet_DS(qcbatchid);
        this.process_QCBatch_EnterDataItem_DS(qcbatchid);
        this.process_QCBatch_EditDataItem_DS(qcbatchid);
    }

    public StringBuffer processSampleSetLines(DataSet sampleSetLineItems) throws SapphireException {
        StringBuffer log = new StringBuffer();
        HashMap<String, String> standard = new HashMap<String, String>();
        standard.put(this.empowerPolicyDef.getTranslate("SampleType").toLowerCase(), this.empowerPolicyDef.getTranslate("Standard"));
        DataSet standardSampleSetLineItems = sampleSetLineItems.getFilteredDataSet(standard);
        HashMap<String, String> control = new HashMap<String, String>();
        control.put(this.empowerPolicyDef.getTranslate("SampleType").toLowerCase(), this.empowerPolicyDef.getTranslate("Control"));
        DataSet controlSampleSetLineItems = sampleSetLineItems.getFilteredDataSet(control);
        HashMap<String, String> unknown = new HashMap<String, String>();
        unknown.put(this.empowerPolicyDef.getTranslate("SampleType").toLowerCase(), this.empowerPolicyDef.getTranslate("Unknown"));
        DataSet unknownSampleSetLineItems = sampleSetLineItems.getFilteredDataSet(unknown);
        HashMap<String, String> missing = new HashMap<String, String>();
        missing.put(this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"), "");
        DataSet standardControlSampleSetLineRules = this.empowerPolicyDef.getUploadStandardSampleSetLineRules();
        this.processSampleSetLineRules(standardSampleSetLineItems, standardControlSampleSetLineRules);
        this.processSampleSetLineRules(controlSampleSetLineItems, standardControlSampleSetLineRules);
        DataSet unknownSampleSetLineRules = this.empowerPolicyDef.getUploadUnknownSampleSetLineRules();
        this.processSampleSetLineRules(unknownSampleSetLineItems, unknownSampleSetLineRules);
        return log;
    }

    private void pruneAddedDataItems(DataSet rules, DataSet results, String handlingReleasedResults) throws SapphireException {
        for (int i = 0; i < results.getRowCount(); ++i) {
            String channel;
            String datasetkey;
            String sdiDataIdList;
            String injection = results.getValue(i, this.empowerPolicyDef.getTranslate("Injection"));
            String keyid1 = results.getString(i, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
            DataSet ret = this.qcBatchDetails.getDynamicallyAddedDataItems("Sample", keyid1, sdiDataIdList = this.getSDIDataIdList(datasetkey = results.getString(i, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"))), injection, channel = results.getValue(i, this.empowerPolicyDef.getTranslate("ChannelName"), ""), handlingReleasedResults);
            if (ret.getRowCount() <= 0) continue;
            Trace.log("Items to be pruned:" + ret.getRowCount());
            for (int j = 0; j < ret.getRowCount(); ++j) {
                this.processLog.addOp(rules.getValue(j, "rule"), "DeleteDataItem", "Sample", keyid1 + "[" + ret.getValue(j, "paramlistid") + "," + ret.getValue(j, "paramlistversionid") + "," + ret.getValue(j, "variantid") + "," + ret.getValue(j, "dataset") + "|" + ret.getValue(j, "paramid") + "," + ret.getValue(j, "paramtype") + "," + ret.getValue(j, "replicate") + "]", "", "", "", "");
            }
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("sdcid", "Sample");
            deleteProps.setProperty("keyid1", ret.getColumnValues("keyid1", DELIMITER));
            deleteProps.setProperty("paramlistid", ret.getColumnValues("paramlistid", DELIMITER));
            deleteProps.setProperty("paramlistversionid", ret.getColumnValues("paramlistversionid", DELIMITER));
            deleteProps.setProperty("variantid", ret.getColumnValues("variantid", DELIMITER));
            deleteProps.setProperty("dataset", ret.getColumnValues("dataset", DELIMITER));
            deleteProps.setProperty("paramid", ret.getColumnValues("paramid", DELIMITER));
            deleteProps.setProperty("paramtype", ret.getColumnValues("paramtype", DELIMITER));
            deleteProps.setProperty("replicateid", ret.getColumnValues("replicateid", DELIMITER));
            this.actionProcessor.processAction("DeleteDataItem", "1", deleteProps);
        }
    }

    public StringBuffer processResults(DataSet results) throws SapphireException {
        StringBuffer log = new StringBuffer();
        DataSet resultRules = this.empowerPolicyDef.getUploadResultRules();
        String handlingReleasedResults = this.empowerPolicyDef.getHandlingReleasedResults();
        this.pruneAddedDataItems(resultRules, results, handlingReleasedResults);
        for (int i = 0; i < resultRules.size(); ++i) {
            String ruleType = resultRules.getString(i, "ruletype");
            for (int currresult = 0; currresult < results.getRowCount(); ++currresult) {
                if ("DataSet".equals(ruleType)) {
                    this.mapResultDataSetColumn(results, currresult, resultRules, i);
                    continue;
                }
                if (!"DataItem".equals(ruleType)) continue;
                this.mapResultDataItemResult(results, currresult, resultRules, i);
            }
        }
        return log;
    }

    private DataSet processUnknownPeaks(DataSet peaks) {
        int sequence = 0;
        if (peaks != null && peaks.getRowCount() > 0) {
            if (this.empowerPolicyDef.saveUnknownPeaks()) {
                Trace.log("Diagnostic::: Processing Unknown Peaks");
                String prevSample = "";
                String prevInj = "";
                for (int i = 0; i < peaks.getRowCount(); ++i) {
                    if (!peaks.getString(i, this.empowerPolicyDef.getTranslate("PeakType").toLowerCase()).equals(this.empowerPolicyDef.getTranslate("Unknown"))) continue;
                    String threshold = this.empowerPolicyDef.getThresholdLevel();
                    String resultColumn = this.empowerPolicyDef.getUnknownPeakResultColumn();
                    String currvalue = peaks.getString(i, this.empowerPolicyDef.getTranslate(resultColumn), "");
                    if (currvalue.length() > 0) {
                        double thresholdval = new BigDecimal(threshold).doubleValue();
                        double currval = new BigDecimal(currvalue).doubleValue();
                        if (currval < thresholdval) {
                            peaks.setString(i, "Name", "IGNOREPEAK");
                            continue;
                        }
                        String currSample = peaks.getString(i, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
                        String curInj = peaks.getValue(i, this.empowerPolicyDef.getTranslate("Injection"));
                        if (prevSample.equals(currSample) && prevInj.equals(curInj)) {
                            ++sequence;
                        } else {
                            sequence = 1;
                            prevSample = currSample;
                            prevInj = curInj;
                        }
                        String peakName = this.getPeakName(peaks, i, sequence);
                        peaks.setString(i, this.empowerPolicyDef.getTranslate("Name"), peakName);
                        continue;
                    }
                    peaks.setString(i, this.empowerPolicyDef.getTranslate("Name"), "IGNOREPEAK");
                }
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put(this.empowerPolicyDef.getTranslate("Name").toLowerCase(), "IGNOREPEAK");
                DataSet remove = peaks.getFilteredDataSet(filter);
                if (remove.getRowCount() > 0) {
                    peaks.removeAll(remove);
                }
            } else {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put(this.empowerPolicyDef.getTranslate("PeakType").toLowerCase(), this.empowerPolicyDef.getTranslate("Unknown"));
                DataSet remove = peaks.getFilteredDataSet(filter);
                peaks.removeAll(remove);
            }
        } else {
            Trace.logDebug("DIAGNOSTIC::: peaks dataset is empty");
        }
        return peaks;
    }

    private String getPeakName(DataSet peaks, int i, int sequence) {
        String peaknameprefix = this.empowerPolicyDef.getUnknownPeakParamId();
        String unknownpeakrule = this.empowerPolicyDef.getUnknownPeakHandlingRule();
        if (unknownpeakrule.equals("AppendRT")) {
            String retentionTime = peaks.getString(i, this.empowerPolicyDef.getTranslate("RetentionTime"), "");
            if (retentionTime.indexOf(".") > -1 && retentionTime.indexOf(".") + 4 < retentionTime.length()) {
                retentionTime = retentionTime.substring(0, retentionTime.indexOf(".") + 4);
            }
            return peaknameprefix + "_" + retentionTime;
        }
        if (unknownpeakrule.equals("AppendRRT")) {
            String relativeRetentionTime = peaks.getString(i, this.empowerPolicyDef.getTranslate("RRT~"), "");
            if (relativeRetentionTime.length() > 0 && relativeRetentionTime.indexOf(".") > -1) {
                if (relativeRetentionTime.indexOf(".") + 4 < relativeRetentionTime.length()) {
                    relativeRetentionTime = relativeRetentionTime.substring(0, relativeRetentionTime.indexOf(".") + 4);
                }
            } else {
                return "IGNOREPEAK";
            }
            return peaknameprefix + "_" + relativeRetentionTime;
        }
        String num = NumericFormatter.formatNumber(sequence, "000");
        return peaknameprefix + "_" + num;
    }

    public StringBuffer processPeaks(DataSet peaks) throws SapphireException {
        StringBuffer log = new StringBuffer();
        Trace.logDebug("Diagnostic::: Processing Peaks: " + peaks.toXML());
        peaks = this.processUnknownPeaks(peaks);
        if (peaks != null && peaks.getRowCount() > 0) {
            int i;
            DataSet peakRules = this.empowerPolicyDef.getUploadPeakRules();
            DataSet unknownPeakUploadResultRule = this.empowerPolicyDef.getUploadUnknownPeakResultRule();
            unknownPeakUploadResultRule.setString(0, "empowercolumn", this.empowerPolicyDef.getUnknownPeakResultColumn());
            DataSet namedPeakUploadResultRule = this.empowerPolicyDef.getUploadNamedPeakResultRule();
            namedPeakUploadResultRule.setString(0, "empowercolumn", this.empowerPolicyDef.getNamedPeakResultColumn());
            for (i = 0; i < peaks.getRowCount(); ++i) {
                if (peaks.getString(i, this.empowerPolicyDef.getTranslate("PeakType").toLowerCase()).equals(this.empowerPolicyDef.getTranslate("Unknown"))) {
                    if (peaks.getString(i, this.empowerPolicyDef.getUnknownPeakResultColumn(), "").length() == 0) {
                        peaks.setString(i, this.empowerPolicyDef.getUnknownPeakResultColumn(), "BLANK");
                    }
                    Trace.logDebug("Diagnostic::: Unknown peak with name:" + peaks.getString(i, this.empowerPolicyDef.getTranslate("Name")));
                    this.mapPeakDataItem(peaks, i, unknownPeakUploadResultRule, 0);
                    continue;
                }
                if (peaks.getString(i, this.empowerPolicyDef.getNamedPeakResultColumn(), "").length() == 0) {
                    peaks.setString(i, this.empowerPolicyDef.getNamedPeakResultColumn(), "BLANK");
                }
                Trace.logDebug("Diagnostic::: Named peak with name:" + peaks.getString(i, this.empowerPolicyDef.getTranslate("Name")));
                this.mapPeakDataItem(peaks, i, namedPeakUploadResultRule, 0);
            }
            for (i = 0; i < peakRules.size(); ++i) {
                String ruleType = peakRules.getString(i, "ruletype");
                for (int currpeak = 0; currpeak < peaks.getRowCount(); ++currpeak) {
                    if (!"DataItem".equals(ruleType)) continue;
                    this.mapPeakDataItem(peaks, currpeak, peakRules, i);
                }
            }
        }
        return log;
    }

    private int findRow_QCBatch_EditDataSet_DS(String paramlistid, String variantid, String dataset) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("paramlistid", paramlistid);
        filter.put("variantid", variantid);
        filter.put("dataset", dataset);
        DataSet ret = this.QCBatch_EditDataSet_DS.getFilteredDataSet(filter);
        if (ret != null && ret.getRowCount() > 0) {
            return ret.getBigDecimal(0, "index").intValue();
        }
        return -1;
    }

    private void addRow_QCBatch_EditDataSet_DS(String rule, String qcbatchid, String paramlistid, String paramlistversionid, String variantid, String dataset, String columnName, String empowerCol, String columnValue) {
        this.processLog.addOp(rule, "EditDataSet", "QCBatch", qcbatchid + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "]", columnName, "SampleSet", empowerCol, columnValue);
        int curr = this.findRow_QCBatch_EditDataSet_DS(paramlistid, variantid, dataset);
        if (curr == -1) {
            curr = this.QCBatch_EditDataSet_DS.addRow();
            this.QCBatch_EditDataSet_DS.setNumber(curr, "index", curr);
        }
        this.QCBatch_EditDataSet_DS.setString(curr, "paramlistid", paramlistid);
        this.QCBatch_EditDataSet_DS.setString(curr, "paramlistversionid", paramlistversionid);
        this.QCBatch_EditDataSet_DS.setString(curr, "variantid", variantid);
        this.QCBatch_EditDataSet_DS.setString(curr, "dataset", dataset);
        this.QCBatch_EditDataSet_DS.setString(curr, columnName, columnValue);
    }

    private void addRow_QCBatch_AddDataSet_DS(String rule, String paramlistid, String paramlistversionid, String variantid, String currds) {
        this.processLog.addOp(rule, "AddDataSet", "QCBatch", "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + currds + "]", "", "", "", "");
        int currRow = this.QCBatch_AddDataSet_DS.addRow();
        this.QCBatch_AddDataSet_DS.setString(currRow, "paramlistid", paramlistid);
        this.QCBatch_AddDataSet_DS.setString(currRow, "paramlistversionid", paramlistversionid);
        this.QCBatch_AddDataSet_DS.setString(currRow, "variantid", variantid);
        this.QCBatch_AddDataSet_DS.setString(currRow, "dataset", currds);
        this.qcBatchDetails.addQCBatchDataSet(paramlistid, paramlistversionid, variantid, new Integer(currds));
        DataSet paramsInfo = this.queryProcessor.getPreparedSqlDataSet("SELECT paramid, paramtype FROM paramlistitem WHERE paramlistid =? and paramlistversionid=? and variantid=?", new Object[]{paramlistid, paramlistversionid, variantid});
        for (int i = 0; i < paramsInfo.getRowCount(); ++i) {
            this.qcBatchDetails.addQCBatchDataItem(paramlistid, paramlistversionid, variantid, new Integer(currds), paramsInfo.getString(i, "paramid"), paramsInfo.getString(i, "paramtype"), 1);
        }
    }

    private void addRow_QCBatch_EnterDataItem_DS(String rule, String qcbatchid, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicate, String empowerCol, String enteredtext) {
        this.processLog.addOp(rule, "EnterDataItem", "QCBatch", qcbatchid + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "|" + paramid + "," + paramtype + "," + replicate + "]", "enteredtext", "SampleSet", empowerCol, enteredtext);
        int row = this.QCBatch_EnterDataItem_DS.addRow();
        this.QCBatch_EnterDataItem_DS.setString(row, "paramlistid", paramlistid);
        if (paramlistversionid == null || paramlistversionid.length() == 0) {
            paramlistversionid = "C";
        }
        this.QCBatch_EnterDataItem_DS.setString(row, "paramlistversionid", paramlistversionid);
        this.QCBatch_EnterDataItem_DS.setString(row, "variantid", variantid);
        this.QCBatch_EnterDataItem_DS.setString(row, "paramid", paramid);
        this.QCBatch_EnterDataItem_DS.setString(row, "paramtype", paramtype);
        this.QCBatch_EnterDataItem_DS.setString(row, "dataset", dataset);
        this.QCBatch_EnterDataItem_DS.setString(row, "replicateid", replicate);
        this.QCBatch_EnterDataItem_DS.setString(row, "enteredtext", enteredtext);
    }

    private void addRow_QCBatch_AddDataItem_DS(String rule, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicate) {
        this.processLog.addOp(rule, "AddDataItem", "QCBatch", "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "|" + paramid + "," + paramtype + "," + replicate + "]", "", "", "", "");
        int currRow = this.QCBatch_AddDataItem_DS.addRow();
        this.QCBatch_AddDataItem_DS.setString(currRow, "paramlistid", paramlistid);
        this.QCBatch_AddDataItem_DS.setString(currRow, "paramlistversionid", paramlistversionid);
        this.QCBatch_AddDataItem_DS.setString(currRow, "variantid", variantid);
        this.QCBatch_AddDataItem_DS.setString(currRow, "dataset", dataset);
        this.QCBatch_AddDataItem_DS.setString(currRow, "paramid", paramid);
        this.QCBatch_AddDataItem_DS.setString(currRow, "paramtype", paramtype);
        this.QCBatch_AddDataItem_DS.setString(currRow, "replicateid", replicate);
        this.QCBatch_AddDataItem_DS.setString(currRow, "displayformat", this.empowerPolicyDef.getDisplayFormat());
        this.qcBatchDetails.addQCBatchDataItem(paramlistid, paramlistversionid, variantid, new Integer(dataset), paramid, paramtype, new Integer(replicate));
    }

    private void addRow_QCBatch_EditDataItem_DS(String rule, String qcbatchid, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicate, String colname, String empowerCol, String colval) {
        this.processLog.addOp(rule, "EditDataItem", "QCBatch", qcbatchid + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "|" + paramid + "," + paramtype + "," + replicate + "]", colname, "SampleSet", empowerCol, colval);
        int currRow = this.findRow_QCBatch_EditDataItem_DS(paramlistid, variantid, dataset, paramid, paramtype, replicate);
        if (currRow == -1) {
            currRow = this.QCBatch_EditDataItem_DS.addRow();
            this.QCBatch_EditDataItem_DS.setNumber(currRow, "index", currRow);
        }
        this.QCBatch_EditDataItem_DS.setString(currRow, "paramlistid", paramlistid);
        this.QCBatch_EditDataItem_DS.setString(currRow, "paramlistversionid", paramlistversionid);
        this.QCBatch_EditDataItem_DS.setString(currRow, "variantid", variantid);
        this.QCBatch_EditDataItem_DS.setString(currRow, "dataset", dataset);
        this.QCBatch_EditDataItem_DS.setString(currRow, "paramid", paramid);
        this.QCBatch_EditDataItem_DS.setString(currRow, "paramtype", paramtype);
        this.QCBatch_EditDataItem_DS.setString(currRow, "replicateid", replicate);
        this.QCBatch_EditDataItem_DS.setString(currRow, colname, colval);
    }

    private boolean preDataEntryCheck(String keyid1, String paramlistid, String variantid, String dataset, String paramid, String paramtype, String replicate) throws SapphireException {
        boolean released = this.qcBatchDetails.checkSampleDataItemReleased(keyid1, paramlistid, variantid, dataset, paramid, paramtype, replicate);
        if (released) {
            Trace.logDebug("Diagnostic::: dataitem already released for paramlistid:" + paramlistid + " variantid:" + variantid + " dataset:" + dataset + " paramid:" + paramid + "paramtype:" + paramtype);
            String handlingReleasedResults = this.empowerPolicyDef.getHandlingReleasedResults();
            if (handlingReleasedResults.equals("Error")) {
                throw new SapphireException("An attempt to upload released result is being made for:" + keyid1);
            }
            if (handlingReleasedResults.equals("Ignore")) {
                Trace.logDebug("Diagnostic::: handling released results says ignore");
                return false;
            }
            Trace.logDebug("Diagnostic::: handling released results says override");
            return true;
        }
        return true;
    }

    private void addRow_Sample_EnterDataItem_DS(String rule, String source, String keyid1, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicate, String empowerColumn, String enteredtext) throws SapphireException {
        if (paramlistid.equals("#")) {
            String loginfo = source + "(" + keyid1 + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "|" + paramid + "," + paramtype + "," + replicate + "] )";
            throw new SapphireException("Invalid paramlistid specified for EnterDataItem: source = " + loginfo);
        }
        int row = this.Sample_EnterDataItem_DS.addRow();
        this.Sample_EnterDataItem_DS.setString(row, "keyid1", keyid1);
        this.Sample_EnterDataItem_DS.setString(row, "paramlistid", paramlistid);
        this.Sample_EnterDataItem_DS.setString(row, "paramlistversionid", paramlistversionid);
        this.Sample_EnterDataItem_DS.setString(row, "variantid", variantid);
        this.Sample_EnterDataItem_DS.setString(row, "paramid", paramid);
        this.Sample_EnterDataItem_DS.setString(row, "paramtype", paramtype);
        this.Sample_EnterDataItem_DS.setString(row, "dataset", dataset);
        this.Sample_EnterDataItem_DS.setString(row, "replicateid", replicate);
        this.Sample_EnterDataItem_DS.setString(row, "enteredtext", enteredtext);
        this.processLog.addOp(rule, "EnterDataItem", "Sample", keyid1 + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "|" + paramid + "," + paramtype + "," + replicate + "]", "enteredtext", source, empowerColumn, enteredtext);
    }

    private int findRow_QCBatch_EditDataItem_DS(String paramlistid, String variantid, String dataset, String paramid, String paramtype, String replicate) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("paramlistid", paramlistid);
        filter.put("variantid", variantid);
        filter.put("dataset", dataset);
        filter.put("paramid", paramid);
        filter.put("paramtype", paramtype);
        filter.put("replicateid", replicate);
        DataSet ret = this.QCBatch_EditDataItem_DS.getFilteredDataSet(filter);
        if (ret != null && ret.getRowCount() > 0) {
            return ret.getBigDecimal(0, "index").intValue();
        }
        return -1;
    }

    private int findRow_Sample_EditDataSet_DS(String keyid1, String paramlistid, String variantid, String dataset) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", keyid1);
        filter.put("paramlistid", paramlistid);
        filter.put("variantid", variantid);
        filter.put("dataset", dataset);
        DataSet ret = this.Sample_EditDataSet_DS.getFilteredDataSet(filter);
        if (ret != null && ret.getRowCount() > 0) {
            return ret.getBigDecimal(0, "index").intValue();
        }
        return -1;
    }

    private int findRow_Sample_AddDataSet_DS(String keyid1, String paramlistid, String paramlistversionid, String variantid) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", keyid1);
        filter.put("paramlistid", paramlistid);
        filter.put("paramlistid", paramlistversionid);
        filter.put("variantid", variantid);
        DataSet ret = this.Sample_AddDataSet_DS.getFilteredDataSet(filter);
        if (ret != null && ret.getRowCount() > 0) {
            return ret.getBigDecimal(0, "index").intValue();
        }
        return -1;
    }

    private int findRow_Param_AddSDI_DS(String keyid1) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", keyid1);
        DataSet ret = this.Param_AddSDI_DS.getFilteredDataSet(filter);
        if (ret != null && ret.getRowCount() > 0) {
            return ret.getBigDecimal(0, "index").intValue();
        }
        return -1;
    }

    private int findRow_Sample_EditDataItem_DS(String keyid1, String paramlistid, String variantid, String dataset, String paramid, String paramtype, String replicate) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", keyid1);
        filter.put("paramlistid", paramlistid);
        filter.put("variantid", variantid);
        filter.put("dataset", dataset);
        filter.put("paramid", paramid);
        filter.put("paramtype", paramtype);
        filter.put("replicateid", replicate);
        DataSet ret = this.Sample_EditDataItem_DS.getFilteredDataSet(filter);
        if (ret != null && ret.getRowCount() > 0) {
            return ret.getBigDecimal(0, "index").intValue();
        }
        return -1;
    }

    private int findRow_Sample_AddReplicate_DS(String keyid1, String paramlistid, String variantid, String dataset, String paramid, String paramtype) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", keyid1);
        filter.put("paramlistid", paramlistid);
        filter.put("variantid", variantid);
        filter.put("dataset", dataset);
        filter.put("paramid", paramid);
        filter.put("paramtype", paramtype);
        DataSet ret = this.Sample_AddReplicate_DS.getFilteredDataSet(filter);
        if (ret != null && ret.getRowCount() > 0) {
            return ret.getBigDecimal(0, "index").intValue();
        }
        return -1;
    }

    private void addRow_Sample_EditDataItem_DS(String rule, String source, String keyid1, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicate, String column, String empowerColumn, String value) {
        int row = this.findRow_Sample_EditDataItem_DS(keyid1, paramlistid, variantid, dataset, paramid, paramtype, replicate);
        if (row == -1) {
            row = this.Sample_EditDataItem_DS.addRow();
            this.Sample_EditDataItem_DS.setNumber(row, "index", row);
        }
        this.Sample_EditDataItem_DS.setString(row, "keyid1", keyid1);
        this.Sample_EditDataItem_DS.setString(row, "paramlistid", paramlistid);
        this.Sample_EditDataItem_DS.setString(row, "paramlistversionid", paramlistversionid);
        this.Sample_EditDataItem_DS.setString(row, "variantid", variantid);
        this.Sample_EditDataItem_DS.setString(row, "paramid", paramid);
        this.Sample_EditDataItem_DS.setString(row, "paramtype", paramtype);
        this.Sample_EditDataItem_DS.setString(row, "dataset", dataset);
        this.Sample_EditDataItem_DS.setString(row, "replicateid", replicate);
        this.Sample_EditDataItem_DS.setString(row, column, value);
        this.processLog.addOp(rule, "EditDataItem", "Sample", keyid1 + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "|" + paramid + "," + paramtype + "," + replicate + "]", column, source, empowerColumn, value);
    }

    private void addRow_Sample_AddReplicate_DS(String rule, String source, String keyid1, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicate) {
        int row = this.findRow_Sample_AddReplicate_DS(keyid1, paramlistid, variantid, dataset, paramid, paramtype);
        int currnumofreplicates = 0;
        int replicatenum = new Integer(replicate);
        this.processLog.addOp(rule, "AddReplicate", "Sample", keyid1 + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "|" + paramid + "," + paramtype + "," + replicate + "]", "", source, "", "");
        if (row == -1) {
            row = this.Sample_AddReplicate_DS.addRow();
            this.Sample_AddReplicate_DS.setNumber(row, "index", row);
            this.Sample_AddReplicate_DS.setString(row, "keyid1", keyid1);
            this.Sample_AddReplicate_DS.setString(row, "paramlistid", paramlistid);
            this.Sample_AddReplicate_DS.setString(row, "paramlistversionid", paramlistversionid);
            this.Sample_AddReplicate_DS.setString(row, "variantid", variantid);
            this.Sample_AddReplicate_DS.setString(row, "paramid", paramid);
            this.Sample_AddReplicate_DS.setString(row, "paramtype", paramtype);
            this.Sample_AddReplicate_DS.setString(row, "dataset", dataset);
            this.Sample_AddReplicate_DS.setString(row, "numreplicate", "" + (replicatenum - 1));
        } else {
            currnumofreplicates = new Integer(this.Sample_AddReplicate_DS.getValue(row, "numreplicate"));
            if (currnumofreplicates < replicatenum - 1) {
                this.Sample_AddReplicate_DS.setString(row, "numreplicate", "" + (replicatenum - 1));
            }
        }
    }

    private void addRow_Sample_AddDataSet_DS(String rule, String source, String keyid1, String paramlistid, String paramlistversionid, String variantid, String dataset, String externalreference) {
        if (this.findRow_Sample_AddDataSet_DS(keyid1, paramlistid, paramlistversionid, variantid) != -1) {
            return;
        }
        int currRow = this.Sample_AddDataSet_DS.addRow();
        this.Sample_AddDataSet_DS.setString(currRow, "keyid1", keyid1);
        this.Sample_AddDataSet_DS.setString(currRow, "paramlistid", paramlistid);
        this.Sample_AddDataSet_DS.setString(currRow, "paramlistversionid", paramlistversionid);
        this.Sample_AddDataSet_DS.setString(currRow, "variantid", variantid);
        this.Sample_AddDataSet_DS.setString(currRow, "externalreference", externalreference);
        this.processLog.addOp(rule, "AddDataSet", "Sample", keyid1 + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "]", "", source, "", "");
        this.qcBatchDetails.addSampleDataSet(keyid1, paramlistid, paramlistversionid, variantid, dataset);
        DataSet paramsInfo = this.queryProcessor.getPreparedSqlDataSet("SELECT paramid, paramtype FROM paramlistitem WHERE paramlistid =? and paramlistversionid=? and variantid=?", new Object[]{paramlistid, paramlistversionid, variantid});
        for (int i = 0; i < paramsInfo.getRowCount(); ++i) {
            this.qcBatchDetails.addSampleDataItem(keyid1, paramlistid, paramlistversionid, variantid, new Integer(dataset), paramsInfo.getString(i, "paramid"), paramsInfo.getString(i, "paramtype"), 1);
        }
    }

    private void addRow_Param_AddSDI_DS(String keyid1) {
        if (this.findRow_Param_AddSDI_DS(keyid1) != -1) {
            return;
        }
        int currRow = this.Param_AddSDI_DS.addRow();
        this.Param_AddSDI_DS.setString(currRow, "keyid1", keyid1);
        this.Param_AddSDI_DS.setNumber(currRow, "index", currRow);
    }

    private void addRow_Sample_EditDataSet_DS(String rule, String source, String keyid1, String paramlistid, String paramlistversionid, String variantid, String dataset, String empowerColumn, String empowerColumnValue, String externalreference) {
        this.processLog.addOp(rule, "EditDataSet", "Sample", keyid1 + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "]", "", source, empowerColumn, empowerColumnValue);
        int c = this.findRow_Sample_EditDataSet_DS(keyid1, paramlistid, variantid, dataset);
        if (c == -1) {
            c = this.Sample_EditDataSet_DS.addRow();
            this.Sample_EditDataSet_DS.setNumber(c, "index", c);
        }
        this.Sample_EditDataSet_DS.setString(c, "keyid1", keyid1);
        this.Sample_EditDataSet_DS.setString(c, "paramlistid", paramlistid);
        this.Sample_EditDataSet_DS.setString(c, "paramlistversionid", paramlistversionid);
        this.Sample_EditDataSet_DS.setString(c, "variantid", variantid);
        this.Sample_EditDataSet_DS.setString(c, "dataset", dataset);
        this.Sample_EditDataSet_DS.setString(c, empowerColumn, empowerColumnValue);
        this.Sample_EditDataSet_DS.setString(c, "externalreference", externalreference);
    }

    private boolean find_Sample_AddDataItem_DS(String keyid1, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicate) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", keyid1);
        filter.put("paramlistid", paramlistid);
        filter.put("paramlistversionid", paramlistversionid);
        filter.put("variantid", variantid);
        filter.put("dataset", dataset);
        filter.put("paramid", paramid);
        filter.put("paramtype", paramtype);
        filter.put("replicateid", replicate);
        DataSet match = this.Sample_AddDataItem_DS.getFilteredDataSet(filter);
        return match.getRowCount() > 0;
    }

    private void addRow_Sample_AddDataItem_DS(String rule, String source, String keyid1, String paramlistid, String paramlistversionid, String variantid, String dataset, String paramid, String paramtype, String replicate, String externalreference, String datatype, boolean doApplyDisplayFormat) {
        if (new Integer(replicate) == 1) {
            if (!this.find_Sample_AddDataItem_DS(keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicate)) {
                int currRow = this.Sample_AddDataItem_DS.addRow();
                this.Sample_AddDataItem_DS.setString(currRow, "keyid1", keyid1);
                this.Sample_AddDataItem_DS.setString(currRow, "paramlistid", paramlistid);
                this.Sample_AddDataItem_DS.setString(currRow, "paramlistversionid", paramlistversionid);
                this.Sample_AddDataItem_DS.setString(currRow, "variantid", variantid);
                this.Sample_AddDataItem_DS.setString(currRow, "dataset", dataset);
                this.Sample_AddDataItem_DS.setString(currRow, "paramid", paramid);
                this.Sample_AddDataItem_DS.setString(currRow, "paramtype", paramtype);
                this.Sample_AddDataItem_DS.setString(currRow, "replicateid", replicate);
                this.Sample_AddDataItem_DS.setString(currRow, "externalreference", externalreference);
                this.Sample_AddDataItem_DS.setString(currRow, "datatypes", datatype);
                if (doApplyDisplayFormat) {
                    this.Sample_AddDataItem_DS.setString(currRow, "displayformat", this.empowerPolicyDef.getDisplayFormat());
                } else {
                    this.Sample_AddDataItem_DS.setString(currRow, "displayformat", "");
                }
                this.processLog.addOp(rule, "AddDataItem", "Sample", keyid1 + "[" + paramlistid + "," + paramlistversionid + "," + variantid + "," + dataset + "] externalreference:" + externalreference + ";added=Y", "", "SampleSetLine", "", "");
            }
        } else {
            this.addRow_Sample_AddReplicate_DS(rule, source, keyid1, paramlistid, paramlistversionid, variantid, dataset, paramid, paramtype, replicate);
        }
        if (dataset == null || dataset.length() == 0) {
            dataset = "1";
        }
        this.qcBatchDetails.addSampleDataItem(keyid1, paramlistid, paramlistversionid, variantid, new Integer(dataset), paramid, paramtype, new Integer(replicate));
    }

    public void renderDiffQCBatchDetails(String qcbatchid, QCBatchDetails refDetails) throws SapphireException {
        QCBatchDetails details = new QCBatchDetails(this.connectionInfo, this.queryProcessor, qcbatchid);
        this.processLog.println("QCBatch Details:");
        this.processLog.addDiffTable(details.getQCBatch(), refDetails.getQCBatch(), new String[]{"s_qcbatchid"});
        this.processLog.println("QCBatch DataSets:");
        this.processLog.addDiffTable(details.getQCBatchDataSets(), refDetails.getQCBatchDataSets(), new String[]{"sdcid", "keyid1", "paramlistid", "paramlistversionid", "variantid", "dataset"});
        this.processLog.println("QCBatch DataItems:");
        this.processLog.addDiffTable(details.getQCBatchDataItems(), refDetails.getQCBatchDataItems(), new String[]{"sdcid", "keyid1", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"});
        this.processLog.println("Samples:");
        this.processLog.addDiffTable(details.getSamples(), refDetails.getSamples(), new String[]{"keyid1"});
        this.processLog.println("Sample DataSets:");
        this.processLog.addDiffTable(details.getSampleDataSets(), refDetails.getSampleDataSets(), new String[]{"sdcid", "keyid1", "paramlistid", "paramlistversionid", "variantid", "dataset", "sdidataid"});
        this.processLog.println("Sample DataItems:");
        this.processLog.addDiffTable(details.getSampleDataItems(), refDetails.getSampleDataItems(), new String[]{"sdcid", "keyid1", "paramlistid", "paramlistversionid", "variantid", "dataset", "paramid", "paramtype", "replicateid"});
    }

    public void runActions(boolean newtransaction) throws SapphireException {
        this.process_Sample_EditSDI_DS(newtransaction);
        this.processLog.println("Samples edited successfully.");
        this.process_Sample_AddDataSet_DS(newtransaction);
        this.processLog.println("Sample DataSets added successfully.\n");
        this.process_Sample_EditDataSet_DS(newtransaction);
        this.processLog.println("Sample DataSets edited successfully.\n");
        this.process_Param_AddSDI_DS(newtransaction);
        this.processLog.println("Params Added successfully.\n");
        this.process_Sample_AddDataItem_DS(newtransaction);
        this.processLog.println("Sample DataItems added successfully.\n");
        this.process_Sample_EnterDataItem_DS(newtransaction);
        this.processLog.println("Sample DataItem results entered successfully.\n");
        this.process_Sample_EditDataItem_DS(newtransaction);
        this.processLog.println("Sample DataItems edited successfully.\n");
    }

    private void process_QCBatch_EditSDI_PL(String qcbatchid) throws SapphireException {
        try {
            if (this.QCBatch_EditSDI_PL.size() > 0) {
                this.processLog.addSummary("QCBatch", qcbatchid, "Edited");
                this.QCBatch_EditSDI_PL.setProperty("sdcid", "QCBatch");
                this.QCBatch_EditSDI_PL.setProperty("keyid1", qcbatchid);
                this.processLog.println("Calling EditSDI QCBatch with: ");
                this.processLog.addTable(this.QCBatch_EditSDI_PL);
                this.actionProcessor.processAction("EditSDI", "1", this.QCBatch_EditSDI_PL);
            }
        }
        catch (SapphireException e) {
            throw new SapphireException("Failed to edit QCBatch as specified in the EmpowerUpload policy", e);
        }
    }

    private String process_QCBatch_AddDataSet_DS(String qcbatchid) throws SapphireException {
        if (this.QCBatch_AddDataSet_DS.getRowCount() > 0) {
            PropertyList addDataSetProps = new PropertyList();
            addDataSetProps.setProperty("sdcid", "QCBatch");
            addDataSetProps.setProperty("keyid1", qcbatchid);
            addDataSetProps.setProperty("paramlistid", this.QCBatch_AddDataSet_DS.getColumnValues("paramlistid", DELIMITER));
            addDataSetProps.setProperty("variantid", this.QCBatch_AddDataSet_DS.getColumnValues("variantid", DELIMITER));
            addDataSetProps.setProperty("paramlistversionid", this.QCBatch_AddDataSet_DS.getColumnValues("paramlistversionid", DELIMITER));
            addDataSetProps.setProperty("propsmatch", "Y");
            try {
                this.processLog.println("Calling AddDataSet on QCBatch with: ");
                this.processLog.addTable(addDataSetProps);
                this.actionProcessor.processAction("AddDataSet", "1", addDataSetProps);
                this.processLog.addSummary("QCBatch", qcbatchid, this.QCBatch_AddDataSet_DS.getRowCount() + " DataSets Added");
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to AddDataSet as specified in the EmpowerUpload policy", e);
            }
            return "\nAdded DataSets to QCBatch";
        }
        return "";
    }

    private void process_QCBatch_EditDataSet_DS(String qcbatchid) throws SapphireException {
        if (this.QCBatch_EditDataSet_DS.getRowCount() > 0) {
            PropertyList dataSetProps = new PropertyList();
            dataSetProps.setProperty("sdcid", "QCBatch");
            dataSetProps.setProperty("keyid1", qcbatchid);
            String[] cols = this.QCBatch_EditDataSet_DS.getColumns();
            String collist = "";
            for (int i = 0; i < cols.length; ++i) {
                dataSetProps.setProperty(cols[i], this.QCBatch_EditDataSet_DS.getColumnValues(cols[i], DELIMITER));
                if (i != 0) {
                    collist = collist + ",";
                }
                collist = collist + cols[i];
            }
            try {
                this.processLog.println("Calling EditDataSet QCBatch with: ");
                this.processLog.addTable(dataSetProps);
                this.actionProcessor.processAction("EditDataSet", "1", dataSetProps);
                this.processLog.addSummary("QCBatch", qcbatchid, "Edited DataSet Columns: " + collist);
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to EditDataSet as specified in the EmpowerUpload policy", e);
            }
        }
    }

    private void process_QCBatch_AddDataItem_DS(String qcbatchid) throws SapphireException {
        if (this.QCBatch_AddDataItem_DS.getRowCount() > 0) {
            PropertyList addDataItemProps = new PropertyList();
            addDataItemProps.setProperty("sdcid", "QCBatch");
            addDataItemProps.setProperty("keyid1", qcbatchid);
            addDataItemProps.setProperty("paramlistid", this.QCBatch_AddDataItem_DS.getColumnValues("paramlistid", DELIMITER));
            addDataItemProps.setProperty("variantid", this.QCBatch_AddDataItem_DS.getColumnValues("variantid", DELIMITER));
            addDataItemProps.setProperty("paramid", this.QCBatch_AddDataItem_DS.getColumnValues("paramid", DELIMITER));
            addDataItemProps.setProperty("paramtype", this.QCBatch_AddDataItem_DS.getColumnValues("paramtype", DELIMITER));
            addDataItemProps.setProperty("dataset", this.QCBatch_AddDataItem_DS.getColumnValues("dataset", DELIMITER));
            addDataItemProps.setProperty("replicateid", this.QCBatch_AddDataItem_DS.getColumnValues("replicateid", DELIMITER));
            addDataItemProps.setProperty("paramlistcheck", "N");
            addDataItemProps.setProperty("propsmatch", "Y");
            addDataItemProps.setProperty("displayformat", this.QCBatch_AddDataItem_DS.getColumnValues("displayformat", DELIMITER));
            try {
                this.processLog.println("Calling AddDataItem QCBatch with: ");
                this.processLog.addTable(addDataItemProps);
                this.actionProcessor.processAction("AddDataItem", "1", addDataItemProps);
                this.processLog.addSummary("QCBatch", qcbatchid, this.QCBatch_AddDataItem_DS.getRowCount() + " DataItems Added");
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to AddDataItem as specified in the EmpowerUpload policy", e);
            }
        }
    }

    private void process_QCBatch_EditDataItem_DS(String qcbatchid) throws SapphireException {
        if (this.QCBatch_EditDataItem_DS.getRowCount() > 0) {
            PropertyList editDataItemProps = new PropertyList();
            String[] cols = this.QCBatch_EditDataItem_DS.getColumns();
            editDataItemProps.setProperty("sdcid", "QCBatch");
            editDataItemProps.setProperty("keyid1", qcbatchid);
            String collist = "";
            for (int i = 0; i < cols.length; ++i) {
                editDataItemProps.setProperty(cols[i], this.QCBatch_EditDataItem_DS.getColumnValues(cols[i], DELIMITER));
                if (i != 0) {
                    collist = collist + ",";
                }
                collist = collist + cols[i];
            }
            try {
                this.processLog.println("Calling EditDataItem QCBatch with: ");
                this.processLog.addTable(editDataItemProps);
                this.actionProcessor.processAction("EditDataItem", "1", editDataItemProps);
                this.processLog.addSummary("QCBatch", qcbatchid, "Edited DataItem Cols: " + collist);
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to EditDataItem as specified in the EmpowerUpload policy", e);
            }
        }
    }

    private void process_QCBatch_EnterDataItem_DS(String qcbatchid) throws SapphireException {
        if (this.QCBatch_EnterDataItem_DS.getRowCount() > 0) {
            PropertyList enterDataItemProps = new PropertyList();
            enterDataItemProps.setProperty("sdcid", "QCBatch");
            enterDataItemProps.setProperty("keyid1", qcbatchid);
            enterDataItemProps.setProperty("paramlistid", this.QCBatch_EnterDataItem_DS.getColumnValues("paramlistid", DELIMITER));
            enterDataItemProps.setProperty("paramlistversionid", this.QCBatch_EnterDataItem_DS.getColumnValues("paramlistversionid", DELIMITER));
            enterDataItemProps.setProperty("variantid", this.QCBatch_EnterDataItem_DS.getColumnValues("variantid", DELIMITER));
            enterDataItemProps.setProperty("paramid", this.QCBatch_EnterDataItem_DS.getColumnValues("paramid", DELIMITER));
            enterDataItemProps.setProperty("paramtype", this.QCBatch_EnterDataItem_DS.getColumnValues("paramtype", DELIMITER));
            enterDataItemProps.setProperty("dataset", this.QCBatch_EnterDataItem_DS.getColumnValues("dataset", DELIMITER));
            enterDataItemProps.setProperty("replicateid", this.QCBatch_EnterDataItem_DS.getColumnValues("replicateid", DELIMITER));
            enterDataItemProps.setProperty("enteredtext", this.QCBatch_EnterDataItem_DS.getColumnValues("enteredtext", DELIMITER));
            enterDataItemProps.setProperty("propsmatch", "Y");
            if (this.empowerPolicyDef.autoReleaseResults()) {
                enterDataItemProps.setProperty("autorelease", "Y");
            }
            if (this.empowerPolicyDef.getHandlingReleasedResults().equals("Override")) {
                enterDataItemProps.setProperty("overridereleased", "Y");
            }
            try {
                this.processLog.println("Calling EnterDataItem QCBatch with: ");
                this.processLog.addTable(enterDataItemProps);
                this.actionProcessor.processAction("EnterDataItem", "1", enterDataItemProps);
                this.processLog.addSummary("QCBatch", qcbatchid, this.QCBatch_EnterDataItem_DS.getRowCount() + " Results entered");
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to enterDataItem as specified in the EmpowerUpload policy", e);
            }
        }
    }

    private String findCurrentParamListVersion(String paramlistid, String variantid) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("paramlistid", paramlistid);
        filter.put("variantid", variantid);
        int i = this.paramListCurrentVersion.findRow(filter);
        if (i == -1) {
            this.paramListCurrentVersion = this.updateParamListCurrentVersion(this.paramListCurrentVersion, paramlistid, variantid);
        }
        return this.paramListCurrentVersion.getString(i, "paramlistversionid", "1");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void mapQCBatchDataSetColumn(String qcbatchid, DataSet sampleSetRules, int ruleItem, String empowerColumnValue) throws SapphireException {
        String rule = "QCBatch DataSet Column";
        String currds = "1";
        String currversion = "";
        String empowerCol = sampleSetRules.getString(ruleItem, "empowercolumn");
        empowerCol = this.empowerPolicyDef.getTranslate(empowerCol);
        String paramlistid = sampleSetRules.getString(ruleItem, "paramlistid");
        String variantid = sampleSetRules.getString(ruleItem, "variantid");
        String sapphireColumn = sampleSetRules.getString(ruleItem, "columnid");
        DataSet match = this.qcBatchDetails.getQCBatchDataSet(paramlistid, variantid);
        String externalreference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName);
        if (match == null || match.getRowCount() == 0) {
            if ("N".equals(sampleSetRules.getString(ruleItem, "AddDSIfNeeded"))) return;
            currversion = this.findCurrentParamListVersion(paramlistid, variantid);
            this.addRow_QCBatch_AddDataSet_DS(rule, paramlistid, currversion, variantid, currds);
            this.addRow_QCBatch_EditDataSet_DS(rule, qcbatchid, paramlistid, currversion, variantid, currds, "externalreference", empowerCol, externalreference);
        } else {
            currds = match.getValue(0, "dataset");
            currversion = match.getString(0, "paramlistversionid");
        }
        this.addRow_QCBatch_EditDataSet_DS(rule, qcbatchid, paramlistid, currversion, variantid, currds, sapphireColumn, empowerCol, empowerColumnValue);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void mapQCBatchDataItemResult(String qcbatchid, DataSet sampleSetRules, int ruleItem, String empowerColumnValue) throws SapphireException {
        String rule = "QCBatch DataItem Result";
        String currds = "1";
        String currversion = "";
        String empowerCol = sampleSetRules.getString(ruleItem, "empowercolumn");
        String paramlistid = sampleSetRules.getString(ruleItem, "paramlistid");
        String variantid = sampleSetRules.getString(ruleItem, "variantid");
        DataSet match = this.qcBatchDetails.getQCBatchDataSet(paramlistid, variantid);
        String externalreference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName);
        if (match == null || match.getRowCount() == 0) {
            if ("N".equals(sampleSetRules.getString(ruleItem, "AddDSIfNeeded"))) return;
            currversion = this.findCurrentParamListVersion(paramlistid, variantid);
            this.addRow_QCBatch_AddDataSet_DS(rule, paramlistid, currversion, variantid, currds);
            this.addRow_QCBatch_EditDataSet_DS(rule, qcbatchid, paramlistid, currversion, variantid, currds, "externalreference", empowerCol, externalreference);
        } else {
            currds = "" + match.getValue(0, "dataset");
            currversion = match.getValue(0, "paramlistversionid");
        }
        String paramid = sampleSetRules.getString(ruleItem, "paramid");
        String paramtype = sampleSetRules.getString(ruleItem, "paramtype");
        String replicate = sampleSetRules.getString(ruleItem, "replicateid");
        if (!this.qcBatchDetails.checkQCBatchDataItem(paramlistid, variantid, currds, paramid, paramtype, replicate)) {
            if ("N".equals(sampleSetRules.getString(ruleItem, "AddDIIfNeeded"))) return;
            this.addRow_Param_AddSDI_DS(paramid);
            this.addRow_QCBatch_AddDataItem_DS(rule, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate);
            this.addRow_QCBatch_EditDataItem_DS(rule, qcbatchid, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerCol, externalreference);
        }
        this.addRow_QCBatch_EnterDataItem_DS(rule, qcbatchid, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, empowerCol, empowerColumnValue);
        this.addRow_QCBatch_EditDataItem_DS(rule, qcbatchid, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerCol, externalreference);
    }

    private void mapQCBatchSDIColumn(String qcbatchid, DataSet sampleSetRules, int ruleItem, String empowerColumnValue) {
        String rule = "QCBatch Column";
        String empowerCol = sampleSetRules.getString(ruleItem, "empowercolumn");
        empowerCol = this.empowerPolicyDef.getTranslate(empowerCol);
        this.processLog.addOp(rule, "EditSDI", "QCBatch", qcbatchid, sampleSetRules.getString(ruleItem, "columnid"), "SampleSet", empowerCol, empowerColumnValue);
        this.QCBatch_EditSDI_PL.setProperty(sampleSetRules.getString(ruleItem, "columnid"), empowerColumnValue);
        this.processLog.println("Editing QCBatch Column:" + sampleSetRules.getString(ruleItem, "columnid") + " with value:" + empowerColumnValue);
    }

    private void mapSampleSDIColumn(String keyid1, DataSet sampleSetLineRules, int ruleItem, String empowerColumnValue) {
        String rule = "Sample Column";
        String columnname = sampleSetLineRules.getString(ruleItem, "columnid");
        String empowerCol = sampleSetLineRules.getString(ruleItem, "empowercolumn");
        empowerCol = this.empowerPolicyDef.getTranslate(empowerCol);
        int row = this.Sample_EditSDI_DS.addRow();
        this.Sample_EditSDI_DS.setString(row, "keyid1", keyid1);
        this.Sample_EditSDI_DS.setString(row, columnname, empowerColumnValue);
        this.processLog.addOp(rule, "EditSDI", "Sample", keyid1, columnname, "SampleSetLine", empowerCol, empowerColumnValue);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void mapSampleDataSetColumn(String keyid1, String datasetkeyes, DataSet sampleSetLineRules, int ruleItem, String empowerColumnValue) throws SapphireException {
        String rule = "Sample DataSet Column";
        String paramlist = sampleSetLineRules.getString(ruleItem, "paramlistid", "");
        String sapphireColumnId = sampleSetLineRules.getString(ruleItem, "columnid", "");
        String variantid = "";
        String externalreference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName);
        if (paramlist.equals("#")) {
            String[] keyarr = StringUtil.split(datasetkeyes, DELIMITER);
            for (int currds = 0; currds < keyarr.length; ++currds) {
                String key = keyarr[currds];
                DataSet dsdetails = this.qcBatchDetails.getSampleDataSetDetailsByKey(key);
                String currPL = dsdetails.getString(0, "paramlistid");
                String currPLVar = dsdetails.getString(0, "variantid");
                String currversion = dsdetails.getString(0, "paramlistversionid");
                String currdataset = dsdetails.getValue(0, "dataset");
                this.addRow_Sample_EditDataSet_DS(rule, "SampleSetLine", keyid1, currPL, currversion, currPLVar, currdataset, sapphireColumnId, empowerColumnValue, externalreference);
            }
            return;
        } else {
            variantid = sampleSetLineRules.getString(ruleItem, "variantid", "");
            DataSet match = this.qcBatchDetails.getSampleDataSet(keyid1, paramlist, variantid);
            String currds = "1";
            String currversion = "";
            if (match == null || match.getRowCount() == 0) {
                currversion = this.findCurrentParamListVersion(paramlist, variantid);
                if ("N".equals(sampleSetLineRules.getString(ruleItem, "AddDSIfNeeded"))) return;
                this.addRow_Sample_AddDataSet_DS(rule, "SampleSetLine", keyid1, paramlist, currversion, variantid, currds, externalreference);
            } else {
                currds = match.getValue(0, "dataset");
                currversion = match.getString(0, "paramlistversionid");
            }
            this.addRow_Sample_EditDataSet_DS(rule, "SampleSetLine", keyid1, paramlist, currversion, variantid, currds, sapphireColumnId, empowerColumnValue, externalreference);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void mapSampleDataItemResult(DataSet sampleSetLineItems, int currsample, DataSet sampleSetLineRules, int ruleItem, String empowerColumnValue) throws SapphireException {
        String rule = "Sample DataItem Result";
        String keyid1 = sampleSetLineItems.getString(currsample, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
        String paramlistid = sampleSetLineRules.getString(ruleItem, "paramlistid");
        String variantid = "";
        String currds = "1";
        String currversion = "";
        String empowerColumn = sampleSetLineRules.getString(ruleItem, "empowercolumn");
        empowerColumn = this.empowerPolicyDef.getTranslate(empowerColumn);
        String paramid = sampleSetLineRules.getString(ruleItem, "paramid");
        String paramtype = sampleSetLineRules.getString(ruleItem, "paramtype");
        String replicate = sampleSetLineRules.getString(ruleItem, "replicateid");
        String externalreference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName);
        boolean dataitemadded = false;
        if (paramlistid.equals("#")) {
            this.mapContextDataItemResult(sampleSetLineItems, currsample, sampleSetLineRules, ruleItem);
            return;
        } else {
            variantid = sampleSetLineRules.getString(ruleItem, "variantid");
            DataSet match = this.qcBatchDetails.getSampleDataSet(keyid1, paramlistid, variantid);
            if (match == null || match.getRowCount() == 0) {
                currversion = this.findCurrentParamListVersion(paramlistid, variantid);
                if ("N".equals(sampleSetLineRules.getString(ruleItem, "AddDSIfNeeded"))) return;
                this.addRow_Sample_AddDataSet_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, externalreference);
            } else {
                if (match.getValue(0, "releasedflag", "N").equals("Y")) {
                    throw new SapphireException("Cannot upload results on Sample since result for:" + paramid + " is already released");
                }
                currds = match.getValue(0, "dataset");
                currversion = match.getString(0, "paramlistversionid");
            }
            if (!this.qcBatchDetails.checkSampleDataItem(keyid1, paramlistid, variantid, currds, paramid, paramtype, replicate)) {
                if ("N".equals(sampleSetLineRules.getString(ruleItem, "AddDIIfNeeded"))) return;
                dataitemadded = true;
                String dataType = this.determineDataType(this.empowerPolicyDef, false, empowerColumnValue);
                boolean doApplyDisplayFormat = this.applyDisplayFormat(this.empowerPolicyDef, false);
                this.addRow_Sample_AddDataItem_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, externalreference, dataType, doApplyDisplayFormat);
            }
            Trace.logDebug("Diagnostic::: dataitemadded:" + dataitemadded);
            if (dataitemadded || this.preDataEntryCheck(keyid1, paramlistid, variantid, currds, paramid, paramtype, replicate)) {
                Trace.logDebug("Processing item:::" + keyid1 + ":" + paramid + ":" + paramtype);
                Trace.logDebug("Diagnostic::: enterdataitem called on paramlistid:" + paramlistid + " variantid:" + variantid + " version:" + currversion + " dataset" + currds + " paramid:" + paramid + "paramtype:" + paramtype + "EmpowerColumn:" + empowerColumn + " empower colimn value:" + empowerColumnValue);
                this.addRow_Sample_EnterDataItem_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, empowerColumn, empowerColumnValue);
                if (dataitemadded) {
                    this.addRow_Sample_EditDataItem_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference);
                    return;
                } else {
                    this.addRow_Sample_EditDataItem_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference + ";added=Y");
                }
                return;
            } else {
                Trace.logDebug("Diagnostic::: skipping data entry:" + keyid1 + ":" + paramid + ":" + paramtype);
            }
        }
    }

    private String getPrimary(String keyes) throws SapphireException {
        if (keyes == null || keyes.length() == 0) {
            throw new SapphireException("DataSetKeys are empty");
        }
        String[] keyArr = StringUtil.split(keyes, DELIMITER);
        String sql = "SELECT s_paramlisttype, sdidataid FROM paramlist,sdidata  WHERE paramlist.paramlistid = sdidata.paramlistid and paramlist.variantid= sdidata.variantid AND sdidataid = ? ";
        for (int i = 0; i < keyArr.length; ++i) {
            String paramlisttype;
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sdidataid", keyArr[i]);
            this.database.createPreparedResultSet("CheckPK", sql, new Object[]{keyArr[i]});
            if (!this.database.getNext("CheckPK") || !"Procedural".equals(paramlisttype = this.database.getString("CheckPK", "s_paramlisttype"))) continue;
            return keyArr[i];
        }
        return keyArr[0];
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void mapContextDataItemResult(DataSet ds, int currdsitem, DataSet sampleSetLineRules, int ruleItem) throws SapphireException {
        String rule = "DataItem Result";
        String keyid1 = ds.getString(currdsitem, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
        String datasetkeyes = ds.getString(currdsitem, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"));
        String sdiDataIdList = this.getSDIDataIdList(datasetkeyes);
        String currds = "1";
        String paramid = sampleSetLineRules.getString(ruleItem, "paramid");
        String paramtype = sampleSetLineRules.getString(ruleItem, "paramtype");
        String replicate = sampleSetLineRules.getString(ruleItem, "replicateid");
        String empowerColumn = sampleSetLineRules.getString(ruleItem, "empowercolumn");
        empowerColumn = this.empowerPolicyDef.getTranslate(empowerColumn);
        String empowerColumnValue = ds.getValue(currdsitem, empowerColumn);
        String externalreference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName);
        String paramlistid = "#";
        String variantid = "#";
        String currversion = "";
        DataSet match = this.qcBatchDetails.getSampleDataItemsByKeys(keyid1, paramid, paramtype, replicate, StringUtil.split(sdiDataIdList, DELIMITER));
        boolean dataitemadded = false;
        if (match == null || match.getRowCount() == 0) {
            if ("N".equals(sampleSetLineRules.getString(ruleItem, "AddDIIfNeeded"))) return;
            String primarykey = this.getPrimary(sdiDataIdList);
            DataSet primary = this.qcBatchDetails.getSampleDataSetDetailsByKey(primarykey);
            if (primary.getRowCount() == 0) {
                throw new SapphireException("Cannot find primary dataset for:" + primarykey);
            }
            paramlistid = primary.getString(0, "paramlistid");
            variantid = primary.getString(0, "variantid");
            currversion = primary.getString(0, "paramlistversionid");
            currds = primary.getValue(0, "dataset", "1");
            String dataType = this.determineDataType(this.empowerPolicyDef, false, empowerColumnValue);
            boolean doApplyDisplayFormat = this.applyDisplayFormat(this.empowerPolicyDef, false);
            this.addRow_Sample_AddDataItem_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, externalreference, dataType, doApplyDisplayFormat);
            dataitemadded = true;
        } else {
            paramlistid = match.getString(0, "paramlistid");
            variantid = match.getString(0, "variantid");
            currds = match.getValue(0, "dataset");
            currversion = match.getString(0, "paramlistversionid");
        }
        if (dataitemadded || this.preDataEntryCheck(keyid1, paramlistid, variantid, currds, paramid, paramtype, replicate)) {
            this.addRow_Sample_EnterDataItem_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, empowerColumn, empowerColumnValue);
            if (dataitemadded) {
                this.addRow_Sample_EditDataItem_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference + ";added=Y");
            } else {
                this.addRow_Sample_EditDataItem_DS(rule, "SampleSetLine", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference);
            }
            Trace.logDebug("Processing item:" + keyid1 + ":" + paramid + ":" + paramtype);
            return;
        } else {
            Trace.logDebug("Skipping item:" + keyid1 + ":" + paramid + ":" + paramtype);
        }
    }

    private String getSDIDataIdList(String limsDataSetKey) throws SapphireException {
        Trace.logDebug("limsdatasetkey: " + limsDataSetKey);
        if (limsDataSetKey.startsWith("QCBATCHITEMID=")) {
            String[] tokens = StringUtil.split(limsDataSetKey, "=");
            DataSet allDataSets = this.qcBatchDetails.getSampleDataSets();
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("s_qcbatchitemid", tokens[1].trim());
            DataSet batchitemdatasets = allDataSets.getFilteredDataSet(filter);
            if (batchitemdatasets == null || batchitemdatasets.getRowCount() == 0) {
                throw new SapphireException("Invalid s_qcbatchitemid:" + tokens[1].trim());
            }
            Trace.logDebug("sdidataidlist: " + batchitemdatasets.getColumnValues("sdidataid", DELIMITER));
            return batchitemdatasets.getColumnValues("sdidataid", DELIMITER);
        }
        if (limsDataSetKey.contains("WORKITEMID")) {
            Trace.logDebug("limsdatasetkey interpretation logic for adhoc mode");
            String[] properties = StringUtil.split(limsDataSetKey, DELIMITER);
            String sdcid = "";
            String keyid1 = "";
            String workitemid = "";
            String workiteminstance = "";
            for (String prop : properties) {
                String[] tokens = StringUtil.split(prop, "=");
                if (tokens.length != 2) {
                    throw new SapphireException("Invalid dataset keys");
                }
                if (tokens[0].equals("SDCID")) {
                    sdcid = tokens[1];
                    continue;
                }
                if (tokens[0].equals("KEYID1")) {
                    keyid1 = tokens[1];
                    continue;
                }
                if (tokens[0].equals("WORKITEMID")) {
                    workitemid = tokens[1];
                    continue;
                }
                if (!tokens[0].equals("WORKITEMINSTANCE")) continue;
                workiteminstance = tokens[1];
            }
            if (sdcid.length() == 0 || keyid1.length() == 0 || workitemid.length() == 0 || workiteminstance.length() == 0) {
                throw new SapphireException("Invalid dataset key");
            }
            DataSet allSampleDataSets = this.qcBatchDetails.getSampleDataSets();
            Trace.logDebug("Sample DataSets:" + allSampleDataSets.toXML());
            HashMap<String, Object> filter = new HashMap<String, Object>();
            filter.put("sdcid", sdcid);
            filter.put("keyid1", keyid1);
            filter.put("workitemid", workitemid);
            filter.put("workiteminstance", new BigDecimal(workiteminstance));
            DataSet match = allSampleDataSets.getFilteredDataSet(filter);
            Trace.log("sdidataid list: " + match.getColumnValues("sdidataid", DELIMITER));
            return match.getColumnValues("sdidataid", DELIMITER);
        }
        return limsDataSetKey;
    }

    private void processSampleSetLineRules(DataSet sampleSetLineItems, DataSet sampleSetLineRules) throws SapphireException {
        for (int i = 0; i < sampleSetLineRules.size(); ++i) {
            String ruleType = sampleSetLineRules.getString(i, "ruletype");
            String empowerColumn = sampleSetLineRules.getString(i, "empowercolumn");
            empowerColumn = this.empowerPolicyDef.getTranslate(empowerColumn);
            for (int currsample = 0; currsample < sampleSetLineItems.getRowCount(); ++currsample) {
                String empowerColumnValue = sampleSetLineItems.getValue(currsample, empowerColumn, DELIMITER);
                String keyid1 = sampleSetLineItems.getString(currsample, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
                if ("Sample".equals(ruleType)) {
                    this.mapSampleSDIColumn(keyid1, sampleSetLineRules, i, empowerColumnValue);
                    continue;
                }
                if ("DataSet".equals(ruleType)) {
                    String datasetkeyes = sampleSetLineItems.getString(currsample, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"));
                    String sdiDataIdList = this.getSDIDataIdList(datasetkeyes);
                    this.mapSampleDataSetColumn(keyid1, sdiDataIdList, sampleSetLineRules, i, empowerColumnValue);
                    continue;
                }
                if (!"DataItem".equals(ruleType)) continue;
                this.mapSampleDataItemResult(sampleSetLineItems, currsample, sampleSetLineRules, i, empowerColumnValue);
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void mapResultDataSetColumn(DataSet results, int resultItem, DataSet resultRules, int ruleItem) throws SapphireException {
        String rule = "Sample DataSet Column";
        String keyid1 = results.getString(resultItem, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
        String paramlist = resultRules.getString(ruleItem, "paramlistid", "");
        String sapphireColumn = resultRules.getString(ruleItem, "columnid");
        String empowerColumn = resultRules.getString(ruleItem, "empowercolumn");
        empowerColumn = this.empowerPolicyDef.getTranslate(empowerColumn);
        String empowerColumnValue = results.getValue(resultItem, empowerColumn, "");
        String variantid = "";
        String currversion = "";
        String resultSetId = results.getValue(resultItem, "ResultSetId", "");
        String resultId = results.getValue(resultItem, "ResultId", "");
        String externalreference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName, resultSetId, resultId);
        if (paramlist.equals("#")) {
            String dataSetKeyes = results.getString(resultItem, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"));
            String sdiDataIdList = this.getSDIDataIdList(dataSetKeyes);
            String[] sdiDataIds = StringUtil.split(sdiDataIdList, DELIMITER);
            for (int currds = 0; currds < sdiDataIds.length; ++currds) {
                String key = sdiDataIds[currds];
                DataSet dsdetails = this.qcBatchDetails.getSampleDataSetDetailsByKey(key);
                String currPL = dsdetails.getString(0, "paramlistid");
                String currPLVar = dsdetails.getString(0, "variantid");
                String currdataset = dsdetails.getValue(0, "dataset");
                currversion = dsdetails.getString(0, "paramlistversionid");
                this.addRow_Sample_EditDataSet_DS(rule, "Result", keyid1, currPL, currversion, currPLVar, currdataset, sapphireColumn, empowerColumnValue, externalreference);
            }
            return;
        } else {
            variantid = resultRules.getString(ruleItem, "variantid");
            DataSet match = this.qcBatchDetails.getSampleDataSet(keyid1, paramlist, variantid);
            String currds = "1";
            if (match == null || match.getRowCount() == 0) {
                if ("N".equals(resultRules.getString(ruleItem, "AddDSIfNeeded"))) return;
                currversion = this.findCurrentParamListVersion(paramlist, variantid);
                this.addRow_Sample_AddDataSet_DS(rule, "Result", keyid1, paramlist, currversion, variantid, currds, externalreference);
            } else {
                currds = match.getValue(0, "dataset");
                currversion = match.getString(0, "paramlistversionid");
            }
            this.addRow_Sample_EditDataSet_DS(rule, "Result", keyid1, paramlist, currversion, variantid, currds, sapphireColumn, empowerColumnValue, externalreference);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void mapResultDataItemResult(DataSet results, int currresult, DataSet rules, int ruleItem) throws SapphireException {
        String rule = "DataItem Result";
        String currds = "1";
        String paramlistid = rules.getString(ruleItem, "paramlistid");
        String variantid = "";
        String paramid = rules.getString(ruleItem, "paramid");
        String paramtype = rules.getString(ruleItem, "paramtype");
        String replicate = results.getValue(currresult, this.empowerPolicyDef.getTranslate("Injection"));
        String keyid1 = results.getString(currresult, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
        String datasetkey = results.getString(currresult, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"));
        String sdiDataIdList = this.getSDIDataIdList(datasetkey);
        String empowerColumn = rules.getString(ruleItem, "empowercolumn");
        empowerColumn = this.empowerPolicyDef.getTranslate(empowerColumn);
        String empowerColumnValue = results.getValue(currresult, empowerColumn, "");
        String resultSetId = results.getValue(currresult, this.empowerPolicyDef.getTranslate("ResultSetId"), "");
        String resultId = results.getValue(currresult, this.empowerPolicyDef.getTranslate("ResultId"), "");
        String channel = results.getValue(currresult, this.empowerPolicyDef.getTranslate("ChannelName"), "");
        String externalreference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName, resultSetId, resultId);
        externalreference = externalreference + ";channel=" + channel;
        String currversion = "";
        if (paramlistid.equals("#")) {
            DataSet match = this.qcBatchDetails.getSampleDataItemsByKeys(keyid1, paramid, paramtype, replicate, StringUtil.split(sdiDataIdList, DELIMITER));
            boolean dataitemadded = false;
            if (match == null || match.getRowCount() == 0) {
                if ("N".equals(rules.getString(ruleItem, "AddDIIfNeeded"))) return;
                if (!replicate.equals("1")) {
                    match = this.qcBatchDetails.getSampleDataItemsByKeys(keyid1, paramid, paramtype, "*", StringUtil.split(sdiDataIdList, DELIMITER));
                    if (match != null && match.getRowCount() > 0) {
                        paramlistid = match.getString(0, "paramlistid");
                        variantid = match.getString(0, "variantid");
                        currds = match.getValue(0, "dataset");
                        currversion = match.getString(0, "paramlistversionid");
                        dataitemadded = true;
                        externalreference = externalreference + ";added=Y";
                        String dataType = this.determineDataType(this.empowerPolicyDef, false, empowerColumnValue);
                        boolean doApplyDisplayFormat = this.applyDisplayFormat(this.empowerPolicyDef, false);
                        this.addRow_Sample_AddDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, externalreference, dataType, doApplyDisplayFormat);
                    } else {
                        String datasetkeyes = match.getString(currresult, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"));
                        String sdiDataIdList1 = this.getSDIDataIdList(datasetkeyes);
                        String primarykey = this.getPrimary(sdiDataIdList1);
                        DataSet primary = this.qcBatchDetails.getSampleDataSetDetailsByKey(primarykey);
                        paramlistid = primary.getString(0, "paramlistid");
                        variantid = primary.getString(0, "variantid");
                        currds = primary.getValue(0, "dataset");
                        currversion = primary.getString(0, "paramlistversionid");
                        int replicatenum = new Integer(replicate);
                        for (int i = 0; i < replicatenum; ++i) {
                            if (i == 0) {
                                this.addRow_Param_AddSDI_DS(paramid);
                            }
                            dataitemadded = true;
                            externalreference = externalreference + ";added=Y";
                            String dataType = this.determineDataType(this.empowerPolicyDef, false, empowerColumnValue);
                            boolean doApplyDisplayFormat = this.applyDisplayFormat(this.empowerPolicyDef, false);
                            this.addRow_Sample_AddDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, "" + (i + 1), externalreference, dataType, doApplyDisplayFormat);
                        }
                    }
                } else {
                    String datasetkeyesnew = results.getString(currresult, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"));
                    String sdiDataIdList1 = this.getSDIDataIdList(datasetkeyesnew);
                    String primarykey = this.getPrimary(sdiDataIdList1);
                    DataSet primary = this.qcBatchDetails.getSampleDataSetDetailsByKey(primarykey);
                    paramlistid = primary.getString(0, "paramlistid");
                    variantid = primary.getString(0, "variantid");
                    currds = primary.getValue(0, "dataset");
                    currversion = primary.getString(0, "paramlistversionid");
                    dataitemadded = true;
                    String dataType = this.determineDataType(this.empowerPolicyDef, false, empowerColumnValue);
                    boolean doApplyDisplayFormat = this.applyDisplayFormat(this.empowerPolicyDef, false);
                    this.addRow_Sample_AddDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, externalreference + ";added=Y", dataType, doApplyDisplayFormat);
                }
            } else {
                paramlistid = match.getString(0, "paramlistid");
                variantid = match.getString(0, "variantid");
                currds = match.getValue(0, "dataset");
                currversion = match.getString(0, "paramlistversionid");
            }
            if (dataitemadded || this.preDataEntryCheck(keyid1, paramlistid, variantid, currds, paramid, paramtype, replicate)) {
                this.addRow_Sample_EnterDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, empowerColumn, empowerColumnValue);
                if (dataitemadded) {
                    this.addRow_Sample_EditDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference + ";added=Y");
                } else {
                    this.addRow_Sample_EditDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference);
                }
                Trace.logDebug("Processing item:" + keyid1 + ":" + paramid + ":" + paramtype);
                return;
            } else {
                Trace.logDebug("Skipping item:" + keyid1 + ":" + paramid + ":" + paramtype);
            }
            return;
        } else {
            variantid = rules.getString(ruleItem, "variantid");
            DataSet matchDS = this.qcBatchDetails.getSampleDataSet(keyid1, paramlistid, variantid);
            boolean dataitemadded = false;
            if (matchDS == null || matchDS.getRowCount() == 0) {
                if ("N".equals(rules.getString(ruleItem, "AddDSIfNeeded"))) return;
                currversion = this.findCurrentParamListVersion(paramlistid, variantid);
                this.addRow_Sample_AddDataSet_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, externalreference);
            } else {
                currds = matchDS.getValue(0, "dataset");
                currversion = matchDS.getString(0, "paramlistversionid");
            }
            if (!this.qcBatchDetails.checkSampleDataItem(keyid1, paramlistid, variantid, currds, paramid, paramtype, replicate)) {
                if ("N".equals(rules.getString(ruleItem, "AddDIIfNeeded"))) return;
                dataitemadded = true;
                externalreference = externalreference + ";added=Y";
                String dataType = this.determineDataType(this.empowerPolicyDef, false, empowerColumnValue);
                boolean doApplyDisplayFormat = this.applyDisplayFormat(this.empowerPolicyDef, false);
                this.addRow_Sample_AddDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, externalreference, dataType, doApplyDisplayFormat);
            }
            if (dataitemadded || this.preDataEntryCheck(keyid1, paramlistid, variantid, currds, paramid, paramtype, replicate)) {
                this.addRow_Sample_EnterDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, empowerColumn, empowerColumnValue);
                if (dataitemadded) {
                    this.addRow_Sample_EditDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference + ";added=Y");
                } else {
                    this.addRow_Sample_EditDataItem_DS(rule, "Result", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference);
                }
                Trace.logDebug("Processing item:" + keyid1 + ":" + paramid + ":" + paramtype);
                return;
            } else {
                Trace.logDebug("Skipping item:" + keyid1 + ":" + paramid + ":" + paramtype);
            }
        }
    }

    private String determineDataType(EmpowerPolicyDef empowerPolicyDef, boolean unknownPeakType, String empowerColumnValue) {
        boolean validateStrictDataType = empowerPolicyDef.getValidateStrictDataType();
        String dataType = "N";
        if (validateStrictDataType && !unknownPeakType) {
            try {
                Float.parseFloat(empowerColumnValue);
                dataType = "N";
            }
            catch (NumberFormatException e) {
                dataType = "A";
            }
        }
        return dataType;
    }

    private boolean applyDisplayFormat(EmpowerPolicyDef empowerPolicyDef, boolean unknownPeakType) {
        boolean doApplyDisplayFormat = true;
        boolean strictDisplayFormat = empowerPolicyDef.getStrictDisplayFormat();
        if (strictDisplayFormat && !unknownPeakType) {
            doApplyDisplayFormat = false;
        }
        return doApplyDisplayFormat;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void mapPeakDataItem(DataSet peaks, int currpeak, DataSet rules, int ruleItem) throws SapphireException {
        String rule = "DataItem Column/Result";
        String currds = "1";
        String paramlistid = rules.getString(ruleItem, "paramlistid");
        String variantid = "";
        String paramtype = rules.getString(ruleItem, "paramtype");
        String keyid1 = peaks.getString(currpeak, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
        String empowerColumn = rules.getString(ruleItem, "empowercolumn");
        empowerColumn = this.empowerPolicyDef.getTranslate(empowerColumn);
        String empowerColumnValue = peaks.getString(currpeak, empowerColumn, "");
        String emppeakName = peaks.getString(currpeak, this.empowerPolicyDef.getTranslate("Name"), "");
        String replicate = peaks.getValue(currpeak, this.empowerPolicyDef.getTranslate("Injection"), "");
        String sapphirecol = rules.getString(ruleItem, "columnid");
        String resultSetId = peaks.getValue(currpeak, this.empowerPolicyDef.getTranslate("ResultSetId"), "");
        String resultId = peaks.getValue(currpeak, this.empowerPolicyDef.getTranslate("ResultId"), "");
        String channel = peaks.getValue(currpeak, this.empowerPolicyDef.getTranslate("ChannelName"), "");
        String externalreference = QCBatchDetails.getExternalReference(this.empowerProject, this.empowerDatabase, this.sampleSetName, resultSetId, resultId);
        externalreference = externalreference + ";channel=" + channel;
        String currversion = "";
        String datasetkeys = peaks.getString(currpeak, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey").toLowerCase());
        String sdiDataIdList = this.getSDIDataIdList(datasetkeys);
        String retentionTime = peaks.getString(currpeak, this.empowerPolicyDef.getTranslate("RetentionTime"), "-999");
        String peakType = peaks.getString(currpeak, this.empowerPolicyDef.getTranslate("PeakType").toLowerCase());
        boolean unknownPeakType = peakType.equalsIgnoreCase(this.empowerPolicyDef.getTranslate("Unknown"));
        String dataType = this.determineDataType(this.empowerPolicyDef, unknownPeakType, empowerColumnValue);
        boolean doApplyDisplayFormat = this.applyDisplayFormat(this.empowerPolicyDef, unknownPeakType);
        String paramid = "";
        if (!paramlistid.equals("#")) return;
        Trace.logDebug("Diagnostic::: In mapPeakDataItem paramlistid is #");
        boolean dataitemadded = false;
        DataSet match = this.qcBatchDetails.getSampleDataItemsForPeak(keyid1, emppeakName, paramtype, replicate, StringUtil.split(sdiDataIdList, DELIMITER));
        if (match == null || match.getRowCount() == 0) {
            Trace.logDebug("Diagnostic::: Did not find a match in sample dataitemkeys");
            if ("N".equals(rules.getString(ruleItem, "AddDIIfNeeded"))) return;
            if (!replicate.equals("1")) {
                match = this.qcBatchDetails.getSampleDataItemsForPeak(keyid1, emppeakName, paramtype, "*", StringUtil.split(sdiDataIdList, DELIMITER));
                if (match != null && match.getRowCount() > 0) {
                    paramlistid = match.getString(0, "paramlistid");
                    variantid = match.getString(0, "variantid");
                    currds = match.getValue(0, "dataset");
                    currversion = match.getString(0, "paramlistversionid");
                    dataitemadded = true;
                    paramid = match.getString(0, "paramid");
                    externalreference = externalreference + ";added=Y";
                    Trace.logDebug("Diagnostic::: Adding dataitem for paramlistid:" + paramlistid + " variantid:" + variantid + " version:" + currversion + " dataset" + currds + " paramid:" + paramid + "paramtype:" + paramtype);
                    this.addRow_Sample_AddDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, externalreference, dataType, doApplyDisplayFormat);
                } else {
                    Trace.logDebug("Diagnostic:::  Match not found");
                    if ((peakType.equals(this.empowerPolicyDef.getTranslate("Missing")) || peakType.equals(this.empowerPolicyDef.getTranslate("Found")) || peakType.equals(this.empowerPolicyDef.getTranslate("Group"))) && !this.empowerPolicyDef.saveUnmatchedComponents()) {
                        Trace.logDebug("Diagnostic::: Peak type is:" + peakType + " and !saveUnmatchedcompontents, returning without adding");
                        return;
                    }
                    String datasetkeyes = peaks.getString(currpeak, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"));
                    sdiDataIdList = this.getSDIDataIdList(datasetkeyes);
                    String primarykey = this.getPrimary(sdiDataIdList);
                    DataSet primary = this.qcBatchDetails.getSampleDataSetDetailsByKey(primarykey);
                    paramlistid = primary.getString(0, "paramlistid");
                    variantid = primary.getString(0, "variantid");
                    currds = primary.getValue(0, "dataset");
                    currversion = primary.getString(0, "paramlistversionid");
                    int replicatenum = new Integer(replicate);
                    for (int i = 0; i < replicatenum; ++i) {
                        if (i == 0) {
                            Trace.logDebug("Diagnostic::: Adding new Param:" + emppeakName);
                            this.addRow_Param_AddSDI_DS(emppeakName);
                        }
                        dataitemadded = true;
                        paramid = emppeakName;
                        externalreference = externalreference + ";added=Y";
                        Trace.logDebug("Diagnostic::: Adding dataitem for paramlistid:" + paramlistid + " variantid:" + variantid + " version:" + currversion + " dataset" + currds + " paramid:" + emppeakName + "paramtype:" + paramtype);
                        this.addRow_Sample_AddDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, emppeakName, paramtype, "" + (i + 1), externalreference, dataType, doApplyDisplayFormat);
                    }
                }
            } else {
                Trace.logDebug("Diagnostic::: replicate is 1");
                if ((peakType.equals(this.empowerPolicyDef.getTranslate("Missing")) || peakType.equals(this.empowerPolicyDef.getTranslate("Found")) || peakType.equals(this.empowerPolicyDef.getTranslate("Group"))) && !this.empowerPolicyDef.saveUnmatchedComponents()) {
                    Trace.logDebug("Diagnostic::: peak type is:" + peakType + " and !saveUnmatchedcompontents, returning without adding");
                    return;
                }
                String datasetkeyes = peaks.getString(currpeak, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"));
                sdiDataIdList = this.getSDIDataIdList(datasetkeyes);
                String primarykey = this.getPrimary(sdiDataIdList);
                DataSet primary = this.qcBatchDetails.getSampleDataSetDetailsByKey(primarykey);
                paramlistid = primary.getString(0, "paramlistid");
                variantid = primary.getString(0, "variantid");
                currds = primary.getValue(0, "dataset");
                currversion = primary.getString(0, "paramlistversionid");
                Trace.logDebug("Diagnostic::: Adding new Param:" + emppeakName);
                this.addRow_Param_AddSDI_DS(emppeakName);
                paramid = emppeakName;
                dataitemadded = true;
                Trace.logDebug("Diagnostic::: Adding dataitem for paramlistid:" + paramlistid + " variantid:" + variantid + " version:" + currversion + " dataset" + currds + " paramid:" + paramid + "paramtype:" + paramtype);
                this.addRow_Sample_AddDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, emppeakName, paramtype, replicate, externalreference + ";added=Y", dataType, doApplyDisplayFormat);
            }
        } else {
            Trace.logDebug("Diagnostic::: Found a matching dataitem so no need to add, ");
            paramlistid = match.getString(0, "paramlistid");
            variantid = match.getString(0, "variantid");
            currds = match.getValue(0, "dataset");
            currversion = match.getString(0, "paramlistversionid");
            paramid = match.getString(0, "paramid");
            Trace.logDebug("Diagnostic::: Found dataitem paramlistid:" + paramlistid + " variantid:" + variantid + " version:" + currversion + " dataset" + currds + " paramid:" + emppeakName + "paramtype:" + paramtype);
        }
        if (sapphirecol.equals("Result")) {
            Trace.logDebug("Diagnostic::: Sapphire column is Result");
            if (peakType.equals(this.empowerPolicyDef.getTranslate("Missing"))) {
                empowerColumnValue = this.empowerPolicyDef.getMissingPeakText();
            }
            Trace.logDebug("Diagnostic::: Doing predataentry check");
            if (dataitemadded || this.preDataEntryCheck(keyid1, paramlistid, variantid, currds, paramid, paramtype, replicate)) {
                Trace.logDebug("Diagnostic::: predataentrycheck passed for dataitem paramlistid:" + paramlistid + " variantid:" + variantid + " version:" + currversion + " dataset" + currds + " paramid:" + paramid + "paramtype:" + paramtype);
                this.addRow_Sample_EnterDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, empowerColumn, empowerColumnValue);
                if (dataitemadded) {
                    this.addRow_Sample_EditDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference + ";added=Y");
                } else {
                    this.addRow_Sample_EditDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference);
                }
                this.addRow_Sample_EditDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "resulttimeoffset", empowerColumn, retentionTime);
                if (this.analyst != null && this.analyst.length() > 0) {
                    this.addRow_Sample_EditDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, paramlistid, paramtype, replicate, "s_analystid", empowerColumn, this.analyst);
                }
                Trace.logDebug("Processing item:" + keyid1 + ":" + paramid + ":" + paramtype);
                return;
            } else {
                Trace.logDebug("Skipping item:" + keyid1 + ":" + paramid + ":" + paramtype);
            }
            return;
        } else {
            this.addRow_Sample_EditDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, sapphirecol, empowerColumn, empowerColumnValue);
            this.addRow_Sample_EditDataItem_DS(rule, "Peak", keyid1, paramlistid, currversion, variantid, currds, paramid, paramtype, replicate, "externalreference", empowerColumn, externalreference);
        }
    }

    private void process_Sample_EditSDI_DS(boolean newtransaction) throws SapphireException {
        String[] cols = this.Sample_EditSDI_DS.getColumns();
        if (cols.length == 0) {
            return;
        }
        PropertyList editProps = new PropertyList();
        editProps.setProperty("sdcid", "Sample");
        for (int i = 0; i < cols.length; ++i) {
            editProps.setProperty(cols[i], this.Sample_EditSDI_DS.getColumnValues(cols[i], DELIMITER));
        }
        this.processLog.println("Calling EditSDI Sample with: ");
        this.processLog.addTable(editProps);
        this.actionProcessor.processAction("EditSDI", "1", editProps, newtransaction);
    }

    private void process_Sample_AddDataSet_DS(boolean newtransaction) throws SapphireException {
        if (this.Sample_AddDataSet_DS.getRowCount() > 0) {
            PropertyList addDataSetProps = new PropertyList();
            addDataSetProps.setProperty("sdcid", "Sample");
            addDataSetProps.setProperty("keyid1", this.Sample_AddDataSet_DS.getColumnValues("keyid1", DELIMITER));
            addDataSetProps.setProperty("paramlistid", this.Sample_AddDataSet_DS.getColumnValues("paramlistid", DELIMITER));
            addDataSetProps.setProperty("paramlistversionid", this.Sample_AddDataSet_DS.getColumnValues("paramlistversionid", DELIMITER));
            addDataSetProps.setProperty("variantid", this.Sample_AddDataSet_DS.getColumnValues("variantid", DELIMITER));
            addDataSetProps.setProperty("propsmatch", "Y");
            this.processLog.println("Calling AddDataSet Sample with: ");
            this.processLog.addTable(addDataSetProps);
            this.actionProcessor.processAction("AddDataSet", "1", addDataSetProps, newtransaction);
        }
    }

    private void process_Sample_EditDataSet_DS(boolean newtransaction) throws SapphireException {
        if (this.Sample_EditDataSet_DS.getRowCount() > 0) {
            PropertyList editDataSetProps = new PropertyList();
            String[] cols = this.Sample_EditDataSet_DS.getColumns();
            editDataSetProps.setProperty("sdcid", "Sample");
            for (int i = 0; i < cols.length; ++i) {
                editDataSetProps.setProperty(cols[i], this.Sample_EditDataSet_DS.getColumnValues(cols[i], DELIMITER));
            }
            try {
                editDataSetProps.setProperty("sdcid", "Sample");
                editDataSetProps.setProperty("propsmatch", "Y");
                this.processLog.println("Calling EditDataSet Sample with: ");
                this.processLog.addTable(editDataSetProps);
                this.actionProcessor.processAction("EditDataSet", "1", editDataSetProps, newtransaction);
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to editDataSets.", e);
            }
        }
    }

    private void process_Sample_AddDataItem_DS(boolean newtransaction) throws SapphireException {
        if (this.Sample_AddDataItem_DS.getRowCount() > 0) {
            PropertyList addDataItemProps = new PropertyList();
            addDataItemProps.setProperty("sdcid", "Sample");
            addDataItemProps.setProperty("keyid1", this.Sample_AddDataItem_DS.getColumnValues("keyid1", DELIMITER));
            addDataItemProps.setProperty("paramlistid", this.Sample_AddDataItem_DS.getColumnValues("paramlistid", DELIMITER));
            addDataItemProps.setProperty("paramlistversionid", this.Sample_AddDataItem_DS.getColumnValues("paramlistversionid", DELIMITER));
            addDataItemProps.setProperty("variantid", this.Sample_AddDataItem_DS.getColumnValues("variantid", DELIMITER));
            addDataItemProps.setProperty("paramid", this.Sample_AddDataItem_DS.getColumnValues("paramid", DELIMITER));
            addDataItemProps.setProperty("paramtype", this.Sample_AddDataItem_DS.getColumnValues("paramtype", DELIMITER));
            addDataItemProps.setProperty("dataset", this.Sample_AddDataItem_DS.getColumnValues("dataset", DELIMITER));
            addDataItemProps.setProperty("propsmatch", "Y");
            addDataItemProps.setProperty("numreplicate", "1");
            addDataItemProps.setProperty("paramlistcheck", "N");
            addDataItemProps.setProperty("datatypes", this.Sample_AddDataItem_DS.getColumnValues("datatypes", DELIMITER));
            addDataItemProps.setProperty("displayformat", this.Sample_AddDataItem_DS.getColumnValues("displayformat", DELIMITER));
            try {
                this.processLog.println("Calling AddDataItem Sample with: ");
                this.processLog.addTable(addDataItemProps);
                this.actionProcessor.processAction("AddDataItem", "1", addDataItemProps, newtransaction);
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to AddDataItem as specified in the EmpowerUpload policy", e);
            }
        }
        if (this.Sample_AddReplicate_DS.getRowCount() > 0) {
            PropertyList replicateProps = new PropertyList();
            replicateProps.setProperty("sdcid", "Sample");
            replicateProps.setProperty("keyid1", this.Sample_AddReplicate_DS.getColumnValues("keyid1", DELIMITER));
            replicateProps.setProperty("paramlistid", this.Sample_AddReplicate_DS.getColumnValues("paramlistid", DELIMITER));
            replicateProps.setProperty("paramlistversionid", this.Sample_AddReplicate_DS.getColumnValues("paramlistversionid", DELIMITER));
            replicateProps.setProperty("variantid", this.Sample_AddReplicate_DS.getColumnValues("variantid", DELIMITER));
            replicateProps.setProperty("dataset", this.Sample_AddReplicate_DS.getColumnValues("dataset", DELIMITER));
            replicateProps.setProperty("paramid", this.Sample_AddReplicate_DS.getColumnValues("paramid", DELIMITER));
            replicateProps.setProperty("paramtype", this.Sample_AddReplicate_DS.getColumnValues("paramtype", DELIMITER));
            replicateProps.setProperty("numreplicate", this.Sample_AddReplicate_DS.getColumnValues("numreplicate", DELIMITER));
            replicateProps.setProperty("propsmatch", "Y");
            try {
                this.processLog.println("Calling AddReplicate Sample with: ");
                this.processLog.addTable(replicateProps);
                this.actionProcessor.processAction("AddReplicate", "1", replicateProps, newtransaction);
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to AddReplicate as specified in the EmpowerUpload policy", e);
            }
        }
    }

    private void process_Sample_EditDataItem_DS(boolean newtransaction) throws SapphireException {
        if (this.Sample_EditDataItem_DS != null && this.Sample_EditDataItem_DS.getRowCount() > 0) {
            PropertyList dataItemProps = new PropertyList();
            String[] cols = this.Sample_EditDataItem_DS.getColumns();
            for (int i = 0; i < cols.length; ++i) {
                dataItemProps.setProperty(cols[i], this.Sample_EditDataItem_DS.getColumnValues(cols[i], DELIMITER));
            }
            try {
                dataItemProps.setProperty("sdcid", "Sample");
                dataItemProps.setProperty("propsmatch", "Y");
                this.processLog.println("Calling EditDataItem Sample with: ");
                this.processLog.addTable(dataItemProps);
                this.actionProcessor.processAction("EditDataItem", "1", dataItemProps, newtransaction);
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to enterDataItem as specified in the EmpowerUpload policy", e);
            }
        }
    }

    private void process_Sample_EnterDataItem_DS(boolean newtransaction) throws SapphireException {
        PropertyList dataItemProps = new PropertyList();
        if (this.Sample_EnterDataItem_DS != null && this.Sample_EnterDataItem_DS.getRowCount() > 0) {
            dataItemProps.setProperty("sdcid", "Sample");
            dataItemProps.setProperty("keyid1", this.Sample_EnterDataItem_DS.getColumnValues("keyid1", DELIMITER));
            dataItemProps.setProperty("paramlistid", this.Sample_EnterDataItem_DS.getColumnValues("paramlistid", DELIMITER));
            dataItemProps.setProperty("paramlistversionid", this.Sample_EnterDataItem_DS.getColumnValues("paramlistversionid", DELIMITER));
            dataItemProps.setProperty("variantid", this.Sample_EnterDataItem_DS.getColumnValues("variantid", DELIMITER));
            dataItemProps.setProperty("paramid", this.Sample_EnterDataItem_DS.getColumnValues("paramid", DELIMITER));
            dataItemProps.setProperty("paramtype", this.Sample_EnterDataItem_DS.getColumnValues("paramtype", DELIMITER));
            dataItemProps.setProperty("dataset", this.Sample_EnterDataItem_DS.getColumnValues("dataset", DELIMITER));
            dataItemProps.setProperty("replicateid", this.Sample_EnterDataItem_DS.getColumnValues("replicateid", DELIMITER));
            dataItemProps.setProperty("enteredtext", this.Sample_EnterDataItem_DS.getColumnValues("enteredtext", DELIMITER));
            if (this.empowerPolicyDef.getHandlingReleasedResults().equals("Override")) {
                dataItemProps.setProperty("overridereleased", "Y");
            } else {
                dataItemProps.setProperty("overridereleased", "N");
            }
            dataItemProps.setProperty("propsmatch", "Y");
            if (this.empowerPolicyDef.autoReleaseResults()) {
                dataItemProps.setProperty("autorelease", "Y");
            }
            try {
                this.processLog.println("Calling EnterDataItem Sample with: ");
                this.processLog.addTable(dataItemProps);
                this.actionProcessor.processAction("EnterDataItem", "1", dataItemProps, newtransaction);
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to enterDataItem as specified in the EmpowerUpload policy", e);
            }
        }
    }

    private void process_Param_AddSDI_DS(boolean newtransaction) throws SapphireException {
        if (this.Param_AddSDI_DS.getRowCount() > 0) {
            PropertyList addOrIgnoreParamProps = new PropertyList();
            addOrIgnoreParamProps.setProperty("sdcid", "Param");
            addOrIgnoreParamProps.setProperty("keyid1", this.Param_AddSDI_DS.getColumnValues("keyid1", DELIMITER));
            addOrIgnoreParamProps.setProperty("ignoreedits", "Y");
            try {
                this.processLog.println("Calling AddSDI Param with: ");
                this.processLog.addTable(addOrIgnoreParamProps);
                this.actionProcessor.processAction("AddOrEditSDI", "1", addOrIgnoreParamProps, newtransaction);
                this.processLog.addOp("Sample DataItem Result/Column", "AddSDI", "Param", addOrIgnoreParamProps.getProperty("newkeyid1", "(empty)"), "", "Peak", "", "");
            }
            catch (SapphireException e) {
                throw new SapphireException("Failed to AddSDI for sdc Param  as specified in the EmpowerUpload policy", e);
            }
            this.Param_AddSDI_DS = new DataSet(this.connectionInfo);
        }
    }

    private static String adjustTimeZone(String value, String formattedvalue) {
        if (formattedvalue.length() > 0) {
            long longval = Long.parseLong(value);
            String t = formattedvalue.substring(formattedvalue.length() - 3);
            int msToAdjust = TimeZone.getTimeZone(t).getOffset(longval);
            long finalDate = longval - (long)msToAdjust;
            return "" + finalDate;
        }
        return value;
    }

    public static HashMap CreateDataSets(ConnectionInfo conn, PropertyList sampleSet, String sampleSetCols, String sampleSetLineCols, String resultCols, String peakCols, String limsidcol, String datasetkeyescol, String uploadFlagCol, EmpowerPolicyDef policyDef) throws SapphireException {
        int i;
        HashMap<String, String> message = new HashMap<String, String>();
        DataSet sampleSetDS = new DataSet(conn);
        sampleSetDS.setColidCaseSensitive(true);
        sampleSetDS.addRow();
        Trace.logDebug("SampleSet returned received on server side:" + sampleSet.toXMLString());
        String[] sampleSetColumns = StringUtil.split(sampleSetCols, DELIMITER);
        Trace.logDebug("Fetching sampleSetColumns:" + sampleSetCols);
        PropertyListCollection sampleSetFields = sampleSet.getCollectionNotNull("fields");
        for (i = 0; i < sampleSetColumns.length; ++i) {
            PropertyList currField = sampleSetFields.find("name", sampleSetColumns[i]);
            Trace.logDebug("Fetching field:" + sampleSetColumns[i]);
            if (currField != null) {
                Trace.logDebug("Found field:" + sampleSetColumns[i]);
                String colname = currField.getProperty("name");
                String type = currField.getProperty("type");
                String value = "";
                if (type.equals("date")) {
                    value = EmpowerUploadProcessor.adjustTimeZone(currField.getProperty("value"), currField.getProperty("formatvalue", ""));
                    sampleSetDS.addColumn(colname, 2);
                } else {
                    value = currField.getProperty("value");
                    sampleSetDS.addColumn(colname, 0);
                }
                sampleSetDS.setValue(0, colname, value);
                continue;
            }
            Trace.logDebug("Adding column field:" + sampleSetColumns[i]);
            sampleSetDS.addColumn(sampleSetColumns[i], 0);
            sampleSetDS.setValue(0, sampleSetColumns[i], "");
        }
        for (i = 0; i < sampleSetColumns.length; ++i) {
            if (sampleSetDS.isValidColumn(sampleSetColumns[i])) continue;
            Trace.logDebug("Adding values for column:" + sampleSetColumns[i]);
            sampleSetDS.addColumn(sampleSetColumns[i], 0);
            sampleSetDS.addColumnValues(sampleSetColumns[i], 0, StringUtil.repeat("", sampleSetDS.getRowCount(), DELIMITER), DELIMITER);
        }
        message.put("SampleSet", sampleSetDS.toXML());
        Trace.logDebug("Created sampleSet:" + sampleSetDS.toXML());
        PropertyListCollection results = sampleSet.getCollection("results");
        if (results == null || results.size() == 0) {
            throw new SapphireException("Results are empty.");
        }
        DataSet sampleSetLinesDS = EmpowerUploadProcessor.getSampleSetLinesFromResults(conn, results, limsidcol, datasetkeyescol, uploadFlagCol, sampleSetLineCols, policyDef);
        if (sampleSetLinesDS == null || sampleSetLinesDS.getRowCount() == 0) {
            throw new SapphireException("No results to upload.");
        }
        Trace.logDebug("Created sampleSetLines:" + sampleSetLinesDS.toXML());
        message.put("SampleSetLines", sampleSetLinesDS.toXML());
        DataSet resultsDS = EmpowerUploadProcessor.getResults(conn, results, limsidcol, datasetkeyescol, uploadFlagCol, resultCols, policyDef);
        if (resultsDS == null || resultsDS.getRowCount() == 0) {
            throw new SapphireException("No results to upload.");
        }
        Trace.logDebug("Created Results DS:" + resultsDS.toXML());
        message.put("Results", resultsDS.toXML());
        DataSet peaksDS = EmpowerUploadProcessor.getPeaks(conn, results, resultsDS, limsidcol, datasetkeyescol, uploadFlagCol, peakCols, policyDef);
        if (peaksDS == null || peaksDS.getRowCount() == 0) {
            throw new SapphireException("No peaks to upload.");
        }
        message.put("Peaks", peaksDS.toXML());
        Trace.logDebug("Created peaks:" + peaksDS.toXML());
        return message;
    }

    private static DataSet getSampleSetLinesFromResults(ConnectionInfo conn, PropertyListCollection results, String limsidcol, String datasetkeyescol, String uploadFlagCol, String sampleSetLineColumns, EmpowerPolicyDef policyDef) throws SapphireException {
        DataSet sampleSetLinesInfo = EmpowerUploadProcessor.getResults(conn, results, limsidcol, datasetkeyescol, uploadFlagCol, sampleSetLineColumns + DELIMITER + policyDef.getTranslate("Injection") + DELIMITER + policyDef.getTranslate("DateAcquired"), policyDef);
        if (sampleSetLinesInfo == null || sampleSetLinesInfo.getRowCount() == 0) {
            throw new SapphireException("No SampleSetLines found to upload");
        }
        HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
        filter.put(policyDef.getTranslate("Injection"), new BigDecimal("1"));
        return sampleSetLinesInfo.getFilteredDataSet(filter);
    }

    private static DataSet getResults(ConnectionInfo conn, PropertyListCollection results, String limsidcol, String datasetkeycol, String uploadFlagCol, String resultsCols, EmpowerPolicyDef policyDef) {
        DataSet resultsDS = new DataSet(conn);
        resultsDS.setColidCaseSensitive(true);
        resultsDS.addColumn(limsidcol, 0);
        resultsDS.addColumn(datasetkeycol, 0);
        resultsDS.addColumn("Sample Position", 0);
        for (int i = 0; i < results.size(); ++i) {
            PropertyList currResult = results.getPropertyList(i);
            PropertyListCollection currResultFields = currResult.getCollection("fields");
            PropertyList sampleIdField = currResultFields.find("name", limsidcol);
            PropertyList uploadFlagField = currResultFields.find("name", uploadFlagCol);
            if (sampleIdField != null) {
                if (uploadFlagField != null) {
                    if ("0".equals(uploadFlagField.getProperty("value", "0"))) {
                        Trace.logDebug("Upload flag value is 0 for result " + sampleIdField);
                        continue;
                    }
                    if (uploadFlagField.getProperty("value").toUpperCase().charAt(0) == 'N') {
                        Trace.logDebug("Upload flag value is N for result " + sampleIdField);
                        continue;
                    }
                }
                int row = resultsDS.addRow();
                String[] columns = StringUtil.split(resultsCols, DELIMITER);
                for (int j = 0; j < columns.length; ++j) {
                    PropertyList matchField = currResultFields.find("name", columns[j]);
                    if (matchField != null) {
                        String type;
                        String val = matchField.getProperty("enumvalue", "");
                        if (val.length() == 0) {
                            val = matchField.getProperty("value", "");
                        }
                        if ((type = matchField.getProperty("type", "")).equals("number")) {
                            BigDecimal bVal;
                            if (!resultsDS.isValidColumn(columns[j])) {
                                resultsDS.addColumn(columns[j], 1);
                            }
                            if (val.length() <= 0) continue;
                            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
                            DecimalFormat df = (DecimalFormat)nf;
                            df.setGroupingUsed(false);
                            df.setMaximumFractionDigits(100);
                            df.setMinimumFractionDigits(0);
                            df.setParseBigDecimal(true);
                            try {
                                bVal = (BigDecimal)df.parse(val);
                            }
                            catch (Exception e) {
                                bVal = new BigDecimal(0);
                            }
                            if (resultsDS.getColumnType(columns[j]) == 1) {
                                resultsDS.setNumber(row, matchField.getProperty("name", ""), bVal);
                                continue;
                            }
                            resultsDS.setValue(row, matchField.getProperty("name", ""), bVal.toString());
                            continue;
                        }
                        if (type.equals("date")) {
                            if (!resultsDS.isValidColumn(columns[j])) {
                                resultsDS.addColumn(columns[j], 2);
                            }
                            if (val.length() <= 0) continue;
                            val = EmpowerUploadProcessor.adjustTimeZone(matchField.getProperty("value"), matchField.getProperty("formatvalue", ""));
                            resultsDS.setValue(row, columns[j], val);
                            continue;
                        }
                        if (!resultsDS.isValidColumn(columns[j])) {
                            resultsDS.addColumn(columns[j], 0);
                        }
                        resultsDS.setString(row, matchField.getProperty("name", ""), val);
                        continue;
                    }
                    resultsDS.setValue(row, columns[j], "");
                }
                continue;
            }
            Trace.logDebug("Result row " + i + "has Sampleid column empty");
        }
        resultsDS.sort(policyDef.getTranslate("DateAcquired"));
        String prevdskeys = "";
        String prevDate = "";
        int position = 0;
        for (int i = 0; i < resultsDS.getRowCount(); ++i) {
            String dskeys = resultsDS.getString(i, datasetkeycol, "");
            String date = resultsDS.getValue(i, "DateAcquired");
            if (dskeys.length() > 0) {
                if (!dskeys.equals(prevdskeys)) {
                    ++position;
                }
                resultsDS.setString(i, policyDef.getTranslate("Sample Position"), position + "");
            }
            prevdskeys = dskeys;
            prevDate = date;
        }
        return resultsDS;
    }

    private static DataSet getPeaks(ConnectionInfo connectionInfo, PropertyListCollection results, DataSet resultsDS1, String limsidcol, String datasetKeyesCol, String uploadFlagCol, String peaksCols, EmpowerPolicyDef policyDef) throws SapphireException {
        if (results == null) {
            throw new SapphireException("results collection is null");
        }
        DataSet peaksDS = new DataSet(connectionInfo);
        peaksDS.setColidCaseSensitive(true);
        peaksDS.addColumn(limsidcol, 0);
        peaksDS.addColumn(datasetKeyesCol, 0);
        peaksDS.addColumn(policyDef.getTranslate("Sample Position"), 0);
        peaksDS.addColumn(datasetKeyesCol, 0);
        peaksDS.addColumn(policyDef.getTranslate("ChannelName"), 0);
        Trace.logDebug("Results size in getPeaks is:" + results.size());
        for (int resultid = 0; resultid < results.size(); ++resultid) {
            PropertyList currResult = results.getPropertyList(resultid);
            PropertyListCollection currResultFields = currResult.getCollection("fields");
            PropertyList sampleIdField = currResultFields.find("name", limsidcol);
            if (sampleIdField == null) {
                Trace.logDebug(limsidcol + " field not found in results.");
                continue;
            }
            PropertyList datasetKeyesField = currResultFields.find("name", datasetKeyesCol);
            if (datasetKeyesField == null) {
                throw new SapphireException(datasetKeyesCol + " field not found in results.");
            }
            PropertyList injectionField = currResultFields.find("name", policyDef.getTranslate("Injection"));
            if (injectionField == null) {
                throw new SapphireException(policyDef.getTranslate("Injection") + " field not found in results.");
            }
            PropertyList channelField = currResultFields.find("name", policyDef.getTranslate("ChannelName"));
            if (channelField == null) {
                throw new SapphireException("ChannelName field not found in results.");
            }
            PropertyList resultIdField = currResultFields.find("name", policyDef.getTranslate("ResultId"));
            PropertyList resultSetIdField = currResultFields.find("name", policyDef.getTranslate("ResultSetId"));
            PropertyList uploadFlagField = currResultFields.find("name", uploadFlagCol);
            if (uploadFlagField == null) {
                throw new SapphireException(uploadFlagCol + " field not found in results.");
            }
            String currSampleId = sampleIdField.getProperty("value", "");
            if (currSampleId == null || currSampleId.length() == 0) continue;
            String uploadFlag = "0";
            if (uploadFlagField != null) {
                uploadFlag = uploadFlagField.getProperty("value", "0");
            }
            if (uploadFlag.equals("0") || uploadFlagField.getProperty("value").toUpperCase().charAt(0) == 'N') {
                Trace.logDebug("Upload flag value is N for peak ");
                continue;
            }
            PropertyListCollection peaks = currResult.getCollection("peaks");
            if (peaks == null || peaks.size() == 0) {
                throw new SapphireException("Peaks collection is empty");
            }
            for (int peakid = 0; peakid < peaks.size(); ++peakid) {
                PropertyList currPeak = peaks.getPropertyList(peakid);
                PropertyListCollection currPeakFields = currPeak.getCollection("fields");
                int row = peaksDS.addRow();
                peaksDS.setString(row, limsidcol, currSampleId);
                HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
                String resultidval = resultIdField.getProperty("value");
                if (resultidval == null) continue;
                filter.put(policyDef.getTranslate("ResultId"), new BigDecimal(resultidval));
                DataSet match = resultsDS1.getFilteredDataSet(filter);
                peaksDS.setString(row, policyDef.getTranslate("Sample Position"), match.getString(0, policyDef.getTranslate("Sample Position")));
                peaksDS.setString(row, datasetKeyesCol, datasetKeyesField.getProperty("value", ""));
                peaksDS.setString(row, policyDef.getTranslate("ResultId"), resultIdField.getProperty("value", ""));
                if (resultSetIdField != null) {
                    peaksDS.setString(row, policyDef.getTranslate("ResultSetId"), resultSetIdField.getProperty("value", ""));
                } else {
                    peaksDS.setString(row, policyDef.getTranslate("ResultSetId"), "NONE");
                }
                peaksDS.setString(row, policyDef.getTranslate("ChannelName"), channelField.getProperty("value", ""));
                peaksDS.setString(row, policyDef.getTranslate("Injection"), injectionField.getProperty("value", ""));
                String[] columns = StringUtil.split(peaksCols, DELIMITER);
                for (int j = 0; j < columns.length; ++j) {
                    PropertyList matchField = currPeakFields.find("name", columns[j]);
                    if (matchField != null) {
                        String val = matchField.getProperty("enumvalue", "");
                        if (val.length() == 0) {
                            val = matchField.getProperty("value", "");
                        }
                        if (!peaksDS.isValidColumn(columns[j])) {
                            peaksDS.addColumn(columns[j], 0);
                        }
                        peaksDS.setString(row, matchField.getProperty("name", ""), val);
                        continue;
                    }
                    peaksDS.setString(row, columns[j], "");
                }
            }
        }
        return peaksDS;
    }

    public void syncQCBatch(ActionProcessor actionProcessor, QueryProcessor queryProcessor, String qcBatchId, DataSet sampleSetLines) throws SapphireException {
        int i;
        String connectionId = actionProcessor.getConnectionid();
        DataSet dsQCBatch = queryProcessor.getPreparedSqlDataSet("SELECT qcbatchsdcid FROM s_qcbatch WHERE s_qcbatchid = ?", new Object[]{qcBatchId});
        DataSet dsBatchItems = queryProcessor.getPreparedSqlDataSet("SELECT bi.s_qcbatchitemid, bi.usersequence, sd.keyid1, sd.sdidataid  FROM s_qcbatchitem bi, sdidata sd WHERE sd.s_qcbatchid = bi.s_qcbatchid AND sd.s_qcbatchitemid = bi.s_qcbatchitemid AND bi.s_qcbatchid = ?", new Object[]{qcBatchId});
        SequenceProcessor sequenceProcessor = new SequenceProcessor(connectionId);
        String[] colArray = sampleSetLines.getColumns();
        DataSet editQCBatchItems = new DataSet(this.connectionInfo);
        PropertyList actionProps = new PropertyList();
        HashMap<String, String> filterMap = new HashMap<String, String>();
        for (i = 0; i < sampleSetLines.getRowCount(); ++i) {
            String limsSampleId = sampleSetLines.getString(i, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"), "");
            if (limsSampleId.length() == 0) continue;
            String datasetkeys = sampleSetLines.getString(i, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey"), "");
            String sdidataIds = this.getSDIDataIdList(datasetkeys);
            String userSequence = sampleSetLines.getString(i, this.empowerPolicyDef.getTranslate("Sample Position"), "" + (i + 1));
            filterMap.clear();
            filterMap.put("keyid1", limsSampleId);
            DataSet filterBatchItems = dsBatchItems.getFilteredDataSet(filterMap);
            if (filterBatchItems == null || filterBatchItems.getRowCount() == 0) {
                throw new SapphireException("No batchitems found for Sample " + limsSampleId);
            }
            String[] datasetKeys = sdidataIds.split(DELIMITER);
            int findRow = -1;
            block1: for (int k = 0; k < filterBatchItems.getRowCount(); ++k) {
                for (int j = 0; j < datasetKeys.length; ++j) {
                    findRow = filterBatchItems.findRow("sdidataid", datasetKeys[j]);
                    if (findRow <= -1) continue;
                    int oldSequence = filterBatchItems.getInt(findRow, "usersequence");
                    String qcBatchItemId = filterBatchItems.getString(findRow, "s_qcbatchitemid");
                    int r = editQCBatchItems.addRow();
                    editQCBatchItems.setString(r, "qcbatchid", qcBatchId);
                    editQCBatchItems.setString(r, "qcbatchitemid", qcBatchItemId);
                    editQCBatchItems.setString(r, "usersequence", "" + userSequence);
                    continue block1;
                }
            }
        }
        if (editQCBatchItems.getRowCount() > 0) {
            actionProps.clear();
            actionProps.setProperty("sdcid", "QCBatch");
            actionProps.setProperty("linkid", "QCBatchItem");
            colArray = editQCBatchItems.getColumns();
            for (i = 0; i < colArray.length; ++i) {
                if (colArray[i].startsWith("empower")) continue;
                actionProps.setProperty(colArray[i], editQCBatchItems.getColumnValues(colArray[i], DELIMITER));
            }
            actionProps.setProperty("s_qcbatchid", editQCBatchItems.getColumnValues("qcbatchid", DELIMITER));
            actionProps.setProperty("s_qcbatchitemid", editQCBatchItems.getColumnValues("qcbatchitemid", DELIMITER));
            actionProps.setProperty("usersequence", editQCBatchItems.getColumnValues("usersequence", DELIMITER));
            actionProcessor.processAction("EditSDIDetail", "1", actionProps);
        }
        StringBuffer deleteBatchItems = new StringBuffer();
        ArrayList<String> deleteBatchItemsLst = new ArrayList<String>();
        for (int i2 = 0; i2 < dsBatchItems.getRowCount(); ++i2) {
            String keyId1 = dsBatchItems.getString(i2, "keyid1");
            String batchItemId = dsBatchItems.getString(i2, "s_qcbatchitemid");
            if (deleteBatchItemsLst.contains(batchItemId)) continue;
            if (sampleSetLines.findRow("empowerlimssampleid", keyId1) < 0) {
                deleteBatchItemsLst.add(batchItemId);
                deleteBatchItems.append(DELIMITER).append(batchItemId);
                continue;
            }
            filterMap.clear();
            filterMap.put("s_qcbatchitemid", batchItemId);
            DataSet dsBI = dsBatchItems.getFilteredDataSet(filterMap);
            String[] datasetIds = dsBI.getColumnValues("sdidataid", DELIMITER).split(DELIMITER);
            filterMap.clear();
            filterMap.put("empowerlimssampleid", keyId1);
            boolean deleteBatchItem = true;
            DataSet ds = sampleSetLines.getFilteredDataSet(filterMap);
            block5: for (int k = 0; k < ds.getRowCount(); ++k) {
                String datasetkeys = ds.getString(k, "empowerlimsdatasetkey", "");
                String sdiDataIdList = this.getSDIDataIdList(datasetkeys);
                String[] sdidataIds = sdiDataIdList.split(DELIMITER);
                for (int d = 0; d < sdidataIds.length; ++d) {
                    if (!datasetIds[0].equals(sdidataIds[d])) continue;
                    deleteBatchItem = false;
                    continue block5;
                }
            }
            if (!deleteBatchItem) continue;
            deleteBatchItemsLst.add(batchItemId);
            deleteBatchItems.append(DELIMITER).append(batchItemId);
        }
        if (deleteBatchItemsLst.size() > 0) {
            actionProps.clear();
            actionProps.setProperty("linkid", "QCBatchItem");
            actionProps.setProperty("sdcid", "QCBatch");
            actionProps.setProperty("keyid1", qcBatchId);
            actionProps.setProperty("s_qcbatchitemid", deleteBatchItems.substring(1));
            actionProcessor.processAction("DeleteSDIDetail", "1", actionProps);
        }
    }

    private void createQCBatchItems(ActionProcessor ap, QueryProcessor qp, String qcBatchId, DataSet qcBatchItems, String qcBatchSDCId) throws SapphireException {
        String connectionId = ap.getConnectionid();
        TranslationProcessor tp = new TranslationProcessor(connectionId);
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", "QCBatch");
        actionProps.setProperty("linkid", "QCBatchItem");
        String[] colArray = qcBatchItems.getColumns();
        for (int i = 0; i < colArray.length; ++i) {
            if (colArray[i].startsWith("empower")) continue;
            actionProps.setProperty(colArray[i], qcBatchItems.getColumnValues(colArray[i], DELIMITER));
        }
        actionProps.setProperty("s_qcbatchid", qcBatchItems.getColumnValues("qcbatchid", DELIMITER));
        actionProps.setProperty("s_qcbatchitemid", qcBatchItems.getColumnValues("qcbatchitemid", DELIMITER));
        actionProps.setProperty("usersequence", qcBatchItems.getColumnValues("usersequence", DELIMITER));
        ap.processAction("AddSDIDetail", "1", actionProps);
        String keyIds = qcBatchItems.getColumnValues("empowerlimssampleid", DELIMITER);
        DAMProcessor damProcessor = new DAMProcessor(connectionId);
        String rsetid = damProcessor.createRSet(qcBatchSDCId, keyIds, null, null);
        if (StringUtil.getLen(rsetid) > 0L) {
            DataSet ds = qp.getPreparedSqlDataSet("SELECT ds.keyid1, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset, ds.sdidataid  FROM sdidata ds, rsetitems rs  WHERE rs.sdcid = ds.sdcid AND rs.keyid1 = ds.keyid1 AND rs.rsetid = ?", new Object[]{rsetid});
            damProcessor.clearRSet(rsetid);
            DataSet dsEditDataSets = new DataSet(this.connectionInfo);
            for (int i = 0; i < qcBatchItems.getRowCount(); ++i) {
                String limsKeyId = qcBatchItems.getString(i, "empowerlimssampleid");
                String datasetkeys = qcBatchItems.getString(i, "empowerlimsdatasetkey");
                String sdiDataIdList = this.getSDIDataIdList(datasetkeys);
                String[] datasetIds = sdiDataIdList.split(DELIMITER);
                String qcbatchItemId = qcBatchItems.getString(i, "qcbatchitemid");
                for (int k = 0; k < datasetIds.length; ++k) {
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("keyid1", limsKeyId);
                    findMap.put("sdidataid", datasetIds[k]);
                    int findRow = ds.findRow(findMap);
                    if (findRow <= -1) continue;
                    ds.setString(findRow, "s_qcbatchid", qcBatchId);
                    ds.setString(findRow, "s_qcbatchitemid", qcbatchItemId);
                    dsEditDataSets.copyRow(ds, findRow, 1);
                }
            }
            if (dsEditDataSets.getRowCount() > 0) {
                actionProps.clear();
                actionProps.setProperty("sdcid", qcBatchSDCId);
                actionProps.setProperty("keyid1", dsEditDataSets.getColumnValues("keyid1", DELIMITER));
                actionProps.setProperty("paramlistid", dsEditDataSets.getColumnValues("paramlistid", DELIMITER));
                actionProps.setProperty("paramlistversionid", dsEditDataSets.getColumnValues("paramlistversionid", DELIMITER));
                actionProps.setProperty("variantid", dsEditDataSets.getColumnValues("variantid", DELIMITER));
                actionProps.setProperty("dataset", dsEditDataSets.getColumnValues("dataset", DELIMITER));
                actionProps.setProperty("s_qcbatchid", dsEditDataSets.getColumnValues("s_qcbatchid", DELIMITER));
                actionProps.setProperty("s_qcbatchitemid", dsEditDataSets.getColumnValues("s_qcbatchitemid", DELIMITER));
                actionProps.setProperty("propsmatch", "Y");
                ap.processAction("EditDataSet", "1", actionProps);
            }
        } else {
            throw new SapphireException(tp.translate("QCBatch creation failed. Unable to create RSET for") + " " + keyIds);
        }
    }

    private String postSaveStatusUpdates(String qcBatchId) throws SapphireException {
        PropertyList props;
        ActionBlock actionBlock = new ActionBlock();
        if (this.QCBatch_EnterDataItem_DS.getRowCount() > 0) {
            props = new PropertyList();
            props.setProperty("sdcid", "QCBatch");
            props.setProperty("keyid1", qcBatchId);
            props.setProperty("keyid2", "(null)");
            props.setProperty("keyid3", "(null)");
            props.setProperty("paramlistid", this.qcBatchDetails.getQCBatchDataSets().getColumnValues("paramlistid", DELIMITER));
            props.setProperty("paramlistversionid", this.qcBatchDetails.getQCBatchDataSets().getColumnValues("paramlistversionid", DELIMITER));
            props.setProperty("variantid", this.qcBatchDetails.getQCBatchDataSets().getColumnValues("variantid", DELIMITER));
            props.setProperty("dataset", this.qcBatchDetails.getQCBatchDataSets().getColumnValues("dataset", DELIMITER));
            props.setProperty("auditactivity", "Empower Upload");
            props.setProperty("auditreason", "Empower Upload");
            actionBlock.setAction("QCBatch-UpdateDataSetStatus", "UpdateDataSetStatus", "1", props);
            props = new PropertyList();
            props.setProperty("sdcid", "QCBatch");
            props.setProperty("keyid1", qcBatchId);
            props.setProperty("auditactivity", "Empower Upload");
            props.setProperty("saveandrelease", this.empowerPolicyDef.autoReleaseResults() ? "Y" : "N");
            actionBlock.setAction("QCBatch-ProcessQCBatch", "ProcessQCBatch", "1", props);
        }
        if (this.Sample_EnterDataItem_DS.getRowCount() > 0) {
            String itemswithresults = this.qcBatchDetails.getSampleDataSets().getColumnValues("keyid1", DELIMITER);
            PropertyList props2 = new PropertyList();
            props2.setProperty("sdcid", "Sample");
            props2.setProperty("keyid1", itemswithresults);
            props2.setProperty("keyid2", "(null)");
            props2.setProperty("keyid3", "(null)");
            props2.setProperty("paramlistid", this.qcBatchDetails.getSampleDataSets().getColumnValues("paramlistid", DELIMITER));
            props2.setProperty("paramlistversionid", this.qcBatchDetails.getSampleDataSets().getColumnValues("paramlistversionid", DELIMITER));
            props2.setProperty("variantid", this.qcBatchDetails.getSampleDataSets().getColumnValues("variantid", DELIMITER));
            props2.setProperty("dataset", this.qcBatchDetails.getSampleDataSets().getColumnValues("dataset", DELIMITER));
            props2.setProperty("auditactivity", "Empower Upload");
            actionBlock.setAction("Sample-UpdateDataSetStatus", "UpdateDataSetStatus", "1", props2);
            props2 = new PropertyList();
            props2.setProperty("sdcid", "Sample");
            props2.setProperty("keyid1", itemswithresults);
            props2.setProperty("keyid2", "(null)");
            props2.setProperty("keyid3", "(null)");
            actionBlock.setAction("Sample-SyncSDIWIStatus", "SyncSDIWIStatus", "1", props2);
            props2 = new PropertyList();
            props2.setProperty("sdcid", "Sample");
            props2.setProperty("keyid1", itemswithresults);
            props2.setProperty("keyid2", "(null)");
            props2.setProperty("keyid3", "(null)");
            props2.setProperty("paramlistid", this.qcBatchDetails.getSampleDataSets().getColumnValues("paramlistid", DELIMITER));
            props2.setProperty("paramlistversionid", this.qcBatchDetails.getSampleDataSets().getColumnValues("paramlistversionid", DELIMITER));
            props2.setProperty("variantid", this.qcBatchDetails.getSampleDataSets().getColumnValues("variantid", DELIMITER));
            props2.setProperty("dataset", this.qcBatchDetails.getSampleDataSets().getColumnValues("dataset", DELIMITER));
            props2.setProperty("auditactivity", "Empower Upload");
            actionBlock.setAction("Sample-SyncSDIDataSetStatus", "SyncSDIDataSetStatus", "1", props2);
            props2 = new PropertyList();
            props2.setProperty("sdcid", "Sample");
            props2.setProperty("keyid1", itemswithresults);
            props2.setProperty("paramlistid", this.qcBatchDetails.getSampleDataSets().getColumnValues("paramlistid", DELIMITER));
            props2.setProperty("paramlistversionid", this.qcBatchDetails.getSampleDataSets().getColumnValues("paramlistversionid", DELIMITER));
            props2.setProperty("variantid", this.qcBatchDetails.getSampleDataSets().getColumnValues("variantid", DELIMITER));
            props2.setProperty("dataset", this.qcBatchDetails.getSampleDataSets().getColumnValues("dataset", DELIMITER));
            props2.setProperty("auditactivity", "Empower Upload");
            props2.setProperty("saveandrelease", this.empowerPolicyDef.autoReleaseResults() ? "Y" : "N");
            actionBlock.setAction("ProcessQCBatch", "ProcessQCBatch", "1", props2);
        }
        props = new PropertyList();
        props.setProperty("sdcid", "QCBatch");
        props.setProperty("keyid1", qcBatchId);
        props.setProperty("auditactivity", "Empower Upload");
        props.setProperty("auditreason", "Empower Upload Action Invoked");
        actionBlock.setAction("UpdateQCBatchStatus", "UpdateQCBatchStatus", "1", props);
        this.actionProcessor.processActionBlock(actionBlock);
        String sql = "SELECT qcbatchstatus FROM s_qcbatch WHERE s_qcbatchid = ?";
        DataSet ret = this.queryProcessor.getPreparedSqlDataSet(sql, new Object[]{qcBatchId});
        if (ret.getRowCount() == 0) {
            throw new SapphireException("Failed to get the updated status of the QCBatch.");
        }
        return ret.getString(0, "qcbatchstatus");
    }

    public static HashMap createEmpowerMessageDS(ConfigurationProcessor cp, ConnectionProcessor connectionProcessor, String connectionid, String policynode, PropertyList selectedData) throws SapphireException {
        PropertyList policy = cp.getPolicy("EmpowerPolicy", policynode);
        if (policy == null) {
            throw new SapphireException("Failed to get policy");
        }
        EmpowerPolicyDef policyDef = new EmpowerPolicyDef(policy);
        String sampleSetCols = policyDef.getUploadSampleSetColumns();
        String sampleSetLineCols = policyDef.getUploadSampleSetLineColumns();
        String resultCols = policyDef.getUploadResultColumns();
        String peakCols = policyDef.getUploadPeakColumns();
        String limsidcol = policyDef.getEmpowerCoreMapping("empowerlimssampleid");
        String limsdatasetkeyescol = policyDef.getEmpowerCoreMapping("empowerlimsdatasetkey");
        String uploadflagcol = policyDef.getEmpowerCoreMapping("empoweruploadflag");
        ConnectionInfo conn = connectionProcessor.getConnectionInfo(connectionid);
        Trace.logDebug("Calling createDataSets:" + selectedData.toXMLString());
        HashMap messageMap = EmpowerUploadProcessor.CreateDataSets(conn, selectedData, sampleSetCols, sampleSetLineCols, resultCols, peakCols, limsidcol, limsdatasetkeyescol, uploadflagcol, policyDef);
        return messageMap;
    }

    private DataSet sanitizePeaks(DataSet peaks) {
        Trace.logDebug("DIAGNOSTIC::: Before sanitize peaks dataset is:" + peaks.toXML());
        DataSet ret = new DataSet(this.connectionInfo);
        ret.setColidCaseSensitive(peaks.getColidCaseSensitive());
        for (int i = 0; i < peaks.getRowCount(); ++i) {
            String currPeakType = peaks.getValue(i, "PeakType".toLowerCase(), "");
            if (currPeakType.length() > 0) {
                Trace.logDebug("PeakType has a value and will be processed:" + currPeakType);
                ret.copyRow(peaks, i, 1);
                continue;
            }
            Trace.logDebug("DIAGNOSTIC::: PeakType is empty skip peaks row:" + i);
        }
        Trace.logDebug("DIAGNOSTIC::: returning updated peaks:" + ret.toXML());
        return ret;
    }
}

