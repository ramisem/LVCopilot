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
import com.labvantage.sapphire.admin.ddt.StorageUnitSDC;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;

public class GetTrackitems
extends BaseAjaxRequest {
    public static String LABVANTAGE_CVS_ID = "$Revision: 54729 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid", "");
        if ("StorageUnitSDC".equals(sdcid)) {
            storageunitid = keyid1;
        } else if (StringUtil.getLen(storageunitid) == 0L) {
            storageunitid = OpalUtil.getColumnValue(this.getQueryProcessor(), "storageunit", "storageunitid", "linksdcid = ? and linkkeyid1 = ?", new String[]{sdcid, keyid1});
        }
        if (StringUtil.getLen(storageunitid) > 0L) {
            try {
                ajaxResponse.addCallbackArgument("trackitemid", StorageUnitSDC.getAllTrackitemsInStorageUnitHeirarchy(this.getQueryProcessor(), storageunitid, this.getConnectionProcessor().isOra()).getColumnValues("trackitemid", ";"));
            }
            catch (SapphireException e) {
                ajaxResponse.addCallbackArgument("trackitemid", "");
                this.logger.error(this.getClass().getName() + ": Unable to get trackitems in Storage Unit " + storageunitid, e);
            }
        }
        ajaxResponse.print();
    }
}

