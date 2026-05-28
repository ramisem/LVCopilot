/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPainter;
import com.labvantage.sapphire.pageelements.workflow.workflowdefpainter.WorkflowDefPainter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Browser;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorkflowDefElementAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54523 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block24: {
            ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
            String props = ajaxResponse.getRequestParameter("properties", "");
            if (props.length() > 0) {
                try {
                    PropertyList workflowprops = new PropertyList(new JSONObject(props));
                    try {
                        int nextElement;
                        WorkflowDefPainter.Tools elementType;
                        boolean remove = ajaxResponse.getRequestParameter("remove", "N").equalsIgnoreCase("Y");
                        boolean refresh = ajaxResponse.getRequestParameter("refresh", "N").equalsIgnoreCase("Y");
                        boolean translate = ajaxResponse.getRequestParameter("translate", "N").equalsIgnoreCase("Y");
                        PropertyListCollection elements = workflowprops.getCollection("elements");
                        if (remove) {
                            String elementid = ajaxResponse.getRequestParameter("elementid", "");
                            if (elementid.length() > 0) {
                                if (elements != null) {
                                    PropertyList element = elements.find("elementid", elementid);
                                    if (element != null) {
                                        elements.remove(element);
                                        ajaxResponse.addCallbackArgument("properties", workflowprops.toJSONString(false));
                                    } else {
                                        ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find task to remove."));
                                    }
                                }
                            } else {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("No task id provided."));
                            }
                            break block24;
                        }
                        if (refresh) {
                            String elementid = ajaxResponse.getRequestParameter("elementid", "");
                            if (elementid.length() > 0) {
                                if (elements == null) break block24;
                                PropertyList element = elements.find("elementid", elementid);
                                if (element != null) {
                                    WorkflowDefPainter.Tools elementtype;
                                    StringBuffer html = new StringBuffer();
                                    StringBuffer script = new StringBuffer();
                                    try {
                                        elementtype = WorkflowDefPainter.Tools.valueOf(element.getProperty("type", WorkflowDefPainter.Tools.LABEL.toString()).toUpperCase());
                                    }
                                    catch (Exception e) {
                                        elementtype = WorkflowDefPainter.Tools.LABEL;
                                    }
                                    if (elementtype != WorkflowDefPainter.Tools.TASK) {
                                        WorkflowDefPainter.renderElement(element, elements.size() - 1, html, script, false, false, ajaxResponse.getRequestParameter("renderhtml5", "N").equalsIgnoreCase("Y"), workflowprops.getProperty("rtl", "N").equalsIgnoreCase("Y"), new Browser(request), this.getSDIProcessor(), translate ? new TranslationProcessor(this.getConnectionId()) : null, this.getConnectionId(), this.logger);
                                    }
                                    ajaxResponse.addCallbackArgument("html", html.toString());
                                    ajaxResponse.addCallbackArgument("script", script.toString());
                                    ajaxResponse.addCallbackArgument("id", elementid);
                                    break block24;
                                }
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find element to refresh."));
                                break block24;
                            }
                            ajaxResponse.setError(this.getTranslationProcessor().translate("No element id provided."));
                            break block24;
                        }
                        try {
                            elementType = WorkflowDefPainter.Tools.valueOf(ajaxResponse.getRequestParameter("element", WorkflowDefPainter.Tools.LABEL.toString()).toUpperCase());
                        }
                        catch (Exception e) {
                            elementType = WorkflowDefPainter.Tools.LABEL;
                        }
                        String x = ajaxResponse.getRequestParameter("x", "200");
                        String y = ajaxResponse.getRequestParameter("y", "200");
                        if (elements == null) {
                            elements = new PropertyListCollection();
                            workflowprops.setProperty("elements", elements);
                        }
                        try {
                            nextElement = TaskDefPainter.getNextIdIndex(elements, Integer.parseInt(ajaxResponse.getRequestParameter("elementindex")), "elementid", "element");
                        }
                        catch (Exception e) {
                            nextElement = TaskDefPainter.getNextIdIndex(elements, -1, "elementid", "element");
                        }
                        PropertyList element = elementType.getProperties("element" + nextElement, x, y);
                        elements.add(element);
                        StringBuffer html = new StringBuffer();
                        StringBuffer script = new StringBuffer();
                        if (elementType != WorkflowDefPainter.Tools.TASK) {
                            WorkflowDefPainter.renderElement(element, elements.size() - 1, html, script, false, false, ajaxResponse.getRequestParameter("renderhtml5", "N").equalsIgnoreCase("Y"), workflowprops.getProperty("rtl", "N").equalsIgnoreCase("Y"), new Browser(request), this.getSDIProcessor(), translate ? new TranslationProcessor(this.getConnectionId()) : null, this.getConnectionId(), this.logger);
                        }
                        ajaxResponse.addCallbackArgument("properties", workflowprops.toJSONString(false));
                        ajaxResponse.addCallbackArgument("html", html.toString());
                        ajaxResponse.addCallbackArgument("script", script.toString());
                        ajaxResponse.addCallbackArgument("elementnum", "" + nextElement);
                    }
                    catch (Exception e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Could not render task."));
                    }
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
            }
        }
        ajaxResponse.print();
    }
}

