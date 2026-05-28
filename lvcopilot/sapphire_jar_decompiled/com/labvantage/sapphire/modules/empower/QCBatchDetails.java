/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.Trace;
import java.math.BigDecimal;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class QCBatchDetails {
    private String qcbatchid = "";
    private DataSet qcBatch;
    private DataSet qcBatchItems;
    private DataSet qcBatchDataSets;
    private DataSet qcBatchDataItems;
    private DataSet qcBatchSampleTypes;
    private DataSet samples;
    private DataSet sampleDataSets;
    private DataSet sampleDataItems;
    private DataSet sampleWorkItems;
    private DataSet reagentLots;
    private DataSet reagentLotDataSets;
    private DataSet reagentLotDataItems;
    private ConnectionInfo connectionInfo;

    public QCBatchDetails(ConnectionInfo connectionInfo, QueryProcessor queryProcessor, String qcbatchid) throws SapphireException {
        this.qcbatchid = qcbatchid;
        this.connectionInfo = connectionInfo;
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT  * FROM s_qcbatch WHERE s_qcbatchid = " + safeSQL.addVar(qcbatchid);
        this.qcBatch = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (this.qcBatch == null || this.qcBatch.getRowCount() == 0) {
            throw new SapphireException("qcbatchid does not exist.");
        }
        safeSQL.reset();
        sql = "SELECT S_QCBATCHITEMID, QCBATCHITEMDESC, QCBATCHSAMPLETYPEID, USERSEQUENCE, LINKTOQCBATCHITEMID, batchitemtype FROM S_QCBATCHITEM WHERE S_QCBATCHID = " + safeSQL.addVar(qcbatchid) + " ORDER BY USERSEQUENCE";
        this.qcBatchItems = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (this.qcBatchItems != null && this.qcBatchItems.getRowCount() > 0) {
            safeSQL.reset();
            sql = "SELECT sdcid, keyid1, paramlistid, paramlistversionid, variantid, dataset FROM sdidata  WHERE sdcid = 'QCBatch' and keyid1 = " + safeSQL.addVar(qcbatchid);
            this.qcBatchDataSets = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (this.qcBatchDataSets != null && this.qcBatchDataSets.getRowCount() > 0) {
                safeSQL.reset();
                sql = "SELECT sdcid, keyid1, paramlistid, paramlistversionid, variantid, paramid, paramtype, dataset, replicateid, enteredtext, transformvalue FROM sdidataitem  WHERE sdcid = 'QCBatch' and keyid1 = " + safeSQL.addVar(qcbatchid);
                this.qcBatchDataItems = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
            safeSQL.reset();
            sql = "SELECT * from s_sample where s_sampleid in ( SELECT distinct keyid1 FROM sdidata WHERE s_qcbatchid =" + safeSQL.addVar(qcbatchid) + " ) ";
            this.samples = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            this.samples.addColumnValues("keyid1", 0, this.samples.getColumnValues("s_sampleid", ";"), ";");
            safeSQL.reset();
            sql = "SELECT * FROM sdidata sd INNER JOIN sdiworkitemitem sdwii  ON sd.sdcid = sdwii.sdcid and sd.keyid1 = sdwii.keyid1 and \nsd.keyid2 = sdwii.keyid2 and sd.keyid3 = sdwii.keyid3 and\nsd.paramlistid =  sdwii.itemkeyid1 and sd.paramlistversionid = sdwii.itemkeyid2 and sd.variantid = sdwii.itemkeyid3\nand sd.dataset = sdwii.iteminstance  WHERE s_qcbatchid = " + safeSQL.addVar(qcbatchid);
            this.sampleDataSets = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            Trace.logDebug("sampleDataSets returned:" + this.sampleDataSets.toXML());
            safeSQL.reset();
            this.sampleDataItems = this.getDataItemsForDataSets(queryProcessor, this.sampleDataSets);
            safeSQL.reset();
            sql = "SELECT * FROM sdiworkitem  WHERE keyid1 IN ( " + safeSQL.addIn(this.samples.getColumnValues("keyid1", ";"), ";") + " )";
            this.sampleWorkItems = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            safeSQL.reset();
            sql = "select s_qcbatchsampletypeid, reagentlotid, reagenttypeid, reagenttypeversionid, qcsampletype, standardlevel  from s_qcbatchsampletype where qcbatchid = " + safeSQL.addVar(qcbatchid);
            this.qcBatchSampleTypes = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            String inClauseReagentlotids = "";
            for (int i = 0; i < this.qcBatchSampleTypes.getRowCount(); ++i) {
                String currlot = this.qcBatchSampleTypes.getString(i, "reagentlotid", "");
                if (currlot.length() <= 0) continue;
                if (inClauseReagentlotids.length() > 0) {
                    inClauseReagentlotids = inClauseReagentlotids + ";";
                }
                inClauseReagentlotids = inClauseReagentlotids + currlot;
            }
            if (inClauseReagentlotids.length() > 0) {
                safeSQL.reset();
                sql = "SELECT * from reagentlot WHERE reagentlotid IN ( " + safeSQL.addIn(inClauseReagentlotids, ";") + ")";
                this.reagentLots = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
                safeSQL.reset();
                sql = "SELECT sdcid, keyid1, paramlistid, paramlistversionid, variantid, dataset, sdidataid, s_qcbatchitemid FROM sdidata WHERE sdcid = 'LV_ReagentLot' and keyid1 in ( " + safeSQL.addIn(inClauseReagentlotids, ";") + " )";
                this.reagentLotDataSets = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
                safeSQL.reset();
                sql = "SELECT * FROM sdidataitem WHERE sdcid = 'LV_ReagentLot' and keyid1 in ( " + safeSQL.addIn(inClauseReagentlotids, ";") + " )";
                this.reagentLotDataItems = queryProcessor.getPreparedSqlDataSet(sql, safeSQL.getValues());
            }
        }
    }

    private DataSet getDataItemsForDataSets(QueryProcessor queryProcessor, DataSet sdidata) {
        DataSet dis = new DataSet();
        String sql = "SELECT * FROM sdidataitem WHERE sdcid = ? and keyid1 = ? and paramlistid = ? and paramlistversionid = ?  and variantid = ? and dataset = ?";
        for (int i = 0; i < sdidata.getRowCount(); ++i) {
            DataSet items = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{sdidata.getString(i, "sdcid"), sdidata.getString(i, "keyid1"), sdidata.getString(i, "paramlistid"), sdidata.getString(i, "paramlistversionid"), sdidata.getString(i, "variantid"), sdidata.getInt(i, "dataset")});
            if (items == null || items.getRowCount() <= 0) continue;
            dis.copyRow(items, -1, 1);
        }
        return dis;
    }

    public DataSet getQCBatch() {
        return this.qcBatch;
    }

    public DataSet getQCBatchDataSets() {
        return this.qcBatchDataSets;
    }

    public DataSet getQCBatchSampleTypes() {
        return this.qcBatchSampleTypes;
    }

    public DataSet getQCBatchItems() {
        return this.qcBatchItems;
    }

    public DataSet getQCBatchDataItems() {
        return this.qcBatchDataItems;
    }

    public DataSet getSamples() {
        return this.samples;
    }

    public DataSet getSampleDataSets() {
        return this.sampleDataSets;
    }

    public DataSet getSampleDataItems() {
        return this.sampleDataItems;
    }

    public DataSet getSampleWorkItems() {
        return this.sampleWorkItems;
    }

    public DataSet getReagentLots() {
        return this.reagentLots;
    }

    public DataSet getReagentLotDataSets() {
        return this.reagentLotDataSets;
    }

    public DataSet getReagentLotDataItems() {
        return this.reagentLotDataItems;
    }

    public DataSet getQCBatchDataSet(String paramlistid, String variant) {
        return this.getDataSet(this.qcBatchDataSets, "QCBatch", this.qcBatch.getString(0, "s_qcbatchid"), paramlistid, variant);
    }

    public DataSet getSampleDataSet(String keyid1, String paramlistid, String variant) {
        return this.getDataSet(this.sampleDataSets, "Sample", keyid1, paramlistid, variant);
    }

    public DataSet getSampleDataItem(String keyid1, String paramid, String paramtype, String replicate) {
        return this.getDataItem(this.sampleDataItems, "Sample", keyid1, paramid, paramtype, replicate);
    }

    public DataSet getReagentLotDataItem(String keyid1, String paramid, String paramtype, String replicate) {
        return this.getDataItem(this.reagentLotDataItems, "LV_ReagentLot", keyid1, paramid, paramtype, replicate);
    }

    public DataSet getSampleDataSetDetailsByKey(String dskey) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("sdidataid", dskey);
        return this.sampleDataSets.getFilteredDataSet(filter);
    }

    private DataSet findMatchingDataItemsForDS(String sdidataid, String keyid1, String paramid, String paramtype, String replicate) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdidataid", sdidataid);
        DataSet currentDS = this.sampleDataSets.getFilteredDataSet(filter);
        if (currentDS != null) {
            String paramlistid = currentDS.getString(0, "paramlistid");
            String variantid = currentDS.getString(0, "variantid");
            BigDecimal dataset = currentDS.getBigDecimal(0, "dataset");
            filter = new HashMap();
            filter.put("keyid1", keyid1);
            filter.put("paramlistid", paramlistid);
            filter.put("variantid", variantid);
            filter.put("dataset", dataset);
            filter.put("paramid", paramid);
            filter.put("paramtype", paramtype);
            if (!replicate.equals("*")) {
                filter.put("replicateid", new BigDecimal(replicate));
            }
            DataSet ret = this.sampleDataItems.getFilteredDataSet(filter);
            return ret;
        }
        return new DataSet();
    }

    private DataSet findMatchingDataItemsByAlias(String sdidataid, String keyid1, String paramalias, String paramtype, String replicate) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdidataid", sdidataid);
        DataSet currentDS = this.sampleDataSets.getFilteredDataSet(filter);
        if (currentDS != null) {
            String paramlistid = currentDS.getString(0, "paramlistid");
            String variantid = currentDS.getString(0, "variantid");
            BigDecimal dataset = currentDS.getBigDecimal(0, "dataset");
            filter = new HashMap();
            filter.put("keyid1", keyid1);
            filter.put("paramlistid", paramlistid);
            filter.put("variantid", variantid);
            filter.put("dataset", dataset);
            filter.put("aliasid", paramalias);
            filter.put("paramtype", paramtype);
            if (!replicate.equals("*")) {
                filter.put("replicateid", new BigDecimal(replicate));
            }
            DataSet ret = this.sampleDataItems.getFilteredDataSet(filter);
            return ret;
        }
        return new DataSet();
    }

    public DataSet getSampleDataItemsForPeak(String keyid1, String peakname, String paramtype, String replicate, String[] dskey) {
        int i;
        DataSet retDIs = new DataSet(this.connectionInfo);
        String[] cols = this.sampleDataItems.getColumns();
        for (i = 0; i < cols.length; ++i) {
            retDIs.addColumn(cols[i], this.sampleDataItems.getColumnType(cols[i]));
        }
        for (i = 0; i < dskey.length; ++i) {
            String currsdidataid = dskey[i];
            DataSet dis = this.findMatchingDataItemsByAlias(currsdidataid, keyid1, peakname, paramtype, replicate);
            if (dis != null && dis.getRowCount() > 0) {
                retDIs.copyRow(dis, -1, 1);
                continue;
            }
            dis = this.findMatchingDataItemsForDS(currsdidataid, keyid1, peakname, paramtype, replicate);
            if (dis == null || dis.getRowCount() <= 0) continue;
            retDIs.copyRow(dis, -1, 1);
        }
        return retDIs;
    }

    public DataSet getSampleDataItemsByKeys(String keyid1, String paramid, String paramtype, String replicate, String[] dskey) {
        int i;
        DataSet retDIs = new DataSet(this.connectionInfo);
        String[] cols = this.sampleDataItems.getColumns();
        for (i = 0; i < cols.length; ++i) {
            retDIs.addColumn(cols[i], this.sampleDataItems.getColumnType(cols[i]));
        }
        for (i = 0; i < dskey.length; ++i) {
            String currsdidataid = dskey[i];
            DataSet dis = this.findMatchingDataItemsForDS(currsdidataid, keyid1, paramid, paramtype, replicate);
            if (dis == null || dis.getRowCount() <= 0) continue;
            retDIs.copyRow(dis, -1, 1);
        }
        return retDIs;
    }

    public boolean checkSampleDataItemReleased(String keyid1, String paramlistid, String variantid, String dataset, String paramid, String paramtype, String replicateid) throws SapphireException {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdcid", "Sample");
        filter.put("keyid1", keyid1);
        if (!paramlistid.equals("#")) {
            filter.put("paramlistid", paramlistid);
        }
        if (!variantid.equals("#")) {
            filter.put("variantid", variantid);
        }
        if (!paramid.equals("#")) {
            filter.put("paramid", paramid);
        }
        if (!paramtype.equals("#")) {
            filter.put("paramtype", paramtype);
        }
        filter.put("dataset", new BigDecimal(dataset));
        filter.put("replicateid", new BigDecimal(replicateid));
        DataSet di = this.sampleDataItems.getFilteredDataSet(filter);
        if (di == null || di.getRowCount() != 1) {
            throw new SapphireException("Did not find exactly one matching dataitem");
        }
        return di.getValue(0, "releasedflag", "N").equals("Y");
    }

    private String findDataSetKey(int index, DataSet dataitems) {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("keyid1", dataitems.getString(index, "keyid1"));
        filter.put("paramlistid", dataitems.getString(index, "paramlistid"));
        filter.put("variantid", dataitems.getString(index, "variantid"));
        filter.put("dataset", dataitems.getBigDecimal(index, "dataset"));
        DataSet ds = this.sampleDataSets.getFilteredDataSet(filter);
        return ds.getString(0, "sdidataid");
    }

    public boolean checkQCBatchDataItem(String paramlistid, String variantid, String dataset, String paramid, String paramtype, String replicateid) {
        return this.checkDataItem(this.qcBatchDataItems, "QCBatch", this.qcBatch.getString(0, "s_qcbatchid"), paramlistid, variantid, dataset, paramid, paramtype, replicateid);
    }

    public boolean checkSampleDataItem(String keyid1, String paramlistid, String variantid, String dataset, String paramid, String paramtype, String replicateid) {
        return this.checkDataItem(this.sampleDataItems, this.qcBatch.getString(0, "qcbatchsdcid"), keyid1, paramlistid, variantid, dataset, paramid, paramtype, replicateid);
    }

    public DataSet getSampleDataItem(String keyid1, String paramid, String paramtype) {
        return this.getDataItem(this.sampleDataItems, this.qcBatch.getString(0, "qcbatchsdcid"), keyid1, paramid, paramtype, "1");
    }

    public void addQCBatchDataSet(String paramlistid, String paramlistversionid, String variantid, int currds) {
        if (this.qcBatchDataSets == null) {
            this.qcBatchDataSets = new DataSet(this.connectionInfo);
        }
        int curr = this.qcBatchDataSets.addRow();
        this.qcBatchDataSets.setString(curr, "sdcid", "QCBatch");
        this.qcBatchDataSets.setString(curr, "keyid1", this.qcbatchid);
        this.qcBatchDataSets.setString(curr, "paramlistid", paramlistid);
        this.qcBatchDataSets.setString(curr, "paramlistversionid", paramlistversionid);
        this.qcBatchDataSets.setString(curr, "variantid", variantid);
        this.qcBatchDataSets.setNumber(curr, "dataset", currds);
    }

    public void addSampleDataSet(String keyid1, String paramlistid, String paramlistversionid, String variantid, String currds) {
        if (this.sampleDataSets == null) {
            this.sampleDataSets = new DataSet(this.connectionInfo);
        }
        int curr = this.sampleDataSets.addRow();
        this.sampleDataSets.setString(curr, "sdcid", "Sample");
        this.sampleDataSets.setString(curr, "keyid1", keyid1);
        this.sampleDataSets.setString(curr, "paramlistid", paramlistid);
        this.sampleDataSets.setString(curr, "paramlistversionid", paramlistversionid);
        this.sampleDataSets.setString(curr, "variantid", variantid);
        this.sampleDataSets.setNumber(curr, "dataset", currds);
    }

    public void addQCBatchDataItem(String paramlistid, String paramlistversionid, String variantid, int currds, String paramid, String paramtype, int replicateid) {
        if (this.qcBatchDataItems == null) {
            this.qcBatchDataItems = new DataSet(this.connectionInfo);
        }
        int curr = this.qcBatchDataItems.addRow();
        this.qcBatchDataItems.setString(curr, "sdcid", "QCBatch");
        this.qcBatchDataItems.setString(curr, "keyid1", this.qcbatchid);
        this.qcBatchDataItems.setString(curr, "paramlistid", paramlistid);
        this.qcBatchDataItems.setString(curr, "paramlistversionid", paramlistversionid);
        this.qcBatchDataItems.setString(curr, "variantid", variantid);
        this.qcBatchDataItems.setString(curr, "paramid", paramid);
        this.qcBatchDataItems.setString(curr, "paramtype", paramtype);
        this.qcBatchDataItems.setNumber(curr, "dataset", currds);
        this.qcBatchDataItems.setNumber(curr, "replicateid", replicateid);
    }

    public void addSampleDataItem(String keyid1, String paramlistid, String paramlistversionid, String variantid, int currds, String paramid, String paramtype, int replicateid) {
        if (this.sampleDataItems == null) {
            this.sampleDataItems = new DataSet(this.connectionInfo);
        }
        int curr = this.sampleDataItems.addRow();
        this.sampleDataItems.setString(curr, "sdcid", "Sample");
        this.sampleDataItems.setString(curr, "keyid1", keyid1);
        this.sampleDataItems.setString(curr, "paramlistid", paramlistid);
        this.sampleDataItems.setString(curr, "paramlistversionid", paramlistversionid);
        this.sampleDataItems.setString(curr, "variantid", variantid);
        this.sampleDataItems.setString(curr, "paramid", paramid);
        this.sampleDataItems.setString(curr, "paramtype", paramtype);
        this.sampleDataItems.setNumber(curr, "dataset", currds);
        this.sampleDataItems.setNumber(curr, "replicateid", replicateid);
    }

    private boolean checkDataItem(DataSet dataitems, String sdcid, String keyid1, String paramlistid, String variantid, String dataset, String paramid, String paramtype, String replicateid) {
        if (dataitems == null || dataitems.getRowCount() == 0) {
            return false;
        }
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdcid", sdcid);
        filter.put("keyid1", keyid1);
        if (!paramlistid.equals("#")) {
            filter.put("paramlistid", paramlistid);
        }
        if (!variantid.equals("#")) {
            filter.put("variantid", variantid);
        }
        if (!paramid.equals("#")) {
            filter.put("paramid", paramid);
        }
        if (!paramtype.equals("#")) {
            filter.put("paramtype", paramtype);
        }
        filter.put("dataset", new BigDecimal(dataset));
        filter.put("replicateid", new BigDecimal(replicateid));
        DataSet di = dataitems.getFilteredDataSet(filter);
        return di != null && di.getRowCount() != 0;
    }

    private DataSet getDataSet(DataSet datasets, String sdcid, String keyid1, String paramlistid, String variantid) {
        if (datasets == null || datasets.getRowCount() == 0) {
            return null;
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("sdcid", sdcid);
        filter.put("keyid1", keyid1);
        filter.put("paramlistid", paramlistid);
        filter.put("variantid", variantid);
        return datasets.getFilteredDataSet(filter);
    }

    private DataSet getDataItem(DataSet datasets, String sdcid, String keyid1, String paramid, String paramtype, String replicate) {
        if (datasets == null || datasets.getRowCount() == 0) {
            return null;
        }
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdcid", sdcid);
        filter.put("keyid1", keyid1);
        if (!paramid.equals("#")) {
            filter.put("paramid", paramid);
        }
        if (!paramtype.equals("#")) {
            filter.put("paramtype", paramtype);
        } else {
            filter.put("paramtype", "Standard");
        }
        if (!replicate.equals("*")) {
            filter.put("replicateid", new BigDecimal(replicate));
        }
        return datasets.getFilteredDataSet(filter);
    }

    public DataSet getDynamicallyAddedDataItems(String sdcid, String keyid1, String datasetkeys, String injection, String channel, String handlingReleasedItems) {
        int i;
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("sdcid", sdcid);
        filter.put("keyid1", keyid1);
        filter.put("replicateid", new BigDecimal(injection));
        DataSet dataitemstocheck = this.sampleDataItems.getFilteredDataSet(filter);
        DataSet ret = new DataSet();
        String[] sdidataidlist = StringUtil.split(datasetkeys, ";");
        for (i = 0; i < sdidataidlist.length; ++i) {
            HashMap<String, String> filter2 = new HashMap<String, String>();
            filter2.put("sdidataid", sdidataidlist[i]);
            DataSet sdidatamatch = this.sampleDataSets.getFilteredDataSet(filter2);
            for (int d = 0; d < dataitemstocheck.getRowCount(); ++d) {
                String extref = dataitemstocheck.getString(d, "externalreference", "");
                if (!extref.contains("channel=" + channel) || !extref.contains("added=Y") || !dataitemstocheck.getString(d, "sdcid").equals(sdidatamatch.getString(0, "sdcid")) || !dataitemstocheck.getString(d, "keyid1").equals(sdidatamatch.getString(0, "keyid1")) || !dataitemstocheck.getString(d, "paramlistid").equals(sdidatamatch.getString(0, "paramlistid")) || !dataitemstocheck.getValue(d, "paramlistversionid").equals(sdidatamatch.getValue(0, "paramlistversionid")) || !dataitemstocheck.getString(d, "variantid").equals(sdidatamatch.getString(0, "variantid")) || !dataitemstocheck.getValue(d, "dataset").equals(sdidatamatch.getValue(0, "dataset")) || dataitemstocheck.getString(d, "releasedflag", "N").equals("Y") && !handlingReleasedItems.equals("Override")) continue;
                ret.copyRow(dataitemstocheck, d, 1);
            }
        }
        for (i = 0; i < ret.getRowCount(); ++i) {
            filter = new HashMap();
            filter.put("sdidataitemid", ret.getValue(i, "sdidataitemid"));
            int row = this.sampleDataItems.findRow(filter);
            if (row == -1) continue;
            this.sampleDataItems.deleteRow(row);
        }
        Trace.logDebug("The following items are identified to be pruned:" + ret.toJSONString());
        return ret;
    }

    public String getReagentLotForSample(String qcbatchitemid) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("s_qcbatchitemid", qcbatchitemid);
        DataSet batchitem = this.qcBatchItems.getFilteredDataSet(filter);
        if (batchitem == null || batchitem.getRowCount() == 0) {
            return "";
        }
        String qcbatchsampletypeid = batchitem.getString(0, "qcbatchsampletypeid");
        filter = new HashMap();
        filter.put("s_qcbatchsampletypeid", qcbatchsampletypeid);
        DataSet matchSampleType = this.qcBatchSampleTypes.getFilteredDataSet(filter);
        return matchSampleType.getString(0, "reagentlotid");
    }

    public static String getExternalReference(String project, String database, String sampleSetName) {
        return "project=" + project + ";database=" + database + ";samplesetname=" + sampleSetName;
    }

    public static String getExternalReference(String project, String database, String sampleSetName, String resultSetId, String resultId) {
        return "project=" + project + ";database=" + database + ";samplesetname=" + sampleSetName + ";resultsetid=" + resultSetId + ";resultid=" + resultId;
    }
}

