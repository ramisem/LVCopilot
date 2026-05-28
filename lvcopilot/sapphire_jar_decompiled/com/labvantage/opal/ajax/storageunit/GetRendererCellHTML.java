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

import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
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
import sapphire.xml.PropertyListCollection;

public class GetRendererCellHTML
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        DataSet dataset = new DataSet();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String mode = ajaxResponse.getRequestParameter("mode");
        String data = ajaxResponse.getRequestParameter("data");
        if ("multi".equals(mode)) {
            try {
                int i;
                JSONObject jsonObject = new JSONObject(data);
                String displayformat = jsonObject.getString("displayformat");
                String columns = jsonObject.getString("columns");
                PropertyListCollection displaycolumns = new PropertyListCollection();
                displaycolumns.setJSONString(columns);
                JSONArray cellArray = new JSONArray(jsonObject.getString("cells"));
                for (i = 0; i < cellArray.length(); ++i) {
                    JSONObject o = cellArray.getJSONObject(i);
                    int row = dataset.addRow();
                    dataset.setString(row, "storageunitid", o.getString("storageunitid"));
                    dataset.setString(row, "trackitemid", o.getString("trackitemid"));
                    dataset.setString(row, "index", o.getString("index"));
                }
                for (i = 0; i < dataset.size(); ++i) {
                    String storageunitid = dataset.getString(i, "storageunitid");
                    String trackitemid = dataset.getString(i, "trackitemid");
                    dataset.setString(i, "html", StorageUnitUtil.parseRendererDisplayData(displaycolumns, displayformat, storageunitid, trackitemid, this.getQueryProcessor(), this.getTranslationProcessor()));
                }
            }
            catch (JSONException e) {
                message = "Error: " + e.getMessage();
                e.printStackTrace();
            }
        } else if ("single".equals(mode)) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                String displayformat = jsonObject.getString("displayformat");
                String storageunitid = jsonObject.getString("storageunitid");
                String trackitemid = jsonObject.getString("trackitemid");
                String columns = jsonObject.getString("columns");
                PropertyListCollection displaycolumns = new PropertyListCollection();
                displaycolumns.setJSONString(columns);
                int row = dataset.addRow();
                dataset.setString(row, "index", jsonObject.getString("index"));
                dataset.setString(row, "html", StorageUnitUtil.parseRendererDisplayData(displaycolumns, displayformat, storageunitid, trackitemid, this.getQueryProcessor(), this.getTranslationProcessor()));
            }
            catch (JSONException e) {
                message = "Error: " + e.getMessage();
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("mode", mode);
        ajaxResponse.addCallbackArgument("data", dataset);
        ajaxResponse.addCallbackArgument("elementid", ajaxResponse.getRequestParameter("elementid"));
        ajaxResponse.print();
    }
}

