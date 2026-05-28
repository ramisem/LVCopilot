/*
 * Decompiled with CFR 0.152.
 */
package sapphire.xml;

import java.io.Serializable;
import java.util.HashMap;
import sapphire.xml.PropertyList;

public class PropertyValue
implements Serializable {
    private String id;
    private boolean isDefault;
    private PropertyList parentPropertyList;
    private String propertyTreeNodeId;
    private HashMap attributes;
    public String value = "";

    public PropertyValue(String id, boolean isDefault, PropertyList parentPropertyList) {
        this.id = id;
        this.parentPropertyList = parentPropertyList;
        this.isDefault = isDefault;
    }

    public String getId() {
        return this.id;
    }

    public PropertyList getParentPropertyList() {
        return this.parentPropertyList;
    }

    public void setPropertyTreeNodeId(String propertyTreeNodeId) {
        this.propertyTreeNodeId = propertyTreeNodeId;
    }

    public String getPropertyTreeNodeId() {
        return this.propertyTreeNodeId;
    }

    public void setAttributes(HashMap attributes) {
        this.attributes = attributes;
    }

    public HashMap getAttributes() {
        return this.attributes;
    }

    public String getAttribute(String attributeId) {
        Object o = null;
        if (this.attributes != null) {
            o = this.attributes.get(attributeId);
        }
        return o != null && o instanceof String ? (String)o : "";
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public String toString() {
        return this.value;
    }
}

