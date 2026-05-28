/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetConsolidationActivityHTML
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        StringBuilder sb = new StringBuilder();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        try {
            int i;
            JSONArray movedata = new JSONArray(ajaxResponse.getRequestParameter("movedata"));
            JSONArray columns = new JSONArray(ajaxResponse.getRequestParameter("columns"));
            boolean print = "print".equals(ajaxResponse.getRequestParameter("mode"));
            HashMap trackitemMap = new HashMap();
            ArrayList<String> trackitems = new ArrayList<String>();
            for (i = 0; i < movedata.length(); ++i) {
                JSONObject data = movedata.getJSONObject(i);
                String trackitemid = data.getString("trackitemid");
                trackitems.add(trackitemid);
                HashMap<String, String> labelMap = new HashMap<String, String>();
                labelMap.put("sourcelabelpath", data.getString("sourcelabelpath"));
                labelMap.put("targetlabelpath", data.getString("targetlabelpath"));
                trackitemMap.put(trackitemid, labelMap);
            }
            if (trackitems.size() > 0) {
                if (print) {
                    sb.append("<style>");
                    sb.append("body { font-family:sans-serif; }");
                    sb.append("table.thinBorderTable {");
                    sb.append("    border: 1px solid lightgrey;");
                    sb.append("}");
                    sb.append(".thinBorderCell {");
                    sb.append("    border: 1px solid lightgrey;");
                    sb.append("    padding:4px;");
                    sb.append("}");
                    sb.append("</style>");
                    sb.append("<table cellpadding=4 cellspacing=0 border=0>");
                    sb.append("<tr><td colspan=2 style='font-weight:bold;font-size:18px;'>").append(ajaxResponse.getRequestParameter("text_ActivityTitle", "Cosolidation Activity")).append("</td></tr>");
                    sb.append("<tr><td>").append(this.getTranslationProcessor().translate("Printed By: </td><td>")).append(this.getConnectionProcessor().getSapphireConnection().getSysuserName()).append("</td></tr>");
                    sb.append("</tr>");
                    sb.append("</table>");
                    sb.append("<hr>");
                }
                sb.append("<table class='thinBorderTable' cellpadding=0 cellspacing=0 border=0>");
                sb.append("<thead><tr>");
                if (print) {
                    sb.append("<th class='thinBorderCell'>&nbsp;</th>");
                }
                for (i = 0; i < columns.length(); ++i) {
                    JSONObject column = columns.getJSONObject(i);
                    String mode = column.getString("mode");
                    if (OpalUtil.isEmpty(mode)) {
                        mode = "Activity Only";
                    }
                    if (print) {
                        if (!"Both".equals(mode) && !"Print Only".equals(mode)) continue;
                        sb.append("<th class='thinBorderCell'>").append(column.getString("title")).append("</th>");
                        continue;
                    }
                    if (!"Both".equals(mode) && !"Activity Only".equals(mode)) continue;
                    sb.append("<th class='thinBorderCell'>").append(column.getString("title")).append("</th>");
                }
                sb.append("<th class='thinBorderCell'>").append(ajaxResponse.getRequestParameter("text_From", this.getTranslationProcessor().translate("From"))).append("</th>");
                sb.append("<th class='thinBorderCell'>").append(ajaxResponse.getRequestParameter("text_To", this.getTranslationProcessor().translate("To"))).append("</th>");
                if (!print) {
                    sb.append("<th class='thinBorderCell'><img src='rc?command=image&image=FlatBlackReplyAll' style='cursor: pointer' onclick='undoTrackItemMove(\"ALL\")' title='Undo All'></th>");
                }
                sb.append("</tr></thead>");
                sb.append("<tbody>");
                SafeSQL safeSQL = new SafeSQL();
                StringBuilder sql = new StringBuilder();
                sql.append("select trackitemid");
                for (int i2 = 0; i2 < columns.length(); ++i2) {
                    JSONObject column = columns.getJSONObject(i2);
                    sql.append(",").append(column.getString("columnid"));
                }
                sql.append(" from trackitem");
                sql.append(" where trackitemid in (").append(safeSQL.addIn(trackitems)).append(")");
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                if (ds.size() > 0) {
                    int index = 0;
                    HashMap<String, String> filter = new HashMap<String, String>();
                    for (String trackitemid : trackitems) {
                        filter.put("trackitemid", trackitemid);
                        int row = ds.findRow(filter);
                        if (row == -1) continue;
                        sb.append("<tr>");
                        if (print) {
                            sb.append("<td class='thinBorderCell'>").append(++index).append(".</td>");
                        }
                        for (int col = 0; col < columns.length(); ++col) {
                            JSONObject column = columns.getJSONObject(col);
                            String columnid = column.getString("columnid");
                            if (columnid.contains(" ")) {
                                columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
                            }
                            String value = ds.getValue(row, columnid);
                            String mode = column.getString("mode");
                            if (OpalUtil.isEmpty(mode)) {
                                mode = "Activity Only";
                            }
                            if (print) {
                                if (!"Both".equals(mode) && !"Print Only".equals(mode)) continue;
                                sb.append("<td class='thinBorderCell'>").append(value).append("</td>");
                                continue;
                            }
                            if (!"Both".equals(mode) && !"Activity Only".equals(mode)) continue;
                            sb.append("<td class='thinBorderCell'>").append(value).append("</td>");
                        }
                        sb.append("<td class='thinBorderCell'>").append((String)((Map)trackitemMap.get(trackitemid)).get("sourcelabelpath")).append("</td>");
                        sb.append("<td class='thinBorderCell'>").append((String)((Map)trackitemMap.get(trackitemid)).get("targetlabelpath")).append("</td>");
                        if (!print) {
                            sb.append("<td class='thinBorderCell'><img src='rc?command=image&image=FlatBlackReply' style='cursor: pointer' onclick='undoTrackItemMove(\"").append(trackitemid).append("\")' title='Undo'></td>");
                        }
                        sb.append("</tr>");
                    }
                }
                sb.append("</tbody></table>");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }
}

