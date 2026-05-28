/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.admin.webadmin.configreport;

import com.labvantage.sapphire.admin.webadmin.configreport.ConfigReportRequestHandler;

public abstract class BaseConfigReport {
    protected String folderLocation;
    protected String fileName;
    protected String imageRoot;
    protected String reportTitle;
    protected String reportStyles;

    public void initialize(String folderLocation, String fileName, String imageRoot, String reportTitle, String reportStyles) {
        this.folderLocation = folderLocation;
        this.fileName = fileName;
        this.imageRoot = imageRoot;
        this.reportTitle = reportTitle;
        this.reportStyles = reportStyles;
    }

    public boolean wantsButtons() {
        return true;
    }

    public boolean wantsListColumns() {
        return true;
    }

    public boolean wantsMaintColumns() {
        return true;
    }

    public void beginReport() throws Exception {
    }

    public void nextWebPage(ConfigReportRequestHandler.WebPage webpage, boolean includeDetails) {
    }

    public void beginButtons(boolean hasButtons) {
    }

    public void nextButton(ConfigReportRequestHandler.Button button) {
    }

    public void endButtons(boolean hasButtons) {
    }

    public void beginListColumns(boolean hasColumns) {
    }

    public void nextListColumn(ConfigReportRequestHandler.ListColumn column) {
    }

    public void endListColumns(boolean hasColumns) {
    }

    public void beginMaintColumns(boolean hasColumns) {
    }

    public void nextMaintColumn(ConfigReportRequestHandler.MaintColumn column) {
    }

    public void endMaintColumns(boolean hasColumns) {
    }

    public void endReport() {
    }

    public String getFinalOutput() {
        return "No content";
    }

    public String getFileExtension() {
        return "txt";
    }
}

