/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.util.studysuite;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionProcessor;
import sapphire.util.ActionBlock;

public class AjaxProcessor
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String strResponse = "";
        String ajaxCommand = request.getParameter("ajaxCommand");
        HashMap<String, String> props = new HashMap<String, String>();
        ActionBlock ab = new ActionBlock();
        ActionProcessor ap = this.getActionProcessor();
        if (ajaxCommand != null && ajaxCommand.equalsIgnoreCase("CopyStudy")) {
            props.put("sdcid", request.getParameter("sdcid"));
            props.put("templateid", request.getParameter("templateid"));
            props.put("copies", request.getParameter("copies"));
            props.put("studysuiteid", request.getParameter("studysuiteid"));
            try {
                ab.setAction("copyStudy", "AddStudy", "1");
                ab.setActionProperties("copyStudy", props);
                ap.processActionBlock(ab);
            }
            catch (Exception e) {
                strResponse = "Error : could not copy the selected Study.  Error from AjaxProcessor: " + this.getClass().getName();
                if (Trace.on) {
                    Trace.logDebug(strResponse);
                }
            }
        } else {
            strResponse = "Error : Unrecognized ajax command!";
            if (Trace.on) {
                Trace.logDebug(strResponse);
            }
        }
        try {
            PrintWriter out = response.getWriter();
            out.write(strResponse);
        }
        catch (IOException e) {
            this.logger.stackTrace(e);
        }
    }
}

