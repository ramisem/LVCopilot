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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ReloadSqlDataSet
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        DataSet ds;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String sdcId = ar.getRequestParameter("sdcid", "");
        String keyid1 = ar.getRequestParameter("keyid1", "");
        String keyid2 = ar.getRequestParameter("keyid2", "");
        String keyid3 = ar.getRequestParameter("keyi3", "");
        String elementId = ar.getRequestParameter("elementid", "");
        String pageId = ar.getRequestParameter("pageid", "");
        HttpSession session = req.getSession();
        PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
        if (pageProps != null) {
            String sql = pageProps.getPropertyListNotNull(elementId).getProperty("linksql", "");
            if (!sql.equals("")) {
                SafeSQL safeSQL = new SafeSQL();
                sql = SafeSQL.replaceAllWithVars(sql, "'[sdcid]'", sdcId, safeSQL);
                sql = SafeSQL.replaceAllWithVars(sql, "'[keyid1]'", keyid1, safeSQL);
                sql = SafeSQL.replaceAllWithVars(sql, "'[keyid2]'", keyid2, safeSQL);
                sql = SafeSQL.replaceAllWithVars(sql, "'[keyid3]'", keyid3, safeSQL);
                sql = SafeSQL.replaceAllWithVars(sql, "'[%currentuser%]'", this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getSysuserId(), safeSQL);
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            } else {
                ds = new DataSet();
            }
        } else {
            ds = new DataSet();
        }
        ds.addColumn("__rowid", 0);
        for (int i = 0; i < ds.getRowCount(); ++i) {
            ds.setString(i, "__rowid", elementId + i);
        }
        ar.addCallbackArgument("ds", ds);
        ar.print();
    }
}

