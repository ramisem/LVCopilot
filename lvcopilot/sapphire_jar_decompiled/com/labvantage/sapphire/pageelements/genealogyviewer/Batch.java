/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.genealogyviewer;

import com.labvantage.sapphire.pageelements.genealogyviewer.BatchDisplayProperty;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import sapphire.util.DataSet;

class Batch
implements Serializable,
Comparable<Batch> {
    private final String sdcid;
    private final String keyid;
    private List<Batch> children = new ArrayList<Batch>();
    private List<Batch> parents = new ArrayList<Batch>();
    private int x;
    private int y;
    private boolean hasMoreChild;
    private boolean hasMoreParent;
    private boolean isShifted = false;
    private String displayValue;
    private Color batchBackgroundColor = Color.WHITE;
    private DataSet dataset;
    private BatchDisplayProperty batchDisplayProperty;
    private String hrefURL = "";
    private Set<String> parentKeySet;
    private Set<String> childKeySet;
    private int parentLevel;
    private int childLevel;
    private String detailPageURL;

    public Batch(String sdcid, String keyid) {
        this.sdcid = sdcid;
        this.keyid = keyid;
        this.displayValue = keyid;
        this.parentKeySet = new HashSet<String>();
        this.childKeySet = new HashSet<String>();
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public BatchDisplayProperty getBatchDisplayProperty() {
        return this.batchDisplayProperty;
    }

    public void setBatchDisplayProperty(BatchDisplayProperty batchDisplayProperty) {
        this.batchDisplayProperty = batchDisplayProperty;
    }

    public boolean isShifted() {
        return this.isShifted;
    }

    public void setShifted(boolean shifted) {
        this.isShifted = shifted;
    }

    public boolean isHasMoreChild() {
        return this.hasMoreChild;
    }

    public void setHasMoreChild(boolean hasMoreChild) {
        this.hasMoreChild = hasMoreChild;
    }

    public boolean isHasMoreParent() {
        return this.hasMoreParent;
    }

    public void setHasMoreParent(boolean hasMoreParent) {
        this.hasMoreParent = hasMoreParent;
    }

    public String getKeyId() {
        return this.keyid;
    }

    public void addChild(Batch child) {
        if (child != null && this.childKeySet.add(child.getKeyId())) {
            this.children.add(child);
        }
    }

    public void addParent(Batch parent) {
        if (parent != null && this.parentKeySet.add(parent.getKeyId())) {
            this.parents.add(parent);
        }
    }

    public List<Batch> getChildren() {
        return this.children;
    }

    public List<Batch> getParents() {
        return this.parents;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public String getDisplayValue() {
        return this.displayValue;
    }

    public void setDisplayValue(String displayValue) {
        this.displayValue = displayValue;
    }

    public Color getBatchBackgroundColor() {
        return this.batchBackgroundColor;
    }

    public void setBatchBackgroundColor(Color batchBackgroundColor) {
        this.batchBackgroundColor = batchBackgroundColor;
    }

    public void setDataSet(DataSet dataset) {
        this.dataset = dataset;
    }

    public DataSet getDataSet() {
        return this.dataset == null ? new DataSet() : this.dataset;
    }

    public String getValue(String columnid) {
        return this.getDataSet().getValue(0, columnid, "");
    }

    @Override
    public int compareTo(Batch o) {
        return this.getKeyId().compareTo(o.getKeyId());
    }

    public void setHrefURL(String hrefURL) {
        this.hrefURL = hrefURL;
    }

    public String getHrefURL() {
        return this.hrefURL;
    }

    public void setParentLevel(int parentLevel) {
        this.parentLevel = parentLevel;
    }

    public void setChildLevel(int childLevel) {
        this.childLevel = childLevel;
    }

    public int getParentLevel() {
        return this.parentLevel;
    }

    public int getChildLevel() {
        return this.childLevel;
    }

    public String getDetailPageURL() {
        return this.detailPageURL;
    }

    public void setDetailPageURL(String detailPageURL) {
        this.detailPageURL = detailPageURL;
    }
}

