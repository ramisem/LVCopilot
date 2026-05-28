/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.datachart.ajax;

import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.util.cache.CacheUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class RefreshSession
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        CacheUtil.remove(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        ar.print();
    }
}

