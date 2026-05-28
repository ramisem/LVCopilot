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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetSamplesForBatch
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String returnSampleIds = null;
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        String sql = "SELECT s_sampleid FROM s_sample WHERE templateflag!='Y' AND batchid=?";
        DataSet dataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{keyid1});
        String sampleIds = dataSet.getColumnValues("s_sampleid", ";");
        returnSampleIds = sampleIds == null || sampleIds.length() == 0 ? "No Data Found" : sampleIds;
        ajaxResponse.addCallbackArgument("data", returnSampleIds);
        ajaxResponse.print();
    }
}

