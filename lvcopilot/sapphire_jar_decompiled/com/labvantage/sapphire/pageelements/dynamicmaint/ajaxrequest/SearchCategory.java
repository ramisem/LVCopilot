/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import com.labvantage.sapphire.pageelements.dynamicmaint.util.Utils;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class SearchCategory
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        DataSet ds;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String sdcid = ar.getRequestParameter("sdcid", "");
        String categoryid = ar.getRequestParameter("categoryid", "");
        String elementid = ar.getRequestParameter("elementid", "");
        String focusFieldId = ar.getRequestParameter("focusfieldid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "[keyid1]");
        if (!categoryid.equals("")) {
            String sql = "SELECT categoryid FROM category WHERE sdcid=? AND categoryid=?";
            Object[] params = new String[]{sdcid, categoryid};
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, params);
        } else {
            ds = new DataSet();
        }
        if (ds != null) {
            Utils.fillKeyids(ds, keyid1, "", "");
        }
        ar.addCallbackArgument("category", ds);
        ar.addCallbackArgument("elementid", elementid);
        ar.addCallbackArgument("focusFieldId", focusFieldId);
        ar.print();
    }
}

