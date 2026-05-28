/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RequestItemOperations
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 74930 $";
    public static final String OPERATION_SUBMISSION_ADDREQUESTDETAIL = "submission_addrequestdetail";
    public static final String OPERATION_SUBMISSION_ACCEPTDENY = "submission_acceptdeny";
    public static final String OPERATION_PULL_VALIDATETAKECUSTODY = "pull_validatetakecustody";
    public static final String OPERATION_PULL_TAKECUSTODY = "pull_takecustody";
    public static final String OPERATION_PULL_VALIDATEPACKAGING = "pull_validatepackaging";
    public static final String OPERATION_REQUESTITEMDETAIL_SETSCAN = "requestitemdetail_setscan";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String operation = ajaxResponse.getRequestParameter("operation", "");
        if (operation.equals(OPERATION_SUBMISSION_ADDREQUESTDETAIL)) {
            this.processSubmissionAddRequestDetail(ajaxResponse);
        } else if (operation.equals(OPERATION_PULL_VALIDATETAKECUSTODY)) {
            this.validateTakeCustody(ajaxResponse);
        } else if (operation.equals(OPERATION_PULL_TAKECUSTODY)) {
            this.takeCustody(ajaxResponse);
        } else if (operation.equals(OPERATION_PULL_VALIDATEPACKAGING)) {
            this.validatePackaging(ajaxResponse);
        } else if (operation.equals(OPERATION_REQUESTITEMDETAIL_SETSCAN)) {
            this.setScannedDetails(ajaxResponse);
        }
        ajaxResponse.print();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void takeCustody(AjaxResponse ajaxResponse) {
        String confirm = "N";
        String msg = "";
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid");
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        try {
            if (StringUtil.getLen(trackitemid) > 0L && StringUtil.getLen(departmentid) > 0L) {
                ErrorDetail errorDetail;
                ErrorHandler errorHandler;
                ActionProcessor ap = this.getActionProcessor();
                PropertyList props = new PropertyList();
                props.setProperty("trackitemid", trackitemid);
                props.setProperty("custodialuserid", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                props.setProperty("custodialdepartmentid", departmentid);
                props.setProperty("currentstorageunitid", "");
                props.setProperty("__sdcruleconfirm", ajaxResponse.getRequestParameter("__sdcruleconfirm", "N"));
                ap.processAction("EditTrackItem", "1", props);
                if (ap.hasInfoErrors() && (errorHandler = ap.getErrorHandler()) != null && errorHandler.hasInfoErrors() && (errorDetail = (ErrorDetail)errorHandler.get(0)) != null) {
                    msg = errorDetail.getMessage();
                }
                this.setScannedDetails(ajaxResponse);
                this.copyRequestItemWorkitems(ajaxResponse);
            }
        }
        catch (ActionException e) {
            ErrorDetail errorDetail;
            ErrorHandler errorHandler = e.getErrorHandler();
            if (errorHandler != null && errorHandler.hasErrors() && (errorDetail = (ErrorDetail)errorHandler.get(0)) != null) {
                if (errorDetail.getErrorType().equals("CONFIRM")) {
                    confirm = "Y";
                }
                msg = errorDetail.getMessage();
            }
        }
        finally {
            ajaxResponse.addCallbackArgument("msg", msg);
            ajaxResponse.addCallbackArgument("confirm", confirm);
        }
    }

    private void copyRequestItemWorkitems(AjaxResponse ajaxResponse) {
        String linkkeyid1 = ajaxResponse.getRequestParameter("linkkeyid1", "");
        String linksdcid = ajaxResponse.getRequestParameter("linksdcid", "");
        String s_requestitemdetailid = ajaxResponse.getRequestParameter("s_requestitemdetailid", "");
        linkkeyid1 = StringUtil.replaceAll(linkkeyid1, "%3B", ";");
        linksdcid = StringUtil.replaceAll(linksdcid, "%3B", ";");
        s_requestitemdetailid = StringUtil.replaceAll(s_requestitemdetailid, "%3B", ";");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_RequestItemDetail");
        props.setProperty("keyid1", s_requestitemdetailid);
        props.setProperty("linkkeyid1", linkkeyid1);
        props.setProperty("linksdcid", linksdcid);
        props.setProperty("keyid3", s_requestitemdetailid);
        props.setProperty("operation", "copysdidetails");
        try {
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
        catch (ActionException e) {
            this.logger.error("Failed to set scan", e.getMessage());
        }
    }

    private void setScannedDetails(AjaxResponse ajaxResponse) {
        String scannedDetails = ajaxResponse.getRequestParameter("scanneddetails");
        if (scannedDetails.length() > 0) {
            String s_requestitemdetailid = ajaxResponse.getRequestParameter("s_requestitemdetailid");
            s_requestitemdetailid = StringUtil.replaceAll(s_requestitemdetailid, "%3B", ";");
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_RequestItemDetail");
            props.setProperty("keyid1", s_requestitemdetailid);
            props.setProperty("scanneddetails", scannedDetails);
            props.setProperty("operation", "setscanflag");
            try {
                this.getActionProcessor().processAction("EditSDI", "1", props);
            }
            catch (ActionException e) {
                this.logger.error("Failed to set scan", e.getMessage());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void validatePackaging(AjaxResponse ajaxResponse) {
        String sampleRsetId = "";
        String trackItemRsetId = "";
        try {
            String linkSdcId = ajaxResponse.getRequestParameter("linksdcid", "Sample");
            StringBuffer noUserCustody = new StringBuffer();
            StringBuffer differentUserCustody = new StringBuffer();
            StringBuffer inPackage = new StringBuffer();
            String currUser = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            DataSet distinctTrackItemDs = null;
            if (linkSdcId.equals("Sample")) {
                String linkKeyid1 = ajaxResponse.getRequestParameter("linkkeyid1", "");
                sampleRsetId = this.getDAMProcessor().createRSet("Sample", linkKeyid1, null, null);
                DataSet trackItemDs = this.getTrackItemDataSet(sampleRsetId);
                String trackItemIds = trackItemDs.getColumnValues("trackitemid", ";");
                if (trackItemIds.length() > 0) {
                    trackItemRsetId = this.getDAMProcessor().createRSet("TrackItemSDC", trackItemIds, null, null);
                    distinctTrackItemDs = this.getDistinctTrackItemDataSet(trackItemRsetId);
                    for (int i = 0; i < distinctTrackItemDs.getRowCount(); ++i) {
                        String packageId;
                        String storageSDCId;
                        String trackItemCustodialUser = distinctTrackItemDs.getValue(i, "custodialuserid", "");
                        if (trackItemCustodialUser.length() > 0) {
                            if (!trackItemCustodialUser.equalsIgnoreCase(currUser)) {
                                differentUserCustody.append(", ");
                                differentUserCustody.append(distinctTrackItemDs.getValue(i, "linkkeyid1", ""));
                            }
                        } else {
                            noUserCustody.append(", ");
                            noUserCustody.append(distinctTrackItemDs.getValue(i, "linkkeyid1", ""));
                        }
                        if (!"LV_Package".equals(storageSDCId = distinctTrackItemDs.getValue(i, "storagesdcid", "")) || (packageId = distinctTrackItemDs.getValue(i, "storagekeyid1", "")).length() <= 0) continue;
                        inPackage.append(", ");
                        inPackage.append(distinctTrackItemDs.getValue(i, "linkkeyid1", ""));
                    }
                }
                if (noUserCustody.length() > 1) {
                    ajaxResponse.addCallbackArgument("noUserCustody", noUserCustody.substring(2));
                } else {
                    ajaxResponse.addCallbackArgument("noUserCustody", "");
                }
                if (differentUserCustody.length() > 1) {
                    ajaxResponse.addCallbackArgument("differentUserCustody", differentUserCustody.substring(2));
                } else {
                    ajaxResponse.addCallbackArgument("differentUserCustody", "");
                }
                if (inPackage.length() > 1) {
                    ajaxResponse.addCallbackArgument("inPackage", inPackage.substring(2));
                } else {
                    ajaxResponse.addCallbackArgument("inPackage", "");
                }
                if (distinctTrackItemDs != null && distinctTrackItemDs.getRowCount() > 0) {
                    ajaxResponse.addCallbackArgument("distinctTrackItemIds", distinctTrackItemDs.getColumnValues("trackitemid", ";"));
                } else {
                    ajaxResponse.addCallbackArgument("distinctTrackItemIds", "");
                }
                ajaxResponse.addCallbackArgument("userDefaultDepartment", this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment());
                String[] requestIdArr = ajaxResponse.getRequestParameter("requestid", "").split(";");
                String[] requestItemIdArr = ajaxResponse.getRequestParameter("requestitemid", "").split(";");
                if (requestItemIdArr.length > 0) {
                    requestIdArr = StringUtil.split(requestIdArr[0], "%3B");
                    requestItemIdArr = StringUtil.split(requestItemIdArr[0], "%3B");
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuffer sql = new StringBuffer();
                    sql.append("SELECT ").append("shippinglocationdepartmentid").append(", ").append("contactaddressid");
                    sql.append(" FROM ").append("s_requestitem");
                    sql.append(" WHERE ").append("requestid").append(" = ").append(safeSQL.addVar(requestIdArr[0]));
                    sql.append(" AND ").append("s_requestitemid").append(" = ").append(safeSQL.addVar(requestItemIdArr[0]));
                    DataSet requestItemDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (requestItemDs != null && requestItemDs.getRowCount() > 0) {
                        ajaxResponse.addCallbackArgument("defaultShippingDepartment", requestItemDs.getValue(0, "shippinglocationdepartmentid", ""));
                        ajaxResponse.addCallbackArgument("defaultShippingContact", requestItemDs.getValue(0, "contactaddressid", ""));
                    } else {
                        ajaxResponse.addCallbackArgument("defaultShippingDepartment", "");
                        ajaxResponse.addCallbackArgument("defaultShippingContact", "");
                    }
                }
            }
        }
        catch (SapphireException sapphireException) {
        }
        finally {
            if (sampleRsetId.length() > 0) {
                this.getDAMProcessor().clearRSet(trackItemRsetId);
                this.getDAMProcessor().clearRSet(sampleRsetId);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void validateTakeCustody(AjaxResponse ajaxResponse) {
        String sampleRsetId = "";
        String trackItemRsetId = "";
        try {
            String linkSdcId = ajaxResponse.getRequestParameter("linksdcid", "Sample");
            StringBuffer sameUserCustody = new StringBuffer();
            StringBuffer differentUserCustody = new StringBuffer();
            StringBuffer inShippedPackage = new StringBuffer();
            String currUser = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            DataSet distinctTrackItemDs = null;
            if (linkSdcId.equals("Sample")) {
                String linkKeyid1 = ajaxResponse.getRequestParameter("linkkeyid1", "");
                sampleRsetId = this.getDAMProcessor().createRSet("Sample", linkKeyid1, null, null);
                DataSet trackItemDs = this.getTrackItemDataSet(sampleRsetId);
                String trackItemIds = trackItemDs.getColumnValues("trackitemid", ";");
                if (trackItemIds.length() > 0) {
                    trackItemRsetId = this.getDAMProcessor().createRSet("TrackItemSDC", trackItemIds, null, null);
                    distinctTrackItemDs = this.getDistinctTrackItemDataSet(trackItemRsetId);
                    HashMap<String, String> packageMap = new HashMap<String, String>();
                    for (int i = 0; i < distinctTrackItemDs.getRowCount(); ++i) {
                        String storageSDCId;
                        String trackItemCustodialUser = distinctTrackItemDs.getValue(i, "custodialuserid", "");
                        if (trackItemCustodialUser.length() > 0) {
                            if (trackItemCustodialUser.equalsIgnoreCase(currUser)) {
                                sameUserCustody.append(", ");
                                sameUserCustody.append(distinctTrackItemDs.getValue(i, "linkkeyid1", ""));
                            } else {
                                differentUserCustody.append(", ");
                                differentUserCustody.append(distinctTrackItemDs.getValue(i, "linkkeyid1", ""));
                            }
                        }
                        if (!"LV_Package".equals(storageSDCId = distinctTrackItemDs.getValue(i, "storagesdcid", ""))) continue;
                        String packageId = distinctTrackItemDs.getValue(i, "storagekeyid1", "");
                        String packageStatus = (String)packageMap.get(packageId);
                        SafeSQL safeSQL = new SafeSQL();
                        if (packageStatus == null) {
                            StringBuffer sql = new StringBuffer();
                            sql.append("SELECT packagestatus FROM s_package WHERE s_packageid=").append(safeSQL.addVar(packageId));
                            DataSet packageDs = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                            packageStatus = packageDs.getRowCount() > 0 ? packageDs.getValue(0, "packagestatus", "") : "";
                            packageMap.put(packageId, packageStatus);
                        }
                        if (!packageStatus.equals("Shipped")) continue;
                        inShippedPackage.append(", ");
                        inShippedPackage.append(distinctTrackItemDs.getValue(i, "linkkeyid1", ""));
                    }
                }
            }
            boolean dataOk = true;
            if (sameUserCustody.length() > 1) {
                dataOk = false;
                ajaxResponse.addCallbackArgument("sameUserCustody", sameUserCustody.substring(2));
            } else {
                ajaxResponse.addCallbackArgument("sameUserCustody", "");
            }
            if (differentUserCustody.length() > 1) {
                dataOk = false;
                ajaxResponse.addCallbackArgument("differentUserCustody", differentUserCustody.substring(2));
            } else {
                ajaxResponse.addCallbackArgument("differentUserCustody", "");
            }
            if (inShippedPackage.length() > 1) {
                dataOk = false;
                ajaxResponse.addCallbackArgument("inShippedPackage", inShippedPackage.substring(2));
            } else {
                ajaxResponse.addCallbackArgument("inShippedPackage", "");
            }
            if (dataOk) {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT department.departmentid";
                sql = sql + " FROM departmentsysuser, department";
                sql = sql + " WHERE departmentsysuser.departmentid = department.departmentid";
                sql = sql + " AND department.externalflag != 'Y'";
                sql = sql + " AND departmentsysuser.sysuserid = " + safeSQL.addVar(currUser);
                sql = sql + " ORDER BY department.departmentid";
                DataSet departmentDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                if (departmentDs.getRowCount() > 0) {
                    ajaxResponse.addCallbackArgument("departmentIds", departmentDs.getColumnValues("departmentid", ";"));
                } else {
                    ajaxResponse.addCallbackArgument("departmentIds", "");
                }
            } else {
                ajaxResponse.addCallbackArgument("departmentIds", "");
            }
            if (distinctTrackItemDs != null && distinctTrackItemDs.getRowCount() > 0) {
                ajaxResponse.addCallbackArgument("distinctTrackItemIds", distinctTrackItemDs.getColumnValues("trackitemid", ";"));
            } else {
                ajaxResponse.addCallbackArgument("distinctTrackItemIds", "");
            }
        }
        catch (SapphireException sapphireException) {
        }
        finally {
            if (sampleRsetId.length() > 0) {
                this.getDAMProcessor().clearRSet(trackItemRsetId);
                this.getDAMProcessor().clearRSet(sampleRsetId);
            }
        }
    }

    private DataSet getTrackItemDataSet(String sampleRsetId) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT trackitemid FROM trackitem WHERE linksdcid = 'Sample' AND ");
        sql.append(" linkkeyid1 IN ( SELECT r.keyid1 FROM rsetitems r WHERE r.rsetid = ").append(safeSQL.addVar(sampleRsetId)).append(" )");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private DataSet getDistinctTrackItemDataSet(String trackItemRsetId) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT DISTINCT trackitemid, custodialuserid, custodialdepartmentid, linkkeyid1");
        sql.append(" ,(SELECT s.labelpath FROM storageunit s WHERE s.storageunitid = trackitem.currentstorageunitid) labelpath");
        sql.append(" ,(SELECT s.linksdcid FROM storageunit s WHERE s.storageunitid = trackitem.currentstorageunitid) storagesdcid");
        sql.append(" ,(SELECT s.linkkeyid1 FROM storageunit s WHERE s.storageunitid = trackitem.currentstorageunitid) storagekeyid1");
        sql.append(" FROM trackitem, rsetitems");
        sql.append(" WHERE trackitemid = rsetitems.keyid1 AND rsetitems.rsetid = ").append(safeSQL.addVar(trackItemRsetId));
        return this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
    }

    private void processSubmissionAddRequestDetail(AjaxResponse ajaxResponse) {
        String requestItemId = ajaxResponse.getRequestParameter("keyid1", "");
        int itemCount = Integer.parseInt(ajaxResponse.getRequestParameter("itemcount", "0"));
        String requestId = "";
        String requestDetilId = "";
        String errorMessage = "";
        if (itemCount > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT s_requestitemdetailid, requestid, requestitemid FROM s_requestitemdetail WHERE requestitemid = " + safeSQL.addVar(requestItemId);
            DataSet requestDetailDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            int requestDetailCount = requestDetailDs.getRowCount();
            if (requestDetailCount < itemCount) {
                int copies = itemCount - requestDetailCount;
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_RequestItemDetail");
                props.setProperty("copies", copies + "");
                props.setProperty("linksdcid", "Sample");
                props.setProperty("requestitemid", requestItemId);
                try {
                    this.getActionProcessor().processAction("AddSDI", "1", props);
                }
                catch (ActionException e) {
                    errorMessage = "Failed to add request details.";
                }
            } else if (requestDetailCount > itemCount) {
                int extra = requestDetailCount - itemCount;
                StringBuffer requestDetailToDeleteKeyid1 = new StringBuffer();
                for (int i = 0; i < extra; ++i) {
                    requestDetailToDeleteKeyid1.append(";").append(requestDetailDs.getValue(i, "s_requestitemdetailid", ""));
                }
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "LV_RequestItemDetail");
                props.setProperty("keyid1", requestDetailToDeleteKeyid1.substring(1));
                try {
                    this.getActionProcessor().processAction("DeleteSDI", "1", props);
                }
                catch (ActionException e) {
                    errorMessage = "Failed to delete request details.";
                }
            }
            if ((requestDetailDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())).getRowCount() > 0) {
                requestId = requestDetailDs.getColumnValues("requestid", ";");
                requestItemId = requestDetailDs.getColumnValues("requestitemid", ";");
                requestDetilId = requestDetailDs.getColumnValues("s_requestitemdetailid", ";");
            } else {
                errorMessage = "No request-detail found. Check request-items.";
            }
        } else {
            errorMessage = "Sample-Count set to 0";
        }
        ajaxResponse.addCallbackArgument("requestid", requestId);
        ajaxResponse.addCallbackArgument("requestitemid", requestItemId);
        ajaxResponse.addCallbackArgument("requestitemdetailid", requestDetilId);
        ajaxResponse.addCallbackArgument("errormessage", errorMessage);
        ajaxResponse.addCallbackArgument("url", ajaxResponse.getRequestParameter("url", ""));
        ajaxResponse.addCallbackArgument("width", ajaxResponse.getRequestParameter("width", ""));
        ajaxResponse.addCallbackArgument("height", ajaxResponse.getRequestParameter("height", ""));
    }
}

