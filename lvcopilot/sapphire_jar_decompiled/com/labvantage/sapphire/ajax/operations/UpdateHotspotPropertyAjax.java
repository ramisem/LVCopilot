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
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class UpdateHotspotPropertyAjax
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private PropertyList productvaluetree = null;
    private PropertyList valuetree = null;
    private PropertyList hotspotPosition = null;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "addToWorkFlowHandler");
        HashMap valueProps = new HashMap();
        valueProps.putAll(ajaxResponse.getRequestParameters());
        if (valueProps.containsKey("productvaluetree") && ((String)valueProps.get("productvaluetree")).length() > 0) {
            this.productvaluetree = new PropertyList();
            this.valuetree = new PropertyList();
            try {
                this.productvaluetree.setPropertyList(((String)valueProps.get("productvaluetree")).trim());
                this.valuetree.setPropertyList(((String)valueProps.get("valuetree")).trim());
                try {
                    this.hotspotPosition = new PropertyList(new JSONObject(valueProps.get("hotspotmap").toString()));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        }
    }
}

