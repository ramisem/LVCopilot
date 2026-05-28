/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter;

import com.labvantage.sapphire.pageelements.datachart.element.conf.data.adapter.StandardAdapterConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public class DataItemParamListKeyAdapterConfiguration
implements Serializable {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private static final String DEFAULT_KEY_ID1_COLUMN = "keyid1";
    private static final String DEFAULT_KEY_ID2_COLUMN = "";
    private static final String DEFAULT_KEY_ID3_COLUMN = "";
    private static final String DEFAULT_OUTPUT_PROPERTY = "paramlistitemkey";
    private final StringExpression rsetId;
    private final StringExpression columnDefinitionValue;
    private final StringExpression displayDefinitionValue;
    private final String outputproperty;
    private final String sdcId;
    private final String keyId1Column;
    private final String keyId2Column;
    private final String keyId3Column;
    private final StandardAdapterConfiguration parent;

    public DataItemParamListKeyAdapterConfiguration(PropertyList dataItemParamListKeyAdapterConf, StandardAdapterConfiguration parent) {
        if (dataItemParamListKeyAdapterConf == null) {
            throw new IllegalArgumentException("Source properties is null");
        }
        if (parent == null) {
            throw new IllegalArgumentException("Parent configuration is null");
        }
        this.parent = parent;
        this.sdcId = dataItemParamListKeyAdapterConf.getProperty("sdcid");
        this.keyId1Column = dataItemParamListKeyAdapterConf.getProperty("keyid1column", DEFAULT_KEY_ID1_COLUMN);
        this.keyId2Column = dataItemParamListKeyAdapterConf.getProperty("keyid2column", "");
        this.keyId3Column = dataItemParamListKeyAdapterConf.getProperty("keyid3column", "");
        this.rsetId = new StringExpression(dataItemParamListKeyAdapterConf.getProperty("rsetid", ""));
        this.columnDefinitionValue = new StringExpression(dataItemParamListKeyAdapterConf.getProperty("columndefinition", ""));
        this.displayDefinitionValue = new StringExpression(dataItemParamListKeyAdapterConf.getProperty("displayvaluedefinition", ""));
        this.outputproperty = dataItemParamListKeyAdapterConf.getProperty("outputproperty", DEFAULT_OUTPUT_PROPERTY);
    }

    public StandardAdapterConfiguration getParent() {
        return this.parent;
    }

    public StringExpression getRsetId() {
        return this.rsetId;
    }

    public StringExpression getColumnDefinitionValue() {
        return this.columnDefinitionValue;
    }

    public StringExpression getDisplayDefinitionValue() {
        return this.displayDefinitionValue;
    }

    public String getSdcId() {
        return this.sdcId;
    }

    public String getKeyId1Column() {
        return this.keyId1Column;
    }

    public String getKeyId2Column() {
        return this.keyId2Column;
    }

    public String getKeyId3Column() {
        return this.keyId3Column;
    }

    public String getOutputproperty() {
        return this.outputproperty;
    }
}

