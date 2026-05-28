/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.groovy;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.pageelements.datachart.groovy.BindingMap;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.ConnectionInfo;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public abstract class AbstractBindingMap
extends BaseCustom
implements BindingMap,
Serializable {
    public static final String ARGUMENTS = "arguments";
    public static final String ARGUMENT_VALUES = "argumentvalues";
    public static final String SAFE_ARGUMENT_VALUES = "safeargumentvalues";
    public static final String TRANSLATION_PROCESSOR_SHORT = "tp";
    public static final String TRANSLATION_PROCESSOR_LONG = "translationprocessor";
    public static final String ADAPTER_OUTPUT_PROPERTIES = "adapteroutputprops";
    public static final String DATA_SET_PROVIDER_OUTPUT_PROPERTIES = "datasetprovideroutputprops";
    public static final String DATA_SET_PROVIDER_RSET_ID = "rsetid";
    public static final String USER = "user";
    public static final String CONNECTION_INFO = "connectioninfo";
    private final HashMap<String, Object> extraBindingMap;
    private final PropertyList argumentValueList;
    private final PropertyList argumentValues;
    private final ConnectionInfo connectionInfo;
    private final HashMap userAttributeMap;

    public AbstractBindingMap(PropertyList argumentValueList, String connectionId) {
        if (argumentValueList == null) {
            throw new IllegalArgumentException("Argument value list is null");
        }
        if (connectionId == null || connectionId.isEmpty()) {
            throw new IllegalArgumentException("Connection ID is null or empty: " + connectionId);
        }
        this.setConnectionId(connectionId);
        this.argumentValues = new PropertyList();
        Set keys = argumentValueList.keySet();
        for (Object key : keys) {
            String argumentId = (String)key;
            String argumentValue = "";
            PropertyList argumentProps = argumentValueList.getPropertyListNotNull(argumentId);
            String valueSeparator = argumentProps.getProperty("valueseparator");
            StringBuilder values = new StringBuilder();
            PropertyListCollection valueCollection = argumentProps.getCollectionNotNull("valuecollection");
            for (int i = 0; i < valueCollection.size(); ++i) {
                PropertyList valueProps = valueCollection.getPropertyList(i);
                String value = valueProps.getProperty("value");
                values.append(valueSeparator).append(value);
            }
            if (values.length() >= valueSeparator.length()) {
                argumentValue = values.substring(valueSeparator.length());
            }
            this.argumentValues.setProperty(argumentId, argumentValue);
        }
        this.argumentValueList = argumentValueList;
        this.extraBindingMap = new HashMap();
        this.connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        this.userAttributeMap = this.connectionInfo.getUserAttributeMap();
    }

    protected Object putToBindingMap(String key, Object value) {
        return this.extraBindingMap.put(key, value);
    }

    protected Object getFromBindingMap(String key) {
        return this.extraBindingMap.get(key);
    }

    @Override
    public HashMap<String, Object> toHashMap() {
        TranslationProcessor translationProcessor = this.getTranslationProcessor();
        HashMap<String, Object> bindingMap = new HashMap<String, Object>();
        bindingMap.put(ARGUMENTS, this.argumentValueList);
        bindingMap.put(ARGUMENT_VALUES, this.argumentValues);
        bindingMap.put(SAFE_ARGUMENT_VALUES, this.getSafeArguments(this.argumentValues));
        bindingMap.put(TRANSLATION_PROCESSOR_SHORT, translationProcessor);
        bindingMap.put(TRANSLATION_PROCESSOR_LONG, translationProcessor);
        bindingMap.put(USER, this.userAttributeMap);
        bindingMap.put(CONNECTION_INFO, this.connectionInfo);
        bindingMap.putAll(this.extraBindingMap);
        return bindingMap;
    }

    private Object getSafeArguments(PropertyList argumentValues) {
        PropertyList safeArguments = new PropertyList(argumentValues);
        boolean isOra = this.getConnectionProcessor().isOra();
        for (String key : argumentValues.keySet()) {
            Object value = argumentValues.get(key);
            if (!(value instanceof String)) continue;
            safeArguments.put(key, SafeSQL.encodeForSQL((String)value, isOra));
        }
        return safeArguments;
    }

    @Override
    public PropertyListCollection getTokenValues() {
        PropertyListCollection tokenValuesCollection = new PropertyListCollection();
        PropertyList argumentValueListValues = new PropertyList();
        Set keySet = this.argumentValueList.keySet();
        for (Object key : keySet) {
            String propertyId = (String)key;
            PropertyList argumentValueProps = this.argumentValueList.getPropertyList(propertyId);
            if (argumentValueProps == null) continue;
            PropertyListCollection valueCollection = argumentValueProps.getCollectionNotNull("valuecollection");
            String valueSeparator = argumentValueProps.getProperty("valueseparator");
            StringBuilder value = new StringBuilder();
            for (int i = 0; i < valueCollection.size(); ++i) {
                PropertyList valueProps = valueCollection.getPropertyList(i);
                value.append(valueSeparator).append(valueProps.getProperty("value"));
            }
            if (value.length() <= valueSeparator.length()) continue;
            argumentValueListValues.setProperty(propertyId, value.substring(valueSeparator.length()));
        }
        tokenValuesCollection.add(argumentValueListValues);
        return tokenValuesCollection;
    }

    protected PropertyList getArgumentValueList() {
        return this.argumentValueList;
    }
}

