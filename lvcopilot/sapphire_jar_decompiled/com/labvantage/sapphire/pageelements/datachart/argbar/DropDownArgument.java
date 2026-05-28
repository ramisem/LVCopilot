/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.argbar;

import com.labvantage.sapphire.pageelements.datachart.argbar.AbstractArgument;
import com.labvantage.sapphire.pageelements.datachart.argbar.ArgumentValue;
import com.labvantage.sapphire.pageelements.datachart.element.conf.argbar.DropDownArgumentConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.ArgumentBarBindingMap;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class DropDownArgument
extends AbstractArgument
implements Serializable {
    private final DropDownArgumentConfiguration dropDownArgumentConf;
    private List<String> optionValueList;
    private List<String> optionDisplayValueList;
    private boolean initDone = false;

    public DropDownArgument(DropDownArgumentConfiguration dropDownArgumentConf, String connectionId, PropertyList requestParams, PropertyList argumentValueList, String requestId) throws SapphireException {
        super(connectionId, dropDownArgumentConf.getParent(), new ArgumentBarBindingMap(argumentValueList, requestParams, connectionId), argumentValueList, requestId);
        this.dropDownArgumentConf = dropDownArgumentConf;
        this.populateValueList(dropDownArgumentConf, new ArgumentBarBindingMap(argumentValueList, requestParams, connectionId));
        if (!dropDownArgumentConf.addEmptyValue() && !this.optionValueList.isEmpty()) {
            ArgumentValue argumentValue = this.getArgumentValue();
            ArrayList<String> valueList = new ArrayList<String>();
            ArrayList<String> displayValueList = new ArrayList<String>();
            valueList.add(this.optionValueList.get(0));
            displayValueList.add(this.optionDisplayValueList.get(0));
            argumentValue.setValueList(valueList, displayValueList, requestId);
        }
        this.initDone = true;
    }

    private void populateValueList(DropDownArgumentConfiguration dropDownArgumentConf, ArgumentBarBindingMap bindingMap) throws SapphireException {
        DropDownArgumentConfiguration.DropDownArgumentType dropDownArgumentType;
        this.optionValueList = new ArrayList<String>();
        this.optionDisplayValueList = new ArrayList<String>();
        if (dropDownArgumentConf.addEmptyValue()) {
            this.optionValueList.add("");
            this.optionDisplayValueList.add("");
        }
        if ((dropDownArgumentType = dropDownArgumentConf.getDropDownArgumentType()) == DropDownArgumentConfiguration.DropDownArgumentType.VALUE_LIST) {
            StringExpression valueListConf1 = dropDownArgumentConf.getValueListConf();
            if (valueListConf1.getExpression().startsWith("$G{")) {
                this.setCascadedArgument(true);
            }
            String valueListConf = valueListConf1.evaluate(bindingMap);
            this.addValueListValues(valueListConf.split(";"));
        } else if (dropDownArgumentType == DropDownArgumentConfiguration.DropDownArgumentType.REFERENCE_TYPE) {
            this.addRefTypeValues(dropDownArgumentConf.getRefTypeId());
        } else if (dropDownArgumentType == DropDownArgumentConfiguration.DropDownArgumentType.SQL) {
            String expression;
            String sql = dropDownArgumentConf.getSql().evaluate(bindingMap);
            if (!sql.equals(expression = dropDownArgumentConf.getSql().getExpression())) {
                this.setCascadedArgument(true);
                this.setCascadedArgumentInputs(dropDownArgumentConf.getSql().findUsedTokens(bindingMap));
            }
            this.addSqlValues(sql);
        } else if (dropDownArgumentType == DropDownArgumentConfiguration.DropDownArgumentType.SDC) {
            this.addSdcValues(dropDownArgumentConf.getSdcId(), dropDownArgumentConf.getExtendWhere(), dropDownArgumentConf.getSDCValueColumn(), dropDownArgumentConf.getSDCDisplayValueColumn());
        } else {
            throw new IllegalArgumentException("Unknown drop-down type");
        }
    }

    private void addValueListValues(String[] arrValues) {
        for (String stringValue : arrValues) {
            String value;
            String[] valueAndDisplayValue = stringValue.split("\\|");
            String displayValue = value = valueAndDisplayValue[0];
            if (valueAndDisplayValue.length > 1) {
                displayValue = valueAndDisplayValue[1];
            }
            this.addValue(value, displayValue);
        }
    }

    private void addRefTypeValues(String refTypeId) {
        if (refTypeId.equals("")) {
            throw new IllegalArgumentException("Argument Reference type is not defined.");
        }
        DataSet ds = this.getQueryProcessor().getRefTypeDataSet(refTypeId);
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String value = ds.getValue(i, "refvalueid", "");
            String displayValue = ds.getValue(i, "refdisplayvalue", value);
            this.addValue(value, displayValue);
        }
    }

    private void addSdcValues(String sdcId, String extendedWhere, String sdcValueColumn, String sdcDisplayValueColumn) {
        SDIRequest r = new SDIRequest();
        r.setSDCid(sdcId);
        if (!extendedWhere.equals("")) {
            r.setQueryWhere(extendedWhere);
        }
        r.setRequestItem("primary");
        r.setRetainRsetid(false);
        SDIData sdi = this.getSDIProcessor().getSDIData(r);
        DataSet ds = sdi.getDataset("primary");
        if (sdcDisplayValueColumn.equals("")) {
            sdcDisplayValueColumn = sdcValueColumn;
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String value = ds.getValue(i, sdcValueColumn, "");
            String displayValue = ds.getValue(i, sdcDisplayValueColumn, value);
            this.addValue(value, displayValue);
        }
    }

    private void addSqlValues(String sql) {
        if (sql.isEmpty()) {
            throw new IllegalArgumentException("Argument SQL is not defined.");
        }
        if (!sql.trim().toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Only select clause allowed");
        }
        DataSet ds = this.getQueryProcessor().getSqlDataSet(sql);
        if (ds == null) {
            throw new IllegalArgumentException("Argument SQL is invalid. " + sql);
        }
        String valueColumnId = ds.getColumnId(0);
        String displayValueColumnId = "";
        if (ds.getColumnCount() > 1) {
            displayValueColumnId = ds.getColumnId(1);
        }
        for (int i = 0; i < ds.getRowCount(); ++i) {
            String value = ds.getValue(i, valueColumnId, "");
            String displayValue = ds.getValue(i, displayValueColumnId, value);
            this.addValue(value, displayValue);
        }
    }

    private void addValue(String value, String displayValue) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null.");
        }
        if (displayValue == null) {
            throw new IllegalArgumentException("Display value is null.");
        }
        if (this.dropDownArgumentConf.getTranslateDisplayValue()) {
            displayValue = this.getTranslationProcessor().translate(displayValue);
        }
        this.optionValueList.add(value);
        this.optionDisplayValueList.add(displayValue);
    }

    @Override
    public PropertyList getProps(ArgumentBarBindingMap bindingMap, String requestId) throws SapphireException {
        PropertyList argumentProps = super.getProps(bindingMap, requestId);
        PropertyList dropDownArgumentProps = new PropertyList();
        dropDownArgumentProps.setProperty("argumentvalue", this.getArgumentValue().getProps(bindingMap, requestId));
        DropDownArgumentConfiguration.DropDownArgumentType dropDownArgumentType = this.dropDownArgumentConf.getDropDownArgumentType();
        dropDownArgumentProps.setProperty("dropdowntype", dropDownArgumentType.getName());
        dropDownArgumentProps.setProperty("multiselect", this.dropDownArgumentConf.isMultiSelect() ? "Y" : "N");
        dropDownArgumentProps.setProperty("addemptyvalue", this.dropDownArgumentConf.addEmptyValue() ? "Y" : "N");
        if (dropDownArgumentType == DropDownArgumentConfiguration.DropDownArgumentType.SDC) {
            dropDownArgumentProps.setProperty("sdcid", this.dropDownArgumentConf.getSdcId());
            dropDownArgumentProps.setProperty("extendedwhere", this.dropDownArgumentConf.getExtendWhere());
            dropDownArgumentProps.setProperty("sdcvaluecolumn", this.dropDownArgumentConf.getSDCValueColumn());
            dropDownArgumentProps.setProperty("sdcdisplayvaluecolumn", this.dropDownArgumentConf.getSDCDisplayValueColumn());
        } else if (dropDownArgumentType == DropDownArgumentConfiguration.DropDownArgumentType.REFERENCE_TYPE) {
            dropDownArgumentProps.setProperty("reftypeid", this.dropDownArgumentConf.getRefTypeId());
        } else if (dropDownArgumentType == DropDownArgumentConfiguration.DropDownArgumentType.VALUE_LIST) {
            dropDownArgumentProps.setProperty("valuelistconf", this.dropDownArgumentConf.getValueListConf().getExpression());
        } else if (dropDownArgumentType == DropDownArgumentConfiguration.DropDownArgumentType.SQL) {
            dropDownArgumentProps.setProperty("dropdownsql", this.dropDownArgumentConf.getSql().getExpression());
        } else {
            throw new IllegalArgumentException("Unknown drop-down argument type: " + (Object)((Object)dropDownArgumentType));
        }
        this.populateValueList(this.dropDownArgumentConf, bindingMap);
        PropertyListCollection valueListCollection = new PropertyListCollection();
        for (int i = 0; i < this.optionValueList.size(); ++i) {
            String value = this.optionValueList.get(i);
            String displayValue = this.optionDisplayValueList.get(i);
            PropertyList valueProps = new PropertyList();
            valueProps.setProperty("value", value);
            valueProps.setProperty("displayvalue", displayValue);
            valueListCollection.add(valueProps);
        }
        dropDownArgumentProps.setProperty("valuelist", valueListCollection);
        dropDownArgumentProps.setProperty("translate", this.dropDownArgumentConf.getTranslateDisplayValue() ? "Y" : "N");
        argumentProps.setProperty("dropdownargumentprops", dropDownArgumentProps);
        return argumentProps;
    }

    @Override
    public List<String> createValueList(List<String> displayValueList) {
        ArrayList<String> valueList = new ArrayList<String>();
        for (String displayValue : displayValueList) {
            for (int i = 0; i < this.optionDisplayValueList.size(); ++i) {
                if (!this.optionDisplayValueList.get(i).equals(displayValue)) continue;
                valueList.add(this.optionValueList.get(i));
            }
        }
        return valueList;
    }

    @Override
    public void setArgumentValue(String valueString, String requestId) {
        if (valueString == null) {
            throw new IllegalArgumentException("Value is null");
        }
        String valueSeparator = this.getArgumentConfiguration().getArgumentValueConfiguration().getValueSeparator();
        List<String> valueList = Arrays.asList(valueString.split(valueSeparator));
        ArrayList<String> displayValueList = new ArrayList<String>();
        for (String value : valueList) {
            for (int i = 0; i < this.optionValueList.size(); ++i) {
                String optionValue = this.optionValueList.get(i);
                if (!optionValue.equals(value)) continue;
                String optionDisplayValue = this.optionDisplayValueList.get(i);
                displayValueList.add(optionDisplayValue);
            }
        }
        this.getArgumentValue().setValueList(valueList, displayValueList, requestId);
    }

    @Override
    public void populateArgumentValueList(PropertyList argumentValueList, ArgumentBarBindingMap argumentBarBindingMap, String requestId) throws SapphireException {
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID is null");
        }
        List<String> valueList = this.getArgumentValue().getValues(requestId);
        if (this.initDone && !this.dropDownArgumentConf.addEmptyValue()) {
            this.populateValueList(this.dropDownArgumentConf, argumentBarBindingMap);
            if (!this.optionValueList.isEmpty() && (valueList == null || valueList.isEmpty())) {
                this.setArgumentValue(this.optionValueList.get(0), requestId);
            }
        }
        super.populateArgumentValueList(argumentValueList, argumentBarBindingMap, requestId);
    }
}

