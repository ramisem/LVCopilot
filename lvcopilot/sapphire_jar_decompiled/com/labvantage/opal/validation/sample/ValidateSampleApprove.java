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

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.opal.validation.BaseAjaxValidation;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateSampleApprove
extends BaseAjaxValidation {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid", "").trim();
        String approvaltype = ajaxResponse.getRequestParameter("approvaltype", "Approval");
        if (OpalUtil.isNotEmpty(sampleid)) {
            sampleid = StringUtil.replaceAll(sampleid, "%3B", ";");
            SafeSQL safeSQL = new SafeSQL();
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s.s_sampleid, s.storagestatus, sf.conditionalapprovalflag from s_sample s, s_samplefamily sf where s.s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ") and sf.s_samplefamilyid = s.samplefamilyid", safeSQL.getValues());
            if (ds != null) {
                for (int i = 0; i < ds.size(); ++i) {
                    String storagestatus = ds.getString(i, "storagestatus");
                    String conditionalapprovalflag = ds.getString(i, "conditionalapprovalflag", "N");
                    if ("Approval".equals(approvaltype) || "Full".equals(approvaltype)) {
                        if ("In Circulation".equals(storagestatus)) {
                            if ("Y".equals(conditionalapprovalflag)) continue;
                            message = this.getTranslationProcessor().translate("One or more of the selected sample(s) is already Approved");
                            break;
                        }
                        if ("Allocated".equals(storagestatus) || "Received".equals(storagestatus) || "Temporary In Lab".equals(storagestatus)) continue;
                        message = this.getTranslationProcessor().translate("Only Allocated, Received, Temporary In Lab or Conditionally approved samples can be Approved");
                        break;
                    }
                    if ("Conditional".equals(approvaltype)) {
                        if ("Allocated".equals(storagestatus) || "Received".equals(storagestatus) || "Temporary In Lab".equals(storagestatus)) continue;
                        message = this.getTranslationProcessor().translate("Only Allocated, Received or Temporary In Lab samples can be Conditionally Approved");
                        break;
                    }
                    if ("Verification".equals(approvaltype)) {
                        if ("Verification Needed".equals(storagestatus)) continue;
                        message = this.getTranslationProcessor().translate("All the selected samples for Verification must have the status of Verification Needed");
                        break;
                    }
                    if (!"request".equals(approvaltype) || "In Circulation".equals(storagestatus)) continue;
                    message = this.getTranslationProcessor().translate("Only \"In Circulation\" samples can be requested for verification");
                    break;
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("approvaltype", approvaltype);
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.print();
    }
}

