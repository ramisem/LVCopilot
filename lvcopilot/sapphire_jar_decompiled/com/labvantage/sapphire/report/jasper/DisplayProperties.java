/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.report.jasper;

import com.labvantage.sapphire.report.jasper.DisplayConstants;
import java.io.Serializable;

public class DisplayProperties
implements Serializable,
Cloneable {
    private String color;
    private String fontFace;
    private int width;
    private int height;
    private int fontSize;
    private byte align;
    private byte decoration;

    public byte getAlign() {
        return this.align;
    }

    public void setAlign(byte align) {
        this.align = align;
    }

    public String getFontFace() {
        return this.fontFace;
    }

    public void setFontFace(String fontFace) {
        this.fontFace = fontFace;
    }

    public int getFontSize() {
        return this.fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getColor() {
        return this.color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public byte getDecoration() {
        return this.decoration;
    }

    public void setDecoration(byte decoration) {
        this.decoration = decoration;
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public static DisplayProperties createDefault() {
        DisplayProperties temp = new DisplayProperties();
        temp.setAlign(DisplayConstants.ALIGN_LEFT);
        temp.setColor("black");
        temp.setFontFace(String.valueOf(10));
        temp.setFontSize(10);
        temp.setHeight(25);
        temp.setWidth(100);
        return temp;
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public DisplayProperties createLike(Editor editor) {
        DisplayProperties result = (DisplayProperties)this.clone();
        editor.edit(result);
        return result;
    }

    public static interface Editor {
        public void edit(DisplayProperties var1);
    }
}

