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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Logger;

public class SetStateTitle
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "UpdateWebPageLogTitleHandler");
        String stateid = ajaxResponse.getRequestParameter("stateid", ajaxResponse.getRequestParameter("webpagelogid", ajaxResponse.getRequestParameter("historyid")));
        String title = ajaxResponse.getRequestParameter("title");
        if (stateid.length() > 0) {
            try {
                this.getQueryProcessor().execSQL(20030, new Object[]{title, stateid});
            }
            catch (Exception e) {
                Logger.logWarn("Failed to update title into webpagelog.");
            }
        }
        ajaxResponse.print();
    }
}

