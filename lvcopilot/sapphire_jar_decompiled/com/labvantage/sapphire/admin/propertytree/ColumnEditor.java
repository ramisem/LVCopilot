/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.EditorUtil;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class ColumnEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue propertyvalue, PropertyList topPropertylist, boolean ancestorvalue, HashMap attributes, PageContext pagecontext, boolean debug) {
        PropertyList parentPropertyList;
        String tflinkid;
        Object ttosdcid;
        String nodeid;
        String tsdcid;
        String sdcid = "";
        String string = tsdcid = attributes.get("sdcid") == null ? "" : attributes.get("sdcid").toString();
        if (tsdcid.length() > 0 && !tsdcid.contains("[")) {
            sdcid = tsdcid;
        } else if (topPropertylist != null) {
            sdcid = topPropertylist.getProperty("sdcid");
        }
        if ("Y".equals(attributes.get("usenodeid")) && (nodeid = (String)attributes.get("nodeid")) != null && nodeid.length() > 0) {
            if (nodeid.endsWith(" Custom")) {
                nodeid = nodeid.substring(0, nodeid.length() - 7);
            } else if (nodeid.endsWith(" Product")) {
                nodeid = nodeid.substring(0, nodeid.length() - 8);
            } else if (nodeid.contains(" Comp ")) {
                nodeid = nodeid.substring(0, nodeid.indexOf(" Comp "));
            }
            sdcid = nodeid;
        }
        if (tsdcid.length() > 0 && tsdcid.contains("[") && tsdcid.contains("]")) {
            sdcid = EditorUtil.replaceTokens(tsdcid, topPropertylist);
            if (sdcid.length() == 0) {
                sdcid = EditorUtil.replaceTokens(tsdcid, propertyvalue.getParentPropertyList());
            }
            if (sdcid.length() == 0) {
                PropertyList parentPropertyList2 = propertyvalue.getParentPropertyList();
                if (parentPropertyList2 != null) {
                    parentPropertyList2 = parentPropertyList2.getParentPropertyValue().getParentPropertyList();
                }
                sdcid = EditorUtil.replaceTokens(tsdcid, parentPropertyList2);
            }
        }
        Object ttableid = attributes.get("tableid");
        String tableid = "";
        if (ttableid != null) {
            tableid = ttableid.toString().indexOf("[") > -1 && ttableid.toString().indexOf("]") > -1 ? EditorUtil.replaceTokens(ttableid.toString(), topPropertylist) : ttableid.toString();
        }
        Object tlinktype = attributes.get("linktype");
        String linktype = "";
        if (tlinktype != null) {
            if (tlinktype.toString().indexOf("[") > -1 && tlinktype.toString().indexOf("]") > -1) {
                linktype = EditorUtil.replaceTokens(tlinktype.toString(), topPropertylist);
            } else if (sdcid.length() == 0) {
                linktype = tlinktype.toString();
            }
        }
        String tosdcid = "";
        if (!linktype.equals("d") && (ttosdcid = attributes.get("tosdcid")) != null) {
            if (ttosdcid.toString().indexOf("[") > -1 && ttosdcid.toString().indexOf("]") > -1) {
                tosdcid = EditorUtil.replaceTokens(ttosdcid.toString(), topPropertylist);
            } else if (sdcid.length() == 0) {
                tosdcid = ttosdcid.toString();
            }
        }
        Object tlinkid = attributes.get("linkid");
        String linkid = "";
        if (tlinkid != null) {
            linkid = tlinkid.toString().indexOf("[") > -1 && tlinkid.toString().indexOf("]") > -1 ? EditorUtil.replaceTokens(tlinkid.toString(), topPropertylist) : tlinkid.toString();
        }
        if (linktype.equalsIgnoreCase("f") && (tflinkid = (String)attributes.get("flinkid")) != null && tflinkid.length() > 0) {
            linkid = tflinkid.indexOf("[") > -1 && tflinkid.indexOf("]") > -1 ? EditorUtil.replaceTokens(tflinkid, topPropertylist) : tlinkid.toString();
        }
        String loadlinks = "N".equals(attributes.get("loadlinks")) ? "N" : "Y";
        String loadprimary = "N".equals(attributes.get("loadprimary")) ? "N" : "Y";
        StringBuffer out = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td><input onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" type=\"text\" name=\"" + fieldname + "\" id=\"" + fieldname + "\" style=\"width:250px " + (ancestorvalue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyvalue + "\"/></td>");
        Button b = new Button(pagecontext);
        if (sdcid.length() == 0) {
            parentPropertyList = propertyvalue.getParentPropertyList();
            if (parentPropertyList != null) {
                parentPropertyList = parentPropertyList.getParentPropertyValue().getParentPropertyList();
            }
            if (parentPropertyList != null) {
                sdcid = parentPropertyList.getProperty("sdcid");
            }
        }
        if (sdcid.length() == 0) {
            parentPropertyList = propertyvalue.getParentPropertyList();
            if (parentPropertyList != null && parentPropertyList.getParentPropertyValue() != null) {
                parentPropertyList = parentPropertyList.getParentPropertyValue().getParentPropertyList();
            }
            if (parentPropertyList != null && parentPropertyList.getParentPropertyValue() != null) {
                parentPropertyList = parentPropertyList.getParentPropertyValue().getParentPropertyList();
            }
            if (parentPropertyList != null) {
                sdcid = parentPropertyList.getProperty("sdcid");
            }
        }
        b.setAction("lookupcolumn( '" + fieldname + "','" + sdcid + "', '" + tableid + "', '" + tosdcid + "', '" + linkid + "', '" + loadlinks + "', '" + loadprimary + "'," + (attributes.get("showtrackitem") != null && attributes.get("showsdialias").toString().equalsIgnoreCase("Y") ? "true" : "false") + "," + (attributes.get("showsdialias") != null && attributes.get("showsdialias").toString().equalsIgnoreCase("Y") ? "true" : "false") + " )");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append("<td>").append(b.getHtml()).append("</td></tr></table>");
        return out.toString();
    }
}

