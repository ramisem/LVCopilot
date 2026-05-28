/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.workflow.workflowdefpainter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class WorkflowDefItemPropsAjaxRender
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "WorkflowDefHandler");
        String mode = ajaxResponse.getRequestParameter("mode", "get");
        String props = ajaxResponse.getRequestParameter("properties", "");
        if (props.length() > 0) {
            try {
                if (mode.equalsIgnoreCase("get")) {
                    PropertyList workflowprops = new PropertyList(new JSONObject(props));
                    ajaxResponse.addCallbackArgument("properties", workflowprops.toXMLString());
                } else {
                    PropertyList workflowprops = new PropertyList();
                    workflowprops.setPropertyList(props);
                    ajaxResponse.addCallbackArgument("properties", workflowprops.toJSONString());
                }
                ajaxResponse.addCallbackArgument("mode", mode);
            }
            catch (Exception e) {
                ajaxResponse.setError(this.getTranslationProcessor().translate("Properties invalid."));
            }
        } else {
            ajaxResponse.setError(this.getTranslationProcessor().translate("No PropertyList string provided."));
        }
        ajaxResponse.print();
    }
}

