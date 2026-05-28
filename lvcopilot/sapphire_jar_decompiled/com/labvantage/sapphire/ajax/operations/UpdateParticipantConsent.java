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
import sapphire.xml.PropertyList;

public class UpdateParticipantConsent
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "top.SaveConsent_AjaxCallback");
        try {
            String participant = ajaxResponse.getRequestParameter("participantid");
            String answers = ajaxResponse.getRequestParameter("answers");
            String answerDt = ajaxResponse.getRequestParameter("answerdt");
            String questionid = ajaxResponse.getRequestParameter("questionid");
            PropertyList props = new PropertyList();
            props.setProperty("participantid", participant);
            props.setProperty("questionid", questionid);
            props.setProperty("answer", answers);
            props.setProperty("answerdt", answerDt);
            this.getActionProcessor().processAction("UpdateParticipantConsent", "1", props);
        }
        catch (Exception e) {
            ajaxResponse.setErrorCallback("SaveConsent_FailAjaxCallback");
            ajaxResponse.addCallbackArgument("msg", "Failed to update participant answers:" + e.getMessage());
            ajaxResponse.print();
            return;
        }
        ajaxResponse.print();
    }
}

