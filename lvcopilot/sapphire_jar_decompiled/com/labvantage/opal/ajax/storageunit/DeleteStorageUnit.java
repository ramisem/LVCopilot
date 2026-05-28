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

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.DeleteSDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class DeleteStorageUnit
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid");
        if (OpalUtil.isNotEmpty(storageunitid)) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "StorageUnitSDC");
            props.setProperty("keyid1", storageunitid);
            props.setProperty("auditreason", ajaxResponse.getRequestParameter("auditreason"));
            try {
                this.getActionProcessor().processActionClass(DeleteSDI.class.getName(), props);
            }
            catch (ActionException e) {
                message = this.getTranslationProcessor().translate("Error deleting storage unit.") + "<br>" + this.getTranslationProcessor().translate("If problem persists, please contact your Administrator.");
                Trace.logError("Error deleting storage unit " + storageunitid + ". " + e.getMessage());
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

