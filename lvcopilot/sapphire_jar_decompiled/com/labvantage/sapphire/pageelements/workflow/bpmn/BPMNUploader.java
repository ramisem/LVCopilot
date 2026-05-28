/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.bpmn;

import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.servlet.command.fileupload.DefaultFileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.util.Logger;
import sapphire.util.StringUtil;

public class BPMNUploader
extends BaseRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        try {
            String error = "";
            String elementid = request.getParameter("elementid");
            try (PrintWriter out = response.getWriter();){
                String contentType = request.getHeader("Content-type");
                if (null != contentType && contentType.startsWith("multipart/")) {
                    FileUpload fu = new FileUpload();
                    List fi = fu.parseRequest(request, 10000000L, 10000000L, "");
                    if (fi.size() > 0) {
                        DefaultFileItem dif = (DefaultFileItem)fi.get(0);
                        if (!dif.isFormField()) {
                            if (dif.getContentType().equalsIgnoreCase("text/plain")) {
                                String output = "<textarea id=\"xmlSource\">" + StringUtil.replaceAll(StringUtil.replaceAll(dif.getString(), ">", "&gt;"), "<", "&lt;") + "</textarea>";
                                out.print(output);
                                response.setContentType("text/html");
                            } else {
                                error = "Invalid file type.";
                            }
                        } else {
                            error = "Invalid Request Type";
                        }
                    } else {
                        error = "No file uploaded";
                    }
                } else {
                    error = "Invalid Request";
                }
                if (error.length() > 0) {
                    error = "<textarea id=\"errorMsg\">" + error + "</textarea>";
                    out.print(error);
                    response.setContentType("text/html");
                }
                out.flush();
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }
}

