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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;

public class LockUnLockAttachment
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String keyid2 = ajaxResponse.getRequestParameter("keyid2", "");
        String keyid3 = ajaxResponse.getRequestParameter("keyid3", "");
        String attachmentnum = ajaxResponse.getRequestParameter("attachmentnum", "");
        String elementid = ajaxResponse.getRequestParameter("elementid", "");
        String lockattachment = ajaxResponse.getRequestParameter("lockattachment", "N");
        String sysuserid = lockattachment.equalsIgnoreCase("Y") ? this.getConnectionProcessor().getSapphireConnection().getSysuserId() : "";
        int r = 2;
        if (sdcid.length() > 0 && keyid1.length() > 0 && attachmentnum.length() > 0) {
            r = this.getQueryProcessor().execSQL(20057, new Object[]{lockattachment, sysuserid, sdcid, keyid1, this.getKeyId(keyid2), this.getKeyId(keyid3), attachmentnum});
        }
        ajaxResponse.addCallbackArgument("elementid", elementid);
        ajaxResponse.addCallbackArgument("updated", r == 1 ? "Y" : "N");
        ajaxResponse.print();
    }

    private String getKeyId(String keyid) {
        return keyid == null || keyid.length() == 0 ? "(null)" : keyid;
    }
}

