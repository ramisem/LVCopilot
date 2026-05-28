/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.scheduleplan.ajax;

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetScheduleTemplateShareCount
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String sdcId = ar.getRequestParameter("sdcid");
        String keyId1 = ar.getRequestParameter("keyid1");
        String keyId2 = ar.getRequestParameter("keyid2");
        String keyId3 = ar.getRequestParameter("keyid3");
        if (sdcId.isEmpty()) {
            throw new ServletException("SDC ID is empty");
        }
        if (keyId1.isEmpty()) {
            throw new ServletException("Key ID1 is empty");
        }
        if (keyId1.contains(";")) {
            throw new ServletException("Only single key ID is supported");
        }
        ArrayList<String> sqlParams = new ArrayList<String>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) c FROM scheduleplanitem spi WHERE spi.scheduletemplatesdcid = ? AND scheduletemplatekeyid1 = ?");
        sqlParams.add(sdcId);
        sqlParams.add(keyId1);
        if (!keyId2.isEmpty()) {
            sql.append(" AND scheduletemplatekeyid2 = ?");
            sqlParams.add(keyId2);
            if (!keyId3.isEmpty()) {
                sql.append(" AND scheduletemplatekeyid3 = ?");
                sqlParams.add(keyId3);
            }
        }
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), sqlParams.toArray());
        ar.addCallbackArgument("count", ds.getBigDecimal(0, "c").toPlainString());
        ar.print();
    }
}

