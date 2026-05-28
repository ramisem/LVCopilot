/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.pdfbox.pdmodel.PDDocument
 *  org.apache.pdfbox.pdmodel.common.PDRectangle
 *  org.apache.pdfbox.printing.PDFPrintable
 */
package com.labvantage.sapphire.report;

import com.labvantage.sapphire.util.Printer;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.print.PrintService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.printing.PDFPrintable;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.xml.PropertyList;

public class PrintPdf
extends BaseAction {
    public static final String ID = "PrintPdf";
    public static final String VERSIONID = "1";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String filename = properties.getProperty("filename", "");
        String printerName = properties.getProperty("printer", "");
        String printerType = properties.getProperty("printertype", "Device");
        String copiesStr = properties.getProperty("numcopies", VERSIONID);
        boolean removeFile = properties.getProperty("deletefile", "Y").startsWith("Y");
        boolean usePrinterMargins = properties.getProperty("useprintermargins", "N").startsWith("Y");
        int copies = this.getCopies(copiesStr);
        Printer printer = new Printer(printerName, printerType, this.getQueryProcessor());
        try {
            this.printPDF(printer, filename, copies, usePrinterMargins);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to print PDF: " + e.getMessage());
        }
        if (removeFile) {
            try {
                Files.delete(Paths.get(filename, new String[0]));
            }
            catch (IOException e) {
                this.logger.error("Could not delete file:" + filename);
            }
        }
    }

    protected void printPDF(Printer printer, byte[] pdfbytes, int copies, boolean usePrinterMargins) throws SapphireException, PrinterException {
        try (PDDocument document = PDDocument.load((byte[])pdfbytes);){
            this.printPDF(printer, document, copies, usePrinterMargins);
        }
        catch (IOException e) {
            this.logger.error("Could not print pdf file." + e.getMessage());
        }
    }

    protected void printPDF(Printer printer, PDDocument document, int copies, boolean usePrinterMargins) throws PrinterException, SapphireException {
        PageFormat pageFormat;
        PrinterJob pjob;
        PrintService[] ps;
        PrintService myPrinterService = null;
        for (PrintService p : ps = PrinterJob.lookupPrintServices()) {
            String printerName = p.getName();
            if (!printerName.equalsIgnoreCase(printer.getPrinterName())) continue;
            myPrinterService = p;
            break;
        }
        if (myPrinterService != null) {
            pjob = PrinterJob.getPrinterJob();
            pjob.setCopies(copies);
            pjob.setPrintService(myPrinterService);
            PageFormat defaultPageFormat = pjob.defaultPage();
            Paper defaultPaper = defaultPageFormat.getPaper();
            double customWidth = printer.getWidth() != null && printer.getWidth().compareTo(BigDecimal.ZERO) > 0 ? printer.getWidth().doubleValue() : defaultPaper.getWidth();
            double customHeight = printer.getHeight() != null && printer.getHeight().compareTo(BigDecimal.ZERO) > 0 ? printer.getHeight().doubleValue() : defaultPaper.getHeight();
            Paper paper = new Paper();
            paper.setSize(customWidth, customHeight);
            if (usePrinterMargins) {
                paper.setImageableArea(defaultPaper.getImageableX(), defaultPaper.getImageableY(), defaultPaper.getImageableWidth(), defaultPaper.getImageableHeight());
            } else {
                paper.setImageableArea(0.0, 0.0, paper.getWidth(), paper.getHeight());
            }
            pageFormat = new PageFormat();
            pageFormat.setPaper(paper);
            if (printer.isRotateAutomatically()) {
                int printOrientation = defaultPageFormat.getOrientation();
                if (document.getNumberOfPages() > 0) {
                    PDRectangle mediaBox = document.getPage(0).getMediaBox();
                    boolean isLandscape = mediaBox.getWidth() > mediaBox.getHeight();
                    int rotation = document.getPage(0).getRotation();
                    if (rotation == 90 || rotation == 270) {
                        isLandscape = !isLandscape;
                    }
                    printOrientation = isLandscape ? 0 : 1;
                }
                pageFormat.setOrientation(printOrientation);
            }
        } else {
            throw new SapphireException("Invalid print service name:" + printer);
        }
        Book book = new Book();
        book.append((Printable)new PDFPrintable(document), pageFormat, document.getNumberOfPages());
        pjob.setPageable(book);
        pjob.setJobName("labelprint" + this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        pjob.print();
    }

    protected void printPDF(Printer printer, String filename, int copies, boolean usePrinterMargins) throws SapphireException, PrinterException {
        File file = new File(filename);
        try (PDDocument document = PDDocument.load((File)file);){
            this.printPDF(printer, document, copies, usePrinterMargins);
        }
        catch (IOException e) {
            throw new SapphireException("Could not load PDF file for printing: " + filename);
        }
    }

    protected int getCopies(String copiesStr) {
        BigDecimal copiesB;
        try {
            copiesB = new BigDecimal(copiesStr);
        }
        catch (NumberFormatException e) {
            copiesB = BigDecimal.ONE;
        }
        return copiesB.intValue();
    }
}

