/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.empower;

import com.labvantage.sapphire.modules.empower.EmpowerDownloadProcessor;
import com.labvantage.sapphire.modules.empower.EmpowerPolicyDef;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class EmpowerDownload
extends BaseAjaxRequest {
    public static final String EMPOWER_POLICY = "EmpowerPolicy";
    public static final String EMPOWER_DEFAULT_NODE = "Sapphire Product";
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "top.wizard_iframe.downloadCallback");
        String qcbatchid = ajaxResponse.getRequestParameter("qcbatchid");
        String empowerProject = ajaxResponse.getRequestParameter("empowerproject");
        String empowerDatabase = ajaxResponse.getRequestParameter("empowerdatabase");
        String sampleSetMethodName = ajaxResponse.getRequestParameter("samplesetmethodname");
        if (qcbatchid.length() == 0) {
            ajaxResponse.setError("filename not specified");
            ajaxResponse.print();
            return;
        }
        try {
            String node = ajaxResponse.getRequestParameter("policynode", "");
            if (node == null || node.length() == 0) {
                node = EMPOWER_DEFAULT_NODE;
            }
            PropertyList policy = this.getConfigurationProcessor().getPolicy(EMPOWER_POLICY, node);
            EmpowerPolicyDef policyDef = new EmpowerPolicyDef(policy);
            EmpowerDownloadProcessor processor = new EmpowerDownloadProcessor(policyDef, this.getActionProcessor(), this.getQueryProcessor(), this.getConnectionProcessor());
            processor.process(qcbatchid, "AQC Mode");
            DataSet ssm = processor.getSampleSetMethod();
            DataSet ssl = processor.getSampleSetLines();
            DataSet comps = processor.getComponents();
            ajaxResponse.addCallbackArgument("samplesetmethod", ssm.toJSONObject(true, true));
            ajaxResponse.addCallbackArgument("samplesetlines", ssl.toJSONObject(true, true));
            ajaxResponse.addCallbackArgument("components", comps.toJSONObject(true, true));
        }
        catch (SapphireException e) {
            ajaxResponse.setError("Failed to download empower data" + e.getMessage());
        }
        ajaxResponse.print();
    }
}

