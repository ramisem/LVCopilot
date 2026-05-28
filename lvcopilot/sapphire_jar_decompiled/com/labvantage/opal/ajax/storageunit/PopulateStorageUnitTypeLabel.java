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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class PopulateStorageUnitTypeLabel
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String storageunittype = ajaxResponse.getRequestParameter("storageunittype", "");
        PropertyList storageUnitTypeDef = StorageUnitTypeDef.getInstance().getTypeDefinition(this.getQueryProcessor(), storageunittype);
        ajaxResponse.addCallbackArgument("storageunittypelabel", storageUnitTypeDef.getProperty("storageunittypelabel", storageunittype));
        ajaxResponse.addCallbackArgument("elementid", ajaxResponse.getRequestParameter("elementid", ""));
        ajaxResponse.addCallbackArgument("rowindex", ajaxResponse.getRequestParameter("rowindex", ""));
        ajaxResponse.addCallbackArgument("columnindex", ajaxResponse.getRequestParameter("columnindex", ""));
        ajaxResponse.print();
    }
}

