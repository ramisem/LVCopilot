/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ValidateEffectiveDate
extends BaseAjaxRequest {
    public static final String ESC_TILDE = "#tilde#";
    public static String LABVANTAGE_CVS_ID = "$Revision: 56332 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcId");
        String keyid1 = ajaxResponse.getRequestParameter("keyId1");
        String keyid2 = ajaxResponse.getRequestParameter("keyId2");
        String keyid3 = ajaxResponse.getRequestParameter("keyId3");
        StringBuilder responseData = new StringBuilder();
        SDCProcessor sdcProcessor = this.getSDCProcessor();
        PropertyList sdcPropertyList = sdcProcessor.getPropertyList(sdcid);
        boolean tripleKey = sdcPropertyList.getProperty("keycolumns").equals("3");
        String tableid = sdcPropertyList.getProperty("tableid");
        String keycolid1 = sdcPropertyList.getProperty("keycolid1");
        String keycolid2 = sdcPropertyList.getProperty("keycolid2");
        String keycolid3 = sdcPropertyList.getProperty("keycolid3");
        QueryProcessor qp = this.getQueryProcessor();
        StringBuilder sql = new StringBuilder();
        DataSet ds = null;
        SafeSQL safeSQL = new SafeSQL();
        String sql1 = "select * from sdiapproval where approvalfunction = 'Versioned' and sdcid = " + safeSQL.addVar(sdcid) + " and keyid1 = " + safeSQL.addVar(keyid1) + " and keyid2 = " + safeSQL.addVar(keyid2);
        if (tripleKey) {
            sql1 = sql1 + " and keyid3 = " + safeSQL.addVar(keyid3);
        }
        if ((ds = qp.getPreparedSqlDataSet(sql1, safeSQL.getValues())) != null && ds.getRowCount() > 0) {
            responseData.append("sdiapproval").append(ESC_TILDE);
        } else {
            responseData.append(ESC_TILDE);
        }
        ds = null;
        safeSQL.reset();
        sql.append("SELECT 'out' FROM ").append(tableid).append(" t WHERE t.versionstatus = 'P' and t.").append(keycolid1).append(" = ").append(safeSQL.addVar(keyid1)).append(" AND t.").append(keycolid2).append(" = ").append(safeSQL.addVar(keyid2));
        if (tripleKey) {
            sql.append(" AND t.").append(keycolid3).append(" = ").append(safeSQL.addVar(keyid3));
        }
        sql.append(" AND t.").append("versioneffectivedt").append(" < ").append(" ( SELECT MAX(").append("versioneffectivedt").append(") FROM ").append(tableid).append(" t2 ").append(" WHERE t2.").append(keycolid1).append(" = t.").append(keycolid1).append(" AND t2.").append(keycolid2).append(" <> t.").append(keycolid2);
        if (tripleKey) {
            sql.append(" AND t2.").append(keycolid3).append(" = t.").append(keycolid3);
        }
        sql.append(" ) ");
        ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.getRowCount() > 0) {
            responseData.append("futureED").append(ESC_TILDE);
        } else {
            responseData.append(ESC_TILDE);
        }
        responseData.append(sdcid).append(ESC_TILDE).append(keyid1).append(ESC_TILDE).append(keyid2).append(ESC_TILDE).append(keyid3);
        ajaxResponse.addCallbackArgument("data", responseData.toString());
        ajaxResponse.print();
    }
}

