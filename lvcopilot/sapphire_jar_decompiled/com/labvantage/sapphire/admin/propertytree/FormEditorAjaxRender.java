/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.propertytree;

import com.labvantage.sapphire.modules.documents.Document;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class FormEditorAjaxRender
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String formprops = ajaxResponse.getRequestParameter("form", "");
        String fieldid = ajaxResponse.getRequestParameter("field", "");
        StringBuffer html = new StringBuffer();
        if (formprops.length() > 0) {
            try {
                PropertyList form;
                if (formprops.startsWith("{") && formprops.endsWith("}")) {
                    form = new PropertyList(new JSONObject(formprops));
                } else {
                    form = new PropertyList();
                    form.setPropertyList(formprops);
                }
                html.append(Document.generatePageThumbnail(this.getConnectionProcessor().getSapphireConnection(), form));
            }
            catch (Exception e) {
                html.append("&nbsp;");
            }
        } else {
            html.append("&nbsp;");
        }
        ajaxResponse.addCallbackArgument("html", html.toString());
        ajaxResponse.addCallbackArgument("field", fieldid);
        ajaxResponse.print();
    }
}

