/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.maint.MaintElement;
import com.labvantage.sapphire.tagext.SDITagUtil;
import javax.servlet.jsp.PageContext;
import sapphire.tagext.SDITagInfo;
import sapphire.xml.PropertyList;

public class MaintWorkflow
extends MaintElement {
    public MaintWorkflow() {
        this.datasetname = "workflow";
    }

    public MaintWorkflow(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.init(pageContext, sdiInfo, connectionid, "workflow");
    }

    @Override
    protected String getDefaultHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "status", "Status") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "workflow", "Workflow") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "version", "Version") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "instance", "Instance") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "%complete", "% Complete") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected String getDefaultRowHtml() {
        StringBuffer html = new StringBuffer();
        PropertyList texts = this.element.getPropertyList("texts");
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"" + this.datasetname + "_row_" + this.rownum + "\">\n");
        html.append(this.getRowSelector());
        html.append("<td class=\"maintdetail_field\" width=\"10\"><img src=\"WEB-CORE/elements/images/" + SDITagUtil.getDisplayValue(this.sdiInfo.getString("workflow", "statusflag"), "N=workflow.gif;A=workflow_active.gif;C=workflow_complete.gif;E=workflow_exclamation.gif") + "\" title=\"" + SDITagUtil.getDisplayValue(this.sdiInfo.getString("workflow", "statusflag"), "N=" + ElementUtil.getText(this.element, "notactive", "Not Active") + ";A=" + ElementUtil.getText(this.element, "active", "Active") + ";C=" + ElementUtil.getText(this.element, "complete", "Complete") + ";E=" + ElementUtil.getText(this.element, "error", "Error") + "") + "\" /></td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("workflow", "workflowid") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("workflow", "workflowversionid") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getInt("workflow", "workflowinstance") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getInt("workflow", "percentcomplete") + "<span>%</span></td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected void setRowKeyids() {
        this.id = this.sdiInfo.getString("workflow", "keyid1") + "|" + this.sdiInfo.getString("workflow", "keyid2") + "|" + this.sdiInfo.getString("workflow", "keyid3") + "|" + this.sdiInfo.getString("workflow", "workflowid") + "|" + this.sdiInfo.getString("workflow", "workflowversionid") + "|" + this.sdiInfo.getInt("workflow", "workflowinstance");
    }
}

