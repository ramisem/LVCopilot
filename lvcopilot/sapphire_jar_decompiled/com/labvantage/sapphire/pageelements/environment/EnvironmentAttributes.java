/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.environment;

import com.labvantage.opal.sql.SQLFactory;
import com.labvantage.opal.sql.SQLGenerator;
import java.util.ArrayList;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.JstlUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class EnvironmentAttributes
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54879 $";
    private String elementJSScript = "WEB-CORE/elements/environment/scripts/environmentattributes.js";

    @Override
    public String getHtml() {
        String reftype;
        int j;
        String lockedBy;
        DataSet primary;
        boolean readonly;
        boolean viewMode = false;
        StringBuffer html = new StringBuffer();
        ArrayList<String> unitsList = new ArrayList<String>();
        TranslationProcessor tp = new TranslationProcessor(this.pageContext);
        PropertyList pagedata = (PropertyList)JstlUtil.evaluateExpression("${pagedata}", this.pageContext);
        boolean bl = readonly = this.element.getProperty("readonly") != null && this.element.getProperty("readonly").equals("Y");
        if (!readonly && (primary = this.sdiInfo.getDataSet("primary")) != null && (lockedBy = primary.getValue(0, "__lockedby", "")) != null && lockedBy.length() > 0) {
            readonly = true;
        }
        if ("View".equalsIgnoreCase(pagedata.getProperty("mode")) || readonly) {
            viewMode = true;
        }
        SDIProcessor sdiProcessor = new SDIProcessor(this.pageContext);
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("Units");
        sdiRequest.setQueryFrom("units");
        sdiRequest.setQueryOrderBy("unitsid");
        sdiRequest.setRequestItem("primary");
        SDIData sdiData = sdiProcessor.getSDIData(sdiRequest);
        DataSet dsUnits = sdiData.getDataset("primary");
        if (dsUnits != null) {
            for (int i = 0; i < dsUnits.size(); ++i) {
                unitsList.add(dsUnits.getValue(i, "unitsid"));
            }
        }
        SQLGenerator sqlGenerator = SQLFactory.getSqlGenerator(this.pageContext);
        QueryProcessor qp = this.getQueryProcessor();
        ArrayList<String> reftypeList = new ArrayList<String>();
        String sql = sqlGenerator.getRefTypeSql();
        DataSet ds = qp.getSqlDataSet(sql.toString());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                reftypeList.add(ds.getValue(i, "reftypeid"));
            }
        }
        html.append("\n<script type=\"text/javascript\" src=\"" + this.elementJSScript + "\"></script>");
        html.append("\n<table cellpadding=\"2\" cellspacing=\"0\" class=\"maintform_table_blue\">");
        html.append("\n<tr>");
        html.append("\n<td colspan=\"3\" class=\"maintform_fieldtitle_blue\">" + tp.translate("Data Type") + "</td>");
        html.append("\n</tr>");
        html.append("\n<tr>");
        html.append("\n<td class=\"maintform_field_blue\" width=\"30\">");
        html.append("\n<input type=\"radio\" id=\"field_datatype_n\" name=\"datatype\" onClick=\"envAttribute.selectType( 'N' );\"");
        html.append(viewMode ? " DISABLED" : "").append(">");
        html.append("\n</td>");
        html.append("<td class=\"maintform_field_blue\" width=\"200\">" + tp.translate("Numeric") + "</td>");
        html.append("<td class=\"maintform_field_blue\" width=\"300\">" + tp.translate("Default Unit") + "&nbsp;");
        if (viewMode) {
            html.append("<input style='border:0' id='field_defaultunits_n'>");
        } else {
            html.append("<select id=\"field_defaultunits_n\" name=\"defaultunits\" onChange=\"envAttribute.updateDD( this );\">");
            html.append("<option></option> ");
            for (j = 0; j < unitsList.size(); ++j) {
                String unit = (String)unitsList.get(j);
                html.append("\n<option value='" + unit + "'>");
                html.append(unit);
                html.append("</option>");
            }
            html.append("</select>");
        }
        html.append("</td>");
        html.append("</tr>");
        html.append("\n<tr>");
        html.append("<td class=\"maintform_field_blue\" width=\"30\">");
        html.append("<input type=\"radio\" id=\"field_datatype_t\" name='datatype' onClick=\"envAttribute.selectType( 'T' );\"");
        html.append(viewMode ? " DISABLED" : "").append(">");
        html.append("\n</td>");
        html.append("<td class=\"maintform_field_blue\" width=\"200\">" + tp.translate("Text") + "</td>");
        html.append("<td class=\"maintform_field_blue\" width=\"200\">&nbsp;</td></tr>");
        html.append("\n<tr><td class=\"maintform_field_blue\" width=\"30\">");
        html.append("<input type=\"radio\" id=\"field_datatype_r\" name=\"datatype\" onClick=\"envAttribute.selectType( 'R' );\"");
        html.append(viewMode ? " DISABLED" : "").append(">").append("</td>");
        html.append("<td class=\"maintform_field_blue\" width=\"200\">" + tp.translate("Reference Type") + "</td>");
        html.append("<td class=\"maintform_field_blue\" width=\"200\">");
        if (viewMode) {
            html.append("<input style=\"border:0\" id=\"field_condreftype_r\">");
        } else {
            html.append("<select id=\"field_condreftype_r\" name=\"condreftype\" onChange=\"envAttribute.updateDD( this );\">");
            html.append("<option></option>");
            for (j = 0; j < reftypeList.size(); ++j) {
                reftype = (String)reftypeList.get(j);
                html.append("<option value='" + reftype + "'>");
                html.append(reftype);
                html.append("</option>");
            }
            html.append("</select>");
        }
        html.append("</td> </tr>");
        html.append("\n<tr>");
        html.append("<td class=\"maintform_field_blue\" width=\"30\">");
        html.append("<input type=\"radio\" id=\"field_datatype_v\" name=\"datatype\" onClick=\"envAttribute.selectType( 'V' );\"");
        html.append(viewMode ? " DISABLED" : "").append(">").append("\n</td>");
        html.append("<td class=\"maintform_field_blue\" width=\"200\">" + tp.translate("Validated Reference Type") + "</td>");
        html.append("<td class=\"maintform_field_blue\" width=\"200\">");
        if (viewMode) {
            html.append("<input style=\"border:0\" id=\"field_condreftype_v\">");
        } else {
            html.append("<select id=\"field_condreftype_v\" name=\"condreftype\" onChange=\"envAttribute.updateDD( this );\">");
            html.append("<option></option>");
            for (j = 0; j < reftypeList.size(); ++j) {
                reftype = (String)reftypeList.get(j);
                html.append("<option value='" + reftype + "'>");
                html.append(reftype);
                html.append("</option>");
            }
            html.append("</select>");
        }
        html.append("</td></tr></table>");
        html.append("<script>\n    envAttribute.storagePopulateData();\n</script>");
        return html.toString();
    }
}

