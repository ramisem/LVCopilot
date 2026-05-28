/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.report.bo;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.SapphireReportEvent;
import com.labvantage.sapphire.report.bo.SapphireBOUtil;
import com.labvantage.sapphire.report.jasper.CommonParamMap;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class SapphireBOReport
extends SapphireReport {
    public SapphireBOUtil boUtil = null;

    public SapphireBOReport(String reporttypeflag) {
        super(reporttypeflag);
    }

    @Override
    public void init() throws SapphireException {
        this.supportedDisplayTypes.add("pdf");
        this.supportedDisplayTypes.add("xls");
        this.defaultDisplayType = "pdf";
    }

    @Override
    public void runReportToWeb(HashMap paramsMap, String displayType, HttpServletRequest request, HttpServletResponse response, String fileName, boolean isFile) throws SapphireException {
        byte[] reportbytes;
        SapphireReportEvent reportevent = this.runReportToWebEvent(paramsMap, displayType, response, isFile);
        try {
            if (displayType.equalsIgnoreCase("rtf") || displayType.equalsIgnoreCase("pdf") || displayType.equalsIgnoreCase("html") || displayType.equalsIgnoreCase("xls") || displayType.equalsIgnoreCase("excel")) {
                paramsMap.put("mode", displayType);
                if (displayType.equalsIgnoreCase("pdf")) {
                    if (this.isControlledReport() && reportevent != null) {
                        paramsMap.put("initialdisposition", this.initialdisposition);
                        paramsMap.put("watermarkflag", this.watermarkflag);
                    }
                    this.boUtil.runReportToWeb(paramsMap, response, fileName, isFile);
                } else {
                    this.boUtil.runReportToWeb(paramsMap, response, fileName, isFile);
                }
            } else {
                throw new SapphireException("Unrecognized display type: " + displayType);
            }
            reportbytes = (byte[])paramsMap.get("reportBytes");
        }
        catch (Exception e) {
            throw new SapphireException("Error running report:" + e.getMessage(), e);
        }
        if (this.isControlledReport() && reportevent != null) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportbytes);
            reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
        }
    }

    public HashMap getViewReportParamMap(HttpServletRequest request) throws Exception {
        CommonParamMap cparamsMap = new CommonParamMap(request);
        this.populateMissingParams(request, cparamsMap, this.paramds);
        cparamsMap.put("SAPPHIRE_ReportID", this.reportid);
        cparamsMap.put("SAPPHIRE_ReportVersion", this.reportversionid);
        cparamsMap.put("SAPPHIRE_ReportTitle", this.reportdesc);
        try {
            String reportPath = this.boUtil.getReportPath(this.reportid, this.reportversionid);
            List<String> params = this.boUtil.getBOReportParameter(reportPath);
            String reportType = this.boUtil.getReportType();
            String regenerateflag = request.getParameter("regenerate");
            if (params != null) {
                DataSet paramvalueds = (DataSet)request.getAttribute("paramvalueds");
                for (int i = 0; i < params.size(); ++i) {
                    String paramname = params.get(i);
                    String value = "";
                    if (regenerateflag != null && "Y".equalsIgnoreCase(regenerateflag)) {
                        int row = paramvalueds.findRow("paramid", paramname);
                        if (row >= 0) {
                            value = paramvalueds.getClob(row, "paramvalueclob", "");
                            if (value != null && value.length() > 0 && value.indexOf(",") >= 0) {
                                value = value.replaceAll(",", ";");
                            }
                        } else {
                            String paramid;
                            row = this.paramds.findRow("paraminto", paramname);
                            if (row >= 0 && (row = paramvalueds.findRow("paramid", paramid = this.paramds.getString(row, "paramid"))) >= 0 && (value = paramvalueds.getClob(row, "paramvalueclob", "")) != null && value.length() > 0 && value.indexOf(",") >= 0) {
                                value = value.replaceAll(",", ";");
                            }
                        }
                    } else {
                        value = request.getParameter(paramname);
                    }
                    value = CommonParamMap.substituteValue(value, cparamsMap);
                    if (value == null || value.length() == 0) {
                        for (int p = 0; p < this.paramds.getRowCount(); ++p) {
                            String paraminto = this.paramds.getString(p, "paraminto");
                            String mandatoryflag = this.paramds.getString(p, "mandatoryflag");
                            if (paramname.equals(paraminto) && !"Y".equals(mandatoryflag)) continue;
                        }
                    } else {
                        String ptype = this.boUtil.getParamType(this.reportid, this.reportversionid, paramname);
                        if ("absreldt".equalsIgnoreCase(ptype) || "dateonly".equalsIgnoreCase(ptype)) {
                            SimpleDateFormat sdf = null;
                            if ("Webi".equalsIgnoreCase(reportType)) {
                                sdf = new SimpleDateFormat(this.boUtil.getBoWebiDateFormat());
                            }
                            cparamsMap.put(paramname, sdf.format(new DateTimeUtil(this.connectionInfo).getCalendar(value).getTime()));
                        } else {
                            cparamsMap.put(paramname, value);
                        }
                    }
                    if ("SAPPHIRE_RSETID".equals(paramname)) {
                        this.createRSetInParamMap(cparamsMap);
                        continue;
                    }
                    if (!"SAPPHIRE_KEYID1List".equals(paramname) || !"rset".equals(cparamsMap.get("SAPPHIRE_KEYID1List"))) continue;
                    String rsetid = (String)cparamsMap.get("SAPPHIRE_RSETID");
                    if (rsetid == null || rsetid.length() == 0) {
                        this.createRSetInParamMap(cparamsMap);
                    }
                    if ((rsetid = (String)cparamsMap.get("SAPPHIRE_RSETID")) != null && rsetid.length() > 0) {
                        String sql = SapphireBOReport.getKeyid1ListSelect(rsetid);
                        cparamsMap.put("SAPPHIRE_KEYID1List", sql);
                        continue;
                    }
                    Trace.logInfo("Failed to Create rsetid");
                }
            }
            this.setMultiListParam(cparamsMap, request, ";");
            if (regenerateflag != null && "Y".equalsIgnoreCase(regenerateflag)) {
                cparamsMap.put("regenerate", regenerateflag);
                cparamsMap.put("parentreporteventid", request.getParameter("reporteventid"));
            }
        }
        catch (SapphireException e) {
            throw new SapphireException("Report Parameter View Problem" + e);
        }
        return cparamsMap;
    }

    @Override
    public void sendReportToEmail(HashMap paramMap, String displayType, String emailfrom, String emailtolist, String emailcclist, String emailsubject, String emailmessage, String fileName) throws SapphireException {
        SapphireBOUtil boUtil = new SapphireBOUtil(this.connectionInfo);
        boUtil.restApiLogon();
        try {
            fileName = fileName.indexOf(File.separator) >= 0 ? fileName.substring(fileName.indexOf(File.separator) + 1) : fileName;
            SapphireReportEvent reportevent = this.sendReportToEmailEvent(paramMap, displayType, emailfrom, emailtolist, emailcclist, emailsubject, emailmessage);
            boUtil.sendToEmail(this.reportid, this.reportversionid, paramMap, displayType, emailfrom, emailtolist, emailcclist, emailsubject, emailmessage, fileName, this.sapphireConnection);
            if (this.isControlledReport() && reportevent != null) {
                SapphireBOUtil boUtilControlReport = new SapphireBOUtil(this.connectionInfo);
                boUtilControlReport.restApiLogon();
                paramMap.put("mode", displayType);
                byte[] reportbytes = boUtilControlReport.getBinaryStream(this.reportid, this.reportversionid, displayType, paramMap);
                boUtilControlReport.restApiLogoff();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportbytes);
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessage("Could not Send BO report to Email ", ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        finally {
            boUtil.restApiLogoff();
        }
    }

    @Override
    public void sendReportToFile(HashMap paramMap, String displayType, String fileName) throws SapphireException {
        SapphireBOUtil boUtil = new SapphireBOUtil(this.connectionInfo);
        boUtil.restApiLogon();
        try {
            SapphireReportEvent reportevent = this.sendReportToFileEvent(paramMap, displayType, fileName);
            boUtil.sendToFile(this.reportid, this.reportversionid, paramMap, displayType, fileName);
            if (this.isControlledReport() && reportevent != null) {
                SapphireBOUtil boUtilControlReport = new SapphireBOUtil(this.connectionInfo);
                boUtilControlReport.restApiLogon();
                paramMap.put("mode", displayType);
                byte[] reportbytes = boUtilControlReport.getBinaryStream(this.reportid, this.reportversionid, displayType, paramMap);
                boUtilControlReport.restApiLogoff();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportbytes);
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessage("Could not Send BO report to File ", ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        finally {
            boUtil.restApiLogoff();
        }
    }

    @Override
    public void sendReportToPrinter(HashMap paramMap, String displayType, String printername, String addressid, String addresstype) throws SapphireException {
        SapphireBOUtil boUtil = new SapphireBOUtil(this.connectionInfo);
        boUtil.restApiLogon();
        try {
            SapphireReportEvent reportevent = this.sendReportToPrinterEvent(paramMap, displayType, printername, addressid, addresstype);
            boUtil.sendToPrinter(this.reportid, this.reportversionid, paramMap, displayType, printername);
            if (this.isControlledReport() && reportevent != null) {
                SapphireBOUtil boUtilControlReport = new SapphireBOUtil(this.connectionInfo);
                boUtilControlReport.restApiLogon();
                paramMap.put("mode", displayType);
                byte[] reportbytes = boUtilControlReport.getBinaryStream(this.reportid, this.reportversionid, displayType, paramMap);
                boUtilControlReport.restApiLogoff();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(reportbytes);
                reportevent.saveEvent(this.connectionInfo, byteArrayInputStream);
            }
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessage("Could not Send BO report to Printer ", ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        finally {
            boUtil.restApiLogoff();
        }
    }
}

