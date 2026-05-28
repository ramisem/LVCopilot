/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.propertytree.EditorUtil;
import com.labvantage.sapphire.admin.propertytree.PropertyTreeDisplayOptions;
import com.labvantage.sapphire.xml.PropertyDefinition;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import com.labvantage.sapphire.xml.PropertyTree;
import com.labvantage.sapphire.xml.PropertyTreeDefHandler;
import com.labvantage.sapphire.xml.SaxUtil;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import org.w3c.dom.Element;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.Browser;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class PropertyListEditor {
    public String getEditor(String nodeid, Element propertydeflist, PropertyList propertylist, PropertyList toppropertylist, String parentid, int propertylistindex, PageContext pagecontext, PropertyTreeDisplayOptions options) throws SapphireException {
        PropertyTree tree = new PropertyTree();
        PropertyTreeDefHandler handler = new PropertyTreeDefHandler(tree);
        handler.setXMLString(propertydeflist.toString());
        handler.setPrintStream(null);
        SaxUtil.parseString(handler);
        return this.getEditor(nodeid, tree.getPropertyDefinitionList(), propertylist, toppropertylist, parentid, propertylistindex, pagecontext, options);
    }

    public String getEditor(String nodeid, PropertyDefinitionList propertyDefinitionList, PropertyList propertylist, PropertyList toppropertylist, String parentid, int propertylistindex, PageContext pagecontext, PropertyTreeDisplayOptions options) {
        StringBuilder output = new StringBuilder();
        StringBuilder showiflist = new StringBuilder();
        TranslationProcessor tp = pagecontext != null ? new TranslationProcessor(pagecontext) : null;
        Browser browser = new Browser(pagecontext);
        if (options.showDebug) {
            Trace.log("PL-EDITOR", "Draw ProperytListEditor");
        }
        HashMap<String, String> showifFields = new HashMap<String, String>();
        for (Object aPropertyDefinitionList : propertyDefinitionList) {
            PropertyDefinition propertyDefinition = (PropertyDefinition)aPropertyDefinitionList;
            String propertyid = propertyDefinition.getId();
            String title = propertyDefinition.getTitle();
            String showiftop = propertyDefinition.getShowIf();
            boolean deprecated = propertyDefinition.isDeprecated();
            boolean advanced = options.cascadeAdvanced || propertyDefinition.isAdvanced();
            boolean showinnode = true;
            if (showiftop.startsWith("node=")) {
                showinnode = false;
                String valuelist = showiftop.substring(showiftop.indexOf("=") + 1);
                if (valuelist != null && valuelist.length() > 0) {
                    String[] values;
                    for (String val : values = StringUtil.split(valuelist, ";")) {
                        if (!val.equals(nodeid)) continue;
                        showinnode = true;
                        break;
                    }
                    showiftop = "";
                }
            }
            if (!showinnode) continue;
            String fieldname = parentid + "_" + propertylistindex + "_" + propertyid;
            String type = propertyDefinition.getType();
            String id = "";
            String display = "";
            if (!options.showAdvanced && showiftop.length() > 0) {
                String delimiter = " OR ";
                boolean logicalANDOperator = false;
                boolean logicalANDOperatorDisplayFlag = true;
                boolean logicalOROperator = false;
                showiftop = StringUtil.replaceAll(showiftop, " || ", " OR ");
                if ((showiftop = StringUtil.replaceAll(showiftop, "||", " OR ")).indexOf(" AND ") > -1) {
                    delimiter = " AND ";
                    logicalANDOperator = true;
                    logicalOROperator = false;
                }
                if (showiftop.indexOf(" OR ") > -1) {
                    delimiter = " OR ";
                    logicalOROperator = true;
                    logicalANDOperator = false;
                }
                String[] showifs = StringUtil.split(showiftop, delimiter);
                display = "none";
                for (int i = 0; i < showifs.length; ++i) {
                    String propertyValue;
                    String[] parts;
                    String showif = showifs[i].trim();
                    String operation = "=";
                    if (showif.indexOf("!=") != -1) {
                        operation = "!=";
                    }
                    if ((parts = StringUtil.split(showif, operation)).length <= 0) continue;
                    String field = parts[0];
                    String value = parts.length == 1 ? "" : parts[1];
                    boolean top = false;
                    boolean parent = false;
                    if (field.startsWith("top.")) {
                        field = field.substring(4);
                        top = true;
                    } else if (field.startsWith("parent.")) {
                        field = field.substring(7);
                        parent = true;
                    }
                    String string = top ? toppropertylist.getProperty(field) : (propertyValue = parent ? propertylist.getParentPropertyValue().getParentPropertyList().getProperty(field) : propertylist.getProperty(field));
                    if (top) {
                        id = "root_0_" + field;
                        showiflist.append("addShowIf( 'root_0_").append(field).append("',");
                    } else if (parent) {
                        id = parentid.substring(0, parentid.lastIndexOf(95)) + "_" + field;
                        showiflist.append("addShowIf( '" + parentid.substring(0, parentid.lastIndexOf(95))).append("_").append(field).append("',");
                    } else {
                        id = parentid + "_" + propertylistindex + "_" + field;
                        showiflist.append("addShowIf( '").append(parentid).append("_").append(propertylistindex).append("_").append(field).append("',");
                    }
                    showiflist.append("'").append(value).append("',");
                    showiflist.append("'").append(fieldname).append("',");
                    showiflist.append("'").append(operation).append("',");
                    showiflist.append("'").append(logicalOROperator).append("',");
                    showiflist.append(logicalANDOperator).append(" );\n");
                    if (logicalANDOperator || logicalOROperator) {
                        if (showifFields.containsKey(fieldname)) {
                            showifFields.put(fieldname, (String)showifFields.get(fieldname) + ";" + id);
                        } else {
                            showifFields.put(fieldname, id);
                        }
                    }
                    if (logicalANDOperator && !logicalANDOperatorDisplayFlag) continue;
                    if ("=".equals(operation)) {
                        if (value.equals("") && propertyValue.length() == 0 || value.equals("*") && propertyValue.length() > 0 || value.length() > 1 && value.endsWith("*") && propertyValue.startsWith(value.substring(0, value.length() - 1)) || (";" + value + ";").indexOf(";" + propertyValue + ";") >= 0) {
                            display = "table-row";
                            continue;
                        }
                        if (!logicalANDOperator) continue;
                        display = "none";
                        logicalANDOperatorDisplayFlag = false;
                        continue;
                    }
                    if (!"!=".equals(operation)) continue;
                    if ((";" + value + ";").indexOf(";" + propertyValue + ";") == -1) {
                        display = "table-row";
                        continue;
                    }
                    if (!logicalANDOperator) continue;
                    display = "none";
                    logicalANDOperatorDisplayFlag = false;
                }
            }
            if (options.showAdvanced || !advanced) {
                output.append("<tr ").append(display.length() > 0 ? "style=\"display:" + display + "\"" : "").append(" id=\"").append(fieldname).append("_ROW\" title=\"").append(propertyDefinition.getHelp()).append("\">");
                output.append("<td width=\"120\" style=\"padding: 3px; background-color: ").append(propertyDefinitionList.getColor()).append("\" valign=\"top\">");
                output.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\"><tr><td nowrap ").append(deprecated ? "style=\"text-decoration: line-through double\"" : "").append(">");
                output.append(title == null ? "&nbsp;" : (options.translate && tp != null ? tp.translate(title) : title));
                output.append("</td><td align=\"right\">");
            }
            String labelsingular = "";
            String labelplural = "";
            boolean showhide = false;
            if (!type.equals("simple")) {
                PropertyDefinitionList subPropertyDefinitionList = propertyDefinition.getPropertyDefinitionList();
                if (subPropertyDefinitionList != null) {
                    labelsingular = subPropertyDefinitionList.getLabelSingular();
                    labelplural = subPropertyDefinitionList.getLabelPlural();
                }
                if (labelsingular.equals("")) {
                    labelsingular = "Item";
                }
                if (labelplural.equals("")) {
                    labelplural = "Items";
                }
                showhide = subPropertyDefinitionList != null && subPropertyDefinitionList.isShowhide();
                boolean bl = showhide = showhide && (options.showAdvanced || !advanced);
                if (showhide) {
                    String expanded = "Y";
                    PropertyValue pv = propertylist.getPropertyValue(propertyid);
                    if (pv != null) {
                        expanded = pv.getAttribute("expanded");
                    }
                    expanded = expanded.equals("Y") ? "N" : "Y";
                    output.append("<input type=\"hidden\" id=\"").append(fieldname).append("__EXPANDED\" name=\"").append(fieldname).append("__EXPANDED\" value=\"").append(expanded).append("\">");
                    output.append("<span style=\"display: none\" id=\"").append(fieldname).append("__SPANSHOW\"><a href='' onClick='toggleexpand( \"").append(fieldname).append("\");sapphire.events.cancelEvent(event, false);'><img style='border-color: black; border-width: 1' src='WEB-CORE/elements/images/doubleup.gif'></a></span>");
                    output.append("<span style=\"display: none\" id=\"").append(fieldname).append("__SPANHIDE\"><a href='' onClick='toggleexpand( \"").append(fieldname).append("\");sapphire.events.cancelEvent(event, false);'><img style='border-color: black; border-width: 1' src='WEB-CORE/elements/images/doubledown.gif'></a></span>");
                }
            }
            if (options.showAdvanced || !advanced) {
                output.append("</td></tr></table></td>\n<td valign=\"top\">\n");
                if (showhide) {
                    output.append("<div id=\"").append(fieldname).append("__TABLEHIDE\">");
                    if (type.equals("collection")) {
                        PropertyListCollection collection = propertylist.getCollection(propertyid);
                        int size = collection == null ? 0 : collection.size();
                        output.append("Click arrow to show ").append(size).append(" ").append(size == 1 ? labelsingular : labelplural);
                    } else {
                        output.append("Click arrow to show more properties");
                    }
                    output.append("</div>");
                    output.append("<div " + (showhide ? " style=\"display: none\" " : "") + " id=\"").append(fieldname).append("__TABLESHOW\">");
                }
            }
            try {
                output.append(EditorUtil.displayEditor(nodeid, propertyDefinition, propertylist, toppropertylist, parentid, propertylistindex, pagecontext, options, tp, browser));
            }
            catch (Exception e) {
                output.append("Failed to display Editor");
            }
            if (options.showAdvanced || !advanced) {
                if (showhide) {
                    output.append("</div>");
                }
                output.append("</td></tr>\n");
            }
            if (!showhide) continue;
            output.append("<script>toggleexpand( '").append(fieldname).append("' );</script>\n");
        }
        if (showiflist.length() > 0) {
            output.append("<script>");
            output.append((CharSequence)showiflist);
            output.append("\n if ( typeof( showIfFields ) != 'undefined' ) { showifFields = new Array(); }");
            for (String propertyid : showifFields.keySet()) {
                output.append("\nshowifFields['" + propertyid + "'] = ").append("'").append((String)showifFields.get(propertyid)).append("';");
            }
            output.append("</script>");
        }
        return output.toString();
    }
}

