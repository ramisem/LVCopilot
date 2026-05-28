/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServlet
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.JspFactory
 *  javax.servlet.jsp.PageContext
 *  net.sf.jasperreports.engine.JasperPrint
 *  net.sf.jasperreports.engine.JasperReport
 */
package com.labvantage.sapphire.servlet.command;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.modules.adhocbrowser.ReportGenerator;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.report.ReportConstants;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.bo.SapphireBOReport;
import com.labvantage.sapphire.report.bo.SapphireBOUtil;
import com.labvantage.sapphire.report.jasper.JasperReportPropertyHandler;
import com.labvantage.sapphire.report.jasper.SapphireJasperReport;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.report.nwa.SapphireNWAReport;
import com.labvantage.sapphire.report.nwa.SapphireNWAUtil;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.PseudoPageContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import sapphire.SapphireException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.LogContext;
import sapphire.util.Logger;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ViewReport
implements ReportConstants {
    protected String logName = this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1).toUpperCase();
    protected Logger logger = new Logger(new LogContext(this.logName, "(none)"));

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void processRequest(Servlet servlet, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        block83: {
            String reportid = request.getParameter("reportid");
            String reportversionid = request.getParameter("reportversionid");
            String webpageid = request.getParameter("pageid");
            String category = request.getParameter("category");
            String displayType = request.getParameter("displaytype");
            RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
            String reporteventid = request.getParameter("reporteventid");
            String regenerateflag = request.getParameter("regenerate");
            String timezone = request.getParameter("timezone") != null ? request.getParameter("timezone").trim() : "";
            String createNewReportEventVersion = request.getParameter("createNewVersion");
            String reportaddressid = request.getParameter("reportaddressid");
            String printerName = null;
            String emailAddress = "";
            String fileName = "";
            String filePath = "";
            ConnectionInfo connectionInfo = new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionId());
            String languageid = request.getParameter("languageid") != null ? request.getParameter("languageid").trim() : (OpalUtil.isNotEmpty(connectionInfo.getLanguage()) ? connectionInfo.getLanguage() : "");
            boolean isViewOnLocalWindow = Boolean.valueOf(request.getParameter("viewonlocalwindow"));
            boolean isPrintToPrinter = Boolean.valueOf(request.getParameter("printer_option"));
            boolean isEmail = Boolean.valueOf(request.getParameter("email_to_address_option"));
            boolean isFile = Boolean.valueOf(request.getParameter("export_to_file_option"));
            if (isPrintToPrinter) {
                printerName = request.getParameter("printer_name");
            } else if (isEmail) {
                emailAddress = request.getParameter("email_address");
                fileName = request.getParameter("filename_param");
            } else if (isFile) {
                filePath = request.getParameter("file_path") == null ? "" : request.getParameter("file_path");
                fileName = request.getParameter("filename_param");
            }
            String strParams = request.getParameter("parameters");
            PropertyList propertyList = new PropertyList();
            if (strParams != null && strParams.length() >= 0) {
                propertyList = this.getParamMap(propertyList, strParams, request, servletContext);
            }
            Enumeration enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String paramKey = (String)enumeration.nextElement();
                propertyList.setProperty(paramKey, request.getParameter(paramKey));
            }
            try {
                List supportedDisplayTypes;
                SapphireReport sr = null;
                String reporttype = "";
                if (reportid != null && reportid.length() > 0) {
                    sr = SapphireReport.getIntance(reportid, reportversionid, connectionInfo, languageid, isFile, request.getParameter("output_format"));
                } else if (reporteventid != null && reporteventid.length() > 0) {
                    sr = SapphireReport.getIntanceReportEventid(reporteventid, connectionInfo);
                } else if (category == null || category.length() <= 0) {
                    sr = new SapphireJasperReport("J");
                    sr.setConnectionInfo(connectionInfo);
                    sr.init();
                }
                if (Optional.ofNullable(sr).isPresent()) {
                    reporttype = sr.getReporttypeflag();
                }
                if ((displayType == null || displayType.length() == 0) && sr != null) {
                    displayType = this.getDisplayType(reportid, reportversionid, reporteventid, requestContext, request, sr);
                }
                if (fileName != null && fileName.trim().length() > 0) {
                    fileName = fileName.contains("[currentuser]") ? StringUtil.replaceAll(fileName, "[currentuser]", connectionInfo.getSysuserId()) : fileName;
                    fileName = (filePath != null && filePath.length() > 0 ? filePath + File.separator : "") + fileName + (displayType != null && displayType.length() > 0 && !fileName.endsWith(displayType) ? "." + displayType : "");
                }
                if (isPrintToPrinter) {
                    this.populatePropertyForPrint(reportid, reportversionid, reporttype, displayType, languageid, timezone, printerName, reportaddressid, propertyList);
                    ActionProcessor ap = new ActionProcessor(requestContext.getConnectionId());
                    ap.processAction("GenerateReport", "1", propertyList);
                    break block83;
                }
                if (isEmail) {
                    this.populatePropertyForEmail(requestContext, reportid, reportversionid, reporttype, displayType, languageid, timezone, emailAddress, fileName, reportaddressid, propertyList);
                    ActionProcessor ap = new ActionProcessor(requestContext.getConnectionId());
                    ap.processAction("GenerateReport", "1", propertyList);
                    break block83;
                }
                if (reportid != null && reportid.length() > 0 && (supportedDisplayTypes = sr.getSupportedDisplayTypes()) != null && supportedDisplayTypes.size() > 0 && !supportedDisplayTypes.contains(displayType)) {
                    throw new SapphireException("CONFIGURATION_ERROR", "Could not process the report '" + reportid + "': The specified display type '" + displayType + "' is not supported.");
                }
                if (reporteventid != null && reporteventid.length() > 0 && regenerateflag != null && "Y".equalsIgnoreCase(regenerateflag)) {
                    QueryProcessor qp = new QueryProcessor(connectionInfo.getConnectionId());
                    DataSet paramvalueDS = qp.getPreparedSqlDataSet("paramvalueds", "SELECT paramid,paramvalueclob, paramtypeflag FROM reporteventparam WHERE reporteventid=?", new Object[]{reporteventid}, true);
                    request.setAttribute("paramvalueds", (Object)paramvalueDS);
                    HashMap paramsMap = new HashMap();
                    switch (reporttype) {
                        case "J": 
                        case "K": {
                            SapphireJasperReport sjp = (SapphireJasperReport)sr;
                            JasperReport jasperReport = sjp.getJasperReport();
                            paramsMap = sjp.getReportParamMap(request);
                            paramsMap.put("jasperreport", jasperReport);
                            if (createNewReportEventVersion != null && createNewReportEventVersion.equals("Y")) {
                                paramsMap.put("createNewVersion", createNewReportEventVersion);
                                paramsMap.put("reporteventid", reporteventid);
                            }
                            sr.runReportToWeb(paramsMap, displayType, request, response, fileName, isFile);
                            break;
                        }
                        case "T": 
                        case "C": {
                            paramsMap = sr.getSimpleRequestParamsMap(request);
                            sr.runReportToWeb(paramsMap, displayType, request, response, fileName, isFile);
                            break;
                        }
                        case "X": {
                            SapphireBOReport sbrp = (SapphireBOReport)sr;
                            try {
                                sbrp.boUtil = new SapphireBOUtil(connectionInfo);
                                sbrp.boUtil.restApiLogon();
                                paramsMap = sbrp.getViewReportParamMap(request);
                                paramsMap.remove("SAPPHIRE_REPORT_TIMEZONE");
                                paramsMap.remove("SAPPHIRE_REPORT_LANGUAGE");
                                sr.runReportToWeb(paramsMap, displayType, request, response, fileName, isFile);
                                break block83;
                            }
                            finally {
                                sbrp.boUtil.restApiLogoff();
                            }
                        }
                        case "N": {
                            SapphireNWAReport snwa = (SapphireNWAReport)sr;
                            snwa.nwaUtil = new SapphireNWAUtil(connectionInfo);
                            paramsMap = snwa.getReportParamMap(request, propertyList);
                            sr.runReportToWeb(paramsMap, "png", request, response, fileName, isFile);
                            break;
                        }
                    }
                    break block83;
                }
                if (!(reporteventid == null || reporteventid.length() <= 0 || createNewReportEventVersion != null && createNewReportEventVersion.equals("Y"))) {
                    HashMap<String, String> props = new HashMap<String, String>();
                    props.put("mode", "storedreportprint");
                    props.put("reporteventid", reporteventid);
                    HashMap requestProps = new RequestProcessor(connectionInfo.getConnectionId()).processRequest(JasperReportPropertyHandler.class.getName(), props);
                    if (requestProps.get("errormessage") == null) {
                        SapphireReportEvent event = (SapphireReportEvent)requestProps.get("storedreportprint");
                        String reportName = OpalUtil.getColumnValue(new QueryProcessor(connectionInfo.getConnectionId()), "reportevent", "reportid", "reporteventid=?", new String[]{reporteventid});
                        event.redoEvent(connectionInfo, request, response, reportName, sr);
                    }
                    break block83;
                }
                if (!(reportid != null && reportid.length() != 0 || category != null && category.length() != 0)) {
                    if (webpageid != null && webpageid.length() > 0) {
                        SapphireJasperUtil.exportPage(request.getParameter("pageid"), request.getParameter("keyid1"), request.getParameter("keyid2"), request.getParameter("keyid3"), request, response);
                    } else if ("xls".equals(request.getParameter("displaytype")) || "xlsx".equals(request.getParameter("displaytype")) || "csv".equals(request.getParameter("displaytype"))) {
                        String contentDispostion = request.getParameter("contentDisposition");
                        long starttime = System.currentTimeMillis();
                        if (request.getParameter("starttime") != null) {
                            starttime = Long.parseLong(request.getParameter("starttime"));
                        }
                        response.setHeader("Content-Disposition", contentDispostion);
                        TranslationProcessor tp = new TranslationProcessor(requestContext.getConnectionid());
                        if ("xls".equals(request.getParameter("displaytype")) || "xlsx".equals(request.getParameter("displaytype"))) {
                            JasperPrint jasperPrint = (JasperPrint)ReportGenerator.getReportObject(request.getSession());
                            HashMap<String, String> reportProps = new HashMap<String, String>();
                            reportProps.put("displayType", request.getParameter("displaytype"));
                            SapphireJasperUtil.runReportToWebExcel(SapphireJasperUtil.getReportBytes(reportProps, jasperPrint), response, "AdhocExcelExport", connectionInfo, "", isFile, request.getParameter("displaytype"));
                            ReportGenerator.removeReportObject(request.getSession());
                            HashMap<String, String> token = new HashMap<String, String>();
                            token.put("number", "" + (System.currentTimeMillis() - starttime));
                            ReportGenerator.setExportSessionStatus(request.getSession(), tp.translate("Done Export to Excel in [number]ms", token) + "<br/><br/>", true);
                        } else {
                            response.setContentType("text/csv");
                            File csvFile = (File)ReportGenerator.getReportObject(request.getSession());
                            BufferedReader reader = null;
                            if (csvFile != null) {
                                try {
                                    String line;
                                    reader = new BufferedReader(new FileReader(csvFile));
                                    while ((line = reader.readLine()) != null) {
                                        response.getOutputStream().write((line + "\r\n").getBytes("UTF-8"));
                                    }
                                    reader.close();
                                    ReportGenerator.removeReportObject(request.getSession());
                                    HashMap<String, String> token = new HashMap<String, String>();
                                    token.put("number", "" + (System.currentTimeMillis() - starttime));
                                    ReportGenerator.setExportSessionStatus(request.getSession(), tp.translate("Done Export to CSV in [number]ms", token) + "<br/><br/>", true);
                                }
                                finally {
                                    if (reader != null) {
                                        reader.close();
                                    }
                                    csvFile.delete();
                                }
                            }
                        }
                    }
                    response.getOutputStream().close();
                    break block83;
                }
                boolean isShowLookup = false;
                if (!"submitselectedreport".equals(request.getParameter("mode")) && category != null && category.length() > 0) {
                    PrintWriter out;
                    DataSet ds = SapphireReport.getReportDataSet("", "", category, connectionInfo);
                    if (ds.getRowCount() >= 1) {
                        isShowLookup = true;
                        out = response.getWriter();
                        out.print(SapphireReport.getReportLookupHtml(ds, request, servletContext));
                        out.flush();
                    } else {
                        out = response.getWriter();
                        out.print("Error: No current report found for category " + category);
                        out.flush();
                    }
                }
                if (isShowLookup || reportid == null || reportid.length() <= 0) break block83;
                if (sr.isPromptRequired(request)) {
                    int p;
                    PrintWriter out = response.getWriter();
                    try {
                        Configuration config = Configuration.getInstance();
                        p = config.getPlatform();
                    }
                    catch (Exception e) {
                        throw new ServletException("Failed to obtain plaform", (Throwable)e);
                    }
                    PageContext pageContext = p == 3 && servlet instanceof HttpServlet ? new PseudoPageContext(((HttpServlet)servlet).getServletContext(), request, response) : JspFactory.getDefaultFactory().getPageContext(servlet, (ServletRequest)request, (ServletResponse)response, "", true, 0, false);
                    out.print(sr.getPromptHtml(pageContext, request, servletContext));
                    out.flush();
                } else {
                    HashMap paramsMap = new HashMap();
                    switch (reporttype) {
                        case "J": 
                        case "K": {
                            SapphireJasperReport sjp = (SapphireJasperReport)sr;
                            JasperReport jasperReport = sjp.getJasperReport();
                            paramsMap = sjp.getReportParamMap(request);
                            paramsMap.put("jasperreport", jasperReport);
                            if (createNewReportEventVersion != null && createNewReportEventVersion.equals("Y")) {
                                paramsMap.put("createNewVersion", createNewReportEventVersion);
                                paramsMap.put("reporteventid", reporteventid);
                            }
                            sr.runReportToWeb(paramsMap, displayType, request, response, fileName, isFile);
                            break;
                        }
                        case "E": {
                            if (createNewReportEventVersion != null && createNewReportEventVersion.equals("Y")) {
                                paramsMap.put("createNewVersion", createNewReportEventVersion);
                                paramsMap.put("reporteventid", reporteventid);
                            }
                            paramsMap.put("SAPPHIRE_REPORT_LANGUAGE", languageid);
                            paramsMap.put("SAPPHIRE_REPORT_TIMEZONE", timezone);
                            sr.runReportToWeb(paramsMap, displayType, request, response, fileName, isFile);
                            break;
                        }
                        case "T": 
                        case "C": {
                            paramsMap = sr.getSimpleRequestParamsMap(request);
                            sr.runReportToWeb(paramsMap, displayType, request, response, fileName, isFile);
                            break;
                        }
                        case "X": {
                            SapphireBOReport sbrp = (SapphireBOReport)sr;
                            try {
                                sbrp.boUtil = new SapphireBOUtil(connectionInfo);
                                sbrp.boUtil.restApiLogon();
                                paramsMap = sbrp.getViewReportParamMap(request);
                                paramsMap.remove("SAPPHIRE_REPORT_TIMEZONE");
                                paramsMap.remove("SAPPHIRE_REPORT_LANGUAGE");
                                sr.runReportToWeb(paramsMap, displayType, request, response, fileName, isFile);
                                break;
                            }
                            finally {
                                sbrp.boUtil.restApiLogoff();
                            }
                        }
                        case "N": {
                            SapphireNWAReport snwa = (SapphireNWAReport)sr;
                            snwa.nwaUtil = new SapphireNWAUtil(connectionInfo);
                            paramsMap = snwa.getReportParamMap(request, propertyList);
                            sr.runReportToWeb(paramsMap, "png", request, response, fileName, isFile);
                        }
                    }
                }
                sr.reset();
            }
            catch (SapphireException sexp) {
                throw new ServletException((Throwable)sexp);
            }
            catch (Throwable e) {
                Logger.logStackTrace(e);
                throw new ServletException("Could not process the report. ", e);
            }
        }
    }

    private void populatePropertyForEmail(RequestContext requestContext, String reportid, String reportversionid, String reporttype, String displayType, String languageid, String timezone, String emailAddress, String fileName, String reportaddressid, PropertyList propertyList) {
        ConnectionProcessor processor = new ConnectionProcessor(requestContext.getConnectionId());
        String email = processor.getConfigProperty("com.labvantage.sapphire.server.emailfromaddress");
        propertyList.setProperty("reportid", reportid);
        propertyList.setProperty("reportversionid", reportversionid);
        propertyList.setProperty("filetype", displayType);
        propertyList.setProperty("destination", "email");
        propertyList.setProperty("emailmessage", "your report has been attached with this file");
        propertyList.setProperty("emailfrom", email);
        propertyList.setProperty("emailtolist", emailAddress);
        propertyList.setProperty("emailsubject", "report");
        propertyList.setProperty("filename", fileName);
        propertyList.setProperty("languageid", !reporttype.equals("X") ? languageid : "");
        propertyList.setProperty("timezone", !reporttype.equals("X") ? timezone : "");
        propertyList.setProperty("reportaddressid", reportaddressid);
    }

    private void populatePropertyForPrint(String reportid, String reportversionid, String reporttype, String displayType, String languageid, String timezone, String printerName, String reportaddressid, PropertyList propertyList) {
        propertyList.setProperty("reportid", reportid);
        propertyList.setProperty("reportversionid", reportversionid);
        propertyList.setProperty("addressid", printerName);
        propertyList.setProperty("addresstype", "Device");
        propertyList.setProperty("filetype", displayType);
        propertyList.setProperty("languageid", !reporttype.equals("X") ? languageid : "");
        propertyList.setProperty("timezone", !reporttype.equals("X") ? timezone : "");
        propertyList.setProperty("reportaddressid", reportaddressid);
    }

    private PropertyList getParamMap(PropertyList paramMap, String strParams, HttpServletRequest request, ServletContext servletContext) {
        String[] params = StringUtil.split(strParams, ";");
        for (int i = 0; i < params.length; ++i) {
            paramMap.setProperty(params[i], request.getParameter(params[i] + "_param"));
            this.logger.info(params[i] + " : " + request.getParameter(params[i] + "_param"));
        }
        return paramMap;
    }

    private String getDisplayType(String reportid, String reportversionid, String reporteventid, RequestContext requestContext, HttpServletRequest request, SapphireReport sr) {
        String displayType = request.getParameter("output_format");
        SafeSQL safeSQL = new SafeSQL();
        if (displayType == null || displayType.length() == 0) {
            String defaultDisplayType = "";
            String alternateDisplayType = "";
            if (reportid != null && reportid.length() > 0) {
                String sqlToGetReportDS = "SELECT * FROM report WHERE reportid =" + safeSQL.addVar(reportid) + " AND reportversionid=" + safeSQL.addVar(reportversionid);
                DataSet reportDS = new QueryProcessor(new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionId()).getConnectionId()).getPreparedSqlDataSet(sqlToGetReportDS, safeSQL.getValues());
                if (reportDS != null && reportDS.size() > 0) {
                    defaultDisplayType = reportDS.getValue(0, "defaultdisplaytype");
                    alternateDisplayType = reportDS.getValue(0, "alternatedisplaytype");
                }
            } else if (reporteventid != null && reporteventid.length() > 0) {
                String sqlTo = "SELECT * FROM reportevent WHERE reporteventid=" + safeSQL.addVar(reporteventid);
                DataSet reporteventDS = new QueryProcessor(new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionId()).getConnectionId()).getPreparedSqlDataSet(sqlTo, safeSQL.getValues());
                if (reporteventDS != null && reporteventDS.size() > 0) {
                    defaultDisplayType = reporteventDS.getValue(0, "displaytype");
                }
            }
            displayType = defaultDisplayType.length() > 0 ? defaultDisplayType : (alternateDisplayType.length() > 0 ? alternateDisplayType : sr.getDefaultDisplayType());
        }
        return displayType.toLowerCase();
    }
}

