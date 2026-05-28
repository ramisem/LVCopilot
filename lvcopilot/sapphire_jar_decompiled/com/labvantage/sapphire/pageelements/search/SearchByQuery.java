/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.search;

import com.labvantage.sapphire.pageelements.search.SearchUtil;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.SDIProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;

public class SearchByQuery
extends BaseElement {
    private HashMap argMap = null;

    public SearchByQuery(PageContext pageContext, String connectionid) {
        this.pageContext = pageContext;
        this.setConnectionId(connectionid);
    }

    public HashMap getQueryArgMap() {
        return this.argMap;
    }

    @Override
    public String getHtml() {
        StringBuffer html = new StringBuffer();
        String sdcid = this.element.getProperty("sdcid");
        String queryqueryfrom = "query";
        String queryquerywhere = "query.basedonid='" + sdcid + "'";
        if (this.element.getPropertyList("querysearch") != null) {
            String categorylist = this.element.getPropertyList("querysearch").getProperty("category");
            String filterlist = this.element.getPropertyList("querysearch").getProperty("filter");
            if (categorylist != null && categorylist.trim().length() > 0) {
                queryqueryfrom = queryqueryfrom + ",categoryitem";
                queryquerywhere = queryquerywhere + " and query.queryid=categoryitem.keyid1 and categoryid in " + SearchUtil.toQueryInClause(categorylist);
            }
            if (filterlist != null && filterlist.trim().length() > 0) {
                queryquerywhere = queryquerywhere + " and query.queryid in " + SearchUtil.toQueryInClause(filterlist);
            }
        }
        SDIProcessor sdip = new SDIProcessor(this.pageContext);
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setQueryFrom(queryqueryfrom);
        sdiRequest.setQueryWhere(queryquerywhere);
        sdiRequest.setQueryOrderBy("query.usersequence, queryid");
        sdiRequest.setRequestItem("primary[queryid,querydesc]");
        sdiRequest.setSDCid("Query");
        sdiRequest.setRetrieve(true);
        DataSet queryids = sdip.getSDIData(sdiRequest).getDataset("primary");
        int rows = queryids.getRowCount();
        if (rows >= 1) {
            String queryidlist = "";
            String selected = this.pageContext.getRequest().getParameter("cascadequeryid");
            boolean isSelect = false;
            if (this.element.getPropertyList("querysearch") != null) {
                if (selected == null || selected.length() == 0) {
                    selected = this.element.getPropertyList("querysearch").getProperty("default");
                }
                isSelect = this.element.getPropertyList("querysearch").getProperty("style").equals("dropdownlist");
            }
            if (isSelect) {
                html.append("<select id=\"queryidselect\" class=\"search_queryidselect\" onchange=\"javascript:startSearch( 'query', this.value )\">");
                html.append("<option value=\"\"></option>\n");
            }
            for (int i = 0; i < rows; ++i) {
                String queryid = queryids.getString(i, "queryid");
                String querydesc = queryids.getString(i, "querydesc");
                if (querydesc == null || querydesc.length() == 0) {
                    querydesc = queryid;
                }
                if (isSelect) {
                    if (queryid.equals(selected)) {
                        html.append("<option value=\"" + queryid + "\" selected>\n");
                    } else {
                        html.append("<option value=\"" + queryid + "\">\n");
                    }
                    html.append(queryid).append("</option>\n");
                } else {
                    html.append(SearchUtil.getIdHtml(queryid, querydesc, false));
                }
                queryidlist = queryidlist + ";" + queryid;
            }
            if (isSelect) {
                html.append("</select>");
            }
            if (queryidlist.length() > 0) {
                queryidlist = queryidlist.substring(1);
            }
            SafeSQL safeSQL = new SafeSQL();
            String sql2 = "select query.queryid, query.cascadedargflag, query.querydesc, queryarg.argid, queryarg.usersequence, queryarg.argdesc, queryarg.argtype, queryarg.sdcid, queryarg.reftypeid, queryarg.argdata, queryarg.defaultvalue, queryarg.arginto from query, queryarg where query.queryid=queryarg.queryid and query.queryid in (" + safeSQL.addIn(queryidlist, ";") + ") and query.basedonid=" + safeSQL.addVar(sdcid) + " order by query.queryid, queryarg.usersequence";
            DataSet queryargs = this.getQueryProcessor().getPreparedSqlDataSet(sql2, safeSQL.getValues());
            this.argMap = this.getQueryArgMap(queryargs);
        }
        return html.toString();
    }

    private HashMap getQueryArgMap(DataSet ds) {
        HashMap argMap = new HashMap();
        int rows = ds.getRowCount();
        for (int i = 0; i < rows; ++i) {
            String queryid = ds.getString(i, "queryid");
            if (argMap.get(queryid) == null) {
                HashMap<String, Object> queryprops = new HashMap<String, Object>();
                queryprops.put("querydesc", ds.getString(i, "querydesc"));
                queryprops.put("cascadedargflag", ds.getString(i, "cascadedargflag"));
                ArrayList arglist = new ArrayList();
                queryprops.put("arglist", arglist);
                argMap.put(queryid, queryprops);
            }
            HashMap<String, String> argprops = new HashMap<String, String>();
            argprops.put("argid", ds.getString(i, "argid"));
            argprops.put("argdesc", ds.getString(i, "argdesc"));
            argprops.put("argtype", ds.getString(i, "argtype"));
            argprops.put("sdcid", ds.getString(i, "sdcid"));
            argprops.put("reftypeid", ds.getString(i, "reftypeid"));
            argprops.put("argdata", ds.getString(i, "argdata"));
            argprops.put("defaultvalue", ds.getString(i, "defaultvalue"));
            argprops.put("arginto", ds.getString(i, "arginto"));
            ((ArrayList)((HashMap)argMap.get(queryid)).get("arglist")).add(argprops);
        }
        return argMap;
    }
}

