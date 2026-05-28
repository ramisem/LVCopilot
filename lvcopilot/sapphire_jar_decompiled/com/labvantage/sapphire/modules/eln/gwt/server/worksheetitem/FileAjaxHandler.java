/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.File;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.servlet.command.fileupload.FileItem;
import com.labvantage.sapphire.servlet.command.fileupload.FileUpload;
import com.labvantage.sapphire.util.file.BaseFileDetails;
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
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.attachment.Attachment;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.Logger;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;

public class FileAjaxHandler
extends BaseAjaxRequest {
    @Override
    public boolean acceptContentType(String contentType) {
        if (contentType.startsWith("multipart/form-data")) {
            return true;
        }
        return super.acceptContentType(contentType);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block62: {
            if (request.getContentType().startsWith("multipart/form-data")) {
                FileUpload fu = new FileUpload();
                try (PrintWriter out = response.getWriter();){
                    String error;
                    JSONObject job;
                    block61: {
                        FileItem fifile;
                        String filename;
                        job = new JSONObject();
                        error = "";
                        servletContext.setAttribute("attachmentpolicynode", (Object)File.attachmentPolicyNode);
                        List<FileItem> fileItems = fu.getFileItems(request, servletContext);
                        if (!FileManager.isValidMagicByte(fileItems, this.getConnectionId())) {
                            error = "File content does not match with the file extension.";
                        }
                        if ((filename = FileUpload.getFileName(fifile = FileUpload.getFileItem(fileItems, "file"))) != null && filename.length() > 0) {
                            String filetype = fifile.getContentType();
                            String elementid = request.getHeader("elementid") != null ? request.getHeader("elementid") : "";
                            String worksheetitemid = request.getHeader("worksheetitemid") != null ? request.getHeader("worksheetitemid") : "";
                            String worksheetitemversionid = request.getHeader("worksheetitemversionid") != null ? request.getHeader("worksheetitemversionid") : "";
                            String attNum = request.getHeader("attachment") != null ? request.getHeader("attachment") : "";
                            String worksheetid = request.getHeader("worksheetid") != null ? request.getHeader("worksheetid") : "";
                            String worksheetversionid = request.getHeader("worksheetversionid") != null ? request.getHeader("worksheetversionid") : "";
                            PropertyList config = new PropertyList();
                            try {
                                if (request.getHeader("config") != null) {
                                    config.setJSONString(request.getHeader("config"));
                                }
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                            FileTypeGroup type2 = null;
                            if (filetype.length() > 0 && FileTypeGroup.isValidTypeGroup(filetype, this.getConnectionId())) {
                                type2 = FileTypeGroup.getFileTypeGroupByType(filetype, this.getConnectionId());
                            } else if (FileTypeGroup.isValidFilename(filename)) {
                                type2 = FileTypeGroup.getFileTypeGroupByFileName(filename);
                            }
                            if (type2 != null && type2 != FileTypeGroup.UNKNOWN) {
                                long maxsize;
                                FileManager.FileData filedata = new FileManager.FileData(fifile.getInputStream(), filetype, true);
                                long filesize = 0L;
                                try {
                                    filesize = fifile.getSize();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                boolean sizeok = true;
                                if (filesize > 0L && filesize > (maxsize = FileManager.getUploadMaxFileSize("Sapphire Custom", this.getConnectionId()))) {
                                    error = "File too large to be uploaded.";
                                }
                                if (sizeok) {
                                    try {
                                        PropertyList fileout = this.storeAsAttachment(filedata, type2, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, filename, attNum, config, Worksheet.getPolicyNode(this.getQueryProcessor(), worksheetid, worksheetversionid));
                                        attNum = fileout.getProperty("attachment", "");
                                        if (attNum.length() > 0) {
                                            job.putOpt("elementid", elementid);
                                            job.putOpt("filedata", fileout.toJSONString());
                                            break block61;
                                        }
                                        error = "Could not upload attachment.";
                                    }
                                    catch (Exception e) {
                                        error = "Failed to upload attachment. " + e.getMessage();
                                    }
                                }
                            } else {
                                error = "Invalid file type provided.";
                            }
                        } else {
                            error = "Invalid filename";
                        }
                    }
                    if (error.length() > 0) {
                        job.putOpt("error", error);
                    }
                    out.print(job.toString());
                    out.flush();
                    break block62;
                }
                catch (Exception e) {
                    throw new ServletException(e.getMessage());
                }
            }
            AjaxResponse ajaxresponse = new AjaxResponse(request, response);
            try {
                String elementid = ajaxresponse.getRequestParameter("elementid");
                Mode mode = Mode.RENDERUPLOAD;
                try {
                    mode = Mode.valueOf(ajaxresponse.getRequestParameter("mode", Mode.RENDERUPLOAD.toString()).toUpperCase());
                }
                catch (Exception error) {
                    // empty catch block
                }
                if (mode == Mode.RENDERUPLOAD) {
                    ajaxresponse.addCallbackArgument("elementid", elementid);
                    ajaxresponse.addCallbackArgument("html", FileAjaxHandler.getUploadArea(elementid));
                    break block62;
                }
                if (mode == Mode.REFRESH) {
                    String fileproperties = ajaxresponse.getRequestParameter("fileproperties", "");
                    if (fileproperties.length() <= 0) break block62;
                    try {
                        PropertyList fileDetails = new PropertyList(new JSONObject(fileproperties));
                        String keyid1 = ajaxresponse.getRequestParameter("worksheetitemid");
                        String keyid2 = ajaxresponse.getRequestParameter("worksheetitemversionid");
                        PropertyList markup = fileDetails.getPropertyList("markup");
                        if (markup != null && markup.getProperty("refresh", "N").equalsIgnoreCase("Y")) {
                            markup.setProperty("refresh", "N");
                            if (markup.getProperty("raw", "N").equalsIgnoreCase("Y")) {
                                markup.setProperty("data", "");
                                fileDetails.setProperty("display", "");
                            } else {
                                PropertyList config = new PropertyList();
                                try {
                                    config.setJSONString(ajaxresponse.getRequestParameter("config"));
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                FileAjaxHandler.refreshFileDetails(fileDetails, "LV_WorksheetItem", keyid1, keyid2, config, this.getConnectionId(), this.logger, false);
                            }
                        }
                        ajaxresponse.addCallbackArgument("elementid", elementid);
                        ajaxresponse.addCallbackArgument("html", FileAjaxHandler.getViewArea(fileDetails, keyid1, keyid2));
                        ajaxresponse.addCallbackArgument("file", fileDetails.toJSONString());
                    }
                    catch (Exception e) {
                        ajaxresponse.setError("Could not process file properties.");
                    }
                    break block62;
                }
                if (mode == Mode.RENDERMARKUP) {
                    String fileproperties = ajaxresponse.getRequestParameter("fileproperties", "");
                    if (fileproperties.length() > 0) {
                        try {
                            PropertyList file = new PropertyList(new JSONObject(fileproperties));
                            ajaxresponse.addCallbackArgument("elementid", elementid);
                            ajaxresponse.addCallbackArgument("html", FileAjaxHandler.getMarkupArea(elementid, file, this.logger, Configuration.isDevmode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId()), this.getTranslationProcessor()));
                        }
                        catch (Exception e) {
                            ajaxresponse.setError("Could not process file properties.");
                        }
                    }
                    break block62;
                }
                if (mode == Mode.RENDERCROPPER) {
                    String fileproperties = ajaxresponse.getRequestParameter("fileproperties", "");
                    if (fileproperties.length() > 0) {
                        try {
                            PropertyList file = new PropertyList(new JSONObject(fileproperties));
                            String keyid1 = ajaxresponse.getRequestParameter("worksheetitemid");
                            String keyid2 = ajaxresponse.getRequestParameter("worksheetitemversionid");
                            ajaxresponse.addCallbackArgument("elementid", elementid);
                            ajaxresponse.addCallbackArgument("html", FileAjaxHandler.getImageEditArea(elementid, file, keyid1, keyid2, this.logger, Configuration.isDevmode(this.getConnectionProcessor().getSapphireConnection().getDatabaseId()), this.getTranslationProcessor()));
                        }
                        catch (Exception e) {
                            ajaxresponse.setError("Could not process file properties.");
                        }
                    }
                    break block62;
                }
                if (mode != Mode.UPLOAD) break block62;
                String filedata = ajaxresponse.getRequestParameter("filedata", "");
                if (filedata.length() > 0) {
                    String filename = ajaxresponse.getRequestParameter("filename");
                    String filetype = ajaxresponse.getRequestParameter("filetype");
                    String worksheetitemid = ajaxresponse.getRequestParameter("worksheetitemid");
                    String worksheetitemversionid = ajaxresponse.getRequestParameter("worksheetitemversionid");
                    String attNum = ajaxresponse.getRequestParameter("attachment");
                    String worksheetid = ajaxresponse.getRequestParameter("worksheetid");
                    String worksheetversionid = ajaxresponse.getRequestParameter("worksheetversionid");
                    if (filedata.startsWith("data:")) {
                        FileTypeGroup type = null;
                        if (filetype.length() > 0 && FileTypeGroup.isValidTypeGroup(filetype, this.getConnectionId())) {
                            type = FileTypeGroup.getFileTypeGroupByType(filetype, this.getConnectionId());
                        } else if (FileTypeGroup.isValidFilename(filename)) {
                            type = FileTypeGroup.getFileTypeGroupByFileName(filename);
                        }
                        if (type != null && type != FileTypeGroup.UNKNOWN) {
                            long maxsize;
                            PropertyList config = new PropertyList();
                            try {
                                config.setJSONString(ajaxresponse.getRequestParameter("config"));
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                            long filesize = 0L;
                            try {
                                filesize = Long.parseLong(ajaxresponse.getRequestParameter("filesize", "0"));
                            }
                            catch (Exception type2) {
                                // empty catch block
                            }
                            boolean sizeok = true;
                            if (filesize > 0L && filesize > (maxsize = FileManager.getUploadMaxFileSize(this.getConnectionId()))) {
                                ajaxresponse.setError("File too large to be uploaded.");
                                sizeok = false;
                            }
                            if (!sizeok) break block62;
                            try {
                                PropertyList fileout = this.storeAsAttachment(filedata, type, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, filename, attNum, config, Worksheet.getPolicyNode(this.getQueryProcessor(), worksheetid, worksheetversionid));
                                attNum = fileout.getProperty("attachment", "");
                                if (attNum.length() > 0) {
                                    ajaxresponse.addCallbackArgument("elementid", elementid);
                                    ajaxresponse.addCallbackArgument("filedata", fileout.toJSONString());
                                    break block62;
                                }
                                ajaxresponse.setError("Could not upload attachment.");
                            }
                            catch (Exception e) {
                                ajaxresponse.setError("Failed to upload attachment. " + e.getMessage(), e);
                            }
                            break block62;
                        }
                        ajaxresponse.setError("Invalid file type provided.");
                        break block62;
                    }
                    ajaxresponse.setError("No data URL provided.");
                    break block62;
                }
                ajaxresponse.setError("No File data provided.");
            }
            finally {
                ajaxresponse.print();
            }
        }
    }

    public static void updateFileObject(PropertyList fileDetails, ByteArrayInputStream bis, FileType fileType, Logger logger, PropertyList config, String worksheetitemid, String connectionid, boolean export) throws Exception {
        StringBuilder display = new StringBuilder();
        FileTypeGroup fileTypeGroup = FileTypeGroup.getFileTypeGroupByFileType(fileType, connectionid);
        String renderStyle = File.getRenderStyle(fileTypeGroup, config, export);
        PropertyList markup = fileDetails.getPropertyList("markup");
        markup.setProperty("aspect", "");
        markup.setProperty("raw", "N");
        BaseFileDetails baseFileDetails = BaseFileDetails.getFileDetails(fileTypeGroup);
        try {
            baseFileDetails.setImageQuality(Integer.parseInt(config.getPropertyListNotNull("imagequality").getPropertyListNotNull(export ? "export" : "render").getProperty("jpegquality", "80")));
        }
        catch (NumberFormatException e) {
            baseFileDetails.setImageQuality(80);
        }
        try {
            baseFileDetails.setImageScale(Integer.parseInt(config.getPropertyListNotNull("imagequality").getPropertyListNotNull(export ? "export" : "render").getProperty("scale", "100")));
        }
        catch (NumberFormatException e) {
            baseFileDetails.setImageScale(100);
        }
        if (export) {
            baseFileDetails.setImageBorder(true);
        }
        baseFileDetails.setUniqueid(worksheetitemid);
        bis.reset();
        try {
            switch (fileTypeGroup) {
                case IMAGE: {
                    int width = FileAjaxHandler.getInt(markup.getProperty("imagewidth", config.getPropertyListNotNull("imagefiles").getProperty("maximagewidth")), 0);
                    int height = FileAjaxHandler.getInt(markup.getProperty("imageheight", config.getPropertyListNotNull("imagefiles").getProperty("maximageheight")), 0);
                    Dimension max = new Dimension(0, 0);
                    Dimension size = new Dimension(width, height);
                    Dimension imageDimension = FileManager.getImageFromBIS(display, bis, size, max, fileType, true, baseFileDetails.getImageScale(), baseFileDetails.getImageQuality(), logger, connectionid);
                    markup.setProperty("imagewidth", "" + (int)Math.round(imageDimension.getWidth()));
                    markup.setProperty("imageheight", "" + (int)Math.round(imageDimension.getHeight()));
                    DecimalFormat f = new DecimalFormat("##.00");
                    markup.setProperty("aspect", "" + f.format(imageDimension.getWidth() / imageDimension.getHeight()));
                    markup.setProperty("total", (int)max.getWidth() + "x" + (int)max.getHeight());
                    markup.setProperty("imagewidth", "" + width);
                    markup.setProperty("imageheight", "" + height);
                    if ((!markup.containsKey("data") || markup.getProperty("data").length() <= 0) && (!markup.containsKey("imageEdited") || !markup.getProperty("imageEdited").equalsIgnoreCase("Y"))) break;
                    display = new StringBuilder(fileDetails.getProperty("display").length() > 0 ? fileDetails.getProperty("display") : display);
                    break;
                }
                case WORD: {
                    WordFileDetails wordFileDetails = (WordFileDetails)baseFileDetails;
                    if (export) {
                        String rule = config.getPropertyListNotNull("wordfiles").getProperty("exportrule", "S");
                        if (rule.equalsIgnoreCase("S")) {
                            wordFileDetails.setFromPage(FileAjaxHandler.getInt(markup.getProperty("from", config.getPropertyListNotNull("wordfiles").getProperty("initialrenderfrom")), 1));
                            wordFileDetails.setToPage(FileAjaxHandler.getInt(markup.getProperty("to", config.getPropertyListNotNull("wordfiles").getProperty("maxwordpages")), -1));
                        } else if (rule.equalsIgnoreCase("R")) {
                            wordFileDetails.setFromPage(FileAjaxHandler.getInt(config.getPropertyListNotNull("wordfiles").getProperty("exportfrom", markup.getProperty("from", config.getPropertyListNotNull("wordfiles").getProperty("initialrenderfrom"))), 1));
                            wordFileDetails.setToPage(FileAjaxHandler.getInt(config.getPropertyListNotNull("wordfiles").getProperty("exportto", markup.getProperty("to", config.getPropertyListNotNull("wordfiles").getProperty("maxwordpages"))), -1));
                        } else {
                            wordFileDetails.setFromPage(1);
                            wordFileDetails.setToPage(-1);
                        }
                    } else {
                        wordFileDetails.setFromPage(FileAjaxHandler.getInt(markup.getProperty("from", config.getPropertyListNotNull("wordfiles").getProperty("initialrenderfrom")), 1));
                        wordFileDetails.setToPage(FileAjaxHandler.getInt(markup.getProperty("to", config.getPropertyListNotNull("wordfiles").getProperty("maxwordpages")), -1));
                    }
                    wordFileDetails.setMaxAllowed(FileAjaxHandler.getInt(config.getPropertyListNotNull("wordfiles").getProperty("maxallowedwordpages"), 10));
                    wordFileDetails.setRenderStyle(renderStyle);
                    wordFileDetails.setFixedLayout(true);
                    if (export) {
                        wordFileDetails.setHtmlPageContainer("<div class=\"document_word document_page\"></div>");
                    }
                    wordFileDetails.setUniqueid(worksheetitemid);
                    display.append((CharSequence)FileManager.getWordHtmlFromBis(bis, wordFileDetails, logger));
                    markup.setProperty("from", "" + wordFileDetails.getFromPage());
                    markup.setProperty("to", "" + wordFileDetails.getToPage());
                    markup.setProperty("total", "" + wordFileDetails.getTotalPagesAvailable());
                    break;
                }
                case PDF: {
                    PdfFileDetails pdfFileDetails = (PdfFileDetails)baseFileDetails;
                    if (export) {
                        String rule = config.getPropertyListNotNull("pdffiles").getProperty("exportrule", "S");
                        if (rule.equalsIgnoreCase("S")) {
                            pdfFileDetails.setFromPage(FileAjaxHandler.getInt(markup.getProperty("from", config.getPropertyListNotNull("pdffiles").getProperty("initialrenderfrom")), 1));
                            pdfFileDetails.setToPage(FileAjaxHandler.getInt(markup.getProperty("to", config.getPropertyListNotNull("pdffiles").getProperty("maxpdfpages")), -1));
                        } else if (rule.equalsIgnoreCase("R")) {
                            pdfFileDetails.setFromPage(FileAjaxHandler.getInt(config.getPropertyListNotNull("pdffiles").getProperty("exportfrom", markup.getProperty("from", config.getPropertyListNotNull("pdffiles").getProperty("initialrenderfrom"))), 1));
                            pdfFileDetails.setToPage(FileAjaxHandler.getInt(config.getPropertyListNotNull("pdffiles").getProperty("exportto", markup.getProperty("to", config.getPropertyListNotNull("pdffiles").getProperty("maxpdfpages"))), -1));
                        } else {
                            pdfFileDetails.setFromPage(1);
                            pdfFileDetails.setToPage(-1);
                        }
                    } else {
                        pdfFileDetails.setFromPage(FileAjaxHandler.getInt(markup.getProperty("from", config.getPropertyListNotNull("pdffiles").getProperty("initialrenderfrom")), 1));
                        pdfFileDetails.setToPage(FileAjaxHandler.getInt(markup.getProperty("to", config.getPropertyListNotNull("pdffiles").getProperty("maxpdfpages")), -1));
                    }
                    pdfFileDetails.setMaxAllowed(FileAjaxHandler.getInt(config.getPropertyListNotNull("pdffiles").getProperty("maxallowedpdfpages"), 10));
                    pdfFileDetails.setRenderStyle(renderStyle);
                    pdfFileDetails.setUniqueid(worksheetitemid);
                    display.append((CharSequence)FileManager.getPdfHtmlFromBis(bis, pdfFileDetails, logger));
                    markup.setProperty("from", "" + pdfFileDetails.getFromPage());
                    markup.setProperty("to", "" + pdfFileDetails.getToPage());
                    markup.setProperty("total", "" + pdfFileDetails.getTotalPagesAvailable());
                    break;
                }
                case EXCEL: {
                    ExcelFileDetails excelFileDetails = (ExcelFileDetails)baseFileDetails;
                    excelFileDetails.setSheetName(markup.getProperty("sheetname", "1"));
                    excelFileDetails.setShowGridLines(markup.getProperty("showgridlines", config.getPropertyListNotNull("excelfiles").getProperty("showgridlines", "Y")).equals("Y"));
                    excelFileDetails.setUniqueid(worksheetitemid);
                    excelFileDetails.setRenderStyle(renderStyle);
                    display.append((CharSequence)FileManager.getExcelHtmlFromBis(bis, excelFileDetails, logger));
                    markup.setProperty("sheetname", "" + excelFileDetails.getSheetName());
                    markup.setProperty("allsheets", "" + excelFileDetails.getAllSheets());
                    break;
                }
                case PPT: {
                    PPTFileDetails pptFileDetails = (PPTFileDetails)baseFileDetails;
                    if (export) {
                        pptFileDetails.setFromSlide(FileAjaxHandler.getInt(markup.getProperty("from"), 1));
                        String rule = config.getPropertyListNotNull("powerpointfiles").getProperty("exportrule", "S");
                        if (rule.equalsIgnoreCase("S")) {
                            pptFileDetails.setFromSlide(FileAjaxHandler.getInt(markup.getProperty("from", config.getPropertyListNotNull("powerpointfiles").getProperty("initialrenderfrom")), 1));
                            pptFileDetails.setToSlide(FileAjaxHandler.getInt(markup.getProperty("to", config.getPropertyListNotNull("powerpointfiles").getProperty("maxpptslides")), -1));
                        } else if (rule.equalsIgnoreCase("R")) {
                            pptFileDetails.setFromSlide(FileAjaxHandler.getInt(config.getPropertyListNotNull("powerpointfiles").getProperty("exportfrom", markup.getProperty("from", config.getPropertyListNotNull("powerpointfiles").getProperty("initialrenderfrom"))), 1));
                            pptFileDetails.setToSlide(FileAjaxHandler.getInt(config.getPropertyListNotNull("powerpointfiles").getProperty("exportto", markup.getProperty("to", config.getPropertyListNotNull("powerpointfiles").getProperty("maxpptslides"))), -1));
                        } else {
                            pptFileDetails.setFromSlide(1);
                            pptFileDetails.setToSlide(-1);
                        }
                    } else {
                        pptFileDetails.setFromSlide(FileAjaxHandler.getInt(markup.getProperty("from", config.getPropertyListNotNull("powerpointfiles").getProperty("initialrenderfrom")), 1));
                        pptFileDetails.setToSlide(FileAjaxHandler.getInt(markup.getProperty("to", config.getPropertyListNotNull("powerpointfiles").getProperty("maxpptslides")), -1));
                    }
                    pptFileDetails.setScaleFactor(FileAjaxHandler.getInt(markup.getProperty("scalefactor"), -1));
                    pptFileDetails.setRenderStyle(renderStyle);
                    pptFileDetails.setMaxAllowed(FileAjaxHandler.getInt(config.getPropertyListNotNull("powerpointfiles").getProperty("maxallowedpptslides"), 5));
                    display.append((CharSequence)FileManager.getPptHtmlFromBis(bis, pptFileDetails, logger));
                    markup.setProperty("from", "" + pptFileDetails.getFromSlide());
                    markup.setProperty("to", "" + pptFileDetails.getToSlide());
                    markup.setProperty("total", "" + pptFileDetails.getTotalSlidesAvailable());
                    break;
                }
                case TXT: {
                    TextFileDetails textFileDetails = (TextFileDetails)baseFileDetails;
                    if (export) {
                        String rule = config.getPropertyListNotNull("textfiles").getProperty("exportrule", "S");
                        if (rule.equalsIgnoreCase("S")) {
                            textFileDetails.setFromLine(FileAjaxHandler.getInt(markup.getProperty("from", config.getPropertyListNotNull("textfiles").getProperty("initialrenderfrom")), 1));
                            textFileDetails.setToLine(FileAjaxHandler.getInt(markup.getProperty("to", config.getPropertyListNotNull("textfiles").getProperty("maxtextlines")), -1));
                        } else if (rule.equalsIgnoreCase("R")) {
                            textFileDetails.setFromLine(FileAjaxHandler.getInt(config.getPropertyListNotNull("textfiles").getProperty("exportfrom", markup.getProperty("from", config.getPropertyListNotNull("textfiles").getProperty("initialrenderfrom"))), 1));
                            textFileDetails.setToLine(FileAjaxHandler.getInt(config.getPropertyListNotNull("textfiles").getProperty("exportto", markup.getProperty("to", config.getPropertyListNotNull("textfiles").getProperty("maxtextlines"))), -1));
                        } else {
                            textFileDetails.setFromLine(1);
                            textFileDetails.setToLine(-1);
                        }
                    } else {
                        textFileDetails.setFromLine(FileAjaxHandler.getInt(markup.getProperty("from", config.getPropertyListNotNull("textfiles").getProperty("initialrenderfrom")), 1));
                        textFileDetails.setToLine(FileAjaxHandler.getInt(markup.getProperty("to", config.getPropertyListNotNull("textfiles").getProperty("maxtextlines")), -1));
                    }
                    textFileDetails.setMaxAllowed(FileAjaxHandler.getInt(config.getPropertyListNotNull("textfiles").getProperty("maxallowedtextlines"), 100));
                    textFileDetails.setRenderStyle(renderStyle);
                    display.append((CharSequence)FileManager.getTxtHTMLFromBis(bis, textFileDetails, logger));
                    markup.setProperty("from", "" + textFileDetails.getFromLine());
                    markup.setProperty("to", "" + textFileDetails.getToLine());
                    markup.setProperty("total", "" + textFileDetails.getTotalLinesAvailable());
                }
            }
        }
        catch (Exception e) {
            display.append(e.getMessage());
        }
        fileDetails.setProperty("display", display.toString());
    }

    private static int getInt(String value, int def) {
        int ret;
        try {
            ret = Integer.parseInt(value);
        }
        catch (Exception e) {
            ret = def;
        }
        return ret;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static void refreshFileDetails(PropertyList fileDetails, String sdcid, String keyid1, String keyid2, PropertyList config, String connectionid, Logger logger, boolean export) throws Exception {
        int attNum = -1;
        try {
            attNum = Integer.parseInt(fileDetails.getProperty("attachment"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (attNum <= -1) throw new Exception("No attachment stored.");
        AttachmentProcessor arp = new AttachmentProcessor(connectionid);
        Attachment attachment = arp.getSDIAttachment(sdcid, keyid1, keyid2, "(null)", attNum);
        if (attachment == null) throw new Exception("Could not obtain attachment.");
        byte[] data = attachment.getData();
        if (data == null) throw new Exception("Attachment has no data. File may be invalid.");
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);){
            if (fileDetails.getProperty("filename").equalsIgnoreCase(attachment.getFilename())) {
                try {
                    FileAjaxHandler.updateFileObject(fileDetails, bis, FileType.getFileTypeByFileName(fileDetails.getProperty("filename"), connectionid), logger, config, keyid1, connectionid, export);
                    return;
                }
                catch (Exception e) {
                    throw new Exception("Could not obtain stored file type.", e);
                }
            }
            logger.warn("Attachment file has changed. Update types.");
            fileDetails.setProperty("filename", attachment.getFilename());
            fileDetails.setProperty("description", attachment.getDescription());
            try {
                fileDetails.setProperty("shortfilename", new java.io.File(attachment.getFilename()).getName());
            }
            catch (Exception e) {
                fileDetails.setProperty("shortfilename", attachment.getFilename());
            }
            FileTypeGroup fileTypeGroup = FileTypeGroup.getFileTypeGroupByFileName(attachment.getFilename());
            if (fileTypeGroup == null || !fileTypeGroup.isSupported()) throw new Exception("Could not discover file type from file.");
            fileDetails.setProperty("filetype", fileTypeGroup.toString());
            PropertyList oldmarkup = fileDetails.getPropertyList("markup");
            if (oldmarkup == null) {
                oldmarkup = new PropertyList();
            }
            PropertyList markup = oldmarkup.copy();
            switch (fileTypeGroup) {
                case IMAGE: {
                    markup.setProperty("imagewidth", oldmarkup.getProperty("imagewidth", config.getPropertyListNotNull("imagefiles").getProperty("maximagewidth")));
                    markup.setProperty("imageheight", oldmarkup.getProperty("imageheight", config.getPropertyListNotNull("imagefiles").getProperty("maximageheight")));
                    break;
                }
                case WORD: {
                    markup.setProperty("from", oldmarkup.getProperty("from", ""));
                    markup.setProperty("to", oldmarkup.getProperty("to", ""));
                    break;
                }
                case PDF: {
                    markup.setProperty("from", oldmarkup.getProperty("from", ""));
                    markup.setProperty("to", oldmarkup.getProperty("to", ""));
                    break;
                }
                case PPT: {
                    markup.setProperty("from", oldmarkup.getProperty("from", ""));
                    markup.setProperty("to", oldmarkup.getProperty("to", ""));
                    break;
                }
                case EXCEL: {
                    markup.setProperty("from", oldmarkup.getProperty("from", ""));
                    markup.setProperty("to", oldmarkup.getProperty("to", ""));
                    break;
                }
                case TXT: {
                    markup.setProperty("from", oldmarkup.getProperty("from", ""));
                    markup.setProperty("to", oldmarkup.getProperty("to", ""));
                }
            }
            fileDetails.setProperty("markup", markup);
            FileAjaxHandler.updateFileObject(fileDetails, bis, FileType.getFileTypeByFileName(attachment.getFilename(), connectionid), logger, config, keyid1, connectionid, export);
            return;
        }
    }

    private static void generateFileObject(PropertyList fileDetails, String filename, FileTypeGroup fileType, String filedata, String attNum, PropertyList config) {
        fileDetails.setProperty("config", config);
        fileDetails.setProperty("attachment", attNum);
        fileDetails.setProperty("filetype", fileType.toString());
        fileDetails.setProperty("filename", filename);
    }

    public static boolean generateFileObject(PropertyList fileDetails, Logger logger, String worksheetitemid, String worksheetitemversionid, String connectionId) throws Exception {
        try {
            PropertyList config = fileDetails.getPropertyList("config");
            PropertyList markup = fileDetails.getPropertyListNotNull("markup");
            if (markup.getProperty("publishattachment").isEmpty()) {
                markup.setProperty("publishattachment", config.getProperty("publishattachment"));
                markup.setProperty("publishattachmentcaption", config.getProperty("publishattachmentcaption"));
            }
            FileAjaxHandler.refreshFileDetails(fileDetails, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, fileDetails.getPropertyList("config"), connectionId, logger, false);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    private PropertyList storeAsAttachment(FileManager.FileData fileData, FileTypeGroup fileType, String sdcid, String keyid1, String keyid2, String filename, String attNum, PropertyList config, String elnpolicynode) throws Exception {
        PropertyList outProp = new PropertyList();
        String filerepositoryid = "";
        String filerepositorynode = "";
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        PropertyList policy = configProcessor.getPolicy("ELNPolicy", elnpolicynode);
        if (policy != null && policy.containsKey("attachments")) {
            PropertyList attachments = policy.getPropertyList("attachments");
            filerepositoryid = attachments.getProperty("filerepositoryid", "");
            filerepositorynode = attachments.getProperty("filerepositorynode", "");
        }
        String orgFilename = filename;
        String worksheetitemid = keyid1;
        sapphire.accessor.AttachmentProcessor attachmentProcessor = new sapphire.accessor.AttachmentProcessor(this.getConnectionId());
        sapphire.attachment.Attachment attachment = null;
        if (attNum.length() > 0) {
            int aN = 1;
            try {
                aN = Integer.parseInt(attNum);
            }
            catch (Exception exception) {
                // empty catch block
            }
            attachment = sapphire.attachment.Attachment.getAttachment(sdcid, keyid1, keyid2, "", aN);
            attachment = attachmentProcessor.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
        } else {
            attachment = sapphire.attachment.Attachment.getAttachment(sdcid, keyid1, keyid2, "");
        }
        attachment.setInputStream(fileData.getInputStream());
        attachment.setSourceFilename(filename);
        attachment.setFilename(filename);
        attachment.setAttachmentType(Attachment.AttachmentType.FILE);
        if (filerepositoryid.length() > 0) {
            ((Attachment)attachment).setAttachmentRepositoryId(filerepositoryid);
            if (filerepositorynode.length() > 0) {
                ((Attachment)attachment).setAttachmentRepositoryNodeId(filerepositorynode);
            }
        }
        sapphire.attachment.Attachment outAtt = null;
        outAtt = attNum.length() > 0 ? attachmentProcessor.editSDIAttachment(attachment) : attachmentProcessor.addSDIAttachment(attachment, false, false, "Sapphire Custom");
        if (outAtt != null) {
            FileAjaxHandler.generateFileObject(outProp, orgFilename, fileType, "", "" + outAtt.getAttachmentNum() + "", config);
        }
        return outProp;
    }

    private PropertyList storeAsAttachment(String filedata, FileTypeGroup fileType, String sdcid, String keyid1, String keyid2, String filename, String attNum, final PropertyList config, String elnpolicynode) throws Exception {
        final PropertyList outProp = new PropertyList();
        String filerepositoryid = "";
        String filerepositorynode = "";
        ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.getConnectionId());
        PropertyList policy = configProcessor.getPolicy("ELNPolicy", elnpolicynode);
        if (policy != null && policy.containsKey("attachments")) {
            PropertyList attachments = policy.getPropertyList("attachments");
            filerepositoryid = attachments.getProperty("filerepositoryid", "");
            filerepositorynode = attachments.getProperty("filerepositorynode", "");
        }
        final String orgFilename = filename;
        FileManager.storeAsAttachment(filedata, fileType, sdcid, keyid1, keyid2, "", filename, attNum, filerepositoryid, filerepositorynode, new FileManager.AttachmentHandler(){

            @Override
            public void processAttachment(ByteArrayInputStream bis, String filename, FileTypeGroup fileType, String filedata, int attachmentNum) {
                FileAjaxHandler.generateFileObject(outProp, orgFilename, fileType, filedata, attachmentNum + "", config);
            }
        }, this.getConnectionId());
        return outProp;
    }

    public static String getUploadArea(String id) {
        return "<div id=\"" + id + "_uploader\" class=\"dropzone\" style=\"height:197px;\"></div>";
    }

    public static String getEditArea(String id, PropertyList fileDetails, String keyid1, String keyid2, Logger logger, PropertyList config, boolean devMode, TranslationProcessor tp, String connectionId) {
        StringBuilder html = new StringBuilder();
        FileType fileType = FileType.getFileTypeByFileName(fileDetails.getProperty("filename", ""), connectionId);
        FileTypeGroup fileTypeGroup = FileTypeGroup.valueOf(fileDetails.getProperty("filetype", FileTypeGroup.TXT.toString()).toUpperCase());
        html.append("<div id=\"filetoolbar\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: rgb(246, 246, 246); border: 1px solid gray; padding: 2px;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"padding-left: 5px; padding-right: 5px;\"><tbody><tr>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.renderUpload('").append(id).append("');\" title=\"").append(tp.translate("Upload a new file")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_upload.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.doRefreshFile('").append(id).append("');\" title=\"").append(tp.translate("Revert file from attachment")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_return.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        if (fileTypeGroup == FileTypeGroup.IMAGE) {
            html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.renderMarkup('").append(id).append("');\" title=\"").append(tp.translate("Markup image")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_draw_paintbrush.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
            html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.renderCropper('").append(id).append("');\" title=\"").append(tp.translate("Crop and Edit image")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_edit_box.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        }
        html.append("</tr></tbody></table>");
        html.append("</td>");
        html.append("</tr></tbody></table>");
        html.append("</div>");
        html.append("<div style=\"background-color:#FFFFFF;border:solid 1px #A9A9A9;\">");
        html.append("<div style=\"padding:5px;\">");
        html.append(tp.translate("Uploaded File")).append(": <strong>").append(SafeHTML.encodeForHTML(fileDetails.getProperty("description", fileDetails.getProperty("shortfilename", fileDetails.getProperty("filename"))))).append("</strong> ").append(tp.translate("of type")).append(" ").append(fileTypeGroup.getName());
        html.append("</div>");
        html.append("<div style=\"padding:5px;\">");
        PropertyList markup = fileDetails.getPropertyList("markup") != null ? fileDetails.getPropertyList("markup") : new PropertyList();
        switch (fileTypeGroup) {
            case IMAGE: {
                boolean raw = markup.getProperty("raw", "N").equalsIgnoreCase("Y");
                html.append(tp.translate("Size")).append(" ");
                html.append("<input ").append(raw ? "disabled " : "").append("type=text size=3 value=\"").append(markup.getProperty("imagewidth", markup.getProperty("x", "0"))).append("\" id=\"").append(id).append("_imagewidth\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">px");
                html.append(" ").append(tp.translate("by")).append(" ");
                html.append("<input  ").append(raw ? "disabled " : "").append("type=text size=3 value=\"").append(markup.getProperty("imageheight", markup.getProperty("y", "0"))).append("\" id=\"").append(id).append("_imageheight\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">px");
                html.append("&nbsp;");
                if (!(fileType.getName().equals("TIFF") || fileType.getName().equals("TIF") || fileType.getName().equals("WMF") || fileType.getName().equals("EMF"))) {
                    html.append("<input type=checkbox id=\"").append(id).append("_raw\" onclick=\"fileEditor.doChange(this,'").append(id).append("')\" ").append(raw ? "checked" : "").append(">");
                    html.append(" ").append(tp.translate("Use Raw Image"));
                    html.append(" ").append(tp.translate("(Native Resolution: ")).append(markup.getProperty("total")).append(")");
                    break;
                }
                html.append(" ").append(tp.translate("(Native Resolution: ")).append(markup.getProperty("total")).append(")");
                break;
            }
            case WORD: 
            case PDF: {
                int wt = FileAjaxHandler.getTotal(markup);
                if (wt > 1 || !markup.containsKey("total")) {
                    html.append(tp.translate("Show pages")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("from", markup.getProperty("x", "1"))).append("\" id=\"").append(id).append("_from\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" ").append(tp.translate("to")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("to", markup.getProperty("y", wt + ""))).append("\" id=\"").append(id).append("_to\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    if (wt <= 0) break;
                    html.append(" (").append(tp.translate("of")).append(" ").append(wt).append(" ").append(tp.translate("pages")).append(")");
                    break;
                }
                html.append("(").append(tp.translate("Single page document")).append(")");
                break;
            }
            case EXCEL: {
                String allSheets = markup.getProperty("allsheets");
                if (allSheets.length() > 0) {
                    String[] parts = allSheets.split(";");
                    html.append(tp.translate("Sheet name or number")).append(" ");
                    String selected = markup.getProperty("sheetname");
                    html.append("<select id=\"").append(id).append("_sheetname\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append("<option " + ("_all".equals(selected) ? " selected " : "") + " value=\"_all\"> - All Sheets - </option>");
                    for (String part : parts) {
                        html.append("<option value=\"" + part + "\"" + (part.equals(selected) ? " selected " : "") + ">" + part + "</option>");
                    }
                    html.append("</select>");
                } else {
                    html.append(tp.translate("Sheet name or number")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("sheetname")).append("\" id=\"").append(id).append("_sheetname\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                }
                boolean showgridlines = markup.getProperty("showgridlines", "N").equalsIgnoreCase("Y");
                html.append("&nbsp;");
                html.append("<input type=checkbox id=\"").append(id).append("_showgridlines\" value=\"" + (showgridlines ? "Y" : "N") + "\" onclick=\"this.value=this.checked?'Y':'N';fileEditor.doChange(this,'").append(id).append("')\" ").append(showgridlines ? "checked" : "").append(">");
                html.append(" <label for=\"").append(id).append("_showgridlines\">").append(tp.translate("Show Gridlines") + "</label>");
                break;
            }
            case PPT: {
                int ppt = FileAjaxHandler.getTotal(markup);
                if (ppt > 1) {
                    html.append(tp.translate("Show slides")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("from", "1")).append("\" id=\"").append(id).append("_from\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" ").append(tp.translate("to")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("to", ppt + "")).append("\" id=\"").append(id).append("_to\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" (").append(tp.translate("of")).append(" ").append(ppt).append(" ").append(tp.translate("slides")).append(")");
                    if (!config.getPropertyListNotNull("powerpointfiles").getProperty("pptrenderstyle").equalsIgnoreCase("image")) break;
                    html.append("&nbsp;");
                    html.append("&nbsp;");
                    html.append(tp.translate("Image Scale Factor")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("scalefactor", "100")).append("\" id=\"").append(id).append("_scalefactor\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">%");
                    break;
                }
                html.append("(").append(tp.translate("Single slide presentation")).append(")");
                break;
            }
            case TXT: {
                int tt = FileAjaxHandler.getTotal(markup);
                if (tt > 1) {
                    html.append(tp.translate("Show lines")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("from", markup.getProperty("x", "1"))).append("\" id=\"").append(id).append("_from\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" ").append(tp.translate("to")).append(" ");
                    html.append("<input type=text size=3 value=\"").append(markup.getProperty("to", markup.getProperty("y", tt + ""))).append("\" id=\"").append(id).append("_to\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\">");
                    html.append(" (").append(tp.translate("of")).append(" ").append(tt).append(" ").append(tp.translate("lines")).append(")");
                    break;
                }
                html.append("(").append(tp.translate("Single line file")).append(")");
            }
        }
        boolean publishAttachment = markup.getProperty("publishattachment", "N").equalsIgnoreCase("Y");
        html.append("&nbsp;");
        html.append("<input type=checkbox id=\"").append(id).append("_publishattachment\" requiresrefresh=\"N\" value=\"" + (publishAttachment ? "Y" : "N") + "\" onclick=\"this.value=this.checked?'Y':'N';fileEditor.doChange(this,'").append(id).append("')\" ").append(publishAttachment ? "checked" : "").append(">");
        html.append(" <label for=\"").append(id).append("_publishattachment\">").append(tp.translate("Add Attachment When Publishing.") + "</label>");
        String publishAttachmentCaption = markup.getProperty("publishattachmentcaption", "[filename]");
        html.append("&nbsp;<span id=\"attachmentcaption\">");
        html.append(" ").append(tp.translate(" Caption: "));
        html.append("<input type=text id=\"").append(id).append("_publishattachmentcaption\" requiresrefresh=\"N\" value=\"" + publishAttachmentCaption + "\" onchange=\"fileEditor.doChange(this,'").append(id).append("')\" ").append(">");
        html.append("</span>");
        html.append("</div>");
        html.append("<div id=\"").append(id).append("_preview\" style=\"margin: 5px; padding:5px; border: solid 1px #CFDFF0;overflow:auto;max-height:300px;\">");
        html.append(FileAjaxHandler.getViewArea(fileDetails, keyid1, keyid2));
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    public static int getTotal(PropertyList markup) {
        int wt = 0;
        try {
            wt = Integer.parseInt(markup.getProperty("total", "0"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        return wt;
    }

    public static String getImageEditArea(String id, PropertyList fileDetails, String keyid1, String keyid2, Logger logger, boolean devMode, TranslationProcessor tp) {
        String url;
        StringBuilder html = new StringBuilder();
        html.append("<div id=\"imageedittoolbar\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"background-color: rgb(246, 246, 246); border: 1px solid gray; padding: 2px;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\">");
        html.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"padding-left: 5px; padding-right: 5px;\"><tbody><tr>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.image.crop('").append(id).append("');\" title=\"").append(tp.translate("Crop Image")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_crop.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.image.rotate('").append(id).append("');\" title=\"").append(tp.translate("Rotate Clockwise 90%")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_transform_rotate_clockwise.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.image.flipH('").append(id).append("');\" title=\"").append(tp.translate("Flip Horizontally")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_transform_flip_horizontal.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("<td align=\"left\" style=\"vertical-align: top;\" onclick=\"fileEditor.image.flipV('").append(id).append("');\" title=\"").append(tp.translate("Flip Vertically")).append("\"><div tabindex=\"0\" class=\"gwt_toolbar_bg\" onmouseover=\"this.className = 'gwt_toolbar_bg_over';\" onmouseout=\"this.className = 'gwt_toolbar_bg';\" style=\"height: 24px; padding-left: 6px; padding-right: 6px;\"><input type=\"text\" tabindex=\"-1\" role=\"presentation\" style=\"opacity: 0; height: 1px; width: 1px; z-index: -1; overflow: hidden; position: absolute;\"><table cellspacing=\"0\" cellpadding=\"0\" style=\"height: 100%;\"><tbody><tr><td align=\"center\" style=\"vertical-align: middle;\"><img src=\"WEB-CORE/imageref/flat/32/flat_black_transform_flip_vertical.svg\" class=\"gwt-Image\" style=\"width: 16px; height: 16px;\"></td></tr></tbody></table></div></td>");
        html.append("</tr></tbody></table>");
        html.append("</td>");
        html.append("</tr></tbody></table>");
        html.append("</div>");
        html.append("<div id=\"").append(id).append("_croppercontainer\" style=\"background-color:#FFFFFF;border:solid 1px #A9A9A9;\">");
        PropertyList markup = fileDetails.getPropertyList("markup");
        String attNum = fileDetails.getProperty("attachment", "");
        if (markup != null && markup.getProperty("raw", "N").equalsIgnoreCase("Y")) {
            url = "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y";
        } else {
            url = fileDetails.getProperty("display");
            if (url.length() > 0) {
                if (!url.startsWith("data:")) {
                    url = "data:" + url;
                }
            } else {
                url = "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y";
            }
        }
        html.append("<img id=\"").append(id).append("_cropper\" src=\"").append(url).append("\" style=\"max-width:100%;\">");
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    public static String getViewArea(PropertyList fileDetails, String keyid1, String keyid2) {
        StringBuilder html = new StringBuilder();
        String attNum = fileDetails.getProperty("attachment", "");
        if (attNum.length() > 0) {
            FileTypeGroup fileType = FileTypeGroup.valueOf(fileDetails.getProperty("filetype", FileTypeGroup.TXT.toString()).toUpperCase());
            switch (fileType) {
                case IMAGE: {
                    String url;
                    PropertyList markup = fileDetails.getPropertyList("markup");
                    if (markup != null && markup.getProperty("raw", "N").equalsIgnoreCase("Y")) {
                        url = "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y";
                    } else {
                        url = fileDetails.getProperty("display");
                        if (url.length() > 0) {
                            if (!url.startsWith("data:")) {
                                url = "data:" + url;
                            }
                        } else {
                            url = "rc?command=image&attachment=LV_WorksheetItem;" + keyid1 + ";" + keyid2 + ";(null);" + attNum + "&nocache=Y";
                        }
                    }
                    html.append("<img ").append(fileDetails.getProperty("imageboder", "N").equalsIgnoreCase("Y") ? "style=\"border:solid 1px black;\" " : "").append("src=\"").append(url).append("\" title=\"").append(fileDetails.get("filename")).append("\">");
                    break;
                }
                case WORD: 
                case PDF: 
                case EXCEL: 
                case PPT: 
                case TXT: {
                    html.append(fileDetails.getProperty("display"));
                }
            }
        }
        return html.toString();
    }

    public static String getMarkupArea(String id, PropertyList fileDetails, Logger logger, boolean devMode, TranslationProcessor tp) {
        StringBuilder html = new StringBuilder();
        FileTypeGroup fileType = FileTypeGroup.valueOf(fileDetails.getProperty("filetype", FileTypeGroup.TXT.toString()).toUpperCase());
        switch (fileType) {
            case IMAGE: {
                html.append("<div id=\"").append(id).append("_paint\" style=\"border:solid 1px #A9A9A9;\"></div>");
                break;
            }
            case WORD: 
            case PDF: 
            case EXCEL: 
            case PPT: 
            case TXT: {
                html.append(tp.translate("No markup available."));
            }
        }
        return html.toString();
    }

    private static enum Mode {
        UPLOAD,
        RENDERUPLOAD,
        REFRESH,
        RENDERCROPPER,
        RENDERMARKUP;

    }
}

