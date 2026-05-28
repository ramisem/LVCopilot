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
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;

public class GetSampleTypePrepType
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 54997 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet ds;
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sourcesampletypeid = ajaxResponse.getRequestParameter("sourcesampletypeid", "");
        String destsampletypeid = ajaxResponse.getRequestParameter("destsampletypeid", "");
        JSONArray array = new JSONArray();
        if (StringUtil.getLen(sourcesampletypeid) > 0L && (ds = this.getQueryProcessor().getPreparedSqlDataSet("select distinct s_preptypeid from s_preptypesampletypemap where sourcesampletypeid = ? and destsampletypeid = ? order by s_preptypeid", (Object[])new String[]{sourcesampletypeid, destsampletypeid})) != null && ds.size() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setQueryFrom("s_preptype");
            sdiRequest.setSDCid("LV_PrepType");
            sdiRequest.setRequestItem("primary");
            sdiRequest.setQueryWhere("s_preptypeid in ('" + ds.getColumnValues("s_preptypeid", "','") + "')");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            if (sdiData != null) {
                DataSet primary = sdiData.getDataset("primary");
                for (int i = 0; i < primary.size(); ++i) {
                    array.put(primary.getString(i, "s_preptypeid"));
                }
            }
        }
        ajaxResponse.addCallbackArgument("preptypes", array.toString());
        ajaxResponse.print();
    }
}

