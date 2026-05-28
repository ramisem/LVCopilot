/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetTrackItemInfo
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid");
        String column = ajaxResponse.getRequestParameter("columns");
        if (column.toLowerCase().contains(" from ") || column.toLowerCase().contains(" union ")) {
            column = "trackitemid";
        }
        DataSet ds = new DataSet();
        if (StringUtil.getLen(trackitemid) > 0L) {
            int ticount = StringUtil.split(trackitemid, ";").length;
            StringBuilder sql = new StringBuilder();
            if (StringUtil.getLen(column) == 0L) {
                sql.append("select * from trackitem");
            } else {
                sql.append("select ").append(column).append(" from trackitem");
            }
            if (ticount > 750) {
                try {
                    String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, null, null);
                    sql.append(" where trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ?)");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                catch (SapphireException e) {
                    ds = new DataSet();
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                sql.append(" where trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
        }
        ajaxResponse.addCallbackArgument("ds", ds);
        ajaxResponse.print();
    }
}

