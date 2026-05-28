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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class ValidateSDI
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54069 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcId = ajaxResponse.getRequestParameter("sdcid");
        String keyId1 = ajaxResponse.getRequestParameter("keyid1");
        String keyId2 = ajaxResponse.getRequestParameter("keyid2");
        String keyId3 = ajaxResponse.getRequestParameter("keyid3");
        PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcId);
        int noOfKeys = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT 1 FROM ";
        if (sdcId.length() > 0) {
            sql = sql + sdcProps.getProperty("tableid") + " WHERE " + sdcProps.getProperty("keycolid1") + " = " + safeSQL.addVar(keyId1);
            if (noOfKeys > 1 && keyId2.length() > 0) {
                sql = sql + " AND " + sdcProps.getProperty("keycolid2") + " = " + safeSQL.addVar(keyId2);
            }
            if (noOfKeys > 2 && keyId3.length() > 0) {
                sql = sql + " AND " + sdcProps.getProperty("keycolid3") + " = " + safeSQL.addVar(keyId3);
            }
        } else {
            ajaxResponse.setError("SDCId/ Keyid1 not supplied properly.");
        }
        ajaxResponse.addCallbackArgument("exists", (ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues())).getRowCount() > 0);
        ajaxResponse.print();
    }
}

