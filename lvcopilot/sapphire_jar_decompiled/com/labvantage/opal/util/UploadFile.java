/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.util;

import com.labvantage.sapphire.pageelements.lookup.FileSystem;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class UploadFile
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 80412 $";
    private static String UPLOADNODE = "Upload Custom";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        PrintWriter out = null;
        String uploadedFiles = "";
        String err = "";
        try {
            out = response.getWriter();
        }
        catch (Exception e) {
            throw new ServletException("Failed to initialise operation.", (Throwable)e);
        }
        try {
            String[] excludeTypes;
            String incExcludes;
            File filePath;
            if (this.getConnectionProcessor() == null || this.getConnectionProcessor().getSapphireConnection() == null || this.getConnectionId() == null || this.getConnectionId().length() == 0) {
                err = "Upload operation can only be used with a valid connection.";
                return;
            }
            FileUpload fu = new FileUpload();
            List fileItems = null;
            String maxUploadSize = servletContext.getInitParameter("maxuploadsize");
            long maxsize = 10000000L;
            HashMap<String, String> hmRequest = new HashMap<String, String>();
            String uploadLocation = "";
            String currentUser = "";
            boolean renameFile = false;
            boolean userFolders = false;
            try {
                long size;
                maxsize = size = Long.parseLong(maxUploadSize);
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
            String sapphireHome = Configuration.getInstance().getSapphireHome();
            String string = uploadLocation = hmRequest.containsKey("uploadlocation") ? hmRequest.get("uploadlocation").toString() : "";
            if (uploadLocation == null || uploadLocation.length() == 0) {
                uploadLocation = UPLOADNODE;
            }
            PropertyList filelocationPolicy = null;
            if (new File(uploadLocation).exists() || uploadLocation.indexOf(File.separatorChar) > -1 || uploadLocation.indexOf("/") > -1) {
                try {
                    filelocationPolicy = this.getConfigurationProcessor().getPolicy("FileLocationPolicy", UPLOADNODE);
                }
                catch (Exception e) {
                    this.logger.warn("Unable to obtain file location policy. Please make sure you have an " + UPLOADNODE + " node.");
                    filelocationPolicy = new PropertyList();
                }
                File destFilePath = new File(uploadLocation);
                String destFilePathString = destFilePath.getAbsolutePath().toLowerCase() + File.separator;
                if (filelocationPolicy.getCollection("locations") == null || filelocationPolicy.getCollection("locations").size() <= 0) throw new Exception("No file locations provided for upload therefore upload destination not restricted.");
                if (!FileManager.isValidFileLocation(destFilePathString, filelocationPolicy)) {
                    throw new SapphireException("Upload location not a valid file location.");
                }
                uploadLocation = destFilePath.getAbsolutePath();
            } else {
                try {
                    filelocationPolicy = this.getConfigurationProcessor().getPolicy("FileLocationPolicy", uploadLocation);
                }
                catch (Exception e) {
                    filelocationPolicy = new PropertyList();
                    throw new Exception("Unable to obtain file location policy. Please make sure you have an " + uploadLocation + " node.");
                }
                String loc = FileManager.getFileLocation(filelocationPolicy);
                if (loc.length() <= 0) throw new Exception("Invalid file location policy node provided.");
                uploadLocation = new File(FileSystem.getFileLocation(loc)).getAbsolutePath();
            }
            if (uploadLocation.lastIndexOf(File.separator) == uploadLocation.length() - 1) {
                uploadLocation = uploadLocation.substring(0, uploadLocation.length() - 1);
            }
            if (!(filePath = new File(uploadLocation)).exists()) {
                filePath.mkdirs();
            }
            fu.setRepositoryPath(uploadLocation);
            if (hmRequest.containsKey("sysuserid")) {
                currentUser = (String)hmRequest.get("sysuserid");
            } else {
                String string2 = currentUser = this.getConnectionProcessor() != null && this.getConnectionProcessor().getSapphireConnection() != null ? this.getConnectionProcessor().getSapphireConnection().getSysuserId() : null;
            }
            if (currentUser == null || currentUser.length() == 0) {
                throw new Exception("No user provided.");
            }
            renameFile = "Y".equalsIgnoreCase((String)hmRequest.get("renamefile"));
            userFolders = "Y".equalsIgnoreCase((String)hmRequest.get("userfolders"));
            String extExcludes = filelocationPolicy.getPropertyList("attachmentexcludes") != null ? filelocationPolicy.getPropertyList("attachmentexcludes").getProperty("filetypestoexclude").trim() : "";
            String string3 = incExcludes = filelocationPolicy.getPropertyList("attachmentexcludes") != null ? filelocationPolicy.getPropertyList("attachmentexcludes").getProperty("filetypestoinclude").trim() : "";
            String[] stringArray = extExcludes.length() > 0 ? (extExcludes.contains(",") ? StringUtil.split(extExcludes.toLowerCase(), ",") : StringUtil.split(extExcludes.toLowerCase(), ";")) : (excludeTypes = new String[]{});
            String[] includeTypes = incExcludes.length() > 0 ? (incExcludes.contains(",") ? StringUtil.split(incExcludes.toLowerCase(), ",") : StringUtil.split(incExcludes.toLowerCase(), ";")) : new String[]{};
            boolean allowBlankExtension = filelocationPolicy.getPropertyList("attachmentexcludes") != null ? filelocationPolicy.getPropertyList("attachmentexcludes").getProperty("blankextension").equalsIgnoreCase("Y") : false;
            for (FileItem fi : fileItems) {
                File file;
                byte[] indata;
                String fileName = fi.getName();
                if (fileName == null) continue;
                fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                fileName = fileName.substring(fileName.lastIndexOf("//") + 1);
                String extension = "";
                int ex = fileName.lastIndexOf(46);
                if (ex > -1) {
                    extension = fileName.substring(ex);
                }
                if (!allowBlankExtension && extension.length() == 0) {
                    throw new SapphireException("No file type found. File rejected.");
                }
                if (includeTypes.length > 0 && extension.length() > 0) {
                    boolean found = false;
                    for (int k = 0; k < includeTypes.length; ++k) {
                        if (!extension.equalsIgnoreCase(includeTypes[k])) continue;
                        found = true;
                    }
                    if (!found) {
                        throw new SapphireException("File type is not in allowable types. File rejected.");
                    }
                }
                if (excludeTypes.length > 0 && extension.length() > 0) {
                    for (int k = 0; k < excludeTypes.length; ++k) {
                        if (!extension.equalsIgnoreCase(excludeTypes[k])) continue;
                        throw new SapphireException("Invalid file type found. File rejected.");
                    }
                }
                if ((indata = fi.get()).length <= 0) continue;
                String uploadFilePath = uploadLocation;
                if (renameFile) {
                    fileName = currentUser + "_" + System.currentTimeMillis() + "_" + fileName;
                }
                if (userFolders) {
                    uploadFilePath = uploadFilePath + File.separator + currentUser;
                }
                if (!(file = new File(uploadFilePath)).exists()) {
                    file.mkdirs();
                }
                uploadFilePath = uploadFilePath + File.separator + fileName;
                uploadedFiles = uploadedFiles + ";" + uploadFilePath;
                FileOutputStream fileout = new FileOutputStream(uploadFilePath);
                fileout.write(indata);
                fileout.close();
            }
            uploadedFiles = uploadedFiles.substring(1);
            return;
        }
        catch (Exception ex) {
            Logger.logError(ex.getMessage(), ex);
            err = ex.getMessage();
            return;
        }
        finally {
            if (err.equals("")) {
                out.print("<script>parent.uploadFileResponse('" + StringUtil.replaceAll(uploadedFiles, "\\", "\\\\") + "')</script>");
            } else {
                out.print("<script>parent.uploadFileResponseError(\"" + StringUtil.replaceAll(err, "\\", "\\\\") + "\")</script>");
            }
            out.flush();
        }
    }
}

