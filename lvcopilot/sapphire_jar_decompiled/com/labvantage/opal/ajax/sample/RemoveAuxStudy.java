/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.sample;

import com.labvantage.sapphire.actions.sdi.DeleteSDIDetail;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class RemoveAuxStudy
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "").trim();
        String studyid = ajaxResponse.getRequestParameter("studyid", "").trim();
        if (sampleid.length() > 0 && studyid.length() > 0) {
            String[] samples;
            studyid = StringUtil.replaceAll(studyid, "%3B", ";");
            PropertyList props = new PropertyList();
            for (String sample : samples = StringUtil.split(sampleid, ";")) {
                try {
                    props.clear();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", sample);
                    props.setProperty("linkid", "Auxiliary Study");
                    props.setProperty("s_studyid", studyid);
                    this.getActionProcessor().processActionClass(DeleteSDIDetail.class.getName(), props);
                }
                catch (ActionException e) {
                    message = this.getTranslationProcessor().translate("Unable to remove Auxiliary Study from Sample") + "<hr>" + e.getMessage();
                    break;
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

