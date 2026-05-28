/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.util.HttpUtil;
import sapphire.util.JsonArray;
import sapphire.util.JsonObject;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class GridWidthEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        JsonObject params = new JsonObject();
        String columnsCollectionid = attributes.containsKey("columnscollectionid") ? (String)attributes.get("columnscollectionid") : (String)attributes.get("fieldscollectionid");
        PropertyList parentPropertyList = propertyValue.getParentPropertyList();
        if (parentPropertyList != null && parentPropertyList.getProperty(columnsCollectionid).length() == 0) {
            PropertyList propertyList = parentPropertyList = parentPropertyList.getParentPropertyValue() != null ? parentPropertyList.getParentPropertyValue().getParentPropertyList() : null;
        }
        if (parentPropertyList != null && parentPropertyList.getProperty(columnsCollectionid).length() == 0) {
            PropertyList propertyList = parentPropertyList = parentPropertyList.getParentPropertyValue() != null ? parentPropertyList.getParentPropertyValue().getParentPropertyList() : null;
        }
        if (parentPropertyList == null) {
            return "ERROR: Could not locate columns collection";
        }
        StringBuilder out = new StringBuilder();
        try {
            String titleprop;
            String idprop;
            PropertyListCollection allColumnsCollection = parentPropertyList.getCollectionNotNull(columnsCollectionid);
            JsonArray allColumns = new JsonArray();
            String string = idprop = attributes.containsKey("idpropertyid") ? attributes.get("idpropertyid").toString() : null;
            if (idprop == null || idprop.length() == 0) {
                idprop = "columnid";
            }
            String string2 = titleprop = attributes.containsKey("titlepropertyid") ? attributes.get("titlepropertyid").toString() : null;
            if (idprop == null || idprop.length() == 0) {
                idprop = "title";
            }
            for (int i = 0; i < allColumnsCollection.size(); ++i) {
                PropertyList columnPL = allColumnsCollection.getPropertyList(i);
                String columnid = columnPL.getProperty(idprop);
                if (columnid.length() == 0) {
                    throw new SapphireException("Column Id.");
                }
                if (!columnPL.getProperty("show", "Y").equals("Y")) continue;
                JsonObject col = new JsonObject();
                col.put("columnid", columnid);
                col.put("title", columnPL.getProperty(titleprop, columnid));
                col.put("sequence", 1000 + i);
                allColumns.put(col);
            }
            PropertyValue collectionPropertyValue = propertyValue.getParentPropertyList().getParentPropertyValue();
            PropertyListCollection allSections = collectionPropertyValue != null ? collectionPropertyValue.getParentPropertyList().getCollectionNotNull(collectionPropertyValue.getId()) : new PropertyListCollection();
            String thisSectionid = propertyValue.getParentPropertyList().getProperty("sectionid");
            for (int i = 0; i < allSections.size(); ++i) {
                PropertyList plsection = allSections.getPropertyList(i);
                if (thisSectionid.equals(plsection.getProperty("sectionid")) || !plsection.getProperty("sectiontype").equals("group")) continue;
                String gridlayout = plsection.getProperty("gridlayout");
                if ((gridlayout = HttpUtil.decodeURIComponent(gridlayout)).length() == 0 || !gridlayout.startsWith("{")) {
                    gridlayout = "{}";
                }
                JsonObject grid = new JsonObject(gridlayout);
                JsonArray columns = grid.getJsonArray("columns");
                for (JsonObject column : columns.toJsonObjectArray()) {
                    String columnid = column.getString("columnid");
                    JsonObject found = allColumns.findJsonObject("columnid", columnid);
                    if (found == null) continue;
                    found.put("used", true);
                }
            }
            params.put("allcolumns", allColumns);
            params.put("fieldname", fieldName);
            String actualValue = ancestorValue ? propertyValue.toString().substring("{|".length(), propertyValue.toString().length() - "|}".length()) : propertyValue.toString();
            actualValue = HttpUtil.decodeURIComponent(actualValue);
            if (actualValue.length() == 0 || !actualValue.startsWith("{")) {
                actualValue = "{}";
            }
            JsonObject grid = new JsonObject(actualValue);
            if (ancestorValue) {
                out.append("<p style=\"color: blue\">");
            }
            out.append("<table><tr><td>");
            out.append("<div id=\"gridwidth_preview_" + fieldName + "\" style=\"zoom:75%\">");
            out.append(GridWidthEditor.getGrid(new JsonArray(allColumns.toString()), grid, true, ancestorValue));
            out.append("</div></td>");
            Button edit = new Button(pageContext);
            String encoded = HttpUtil.encode(params.toString());
            edit.setId(fieldName + "_editbutton");
            edit.setAction("lookupGridWidth( '" + fieldName + "', '" + encoded + "' )");
            edit.setText("Edit...");
            edit.setMargin("none");
            edit.setHighlight("false");
            Button reset = new Button(pageContext);
            reset.setAction("resetGridWidth( '" + fieldName + "' )");
            reset.setId(fieldName + "_resetbutton");
            reset.setText("Reset");
            out.append("<td>" + edit.getHtml() + "</td>");
            out.append("<td>" + reset.getHtml() + "</td>");
            out.append("</tr></table>");
            if (ancestorValue) {
                out.append("</p>");
            }
        }
        catch (Exception e) {
            out.append("Unable to render grid. " + e.getMessage() + ". Try saving.");
        }
        out.append("<input onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" type=\"hidden\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:250px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyValue + "\"/>");
        return out.toString();
    }

    public static String getGrid(JsonArray allColumns, JsonObject grid, boolean preview, boolean ancestorValue) throws JSONException, SapphireException {
        int index;
        JsonArray gridColumns = grid.getJsonArray("columns");
        StringBuilder out = new StringBuilder();
        out.append("<table user-select: none\" cellpadding=\"5\">");
        out.append("<tr>");
        out.append(" <td>");
        out.append("<div>");
        if (!preview) {
            out.append(" <div class=\"gridwidth_grid_titlebar\">Maint Page (Use \ud83e\udc44 \ud83e\udc46 \ud83e\udc45 \ud83e\udc47 to move and resize, ctrl-X to remove, ctrl-d new row, )</div>");
        }
        StringBuilder unusedout = new StringBuilder();
        out.append("<div id=\"gridwidth_grid_container\" " + (ancestorValue ? " style=\"border:3px solid blue\"" : "") + " class=\"gridwidth_grid_container\">");
        unusedout.append("<div style=\"display:none\" id=\"unused_gridwidth_grid_container\">");
        for (index = 0; index < gridColumns.length(); ++index) {
            JsonObject gridColumn = gridColumns.getJsonObject(index);
            String columnid = gridColumn.getString("columnid");
            JsonObject found = allColumns.findJsonObject("columnid", columnid);
            if (found == null) continue;
            found.put("width", Math.abs(gridColumn.getInt("width", 3)));
            found.put("newline", gridColumn.getString("newline"));
            found.put("sequence", gridColumn.getInt("sequence", index));
            found.put("used", true);
            found.put("usedtext", "Y");
            found.put("usedhere", true);
        }
        allColumns.sort("sequence");
        allColumns.sort("usedtext");
        for (index = 0; index < allColumns.size(); ++index) {
            JsonObject column = allColumns.getJsonObject(index);
            int sequence = index;
            String title = column.getString("title");
            String columnid = column.getString("columnid");
            int width = column.getInt("width", 3);
            String newline = column.getString("newline");
            boolean isUsed = column.getBoolean("used");
            boolean autoAdded = false;
            if (preview && !isUsed) {
                column.put("usedhere", true);
                autoAdded = true;
            }
            StringBuilder columnout = new StringBuilder();
            String baseid = "__gridlayout_" + index;
            String innerStyle = "";
            if (newline.equals("Y")) {
                columnout.append("<div name=\"spacer\" style=\"flex-basis: 100%;height:0\"></div>");
                innerStyle = "border-left-width: 3px";
            }
            columnout.append("<div onclick=\"document.getElementById( '" + baseid + "_input' ).focus()\" id=\"" + baseid + "\" class=\"gridwidth_grid_item_outer\" style=\"width:" + 75 * width + "\">");
            columnout.append("<div id=\"" + baseid + "_inner\" style=\"" + innerStyle + "\" class=\"gridwidth_grid_item_inner" + (autoAdded ? "_autoadded" : "") + "\">");
            if (preview) {
                columnout.append("<span class=\"gridwidth_grid_item_label\">" + width + " " + title + (autoAdded ? " (??)" : "") + "</span>&nbsp;");
            } else {
                columnout.append("<table cellspacing=\"0\" cellpadding=\"0\"><tr>");
                columnout.append("<td>");
                columnout.append("<input index=\"" + index + "\" baseid=\"" + baseid + "\"  id=\"" + baseid + "_input\" used=\"" + (isUsed ? "Y" : "N") + "\" usedhere=\"" + (column.getBoolean("usedhere") ? "Y" : "N") + "\" title=\"" + title + "\" sequence=\"" + sequence + "\" columnid=\"" + columnid + "\" newline=\"" + newline + "\" onchange=\"setWidthAndSave(this)\" onkeydown=\"dokeydown( event, this )\" class=\"gridwidth_grid_item_input\" value=\"" + width + "\" type=number min=1 max=12 />");
                columnout.append("</td><td>");
                columnout.append("<span id=\"" + baseid + "_label\" class=\"gridwidth_grid_item_label\">" + title + "</span>&nbsp;");
                columnout.append("</td></tr></table>");
            }
            columnout.append("</div>");
            columnout.append("</div>");
            if (column.getBoolean("usedhere")) {
                out.append((CharSequence)columnout);
                continue;
            }
            unusedout.append((CharSequence)columnout);
        }
        unusedout.append("</div>");
        out.append("</div>");
        out.append("</div>");
        out.append(" </td>");
        out.append("</tr>");
        out.append("</table>");
        out.append((CharSequence)unusedout);
        return out.toString();
    }
}

