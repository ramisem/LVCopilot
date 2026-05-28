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

import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPropertiesStepType;
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

public class TaskDefPropertiesStepTypeAjaxSaver
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            if (pageContext == null) {
                ajaxResponse.setError("Could not create page context.");
            }
            try {
                PropertyList stepprops = new PropertyList(new JSONObject(props));
                PropertyList out = TaskDefPropertiesStepType.getProperties(stepprops, ajaxResponse.getRequestParameter("stepid"), ajaxResponse.getRequestParameter("steptypeid"), ajaxResponse.getRequestParameter("steptypenode"), pageContext, this.logger);
                ajaxResponse.addCallbackArgument("steptypeoverrides", out.getPropertyList("steptypeoverrides").toJSONString(true));
                ajaxResponse.addCallbackArgument("steptypemerged", out.getPropertyList("steptypemerged").toJSONString(true));
                ajaxResponse.addCallbackArgument("refreshvariables", ajaxResponse.getRequestParameter("refreshvariables", "N"));
                ajaxResponse.addCallbackArgument("refresh", ajaxResponse.getRequestParameter("refresh", "N"));
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

