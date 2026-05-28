/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.EditorUtil;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class YesNoEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyvalue, PropertyList toppropertylist, boolean ancestorvalue, HashMap attributes, PageContext pageContext, boolean debug) {
        TranslationProcessor tp = pageContext != null ? new TranslationProcessor(pageContext) : null;
        StringBuffer output = new StringBuffer();
        String customstyle = attributes.containsKey("customstyle") ? attributes.get("customstyle").toString() : "";
        String customonchange = attributes.containsKey("customonchange") ? attributes.get("customonchange").toString() : "propertyChange();";
        output.append("<select name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" style=\"width:200px;").append(ancestorvalue ? "color:blue" : "").append(customstyle).append("\" onchange=\"").append(customonchange).append(";this.style.color='black';checkEvent( this )\">");
        output.append("<option value=\"").append(ancestorvalue ? propertyvalue.value : "").append("\">").append(ancestorvalue ? (propertyvalue.value.equals("{|Y|}") ? "{|Yes|}" : "{|No|}") : "").append("</option>");
        if (propertyvalue.value.equalsIgnoreCase("Y")) {
            output.append("<option value=\"Y\" selected>").append(tp != null ? tp.translate("Yes") : "Yes").append("</option>");
            output.append("<option value=\"N\">").append(tp != null ? tp.translate("No") : "No").append("</option>");
        } else if (propertyvalue.value.equalsIgnoreCase("N")) {
            output.append("<option value=\"Y\">").append(tp != null ? tp.translate("Yes") : "Yes").append("</option>");
            output.append("<option value=\"N\" selected>").append(tp != null ? tp.translate("No") : "No").append("</option>");
        } else {
            output.append("<option value=\"Y\">").append(tp != null ? tp.translate("Yes") : "Yes").append("</option>");
            output.append("<option value=\"N\">").append(tp != null ? tp.translate("No") : "No").append("</option>");
        }
        output.append("</select>");
        String pastevalue = (String)attributes.get("pastevalue");
        if (pastevalue != null && pastevalue.startsWith("Y")) {
            output.append(EditorUtil.showPasteButton(fieldName, attributes, pageContext));
        }
        return output.toString();
    }
}

