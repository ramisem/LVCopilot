/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.study;

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

public class GetStudyEvents
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studyid = ajaxResponse.getRequestParameter("studyid");
        JSONArray eventsArray = new JSONArray();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_clinicaleventid, clinicaleventdesc from S_CLINICALEVENT where sstudyid = ? and (activeflag = 'Y' or activeflag is null) order by USERSEQUENCE", new Object[]{studyid});
        if (ds != null && ds.size() > 0) {
            try {
                for (int i = 0; i < ds.size(); ++i) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("s_clinicaleventid", ds.getString(i, "s_clinicaleventid", ""));
                    jsonObject.put("clinicaleventdesc", ds.getString(i, "clinicaleventdesc", ""));
                    eventsArray.put(jsonObject);
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("events", eventsArray.toString());
        ajaxResponse.print();
    }
}

