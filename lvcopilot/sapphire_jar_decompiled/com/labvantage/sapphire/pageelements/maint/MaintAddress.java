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

public class MaintAddress
extends MaintElement {
    public MaintAddress() {
        this.datasetname = "address";
    }

    public MaintAddress(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.init(pageContext, sdiInfo, connectionid, "address");
    }

    @Override
    protected String getDefaultHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "addressid", "Address Id") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "addresstype", "Type") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "contactfunction", "Function") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected String getDefaultRowHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"" + this.datasetname + "_row_" + this.rownum + "\">\n");
        html.append(this.getRowSelector());
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("address", "addressid") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("address", "addresstype") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("address", "contactfunction") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected void setRowKeyids() {
        this.id = this.sdiInfo.getString("address", "keyid1") + "|" + this.sdiInfo.getString("address", "keyid2") + "|" + this.sdiInfo.getString("address", "keyid3") + "|" + this.sdiInfo.getString("address", "addressid") + "|" + this.sdiInfo.getString("address", "addresstype") + "|" + this.sdiInfo.getString("address", "contactfunction");
    }
}

