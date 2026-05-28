/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import com.labvantage.sapphire.modules.empower.QCBatchDetails;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class EmpowerDownloadProcessor
implements Serializable {
    private QueryProcessor queryProcessor;
    private EmpowerPolicyDef empowerPolicyDef;
    private DataSet sampleSetMethod;
    private DataSet sampleSetLines;
    private DataSet components;
    private DataSet qcBatchSampleTypes;
    private DataSet reagentLotLines;
    private DataSet reagentLotComponents;
    private String limssampleidcol = "";
    private String limsdatasetkeyidcol = "";
    private String limsqcbatchidcol = "";
    private ConnectionInfo connectionInfo;
    public static final String AQC_MODE = "AQC Mode";
    public static final String NON_AQC_MODE = "Non AQC Mode";
    public static final String DELIMITER = ";";

    public EmpowerDownloadProcessor(EmpowerPolicyDef def, ActionProcessor actionProcessor, QueryProcessor queryProcessor, ConnectionProcessor connectionProcessor) throws SapphireException {
        this.empowerPolicyDef = def;
        this.queryProcessor = queryProcessor;
        this.limssampleidcol = this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid");
        this.limsdatasetkeyidcol = this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey");
        this.limsqcbatchidcol = this.empowerPolicyDef.getEmpowerCoreMapping("empowerqcbatchid");
        String connectionid = connectionProcessor.getConnectionid();
        this.connectionInfo = connectionProcessor.getConnectionInfo(connectionid);
    }

    public void processCandidateSamples(DataSet sdiworkitemssummary) throws SapphireException {
        if (sdiworkitemssummary.getRowCount() == 0) {
            throw new SapphireException("No sdiworkitems specified");
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT * from s_sample where s_sampleid IN ( " + safeSQL.addIn(sdiworkitemssummary.getColumnValues("keyid1", DELIMITER), DELIMITER) + " )";
        DataSet samples = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        String sdidataInClause = "";
        for (int i = 0; i < sdiworkitemssummary.getRowCount(); ++i) {
            sdiworkitemssummary.setString(i, "Position", "" + (i + 1));
            String currDataSetKeyes = sdiworkitemssummary.getString(i, "datasetkeyes");
            String oldkeylist = EmpowerDownloadProcessor.getSDIDataIdListAdhocMode(this.queryProcessor, currDataSetKeyes);
            String[] sdidataids = StringUtil.split(oldkeylist, DELIMITER);
            for (int j = 0; j < sdidataids.length; ++j) {
                if (sdidataInClause.length() > 0) {
                    sdidataInClause = sdidataInClause + DELIMITER;
                }
                sdidataInClause = sdidataInClause + "" + sdidataids[j] + "";
            }
        }
        safeSQL.reset();
        DataSet sdidata = this.queryProcessor.getPreparedSqlDataSet("SELECT * from sdidata where sdidataid in ( " + safeSQL.addIn(sdidataInClause, DELIMITER) + " )", safeSQL.getValues());
        DataSet sdidataitems = this.getSDIDataItems(sdidata);
        this.sampleSetLines = this.fetchCandidateSampleSetLine(sdiworkitemssummary, samples, sdidataitems);
        this.components = this.fetchCandidateSampleComponents(sdiworkitemssummary, samples, sdidata, sdidataitems);
    }

    private DataSet getQCBatchItemDataItems(String[] sdidataid, DataSet allDataSets, DataSet allDataItems, String paramtype) {
        DataSet matchSdiData = new DataSet(this.connectionInfo);
        for (int i = 0; i < sdidataid.length; ++i) {
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sdidataid", sdidataid[i]);
            if (matchSdiData.getRowCount() == 0) {
                matchSdiData = allDataSets.getFilteredDataSet(filter);
                continue;
            }
            matchSdiData.copyRow(allDataSets.getFilteredDataSet(filter), -1, 1);
        }
        DataSet matchDataItems = new DataSet(this.connectionInfo);
        for (int i = 0; i < matchSdiData.getRowCount(); ++i) {
            HashMap<String, Object> filter = new HashMap<String, Object>();
            filter.put("sdcid", matchSdiData.getString(i, "sdcid"));
            filter.put("keyid1", matchSdiData.getString(i, "keyid1"));
            filter.put("paramlistid", matchSdiData.getString(i, "paramlistid"));
            filter.put("paramlistversionid", matchSdiData.getString(i, "paramlistversionid"));
            filter.put("variantid", matchSdiData.getString(i, "variantid"));
            filter.put("dataset", matchSdiData.getBigDecimal(i, "dataset"));
            filter.put("paramtype", paramtype);
            DataSet currdataitems = allDataItems.getFilteredDataSet(filter);
            for (int j = 0; j < currdataitems.getRowCount(); ++j) {
                currdataitems.setString(j, "sdidataid", matchSdiData.getString(i, "sdidataid"));
            }
            matchDataItems.copyRow(currdataitems, -1, 1);
        }
        return matchDataItems;
    }

    private DataSet getSDIDataItems(DataSet sdidata) throws SapphireException {
        DataSet sdidataitems = new DataSet(this.connectionInfo);
        for (int i = 0; i < sdidata.getRowCount(); ++i) {
            String sql = "SELECT * FROM sdidataitem WHERE sdcid = ? and  keyid1 = ? and  paramlistid = ? and  paramlistversionid = ? and  variantid = ? and  dataset = ?";
            DataSet currdataitems = this.queryProcessor.getPreparedSqlDataSet(sql, new Object[]{sdidata.getString(i, "sdcid"), sdidata.getString(i, "keyid1"), sdidata.getString(i, "paramlistid"), sdidata.getString(i, "paramlistversionid"), sdidata.getString(i, "variantid"), sdidata.getValue(i, "dataset")});
            for (int j = 0; j < currdataitems.getRowCount(); ++j) {
                currdataitems.setString(j, "sdidataid", sdidata.getString(i, "sdidataid"));
            }
            sdidataitems.copyRow(currdataitems, -1, 1);
        }
        return sdidataitems;
    }

    public void process(String qcBatchId, String mode) throws SapphireException {
        if (qcBatchId.length() == 0) {
            throw new SapphireException("QCBatchId not specified.");
        }
        QCBatchDetails qcBatchDetails = new QCBatchDetails(this.connectionInfo, this.queryProcessor, qcBatchId);
        String flag = qcBatchDetails.getQCBatch().getString(0, "blockflag", "N");
        if ("Y".equals(flag)) {
            throw new SapphireException("The QCBatch is blocked for download");
        }
        this.sampleSetMethod = this.fetchSampleSetMethod(qcBatchDetails);
        this.sampleSetLines = this.fetchSampleSetLine(qcBatchDetails);
        this.components = this.fetchComponents(qcBatchDetails);
        this.qcBatchSampleTypes = qcBatchDetails.getQCBatchSampleTypes();
        this.reagentLotLines = this.fetchReagentLotLine(qcBatchDetails);
        this.reagentLotComponents = this.fetchReagentLotComponents(qcBatchDetails);
    }

    public DataSet getSampleSetMethod() {
        return this.sampleSetMethod;
    }

    public DataSet getSampleSetLines() {
        return this.sampleSetLines;
    }

    public DataSet getQCBatchSampleTypes() {
        return this.qcBatchSampleTypes;
    }

    public DataSet getReagentLotLines() {
        return this.reagentLotLines;
    }

    public DataSet getComponents() {
        return this.components;
    }

    public DataSet getReagentLotComponents() {
        return this.reagentLotComponents;
    }

    public DataSet fetchSampleSetMethod(QCBatchDetails qcBatchDetails) throws SapphireException {
        DataSet sampleSetMethod = new DataSet(this.connectionInfo);
        sampleSetMethod.setColidCaseSensitive(true);
        sampleSetMethod.addRow();
        DataSet qcBatch = qcBatchDetails.getQCBatch();
        sampleSetMethod.setString(0, this.limsqcbatchidcol, qcBatch.getString(0, "s_qcbatchid"));
        DataSet sampleSetMethodRules = this.empowerPolicyDef.getDownloadSampleSetMethodRules();
        for (int i = 0; i < sampleSetMethodRules.getRowCount(); ++i) {
            String ruleType = sampleSetMethodRules.getString(i, "ruletype");
            String empowerColumn = sampleSetMethodRules.getString(i, "empowercolumn");
            if (!ruleType.equals("QCBatch")) continue;
            String sapphireColumn = sampleSetMethodRules.getString(i, "columnid");
            String sapphireColumnValue = qcBatchDetails.getQCBatch().getValue(0, sapphireColumn, "");
            sampleSetMethod.setString(0, empowerColumn, sapphireColumnValue);
        }
        return sampleSetMethod;
    }

    private DataSet getParentDS(QCBatchDetails qcBatchDetails, DataSet dataitem) {
        DataSet allDS = qcBatchDetails.getSampleDataSets();
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdcid", "Sample");
        filter.put("keyid1", dataitem.getString(0, "keyid1"));
        filter.put("paramlistid", dataitem.getString(0, "paramlistid"));
        filter.put("variantid", dataitem.getString(0, "variantid"));
        filter.put("dataset", new BigDecimal(dataitem.getValue(0, "dataset")));
        return allDS.getFilteredDataSet(filter);
    }

    private int getBatchItemPosition(QCBatchDetails qcBatchDetails, String batchitemid) {
        DataSet ds = qcBatchDetails.getQCBatchItems();
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("s_qcbatchitemid", batchitemid);
        DataSet match = ds.getFilteredDataSet(filter);
        return match.getInt(0, "usersequence");
    }

    private String getDataSetKeys(String keyid1, DataSet sampleDataSets) {
        DataSet ds = sampleDataSets;
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", keyid1);
        DataSet match = ds.getFilteredDataSet(filter);
        return match.getColumnValues("sdidataid", DELIMITER);
    }

    private String getReagentLotDataSetKeys(String keyid1, DataSet ds) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", keyid1);
        DataSet match = ds.getFilteredDataSet(filter);
        return match.getColumnValues("sdidataid", DELIMITER);
    }

    public PropertyList updateSampleSetMethod(PropertyList inSampleSetMethod) {
        DataSet sampleSetMethodDS = this.getSampleSetMethod();
        String[] cols = sampleSetMethodDS.getColumns();
        PropertyListCollection fields = inSampleSetMethod.getCollection("fields");
        if (fields == null) {
            fields = new PropertyListCollection();
            inSampleSetMethod.setProperty("fields", fields);
        }
        for (int i = 0; i < cols.length; ++i) {
            String currCol = cols[i];
            if (this.isStandard(currCol)) {
                inSampleSetMethod.setProperty(currCol, sampleSetMethodDS.getValue(0, currCol, ""));
                continue;
            }
            PropertyList field = new PropertyList();
            field.setProperty("name", currCol);
            field.setProperty("value", sampleSetMethodDS.getValue(0, currCol, ""));
            if (sampleSetMethodDS.getColumnType(currCol) == 1) {
                field.setProperty("type", "int");
            } else {
                field.setProperty("type", "string");
            }
            fields.add(field);
        }
        DataSet sampleSetLinesDS = this.getSampleSetLines();
        PropertyListCollection sampleSetLinesCollection = inSampleSetMethod.getCollection("samplesetlines");
        if (sampleSetLinesCollection == null) {
            sampleSetLinesCollection = new PropertyListCollection();
            inSampleSetMethod.setProperty("samplesetlines", sampleSetLinesCollection);
        }
        for (int i = 0; i < sampleSetLinesDS.getRowCount(); ++i) {
            PropertyList field;
            String currCol;
            int j;
            PropertyListCollection sslfields;
            PropertyList ssl;
            String[] sslcols;
            String sampleid = sampleSetLinesDS.getString(i, this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid"));
            int match = this.findSampleSetLine(sampleid, sampleSetLinesCollection);
            if (match != -1) {
                sslcols = sampleSetLinesDS.getColumns();
                ssl = sampleSetLinesCollection.getPropertyList(match);
                sslfields = ssl.getCollection("fields");
                if (sslfields == null) {
                    sslfields = new PropertyListCollection();
                    ssl.setProperty("fields", sslfields);
                }
                for (j = 0; j < sslcols.length; ++j) {
                    currCol = sslcols[j];
                    field = new PropertyList();
                    field.setProperty("name", currCol);
                    field.setProperty("value", sampleSetLinesDS.getValue(i, currCol, ""));
                    if (sampleSetLinesDS.getColumnType(currCol) == 1) {
                        field.setProperty("type", "int");
                    } else {
                        field.setProperty("type", "string");
                    }
                    sslfields.add(field);
                }
            } else {
                sslcols = sampleSetLinesDS.getColumns();
                ssl = new PropertyList();
                sslfields = new PropertyListCollection();
                for (j = 0; j < sslcols.length; ++j) {
                    currCol = sslcols[j];
                    field = new PropertyList();
                    field.setProperty("name", currCol);
                    field.setProperty("value", sampleSetLinesDS.getValue(i, currCol, ""));
                    if (sampleSetLinesDS.getColumnType(currCol) == 1) {
                        field.setProperty("type", "int");
                    } else {
                        field.setProperty("type", "string");
                    }
                    sslfields.add(field);
                }
                ssl.setProperty("fields", sslfields);
                sampleSetLinesCollection.add(ssl);
            }
            inSampleSetMethod.setProperty("samplesetlines", sampleSetLinesCollection);
        }
        DataSet componentsDS = this.getComponents();
        return inSampleSetMethod;
    }

    private int findSampleSetLine(String sampleid, PropertyListCollection samplesetlines) {
        int match = -1;
        for (int i = 0; i < samplesetlines.size(); ++i) {
            PropertyList currSSL = samplesetlines.getPropertyList(i);
            PropertyListCollection fields = currSSL.getCollection("fields");
            for (int j = 0; j < fields.size(); ++j) {
                String currSampleId;
                PropertyList currField = fields.getPropertyList(j);
                if (!currField.getProperty("name").equals(this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid")) || !sampleid.equals(currSampleId = currField.getProperty("value"))) continue;
                match = i;
                break;
            }
            if (match != -1) break;
        }
        return match;
    }

    private boolean isStandard(String colname) {
        return colname.equals("comments") || colname.equals("date") || colname.equals("islocked") || colname.equals("modifiedby") || colname.equals("name") || colname.equals("offsetbathb") || colname.equals("offsettime") || colname.equals("revisioncomment") || colname.equals("version");
    }

    public DataSet fetchSampleSetLine(QCBatchDetails qcBatchDetails) throws SapphireException {
        int i;
        DataSet sampleSetLines = new DataSet(this.connectionInfo);
        sampleSetLines.setColidCaseSensitive(true);
        sampleSetLines.setColidCaseSensitive(true);
        sampleSetLines.addColumn("Sample Position", 1);
        sampleSetLines.addColumn(this.limssampleidcol, 0);
        sampleSetLines.addColumn("SampleType", 0);
        sampleSetLines.addColumn(this.limsdatasetkeyidcol, 0);
        sampleSetLines.addColumn("Level", 0);
        DataSet standardSampleSetLineRules = this.empowerPolicyDef.getDownloadStandardSampleSetLineRules();
        DataSet standardSampleSetLines = this.processSampleSetLine(qcBatchDetails, standardSampleSetLineRules, "Standard");
        DataSet controlSampleSetLines = this.processSampleSetLine(qcBatchDetails, standardSampleSetLineRules, "Control");
        DataSet unknownSampleSetLineRules = this.empowerPolicyDef.getDownloadUnknownSampleSetLineRules();
        DataSet unknownSampleSetLines = this.processSampleSetLine(qcBatchDetails, unknownSampleSetLineRules, "Unknown");
        sampleSetLines.copyRow(standardSampleSetLines, -1, 1);
        sampleSetLines.copyRow(controlSampleSetLines, -1, 1);
        sampleSetLines.copyRow(unknownSampleSetLines, -1, 1);
        DataSet mysortedssl = new DataSet();
        mysortedssl.setColidCaseSensitive(true);
        int maxpos = -1;
        for (i = 0; i < sampleSetLines.getRowCount(); ++i) {
            int currpos = sampleSetLines.getBigDecimal(i, "Sample Position", new BigDecimal(-1)).intValue();
            if (currpos <= maxpos) continue;
            maxpos = currpos;
        }
        for (i = 1; i <= maxpos; ++i) {
            HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
            filter.put("Sample Position", new BigDecimal(i));
            DataSet pos = sampleSetLines.getFilteredDataSet(filter);
            if (pos == null) continue;
            mysortedssl.copyRow(pos, 0, 1);
        }
        sampleSetLines = mysortedssl;
        for (i = 0; i < sampleSetLines.getRowCount(); ++i) {
            Trace.logInfo("Trace after sort SSL" + i + " has position:" + sampleSetLines.getValue(i, "Sample Position"));
        }
        return sampleSetLines;
    }

    public DataSet fetchReagentLotLine(QCBatchDetails qcBatchDetails) throws SapphireException {
        DataSet std = qcBatchDetails.getReagentLots();
        if (std == null) {
            return new DataSet(this.connectionInfo);
        }
        DataSet standardReagentLotLineRules = this.empowerPolicyDef.getDownloadStandardReagentLotLineRules();
        DataSet reagentLotLines = this.processReagentLotLine(qcBatchDetails, standardReagentLotLineRules, std);
        return reagentLotLines;
    }

    private String findStandardLevel(QCBatchDetails qcBatchDetails, String sampleId) {
        DataSet sampleDataSets = qcBatchDetails.getSampleDataSets();
        DataSet qcbatchSampleTypes = qcBatchDetails.getQCBatchSampleTypes();
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("keyid1", sampleId);
        DataSet match = sampleDataSets.getFilteredDataSet(filter);
        if (match.getRowCount() > 0) {
            String qcbatchitemid = match.getString(0, "s_qcbatchitemid");
            filter = new HashMap();
            filter.put("s_qcbatchitemid", qcbatchitemid);
            DataSet qcbatchitem = qcBatchDetails.getQCBatchItems().getFilteredDataSet(filter);
            filter = new HashMap();
            filter.put("s_qcbatchsampletypeid", qcbatchitem.getString(0, "qcbatchsampletypeid"));
            DataSet qcbatchsampletype = qcbatchSampleTypes.getFilteredDataSet(filter);
            if (qcbatchsampletype.getRowCount() > 0) {
                return qcbatchsampletype.getString(0, "standardlevel");
            }
        }
        return "";
    }

    public DataSet fetchCandidateSampleSetLine(DataSet sdiworkitemlist, DataSet samples, DataSet sdidataitems) throws SapphireException {
        DataSet sampleSetLines = this.processCandidateSampleSetLine(this.empowerPolicyDef.getDownloadUnknownSampleSetLineRules(), sdiworkitemlist, samples, sdidataitems);
        sampleSetLines.sort("Sample Position");
        return sampleSetLines;
    }

    private DataSet processSampleSetLine(QCBatchDetails qcBatchDetails, DataSet rules, String sampletype) throws SapphireException {
        String limssampleidcol = this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimssampleid");
        String limsdatasetkeyidcol = this.empowerPolicyDef.getEmpowerCoreMapping("empowerlimsdatasetkey");
        DataSet sampleSetLines = new DataSet(this.connectionInfo);
        sampleSetLines.setColidCaseSensitive(true);
        sampleSetLines.addColumn("Sample Position", 1);
        sampleSetLines.addColumn(limssampleidcol, 0);
        sampleSetLines.addColumn("SampleType", 0);
        sampleSetLines.addColumn(limsdatasetkeyidcol, 0);
        sampleSetLines.addColumn("qcbatchitemid", 0);
        DataSet qcBatchItems = qcBatchDetails.getQCBatchItems();
        DataSet sdiData = qcBatchDetails.getSampleDataSets();
        DataSet sampleDS = qcBatchDetails.getSamples();
        DataSet sampleSDIWIs = qcBatchDetails.getSampleWorkItems();
        for (int currItem = 0; currItem < qcBatchItems.getRowCount(); ++currItem) {
            String currQCBatchItemId = qcBatchItems.getString(currItem, "s_qcbatchitemid");
            HashMap<String, String> sdidatafilter = new HashMap<String, String>();
            sdidatafilter.put("s_qcbatchitemid", currQCBatchItemId);
            DataSet currQCBatchItemDataSets = sdiData.getFilteredDataSet(sdidatafilter);
            if (currQCBatchItemDataSets == null) {
                throw new SapphireException("No sdidata found for batch item.");
            }
            int currDS = 1;
            if (currQCBatchItemDataSets.getRowCount() >= 1) {
                int max = 1;
                for (int i = 0; i < currQCBatchItemDataSets.size(); ++i) {
                    if (currQCBatchItemDataSets.getInt(i, "dataset") <= max) continue;
                    max = currQCBatchItemDataSets.getInt(i, "dataset");
                }
                currDS = max;
            }
            HashMap<String, String> sampleFilter = new HashMap<String, String>();
            sampleFilter.put("keyid1", currQCBatchItemDataSets.getString(0, "keyid1"));
            DataSet currSample = sampleDS.getFilteredDataSet(sampleFilter);
            String currSampleId = currSample.getString(0, "keyid1");
            String currSampleType = qcBatchItems.getString(currItem, "batchitemtype", "");
            if (!(sampletype.equals("Standard") && currSampleType.equals("Standard") || sampletype.equals("Control") && currSampleType.equals("Control")) && (!sampletype.equals("Unknown") || currSampleType.equals("Standard") || currSampleType.equals("Control"))) continue;
            int currRow = sampleSetLines.addRow();
            sampleSetLines.setString(currRow, limssampleidcol, currSampleId);
            String newDataSetKeys = "QCBATCHITEMID=" + currQCBatchItemId;
            String oldDataSetKeys = currQCBatchItemDataSets.getColumnValues("sdidataid", DELIMITER);
            sampleSetLines.setString(currRow, limsdatasetkeyidcol, newDataSetKeys);
            sampleSetLines.setNumber(currRow, "Sample Position", qcBatchItems.getInt(currItem, "usersequence"));
            sampleSetLines.setString(currRow, "SampleType", currSampleType);
            if (currSampleType.equals("Standard")) {
                sampleSetLines.setString(currRow, "Level", this.findStandardLevel(qcBatchDetails, currSampleId));
            }
            for (int i = 0; i < rules.getRowCount(); ++i) {
                String sapphireColumn;
                String ruleType = rules.getString(i, "ruletype");
                String empowerColumn = rules.getString(i, "empowercolumn");
                if (ruleType.equals("Sample")) {
                    sapphireColumn = rules.getString(i, "columnid");
                    String sapphireColumnValue = currSample.getValue(0, sapphireColumn, "");
                    sampleSetLines.setString(currRow, empowerColumn, sapphireColumnValue);
                }
                if (ruleType.equals("WorkItemCol")) {
                    sapphireColumn = rules.getString(i, "columnid");
                    DataSet currentSDIWI = this.getSampleSDIWorkItem(oldDataSetKeys.split(DELIMITER)[0], sdiData, sampleSDIWIs);
                    if (currentSDIWI.getRowCount() <= 0) continue;
                    String sapphireColumnValue = currentSDIWI.getValue(0, sapphireColumn, "");
                    sampleSetLines.setString(currRow, empowerColumn, sapphireColumnValue);
                    continue;
                }
                if (!ruleType.equals("DataItem")) continue;
                String paramid = rules.getString(i, "paramid");
                String paramtype = rules.getString(i, "paramtype");
                DataSet match = qcBatchDetails.getSampleDataItem(currSample.getString(0, "keyid1"), paramid, paramtype);
                if (match.getRowCount() == 0) {
                    Trace.log("Ignoring rule. Match not found for sample " + currSample.getString(0, "keyid1") + " paramid:" + paramid + " paramtype:" + paramtype);
                    continue;
                }
                if (match.getRowCount() == 1) {
                    sampleSetLines.setString(currRow, empowerColumn, this.getTransformValByDataType(match));
                    continue;
                }
                if (match.getRowCount() <= 1) continue;
                HashMap<String, BigDecimal> dsfilter = new HashMap<String, BigDecimal>();
                dsfilter.put("dataset", new BigDecimal(currDS));
                match = match.getFilteredDataSet(dsfilter);
                if (match == null || match.getRowCount() <= 0) continue;
                sampleSetLines.setString(currRow, empowerColumn, this.getTransformValByDataType(match));
            }
        }
        return sampleSetLines;
    }

    private String getTransformValByDataType(DataSet match) {
        String type;
        if (match.getRowCount() > 1) {
            match.sort("dataset desc");
        }
        if ((type = match.getValue(0, "datatypes", "T")).equals("N") || type.equals("NC")) {
            return match.getValue(0, "transformvalue");
        }
        if (type.equals("D")) {
            return match.getValue(0, "transformdt");
        }
        return match.getValue(0, "transformtext");
    }

    private DataSet processCandidateSampleSetLine(DataSet rules, DataSet sdiworkitemlist, DataSet sampleDS, DataSet sdidataitems) throws SapphireException {
        DataSet sampleSetLines = new DataSet(this.connectionInfo);
        sampleSetLines.setColidCaseSensitive(true);
        sampleSetLines.addColumn("sdiworkitemid", 0);
        sampleSetLines.addColumn("workitemid", 0);
        sampleSetLines.addColumn(this.limssampleidcol, 0);
        sampleSetLines.addColumn("SampleType", 0);
        sampleSetLines.addColumn(this.limsdatasetkeyidcol, 0);
        sampleSetLines.addColumn("sampledesc", 0);
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT * FROM sdiworkitem  WHERE sdiworkitemid IN ( " + safeSQL.addIn(sdiworkitemlist.getColumnValues("sdiworkitemid", DELIMITER), DELIMITER) + " )";
        DataSet sdiworkitemdetails = this.queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (int i = 0; i < rules.getRowCount(); ++i) {
            String ruleType = rules.getString(i, "ruletype");
            String empowerColumn = rules.getString(i, "empowercolumn");
            for (int j = 0; j < sdiworkitemlist.getRowCount(); ++j) {
                String sapphireColumn;
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("sdiworkitemid", sdiworkitemlist.getString(j, "sdiworkitemid"));
                int currrow = sampleSetLines.findRow(filter);
                if (currrow == -1) {
                    currrow = sampleSetLines.addRow();
                    sampleSetLines.setString(currrow, "sdiworkitemid", sdiworkitemlist.getString(j, "sdiworkitemid"));
                    sampleSetLines.setString(currrow, this.limssampleidcol, sdiworkitemlist.getString(j, "keyid1"));
                    sampleSetLines.setString(currrow, "workitemid", sdiworkitemlist.getString(j, "workitemid"));
                    sampleSetLines.setString(currrow, "Position", "" + (j + 1));
                }
                filter = new HashMap();
                filter.put("s_sampleid", sdiworkitemlist.getString(j, "keyid1"));
                DataSet currSample = sampleDS.getFilteredDataSet(filter);
                filter = new HashMap();
                filter.put("sdiworkitemid", sdiworkitemlist.getString(j, "sdiworkitemid"));
                DataSet currSampleWI = sdiworkitemdetails.getFilteredDataSet(filter);
                sampleSetLines.setString(currrow, "sampledesc", currSample.getString(0, "sampledesc"));
                String dataSetKeys = sdiworkitemlist.getString(j, "datasetkeyes");
                sampleSetLines.setString(currrow, this.limsdatasetkeyidcol, dataSetKeys);
                sampleSetLines.setString(currrow, "SampleType", currSample.getString(0, "qcsampletype"));
                if (ruleType.equals("Sample")) {
                    sapphireColumn = rules.getString(i, "columnid");
                    String sapphireColumnValue = currSample.getValue(0, sapphireColumn, "");
                    sampleSetLines.setString(currrow, empowerColumn, sapphireColumnValue);
                    continue;
                }
                if (ruleType.equals("WorkItemCol")) {
                    sapphireColumn = rules.getString(i, "columnid");
                    String wiColumnValue = currSampleWI.getValue(0, sapphireColumn, "");
                    sampleSetLines.setString(currrow, empowerColumn, wiColumnValue);
                    continue;
                }
                if (!ruleType.equals("DataItem")) continue;
                String paramid = rules.getString(i, "paramid");
                String paramtype = rules.getString(i, "paramtype");
                String newdatasetkey = sdiworkitemlist.getString(j, "datasetkeyes", "");
                String olddatasetkey = EmpowerDownloadProcessor.getSDIDataIdListAdhocMode(this.queryProcessor, newdatasetkey);
                String[] currsdidataids = StringUtil.split(olddatasetkey, DELIMITER);
                DataSet match = this.getWorkItemDataItem(currsdidataids, paramid, paramtype, "1", sdidataitems);
                if (match.getRowCount() == 0) {
                    Trace.log("Ignoring rule. Match not found for workitem" + sdiworkitemlist.getString(j, "sdiworkitemid") + " paramid:" + paramid + " paramtype:" + paramtype);
                    continue;
                }
                if (match.getRowCount() < 1) continue;
                sampleSetLines.setString(currrow, empowerColumn, this.getTransformValByDataType(match));
            }
        }
        return sampleSetLines;
    }

    private DataSet processReagentLotLine(QCBatchDetails qcBatchDetails, DataSet rules, DataSet reagentLots) {
        DataSet reagentLotLines = new DataSet(this.connectionInfo);
        DataSet reagentLotDataItems = qcBatchDetails.getReagentLotDataItems();
        reagentLotLines.setColidCaseSensitive(true);
        reagentLotLines.addColumn("QCSampleType", 0);
        reagentLotLines.addColumn("Level", 0);
        reagentLotLines.addColumn("reagentlotid", 0);
        reagentLotLines.addColumn("reagenttypeid", 0);
        reagentLotLines.addColumn(this.limsdatasetkeyidcol, 0);
        DataSet qcBatchSampleTypes = qcBatchDetails.getQCBatchSampleTypes();
        for (int j = 0; j < reagentLots.getRowCount(); ++j) {
            int currrow = 0;
            String currReagentLotId = reagentLots.getString(j, "reagentlotid");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("reagentlotid", currReagentLotId);
            DataSet find = reagentLotLines.getFilteredDataSet(filter);
            if (find.getRowCount() > 0) {
                currrow = new Integer(find.getString(0, "index"));
            } else {
                currrow = reagentLotLines.addRow();
                reagentLotLines.setString(currrow, "index", currrow + "");
            }
            HashMap<String, String> qctypefilter = new HashMap<String, String>();
            qctypefilter.put("reagentlotid", currReagentLotId);
            DataSet match = qcBatchSampleTypes.getFilteredDataSet(qctypefilter);
            reagentLotLines.setString(currrow, "QCSampleType", match.getString(0, "qcsampletype", ""));
            reagentLotLines.setString(currrow, "Level", match.getString(0, "standardlevel", ""));
            reagentLotLines.setString(currrow, "reagentlotid", currReagentLotId);
            String dataSetKeys = this.getDataSetKeys(currReagentLotId, qcBatchDetails.getReagentLotDataSets());
            reagentLotLines.setString(currrow, this.limsdatasetkeyidcol, dataSetKeys);
            reagentLotLines.setString(currrow, "reagenttypeid", reagentLots.getString(j, "reagenttypeid"));
            for (int i = 0; i < rules.getRowCount(); ++i) {
                String ruleType = rules.getString(i, "ruletype");
                String empowerColumn = rules.getString(i, "empowercolumn");
                if (ruleType.equals("ReagentLot")) {
                    String sapphireColumn = rules.getString(i, "columnid");
                    String sapphireColumnValue = reagentLots.getValue(j, sapphireColumn, "");
                    reagentLotLines.setString(currrow, empowerColumn, sapphireColumnValue);
                    continue;
                }
                if (!ruleType.equals("DataItem")) continue;
                String paramid = rules.getString(i, "paramid");
                String paramtype = rules.getString(i, "paramtype");
                filter = new HashMap();
                filter.put("reagentlotid", currReagentLotId);
                filter.put("reagenttypeid", reagentLots.getString(j, "reagenttypeid"));
                if (paramid.length() > 0 && !paramid.equals("#")) {
                    filter.put("paramid", paramid);
                }
                filter.put("paramtype", paramtype);
                match = reagentLotDataItems.getFilteredDataSet(filter);
                if (match.getRowCount() == 0) {
                    Trace.log("Ignoring rule. Match not found for reagentlot " + reagentLots.getString(j, "reagentlotid") + " paramid:" + paramid + " paramtype:" + paramtype);
                    continue;
                }
                if (match.getRowCount() < 1) continue;
                reagentLotLines.setString(currrow, empowerColumn, this.getTransformValByDataType(match));
            }
        }
        return reagentLotLines;
    }

    private DataSet processComponents(QCBatchDetails qcBatchDetails, DataSet rules, String sampletype) throws SapphireException {
        DataSet components = new DataSet(this.connectionInfo);
        components.setColidCaseSensitive(true);
        DataSet qcBatchItems = qcBatchDetails.getQCBatchItems();
        DataSet sdiData = qcBatchDetails.getSampleDataSets();
        DataSet sampleDS = qcBatchDetails.getSamples();
        for (int currItem = 0; currItem < qcBatchItems.getRowCount(); ++currItem) {
            HashMap<String, String> sdidatafilter = new HashMap<String, String>();
            String currQCBatchItem = qcBatchItems.getString(currItem, "s_qcbatchitemid");
            sdidatafilter.put("s_qcbatchitemid", currQCBatchItem);
            DataSet currSdiData = sdiData.getFilteredDataSet(sdidatafilter);
            if (currSdiData == null) {
                throw new SapphireException("No sdidata found for batch item.");
            }
            HashMap<String, String> sampleFilter = new HashMap<String, String>();
            sampleFilter.put("keyid1", currSdiData.getString(0, "keyid1"));
            DataSet currSample = sampleDS.getFilteredDataSet(sampleFilter);
            String currSampleId = currSample.getString(0, "keyid1");
            String currSampleType = currSample.getString(0, "qcsampletype");
            if (!(sampletype.equals("Standard") && currSampleType.equals("Standard") || sampletype.equals("Control") && currSampleType.equals("Control")) && (!sampletype.equals("Unknown") || currSampleType.equals("Standard") || currSampleType.equals("Control"))) continue;
            for (int i = 0; i < rules.getRowCount(); ++i) {
                String empowerColumn = rules.getString(i, "empowercolumn");
                String sapphireColumn = rules.getValue(i, "columnid");
                String paramtype = rules.getString(i, "paramtype");
                String context = rules.getString(i, "context");
                String[] sdidataid = StringUtil.split(currSdiData.getColumnValues("sdidataid", DELIMITER), DELIMITER);
                DataSet currdataitems = null;
                currdataitems = context.equals("Current") ? this.getQCBatchItemDataItems(sdidataid, sdiData, qcBatchDetails.getSampleDataItems(), paramtype) : this.fetchReagentLotDataItems(qcBatchDetails, currSdiData, paramtype);
                HashSet<String> processedParams = new HashSet<String>();
                for (int currdi = 0; currdi < currdataitems.getRowCount(); ++currdi) {
                    String paramid = currdataitems.getString(currdi, "paramid");
                    String paramAlias = currdataitems.getString(currdi, "aliasid", paramid);
                    boolean duplicatesExist = false;
                    if (processedParams.contains(paramid)) {
                        Trace.logDebug("multiple items with same paramid:" + paramid);
                        duplicatesExist = true;
                    }
                    processedParams.add(paramid);
                    String sapphireColumnValue = currdataitems.getValue(currdi, sapphireColumn);
                    String currentlimsdatasetkey = "QCBATCHITEMID=" + currQCBatchItem;
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("Component", paramAlias);
                    filter.put(this.limsdatasetkeyidcol, currentlimsdatasetkey);
                    DataSet match = components.getFilteredDataSet(filter);
                    int comprow = -1;
                    if (match == null || match.getRowCount() == 0) {
                        comprow = components.addRow();
                        components.setString(comprow, "index", comprow + "");
                        components.setNumber(comprow, "Sample Position", qcBatchItems.getInt(currItem, "usersequence"));
                        components.setString(comprow, "Component", paramAlias);
                        components.setString(comprow, this.limssampleidcol, currSampleId);
                        components.setString(comprow, "SampleType", currSample.getString(0, "qcsampletype"));
                        components.setString(comprow, empowerColumn, sapphireColumnValue);
                        components.setString(comprow, this.limsdatasetkeyidcol, currentlimsdatasetkey);
                        components.setNumber(comprow, "dataset", currdataitems.getInt(currdi, "dataset"));
                        continue;
                    }
                    if (match.getRowCount() == 1) {
                        int currentDS;
                        Trace.logDebug("Found one match: setting empowerColumn:" + empowerColumn + " to " + sapphireColumnValue);
                        comprow = new Integer(match.getString(0, "index"));
                        if (!duplicatesExist) {
                            components.setString(comprow, empowerColumn, sapphireColumnValue);
                            continue;
                        }
                        int prevDS = components.getInt(comprow, "dataset");
                        if (prevDS >= (currentDS = currdataitems.getInt(currdi, "dataset"))) continue;
                        components.setString(comprow, empowerColumn, sapphireColumnValue);
                        components.setNumber(comprow, "dataset", currentDS);
                        continue;
                    }
                    throw new SapphireException("Invalid configuration. Multiple matches found for Peak " + paramid);
                }
            }
        }
        return components;
    }

    private DataSet processCandidateComponents(DataSet sdiworkitems, DataSet rules, DataSet samples, DataSet sdidata, DataSet sdidataitems) throws SapphireException {
        DataSet components = new DataSet(this.connectionInfo);
        components.addColumn("Component", 0);
        components.addColumn("Position", 1);
        components.addColumn("sdiworkitemid", 0);
        components.addColumn(this.limssampleidcol, 0);
        components.addColumn(this.limsdatasetkeyidcol, 0);
        components.addColumn("SampleType", 0);
        components.addColumn("dataset", 1);
        for (int i = 0; i < rules.getRowCount(); ++i) {
            String empowerColumn = rules.getString(i, "empowercolumn");
            String sapphireColumn = rules.getValue(i, "columnid");
            String paramtype = rules.getString(i, "paramtype");
            for (int currwi = 0; currwi < sdiworkitems.getRowCount(); ++currwi) {
                String sdiworkitemid = sdiworkitems.getString(currwi, "sdiworkitemid");
                String sampleid1 = sdiworkitems.getString(currwi, "keyid1");
                int currsamplerow = samples.findRow("s_sampleid", sampleid1);
                if (currsamplerow == -1) continue;
                String newdatasetkey = sdiworkitems.getString(currwi, "datasetkeyes", "");
                String olddatasetkey = EmpowerDownloadProcessor.getSDIDataIdListAdhocMode(this.queryProcessor, newdatasetkey);
                String[] currsdidataids = StringUtil.split(olddatasetkey, DELIMITER);
                DataSet currdataitems = this.getWorkItemDataItem(currsdidataids, "", paramtype, "1", sdidataitems);
                HashSet<String> processedParams = new HashSet<String>();
                for (int currdi = 0; currdi < currdataitems.getRowCount(); ++currdi) {
                    String paramid = currdataitems.getString(currdi, "paramid");
                    String paramAlias = currdataitems.getString(currdi, "aliasid", paramid);
                    boolean duplicatesExist = false;
                    if (processedParams.contains(paramid)) {
                        Trace.logDebug("multiple items with same paramid:" + paramid);
                        duplicatesExist = true;
                    }
                    processedParams.add(paramid);
                    String sapphireColumnValue = currdataitems.getValue(currdi, sapphireColumn);
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("Component".toLowerCase(), paramAlias);
                    filter.put("sdiworkitemid", sdiworkitemid);
                    DataSet match = components.getFilteredDataSet(filter);
                    int comprow = -1;
                    if (match == null || match.getRowCount() == 0) {
                        comprow = components.addRow();
                        components.setString(comprow, "index", comprow + "");
                        components.setString(comprow, "Component", paramAlias);
                        components.setString(comprow, "sdiworkitemid", sdiworkitemid);
                        components.setString(comprow, this.limssampleidcol, sampleid1);
                        components.setString(comprow, this.limsdatasetkeyidcol, sdiworkitems.getString(currwi, "datasetkeyes", ""));
                        components.setString(comprow, "SampleType", samples.getString(currsamplerow, "qcsampletype", ""));
                        components.setString(comprow, empowerColumn, sapphireColumnValue);
                        components.setNumber(comprow, "dataset", currdataitems.getInt(currdi, "dataset"));
                        continue;
                    }
                    if (match.getRowCount() == 1) {
                        int currentDS;
                        comprow = new Integer(match.getString(0, "index"));
                        if (!duplicatesExist) {
                            components.setString(comprow, empowerColumn, sapphireColumnValue);
                            continue;
                        }
                        int prevDS = components.getInt(comprow, "dataset");
                        if (prevDS >= (currentDS = currdataitems.getInt(currdi, "dataset"))) continue;
                        components.setString(comprow, empowerColumn, sapphireColumnValue);
                        components.setNumber(comprow, "dataset", currentDS);
                        continue;
                    }
                    throw new SapphireException("Invalid configuration. Multiple matches found for Peak " + paramid);
                }
            }
        }
        return components;
    }

    private DataSet processReagentLotComponents(QCBatchDetails qcBatchDetails, DataSet rules) throws SapphireException {
        DataSet components = new DataSet(this.connectionInfo);
        DataSet reagentLots = qcBatchDetails.getReagentLots();
        if (reagentLots == null) {
            return components;
        }
        DataSet reagentLotDataItems = qcBatchDetails.getReagentLotDataItems();
        for (int i = 0; i < rules.getRowCount(); ++i) {
            String empowerColumn = rules.getString(i, "empowercolumn");
            String sapphireColumn = rules.getValue(i, "columnid");
            String paramtype = rules.getString(i, "paramtype");
            for (int j = 0; j < reagentLots.getRowCount(); ++j) {
                String reagentLotId = reagentLots.getString(j, "reagentlotid");
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("keyid1", reagentLotId);
                filter.put("paramtype", paramtype);
                DataSet currdataitems = reagentLotDataItems.getFilteredDataSet(filter);
                HashSet<String> processedParams = new HashSet<String>();
                for (int currdi = 0; currdi < currdataitems.getRowCount(); ++currdi) {
                    String paramid = currdataitems.getString(currdi, "paramid");
                    String paramAlias = currdataitems.getString(currdi, "aliasid", paramid);
                    if (processedParams.contains(paramid)) {
                        throw new SapphireException("ReagentLot" + reagentLotId + " contains multiple data items with the same param " + paramid);
                    }
                    processedParams.add(paramid);
                    String sapphireColumnValue = currdataitems.getValue(currdi, sapphireColumn);
                    filter = new HashMap();
                    filter.put("Component".toLowerCase(), paramAlias);
                    filter.put("reagentlotid", reagentLotId);
                    DataSet match = components.getFilteredDataSet(filter);
                    int comprow = -1;
                    if (match == null || match.getRowCount() == 0) {
                        comprow = components.addRow();
                        components.setString(comprow, "index", comprow + "");
                        components.setString(comprow, "Component", paramAlias);
                        components.setString(comprow, "reagentlotid", reagentLotId);
                        components.setString(comprow, empowerColumn, sapphireColumnValue);
                        continue;
                    }
                    if (match.getRowCount() == 1) {
                        comprow = new Integer(match.getString(0, "index"));
                        components.setString(comprow, empowerColumn, sapphireColumnValue);
                        continue;
                    }
                    throw new SapphireException("Invalid configuration. Multiple matches found for Peak " + paramid);
                }
            }
        }
        return components;
    }

    private DataSet processCandidateReagentLotComponents(DataSet rules, DataSet reagentLots, DataSet reagentLotDataSets, DataSet reagentLotDataItems) throws SapphireException {
        DataSet components = new DataSet(this.connectionInfo);
        for (int i = 0; i < rules.getRowCount(); ++i) {
            String empowerColumn = rules.getString(i, "empowercolumn");
            String sapphireColumn = rules.getString(i, "columnid");
            String paramtype = rules.getString(i, "paramtype");
            String context = rules.getString(i, "context");
            for (int j = 0; j < reagentLots.getRowCount(); ++j) {
                String reagentLotId = reagentLots.getString(j, "reagentlotid");
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("keyid1", reagentLotId);
                if (paramtype != null) {
                    filter.put("paramtype", paramtype);
                }
                DataSet currdataitems = reagentLotDataItems.getFilteredDataSet(filter);
                HashSet<String> processedParams = new HashSet<String>();
                for (int currdi = 0; currdi < currdataitems.getRowCount(); ++currdi) {
                    String paramid = currdataitems.getString(currdi, "paramid");
                    String paramAlias = currdataitems.getString(currdi, "aliasid", paramid);
                    if (processedParams.contains(paramid)) {
                        throw new SapphireException("ReagentLot" + reagentLotId + " contains multiple data items with the same param " + paramid);
                    }
                    processedParams.add(paramid);
                    String sapphireColumnValue = currdataitems.getValue(currdi, sapphireColumn);
                    filter = new HashMap();
                    filter.put("Component", paramAlias);
                    filter.put("reagentlotid", reagentLotId);
                    DataSet match = components.getFilteredDataSet(filter);
                    int comprow = -1;
                    if (match == null || match.getRowCount() == 0) {
                        comprow = components.addRow();
                        components.setString(comprow, "index", comprow + "");
                        components.setString(comprow, "Component", paramAlias);
                        components.setString(comprow, "reagentlotid", reagentLotId);
                        components.setString(comprow, empowerColumn, sapphireColumnValue);
                        continue;
                    }
                    if (match.getRowCount() == 1) {
                        comprow = new Integer(match.getString(0, "index"));
                        components.setString(comprow, empowerColumn, sapphireColumnValue);
                        continue;
                    }
                    throw new SapphireException("Invalid configuration. Multiple matches found for Peak " + paramid);
                }
            }
        }
        return components;
    }

    public DataSet fetchReagentLotComponents(QCBatchDetails qcBatchDetails) throws SapphireException {
        DataSet rlComponentRules = this.empowerPolicyDef.getDownloadRegentLotComponentRules();
        DataSet components = this.processReagentLotComponents(qcBatchDetails, rlComponentRules);
        return components;
    }

    public DataSet fetchCandidateSampleComponents(DataSet sdiworkitems, DataSet samples, DataSet sdidata, DataSet sdidataitems) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("qcsampletype", "Unknown");
        DataSet unk = samples.getFilteredDataSet(filter);
        filter.put("qcsampletype", "Standard");
        DataSet unknownComponentRules = this.empowerPolicyDef.getDownloadUnknownComponentRules();
        DataSet components = this.processCandidateComponents(sdiworkitems, unknownComponentRules, unk, sdidata, sdidataitems);
        for (int i = 0; i < components.getRowCount(); ++i) {
            String currsdiwortitemid = components.getString(i, "sdiworkitemid");
            int wirow = sdiworkitems.findRow("sdiworkitemid", currsdiwortitemid);
            String position = sdiworkitems.getString(wirow, "Position", "");
            components.setNumber(i, "Position", position);
        }
        components.sort("Position");
        return components;
    }

    public DataSet fetchComponents(QCBatchDetails qcBatchDetails) throws SapphireException {
        DataSet components = new DataSet(this.connectionInfo);
        components.setColidCaseSensitive(true);
        components.addColumn("Component", 0);
        components.addColumn("Sample Position", 1);
        components.addColumn(this.limssampleidcol, 0);
        components.addColumn("SampleType", 0);
        components.addColumn(this.limsdatasetkeyidcol, 0);
        DataSet standardComponentRules = this.empowerPolicyDef.getDownloadStandardComponentRules();
        DataSet unknownComponentRules = this.empowerPolicyDef.getDownloadUnknownComponentRules();
        DataSet standard = this.processComponents(qcBatchDetails, standardComponentRules, "Standard");
        DataSet control = this.processComponents(qcBatchDetails, standardComponentRules, "Control");
        DataSet unknown = this.processComponents(qcBatchDetails, unknownComponentRules, "Unknown");
        components.copyRow(standard, -1, 1);
        components.copyRow(control, -1, 1);
        components.copyRow(unknown, -1, 1);
        components.sort("Sample Position");
        return components;
    }

    private String findPosition(String sampleid, String positioncol) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put(this.limssampleidcol, sampleid);
        DataSet match = this.getSampleSetLines().getFilteredDataSet(filter);
        return match.getValue(0, positioncol);
    }

    private DataSet fetchReagentLotDataItems(QCBatchDetails qcBatchDetails, DataSet currSdiData, String paramtype) {
        DataSet ret = new DataSet(this.connectionInfo);
        HashSet<String> processedRL = new HashSet<String>();
        for (int i = 0; i < currSdiData.getRowCount(); ++i) {
            String qcbatchitemid = currSdiData.getString(i, "s_qcbatchitemid");
            String reagentlotid = qcBatchDetails.getReagentLotForSample(qcbatchitemid);
            if (reagentlotid == null || reagentlotid.length() == 0 || processedRL.contains(reagentlotid)) continue;
            processedRL.add(reagentlotid);
            DataSet ds = qcBatchDetails.getReagentLotDataItems();
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("keyid1", reagentlotid);
            filter.put("paramtype", paramtype);
            ret.copyRow(ds.getFilteredDataSet(filter), -1, 1);
        }
        return ret;
    }

    public static void blockQCBatch(ActionProcessor ap, String qcBatchId, String empowerProject, String empowerDatabase, String sampleSetName) throws SapphireException {
        PropertyList editProps = new PropertyList();
        editProps.setProperty("sdcid", "QCBatch");
        editProps.setProperty("keyid1", qcBatchId);
        editProps.setProperty("blockflag", "Y");
        String externalReference = QCBatchDetails.getExternalReference(empowerProject, empowerDatabase, sampleSetName);
        editProps.setProperty("externalreference", externalReference);
        ap.processAction("EditSDI", "1", editProps);
    }

    public DataSet processCandidateReagentLots(String reagentLotsIds) throws SapphireException {
        SafeSQL safeSQL = new SafeSQL();
        String s1 = "SELECT * FROM reagentlot WHERE reagentlotid in ( " + safeSQL.addIn(reagentLotsIds, DELIMITER) + ")";
        DataSet reagentLots = this.queryProcessor.getPreparedSqlDataSet(s1, safeSQL.getValues());
        safeSQL.reset();
        String s2 = "SELECT * FROM sdidata WHERE keyid1 in ( " + safeSQL.addIn(reagentLotsIds, DELIMITER) + ") ORDER by keyid1 ";
        DataSet reagentLotDataSets = this.queryProcessor.getPreparedSqlDataSet(s2, safeSQL.getValues());
        safeSQL.reset();
        String s3 = "SELECT * FROM sdidataitem WHERE keyid1 in ( " + safeSQL.addIn(reagentLotsIds, DELIMITER) + ") ORDER by keyid1 ";
        DataSet reagentLotDataItems = this.queryProcessor.getPreparedSqlDataSet(s3, safeSQL.getValues());
        reagentLots.addColumnValues("keyid1", 0, reagentLots.getColumnValues("reagentlotid", DELIMITER), DELIMITER);
        this.reagentLotLines = this.processReagentLotLines(this.empowerPolicyDef.getDownloadStandardReagentLotLineRules(), reagentLots, reagentLotDataSets, reagentLotDataItems);
        this.reagentLotComponents = this.processCandidateReagentLotComponents(this.empowerPolicyDef.getDownloadRegentLotComponentRules(), reagentLots, reagentLotDataSets, reagentLotDataItems);
        return this.reagentLotLines;
    }

    private DataSet processReagentLotLines(DataSet rules, DataSet reagentLots, DataSet reagentLotDataSets, DataSet reagentLotDataItems) {
        DataSet reagentLotLines = new DataSet(this.connectionInfo);
        reagentLotLines.setColidCaseSensitive(true);
        reagentLotLines.addColumn("reagenttypeid", 0);
        reagentLotLines.addColumn("reagentlotid", 0);
        reagentLotLines.addColumn("reagentlotdesc", 0);
        reagentLotLines.addColumn(this.limsdatasetkeyidcol, 0);
        for (int j = 0; j < reagentLots.getRowCount(); ++j) {
            int currrow = 0;
            HashMap<String, String> rlfilter = new HashMap<String, String>();
            rlfilter.put("reagentlotid", reagentLots.getString(j, "reagentlotid"));
            DataSet find = reagentLotLines.getFilteredDataSet(rlfilter);
            if (find.getRowCount() > 0) {
                currrow = new Integer(find.getString(0, "index"));
            } else {
                currrow = reagentLotLines.addRow();
                reagentLotLines.setString(currrow, "index", currrow + "");
            }
            reagentLotLines.setString(currrow, "reagentlotid", reagentLots.getString(j, "reagentlotid"));
            reagentLotLines.setString(currrow, "reagenttypeid", reagentLots.getString(j, "reagenttypeid"));
            reagentLotLines.setString(currrow, "reagentlotdesc", reagentLots.getString(j, "reagentlotdesc"));
            String dataSetKeys = this.getReagentLotDataSetKeys(reagentLots.getString(j, "reagentlotid"), reagentLotDataSets);
            reagentLotLines.setString(currrow, this.limsdatasetkeyidcol, dataSetKeys);
            for (int i = 0; i < rules.getRowCount(); ++i) {
                String ruleType = rules.getString(i, "ruletype");
                String empowerColumn = rules.getString(i, "empowercolumn");
                if (ruleType.equals("ReagentLot")) {
                    String sapphireColumn = rules.getString(i, "columnid");
                    String sapphireColumnValue = reagentLots.getValue(j, sapphireColumn, "");
                    reagentLotLines.setString(currrow, empowerColumn, sapphireColumnValue);
                    continue;
                }
                if (!ruleType.equals("DataItem")) continue;
                String paramid = rules.getString(i, "paramid");
                String paramtype = rules.getString(i, "paramtype");
                DataSet match = this.getReagentLotDataItem(reagentLots.getString(j, "reagentlotid"), paramid, paramtype, "1", reagentLotDataItems);
                if (match.getRowCount() == 0) {
                    Trace.log("Ignoring rule. Match not found for sample " + reagentLots.getString(j, "reagentlotid") + " paramid:" + paramid + " paramtype:" + paramtype);
                    continue;
                }
                if (match.getRowCount() < 1) continue;
                reagentLotLines.setString(currrow, empowerColumn, this.getTransformValByDataType(match));
            }
        }
        return reagentLotLines;
    }

    private DataSet getWorkItemDataItem(String[] sdidataids, String paramid, String paramType, String replicateid, DataSet sdidataitems) {
        int i;
        HashMap<String, Object> filter = new HashMap<String, Object>();
        if (paramid.length() > 0 && !paramid.equals("#")) {
            filter.put("paramid", paramid);
        }
        if (paramType.length() > 0 && !paramType.equals("#")) {
            filter.put("paramtype", paramType);
        }
        if (replicateid.length() > 0) {
            filter.put("replicateid", new BigDecimal(replicateid));
        }
        DataSet sampleDataItems = sdidataitems.getFilteredDataSet(filter);
        for (i = 0; i < sampleDataItems.getRowCount(); ++i) {
            sampleDataItems.setString(i, "match", "N");
        }
        for (i = 0; i < sampleDataItems.getRowCount(); ++i) {
            String currsdidataid = sampleDataItems.getString(i, "sdidataid");
            for (int currid = 0; currid < sdidataids.length; ++currid) {
                if (!currsdidataid.equals(sdidataids[currid])) continue;
                sampleDataItems.setString(i, "match", "Y");
            }
        }
        filter = new HashMap();
        filter.put("match", "Y");
        return sampleDataItems.getFilteredDataSet(filter);
    }

    private DataSet getSampleSDIWorkItem(String sdidataid, DataSet sampleDataSets, DataSet allsdiworkitems) throws SapphireException {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdidataid", sdidataid);
        DataSet matchingDataSet = sampleDataSets.getFilteredDataSet(filter);
        if (matchingDataSet.getRowCount() == 0) {
            throw new SapphireException("Invalid sdidataid");
        }
        filter = new HashMap();
        filter.put("sdcid", matchingDataSet.getString(0, "sdcid"));
        filter.put("keyid1", matchingDataSet.getString(0, "keyid1"));
        filter.put("workitemid", matchingDataSet.getValue(0, "sourceworkitemid"));
        filter.put("workiteminstance", new BigDecimal(matchingDataSet.getValue(0, "sourceworkiteminstance")));
        DataSet matchingSDIWI = allsdiworkitems.getFilteredDataSet(filter);
        return matchingSDIWI;
    }

    private DataSet getQCBatchItemDataItem(String[] sdidataids, String paramid, String paramType, String replicateid, DataSet sdidataitems) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        if (paramid.length() > 0 && !paramid.equals("#")) {
            filter.put("paramid", paramid);
        }
        if (paramType.length() > 0 && !paramType.equals("#")) {
            filter.put("paramtype", paramType);
        }
        if (replicateid.length() > 0) {
            filter.put("replicateid", new BigDecimal(replicateid));
        }
        DataSet sampleDataItems = sdidataitems.getFilteredDataSet(filter);
        for (int i = 0; i < sampleDataItems.getRowCount(); ++i) {
            String currsdidataid = sampleDataItems.getString(i, "sdidataid");
            for (int currid = 0; currid < sdidataids.length; ++currid) {
                if (!currsdidataid.equals(sdidataids[currid])) continue;
                sampleDataItems.setString(i, "match", "Y");
            }
        }
        filter = new HashMap();
        filter.put("match", "Y");
        return sampleDataItems.getFilteredDataSet(filter);
    }

    private DataSet getSampleDataItem(String keyid1, String paramid, String paramType, String replicateid, DataSet sampleDataItems) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("keyid1", keyid1);
        if (paramid.length() > 0 && !paramid.equals("#")) {
            filter.put("paramid", paramid);
        }
        if (paramType.length() > 0 && !paramType.equals("#")) {
            filter.put("paramtype", paramType);
        }
        if (replicateid.length() > 0) {
            filter.put("replicateid", new BigDecimal(replicateid));
        }
        return sampleDataItems.getFilteredDataSet(filter);
    }

    private DataSet getReagentLotDataItem(String keyid1, String paramid, String paramType, String replicateid, DataSet dataItems) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("keyid1", keyid1);
        if (paramid.length() > 0 && !paramid.equals("#")) {
            filter.put("paramid", paramid);
        }
        if (paramType.length() > 0) {
            filter.put("paramtype", paramType);
        }
        if (replicateid.length() > 0) {
            filter.put("replicateid", new BigDecimal(replicateid));
        }
        return dataItems.getFilteredDataSet(filter);
    }

    public static String createLightWeightQCBatch(ActionProcessor actionProcessor, QueryProcessor queryProcessor, String qcBatchSDCId, PropertyList qcBatchProps, DataSet qcBatchSampleTypes, DataSet qcBatchItems) throws SapphireException {
        String qcBatchId = "";
        String connectionId = actionProcessor.getConnectionid();
        if (qcBatchSDCId == null || qcBatchSDCId.length() == 0) {
            qcBatchSDCId = "Sample";
        }
        PropertyList actionProps = new PropertyList(qcBatchProps);
        actionProps.setProperty("qcbatchsdcid", qcBatchSDCId);
        actionProps.setProperty("qcbatchstatus", "Initial");
        actionProps.setProperty("evaloption", "None");
        actionProps.setProperty("blockflag", "Y");
        actionProps.setProperty("sdcid", "QCBatch");
        actionProcessor.processAction("AddSDI", "1", actionProps);
        qcBatchId = actionProps.getProperty("newkeyid1");
        actionProps.clear();
        if (qcBatchSampleTypes.getRowCount() > 0) {
            actionProps.setProperty("qcbatchid", qcBatchId);
            String[] colArray = qcBatchSampleTypes.getColumns();
            for (int i = 0; i < colArray.length; ++i) {
                actionProps.setProperty(colArray[i], qcBatchSampleTypes.getColumnValues(colArray[i], DELIMITER));
            }
            actionProps.setProperty("sdcid", "QCBatchSampleType");
            actionProps.setProperty("copies", "" + qcBatchSampleTypes.getRowCount());
            actionProps.setProperty("qcsampletype", "Standard");
            StringBuffer strUserSequences = new StringBuffer();
            for (int i = 1; i <= qcBatchSampleTypes.getRowCount(); ++i) {
                strUserSequences.append(DELIMITER + i);
            }
            actionProps.setProperty("usersequence", strUserSequences.substring(1));
            actionProcessor.processAction("AddSDI", "1", actionProps);
            String batchSampleTypeIds = actionProps.getProperty("newkeyid1");
            qcBatchSampleTypes.addColumnValues("qcbatchsampletypeid", 0, batchSampleTypeIds, DELIMITER);
        }
        SequenceProcessor sequenceProcessor = new SequenceProcessor(connectionId);
        for (int i = 0; i < qcBatchItems.getRowCount(); ++i) {
            String limsSampleId = qcBatchItems.getString(i, "empowerlimssampleid", "");
            if (limsSampleId.length() == 0) continue;
            qcBatchItems.setString(i, "usersequence", "" + (i + 1));
            qcBatchItems.setString(i, "qcbatchid", qcBatchId);
            qcBatchItems.setString(i, "qcbatchitemid", OpalUtil.getNextSequence("s_qcbatchitem", sequenceProcessor));
            qcBatchItems.setString(i, "batchitemtype", "Unknown");
        }
        EmpowerDownloadProcessor.createQCBatchItems(actionProcessor, queryProcessor, qcBatchId, qcBatchItems, qcBatchSDCId);
        return qcBatchId;
    }

    private static void createQCBatchItems(ActionProcessor ap, QueryProcessor qp, String qcBatchId, DataSet qcBatchItems, String qcBatchSDCId) throws SapphireException {
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
        ConnectionProcessor cp = new ConnectionProcessor(connectionId);
        ConnectionInfo connectionInfo = cp.getConnectionInfo(connectionId);
        if (StringUtil.getLen(rsetid) > 0L) {
            DataSet ds = qp.getPreparedSqlDataSet("SELECT ds.keyid1, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset, ds.sdidataid  FROM sdidata ds, rsetitems rs  WHERE rs.sdcid = ds.sdcid AND rs.keyid1 = ds.keyid1 AND rs.rsetid = ?", new Object[]{rsetid});
            damProcessor.clearRSet(rsetid);
            DataSet dsEditDataSets = new DataSet(connectionInfo);
            for (int i = 0; i < qcBatchItems.getRowCount(); ++i) {
                String limsKeyId = qcBatchItems.getString(i, "empowerlimssampleid");
                Trace.logDebug("eu_limsdatasetkey: " + qcBatchItems.getString(i, "empowerlimsdatasetkey"));
                String newdatasetkey = qcBatchItems.getString(i, "empowerlimsdatasetkey");
                String[] datasetIds = EmpowerDownloadProcessor.getSDIDataIdListAdhocMode(qp, newdatasetkey).split(DELIMITER);
                String qcbatchItemId = qcBatchItems.getString(i, "qcbatchitemid");
                for (int k = 0; k < datasetIds.length; ++k) {
                    HashMap<String, String> findMap = new HashMap<String, String>();
                    findMap.put("keyid1", limsKeyId);
                    findMap.put("sdidataid", datasetIds[k]);
                    int findRow = ds.findRow(findMap);
                    if (findRow <= -1) continue;
                    ds.setString(findRow, "s_qcbatchid", qcBatchId);
                    ds.setString(findRow, "s_qcbatchitemid", qcbatchItemId);
                    ds.setString(findRow, "blockflag", "Y");
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
                actionProps.setProperty("blockflag", dsEditDataSets.getColumnValues("blockflag", DELIMITER));
                ap.processAction("EditDataSet", "1", actionProps);
            }
        } else {
            throw new SapphireException(tp.translate("QCBatch creation failed.") + " " + tp.translate("Unable to create RSET for") + " " + keyIds);
        }
    }

    public static String getSDIDataIdListAdhocMode(QueryProcessor queryProcessor, String newDatasetKeys) throws SapphireException {
        if (newDatasetKeys.contains("WORKITEMID")) {
            Trace.log("fetching sdidataid list for adhoc mode");
            String[] properties = StringUtil.split(newDatasetKeys, DELIMITER);
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
            String sql = "SELECT sdidataid from sdidata sd INNER JOIN sdiworkitemitem sdwii  ON sd.sdcid = sdwii.sdcid and sd.keyid1 = sdwii.keyid1 and \nsd.keyid2 = sdwii.keyid2 and sd.keyid3 = sdwii.keyid3 and\nsd.paramlistid =  sdwii.itemkeyid1 and sd.paramlistversionid = sdwii.itemkeyid2 and sd.variantid = sdwii.itemkeyid3\nand sd.dataset = sdwii.iteminstance  WHERE sd.sdcid = ? AND sd.keyid1= ? AND  sdwii.workitemid = ? AND sdwii.workiteminstance = ?";
            SafeSQL safeSQL = new SafeSQL();
            safeSQL.addVar(sdcid);
            safeSQL.addVar(keyid1);
            safeSQL.addVar(workitemid);
            safeSQL.addVar(workiteminstance);
            DataSet sampleDataSets = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            Trace.logDebug("Sample DataSets:" + sampleDataSets.toXML());
            Trace.logDebug("sdidataid list: " + sampleDataSets.getColumnValues("sdidataid", DELIMITER));
            return sampleDataSets.getColumnValues("sdidataid", DELIMITER);
        }
        return newDatasetKeys;
    }
}

