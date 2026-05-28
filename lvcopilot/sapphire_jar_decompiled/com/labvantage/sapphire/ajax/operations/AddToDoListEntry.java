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
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class AddToDoListEntry
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90968 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "AddToDoListEntryHandler");
        HashMap<String, String> actionProps = new HashMap<String, String>();
        HashMap ajaxProps = (HashMap)ajaxResponse.getRequestParameters();
        boolean allow = false;
        try {
            List userRoleList = (List)this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getUserAttributeMap().get("rolelist");
            if (userRoleList.contains("WebPage-Admin") || userRoleList.contains("Administrator")) {
                allow = true;
            }
            if (allow || SecurityPolicyUtil.isActionPermitted(this.getConnectionId(), "ajax", "AddToDoListEntry", null)) {
                for (String propertyid : ajaxProps.keySet()) {
                    actionProps.put(propertyid, ajaxProps.get(propertyid).toString());
                }
                if (!allow && !SecurityPolicyUtil.isActionPermitted(this.getConnectionId(), "ajax", (String)actionProps.get("actionid"), actionProps)) {
                    throw new SapphireException((String)actionProps.get("actionid") + " in AddToDoListEntry action execution via AJAX not permitted by security policy.");
                }
            } else {
                throw new SapphireException("AddToDoListEntry action execution via AJAX not permitted by security policy and the user does not have WebPage-Admin or Adminitrator roles to bypass policy.");
            }
            this.getActionProcessor().processAction("AddToDoListEntry", "1", actionProps);
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to add todolistentry. Exception: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

