/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.report.digitalsignature.ajax;

import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class GetSigningProperties
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        PropertyList signingPl = new PropertyList();
        try {
            DataSet ds = this.getQueryProcessor().getRefTypeDataSet("SigningMode");
            signingPl.setProperty("signingmode", ds.getColumnValues("refvalueid", ";"));
            DataSet signingProviderDs = this.getQueryProcessor().getSqlDataSet("SELECT propertytreeid FROM propertytree WHERE propertytreetype = 'SigningProvider'");
            signingPl.setProperty("signingprovider", signingProviderDs.getColumnValues("propertytreeid", ";"));
            WebAdminProcessor webadmin = new WebAdminProcessor(this.getConnectionid());
            ConfigurationProcessor configuration = new ConfigurationProcessor(this.getConnectionid());
            signingPl.setProperty("defaultsigningmode", configuration.getSysConfigProperty("signingmode", ""));
            signingPl.setProperty("defaultsigningprovider", configuration.getSysConfigProperty("signingprovider", ""));
            signingPl.setProperty("defaultsigningprovidernode", configuration.getSysConfigProperty("signingprovidernode", ""));
            for (int i = 0; i < signingProviderDs.getRowCount(); ++i) {
                String signingProvider = signingProviderDs.getString(i, "propertytreeid");
                StringBuilder nodeSb = new StringBuilder();
                PropertyTree tree = webadmin.getPropertyTree(signingProvider);
                ArrayList nodes = tree.getAllNodes();
                for (Node node : nodes) {
                    if (node.isLocked()) continue;
                    nodeSb.append(";").append(node.getId());
                }
                if (nodeSb.toString().isEmpty()) continue;
                signingPl.setProperty(signingProvider, nodeSb.substring(1));
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Unable to fetch PDF Signing System Configuration properties.");
        }
        ajaxResponse.addCallbackArgument("value", signingPl.toJSONString());
        ajaxResponse.print();
    }
}

