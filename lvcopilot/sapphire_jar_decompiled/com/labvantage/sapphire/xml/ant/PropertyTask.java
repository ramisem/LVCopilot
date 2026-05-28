/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import com.labvantage.sapphire.xml.ant.CollectionTask;
import com.labvantage.sapphire.xml.ant.PropertyListTask;
import org.apache.tools.ant.Task;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class PropertyTask
extends Task {
    private String propertyid;
    private CollectionTask collection;
    private PropertyListTask propertyList;

    public String getPropertyid() {
        return this.propertyid;
    }

    public void setId(String propertyid) {
        this.propertyid = propertyid;
    }

    public void setPropertyid(String propertyid) {
        this.propertyid = propertyid;
    }

    public void addConfiguredCollection(CollectionTask collection) {
        this.collection = collection;
    }

    public void addConfiguredPropertyList(PropertyListTask propertyList) {
        this.propertyList = propertyList;
    }

    public PropertyListCollection getCollection() {
        return this.collection != null ? this.collection.getCollection() : null;
    }

    public PropertyList getPropertyList() {
        return this.propertyList != null ? this.propertyList.getPropertyListTransfer() : null;
    }
}

