/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.Cookie
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.datachart.servlet;

import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.ExcelExportHelper;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.ExportDataConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DataExportStreamer
extends BaseRequest {
    private static final int BUFFER_SIZE = 1024;
    private static final int COOKIE_AGE = 600;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        File tempFile = null;
        InputStream in = null;
        ServletOutputStream servletOutputStream = null;
        try {
            String chartId = request.getParameter("chartid");
            String cookieName = request.getParameter("cookiename");
            String cookieValue = request.getParameter("cookievalue");
            cookieName = URLEncoder.encode(cookieName, "UTF-8");
            cookieValue = URLEncoder.encode(cookieValue, "UTF-8");
            ExportDataConfiguration.ExportType exportType = ExportDataConfiguration.ExportType.fromString(request.getParameter("exporttype"));
            String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
            CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
            if (cachedDataChart == null) {
                throw new IllegalArgumentException("Cache does not contain attribute " + chartId);
            }
            Data data = cachedDataChart.getData();
            ChartConfiguration chartConf = cachedDataChart.getChart().getChartConfiguration();
            ChartBindingMap chartBindingMap = new ChartBindingMap(chartId, data, cachedDataChart.getArgumentBar().getArgumentValueList(cachedDataChart.getRequestParams(), cachedDataChart.getRequestId()), this.getConnectionId());
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0L);
            if (exportType != ExportDataConfiguration.ExportType.EXCEL) {
                throw new IllegalArgumentException("Unknown export type: " + (Object)((Object)exportType));
            }
            ExcelExportHelper excelExportHelper = new ExcelExportHelper(chartConf.getDataExportConf().getExcelExportConfiguration(), chartBindingMap, this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
            tempFile = excelExportHelper.buildWorkbook(data);
            String fileName = tempFile.getName();
            if (fileName.toLowerCase().endsWith(".xls")) {
                response.setContentType("application/vnd.ms-excel");
            } else if (fileName.toLowerCase().endsWith(".xlsx")) {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            } else {
                response.setContentType("application/octet-stream");
            }
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);
            long length = tempFile.length();
            response.setContentLength((int)length);
            byte[] buf = new byte[1024];
            in = new BufferedInputStream(new FileInputStream(tempFile));
            servletOutputStream = response.getOutputStream();
            while ((length = (long)in.read(buf)) != -1L) {
                servletOutputStream.write(buf, 0, (int)length);
            }
            if (!cookieName.isEmpty() && !cookieValue.isEmpty()) {
                Cookie cookie = new Cookie(cookieName, cookieValue);
                cookie.setMaxAge(600);
                response.addCookie(cookie);
            }
        }
        catch (Exception ex) {
            this.logger.error("Error while streaming export file", ex);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (servletOutputStream != null) {
                    servletOutputStream.close();
                }
                if (tempFile != null) {
                    tempFile.delete();
                }
            }
            catch (IOException iOException) {}
        }
    }
}

