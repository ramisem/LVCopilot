/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.util;

import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ColumnManager {
    private TranslationProcessor tp;
    private SDCProcessor sp;
    private String formPrefix = "pr";

    public ColumnManager(TranslationProcessor tp, SDCProcessor sp) {
        this.tp = tp;
        this.sp = sp;
    }

    public void setFormPrefix(String newPrefix) {
        this.formPrefix = newPrefix;
    }

    public String getLookupScript(String lookupSdcId, String columnid, String restrictivewhere, PropertyListCollection lookupColCollection) {
        return this.getLookupScript(lookupSdcId, columnid, restrictivewhere, lookupColCollection, "", false);
    }

    public String getLookupScript(String lookupSdcId, String columnid, String restrictivewhere, PropertyListCollection lookupColCollection, boolean showTemplates) {
        return this.getLookupScript(lookupSdcId, columnid, restrictivewhere, lookupColCollection, "", showTemplates);
    }

    public String getLookupScript(String lookupSdcId, String columnid, String restrictivewhere, PropertyListCollection lookupColCollection, String selectorType, boolean showTemplates) {
        StringBuilder lookupScript = new StringBuilder();
        if (lookupColCollection.size() == 0 && lookupSdcId != null && !lookupSdcId.equals("")) {
            PropertyList sdcProps = this.sp.getProperties(lookupSdcId);
            PropertyList col = new PropertyList();
            col.setProperty("id", sdcProps.getProperty("keycolid1"));
            col.setProperty("columnid", sdcProps.getProperty("keycolid1"));
            col.setProperty("mode", "Display and Return");
            col.setProperty("maptocolumn", columnid);
            lookupColCollection.add(col);
            if (!sdcProps.getProperty("keycolid2", "").equals("")) {
                col = new PropertyList();
                col.setProperty("id", sdcProps.getProperty("keycolid2"));
                col.setProperty("columnid", sdcProps.getProperty("keycolid2"));
                col.setProperty("mode", "Display and Return");
                col.setProperty("maptocolumn", sdcProps.getProperty("keycolid2"));
                lookupColCollection.add(col);
            }
            if (!sdcProps.getProperty("keycolid3", "").equals("")) {
                col = new PropertyList();
                col.setProperty("id", sdcProps.getProperty("keycolid3"));
                col.setProperty("columnid", sdcProps.getProperty("keycolid3"));
                col.setProperty("mode", "Display and Return");
                col.setProperty("maptocolumn", sdcProps.getProperty("keycolid3"));
                lookupColCollection.add(col);
            }
            col = new PropertyList();
            col.setProperty("id", sdcProps.getProperty("desccol"));
            col.setProperty("columnid", sdcProps.getProperty("desccol"));
            col.setProperty("mode", "Display Only");
            lookupColCollection.add(col);
        }
        lookupScript.append("<script>var oLUPD_").append(this.formPrefix).append("_").append(columnid).append("={");
        lookupScript.append("\"selectortype\":\"").append(selectorType).append("\",");
        lookupScript.append("\"restrictivewhere\":\"").append(restrictivewhere.replaceAll("\"", "\\\\\"")).append("\",");
        if (showTemplates) {
            lookupScript.append("\"showtemplates\":\"").append("Y").append("\",");
        }
        lookupScript.append("\"columns\":[");
        for (int j = 0; j < lookupColCollection.size(); ++j) {
            PropertyList lookupcolumn = lookupColCollection.getPropertyList(j);
            String lId = lookupcolumn.getProperty("id", "");
            String lColumnid = lookupcolumn.getProperty("columnid", "");
            String lTitle = lookupcolumn.getProperty("title", "").replaceAll("\"", "\\\\\"");
            String lMode = lookupcolumn.getProperty("mode", "");
            String lDisplayvalue = lookupcolumn.getProperty("displayvalue", "").replaceAll("\"", "\\\\\"");
            String lPseudocolumn = lookupcolumn.getProperty("pseudocolumn", "").replaceAll("\"", "\\\\\"");
            String lMaptocolumn = lookupcolumn.getProperty("maptocolumn", "");
            String lSearchable = lookupcolumn.getProperty("searchable", "");
            String lumode = lMode.equalsIgnoreCase("Return Only") || lMode.equalsIgnoreCase("Hidden Only") ? "Hidden Value" : "Display Text";
            lTitle = this.tp.translate(lTitle);
            if (j > 0) {
                lookupScript.append(",");
            }
            lookupScript.append("{\"id\":\"").append(lId).append("\"");
            if (!lMode.equalsIgnoreCase("Return Only") && !lMode.equalsIgnoreCase("Display and Return") || lMaptocolumn.trim().equals("")) {
                lMaptocolumn = "";
            }
            lookupScript.append(",\"mapfieldid\":\"").append(lMaptocolumn).append("\"");
            lookupScript.append(",\"title\":\"").append(lTitle).append("\"");
            lookupScript.append(",\"lumode\":\"").append(lMode).append("\"");
            lookupScript.append(",\"columnid\":\"").append(lColumnid).append("\"");
            if (!lSearchable.equals("")) {
                lookupScript.append(",\"searchable\":\"").append(lSearchable).append("\"");
            }
            lookupScript.append(",\"mode\":\"").append(lumode).append("\"");
            if (!lDisplayvalue.equals("")) {
                lookupScript.append(",\"displayvalue\":\"").append(lDisplayvalue).append("\"");
            }
            if (!lPseudocolumn.equals("")) {
                lookupScript.append(",\"pseudocolumn\":\"").append(lPseudocolumn).append("\"");
            }
            lookupScript.append("}");
        }
        lookupScript.append("],\"sdcid\":\"").append(lookupSdcId).append("\"};</script>");
        return lookupScript.toString();
    }
}

