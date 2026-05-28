/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.actions;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import com.labvantage.opal.util.DataItem;
import com.labvantage.opal.util.SDIDataSet;
import com.labvantage.opal.util.SDIWorkItem;
import com.labvantage.sapphire.SDI;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DSActions
extends BaseAction {
    static final String LABVANTAGE_CVS_ID = "$Revision: 84916 $";
    public static final String DSSTATUSCOLUMN = "s_datasetstatus";
    public static final String DSSTATUS_INITIAL = "Initial";
    public static final String DSSTATUS_ASSIGNED = "Assigned";
    public static final String DSSTATUS_INPROGRESS = "InProgress";
    public static final String DSSTATUS_REMEASURED = "Remeasured";
    public static final String DSSTATUS_RETESTED = "Retested";
    public static final String DSSTATUS_RELEASED = "Released";
    public static final String DSSTATUS_COMPLETED = "Completed";
    public static final String DSSTATUS_ALLENTERED = "DataEntered";
    public static final String DSSTATUS_CANCELLED = "Cancelled";
    public static final int DATAITEM_ALL = 0;
    public static final int DATAITEM_MANDATORY = 1;
    public static final int DATAITEM_OPTIONAL = 2;
    public static final String SDISTATUSCOLUMN = "samplestatus";
    public static final String SDISTATUS_INITIAL = "Initial";
    public static final String SDISTATUS_RECEIVED = "Received";
    public static final String SDISTATUS_INPROGRESS = "InProgress";
    private SQLGenerator __SqlGenerator;

    @Override
    public int processAction(String actionid, String actionversionid, HashMap props) {
        int rc = 1;
        this.__SqlGenerator = SQLFactory.getSqlGenerator(this.getConnectionProcessor().isOra());
        rc = actionid.equals("RemeasureDataSet") ? this.doRemeasureDataSet(props) : (actionid.equals("CancelDataSet") ? this.doCancelDataSet(props) : (actionid.equals("UncancelDataSet") ? this.doUncancelDataSet(props) : (actionid.equals("RetestDataSet") ? this.doRetestDataSet(props) : (actionid.equals("IsAnyDataReleased") ? this.doIsAnyDataReleased(props) : (actionid.equals("ChangeSampleStatus") ? this.doChangeSampleStatus(props) : (actionid.equals("GetSDIDataSet") ? this.doGetSDIDataSet(props) : (actionid.equals("GetWorkitemItem") ? this.doGetWorkitemItem(props) : (actionid.equals("CopySDIDataSet") ? this.doCopySDIDataSet(props) : this.setError("Action " + actionid + " not found.")))))))));
        return rc;
    }

    private int doRemeasureDataSet(HashMap props) {
        int rc = 1;
        String sdcid = (String)props.get("sdcid");
        String keyid1 = (String)props.get("keyid1");
        String keyid2 = (String)props.get("keyid2");
        String keyid3 = (String)props.get("keyid3");
        String paramlistid = (String)props.get("paramlistid");
        String paramlistversionid = (String)props.get("paramlistversionid");
        String variantid = (String)props.get("variantid");
        String dataset = (String)props.get("dataset");
        String auditreason = (String)props.get("auditreason");
        String auditactivity = (String)props.get("auditactivity");
        String auditsignedflag = (String)props.get("auditsignedflag");
        String newdsstatus = (String)props.get("newdsstatus");
        boolean createMultipleInstances = "Y".equalsIgnoreCase((String)props.get("createmultipleinstances"));
        SDIDataSet sdidataset = null;
        ArrayList<SDIDataSet> dslist = new ArrayList<SDIDataSet>();
        SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
        if (!sdi.isValid()) {
            return this.setError("Invalid input: " + sdi.getKeyText());
        }
        if (paramlistid.length() == 0 || paramlistversionid.length() == 0 || variantid.length() == 0 || dataset.length() == 0) {
            return this.setError("Missing mandatory input");
        }
        newdsstatus = newdsstatus == null ? "" : newdsstatus;
        props.put("addnewonly", "n");
        props.remove("newdsstatus");
        try {
            String[] var1 = StringUtil.split(paramlistid, ";");
            String[] var2 = StringUtil.split(paramlistversionid, ";");
            String[] var3 = StringUtil.split(variantid, ";");
            String[] var4 = StringUtil.split(dataset, ";");
            String[] varKeyid1 = StringUtil.split(keyid1, ";");
            String[] varKeyid2 = null;
            String[] varKeyid3 = null;
            if (keyid2 != null && keyid2.length() > 0 && varKeyid1.length != (varKeyid2 = StringUtil.split(keyid2, ";")).length) {
                return this.setError("Number of KeyId2 passed in action input is not matching with KeyId1.");
            }
            if (keyid3 != null && keyid3.length() > 0 && varKeyid1.length != (varKeyid3 = StringUtil.split(keyid3, ";")).length) {
                return this.setError("Number of KeyId3 passed in action input is not matching with KeyId1.");
            }
            ActionProcessor ap = this.getActionProcessor();
            QueryProcessor qp = this.getQueryProcessor();
            DataSet newDataSetInstances = new DataSet(this.connectionInfo);
            DataSet editOldDataset = new DataSet();
            DataSet editNewDataset = new DataSet();
            for (int i = 0; i < var1.length; ++i) {
                if (varKeyid1.length > i) {
                    sdi = new SDI(sdcid, varKeyid1[i], varKeyid2 != null ? varKeyid2[i] : "", varKeyid3 != null ? varKeyid3[i] : "");
                }
                sdidataset = new SDIDataSet(sdi, var1[i], var2[i], var3[i], var4[i], qp);
                if (!createMultipleInstances && dslist.contains(sdidataset)) continue;
                DataSet dsInfo = qp.getPreparedSqlDataSet("SELECT s_qcbatchid, s_qcbatchitemid, scheduleplanid, scheduleplanitemid, trackitemid, arraymethodid, arraymethodversionid, arraymethodinstance FROM sdidata  WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = ? AND paramlistid = ? AND paramlistversionid = ?  AND variantid = ? AND dataset = ?", (Object[])new String[]{sdi.getSdcid(), sdi.getKeyid1(), sdi.getKeyid2(), sdi.getKeyid3(), sdidataset.getParamListID(), sdidataset.getParamListVersionID(), sdidataset.getVariantID(), sdidataset.getDataSet()});
                String arrayMethodId = "";
                String arrayMethodVersionId = "";
                String qcBatchId = "";
                String scheduleplanId = "";
                if (dsInfo.getRowCount() > 0) {
                    arrayMethodId = dsInfo.getValue(0, "arraymethodid");
                    qcBatchId = dsInfo.getValue(0, "s_qcbatchid");
                    scheduleplanId = dsInfo.getValue(0, "scheduleplanid");
                }
                int r = editOldDataset.addRow();
                editOldDataset.setString(r, "keyid1", sdi.getKeyid1());
                editOldDataset.setString(r, "keyid2", sdi.getKeyid2());
                editOldDataset.setString(r, "keyid3", sdi.getKeyid3());
                editOldDataset.setString(r, "paramlistid", sdidataset.getParamListID());
                editOldDataset.setString(r, "paramlistversionid", sdidataset.getParamListVersionID());
                editOldDataset.setString(r, "variantid", sdidataset.getVariantID());
                editOldDataset.setString(r, "dataset", sdidataset.getDataSet());
                editOldDataset.setString(r, "s_remeasuredflag", "Y");
                r = editNewDataset.addRow();
                editNewDataset.setString(r, "keyid1", sdi.getKeyid1());
                editNewDataset.setString(r, "keyid2", sdi.getKeyid2());
                editNewDataset.setString(r, "keyid3", sdi.getKeyid3());
                editNewDataset.setString(r, "paramlistid", sdidataset.getParamListID());
                editNewDataset.setString(r, "paramlistversionid", sdidataset.getParamListVersionID());
                editNewDataset.setString(r, "variantid", sdidataset.getVariantID());
                editNewDataset.setString(r, "dataset", sdidataset.getDataSet());
                editNewDataset.setString(r, "s_remeasureinstance", sdidataset.getDataSet());
                editNewDataset.setString(r, DSSTATUSCOLUMN, newdsstatus);
                editNewDataset.setString(r, "wapstatus", "Never");
                if (arrayMethodId.length() > 0) {
                    arrayMethodVersionId = dsInfo.getValue(0, "arraymethodversionid");
                    int arrayMethodInstance = dsInfo.getInt(0, "arraymethodinstance");
                    editNewDataset.setString(r, "arraymethodid", arrayMethodId);
                    editNewDataset.setString(r, "arraymethodversionid", arrayMethodVersionId);
                    editNewDataset.setString(r, "arraymethodinstance", Integer.toString(arrayMethodInstance + 1));
                }
                if (qcBatchId.length() > 0) {
                    String qcBatchItemId = dsInfo.getValue(0, "s_qcbatchitemid");
                    editNewDataset.setString(r, "s_qcbatchid", qcBatchId);
                    editNewDataset.setString(r, "s_qcbatchitemid", qcBatchItemId);
                }
                if (scheduleplanId.length() > 0) {
                    String scheduleplanItemId = dsInfo.getValue(0, "scheduleplanitemid");
                    String trackItemId = dsInfo.getValue(0, "trackitemid");
                    editNewDataset.setString(r, "scheduleplanid", scheduleplanId);
                    editNewDataset.setString(r, "scheduleplanitemid", scheduleplanItemId);
                    editNewDataset.setString(r, "trackitemid", trackItemId);
                }
                dslist.add(sdidataset);
            }
            if (editOldDataset.getRowCount() > 0) {
                PropertyList editDatasetProps = new PropertyList();
                editDatasetProps.setProperty("sdcid", sdcid);
                editDatasetProps.setProperty("keyid1", editOldDataset.getColumnValues("keyid1", ";"));
                editDatasetProps.setProperty("keyid2", editOldDataset.getColumnValues("keyid2", ";"));
                editDatasetProps.setProperty("keyid3", editOldDataset.getColumnValues("keyid3", ";"));
                editDatasetProps.setProperty("paramlistid", editOldDataset.getColumnValues("paramlistid", ";"));
                editDatasetProps.setProperty("paramlistversionid", editOldDataset.getColumnValues("paramlistversionid", ";"));
                editDatasetProps.setProperty("variantid", editOldDataset.getColumnValues("variantid", ";"));
                editDatasetProps.setProperty("dataset", editOldDataset.getColumnValues("dataset", ";"));
                editDatasetProps.setProperty("s_remeasuredflag", editOldDataset.getColumnValues("s_remeasuredflag", ";"));
                editDatasetProps.setProperty("propsmatch", "Y");
                editDatasetProps.setProperty("auditreason", auditreason != null && auditreason.length() > 0 ? auditreason : "");
                editDatasetProps.setProperty("auditactivity", auditactivity != null && auditactivity.length() > 0 ? auditactivity : "");
                editDatasetProps.setProperty("auditsignedflag", auditsignedflag != null && auditsignedflag.length() > 0 ? auditsignedflag : "N");
                ap.processAction("EditDataSet", "1", editDatasetProps);
                for (int d = 0; d < editOldDataset.getRowCount(); ++d) {
                    PropertyList copyDatasetProps = new PropertyList();
                    copyDatasetProps.setProperty("sdcid", sdcid);
                    copyDatasetProps.setProperty("keyid1", editOldDataset.getValue(d, "keyid1"));
                    copyDatasetProps.setProperty("keyid2", editOldDataset.getValue(d, "keyid2"));
                    copyDatasetProps.setProperty("keyid3", editOldDataset.getValue(d, "keyid3"));
                    copyDatasetProps.setProperty("paramlistid", editOldDataset.getValue(d, "paramlistid"));
                    copyDatasetProps.setProperty("paramlistversionid", editOldDataset.getValue(d, "paramlistversionid"));
                    copyDatasetProps.setProperty("variantid", editOldDataset.getValue(d, "variantid"));
                    copyDatasetProps.setProperty("dataset", editOldDataset.getValue(d, "dataset"));
                    copyDatasetProps.setProperty("auditreason", auditreason != null && auditreason.length() > 0 ? auditreason : "");
                    copyDatasetProps.setProperty("auditactivity", auditactivity != null && auditactivity.length() > 0 ? auditactivity : "");
                    copyDatasetProps.setProperty("auditsignedflag", auditsignedflag != null && auditsignedflag.length() > 0 ? auditsignedflag : "N");
                    ap.processAction("CopyDataSet", "1", copyDatasetProps);
                    sdi = new SDI(sdcid, editNewDataset.getValue(d, "keyid1"), editNewDataset.getValue(d, "keyid2"), editNewDataset.getValue(d, "keyid3"));
                    sdidataset = new SDIDataSet(sdi, editNewDataset.getValue(d, "paramlistid"), editNewDataset.getValue(d, "paramlistversionid"), editNewDataset.getValue(d, "variantid"), editNewDataset.getValue(d, "dataset"), qp);
                    int maxInstance = sdidataset.getMaxInstance();
                    editNewDataset.setValue(d, "dataset", String.valueOf(maxInstance));
                    int newDSRow = newDataSetInstances.addRow();
                    sdidataset.setDataSet(String.valueOf(maxInstance));
                    newDataSetInstances.setString(newDSRow, "sdcid", sdcid);
                    newDataSetInstances.setString(newDSRow, "paramlistid", sdidataset.getParamListID());
                    newDataSetInstances.setString(newDSRow, "paramlistversionid", sdidataset.getParamListVersionID());
                    newDataSetInstances.setString(newDSRow, "variantid", sdidataset.getVariantID());
                    newDataSetInstances.setString(newDSRow, "keyid1", sdidataset.getSDI().keyid1);
                    newDataSetInstances.setString(newDSRow, "keyid2", sdidataset.getSDI().keyid3);
                    newDataSetInstances.setString(newDSRow, "keyid3", sdidataset.getSDI().keyid3);
                    newDataSetInstances.setString(newDSRow, "dataset", String.valueOf(maxInstance));
                    newDataSetInstances.setString(newDSRow, "sdidataid", sdidataset.getSDIDataId());
                }
                if (editNewDataset.getRowCount() > 0) {
                    editDatasetProps = new PropertyList();
                    editDatasetProps.setProperty("sdcid", sdcid);
                    editDatasetProps.setProperty("keyid1", editNewDataset.getColumnValues("keyid1", ";"));
                    editDatasetProps.setProperty("keyid2", editNewDataset.getColumnValues("keyid2", ";"));
                    editDatasetProps.setProperty("keyid3", editNewDataset.getColumnValues("keyid3", ";"));
                    editDatasetProps.setProperty("paramlistid", editNewDataset.getColumnValues("paramlistid", ";"));
                    editDatasetProps.setProperty("paramlistversionid", editOldDataset.getColumnValues("paramlistversionid", ";"));
                    editDatasetProps.setProperty("variantid", editNewDataset.getColumnValues("variantid", ";"));
                    editDatasetProps.setProperty("dataset", editNewDataset.getColumnValues("dataset", ";"));
                    editDatasetProps.setProperty(DSSTATUSCOLUMN, editNewDataset.getColumnValues(DSSTATUSCOLUMN, ";"));
                    editDatasetProps.setProperty("s_remeasureinstance", editNewDataset.getColumnValues("s_remeasureinstance", ";"));
                    editDatasetProps.setProperty("wapstatus", editNewDataset.getColumnValues("wapstatus", ";"));
                    if (editNewDataset.isValidColumn("arraymethodid")) {
                        editDatasetProps.setProperty("arraymethodid", editNewDataset.getColumnValues("arraymethodid", ";"));
                        editDatasetProps.setProperty("arraymethodversionid", editNewDataset.getColumnValues("arraymethodversionid", ";"));
                        editDatasetProps.setProperty("arraymethodinstance", editNewDataset.getColumnValues("arraymethodinstance", ";"));
                    }
                    if (editNewDataset.isValidColumn("s_qcbatchid")) {
                        editDatasetProps.setProperty("s_qcbatchid", editNewDataset.getColumnValues("s_qcbatchid", ";"));
                        editDatasetProps.setProperty("s_qcbatchitemid", editNewDataset.getColumnValues("s_qcbatchitemid", ";"));
                    }
                    if (editNewDataset.isValidColumn("scheduleplanid")) {
                        editDatasetProps.setProperty("scheduleplanid", editNewDataset.getColumnValues("scheduleplanid", ";"));
                        editDatasetProps.setProperty("scheduleplanitemid", editNewDataset.getColumnValues("scheduleplanitemid", ";"));
                        editDatasetProps.setProperty("trackitemid", editNewDataset.getColumnValues("trackitemid", ";"));
                    }
                    editDatasetProps.setProperty("propsmatch", "Y");
                    ap.processAction("EditDataSet", "1", editDatasetProps);
                }
            }
            props.put("newdatasetinstancexml", newDataSetInstances.toXML());
        }
        catch (ActionException e) {
            return this.setError("Exception caught: " + e.getMessage());
        }
        return rc;
    }

    private int doCancelDataSet(HashMap props) {
        String keyid3;
        String keyid2;
        String keyid1;
        int rc = 1;
        PropertyList plProps = new PropertyList(props);
        String sdcid = plProps.getProperty("sdcid", "");
        SDI sdi = new SDI(sdcid, keyid1 = plProps.getProperty("keyid1", ""), keyid2 = plProps.getProperty("keyid2", ""), keyid3 = plProps.getProperty("keyid3", ""));
        if (!sdi.isValid()) {
            return this.setError("Invalid input: " + sdi.getKeyText());
        }
        try {
            ActionProcessor ap = this.getActionProcessor();
            props.put(DSSTATUSCOLUMN, DSSTATUS_CANCELLED);
            props.put("cancelleddt", "n");
            props.put("cancelledby", this.connectionInfo.getSysuserId());
            ap.processAction("EditDataSet", "1", props);
        }
        catch (ActionException e) {
            return this.setError("Exception caught: " + e.getMessage());
        }
        return rc;
    }

    private int doUncancelDataSet(HashMap props) {
        int rc = 1;
        PropertyList plProps = new PropertyList(props);
        String sdcid = plProps.getProperty("sdcid", "");
        String keyid1 = plProps.getProperty("keyid1", "");
        String keyid2 = plProps.getProperty("keyid2", "");
        String keyid3 = plProps.getProperty("keyid3", "");
        String paramlistid = plProps.getProperty("paramlistid", "");
        String paramlistversionid = plProps.getProperty("paramlistversionid", "");
        String variantid = plProps.getProperty("variantid", "");
        String dataset = plProps.getProperty("dataset", "");
        SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
        if (!sdi.isValid()) {
            return this.setError("Invalid input: " + sdi.getKeyText());
        }
        if (paramlistid.length() == 0 || paramlistversionid.length() == 0 || variantid.length() == 0 || dataset.length() == 0) {
            return this.setError("Missing mandatory input");
        }
        try {
            ActionProcessor ap = this.getActionProcessor();
            StringBuffer dsStatus = this.getDatasetStatusBeforeCancellation(plProps);
            if (dsStatus.length() > 0) {
                props.put(DSSTATUSCOLUMN, dsStatus.substring(1));
            } else {
                props.put(DSSTATUSCOLUMN, "Initial");
            }
            ap.processAction("EditDataSet", "1", props);
            ap.processAction("UpdateDatasetStatus", "1", props);
        }
        catch (ActionException e) {
            return this.setError("Exception caught: " + e.getMessage());
        }
        return rc;
    }

    private StringBuffer getDatasetStatusBeforeCancellation(PropertyList plProps) {
        String sdcid = plProps.getProperty("sdcid", "");
        String keyid1 = plProps.getProperty("keyid1", "");
        String keyid2 = plProps.getProperty("keyid2", "");
        String keyid3 = plProps.getProperty("keyid3", "");
        String paramlistid = plProps.getProperty("paramlistid", "");
        String paramlistversionid = plProps.getProperty("paramlistversionid", "");
        String variantid = plProps.getProperty("variantid", "");
        String dataset = plProps.getProperty("dataset", "");
        String[] paramlistidArr = StringUtil.split(paramlistid, ";");
        String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
        String[] variantidArr = StringUtil.split(variantid, ";");
        String[] datasetArr = StringUtil.split(dataset, ";");
        StringBuffer dsStatus = new StringBuffer();
        if (this.isAuditTableExist("Dataset")) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("select * FROM a_sdidata ");
            sql.append("WHERE sdcid=" + safeSQL.addVar(sdcid) + " ");
            sql.append("AND keyid1=" + safeSQL.addVar(keyid1) + " ");
            if (!keyid2.equals("")) {
                sql.append("AND keyid2=" + safeSQL.addVar(keyid2) + " ");
            }
            if (!keyid3.equals("")) {
                sql.append("AND keyid3=" + safeSQL.addVar(keyid3) + " ");
            }
            sql.append("AND s_datasetstatus!='Cancelled' ");
            sql.append("AND paramlistid IN ( ").append(safeSQL.addIn(paramlistid, ";")).append(" ) ");
            sql.append("AND paramlistversionid IN ( ").append(safeSQL.addIn(paramlistversionid, ";")).append(" ) ");
            sql.append("AND variantid IN ( ").append(safeSQL.addIn(variantid, ";")).append(" ) ");
            sql.append("AND dataset IN ( ").append(safeSQL.addIn(dataset, ";")).append(" ) ");
            sql.append("ORDER BY auditsequence desc ");
            DataSet ds = null;
            try {
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
            catch (Exception e) {
                return dsStatus;
            }
            if (ds != null && ds.size() > 0) {
                HashMap<String, Object> findMap = new HashMap<String, Object>();
                BigDecimal datasetNumber = null;
                for (int i = 0; i < paramlistidArr.length; ++i) {
                    findMap.clear();
                    datasetNumber = new BigDecimal(datasetArr[i]);
                    findMap.put("paramlistid", paramlistidArr[i]);
                    findMap.put("paramlistversionid", paramlistversionidArr[i]);
                    findMap.put("variantid", variantidArr[i]);
                    findMap.put("dataset", datasetNumber);
                    DataSet filteredDS = ds.getFilteredDataSet(findMap);
                    if (filteredDS != null && filteredDS.size() > 0) {
                        dsStatus.append(";" + filteredDS.getValue(0, DSSTATUSCOLUMN));
                        continue;
                    }
                    dsStatus.append(";Initial");
                }
            }
        }
        return dsStatus;
    }

    private boolean isAuditTableExist(String sdcid) {
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdc = sdcProcessor.getPropertyList(sdcid);
        String auditflag = sdc.getProperty("auditedflag");
        return !auditflag.equalsIgnoreCase("N");
    }

    private int doRetestDataSet(HashMap props) {
        ArrayList<String> workitemlist = new ArrayList<String>();
        String sdcid = (String)props.get("sdcid");
        String keyid1 = (String)props.get("keyid1");
        String keyid2 = (String)props.get("keyid2");
        String keyid3 = (String)props.get("keyid3");
        String workitemid = (String)props.get("workitemid");
        String workitemversionid = (String)props.get("workitemversionid");
        String workiteminstance = (String)props.get("workiteminstance");
        String paramlistid = (String)props.get("paramlistid");
        String paramlistversionid = (String)props.get("paramlistversionid");
        String variantid = (String)props.get("variantid");
        String dataset = (String)props.get("dataset");
        String newdsstatus = (String)props.get("newdsstatus");
        String auditreason = (String)props.get("auditreason");
        String auditactivity = (String)props.get("auditactivity");
        String auditsignedflag = (String)props.get("auditsignedflag");
        if (auditreason == null) {
            auditreason = "";
        }
        if (auditactivity == null) {
            auditactivity = "";
        }
        if (auditsignedflag == null) {
            auditsignedflag = "";
        }
        DataSet dsProps = new DataSet();
        dsProps.addColumnValues("sdcid", 0, sdcid, ";");
        dsProps.addColumnValues("keyid1", 0, keyid1, ";");
        dsProps.addColumnValues("keyid2", 0, keyid2, ";");
        dsProps.addColumnValues("keyid3", 0, keyid3, ";");
        if (workitemid != null && workitemid.length() > 0) {
            if (workiteminstance != null && workiteminstance.length() > 0) {
                dsProps.addColumnValues("workitemid", 0, workitemid, ";");
                dsProps.addColumnValues("workiteminstance", 0, workiteminstance, ";");
                if (workitemversionid != null && workitemversionid.length() > 0) {
                    dsProps.addColumnValues("workitemversionid", 0, workitemversionid, ";");
                }
            } else {
                return this.setError("Invalid input: workiteminstance is null.");
            }
        }
        if (paramlistid != null && paramlistid.length() > 0 && paramlistversionid != null && paramlistversionid.length() > 0 && variantid != null && variantid.length() > 0 && dataset != null && dataset.length() > 0) {
            String[] var1 = StringUtil.split(paramlistid, ";");
            String[] var2 = StringUtil.split(paramlistversionid, ";");
            String[] var3 = StringUtil.split(variantid, ";");
            String[] var4 = StringUtil.split(dataset, ";");
            if (var1.length == var2.length && var2.length == var3.length && var3.length == var4.length) {
                dsProps.addColumnValues("paramlistid", 0, paramlistid, ";");
                dsProps.addColumnValues("paramlistversionid", 0, paramlistversionid, ";");
                dsProps.addColumnValues("variantid", 0, variantid, ";");
                dsProps.addColumnValues("dataset", 0, dataset, ";");
            } else {
                return this.setError("Invalid input: Action inputs are not valid.");
            }
        }
        dsProps.addColumnValues("newdsstatus", 0, newdsstatus, ";", "");
        dsProps.padColumns();
        dsProps.sort("keyid1,keyid2,keyid3");
        ArrayList<DataSet> keyGroups = dsProps.getGroupedDataSets("keyid1,keyid2,keyid3");
        for (int g = 0; g < keyGroups.size(); ++g) {
            DataSet ds = keyGroups.get(g);
            sdcid = ds.getString(0, "sdcid");
            SDI sdi = new SDI(sdcid, keyid1 = ds.getString(0, "keyid1"), keyid2 = ds.getString(0, "keyid2"), keyid3 = ds.getString(0, "keyid3"));
            if (!sdi.isValid()) {
                return this.setError("Invalid input: " + sdi.getKeyText());
            }
            if (ds.isValidColumn("workitemid")) {
                String[] workitemidlist = StringUtil.split(ds.getColumnValues("workitemid", ";"), ";");
                String[] workiteminstancelist = StringUtil.split(ds.getColumnValues("workiteminstance", ";"), ";");
                String[] newdsStatList = StringUtil.split(ds.getColumnValues("newdsstatus", ";"), ";");
                if (ds.isValidColumn("workitemversionid")) {
                    workitemversionid = ds.getColumnValues("workitemversionid", ";");
                } else {
                    workitemversionid = "";
                    StringBuffer sql = new StringBuffer();
                    SafeSQL safeSQL = new SafeSQL();
                    sql.append("SELECT workitemid, workitemversionid, workiteminstance FROM sdiworkitem ").append(" WHERE sdcid = " + safeSQL.addVar(sdcid)).append(" AND keyid1 = " + safeSQL.addVar(keyid1));
                    if (keyid2 != null && !"(null)".equalsIgnoreCase(keyid2) && keyid2.length() > 0) {
                        sql.append(" AND keyid2 = " + safeSQL.addVar(keyid2));
                    }
                    if (keyid3 != null && !"(null)".equalsIgnoreCase(keyid3) && keyid3.length() > 0) {
                        sql.append(" AND keyid3 = " + safeSQL.addVar(keyid3));
                    }
                    DataSet workitemversionDS = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    for (int i = 0; i < workitemidlist.length; ++i) {
                        HashMap<String, Object> findMap = new HashMap<String, Object>();
                        findMap.put("workitemid", workitemidlist[i]);
                        findMap.put("workiteminstance", new BigDecimal(workiteminstancelist[i]));
                        int rowindex = workitemversionDS.findRow(findMap);
                        workitemversionid = workitemversionid + ";" + workitemversionDS.getValue(rowindex, "workitemversionid");
                    }
                    workitemversionid = workitemversionid.substring(1);
                }
                String[] workitemversionlist = StringUtil.split(workitemversionid, ";");
                try {
                    if (workitemidlist.length > 0) {
                        for (int i = 0; i < workitemidlist.length; ++i) {
                            SDIWorkItem sdiworkitem = new SDIWorkItem(sdi, workitemidlist[i], workitemversionlist[i], workiteminstancelist[i], this.getConnectionProcessor().isOra());
                            if (!workitemlist.contains(sdiworkitem.getWorkitemID())) {
                                this.retestSDIWorkitem(sdiworkitem, newdsStatList[i], auditreason, auditactivity, auditsignedflag);
                            }
                            workitemlist.add(sdiworkitem.getWorkitemID());
                        }
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", sdcid);
                        actionProps.setProperty("keyid1", keyid1);
                        if (keyid2 != null && !"(null)".equalsIgnoreCase(keyid2) && keyid2.length() > 0) {
                            actionProps.setProperty("keyid2", keyid2);
                        }
                        if (keyid3 != null && !"(null)".equalsIgnoreCase(keyid3) && keyid3.length() > 0) {
                            actionProps.setProperty("keyid3", keyid3);
                        }
                        actionProps.setProperty("columnid", "moddt");
                        actionProps.setProperty("value", "n");
                        actionProps.setProperty("auditreason", auditreason);
                        actionProps.setProperty("auditactivity", auditactivity);
                        actionProps.setProperty("auditsignedflag", auditsignedflag);
                        this.getActionProcessor().processAction("SetSDIDate", "1", actionProps);
                    }
                }
                catch (SapphireException e) {
                    return this.setError("Exception caught: " + e.getMessage());
                }
            }
            if (!ds.isValidColumn("paramlistid")) continue;
            String[] var1 = StringUtil.split(ds.getColumnValues("paramlistid", ";"), ";");
            String[] var2 = StringUtil.split(ds.getColumnValues("paramlistversionid", ";"), ";");
            String[] var3 = StringUtil.split(ds.getColumnValues("variantid", ";"), ";");
            String[] var4 = StringUtil.split(ds.getColumnValues("dataset", ";"), ";");
            String[] newdsStatList = StringUtil.split(ds.getColumnValues("newdsstatus", ";"), ";");
            try {
                for (int i = 0; i < var1.length; ++i) {
                    SDIDataSet sdidataset = new SDIDataSet(sdi, var1[i], var2[i], var3[i], var4[i], this.getQueryProcessor());
                    SDIWorkItem sdiworkitem = sdidataset.getParentWorkItem();
                    if (sdiworkitem != null) {
                        if (workitemlist.contains(sdi.getKeyText() + sdiworkitem.getWorkitemID())) continue;
                        this.retestSDIWorkitem(sdiworkitem, newdsStatList[i], auditreason, auditactivity, auditsignedflag);
                        workitemlist.add(sdi.getKeyText() + sdiworkitem.getWorkitemID());
                        continue;
                    }
                    this.setError("Retest not allowed", "INFORMATION", "Selected Dataset does not belong to any Test.");
                }
                continue;
            }
            catch (Exception e) {
                return this.setError("Exception caught: " + e.getMessage());
            }
        }
        return 1;
    }

    private void retestSDIWorkitem(SDIWorkItem sdiworkitem, String newdsstatus, String auditreason, String auditactivity, String auditsignedflag) throws SapphireException {
        SDI sdi = sdiworkitem.getSDI();
        SafeSQL safeSQL = new SafeSQL();
        this.database.createPreparedResultSet("getsdiworkitemdetails", "SELECT groupid, groupinstance, workitemtypeflag, scheduleplanid, scheduleplanitemid, trackitemid, s_assigneddepartment FROM sdiworkitem  WHERE sdcid = " + safeSQL.addVar(sdi.getSdcid()) + " AND keyid1 = " + safeSQL.addVar(sdi.getKeyid1()) + " AND keyid2 = " + safeSQL.addVar(sdi.getKeyid2()) + " AND keyid3 = " + safeSQL.addVar(sdi.getKeyid3()) + " AND WORKITEMID = " + safeSQL.addVar(sdiworkitem.getWorkitemID()) + "  AND WORKITEMINSTANCE = " + safeSQL.addVar(sdiworkitem.getWorkitemInstance()), safeSQL.getValues());
        DataSet sdiwiDetails = new DataSet(this.database.getResultSet("getsdiworkitemdetails"));
        this.database.closeResultSet("getsdiworkitemdetails");
        HashMap<String, String> props = new HashMap<String, String>();
        props.put("sdcid", sdi.getSdcid());
        props.put("keyid1", sdi.getKeyid1());
        props.put("keyid2", sdi.getKeyid2());
        props.put("keyid3", sdi.getKeyid3());
        DataSet childds = this.getChildrenSDIWorkItems(sdiworkitem);
        ActionProcessor ap = this.getActionProcessor();
        List list = this.getSDIDataSets(sdiworkitem, childds);
        props.put("paramlistid", (String)list.get(0));
        props.put("paramlistversionid", (String)list.get(1));
        props.put("variantid", (String)list.get(2));
        props.put("dataset", (String)list.get(3));
        props.put("s_retestedflag", "Y");
        props.put("auditreason", auditreason);
        props.put("auditactivity", auditactivity);
        props.put("auditsignedflag", auditsignedflag);
        ap.processAction("EditDataSet", "1", props);
        this.editSDIWorkitem(sdiworkitem, childds, "s_retestedflag", "Y", auditreason, auditactivity, auditsignedflag);
        this.logger.debug("Adding Workitem " + sdiworkitem.getWorkitemID() + "...");
        props.remove("paramlistid");
        props.remove("paramlistversionid");
        props.remove("variantid");
        props.remove("dataset");
        props.remove("s_retestedflag");
        props.put("workitemid", sdiworkitem.getWorkitemID());
        props.put("workitemversionid", sdiworkitem.getWorkitemVersionID());
        props.put("applyworkitem", "Y");
        props.put("forcenew", "R");
        props.put("auditreason", auditreason);
        props.put("auditactivity", auditactivity);
        props.put("auditsignedflag", auditsignedflag);
        props.put("wapstatus", "Never");
        String assignedDepartment = "";
        if (sdiwiDetails.getRowCount() > 0) {
            if (sdiwiDetails.getValue(0, "groupid", "").length() > 0 && sdiwiDetails.getValue(0, "workitemtypeflag", "").equals("W")) {
                props.put("groupid", sdiwiDetails.getValue(0, "groupid", ""));
                props.put("groupinstance", sdiwiDetails.getValue(0, "groupinstance", ""));
            }
            props.put("scheduleplanid", sdiwiDetails.getString(0, "scheduleplanid"));
            props.put("scheduleplanitemid", sdiwiDetails.getString(0, "scheduleplanitemid"));
            props.put("trackitemid", sdiwiDetails.getString(0, "trackitemid"));
            assignedDepartment = sdiwiDetails.getString(0, "s_assigneddepartment", "");
        }
        ap.processAction("AddSDIWorkitem", "1", props);
        String[] addedinstance = StringUtil.split((String)props.get("workiteminstance"), ";");
        String newinstance = addedinstance[addedinstance.length - 1];
        if (newinstance == null || newinstance.length() == 0) {
            newinstance = Integer.toString(this.getNextSDIWorkitemInstance(sdiworkitem) - 1);
        }
        this.logger.debug("Setting retest instance on Workitem...");
        String oldinstance = sdiworkitem.getWorkitemInstance();
        sdiworkitem.setWorkitemInstance(newinstance);
        childds = this.getChildrenSDIWorkItems(sdiworkitem);
        StringBuffer columns = new StringBuffer("s_retestinstance");
        StringBuffer values = new StringBuffer(oldinstance);
        if (assignedDepartment.length() > 0) {
            columns.append(";").append("s_assigneddepartment");
            values.append(";").append(assignedDepartment);
        }
        this.editSDIWorkitem(sdiworkitem, childds, columns.toString(), values.toString(), auditreason, auditactivity, auditsignedflag);
        if (newdsstatus != null && newdsstatus.length() > 0) {
            this.logger.debug("Setting added Dataset's status...");
            list = this.getSDIDataSets(sdiworkitem, childds);
            props.remove("workiteminstance");
            props.put("paramlistid", (String)list.get(0));
            props.put("paramlistversionid", (String)list.get(1));
            props.put("variantid", (String)list.get(2));
            props.put("dataset", (String)list.get(3));
            props.put(DSSTATUSCOLUMN, newdsstatus);
            props.put("s_retestedflag", "(none)");
            props.put("auditreason", auditreason);
            props.put("auditactivity", auditactivity);
            props.put("auditsignedflag", auditsignedflag);
            ap.processAction("EditDataSet", "1", props);
        }
    }

    private DataSet getChildrenSDIWorkItems(SDIWorkItem sdiworkitem) {
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT c.workitemid, c.workiteminstance FROM sdiworkitem p INNER JOIN sdiworkitem c ON p.sdcid = c.sdcid AND p.keyid1 = c.keyid1 AND p.keyid2 = c.keyid2 AND p.keyid3 = c.keyid3 AND p.groupid = c.groupid AND p.groupinstance = c.groupinstance WHERE p.sdcid = " + safeSQL.addVar(sdiworkitem.getSDI().getSdcid()) + " AND p.keyid1 = " + safeSQL.addVar(sdiworkitem.getSDI().getKeyid1()) + " AND p.keyid2 = " + safeSQL.addVar(sdiworkitem.getSDI().getKeyid2()) + " AND p.keyid3 = " + safeSQL.addVar(sdiworkitem.getSDI().getKeyid3()) + " AND p.workitemid = " + safeSQL.addVar(sdiworkitem.getWorkitemID()) + " AND p.workiteminstance = " + safeSQL.addVar(sdiworkitem.getWorkitemInstance()) + " AND c.workitemtypeflag = 'W' AND p.workitemtypeflag = 'P'";
        DataSet childds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return childds;
    }

    private int doGetWorkitemItem(HashMap props) {
        int rc = 1;
        String workitemid = ((String)props.get("workitemid")).trim();
        String workitemversionid = ((String)props.get("workitemversionid")).trim();
        String sdcid = ((String)props.get("sdcid")).trim();
        if (workitemid == null || workitemid.length() == 0) {
            return this.setError("Workitem ID is not given.");
        }
        if (sdcid == null || sdcid.length() == 0) {
            return this.setError("No SDC specified.");
        }
        List list = this.getWorkitemItem(workitemid, workitemversionid, sdcid);
        props.put("keyid1", list.get(0));
        props.put("keyid2", list.get(1));
        props.put("keyid3", list.get(2));
        return rc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private List getWorkitemItem(String workitemid, String workitemversionid, String sdcid) {
        ArrayList<String> list = new ArrayList<String>();
        StringBuffer keyid1 = new StringBuffer();
        StringBuffer keyid2 = new StringBuffer();
        StringBuffer keyid3 = new StringBuffer();
        try {
            SafeSQL safeSQL = this.__SqlGenerator.getWorkitemDetails(workitemid, workitemversionid, sdcid);
            this.database.createPreparedResultSet("_rs_doGetWorkitemItem_0", safeSQL.getPreparedSQL(), safeSQL.getValues());
            while (this.database.getNext("_rs_doGetWorkitemItem_0")) {
                keyid1.append(this.database.getString("_rs_doGetWorkitemItem_0", "keyid1")).append(";");
                keyid2.append(this.database.getString("_rs_doGetWorkitemItem_0", "keyid2")).append(";");
                keyid3.append(this.database.getString("_rs_doGetWorkitemItem_0", "keyid3")).append(";");
            }
        }
        catch (SapphireException e) {
            this.logger.error("getWorkitemItem error", e);
        }
        finally {
            this.database.closeResultSet("_rs_doGetWorkitemItem_0");
        }
        if (keyid1.length() > 0) {
            keyid1.deleteCharAt(keyid1.length() - 1);
        }
        if (keyid2.length() > 0) {
            keyid2.deleteCharAt(keyid2.length() - 1);
        }
        if (keyid3.length() > 0) {
            keyid3.deleteCharAt(keyid3.length() - 1);
        }
        list.add(keyid1.toString());
        list.add(keyid2.toString());
        list.add(keyid3.toString());
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private List getSDIWorkitemItem(SDIWorkItem sdiworkitem, DataSet childds, String itemsdcid) {
        ArrayList<String> list = new ArrayList<String>();
        SDI sdi = sdiworkitem.getSDI();
        StringBuffer itemkeyid1 = new StringBuffer();
        StringBuffer itemkeyid2 = new StringBuffer();
        StringBuffer itemkeyid3 = new StringBuffer();
        StringBuffer iteminstance = new StringBuffer();
        try {
            SafeSQL safeSQL = this.__SqlGenerator.getSdiWorkitemDetails(sdi, sdiworkitem.getWorkitemID(), sdiworkitem.getWorkitemInstance(), itemsdcid);
            this.database.createPreparedResultSet("_rs_doGetWorkitemItem_0", safeSQL.getPreparedSQL(), safeSQL.getValues());
            while (this.database.getNext("_rs_doGetWorkitemItem_0")) {
                itemkeyid1.append(this.database.getString("_rs_doGetWorkitemItem_0", "ITEMKEYID1")).append(";");
                itemkeyid2.append(this.database.getString("_rs_doGetWorkitemItem_0", "ITEMKEYID2")).append(";");
                itemkeyid3.append(this.database.getString("_rs_doGetWorkitemItem_0", "ITEMKEYID3")).append(";");
                iteminstance.append(this.database.getString("_rs_doGetWorkitemItem_0", "ITEMINSTANCE")).append(";");
            }
        }
        catch (SapphireException e) {
            this.logger.error("getSDIWorkitemItem error", e);
        }
        finally {
            this.database.closeResultSet("_rs_doGetWorkitemItem_0");
        }
        for (int i = 0; i < childds.size(); ++i) {
            try {
                SafeSQL safeSQL = this.__SqlGenerator.getSdiWorkitemDetails(sdi, childds.getValue(i, "workitemid"), childds.getValue(i, "workiteminstance"), itemsdcid);
                this.database.createPreparedResultSet("_rs_doGetWorkitemItem_0", safeSQL.getPreparedSQL(), safeSQL.getValues());
                while (this.database.getNext("_rs_doGetWorkitemItem_0")) {
                    itemkeyid1.append(this.database.getString("_rs_doGetWorkitemItem_0", "ITEMKEYID1")).append(";");
                    itemkeyid2.append(this.database.getString("_rs_doGetWorkitemItem_0", "ITEMKEYID2")).append(";");
                    itemkeyid3.append(this.database.getString("_rs_doGetWorkitemItem_0", "ITEMKEYID3")).append(";");
                    iteminstance.append(this.database.getString("_rs_doGetWorkitemItem_0", "ITEMINSTANCE")).append(";");
                }
                continue;
            }
            catch (SapphireException e) {
                this.logger.error("getSDIWorkitemItem error", e);
                continue;
            }
            finally {
                this.database.closeResultSet("_rs_doGetWorkitemItem_0");
            }
        }
        if (itemkeyid1.length() > 0) {
            itemkeyid1.deleteCharAt(itemkeyid1.length() - 1);
        }
        if (itemkeyid2.length() > 0) {
            itemkeyid2.deleteCharAt(itemkeyid2.length() - 1);
        }
        if (itemkeyid3.length() > 0) {
            itemkeyid3.deleteCharAt(itemkeyid3.length() - 1);
        }
        if (iteminstance.length() > 0) {
            iteminstance.deleteCharAt(iteminstance.length() - 1);
        }
        list.add(itemkeyid1.toString());
        list.add(itemkeyid2.toString());
        list.add(itemkeyid3.toString());
        list.add(iteminstance.toString());
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private List getSDIDataSets(SDIWorkItem sdiworkitem, DataSet childds) {
        ArrayList<String> list = new ArrayList<String>();
        SDI sdi = sdiworkitem.getSDI();
        StringBuffer itemkeyid1 = new StringBuffer();
        StringBuffer itemkeyid2 = new StringBuffer();
        StringBuffer itemkeyid3 = new StringBuffer();
        StringBuffer iteminstance = new StringBuffer();
        try {
            SafeSQL safeSQL = this.__SqlGenerator.getSdiWorkitemDataSets(sdi, sdiworkitem.getWorkitemID(), sdiworkitem.getWorkitemInstance());
            this.database.createPreparedResultSet("_rs_doGetWorkitemItem_0", safeSQL.getPreparedSQL(), safeSQL.getValues());
            while (this.database.getNext("_rs_doGetWorkitemItem_0")) {
                itemkeyid1.append(this.database.getString("_rs_doGetWorkitemItem_0", "PARAMLISTID")).append(";");
                itemkeyid2.append(this.database.getString("_rs_doGetWorkitemItem_0", "PARAMLISTVERSIONID")).append(";");
                itemkeyid3.append(this.database.getString("_rs_doGetWorkitemItem_0", "VARIANTID")).append(";");
                iteminstance.append(this.database.getString("_rs_doGetWorkitemItem_0", "DATASET")).append(";");
            }
        }
        catch (SapphireException e) {
            this.logger.error("getSDIDataSets error", e);
        }
        finally {
            this.database.closeResultSet("_rs_doGetWorkitemItem_0");
        }
        for (int i = 0; i < childds.size(); ++i) {
            try {
                SafeSQL safeSQL = this.__SqlGenerator.getSdiWorkitemDataSets(sdi, childds.getValue(i, "workitemid"), childds.getValue(i, "workiteminstance"));
                this.database.createPreparedResultSet("_rs_doGetWorkitemItem_0", safeSQL.getPreparedSQL(), safeSQL.getValues());
                while (this.database.getNext("_rs_doGetWorkitemItem_0")) {
                    itemkeyid1.append(this.database.getString("_rs_doGetWorkitemItem_0", "PARAMLISTID")).append(";");
                    itemkeyid2.append(this.database.getString("_rs_doGetWorkitemItem_0", "PARAMLISTVERSIONID")).append(";");
                    itemkeyid3.append(this.database.getString("_rs_doGetWorkitemItem_0", "VARIANTID")).append(";");
                    iteminstance.append(this.database.getString("_rs_doGetWorkitemItem_0", "DATASET")).append(";");
                }
                continue;
            }
            catch (SapphireException e) {
                this.logger.error("getSDIDataSets error", e);
                continue;
            }
            finally {
                this.database.closeResultSet("_rs_doGetWorkitemItem_0");
            }
        }
        if (itemkeyid1.length() > 0) {
            itemkeyid1.deleteCharAt(itemkeyid1.length() - 1);
        }
        if (itemkeyid2.length() > 0) {
            itemkeyid2.deleteCharAt(itemkeyid2.length() - 1);
        }
        if (itemkeyid3.length() > 0) {
            itemkeyid3.deleteCharAt(itemkeyid3.length() - 1);
        }
        if (iteminstance.length() > 0) {
            iteminstance.deleteCharAt(iteminstance.length() - 1);
        }
        list.add(itemkeyid1.toString());
        list.add(itemkeyid2.toString());
        list.add(itemkeyid3.toString());
        list.add(iteminstance.toString());
        return list;
    }

    private void editSDIWorkitem(SDIWorkItem sdiworkitem, DataSet childds, String column, String value, String auditreason, String auditactivity, String auditsignedflag) throws SapphireException {
        SDI sdi = sdiworkitem.getSDI();
        HashMap<String, String> actionProps = new HashMap<String, String>();
        actionProps.put("sdcid", sdi.getSdcid());
        actionProps.put("keyid1", sdi.getKeyid1());
        actionProps.put("keyid2", sdi.getKeyid2());
        actionProps.put("keyid3", sdi.getKeyid3());
        if (childds.size() > 0) {
            actionProps.put("workitemid", sdiworkitem.getWorkitemID() + ";" + childds.getColumnValues("workitemid", ";"));
            actionProps.put("workiteminstance", sdiworkitem.getWorkitemInstance() + ";" + childds.getColumnValues("workiteminstance", ";"));
        } else {
            actionProps.put("workitemid", sdiworkitem.getWorkitemID());
            actionProps.put("workiteminstance", sdiworkitem.getWorkitemInstance());
        }
        actionProps.put("auditreason", auditreason);
        actionProps.put("auditactivity", auditactivity);
        actionProps.put("auditsignedflag", auditsignedflag);
        String[] columns = StringUtil.split(column, ";");
        String[] values = StringUtil.split(value, ";");
        for (int c = 0; c < columns.length; ++c) {
            if (values[c].equals("null") || values[c].length() <= 0) continue;
            actionProps.put(columns[c], values[c]);
        }
        this.getActionProcessor().processAction("EditSDIWorkItem", "1", actionProps);
    }

    private int doIsAnyDataReleased(HashMap props) {
        int rc = 1;
        String sdcid = (String)props.get("sdcid");
        String keyid1 = (String)props.get("keyid1");
        String keyid2 = (String)props.get("keyid2");
        String keyid3 = (String)props.get("keyid3");
        String dataitemflag = (String)props.get("checkdataitem");
        int count = 0;
        props.put("result", "No");
        SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
        sdcid = sdi.getSdcid();
        keyid1 = sdi.getKeyid1();
        keyid2 = sdi.getKeyid2();
        keyid3 = sdi.getKeyid3();
        if (!sdi.isValid()) {
            return this.setError("Missing required input(s).");
        }
        props.put("keyid2", keyid2);
        props.put("keyid3", keyid3);
        if (dataitemflag.equalsIgnoreCase("Y")) {
            try {
                SafeSQL safeSQL = this.__SqlGenerator.getReleasedCount(sdi);
                this.database.createPreparedResultSet("_rs_IsAnyDatasetReleased_001", safeSQL.getPreparedSQL(), safeSQL.getValues());
                if (this.database.getNext("_rs_IsAnyDatasetReleased_001")) {
                    count = this.database.getInt("_rs_IsAnyDatasetReleased_001", "RELEASEDCOUNT");
                }
                this.database.closeResultSet("_rs_IsAnyDatasetReleased_001");
            }
            catch (SapphireException e) {
                return this.setError("OPAL-ERROR: SapphireException caught: " + e.getMessage());
            }
            catch (Exception e) {
                return this.setError("OPAL-ERROR: Exception caught: " + e.getMessage());
            }
            if (count == 0) {
                props.put("result", "No");
            } else {
                props.put("result", "Yes");
            }
        } else {
            ArrayList<String> dslist = new ArrayList<String>();
            try {
                SafeSQL safeSQL = this.__SqlGenerator.getDatasetKey(sdi);
                this.database.createPreparedResultSet("_rs_IsAnyDatasetReleased_002", safeSQL.getPreparedSQL(), safeSQL.getValues());
                while (this.database.getNext("_rs_IsAnyDatasetReleased_002")) {
                    dslist.add(this.database.getString("_rs_IsAnyDatasetReleased_002", "DATASET"));
                }
                this.database.closeResultSet("_rs_IsAnyDatasetReleased_002");
            }
            catch (SapphireException e) {
                return this.setError("OPAL-ERROR: SapphireException caught: " + e.getMessage());
            }
            catch (Exception e) {
                return this.setError("OPAL-ERROR: Exception caught: " + e.getMessage());
            }
            try {
                for (int i = 0; i < dslist.size(); ++i) {
                    SafeSQL safeSQL = this.__SqlGenerator.getNotReleasedCount(sdi, (String)dslist.get(i));
                    this.database.createPreparedResultSet("_rs_IsAnyDatasetReleased_003", safeSQL.getPreparedSQL(), safeSQL.getValues());
                    if (this.database.getNext("_rs_IsAnyDatasetReleased_003")) {
                        count = this.database.getInt("_rs_IsAnyDatasetReleased_003", "NORELEASEDCOUNT");
                    }
                    this.database.closeResultSet("_rs_IsAnyDatasetReleased_003");
                    if (count != 0) continue;
                    props.put("result", "Yes");
                    break;
                }
            }
            catch (SapphireException e) {
                return this.setError("OPAL-ERROR: SapphireException caught: " + e.getMessage());
            }
            catch (Exception e) {
                return this.setError("OPAL-ERROR: Exception caught: " + e.getMessage());
            }
        }
        return rc;
    }

    private int doChangeSampleStatus(HashMap props) {
        int rc = 1;
        ActionProcessor ap = this.getActionProcessor();
        try {
            ap.processAction("RestartWorkflow", "1", props);
        }
        catch (ActionException e) {
            this.logger.error("Error running RestartWorkflow", e);
        }
        return rc;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int doGetSDIDataSet(HashMap props) {
        int rc = 1;
        String sdcid = (String)props.get("sdcid");
        String keyid1 = (String)props.get("keyid1");
        String keyid2 = (String)props.get("keyid2");
        String keyid3 = (String)props.get("keyid3");
        SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
        sdcid = sdi.getSdcid();
        keyid1 = sdi.getKeyid1();
        keyid2 = sdi.getKeyid2();
        keyid3 = sdi.getKeyid3();
        int count = 0;
        try {
            List list = this.getSDIDataSet(sdi);
            List list_1 = (List)list.get(0);
            List list_2 = (List)list.get(1);
            List list_3 = (List)list.get(2);
            List list_4 = (List)list.get(3);
            StringBuffer ds1 = new StringBuffer();
            StringBuffer ds2 = new StringBuffer();
            StringBuffer ds3 = new StringBuffer();
            StringBuffer ds4 = new StringBuffer();
            for (int i = 0; i < list_1.size(); ++i) {
                ds1.append(list_1.get(i)).append(";");
                ds2.append(list_2.get(i)).append(";");
                ds3.append(list_3.get(i)).append(";");
                ds4.append(list_4.get(i)).append(";");
            }
            if (ds1.length() > 0) {
                ds1.deleteCharAt(ds1.length() - 1);
                ds2.deleteCharAt(ds2.length() - 1);
                ds3.deleteCharAt(ds3.length() - 1);
                ds4.deleteCharAt(ds4.length() - 1);
            }
            props.put("paramlistid", ds1.toString());
            props.put("paramlistversionid", ds2.toString());
            props.put("variantid", ds3.toString());
            props.put("dataset", ds4.toString());
        }
        catch (SapphireException e) {
            int n = this.setError("Unable to get DataSet for SDI. Exception caught: " + e.getMessage());
            return n;
        }
        finally {
            props.put("count", Integer.toString(count));
        }
        return rc;
    }

    private List getSDIDataSet(SDI sdi) throws SapphireException {
        ArrayList list = new ArrayList();
        ArrayList<String> list_0 = new ArrayList<String>();
        ArrayList<String> list_1 = new ArrayList<String>();
        ArrayList<String> list_2 = new ArrayList<String>();
        ArrayList<String> list_3 = new ArrayList<String>();
        ArrayList<String> status = new ArrayList<String>();
        SafeSQL safeSQL = this.__SqlGenerator.getSdiDatasets(sdi);
        this.database.createPreparedResultSet("_rs_getSDIDataSet_001", safeSQL.getPreparedSQL(), safeSQL.getValues());
        while (this.database.getNext("_rs_getSDIDataSet_001")) {
            list_0.add(this.database.getString("_rs_getSDIDataSet_001", "PARAMLISTID"));
            list_1.add(this.database.getString("_rs_getSDIDataSet_001", "PARAMLISTVERSIONID"));
            list_2.add(this.database.getString("_rs_getSDIDataSet_001", "VARIANTID"));
            list_3.add(Integer.toString(this.database.getInt("_rs_getSDIDataSet_001", "DATASET")));
            status.add(this.database.getString("_rs_getSDIDataSet_001", "S_DATASETSTATUS"));
        }
        this.database.closeResultSet("_rs_getSDIDataSet_001");
        list.add(list_0);
        list.add(list_1);
        list.add(list_2);
        list.add(list_3);
        list.add(status);
        return list;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private int getNextSDIWorkitemInstance(SDIWorkItem sdiworkitem) {
        int nextInstance = 0;
        SDI sdi = sdiworkitem.getSDI();
        try {
            SafeSQL safeSQL = this.__SqlGenerator.getMaxSdiWorkitemInstance(sdi, sdiworkitem.getWorkitemID());
            this.database.createPreparedResultSet("_rs_getNextSDIWorkitemInstance_0", safeSQL.getPreparedSQL(), safeSQL.getValues());
            if (this.database.getNext("_rs_getNextSDIWorkitemInstance_0")) {
                nextInstance = this.database.getInt("_rs_getNextSDIWorkitemInstance_0", "WORKITEMINSTANCE");
            }
        }
        catch (SapphireException e) {
            this.logger.error("getNextSDIWorkitemInstance error", e);
        }
        finally {
            this.database.closeResultSet("_rs_getNextSDIWorkitemInstance_0");
        }
        return ++nextInstance;
    }

    private int doCopySDIDataSet(HashMap props) {
        int i;
        String[] targetkey1array;
        int rc = 1;
        String targetsdcid = (String)props.get("sdcid");
        String targetkeyid1 = (String)props.get("keyid1");
        String targetkeyid2 = (String)props.get("keyid2");
        String targetkeyid3 = (String)props.get("keyid3");
        String sourcesdcid = (String)props.get("sourcesdcid");
        String sourcekeyid1 = (String)props.get("sourcekeyid1");
        String sourcekeyid2 = (String)props.get("sourcekeyid2");
        String sourcekeyid3 = (String)props.get("sourcekeyid3");
        String paramlistids = (String)props.get("paramlistid");
        String paramlistversionids = (String)props.get("paramlistversionid");
        String variantids = (String)props.get("variantid");
        if (sourcesdcid == null || sourcesdcid.length() == 0 || sourcekeyid1 == null || sourcekeyid1.length() == 0) {
            return this.setError("Invalid input : Source input missing.");
        }
        if (targetsdcid == null || targetsdcid.length() == 0 || targetkeyid1 == null || targetkeyid1.length() == 0) {
            return this.setError("Invalid input : Target input missing.");
        }
        String[] sourcekey1array = StringUtil.split(sourcekeyid1, ";");
        if (sourcekey1array.length != (targetkey1array = StringUtil.split(targetkeyid1, ";")).length) {
            return this.setError("Invalid input : count of target keyid1 not matching with count of source keyid1 .");
        }
        String[] sourcekey2array = null;
        String[] sourcekey3array = null;
        String[] targetkey2array = null;
        String[] targetkey3array = null;
        if (sourcekeyid2 != null && sourcekeyid2.length() > 0) {
            sourcekey2array = StringUtil.split(sourcekeyid2, ";");
            if (sourcekey1array.length != sourcekey2array.length) {
                return this.setError("Invalid input : count of source keyid2 not matching with count of source keyid1 .");
            }
        } else {
            sourcekey2array = new String[sourcekey1array.length];
            for (i = 0; i < sourcekey2array.length; ++i) {
                sourcekey2array[i] = "(null)";
            }
        }
        if (sourcekeyid3 != null && sourcekeyid3.length() > 0) {
            sourcekey3array = StringUtil.split(sourcekeyid3, ";");
            if (sourcekey1array.length != sourcekey3array.length) {
                return this.setError("Invalid input : count of source keyid3 not matching with count of source keyid1 .");
            }
        } else {
            sourcekey3array = new String[sourcekey1array.length];
            for (i = 0; i < sourcekey3array.length; ++i) {
                sourcekey3array[i] = "(null)";
            }
        }
        if (targetkeyid2 != null && targetkeyid2.length() > 0) {
            targetkey2array = StringUtil.split(targetkeyid2, ";");
            if (targetkey2array.length != targetkey1array.length) {
                return this.setError("Invalid input : count of target keyid2 not matching with count of target keyid1 .");
            }
        } else {
            targetkey2array = new String[targetkey1array.length];
            for (i = 0; i < targetkey2array.length; ++i) {
                targetkey2array[i] = "(null)";
            }
        }
        if (targetkeyid3 != null && targetkeyid3.length() > 0) {
            targetkey3array = StringUtil.split(targetkeyid3, ";");
            if (targetkey3array.length != targetkey1array.length) {
                return this.setError("Invalid input : count of target keyid3 not matching with count of target keyid1 .");
            }
        } else {
            targetkey3array = new String[targetkey1array.length];
            for (i = 0; i < targetkey3array.length; ++i) {
                targetkey3array[i] = "(null)";
            }
        }
        if (paramlistids != null && paramlistids.length() > 0 && paramlistversionids != null && paramlistversionids.length() > 0 && variantids != null && variantids.length() > 0) {
            String[] paramlistidarray = StringUtil.split(paramlistids, ";");
            String[] paramlistversionidarray = StringUtil.split(paramlistversionids, ";");
            String[] variantidarray = StringUtil.split(variantids, ";");
            if (paramlistidarray.length != paramlistversionidarray.length || paramlistidarray.length != variantidarray.length) {
                return this.setError("Invalid input :Count of paramlistids not equal to versionids or variantids.");
            }
        }
        try {
            ActionProcessor ap = this.getActionProcessor();
            HashMap<String, String> addDataSetProps = new HashMap<String, String>();
            addDataSetProps.put("sdcid", targetsdcid);
            addDataSetProps.put("addnewonly", "N");
            StringBuffer sbParamlistIds = new StringBuffer();
            StringBuffer sbParamlistVersionIds = new StringBuffer();
            StringBuffer sbVariantIds = new StringBuffer();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT KEYID1, KEYID2, KEYID3, PARAMLISTID, PARAMLISTVERSIONID, VARIANTID, DATASET ").append(" FROM SDIDATA ").append(" WHERE SDCID = ? AND KEYID1 = ?  AND KEYID2 = ? AND KEYID3 = ? ");
            PreparedStatement psmtGetSDIDataset = this.database.prepareStatement("GetSDIDataset", sql.toString());
            DataSet sourceDatasets = new DataSet();
            if (paramlistids != null && paramlistids.trim().length() > 0) {
                addDataSetProps.put("keyid1", targetkeyid1);
                addDataSetProps.put("keyid2", targetkeyid2);
                addDataSetProps.put("keyid3", targetkeyid3);
                addDataSetProps.put("paramlistid", paramlistids);
                addDataSetProps.put("paramlistversionid", paramlistversionids);
                addDataSetProps.put("variantid", variantids);
                ap.processAction("AddDataSet", "1", addDataSetProps);
            } else {
                DataSet propDs = new DataSet();
                propDs.addColumn("keyid1", 0);
                propDs.addColumn("keyid2", 0);
                propDs.addColumn("keyid3", 0);
                propDs.addColumn("paramlistid", 0);
                propDs.addColumn("paramlistversionid", 0);
                propDs.addColumn("variantid", 0);
                for (int i2 = 0; i2 < sourcekey1array.length; ++i2) {
                    sbParamlistIds.setLength(0);
                    sbParamlistVersionIds.setLength(0);
                    sbVariantIds.setLength(0);
                    try {
                        psmtGetSDIDataset.setString(1, sourcesdcid);
                        psmtGetSDIDataset.setString(2, sourcekey1array[i2]);
                        psmtGetSDIDataset.setString(3, sourcekey2array[i2]);
                        psmtGetSDIDataset.setString(4, sourcekey3array[i2]);
                        DataSet ds = new DataSet(psmtGetSDIDataset.executeQuery());
                        for (int k = 0; k < ds.getRowCount(); ++k) {
                            int row = propDs.addRow();
                            propDs.setString(row, "keyid1", targetkey1array[i2]);
                            propDs.setString(row, "keyid2", targetkey2array[i2]);
                            propDs.setString(row, "keyid3", targetkey3array[i2]);
                            propDs.setString(row, "paramlistid", ds.getValue(k, "paramlistid"));
                            propDs.setString(row, "paramlistversionid", ds.getValue(k, "paramlistversionid"));
                            propDs.setString(row, "variantid", ds.getValue(k, "variantid"));
                            sourceDatasets.copyRow(ds, k, 1);
                        }
                        continue;
                    }
                    catch (Exception e) {
                        return this.setError("Unable to get DataSet for source SDI. Exception caught: " + e.getMessage(), e);
                    }
                }
                if (propDs.getRowCount() > 0) {
                    addDataSetProps.put("keyid1", propDs.getColumnValues("keyid1", ";"));
                    addDataSetProps.put("keyid2", propDs.getColumnValues("keyid2", ";"));
                    addDataSetProps.put("keyid3", propDs.getColumnValues("keyid3", ";"));
                    addDataSetProps.put("paramlistid", propDs.getColumnValues("paramlistid", ";"));
                    addDataSetProps.put("paramlistversionid", propDs.getColumnValues("paramlistversionid", ";"));
                    addDataSetProps.put("variantid", propDs.getColumnValues("variantid", ";"));
                    addDataSetProps.put("propsmatch", "Y");
                    ap.processAction("AddDataSet", "1", addDataSetProps);
                }
            }
            StringBuffer addkeyid1s = new StringBuffer();
            StringBuffer addkeyid2s = new StringBuffer();
            StringBuffer addkeyid3s = new StringBuffer();
            StringBuffer addparamlistids = new StringBuffer();
            StringBuffer addparamlistversionids = new StringBuffer();
            StringBuffer addvariantids = new StringBuffer();
            StringBuffer adddatasets = new StringBuffer();
            StringBuffer addparamids = new StringBuffer();
            StringBuffer addparamtypes = new StringBuffer();
            StringBuffer numreplicate = new StringBuffer();
            StringBuffer editkeyid1s = new StringBuffer();
            StringBuffer editkeyid2s = new StringBuffer();
            StringBuffer editkeyid3s = new StringBuffer();
            StringBuffer editparamlistids = new StringBuffer();
            StringBuffer editparamlistversionids = new StringBuffer();
            StringBuffer editvariantids = new StringBuffer();
            StringBuffer editdatasets = new StringBuffer();
            StringBuffer editparamids = new StringBuffer();
            StringBuffer editparamtypes = new StringBuffer();
            StringBuffer replicateids = new StringBuffer();
            StringBuffer enteredtexts = new StringBuffer();
            HashMap<String, Integer> numReplicateMap = new HashMap<String, Integer>();
            ArrayList<String> mapkeyarray = new ArrayList<String>();
            sql.setLength(0);
            sql.append("SELECT PARAMID, PARAMTYPE, REPLICATEID, MANDATORYFLAG, RELEASEDFLAG");
            sql.append(", ENTEREDVALUE , ENTEREDTEXT, TRANSFORMVALUE, DISPLAYFORMAT, USERSEQUENCE ");
            sql.append(" FROM SDIDATAITEM");
            sql.append(" WHERE SDCID = '" + sourcesdcid + "'");
            sql.append(" AND KEYID1 = ?  AND KEYID2 = ? AND KEYID3 = ? ");
            sql.append(" AND PARAMLISTID = ? AND PARAMLISTVERSIONID = ? AND VARIANTID = ? AND DATASET = ?");
            sql.append(" ORDER BY PARAMTYPE, PARAMID, REPLICATEID ");
            if (sourceDatasets != null && sourceDatasets.getRowCount() > 0) {
                PreparedStatement psmtGetDataItems = this.database.prepareStatement("GetDataItems", sql.toString());
                for (int i3 = 0; i3 < sourceDatasets.getRowCount(); ++i3) {
                    String keyid1 = sourceDatasets.getValue(i3, "keyid1");
                    String keyid2 = sourceDatasets.getValue(i3, "keyid2");
                    String keyid3 = sourceDatasets.getValue(i3, "keyid3");
                    String paramlistid = sourceDatasets.getValue(i3, "paramlistid");
                    String paramlistversionid = sourceDatasets.getValue(i3, "paramlistversionid");
                    String variantid = sourceDatasets.getValue(i3, "variantid");
                    String dataset = sourceDatasets.getValue(i3, "dataset");
                    SDI _sourcesdi = new SDI(sourcesdcid, keyid1, keyid2, keyid3);
                    SDIDataSet sdidataset = new SDIDataSet(_sourcesdi, paramlistid, paramlistversionid, variantid, dataset, this.getQueryProcessor());
                    psmtGetDataItems.setString(1, keyid1);
                    psmtGetDataItems.setString(2, keyid2);
                    psmtGetDataItems.setString(3, keyid3);
                    psmtGetDataItems.setString(4, paramlistid);
                    psmtGetDataItems.setString(5, paramlistversionid);
                    psmtGetDataItems.setString(6, variantid);
                    psmtGetDataItems.setString(7, dataset);
                    DataSet dsDataItems = new DataSet(psmtGetDataItems.executeQuery());
                    List sourcedataitemList = this.getDataItemList(dsDataItems, sdidataset);
                    if (sourcedataitemList == null || sourcedataitemList.size() == 0) continue;
                    numReplicateMap.clear();
                    mapkeyarray.clear();
                    for (int k = 0; k < sourcedataitemList.size(); ++k) {
                        DataItem sourceDataItem = (DataItem)sourcedataitemList.get(k);
                        String mapkey = "";
                        String enteredText = sourceDataItem.getEnteredText();
                        if (enteredText == null) {
                            enteredText = "(null)";
                        }
                        if (numReplicateMap.containsKey(mapkey = sourceDataItem.getParamID() + sourceDataItem.getParamType())) {
                            Integer numrep = (Integer)numReplicateMap.get(mapkey);
                            numrep = new Integer(numrep + 1);
                            numReplicateMap.put(mapkey, numrep);
                        } else {
                            addkeyid1s.append(targetkey1array[i3] + ";");
                            addkeyid2s.append(targetkey2array[i3] + ";");
                            addkeyid3s.append(targetkey3array[i3] + ";");
                            addparamlistids.append(paramlistid + ";");
                            addparamlistversionids.append(paramlistversionid + ";");
                            addvariantids.append(variantid + ";");
                            adddatasets.append(dataset + ";");
                            addparamids.append(sourceDataItem.getParamID() + ";");
                            addparamtypes.append(sourceDataItem.getParamType() + ";");
                            mapkeyarray.add(mapkey);
                            numReplicateMap.put(mapkey, new Integer(1));
                        }
                        editkeyid1s.append(targetkey1array[i3] + ";");
                        editkeyid2s.append(targetkey2array[i3] + ";");
                        editkeyid3s.append(targetkey3array[i3] + ";");
                        editparamlistids.append(paramlistid + ";");
                        editparamlistversionids.append(paramlistversionid + ";");
                        editvariantids.append(variantid + ";");
                        editdatasets.append(dataset + ";");
                        editparamids.append(sourceDataItem.getParamID() + ";");
                        editparamtypes.append(sourceDataItem.getParamType() + ";");
                        replicateids.append(sourceDataItem.getReplicate() + ";");
                        enteredtexts.append(enteredText + ";");
                    }
                    for (int x = 0; x < mapkeyarray.size(); ++x) {
                        numreplicate.append(numReplicateMap.get(mapkeyarray.get(x)) + ";");
                    }
                }
            }
            HashMap<String, String> actionprops = new HashMap<String, String>();
            if (addkeyid1s.length() > 0) {
                addkeyid1s.setLength(addkeyid1s.length() - 1);
                addkeyid2s.setLength(addkeyid2s.length() - 1);
                addkeyid3s.setLength(addkeyid3s.length() - 1);
                addparamlistids.setLength(addparamlistids.length() - 1);
                addparamlistversionids.setLength(addparamlistversionids.length() - 1);
                addvariantids.setLength(addvariantids.length() - 1);
                adddatasets.setLength(adddatasets.length() - 1);
                addparamids.setLength(addparamids.length() - 1);
                addparamtypes.setLength(addparamtypes.length() - 1);
                numreplicate.setLength(numreplicate.length() - 1);
                actionprops.put("sdcid", targetsdcid);
                actionprops.put("keyid1", addkeyid1s.toString());
                actionprops.put("keyid2", addkeyid2s.toString());
                actionprops.put("keyid3", addkeyid3s.toString());
                actionprops.put("paramlistid", addparamlistids.toString());
                actionprops.put("paramlistversionid", addparamlistversionids.toString());
                actionprops.put("variantid", addvariantids.toString());
                actionprops.put("dataset", adddatasets.toString());
                actionprops.put("paramid", addparamids.toString());
                actionprops.put("paramtype", addparamtypes.toString());
                actionprops.put("numreplicate", numreplicate.toString());
                actionprops.put("propsmatch", "Y");
                ap.processAction("ExtendDataSet", "1", actionprops);
            }
            if (editkeyid1s.length() > 0) {
                editkeyid1s.setLength(editkeyid1s.length() - 1);
                editkeyid2s.setLength(editkeyid2s.length() - 1);
                editkeyid3s.setLength(editkeyid3s.length() - 1);
                editparamlistids.setLength(editparamlistids.length() - 1);
                editparamlistversionids.setLength(editparamlistversionids.length() - 1);
                editvariantids.setLength(editvariantids.length() - 1);
                editdatasets.setLength(editdatasets.length() - 1);
                editparamids.setLength(editparamids.length() - 1);
                editparamtypes.setLength(editparamtypes.length() - 1);
                replicateids.setLength(replicateids.length() - 1);
                enteredtexts.setLength(enteredtexts.length() - 1);
                actionprops.clear();
                actionprops.put("sdcid", targetsdcid);
                actionprops.put("keyid1", editkeyid1s.toString());
                actionprops.put("keyid2", editkeyid2s.toString());
                actionprops.put("keyid3", editkeyid3s.toString());
                actionprops.put("paramlistid", editparamlistids.toString());
                actionprops.put("paramlistversionid", editparamlistversionids.toString());
                actionprops.put("variantid", editvariantids.toString());
                actionprops.put("dataset", editdatasets.toString());
                actionprops.put("paramid", editparamids.toString());
                actionprops.put("paramtype", editparamtypes.toString());
                actionprops.put("replicateid", replicateids.toString());
                actionprops.put("enteredtext", enteredtexts.toString());
                ap.processAction("EnterDataItem", "1", actionprops);
            }
        }
        catch (Exception ex) {
            return this.setError("OPAL-ERROR: Exception caught: " + ex.getMessage(), ex);
        }
        return rc;
    }

    private List getDataItemList(DataSet ds, SDIDataSet sdiDataset) {
        ArrayList<DataItem> dataItemList = new ArrayList<DataItem>();
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String released;
                DataItem dataItem = new DataItem();
                dataItem.setSDIDataSet(sdiDataset);
                dataItem.setParamID(ds.getValue(i, "PARAMID"));
                dataItem.setParamType(ds.getValue(i, "PARAMTYPE"));
                dataItem.setReplicate(ds.getValue(i, "REPLICATEID"));
                dataItem.setEnteredValue(ds.getValue(i, "ENTEREDVALUE"));
                dataItem.setEnteredText(ds.getValue(i, "ENTEREDTEXT"));
                BigDecimal bigDecimalTrValue = ds.getBigDecimal(i, "TRANSFORMVALUE");
                if (bigDecimalTrValue != null) {
                    dataItem.setTransformValue(bigDecimalTrValue.toString());
                }
                dataItem.setDisplayFormat(ds.getValue(i, "DISPLAYFORMAT"));
                dataItem.setUserSequence(ds.getValue(i, "USERSEQUENCE"));
                String mandatory = ds.getValue(i, "MANDATORYFLAG");
                if (mandatory != null && mandatory.equals("Y")) {
                    dataItem.setMandatory(true);
                }
                if ((released = ds.getValue(i, "RELEASEDFLAG")) != null && released.equals("Y")) {
                    dataItem.setReleased(true);
                }
                dataItemList.add(dataItem);
            }
        }
        return dataItemList;
    }
}

