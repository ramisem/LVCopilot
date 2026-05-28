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

public class SecurityCheck
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "sapphire.CallHandler");
        if (ajaxResponse != null) {
            try {
                String sdcid = ajaxResponse.getRequestParameter("sdcid");
                String operation = ajaxResponse.getRequestParameter("operation");
                String accesstype = ajaxResponse.getRequestParameter("accesstype");
                String securityMode = ajaxResponse.getRequestParameter("securitymode");
                String itemsKeyId1 = ajaxResponse.getRequestParameter("keyid1");
                String itemsKeyId2 = ajaxResponse.getRequestParameter("keyid2");
                String itemsKeyId3 = ajaxResponse.getRequestParameter("keyid3");
                PropertyList pl = new PropertyList();
                pl.setProperty("sdcid", sdcid);
                pl.setProperty("operation", operation);
                pl.setProperty("accesstype", accesstype);
                pl.setProperty("keyid1", itemsKeyId1);
                pl.setProperty("keyid2", itemsKeyId2);
                pl.setProperty("keyid3", itemsKeyId3);
                try {
                    if ("D".equalsIgnoreCase(securityMode) || "SDIWorkItem".equalsIgnoreCase(sdcid) || "B".equalsIgnoreCase(securityMode)) {
                        this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.ddt.DepartmentalSecurityCheck", pl);
                    } else if ("S".equalsIgnoreCase(securityMode)) {
                        this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.ddt.SDISecurityCheck", pl);
                    } else {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Unrecognized security type!"));
                    }
                    String outoperation = pl.getProperty("operation");
                    String failedsdis = pl.getProperty("failedsdis");
                    String passedsdis = pl.getProperty("passedsdis");
                    this.logDebug("outoperation = " + outoperation);
                    this.logDebug("failedsdis = " + failedsdis);
                    this.logDebug("passedsdis = " + passedsdis);
                    ajaxResponse.addCallbackArgument("operation", outoperation);
                    ajaxResponse.addCallbackArgument("failedsdis", failedsdis);
                    ajaxResponse.addCallbackArgument("passedsdis", passedsdis);
                    ajaxResponse.addCallbackArgument("securitymode", securityMode);
                    ajaxResponse.addCallbackArgument("sdcid", sdcid);
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

