/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln;

public class DigitalSignatureRectangle {
    private String lowerLeftX;
    private String lowerLeftY;
    private String upperRightX;
    private String upperRightY;
    private String width;
    private String height;
    private String page;

    public DigitalSignatureRectangle(String lowerLeftX, String lowerLeftY, String upperRightX, String upperRightY, String width, String height, String page) {
        this.lowerLeftX = lowerLeftX;
        this.lowerLeftY = lowerLeftY;
        this.upperRightX = upperRightX;
        this.upperRightY = upperRightY;
        this.width = width;
        this.height = height;
        this.page = page;
    }

    public String getLowerLeftX() {
        return this.lowerLeftX;
    }

    public void setLowerLeftX(String lowerLeftX) {
        this.lowerLeftX = lowerLeftX;
    }

    public String getLowerLeftY() {
        return this.lowerLeftY;
    }

    public void setLowerLeftY(String lowerLeftY) {
        this.lowerLeftY = lowerLeftY;
    }

    public String getUpperRightX() {
        return this.upperRightX;
    }

    public void setUpperRightX(String upperRightX) {
        this.upperRightX = upperRightX;
    }

    public String getUpperRightY() {
        return this.upperRightY;
    }

    public void setUpperRightY(String upperRightY) {
        this.upperRightY = upperRightY;
    }

    public String getWidth() {
        return this.width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return this.height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getPage() {
        return this.page;
    }

    public void setPage(String page) {
        this.page = page;
    }
}

