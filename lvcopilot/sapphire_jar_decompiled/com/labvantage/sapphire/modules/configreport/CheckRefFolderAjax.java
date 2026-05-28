/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.configreport;

import com.labvantage.sapphire.Trace;
import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class CheckRefFolderAjax
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "checkRefFolder");
        String refreportfolder = ajaxResponse.getRequestParameter("refreportfolder");
        try {
            String configfile = refreportfolder + File.separator + "xmlreport" + File.separator + "config.xml";
            File f = new File(configfile);
            ajaxResponse.addCallbackArgument("exists", f.exists());
        }
        catch (Exception e) {
            Trace.logError("Failed to check folder existence", e);
            ajaxResponse.setError("Failed to process request. Reason: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

