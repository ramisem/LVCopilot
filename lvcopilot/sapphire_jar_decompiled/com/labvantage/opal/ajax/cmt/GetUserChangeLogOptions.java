/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.cmt;

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class GetUserChangeLogOptions
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String changelogoptions = this.getConfigurationProcessor().getProfileProperty("changelogoptions");
        if (OpalUtil.isNotEmpty(changelogoptions)) {
            try {
                JSONObject jsonObject = new JSONObject(changelogoptions);
                String changerequestid = jsonObject.getString("changerequestid");
                String status = OpalUtil.getColumnValue(this.getQueryProcessor(), "changerequest", "changerequeststatus", "changerequestid = ?", new String[]{changerequestid});
                if ("Completed".equals(status) || "Cancelled".equals(status) || "Approved".equals(status)) {
                    jsonObject.put("changerequestid", "");
                    jsonObject.put("changerequestdesc", "");
                }
                changelogoptions = jsonObject.toString();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("options", changelogoptions);
        ajaxResponse.print();
    }
}

