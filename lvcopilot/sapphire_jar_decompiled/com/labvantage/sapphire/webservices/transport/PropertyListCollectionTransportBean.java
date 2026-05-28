/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.webservices.transport;

import com.labvantage.sapphire.webservices.transport.PropertyListTransportBean;
import java.io.Serializable;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PropertyListCollectionTransportBean
implements Serializable {
    private PropertyListTransportBean[] propertyLists = new PropertyListTransportBean[0];
    private String id = "";

    public PropertyListCollectionTransportBean() {
    }

    public PropertyListCollectionTransportBean(PropertyListCollection propertyListCollection) {
        this.setPropertyListCollection(propertyListCollection);
    }

    public PropertyListCollection toPropertyListCollection() {
        return this.getPropertyListCollection();
    }

    public PropertyListTransportBean[] getPropertyLists() {
        return this.propertyLists;
    }

    public String getId() {
        return this.id;
    }

    public void setPropertyLists(PropertyListTransportBean[] propertyLists) {
        this.propertyLists = propertyLists;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected void setPropertyListCollection(PropertyListCollection propertyListCollection) {
        if (propertyListCollection != null) {
            this.setId(propertyListCollection.getId());
            this.propertyLists = new PropertyListTransportBean[propertyListCollection.size()];
            for (int i = 0; i < propertyListCollection.size(); ++i) {
                PropertyList pl = propertyListCollection.getPropertyList(i);
                this.propertyLists[i] = new PropertyListTransportBean();
                this.propertyLists[i].setPropertyList(pl);
            }
        }
    }

    protected PropertyListCollection getPropertyListCollection() {
        PropertyListCollection props = new PropertyListCollection();
        props.setId(this.id);
        for (int i = 0; i < this.propertyLists.length; ++i) {
            props.add(this.propertyLists[i].getPropertyList());
        }
        return props;
    }
}

