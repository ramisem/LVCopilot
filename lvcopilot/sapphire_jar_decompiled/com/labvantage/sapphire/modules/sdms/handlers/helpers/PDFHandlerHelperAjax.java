/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.LoadOptions
 *  com.aspose.pdf.XpsLoadOptions
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.sdms.handlers.helpers;

import com.aspose.pdf.Document;
import com.aspose.pdf.LoadOptions;
import com.aspose.pdf.XpsLoadOptions;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.maint.EditorStyleField;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.DocumentFileParsingOptions;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import com.labvantage.sapphire.util.file.PdfFileDetails;
import com.labvantage.sapphire.util.file.WordFileDetails;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.accessor.AttachmentProcessor;
import sapphire.attachment.Attachment;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PDFHandlerHelperAjax
extends BaseAjaxRequest {
    private void getVariables(String attachmenthandlerid, StringBuilder html, StringBuilder script, PageContext pageContext) {
        int i;
        DocumentFileParsingOptions documentFileParsingOptions = new DocumentFileParsingOptions();
        DataSet vars = this.getQueryProcessor().getPreparedSqlDataSet("SELECT propertyclob FROM attachmenthandler WHERE attachmenthandlerid=?", new Object[]{attachmenthandlerid}, true);
        if (vars != null && vars.size() == 1) {
            PropertyList setupvars = new PropertyList();
            try {
                setupvars = new PropertyList(new JSONObject(vars.getClob(0, "propertyclob", "{}")));
            }
            catch (Exception exception) {
                // empty catch block
            }
            PropertyListCollection variables = setupvars.getCollectionNotNull("variables");
            for (i = 0; i < variables.size(); ++i) {
                PropertyList variable = variables.getPropertyList(i);
                String varid = variable.getProperty("variableid");
                if (varid.equalsIgnoreCase("clearworkingfolder")) {
                    documentFileParsingOptions.setClearWorkingFolder(variable.getProperty("value").equalsIgnoreCase("Y"));
                }
                if (varid.equalsIgnoreCase("extracttables")) {
                    documentFileParsingOptions.setExtractTables(variable.getProperty("value").equalsIgnoreCase("Y"));
                }
                if (varid.equalsIgnoreCase("extractasxml")) {
                    documentFileParsingOptions.setExtractAsXML(variable.getProperty("value").equalsIgnoreCase("Y"));
                }
                if (varid.equalsIgnoreCase("extracttext")) {
                    documentFileParsingOptions.setExtractText(variable.getProperty("value").equalsIgnoreCase("Y"));
                }
                if (varid.equalsIgnoreCase("extractimages")) {
                    documentFileParsingOptions.setExtractImages(variable.getProperty("value").equalsIgnoreCase("Y"));
                    continue;
                }
                if (varid.equalsIgnoreCase("endtablete") || varid.equalsIgnoreCase("endtabletext")) {
                    documentFileParsingOptions.setEndTableText(variable.getProperty("value"));
                    continue;
                }
                if (varid.equalsIgnoreCase("mergecolumns")) {
                    documentFileParsingOptions.setMergeColumns(variable.getProperty("value"));
                    continue;
                }
                if (varid.equalsIgnoreCase("tablecolumns")) {
                    documentFileParsingOptions.setTableColumns(variable.getProperty("value"));
                    continue;
                }
                if (varid.equalsIgnoreCase("pageto")) {
                    try {
                        documentFileParsingOptions.setPageTo(Integer.parseInt(variable.getProperty("value")));
                    }
                    catch (Exception exception) {}
                    continue;
                }
                if (varid.equalsIgnoreCase("pagefrom")) {
                    try {
                        documentFileParsingOptions.setPageFrom(Integer.parseInt(variable.getProperty("value")));
                    }
                    catch (Exception exception) {}
                    continue;
                }
                if (!varid.equalsIgnoreCase("tabletoextract")) continue;
                try {
                    documentFileParsingOptions.setTableToExtract(Integer.parseInt(variable.getProperty("value")));
                    continue;
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        html.append("<div class='grid'>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("Extract Tables");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        EditorStyleField editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("extracttables");
            editorStyleField.setElementid("extracttables");
            editorStyleField.setFieldValue(documentFileParsingOptions.getExtractTables() ? "Y" : "N");
            editorStyleField.setEditorStyleId("Yes No Checkbox");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception variables) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("Extract Images");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("extractimages");
            editorStyleField.setElementid("extractimages");
            editorStyleField.setFieldValue(documentFileParsingOptions.getExtractImages() ? "Y" : "N");
            editorStyleField.setEditorStyleId("Yes No Checkbox");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception variables) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("Extract Text");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("extracttext");
            editorStyleField.setElementid("extracttext");
            editorStyleField.setFieldValue(documentFileParsingOptions.getExtractText() ? "Y" : "N");
            editorStyleField.setEditorStyleId("Yes No Checkbox");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception variables) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("Extract As XML");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("extractasxml");
            editorStyleField.setElementid("extractasxml");
            editorStyleField.setFieldValue(documentFileParsingOptions.getExtractAsXML() ? "Y" : "N");
            editorStyleField.setEditorStyleId("Yes No Checkbox");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception variables) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("Table Columns");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("tablecolumns");
            editorStyleField.setElementid("tablecolumns");
            editorStyleField.setFieldValue(StringUtil.arrayToString(documentFileParsingOptions.getTableColumns(), ";"));
            editorStyleField.setEditorStyleId("String");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception variables) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("From Page");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("pagefrom");
            editorStyleField.setElementid("pagefrom");
            editorStyleField.setFieldValue("" + documentFileParsingOptions.getPageFrom());
            editorStyleField.setEditorStyleId("String");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception variables) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("To Page");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("pageto");
            editorStyleField.setElementid("pageto");
            editorStyleField.setFieldValue("" + documentFileParsingOptions.getPageTo());
            editorStyleField.setEditorStyleId("String");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception variables) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("Table To Extract");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("tabletoextract");
            editorStyleField.setElementid("tabletoextract");
            editorStyleField.setFieldValue("" + documentFileParsingOptions.getTableToExtract());
            editorStyleField.setEditorStyleId("String");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception variables) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("Merge Columns");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("mergecolumns");
            editorStyleField.setElementid("mergecolumns");
            StringBuilder temp = new StringBuilder();
            for (i = 0; i < documentFileParsingOptions.getMergeColumns().size(); ++i) {
                if (temp.length() > 0) {
                    temp.append("|");
                }
                DataSetUtil.MergeColumn m = documentFileParsingOptions.getMergeColumns().get(i);
                temp.append(m.getFrom());
                temp.append(";");
                temp.append(m.getTo());
            }
            editorStyleField.setFieldValue(temp.toString());
            editorStyleField.setEditorStyleId("String");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception temp) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldtitle'>");
        html.append("End Table Text");
        html.append("</div>");
        html.append("<div class='fieldvalue'>");
        editorStyleField = new EditorStyleField(pageContext);
        try {
            editorStyleField.setFieldName("endtablete");
            editorStyleField.setElementid("endtablete");
            editorStyleField.setFieldValue(documentFileParsingOptions.getEndTableText());
            editorStyleField.setEditorStyleId("String");
            html.append(editorStyleField.getHtml());
        }
        catch (Exception temp) {
            // empty catch block
        }
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldvalue'>");
        html.append("&nbsp;");
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='buttongridrow'>");
        html.append("<div class='buttongridcolumn'>");
        Button button = new Button(pageContext);
        button.setText("Run Parse");
        button.setAction("pdf.runParse()");
        html.append(button.getHtml());
        html.append("</div>");
        html.append("<div class='buttongridcolumn'>");
        button = new Button(pageContext);
        button.setText("Save Settings");
        button.setAction("pdf.saveSettings()");
        html.append(button.getHtml());
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldvalue'>");
        html.append("&nbsp;");
        html.append("</div>");
        html.append("</div>");
        html.append("<div class='gridrow'>");
        html.append("<div class='fieldvalue'>");
        html.append("<a target=\"orginal\" href=\"rc?command=attachment&mode=view&sdcid=").append("LV_AttachmentHandler").append("&keyid1=").append(attachmenthandlerid).append("&attachmentclass=HandlerExampleFile\">View Original Document</a>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
    }

    private void getOriginal(String attachmenthandlerid, AjaxResponse ajaxResponse, StringBuilder html, StringBuilder script, PageContext pageContext) {
        Attachment attachment = new Attachment();
        attachment.setSDCId("LV_AttachmentHandler");
        attachment.setKeyId1(attachmenthandlerid);
        attachment.setAttachmentClass("HandlerExampleFile");
        AttachmentProcessor ap = new AttachmentProcessor(pageContext);
        ap.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
        if (attachment != null) {
            try {
                StringBuilder temp;
                String filename = attachment.getSourceFilename();
                if (FileType.getFileType(filename, this.getConnectionId()).getName().equals("PDF")) {
                    PdfFileDetails fileDetails = new PdfFileDetails();
                    fileDetails.setFromPage(-1);
                    fileDetails.setToPage(-1);
                    fileDetails.setRenderStyle("html");
                    fileDetails.setHtmlPageContainer("<div class=\"document_pdf document_page\"></div>");
                    temp = FileManager.getPdfHtmlFromBis(attachment.getInputStream(), fileDetails, this.logger);
                } else if (FileType.getFileType(filename, this.getConnectionId()).getName().equals("DOC") || FileType.getFileType(filename, this.getConnectionId()).getName().equals("DOCX")) {
                    WordFileDetails fileDetails = new WordFileDetails();
                    fileDetails.setFromPage(-1);
                    fileDetails.setToPage(-1);
                    fileDetails.setRenderStyle("html");
                    fileDetails.setHtmlPageContainer("<div class=\"document_word document_page\"></div>");
                    temp = FileManager.getWordHtmlFromBis(attachment.getInputStream(), fileDetails, this.logger);
                } else if (FileType.getFileType(filename, this.getConnectionId()).getName().equals("XPS")) {
                    XpsLoadOptions psLoadOptions = new XpsLoadOptions();
                    Document pdfDocument = new Document(attachment.getInputStream(), (LoadOptions)psLoadOptions);
                    this.logger.debug("XPS Read");
                    PdfFileDetails fileDetails = new PdfFileDetails();
                    fileDetails.setFromPage(-1);
                    fileDetails.setToPage(-1);
                    fileDetails.setRenderStyle("html");
                    fileDetails.setHtmlPageContainer("<div class=\"document_xps document_page\"></div>");
                    temp = FileManager.getPdfHtmlFromDoc(pdfDocument, fileDetails, this.logger);
                } else {
                    this.logger.error("Invalid file provided");
                    temp = new StringBuilder("Invalid file provided.");
                }
                html.append((CharSequence)temp);
            }
            catch (Exception e) {
                this.logger.error("Failed to render example file");
                html.append("Failed to render example file.");
            }
        } else {
            this.logger.error("Could not load example file");
            html.append("Could not load example file.");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void parse(String attachmenthandlerid, AjaxResponse ajaxResponse, StringBuilder html, StringBuilder script, PageContext pageContext) {
        html.append("<div class=\"results\">");
        DocumentFileParsingOptions documentFileParsingOptions = new DocumentFileParsingOptions();
        documentFileParsingOptions.setExtractTables(ajaxResponse.getRequestParameter("extracttables", "Y").equalsIgnoreCase("Y"));
        documentFileParsingOptions.setExtractText(ajaxResponse.getRequestParameter("extracttext", "N").equalsIgnoreCase("Y"));
        documentFileParsingOptions.setExtractImages(ajaxResponse.getRequestParameter("extractimages", "N").equalsIgnoreCase("Y"));
        documentFileParsingOptions.setExtractAsXML(ajaxResponse.getRequestParameter("extractasxml", "N").equalsIgnoreCase("Y"));
        documentFileParsingOptions.setTableColumns(ajaxResponse.getRequestParameter("tablecolumns", ""));
        try {
            documentFileParsingOptions.setPageFrom(Integer.parseInt(ajaxResponse.getRequestParameter("pagefrom", "-1")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            documentFileParsingOptions.setPageTo(Integer.parseInt(ajaxResponse.getRequestParameter("pageto", "-1")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            documentFileParsingOptions.setTableToExtract(Integer.parseInt(ajaxResponse.getRequestParameter("tabletoextract", "-1")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        documentFileParsingOptions.setMergeColumns(ajaxResponse.getRequestParameter("mergecolumns", ""));
        documentFileParsingOptions.setEndTableText(ajaxResponse.getRequestParameter("endtablete", ""));
        Attachment attachment = new Attachment();
        attachment.setSDCId("LV_AttachmentHandler");
        attachment.setKeyId1(attachmenthandlerid);
        attachment.setAttachmentClass("HandlerExampleFile");
        AttachmentProcessor ap = new AttachmentProcessor(pageContext);
        ap.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.DISABLED);
        Path dir = null;
        try {
            dir = FileUtil.createTempDirectory("pdfparsehelper", true);
        }
        catch (Exception e) {
            this.logger.warn(e.getMessage());
        }
        if (dir != null) {
            try {
                String data2;
                ArrayList<DataSet> tables = new ArrayList<DataSet>();
                try {
                    FileManager.parseDocumentFile(attachment.getInputStream(), FileType.getFileType(attachment.getFilename(), this.getConnectionId()), dir, documentFileParsingOptions, tables, this.getConnectionId());
                }
                catch (Exception e) {
                    this.logger.warn(e.getMessage());
                }
                boolean rendered = false;
                if (documentFileParsingOptions.getExtractTables()) {
                    rendered = true;
                    html.append("<div class=\"tables\">");
                    if (tables.size() > 0) {
                        for (int i = 0; i < tables.size(); ++i) {
                            DataSet table = (DataSet)tables.get(i);
                            html.append("<div class=\"label\">Table ").append(i + 1).append("</div>");
                            html.append("<div class=\"table\">");
                            html.append(table.toHTML());
                            html.append("</div>");
                        }
                    } else {
                        html.append("No tables found.");
                    }
                    html.append("</div>");
                }
                File[] files = dir.toFile().listFiles();
                if (documentFileParsingOptions.getExtractImages()) {
                    rendered = true;
                    html.append("<div class=\"images\">");
                    boolean found = false;
                    for (File file : files) {
                        if (FileTypeGroup.getFileTypeGroupByFileName(file.getName()) != FileTypeGroup.IMAGE) continue;
                        found = true;
                        html.append("<div class=\"label\">Image ").append(file.getName()).append("</div>");
                        html.append("<div class=\"image\">");
                        FileManager.FileData fileData = new FileManager.FileData(file.toPath(), FileType.getFileType(file.getName(), this.getConnectionId()).getMime());
                        html.append("<img src=\"").append(fileData.getDataURL()).append("\">");
                        html.append("</div>");
                    }
                    if (!found) {
                        html.append("No images found.");
                    }
                    html.append("</div>");
                }
                if (documentFileParsingOptions.getExtractText()) {
                    rendered = true;
                    html.append("<div class=\"texts\">");
                    boolean found = false;
                    for (File file : files) {
                        if (!FileType.getFileType(file.getName(), this.getConnectionId()).getName().equals("TXT")) continue;
                        found = true;
                        html.append("<div class=\"label\">Text ").append(file.getName()).append("</div>");
                        html.append("<div class=\"text\">");
                        html.append("<textarea readonly class=\"filecontent\">");
                        try {
                            data2 = new String(Files.readAllBytes(file.toPath()));
                            html.append(data2);
                        }
                        catch (Exception data2) {
                            // empty catch block
                        }
                        html.append("</textarea>");
                        html.append("</div>");
                    }
                    if (!found) {
                        html.append("No text found.");
                    }
                    html.append("</div>");
                }
                if (documentFileParsingOptions.getExtractAsXML()) {
                    rendered = true;
                    html.append("<div class=\"xmls\">");
                    boolean found = false;
                    for (File file : files) {
                        if (!FileType.getFileType(file.getName(), this.getConnectionId()).getName().equals("XML")) continue;
                        found = true;
                        html.append("<div class=\"label\">XML ").append(file.getName()).append("</div>");
                        html.append("<div class=\"xml\">");
                        html.append("<textarea readonly class=\"filecontent\">");
                        try {
                            data2 = new String(Files.readAllBytes(file.toPath()));
                            html.append(data2);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        html.append("</textarea>");
                        html.append("</div>");
                    }
                    if (!found) {
                        html.append("No xml found.");
                    }
                    html.append("</div>");
                }
                if (!rendered) {
                    html.append("<div class=\"msg\">No extraction selected.</div>");
                }
            }
            finally {
                try {
                    Files.deleteIfExists(dir);
                }
                catch (Exception e) {
                    this.logger.warn(e.getMessage());
                }
            }
        }
        html.append("</div>");
    }

    private void save(String attachmenthandlerid, AjaxResponse ajaxResponse, StringBuilder html, StringBuilder script, PageContext pageContext) {
        DataSet vars = this.getQueryProcessor().getPreparedSqlDataSet("SELECT propertyclob FROM attachmenthandler WHERE attachmenthandlerid=?", new Object[]{attachmenthandlerid}, true);
        if (vars != null && vars.size() == 1) {
            PropertyList setupvars = new PropertyList();
            try {
                setupvars = new PropertyList(new JSONObject(vars.getClob(0, "propertyclob", "{}")));
            }
            catch (Exception exception) {
                // empty catch block
            }
            PropertyListCollection variables = setupvars.getCollectionNotNull("variables");
            for (int i = 0; i < variables.size(); ++i) {
                PropertyList variable = variables.getPropertyList(i);
                String varid = variable.getProperty("variableid");
                if (!ajaxResponse.getRequestParameters().containsKey(varid)) continue;
                String val = ajaxResponse.getRequestParameter(varid, "");
                variable.setProperty("value", val);
            }
            this.getQueryProcessor().execPreparedUpdate("update attachmenthandler set propertyclob=? where attachmenthandlerid=?", new Object[]{setupvars.toJSONString(), attachmenthandlerid});
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "getActionPropertiesHandler");
        try {
            String attachmenthandler = ajaxResponse.getRequestParameter("attachmenthandlerid");
            Mode mode = Mode.VARIABLES;
            try {
                mode = Mode.valueOf(ajaxResponse.getRequestParameter("mode", mode.toString()).toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
            StringBuilder html = new StringBuilder();
            StringBuilder script = new StringBuilder();
            switch (mode) {
                case VARIABLES: {
                    this.getVariables(attachmenthandler, html, script, ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    break;
                }
                case ORIGINAL: {
                    this.getOriginal(attachmenthandler, ajaxResponse, html, script, ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    break;
                }
                case PARSE: {
                    this.parse(attachmenthandler, ajaxResponse, html, script, ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    break;
                }
                case SAVE: {
                    this.save(attachmenthandler, ajaxResponse, html, script, ajaxResponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response));
                    break;
                }
            }
            ajaxResponse.addCallbackArgument("html", html.toString());
            ajaxResponse.addCallbackArgument("script", script.toString());
        }
        catch (Exception e) {
            ajaxResponse.setError(e.getMessage());
        }
        finally {
            ajaxResponse.print();
        }
    }

    static enum Mode {
        PARSE,
        VARIABLES,
        ORIGINAL,
        SAVE;

    }
}

