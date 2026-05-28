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
import sapphire.accessor.TranslationProcessor;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;
import sapphire.xml.PropertyValue;

public class GAPEditor
implements TypeSimple {
    protected String pageid;
    protected String readonly;
    protected String columnsPropertyid;
    protected String width;
    protected String height;

    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        this.initEditorCommon(attributes);
        PropertyListCollection columns = topPropertyList.getCollection(this.columnsPropertyid);
        StringBuilder variablelistSb = new StringBuilder();
        if (columns != null && columns.size() > 0) {
            for (int i = 0; i < columns.size(); ++i) {
                variablelistSb.append(";" + columns.getPropertyList(i).getProperty("columnid"));
            }
        }
        String variablelist = variablelistSb.length() > 1 ? variablelistSb.substring(1) : "";
        StringBuffer out = new StringBuffer("");
        out.append("<table cellpadding=\"0\" cellspacing=\"0\">");
        this.renderEditButtonRow(out, "actionblock", "Edit Action Block", fieldName, variablelist, pageContext);
        String flowchartid = "flowchart_" + fieldName;
        String elementid = "actionblock_" + fieldName;
        out.append("<tr><td><div name=\"flowchart\" id=\"" + flowchartid + "\" valuefieldid=\"" + fieldName + "\" style=\"overflow:auto;display:block;height:" + this.height + "px;width:" + this.width + "px;border:1px solid gray;background-color:white;padding-left:10px\"></div></td>");
        out.append("</tr></table>");
        out.append("<script>sapphire.gwt.addGWTElement( \"actionblock\", \"" + elementid + "\", {'flowchartid':'" + flowchartid + "'} );");
        out.append("</script>");
        out.append("<textarea" + (!"N".equals(this.readonly) ? " readonly" : "") + " onchange=\"propertyChange();onActionBlockXMLChange( '" + fieldName + "' );\" onkeyup=\"propertyChange()\" name=\"" + fieldName + "\" id=\"" + fieldName + "\" style=\"display:none;width:" + this.width + "px;height:" + this.height + "px " + (ancestorValue ? "; color:blue" : "") + "\" onchange=\"this.style.color='black';checkEvent( this )\">" + propertyValue.value.replaceAll("!]!]!>", "]]>") + "</textarea>");
        return out.toString();
    }

    protected void initEditorCommon(HashMap attributes) {
        this.pageid = (String)attributes.get("pageid");
        this.readonly = (String)attributes.get("readonly");
        this.columnsPropertyid = (String)attributes.get("columnspropertyid");
        if (this.columnsPropertyid == null || this.columnsPropertyid.length() == 0) {
            this.columnsPropertyid = "columns";
        }
        this.width = "500";
        if (attributes.containsKey("width")) {
            this.width = attributes.get("width").toString();
        }
        this.height = "250";
        if (attributes.containsKey("height")) {
            this.height = attributes.get("height").toString();
        }
        if (this.pageid == null || this.pageid.length() == 0) {
            this.pageid = "LV_ActionBlockEditor";
        }
    }

    protected void renderEditButtonRow(StringBuffer out, String mode, String buttontext, String fieldName, String variablelist, PageContext pageContext) {
        if (!"Y".equals(this.readonly)) {
            String url = "rc?command=page&page=" + this.pageid + "&mode=" + mode + "&layoutscrolling=N&fieldid=" + fieldName + "&variablelist=" + variablelist;
            Button b = new Button(pageContext);
            b.setAction("sapphire.ui.dialog.open(  '" + new TranslationProcessor(pageContext).translate("Edit Action Block Script") + "', '" + url + "', true, 1280, 800 );");
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

