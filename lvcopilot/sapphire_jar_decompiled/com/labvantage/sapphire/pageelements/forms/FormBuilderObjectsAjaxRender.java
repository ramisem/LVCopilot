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

import com.labvantage.sapphire.pageelements.forms.FormBuilder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class FormBuilderObjectsAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "FormBuilderHandler");
        String props = ajaxResponse.getRequestParameter("formproperties", "");
        if (props.length() > 0) {
            FormBuilder.Mode buildMode = FormBuilder.Mode.getMode(ajaxResponse.getRequestParameter("buildmode"));
            boolean isFormlet = ajaxResponse.getRequestParameter("formlet", "N").equalsIgnoreCase("Y");
            try {
                PropertyList formprop = new PropertyList(new JSONObject(props));
                try {
                    String objectsHtml = FormBuilder.getObjectsHtml(formprop, buildMode, isFormlet, this.getConnectionId(), this.getTranslationProcessor(), request);
                    ajaxResponse.addCallbackArgument("html", objectsHtml);
                }
                catch (Exception e) {
                    ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain objects HTML."));
                }
            }
            catch (Exception e2) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Could not obtain propertylist from string provided."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

