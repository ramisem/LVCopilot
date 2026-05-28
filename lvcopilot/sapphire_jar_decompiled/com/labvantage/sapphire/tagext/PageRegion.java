/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.tagext;

import com.labvantage.sapphire.tagext.PageContent;
import java.util.HashMap;
import java.util.Map;

public class PageRegion {
    private String templateId;
    private Map pageContents = new HashMap();
    private PageRegion innerRegion;

    public PageRegion(String templateId) {
        this.templateId = templateId;
    }

    public void addPageContent(PageContent pageContent) {
        this.pageContents.put(pageContent.getName(), pageContent);
    }

    public PageContent getPageContent(String name) {
        return (PageContent)this.pageContents.get(name);
    }

    public Map getPageContents() {
        return this.pageContents;
    }

    public String getTemplateId() {
        return this.templateId;
    }

    public void setInnerRegion(PageRegion innerRegion) {
        this.innerRegion = innerRegion;
    }

    public PageRegion getInnerRegion() {
        return this.innerRegion;
    }
}

