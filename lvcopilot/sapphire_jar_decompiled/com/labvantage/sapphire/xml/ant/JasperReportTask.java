/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.sf.jasperreports.engine.JRExporterParameter
 *  net.sf.jasperreports.engine.JasperCompileManager
 *  net.sf.jasperreports.engine.JasperFillManager
 *  net.sf.jasperreports.engine.JasperPrint
 *  net.sf.jasperreports.engine.JasperReport
 *  net.sf.jasperreports.engine.design.JasperDesign
 *  net.sf.jasperreports.engine.export.JRPdfExporter
 *  net.sf.jasperreports.engine.xml.JRXmlLoader
 *  org.apache.tools.ant.BuildException
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import com.labvantage.sapphire.xml.ant.ConnectionTask;
import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class JasperReportTask
extends Task {
    ConnectionTask connection;
    File reportfile;
    File outputfile;

    public void setReportfile(File reportfile) {
        this.reportfile = reportfile;
    }

    public void setOutputfile(File outputfile) {
        this.outputfile = outputfile;
    }

    public void execute() throws BuildException {
        if (this.connection == null) {
            throw new BuildException("Connection task not defined");
        }
        DBUtil dbu = this.connection.getConnection(true);
        HashMap<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("BUILD", Build.getBuild());
        paramsMap.put("BUILDDATE", new Date(System.currentTimeMillis()));
        try {
            this.log("Executing Jasper report " + this.reportfile.getAbsolutePath() + "...");
            JasperDesign jasperDesign = JRXmlLoader.load((File)this.reportfile);
            SapphireJasperUtil.setJasperCompilerClassPatch();
            JasperReport jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
            JasperPrint jasperPrint = JasperFillManager.fillReport((JasperReport)jasperReport, paramsMap, (Connection)dbu.getConnection());
            JRPdfExporter exporter = new JRPdfExporter();
            exporter.setParameter(JRExporterParameter.JASPER_PRINT, (Object)jasperPrint);
            exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, (Object)this.outputfile.getAbsolutePath());
            exporter.exportReport();
        }
        catch (Exception e) {
            throw new BuildException("Failed to run Jasper report '" + this.reportfile.getAbsolutePath() + "'. Reason: " + e.getMessage(), (Throwable)e);
        }
        finally {
            dbu.reset();
        }
        this.log("Execute Jasper Report complete");
    }

    public void addConfiguredConnection(ConnectionTask connection) {
        this.connection = connection;
    }
}

