/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class FindKitItemsInALot
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String keylotid = ajaxResponse.getRequestParameter("keylotid", "");
        if (keylotid.indexOf("%3B") > -1) {
            keylotid = StringUtil.replaceAll(keylotid, "%3B", "','");
        }
        TranslationProcessor tp = this.getTranslationProcessor();
        if (keylotid.length() == 0) {
            ajaxResponse.setError(tp.translate("keylotid not defined."));
            ajaxResponse.print();
        } else {
            QueryProcessor qp = this.getQueryProcessor();
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT trackitemid FROM trackitem WHERE linksdcid='LV_ReagentLot' and linkkeyid1 in( " + safeSQL.addIn(keylotid) + " )";
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            String items = ds.getColumnValues("trackitemid", ";");
            ajaxResponse.addCallbackArgument("items", items);
            ajaxResponse.print();
        }
    }
}

