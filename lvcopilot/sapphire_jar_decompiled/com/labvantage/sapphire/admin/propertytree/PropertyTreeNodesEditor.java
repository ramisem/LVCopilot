/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.xml.Node;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class PropertyTreeNodesEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        StringBuffer output = new StringBuffer();
        String propertytreeid = (String)attributes.get("propertytreeid");
        String refpropertyid = (String)attributes.get("refpropertyid");
        boolean leafnodes = !"N".equals(attributes.get("leafnodes"));
        boolean excludeLocked = !"N".equals(attributes.get("excludelocked"));
        String sortOrder = (String)attributes.get("sortorder");
        try {
            PropertyList parentPropertyList;
            WebAdminProcessor webadmin = new WebAdminProcessor(pageContext);
            if ((propertytreeid == null || propertytreeid.length() == 0) && refpropertyid != null && refpropertyid.length() > 0 && topPropertyList != null && (propertytreeid = topPropertyList.getProperty(refpropertyid)).length() == 0 && (parentPropertyList = propertyValue.getParentPropertyList()) != null) {
                propertytreeid = parentPropertyList.getProperty(refpropertyid);
            }
            ArrayList<String> values = new ArrayList<String>();
            if (propertytreeid != null && propertytreeid.length() > 0) {
                String customstyle;
                int index;
                String currentValue;
                PropertyTree tree = webadmin.getPropertyTree(propertytreeid);
                ArrayList nodes = tree.getAllNodes();
                for (Node node : nodes) {
                    if (leafnodes) {
                        if (node.getNodeList().size() != 0 || excludeLocked && node.isLocked()) continue;
                        values.add(node.getId());
                        continue;
                    }
                    if (excludeLocked && node.isLocked()) continue;
                    values.add(node.getId());
                }
                if (sortOrder != null) {
                    if ("A".equalsIgnoreCase(sortOrder)) {
                        Collections.sort(values);
                    } else {
                        values.sort((o1, o2) -> -((String)o1).compareToIgnoreCase((String)o2));
                    }
                }
                String disValue = currentValue = ancestorValue ? propertyValue.value : "";
                if (ancestorValue && values != null && currentValue.startsWith("{|") && currentValue.endsWith("|}") && (index = values.indexOf(currentValue.substring("{|".length(), currentValue.length() - "|}".length()))) >= 0) {
                    disValue = "{|" + values.get(index) + "|}";
                }
                StringBuffer options = new StringBuffer();
                int maxdisplayvaluelength = 150;
                if (attributes.containsKey("customstyle")) {
                    customstyle = attributes.get("customstyle").toString();
                    if (!customstyle.endsWith(";")) {
                        customstyle = customstyle + ";";
                    }
                } else {
                    customstyle = "";
                }
                String customonchange = attributes.containsKey("customonchange") ? attributes.get("customonchange").toString() : "propertyChange();";
                if (ancestorValue && values != null && currentValue.startsWith("{|") && currentValue.endsWith("|}") && !values.contains(currentValue.substring("{|".length(), currentValue.length() - "|}".length()))) {
                    disValue = "?-" + disValue + "-?";
                }
                options.append("<option value=\"").append(ancestorValue ? propertyValue.value : "").append("\">").append(disValue).append("</option>");
                boolean selected = false;
                for (int i = 0; i < values.size(); ++i) {
                    String value;
                    String displayvalue = value = (String)values.get(i);
                    if (value != null && value.length() > 0) {
                        if (value.equals(propertyValue.value)) {
                            selected = true;
                            options.append("<option value='").append(value).append("' selected>").append(displayvalue).append("</option>");
                        } else {
                            options.append("<option value='").append(value).append("'>").append(displayvalue).append("</option>");
                        }
                    }
                    if (displayvalue == null || displayvalue.length() < 20) continue;
                    maxdisplayvaluelength = displayvalue.length() * 8;
                }
                if (!(ancestorValue || "".equals(propertyValue.value) || selected)) {
                    options.append("<option value='").append(propertyValue.value).append("' selected>").append("?-" + propertyValue.value + "-?").append("</option>");
                }
                output.append("<select name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" style=\"width:").append(maxdisplayvaluelength).append("px; ").append(ancestorValue ? "color:blue;" : "").append(customstyle).append("\" onchange=\"this.style.color='black';checkEvent( this );").append(customonchange).append("\">");
                output.append(options);
                output.append("</select>");
            } else {
                output.append("<input type=\"text\" name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" ").append("style=\"").append(ancestorValue ? "color:blue;" : "").append("\" ").append("onchange=\"").append("this.style.color='black';checkEvent( this ); ").append("\" ").append("value=\"").append(propertyValue).append("\"/>");
            }
        }
        catch (Exception e) {
            output.append("ERROR: " + e.getMessage());
        }
        return output.toString();
    }
}

