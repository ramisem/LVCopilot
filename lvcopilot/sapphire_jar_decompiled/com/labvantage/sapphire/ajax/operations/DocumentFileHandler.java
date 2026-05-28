/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.codec.binary.Base64
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.PdfFileDetails;
import com.labvantage.sapphire.util.file.WordFileDetails;
import java.io.ByteArrayInputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class DocumentFileHandler
extends BaseAjaxRequest {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block12: {
            AjaxResponse ajaxResponse = new AjaxResponse(request, response, "DocumentFileHandler");
            try {
                String filedata = ajaxResponse.getRequestParameter("filedata");
                if (filedata.length() <= 0) break block12;
                try {
                    byte[] bytes = Base64.decodeBase64((String)filedata.substring(filedata.indexOf(";") + 8));
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);){
                        StringBuilder out = new StringBuilder();
                        if (filedata.startsWith("data:application/vnd.openxmlformats-officedocument.wordprocessingml.document;") || filedata.startsWith("data:application/msword;")) {
                            WordFileDetails wordDetails = new WordFileDetails();
                            wordDetails.setFromPage(-1);
                            wordDetails.setToPage(-1);
                            wordDetails.setExportPageSetup(false);
                            wordDetails.setExportPageMargins(false);
                            out.append((CharSequence)FileManager.getWordHtmlFromBis(bis, wordDetails, this.logger));
                        } else if (filedata.startsWith("data:application/pdf;")) {
                            PdfFileDetails pdfDetails = new PdfFileDetails();
                            pdfDetails.setFromPage(-1);
                            pdfDetails.setToPage(-1);
                            out.append((CharSequence)FileManager.getPdfHtmlFromBis(bis, pdfDetails, this.logger));
                        } else {
                            throw new Exception("Invalid file type.");
                        }
                        ajaxResponse.addCallbackArgument("html", out.toString());
                    }
                }
                catch (Exception e) {
                    this.logger.error("Failed to load base64.", e);
                }
            }
            finally {
                ajaxResponse.print();
            }
        }
    }
}

