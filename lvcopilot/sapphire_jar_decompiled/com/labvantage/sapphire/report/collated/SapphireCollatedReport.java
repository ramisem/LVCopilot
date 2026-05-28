/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.IDocument
 *  com.aspose.pdf.facades.PdfFileEditor
 *  com.aspose.pdf.facades.PdfViewer
 *  com.aspose.pdf.printing.PdfPrinterSettings
 *  com.aspose.pdf.printing.PrintPageSettings
 *  com.aspose.pdf.printing.PrintPaperSize
 *  com.aspose.pdf.printing.PrinterMargins
 *  com.aspose.words.Document
 *  com.aspose.words.DocumentBuilder
 *  com.aspose.words.PageSetup
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  net.sf.jasperreports.engine.JasperReport
 */
package com.labvantage.sapphire.report.collated;

import com.aspose.pdf.IDocument;
import com.aspose.pdf.facades.PdfFileEditor;
import com.aspose.pdf.facades.PdfViewer;
import com.aspose.pdf.printing.PdfPrinterSettings;
import com.aspose.pdf.printing.PrintPageSettings;
import com.aspose.pdf.printing.PrintPaperSize;
import com.aspose.pdf.printing.PrinterMargins;
import com.aspose.words.Document;
import com.aspose.words.DocumentBuilder;
import com.aspose.words.PageSetup;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.collated.SapphireCollatedUtil;
import com.labvantage.sapphire.report.jasper.CommonParamMap;
import com.labvantage.sapphire.report.jasper.SapphireJasperReport;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperReport;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SapphireCollatedReport
extends SapphireReport {
    private Map<String, String> replaceMapForTitlePage = new HashMap<String, String>();
    private Map<String, String> replaceMapForSeparatorPage = new HashMap<String, String>();
    private M18NUtil m18NUtil;
    DataSet allreportItems = new DataSet();
    List<String> reportitemList = new ArrayList<String>();
    Set<String> attachmentSet = new HashSet<String>();

    public SapphireCollatedReport(String reporttypeflag) {
        super(reporttypeflag);
    }

    @Override
    public void runReportToWeb(HashMap paramMap, String displaytype, HttpServletRequest request, HttpServletResponse response, String fileName, boolean isFile) throws SapphireException {
        SDIData reportDetail = this.getReportDetails();
        this.allreportItems = reportDetail.getDataset("reportitem");
        this.populateReportItem(this.getMapFromRequest(request), this.allreportItems);
        String message = SapphireCollatedUtil.validateParentReport(this.getReportid(), reportDetail.getDataset("reportparam"), this.getMapFromRequest(request));
        if (message.isEmpty()) {
            message = SapphireCollatedUtil.validateReportGeneration(this.allreportItems, reportDetail.getDataset("reportparam"), reportDetail.getDataset("reportparammap"), this.getMapFromRequest(request), this.connectionInfo.getConnectionId(), this.reportitemList);
        }
        if (OpalUtil.isNotEmpty(message)) {
            try {
                ServletOutputStream ouputStream = response.getOutputStream();
                ouputStream.print(message);
                ouputStream.flush();
                ouputStream.close();
            }
            catch (Exception exp) {
                throw new SapphireException("Error running report:" + ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), exp);
            }
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            SapphireReportEvent reportevent;
            LinkedHashMap<String, Integer> pageCount = new LinkedHashMap<String, Integer>();
            Integer lastPageCount = 0;
            ArrayList<String> reportids = new ArrayList<String>();
            switch (displaytype.toLowerCase()) {
                case "pdf": {
                    com.aspose.pdf.Document collatedReport = this.getCollatedReportAsPDF(paramMap, displaytype, request, isFile, this.languageid, pageCount, lastPageCount, reportids);
                    collatedReport.save((OutputStream)byteArrayOutputStream);
                    SapphireJasperUtil.runReportToWebPdf(byteArrayOutputStream.toByteArray(), response, this.getReportid(), this.connectionInfo, fileName, isFile);
                    break;
                }
                case "docx": {
                    Document collatedDocument = this.getCollatedReportAsDOCX(paramMap, displaytype, request, isFile, this.languageid, pageCount, lastPageCount);
                    collatedDocument.save((OutputStream)byteArrayOutputStream, 20);
                    SapphireJasperUtil.runReportToWebWord(byteArrayOutputStream.toByteArray(), response, this.getReportid(), this.connectionInfo, fileName, isFile, displaytype);
                    break;
                }
            }
            if (this.isControlledReport() && (reportevent = this.runReportToWebEvent(paramMap, this.displayType, response, isFile)) != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
                byteArrayInputStream.close();
            }
        }
        catch (Exception exp) {
            throw new SapphireException("Error running report:" + ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), exp);
        }
    }

    @Override
    public void sendReportToFile(HashMap paramMap, String displaytype, String fileName) throws SapphireException {
        File file = new File(fileName);
        SDIData reportDetail = this.getReportDetails();
        this.allreportItems = reportDetail.getDataset("reportitem");
        this.populateReportItem(paramMap, this.allreportItems);
        String message = SapphireCollatedUtil.validateParentReport(this.getReportid(), reportDetail.getDataset("reportparam"), paramMap);
        if (message.isEmpty()) {
            message = SapphireCollatedUtil.validateReportGeneration(this.allreportItems, reportDetail.getDataset("reportparam"), reportDetail.getDataset("reportparammap"), paramMap, this.connectionInfo.getConnectionId(), this.reportitemList);
        }
        if (OpalUtil.isNotEmpty(message)) {
            throw new SapphireException("Error running report:" + message);
        }
        try (FileOutputStream fos = new FileOutputStream(file);){
            SapphireReportEvent reportevent;
            LinkedHashMap<String, Integer> pageCount = new LinkedHashMap<String, Integer>();
            int lastPageCount = 0;
            ArrayList<String> reportids = new ArrayList<String>();
            switch (displaytype.toLowerCase()) {
                case "pdf": {
                    com.aspose.pdf.Document collatedReport = this.getCollatedReportAsPDF(paramMap, displaytype, null, this.isFile, this.languageid, pageCount, lastPageCount, reportids);
                    collatedReport.save((OutputStream)fos);
                    break;
                }
                case "docx": {
                    Document collatedDocument = this.getCollatedReportAsDOCX(paramMap, displaytype, null, this.isFile, this.languageid, pageCount, lastPageCount);
                    collatedDocument.save((OutputStream)fos, 20);
                    break;
                }
            }
            if (this.isControlledReport() && (reportevent = this.sendReportToFileEvent(paramMap, this.displayType, fileName)) != null) {
                byte[] reportByte = Files.readAllBytes(file.toPath());
                if (this.displayType.equalsIgnoreCase("pdf") && !reportevent.isDigitallysigned().equalsIgnoreCase("Y") && this.signingmode != null && (this.signingmode.equals("Automatic") || this.signingmode.equals("With Report Confirmation") && this.initialdisposition != null && this.initialdisposition.equals("Confirmed"))) {
                    this.signReport(fileName);
                    reportByte = Files.readAllBytes(new File(fileName).toPath());
                    reportevent.setDigitallysigned("Y");
                }
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportByte);
                reportevent.setReportStream(byteArrayInputStream);
                reportevent.saveEvent(this.sapphireConnection);
                byteArrayInputStream.close();
            }
        }
        catch (Exception exp) {
            throw new SapphireException("Error running report:" + ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), exp);
        }
    }

    @Override
    public void sendReportToEmail(HashMap paramMap, String displaytype, String emailfrom, String emailtolist, String emailcclist, String emailsubject, String emailmessage, String fileName) throws SapphireException {
        String ext = "";
        if (OpalUtil.isEmpty(displaytype)) {
            this.displayType = "pdf";
            ext = ".pdf";
        } else {
            ext = displaytype.equals("docx") ? ".docx" : "." + displaytype;
        }
        String prefix = OpalUtil.isNotEmpty(fileName) ? (String)paramMap.get("SAPPHIRE_ReportID") : this.resolvedFileName(fileName);
        PropertyList actionProps = new PropertyList();
        String logicalFileName = prefix + ext;
        File file = null;
        try {
            file = FileUtil.createTempFile(prefix, ext).toFile();
        }
        catch (IOException e) {
            throw new SapphireException("Error running report:" + e);
        }
        try (FileOutputStream fos = new FileOutputStream(file);){
            SapphireReportEvent reportevent;
            SDIData reportDetail = this.getReportDetails();
            this.allreportItems = reportDetail.getDataset("reportitem");
            this.populateReportItem(paramMap, this.allreportItems);
            String message = SapphireCollatedUtil.validateParentReport(this.getReportid(), reportDetail.getDataset("reportparam"), paramMap);
            if (message.isEmpty()) {
                message = SapphireCollatedUtil.validateReportGeneration(this.allreportItems, reportDetail.getDataset("reportparam"), reportDetail.getDataset("reportparammap"), paramMap, this.connectionInfo.getConnectionId(), this.reportitemList);
            }
            if (OpalUtil.isNotEmpty(message)) {
                throw new SapphireException("Error running report:" + message);
            }
            LinkedHashMap<String, Integer> pageCount = new LinkedHashMap<String, Integer>();
            int lastPageCount = 0;
            ArrayList<String> reportids = new ArrayList<String>();
            switch (displaytype.toLowerCase()) {
                case "pdf": {
                    com.aspose.pdf.Document collatedReport = this.getCollatedReportAsPDF(paramMap, displaytype, null, this.isFile, this.languageid, pageCount, lastPageCount, reportids);
                    collatedReport.save((OutputStream)fos);
                    break;
                }
                case "docx": {
                    Document collatedDocument = this.getCollatedReportAsDOCX(paramMap, displaytype, null, this.isFile, this.languageid, pageCount, lastPageCount);
                    collatedDocument.save((OutputStream)fos, 20);
                    break;
                }
            }
            actionProps.put("from", emailfrom);
            actionProps.put("to", emailtolist);
            actionProps.put("cc", emailcclist);
            actionProps.put("subject", emailsubject);
            actionProps.put("message", emailmessage);
            actionProps.put("filename", file.getPath());
            actionProps.put("logicalfilename", logicalFileName);
            ActionService ac = new ActionService(this.sapphireConnection);
            ac.processAction("SendMail", "1", actionProps);
            fos.close();
            if (this.isControlledReport() && (reportevent = this.sendReportToEmailEvent(paramMap, this.displayType, emailfrom, emailtolist, emailcclist, emailsubject, emailmessage)) != null) {
                byte[] reportByte = Files.readAllBytes(file.toPath());
                if (this.displayType.equalsIgnoreCase("pdf") && !reportevent.isDigitallysigned().equalsIgnoreCase("Y") && this.signingmode != null && (this.signingmode.equals("Automatic") || this.signingmode.equals("With Report Confirmation") && this.initialdisposition != null && this.initialdisposition.equals("Confirmed"))) {
                    this.signReport(fileName);
                    reportByte = Files.readAllBytes(new File(fileName).toPath());
                    reportevent.setDigitallysigned("Y");
                }
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportByte);
                reportevent.setReportStream(byteArrayInputStream);
                reportevent.saveEvent(this.sapphireConnection);
                byteArrayInputStream.close();
            }
        }
        catch (Exception exp) {
            throw new SapphireException("Error running report:" + ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), exp);
        }
    }

    @Override
    public void sendReportToPrinter(HashMap paramMap, String displaytype, String printername, String addressid, String addresstype) throws SapphireException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            SDIData reportDetail = this.getReportDetails();
            this.allreportItems = reportDetail.getDataset("reportitem");
            this.populateReportItem(paramMap, this.allreportItems);
            String message = SapphireCollatedUtil.validateParentReport(this.getReportid(), reportDetail.getDataset("reportparam"), paramMap);
            if (message.isEmpty()) {
                message = SapphireCollatedUtil.validateReportGeneration(this.allreportItems, reportDetail.getDataset("reportparam"), reportDetail.getDataset("reportparammap"), paramMap, this.connectionInfo.getConnectionId(), this.reportitemList);
            }
            if (OpalUtil.isNotEmpty(message)) {
                throw new SapphireException("Error running report:" + message);
            }
            LinkedHashMap<String, Integer> pageCount = new LinkedHashMap<String, Integer>();
            int lastPageCount = 0;
            ArrayList<String> reportids = new ArrayList<String>();
            com.aspose.pdf.Document collatedReport = this.getCollatedReportAsPDF(paramMap, "pdf", null, this.isFile, this.languageid, pageCount, lastPageCount, reportids);
            this.printDocument(printername, collatedReport);
            collatedReport.save((OutputStream)byteArrayOutputStream);
            if (this.isControlledReport()) {
                byte[] reportByte = byteArrayOutputStream.toByteArray();
                SapphireReportEvent reportevent = this.sendReportToPrinterEvent(paramMap, this.displayType, printername, addressid, addresstype);
                if (reportevent != null) {
                    if (this.displayType.equalsIgnoreCase("pdf") && !reportevent.isDigitallysigned().equalsIgnoreCase("Y") && this.signingmode != null && (this.signingmode.equals("Automatic") || this.signingmode.equals("With Report Confirmation") && this.initialdisposition != null && this.initialdisposition.equals("Confirmed"))) {
                        reportByte = this.signReport(reportByte);
                        reportevent.setDigitallysigned("Y");
                    }
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportByte);
                    reportevent.setReportStream(byteArrayInputStream);
                    reportevent.saveEvent(this.sapphireConnection);
                    byteArrayInputStream.close();
                }
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to print Report:" + e.getMessage());
        }
    }

    private void printDocument(String printername, com.aspose.pdf.Document file) {
        PdfViewer viewer = new PdfViewer();
        viewer.bindPdf((IDocument)file);
        viewer.setAutoResize(true);
        viewer.setAutoRotate(true);
        viewer.setPrintPageDialog(false);
        PdfPrinterSettings printerSettings = new PdfPrinterSettings();
        PrintPageSettings pageSettings = new PrintPageSettings();
        printerSettings.setPrinterName(printername);
        pageSettings.setPaperSize(new PrintPaperSize("A4", 827, 1169));
        pageSettings.setMargins(new PrinterMargins(0, 0, 0, 0));
        viewer.printDocumentWithSettings(pageSettings, printerSettings);
        viewer.close();
    }

    private Document getCollatedReportAsDOCX(HashMap paramMap, String displaytype, HttpServletRequest request, boolean isFile, String languageid, Map<String, Integer> pageCount, Integer lastPageCount) throws Exception {
        Document collatedDocument = null;
        TranslationProcessor tp = new TranslationProcessor(this.connectionInfo.getConnectionId());
        tp.setLanguage(paramMap.get("SAPPHIRE_REPORT_LANGUAGE") != null ? paramMap.get("SAPPHIRE_REPORT_LANGUAGE").toString() : this.connectionInfo.getLanguage());
        if (OpalUtil.isNotEmpty(this.getCollatedReportTitle())) {
            if (request != null) {
                paramMap.putAll(this.getMapFromRequest(request));
            }
            collatedDocument = this.createTitlePage(paramMap);
            pageCount.put(this.getReportid() + "#" + this.getReportdesc(), lastPageCount);
        } else {
            pageCount.put(this.getReportid() + "#" + this.getReportdesc(), 1);
        }
        Document childReport = null;
        for (int i = 0; i < this.allreportItems.getRowCount(); ++i) {
            if (!this.reportitemList.contains(this.allreportItems.getString(i, "reportitemid"))) continue;
            if (this.allreportItems.getString(i, "itemtype").equalsIgnoreCase("Report")) {
                Map<String, ChildItem> childReportInfo = this.getDocuments(this.allreportItems, i, paramMap, displaytype, request, isFile, languageid);
                childReport = this.getReportAsDOCX(paramMap, pageCount, lastPageCount, childReport, childReportInfo);
            }
            if (!this.allreportItems.getString(i, "itemtype").equalsIgnoreCase("Attachment")) continue;
            Map<String, ChildItem> attachedELN = this.getAttachmentReport(this.allreportItems, i, this.getMapFromRequest(request), "docx");
            childReport = this.getReportAsDOCX(paramMap, pageCount, lastPageCount, childReport, attachedELN);
        }
        if (childReport != null) {
            Document docWithTOC = childReport.deepClone();
            if (this.isIncludeTOCFlag()) {
                DocumentBuilder tocbuilder = new DocumentBuilder(docWithTOC);
                tocbuilder.getParagraphFormat().setStyleIdentifier(5);
                tocbuilder.getCurrentParagraph().getParagraphFormat().setAlignment(1);
                tocbuilder.writeln(tp.translate("Table of Contents"));
                tocbuilder.getCurrentParagraph().getParagraphFormat().setAlignment(0);
                this.createTableOfContents(tocbuilder);
                tocbuilder.insertBreak(1);
                docWithTOC.updateFields();
            }
            if (collatedDocument != null) {
                collatedDocument.appendDocument(docWithTOC, 1);
            } else {
                collatedDocument = docWithTOC.deepClone();
            }
        }
        SapphireCollatedUtil.replaceTextWithImageForWord(collatedDocument, "[report.logo]", SapphireCollatedUtil.getLogo(request, this.connectionInfo.getConnectionId(), this.getAddressid()));
        return collatedDocument;
    }

    private Document getReportAsDOCX(Map paranMap, Map<String, Integer> pageCount, Integer lastPageCount, Document childReport, Map<String, ChildItem> childReportInfo) throws Exception {
        for (Map.Entry<String, ChildItem> item : childReportInfo.entrySet()) {
            Document doc = null;
            if (item.getValue().getAttachmentType().equalsIgnoreCase(FileType.NamedType.IMAGE.getName())) {
                doc = new Document();
                DocumentBuilder builder = new DocumentBuilder(doc);
                PageSetup ps = builder.getPageSetup();
                ps.setPaperSize(1);
                builder.insertImage(item.getValue().getItemByte());
            } else {
                doc = new Document((InputStream)new ByteArrayInputStream(item.getValue().getItemByte()));
            }
            if (childReport == null) {
                if (OpalUtil.isNotEmpty(this.getChildReportTitle())) {
                    childReport = this.createTitleForChildReport(paranMap, item.getValue());
                    childReport.appendDocument(doc, 1);
                    lastPageCount = lastPageCount + 1;
                    pageCount.put(item.getValue().getItemName() + "#" + item.getValue().getItemDescription(), lastPageCount);
                    continue;
                }
                childReport = (Document)doc.deepClone(true);
                lastPageCount = lastPageCount + doc.getPageCount();
                continue;
            }
            if (OpalUtil.isNotEmpty(this.getChildReportTitle())) {
                childReport.appendDocument(this.createTitleForChildReport(paranMap, item.getValue()), 1);
                childReport.appendDocument(doc, 1);
                lastPageCount = lastPageCount + 1;
                pageCount.put(item.getValue().getItemName() + "#" + item.getValue().getItemDescription(), lastPageCount);
                continue;
            }
            childReport.appendDocument(doc, 1);
            lastPageCount = lastPageCount + doc.getPageCount();
        }
        return childReport;
    }

    private com.aspose.pdf.Document getCollatedReportAsPDF(HashMap paramMap, String displaytype, HttpServletRequest request, boolean isFile, String languageid, Map<String, Integer> pageCount, Integer lastPageCount, List<String> reportids) throws Exception {
        TranslationProcessor tp = new TranslationProcessor(this.connectionInfo.getConnectionId());
        tp.setLanguage(paramMap.get("SAPPHIRE_REPORT_LANGUAGE") != null ? paramMap.get("SAPPHIRE_REPORT_LANGUAGE").toString() : this.connectionInfo.getLanguage());
        ArrayList<com.aspose.pdf.Document> listOfDocs = new ArrayList<com.aspose.pdf.Document>();
        com.aspose.pdf.Document collatedReport = null;
        HashMap<String, String> reportParameter = new HashMap<String, String>();
        reportParameter.putAll(paramMap);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            if (OpalUtil.isNotEmpty(this.getCollatedReportTitle())) {
                if (request != null) {
                    reportParameter.putAll(this.getMapFromRequest(request));
                }
                Document titleWordDoc = this.createTitlePage(reportParameter);
                titleWordDoc.save((OutputStream)byteArrayOutputStream, 40);
                com.aspose.pdf.Document titleDoc = new com.aspose.pdf.Document(byteArrayOutputStream.toByteArray());
                listOfDocs.add(titleDoc);
                lastPageCount = 1;
                pageCount.put(this.getReportid() + "#" + this.getReportdesc(), lastPageCount);
            } else {
                pageCount.put(this.getReportid() + "#" + this.getReportdesc(), 1);
            }
            for (int i = 0; i < this.allreportItems.getRowCount(); ++i) {
                if (!this.reportitemList.contains(this.allreportItems.getString(i, "reportitemid"))) continue;
                if (this.allreportItems.getString(i, "itemtype").equalsIgnoreCase("Report")) {
                    Map<String, ChildItem> childReportInfo = this.getDocuments(this.allreportItems, i, reportParameter, displaytype, request, isFile, languageid);
                    lastPageCount = this.workonChildDocPDF(reportParameter, pageCount, lastPageCount, reportids, listOfDocs, childReportInfo);
                }
                if (!this.allreportItems.getString(i, "itemtype").equalsIgnoreCase("Attachment")) continue;
                Map<String, ChildItem> attachedELN = this.getAttachmentReport(this.allreportItems, i, reportParameter, "pdf");
                lastPageCount = this.workonChildDocPDF(reportParameter, pageCount, lastPageCount, reportids, listOfDocs, attachedELN);
            }
            com.aspose.pdf.Document[] collatedDoc = new com.aspose.pdf.Document[listOfDocs.size()];
            for (int count = 0; count < listOfDocs.size(); ++count) {
                collatedDoc[count] = (com.aspose.pdf.Document)listOfDocs.get(count);
            }
            PdfFileEditor pdfEditor = new PdfFileEditor();
            collatedReport = new com.aspose.pdf.Document();
            pdfEditor.concatenate((IDocument[])collatedDoc, (IDocument)collatedReport);
            SapphireCollatedUtil.addBookmarks(this.getReportid(), this.getReportdesc(), collatedReport, pageCount);
            if (this.isIncludeTOCFlag()) {
                SapphireCollatedUtil.createTOCPage(collatedReport, pageCount, this.getReportid(), this.getCollatedReportTitle(), tp);
            }
            SapphireCollatedUtil.setPageNumber(collatedReport, this.getCollatedReportTitle(), tp);
            SapphireCollatedUtil.replaceTextWithImageForPDF(collatedReport, "[report.logo]", SapphireCollatedUtil.getLogo(request, this.connectionInfo.getConnectionId(), this.getAddressid()));
            com.aspose.pdf.Document document = collatedReport;
            return document;
        }
    }

    private Integer workonChildDocPDF(Map paramMap, Map<String, Integer> pageCount, int lastPageCount, List<String> reportids, List<com.aspose.pdf.Document> listOfDocs, Map<String, ChildItem> childDocMap) throws Exception {
        for (Map.Entry<String, ChildItem> item : childDocMap.entrySet()) {
            ByteArrayOutputStream titleDocForChildbyteArrayOutputStream = new ByteArrayOutputStream();
            Throwable throwable = null;
            try {
                if (OpalUtil.isNotEmpty(this.getChildReportTitle())) {
                    Document titleDocForChild = this.createTitleForChildReport(paramMap, item.getValue());
                    titleDocForChild.save((OutputStream)titleDocForChildbyteArrayOutputStream, 40);
                    com.aspose.pdf.Document titlepdf = new com.aspose.pdf.Document(titleDocForChildbyteArrayOutputStream.toByteArray());
                    ++lastPageCount;
                    listOfDocs.add(titlepdf);
                    titleDocForChildbyteArrayOutputStream.close();
                }
                com.aspose.pdf.Document childpdf = null;
                if (item.getValue().getAttachmentType().equalsIgnoreCase(FileType.NamedType.IMAGE.getName())) {
                    Document doc = new Document();
                    DocumentBuilder builder = new DocumentBuilder(doc);
                    PageSetup ps = builder.getPageSetup();
                    ps.setPaperSize(1);
                    builder.insertImage(item.getValue().getItemByte());
                    ByteArrayOutputStream byteArrayOutputStreamForImage = new ByteArrayOutputStream();
                    doc.save((OutputStream)byteArrayOutputStreamForImage, 40);
                    childpdf = new com.aspose.pdf.Document(byteArrayOutputStreamForImage.toByteArray());
                    byteArrayOutputStreamForImage.close();
                } else {
                    childpdf = new com.aspose.pdf.Document(item.getValue().getItemByte());
                }
                if (item.getValue().itemType.equalsIgnoreCase("Attachment")) {
                    childpdf.getOutlines().delete();
                }
                if (OpalUtil.isNotEmpty(this.getChildReportTitle())) {
                    pageCount.put(item.getValue().getItemName() + "#" + item.getValue().getItemDescription() + "#" + item.getValue().getAttachmentClass(), lastPageCount);
                } else {
                    pageCount.put(item.getValue().getItemName() + "#" + item.getValue().getItemDescription() + "#" + item.getValue().getAttachmentClass(), lastPageCount + 1);
                }
                listOfDocs.add(childpdf);
                lastPageCount += childpdf.getPages().size();
                reportids.add(item.getKey());
            }
            catch (Throwable throwable2) {
                throwable = throwable2;
                throw throwable2;
            }
            finally {
                if (titleDocForChildbyteArrayOutputStream == null) continue;
                if (throwable != null) {
                    try {
                        titleDocForChildbyteArrayOutputStream.close();
                    }
                    catch (Throwable throwable3) {
                        throwable.addSuppressed(throwable3);
                    }
                    continue;
                }
                titleDocForChildbyteArrayOutputStream.close();
            }
        }
        return lastPageCount;
    }

    private Map<String, ChildItem> getAttachmentReport(DataSet elnAttachments, int row, HashMap paramMap, String format) throws SapphireException {
        AttachmentProcessor ap = new AttachmentProcessor(this.connectionInfo.getConnectionId());
        LinkedHashMap<String, ChildItem> childReportInfo = new LinkedHashMap<String, ChildItem>();
        String attachmentsdc = elnAttachments.getString(row, "attachmentsdcid");
        String attachmentkeyid1 = elnAttachments.getString(row, "attachmentkeyid1");
        String[] tokens = StringUtil.getTokens(attachmentkeyid1, "[", "]");
        String selectedValues = "";
        ArrayList<String> keyid1List = new ArrayList<String>();
        ArrayList<String> keyid2List = new ArrayList<String>();
        ArrayList<String> keyid3List = new ArrayList<String>();
        if (tokens.length > 0) {
            selectedValues = (String)paramMap.get(tokens[0]);
            if (OpalUtil.isNotEmpty(selectedValues)) {
                String[] selectedAttachmentsArray = selectedValues.split(";");
                for (int i = 0; i < selectedAttachmentsArray.length; ++i) {
                    String[] keyids = StringUtil.split(selectedAttachmentsArray[i], "|");
                    if (OpalUtil.isNotEmpty(keyids[0])) {
                        keyid1List.add(keyids[0]);
                    }
                    if (keyids.length >= 2) {
                        keyid2List.add(keyids[1]);
                    }
                    if (keyids.length != 3) continue;
                    keyid3List.add(keyids[2]);
                }
            }
        } else {
            keyid1List.addAll(Arrays.asList(elnAttachments.getString(row, "attachmentkeyid1").split(";")));
            if (OpalUtil.isNotEmpty(elnAttachments.getString(row, "attachmentkeyid2"))) {
                keyid2List.addAll(Arrays.asList(elnAttachments.getString(row, "attachmentkeyid2").split(";")));
            }
            if (OpalUtil.isNotEmpty(elnAttachments.getString(row, "attachmentkeyid3"))) {
                keyid3List.addAll(Arrays.asList(elnAttachments.getString(row, "attachmentkeyid3").split(";")));
            }
        }
        for (int keyidcount = 0; keyidcount < keyid1List.size(); ++keyidcount) {
            String keyid1 = (String)keyid1List.get(keyidcount);
            String keyid2 = OpalUtil.isNotEmpty(keyid2List) ? (String)keyid2List.get(keyidcount) : "";
            String keyid3 = OpalUtil.isNotEmpty(keyid3List) ? (String)keyid3List.get(keyidcount) : "";
            String attachmentclass = SapphireCollatedUtil.parseExpression(elnAttachments.getString(row, "attachmentclass"), paramMap);
            String attachmntInfo = keyid1 + "%3B" + keyid2 + "%3B" + keyid3 + "%3B" + attachmentclass;
            if (this.attachmentSet.contains(attachmntInfo)) continue;
            this.attachmentSet.add(attachmntInfo);
            DataSet attchmentNumDS = this.getAttachmentNumbers(attachmentsdc, keyid1, keyid2, keyid3, attachmentclass);
            if (!OpalUtil.isNotEmpty(attchmentNumDS)) continue;
            for (int atcount = 0; atcount < attchmentNumDS.getRowCount(); ++atcount) {
                Integer attachmentNum = Integer.valueOf(attchmentNumDS.getValue(atcount, "attachmentnum"));
                Attachment elnAttachment = ap.getSDIAttachment(attachmentsdc, keyid1, keyid2, keyid3, attachmentNum);
                if (elnAttachment == null) continue;
                byte[] attachmentByte = elnAttachment.getData();
                FileType.NamedType fileType = FileType.getFileType(elnAttachment.getFilename(), this.connectionInfo.getConnectionId()).getType();
                if (format.equalsIgnoreCase("pdf") && !fileType.equals((Object)FileType.NamedType.ADOBE) && !fileType.equals((Object)FileType.NamedType.IMAGE) || format.equalsIgnoreCase("docx") && !fileType.equals((Object)FileType.NamedType.WORD) && !fileType.equals((Object)FileType.NamedType.IMAGE)) continue;
                ChildItem childItem = new ChildItem(elnAttachments.getString(row, "reportitemid"), "", "", "Attachment", attachmentsdc + "-" + keyid1 + (OpalUtil.isNotEmpty(keyid2) ? "_" + keyid2 : "") + (OpalUtil.isNotEmpty(keyid3) ? "_" + keyid3 : ""), elnAttachment.getDescription(), attachmentByte, fileType.getName(), attchmentNumDS.getString(atcount, "createby"), attchmentNumDS.getString(atcount, "sysuserdesc"), this.m18NUtil.format(attchmentNumDS.getCalendar(atcount, "createdt")), keyid1 + (OpalUtil.isNotEmpty(keyid2) ? "|" + keyid2 : "") + (OpalUtil.isNotEmpty(keyid3) ? "|" + keyid3 : ""), attachmentclass);
                childReportInfo.put(elnAttachment.getDescription(), childItem);
            }
        }
        return childReportInfo;
    }

    public void createTableOfContents(DocumentBuilder builder) throws Exception {
        builder.insertTableOfContents("\\o \"1-4\" \\t \"Caption,3\" \\h \\z \\u");
        builder.getParagraphFormat().setStyleIdentifier(1);
    }

    private Document createTitlePage(Map paramMap) throws Exception {
        HashMap<String, Object> argumentlist = new HashMap<String, Object>();
        StringBuilder arg = new StringBuilder();
        this.populateArgumentsInfo(null, paramMap, argumentlist, arg, "Title");
        File f = new File(SapphireCollatedReport.substituteConfigurationPaths(this.getCollatedReportTitle()));
        Document titleDoc = new Document(f.getAbsolutePath());
        this.populateMapForTitlePage(this.getReportid(), OpalUtil.isNotEmpty(this.getReportdesc()) ? this.getReportdesc() : "", arg.toString(), argumentlist);
        SapphireCollatedUtil.replaceTokens(this.replaceMapForTitlePage, titleDoc);
        return titleDoc;
    }

    private void populateArgumentsInfo(ChildItem item, Map paramMap, Map<String, Object> argumentlist, StringBuilder arg, String page) {
        SDIData reportDetail = this.getReportDetails();
        DataSet reportParam = null;
        if (page.equalsIgnoreCase("Title")) {
            reportParam = reportDetail.getDataset("reportparam");
            this.populateArgumentsInfo(reportParam, paramMap, argumentlist, arg);
        } else if (item.getItemType().equalsIgnoreCase("Report")) {
            reportParam = this.getReportParam(item.getChildreportid(), item.getChildreportversionid());
            this.populateArgumentsInfo(reportParam, paramMap, argumentlist, arg);
        } else {
            arg.append("Keyid: " + item.getItemKeyid());
            argumentlist.put("Keyid", item.getItemKeyid());
        }
    }

    private void populateArgumentsInfo(DataSet reportParam, Map paramMap, Map<String, Object> argumentlist, StringBuilder arg) {
        for (int i = 0; i < reportParam.getRowCount(); ++i) {
            if (!paramMap.containsKey(reportParam.getString(i, "paramid")) && !paramMap.containsKey(reportParam.getString(i, "paraminto"))) continue;
            argumentlist.put(reportParam.getString(i, "paramid"), paramMap.get(reportParam.getString(i, "paramid")) != null ? paramMap.get(reportParam.getString(i, "paramid")) : paramMap.get(reportParam.getString(i, "paraminto")));
            if (OpalUtil.isNotEmpty(reportParam.getString(i, "paraminto"))) {
                argumentlist.put(reportParam.getString(i, "paraminto"), paramMap.get(reportParam.getString(i, "paraminto")));
            }
            arg.append(reportParam.getString(i, "paramdesc")).append(": ").append(paramMap.get(reportParam.getString(i, "paramid")) != null ? paramMap.get(reportParam.getString(i, "paramid")) : (paramMap.get(reportParam.getString(i, "paraminto")) != null ? paramMap.get(reportParam.getString(i, "paraminto")) : "")).append("\r");
        }
    }

    private Document createTitleForChildReport(Map paramMap, ChildItem item) throws Exception {
        HashMap<String, Object> argumentlist = new HashMap<String, Object>();
        StringBuilder arg = new StringBuilder();
        this.populateArgumentsInfo(item, paramMap, argumentlist, arg, "Separator");
        File childTitle = new File(SapphireCollatedReport.substituteConfigurationPaths(this.getChildReportTitle()));
        Document childTitleDoc = new Document(childTitle.getAbsolutePath());
        this.populateMapForSeparatorPage(item.getItemName(), item.getItemDescription(), item.getCreadtedBy(), item.getCreadtedByName(), item.getCreatedOn(), arg.toString(), argumentlist);
        SapphireCollatedUtil.replaceTokens(this.replaceMapForSeparatorPage, childTitleDoc);
        return childTitleDoc;
    }

    private String getKeyidsToReplace(String keyids) {
        return keyids.replace(";", ",");
    }

    private HashMap getReportParams(HttpServletRequest request, SapphireJasperReport childJasper) {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        String connectionid = requestContext.getConnectionId();
        HashMap<String, String> paramMap = this.getMapFromRequest(request);
        this.populateParamForChild(childJasper, paramMap);
        CommonParamMap cparamsMap = new CommonParamMap(paramMap, connectionid);
        childJasper.populatedParam(request, cparamsMap);
        String addressid = request.getParameter("reportaddressid");
        if (OpalUtil.isEmpty(addressid)) {
            addressid = OpalUtil.isNotEmpty(childJasper.getAddressid()) ? childJasper.getAddressid() : "Global";
        }
        SDIData address = this.populatedAddressInfo(addressid);
        cparamsMap.put("addressid", address);
        return cparamsMap;
    }

    private HashMap<String, String> getMapFromRequest(HttpServletRequest request) {
        Enumeration e = request.getParameterNames();
        HashMap<String, String> paramMap = new HashMap<String, String>();
        while (e.hasMoreElements()) {
            String paramKey = (String)e.nextElement();
            paramMap.put(paramKey, request.getParameter(paramKey));
        }
        return paramMap;
    }

    public HashMap getReportParams(HashMap actionprops, SapphireJasperReport childJasper) {
        HashMap paramMap = new HashMap();
        this.populateParamForChild(childJasper, paramMap);
        HashMap cparamsMap = childJasper.getReportParamMap(actionprops);
        paramMap.putAll(cparamsMap);
        return paramMap;
    }

    private void populateParamForChild(SapphireJasperReport childJasper, Map paramMap) {
        DataSet reportMappingDS = this.getReportParamMap(childJasper.getReportid(), childJasper.getReportversionid());
        for (int i = 0; i < reportMappingDS.getRowCount(); ++i) {
            if (!paramMap.containsKey(reportMappingDS.getString(i, "parentparamid"))) continue;
            paramMap.put(reportMappingDS.getString(i, "childreportparamid"), paramMap.get(reportMappingDS.getString(i, "parentparamid")));
        }
    }

    @Override
    public void init() throws SapphireException {
        Configuration config = Configuration.getInstance();
        this.supportedDisplayTypes.add("pdf");
        this.supportedDisplayTypes.add("docx");
        this.defaultDisplayType = "pdf";
        this.m18NUtil = new M18NUtil(this.connectionInfo);
    }

    private DataSet getAttachmentNumbers(String attachmentsdcid, String attachmentkeyid1, String attachmentkeyid2, String attachmentkeyid3, String attachmentclass) {
        SafeSQL safeSQL = new SafeSQL();
        QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        StringBuilder query = new StringBuilder();
        query.append(" SELECT attachmentnum, sda.createdt, sda.createby, sysuserdesc FROM sdiattachment sda, sysuser ").append(" WHERE sdcid = ").append(safeSQL.addVar(attachmentsdcid)).append(" AND keyid1 = ").append(safeSQL.addVar(attachmentkeyid1)).append(" AND sda.createby = sysuserid");
        if (OpalUtil.isNotEmpty(attachmentkeyid2)) {
            query.append(" AND keyid2 = ").append(safeSQL.addVar(attachmentkeyid2));
        }
        if (OpalUtil.isNotEmpty(attachmentkeyid3)) {
            query.append(" AND keyid3 = ").append(safeSQL.addVar(attachmentkeyid3));
        }
        query.append(" AND attachmentclass = ").append(safeSQL.addVar(attachmentclass));
        return qp.getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
    }

    public DataSet getReportParamMap(String childreportid, String childreportversionid) {
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder query = new StringBuilder();
        QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        query.append(" SELECT * from reportparammap").append(" WHERE reportid = ").append(safeSQL.addVar(this.reportid)).append(" AND reportversionid = ").append(safeSQL.addVar(this.reportversionid)).append(" AND childreportid = ").append(safeSQL.addVar(childreportid)).append(" order by usersequence");
        return qp.getPreparedSqlDataSet(query.toString(), safeSQL.getValues());
    }

    private Map<String, ChildItem> getDocuments(DataSet childreports, int row, HashMap reportParam, String displaytype, HttpServletRequest request, boolean isFile, String languageid) throws Exception {
        LinkedHashMap<String, ChildItem> childReportInfo = new LinkedHashMap<String, ChildItem>();
        HashMap paramMap = new HashMap();
        paramMap.putAll(reportParam);
        String childReportid = childreports.getString(row, "childreportid");
        String childReportVersionid = childreports.getString(row, "childreportversionid");
        SapphireReport sr = SapphireReport.getIntance(childReportid, childReportVersionid, this.connectionInfo, languageid, isFile, displaytype);
        SapphireJasperReport childJasper = (SapphireJasperReport)sr;
        JasperReport jasperReport = childJasper.getJasperReport();
        HashMap childParamsMap = null;
        childParamsMap = request == null ? this.getReportParams(paramMap, childJasper) : this.getReportParams(request, childJasper);
        childParamsMap.put("jasperreport", jasperReport);
        paramMap.putAll(childParamsMap);
        ChildItem childItem = new ChildItem(childreports.getString(row, "reportitemid"), childreports.getString(row, "childreportid"), childreports.getString(row, "childreportversionid"), "Report", childReportid, sr.getReportdesc(), ((SapphireJasperReport)sr).getReportBytes(paramMap, displaytype, request), "", this.connectionInfo.getSysuserName(), this.connectionInfo.getSysuserId(), this.m18NUtil.format(this.m18NUtil.getNowCalendar()), "", "");
        childReportInfo.put(childReportid, childItem);
        return childReportInfo;
    }

    private void populateMapForTitlePage(String repotrid, String reportdesc, String arguments, Map<String, Object> argumentlist) {
        this.replaceMapForTitlePage.put("[report.reportid]", repotrid);
        this.replaceMapForTitlePage.put("[report.reportdesc]", reportdesc);
        this.replaceMapForTitlePage.put("[currentuser]", this.connectionInfo.getSysuserId());
        this.replaceMapForTitlePage.put("[currentuser.department]", OpalUtil.isNotEmpty(this.connectionInfo.getDefaultDepartment()) ? this.connectionInfo.getDefaultDepartment() : "");
        this.replaceMapForTitlePage.put("[currentdepartment]", OpalUtil.isNotEmpty(this.connectionInfo.getDefaultDepartment()) ? this.connectionInfo.getDefaultDepartment() : "");
        this.replaceMapForTitlePage.put("[currentuser.name]", this.connectionInfo.getSysuserName());
        this.replaceMapForTitlePage.put("[now]", this.m18NUtil.format(this.m18NUtil.getNowCalendar()));
        this.replaceMapForTitlePage.put("[database]", this.connectionInfo.getDatabaseId());
        for (Map.Entry<String, Object> entry : argumentlist.entrySet()) {
            this.replaceMapForTitlePage.put("[argument." + entry.getKey() + "]", OpalUtil.isNotEmpty(entry.getValue().toString()) ? entry.getValue().toString() : "");
        }
        this.replaceMapForTitlePage.put("[argument]", arguments);
    }

    private void populateMapForSeparatorPage(String itemid, String itemdesc, String createdby, String createdbyName, String createdate, String arguments, Map<String, Object> argumentlist) {
        this.replaceMapForSeparatorPage.put("[item.itemid]", itemid);
        this.replaceMapForSeparatorPage.put("[item.itemdesc]", itemdesc);
        this.replaceMapForSeparatorPage.put("[currentuser]", createdby);
        this.replaceMapForSeparatorPage.put("[currentuser.name]", createdbyName);
        this.replaceMapForSeparatorPage.put("[currentuser.department]", OpalUtil.isNotEmpty(this.connectionInfo.getDefaultDepartment()) ? this.connectionInfo.getDefaultDepartment() : "");
        this.replaceMapForSeparatorPage.put("[currentdepartment]", OpalUtil.isNotEmpty(this.connectionInfo.getDefaultDepartment()) ? this.connectionInfo.getDefaultDepartment() : "");
        this.replaceMapForSeparatorPage.put("[now]", createdate);
        this.replaceMapForSeparatorPage.put("[database]", this.connectionInfo.getDatabaseId());
        for (Map.Entry<String, Object> entry : argumentlist.entrySet()) {
            this.replaceMapForTitlePage.put("[argument." + entry.getKey() + "]", entry.getValue().toString());
        }
        this.replaceMapForSeparatorPage.put("[argument]", arguments);
    }

    public static String substituteConfigurationPaths(String filename) {
        try {
            Configuration config = Configuration.getInstance();
            filename = StringUtil.replaceAll(filename, "[labvantagehome]", config.getSapphireHome(), false);
            filename = StringUtil.replaceAll(filename, "[sapphirehome]", config.getSapphireHome(), false);
            filename = StringUtil.replaceAll(filename, "[applicationhome]", config.getApplicationHome(), false);
            filename = StringUtil.replaceAll(filename, "\\", "/");
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        return filename;
    }

    private void populateReportItem(HashMap paramMap, DataSet allreportItems) {
        for (int childCount = 0; childCount < allreportItems.getRowCount(); ++childCount) {
            String showifparamid = allreportItems.getString(childCount, "showifparamid");
            String showifparavalue = allreportItems.getString(childCount, "showifparamvalue");
            if (OpalUtil.isNotEmpty(showifparamid)) {
                if (paramMap.get(showifparamid) == null || !paramMap.get(showifparamid).toString().equalsIgnoreCase(showifparavalue)) continue;
                if (allreportItems.getString(childCount, "itemtype").equalsIgnoreCase("Report")) {
                    this.reportitemList.add(allreportItems.getString(childCount, "reportitemid"));
                    continue;
                }
                if (!allreportItems.getString(childCount, "itemtype").equalsIgnoreCase("Attachment")) continue;
                this.reportitemList.add(allreportItems.getString(childCount, "reportitemid"));
                continue;
            }
            if (allreportItems.getString(childCount, "itemtype").equalsIgnoreCase("Report")) {
                this.reportitemList.add(allreportItems.getString(childCount, "reportitemid"));
                continue;
            }
            if (!allreportItems.getString(childCount, "itemtype").equalsIgnoreCase("Attachment")) continue;
            this.reportitemList.add(allreportItems.getString(childCount, "reportitemid"));
        }
    }

    class ChildItem {
        private String itemType;
        private String itemKeyid;
        private String itemName;
        private String itemDescription;
        private byte[] itemByte;
        private String attachmentType;
        private String creadtedBy;
        private String creadtedByName;
        private String createdOn;
        private String itemid;
        private String childreportid;
        private String childreportversionid;
        private String attachmentClass;

        public String getAttachmentClass() {
            return this.attachmentClass;
        }

        public void setAttachmentClass(String attachmentClass) {
            this.attachmentClass = attachmentClass;
        }

        public String getChildreportid() {
            return this.childreportid;
        }

        public void setChildreportid(String childreportid) {
            this.childreportid = childreportid;
        }

        public String getChildreportversionid() {
            return this.childreportversionid;
        }

        public void setChildreportversionid(String childreportversionid) {
            this.childreportversionid = childreportversionid;
        }

        public String getItemType() {
            return this.itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public String getItemName() {
            return this.itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getItemDescription() {
            return this.itemDescription;
        }

        public void setItemDescription(String itemDescription) {
            this.itemDescription = itemDescription;
        }

        public byte[] getItemByte() {
            return this.itemByte;
        }

        public void setItemByte(byte[] itemByte) {
            this.itemByte = itemByte;
        }

        public String getItemKeyid() {
            return this.itemKeyid;
        }

        public void setItemKeyid(String itemKeyid) {
            this.itemKeyid = itemKeyid;
        }

        public String getAttachmentType() {
            return this.attachmentType;
        }

        public void setAttachmentType(String attachmentType) {
            this.attachmentType = attachmentType;
        }

        public String getCreadtedBy() {
            return this.creadtedBy;
        }

        public void setCreadtedBy(String creadtedBy) {
            this.creadtedBy = creadtedBy;
        }

        public String getCreatedOn() {
            return this.createdOn;
        }

        public void setCreatedOn(String createdOn) {
            this.createdOn = createdOn;
        }

        public String getItemid() {
            return this.itemid;
        }

        public void setItemid(String itemid) {
            this.itemid = itemid;
        }

        public String getCreadtedByName() {
            return this.creadtedByName;
        }

        public void setCreadtedByName(String creadtedByName) {
            this.creadtedByName = creadtedByName;
        }

        public ChildItem(String itemid, String childreportid, String childreportversionid, String itemType, String itemName, String itemDescription, byte[] itemByte, String attachmentType, String creadtedBy, String creadtedByName, String createdOn, String keyid, String attachmentClass) {
            this.itemType = itemType;
            this.itemName = itemName;
            this.itemDescription = itemDescription;
            this.itemByte = itemByte;
            this.attachmentType = attachmentType;
            this.creadtedBy = creadtedBy;
            this.creadtedByName = creadtedByName;
            this.createdOn = createdOn;
            this.itemid = itemid;
            this.childreportversionid = childreportversionid;
            this.childreportid = childreportid;
            this.itemKeyid = keyid;
            this.attachmentClass = attachmentClass;
        }
    }
}

