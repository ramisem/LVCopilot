/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.genealogyviewer;

import java.util.HashMap;
import java.util.Map;

class BatchDisplayProperty {
    private String sdcid;
    private String displayExpression;
    private String colorCodeColumn;
    private String linkurl;
    private String detailpageurl = "";
    private Map<String, String> colorCodeMap;

    public BatchDisplayProperty(String sdcid, String displayExpression, String colorCodeColumn) {
        this.sdcid = sdcid;
        this.displayExpression = displayExpression;
        this.colorCodeColumn = colorCodeColumn;
        this.colorCodeMap = new HashMap<String, String>();
    }

    public String getSdcid() {
        return this.sdcid;
    }

    public String getDisplayExpression() {
        return this.displayExpression;
    }

    public String getColorCodeColumn() {
        return this.colorCodeColumn;
    }

    public void setColorCodeValue(String value, String color) {
        this.colorCodeMap.put(value, color);
    }

    public String getLinkurl() {
        return this.linkurl;
    }

    public void setLinkurl(String linkurl) {
        this.linkurl = linkurl;
    }

    public String getDetailpageurl() {
        return this.detailpageurl;
    }

    public void setDetailpageurl(String detailpageurl) {
        this.detailpageurl = detailpageurl;
    }

    public Map<String, String> getColorCodeMap() {
        return this.colorCodeMap;
    }
}

