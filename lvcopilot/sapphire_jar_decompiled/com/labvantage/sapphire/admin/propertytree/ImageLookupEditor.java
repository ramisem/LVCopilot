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
import sapphire.util.HttpUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class ImageLookupEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldname, PropertyValue propertyvalue, PropertyList toppropertylist, boolean ancestorvalue, HashMap attributes, PageContext pageContext, boolean debug) {
        String category;
        boolean imageRef = false;
        boolean svg = false;
        boolean svgcontent = false;
        String string = category = attributes.get("imagerefcategory") != null ? attributes.get("imagerefcategory").toString() : "";
        if (attributes.get("imageref") != null && (attributes.get("imageref").toString().equalsIgnoreCase("Y") || attributes.get("imageref").toString().equalsIgnoreCase("S") || attributes.get("imageref").toString().equalsIgnoreCase("I") || attributes.get("imageref").toString().equalsIgnoreCase("C"))) {
            imageRef = true;
            if (attributes.get("imageref").toString().equalsIgnoreCase("S")) {
                svg = true;
            } else if (attributes.get("imageref").toString().equalsIgnoreCase("C")) {
                svg = true;
                svgcontent = true;
            }
        }
        Button icon = new Button(pageContext);
        if (imageRef) {
            if (svg || svgcontent) {
                if (svgcontent) {
                    icon.setAction("sapphire.lookup.image.open('" + fieldname + "','', true, true, true, true, '" + category + "' )");
                } else {
                    icon.setAction("sapphire.lookup.image.open('" + fieldname + "','', true, true, true, false, '" + category + "' )");
                }
            } else {
                icon.setAction("sapphire.lookup.image.open('" + fieldname + "','', true, true, false, false, '" + category + "' )");
            }
        } else {
            icon.setAction("sapphire.lookup.image.open('" + fieldname + "' )");
        }
        icon.setImg("rc?command=image&image=" + (svg || svgcontent ? "FlatBlackStar" : "EmoticonSmile") + "&size=16");
        icon.setMargin("none");
        icon.setStyle("margin-top:2px");
        icon.setHighlight("false");
        Button file = null;
        if (!svg && !svgcontent) {
            file = new Button(pageContext);
            file.setAction("lookupwebfile( '" + fieldname + "', '', '" + HttpUtil.getAppRoot(pageContext.getServletContext()) + "','')");
            file.setImg("rc?command=image&image=Folder&size=16");
            file.setMargin("none");
            file.setStyle("margin-top:2px");
            file.setHighlight("false");
        }
        StringBuffer output = new StringBuffer();
        output.append("<table style=\"display:inline-block\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\"><tr><td>");
        String imageurl = "";
        if (propertyvalue.toString().length() > 0) {
            String actualValue;
            String string2 = actualValue = ancestorvalue ? propertyvalue.toString().substring("{|".length(), propertyvalue.toString().length() - "|}".length()) : propertyvalue.toString();
            imageurl = svg || svgcontent ? "rc?command=image&svgimage=" + actualValue + "&size=32" : (actualValue.startsWith("rc?command=image") || actualValue.endsWith(".png") || actualValue.endsWith(".svg") || actualValue.endsWith(".jpg") || actualValue.endsWith(".gif") ? actualValue : "rc?command=image&image=" + actualValue + "&size=32");
            output.append("<div id=\"").append(fieldname).append("_preview\" style=\"display:inline-block;height:32px;width:32px;background-repeat:none;background-position:center;background-size: cover;background-image:url('").append(imageurl).append("')\"></div>");
        } else {
            output.append("<div id=\"").append(fieldname).append("_preview\" style=\"display:inline-block;height:32px;width:32px;background-repeat:none;background-position:center;background-size: cover;\"></div>");
        }
        output.append("</td><td>");
        output.append("<input type=\"text\" name=\"").append(fieldname).append("\" id=\"").append(fieldname).append("\" style=\"width:250px ").append(ancestorvalue ? "; color:blue" : "").append("\" onchange=\"propertyChange();this.style.color='black';checkEvent( this );document.getElementById('").append(fieldname).append("_preview').style.backgroundImage='url(' + (this.value.startsWith( 'rc?command=image' ) || this.value.endsWith( '.png' ) || this.value.endsWith( '.svg' ) || this.value.endsWith( '.jpg' )  || this.value.endsWith( '.gif' )? this.value : ").append(svg ? "('rc?command=image&svgimage=' + this.value + '&size=32')" : "('rc?command=image&image=' + this.value + '&size=32')").append(") +  ')';\" value=\"").append(propertyvalue).append("\"/>");
        if (svg || svgcontent) {
            output.append("</td><td>").append(icon.getHtml()).append("</td></tr></table>");
        } else {
            output.append("</td><td>").append(icon.getHtml()).append("&nbsp;").append(imageRef ? "" : file.getHtml()).append("</td></tr></table>");
        }
        return output.toString();
    }
}

