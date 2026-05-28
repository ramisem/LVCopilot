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

public class GetDepartmentType
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        String departmentids = ajaxResponse.getRequestParameter("departmentids", "");
        String datasetname = ajaxResponse.getRequestParameter("datasetname", "");
        String rowids = ajaxResponse.getRequestParameter("rowids", "");
        String testingflags = "";
        if (departmentids.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT departmentid, testingflag ");
            sql.append(" FROM department WHERE departmentid in (" + safeSQL.addIn(departmentids, ";") + ")");
            DataSet departmentDS = qp.getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (departmentDS != null && departmentDS.size() > 0) {
                String[] departmentidsArr;
                for (String departmentid : departmentidsArr = departmentids.split(";")) {
                    int indx = departmentDS.findRow("departmentid", departmentid);
                    testingflags = indx > -1 && departmentDS.getString(indx, "testingflag", "N").equalsIgnoreCase("Y") ? testingflags + ";Y" : testingflags + ";N";
                }
            }
        }
        ajaxResponse.addCallbackArgument("departmentids", departmentids);
        ajaxResponse.addCallbackArgument("testingflags", testingflags.substring(1));
        ajaxResponse.addCallbackArgument("datasetname", datasetname);
        ajaxResponse.addCallbackArgument("rowids", rowids);
        ajaxResponse.print();
    }
}

