/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.data;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetActionProperties
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "getActionPropertiesHandler");
        QueryProcessor qp = this.getQueryProcessor();
        String actionid = ajaxResponse.getRequestParameter("actionid");
        if (actionid != null && actionid.length() > 0) {
            DataSet ds = qp.getPreparedSqlDataSet("SELECT * FROM actionproperty WHERE actionid = ? AND actionversionid = '1' ORDER BY usersequence, propertyid", new Object[]{actionid});
            ajaxResponse.addCallbackArgument("actionproperties", ds);
            ajaxResponse.addCallbackArgument("count", ds.size());
            ajaxResponse.addCallbackArgument("columns", ds.getColumns().length);
        } else {
            ajaxResponse.setError("Actionid property not defined for service!");
        }
        ajaxResponse.print();
    }
}

