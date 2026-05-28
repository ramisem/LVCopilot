/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.cmt;

import com.labvantage.sapphire.actions.cmt.CheckOutSDI;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.error.ErrorHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class ExecuteCheckOutSDI
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
        String propertytreenodeid = ajaxResponse.getRequestParameter("propertytreenodeid", "");
        String changerequestid = ajaxResponse.getRequestParameter("changerequestid", "");
        String departmentid = ajaxResponse.getRequestParameter("departmentid", "");
        String newChangeLogId = "";
        if (sdcid.length() > 0 && keyid1.length() > 0) {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", sdcid);
            props.setProperty("keyid1", keyid1);
            if (!"PropertyTree".equals(sdcid)) {
                if (keyid2.length() > 0) {
                    props.setProperty("keyid2", keyid2);
                }
                if (keyid3.length() > 0) {
                    props.setProperty("keyid3", keyid3);
                }
            } else {
                props.setProperty("propertytreenodeid", propertytreenodeid);
            }
            props.setProperty("departmentid", departmentid);
            props.setProperty("changerequestid", changerequestid);
            try {
                this.getActionProcessor().processActionClass(CheckOutSDI.class.getName(), props);
                newChangeLogId = props.getProperty("changelogid", "");
            }
            catch (ActionException e) {
                message = this.getTranslationProcessor().translate("Error Checking Out selected item(s).");
                message = message + "<hr>";
                ErrorHandler errorHandler = e.getErrorHandler();
                message = errorHandler != null ? message + this.getTranslationProcessor().translate("Reason: ") + this.getTranslationProcessor().translate(errorHandler.getLastErrorMessage()) : message + this.getTranslationProcessor().translate(e.getMessage());
                message = message + "<hr>";
                message = message + this.getTranslationProcessor().translate("If problem persists, please contact Administrator.");
                e.printStackTrace();
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("newchangelogid", newChangeLogId);
        ajaxResponse.print();
    }
}

