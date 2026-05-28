/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.util;

import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetFreezeThawData
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String color_pass = "#0DA80D";
        String color_warn = "#FF7F2A";
        String color_max = "#FF6600";
        String color_fail = "#FF0000";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String message = "";
        StringBuilder sb = new StringBuilder();
        String sdcid = ajaxResponse.getRequestParameter("sdcid", "");
        String keyid1 = ajaxResponse.getRequestParameter("keyid1", "");
        String trackitemid = ajaxResponse.getRequestParameter("trackitemid", "");
        ArrayList ds = null;
        if (trackitemid.length() == 0) {
            if (sdcid.length() > 0 && keyid1.length() > 0) {
                String tableid = this.getSDCProcessor().getProperty(sdcid, "tableid");
                String keycolid1 = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
                ds = this.getQueryProcessor().getPreparedSqlDataSet("select t.freezethawcount, t.freezethawcountwarn, t.freezethawcountmax from trackitem t, " + tableid + " t2 where t.linksdcid = ? and t.linkkeyid1 = t2." + keycolid1 + " and t.linkkeyid1 = ?", (Object[])new String[]{sdcid, keyid1});
            }
        } else {
            ds = this.getQueryProcessor().getPreparedSqlDataSet("select freezethawcount, freezethawcountwarn, freezethawcountmax from trackitem where trackitemid = ?", (Object[])new String[]{trackitemid});
        }
        if (ds != null && ds.size() > 0) {
            int freezethawcount = ((DataSet)ds).getInt(0, "freezethawcount", -1);
            int freezethawcountwarn = ((DataSet)ds).getInt(0, "freezethawcountwarn", -1);
            int freezethawcountmax = ((DataSet)ds).getInt(0, "freezethawcountmax", -1);
            String backgroundColor = color_pass;
            if (freezethawcount > 0 && freezethawcount >= freezethawcountwarn) {
                backgroundColor = freezethawcount == freezethawcountwarn ? color_warn : (freezethawcount <= freezethawcountmax ? color_max : color_fail);
            }
            sb.append("<div id='ftdatadiv' style='display:flex;flex-direction:column;position:fixed;background:white;border-radius:3px;box-shadow: 0 0 10px #999;'>");
            sb.append("<div style='color:#2d3436;font-size:10px;background-color:#dcdde1;border-top-left-radius:3px;border-top-right-radius:3px;padding:2px;text-align:center;'>");
            sb.append(this.getTranslationProcessor().translate("Freeze Thaw")).append("</div>");
            sb.append("<div style='display:flex;'>");
            sb.append("<div style='display:flex;flex-direction:column;'>");
            sb.append("<div style='color:#2d3436;font-size:8px;background-color:#dfe6e9;padding:2px;width:26px;text-align:center;'>").append(this.getTranslationProcessor().translate("WARN")).append("</div>");
            sb.append("<div style='color:black;font-size:18px;padding:4px;text-align:center;padding-top:10px;flex-grow:2;border-bottom-right-radius:3px;color:#95a5a6;'>").append(freezethawcountwarn == -1 ? "&nbsp;" : Integer.valueOf(freezethawcountwarn)).append("</div>");
            sb.append("</div>");
            sb.append("<div style='display:flex;flex-direction:column;'>");
            sb.append("<div style='color:#2d3436;font-size:8px;background-color:#dfe6e9;padding:2px;text-align:center;'>").append(this.getTranslationProcessor().translate("COUNT")).append("</div>");
            sb.append("<div style='color:#eee;font-weight:bold;font-size:30px;background-color:").append(backgroundColor).append(";padding:2px;text-align:center;min-width:30px;'>");
            sb.append(freezethawcount == -1 ? "0" : Integer.valueOf(freezethawcount));
            sb.append("</div></div>");
            sb.append("<div style='display:flex;flex-direction:column;justify-content:top;'>");
            sb.append("<div style='color:#2d3436;font-size:8px;background-color:#dfe6e9;padding:2px;width:26px;text-align:center;'>").append(this.getTranslationProcessor().translate("MAX")).append("</div>");
            sb.append("<div style='color:black;font-size:18px;padding:4px;text-align:center;padding-top:10px;flex-grow:2;border-bottom-right-radius:3px;color:#95a5a6;'>").append(freezethawcountmax == -1 ? "&nbsp;" : Integer.valueOf(freezethawcountmax)).append("</div>");
            sb.append("</div>");
            sb.append("</div>");
            sb.append("</div>");
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }
}

