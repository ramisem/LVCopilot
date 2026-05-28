/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefProperties;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class TaskDefPropsBtnsAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            PropertyList stepprops;
            boolean descendant = ajaxResponse.getRequestParameter("descendant", "N").equalsIgnoreCase("Y");
            try {
                stepprops = new PropertyList(new JSONObject(props));
            }
            catch (Exception e) {
                stepprops = null;
                ajaxResponse.setError("Could not create properties.");
            }
            PropertyList parentprops = null;
            if (descendant) {
                String parentpropsstring = ajaxResponse.getRequestParameter("parentprops", "");
                try {
                    parentprops = new PropertyList(new JSONObject(parentpropsstring));
                }
                catch (Exception e) {
                    parentprops = null;
                    ajaxResponse.setError("Could not create parent properties.");
                }
            }
            boolean viewOnly = ajaxResponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            if (pageContext == null) {
                ajaxResponse.setError("Could not create page context.");
            }
            String stepid = ajaxResponse.getRequestParameter("id1", "");
            String selectedBtn = ajaxResponse.getRequestParameter("selectedbutton", "");
            if (pageContext != null && stepprops != null) {
                StringBuffer script = new StringBuffer();
                ajaxResponse.addCallbackArgument("html", TaskDefProperties.getStepToolbarTab(stepid, selectedBtn, stepprops, parentprops, viewOnly, descendant, pageContext, this.getConnectionId(), script));
                ajaxResponse.addCallbackArgument("script", script.toString());
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

