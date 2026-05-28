/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import com.labvantage.sapphire.util.file.BaseFileDetails;

public class BasePagedFileDetails
extends BaseFileDetails {
    private int fromPage = 1;
    private int toPage = 1;
    private int totalPagesAvailable;
    private String htmlPageContainer = "";
    private boolean fixedLayout = false;

    public boolean isFixedLayout() {
        return this.fixedLayout;
    }

    public void setFixedLayout(boolean fixedLayout) {
        this.fixedLayout = fixedLayout;
    }

    public String getHtmlPageContainer() {
        return this.htmlPageContainer;
    }

    public void setHtmlPageContainer(String htmlPageContainer) {
        this.htmlPageContainer = htmlPageContainer;
    }

    public int getFromPage() {
        return this.fromPage;
    }

    public void setFromPage(int fromPage) {
        this.fromPage = fromPage;
    }

    public int getToPage() {
        return this.toPage;
    }

    public void setToPage(int toPage) {
        this.toPage = toPage;
    }

    public int getTotalPagesAvailable() {
        return this.totalPagesAvailable;
    }

    public void setTotalPagesAvailable(int totalPagesAvailable) {
        this.totalPagesAvailable = totalPagesAvailable;
    }
}

