/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

public class TableProperty {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";
    private String propertyid;
    private String type;

    public TableProperty(String propertyid, String type) {
        this.propertyid = propertyid;
        this.type = type;
    }

    public String getPropertyid() {
        return this.propertyid;
    }

    public void setPropertyid(String propertyid) {
        this.propertyid = propertyid;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

