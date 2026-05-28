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

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetAttachments
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String sdcid = ar.getRequestParameter("sdcid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "");
        String keyid2 = ar.getRequestParameter("keyid2", "");
        String keyid3 = ar.getRequestParameter("keyid3", "");
        ArrayList<String> params = new ArrayList<String>();
        String sql = "SELECT * FROM sdiattachment WHERE sdcid=? AND keyid1=? ";
        params.add(sdcid);
        params.add(keyid1);
        if (!keyid2.equals("")) {
            sql = sql + "AND keyid2=? ";
            params.add(keyid2);
        }
        if (!keyid3.equals("")) {
            sql = sql + "AND keyid3=? ";
            params.add(keyid3);
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])params.toArray(new String[params.size()]));
        ar.addCallbackArgument("attachments", ds);
        ar.print();
    }
}

