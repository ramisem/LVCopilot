/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.sapphire.actions.sdi.EditSDI;
import com.labvantage.sapphire.admin.ddt.RequestManagementUtil;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_RequestItemDetail
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 102348 $";
    public static final String SDCID = "LV_RequestItemDetail";
    public static final String TABLEID = "s_requestitemdetail";
    public static final String COLUMN_KEYID1 = "s_requestitemdetailid";
    public static final String COLUMN_REQUESTITEMID = "requestitemid";
    public static final String COLUMN_REQUESTID = "requestid";
    public static final String COLUMN_EXTERNALID = "externalid";
    public static final String COLUMN_LINKSDCID = "linksdcid";
    public static final String COLUMN_LINKKEYID1 = "linkkeyid1";
    public static final String COLUMN_LINKKEYID2 = "linkkeyid2";
    public static final String COLUMN_LINKKEYID3 = "linkkeyid3";
    public static final String COLUMN_REQUESTITEMDETAILSTATUS = "requestitemdetailstatus";
    public static final String COLUMN_SCANFLAG = "scanflag";

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        this.setRequestItemDetailStatus(RequestManagementUtil.RequestItemDetailStatus.PENDING, primary);
        this.resolveRequestId(primary);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void resolveRequestId(DataSet primary) throws SapphireException {
        try {
            StringBuffer sql = new StringBuffer("SELECT ").append(COLUMN_REQUESTID);
            sql.append(" FROM ").append("s_requestitem");
            sql.append(" WHERE ").append("s_requestitemid").append(" = ?");
            PreparedStatement requestItemStatement = this.database.prepareStatement("requestitem", sql.toString());
            HashMap<String, String> requestMap = new HashMap<String, String>();
            for (int i = 0; i < primary.getRowCount(); ++i) {
                DataSet requestItemDS;
                String enteredRequestId = primary.getValue(i, COLUMN_REQUESTID, "");
                String requestItemId = primary.getValue(i, COLUMN_REQUESTITEMID, "");
                if (enteredRequestId.length() != 0 || requestItemId.length() <= 0) continue;
                if (requestItemId.contains(";")) {
                    requestItemId = requestItemId.split(";")[0];
                }
                requestItemStatement.setString(1, requestItemId);
                String requestId = (String)requestMap.get(requestItemId);
                if ((requestId == null || requestId.length() == 0) && (requestItemDS = new DataSet(requestItemStatement.executeQuery())).getRowCount() > 0) {
                    requestId = requestItemDS.getValue(0, COLUMN_REQUESTID, "");
                    requestMap.put(requestItemId, requestId);
                }
                if (requestId.length() <= 0) continue;
                primary.setString(i, COLUMN_REQUESTID, requestId);
            }
        }
        catch (SQLException e) {
            this.logger.error("Failed to retrieve requestitems", e.toString());
        }
        finally {
            this.database.closeStatement("requestitem");
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        StringBuffer requestItemIds = new StringBuffer("");
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String requestItemId = primary.getValue(i, COLUMN_REQUESTITEMID, "");
            if (requestItemIds.indexOf(requestItemId) != -1) continue;
            requestItemIds.append(";").append(requestItemId);
        }
        this.updateItemCountForRequestItem(requestItemIds.substring(1));
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String requestItemDetailId = actionProps.getProperty("keyid1");
        StringBuffer requestItemIds = new StringBuffer("");
        if (StringUtil.getLen(requestItemDetailId) > 0L) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select requestitemid from s_requestitemdetail where s_requestitemdetailid in ( select r.keyid1 from rsetitems r where r.rsetid = ?)", (Object[])new String[]{rsetid});
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String requestItemId = ds.getString(i, COLUMN_REQUESTITEMID);
                if (requestItemIds.indexOf(requestItemId) != -1) continue;
                requestItemIds.append(";").append(requestItemId);
            }
            actionProps.setProperty(COLUMN_REQUESTITEMID, requestItemIds.substring(1));
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String requestItemIds = actionProps.getProperty(COLUMN_REQUESTITEMID, "");
        this.updateItemCountForRequestItem(requestItemIds);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateItemCountForRequestItem(String requestItemIds) throws SapphireException {
        try {
            String[] requestItemIdArr;
            for (String requestItemId : requestItemIdArr = StringUtil.split(requestItemIds, ";")) {
                StringBuffer sql = new StringBuffer("SELECT ").append("itemcount");
                sql.append(" FROM ").append("s_requestitem");
                sql.append(" WHERE ").append("s_requestitemid").append(" =? ");
                PreparedStatement requestItemStatement = this.database.prepareStatement("requestitem", sql.toString());
                requestItemStatement.setString(1, requestItemId);
                DataSet requestItemDS = new DataSet(requestItemStatement.executeQuery());
                int itemCount = 0;
                if (requestItemDS.getRowCount() > 0) {
                    itemCount = requestItemDS.getInt(0, "itemcount");
                }
                StringBuffer requestItemDetailCountSql = new StringBuffer(" SELECT COUNT(*) ");
                requestItemDetailCountSql.append(" FROM ").append(TABLEID);
                requestItemDetailCountSql.append(" WHERE ");
                requestItemDetailCountSql.append(COLUMN_REQUESTITEMID).append(" = '").append(requestItemId).append("'");
                int requestItemDetailCount = this.database.getCount(requestItemDetailCountSql.toString());
                if (itemCount <= 0 || itemCount == requestItemDetailCount) continue;
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_RequestItem");
                props.setProperty("keyid1", requestItemId);
                props.setProperty("itemcount", Integer.toString(requestItemDetailCount));
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
        catch (SQLException e) {
            this.logger.error("Failed to retrieve requestitems", e.toString());
        }
        finally {
            this.database.closeStatement("requestitem");
        }
    }

    private void setRequestItemSDIDetailsToSample(DataSet primary, String requestid, String nextRequestStatus) throws SapphireException {
        StringBuffer sampleIdBuffer = new StringBuffer();
        StringBuffer requestItemIdBuffer = new StringBuffer();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!primary.getValue(i, COLUMN_LINKSDCID, "Sample").equals("Sample")) continue;
            String sampleId = primary.getValue(i, COLUMN_LINKKEYID1, "");
            String requestItemId = primary.getValue(i, COLUMN_REQUESTITEMID, this.getOldPrimaryValue(primary, i, COLUMN_REQUESTITEMID));
            sampleIdBuffer.append(";").append(sampleId);
            requestItemIdBuffer.append(";").append(requestItemId);
        }
        if (sampleIdBuffer.length() > 0) {
            boolean isAsyncCopySDIDetailsToRequest = this.getConfigurationProcessor().getPolicy("BatchSamplePolicy", "Sapphire Custom").getProperty("asynccopysdidetailstorequest", "Y").equals("Y");
            ActionBlock ab = new ActionBlock();
            PropertyList props = new PropertyList();
            props.setProperty("sourcesdcid", "LV_RequestItem");
            props.setProperty("sourcekeyid1", requestItemIdBuffer.substring(1));
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", sampleIdBuffer.substring(1));
            props.setProperty("copyspec", "Y");
            props.setProperty("copyworkitem", "Y");
            props.setProperty("usecurrentversion", "Y");
            props.setProperty("applysourceworkitem", "Y");
            props.setProperty("specapplyflag", "Y");
            ab.setAction("CopySDIDetails", "CopySDIDetail", "1", props);
            if (nextRequestStatus != null && nextRequestStatus.length() > 0) {
                PropertyList requestProp = new PropertyList();
                requestProp.setProperty("sdcid", "Request");
                requestProp.setProperty("keyid1", requestid);
                if (RequestManagementUtil.isAllSamplesReceived(requestid, this.getQueryProcessor())) {
                    nextRequestStatus = RequestManagementUtil.RequestStatus.RECEIVED.getStatusValue();
                    requestProp.setProperty("receiveddt", "NOW");
                }
                requestProp.setProperty("requeststatus", nextRequestStatus);
                if (nextRequestStatus.equalsIgnoreCase(RequestManagementUtil.RequestStatus.ACCEPTED.getStatusValue())) {
                    requestProp.setProperty("accepteddt", "NOW");
                }
                if (isAsyncCopySDIDetailsToRequest && !this.connectionInfo.getSysuserId().equals("(system)")) {
                    ab.setAction("EditSDI", "EditSDI", "1", requestProp);
                }
            }
            if (isAsyncCopySDIDetailsToRequest && !this.connectionInfo.getSysuserId().equals("(system)")) {
                AutomationService as = new AutomationService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                try {
                    as.addToDoListEntry("", ab, "", true, this.connectionInfo.getSysuserId(), "");
                }
                catch (ServiceException e) {
                    throw new ActionException("Unable to send processing to ToDoList");
                }
            } else {
                this.getActionProcessor().processActionBlock(ab);
            }
        }
    }

    private void setSampleFields(DataSet primary) throws ActionException {
        StringBuffer sampleIdBuffer = new StringBuffer();
        StringBuffer requestItemDetailIdBuffer = new StringBuffer();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (!primary.getValue(i, COLUMN_LINKSDCID, "Sample").equals("Sample")) continue;
            String sampleId = primary.getValue(i, COLUMN_LINKKEYID1, "");
            String requestItemDetailId = primary.getValue(i, COLUMN_KEYID1, "");
            if (sampleId.length() <= 0 || !this.hasPrimaryValueChanged(primary, i, COLUMN_LINKKEYID1)) continue;
            sampleIdBuffer.append(";").append(sampleId);
            requestItemDetailIdBuffer.append(";").append(requestItemDetailId);
        }
        if (sampleIdBuffer.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Sample");
            props.setProperty("keyid1", sampleIdBuffer.substring(1));
            props.setProperty("requestitemdetailid", requestItemDetailIdBuffer.substring(1));
            ActionBlock actionBlock = new ActionBlock();
            actionBlock.setAction("Sample EditSDI", "EditSDI", "1", props);
            this.getActionProcessor().processActionBlock(actionBlock);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String operation = actionProps.getProperty("operation", "");
        if (operation.length() > 0) {
            this.handleOperationsPreEdit(primary, operation, actionProps);
            String scannedDetails = actionProps.getProperty("scanneddetails");
            if (scannedDetails.length() > 0) {
                this.setScannedFlag(primary, scannedDetails);
            }
        }
    }

    private void setScannedFlag(DataSet primary, String scannedDetails) {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String requestDetailId = primary.getValue(i, COLUMN_KEYID1, "");
            if (scannedDetails.indexOf(requestDetailId) < 0) continue;
            primary.setString(i, COLUMN_SCANFLAG, "Y");
        }
    }

    private void handleOperationsPreEdit(DataSet primary, String operation, PropertyList actionProps) throws SapphireException {
        if (operation.equalsIgnoreCase("cancel") || operation.equalsIgnoreCase("cancelonly")) {
            this.setRequestItemDetailStatus(RequestManagementUtil.RequestItemDetailStatus.CANCELLED, primary);
        }
        if (operation.equalsIgnoreCase("disposedetail") || operation.equalsIgnoreCase("shipdetail")) {
            this.setRequestItemDetailStatus(RequestManagementUtil.RequestItemDetailStatus.DONE, primary);
        }
        if (operation.contains("setsamplefk")) {
            this.setSampleFields(primary);
        }
        if (operation.contains("copysdidetails")) {
            this.setRequestItemSDIDetailsToSample(primary, actionProps.getProperty("operation_requestid"), actionProps.getProperty("nextrequeststatus"));
        }
    }

    private void setRequestItemDetailStatus(RequestManagementUtil.RequestItemDetailStatus status, DataSet primary) {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            primary.setString(i, COLUMN_REQUESTITEMDETAILSTATUS, status.getStatusValue());
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String operation = actionProps.getProperty("operation", "");
        if (operation.length() > 0) {
            this.handleOperationsPostEdit(primary, operation);
        }
    }

    private void handleOperationsPostEdit(DataSet primary, String operation) throws SapphireException {
        HashSet<String> uniqueRequestItems = new HashSet<String>();
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String requestItemId;
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_REQUESTITEMDETAILSTATUS) || (requestItemId = primary.getValue(i, COLUMN_REQUESTITEMID, this.getOldPrimaryValue(primary, i, COLUMN_REQUESTITEMID))) == null || requestItemId.length() <= 0) continue;
            uniqueRequestItems.add(requestItemId);
        }
        if (uniqueRequestItems.size() > 0) {
            StringBuffer requestItemIdToEdit = new StringBuffer();
            if (operation.equalsIgnoreCase("cancel")) {
                String sql = "SELECT requestitemdetailstatus FROM s_requestitemdetail WHERE requestitemid = ? AND requestitemdetailstatus != '" + RequestManagementUtil.RequestItemDetailStatus.CANCELLED.getStatusValue() + "'";
                this.checkSisterDetailStatuses(uniqueRequestItems, requestItemIdToEdit, sql);
                if (requestItemIdToEdit.length() > 0) {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "LV_RequestItem");
                    props.setProperty("keyid1", requestItemIdToEdit.substring(1));
                    props.setProperty("operation", "cancel");
                    props.setProperty("activity", "detail_cancel");
                    this.getActionProcessor().processAction("EditSDI", "1", props);
                }
            }
            if (operation.equals("disposedetail") || uniqueRequestItems.size() > 0) {
                SafeSQL safeSQL = new SafeSQL();
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_requestitem.s_requestitemid, s_requestitem.quantity, s_requestitem.requestitemstatus, (select count(s_requestitemdetail.s_requestitemdetailid) from s_requestitemdetail where s_requestitemdetail.requestitemid = s_requestitem.s_requestitemid and requestitemdetailstatus = 'Pending') pendingcount, (select count(s_requestitemdetail.s_requestitemdetailid) from s_requestitemdetail where s_requestitemdetail.requestitemid = s_requestitem.s_requestitemid and requestitemdetailstatus = 'Done') donecount, (select count(s_requestitemdetail.s_requestitemdetailid) from s_requestitemdetail where s_requestitemdetail.requestitemid = s_requestitem.s_requestitemid and requestitemdetailstatus = 'Cancelled') cancelledcount from s_requestitem, reagenttype where reagenttype.reagenttypeid = s_requestitem.reagenttypeid and reagenttype.reagenttypeversionid = s_requestitem.reagenttypeversionid and s_requestitemid in (" + safeSQL.addIn(uniqueRequestItems) + ") and reagenttype.contentflag = 'K'", safeSQL.getValues());
                if (ds != null && ds.size() > 0) {
                    DataSet editDS = new DataSet();
                    for (int i = 0; i < ds.size(); ++i) {
                        int row;
                        String s_requestitemid = ds.getString(i, "s_requestitemid");
                        String requestitemstatus = ds.getString(i, "requestitemstatus");
                        int quantity = ds.getInt(i, "quantity", 0);
                        int donecount = ds.getInt(i, "donecount", 0) + ds.getInt(i, "cancelledcount", 0);
                        if (quantity <= 0 || donecount <= 0) continue;
                        if (donecount < quantity && !RequestManagementUtil.RequestItemStatus.INPROGRESS.getStatusValue().equals(requestitemstatus)) {
                            row = editDS.addRow();
                            editDS.setString(row, "keyid1", s_requestitemid);
                            editDS.setString(row, "requestitemstatus", RequestManagementUtil.RequestItemStatus.INPROGRESS.getStatusValue());
                            continue;
                        }
                        if (donecount < quantity || RequestManagementUtil.RequestItemStatus.DONE.getStatusValue().equals(requestitemstatus)) continue;
                        row = editDS.addRow();
                        editDS.setString(row, "keyid1", s_requestitemid);
                        editDS.setString(row, "requestitemstatus", RequestManagementUtil.RequestItemStatus.DONE.getStatusValue());
                    }
                    if (editDS.size() > 0) {
                        PropertyList props = new PropertyList();
                        props.setProperty("sdcid", "LV_RequestItem");
                        props.setProperty("keyid1", editDS.getColumnValues("keyid1", ";"));
                        props.setProperty("requestitemstatus", editDS.getColumnValues("requestitemstatus", ";"));
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                } else {
                    Object props;
                    String sql = "SELECT requestitemdetailstatus FROM s_requestitemdetail WHERE requestitemid = ? AND requestitemdetailstatus NOT IN ( '" + RequestManagementUtil.RequestItemDetailStatus.CANCELLED.getStatusValue() + "','" + RequestManagementUtil.RequestItemDetailStatus.DONE.getStatusValue() + "')";
                    this.checkSisterDetailStatuses(uniqueRequestItems, requestItemIdToEdit, sql);
                    if (requestItemIdToEdit.length() > 0) {
                        props = new PropertyList();
                        ((PropertyList)props).setProperty("sdcid", "LV_RequestItem");
                        ((PropertyList)props).setProperty("keyid1", requestItemIdToEdit.substring(1));
                        ((PropertyList)props).setProperty("operation", "done");
                        ((PropertyList)props).setProperty("activity", "detail_done");
                        this.getActionProcessor().processAction("EditSDI", "1", (HashMap)props);
                        String[] requestItemIdToEditArr = requestItemIdToEdit.substring(1).split(";");
                        for (int i = 0; i < requestItemIdToEditArr.length; ++i) {
                            uniqueRequestItems.remove(requestItemIdToEditArr[i]);
                        }
                    }
                    if (uniqueRequestItems.size() > 0) {
                        requestItemIdToEdit = new StringBuffer();
                        for (String requestItemId : uniqueRequestItems) {
                            requestItemIdToEdit.append(";").append(requestItemId);
                        }
                        props = new PropertyList();
                        ((PropertyList)props).setProperty("sdcid", "LV_RequestItem");
                        ((PropertyList)props).setProperty("keyid1", requestItemIdToEdit.substring(1));
                        ((PropertyList)props).setProperty("operation", "inprogress");
                        ((PropertyList)props).setProperty("activity", "detail_inprogress");
                        this.getActionProcessor().processAction("EditSDI", "1", (HashMap)props);
                    }
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void checkSisterDetailStatuses(Set<String> uniqueRequestItems, StringBuffer requestItemIdToEdit, String sql) throws SapphireException {
        PreparedStatement selectSisterDetailsStatement = this.database.prepareStatement("sisterdetails", sql);
        try {
            Iterator<String> iter = uniqueRequestItems.iterator();
            while (iter.hasNext()) {
                String requestItemId = iter.next();
                selectSisterDetailsStatement.setString(1, requestItemId);
                DataSet sisterDetails = new DataSet(selectSisterDetailsStatement.executeQuery());
                if (sisterDetails.getRowCount() != 0) continue;
                requestItemIdToEdit.append(";").append(requestItemId);
                iter.remove();
            }
        }
        catch (SQLException e) {
            this.logger.error("Failed to retrieve request-item-details", e.toString());
        }
        finally {
            this.database.closeStatement("sisterdetails");
        }
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }
}

