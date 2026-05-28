/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.pdf.DocSaveOptions
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.SaveOptions
 *  com.aspose.words.Document
 *  com.aspose.words.DocumentBuilder
 *  com.aspose.words.Section
 *  org.apache.commons.io.IOUtils
 *  org.jsoup.Jsoup
 *  org.jsoup.nodes.Document
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.aspose.pdf.DocSaveOptions;
import com.aspose.pdf.SaveOptions;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.Section;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.eln.WordWorksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.FileAjaxHandler;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.BasePagedFileDetails;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTypeGroup;
import com.labvantage.sapphire.util.file.PdfFileDetails;
import com.labvantage.sapphire.util.file.WordFileDetails;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class File
extends BaseWorksheetItem {
    public static String attachmentPolicyNode = "Sapphire Custom";

    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setSupportsExport(true);
        worksheetItemOptions.setViewerHTMLasTOC(true);
        int attachmentNum = File.getInt(this.getContentsPL().getProperty("attachment"), 1);
        boolean attachmentMissing = this.attachmentMissing(attachmentNum);
        worksheetItemOptions.setHasExportHTML(this.hasExportedHTML() || attachmentMissing);
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/extscripts/jquery/plugins/jquery.cropper.min.js");
        worksheetItemIncludes.addStyleInclude("WEB-CORE/extscripts/jquery/plugins/jquery.cropper.min.css");
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/file.js");
        worksheetItemIncludes.setJSObjectName("fileEditor");
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getViewHTML(false);
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        String renderStyle = File.getRenderStyle(this.getFileTypeGroup(this.getContentsPL()), this.getConfig(), true);
        if (renderStyle.equalsIgnoreCase("HTML") || renderStyle.equalsIgnoreCase("Image")) {
            return this.getViewHTML(true);
        }
        return "";
    }

    private String getViewHTML(boolean export) {
        StringBuilder html = new StringBuilder();
        try {
            PropertyList contentsPL = this.getContentsPL();
            int attachmentNum = File.getInt(contentsPL.getProperty("attachment"), 1);
            if (this.attachmentMissing(attachmentNum)) {
                if (export) {
                    return "";
                }
                String instructionText = this.getTranslationProcessor().translate("Click Edit to add a file");
                return "<div class=\"worksheet_instructiontext\">" + instructionText + "</div>";
            }
            if (export) {
                this.updateExportFileObject(contentsPL, this.getConfig());
                contentsPL.setProperty("imageborder", "Y");
            }
            html.append(FileAjaxHandler.getViewArea(contentsPL, this.getWorksheetItemId(), this.getWorksheetItemVersionId()));
        }
        catch (Exception e) {
            this.logError("Could not load editor.", e);
        }
        return html.toString();
    }

    private boolean attachmentMissing(int attachmentNum) {
        try {
            return this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM sdiattachment WHERE sdcid=? AND keyid1=? AND keyid2=? AND attachmentnum=?", new Object[]{"LV_WorksheetItem", this.getWorksheetItemId(), this.getWorksheetItemVersionId(), attachmentNum}) == 0;
        }
        catch (SapphireException e) {
            return false;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void addWordContent(WordWorksheet wordWorksheet, Document document, DocumentBuilder builder, PropertyList exportOptions) throws Exception {
        block26: {
            try {
                PropertyList contentsPL = this.getContentsPL();
                PropertyList configPL = this.getConfig();
                int attachmentNum = File.getInt(contentsPL.getProperty("attachment"), 1);
                boolean attachmentMissing = this.attachmentMissing(attachmentNum);
                String renderStyle = attachmentMissing ? "html" : File.getRenderStyle(this.getFileTypeGroup(this.getContentsPL()), this.getConfig(), true);
                String exportTo = this.getExportTo(exportOptions);
                PropertyList markup = contentsPL.getPropertyListNotNull("markup");
                boolean publishAttachment = markup.getProperty("publishattachment").equals("Y");
                String publishAttachmentCaption = markup.getProperty("publishattachmentcaption", "[filename]");
                if (renderStyle.equalsIgnoreCase("image") || renderStyle.equalsIgnoreCase("html") || attachmentMissing) {
                    if (renderStyle.equalsIgnoreCase("html")) {
                        exportOptions.setProperty("resetmargins", "Y");
                    }
                    super.addWordContent(wordWorksheet, document, builder, exportOptions);
                } else {
                    try {
                        FileTypeGroup fileTypeGroup = this.getFileTypeGroup(contentsPL);
                        if (fileTypeGroup.equals(FileTypeGroup.PDF) && exportTo.equalsIgnoreCase("PDF")) {
                            this.writePdfInsertionPlaceholder(wordWorksheet, document, builder, configPL, contentsPL, attachmentNum, this.logger);
                        } else {
                            this.injectIntoWordWorksheet(wordWorksheet, document, builder, configPL, contentsPL, attachmentNum, this.logger);
                        }
                    }
                    catch (Exception e) {
                        builder.insertHtml("<div style=\"color:red\">Injection mode failed. Please try a different render style.</div>", true);
                        publishAttachment = false;
                    }
                }
                if (!publishAttachment) break block26;
                if (publishAttachmentCaption.length() == 0) {
                    publishAttachmentCaption = "[filename]";
                }
                if (exportTo.equalsIgnoreCase("PDF")) {
                    String key = "LV_WorksheetItem;" + this.getWorksheetItemId() + ";" + this.getWorksheetItemVersionId() + ";(null);" + attachmentNum;
                    wordWorksheet.cachePdfCaption(key, publishAttachmentCaption);
                    builder.insertHtml("<br>::ADDATTACHMENT::" + key + "::");
                    break block26;
                }
                AttachmentProcessor arp = new AttachmentProcessor(this.getSapphireConnection().getConnectionId());
                Attachment attachment = arp.getSDIAttachment("LV_WorksheetItem", this.getWorksheetItemId(), this.getWorksheetItemVersionId(), "(null)", attachmentNum);
                if (attachment == null) break block26;
                InputStream inputStream = attachment.getInputStream();
                String extension = FileManager.getExtension(attachment.getFilename());
                java.io.File file = FileUtil.createTempFile("elnexport", "." + extension).toFile();
                try (FileOutputStream out = new FileOutputStream(file);){
                    IOUtils.copy((InputStream)inputStream, (OutputStream)out);
                    publishAttachmentCaption = StringUtil.replaceAll(publishAttachmentCaption, "[filename]", attachment.getSourceFilename());
                    builder.insertOleObjectAsIcon(file.getAbsolutePath(), false, null, publishAttachmentCaption);
                }
                finally {
                    file.delete();
                }
            }
            catch (Exception e) {
                builder.insertHtml("<div style=\"color:red\">Injection mode failed. Please try a publish render style.</div>", true);
                this.logError("Could not write content: " + e.getMessage(), e);
            }
        }
    }

    private PropertyList getContentsPL() {
        try {
            return this.getContents().length() > 0 ? new PropertyList(new JSONObject(this.getContents())) : new PropertyList();
        }
        catch (JSONException e) {
            return new PropertyList();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void injectIntoWordWorksheet(WordWorksheet wordWorksheet, Document document, DocumentBuilder builder, PropertyList config, PropertyList contents, int attachmentNum, Logger logger) throws Exception {
        BasePagedFileDetails fileDetails;
        String worksheetitemid = this.getWorksheetItemId();
        String worksheetitemversionid = this.getWorksheetItemVersionId();
        AttachmentProcessor arp = new AttachmentProcessor(this.getSapphireConnection().getConnectionId());
        Attachment attachment = arp.getSDIAttachment("LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "(null)", attachmentNum);
        if (attachment == null) throw new SapphireException("Could not obtain attachment.");
        byte[] data = attachment.getData();
        if (data == null) throw new SapphireException("Attachment has no data. File may be invalid.");
        FileTypeGroup fileTypeGroup = this.getFileTypeGroup(contents);
        int from = 1;
        int to = 1;
        if (!fileTypeGroup.equals(FileTypeGroup.EXCEL)) {
            int[] range = this.getExportRange(config, contents);
            from = range[0];
            to = range[1];
        }
        if (fileTypeGroup.equals(FileTypeGroup.PDF)) {
            fileDetails = new PdfFileDetails();
            fileDetails.setFromPage(from);
            fileDetails.setToPage(to);
            fileDetails.setMaxAllowed(10000);
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            com.aspose.pdf.Document pdfDocument = FileManager.getPdfDocumentFromBis(bis, (PdfFileDetails)fileDetails, logger);
            DocSaveOptions saveOptions = new DocSaveOptions();
            saveOptions.setFormat(1);
            saveOptions.setRecognizeBullets(true);
            String recognitionMode = config.getPropertyListNotNull("pdffiles").getProperty("recognitionmode", "N");
            if (!recognitionMode.equals("N")) {
                int mode = recognitionMode.equals("E") ? 2 : (recognitionMode.equals("T") ? 0 : 1);
                saveOptions.setMode(mode);
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            pdfDocument.save((OutputStream)os, (SaveOptions)saveOptions);
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            Document fragment = new Document((InputStream)is);
            this.injectFragment(builder, fragment);
            return;
        } else {
            if (!fileTypeGroup.equals(FileTypeGroup.WORD)) return;
            fileDetails = new WordFileDetails();
            fileDetails.setFromPage(from);
            fileDetails.setToPage(to);
            fileDetails.setMaxAllowed(10000);
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            Document fragment = FileManager.getWordDocumentFromBis(bis, (WordFileDetails)fileDetails, logger);
            this.injectFragment(builder, fragment);
        }
    }

    public static String getRenderStyle(FileTypeGroup fileTypeGroup, PropertyList configPL, boolean publish) {
        String renderStyle = "HTML";
        String publishRenderStyle = "S";
        if (fileTypeGroup.equals(FileTypeGroup.PDF)) {
            PropertyList fileProps = configPL.getPropertyListNotNull("pdffiles");
            renderStyle = fileProps.getProperty("renderstyle", "HTML");
            if (publish) {
                publishRenderStyle = fileProps.getProperty("publishrenderstyle", renderStyle);
            }
        } else if (fileTypeGroup.equals(FileTypeGroup.WORD)) {
            PropertyList fileProps = configPL.getPropertyListNotNull("wordfiles");
            renderStyle = fileProps.getProperty("renderstyle", "HTML");
            if (publish) {
                publishRenderStyle = fileProps.getProperty("publishrenderstyle", renderStyle);
            }
        } else if (fileTypeGroup.equals(FileTypeGroup.EXCEL)) {
            PropertyList fileProps = configPL.getPropertyListNotNull("excelfiles");
            renderStyle = fileProps.getProperty("renderstyle", "HTML");
            if (publish) {
                publishRenderStyle = fileProps.getProperty("publishrenderstyle", renderStyle);
            }
        } else if (fileTypeGroup.equals(FileTypeGroup.PPT)) {
            PropertyList fileProps = configPL.getPropertyListNotNull("powerpointfiles");
            renderStyle = fileProps.getProperty("pptrenderstyle", "HTML");
            if (publish) {
                publishRenderStyle = fileProps.getProperty("publishrenderstyle", renderStyle);
            }
        } else if (fileTypeGroup.equals(FileTypeGroup.TXT)) {
            PropertyList fileProps = configPL.getPropertyListNotNull("textfiles");
            renderStyle = fileProps.getProperty("renderstyle", "HTML");
            if (publish) {
                publishRenderStyle = fileProps.getProperty("publishrenderstyle", renderStyle);
            }
        }
        if (publish && !publishRenderStyle.equals("S")) {
            return publishRenderStyle;
        }
        return renderStyle;
    }

    private String getExportTo(PropertyList exportOptions) {
        return exportOptions.getProperty("exportto", "html");
    }

    private void injectFragment(DocumentBuilder builder, Document fragment) {
        for (Section section : fragment.getSections()) {
            section.clearHeadersFooters();
            section.getPageSetup().setRestartPageNumbering(false);
        }
        builder.insertDocument(fragment, 2);
    }

    private void writePdfInsertionPlaceholder(WordWorksheet wordWorksheet, Document document, DocumentBuilder builder, PropertyList configPL, PropertyList contentsPL, int attachmentNum, Logger logger) throws Exception {
        String worksheetitemid = this.getWorksheetItemId();
        String worksheetitemversionid = this.getWorksheetItemVersionId();
        int[] range = this.getExportRange(configPL, contentsPL);
        int from = range[0];
        int to = range[1];
        int pages = 1;
        if (from == -1) {
            byte[] data;
            AttachmentProcessor arp = new AttachmentProcessor(this.getSapphireConnection().getConnectionId());
            Attachment attachment = arp.getSDIAttachment("LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "(null)", attachmentNum);
            if (attachment != null && (data = attachment.getData()) != null) {
                PdfFileDetails pdfFileDetails = new PdfFileDetails();
                pdfFileDetails.setFromPage(-1);
                pdfFileDetails.setToPage(-1);
                pdfFileDetails.setMaxAllowed(10000);
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                com.aspose.pdf.Document nestedPDF = FileManager.getPdfDocumentFromBis(bis, pdfFileDetails, logger);
                pages = nestedPDF.getPages().size();
                String attachmentKey = "LV_WorksheetItem;" + worksheetitemid + ";" + worksheetitemversionid + ";(null);" + attachmentNum;
                wordWorksheet.cachePdfAttachment(attachmentKey, nestedPDF);
            }
        } else {
            pages = to - from;
        }
        builder.insertBreak(1);
        builder.insertHtml("::INJECT_PDFATTACHMENT::LV_WorksheetItem;" + worksheetitemid + ";" + worksheetitemversionid + ";(null);" + attachmentNum + ";" + range[0] + ";" + range[1] + "::");
        builder.insertBreak(1);
        for (int i = 0; i < pages - 2; ++i) {
            builder.insertHtml("::REMOVE_THIS_PAGE::");
            builder.insertBreak(1);
        }
        builder.insertHtml("::REMOVE_THIS_PAGE::");
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        long maxsize;
        StringBuilder html = new StringBuilder();
        PropertyList content = this.getContentsPL();
        try {
            String attNum = content.getProperty("attachment", "");
            if (attNum.length() == 0) {
                html.append(FileAjaxHandler.getUploadArea(this.getElementId()));
            } else {
                html.append(FileAjaxHandler.getEditArea(this.getElementId(), content, this.getWorksheetItemId(), this.getWorksheetItemVersionId(), new Logger(this.logContext), this.config, Configuration.isDevmode(this.getSapphireConnection().getDatabaseId()), this.getTranslationProcessor(), this.getSapphireConnection().getConnectionId()));
            }
        }
        catch (Exception e) {
            this.logError("Could not load editor.", e);
        }
        html.append("<textarea style=\"display:none;\" id=\"").append(this.getElementId()).append("_content\">");
        String json = content.size() > 0 ? content.toJSONString() : "";
        json = StringUtil.replaceAll(json, "&quot;", "!|!QUOT!|!");
        html.append(json);
        html.append("</textarea>");
        ArrayList<FileTypeGroup> excludeTypes = new ArrayList<FileTypeGroup>();
        String filetype = this.config.getProperty("filetype");
        if (filetype.length() > 0 && !filetype.equals("any")) {
            if (!filetype.equals("img")) {
                excludeTypes.add(FileTypeGroup.IMAGE);
            }
            if (!filetype.equals("pdf")) {
                excludeTypes.add(FileTypeGroup.PDF);
            }
            if (!filetype.equals("word")) {
                excludeTypes.add(FileTypeGroup.WORD);
            }
            if (!filetype.equals("excel")) {
                excludeTypes.add(FileTypeGroup.EXCEL);
            }
            if (!filetype.equals("ppt")) {
                excludeTypes.add(FileTypeGroup.PPT);
            }
            if (!filetype.equals("txt")) {
                excludeTypes.add(FileTypeGroup.TXT);
            }
        } else {
            if (!this.config.getPropertyListNotNull("imagefiles").getProperty("allowimage", "Y").equalsIgnoreCase("Y")) {
                excludeTypes.add(FileTypeGroup.IMAGE);
            }
            if (!this.config.getPropertyListNotNull("wordfiles").getProperty("allowword", "Y").equalsIgnoreCase("Y")) {
                excludeTypes.add(FileTypeGroup.WORD);
            }
            if (!this.config.getPropertyListNotNull("excelfiles").getProperty("allowexcel", "Y").equalsIgnoreCase("Y")) {
                excludeTypes.add(FileTypeGroup.EXCEL);
            }
            if (!this.config.getPropertyListNotNull("powerpointfiles").getProperty("allowpowerpoint", "Y").equalsIgnoreCase("Y")) {
                excludeTypes.add(FileTypeGroup.PPT);
            }
            if (!this.config.getPropertyListNotNull("pdffiles").getProperty("allowpdf", "Y").equalsIgnoreCase("Y")) {
                excludeTypes.add(FileTypeGroup.PDF);
            }
            if (!this.config.getPropertyListNotNull("textfiles").getProperty("allowtext", "Y").equalsIgnoreCase("Y")) {
                excludeTypes.add(FileTypeGroup.TXT);
            }
        }
        html.append("<script>");
        html.append("fileEditor.dZMsg='").append(this.getTranslationProcessor().translate("Drag over your file or click to select a file.")).append("';");
        html.append("fileEditor.settings['").append(this.getElementId()).append("'] = {");
        html.append("'fileTypes':");
        html.append("'").append(FileTypeGroup.getPreviewTypes(excludeTypes)).append("'");
        html.append(",");
        html.append("'config':'").append(this.config.toJSONString()).append("'");
        html.append(",");
        html.append("'worksheetid':").append("'").append(this.getWorksheetId()).append("'");
        html.append(",");
        html.append("'worksheetversionid':").append("'").append(this.getWorksheetVersionId()).append("'");
        html.append(",");
        html.append("'worksheetitemid':").append("'").append(this.getWorksheetItemId()).append("'");
        html.append(",");
        html.append("'worksheetversionitemid':").append("'").append(this.getWorksheetItemVersionId()).append("'");
        html.append("};");
        try {
            maxsize = FileManager.getUploadMaxFileSizeMB(attachmentPolicyNode, this.getSapphireConnection().getConnectionId());
        }
        catch (NumberFormatException e) {
            this.logWarn("Invalid max size provided. Default to 5.");
            maxsize = 5L;
        }
        html.append("fileEditor.maxsize=").append(maxsize).append(";");
        html.append("</script>");
        return html.toString();
    }

    @Override
    public String getLiveIndexingText() {
        if (this.hasContents()) {
            StringBuilder html = new StringBuilder();
            try {
                PropertyList contents = this.getContentsPL();
                html.append(FileAjaxHandler.getViewArea(contents, this.getWorksheetItemId(), this.getWorksheetItemVersionId()));
                org.jsoup.nodes.Document jdoc = Jsoup.parse((String)html.toString());
                return jdoc.body().text();
            }
            catch (Exception e) {
                this.logError("Could not load content.", e);
                return "";
            }
        }
        return "";
    }

    @Override
    public String validateContents(String contents) throws SapphireException {
        contents = HttpUtil.decodeURIComponent(contents);
        try {
            PropertyList fileDetails = new PropertyList(new JSONObject(contents));
            boolean fileDetailsUpdated = FileAjaxHandler.generateFileObject(fileDetails, this.logger, this.getWorksheetItemId(), this.getWorksheetItemVersionId(), this.getSapphireConnection().getConnectionId());
            if (fileDetailsUpdated) {
                contents = fileDetails.toJSONString();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return super.validateContents(contents);
    }

    private int[] getExportRange(PropertyList config, PropertyList content) {
        FileTypeGroup fileTypeGroup = this.getFileTypeGroup(content);
        PropertyList controlConfig = null;
        switch (fileTypeGroup) {
            case WORD: {
                controlConfig = config.getPropertyList("wordfiles");
                break;
            }
            case PDF: {
                controlConfig = config.getPropertyList("pdffiles");
                break;
            }
            case PPT: {
                controlConfig = config.getPropertyList("powerpointfiles");
                break;
            }
            case TXT: {
                controlConfig = config.getPropertyList("textfiles");
            }
        }
        int from = 1;
        int to = 1;
        if (controlConfig != null) {
            String exportrule = controlConfig.getProperty("exportrule", "S");
            if (exportrule.equalsIgnoreCase("S")) {
                PropertyList markup = content.getPropertyListNotNull("markup");
                from = File.getInt(markup.getProperty("from", "1"), from);
                to = File.getInt(markup.getProperty("to", "5"), to);
            } else if (exportrule.equalsIgnoreCase("R")) {
                from = File.getInt(controlConfig.getProperty("exportfrom"), from);
                to = File.getInt(controlConfig.getProperty("exportto"), to);
            } else {
                from = -1;
                to = -1;
            }
        }
        return new int[]{from, to};
    }

    private void updateExportFileObject(PropertyList fileDetails, PropertyList config) throws Exception {
        FileTypeGroup fileTypeGroup = this.getFileTypeGroup(fileDetails);
        PropertyList controlCofig = null;
        String maxAllowedPropertyName = null;
        switch (fileTypeGroup) {
            case WORD: {
                controlCofig = this.getConfig().getPropertyList("wordfiles");
                maxAllowedPropertyName = "maxallowedwordpages";
                break;
            }
            case PDF: {
                controlCofig = this.getConfig().getPropertyList("pdffiles");
                maxAllowedPropertyName = "maxallowedpdfpages";
                break;
            }
            case PPT: {
                controlCofig = this.getConfig().getPropertyList("powerpointfiles");
                maxAllowedPropertyName = "maxallowedpptslides";
                break;
            }
            case TXT: {
                controlCofig = this.getConfig().getPropertyList("textfiles");
                maxAllowedPropertyName = "maxallowedtextlines";
            }
        }
        if (fileTypeGroup.equals(FileTypeGroup.EXCEL)) {
            FileAjaxHandler.refreshFileDetails(fileDetails, "LV_WorksheetItem", this.getWorksheetItemId(), this.getWorksheetItemVersionId(), this.getConfig(), this.getSapphireConnection().getConnectionId(), new Logger(this.logContext), true);
        } else if (controlCofig != null) {
            String fileRenderStyle;
            boolean forceRefresh = fileTypeGroup.equals(FileTypeGroup.WORD) || fileTypeGroup.equals(FileTypeGroup.PDF);
            String exportrule = controlCofig.getProperty("exportrule", "S");
            String renderStyle = File.getRenderStyle(fileTypeGroup, config, true);
            String string = fileRenderStyle = fileDetails.getProperty("display").startsWith("<img") ? "Image" : "HTML";
            if (forceRefresh || !exportrule.equalsIgnoreCase("S") || !renderStyle.equals(fileRenderStyle)) {
                FileAjaxHandler.refreshFileDetails(fileDetails, "LV_WorksheetItem", this.getWorksheetItemId(), this.getWorksheetItemVersionId(), this.getConfig(), this.getSapphireConnection().getConnectionId(), new Logger(this.logContext), true);
            }
        }
    }

    private boolean hasExportedHTML() {
        String exportrule;
        boolean hasExportedHTML = false;
        PropertyList contentsPL = this.getContentsPL();
        if (contentsPL.size() == 0) {
            return false;
        }
        String renderStyle = File.getRenderStyle(this.getFileTypeGroup(contentsPL), this.getConfig(), true);
        if (renderStyle.equalsIgnoreCase("I")) {
            return false;
        }
        PropertyList controlCofig = null;
        FileTypeGroup fileTypeGroup = this.getFileTypeGroup(new PropertyList(new JSONObject(contentsPL)));
        if (fileTypeGroup.equals(FileTypeGroup.WORD)) {
            return true;
        }
        switch (fileTypeGroup) {
            case WORD: {
                controlCofig = this.getConfig().getPropertyList("wordfiles");
                break;
            }
            case PDF: {
                controlCofig = this.getConfig().getPropertyList("pdffiles");
                break;
            }
            case PPT: {
                controlCofig = this.getConfig().getPropertyList("powerpointfiles");
                break;
            }
            case TXT: {
                controlCofig = this.getConfig().getPropertyList("textfiles");
            }
        }
        if (controlCofig != null && !(exportrule = controlCofig.getProperty("exportrule", "S")).equalsIgnoreCase("S")) {
            hasExportedHTML = true;
        }
        return hasExportedHTML;
    }

    private FileTypeGroup getFileTypeGroup(PropertyList contents) {
        return FileTypeGroup.valueOf(contents.getProperty("filetype", "UNKNOWN"));
    }
}

