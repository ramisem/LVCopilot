/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.servlet;

import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.io.File;
import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;

public class BaseContext
implements Serializable {
    protected transient PropertyList propertyList;

    public BaseContext(PropertyList propertyList) {
        this.propertyList = propertyList;
    }

    public PropertyList getPropertyList() {
        return this.propertyList;
    }

    public String getProperty(String propertyId) {
        return this.propertyList.getProperty(propertyId);
    }

    public PropertyList getPropertyList(String propertyid) {
        return this.propertyList != null ? this.propertyList.getPropertyList(propertyid) : null;
    }

    public String getEncodedProperty(String propertyid) {
        return HttpUtil.encodeURIComponent(this.getProperty(propertyid));
    }

    public String getDecodedProperty(String propertyid) {
        return HttpUtil.decodeURIComponent(this.getProperty(propertyid));
    }

    public boolean isProperty(String propertyid) {
        return this.propertyList.containsKey(propertyid);
    }

    public synchronized void setProperty(String propertyId, String value) {
        this.propertyList.setProperty(propertyId, value);
    }

    public synchronized void setProperty(String propertyId, PropertyList value) {
        this.propertyList.setProperty(propertyId, value);
    }

    public synchronized void setPropertyTree(String elementId, File file, String nodeId, PropertyDefinitionList propertyDefinitionList) throws SapphireException {
        if (elementId != null && elementId.length() > 0) {
            PropertyList dataBlock = new PropertyList(elementId);
            dataBlock.setPropertyTree(file, nodeId, propertyDefinitionList);
            this.propertyList.setProperty(elementId, dataBlock);
        } else {
            this.propertyList.setPropertyTree(file, nodeId, propertyDefinitionList);
        }
    }

    public synchronized void applySecurity(String rolelist, String modulelist) throws SapphireException {
        String xml = this.propertyList.toXMLString(rolelist, modulelist);
        this.propertyList.clear();
        this.propertyList.setPropertyList(xml);
    }
}

