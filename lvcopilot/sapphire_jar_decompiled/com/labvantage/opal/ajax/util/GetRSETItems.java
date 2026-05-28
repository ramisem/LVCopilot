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

public class GetRSETItems
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String rsetitems = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String rsetid = ajaxResponse.getRequestParameter("rsetid", "");
        boolean clearrset = "Y".equals(ajaxResponse.getRequestParameter("clearrset", "Y"));
        if (!rsetid.isEmpty()) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select keyid1 from RSETITEMS where rsetid = ? order by rsetseq", (Object[])new String[]{rsetid});
            if (ds != null && !ds.isEmpty()) {
                rsetitems = ds.getColumnValues("keyid1", ";");
            }
            if (clearrset) {
                this.getDAMProcessor().clearRSet(rsetid);
            }
        }
        ajaxResponse.addCallbackArgument("rsetitems", rsetitems);
        ajaxResponse.print();
    }
}

