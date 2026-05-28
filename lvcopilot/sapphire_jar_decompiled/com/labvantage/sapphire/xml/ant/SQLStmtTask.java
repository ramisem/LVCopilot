/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import org.apache.tools.ant.Task;

public class SQLStmtTask
extends Task {
    private String sql;

    public void addText(String text) {
        this.sql = text;
    }

    public String getSQL() {
        return this.getProject().replaceProperties(this.sql);
    }
}

