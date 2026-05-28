/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetRequestItemsToShip
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String requestid = ajaxResponse.getRequestParameter("requestid", "").trim();
        String sdcid = "";
        String keyid1 = "";
        String submitbydepartmentid = "";
        String sitedepartmentid = "";
        DataSet itemsPendingShipment = new DataSet();
        if (requestid.length() > 0) {
            String sql = "select s_requestitemdetail.linksdcid, s_requestitemdetail.linkkeyid1, (select s_request.submitbydepartmentid from s_request where s_request.s_requestid = s_requestitemdetail.requestid) submitbydepartmentid, (select s_request.sitedepartmentid from s_request where s_request.s_requestid = s_requestitemdetail.requestid) sitedepartmentid,       (select su.labelpath from trackitem ti, storageunit su       where ti.linksdcid = s_requestitemdetail.linksdcid and ti.linkkeyid1 = s_requestitemdetail.linkkeyid1           and ti.currentstorageunitid = su.storageunitid and su.linksdcid = 'LV_Package') currentstorageunit from s_requestitemdetail where s_requestitemdetail.requestid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{requestid});
            if (ds != null && ds.size() > 0) {
                submitbydepartmentid = ds.getString(0, "submitbydepartmentid", "");
                sitedepartmentid = ds.getString(0, "sitedepartmentid", "");
                for (int i = 0; i < ds.size(); ++i) {
                    if (ds.getString(i, "currentstorageunit", "").length() != 0) continue;
                    itemsPendingShipment.copyRow(ds, i, 1);
                }
            }
            if (itemsPendingShipment.size() > 0) {
                sdcid = itemsPendingShipment.getString(0, "linksdcid");
                keyid1 = itemsPendingShipment.getColumnValues("linkkeyid1", ";");
            }
        }
        ajaxResponse.addCallbackArgument("requestid", requestid);
        ajaxResponse.addCallbackArgument("sdcid", sdcid);
        ajaxResponse.addCallbackArgument("keyid1", keyid1);
        ajaxResponse.addCallbackArgument("submitbydepartmentid", submitbydepartmentid);
        ajaxResponse.addCallbackArgument("sitedepartmentid", sitedepartmentid);
        ajaxResponse.print();
    }
}

