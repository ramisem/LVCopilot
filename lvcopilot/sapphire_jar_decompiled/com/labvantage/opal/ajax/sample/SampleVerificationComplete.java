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

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.actions.sdi.EditSDI;
import java.util.HashSet;
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

public class SampleVerificationComplete
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = StringUtil.replaceAll(ajaxResponse.getRequestParameter("sampleid", ""), "%3B", ";");
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sampleid, samplefamilyid, storagestatus from s_sample where s_sampleid in (" + safeSQL.addIn(sampleid, ";") + ")", safeSQL.getValues());
        if (ds != null) {
            HashSet<String> familySet = new HashSet<String>();
            for (int i = 0; i < ds.size(); ++i) {
                if (!"Verification Needed".equals(ds.getString(i, "storagestatus"))) {
                    message = this.getTranslationProcessor().translate("Sample(s) must be in \"Verification Needed\" status to mark Verification Complete.");
                    break;
                }
                familySet.add(ds.getString(i, "samplefamilyid"));
            }
            if (message.length() == 0 && familySet.size() > 0) {
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("sdcid", "Sample");
                    props.setProperty("keyid1", ds.getColumnValues("s_sampleid", ";"));
                    props.setProperty("storagestatus", "In Circulation");
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                    props.clear();
                    props.setProperty("sdcid", "LV_SampleFamily");
                    props.setProperty("keyid1", OpalUtil.toDelimitedString(familySet, ";"));
                    props.setProperty("verifiedby", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
                    props.setProperty("verifieddt", "n");
                    props.setProperty("conditionalapprovalreason", "(null)");
                    props.setProperty("conditionalapprovalflag", "N");
                    this.getActionProcessor().processActionClass(EditSDI.class.getName(), props);
                }
                catch (ActionException ae) {
                    ErrorHandler ehandler = ae.getErrorHandler();
                    message = "Failed to complete verification.<br>" + ((ErrorDetail)ehandler.get(0)).getMessage() + "<br>";
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.print();
    }
}

