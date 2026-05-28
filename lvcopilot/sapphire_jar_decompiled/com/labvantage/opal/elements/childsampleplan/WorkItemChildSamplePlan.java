/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.opal.elements.childsampleplan;

import com.labvantage.opal.elements.childsampleplan.BaseChildSamplePlan;
import com.labvantage.sapphire.tagext.JavaScriptAPITag;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class WorkItemChildSamplePlan
extends BaseChildSamplePlan {
    @Override
    public String getHtml() {
        this.isEmbedded = true;
        boolean isBioBankingType = "BioBanking".equals(this.element.getProperty("type", "BioBanking"));
        String workitemid = this.requestContext.getProperty("keyid1");
        String workitemversionid = this.requestContext.getProperty("keyid2");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select embedchildsampleplanid, embedchildsampleplanversionid, applicablesampletypeid, autoapplyflag, versionstatus from workitem where workitemid = ? and workitemversionid = ?", (Object[])new String[]{workitemid, workitemversionid});
        String childsampleplanid = ds.getString(0, "embedchildsampleplanid", "");
        String childsampleplanversionid = ds.getString(0, "embedchildsampleplanversionid", "");
        String versionstatus = ds.getString(0, "versionstatus", "");
        StringBuilder sb = new StringBuilder();
        sb.append(JavaScriptAPITag.getJQueryAPI(true, false, null, this.pageContext));
        if (childsampleplanid.length() == 0) {
            sb.append("<script type='text/javascript' src='WEB-OPAL/elements/childsampleplan/scripts/childsampleplan.js'></script>");
            sb.append(this.getTranslationProcessor().translate("No Child Sample Plan defined."));
            sb.append("<br><br>");
        } else {
            boolean hasOverride;
            if ("C".equals(versionstatus)) {
                this.element.setProperty("mode", "View");
            }
            boolean viewOnly = "View".equals(this.element.getProperty("mode"));
            String privatechildsampleplanid = this.requestContext.getProperty("privatechildsampleplanid");
            String privatechildsampleplanversionid = this.requestContext.getProperty("privatechildsampleplanversionid");
            boolean bl = hasOverride = StringUtil.getLen(privatechildsampleplanid) > 0L && StringUtil.getLen(privatechildsampleplanversionid) > 0L;
            if (hasOverride) {
                childsampleplanid = privatechildsampleplanid;
                childsampleplanversionid = privatechildsampleplanversionid;
            }
            sb.append("<script type='text/javascript'>");
            sb.append("\n_lvdetailpresavearray.push( '").append(this.element.getId()).append("' );");
            sb.append("\nfunction detailpresave_").append(this.element.getId()).append("() {return childsampleplan.validate();}");
            sb.append("</script>");
            PropertyList columntitletext = this.element.getPropertyListNotNull("columntitletext");
            ds = this.getQueryProcessor().getPreparedSqlDataSet("select markparentconsumedflag, quantityvalidationflag, useforaccessionflag from s_childsampleplan where s_childsampleplanid = ? and s_childsampleplanversionid = ?", (Object[])new String[]{childsampleplanid, childsampleplanversionid});
            sb.append("<input type='hidden' name='__").append(this.element).append("_embedflag' value='Y'>");
            if (hasOverride) {
                sb.append("<div style='padding:4px;color:red;'>");
                sb.append(this.getTranslationProcessor().translate("This Child Sample Plan definition has been overridden"));
                sb.append("</div>");
            }
            sb.append("<table cellpadding=4 cellspacing=0 border=0 style='margin:5px;'>");
            sb.append("<tr>");
            sb.append("<td class='maintform_fieldtitle'>").append(this.getTranslationProcessor().translate(columntitletext.getProperty("Mark Parent Sample Consumed", "Mark Parent Sample Consumed"))).append("</td>");
            sb.append("<td class='maintform_field'>");
            sb.append("<input id='__").append(this.elementid).append("_markparentconsumedflag' name='__").append(this.elementid).append("_markparentconsumedflag' type=checkbox");
            sb.append("Y".equals(ds.getString(0, "markparentconsumedflag", "N")) ? " checked" : "").append(viewOnly ? " disabled" : "").append(" onchange='setChangesMade( true );'>");
            sb.append("</td>");
            sb.append("<td class='maintform_fieldtitle'>").append(this.getTranslationProcessor().translate(columntitletext.getProperty("Validate Parent Quantity", "Validate Parent Quantity"))).append("</td>");
            sb.append("<td class='maintform_field'>");
            sb.append("<input id='__").append(this.elementid).append("_quantityvalidationflag' name='__").append(this.elementid).append("_quantityvalidationflag' type=checkbox");
            sb.append("Y".equals(ds.getString(0, "quantityvalidationflag", "N")) ? " checked" : "").append(viewOnly ? " disabled" : "").append(" onchange='setChangesMade( true );'></td>");
            if (isBioBankingType) {
                sb.append("<td class='maintform_fieldtitle'>").append(this.getTranslationProcessor().translate(columntitletext.getProperty("Used for Accessioning", "Used for Accessioning"))).append("</td>");
                sb.append("<td class='maintform_field'>");
                sb.append("<input id='__").append(this.elementid).append("_useforaccessionflag' name='__").append(this.elementid).append("_useforaccessionflag' type=checkbox");
                sb.append("Y".equals(ds.getString(0, "useforaccessionflag", "N")) ? " checked" : "").append(viewOnly ? " disabled" : "").append(" onchange='setChangesMade( true );'></td>");
            }
            sb.append("</tr>");
            sb.append("</table>");
            sb.append(this.getElementHtml(childsampleplanid, childsampleplanversionid));
        }
        return sb.toString();
    }
}

