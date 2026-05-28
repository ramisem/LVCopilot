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

import com.labvantage.opal.util.StorageUnitTypeDef;
import java.util.Map;
import java.util.TreeSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetStorageUnitTypes
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        JSONArray typeArray = new JSONArray();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        TreeSet<String> nodeSet = new TreeSet<String>();
        StorageUnitTypeDef storageUnitTypeDef = StorageUnitTypeDef.getInstance();
        Map<String, PropertyList> typeMap = storageUnitTypeDef.getTypeMap(this.getQueryProcessor());
        for (String node : typeMap.keySet()) {
            if (sdcid.length() <= 0 || !sdcid.equals(typeMap.get(node).getPropertyListNotNull("template").getProperty("sdcid"))) continue;
            typeArray.put(typeMap.get(node).toJSONObject());
            nodeSet.add(node);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<select id='cust_pr0_sutypeoptions' name='cust_pr0_sutypeoptions' class='mandatoryfield'>");
        for (String node : nodeSet) {
            sb.append("<option value='").append(node).append("'>").append(storageUnitTypeDef.getStorageUnitTypeLabel(this.getQueryProcessor(), node)).append("</option>");
        }
        sb.append("</select>");
        ajaxResponse.addCallbackArgument("array", typeArray.toString());
        ajaxResponse.addCallbackArgument("option", sb.toString());
        ajaxResponse.print();
    }
}

