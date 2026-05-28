/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.datafile;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.datafile.DataFileUtil;
import com.labvantage.sapphire.util.file.FileManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class LoadDataFile
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse;
        block23: {
            ajaxResponse = new AjaxResponse(request, response, "dataFileDefMaint.haveContent");
            String tempid = ajaxResponse.getRequestParameter("tempid");
            String fullfilename = ajaxResponse.getRequestParameter("filename");
            String filetype = ajaxResponse.getRequestParameter("filetype");
            String delimiter = ajaxResponse.getRequestParameter("delimiter");
            String worksheet = ajaxResponse.getRequestParameter("worksheet");
            String encoding = ajaxResponse.getRequestParameter("encoding", "UTF-8");
            try {
                DataSet exampleFileContent = null;
                if (fullfilename.length() > 0 || tempid.length() > 0) {
                    if (tempid.length() > 0) {
                        try {
                            try {
                                DataFileUtil.checkFile(FileManager.getUploadDFDMaxFileSize(this.getConnectionId()), tempid, this.getQueryProcessor(), this.getConnectionId());
                            }
                            catch (SapphireException e) {
                                ajaxResponse.setError(e.getMessage());
                            }
                            if (filetype.equals("excel")) {
                                exampleFileContent = DataFileUtil.readExcelFile(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), tempid, worksheet);
                            }
                            exampleFileContent = DataFileUtil.readTxtFile(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), tempid, delimiter, encoding);
                        }
                        finally {
                            FileManager.TempFile.removeTempFile(tempid, this.getActionProcessor(), this.getQueryProcessor(), this.getConnectionId());
                        }
                    } else {
                        String path = fullfilename.substring(0, fullfilename.lastIndexOf(47) + 1);
                        String fileName = fullfilename.substring(fullfilename.lastIndexOf("/") + 1);
                        try {
                            DataFileUtil.checkFile(FileManager.getUploadDFDMaxFileSize(this.getConnectionId()), fullfilename);
                        }
                        catch (SapphireException e) {
                            ajaxResponse.setError(e.getMessage());
                        }
                        exampleFileContent = filetype.equals("excel") ? DataFileUtil.readExcelFile(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), path, fileName, worksheet) : DataFileUtil.readTxtFile(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), path, fileName, delimiter, encoding);
                    }
                    if (exampleFileContent == null) {
                        ajaxResponse.setError("Example file could not be loaded..");
                    } else {
                        if (exampleFileContent.getRowCount() > 200) {
                            this.logger.warn("Example file has more than 200 rows. Truncating to 200.");
                            for (int i = exampleFileContent.getRowCount(); i > 200; --i) {
                                exampleFileContent.deleteRow(i - 1);
                            }
                        }
                        if (exampleFileContent.getColumnCount() > 100) {
                            this.logger.warn("Example file has more than 100 columns. Truncating to 100.");
                            DataSet copy = new DataSet();
                            copy.setM18NUtil(copy.getM18n());
                            for (int c = 0; c < 100; ++c) {
                                copy.addColumn(exampleFileContent.getColumnId(c), exampleFileContent.getColumnType(exampleFileContent.getColumnId(c)));
                            }
                            for (int r = 0; r < exampleFileContent.getRowCount(); ++r) {
                                copy.addRow();
                                for (int c = 0; c < 100; ++c) {
                                    copy.setValue(r, exampleFileContent.getColumnId(c), exampleFileContent.getValue(r, exampleFileContent.getColumnId(c)));
                                }
                            }
                            exampleFileContent = copy;
                        }
                        ajaxResponse.addCallbackArgument("filecontent", exampleFileContent.toXML());
                    }
                    break block23;
                }
                if (fullfilename.length() == 0) {
                    ajaxResponse.setError("filename not specified");
                } else {
                    ajaxResponse.setError("file not provided");
                }
            }
            catch (Exception e) {
                Trace.logError("Failed to import file", e);
                ajaxResponse.setError("Failed to process request. Reason: " + e.getMessage(), e);
            }
        }
        ajaxResponse.print();
    }
}

