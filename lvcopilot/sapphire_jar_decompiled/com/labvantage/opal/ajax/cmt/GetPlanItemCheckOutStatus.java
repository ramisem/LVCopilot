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

import com.labvantage.opal.util.OpalUtil;
import java.util.HashMap;
import java.util.UUID;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetPlanItemCheckOutStatus
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1");
        String rowid = ajaxResponse.getRequestParameter("rowid");
        String elementid = ajaxResponse.getRequestParameter("elementid");
        String result = "";
        String imgtitle = "";
        HashMap<String, String> tokenMap = new HashMap<String, String>();
        if ("Y".equals(this.getSDCProcessor().getProperty(sdcid, "changecontrolledflag"))) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select checkedoutbydepartmentid, checkedoutbyuserid from changelog where linksdcid = ? and linkkeyid1 = ? and changelogstatus = 'Checked Out'", (Object[])new String[]{sdcid, keyid1});
            if (OpalUtil.isNotEmpty(ds)) {
                String checkedoutbyuserid = ds.getString(0, "checkedoutbyuserid", "");
                String checkedoutbydepartmentid = ds.getString(0, "checkedoutbydepartmentid", "");
                if (checkedoutbydepartmentid.length() > 0) {
                    result = OpalUtil.toList(this.getConnectionProcessor().getSapphireConnection().getDepartmentList(), ";").contains(checkedoutbydepartmentid) ? "3" : "4";
                    tokenMap.put("departmentid", checkedoutbydepartmentid);
                    tokenMap.put("sdcid", sdcid);
                    imgtitle = this.getTranslationProcessor().translate("Checked out to department [departmentid] (Managed by [sdcid])", tokenMap);
                } else if (checkedoutbyuserid.length() > 0) {
                    result = this.getConnectionProcessor().getSapphireConnection().getSysuserId().equals(checkedoutbyuserid) ? "1" : "2";
                    tokenMap.put("userid", checkedoutbyuserid);
                    tokenMap.put("sdcid", sdcid);
                    imgtitle = this.getTranslationProcessor().translate("Checked out by [userid] (Managed by [sdcid])", tokenMap);
                }
            } else {
                result = "5";
                tokenMap.put("sdcid", sdcid);
                imgtitle = this.getTranslationProcessor().translate("Under Change Control (Managed by [sdcid])", tokenMap);
            }
        } else {
            result = "0";
            tokenMap.put("sdcid", sdcid);
            imgtitle = this.getTranslationProcessor().translate("Managed by [sdcid]", tokenMap);
        }
        ajaxResponse.addCallbackArgument("result", result);
        ajaxResponse.addCallbackArgument("title", imgtitle);
        ajaxResponse.addCallbackArgument("elementid", elementid);
        ajaxResponse.addCallbackArgument("rowid", rowid);
        ajaxResponse.addCallbackArgument("sdcid", sdcid);
        ajaxResponse.addCallbackArgument("keyid1", keyid1);
        ajaxResponse.addCallbackArgument("imgid", UUID.randomUUID().toString());
        ajaxResponse.print();
    }
}

