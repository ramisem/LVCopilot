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
import sapphire.accessor.ActionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class TestLAM
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            int count = Integer.parseInt(ajaxResponse.getRequestParameter("count"));
            PropertyList addBatch = new PropertyList();
            addBatch.setProperty("sdcid", "Batch");
            addBatch.setProperty("copies", "1");
            ActionProcessor ap = this.getActionProcessor();
            ap.processAction("AddSDI", "1", addBatch);
            String batchid = addBatch.getProperty("newkeyid1");
            PropertyList addSample = new PropertyList();
            addSample.setProperty("sdcid", "Sample");
            addSample.setProperty("templateid", "Precious Metals");
            addSample.setProperty("batchid", batchid);
            for (int i = 0; i < count; ++i) {
                ap.processAction("AddSDI", "1", addSample, false, true);
            }
            ajaxResponse.addCallbackArgument("batchid", batchid);
            ajaxResponse.print();
        }
        catch (SapphireException e) {
            ajaxResponse.setError("Failed to load stats", e);
        }
    }
}

