/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.taskdefpainter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class TaskDefItemPropsAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "TaskDefHandler");
        String mode = ajaxResponse.getRequestParameter("mode", "get");
        String props = ajaxResponse.getRequestParameter("properties", "");
        try {
            if (mode.equalsIgnoreCase("get")) {
                PropertyList taskprops = props.length() > 0 ? new PropertyList(new JSONObject(props)) : new PropertyList();
                ajaxResponse.addCallbackArgument("properties", taskprops.toXMLString());
            } else {
                PropertyList taskprops = new PropertyList();
                if (props.length() > 0) {
                    taskprops.setPropertyList(props);
                }
                ajaxResponse.addCallbackArgument("properties", taskprops.toJSONString());
            }
            ajaxResponse.addCallbackArgument("mode", mode);
        }
        catch (Exception e) {
            ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
        }
        ajaxResponse.print();
    }
}

