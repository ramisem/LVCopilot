/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.childsampleplan;

import com.labvantage.opal.util.ColumnUtil;
import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetCellHTML
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        String html = "&nbsp;";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String fieldid = ajaxResponse.getRequestParameter("fieldid");
        String value = ajaxResponse.getRequestParameter("value");
        PropertyList columnProp = new PropertyList();
        try {
            columnProp.setJSONString(ajaxResponse.getRequestParameter("column"));
            String columnid = columnProp.getProperty("column");
            if (columnid.length() > 0) {
                if (columnid.contains(" ")) {
                    columnid = columnid.substring(columnid.lastIndexOf(" ") + 1);
                    columnProp.setProperty("mode", "readonly");
                }
                if (OpalUtil.isEmpty(value)) {
                    value = columnProp.getProperty("default");
                }
                if ("readonly".equals(columnProp.getProperty("mode"))) {
                    html = value;
                } else {
                    String attributes = " value=\"" + value + "\" columnid=\"" + columnid.toLowerCase() + "\"";
                    html = ColumnUtil.getColumnHtml(columnProp, fieldid + "_field", attributes, this.getQueryProcessor(), this.getSDIProcessor());
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("html", html);
        ajaxResponse.addCallbackArgument("fieldid", fieldid);
        ajaxResponse.print();
    }
}

