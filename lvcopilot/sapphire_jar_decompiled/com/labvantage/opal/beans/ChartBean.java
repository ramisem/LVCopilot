/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 */
package com.labvantage.opal.beans;

import com.labvantage.opal.beans.BasePageBean;
import com.labvantage.opal.qcbatch.QCBatch;
import com.labvantage.opal.qcbatch.QCBatchPool;
import com.labvantage.opal.qcbatch.QCBatchSampleType;
import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.stats.Stats;
import com.labvantage.opal.stats.StatsList;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.util.Query;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import javax.servlet.ServletRequest;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ChartBean
extends BasePageBean {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 89929 $";
    private ArrayList __ParameterIDS;
    private DataSet __ParamsetDataSet;
    private HashMap __ChartingHashmap;
    private HashMap __ParamMap;
    private List __MissingParam;
    private List __SampleList = new ArrayList();
    private PropertyList __PageData;
    private PropertyList __ChartElementProperties;
    private transient SQLGenerator __SqlGenerator;
    private String __ClFromRequest;
    private String __DataSet;
    private String __ErrorMsg = "";
    private String __KeyId1;
    private String __MaxRows;
    private String __ParamListID;
    private String __ParamListVersionID;
    private String __ParamID;
    private String __ParamType;
    private String __QueryID;
    private String __SysUserID;
    private String __SdFromRequest;
    private String __VariantID;
    private StringBuffer __GuiStringBuffer;
    private String __ParameterTypes;
    private String __SqlQuery;
    private static final int MAX_QUERY_COUNT = 20;
    private static final int CONSTANT_VALUE = 3;
    private String __qcBatchSampleTypeId;
    private String __qcBatchId;
    private String __qcSampleType;

    public ChartBean() {
        this.__MissingParam = new ArrayList();
        this.__ParameterIDS = new ArrayList();
        this.__ChartingHashmap = new HashMap();
        this.__GuiStringBuffer = new StringBuffer();
    }

    public ChartBean(String sqlQuery) {
        this.__MissingParam = new ArrayList();
        this.__ParameterIDS = new ArrayList();
        this.__ChartingHashmap = new HashMap();
        this.__GuiStringBuffer = new StringBuffer();
        this.__SqlQuery = sqlQuery;
    }

    public void setQueryid(String queryid) {
        this.__QueryID = queryid;
    }

    @Override
    public void setKeyid1(String keyid1) {
        this.__KeyId1 = keyid1;
    }

    public void setParamlistid(String paramlistid) {
        this.__ParamListID = paramlistid;
    }

    public void setParamlistversionid(String paramlistversionid) {
        this.__ParamListVersionID = paramlistversionid;
    }

    public void setVariantid(String variantid) {
        this.__VariantID = variantid;
    }

    public void setDataset(String dataset) {
        this.__DataSet = dataset;
    }

    public void setParamid(String paramid) {
        this.__ParamID = paramid;
    }

    public void setParamtype(String paramtype) {
        this.__ParamType = paramtype;
    }

    public void setMaxrows(String maxrows) {
        this.__MaxRows = maxrows;
    }

    public String getMaxrows() {
        return this.__MaxRows;
    }

    public boolean createStats() {
        int queryrows;
        long current = System.currentTimeMillis();
        TranslationProcessor tp = new TranslationProcessor(this.pagecontext);
        Logger.logDebug("[Chart Bean] Creating Chart Stats...");
        ServletRequest req = this.pagecontext.getRequest();
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.pagecontext);
        this.__PageData = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", this.pagecontext);
        PropertyList queryprops = this.__PageData.getPropertyList("queryprops");
        if (this.__QueryID == null) {
            this.__QueryID = queryprops.getProperty("queryid");
        }
        if (this.__MaxRows == null || this.__MaxRows.length() == 0) {
            this.__MaxRows = queryprops.getProperty("maxrows");
        }
        if (!this.validateParameters()) {
            Logger.logDebug("Stats creation failed. Invalid Parameters.");
            return false;
        }
        this.__ParamMap = new HashMap();
        PropertyListCollection coll = queryprops.getCollection("extraparam");
        for (int i = 0; i < coll.size(); ++i) {
            PropertyList list = coll.getPropertyList(i);
            String propertyname = list.getProperty("parameter");
            String propertyvalue = list.getProperty("value");
            if (req.getParameter(propertyname) != null) {
                this.__ParamMap.put("[" + list.getProperty("parameter") + "]", req.getParameter(propertyname));
                continue;
            }
            this.__ParamMap.put("[" + list.getProperty("parameter") + "]", propertyvalue);
        }
        this.__ParamListID = this.__ParamListID.replace('^', '%');
        this.__ParamMap.put("[keyid1]", this.__KeyId1);
        this.__ParamMap.put("[paramlistid]", this.__ParamListID);
        this.__ParamMap.put("[paramlistversionid]", this.__ParamListVersionID);
        this.__ParamMap.put("[variantid]", this.__VariantID);
        this.__ParamMap.put("[dataset]", this.__DataSet);
        try {
            queryrows = Integer.parseInt(this.__MaxRows);
        }
        catch (Exception e) {
            queryrows = 20;
        }
        Query query = new Query(this.pagecontext, this.__QueryID);
        query.setSysuserid(this.getSysuserid());
        long q = System.currentTimeMillis();
        DataSet ds = query.getGenericQueryDataset(this.__ParamMap);
        Logger.logDebug("[Chart Bean] Query results took " + (System.currentTimeMillis() - q) + " ms.");
        if (ds.size() <= 0) {
            HashMap<String, String> valueMap = new HashMap<String, String>();
            valueMap.put("queryid", this.__QueryID);
            this.__ErrorMsg = tp.translate("Query [queryid] returned no data.", valueMap);
            Logger.logDebug("[Chart Bean] Stats Creation failed. No data found.");
            return false;
        }
        StringBuffer sb = new StringBuffer();
        int comp = queryrows == 0 ? ds.size() : queryrows;
        for (int i = 0; i < comp; ++i) {
            String sampleid = ds.getValue(i, "keyid1");
            this.__SampleList.add(sampleid);
            sb.append(sampleid);
            sb.append(ds.getValue(i, "paramlistid"));
            sb.append(ds.getValue(i, "paramlistversionid"));
            sb.append(ds.getValue(i, "variantid"));
            sb.append(ds.getValue(i, "dataset"));
            sb.append("','");
        }
        if (sb.length() > 3) {
            sb.delete(sb.length() - 3, sb.length());
        }
        String evalStatus = "";
        String[] paramidArray = StringUtil.split(this.__ParamID, ";");
        String[] paramTypeArray = StringUtil.split(this.__ParamType, ";");
        this.__ParameterIDS = new ArrayList();
        ArrayList<String> paramidAndTypeList = new ArrayList<String>();
        String lastParamid = "";
        String lastParamidType = "";
        for (int i = 0; i < paramidArray.length; ++i) {
            String paramid = "";
            String t1 = paramidArray[i];
            paramid = t1 = t1.replace('^', '%');
            String paramType = "";
            String t2 = paramTypeArray[i];
            paramType = t2 = t2.replace('^', '%');
            if (lastParamid.equals(paramid) && lastParamidType.equals(paramType)) continue;
            String combined = paramid + ":" + paramType;
            paramidAndTypeList.add(combined);
            lastParamid = paramid;
            lastParamidType = paramType;
        }
        int calculated_size = paramidAndTypeList.size();
        for (int i = 0; i < calculated_size; ++i) {
            paramidArray[i] = "";
            paramTypeArray[i] = "";
        }
        int k = 0;
        for (int j = 0; j < calculated_size; ++j) {
            String temp = (String)paramidAndTypeList.get(j);
            String temp1 = temp.substring(0, temp.indexOf(":"));
            String temp2 = temp.substring(temp.indexOf(":") + 1, temp.length());
            paramidArray[k] = temp1;
            paramTypeArray[k] = temp2;
            ++k;
        }
        StringBuffer _sb = new StringBuffer();
        for (int i = 0; i < calculated_size; ++i) {
            _sb.append(paramidArray[i] + paramTypeArray[i] + ";");
        }
        _sb.deleteCharAt(_sb.length() - 1);
        SafeSQL safeSQL = this.__SqlGenerator.getSdiDataitemForTypeAndDs(sb.toString(), StringUtil.replaceAll(_sb.toString(), ";", "','"));
        ds = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        DataSet ds2 = new DataSet();
        String lastkeyid = "";
        for (int i = 0; i < this.__SampleList.size(); ++i) {
            String keyid = (String)this.__SampleList.get(i);
            if (!keyid.equals(lastkeyid)) {
                for (int j = 0; j < ds.size(); ++j) {
                    if (!ds.getValue(j, "keyid1").equals(keyid)) continue;
                    ds2.copyRow(ds, j, 1);
                }
            }
            lastkeyid = keyid;
        }
        NumberFormat fmt = NumberFormat.getNumberInstance((Locale)this.pagecontext.getAttribute("locale"));
        if (ds2.size() > 0) {
            for (int x = 0; x < calculated_size; ++x) {
                this.__ParameterIDS.add(paramidArray[x] + "(" + paramTypeArray[x] + ")");
                String paramid = paramidArray[x];
                String paramType = paramTypeArray[x];
                String keyid1 = "";
                int maxNumberOfReplicates = this.getMaxReplicatesTrend(paramid, paramType, ds2);
                StatsList paramStatsList = new StatsList();
                paramStatsList.setStatsListParamid(paramid + "(" + paramType + ")");
                Stats stats_0 = new Stats();
                stats_0.setYparam(paramid + "(" + paramType + ")");
                TreeSet<String> set = new TreeSet<String>();
                for (int i = 0; i < ds2.size(); ++i) {
                    if (!ds2.getValue(i, "PARAMID").equals(paramid) || !ds2.getValue(i, "PARAMTYPE").equals(paramType)) continue;
                    keyid1 = ds2.getValue(i, "KEYID1");
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("KEYID1", keyid1);
                    map.put("PARAMID", paramid);
                    map.put("PARAMTYPE", paramType);
                    if (set.contains(map.toString())) continue;
                    set.add(map.toString());
                    double transformValue = 9.0E99;
                    String displayValue = "";
                    evalStatus = "PASS";
                    transformValue = this.getAverageTransformValueTrend(i, keyid1, paramid, paramType, ds2).doubleValue();
                    displayValue = String.valueOf(this.getAverageDisplayValueTrend(i, keyid1, paramid, ds2));
                    stats_0.addX(keyid1 != null ? keyid1 + "$" + displayValue : "");
                    stats_0.addValue(transformValue != 9.0E99 ? transformValue : 9.0E99);
                    stats_0.addTransformValue(transformValue != 9.0E99 ? transformValue : 9.0E99);
                    stats_0.setEvalStatus(evalStatus != null ? evalStatus : "");
                }
                paramStatsList.add(0, stats_0);
                for (int replicateIndex = 1; replicateIndex <= maxNumberOfReplicates; ++replicateIndex) {
                    double repTransformValue = 9.0E99;
                    String repDisplayValue = "";
                    Stats statsRep = new Stats();
                    statsRep.setYparam(paramid + "(" + paramType + ")");
                    TreeSet<String> set1 = new TreeSet<String>();
                    for (int l = 0; l < ds2.size(); ++l) {
                        if (!ds2.getValue(l, "PARAMID").equals(paramid) || !ds2.getValue(l, "PARAMTYPE").equals(paramType)) continue;
                        String keyid11 = ds2.getValue(l, "KEYID1");
                        String replicateid1 = ds2.getValue(l, "REPLICATEID");
                        HashMap<String, String> map2 = new HashMap<String, String>();
                        map2.put("KEYID1", keyid11);
                        map2.put("PARAMID", paramid);
                        map2.put("PARAMTYPE", paramType);
                        if (replicateIndex != Integer.valueOf(replicateid1) || set1.contains(map2.toString())) continue;
                        set1.add(map2.toString());
                        evalStatus = "PASS";
                        try {
                            double d = repTransformValue = ds2.getBigDecimal(l, "TRANSFORMVALUE") != null ? ds2.getBigDecimal(l, "TRANSFORMVALUE").doubleValue() : 9.0E99;
                            if (ds2.getValue(l, "DISPLAYVALUE") != null) {
                                try {
                                    repDisplayValue = ds2.getValue(l, "DISPLAYVALUE");
                                }
                                catch (Exception ex) {
                                    repDisplayValue = "    ";
                                }
                            } else {
                                repDisplayValue = "    ";
                            }
                        }
                        catch (Exception e) {
                            Trace.logError("ERROR in Chart bean.java at line 617", e);
                        }
                        statsRep.addX(keyid11 != null ? keyid11 + "$" + repDisplayValue : "");
                        statsRep.addValue(repTransformValue != 9.0E99 ? repTransformValue : 9.0E99);
                        statsRep.addTransformValue(repTransformValue != 9.0E99 ? repTransformValue : 9.0E99);
                        statsRep.setEvalStatus(evalStatus != null ? evalStatus : String.valueOf(9.0E99));
                    }
                    paramStatsList.add(replicateIndex, statsRep);
                }
                paramStatsList.getOverallMax();
                paramStatsList.getOverallMin();
                paramStatsList.getOverallCL();
                paramStatsList.getOverallLCL();
                paramStatsList.getOverallSD();
                paramStatsList.getOverallUCL();
                this.__ChartingHashmap.put(paramid + "(" + paramType + ")", paramStatsList);
            }
            Logger.logDebug("[ChartBean] StatsList creation complete. Took " + (System.currentTimeMillis() - current) + " ms.");
        }
        this.pagecontext.setAttribute("ChartingHashmap", (Object)this.__ChartingHashmap);
        this.pagecontext.setAttribute("paramids", (Object)this.__ParameterIDS);
        Logger.logDebug("[ChartBean] Stats creation complete. Took " + (System.currentTimeMillis() - current) + " ms.");
        return true;
    }

    public boolean createQCStats() {
        long current = System.currentTimeMillis();
        ServletRequest req = this.pagecontext.getRequest();
        String qcBatchSampleTypeId = req.getParameter("qcbatchsampletypeid");
        String qcSampleType = req.getParameter("qcsampletype");
        String qcBatchId = req.getParameter("qcbatchid");
        this.__qcBatchSampleTypeId = qcBatchSampleTypeId;
        this.__qcSampleType = qcSampleType;
        this.__qcBatchId = qcBatchId;
        DataSet ds = null;
        Logger.logDebug("[Chart Bean] Creating QC Chart Stats...");
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.pagecontext);
        this.__PageData = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", this.pagecontext);
        this.__ChartElementProperties = (PropertyList)JstlUtil.evaluateExpression("${chart}", this.pagecontext);
        PropertyList queryprops = this.__PageData.getPropertyList("queryprops");
        QueryProcessor qp = this.getQueryProcessor();
        long q = System.currentTimeMillis();
        if (this.__SqlQuery == null || this.__SqlQuery.length() == 0) {
            try {
                if (this.__QueryID == null) {
                    this.__QueryID = queryprops.getProperty("queryid");
                }
            }
            catch (Exception ex) {
                Logger.logDebug("[Chart Bean] ############ __QueryID IS NULL ############>");
            }
            if (this.__MaxRows == null || this.__MaxRows.length() == 0) {
                this.__MaxRows = req.getParameter("maxrows");
            }
            int queryrows = 0;
            try {
                queryrows = Integer.parseInt(this.__MaxRows);
            }
            catch (NumberFormatException nfe) {
                queryrows = 20;
            }
            QCBatch qcBatch = QCBatchPool.getQCBatch(qp, qcBatchId);
            String qcBatchQueryId = qcBatch.getQCBatchQueryId();
            if (qcBatchQueryId == null || qcBatchQueryId.trim().length() == 0) {
                Logger.logDebug("[EvaluateQCBatch] Dataitems selection query not defined in QCBatch " + qcBatchId);
            }
            String keys = "";
            if (qcBatchQueryId != null && qcBatchQueryId.length() > 0) {
                keys = qp.getKeyid1List("QCBatch", qcBatchQueryId, qcBatchId, null, null, null, null);
                Logger.logDebug("[Chart Bean] QCBatches returned by Query '" + qcBatchQueryId + "': " + keys);
            } else {
                keys = qcBatchId;
            }
            if (keys != null && keys.length() > 0) {
                keys = keys.replaceAll(";", "','");
                QCBatchSampleType qcBatchSampleType = new QCBatchSampleType(qp, qcBatchSampleTypeId);
                String parameterType = qcBatchSampleType.getColumnValue("evaluateparamtype");
                String currentBatchId = qcBatch.getQCBatchID();
                String qcbatchSDCId = qcBatch.getQCBatchSDC();
                if (qcbatchSDCId == null || qcbatchSDCId.equals("")) {
                    qcbatchSDCId = "Sample";
                }
                SDCProcessor sdcProcessor = new SDCProcessor(this.pagecontext);
                String tableid = sdcProcessor.getProperty(qcbatchSDCId, "tableid");
                String keycolid1 = sdcProcessor.getProperty(qcbatchSDCId, "keycolid1");
                SafeSQL safeSQL = this.__SqlGenerator.getQCDataPointsForEvaluation(qcBatchSampleTypeId, String.valueOf(queryrows), parameterType, keys, currentBatchId, true, qcbatchSDCId, tableid, keycolid1);
                ds = qp.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
                if (ds == null || ds.size() == 0) {
                    Logger.logDebug("[Chart Bean] No  Data Points found for evaluation of the QCBatchSampleType " + qcBatchSampleType);
                    return false;
                }
            }
        } else {
            ds = qp.getSqlDataSet(this.__SqlQuery);
        }
        this.__ParameterTypes = ds.getColumnValues("PARAMTYPE", ";");
        if (this.__ParameterTypes == null) {
            this.__ParameterTypes = "No Paramaters";
        }
        SDI sdi = new SDI("QCBatchSampleType", qcBatchSampleTypeId, null, null);
        SafeSQL safeSQL = this.__SqlGenerator.getQCBatchParamSets(sdi);
        this.__ParamsetDataSet = qp.getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        if (ds.size() <= 0) {
            this.__ErrorMsg = " Query returned no data.";
            Logger.logDebug("[Chart Bean] Stats Creation failed. No data found.");
            return false;
        }
        ArrayList<String> paramidArray = new ArrayList<String>();
        this.__ParamsetDataSet.sort("PARAMID");
        String paramIdsTemp = this.__ParamsetDataSet.getColumnValues("PARAMID", ";");
        String[] paramIds = StringUtil.split(paramIdsTemp, ";");
        for (int i = 0; i < paramIds.length; ++i) {
            paramidArray.add(paramIds[i]);
            this.__ParameterIDS.add(paramIds[i]);
        }
        String qcbatchid = "";
        String paramlistid = "";
        String positionValue = "";
        String paramlistversionid = "";
        String createdate = "";
        String evalStatus = "";
        if (ds.size() > 0) {
            for (int x = 0; x < paramidArray.size(); ++x) {
                String paramid = (String)paramidArray.get(x);
                String keyid1 = "";
                int maxNumberOfReplicates = this.getMaxReplicates(paramid, ds);
                StatsList paramStatsList = new StatsList();
                paramStatsList.setStatsListParamid(paramid);
                Stats stats_0 = new Stats();
                String yparams = ds.getColumnValues("PARAMID", ";");
                String[] yparamsarray = StringUtil.split(yparams, ";");
                ArrayList<String> yp = new ArrayList<String>();
                for (int dd = 0; dd < yparamsarray.length; ++dd) {
                    yp.add(yparamsarray[dd]);
                }
                if (yp.contains(paramid)) {
                    stats_0.setYparam(paramid);
                }
                TreeSet<String> set = new TreeSet<String>();
                for (int i = 0; i < ds.size(); ++i) {
                    if (!ds.getValue(i, "PARAMID").equals(paramid)) continue;
                    keyid1 = ds.getValue(i, "KEYID1");
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put("KEYID1", keyid1);
                    map.put("PARAMID", paramid);
                    if (set.contains(map.toString())) continue;
                    set.add(map.toString());
                    double transformValue = 9.0E99;
                    String displayValue = "";
                    qcbatchid = ds.getValue(i, "s_qcbatchid") != null ? ds.getValue(i, "s_qcbatchid") : "";
                    paramlistid = ds.getValue(i, "paramlistid") != null ? ds.getValue(i, "paramlistid") : "";
                    positionValue = ds.getValue(i, "usersequence") != null ? ds.getValue(i, "usersequence") : "";
                    paramlistversionid = ds.getValue(i, "paramlistversionid") != null ? ds.getValue(i, "paramlistversionid") : "";
                    createdate = ds.getValue(i, "createdt") != null ? ds.getValue(i, "createdt") : "";
                    evalStatus = ds.getValue(i, "s_qcevalstatus") != null ? ds.getValue(i, "s_qcevalstatus") : "";
                    try {
                        transformValue = this.getAverageTransformValue(i, keyid1, paramid, ds).doubleValue();
                        displayValue = String.valueOf(this.getAverageDisplayValue(i, keyid1, paramid, ds));
                    }
                    catch (Exception ex) {
                        transformValue = 9.0E99;
                        displayValue = "    ";
                    }
                    stats_0.addX(keyid1 != null ? keyid1 + "$" + displayValue : "");
                    stats_0.addValue(transformValue != 9.0E99 ? transformValue : 9.0E99);
                    stats_0.addTransformValue(transformValue != 9.0E99 ? transformValue : 9.0E99);
                    stats_0.setEvalStatus(evalStatus != null ? evalStatus : "");
                    stats_0.setQCBatchId(qcbatchid != null ? qcbatchid : "");
                    stats_0.setParamListId(paramlistid != null ? paramlistid : "");
                    stats_0.setParamListVersionId(paramlistversionid != null ? paramlistversionid : "");
                    stats_0.setPositionValue(positionValue != null ? positionValue : "");
                    stats_0.setCreateDateValue(createdate != null ? createdate : "");
                }
                paramStatsList.add(0, stats_0);
                for (int replicateIndex = 1; replicateIndex <= maxNumberOfReplicates; ++replicateIndex) {
                    double repTransformValue = 9.0E99;
                    String repDisplayValue = "";
                    Stats statsRep = new Stats();
                    if (yp.contains(paramid)) {
                        statsRep.setYparam(paramid);
                    }
                    TreeSet<String> set1 = new TreeSet<String>();
                    for (int l = 0; l < ds.size(); ++l) {
                        if (!ds.getValue(l, "PARAMID").equals(paramid)) continue;
                        String keyid11 = ds.getValue(l, "KEYID1");
                        String replicateid1 = ds.getValue(l, "REPLICATEID");
                        HashMap<String, String> map2 = new HashMap<String, String>();
                        map2.put("KEYID1", keyid11);
                        map2.put("PARAMID", paramid);
                        if (replicateIndex != Integer.valueOf(replicateid1) || set1.contains(map2.toString())) continue;
                        set1.add(map2.toString());
                        qcbatchid = ds.getValue(l, "s_qcbatchid") != null ? ds.getValue(l, "s_qcbatchid") : "";
                        paramlistid = ds.getValue(l, "paramlistid") != null ? ds.getValue(l, "paramlistid") : "";
                        positionValue = ds.getValue(l, "usersequence") != null ? ds.getValue(l, "usersequence") : "";
                        paramlistversionid = ds.getValue(l, "paramlistversionid") != null ? ds.getValue(l, "paramlistversionid") : "";
                        createdate = ds.getValue(l, "createdt") != null ? ds.getValue(l, "createdt") : "";
                        evalStatus = ds.getValue(l, "s_qcevalstatus") != null ? ds.getValue(l, "s_qcevalstatus") : "";
                        try {
                            double d = repTransformValue = ds.getBigDecimal(l, "TRANSFORMVALUE") != null ? ds.getBigDecimal(l, "TRANSFORMVALUE").doubleValue() : 9.0E99;
                            if (ds.getValue(l, "DISPLAYVALUE") != null) {
                                try {
                                    repDisplayValue = ds.getValue(l, "DISPLAYVALUE");
                                }
                                catch (Exception ex) {
                                    repDisplayValue = "    ";
                                }
                            } else {
                                repDisplayValue = "    ";
                            }
                        }
                        catch (Exception ex) {
                            Trace.logError("Error in ChartBean.java", ex);
                        }
                        statsRep.addX(keyid11 != null ? keyid11 + "$" + repDisplayValue : "");
                        statsRep.addValue(repTransformValue != 9.0E99 ? repTransformValue : 9.0E99);
                        statsRep.addTransformValue(repTransformValue != 9.0E99 ? repTransformValue : 9.0E99);
                        statsRep.setEvalStatus(evalStatus != null ? evalStatus : "");
                        statsRep.setQCBatchId(qcbatchid != null ? qcbatchid : "");
                        statsRep.setParamListId(paramlistid != null ? paramlistid : "");
                        statsRep.setParamListVersionId(paramlistversionid != null ? paramlistversionid : "");
                        statsRep.setPositionValue(positionValue != null ? positionValue : "");
                        statsRep.setCreateDateValue(createdate != null ? createdate : "");
                    }
                    paramStatsList.add(replicateIndex, statsRep);
                }
                paramStatsList.getOverallMax();
                paramStatsList.getOverallMin();
                paramStatsList.getOverallCL();
                paramStatsList.getOverallLCL();
                paramStatsList.getOverallSD();
                paramStatsList.getOverallUCL();
                this.__ChartingHashmap.put(paramid, paramStatsList);
            }
            Logger.logDebug("[Chart Bean] StatsList creation complete. Took " + (System.currentTimeMillis() - current) + " ms.");
            long current1 = System.currentTimeMillis();
            this.createClientScript();
            Logger.logDebug("[Chart Bean] JavaScript code complete. Took " + (System.currentTimeMillis() - current1) + " ms.");
        }
        return true;
    }

    private StringBuffer createSampleArray() {
        StringBuffer guiSampleArrayBuffer = new StringBuffer();
        guiSampleArrayBuffer.append("samplearray[").append(0).append("] = new Array (");
        guiSampleArrayBuffer.append("'qcbatchid','sampleid','position','create date'");
        for (int k = 0; k < this.__ParameterIDS.size(); ++k) {
            if (this.__ChartingHashmap.get(this.__ParameterIDS.get(k)) == null) continue;
            guiSampleArrayBuffer.append(",'").append((String)this.__ParameterIDS.get(k)).append("'");
        }
        guiSampleArrayBuffer.append(");\n");
        return guiSampleArrayBuffer;
    }

    private StringBuffer createDataSetArray() {
        int datasetcount = 0;
        StringBuffer dataSetArray = new StringBuffer();
        HashMap<String, String> doneList = new HashMap<String, String>();
        for (int k = 0; k < this.__ParameterIDS.size(); ++k) {
            if (this.__ChartingHashmap.get(this.__ParameterIDS.get(k)) == null) continue;
            StatsList list = (StatsList)this.__ChartingHashmap.get(this.__ParameterIDS.get(k));
            Stats stats = (Stats)((Object)list.get(0));
            ArrayList paramlistids = (ArrayList)stats.getAllParamListIds();
            ArrayList paramlistsversionids = (ArrayList)stats.getAllParamListVersionIds();
            for (int j = 0; j < paramlistids.size(); ++j) {
                String tempparamlistids = "";
                String tempparamlistsversionids = "";
                try {
                    tempparamlistids = (String)paramlistids.get(j);
                    tempparamlistsversionids = (String)paramlistsversionids.get(j);
                }
                catch (Exception ex) {
                    Trace.logError("Error in Chart Bean", ex);
                }
                if (doneList.get(tempparamlistids + "," + tempparamlistsversionids) != null) continue;
                doneList.put(tempparamlistids + "," + tempparamlistsversionids, "Y");
                dataSetArray.append("datasetarray[").append(datasetcount).append("] = new Array ('" + tempparamlistids + "','" + tempparamlistsversionids + "');\n");
                ++datasetcount;
            }
        }
        return dataSetArray;
    }

    private StringBuffer createParameterArray() {
        StringBuffer parameterArray = new StringBuffer();
        int parameterCount = 0;
        for (int k = 0; k < this.__ParameterIDS.size(); ++k) {
            HashMap<String, String> doneList = new HashMap<String, String>();
            if (this.__ChartingHashmap.get(this.__ParameterIDS.get(k)) == null) continue;
            StatsList list = (StatsList)this.__ChartingHashmap.get(this.__ParameterIDS.get(k));
            Stats stats = (Stats)((Object)list.get(0));
            ArrayList paramlistids = (ArrayList)stats.getAllParamListIds();
            ArrayList paramlistsversionids = (ArrayList)stats.getAllParamListVersionIds();
            String tempparamlistid = "";
            String tempparamlistsversionid = "";
            String paramstring = "";
            for (int i = 0; i < paramlistids.size(); ++i) {
                try {
                    tempparamlistid = (String)paramlistids.get(i);
                    tempparamlistsversionid = (String)paramlistsversionids.get(i);
                }
                catch (Exception e) {
                    Trace.logError("Error in Chart Bean", e);
                }
                if (doneList.get(tempparamlistid + "," + tempparamlistsversionid) != null) continue;
                doneList.put(tempparamlistid + "," + tempparamlistsversionid, "Y");
                paramstring = paramstring.length() == 0 ? tempparamlistid + "(" + tempparamlistsversionid + ")" : paramstring + "/" + tempparamlistid + "(" + tempparamlistsversionid + ")";
            }
            parameterArray.append("parammeterarray[").append(parameterCount).append("] = new Array ('" + paramstring + "','" + list.getStatsListParamid() + "');\n");
            ++parameterCount;
        }
        return parameterArray;
    }

    private StringBuffer createSampleDataArray() {
        StringBuffer sampleDataArray = new StringBuffer();
        DataSet ds = new DataSet();
        for (int currparam = 0; currparam < this.__ParameterIDS.size(); ++currparam) {
            StatsList list = (StatsList)this.__ChartingHashmap.get(this.__ParameterIDS.get(currparam));
            for (int currstatsitem = 0; currstatsitem < list.size(); ++currstatsitem) {
                Stats stats = (Stats)((Object)list.get(currstatsitem));
                ArrayList values = (ArrayList)stats.getValuesAsList();
                ArrayList qcbatchids = (ArrayList)stats.getAllQCBatchIds();
                ArrayList positions = (ArrayList)stats.getAllPositionValues();
                ArrayList createdates = (ArrayList)stats.getAllCreateDates();
                for (int j = 0; j < values.size(); ++j) {
                    String displayvalue = "";
                    displayvalue = stats.getX(j) != null ? stats.getX(j) : " $ ";
                    try {
                        displayvalue = displayvalue.substring(displayvalue.indexOf("$") + 1, displayvalue.length());
                    }
                    catch (Exception e) {
                        displayvalue = "";
                    }
                    String currqcbatchidvalue = qcbatchids.get(j) != null ? (String)qcbatchids.get(j) : "";
                    String currpositionvalue = positions.get(j) != null ? (String)positions.get(j) : "";
                    String currsampleidvalue = stats.getX(j) != null ? stats.getX(j) : "";
                    currsampleidvalue = currsampleidvalue.substring(0, currsampleidvalue.indexOf("$"));
                    String currcreatedatevalue = createdates.get(j) != null ? (String)createdates.get(j) : "";
                    String currdisplayvaluedisp = displayvalue;
                    int currrow = -1;
                    for (int x = 0; x < ds.size(); ++x) {
                        String tempbatch = ds.getString(x, "qcbatchid");
                        String tempsampleid = ds.getString(x, "sampleid");
                        if (!tempbatch.equals(currqcbatchidvalue) || !tempsampleid.equals(currsampleidvalue)) continue;
                        currrow = x;
                        break;
                    }
                    if (currrow == -1) {
                        currrow = ds.addRow();
                        ds.setString(currrow, "qcbatchid", currqcbatchidvalue);
                        ds.setString(currrow, "sampleid", currsampleidvalue);
                        ds.setString(currrow, "position", currpositionvalue);
                        ds.setString(currrow, "createdate", currcreatedatevalue);
                    }
                    if (ds.getString(currrow, "displayvalue", "").length() > 0) {
                        currdisplayvaluedisp = ds.getString(currrow, "displayvalue", "") + "," + currdisplayvaluedisp;
                    }
                    ds.setString(currrow, this.__ParameterIDS.get(currparam).toString(), currdisplayvaluedisp);
                }
            }
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            sampleDataArray.append("samplearray[").append(i + 1).append("] = new Array (");
            sampleDataArray.append("'").append(ds.getString(i, "qcbatchid")).append("'");
            sampleDataArray.append(",'").append(ds.getString(i, "sampleid")).append("'");
            sampleDataArray.append(",'").append(ds.getString(i, "position")).append("'");
            sampleDataArray.append(",'").append(ds.getString(i, "createdate")).append("'");
            String paramvallist = "";
            for (int j = 0; j < this.__ParameterIDS.size(); ++j) {
                paramvallist = paramvallist + "'" + ds.getString(i, this.__ParameterIDS.get(j).toString(), "") + "'";
                if (j == this.__ParameterIDS.size() - 1) continue;
                paramvallist = paramvallist + ",";
            }
            sampleDataArray.append(",").append(paramvallist).append(")\n");
        }
        return sampleDataArray;
    }

    private void createClientScript() {
        this.__GuiStringBuffer.append("\n").append(this.createDataSetArray().toString()).append("\n");
        this.__GuiStringBuffer.append("\n").append(this.createParameterArray().toString()).append("\n");
        this.__GuiStringBuffer.append("\n").append(this.createSampleArray().toString()).append("\n");
        this.__GuiStringBuffer.append("\n").append(this.createSampleDataArray().toString()).append("\n");
    }

    public String getParameterType() {
        return this.__ParameterTypes;
    }

    public void setRequestArguments(String sd, String cl, String rows) {
        this.__ClFromRequest = cl;
        this.__SdFromRequest = sd;
        this.__MaxRows = rows;
    }

    private int getMaxReplicatesTrend(String paramid, String paramType, DataSet ds) {
        int maxReplicateValue = 0;
        for (int i = 0; i < ds.size(); ++i) {
            int tempValue;
            if (!paramid.equals(ds.getValue(i, "PARAMID") != null ? ds.getValue(i, "PARAMID") : "") || !paramType.equals(ds.getValue(i, "PARAMTYPE") != null ? ds.getValue(i, "PARAMTYPE") : "")) continue;
            int n = tempValue = ds.getBigDecimal(i, "REPLICATEID") != null ? ds.getBigDecimal(i, "REPLICATEID").intValue() : 1;
            if (maxReplicateValue >= tempValue) continue;
            maxReplicateValue = tempValue;
        }
        return maxReplicateValue;
    }

    private int getMaxReplicates(String paramid, DataSet ds) {
        int maxReplicateValue = 0;
        for (int i = 0; i < ds.size(); ++i) {
            int tempValue;
            if (!paramid.equals(ds.getValue(i, "PARAMID") != null ? ds.getValue(i, "PARAMID") : "")) continue;
            int n = tempValue = ds.getBigDecimal(i, "REPLICATEID") != null ? ds.getBigDecimal(i, "REPLICATEID").intValue() : 1;
            if (maxReplicateValue >= tempValue) continue;
            maxReplicateValue = tempValue;
        }
        return maxReplicateValue;
    }

    public Object getAverageDisplayValue(int rowNum, String keyid1, String paramid, DataSet ds) {
        NumberFormat fmt = NumberFormat.getNumberInstance((Locale)this.pagecontext.getAttribute("locale"));
        double values = 0.0;
        double replicateCount = 0.0;
        String ifdisplayisstring = "";
        for (int j = rowNum; j < ds.size() && ds.getValue(j, "keyid1").equals(keyid1); ++j) {
            if (!paramid.equals(ds.getValue(j, "PARAMID"))) continue;
            try {
                values += ds.getBigDecimal(j, "transformvalue") != null ? ds.getBigDecimal(j, "transformvalue").doubleValue() : 0.0;
                replicateCount += 1.0;
                continue;
            }
            catch (Exception ex) {
                ifdisplayisstring = ds.getValue(j, "DISPLAYVALUE");
                Trace.logError("Error in Chart Bean", ex);
            }
        }
        double result = 0.0;
        try {
            result = values / replicateCount;
            return String.valueOf(result);
        }
        catch (Exception e) {
            return ifdisplayisstring;
        }
    }

    public BigDecimal getAverageTransformValue(int rowNum, String keyid1, String paramid, DataSet ds) {
        double values = 0.0;
        double replicateCount = 0.0;
        for (int j = rowNum; j < ds.size() && (ds.getValue(j, "keyid1") != null ? ds.getValue(j, "keyid1") : "").equals(keyid1); ++j) {
            if (!paramid.equals(ds.getValue(j, "PARAMID") != null ? ds.getValue(j, "PARAMID") : "")) continue;
            try {
                values += ds.getBigDecimal(j, "transformvalue") != null ? ds.getBigDecimal(j, "transformvalue").doubleValue() : 0.0;
                replicateCount += 1.0;
                continue;
            }
            catch (Exception ex) {
                Trace.logError("Error in Chart Bean", ex);
            }
        }
        return new BigDecimal(values / replicateCount);
    }

    public BigDecimal getAverageTransformValueTrend(int rowNum, String keyid1, String paramid, String paramType, DataSet ds) {
        double values = 0.0;
        double replicateCount = 0.0;
        for (int j = rowNum; j < ds.size() && (ds.getValue(j, "keyid1") != null ? ds.getValue(j, "keyid1") : "").equals(keyid1); ++j) {
            if (!paramid.equals(ds.getValue(j, "PARAMID") != null ? ds.getValue(j, "PARAMID") : "") || !paramType.equals(ds.getValue(j, "PARAMTYPE") != null ? ds.getValue(j, "PARAMTYPE") : "")) continue;
            try {
                values += ds.getBigDecimal(j, "transformvalue") != null ? ds.getBigDecimal(j, "transformvalue").doubleValue() : 0.0;
                replicateCount += 1.0;
                continue;
            }
            catch (Exception ex) {
                Trace.logError("Error in Chart Bean", ex);
            }
        }
        return new BigDecimal(values / replicateCount);
    }

    public Object getAverageDisplayValueTrend(int rowNum, String keyid1, String paramid, DataSet ds) {
        double values = 0.0;
        double replicateCount = 0.0;
        String ifdisplayisstring = "";
        for (int j = rowNum; j < ds.size() && ds.getValue(j, "keyid1").equals(keyid1); ++j) {
            if (!paramid.equals(ds.getValue(j, "PARAMID"))) continue;
            try {
                values += ds.getBigDecimal(j, "transformvalue") != null ? ds.getBigDecimal(j, "transformvalue").doubleValue() : 0.0;
                replicateCount += 1.0;
                continue;
            }
            catch (Exception ex) {
                ifdisplayisstring = ds.getValue(j, "DISPLAYVALUE");
                Trace.logError("Error in Chart Bean", ex);
            }
        }
        double result = 0.0;
        try {
            result = values / replicateCount;
            return String.valueOf(result);
        }
        catch (Exception e) {
            return ifdisplayisstring;
        }
    }

    public boolean parseQCStats() {
        this.pagecontext.setAttribute("ChartingHashmap", (Object)this.__ChartingHashmap);
        this.pagecontext.setAttribute("paramids", (Object)this.__ParameterIDS);
        this.pagecontext.setAttribute("sdFromRequest", (Object)this.__SdFromRequest);
        this.pagecontext.setAttribute("clFromRequest", (Object)this.__ClFromRequest);
        this.pagecontext.setAttribute("paramsetDataSet", (Object)this.__ParamsetDataSet);
        this.pagecontext.setAttribute("qcbatchsampletypeid", (Object)this.__qcBatchSampleTypeId);
        this.pagecontext.setAttribute("qcbatchid", (Object)this.__qcBatchId);
        this.pagecontext.setAttribute("qcsampletype", (Object)this.__qcSampleType);
        return true;
    }

    public boolean validateParameters() {
        boolean ret = true;
        TranslationProcessor tp = new TranslationProcessor(this.pagecontext);
        this.__ErrorMsg = tp.translate("Missing Request Parameter:") + "<br>";
        if (this.__QueryID == null || this.__QueryID.trim().length() == 0) {
            this.__ErrorMsg = this.__ErrorMsg + "queryid<br>";
            this.__MissingParam.add("queryid");
            ret = false;
        }
        if (this.__ParamListID == null || this.__ParamListID.trim().length() == 0) {
            this.__ErrorMsg = this.__ErrorMsg + "paramlistid<br>";
            this.__MissingParam.add("paramlistid");
            ret = false;
        }
        if (this.__ParamListVersionID == null || this.__ParamListVersionID.trim().length() == 0) {
            this.__ErrorMsg = this.__ErrorMsg + "paramlistversionid<br>";
            this.__MissingParam.add("paramlistversionid");
            ret = false;
        }
        if (this.__VariantID == null || this.__VariantID.trim().length() == 0) {
            this.__ErrorMsg = this.__ErrorMsg + "variantid<br>";
            this.__MissingParam.add("variantid");
            ret = false;
        }
        if (this.__DataSet == null || this.__DataSet.trim().length() == 0) {
            this.__ErrorMsg = this.__ErrorMsg + "dataset<br>";
            this.__MissingParam.add("dataset");
            ret = false;
        }
        if (this.__ParamID == null || this.__ParamID.trim().length() == 0) {
            this.__ErrorMsg = this.__ErrorMsg + "paramid<br>";
            this.__MissingParam.add("paramid");
            ret = false;
        }
        if (this.__ParamType == null || this.__ParamType.trim().length() == 0) {
            this.__ErrorMsg = this.__ErrorMsg + "paramtype";
            this.__MissingParam.add("paramtype");
            ret = false;
        }
        return ret;
    }

    public DataSet getDataItems() {
        SafeSQL safeSQL = this.__SqlGenerator.getDataItems(this.__KeyId1, this.__ParamListID, this.__ParamListVersionID, this.__VariantID, this.__DataSet);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        return ds;
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
        long current = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        TranslationProcessor tp = new TranslationProcessor(this.pagecontext);
        sb.append("<table cellpadding=2 cellspacing=0 border=0 width='200'>");
        sb.append("<tr><td class='gridmaint_fieldtitle'>").append(tp.translate("Query ID")).append("</td></tr>");
        sb.append("<tr><td class='gridmaint_field' align=right>");
        sb.append(this.__QueryID);
        sb.append("</td></tr>");
        sb.append("<tr><td height=5></td></tr>");
        sb.append("<tr><td class='gridmaint_fieldtitle'>").append(tp.translate("Query Arguments")).append("</td></tr>");
        SafeSQL safeSQL = this.__SqlGenerator.getQueryArgsForQuery(this.__QueryID);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(safeSQL.getPreparedSQL(), safeSQL.getValues());
        for (int i = 0; i < ds.size(); ++i) {
            sb.append("<tr><td class='gridmaint_field'><I>");
            sb.append(ds.getValue(i, "argid"));
            sb.append("</I></td></tr>");
            sb.append("<tr><td class='gridmaint_field' align='right'>");
            String a = ds.getValue(i, "arginto");
            if (a.indexOf("keyid1") == -1 && a.indexOf("paramlistid") == -1 && a.indexOf("paramlistversionid") == -1 && a.indexOf("variantid") == -1) {
                String queryparam = a.substring(1, a.length() - 1);
                String paramvalue = this.pagecontext.getRequest().getParameter(queryparam);
                sb.append("<input size=15 value='");
                if (paramvalue != null) {
                    sb.append(paramvalue.replace('^', '%'));
                } else {
                    sb.append(String.valueOf(this.__ParamMap.get(a)).replace('^', '%'));
                }
                sb.append("' id='" + queryparam + "' name='" + queryparam + "'>");
            } else {
                sb.append(OpalUtil.parseRequestString(this.pagecontext, a).replace('^', '%'));
            }
            sb.append("</td></tr>");
            sb.append("<tr><td height=5 class='gridmaint_field'></td></tr>");
        }
        String rows = this.pagecontext.getRequest().getParameter("maxrows");
        if (rows == null) {
            rows = this.__MaxRows;
        }
        sb.append("<tr><td class='gridmaint_field'><I>");
        sb.append(tp.translate("Rows"));
        sb.append("</I></td></tr>");
        sb.append("<tr><td class='gridmaint_field' align='right'>");
        sb.append("<input size=15 value='");
        sb.append(rows).append("' name='maxrows'>");
        sb.append("</td></tr>");
        sb.append("</table>");
        Logger.logDebug("[Chart Bean] Query Panel complete. Took " + (System.currentTimeMillis() - current) + " ms.");
        return sb.toString();
    }

    public String getDataPoints() {
        StringBuffer sb = new StringBuffer();
        NumberFormat fmt = NumberFormat.getNumberInstance((Locale)this.pagecontext.getAttribute("locale"));
        TranslationProcessor tp = new TranslationProcessor(this.pagecontext);
        sb.append("<div><table cellpadding=2 cellspacing=0 border=0 width='100%' class='gridmaint_table'>");
        int size1 = this.__ParameterIDS.size();
        sb.append("<tr><td class='gridmaint_field'>");
        sb.append(tp.translate("Item")).append(".");
        sb.append("</td><td class='gridmaint_field'>");
        sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        sb.append(tp.translate("SampleID"));
        sb.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        sb.append("</td>");
        for (int d = 0; d < size1; ++d) {
            sb.append("<td class='gridmaint_field'>").append(this.__ParameterIDS.get(d)).append("</td>");
        }
        sb.append("</tr>");
        StatsList statsListtemp = (StatsList)this.__ChartingHashmap.get(this.__ParameterIDS.get(0));
        Stats statstemp = (Stats)((Object)statsListtemp.get(0));
        List statsvaluelisttemp = statstemp.getValuesAsList();
        int numberOfKeyids = statsvaluelisttemp.size();
        for (int i = 0; i < numberOfKeyids; ++i) {
            sb.append("<tr><td class='gridmaint_field' align='center'>");
            sb.append(fmt.format(i + 1)).append(".");
            sb.append("</td>");
            sb.append("<td class='gridmaint_field' align='center'>");
            String keyid = statstemp.getX(i);
            String keyid1 = keyid.substring(0, keyid.indexOf("$"));
            sb.append(keyid1).append("</td>");
            for (int g = 0; g < this.__ParameterIDS.size(); ++g) {
                StatsList statsList = (StatsList)this.__ChartingHashmap.get(this.__ParameterIDS.get(g));
                Stats stats = (Stats)((Object)statsList.get(0));
                List statsvaluelist = stats.getValuesAsList();
                try {
                    String keyidTemp = stats.getX(i);
                    String keyidTemp1 = keyidTemp.substring(0, keyid.indexOf("$"));
                    if (keyidTemp1.equals(keyid1)) {
                        double value = (Double)statsvaluelist.get(i);
                        sb.append("<td class='gridmaint_field' align='center'>").append(fmt.format(value)).append("</td>");
                        continue;
                    }
                    sb.append("<td class='gridmaint_field' align='center'>").append("</td>");
                    continue;
                }
                catch (Exception e) {
                    sb.append("<td class='gridmaint_field' align='center'>").append("</td>");
                }
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    public String getQCDataPoints() {
        StringBuffer sb = new StringBuffer();
        if (this.__ParamsetDataSet != null && this.__ParamsetDataSet.size() > 0) {
            for (int i = 0; i < this.__ParamsetDataSet.size(); ++i) {
                String paramsetId = this.__ParamsetDataSet.getString(i, "S_QCBATCHPARAMSETID");
                String paramId = this.__ParamsetDataSet.getString(i, "PARAMID");
                String qvalStatus = this.__ParamsetDataSet.getString(i, "EVALSTATUS");
                String targetValue = this.__ParamsetDataSet.getString(i, "TARGETVALUE");
                String targetUnits = this.__ParamsetDataSet.getString(i, "TARGETUNITS");
                String sd = this.__ParamsetDataSet.getString(i, "SD");
                sb.append("paramsets[" + i + "] = new Array(\"");
                sb.append(paramsetId + "\",\"");
                sb.append(paramId + "\",\"");
                sb.append(qvalStatus + "\",\"");
                sb.append(targetValue + "\",\"");
                sb.append(targetUnits + "\",\"");
                sb.append(sd + "\");\n");
            }
        }
        return this.__GuiStringBuffer.toString() + "\n" + sb.toString() + "\n";
    }

    public String getQCDataPoints2() {
        StringBuffer guiParamsetArrayBuffer2 = new StringBuffer();
        HashMap<String, String> __Statistics = new HashMap<String, String>();
        for (int k = 0; k < this.__ParameterIDS.size(); ++k) {
            StatsList list = (StatsList)this.__ChartingHashmap.get(this.__ParameterIDS.get(k));
            double tempCL = list.getOverallCL();
            double tempSD = list.getOverallSD();
            String strCL = String.valueOf(tempCL);
            String strSD = String.valueOf(tempSD);
            String effectiveStats = strSD + ":" + strCL;
            __Statistics.put(list.getStatsListParamid(), effectiveStats);
        }
        if (__Statistics.size() > 0) {
            Iterator iter = __Statistics.keySet().iterator();
            int count = 0;
            while (iter.hasNext()) {
                Object key = iter.next();
                Object value = __Statistics.get(key);
                guiParamsetArrayBuffer2.append("paramsets2[" + count + "] = new Array(\"");
                guiParamsetArrayBuffer2.append(String.valueOf(key) + "\",\"");
                guiParamsetArrayBuffer2.append(String.valueOf(value) + "\",\"");
                guiParamsetArrayBuffer2.append("\");\n");
                ++count;
            }
        }
        return guiParamsetArrayBuffer2.toString();
    }

    public PropertyList getPagedata() {
        return this.__PageData;
    }

    public void setPagedata(PropertyList parPagedata) {
        this.__PageData = parPagedata;
    }

    public PropertyList getChartElementProperties() {
        return this.__ChartElementProperties;
    }

    public void setChartElementProperties(PropertyList elementProperties) {
        this.__ChartElementProperties = elementProperties;
    }

    public List getMissingparam() {
        return this.__MissingParam;
    }

    public String getErrormsg() {
        return this.__ErrorMsg;
    }

    public String getSysuserid() {
        return this.__SysUserID;
    }

    public void setSysuserid(String sysuserid) {
        this.__SysUserID = sysuserid;
    }
}

