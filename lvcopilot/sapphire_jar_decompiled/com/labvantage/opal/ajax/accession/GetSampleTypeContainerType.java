/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.accession;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetSampleTypeContainerType
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String param_sampletypeid = ajaxResponse.getRequestParameter("sampletypeid");
        String uuid = ajaxResponse.getRequestParameter("uuid");
        JSONArray array = new JSONArray();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select containertypeid from s_sampletypecontainertype where s_sampletypeid = ? and activeflag = 'Y' order by containertypeid", (Object[])new String[]{param_sampletypeid});
        for (int i = 0; i < ds.size(); ++i) {
            array.put(ds.getString(i, "containertypeid"));
        }
        ajaxResponse.addCallbackArgument("data", array);
        ajaxResponse.addCallbackArgument("uuid", uuid);
        ajaxResponse.print();
    }
}

