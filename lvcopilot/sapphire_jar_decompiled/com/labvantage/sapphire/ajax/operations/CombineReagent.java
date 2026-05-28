/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CombineReagent
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        String jsonstr = ajaxResponse.getRequestParameter("jsonstr");
        String reagenttypeid = ajaxResponse.getRequestParameter("reagenttypeid");
        String reagenttypeversionid = ajaxResponse.getRequestParameter("reagenttypeversionid");
        String totalusedamount = ajaxResponse.getRequestParameter("totalusedamount");
        String totalusedamountunit = ajaxResponse.getRequestParameter("totalusedamountunit");
        String totalusedamountunittype = ajaxResponse.getRequestParameter("totalusedamountunittype");
        String paramcount = ajaxResponse.getRequestParameter("paramcount");
        String foundmanagedcontainer = ajaxResponse.getRequestParameter("foundmanagedcontainer", "Y");
        String virtuallot = "";
        String virtualtrackitem = "";
        try {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_ReagentLot");
            props.setProperty("reagenttypeid", reagenttypeid);
            props.setProperty("reagenttypeversionid", reagenttypeversionid);
            props.setProperty("amountinitial", totalusedamount);
            props.setProperty("amountinitialunits", totalusedamountunit);
            props.setProperty("amountinitialunitstype", totalusedamountunittype);
            props.setProperty("containersinitial", "1");
            props.setProperty("reagentstatus", "Active");
            props.setProperty("contentflag", "V");
            props.setProperty("managecontainerinventoryflag", foundmanagedcontainer);
            props.setProperty("reagentlotdesc", "This is a Virtual Lot");
            this.getActionProcessor().processAction("AddSDI", "1", props);
            virtuallot = (String)props.get("newkeyid1");
            virtualtrackitem = this.getTrackItem(qp, virtuallot);
            this.addReagentLotRecipe(jsonstr, virtuallot, reagenttypeid, reagenttypeversionid, connectionInfo);
            if (paramcount != null && paramcount.length() > 0) {
                CombineReagent.updateParamValue(virtuallot, jsonstr, qp, this.getActionProcessor(), false);
            } else {
                CombineReagent.updateParamValue(virtuallot, jsonstr, qp, this.getActionProcessor(), true);
            }
            ajaxResponse.addCallbackArgument("errormsg", "");
            ajaxResponse.addCallbackArgument("virtuallot", virtuallot);
            ajaxResponse.addCallbackArgument("virtualtrackitem", virtualtrackitem);
            ajaxResponse.addCallbackArgument("virtualamount", totalusedamount);
            ajaxResponse.addCallbackArgument("virtualamountunit", totalusedamountunit);
            ajaxResponse.addCallbackArgument("virtualamountunittype", totalusedamountunittype);
            ajaxResponse.addCallbackArgument("reagenttypeid", reagenttypeid);
            ajaxResponse.addCallbackArgument("reagenttypeversionid", reagenttypeversionid);
            ajaxResponse.addCallbackArgument("managecontainerinventoryflag", foundmanagedcontainer);
            ajaxResponse.print();
        }
        catch (Exception e) {
            ajaxResponse.addCallbackArgument("errormsg", "Failed to create Virtual Lot. Exception: " + e.getMessage());
            ajaxResponse.print();
        }
    }

    private static void updateParamValue(String virtulaLot, String jsonstr, QueryProcessor qp, ActionProcessor ap, boolean copyfromParent) throws SapphireException {
        try {
            JSONObject jsonObject;
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer rlDataSetSQL = new StringBuffer();
            rlDataSetSQL.append("SELECT sdidata.keyid1, paramid, paramtype, replicateid, sdidata.paramlistid,  ");
            rlDataSetSQL.append(" sdidata.paramlistversionid, sdidata.variantid, sdidata.dataset FROM sdidata, sdidataitem ");
            rlDataSetSQL.append(" WHERE sdidata.sdcid='LV_ReagentLot' ");
            rlDataSetSQL.append(" AND sdidata.keyid1=" + safeSQL.addVar(virtulaLot));
            rlDataSetSQL.append(" AND sdidataitem.paramlistid=sdidata.paramlistid ");
            rlDataSetSQL.append(" AND sdidataitem.sdcid='LV_ReagentLot' ");
            rlDataSetSQL.append(" AND sdidataitem.paramlistversionid = sdidata.paramlistversionid ");
            rlDataSetSQL.append(" AND sdidataitem.variantid = sdidata.variantid ");
            rlDataSetSQL.append(" AND sdidataitem.keyid1=sdidata.keyid1");
            DataSet reagentLotDSItems = qp.getPreparedSqlDataSet(rlDataSetSQL.toString(), safeSQL.getValues());
            if (reagentLotDSItems == null || reagentLotDSItems.getRowCount() == 0) {
                return;
            }
            StringBuffer paramlistid = new StringBuffer();
            StringBuffer paramlistversionid = new StringBuffer();
            StringBuffer variantid = new StringBuffer();
            StringBuffer dataset = new StringBuffer();
            StringBuffer paramid = new StringBuffer();
            StringBuffer paramtype = new StringBuffer();
            StringBuffer replicateid = new StringBuffer();
            StringBuffer enteredtext = new StringBuffer();
            reagentLotDSItems.addColumn("enteredtext", 0);
            reagentLotDSItems.addColumn("enteredunits", 0);
            reagentLotDSItems.addColumn("currdataset", 1);
            boolean changed = false;
            HashMap<String, String> filterMap = new HashMap<String, String>();
            if (copyfromParent) {
                jsonObject = new JSONObject(jsonstr);
                JSONArray recipe = (JSONArray)jsonObject.get("recipe");
                if (recipe.length() > 0) {
                    JSONObject hm = (JSONObject)recipe.get(0);
                    String reagentLotId = (String)hm.get("reagentlot");
                    safeSQL.reset();
                    StringBuffer sql = new StringBuffer("select keyid1, paramid,paramtype,enteredvalue,enteredtext,enteredunits from sdidataitem");
                    sql.append(" where keyid1=" + safeSQL.addVar(reagentLotId));
                    sql.append(" and sdcid='LV_ReagentLot'");
                    DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    if (ds != null && ds.size() > 0) {
                        for (int i = 0; i < ds.size(); ++i) {
                            String id = ds.getString(i, "paramid", "");
                            String type = ds.getString(i, "paramtype", "");
                            String valueunit = ds.getString(i, "enteredtext", "");
                            filterMap.clear();
                            filterMap.put("paramid", id);
                            filterMap.put("paramtype", type);
                            int rlRow = reagentLotDSItems.findRow(filterMap);
                            if (rlRow < 0) continue;
                            paramlistid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramlistid"));
                            paramlistversionid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramlistversionid"));
                            variantid.append(";").append(reagentLotDSItems.getValue(rlRow, "variantid"));
                            dataset.append(";").append(reagentLotDSItems.getValue(rlRow, "dataset"));
                            paramid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramid"));
                            paramtype.append(";").append(reagentLotDSItems.getValue(rlRow, "paramtype"));
                            replicateid.append(";").append(reagentLotDSItems.getValue(rlRow, "replicateid"));
                            enteredtext.append(";").append(valueunit);
                            changed = true;
                        }
                    }
                }
            } else {
                jsonObject = new JSONObject(jsonstr);
                JSONArray params = (JSONArray)jsonObject.get("params");
                for (int i = 0; i < params.length(); ++i) {
                    JSONObject hm = (JSONObject)params.get(i);
                    String id = (String)hm.get("paramid");
                    String type = (String)hm.get("paramtype");
                    String value = (String)hm.get("value");
                    String unit = (String)hm.get("unit");
                    filterMap.clear();
                    filterMap.put("paramid", id);
                    filterMap.put("paramtype", type);
                    int rlRow = reagentLotDSItems.findRow(filterMap);
                    if (rlRow < 0) continue;
                    paramlistid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramlistid"));
                    paramlistversionid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramlistversionid"));
                    variantid.append(";").append(reagentLotDSItems.getValue(rlRow, "variantid"));
                    dataset.append(";").append(reagentLotDSItems.getValue(rlRow, "dataset"));
                    paramid.append(";").append(reagentLotDSItems.getValue(rlRow, "paramid"));
                    paramtype.append(";").append(reagentLotDSItems.getValue(rlRow, "paramtype"));
                    replicateid.append(";").append(reagentLotDSItems.getValue(rlRow, "replicateid"));
                    enteredtext.append(";").append(value + unit);
                    changed = true;
                }
            }
            if (changed) {
                HashMap<String, String> actionProps = new HashMap<String, String>();
                actionProps.put("sdcid", "LV_ReagentLot");
                actionProps.put("keyid1", virtulaLot);
                actionProps.put("paramlistid", paramlistid.substring(1));
                actionProps.put("paramlistversionid", paramlistversionid.substring(1));
                actionProps.put("variantid", variantid.substring(1));
                actionProps.put("dataset", dataset.substring(1));
                actionProps.put("paramid", paramid.substring(1));
                actionProps.put("paramtype", paramtype.substring(1));
                actionProps.put("replicateid", replicateid.substring(1));
                actionProps.put("enteredtext", enteredtext.substring(1));
                actionProps.put("applylock", "Y");
                ap.processAction("EnterDataItem", "1", actionProps);
            }
        }
        catch (Exception e) {
            throw new SapphireException("DB_ACTION_FAILED", "Not able to update Consumable Params Value", e);
        }
    }

    private void addReagentLotRecipe(String jsonstr, String reagentLotid, String reagentTypeid, String reagentTypeVersionid, ConnectionInfo connectionInfo) throws SapphireException {
        String recipeitemtype = "Reagent";
        char decimalSeparator = FormatUtil.getInstance(connectionInfo).getDecimalSeparator();
        try {
            JSONObject jsonObject = new JSONObject(jsonstr);
            JSONArray recipe = (JSONArray)jsonObject.get("recipe");
            for (int i = 0; i < recipe.length(); ++i) {
                String reagenttyperecipeitemid = "R-" + (i + 1);
                JSONObject hm = (JSONObject)recipe.get(i);
                String trackitemreagentlotid = (String)hm.get("reagentlot");
                String trackitemid = (String)hm.get("trackitem");
                String usedamount = (String)hm.get("usedamount");
                String usedamountunit = (String)hm.get("usedamountunit");
                String usedamountunittype = (String)hm.get("usedamountunittype");
                PropertyList reagentRecipeActionProps = new PropertyList();
                reagentRecipeActionProps.setProperty("sdcid", "LV_ReagentLot");
                reagentRecipeActionProps.setProperty("linkid", "ReagentLotRecipe");
                reagentRecipeActionProps.setProperty("keyid1", reagentLotid);
                reagentRecipeActionProps.setProperty("reagentlotrecipeitemid", reagenttyperecipeitemid);
                reagentRecipeActionProps.setProperty("recipeitemtype", recipeitemtype);
                reagentRecipeActionProps.setProperty("includereagenttypeid", reagentTypeid);
                reagentRecipeActionProps.setProperty("includereagenttypeversionid", reagentTypeVersionid);
                reagentRecipeActionProps.setProperty("trackitemid", trackitemid);
                reagentRecipeActionProps.setProperty("trackitemreagentlotid", trackitemreagentlotid);
                reagentRecipeActionProps.setProperty("amount", usedamount.replace('.', decimalSeparator));
                reagentRecipeActionProps.setProperty("amounttext", usedamount.replace('.', decimalSeparator));
                reagentRecipeActionProps.setProperty("amountunits", usedamountunit);
                reagentRecipeActionProps.setProperty("amountunitstype", usedamountunittype);
                reagentRecipeActionProps.setProperty("usersequence", String.valueOf(i + 1));
                this.getActionProcessor().processAction("AddSDIDetail", "1", reagentRecipeActionProps);
            }
        }
        catch (Exception e) {
            throw new SapphireException("DB_ACTION_FAILED", "Not able to Add Consumable Recipe", e);
        }
    }

    private String getTrackItem(QueryProcessor qp, String virtuallot) {
        String virtualtrackitem = "";
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("select trackitemid from trackitem ");
        sql.append(" WHERE linkkeyid1=" + safeSQL.addVar(virtuallot));
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (ds != null && ds.size() > 0) {
            virtualtrackitem = ds.getString(0, "trackitemid");
        }
        return virtualtrackitem;
    }

    private int getScale(String amountTextStr) {
        String[] amountTextStrArr = StringUtil.split(amountTextStr, ".");
        if (amountTextStrArr.length > 1) {
            return amountTextStrArr[1].length();
        }
        return 0;
    }

    private String removeLastZeros(String amountTextStr, int actaulScale) {
        String temp = amountTextStr;
        int scale = this.getScale(amountTextStr);
        if (scale > actaulScale) {
            for (int i = amountTextStr.length(); i > 1; --i) {
                if (amountTextStr.charAt(i - 1) != '0') {
                    if (amountTextStr.charAt(i - 1) != '.') break;
                    temp = temp.substring(0, temp.length() - 1);
                    break;
                }
                temp = temp.substring(0, temp.length() - 1);
                if (scale == ++actaulScale) break;
            }
        }
        return temp;
    }
}

