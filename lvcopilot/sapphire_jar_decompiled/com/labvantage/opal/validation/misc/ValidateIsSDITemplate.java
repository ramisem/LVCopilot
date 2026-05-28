/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class ValidateIsSDITemplate
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "").trim();
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "").trim();
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "").trim();
        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "").trim();
        String templateflag = "N";
        if (sdcid.length() > 0) {
            int keyColCount = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
            String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
            String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
            String keycolid2 = this.getSDCProcessor().getProperty(sdcid, "keycolid2");
            String keycolid3 = this.getSDCProcessor().getProperty(sdcid, "keycolid3");
            if (!keyid1.contains(";")) {
                DataSet ds;
                SafeSQL safeSQL = new SafeSQL();
                String sql = "select templateflag from " + tableid + " where " + keycolid1 + " = " + safeSQL.addVar(keyid1);
                if (keyColCount > 1) {
                    sql = sql + " and " + keycolid2 + " = " + safeSQL.addVar(keyid2);
                    if (keyColCount > 2) {
                        sql = sql + " and " + keycolid3 + " = " + safeSQL.addVar(keyid3);
                    }
                }
                if ((ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())) != null && ds.size() > 0) {
                    templateflag = ds.getString(0, "templateflag", "N");
                }
            } else {
                try {
                    String rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                    String sql = "select t.templateflag from " + tableid + " t, rsetitems r";
                    sql = sql + " where t." + keycolid1 + " = r.keyid1";
                    if (keyColCount > 1) {
                        sql = sql + " and t." + keycolid2 + " = r.keyid2";
                        if (keyColCount > 2) {
                            sql = sql + " and t." + keycolid3 + " = r.keyid3";
                        }
                    }
                    sql = sql + " and t.templateflag = 'Y'";
                    sql = sql + " and r.rsetid = ?";
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{rsetid});
                    if (ds != null && ds.size() > 0) {
                        templateflag = "Y";
                    }
                }
                catch (SapphireException e) {
                    e.printStackTrace();
                }
            }
        }
        ajaxResponse.addCallbackArgument("templateflag", templateflag);
        ajaxResponse.print();
    }
}

