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
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class TabColumnEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertylist, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        PropertyList collectionParentPL;
        String datasetname = (String)attributes.get("datasetname");
        PropertyList propertyList = collectionParentPL = propertyValue.getParentPropertyList() != null && propertyValue.getParentPropertyList().getParentPropertyValue() != null ? propertyValue.getParentPropertyList().getParentPropertyValue().getParentPropertyList() : null;
        if (collectionParentPL != null && "dataitem".equals(collectionParentPL.getProperty("type"))) {
            datasetname = "dataitem";
        } else if (collectionParentPL != null && "dataset".equals(collectionParentPL.getProperty("type"))) {
            datasetname = "dataset";
        }
        String sdcid = (String)attributes.get("sdcid");
        String tableid = (String)attributes.get("tableid");
        String idfrom = (String)attributes.get("idfromrootpropid");
        if ((sdcid == null || sdcid.length() == 0) && topPropertylist != null) {
            sdcid = topPropertylist.getProperty("sdcid");
        }
        if ((sdcid == null || sdcid.length() == 0) && topPropertylist != null) {
            if (tableid != null && tableid.length() > 0) {
                sdcid = tableid.equals("study") ? "StudySDC" : tableid;
            } else if (idfrom != null && idfrom.length() > 0) {
                sdcid = topPropertylist.getProperty(idfrom).equals("study") ? "StudySDC" : topPropertylist.getProperty(idfrom);
            }
        }
        StringBuffer out = new StringBuffer("<table cellpadding=\"0\" cellspacing=\"0\"><tr><td><input onchange=\"propertyChange()\" onkeyup=\"propertyChange()\" type=\"text\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"width:250px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\" value=\"" + propertyValue + "\"/></td>");
        Button b = new Button(pageContext);
        b.setAction("lookupdatasetcolumn( '" + fieldName + "','" + datasetname + "','" + sdcid + "')");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append("<td>" + b.getHtml() + "</td></tr></table>");
        return out.toString();
    }
}

