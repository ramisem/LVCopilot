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

import java.util.ArrayList;
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

public class GetSamplesByProcessType
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid");
        String processtype = ajaxResponse.getRequestParameter("processtype");
        StringBuilder sql = new StringBuilder();
        SafeSQL safeSQL = new SafeSQL();
        sql.append("SELECT s.s_sampleid");
        sql.append(" FROM s_sample s, s_samplefamily sf, s_eventdefstspecimendef e");
        sql.append(" WHERE sf.S_SAMPLEFAMILYID = s.SAMPLEFAMILYID");
        sql.append(" AND e.S_EVENTDEFID = sf.EVENTDEFID");
        sql.append(" AND e.S_SAMPLETYPEID = sf.SAMPLETYPEID");
        sql.append(" AND e.S_SPECIMENDEFID = sf.SPECIMENDEFID");
        sql.append(" AND e.PROCESSTYPE = ").append(safeSQL.addVar(processtype));
        ArrayList ds = null;
        if (StringUtil.split(sampleid, ";").length > 1000) {
            try {
                String rsetid = this.getDAMProcessor().createRSet("Sample", sampleid, null, null);
                sql.append(" and s.s_sampleid in (select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
                ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                this.getDAMProcessor().clearRSet(rsetid);
            }
            catch (SapphireException e) {
                e.printStackTrace();
            }
        } else {
            sql.append(" and s.s_sampleid in (").append(safeSQL.addIn(sampleid, ";")).append(")");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        }
        ajaxResponse.addCallbackArgument("sampleid", ds != null && ds.size() > 0 ? ((DataSet)ds).getColumnValues("s_sampleid", ";") : "");
        ajaxResponse.addCallbackArgument("processtype", processtype);
        ajaxResponse.print();
    }
}

