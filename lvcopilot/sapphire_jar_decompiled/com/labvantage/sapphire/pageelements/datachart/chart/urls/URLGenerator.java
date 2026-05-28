/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jfree.chart.urls.CategoryURLGenerator
 *  org.jfree.chart.urls.PieURLGenerator
 *  org.jfree.chart.urls.XYURLGenerator
 *  org.jfree.data.category.CategoryDataset
 *  org.jfree.data.general.Dataset
 *  org.jfree.data.general.PieDataset
 *  org.jfree.data.xy.XYDataset
 */
package com.labvantage.sapphire.pageelements.datachart.chart.urls;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.TraceableSeriesGroup;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.DOMEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.EventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.ExpressionToolTipEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.OpenMenuEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.OpenURLEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.StandardEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.ToolTipEventConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.plot.urlgenerator.URLGeneratorConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import sapphire.SapphireException;
import sapphire.util.DataSet;

public class URLGenerator
extends BaseCustom
implements XYURLGenerator,
CategoryURLGenerator,
PieURLGenerator,
Serializable {
    private final URLGeneratorConfiguration urlGeneratorConf;
    private final Data data;
    private final ChartBindingMap chartBindingMap;

    public URLGenerator(URLGeneratorConfiguration urlGeneratorConf, Data data, String connectionId, ChartBindingMap chartBindingMap) {
        if (urlGeneratorConf == null) {
            throw new IllegalArgumentException("Source configuration is null");
        }
        if (data == null) {
            throw new IllegalArgumentException("Data is null");
        }
        if (chartBindingMap == null) {
            throw new IllegalArgumentException("Chart binding map is null");
        }
        this.urlGeneratorConf = urlGeneratorConf;
        this.data = data;
        this.chartBindingMap = chartBindingMap;
        this.setConnectionId(connectionId);
    }

    public String generateURL(String plotId, String seriesGroupId, String seriesId, String item) {
        HashMap<String, StringBuilder> domEventMap = new HashMap<String, StringBuilder>();
        this.chartBindingMap.setActiveItem(plotId, seriesGroupId, seriesId, Integer.parseInt(item));
        for (EventConfiguration eventConf : this.urlGeneratorConf.getEventConfigurationList()) {
            try {
                if (!eventConf.isEnabled().evaluate(this.chartBindingMap).booleanValue()) continue;
                if (eventConf.getEventType() == EventConfiguration.EventType.DOM_EVENT) {
                    this.generateDOMEventURL(eventConf.getDOMEventConfiguration(), domEventMap);
                    continue;
                }
                if (eventConf.getEventType() == EventConfiguration.EventType.STANDARD_EVENT) {
                    this.generateStandardEventURL(eventConf.getStandardEventConfiguration(), domEventMap, plotId, seriesGroupId, seriesId, item);
                    continue;
                }
                throw new IllegalArgumentException("Unknown event type: " + (Object)((Object)eventConf.getEventType()));
            }
            catch (SapphireException e) {
                this.logger.error("Error while creating event: " + eventConf.getEventId());
            }
        }
        return this.createURL(domEventMap);
    }

    private String createURL(Map<String, StringBuilder> domEventMap) {
        StringBuilder url = new StringBuilder();
        for (String domEvent : domEventMap.keySet()) {
            url.append(" ").append(domEvent).append("=\"");
            url.append(HttpUtil.htmlEncode(domEventMap.get(domEvent).toString()));
            url.append("\"");
        }
        return url.toString();
    }

    private void generateStandardEventURL(StandardEventConfiguration standardEventConf, Map<String, StringBuilder> domEventMap, String plotId, String seriesGroupId, String seriesId, String item) {
        if (standardEventConf.getStandardEventType() == StandardEventConfiguration.StandardEventType.EXPRESSION_TOOL_TIP) {
            String expression;
            ExpressionToolTipEventConfiguration expressionToolTipEventConf = standardEventConf.getExpressionToolTipEventConfiguration();
            try {
                expression = URLEncoder.encode(expressionToolTipEventConf.getToolTip().getExpression(), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                return;
            }
            StringBuilder onMouseOver = this.getDOMEvent(domEventMap, "onmouseover");
            onMouseOver.append(this.chartBindingMap.getDataChartJsObject()).append(".standardEvent.expressionTip('").append(plotId).append("', '").append(seriesGroupId).append("', '").append(seriesId).append("', '").append(item).append("', '").append(expression).append("');");
            StringBuilder onMouseOut = this.getDOMEvent(domEventMap, "onmouseout");
            onMouseOut.append(this.chartBindingMap.getDataChartJsObject()).append(".standardEvent.unTip();");
            StringBuilder onMouseDown = this.getDOMEvent(domEventMap, "onmousedown");
            onMouseDown.append(this.chartBindingMap.getDataChartJsObject()).append(".standardEvent.unTip();");
        } else if (standardEventConf.getStandardEventType() == StandardEventConfiguration.StandardEventType.TOOL_TIP) {
            String toolTipProperties;
            ToolTipEventConfiguration toolTipEventConf = standardEventConf.getToolTipEventConfiguration();
            try {
                toolTipProperties = URLEncoder.encode(toolTipEventConf.toJSONString(), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                return;
            }
            StringBuilder onMouseOver = this.getDOMEvent(domEventMap, "onmouseover");
            onMouseOver.append(this.chartBindingMap.getDataChartJsObject()).append(".standardEvent.tip('").append(plotId).append("', '").append(seriesGroupId).append("', '").append(seriesId).append("', '").append(item).append("', '").append(toolTipProperties).append("');");
            StringBuilder onMouseOut = this.getDOMEvent(domEventMap, "onmouseout");
            onMouseOut.append(this.chartBindingMap.getDataChartJsObject()).append(".standardEvent.unTip();");
            StringBuilder onMouseDown = this.getDOMEvent(domEventMap, "onmousedown");
            onMouseDown.append(this.chartBindingMap.getDataChartJsObject()).append(".standardEvent.unTip();");
        } else if (standardEventConf.getStandardEventType() == StandardEventConfiguration.StandardEventType.OPEN_URL) {
            String expression;
            OpenURLEventConfiguration openURLEventConf = standardEventConf.getOpenURLEventConfiguration();
            String target = openURLEventConf.getTarget();
            try {
                expression = URLEncoder.encode(openURLEventConf.getURL().getExpression(), "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                return;
            }
            StringBuilder onMouseClick = this.getDOMEvent(domEventMap, "onclick");
            onMouseClick.append(this.chartBindingMap.getDataChartJsObject()).append(".standardEvent.openURL('").append(plotId).append("', '").append(seriesGroupId).append("', '").append(seriesId).append("', '").append(item).append("', '").append(expression).append("', '").append(target).append("');");
        } else if (standardEventConf.getStandardEventType() == StandardEventConfiguration.StandardEventType.OPEN_MENU) {
            OpenMenuEventConfiguration openMenuEventConf = standardEventConf.getOpenMenuEventConfiguration();
            String urlGeneratorId = openMenuEventConf.getParent().getParent().getParent().getUrlGeneratorId();
            String eventId = openMenuEventConf.getParent().getParent().getEventId();
            StringBuilder onMouseDown = this.getDOMEvent(domEventMap, "onmousedown");
            onMouseDown.append(this.chartBindingMap.getDataChartJsObject()).append(".standardEvent.openMenu('").append(urlGeneratorId).append("', '").append(eventId).append("', '").append(plotId).append("', '").append(seriesGroupId).append("', '").append(seriesId).append("', '").append(item).append("');");
        } else {
            throw new IllegalArgumentException("Unknown standard event: " + (Object)((Object)standardEventConf.getStandardEventType()));
        }
    }

    private StringBuilder getDOMEvent(Map<String, StringBuilder> domEventMap, String eventName) {
        StringBuilder event = domEventMap.get(eventName.toLowerCase());
        if (event == null) {
            event = new StringBuilder();
            domEventMap.put(eventName.toLowerCase(), event);
        }
        event.append(" ");
        return event;
    }

    private void generateDOMEventURL(DOMEventConfiguration domEventConf, Map<String, StringBuilder> domEventMap) {
        String javaScript = null;
        try {
            javaScript = domEventConf.getJavaScript().evaluate(this.chartBindingMap);
        }
        catch (SapphireException e) {
            this.logger.error("Error while evaluating JavaScript: " + domEventConf.getJavaScript().getExpression());
        }
        if (javaScript != null) {
            if (domEventConf.getDOMEventType() == DOMEventConfiguration.DOMEventType.ON_CLICK) {
                StringBuilder onClick = this.getDOMEvent(domEventMap, "onclick");
                onClick.append(javaScript).append(";");
            } else if (domEventConf.getDOMEventType() == DOMEventConfiguration.DOMEventType.MOUSE_OVER) {
                StringBuilder onMouseOver = this.getDOMEvent(domEventMap, "onmouseover");
                onMouseOver.append(javaScript).append(";");
            } else if (domEventConf.getDOMEventType() == DOMEventConfiguration.DOMEventType.MOUSE_OUT) {
                StringBuilder onMouseOut = this.getDOMEvent(domEventMap, "onmouseout");
                onMouseOut.append(javaScript).append(";");
            } else if (domEventConf.getDOMEventType() == DOMEventConfiguration.DOMEventType.MOUSE_UP) {
                StringBuilder onMouseUp = this.getDOMEvent(domEventMap, "onmouseup");
                onMouseUp.append(javaScript).append(";");
            } else {
                throw new IllegalArgumentException("Unknown DOM event: " + (Object)((Object)domEventConf.getDOMEventType()));
            }
        }
    }

    public String generateURL(XYDataset dataset, int series, int item) {
        if (!this.data.hasTraceableSeriesGroup((Dataset)dataset)) {
            return "";
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup((Dataset)dataset);
        String plotId = traceableSeriesGroup.getPlotId();
        String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
        String seriesId = traceableSeriesGroup.getSeriesId(series);
        return this.generateURL(plotId, seriesGroupId, seriesId, Integer.toString(item));
    }

    public String generateURL(CategoryDataset dataset, int series, int category) {
        if (!this.data.hasTraceableSeriesGroup((Dataset)dataset)) {
            return "";
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup((Dataset)dataset);
        String plotId = traceableSeriesGroup.getPlotId();
        String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
        String seriesId = traceableSeriesGroup.getSeriesId(series);
        String columnId = traceableSeriesGroup.getSeriesGroupColumnConfiguration().getDomainValueColumn();
        DataSet ds = traceableSeriesGroup.getSeriesDataSet(seriesId);
        Comparable categoryKey = dataset.getColumnKey(category);
        HashMap<String, String> findMap = new HashMap<String, String>();
        findMap.put(columnId, categoryKey.toString());
        int itemIndex = ds.findRow(findMap);
        return this.generateURL(plotId, seriesGroupId, seriesId, Integer.toString(itemIndex));
    }

    public String generateURL(PieDataset dataset, Comparable key, int pieIndex) {
        if (!this.data.hasTraceableSeriesGroup((Dataset)dataset)) {
            return "";
        }
        TraceableSeriesGroup traceableSeriesGroup = this.data.getTraceableSeriesGroup((Dataset)dataset);
        String plotId = traceableSeriesGroup.getPlotId();
        String seriesGroupId = traceableSeriesGroup.getSeriesGroupId();
        String seriesId = traceableSeriesGroup.getSeriesId(0);
        int itemIndex = dataset.getKeys().lastIndexOf(key);
        return this.generateURL(plotId, seriesGroupId, seriesId, String.valueOf(itemIndex));
    }
}

