/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class ViewLabel {
    protected String logName = this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1).toUpperCase();
    protected Logger logger = new Logger(new LogContext(this.logName, "(none)"));

    public void processRequest(Servlet servlet, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        ConnectionInfo connectionInfo = new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionId());
        PropertyList properties = new PropertyList();
        properties.setProperty("sdcid", request.getParameter("sdcid"));
        properties.setProperty("keyid1", request.getParameter("keyid1"));
        properties.setProperty("keyid2", request.getParameter("keyid2"));
        properties.setProperty("keyid3", request.getParameter("keyid3"));
        String labelmethodId = request.getParameter("labelmethodid");
        properties.setProperty("labelmethodid", labelmethodId);
        properties.setProperty("labelmethodversionid", request.getParameter("labelmethodversionid"));
        properties.setProperty("labeleventid", request.getParameter("labeleventid"));
        properties.setProperty("labeldata", request.getParameter("labeldata"));
        properties.setProperty("printeraddressid", request.getParameter("printeraddressid"));
        properties.setProperty("printeraddresstype", request.getParameter("printeraddresstype"));
        properties.setProperty("numcopies", request.getParameter("numcopies"));
        properties.setProperty("jsontest", request.getParameter("jsontest"));
        properties.setProperty("useprintermargins", request.getParameter("useprintermargins"));
        properties.setProperty("returnpdfbytes", "Y");
        try {
            ActionProcessor ap = new ActionProcessor(requestContext.getConnectionId());
            ap.processAction("GenerateLabel", "1", properties);
            Object bytes = properties.get("pdfbytes");
            byte[] reportBytes = (byte[])bytes;
            if (reportBytes != null) {
                SapphireJasperUtil.runReportToWebPdf(reportBytes, response, labelmethodId, connectionInfo, "", false);
            }
        }
        catch (SapphireException sexp) {
            throw new ServletException((Throwable)sexp);
        }
        catch (Throwable e) {
            Logger.logStackTrace(e);
            throw new ServletException("Could not process the label method.", e);
        }
    }
}

