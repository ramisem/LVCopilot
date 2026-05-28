/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.sdi;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.SdcInfo;
import com.labvantage.sapphire.actions.sdidata.BaseSDIDataAction;
import com.labvantage.sapphire.services.DDTService;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class UndoSDIColumnValue
extends BaseAction
implements sapphire.action.UndoSDIColumnValue {
    static final String LABVANTAGE_CVS_ID = "$Revision: 103730 $";
    private QueryProcessor __QueryProcessor;
    private String __tableid = null;
    private String[] __keyCols = null;
    private boolean __isDataset = false;
    private boolean __isWorkItem = false;
    private boolean __isSDI = false;
    boolean propsMatch = false;
    boolean rollupStatus = false;
    TranslationProcessor __tp = null;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        SafeSQL safeSQL;
        StringBuffer sqlValidateStatus;
        this.__QueryProcessor = this.getQueryProcessor();
        this.__tp = this.getTranslationProcessor();
        ActionProcessor ap = this.getActionProcessor();
        String sdcid = properties.getProperty("sdcid", "");
        String keyid1 = properties.getProperty("keyid1", "");
        String keyid2 = properties.getProperty("keyid2", "");
        String keyid3 = properties.getProperty("keyid3", "");
        String paramlistid = properties.getProperty("paramlistid", "");
        String paramlistversionid = properties.getProperty("paramlistversionid", "");
        String variantid = properties.getProperty("variantid", "");
        String dataset = properties.getProperty("dataset", "");
        String statuscolumn = properties.getProperty("statuscolumn", "");
        String validatestatuscolumnvalue = properties.getProperty("validatestatuscolumnvalue", "");
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] keyid2Arr = StringUtil.split(keyid2, ";");
        String[] keyid3Arr = StringUtil.split(keyid3, ";");
        String[] paramlistidArr = StringUtil.split(paramlistid, ";");
        String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
        String[] variantidArr = StringUtil.split(variantid, ";");
        String[] datasetArr = StringUtil.split(dataset, ";");
        String workItemId = properties.getProperty("workitemid");
        String workItemInstance = properties.getProperty("workiteminstance");
        String[] workItemIdArr = StringUtil.split(workItemId, ";");
        String[] workItemInstanceArr = StringUtil.split(workItemInstance, ";");
        this.propsMatch = StringUtil.getYN(properties.getProperty("propsmatch"), "N").equals("Y");
        this.rollupStatus = StringUtil.getYN(properties.getProperty("rollupstatus"), "Y").equals("Y");
        if (paramlistid.length() > 0) {
            this.__tableid = "sdidata";
            this.__isDataset = true;
            this.__isWorkItem = false;
            this.__isSDI = false;
        } else if (workItemId.length() > 0) {
            this.__tableid = "sdiworkitem";
            this.__isWorkItem = true;
            this.__isDataset = false;
            this.__isSDI = false;
        } else {
            this.__tableid = SdcInfo.getTableId(sdcid, this.__QueryProcessor);
            this.__isSDI = true;
            this.__isDataset = false;
            this.__isWorkItem = false;
        }
        this.__keyCols = DDTService.getKeyColumns(this.database, this.__tableid);
        if (sdcid.length() == 0 || keyid1.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.__tp.translate("SDC Id/ KeyId1 not specified."));
        }
        if (this.__keyCols.length > 1 && (keyid2.length() == 0 || keyid2Arr.length != keyid1Arr.length)) {
            throw new SapphireException("INVALID_PROPERTY", this.__tp.translate("KeyId2 not specified for all rows."));
        }
        if (this.__keyCols.length < 2) {
            keyid2 = null;
        }
        if (this.__keyCols.length > 2 && (keyid3.length() == 0 || keyid3Arr.length != keyid1Arr.length)) {
            throw new SapphireException("INVALID_PROPERTY", this.__tp.translate("KeyId3 not specified for all rows."));
        }
        if (this.__keyCols.length < 3) {
            keyid3 = null;
        }
        if (statuscolumn.length() == 0 || validatestatuscolumnvalue.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", this.__tp.translate("Status column and/or status column value to be validated for the action to execute is not specified."));
        }
        if ((paramlistid.length() > 0 || paramlistversionid.length() > 0 || variantid.length() > 0 || dataset.length() > 0) && paramlistidArr.length != paramlistversionidArr.length && paramlistidArr.length != variantidArr.length && paramlistidArr.length != datasetArr.length) {
            throw new SapphireException("INVALID_PROPERTY", this.__tp.translate("Count of Paramlistid not matching with either paramlistversionid, variantid or dataset."));
        }
        if ((workItemId.length() > 0 || workItemInstance.length() > 0) && workItemIdArr.length != workItemInstanceArr.length) {
            throw new SapphireException("INVALID_PROPERTY", this.__tp.translate("Count of Workitem Id not matching with count of Workitem instance specified."));
        }
        DAMProcessor dam = this.getDAMProcessor();
        String rsetId = BaseSDIDataAction.createBypassSecurityRSet(sdcid, keyid1, keyid2, keyid3, this.database, this.connectionInfo, false);
        HashMap<String, Object> filter = new HashMap<String, Object>();
        DataSet sdiWI = new DataSet();
        PropertyList policy = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom");
        boolean cancelIncomplete = false;
        boolean cancel = false;
        if (policy != null) {
            String cancelOption = policy.getProperty("cancelactionbehavior", "");
            if (cancelOption.equalsIgnoreCase("Cancel all Children")) {
                cancel = true;
            } else if (cancelOption.equalsIgnoreCase("Cancel Incomplete Children")) {
                cancelIncomplete = true;
                cancel = true;
            }
        }
        if (this.__isWorkItem) {
            sqlValidateStatus = new StringBuffer();
            safeSQL = new SafeSQL();
            sqlValidateStatus.append("SELECT * FROM " + this.__tableid + " t, rsetitems rs ").append(" WHERE t.sdcid = rs.sdcid and t.keyid1 = rs.keyid1 and t.keyid2 = rs.keyid2 ").append(" and t.keyid3 = rs.keyid3 and rs.rsetid = " + safeSQL.addVar(rsetId) + " ");
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sqlValidateStatus.toString(), safeSQL.getValues());
            DataSet swiParentGrp = new DataSet();
            for (int i = 0; i < keyid1Arr.length; ++i) {
                String keyId1 = keyid1Arr[i];
                String keyId2 = this.__keyCols.length > 1 ? keyid2Arr[i] : "(null)";
                String keyId3 = this.__keyCols.length > 2 ? keyid3Arr[i] : "(null)";
                for (int wi = 0; wi < workItemIdArr.length; ++wi) {
                    if (this.propsMatch) {
                        wi = i;
                    }
                    String wItemId = workItemIdArr[wi];
                    String wItemInstance = workItemInstanceArr[wi];
                    filter.clear();
                    filter.put("sdcid", sdcid);
                    filter.put("keyid1", keyId1);
                    filter.put("keyid2", keyId2);
                    filter.put("keyid3", keyId3);
                    filter.put("workitemid", wItemId);
                    filter.put("workiteminstance", new BigDecimal(wItemInstance));
                    int findRow = ds.findRow(filter);
                    String status = ds.getValue(findRow, statuscolumn);
                    String workItemTypeFlag = ds.getValue(findRow, "workitemtypeflag");
                    String groupId = ds.getValue(findRow, "groupid");
                    String groupInstance = ds.getValue(findRow, "groupinstance");
                    if (!status.equalsIgnoreCase(validatestatuscolumnvalue)) {
                        HashMap<String, String> valueMap = new HashMap<String, String>();
                        valueMap.put("validatestatuscolumnvalue", this.__tp.translate(validatestatuscolumnvalue));
                        throw new SapphireException("INVALID_SDI", this.__tp.translate("One or more selected SDI(s) are not in [validatestatuscolumnvalue] status. Operation cannot proceed.", valueMap));
                    }
                    int r = sdiWI.addRow();
                    sdiWI.setString(r, "sdcid", sdcid);
                    sdiWI.setString(r, "keyid1", keyId1);
                    sdiWI.setString(r, "keyId2", keyId2);
                    sdiWI.setString(r, "keyId3", keyId3);
                    sdiWI.setString(r, "workitemid", wItemId);
                    sdiWI.setString(r, "workiteminstance", wItemInstance);
                    sdiWI.setString(r, "workitemtypeflag", workItemTypeFlag);
                    sdiWI.setString(r, "groupid", groupId);
                    sdiWI.setString(r, "groupinstance", groupInstance);
                    if ("Cancelled".equals(validatestatuscolumnvalue) && cancel && "P".equals(workItemTypeFlag)) {
                        swiParentGrp.copyRow(sdiWI, r, 1);
                    }
                    if (!this.propsMatch) continue;
                    wi = workItemIdArr.length;
                }
            }
            int rowCnt = sdiWI.getRowCount();
            for (int i = 0; i < swiParentGrp.getRowCount(); ++i) {
                filter.clear();
                filter.put("sdcid", swiParentGrp.getValue(i, "sdcid"));
                filter.put("keyid1", swiParentGrp.getValue(i, "keyid1"));
                filter.put("keyid2", swiParentGrp.getValue(i, "keyid2"));
                filter.put("keyid3", swiParentGrp.getValue(i, "keyid3"));
                filter.put("groupid", swiParentGrp.getValue(i, "groupid"));
                filter.put("groupinstance", new BigDecimal(swiParentGrp.getValue(i, "groupinstance")));
                DataSet members = ds.getFilteredDataSet(filter);
                for (int k = 0; k < members.getRowCount(); ++k) {
                    if ("P".equals(members.getValue(k, "workitemtypeflag")) || !"Cancelled".equals(members.getValue(k, "workitemstatus"))) continue;
                    filter.clear();
                    filter.put("sdcid", members.getValue(k, "sdcid"));
                    filter.put("keyid1", members.getValue(k, "keyid1"));
                    filter.put("keyid2", members.getValue(k, "keyid2"));
                    filter.put("keyid3", members.getValue(k, "keyid3"));
                    filter.put("workitemid", members.getValue(k, "workitemid"));
                    filter.put("workiteminstance", members.getValue(k, "workiteminstance"));
                    int findSWI = sdiWI.findRow(filter);
                    if (findSWI >= 0) continue;
                    int r = sdiWI.addRow();
                    sdiWI.setString(r, "sdcid", members.getValue(k, "sdcid"));
                    sdiWI.setString(r, "keyid1", members.getValue(k, "keyid1"));
                    sdiWI.setString(r, "keyId2", members.getValue(k, "keyid2"));
                    sdiWI.setString(r, "keyId3", members.getValue(k, "keyid3"));
                    sdiWI.setString(r, "workitemid", members.getValue(k, "workitemid"));
                    sdiWI.setString(r, "workiteminstance", members.getValue(k, "workiteminstance"));
                    sdiWI.setString(r, "workitemtypeflag", members.getValue(k, "workitemtypeflag"));
                    sdiWI.setString(r, "groupid", members.getValue(k, "groupid"));
                    sdiWI.setString(r, "groupinstance", members.getValue(k, "groupinstance"));
                }
            }
            if (sdiWI.getRowCount() > rowCnt) {
                PropertyList callUndoAgain = new PropertyList();
                callUndoAgain.setProperty("sdcid", properties.getProperty("sdcid"));
                callUndoAgain.setProperty("keyid1", sdiWI.getColumnValues("keyid1", ";"));
                callUndoAgain.setProperty("keyid2", sdiWI.getColumnValues("keyid2", ";"));
                callUndoAgain.setProperty("keyid3", sdiWI.getColumnValues("keyid3", ";"));
                callUndoAgain.setProperty("workitemid", sdiWI.getColumnValues("workitemid", ";"));
                callUndoAgain.setProperty("workiteminstance", sdiWI.getColumnValues("workiteminstance", ";"));
                callUndoAgain.setProperty("validatestatuscolumnvalue", "Cancelled");
                callUndoAgain.setProperty("statuscolumn", "workitemstatus");
                callUndoAgain.setProperty("propsmatch", "Y");
                callUndoAgain.setProperty("auditactivity", properties.getProperty("auditactivity"));
                callUndoAgain.setProperty("auditreason", properties.getProperty("auditreason"));
                callUndoAgain.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                this.processAction(callUndoAgain);
                return;
            }
        } else if (this.__isDataset) {
            sqlValidateStatus = new StringBuffer();
            safeSQL = new SafeSQL();
            sqlValidateStatus.append("SELECT * FROM " + this.__tableid + " t, rsetitems rs ").append(" WHERE t.sdcid = rs.sdcid and t.keyid1 = rs.keyid1 and t.keyid2 = rs.keyid2 ").append(" and t.keyid3 = rs.keyid3 and rs.rsetid = " + safeSQL.addVar(rsetId) + " and t." + statuscolumn + "!=" + safeSQL.addVar(validatestatuscolumnvalue));
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sqlValidateStatus.toString(), safeSQL.getValues());
            for (int i = 0; i < keyid1Arr.length; ++i) {
                String keyId1 = keyid1Arr[i];
                String keyId2 = this.__keyCols.length > 1 ? keyid2Arr[i] : "(null)";
                String keyId3 = this.__keyCols.length > 2 ? keyid3Arr[i] : "(null)";
                for (int pl = 0; pl < paramlistidArr.length; ++pl) {
                    if (this.propsMatch) {
                        pl = i;
                    }
                    String paramListId = paramlistidArr[pl];
                    String paramListVersionId = paramlistversionidArr[pl];
                    String variantId = variantidArr[pl];
                    String datasetId = datasetArr[pl];
                    filter.clear();
                    filter.put("sdcid", sdcid);
                    filter.put("keyid1", keyId1);
                    filter.put("keyid2", keyId2);
                    filter.put("keyid3", keyId3);
                    filter.put("paramlistid", paramListId);
                    filter.put("paramlistversionid", paramListVersionId);
                    filter.put("variantid", variantId);
                    filter.put("dataset", new BigDecimal(datasetId));
                    int findRow = ds.findRow(filter);
                    if (findRow > -1) {
                        HashMap<String, String> valueMap = new HashMap<String, String>();
                        valueMap.put("validatestatuscolumnvalue", this.__tp.translate(validatestatuscolumnvalue));
                        throw new SapphireException("INVALID_SDI", this.__tp.translate("One or more selected SDI(s) are not in [validatestatuscolumnvalue] status. Operation cannot proceed.", valueMap));
                    }
                    if (!this.propsMatch) continue;
                    pl = paramlistidArr.length;
                }
            }
        } else if (this.__isSDI) {
            sqlValidateStatus = new StringBuffer();
            safeSQL = new SafeSQL();
            sqlValidateStatus.append("SELECT count(1) FROM " + this.__tableid + " t, rsetitems rs ").append(" WHERE t." + this.__keyCols[0] + " = rs.keyid1 ").append(this.__keyCols.length > 1 ? " and t." + this.__keyCols[1] + " = rs.keyid2 " : "").append(this.__keyCols.length > 2 ? " and t." + this.__keyCols[2] + " = rs.keyid3 " : "").append(" and rs.rsetid = " + safeSQL.addVar(rsetId) + " and t." + statuscolumn + "!=" + safeSQL.addVar(validatestatuscolumnvalue));
            int invalidSDICnt = this.__QueryProcessor.getPreparedCount(sqlValidateStatus.toString(), safeSQL.getValues());
            if (invalidSDICnt > 0) {
                HashMap<String, String> valueMap = new HashMap<String, String>();
                valueMap.put("validatestatuscolumnvalue", this.__tp.translate(validatestatuscolumnvalue));
                throw new SapphireException("INVALID_SDI", this.__tp.translate("One or more selected SDI(s) are not in [validatestatuscolumnvalue] status. Operation cannot proceed.", valueMap));
            }
        }
        try {
            Map<String, String> prevStatusMap = this.getPreviousStatus(properties, rsetId);
            String prevStatus = prevStatusMap.get("previousStatus");
            properties.remove("statuscolumn");
            properties.remove("validatestatuscolumnvalue");
            properties.remove("defaultvalue");
            properties.setProperty(statuscolumn, prevStatus);
            if ("Disposed".equalsIgnoreCase(validatestatuscolumnvalue)) {
                Map<String, String> prevDisposalStatusMap;
                String disposalStatus = properties.getProperty("disposalstatus", "");
                String prevDisposalStatus = null;
                if ((disposalStatus.length() == 0 || disposalStatus.equalsIgnoreCase("(null)")) && (prevDisposalStatusMap = this.getPreviousDisposalStatus(properties, rsetId)).size() > 0) {
                    prevDisposalStatus = prevDisposalStatusMap.get("previousDisposalStatus");
                }
                if (prevDisposalStatus != null && prevDisposalStatus.length() > 0) {
                    properties.setProperty("disposalstatus", prevDisposalStatus);
                }
            }
            if (this.__isDataset) {
                if ("Cancelled".equals(validatestatuscolumnvalue)) {
                    properties.setProperty("cancelleddt", "");
                    properties.setProperty("cancelledby", "");
                }
                ap.processAction("EditDataSet", "1", properties);
                if ("Cancelled".equals(validatestatuscolumnvalue)) {
                    properties.remove("cancelleddt");
                    properties.remove("cancelledby");
                }
                ap.processAction("UpdateDatasetStatus", "1", properties);
                if (this.rollupStatus) {
                    ap.processAction("SyncSDIWIStatus", "1", properties);
                }
            } else if (this.__isWorkItem) {
                boolean previousRollUp = false;
                if (this.rollupStatus) {
                    previousRollUp = true;
                }
                if ("Cancelled".equals(validatestatuscolumnvalue) && cancel) {
                    this.uncancelDatasets(properties, sdiWI, rsetId);
                }
                ap.processAction("EditSDIWorkItem", "1", properties);
                if (this.rollupStatus || previousRollUp) {
                    this.syncSDIWorkItemGroupStatus(properties, sdiWI);
                }
            } else if (this.__isSDI) {
                String[] keyid1Array = StringUtil.split(keyid1, ";");
                String[] prevStatusArr = StringUtil.split(prevStatus, ";");
                StringBuffer editTI = new StringBuffer();
                if (sdcid.equals("Sample")) {
                    for (int i = 0; i < keyid1Array.length; ++i) {
                        DataSet ds;
                        if ("Initial".equals(prevStatusArr[i]) && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct t.trackitemid from trackitem t  where t.linksdcid = 'Sample' and t.linkkeyid1 = ? and t.currentstorageunitid is not null", (Object[])new String[]{keyid1Array[i]})).getRowCount() > 0) {
                            editTI.append(";").append(ds.getValue(0, "trackitemid"));
                        }
                        if (!"samplestatus".equals(statuscolumn)) continue;
                        properties.setProperty("storagestatus", prevStatusMap.get("previousStorageStatus"));
                    }
                }
                properties.setProperty("modtool", "UndoSDIColumnValue");
                ap.processAction("EditSDI", "1", properties);
                if (editTI.length() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("trackitemid", editTI.substring(1));
                    props.setProperty("currentstorageunitid", "");
                    props.setProperty("auditreason", properties.getProperty("auditreason"));
                    props.setProperty("auditactivity", properties.getProperty("auditactivity"));
                    props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                    this.getActionProcessor().processAction("EditTrackItem", "1", props);
                }
            }
        }
        catch (SapphireException se) {
            throw new SapphireException("ACTION_BLOCK_FAILED", this.__tp.translate("Exception caught:") + " " + this.__tp.translate(ErrorUtil.extractMessageFromException(se, ErrorUtil.isUserAdmin(this.getConnectionId()))));
        }
        dam.clearRSet(rsetId);
    }

    private Map<String, String> getPreviousDisposalStatus(PropertyList properties, String rsetId) throws SapphireException {
        boolean isAuditTableExist;
        HashMap<String, String> previousDisposalStatusMap = new HashMap<String, String>();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        String keyid1 = properties.getProperty("keyid1", "");
        String keyid2 = properties.getProperty("keyid2", "");
        String keyid3 = properties.getProperty("keyid3", "");
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] keyid2Arr = StringUtil.split(keyid2, ";");
        String[] keyid3Arr = StringUtil.split(keyid3, ";");
        String sdcid = properties.getProperty("sdcid", "");
        boolean bl = isAuditTableExist = !sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N");
        if (isAuditTableExist && sdcid.equals("Sample") && this.__tableid.equals(SdcInfo.getTableId(sdcid, this.__QueryProcessor))) {
            SafeSQL safeSQL = new SafeSQL();
            HashMap<String, String> filter = new HashMap<String, String>();
            StringBuffer previousDisposalStatuses = new StringBuffer("");
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT * FROM A_" + this.__tableid + " t, rsetitems rs").append(" WHERE  t." + this.__keyCols[0] + " = rs.keyid1 ").append(this.__keyCols.length > 1 ? " and t." + this.__keyCols[1] + " = rs.keyid2 " : "").append(this.__keyCols.length > 2 ? " and t." + this.__keyCols[2] + " = rs.keyid3 " : "").append(" and rs.rsetid = " + safeSQL.addVar(rsetId)).append(" AND t.disposalstatus  not in ('Disposed', 'Marked for Disposal')").append(" ORDER BY t.auditsequence desc ");
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.getRowCount() > 0) {
                for (int i = 0; i < keyid1Arr.length; ++i) {
                    int findRow;
                    String keyId1 = keyid1Arr[i];
                    String keyId2 = this.__keyCols.length > 1 ? keyid2Arr[i] : "(null)";
                    String keyId3 = this.__keyCols.length > 2 ? keyid3Arr[i] : "(null)";
                    filter.clear();
                    filter.put("sdcid", sdcid);
                    filter.put("keyid1", keyId1);
                    if (this.__keyCols.length > 1) {
                        filter.put("keyid2", keyId2);
                    }
                    if (this.__keyCols.length > 2) {
                        filter.put("keyid3", keyId3);
                    }
                    if ((findRow = ds.findRow(filter)) <= -1) continue;
                    previousDisposalStatuses.append(";").append(ds.getValue(findRow, "disposalstatus", ""));
                }
                if (previousDisposalStatuses.toString().trim().length() > 0) {
                    previousDisposalStatusMap.put("previousDisposalStatus", previousDisposalStatuses.substring(1));
                }
            }
        }
        return previousDisposalStatusMap;
    }

    private Map<String, String> getPreviousStatus(PropertyList properties, String rsetId) throws SapphireException {
        boolean isAuditTableExist;
        HashMap<String, String> statusMap = new HashMap<String, String>();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        StringBuffer prevStatus = new StringBuffer();
        String keyid1 = properties.getProperty("keyid1", "");
        String keyid2 = properties.getProperty("keyid2", "");
        String keyid3 = properties.getProperty("keyid3", "");
        String paramlistid = properties.getProperty("paramlistid", "");
        String paramlistversionid = properties.getProperty("paramlistversionid", "");
        String variantid = properties.getProperty("variantid", "");
        String dataset = properties.getProperty("dataset", "");
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] keyid2Arr = StringUtil.split(keyid2, ";");
        String[] keyid3Arr = StringUtil.split(keyid3, ";");
        String[] paramlistidArr = StringUtil.split(paramlistid, ";");
        String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
        String[] variantidArr = StringUtil.split(variantid, ";");
        String[] datasetArr = StringUtil.split(dataset, ";");
        String statuscolumn = properties.getProperty("statuscolumn", "");
        String validatestatuscolumnvalue = properties.getProperty("validatestatuscolumnvalue", "");
        String defaultvalue = properties.getProperty("defaultvalue", "");
        String workItemId = properties.getProperty("workitemid");
        String workItemInstance = properties.getProperty("workiteminstance");
        String[] workItemIdArr = StringUtil.split(workItemId, ";");
        String[] workItemInstanceArr = StringUtil.split(workItemInstance, ";");
        String sdcid = properties.getProperty("sdcid", "");
        boolean bl = isAuditTableExist = !sdcProcessor.getProperty(sdcid, "auditedflag").equalsIgnoreCase("N");
        if (isAuditTableExist) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            StringBuffer sqlAll = new StringBuffer();
            DataSet dsAll = new DataSet();
            if (this.__isSDI) {
                sqlAll.append("SELECT * FROM A_" + this.__tableid + " t, rsetitems rs").append(" WHERE  t." + this.__keyCols[0] + " = rs.keyid1 ").append(this.__keyCols.length > 1 ? " and t." + this.__keyCols[1] + " = rs.keyid2 " : "").append(this.__keyCols.length > 2 ? " and t." + this.__keyCols[2] + " = rs.keyid3 " : "").append(" and rs.rsetid = " + safeSQL.addVar(rsetId)).append(" ORDER BY t.auditsequence desc ");
                dsAll = this.__QueryProcessor.getPreparedSqlDataSet(sqlAll.toString(), safeSQL.getValues());
                String auditSequences = this.getUndoStatusAuditSequences(dsAll);
                String[] auditSequencesArr = auditSequences.split(",");
                HashSet<String> auditSequencesSet = new HashSet<String>(Arrays.asList(auditSequencesArr));
                auditSequences = String.join((CharSequence)",", auditSequencesSet);
                safeSQL.reset();
                sql.append("SELECT * FROM A_" + this.__tableid + " t, rsetitems rs").append(" WHERE  t." + this.__keyCols[0] + " = rs.keyid1 ").append(this.__keyCols.length > 1 ? " and t." + this.__keyCols[1] + " = rs.keyid2 " : "").append(this.__keyCols.length > 2 ? " and t." + this.__keyCols[2] + " = rs.keyid3 " : "").append(" and rs.rsetid = " + safeSQL.addVar(rsetId)).append(" AND t." + statuscolumn + " NOT IN(" + safeSQL.addIn(validatestatuscolumnvalue + ";" + "Pending AutoApprEval", ";")).append(") ").append(auditSequences.length() > 0 ? " AND auditsequence NOT IN(" + safeSQL.addIn(auditSequences, ",") + " )" : "").append(" ORDER BY t.auditsequence desc ");
            } else {
                sql.append("SELECT * FROM A_" + this.__tableid + " t, rsetitems rs").append(" WHERE t.sdcid = rs.sdcid and t.keyid1 = rs.keyid1 and t.keyid2 = rs.keyid2 and t.keyid3 = rs.keyid3 and rs.rsetid = " + safeSQL.addVar(rsetId)).append(" AND t." + statuscolumn + "!=" + safeSQL.addVar(validatestatuscolumnvalue) + " ORDER BY t.auditsequence desc ");
            }
            DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            HashMap<String, Object> filter = new HashMap<String, Object>();
            if (this.__isDataset) {
                for (int i = 0; i < keyid1Arr.length; ++i) {
                    String keyId1 = keyid1Arr[i];
                    String keyId2 = this.__keyCols.length > 1 ? keyid2Arr[i] : "(null)";
                    String keyId3 = this.__keyCols.length > 2 ? keyid3Arr[i] : "(null)";
                    for (int pl = 0; pl < paramlistidArr.length; ++pl) {
                        if (this.propsMatch) {
                            pl = i;
                        }
                        String paramListId = paramlistidArr[pl];
                        String paramListVersionId = paramlistversionidArr[pl];
                        String variantId = variantidArr[pl];
                        String datasetId = datasetArr[pl];
                        filter.clear();
                        filter.put("sdcid", sdcid);
                        filter.put("keyid1", keyId1);
                        filter.put("keyid2", keyId2);
                        filter.put("keyid3", keyId3);
                        filter.put("paramlistid", paramListId);
                        filter.put("paramlistversionid", paramListVersionId);
                        filter.put("variantid", variantId);
                        filter.put("dataset", new BigDecimal(datasetId));
                        int findRow = ds.findRow(filter);
                        if (findRow > -1) {
                            prevStatus.append(";" + ds.getValue(findRow, statuscolumn));
                        } else {
                            if (defaultvalue.equalsIgnoreCase("")) {
                                throw new SapphireException("INVALID_SDI", this.__tp.translate("For one or more selected SDI(s) previous status not found in audit table. Also default value not specified in action input."));
                            }
                            prevStatus.append(";" + defaultvalue);
                        }
                        if (!this.propsMatch) continue;
                        pl = paramlistidArr.length;
                    }
                }
            } else if (this.__isWorkItem) {
                for (int i = 0; i < keyid1Arr.length; ++i) {
                    String keyId1 = keyid1Arr[i];
                    String keyId2 = this.__keyCols.length > 1 ? keyid2Arr[i] : "(null)";
                    String keyId3 = this.__keyCols.length > 2 ? keyid3Arr[i] : "(null)";
                    for (int wi = 0; wi < workItemIdArr.length; ++wi) {
                        if (this.propsMatch) {
                            wi = i;
                        }
                        String wItemId = workItemIdArr[wi];
                        String wItemInstance = workItemInstanceArr[wi];
                        filter.clear();
                        filter.put("sdcid", sdcid);
                        filter.put("keyid1", keyId1);
                        filter.put("keyid2", keyId2);
                        filter.put("keyid3", keyId3);
                        filter.put("workitemid", wItemId);
                        filter.put("workiteminstance", new BigDecimal(wItemInstance));
                        int findRow = ds.findRow(filter);
                        if (findRow > -1) {
                            prevStatus.append(";" + ds.getValue(findRow, statuscolumn));
                        } else {
                            if (defaultvalue.equalsIgnoreCase("")) {
                                throw new SapphireException("INVALID_SDI", this.__tp.translate("For one or more selected SDI(s) previous status not found in audit table. Also default value not specified in action input."));
                            }
                            prevStatus.append(";" + defaultvalue);
                        }
                        if (!this.propsMatch) continue;
                        wi = workItemIdArr.length;
                    }
                }
            } else if (this.__isSDI) {
                StringBuilder previousstoragestatus = new StringBuilder();
                for (int i = 0; i < keyid1Arr.length; ++i) {
                    int findRow;
                    String keyId1 = keyid1Arr[i];
                    String keyId2 = this.__keyCols.length > 1 ? keyid2Arr[i] : "(null)";
                    String keyId3 = this.__keyCols.length > 2 ? keyid3Arr[i] : "(null)";
                    filter.clear();
                    filter.put("sdcid", sdcid);
                    filter.put("keyid1", keyId1);
                    if (this.__keyCols.length > 1) {
                        filter.put("keyid2", keyId2);
                    }
                    if (this.__keyCols.length > 2) {
                        filter.put("keyid3", keyId3);
                    }
                    if ((findRow = ds.findRow(filter)) > -1) {
                        prevStatus.append(";" + ds.getValue(findRow, statuscolumn));
                        if (!"Sample".equals(sdcid)) continue;
                        previousstoragestatus.append(";").append(ds.getValue(findRow, "storagestatus"));
                        continue;
                    }
                    if (defaultvalue.equalsIgnoreCase("")) {
                        throw new SapphireException("INVALID_SDI", this.__tp.translate("For one or more selected SDI(s) previous status not found in audit table. Also default value not specified in action input."));
                    }
                    prevStatus.append(";" + defaultvalue);
                    if (!"Sample".equals(sdcid)) continue;
                    previousstoragestatus.append(";").append(ds.getValue(findRow, "storagestatus"));
                }
                if ("Sample".equals(sdcid)) {
                    statusMap.put("previousStorageStatus", previousstoragestatus.substring(1));
                }
            }
        } else {
            if (defaultvalue.equalsIgnoreCase("")) {
                throw new SapphireException("INVALID_SDI", this.__tp.translate("Cannot proceed. Audit table is not found and default value is not specified in action input."));
            }
            prevStatus.append(";" + defaultvalue);
        }
        statusMap.put("previousStatus", prevStatus.substring(1));
        return statusMap;
    }

    private String getUndoStatusAuditSequences(DataSet dsAll) {
        StringBuffer auditSequences = new StringBuffer();
        for (int i = 0; i < dsAll.getRowCount(); ++i) {
            int nextRow;
            if (!"UndoSDIColumnValue".equals(dsAll.getValue(i, "modtool")) || (nextRow = i + 1) >= dsAll.getRowCount()) continue;
            auditSequences.append(",").append(dsAll.getValue(nextRow, "auditsequence"));
        }
        if (auditSequences.length() > 0) {
            return auditSequences.substring(1);
        }
        return "";
    }

    private void uncancelDatasets(PropertyList properties, DataSet sdiWI, String rsetId) throws SapphireException {
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT ds.sdcid, ds.keyid1, ds.keyid2, ds.keyid3, ds.paramlistid, ds.paramlistversionid, ds.variantid, ds.dataset, ds.sourceworkitemid, ds.sourceworkiteminstance ").append(" FROM sdidata ds, sdiworkitemitem swii, sdiworkitem swi, rsetitems rs ").append("  WHERE  ds.sdcid = swii.sdcid").append(" and ds.keyid1 = swii.keyid1").append(" and ds.keyid2 = swii.keyid2").append(" and ds.keyid3 = swii.keyid3").append(" and ds.paramlistid = swii.itemkeyid1").append(" and ds.paramlistversionid = swii.itemkeyid2").append(" and ds.variantid = swii.itemkeyid3").append(" and ds.dataset = swii.iteminstance").append(" and ds.sourceworkitemid = swii.workitemid").append(" and ds.sourceworkiteminstance = swii.workiteminstance").append(" and swii.sdcid = swi.sdcid and swii.keyid1 = swi.keyid1 and swii.keyid2 = swi.keyid2 and swii.keyid3 = swi.keyid3").append(" and swii.workitemid = swi.workitemid and swii.workiteminstance = swi.workiteminstance ").append(" and swi.sdcid = rs.sdcid and swi.keyid1 = rs.keyid1 and swi.keyid2 = rs.keyid2 ").append(" and swi.keyid3 = rs.keyid3 and rs.rsetid=" + safeSQL.addVar(rsetId) + " and ds.s_datasetstatus = 'Cancelled'");
        DataSet ds = this.__QueryProcessor.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds.getRowCount() > 0) {
            HashMap<String, Object> filter = new HashMap<String, Object>();
            DataSet undoDatasets = new DataSet();
            for (int i = 0; i < sdiWI.getRowCount(); ++i) {
                filter.clear();
                filter.put("sdcid", sdiWI.getValue(i, "sdcid"));
                filter.put("keyid1", sdiWI.getValue(i, "keyid1"));
                filter.put("keyid2", sdiWI.getValue(i, "keyid2"));
                filter.put("keyid3", sdiWI.getValue(i, "keyid3"));
                filter.put("sourceworkitemid", sdiWI.getValue(i, "workitemid"));
                filter.put("sourceworkiteminstance", new BigDecimal(sdiWI.getValue(i, "workiteminstance")));
                DataSet cancelledDataSets = ds.getFilteredDataSet(filter);
                undoDatasets.copyRow(cancelledDataSets, -1, 1);
            }
            if (undoDatasets.getRowCount() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", properties.getProperty("sdcid"));
                props.setProperty("keyid1", undoDatasets.getColumnValues("keyid1", ";"));
                props.setProperty("keyid2", undoDatasets.getColumnValues("keyid2", ";"));
                props.setProperty("keyid3", undoDatasets.getColumnValues("keyid3", ";"));
                props.setProperty("paramlistid", undoDatasets.getColumnValues("paramlistid", ";"));
                props.setProperty("paramlistversionid", undoDatasets.getColumnValues("paramlistversionid", ";"));
                props.setProperty("variantid", undoDatasets.getColumnValues("variantid", ";"));
                props.setProperty("dataset", undoDatasets.getColumnValues("dataset", ";"));
                props.setProperty("validatestatuscolumnvalue", "Cancelled");
                props.setProperty("statuscolumn", "s_datasetstatus");
                props.setProperty("propsmatch", "Y");
                props.setProperty("rollupstatus", "N");
                props.setProperty("auditactivity", properties.getProperty("auditactivity"));
                props.setProperty("auditreason", properties.getProperty("auditreason"));
                props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                this.processAction(props);
            }
        }
    }

    private void syncSDIWorkItemGroupStatus(PropertyList properties, DataSet sdiWI) throws SapphireException {
        DataSet swiWithGrp = new DataSet();
        for (int i = 0; i < sdiWI.getRowCount(); ++i) {
            if (sdiWI.getValue(i, "groupid").length() <= 0) continue;
            swiWithGrp.copyRow(sdiWI, i, 1);
        }
        if (swiWithGrp.getRowCount() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", properties.getProperty("sdcid"));
            props.setProperty("keyid1", swiWithGrp.getColumnValues("keyid1", ";"));
            props.setProperty("keyid2", swiWithGrp.getColumnValues("keyid2", ";"));
            props.setProperty("keyid3", swiWithGrp.getColumnValues("keyid3", ";"));
            props.setProperty("workitemid", swiWithGrp.getColumnValues("workitemid", ";"));
            props.setProperty("workiteminstance", swiWithGrp.getColumnValues("workiteminstance", ";"));
            props.setProperty("groupid", swiWithGrp.getColumnValues("groupid", ";"));
            props.setProperty("groupinstance", swiWithGrp.getColumnValues("groupinstance", ";"));
            props.setProperty("syncsdiworkitemgroupstatusonly", "Y");
            props.setProperty("auditactivity", properties.getProperty("auditactivity"));
            props.setProperty("auditreason", properties.getProperty("auditreason"));
            props.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
            this.getActionProcessor().processAction("SyncSDIWIStatus", "1", props);
        }
    }
}

