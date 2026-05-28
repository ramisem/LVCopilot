/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.logfileviewer;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerUtil;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.StringUtil;

public class LogViewerDownloadHandler
extends BaseRequest {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
        String dothis = request.getParameter("dothis");
        String rolelist = ";" + info.getRoleList() + ";";
        if (!rolelist.contains(";Administrator;") && !rolelist.contains(";IssueSubmitter;")) {
            throw new ServletException("Unable to execute request. You do not have sufficient privileges");
        }
        try {
            if (dothis.equals("DOWNLOADSNAPSHOTS")) {
                boolean deleteDownloadFile;
                String downloadFileName;
                String downloadFilePath;
                String requestedFilesList = request.getParameter("files");
                String[] requestedFiles = StringUtil.split(requestedFilesList, "|");
                String snapshotFolder = LogViewerUtil.getSnapshotFolder(info.getConnectionId());
                if (requestedFiles.length == 1) {
                    downloadFilePath = FileUtil.resolvePath(snapshotFolder, requestedFiles[0]);
                    downloadFileName = requestedFiles[0];
                    deleteDownloadFile = false;
                } else {
                    ArrayList<String> files = new ArrayList<String>();
                    for (String requestedFile : requestedFiles) {
                        files.add(snapshotFolder + "/" + requestedFile + "|" + requestedFile);
                    }
                    File zipFile = this.createZipFile(files);
                    downloadFilePath = zipFile.getAbsolutePath();
                    downloadFileName = "snapshots.zip";
                    deleteDownloadFile = true;
                }
                if (downloadFilePath.length() > 0) {
                    this.streamFileOut(response, downloadFilePath, downloadFileName, deleteDownloadFile);
                }
            } else if (dothis.equals("DOWNLOADREQUESTFILE")) {
                String snapshotFilename = request.getParameter("snapshotfilename");
                String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(this.getConnectionid(), snapshotFilename);
                String filename = request.getParameter("filename");
                String downloadFilename = request.getParameter("downloadfilename");
                File f = new File(FileUtil.resolvePath(snapshotFolderName, filename));
                if (f.exists()) {
                    this.streamFileOut(response, f.getAbsolutePath(), downloadFilename, true);
                }
            }
        }
        catch (Exception e) {
            Trace.logError("Unable to execute LogViewer command", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void streamFileOut(HttpServletResponse response, String downloadFilePath, String downloadFileName, boolean deleteDownloadFile) throws IOException {
        File file = new File(downloadFilePath);
        BufferedInputStream input = null;
        FilterOutputStream output = null;
        try {
            int length;
            input = new BufferedInputStream(new FileInputStream(file), 8192);
            output = new BufferedOutputStream((OutputStream)response.getOutputStream(), 8192);
            response.setContentType(downloadFileName.endsWith("zip") ? "application/zip" : "text");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + downloadFileName + "\"");
            byte[] buffer = new byte[8192];
            while ((length = input.read(buffer)) > -1) {
                ((BufferedOutputStream)output).write(buffer, 0, length);
            }
            ((BufferedOutputStream)output).flush();
        }
        finally {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            if (file.exists() && deleteDownloadFile) {
                file.delete();
            }
        }
    }

    private File createZipFile(ArrayList<String> files) throws IOException, SapphireException {
        File zipFile = File.createTempFile("logfileviewer", "zip");
        zipFile.deleteOnExit();
        FileOutputStream zipFos = null;
        ZipOutputStream zipOut = null;
        try {
            zipFos = new FileOutputStream(zipFile);
            zipOut = new ZipOutputStream(new BufferedOutputStream(zipFos));
            for (String file : files) {
                int length;
                String[] parts = StringUtil.split(file, "|");
                File aFile = new File(parts[0]);
                FileInputStream fis = new FileInputStream(aFile);
                ZipEntry zipEntry = new ZipEntry(parts[parts.length - 1]);
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.closeEntry();
                fis.close();
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to merge files", e);
            throw new SapphireException("Failed to zip log files", e);
        }
        finally {
            try {
                if (zipOut != null) {
                    zipOut.close();
                }
                if (zipFos != null) {
                    zipFos.close();
                }
            }
            catch (IOException iOException) {}
        }
        return zipFile;
    }
}

