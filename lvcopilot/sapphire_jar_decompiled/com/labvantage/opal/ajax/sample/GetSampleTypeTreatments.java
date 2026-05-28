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
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetSampleTypeTreatments
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 53388 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampletype = ajaxResponse.getRequestParameter("sampletypeid", "");
        DataSet ds = null;
        if (StringUtil.getLen(sampletype) > 0L) {
            String rsetid = "";
            try {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_Treatment");
                sdiRequest.setQueryFrom("s_treatmenttype");
                sdiRequest.setRequestItem("primary");
                sdiRequest.setRetainRsetid(true);
                SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                if (sdiData != null) {
                    rsetid = sdiData.getRsetid();
                    SafeSQL safeSQL = new SafeSQL();
                    StringBuilder sql = new StringBuilder();
                    sql.append("select s_treatmenttype.s_treatmenttypeid");
                    sql.append(" from s_treatmenttype, s_sampletypetreattypemap");
                    sql.append(" where s_treatmenttype.s_treatmenttypeid = s_sampletypetreattypemap.s_treatmenttypeid");
                    sql.append(" and s_sampletypetreattypemap.s_sampletypeid = ").append(safeSQL.addVar(sampletype));
                    sql.append(" and s_treatmenttype.s_treatmenttypeid in ( select r.keyid1 from rsetitems r where r.rsetid = ").append(safeSQL.addVar(rsetid)).append(")");
                    sql.append(" order by s_treatmenttype.s_treatmenttypeid");
                    ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
                }
            }
            finally {
                if (StringUtil.getLen(rsetid) > 0L) {
                    this.getDAMProcessor().clearRSet(rsetid);
                }
            }
        }
        ajaxResponse.addCallbackArgument("ds", ds == null ? new DataSet() : ds);
        ajaxResponse.print();
    }
}

