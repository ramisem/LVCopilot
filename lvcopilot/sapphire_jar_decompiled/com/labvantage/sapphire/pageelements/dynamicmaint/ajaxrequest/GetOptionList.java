/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GetOptionList
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        DataSet ds;
        AjaxResponse ar = new AjaxResponse(req, resp);
        String dropdownsql = ar.getRequestParameter("dropdownsql", "");
        String elementid = ar.getRequestParameter("elementid", "");
        String reftypeid = ar.getRequestParameter("reftypeid", "");
        String selectedValue = ar.getRequestParameter("selectedvalue", "");
        String pageId = ar.getRequestParameter("pageid", "");
        PropertyListCollection coll = new PropertyListCollection();
        PropertyList plEmpty = new PropertyList();
        plEmpty.setProperty("key", "");
        plEmpty.setProperty("value", "");
        coll.add(0, plEmpty);
        if (dropdownsql.startsWith("sql")) {
            String sql = this.getSqlFromCache(dropdownsql, (PropertyList)req.getSession().getAttribute("DYM_" + pageId));
            ds = this.getQueryProcessor().getSqlDataSet(sql);
        } else {
            ds = !reftypeid.equals("") ? this.getQueryProcessor().getRefTypeDataSet(reftypeid) : new DataSet();
        }
        if (ds != null && ds.getRowCount() > 0) {
            for (int i = 0; i < ds.getRowCount(); ++i) {
                PropertyList pl = new PropertyList();
                pl.setProperty("key", ds.getValue(i, ds.getColumnId(0)));
                if (ds.getColumnCount() > 1) {
                    pl.setProperty("value", ds.getValue(i, ds.getColumnId(1), ds.getValue(i, ds.getColumnId(0))));
                } else {
                    pl.setProperty("value", ds.getValue(i, ds.getColumnId(0)));
                }
                coll.add(i + 1, pl);
            }
        }
        ar.addCallbackArgument("optionlist", coll.toJSONArray());
        if (!elementid.equals("")) {
            ar.addCallbackArgument("elementid", elementid);
        }
        if (!reftypeid.equals("")) {
            ar.addCallbackArgument("reftypeid", reftypeid);
        }
        ar.addCallbackArgument("selectedvalue", selectedValue);
        ar.print();
    }

    private String[] splitPreserveEmptyTokens(String s, String delim) {
        if (s == null || delim == null) {
            return new String[0];
        }
        if (s.equals("")) {
            return new String[]{""};
        }
        if (delim.equals("")) {
            return new String[]{s};
        }
        ArrayList<String> list = new ArrayList<String>();
        int posStart = 0;
        int posEnd = s.indexOf(delim, posStart);
        while (posEnd != -1) {
            list.add(s.substring(posStart, posEnd));
            posStart = posEnd + delim.length();
            posEnd = s.indexOf(delim, posStart);
        }
        list.add(s.substring(posStart));
        return list.toArray(new String[list.size()]);
    }

    private String getSqlFromCache(String sqlIdWithArguments, PropertyList pageProps) {
        String sql = "";
        String[] arr = this.splitPreserveEmptyTokens(sqlIdWithArguments, ";");
        String sqlId = arr[0];
        boolean isOracle = this.getConnectionProcessor().isOra();
        List<String> arguments = Arrays.asList(arr).stream().skip(1L).map(bindvalue -> SafeSQL.encodeForSQL(bindvalue, isOracle)).collect(Collectors.toList());
        if (pageProps == null) {
            return sql;
        }
        block0: for (Object key : pageProps.keySet()) {
            String elementId = (String)key;
            PropertyList elementProps = pageProps.getPropertyList(elementId);
            if (elementProps == null || !elementProps.containsKey("dropdownsqls")) continue;
            PropertyListCollection elementSqlProps = elementProps.getCollection("dropdownsqls");
            for (int i = 0; i < elementSqlProps.size(); ++i) {
                PropertyList sqlProps = elementSqlProps.getPropertyList(i);
                if (!sqlProps.getProperty("sqlid", "").equals(sqlId)) continue;
                sql = sqlProps.getProperty("sql");
                sql = this.replaceArguments(sql, arguments);
                continue block0;
            }
        }
        return sql;
    }

    private String replaceArguments(String sql, List<String> arguments) {
        StringBuilder parsedSql = new StringBuilder();
        char[] chars = sql.toCharArray();
        boolean isVariable = false;
        int argumentIndex = 0;
        for (char c : chars) {
            if (c == '[') {
                isVariable = true;
                continue;
            }
            if (c == ']') {
                isVariable = false;
                if (argumentIndex < arguments.size()) {
                    parsedSql.append(arguments.get(argumentIndex));
                } else {
                    this.logger.error("Too few arguments for sql: " + sql);
                }
                ++argumentIndex;
                continue;
            }
            if (isVariable) continue;
            parsedSql.append(c);
        }
        return parsedSql.toString();
    }
}

