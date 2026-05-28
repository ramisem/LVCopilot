/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.configreport;

import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.configreport.ConfigReportPolicyDef;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.NodeList;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ConfigReportOptionsAjaxGetter
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "top.wizard_iframe.reportOptions_AjaxCallback");
        String titleList = "";
        String nodeListStr = "";
        try {
            WebAdminProcessor processor = new WebAdminProcessor(this.getConnectionId());
            PropertyTree tree = processor.getPropertyTree("ConfigReportPolicy");
            ArrayList allNodes = tree.getAllNodes();
            for (int i = 0; i < allNodes.size(); ++i) {
                PropertyList policy;
                ConfigReportPolicyDef policyDef;
                String nodename = allNodes.get(i).toString();
                Node currnode = tree.getNode(nodename);
                NodeList children = tree.getNodeDescendantList(currnode.getNodeId());
                if (children.size() != 0 || !(policyDef = new ConfigReportPolicyDef(policy = this.getConfigurationProcessor().getPolicy("ConfigReportPolicy", allNodes.get(i).toString()))).show()) continue;
                String title = policyDef.getRuleTitle();
                if (titleList.length() > 0) {
                    titleList = titleList + DELIMITER;
                    nodeListStr = nodeListStr + DELIMITER;
                }
                titleList = titleList + title;
                nodeListStr = nodeListStr + allNodes.get(i).toString();
            }
        }
        catch (Exception e) {
            ajaxResponse.addCallbackArgument("optionshtml", "ERROR:" + e.getMessage());
            ajaxResponse.print();
            return;
        }
        String selectednode = ajaxResponse.getRequestParameter("selectednode");
        String includediffreport = ajaxResponse.getRequestParameter("includediffreport");
        ajaxResponse.addCallbackArgument("optionshtml", this.getHtml(selectednode, nodeListStr, titleList));
        ajaxResponse.print();
    }

    private String getHtml(String selectednode, String nodeListStr, String titleList) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Select Configuration Report: <select name=\"policynode\" id=\"policynode\" >");
        String[] nodes = StringUtil.split(nodeListStr, DELIMITER);
        String[] titles = StringUtil.split(titleList, DELIMITER);
        buffer.append("<option value=\"\"></option>");
        for (int i = 0; i < nodes.length; ++i) {
            if (nodes[i].equals(selectednode)) {
                buffer.append("<option value=\"" + nodes[i] + "\" selected>" + titles[i] + "</option>");
                continue;
            }
            buffer.append("<option value=\"" + nodes[i] + "\">" + titles[i] + "</option>");
        }
        buffer.append("</select>");
        return buffer.toString();
    }
}

