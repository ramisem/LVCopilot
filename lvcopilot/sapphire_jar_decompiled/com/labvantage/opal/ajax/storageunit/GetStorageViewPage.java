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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetStorageViewPage
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        String storageUnitID = ajaxResponse.getRequestParameter("fieldId", "");
        String argument = ajaxResponse.getRequestParameter("args", "");
        String page = "";
        String linkkeyid1 = "";
        String linksdcid = "";
        String[] args = new String[]{};
        if (argument.contains(";")) {
            args = StringUtil.split(argument, ";");
        }
        StringBuilder sql = new StringBuilder();
        if (this.getConnectionProcessor().isOra()) {
            sql.setLength(0);
            sql.append("select storageunitid, parentid, linksdcid, linkkeyid1, level ");
            sql.append(" from storageunit");
            sql.append(" connect by prior parentid = storageunitid");
            sql.append(" start with storageunitid = ?");
        } else {
            sql.setLength(0);
            sql.append("WITH StorageUnitTree (storageunitid, parentid, linksdcid, linkkeyid1)");
            sql.append(" AS ( ");
            sql.append("    SELECT su.storageunitid,su.parentid,su.linksdcid,su.linkkeyid1 ");
            sql.append("    FROM storageunit AS su ");
            sql.append("    WHERE su.storageunitid = ? ");
            sql.append("    UNION ALL ");
            sql.append("    SELECT su.storageunitid,su.parentid,su.linksdcid,su.linkkeyid1 ");
            sql.append("    FROM storageunit AS su ");
            sql.append("    INNER JOIN StorageUnitTree AS d ");
            sql.append("    ON su.storageunitid = d.parentid ");
            sql.append(" ) ");
            sql.append(" select storageunitid, parentid, linksdcid, linkkeyid1 ");
            sql.append(" from StorageUnitTree ");
        }
        String defaultviewpage = "StorageUnitSDCView";
        String viewpageid = "";
        DataSet ds = qp.getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{storageUnitID});
        if (ds != null) {
            block0: for (String token : args) {
                if (token.equals("")) continue;
                String[] parts = StringUtil.split(token, "=");
                page = parts[1];
                String sdcid = parts[0];
                if (sdcid.equalsIgnoreCase("StorageUnitSDC") && viewpageid.equals("")) {
                    defaultviewpage = page;
                    linksdcid = sdcid;
                    linkkeyid1 = storageUnitID;
                    continue;
                }
                for (int i = 0; i < ds.size() && viewpageid.equals(""); ++i) {
                    if (!sdcid.equalsIgnoreCase(ds.getString(i, "linksdcid"))) continue;
                    linkkeyid1 = ds.getString(i, "linkkeyid1");
                    linksdcid = ds.getString(i, "linksdcid");
                    viewpageid = page;
                    continue block0;
                }
            }
            if (viewpageid.length() == 0) {
                viewpageid = defaultviewpage;
            }
        }
        ajaxResponse.addCallbackArgument("storageunitid", storageUnitID);
        ajaxResponse.addCallbackArgument("viewpageid", viewpageid);
        ajaxResponse.addCallbackArgument("linkkeyid1", linkkeyid1);
        ajaxResponse.addCallbackArgument("linksdcid", linksdcid);
        ajaxResponse.print();
    }
}

