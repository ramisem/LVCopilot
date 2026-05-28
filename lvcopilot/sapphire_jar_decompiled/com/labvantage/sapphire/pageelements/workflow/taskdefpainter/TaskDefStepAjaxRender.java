/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.pageelements.workflow.taskdefpainter.TaskDefPainter;
import java.util.Random;
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

public class TaskDefStepAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54656 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block42: {
            ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
            String props = ajaxResponse.getRequestParameter("properties", "");
            if (props.length() > 0) {
                try {
                    PropertyList taskprops = new PropertyList(new JSONObject(props));
                    try {
                        boolean remove = ajaxResponse.getRequestParameter("remove", "N").equalsIgnoreCase("Y");
                        boolean copy = ajaxResponse.getRequestParameter("copy", "N").equalsIgnoreCase("Y");
                        if (remove) {
                            String stepid = ajaxResponse.getRequestParameter("stepid", "");
                            if (stepid.length() > 0) {
                                if (taskprops.getProperty("startstepid", "").equals(stepid)) {
                                    taskprops.setProperty("startstepid", "");
                                }
                                TaskDefPainter.removeWindow(taskprops, stepid);
                                ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString(true));
                            } else {
                                String elementid = ajaxResponse.getRequestParameter("elementid", "");
                                if (elementid.length() > 0) {
                                    TaskDefPainter.removeElement(taskprops, elementid);
                                    ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString(true));
                                } else {
                                    ajaxResponse.setError(this.getTranslationProcessor().translate("No step id provided."));
                                }
                            }
                            break block42;
                        }
                        if (copy) {
                            String stepid = ajaxResponse.getRequestParameter("stepid", "");
                            if (stepid.length() > 0) {
                                boolean devMode;
                                int nextStep;
                                StringBuffer html = new StringBuffer();
                                StringBuffer script = new StringBuffer();
                                if (taskprops.getCollection("steps") == null) {
                                    taskprops.setProperty("steps", new PropertyListCollection());
                                }
                                PropertyListCollection steps = taskprops.getCollection("steps");
                                try {
                                    nextStep = TaskDefPainter.getNextIdIndex(steps, Integer.parseInt(ajaxResponse.getRequestParameter("stepindex")), "stepid", "step");
                                }
                                catch (Exception e) {
                                    nextStep = TaskDefPainter.getNextIdIndex(steps, -1, "stepid", "step");
                                }
                                ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
                                try {
                                    devMode = cp.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                                }
                                catch (Exception e) {
                                    devMode = false;
                                }
                                PropertyList toCopy = steps.find("stepid", stepid);
                                if (toCopy != null) {
                                    PropertyList copied = toCopy.copy();
                                    String oldx = toCopy.getProperty("x", "0");
                                    String oldy = toCopy.getProperty("y", "0");
                                    int x = 0;
                                    int y = 0;
                                    try {
                                        x = Integer.parseInt(oldx);
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                    try {
                                        y = Integer.parseInt(oldy);
                                    }
                                    catch (Exception exception) {
                                        // empty catch block
                                    }
                                    copied.setProperty("title", copied.getProperty("title") + " Copy");
                                    copied.setProperty("x", "" + (x + 100));
                                    copied.setProperty("y", "" + (y + 50));
                                    copied.setProperty("stepid", "step" + nextStep);
                                    if (copied.containsKey("next")) {
                                        copied.remove("next");
                                    }
                                    steps.add(copied);
                                    TaskDefPainter.renderWindow(copied, null, null, nextStep, html, script, false, false, taskprops.getProperty("startstepid", ""), devMode, ajaxResponse.getRequestParameter("renderhtml5", "N").equalsIgnoreCase("Y"), taskprops.getProperty("rtl", "N").equalsIgnoreCase("Y"), request.getSession(), new WebAdminProcessor(this.getConnectionId()), new Browser(request), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                                    ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString(true));
                                    ajaxResponse.addCallbackArgument("html", html.toString());
                                    ajaxResponse.addCallbackArgument("script", script.toString());
                                    ajaxResponse.addCallbackArgument("stepnumber", "" + nextStep);
                                    break block42;
                                }
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find step id provided."));
                                break block42;
                            }
                            ajaxResponse.setError(this.getTranslationProcessor().translate("No step id provided."));
                            break block42;
                        }
                        String stepType = ajaxResponse.getRequestParameter("steptype", "");
                        String stepNode = ajaxResponse.getRequestParameter("node", "");
                        if (stepType.length() > 0) {
                            boolean devMode;
                            int nextStep;
                            String x = ajaxResponse.getRequestParameter("x", "200");
                            String y = ajaxResponse.getRequestParameter("y", "200");
                            PropertyList step = new PropertyList();
                            step.setProperty("x", x);
                            step.setProperty("y", y);
                            if (taskprops.getCollection("steps") == null) {
                                taskprops.setProperty("steps", new PropertyListCollection());
                            }
                            PropertyListCollection steps = taskprops.getCollection("steps");
                            StringBuffer html = new StringBuffer();
                            StringBuffer script = new StringBuffer();
                            try {
                                nextStep = TaskDefPainter.getNextIdIndex(steps, Integer.parseInt(ajaxResponse.getRequestParameter("stepindex")), "stepid", "step");
                            }
                            catch (Exception e) {
                                nextStep = TaskDefPainter.getNextIdIndex(steps, -1, "stepid", "step");
                            }
                            ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
                            try {
                                devMode = cp.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                            }
                            catch (Exception e) {
                                devMode = false;
                            }
                            step.setProperty("stepid", "step" + nextStep);
                            if (steps.size() == 0) {
                                taskprops.setProperty("startstepid", step.getProperty("stepid"));
                            }
                            steps.add(step);
                            TaskDefPainter.renderWindow(step, stepType, stepNode, nextStep, html, script, false, false, taskprops.getProperty("startstepid", ""), devMode, ajaxResponse.getRequestParameter("renderhtml5", "N").equalsIgnoreCase("Y"), taskprops.getProperty("rtl", "N").equalsIgnoreCase("Y"), request.getSession(), new WebAdminProcessor(this.getConnectionId()), new Browser(request), this.getConnectionProcessor().getSapphireConnection(), this.logger);
                            ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString(true));
                            ajaxResponse.addCallbackArgument("html", html.toString());
                            ajaxResponse.addCallbackArgument("script", script.toString());
                            ajaxResponse.addCallbackArgument("stepnumber", "" + nextStep);
                            break block42;
                        }
                        String elementid = ajaxResponse.getRequestParameter("elementid");
                        boolean refresh = ajaxResponse.getRequestParameter("refresh").equalsIgnoreCase("Y");
                        if (refresh && elementid.length() > 0) {
                            PropertyList element;
                            PropertyListCollection elements = taskprops.getCollection("elements");
                            PropertyList propertyList = element = elements != null ? elements.find("elementid", elementid) : null;
                            if (element != null) {
                                StringBuffer html = new StringBuffer();
                                StringBuffer script = new StringBuffer();
                                try {
                                    TaskDefPainter.Tools elementtype = TaskDefPainter.Tools.valueOf(element.getProperty("type", TaskDefPainter.Tools.LABEL.toString()).toUpperCase());
                                }
                                catch (Exception e) {
                                    TaskDefPainter.Tools elementtype = TaskDefPainter.Tools.LABEL;
                                }
                                TaskDefPainter.renderElement(element, elements.size() - 1, html, script, false, false, true, taskprops.getProperty("rtl", "N").equalsIgnoreCase("Y"), new Browser(request), this.getConnectionId(), ajaxResponse.getRequestParameter("translate", "N").equalsIgnoreCase("Y") ? new TranslationProcessor(this.getConnectionId()) : null, this.logger);
                                ajaxResponse.addCallbackArgument("html", html.toString());
                                ajaxResponse.addCallbackArgument("script", script.toString());
                                ajaxResponse.addCallbackArgument("id", elementid);
                                break block42;
                            }
                            ajaxResponse.setError(this.getTranslationProcessor().translate("Could not find element to refresh."));
                            break block42;
                        }
                        String elementtype = ajaxResponse.getRequestParameter("elementtype");
                        if (elementtype.length() > 0) {
                            TaskDefPainter.Tools tool = TaskDefPainter.Tools.LABEL;
                            try {
                                tool = TaskDefPainter.Tools.valueOf(elementtype.toUpperCase());
                            }
                            catch (Exception e) {
                                tool = TaskDefPainter.Tools.LABEL;
                            }
                            String x = ajaxResponse.getRequestParameter("x", "200");
                            String y = ajaxResponse.getRequestParameter("y", "200");
                            PropertyList element = tool.getProperties("element" + (new Random().nextInt(999) + 1), x, y);
                            if (taskprops.getCollection("elements") == null) {
                                taskprops.setProperty("elements", new PropertyListCollection());
                            }
                            PropertyListCollection elements = taskprops.getCollection("elements");
                            StringBuffer html = new StringBuffer();
                            StringBuffer script = new StringBuffer();
                            ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
                            try {
                                boolean devMode = cp.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
                            }
                            catch (Exception e) {
                                boolean devMode = false;
                            }
                            element.setProperty("type", tool.toString());
                            elements.add(element);
                            String newelementid = TaskDefPainter.renderElement(element, 1, html, script, true, false, ajaxResponse.getRequestParameter("renderhtml5", "N").equalsIgnoreCase("Y"), taskprops.getProperty("rtl", "N").equalsIgnoreCase("Y"), new Browser(request), this.getConnectionId(), this.getTranslationProcessor(), this.logger);
                            ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString(true));
                            ajaxResponse.addCallbackArgument("html", html.toString());
                            ajaxResponse.addCallbackArgument("script", script.toString());
                            ajaxResponse.addCallbackArgument("elementid", newelementid);
                            break block42;
                        }
                        ajaxResponse.setError(this.getTranslationProcessor().translate("No step type or element type string provided."));
                    }
                    catch (Exception e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Failed to render step."));
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

