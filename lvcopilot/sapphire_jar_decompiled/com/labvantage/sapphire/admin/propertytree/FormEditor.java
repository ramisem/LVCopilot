/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.documents.Document;
import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.forms.FormBuilder;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.Browser;
import sapphire.util.Logger;
import sapphire.xml.DOMUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class FormEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        boolean devMode;
        StringBuffer out = new StringBuffer("");
        out.append("<script>");
        out.append("function _updateFormThumbnail(el){");
        out.append("var callprops = {'form':el.value,'field':el.id};");
        out.append("sapphire.ajax.callClass('com.labvantage.sapphire.admin.propertytree.FormEditorAjaxRender', '_updateFormThumbnail_Callback', callprops);");
        out.append("}");
        out.append("function _updateFormThumbnail_Callback(html, field){");
        out.append("document.getElementById(field + '_div').innerHTML = html;");
        out.append("}");
        out.append("function _formCallback(sFieldId, sLayout, sProperties, lEmbedded, sObject, oProps){");
        out.append("var oEl = document.getElementById(sFieldId);");
        out.append("if(oEl!=null){");
        out.append("if (oEl.nodeName=='TEXTAREA'){");
        out.append("oEl.innerText = sObject;");
        out.append("}else{");
        out.append("oEl.value = sObject;");
        out.append("}");
        out.append("sapphire.events.fireEvent(oEl, 'onchange');;");
        out.append("}");
        out.append("}");
        out.append("function _openForm(sFieldName){");
        out.append("var oPropList = sapphire.util.propertyList.create();");
        out.append("oPropList.set('showtitle','Y');");
        out.append("oPropList.set('callback','_formCallback');");
        out.append("oPropList.set('fieldids',sFieldName);");
        out.append("oPropList.set('formlet','N');");
        FormBuilder.Mode mode = FormBuilder.Mode.SIMPLEFORM;
        if (attributes.containsKey("mode") && attributes.get("mode").toString().length() > 0) {
            try {
                mode = FormBuilder.Mode.valueOf(attributes.get("mode").toString().toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        out.append("oPropList.set('mode','").append(mode.toString().toLowerCase()).append("');");
        out.append("oPropList.set('formobject',sapphire.util.url.encodeComponent(document.getElementById(sFieldName).value));");
        out.append("oPropList.set('xmlmode','Y');");
        out.append("oPropList.set('viewonly','N');");
        out.append("oPropList.set('showbuttons','Y');");
        out.append("oPropList.set('showobjects','Y');");
        out.append("oPropList.set('embedded','N');");
        out.append("oPropList.set('encodedformobject','Y');");
        out.append("sapphire.lookup.util.openWindow('formbuilder','LabVantage Form Builder','rc?command=page&page=LV_FormBuilder',800, 600, false, oPropList, true, true );");
        out.append("}");
        out.append("</script>");
        PropertyList form = new PropertyList();
        if (propertyValue.value.length() > 0) {
            try {
                form.setPropertyList(propertyValue.value);
            }
            catch (Exception e) {
                Logger.logError("Could not parse properties");
            }
        }
        StringBuffer tnhtml = new StringBuffer();
        Browser br = new Browser(pageContext);
        tnhtml.append("<div id=\"").append(fieldName).append("_div\" style=\"min-height:600px;min-width:350px;zoom:").append(br.isIE() ? "15%" : "30%;border:solid 1px black;webkit-box-shadow: 0px 0px 5px #044;margin:5px 5px 5px 5px").append(";\">");
        if (propertyValue.value.length() > 0) {
            try {
                tnhtml.append(Document.generatePageThumbnail(new ConnectionProcessor(pageContext).getSapphireConnection(), form));
            }
            catch (Exception e) {
                tnhtml.append("&nbsp;");
            }
        } else {
            tnhtml.append("&nbsp;");
        }
        tnhtml.append("</div>");
        propertyValue.value = DOMUtil.convertChars(propertyValue.value);
        ConfigurationProcessor config = new ConfigurationProcessor(pageContext);
        try {
            devMode = config.getSysConfigProperty("devmode", "N").equalsIgnoreCase("Y");
        }
        catch (Exception e) {
            devMode = false;
        }
        if (br.isIE()) {
            out.append("<table cellpadding=\"0\" cellspacing=\"0\" style=\"table-layout:fixed;width:400px;padding: 4px 4px 4px 4px;\"><tr>");
        } else {
            out.append("<table cellpadding=\"1\" cellspacing=\"0\"><tr>");
        }
        if (br.isIE()) {
            out.append("<td valign=center style=\"border:solid 1px black;width:180px;\">");
        } else {
            out.append("<td valign=center>");
        }
        out.append(tnhtml);
        if (br.isIE()) {
            // empty if block
        }
        out.append("</td>");
        if (br.isIE()) {
            out.append("<td valign=center style=\"padding-left:5px;\">");
        } else {
            out.append("<td valign=center>");
        }
        Button b = new Button(pageContext);
        b.setAction("_openForm('" + fieldName + "');");
        b.setImg("WEB-CORE/elements/images/ellipsisblank.gif");
        b.setMargin("none");
        b.setHighlight("false");
        out.append(b.getHtml());
        out.append("<input type=\"hidden\" onchange=\"_updateFormThumbnail(this);propertyChange();\" onkeyup=\"propertyChange()\" name=\"").append(fieldName).append("\" id=\"").append(fieldName).append("\" style=\"display:").append(devMode ? "inline" : "none").append(";").append(ancestorValue ? "; color:blue" : "").append("\" onchange=\"this.style.color='black';checkEvent( this );\"").append("").append(" value=\"").append(propertyValue).append("\">");
        out.append("</td>");
        out.append("</tr></table>");
        return out.toString();
    }
}

