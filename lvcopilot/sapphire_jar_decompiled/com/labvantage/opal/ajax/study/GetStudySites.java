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

import com.labvantage.sapphire.admin.ddt.Study;
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
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;

public class GetStudySites
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studyid = ajaxResponse.getRequestParameter("studyid");
        JSONArray studySiteArray = new JSONArray();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_StudySite");
        sdiRequest.setRequestItem("primary[s_studysiteid,studysitedesc]");
        sdiRequest.setQueryFrom("s_studysite");
        sdiRequest.setQueryOrderBy("studysitedesc");
        sdiRequest.setQueryWhere("sstudyid = '" + SafeSQL.encodeForSQL(studyid, this.getConnectionProcessor().isOra()) + "'");
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        if (sdiData != null) {
            DataSet primary = sdiData.getDataset("primary");
            for (int i = 0; i < primary.size(); ++i) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("s_studysiteid", primary.getString(i, "s_studysiteid"));
                    jsonObject.put("studysitedesc", HttpUtil.htmlEncode(primary.getString(i, "studysitedesc")));
                    studySiteArray.put(jsonObject);
                    continue;
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        ajaxResponse.addCallbackArgument("sites", studySiteArray.toString());
        ajaxResponse.addCallbackArgument("protocoldriven", Study.isProtocolDriven(this.getQueryProcessor(), studyid));
        ajaxResponse.print();
    }
}

