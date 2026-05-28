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

import com.labvantage.sapphire.util.clinicalbb.BusinessRulesUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class ValidateProtocolRevision
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        String error;
        block7: {
            error = "";
            ajaxResponse = new AjaxResponse(request, response);
            String protocolIds = ajaxResponse.getRequestParameter("protocolIds");
            String revisions = ajaxResponse.getRequestParameter("revisions");
            try {
                String errorMsg = BusinessRulesUtil.checkIfSiteOrPartRefCPRev(revisions, protocolIds, this.getDAMProcessor(), this.getQueryProcessor());
                if (errorMsg.length() > 0) {
                    if (errorMsg.indexOf("site") != -1) {
                        error = this.getTranslationProcessor().translate("Protocol Revision cannot be deleted. \n There are Sites referencing to the selected Revision.");
                    } else if (errorMsg.indexOf("participant") != -1) {
                        error = this.getTranslationProcessor().translate("Protocol Revision cannot be deleted. \n There are Participants referencing to the selected Revision.");
                    }
                    break block7;
                }
                DataSet ds = BusinessRulesUtil.getVersionCountForSelRev(revisions, protocolIds, this.getDAMProcessor(), this.getQueryProcessor());
                String[] versionCount = ds.getColumnValues("versioncount", ";").split(";");
                error = "All associated records(cohorts, event definitions) will get deleted. \n Do you wish to continue?";
                for (int i = 0; i < versionCount.length; ++i) {
                    if (Integer.parseInt(versionCount[i]) <= 1) continue;
                    error = "Multiple Versions exist for the selected Revision. All versions will get deleted. \n Do you wish to continue?";
                    break;
                }
            }
            catch (SapphireException e) {
                throw new ServletException((Throwable)e);
            }
        }
        ajaxResponse.addCallbackArgument("respmsg", error);
        ajaxResponse.print();
    }
}

