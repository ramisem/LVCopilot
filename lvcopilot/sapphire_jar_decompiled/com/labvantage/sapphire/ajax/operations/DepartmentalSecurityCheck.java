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

public class DepartmentalSecurityCheck
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "sapphire.connection.dsCallHandler");
        if (ajaxResponse != null) {
            try {
                String sdcid = ajaxResponse.getRequestParameter("sdcid");
                String operation = ajaxResponse.getRequestParameter("operation");
                String accesstype = ajaxResponse.getRequestParameter("accesstype");
                String items = ajaxResponse.getRequestParameter("keyid1");
                PropertyList pl = new PropertyList();
                pl.setProperty("sdcid", sdcid);
                pl.setProperty("operation", operation);
                pl.setProperty("accesstype", accesstype);
                pl.setProperty("keyid1", items);
                try {
                    this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.ddt.DepartmentalSecurityCheck", pl);
                    String outoperation = pl.getProperty("operation");
                    String failedsdis = pl.getProperty("failedsdis");
                    String passedsdis = pl.getProperty("passedsdis");
                    this.logDebug("outoperation = " + outoperation);
                    this.logDebug("failedsdis = " + failedsdis);
                    this.logDebug("passedsdis = " + passedsdis);
                    ajaxResponse.addCallbackArgument("operation", outoperation);
                    ajaxResponse.addCallbackArgument("failedsdis", failedsdis);
                    ajaxResponse.addCallbackArgument("passedsdis", passedsdis);
                }
                catch (Exception e) {
                    ajaxResponse.setError(e.getMessage());
                }
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not create JSON Object."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No Properties provided."));
        }
        ajaxResponse.print();
    }
}

