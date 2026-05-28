/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.stepdef;

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

public class GetTrackItemQty
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "StepHandler");
        String row = ajaxResponse.getRequestParameter("row", "0");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid");
        DataSet ds = new DataSet();
        if (StringUtil.getLen(trackitemid) > 0L) {
            int ticount = StringUtil.split(trackitemid, ";").length;
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select qtycurrent, qtyunits, containertypeid, qtycurrenttype, linkkeyid1 from trackitem");
            if (ticount > 750) {
                try {
                    String rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, null, null);
                    sql.append(" where trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(" )");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                    this.getDAMProcessor().clearRSet(rsetid);
                }
                catch (SapphireException e) {
                    ds = new DataSet();
                }
            } else {
                sql.append(" where trackitemid in ( ").append(safeSQL.addIn(trackitemid, ";")).append(" )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            }
        }
        ajaxResponse.addCallbackArgument("qtycurrent", ds.getValue(0, "qtycurrent"));
        ajaxResponse.addCallbackArgument("qtyunits", ds.getValue(0, "qtyunits"));
        ajaxResponse.addCallbackArgument("qtycurrenttype", ds.getValue(0, "qtycurrenttype"));
        ajaxResponse.addCallbackArgument("containertypeid", ds.getValue(0, "containertypeid"));
        ajaxResponse.addCallbackArgument("row", row);
        ajaxResponse.addCallbackArgument("reagentlotid", ds.getValue(0, "linkkeyid1"));
        ajaxResponse.print();
    }
}

