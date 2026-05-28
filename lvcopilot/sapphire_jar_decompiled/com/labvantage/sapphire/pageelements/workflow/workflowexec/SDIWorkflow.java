/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.workflow.workflowexec;

import com.labvantage.sapphire.admin.ddt.LV_TaskDef;
import com.labvantage.sapphire.admin.ddt.LV_WorkflowDef;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerCodes;
import com.labvantage.sapphire.pageelements.gwt.shared.WorkflowManagerConstants;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.accessor.WorkflowProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;

public class SDIWorkflow
extends BaseElement
implements WorkflowManagerConstants {
    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        WorkflowProcessor workflowProcessor = new WorkflowProcessor(this.pageContext);
        String sdcid = null;
        DataSet primary = null;
        String[] keycols = null;
        if (this.sdiInfo != null) {
            sdcid = this.sdiInfo.getSdcid();
            primary = this.sdiInfo.getDataSet("primary");
            keycols = this.sdiInfo.getKeycols();
        } else {
            sdcid = this.requestContext.getProperty("sdcid");
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDIList(sdcid, this.requestContext.getProperty("keyid1"), this.requestContext.getProperty("keyid2"), this.requestContext.getProperty("keyid3"));
            sdiRequest.setRequestItem("primary");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            primary = sdiData.getDataset("primary");
            keycols = sdiData.getKeys("primary");
        }
        String keyid1 = primary.getColumnValues(keycols[0], ";");
        String keyid2 = keycols.length >= 2 ? primary.getColumnValues(keycols[1], ";") : "";
        String keyid3 = keycols.length >= 3 ? primary.getColumnValues(keycols[2], ";") : "";
        try {
            TranslationProcessor tp = this.getTranslationProcessor();
            DataSet data = workflowProcessor.getSDIWorkflowData(sdcid, keyid1, keyid2, keyid3);
            html.append("<table class=\"maintdetail_table\" id=\"sdiworkflow\" cellpadding=\"3\" cellspacing=\"0\">");
            html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">");
            html.append("<td class=\"maintdetail_header\">").append(tp.translate("Workflow")).append("</td>");
            html.append("<td class=\"maintdetail_header\">").append(tp.translate("Execution Name")).append("</td>");
            html.append("<td class=\"maintdetail_header\">").append(tp.translate("Task")).append("</td>");
            html.append("<td class=\"maintdetail_header\">").append(tp.translate("Started")).append("</td>");
            html.append("<td class=\"maintdetail_header\">").append(tp.translate("Complete")).append("</td>");
            html.append("<td class=\"maintdetail_header\">").append(tp.translate("Status")).append("</td>");
            html.append("</tr>\n");
            String lastWorkflow = "__";
            String lastWorkflowexec = "__";
            String lastTask = "__";
            for (int i = 0; i < data.size(); ++i) {
                String taskexecid;
                String task;
                String workflowexec;
                html.append("<tr height=\"10\" class=\"maintdetail_row\">");
                String workflow = LV_WorkflowDef.getText(data.getValue(i, "workflowdefid"), data.getValue(i, "workflowdefversionid"), data.getValue(i, "workflowdefvariantid"));
                if (!workflow.equals(lastWorkflow)) {
                    int j;
                    for (j = i + 1; j < data.size() && LV_WorkflowDef.getText(data.getValue(j, "workflowdefid"), data.getValue(j, "workflowdefversionid"), data.getValue(j, "workflowdefvariantid")).equals(workflow); ++j) {
                    }
                    html.append("<td class=\"maintdetail_field\" rowspan=\"").append(j - i).append("\"><table border=\"0\"><tr><td width=\"16px\"><img src=\"").append(WorkflowManagerCodes.getWorkflowExecStatusImage(data.getValue(i, "wfexecstatus"))).append("\"/></td><td>").append(workflow).append("</td></tr></table>").append("</td>");
                    lastWorkflow = workflow;
                }
                if (!(workflowexec = data.getValue(i, "workflowexecname", data.getValue(i, "workflowexecid"))).equals(lastWorkflowexec)) {
                    int j;
                    for (j = i + 1; j < data.size() && data.getValue(j, "workflowexecname", data.getValue(i, "workflowexecid")).equals(workflowexec); ++j) {
                    }
                    html.append("<td class=\"maintdetail_field\" rowspan=\"").append(j - i).append("\"><table border=\"0\"><tr><td width=\"16px\"><img src=\"").append(WorkflowManagerCodes.getWorkflowExecStatusImage(data.getValue(i, "wfeexecstatus"))).append("\"/></td><td>").append(workflowexec).append("</td></tr></table>").append("</td>");
                    lastWorkflowexec = workflowexec;
                }
                if (!(task = LV_TaskDef.getText(data.getValue(i, "taskdefid"), data.getValue(i, "taskdefversionid"), data.getValue(i, "taskdefvariantid"))).equals(lastTask)) {
                    int j;
                    for (j = i + 1; j < data.size() && LV_TaskDef.getText(data.getValue(j, "taskdefid"), data.getValue(j, "taskdefversionid"), data.getValue(j, "taskdefvariantid")).equals(task); ++j) {
                    }
                    html.append("<td class=\"maintdetail_field\" rowspan=\"").append(j - i).append("\"><table border=\"0\"><tr><td width=\"16px\"><img src=\"").append(data.getValue(i, "icon")).append("\"/></td><td>").append(task).append("</td></tr></table></td>");
                    lastTask = task;
                }
                if ((taskexecid = data.getValue(i, "taskexecid")).length() > 0) {
                    html.append("<td class=\"maintdetail_field\">").append(data.getValue(i, "startdt")).append("</td>");
                    html.append("<td class=\"maintdetail_field\">").append(data.getValue(i, "completedt")).append("</td>");
                    String execstatus = data.getValue(i, "execstatus");
                    html.append("<td class=\"maintdetail_field\"><table width=\"100%\" border=\"0\">");
                    html.append("<tr><td colspan=\"3\">").append(data.getValue(i, "summary")).append("<td></tr>");
                    html.append("<tr><td><img src=\"").append(WorkflowManagerCodes.getWorkflowExecStatusImage(execstatus)).append("\"/></td><td>").append(WorkflowManagerCodes.getWorkflowExecStatusText(execstatus)).append("</td><td><a href=\"javascript:showTaskExecPopup('").append(taskexecid).append("')\">Details</a><td></tr>");
                    html.append("</table></td>");
                } else {
                    html.append("<td class=\"maintdetail_field\" colspan=\"3\">").append(tp.translate("Not started")).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>\n");
        }
        catch (SapphireException e) {
            html.append(e.getMessage());
        }
        return html.toString();
    }
}

