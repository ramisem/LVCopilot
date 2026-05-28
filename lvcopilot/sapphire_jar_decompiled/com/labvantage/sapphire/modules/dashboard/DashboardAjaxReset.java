/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class DashboardAjaxReset
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "DashboardHandler");
        try {
            try {
                String elementid;
                String ptreeid;
                String pagename = ajaxResponse.getRequestParameter("pagename");
                WebAdminProcessor wap = new WebAdminProcessor(this.getConnectionId());
                String productedition = wap.getDefaultPageEdition(pagename);
                try {
                    ptreeid = ajaxResponse.getRequestParameter("propertytreeid");
                    elementid = ajaxResponse.getRequestParameter("elementid");
                }
                catch (Exception e2) {
                    ptreeid = "";
                    elementid = "";
                }
                try {
                    if (ptreeid.length() == 0 || elementid.length() == 0) {
                        wap.clearUserOverrides(pagename, productedition);
                    } else {
                        wap.clearUserOverrides(pagename, productedition, ptreeid, elementid);
                    }
                }
                catch (Exception e3) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not clear user overrides."));
                }
            }
            catch (Exception e1) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain page name."));
            }
        }
        catch (Exception e) {
            ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain properties."));
        }
        ajaxResponse.print();
    }
}

