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

public class AddQuestionsToParticipants
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "studymaint.AddQuestions_AjaxCallback");
        ajaxResponse.setErrorCallback("studymaint.AddQuestions_ErrorAjaxCallback");
        try {
            String studyid = ajaxResponse.getRequestParameter("studyid");
            String questionid = ajaxResponse.getRequestParameter("questionid");
            String questiontext = ajaxResponse.getRequestParameter("questiontext");
            PropertyList props = new PropertyList();
            props.setProperty("studyid", studyid);
            props.setProperty("questionid", questionid);
            props.setProperty("questiontext", questiontext);
            props.setProperty("answereditorstyleid", ajaxResponse.getRequestParameter("answereditorstyleid"));
            props.setProperty("answergranted", ajaxResponse.getRequestParameter("answergranted"));
            props.setProperty("consenttype", ajaxResponse.getRequestParameter("consenttype"));
            this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.clinicalbb.AddConsentQuestions", props);
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to load stats", e);
            ajaxResponse.print();
        }
        ajaxResponse.print();
    }
}

