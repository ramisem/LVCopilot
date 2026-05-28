/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.pageelements.advancedsearch.QueryArg;
import com.labvantage.sapphire.pageelements.gwt.shared.ArrayConstants;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetQueryDiv
extends BaseAjaxRequest
implements ArrayConstants {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            PropertyList data = new PropertyList(new JSONObject(request.getParameter("data")));
            String sdcid = data.getProperty("querybasedonid");
            String queryId = data.getProperty("queryid");
            String queryParams = data.getProperty("queryparams");
            ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
            String sysuser = connectionInfo.getSysuserId();
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            PageContext pageContext = ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
            if (pageContext == null) {
                ajaxResponse.setError("Could not create page context.");
            }
            HashMap argMap = this.getQueryArgMap(this.getQueryProcessor(), sdcid, queryId, queryParams);
            String html = "";
            if (argMap != null) {
                QueryArg queryArgs = new QueryArg(pageContext, false, argMap);
                html = queryArgs.getArgDivHtml(queryId, (HashMap)argMap.get(queryId), sdcid, "", sysuser, 1);
            }
            this.write(html);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private HashMap getQueryArgMap(QueryProcessor qp, String sdcid, String queryId, String queryParams) {
        String selectArgs = "SELECT query.queryid, query.cascadedargflag, query.querydesc, queryarg.argid, queryarg.usersequence, queryarg.argdesc, queryarg.argtype, queryarg.sdcid, queryarg.reftypeid, queryarg.argdata, queryarg.defaultvalue, queryarg.arginto, queryarg.weblookupurl FROM query, queryarg WHERE query.queryid=queryarg.queryid  AND query.basedonid=queryarg.basedonid AND query.queryid = ? and query.basedonid = ? order by query.queryid, queryarg.usersequence";
        DataSet queryargs = qp.getPreparedSqlDataSet(selectArgs, (Object[])new String[]{queryId, sdcid});
        HashMap argMap = new HashMap();
        int rows = queryargs.getRowCount();
        HashMap<String, String> paramMap = new HashMap<String, String>();
        if (queryParams != null && queryParams.length() > 0) {
            String[] paramArray = StringUtil.split(queryParams, ";");
            for (int i = 0; i < paramArray.length; ++i) {
                String argInto = paramArray[i].substring(0, paramArray[i].indexOf(":"));
                String argVal = paramArray[i].substring(paramArray[i].indexOf(":") + 1);
                paramMap.put(argInto.trim(), argVal.trim());
            }
        }
        if (rows == 0) {
            return null;
        }
        for (int i = 0; i < rows; ++i) {
            String queryid = queryargs.getString(i, "queryid");
            if (argMap.get(queryid) == null) {
                HashMap<String, Object> queryprops = new HashMap<String, Object>();
                queryprops.put("querydesc", queryargs.getString(i, "querydesc"));
                queryprops.put("cascadedargflag", queryargs.getString(i, "cascadedargflag"));
                ArrayList arglist = new ArrayList();
                queryprops.put("arglist", arglist);
                argMap.put(queryid, queryprops);
            }
            HashMap<String, String> argprops = new HashMap<String, String>();
            argprops.put("argid", queryargs.getString(i, "argid"));
            argprops.put("argdesc", queryargs.getString(i, "argdesc"));
            argprops.put("argtype", queryargs.getString(i, "argtype"));
            argprops.put("sdcid", queryargs.getString(i, "sdcid"));
            argprops.put("reftypeid", queryargs.getString(i, "reftypeid"));
            argprops.put("argdata", queryargs.getString(i, "argdata"));
            String argInto = queryargs.getString(i, "arginto", "");
            String defaultValue = "";
            defaultValue = paramMap.containsKey(argInto) ? (String)paramMap.get(argInto) : queryargs.getString(i, "defaultvalue", "");
            argprops.put("defaultvalue", defaultValue);
            argprops.put("arginto", queryargs.getString(i, "arginto"));
            argprops.put("weblookupurl", queryargs.getString(i, "weblookupurl"));
            ((ArrayList)((HashMap)argMap.get(queryid)).get("arglist")).add(argprops);
        }
        return argMap;
    }
}

