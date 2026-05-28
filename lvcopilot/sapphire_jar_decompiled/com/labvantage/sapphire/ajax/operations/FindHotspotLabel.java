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

public class FindHotspotLabel
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcId = ajaxResponse.getRequestParameter("sdcid");
        String searchString = ajaxResponse.getRequestParameter("searchstring");
        String props = ajaxResponse.getRequestParameter("props");
        boolean caseSensitive = ajaxResponse.getRequestParameter("casesensitive", "Y").equals("Y");
        SDCProcessor sdcp = this.getSDCProcessor();
        PropertyList sdcProps = sdcp.getProperties(sdcId);
        String locationLabel = "LOCATIONLABEL";
        String tableid = sdcProps.getProperty("tableid");
        String keycolid1 = sdcProps.getProperty("keycolid1");
        if (!caseSensitive) {
            searchString = searchString.toUpperCase();
        }
        QueryProcessor qp = this.getQueryProcessor();
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT distinct " + locationLabel + " FROM " + tableid + " WHERE " + (!caseSensitive ? "upper" : "") + " ( " + keycolid1 + " ) like (" + safeSQL.addVar(searchString + "%") + ")";
        DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        String label = ds.getColumnValues("LOCATIONLABEL", ";");
        ajaxResponse.addCallbackArgument("id", props);
        ajaxResponse.addCallbackArgument("KeyId1", searchString);
        ajaxResponse.addCallbackArgument("sdcId", sdcId);
        ajaxResponse.addCallbackArgument("props", label);
        ajaxResponse.print();
    }
}

