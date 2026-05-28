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

import java.math.BigDecimal;
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

public class CheckNonCancelableDataSet
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54062 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        DataSet dsNonCancelableDataSet;
        QueryProcessor qp = this.getQueryProcessor();
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
        String paramlistid = ajaxResponse.getRequestParameter("paramlistid", "");
        String paramlistversionid = ajaxResponse.getRequestParameter("paramlistversionid", "");
        String variantid = ajaxResponse.getRequestParameter("variantid", "");
        String dataset = ajaxResponse.getRequestParameter("dataset", "");
        String operation = ajaxResponse.getRequestParameter("operation", "");
        String[] paramlistidArr = StringUtil.split(paramlistid, ";");
        String[] paramlistversionidArr = StringUtil.split(paramlistversionid, ";");
        String[] variantidArr = StringUtil.split(variantid, ";");
        String[] datasetArr = StringUtil.split(dataset, ";");
        StringBuffer nonCancelableUncancelableDataSet = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sqlParamList = new StringBuffer();
        sqlParamList.append("SELECT * FROM sdidata ");
        sqlParamList.append(" WHERE paramlistid IN ( ").append(safeSQL.addIn(paramlistid, ";")).append(" )");
        sqlParamList.append(" AND paramlistversionid IN ( ").append(safeSQL.addIn(paramlistversionid, ";")).append(" )");
        sqlParamList.append(" AND variantid IN ( ").append(safeSQL.addIn(variantid, ";")).append(" )");
        sqlParamList.append(" AND dataset IN ( ").append(safeSQL.addIn(dataset, ";")).append(" )");
        if (operation.equalsIgnoreCase("cancel")) {
            sqlParamList.append(" AND ( s_cancellableflag='N' or s_cancellableflag is null )");
        } else {
            sqlParamList.append(" AND s_datasetstatus!='Cancelled'");
        }
        sqlParamList.append(" AND sdcid=" + safeSQL.addVar(sdcid));
        sqlParamList.append(" AND keyid1=" + safeSQL.addVar(keyid1));
        if (!keyid2.equals("")) {
            sqlParamList.append(" AND keyid2=" + safeSQL.addVar(keyid2));
        }
        if (!keyid3.equals("")) {
            sqlParamList.append(" AND keyid3=" + safeSQL.addVar(keyid3));
        }
        if ((dsNonCancelableDataSet = qp.getPreparedSqlDataSet(sqlParamList.toString(), safeSQL.getValues())).size() > 0) {
            HashMap<String, Object> findMap = new HashMap<String, Object>();
            BigDecimal datasetNumber = null;
            for (int i = 0; i < paramlistidArr.length; ++i) {
                findMap.clear();
                int rows = -1;
                datasetNumber = new BigDecimal(datasetArr[i]);
                findMap.put("paramlistid", paramlistidArr[i]);
                findMap.put("paramlistversionid", paramlistversionidArr[i]);
                findMap.put("variantid", variantidArr[i]);
                findMap.put("dataset", datasetNumber);
                rows = dsNonCancelableDataSet.findRow(findMap);
                if (rows < 0) continue;
                nonCancelableUncancelableDataSet.append(",").append(!keyid1.equals("") && !keyid1.equals("(null)") ? keyid1 + ";" : "");
                nonCancelableUncancelableDataSet.append(paramlistidArr[i] + ";" + paramlistversionidArr[i] + ";" + variantidArr[i] + ";" + datasetArr[i]);
            }
        }
        if (nonCancelableUncancelableDataSet.length() > 0) {
            ajaxResponse.addCallbackArgument("callbackMsg", nonCancelableUncancelableDataSet.substring(1));
        } else {
            ajaxResponse.addCallbackArgument("callbackMsg", "");
        }
        ajaxResponse.print();
    }
}

