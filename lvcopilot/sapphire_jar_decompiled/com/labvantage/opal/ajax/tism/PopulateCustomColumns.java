/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.tism;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class PopulateCustomColumns
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        DataSet ds = null;
        ArrayList<String> dsColumns = new ArrayList<String>();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        try {
            int i;
            ArrayList<String> columnList = new ArrayList<String>();
            JSONArray columnsArray = new JSONArray(ajaxResponse.getRequestParameter("columns"));
            for (i = 0; i < columnsArray.length(); ++i) {
                JSONObject column = columnsArray.getJSONObject(i);
                String columnid = column.getString("id");
                if (!OpalUtil.isNotEmpty(columnid)) continue;
                columnList.add(columnid);
            }
            ds = this.getQueryProcessor().getPreparedSqlDataSet("select " + OpalUtil.toDelimitedString(columnList, ", ") + " from trackitem where trackitemid = ?", (Object[])new String[]{trackitemid});
            if (ds != null && ds.size() > 0) {
                for (i = 0; i < ds.getColumnCount(); ++i) {
                    dsColumns.add(ds.getColumnId(i));
                }
            }
        }
        catch (JSONException e) {
            message = "Error happened while populating custom column values: " + e.getMessage();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("frame", ajaxResponse.getRequestParameter("frame", ""));
        ajaxResponse.addCallbackArgument("trackitemid", trackitemid);
        ajaxResponse.addCallbackArgument("columns", OpalUtil.toDelimitedString(dsColumns, ";"));
        ajaxResponse.addCallbackArgument("dataset", ds == null ? new DataSet() : ds);
        ajaxResponse.print();
    }
}

