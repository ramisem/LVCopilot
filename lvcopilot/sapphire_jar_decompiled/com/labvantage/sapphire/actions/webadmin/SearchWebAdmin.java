/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.webadmin;

import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.HttpUtil;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SearchWebAdmin
extends BaseAction {
    public static final String PROPERTY_SEARCHTEXT = "searchtext";
    public static final String PROPERTY_PROPERTYTREEROOT = "propertytreeroot";
    public static final String PROPERTY_WEBPAGEROOT = "webpageroot";
    private String searchPropertyid = "";
    private String searchWebpageid = "";
    private String searchPropertytreeid = "";
    private int hitCount = 0;

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        SafeSQL safeSQL;
        String searchText = properties.getProperty(PROPERTY_SEARCHTEXT);
        String propertyTreeRoot = properties.getProperty(PROPERTY_PROPERTYTREEROOT);
        String webpageRoot = properties.getProperty(PROPERTY_WEBPAGEROOT);
        boolean directivesFound = false;
        do {
            int i;
            directivesFound = false;
            String newSearchText = this.findDirectives(searchText, new String[]{"PROPERTY:", "PROPERTYID:"});
            if (!searchText.equals(newSearchText) && (i = newSearchText.indexOf(" ")) > 0) {
                this.searchPropertyid = newSearchText.substring(0, i);
                searchText = newSearchText.substring(i + 1).trim();
                directivesFound = true;
            }
            if (!searchText.equals(newSearchText = this.findDirectives(searchText, new String[]{"PAGE:", "PAGEID:", "WEBPAGE:", "WEBPAGEID:"})) && (i = newSearchText.indexOf(" ")) > 0) {
                this.searchWebpageid = newSearchText.substring(0, i);
                searchText = newSearchText.substring(i + 1).trim();
                directivesFound = true;
            }
            if (searchText.equals(newSearchText = this.findDirectives(searchText, new String[]{"TREE:", "PROPERTYTREE:", "TREEID:", "PROPERTYTREEID:", "ELEMENT:", "ELEMENTID:", "LAYOUT:", "LAYOUTID:", "PAGETYPE:", "PAGETYPEID:"})) || (i = newSearchText.indexOf(" ")) <= 0) continue;
            this.searchPropertytreeid = newSearchText.substring(0, i);
            searchText = newSearchText.substring(i + 1).trim();
            directivesFound = true;
        } while (directivesFound);
        SimpleDateFormat formatter = new SimpleDateFormat("d-MMM-yy");
        int searchCount = 0;
        StringBuffer output = new StringBuffer();
        if (this.searchWebpageid.length() == 0) {
            safeSQL = new SafeSQL();
            String treeSQL = "select propertytreeid, propertytreetype, createdt, moddt, modby, objectname, valuetree from propertytree where propertytreetype in ( 'Element', 'Page Type', 'Layout', 'Gizmo', 'StellarPageType', 'StellarElement', 'StellarDataSource', 'StellarGizmo' )";
            if (this.searchPropertytreeid.length() > 0) {
                treeSQL = treeSQL + " AND propertytreeid=" + safeSQL.addVar(this.searchPropertytreeid);
            }
            treeSQL = treeSQL + " order by propertytreetype, propertytreeid";
            this.database.createPreparedResultSet(treeSQL, safeSQL.getValues());
            while (this.database.getNext()) {
                boolean first = true;
                String propertytreeid = this.database.getString("propertytreeid");
                String xml = this.database.getClob("valuetree");
                if (xml == null || xml.indexOf(searchText) < 0) continue;
                String propertytreetype = this.database.getString("propertytreetype");
                String objectname = this.database.getString("objectname");
                Timestamp moddtvalue = this.database.getTimestamp("moddt");
                String moddt = moddtvalue == null ? "" : formatter.format(moddtvalue);
                String modby = this.database.getString("modby");
                PropertyTree propertyTree = new PropertyTree();
                propertyTree.setValueXML(xml);
                ArrayList nodes = propertyTree.getAllNodes();
                for (int j = 0; j < nodes.size(); ++j) {
                    ++searchCount;
                    Node node = (Node)nodes.get(j);
                    PropertyList pl = node.getPropertyList(true);
                    StringBuffer sampleText = new StringBuffer();
                    String plxml = pl.toXMLString();
                    int plPos = plxml.indexOf(searchText);
                    if (plPos < 0) continue;
                    boolean found = false;
                    sampleText.append("<table border=1 style=\"border: 1px solid #666666\" cellspacing=\"0\" >");
                    while (plPos >= 0) {
                        String innerText = this.getSampleText(plxml, plPos, searchText);
                        if (innerText.length() > 0) {
                            found = true;
                            ++this.hitCount;
                            sampleText.append(innerText);
                        }
                        plPos = plxml.indexOf(searchText, plPos + 10);
                    }
                    sampleText.append("</table>");
                    if (!found) continue;
                    output.append(this.getLine(!first, propertyTreeRoot + "&ptreeid=" + propertytreeid + "&ptreetype=" + propertytreetype + "&selectednodeid=" + HttpUtil.encodeURIComponent(node.getNodeId()), propertytreetype + " <b>" + propertytreeid + "</b>, " + node.getNodeId() + " Node", sampleText.toString(), "Last Modified on " + moddt + " by " + modby + ". " + (objectname != null && objectname.length() > 0 ? "Refers to " + objectname : "")));
                    first = false;
                }
            }
        }
        safeSQL = new SafeSQL();
        String pageSQL = "SELECT wppt.webpageid, wppt.productedition, wppt.propertytreeid, wppt.elementid, wppt.productvaluetree, wppt.valuetree, wppt.componentvaluetree, wppt.extendnodeid, wp.createdt, wp.moddt, wp.modby, pt.propertytreetype FROM webpage wp, webpagepropertytree wppt, propertytree pt WHERE wp.webpageid = wppt.webpageid AND wppt.propertytreeid = pt.propertytreeid ";
        if (this.searchPropertytreeid.length() > 0) {
            pageSQL = pageSQL + " AND wppt.propertytreeid=" + safeSQL.addVar(this.searchPropertytreeid);
        }
        if (this.searchWebpageid.length() > 0) {
            pageSQL = pageSQL + " AND wppt.webpageid=" + safeSQL.addVar(this.searchWebpageid);
        }
        pageSQL = pageSQL + " ORDER BY wppt.webpageid";
        this.database.createPreparedResultSet(pageSQL, safeSQL.getValues());
        String lastPage = "";
        boolean first = false;
        while (this.database.getNext()) {
            String componentvaluetree;
            String productplxml;
            String webpageid = this.database.getString("webpageid");
            String edition = this.database.getString("productedition");
            if (!webpageid.equals(lastPage)) {
                first = true;
            }
            lastPage = webpageid;
            ++searchCount;
            String plxml = this.database.getClob("valuetree");
            boolean found = false;
            if (plxml != null && plxml.length() > 0) {
                found = this.searchPageOverrides(searchText, webpageRoot, formatter, output, first, webpageid, edition, plxml);
            }
            if (!found && (productplxml = this.database.getClob("productvaluetree")) != null && productplxml.length() > 0) {
                found = this.searchPageOverrides(searchText, webpageRoot, formatter, output, first, webpageid, edition, productplxml);
            }
            if (!found && (componentvaluetree = this.database.getClob("componentvaluetree")) != null && componentvaluetree.length() > 0) {
                found = this.searchPageOverrides(searchText, webpageRoot, formatter, output, first, webpageid, edition, componentvaluetree);
            }
            if (!found) continue;
            first = false;
        }
        properties.put("html", output.toString());
        properties.put("hitcount", Integer.toString(this.hitCount));
        properties.put("searchcount", Integer.toString(searchCount));
    }

    private String findDirectives(String searchText, String[] directiveList) {
        for (int i = 0; i < directiveList.length; ++i) {
            if (!searchText.toUpperCase().startsWith(directiveList[i].toUpperCase())) continue;
            return searchText.substring(directiveList[i].length()).trim();
        }
        return searchText;
    }

    private boolean searchPageOverrides(String searchText, String webpageRoot, SimpleDateFormat formatter, StringBuffer output, boolean first, String webpageid, String edition, String plxml) throws SapphireException {
        String propertytreeid = this.database.getString("propertytreeid");
        String propertytreetype = this.database.getString("propertytreetype");
        String elementid = this.database.getString("elementid");
        int plPos = plxml.indexOf(searchText);
        StringBuffer sampleText = new StringBuffer();
        while (plPos >= 0) {
            String innerText = this.getSampleText(plxml, plPos, searchText);
            if (innerText.length() > 0) {
                ++this.hitCount;
                sampleText.append(innerText);
            }
            plPos = plxml.indexOf(searchText, plPos + 10);
        }
        if (sampleText.length() > 0) {
            Timestamp moddtvalue = this.database.getTimestamp("moddt");
            String moddt = moddtvalue == null ? "" : formatter.format(moddtvalue);
            String modby = this.database.getString("modby");
            output.append(this.getLine(!first, webpageRoot + "&pageid=" + webpageid + "&edition=" + edition + "&selectedptreeid=" + propertytreeid + "&selectedptreetype=" + propertytreetype + "&selectedelementid=" + elementid, "Page <b>" + webpageid + "</b>, " + propertytreetype + " " + propertytreeid + " (" + elementid + ")", "<table border=1 style=\"border: 1px solid #666666\" cellspacing=\"0\" >" + sampleText.toString() + "</table>", "Last Modified on " + moddt + " by " + modby + "."));
        }
        return sampleText.length() > 0;
    }

    private String getSampleText(String plxml, int plPos, String searchText) {
        int openCdata = plxml.lastIndexOf("<![CDATA[", plPos);
        int closeCdata = plxml.indexOf("]]>", plPos);
        int openPropertyCheck = plxml.lastIndexOf("<property ", plPos);
        int openPropertyListCheck = plxml.lastIndexOf("<propertylist ", plPos);
        int openProperty = plxml.lastIndexOf("<property ", openCdata);
        int openQuote = plxml.indexOf("id=\"", openProperty);
        int closeQuote = plxml.indexOf("\"", openQuote + 5);
        String sampleText = "";
        if (openPropertyCheck < openCdata && openPropertyListCheck < openCdata && openQuote > 0 && closeQuote > 0 && openCdata > 0 && closeCdata > 0) {
            String propertyid = plxml.substring(openQuote + 4, closeQuote);
            if (this.searchPropertyid.length() == 0 || propertyid.equalsIgnoreCase(this.searchPropertyid)) {
                String value = plxml.substring(openCdata + 9, closeCdata);
                value = this.tidyString(searchText, value);
                sampleText = "<tr><td style=\"background-color:wheat; border-right: 1px solid #666666\">" + propertyid + "</td><td>" + value + "</td></tr>";
            }
        } else if (openPropertyCheck > 0 && this.searchPropertyid.length() == 0) {
            int extra = 100;
            String value = plxml.length() > openPropertyCheck + extra + 2 ? plxml.substring(openPropertyCheck, openPropertyCheck + extra) : plxml.substring(openPropertyCheck);
            value = this.tidyString(searchText, value);
            sampleText = "<tr><td colspan=\"2\" style=\"border-right: 1px solid #666666\">" + value + "</td></tr>";
        }
        return sampleText;
    }

    private String tidyString(String searchText, String value) {
        value = StringUtil.replaceAll(value, "&", "&amp;");
        value = StringUtil.replaceAll(value, "\"", "&quot;");
        value = StringUtil.replaceAll(value, "<", "&lt;");
        value = StringUtil.replaceAll(value, ">", "&gt;");
        value = StringUtil.replaceAll(value, searchText, "<b>" + searchText + "</b>");
        return value;
    }

    private StringBuffer getLine(boolean indent, String url, String label, String sampleText, String greenLine) {
        StringBuffer output = new StringBuffer();
        output.append("<p style=\"font-size: 10pt; margin-left: " + (indent ? "80" : "20") + "\">");
        output.append("<a target=\"searchdetails\" href=\"" + url + "\">" + label + "</a>");
        output.append(sampleText.length() > 0 ? "<br>" + sampleText + "" : "");
        output.append("<span style=\"color: green\">" + greenLine + "</span>");
        output.append("</p><br>");
        return output;
    }
}

