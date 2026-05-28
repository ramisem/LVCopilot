/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.scheduleplan.ajax;

import com.labvantage.sapphire.pageelements.scheduleplan.PlanItemHelper;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetDataView
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String dataViewHtml;
        PlanItemHelper planItemHelper;
        PropertyList element;
        AjaxResponse ar = new AjaxResponse(request, response);
        String primarySDCId = ar.getRequestParameter("sdcid");
        String primaryKeyId1 = ar.getRequestParameter("keyid1");
        String primaryKeyId2 = ar.getRequestParameter("keyid2");
        String primaryKeyId3 = ar.getRequestParameter("keyid3");
        String elementJsonString = HttpUtil.decodeURIComponent(ar.getRequestParameter("element"));
        String elementId = ar.getRequestParameter("elementid");
        String selectedNodesJsonString = ar.getRequestParameter("selectednodes");
        String oldRSetId = ar.getRequestParameter("rsetid");
        if (primarySDCId.isEmpty()) {
            throw new ServletException("Primary SDC ID is empty");
        }
        if (primaryKeyId1.isEmpty()) {
            throw new ServletException("Primary key ID1 is empty");
        }
        if (elementJsonString.isEmpty()) {
            throw new ServletException("Element JSON is empty");
        }
        if (elementId.isEmpty()) {
            throw new ServletException("Element ID is empty");
        }
        try {
            element = new PropertyList(new JSONObject(elementJsonString));
            element.setId(elementId);
        }
        catch (JSONException e) {
            throw new ServletException("Element JSON is not valid: " + elementJsonString, (Throwable)e);
        }
        try {
            planItemHelper = new PlanItemHelper(this.getConnectionId(), primarySDCId, primaryKeyId1, primaryKeyId2, primaryKeyId3, element, elementId + "ds");
        }
        catch (SapphireException e) {
            throw new ServletException((Throwable)e);
        }
        PageContext pageContext = ar.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        pageContext.setAttribute("jsincluded", (Object)"Y");
        PropertyList selectedNodes = planItemHelper.getSelectedNodes(selectedNodesJsonString);
        this.setDefaultSource(selectedNodes, element);
        try {
            dataViewHtml = planItemHelper.getDataViewHtml(elementId, element, pageContext, selectedNodes);
        }
        catch (SapphireException e) {
            throw new ServletException("Cannot get data view HTML", (Throwable)e);
        }
        this.getDAMProcessor().clearRSet(oldRSetId);
        String rsetId = planItemHelper.getRsetId();
        ar.addCallbackArgument("dataviewhtml", dataViewHtml);
        ar.addCallbackArgument("rsetid", rsetId);
        ar.print();
    }

    private void setDefaultSource(PropertyList selectedNodes, PropertyList element) {
        PropertyListCollection nodeCollection = selectedNodes.getCollectionNotNull("nodecollection");
        if (nodeCollection.size() == 1) {
            PropertyList nodeProps = nodeCollection.getPropertyList(0);
            PropertyList data = nodeProps.getPropertyListNotNull("data");
            String refSdcId = data.getProperty("refsdcid");
            String refKeyId1 = data.getProperty("refkeyid1");
            String refKeyId2 = data.getProperty("refkeyid2");
            String refKeyId3 = data.getProperty("refkeyid3");
            if (!refSdcId.isEmpty()) {
                PropertyListCollection columns = element.getCollectionNotNull("columns");
                for (int i = 0; i < columns.size(); ++i) {
                    PropertyList columnProps = columns.getPropertyList(i);
                    String columnId = columnProps.getProperty("columnid");
                    String mode = columnProps.getProperty("mode");
                    if (columnId.equals("linksdcid")) {
                        columnProps.setProperty("defaultvalue", refSdcId);
                        if (mode.equals("hidden")) continue;
                        columnProps.setProperty("mode", "readonly");
                        continue;
                    }
                    if (columnId.equals("linkkeyid1")) {
                        columnProps.setProperty("defaultvalue", refKeyId1);
                        if (mode.equals("hidden")) continue;
                        columnProps.setProperty("mode", "readonly");
                        continue;
                    }
                    if (columnId.equals("linkkeyid2")) {
                        columnProps.setProperty("defaultvalue", refKeyId2);
                        if (mode.equals("hidden")) continue;
                        columnProps.setProperty("mode", "readonly");
                        continue;
                    }
                    if (!columnId.equals("linkkeyid3")) continue;
                    columnProps.setProperty("defaultvalue", refKeyId3);
                    if (mode.equals("hidden")) continue;
                    columnProps.setProperty("mode", "readonly");
                }
            }
        }
    }
}

