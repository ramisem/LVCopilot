/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.tools.ant.Task
 */
package com.labvantage.sapphire.xml.ant;

import org.apache.tools.ant.Task;

public class DataTask
extends Task {
    private String data;
    private String separator;

    public void addText(String text) {
        this.data = text;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getSeparator() {
        return this.separator;
    }

    public String getData() {
        return this.getProject().replaceProperties(this.data);
    }
}

