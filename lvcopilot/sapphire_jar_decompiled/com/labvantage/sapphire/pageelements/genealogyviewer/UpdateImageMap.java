/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.genealogyviewer;

import com.labvantage.sapphire.pageelements.genealogyviewer.GenealogyViewer;
import com.labvantage.sapphire.pageelements.genealogyviewer.GenealogyViewerUtil;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class UpdateImageMap
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 72168 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String jsonStr = ajaxResponse.getRequestParameter("jsonStr");
        try {
            JSONObject jsonObject = new JSONObject(jsonStr);
            String rootBatchId = jsonObject.getString("rootBatchId");
            String parentLevel = jsonObject.getString("parentLevel");
            String childLevel = jsonObject.getString("childLevel");
            String batchSequence = jsonObject.getString("batchsequence");
            String webAppRoot = jsonObject.getString("webAppRoot");
            String parentColumn = jsonObject.getString("parentColumn");
            String childColumn = jsonObject.getString("childColumn");
            String dbtable = jsonObject.getString("dbtable");
            String displayValue = jsonObject.getString("displayValue");
            String hrefURL = jsonObject.getString("hrefURL");
            String returnToListUrl = jsonObject.getString("returnToListUrl");
            String maxParentCount = jsonObject.getString("maxParentCount");
            String maxChildCount = jsonObject.getString("maxChildCount");
            String nodeWidth = jsonObject.getString("nodeWidth");
            String nodeHeight = jsonObject.getString("nodeHeight");
            String parentSDC = jsonObject.getString("parentSDC");
            String parentTableId = jsonObject.getString("parentTableId");
            String linkCol = jsonObject.getString("linkCol");
            String colorCodeColumn = jsonObject.getString("colorCodeColumn");
            String nodeColors = jsonObject.getString("nodeColors");
            String currNodeHighlightFlag = jsonObject.getString("currentnodehighlightflag");
            String currNodeTextFlag = jsonObject.getString("currentnodetextflag");
            String currNodeText = jsonObject.getString("currentnodetext");
            String currNodeColor = jsonObject.getString("currentnodecolor");
            String legendTitle = jsonObject.getString("legendTitle");
            String legendText = jsonObject.getString("legendText");
            HashMap<String, String> elementLevel = new HashMap<String, String>();
            elementLevel.put(GenealogyViewer.PARENT_LEVEL, parentLevel);
            elementLevel.put(GenealogyViewer.CHILD_LEVEL, childLevel);
            PropertyList elementProperty = new PropertyList();
            elementProperty.setProperty("parentcolumn", parentColumn);
            elementProperty.setProperty("childcolumn", childColumn);
            elementProperty.setProperty("table", dbtable);
            elementProperty.setProperty("displayvalue", displayValue);
            elementProperty.setProperty("hrefURL", hrefURL);
            elementProperty.setProperty("returntolistpage", returnToListUrl);
            elementProperty.setProperty("maxparentcount", maxParentCount);
            elementProperty.setProperty("maxchildcount", maxChildCount);
            elementProperty.setProperty("nodewidth", nodeWidth);
            elementProperty.setProperty("nodeheight", nodeHeight);
            elementProperty.setProperty("parentsdc", parentSDC);
            elementProperty.setProperty("parenttable", parentTableId);
            elementProperty.setProperty("linkcolumn", linkCol);
            elementProperty.setProperty("basedoncolumn", colorCodeColumn);
            elementProperty.setProperty("nodecolor", nodeColors);
            elementProperty.setProperty("currentnodehighlightflag", currNodeHighlightFlag);
            elementProperty.setProperty("currentnodetextflag", currNodeTextFlag);
            elementProperty.setProperty("currentnodetext", currNodeText);
            elementProperty.setProperty("currentnodecolor", currNodeColor);
            elementProperty.setProperty("legendtitle", legendTitle);
            elementProperty.setProperty("legendtext", legendText);
            elementProperty.setProperty("heterogeneous", jsonObject.has("heterogeneous") ? jsonObject.getString("heterogeneous") : "N");
            elementProperty.setProperty("childsdccolumn", jsonObject.has("childsdccolumn") ? jsonObject.getString("childsdccolumn") : "");
            elementProperty.setProperty("parentsdccolumn", jsonObject.has("parentsdccolumn") ? jsonObject.getString("parentsdccolumn") : "");
            elementProperty.setProperty("fullview", jsonObject.has("fullview") ? jsonObject.getString("fullview") : "N");
            boolean heterogeneous = "Y".equals(elementProperty.getProperty("heterogeneous"));
            if (heterogeneous) {
                String additionalsdc = ajaxResponse.getRequestParameter("additionalsdc", "");
                PropertyListCollection pl = new PropertyListCollection();
                pl.setJSONString("[" + additionalsdc + "]");
                elementProperty.setProperty("additionalsdc", pl);
            }
            GenealogyViewerUtil model = new GenealogyViewerUtil(this.getQueryProcessor(), this.getDAMProcessor(), this.getSDCProcessor(), this.getTranslationProcessor(), this.getConnectionProcessor().isOra(), rootBatchId, elementLevel, batchSequence, elementProperty, webAppRoot);
            try {
                model.setParentLevel(Integer.parseInt(parentLevel));
                model.setChildLevel(Integer.parseInt(childLevel));
            }
            catch (NumberFormatException e) {
                model.setParentLevel(Integer.MAX_VALUE);
                model.setChildLevel(Integer.MAX_VALUE);
            }
            model.setLayout();
            boolean isFullView = model.isFullView();
            SoftReference<GenealogyViewerUtil> modelReference = new SoftReference<GenealogyViewerUtil>(model);
            String cacheKey = "genealogy_model_" + rootBatchId + "_" + this.getConnectionProcessor().getConnectionid();
            CacheUtil.put(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "BatchImageMap", cacheKey, modelReference);
            JSONArray jArray = new JSONArray();
            this.getImageMap(model, jArray);
            JSONObject isFullViewJsn = new JSONObject();
            try {
                isFullViewJsn.put("isFullView", isFullView);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            jArray.put(isFullViewJsn);
            ajaxResponse.addCallbackArgument("mymaps", jArray.toString());
            ajaxResponse.addCallbackArgument("sdcid", model.getRootBatchSDC());
            ajaxResponse.addCallbackArgument("detailpageurl", model.getDetailpageurl());
            ajaxResponse.print();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getImageMap(GenealogyViewerUtil model, JSONArray jArray) {
        StringBuilder map = new StringBuilder();
        map.append("<map name=\"batchmap\">\n");
        model.addMapArea(jArray);
        map.append("</map>\n");
        return map.toString();
    }
}

