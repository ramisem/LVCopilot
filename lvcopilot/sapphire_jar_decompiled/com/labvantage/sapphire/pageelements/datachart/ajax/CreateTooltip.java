/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.datachart.ajax;

import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class CreateTooltip
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext sc) throws ServletException {
        PropertyList config;
        AjaxResponse ar = new AjaxResponse(request, response);
        String chartId = ar.getRequestParameter("chartid");
        String plotId = ar.getRequestParameter("plotid");
        String seriesGroupId = ar.getRequestParameter("seriesgroupid");
        String seriesId = ar.getRequestParameter("seriesid");
        String item = ar.getRequestParameter("item");
        String toolTipConfig = ar.getRequestParameter("tooltipconfig");
        this.validateInputParams(chartId, seriesGroupId, seriesId, item, toolTipConfig);
        try {
            config = new PropertyList(new JSONObject(URLDecoder.decode(toolTipConfig, "UTF-8")));
        }
        catch (UnsupportedEncodingException e1) {
            throw new ServletException("Unsupported encoding", (Throwable)e1);
        }
        catch (JSONException e2) {
            throw new ServletException("Cannot parse properties", (Throwable)e2);
        }
        String userCacheChartId = Util.getUserChartCacheId(chartId, this.getConnectionId());
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", userCacheChartId);
        if (cachedDataChart == null) {
            throw new IllegalArgumentException("Cache does not contain attribute " + userCacheChartId);
        }
        TraceableSeriesGroup traceableSeriesGroup = cachedDataChart.getData().getTraceableSeriesGroup(plotId, seriesGroupId);
        DataSet ds = traceableSeriesGroup.getSeriesDataSet(seriesId);
        Integer itemIndex = this.getItemIndex(item, cachedDataChart, ds, plotId, seriesGroupId);
        StringBuilder result = new StringBuilder();
        result.append("<table>");
        result.append(this.createHeader(config, plotId, seriesGroupId, seriesId, cachedDataChart, itemIndex));
        PropertyListCollection toolTipItems = config.getCollectionNotNull("tooltipitems");
        for (int i = 0; i < toolTipItems.size(); ++i) {
            PropertyList configItem = toolTipItems.getPropertyList(i);
            result.append(this.createTooltipRow(configItem, plotId, seriesGroupId, seriesId, cachedDataChart, ds, itemIndex));
        }
        result.append("</table>");
        ar.addCallbackArgument("result", result);
        ar.print();
    }

    private void validateInputParams(String chartId, String seriesGroupId, String seriesId, String item, String toolTipConfig) {
        if (chartId == null || chartId.isEmpty()) {
            throw new IllegalArgumentException("Chart ID is null or empty: " + chartId);
        }
        if (seriesGroupId == null || seriesGroupId.isEmpty()) {
            throw new IllegalArgumentException("Series group ID is null or empty: " + seriesGroupId);
        }
        if (seriesId == null || seriesId.isEmpty()) {
            throw new IllegalArgumentException("Series ID is null or empty: " + seriesId);
        }
        if (item == null || item.isEmpty()) {
            throw new IllegalArgumentException("Item is null or empty: " + item);
        }
        if (toolTipConfig == null) {
            throw new IllegalArgumentException("Tool tip configuration is null");
        }
    }

    private String createTooltipRow(PropertyList configItem, String plotId, String seriesGroupId, String seriesId, CachedDataChart cachedDataChart, DataSet ds, Integer itemIndex) {
        String title = configItem.getProperty("title", "");
        String columnId = configItem.getProperty("columnid", "");
        String numberFormat = configItem.getProperty("numberformat", "");
        String dateFormat = configItem.getProperty("dateformat", "");
        String timeFormat = configItem.getProperty("timeformat", "");
        String displayValue = configItem.getProperty("displayvalue", "");
        String pseudo = configItem.getProperty("pseudo", "");
        boolean translate = configItem.getProperty("translate", "").equals("Y");
        boolean enabled = configItem.getProperty("enabled", "Y").equals("Y");
        String tooltipRow = "";
        if (enabled) {
            int columnType;
            String value = !pseudo.equals("") ? this.evalPseudo(plotId, seriesGroupId, seriesId, cachedDataChart, itemIndex, pseudo) : ((columnType = ds.getColumnType(columnId)) == 1 ? this.getNumberValue(ds, columnId, itemIndex, numberFormat) : (columnType == 2 ? this.getDateValue(ds, columnId, itemIndex, dateFormat, timeFormat) : this.getStringValue(ds, columnId, itemIndex)));
            if (!displayValue.equals("")) {
                value = this.evalDisplayValue(value, displayValue);
            }
            if (translate) {
                value = this.getTranslationProcessor().translate(value);
            }
            title = this.getTranslationProcessor().translate(title);
            tooltipRow = "<tr><td style='font-weight:bold'>" + title + "</td><td>" + value + "</td></tr>";
        }
        return tooltipRow;
    }

    private String createHeader(PropertyList config, String plotId, String seriesGroupId, String seriesId, CachedDataChart cachedDataChart, Integer itemIndex) {
        String retVal = "";
        String header = config.getProperty("header", "");
        if (!header.equals("")) {
            header = this.evalPseudo(plotId, seriesGroupId, seriesId, cachedDataChart, itemIndex, header);
            retVal = "<tr><td colspan=2 style='text-align:center;background-color:darkgrey;color:white;font-weight:bold'>" + header + "</td></tr>";
        }
        return retVal;
    }

    private Integer getItemIndex(String item, CachedDataChart cachedDataChart, DataSet ds, String plotId, String seriesGroupId) {
        Integer itemIndex;
        try {
            itemIndex = Integer.parseInt(item);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Item is not a valid number; " + item, e);
        }
        return itemIndex;
    }

    private String evalPseudo(String plotId, String seriesGroupId, String seriesId, CachedDataChart cachedDataChart, int itemIndex, String pseudo) {
        String retValue;
        try {
            ChartBindingMap chartBindingMap = new ChartBindingMap(cachedDataChart.getChartId(), cachedDataChart.getData(), cachedDataChart.getArgumentBar().getArgumentValueList(cachedDataChart.getRequestParams(), cachedDataChart.getRequestId()), this.getConnectionId());
            chartBindingMap.setActiveItem(plotId, seriesGroupId, seriesId, itemIndex);
            retValue = new StringExpression(pseudo).evaluate(chartBindingMap);
        }
        catch (SapphireException e) {
            retValue = e.toString();
        }
        return retValue;
    }

    private String evalDisplayValue(String value, String displayValue) {
        String[] arrDisplayValue;
        String retVal = value;
        for (String valueRule : arrDisplayValue = displayValue.split(";")) {
            int index = valueRule.indexOf("=");
            if (index == -1) continue;
            String ruleValue = valueRule.substring(0, index);
            String textToDisplay = valueRule.substring(index + 1);
            if (!value.equals(ruleValue)) continue;
            retVal = textToDisplay;
        }
        return retVal;
    }

    private String getStringValue(DataSet ds, String columnId, Integer itemIndex) {
        return ds.getValue((int)itemIndex, columnId, "");
    }

    private String getDateValue(DataSet ds, String columnId, Integer itemIndex, String dateFormat, String timeFormat) {
        String value = "";
        M18NUtil m18NUtil = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()));
        int iDateFormat = this.parseDateFormat(dateFormat);
        int iTimeFormat = this.parseDateFormat(timeFormat);
        Locale locale = m18NUtil.getLocale();
        Calendar calendar = ds.getCalendar(itemIndex, columnId);
        if (calendar != null) {
            value = !dateFormat.equals("") && timeFormat.equals("") ? DateFormat.getDateInstance(iDateFormat, locale).format(calendar.getTime()) : (!dateFormat.equals("") && !timeFormat.equals("") ? DateFormat.getDateTimeInstance(iDateFormat, iTimeFormat, locale).format(calendar.getTime()) : m18NUtil.format(calendar));
        }
        return value;
    }

    private String getNumberValue(DataSet ds, String columnId, Integer itemIndex, String numberFormat) {
        String value = !numberFormat.equals("") ? new DecimalFormat(numberFormat).format(ds.getDouble(itemIndex, columnId)) : ds.getValue((int)itemIndex, columnId, "");
        return value;
    }

    private int parseDateFormat(String dateFormat) {
        switch (dateFormat) {
            case "long": {
                return 1;
            }
            case "medium": {
                return 2;
            }
        }
        return 3;
    }
}

