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

import com.labvantage.sapphire.util.http.HttpUtil;
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

public class GetCohortEvents
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studysiteid = ajaxResponse.getRequestParameter("studysiteid", "").trim();
        String cohortid = ajaxResponse.getRequestParameter("cohortid", "").trim();
        JSONArray eventsArray = new JSONArray();
        StringBuilder sql = new StringBuilder();
        sql.append("select ed.s_eventdefid, ed.eventdeflabel, ed.eventdeftype, ed.visittype, coalesce( ed.allowmultipleflag, 'N') allowmultipleflag, ed.parenteventdefid");
        sql.append(" ,(select count(esd.S_SPECIMENDEFID) from S_EVENTDEFSTSPECIMENDEF esd where esd.S_EVENTDEFID = ed.S_EVENTDEFID) specimenrowcount");
        sql.append(" from s_eventdef ed, s_studysite ss, s_clinicalprotocol cp");
        sql.append(" where ss.s_studysiteid = ?");
        sql.append(" and ed.cohortid = ?");
        sql.append(" and ss.clinicalprotocolid = ed.clinicalprotocolid");
        sql.append(" and ss.clinicalprotocolrevision = ed.clinicalprotocolrevision");
        sql.append(" and ed.clinicalprotocolid = cp.s_clinicalprotocolid");
        sql.append(" and ed.clinicalprotocolrevision = cp.s_clinicalprotocolrevision");
        sql.append(" and ed.clinicalprotocolversionid = cp.s_clinicalprotocolversionid");
        sql.append(" and cp.versionstatus = 'C'");
        sql.append(" order by ed.usersequence");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{studysiteid, cohortid});
        if (ds != null && ds.size() > 0) {
            try {
                JSONObject jsonObject;
                String eventdeftype;
                int i;
                for (i = 0; i < ds.size(); ++i) {
                    eventdeftype = ds.getString(i, "eventdeftype");
                    if (!"Visit".equals(eventdeftype)) continue;
                    jsonObject = new JSONObject();
                    for (int col = 0; col < ds.getColumnCount(); ++col) {
                        String columnid = ds.getColumnId(col);
                        if ("eventdeflabel".equals(columnid)) {
                            jsonObject.put(columnid, HttpUtil.htmlEncode(ds.getValue(i, columnid, "")));
                            continue;
                        }
                        jsonObject.put(columnid, ds.getValue(i, columnid, ""));
                    }
                    eventsArray.put(jsonObject);
                }
                block4: for (i = 0; i < ds.size(); ++i) {
                    eventdeftype = ds.getString(i, "eventdeftype");
                    if (!"Timepoint".equals(eventdeftype)) continue;
                    jsonObject = new JSONObject();
                    for (int col = 0; col < ds.getColumnCount(); ++col) {
                        String columnid = ds.getColumnId(col);
                        if ("eventdeflabel".equals(columnid)) {
                            jsonObject.put(columnid, HttpUtil.htmlEncode(ds.getValue(i, columnid, "")));
                            continue;
                        }
                        jsonObject.put(columnid, ds.getValue(i, columnid, ""));
                    }
                    String parenteventdefid = ds.getString(i, "parenteventdefid", "");
                    for (int row = 0; row < eventsArray.length(); ++row) {
                        JSONObject object = eventsArray.getJSONObject(row);
                        if (!parenteventdefid.equals(object.getString("s_eventdefid"))) continue;
                        JSONArray timepoints = object.has("timepoints") ? object.getJSONArray("timepoints") : new JSONArray();
                        timepoints.put(jsonObject);
                        object.put("timepoints", timepoints);
                        continue block4;
                    }
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

