/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.pageelements.ElementUtil;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetAccessionSummary
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        StringBuilder sb = new StringBuilder();
        String sampleid = ajaxResponse.getRequestParameter("sampleid");
        if (OpalUtil.isNotEmpty(sampleid)) {
            String param_columns = ajaxResponse.getRequestParameter("columns");
            PropertyListCollection columns = new PropertyListCollection();
            try {
                columns.setJSONString(param_columns);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            StringBuilder sql = new StringBuilder();
            if (columns.size() > 0) {
                sql.append("select ");
                for (int colindex = 0; colindex < columns.size(); ++colindex) {
                    PropertyList columnList = columns.getPropertyList(colindex);
                    String columnid = columnList.getProperty("columnid").trim();
                    if (!OpalUtil.isNotEmpty(columnid)) continue;
                    sql.append(columnid).append(",");
                }
                sql.append(" (select sm.sourcesampleid from s_samplemap sm where sm.destsampleid = s_sample.s_sampleid) parentsampleid");
                sql.append(" from s_sample");
                DataSet ds = null;
                SafeSQL safeSQL = new SafeSQL();
                if (StringUtil.split(sampleid, ";").length > 1000) {
                    try {
                        String rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
                        sql.append(" where s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
                        sql.append(" order by s_sampleid");
                        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                        if (OpalUtil.isNotEmpty(rsetid)) {
                            this.getDAMProcessor().clearRSet(rsetid);
                        }
                    }
                    catch (SapphireException e) {
                        Logger.logError("Error creating RSET: " + e.getMessage());
                    }
                } else {
                    sql.append(" where s_sampleid in (").append(safeSQL.addIn(sampleid, ";")).append(")");
                    sql.append(" order by s_sampleid");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                }
                if (ds != null) {
                    sb.append("<table cellpadding='5px' cellspacing='0' style='border-bottom: 1px solid #c2cbd0;border-right: 1px solid #c2cbd0;' id='table_summary'>");
                    sb.append("<thead>");
                    sb.append("<tr class='list_tableheadrow'>");
                    for (int i = 0; i < columns.size(); ++i) {
                        PropertyList columnList = columns.getPropertyList(i);
                        String columnid = columnList.getProperty("columnid");
                        if ("Hidden".equals(columnList.getProperty("mode", "Display Value"))) continue;
                        String title = columnList.getProperty("title", columnid);
                        if (i == 0) {
                            sb.append("<th colspan='2'>");
                        } else {
                            sb.append("<th>");
                        }
                        sb.append(this.getTranslationProcessor().translate(title));
                        sb.append("</th>");
                    }
                    sb.append("</tr>");
                    sb.append("</thead>");
                    sb.append("<tbody>");
                    ArrayList<String> parentSampleList = new ArrayList<String>();
                    if (ds.size() > 0) {
                        DataSet data = new DataSet();
                        DataSet child = new DataSet();
                        for (int i = 0; i < ds.size(); ++i) {
                            String parentsampleid = ds.getString(i, "parentsampleid", "");
                            if (OpalUtil.isEmpty(parentsampleid)) {
                                data.copyRow(ds, i, 1);
                                parentSampleList.add(ds.getString(i, "s_sampleid"));
                                continue;
                            }
                            child.copyRow(ds, i, 1);
                        }
                        boolean even = true;
                        for (int colindex = 0; colindex < columns.size(); ++colindex) {
                            PropertyList column = columns.getPropertyList(colindex);
                            String dateformat = column.getProperty("dateformat", "");
                            if (!OpalUtil.isNotEmpty(dateformat)) continue;
                            String columnid = column.getProperty("columnid");
                            if (OpalUtil.isNotEmpty(columnid) && columnid.contains(" ")) {
                                columnid = columnid.substring(columnid.lastIndexOf(" ")).trim();
                            }
                            if (!data.isValidColumn(columnid) || 2 != data.getColumnType(columnid)) continue;
                            data.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), dateformat));
                        }
                        for (int i = 0; i < data.size(); ++i) {
                            sb.append("<tr class='").append(even ? "list_tableroweven" : "list_tablerowodd").append("'>");
                            for (int col = 0; col < columns.size(); ++col) {
                                PropertyList column = columns.getPropertyList(col);
                                String columnid = column.getProperty("columnid");
                                if ("Hidden".equals(column.getProperty("mode", "Display Value"))) continue;
                                String dbcolumn = columnid;
                                if (dbcolumn.trim().startsWith("(")) {
                                    dbcolumn = dbcolumn.substring(dbcolumn.lastIndexOf(" ")).trim();
                                }
                                String value = data.getValue(i, dbcolumn, "&nbsp;");
                                String linkurl = column.getProperty("linkurl");
                                if (OpalUtil.isNotEmpty(linkurl)) {
                                    String[] tokens;
                                    for (String token : tokens = StringUtil.getTokens(linkurl)) {
                                        if (!data.isValidColumn(token)) continue;
                                        linkurl = StringUtil.replaceAll(linkurl, "[" + token + "]", data.getValue(i, token, ""));
                                    }
                                    value = "<a href=\"javascript:openLinkURL( '" + linkurl + "' )\">" + value + "</a>";
                                }
                                sb.append("<td class='list_tablebodycell' ").append(col == 0 ? "colspan='2'" : "").append(">");
                                if (value.contains("</script")) {
                                    sb.append(SafeHTML.encodeForHTML(value));
                                } else {
                                    sb.append(value);
                                }
                                if ("storagestatus".equals(columnid) && "Received".equals(data.getValue(i, columnid))) {
                                    sb.append(" <img class='approvalError' src='rc?command=image&image=FlatBlackInformation2&size=16' style='cursor:pointer;' sampleid=\"").append(ds.getString(i, "s_sampleid")).append("\">");
                                }
                                sb.append("</td>");
                            }
                            sb.append("</tr>");
                            boolean bl = even = !even;
                            if (child.size() <= 0) continue;
                            child.setString(-1, "hasparent", "N");
                            String s_sampleid = data.getString(i, "s_sampleid");
                            DataSet _ds = new DataSet();
                            for (int childrow = 0; childrow < child.size(); ++childrow) {
                                if (!s_sampleid.equals(child.getString(childrow, "parentsampleid"))) continue;
                                _ds.copyRow(child, childrow, 1);
                            }
                            for (int colindex = 0; colindex < columns.size(); ++colindex) {
                                PropertyList column = columns.getPropertyList(colindex);
                                String dateformat = column.getProperty("dateformat", "");
                                if (!OpalUtil.isNotEmpty(dateformat)) continue;
                                String columnid = column.getProperty("columnid");
                                if (OpalUtil.isNotEmpty(columnid) && columnid.contains(" ")) {
                                    columnid = columnid.substring(columnid.lastIndexOf(" ")).trim();
                                }
                                if (!_ds.isValidColumn(columnid) || 2 != _ds.getColumnType(columnid)) continue;
                                _ds.setDateDisplayFormat(columnid, ElementUtil.getDateFormat(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), dateformat));
                            }
                            for (int j = 0; j < _ds.size(); ++j) {
                                sb.append("<tr class='").append(even ? "list_tableroweven" : "list_tablerowodd").append("'>");
                                sb.append("<td class='list_tablebodycell' style='width:24px;position:relative;'>");
                                if (j < _ds.size() - 1) {
                                    sb.append("<div class='treebranch'><div class='tl'></div><div class='tr'></div></div>");
                                } else {
                                    sb.append("<div class='treebranchlast'><div class='tl'></div><div class='tr'></div></div>");
                                }
                                sb.append("</td>");
                                for (int col = 0; col < columns.size(); ++col) {
                                    PropertyList columnList = columns.getPropertyList(col);
                                    if ("Hidden".equals(columnList.getProperty("mode", "Display Value"))) continue;
                                    String dbcolumn = columnList.getProperty("columnid");
                                    if (dbcolumn.trim().contains(" ")) {
                                        dbcolumn = dbcolumn.substring(dbcolumn.lastIndexOf(" ")).trim();
                                    }
                                    String value = _ds.getValue(j, dbcolumn, "&nbsp;");
                                    String linkurl = columnList.getProperty("linkurl");
                                    if (OpalUtil.isNotEmpty(linkurl)) {
                                        String[] tokens;
                                        for (String token : tokens = StringUtil.getTokens(linkurl)) {
                                            if (!_ds.isValidColumn(token)) continue;
                                            linkurl = StringUtil.replaceAll(linkurl, "[" + token + "]", _ds.getValue(j, token, ""));
                                        }
                                        value = "<a href=\"javascript:openLinkURL( '" + linkurl + "' )\">" + value + "</a>";
                                    }
                                    sb.append("<td class='list_tablebodycell'>").append(value).append("</td>");
                                }
                                sb.append("</tr>");
                                even = !even;
                            }
                        }
                        if (child.size() > 0) {
                            DataSet _ds = new DataSet();
                            for (int childrow = 0; childrow < child.size(); ++childrow) {
                                if (parentSampleList.contains(child.getString(childrow, "parentsampleid"))) continue;
                                _ds.copyRow(child, childrow, 1);
                            }
                            if (_ds.size() > 0) {
                                _ds.sort("parentsampleid");
                                ArrayList<DataSet> list = _ds.getGroupedDataSets("parentsampleid");
                                for (DataSet d : list) {
                                    sb.append("<tr class='").append(even ? "list_tableroweven" : "list_tablerowodd").append("'>");
                                    sb.append("<td class='list_tablebodycell' colspan='").append(columns.size() + 2).append("'>");
                                    sb.append(d.getString(0, "parentsampleid"));
                                    sb.append(" (").append(this.getTranslationProcessor().translate("Parent sample not received in this session")).append(")");
                                    sb.append("</td></tr>");
                                    even = !even;
                                    for (int childrow = 0; childrow < d.size(); ++childrow) {
                                        sb.append("<tr class='").append(even ? "list_tableroweven" : "list_tablerowodd").append("'>");
                                        sb.append("<td class='list_tablebodycell' style='width:20px;position:relative;'>");
                                        if (childrow < d.size() - 1) {
                                            sb.append("<div class='treebranch'><div class='tl'></div><div class='tr'></div></div>");
                                        } else {
                                            sb.append("<div class='treebranchlast'><div class='tl'></div><div class='tr'></div></div>");
                                        }
                                        sb.append("</td>");
                                        for (int col = 0; col < columns.size(); ++col) {
                                            PropertyList column = columns.getPropertyList(col);
                                            if ("Hidden".equals(column.getProperty("mode", "Display Value"))) continue;
                                            String columnid = column.getProperty("columnid");
                                            String value = d.getValue(childrow, columnid, "&nbsp;");
                                            String linkurl = column.getProperty("linkurl");
                                            if (OpalUtil.isNotEmpty(linkurl)) {
                                                String[] tokens;
                                                for (String token : tokens = StringUtil.getTokens(linkurl)) {
                                                    if (!d.isValidColumn(token)) continue;
                                                    linkurl = StringUtil.replaceAll(linkurl, "[" + token + "]", d.getValue(childrow, token, ""));
                                                }
                                                value = "<a href=\"javascript:openLinkURL( '" + linkurl + "' )\">" + value + "</a>";
                                            }
                                            sb.append("<td class='list_tablebodycell'>").append(value).append("</td>");
                                        }
                                        sb.append("</tr>");
                                        even = !even;
                                    }
                                }
                            }
                        }
                    }
                    sb.append("</tbody>");
                    sb.append("</table>");
                } else {
                    sb.append("Error fetching records");
                }
            }
        }
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }
}

