/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.aqm;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.validation.BaseAjaxValidation;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class ValidateBatchOperations
extends BaseAjaxValidation {
    protected static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String operation = ajaxResponse.getRequestParameter("operation");
        ErrorHandler errorHandler = new ErrorHandler();
        if (operation.equalsIgnoreCase("unrelease")) {
            this.validateUnRelease(ajaxResponse, errorHandler);
        }
        String message = "";
        if (errorHandler.hasErrors() || errorHandler.size() > 0) {
            message = ErrorUtil.formatErrorMessage(errorHandler);
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private void validateUnRelease(AjaxResponse ajaxResponse, ErrorHandler errorHandler) {
        String validateSdcId = ajaxResponse.getRequestParameter("validatesdcid", "Batch");
        String keyid1List = ajaxResponse.getRequestParameter("keyid1");
        String[] keyid1Array = StringUtil.split(keyid1List, ";");
        StringBuffer noApprovalSDIs = new StringBuffer();
        for (int i = 0; i < keyid1Array.length; ++i) {
            String keyid1 = keyid1Array[i];
            String sql = "SELECT approvaltypeid FROM sdiapproval WHERE sdcid = '" + validateSdcId + "' AND keyid1 = ?";
            DataSet approvalDs = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{keyid1});
            if (approvalDs.getRowCount() != 0) continue;
            noApprovalSDIs.append(",").append("<br>").append(keyid1);
        }
        if (noApprovalSDIs.length() > 0) {
            if (validateSdcId.equals("LV_BatchStage")) {
                errorHandler.add("BatchStage UnRelease Validation", "", "Validation failed", "VALIDATION", this.getTranslationProcessor().translate("Only stages requiring approval can be Un-Released. Following Batch-Stage do not have approvals:") + noApprovalSDIs.substring(1) + ".");
            } else {
                errorHandler.add("Batch UnRelease Validation", "", "Validation failed", "VALIDATION", this.getTranslationProcessor().translate("Only batches requiring approval can be Un-Released. Following Batches do not have approvals:") + noApprovalSDIs.substring(1) + ".");
            }
        }
    }
}

