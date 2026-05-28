/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.pageelements.ElementUtil;
import com.labvantage.sapphire.pageelements.maint.MaintElement;
import javax.servlet.jsp.PageContext;
import sapphire.tagext.SDITagInfo;

public class MaintWorkItem
extends MaintElement {
    public MaintWorkItem() {
        this.datasetname = "workitem";
    }

    public MaintWorkItem(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.init(pageContext, sdiInfo, connectionid, "dataset");
    }

    @Override
    protected String getDefaultHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "workitemid", "Work Item Id") + "/" + ElementUtil.getText(this.element, "instance", "Instance") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected String getDefaultRowHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"" + this.datasetname + "_row_" + this.rownum + "\">\n");
        html.append(this.getRowSelector());
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("workitem", "workitemid") + "<span>(" + ElementUtil.getText(this.element, "instance", "Instance") + " </span>" + this.sdiInfo.getValue("workitem", "workiteminstance") + "<span>)</span></td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected void setRowKeyids() {
        String keyid1 = this.sdiInfo.getString("workitem", "keyid1");
        String keyid2 = this.sdiInfo.getString("workitem", "keyid2");
        String keyid3 = this.sdiInfo.getString("workitem", "keyid3");
        String workitemid = this.sdiInfo.getString("workitem", "workitemid");
        String workiteminstance = this.sdiInfo.getValue("workitem", "workiteminstance");
        this.id = keyid1 + "|" + keyid2 + "|" + keyid3 + "|" + workitemid + "|" + workiteminstance;
    }
}

