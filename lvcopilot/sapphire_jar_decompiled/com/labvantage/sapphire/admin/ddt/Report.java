/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRParameter
 *  net.sf.jasperreports.engine.JasperReport
 *  org.apache.commons.io.FileUtils
 */
package com.labvantage.sapphire.admin.ddt;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.report.bo.SapphireBOUtil;
import com.labvantage.sapphire.report.jasper.CommonParamMap;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.file.FileType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.io.FileUtils;
import sapphire.SapphireException;
import sapphire.action.BaseSDCRules;
import sapphire.attachment.Attachment;
import sapphire.report.BaseJavaReport;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class Report
extends BaseSDCRules {
    @Override
    public boolean requiresBeforeEditImage() {
        return true;
    }

    @Override
    public void preAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String reportid = primary.getValue(i, "reportid");
            String reporttypeflag = null;
            this.database.createPreparedResultSet("SELECT\treporttypeflag FROM\treport WHERE\treportid = ?", new Object[]{reportid});
            while (this.database.getNext()) {
                reporttypeflag = this.database.getString("reporttypeflag");
            }
            if (reporttypeflag == null) continue;
            primary.setValue(i, "reporttypeflag", reporttypeflag);
        }
    }

    @Override
    public void preEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String versionstatus = primary.getString(i, "versionstatus");
            String reporttypeflag = primary.getString(i, "reporttypeflag");
            if (reporttypeflag == null || reporttypeflag.length() == 0) {
                reporttypeflag = this.getBeforeEditImage().getDataset("primary").getString(i, "reporttypeflag");
            }
            if (reporttypeflag == null || !reporttypeflag.equalsIgnoreCase("J")) continue;
            String librarydir = primary.getString(i, "librarydir");
            if (librarydir == null || librarydir.length() == 0) {
                librarydir = this.getBeforeEditImage().getDataset("primary").getString(i, "librarydir");
            }
            if (librarydir == null || librarydir.length() <= 0) continue;
            File file = new File(librarydir);
            if (!file.isAbsolute()) {
                file = new File(SapphireJasperUtil.getReportPath(this.getConnectionid(), librarydir) + "/" + librarydir);
            }
            if (file.exists() && file.canRead()) {
                if ("C".equals(versionstatus) && this.hasPrimaryValueChanged(primary, i, "versionstatus")) {
                    long checksumvalue = SapphireJasperUtil.hashFile(file);
                    primary.addColumn("checksumvalue", 1);
                    primary.setValue(i, "checksumvalue", "" + checksumvalue);
                    continue;
                }
                if ("E".equalsIgnoreCase(versionstatus) || !"C".equals(this.getBeforeEditImage().getDataset("primary").getString(i, "versionstatus"))) continue;
                throw new SapphireException("Cannot change an approved(Version Status:current) report.");
            }
            throw new SapphireException("Report File," + file.getAbsolutePath() + " is not found or cannot be read. Cannot Save.");
        }
    }

    @Override
    public void postAdd(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet primary = sdiData.getDataset("primary");
        for (int i = 0; i < primary.size(); ++i) {
            String reporttypeflag = primary.getString(i, "reporttypeflag");
            String librarydir = primary.getString(i, "librarydir");
            String reportid = primary.getString(i, "reportid");
            String reportversionid = primary.getString(i, "reportversionid");
            if (reporttypeflag == null) continue;
            this.database.createPreparedResultSet("existingparams", "select reportid from reportparam where reportid=? and reportversionid=?", new Object[]{reportid, reportversionid});
            if (reporttypeflag.equalsIgnoreCase("J")) {
                if (!this.database.getNext("existingparams") && librarydir != null && librarydir.length() > 0) {
                    File file = new File(librarydir);
                    if (!file.isAbsolute()) {
                        file = new File(SapphireJasperUtil.getReportPath(this.getConnectionid(), librarydir) + "/" + librarydir);
                    }
                    if (!file.exists() || !file.canRead()) continue;
                    DataSet paramds = this.addColumns();
                    try {
                        JasperReport jasperReport = SapphireJasperUtil.loadReport(librarydir, reporttypeflag, new ConnectionInfo((SapphireConnection)this.connectionInfo), reportid);
                        JRParameter[] jrparams = jasperReport.getParameters();
                        int row = 0;
                        for (int p = 0; p < jrparams.length; ++p) {
                            JRParameter param = jrparams[p];
                            if (param.isSystemDefined() || CommonParamMap.isCommonParam(param.getName())) continue;
                            paramds = this.addRows(paramds, row, reportid, reportversionid, param.getName());
                            ++row;
                        }
                        DataSetUtil.insert(this.database, paramds, "reportparam");
                        continue;
                    }
                    catch (Exception e) {
                        throw new SapphireException("Exception when auto insert report params from the registered file," + librarydir + ":" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                    }
                }
                this.logger.info("Report " + reportid + " version " + reportversionid + " has existing reportparams or report location not specified. Will not auto generate.");
                continue;
            }
            if (reporttypeflag.equalsIgnoreCase("X")) {
                if (this.database.getNext("existingparams") || librarydir == null || librarydir.length() <= 0) continue;
                try {
                    SapphireBOUtil boUtil = new SapphireBOUtil(this.connectionInfo);
                    boUtil.restApiLogon();
                    List<String> parameters = boUtil.getBOReportParameter(librarydir == null ? "/" : librarydir);
                    boUtil.restApiLogoff();
                    if (parameters == null) continue;
                    DataSet paramds = this.addColumns();
                    int row = 0;
                    for (int p = 0; p < parameters.size(); ++p) {
                        if (CommonParamMap.isCommonParam(parameters.get(p))) continue;
                        paramds = this.addRows(paramds, row, reportid, reportversionid, parameters.get(p));
                        ++row;
                    }
                    DataSetUtil.insert(this.database, paramds, "reportparam");
                    continue;
                }
                catch (Exception e) {
                    throw new SapphireException("Exception when auto insert report params from the registered report," + librarydir, e);
                }
            }
            if (reporttypeflag.equalsIgnoreCase("C")) {
                String objectname = primary.getString(i, "objectname", "");
                if (this.database.getNext("existingparams") || objectname == null || objectname.length() <= 0) continue;
                try {
                    Class<?> c = Class.forName(objectname);
                    BaseJavaReport javaReport = (BaseJavaReport)c.newInstance();
                    String[] parameters = javaReport.getReportParameters();
                    DataSet paramds = this.addColumns();
                    int row = 0;
                    for (int p = 0; p < parameters.length; ++p) {
                        paramds = this.addRows(paramds, row, reportid, reportversionid, parameters[p]);
                        ++row;
                    }
                    DataSetUtil.insert(this.database, paramds, "reportparam");
                }
                catch (Exception c) {}
                continue;
            }
            if (reporttypeflag.equalsIgnoreCase("K") && actionProps.containsKey("attachmentchanged") && "Y".equalsIgnoreCase(actionProps.getProperty("attachmentchanged", "N"))) {
                File reportFolder = new File(SapphireJasperUtil.getReportPath(this.getConnectionid(), librarydir) + "/" + reportid);
                if (!reportFolder.exists()) continue;
                try {
                    FileUtils.cleanDirectory((File)reportFolder);
                    FileUtils.deleteDirectory((File)reportFolder);
                    continue;
                }
                catch (IOException e) {
                    throw new SapphireException("Unable to clear prior report folder." + reportFolder.toString(), e);
                }
            }
            this.logger.info("Report " + reportid + " version " + reportversionid + " has existing reportparams or report location not specified. Will not auto generate.");
        }
    }

    @Override
    public void preAddDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet reportparammap;
        DataSet reportitem = sdiData.getDataset("reportitem");
        if (OpalUtil.isNotEmpty(reportitem)) {
            for (int r = 0; r < reportitem.getRowCount(); ++r) {
                if (!"C".equals(reportitem.getValue(r, "childreportversionid"))) continue;
                reportitem.setValue(r, "childreportversionid", null);
            }
        }
        if (OpalUtil.isNotEmpty(reportparammap = sdiData.getDataset("reportparammap"))) {
            for (int r = 0; r < reportparammap.getRowCount(); ++r) {
                String reportinfo = reportparammap.getValue(r, "childreportid");
                reportparammap.setValue(r, "childreportid", StringUtil.split(reportinfo, "|")[0]);
            }
        }
    }

    @Override
    public void preEditDetail(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        DataSet reportparammap;
        DataSet reportitem = sdiData.getDataset("reportitem");
        if (OpalUtil.isNotEmpty(reportitem)) {
            for (int r = 0; r < reportitem.getRowCount(); ++r) {
                if (!"C".equals(reportitem.getValue(r, "childreportversionid"))) continue;
                reportitem.setValue(r, "childreportversionid", null);
            }
        }
        if (OpalUtil.isNotEmpty(reportparammap = sdiData.getDataset("reportparammap"))) {
            for (int r = 0; r < reportparammap.getRowCount(); ++r) {
                String reportinfo = reportparammap.getValue(r, "childreportid");
                reportparammap.setValue(r, "childreportid", StringUtil.split(reportinfo, "|")[0]);
            }
        }
    }

    private DataSet addColumns() {
        DataSet paramds = new DataSet();
        paramds.addColumn("reportid", 0);
        paramds.addColumn("reportversionid", 0);
        paramds.addColumn("paramid", 0);
        paramds.addColumn("paramtype", 0);
        paramds.addColumn("paraminto", 0);
        return paramds;
    }

    private DataSet addRows(DataSet paramds, int row, String reportid, String reportversionid, String paramname) {
        paramds.addRow();
        paramds.setValue(row, "reportid", reportid);
        paramds.setValue(row, "reportversionid", reportversionid);
        paramds.setValue(row, "paramid", "param" + (row + 1));
        paramds.setValue(row, "paramtype", "string");
        paramds.setValue(row, "paraminto", paramname);
        return paramds;
    }

    @Override
    public void postEdit(SDIData sdiData, PropertyList actionProps) throws SapphireException {
        this.postAdd(sdiData, actionProps);
    }

    @Override
    public void postDeleteSDIAttachment(Attachment attachment) throws SapphireException {
        FileType f;
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getSourceFilename() != null && attachment.getSourceFilename().length() > 0 && (f = FileType.getFileTypeByFileName(attachment.getSourceFilename(), this.getConnectionId())).getName().equals("JAR")) {
            LabVantageClassLoader.reset(LabVantageClassLoader.ClassLoaderType.REPORT, this.getConnectionInfo().getDatabaseId());
        }
    }

    @Override
    public void postEditSDIAttachment(Attachment attachment) throws SapphireException {
        FileType f;
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getSourceFilename() != null && attachment.getSourceFilename().length() > 0 && (f = FileType.getFileTypeByFileName(attachment.getSourceFilename(), this.getConnectionId())).getName().equals("JAR")) {
            LabVantageClassLoader.reset(LabVantageClassLoader.ClassLoaderType.REPORT, this.getConnectionInfo().getDatabaseId());
        }
    }

    @Override
    public void postAddSDIAttachment(Attachment attachment) throws SapphireException {
        FileType f;
        if (attachment.getAttachmentType() == Attachment.AttachmentType.FILE && attachment.getSourceFilename() != null && attachment.getSourceFilename().length() > 0 && (f = FileType.getFileTypeByFileName(attachment.getSourceFilename(), this.getConnectionId())).getName().equals("JAR")) {
            LabVantageClassLoader.reset(LabVantageClassLoader.ClassLoaderType.REPORT, this.getConnectionInfo().getDatabaseId());
        }
    }
}

