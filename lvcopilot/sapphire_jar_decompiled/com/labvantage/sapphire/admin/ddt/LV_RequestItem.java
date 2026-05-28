/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.admin.ddt.RequestManagementUtil;
import com.labvantage.sapphire.pageelements.simplespec.action.GetNewSpecId;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseSDCRules;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LV_RequestItem
extends BaseSDCRules {
    static final String LABVANTAGE_CVS_ID = "$Revision: 84852 $";
    public static final String SDCID = "LV_RequestItem";
    public static final String TABLEID = "s_requestitem";
    public static final String COLUMN_KEYID1 = "s_requestitemid";
    public static final String COLUMN_REQUESTID = "requestid";
    public static final String COLUMN_REQUESTITEMSTATUS = "requestitemstatus";
    public static final String COLUMN_PRODUCTID = "productid";
    public static final String COLUMN_PRODUCTVERSIONID = "productversionid";
    public static final String COLUMN_ITEMCOUNT = "itemcount";
    public static final String COLUMN_APPLIEDFLAG = "appliedflag";
    public static final String COLUMN_TEMPLATESDCID = "templatesdcid";
    public static final String COLUMN_TEMPLATEKEYID1 = "templatekeyid1";
    public static final String COLUMN_TEMPLATEKEYID2 = "templatekeyid2";
    public static final String COLUMN_TEMPLATEKEYID3 = "templatekeyid3";
    public static final String COLUMN_SHIPPINGLOCATIONDEPARTMENTID = "shippinglocationdepartmentid";
    public static final String COLUMN_CONTACTADDRESSID = "contactaddressid";
    public static final String COLUMN_CONTACTADDRESSTYPE = "contactaddresstype";
    public static final String DEFAULT_TEMPLATESDCID = "Sample";
    public static final String COLUMN_EMBEDDEDSPECID = "embeddedspecid";
    public static final String COLUMN_EMBEDDEDSPECVERSIONID = "embeddedspecversionid";
    private Map<String, Map<String, String>> requestContactMap = new HashMap<String, Map<String, String>>();

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String templateKeyId1 = actionProps.getProperty(COLUMN_TEMPLATEKEYID1, "");
        if (!actionProps.getProperty("templateflag").equalsIgnoreCase("Y")) {
            this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.PENDING, primary);
        }
        if (templateKeyId1.length() > 0) {
            this.copyEmbeddedSpec(templateKeyId1, sdiData);
        }
        this.setContactAddressType(sdiData);
        this.copyShippingInfoFromRequest(primary);
        for (int i = 0; i < primary.size(); ++i) {
            String reagenttypeversionid;
            String reagenttypeid = primary.getString(i, "reagenttypeid", "");
            if (reagenttypeid.length() <= 0 || (reagenttypeversionid = primary.getString(i, "reagenttypeversionid", "").trim()).length() != 0) continue;
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select reagenttypeid, reagenttypeversionid, versionstatus from reagenttype where reagenttypeid = ?", (Object[])new String[]{reagenttypeid});
            if (OpalUtil.isNotEmpty(ds)) {
                int row = ds.findRow("versionstatus", "C");
                if (row != -1) {
                    reagenttypeversionid = ds.getValue(row, "reagenttypeversionid", "1");
                } else {
                    int version = 1;
                    for (int j = 0; j < ds.size(); ++j) {
                        int reagenttypeversion;
                        String versionstatus = ds.getString(j, "versionstatus", "");
                        try {
                            reagenttypeversion = Integer.parseInt(ds.getString(j, "reagenttypeversionid", "1"));
                        }
                        catch (NumberFormatException e) {
                            reagenttypeversion = 1;
                        }
                        if (!"P".equals(versionstatus) || version >= reagenttypeversion) continue;
                        version = reagenttypeversion;
                    }
                    reagenttypeversionid = String.valueOf(version);
                }
                primary.setString(i, "reagenttypeversionid", reagenttypeversionid);
                continue;
            }
            throw new SapphireException("RequestItem", "VALIDATION", this.getTranslationProcessor().translate("Given Reagent Type not found") + ": " + reagenttypeid);
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        String saveMode = actionProps.getProperty("_savemode", "");
        String newkeyid1 = actionProps.getProperty("newkeyid1", "");
        String requestId = String.join((CharSequence)";", Stream.of(StringUtil.split(actionProps.getProperty(COLUMN_REQUESTID, ""), ";")).collect(Collectors.toSet()));
        if (saveMode.equalsIgnoreCase("full") && !newkeyid1.isEmpty() && !requestId.isEmpty()) {
            HashMap<String, String> props = new HashMap<String, String>();
            props.put("sdcid", "Request");
            props.put("keyid1", requestId);
            props.put("operation", "sendforacceptance");
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
    }

    private void copyEmbeddedSpec(String templateKeyId1, SDIData sdiData) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String embeddedSpecId = primary.getValue(i, COLUMN_EMBEDDEDSPECID, "");
            String embeddedSpecVersionId = primary.getValue(i, COLUMN_EMBEDDEDSPECVERSIONID, "");
            String requestItemId = primary.getValue(i, COLUMN_REQUESTID, "");
            if (embeddedSpecId.length() <= 0) continue;
            String newSpecId = null;
            PropertyList getNewSpecIdProps = new PropertyList();
            getNewSpecIdProps.setProperty("sdcid", SDCID);
            getNewSpecIdProps.setProperty("keyid1", requestItemId);
            this.getActionProcessor().processActionClass(GetNewSpecId.class.getName(), getNewSpecIdProps);
            newSpecId = getNewSpecIdProps.getProperty("newspecid");
            SDI newEmbeddedSpec = this.createNewEmbeddedSDI("SpecSDC", embeddedSpecId, embeddedSpecVersionId, null, newSpecId, "1", null, "embeddedflag");
            if (newEmbeddedSpec == null) continue;
            primary.setValue(i, COLUMN_EMBEDDEDSPECID, newEmbeddedSpec.getKeyid1());
            primary.setValue(i, COLUMN_EMBEDDEDSPECVERSIONID, newEmbeddedSpec.getKeyid2());
        }
    }

    private SDI createNewEmbeddedSDI(String sdcId, String embeddedTemplateKeyId1, String embeddedTemplateKeyId2, String embeddedTemplateKeyId3, String newKeyId1, String newKeyId2, String newKeyid3, String embeddedFlagColumnName) throws ActionException {
        PropertyList props = new PropertyList();
        props.setProperty(embeddedFlagColumnName, "Y");
        props.setProperty("sdcid", sdcId);
        props.setProperty(COLUMN_TEMPLATEKEYID1, embeddedTemplateKeyId1);
        props.setProperty("keyid1", newKeyId1);
        if (embeddedTemplateKeyId2 != null && embeddedTemplateKeyId2.length() > 0) {
            props.setProperty("keyid2", newKeyId2);
            props.setProperty(COLUMN_TEMPLATEKEYID2, embeddedTemplateKeyId2);
        }
        if (embeddedTemplateKeyId3 != null && embeddedTemplateKeyId3.length() > 0) {
            props.setProperty("keyid3", newKeyid3);
            props.setProperty(COLUMN_TEMPLATEKEYID3, embeddedTemplateKeyId3);
        }
        props.setProperty("copies", "1");
        this.getActionProcessor().processAction("AddSDI", "1", props);
        return new SDI(sdcId, props.getProperty("newkeyid1"), props.getProperty("newkeyid2"), null);
    }

    private void copyShippingInfoFromRequest(DataSet primary) {
        if (primary.size() > 0) {
            for (int i = 0; i < primary.size(); ++i) {
                if (primary.getString(i, COLUMN_CONTACTADDRESSID, "").length() != 0) continue;
                String requestid = primary.getString(i, COLUMN_REQUESTID);
                Map<String, String> map = this.getRequestContactInfo(requestid);
                primary.setString(i, COLUMN_SHIPPINGLOCATIONDEPARTMENTID, map.get(COLUMN_SHIPPINGLOCATIONDEPARTMENTID));
                primary.setString(i, COLUMN_CONTACTADDRESSID, map.get(COLUMN_CONTACTADDRESSID));
                primary.setString(i, COLUMN_CONTACTADDRESSTYPE, map.get(COLUMN_CONTACTADDRESSTYPE));
            }
        }
    }

    private Map<String, String> getRequestContactInfo(String requestid) {
        if (!this.requestContactMap.containsKey(requestid)) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select requestclass, sitedepartmentid, submitbydepartmentid, contactaddressid, contactaddresstype from s_request where s_requestid = ?", (Object[])new String[]{requestid});
            HashMap<String, String> map = new HashMap<String, String>();
            if (ds != null && ds.size() > 0) {
                String requestclass = ds.getString(0, "requestclass", "");
                if ("Kit".equals(requestclass)) {
                    map.put(COLUMN_SHIPPINGLOCATIONDEPARTMENTID, ds.getString(0, "submitbydepartmentid", ""));
                    map.put(COLUMN_CONTACTADDRESSID, ds.getString(0, COLUMN_CONTACTADDRESSID, ""));
                    map.put(COLUMN_CONTACTADDRESSTYPE, ds.getString(0, COLUMN_CONTACTADDRESSTYPE, ""));
                } else if ("Submission".equals(requestclass)) {
                    map.put(COLUMN_SHIPPINGLOCATIONDEPARTMENTID, ds.getString(0, "sitedepartmentid", ""));
                    map.put(COLUMN_CONTACTADDRESSID, ds.getString(0, COLUMN_CONTACTADDRESSID, ""));
                    map.put(COLUMN_CONTACTADDRESSTYPE, ds.getString(0, COLUMN_CONTACTADDRESSTYPE, ""));
                }
            } else {
                map.put(COLUMN_SHIPPINGLOCATIONDEPARTMENTID, "");
                map.put(COLUMN_CONTACTADDRESSID, "");
                map.put(COLUMN_CONTACTADDRESSTYPE, "");
            }
            this.requestContactMap.put(requestid, map);
        }
        return this.requestContactMap.get(requestid);
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String operation = actionProps.getProperty("operation", "");
        if (operation.equals("acceptdeny")) {
            String approvalFlag = actionProps.getProperty("approvalflag");
            if (approvalFlag.equals("U")) {
                this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.PENDING, primary);
            }
            if (approvalFlag.equals("F")) {
                this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.DENIED, primary);
            }
        } else if (operation.equals("denyonly")) {
            this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.DENIED, primary);
        } else if (operation.equalsIgnoreCase("inprogress")) {
            this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.INPROGRESS, primary);
        } else if (operation.equalsIgnoreCase("pending")) {
            this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.PENDING, primary);
        } else if (operation.equalsIgnoreCase("cancel")) {
            this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.CANCELLED, primary);
        } else if (operation.equalsIgnoreCase("cancelonly")) {
            this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.CANCELLED, primary);
        } else if (operation.equalsIgnoreCase("requestitemcancel")) {
            this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.CANCELLED, primary);
            this.cancelDetails(primary);
        } else if (operation.equalsIgnoreCase("done")) {
            this.setRequestItemStatus(RequestManagementUtil.RequestItemStatus.DONE, primary);
        } else {
            this.setContactAddressType(sdiData);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void cancelDetails(DataSet primary) throws SapphireException {
        StringBuffer requestDetailBuffer = new StringBuffer();
        String sql = "SELECT s_requestitemdetailid FROM s_requestitemdetail WHERE requestid = ?  AND requestitemid = ?  AND requestitemdetailstatus != '" + RequestManagementUtil.RequestItemDetailStatus.CANCELLED.getStatusValue() + "'";
        PreparedStatement requestItemDetailsStatement = this.database.prepareStatement("requestitemdetails", sql);
        try {
            for (int i = 0; i < primary.getRowCount(); ++i) {
                String requestItemId = primary.getValue(i, COLUMN_KEYID1, "");
                String requestId = primary.getValue(i, COLUMN_REQUESTID, this.getOldPrimaryValue(primary, i, COLUMN_REQUESTID));
                if (requestId.length() <= 0 || requestItemId.length() <= 0) continue;
                requestItemDetailsStatement.setString(1, requestId);
                requestItemDetailsStatement.setString(2, requestItemId);
                DataSet requestItemDetailsDs = new DataSet(requestItemDetailsStatement.executeQuery());
                if (requestItemDetailsDs.getRowCount() <= 0) continue;
                requestDetailBuffer.append(";").append(requestItemDetailsDs.getColumnValues("s_requestitemdetailid", ";"));
            }
            if (requestDetailBuffer.length() > 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_RequestItemDetail");
                props.setProperty("keyid1", requestDetailBuffer.substring(1));
                props.setProperty("operation", "cancelonly");
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
        catch (SQLException e) {
            this.logger.error(e.getMessage());
        }
        finally {
            this.database.closeStatement("requestitemdetails");
        }
    }

    private void setContactAddressType(SDIData sdiData) {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            if (primary.getValue(i, COLUMN_CONTACTADDRESSID, "").length() <= 0 || primary.getValue(i, COLUMN_CONTACTADDRESSTYPE, "").length() != 0) continue;
            primary.setString(i, COLUMN_CONTACTADDRESSTYPE, "Contact");
        }
    }

    private void setRequestItemStatus(RequestManagementUtil.RequestItemStatus status, DataSet primary) {
        for (int i = 0; i < primary.getRowCount(); ++i) {
            primary.setString(i, COLUMN_REQUESTITEMSTATUS, status.getStatusValue());
        }
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        String operation = actionProps.getProperty("operation", "");
        this.handleOperationsPostEdit(primary, operation);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleOperationsPostEdit(DataSet primary, String operation) throws SapphireException {
        HashSet<String> uniqueRequests = new HashSet<String>();
        String requestStatus = null;
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String requestId;
            if (!this.hasPrimaryValueChanged(primary, i, COLUMN_REQUESTITEMSTATUS) || (requestId = primary.getValue(i, COLUMN_REQUESTID, this.getOldPrimaryValue(primary, i, COLUMN_REQUESTID))) == null || requestId.length() <= 0) continue;
            uniqueRequests.add(requestId);
        }
        if (uniqueRequests.size() > 0) {
            PropertyList props;
            StringBuffer requestIdToOpen = new StringBuffer();
            StringBuffer requestIdToCancel = new StringBuffer();
            StringBuffer requestIdToCompleted = new StringBuffer();
            StringBuffer requestIdToDenied = new StringBuffer();
            String sql = "SELECT r1.requeststatus, r2.requestitemstatus FROM s_request r1, s_requestitem r2 WHERE r1.s_requestid = ? AND r1.s_requestid = r2.requestid";
            PreparedStatement selectSisterItemsStatement = this.database.prepareStatement("sisteritems", sql);
            try {
                for (String request : uniqueRequests) {
                    String[] requestArr = request.split(",");
                    selectSisterItemsStatement.setString(1, requestArr[0]);
                    DataSet sisterItems = new DataSet(selectSisterItemsStatement.executeQuery());
                    requestStatus = sisterItems.getValue(0, "requeststatus", "");
                    RequestManagementUtil.RequestStatus currentRequestStatus = RequestManagementUtil.RequestStatus.getRequestStatus(requestStatus);
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put(COLUMN_REQUESTITEMSTATUS, RequestManagementUtil.RequestItemStatus.CANCELLED.getStatusValue());
                    DataSet nonCancelledSisterRequestItems = sisterItems.getFilteredDataSet(filter, true);
                    filter.put(COLUMN_REQUESTITEMSTATUS, RequestManagementUtil.RequestItemStatus.DONE.getStatusValue());
                    DataSet doneSisterRequestItems = sisterItems.getFilteredDataSet(filter);
                    filter.put(COLUMN_REQUESTITEMSTATUS, RequestManagementUtil.RequestItemStatus.CANCELLED.getStatusValue());
                    DataSet cancelledSisterRequestItems = sisterItems.getFilteredDataSet(filter);
                    filter.put(COLUMN_REQUESTITEMSTATUS, RequestManagementUtil.RequestItemStatus.DENIED.getStatusValue());
                    DataSet deniedSisterRequestItems = sisterItems.getFilteredDataSet(filter);
                    filter.put(COLUMN_REQUESTITEMSTATUS, RequestManagementUtil.RequestItemStatus.INPROGRESS.getStatusValue());
                    DataSet inProgressSisterRequestItems = sisterItems.getFilteredDataSet(filter);
                    int requestItemCount = sisterItems.getRowCount();
                    int countOfDoneRequestItems = doneSisterRequestItems.getRowCount();
                    int countOfCancelledRequestItems = cancelledSisterRequestItems.getRowCount();
                    int countOfDeniedRequestItems = deniedSisterRequestItems.getRowCount();
                    int countOfFinishedRequestItems = countOfDoneRequestItems + countOfCancelledRequestItems + countOfDeniedRequestItems;
                    int countWorkStartedRequestItems = countOfFinishedRequestItems + inProgressSisterRequestItems.getRowCount();
                    if (operation.equalsIgnoreCase("cancel") && nonCancelledSisterRequestItems.getRowCount() == 0) {
                        requestIdToCancel.append(";").append(requestArr[0]);
                        continue;
                    }
                    if (countOfDeniedRequestItems == requestItemCount) {
                        if (operation.equals("denyonly")) continue;
                        requestIdToDenied.append(";").append(requestArr[0]);
                        continue;
                    }
                    if (countOfFinishedRequestItems == requestItemCount) {
                        if (operation.equalsIgnoreCase("cancelonly") || !currentRequestStatus.isAfter(RequestManagementUtil.RequestStatus.PENDINGACCEPTANCE) || !currentRequestStatus.isBefore(RequestManagementUtil.RequestStatus.COMPLETED)) continue;
                        requestIdToCompleted.append(";").append(requestArr[0]);
                        continue;
                    }
                    if (countWorkStartedRequestItems <= 0) continue;
                    requestIdToOpen.append(";").append(requestArr[0]);
                }
            }
            catch (SQLException e) {
                this.logger.error("Failed to retrieve request-item-details", e.toString());
            }
            finally {
                this.database.closeStatement("sisterdetails");
            }
            if (requestIdToOpen.length() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", "Request");
                props.setProperty("keyid1", requestIdToOpen.substring(1));
                props.setProperty("operation", RequestManagementUtil.RequestOperation.OPEN.getOperationValue());
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
            if (requestIdToCancel.length() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", "Request");
                props.setProperty("keyid1", requestIdToCancel.substring(1));
                props.setProperty("operation", RequestManagementUtil.RequestOperation.CANCEL.getOperationValue());
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
            if (requestIdToCompleted.length() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", "Request");
                props.setProperty("keyid1", requestIdToCompleted.substring(1));
                props.setProperty("operation", RequestManagementUtil.RequestOperation.COMPLETE.getOperationValue());
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
            if (requestIdToDenied.length() > 0) {
                props = new PropertyList();
                props.setProperty("sdcid", "Request");
                props.setProperty("keyid1", requestIdToDenied.substring(1));
                props.setProperty("operation", RequestManagementUtil.RequestOperation.ACCEPT.getOperationValue());
                props.setProperty("approvalevaluation", "F");
                props.setProperty("rolldownstatus", "N");
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
        }
    }

    @Override
    public void postAddWorkItem(SDIData sdiData, PropertyList actionProps) throws SapphireException {
    }

    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        String requestItemIds = actionProps.getProperty("keyid1", "");
        if (requestItemIds.length() > 0) {
            StringBuffer sql = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            sql.append("SELECT s_requestitemid");
            sql.append(", ").append(COLUMN_EMBEDDEDSPECID);
            sql.append(", ").append(COLUMN_EMBEDDEDSPECVERSIONID);
            sql.append(" FROM s_requestitem ");
            sql.append(" WHERE s_requestitemid in (").append(safeSQL.addIn(requestItemIds, ";")).append(")");
            StringBuffer embeddedSpecIdBuffer = new StringBuffer();
            StringBuffer embeddedSpecVersionIdBuffer = new StringBuffer();
            DataSet requestitemDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            for (int i = 0; i < requestitemDs.getRowCount(); ++i) {
                String embeddedSpecId = requestitemDs.getValue(i, COLUMN_EMBEDDEDSPECID, "");
                if (embeddedSpecId.length() <= 0) continue;
                String embeddedSpecVersionId = requestitemDs.getValue(i, COLUMN_EMBEDDEDSPECVERSIONID, "");
                embeddedSpecIdBuffer.append(";").append(embeddedSpecId);
                embeddedSpecVersionIdBuffer.append(";").append(embeddedSpecVersionId);
            }
            if (embeddedSpecIdBuffer.length() > 0) {
                actionProps.setProperty(COLUMN_EMBEDDEDSPECID, embeddedSpecIdBuffer.substring(1));
                actionProps.setProperty(COLUMN_EMBEDDEDSPECVERSIONID, embeddedSpecVersionIdBuffer.substring(1));
            }
        }
    }

    @Override
    public void postDelete(String rsetid, PropertyList actionProps) throws SapphireException {
        this.deleteEmbeddedSpec(actionProps);
    }

    private void deleteEmbeddedSpec(PropertyList actionProps) throws SapphireException {
        String embeddedSpecIds = actionProps.getProperty(COLUMN_EMBEDDEDSPECID, "");
        String embeddedSpecVersionId = actionProps.getProperty(COLUMN_EMBEDDEDSPECVERSIONID, "");
        if (embeddedSpecIds.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "SpecSDC");
            props.setProperty("keyid1", embeddedSpecIds);
            props.setProperty("keyid2", embeddedSpecVersionId);
            this.getActionProcessor().processAction("DeleteSDI", "1", props);
        }
    }
}

