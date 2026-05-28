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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class LiteQCBatchAjaxDelete
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "blockQCBatch_AjaxCallback");
        String qcbatchid = ajaxResponse.getRequestParameter("qcbatchid");
        try {
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("sdcid", "QCBatch");
            deleteProps.setProperty("keyid1", qcbatchid);
            this.getActionProcessor().processAction("DeleteSDI", "1", deleteProps);
        }
        catch (SapphireException e) {
            ajaxResponse.setError("Failed to delete the QCBatch " + qcbatchid);
        }
        ajaxResponse.print();
    }
}

