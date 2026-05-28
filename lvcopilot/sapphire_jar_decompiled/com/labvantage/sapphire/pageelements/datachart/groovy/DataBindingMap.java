/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.groovy.AbstractBindingMap;
import java.io.Serializable;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class DataBindingMap
extends AbstractBindingMap
implements Serializable {
    public static final String ROW_NUMBER = "rownumber";
    public static final String DATASET = "dataset";
    private final PropertyList adapterOutputProperties = new PropertyList();
    private Integer rowNumber;
    private String dataSetProviderRSetId;

    public DataBindingMap(PropertyList argumentValueList, String connectionId) {
        super(argumentValueList, connectionId);
        this.putToBindingMap("adapteroutputprops", this.adapterOutputProperties);
        this.putToBindingMap("datasetprovideroutputprops", new PropertyList());
        this.rowNumber = 0;
    }

    public void setActiveRow(Integer rowNumber) {
        if (rowNumber == null || rowNumber.compareTo(0) < 0) {
            throw new IllegalArgumentException("Row number is null or negative: " + rowNumber);
        }
        this.rowNumber = rowNumber;
        this.putToBindingMap(ROW_NUMBER, rowNumber);
    }

    public void setActiveDataSet(DataSet dataSet) {
        this.putToBindingMap(DATASET, dataSet);
    }

    public void addAdapterOutputProperties(String adapterId, PropertyList adapterOutputProps) {
        if (adapterId == null) {
            throw new IllegalArgumentException("Adapter ID is null");
        }
        if (adapterOutputProps == null) {
            throw new IllegalArgumentException("Adapter output properties is null");
        }
        this.adapterOutputProperties.setProperty(adapterId, adapterOutputProps);
        this.putToBindingMap("adapteroutputprops", this.adapterOutputProperties);
    }

    public void setDataSetProviderOutputProperties(PropertyList dataSetProviderOutputProps) {
        if (dataSetProviderOutputProps == null) {
            throw new IllegalArgumentException("Data set provider output properties is null");
        }
        this.putToBindingMap("datasetprovideroutputprops", dataSetProviderOutputProps);
    }

    @Override
    public PropertyListCollection getTokenValues() {
        PropertyListCollection tokenValuesCollection = super.getTokenValues();
        PropertyList dataTokenValues = new PropertyList();
        dataTokenValues.setProperty(ROW_NUMBER, this.rowNumber.toString());
        tokenValuesCollection.add(dataTokenValues);
        return tokenValuesCollection;
    }

    public void setDataSetProviderRSetId(String dataSetProviderRSetId) {
        if (dataSetProviderRSetId == null) {
            throw new IllegalArgumentException("Data set provider RSet ID is null");
        }
        this.putToBindingMap("rsetid", dataSetProviderRSetId);
    }
}

