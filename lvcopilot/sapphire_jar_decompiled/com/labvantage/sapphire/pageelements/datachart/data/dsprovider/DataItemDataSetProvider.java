/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.dsprovider;

import com.labvantage.sapphire.pageelements.datachart.data.Data;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.DataSetAdapterProcessor;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.AbstractDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.DataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.QueryDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.data.dsprovider.SDIListDataSetProvider;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.DataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.StandardDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.AdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.CustomDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.DataItemDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.QueryDataSetConfiguration;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.dsprovider.SDIListDataSetProviderConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.util.HashSet;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DataItemDataSetProvider
extends AbstractDataSetProvider {
    private static final String LIMITTYPEIDCOL = "limittypeid";
    private static final String SPECIDCOL = "specid";
    private final DataItemDataSetConfiguration dataItemDataSetConfiguration;
    private final String connectionId;
    private final DataBindingMap dataBindingMap;
    private PropertyList outputPropValues = new PropertyList();

    public DataItemDataSetProvider(String connectionId, DataItemDataSetConfiguration dataItemDataSetConf, DataBindingMap dataBindingMap) {
        super(connectionId);
        this.dataItemDataSetConfiguration = dataItemDataSetConf;
        this.connectionId = connectionId;
        this.dataBindingMap = dataBindingMap;
    }

    @Override
    public DataSet getDataSet() {
        String sdcId = "Sample";
        DataSet ds = new DataSet();
        if (this.dataItemDataSetConfiguration.getMode().equals(DataSetProviderConfiguration.DataSetType.CUSTOM.getName())) {
            DataSetProvider customDataSetProvider;
            try {
                CustomDataSetConfiguration customDataSetConf = new CustomDataSetConfiguration(this.dataItemDataSetConfiguration.getCustomDataSetProps(), this.dataItemDataSetConfiguration.getParent().getParent());
                customDataSetProvider = Data.createCustomDataSetProvider(customDataSetConf, this.connectionId, this.dataBindingMap);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Cannot create custom data set provider", e);
            }
            ds = customDataSetProvider.getDataSet();
            this.setRSetId(customDataSetProvider.getRSetId());
            this.outputPropValues = customDataSetProvider.getOutputProps();
        } else if (this.dataItemDataSetConfiguration.getMode().equals(StandardDataSetProviderConfiguration.StandardDataSetType.SDI_LIST.getName())) {
            SDIListDataSetProvider sdiListDataSetProvider;
            SDIListDataSetProviderConfiguration sdiListDataSetProviderConf = new SDIListDataSetProviderConfiguration(this.dataItemDataSetConfiguration.getSdiListDataSetProps(), this.dataItemDataSetConfiguration.getParent());
            try {
                sdiListDataSetProvider = new SDIListDataSetProvider(sdiListDataSetProviderConf, this.connectionId, this.dataBindingMap);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("Cannot create SDI List data set provider", e);
            }
            ds = sdiListDataSetProvider.getDataSet();
            this.setRSetId(sdiListDataSetProvider.getRSetId());
            this.outputPropValues = sdiListDataSetProvider.getOutputProps();
        } else {
            QueryDataSetProvider queryDataSetProvider;
            QueryDataSetConfiguration queryDataSetConf = new QueryDataSetConfiguration(this.dataItemDataSetConfiguration.getQueryDataSetProps(), this.dataItemDataSetConfiguration.getParent());
            try {
                queryDataSetProvider = new QueryDataSetProvider(this.connectionId, queryDataSetConf, this.dataBindingMap);
            }
            catch (SapphireException e) {
                throw new IllegalArgumentException("Cannot create query data set provide", e);
            }
            ds = queryDataSetProvider.getDataSet();
            this.setRSetId(queryDataSetProvider.getRSetId());
            sdcId = queryDataSetConf.getBasedOnId();
        }
        if (ds.getRowCount() > 0) {
            String[] rowNumKeyCols;
            PropertyListCollection sortColumns;
            PropertyList dataitemParamListKeyProps;
            boolean enableParamListKey;
            PropertyList sdiNotesAdapterProps = this.dataItemDataSetConfiguration.getAddSdiNotesAdapterProps().copy();
            boolean enableAddSdiNotesAdapter = sdiNotesAdapterProps.getProperty("enable", "Y").startsWith("Y");
            String addNotesColumnId = "";
            if (enableAddSdiNotesAdapter) {
                sdiNotesAdapterProps.setProperty("sdcid", "Sample");
                sdiNotesAdapterProps.setProperty("keyid1column", "s_sampleid");
                sdiNotesAdapterProps.setProperty("rsetid", this.getRSetId());
                addNotesColumnId = sdiNotesAdapterProps.getProperty("columnname", "sdinotes");
                PropertyList standardAdapterProps = new PropertyList("standardadapterprops");
                standardAdapterProps.setProperty("standardadaptertype", "Add SDI Notes");
                standardAdapterProps.setProperty("addsdinotesadapterprops", sdiNotesAdapterProps);
                PropertyList addSdiNotesAdapter = new PropertyList();
                addSdiNotesAdapter.setProperty("adapterid", "addsdinotes");
                addSdiNotesAdapter.setProperty("adaptertype", "Standard");
                addSdiNotesAdapter.setProperty("enable", "Yes");
                addSdiNotesAdapter.setProperty("standardadapterprops", standardAdapterProps);
                AdapterConfiguration adapterConfiguration = new AdapterConfiguration(addSdiNotesAdapter, this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDatasetAdapterConf());
                ds = this.runDataSetAdapter(ds, adapterConfiguration);
            }
            if (enableParamListKey = (dataitemParamListKeyProps = this.dataItemDataSetConfiguration.getDataItemParamListKeyProps()).getProperty("enable", "Y").startsWith("Y")) {
                dataitemParamListKeyProps.setProperty("sdcid", "Sample");
                dataitemParamListKeyProps.setProperty("keyid1column", "s_sampleid");
                if (dataitemParamListKeyProps.getProperty("columndefinition", "").isEmpty()) {
                    dataitemParamListKeyProps.setProperty("columndefinition", "[paramlistid]&[paramid]&[paramtype]");
                }
                dataitemParamListKeyProps.setProperty("displayvaluedefinition", "[paramlistid] / [paramid] ([paramtype])");
                dataitemParamListKeyProps.setProperty("rsetid", this.getRSetId());
                PropertyList standardAdapterProps = new PropertyList("standardadapterprops");
                standardAdapterProps.setProperty("standardadaptertype", "Find ParamListItem keys");
                standardAdapterProps.setProperty("dataitemparamlistkeyadapterprops", dataitemParamListKeyProps);
                PropertyList findParamListItemKeysProps = new PropertyList();
                findParamListItemKeysProps.setProperty("adapterid", "dataitemparamlistkey");
                findParamListItemKeysProps.setProperty("adaptertype", "Standard");
                findParamListItemKeysProps.setProperty("enable", "Yes");
                findParamListItemKeysProps.setProperty("standardadapterprops", standardAdapterProps);
                AdapterConfiguration adapterConfiguration = new AdapterConfiguration(findParamListItemKeysProps, this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDatasetAdapterConf());
                ds = this.runDataSetAdapter(ds, adapterConfiguration);
            }
            PropertyList sdiDataItemAdapterProps = this.dataItemDataSetConfiguration.getSdiDataItemAdapterProps().copy();
            sdiDataItemAdapterProps.setProperty("sdcid", sdcId);
            sdiDataItemAdapterProps.setProperty("rsetid", this.getRSetId());
            PropertyList specs = sdiDataItemAdapterProps.getPropertyListNotNull("specs");
            StringExpression includeSpecifications = new StringExpression(specs.getProperty("includespecs"));
            String includeSpecsStr = includeSpecifications.evaluateNoException(this.dataBindingMap);
            boolean includeSpecs = includeSpecsStr.startsWith("Y");
            if (specs.getProperty("includespecs", "").isEmpty()) {
                specs.setProperty("includespecs", "Y");
            }
            if (enableAddSdiNotesAdapter) {
                PropertyListCollection columns = sdiDataItemAdapterProps.getCollectionNotNull("columns");
                PropertyList notesCol = new PropertyList();
                notesCol.setProperty("columnid", addNotesColumnId);
                notesCol.setProperty("tableid", "primary");
                columns.add(notesCol);
            }
            if ((sortColumns = sdiDataItemAdapterProps.getCollectionNotNull("sortcolumns")).isEmpty()) {
                PropertyList newSortCol = new PropertyList();
                newSortCol.setProperty("columnid", "createdt");
                newSortCol.setProperty("tableid", "primary");
                sortColumns.add(newSortCol);
            }
            PropertyList filters = sdiDataItemAdapterProps.getPropertyListNotNull("filters");
            String releasedOnly = filters.getProperty("releasedonly", "Y");
            filters.setProperty("releasedonly", releasedOnly);
            String ignorecancelled = filters.getProperty("ignorecancelled", "Y");
            filters.setProperty("ignorecancelled", ignorecancelled);
            String resultentered = filters.getProperty("resultentered", "Y");
            filters.setProperty("resultentered", resultentered);
            PropertyList standardAdapterProps = new PropertyList("standardadapterprops");
            standardAdapterProps.setProperty("standardadaptertype", "Add SDI Data Items");
            standardAdapterProps.setProperty("sdidataitemadapterprops", sdiDataItemAdapterProps);
            PropertyList sdiDataItems = new PropertyList();
            sdiDataItems.setProperty("adapterid", "sdidataitems");
            sdiDataItems.setProperty("adaptertype", "Standard");
            sdiDataItems.setProperty("enable", "Yes");
            sdiDataItems.setProperty("standardadapterprops", standardAdapterProps);
            AdapterConfiguration adapterConfiguration = new AdapterConfiguration(sdiDataItems, this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDatasetAdapterConf());
            ds = this.runDataSetAdapter(ds, adapterConfiguration);
            String extractBondariesAdapterId = "dataitemdatasetprovider_extractboundaries";
            String sequenceAdapterId = "dataitemdatasetprovider_sequence1";
            List<AdapterConfiguration> adapterConfigurations = this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDatasetAdapterConf().getAdapterConfigurationList();
            for (int i = 0; i < adapterConfigurations.size(); ++i) {
                AdapterConfiguration adapterConfA = adapterConfigurations.get(i);
                if (!adapterConfA.getAdapterId().equals(extractBondariesAdapterId) && !adapterConfA.getAdapterId().equals(sequenceAdapterId)) continue;
                adapterConfigurations.remove(i);
                --i;
            }
            PropertyList sequenceAdapterProps = new PropertyList("sequenceadapterprops");
            PropertyListCollection sequenceKeyColumnsCollection = sequenceAdapterProps.getCollectionNotNull("sequencekeycolumncollection");
            for (String colid : rowNumKeyCols = new String[]{"sdcid", "keyid1"}) {
                PropertyList sequenceColProps = new PropertyList();
                sequenceColProps.setProperty("columnid", colid);
                sequenceKeyColumnsCollection.add(sequenceColProps);
            }
            sequenceAdapterProps.setProperty("sequencecolumnid", "rownumber");
            standardAdapterProps = new PropertyList("standardadapterprops");
            standardAdapterProps.setProperty("standardadaptertype", "Sequence");
            standardAdapterProps.setProperty("sequenceadapterprops", sequenceAdapterProps);
            PropertyList sequenceAdapter = new PropertyList();
            sequenceAdapter.setProperty("adapterid", sequenceAdapterId);
            sequenceAdapter.setProperty("adaptertype", "Standard");
            sequenceAdapter.setProperty("enable", "Yes");
            sequenceAdapter.setProperty("standardadapterprops", standardAdapterProps);
            adapterConfiguration = new AdapterConfiguration(sequenceAdapter, this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDatasetAdapterConf());
            this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDatasetAdapterConf().getAdapterConfigurationList().add(adapterConfiguration);
            if (includeSpecs) {
                PropertyList boundary;
                PropertyList extractBoundariesAdapterProps = this.dataItemDataSetConfiguration.getExtractBoundariesAdapterProps().copy();
                String valueCol = extractBoundariesAdapterProps.getProperty("valuecolumn", "");
                if (valueCol.isEmpty()) {
                    extractBoundariesAdapterProps.setProperty("valuecolumn", "transformvalue");
                }
                HashSet<String> limittypes = new HashSet<String>();
                HashSet<String> specids = new HashSet<String>();
                for (int i = 0; i < ds.getRowCount(); ++i) {
                    String specid;
                    String limittype = ds.getValue(i, LIMITTYPEIDCOL, "");
                    if (!limittype.isEmpty()) {
                        limittypes.add(limittype);
                    }
                    if ((specid = ds.getValue(i, SPECIDCOL, "")).isEmpty()) continue;
                    specids.add(specid);
                }
                extractBoundariesAdapterProps.setProperty("boundarykeycolumn", LIMITTYPEIDCOL);
                PropertyListCollection boundaryKeyCols = extractBoundariesAdapterProps.getCollectionNotNull("boundarykeycolumns");
                if (specids.size() > 1) {
                    PropertyList extraKeyCol = new PropertyList();
                    extraKeyCol.setProperty("columnid", SPECIDCOL);
                    boundaryKeyCols.add(extraKeyCol);
                }
                PropertyListCollection boundariesCollection = extractBoundariesAdapterProps.getCollectionNotNull("boundarycollection");
                boolean showOneLimittypeOnly = extractBoundariesAdapterProps.getProperty("showonelimittypeonly", "Y").toLowerCase().startsWith("y");
                String limittypeToShow = extractBoundariesAdapterProps.getProperty("showlimittype", "InSpec");
                PropertyList overlappingLimitProps = extractBoundariesAdapterProps.getPropertyListNotNull("overlappinglimitprops");
                boolean hideOverLappingLimits = overlappingLimitProps.getProperty("hideoverlappinglimits", "Y").toLowerCase().startsWith("y");
                boolean showInSpecLimit = overlappingLimitProps.getProperty("showinspeclimits", "No").toLowerCase().startsWith("y");
                String seriesIdColumn = this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDataSetConfiguration().getSeriesIdColumn();
                if (showOneLimittypeOnly) {
                    Object boundary2 = new PropertyList();
                    ((PropertyList)boundary2).setProperty("boundaryid", limittypeToShow);
                    ((PropertyList)boundary2).setProperty("boundaryvaluecol", "value1");
                    ((PropertyList)boundary2).setProperty("boundaryseriesid", "$G{(dataset.getValue(rownumber, \"" + seriesIdColumn + "\") + \" " + limittypeToShow + "\" +  \" value1\")" + "}");
                    boundariesCollection.add(boundary2);
                    boundary2 = new PropertyList();
                    ((PropertyList)boundary2).setProperty("boundaryid", limittypeToShow);
                    ((PropertyList)boundary2).setProperty("boundaryvaluecol", "value2");
                    ((PropertyList)boundary2).setProperty("boundaryseriesid", "$G{(dataset.getValue(rownumber, \"" + seriesIdColumn + "\") + \" " + limittypeToShow + "\" +  \" value2\")" + "}");
                    boundariesCollection.add(boundary2);
                } else if (!hideOverLappingLimits) {
                    for (String limittype : limittypes) {
                        boundary = new PropertyList();
                        boundary.setProperty("boundaryid", limittype);
                        boundary.setProperty("boundaryvaluecol", "value1");
                        boundary.setProperty("boundaryseriesid", "$G{(dataset.getValue(rownumber, \"" + seriesIdColumn + "\") + \" " + limittype + "\" +  \" value1\")" + "}");
                        boundariesCollection.add(boundary);
                        boundary = new PropertyList();
                        boundary.setProperty("boundaryid", limittype);
                        boundary.setProperty("boundaryvaluecol", "value2");
                        boundary.setProperty("boundaryseriesid", "$G{(dataset.getValue(rownumber, \"" + seriesIdColumn + "\") + \" " + limittype + "\" +  \" value2\")" + "}");
                        boundariesCollection.add(boundary);
                    }
                } else {
                    for (String limittype : limittypes) {
                        if (!showInSpecLimit && limittype.toLowerCase().startsWith("inspec")) continue;
                        boundary = new PropertyList();
                        boundary.setProperty("boundaryid", limittype);
                        String script = "(dataset.getValue(rownumber, \"operator1\").equals(\">=\") && !dataset.getValue(rownumber, \"value2\").isEmpty()) ? \"value2\" : \"value1\"";
                        boundary.setProperty("boundaryvaluecol", "$G{" + script + "}");
                        if (limittype.startsWith("Upper") || limittype.startsWith("Lower")) {
                            String seriesGroupId = limittype.substring(6);
                            boundary.setProperty("seriesgroupid", seriesGroupId);
                        } else if (limittype.equals("Target")) {
                            boundary.setProperty("seriesgroupid", limittype);
                        }
                        boundariesCollection.add(boundary);
                    }
                }
                PropertyListCollection primaryKeyColumns = extractBoundariesAdapterProps.getCollectionNotNull("keycolumns");
                PropertyList keyColProps = new PropertyList();
                keyColProps.setProperty("columnid", "sdidataitemid");
                primaryKeyColumns.add(keyColProps);
                standardAdapterProps = new PropertyList("standardadapterprops");
                standardAdapterProps.setProperty("standardadaptertype", "Extract Boundaries");
                standardAdapterProps.setProperty("extractboundariesadapterprops", extractBoundariesAdapterProps);
                PropertyList extractBoundaries = new PropertyList();
                extractBoundaries.setProperty("adapterid", extractBondariesAdapterId);
                extractBoundaries.setProperty("adaptertype", "Standard");
                extractBoundaries.setProperty("enable", "Yes");
                extractBoundaries.setProperty("standardadapterprops", standardAdapterProps);
                adapterConfiguration = new AdapterConfiguration(extractBoundaries, this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDatasetAdapterConf());
                this.dataItemDataSetConfiguration.getParent().getParent().getParent().getDatasetAdapterConf().getAdapterConfigurationList().add(adapterConfiguration);
            }
        }
        return ds;
    }

    private DataSet runDataSetAdapter(DataSet ds, AdapterConfiguration adapterConfiguration) {
        DataSetAdapterProcessor adapterProcessor = new DataSetAdapterProcessor(this.connectionId);
        if (adapterConfiguration.isEnabled()) {
            adapterProcessor.processDataSetAdapter(adapterConfiguration, ds, this.dataBindingMap);
            ds = adapterProcessor.getProcessedDataSet();
            PropertyList adapterProcessorOutputProps = adapterProcessor.getOutputProps();
            this.outputPropValues.setProperty(adapterConfiguration.getAdapterId(), adapterProcessorOutputProps);
        }
        return ds;
    }

    @Override
    public PropertyList getOutputProps() {
        return this.outputPropValues;
    }
}

