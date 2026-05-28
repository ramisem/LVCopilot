/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.adhocbrowser;

import java.io.Serializable;

public class AdhocArgument
implements Serializable {
    private String columnid = "";
    private String title = "";

    public String getColumnid() {
        return this.columnid;
    }

    public void setColumnid(String columnid) {
        this.columnid = columnid;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

