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

import com.labvantage.sapphire.pageelements.workflow.bpmn.BPMNImporter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class WorkflowDefBPMNAjaxImporter
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53106 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        this.logDebug("Starting BPMN Import...");
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
        String xmlstring = ajaxResponse.getRequestParameter("xmlstring", "");
        if (xmlstring.length() > 0) {
            String props = ajaxResponse.getRequestParameter("properties", "");
            if (props.length() > 0) {
                try {
                    PropertyList workflowprops = new PropertyList(new JSONObject(props));
                    try {
                        PropertyList newprops = BPMNImporter.importBPMN(xmlstring);
                        if (newprops.getCollection("tasks") != null && newprops.getCollection("tasks").size() > 0) {
                            if (workflowprops.getCollection("tasks") == null) {
                                workflowprops.setProperty("tasks", newprops.getCollection("tasks"));
                            } else {
                                workflowprops.getCollection("tasks").addAll(newprops.getCollection("tasks"));
                            }
                        } else {
                            this.logDebug("No tasks from import to add to workflow.");
                        }
                        if (newprops.getCollection("elements") != null && newprops.getCollection("elements").size() > 0) {
                            if (workflowprops.getCollection("elements") == null) {
                                workflowprops.setProperty("elements", newprops.getCollection("elements"));
                            } else {
                                workflowprops.getCollection("elements").addAll(newprops.getCollection("elements"));
                            }
                        } else {
                            this.logDebug("No elements from import to add to workflow.");
                        }
                        String ret = workflowprops.toJSONString(false);
                        this.logDebug("Import finished. Return length = " + ret.length() + ".");
                        ajaxResponse.addCallbackArgument("properties", ret);
                    }
                    catch (Exception e) {
                        this.logger.error("Could not import", e);
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Could not import."));
                    }
                }
                catch (Exception e) {
                    this.logger.error("Properties invalid", e);
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
                }
            } else {
                this.logger.error("No properties provided");
                ajaxResponse.setError(this.getTranslationProcessor().translate("No properties provided."));
            }
        } else {
            this.logger.error("No XML string provided.");
            ajaxResponse.setError(this.getTranslationProcessor().translate("No XML string provided."));
        }
        ajaxResponse.print();
        this.logDebug("...Finished BPMN Import.");
    }
}

