/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.util.sdiapproval;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.util.sdiapproval.ApprovalUtil;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AjaxProcessor
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String strResponse = "";
        String ajaxCommand = request.getParameter("ajaxCommand");
        if (ajaxCommand != null && ajaxCommand.equalsIgnoreCase("GetApprovalSteps")) {
            String approvaltypeid = request.getParameter("approvaltypeid");
            if (approvaltypeid != null && approvaltypeid.length() > 0) {
                strResponse = ApprovalUtil.getApprovalsteps(approvaltypeid, this.getQueryProcessor());
            } else {
                strResponse = "Error : Approval Type Ids not specified in the ajax request.";
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

