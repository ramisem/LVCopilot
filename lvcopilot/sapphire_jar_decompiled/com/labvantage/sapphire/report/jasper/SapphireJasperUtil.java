/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.pdfview.PDFFile
 *  com.sun.pdfview.PDFPage
 *  com.sun.pdfview.PDFRenderer
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  net.sf.jasperreports.components.table.BaseColumn
 *  net.sf.jasperreports.components.table.StandardTable
 *  net.sf.jasperreports.engine.DefaultJasperReportsContext
 *  net.sf.jasperreports.engine.JRAbstractExporter
 *  net.sf.jasperreports.engine.JRBand
 *  net.sf.jasperreports.engine.JRChild
 *  net.sf.jasperreports.engine.JRDataSource
 *  net.sf.jasperreports.engine.JRElement
 *  net.sf.jasperreports.engine.JRException
 *  net.sf.jasperreports.engine.JRExpressionChunk
 *  net.sf.jasperreports.engine.JRGroup
 *  net.sf.jasperreports.engine.JRPropertiesUtil
 *  net.sf.jasperreports.engine.JRReport
 *  net.sf.jasperreports.engine.JRSection
 *  net.sf.jasperreports.engine.JRStaticText
 *  net.sf.jasperreports.engine.JasperCompileManager
 *  net.sf.jasperreports.engine.JasperFillManager
 *  net.sf.jasperreports.engine.JasperPrint
 *  net.sf.jasperreports.engine.JasperReport
 *  net.sf.jasperreports.engine.JasperReportsContext
 *  net.sf.jasperreports.engine.base.JRBaseImage
 *  net.sf.jasperreports.engine.base.JRBaseSubreport
 *  net.sf.jasperreports.engine.data.JRMapCollectionDataSource
 *  net.sf.jasperreports.engine.design.JRDesignComponentElement
 *  net.sf.jasperreports.engine.design.JRDesignFrame
 *  net.sf.jasperreports.engine.design.JRDesignStaticText
 *  net.sf.jasperreports.engine.design.JRDesignSubreport
 *  net.sf.jasperreports.engine.design.JasperDesign
 *  net.sf.jasperreports.engine.export.HtmlExporter
 *  net.sf.jasperreports.engine.export.HtmlResourceHandler
 *  net.sf.jasperreports.engine.export.JRCsvExporter
 *  net.sf.jasperreports.engine.export.JRPdfExporter
 *  net.sf.jasperreports.engine.export.JRPrintServiceExporter
 *  net.sf.jasperreports.engine.export.JRRtfExporter
 *  net.sf.jasperreports.engine.export.JRXlsExporter
 *  net.sf.jasperreports.engine.export.ooxml.JRDocxExporter
 *  net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
 *  net.sf.jasperreports.engine.util.JRLoader
 *  net.sf.jasperreports.engine.util.JRSaver
 *  net.sf.jasperreports.engine.xml.JRXmlLoader
 *  net.sf.jasperreports.engine.xml.JRXmlWriter
 *  net.sf.jasperreports.export.ExporterConfiguration
 *  net.sf.jasperreports.export.ExporterInput
 *  net.sf.jasperreports.export.ExporterOutput
 *  net.sf.jasperreports.export.ReportExportConfiguration
 *  net.sf.jasperreports.export.SimpleExporterInput
 *  net.sf.jasperreports.export.SimpleHtmlExporterOutput
 *  net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
 *  net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration
 *  net.sf.jasperreports.export.SimpleWriterExporterOutput
 *  net.sf.jasperreports.export.SimpleXlsReportConfiguration
 *  net.sf.jasperreports.export.SimpleXlsxReportConfiguration
 *  net.sf.jasperreports.web.util.WebHtmlResourceHandler
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.maskingrules.DataMaskUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.report.ReportConstants;
import com.labvantage.sapphire.report.jasper.CommonParamMap;
import com.labvantage.sapphire.report.jasper.FormReportDesigner;
import com.labvantage.sapphire.report.jasper.ListReportDesigner;
import com.labvantage.sapphire.report.jasper.ReportDataRetriever;
import com.labvantage.sapphire.report.jasper.ReportDesigner;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.print.PrintService;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.MediaSize;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.PrinterName;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.components.table.BaseColumn;
import net.sf.jasperreports.components.table.StandardTable;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRAbstractExporter;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpressionChunk;
import net.sf.jasperreports.engine.JRGroup;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JRSection;
import net.sf.jasperreports.engine.JRStaticText;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.base.JRBaseImage;
import net.sf.jasperreports.engine.base.JRBaseSubreport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.JRDesignComponentElement;
import net.sf.jasperreports.engine.design.JRDesignFrame;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.engine.xml.JRXmlWriter;
import net.sf.jasperreports.export.ExporterConfiguration;
import net.sf.jasperreports.export.ExporterInput;
import net.sf.jasperreports.export.ExporterOutput;
import net.sf.jasperreports.export.ReportExportConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePrintServiceExporterConfiguration;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.web.util.WebHtmlResourceHandler;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.report.JasperReportScriptlet;
import sapphire.servlet.RequestContext;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SapphireJasperUtil
implements ReportConstants {
    public static String REPORT_ROOT = "";
    private static JasperReportsContext jasperReportsContext;
    private static JRPropertiesUtil jrPropertiesUtil;

    public static void setJasperCompilerClassPatch() {
        String compilerPathSeparator = System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0 ? ":" : ";";
        String compilerPath = REPORT_ROOT + "/lib/jasperreports.jar" + compilerPathSeparator + REPORT_ROOT + "/lib/sapphirejasperhtmlcomponent.jar" + compilerPathSeparator + REPORT_ROOT + "/lib/sapphire.jar" + compilerPathSeparator + REPORT_ROOT + "/lib/iReport.jar";
        Trace.logInfo("::Jasper compilerPath::" + compilerPath);
        jasperReportsContext = DefaultJasperReportsContext.getInstance();
        jrPropertiesUtil = JRPropertiesUtil.getInstance((JasperReportsContext)jasperReportsContext);
        jrPropertiesUtil.setProperty("net.sf.jasperreports.compiler.classpath", compilerPath);
    }

    public static JasperReport loadReport(String reportfile, String reporttypeflag, ConnectionInfo connectionInfo, String reportid) throws Exception {
        return SapphireJasperUtil.loadReport(reportfile, reporttypeflag, connectionInfo, reportid, "", "", false, "");
    }

    public static JasperReport loadReport(String reportfile, String reporttypeflag, ConnectionInfo connectionInfo, String reportid, String versionstatus, String languageid, boolean isFile, String displayType) throws Exception {
        JasperReport jasperReport = null;
        File reportfileFile = new File(reportfile);
        if (!reportfileFile.isAbsolute()) {
            reportfileFile = new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + reportfile);
        }
        if (reportfile.indexOf(".jasper") > 0) {
            jasperReport = (JasperReport)JRLoader.loadObject((File)reportfileFile);
            SapphireJasperUtil.loadSubReport(reportfile, reporttypeflag, versionstatus, jasperReport.getAllBands(), connectionInfo, languageid, isFile, displayType);
        } else {
            if (reportfile.indexOf(".xml") > 0 || reportfile.indexOf(".jrxml") > 0) {
                File jasperFile = new File(reportfile.substring(0, reportfile.indexOf(".")) + (languageid.length() > 0 ? "-" + languageid + ".jasper" : ".jasper"));
                if (!jasperFile.isAbsolute()) {
                    jasperFile = new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + reportfile.substring(0, reportfile.indexOf(".")) + (languageid.length() > 0 ? "-" + languageid + ".jasper" : ".jasper"));
                }
                if (jasperFile.exists() && jasperFile.lastModified() > reportfileFile.lastModified()) {
                    try {
                        jasperReport = (JasperReport)JRLoader.loadObject((File)jasperFile);
                        SapphireJasperUtil.loadSubReport(reportfile, reporttypeflag, versionstatus, jasperReport.getAllBands(), connectionInfo, languageid, isFile, displayType);
                    }
                    catch (Exception e) {
                        throw new Exception(e.getMessage());
                    }
                }
            }
            if (jasperReport == null) {
                JasperDesign jasperDesign = JRXmlLoader.load((File)reportfileFile);
                SapphireJasperUtil.loadSubReport(reportfile, reporttypeflag, versionstatus, jasperDesign.getAllBands(), connectionInfo, languageid, isFile, displayType);
                if (languageid.length() > 0) {
                    TranslationProcessor tp = new TranslationProcessor(connectionInfo.getConnectionId());
                    tp.setLanguage(languageid);
                    tp.setTextType(reportid);
                    JRGroup[] g = jasperDesign.getGroups();
                    for (int i = 0; i < g.length; ++i) {
                        SapphireJasperUtil.translateStaticText(g[i].getGroupHeaderSection(), tp);
                        SapphireJasperUtil.translateStaticText(g[i].getGroupFooterSection(), tp);
                    }
                    SapphireJasperUtil.translateStaticText(jasperDesign.getColumnFooter(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getColumnHeader(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getDetailSection(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getPageFooter(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getLastPageFooter(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getPageHeader(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getSummary(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getTitle(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getSummary(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesign.getNoData(), tp);
                }
                SapphireJasperUtil.setJasperCompilerClassPatch();
                jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
                String filePath = reportfile.substring(0, reportfile.indexOf(".")) + "-" + languageid + ".jasper";
                File file = new File(filePath);
                if (!file.isAbsolute()) {
                    JRSaver.saveObject((Object)jasperReport, (File)new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + reportfile.substring(0, reportfile.indexOf(".")) + (languageid.length() > 0 ? "-" + languageid + ".jasper" : ".jasper")));
                } else {
                    JRSaver.saveObject((Object)jasperReport, (File)new File(reportfile.substring(0, reportfile.indexOf(".")) + (languageid.length() > 0 ? "-" + languageid + ".jasper" : ".jasper")));
                }
            }
        }
        return jasperReport;
    }

    public static void loadSubReport(String reportfile, String reporttypeflag, String versionstatus, JRBand[] jrAllBands, ConnectionInfo connectionInfo, String languageid, boolean isFile, String displayTpe) throws Exception {
        String reportFolder = reportfile.substring(0, reportfile.contains("/") ? reportfile.lastIndexOf(47) : reportfile.lastIndexOf(92));
        JasperReport jasperReport = null;
        JRBand[] jrBands = jrAllBands;
        List elements = null;
        for (int i = 0; i < jrBands.length; ++i) {
            elements = jrBands[i].getChildren();
            for (JRChild childElement : elements) {
                File subReportJasperFile;
                String filePath;
                File subJasperFile;
                JRBaseSubreport subReportElement;
                JasperDesign jasperDesignSubReport = null;
                if (childElement instanceof JRBaseImage) {
                    JRBaseImage image = (JRBaseImage)childElement;
                    if (isFile && displayTpe.equalsIgnoreCase("html")) {
                        image.setLazy(true);
                    }
                }
                if (!(childElement instanceof JRBaseSubreport) && !(childElement instanceof JRDesignSubreport)) continue;
                JRExpressionChunk[] expressionChunks = null;
                if (childElement instanceof JRBaseSubreport) {
                    subReportElement = (JRBaseSubreport)childElement;
                    expressionChunks = subReportElement.getExpression().getChunks();
                } else if (childElement instanceof JRDesignSubreport) {
                    subReportElement = (JRDesignSubreport)childElement;
                    expressionChunks = subReportElement.getExpression().getChunks();
                }
                ArrayList<String> expressionText = new ArrayList<String>();
                if (expressionChunks != null) {
                    for (int chunk = 0; chunk < expressionChunks.length; ++chunk) {
                        expressionText.add(expressionChunks[chunk].getText());
                    }
                }
                if (!expressionText.contains("SUBREPORT_DIR") && !expressionText.contains("SAPPHIRE_ReportPath")) continue;
                String subReportid = "";
                for (int j = 0; j < expressionText.size(); ++j) {
                    if (!expressionText.get(j).toString().contains(".jasper") || !expressionText.get(j).toString().contains("+")) continue;
                    String expression = expressionText.get(j).toString().replaceAll("\\s", "").trim();
                    subReportid = expression.substring(expression.indexOf(43) + 2, expression.length() - 1).replace(".jasper", ".jrxml");
                    break;
                }
                if (subReportid.length() <= 0) continue;
                File subReportFile = new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + reportfile.substring(0, reportfile.contains("/") ? reportfile.lastIndexOf(47) : reportfile.lastIndexOf(92)) + "/" + subReportid);
                if ("C".equals(versionstatus) && !reporttypeflag.equals("K")) {
                    subJasperFile = new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + reportfile.substring(0, reportfile.contains("/") ? reportfile.lastIndexOf(47) : reportfile.lastIndexOf(92)) + "/" + subReportid.replace(".jrxml", ".jasper"));
                    if (languageid.length() > 0) {
                        subJasperFile = new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + reportfile.substring(0, reportfile.contains("/") ? reportfile.lastIndexOf(47) : reportfile.lastIndexOf(92)) + "/" + subReportid.replace(".jrxml", "-" + languageid + ".jasper"));
                    }
                    if (subJasperFile.exists() && subJasperFile.lastModified() < subReportFile.lastModified()) {
                        throw new SapphireException("File:" + subReportFile.getAbsolutePath() + " has been changed after the report version was approved.");
                    }
                }
                if ((subJasperFile = languageid.length() > 0 ? new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + reportfile.substring(0, reportfile.contains("/") ? reportfile.lastIndexOf(47) : reportfile.lastIndexOf(92)) + "/" + subReportid.replace(".jrxml", "-" + languageid + ".jasper")) : new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + reportfile.substring(0, reportfile.contains("/") ? reportfile.lastIndexOf(47) : reportfile.lastIndexOf(92)) + "/" + subReportid.replace(".jrxml", ".jasper"))).exists()) continue;
                jasperDesignSubReport = JRXmlLoader.load((File)subReportFile);
                SapphireJasperUtil.loadSubReport(reportFolder + "/" + subReportid, reporttypeflag, versionstatus, jasperDesignSubReport.getAllBands(), connectionInfo, languageid, isFile, displayTpe);
                if (languageid.length() > 0) {
                    TranslationProcessor tp = new TranslationProcessor(connectionInfo.getConnectionId());
                    tp.setLanguage(languageid);
                    tp.setTextType(subReportid);
                    JRGroup[] g = jasperDesignSubReport.getGroups();
                    for (int k = 0; k < g.length; ++k) {
                        SapphireJasperUtil.translateStaticText(g[k].getGroupHeaderSection(), tp);
                        SapphireJasperUtil.translateStaticText(g[k].getGroupFooterSection(), tp);
                    }
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getColumnFooter(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getColumnHeader(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getDetailSection(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getPageFooter(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getLastPageFooter(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getPageHeader(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getSummary(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getTitle(), tp);
                    SapphireJasperUtil.translateStaticText(jasperDesignSubReport.getSummary(), tp);
                }
                SapphireJasperUtil.setJasperCompilerClassPatch();
                jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesignSubReport);
                if (languageid.length() > 0) {
                    filePath = reportfile.substring(0, reportfile.contains("/") ? reportfile.lastIndexOf(47) : reportfile.lastIndexOf(92)) + "/" + subReportid.replace(".jrxml", "-" + languageid + ".jasper");
                    subReportJasperFile = new File(filePath);
                    if (!subReportJasperFile.isAbsolute()) {
                        subReportJasperFile = new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + filePath);
                    }
                    if (!subReportJasperFile.exists() || subReportJasperFile.lastModified() < subReportFile.lastModified()) {
                        if (subReportJasperFile.isAbsolute()) {
                            JRSaver.saveObject((Object)jasperReport, (File)subReportJasperFile);
                        } else {
                            JRSaver.saveObject((Object)jasperReport, (File)new File(filePath));
                        }
                    }
                }
                if (!(subReportJasperFile = new File(filePath = reportfile.substring(0, reportfile.contains("/") ? reportfile.lastIndexOf(47) : reportfile.lastIndexOf(92)) + "/" + subReportid.replace(".jrxml", ".jasper"))).isAbsolute()) {
                    JRSaver.saveObject((Object)jasperReport, (File)new File(SapphireJasperUtil.getReportPath(connectionInfo.getConnectionId(), reportfile) + "/" + (reporttypeflag.equals("K") ? connectionInfo.getDatabaseId() : "") + "/" + filePath));
                    continue;
                }
                JRSaver.saveObject((Object)jasperReport, (File)new File(filePath));
            }
        }
    }

    private static void translateStaticText(JRSection section, TranslationProcessor tp) {
        if (section != null && section.getBands() != null) {
            JRBand[] bands = section.getBands();
            for (int b = 0; b < bands.length; ++b) {
                SapphireJasperUtil.translateStaticText(bands[b], tp);
            }
        }
    }

    private static void translateStaticText(JRBand band, TranslationProcessor tp) {
        if (band != null && band.getElements() != null) {
            JRElement[] elements = band.getElements();
            for (int i = 0; i < elements.length; ++i) {
                if (elements[i] instanceof JRDesignStaticText) {
                    String text = ((JRDesignStaticText)elements[i]).getText();
                    String transtext = tp.translate(text);
                    ((JRDesignStaticText)elements[i]).setText(transtext);
                    continue;
                }
                if (elements[i] instanceof JRDesignComponentElement) {
                    if (!((JRDesignComponentElement)elements[i]).getComponentKey().getName().equals("table")) continue;
                    StandardTable component = (StandardTable)((JRDesignComponentElement)elements[i]).getComponent();
                    List baseColumns = component.getColumns();
                    for (int j = 0; j < baseColumns.size(); ++j) {
                        JRElement[] tableColumnElements;
                        if (((BaseColumn)baseColumns.get(j)).getColumnHeader() == null || (tableColumnElements = ((BaseColumn)baseColumns.get(j)).getColumnHeader().getElements()).length <= 0 || !(tableColumnElements[0] instanceof JRDesignStaticText)) continue;
                        String text = ((JRDesignStaticText)tableColumnElements[0]).getText();
                        String transText = tp.translate(text);
                        ((JRDesignStaticText)tableColumnElements[0]).setText(transText);
                    }
                    continue;
                }
                if (!(elements[i] instanceof JRDesignFrame)) continue;
                SapphireJasperUtil.translateStaticText((JRDesignFrame)elements[i], tp);
            }
        }
    }

    private static void translateStaticText(JRDesignFrame jrDesignFrame, TranslationProcessor tp) {
        JRElement[] childElements;
        for (JRElement jrElement : childElements = jrDesignFrame.getElements()) {
            if (jrElement instanceof JRDesignStaticText) {
                SapphireJasperUtil.translateStaticText((JRDesignStaticText)jrElement, tp);
            }
            if (!(jrElement instanceof JRDesignFrame)) continue;
            SapphireJasperUtil.translateStaticText((JRDesignFrame)jrElement, tp);
        }
    }

    private static void translateStaticText(JRDesignStaticText jrDesignStaticText, TranslationProcessor tp) {
        String text = jrDesignStaticText.getText();
        String transtext = tp.translate(text);
        jrDesignStaticText.setText(transtext);
    }

    public static JasperReport loadReport(String reportfile, TranslationProcessor tp) throws Exception {
        String languagepre = tp.getLanguage() != null && tp.getLanguage().length() > 0 ? tp.getLanguage() + "-" : "";
        JasperReport jasperReport = null;
        File reportfileFile = new File(SapphireJasperUtil.getReportPath(tp.getConnectionid(), reportfile) + "/" + reportfile);
        if (reportfile.indexOf(".jasper") > 0) {
            jasperReport = (JasperReport)JRLoader.loadObject((File)reportfileFile);
        } else {
            File jasperFile;
            if ((reportfile.indexOf(".xml") > 0 || reportfile.indexOf(".jrxml") > 0) && (jasperFile = new File(SapphireJasperUtil.getReportPath(tp.getConnectionid(), reportfile) + "/" + languagepre + reportfile.substring(0, reportfile.indexOf(".")) + "jasper")).exists() && jasperFile.lastModified() > reportfileFile.lastModified()) {
                try {
                    jasperReport = (JasperReport)JRLoader.loadObject((File)jasperFile);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (jasperReport == null) {
                JasperDesign jasperDesign = JRXmlLoader.load((File)reportfileFile);
                JRElement[] elements = jasperDesign.getDetailSection().getBands()[0].getElements();
                for (int i = 0; i < elements.length; ++i) {
                    if (!(elements[i] instanceof JRStaticText)) continue;
                    ((JRStaticText)elements[i]).setText("Hello");
                }
                SapphireJasperUtil.setJasperCompilerClassPatch();
                jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
                JRSaver.saveObject((Object)jasperReport, (File)new File(SapphireJasperUtil.getReportPath(tp.getConnectionid(), reportfile) + "/" + languagepre + reportfile.substring(0, reportfile.indexOf(".")) + ".jasper"));
            }
        }
        return jasperReport;
    }

    public static void checkDynamicGroupBy(PropertyList element, ServletRequest request) {
        PropertyListCollection groupbys = element.getCollection("groupby");
        if (groupbys != null && groupbys.size() > 0) {
            String groupbycolid = request.getParameter("groupby");
            if (groupbycolid == null || groupbycolid.length() == 0) {
                element.remove("groupby");
            } else {
                int foundIndex = -1;
                for (int i = 0; !(i >= groupbys.size() || i == 0 && groupbycolid.equals(groupbys.getPropertyList(i).getProperty("columnid"))); ++i) {
                    if (!groupbycolid.equals(groupbys.getPropertyList(i).getProperty("columnid"))) continue;
                    foundIndex = i;
                    break;
                }
                if (foundIndex > 0) {
                    PropertyList currentGroupBy = groupbys.getPropertyList(foundIndex);
                    groupbys.remove(foundIndex);
                    groupbys.add(0, currentGroupBy);
                }
            }
        }
    }

    public static void exportPage(String pageid, String keyid1, String keyid2, String keyid3, HttpServletRequest request, HttpServletResponse response) throws SapphireException {
        RequestContext requestContext = (RequestContext)request.getAttribute("RequestContext");
        ConnectionInfo connectionInfo = new ConnectionProcessor(requestContext.getConnectionId()).getConnectionInfo(requestContext.getConnectionid());
        RequestProcessor rp = new RequestProcessor(requestContext.getConnectionId());
        try {
            PropertyList pagedata = rp.getWebPageProperties(pageid, requestContext);
            PropertyList element = pagedata.getPropertyList("list");
            SapphireJasperUtil.checkDynamicGroupBy(element, (ServletRequest)request);
            String displaytype = request.getParameter("displaytype");
            if (displaytype == null || displaytype.length() == 0) {
                displaytype = "pdf";
            }
            String title = request.getParameter("title");
            boolean type = false;
            String listorform = "List";
            if (element == null) {
                element = pagedata.getPropertyList("maint");
                type = true;
                listorform = "Form";
            }
            String sdcid = element.getProperty("sdcid");
            if (title == null || title.length() == 0) {
                title = sdcid + " " + listorform;
            }
            SDIData sdidata = ReportDataRetriever.getSDIData(sdcid, keyid1, keyid2, keyid3, element, requestContext);
            DataSet ds = sdidata.getDataset("primary");
            JasperDesign jasperDesign = null;
            JasperReport jasperReport = null;
            String generatedDir = REPORT_ROOT + "/generated";
            File gDir = new File(generatedDir);
            if (!gDir.exists()) {
                gDir.mkdir();
            }
            if (!"xls".equals(displaytype) || !"xlsx".equals(displaytype)) {
                // empty if block
            }
            if (jasperReport == null) {
                ds = DataMaskUtil.getMaskedDataSetOnly(ds);
                File file = new File(REPORT_ROOT + "/OOB/sapphireListPage.jrxml");
                if (!file.exists()) {
                    file = new File(REPORT_ROOT + "/OOB/sapphireListPage.xml");
                }
                if (file.exists()) {
                    jasperDesign = JRXmlLoader.load((File)file);
                    jasperDesign.setQuery(null);
                }
                ReportDesigner reportDesigner = null;
                if (jasperDesign == null || "xls".equals(displaytype) || "xlsx".equals(displaytype) || "csv".equals(displaytype)) {
                    if (!type) {
                        reportDesigner = new ListReportDesigner(title, element);
                    } else if (type) {
                        reportDesigner = new FormReportDesigner(title, element);
                    }
                    reportDesigner.setExampleData(ds);
                    reportDesigner.setDisplayType(displaytype);
                    jasperDesign = reportDesigner.createJasperDesign();
                    FileOutputStream fileout = new FileOutputStream(generatedDir + "/" + pageid + ("xls".equals(displaytype) ? "_xls.jrxml" : ("xlsx".equals(displaytype) ? "_xlsx.jrxml" : "_csv.jrxml")));
                    JRXmlWriter.writeReport((JRReport)jasperDesign, (OutputStream)fileout, (String)"UTF-8");
                    fileout.close();
                } else {
                    if (!type) {
                        reportDesigner = new ListReportDesigner(title, element, jasperDesign);
                    } else if (type) {
                        reportDesigner = new FormReportDesigner(title, element, jasperDesign);
                    }
                    reportDesigner.setExampleData(ds);
                    reportDesigner.setDisplayType(displaytype);
                    jasperDesign = reportDesigner.modifyJasperDesign();
                    FileOutputStream fileout = new FileOutputStream(generatedDir + "/" + pageid + ".jrxml");
                    JRXmlWriter.writeReport((JRReport)jasperDesign, (OutputStream)fileout, (String)"UTF-8");
                    fileout.close();
                }
                SapphireJasperUtil.setJasperCompilerClassPatch();
                jasperReport = JasperCompileManager.compileReport((JasperDesign)jasperDesign);
                if (!("xls".equals(displaytype) || "xlsx".equals(displaytype) || "csv".equals(displaytype))) {
                    JRSaver.saveObject((Object)jasperReport, (File)new File(generatedDir + "/" + pageid + ".jasper"));
                } else {
                    JRSaver.saveObject((Object)jasperReport, (File)new File(generatedDir + "/" + pageid + ("xls".equals(displaytype) ? "_xls.jrxml" : ("xlsx".equals(displaytype) ? "_xlsx.jrxml" : "_csv.jrxml"))));
                }
            }
            JRMapCollectionDataSource datasource = new JRMapCollectionDataSource((Collection)ds);
            CommonParamMap paramsMap = new CommonParamMap(request);
            paramsMap.put("displayType", displaytype);
            if ("pdf".equals(displaytype)) {
                SapphireJasperUtil.runReportToWebPdf(SapphireJasperUtil.getReportBytes(jasperReport, (Map)paramsMap, (JRDataSource)datasource), response, pageid, connectionInfo, "", false);
            } else if ("html".equals(displaytype)) {
                SapphireJasperUtil.runReportToWebHtml(SapphireJasperUtil.getReportBytes(jasperReport, (Map)paramsMap, (JRDataSource)datasource), response, pageid, connectionInfo, "", false);
            } else if ("xls".equals(displaytype) || "xlsx".equals(displaytype)) {
                SapphireJasperUtil.runReportToWebExcel(SapphireJasperUtil.getReportBytes(jasperReport, (Map)paramsMap, (JRDataSource)datasource), response, pageid, connectionInfo, "", false, displaytype);
            } else if ("rtf".equals(displaytype)) {
                SapphireJasperUtil.runReportToWebRTF(SapphireJasperUtil.getReportBytes(jasperReport, (Map)paramsMap, (JRDataSource)datasource), response, pageid, connectionInfo, "", false);
            } else if ("doc".equals(displaytype) || "docx".equals(displaytype)) {
                SapphireJasperUtil.runReportToWebWord(SapphireJasperUtil.getReportBytes(jasperReport, (Map)paramsMap, (JRDataSource)datasource), response, pageid, connectionInfo, "", false, displaytype);
            } else if ("csv".equals(displaytype)) {
                SapphireJasperUtil.runReportToWebCSV(SapphireJasperUtil.getReportBytes(jasperReport, (Map)paramsMap, (JRDataSource)datasource), response, pageid, connectionInfo, "", false);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Error Exporting Page:" + pageid + ":" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(connectionInfo.getConnectionId())), e);
        }
    }

    public static byte[] getReportBytes(JasperReport jasperReport, Map paramsMap, JRDataSource datasource) throws JRException {
        JasperPrint jasperPrint = JasperFillManager.fillReport((JasperReport)jasperReport, (Map)paramsMap, (JRDataSource)datasource);
        return SapphireJasperUtil.getReportBytes(paramsMap, jasperPrint);
    }

    public static byte[] getReportBytes(JasperReport jasperReport, Map paramsMap, Connection conn) throws JRException {
        JasperPrint jasperPrint = JasperFillManager.fillReport((JasperReport)jasperReport, (Map)paramsMap, (Connection)conn);
        return SapphireJasperUtil.getReportBytes(paramsMap, jasperPrint);
    }

    public static byte[] getReportBytes(Map paramsMap, JasperPrint jasperPrint) throws JRException {
        String displayType = (String)paramsMap.get("displayType");
        if (displayType == null || displayType.isEmpty()) {
            displayType = "pdf";
        }
        HttpServletRequest request = paramsMap.get("request") != null ? (HttpServletRequest)paramsMap.get("request") : null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JRAbstractExporter exporter = SapphireJasperUtil.getExporter(displayType);
        exporter.setExporterInput((ExporterInput)new SimpleExporterInput(jasperPrint));
        ExporterOutput exporterOutput = SapphireJasperUtil.getExportOutput(displayType, baos);
        if (displayType.equals("html") && request != null && exporterOutput != null) {
            ((SimpleHtmlExporterOutput)exporterOutput).setImageHandler((HtmlResourceHandler)new WebHtmlResourceHandler("jasperReport/image?image={0}"));
            request.getSession().setAttribute("net.sf.jasperreports.j2ee.jasper_print", (Object)jasperPrint);
        }
        exporter.setExporterOutput(exporterOutput);
        exporter.exportReport();
        if (paramsMap.get("REPORT_SCRIPTLET") != null) {
            HashMap fieldvaluesMap = null;
            if (paramsMap.get("REPORT_SCRIPTLET") instanceof JasperReportScriptlet) {
                fieldvaluesMap = ((JasperReportScriptlet)((Object)paramsMap.get("REPORT_SCRIPTLET"))).getFieldValueMap();
            }
            if (fieldvaluesMap != null) {
                paramsMap.putAll(fieldvaluesMap);
            }
        }
        return baos.toByteArray();
    }

    public static void runReportToWebPdf(byte[] reportBytes, HttpServletResponse response, String reportid, ConnectionInfo connectionInfo, String fileName, boolean isFile) throws IOException {
        response.setContentLength(reportBytes.length);
        if (OpalUtil.isNotEmpty(reportid)) {
            reportid = SapphireJasperUtil.generateReportID(reportid, connectionInfo);
            if (isFile && OpalUtil.isNotEmpty(fileName)) {
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            } else if (isFile) {
                response.setHeader("Content-Disposition", "attachment; filename=" + reportid + (!reportid.endsWith("pdf") ? ".pdf" : ""));
            } else {
                response.setHeader("Content-Disposition", "inline; filename=" + reportid + (!reportid.endsWith("pdf") ? ".pdf" : ""));
            }
        }
        if (isFile) {
            response.setContentType("application/x-download");
        } else {
            response.setContentType("application/pdf");
        }
        SapphireJasperUtil.writeResponseFromByte(reportBytes, response);
    }

    public static void runReportToWebExcel(byte[] reportByte, HttpServletResponse response, String reportid, ConnectionInfo connectionInfo, String fileName, boolean isFile, String displayType) throws IOException {
        response.setContentLength(reportByte.length);
        if (OpalUtil.isNotEmpty(reportid)) {
            reportid = SapphireJasperUtil.generateReportID(reportid, connectionInfo);
            if (isFile && OpalUtil.isNotEmpty(fileName)) {
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=" + reportid + (!reportid.endsWith("xls") || !reportid.endsWith("xlsx") ? (displayType.equals("xls") ? ".xls" : ".xlsx") : ""));
            }
        }
        if (displayType.equals("xls")) {
            response.setContentType("application/vnd.ms-excel");
        } else {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        }
        SapphireJasperUtil.writeResponseFromByte(reportByte, response);
    }

    public static void runReportToWebRTF(byte[] reportByte, HttpServletResponse response, String reportid, ConnectionInfo connectionInfo, String fileName, boolean isFile) throws IOException {
        response.setContentLength(reportByte.length);
        if (OpalUtil.isNotEmpty(reportid)) {
            reportid = SapphireJasperUtil.generateReportID(reportid, connectionInfo);
            if (isFile && OpalUtil.isNotEmpty(fileName)) {
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=" + reportid + (!reportid.endsWith("rtf") ? ".rtf" : ""));
            }
        }
        response.setContentType("application/msword");
        SapphireJasperUtil.writeResponseFromByte(reportByte, response);
    }

    public static void runReportToWebWord(byte[] reportByte, HttpServletResponse response, String reportid, ConnectionInfo connectionInfo, String fileName, boolean isFile, String displayType) throws IOException {
        response.setContentLength(reportByte.length);
        if (OpalUtil.isNotEmpty(reportid)) {
            reportid = SapphireJasperUtil.generateReportID(reportid, connectionInfo);
            if (isFile && OpalUtil.isNotEmpty(fileName)) {
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=" + reportid + (!reportid.endsWith("doc") || !reportid.endsWith("docx") ? ("doc".equals(displayType) ? ".doc" : ".docx") : ""));
            }
        }
        if (displayType.equals("doc")) {
            response.setContentType("application/msword");
        } else {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }
        SapphireJasperUtil.writeResponseFromByte(reportByte, response);
    }

    public static void runReportToWebCSV(byte[] reportByte, HttpServletResponse response, String reportid, ConnectionInfo connectionInfo, String fileName, boolean isFile) throws IOException {
        response.setContentLength(reportByte.length);
        if (OpalUtil.isNotEmpty(reportid)) {
            reportid = SapphireJasperUtil.generateReportID(reportid, connectionInfo);
            if (isFile && OpalUtil.isNotEmpty(fileName)) {
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            } else {
                response.setHeader("Content-Disposition", "attachment; filename=" + reportid + (!reportid.endsWith("csv") ? ".csv" : ""));
            }
        }
        response.setContentType("text/csv");
        SapphireJasperUtil.writeResponseFromByte(reportByte, response);
    }

    public static void runReportToWebHtml(byte[] reportByte, HttpServletResponse response, String reportid, ConnectionInfo connectionInfo, String fileName, boolean isFile) throws IOException {
        response.setContentLength(reportByte.length);
        if (OpalUtil.isNotEmpty(reportid)) {
            reportid = SapphireJasperUtil.generateReportID(reportid, connectionInfo);
            if (isFile && OpalUtil.isNotEmpty(fileName)) {
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
            } else {
                response.setHeader("Content-Disposition", "inline; filename=" + reportid + (!reportid.endsWith("html") ? ".html" : ""));
            }
        }
        if (isFile) {
            response.setContentType("application/x-download");
        } else {
            response.setContentType("text/html");
        }
        SapphireJasperUtil.writeResponseFromByte(reportByte, response);
    }

    private static void writeResponseFromByte(byte[] reportByte, HttpServletResponse response) throws IOException {
        ServletOutputStream ouputStream = response.getOutputStream();
        ouputStream.write(reportByte, 0, reportByte.length);
        ouputStream.flush();
        ouputStream.close();
    }

    private static String generateReportID(String reportid, ConnectionInfo connectionInfo) {
        String uesrid = connectionInfo.getSysuserId();
        reportid = reportid + "_" + uesrid + "_" + new SimpleDateFormat("MMM-dd-yyyy-hh-mm-ss").format(new Date());
        reportid = HttpUtil.encodeURIComponent(reportid);
        return reportid;
    }

    public static JasperPrint getJasperPrint(JasperReport jasperReport, Map paramsMap, JRDataSource datasource) throws JRException {
        JasperPrint jasperPrint = JasperFillManager.fillReport((JasperReport)jasperReport, (Map)paramsMap, (JRDataSource)datasource);
        return jasperPrint;
    }

    public static JasperPrint getJasperPrint(JasperReport jasperReport, Map paramsMap, Connection conn) throws JRException {
        JasperPrint jasperPrint = JasperFillManager.fillReport((JasperReport)jasperReport, (Map)paramsMap, (Connection)conn);
        if (paramsMap.get("REPORT_SCRIPTLET") != null) {
            HashMap fieldvaluesMap = null;
            if (paramsMap.get("REPORT_SCRIPTLET") instanceof JasperReportScriptlet) {
                fieldvaluesMap = ((JasperReportScriptlet)((Object)paramsMap.get("REPORT_SCRIPTLET"))).getFieldValueMap();
            }
            if (fieldvaluesMap != null) {
                paramsMap.putAll(fieldvaluesMap);
            }
        }
        return jasperPrint;
    }

    private static ExporterOutput getExportOutput(String displayType, ByteArrayOutputStream baos) {
        SimpleOutputStreamExporterOutput exporterOutput = null;
        switch (displayType) {
            case "pdf": 
            case "xls": 
            case "xlsx": 
            case "docx": 
            case "doc": {
                exporterOutput = new SimpleOutputStreamExporterOutput((OutputStream)baos);
                break;
            }
            case "rtf": 
            case "csv": {
                exporterOutput = new SimpleWriterExporterOutput((OutputStream)baos);
                break;
            }
            case "html": {
                exporterOutput = new SimpleHtmlExporterOutput((OutputStream)baos, "UTF-8");
                break;
            }
        }
        return exporterOutput;
    }

    public static void exportReportToPrinter(JasperPrint jasperPrint, String printer) throws SapphireException {
        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setExporterInput((ExporterInput)new SimpleExporterInput(jasperPrint));
        float pageHeight = jasperPrint.getPageHeight();
        float pageWidth = jasperPrint.getPageWidth();
        HashPrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        MediaSizeName mediaSizeName = MediaSize.findMedia(pageWidth / 72.0f, pageHeight / 72.0f, 25400);
        aset.add(new Copies(1));
        aset.add(mediaSizeName);
        SimplePrintServiceExporterConfiguration printServiceExporterConfiguration = new SimplePrintServiceExporterConfiguration();
        printServiceExporterConfiguration.setPrintRequestAttributeSet((PrintRequestAttributeSet)aset);
        exporter.setConfiguration((ExporterConfiguration)printServiceExporterConfiguration);
        if (printer != null && printer.length() > 0) {
            SimplePrintServiceExporterConfiguration simplePrintServiceExporterConfiguration = new SimplePrintServiceExporterConfiguration();
            HashPrintServiceAttributeSet serviceAttributeSet = new HashPrintServiceAttributeSet();
            serviceAttributeSet.add(new PrinterName(printer, null));
            simplePrintServiceExporterConfiguration.setPrintServiceAttributeSet((PrintServiceAttributeSet)serviceAttributeSet);
            exporter.setConfiguration((ExporterConfiguration)simplePrintServiceExporterConfiguration);
        }
        try {
            exporter.exportReport();
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static byte[] exportAndPrintReport(JasperPrint jasperPrint, String printer) throws SapphireException {
        byte[] reportByte = null;
        String path = REPORT_ROOT + "/generated";
        File filePath = new File(path);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        String filepath = path + "/l7v1s0.pdf";
        SapphireJasperUtil.exportReportToFile(jasperPrint, "pdf", filepath);
        try {
            File file = new File(filepath);
            reportByte = Files.readAllBytes(file.toPath());
            ByteBuffer bb = ByteBuffer.wrap(reportByte);
            final PDFFile pdfFile = new PDFFile(bb);
            Printable printable = new Printable(){

                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    int pagenum = pageIndex + 1;
                    if (pagenum >= 1 && pagenum <= pdfFile.getNumPages()) {
                        Graphics2D graphics2D = (Graphics2D)graphics;
                        PDFPage page = pdfFile.getPage(pagenum);
                        Rectangle imageArea = new Rectangle((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY(), (int)pageFormat.getImageableWidth(), (int)pageFormat.getImageableHeight());
                        graphics2D.translate(0, 0);
                        PDFRenderer pdfRenderer = new PDFRenderer(page, graphics2D, imageArea, null, null);
                        try {
                            page.waitForFinish();
                            pdfRenderer.run();
                        }
                        catch (InterruptedException exception) {
                            exception.printStackTrace();
                        }
                        return 0;
                    }
                    return 1;
                }
            };
            PrintService myPrinterService = null;
            PrintService[] ps = PrinterJob.lookupPrintServices();
            for (int i = 0; i < ps.length; ++i) {
                String printerName = ps[i].getName();
                if (!printerName.equalsIgnoreCase(printer)) continue;
                myPrinterService = ps[i];
                break;
            }
            if (myPrinterService == null) {
                throw new SapphireException("Invalid print service name:" + printer);
            }
            PrinterJob pjob = PrinterJob.getPrinterJob();
            pjob.setPrintService(myPrinterService);
            PageFormat pf = pjob.defaultPage();
            Paper paper = pf.getPaper();
            paper.setSize(pdfFile.getPage(1).getWidth(), pdfFile.getPage(1).getHeight());
            paper.setImageableArea(0.0, 0.0, pdfFile.getPage(1).getWidth(), pdfFile.getPage(1).getHeight());
            pf.setPaper(paper);
            Book book = new Book();
            book.append(printable, pf, pdfFile.getNumPages());
            pjob.setPageable(book);
            pjob.print();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to print Report:" + e.getMessage());
        }
        return reportByte;
    }

    public static void exportReportToFile(JasperPrint jasperPrint, String displaytype, String filename, HashMap paramMap) throws SapphireException {
        JRAbstractExporter exporter = null;
        exporter = paramMap != null && paramMap.get("exporterobject") != null && paramMap.get("exporterobject") instanceof JRAbstractExporter ? (JRAbstractExporter)paramMap.get("exporterobject") : SapphireJasperUtil.getExporter(displaytype);
        exporter.setExporterInput((ExporterInput)new SimpleExporterInput(jasperPrint));
        if (displaytype.equalsIgnoreCase("html")) {
            exporter.setExporterOutput((ExporterOutput)new SimpleHtmlExporterOutput(filename, "UTF-8"));
        } else if (displaytype.equalsIgnoreCase("rtf") || displaytype.equalsIgnoreCase("csv")) {
            exporter.setExporterOutput((ExporterOutput)new SimpleWriterExporterOutput(filename));
        } else {
            exporter.setExporterOutput((ExporterOutput)new SimpleOutputStreamExporterOutput(filename));
        }
        try {
            exporter.exportReport();
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    public static void exportReportToFile(JasperPrint jasperPrint, String displaytype, String filename) throws SapphireException {
        SapphireJasperUtil.exportReportToFile(jasperPrint, displaytype, filename, null);
    }

    public static long hashFile(File writtenfile) throws SapphireException {
        long fhdest = 0L;
        try {
            FileInputStream fos = new FileInputStream(writtenfile);
            FileChannel fcout = fos.getChannel();
            int sz = (int)fcout.size();
            ByteBuffer bbin = ByteBuffer.allocate(131072);
            int nbytes = -1;
            long offset = 0L;
            while ((nbytes = fcout.read(bbin)) != -1) {
                bbin.rewind();
                fhdest += SapphireJasperUtil.hashContents(bbin.array(), nbytes, offset);
                offset += (long)nbytes;
            }
        }
        catch (Exception e) {
            throw new SapphireException("Exception when calculate checksum for file " + writtenfile.getAbsolutePath(), e);
        }
        return fhdest;
    }

    private static long hashContents(byte[] contents, int bytesread, long offset) {
        long iHash = 0L;
        for (int i = 0; i < bytesread; ++i) {
            iHash += (offset + (long)i) * (long)contents[i];
        }
        return iHash;
    }

    private static JRAbstractExporter getExporter(String displaytype) {
        HtmlExporter exporter = null;
        try {
            Class<?> customClass = null;
            if (displaytype.equals("html")) {
                customClass = Class.forName("sapphire.custom.report.CustomJRHtmlExporter");
            } else if (displaytype.equals("xls")) {
                customClass = Class.forName("sapphire.custom.report.CustomJRXlsExporter");
            } else if (displaytype.equals("xlsx")) {
                customClass = Class.forName("sapphire.custom.report.CustomJRXlsxExporter");
            } else if (displaytype.equals("pdf")) {
                customClass = Class.forName("sapphire.custom.report.CustomJRPdfExporter");
            } else if (displaytype.equals("doc") || displaytype.equals("docx")) {
                customClass = Class.forName("sapphire.custom.report.CustomJRDocxExporter");
            } else if (displaytype.equals("csv")) {
                customClass = Class.forName("sapphire.custom.report.CustomJRCsvExporter");
            }
            if (customClass != null) {
                exporter = (JRAbstractExporter)customClass.newInstance();
            }
            Trace.logInfo("Found custom jasper exporter..." + customClass.getName());
        }
        catch (Exception customClass) {
            // empty catch block
        }
        if (exporter == null) {
            SimpleXlsReportConfiguration configuration;
            if (displaytype.equals("html")) {
                exporter = new HtmlExporter();
            } else if (displaytype.equals("xls")) {
                exporter = new JRXlsExporter();
                configuration = new SimpleXlsReportConfiguration();
                configuration.setDetectCellType(Boolean.valueOf(true));
                configuration.setRemoveEmptySpaceBetweenColumns(Boolean.valueOf(true));
                configuration.setMaxRowsPerSheet(Integer.valueOf(50000));
                exporter.setConfiguration((ReportExportConfiguration)configuration);
            } else if (displaytype.equals("xlsx")) {
                exporter = new JRXlsxExporter();
                configuration = new SimpleXlsxReportConfiguration();
                configuration.setDetectCellType(Boolean.valueOf(true));
                configuration.setRemoveEmptySpaceBetweenColumns(Boolean.valueOf(true));
                configuration.setMaxRowsPerSheet(Integer.valueOf(50000));
                exporter.setConfiguration((ReportExportConfiguration)configuration);
            } else if (displaytype.equals("rtf")) {
                exporter = new JRRtfExporter();
            } else if (displaytype.equals("pdf")) {
                exporter = new JRPdfExporter();
            } else if (displaytype.equals("doc") || displaytype.equals("docx")) {
                exporter = new JRDocxExporter();
            } else if (displaytype.equals("csv")) {
                exporter = new JRCsvExporter();
                configuration = new SimpleXlsReportConfiguration();
                configuration.setDetectCellType(Boolean.valueOf(true));
                configuration.setMaxRowsPerSheet(Integer.valueOf(50000));
                exporter.setConfiguration((ReportExportConfiguration)configuration);
            }
        }
        return exporter;
    }

    public static String getReportPath(String connectionid, String reportfile) throws SapphireException {
        String filePath = "";
        if (reportfile.startsWith("OOB/")) {
            filePath = REPORT_ROOT;
        } else {
            ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(connectionid);
            PropertyList filelocationPolicy = configurationProcessor.getPolicy("FileLocationPolicy", "ReportLookUp Custom");
            PropertyListCollection plc = filelocationPolicy.getCollectionNotNull("locations");
            for (int l = 0; l < plc.size(); ++l) {
                PropertyList location = plc.getPropertyList(l);
                String id = location.getProperty("id");
                if (!id.equals("reportlookup")) continue;
                filePath = FileUtil.substituteConfigurationPaths(location.getProperty("location"));
                break;
            }
        }
        return filePath;
    }

    public static void deleteFile(File file) {
        if (file != null && file.exists()) {
            try {
                FileUtil.deleteAll(file);
            }
            catch (Exception e) {
                Trace.logError("Failed to remove file.", e);
            }
        }
    }

    static {
        try {
            REPORT_ROOT = Configuration.getInstance().getApplicationHome() + "/reports";
            jasperReportsContext = DefaultJasperReportsContext.getInstance();
            jrPropertiesUtil = JRPropertiesUtil.getInstance((JasperReportsContext)jasperReportsContext);
            jrPropertiesUtil.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

