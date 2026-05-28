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

public class ValidationEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue validationrule, PropertyList toppropertylist, boolean ancestorvalue, HashMap attributes, PageContext pagecontext, boolean debug) {
        String mode = (String)attributes.get("mode");
        if ((mode == null || mode.length() == 0) && "maint".equals(pagecontext.getRequest().getParameter("ptreeid"))) {
            mode = "maint";
        }
        if ((mode == null || mode.length() == 0) && "Prompt".equals(pagecontext.getRequest().getParameter("ptreeid"))) {
            mode = "prompt";
        }
        String currentField = "";
        if (attributes.get("fieldid") != null) {
            currentField = (String)attributes.get("fieldid");
        }
        String currentFieldDataType = "";
        String dfdheight = "650";
        if (attributes.get("fielddatatype") != null && ((currentFieldDataType = (String)attributes.get("fielddatatype")).equals("number") || currentFieldDataType.equals("date"))) {
            dfdheight = "350";
        }
        String inputWidth = "DFD".equalsIgnoreCase(mode) ? "100" : "250";
        String popupWidth = "DFD".equalsIgnoreCase(mode) ? "950" : "450";
        String popupHeight = "DFD".equalsIgnoreCase(mode) ? dfdheight : "220";
        String customonchange = attributes.containsKey("customonchange") ? attributes.get("customonchange").toString() : "propertyChange();";
        if (customonchange.length() == 0) {
            customonchange = (String)attributes.get("onchange");
        }
        StringBuffer out = new StringBuffer("");
        out.append("<input onchange=\"" + customonchange + "\"" + customonchange + "\" onclick=\"" + customonchange + "\" type=\"text\" name=\"" + fieldname + "\" id=\"" + fieldname + "\"  value=\"" + validationrule + "\"/>");
        Button b = new Button(pagecontext);
        b.setAction("lookupvalidation('" + fieldname + "', '" + currentField + "', '" + currentFieldDataType + "', '" + mode + "', " + popupWidth + "," + popupHeight + ", " + (attributes.containsKey("advanced") ? attributes.get("advanced") : "false") + ");");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append(b.getHtml());
        return out.toString();
    }
}

