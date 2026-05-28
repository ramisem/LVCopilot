/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar;

import com.labvantage.sapphire.pageelements.datachart.element.conf.chart.toolbar.StandardOperationConfiguration;
import com.labvantage.sapphire.pageelements.datachart.groovy.StringExpression;
import java.io.Serializable;
import sapphire.xml.PropertyList;

public final class ShowSDINotesConfiguration
implements Serializable {
    private static final String DEFAULT_HIGHLIGHT_CONTEXT = "N";
    private final StandardOperationConfiguration parent;
    private final String sdcIdColumn;
    private final String keyId1Column;
    private final String keyId2Column;
    private final String keyId3Column;
    private final StringExpression context;
    private final boolean highlightContext;

    public ShowSDINotesConfiguration(PropertyList showSDINotesProps, StandardOperationConfiguration parent) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent is null");
        }
        if (showSDINotesProps == null) {
            throw new IllegalArgumentException("Source props is null");
        }
        this.sdcIdColumn = showSDINotesProps.getProperty("sdcidcolumn");
        this.keyId1Column = showSDINotesProps.getProperty("keyid1column");
        this.keyId2Column = showSDINotesProps.getProperty("keyid2column");
        this.keyId3Column = showSDINotesProps.getProperty("keyid3column");
        this.context = new StringExpression(showSDINotesProps.getProperty("context"));
        this.highlightContext = showSDINotesProps.getProperty("highlightcontext", DEFAULT_HIGHLIGHT_CONTEXT).toLowerCase().startsWith("y");
        this.parent = parent;
    }

    public ShowSDINotesConfiguration(ShowSDINotesConfiguration copy, StandardOperationConfiguration parent) {
        this.parent = parent;
        this.sdcIdColumn = copy.sdcIdColumn;
        this.keyId1Column = copy.keyId1Column;
        this.keyId2Column = copy.keyId2Column;
        this.keyId3Column = copy.keyId3Column;
        this.context = copy.context;
        this.highlightContext = copy.highlightContext;
    }

    public StandardOperationConfiguration getParent() {
        return this.parent;
    }

    public String getSdcIdColumn() {
        return this.sdcIdColumn;
    }

    public String getKeyId1Column() {
        return this.keyId1Column;
    }

    public String getKeyId2Column() {
        return this.keyId2Column;
    }

    public String getKeyId3Column() {
        return this.keyId3Column;
    }

    public StringExpression getContext() {
        return this.context;
    }

    public boolean isHighlightContext() {
        return this.highlightContext;
    }
}

