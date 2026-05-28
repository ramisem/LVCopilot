/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.file;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import sapphire.xml.PropertyList;

public class ChemicalFileDetails {
    boolean showAtomNumbers = false;
    boolean showAtomColors = true;
    boolean showDescriptors = false;
    String highlightAtoms = "";
    String highlightColor = "blue";
    String renderFormat = "svg";
    String showCarbons = "none";
    int imageWidth = 600;
    int imageHeight = 300;
    Map<String, Object> fields = new HashMap<String, Object>();
    public static final String IMAGEWIDTH = "imagewidth";
    public static final String IMAGEHEIGHT = "imageheight";
    public static final String SHOWATOMNUMBERS = "showatomnumbers";
    public static final String SHOWATOMCOLORS = "showatomcolors";
    public static final String SHOWCARBONS = "showcarbons";
    public static final String SHOWDESCRIPTORS = "showdescriptors";
    public static final String RENDERFORMAT = "renderformat";
    public static final String HIGHLIGHTCOLOR = "highlightcolor";
    public static final String HIGHLIGHTATOMS = "highlightatoms";
    public static final String FIELDS = "fields";

    public void setConfig(PropertyList config) {
        this.imageWidth = ChemicalFileDetails.getInt(config.getProperty("defaultimagewidth"), 600);
        this.imageHeight = ChemicalFileDetails.getInt(config.getProperty("defaultimageheight"), 300);
        this.showAtomNumbers = config.getProperty(SHOWATOMNUMBERS, "N").equals("Y");
        this.showAtomColors = config.getProperty(SHOWATOMCOLORS, "Y").equals("Y");
        this.showCarbons = config.getProperty(SHOWCARBONS, "none");
        this.renderFormat = config.getProperty(RENDERFORMAT, "png");
        this.highlightColor = config.getProperty(HIGHLIGHTCOLOR, "blue");
        this.showDescriptors = config.getPropertyListNotNull("descriptors").getProperty("show", "N").equals("Y");
    }

    public void setMarkup(PropertyList config, PropertyList markup) {
        this.imageWidth = ChemicalFileDetails.getInt(markup.getProperty(IMAGEWIDTH), 600);
        this.imageHeight = ChemicalFileDetails.getInt(markup.getProperty(IMAGEHEIGHT), 300);
        this.showAtomNumbers = markup.getProperty(SHOWATOMNUMBERS, "N").equals("Y");
        this.showAtomColors = markup.getProperty(SHOWATOMCOLORS, "Y").equals("Y");
        this.showCarbons = markup.getProperty(SHOWCARBONS, "none");
        this.showDescriptors = markup.getProperty(SHOWDESCRIPTORS, "N").equals("Y");
        this.highlightAtoms = markup.getProperty(HIGHLIGHTATOMS);
        this.renderFormat = config.getProperty(RENDERFORMAT, "png");
        this.highlightColor = config.getProperty(HIGHLIGHTCOLOR, "blue");
    }

    public void addField(String id, Object value) {
        this.fields.put(id, value);
    }

    public PropertyList getMarkup() {
        PropertyList markup = new PropertyList();
        markup.setProperty(IMAGEWIDTH, "" + this.imageWidth);
        markup.setProperty(IMAGEHEIGHT, "" + this.imageHeight);
        markup.setProperty(SHOWATOMCOLORS, this.showAtomColors ? "Y" : "N");
        markup.setProperty(SHOWATOMNUMBERS, this.showAtomNumbers ? "Y" : "N");
        markup.setProperty(SHOWCARBONS, this.showCarbons);
        markup.setProperty(SHOWDESCRIPTORS, this.showDescriptors ? "Y" : "N");
        markup.setProperty(HIGHLIGHTATOMS, this.highlightAtoms);
        JSONObject jsFields = new JSONObject(this.fields);
        markup.setProperty(FIELDS, jsFields.toString());
        return markup;
    }

    public String getShowCarbons() {
        return this.showCarbons;
    }

    public void setShowCarbons(String showCarbons) {
        this.showCarbons = showCarbons;
    }

    public int getImageWidth() {
        return this.imageWidth == 0 ? 600 : this.imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return this.imageHeight == 0 ? 300 : this.imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getHighlightAtoms() {
        return this.highlightAtoms;
    }

    public void setHighlightAtoms(String highlightAtoms) {
        this.highlightAtoms = highlightAtoms;
    }

    public String getHighlightColor() {
        return this.highlightColor;
    }

    public void setHighlightColor(String highlightColor) {
        this.highlightColor = highlightColor;
    }

    public String getRenderFormat() {
        return this.renderFormat;
    }

    public void setRenderFormat(String renderFormat) {
        this.renderFormat = renderFormat;
    }

    public boolean isShowDescriptors() {
        return this.showDescriptors;
    }

    public void setShowDescriptors(boolean showDescriptors) {
        this.showDescriptors = showDescriptors;
    }

    public boolean isShowAtomNumbers() {
        return this.showAtomNumbers;
    }

    public void setShowAtomNumbers(boolean showAtomNumbers) {
        this.showAtomNumbers = showAtomNumbers;
    }

    public boolean isShowAtomColors() {
        return this.showAtomColors;
    }

    public void setShowAtomColors(boolean showAtomColors) {
        this.showAtomColors = showAtomColors;
    }

    private static int getInt(String value, int def) {
        int ret;
        try {
            ret = Integer.parseInt(value);
        }
        catch (Exception e) {
            ret = def;
        }
        return ret;
    }
}

