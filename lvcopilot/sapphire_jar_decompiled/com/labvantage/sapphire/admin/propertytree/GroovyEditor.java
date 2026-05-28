/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.util.groovy.GroovyBindVariableRegister;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class GroovyEditor
implements TypeSimple {
    protected String readonly;
    protected String width;
    protected String height;
    protected String scripttype;
    protected String groovyvariablecode;

    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        this.initEditorCommon(attributes);
        StringBuffer out = new StringBuffer("");
        out.append("<table cellpadding=\"0\" cellspacing=\"0\">");
        this.renderEditButtonRow(out, "groovy", "javascript".equals(this.scripttype) ? "Edit Javascript" : "Edit Groovy", fieldName, pageContext);
        out.append("<tr><td><textarea" + (!"N".equals(this.readonly) ? " readonly" : "") + " onchange=\"propertyChange();onActionBlockXMLChange( '" + fieldName + "' );\" onkeyup=\"propertyChange()\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" wrap=\"off\" style=\"display:block;width:" + this.width + "px;height:" + this.height + "px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\">" + propertyValue.value.replaceAll("!]!]!>", "]]>") + "</textarea>");
        out.append("</td></tr></table>");
        return out.toString();
    }

    protected void initEditorCommon(HashMap attributes) {
        this.readonly = (String)attributes.get("readonly");
        this.scripttype = (String)attributes.get("scripttype");
        this.width = "500";
        if (attributes.containsKey("width")) {
            this.width = attributes.get("width").toString();
        }
        this.height = "100";
        if (attributes.containsKey("height")) {
            this.height = attributes.get("height").toString();
        }
        this.groovyvariablecode = (String)attributes.get("groovyvariablecode");
    }

    protected void renderEditButtonRow(StringBuffer out, String mode, String buttontext, String fieldName, PageContext pageContext) {
        if (!"Y".equals(this.readonly)) {
            Button b = new Button(pageContext);
            String url = "rc?command=file&file=WEB-CORE/pagetypes/actionblock/scripteditor.jsp&mode=" + mode + "&fieldid=" + fieldName + "&scripttype=" + this.scripttype;
            if ("javascript".equals(this.scripttype)) {
                b.setAction("sapphire.ui.dialog.open( 'Java Script Editor', '" + url + "', true, 700, 500 );");
            } else {
                if (this.groovyvariablecode != null && this.groovyvariablecode.length() > 0) {
                    url = url + "&variablelist=" + GroovyBindVariableRegister.getVariables(this.groovyvariablecode);
                }
                b.setAction("sapphire.ui.dialog.open( 'Groovy Editor', '" + url + "', true, 900, 600 );");
            }
            b.setImg("WEB-CORE/images/gif/Edit.gif");
            b.setText(buttontext);
            b.setId("OpenEvergreenGAPEditor");
            b.setMargin("none");
            b.setHighlight("false");
            b.setWidth("125px");
            b.setTip("Open Editor");
            out.append("<tr><td>" + b.getHtml() + "</td></tr>");
        }
    }
}

