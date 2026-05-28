/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import com.labvantage.sapphire.util.format.NumericFormatter;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.SequenceProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DownloadOptionsAjaxGetter
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "top.wizard_iframe.downloadOptions_AjaxCallback");
        String projectfullname = ajaxResponse.getRequestParameter("project");
        String templatenames = ajaxResponse.getRequestParameter("templatenames");
        String selectedmode = ajaxResponse.getRequestParameter("selectedmode");
        String selectedname = ajaxResponse.getRequestParameter("selectedname");
        String selectedbasedon = ajaxResponse.getRequestParameter("selectedbasedon");
        String selectednode = ajaxResponse.getRequestParameter("selectednode");
        String titleList = "";
        String nodeListStr = "";
        try {
            WebAdminProcessor processor = new WebAdminProcessor(this.getConnectionId());
            PropertyTree tree = processor.getPropertyTree("EmpowerPolicy");
            ArrayList allNodes = tree.getAllNodes();
            String[] projectNameStack = StringUtil.split(projectfullname, "\\");
            for (int i = projectNameStack.length; i > 0; --i) {
                String project = projectNameStack[i - 1];
                ArrayList nList = this.findNodesForProject(allNodes, project);
                if (nList == null || nList.size() <= 0) continue;
                for (int j = 0; j < nList.size(); ++j) {
                    PropertyList policy = this.getConfigurationProcessor().getPolicy("EmpowerPolicy", nList.get(j).toString());
                    EmpowerPolicyDef policyDef = new EmpowerPolicyDef(policy);
                    String title = policyDef.getRuleTitle();
                    if (titleList.length() > 0) {
                        titleList = titleList + DELIMITER;
                        nodeListStr = nodeListStr + DELIMITER;
                    }
                    titleList = titleList + title;
                    nodeListStr = nodeListStr + nList.get(j).toString();
                }
                break;
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Cannot find policy node list:" + e.getMessage());
            ajaxResponse.print();
            return;
        }
        ajaxResponse.addCallbackArgument("optionshtml", this.getHtml(selectedmode, selectedname, selectedbasedon, selectednode, templatenames, nodeListStr, titleList));
        if (selectedname == null || selectedname.length() == 0) {
            ajaxResponse.addCallbackArgument("generatedname", this.getSampleSetMethodName());
        }
        ajaxResponse.print();
    }

    private String getSampleSetMethodName() {
        SequenceProcessor sequenceProcessor = this.getSequenceProcessor();
        int id = sequenceProcessor.getSequence("samplesetmethod", "samplesetmethod");
        if (id == -1) {
            Trace.log("Error getting sequence for tracelog");
            return "ERROR";
        }
        String num = NumericFormatter.formatNumber(id, "0000000");
        return "SSM_" + num;
    }

    private ArrayList findNodesForProject(ArrayList allNodes, String projectName) {
        ArrayList<String> nodeList = new ArrayList<String>();
        for (int i = 0; i < allNodes.size(); ++i) {
            String currNode = allNodes.get(i).toString();
            if (!currNode.startsWith(projectName + "|") && !currNode.equals(projectName)) continue;
            nodeList.add(currNode);
        }
        return nodeList;
    }

    private String getHtml(String selectedmode, String selectedname, String selectedbasedon, String selectednode, String templateList, String nodeListStr, String titleList) {
        StringBuffer buffer = new StringBuffer();
        if (selectedmode == null || selectedmode.length() == 0) {
            selectedmode = "AQC Mode";
        }
        TranslationProcessor tp = this.getTranslationProcessor();
        buffer.append("<table style=\"padding-left:125px; width:100%\">");
        buffer.append("<tr height=\"20\">");
        buffer.append("<td></td>");
        buffer.append("</tr>");
        buffer.append("<td>");
        buffer.append("<fieldset style=\"margin-left:50px;padding-left:25px; width:80%\">");
        buffer.append("<legend>" + tp.translate("Download Mode") + "</legend>");
        buffer.append("<table cellspacing=\"0\" cellpadding=\"10\" style=\"margin-bottom:0\">");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<table>");
        buffer.append("<tr>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        if (selectedmode.equals("AQC Mode")) {
            buffer.append("<td colspan=\"2\"><input type=\"radio\" name=\"download_p1_mode\" id=\"download_p1_mode_1\" checked> ");
        } else {
            buffer.append("<td colspan=\"2\"><input type=\"radio\" name=\"download_p1_mode\" id=\"download_p1_mode_1\"> ");
        }
        buffer.append(tp.translate("By selecting a QC Batch ( AQC Mode ) "));
        buffer.append("\t\t\t</td>");
        buffer.append("\t</tr> ");
        buffer.append("<tr>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        if (!selectedmode.equals("AQC Mode")) {
            buffer.append("<td colspan=\"2\"><input type=\"radio\" name=\"download_p1_mode\" id=\"download_p1_mode_2\" checked> ");
        } else {
            buffer.append("<td colspan=\"2\"><input type=\"radio\" name=\"download_p1_mode\" id=\"download_p1_mode_2\"> ");
        }
        buffer.append(tp.translate("By selecting individual Samples ( Candidate Mode )"));
        buffer.append("</td> ");
        buffer.append("</tr> ");
        buffer.append("</table> ");
        buffer.append("\t</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</fieldset> ");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("<tr height=\"20\"> ");
        buffer.append("<td></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append(" <fieldset style=\"margin-left:50px;padding-left:25px; width:80%\"> ");
        buffer.append(" <legend>" + tp.translate("SampleSetMethod Name") + "</legend>");
        buffer.append("<table cellspacing=\"0\" cellpadding=\"10\" style=\"margin-bottom:0\">");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<table>");
        buffer.append("<tr>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        if (selectedname == null || selectedname.length() == 0) {
            buffer.append(" <td><input type=\"radio\" name=\"download_p1_create\"  id=\"download_p1_create_1\" checked> " + tp.translate("Generate a Sequential SampleSetMethod name"));
        } else {
            buffer.append(" <td><input type=\"radio\" name=\"download_p1_create\"  id=\"download_p1_create_1\"> " + tp.translate("Generate a Sequential SampleSetMethod name"));
        }
        buffer.append(" </td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        if (selectedname == null || selectedname.length() == 0) {
            buffer.append("<td><input type=\"radio\" name=\"download_p1_create\" id=\"download_p1_create_2\" > " + tp.translate("Create a SampleSetMethod called") + " <input type=\"text\" ");
            buffer.append("name=\"download_p1_create_name\" id=\"download_p1_create_name\"");
        } else {
            buffer.append("<td><input type=\"radio\" name=\"download_p1_create\" id=\"download_p1_create_2\" checked> " + tp.translate("Create a SampleSetMethod called") + " <input type=\"text\" ");
            buffer.append("name=\"download_p1_create_name\" id=\"download_p1_create_name\"  value=\"" + selectedname + "\"");
        }
        buffer.append("onclick=\"document.getElementById('download_p1_create_2').checked = true;\" value=\"\">");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</td>");
        buffer.append(" </tr>");
        buffer.append("  </table>");
        buffer.append("</fieldset>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("<tr height=\"20\">");
        buffer.append("  <td></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<fieldset style=\"margin-left:50px;padding-left:25px; width:80%\">");
        buffer.append("<legend>" + tp.translate("Base SampleSetMethod") + "</legend>");
        buffer.append("<table cellspacing=\"0\" cellpadding=\"10\" style=\"margin-bottom:0\"> ");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<table>");
        buffer.append("<tr>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        buffer.append("<td colspan=\"2\">" + tp.translate("Select SampleSetMethod Base to Copy") + "</td>");
        buffer.append("<td><select name=\"download_p4_method\" id=\"download_p4_method\"> ");
        String[] templates = StringUtil.split(templateList, DELIMITER);
        for (int i = 0; i < templates.length; ++i) {
            if (templates[i].equals(selectedbasedon)) {
                buffer.append("<option value=\"" + templates[i] + "\" selected>" + templates[i] + "</option>");
                continue;
            }
            buffer.append("<option value=\"" + templates[i] + "\">" + templates[i] + "</option>");
        }
        buffer.append("</select></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append(" </table>");
        buffer.append(" </fieldset>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("<tr height=\"20\">");
        buffer.append("<td></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td>");
        buffer.append("<fieldset style=\"margin-left:50px;padding-left:25px; width:80%\"> ");
        buffer.append("<legend>" + tp.translate("Empower Rules") + "</legend>");
        buffer.append("<table cellspacing=\"0\" cellpadding=\"10\" style=\"margin-bottom:0\">");
        buffer.append(" <tr>");
        buffer.append("<td>");
        buffer.append(" <table>");
        buffer.append(" <tr>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        buffer.append("<td colspan=\"2\">" + tp.translate("Select Empower Rules") + "</td>");
        buffer.append("<td><select name=\"download_policy_node\" id=\"download_policy_node\" >");
        String[] nodes = StringUtil.split(nodeListStr, DELIMITER);
        String[] titles = StringUtil.split(titleList, DELIMITER);
        for (int i = 0; i < nodes.length; ++i) {
            if (nodes[i].equals(selectednode)) {
                buffer.append("<option value=\"" + nodes[i] + "\" selected>" + titles[i] + "</option>");
                continue;
            }
            buffer.append("<option value=\"" + nodes[i] + "\">" + titles[i] + "</option>");
        }
        buffer.append("  </select></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        buffer.append("<td width=\"100\">&nbsp;</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</td>");
        buffer.append("   </tr>");
        buffer.append("</table>");
        buffer.append("</fieldset>");
        buffer.append("  </td>");
        buffer.append("</tr>");
        buffer.append("</table> ");
        return buffer.toString();
    }
}

