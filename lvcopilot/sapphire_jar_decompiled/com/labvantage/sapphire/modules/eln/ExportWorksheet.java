/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.words.SaveFormat
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.eln;

import com.aspose.words.SaveFormat;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.modules.eln.WordWorksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class ExportWorksheet
extends BaseRequest
implements ELNConstants {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block41: {
            try {
                String worksheetid = request.getParameter("worksheetid");
                String worksheetversionid = request.getParameter("worksheetversionid");
                String exportto = request.getParameter("exportto");
                PropertyList exportOptions = new PropertyList(new JSONObject(request.getParameter("exportoptions") != null && request.getParameter("exportoptions").length() > 0 ? request.getParameter("exportoptions") : "{}"));
                String reporteventid = request.getParameter("reporteventid");
                exportOptions.setProperty("exportto", exportto);
                String connectionid = this.getConnectionid();
                if (connectionid == null || connectionid.length() == 0) {
                    PrintWriter out = response.getWriter();
                    out.println("Connection has expired. Unable to regenerate export page. <br><br>Please log in and try again.");
                    break block41;
                }
                if (OpalUtil.isNotEmpty(reporteventid)) {
                    SapphireReportEvent event = new SapphireReportEvent(reporteventid, "LV_Worksheet", this.getConnectionProcessor().getSapphireConnection());
                    WordWorksheet export = new WordWorksheet(this.getConnectionProcessor().getSapphireConnection(), event.getWorksheetid(), event.getWorksheetversionid(), exportOptions);
                    if (event.getDisplaytype().equalsIgnoreCase(SaveFormat.getName((int)20).toLowerCase())) {
                        export.streamToWordForExistingEvent(response, event);
                    } else if (event.getDisplaytype().equalsIgnoreCase(SaveFormat.getName((int)40).toLowerCase())) {
                        export.streamToPdfForExistingEvent(response, event);
                    }
                    break block41;
                }
                if (OpalUtil.isNotEmpty(exportto) && exportto.equalsIgnoreCase("ZIP")) {
                    ActionProcessor ap = new ActionProcessor(this.getConnectionid());
                    PropertyList actionProps = new PropertyList();
                    actionProps.setProperty("sdcid", "LV_Worksheet");
                    actionProps.setProperty("keyid1", worksheetid);
                    actionProps.setProperty("keyid2", worksheetversionid);
                    actionProps.setProperty("dirname", System.getProperty("java.io.tmpdir") + File.separator + "worksheetexport");
                    actionProps.setProperty("filename", worksheetid + "_" + worksheetversionid + ".xml");
                    actionProps.setProperty("exporttype", "E");
                    actionProps.setProperty("exportid", "Standard");
                    actionProps.setProperty("zipoutput", "Y");
                    ap.processAction("ExportSDI", "1", actionProps);
                    File file = new File(actionProps.getProperty("dirname"), worksheetid + "_" + worksheetversionid + ".zip");
                    try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file), 8192);
                         BufferedOutputStream output = new BufferedOutputStream((OutputStream)response.getOutputStream(), 8192);){
                        int length;
                        response.setContentType("application/zip");
                        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
                        byte[] buffer = new byte[8192];
                        while ((length = input.read(buffer)) > -1) {
                            output.write(buffer, 0, length);
                        }
                        output.flush();
                    }
                    finally {
                        Files.deleteIfExists(file.toPath());
                    }
                    this.logActivity(worksheetid, worksheetversionid, reporteventid, exportto);
                    break block41;
                }
                ConnectionProcessor cp = new ConnectionProcessor(this.getConnectionid());
                WordWorksheet export = new WordWorksheet(cp.getSapphireConnection(), worksheetid, worksheetversionid, exportOptions);
                export.createDocument();
                if (exportto.equalsIgnoreCase("Word")) {
                    reporteventid = export.streamToWord(response);
                } else if (exportto.equalsIgnoreCase("PDF")) {
                    reporteventid = export.streamToPdf(response);
                } else if (exportto.equalsIgnoreCase("HTML")) {
                    export.streamToHTML(response);
                }
                this.logActivity(worksheetid, worksheetversionid, reporteventid, exportto);
            }
            catch (Exception e) {
                Logger.logStackTrace(e);
                throw new ServletException(ErrorUtil.extractMessage("Could not export the worksheet. ", ErrorUtil.isUserAdmin(this.getConnectionId())), (Throwable)e);
            }
        }
    }

    private void logActivity(String worksheetid, String worksheetversionid, String reporteventid, String exportto) throws ActionException {
        PropertyList activityProps = new PropertyList();
        activityProps.setProperty("worksheetid", worksheetid);
        activityProps.setProperty("worksheetversionid", worksheetversionid);
        activityProps.setProperty("reporteventid", reporteventid);
        activityProps.setProperty("activitytype", "Export");
        activityProps.setProperty("activitylog", "Exported worksheet to " + exportto);
        ActionProcessor actionProcessor = new ActionProcessor(this.getConnectionid());
        actionProcessor.processActionClass(AddWorksheetActivity.class.getName(), activityProps);
    }
}

