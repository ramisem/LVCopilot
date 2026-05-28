/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import com.labvantage.sapphire.webservices.transport.PropertyListCollectionTransportBean;
import com.labvantage.sapphire.webservices.transport.PropertyListTransportBean;
import java.io.Serializable;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PropertyValueTransportBean
implements Serializable {
    private PropertyListCollectionTransportBean propertyListCollection = null;
    private PropertyListTransportBean propertyList = null;
    private String propertyValue = "";
    private String id;
    private int type = 0;

    public PropertyValueTransportBean() {
        this.id = "";
    }

    public PropertyValueTransportBean(String id, Object value) {
        id = "";
        this.setValue(id, value);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPropertyValue() {
        return this.propertyValue;
    }

    public PropertyListTransportBean getPropertyListValue() {
        return this.propertyList;
    }

    public PropertyListCollectionTransportBean getPropertyListCollectionValue() {
        return this.propertyListCollection;
    }

    public String getPropertyId() {
        return this.id;
    }

    public void setPropertyValue(String value) {
        this.propertyValue = value;
    }

    public void setPropertyListValue(PropertyListTransportBean value) {
        this.propertyList = value;
    }

    public void setPropertyListCollectionValue(PropertyListCollectionTransportBean value) {
        this.propertyListCollection = value;
    }

    public void setPropertyId(String id) {
        this.id = id;
    }

    protected void setValue(String id, Object value) {
        this.setPropertyId(id);
        if (value instanceof PropertyList) {
            this.type = 1;
            this.propertyList = new PropertyListTransportBean();
            this.propertyList.setPropertyList((PropertyList)value);
        } else if (value instanceof PropertyListCollection) {
            this.type = 2;
            this.propertyListCollection = new PropertyListCollectionTransportBean();
            this.propertyListCollection.setPropertyListCollection((PropertyListCollection)value);
        } else {
            this.type = 0;
            this.propertyValue = value.toString();
        }
    }

    protected Object getValue() {
        switch (this.type) {
            case 0: {
                return this.propertyValue;
            }
            case 1: {
                return this.propertyList;
            }
            case 2: {
                return this.propertyListCollection;
            }
        }
        return null;
    }
}

