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

public class GetPrepTypeTreatments
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53388 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String preptypeid = ajaxResponse.getRequestParameter("preptypeid", "");
        JSONArray array = new JSONArray();
        if (StringUtil.getLen(preptypeid) > 0L) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select s_treatmenttypeid from s_preptypetreatmenttypemap where s_preptypeid = ").append(safeSQL.addVar(preptypeid));
            sql.append(" order by s_treatmenttypeid");
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                for (int i = 0; i < ds.size(); ++i) {
                    array.put(ds.getString(i, "s_treatmenttypeid"));
                }
            }
        }
        ajaxResponse.addCallbackArgument("treatments", array.toString());
        ajaxResponse.addCallbackArgument("rowindex", ajaxResponse.getRequestParameter("rowindex", ""));
        ajaxResponse.print();
    }
}

