/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.jasper.DisplayProperties;
import java.io.Serializable;

public class ReportColumn
implements Serializable {
    private String thTitle;
    private String name;
    private String title;
    private Class javaClass;
    private boolean include = true;
    private boolean ascending = true;
    private int order;
    private int sortOrder;
    private byte aggregator;
    private DisplayProperties displayProperties = new DisplayProperties();
    private String formatterMethod;

    public boolean isInclude() {
        return this.include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public DisplayProperties getDisplayProperties() {
        return this.displayProperties;
    }

    public void setDisplayProperties(DisplayProperties displayProperties) {
        this.displayProperties = displayProperties;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class getJavaClass() {
        return this.javaClass;
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThTitle() {
        return this.thTitle;
    }

    public void setThTitle(String thTitle) {
        this.thTitle = thTitle;
    }

    public byte getAggregator() {
        return this.aggregator;
    }

    public void setAggregator(byte aggregator) {
        this.aggregator = aggregator;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isAscending() {
        return this.ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public String getFormatterMethod() {
        return this.formatterMethod;
    }

    public void setFormatterMethod(String formatterMethod) {
        this.formatterMethod = formatterMethod;
    }
}

