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

public class MaintDataSet
extends MaintElement {
    public MaintDataSet() {
        this.datasetname = "dataset";
    }

    public MaintDataSet(PageContext pageContext, SDITagInfo sdiInfo, String connectionid) {
        this.init(pageContext, sdiInfo, connectionid, "dataset");
    }

    @Override
    protected String getDefaultHeaderHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_headerrow\">\n");
        html.append(this.getHeaderSelector());
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "test", "Test") + "/" + ElementUtil.getText(this.element, "version", "Version") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "variant", "Variant") + "</td>\n");
        html.append("<td class=\"maintdetail_header\">" + ElementUtil.getText(this.element, "dataset", "Dataset") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected String getDefaultRowHtml() {
        StringBuffer html = new StringBuffer();
        html.append("<tr height=\"10\" class=\"maintdetail_row\" id=\"" + this.datasetname + "_row_" + this.rownum + "\">\n");
        html.append(this.getRowSelector());
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("dataset", "paramlistid") + "<span>(" + ElementUtil.getText(this.element, "version", "Version") + ": </span>" + this.sdiInfo.getString("dataset", "paramlistversionid") + "<span>)</span></td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getString("dataset", "variantid") + "</td>\n");
        html.append("<td class=\"maintdetail_field\">" + this.sdiInfo.getInt("dataset", "dataset") + "</td>\n");
        html.append("</tr>");
        return html.toString();
    }

    @Override
    protected void setRowKeyids() {
        String keyid1 = this.sdiInfo.getString("dataset", "keyid1");
        String keyid2 = this.sdiInfo.getString("dataset", "keyid2");
        String keyid3 = this.sdiInfo.getString("dataset", "keyid3");
        String paramlistid = this.sdiInfo.getString("dataset", "paramlistid");
        String paramlistversionid = this.sdiInfo.getString("dataset", "paramlistversionid");
        String variantid = this.sdiInfo.getString("dataset", "variantid");
        String datasetVal = "" + this.sdiInfo.getInt("dataset", "dataset");
        this.id = keyid1 + "|" + keyid2 + "|" + keyid3 + "|" + paramlistid + "|" + paramlistversionid + "|" + variantid + "|" + datasetVal;
    }
}

