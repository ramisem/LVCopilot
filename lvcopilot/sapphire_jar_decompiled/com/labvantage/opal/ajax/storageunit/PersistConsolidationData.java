/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class PersistConsolidationData
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        String confirm;
        String message;
        block8: {
            message = "";
            confirm = "";
            ajaxResponse = new AjaxResponse(request, response);
            String data = ajaxResponse.getRequestParameter("data");
            if (OpalUtil.isNotEmpty(data)) {
                String auditreason = ajaxResponse.getRequestParameter("auditreason");
                try {
                    DataSet ds = new DataSet();
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String trackitemid = jsonObject.getString("trackitemid");
                        String targetid = jsonObject.getString("targetid");
                        if (!OpalUtil.isNotEmpty(trackitemid) || !OpalUtil.isNotEmpty(targetid)) continue;
                        int row = ds.addRow();
                        ds.setString(row, "trackitemid", trackitemid);
                        ds.setString(row, "currentstorageunitid", targetid);
                    }
                    if (ds.size() <= 0) break block8;
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "TrackItemSDC");
                    props.setProperty("keyid1", ds.getColumnValues("trackitemid", ";"));
                    props.setProperty("currentstorageunitid", ds.getColumnValues("currentstorageunitid", ";"));
                    if (OpalUtil.isNotEmpty(auditreason)) {
                        props.setProperty("auditreason", auditreason);
                    }
                    try {
                        props.setProperty("__sdcruleconfirm", ajaxResponse.getRequestParameter("confirm", "N"));
                        this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    }
                    catch (ActionException e) {
                        if ("CONFIRM".equalsIgnoreCase(e.getErrorType())) {
                            confirm = e.getMessage();
                            break block8;
                        }
                        message = this.getTranslationProcessor().translate("Unable to save data.") + "<hr>" + e.getMessage() + "<hr>" + this.getTranslationProcessor().translate("If problem persists, please contact your Administrator.");
                        this.logger.error("PersistConsolidationData", e);
                    }
                }
                catch (JSONException e) {
                    message = this.getTranslationProcessor().translate("Unable to save data.") + "<hr>" + e.getMessage() + "<hr>" + this.getTranslationProcessor().translate("If problem persists, please contact your Administrator.");
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("confirm", confirm);
        ajaxResponse.print();
    }
}

