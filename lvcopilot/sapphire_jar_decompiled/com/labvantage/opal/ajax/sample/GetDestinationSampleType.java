/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.sample;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetDestinationSampleType
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53387 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sourcesampletypeid = ajaxResponse.getRequestParameter("sourcesampletypeid", "");
        JSONArray array = new JSONArray();
        if (StringUtil.getLen(sourcesampletypeid) > 0L) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select distinct destsampletypeid");
            sql.append(" from s_preptypesampletypemap");
            sql.append(" where sourcesampletypeid = ").append(safeSQL.addVar(sourcesampletypeid));
            sql.append(" order by destsampletypeid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    array.put(ds.getString(i, "destsampletypeid"));
                }
            }
        }
        ajaxResponse.addCallbackArgument("sampletypeid", sourcesampletypeid);
        ajaxResponse.addCallbackArgument("sampletypes", array.toString());
        ajaxResponse.addCallbackArgument("derivativesampletypeid", ajaxResponse.getRequestParameter("derivativesampletypeid", ""));
        ajaxResponse.addCallbackArgument("rowindex", ajaxResponse.getRequestParameter("rowindex", ""));
        ajaxResponse.print();
    }
}

