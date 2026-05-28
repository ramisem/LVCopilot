/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.modules.datafile.DataFileUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.util.DataSet;
import sapphire.util.Logger;

public class UploadExcel
extends BaseRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block22: {
            ajaxResponse = new AjaxResponse(request, response, "ImportTransferMapHandler");
            PrintWriter out = null;
            String uploadedFiles = "";
            String err = "";
            String methodid = request.getParameter("transfermethodid");
            String methodversionid = request.getParameter("transfermethodversionid");
            ArrayList fileInfo = new ArrayList();
            try {
                File filePath;
                out = response.getWriter();
                FileUpload fu = new FileUpload();
                Object var12_14 = null;
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
                List list = fu.parseRequest(request);
                for (FileItem fi : list) {
                    if (!fi.isFormField()) continue;
                    String fieldName = fi.getFieldName();
                    String fieldValue = new String(fi.get(), "UTF-8");
                    hmRequest.put(fieldName, fieldValue);
                }
                uploadLocation = (String)hmRequest.get("uploadlocation");
                if (uploadLocation == null || uploadLocation.length() == 0) {
                    uploadLocation = Configuration.getInstance().getSapphireHome() + "/temp/";
                }
                if (uploadLocation.lastIndexOf(File.separator) == uploadLocation.length() - 1) {
                    uploadLocation = uploadLocation.substring(0, uploadLocation.length() - 1);
                }
                if (!(filePath = new File(uploadLocation)).exists()) {
                    filePath.mkdirs();
                }
                fu.setRepositoryPath(uploadLocation);
                currentUser = hmRequest.containsKey("sysuserid") ? (String)hmRequest.get("sysuserid") : this.getConnectionProcessor().getSapphireConnection().getSysuserId();
                renameFile = "Y".equalsIgnoreCase((String)hmRequest.get("renamefile"));
                userFolders = "Y".equalsIgnoreCase((String)hmRequest.get("userfolders"));
                Iterator i = list.iterator();
                boolean j = false;
                while (i.hasNext()) {
                    File file;
                    FileItem fi = (FileItem)i.next();
                    String fileName = fi.getName();
                    if (fileName == null) continue;
                    fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                    fileName = fileName.substring(fileName.lastIndexOf("//") + 1);
                    byte[] indata = fi.get();
                    if (indata.length <= 0) continue;
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
                    HashMap<String, String> fileinfo = new HashMap<String, String>();
                    fileinfo.put("filepath", uploadFilePath);
                    fileinfo.put("filename", fileName);
                    fileInfo.add(fileinfo);
                    uploadFilePath = uploadFilePath + File.separator + fileName;
                    uploadedFiles = uploadedFiles + ";" + uploadFilePath;
                    FileOutputStream fileout = new FileOutputStream(uploadFilePath);
                    fileout.write(indata);
                    fileout.close();
                }
                uploadedFiles = uploadedFiles.substring(1);
                StringBuffer transferMap = new StringBuffer();
                String transfermap = "";
                StringBuffer colorMap = new StringBuffer();
                String colormap = "";
                if (fileInfo.isEmpty()) break block22;
                DataSet dataSetRaw = DataFileUtil.readExcelFile(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), (String)((Map)fileInfo.get(0)).get("filepath"), (String)((Map)fileInfo.get(0)).get("filename"), "Source Target Mapping");
                DataSet dataSet = DataFileUtil.convertToGrid(dataSetRaw);
                dataSet.setColidCaseSensitive(false);
                if (dataSet != null && !dataSet.isEmpty()) {
                    for (int k = 0; k < dataSet.size(); ++k) {
                        transferMap.append("|").append(dataSet.getValue(k, "Target Array Index")).append(",").append(dataSet.getValue(k, "Target Row Index")).append(",").append(dataSet.getValue(k, "Target Column Index")).append("-").append(dataSet.getValue(k, "Source Array Index")).append(",").append(dataSet.getValue(k, "Source Row Index")).append(",").append(dataSet.getValue(k, "Source Column Index"));
                    }
                    if (transferMap.length() > 0) {
                        transfermap = transferMap.substring(1);
                    }
                }
                DataSet colorDSraw = DataFileUtil.readExcelFile(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), (String)((Map)fileInfo.get(0)).get("filepath"), (String)((Map)fileInfo.get(0)).get("filename"), "Color Map");
                DataSet colorDS = DataFileUtil.convertToGrid(colorDSraw);
                colorDS.setColidCaseSensitive(false);
                if (colorDS != null && !colorDS.isEmpty()) {
                    for (int k = 0; k < colorDS.size(); ++k) {
                        colorMap.append("|").append(colorDS.getValue(k, "Source Array Index")).append(";").append(colorDS.getValue(k, "Source Row Index")).append(";").append(colorDS.getValue(k, "Source Column Index")).append("-").append(colorDS.getValue(k, "Group"));
                    }
                    if (colorMap.length() > 0) {
                        colormap = colorMap.substring(1);
                    }
                }
                if (transfermap.length() <= 0 && colormap.length() <= 0) break block22;
                ajaxResponse.setCallback("uploadcallback");
                ajaxResponse.addCallbackArgument("transfermap", transfermap);
                ajaxResponse.addCallbackArgument("colormap", colormap);
            }
            catch (Exception ex) {
                Logger.logError(ex.getMessage(), ex);
                err = ex.getMessage();
                ajaxResponse.setError(err);
            }
            finally {
                if (fileInfo.isEmpty()) break block22;
                for (Map map : fileInfo) {
                    File file = new File((String)map.get("filepath") + File.separator + (String)map.get("filename"));
                    file.delete();
                }
            }
        }
        ajaxResponse.print();
    }
}

