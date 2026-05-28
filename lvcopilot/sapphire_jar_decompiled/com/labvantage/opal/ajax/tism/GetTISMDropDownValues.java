/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.tism;

import com.labvantage.opal.ajax.util.GetSQLDataSet;
import com.labvantage.opal.util.OpalUtil;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetTISMDropDownValues
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sql = this.getConfigurationProcessor().getProfileProperty("tismddsql");
        if (OpalUtil.isNotEmpty(sql)) {
            DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
            if (ds != null) {
                String columns = GetSQLDataSet.getSelectColumns(sql);
                if ("*".equals(columns)) {
                    ArrayList<String> list = new ArrayList<String>();
                    for (int i = 0; i < ds.getColumnCount(); ++i) {
                        list.add(ds.getColumnId(i));
                    }
                    columns = OpalUtil.toDelimitedString(list, ";");
                }
                ajaxResponse.addCallbackArgument("ds", ds);
                ajaxResponse.addCallbackArgument("columns", columns);
            } else {
                ajaxResponse.addCallbackArgument("ds", new DataSet());
                ajaxResponse.addCallbackArgument("columns", "");
            }
        } else {
            ajaxResponse.addCallbackArgument("ds", new DataSet());
            ajaxResponse.addCallbackArgument("columns", "");
        }
        ajaxResponse.print();
    }
}

