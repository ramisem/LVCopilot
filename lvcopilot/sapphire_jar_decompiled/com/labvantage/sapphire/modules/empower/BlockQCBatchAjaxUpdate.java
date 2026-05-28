/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.modules.empower.EmpowerDownloadProcessor;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class BlockQCBatchAjaxUpdate
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "top.wizard_iframe.blockQCBatch_AjaxCallback");
        String project = ajaxResponse.getRequestParameter("projectname");
        String database = ajaxResponse.getRequestParameter("databasename");
        String qcbatchid = ajaxResponse.getRequestParameter("qcbatchid");
        String samplesetmethodname = ajaxResponse.getRequestParameter("samplesetmethodname");
        String mode = ajaxResponse.getRequestParameter("downloadmode");
        try {
            if (mode.equals("AQC Mode")) {
                EmpowerDownloadProcessor.blockQCBatch(this.getActionProcessor(), qcbatchid, project, database, samplesetmethodname);
            }
        }
        catch (SapphireException e) {
            ajaxResponse.setError("Failed to block the QCBatch " + qcbatchid);
        }
        ajaxResponse.print();
    }
}

