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

import com.labvantage.sapphire.Trace;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class CheckForQCBatchApproval
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block5: {
            ajaxResponse = new AjaxResponse(request, response);
            String qcBatchId = ajaxResponse.getRequestParameter("qcbatchid", "");
            String rowNum = ajaxResponse.getRequestParameter("rownum", "");
            String disposition = ajaxResponse.getRequestParameter("disposition", "");
            TranslationProcessor tp = this.getTranslationProcessor();
            if (qcBatchId.length() == 0) {
                ajaxResponse.setError(tp.translate("No QCBatch Id specified."));
                ajaxResponse.print();
            }
            try {
                ConfigurationProcessor cp = new ConfigurationProcessor(this.getConnectionId());
                PropertyList policy = cp.getPolicy("AQCPolicy", "Sapphire Custom");
                if (policy != null) {
                    String policyValue = policy.getProperty("qcbatchreviewvalidation", "Independent of Dataset Approval");
                    String allowApproval = "";
                    if ("Requires Dataset Approval".equalsIgnoreCase(policyValue)) {
                        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select 1 from sdidata where s_qcbatchid = ?  and s_datasetstatus not in( 'Completed','Cancelled' )", (Object[])new String[]{qcBatchId});
                        allowApproval = ds.getRowCount() > 0 ? "N" : "Y";
                        ajaxResponse.addCallbackArgument("allowApproval", allowApproval);
                        ajaxResponse.addCallbackArgument("rownum", rowNum);
                        ajaxResponse.addCallbackArgument("disposition", disposition);
                    }
                    break block5;
                }
                throw new SapphireException(tp.translate("Failed to retrieve policy."));
            }
            catch (Exception e) {
                Trace.logError(tp.translate("Failed to retrieve policy."), e);
                ajaxResponse.setError(tp.translate("Failed to retrieve policy.") + e.getMessage());
            }
        }
        ajaxResponse.print();
    }
}

