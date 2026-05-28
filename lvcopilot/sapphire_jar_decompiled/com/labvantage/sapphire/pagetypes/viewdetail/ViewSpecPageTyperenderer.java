/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pagetypes.viewdetail;

import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.SapphireException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.tagext.PageTagInfo;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ViewSpecPageTyperenderer {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    public static String getHtml(PropertyList pagedata, PageContext pageContext, PageTagInfo pageinfo, String sdcid, String keyid1, String keyid2, String keyid3, boolean oosOnly, String pageTitle) throws SapphireException {
        PropertyList parent = pagedata.getPropertyList("parent");
        PropertyList primary = pagedata.getPropertyList("primary");
        PropertyList specdetail = pagedata.getPropertyList("specdetail");
        String parentSdcId = parent.getProperty("sdcid");
        String primarySdcId = primary.getProperty("sdcid");
        StringBuffer bodyHtml = new StringBuffer();
        StringBuffer titleHtml = new StringBuffer();
        String rsetId = "";
        String detailTable = "sdispec";
        TranslationProcessor tp = new TranslationProcessor(pageContext);
        QueryProcessor qp = pageinfo.getQueryProcessor();
        SDCProcessor sdp = new SDCProcessor(pageContext);
        DAMProcessor dp = new DAMProcessor(pageContext);
        StringBuffer sql = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        String primarytableId = sdp.getProperty(primarySdcId, "tableid");
        DataSet dsPrimary = new DataSet();
        String primaryKeycolid1 = sdp.getProperty(primarySdcId, "keycolid1");
        PropertyListCollection specColumns = specdetail.getCollection("columns");
        DataSet dsRefValues = qp.getRefTypeDataSet("Spec Condition");
        PropertyListCollection primaryColumns = primary.getCollection("columns");
        int primarycolSize = primaryColumns.size();
        if (parentSdcId.equals(sdcid)) {
            DataSet dsParent;
            String parentTable = sdp.getProperty(parentSdcId, "tableid");
            String parentkeycolid1 = sdp.getProperty(parentSdcId, "keycolid1");
            String parentkeycolid2 = sdp.getProperty(parentSdcId, "keycolid2");
            String parentkeycolid3 = sdp.getProperty(parentSdcId, "keycolid3");
            PropertyListCollection parentColumns = parent.getCollection("columns");
            int colSize = parentColumns.size();
            sql.append("SELECT ").append(parentTable).append(".*");
            for (int i = 0; i < colSize; ++i) {
                PropertyList column = parentColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (!columnid.startsWith("(")) continue;
                sql.append(",").append(columnid).append(",");
            }
            if (sql.toString().endsWith(",")) {
                sql.setLength(sql.length() - 1);
            }
            sql.append(" FROM ").append(parentTable).append(" WHERE ").append(parentkeycolid1).append(" = ").append(safeSQL.addVar(keyid1));
            if (parentkeycolid2.length() > 0) {
                sql.append(" ").append(parentkeycolid2).append(" = ").append(safeSQL.addVar(keyid2));
            }
            if (parentkeycolid3.length() > 0) {
                sql.append(" ").append(parentkeycolid3).append(" = ").append(safeSQL.addVar(keyid3));
            }
            if ((dsParent = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null && dsParent.getRowCount() > 0) {
                String[] dsparentCols = dsParent.getColumns();
                for (int col = 0; col < dsparentCols.length; ++col) {
                    if (!pageTitle.contains("[" + dsparentCols[col] + "]")) continue;
                    pageTitle = StringUtil.replaceAll(pageTitle, "[" + dsparentCols[col] + "]", dsParent.getValue(0, dsparentCols[col]));
                }
            }
            sql.setLength(0);
            safeSQL.reset();
            sql.append("SELECT sdccolumnid, sdccolumnid2, sdccolumnid3 FROM sdclink WHERE  sdcid = ").append(safeSQL.addVar(primarySdcId)).append(" AND linksdcid = ").append(safeSQL.addVar(parentSdcId));
            DataSet dsLink = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            String sdccolumnid = dsLink.getValue(0, "sdccolumnid");
            String sdccolumnid2 = dsLink.getValue(0, "sdccolumnid2");
            String sdccolumnid3 = dsLink.getValue(0, "sdccolumnid3");
            sql.setLength(0);
            safeSQL.reset();
            sql.append("SELECT ");
            for (int i = 0; i < primarycolSize; ++i) {
                PropertyList column = primaryColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                sql.append(columnid);
                if (i >= primarycolSize - 1) continue;
                sql.append(",");
            }
            sql.append(" FROM ").append(primarytableId).append(" WHERE ").append(sdccolumnid).append(" = ").append(safeSQL.addVar(keyid1));
            if (sdccolumnid2.length() > 0) {
                sql.append(" ").append(sdccolumnid2).append(" = ").append(safeSQL.addVar(keyid2));
            }
            if (sdccolumnid3.length() > 0) {
                sql.append(" ").append(sdccolumnid3).append(" = ").append(safeSQL.addVar(keyid3));
            }
            sql.append(" ORDER BY ").append(sdccolumnid);
            if (sdccolumnid2.length() > 0) {
                sql.append(",").append(sdccolumnid2);
            }
            if (sdccolumnid3.length() > 0) {
                sql.append(",").append(sdccolumnid3);
            }
            if ((keyid1 = (dsPrimary = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())).getColumnValues(primaryKeycolid1, ";")) == null || keyid1.length() <= 0) {
                HashMap<String, String> token = new HashMap<String, String>();
                token.put("primarysdi", sdp.getProperty(primarySdcId, "singular"));
                token.put("parentsdi", sdp.getProperty(parentSdcId, "singular"));
                token.put("keyid1", dsParent.getValue(0, parentkeycolid1));
                throw new SapphireException(tp.translate("No [primarysdi] exists in the [parentsdi] [keyid1]", token));
            }
            rsetId = dp.createRSet(primarySdcId, keyid1, null, null);
            bodyHtml.append("<table class=\"gridmaint_table\" cellpadding=\"3\" cellspacing=\"0\" style=\"width:99%;height:10%;display:table;\" align=\"center\">");
            bodyHtml.append("<tbody>");
            int parentColSize = parentColumns.size();
            for (int i = 0; i < parentColSize; ++i) {
                if (i % 2 == 0) {
                    bodyHtml.append("<tr>");
                }
                PropertyList column = parentColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                String title = column.getProperty("title", columnid);
                String dispValue = column.getProperty("displayvalue");
                if (columnid.startsWith("(")) {
                    columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                    if (title.startsWith("(")) {
                        title = columnid;
                    }
                }
                int colspan = 1;
                if (i == parentColSize - 1) {
                    colspan = 3;
                }
                bodyHtml.append("<td  class=\"maintform_fieldtitle\" width=15% style=\"background-color:#F0F0F0\" valign=top nowrap>").append(tp.translate(title)).append("</td>");
                bodyHtml.append("<td  width=35% class=\"maintform_field\" colspan=").append(colspan).append(" valign=top>");
                if (columnid.contains("condition")) {
                    String condition = dsParent.getValue(0, columnid);
                    int f = dsRefValues.findRow("refvalueid", condition);
                    String refDispIcon = "";
                    if (f > -1) {
                        refDispIcon = dsRefValues.getValue(f, "refdisplayicon");
                    }
                    if (refDispIcon.length() > 0) {
                        bodyHtml.append("<img src=\"" + refDispIcon + "\" title=\"" + condition + "\"/>");
                    } else {
                        bodyHtml.append(condition);
                    }
                } else {
                    String colValue = dsParent.getValue(0, columnid);
                    if (dispValue.trim().length() > 0) {
                        colValue = ViewSpecPageTyperenderer.resolveDisplayValue(dispValue, colValue);
                    }
                    bodyHtml.append(colValue);
                }
                bodyHtml.append("</td>");
                if (i % 2 > 0) {
                    bodyHtml.append("</tr>");
                    continue;
                }
                if (i != parentColSize - 1) continue;
                bodyHtml.append("</tr>");
            }
            bodyHtml.append("</tbody>");
            bodyHtml.append("</table>");
        } else if (primarySdcId.equals(sdcid)) {
            safeSQL.reset();
            rsetId = dp.createRSet(primarySdcId, keyid1, null, null);
            sql.setLength(0);
            sql.append("SELECT ");
            for (int i = 0; i < primarycolSize; ++i) {
                PropertyList column = primaryColumns.getPropertyList(i);
                String columnid = column.getProperty("columnid");
                if (columnid.startsWith("(")) {
                    sql.append(columnid);
                } else {
                    sql.append(primarytableId).append(".").append(columnid);
                }
                if (i >= primarycolSize - 1) continue;
                sql.append(",");
            }
            sql.append(" FROM ").append(primarytableId).append(" , rsetitems r ").append(" WHERE r.sdcid = ").append(safeSQL.addVar(primarySdcId)).append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND ").append(primarytableId).append(".").append(primaryKeycolid1).append(" = r.keyid1").append(" ORDER BY ").append(primaryKeycolid1);
            dsPrimary = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        sql.setLength(0);
        safeSQL.reset();
        sql.append(" SELECT DISTINCT t.* FROM " + detailTable + " t,  rsetitems r WHERE r.sdcid = ").append(safeSQL.addVar(primarySdcId)).append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND t.sdcid = r.sdcid AND t.keyid1 = r.keyid1 AND t.appliedflag = 'Y' ").append(oosOnly ? " AND t.oosgeneratingflag = 'Y'" : "").append(" ORDER BY t.keyid1");
        DataSet dsSpecs = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        sql.setLength(0);
        safeSQL.reset();
        sql.append(" SELECT DISTINCT s.* FROM spec s, ").append(detailTable).append(" t, rsetitems r WHERE r.sdcid = ").append(safeSQL.addVar(primarySdcId)).append(" AND r.rsetid = ").append(safeSQL.addVar(rsetId)).append(" AND t.keyid1 = r.keyid1 AND s.specid = t.specid AND s.specversionid = t.specversionid AND t.appliedflag = 'Y' ").append(oosOnly ? " AND ( s.oosgeneratingflag = 'Y' OR EXISTS ( SELECT 1 FROM sdispec WHERE sdcid = t.sdcid AND keyid1 = t.keyid1 AND t.oosgeneratingflag = 'Y' ) )" : "").append(" ORDER BY s.specid, s.specversionid");
        DataSet specs = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (specs.getRowCount() == 0) {
            throw new SapphireException(tp.translate("No Specification exists."));
        }
        bodyHtml.append("<br><div id= \"specdetail_wrapper\" width=98% >");
        bodyHtml.append("<table id= \"specdetail_table\" cellpadding=\"3\" cellspacing=\"0\"  style=\"width:100%;display:table;\" align=\"center\">");
        bodyHtml.append("<thead><tr id =\"specRow0\"><td class=\"maintform_fieldtitle\" style=\"background-color:#F0F0F0\" valign=top width=\"90px\">").append(tp.translate("Specifications")).append("</td>");
        for (int r = 0; r < specs.getRowCount(); ++r) {
            String specId = specs.getValue(r, "specid");
            String specVersionId = specs.getValue(r, "specversionid");
            bodyHtml.append("<td class=\"maintform_fieldtitle\"   style=\"background-color:#F0F0F0\"  width=\"90px\" valign=top nowrap>").append(specId).append("(").append(specVersionId).append(")</td>");
        }
        bodyHtml.append("</tr>");
        for (int c = 0; c < specColumns.size(); ++c) {
            PropertyList column = specColumns.getPropertyList(c);
            String columnid = column.getProperty("columnid");
            String titleId = column.getProperty("title", columnid);
            String dispValue = column.getProperty("displayvalue");
            bodyHtml.append("<tr id =\"specRow").append(c + 1).append("\"><td class=\"maintform_fieldtitle\" width=\"90px\"  style=\"background-color:#F0F0F0;\" valign=top nowrap> " + tp.translate(titleId) + "</td>");
            for (int r = 0; r < specs.getRowCount(); ++r) {
                String colValue = specs.getValue(r, columnid);
                if (dispValue.trim().length() > 0) {
                    colValue = ViewSpecPageTyperenderer.resolveDisplayValue(dispValue, colValue);
                }
                bodyHtml.append("<td class=\"maintform_field\" width=\"90px\" valign=\"top\"  nowrap> ").append(SafeHTML.encodeForHTML(colValue)).append("</td>");
            }
            bodyHtml.append("</tr>");
        }
        int totalWidth = 0;
        StringBuffer sampleHeaders = new StringBuffer();
        for (int c = 0; c < primaryColumns.size(); ++c) {
            PropertyList column = primaryColumns.getPropertyList(c);
            String columnid = column.getProperty("columnid");
            String titleId = column.getProperty("title", columnid);
            String width = column.getProperty("width", "90");
            totalWidth += Integer.parseInt(width);
            if (columnid.startsWith("(")) {
                columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                if (titleId.startsWith("(")) {
                    titleId = columnid;
                }
            }
            sampleHeaders.append("<td  class=\"maintform_fieldtitle\" style=\"background-color:#F0F0F0;width:" + width + "px; \" valign=top nowrap>").append(titleId).append("</td>");
        }
        bodyHtml.append("<tr><td style=\"padding: 0px;\" align=left width=" + totalWidth + "px>");
        bodyHtml.append("<table id=\"sampleHeaders\" cellpadding =\"3\" cellspacing = \"0\" style=\"width:100%;height:100%;table-layout:fixed;border-collapse: collapse;\"><tr>");
        bodyHtml.append(sampleHeaders).append("</tr></table>");
        bodyHtml.append("<td colspan=\"").append(specs.getRowCount()).append("\"  class=\"maintform_field\" style=\"background-color:#F0F0F0\">&nbsp;</td></tr>");
        bodyHtml.append("</thead><tbody>");
        for (int r = 0; r < dsPrimary.getRowCount(); ++r) {
            bodyHtml.append("<tr>");
            bodyHtml.append("<td style=\"padding: 0px;\" align=left width=\"").append(totalWidth).append("px\"><table  cellpadding = \"3\" cellspacing = \"0\" style=\"width:100%;table-layout:fixed;;border-collapse: collapse;\"><tr>");
            String primaryKeyId1 = "";
            for (int c = 0; c < primaryColumns.size(); ++c) {
                PropertyList column = primaryColumns.getPropertyList(c);
                String columnid = column.getProperty("columnid");
                String titleId = column.getProperty("title", columnid);
                String width = column.getProperty("width", "90");
                String dispValue = column.getProperty("displayvalue");
                if (columnid.startsWith("(")) {
                    columnid = columnid.substring(columnid.lastIndexOf(")") + 1).trim();
                    if (titleId.startsWith("(")) {
                        titleId = columnid;
                    }
                }
                String colValue = dsPrimary.getValue(r, columnid);
                if (columnid.equals(primaryKeycolid1)) {
                    primaryKeyId1 = colValue;
                }
                if (dispValue.trim().length() > 0) {
                    colValue = ViewSpecPageTyperenderer.resolveDisplayValue(dispValue, colValue);
                }
                bodyHtml.append("<td class=\"maintform_field\" valign=top style=\"width:" + width + "px;overflow:hidden;word-wrap: break-word;\">").append(SafeHTML.encodeForHTMLAttribute(colValue)).append("</td>");
            }
            bodyHtml.append("</tr></table></td>");
            for (int s = 0; s < specs.getRowCount(); ++s) {
                String oosFlag = specs.getValue(s, "oosgeneratingflag");
                HashMap<String, String> findSpec = new HashMap<String, String>();
                findSpec.put("keyid1", primaryKeyId1);
                findSpec.put("specid", specs.getValue(s, "specid"));
                findSpec.put("specversionid", specs.getValue(s, "specversionid"));
                int findRow = dsSpecs.findRow(findSpec);
                if (findRow > -1) {
                    bodyHtml.append("<td class=\"maintform_field\"  style=\"padding: 0px;\"  valign=center>");
                    String condition = dsSpecs.getValue(findRow, "condition");
                    String sdispec_oos = dsSpecs.getValue(findRow, "oosgeneratingflag");
                    int f = dsRefValues.findRow("refvalueid", condition);
                    String refDispIcon = "";
                    if (f > -1) {
                        refDispIcon = dsRefValues.getValue(f, "refdisplayicon");
                    }
                    if (refDispIcon.length() > 0) {
                        bodyHtml.append("<span valign=center style=\"color:#000000;font-weight:600;font-size:10px;padding:12px\"><a href=\"javascript:displayDiv('" + primarySdcId + "','" + primaryKeyId1 + "','" + specs.getValue(s, "specid") + "','" + specs.getValue(s, "specversionid") + "')\" title = \"" + tp.translate("View Data Details") + "\"> <img src=\"" + refDispIcon + "\" title=\"" + condition + "\" /></a>");
                    } else {
                        bodyHtml.append(condition);
                    }
                    if (!oosFlag.equalsIgnoreCase(sdispec_oos)) {
                        if ("Y".equalsIgnoreCase(sdispec_oos)) {
                            bodyHtml.append("&nbsp;(" + tp.translate("OOS") + ")");
                        } else {
                            bodyHtml.append("&nbsp;(" + tp.translate("Non OOS") + ")");
                        }
                    }
                    bodyHtml.append("</span>");
                } else {
                    bodyHtml.append("<td class=\"maintform_field\" style=\"background-color:#DCDCDC;\" align=center>");
                    bodyHtml.append("&nbsp;");
                }
                bodyHtml.append("</td>");
            }
            bodyHtml.append("</tr>");
        }
        bodyHtml.append("</tbody></table></div>");
        titleHtml.append("<table cellpadding=10 cellspacing=0 width=100% height=5%>").append("<tr height=\"8\"><td class=layout_pagetitle>&nbsp;").append(pageTitle).append("</td></tr><tr></table>").append("<table width=35% border=0><tr>").append("<td style=\"background-color:#f9f9f9; color: #000000; font-weight:600;font-family:font-size: 12px;\"nowrap align=\"left\"  width=\"auto\">&nbsp;&nbsp;&nbsp;").append(tp.translate("Include Only OOS Generating Specs")).append("&nbsp;&nbsp;<input id=\"onlyoos\" type = \"checkbox\" ").append(oosOnly ? "checked" : "");
        titleHtml.append(" onclick = \"reloadPage(this);\"></td></tr></table>");
        return titleHtml.toString() + bodyHtml.toString();
    }

    private static String resolveDisplayValue(String dispValue, String value) {
        String[] dispValueArray = StringUtil.split(dispValue, ";");
        for (int r = 0; r < dispValueArray.length; ++r) {
            String[] dispValues = StringUtil.split(dispValueArray[r], "=");
            if (!value.equals(dispValues[0])) continue;
            return dispValues[1];
        }
        Logger.logInfo("Display Value could not be resolved: " + dispValue);
        return value;
    }
}

