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

import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetWorkItemColumnValue
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String value = "";
        String workitemid = ajaxResponse.getRequestParameter("workitemid");
        String workitemversionid = ajaxResponse.getRequestParameter("workitemversionid", "");
        String columnid = ajaxResponse.getRequestParameter("columnid", "");
        if (OpalUtil.isNotEmpty(workitemid) && OpalUtil.isNotEmpty(columnid)) {
            boolean isValidColumn = false;
            DataSet columnds = this.getSDCProcessor().getColumnData("WorkItem");
            for (int i = 0; i < columnds.size(); ++i) {
                if (!columnid.equals(columnds.getString(i, "columnid"))) continue;
                isValidColumn = true;
                break;
            }
            if (isValidColumn) {
                Object[] keys = new String[]{workitemid};
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select " + columnid + ", workitemversionid, versionstatus from workitem where workitemid = ? and versionstatus in ( 'P', 'C' ) order by createdt desc", keys);
                if (ds != null && ds.size() > 0) {
                    value = ds.getValue(0, columnid, "");
                    boolean fromcurrentversion = OpalUtil.isEmpty(workitemversionid);
                    for (int i = 0; i < ds.size(); ++i) {
                        if (fromcurrentversion) {
                            if (!"C".equals(ds.getString(i, "versionstatus"))) continue;
                            value = ds.getValue(i, columnid, "");
                            break;
                        }
                        if (!workitemversionid.equals(ds.getString(i, "workitemversionid"))) continue;
                        value = ds.getValue(i, columnid, "");
                        break;
                    }
                }
            }
        }
        ajaxResponse.addCallbackArgument("value", value);
        ajaxResponse.addCallbackArgument("parameters", new JSONObject(ajaxResponse.getRequestParameters()));
        ajaxResponse.print();
    }
}

