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

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class ValidateAddChildStorage
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid", "");
        if (storageunitid.length() > 0) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunittype, propertytreeid from storageunit where storageunitid = ?", (Object[])new String[]{storageunitid});
            if (ds != null && ds.size() > 0) {
                String storageunittype = ds.getString(0, "storageunittype", "");
                String propertytreeid = ds.getString(0, "propertytreeid", "");
                PropertyList propertyList = StorageUnitUtil.getDefinition(new WebAdminProcessor(this.getConnectionid()), storageunittype, propertytreeid);
                if (propertyList.getCollectionNotNull("childrentypes").size() == 0) {
                    message = this.getTranslationProcessor().translate("Selected storage unit does not allow adding child storage units");
                }
            }
        } else {
            message = this.getTranslationProcessor().translate("Missing storage unit");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("storageunitid", storageunitid);
        ajaxResponse.print();
    }
}

