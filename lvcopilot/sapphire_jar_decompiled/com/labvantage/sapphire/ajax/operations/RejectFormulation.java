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
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class RejectFormulation
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
        String auditreason = ajaxResponse.getRequestParameter("auditreason", "");
        String auditactivity = ajaxResponse.getRequestParameter("auditactivity", "");
        String auditsignedflag = ajaxResponse.getRequestParameter("auditsignedflag", "");
        String buttonactivity = ajaxResponse.getRequestParameter("buttonactivity", "");
        String formulationstatus = ajaxResponse.getRequestParameter("formulationstatus", "Rejected");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Product");
        props.setProperty("keyid1", keyid1);
        props.setProperty("keyid2", keyid2);
        props.setProperty("formulationstatus", formulationstatus);
        props.setProperty("auditreason", auditreason);
        props.setProperty("auditactivity", auditactivity);
        props.setProperty("auditsignedflag", auditsignedflag);
        props.setProperty("buttonactivity", buttonactivity);
        try {
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
        catch (ActionException e) {
            this.logger.error("Failed to set scan", e.getMessage());
        }
        ajaxResponse.print();
    }
}

