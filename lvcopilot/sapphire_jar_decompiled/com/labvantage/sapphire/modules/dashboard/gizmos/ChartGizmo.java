/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.util.DashboardChart;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.DataChartHelper;
import com.labvantage.sapphire.pageelements.datachart.element.DataChart;
import java.math.BigDecimal;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class ChartGizmo
extends sapphire.pageelements.BaseGizmo {
    public static final String CHARTTYPE_PROPERTY = "charttype";
    public static final String CHARTTITLE_PROPERTY = "charttitle";
    public static final String CHARTSUBTITLE_PROPERTY = "chartsubtitle";
    public static final String SHOWTITLE_PROPERTY = "showtitle";
    public static final String MONITORID_PROPERTY = "monitorid";
    public static final String MONITORSQL_PROPERTY = "monitorsql";
    public static final String MONITORALL_PROPERTY = "monitorall";
    public static final String DISPLAYAXISLABELS_PROPERTY = "displayaxislabels";
    public static final String XAXISLABEL_PROPERTY = "xaxislabel";
    public static final String YAXISLABEL_PROPERTY = "yaxislabel";
    public static final String SHOWLEGEND_PROPERTY = "showlegend";
    public static final String CHARTTYPE_PIECHART = "pie chart";
    public static final String CHARTTYPE_PIECHART3D = "pie chart 3d";
    public static final String CHARTTYPE_BARCHART = "bar chart";
    public static final String CHARTTYPE_SPARKLINE = "sparkline";
    public static final String CHARTTYPE_DATECHART = "date chart";
    public static final String CHARTTYPE_LINECHART = "line chart";
    public static final String CHARTTYPE_BARCHART3D = "bar chart 3d";
    public static final String CHARTTYPE_LINECHART3D = "line chart 3d";
    private String defaultImageSrc = "";
    private DataSet dataset;

    @Override
    public PropertyList getUserProperties() {
        PropertyList up = super.getUserProperties();
        up.setProperty(CHARTTITLE_PROPERTY, "Y");
        up.setProperty(CHARTSUBTITLE_PROPERTY, "Y");
        up.setProperty(SHOWTITLE_PROPERTY, "Y");
        up.setProperty(DISPLAYAXISLABELS_PROPERTY, "Y");
        up.setProperty(XAXISLABEL_PROPERTY, "Y");
        up.setProperty(YAXISLABEL_PROPERTY, "Y");
        up.setProperty(SHOWLEGEND_PROPERTY, "Y");
        return up;
    }

    @Override
    public boolean init() {
        this.setRefreshOnResize(true);
        this.evaluateProps(this.element, new String[]{CHARTTITLE_PROPERTY, CHARTSUBTITLE_PROPERTY, XAXISLABEL_PROPERTY, YAXISLABEL_PROPERTY}, BaseGizmo.I18NFormat.CLIENT);
        this.evaluateProps(this.element, new String[]{"data", "dataaggregate", MONITORSQL_PROPERTY}, BaseGizmo.I18NFormat.DATABASE);
        if (this.element.getProperty("viewurl").length() > 0 && this.element.getProperty("viewurl").contains("[")) {
            ArrayList<String> ignore = new ArrayList<String>();
            ignore.add("viewurl");
            PropertyList temp = new PropertyList();
            String url = this.element.getProperty("viewurl");
            url = StringUtil.replaceAll(url, "[measurecategory]", "||{{measurecategory}}||");
            url = StringUtil.replaceAll(url, "[measureseries]", "||{{measureseries}}||");
            temp.setProperty("viewurl", url);
            BaseGizmo.evaluateExpression(this.getGizmoDefId(), temp, BaseGizmo.I18NFormat.CLIENT, this.getParameters(), null, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), ignore);
            url = temp.getProperty("viewurl");
            url = StringUtil.replaceAll(url, "||{{measurecategory}}||", "[measurecategory]");
            url = StringUtil.replaceAll(url, "||{{measureseries}}||", "[measureseries]");
            this.element.setProperty("viewurl_copy", url);
        } else {
            this.element.setProperty("viewurl_copy", this.element.getProperty("viewurl"));
        }
        TranslationProcessor tp = new TranslationProcessor(this.getConnectionid());
        String ct = this.element.getProperty(CHARTTYPE_PROPERTY);
        this.defaultImageSrc = ct.equalsIgnoreCase(tp.translate(CHARTTYPE_PIECHART3D)) || ct.equalsIgnoreCase(tp.translate(CHARTTYPE_PIECHART)) ? "ChartPie" : (ct.equalsIgnoreCase(tp.translate(CHARTTYPE_BARCHART3D)) || ct.equalsIgnoreCase(tp.translate(CHARTTYPE_BARCHART)) ? "ChartColumn" : (ct.equalsIgnoreCase(tp.translate(CHARTTYPE_LINECHART3D)) || ct.equalsIgnoreCase(tp.translate(CHARTTYPE_LINECHART)) ? "ChartLine" : "ChartArea"));
        return true;
    }

    private void evaluateProps(PropertyList props, String[] names, BaseGizmo.I18NFormat i18NFormat) {
        for (String name : names) {
            if (!props.containsKey(name)) continue;
            if (props.get(name) instanceof String) {
                props.setProperty(name + "_copy", this.evaluateExpression(this.getTranslationProcessor().translate(props.getProperty(name)), i18NFormat));
                continue;
            }
            if (!(props.get(name) instanceof PropertyList)) continue;
            props.setProperty(name + "_copy", (PropertyList)props.getPropertyList(name).clone());
            this.evaluateExpression(props.getPropertyList(name + "_copy"), i18NFormat);
        }
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        if (this.element == null) {
            html.append("No element data found.");
        } else {
            try {
                return this.getDataChartHTML(this.element);
            }
            catch (SapphireException e) {
                html.append("Could not load chart. ").append(e.getMessage());
                this.logger.error("Chart exception: " + e.getMessage(), e);
            }
        }
        return html.toString();
    }

    private String getDataChartHTML(PropertyList element) throws SapphireException {
        StringBuilder html = new StringBuilder();
        DashboardChart dc = new DashboardChart();
        String connid = this.getConnectionId();
        dc.setSDCProcessor(new SDCProcessor(connid));
        dc.setQp(new QueryProcessor(connid));
        dc.setTranslationProcessor(new TranslationProcessor(connid));
        this.dataset = dc.getDataSet(element);
        int chartWidth = this.getWidth();
        int chartHeight = this.getHeight() - 17;
        if (OpalUtil.isNotEmpty(this.dataset)) {
            this.fixDataSet();
            String webPageId = element.getProperty("datachartpage", "LV_ChartGizmoPage");
            RequestContext requestContext = new RequestContext(new PropertyList());
            DataChartHelper dataChartHelper = new DataChartHelper(this.getConnectionProcessor().getConnectionid());
            PropertyList elements = dataChartHelper.getWebPageConfiguration(webPageId, requestContext);
            String charttype = element.getProperty(CHARTTYPE_PROPERTY).toLowerCase();
            if (element.getProperty("__charttype") != null && !element.getProperty("__charttype").equalsIgnoreCase(charttype)) {
                charttype = element.getProperty("__charttype").toLowerCase();
            }
            this.setDataproviderProps(charttype, element, elements);
            this.setDatachartProps(charttype, element, elements);
            this.setPlotProps(charttype, element, elements);
            requestContext.setProperty("pagedata", elements.getPropertyListNotNull("pagedata"));
            requestContext.setProperty("elements", elements);
            DataChart dataChart = new DataChart();
            dataChart.setDataSet(this.dataset);
            dataChart.setPageContext(null);
            dataChart.setRequestContext(requestContext, this.getConnectionProcessor().getConnectionid());
            dataChart.init();
            String chartId = dataChart.getChartId();
            String dataChartJsVar = chartId + "_JS";
            String dataTrendingContainerElemId = chartId + "_Container";
            String imageTag = dataChartHelper.getChartImageTag(dataChart.getChartId(), Integer.toString(chartWidth), Integer.toString(chartHeight), false);
            html.append("        <link rel=\"stylesheet\" href=\"WEB-CORE/extscripts/jquery/themes/smoothness/jquery-ui.css\" type=\"text/css\"/>\n").append("        <link rel=\"stylesheet\" href=\"WEB-CORE/elements/datachart/stylesheets/datachart.css\"      type=\"text/css\"/>\n").append("        <link rel=\"stylesheet\" href=\"WEB-CORE/elements/datachart/stylesheets/standardevent.css\"  type=\"text/css\"/>\n").append("        <link rel=\"stylesheet\" href=\"WEB-CORE/elements/datachart/stylesheets/zoomplot.css\"       type=\"text/css\"/>\n").append("        <script language=\"JavaScript\" src=\"WEB-CORE/elements/datachart/scripts/datachart.js\"></script>\n").append("        <script language=\"JavaScript\" src=\"WEB-CORE/elements/datachart/scripts/standardevent.js\"></script>\n").append("        <script language=\"JavaScript\" src=\"WEB-CORE/elements/datachart/scripts/zoomplot.js\"></script>\n").append("        <script language=\"JavaScript\" src=\"WEB-CORE/elements/datachart/scripts/standardoperation.js\"></script>\n");
            html.append("<script>");
            html.append("var ").append(dataChartJsVar).append(" = new TRE.DataChart('").append(dataChartJsVar).append("', '").append(chartId).append("', $('#").append(dataTrendingContainerElemId).append("'), '").append(new PropertyList().toJSONString(false)).append("');");
            html.append("</script>");
            html.append("<div class='dataChartContainer' id='").append(dataTrendingContainerElemId).append("'>");
            html.append(imageTag);
            html.append("</div>");
        }
        return html.toString();
    }

    private void fixDataSet() {
        int i;
        BigDecimal sum = new BigDecimal(0);
        for (i = 0; i < this.dataset.getRowCount(); ++i) {
            sum = sum.add(this.dataset.getBigDecimal(i, "measurevalue", new BigDecimal(0)));
        }
        if (!this.dataset.isValidColumn("measureseries")) {
            this.dataset.addColumn("measureseries", 0);
        }
        if (!this.dataset.isValidColumn("measurecategory")) {
            this.dataset.addColumn("measurecategory", 0);
        }
        if (!this.dataset.isValidColumn("measurepercentage")) {
            this.dataset.addColumn("measurepercentage", 1);
        }
        this.dataset.addColumn("rownumber", 1);
        for (i = 0; i < this.dataset.getRowCount(); ++i) {
            this.dataset.setString(i, "measurecategory", this.dataset.getString(i, "measurecategory", this.dataset.getValue(i, "measuredate", "-")));
            this.dataset.setString(i, "seriesid", this.dataset.getString(i, "measureseries", "Series 1"));
            this.dataset.setNumber(i, "measurepercentage", sum.compareTo(new BigDecimal(0)) > 0 ? this.dataset.getBigDecimal(i, "measurevalue", new BigDecimal(0)).multiply(new BigDecimal(100)).divide(sum, 0, 4) : new BigDecimal(0));
            this.dataset.setNumber(i, "rownumber", i);
        }
    }

    private void setPlotProps(String charttype, PropertyList element, PropertyList elements) {
        PropertyList categoryPlot = elements.getPropertyList("categoryplot");
        PropertyList piePlot = elements.getPropertyList("pieplot");
        PropertyList dateChart = elements.getPropertyList("datechart");
        PropertyList sparkLine = elements.getPropertyList(CHARTTYPE_SPARKLINE);
        boolean displayaxislabels = element.getProperty(DISPLAYAXISLABELS_PROPERTY).equalsIgnoreCase("Y");
        String labelpositiontype = element.getProperty("labelpositiontype", "");
        boolean autorangeyaxis = element.getProperty("autorangeyaxis", "Y").equals("Y");
        boolean verticalticklabels = element.getProperty("verticalticklabels", "Y").equals("Y");
        String ymin = element.getProperty("ymin", "");
        String ymax = element.getProperty("ymax", "");
        if (displayaxislabels) {
            categoryPlot.getCollection("domainaxiscollection").getPropertyList(0).setProperty("label", element.getProperty("xaxislabel_copy"));
            categoryPlot.getCollection("rangeaxiscollection").getPropertyList(0).setProperty("label", element.getProperty("yaxislabel_copy"));
            dateChart.getCollection("domainaxiscollection").getPropertyList(0).setProperty("label", element.getProperty("xaxislabel_copy"));
            dateChart.getCollection("rangeaxiscollection").getPropertyList(0).setProperty("label", element.getProperty("yaxislabel_copy"));
        } else {
            categoryPlot.getCollection("domainaxiscollection").getPropertyList(0).setProperty("label", "");
            categoryPlot.getCollection("rangeaxiscollection").getPropertyList(0).setProperty("label", "");
            dateChart.getCollection("domainaxiscollection").getPropertyList(0).setProperty("label", "");
            dateChart.getCollection("rangeaxiscollection").getPropertyList(0).setProperty("label", "");
        }
        if (!labelpositiontype.isEmpty()) {
            categoryPlot.getCollection("domainaxiscollection").getPropertyList(0).getPropertyList("categoryaxisprops").setProperty("labelpositiontype", labelpositiontype);
        }
        if (!(autorangeyaxis || ymin.isEmpty() || ymax.isEmpty())) {
            categoryPlot.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("autorangeyaxis", "N");
            categoryPlot.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("lowerrange", ymin);
            categoryPlot.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("upperrange", ymax);
            dateChart.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("autorangeyaxis", "N");
            dateChart.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("lowerrange", ymin);
            dateChart.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("upperrange", ymax);
            sparkLine.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("autorangeyaxis", "N");
            sparkLine.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("lowerrange", ymin);
            sparkLine.getCollection("rangeaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("upperrange", ymax);
        }
        if (verticalticklabels) {
            dateChart.getCollection("domainaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("verticalticklabels", "Y");
            sparkLine.getCollection("domainaxiscollection").getPropertyList(0).getPropertyList("valueaxisprops").setProperty("verticalticklabels", "Y");
        }
        switch (charttype) {
            case "bar chart": {
                categoryPlot.getCollection("renderercollection").getPropertyList(0).setProperty("renderertype", "Bar");
                break;
            }
            case "bar chart 3d": {
                categoryPlot.getCollection("renderercollection").getPropertyList(0).setProperty("renderertype", "Bar (3D)");
                break;
            }
            case "pie chart": {
                piePlot.setProperty("use3d", "N");
                break;
            }
            case "pie chart 3d": {
                piePlot.setProperty("use3d", "Y");
                break;
            }
            case "line chart": {
                categoryPlot.getCollection("renderercollection").getPropertyList(0).setProperty("renderertype", "Line and Shape");
                break;
            }
            case "sparkline": {
                break;
            }
            default: {
                categoryPlot.getCollection("renderercollection").getPropertyList(0).setProperty("renderertype", "Bar");
            }
        }
    }

    private void setDatachartProps(String charttype, PropertyList element, PropertyList elements) {
        PropertyList datachart = elements.getPropertyList("datachart");
        PropertyList plotProps = datachart.getPropertyList("plotprops");
        PropertyList titleProps = datachart.getCollection("titlecollection").getPropertyList(1);
        PropertyList subtitleProps = datachart.getCollection("titlecollection").getPropertyList(2);
        PropertyList tooltipProps = plotProps.getCollection("urlgeneratorcollection").getPropertyList(1).getCollection("eventcollection").getPropertyList(0);
        PropertyList pietooltipProps = plotProps.getCollection("urlgeneratorcollection").getPropertyList(2).getCollection("eventcollection").getPropertyList(0);
        PropertyList gotolistProps = plotProps.getCollection("urlgeneratorcollection").getPropertyList(1).getCollection("eventcollection").getPropertyList(1);
        PropertyList piegotolistProps = plotProps.getCollection("urlgeneratorcollection").getPropertyList(2).getCollection("eventcollection").getPropertyList(1);
        PropertyList paintsConfig = plotProps.getPropertyList("drawingsupplierprops").getPropertyList("paintsconfig");
        String chartTitle = element.getProperty("charttitle_copy", "");
        String chartSubTitle = element.getProperty("chartsubtitle_copy", "");
        String viewUrl = element.getProperty("viewurl_copy", "");
        String viewtarget = element.getProperty("viewtarget", "");
        String paintconftype = element.getProperty("paintconftype", "");
        String colorscheme = element.getProperty("colorscheme", "");
        String singlecolor = element.getProperty("singlecolor", "");
        String tooltip = element.getProperty("tooltip", "");
        boolean showlegend = element.getProperty(SHOWLEGEND_PROPERTY).equalsIgnoreCase("Y");
        boolean showtitle = element.getProperty(SHOWTITLE_PROPERTY).equalsIgnoreCase("Y");
        switch (charttype) {
            case "bar chart": {
                plotProps.setProperty("elementid", "categoryplot");
                break;
            }
            case "bar chart 3d": {
                plotProps.setProperty("elementid", "categoryplot");
                break;
            }
            case "pie chart": {
                plotProps.setProperty("elementid", "pieplot");
                break;
            }
            case "pie chart 3d": {
                plotProps.setProperty("elementid", "pieplot");
                break;
            }
            case "line chart": {
                plotProps.setProperty("elementid", "categoryplot");
                break;
            }
            case "sparkline": {
                plotProps.setProperty("elementid", CHARTTYPE_SPARKLINE);
                break;
            }
            case "date chart": {
                plotProps.setProperty("elementid", "datechart");
                break;
            }
            default: {
                plotProps.setProperty("elementid", "categoryplot");
            }
        }
        if (!viewUrl.isEmpty()) {
            gotolistProps.setProperty("enable", "Y");
            gotolistProps.getPropertyList("standardeventprops").getPropertyList("openurleventprops").setProperty("url", viewUrl);
            gotolistProps.getPropertyList("standardeventprops").getPropertyList("openurleventprops").setProperty("target", viewtarget);
            piegotolistProps.setProperty("enable", "Y");
            piegotolistProps.getPropertyList("standardeventprops").getPropertyList("openurleventprops").setProperty("url", viewUrl);
            piegotolistProps.getPropertyList("standardeventprops").getPropertyList("openurleventprops").setProperty("target", viewtarget);
        }
        if (!tooltip.isEmpty()) {
            tooltipProps.getPropertyList("standardeventprops").getPropertyList("tooltipeventprops").getCollection("tooltipitems").getPropertyList(0).setProperty("pseudo", tooltip);
            pietooltipProps.getPropertyList("standardeventprops").getPropertyList("tooltipeventprops").getCollection("tooltipitems").getPropertyList(0).setProperty("pseudo", tooltip);
        }
        if (showtitle && !chartTitle.isEmpty()) {
            titleProps.setProperty("visible", "Y");
            titleProps.getPropertyList("texttitleprops").setProperty("text", chartTitle);
        }
        if (showtitle && !chartSubTitle.isEmpty()) {
            subtitleProps.setProperty("visible", "Y");
            subtitleProps.getPropertyList("texttitleprops").setProperty("text", chartSubTitle);
        }
        if (paintconftype.equals("Color Scheme") && !colorscheme.isEmpty()) {
            paintsConfig.setProperty("paintconftype", "Color Scheme");
            paintsConfig.setProperty("colorscheme", colorscheme);
        } else if (paintconftype.equals("Standard") && !singlecolor.isEmpty()) {
            paintsConfig.setProperty("paintconftype", "Standard");
            paintsConfig.getCollection("paintcollection").getPropertyList(0).setProperty("painttype", "Color");
            paintsConfig.getCollection("paintcollection").getPropertyList(0).getPropertyList("colorprops").setProperty("color", singlecolor);
        }
        if (showlegend) {
            datachart.setProperty(SHOWLEGEND_PROPERTY, "Y");
        }
    }

    private void setDataproviderProps(String charttype, PropertyList element, PropertyList elements) {
        String sql = element.getProperty(MONITORSQL_PROPERTY);
        PropertyList dataprovider = elements.getPropertyList("dataprovider");
        elements.getPropertyList("dataprovider").getPropertyList("datasetprops").getPropertyList("standarddatasetprops").getPropertyList("sqldatasetprops").setProperty("sql", sql);
        switch (charttype) {
            case "bar chart": {
                dataprovider.setProperty("seriesgroupbuilderid", "categoryplot");
                break;
            }
            case "bar chart 3d": {
                dataprovider.setProperty("seriesgroupbuilderid", "categoryplot");
                break;
            }
            case "pie chart": {
                dataprovider.setProperty("seriesgroupbuilderid", "pieplot");
                break;
            }
            case "pie chart 3d": {
                dataprovider.setProperty("seriesgroupbuilderid", "pieplot");
                break;
            }
            case "line chart": {
                dataprovider.setProperty("seriesgroupbuilderid", "categoryplot");
                break;
            }
            case "sparkline": {
                dataprovider.setProperty("seriesgroupbuilderid", CHARTTYPE_SPARKLINE);
                break;
            }
            case "date chart": {
                dataprovider.setProperty("seriesgroupbuilderid", "datechart");
                break;
            }
            default: {
                dataprovider.setProperty("seriesgroupbuilderid", "categoryplot");
            }
        }
    }

    @Override
    public String getScript() {
        return "function dispose" + this.elementid + "(){var oD = document;if(oD.getElementById('buf_" + this.elementid + "chartimg')!=null){var oE = oD.getElementById('" + this.elementid + "chartimg');if(oE!=null)oE.usemap=null;}};dispose" + this.elementid + "();";
    }

    @Override
    public String getTitle() {
        return this.element.getProperty(CHARTTITLE_PROPERTY, "Chart Gizmo");
    }

    @Override
    public String getIcon() {
        return this.getImage(this.element.getProperty(CHARTTITLE_PROPERTY, "Chart Gizmo"), this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return this.defaultImageSrc;
    }
}

