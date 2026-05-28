/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.servlet.command.MultipartRequest;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import com.labvantage.sapphire.util.images.ImageRef;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

public class UploadRequest
extends MultipartRequest {
    private String tempMultiPart(HttpServletRequest request, HttpServletResponse response, String connectionId, List fileItems, JSONObject job) throws IOException {
        String errormessage;
        block65: {
            FileItem fifile;
            String fileName;
            long maxsize;
            String tempid;
            block63: {
                tempid = "";
                PrintWriter out = response.getWriter();
                errormessage = "";
                String ms = request.getHeader("maxfilesize");
                maxsize = -1L;
                if (ms != null && ms.length() > 0) {
                    try {
                        maxsize = Long.parseLong(ms);
                        if (maxsize <= 0L) {
                            maxsize = FileManager.getUploadMaxFileSize(connectionId);
                            break block63;
                        }
                        maxsize *= 1000000L;
                    }
                    catch (Exception e) {
                        maxsize = FileManager.getUploadMaxFileSize(connectionId);
                    }
                } else {
                    maxsize = FileManager.getUploadMaxFileSize(connectionId);
                }
            }
            if ((fileName = FileUpload.getFileName(fifile = FileUpload.getFileItem(fileItems, "file"))) != null) {
                try {
                    String type = fifile.getContentType();
                    String createtempfile = FileUpload.getFileItemValue(fileItems, "createtempfile");
                    boolean createTempFile = createtempfile == null ? true : !createtempfile.equalsIgnoreCase("N");
                    String s = FileUpload.getFileItemValue(fileItems, "usefilesystem");
                    boolean useFileSystem = createTempFile ? s == null || s.length() == 0 || !s.equalsIgnoreCase("N") : true;
                    UploadSource source = UploadSource.OTHER;
                    try {
                        source = UploadSource.valueOf(FileUpload.getFileItemValue(fileItems, "uploadsource"));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    String elementid = FileUpload.getFileItemValue(fileItems, "elementid");
                    if (elementid != null && elementid.length() > 0) {
                        job.put("elementid", elementid);
                    }
                    boolean showThumbnails = (s = FileUpload.getFileItemValue(fileItems, "showthumbnails")) == null || !s.equalsIgnoreCase("N");
                    String filelocationpolicy = FileUpload.getFileItemValue(fileItems, "locationpolicynode");
                    if (filelocationpolicy == null || filelocationpolicy.length() == 0) {
                        filelocationpolicy = "Upload Custom";
                    }
                    job.put("locationpolicynode", filelocationpolicy);
                    String filelocationpolicyitem = FileUpload.getFileItemValue(fileItems, "locationpolicyitem");
                    if (filelocationpolicyitem == null) {
                        filelocationpolicyitem = "";
                    }
                    job.put("locationpolicyitem", filelocationpolicyitem);
                    if (FileManager.isValidFileType(fileName, filelocationpolicy, connectionId)) {
                        boolean cont;
                        String usablefilename;
                        block64: {
                            FileManager.FileData fileData = new FileManager.FileData(fifile.getInputStream(), type, useFileSystem);
                            if (source == UploadSource.FILEUPLOADER && showThumbnails && FileTypeGroup.getFileTypeGroupByType(fileData.getMimetype(), connectionId) == FileTypeGroup.IMAGE) {
                                showThumbnails = false;
                            }
                            usablefilename = fileName;
                            String renameex = FileUpload.getFileItemValue(fileItems, "renameexpression");
                            if (renameex.length() > 0) {
                                String[] tok = StringUtil.getExpressionTokens(renameex);
                                usablefilename = renameex;
                                for (int i = 0; i < tok.length; ++i) {
                                    String v = "";
                                    if (tok[i].equalsIgnoreCase("filename")) {
                                        v = FileManager.getFileName(fileName, false);
                                    } else if (tok[i].equalsIgnoreCase("extension")) {
                                        v = FileManager.getExtension(fileName);
                                    } else if (tok[i].equalsIgnoreCase("currentuser")) {
                                        v = new ConnectionProcessor(connectionId).getSapphireConnection().getSysuserId();
                                    } else if (tok[i].equalsIgnoreCase("timestamp")) {
                                        v = System.currentTimeMillis() + "";
                                    }
                                    usablefilename = StringUtil.replaceAll(usablefilename, "[" + tok[i] + "]", v);
                                }
                            }
                            cont = false;
                            if (createTempFile) {
                                FileManager.TempFile tempFile = new FileManager.TempFile(fileData, usablefilename, FileManager.TempSource.UPLOAD, showThumbnails, connectionId);
                                tempid = tempFile.setTempFile(maxsize, filelocationpolicy, connectionId);
                                if (job != null) {
                                    try {
                                        job.put("tempid", tempid);
                                        if (showThumbnails) {
                                            String t = tempFile.getThumbnail();
                                            if (t == null) {
                                                FileType fileType = FileType.getFileType(usablefilename, connectionId);
                                                ConnectionProcessor cp = new ConnectionProcessor(connectionId);
                                                ImageRef imageRef = new ImageRef(cp.getSapphireConnection());
                                                imageRef.setImage(fileType.getImageRefId());
                                                job.put("thumbnail", imageRef.getSrc());
                                            } else {
                                                job.put("thumbnail", t);
                                            }
                                        }
                                        cont = true;
                                    }
                                    catch (Exception t) {}
                                }
                            } else if (fileData.getSize() < maxsize) {
                                String subdirectory = FileUpload.getFileItemValue(fileItems, "subdirectory");
                                String filelocation = FileManager.getFileLocation(filelocationpolicy, filelocationpolicyitem, connectionId);
                                Path finalpath = null;
                                Path path = null;
                                try {
                                    path = Paths.get(filelocation, new String[0]);
                                    if (subdirectory != null && subdirectory.length() > 0) {
                                        path = FileUtil.resolvePath(path, subdirectory);
                                    }
                                    finalpath = FileUtil.resolvePath(path, usablefilename);
                                }
                                catch (IOException e) {
                                    Logger.logError(e.getMessage());
                                }
                                Logger.logDebug("Path: " + finalpath.toString());
                                if (finalpath != null && Files.exists(finalpath, new LinkOption[0]) && path != null) {
                                    finalpath = null;
                                    String fname = FileManager.getFileName(usablefilename, false);
                                    String ext = FileManager.getExtension(usablefilename);
                                    String newFname = "";
                                    Path temppath = null;
                                    for (int i = 0; i < 999; ++i) {
                                        newFname = fname + String.format("%03d", i + 1) + "." + ext;
                                        Path renamepath = null;
                                        try {
                                            renamepath = FileUtil.resolvePath(path, newFname);
                                        }
                                        catch (Exception e) {
                                            Logger.logError(e.getMessage());
                                        }
                                        if (renamepath == null || Files.exists(renamepath, new LinkOption[0])) continue;
                                        temppath = renamepath;
                                        break;
                                    }
                                    if (newFname.length() > 0 && !newFname.equalsIgnoreCase(usablefilename) && temppath != null) {
                                        usablefilename = newFname;
                                        finalpath = temppath;
                                    } else {
                                        finalpath = null;
                                        errormessage = "File already exists on server and cannot auto rename.";
                                    }
                                }
                                if (finalpath != null) {
                                    try {
                                        fileData.setFile(finalpath);
                                        if (Files.exists(fileData.getFile(), new LinkOption[0])) {
                                            String sd;
                                            if (showThumbnails) {
                                                String t;
                                                FileManager.FileData thumb = FileManager.generateThumbnail(fileData, -1, -1, new Logger(new LogContext(this.getClass().getName(), "(none)")), connectionId);
                                                String string = t = thumb != null ? thumb.getDataURL() : "";
                                                if (t != null && t.length() > 0) {
                                                    job.put("thumbnail", t);
                                                } else {
                                                    FileType fileType = FileType.getFileType(usablefilename, connectionId);
                                                    ConnectionProcessor cp = new ConnectionProcessor(connectionId);
                                                    ImageRef imageRef = new ImageRef(cp.getSapphireConnection());
                                                    imageRef.setImage(fileType.getImageRefId());
                                                    job.put("thumbnail", imageRef.getSrc());
                                                }
                                            }
                                            String string = sd = subdirectory != null && subdirectory.length() > 0 ? subdirectory : "";
                                            if (sd.length() > 0) {
                                                if (sd.contains("\\")) {
                                                    StringUtil.replaceAll(sd, "\\", FileSystems.getDefault().getSeparator());
                                                } else if (sd.contains("/")) {
                                                    StringUtil.replaceAll(sd, "\\", FileSystems.getDefault().getSeparator());
                                                }
                                                if (!sd.endsWith(FileSystems.getDefault().getSeparator())) {
                                                    sd = sd + FileSystems.getDefault().getSeparator();
                                                }
                                            }
                                            job.put("subdirectory", sd);
                                            cont = true;
                                            break block64;
                                        }
                                        errormessage = "Failed to write file.";
                                    }
                                    catch (Exception e) {
                                        errormessage = "Could not write file. Error: " + e.getMessage();
                                    }
                                }
                            } else {
                                errormessage = "Uploaded file too large to be stored.";
                            }
                        }
                        if (cont) {
                            if (job != null) {
                                try {
                                    job.put("filename", usablefilename);
                                    if (usablefilename.equalsIgnoreCase(fileName)) {
                                        job.put("sourcefilename", fileName);
                                    }
                                    job.put("type", type);
                                }
                                catch (Exception exception) {}
                            }
                        } else {
                            if (errormessage.length() == 0) {
                                errormessage = "Could not upload file.";
                            }
                            Logger.logError(errormessage);
                        }
                        break block65;
                    }
                    errormessage = "File type is not valid";
                }
                catch (Exception e) {
                    errormessage = e.getMessage();
                    Logger.logError(errormessage);
                }
            } else {
                errormessage = "No filename provided";
            }
        }
        return errormessage;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void upload(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            Logger.logDebug("Upload Request initialised");
            List fileItems = this.getFileItems(request, servletContext);
            try {
                String errorMsg = "";
                PrintWriter out = response.getWriter();
                try {
                    JSONObject job = new JSONObject();
                    String connectionId = "";
                    try {
                        connectionId = ((RequestContext)request.getAttribute("RequestContext")).getConnectionId();
                    }
                    catch (Exception e3) {
                        Logger.logError("Could not obtain connection id.");
                        Logger.logStackTrace(e3);
                    }
                    if (!FileManager.isValidMagicByte(fileItems, connectionId)) {
                        errorMsg = "File content does not match with the file extension.";
                    }
                    if (FileManager.hasIllegalJSTags(fileItems, connectionId)) {
                        errorMsg = "File content has illegal tag.";
                    }
                    Logger.logDebug("Upload Request Filename = " + FileUpload.getFileItemValue(fileItems, "filename"));
                    HashMap addtionalFields = this.getAddtionalFields(request, fileItems);
                    String csrftoken = (String)addtionalFields.get("csrftoken");
                    if (!(request.getSession().getAttribute("csrftoken") == null || csrftoken != null && csrftoken.equals(request.getSession().getAttribute("csrftoken")))) {
                        errorMsg = (errorMsg.length() > 0 ? errorMsg + " and " : "") + "csrftoken is not correct";
                    }
                    if (errorMsg.length() == 0) {
                        errorMsg = this.tempMultiPart(request, response, connectionId, fileItems, job);
                    }
                    if (errorMsg.length() > 0) {
                        Logger.logError("Upload Request Error: " + errorMsg);
                        out.print(errorMsg);
                        response.setStatus(406);
                    } else {
                        Logger.logDebug("Upload Request Successful");
                        out.print(job.toString());
                    }
                    out.flush();
                }
                finally {
                    Logger.logDebug("Upload Request Finally called. ErrorMsg = " + errorMsg);
                    out.close();
                    try {
                        for (Object obj : fileItems) {
                            FileItem item = (FileItem)obj;
                            item.delete();
                        }
                    }
                    catch (Throwable e) {
                        Logger.logDebug("Failed to clean up in finally. E = " + e.getMessage());
                    }
                }
            }
            catch (IOException e) {
                Logger.logError("Could not get response.", e);
                throw new ServletException(e.getMessage(), (Throwable)e);
            }
        }
        catch (Throwable e) {
            Logger.logError("Could not parse request.", e);
            Logger.logDebug("Upload Request Failed - " + e.getMessage());
            throw new ServletException("Could not parse request.", e);
        }
        finally {
            Logger.logDebug("Upload Request Finished");
        }
    }

    public static enum UploadSource {
        FILEUPLOADER,
        OTHER;

    }
}

