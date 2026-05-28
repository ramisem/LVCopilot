/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.ajax.operations;

import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorDetail;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class ProcessAction
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90968 $";

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "ProcessActionHandler");
        String actionid = ajaxResponse.getRequestParameter("actionid");
        String actionversionid = ajaxResponse.getRequestParameter("actionversionid", "1");
        String actionclass = ajaxResponse.getRequestParameter("actionclass");
        HashMap actionProps = new HashMap();
        actionProps.putAll(ajaxResponse.getRequestParameters());
        try {
            if (actionid.length() > 0 && actionversionid.length() > 0) {
                if (!SecurityPolicyUtil.isActionPermitted(this.getConnectionid(), "ajax", actionid, actionProps)) throw new ActionException(actionid + " action execution via AJAX not permitted by security policy.");
                this.getActionProcessor().processAction(actionid, actionversionid, actionProps);
                ajaxResponse.addCallbackArgument("actionid", actionid);
                ajaxResponse.addCallbackArgument("actionsuccess", "1".equals(actionProps.get("(return)")));
                ajaxResponse.addCallbackArgument("returnproperties", actionProps);
            } else if (actionclass.length() > 0) {
                if (!SecurityPolicyUtil.isActionClassPermitted(this.getConnectionId(), "ajax", actionclass)) throw new ActionException(actionid + " action execution via AJAX not permitted by security policy.");
                this.getActionProcessor().processActionClass(actionclass, actionProps, false);
                ajaxResponse.addCallbackArgument("actionclass", actionclass);
                ajaxResponse.addCallbackArgument("actionsuccess", "1".equals(actionProps.get("(return)")));
                ajaxResponse.addCallbackArgument("returnproperties", actionProps);
            } else {
                ajaxResponse.setError("Actionid not specified in call properties!");
            }
        }
        catch (ActionException e) {
            ErrorHandler errorHandler = e.getErrorHandler();
            if (errorHandler.hasErrors()) {
                StringBuffer sb = new StringBuffer();
                sb.append("Failed to process action '" + (actionid.length() > 0 ? actionid : actionclass) + "'.");
                sb.append("<hr>");
                for (int i = 0; i < errorHandler.size(); ++i) {
                    sb.append(((ErrorDetail)errorHandler.get(i)).getMessage()).append("<br>");
                }
                ajaxResponse.setError(sb.toString(), e);
            }
            ajaxResponse.setError("Failed to process action '" + (actionid.length() > 0 ? actionid : actionclass) + "'. Exception: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

