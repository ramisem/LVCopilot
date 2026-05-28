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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class TaskDefTransitionAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                PropertyList taskprops = new PropertyList(new JSONObject(props));
                try {
                    String fromStep = ajaxResponse.getRequestParameter("from", "");
                    String toStep = ajaxResponse.getRequestParameter("to", "");
                    if (fromStep.length() > 0 && toStep.length() > 0) {
                        PropertyListCollection steps = taskprops.getCollection("steps");
                        if (steps != null) {
                            String moveFromFromStep = ajaxResponse.getRequestParameter("movefrom", "");
                            String moveFromToStep = ajaxResponse.getRequestParameter("moveto", "");
                            PropertyList step = steps.find("stepid", fromStep);
                            if (step != null) {
                                PropertyListCollection transitions;
                                PropertyList next = step.getPropertyList("next");
                                if (next == null) {
                                    next = new PropertyList();
                                    step.setProperty("next", next);
                                }
                                if ((transitions = next.getCollection("transitions")) == null) {
                                    transitions = new PropertyListCollection();
                                    next.setProperty("transitions", transitions);
                                }
                                PropertyList transition = null;
                                boolean remove = ajaxResponse.getRequestParameter("remove", "N").equalsIgnoreCase("Y");
                                if (remove) {
                                    transition = transitions.find("stepid", toStep);
                                    if (transition != null) {
                                        transitions.remove(transition);
                                    } else {
                                        ajaxResponse.setError("Could not find to step " + toStep + ".");
                                    }
                                } else {
                                    boolean handled = false;
                                    if (moveFromFromStep.length() > 0) {
                                        PropertyList oldstep = steps.find("stepid", moveFromFromStep);
                                        if (oldstep != null) {
                                            PropertyListCollection oldtransitions = step.getPropertyList("next") != null ? step.getPropertyList("next").getCollection("transitions") : null;
                                            PropertyList propertyList = transition = oldtransitions != null ? oldtransitions.find("stepid", toStep) : null;
                                            if (transition != null) {
                                                oldtransitions.remove(transition);
                                            } else {
                                                transition = new PropertyList();
                                            }
                                        } else {
                                            transition = new PropertyList();
                                        }
                                        transition.setProperty("transitionid", fromStep + "_to_" + toStep);
                                        transition.setProperty("stepid", toStep);
                                        transitions.add(transition);
                                        handled = true;
                                    } else if (moveFromToStep.length() > 0 && (transition = transitions.find("stepid", toStep)) != null) {
                                        handled = true;
                                        transition.setProperty("transitionid", fromStep + "_to_" + moveFromToStep);
                                        transition.setProperty("stepid", moveFromToStep);
                                    }
                                    if (!handled) {
                                        transition = new PropertyList();
                                        transition.setProperty("transitionid", fromStep + "_to_" + toStep);
                                        transition.setProperty("stepid", toStep);
                                        transitions.add(transition);
                                    }
                                }
                                ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString(true));
                                ajaxResponse.addCallbackArgument("transition", transition == null ? new PropertyList().toJSONString(true) : transition.toJSONString(true));
                            } else {
                                ajaxResponse.setError("Could not find step " + fromStep + ".");
                            }
                        } else {
                            ajaxResponse.setError("No steps defined.");
                        }
                    } else {
                        ajaxResponse.setError("No from or to step provided.");
                    }
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("No type string provided."));
                }
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

