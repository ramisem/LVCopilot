/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.SDIDataItemAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class SDIDataItemFilterConfiguration
implements Serializable {
    private final SDIDataItemAdapterConfiguration parent;
    private final StringExpression paramListId;
    private final StringExpression paramListVersionId;
    private final StringExpression variantId;
    private final StringExpression dataSet;
    private final StringExpression paramId;
    private final StringExpression paramType;
    private final StringExpression replicateId;
    private final StringExpression columnIdList;
    private final StringExpression columnValues;

    public SDIDataItemFilterConfiguration(PropertyList columnProps, SDIDataItemAdapterConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (columnProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.parent = parent;
        this.paramListId = new StringExpression(columnProps.getProperty("paramlistid", ""));
        this.paramListVersionId = new StringExpression(columnProps.getProperty("paramlistversionid", ""));
        this.variantId = new StringExpression(columnProps.getProperty("variantid", ""));
        this.dataSet = new StringExpression(columnProps.getProperty("dataset", ""));
        this.paramId = new StringExpression(columnProps.getProperty("paramid", ""));
        this.paramType = new StringExpression(columnProps.getProperty("paramtype", ""));
        this.replicateId = new StringExpression(columnProps.getProperty("replicateid", ""));
        this.columnIdList = new StringExpression(columnProps.getProperty("columnids", ""));
        this.columnValues = new StringExpression(columnProps.getProperty("columnvalues", ""));
    }

    public StringExpression getParamListId() {
        return this.paramListId;
    }

    public StringExpression getDataSet() {
        return this.dataSet;
    }

    public StringExpression getReplicateId() {
        return this.replicateId;
    }

    public StringExpression getParamListVersionId() {
        return this.paramListVersionId;
    }

    public StringExpression getVariantId() {
        return this.variantId;
    }

    public StringExpression getParamId() {
        return this.paramId;
    }

    public StringExpression getParamType() {
        return this.paramType;
    }

    public SDIDataItemAdapterConfiguration getParent() {
        return this.parent;
    }

    public StringExpression getColumnIdList() {
        return this.columnIdList;
    }

    public StringExpression getColumnValues() {
        return this.columnValues;
    }
}

