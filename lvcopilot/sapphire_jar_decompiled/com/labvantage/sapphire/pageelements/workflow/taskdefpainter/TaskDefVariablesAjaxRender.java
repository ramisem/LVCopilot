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
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefVariables;
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

public class TaskDefVariablesAjaxRender
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        StringBuffer html = new StringBuffer();
        PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        if (pageContext == null) {
            ajaxResponse.setError("Could not create page context.");
        }
        boolean viewOnly = ajaxResponse.getRequestParameter("viewonly", "N").equalsIgnoreCase("Y");
        boolean descendant = ajaxResponse.getRequestParameter("descendant", "N").equalsIgnoreCase("Y");
        boolean changed = false;
        PropertyList propslist = null;
        if (props.length() > 0) {
            try {
                String area;
                propslist = new PropertyList(new JSONObject(props));
                if (ajaxResponse.getRequestParameter("usage", "N").equalsIgnoreCase("Y")) {
                    String variableid = ajaxResponse.getRequestParameter("variableid", "");
                    TaskDefVariables.findVariableUsage(propslist, variableid.length() > 0 ? variableid : "", this.logger);
                    changed = true;
                }
                if ((area = ajaxResponse.getRequestParameter("area", "properties")).equalsIgnoreCase("properties")) {
                    html.append(TaskDefVariables.getVariablePropertiesHTML(propslist, ajaxResponse.getRequestParameter("selected", ""), viewOnly, descendant, this.getConnectionId(), pageContext, this.logger));
                    ajaxResponse.addCallbackArgument("html", html.toString());
                    ajaxResponse.addCallbackArgument("selected", ajaxResponse.getRequestParameter("selected", ""));
                } else if (area.equalsIgnoreCase("tree")) {
                    changed = TaskDefVariables.syncVariables(propslist, ajaxResponse.getRequestParameter("stepid", ""), this.getSDCProcessor(), this.logger);
                    if (!ajaxResponse.getRequestParameter("propsonly", "N").equalsIgnoreCase("Y")) {
                        TaskDefProperties.ItemType highlightType = null;
                        if (ajaxResponse.getRequestParameter("highlighttype", "").length() > 0) {
                            try {
                                highlightType = TaskDefProperties.ItemType.valueOf(ajaxResponse.getRequestParameter("highlighttype", "").toUpperCase());
                            }
                            catch (Exception e) {
                                highlightType = null;
                            }
                        }
                        html.append(TaskDefVariables.getVariablesTreeHTML(propslist, ajaxResponse.getRequestParameter("selected", ""), true, ajaxResponse.getRequestParameter("painter", "N").equalsIgnoreCase("Y") ? "painter_variable" : "", viewOnly, descendant, highlightType, ajaxResponse.getRequestParameter("highlightid", ""), this.getRequestContext().getPropertyList("userconfig"), pageContext, this.logger));
                    }
                    ajaxResponse.addCallbackArgument("html", html.toString());
                    ajaxResponse.addCallbackArgument("selected", ajaxResponse.getRequestParameter("selected", ""));
                    ajaxResponse.addCallbackArgument("refresh", ajaxResponse.getRequestParameter("refresh", "Y"));
                }
                ajaxResponse.addCallbackArgument("returnprops", changed && propslist != null ? propslist.toJSONString(false) : "");
                ajaxResponse.addCallbackArgument("variableid", ajaxResponse.getRequestParameter("variableid", ""));
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            html.append(TaskDefVariables.getVariablePropertiesHTML(null, "", viewOnly, descendant, this.getConnectionId(), pageContext, this.logger));
        }
        ajaxResponse.print();
    }
}

