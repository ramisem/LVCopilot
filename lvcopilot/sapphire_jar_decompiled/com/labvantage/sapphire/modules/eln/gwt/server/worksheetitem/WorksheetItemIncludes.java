/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import java.util.ArrayList;

public class WorksheetItemIncludes {
    private ArrayList<String> scriptIncludes = new ArrayList();
    private ArrayList<String> styleIncludes = new ArrayList();
    private String jsObjectName = "";

    public void addScriptInclude(String filename) {
        this.scriptIncludes.add(filename);
    }

    public ArrayList<String> getScriptIncludes() {
        return this.scriptIncludes;
    }

    public void addStyleInclude(String filename) {
        this.styleIncludes.add(filename);
    }

    public ArrayList<String> getStyleIncludes() {
        return this.styleIncludes;
    }

    public void setJSObjectName(String jsObjectName) {
        this.jsObjectName = jsObjectName;
    }

    public String getJSObjectName() {
        return this.jsObjectName;
    }
}

