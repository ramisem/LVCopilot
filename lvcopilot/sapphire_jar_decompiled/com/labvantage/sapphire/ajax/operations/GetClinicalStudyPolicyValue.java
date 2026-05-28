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
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetClinicalStudyPolicyValue
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String policyId = ajaxResponse.getRequestParameter("policyid", "");
        String nodeId = ajaxResponse.getRequestParameter("nodeid", "");
        String propertyId = ajaxResponse.getRequestParameter("propertyid", "");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (policyId.length() == 0) {
            ajaxResponse.setError(tp.translate("Policy Id not defined."));
            ajaxResponse.print();
        }
        if (propertyId.length() == 0) {
            ajaxResponse.setError(tp.translate("Property Id not defined."));
        } else {
            try {
                String templateVal = "";
                ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
                PropertyList pl = configProcessor.getPolicy(policyId, nodeId);
                PropertyList addstudyoptions = pl.getPropertyList(propertyId);
                String promptfortemplate = "";
                String defaulttemplate = "";
                String promptPageToChooseTemplate = "";
                String promptPageToInputStudy = "";
                if (addstudyoptions == null || addstudyoptions.size() == 0) {
                    HashMap<String, String> tokenMap = new HashMap<String, String>();
                    tokenMap.put("propertyId", propertyId);
                    tokenMap.put("policyId", policyId);
                    ajaxResponse.setError(tp.translate("Property [propertyId] definition is missing in the policy [policyId]", tokenMap));
                } else {
                    if (addstudyoptions.getProperty("promptfortemplate").length() > 0) {
                        promptfortemplate = addstudyoptions.getProperty("promptfortemplate");
                    }
                    if (addstudyoptions.getProperty("defaulttemplate").length() > 0) {
                        defaulttemplate = addstudyoptions.getProperty("defaulttemplate");
                    }
                    if (addstudyoptions.getProperty("promptpagetochoosetemplate").length() > 0) {
                        promptPageToChooseTemplate = addstudyoptions.getProperty("promptpagetochoosetemplate");
                    }
                    if (addstudyoptions.getProperty("promptpagetoinputstudyid").length() > 0) {
                        promptPageToInputStudy = addstudyoptions.getProperty("promptpagetoinputstudyid");
                    }
                }
                this.logger.info("GetClinicalStudyPolicyValue response - prompt for template " + promptfortemplate + " ; default template : " + defaulttemplate);
                ajaxResponse.addCallbackArgument("promptfortemplate", promptfortemplate);
                ajaxResponse.addCallbackArgument("defaulttemplate", defaulttemplate);
                ajaxResponse.addCallbackArgument("promptpagetochoosetemplate", promptPageToChooseTemplate);
                ajaxResponse.addCallbackArgument("promptpagetoinputstudy", promptPageToInputStudy);
            }
            catch (Exception e) {
                Trace.logError(tp.translate("Failed to retrieve policy."), e);
                ajaxResponse.setError(tp.translate("Failed to retrieve policy.") + e.getMessage());
            }
        }
        ajaxResponse.print();
    }
}

