/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.pdfview.PDFFile
 *  com.sun.pdfview.PDFPrintPage
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.http.HttpEntity
 *  org.apache.http.HttpResponse
 *  org.apache.http.client.methods.HttpGet
 *  org.apache.http.client.methods.HttpPost
 *  org.apache.http.client.methods.HttpPut
 *  org.apache.http.client.methods.HttpRequestBase
 *  org.apache.http.client.methods.HttpUriRequest
 *  org.apache.http.entity.StringEntity
 *  org.apache.http.impl.client.DefaultHttpClient
 *  org.apache.http.util.EntityUtils
 */
package com.labvantage.sapphire.report.bo;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPrintPage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.OrientationRequested;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class SapphireBOUtil {
    private String boWebiDateFormat;
    private String bousername;
    private String bopassword;
    private String bourl;
    private String boauthenticationtype;
    private String bocmsname;
    private String reportType = null;
    private ConnectionInfo connectionInfo = null;
    private String actualReportID;
    private String logonToken;
    private String DOCUMENT_SERVICE_API_URL = "/biprws/raylight/v1/documents/";
    private String BI_PLATFORM_SERVICE_API_URL = "/biprws/v1/";
    public static final String GET_REQUEST = "GET";
    public static final String POST_REQUEST = "POST";
    public static final String PUT_REQUEST = "PUT";
    public static final String APPLICATION_XML_UTF_8 = "application/xml; charset=utf-8";
    public static final String JSON_UTF_8 = "application/json; charset=utf-8";
    public static final String MICROSOFT_EXCEL = "application/vnd.ms-excel";
    public static final String PDF = "application/pdf";
    public static final String HTML_UTF_8 = "text/html; charset=utf-8";

    public SapphireBOUtil() {
    }

    public SapphireBOUtil(ConnectionInfo connInfo) throws SapphireException {
        this.connectionInfo = connInfo;
        try {
            long start = System.currentTimeMillis();
            ConfigurationProcessor cp = new ConfigurationProcessor(this.connectionInfo.getConnectionId());
            this.setBousername(cp.getProfileProperty("bousername"));
            this.setBopassword(EncryptDecrypt.decrypt(cp.getProfileProperty("bopassword"), this.connectionInfo.getDatabaseId()));
            this.setBourl(cp.getProfileProperty("bourl"));
            this.setBocmsname(cp.getProfileProperty("bocmsname"));
            this.setBoWebiDateFormat(cp.getProfileProperty("bowebidateformat", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            this.setBoauthenticationtype("sec" + cp.getProfileProperty("boauthenticationtype"));
            String string = cp.getProfileProperty("boconnectiontimeout", "");
        }
        catch (Exception e) {
            throw new SapphireException("CONFIGURATION_ERROR", "BO Server is Down or  Login Failed Due to Wrong Profile Parameter" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    public void setBoWebiDateFormat(String boWebiDateFormat) {
        this.boWebiDateFormat = boWebiDateFormat;
    }

    public String getBousername() {
        return this.bousername;
    }

    public void setBousername(String bousername) {
        this.bousername = bousername;
    }

    public String getBopassword() {
        return this.bopassword;
    }

    public void setBopassword(String bopassword) {
        this.bopassword = bopassword;
    }

    public String getBourl() {
        return this.bourl;
    }

    public void setBourl(String bourl) {
        this.bourl = bourl;
    }

    public String getBoauthenticationtype() {
        return this.boauthenticationtype;
    }

    public void setBoauthenticationtype(String boauthenticationtype) {
        this.boauthenticationtype = boauthenticationtype;
    }

    public String getBocmsname() {
        return this.bocmsname;
    }

    public void setBocmsname(String bocmsname) {
        this.bocmsname = bocmsname;
    }

    public Map<String, JSONObject> getReportListMap(String reportPath) throws SapphireException {
        this.actualReportID = reportPath.substring(reportPath.lastIndexOf("/") + 1);
        HashMap<String, JSONObject> allReportMap = new HashMap<String, JSONObject>();
        ArrayList<String> folderNames = new ArrayList<String>();
        if (reportPath.charAt(0) == '/') {
            reportPath = reportPath.substring(1);
        }
        if (reportPath.contains("/")) {
            folderNames.addAll(Arrays.asList(StringUtil.split(reportPath, "/")));
        } else {
            folderNames.add(reportPath);
        }
        ConfigurationProcessor configuration = new ConfigurationProcessor(this.connectionInfo.getConnectionId());
        String rootFolderName = configuration.getProfileProperty("borootfoldername", "Root Folder");
        try {
            String urltoGetInfoStore = this.getBourl() + this.BI_PLATFORM_SERVICE_API_URL + "infostore";
            String rootFolderID = this.getFolderID(rootFolderName, urltoGetInfoStore);
            Map<String, JSONObject> foldersUnderRootFolder = this.getChildrens(urltoGetInfoStore, rootFolderID, "folder");
            this.traverseFolder(urltoGetInfoStore, folderNames, foldersUnderRootFolder, allReportMap, 1);
        }
        catch (Exception e) {
            throw new SapphireException("CONFIGURATION_ERROR", e.getMessage());
        }
        return allReportMap;
    }

    public Map getReportList(String reportPath) throws JSONException, SapphireException {
        HashMap<String, String> reports = new HashMap<String, String>();
        for (Map.Entry<String, JSONObject> report : this.getReportListMap(reportPath).entrySet()) {
            reports.put(report.getKey(), report.getValue().getString("type"));
        }
        return reports;
    }

    private Map<String, JSONObject> getChildrens(String url, String folderID, String category) throws IOException, SapphireException, JSONException {
        HashMap<String, JSONObject> children = new HashMap<String, JSONObject>();
        HttpResponse response = this.handleRequest(url + "/" + folderID + "/children", GET_REQUEST, null, this.populateCommonHeaderParam(), APPLICATION_XML_UTF_8, JSON_UTF_8);
        this.handleError(response);
        String responseString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
        JSONObject jsonObject = new JSONObject(responseString);
        if (jsonObject != null) {
            JSONArray infoObjects = jsonObject.getJSONArray("entries");
            for (int i = 0; i < infoObjects.length(); ++i) {
                if (OpalUtil.isNotEmpty(category)) {
                    if (!((JSONObject)infoObjects.get(i)).getString("type").equalsIgnoreCase(category)) continue;
                    children.put(((JSONObject)infoObjects.get(i)).getString("name").toLowerCase(), (JSONObject)infoObjects.get(i));
                    continue;
                }
                children.put(((JSONObject)infoObjects.get(i)).getString("name").toLowerCase(), (JSONObject)infoObjects.get(i));
            }
        }
        return children;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void traverseFolder(String url, List<String> folderNames, Map<String, JSONObject> childFolders, Map<String, JSONObject> allReportMap, int count) throws SapphireException, JSONException, IOException {
        if (folderNames.size() == count) {
            JSONObject folderInfo = childFolders.get(folderNames.get(count - 1).toLowerCase());
            if (null == folderInfo) throw new SapphireException("Folder not found");
            String folderpath = "/" + String.join((CharSequence)"/", folderNames);
            String folderIdName = folderInfo.getString("id") + "%3B" + folderpath;
            this.getWebiReportsInFolder(url, folderIdName, allReportMap);
            return;
        } else {
            if (!childFolders.containsKey(folderNames.get(count - 1).toLowerCase())) return;
            JSONObject folderInfo = childFolders.get(folderNames.get(count - 1).toLowerCase());
            String folderId = folderInfo.getString("id");
            Map<String, JSONObject> subFolders = this.getChildrens(url, folderId, "folder");
            this.traverseFolder(url, folderNames, subFolders, allReportMap, count + 1);
        }
    }

    private void getWebiReportsInFolder(String url, String folderIdName, Map<String, JSONObject> allReportMap) throws IOException, SapphireException, JSONException {
        String folderID = StringUtil.split(folderIdName, "%3B")[0];
        String folderName = StringUtil.split(folderIdName, "%3B")[1];
        Map<String, JSONObject> allChilds = this.getChildrens(url, folderID, "");
        for (Map.Entry<String, JSONObject> child : allChilds.entrySet()) {
            if (child.getValue().getString("type").equalsIgnoreCase("Folder")) {
                this.getWebiReportsInFolder(url, child.getValue().getString("id") + "%3B" + folderName + "/" + child.getValue().getString("name"), allReportMap);
                continue;
            }
            if (!child.getValue().getString("type").equalsIgnoreCase("WEBI")) continue;
            allReportMap.put(folderName + "/" + child.getValue().getString("name"), child.getValue());
        }
    }

    private String getFolderID(String folderName, String url) throws IOException, SapphireException, JSONException {
        HttpResponse response = this.handleRequest(url, GET_REQUEST, null, this.populateCommonHeaderParam(), APPLICATION_XML_UTF_8, JSON_UTF_8);
        this.handleError(response);
        String responseString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
        JSONObject jsonObject = new JSONObject(responseString);
        String rootFolderID = "";
        if (jsonObject != null) {
            JSONArray infoObjects = jsonObject.getJSONArray("entries");
            for (int i = 0; i < infoObjects.length(); ++i) {
                String name = ((JSONObject)infoObjects.get(i)).getString("name");
                if (!name.equalsIgnoreCase(folderName)) continue;
                rootFolderID = ((JSONObject)infoObjects.get(i)).getString("id");
                break;
            }
        }
        return rootFolderID;
    }

    public Map<String, String> getReportInfo(String reportpath) throws SapphireException, JSONException, IOException {
        HashMap<String, String> reportinfo = new HashMap<String, String>();
        String selectedReportName = reportpath.substring(reportpath.lastIndexOf("/") + 1);
        Map<String, JSONObject> reports = this.getReportListMap(reportpath.substring(0, reportpath.lastIndexOf("/")));
        for (Map.Entry<String, JSONObject> report : reports.entrySet()) {
            String reportName = report.getKey().contains("/") ? report.getKey().substring(report.getKey().lastIndexOf("/") + 1) : report.getKey();
            if (!reportName.equalsIgnoreCase(selectedReportName)) continue;
            String reportid = report.getValue().getString("id");
            reportinfo.put(reportid, this.getBOXIReportType(reportid));
            break;
        }
        return reportinfo;
    }

    public List<String> getBOReportParameter(String reportpath) throws SapphireException, JSONException, IOException {
        List<String> reportParameter = null;
        Map<String, String> reportinfo = this.getReportInfo(reportpath);
        String reportid = "";
        for (Map.Entry<String, String> report : reportinfo.entrySet()) {
            this.setReportType(report.getValue());
            reportid = report.getKey();
        }
        try {
            reportParameter = this.getReportParameterList(reportid);
        }
        catch (Exception e) {
            throw new SapphireException("CONFIGURATION_ERROR", e.getMessage());
        }
        return reportParameter;
    }

    private String getBOXIReportType(String reportid) throws IOException, SapphireException, JSONException {
        String boxiType = "";
        String url = this.getBourl() + this.BI_PLATFORM_SERVICE_API_URL + "infostore/" + reportid;
        HttpResponse response = this.handleRequest(url, GET_REQUEST, null, this.populateCommonHeaderParam(), APPLICATION_XML_UTF_8, JSON_UTF_8);
        this.handleError(response);
        String responseString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
        JSONObject jsonObject = new JSONObject(responseString);
        if (jsonObject != null) {
            boxiType = jsonObject.getString("type");
        }
        return boxiType;
    }

    public void runReportToWeb(HashMap requestProps, HttpServletResponse response, String fileName, boolean isFile) throws SapphireException {
        try {
            String reportName = requestProps.get("SAPPHIRE_ReportID").toString();
            String reportVersionid = requestProps.get("SAPPHIRE_ReportVersion").toString();
            String format = requestProps.get("mode").toString();
            byte[] reportBytes = this.getBinaryStream(reportName, reportVersionid, format, requestProps);
            if (isFile) {
                response.setContentType("application/x-download");
            } else {
                switch (format) {
                    case "rtf": {
                        response.setContentType("application/msword");
                        break;
                    }
                    case "excel": 
                    case "xls": {
                        response.setContentType(MICROSOFT_EXCEL);
                        break;
                    }
                    case "pdf": {
                        response.setContentType(PDF);
                        break;
                    }
                    case "html": {
                        response.setContentType(HTML_UTF_8);
                        break;
                    }
                    default: {
                        throw new SapphireException("CONFIGURATION_ERROR", "Could not process the report '" + reportName + "': The specified display type '" + format + "' is not supported.");
                    }
                }
            }
            response.setContentLength(reportBytes.length);
            if (reportName != null && reportName.length() > 0) {
                String uesrid = this.connectionInfo.getSysuserId();
                reportName = reportName + "_" + uesrid + "_" + new SimpleDateFormat("MMM-dd-yyyy-hh-mm-ss").format(new Date());
                reportName = HttpUtil.encodeURIComponent(reportName);
                if (isFile && OpalUtil.isNotEmpty(fileName)) {
                    response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
                } else if (format.equalsIgnoreCase("pdf")) {
                    response.setHeader("Content-Disposition", "inline; filename=" + reportName + (!reportName.endsWith("pdf") ? ".pdf" : ""));
                } else if (format.equalsIgnoreCase("xls") || format.equalsIgnoreCase("excel")) {
                    response.setHeader("Content-Disposition", "inline; filename=" + reportName + (!reportName.endsWith("xls") ? ".xls" : ""));
                } else if (format.equalsIgnoreCase("rtf")) {
                    response.setHeader("Content-Disposition", "inline; filename=" + reportName + (!reportName.endsWith("rtf") ? ".rtf" : ""));
                }
            }
            if (format.equalsIgnoreCase("pdf")) {
                String initialdisposition = (String)requestProps.get("initialdisposition");
                String watermarkflag = (String)requestProps.get("watermarkflag");
                if (watermarkflag == null || watermarkflag.length() == 0) {
                    watermarkflag = "Y";
                }
                if (initialdisposition != null && initialdisposition.length() > 0 && initialdisposition.equals("Pending") && watermarkflag != null && watermarkflag.equals("Y")) {
                    reportBytes = SapphireReport.addWatermark(reportBytes, "Pending Confirmation");
                } else if (initialdisposition != null && initialdisposition.length() > 0 && initialdisposition.equals("Confirmed")) {
                    reportBytes = SapphireReport.removeWatermark(reportBytes);
                }
            }
            requestProps.put("reportBytes", reportBytes);
            ServletOutputStream ouputStream = response.getOutputStream();
            ouputStream.write(reportBytes);
            ouputStream.flush();
            ouputStream.close();
        }
        catch (Exception e) {
            Trace.logInfo(e.getMessage());
            throw new SapphireException(e.getMessage());
        }
    }

    public void runReportToWeb(byte[] reportbytes, HttpServletResponse response, String displayType) throws SapphireException {
        try {
            if (displayType.equalsIgnoreCase("excel") || displayType.equalsIgnoreCase("xls")) {
                displayType = "vnd.ms-excel";
            }
            response.setContentType("application/" + displayType);
            response.setContentLength(reportbytes.length);
            ServletOutputStream ouputStream = response.getOutputStream();
            ouputStream.write(reportbytes, 0, reportbytes.length);
            ouputStream.flush();
            ouputStream.close();
        }
        catch (Exception e) {
            Trace.logInfo(e.getMessage());
        }
    }

    public byte[] getBinaryStream(String reportName, String reportVersionid, String format, HashMap requestProps) throws SapphireException {
        String repID = this.getDocumentID(reportName, reportVersionid);
        return this.getDocumentDetails(repID, requestProps);
    }

    public void sendToEmail(String reportid, String reportVersionid, HashMap paramMap, String displayType, String emailfrom, String emailtolist, String emailmcclist, String emailsubject, String emailmessage, String fileName, SapphireConnection sapphireConnection) throws SapphireException, JSONException, IOException {
        String reportPath = this.getReportPath(reportid, reportVersionid);
        Map<String, String> reportinfo = this.getReportInfo(reportPath);
        String documentid = "";
        for (Map.Entry<String, String> report : reportinfo.entrySet()) {
            this.setReportType(report.getValue());
            documentid = report.getKey();
        }
        try {
            if (fileName == null || fileName.length() == 0) {
                fileName = this.getFileName(reportid, displayType);
            }
            if (this.getReportType().equalsIgnoreCase("Webi")) {
                if (!(displayType.equalsIgnoreCase("excel") || displayType.equalsIgnoreCase("xls") || displayType.equalsIgnoreCase("PDF"))) {
                    throw new SapphireException("CONFIGURATION_ERROR", "Could not process the report '" + reportid + "': The specified file type '" + displayType + "' is not supported.");
                }
                this.scheduleToSMTP(documentid, displayType, fileName, paramMap, emailtolist, emailfrom, emailmcclist, "", emailsubject, emailmessage);
            }
        }
        catch (Exception e) {
            throw new SapphireException("CONFIGURATION_ERROR", e.getMessage());
        }
    }

    private void printWebiReport(String reportName, String reportVersionid, HashMap paramMap, String displayType, String myPrinterName) throws SapphireException {
        try {
            Book book;
            PrinterJob pjob;
            byte[] byteArr = this.getBinaryStream(reportName, reportVersionid, displayType, paramMap);
            String repID = this.getDocumentID(reportName, reportVersionid);
            String pageOrientation = this.getReportPageOrientation(repID);
            ByteBuffer bb = ByteBuffer.wrap(byteArr);
            PDFFile pdfFile = new PDFFile(bb);
            PDFPrintPage pages = new PDFPrintPage(pdfFile);
            PrintService myPrinterService = null;
            PrintService[] ps = PrinterJob.lookupPrintServices();
            for (int i = 0; i < ps.length; ++i) {
                String printerName = ps[i].getName();
                if (!printerName.equalsIgnoreCase(myPrinterName)) continue;
                myPrinterService = ps[i];
                break;
            }
            if (myPrinterService != null) {
                pjob = PrinterJob.getPrinterJob();
                pjob.setPrintService(myPrinterService);
                PageFormat pf = pjob.defaultPage();
                pjob.setJobName(reportName);
                book = new Book();
                book.append((Printable)pages, pf, pdfFile.getNumPages());
                HashPrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
                if (pageOrientation != null && !pageOrientation.isEmpty()) {
                    switch (pageOrientation) {
                        case "Landscape": {
                            printRequestAttributeSet.add(OrientationRequested.LANDSCAPE);
                            break;
                        }
                        case "Portrait": {
                            printRequestAttributeSet.add(OrientationRequested.PORTRAIT);
                            break;
                        }
                        default: {
                            printRequestAttributeSet.add(OrientationRequested.PORTRAIT);
                        }
                    }
                }
                Paper paper = new Paper();
                paper.setImageableArea(0.0, 0.0, 700.0, 800.0);
                for (int pi = 0; pi < pdfFile.getNumPages(); ++pi) {
                    pf = pjob.getPageFormat(printRequestAttributeSet);
                    pf.setPaper(paper);
                    book.setPage(pi, (Printable)pages, pf);
                }
            } else {
                throw new SapphireException("Invalid print service name:" + myPrinterName);
            }
            pjob.setPageable(book);
            pjob.print();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void sendToPrinter(String reportid, String reportVersionid, HashMap paramMap, String displayType, String printerName) throws SapphireException {
        this.setReportType(paramMap.get("reporttype") != null ? paramMap.get("reporttype").toString() : "");
        try {
            if (this.getReportType().equalsIgnoreCase("Webi")) {
                this.printWebiReport(reportid, reportVersionid, paramMap, displayType, printerName);
            }
        }
        catch (Exception e) {
            throw new SapphireException("CONFIGURATION_ERROR", e.getMessage());
        }
        Trace.logInfo("The BO XI report " + reportid + " has been scheduled to Printer.");
    }

    public void sendToFile(String reportid, String reportVersionid, HashMap paramMap, String displayType, String fileName) throws SapphireException, JSONException, IOException {
        String reportPath = this.getReportPath(reportid, reportVersionid);
        Map<String, String> reportinfo = this.getReportInfo(reportPath);
        String documentid = "";
        for (Map.Entry<String, String> report : reportinfo.entrySet()) {
            this.setReportType(report.getValue());
            documentid = report.getKey();
        }
        try {
            if (this.getReportType().equalsIgnoreCase("Webi")) {
                if (!(displayType.equalsIgnoreCase("excel") || displayType.equalsIgnoreCase("xls") || displayType.equalsIgnoreCase("PDF"))) {
                    throw new SapphireException("CONFIGURATION_ERROR", "Could not process the report '" + reportid + "': The specified file type '" + displayType + "' is not supported.");
                }
                this.scheduleToFileDestination(documentid, displayType, fileName, paramMap);
            }
        }
        catch (Exception e) {
            throw new SapphireException("CONFIGURATION_ERROR", e.getMessage());
        }
    }

    private void scheduleToFileDestination(String documentid, String displayType, String fileName, HashMap paramMap) throws SapphireException, JSONException, IOException {
        String filePath = "";
        if (fileName.lastIndexOf(File.separator) > 0) {
            filePath = fileName.substring(0, fileName.lastIndexOf(File.separator));
        }
        fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
        String url = this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentid + "/schedules";
        JSONObject paramJson = this.populateParameterValue(this.getReportParameterInfo(documentid), paramMap);
        StringBuilder postbody = new StringBuilder();
        postbody.append("<name>CrystalEnterprise.DiskUnmanaged</name>").append("<format @type=\"" + displayType + "\"/>").append("<destination @keepInstanceInHistory=\"true\">").append("<useSpecificName>" + fileName + "</useSpecificName>").append("<filesystem>").append("<directory>" + filePath + "</directory>").append("</filesystem>").append("</destination>");
        JSONObject postData = new JSONObject();
        postData.put("schedule", XML.toJSONObject(postbody.toString()));
        postData.getJSONObject("schedule").put("parameters", paramJson.getJSONObject("parameters"));
        HttpResponse response = this.handleRequest(url, POST_REQUEST, postData.toString(), this.populateCommonHeaderParam(), JSON_UTF_8, JSON_UTF_8);
        this.handleError(response);
        String responseString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
        Logger.logInfo(responseString);
    }

    private void scheduleToSMTP(String documentid, String displayType, String fileName, HashMap paramMap, String emailtolist, String emailfromlist, String emailcclist, String emailbcclist, String emailsubject, String message) throws SapphireException, JSONException, IOException, IllegalAccessException, NoSuchFieldException {
        String url = this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentid + "/schedules";
        JSONObject paramJson = this.populateParameterValue(this.getReportParameterInfo(documentid), paramMap);
        StringBuilder postbody = new StringBuilder();
        postbody.append("<name>CrystalEnterprise.SMTP</name>").append("<format @type=\"" + displayType + "\"/>").append("<destination @keepInstanceInHistory=\"true\">").append("<useSpecificName @fileExtension=\"false\"></useSpecificName>").append("<mail>").append("<from>" + emailfromlist + "</from>").append("<to>" + emailtolist + "</to>").append("<cc>" + emailcclist + "</cc>").append("<bcc>" + emailbcclist + "</bcc>").append("<subject>" + emailsubject + "</subject>").append("<message>" + message + "</message>").append("<addAttachment>true</addAttachment>").append("</mail>").append("</destination>");
        JSONObject postData = new JSONObject();
        postData.put("schedule", XML.toJSONObject(postbody.toString()));
        postData.getJSONObject("schedule").put("parameters", paramJson.getJSONObject("parameters"));
        postData.getJSONObject("schedule").getJSONObject("destination").getJSONObject("useSpecificName").put("$", fileName);
        JSONObject modifiedJSON = new JSONObject(postData.getJSONObject("schedule").getJSONObject("destination").toString());
        postData.getJSONObject("schedule").getJSONObject("destination").remove("mail");
        postData.getJSONObject("schedule").getJSONObject("destination").remove("useSpecificName");
        postData.getJSONObject("schedule").getJSONObject("destination").remove("@keepInstanceInHistory");
        Field changeMap = postData.getJSONObject("schedule").getJSONObject("destination").getClass().getDeclaredField("myHashMap");
        changeMap.setAccessible(true);
        changeMap.set(postData.getJSONObject("schedule").getJSONObject("destination"), new LinkedHashMap());
        changeMap.setAccessible(false);
        postData.getJSONObject("schedule").getJSONObject("destination").put("@keepInstanceInHistory", true);
        postData.getJSONObject("schedule").getJSONObject("destination").put("useSpecificName", modifiedJSON.get("useSpecificName"));
        postData.getJSONObject("schedule").getJSONObject("destination").put("mail", modifiedJSON.get("mail"));
        HttpResponse response = this.handleRequest(url, POST_REQUEST, postData.toString(), this.populateCommonHeaderParam(), JSON_UTF_8, JSON_UTF_8);
        this.handleError(response);
        String responseString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
        Logger.logInfo(responseString);
    }

    String getFileName(String filename, String fileformat) {
        String outputFileName = "";
        if ((fileformat.equalsIgnoreCase("excel") || fileformat.equalsIgnoreCase("xls") || fileformat.equalsIgnoreCase("ExcelDataOnly")) && filename.length() > 0) {
            outputFileName = filename + ".xls";
        } else if (fileformat.equalsIgnoreCase("PDF") && filename.length() > 0) {
            outputFileName = filename + ".pdf";
        } else if ((fileformat.equalsIgnoreCase("TextTabSeparated") || fileformat.equalsIgnoreCase("TextPlain")) && filename.length() > 0) {
            outputFileName = filename + ".txt";
        } else if (fileformat.equalsIgnoreCase("Word") && filename.length() > 0) {
            outputFileName = filename + ".doc";
        } else if (fileformat.equalsIgnoreCase("RTF") && filename.length() > 0) {
            outputFileName = filename + ".rtf";
        } else if (filename.length() > 0) {
            outputFileName = filename + ".rpt";
        }
        return outputFileName;
    }

    public String getReportPath(String reportid, String reportVersionid) {
        String reportPath = null;
        QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        if (reportid != null) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select librarydir from report where reportid=" + safeSQL.addVar(reportid) + " and reportversionid=" + safeSQL.addVar(reportVersionid);
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            reportPath = ds.getValue(0, "librarydir");
            ds.clear();
        }
        return reportPath;
    }

    public String getParamType(String reportid, String reportVersionid, String pname) {
        String ptype = null;
        QueryProcessor qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        if (reportid != null) {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT paramtype FROM reportparam WHERE reportid=" + safeSQL.addVar(reportid) + " AND reportversionid=" + safeSQL.addVar(reportVersionid) + " AND paraminto=" + safeSQL.addVar(pname);
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            ptype = ds.getValue(0, "paramtype");
            ds.clear();
        }
        return ptype;
    }

    public String getBoWebiDateFormat() {
        return this.boWebiDateFormat;
    }

    public String getReportType() {
        return this.reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String restApiLogon() throws SapphireException {
        try {
            HttpResponse response = null;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userName", this.getBousername());
            jsonObject.put("password", this.getBopassword());
            jsonObject.put("auth", this.getBoauthenticationtype());
            response = this.handleRequest(this.getBourl() + "/biprws/logon/long", POST_REQUEST, jsonObject.toString(), null, JSON_UTF_8, JSON_UTF_8);
            this.logonToken = response.getFirstHeader("X-SAP-LogonToken").getValue();
        }
        catch (Exception exp) {
            throw new SapphireException(exp.getMessage());
        }
        return this.logonToken;
    }

    public void restApiLogoff() throws SapphireException {
        try {
            if (this.logonToken != null) {
                this.handleRequest(this.getBourl() + "/biprws/logoff", POST_REQUEST, this.logonToken, this.populateCommonHeaderParam(), APPLICATION_XML_UTF_8, APPLICATION_XML_UTF_8);
                this.logonToken = null;
            }
        }
        catch (Exception exp) {
            throw new SapphireException(exp.getMessage());
        }
    }

    public String getDocumentID(String reportName, String reportVersionid) throws SapphireException {
        String docID = "";
        try {
            String reportPath = this.getReportPath(reportName, reportVersionid);
            for (Map.Entry<String, String> report : this.getReportInfo(reportPath).entrySet()) {
                docID = report.getKey();
            }
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage());
        }
        return docID;
    }

    private byte[] getDocumentDetails(String documentID, HashMap requestProps) throws SapphireException {
        try {
            byte[] res = null;
            if (documentID == null && "".equals(documentID)) {
                throw new Exception("Document ID Missing");
            }
            String responseType = null;
            Map<String, String> headerParams = this.populateCommonHeaderParam();
            HttpResponse response = null;
            Object mode = requestProps.get("mode");
            boolean pagination = false;
            if (null != mode && mode.toString().equalsIgnoreCase("pdf")) {
                responseType = PDF;
                pagination = true;
            } else if (null != mode && requestProps.get("mode").toString().equalsIgnoreCase("xls")) {
                responseType = MICROSOFT_EXCEL;
            } else if (null != mode && requestProps.get("mode").toString().equalsIgnoreCase("html")) {
                responseType = HTML_UTF_8;
            } else {
                responseType = PDF;
                pagination = true;
            }
            JSONObject parameterJson = this.getReportParameterInfo(documentID);
            response = this.handleRequest(this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentID + "/parameters", PUT_REQUEST, this.populateParameterValue(parameterJson, requestProps).toString(), headerParams, JSON_UTF_8, APPLICATION_XML_UTF_8);
            this.handleError(response);
            String url = "";
            response = this.handleRequest(this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentID + "/reports", GET_REQUEST, null, headerParams, APPLICATION_XML_UTF_8, JSON_UTF_8);
            List<String> reportIDs = this.getReportID(response);
            ArrayList<byte[]> reponses = new ArrayList<byte[]>();
            int length = 0;
            for (int i = 0; i < reportIDs.size(); ++i) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                url = pagination ? this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentID + "/reports/" + reportIDs.get(i) + "/pages" : this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentID + "/reports/" + reportIDs.get(i);
                HttpResponse reportResponse = this.handleRequest(url, GET_REQUEST, null, headerParams, APPLICATION_XML_UTF_8, responseType);
                this.handleError(reportResponse);
                reportResponse.getEntity().writeTo((OutputStream)baos);
                byte[] reportBytes = baos.toByteArray();
                length += reportBytes.length;
                reponses.add(reportBytes);
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(length);
            for (int i = 0; i < reponses.size(); ++i) {
                byteBuffer.put((byte[])reponses.get(i));
            }
            res = byteBuffer.array();
            return res;
        }
        catch (Exception exp) {
            throw new SapphireException("Failed to get document details::" + exp.getMessage());
        }
    }

    private Map<String, String> populateCommonHeaderParam() {
        HashMap<String, String> headerParams = new HashMap<String, String>();
        headerParams.put("X-SAP-LogonToken", this.logonToken);
        return headerParams;
    }

    private List<String> getReportParameterList(String documentID) throws SapphireException {
        try {
            JSONObject jsonObj = this.getReportParameterInfo(documentID);
            jsonObj = jsonObj.getJSONObject("parameters");
            ArrayList<String> values = new ArrayList<String>();
            if (((JSONArray)jsonObj.get("parameter")).length() > 0) {
                for (int i = 0; i < ((JSONArray)jsonObj.get("parameter")).length(); ++i) {
                    Object paramKey = ((JSONArray)jsonObj.get("parameter")).getJSONObject(i).get("technicalName");
                    values.add(paramKey.toString());
                }
            }
            return values;
        }
        catch (Exception exp) {
            throw new SapphireException(exp.getMessage());
        }
    }

    private JSONObject getReportParameterInfo(String documentID) throws SapphireException, JSONException {
        return new JSONObject(this.getReportParameterInfo(documentID, JSON_UTF_8));
    }

    private String getReportParameterInfo(String documentID, String outputFormat) throws SapphireException {
        try {
            Map<String, String> headerParams = this.populateCommonHeaderParam();
            HttpResponse response = this.handleRequest(this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentID + "/parameters", GET_REQUEST, null, headerParams, APPLICATION_XML_UTF_8, outputFormat);
            this.handleError(response);
            String responseString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
            return responseString;
        }
        catch (Exception exp) {
            throw new SapphireException(exp.getMessage());
        }
    }

    private JSONObject populateParameterValue(JSONObject parameterJson, HashMap requestProps) throws SapphireException {
        try {
            JSONArray jsonArray = parameterJson.getJSONObject("parameters").getJSONArray("parameter");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject parameter = jsonArray.getJSONObject(i);
                String paramID = parameter.get("technicalName").toString();
                jsonArray.getJSONObject(i).getJSONObject("answer").remove("info");
                String paramVal = null != requestProps.get(paramID) ? requestProps.get(paramID).toString() : "";
                String[] paramVals = paramVal.split(";");
                if (!jsonArray.getJSONObject(i).getJSONObject("answer").has("values")) {
                    jsonArray.getJSONObject(i).getJSONObject("answer").put("values", new JSONObject());
                }
                jsonArray.getJSONObject(i).getJSONObject("answer").getJSONObject("values").put("value", new JSONArray());
                for (int j = 0; j < paramVals.length; ++j) {
                    if (paramVals[j].equalsIgnoreCase("")) continue;
                    jsonArray.getJSONObject(i).getJSONObject("answer").getJSONObject("values").getJSONArray("value").put(j, paramVals[j]);
                }
            }
        }
        catch (JSONException jexp) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(jexp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
        return parameterJson;
    }

    private JSONObject getDataProviders(String documentID) throws SapphireException {
        try {
            Map<String, String> headerParams = this.populateCommonHeaderParam();
            HttpResponse response = this.handleRequest(this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentID + "/dataproviders", GET_REQUEST, null, headerParams, APPLICATION_XML_UTF_8, JSON_UTF_8);
            String responseString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
            return new JSONObject(responseString);
        }
        catch (Exception exp) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private String getReportPageOrientation(String documentID) throws SapphireException {
        String pageOrientation = "";
        try {
            JSONObject report;
            if (documentID == null && "".equals(documentID)) {
                throw new Exception("Document ID Missing");
            }
            HttpResponse response = null;
            Map<String, String> headerParams = this.populateCommonHeaderParam();
            response = this.handleRequest(this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentID + "/reports", GET_REQUEST, null, headerParams, APPLICATION_XML_UTF_8, JSON_UTF_8);
            List<String> reportIDs = this.getReportID(response);
            String reportID = OpalUtil.isNotEmpty(reportIDs) ? reportIDs.get(0) : "";
            response = this.handleRequest(this.getBourl() + this.DOCUMENT_SERVICE_API_URL + documentID + "/reports/" + reportID, GET_REQUEST, null, headerParams, APPLICATION_XML_UTF_8, JSON_UTF_8);
            String reportString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
            JSONObject reportJSON = new JSONObject(reportString);
            JSONObject jSONObject = report = null != reportJSON ? reportJSON.getJSONObject("report") : null;
            if (report != null) {
                pageOrientation = report.getJSONObject("pageSettings").getJSONObject("format").get("@orientation").toString();
            }
            return pageOrientation;
        }
        catch (Exception exp) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
    }

    private List<String> getReportID(HttpResponse response) throws SapphireException {
        ArrayList<String> reportID = new ArrayList<String>();
        try {
            JSONObject report;
            this.handleError(response);
            String reportString = EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8");
            JSONObject reportJSON = new JSONObject(reportString);
            JSONObject jSONObject = report = null != reportJSON ? reportJSON.getJSONObject("reports") : null;
            if (report != null) {
                for (int i = 0; i < report.getJSONArray("report").length(); ++i) {
                    reportID.add(report.getJSONArray("report").getJSONObject(i).get("id").toString());
                }
            }
        }
        catch (Exception exp) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
        }
        return reportID;
    }

    public HttpResponse handleRequest(String url, String method, String postBody, Map<String, String> headerParams, String contentType, String accept) throws IOException {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        switch (method) {
            case "GET": {
                HttpResponse httpGetResponse;
                HttpGet getRequest = new HttpGet(url);
                if (headerParams.size() > 0) {
                    for (Map.Entry<String, String> val : headerParams.entrySet()) {
                        getRequest.setHeader(val.getKey(), val.getValue());
                    }
                }
                this.setBasicHeaderParameter(contentType, accept, (HttpRequestBase)getRequest);
                response = httpGetResponse = httpclient.execute((HttpUriRequest)getRequest);
                break;
            }
            case "POST": {
                HttpResponse httpPostResponse;
                HttpPost postRequest = new HttpPost(url);
                StringEntity postEntity = new StringEntity(postBody, "UTF-8");
                postEntity.setContentType(contentType);
                if (null != headerParams && headerParams.size() > 0) {
                    for (Map.Entry<String, String> val : headerParams.entrySet()) {
                        postRequest.setHeader(val.getKey(), val.getValue());
                    }
                }
                this.setBasicHeaderParameter(contentType, accept, (HttpRequestBase)postRequest);
                postRequest.setEntity((HttpEntity)postEntity);
                response = httpPostResponse = httpclient.execute((HttpUriRequest)postRequest);
                break;
            }
            case "PUT": {
                HttpResponse httpPutResponse;
                HttpPut putRequest = new HttpPut(url);
                StringEntity putEntity = new StringEntity(postBody, "UTF-8");
                putEntity.setContentType(contentType);
                if (headerParams.size() > 0) {
                    for (Map.Entry<String, String> val : headerParams.entrySet()) {
                        putRequest.setHeader(val.getKey(), val.getValue());
                    }
                }
                this.setBasicHeaderParameter(contentType, accept, (HttpRequestBase)putRequest);
                putRequest.setEntity((HttpEntity)putEntity);
                response = httpPutResponse = httpclient.execute((HttpUriRequest)putRequest);
                break;
            }
        }
        return response;
    }

    private void setBasicHeaderParameter(String contentType, String accept, HttpRequestBase request) {
        if (null != accept && !accept.equalsIgnoreCase("")) {
            request.setHeader("Accept", accept);
        }
        request.setHeader("Content-Type", contentType);
    }

    private void handleError(HttpResponse response) throws SapphireException {
        int responseCode = response.getStatusLine().getStatusCode();
        if (responseCode != 200) {
            try {
                throw new SapphireException("Error while generating BO Report, Reason: " + EntityUtils.toString((HttpEntity)response.getEntity(), (String)"UTF-8"));
            }
            catch (Exception exp) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(exp, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())));
            }
        }
    }
}

