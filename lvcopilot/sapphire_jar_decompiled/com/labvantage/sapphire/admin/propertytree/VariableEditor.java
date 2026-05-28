/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.admin.propertytree.ListEditor;
import com.labvantage.sapphire.admin.propertytree.StringEditor;
import com.labvantage.sapphire.admin.propertytree.TypeSimple;
import com.labvantage.sapphire.pageelements.controls.Button;
import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import org.json.JSONObject;
import sapphire.util.Browser;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyValue;

public class VariableEditor
implements TypeSimple {
    @Override
    public String getEditor(String fieldName, PropertyValue propertyValue, PropertyList topPropertyList, boolean ancestorValue, HashMap attributes, PageContext pageContext, boolean debug) {
        StringBuffer out = new StringBuffer();
        boolean readonly = false;
        if (pageContext != null && (pageContext.getRequest().getParameter("steptypeid") == null || pageContext.getRequest().getParameter("steptypeid").length() == 0)) {
            readonly = true;
        }
        if (attributes.containsKey("readonly")) {
            readonly = attributes.get("readonly").toString().equalsIgnoreCase("Y");
        }
        if (readonly) {
            HashMap<String, String> stratt = new HashMap<String, String>();
            stratt.put("readonly", "Y");
            if (attributes.containsKey("customonchange")) {
                stratt.put("customonchange", (String)attributes.get("customonchange"));
            }
            if (attributes.containsKey("onblur")) {
                stratt.put("onblur", (String)attributes.get("onblur"));
            }
            if (attributes.containsKey("readonly")) {
                stratt.put("readonly", (String)attributes.get("readonly"));
            }
            if (attributes.containsKey("customstyle")) {
                stratt.put("customstyle", (String)attributes.get("customstyle"));
            }
            out.append("<table cellpadding=0 cellspacing=0 border=0><tr><td style=\"padding-top:3px;\">");
            out.append(new StringEditor().getEditor(fieldName, propertyValue, topPropertyList, ancestorValue, stratt, pageContext, debug));
            out.append("</tr></td></table>");
        } else {
            HashMap<String, String> listatt = new HashMap<String, String>();
            listatt.put("editable", "Y");
            listatt.put("editmode", "simple");
            listatt.put("textentry", "N");
            if (attributes.containsKey("values")) {
                listatt.put("values", (String)attributes.get("values"));
            } else {
                listatt.put("callbackmethod", "getVariableList");
            }
            StringBuffer cba = new StringBuffer();
            cba.append("{");
            if (!attributes.containsKey("subvariables") || !attributes.get("subvariables").toString().equalsIgnoreCase("Y")) {
                cba.append("subvariables:false");
            } else {
                cba.append("subvariables:true");
            }
            if (attributes.containsKey("rootvariables") && attributes.get("rootvariables").toString().equalsIgnoreCase("Y")) {
                cba.append(",rootvariables:true");
            } else {
                cba.append(",rootvariables:false");
            }
            if (attributes.containsKey("basedon") && attributes.get("basedon").toString().length() > 0) {
                try {
                    cba.append(",basedon:").append(new PropertyList(new JSONObject(attributes.get("basedon").toString())).toJSONString(false));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (!attributes.containsKey("prefix") || !attributes.get("prefix").toString().equalsIgnoreCase("Y")) {
                cba.append(",prefix:false");
            } else {
                cba.append(",prefix:true");
            }
            if (attributes.containsKey("validtypes")) {
                cba.append(",types:'").append(attributes.get("validtypes").toString()).append("'");
            } else {
                cba.append(",types:''");
            }
            if (attributes.containsKey("sdcid")) {
                cba.append(",sdcid:'").append(attributes.get("sdcid").toString()).append("'");
            } else {
                String sdc;
                String string = sdc = topPropertyList == null ? null : topPropertyList.findProperty("sdcid");
                if (sdc != null && sdc.length() > 0) {
                    cba.append(",sdcid:'").append(sdc).append("'");
                }
            }
            cba.append("}");
            out.append("<script>" + fieldName + "_callbackarguments=").append(cba.toString()).append(";</script>");
            listatt.put("callbackarguments", cba.toString());
            if (attributes.containsKey("customonchange")) {
                listatt.put("customonchange", (String)attributes.get("customonchange"));
            }
            if (attributes.containsKey("onblur")) {
                listatt.put("onblur", (String)attributes.get("onblur"));
            }
            if (attributes.containsKey("disabled")) {
                listatt.put("disabled", (String)attributes.get("disabled"));
            }
            if (attributes.containsKey("readonly")) {
                listatt.put("readonly", (String)attributes.get("readonly"));
            }
            if (attributes.containsKey("customstyle")) {
                listatt.put("customstyle", (String)attributes.get("customstyle"));
            }
            out.append("<table cellpadding=0 cellspacing=0 border=0><tr><td style=\"padding-top:3px;\">");
            out.append(new ListEditor().getEditor(fieldName, propertyValue, topPropertyList, ancestorValue, listatt, pageContext, debug));
            Button btn = new Button(pageContext);
            btn.setImg("WEB-CORE/images/png/Add.png");
            btn.setAction("if(typeof(addVariable)!='undefined'){addVariable('" + fieldName + "')}");
            btn.setId("btn_" + fieldName + "_add");
            Browser b = new Browser(pageContext);
            btn.setStyle("height:22px;" + (b.isIE() ? "padding:0;margin:0;" : ""));
            btn.setTip("Add Variable");
            out.append("</td><td>");
            out.append(btn.getHtml());
            out.append("</tr></td></table>");
        }
        return out.toString();
    }
}

