/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.search;

public class SearchRequest {
    private String enteredQuery = "";
    private String searchQuery;
    private int searchLimit = -1;
    private String sdcid = "";
    private boolean showSDI = true;
    private boolean showAttachments = true;
    private boolean showNotes = true;
    private boolean showTemplates = true;

    public SearchRequest(String enteredQuery) {
        this.enteredQuery = enteredQuery;
    }

    public String getEnteredQuery() {
        return this.enteredQuery;
    }

    public int getSearchLimit() {
        return this.searchLimit;
    }

    public void setSearchLimit(int searchLimit) {
        this.searchLimit = searchLimit;
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public void setSdcid(String sdcid) {
        this.sdcid = sdcid;
    }

    public boolean isShowSDI() {
        return this.showSDI;
    }

    public void setShowSDI(boolean showSDI) {
        this.showSDI = showSDI;
    }

    public boolean isShowAttachments() {
        return this.showAttachments;
    }

    public void setShowAttachments(boolean showAttachments) {
        this.showAttachments = showAttachments;
    }

    public boolean isShowNotes() {
        return this.showNotes;
    }

    public void setShowNotes(boolean showNotes) {
        this.showNotes = showNotes;
    }

    public boolean isShowTemplates() {
        return this.showTemplates;
    }

    public void setShowTemplates(boolean showTemplates) {
        this.showTemplates = showTemplates;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchQuery() {
        return this.searchQuery;
    }
}

