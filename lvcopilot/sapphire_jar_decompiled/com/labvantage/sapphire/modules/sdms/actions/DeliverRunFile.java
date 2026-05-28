/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.util.DeliverRunFileUtil;
import com.labvantage.sapphire.report.SapphireReport;
import com.labvantage.sapphire.report.jasper.SapphireJavaTalendReport;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.action.BaseAction;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DeliverRunFile
extends BaseAction
implements sapphire.action.DeliverRunFile {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        instrumentid = properties.getProperty("instrumentid", "");
        sdcid = properties.getProperty("sdcid", "");
        keyid1 = properties.getProperty("keyid1", "");
        keyid2 = properties.getProperty("keyid2", "");
        keyid3 = properties.getProperty("keyid3", "");
        if (instrumentid.length() == 0) {
            safeSQL = new SafeSQL();
            if (sdcid.equals("QCBatch")) {
                sql = new StringBuffer("SELECT instrumentid FROM s_qcbatch WHERE s_qcbatchid in (").append(safeSQL.addIn(keyid1)).append(")");
            } else if (sdcid.equals("LV_Array")) {
                sql = new StringBuffer("SELECT instrumentid FROM arrayarraymethoditem WHERE arrayid in (").append(safeSQL.addIn(keyid1)).append(") ORDER BY usersequence desc");
            } else if (sdcid.equals("DataSet")) {
                sql = new StringBuffer("SELECT s_instrumentid instrumentid FROM sdidata WHERE sdidataid in (").append(safeSQL.addIn(keyid1)).append(")");
            } else {
                throw new ActionException("No instrument specified so unable to deliver run-file");
            }
            if (sql.length() > 0 && (ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues())) != null) {
                if (ds.size() == 1) {
                    instrumentid = ds.getValue(0, "instrumentid");
                } else if (ds.size() > 1) {
                    if (DeliverRunFileUtil.areAllSame(ds, "instrumentid")) {
                        instrumentid = ds.getValue(0, "instrumentid");
                    } else {
                        throw new ActionException("All SDIs must share the same instrumentid if the instrumentid is not provided.");
                    }
                }
            }
        }
        if (instrumentid.length() == 0) {
            throw new ActionException("No instrument specified so unable to deliver run-file");
        }
        instrumentDS = DeliverRunFileUtil.getInstrumentDetails(instrumentid, this.getConnectionid());
        collectorid = instrumentDS.getValue(0, "sdmscollectorid");
        contentmode = properties.getProperty("contentmode");
        fixedcontent = properties.getProperty("fixedcontent", "");
        runFileName = "";
        tempFile = null;
        if (contentmode.equalsIgnoreCase("Report")) {
            reportid = properties.getProperty("reportid");
            reportversionid = properties.getProperty("reportversionid", "C");
            language = properties.getProperty("languageid", "");
            if (reportid.length() == 0 && (list = DeliverRunFileUtil.getReportsForSDC(instrumentDS, sdcid)).size() > 0) {
                reportid = StringUtil.split(list.get(0), ";")[0];
                reportversionid = StringUtil.split(list.get(0), ";")[1];
            }
            if (reportid.length() == 0) {
                throw new ActionException("Unable to determine the reportid for the run-file");
            }
            if (reportversionid.equals("C")) {
                reportversionid = DeliverRunFileUtil.getCVersion(this.getQueryProcessor(), reportid);
            }
            try {
                generatReportProps = new PropertyList();
                generatReportProps.setProperty("reportid", reportid);
                generatReportProps.setProperty("reportversionid", reportversionid);
                generatReportProps.setProperty("destination", "file");
                sr = SapphireReport.getIntance(reportid, reportversionid, this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()), true, language, false, "");
                runFileName = properties.getProperty("filename");
                runFileName = this.resolveFileName(runFileName, properties);
                if (sr instanceof SapphireJavaTalendReport) {
                    runFileName = ((SapphireJavaTalendReport)sr).getLogicalFileName(runFileName);
                    if (sr.getReporttypeflag().equalsIgnoreCase("T") && runFileName.trim().length() == 0) {
                        generatReportProps.setProperty("filename", "deliverrunfile.");
                    } else {
                        tempFile = FileUtil.createTempFile(FileManager.getFileName(runFileName, false), "." + FileManager.getExtension(runFileName)).toFile();
                        generatReportProps.setProperty("filename", tempFile.getAbsolutePath());
                    }
                } else {
                    tempFile = FileUtil.createTempFile(FileManager.getFileName(runFileName, false), "." + FileManager.getExtension(runFileName)).toFile();
                    generatReportProps.setProperty("filename", tempFile.getAbsolutePath());
                }
                params = sr.getParamds();
                for (i = 0; i < params.size(); ++i) {
                    into = params.getValue(i, "paraminto");
                    generatReportProps.setProperty("param" + (i + 1), properties.getProperty(into));
                }
                this.getActionProcessor().processAction("GenerateReport", "1", generatReportProps);
                if (!sr.getReporttypeflag().equalsIgnoreCase("T") || !(sr instanceof SapphireJavaTalendReport) || !generatReportProps.containsKey("talendfilename") || tempFile != null) ** GOTO lbl105
                runFileName = generatReportProps.getProperty("talendfilename");
                tempFile = new File(generatReportProps.getProperty("talendfilepath"));
            }
            catch (Exception e) {
                throw new SapphireException("Failed to create temporary report file: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        } else if (contentmode.equalsIgnoreCase("Fixed")) {
            runFileName = properties.getProperty("filename", "sequence.txt");
            runFileName = this.resolveFileName(runFileName, properties);
            fos = null;
            try {
                if (fixedcontent.length() > 0) {
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[keyid1]", keyid1);
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[keyid2]", keyid2);
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[keyid3]", keyid3);
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[sdcid]", sdcid);
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[currentuser]", this.connectionInfo.getSysuserId());
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[paramlistid]", properties.getProperty("paramlistid", ""));
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[paramlistversionid]", properties.getProperty("paramlistid", ""));
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[variantid]", properties.getProperty("paramlistid", ""));
                    fixedcontent = StringUtil.replaceAll(fixedcontent, "[dataset]", properties.getProperty("paramlistid", ""));
                }
                tempFile = FileUtil.createTempFile(FileManager.getFileName(runFileName, false), "." + FileManager.getExtension(runFileName)).toFile();
                fos = new FileOutputStream(tempFile);
                fos.write(fixedcontent.getBytes());
                fos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    fos.close();
                }
                catch (IOException var14_18) {}
            }
        }
        try {
            commandid = SDMSUtil.sendCollectorCommand(this.getQueryProcessor(), this.getActionProcessor(), collectorid, instrumentid, "COLLECTORCOMMAND_DELIVERINSTRUMENTFILE", runFileName, tempFile);
            properties.setProperty("collectorid", collectorid);
            properties.setProperty("collectorcommandid", commandid);
            properties.setProperty("logicalfilename", runFileName);
            tempFile.delete();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to send file");
        }
    }

    private String resolveFileName(String runFileName, PropertyList properties) {
        String[] tokens = StringUtil.getTokens(runFileName);
        DateTimeFormatter fullYearFormatter = DateTimeFormatter.ofPattern("yyyy");
        String fullYear = LocalDateTime.now().format(fullYearFormatter);
        DateTimeFormatter shortYearFormatter = DateTimeFormatter.ofPattern("yy");
        String shortYear = LocalDateTime.now().format(shortYearFormatter);
        DateTimeFormatter shortMonthFormatter = DateTimeFormatter.ofPattern("MM");
        String shortMonth = LocalDateTime.now().format(shortMonthFormatter);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd");
        String date = LocalDateTime.now().format(dateFormatter);
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        String month = LocalDateTime.now().format(monthFormatter);
        for (String token : tokens) {
            String replaceWith = "";
            if (token.equalsIgnoreCase("currentuser")) {
                replaceWith = this.connectionInfo.getSysuserId();
            } else if (token.toLowerCase().contains("yyyy") || token.toLowerCase().contains("yy") || token.toLowerCase().contains("dd") || token.toLowerCase().contains("mmm") || token.toLowerCase().contains("mmm")) {
                replaceWith = token.toLowerCase().replace("yyyy", fullYear).replace("yy", shortYear).replace("dd", date).replace("mmm", month).replace("mm", shortMonth);
            } else {
                replaceWith = properties.getProperty(token, "");
                replaceWith = StringUtil.replaceAll(replaceWith, ";", "_");
            }
            replaceWith = replaceWith.replaceAll("\\s", "");
            runFileName = StringUtil.replaceAll(runFileName, "[" + token + "]", replaceWith);
        }
        return runFileName;
    }
}

