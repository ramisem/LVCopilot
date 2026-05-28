/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.tools.ant.filters.StringInputStream
 */
package com.labvantage.sapphire.messaging;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerUtil;
import com.labvantage.sapphire.messaging.MessageLogUtil;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.tools.ant.filters.StringInputStream;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.xml.PropertyListCollection;

public class MessageLogDetailsDownloadHandler
extends BaseRequest {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
        try {
            String dothis = request.getParameter("dothis");
            if (dothis.equals("GETVALIDATIONLOG")) {
                String messagelogid = request.getParameter("messagelogid");
                String downloadFilename = messagelogid + "_validationlog.html";
                String snapshotFolder = LogViewerUtil.getSnapshotFolder(info.getConnectionId());
                this.streamFileOut(response, messagelogid, "validationlog", snapshotFolder, downloadFilename);
            } else if (dothis.equals("GETPROCESSLOG")) {
                String messagelogid = request.getParameter("messagelogid");
                String downloadFilename = messagelogid + "_processlog.html";
                String snapshotFolder = LogViewerUtil.getSnapshotFolder(info.getConnectionId());
                this.streamFileOut(response, messagelogid, "processlog", snapshotFolder, downloadFilename);
            }
        }
        catch (Exception e) {
            Trace.logError("Unable to execute download validationlog command", e);
        }
    }

    public void streamFileOut(HttpServletResponse response, String messagelogid, String column, String downloadFilePath, String downloadFileName) throws SapphireException {
        File file = new File(downloadFilePath);
        BufferedOutputStream output = null;
        try {
            output = new BufferedOutputStream((OutputStream)response.getOutputStream(), 8192);
            response.setContentType("html");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName + "\"");
            String sql = "SELECT " + column + " FROM messagelog WHERE messagelogid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{messagelogid}, true);
            if (ds != null && ds.size() > 0) {
                String log = ds.getValue(0, column);
                if (log.length() > 0) {
                    if (column.equals("validationlog")) {
                        PropertyListCollection validationreport = new PropertyListCollection();
                        validationreport.setJSONString(log);
                        if (validationreport.size() > 0) {
                            int length;
                            String validationlogtxt = MessageLogUtil.getValidationLogHtml(this.getTranslationProcessor(), validationreport);
                            StringInputStream stringInputStream = new StringInputStream(validationlogtxt);
                            byte[] buffer = new byte[8192];
                            while ((length = stringInputStream.read(buffer)) > -1) {
                                output.write(buffer, 0, length);
                            }
                            output.flush();
                            stringInputStream.close();
                        }
                    } else {
                        int length;
                        String css = "<style>P.processlog { font: Arial;font-size:10pt;}\ntable.viewlist {\n    font: Arial;\n    font-size: 10pt;\n    margin-top: 10px;\n    margin-left: 20px;\n    border: 1px solid #333;\n    padding: 0;\n    border-spacing: 0;\n    border-collapse: collapse;\n    empty-cells: show\n}\nth.viewlisthead {\n    background: gainsboro;\n    border: 1px solid #333\n}\ntd.viewlistcol {\n    font: Arial;\n    font-size: 10pt;\n    border: 1px solid #333\n}</style>";
                        String processlogtxt = css + "\n" + log.replaceAll("\\n", "<P class=processlog>");
                        StringInputStream stringInputStream = new StringInputStream(processlogtxt);
                        byte[] buffer = new byte[8192];
                        while ((length = stringInputStream.read(buffer)) > -1) {
                            output.write(buffer, 0, length);
                        }
                        output.flush();
                        stringInputStream.close();
                    }
                }
                output.close();
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to download validation log", e);
            throw new SapphireException("Failed to download validation log for messagelogid " + messagelogid, e);
        }
    }
}

