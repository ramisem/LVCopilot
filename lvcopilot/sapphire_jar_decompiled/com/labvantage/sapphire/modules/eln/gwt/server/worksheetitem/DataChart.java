/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.DataChartHelper;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataChart
extends BaseWorksheetItem {
    private static final String DEFAULT_CHART_TYPE = "Line Chart";
    private static final String DEFAULT_LINE_CHART_STYLE = "2D Line";
    private static final String DEFAULT_BAR_CHART_STYLE = "2D Columns";
    private static final String DEFAULT_PIE_CHART = "2D Pie";
    private static final String DEFAULT_DATA_TYPE = "SQL";
    private static final String PLOT_ID_COLUMN = "plotid";
    private static final String CHART_TITLE_ID = "_charttitle";
    private static final String DEFAULT_PLOT_ID = "1";

    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        String sdcid = this.config.getProperty("sdcid");
        if (sdcid.length() > 0) {
            worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty("source"), sdcid);
            worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        }
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/datachart.js");
        worksheetItemIncludes.setJSObjectName("dataChart");
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getHTML();
    }

    private String getHTML() throws SapphireException {
        RequestContext requestContext;
        PropertyList elements;
        boolean webPageExists;
        String message;
        StringBuilder html = new StringBuilder();
        DataChartHelper dataChartHelper = new DataChartHelper(this.getConnectionProcessor().getConnectionid());
        String webPageId = null;
        try {
            webPageId = this.getWebPageId();
        }
        catch (SapphireException e) {
            message = "Error while defining web page: " + e.getMessage();
            this.logError(message, e);
            html.append(dataChartHelper.formatErrorMessage(message));
        }
        boolean bl = webPageExists = this.getQueryProcessor().getPreparedSqlDataSet("SELECT webpageid FROM webpage WHERE webpageid = ?", (Object[])new String[]{webPageId}).getRowCount() == 1;
        if (!webPageExists) {
            message = "Web page not found for given chart style: " + webPageId;
            this.logError(message);
            html.append(dataChartHelper.formatErrorMessage(message));
            webPageId = null;
        }
        if (webPageId != null && (elements = dataChartHelper.getWebPageConfiguration(webPageId, requestContext = new RequestContext(new PropertyList()))) != null) {
            this.applyConfiguration(elements);
            requestContext.setProperty("pagedata", elements.getPropertyListNotNull("pagedata"));
            requestContext.setProperty("elements", elements);
            com.labvantage.sapphire.pageelements.datachart.element.DataChart dataChart = new com.labvantage.sapphire.pageelements.datachart.element.DataChart();
            dataChart.setPageContext(null);
            dataChart.setRequestContext(requestContext, this.getConnectionProcessor().getConnectionid());
            dataChart.init();
            html.append(dataChartHelper.getChartImageTag(dataChart.getChartId(), this.config.getProperty("width", "400"), this.config.getProperty("height", "400"), false));
        }
        return html.toString();
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getHTML();
    }

    private void applyConfiguration(PropertyList elements) throws SapphireException {
        this.applyDataProviderConfiguration(elements);
        this.applyDataChartConfiguration(elements);
    }

    private void applyDataChartConfiguration(PropertyList elements) {
        PropertyList dataChartProps = elements.getPropertyListNotNull("datachart");
        String chartTitle = this.config.getProperty("charttitle");
        if (!chartTitle.isEmpty()) {
            dataChartProps.setProperty("charttitleid", CHART_TITLE_ID);
            PropertyListCollection titleCollection = dataChartProps.getCollection("titlecollection");
            PropertyList titleProps = new PropertyList();
            if (titleCollection == null) {
                titleCollection = new PropertyListCollection();
                dataChartProps.setProperty("titlecollection", titleCollection);
            }
            titleCollection.add(titleProps);
            titleProps.setProperty("titleid", CHART_TITLE_ID);
            titleProps.setProperty("titletype", "Text");
            PropertyList textTitleProps = new PropertyList();
            titleProps.setProperty("texttitleprops", textTitleProps);
            textTitleProps.setProperty("text", chartTitle);
        }
    }

    private void applyDataProviderConfiguration(PropertyList elements) throws SapphireException {
        String sdcId = this.config.getProperty("sdcid");
        if (sdcId.isEmpty()) {
            throw new SapphireException("SDC Id is empty");
        }
        PropertyList dataProps = this.config.getPropertyListNotNull("dataprops");
        DataType dataType = DataType.valueOf(dataProps.getProperty("datatype", DEFAULT_DATA_TYPE).toUpperCase().replaceAll(" ", ""));
        PropertyList dataProvider = elements.getPropertyListNotNull("dataprovider");
        PropertyList dataSetProps = new PropertyList();
        dataProvider.setProperty("datasetprops", dataSetProps);
        dataSetProps.setProperty("datasettype", "Standard");
        PropertyList standardDataSetProps = new PropertyList();
        dataSetProps.setProperty("standarddatasetprops", standardDataSetProps);
        PropertyList dataSetSeriesProps = new PropertyList();
        dataProvider.setProperty("datasetseriesprops", dataSetSeriesProps);
        dataSetSeriesProps.setProperty("plotidcolumn", PLOT_ID_COLUMN);
        dataSetSeriesProps.setProperty("seriesgroupidcolumn", PLOT_ID_COLUMN);
        dataSetSeriesProps.setProperty("seriesidcolumn", PLOT_ID_COLUMN);
        PropertyListCollection plotCollection = new PropertyListCollection();
        dataSetSeriesProps.setProperty("plotcollection", plotCollection);
        PropertyList plotProps = new PropertyList();
        plotCollection.add(plotProps);
        PropertyListCollection seriesGroupCollection = new PropertyListCollection();
        plotProps.setProperty("seriesgroupcollection", seriesGroupCollection);
        PropertyList seriesGroupProps = new PropertyList();
        seriesGroupCollection.add(seriesGroupProps);
        PropertyListCollection seriesGroupBuilderCollection = new PropertyListCollection();
        seriesGroupProps.setProperty("seriesgroupbuildercollection", seriesGroupBuilderCollection);
        PropertyList seriesGroupBuilderProps = new PropertyList();
        seriesGroupBuilderCollection.add(seriesGroupBuilderProps);
        seriesGroupBuilderProps.setProperty("seriesgrouptype", "Default Category Dataset");
        PropertyList defaultCategoryDataSetProps = new PropertyList();
        seriesGroupBuilderProps.setProperty("defaultcategorydatasetprops", defaultCategoryDataSetProps);
        defaultCategoryDataSetProps.setProperty("xcolumn", "keyid1");
        defaultCategoryDataSetProps.setProperty("ycolumn", "transformvalue");
        PropertyList dataSetProviderProps = dataProvider.getPropertyListNotNull("datasetadapterprops");
        if (dataType == DataType.SQL) {
            standardDataSetProps.setProperty("standarddatasettype", DEFAULT_DATA_TYPE);
            PropertyList sqlDataSetProps = new PropertyList();
            standardDataSetProps.setProperty("sqldatasetprops", sqlDataSetProps);
            String sql = dataProps.getPropertyListNotNull("sqlprops").getProperty("sql");
            if (!sql.contains(PLOT_ID_COLUMN)) {
                sql = sql.toLowerCase().replaceFirst("select ", "select '1' plotid, ");
            }
            sqlDataSetProps.setProperty("sql", sql);
        } else if (dataType == DataType.CONTROL) {
            standardDataSetProps.setProperty("standarddatasettype", "SDI List");
            PropertyList sdiListDataSetProps = new PropertyList();
            standardDataSetProps.setProperty("sdilistdatasetprops", sdiListDataSetProps);
            StringBuilder keyId1s = new StringBuilder();
            StringBuilder keyId2s = new StringBuilder();
            StringBuilder keyId3s = new StringBuilder();
            this.buildKeyLists(sdcId, keyId1s, keyId2s, keyId3s);
            if (keyId1s.length() > 0) {
                sdiListDataSetProps.setProperty("sdcid", sdcId);
                sdiListDataSetProps.setProperty("keyid1", keyId1s.substring(1));
                if (keyId2s.length() > 0) {
                    sdiListDataSetProps.setProperty("keyid2", keyId2s.substring(1));
                    if (keyId3s.length() > 0) {
                        sdiListDataSetProps.setProperty("keyid3", keyId3s.substring(1));
                    }
                }
                PropertyList columnConfigurationAdapterProps = this.addNewStandardAdapter(dataSetProviderProps, "columns", "Column Configuration", "columnconfigurationadapterprops");
                PropertyListCollection pseudoColumns = columnConfigurationAdapterProps.getCollectionNotNull("pseudocolumns");
                PropertyList pseudoColumnProps = new PropertyList();
                pseudoColumns.add(pseudoColumnProps);
                pseudoColumnProps.setProperty("columnid", PLOT_ID_COLUMN);
                pseudoColumnProps.setProperty("columntype", "Text");
                pseudoColumnProps.setProperty("expression", DEFAULT_PLOT_ID);
                this.addSDIDataItemAdepter(dataSetProviderProps, sdcId);
            }
        } else if (dataType.equals((Object)DataType.QUERY)) {
            String keyId1Param;
            PropertyList queryParamProps;
            standardDataSetProps.setProperty("standarddatasettype", "Query");
            PropertyList queryDataSetProps = new PropertyList();
            standardDataSetProps.setProperty("querydatasetprops", queryDataSetProps);
            PropertyList queryProps = dataProps.getPropertyListNotNull("queryprops");
            queryDataSetProps.setProperty("basedonid", sdcId);
            queryDataSetProps.setProperty("queryid", queryProps.getProperty("queryid"));
            PropertyListCollection queryParams = queryDataSetProps.getCollectionNotNull("queryparams");
            PropertyListCollection queryParamCollection = queryProps.getCollectionNotNull("queryparamcollection");
            for (int i = 0; i < queryParamCollection.size(); ++i) {
                PropertyList queryParam = queryParamCollection.getPropertyList(i);
                String propertyId = queryParam.getProperty("queryparam");
                String value = queryParam.getProperty("value");
                queryParamProps = new PropertyList();
                queryParamProps.setProperty("queryparam", propertyId);
                queryParamProps.setProperty("value", value);
                queryParams.add(queryParamProps);
            }
            StringBuilder keyId1s = new StringBuilder();
            StringBuilder keyId2s = new StringBuilder();
            StringBuilder keyId3s = new StringBuilder();
            this.buildKeyLists(sdcId, keyId1s, keyId2s, keyId3s);
            String sdcIdParam = queryProps.getProperty("sdcidparam");
            if (!sdcIdParam.isEmpty()) {
                queryParamProps = new PropertyList();
                queryParamProps.setProperty(sdcIdParam, sdcId);
                queryParams.add(queryParamProps);
            }
            if (!(keyId1Param = queryProps.getProperty("keyid1param")).isEmpty()) {
                PropertyList queryParam1Props = new PropertyList();
                queryParam1Props.setProperty(keyId1Param, keyId1s.toString());
                queryParams.add(queryParam1Props);
                String keyId2Param = queryProps.getProperty("keyid2param");
                if (!keyId2Param.isEmpty()) {
                    PropertyList queryParam2Props = new PropertyList();
                    queryParam2Props.setProperty(keyId2Param, keyId2s.toString());
                    queryParams.add(queryParam2Props);
                    String keyId3Param = queryProps.getProperty("keyid3param");
                    if (!keyId3Param.isEmpty()) {
                        PropertyList queryParam3Props = new PropertyList();
                        queryParam3Props.setProperty(keyId3Param, keyId3s.toString());
                        queryParams.add(queryParam3Props);
                    }
                }
            }
            this.addSDIDataItemAdepter(dataSetProviderProps, sdcId);
        } else {
            throw new SapphireException("Unknown data type: " + (Object)((Object)dataType));
        }
    }

    private void buildKeyLists(String sdcId, StringBuilder keyId1s, StringBuilder keyId2s, StringBuilder keyId3s) throws SapphireException {
        String sql = "SELECT sdcid, keyid1, keyid2, keyid3 FROM worksheetitemsdi WHERE worksheetitemsdi.worksheetitemid = ? AND worksheetitemsdi.worksheetitemversionid = ? AND worksheetitemsdi.sdcid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{this.getWorksheetItemId(), this.getWorksheetItemVersionId(), sdcId});
        for (int i = 0; i < ds.getRowCount(); ++i) {
            keyId1s.append(";").append(ds.getString(i, "keyid1", ""));
            String keyId2 = ds.getString(i, "keyid2", "");
            if (keyId2.isEmpty()) continue;
            keyId2s.append(";").append(keyId2);
            String keyId3 = ds.getString(i, "keyid3", "");
            if (keyId3.isEmpty()) continue;
            keyId3s.append(";").append(keyId3);
        }
    }

    private void addSDIDataItemAdepter(PropertyList dataSetProviderProps, String sdcId) {
        PropertyList sdiDataItemAdapterProps = this.addNewStandardAdapter(dataSetProviderProps, "addsdidataitems", "Add SDI Data Items", "sdidataitemadapterprops");
        sdiDataItemAdapterProps.setProperty("sdcid", sdcId);
        sdiDataItemAdapterProps.setProperty("includeallprimarycolumns", "Y");
        PropertyList filters = sdiDataItemAdapterProps.getPropertyListNotNull("filters");
        filters.setProperty("ignorecancelled", "Y");
        filters.setProperty("resultentered", "Y");
    }

    private PropertyList addNewStandardAdapter(PropertyList dataSetProviderProps, String adapterId, String standardAdapterType, String adapterPropertyListKey) {
        PropertyListCollection adapterCollection = dataSetProviderProps.getCollectionNotNull("adaptercollection");
        PropertyList adapterProps = new PropertyList();
        adapterCollection.add(adapterProps);
        adapterProps.setProperty("adaptertype", "Standard");
        adapterProps.setProperty("adapterid", adapterId);
        PropertyList standardAdapter1Props = adapterProps.getPropertyListNotNull("standardadapterprops");
        standardAdapter1Props.setProperty("standardadaptertype", standardAdapterType);
        return standardAdapter1Props.getPropertyListNotNull(adapterPropertyListKey);
    }

    private String getWebPageId() throws SapphireException {
        StringBuilder webPageId = new StringBuilder();
        String chartTypeString = this.config.getProperty("charttype", DEFAULT_CHART_TYPE).replaceAll(" ", "");
        ChartType chartType = ChartType.valueOf(chartTypeString.toUpperCase());
        String chartStyle = this.config.getProperty(chartTypeString.toLowerCase() + "style", this.getDefaultChartStyle(chartType)).replaceAll(" ", "");
        if (chartType != ChartType.CUSTOMCHART || chartStyle.isEmpty()) {
            if (chartType == ChartType.CUSTOMCHART) {
                chartType = ChartType.valueOf(DEFAULT_CHART_TYPE.replaceAll(" ", "").toUpperCase());
                chartStyle = this.getDefaultChartStyle(chartType);
            }
            webPageId.append("LV_");
            webPageId.append(chartTypeString);
            webPageId.append(chartStyle);
        } else {
            webPageId.append(chartStyle);
        }
        return webPageId.toString();
    }

    private String getDefaultChartStyle(ChartType chartType) throws SapphireException {
        String chartStyle;
        if (chartType == ChartType.LINECHART) {
            chartStyle = DEFAULT_LINE_CHART_STYLE;
        } else if (chartType == ChartType.BARCHART) {
            chartStyle = DEFAULT_BAR_CHART_STYLE;
        } else if (chartType == ChartType.PIECHART) {
            chartStyle = DEFAULT_PIE_CHART;
        } else if (chartType == ChartType.CUSTOMCHART) {
            chartStyle = "";
        } else {
            throw new SapphireException("Unknown chart type: " + (Object)((Object)chartType));
        }
        return chartStyle;
    }

    private static enum DataType {
        SQL,
        CONTROL,
        QUERY;

    }

    private static enum ChartType {
        LINECHART,
        BARCHART,
        PIECHART,
        CUSTOMCHART;

    }
}

