/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletRequest
 *  org.jfree.chart.ChartRenderingInfo
 *  org.jfree.chart.entity.EntityCollection
 *  org.jfree.chart.entity.StandardEntityCollection
 */
package com.labvantage.sapphire.pageelements.datachart.element;

import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentBar;
import com.labvantage.sapphire.pageelements.datachart.chart.Chart;
import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.ArgumentBarConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.ChartConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ChartBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.pageelements.datachart.session.CachedDataChart;
import com.labvantage.sapphire.util.cache.CacheUtil;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javax.servlet.ServletRequest;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataChart
extends BaseElement {
    private String chartId = null;
    private String cacheChartId = null;
    private PropertyList requestParams = null;
    private DataSet dataSet;

    public void setRequestContext(RequestContext requestContext, String connectionId) {
        this.requestContext = requestContext;
        if (connectionId != null && connectionId.length() > 0) {
            this.setConnectionId(connectionId);
            if (this.getConnectionProcessor() != null) {
                this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId());
            }
        }
        this.browser = null;
    }

    public void setDataSet(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        try {
            this.init();
            String dataTrendingContainerElemId = this.chartId + "_Container";
            String dataChartJsObject = this.chartId + "_JS";
            html.append("<div class='dataChartContainer' id='").append(dataTrendingContainerElemId).append("'></div>");
            html.append("<script>");
            html.append("var ").append(dataChartJsObject).append(" = new TRE.DataChart('").append(dataChartJsObject).append("', '").append(this.chartId).append("', $('#").append(dataTrendingContainerElemId).append("'), '").append(this.getRequestParams().toJSONString(false)).append("');");
            html.append("$(window).ready(").append(dataChartJsObject).append(".doOnLoad);");
            html.append("</script>");
        }
        catch (Exception ex) {
            this.logger.error("Error while creating chart element", ex);
            html = new StringBuilder();
            html.append("Error while creating chart element: ").append("<br>");
            html.append(ex.getClass().getName()).append(": ").append(ex.getMessage()).append("<br>");
            for (StackTraceElement stackElem : ex.getStackTrace()) {
                html.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(stackElem.toString());
                html.append("<br>");
            }
            if (ex.getCause() != null) {
                html.append("Caused by: ").append("<br>");
                html.append(ex.getCause().getClass().getName()).append(": ").append(ex.getCause().getMessage()).append("<br>");
                for (StackTraceElement stackElem : ex.getCause().getStackTrace()) {
                    html.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(stackElem.toString());
                    html.append("<br>");
                }
            }
            CacheUtil.remove(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", this.getCacheChartId());
        }
        return html.toString();
    }

    public String getChartId() {
        if (this.chartId == null) {
            throw new IllegalStateException("Chart not created yet");
        }
        return this.chartId;
    }

    public String getCacheChartId() {
        if (this.cacheChartId == null) {
            throw new IllegalStateException("Chart not created yet");
        }
        return this.cacheChartId;
    }

    public PropertyList getRequestParams() {
        if (this.requestParams == null) {
            throw new IllegalStateException("Chart not created yet");
        }
        return this.requestParams;
    }

    public void init() throws SapphireException {
        ChartRenderingInfo chartRenderingInfo;
        ArgumentBar argumentBar;
        ArgumentBarConfiguration argumentBarConf;
        ChartConfiguration chartConf;
        DataConfiguration dataConf;
        int randomNum = ThreadLocalRandom.current().nextInt(0, 10000);
        String requestId = Long.toString(System.currentTimeMillis()) + "_" + Integer.toString(randomNum);
        this.requestParams = new PropertyList();
        if (this.pageContext != null) {
            this.requestParams = this.requestParamsToPropertyList(this.pageContext.getRequest());
        }
        this.chartId = this.createChartId(requestId);
        CachedDataChart cachedDataChart = (CachedDataChart)CacheUtil.get(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", this.getCacheChartId());
        PropertyList elements = this.requestContext.getPropertyList("elements");
        if (!(cachedDataChart == null || cachedDataChart.getArgumentBar().getConnectionId() != null && cachedDataChart.getArgumentBar().getConnectionId().equals(this.getConnectionId()))) {
            cachedDataChart = null;
        }
        if (cachedDataChart == null) {
            dataConf = new DataConfiguration(elements.getPropertyList("dataprovider"));
            chartConf = new ChartConfiguration(elements.getPropertyList("datachart"), this.connectionInfo);
            argumentBarConf = new ArgumentBarConfiguration(elements.getPropertyList("argumentbar"), this.connectionInfo, this.pageContext);
            argumentBar = new ArgumentBar(argumentBarConf, this.getRequestParams(), this.getConnectionId(), requestId);
            StandardEntityCollection entityCollection = new StandardEntityCollection();
            chartRenderingInfo = new ChartRenderingInfo((EntityCollection)entityCollection);
        } else {
            dataConf = cachedDataChart.getData().getDataConfiguration();
            chartConf = cachedDataChart.getChart().getChartConfiguration();
            argumentBar = cachedDataChart.getArgumentBar();
            argumentBarConf = argumentBar.getArgumentBarConfiguration();
            chartRenderingInfo = cachedDataChart.getChartRenderingInfo();
        }
        boolean hasClientSideArguments = argumentBarConf.hasClientSideArguments();
        PropertyList argumentValueList = argumentBar.getArgumentValueList(this.getRequestParams(), requestId);
        Data data = hasClientSideArguments ? new Data(dataConf, this.connectionInfo) : new Data(dataConf, new DataBindingMap(argumentValueList, this.getConnectionId()), this.dataSet, this.connectionInfo);
        Chart chart = new Chart(chartConf, data, new ChartBindingMap(this.chartId, data, argumentValueList, this.getConnectionId()), this.connectionInfo, elements);
        cachedDataChart = new CachedDataChart.Builder(this.chartId, this.element, chart, argumentBar, data, this.getRequestParams(), elements, requestId).chartRenderingInfo(chartRenderingInfo).build();
        CacheUtil.put(this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getDatabaseId(), "DataCharts", this.getCacheChartId(), cachedDataChart);
    }

    private String createChartId(String requestId) {
        String chartId;
        PropertyList pageData = this.requestContext.getPropertyList("pagedata");
        String pageId = pageData.getProperty("webpageid");
        String currentUser = this.connectionInfo.getSysuserId();
        String scope = pageData.getProperty("configurationscope");
        if (scope.equalsIgnoreCase("request")) {
            chartId = pageId + currentUser + requestId;
        } else if (scope.equalsIgnoreCase("session")) {
            chartId = pageId + currentUser;
        } else {
            StringBuilder customChartId = new StringBuilder();
            customChartId.append(pageId);
            customChartId.append(currentUser);
            PropertyListCollection scopeRequestParamCollection = pageData.getCollectionNotNull("requestparamcollection");
            for (int i = 0; i < scopeRequestParamCollection.size(); ++i) {
                PropertyList scopeRequestParamProps = scopeRequestParamCollection.getPropertyList(i);
                String scopeRequestParamId = scopeRequestParamProps.getProperty("requestparamid");
                customChartId.append(this.getRequestParams().getProperty(scopeRequestParamId, ""));
            }
            chartId = customChartId.toString();
        }
        chartId = chartId.replaceAll("[^A-Za-z0-9]", "");
        this.cacheChartId = chartId + ";" + this.connectionInfo.getConnectionId();
        return chartId;
    }

    private PropertyList requestParamsToPropertyList(ServletRequest request) {
        PropertyList returnProps = new PropertyList();
        if (request.getCharacterEncoding() == null) {
            try {
                request.setCharacterEncoding("UTF-8");
            }
            catch (UnsupportedEncodingException ex) {
                this.logger.error("Error while parsing arguments", ex);
            }
        }
        Map parameterMap = request.getParameterMap();
        Set parameterMapKeySet = parameterMap.keySet();
        for (String parameterMapKey : parameterMapKeySet) {
            String[] parameterMapValueArr = (String[])parameterMap.get(parameterMapKey);
            if (parameterMapValueArr == null || parameterMapValueArr.length <= 0) continue;
            try {
                returnProps.setProperty(parameterMapKey, new PropertyList(new JSONObject(parameterMapValueArr[0])));
            }
            catch (JSONException e) {
                returnProps.setProperty(parameterMapKey, parameterMapValueArr[0]);
            }
        }
        return returnProps;
    }
}

