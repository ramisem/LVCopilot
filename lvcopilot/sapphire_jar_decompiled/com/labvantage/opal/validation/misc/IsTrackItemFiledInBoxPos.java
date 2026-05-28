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

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class IsTrackItemFiledInBoxPos
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid");
        int ticount = StringUtil.split(trackitemid, ";").length;
        ArrayList ds = null;
        StringBuilder sql = new StringBuilder();
        sql.append("select ti.trackitemid, ti.linkkeyid1");
        sql.append(" from trackitem ti, storageunit su");
        sql.append(" where su.storageunittype = 'BoxPos'");
        sql.append(" and su.storageunitid = ti.currentstorageunitid");
        if (ticount > 1000) {
            String rsetid = null;
            try {
                rsetid = this.getDAMProcessor().createRSet("TrackItemSDC", trackitemid, null, null);
                sql.append(" and ti.trackitemid in ( select r.keyid1 from rsetitems r where r.rsetid = ? )");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{rsetid});
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
            finally {
                if (StringUtil.getLen(rsetid) > 0L) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            }
        } else {
            SafeSQL safeSQL = new SafeSQL();
            sql.append(" and ti.trackitemid in (").append(safeSQL.addIn(trackitemid, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        ajaxResponse.addCallbackArgument("reserved", ds != null && ds.size() > 0 ? "Y" : "N");
        ajaxResponse.print();
    }
}

