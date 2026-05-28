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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class FindSDCs
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdclist = ajaxResponse.getRequestParameter("sdclist");
        String callbackExtras = ajaxResponse.getRequestParameter("callbackExtras");
        QueryProcessor qp = this.getQueryProcessor();
        SafeSQL safeSQL = new SafeSQL();
        DataSet systableDs = qp.getPreparedSqlDataSet("select sdc.sdcid from systable inner join sdc on systable.tableid=sdc.tableid where ( ( sdc.searchableflag='Y' ) and sdc.sdcid in (" + safeSQL.addIn(sdclist) + ") ) order by 1", safeSQL.getValues());
        String list = systableDs.getColumnValues("sdcid", ";");
        ajaxResponse.addCallbackArgument("sdclist", list);
        ajaxResponse.addCallbackArgument("callbackExtras", callbackExtras);
        ajaxResponse.print();
    }
}

