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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class MoveStorageUnit
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String parentid = ajaxResponse.getRequestParameter("parentid");
        String storageunitid = ajaxResponse.getRequestParameter("storageunitid", "");
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        if (OpalUtil.isNotEmpty(parentid)) {
            if (storageunitid.length() == 0 && sdcid.length() > 0 && keyid1.length() > 0) {
                DataSet ds;
                if ((keyid1 = StringUtil.replaceAll(keyid1, "%3B", ";")).contains(";")) {
                    SafeSQL safeSQL = new SafeSQL();
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, moveableflag from storageunit where linksdcid = " + safeSQL.addVar(sdcid) + " and linkkeyid1 in (" + safeSQL.addIn(keyid1, ";") + ")", safeSQL.getValues());
                } else {
                    ds = this.getQueryProcessor().getPreparedSqlDataSet("select storageunitid, moveableflag from storageunit where linksdcid = ? and linkkeyid1 = ?", (Object[])new String[]{sdcid, keyid1});
                }
                if (OpalUtil.isNotEmpty(ds)) {
                    for (int i = 0; i < ds.size(); ++i) {
                        if ("Y".equals(ds.getString(i, "moveableflag"))) continue;
                        message = this.getTranslationProcessor().translate("Selected item is not Moveable");
                        break;
                    }
                    if (message.length() == 0) {
                        storageunitid = ds.getColumnValues("storageunitid", ";");
                    }
                }
            }
            if (message.length() == 0 && storageunitid.length() > 0) {
                try {
                    PropertyList props = new PropertyList();
                    props.setProperty("storageunitid", storageunitid);
                    props.setProperty("parentid", parentid);
                    props.setProperty("auditreason", ajaxResponse.getRequestParameter("auditreason", "MoveStorageUnit"));
                    props.setProperty("auditactivity", ajaxResponse.getRequestParameter("auditactivity", ""));
                    props.setProperty("auditsignedflag", ajaxResponse.getRequestParameter("auditsignedflag", ""));
                    this.getActionProcessor().processActionClass(com.labvantage.sapphire.actions.storage.MoveStorageUnit.class.getName(), props);
                }
                catch (ActionException e) {
                    message = this.getTranslationProcessor().translate("Failed to move Storage Unit.") + "<hr>" + e.getMessage() + "<hr>" + this.getTranslationProcessor().translate("If problem persists, please contact your Administrator.");
                    Trace.logError("Failed to move storage unit. " + e.getMessage());
                }
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("storageunitid", storageunitid);
        ajaxResponse.addCallbackArgument("sdcid", sdcid);
        ajaxResponse.addCallbackArgument("keyid1", keyid1);
        ajaxResponse.print();
    }
}

