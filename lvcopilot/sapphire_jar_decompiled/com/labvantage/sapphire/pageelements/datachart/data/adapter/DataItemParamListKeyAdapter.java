/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.data.adapter;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.pageelements.datachart.data.adapter.AbstractDataSetAdapter;
import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.DataItemParamListKeyAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.DataBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import com.labvantage.sapphire.pageelements.datachart.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import sapphire.SapphireException;
import sapphire.action.ActionConstants;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class DataItemParamListKeyAdapter
extends AbstractDataSetAdapter
implements ActionConstants {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String rSetId;
    private final DataItemParamListKeyAdapterConfiguration dataItemParamListKeyAdapterConf;
    private String outputproperty;
    private PropertyList outputProps;

    public DataItemParamListKeyAdapter(String connectionId, DataBindingMap dataBindingMap, DataItemParamListKeyAdapterConfiguration dataItemParamListKeyAdapterConf) {
        super(connectionId, dataBindingMap);
        this.dataItemParamListKeyAdapterConf = dataItemParamListKeyAdapterConf;
        try {
            this.rSetId = dataItemParamListKeyAdapterConf.getRsetId().evaluate(dataBindingMap);
        }
        catch (SapphireException e) {
            throw new IllegalArgumentException("Cannot evaluate RSet ID expression: " + dataItemParamListKeyAdapterConf.getRsetId().getExpression());
        }
        this.outputproperty = dataItemParamListKeyAdapterConf.getOutputproperty();
    }

    @Override
    public void processDataSetAdapter(DataSet dataSet) throws SapphireException {
        String sdcId = this.dataItemParamListKeyAdapterConf.getSdcId();
        String keyId1Column = this.dataItemParamListKeyAdapterConf.getKeyId1Column();
        String keyId2Column = this.dataItemParamListKeyAdapterConf.getKeyId2Column();
        String keyId3Column = this.dataItemParamListKeyAdapterConf.getKeyId3Column();
        this.outputProps = new PropertyList();
        if (sdcId.isEmpty()) {
            throw new IllegalArgumentException("SDC ID is empty");
        }
        if (keyId1Column.isEmpty()) {
            throw new IllegalArgumentException("Key ID 1 column is empty");
        }
        StringExpression columnDefExpresssion = this.dataItemParamListKeyAdapterConf.getColumnDefinitionValue();
        String columnDefinitionValue = columnDefExpresssion.getExpression().contains("[") && !columnDefExpresssion.getExpression().startsWith("$G{") ? columnDefExpresssion.getExpression() : columnDefExpresssion.evaluate(this.getDataBindingMap());
        StringExpression displayValueDefinitionExpression = this.dataItemParamListKeyAdapterConf.getDisplayDefinitionValue();
        String displayValueDefinition = displayValueDefinitionExpression.getExpression().contains("[") && !displayValueDefinitionExpression.getExpression().startsWith("$G{") ? displayValueDefinitionExpression.getExpression() : displayValueDefinitionExpression.evaluate(this.getDataBindingMap());
        int sdiCount = 0;
        boolean clearRSet = false;
        String result = "";
        if (this.rSetId.isEmpty()) {
            ArrayList<String> keyId1List = new ArrayList<String>();
            ArrayList<String> keyId2List = new ArrayList<String>();
            ArrayList<String> keyId3List = new ArrayList<String>();
            Util.populateKeyLists(dataSet, keyId1List, keyId2List, keyId3List, sdcId, keyId1Column, keyId2Column, keyId3Column);
            sdiCount = keyId1List.size();
            StringBuilder keyId1s = new StringBuilder();
            StringBuilder keyId2s = new StringBuilder();
            StringBuilder keyId3s = new StringBuilder();
            for (int i = 0; i < sdiCount; ++i) {
                keyId1s.append(";").append((String)keyId1List.get(i));
                keyId2s.append(";").append((String)keyId2List.get(i));
                keyId3s.append(";").append((String)keyId3List.get(i));
            }
            this.rSetId = this.getDAMProcessor().createRSet(sdcId, keyId1s.substring(1), keyId2s.substring(1), keyId3s.substring(1));
            clearRSet = true;
        }
        if (sdiCount > 0 || !this.rSetId.isEmpty()) {
            List<Object> valueTokens = OpalUtil.getKeywordTokens(columnDefinitionValue);
            if (valueTokens.size() == 0) {
                valueTokens = Arrays.asList(StringUtil.split(columnDefinitionValue, "&"));
                columnDefinitionValue = "";
                for (int j = 0; j < valueTokens.size(); ++j) {
                    columnDefinitionValue = columnDefinitionValue + "&[" + (String)valueTokens.get(j) + "]";
                }
                if (!columnDefinitionValue.isEmpty()) {
                    columnDefinitionValue = columnDefinitionValue.substring(1);
                }
            }
            String columns = "";
            for (int i = 0; i < valueTokens.size(); ++i) {
                String col = (String)valueTokens.get(i);
                columns = columns + "," + col;
            }
            if (!columns.isEmpty()) {
                columns = columns.substring(1);
            }
            String paramSql = "select distinct sdidataitem.paramlistid, sdidataitem.paramlistversionid, sdidataitem.variantid, sdidataitem.paramid, sdidataitem.paramtype from rsetitems left join sdidataitem on sdidataitem.sdcid = rsetitems.sdcid and sdidataitem.keyid1 = rsetitems.keyid1 and sdidataitem.keyid2 = rsetitems.keyid2 and sdidataitem.keyid3 = rsetitems.keyid3 where rsetitems.rsetid = ? and sdidataitem.datatypes in ('N', 'NC') and sdidataitem.displayvalue is not null order by paramlistid, paramlistversionid, variantid, paramid, paramtype desc";
            DataSet paramDs = this.getQueryProcessor().getPreparedSqlDataSet(paramSql, (Object[])new String[]{this.rSetId});
            if (clearRSet) {
                this.getDAMProcessor().clearRSet(this.rSetId);
            }
            ArrayList<DataSet> groupedDs = paramDs.getGroupedDataSets(columns);
            for (DataSet paramDataSet : groupedDs) {
                ArrayList displayValueTokens = null;
                String paramlistid = paramDataSet.getString(0, "paramlistid");
                String paramlistversionid = paramDataSet.getString(0, "paramlistversionid");
                String variantid = paramDataSet.getString(0, "variantid");
                String paramid = paramDataSet.getString(0, "paramid");
                String paramtype = paramDataSet.getString(0, "paramtype");
                HashMap<String, String> substitutionParams = new HashMap<String, String>();
                substitutionParams.put("paramlistid", paramlistid);
                substitutionParams.put("paramlistversionid", paramlistversionid);
                substitutionParams.put("variantid", variantid);
                substitutionParams.put("paramid", paramid);
                substitutionParams.put("paramtype", paramtype);
                displayValueTokens = OpalUtil.getKeywordTokens(displayValueDefinition);
                String itemDisplayValue = OpalUtil.searchAndReplaceTokens(displayValueDefinition, displayValueTokens, substitutionParams, false);
                String definitionValue = OpalUtil.searchAndReplaceTokens(columnDefinitionValue, valueTokens, substitutionParams, false);
                result = result + ";" + definitionValue + "|" + itemDisplayValue;
            }
            if (result.length() > 0) {
                result = result.substring(1);
            }
        }
        this.setProcessedDataSet(dataSet);
        this.outputProps.setProperty(this.outputproperty, result);
    }

    @Override
    public PropertyList getOutputProps() {
        return this.outputProps;
    }

    @Override
    public DataBindingMap getDataBindingMap() {
        return super.getDataBindingMap();
    }

    @Override
    public DataSet getProcessedDataSet() {
        return super.getProcessedDataSet();
    }

    @Override
    public void setProcessedDataSet(DataSet processedDataSet) {
        super.setProcessedDataSet(processedDataSet);
    }

    @Override
    public PropertyList getOutputProperties() {
        return this.outputProps;
    }
}

