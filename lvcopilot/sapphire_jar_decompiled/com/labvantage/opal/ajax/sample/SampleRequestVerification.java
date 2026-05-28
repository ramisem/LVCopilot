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

import com.labvantage.sapphire.actions.sdi.EditSDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SampleRequestVerification
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = StringUtil.replaceAll(ajaxResponse.getRequestParameter("sampleid", ""), "%3B", ";");
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid, samplefamilyid, storagestatus from s_sample where s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ")", safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                if ("In Circulation".equals(ds.getString(i, "storagestatus"))) continue;
                message = this.getTranslationProcessor().translate("Sample(s) must be in \"In Circulation\" status to Request Verification.");
                break;
            }
            if (message.length() == 0) {
                PropertyList props = new PropertyList();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", ds.getColumnValues("s_sampleid", ";"));
                props.setProperty("storagestatus", "Verification Needed");
                try {
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
                catch (ActionException ae) {
                    ErrorHandler ehandler = ae.getErrorHandler();
                    message = this.getTranslationProcessor().translate("Failed to request verification.") + "<br>" + ((ErrorDetail)ehandler.get(0)).getMessage() + "<br>";
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

