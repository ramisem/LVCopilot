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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetKitRequestDetailItems
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String requestitemid = ajaxResponse.getRequestParameter("requestitemid", "");
        String requestid = "";
        String trackitemid = "";
        if (requestitemid.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select s_requestitemdetailid, requestid, linksdcid, linkkeyid1, requestitemdetailstatus, (select s_requestitem.shippinglocationdepartmentid from s_requestitem where s_requestitem.s_requestitemid = s_requestitemdetail.requestitemid) shippinglocationdepartmentid, (select s_requestitem.contactaddressid from s_requestitem where s_requestitem.s_requestitemid = s_requestitemdetail.requestitemid) contactaddressid, (select su.linkkeyid1 from storageunit su, trackitem ti where ti.TRACKITEMID = s_requestitemdetail.linkkeyid1 and ti.currentstorageunitid = su.storageunitid) packageid from s_requestitemdetail where requestitemid in (" + safeSQL.addIn(requestitemid, "|") + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (OpalUtil.isNotEmpty(ds)) {
                String shippinglocationdepartmentid = ds.getString(0, "shippinglocationdepartmentid", "");
                String contactaddressid = ds.getString(0, "contactaddressid", "");
                DataSet unshippedds = new DataSet();
                for (int i = 0; i < ds.size(); ++i) {
                    if (!shippinglocationdepartmentid.equals(ds.getString(i, "shippinglocationdepartmentid", "")) || !contactaddressid.equals(ds.getString(i, "contactaddressid", ""))) {
                        message = this.getTranslationProcessor().translate("All selected items must have same Ship To Contact information.");
                        break;
                    }
                    if (ds.getString(i, "packageid", "").length() != 0) continue;
                    unshippedds.copyRow(ds, i, 1);
                }
                if (unshippedds.size() > 0) {
                    requestid = unshippedds.getString(0, "requestid");
                    trackitemid = unshippedds.getColumnValues("linkkeyid1", ";");
                } else {
                    message = this.getTranslationProcessor().translate("No items found to be shipped in the selected Kit Request. All items have already been shipped.");
                }
            } else {
                message = this.getTranslationProcessor().translate("No Kits found to be shipped in the selected Kit Request. Please make sure you have reserved Kits to be shipped.");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("requestid", requestid);
        ajaxResponse.addCallbackArgument("trackitemid", trackitemid);
        ajaxResponse.print();
    }
}

