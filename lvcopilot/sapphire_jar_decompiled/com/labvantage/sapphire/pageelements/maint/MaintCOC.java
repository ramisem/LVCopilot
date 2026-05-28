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

public class MaintCOC
extends MaintElement {
    public MaintCOC() {
        this.datasetname = "coc";
    }

    public MaintCOC(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.init(pageContext, sdiInfo, connectionid, "coc");
    }

    @Override
    protected String getDefaultHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "cocid", "Coc Id") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "fromcustodian", "From Custodian") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "tocustodian", "To Custodian") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected String getDefaultRowHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"" + this.datasetname + "_row_" + this.rownum + "\">\n");
        html.append(this.getRowSelector());
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("coc", "cocid") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("coc", "fromcustodianid") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("coc", "tocustodianid") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected void setRowKeyids() {
        this.id = this.sdiInfo.getString("coc", "keyid1") + "|" + this.sdiInfo.getString("coc", "keyid2") + "|" + this.sdiInfo.getString("coc", "keyid3") + "|" + this.sdiInfo.getString("coc", "cocid");
    }
}

