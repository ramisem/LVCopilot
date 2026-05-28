/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn;

import com.labvantage.sapphire.pageelements.workflow.bpmn.BPMNExporter;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.ByteArrayInputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class BPMNDownloader
extends BaseRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        try {
            String error = "";
            String keyid1 = request.getParameter("keyid1");
            String keyid2 = request.getParameter("keyid2");
            String keyid3 = request.getParameter("keyid3");
            String properties = request.getParameter("properties");
            boolean xpdl = request.getParameter("xpdl") != null ? request.getParameter("xpdl").equalsIgnoreCase("Y") : false;
            PropertyList workflowprops = null;
            if (properties != null && properties.length() > 0) {
                try {
                    workflowprops = new PropertyList(new JSONObject(properties));
                }
                catch (Exception e) {
                    Logger.logWarn("Could not parse properties.");
                }
            }
            if (workflowprops != null) {
                workflowprops.setProperty("workflowdefid", keyid1 != null ? keyid1 : "");
                workflowprops.setProperty("workflowdefversionid", keyid2 != null ? keyid2 : "");
                workflowprops.setProperty("workflowdefvariantid", keyid3 != null ? keyid3 : "");
                String xmlstring = "";
                try {
                    xmlstring = BPMNExporter.exportBPMN(workflowprops, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId(), xpdl);
                    byte[] bytes = xmlstring.getBytes("UTF-8");
                    response.setContentType("application/force-download");
                    response.setContentLength(bytes.length);
                    response.setHeader("Content-Disposition", "attachment;filename=" + keyid1 + "(" + keyid2 + "_" + keyid3 + ")." + (xpdl ? "xpdl" : "bpmn") + "");
                    try (ServletOutputStream out = response.getOutputStream();){
                        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);){
                            out.write(bytes, 0, bytes.length);
                        }
                        out.flush();
                    }
                }
                catch (Exception e) {
                    error = e.getMessage();
                }
            } else {
                error = "No properties provided.";
            }
            if (error.length() > 0) {
                Logger.logError(error);
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }
}

