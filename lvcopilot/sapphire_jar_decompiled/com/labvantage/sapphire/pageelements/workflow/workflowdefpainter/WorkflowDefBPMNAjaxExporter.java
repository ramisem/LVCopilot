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

import com.labvantage.sapphire.pageelements.workflow.bpmn.BPMNExporter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class WorkflowDefBPMNAjaxExporter
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                PropertyList workflowprops = new PropertyList(new JSONObject(props));
                workflowprops.setProperty("workflowdefid", ajaxResponse.getRequestParameter("keyid1", ""));
                workflowprops.setProperty("workflowdefversionid", ajaxResponse.getRequestParameter("keyid2", ""));
                workflowprops.setProperty("workflowdefvariantid", ajaxResponse.getRequestParameter("keyid3", ""));
                try {
                    String xmlstring = BPMNExporter.exportBPMN(workflowprops, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId(), ajaxResponse.getRequestParameter("xpdl", "N").equalsIgnoreCase("Y"));
                    ajaxResponse.addCallbackArgument("xmlstring", xmlstring);
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not import."));
                }
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No properties provided."));
        }
        ajaxResponse.print();
    }
}

