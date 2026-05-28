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

public class GetProtocolRulePolicyProperty
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String policyId = ajaxResponse.getRequestParameter("policyid", "");
        String nodeId = ajaxResponse.getRequestParameter("nodeid", "");
        String propertyList = ajaxResponse.getRequestParameter("propertylist", "");
        String propertyId = ajaxResponse.getRequestParameter("propertyid", "");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (policyId.length() == 0) {
            ajaxResponse.setError(tp.translate("Policy Id not defined."));
            ajaxResponse.print();
        }
        if (nodeId.length() == 0) {
            ajaxResponse.setError(tp.translate("Node Id not defined."));
            ajaxResponse.print();
        }
        if (propertyList.length() == 0) {
            ajaxResponse.setError(tp.translate("Property List not defined."));
            ajaxResponse.print();
        }
        if (propertyId.length() == 0) {
            ajaxResponse.setError(tp.translate("Property Id not defined."));
        } else {
            try {
                ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
                PropertyList pl = configProcessor.getPolicy(policyId, nodeId);
                PropertyList rules = pl.getPropertyList(propertyList);
                String hasProtocolRule = "";
                if (rules == null || rules.size() == 0) {
                    HashMap<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("propertyId", propertyId);
                    valueMap.put("policyId", policyId);
                    ajaxResponse.setError(tp.translate("Property [propertyId] definition is missing in the policy [policyId]", valueMap));
                } else if (rules.getProperty(propertyId).length() > 0) {
                    hasProtocolRule = rules.getProperty(propertyId);
                }
                this.logger.info("GetProtocolRulePolicyProperty response - Study Has Protocol Rule " + hasProtocolRule);
                ajaxResponse.addCallbackArgument("hasProtocolRule", hasProtocolRule);
            }
            catch (Exception e) {
                Trace.logError(tp.translate("Failed to retrieve policy."), e);
                ajaxResponse.setError(tp.translate("Failed to retrieve policy.") + e.getMessage());
            }
        }
        ajaxResponse.print();
    }
}

