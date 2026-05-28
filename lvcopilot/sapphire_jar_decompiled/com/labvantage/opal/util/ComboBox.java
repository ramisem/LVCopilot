/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import javax.servlet.jsp.PageContext;

public class ComboBox {
    private String id;
    private String optionselector;
    private boolean editable;
    private String[] options;
    private PageContext pageContext;

    ComboBox(String id, PageContext pageContext) {
        this.id = id;
        this.pageContext = pageContext;
    }

    ComboBox(String id, PageContext pageContext, boolean editable) {
        this.id = id;
        this.pageContext = pageContext;
        this.editable = editable;
    }

    String getHtml() {
        StringBuffer sb = new StringBuffer();
        return sb.toString();
    }

    void addOption(String option) {
        this.options[this.options.length] = option;
    }

    void setOptionselector(String optionselector) {
        this.optionselector = optionselector;
    }

    String getOptionselector() {
        return this.optionselector;
    }

    private String getScripts() {
        StringBuffer sb = new StringBuffer();
        return sb.toString();
    }
}

