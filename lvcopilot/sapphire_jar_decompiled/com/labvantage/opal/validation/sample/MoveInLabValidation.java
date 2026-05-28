/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.sample;

import com.labvantage.opal.handler.ErrorUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class MoveInLabValidation
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53421 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid1"), "%3B", ";");
        String invalidSample = this.getInvalidSample(sampleid);
        ErrorHandler errorHandler = new ErrorHandler();
        if (invalidSample.length() > 0) {
            errorHandler.add(this.getTranslationProcessor().translate("MoveInLab Validation"), "", this.getTranslationProcessor().translate("Validation failure"), "VALIDATION", this.getTranslationProcessor().translate("Following samples are not confirmed or not in Allocated status") + "<br><ul><li>" + StringUtil.replaceAll(invalidSample, ";", "</li><li>") + "</li></ul>");
        }
        if (errorHandler.size() > 0) {
            message = ErrorUtil.formatErrorMessage(errorHandler);
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }

    private String getInvalidSample(String sampleid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuffer sql = new StringBuffer();
        sql.append(" select distinct s_sampleid from s_sample where s_sampleid in (");
        sql.append(safeSQL.addIn(sampleid, ";"));
        sql.append(") ");
        sql.append(" and ( confirmeddt is not null or storagestatus != 'Allocated' )");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        return ds.size() > 0 ? ds.getColumnValues("s_sampleid", ";") : "";
    }
}

