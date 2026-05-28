/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRDataSource
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JasperReport
 *  net.sf.jasperreports.engine.data.JsonDataSource
 */
package com.labvantage.sapphire.actions.label;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.report.PrintPdf;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.jasper.SapphireJasperReport;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.Printer;
import java.awt.print.PrinterException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.DAMProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GenerateLabel
extends PrintPdf
implements sapphire.action.GenerateLabel {
    public static final String ID = "GenerateLabel";
    public static final String VERSIONID = "1";
    String[] arrLabelMethodOverrides = new String[]{"labelmethodtype", "controlledflag", "labelformat", "numcopies", "labelsdcid", "selectclause", "fromclause", "whereclause", "orderbyclause", "exportlocation", "printeraddressid"};
    String[] arrLabelMethodExcludes = new String[]{"createdt", "createby", "createtool", "moddt", "modby", "modtool", "reprintfromid", "reprintcount"};
    private String currentUser = "";
    private String PROPERTY_AUDITREASON = "";
    private String PROPERTY_AUDITSIGNEDFLAG = "";
    private String PROPERTY_AUDITACTIVITY = "";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        boolean regenerateLabel;
        this.logger.info("Start of GenerateLabel Action");
        String keyid1 = properties.getProperty("keyid1");
        String keyid2 = properties.getProperty("keyid2");
        String keyid3 = properties.getProperty("keyid3");
        this.PROPERTY_AUDITREASON = properties.getProperty("auditreason");
        this.PROPERTY_AUDITSIGNEDFLAG = properties.getProperty("auditsignedflag");
        this.PROPERTY_AUDITACTIVITY = properties.getProperty("auditactivity");
        String labelMethodId = properties.getProperty("labelmethodid");
        String labelMethodVersionId = properties.getProperty("labelmethodversionid");
        String labelEventId = properties.getProperty("labeleventid");
        String datasetXml = properties.getProperty("labeldata");
        String printer = properties.getProperty("printeraddressid", "");
        String printerType = properties.getProperty("printeraddresstype", "Device");
        String copiesStr = properties.getProperty("numcopies", VERSIONID);
        boolean jsonTest = properties.getProperty("jsontest", "N").startsWith("Y");
        boolean usePrinterMargins = properties.getProperty("useprintermargins", "N").startsWith("Y");
        boolean returnPdfBytes = properties.getProperty("returnpdfbytes", "N").startsWith("Y");
        int copies = this.getCopies(copiesStr);
        this.currentUser = this.connectionInfo.getSysuserId();
        HashMap labelMethodAttributes = new HashMap();
        boolean isJasperJSONLabelMethod = false;
        boolean isJasperReport = false;
        HashMap labelMap = new HashMap();
        if (OpalUtil.isNotEmpty(labelMethodId)) {
            labelMethodAttributes = this.getLabelMethodVersion(labelMethodId, labelMethodVersionId);
            isJasperJSONLabelMethod = labelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("Jasper");
            isJasperReport = labelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("Report");
            if (!isJasperReport) {
                labelMap = this.generateLabelMethodLabel(keyid1, keyid2, keyid3, labelMethodId, labelMethodVersionId, properties, datasetXml, labelEventId);
            }
            regenerateLabel = false;
        } else {
            regenerateLabel = true;
            labelMap = this.generateLabelMethodLabel(keyid1, keyid2, keyid3, labelMethodId, labelMethodVersionId, properties, datasetXml, labelEventId);
            String lmType = (String)labelMap.get("labelmethodtype");
            if (lmType.contains("Jasper")) {
                isJasperJSONLabelMethod = true;
            }
        }
        if (isJasperJSONLabelMethod || isJasperReport) {
            String reportId = "";
            String reportVersionId = "";
            if (!regenerateLabel) {
                if (keyid1.isEmpty()) {
                    throw new SapphireException(this.getTranslationProcessor().translate("No keyid1 given!"));
                }
                reportId = labelMethodAttributes.get("reportid").toString();
                reportVersionId = labelMethodAttributes.get("reportversionid").toString();
            }
            if (isJasperReport) {
                boolean printDirectly = properties.getProperty("printdirectly", "N").startsWith("Y");
                if (printDirectly) {
                    properties.setProperty("reportid", reportId);
                    properties.setProperty("reportversionid", reportVersionId);
                    properties.setProperty("addressid", printer);
                    properties.setProperty("addresstype", printerType);
                    properties.setProperty("destination", "printer");
                    properties.setProperty("auditreason", this.PROPERTY_AUDITREASON);
                    properties.setProperty("auditactivity", this.PROPERTY_AUDITACTIVITY);
                    properties.setProperty("auditsignedflag", this.PROPERTY_AUDITSIGNEDFLAG);
                    this.getActionProcessor().processAction("GenerateReport", VERSIONID, properties);
                } else {
                    SapphireReport sr = SapphireReport.getIntance(reportId, reportVersionId, this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), "", false, "pdf");
                    sr.setSapphireConnection(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                    SapphireJasperReport sjp = (SapphireJasperReport)sr;
                    String labelSdcid = (String)labelMethodAttributes.get("labelsdcid");
                    if (labelSdcid != null) {
                        properties.setProperty("sdcid", labelSdcid);
                    }
                    HashMap reportParameters = sjp.getReportParamMap(properties);
                    JasperReport report = sjp.getJasperReport();
                    try {
                        byte[] pdfBytes = SapphireJasperUtil.getReportBytes(report, (Map)reportParameters, this.database.getConnection());
                        if (returnPdfBytes) {
                            properties.put("pdfbytes", pdfBytes);
                        }
                        this.printPDF(new Printer(printer, printerType, this.getQueryProcessor()), pdfBytes, copies, usePrinterMargins);
                    }
                    catch (PrinterException | JRException e) {
                        throw new SapphireException("Could not create label report.", e);
                    }
                }
            } else {
                try {
                    int rowCount;
                    DataSet labelEventData = new DataSet();
                    DataSet labeldata = (DataSet)labelMap.get("dataset");
                    if (regenerateLabel) {
                        SafeSQL safeSQL = new SafeSQL();
                        String labelEventSQL = "SELECT labeleventid, le.labelmethodid, le.labelmethodversionid, labeldata, printeraddressid, reportid, reportversionid FROM labelevent le JOIN labelmethod lm ON le.labelmethodid = lm.labelmethodid AND le.labelmethodversionid = lm.labelmethodversionid WHERE labeleventid IN (" + safeSQL.addIn(labelEventId, ";") + ")";
                        labelEventData = this.getQueryProcessor().getPreparedSqlDataSet(labelEventSQL, safeSQL.getValues(), true);
                        rowCount = labelEventData.getRowCount();
                    } else {
                        rowCount = 1;
                    }
                    for (int i = 0; i < rowCount; ++i) {
                        Locale userLocale;
                        if (regenerateLabel) {
                            printer = labelEventData.getString(i, "printeraddressid", printer);
                            reportId = labelEventData.getString(i, "reportid", "");
                            reportVersionId = labelEventData.getString(i, "reportversionid", "");
                            String xml = labelEventData.getClob(i, "labeldata");
                            labeldata = new DataSet(xml);
                        }
                        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
                        if (reportId.length() <= 0) {
                            throw new SapphireException("Report id or versionid not defined!");
                        }
                        SapphireReport sr = SapphireReport.getIntance(reportId, reportVersionId, connectionInfo, "", false, "pdf");
                        SapphireJasperReport sjp = (SapphireJasperReport)sr;
                        JasperReport jasperReport = sjp.getJasperReport();
                        PropertyList props = new PropertyList();
                        props.setProperty("displayType", "pdf");
                        JSONObject dsJSON = labeldata.toJSONObject(false, labeldata.getColumns(), false, false, false);
                        String jsStr = dsJSON.optJSONArray("rows").toString();
                        if (jsonTest) {
                            properties.setProperty("json", jsStr);
                            continue;
                        }
                        int dateFormat = 3;
                        int dateTimeDateFormat = 3;
                        int dateTimeTimeFormat = 3;
                        try {
                            PropertyList dateFormatPolicy = this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom");
                            String defaultdateformat = dateFormatPolicy.getProperty("defaultdateformat", "");
                            String defaultdateonlyformat = dateFormatPolicy.getProperty("defaultdateonlyformat", "");
                            if (!defaultdateonlyformat.equals("")) {
                                int n = defaultdateonlyformat.equals("L") ? 1 : (dateFormat = defaultdateonlyformat.equals("M") ? 2 : 3);
                            }
                            if (!defaultdateformat.equals("")) {
                                String[] formatS = StringUtil.split(defaultdateformat, " ");
                                int n = formatS[0].equals("S") ? 3 : (dateTimeDateFormat = formatS[0].equals("L") ? 1 : 2);
                                if (formatS.length == 2) {
                                    dateTimeTimeFormat = formatS[1].equals("S") ? 3 : (formatS[1].equals("L") ? 1 : 2);
                                }
                            }
                        }
                        catch (SapphireException dateFormatPolicy) {
                            // empty catch block
                        }
                        String userLocaleStr = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getLocale();
                        if (userLocaleStr == null) {
                            userLocaleStr = new DateTimeUtil().getLocale().toString();
                        }
                        if (userLocaleStr.contains("_")) {
                            String country = userLocaleStr.substring(0, userLocaleStr.indexOf(95));
                            String language = userLocaleStr.substring(userLocaleStr.indexOf(95) + 1);
                            userLocale = new Locale(country, language);
                        } else {
                            userLocale = new Locale(userLocaleStr);
                        }
                        DateFormat df = DateFormat.getDateInstance(dateFormat, userLocale);
                        DateFormat dtf = DateFormat.getDateTimeInstance(dateTimeDateFormat, dateTimeTimeFormat, userLocale);
                        String dateTimeFormatStr = "";
                        if (dtf instanceof SimpleDateFormat) {
                            dateTimeFormatStr = ((SimpleDateFormat)dtf).toPattern();
                        }
                        ByteArrayInputStream jsOnStream = new ByteArrayInputStream(jsStr.getBytes(Charset.forName("UTF-8")));
                        JsonDataSource source = new JsonDataSource((InputStream)jsOnStream);
                        if (!regenerateLabel) {
                            source.setDatePattern(dateTimeFormatStr);
                        }
                        byte[] pdfBytes = SapphireJasperUtil.getReportBytes(jasperReport, (Map)sjp.getReportParamMap(props), (JRDataSource)source);
                        if (returnPdfBytes) {
                            properties.put("pdfbytes", pdfBytes);
                            continue;
                        }
                        this.printPDF(new Printer(printer, printerType, this.getQueryProcessor()), pdfBytes, copies, usePrinterMargins);
                    }
                }
                catch (PrinterException | JRException e) {
                    throw new SapphireException("Could not create label report.", e);
                }
            }
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private HashMap generateLabelMethodLabel(String keyid1, String keyid2, String keyid3, String labelMethodId, String labelMethodVersionId, PropertyList properties, String datasetXml, String labelEventId) throws SapphireException {
        HashMap labelEvent = new HashMap();
        HashMap labelMethodAttributes = new HashMap();
        if (labelMethodId.length() > 0) {
            labelMethodAttributes = this.getLabelMethodVersion(labelMethodId, labelMethodVersionId);
            labelMethodAttributes = this.getLabelMethodAttributes(labelMethodAttributes, properties);
            if (keyid1.length() > 0) {
                labelEvent = this.createLabelFromLabelMethodAndKeyid(keyid1, keyid2, keyid3, labelMethodAttributes);
            } else {
                if (datasetXml.length() <= 0) throw new SapphireException("If labelmethodid is passed in, a list of keyid1 s or a dataset xml must also be passed into the action.");
                labelEvent = this.createLabelFromLabelMethodAndXml(datasetXml, labelMethodAttributes, "", keyid1, keyid2, keyid3);
            }
        } else {
            if (labelEventId.length() <= 0) throw new SapphireException("Mandatory inputs labelmethodid or labeleventid not passed into the action. Cannot continue.");
            labelEvent = this.regenerateLabelFromLabelEvent(labelEventId);
        }
        properties.put("exportfilepath", labelEvent.get("filepath"));
        properties.put("newlabeleventid", labelEvent.get("newlabelevent"));
        return labelEvent;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private HashMap getLabelMethodVersion(String labelMethodId, String labelMethodVersionId) throws SapphireException {
        DataSet labelMethodDetails = this.getLabelMethodDetails(labelMethodId);
        HashMap<String, String> filter = new HashMap<String, String>();
        DataSet dsTemp = new DataSet();
        if (labelMethodVersionId.length() == 0) {
            labelMethodVersionId = "C";
            this.logger.info("GenerateLabel.getLabelMethodVersion -> labelmethodversionid not passed in, looking for the most Current version");
        }
        if (labelMethodVersionId.equals("C")) {
            int row = labelMethodDetails.findRow("versionstatus", "C");
            if (row >= 0) {
                return this.getHashMapFromDataset(labelMethodDetails, row);
            }
            filter.clear();
            filter.put("versionstatus", "P");
            dsTemp = labelMethodDetails.getFilteredDataSet(filter);
            if (dsTemp.size() <= 0) throw new SapphireException("Did not get a row for labelmethodid = " + labelMethodId + " and labelmethodversionid = " + labelMethodVersionId + ". Cannot continue.");
            dsTemp.sort("labelmethodversionid d");
            this.logger.info("GenerateLabel.getLabelMethodVersion -> labelmethodversionid not passed in, a current version of " + labelMethodId + " is also not present. Using the latest provisional one instead.");
            return this.getHashMapFromDataset(dsTemp, 0);
        }
        int row = labelMethodDetails.findRow("labelmethodversionid", labelMethodVersionId);
        if (row < 0) throw new SapphireException("Did not get a row for labelmethodid = " + labelMethodId + " and labelmethodversionid = " + labelMethodVersionId + ". Cannot continue.");
        return this.getHashMapFromDataset(labelMethodDetails, row);
    }

    private HashMap createLabelFromLabelMethodAndKeyid(String keyid1, String keyid2, String keyid3, HashMap labelMethodAttributes) throws SapphireException {
        DataSet labelData = new DataSet();
        String newlabelEvent = "";
        String filePath = "";
        HashMap<String, Object> labelMethod = new HashMap<String, Object>();
        String sdcId = labelMethodAttributes.get("labelsdcid").toString();
        String columns = labelMethodAttributes.get("selectclause").toString();
        String from = labelMethodAttributes.get("fromclause").toString();
        String where = labelMethodAttributes.get("whereclause").toString();
        String orderby = labelMethodAttributes.get("orderbyclause").toString();
        this.logger.info("GenerateLabel.createLabelFromLabelMethodAndKeyid -> Creating rset for " + keyid1);
        DAMProcessor dam = this.getDAMProcessor();
        if (keyid2 == null || keyid2.length() == 0) {
            keyid2 = null;
        }
        if (keyid3 == null || keyid3.length() == 0) {
            keyid3 = null;
        }
        String rset_id = this.getRSet(sdcId, keyid1, keyid2, keyid3, dam);
        String tableid = this.getTableId(sdcId);
        String keycolid1 = this.getKeycolid1(sdcId);
        if (columns.length() == 0) {
            columns = "*";
        } else {
            HashMap<String, String> hmContent = new HashMap<String, String>();
            hmContent.put("currentuser", this.currentUser);
            columns = this.getSubstitutedContent(columns, hmContent);
        }
        SafeSQL safeSQL = new SafeSQL();
        String completeSQL = "select " + columns + " from " + tableid;
        if (from != null && from.length() > 0) {
            completeSQL = completeSQL + " " + from;
        }
        completeSQL = completeSQL + " , rsetitems ";
        completeSQL = completeSQL + " where ( rsetitems.rsetid = " + safeSQL.addVar(rset_id) + " and rsetitems.keyid1 = " + tableid + "." + keycolid1 + ")";
        if (where != null && where.length() > 0) {
            String whereTemp = where.trim().toLowerCase();
            boolean isStartWithOperator = whereTemp.indexOf("and ") == 0 || whereTemp.indexOf("or ") == 0;
            String keyid1ForWhereClause = StringUtil.replaceAll(keyid1, ";", "','");
            String keyid2ForWhereClause = StringUtil.replaceAll(keyid2, ";", "','");
            String keyid3ForWhereClause = StringUtil.replaceAll(keyid2, ";", "','");
            String resolvedWhereClause = StringUtil.replaceAll(where, "[keyid1]", keyid1ForWhereClause);
            resolvedWhereClause = StringUtil.replaceAll(resolvedWhereClause, "[keyid2]", keyid2ForWhereClause);
            resolvedWhereClause = StringUtil.replaceAll(resolvedWhereClause, "[keyid3]", keyid3ForWhereClause);
            completeSQL = completeSQL + (isStartWithOperator ? " " : " and ") + resolvedWhereClause;
        }
        if (orderby != null && orderby.length() > 0) {
            completeSQL = completeSQL + " order by " + orderby;
        }
        labelData = this.getQueryProcessor().getPreparedSqlDataSet(completeSQL, safeSQL.getValues());
        dam.clearRSet(rset_id);
        if (!labelData.isValidColumn("count")) {
            labelData.addColumn("count", 0);
            labelData.setValue(-1, "count", labelMethodAttributes.get("numcopies").toString());
        }
        labelMethod.put("dataset", labelData);
        if (labelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("BarTender") || labelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("")) {
            filePath = this.sendToBarTender(labelData, labelMethodAttributes);
        } else if (!labelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("Jasper") && !labelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("Report")) {
            throw new SapphireException("Only Bartender and Jasper are supported as a valid LabelMethodType at this point.");
        }
        if (labelMethodAttributes.get("controlledflag").toString().equalsIgnoreCase("Y")) {
            newlabelEvent = this.createNewLabelEvent(labelMethodAttributes, "", labelData.toXML(), sdcId, keyid1, keyid2, keyid3);
        }
        labelMethod.put("filepath", filePath);
        labelMethod.put("newlabelevent", newlabelEvent);
        return labelMethod;
    }

    private void createNewLabelEventItem(String labeleventid, String sdcId, String keyid1, String keyid2, String keyid3) throws SapphireException {
        DataSet evtitemds = new DataSet();
        evtitemds.addColumn("labeleventid", 0);
        evtitemds.addColumn("labeleventitemid", 1);
        evtitemds.addColumnValues("itemsdcid", 0, sdcId, ";", "");
        evtitemds.addColumnValues("itemkeyid1", 0, keyid1, ";", "");
        evtitemds.addColumnValues("itemkeyid2", 0, keyid2, ";", "(null)");
        evtitemds.addColumnValues("itemkeyid3", 0, keyid3, ";", "(null)");
        evtitemds.addColumn("createdt", 2);
        evtitemds.setDate(-1, "createdt", DateTimeUtil.getNowCalendar());
        evtitemds.addColumn("createby", 0);
        evtitemds.setValue(-1, "createby", this.connectionInfo.getSysuserId());
        evtitemds.padColumn("itemsdcid");
        evtitemds.padColumn("itemkeyid2");
        evtitemds.padColumn("itemkeyid3");
        for (int i = 0; i < evtitemds.getRowCount(); ++i) {
            evtitemds.setNumber(i, "labeleventitemid", i);
        }
        evtitemds.setValue(-1, "labeleventid", labeleventid);
        try {
            DataSetUtil.insert(this.database, evtitemds, "labeleventitem");
        }
        catch (SapphireException e) {
            throw new SapphireException(e);
        }
    }

    private HashMap createLabelFromLabelMethodAndXml(String xml, HashMap hmLabelMethodAttributes, String sourceLabelEventId, String keyid1, String keyid2, String keyid3) throws SapphireException {
        String newlabelEvent = "";
        String filePath = "";
        HashMap<String, String> labelMethod = new HashMap<String, String>();
        String sdcId = hmLabelMethodAttributes.get("labelsdcid").toString();
        this.logger.info("GenerateLabel.createLabelFromLabelMethodAndXml -> Creating dataset from xml");
        DataSet dsLabelData = new DataSet(xml);
        if (!dsLabelData.isValidColumn("count")) {
            dsLabelData.addColumn("count", 0);
            dsLabelData.setValue(-1, "count", hmLabelMethodAttributes.get("numcopies").toString());
        }
        if (hmLabelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("BarTender") || hmLabelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("")) {
            filePath = this.sendToBarTender(dsLabelData, hmLabelMethodAttributes);
        } else if (!hmLabelMethodAttributes.get("labelmethodtype").toString().equalsIgnoreCase("Jasper")) {
            throw new SapphireException("Only Bartender and Jasper are supported as a valid LabelMethodType at this point.");
        }
        if (hmLabelMethodAttributes.get("controlledflag").toString().equalsIgnoreCase("Y")) {
            newlabelEvent = this.createNewLabelEvent(hmLabelMethodAttributes, sourceLabelEventId, xml, sdcId, keyid1, keyid2, keyid3);
        }
        labelMethod.put("filepath", filePath);
        labelMethod.put("newlabelevent", newlabelEvent);
        return labelMethod;
    }

    private HashMap regenerateLabelFromLabelEvent(String sourceLabelEventId) throws SapphireException {
        DataSet labelEventData = new DataSet();
        HashMap<String, String> labelEvent = new HashMap<String, String>();
        String newLabelEvent = "";
        String filePath = "";
        String reprintCount = "";
        ArrayList excludeList = this.getExcludes();
        String keyid1 = "";
        String keyid2 = "";
        String keyid3 = "";
        String labelmethodType = "";
        String tracelogid = "";
        this.logger.info("GenerateLabel.regenerateLabelFromLabelEvent -> Getting label event details for source labelevent: " + sourceLabelEventId);
        SafeSQL safeSQL = new SafeSQL();
        String labelEventSQL = "select * from labelevent where labeleventid in (" + safeSQL.addIn(sourceLabelEventId, ";") + ")";
        labelEventData = this.getQueryProcessor().getPreparedSqlDataSet(labelEventSQL, safeSQL.getValues(), true);
        sourceLabelEventId = "";
        for (int i = 0; i < labelEventData.size(); ++i) {
            HashMap<String, String> labelMethodAttributes = new HashMap<String, String>();
            String xml = labelEventData.getClob(i, "labeldata");
            for (int j = 0; j < labelEventData.getColumnCount(); ++j) {
                String colId = labelEventData.getColumnId(j);
                if (excludeList.contains(colId)) continue;
                labelMethodAttributes.put(colId, labelEventData.getValue(i, colId));
            }
            String labeleventidtemp = labelEventData.getString(i, "labeleventid");
            safeSQL.reset();
            String labelEventItemSQL = "select itemkeyid1, itemkeyid2, itemkeyid3 from labeleventitem where labeleventid in (" + safeSQL.addIn(labeleventidtemp, ";") + ")";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(labelEventItemSQL, safeSQL.getValues());
            if (ds.size() > 0) {
                keyid1 = ds.getColumnValues("itemkeyid1", ";");
                keyid2 = ds.getColumnValues("itemkeyid2", ";");
                keyid3 = ds.getColumnValues("itemkeyid3", ";");
            }
            this.logger.info("GenerateLabel.regenerateLabelFromLabelEvent -> Creating label for source labelevent: " + labelEventData.getString(i, "labeleventid"));
            HashMap hmTemp = this.createLabelFromLabelMethodAndXml(xml, labelMethodAttributes, labelEventData.getString(i, "labeleventid"), keyid1, keyid2, keyid3);
            sourceLabelEventId = sourceLabelEventId + ";" + labelEventData.getString(i, "labeleventid");
            newLabelEvent = newLabelEvent + ";" + hmTemp.get("newlabelevent");
            filePath = filePath + ";" + hmTemp.get("filepath");
            reprintCount = reprintCount + ";" + (labelEventData.getInt(i, "reprintcount", 0) + 1);
            labelmethodType = labelmethodType + ";" + labelEventData.getString(i, "labelmethodtype", "");
            tracelogid = tracelogid + ";" + labelEventData.getString(i, "tracelogid", "");
        }
        sourceLabelEventId = this.stripFirstSemiColon(sourceLabelEventId);
        newLabelEvent = this.stripFirstSemiColon(newLabelEvent);
        filePath = this.stripFirstSemiColon(filePath);
        reprintCount = this.stripFirstSemiColon(reprintCount);
        labelmethodType = this.stripFirstSemiColon(labelmethodType);
        tracelogid = this.stripFirstSemiColon(tracelogid);
        HashMap<String, String> updateLabelEvent = new HashMap<String, String>();
        updateLabelEvent.put("sdcid", "LV_LabelEvent");
        updateLabelEvent.put("keyid1", sourceLabelEventId);
        updateLabelEvent.put("reprintcount", reprintCount);
        updateLabelEvent.put("tracelogid", tracelogid);
        updateLabelEvent.put("auditactivity", this.PROPERTY_AUDITACTIVITY);
        updateLabelEvent.put("auditsignedflag", this.PROPERTY_AUDITSIGNEDFLAG);
        this.logger.info("GenerateLabel.regenerateLabelFromLabelEvent -> Updating the reprint count for the source labelevent: " + sourceLabelEventId);
        try {
            this.getActionProcessor().processAction("EditSDI", VERSIONID, updateLabelEvent);
        }
        catch (ActionException actionException) {
            throw new SapphireException("PROCESSACTION_FAILED", "Cannot increment ReprintCount, EditSDI failed: " + ErrorUtil.extractMessageFromException(actionException, ErrorUtil.isUserAdmin(this.getConnectionId())), actionException);
        }
        labelEvent.put("filepath", filePath);
        labelEvent.put("newlabelevent", newLabelEvent);
        labelEvent.put("labelmethodtype", labelmethodType);
        return labelEvent;
    }

    private DataSet getLabelMethodDetails(String labelMethodId) {
        this.logger.info("GenerateLabel.getLabelMethodDetails -> Getting labelmethod details for " + labelMethodId);
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = new DataSet();
        String sql = "select * from labelmethod where labelmethodid = " + safeSQL.addVar(labelMethodId) + " order by labelmethodversionid";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return ds;
    }

    private DataSet getReportDetails(String reportId) {
        this.logger.info("GenerateLabel.getReportDetails -> Getting report details for " + reportId);
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = new DataSet();
        String sql = "select * from report where reportid = " + safeSQL.addVar(reportId) + " order by reportversionid";
        ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        return ds;
    }

    private HashMap getLabelMethodAttributes(HashMap hmRetrievedLabelMethodAttributes, PropertyList properties) {
        int i;
        this.logger.info("GenerateLabel.getLabelMethodAttributes -> Getting labelmethod attributes");
        HashMap labelMethodAttributes = (HashMap)hmRetrievedLabelMethodAttributes.clone();
        ArrayList excludeList = this.getExcludes();
        for (i = 0; i < excludeList.size(); ++i) {
            labelMethodAttributes.remove(excludeList.get(i));
        }
        for (i = 0; i < this.arrLabelMethodOverrides.length; ++i) {
            if (properties.getProperty(this.arrLabelMethodOverrides[i]).length() <= 0) continue;
            labelMethodAttributes.put(this.arrLabelMethodOverrides[i], properties.getProperty(this.arrLabelMethodOverrides[i]));
        }
        this.logger.info("GenerateLabel.getLabelMethodAttributes -> Got labelmethod attributes: " + labelMethodAttributes);
        return labelMethodAttributes;
    }

    public HashMap getHashMapFromDataset(DataSet ds, int row) {
        HashMap<String, String> hm = new HashMap<String, String>();
        for (int i = 0; i < ds.getColumnCount(); ++i) {
            String colId = ds.getColumnId(i);
            hm.put(colId, ds.getValue(row, colId, ""));
        }
        return hm;
    }

    private String createNewLabelEvent(HashMap labelAttributes, String reprintFromid, String xml, String sdcId, String keyid1, String keyid2, String keyid3) throws SapphireException {
        HashMap<String, String> labelEventProps = new HashMap<String, String>();
        labelEventProps.put("sdcid", "LV_LabelEvent");
        labelEventProps.putAll(labelAttributes);
        labelEventProps.put("verifystatus", "Scheduled");
        if (reprintFromid != null && reprintFromid.length() > 0) {
            labelEventProps.put("reprintfromid", reprintFromid);
        }
        if (labelEventProps.get("printeraddressid") != null && labelEventProps.get("printeraddressid").toString().length() > 0) {
            labelEventProps.put("printeraddresstype", "Device");
        }
        SequenceProcessor sp = this.getSequenceProcessor();
        int nextSequence = sp.getSequence("LV_LabelEvent", "mainkey");
        labelEventProps.put("keyid1", "LE_" + StringUtil.padLeft("" + nextSequence, 13, '0'));
        labelEventProps.put("overrideautokey", "Y");
        labelEventProps.put("auditreason", this.PROPERTY_AUDITREASON);
        labelEventProps.put("auditactivity", this.PROPERTY_AUDITACTIVITY);
        labelEventProps.put("auditsignedflag", this.PROPERTY_AUDITSIGNEDFLAG);
        labelEventProps.put("tracelogid", "");
        this.logger.info("GenerateLabel.createNewLabelEvent -> Creating new label event");
        String newkeyid1 = "";
        try {
            this.getActionProcessor().processAction("AddSDI", VERSIONID, labelEventProps);
            newkeyid1 = labelEventProps.get("newkeyid1").toString();
        }
        catch (ActionException actionException) {
            throw new SapphireException("PROCESSACTION_FAILED", "Action AddSDI failed: " + ErrorUtil.extractMessageFromException(actionException, ErrorUtil.isUserAdmin(this.getConnectionId())), actionException);
        }
        ((DBUtil)this.database).updateClob("labelevent", "labeldata", xml, StringUtil.split("labeleventid", ";"), StringUtil.split(newkeyid1, ";"));
        this.createNewLabelEventItem(newkeyid1, sdcId, keyid1, keyid2, keyid3);
        return newkeyid1;
    }

    private String sendToBarTender(DataSet dsLabelData, HashMap hmLabelMethodAttributes) throws SapphireException {
        String labelHome = Configuration.getInstance().getApplicationHome() + File.separator + "labels" + File.separator;
        String exportLocation = hmLabelMethodAttributes.get("exportlocation") == null || hmLabelMethodAttributes.get("exportlocation").equals("") ? labelHome : hmLabelMethodAttributes.get("exportlocation").toString();
        String labelFormat = hmLabelMethodAttributes.get("labelformat") == null ? "" : hmLabelMethodAttributes.get("labelformat").toString();
        String printer = hmLabelMethodAttributes.get("printeraddressid") == null ? "" : hmLabelMethodAttributes.get("printeraddressid").toString();
        String numcopies = hmLabelMethodAttributes.get("numcopies") == null ? "" : hmLabelMethodAttributes.get("numcopies").toString();
        boolean uncPath = exportLocation.indexOf(64) <= 0;
        String emailAddress = "";
        String fileLocation = "";
        String fileNameWithPath = "";
        if (!uncPath) {
            emailAddress = exportLocation;
        }
        labelFormat = this.getAbsoluteFromRelativePath(labelFormat, labelHome, true);
        File fi = new File(labelFormat);
        this.logger.info("GenerateLabel.sendToBarTender -> Checking the label format file at " + labelFormat);
        this.logger.info("GenerateLabel.sendToBarTender -> " + (fi.exists() ? "Label format exists" : "Label format does NOT exist") + "  Size = " + fi.length());
        StringBuffer sbHeaderData = new StringBuffer();
        StringBuffer sbRowData = new StringBuffer();
        StringBuffer sbOutputData = new StringBuffer();
        if (dsLabelData.getRowCount() > 0) {
            File bartenderFile;
            int i;
            this.logger.info("GenerateLabel.sendToBarTender -> Getting physical printer path");
            String printerPath = "";
            SafeSQL safeSQL = new SafeSQL();
            String sql = "select printerid from address where addressid=" + safeSQL.addVar(printer) + " and addresstype='Device'";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            if (ds.getRowCount() > 0) {
                printerPath = ds.getValue(0, "printerid", "");
            }
            this.logger.info("GenerateLabel.sendToBarTender -> Creating bartender file");
            sbOutputData.append("%BTW% /F=\"").append(labelFormat).append("\" /P /PRN=\"").append(printerPath);
            sbOutputData.append("\" /D=\"%Trigger File Name%\" /R=3 /DbTextHeader=3");
            if (numcopies.length() > 0) {
                sbOutputData.append(" /C=").append(numcopies);
            }
            sbOutputData.append("\r\n%END%\r\n");
            for (i = 0; i < dsLabelData.getColumnCount(); ++i) {
                sbHeaderData.append("\t").append(dsLabelData.getColumnId(i));
            }
            sbOutputData.append(sbHeaderData.substring(1));
            for (i = 0; i < dsLabelData.getRowCount(); ++i) {
                for (int j = 0; j < dsLabelData.getColumnCount(); ++j) {
                    sbRowData.append(dsLabelData.getValue(i, dsLabelData.getColumnId(j))).append("\t");
                }
                sbRowData.append("\r\n");
            }
            try {
                fileLocation = uncPath ? this.getAbsoluteFromRelativePath(exportLocation, labelHome) : labelHome;
                File filePath = new File(fileLocation);
                if (!filePath.exists()) {
                    filePath.mkdirs();
                }
                fileNameWithPath = fileLocation + File.separator + this.getLabelFileName(".tmp");
                bartenderFile = new File(fileNameWithPath);
                FileOutputStream fos = new FileOutputStream(bartenderFile);
                PrintStream out = new PrintStream((OutputStream)fos, true, "UTF-8");
                out.print(sbOutputData.toString());
                out.print("\r\n");
                out.print(sbRowData.toString());
                out.close();
                fos.close();
                fileNameWithPath = fileLocation + File.separator + this.getLabelFileName(".dd");
                File renameToFile = new File(fileNameWithPath);
                boolean renamed = bartenderFile.renameTo(renameToFile);
                if (!renamed) {
                    throw new SapphireException("Failed to rename bartender file to .dd after writing details in it.");
                }
                this.logger.info("GenerateLabel.sendToBarTender -> Storing bartender file at " + fileNameWithPath);
            }
            catch (IOException e) {
                throw new SapphireException("FILE_ACCESS_FAILURE", "Failed to write to location: " + fileNameWithPath, e);
            }
            if (!uncPath) {
                this.logger.info("GenerateLabel.sendToBarTender -> Emailing bartender file to " + emailAddress);
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("address", emailAddress);
                hm.put("message", "");
                hm.put("subject", "Label for Printing");
                hm.put("auditreason", this.PROPERTY_AUDITREASON);
                hm.put("auditactivity", this.PROPERTY_AUDITACTIVITY);
                hm.put("auditsignedflag", this.PROPERTY_AUDITSIGNEDFLAG);
                hm.put("filename", fileNameWithPath);
                this.getActionProcessor().processAction("SendMail", VERSIONID, hm, true);
                bartenderFile.delete();
            }
        }
        return fileNameWithPath;
    }

    private String getAbsoluteFromRelativePath(String fileLocation, String labelHome) {
        return this.getAbsoluteFromRelativePath(fileLocation, labelHome, false);
    }

    private String getAbsoluteFromRelativePath(String fileLocation, String labelHome, boolean windowsSpecific) {
        boolean isAbsolute;
        File f = new File(fileLocation);
        boolean bl = isAbsolute = windowsSpecific ? this.isWindowsSpecificAbsolutePath(fileLocation) : f.isAbsolute();
        if (!isAbsolute) {
            fileLocation = fileLocation.startsWith("\\") || fileLocation.startsWith("/") ? labelHome + fileLocation.substring(1) : labelHome + fileLocation;
        }
        return fileLocation;
    }

    private boolean isWindowsSpecificAbsolutePath(String fileLocation) {
        boolean absolutePath = false;
        if (fileLocation.startsWith("\\\\") || fileLocation.substring(1).startsWith(":")) {
            absolutePath = true;
        }
        return absolutePath;
    }

    private ArrayList getExcludes() {
        ArrayList<String> excludeList = new ArrayList<String>();
        for (int i = 0; i < this.arrLabelMethodExcludes.length; ++i) {
            excludeList.add(this.arrLabelMethodExcludes[i]);
        }
        return excludeList;
    }

    private String stripFirstSemiColon(String input) {
        if (input.startsWith(";")) {
            input = input.substring(1);
        }
        return input;
    }

    private synchronized String getLabelFileName(String ext) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-hhmmssSS");
        String strDate = sdf.format(new Date(System.currentTimeMillis()));
        String labelFileName = this.currentUser + "-" + strDate + "-" + Thread.currentThread().getId() + "-" + this.randomInteger(1111, 9999) + ext;
        return labelFileName;
    }

    private int randomInteger(int min, int max) {
        return min + (int)(Math.random() * (double)(max - min + 1));
    }

    private String getRSet(String sdcid, String keyid1, String keyid2, String keyid3, DAMProcessor dam) {
        String rSetID = "";
        try {
            rSetID = dam.createRSet(sdcid, keyid1, keyid2, keyid3);
        }
        catch (SapphireException e) {
            Trace.logError("Failed to create RSET " + e.getMessage());
        }
        return rSetID;
    }

    private String getKeycolid1(String sdcid) {
        SDCProcessor sdcp = this.getSDCProcessor();
        return sdcp.getSDCProperties(sdcid).get("keycolid1").toString();
    }

    private String getTableId(String sdcid) {
        SDCProcessor sdcp = this.getSDCProcessor();
        return sdcp.getSDCProperties(sdcid).get("tableid").toString();
    }

    private String getSubstitutedContent(String content, HashMap<String, String> contentMap) {
        String substitutedContent = content;
        int fromIndex = 0;
        int openPerIndex = 0;
        int closePerIndex = 0;
        String placeHolderKey = "";
        String placeHolderValue = "";
        openPerIndex = content.indexOf("[", fromIndex);
        closePerIndex = content.indexOf("]", openPerIndex);
        while (openPerIndex > -1 && closePerIndex > -1) {
            placeHolderKey = content.substring(openPerIndex + 1, closePerIndex).toLowerCase();
            if (contentMap.containsKey(placeHolderKey)) {
                placeHolderValue = contentMap.get(placeHolderKey);
                placeHolderValue = placeHolderValue == null ? "" : placeHolderValue;
                substitutedContent = StringUtil.replaceAll(substitutedContent, "[" + placeHolderKey + "]", placeHolderValue, false);
            }
            fromIndex = openPerIndex + 1;
            openPerIndex = content.indexOf("[", fromIndex);
            closePerIndex = content.indexOf("]", openPerIndex);
        }
        return substitutedContent;
    }
}

