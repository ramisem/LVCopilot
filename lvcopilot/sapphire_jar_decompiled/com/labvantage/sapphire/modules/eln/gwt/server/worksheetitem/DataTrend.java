/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.DataChartHelper;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.pageelements.datachart.element.DataChart;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import sapphire.SapphireException;
import sapphire.ext.BaseWorksheetItem;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataTrend
extends BaseWorksheetItem {
    private static final String TREND_OVER_SDC_PROP = "trendoversdc";
    private static final String PARAM_LIST_ID_PROP = "paramlistid";
    private static final String SOURCE_SDC_PROP = "sourcesdc";
    private static final String RANGE_MODE_PROP = "rangemode";
    private static final String ORDER_BY_COLUMN_PROP = "orderbycolumn";
    private static final String DATA_SOURCE_PROP = "source";
    private static final Map<String, String> PROPERTY_DISPLAY_NAME_MAP = new HashMap<String, String>();
    private static final String DAYS_BACK_PROP = "daysback";
    private static final String DAYS_FORWARD_PROP = "daysforward";
    private static final String MATCHING_COLUMNS_COLLECTION = "matchingcolumns";
    private static final String PARAM_LIST_VERSION_ID_PROP = "paramlistversionid";
    private static final String VARIANT_ID_PROP = "variantid";
    private static final String PARAM_ID_PROP = "paramid";
    private static final String PARAM_TYPE_PROP = "paramtype";
    private static final String WORK_ITEM_ID_PROP = "workitemid";
    private static final String WORK_ITEM_VERSION_ID_PROP = "workitemversionid";
    private static final String WORK_ITEM_INSTANCE_PROP = "workiteminstance";
    private static final String IGNORE_CANCELLED_PROP = "ignorecancelled";
    private static final String RESULT_ENTERED_PROP = "resultentered";
    private static final String RELEASED_ONLY_PROP = "releasedonly";
    private static final String INCLUDE_OUTLIERS_PROP = "includeoutliers";
    private static final String IGNORE_RETESTED_PROP = "ignoreretested";
    private static final String IGNORE_REMEASURED_PROP = "ignoreremeasured";
    private static final String BACK_COUNT_PROP = "backcount";
    private static final String WIDTH_PROP = "width";
    private static final String HEIGHT_PROP = "height";
    private static final String TITLE_PROP = "title";
    private static final String DATA_SOURCE_CONTROL = "Control";
    private static final String DATE_RANGE_MODE = "date";
    private static final String COUNT_RANGE_MODE = "count";
    private static final String SDI_LIST_RANGE_MODE = "sdilist";
    private static final String COUNT_COLUMN = "row";
    private static final String CHART_TITLE_ID = "charttitle";
    private static final int HTML_VIEW = 0;
    private static final int HTML_EXPORT = 1;
    private static final int HTML_COMPLETE = 2;
    private static final int HTML_EDITOR = 3;

    @Override
    public void setupOptions(WorksheetItemOptions worksheetItemOptions) {
        worksheetItemOptions.setViewOnly(true);
        String sourcesdc = this.config.getProperty(SOURCE_SDC_PROP);
        if (sourcesdc.length() > 0) {
            worksheetItemOptions.setSupportsSDIs(true, this.config.getProperty(DATA_SOURCE_PROP), sourcesdc);
            worksheetItemOptions.addOperations(this.config.getCollection("operations"));
        }
    }

    @Override
    public void setupIncludes(WorksheetItemIncludes worksheetItemIncludes) {
        worksheetItemIncludes.addScriptInclude("WEB-CORE/modules/eln/worksheetitem/scripts/datatrend.js");
        worksheetItemIncludes.setJSObjectName("dataTrend");
    }

    @Override
    public String getCompleteHTML() throws SapphireException {
        return this.getHTMLNoException(2);
    }

    @Override
    public String getViewHTML() throws SapphireException {
        return this.getHTMLNoException(0);
    }

    private String getHTMLNoException(int mode) throws SapphireException {
        String html;
        try {
            html = this.getHTML(mode);
        }
        catch (SapphireException e) {
            this.logError(e);
            DataChartHelper dataChartHelper = new DataChartHelper(this.getConnectionProcessor().getConnectionid());
            html = dataChartHelper.formatErrorMessage(e.getMessage());
        }
        return html;
    }

    @Override
    public String getEditorHTML() throws SapphireException {
        return this.getHTMLNoException(3);
    }

    @Override
    public String getExportHTML(PropertyList exportOptions) throws SapphireException {
        return this.getHTML(1);
    }

    private String getHTML(int mode) throws SapphireException {
        PropertyList elements;
        DataChartHelper dataChartHelper;
        StringBuilder html = new StringBuilder();
        String webPageId = "LV_DataTrendControl";
        RequestContext requestContext = new RequestContext(new PropertyList());
        try {
            dataChartHelper = new DataChartHelper(this.getConnectionProcessor().getConnectionid());
        }
        catch (SapphireException e) {
            this.logError(e);
            throw new SapphireException("Cannot create data chart helper", e);
        }
        try {
            elements = dataChartHelper.getWebPageConfiguration(webPageId, requestContext);
        }
        catch (SapphireException e) {
            this.logError(e);
            throw new SapphireException("Cannot get properties for web page: " + webPageId, e);
        }
        this.setQueryDataSetProps(elements);
        this.setDataSetProps(elements);
        this.setAdaptersProps(elements);
        this.setDataChartProps(elements);
        requestContext.setProperty("pagedata", elements.getPropertyListNotNull("pagedata"));
        requestContext.setProperty("elements", elements);
        DataChart dataChart = new DataChart();
        dataChart.setPageContext(null);
        dataChart.setRequestContext(requestContext, this.getConnectionProcessor().getConnectionid());
        try {
            dataChart.init();
        }
        catch (SapphireException e) {
            this.logError(e);
            throw new SapphireException("Cannot create chart", e);
        }
        html.append(dataChartHelper.getChartImageTag(dataChart.getChartId(), this.getDetokenizedConfigProperty(WIDTH_PROP, "400"), this.getDetokenizedConfigProperty(HEIGHT_PROP, "400"), mode == 1 || mode == 2));
        html.append(mode == 1 ? "<br><br>" : "");
        return html.toString();
    }

    private void setDataChartProps(PropertyList elements) throws SapphireException {
        String elementId;
        String rangeMode;
        switch (rangeMode = this.getConfigProperty(RANGE_MODE_PROP, true)) {
            case "date": {
                elementId = "dateplot";
                break;
            }
            case "count": {
                elementId = "countplot";
                break;
            }
            case "sdilist": {
                elementId = "sdilistplot";
                break;
            }
            default: {
                throw new SapphireException("Unknown range mode: " + rangeMode);
            }
        }
        elements.getPropertyListNotNull("datachart").getPropertyListNotNull("plotprops").setProperty("elementid", elementId);
        PropertyListCollection titleCollection = elements.getPropertyListNotNull("datachart").getCollectionNotNull("titlecollection");
        for (int i = 0; i < titleCollection.size(); ++i) {
            PropertyList titleProps = titleCollection.getPropertyList(i);
            if (!titleProps.getProperty("titleid").equals(CHART_TITLE_ID)) continue;
            PropertyList textTitleProps = titleProps.getPropertyListNotNull("texttitleprops");
            textTitleProps.setProperty("text", this.getDetokenizedConfigProperty(TITLE_PROP));
        }
    }

    private void setAdaptersProps(PropertyList elements) throws SapphireException {
        String ignoreRemeasured;
        String ignoreRetested;
        String includeOutliers;
        String releasedOnly;
        String resultEntered;
        PropertyList sdiDataItemAdapterProps = new PropertyList();
        PropertyListCollection adapterCollection = elements.getPropertyListNotNull("dataprovider").getPropertyListNotNull("datasetadapterprops").getCollectionNotNull("adaptercollection");
        for (int i = 0; i < adapterCollection.size(); ++i) {
            PropertyList standardAdapterProps = adapterCollection.getPropertyList(i).getPropertyListNotNull("standardadapterprops");
            if (!standardAdapterProps.getProperty("standardadaptertype").equals("Add SDI Data Items")) continue;
            sdiDataItemAdapterProps = standardAdapterProps.getPropertyList("sdidataitemadapterprops");
        }
        sdiDataItemAdapterProps.setProperty("sdcid", this.getConfigProperty(TREND_OVER_SDC_PROP, true));
        PropertyList filtersProps = sdiDataItemAdapterProps.getPropertyListNotNull("filters");
        String ignoreCancelled = this.getConfigProperty(IGNORE_CANCELLED_PROP, false);
        if (!ignoreCancelled.isEmpty()) {
            filtersProps.setProperty(IGNORE_CANCELLED_PROP, ignoreCancelled);
        }
        if (!(resultEntered = this.getConfigProperty(RESULT_ENTERED_PROP, false)).isEmpty()) {
            filtersProps.setProperty(RESULT_ENTERED_PROP, resultEntered);
        }
        if (!(releasedOnly = this.getConfigProperty(RELEASED_ONLY_PROP, false)).isEmpty()) {
            filtersProps.setProperty(RELEASED_ONLY_PROP, releasedOnly);
        }
        if (!(includeOutliers = this.getConfigProperty(INCLUDE_OUTLIERS_PROP, false)).isEmpty()) {
            filtersProps.setProperty(INCLUDE_OUTLIERS_PROP, includeOutliers);
        }
        if (!(ignoreRetested = this.getConfigProperty(IGNORE_RETESTED_PROP, false)).isEmpty()) {
            filtersProps.setProperty(IGNORE_RETESTED_PROP, ignoreRetested);
        }
        if (!(ignoreRemeasured = this.getConfigProperty(IGNORE_REMEASURED_PROP, false)).isEmpty()) {
            filtersProps.setProperty(IGNORE_REMEASURED_PROP, ignoreRemeasured);
        }
        PropertyListCollection paramListFilters = filtersProps.getCollectionNotNull("paramlistfilters");
        paramListFilters.add(this.getParamListFilters());
        PropertyListCollection workItemFilters = filtersProps.getCollectionNotNull("workitemfilters");
        workItemFilters.add(this.getWorkItemFilters());
        PropertyList sequenceAdapterProps = new PropertyList();
        for (int i = 0; i < adapterCollection.size(); ++i) {
            PropertyList standardAdapterProps = adapterCollection.getPropertyList(i).getPropertyListNotNull("standardadapterprops");
            if (!standardAdapterProps.getProperty("standardadaptertype").equals("Sequence")) continue;
            sequenceAdapterProps = standardAdapterProps.getPropertyList("sequenceadapterprops");
        }
        PropertyList trendOverSdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(this.getConfigProperty(TREND_OVER_SDC_PROP, true)));
        String trendOverKeyColId1 = trendOverSdcProps.getProperty("keycolid1");
        String trendOverKeyColId2 = trendOverSdcProps.getProperty("keycolid1");
        String trendOverKeyColId3 = trendOverSdcProps.getProperty("keycolid1");
        PropertyListCollection sequenceKeyColumnCollection = sequenceAdapterProps.getCollectionNotNull("sequencekeycolumncollection");
        PropertyList sequenceKeyColumn1Props = new PropertyList();
        sequenceKeyColumn1Props.setProperty("columnid", trendOverKeyColId1);
        sequenceKeyColumnCollection.add(sequenceKeyColumn1Props);
        if (!trendOverKeyColId2.isEmpty()) {
            PropertyList sequenceKeyColumn2Props = new PropertyList();
            sequenceKeyColumn2Props.setProperty("columnid", trendOverKeyColId2);
            sequenceKeyColumnCollection.add(sequenceKeyColumn2Props);
            if (!trendOverKeyColId3.isEmpty()) {
                PropertyList sequenceKeyColumn3Props = new PropertyList();
                sequenceKeyColumn3Props.setProperty("columnid", trendOverKeyColId3);
                sequenceKeyColumnCollection.add(sequenceKeyColumn3Props);
            }
        }
    }

    private PropertyList getWorkItemFilters() throws SapphireException {
        PropertyList workItemFilters = new PropertyList();
        String workItemId = this.getConfigProperty("filters/workitemid", false);
        String workItemVersionId = this.getConfigProperty("filters/workitemversionid", false);
        String workItemInstance = this.getConfigProperty("filters/workiteminstance", false);
        if (!workItemId.isEmpty()) {
            workItemFilters.setProperty(WORK_ITEM_ID_PROP, workItemId);
        }
        if (!workItemVersionId.isEmpty()) {
            workItemFilters.setProperty(WORK_ITEM_VERSION_ID_PROP, workItemVersionId);
        }
        if (!workItemInstance.isEmpty()) {
            workItemFilters.setProperty(WORK_ITEM_INSTANCE_PROP, workItemInstance);
        }
        return workItemFilters;
    }

    private void setDataSetProps(PropertyList elements) throws SapphireException {
        String rangeMode;
        PropertyList seriesGroupBuilderProps = elements.getPropertyListNotNull("dataprovider").getPropertyListNotNull("datasetseriesprops").getCollectionNotNull("plotcollection").getPropertyList(0).getCollectionNotNull("seriesgroupcollection").getPropertyList(0).getCollectionNotNull("seriesgroupbuildercollection").getPropertyList(0);
        switch (rangeMode = this.getConfigProperty(RANGE_MODE_PROP, true)) {
            case "date": {
                seriesGroupBuilderProps.setProperty("seriesgrouptype", "Time Period Values Collection");
                PropertyList timePeriodValuesCollectionProps = seriesGroupBuilderProps.getPropertyListNotNull("timeperiodvaluescollectionprops");
                timePeriodValuesCollectionProps.setProperty("xcolumn", this.getConfigProperty(ORDER_BY_COLUMN_PROP, true));
                break;
            }
            case "count": {
                seriesGroupBuilderProps.setProperty("seriesgrouptype", "XY Series Collection");
                PropertyList xySeriesCollectionProps = seriesGroupBuilderProps.getPropertyListNotNull("xyseriescollectionprops");
                xySeriesCollectionProps.setProperty("xcolumn", COUNT_COLUMN);
                break;
            }
            case "sdilist": {
                PropertyList trendOverSdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(this.getConfigProperty(TREND_OVER_SDC_PROP, true)));
                String trendOverKeyColId1 = trendOverSdcProps.getProperty("keycolid1");
                seriesGroupBuilderProps.setProperty("seriesgrouptype", "Default Category Dataset");
                PropertyList defaultCategoryDataSet = seriesGroupBuilderProps.getPropertyListNotNull("defaultcategorydatasetprops");
                defaultCategoryDataSet.setProperty("xcolumn", trendOverKeyColId1);
                break;
            }
            default: {
                throw new SapphireException("Unknown range mode: " + rangeMode);
            }
        }
    }

    private void setQueryDataSetProps(PropertyList elements) throws SapphireException {
        String rangeMode;
        StringBuilder fromClause = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        switch (rangeMode = this.getConfigProperty(RANGE_MODE_PROP, true)) {
            case "date": {
                this.buildFromAndWhereForDateMode(fromClause, whereClause);
                break;
            }
            case "count": {
                this.buildFromAndWhereForCountMode(fromClause, whereClause);
                break;
            }
            case "sdilist": {
                this.buildFromAndWhereForSDIListMode(fromClause, whereClause);
                break;
            }
            default: {
                throw new SapphireException("Unknown range mode: " + rangeMode);
            }
        }
        PropertyList queryDataSetProps = elements.getPropertyListNotNull("dataprovider").getPropertyListNotNull("datasetprops").getPropertyListNotNull("standarddatasetprops").getPropertyListNotNull("querydatasetprops");
        queryDataSetProps.setProperty("basedonid", this.getConfigProperty(SOURCE_SDC_PROP, true));
        queryDataSetProps.setProperty("fromclause", fromClause.toString());
        queryDataSetProps.setProperty("whereclause", whereClause.toString());
    }

    private void buildFromAndWhereForCountMode(StringBuilder fromClause, StringBuilder whereClause) throws SapphireException {
        DataSet sdiDs = this.getSourceSDIDataSet();
        String backCount = this.getConfigProperty(BACK_COUNT_PROP, true);
        String trendOverSdcId = this.getConfigProperty(SOURCE_SDC_PROP, true);
        PropertyList trendOverSdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(trendOverSdcId));
        String trendOverTableId = trendOverSdcProps.getProperty("tableid");
        String trendOverKeyColId1 = trendOverSdcProps.getProperty("keycolid1");
        String trendOverKeyColId2 = trendOverSdcProps.getProperty("keycolid2");
        String trendOverKeyColId3 = trendOverSdcProps.getProperty("keycolid3");
        if (sdiDs.getRowCount() == 0) {
            throw new SapphireException("Choose base SDI for count trend");
        }
        if (sdiDs.getRowCount() > 1) {
            throw new SapphireException("Choose only single base SDI for count trend");
        }
        String orderByColumn = this.getConfigProperty(ORDER_BY_COLUMN_PROP, true);
        String baseSDIKeyId1 = sdiDs.getString(0, "keyid1", "");
        String baseSDIKeyId2 = sdiDs.getString(0, "keyid2", "");
        String baseSDIKeyId3 = sdiDs.getString(0, "keyid3", "");
        fromClause.append(trendOverTableId);
        whereClause.append(trendOverKeyColId1).append(" IN (");
        if (this.getConnectionProcessor().getSapphireConnection().getSapphireDatabase().getDbms().equals("ORA")) {
            whereClause.append("(SELECT ").append(trendOverKeyColId1).append(" FROM (SELECT ").append(trendOverTableId).append(".").append(trendOverKeyColId1).append(", row_number() OVER (ORDER BY ").append(trendOverTableId).append(".").append(orderByColumn).append(" DESC) r");
        } else {
            whereClause.append("SELECT TOP ").append(backCount).append(" ").append(trendOverTableId).append(".").append(trendOverKeyColId1);
        }
        whereClause.append(" FROM ").append(trendOverTableId).append(", ").append(trendOverTableId).append(" p ");
        StringBuilder matchingColumnsWhereFragment = this.getMatchingColumnsWhereFragment(trendOverTableId);
        if (matchingColumnsWhereFragment.length() <= 0) {
            throw new SapphireException("Choose at least single matching column");
        }
        whereClause.append(" WHERE (").append(matchingColumnsWhereFragment.substring(4)).append(")");
        whereClause.append(" AND (p.").append(trendOverKeyColId1).append(" = '").append(baseSDIKeyId1).append("'");
        if (!trendOverKeyColId2.isEmpty() && !baseSDIKeyId2.isEmpty()) {
            whereClause.append("AND p.").append(trendOverKeyColId2).append(" = '").append(baseSDIKeyId2).append("'");
            if (!trendOverKeyColId3.isEmpty() && !baseSDIKeyId3.isEmpty()) {
                whereClause.append(" AND p.").append(trendOverKeyColId3).append(" = '").append(baseSDIKeyId3).append("'");
            }
        }
        whereClause.append(")");
        whereClause.append("AND ").append(trendOverTableId).append(".").append(orderByColumn).append(" <= p.").append(orderByColumn);
        if (this.getConnectionProcessor().getSapphireConnection().getSapphireDatabase().getDbms().equals("ORA")) {
            whereClause.append(")  WHERE r <= ").append(backCount).append(")");
        } else {
            whereClause.append(" ORDER BY ").append(trendOverTableId).append(".").append(orderByColumn).append(" DESC");
        }
        whereClause.append(")");
    }

    private StringBuilder getMatchingColumnsWhereFragment(String trendOverTableId) throws SapphireException {
        StringBuilder matchingColumnsWhereFragment = new StringBuilder();
        PropertyListCollection matchingColumns = this.getConfigCollection(MATCHING_COLUMNS_COLLECTION, true);
        for (int i = 0; i < matchingColumns.size(); ++i) {
            PropertyList matchingColumnProps = matchingColumns.getPropertyList(i);
            String columnId = matchingColumnProps.getProperty("columnid");
            if (columnId.isEmpty()) continue;
            matchingColumnsWhereFragment.append(" AND ").append(trendOverTableId).append(".").append(columnId).append(" = p.").append(columnId);
        }
        return matchingColumnsWhereFragment;
    }

    private void buildFromAndWhereForSDIListMode(StringBuilder fromClause, StringBuilder whereClause) throws SapphireException {
        String dataSourceKeyId2;
        String dataSourceKeyId1;
        String dataSourceKeyColId2;
        String dataSourceKeyColId1;
        String dataSourceTableId;
        String sourceSdcId = this.getConfigProperty(SOURCE_SDC_PROP, true);
        String trendOverSdcId = this.getConfigProperty(SOURCE_SDC_PROP, true);
        PropertyList sourceSdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(sourceSdcId));
        String sourceTableId = sourceSdcProps.getProperty("tableid");
        String sourceKeyColId1 = sourceSdcProps.getProperty("keycolid1");
        String sourceKeyColId2 = sourceSdcProps.getProperty("keycolid2");
        String sourceKeyColId3 = sourceSdcProps.getProperty("keycolid3");
        String dataSource = this.getConfigProperty(DATA_SOURCE_PROP, true);
        if (dataSource.equals(DATA_SOURCE_CONTROL)) {
            dataSourceTableId = "worksheetitemsdi";
            dataSourceKeyColId1 = "worksheetitemid";
            dataSourceKeyColId2 = "worksheetitemversionid";
            dataSourceKeyId1 = this.getWorksheetItemId();
            dataSourceKeyId2 = this.getWorksheetItemVersionId();
        } else {
            dataSourceTableId = "worksheetsdi";
            dataSourceKeyColId1 = "worksheetid";
            dataSourceKeyColId2 = "worksheetversionid";
            dataSourceKeyId1 = this.getWorksheetId();
            dataSourceKeyId2 = this.getWorksheetVersionId();
        }
        whereClause.append(dataSourceTableId).append(".sdcid = '").append(sourceSdcId).append("' AND ").append(dataSourceTableId).append(".").append(dataSourceKeyColId1).append(" = '").append(dataSourceKeyId1).append("' AND ").append(dataSourceTableId).append(".").append(dataSourceKeyColId2).append(" = '").append(dataSourceKeyId2).append("'");
        fromClause.append(dataSourceTableId).append(" ");
        if (sourceSdcId.equals("SDIWorkItem")) {
            fromClause.append("JOIN sdiworkitem ON ").append(dataSourceTableId).append(".keyid1 = sdiworkitem.sdiworkitemid ");
            if (!trendOverSdcId.equals("SDIWorkItem")) {
                fromClause.append("JOIN ").append(sourceTableId).append(" ON sdiworkitem.keyid1 = ").append(sourceTableId).append(".").append(sourceKeyColId1);
                if (!sourceKeyColId2.isEmpty()) {
                    fromClause.append(" AND sdiworkitem.keyid2 = ").append(sourceTableId).append(".").append(sourceKeyColId2);
                    if (!sourceKeyColId3.isEmpty()) {
                        fromClause.append(" AND sdiworkitem.keyid3 = ").append(sourceTableId).append(".").append(sourceKeyColId3);
                    }
                }
                whereClause.append("AND sdiworkitem.sdcid = '").append(trendOverSdcId).append("'");
            }
        } else {
            fromClause.append("JOIN ").append(sourceTableId).append(" ON ").append(dataSourceTableId).append(".keyid1 = ").append(sourceTableId).append(".").append(sourceKeyColId1);
            if (!sourceKeyColId2.isEmpty()) {
                fromClause.append(" AND ").append(dataSourceTableId).append(".keyid2 = ").append(sourceTableId).append(".").append(sourceKeyColId2);
                if (!sourceKeyColId3.isEmpty()) {
                    fromClause.append(" AND ").append(dataSourceTableId).append(".keyid3 = ").append(sourceTableId).append(".").append(sourceKeyColId3);
                }
            }
        }
    }

    private void buildFromAndWhereForDateMode(StringBuilder fromClause, StringBuilder whereClause) throws SapphireException {
        String trendOverSdcId = this.getConfigProperty(SOURCE_SDC_PROP, true);
        PropertyList trendOverSdcProps = new PropertyList(this.getSDCProcessor().getSDCProperties(trendOverSdcId));
        String trendOverTableId = trendOverSdcProps.getProperty("tableid");
        String trendOverKeyColId1 = trendOverSdcProps.getProperty("keycolid1");
        String trendOverKeyColId2 = trendOverSdcProps.getProperty("keycolid2");
        String trendOverKeyColId3 = trendOverSdcProps.getProperty("keycolid3");
        DataSet sdiDs = this.getSourceSDIDataSet();
        String dateRangeColumn = this.getConfigProperty(ORDER_BY_COLUMN_PROP, true);
        String daysBack = this.getConfigProperty(DAYS_BACK_PROP, true);
        String daysForward = this.getConfigProperty(DAYS_FORWARD_PROP, true);
        if (sdiDs.getRowCount() == 0) {
            throw new SapphireException("Choose base SDI for data trend");
        }
        if (sdiDs.getRowCount() > 1) {
            throw new SapphireException("Choose only single base SDI for date trend");
        }
        String baseSDIKeyId1 = sdiDs.getString(0, "keyid1", "");
        String baseSDIKeyId2 = sdiDs.getString(0, "keyid2", "");
        String baseSDIKeyId3 = sdiDs.getString(0, "keyid3", "");
        fromClause.append(trendOverTableId).append(", ").append(trendOverTableId).append(" p");
        whereClause.append("(p.").append(trendOverKeyColId1).append(" = '").append(baseSDIKeyId1).append("'");
        if (!trendOverKeyColId2.isEmpty() && !baseSDIKeyId2.isEmpty()) {
            whereClause.append("AND p.").append(trendOverKeyColId2).append(" = '").append(baseSDIKeyId2).append("'");
            if (!trendOverKeyColId3.isEmpty() && !baseSDIKeyId3.isEmpty()) {
                whereClause.append(" AND p.").append(trendOverKeyColId3).append(" = '").append(baseSDIKeyId3).append("'");
            }
        }
        whereClause.append(")");
        StringBuilder matchingColumnsWhereFragment = this.getMatchingColumnsWhereFragment(trendOverTableId);
        if (matchingColumnsWhereFragment.length() <= 0) {
            throw new SapphireException("Choose at least single matching column");
        }
        whereClause.append(" AND (").append(matchingColumnsWhereFragment.substring(4)).append(")");
        whereClause.append(" AND (").append(trendOverTableId).append(".").append(dateRangeColumn).append(" BETWEEN p.").append(dateRangeColumn).append(" - ").append(daysBack).append(" AND p.").append(dateRangeColumn).append(" + ").append(daysForward).append(")");
    }

    private String getConfigProperty(String propertyId, boolean mandatory) throws SapphireException {
        String propertyValue = this.getDetokenizedConfigProperty(propertyId);
        if (mandatory && propertyValue.isEmpty()) {
            this.throwRequiredException(propertyId);
        }
        return propertyValue;
    }

    private PropertyListCollection getConfigCollection(String collectionId, boolean mandatory) throws SapphireException {
        PropertyListCollection collection = this.config.getCollection(collectionId);
        if (mandatory && collection == null) {
            return this.throwRequiredException(collectionId);
        }
        return collection;
    }

    private PropertyListCollection throwRequiredException(String id) throws SapphireException {
        String displayName = PROPERTY_DISPLAY_NAME_MAP.get(id);
        if (displayName == null) {
            displayName = id;
        }
        String message = displayName + " is required.";
        this.worksheetItemOptions.setRequiresConfig(true, this.getTranslationProcessor().translate(message));
        throw new SapphireException(message);
    }

    private PropertyList getParamListFilters() throws SapphireException {
        PropertyList paramListFilter = new PropertyList();
        String paramListId = this.getConfigProperty("filters/paramlistid", false);
        String paramListVersionId = this.getConfigProperty("filters/paramlistversionid", false);
        String variantId = this.getConfigProperty("filters/variantid", false);
        String paramId = this.getConfigProperty("filters/paramid", false);
        String paramType = this.getConfigProperty("filters/paramtype", false);
        if (!paramListId.isEmpty()) {
            paramListFilter.setProperty(PARAM_LIST_ID_PROP, paramListId);
        }
        if (!paramListVersionId.isEmpty()) {
            paramListFilter.setProperty(PARAM_LIST_VERSION_ID_PROP, paramListVersionId);
        }
        if (!variantId.isEmpty()) {
            paramListFilter.setProperty(VARIANT_ID_PROP, variantId);
        }
        if (!paramId.isEmpty()) {
            paramListFilter.setProperty(PARAM_ID_PROP, paramId);
        }
        if (!paramType.isEmpty()) {
            paramListFilter.setProperty(PARAM_TYPE_PROP, paramType);
        }
        return paramListFilter;
    }

    private DataSet getSourceSDIDataSet() throws SapphireException {
        String selectClause;
        String fromClause;
        String whereClause;
        String sourceTableId;
        String sourceSdcId = this.getConfigProperty(SOURCE_SDC_PROP, true);
        String dataSource = this.getConfigProperty(DATA_SOURCE_PROP, true);
        ArrayList<String> sqlParams = new ArrayList<String>();
        if (dataSource.equals(DATA_SOURCE_CONTROL)) {
            sourceTableId = "worksheetitemsdi";
            whereClause = sourceTableId + ".worksheetitemid = ? AND " + sourceTableId + ".worksheetitemversionid = ? AND " + sourceTableId + ".sdcid = ?";
            sqlParams.add(this.getWorksheetItemId());
            sqlParams.add(this.getWorksheetItemVersionId());
            sqlParams.add(sourceSdcId);
        } else {
            sourceTableId = "worksheetsdi";
            whereClause = sourceTableId + ".worksheetid = ? AND " + sourceTableId + ".worksheetversionid = ? AND " + sourceTableId + ".sdcid = ?";
            sqlParams.add(this.getWorksheetId());
            sqlParams.add(this.getWorksheetVersionId());
            sqlParams.add(sourceSdcId);
        }
        if (sourceSdcId.equals("SDIWorkItem")) {
            fromClause = sourceTableId + " JOIN sdiworkitem on worksheetitemsdi.keyid1 = sdiworkitem.sdiworkitemid";
            selectClause = "sdiworkitem.sdcid, sdiworkitem.keyid1, sdiworkitem.keyid2, sdiworkitem.keyid3";
        } else {
            fromClause = sourceTableId;
            selectClause = sourceTableId + ".sdcid, " + sourceTableId + ".keyid1, " + sourceTableId + ".keyid2, " + sourceTableId + ".keyid3";
        }
        String sql = "SELECT " + selectClause + " FROM " + fromClause + " WHERE " + whereClause;
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, sqlParams.toArray());
        return ds;
    }

    static {
        PROPERTY_DISPLAY_NAME_MAP.put(TREND_OVER_SDC_PROP, "Trend Over SDC");
        PROPERTY_DISPLAY_NAME_MAP.put(SOURCE_SDC_PROP, "Source SDC");
        PROPERTY_DISPLAY_NAME_MAP.put(RANGE_MODE_PROP, "Range Mode");
        PROPERTY_DISPLAY_NAME_MAP.put(ORDER_BY_COLUMN_PROP, "Order By Column");
        PROPERTY_DISPLAY_NAME_MAP.put(DATA_SOURCE_PROP, "Data Source");
        PROPERTY_DISPLAY_NAME_MAP.put(PARAM_LIST_ID_PROP, "Param List");
        PROPERTY_DISPLAY_NAME_MAP.put(PARAM_LIST_VERSION_ID_PROP, "Param List Version");
        PROPERTY_DISPLAY_NAME_MAP.put(VARIANT_ID_PROP, "Variant");
        PROPERTY_DISPLAY_NAME_MAP.put(PARAM_ID_PROP, "Param");
        PROPERTY_DISPLAY_NAME_MAP.put(PARAM_TYPE_PROP, "Param Type");
        PROPERTY_DISPLAY_NAME_MAP.put(DAYS_BACK_PROP, "Days Back");
        PROPERTY_DISPLAY_NAME_MAP.put(DAYS_FORWARD_PROP, "Days Forward");
        PROPERTY_DISPLAY_NAME_MAP.put(MATCHING_COLUMNS_COLLECTION, "Matching Columns Collection");
        PROPERTY_DISPLAY_NAME_MAP.put(WORK_ITEM_ID_PROP, "Work Item");
        PROPERTY_DISPLAY_NAME_MAP.put(WORK_ITEM_VERSION_ID_PROP, "Work Item Version");
        PROPERTY_DISPLAY_NAME_MAP.put(WORK_ITEM_INSTANCE_PROP, "Work item Instance");
        PROPERTY_DISPLAY_NAME_MAP.put(IGNORE_CANCELLED_PROP, "Ignore Cancelled");
        PROPERTY_DISPLAY_NAME_MAP.put(RESULT_ENTERED_PROP, "Result Entered");
        PROPERTY_DISPLAY_NAME_MAP.put(RELEASED_ONLY_PROP, "Released Only");
        PROPERTY_DISPLAY_NAME_MAP.put(INCLUDE_OUTLIERS_PROP, "Include Outliers");
        PROPERTY_DISPLAY_NAME_MAP.put(IGNORE_RETESTED_PROP, "Ignore Retested");
        PROPERTY_DISPLAY_NAME_MAP.put(IGNORE_REMEASURED_PROP, "Ignore Remeasured");
        PROPERTY_DISPLAY_NAME_MAP.put(BACK_COUNT_PROP, "Back Count");
        PROPERTY_DISPLAY_NAME_MAP.put(TITLE_PROP, "Title");
    }
}

