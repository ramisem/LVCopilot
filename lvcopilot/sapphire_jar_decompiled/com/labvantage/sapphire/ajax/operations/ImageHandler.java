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
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class ImageHandler
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    public static final String ATTACHMENTUSE = "HTMLEditor";
    private static final String OPERATION = "rc?command=operation&operationclass=" + ImageHandler.class.getName();

    public static String getOperationURL(String tempid) {
        return ImageHandler.getOperationURL(null, null, null, null, null, tempid);
    }

    public static String getOperationURL(String source, String tempid) {
        return ImageHandler.getOperationURL(null, null, null, null, source, tempid);
    }

    public static String getOperationURL(String sdcid, String keyid1, String keyid2, String keyid3, String source, String tempid) {
        StringBuffer url = new StringBuffer();
        url.append(OPERATION);
        if (sdcid != null && sdcid.length() > 0 && !sdcid.equalsIgnoreCase("(null)")) {
            url.append("&sdcid=").append(sdcid);
        }
        if (keyid1 != null && keyid1.length() > 0 && !keyid1.equalsIgnoreCase("(null)")) {
            url.append("&keyid1=").append(keyid1);
            if (keyid2 != null && keyid2.length() > 0 && !keyid2.equalsIgnoreCase("(null)")) {
                url.append("&keyid2=").append(keyid2);
                if (keyid3 != null && keyid3.length() > 0 && !keyid3.equalsIgnoreCase("(null)")) {
                    url.append("&keyid3=").append(keyid3);
                }
            }
        }
        if (source != null && source.length() > 0) {
            url.append("&source=").append(source);
        }
        if (tempid.length() > 0) {
            url.append("&id=").append(tempid);
        }
        return url.toString();
    }

    public static FileManager.TempFile getImageData(String id, String sdcid, String keyid1, String keyid2, String keyid3, QueryProcessor queryProcessor, String connectionId) {
        return FileManager.TempFile.getTempFile(sdcid, keyid1, keyid2, keyid3, id, false, queryProcessor, connectionId);
    }

    public static String storeAsAttachment(String image, String sdcid, String keyid1, String keyid2, String keyid3, String attNum, String attClass, String filename, String description, QueryProcessor queryProcessor, AttachmentProcessor attachmentProcessor, ConfigurationProcessor configProcessor) {
        return FileManager.storeImageAsAttachment(image, sdcid, keyid1, keyid2, keyid3, attNum, attClass, ATTACHMENTUSE, filename, description, true, "Attachment Custom", "item1", null, queryProcessor, attachmentProcessor, configProcessor);
    }

    public static String storeAsAttachment(String image, String sdcid, String keyid1, String keyid2, String keyid3, String attNum, String attClass, String attUse, String filename, String description, boolean attachmentsByReference, String locationpolicynode, String locationpolicyitem, QueryProcessor queryProcessor, AttachmentProcessor attachmentProcessor, ConfigurationProcessor configProcessor) {
        return FileManager.storeImageAsAttachment(image, sdcid, keyid1, keyid2, keyid3, attNum, attClass, ATTACHMENTUSE, filename, description, attachmentsByReference, "Attachment Custom", "item1", null, queryProcessor, attachmentProcessor, configProcessor);
    }

    public static void cleanUp(String id, String sdcid, String keyid1, String keyid2, String keyid3, ActionProcessor actionProcessor) {
        PropertyList sditemp = new PropertyList();
        sditemp.setProperty("keyid1", keyid1);
        if (keyid2.length() > 0) {
            sditemp.setProperty("keyid2", keyid2);
        }
        if (keyid3.length() > 0) {
            sditemp.setProperty("keyid3", keyid3);
        }
        sditemp.setProperty("sdcid", sdcid);
        sditemp.setProperty("mode", "remove");
        sditemp.setProperty("tempid", id);
        try {
            actionProcessor.processActionClass("com.labvantage.sapphire.actions.sdi.SDITemp", sditemp);
        }
        catch (Exception e) {
            Trace.logError("Failed to remove temp image.", e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String keyid1 = "";
        String keyid2 = "";
        String keyid3 = "";
        String sdcid = "";
        if (request.getParameter("id") != null && request.getParameter("id").length() > 0) {
            keyid1 = request.getParameter("keyid1") != null ? request.getParameter("keyid1") : "";
            keyid2 = request.getParameter("keyid2") != null ? request.getParameter("keyid2") : "(null)";
            keyid3 = request.getParameter("keyid3") != null ? request.getParameter("keyid3") : "(null)";
            sdcid = request.getParameter("sdcid") != null ? request.getParameter("sdcid") : "";
            String id = request.getParameter("id").toString();
            FileManager.TempFile outP = ImageHandler.getImageData(id, sdcid, keyid1, keyid2, keyid3, this.getQueryProcessor(), this.getConnectionId());
            if (outP != null) {
                byte[] data = outP.getData().getData();
                if (outP.getData().getData().length > 0) {
                    try {
                        response.setContentType(outP.getMimeType().length() > 0 ? outP.getMimeType() : FileType.getFileTypeByName("PNG", this.getConnectionId()).getMime());
                        response.setHeader("Content-disposition", "attachment; filename=" + HttpUtil.encodeURIComponent(outP.getFileName()));
                        ServletOutputStream outputStream = response.getOutputStream();
                        try {
                            outputStream.write(data);
                        }
                        finally {
                            outputStream.flush();
                            outputStream.close();
                        }
                    }
                    catch (Exception e) {
                        Trace.logError("Could not convert file data from base64. Error = " + e.getMessage());
                    }
                }
            }
        } else {
            PrintWriter out = null;
            ArrayList fileInfo = new ArrayList();
            try {
                String source;
                String id;
                out = response.getWriter();
                FileUpload fu = new FileUpload();
                List fileItems = null;
                String maxUploadSize = servletContext.getInitParameter("maxuploadsize");
                int maxsize = 10000000;
                HashMap<String, String> hmRequest = new HashMap<String, String>();
                String uploadLocation = "";
                String currentUser = "";
                boolean renameFile = false;
                boolean userFolders = false;
                try {
                    int size;
                    maxsize = size = Integer.parseInt(maxUploadSize);
                }
                catch (NumberFormatException size) {
                    // empty catch block
                }
                fu.setSizeMax(maxsize);
                fu.setSizeThreshold(maxsize);
                fileItems = fu.parseRequest(request);
                for (FileItem fi : fileItems) {
                    if (!fi.isFormField()) continue;
                    String fieldName = fi.getFieldName();
                    String fieldValue = new String(fi.get(), "UTF-8");
                    hmRequest.put(fieldName, fieldValue);
                }
                String string = id = hmRequest.containsKey("id") ? hmRequest.get("id").toString() : "";
                String string2 = hmRequest.containsKey("source") ? hmRequest.get("source").toString() : (source = request.getParameter("source") != null ? request.getParameter("source") : "Upload");
                String string3 = hmRequest.containsKey("sdcid") ? hmRequest.get("sdcid").toString() : (sdcid = request.getParameter("sdcid") != null ? request.getParameter("sdcid") : "");
                String string4 = hmRequest.containsKey("keyid1") ? hmRequest.get("keyid1").toString() : (keyid1 = request.getParameter("keyid1") != null ? request.getParameter("keyid1") : "");
                String string5 = hmRequest.containsKey("keyid2") ? hmRequest.get("keyid2").toString() : (keyid2 = request.getParameter("keyid2") != null ? request.getParameter("keyid2") : "(null)");
                keyid3 = hmRequest.containsKey("keyid3") ? hmRequest.get("keyid3").toString() : (request.getParameter("keyid3") != null ? request.getParameter("keyid3") : "(null)");
                Iterator i = fileItems.iterator();
                boolean j = false;
                while (i.hasNext()) {
                    FileItem fi = (FileItem)i.next();
                    String fileName = fi.getName();
                    if (fileName == null) continue;
                    fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                    fileName = fileName.substring(fileName.lastIndexOf("//") + 1);
                    FileType type = FileType.getFileType(fileName, this.getConnectionId());
                    byte[] indata = fi.get();
                    if (indata.length <= 0) continue;
                    FileManager.TempFile tempFile = new FileManager.TempFile(new FileManager.FileData(indata, type.getMime()), fileName, fileName, FileManager.TempSource.RICHTEXT, Attachment.AttachmentType.FILE.getFlag(), false, this.getConnectionId());
                    String tempid = tempFile.setTempFile(sdcid, keyid1, keyid2, keyid3, maxsize, "", this.getConnectionId());
                    String outR = "";
                    if (tempid.length() > 0) {
                        outR = "{\"location\":\"" + ImageHandler.getOperationURL(sdcid, keyid1, keyid2, keyid3, "", tempid) + "\"}";
                    } else {
                        StringBuffer s = new StringBuffer();
                        s.append("{\"location\":\"");
                        s.append("data:").append(tempFile.getMimeType()).append(";base64,");
                        s.append(tempFile.getData().getBase64());
                        s.append("\"}");
                        outR = s.toString();
                    }
                    out.write(outR);
                    break;
                }
            }
            catch (Exception ex) {
                Logger.logError(ex.getMessage(), ex);
            }
            finally {
                if (!fileInfo.isEmpty()) {
                    for (Map fileinfo : fileInfo) {
                        File file = new File((String)fileinfo.get("filepath") + File.separator + (String)fileinfo.get("filename"));
                        file.delete();
                    }
                }
                out.flush();
            }
        }
    }
}

