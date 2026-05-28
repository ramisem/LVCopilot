/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.report.nwa;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.nwa.SapphireNWAUtil;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.ServiceException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SapphireNWAReport
extends SapphireReport {
    public SapphireNWAUtil nwaUtil;

    public SapphireNWAReport(String reporttypeflag) {
        super(reporttypeflag);
    }

    @Override
    public void init() throws SapphireException {
        this.defaultDisplayType = "pdf";
    }

    @Override
    public void runReportToWeb(HashMap paramMap, String displayType, HttpServletRequest request, HttpServletResponse response, String fileName, boolean isFile) throws SapphireException {
        SapphireReportEvent reportevent = this.runReportToWebEvent(paramMap, displayType, response, isFile);
        byte[] image = null;
        image = this.nwaUtil.runReport(paramMap, this.paramds, this.primaryds.getString(0, "librarydir", ""), response);
        if (this.isControlledReport() && reportevent != null) {
            PropertyListCollection generatedFiles = this.nwaUtil.downloadGeneratedFiles(this.primaryds.getString(0, "librarydir", ""), reportevent);
            PropertyList stats = this.nwaUtil.getStats();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(image);
            reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
            this.nwaUtil.addToReportEvent(reportevent.getReporteventid(), generatedFiles);
        }
    }

    @Override
    public void sendReportToEmail(HashMap paramMap, String displaytype, String emailfrom, String emailtolist, String emailcclist, String emailsubject, String emailmessage, String fileName) throws SapphireException {
        SapphireReportEvent reportevent = this.sendReportToEmailEvent(paramMap, displaytype, emailfrom, emailtolist, emailcclist, emailsubject, emailmessage);
        PropertyList actionProps = new PropertyList();
        byte[] image = null;
        actionProps.put("from", emailfrom);
        actionProps.put("to", emailtolist);
        actionProps.put("cc", emailcclist);
        actionProps.put("subject", emailsubject);
        actionProps.put("message", emailmessage);
        String ext = ".png";
        try {
            File temp;
            if (displaytype == null || displaytype.length() == 0) {
                ext = ".png";
            }
            if (fileName == null || fileName.trim().length() == 0) {
                temp = File.createTempFile(this.reportid, ext);
            } else {
                String name = (fileName = fileName + ext).indexOf(File.separator) > -1 ? fileName.substring(fileName.indexOf(File.separator), fileName.lastIndexOf(".")) : fileName.substring(0, fileName.lastIndexOf("."));
                temp = File.createTempFile(name, ext);
            }
            temp.deleteOnExit();
            if (this.nwaUtil == null) {
                this.nwaUtil = new SapphireNWAUtil(this.connectionInfo);
            }
            image = this.nwaUtil.sendToFile(paramMap, this.paramds, this.primaryds.getString(0, "librarydir", ""), temp.getPath());
            actionProps.put("filename", temp.getPath());
        }
        catch (IOException e) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not create a temporary file " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        try {
            ActionService ac = new ActionService(this.sapphireConnection);
            ac.processAction("SendMail", "1", actionProps);
            if (this.isControlledReport() && reportevent != null) {
                PropertyListCollection generatedFiles = this.nwaUtil.downloadGeneratedFiles(this.primaryds.getString(0, "librarydir", ""), reportevent);
                PropertyList stats = this.nwaUtil.getStats();
                reportevent.saveEvent(image, this.connectionInfo, stats);
                this.nwaUtil.addToReportEvent(reportevent.getReporteventid(), generatedFiles);
            }
        }
        catch (ServiceException e) {
            throw new SapphireException("Failed to send generated report", e);
        }
    }

    @Override
    public void sendReportToFile(HashMap paramMap, String displaytype, String fileName) throws SapphireException {
        SapphireReportEvent reportevent = this.sendReportToFileEvent(paramMap, displaytype, fileName);
        byte[] image = null;
        if (this.nwaUtil == null) {
            this.nwaUtil = new SapphireNWAUtil(this.connectionInfo);
        }
        image = this.nwaUtil.sendToFile(paramMap, this.paramds, this.primaryds.getString(0, "librarydir", ""), fileName);
        if (this.isControlledReport() && reportevent != null) {
            PropertyListCollection generatedFiles = this.nwaUtil.downloadGeneratedFiles(this.primaryds.getString(0, "librarydir", ""), reportevent);
            PropertyList stats = this.nwaUtil.getStats();
            reportevent.saveEvent(image, this.connectionInfo, stats);
            this.nwaUtil.addToReportEvent(reportevent.getReporteventid(), generatedFiles);
        }
    }

    @Override
    public void sendReportToPrinter(HashMap paramMap, String displaytype, String printername, String addressid, String addresstype) throws SapphireException {
        File temp;
        SapphireReportEvent reportevent = this.sendReportToPrinterEvent(paramMap, displaytype, printername, addressid, addresstype);
        byte[] image = null;
        String ext = ".png";
        try {
            if (displaytype == null || displaytype.length() == 0) {
                ext = ".png";
            }
            temp = File.createTempFile(this.reportid, ext);
            temp.deleteOnExit();
            if (this.nwaUtil == null) {
                this.nwaUtil = new SapphireNWAUtil(this.connectionInfo);
            }
            image = this.nwaUtil.sendToFile(paramMap, this.paramds, this.primaryds.getString(0, "librarydir", ""), temp.getPath());
        }
        catch (IOException e) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not create a temporary file " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        this.printImage(printername, temp.getPath());
        if (this.isControlledReport() && reportevent != null) {
            PropertyListCollection generatedFiles = this.nwaUtil.downloadGeneratedFiles(this.primaryds.getString(0, "librarydir", ""), reportevent);
            PropertyList stats = this.nwaUtil.getStats();
            reportevent.saveEvent(image, this.connectionInfo, stats);
            this.nwaUtil.addToReportEvent(reportevent.getReporteventid(), generatedFiles);
        }
    }

    public HashMap getReportParamMap(HttpServletRequest request, PropertyList propertyList) throws Exception {
        HashMap<String, String> paramsMap;
        block5: {
            Iterator iter;
            block4: {
                paramsMap = new HashMap<String, String>();
                Set props = propertyList.keySet();
                iter = props.iterator();
                String regenerateflag = "N";
                if (request != null) {
                    regenerateflag = request.getParameter("regenerate");
                }
                if (request == null || regenerateflag == null || !"Y".equalsIgnoreCase(regenerateflag)) break block4;
                DataSet ds = (DataSet)request.getAttribute("paramvalueds");
                if (ds == null || ds.size() <= 0) break block5;
                for (int i = 0; i < ds.size(); ++i) {
                    String paramid = ds.getString(i, "paramid");
                    String paramvalue = ds.getClob(i, "paramvalueclob", "");
                    String paramtypeflag = ds.getString(i, "paramtypeflag");
                    if ("O".equals(paramtypeflag)) continue;
                    if (paramvalue != null && paramvalue.length() > 0 && paramvalue.indexOf(",") >= 0) {
                        paramvalue = paramvalue.replaceAll(",", ";");
                    }
                    paramsMap.put(paramid, paramvalue);
                }
                if (regenerateflag == null || !"Y".equalsIgnoreCase(regenerateflag)) break block5;
                paramsMap.put("regenerate", regenerateflag);
                paramsMap.put("parentreporteventid", request.getParameter("reporteventid"));
                break block5;
            }
            while (iter.hasNext()) {
                String param = iter.next().toString();
                paramsMap.put(param, propertyList.getProperty(param));
            }
        }
        return paramsMap;
    }

    private void printImage(String printerName, String imageFileName) throws SapphireException {
        if (imageFileName.length() == 0) {
            throw new SapphireException("Image file name is empty. Specify a valid filename");
        }
        if (printerName.length() == 0) {
            throw new SapphireException("Printer name is empty. Specify a valid printer");
        }
        HashPrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new Copies(1));
        PrintService[] pss = PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.GIF, pras);
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
            FileInputStream fin = new FileInputStream(imageFileName);
            SimpleDoc doc = new SimpleDoc(fin, DocFlavor.INPUT_STREAM.GIF, null);
            job.print(doc, pras);
            fin.close();
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("File does not exist");
        }
        catch (PrintException e) {
            throw new SapphireException("Failed to print file." + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
        catch (IOException e) {
            throw new SapphireException("Failed to print file." + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }
}

