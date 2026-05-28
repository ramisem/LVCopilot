/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.request;

import com.labvantage.opal.util.OpalUtil;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetRequestPackageInformation
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String senderdepartmentid = "";
        String senderdepartmentdesc = "";
        String senderaddressid = "";
        String senderaddresstype = "";
        String recipientdepartmentid = "";
        String recipientdepartmentdesc = "";
        String recipientaddressid = "";
        String recipientaddresstype = "";
        String requestid = ajaxResponse.getRequestParameter("requestid", "");
        String requestitemid = ajaxResponse.getRequestParameter("requestitemid", "");
        DataSet ds = null;
        if (requestitemid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select s_request.s_requestid, s_request.requeststatus, s_request.requestclass, s_request.requesterid, s_request.requesttype, s_request.contactaddressid requestcontactaddressid, s_request.contactaddresstype requestcontactaddresstype, s_requestitem.shippinglocationdepartmentid, (select d.departmentdesc from department d where d.departmentid = s_requestitem.shippinglocationdepartmentid) shippinglocationdepartmentdesc, s_requestitem.contactaddressid, s_requestitem.contactaddresstype, s_request.sitedepartmentid, (select d.departmentdesc from department d where d.departmentid = s_request.sitedepartmentid) sitedepartmentdesc, submitbydepartmentid, (select d.departmentdesc from department d where d.departmentid = s_request.submitbydepartmentid) submitbydepartmentdesc from s_request, s_requestitem where s_requestitemid in (" + safeSQL.addIn(requestitemid, "|") + ") and s_requestitem.requestid = s_request.s_requestid";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        } else if (requestid.length() > 0) {
            String sql = "select s_requestid, requeststatus, requestclass, requesterid, requesttype, contactaddressid requestcontactaddressid, contactaddresstype requestcontactaddresstype, '' shippinglocationdepartmentid, '' shippinglocationdepartmentdesc, '' contactaddressid, '' contactaddresstype, sitedepartmentid, (select d.departmentdesc from department d where d.departmentid = s_request.sitedepartmentid) sitedepartmentdesc, submitbydepartmentid, (select d.departmentdesc from department d where d.departmentid = s_request.submitbydepartmentid) submitbydepartmentdesc from s_request where s_requestid = ?";
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{requestid});
        }
        if (OpalUtil.isNotEmpty(ds)) {
            String requesterid = ds.getString(0, "requesterid", "");
            String shippinglocationdepartmentid = ds.getString(0, "shippinglocationdepartmentid", "");
            String shippinglocationdepartmentdesc = ds.getString(0, "shippinglocationdepartmentdesc", "");
            String submitbydepartmentid = ds.getString(0, "submitbydepartmentid", "");
            String submitbydepartmentdesc = ds.getString(0, "submitbydepartmentdesc", "");
            String contactaddressid = ds.getString(0, "contactaddressid", ds.getString(0, "requestcontactaddressid", ""));
            String contactaddresstype = ds.getString(0, "contactaddresstype", ds.getString(0, "requestcontactaddresstype", ""));
            if (contactaddressid.length() == 0) {
                message = this.getTranslationProcessor().translate("Missing shipping contact information in Request");
            } else {
                String sitedepartmentid = ds.getString(0, "sitedepartmentid", "");
                String sitedepartmentdesc = ds.getString(0, "sitedepartmentdesc", "");
                String requestclass = ds.getString(0, "requestclass", "");
                if ("Kit".equals(requestclass) || "Pull".equals(requestclass)) {
                    recipientdepartmentid = shippinglocationdepartmentid.length() == 0 ? submitbydepartmentid : shippinglocationdepartmentid;
                    String string = recipientdepartmentdesc = shippinglocationdepartmentdesc.length() == 0 ? submitbydepartmentdesc : shippinglocationdepartmentdesc;
                    if (requesterid.length() > 0) {
                        recipientaddressid = contactaddressid;
                        recipientaddresstype = contactaddresstype;
                    }
                    senderdepartmentid = sitedepartmentid;
                    senderdepartmentdesc = sitedepartmentdesc;
                    if (senderdepartmentid.length() == 0) {
                        senderdepartmentid = this.getConnectionProcessor().getSapphireConnection().getDefaultDepartment();
                        senderdepartmentdesc = OpalUtil.getColumnValue(this.getQueryProcessor(), "department", "departmentdesc", "departmentid=?", new String[]{senderdepartmentid});
                    }
                } else if ("Submission".equals(requestclass)) {
                    recipientdepartmentid = shippinglocationdepartmentid.length() == 0 ? sitedepartmentid : shippinglocationdepartmentid;
                    String string = recipientdepartmentdesc = shippinglocationdepartmentdesc.length() == 0 ? sitedepartmentdesc : shippinglocationdepartmentdesc;
                    if (requesterid.length() > 0) {
                        recipientaddressid = contactaddressid;
                        recipientaddresstype = contactaddresstype;
                    }
                    senderdepartmentid = submitbydepartmentid;
                    senderdepartmentdesc = submitbydepartmentdesc;
                }
                String sysuserId = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                Map<String, String> sysuserAddressMap = this.getUserAddressInfo(sysuserId);
                senderaddressid = sysuserAddressMap.get("addressid");
                senderaddresstype = sysuserAddressMap.get("addresstype");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("senderdepartmentid", senderdepartmentid);
        ajaxResponse.addCallbackArgument("senderdepartmentdesc", senderdepartmentdesc);
        ajaxResponse.addCallbackArgument("senderaddressid", senderaddressid);
        ajaxResponse.addCallbackArgument("senderaddresstype", senderaddresstype);
        ajaxResponse.addCallbackArgument("recipientdepartmentid", recipientdepartmentid);
        ajaxResponse.addCallbackArgument("recipientdepartmentdesc", recipientdepartmentdesc);
        ajaxResponse.addCallbackArgument("recipientaddressid", recipientaddressid);
        ajaxResponse.addCallbackArgument("recipientaddresstype", recipientaddresstype);
        ajaxResponse.print();
    }

    private Map<String, String> getUserAddressInfo(String sysuserid) {
        HashMap<String, String> map = new HashMap<String, String>();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select addressid, addresstype from sdiaddress where sdcid='User' and keyid1=? order by usersequence", (Object[])new String[]{sysuserid});
        if (OpalUtil.isNotEmpty(ds)) {
            map.put("addressid", ds.getString(0, "addressid", ""));
            map.put("addresstype", ds.getString(0, "addresstype", ""));
        }
        return map;
    }
}

