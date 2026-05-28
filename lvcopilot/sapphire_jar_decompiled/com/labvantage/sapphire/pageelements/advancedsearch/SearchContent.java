/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.advancedsearch;

import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.Browser;
import sapphire.xml.PropertyList;

public abstract class SearchContent {
    protected PropertyList contentProperties;
    protected PageContext pageContext;
    protected String sdcid;
    protected String cookieKey;
    protected PropertyList element;
    protected boolean isLastSearchType;
    protected String contentName;
    TranslationProcessor translator = null;
    protected int maxHeight = 0;
    Browser browser;

    protected SearchContent() {
    }

    public void init(PageContext pageContext, boolean isLastSearchType, PropertyList contentProperties, String sdcid, PropertyList elementProperties, String cookieKey) {
        this.init(pageContext, isLastSearchType, contentProperties, sdcid, elementProperties, cookieKey, 0, new TranslationProcessor(pageContext));
    }

    public void init(PageContext pageContext, boolean isLastSearchType, PropertyList contentProperties, String sdcid, PropertyList elementProperties, String cookieKey, int maxHeight, TranslationProcessor tp) {
        this.contentProperties = contentProperties;
        this.pageContext = pageContext;
        this.sdcid = sdcid;
        this.element = elementProperties;
        this.isLastSearchType = isLastSearchType;
        this.cookieKey = cookieKey;
        this.maxHeight = maxHeight;
        this.translator = tp;
        this.browser = new Browser(pageContext);
    }

    public abstract String getHtml();
}

