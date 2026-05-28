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

import com.labvantage.sapphire.EncryptDecrypt;
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

public class AddSDI
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 90968 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "AddSDIHandler");
        HashMap<String, String> actionProps = new HashMap<String, String>();
        try {
            actionProps.putAll(ajaxResponse.getRequestParameters());
            String obfuscatedsdcid = (String)actionProps.get("sdcid");
            String sdcid = "";
            if (!obfuscatedsdcid.startsWith("{@}")) {
                throw new SapphireException("AddSDI action execution via AJAX not permitted by submitting sdcid from client side.");
            }
            sdcid = EncryptDecrypt.unobfsql(obfuscatedsdcid);
            actionProps.put("sdcid", sdcid);
            List userRoleList = (List)this.getConnectionProcessor().getConnectionInfo(this.getConnectionid()).getUserAttributeMap().get("rolelist");
            if (!("Category".equals(sdcid) || userRoleList.contains("WebPage-Admin") || userRoleList.contains("Administrator") || SecurityPolicyUtil.isActionPermitted(this.getConnectionId(), "ajax", "AddSDI", actionProps))) {
                throw new SapphireException("AddSDI action execution via AJAX not permitted by security policy except for Category SDC or user must have WebPage-Admin or Administrator role to bypass it.");
            }
            this.getActionProcessor().processAction("AddSDI", "1", actionProps);
            ajaxResponse.addCallbackArgument("newkeyid1", actionProps.get("newkeyid1"));
            ajaxResponse.addCallbackArgument("newkeyid2", actionProps.get("newkeyid2"));
            ajaxResponse.addCallbackArgument("newkeyid3", actionProps.get("newkeyid3"));
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to add SDI. Exception: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

