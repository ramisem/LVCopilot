/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 */
package com.labvantage.opal.beans;

import com.labvantage.opal.beans.BasePageBean;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.stats.Stats;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.Query;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletRequest;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.JstlUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DEChartDataBean
extends BasePageBean {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54170 $";
    public String maxrows;
    public String sampleDataFromRequest;
    public String enteredValueFromRequest;
    public String paramidFromRequest;
    public String masSampleDataFromRequest;
    public String masEnteredValueFromRequest;
    public String masParamidFromRequest;
    public String masStatusFromRequest;
    public String masReplicateFlagsFromRequest;
    public String sdFromRequest;
    public String clFromRequest;
    public String limitParamidFromRequest;
    public DataSet sampleDataSet;
    public DataSet masterQCSampleDataSet;
    public DataSet sampleWithoutReplicatesDataSet;
    public DataSet paramsetDataSet;
    public DataSet replicateFlagDataSet;
    public DataSet sampleAvgFlagDataSet;
    public StringBuffer guiStringBuffer = new StringBuffer();
    private String queryid;
    private String keyid1;
    private String paramlistid;
    private String paramlistversionid;
    private String variantid;
    private String dataset1;
    private String paramid;
    private String paramtype;
    private String samplecount;
    private String errormsg = "";
    private String sysuserid;
    private List statslist = new ArrayList();
    private List masterQCSampleList = new ArrayList();
    private List samplelist = new ArrayList();
    private List missingparam = new ArrayList();
    private HashMap parammap;
    private HashMap statistics;
    private SQLGenerator __SqlGenerator;
    private PropertyList pagedata;

    public void setQueryid(String queryid) {
        this.queryid = queryid;
    }

    @Override
    public void setKeyid1(String keyid1) {
        this.keyid1 = keyid1;
    }

    public void setParamlistid(String paramlistid) {
        this.paramlistid = paramlistid;
    }

    public void setParamlistversionid(String paramlistversionid) {
        this.paramlistversionid = paramlistversionid;
    }

    public void setVariantid(String variantid) {
        this.variantid = variantid;
    }

    public void setDataset(String dataset) {
        this.dataset1 = dataset;
    }

    public void setParamid(String paramid) {
        this.paramid = paramid;
    }

    public void setParamtype(String paramtype) {
        this.paramtype = paramtype;
    }

    public void setSamplecount(String samplecount) {
        this.samplecount = samplecount;
    }

    public void setMaxrows(String maxrows) {
        this.maxrows = maxrows;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean createStats() {
        block27: {
            int count;
            TranslationProcessor tp = new TranslationProcessor(this.pagecontext);
            ServletRequest req = this.pagecontext.getRequest();
            this.__SqlGenerator = SQLFactory.getSqlGenerator(this.pagecontext);
            this.pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", this.pagecontext);
            List paramlist = OpalUtil.stringArrayToList(StringUtil.split(this.paramid, ";"));
            List paramtypelist = OpalUtil.stringArrayToList(StringUtil.split(this.paramtype, ";"));
            LinkedHashMap pmap = new LinkedHashMap();
            if (paramlist.size() == paramtypelist.size()) {
                for (int i = 0; i < paramlist.size(); ++i) {
                    String paramid = (String)paramlist.get(i);
                    if (!pmap.containsKey(paramid)) {
                        pmap.put(paramid, new HashSet());
                    }
                    ((Set)pmap.get(paramid)).add(paramtypelist.get(i));
                }
            }
            PropertyList queryprops = this.pagedata.getPropertyList("queryprops");
            if (this.queryid == null) {
                this.queryid = queryprops.getProperty("queryid");
            }
            if (this.maxrows == null || this.maxrows.length() == 0) {
                this.maxrows = queryprops.getProperty("maxrows");
            }
            if (!this.validateParameters()) {
                Logger.logDebug("Stats creation failed. Invalid Parameters.");
                return false;
            }
            this.parammap = new HashMap();
            PropertyListCollection coll = queryprops.getCollection("extraparam");
            for (int i = 0; i < coll.size(); ++i) {
                PropertyList list = coll.getPropertyList(i);
                String propertyname = list.getProperty("parameter");
                String propertyvalue = list.getProperty("value");
                if (req.getParameter(propertyname) != null) {
                    this.parammap.put("[" + list.getProperty("parameter") + "]", req.getParameter(propertyname));
                    continue;
                }
                this.parammap.put("[" + list.getProperty("parameter") + "]", propertyvalue);
            }
            this.parammap.put("[keyid1]", this.keyid1);
            this.parammap.put("[paramlistid]", this.paramlistid);
            this.parammap.put("[paramlistversionid]", this.paramlistversionid);
            this.parammap.put("[variantid]", this.variantid);
            this.parammap.put("[dataset]", this.dataset1);
            int queryrows = 0;
            try {
                queryrows = Integer.parseInt(this.maxrows);
            }
            catch (Exception list) {
                // empty catch block
            }
            Query query = new Query(this.pagecontext, this.queryid);
            query.setSysuserid(this.getSysuserid());
            DataSet ds = query.getGenericQueryDataset(this.parammap);
            if (ds.size() <= 0) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("queryid", this.queryid);
                this.errormsg = tp.translate("Query [queryid] returned no data.", valueMap);
                Logger.logDebug("Stats Creation failed. No data found.");
                return false;
            }
            int n = count = queryrows == 0 ? ds.size() : queryrows;
            if (count > ds.size()) {
                count = ds.size();
            }
            DAMProcessor damProcessor = new DAMProcessor(this.pagecontext);
            String rsetid = null;
            try {
                String _keyid1 = ds.getColumnValues("keyid1", ";");
                String _paramlistid = ds.getColumnValues("paramlistid", ";");
                String _paramlistversionid = ds.getColumnValues("paramlistversionid", ";");
                String _variantid = ds.getColumnValues("variantid", ";");
                String _dataset = ds.getColumnValues("dataset", ";");
                NumberFormat fmt = NumberFormat.getNumberInstance(HttpUtil.getSessionLocale(this.pagecontext));
                rsetid = damProcessor.createRSetDS(this.getSdcid(), _keyid1, null, null, _paramlistid, _paramlistversionid, _variantid, _dataset, true);
                if (StringUtil.getLen(rsetid) > 0L) {
                    LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                    for (String paramid : pmap.keySet()) {
                        StringBuilder sql = new StringBuilder();
                        sql.append("select s.keyid1, s.paramlistid, s.paramlistversionid, s.variantid, s.dataset, s.paramid, s.paramtype, s.replicateid, s.transformvalue");
                        sql.append(" from sdidataitem s, rsetitemsds r");
                        sql.append(" where s.sdcid = r.sdcid ");
                        sql.append(" and s.keyid1 = r.keyid1");
                        sql.append(" and s.paramlistid = r.paramlistid");
                        sql.append(" and s.paramlistversionid = r.paramlistversionid");
                        sql.append(" and s.variantid = r.variantid");
                        sql.append(" and s.dataset = r.dataset");
                        sql.append(" and s.paramid = '").append(paramid).append("'");
                        sql.append(" and s.replicateid = '1'");
                        sql.append(" and r.rsetid = '").append(rsetid).append("'");
                        sql.append(" order by r.rsetseq");
                        DataSet dsdata = this.getQueryProcessor().getSqlDataSet(sql.toString());
                        if (dsdata == null || dsdata.size() <= 0) continue;
                        dsdata.sort("paramtype");
                        ArrayList<DataSet> list = dsdata.getGroupedDataSets("paramtype");
                        for (int row = 0; row < list.size(); ++row) {
                            dsdata = (DataSet)list.get(row);
                            String paramType = dsdata.getValue(0, "paramtype");
                            if (!((Set)pmap.get(paramid)).contains(paramType)) continue;
                            Stats stats = new Stats();
                            stats.setYparam(paramid + " (" + paramType + ")");
                            for (int i = 0; i < count; ++i) {
                                String key = String.valueOf(i);
                                String keyid = dsdata.getValue(i, "keyid1");
                                stats.addX(keyid);
                                try {
                                    String value = fmt.format(dsdata.getBigDecimal(i, "transformvalue"));
                                    Number number = fmt.parse(value);
                                    stats.addValue(number.doubleValue());
                                    if (map.containsKey(key)) {
                                        map.put(key, map.get(key) + "<hr>" + keyid + " [" + paramid + ":" + dsdata.getValue(i, "paramtype") + ":<b>" + value + "</b>]");
                                        continue;
                                    }
                                    map.put(key, keyid + " [" + paramid + ":" + dsdata.getValue(i, "paramtype") + ":<b>" + value + "</b>]");
                                    continue;
                                }
                                catch (Exception e) {
                                    stats.addValue(9.0E99);
                                    if (map.containsKey(key)) {
                                        map.put(key, map.get(key) + "<hr>" + keyid + " [" + paramid + ":" + dsdata.getValue(i, "paramtype") + "]");
                                        continue;
                                    }
                                    map.put(key, keyid + " [" + paramid + ":" + dsdata.getValue(i, "paramtype") + "]");
                                }
                            }
                            this.statslist.add(stats);
                        }
                    }
                    for (Object o : map.keySet()) {
                        String key = (String)o;
                        this.samplelist.add(map.get(key));
                    }
                }
                this.pagecontext.setAttribute("statslist", (Object)this.statslist);
                if (StringUtil.getLen(rsetid) <= 0L) break block27;
                damProcessor.clearRSet(rsetid);
            }
            catch (SapphireException e) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("queryid", this.queryid);
                this.errormsg = tp.translate("Query [queryid] returned no data.", valueMap);
                Logger.logDebug("Stats Creation failed. No data found.");
                boolean bl = false;
                return bl;
            }
            finally {
                if (StringUtil.getLen(rsetid) > 0L) {
                    damProcessor.clearRSet(rsetid);
                }
            }
        }
        return true;
    }

    public boolean createQCStats() {
        ServletRequest req = this.pagecontext.getRequest();
        String qcBatchSampleTypeId = req.getParameter("qcbatchsampletypeid");
        String qcBatchId = req.getParameter("qcbatchid");
        StringBuffer guiSampleArrayBuffer = new StringBuffer();
        StringBuffer guiSampleDataArrayBuffer = new StringBuffer();
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.pagecontext);
        this.pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", this.pagecontext);
        if (this.maxrows == null || this.maxrows.length() == 0) {
            this.maxrows = req.getParameter("maxrows");
        }
        int queryrows = 0;
        try {
            queryrows = Integer.parseInt(this.maxrows);
        }
        catch (NumberFormatException nfe) {
            queryrows = 20;
        }
        String datasetSQL = "select ds.s_qcbatchid, qcbi.s_qcbatchitemid, ds.keyid1, ds.keyid2, ds.keyid3, sss.createdt, ds.paramlistid, ds.paramlistversionid, ds.VARIANTID, ds.dataset, di.paramid, di.paramtype, di.replicateid, di.enteredvalue, di.transformvalue, di.S_QCEVALSTATUS, qcbi.usersequence from ( select ss.s_sampleid, ss.createdt from ( select distinct s.s_sampleid, s.createdt from s_sample s,  s_qcbatchitem bi, sdidata bids, s_qcbatchsampletype qcbst where s.s_sampleid = bids.keyid1 and bids.sdcid = 'Sample' and bids.s_qcbatchitemid = bi.s_qcbatchitemid and bids.s_qcbatchid = bi.s_qcbatchid and bids.s_qcbatchid IN (select t1.s_qcbatchid from s_qcbatch t1, s_qcbatch t2 where t1.QCMETHODID = t2.QCMETHODID AND t1.CREATEDT <= t2.CREATEDT AND t2.S_QCBATCHID = '" + qcBatchId + "') and bi.qcbatchsampletypeid = qcbst.s_qcbatchsampletypeid and qcbst.qcmethodsampletypeid  = ( select qcmethodsampletypeid from s_qcbatchsampletype where s_qcbatchsampletypeid = '" + qcBatchSampleTypeId + "') order by createdt desc ) ss where rownum <= " + queryrows + " ) sss, sdidata ds, sdidataitem di, s_qcbatchitem qcbi, paramlist pl where ds.sdcid = di.sdcid and ds.sdcid = 'Sample' and ds.keyid1 = sss.s_sampleid and ds.keyid1 = di.keyid1 and ds.keyid2 = di.keyid2 and ds.keyid3 = di.keyid3 and ds.paramlistid = di.paramlistid and ds.paramlistversionid = di.paramlistversionid and ds.variantid = di.variantid and ds.dataset = di.dataset and ds.s_qcbatchitemid = qcbi.s_qcbatchitemid and ds.s_qcbatchid = qcbi.s_qcbatchid and di.paramtype='Standard' and pl.paramlistid = ds.paramlistid and pl.paramlistversionid = ds.paramlistversionid and pl.variantid = ds.variantid and nvl(s_paramlisttype,'XXX') <> 'Preparation' order by ds.keyid1, ds.usersequence, di.usersequence, qcbi.usersequence, di.replicateid ";
        String paramsetSQL = "select S_QCBATCHPARAMSETID, PARAMID, EVALSTATUS, TARGETVALUE, TARGETUNITS, SD from s_qcbatchparamset where s_qcbatchsampletypeid = '" + qcBatchSampleTypeId + "'";
        DataSet ds = this.getQueryProcessor().getSqlDataSet(datasetSQL);
        this.paramsetDataSet = this.getQueryProcessor().getSqlDataSet(paramsetSQL);
        if (ds.size() <= 0) {
            this.errormsg = " Query returned no data.";
            Logger.logDebug("Stats Creation failed. No data found.");
            return false;
        }
        if (ds != null && ds.size() > 0) {
            int i;
            this.sampleDataSet = new DataSet();
            this.masterQCSampleDataSet = new DataSet();
            this.masterQCSampleDataSet.addColumn("KEYID1", 0);
            this.masterQCSampleDataSet.addColumn("PARAMID", 0);
            this.masterQCSampleDataSet.addColumn("ENTEREDVALUE", 0);
            this.masterQCSampleDataSet.addColumn("QCEVALSTATUS", 0);
            this.replicateFlagDataSet = new DataSet();
            this.replicateFlagDataSet.addColumn("FLAG", 0);
            String[] columns = ds.getColumns();
            for (int i2 = 0; i2 < columns.length; ++i2) {
                if (columns[i2].equalsIgnoreCase("S_QCBATCHITEMID") || columns[i2].equalsIgnoreCase("DATASET") || columns[i2].equalsIgnoreCase("ENTEREDVALUE")) continue;
                this.sampleDataSet.addColumn(columns[i2], ds.getColumnType(columns[i2]));
            }
            String lastKeyId1 = "";
            ArrayList<String> localParamTrackList = new ArrayList<String>();
            ArrayList<String> datasetCheckList = new ArrayList<String>();
            ArrayList<String> paramidList = new ArrayList<String>();
            int datasetCount = 0;
            int parameterCount = 0;
            int sampleCount = 0;
            for (i = 0; i < ds.size(); ++i) {
                String keyid1 = ds.getValue(i, "keyid1");
                String paramid = ds.getValue(i, "paramid");
                String paramlistid = ds.getValue(i, "paramlistid");
                String positionValue = ds.getValue(i, "usersequence");
                String paramlistversionid = ds.getValue(i, "paramlistversionid");
                String enteredValue = ds.getValue(i, "transformvalue");
                String evalStatus = "";
                evalStatus = ds.getValue(i, "S_QCEVALSTATUS") == null || ds.getValue(i, "S_QCEVALSTATUS").length() == 0 ? "NULL" : ds.getValue(i, "S_QCEVALSTATUS");
                int rNum = this.masterQCSampleDataSet.addRow();
                this.masterQCSampleDataSet.setValue(rNum, "KEYID1", keyid1);
                this.masterQCSampleDataSet.setValue(rNum, "PARAMID", paramid);
                this.masterQCSampleDataSet.setValue(rNum, "ENTEREDVALUE", enteredValue);
                this.masterQCSampleDataSet.setValue(rNum, "QCEVALSTATUS", evalStatus);
                String value = "";
                if (!lastKeyId1.equals(keyid1)) {
                    if (!lastKeyId1.equals("")) {
                        guiSampleDataArrayBuffer.append(");\n");
                    }
                    guiSampleDataArrayBuffer.append("samplearray[").append(++sampleCount).append("] = new Array (");
                    guiSampleDataArrayBuffer.append("'").append(ds.getValue(i, "s_qcbatchid")).append("'");
                    guiSampleDataArrayBuffer.append(",'").append(keyid1).append("'");
                    guiSampleDataArrayBuffer.append(",'").append(positionValue).append("'");
                    guiSampleDataArrayBuffer.append(",'").append(ds.getValue(i, "createdt")).append("'");
                    localParamTrackList.clear();
                    lastKeyId1 = keyid1;
                }
                if (lastKeyId1.equals(ds.getValue(0, "keyid1")) && !datasetCheckList.contains(paramlistid + paramlistversionid)) {
                    datasetCheckList.add(paramlistid + paramlistversionid);
                    this.guiStringBuffer.append("datasetarray[").append(datasetCount).append("] = new Array ('").append(paramlistid).append("','").append(paramlistversionid).append("');\n");
                    ++datasetCount;
                }
                if (localParamTrackList.contains(paramid)) continue;
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("keyid1", keyid1);
                map.put("paramid", paramid);
                int rNum2 = this.replicateFlagDataSet.addRow();
                if (ds.findRow(map, i + 1) != -1) {
                    value = this.getAverageValue(i, keyid1, paramid, ds);
                    this.replicateFlagDataSet.setValue(rNum2, "FLAG", "AVG");
                } else {
                    value = ds.getValue(i, "transformvalue");
                    this.replicateFlagDataSet.setValue(rNum2, "FLAG", "REP");
                }
                int rowNum = this.sampleDataSet.addRow();
                for (int j = 0; j < columns.length; ++j) {
                    if (!(columns[j].equalsIgnoreCase("S_QCBATCHITEMID") || columns[j].equalsIgnoreCase("DATASET") || columns[j].equalsIgnoreCase("ENTEREDVALUE") || columns[j].equals("transformvalue"))) {
                        this.sampleDataSet.setValue(rowNum, columns[j], ds.getValue(i, columns[j]));
                        continue;
                    }
                    if (!columns[j].equals("transformvalue")) continue;
                    this.sampleDataSet.setValue(rowNum, columns[j], value);
                }
                guiSampleDataArrayBuffer.append(",'").append(value).append("'");
                localParamTrackList.add(paramid);
                if (!lastKeyId1.equals(ds.getValue(0, "keyid1"))) continue;
                this.guiStringBuffer.append("parammeterarray[").append(parameterCount).append("] = new Array ('").append(paramlistid).append("(").append(paramlistversionid).append(")','").append(paramid).append("');\n");
                paramidList.add(paramid);
                ++parameterCount;
            }
            guiSampleDataArrayBuffer.append(");\n");
            guiSampleArrayBuffer.append("samplearray[").append(0).append("] = new Array (");
            guiSampleArrayBuffer.append("'qcbatchid','sampleid','position','createdate'");
            for (i = 0; i < paramidList.size(); ++i) {
                guiSampleArrayBuffer.append(",'").append((String)paramidList.get(i)).append("'");
            }
            guiSampleArrayBuffer.append(");\n");
            this.guiStringBuffer.append("\n").append(guiSampleArrayBuffer.toString()).append("\n").append(guiSampleDataArrayBuffer.toString());
            this.sampleDataSet.showData();
        }
        this.masterQCSampleDataSet.showData();
        this.statistics = new HashMap();
        ArrayList<String> paramidArray = new ArrayList<String>();
        double sd = 9.0E99;
        double cl = 9.0E99;
        DataSet ds2 = new DataSet();
        ds2.addColumnValues("KEYID1", 0, this.sampleDataSet.getColumnValues("KEYID1", ";"), ";");
        ds2.addColumnValues("ENTEREDVALUE", 0, this.sampleDataSet.getColumnValues("transformvalue", ";"), ";");
        ds2.addColumnValues("PARAMID", 0, this.sampleDataSet.getColumnValues("PARAMID", ";"), ";");
        String paramIdsTemp = this.paramsetDataSet.getColumnValues("PARAMID", ";");
        String[] paramIds = StringUtil.split(paramIdsTemp, ";");
        paramidArray.addAll(Arrays.asList(paramIds));
        NumberFormat fmt = NumberFormat.getNumberInstance(HttpUtil.getSessionLocale(this.pagecontext));
        if (ds2.size() > 0) {
            int paramsize = paramidArray.size();
            int ds2size = ds2.size();
            for (int x = 0; x < paramsize; ++x) {
                String paramid = (String)paramidArray.get(x);
                Stats stats = new Stats();
                stats.setYparam(paramid);
                if (sd != 9.0E99) {
                    stats.setSD(sd);
                }
                if (cl != 9.0E99) {
                    stats.setCL(cl);
                }
                for (int i = 0; i < ds2size; ++i) {
                    if (!ds2.getValue(i, "PARAMID").equals(paramid)) continue;
                    stats.addX(ds2.getValue(i, "KEYID1"));
                    try {
                        Number number = fmt.parse(ds2.getValue(i, "ENTEREDVALUE"));
                        stats.addValue(number.doubleValue());
                        continue;
                    }
                    catch (Exception e) {
                        stats.addValue(9.0E99);
                    }
                }
                double tempCL = stats.getCL();
                double tempSD = stats.getSD();
                String strCL = String.valueOf(tempCL);
                String strSD = String.valueOf(tempSD);
                String effectiveStats = strCL + ":" + strSD;
                this.statistics.put(stats.getYparam(), effectiveStats);
            }
        }
        return true;
    }

    public String getAverageValue(int rowNum, String keyid1, String paramid, DataSet ds) {
        double values = 0.0;
        double replicateCount = 0.0;
        for (int j = rowNum; j < ds.size() && ds.getValue(j, "keyid1").equals(keyid1); ++j) {
            if (!paramid.equals(ds.getValue(j, "PARAMID"))) continue;
            try {
                values += Double.parseDouble(ds.getValue(j, "transformvalue"));
                replicateCount += 1.0;
                continue;
            }
            catch (NumberFormatException numberFormatException) {
                continue;
            }
            catch (NullPointerException nullPointerException) {
                // empty catch block
            }
        }
        return Double.toString(values / replicateCount);
    }

    public boolean parseQCStats() {
        int i;
        Stats stats;
        String paramid;
        int x;
        long current = System.currentTimeMillis();
        ArrayList<String> paramidArray = new ArrayList<String>();
        ServletRequest req = this.pagecontext.getRequest();
        double sd = 9.0E99;
        double cl = 9.0E99;
        try {
            sd = Double.parseDouble(req.getParameter("sd"));
            cl = Double.parseDouble(req.getParameter("cl"));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        DataSet sampleDS = new DataSet();
        sampleDS.addColumnValues("KEYID1", 0, this.sampleDataFromRequest, ";");
        sampleDS.addColumnValues("ENTEREDVALUE", 0, this.enteredValueFromRequest, ";");
        sampleDS.addColumnValues("PARAMID", 0, this.paramidFromRequest, ";");
        DataSet masterDS = new DataSet();
        masterDS.addColumnValues("KEYID1", 0, this.masSampleDataFromRequest, ";");
        masterDS.addColumnValues("ENTEREDVALUE", 0, this.masEnteredValueFromRequest, ";");
        masterDS.addColumnValues("PARAMID", 0, this.masParamidFromRequest, ";");
        masterDS.addColumnValues("QCEVALSTATUS", 0, this.masStatusFromRequest, ";");
        DataSet replicateFlagDS = new DataSet();
        replicateFlagDS.addColumnValues("FLAG", 0, this.masReplicateFlagsFromRequest, ";");
        replicateFlagDS.showData();
        String[] paramIds = StringUtil.split(this.paramid, ";");
        paramidArray.addAll(Arrays.asList(paramIds));
        masterDS.showData();
        if (masterDS.size() > 0) {
            for (x = 0; x < paramidArray.size(); ++x) {
                paramid = (String)paramidArray.get(x);
                stats = new Stats();
                stats.setYparam(paramid);
                if (sd != 9.0E99) {
                    stats.setSD(sd);
                }
                if (cl != 9.0E99) {
                    stats.setCL(cl);
                }
                for (i = 0; i < masterDS.size(); ++i) {
                    if (!masterDS.getValue(i, "PARAMID").equals(paramid)) continue;
                    try {
                        stats.addValue(Double.parseDouble(masterDS.getValue(i, "ENTEREDVALUE")));
                        stats.addX(masterDS.getValue(i, "KEYID1"));
                        stats.setEvalStatus(masterDS.getValue(i, "QCEVALSTATUS"));
                        continue;
                    }
                    catch (Exception e) {
                        stats.addValue(9.0E99);
                        stats.addX(masterDS.getValue(i, "KEYID1"));
                        stats.setEvalStatus(masterDS.getValue(i, "QCEVALSTATUS"));
                        Logger.logError(e.getMessage());
                    }
                }
                this.masterQCSampleList.add(stats);
            }
        }
        if (sampleDS.size() > 0) {
            for (x = 0; x < paramidArray.size(); ++x) {
                paramid = (String)paramidArray.get(x);
                stats = new Stats();
                stats.setYparam(paramid);
                if (sd != 9.0E99) {
                    stats.setSD(sd);
                }
                if (cl != 9.0E99) {
                    stats.setCL(cl);
                }
                for (i = 0; i < sampleDS.size(); ++i) {
                    if (!sampleDS.getValue(i, "PARAMID").equals(paramid)) continue;
                    try {
                        stats.addValue(Double.parseDouble(sampleDS.getValue(i, "ENTEREDVALUE")));
                        stats.addX(sampleDS.getValue(i, "KEYID1"));
                        stats.setEvalStatus(sampleDS.getValue(i, "QCEVALSTATUS"));
                        continue;
                    }
                    catch (Exception e) {
                        stats.addValue(9.0E99);
                        stats.addX(sampleDS.getValue(i, "KEYID1"));
                        stats.setEvalStatus(sampleDS.getValue(i, "QCEVALSTATUS"));
                        Logger.logError(e.getMessage());
                    }
                }
                this.statslist.add(stats);
            }
        }
        this.pagecontext.setAttribute("statslist", (Object)this.statslist);
        this.pagecontext.setAttribute("masterQCSampleList", (Object)this.masterQCSampleList);
        Logger.logDebug("Stats creation complete. Took " + (System.currentTimeMillis() - current) + " ms.");
        return true;
    }

    public boolean validateParameters() {
        boolean ret = true;
        TranslationProcessor tp = new TranslationProcessor(this.pagecontext);
        this.errormsg = tp.translate("Missing Request Parameter:") + "<br>";
        if (this.queryid == null || this.queryid.trim().length() == 0) {
            this.errormsg = this.errormsg + "queryid<br>";
            this.missingparam.add("queryid");
            ret = false;
        }
        if (this.paramlistid == null || this.paramlistid.trim().length() == 0) {
            this.errormsg = this.errormsg + "paramlistid<br>";
            this.missingparam.add("paramlistid");
            ret = false;
        }
        if (this.paramlistversionid == null || this.paramlistversionid.trim().length() == 0) {
            this.errormsg = this.errormsg + "paramlistversionid<br>";
            this.missingparam.add("paramlistversionid");
            ret = false;
        }
        if (this.variantid == null || this.variantid.trim().length() == 0) {
            this.errormsg = this.errormsg + "variantid<br>";
            this.missingparam.add("variantid");
            ret = false;
        }
        if (this.dataset1 == null || this.dataset1.trim().length() == 0) {
            this.errormsg = this.errormsg + "dataset<br>";
            this.missingparam.add("dataset");
            ret = false;
        }
        if (this.paramid == null || this.paramid.trim().length() == 0) {
            this.errormsg = this.errormsg + "paramid<br>";
            this.missingparam.add("paramid");
            ret = false;
        }
        if (this.paramtype == null || this.paramtype.trim().length() == 0) {
            this.errormsg = this.errormsg + "paramtype";
            this.missingparam.add("paramtype");
            ret = false;
        }
        return ret;
    }

    public DataSet getDataItems() {
        SafeSQL safeSQL = this.__SqlGenerator.getDataItems(this.keyid1, this.paramlistid, this.paramlistversionid, this.variantid, this.dataset1);
        return this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
    }

    public List getTrendQueryList() {
        ArrayList<Cloneable> querylist = new ArrayList<Cloneable>();
        ArrayList<String> idlist = new ArrayList<String>();
        DataSet ds = this.getQueryProcessor().getSqlDataSet(this.__SqlGenerator.getQueryArgsForTrendChartQuery());
        if (ds.size() > 0) {
            for (int i = 0; i < ds.size(); ++i) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("queryid", ds.getValue(i, "queryid"));
                map.put("argid", ds.getValue(i, "argid"));
                map.put("arginto", ds.getValue(i, "arginto"));
                querylist.add(map);
                idlist.add(ds.getValue(i, "queryid"));
            }
            querylist.add(idlist);
        }
        return querylist;
    }

    public String getQueryPanel() {
        TranslationProcessor tp = new TranslationProcessor(this.pagecontext);
        long current = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width='200'>");
        sb.append("<tr><td class='gridmaint_fieldtitle'>");
        sb.append(tp.translate("Query ID"));
        sb.append("</td></tr>");
        sb.append("<tr><td class='gridmaint_field' align=right>");
        sb.append(this.queryid);
        sb.append("</td></tr>");
        sb.append("<tr><td height=5></td></tr>");
        sb.append("<tr><td class='gridmaint_fieldtitle'>");
        sb.append(tp.translate("Query Arguments"));
        sb.append("</td></tr>");
        SafeSQL safeSQL = this.__SqlGenerator.getQueryArgsForQuery(this.queryid);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        for (int i = 0; i < ds.size(); ++i) {
            sb.append("<tr><td class='gridmaint_field'><I>");
            sb.append(ds.getValue(i, "argid"));
            sb.append("</I></td></tr>");
            sb.append("<tr><td class='gridmaint_field' align='right'>");
            String a = ds.getValue(i, "arginto");
            if (a.indexOf("keyid1") == -1 && a.indexOf("paramlistid") == -1 && a.indexOf("paramlistversionid") == -1 && a.indexOf("variantid") == -1 && a.indexOf("dataset") == -1) {
                String queryparam = a.substring(1, a.length() - 1);
                String paramvalue = this.pagecontext.getRequest().getParameter(queryparam);
                sb.append("<input size=15 value='");
                if (paramvalue != null) {
                    sb.append(paramvalue);
                } else {
                    sb.append(this.parammap.get(a));
                }
                sb.append("' id='").append(queryparam).append("' name='").append(queryparam).append("'>");
            } else {
                sb.append(OpalUtil.parseRequestString(this.pagecontext, a));
            }
            sb.append("</td></tr>");
            sb.append("<tr><td height=5 class='gridmaint_field'></td></tr>");
        }
        String rows = this.pagecontext.getRequest().getParameter("maxrows");
        if (rows == null) {
            rows = this.maxrows;
        }
        sb.append("<tr><td class='gridmaint_field'><I>");
        sb.append("Rows");
        sb.append("</I></td></tr>");
        sb.append("<tr><td class='gridmaint_field' align='right'>");
        sb.append("<input size=15 value='");
        sb.append(rows).append("' name='maxrows'>");
        sb.append("</td></tr>");
        sb.append("</table>");
        Logger.logDebug("Query Panel complete. Took " + (System.currentTimeMillis() - current) + " ms.");
        return sb.toString();
    }

    public String getDataPoints() {
        StringBuffer sb = new StringBuffer();
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width='190'>");
        for (int i = 0; i < this.samplelist.size(); ++i) {
            sb.append("<tr><td class='gridmaint_field'>");
            sb.append(i + 1).append(".");
            sb.append("</td><td class='gridmaint_field'>");
            sb.append(this.samplelist.get(i));
            sb.append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    public String getQCDataPoints() {
        StringBuffer guiParamsetArrayBuffer = new StringBuffer();
        if (this.paramsetDataSet != null && this.paramsetDataSet.size() > 0) {
            for (int i = 0; i < this.paramsetDataSet.size(); ++i) {
                String paramsetId = this.paramsetDataSet.getString(i, "S_QCBATCHPARAMSETID");
                String paramId = this.paramsetDataSet.getString(i, "PARAMID");
                String qvalStatus = this.paramsetDataSet.getString(i, "EVALSTATUS");
                String targetValue = this.paramsetDataSet.getString(i, "TARGETVALUE");
                String targetUnits = this.paramsetDataSet.getString(i, "TARGETUNITS");
                String sd = this.paramsetDataSet.getString(i, "SD");
                guiParamsetArrayBuffer.append("paramsets[").append(i).append("] = new Array(\"");
                guiParamsetArrayBuffer.append(paramsetId).append("\",\"");
                guiParamsetArrayBuffer.append(paramId).append("\",\"");
                guiParamsetArrayBuffer.append(qvalStatus).append("\",\"");
                guiParamsetArrayBuffer.append(targetValue).append("\",\"");
                guiParamsetArrayBuffer.append(targetUnits).append("\",\"");
                guiParamsetArrayBuffer.append(sd).append("\");\n");
            }
        }
        return this.guiStringBuffer.toString() + "\n" + guiParamsetArrayBuffer.toString() + "\n";
    }

    public String getQCDataPoints2() {
        StringBuffer guiParamsetArrayBuffer2 = new StringBuffer();
        if (this.statistics != null && this.statistics.size() > 0) {
            Iterator iter = this.statistics.keySet().iterator();
            int count = 0;
            while (iter.hasNext()) {
                Object key = iter.next();
                Object value = this.statistics.get(key);
                guiParamsetArrayBuffer2.append("paramsets2[").append(count).append("] = new Array(\"");
                guiParamsetArrayBuffer2.append(String.valueOf(key)).append("\",\"");
                guiParamsetArrayBuffer2.append(String.valueOf(value)).append("\",\"");
                guiParamsetArrayBuffer2.append("\");\n");
                ++count;
            }
        }
        return guiParamsetArrayBuffer2.toString();
    }

    public PropertyList getPagedata() {
        return this.pagedata;
    }

    public void setPagedata(PropertyList parPagedata) {
        this.pagedata = parPagedata;
    }

    public List getMissingparam() {
        return this.missingparam;
    }

    public String getErrormsg() {
        return this.errormsg;
    }

    public String getSysuserid() {
        return this.sysuserid;
    }

    public void setSysuserid(String sysuserid) {
        this.sysuserid = sysuserid;
    }

    public String getSamplecount() {
        return this.samplecount;
    }
}

