/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.tagext;

public class PageContent {
    private String _name;
    private String _content;
    private boolean _isDirect;

    public PageContent(String name, String content, boolean isDirect) {
        this._name = name;
        this._content = content;
        this._isDirect = isDirect;
    }

    public String getName() {
        return this._name;
    }

    public String getContent() {
        return this._content;
    }

    public boolean isDirect() {
        return this._isDirect;
    }
}

