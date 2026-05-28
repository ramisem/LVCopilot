/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.pkg;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;

public class PackageValidate
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 54732 $";
    private static final String MODE_EDIT = "edit";
    private static final String MODE_MANAGE = "manage";
    private static final String MODE_UNPACK = "unpack";
    private static final String MODE_PACK = "pack";
    private static final String MODE_SHIP = "ship";
    private static final String MODE_UNSHIP = "unship";
    private static final String MODE_ONHOLD = "onhold";
    private static final String MODE_REMOVEHOLD = "removehold";
    private static final String MODE_EMPTY = "empty";
    private static final String MODE_RECEIVE = "receive";
    private static final String MODE_CANCEL = "cancel";
    private static final String MODE_ASSIGNSTORAGE = "assign";
    private static final String MODE_UNASSIGNSTORAGE = "unassign";
    private static final String MODE_UPDATESENDER = "updatesender";
    private static final String MODE_UPDATERECEIVER = "updatereceiver";
    private static final String RETURNTOLISTPAGE = "LV_PackageList";
    public static final String PAGE_TISM = "LV_FileTrackItem";
    public static final String LV_BOX_TYPESORTED = "Sorted";
    public static final String LV_BOX_TYPEUNSORTED = "Unsorted";
    private List<String> departmentList;
    public static ArrayList<String> allowedManageStatus = new ArrayList();

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String nextUrl;
        String message;
        AjaxResponse ajaxResponse;
        boolean isOperationCall;
        block41: {
            String returntolistpage;
            String packageid;
            isOperationCall = false;
            ajaxResponse = new AjaxResponse(request, response);
            String mode = ajaxResponse.getRequestParameter("mode");
            if (StringUtil.getLen(mode) == 0L) {
                mode = request.getParameter("mode");
                boolean bl = isOperationCall = StringUtil.getLen(mode) > 0L;
            }
            if (StringUtil.getLen(packageid = ajaxResponse.getRequestParameter("packageid")) == 0L) {
                packageid = request.getParameter("packageid");
            }
            if (StringUtil.getLen(returntolistpage = ajaxResponse.getRequestParameter("returntolistpage")) == 0L) {
                returntolistpage = request.getParameter("returntolistpage");
            }
            if (returntolistpage == null || returntolistpage.trim().length() == 0) {
                returntolistpage = RETURNTOLISTPAGE;
            }
            String currentuser = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            message = "";
            nextUrl = "";
            try {
                HashMap<String, String> map = PackageValidate.getPackageInfo(this.getQueryProcessor(), packageid);
                if (map == null || map.size() <= 0) break block41;
                map.put("returntolistpage", returntolistpage);
                map.put("currentuser", currentuser);
                try {
                    if (MODE_EDIT.equals(mode)) {
                        nextUrl = this.editPackage(map);
                    }
                    if (MODE_MANAGE.equals(mode)) {
                        nextUrl = this.managePackage(map);
                    } else if (MODE_UNPACK.equals(mode)) {
                        nextUrl = this.unpackPackage(map);
                    } else if (MODE_PACK.equals(mode)) {
                        nextUrl = this.packPackage(map);
                    } else if (MODE_UPDATESENDER.equals(mode)) {
                        nextUrl = this.validateSender(map);
                    } else if (MODE_UPDATERECEIVER.equals(mode)) {
                        nextUrl = this.validateReceiver(map);
                    }
                }
                catch (SapphireException e) {
                    message = e.getMessage();
                }
                try {
                    if (MODE_SHIP.equals(mode)) {
                        this.shipPackage(map);
                    } else if (MODE_UNSHIP.equals(mode)) {
                        this.unshipPackage(map);
                    } else if (MODE_ONHOLD.equals(mode)) {
                        this.placePackageOnHold(map);
                    } else if (MODE_REMOVEHOLD.equals(mode)) {
                        this.removePackageFromHold(map);
                    } else if (MODE_EMPTY.equals(mode)) {
                        this.emptyPackage(map, currentuser);
                    } else if (MODE_RECEIVE.equals(mode)) {
                        this.receivePackage(map);
                    } else if (MODE_CANCEL.equals(mode)) {
                        this.cancelPackage(map);
                    } else if (MODE_ASSIGNSTORAGE.equals(mode)) {
                        this.assignPackageStorage(map);
                    } else if (MODE_UNASSIGNSTORAGE.equals(mode)) {
                        this.unassignPackageStorage();
                    }
                }
                catch (SapphireException e) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("<div style='font:bold 12px verdana;color:red'>");
                    sb.append("Validation Error");
                    sb.append("</div>");
                    sb.append("<hr>");
                    sb.append("<div style='font:normal 12px verdana;color:red'>");
                    sb.append(e.getMessage());
                    sb.append("</div>");
                    message = sb.toString();
                }
            }
            catch (Exception e) {
                message = e.getMessage();
            }
        }
        if (isOperationCall) {
            try {
                PrintWriter out = response.getWriter();
                out.write(message);
                out.flush();
                out.close();
            }
            catch (IOException e) {
                this.logger.error("Error", e);
            }
        } else {
            ajaxResponse.addCallbackArgument("message", message);
            ajaxResponse.addCallbackArgument("url", nextUrl);
            ajaxResponse.print();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String editPackage(HashMap packageMap) throws SapphireException {
        String nextURL = "";
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
        String returntolistpage = (String)packageMap.get("returntolistpage");
        String packageid = (String)packageMap.get("packageid");
        if (status.equalsIgnoreCase("Cancelled")) {
            if (this.isDepartmentMember(recipientdepartmentid)) {
                return "rc?command=page&page=LV_ExpPackageMaint&returntolistpage=" + returntolistpage + "&sdcid=" + "LV_Package" + "&keyid1=" + HttpUtil.encodeURIComponent(packageid) + "&mode=Edit";
            }
            if (!this.isDepartmentMember(senderdepartmentid)) throw new SapphireException(this.getTranslationProcessor().translate("User must be a member of Package's Origination or Destination custodial domain to edit Package details"));
            return "rc?command=page&page=LV_NewPackageMaint&returntolistpage=" + returntolistpage + "&sdcid=" + "LV_Package" + "&keyid1=" + HttpUtil.encodeURIComponent(packageid, "UTF-8") + "&mode=Edit";
        }
        if (status.equalsIgnoreCase("Expected") || status.equalsIgnoreCase("On Hold") || status.equalsIgnoreCase("Emptied") || status.equalsIgnoreCase("Received")) {
            if (!this.isDepartmentMember(recipientdepartmentid)) throw new SapphireException(this.getTranslationProcessor().translate("User must be a member of Package's Destination custodial domain to edit Package details"));
            return "rc?command=page&page=LV_ExpPackageMaint&returntolistpage=" + returntolistpage + "&sdcid=" + "LV_Package" + "&keyid1=" + HttpUtil.encodeURIComponent(packageid) + "&mode=Edit";
        }
        if (!this.isDepartmentMember(senderdepartmentid)) throw new SapphireException(this.getTranslationProcessor().translate("User must be a member of Package's Origination custodial domain to edit Package details"));
        return "rc?command=page&page=LV_NewPackageMaint&returntolistpage=" + returntolistpage + "&sdcid=" + "LV_Package" + "&keyid1=" + HttpUtil.encodeURIComponent(packageid, "UTF-8") + "&mode=Edit";
    }

    private String managePackage(HashMap packageMap) throws SapphireException {
        String forwardPage;
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
        String returntolistpage = (String)packageMap.get("returntolistpage");
        String packageid = (String)packageMap.get("packageid");
        if (!allowedManageStatus.contains(status)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You can manage contents of only Created, Expected, Received, UnPacked, Emptied or On-Hold package."));
        }
        if (status.equals("Created")) {
            if (!this.isDepartmentMember(senderdepartmentid)) {
                throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of Package's origination custodial domain.") + " (" + packageid + ")");
            }
            forwardPage = "LV_PackNewPackage";
        } else {
            if (!this.isDepartmentMember(recipientdepartmentid)) {
                throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of Package's destination custodial domain.") + " (" + packageid + ")");
            }
            forwardPage = "LV_PackExpPackage";
        }
        String packageStorageUnitID = StorageUnitSDC.getStorageUnitId(this.getQueryProcessor(), "LV_Package", packageid);
        if (packageStorageUnitID == null || packageStorageUnitID.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Package does not have associated Storage Unit."));
        }
        return "rc?command=page&page=" + forwardPage + "&returntolistpage=" + returntolistpage + "&targetsdcid=StorageUnitSDC&targetkeyid1=" + packageStorageUnitID;
    }

    private String unpackPackage(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String lockedby = OpalUtil.getSDILockedBy(this.getQueryProcessor(), "LV_Package", packageid);
        if (StringUtil.getLen(lockedby) > 0L) {
            throw new SapphireException("{{The package you are trying to unpack has been locked by}} " + lockedby);
        }
        String returntolistpage = (String)packageMap.get("returntolistpage");
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        if (!status.equals("Received")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Package has status \"" + status + "\".") + "<br>" + this.getTranslationProcessor().translate("Only Received packages can be unpacked.") + " (" + packageid + ")");
        }
        if (!this.isDepartmentMember(recipientdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of Package's destination custodial domain"));
        }
        String nextURL = "rc?command=page&page=LV_UnpackPackage&returntolistpage=" + returntolistpage + "&sourcesdcid=StorageUnitSDC&sourcekeyid1=" + StorageUnitSDC.getStorageUnitId(this.getQueryProcessor(), "LV_Package", packageid);
        return nextURL;
    }

    private String packPackage(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String returntolistpage = (String)packageMap.get("returntolistpage");
        String packagetype = (String)packageMap.get("packagetype");
        String status = (String)packageMap.get("status");
        String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
        if (!packagetype.equalsIgnoreCase("PKG")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The package is not of type \"PKG\".") + " (" + packageid + ")");
        }
        if (!status.equalsIgnoreCase("Created")) {
            throw new SapphireException("The package (" + packageid + ") has status \"" + status + "\".<br>" + this.getTranslationProcessor().translate("Only packages with a status of \"Created\" can have boxes packed."));
        }
        if (!this.isDepartmentMember(senderdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of package's origination custodial domain.") + " (" + packageid + ")");
        }
        String packageStorageUnitID = StorageUnitSDC.getStorageUnitId(this.getQueryProcessor(), "LV_Package", packageid);
        if (packageStorageUnitID == null || packageStorageUnitID.length() == 0) {
            throw new SapphireException(this.getTranslationProcessor().translate("Package does not have associated Storage Unit."));
        }
        String nextURL = "rc?command=page&page=LV_PackBoxPackage&returntolistpage=" + returntolistpage + "&targetsdcid=StorageUnitSDC&targetkeyid1=" + packageStorageUnitID;
        return nextURL;
    }

    private void shipPackage(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String status = (String)packageMap.get("status");
        String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
        if (!status.equalsIgnoreCase("Created")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The package has status \"" + status + "\".") + "<br>" + this.getTranslationProcessor().translate("Only packages with status of \"Created\" can be shipped.") + " (" + packageid + ")");
        }
        if (!this.isDepartmentMember(senderdepartmentid)) {
            throw new SapphireException("You are not a member of package's (" + packageid + ") Origination custodial domain (" + senderdepartmentid + ").");
        }
    }

    private void unshipPackage(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String status = (String)packageMap.get("status");
        String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
        if (!status.equalsIgnoreCase("Shipped")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The package has a status \"" + status + "\".") + "<br>" + this.getTranslationProcessor().translate("Only packages with status of \"Shipped\" can be unshipped.") + " (" + packageid + ")");
        }
        if (!this.isDepartmentMember(senderdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of package's origination custodial domain. (" + packageid + ")"));
        }
    }

    private void placePackageOnHold(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String packagetype = (String)packageMap.get("packagetype");
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        if (!status.equalsIgnoreCase("Received")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Only packages with status of \"Received\" can be placed on hold"));
        }
        if (!this.isDepartmentMember(recipientdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of Package's Destination custodial domain.") + " (" + packageid + ")");
        }
        if (!packagetype.equals("PKG")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Only packages of type \"PKG\" can be placed on hold"));
        }
    }

    private void removePackageFromHold(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String packagetype = (String)packageMap.get("packagetype");
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        if (!status.equalsIgnoreCase("On Hold")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The package has status \"" + status + "\".") + "<br>" + this.getTranslationProcessor().translate("Only packages with a status of \"On Hold\" can be removed from hold.") + " (" + packageid + ")");
        }
        if (!this.isDepartmentMember(recipientdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of Package's destination custodial domain.") + " (" + packageid + ")");
        }
        if (!packagetype.equals("PKG")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Only packages of type \"PKG\" can be removed from hold"));
        }
    }

    private void emptyPackage(HashMap packageMap, String currentuser) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String packagetype = (String)packageMap.get("packagetype");
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        if (!status.equalsIgnoreCase("Received") && !status.equalsIgnoreCase("UnPacked")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The package has status \"" + status + "\".") + "<br>" + this.getTranslationProcessor().translate("Only received/unpacked packages can be marked empty.") + " (" + packageid + ")");
        }
        if (!this.isDepartmentMember(recipientdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of Package's destination custodial domain.") + " (" + packageid + ")");
        }
        if (!packagetype.equals("PKG")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Only packages of type \"PKG\" can be marked empty."));
        }
        if (!PackageValidate.isPackageEmpty(this.getQueryProcessor(), packageid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Package is not empty.") + "<br>" + this.getTranslationProcessor().translate("Only empty packages can be marked empty.") + " (" + packageid + ")");
        }
        if (!PackageValidate.isContactInfoAvailable(this.getQueryProcessor(), currentuser)) {
            throw new SapphireException(this.getTranslationProcessor().translate("No contact information available for the current user to record against this operation. Please set up your contact information."));
        }
    }

    private void receivePackage(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String packagetype = (String)packageMap.get("packagetype");
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        if (!status.equalsIgnoreCase("Shipped") && !status.equalsIgnoreCase("Expected")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The package has status \"" + status + "\".") + "<br>" + this.getTranslationProcessor().translate("Only packages with status of \"Shipped\" or \"Expected\" can be received") + " (" + packageid + ")");
        }
        if (!this.isDepartmentMember(recipientdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of Package's destination custodial domain.") + " (" + packageid + ")");
        }
        if (!packagetype.equals("PKG")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Only packages of type \"PKG\" can be received"));
        }
    }

    private void cancelPackage(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String packagetype = (String)packageMap.get("packagetype");
        String status = (String)packageMap.get("status");
        String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        if (!status.equalsIgnoreCase("Created") && !status.equalsIgnoreCase("Expected")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The package has status \"" + status + "\".") + "<br>" + this.getTranslationProcessor().translate("Only \"Created\" or \"Expected\" packages can be cancelled.") + " (" + packageid + ")");
        }
        if (!this.isDepartmentMember(recipientdepartmentid) && !this.isDepartmentMember(senderdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of Package's destination or origination custodial domain.") + " (" + packageid + ")");
        }
        if (!packagetype.equals("PKG")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Only packages of type \"PKG\" can be cancelled"));
        }
        if (!PackageValidate.isPackageEmpty(this.getQueryProcessor(), packageid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("Package is not empty.") + "<br>" + this.getTranslationProcessor().translate("Only empty packages can be cancelled."));
        }
    }

    private void assignPackageStorage(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String status = (String)packageMap.get("status");
        if (status.equalsIgnoreCase("Cancelled") || status.equalsIgnoreCase("Shipped")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The package has status \"" + status + "\".") + "<br>" + this.getTranslationProcessor().translate("Packages with this status can not be assigned to temporary location.") + " (" + packageid + ")");
        }
    }

    private void unassignPackageStorage() {
    }

    private String validateSender(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String returntolistpage = (String)packageMap.get("returntolistpage");
        String status = (String)packageMap.get("status");
        if (status.equals("Emptied")) {
            throw new SapphireException(this.getTranslationProcessor().translate("You can not update the sender information once a package is Emptied.") + "<br>" + this.getTranslationProcessor().translate("Use the Update Receiver Information page."));
        }
        return "rc?command=page&page=LV_ProxyPackageSend&returntolistpage=" + returntolistpage + "&sdcid=" + "LV_Package" + "&keyid1=" + HttpUtil.encodeURIComponent(packageid);
    }

    private String validateReceiver(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String returntolistpage = (String)packageMap.get("returntolistpage");
        String status = (String)packageMap.get("status");
        if (!status.equals("Shipped") && !status.equals("Emptied")) {
            throw new SapphireException(this.getTranslationProcessor().translate("You can not update the receiver information until it is Shipped.") + "<br>" + this.getTranslationProcessor().translate("Use the Update Sender Information page."));
        }
        return "rc?command=page&page=LV_ProxyPackageRec&returntolistpage=" + returntolistpage + "&sdcid=" + "LV_Package" + "&keyid1=" + HttpUtil.encodeURIComponent(packageid, "UTF-8");
    }

    private static boolean isPackageEmpty(QueryProcessor qp, String packageid) {
        DataSet ds = qp.getPreparedSqlDataSet("select trackitem.trackitemid from trackitem where trackitem.currentstorageunitid = ( select storageunit.storageunitid from storageunit where storageunit.linksdcid = 'LV_Package' and storageunit.linkkeyid1 = ? )", (Object[])new String[]{packageid});
        return ds != null && ds.size() == 0;
    }

    private static boolean isContactInfoAvailable(QueryProcessor qp, String currentuser) {
        DataSet ds = qp.getPreparedSqlDataSet("SELECT keyid1 FROM sdiaddress WHERE  sdcid = 'User'  AND keyid1 = ?  AND addresstype = 'Contact'", new Object[]{currentuser});
        return ds != null && ds.size() > 0;
    }

    public static boolean isAnySampleTemporaryInLab(QueryProcessor qp, String sdcid, String keyid1) {
        StringBuilder sql = new StringBuilder();
        String storageunitsize = OpalUtil.getColumnValue(qp, "storageunit", "storageunitsize", "linksdcid = ? and linkkeyid1=?", new String[]{sdcid, keyid1});
        if ("0".equals(storageunitsize)) {
            sql.append("select s.samplestatus from s_sample s");
            sql.append(" where s.s_sampleid in");
            sql.append(" ( select t.linkkeyid1 from trackitem t");
            sql.append(" where t.linksdcid = 'Sample'");
            sql.append(" and t.currentstorageunitid =");
            sql.append(" ( select su.storageunitid from storageunit su");
            sql.append(" where su.linksdcid = ?");
            sql.append(" and su.linkkeyid1 = ?");
            sql.append(" ) ) and s.storagestatus = 'Temporary In Lab'");
        } else {
            sql.append("select s.samplestatus from s_sample s");
            sql.append(" where s.s_sampleid in");
            sql.append(" ( select t.linkkeyid1 from trackitem t");
            sql.append(" where t.linksdcid = 'Sample'");
            sql.append(" and t.currentstorageunitid in ");
            sql.append(" ( select su.storageunitid from storageunit su");
            sql.append(" where su.parentid =( select su2.storageunitid from storageunit su2");
            sql.append(" where su2.linksdcid = ? and su2.linkkeyid1 = ? ) ) )");
            sql.append(" and s.storagestatus = 'Temporary In Lab'");
        }
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{sdcid, keyid1});
        return ds != null && ds.size() > 0;
    }

    protected static String processAction(ActionProcessor ap, String actionid, String actionversionid, HashMap props) throws ActionException {
        ErrorHandler errorHandler;
        StringBuilder sb = new StringBuilder();
        ap.processAction(actionid, actionversionid, props);
        if (ap.hasInfoErrors() && (errorHandler = ap.getErrorHandler()) != null) {
            for (Object anErrorHandler : errorHandler) {
                ErrorDetail detail = (ErrorDetail)anErrorHandler;
                sb.append(detail.getErrorid()).append(": ").append(detail.getMessage()).append("<br>");
            }
        }
        return sb.toString();
    }

    public static HashMap<String, String> getPackageInfo(QueryProcessor queryProcessor, String packageid) {
        HashMap<String, String> map = new HashMap<String, String>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT PACKAGESTATUS, PACKAGETYPE, SENDERDEPARTMENTID, RECIPIENTDEPARTMENTID");
        sql.append(" FROM ").append("s_package");
        sql.append(" WHERE S_PACKAGEID = ?");
        DataSet ds = queryProcessor.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{packageid});
        if (ds != null && ds.size() > 0) {
            map.put("packageid", packageid);
            map.put("packagetype", ds.getString(0, "PACKAGETYPE", ""));
            map.put("status", ds.getString(0, "PACKAGESTATUS", ""));
            map.put("senderdepartmentid", ds.getString(0, "SENDERDEPARTMENTID", ""));
            map.put("recipientdepartmentid", ds.getString(0, "RECIPIENTDEPARTMENTID", ""));
        }
        return map;
    }

    public static DataSet getPackageContent(QueryProcessor qp, String packageid) {
        DataSet content = new DataSet();
        content.addColumn("sdcid", 0);
        content.addColumn("keyid1", 0);
        DataSet ds = qp.getPreparedSqlDataSet("select trackitem.linksdcid, trackitem.linkkeyid1 from trackitem where trackitem.currentstorageunitid = (select storageunit.storageunitid from storageunit where storageunit.linksdcid = 'LV_Package' and storageunit.linkkeyid1 = ? )", (Object[])new String[]{packageid});
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                int row = content.addRow();
                content.setValue(row, "sdcid", ds.getValue(i, "linksdcid"));
                content.setValue(row, "keyid1", ds.getValue(i, "linkkeyid1"));
            }
        }
        return content;
    }

    protected boolean isDepartmentMember(String department) {
        return this.getDepartmentList().contains(department);
    }

    @Override
    protected List getDepartmentList() {
        if (this.departmentList == null) {
            this.departmentList = new ArrayList<String>();
            this.departmentList.addAll(Arrays.asList(StringUtil.split(this.getConnectionProcessor().getSapphireConnection().getDepartmentList(), ";")));
        }
        return this.departmentList;
    }

    static {
        allowedManageStatus.add("Created");
        allowedManageStatus.add("Received");
        allowedManageStatus.add("UnPacked");
        allowedManageStatus.add("Emptied");
        allowedManageStatus.add("Expected");
        allowedManageStatus.add("On Hold");
        allowedManageStatus.add("Unpacked");
    }
}

