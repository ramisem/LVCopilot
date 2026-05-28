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

import com.labvantage.opal.validation.pkg.PackageValidate;
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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.HttpUtil;
import sapphire.util.StringUtil;

public class CDTValidate
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    private static final String MODE_RECEIVE = "receive";
    private static final String MODE_UNPACK = "unpack";
    private static final String MODE_CANCEL = "cancel";
    private static final String MODE_EDIT = "edit";
    private static final String RETURNTOLISTPAGE = "LV_CDTList";
    private List<String> departmentList;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        boolean isOperationCall;
        String message;
        String nextURL;
        block20: {
            String returntolistpage;
            String packageid;
            nextURL = "";
            message = "";
            isOperationCall = false;
            String currentuser = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
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
            try {
                HashMap<String, String> map = PackageValidate.getPackageInfo(this.getQueryProcessor(), packageid);
                if (map.size() <= 0) break block20;
                map.put("returntolistpage", returntolistpage);
                map.put("currentuser", currentuser);
                try {
                    if (MODE_UNPACK.equals(mode)) {
                        nextURL = this.unpackCDT(map);
                    } else if (MODE_EDIT.equals(mode)) {
                        nextURL = this.editCDT(map);
                    }
                }
                catch (SapphireException e) {
                    message = e.getMessage();
                }
                try {
                    if (MODE_RECEIVE.equals(mode)) {
                        this.receiveCDT(map);
                    } else if (MODE_CANCEL.equals(mode)) {
                        this.cancelCDT(map);
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
            ajaxResponse.addCallbackArgument("url", nextURL);
            ajaxResponse.print();
        }
    }

    private String unpackCDT(HashMap packageMap) throws SapphireException {
        String packageid = (String)packageMap.get("packageid");
        String returntolistpage = (String)packageMap.get("returntolistpage");
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        if (!status.equals("Received") && !status.equals("Cancelled")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Selected CDT has status") + " " + status + ".<br>" + this.getTranslationProcessor().translate("Only Received/Cancelled CDTs can be unpacked."));
        }
        if (!this.isDepartmentMember(recipientdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of CDT's destination custodial domain"));
        }
        String nextURL = "rc?command=page&page=LV_UnpackPackage&returntolistpage=" + returntolistpage + "&sourcesdcid=StorageUnitSDC&sourcekeyid1=" + StorageUnitSDC.getStorageUnitId(this.getQueryProcessor(), "LV_Package", packageid);
        return nextURL;
    }

    private void receiveCDT(HashMap packageMap) throws SapphireException {
        String packagetype = (String)packageMap.get("packagetype");
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        if (!status.equalsIgnoreCase("Shipped")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The selected CDT has status") + " " + status + ".<br>" + this.getTranslationProcessor().translate("Only CDTs with status of Shipped can be received"));
        }
        if (!this.isDepartmentMember(recipientdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of CDT's destination custodial domain"));
        }
        if (!packagetype.equals("CDT")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Only CDTs can be received"));
        }
    }

    private void cancelCDT(HashMap packageMap) throws SapphireException {
        String packagetype = (String)packageMap.get("packagetype");
        String status = (String)packageMap.get("status");
        String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
        if (!status.equalsIgnoreCase("Shipped")) {
            throw new SapphireException(this.getTranslationProcessor().translate("The selected CDT has status") + " " + status + "<br>" + this.getTranslationProcessor().translate("Only Shipped CDTs can be cancelled"));
        }
        if (!this.isDepartmentMember(senderdepartmentid)) {
            throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of CDT's origination custodial domain."));
        }
        if (!packagetype.equals("CDT")) {
            throw new SapphireException(this.getTranslationProcessor().translate("Only packages of type CDT can be cancelled"));
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private String editCDT(HashMap packageMap) throws SapphireException {
        String nextURL = "";
        String status = (String)packageMap.get("status");
        String recipientdepartmentid = (String)packageMap.get("recipientdepartmentid");
        String senderdepartmentid = (String)packageMap.get("senderdepartmentid");
        String returntolistpage = (String)packageMap.get("returntolistpage");
        String packageid = (String)packageMap.get("packageid");
        if (status.equalsIgnoreCase("Expected") || status.equalsIgnoreCase("On Hold") || status.equalsIgnoreCase("Received")) {
            if (!this.isDepartmentMember(recipientdepartmentid)) throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of CDT's destination custodial domain."));
            return "rc?command=page&page=LV_CDTMaint&returntolistpage=" + returntolistpage + "&sdcid=" + "LV_Package" + "&keyid1=" + HttpUtil.encodeURIComponent(packageid) + "&mode=Edit";
        }
        if (!this.isDepartmentMember(senderdepartmentid)) throw new SapphireException(this.getTranslationProcessor().translate("You are not a member of CDT's origination custodial domain."));
        return "rc?command=page&page=LV_CDTMaint&returntolistpage=" + returntolistpage + "&sdcid=" + "LV_Package" + "&keyid1=" + HttpUtil.encodeURIComponent(packageid) + "&mode=Edit";
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
}

