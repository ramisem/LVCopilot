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
import sapphire.util.HttpUtil;

public class MaintSpec
extends MaintElement {
    public MaintSpec() {
        this.datasetname = "spec";
    }

    public MaintSpec(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.init(pageContext, sdiInfo, connectionid, "spec");
    }

    @Override
    protected String getDefaultHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "specification", "Specification") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "version", "Version") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "description", "Description") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected String getDefaultRowHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"" + this.datasetname + "_row_" + this.rownum + "\">\n");
        html.append(this.getRowSelector());
        html.append("<td class=\"maintdetail_field\"><a target=\"specview\" href=\"rc?page=SpecView&keyid1=" + HttpUtil.encodeURIComponent(this.sdiInfo.getString("spec", "specid")) + "&keyid2=" + this.sdiInfo.getString("spec", "specversionid") + "\">");
        html.append("&nbsp;" + this.sdiInfo.getString("spec", "specid") + "</a></td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("spec", "specversionid") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getValue("spec", "specdesc") + "&nbsp;</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected void setRowKeyids() {
        this.id = this.sdiInfo.getString("spec", "keyid1") + "|" + this.sdiInfo.getString("spec", "keyid2") + "|" + this.sdiInfo.getString("spec", "keyid3") + "|" + this.sdiInfo.getString("spec", "specid") + "|" + this.sdiInfo.getString("spec", "specversionid");
    }
}

