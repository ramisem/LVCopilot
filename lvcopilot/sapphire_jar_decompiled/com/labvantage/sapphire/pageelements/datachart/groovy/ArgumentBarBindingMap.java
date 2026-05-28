/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.pageelements.datachart.groovy.AbstractBindingMap;
import java.io.Serializable;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public final class ArgumentBarBindingMap
extends AbstractBindingMap
implements Serializable {
    public static final String REQUEST_PARAMS = "requestparams";

    public ArgumentBarBindingMap(PropertyList argumentValueList, PropertyList requestParams, String connectionId) {
        this(argumentValueList, requestParams, new PropertyList(), connectionId);
    }

    public ArgumentBarBindingMap(PropertyList argumentValueList, PropertyList requestParams, PropertyList dataSetAdapterOutputProps, String connectionId) {
        super(argumentValueList, connectionId);
        if (requestParams == null) {
            throw new IllegalArgumentException("Request params is null");
        }
        this.putToBindingMap("datasetprovideroutputprops", dataSetAdapterOutputProps);
        this.putToBindingMap(REQUEST_PARAMS, requestParams);
    }

    @Override
    public PropertyListCollection getTokenValues() {
        PropertyListCollection tokenValuesCollection = super.getTokenValues();
        PropertyList requestParams = (PropertyList)this.getFromBindingMap(REQUEST_PARAMS);
        tokenValuesCollection.add(0, requestParams);
        return tokenValuesCollection;
    }
}

