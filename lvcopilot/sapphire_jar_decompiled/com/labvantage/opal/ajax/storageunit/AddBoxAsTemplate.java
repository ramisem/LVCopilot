/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.storageunit;

import com.labvantage.opal.actions.storageunit.AddStorageUnit;
import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class AddBoxAsTemplate
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String boxid = ajaxResponse.getRequestParameter("boxid", "");
        String boxtemplateid = ajaxResponse.getRequestParameter("templateid", "");
        String newboxid = "";
        if (boxid.length() > 0 && boxtemplateid.length() > 0) {
            if (boxtemplateid.equals(OpalUtil.getColumnValue(this.getQueryProcessor(), "s_box", "s_boxid", "s_boxid = ? and templateflag = 'Y'", new String[]{boxtemplateid}))) {
                message = this.getTranslationProcessor().translate("The entered Box Template already exists") + ": " + boxtemplateid;
            } else {
                String storageunittype = OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "storageunittype", "linksdcid='LV_Box' and linkkeyid1=?", new String[]{boxid});
                PropertyList props = new PropertyList();
                props.setProperty("storageunittype", storageunittype);
                props.setProperty("copies", "1");
                props.setProperty("primary_sdcid", "LV_Box");
                props.setProperty("primary_keyid1", boxtemplateid);
                props.setProperty("primary_templatekeyid1", boxid);
                props.setProperty("primary_templateflag", "Y");
                props.setProperty("primary_overrideautokey", "Y");
                props.setProperty("auditactivity", "Save As Template");
                props.setProperty("auditreason", "New Box Template");
                try {
                    this.getActionProcessor().processActionClass(AddStorageUnit.class.getName(), props);
                    String storageunitid = props.getProperty("newkeyid1");
                    newboxid = OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "linkkeyid1", "storageunitid=?", new String[]{storageunitid});
                }
                catch (ActionException e) {
                    message = this.getTranslationProcessor().translate("Failed to add Box Template") + "<hr>" + e.getMessage();
                    e.printStackTrace();
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("newboxid", newboxid);
        ajaxResponse.print();
    }
}

