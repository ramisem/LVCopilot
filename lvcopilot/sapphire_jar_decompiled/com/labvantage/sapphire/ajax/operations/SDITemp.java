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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDITemp
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response, "SDITempHandler");
        Map params = ajaxResponse.getRequestParameters();
        PropertyList props = new PropertyList();
        Iterator it = params.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            String value = params.get(key).toString();
            props.setProperty(key, value);
        }
        try {
            String tempid;
            this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.sdi.SDITemp", props, false);
            if ((props.getProperty("mode").equalsIgnoreCase("list") || props.getProperty("mode").equalsIgnoreCase("listfull")) && (tempid = props.getProperty("tempid", "")).length() > 0) {
                if (props.getProperty("mode").equalsIgnoreCase("list")) {
                    ajaxResponse.addCallbackArgument("tempid", tempid);
                }
                if (props.getProperty("mode").equalsIgnoreCase("listfull")) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    String[] tempids = StringUtil.split(tempid, ";");
                    for (int i = 0; i < tempids.length; ++i) {
                        String tempvalueid = "tempvalue" + (i + 1);
                        String tempvalue = props.getProperty(tempvalueid, "");
                        map.put(tempids[i], tempvalue);
                    }
                    ajaxResponse.addCallbackArgument("tempvalue", map);
                }
                ajaxResponse.addCallbackArgument("modby", props.getProperty("modby", ""));
                ajaxResponse.addCallbackArgument("moddt", props.getProperty("moddt", ""));
                ajaxResponse.addCallbackArgument("count", props.getProperty("count", ""));
            }
        }
        catch (Exception e) {
            ajaxResponse.setError(e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

