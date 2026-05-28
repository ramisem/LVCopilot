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
import java.util.HashMap;
import java.util.HashSet;
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

public class GetRendererCellSampleType
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        DataSet dataset = new DataSet();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String data = ajaxResponse.getRequestParameter("data");
        try {
            HashSet<String> trackitemSet = new HashSet<String>();
            JSONObject jsonObject = new JSONObject(data);
            JSONArray cellArray = new JSONArray(jsonObject.getString("cells"));
            for (int i = 0; i < cellArray.length(); ++i) {
                JSONObject o = cellArray.getJSONObject(i);
                String trackitemid = o.getString("trackitemid");
                if (!OpalUtil.isNotEmpty(trackitemid)) continue;
                trackitemSet.add(trackitemid);
                int row = dataset.addRow();
                dataset.setString(row, "trackitemid", trackitemid);
                dataset.setString(row, "cellid", o.getString("id"));
            }
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select trackitem.trackitemid, s_sample.sampletypeid from s_sample, trackitem where trackitem.linksdcid = 'Sample' and s_sample.s_sampleid = trackitem.linkkeyid1 and trackitem.trackitemid in (" + safeSQL.addIn(OpalUtil.toDelimitedString(trackitemSet, "','")) + ")", safeSQL.getValues());
            if (ds != null) {
                HashMap<String, String> filter = new HashMap<String, String>();
                for (int i = 0; i < dataset.size(); ++i) {
                    String trackitemid = dataset.getString(i, "trackitemid");
                    filter.put("trackitemid", trackitemid);
                    DataSet filteredDataSet = ds.getFilteredDataSet(filter);
                    if (filteredDataSet == null || filteredDataSet.size() <= 0) continue;
                    dataset.setString(i, "sampletypeid", filteredDataSet.getString(0, "sampletypeid", ""));
                }
            }
        }
        catch (JSONException e) {
            message = "Error: " + e.getMessage();
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("data", dataset);
        ajaxResponse.addCallbackArgument("elementid", ajaxResponse.getRequestParameter("elementid"));
        ajaxResponse.print();
    }
}

