/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.kit;

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class FindKitInventory
extends BaseAjaxRequest {
    public static final String PROPERTY_REQUESTID = "requestid";
    public static final String PROPERTY_REQUESTITEMID = "requestitemid";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String reagenttypeid = "";
        String inventoryCount = "0";
        String trackitemid = "";
        String requestitemid = ajaxResponse.getRequestParameter(PROPERTY_REQUESTITEMID, "").trim();
        if (requestitemid.length() == 0) {
            message = this.getTranslationProcessor().translate("Missing mandatory property") + " :" + PROPERTY_REQUESTITEMID;
        }
        if (OpalUtil.isNotEmpty(ds = this.getQueryProcessor().getPreparedSqlDataSet("select reagentclass, reagenttypeid, reagenttypeversionid from s_requestitem where s_requestitemid = ?", (Object[])new String[]{requestitemid}))) {
            reagenttypeid = ds.getString(0, "reagenttypeid", "");
            String reagenttypeversionid = ds.getValue(0, "reagenttypeversionid", "1");
            String sql = "select trackitem.trackitemid from trackitem, reagentlot where trackitem.linksdcid = 'LV_ReagentLot' and trackitem.linkkeyid1 = reagentlot.reagentlotid and reagentlot.reagenttypeid = ? and reagentlot.reagenttypeversionid = ? and trackitem.trackitemstatus = 'Valid'";
            DataSet ds2 = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{reagenttypeid, reagenttypeversionid});
            if (OpalUtil.isNotEmpty(ds2)) {
                inventoryCount = String.valueOf(ds2.size());
                trackitemid = ds2.getColumnValues("trackitemid", ";");
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument(PROPERTY_REQUESTITEMID, requestitemid);
        ajaxResponse.addCallbackArgument("reagenttypeid", reagenttypeid);
        ajaxResponse.addCallbackArgument("inventoryCount", inventoryCount);
        ajaxResponse.addCallbackArgument("trackitemid", trackitemid);
        ajaxResponse.print();
    }
}

