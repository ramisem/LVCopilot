/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

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
import sapphire.util.SafeSQL;

public class GetEventSampleTypes
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String param_eventid = ajaxResponse.getRequestParameter("eventid");
        JSONObject jsonObject = new JSONObject();
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_eventdefid, s_sampletypeid from s_eventdefsampletype where s_eventdefid in (" + safeSQL.addIn(param_eventid, ";") + ") order by s_eventdefid, s_sampletypeid", safeSQL.getValues());
        ArrayList<DataSet> list = ds.getGroupedDataSets("s_eventdefid");
        for (DataSet dataset : list) {
            String eventid = dataset.getString(0, "s_eventdefid");
            JSONArray array = new JSONArray();
            for (int i = 0; i < dataset.size(); ++i) {
                array.put(dataset.getString(i, "s_sampletypeid"));
            }
            try {
                jsonObject.put(eventid, array);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("data", jsonObject);
        ajaxResponse.print();
    }
}

