/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ReloadParent
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        DataSet ds;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String keyid1 = ar.getRequestParameter("parentkeyid1", "");
        String keyid2 = ar.getRequestParameter("parentkeyid2", "");
        String pageId = ar.getRequestParameter("pageid", "");
        HttpSession session = req.getSession();
        PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
        if (pageProps != null && !keyid1.equals("")) {
            String parentSdc = pageProps.getPropertyListNotNull("pagetype").getPropertyListNotNull("parentsdc").getProperty("parentsdcid", "");
            ArrayList<String> params = new ArrayList<String>();
            String tableId = this.getSDCProcessor().getProperty(parentSdc, "tableid", "");
            String primaryKeyCol1 = this.getSDCProcessor().getProperty(parentSdc, "keycolid1", "");
            String primaryKeyCol2 = this.getSDCProcessor().getProperty(parentSdc, "keycolid2", "");
            params.add(keyid1);
            String sql = "SELECT * FROM " + tableId + " WHERE " + primaryKeyCol1 + "=?";
            if (!primaryKeyCol2.equals("")) {
                sql = sql + " and " + primaryKeyCol2 + "=?";
                params.add(keyid2);
            }
            if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])params.toArray(new String[params.size()]))) != null && ds.getRowCount() == 1) {
                ds.addColumn("__rowid", 0);
                ds.setString(0, "__rowid", "parent0");
            } else {
                ds = new DataSet();
            }
        } else {
            ds = new DataSet();
        }
        ar.addCallbackArgument("ds", ds);
        ar.print();
    }
}

