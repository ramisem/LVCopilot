/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.report;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.actions.report.BaseReportAction;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.bo.SapphireBOReport;
import com.labvantage.sapphire.report.bo.SapphireBOUtil;
import com.labvantage.sapphire.report.collated.SapphireCollatedReport;
import com.labvantage.sapphire.report.jasper.SapphireJasperReport;
import com.labvantage.sapphire.report.jasper.SapphireJavaTalendReport;
import com.labvantage.sapphire.report.nwa.SapphireNWAReport;
import com.labvantage.sapphire.report.nwa.SapphireNWAUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GenerateReport
extends BaseReportAction
implements sapphire.action.GenerateReport {
    /*
     * Unable to fully structure code
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        reporteventid = properties.getProperty("reporteventid", "");
        reportid = properties.getProperty("reportid", "");
        reportversionid = properties.getProperty("reportversionid", "");
        languageid = properties.getProperty("languageid", "");
        timezone = properties.getProperty("timezone", "");
        reportaddressid = properties.getProperty("reportaddressid", "");
        qp = new QueryProcessor(this.connectionInfo.getConnectionId());
        safeSQL = new SafeSQL();
        addressid = "";
        addresstype = "";
        filename = "";
        filetype = "";
        destination = "";
        emailtolist = "";
        emailcclist = "";
        emailfrom = "";
        emailsubject = "";
        emailmessage = "";
        if (reporteventid.length() > 0 && reportid.length() == 0 && (reporteventDS = qp.getPreparedSqlDataSet(sql = "SELECT * FROM reportevent WHERE reporteventid=" + safeSQL.addVar(reporteventid), safeSQL.getValues())) != null && reporteventDS.size() > 0) {
            reportid = reporteventDS.getString(0, "reportid", "");
            reportversionid = reporteventDS.getString(0, "reportversionid", "");
            addressid = reporteventDS.getString(0, "addressid", "");
            addresstype = reporteventDS.getString(0, "addresstype", "");
            filetype = reporteventDS.getString(0, "displaytype", "pdf").toLowerCase();
            filename = reporteventDS.getString(0, "filename", "");
            emailfrom = reporteventDS.getString(0, "emailfrom", "");
            emailtolist = reporteventDS.getString(0, "emailto", "");
            emailcclist = reporteventDS.getString(0, "emailcc", "");
            emailsubject = reporteventDS.getString(0, "emailsubject", "");
            emailmessage = reporteventDS.getString(0, "emailmessage", "");
            eventtype = reporteventDS.getString(0, "eventtype", "");
            if (eventtype.equalsIgnoreCase("Print")) {
                destination = "printer";
            } else if (eventtype.equalsIgnoreCase("Email")) {
                destination = "email";
            } else if (eventtype.equalsIgnoreCase("Export")) {
                destination = "file";
            }
            safeSQL.reset();
            paramValueSQL = "SELECT paramid,paramvalueclob FROM reporteventparam WHERE reporteventid=" + safeSQL.addVar(reporteventid) + " and paramtypeflag != 'O'";
            paramvalueDS = qp.getPreparedSqlDataSet(paramValueSQL, safeSQL.getValues(), true);
            if (paramvalueDS != null && paramvalueDS.size() > 0) {
                for (i = 0; i < paramvalueDS.size(); ++i) {
                    paramid = paramvalueDS.getString(i, "paramid", "");
                    if (properties.getProperty(paramid, "").length() != 0) continue;
                    paramvalue = paramvalueDS.getClob(i, "paramvalueclob", "");
                    if (paramvalue != null && paramvalue.length() > 0 && paramvalue.indexOf(",") >= 0) {
                        paramvalue = paramvalue.replaceAll(",", ";");
                    }
                    properties.setProperty(paramid, paramvalue);
                }
            }
            properties.setProperty("regenerate", "Y");
        }
        conninfo = new ConnectionProcessor(this.connectionInfo.getConnectionId()).getConnectionInfo(this.connectionInfo.getConnectionId());
        sr = SapphireReport.getIntance(reportid, reportversionid, conninfo, languageid, true, properties.getProperty("filetype", filetype).toLowerCase());
        addressid = properties.getProperty("addressid", addressid);
        addresstype = properties.getProperty("addresstype", addresstype);
        filename = properties.getProperty("filename", filename);
        safeSQL.reset();
        sqlToGetReportDS = "SELECT * from report where reportid =" + safeSQL.addVar(reportid);
        reportDS = qp.getPreparedSqlDataSet(sqlToGetReportDS, safeSQL.getValues());
        if ((reportDS.getString(0, "defaultdisplaytype", "").length() > 0 || reportDS.getString(0, "alternatedisplaytype", "").length() > 0) && properties.getProperty("filetype").length() == 0) {
            defaultDisplayType = reportDS.getString(0, "defaultdisplaytype", "");
            alternateDisplayType = reportDS.getString(0, "alternatedisplaytype", "");
            filetype = defaultDisplayType.length() > 0 ? defaultDisplayType.toLowerCase() : (alternateDisplayType.length() > 0 ? alternateDisplayType.toLowerCase() : sr.getDefaultDisplayType());
        } else {
            filetype = properties.getProperty("filetype", filetype).toLowerCase();
        }
        destination = properties.getProperty("destination", destination);
        emailtolist = properties.getProperty("emailtolist", emailtolist);
        emailcclist = properties.getProperty("emailcclist", emailcclist);
        emailfrom = properties.getProperty("emailfrom", emailfrom);
        emailsubject = properties.getProperty("emailsubject", emailsubject);
        emailmessage = properties.getProperty("emailmessage", emailmessage);
        printername = "";
        dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        date = dateFormat.format(Calendar.getInstance().getTime());
        userName = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId();
        propertyMap = new HashMap<String, String>(properties);
        propertyMap.put("currentuser", userName);
        propertyMap.put("currentdate", date);
        propertyMap.put("currentdatetime", date);
        if (filename != null && filename.trim().length() > 0) {
            filename = this.getConvertedContent(filename, propertyMap);
        }
        if (sr instanceof SapphireJavaTalendReport) {
            filename = ((SapphireJavaTalendReport)sr).getLogicalFileName(filename);
        }
        try {
            printername = this.getPrinter(addressid, addresstype);
        }
        catch (Exception var29_31) {
            // empty catch block
        }
        if (reportid.length() == 0) {
            throw new SapphireException("INVALID_PROPERTY", "Reportid not specified");
        }
        if (destination.length() == 0) {
            destination = "printer";
            properties.setProperty("destination", destination);
        }
        if (destination.equalsIgnoreCase("file") && filename.length() == 0) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not process the report '" + reportid + "': The filename property is empty when the destination is file");
        }
        if (destination.equalsIgnoreCase("file") && filename.indexOf(".") == -1) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not process the report '" + reportid + "': The filename property do not contain the file name with Extention");
        }
        if (!(destination.equalsIgnoreCase("printer") || destination.equalsIgnoreCase("email") || destination.equalsIgnoreCase("file"))) {
            throw new SapphireException("PROCESSACTION_FAILED", "Could not process the report '" + reportid + "': Incorrect destination specified");
        }
        reportParameters = new HashMap();
        reportType = "";
        reportParameters.put("psSAPPHIRE_CurrentUser", userName);
        reportParameters.put("SAPPHIRE_CurrentUser", userName);
        reportParameters.put("pmSAPPHIRE_CurrentUser", userName);
        paramds = sr.getParamds();
        sr.setSapphireConnection(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        boUtil = null;
        if (sr instanceof SapphireJasperReport) {
            sjp = (SapphireJasperReport)sr;
            sjp.getJasperReport();
            reportParameters = sjp.getReportParamMap(properties);
            reportParameters.put("exporterobject", properties.get("exporterobject"));
        }
        if (sr instanceof SapphireCollatedReport) {
            reportParameters.putAll(properties);
        } else if (sr instanceof SapphireJavaTalendReport) {
            for (i = 0; i < paramds.size(); ++i) {
                paramid = paramds.getValue(i, "paramid");
                actualParamid = paramds.getValue(i, "paraminto");
                paramvalue = properties.getProperty(paramid);
                actualParamvalue = properties.getProperty(actualParamid);
                paramXvalue = properties.getProperty("param" + (i + 1));
                if (paramvalue != null && paramvalue.length() > 0) {
                    reportParameters.put(actualParamid, paramvalue);
                    reportParameters.put(paramid, paramvalue);
                    continue;
                }
                if (actualParamvalue != null && actualParamvalue.length() > 0) {
                    reportParameters.put(actualParamid, actualParamvalue);
                    reportParameters.put(paramid, actualParamvalue);
                    continue;
                }
                if (paramXvalue != null && paramXvalue.length() > 0) {
                    reportParameters.put(actualParamid, paramXvalue);
                    reportParameters.put(paramid, paramXvalue);
                    continue;
                }
                if (actualParamvalue != null) {
                    reportParameters.put(actualParamid, actualParamvalue);
                    reportParameters.put(paramid, actualParamvalue);
                    continue;
                }
                if (paramvalue != null) {
                    reportParameters.put(actualParamid, paramvalue);
                    reportParameters.put(paramid, paramvalue);
                    continue;
                }
                if (paramXvalue == null) continue;
                reportParameters.put(actualParamid, paramXvalue);
                reportParameters.put(paramid, paramXvalue);
            }
        } else {
            if (sr instanceof SapphireNWAReport) {
                snwa = (SapphireNWAReport)sr;
                try {
                    snwa.nwaUtil = new SapphireNWAUtil(conninfo);
                    reportParameters = snwa.getReportParamMap(null, properties);
                }
                catch (Exception e) {
                    throw new SapphireException("PROCESSACTION_FAILED", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
                }
            }
            if (sr instanceof SapphireBOReport) {
                try {
                    boUtil = new SapphireBOUtil(this.connectionInfo);
                    boUtil.restApiLogon();
                    reportPath = boUtil.getReportPath(reportid, reportversionid);
                    for (Map.Entry<String, String> report : boUtil.getReportInfo(reportPath).entrySet()) {
                        reportType = report.getValue();
                    }
                    for (i = 0; i < paramds.getRowCount(); ++i) {
                        paramid = paramds.getValue(i, "paramid");
                        paramvalue = properties.getProperty(paramid);
                        actualParamid = paramds.getValue(i, "paraminto");
                        actualParamvalue = properties.getProperty(actualParamid);
                        paramType = paramds.getValue(i, "paramtype");
                        actualParamType = boUtil.getParamType(reportid, reportversionid, actualParamid);
                        if (("absreldt".equalsIgnoreCase(paramType) || "dateonly".equalsIgnoreCase(paramType)) && ("absreldt".equalsIgnoreCase(actualParamType) || "dateonly".equalsIgnoreCase(actualParamType))) {
                            sdf = null;
                            if ("Webi".equalsIgnoreCase(reportType)) {
                                sdf = new SimpleDateFormat(boUtil.getBoWebiDateFormat());
                            }
                            if (sdf != null) {
                                paramvalue = sdf.format(new DateTimeUtil(this.connectionInfo).getCalendar(actualParamvalue).getTime());
                                actualParamvalue = sdf.format(new DateTimeUtil(this.connectionInfo).getCalendar(actualParamvalue).getTime());
                            }
                        }
                        if (paramvalue != null && paramvalue.length() > 0) {
                            reportParameters.put(actualParamid, paramvalue);
                            reportParameters.put(paramid, paramvalue);
                            continue;
                        }
                        if (actualParamvalue != null && actualParamvalue.length() > 0) {
                            reportParameters.put(actualParamid, actualParamvalue);
                            reportParameters.put(paramid, actualParamvalue);
                            continue;
                        }
                        if (actualParamvalue != null) {
                            reportParameters.put(actualParamid, actualParamvalue);
                            reportParameters.put(paramid, actualParamvalue);
                            continue;
                        }
                        if (paramvalue != null) {
                            reportParameters.put(actualParamid, paramvalue);
                            reportParameters.put(paramid, paramvalue);
                            continue;
                        }
                        throw new SapphireException("PROCESSACTION_FAILED", "Paramter " + paramid + "Not defined");
                    }
                    regenerateflag = properties.getProperty("regenerate", "");
                    if (!regenerateflag.equalsIgnoreCase("Y")) ** GOTO lbl237
                    reportParameters.put("regenerate", regenerateflag);
                    reportParameters.put("parentreporteventid", properties.getProperty("reporteventid", ""));
                }
                catch (Exception e) {
                    throw new SapphireException("PROCESSACTION_FAILED", "Fialed to get BO report " + e.getMessage());
                }
                finally {
                    boUtil.restApiLogoff();
                }
            }
        }
lbl237:
        // 7 sources

        try {
            if (destination.equalsIgnoreCase("email")) {
                sr.sendReportToEmail(reportParameters, filetype, emailfrom, emailtolist, emailcclist, emailsubject, emailmessage, filename);
            } else if (destination.equalsIgnoreCase("file")) {
                if (sr.getReporttypeflag().equalsIgnoreCase("T") && "deliverrunfile.".equalsIgnoreCase(filename)) {
                    filename = "";
                } else {
                    path = filename;
                    if (filename.lastIndexOf(File.separator) > 0) {
                        path = filename.substring(0, filename.lastIndexOf(File.separator));
                    }
                    if (!(filePath = new File(path)).exists()) {
                        filePath.mkdirs();
                    }
                }
                reportParameters.put("lvsexporterflag", "A");
                sr.sendReportToFile(reportParameters, filetype, filename);
                if (sr.getReporttypeflag().equalsIgnoreCase("T") && reportParameters.containsKey("talendfilename")) {
                    properties.setProperty("talendfilename", (String)reportParameters.get("talendfilename"));
                    properties.setProperty("talendfilepath", (String)reportParameters.get("talendfilepath"));
                }
            } else if (destination.equalsIgnoreCase("printer")) {
                reportParameters.put("reporttype", reportType);
                sr.sendReportToPrinter(reportParameters, filetype, printername, addressid, addresstype);
            }
        }
        catch (Exception e) {
            throw new SapphireException("PROCESSACTION_FAILED", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        finally {
            if (sr != null) {
                sr.reset();
            }
        }
    }

    private String getConvertedContent(String displayFormat, HashMap contentMap) {
        String displayValue = displayFormat;
        int fromIndex = 0;
        int openPerIndex = 0;
        int closePerIndex = 0;
        String placeHolderKey = "";
        String placeHolderValue = "";
        openPerIndex = displayFormat.indexOf("[", fromIndex);
        closePerIndex = displayFormat.indexOf("]", openPerIndex);
        while (openPerIndex > -1 && closePerIndex > -1) {
            placeHolderKey = displayFormat.substring(openPerIndex + 1, closePerIndex).toLowerCase();
            if (contentMap.containsKey(placeHolderKey)) {
                placeHolderValue = String.valueOf(contentMap.get(placeHolderKey));
                displayValue = StringUtil.replaceAll(displayValue, "[" + placeHolderKey + "]", placeHolderValue, false);
            }
            fromIndex = openPerIndex + 1;
            openPerIndex = displayFormat.indexOf("[", fromIndex);
            closePerIndex = displayFormat.indexOf("]", openPerIndex);
        }
        if (displayValue.length() == 0) {
            displayValue = "&nbsp;";
        }
        return displayValue;
    }
}

