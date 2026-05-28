/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 *  org.apache.commons.codec.digest.DigestUtils
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.EditorUtil;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import org.apache.commons.codec.digest.DigestUtils;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class StringEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        String transContext;
        String style;
        String regexerror;
        String regex;
        String width;
        String type = (String)attributes.get("password");
        type = type != null && type.equals("Y") ? "password" : "text";
        String maxlength = (String)attributes.get("maxlength");
        if (maxlength == null) {
            maxlength = "";
        }
        if ((width = (String)attributes.get("width")) == null || width.length() == 0) {
            width = "200px";
        }
        if ((regex = (String)attributes.get("regex")) == null) {
            regex = "";
        }
        if ((regexerror = (String)attributes.get("regexerror")) == null || regexerror.length() == 0) {
            regexerror = "Invalid value entered. Please try again.";
        }
        propertyValue.value = DOMUtil.convertChars(propertyValue.value);
        String customonchange = attributes.containsKey("customonchange") ? attributes.get("customonchange").toString() : "propertyChange();";
        String onblur = "";
        if (attributes.containsKey("onblur")) {
            onblur = " onblur=\"" + attributes.get("onblur") + "\" ";
        }
        String disabled = "";
        if (attributes.containsKey("disabled") && attributes.get("disabled").toString().equalsIgnoreCase("Y")) {
            disabled = " disabled=\"true\" ";
        } else if (attributes.containsKey("readonly") && attributes.get("readonly").toString().equalsIgnoreCase("Y")) {
            disabled = disabled + " readonly=\"true\" ";
        }
        String string = style = attributes.containsKey("customstyle") ? attributes.get("customstyle") + ";" : "";
        if (!width.endsWith("px") && !width.endsWith("%")) {
            width = width + "px";
        }
        StringBuffer output = new StringBuffer();
        String string2 = transContext = "Y".equals(propertyValue.getAttribute("translate")) ? "W" : propertyValue.getAttribute("translate");
        if (attributes.containsKey("longstring") && attributes.get("longstring").toString().equalsIgnoreCase("Y")) {
            output.append("<textarea rows=\"1\" ").append(onblur).append(disabled).append("  onchange=\"").append(customonchange).append("\" ").append("  onkeyup=\"").append(customonchange).append("\" ").append("type=\"").append(type).append("\" maxlength=\"").append(maxlength).append("\" name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" style=\"").append(style).append("overflow:hidden;height:21px;width:").append(width).append(";").append(ancestorValue ? "color=blue;" : "").append("\"   =\"this.style.color='black'; ").append(regex.length() > 0 ? "if ( this.value.length > 0 && this.value.match( " + regex + " ) == null ) { sapphire.alert( '" + regexerror + "' ); this.value = '';this.focus();return false;}" : "").append("\">").append(propertyValue).append("</textarea>");
        } else {
            output.append("<input ").append(onblur).append(disabled).append("  onkeyup=\"").append(customonchange + (transContext.length() > 0 ? ";showSuggestion( {'columns':[{'columnid':'textid','width':'400'}],'tableid':'transmaster','sdcid':'transmaster'} )" : "")).append("\" ").append("type=\"").append(type).append("\" maxlength=\"").append(maxlength).append("\" name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" style=\"").append(style).append("width:").append(width).append(";").append(ancestorValue ? "color:blue;" : "").append(transContext.length() > 0 ? "border:1px solid green" : "").append("\" onchange=\"").append(customonchange).append("; ").append("this.style.color='black';checkEvent( this ); ").append(regex.length() > 0 ? "if ( this.value.length > 0 && this.value.match( " + regex + " ) == null ) { sapphire.alert( '" + regexerror + "' ); this.value = '';this.focus();return false;}" : "");
            String value = propertyValue.value;
            if (type.equals("password") && value.length() > 0) {
                String hashcode = "HEX_" + DigestUtils.md5Hex((String)value);
                pageContext.getSession().setAttribute(hashcode, (Object)value);
                value = hashcode;
            }
            output.append("\" value=\"").append(value).append("\"/>");
        }
        String pastevalue = (String)attributes.get("pastevalue");
        if (pastevalue != null && pastevalue.startsWith("Y")) {
            output.append(EditorUtil.showPasteButton(fieldName, attributes, pageContext));
        }
        return output.toString();
    }
}

