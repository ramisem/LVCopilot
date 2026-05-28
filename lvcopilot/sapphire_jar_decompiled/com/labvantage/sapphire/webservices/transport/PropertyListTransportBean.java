/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import com.labvantage.sapphire.webservices.transport.PropertyListCollectionTransportBean;
import com.labvantage.sapphire.webservices.transport.PropertyValueTransportBean;
import java.io.Serializable;
import java.util.Set;
import sapphire.xml.PropertyList;

public class PropertyListTransportBean
implements Serializable {
    private PropertyValueTransportBean[] propertyValues = new PropertyValueTransportBean[0];
    private String id = "";

    public PropertyListTransportBean() {
    }

    public PropertyListTransportBean(PropertyList propertyList) {
        this.setPropertyList(propertyList);
    }

    public PropertyList toPropertyList() {
        return this.getPropertyList();
    }

    public String getId() {
        return this.id;
    }

    public PropertyValueTransportBean[] getPropertyValues() {
        return this.propertyValues;
    }

    public void setPropertyValues(PropertyValueTransportBean[] values) {
        this.propertyValues = values;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected void setPropertyList(PropertyList propertyList) {
        if (propertyList != null) {
            this.setId(propertyList.getId());
            Set keyset = propertyList.keySet();
            this.propertyValues = new PropertyValueTransportBean[keyset.size()];
            int propIndex = 0;
            for (String propertyid : keyset) {
                Object value = propertyList.get(propertyid);
                this.propertyValues[propIndex] = new PropertyValueTransportBean();
                this.propertyValues[propIndex].setValue(propertyid, value);
                ++propIndex;
            }
        }
    }

    protected PropertyList getPropertyList() {
        PropertyList props = new PropertyList();
        props.setId(this.id);
        for (int i = 0; i < this.propertyValues.length; ++i) {
            Object value = this.propertyValues[i].getValue();
            if (value instanceof PropertyListCollectionTransportBean) {
                props.put(this.propertyValues[i].getId(), ((PropertyListCollectionTransportBean)value).getPropertyListCollection());
                continue;
            }
            if (value instanceof PropertyListTransportBean) {
                props.put(this.propertyValues[i].getId(), ((PropertyListTransportBean)value).getPropertyList());
                continue;
            }
            props.put(this.propertyValues[i].getId(), value.toString());
        }
        return props;
    }
}

