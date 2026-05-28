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
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class TestLAMGetCount
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            String batchid = ajaxResponse.getRequestParameter("batchid");
            int count = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM s_sample WHERE batchid=?", new Object[]{batchid});
            ajaxResponse.addCallbackArgument("samplecount", count);
            ajaxResponse.print();
        }
        catch (SapphireException e) {
            ajaxResponse.setError("Failed to load stats", e);
        }
    }
}

