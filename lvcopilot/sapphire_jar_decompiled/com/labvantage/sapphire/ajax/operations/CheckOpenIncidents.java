/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class CheckOpenIncidents
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 99038 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet dsOpenIncidents;
        QueryProcessor qp = this.getQueryProcessor();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
        String[] keyid1Arr = StringUtil.split(keyid1, ";");
        String[] keyid2Arr = StringUtil.split(keyid2, ";");
        String[] keyid3Arr = StringUtil.split(keyid3, ";");
        String callbackMsg = "";
        StringBuffer sqlOpenIncidents = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        sqlOpenIncidents.append("SELECT incitem.sourcekeyid1,incitem.sourcekeyid2,incitem.sourcekeyid3").append(" FROM incidentitem incitem,incident inc ").append(" WHERE inc.incidentid=incitem.incidentid").append(" AND inc.incidentstatus not in ('Closed','Cancelled')").append(" AND incitem.sourcesdcid=" + safeSQL.addVar(sdcid)).append(" AND incitem.sourcekeyid1 IN ( ").append(safeSQL.addIn(keyid1, ";")).append(" )");
        if (!keyid2.equals("")) {
            sqlOpenIncidents.append(" AND incitem.sourcekeyid2 IN ( ").append(safeSQL.addIn(keyid2, ";")).append(" )");
        }
        if (!keyid3.equals("")) {
            sqlOpenIncidents.append(" AND incitem.sourcekeyid3 IN ( ").append(safeSQL.addIn(keyid3, ";")).append(" )");
        }
        if ((dsOpenIncidents = qp.getPreparedSqlDataSet(sqlOpenIncidents.toString(), safeSQL.getValues())).size() > 0) {
            if (!keyid2.equals("") || !keyid3.equals("")) {
                HashMap<String, String> findMap = new HashMap<String, String>();
                for (int i = 0; i < keyid1Arr.length; ++i) {
                    findMap.clear();
                    int rows = -1;
                    findMap.put("sourcekeyid1", keyid1Arr[i]);
                    if (!keyid2.equals("") && keyid2Arr.length > i) {
                        findMap.put("sourcekeyid2", keyid2Arr[i]);
                    }
                    if (!keyid3.equals("") && keyid3Arr.length > i) {
                        findMap.put("sourcekeyid3", keyid3Arr[i]);
                    }
                    if ((rows = dsOpenIncidents.findRow(findMap)) < 0) continue;
                    callbackMsg = "One or More selected Sample(s) have Open Incidents. Operation cannot proceed.";
                    break;
                }
            } else {
                callbackMsg = "One or More selected Sample(s) have Open Incidents. Operation cannot proceed.";
            }
        }
        ajaxResponse.addCallbackArgument("callbackMsg", callbackMsg);
        ajaxResponse.print();
    }
}

