/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.forms;

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class FormBuilderFieldAjaxSave
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53852 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "FormBuilderHandler");
        String props = ajaxResponse.getRequestParameter("fieldproperties", "");
        if (props.length() > 0) {
            String fieldid = ajaxResponse.getRequestParameter("fieldid", "");
            if (fieldid.length() > 0) {
                DataSet test = this.getQueryProcessor().getPreparedSqlDataSet("SELECT fieldid FROM field WHERE fieldid=?", new Object[]{fieldid});
                if (test != null && test.size() == 0) {
                    String description = ajaxResponse.getRequestParameter("description", "");
                    String fieldlabel = ajaxResponse.getRequestParameter("fieldlabel", "");
                    String defaulteditor = ajaxResponse.getRequestParameter("defaulteditor", "text");
                    String fieldtype = ajaxResponse.getRequestParameter("fieldtype", "String");
                    String editable = ajaxResponse.getRequestParameter("editable", "");
                    editable = editable.equalsIgnoreCase("yes") || editable.equalsIgnoreCase("true") ? "Y" : "N";
                    try {
                        PropertyList fieldprop = new PropertyList(new JSONObject(props));
                        if (fieldprop.containsKey("labels")) {
                            fieldprop.remove("labels");
                        }
                        if (fieldprop.containsKey("fieldid")) {
                            fieldprop.remove("fieldid");
                        }
                        if (fieldprop.containsKey("datatype")) {
                            fieldprop.remove("datatype");
                        }
                        if (fieldprop.containsKey("type")) {
                            fieldprop.remove("type");
                        }
                        props = fieldprop.toXMLString();
                        HashMap<String, String> actionProps = new HashMap<String, String>();
                        actionProps.put("keyid1", fieldid);
                        actionProps.put("fielddesc", description);
                        actionProps.put("fieldlabel", fieldlabel);
                        actionProps.put("defaulteditor", defaulteditor);
                        actionProps.put("fieldtype", fieldtype);
                        actionProps.put("editableflag", editable);
                        actionProps.put("fieldobject", props);
                        actionProps.put("sdcid", "LV_Field");
                        actionProps.put("copies", "1");
                        try {
                            this.getActionProcessor().processAction("AddSDI", "1", actionProps);
                        }
                        catch (Exception e3) {
                            ajaxResponse.setError(this.getTranslationProcessor().translate("Failed to add field because:") + " " + this.getTranslationProcessor().translate(e3.getMessage()));
                        }
                        ajaxResponse.addCallbackArgument("fieldid", fieldid);
                        ajaxResponse.addCallbackArgument("editableflag", editable);
                        ajaxResponse.addCallbackArgument("defaulteditor", defaulteditor);
                        ajaxResponse.addCallbackArgument("fieldtype", fieldtype);
                    }
                    catch (Exception e1) {
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Failed to create properties."));
                    }
                } else {
                    HashMap<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("fieldid", fieldid);
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Field with Id [fieldid] already exists.", valueMap));
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No field Id provided."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

