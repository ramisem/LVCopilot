/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;

public class GetSampleTypeContainers
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampletypeid = ajaxResponse.getRequestParameter("sampletypeid");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setRequestItem("primary");
        sdiRequest.setSDCid("ContainerType");
        sdiRequest.setQueryFrom("containertype");
        sdiRequest.setQueryWhere("containertype.containertypeid in (select s.containertypeid from s_sampletypecontainertype s where s.s_sampletypeid = '" + SafeSQL.encodeForSQL(sampletypeid, this.getConnectionProcessor().isOra()) + "' and (s.activeflag = 'Y' or s.activeflag is null))");
        DataSet containerds = this.getSDIProcessor().getSDIData(sdiRequest).getDataset("primary");
        JSONArray containerArray = new JSONArray();
        if (containerds != null) {
            for (int i = 0; i < containerds.size(); ++i) {
                containerArray.put(containerds.getString(i, "containertypeid"));
            }
        }
        ajaxResponse.addCallbackArgument("sampletypeid", sampletypeid);
        ajaxResponse.addCallbackArgument("containers", containerArray.toString());
        ajaxResponse.addCallbackArgument("rowindex", ajaxResponse.getRequestParameter("rowindex"));
        ajaxResponse.print();
    }
}

