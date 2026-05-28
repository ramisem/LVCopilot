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

import java.util.HashMap;
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

public class FindSDIs
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 87197 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcId = ajaxResponse.getRequestParameter("sdcid");
        String searchString = ajaxResponse.getRequestParameter("searchstring");
        String parentsdcid = ajaxResponse.getRequestParameter("parentsdcid");
        String props = ajaxResponse.getRequestParameter("props");
        boolean caseSensitive = ajaxResponse.getRequestParameter("casesensitive", "Y").equals("Y");
        SDCProcessor sdcp = this.getSDCProcessor();
        PropertyList sdcProps = sdcp.getProperties(sdcId);
        String tableid = sdcProps.getProperty("tableid");
        String keycolid1 = sdcProps.getProperty("keycolid1");
        if (!caseSensitive) {
            searchString = searchString.toUpperCase();
        }
        QueryProcessor qp = this.getQueryProcessor();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT distinct " + keycolid1 + ", locationlabel FROM " + tableid + " WHERE " + (!caseSensitive ? "upper" : "") + " ( " + keycolid1 + " ) like (" + safeSQL.addVar(searchString + "%") + ")");
        if (sdcId.equalsIgnoreCase("Location")) {
            sql.append(" and parentlocationid = " + safeSQL.addVar(parentsdcid));
        } else if (sdcId.equalsIgnoreCase("SamplePoint")) {
            sql.append(" and LOCATIONID = " + safeSQL.addVar(parentsdcid));
        }
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        HashMap<String, String> childItems = new HashMap<String, String>();
        if (ds != null && ds.size() > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                String k = ds.getString(i, keycolid1, "");
                String v = ds.getString(i, "locationlabel", "");
                childItems.put(k, v);
            }
        }
        String list = ds.getColumnValues("locationlabel", ";");
        ajaxResponse.addCallbackArgument("list", childItems);
        ajaxResponse.addCallbackArgument("props", props);
        ajaxResponse.print();
    }
}

