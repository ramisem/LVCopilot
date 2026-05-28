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

import com.labvantage.sapphire.util.UnitsUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.SafeSQL;

public class ValidateBatchUseAmount
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53820 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        QueryProcessor qp = this.getQueryProcessor();
        FormatUtil formatUtil = FormatUtil.getInstance();
        String parentProductid = ajaxResponse.getRequestParameter("parentproductid");
        String parentproductversionid = ajaxResponse.getRequestParameter("parentproductversionid");
        String parentBatchid = ajaxResponse.getRequestParameter("parentbatchid");
        String targetAmount = ajaxResponse.getRequestParameter("targetamount");
        String amountActual = ajaxResponse.getRequestParameter("amountactual");
        String amountUnit = ajaxResponse.getRequestParameter("amountunit");
        String batchItemid = ajaxResponse.getRequestParameter("batchitemid");
        String batchid = ajaxResponse.getRequestParameter("batchid");
        String productId = ajaxResponse.getRequestParameter("productid");
        String productVersionid = ajaxResponse.getRequestParameter("productversionid");
        String prevParentBatchId = "";
        String prevAmountActual = "";
        String prevAmountUnit = "";
        String msg = "";
        try {
            SafeSQL safeSQL = new SafeSQL();
            DataSet batchgenealogyDS = qp.getPreparedSqlDataSet("select parentbatchid, amountactual, amountunits from s_batchgenealogy where s_batchid = " + safeSQL.addVar(batchid) + " and s_batchitemid = " + safeSQL.addVar(batchItemid) + " and parentproductid = " + safeSQL.addVar(parentProductid) + " and parentproductversionid = " + safeSQL.addVar(parentproductversionid), safeSQL.getValues());
            if (batchgenealogyDS.size() > 0) {
                prevParentBatchId = batchgenealogyDS.getString(0, "parentbatchid", "");
                prevAmountActual = batchgenealogyDS.getValue(0, "amountactual", "");
                prevAmountUnit = batchgenealogyDS.getString(0, "amountunits", "");
            }
            safeSQL.reset();
            DataSet trackitemDS = qp.getPreparedSqlDataSet("select qtycurrent,qtyunits from trackitem where linksdcid = 'Batch' and  linkkeyid1 = " + safeSQL.addVar(parentBatchid), safeSQL.getValues());
            if (trackitemDS != null && trackitemDS.size() > 0) {
                Double availQuantity = trackitemDS.getDouble(0, "qtycurrent", 0.0);
                String availQuantityUnit = trackitemDS.getString(0, "qtyunits", "");
                if (parentBatchid.equalsIgnoreCase(prevParentBatchId) && prevAmountActual.trim().length() > 0) {
                    if (prevAmountUnit.equalsIgnoreCase(amountUnit)) {
                        availQuantity = availQuantity + formatUtil.parseBigDecimal(prevAmountActual).doubleValue();
                    } else {
                        String convertedValue = UnitsUtil.getConvertedValue(qp, prevAmountUnit, availQuantityUnit, prevAmountActual);
                        availQuantity = availQuantity + formatUtil.parseBigDecimal(convertedValue).doubleValue();
                    }
                }
                if (!amountUnit.equalsIgnoreCase(availQuantityUnit)) {
                    amountActual = UnitsUtil.getConvertedValue(qp, amountUnit, availQuantityUnit, amountActual.replace(formatUtil.getDecimalSeparator(), '.'));
                }
                if (amountActual != null && formatUtil.parseBigDecimal(amountActual).doubleValue() > availQuantity) {
                    msg = "Warning: Entered Actual Amount exceeds Current Batch quantity. Upon Saving, the Batch quantity will be set to 0. Otherwise correct and use an Amount <= " + availQuantity + " " + availQuantityUnit;
                }
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to validate UseAmount. Exception: " + e.getMessage(), e);
        }
        ajaxResponse.addCallbackArgument("message", msg);
        ajaxResponse.print();
    }
}

