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
import com.labvantage.sapphire.modules.storage.StorageUnitUtil;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class ValidateStorageRestrictions
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        List<String> errors;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String msg = "";
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid");
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid");
        if (StringUtil.getLen(trackitemid) > 0L && StringUtil.getLen(storageunitid) > 0L && (errors = StorageUnitUtil.validateStorageRestrictions(this.getQueryProcessor(), this.getDAMProcessor(), storageunitid, trackitemid, this.getConnectionProcessor().getSapphireConnection())) != null && errors.size() > 0) {
            msg = OpalUtil.toDelimitedString(errors, "<br><br>");
        }
        ajaxResponse.addCallbackArgument("msg", msg);
        ajaxResponse.addCallbackArgument("trackitemid", trackitemid);
        ajaxResponse.print();
    }
}

