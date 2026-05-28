/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.codec.digest.DigestUtils
 */
package com.labvantage.sapphire.pageelements.list;

import com.labvantage.sapphire.Cache;
import com.labvantage.sapphire.tagext.SDITag;
import com.labvantage.sapphire.tagext.SDITagUtil;
import com.labvantage.sapphire.util.groovy.GroovyUtil;
import com.labvantage.sapphire.util.json.JSONUtil;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONObject;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ListAjaxRequest
extends BaseAjaxRequest {
    private static Cache deferedColumnMap = new Cache("Defered Column Cache", 1000);

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            String deferredcolumncode = request.getParameter("deferredcolumncode");
            String sdcid = request.getParameter("sdcid");
            String keyid1 = request.getParameter("keyid1");
            String keyid2 = request.getParameter("keyid2");
            String keyid3 = request.getParameter("keyid3");
            String[] keyid1s = StringUtil.split(keyid1, ";");
            String rowsperpage = request.getParameter("rowsperpage");
            int rowspageInt = Integer.parseInt(rowsperpage);
            String currentpage = request.getParameter("currentpage");
            if (keyid1s.length > rowspageInt) {
                StringBuffer keyid1sb = new StringBuffer(keyid1);
                StringBuffer keyid2sb = new StringBuffer(keyid2);
                StringBuffer keyid3sb = new StringBuffer(keyid3);
                SDITag.processPageRequest(keyid1sb, keyid2sb, keyid3sb, currentpage, rowsperpage);
                keyid1 = keyid1sb.toString();
                keyid2 = keyid2sb.toString();
                keyid3 = keyid3sb.toString();
                keyid1s = StringUtil.split(keyid1, ";");
            }
            if (deferredcolumncode != null && deferredcolumncode.length() > 0) {
                String[] codes = StringUtil.split(deferredcolumncode, ";");
                JSONObject jsonResponseObject = new JSONObject();
                String rsetid = "";
                String deferredcolumnids = "";
                for (int i = 0; i < codes.length; ++i) {
                    PropertyList deferredColumnPL = (PropertyList)deferedColumnMap.get(codes[i]);
                    String columnid = deferredColumnPL.getProperty("columnid");
                    deferredcolumnids = deferredcolumnids + ";" + columnid;
                    String sql = deferredColumnPL.getPropertyList("deferreddata").getProperty("sql");
                    if (sql.length() == 0) {
                        throw new Exception("Must define SQL for Deferred Display Column: " + columnid);
                    }
                    if (sql.startsWith("$G{")) {
                        sql = GroovyUtil.evaluate(sql, this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), null, null, this.getSDCProcessor().getProperties(sdcid));
                    }
                    sql = StringUtil.replaceAll(sql, "[sdcid]", sdcid);
                    DataSet dataSet = null;
                    SafeSQL safeSQL = new SafeSQL();
                    String[] tokens = StringUtil.getTokens(sql, "[", "]", true, true);
                    if (keyid1s.length > 950 || keyid2.length() > 0 || sql.indexOf("[rsetid]") > 0) {
                        int t;
                        if (rsetid.length() == 0) {
                            rsetid = this.getDAMProcessor().createRSet(sdcid, keyid1, keyid2, keyid3);
                        }
                        if (sql.indexOf("[keyid1list]") > 0) {
                            sql = StringUtil.replaceAll(sql, "[keyid1list]", "select keyid1 from rsetitems where rsetid=?");
                            for (t = 0; t < tokens.length; ++t) {
                                if (!"keyid1list".equals(tokens[t])) continue;
                                safeSQL.addVar(rsetid);
                            }
                        }
                        if (sql.indexOf("'[rsetid]'") >= 0) {
                            sql = StringUtil.replaceAll(sql, "'[rsetid]'", "?");
                            for (t = 0; t < tokens.length; ++t) {
                                if (!"rsetid".equals(tokens[t])) continue;
                                safeSQL.addVar(rsetid);
                            }
                        }
                        dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    } else {
                        StringBuilder sb = new StringBuilder();
                        for (int t = 0; t < tokens.length; ++t) {
                            if (!"keyid1list".equals(tokens[t])) continue;
                            for (int k = 0; k < keyid1s.length; ++k) {
                                if (t == 0) {
                                    sb.append(k > 0 ? ",?" : "?");
                                }
                                safeSQL.addVar(keyid1s[k]);
                            }
                        }
                        sql = StringUtil.replaceAll(sql, "[keyid1list]", sb.toString());
                        dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    }
                    if (dataSet == null) {
                        throw new Exception("SQL for Deferred Display Column has error. Check log for details.");
                    }
                    String displayvalue = deferredColumnPL.getProperty("displayvalue").trim();
                    String pseudocolumn = deferredColumnPL.getProperty("pseudocolumn").trim();
                    PropertyList link = deferredColumnPL.getPropertyList("link");
                    String[] tokencolumns = null;
                    if (pseudocolumn.length() > 0) {
                        tokencolumns = StringUtil.getTokens(pseudocolumn);
                    } else if (displayvalue.length() > 0) {
                        tokencolumns = StringUtil.getTokens(displayvalue);
                    } else if (link != null && link.getProperty("href").trim().length() > 0) {
                        tokencolumns = StringUtil.getTokens(link.getProperty("href"));
                    }
                    String multivaluedelimitor = "Vertical".equals(deferredColumnPL.getPropertyList("deferreddata").getProperty("multirowsdisplay")) ? "<br/>" : "&nbsp";
                    int maxValuesToDisplay = Integer.parseInt(deferredColumnPL.getPropertyList("deferreddata").getProperty("maxrowspersdi", "5"));
                    dataSet.addColumn(columnid + "_displayvalue", 0);
                    String previouskeyid1 = "";
                    String previouskeyid2 = "";
                    String previouskeyid3 = "";
                    int valueCount = 0;
                    dataSet.sort("keyid1,keyid2,keyid3");
                    int newrow = 0;
                    ArrayList toRemoveList = new ArrayList();
                    for (int row = 0; row < dataSet.getRowCount(); ++row) {
                        String currentkeyid1 = dataSet.getValue(row, "keyid1");
                        String currentkeyid2 = dataSet.getValue(row, "keyid2");
                        String currentkeyid3 = dataSet.getValue(row, "keyid3");
                        String value = dataSet.getValue(row, columnid);
                        if (pseudocolumn.length() > 0 && value.length() > 0) {
                            value = pseudocolumn;
                        } else if (displayvalue.length() > 0) {
                            value = SDITagUtil.getDisplayValue(value, displayvalue);
                        }
                        if (link != null && link.getProperty("href").trim().length() > 0) {
                            String title = link.getProperty("title");
                            value = "<a href=\"" + link.getProperty("href") + "\" target=\"" + link.getProperty("target") + "\" " + (title != null && title.length() > 0 ? " title=\"" + title + "\"" : "") + ">" + value + "</a>";
                        }
                        if (tokencolumns != null && tokencolumns.length > 0) {
                            for (int t = 0; t < tokencolumns.length; ++t) {
                                if ("rowid".equals(tokencolumns[t])) continue;
                                if (dataSet.isValidColumn(tokencolumns[t])) {
                                    value = StringUtil.replaceAll(value, "[" + tokencolumns[t] + "]", dataSet.getValue(row, tokencolumns[t]));
                                    continue;
                                }
                                throw new Exception("[" + tokencolumns[t] + "] is not retrieved in Deferred Data SQL: " + sql);
                            }
                        }
                        dataSet.setValue(row, columnid + "_displayvalue", value);
                        if (!(currentkeyid1.equals(previouskeyid1) && currentkeyid2.equals(previouskeyid2) && currentkeyid3.equals(previouskeyid3))) {
                            valueCount = 1;
                            newrow = row;
                            dataSet.setValue(newrow, columnid + "_displayvalue", value);
                        } else {
                            if (valueCount <= maxValuesToDisplay) {
                                dataSet.setValue(newrow, columnid + "_displayvalue", dataSet.getValue(newrow, columnid + "_displayvalue") + multivaluedelimitor + (valueCount == maxValuesToDisplay ? "..." : value));
                                ++valueCount;
                            }
                            if (row != newrow) {
                                toRemoveList.add(dataSet.get(row));
                            }
                        }
                        previouskeyid1 = currentkeyid1;
                        previouskeyid2 = currentkeyid2;
                        previouskeyid3 = currentkeyid3;
                    }
                    dataSet.removeAll(toRemoveList);
                    jsonResponseObject.put(columnid, JSONUtil.toJSONObject(dataSet));
                }
                if (rsetid.length() > 0) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                jsonResponseObject.put("deferredcolumncode", deferredcolumncode);
                jsonResponseObject.put("deferredcolumnids", deferredcolumnids.substring(1));
                jsonResponseObject.write(response.getWriter());
            }
        }
        catch (Exception e) {
            try {
                response.getWriter().write("Error:" + e.getMessage());
                this.logError("ListAjaxRequest Error:" + e.getMessage(), e);
            }
            catch (Exception ioe) {
                throw new ServletException((Throwable)ioe);
            }
        }
    }

    public static String registerDeferredColumn(PropertyList propertyList) {
        String code = propertyList.toJSONString(false, false);
        String deferredColumnCode = DigestUtils.md5Hex((String)code);
        deferedColumnMap.put(deferredColumnCode, propertyList);
        return deferredColumnCode;
    }
}

