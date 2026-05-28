/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.cells.ImageFormat
 *  com.aspose.cells.ImageOrPrintOptions
 *  com.aspose.cells.SheetRender
 *  com.aspose.cells.Workbook
 *  com.aspose.cells.WorkbookRender
 *  com.aspose.cells.Worksheet
 *  com.aspose.pdf.Document
 *  com.aspose.pdf.IDocument
 *  com.aspose.pdf.facades.PdfViewer
 *  com.aspose.pdf.printing.PdfPrinterSettings
 *  com.aspose.words.Document
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.commons.io.FileUtils
 */
package com.labvantage.sapphire.report.jasper;

import com.aspose.cells.ImageFormat;
import com.aspose.cells.ImageOrPrintOptions;
import com.aspose.cells.SheetRender;
import com.aspose.cells.Workbook;
import com.aspose.cells.WorkbookRender;
import com.aspose.cells.Worksheet;
import com.aspose.pdf.IDocument;
import com.aspose.pdf.facades.PdfViewer;
import com.aspose.pdf.printing.PdfPrinterSettings;
import com.aspose.words.Document;
import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.report.DoubleOutputStream;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.jasper.TalendJavaReport;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import sapphire.SapphireException;
import sapphire.report.BaseJavaReport;
import sapphire.report.PrintReportOptions;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class SapphireJavaTalendReport
extends SapphireReport {
    BaseJavaReport javaReport = null;

    public SapphireJavaTalendReport(String reporttypeflag) {
        super(reporttypeflag);
    }

    @Override
    public void init() throws SapphireException {
        Class<Object> c;
        String className;
        LabVantageClassLoader classLoader = null;
        if (SecurityPolicyUtil.isJavaAttachmentsPermitted(this.connectionInfo.getConnectionId(), LabVantageClassLoader.ClassLoaderType.REPORT.getArea())) {
            DataSet actionAttsDs = this.reportData != null ? this.reportData.getDataset("attachment") : null;
            String appresourceid = this.reportData != null && this.reportData.getDataset("primary") != null ? this.reportData.getDataset("primary").getValue(0, "appresourceid", "") : null;
            try {
                String[] excludedJars = new String[]{"sapphire"};
                classLoader = LabVantageClassLoader.getClassLoader(LabVantageClassLoader.ClassLoaderType.REPORT, this.reportid, appresourceid, actionAttsDs, null, excludedJars, this.connectionInfo);
            }
            catch (Exception e) {
                throw new SapphireException("Failed to load attachments as libraries", e);
            }
        } else {
            Trace.logDebug("Class loaders disabled in security policy.");
        }
        if (this.reporttypeflag.equals("C")) {
            className = this.primaryds.getValue(0, "objectname");
            try {
                c = classLoader != null ? classLoader.loadClass(className) : Class.forName(className);
                this.javaReport = (BaseJavaReport)c.newInstance();
            }
            catch (Throwable e) {
                e.printStackTrace();
                throw new SapphireException(e);
            }
            this.defaultDisplayType = FileManager.getExtension(this.javaReport.getLogicalFileName(""));
        } else if (this.reporttypeflag.equals("T")) {
            className = this.primaryds.getValue(0, "objectname");
            try {
                c = classLoader != null ? classLoader.loadClass(TalendJavaReport.class.getName()) : TalendJavaReport.class;
                TalendJavaReport talendJavaReport = (TalendJavaReport)c.newInstance();
                talendJavaReport.setObjectName(className);
                this.javaReport = talendJavaReport;
            }
            catch (Throwable e) {
                e.printStackTrace();
                throw new SapphireException(e);
            }
            this.defaultDisplayType = FileManager.getExtension(this.javaReport.getLogicalFileName(""));
        }
        this.javaReport.setClassLoader(classLoader != null ? classLoader : this.getClass().getClassLoader());
        this.javaReport.setConnectionId(this.connectionInfo.getConnectionId());
    }

    public String getLogicalFileName(String defaultFilename) {
        return this.javaReport.getLogicalFileName(defaultFilename);
    }

    public String getMimeType(String defaultFilename) {
        return this.javaReport.getMimeType(defaultFilename);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void runReportToWeb(HashMap paramsMap, String displayType, HttpServletRequest request, HttpServletResponse response, String defaultFileName, boolean isFile) throws SapphireException {
        SapphireReportEvent reportevent = this.runReportToWebEvent(paramsMap, displayType, response, isFile);
        try {
            Object outputStream;
            this.javaReport.init(this.reportid, this.reportversionid, paramsMap, this.connectionInfo);
            String fileName = this.javaReport.getLogicalFileName(defaultFileName);
            if (fileName == null || fileName.length() == 0) {
                throw new SapphireException("Unable to resolve a filename for the report");
            }
            String mimeType = this.javaReport.getMimeType(fileName);
            if (mimeType == null || mimeType.length() == 0 && fileName != null && fileName.length() > 0) {
                mimeType = FileType.getFileTypeByFileName(fileName, this.connectionInfo.getConnectionId()).getMime();
            }
            if (mimeType == null || mimeType.length() == 0) {
                throw new SapphireException("Unable to resolve a mimetype for the report");
            }
            if (isFile) {
                response.setContentType("application/download");
            } else {
                response.setContentType(mimeType);
            }
            response.setHeader("Content-Disposition", "attachment; filename=" + HttpUtil.encodeURIComponent(fileName));
            ByteArrayOutputStream byteOutputStream = null;
            if (this.isControlledReport()) {
                byteOutputStream = new ByteArrayOutputStream();
                outputStream = new DoubleOutputStream((OutputStream)response.getOutputStream(), byteOutputStream);
            } else {
                outputStream = response.getOutputStream();
            }
            try {
                LabVantageClassLoader.executeCode(this.javaReport.getClassLoader(), () -> this.lambda$runReportToWeb$0((OutputStream)outputStream), false);
            }
            finally {
                this.finalCloseFlushOutputStream((OutputStream)outputStream);
            }
            if (this.isControlledReport()) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteOutputStream.toByteArray());
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Error running report " + this.reportid + ":" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    private void finalCloseFlushOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void sendReportToEmail(HashMap paramsMap, String displaytype, String emailfrom, String emailtolist, String emailcclist, String emailsubject, String emailmessage, String defaultFilename) throws SapphireException {
        SapphireReportEvent reportevent = this.sendReportToEmailEvent(paramsMap, displaytype, emailfrom, emailtolist, emailcclist, emailsubject, emailmessage);
        File file = null;
        try {
            this.javaReport.init(this.reportid, this.reportversionid, paramsMap, this.connectionInfo);
            String fileName = this.javaReport.getLogicalFileName(defaultFilename);
            file = File.createTempFile(FileManager.getFileName(fileName, false), "." + FileManager.getExtension(fileName));
            file.deleteOnExit();
            FileOutputStream outputStream = new FileOutputStream(file);
            try {
                LabVantageClassLoader.executeCode(this.javaReport.getClassLoader(), () -> this.javaReport.runReport(outputStream), false);
                PropertyList actionProps = new PropertyList();
                actionProps.put("from", emailfrom);
                actionProps.put("to", emailtolist);
                actionProps.put("cc", emailcclist);
                actionProps.put("subject", emailsubject);
                actionProps.put("message", emailmessage);
                actionProps.put("filename", file.getAbsolutePath());
                actionProps.put("logicalfilename", fileName);
                ActionService ac = new ActionService(this.sapphireConnection);
                ac.processAction("SendMail", "1", actionProps);
            }
            finally {
                this.finalCloseFlushOutputStream(outputStream);
            }
            if (reportevent != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray((File)file));
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to send generated report: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void sendReportToFile(HashMap paramsMap, String displayType, String absoluteFileName) throws SapphireException {
        try {
            String talendfilename;
            this.javaReport.init(this.reportid, this.reportversionid, paramsMap, this.connectionInfo);
            if (absoluteFileName.length() == 0 && (talendfilename = this.javaReport.getLogicalFileName("")).trim().length() > 0) {
                File tempFile = FileUtil.createTempFile(FileManager.getFileName(talendfilename, false), "." + FileManager.getExtension(talendfilename)).toFile();
                absoluteFileName = tempFile.getAbsolutePath();
                paramsMap.put("talendfilename", talendfilename);
                paramsMap.put("talendfilepath", absoluteFileName);
            }
            File file = new File(absoluteFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            try {
                LabVantageClassLoader.executeCode(this.javaReport.getClassLoader(), () -> this.javaReport.runReport(outputStream), false);
            }
            finally {
                this.finalCloseFlushOutputStream(outputStream);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to send generated report: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void sendReportToPrinter(HashMap paramsMap, String displayType, String printer, String addressid, String addresstype) throws SapphireException {
        SapphireReportEvent reportevent = this.sendReportToPrinterEvent(paramsMap, displayType, printer, addressid, addresstype);
        File file = null;
        try {
            this.javaReport.init(this.reportid, this.reportversionid, paramsMap, this.connectionInfo);
            String fileName = this.javaReport.getLogicalFileName("");
            file = File.createTempFile(FileManager.getFileName(fileName, false), "." + FileManager.getExtension(fileName));
            file.deleteOnExit();
            FileOutputStream outputStream = new FileOutputStream(file);
            try {
                LabVantageClassLoader.executeCode(this.javaReport.getClassLoader(), () -> this.javaReport.runReport(outputStream), false);
            }
            finally {
                this.finalCloseFlushOutputStream(outputStream);
            }
            PrintReportOptions options = new PrintReportOptions(file.getName());
            this.javaReport.adjustPrintOptions(options);
            this.printFile(printer, file, options);
            if (reportevent != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray((File)file));
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to send generated report: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        finally {
            if (file != null && file.exists()) {
                file.delete();
            }
        }
    }

    public boolean printFile(String printerName, File file, PrintReportOptions options) throws SapphireException {
        block14: {
            if (printerName.length() == 0) {
                throw new SapphireException("Printer name is empty. Specify a valid printer");
            }
            try {
                if (options.printMode == 2) {
                    Workbook workbook = new Workbook(file.getAbsolutePath());
                    ImageOrPrintOptions imgOptions = new ImageOrPrintOptions();
                    if (options.excelSheetIndexToPrint == -1) {
                        imgOptions.setImageFormat(ImageFormat.getTiff());
                        WorkbookRender wr = new WorkbookRender(workbook, imgOptions);
                        wr.toPrinter(printerName);
                    } else {
                        Worksheet sheet = workbook.getWorksheets().get(options.excelSheetIndexToPrint);
                        SheetRender sheetRender = new SheetRender(sheet, imgOptions);
                        sheetRender.toPrinter(printerName);
                    }
                    break block14;
                }
                if (options.printMode == 3 || options.printMode == 1) {
                    Document doc = new Document(file.getAbsolutePath());
                    doc.print(printerName);
                    break block14;
                }
                if (options.printMode == 4) {
                    com.aspose.pdf.Document doc = new com.aspose.pdf.Document(file.getAbsolutePath());
                    PdfViewer pdfViewer = new PdfViewer((IDocument)doc);
                    PdfPrinterSettings printerSettings = pdfViewer.getDefaultPrinterSettings().deepClone();
                    printerSettings.setPrinterName(printerName);
                    pdfViewer.printDocumentWithSettings(printerSettings);
                    break block14;
                }
                if (options.printMode == 5) {
                    HashPrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
                    pras.add(new Copies(1));
                    PrintService[] pss = PrintServiceLookup.lookupPrintServices(options.imagePrintFavor, pras);
                    if (pss.length == 0) {
                        throw new RuntimeException("No printer services available.");
                    }
                    PrintService ps = pss[0];
                    for (int i = 0; i < pss.length; ++i) {
                        PrintService curr = pss[i];
                        if (!curr.getName().equals(printerName)) continue;
                        ps = pss[i];
                    }
                    DocPrintJob job = ps.createPrintJob();
                    try {
                        FileInputStream fin = new FileInputStream(file);
                        SimpleDoc doc = new SimpleDoc(fin, options.imagePrintFavor, null);
                        job.print(doc, pras);
                        fin.close();
                        break block14;
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to print file." + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
                    }
                }
                if (options.printMode == 6) {
                    this.javaReport.sendToPrinter(printerName, file, options);
                    break block14;
                }
                throw new SapphireException("Failed to print file " + file.getAbsolutePath() + ". Unrecognized PrintMode");
            }
            catch (Exception e) {
                throw new SapphireException("Failed to print file " + file.getAbsolutePath() + ": " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
        }
        return true;
    }

    @Override
    public boolean canPrint() {
        return this.javaReport.canPrint();
    }

    private /* synthetic */ void lambda$runReportToWeb$0(OutputStream outputStream) throws SapphireException {
        this.javaReport.runReport(outputStream);
    }
}

