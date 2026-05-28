/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  net.sf.jasperreports.engine.JRParameter
 *  net.sf.jasperreports.engine.JasperPrint
 *  net.sf.jasperreports.engine.JasperReport
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.jasper.CommonParamMap;
import com.labvantage.sapphire.report.jasper.JasperReportPropertyHandler;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.services.ActionService;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.ServiceException;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.file.FileTransfer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SapphireJasperReport
extends SapphireReport {
    private JasperReport jasperReport;
    private JasperPrint jasperPrint;
    private String reportpath;

    public SapphireJasperReport(String reporttypeflag) {
        super(reporttypeflag);
    }

    @Override
    public void init() throws SapphireException {
        if (this.primaryds != null) {
            if (this.reporttypeflag.equals("J")) {
                this.getJasperReport();
            } else if (this.reporttypeflag.equals("K")) {
                this.loadReportFromAttachment();
                this.getJasperReport();
            }
        }
        this.supportedDisplayTypes.add("pdf");
        this.supportedDisplayTypes.add("xlsx");
        this.supportedDisplayTypes.add("xls");
        this.supportedDisplayTypes.add("docx");
        this.supportedDisplayTypes.add("doc");
        this.supportedDisplayTypes.add("csv");
        this.supportedDisplayTypes.add("rtf");
        this.supportedDisplayTypes.add("html");
        this.defaultDisplayType = "pdf";
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void loadReportFromAttachment() throws SapphireException {
        boolean folderCreated;
        String reportFolderName = this.resolveReportFolder();
        String reportobjectname = this.primaryds.getString(0, "librarydir", "");
        File reportFolderDBSpecific = new File(SapphireJasperUtil.getReportPath(this.connectionInfo.getConnectionId(), reportFolderName) + "/" + this.connectionInfo.getDatabaseId());
        if (!reportFolderDBSpecific.exists() && !(folderCreated = reportFolderDBSpecific.mkdir())) {
            throw new SapphireException("Unable to create report folder. Report Folder-->" + reportFolderDBSpecific);
        }
        File reportFolder = new File(reportFolderDBSpecific.getAbsolutePath() + "/" + reportFolderName);
        if (!reportFolder.exists()) {
            DataSet sdiattachment;
            boolean folderCreated2 = reportFolder.mkdir();
            if (!folderCreated2) {
                throw new SapphireException("Unable to create report folder. Report Folder-->" + reportFolder);
            }
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.connectionInfo.getConnectionId());
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("attachmentclass", "CustomReport");
            DataSet dataSet = sdiattachment = this.reportData != null ? this.reportData.getDataset("attachment") : null;
            if (!OpalUtil.isNotEmpty(sdiattachment)) return;
            DataSet filteredAttachment = sdiattachment.getFilteredDataSet(filter);
            if (OpalUtil.isEmpty(filteredAttachment)) {
                filter.put("attachmentclass", "OOBReport");
                filteredAttachment = sdiattachment.getFilteredDataSet(filter);
            }
            if (!OpalUtil.isNotEmpty(filteredAttachment)) throw new SapphireException("The Report Attachment not found.");
            for (int a = 0; a < filteredAttachment.getRowCount(); ++a) {
                try {
                    String sdcid = filteredAttachment.getValue(a, "sdcid", "");
                    String keyid1 = filteredAttachment.getValue(a, "keyid1", "(null)");
                    String keyid2 = filteredAttachment.getValue(a, "keyid2", "(null)");
                    String keyid3 = filteredAttachment.getValue(a, "keyid3", "(null)");
                    sapphire.attachment.Attachment jar = sapphire.attachment.Attachment.getAttachment(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(filteredAttachment.getValue(a, "attachmentnum", "")));
                    Path reportPath = attachmentProcessor.getSDIAttachmentLocalFile((Attachment)jar, true);
                    if (reportPath == null || !Files.exists(reportPath, new LinkOption[0])) continue;
                    FileTransfer.safeFileTransfer(reportPath.toFile(), reportFolder, null);
                    try {
                        List subjars;
                        if (!Files.isDirectory(reportPath, new LinkOption[0]) || (subjars = Files.walk(reportPath, new FileVisitOption[0]).sorted().collect(Collectors.toList())) == null || subjars.size() <= 0) continue;
                        for (Path sp : subjars) {
                            String name = sp.getFileName().toString();
                            if (!reportobjectname.equalsIgnoreCase(name)) continue;
                            this.primaryds.setString(0, "librarydir", reportFolderName + "/" + name);
                        }
                        continue;
                    }
                    catch (Exception e) {
                        throw new SapphireException("Failed to load report. ", e);
                    }
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to load report. ", e);
                }
            }
            return;
        }
        Path reportPath = reportFolder.toPath();
        try {
            List subfiles;
            if (!Files.isDirectory(reportPath, new LinkOption[0]) || (subfiles = Files.walk(reportPath, new FileVisitOption[0]).sorted().collect(Collectors.toList())) == null || subfiles.size() <= 0) return;
            for (Path sp : subfiles) {
                String name = sp.getFileName().toString();
                if (!reportobjectname.equalsIgnoreCase(name)) continue;
                this.primaryds.setString(0, "librarydir", reportFolderName + "/" + name);
                return;
            }
            return;
        }
        catch (Exception e) {
            throw new SapphireException("Failed to set actual report patch. ", e);
        }
    }

    private String resolveReportFolder() {
        return this.reportid;
    }

    @Override
    public void runReportToWeb(HashMap paramsMap, String displayType, HttpServletRequest request, HttpServletResponse response, String fileName, boolean isFile) throws SapphireException {
        SapphireReportEvent reportevent = this.runReportToWebEvent(paramsMap, displayType, response, isFile);
        byte[] reportBytes = null;
        try {
            reportBytes = this.getReportBytes(paramsMap, displayType, request);
            switch (displayType) {
                case "pdf": {
                    if (this.isControlledReport() && reportevent != null) {
                        reportBytes = this.getModifiedReportBytes(reportevent, reportBytes);
                    }
                    SapphireJasperUtil.runReportToWebPdf(reportBytes, response, (String)paramsMap.get("SAPPHIRE_ReportID"), this.connectionInfo, fileName, isFile);
                    break;
                }
                case "xls": 
                case "xlsx": {
                    SapphireJasperUtil.runReportToWebExcel(reportBytes, response, (String)paramsMap.get("SAPPHIRE_ReportID"), this.connectionInfo, fileName, isFile, displayType);
                    break;
                }
                case "doc": 
                case "docx": {
                    SapphireJasperUtil.runReportToWebWord(reportBytes, response, (String)paramsMap.get("SAPPHIRE_ReportID"), this.connectionInfo, fileName, isFile, displayType);
                    break;
                }
                case "rtf": {
                    SapphireJasperUtil.runReportToWebRTF(reportBytes, response, (String)paramsMap.get("SAPPHIRE_ReportID"), this.connectionInfo, fileName, isFile);
                    break;
                }
                case "csv": {
                    SapphireJasperUtil.runReportToWebCSV(reportBytes, response, (String)paramsMap.get("SAPPHIRE_ReportID"), this.connectionInfo, fileName, isFile);
                    break;
                }
                case "html": {
                    SapphireJasperUtil.runReportToWebHtml(reportBytes, response, (String)paramsMap.get("SAPPHIRE_ReportID"), this.connectionInfo, fileName, isFile);
                    break;
                }
                default: {
                    throw new SapphireException("Unrecognized display type: " + displayType);
                }
            }
            if (this.isControlledReport() && reportevent != null) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportBytes);
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
                byteArrayInputStream.close();
            }
        }
        catch (Exception e) {
            throw new SapphireException("Error running report:" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
    }

    public byte[] getReportBytes(HashMap paramsMap, String displayType, HttpServletRequest request) throws SapphireException {
        paramsMap.put("mode", "reportbytes");
        paramsMap.put("displayType", displayType);
        paramsMap.put("request", request);
        HashMap requestProps = new RequestProcessor(this.connectionInfo.getConnectionId()).processRequest(JasperReportPropertyHandler.class.getName(), paramsMap);
        if (requestProps.get("errormessage") != null) {
            throw new SapphireException((String)requestProps.get("errormessage"));
        }
        byte[] reportBytes = (byte[])requestProps.get("reportbytes");
        return reportBytes;
    }

    private byte[] getModifiedReportBytes(SapphireReportEvent reportevent, byte[] reportBytes) throws SapphireException {
        if (this.watermarkflag == null || this.watermarkflag.length() == 0) {
            this.watermarkflag = "Y";
        }
        if (this.initialdisposition != null && this.initialdisposition.equals("Pending") && this.watermarkflag != null && this.watermarkflag.equals("Y")) {
            reportBytes = SapphireReport.addWatermark(reportBytes, "Pending Confirmation");
        } else if (this.initialdisposition != null && this.initialdisposition.equals("Confirmed")) {
            reportBytes = SapphireReport.removeWatermark(reportBytes);
        }
        if (!reportevent.isDigitallysigned().equalsIgnoreCase("Y") && this.signingmode != null && (this.signingmode.equals("Automatic") || this.signingmode.equals("With Report Confirmation") && this.initialdisposition != null && this.initialdisposition.equals("Confirmed"))) {
            HashMap<String, Object> hm = new HashMap<String, Object>();
            hm.put("mode", "signbytes");
            hm.put("signbytes", reportBytes);
            hm.put("signreport", this);
            HashMap requestHm = new RequestProcessor(this.connectionInfo.getConnectionId()).processRequest(JasperReportPropertyHandler.class.getName(), hm);
            if (requestHm.get("errormessage") == null) {
                reportBytes = (byte[])requestHm.get("signbytes");
            }
            reportevent.setDigitallysigned("Y");
        }
        return reportBytes;
    }

    public JasperReport getJasperReport() throws SapphireException {
        if (this.jasperReport == null) {
            String versionstatus = this.primaryds.getValue(0, "versionstatus");
            String checksumvalue = this.primaryds.getValue(0, "checksumvalue");
            String reportfile = this.primaryds.getValue(0, "librarydir");
            if (reportfile.length() > 0) {
                try {
                    long filechecksum;
                    File file = new File(reportfile);
                    if (!file.isAbsolute()) {
                        file = new File(SapphireJasperUtil.getReportPath(this.connectionInfo.getConnectionId(), reportfile) + "/" + (this.reporttypeflag.equals("K") ? this.connectionInfo.getDatabaseId() : "") + "/" + reportfile);
                    }
                    if (!file.exists() || !file.canRead()) {
                        throw new SapphireException("File:" + file.getAbsolutePath() + " does not exist or cannot be read.");
                    }
                    if ("C".equals(versionstatus) && !this.reporttypeflag.equals("K") && (filechecksum = SapphireJasperUtil.hashFile(file)) != Long.parseLong(checksumvalue)) {
                        throw new SapphireException("File:" + file.getAbsolutePath() + "has been changed after the report version was approved.");
                    }
                    this.reportpath = file.getParent();
                    this.reportpath = this.reportpath.replace("\\", "/");
                    this.jasperReport = SapphireJasperUtil.loadReport(reportfile, this.reporttypeflag, this.connectionInfo, this.reportid, versionstatus, this.languageid.trim(), this.isFile, this.displayType);
                }
                catch (Exception e) {
                    throw new SapphireException("Could not load the report from " + SapphireJasperUtil.getReportPath(this.connectionInfo.getConnectionId(), reportfile) + "/" + (this.reporttypeflag.equals("K") ? this.connectionInfo.getDatabaseId() : "") + reportfile + ":" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
                }
            } else {
                throw new SapphireException("Report Location is empty.");
            }
        }
        return this.jasperReport;
    }

    public HashMap getReportParamMap(HashMap actionprops) {
        String addressid;
        String regenerateflag;
        CommonParamMap cparamsMap = new CommonParamMap(actionprops, this.connectionInfo);
        this.setReportParamCommon(cparamsMap, actionprops);
        String string = regenerateflag = actionprops.get("regenerate") != null ? actionprops.get("regenerate").toString() : "";
        if ("Y".equalsIgnoreCase(regenerateflag)) {
            cparamsMap.put("regenerate", regenerateflag);
            cparamsMap.put("parentreporteventid", actionprops.get("reporteventid"));
        }
        this.setInClauseParam((Map)cparamsMap, actionprops);
        String string2 = addressid = actionprops.get("reportaddressid") != null ? actionprops.get("reportaddressid").toString() : "";
        if (OpalUtil.isEmpty(addressid)) {
            addressid = OpalUtil.isNotEmpty(this.getAddressid()) ? this.getAddressid() : "Global";
        }
        SDIData address = this.populatedAddressInfo(addressid);
        cparamsMap.put("addressid", address);
        return cparamsMap;
    }

    public HashMap getReportParamMap(HttpServletRequest request) {
        CommonParamMap cparamsMap = new CommonParamMap(request);
        this.populatedParam(request, cparamsMap);
        String addressid = request.getParameter("reportaddressid");
        if (OpalUtil.isEmpty(addressid)) {
            addressid = OpalUtil.isNotEmpty(this.getAddressid()) ? this.getAddressid() : "Global";
        }
        SDIData address = this.populatedAddressInfo(addressid);
        cparamsMap.put("addressid", address);
        return cparamsMap;
    }

    public void populatedParam(HttpServletRequest request, CommonParamMap cparamsMap) {
        HashMap paramsMap = this.getSimpleRequestParamsMap(request);
        this.setReportParamCommon(cparamsMap, paramsMap);
        String regenerateflag = request.getParameter("regenerate");
        if (regenerateflag != null && "Y".equalsIgnoreCase(regenerateflag)) {
            cparamsMap.put("regenerate", regenerateflag);
            cparamsMap.put("parentreporteventid", request.getParameter("reporteventid"));
        }
        this.setInClauseParam((Map)cparamsMap, request);
    }

    private void setReportParamCommon(HashMap cparamsMap, Map actionProps) {
        cparamsMap.put("SAPPHIRE_ReportPath", this.reportpath);
        cparamsMap.put("SAPPHIRE_ReportID", this.reportid);
        cparamsMap.put("SAPPHIRE_ReportVersion", this.reportversionid);
        cparamsMap.put("SAPPHIRE_ReportTitle", this.reportdesc);
        cparamsMap.put("SUBREPORT_DIR", this.reportpath + "/");
        if (this.isControlledReport()) {
            String[] tokens = new String[]{this.sdcidvalue, this.keyid1value, this.keyid2value, this.keyid3value};
            ArrayList<String> templist = null;
            for (int i = 0; i < tokens.length; ++i) {
                if (tokens[i] == null || tokens[i].length() <= 0 || tokens[i].indexOf("[") != 0 || tokens[i].indexOf("]") <= 0) continue;
                if (templist == null) {
                    templist = new ArrayList<String>();
                }
                templist.add(StringUtil.getTokens(tokens[i])[0]);
            }
            if (templist != null) {
                cparamsMap.put("fieldvaluecollectionlist", templist);
            }
        }
        JRParameter[] params = this.jasperReport.getParameters();
        StringBuffer errorString = new StringBuffer();
        M18NUtil m18n = new M18NUtil(this.connectionInfo);
        for (int i = 0; i < params.length; ++i) {
            JRParameter param = params[i];
            if (!param.isSystemDefined()) {
                String paraminto;
                int p;
                String paramname = param.getName();
                String value = (String)actionProps.get(paramname);
                if ((value == null || value.length() == 0) && OpalUtil.isEmpty(value = (String)actionProps.get(paramname.toLowerCase()))) {
                    for (p = 0; p < this.paramds.getRowCount(); ++p) {
                        paraminto = this.paramds.getString(p, "paraminto");
                        String paramid = this.paramds.getString(p, "paramid");
                        if (!paramname.equals(paraminto)) continue;
                        value = (String)actionProps.get(paramid.toLowerCase());
                    }
                }
                if (value == null || value.length() == 0) {
                    for (p = 0; p < this.paramds.getRowCount(); ++p) {
                        paraminto = this.paramds.getString(p, "paraminto");
                        String paramtype = this.paramds.getString(p, "paramtype");
                        if (!paramname.equals(paraminto) || !paramtype.equalsIgnoreCase("hidden")) continue;
                        value = this.paramds.getValue(p, "paramvalue");
                    }
                }
                if ((value = CommonParamMap.substituteValue(value, cparamsMap)) == null || value.length() == 0) {
                    for (p = 0; p < this.paramds.getRowCount(); ++p) {
                        paraminto = this.paramds.getString(p, "paraminto");
                        String mandatoryflag = this.paramds.getString(p, "mandatoryflag");
                        String paramtype = this.paramds.getString(p, "paramtype");
                        if (!paramname.equals(paraminto) || !"Y".equals(mandatoryflag)) continue;
                        errorString.append("Input of " + this.paramds.getString(p, "paramdesc") + " is required");
                    }
                } else {
                    String valueclassname = param.getValueClassName();
                    Object valueobj = value;
                    if ("java.util.Date".equals(valueclassname)) {
                        int row = this.paramds.findRow("paraminto", paramname);
                        valueobj = row >= 0 && "dateonly".equals(this.paramds.getString(row, "paramtype")) ? m18n.parseCalendar(value, false).getTime() : m18n.parseCalendar(value).getTime();
                    } else if ("java.sql.Timestamp".equals(valueclassname)) {
                        int row = this.paramds.findRow("paraminto", paramname);
                        valueobj = row >= 0 && "dateonly".equals(this.paramds.getString(row, "paramtype")) ? m18n.parseTimestamp(value, false) : m18n.parseTimestamp(value);
                    } else if ("java.math.BigDecimal".equals(valueclassname)) {
                        valueobj = m18n.parseBigDecimal(value);
                    } else if ("java.lang.Double".equals(valueclassname)) {
                        valueobj = Double.valueOf(m18n.parseBigDecimal(value).toString());
                    } else if ("java.lang.Float".equals(valueclassname)) {
                        valueobj = Float.valueOf(m18n.parseBigDecimal(value).toString());
                    } else if ("java.lang.Integer".equals(valueclassname)) {
                        valueobj = Integer.valueOf(m18n.parseBigDecimal(value).toString());
                    } else if ("java.lang.Long".equals(valueclassname)) {
                        valueobj = Long.valueOf(m18n.parseBigDecimal(value).toString());
                    }
                    cparamsMap.put(paramname, valueobj);
                }
            }
            if ("SAPPHIRE_RSETID".equals(param.getName())) {
                this.createRSetInParamMap(cparamsMap);
                continue;
            }
            if (!"SAPPHIRE_KEYID1List".equals(param.getName()) || !"rset".equals(cparamsMap.get("SAPPHIRE_KEYID1List"))) continue;
            String rsetid = (String)cparamsMap.get("SAPPHIRE_RSETID");
            if (rsetid == null || rsetid.length() == 0) {
                this.createRSetInParamMap(cparamsMap);
            }
            if ((rsetid = (String)cparamsMap.get("SAPPHIRE_RSETID")) == null || rsetid.length() <= 0) continue;
            String sql = SapphireJasperReport.getKeyid1ListSelect(rsetid);
            cparamsMap.put("SAPPHIRE_KEYID1List", sql);
        }
    }

    @Override
    public void sendReportToEmail(HashMap paramMap, String displayType, String emailfrom, String emailtolist, String emailcclist, String emailsubject, String emailmessage, String fileName) throws SapphireException {
        block25: {
            File temp;
            String prefix;
            if (!this.getSupportedDisplayTypes().contains(displayType)) {
                throw new SapphireException("CONFIGURATION_ERROR", "Could not process the report '" + this.reportid + "': The specified file type '" + displayType + "' is not supported.");
            }
            SapphireReportEvent reportevent = this.sendReportToEmailEvent(paramMap, displayType, emailfrom, emailtolist, emailcclist, emailsubject, emailmessage);
            PropertyList actionProps = new PropertyList();
            actionProps.put("from", emailfrom);
            actionProps.put("to", emailtolist);
            actionProps.put("cc", emailcclist);
            actionProps.put("subject", emailsubject);
            actionProps.put("message", emailmessage);
            String ext = "";
            byte[] reportByte = null;
            try {
                if (displayType == null || displayType.length() == 0) {
                    displayType = "pdf";
                    ext = ".pdf";
                } else {
                    ext = displayType.equals("excel") ? ".xls" : "." + displayType;
                }
                prefix = fileName == null || fileName.trim().length() == 0 ? (String)paramMap.get("SAPPHIRE_ReportID") : this.resolvedFileName(fileName);
                temp = FileUtil.createTempFile(prefix, ext).toFile();
                String logicalFileName = prefix + ext;
                this.jasperPrint = SapphireJasperUtil.getJasperPrint(this.getJasperReport(), (Map)paramMap, this.sapphireConnection.getConnection());
                List pages = this.jasperPrint.getPages();
                if (pages == null || pages.size() == 0) {
                    Trace.logInfo("Warning: Report" + this.reportid + " has no page. Do not email.");
                }
                SapphireJasperUtil.exportReportToFile(this.jasperPrint, displayType, temp.getPath(), paramMap);
                reportByte = Files.readAllBytes(temp.toPath());
                actionProps.put("filename", temp.getPath());
                actionProps.put("logicalfilename", logicalFileName);
            }
            catch (IOException e) {
                throw new SapphireException("PROCESSACTION_FAILED", "Could not create a temporary file " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
            try {
                if (reportevent != null && displayType.equalsIgnoreCase("pdf")) {
                    reportByte = this.getModifiedReportBytes(reportevent, reportByte);
                    File watermarkedFile = FileUtil.createTempFile(prefix, ext).toFile();
                    Files.write(watermarkedFile.toPath(), reportByte, new OpenOption[0]);
                    actionProps.put("filename", watermarkedFile.getPath());
                }
                ActionService ac = new ActionService(this.sapphireConnection);
                ac.processAction("SendMail", "1", actionProps);
                if (reportevent == null) break block25;
                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportByte);){
                    reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
                }
            }
            catch (ServiceException | IOException e) {
                throw new SapphireException("Failed to send generated report", e);
            }
            finally {
                SapphireJasperUtil.deleteFile(temp);
            }
        }
    }

    @Override
    public void sendReportToFile(HashMap paramMap, String displayType, String fileName) throws SapphireException {
        if (!this.getSupportedDisplayTypes().contains(displayType)) {
            throw new SapphireException("CONFIGURATION_ERROR", "Could not process the report '" + this.reportid + "': The specified file type '" + displayType + "' is not supported.");
        }
        SapphireReportEvent reportevent = this.sendReportToFileEvent(paramMap, displayType, fileName);
        try {
            this.jasperPrint = SapphireJasperUtil.getJasperPrint(this.getJasperReport(), (Map)paramMap, this.sapphireConnection.getConnection());
            SapphireJasperUtil.exportReportToFile(this.jasperPrint, displayType, fileName, paramMap);
            Path filePath = new File(fileName).toPath();
            byte[] reportByte = Files.readAllBytes(new File(fileName).toPath());
            if (this.isControlledReport() && reportevent != null) {
                if (displayType.equalsIgnoreCase("pdf")) {
                    reportByte = this.getModifiedReportBytes(reportevent, reportByte);
                    Files.deleteIfExists(filePath);
                    Files.write(filePath, reportByte, new OpenOption[0]);
                }
                reportevent.setReportStream(new ByteArrayInputStream(reportByte));
                reportevent.saveEvent(this.sapphireConnection);
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    @Override
    public void sendReportToPrinter(HashMap paramMap, String displayType, String printer, String addressid, String addresstype) throws SapphireException {
        byte[] reportByte = null;
        SapphireReportEvent reportevent = this.sendReportToPrinterEvent(paramMap, displayType, printer, addressid, addresstype);
        try {
            this.jasperPrint = SapphireJasperUtil.getJasperPrint(this.getJasperReport(), (Map)paramMap, this.sapphireConnection.getConnection());
            boolean isIgnorePagination = this.jasperReport.isIgnorePagination();
            if (isIgnorePagination) {
                reportByte = SapphireJasperUtil.exportAndPrintReport(this.jasperPrint, printer);
            } else {
                SapphireJasperUtil.exportReportToPrinter(this.jasperPrint, printer);
                paramMap.put("displayType", displayType);
                reportByte = SapphireJasperUtil.getReportBytes(paramMap, this.jasperPrint);
            }
            if (reportevent != null) {
                if (displayType.equalsIgnoreCase("pdf") && !reportevent.isDigitallysigned().equalsIgnoreCase("Y") && this.signingmode != null && (this.signingmode.equals("Automatic") || this.signingmode.equals("With Report Confirmation") && this.initialdisposition != null && this.initialdisposition.equals("Confirmed"))) {
                    reportByte = this.signReport(reportByte);
                    reportevent.setDigitallysigned("Y");
                }
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportByte);
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
                byteArrayInputStream.close();
            }
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public byte[] getReportByteForWeb(HashMap paramsMap, String displayType, HttpServletRequest request, HttpServletResponse response, String fileName, boolean isFile, String languageid, String timezone) throws SapphireException {
        SapphireReportEvent reportevent = this.runReportToWebEvent(paramsMap, displayType, response, isFile);
        byte[] reportBytes = null;
        try {
            reportBytes = this.getReportBytes(paramsMap, displayType, request);
            switch (displayType) {
                case "pdf": {
                    if (!this.isControlledReport() || reportevent == null) break;
                    reportBytes = this.getModifiedReportBytes(reportevent, reportBytes);
                }
            }
        }
        catch (Exception exp) {
            throw new SapphireException("Error running report:" + ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), exp);
        }
        return reportBytes;
    }
}

