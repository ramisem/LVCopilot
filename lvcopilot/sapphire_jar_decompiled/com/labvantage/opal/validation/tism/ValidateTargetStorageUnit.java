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
import com.labvantage.sapphire.admin.ddt.TrackItemSDC;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class ValidateTargetStorageUnit
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 73611 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        StringBuffer sb = new StringBuffer();
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid");
        if (StringUtil.getLen(storageunitid.trim()) > 0L) {
            String userid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
            sb.append(TrackItemSDC.validateTargetStorageUnit(this.getQueryProcessor(), this.getTranslationProcessor(), storageunitid, userid, this.getDepartmentList(), true, false));
            if (sb.length() == 0) {
                TISMUtil.saveUserSelectedStorageUnit(storageunitid, true, this.getConnectionid());
            }
        }
        ajaxResponse.addCallbackArgument("message", sb.toString());
        ajaxResponse.addCallbackArgument("storageunitid", storageunitid);
        ajaxResponse.print();
    }
}

