/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;

public class DownloadRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void download(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block13: {
            try {
                QueryProcessor qp;
                FileManager.TempFile outP;
                if (request.getParameter("id") == null || request.getParameter("id").length() <= 0) break block13;
                RequestContext rc = RequestContext.getRequestContext(request);
                String id = rc.getProperty("id");
                if (id.length() == 0) {
                    id = rc.getProperty("tempid");
                }
                if ((outP = FileManager.TempFile.getTempFile(id, false, qp = new QueryProcessor(rc.getConnectionId()), rc.getConnectionId())) != null) {
                    byte[] data = outP.getData().getData();
                    if (outP.getData().getData().length > 0) {
                        long downloadLimit = FileManager.getDownloadMaxFileSize(rc.getConnectionId());
                        if ((long)outP.getData().getData().length < downloadLimit) {
                            try {
                                response.setContentType(outP.getMimeType().length() > 0 ? outP.getMimeType() : FileType.getFileTypeByName("UNKNOWN", rc.getConnectionId()).getMime());
                                response.setHeader("Content-disposition", "attachment; filename=" + HttpUtil.encodeURIComponent(outP.getFileName()));
                                ServletOutputStream outputStream = response.getOutputStream();
                                try {
                                    outputStream.write(data);
                                    if (rc.getProperty("selfdestruct").equalsIgnoreCase("Y")) {
                                        FileManager.TempFile.removeTempFile(id, new ActionProcessor(rc.getConnectionId()), qp, rc.getConnectionId());
                                    }
                                    break block13;
                                }
                                finally {
                                    outputStream.flush();
                                    outputStream.close();
                                }
                            }
                            catch (Exception e) {
                                Trace.logError("Could not obtain temp file. Error = " + e.getMessage());
                            }
                            break block13;
                        }
                        if (downloadLimit == 0L) {
                            throw new ServletException("Downloads swtiched off in system configuration from zero download limit.");
                        }
                        throw new ServletException("Data too large to download.");
                    }
                    throw new ServletException("No temp file data.");
                }
                throw new ServletException("Failed to find temp file.");
            }
            catch (Exception e) {
                Logger.logError("Could not parse request.", e);
                throw new ServletException("Could not parse request.", (Throwable)e);
            }
        }
    }
}

