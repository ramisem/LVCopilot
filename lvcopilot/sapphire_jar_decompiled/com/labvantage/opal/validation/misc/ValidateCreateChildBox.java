/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.misc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateCreateChildBox
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String boxid = StringUtil.replaceAll(ajaxResponse.getRequestParameter("keyid1", ""), "%3B", ";");
        String sysuserid = this.getConnectionProcessor().getSapphireConnection().getSysuserId();
        SafeSQL safeSQL = new SafeSQL();
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_box.s_boxid, s_box.boxstatus, s_box.boxtype, trackitem.custodialuserid from s_box, trackitem where trackitem.linksdcid = 'LV_Box' and trackitem.linkkeyid1 = s_box.s_boxid and s_box.s_boxid in (" + safeSQL.addIn(boxid, ";") + ")", safeSQL.getValues());
        if (ds != null) {
            for (int i = 0; i < ds.size(); ++i) {
                String custodialuserid = ds.getString(i, "custodialuserid", "");
                if (!custodialuserid.equals(sysuserid)) {
                    message = this.getTranslationProcessor().translate("User must have custody of Box to create Child box(es)");
                    break;
                }
                String boxstatus = ds.getString(i, "boxstatus", "");
                if (boxstatus.equals("Empty")) {
                    message = this.getTranslationProcessor().translate("Selected Box is Empty");
                    break;
                }
                String boxtype = ds.getString(i, "boxtype", "");
                if (boxtype.equals("Sorted")) continue;
                message = this.getTranslationProcessor().translate("Child box is allowed to be created only from Sorted Box");
                break;
            }
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.print();
    }
}

