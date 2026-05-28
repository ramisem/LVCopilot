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

public class UpRevisionSiteAndParticipants
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String siteid = ajaxResponse.getRequestParameter("siteid", "");
        String participantid = ajaxResponse.getRequestParameter("participantid", "");
        String revisionid = ajaxResponse.getRequestParameter("revisionid", "");
        String participantFilter = ajaxResponse.getRequestParameter("participantfilter", "");
        String auditReason = ajaxResponse.getRequestParameter("auditreason", "");
        String auditActivity = ajaxResponse.getRequestParameter("auditactivity", "");
        String auditSignedFlag = ajaxResponse.getRequestParameter("auditsignedflag", "");
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("siteid", siteid);
        actionProps.setProperty("participantid", participantid);
        actionProps.setProperty("clinicalprotocolrevision", revisionid);
        actionProps.setProperty("participantfilter", participantFilter);
        actionProps.setProperty("auditactivity", auditActivity);
        actionProps.setProperty("auditreason", auditReason);
        actionProps.setProperty("auditsignedflag", auditSignedFlag);
        String returnVal = "success";
        try {
            this.getActionProcessor().processAction("UpRevSiteNPartcpnt", "1", actionProps);
        }
        catch (ActionException e) {
            returnVal = "failure";
        }
        ajaxResponse.addCallbackArgument("message", returnVal);
        ajaxResponse.print();
    }
}

