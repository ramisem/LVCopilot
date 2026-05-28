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
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CreateStudyUsingTemplate
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String templateid = ajaxResponse.getRequestParameter("templateid", "");
        String studyid = ajaxResponse.getRequestParameter("studyid", "");
        String activeFlag = ajaxResponse.getRequestParameter("activeflag", "");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (templateid.length() == 0) {
            ajaxResponse.setError(tp.translate("Study Template not found."));
            ajaxResponse.print();
        }
        if (studyid.length() == 0 && StringUtil.getLen(this.getSDCProcessor().getProperty("Study", "keygenerationrule")) == 0L) {
            ajaxResponse.setError(tp.translate("Study Id not found."));
            ajaxResponse.print();
        } else {
            try {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "Study");
                actionProps.setProperty("templateid", templateid);
                if (StringUtil.getLen(studyid) > 0L) {
                    actionProps.setProperty("keyid1", studyid);
                    actionProps.setProperty("overrideautokey", "Y");
                }
                actionProps.setProperty("studystatus", "Initial");
                if (activeFlag != null && activeFlag.trim().length() > 0) {
                    actionProps.setProperty("activeflag", activeFlag);
                }
                this.getActionProcessor().processAction("AddSDI", "1", actionProps);
                studyid = actionProps.getProperty("newkeyid1");
                ajaxResponse.addCallbackArgument("studyid", studyid);
            }
            catch (Exception e) {
                Trace.logError(tp.translate("Failed to retrieve policy."), e);
                ajaxResponse.setError(tp.translate("Failed to retrieve policy.") + e.getMessage());
            }
        }
        ajaxResponse.print();
    }
}

