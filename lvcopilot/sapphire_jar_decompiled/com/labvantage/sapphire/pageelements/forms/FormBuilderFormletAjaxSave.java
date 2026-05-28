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

import com.labvantage.sapphire.pageelements.controls.RichTextEditor;
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

public class FormBuilderFormletAjaxSave
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 53852 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "FormBuilderHandler");
        String props = ajaxResponse.getRequestParameter("formproperties", "");
        if (props.length() > 0) {
            String layout = ajaxResponse.getRequestParameter("formlayout", "");
            if (layout.length() > 0) {
                String formletid = ajaxResponse.getRequestParameter("formletid", "");
                if (formletid.length() > 0) {
                    String formletversion = ajaxResponse.getRequestParameter("formletversion", "1");
                    DataSet test = this.getQueryProcessor().getPreparedSqlDataSet("SELECT formletid FROM formlet WHERE formletid=?", new Object[]{formletid});
                    if (test != null && test.size() == 0) {
                        String description = ajaxResponse.getRequestParameter("description", "");
                        String editable = ajaxResponse.getRequestParameter("editable", "");
                        editable = editable.equalsIgnoreCase("yes") || editable.equalsIgnoreCase("true") ? "Y" : "N";
                        try {
                            PropertyList formletprop = new PropertyList(new JSONObject(props));
                            props = formletprop.toXMLString();
                            try {
                                String xhtml = RichTextEditor.convertToXHTML(layout);
                                HashMap<String, String> actionProps = new HashMap<String, String>();
                                actionProps.put("keyid1", formletid);
                                actionProps.put("keyid2", formletversion);
                                actionProps.put("formletdesc", description);
                                actionProps.put("editableflag", editable);
                                actionProps.put("formletobjects", props);
                                actionProps.put("formletlayout", xhtml);
                                actionProps.put("sdcid", "LV_Formlet");
                                actionProps.put("copies", "1");
                                try {
                                    this.getActionProcessor().processAction("AddSDI", "1", actionProps);
                                }
                                catch (Exception e3) {
                                    ajaxResponse.setError(this.getTranslationProcessor().translate("Failed to add formlet because:") + " " + this.getTranslationProcessor().translate(e3.getMessage()));
                                }
                                ajaxResponse.addCallbackArgument("formletid", formletid);
                            }
                            catch (Exception e2) {
                                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not convert layout to XML."));
                            }
                        }
                        catch (Exception e1) {
                            ajaxResponse.setError(this.getTranslationProcessor().translate("Failed to create properties."));
                        }
                    } else {
                        HashMap<String, String> valueMap = new HashMap<String, String>();
                        valueMap.put("formletid", formletid);
                        ajaxResponse.setError(this.getTranslationProcessor().translate("Formlet with Id [formletid] already exists.", valueMap));
                    }
                } else {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("No formlet Id provided."));
                }
            } else {
                ajaxResponse.setError(this.getTranslationProcessor().translate("No formlet content."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

