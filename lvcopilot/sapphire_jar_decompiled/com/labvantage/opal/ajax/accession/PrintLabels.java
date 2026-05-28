/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import com.labvantage.sapphire.actions.label.GenerateLabel;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class PrintLabels
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "").trim();
        String labelmethodid = ajaxResponse.getRequestParameter("labelmethodid", "").trim();
        String labelmethodversionid = ajaxResponse.getRequestParameter("labelmethodversionid", "").trim();
        String printeraddressid = ajaxResponse.getRequestParameter("printeraddressid", "").trim();
        String numcopies = ajaxResponse.getRequestParameter("numcopies", "1").trim();
        if (sampleid.length() > 0 && labelmethodid.length() > 0 && labelmethodversionid.length() > 0 && printeraddressid.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("keyid1", StringUtil.replaceAll(sampleid, "%3B", ";"));
            props.setProperty("labelmethodid", labelmethodid);
            props.setProperty("labelmethodversionid", labelmethodversionid);
            props.setProperty("printeraddressid", printeraddressid);
            props.setProperty("printeraddresstype", "Device");
            props.setProperty("numcopies", numcopies);
            try {
                this.getActionProcessor().processActionClass(GenerateLabel.class.getName(), props);
            }
            catch (ActionException e) {
                message = this.getTranslationProcessor().translate("Error printing labels") + ": " + e.getMessage();
            }
        } else {
            message = this.getTranslationProcessor().translate("Error printing labels") + ": " + this.getTranslationProcessor().translate("Missing printing inputs.");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

