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

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.empower.EmpowerUploadProcessor;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class EmpowerUploadDataAjaxSetter
extends BaseAjaxRequest {
    public static final String DELIMITER = ";";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "uploadDataSetter_AjaxCallback");
        ajaxResponse.setErrorCallback("uploadDataSetter_AjaxErrorCallback");
        String selecteddataStr = ajaxResponse.getRequestParameter("selecteddata");
        String silentUpload = ajaxResponse.getRequestParameter("silentupload");
        String samplesetname = ajaxResponse.getRequestParameter("samplesetname");
        String impersonationUser = ajaxResponse.getRequestParameter("impersonationuser");
        if (silentUpload.equals("true")) {
            String policynode = ajaxResponse.getRequestParameter("policynode");
            String projectname = ajaxResponse.getRequestParameter("projectname");
            String databasename = ajaxResponse.getRequestParameter("databasename");
            try {
                PropertyList selecteddataPL = new PropertyList(new JSONObject(selecteddataStr));
                HashMap map = EmpowerUploadProcessor.createEmpowerMessageDS(this.getConfigurationProcessor(), this.getConnectionProcessor(), this.getConnectionProcessor().getConnectionid(), policynode, selecteddataPL);
                if (map == null) {
                    throw new SapphireException("Failed to create upload datasets.");
                }
                String ss = (String)map.get("SampleSet");
                String ssl = (String)map.get("SampleSetLines");
                String results = (String)map.get("Results");
                String peaks = (String)map.get("Peaks");
                PropertyList properties = new PropertyList();
                properties.setProperty("sampleset", ss);
                properties.setProperty("samplesetlines", ssl);
                properties.setProperty("results", results);
                properties.setProperty("peaks", peaks);
                properties.setProperty("analyst", impersonationUser);
                properties.setProperty("policynode", policynode);
                properties.setProperty("empowerproject", projectname);
                properties.setProperty("empowerdatabase", databasename);
                this.getActionProcessor().processAction("EmpowerUpload", "1", properties);
            }
            catch (SapphireException e) {
                Trace.logError("Silent Mode: Error processing upload request." + e.getMessage());
                ajaxResponse.setError("Silent Mode: Error processing upload request." + e.getMessage());
            }
            catch (JSONException e) {
                Trace.logError("Silent Mode: Error processing upload request." + e.getMessage());
                ajaxResponse.setError("Silent Mode: Error processing upload request." + e.getMessage());
            }
        }
        request.getSession().setAttribute("selecteddata", (Object)selecteddataStr);
        ajaxResponse.print();
    }
}

