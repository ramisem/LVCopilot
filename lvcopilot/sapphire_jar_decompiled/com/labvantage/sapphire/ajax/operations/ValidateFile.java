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

import com.labvantage.sapphire.pageelements.attachment.AttachmentType_File;
import com.labvantage.sapphire.services.Attachment;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ValidateFile
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 86556 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block12: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response);
            try {
                String filename = ajaxResponse.getRequestParameter("filename");
                if (filename != null && filename.length() > 0) {
                    boolean attachmentUpload = ajaxResponse.getRequestParameter("attachmentupload", "N").equalsIgnoreCase("Y");
                    if (attachmentUpload) {
                        int attachmentNum;
                        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
                        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
                        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
                        String path = filename.indexOf("/") > -1 ? filename.substring(0, filename.lastIndexOf("/") + 1) : filename.substring(0, filename.lastIndexOf("\\") + 1);
                        filename = filename.indexOf("/") > -1 ? filename.substring(filename.lastIndexOf("/") + 1) : filename.substring(filename.lastIndexOf("\\") + 1);
                        filename = AttachmentType_File.renameFileFromPolicy(filename, this.getConnectionId(), keyid2.length() > 0, keyid3.length() > 0);
                        try {
                            attachmentNum = Integer.parseInt(ajaxResponse.getRequestParameter("attachmentnum", "0"));
                        }
                        catch (NumberFormatException e) {
                            attachmentNum = 0;
                        }
                        filename = path + Attachment.evaluateFileNameExpressions(ajaxResponse.getRequestParameter("sdcid", ""), keyid1, keyid2, keyid3, attachmentNum, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), filename, null);
                    }
                    try {
                        File file = new File(filename);
                        JSONObject job = new JSONObject();
                        job.put("filename", filename);
                        job.put("exists", file.exists());
                        if (file.exists()) {
                            job.put("size", file.length());
                            job.put("moddt", new SimpleDateFormat().format(new Date(file.lastModified())));
                            job.put("isFile", file.isFile());
                            job.put("canRead", file.canRead());
                            job.put("canWrite", file.canWrite());
                            job.put("isHidden", file.isHidden());
                        } else {
                            job.put("size", 0);
                            job.put("moddt", "");
                            job.put("isFile", false);
                            job.put("canRead", false);
                            job.put("canWrite", false);
                            job.put("isHidden", false);
                        }
                        ajaxResponse.addCallbackArgument("file", job);
                        String elementid = ajaxResponse.getRequestParameter("elementid");
                        if (elementid != null && elementid.length() > 0) {
                            ajaxResponse.addCallbackArgument("elementid", elementid);
                        }
                        break block12;
                    }
                    catch (Exception e) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain file information."));
                    }
                    break block12;
                }
                ajaxResponse.setError(this.getTranslationProcessor().translate("No filename string provided."));
            }
            finally {
                ajaxResponse.print();
            }
        }
    }
}

