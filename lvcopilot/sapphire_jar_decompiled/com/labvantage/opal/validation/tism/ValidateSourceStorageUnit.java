/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.tism;

import com.labvantage.opal.pagetype.tism.TISMUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class ValidateSourceStorageUnit
extends BaseAjaxRequest {
    final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid");
        if (StringUtil.getLen(storageunitid.trim()) > 0L) {
            TISMUtil.saveUserSelectedStorageUnit(storageunitid, false, this.getConnectionid());
        }
        ajaxResponse.addCallbackArgument("message", "");
        ajaxResponse.addCallbackArgument("storageunitid", storageunitid);
        ajaxResponse.print();
    }
}

