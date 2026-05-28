/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.sample;

import com.labvantage.opal.util.DataEntryPageUtil;
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
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetSampleTypeDependentType
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        JSONObject jsonObject;
        String id;
        JSONObject jsonDropDown;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampletypeid = ajaxResponse.getRequestParameter("sampletypeid", "");
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        boolean isOracle = this.getConnectionProcessor().isOra();
        try {
            jsonDropDown = new JSONObject(ajaxResponse.getRequestParameter("dropdowndefinition"));
        }
        catch (JSONException e) {
            jsonDropDown = new JSONObject();
        }
        String querywhere = "";
        String queryorderby = "";
        String valuecolumn = "";
        String displaycolumn = "";
        JSONArray jsonArray_preptype = new JSONArray();
        JSONArray jsonArray_collectmethod = new JSONArray();
        JSONArray jsonArray_containertype = new JSONArray();
        SDIRequest sdiRequest = new SDIRequest();
        String name = "preptypeid";
        try {
            JSONObject dropdowndefinition = jsonDropDown.has(name) ? jsonDropDown.getJSONObject(name) : new JSONObject();
            querywhere = dropdowndefinition.has("querywhere") ? dropdowndefinition.getString("querywhere") : "";
            queryorderby = dropdowndefinition.has("queryorderby") ? dropdowndefinition.getString("queryorderby") : "";
            valuecolumn = dropdowndefinition.has("valuecolumn") ? dropdowndefinition.getString("valuecolumn") : "";
            displaycolumn = dropdowndefinition.has("displaycolumn") ? dropdowndefinition.getString("displaycolumn") : "";
        }
        catch (JSONException dropdowndefinition) {
            // empty catch block
        }
        valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "s_preptypeid";
        displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "preptypedesc";
        sdiRequest.setSDCid("LV_PrepType");
        sdiRequest.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
        sdiRequest.setQueryFrom("s_preptype");
        sdiRequest.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
        String whereclause = "s_preptypeid in (select t.s_preptypeid from s_preptypesampletypemap t where t.destsampletypeid = '" + SafeSQL.encodeForSQL(sampletypeid, isOracle) + "' and t.activeflag = 'Y')";
        if (StringUtil.getLen(querywhere) > 0L) {
            whereclause = whereclause + " and " + DataEntryPageUtil.parseDropDownWhereSubstitutions(querywhere, sysuserid);
        }
        sdiRequest.setQueryWhere(whereclause);
        DataSet ds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                id = ds.getString(i, valuecolumn);
                jsonObject = new JSONObject();
                try {
                    jsonObject.put("idvalue", id);
                    jsonObject.put("descvalue", ds.getString(i, displaycolumn, id));
                    jsonArray_preptype.put(jsonObject);
                    continue;
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        name = "collectmethodid";
        try {
            JSONObject dropdowndefinition = jsonDropDown.has(name) ? jsonDropDown.getJSONObject(name) : new JSONObject();
            querywhere = dropdowndefinition.has("querywhere") ? dropdowndefinition.getString("querywhere") : "";
            queryorderby = dropdowndefinition.has("queryorderby") ? dropdowndefinition.getString("queryorderby") : "";
            valuecolumn = dropdowndefinition.has("valuecolumn") ? dropdowndefinition.getString("valuecolumn") : "";
            displaycolumn = dropdowndefinition.has("displaycolumn") ? dropdowndefinition.getString("displaycolumn") : "";
        }
        catch (JSONException dropdowndefinition) {
            // empty catch block
        }
        valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "s_collectmethodid";
        displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "collectmethoddesc";
        sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_CollectMeth");
        sdiRequest.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
        sdiRequest.setQueryFrom("s_collectmethod");
        sdiRequest.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
        whereclause = "s_collectmethodid in (select t.s_collectmethodid from s_sampletypecollectmethodmap t where t.s_sampletypeid = '" + SafeSQL.encodeForSQL(sampletypeid, isOracle) + "' and t.activeflag = 'Y')";
        if (StringUtil.getLen(querywhere) > 0L) {
            whereclause = whereclause + " and " + DataEntryPageUtil.parseDropDownWhereSubstitutions(querywhere, sysuserid);
        }
        sdiRequest.setQueryWhere(whereclause);
        ds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                id = ds.getString(i, valuecolumn);
                jsonObject = new JSONObject();
                try {
                    jsonObject.put("idvalue", id);
                    jsonObject.put("descvalue", ds.getString(i, displaycolumn, id));
                    jsonArray_collectmethod.put(jsonObject);
                    continue;
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        name = "containertypeid";
        try {
            JSONObject dropdowndefinition = jsonDropDown.has(name) ? jsonDropDown.getJSONObject(name) : new JSONObject();
            querywhere = dropdowndefinition.has("querywhere") ? dropdowndefinition.getString("querywhere") : "";
            queryorderby = dropdowndefinition.has("queryorderby") ? dropdowndefinition.getString("queryorderby") : "";
            valuecolumn = dropdowndefinition.has("valuecolumn") ? dropdowndefinition.getString("valuecolumn") : "";
            displaycolumn = dropdowndefinition.has("displaycolumn") ? dropdowndefinition.getString("displaycolumn") : "";
        }
        catch (JSONException dropdowndefinition) {
            // empty catch block
        }
        valuecolumn = valuecolumn.length() > 0 ? valuecolumn : "containertypeid";
        displaycolumn = displaycolumn.length() > 0 ? displaycolumn : "containertypedesc";
        sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("ContainerType");
        sdiRequest.setRequestItem("primary[" + valuecolumn + "," + displaycolumn + "]");
        sdiRequest.setQueryFrom("containertype");
        sdiRequest.setQueryOrderBy(queryorderby.length() > 0 ? queryorderby : displaycolumn);
        whereclause = "containertypeid in (select t.containertypeid from s_sampletypecontainertype t where t.s_sampletypeid = '" + SafeSQL.encodeForSQL(sampletypeid, isOracle) + "' and t.activeflag = 'Y')";
        if (StringUtil.getLen(querywhere) > 0L) {
            whereclause = whereclause + " and " + DataEntryPageUtil.parseDropDownWhereSubstitutions(querywhere, sysuserid);
        }
        sdiRequest.setQueryWhere(whereclause);
        ds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                id = ds.getString(i, valuecolumn);
                jsonObject = new JSONObject();
                try {
                    jsonObject.put("idvalue", id);
                    jsonObject.put("descvalue", ds.getString(i, displaycolumn, id));
                    jsonArray_containertype.put(jsonObject);
                    continue;
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        ajaxResponse.addCallbackArgument("sampletypeid", sampletypeid);
        ajaxResponse.addCallbackArgument("rowindex", ajaxResponse.getRequestParameter("rowindex", ""));
        ajaxResponse.addCallbackArgument("preptypedata", jsonArray_preptype.toString());
        ajaxResponse.addCallbackArgument("collectmethoddata", jsonArray_collectmethod.toString());
        ajaxResponse.addCallbackArgument("containertypedata", jsonArray_containertype.toString());
        ajaxResponse.print();
    }
}

