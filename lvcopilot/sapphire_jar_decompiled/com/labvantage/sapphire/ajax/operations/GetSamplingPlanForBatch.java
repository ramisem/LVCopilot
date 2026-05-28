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

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetSamplingPlanForBatch
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53820 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String mode = ajaxResponse.getRequestParameter("mode", "");
        if ("Composite".equals(mode)) {
            String batchids = ajaxResponse.getRequestParameter("batchids", "");
            String rejectedBatches = this.getRejectedBatches(batchids);
            if (rejectedBatches.length() > 0) {
                ajaxResponse.addCallbackArgument("rejectedbatches", rejectedBatches);
                ajaxResponse.print();
            } else {
                this.checkingForCompositeBatch(ajaxResponse, batchids);
            }
        }
    }

    private void checkingForCompositeBatch(AjaxResponse ajaxResponse, String batchids) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT distinct productid,productversionid,samplingplanid,samplingplanversionid,batchtype FROM s_batch");
        sql.append(" WHERE s_batchid in (").append(safeSQL.addIn(batchids.replaceAll(";", "','"))).append(") ORDER BY productid,productversionid");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        String msg = "null";
        String samplingplanid = "";
        String samplingplanversionid = "";
        ArrayList<DataSet> prodSamplingPlanList = ds.getGroupedDataSets("productid,productversionid");
        if (prodSamplingPlanList.size() > 1) {
            msg = "Composite batches require all the batches to belong to the same product. Use 'Link Batches' to choose batches of different products";
        } else if (prodSamplingPlanList.size() == 1) {
            ds.sort("samplingplanid,samplingplanversionid");
            ArrayList<DataSet> samplingPlanList = ds.getGroupedDataSets("samplingplanid,samplingplanversionid");
            for (int i = 0; i < samplingPlanList.size(); ++i) {
                DataSet samplingPlanDS = (DataSet)samplingPlanList.get(i);
                samplingplanid = samplingplanid + ";" + samplingPlanDS.getString(0, "samplingplanid", "");
                samplingplanversionid = samplingplanversionid + ";" + samplingPlanDS.getString(0, "samplingplanversionid", "");
            }
            samplingplanid = samplingplanid.substring(1);
            samplingplanversionid = samplingplanversionid.substring(1);
        }
        ds.sort("batchtype");
        ArrayList<DataSet> commonBatchTypeList = ds.getGroupedDataSets("batchtype");
        String commonBatchType = "";
        if (commonBatchTypeList.size() == 1) {
            commonBatchType = ds.getString(0, "batchtype", "");
        }
        ajaxResponse.addCallbackArgument("rejectedbatches", "null");
        ajaxResponse.addCallbackArgument("msg", msg);
        ajaxResponse.addCallbackArgument("samplingplanid", samplingplanid);
        ajaxResponse.addCallbackArgument("samplingplanversionidid", samplingplanversionid);
        ajaxResponse.addCallbackArgument("batchtype", commonBatchType);
        ajaxResponse.print();
    }

    private String getRejectedBatches(String batchids) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer("SELECT s_batchid FROM s_batch");
        sql.append(" WHERE s_batchid in (").append(safeSQL.addIn(batchids.replaceAll(";", "','"))).append(")");
        sql.append(" AND batchstatus = 'Rejected'");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        String rejectedBatchids = "";
        if (ds != null && ds.size() > 0) {
            rejectedBatchids = ds.getColumnValues("s_batchid", ",");
        }
        return rejectedBatchids;
    }
}

