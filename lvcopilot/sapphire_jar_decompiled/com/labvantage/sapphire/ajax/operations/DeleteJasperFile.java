/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  net.sf.jasperreports.engine.JRBand
 *  net.sf.jasperreports.engine.JRChild
 *  net.sf.jasperreports.engine.JRExpressionChunk
 *  net.sf.jasperreports.engine.base.JRBaseSubreport
 *  net.sf.jasperreports.engine.design.JRDesignSubreport
 *  net.sf.jasperreports.engine.design.JasperDesign
 *  net.sf.jasperreports.engine.xml.JRXmlLoader
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.report.jasper.SapphireJasperUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRExpressionChunk;
import net.sf.jasperreports.engine.base.JRBaseSubreport;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class DeleteJasperFile
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    static final String REPORT_EXTENSION = ".jasper";
    static final String AJAX_MESSAGE = "message";
    static final String REPORT_TYPE = "reportType";
    static final String REPORT_PATH = "reportPath";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        HashMap<String, String> deleteResponse = new HashMap<String, String>();
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        try {
            String selectedReports = ajaxResponse.getRequestParameter("selectedReports");
            String[] selectedReportsArray = StringUtil.split(selectedReports, "%3B");
            HashMap<String, Map<String, String>> reportInfo = new HashMap<String, Map<String, String>>();
            this.populateReportInfo(selectedReportsArray, reportInfo);
            Set<String> reportids = reportInfo.keySet();
            boolean differentThanJasperReport = this.isDifferentThanJasperReport(reportInfo, reportids);
            if (differentThanJasperReport) {
                deleteResponse.put("Error", "Select Only Jasper/Embedded Jasper Report");
                ajaxResponse.addCallbackArgument(AJAX_MESSAGE, deleteResponse);
            } else {
                for (String key : reportids) {
                    String reportPath;
                    Map innerMap = (Map)reportInfo.get(key);
                    if (((String)innerMap.get(REPORT_TYPE)).equalsIgnoreCase("K")) {
                        deleteResponse.put(key, this.deleteEmbeddedJasperReport(key, connectionInfo));
                        ajaxResponse.addCallbackArgument(AJAX_MESSAGE, deleteResponse);
                        continue;
                    }
                    String parentReport = reportPath.substring((reportPath = (String)innerMap.get(REPORT_PATH)).contains("/") ? reportPath.lastIndexOf(47) + 1 : reportPath.lastIndexOf(92) + 1, reportPath.length());
                    parentReport = parentReport.replace(".jrxml", "");
                    File reportFile = new File(reportPath);
                    if (!reportFile.isAbsolute()) {
                        reportFile = new File(SapphireJasperUtil.getReportPath(this.getConnectionid(), reportPath) + File.separator + reportPath);
                    }
                    if (!reportFile.exists() || !reportFile.canRead()) {
                        throw new SapphireException("File:" + reportFile.getAbsolutePath() + " does not exist or cannot be read.");
                    }
                    JasperDesign jasperDesign = JRXmlLoader.load((File)reportFile);
                    Set<String> reportName = this.getSubreportName(jasperDesign);
                    reportName.add(parentReport);
                    List<String> sortedReportName = reportName.stream().collect(Collectors.toList());
                    Collections.sort(sortedReportName, (o1, o2) -> o1.compareTo((String)o2));
                    deleteResponse.put(key, DeleteJasperFile.deleteFileByFileName(SapphireJasperUtil.getReportPath(this.getConnectionid(), reportPath) + File.separator + reportPath.substring(0, reportPath.contains("/") ? reportPath.lastIndexOf(47) : reportPath.lastIndexOf(92)), sortedReportName));
                    ajaxResponse.addCallbackArgument(AJAX_MESSAGE, deleteResponse);
                }
            }
        }
        catch (Exception e) {
            deleteResponse.put("Error", e.getMessage());
            ajaxResponse.addCallbackArgument(AJAX_MESSAGE, deleteResponse);
        }
        ajaxResponse.print();
    }

    private Set<String> getSubreportName(JasperDesign jasperDesign) {
        HashSet<String> reportName = new HashSet<String>();
        JRBand[] jrBands = jasperDesign.getAllBands();
        List elements = null;
        for (int i = 0; i < jrBands.length; ++i) {
            elements = jrBands[i].getChildren();
            block1: for (JRChild childElement : elements) {
                JRBaseSubreport subReportElement;
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
                    if (!expressionText.get(j).toString().contains(REPORT_EXTENSION) || !expressionText.get(j).toString().contains("+")) continue;
                    String expression = expressionText.get(j).toString().replaceAll("\\s", "").trim();
                    subReportid = expression.substring(expression.indexOf(43) + 2, expression.length() - 1).replace(REPORT_EXTENSION, "");
                    reportName.add(subReportid);
                    continue block1;
                }
            }
        }
        return reportName;
    }

    private String deleteEmbeddedJasperReport(String reportName, ConnectionInfo connectionInfo) throws SapphireException {
        return DeleteJasperFile.deleteAllFileinFolder(SapphireJasperUtil.getReportPath(this.getConnectionid(), reportName) + File.separator + connectionInfo.getDatabaseId() + File.separator + reportName);
    }

    private boolean isDifferentThanJasperReport(Map<String, Map<String, String>> reportInfo, Set<String> reportids) {
        boolean differentThanJasperReport = false;
        for (String key : reportids) {
            Map<String, String> innerMap = reportInfo.get(key);
            if (innerMap.get(REPORT_TYPE).equalsIgnoreCase("J") || innerMap.get(REPORT_TYPE).equalsIgnoreCase("K")) continue;
            differentThanJasperReport = true;
            break;
        }
        return differentThanJasperReport;
    }

    private void populateReportInfo(String[] selectedReportsArray, Map<String, Map<String, String>> reportInfo) {
        for (String selectedReport : selectedReportsArray) {
            String[] selectedReportArray = StringUtil.split(selectedReport, "|");
            String reportid = selectedReportArray[0];
            String reportversionid = selectedReportArray[1];
            QueryProcessor qp = this.getQueryProcessor();
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT DISTINCT reportid, librarydir, reporttypeflag FROM report WHERE reportid=" + safeSQL.addVar(reportid) + " and reportversionid=" + safeSQL.addVar(reportversionid);
            DataSet ds = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
            HashMap<String, String> reportMap = new HashMap<String, String>();
            reportMap.put(REPORT_PATH, ds.getString(0, "librarydir"));
            reportMap.put(REPORT_TYPE, ds.getString(0, "reporttypeflag"));
            reportInfo.put(reportid, reportMap);
        }
    }

    private static String deleteFileByFileName(String directoryName, List<String> reportNames) throws SapphireException {
        String message = "";
        try {
            File directory = new File(directoryName);
            if (directory.exists()) {
                for (String fname : reportNames) {
                    File[] files = directory.listFiles((dir, name) -> name.contains(fname) && name.toLowerCase().endsWith(REPORT_EXTENSION));
                    if (files != null && files.length > 0) {
                        for (File file : files) {
                            Files.deleteIfExists(file.toPath());
                        }
                        message = "Successfully deleted all compiled jasper report and its corresponding subreports";
                        continue;
                    }
                    message = "No Compiled Jasper Report and its corresponding subreports found";
                }
            } else {
                message = "Report directory " + directory + " not found";
            }
        }
        catch (IOException ioe) {
            throw new SapphireException("File not deleted: " + ioe);
        }
        return message;
    }

    private static String deleteAllFileinFolder(String directoryName) throws SapphireException {
        String message = "";
        File directory = new File(directoryName);
        if (directory.exists()) {
            if (DeleteJasperFile.deleteDirectory(directory)) {
                message = "Successfully deleted all compiled jasper report and its corresponding subreports";
            }
        } else {
            message = "No Compiled Jasper Report and its corresponding subreports found";
        }
        return message;
    }

    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                DeleteJasperFile.deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}

