/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.util.OpalUtil;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetPhysicalStoreID
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid", "");
        StringBuilder physicalstoreid = new StringBuilder();
        if (OpalUtil.isNotEmpty(storageunitid)) {
            storageunitid = StringUtil.replaceAll(storageunitid, "%3B", ";");
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, linkkeyid1 from storageunit where linksdcid = 'PhysicalStore' and storageunitid in (" + safeSQL.addIn(storageunitid, ";") + ")", safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                if (ds.size() > 1) {
                    String[] idarray;
                    HashMap<String, String> filter = new HashMap<String, String>();
                    for (String s : idarray = StringUtil.split(storageunitid, ";")) {
                        filter.put("storageunitid", s);
                        int row = ds.findRow(filter);
                        if (row == -1) continue;
                        if (physicalstoreid.length() > 0) {
                            physicalstoreid.append(";");
                        }
                        physicalstoreid.append(ds.getString(row, "linkkeyid1", ""));
                    }
                } else {
                    physicalstoreid.append(ds.getString(0, "linkkeyid1", ""));
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("physicalstoreid", physicalstoreid.toString());
        ajaxResponse.print();
    }
}

