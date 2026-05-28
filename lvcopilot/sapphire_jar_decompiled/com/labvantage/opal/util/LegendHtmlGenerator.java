/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.opal.util;

import com.labvantage.sapphire.Trace;
import java.util.HashMap;
import java.util.Set;
import javax.servlet.jsp.PageContext;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LegendHtmlGenerator {
    private static final boolean __Debug = false;

    public static String getHtml(String elementId, PageContext pageContext) {
        String alignment = "";
        String mandatoryColor = "";
        String calcColor = "";
        String specColor = "";
        StringBuffer sbLegend = new StringBuffer();
        if (alignment.equalsIgnoreCase("")) {
            alignment = "right";
        }
        try {
            HashMap<String, String> hmColors = new HashMap<String, String>();
            Set keys = hmColors.keySet();
            HashMap<String, String> hmStaticLegend = new HashMap<String, String>();
            PropertyList plDataEntry = (PropertyList)pageContext.getAttribute(elementId, 2);
            PropertyList plPageData = (PropertyList)pageContext.getAttribute("pagedata", 2);
            hmStaticLegend.put("Released", "<font style=\"font-style:italic;\">italics</font>");
            hmStaticLegend.put("User Not Certified", LegendHtmlGenerator.getLegendItem("#FBCD74"));
            String enalbeResourcemanagement = plPageData.getProperty("enableresourcemanagement");
            if (enalbeResourcemanagement != null && enalbeResourcemanagement.equalsIgnoreCase("Y")) {
                hmStaticLegend.put("Valid", LegendHtmlGenerator.getLegendItem("#00FF00"));
                hmStaticLegend.put("Overridden", LegendHtmlGenerator.getLegendItem("#FF6600"));
                hmStaticLegend.put("Invalid", LegendHtmlGenerator.getLegendItem("#FF0000"));
            }
            if (plPageData == null) {
                throw new Exception("Could not access the pagetype properties. ");
            }
            PropertyList plLegend = plPageData.getPropertyList("legend");
            if (plLegend != null) {
                if (plLegend.getProperty("show").equalsIgnoreCase("N")) {
                    return "";
                }
                alignment = plLegend.getProperty("align");
                if (alignment.equalsIgnoreCase("")) {
                    alignment = "right";
                }
            }
            if (plDataEntry == null) {
                throw new Exception("Could not access the dataentry element " + elementId);
            }
            PropertyList plColors = plDataEntry.getPropertyList("dataentrycolumn").getPropertyList("colors");
            if (plColors != null) {
                mandatoryColor = plColors.getProperty("mandatory");
                calcColor = plColors.getProperty("calculation");
                specColor = plColors.getProperty("specification");
                hmColors.put("Mandatory", mandatoryColor);
                hmColors.put("Calculation", calcColor);
                if (specColor.indexOf(";") >= 0) {
                    String[] arrSpecColors = StringUtil.split(specColor, ";");
                    for (int i = 0; i < arrSpecColors.length; ++i) {
                        if (arrSpecColors[i].equalsIgnoreCase("")) continue;
                        hmColors.put(arrSpecColors[i].substring(0, arrSpecColors[i].indexOf("=")), arrSpecColors[i].substring(arrSpecColors[i].indexOf("=") + 1));
                    }
                }
                sbLegend.append("<table cellspacing=0 cellpadding=0 width=90% height=10 border=0><tr height=10>\n");
                sbLegend.append("<td align=" + alignment + " height=10>");
                sbLegend.append("<table border=0 cellspacing=0 cellpadding=0 id=\"legendtable\">\n");
                sbLegend.append("<tr height=10><td height=10>");
                sbLegend.append("<a href=\"/Show/Hide color legend\" onmouseover=\"toggleLegend(&#034;" + alignment + "&#034;)\" onmouseout=\"toggleLegend(&#034;" + alignment + "&#034;)\" onclick=\"return false;\" id=legenda style=\"color:brown;text-decoration:underline\">Legend</a>");
                sbLegend.append("</td></tr>\n");
                sbLegend.append("<tr><td>\n");
                sbLegend.append("<div id=\"legenddiv\" class=legenddiv>");
                sbLegend.append("<table style=\"margin:0px; padding:0px\" border=0 cellspacing=0 cellpadding=3>\n");
                for (String text : keys) {
                    String color = (String)hmColors.get(text);
                    sbLegend.append("<tr>\n");
                    sbLegend.append("<td nowrap>\n");
                    sbLegend.append(LegendHtmlGenerator.getLegendItem(color));
                    sbLegend.append("</td>\n");
                    sbLegend.append("<td nowrap>-&nbsp;" + text + "</td>\n");
                    sbLegend.append("</tr>\n");
                }
                keys = hmStaticLegend.keySet();
                for (String text : keys) {
                    String item = (String)hmStaticLegend.get(text);
                    sbLegend.append("<tr>\n");
                    sbLegend.append("<td nowrap>" + item + "</td>\n");
                    sbLegend.append("<td nowrap>-&nbsp;" + text + "</td>\n");
                    sbLegend.append("</tr>\n");
                }
                sbLegend.append("</table>\n");
                sbLegend.append("</div></td></tr></table>");
                sbLegend.append("</td></tr></table>");
            }
        }
        catch (Exception e) {
            Trace.logDebug("OPAL_ERR: LegendHtmlGenerator.getHtml -> Error retrieving Dataentry column colors. " + e);
            sbLegend = new StringBuffer("");
        }
        return sbLegend.toString();
    }

    public static String getLegendItem(String color) {
        StringBuffer sbLegendItem = new StringBuffer();
        sbLegendItem.append("<table style=\"border:1px solid black; width:5px; height:5px; margin:0px; padding:0px; background-color:" + color + "\"  height=5px cellspacing=0 cellpadding=0>\n");
        sbLegendItem.append("    <tr height=5px>\n");
        sbLegendItem.append("        <td width=5 height=5>&nbsp;&nbsp;</td>\n");
        sbLegendItem.append("    </tr>\n");
        sbLegendItem.append("</table>\n");
        return sbLegendItem.toString();
    }
}

