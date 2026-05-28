/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.opal.util.SdcInfo;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.DataSet;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;

public class DropDownGenerator {
    private static final boolean __Debug = false;

    public static String getSdiDropDown(String sdcId, String dataColumn, String displayColumn, String whereClause, String dropDownName, String dropDownStyle, String currentUser, PageContext pageContext) {
        StringBuffer sbHtml = new StringBuffer("");
        QueryProcessor qp = new QueryProcessor(pageContext);
        DataSet ds = new DataSet();
        String keyid1col = "";
        String sql = "";
        if (sdcId.equalsIgnoreCase("")) {
            return "SdcId not found. Cannot retrieve dropdown values.";
        }
        keyid1col = SdcInfo.getKeyidColumn(1, sdcId, qp);
        if (dataColumn.equalsIgnoreCase("")) {
            dataColumn = keyid1col;
        }
        if (displayColumn.equalsIgnoreCase("")) {
            displayColumn = dataColumn;
        }
        if (dropDownName.equalsIgnoreCase("")) {
            dropDownName = "dd_" + sdcId;
        }
        sql = DropDownGenerator.getSelectStatement(sdcId, dataColumn, displayColumn, whereClause, currentUser, qp);
        ds = qp.getSqlDataSet(sql);
        sbHtml.append(DropDownGenerator.getDropDownHtml(ds, dataColumn, displayColumn, dropDownName, dropDownStyle, pageContext));
        qp = null;
        ds = null;
        return sbHtml.toString();
    }

    private static String getSelectStatement(String sdcId, String dataColumn, String displayColumn, String whereClause, String currentUser, QueryProcessor qp) {
        String sql = "";
        String tableId = "";
        tableId = SdcInfo.getTableId(sdcId, qp);
        if (currentUser.equalsIgnoreCase("")) {
            sql = "select " + dataColumn + ", " + displayColumn + " ";
            sql = sql + "from " + tableId + " ";
        } else {
            sql = "select distinct " + dataColumn + ", " + displayColumn + " from " + tableId + ", sdirole ";
            sql = sql + "where sdirole.roleid in (select roleid from sysuserrole where sysuserid='" + currentUser + "') ";
            sql = sql + "and " + tableId + "." + dataColumn + " = sdirole.keyid1 ";
            sql = sql + "and sdirole.sdcid= '" + sdcId + "' ";
        }
        if (!whereClause.equalsIgnoreCase("")) {
            sql = sql + "where " + whereClause + " ";
        }
        sql = sql + "order by " + dataColumn;
        return sql;
    }

    public static String getSdiDropDown(HashMap hmSdi, String dataColumn, String displayColumn, String dropDownName, String dropDownStyle, PageContext pageContext) {
        String keyid1col = "";
        QueryProcessor qp = new QueryProcessor(pageContext);
        DataSet ds = new DataSet();
        String sdcId = "";
        if (hmSdi.get("sdcid") != null) {
            sdcId = (String)hmSdi.get("sdcid");
        }
        ds = SdcInfo.getDataSetObjectForSdi(hmSdi, pageContext);
        if (sdcId.equalsIgnoreCase("")) {
            return "SdcId not found. Cannot retrieve dropdown values.";
        }
        keyid1col = SdcInfo.getKeyidColumn(1, sdcId, qp);
        if (dataColumn.equalsIgnoreCase("")) {
            dataColumn = keyid1col;
        }
        if (displayColumn.equalsIgnoreCase("")) {
            displayColumn = dataColumn;
        }
        if (dropDownName.equalsIgnoreCase("")) {
            dropDownName = "dd_" + sdcId;
        }
        return DropDownGenerator.getDropDownHtml(ds, dataColumn, displayColumn, dropDownName, dropDownStyle, pageContext);
    }

    public static String getDropDownHtml(DataSet ds, String dataColumn, String displayColumn, String dropDownName, String dropDownStyle, PageContext pageContext) {
        StringBuffer sbHtml = new StringBuffer();
        String dataValue = "";
        String displayValue = "";
        if (ds.getRowCount() >= 1) {
            sbHtml.append("<select name=\"" + dropDownName + "\" id=\"" + dropDownName + "\" class=\"" + dropDownStyle + "\">\n");
            TranslationProcessor tp = new TranslationProcessor(pageContext);
            sbHtml.append("<option value=\"\">").append(tp.translate("-- None --")).append("</option>\n");
            for (int i = 0; i < ds.getRowCount(); ++i) {
                dataValue = ds.getValue(i, dataColumn);
                displayValue = ds.getValue(i, displayColumn);
                if (displayValue.equalsIgnoreCase("")) {
                    displayValue = dataValue;
                }
                sbHtml.append("<option value=\"" + dataValue + "\">" + SafeHTML.encodeForHTML(displayValue) + "</option>\n");
            }
            sbHtml.append("</select>\n");
        }
        return sbHtml.toString();
    }

    public static String getTemplateDropDown(String templateString, String dropDownName, String dropDownStyle, String eventString, boolean enableNone) {
        StringBuffer sbHtml = new StringBuffer();
        sbHtml.append("<select name=\"" + dropDownName + "\" id=\"" + dropDownName + "\" ");
        if (!eventString.equalsIgnoreCase("")) {
            sbHtml.append(eventString);
        }
        if (!dropDownStyle.equalsIgnoreCase("")) {
            sbHtml.append(" class=\"" + dropDownStyle + "\" ");
        }
        sbHtml.append(" >\n");
        if (enableNone) {
            sbHtml.append("\t<option value=\"None\">-- None --</option>\n");
        }
        if (!templateString.equalsIgnoreCase("")) {
            String[] arrTemplates = StringUtil.split(templateString, ";");
            for (int i = 0; i < arrTemplates.length; ++i) {
                sbHtml.append("\t<option value=\"" + arrTemplates[i] + "\">" + arrTemplates[i] + "</option>\n");
            }
        }
        sbHtml.append("</select>\n");
        return sbHtml.toString();
    }
}

