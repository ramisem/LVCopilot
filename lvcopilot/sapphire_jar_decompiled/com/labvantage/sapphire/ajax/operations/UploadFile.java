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

import com.labvantage.sapphire.util.file.ExcelFileDetails;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import com.labvantage.sapphire.util.file.PPTFileDetails;
import com.labvantage.sapphire.util.file.PdfFileDetails;
import com.labvantage.sapphire.util.file.TextFileDetails;
import com.labvantage.sapphire.util.file.WordFileDetails;
import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class UploadFile
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block30: {
            ajaxResponse = new AjaxResponse(request, response);
            try {
                mode = UploadMode.UPLOAD;
                try {
                    mode = UploadMode.valueOf(ajaxResponse.getRequestParameter("mode", UploadMode.UPLOAD.toString()).toUpperCase());
                }
                catch (Exception var6_6) {
                    // empty catch block
                }
                file = ajaxResponse.getRequestParameter("file");
                tempid = ajaxResponse.getRequestParameter("tempid");
                filename = ajaxResponse.getRequestParameter("filename");
                fileData = null;
                if (tempid.length() > 0 && filename.length() == 0) {
                    tempFile = FileManager.TempFile.getTempFile(tempid, false, this.getQueryProcessor(), this.getConnectionId());
                    filename = tempFile.getFileName();
                    fileData = tempFile != null ? tempFile.getData() : null;
                } else {
                    if (filename.startsWith("[filelocation]/")) {
                        filelocationpolicy = ajaxResponse.getRequestParameter("filelocationpolicy", "Upload Custom");
                        loc = FileManager.getFileLocation(filelocationpolicy, filelocationid = ajaxResponse.getRequestParameter("filelocationid", ""), this.getConnectionId());
                        if (loc.length() > 0 && !loc.endsWith("/")) {
                            loc = loc + "/";
                        }
                        filename = StringUtil.replaceAll(filename, "[filelocation]/", loc);
                    }
                    fileData = new FileManager.FileData(new File(filename), FileType.getFileType(filename, this.getConnectionId()).getMime(), true);
                }
                if (mode == UploadMode.PREVIEWCLIENT) {
                    ajaxResponse.addCallbackArgument("preview", "");
                    ajaxResponse.addCallbackArgument("previewtext", this.getTranslationProcessor().translate("No preview available"));
                    break block30;
                }
                if (mode == UploadMode.PREVIEWSERVER) {
                    preview = new StringBuilder();
                    previewText = new StringBuilder();
                    type = FileTypeGroup.getFileTypeGroupByFileName(filename);
                    if (type != null && type.isSupported()) {
                        if (fileData != null) {
                            filedata = fileData.getData();
                            if (filedata != null && filedata.length > 0) {
                                previewText.append(this.getTranslationProcessor().translate("Preview of "));
                                try {
                                    bis = new ByteArrayInputStream(filedata);
                                    try {
                                        switch (1.$SwitchMap$com$labvantage$sapphire$util$file$FileTypeGroup[type.ordinal()]) {
                                            case 1: {
                                                textFileDetails = new TextFileDetails();
                                                textFileDetails.setFromLine(1);
                                                textFileDetails.setFromLine(20);
                                                preview.append((CharSequence)FileManager.getTxtHTMLFromBis(bis, textFileDetails, this.logger));
                                                previewText.append(this.getTranslationProcessor().translate("Text File ")).append(" (lines 0 - 20 of ").append(textFileDetails.getTotalLinesAvailable()).append(")");
                                                ** break;
lbl52:
                                                // 1 sources

                                                break;
                                            }
                                            case 2: {
                                                maxsize = new Dimension(800, 600);
                                                size = FileManager.getImageFromBIS(preview, bis, null, maxsize, this.logger, this.getConnectionId());
                                                previewText.append(this.getTranslationProcessor().translate("Image ")).append(" (size ").append(maxsize.getWidth()).append(" x ").append(maxsize.getHeight()).append(" from ").append(size.getWidth()).append(" x ").append(size.getHeight()).append(")");
                                                ** break;
lbl59:
                                                // 1 sources

                                                break;
                                            }
                                            case 3: {
                                                wordDetails = new WordFileDetails();
                                                wordDetails.setFromPage(1);
                                                wordDetails.setToPage(1);
                                                preview.append((CharSequence)FileManager.getWordHtmlFromBis(bis, wordDetails, this.logger));
                                                previewText.append(this.getTranslationProcessor().translate("Word Document ")).append(" (page 1 of ").append(wordDetails.getTotalPagesAvailable()).append(")");
                                                ** break;
lbl69:
                                                // 1 sources

                                                break;
                                            }
                                            case 4: {
                                                pptFileDetails = new PPTFileDetails();
                                                pptFileDetails.setFromSlide(1);
                                                pptFileDetails.setToSlide(1);
                                                pptFileDetails.setScaleFactor(1);
                                                preview.append((CharSequence)FileManager.getPptHtmlFromBis(bis, pptFileDetails, this.logger));
                                                previewText.append(this.getTranslationProcessor().translate("PowerPoint Slides ")).append(" (page 1 of ").append(pptFileDetails.getTotalSlidesAvailable()).append(")");
                                                ** break;
lbl80:
                                                // 1 sources

                                                break;
                                            }
                                            case 5: {
                                                excelFileDetails = new ExcelFileDetails();
                                                excelFileDetails.setShowGridLines(true);
                                                preview.append((CharSequence)FileManager.getExcelHtmlFromBis(bis, excelFileDetails, this.logger));
                                                previewText.append(this.getTranslationProcessor().translate("Excel Workbook ")).append(" (sheet 1 of ").append(excelFileDetails.getAllSheets().length()).append(")");
                                                ** break;
lbl89:
                                                // 1 sources

                                                break;
                                            }
                                            case 6: {
                                                pdfFileDetails = new PdfFileDetails();
                                                pdfFileDetails.setFromPage(1);
                                                pdfFileDetails.setToPage(1);
                                                preview.append((CharSequence)FileManager.getPdfHtmlFromBis(bis, pdfFileDetails, this.logger));
                                                previewText.append(this.getTranslationProcessor().translate("PDF Document ")).append(" (page 1 of ").append(pdfFileDetails.getTotalPagesAvailable()).append(")");
                                                break;
                                            }
                                            ** default:
lbl100:
                                            // 1 sources

                                            break;
                                        }
                                    }
                                    finally {
                                        bis.close();
                                        filedata = null;
                                    }
                                }
                                catch (Exception e) {
                                    this.logger.error("Could not preview file.");
                                }
                            } else {
                                this.logger.error("Could not read file for preview.");
                            }
                        } else {
                            this.logger.error("Could not load file for preview.");
                        }
                    } else {
                        this.logger.error("Invalid file type to preview.");
                    }
                    ajaxResponse.addCallbackArgument("preview", preview.toString());
                    ajaxResponse.addCallbackArgument("previewtext", preview.length() > 0 ? previewText : this.getTranslationProcessor().translate("No preview available"));
                    break block30;
                }
                ajaxResponse.addCallbackArgument("filename", "");
            }
            finally {
                ajaxResponse.print();
            }
        }
    }

    public static enum UploadMode {
        UPLOAD,
        PREVIEWCLIENT,
        PREVIEWSERVER;

    }
}

