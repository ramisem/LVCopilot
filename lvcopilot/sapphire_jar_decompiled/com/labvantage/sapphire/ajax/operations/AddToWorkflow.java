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

import com.labvantage.sapphire.admin.ddt.LV_WorkflowDef;
import com.labvantage.sapphire.admin.ddt.LV_WorkflowExec;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerCodes;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class AddToWorkflow
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "addToWorkFlowHandler");
        HashMap<String, String> actionProps = new HashMap<String, String>();
        try {
            actionProps.putAll(ajaxResponse.getRequestParameters());
            if (actionProps.containsKey("workflowexecid") && ((String)actionProps.get("workflowexecid")).length() > 0) {
                this.getActionProcessor().processAction("AddToWorkflow", "1", actionProps);
                ajaxResponse.addCallbackArgument("status", "ok");
                ajaxResponse.addCallbackArgument("statusmessage", (String)actionProps.get("statusmessage"));
                ajaxResponse.addCallbackArgument("taskqueueitems", Integer.parseInt((String)actionProps.get("taskqueueitems")));
            } else if (actionProps.containsKey("starttaskdefitemid") && ((String)actionProps.get("starttaskdefitemid")).length() > 0) {
                this.getActionProcessor().processAction("AddToWorkflow", "1", actionProps);
                ajaxResponse.addCallbackArgument("status", "ok");
                ajaxResponse.addCallbackArgument("statusmessage", (String)actionProps.get("statusmessage"));
                ajaxResponse.addCallbackArgument("taskqueueitems", Integer.parseInt((String)actionProps.get("taskqueueitems")));
            } else {
                DataSet workflowdef = this.getQueryProcessor().getPreparedSqlDataSet("SELECT execstatus, exectypeflag FROM workflowdef WHERE workflowdefid = ? AND workflowdefversionid = ? AND workflowdefvariantid = ?", new Object[]{(String)actionProps.get("workflowdefid"), (String)actionProps.get("workflowdefversionid"), (String)actionProps.get("workflowdefvariantid")});
                if (workflowdef != null && workflowdef.size() == 1) {
                    String wfexecstatus = workflowdef.getValue(0, "execstatus", "A");
                    if (wfexecstatus.equals("A")) {
                        if (workflowdef.getValue(0, "exectypeflag", "S").equals("N")) {
                            ajaxResponse.addCallbackArgument("status", "selectexec");
                        } else if (workflowdef.getValue(0, "exectypeflag", "S").equals("A")) {
                            ajaxResponse.addCallbackArgument("status", "createexec");
                            ajaxResponse.addCallbackArgument("statusmessage", "");
                            ajaxResponse.addCallbackArgument("taskqueueitems", 0);
                            String sdcid = (String)actionProps.get("sdcid");
                            ajaxResponse.addCallbackArgument("workflowexecname", LV_WorkflowExec.generateName(this.getQueryProcessor(), this.getSDCProcessor(), this.getSequenceProcessor(), sdcid, (String)actionProps.get("keyid1"), (String)actionProps.get("workflowdefid"), (String)actionProps.get("workflowdefversionid"), (String)actionProps.get("workflowdefvariantid")));
                        } else {
                            actionProps.put("fromAjaxAddToWorkflow", "Y");
                            this.getActionProcessor().processAction("AddToWorkflow", "1", actionProps);
                            String ambiguousStartTasks = (String)actionProps.get("ambiguousstarttasks");
                            if (ambiguousStartTasks == null || ambiguousStartTasks.equals("N")) {
                                ajaxResponse.addCallbackArgument("status", "ok");
                                ajaxResponse.addCallbackArgument("statusmessage", (String)actionProps.get("statusmessage"));
                                ajaxResponse.addCallbackArgument("taskqueueitems", Integer.parseInt((String)actionProps.get("taskqueueitems")));
                            } else {
                                ajaxResponse.addCallbackArgument("status", "selecttask");
                            }
                        }
                    } else {
                        ajaxResponse.addCallbackArgument("status", "error");
                        ajaxResponse.addCallbackArgument("statusmessage", "Workflow " + LV_WorkflowDef.getText((String)actionProps.get("workflowdefid"), (String)actionProps.get("workflowdefversionid"), (String)actionProps.get("workflowdefvariantid")) + " is in a " + WorkflowManagerCodes.getWorkflowExecStatusText(wfexecstatus) + " state");
                    }
                } else {
                    ajaxResponse.addCallbackArgument("status", "error");
                    ajaxResponse.addCallbackArgument("statusmessage", "Unrecognized workflow " + LV_WorkflowDef.getText((String)actionProps.get("workflowdefid"), (String)actionProps.get("workflowdefversionid"), (String)actionProps.get("workflowdefvariantid")));
                }
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to add SDI. Exception: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

