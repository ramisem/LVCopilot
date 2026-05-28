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
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetBoxViewerHTML
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String message = "";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String boxid = ajaxResponse.getRequestParameter("boxid", "");
        String stepprops = ajaxResponse.getRequestParameter("stepprops", "");
        if (OpalUtil.isNotEmpty(stepprops)) {
            try {
                stepprops = URLDecoder.decode(stepprops, "UTF-8");
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        StringBuilder sb = new StringBuilder();
        String[] boxArray = StringUtil.split(boxid, ";");
        TabGroup tabGroup = new TabGroup();
        tabGroup.setId("tabgroup0");
        tabGroup.setAppearance("modern");
        int index = 0;
        for (String box : boxArray) {
            Tab tab = new Tab();
            tab.setId("box_" + index);
            tab.setAppearance("modern");
            tab.setText(box);
            tab.setExpanded("true");
            tab.setAction("loadContainerDisplay(this)");
            tab.setContent("Box " + box);
            tabGroup.setTab(tab);
            ++index;
        }
        sb.append(tabGroup.getHtml());
        sb.append("<script type='text/javascript'>");
        index = 0;
        for (String box : boxArray) {
            PropertyList props = new PropertyList();
            props.setProperty("mode", "Consolidate");
            props.setProperty("animate", "N");
            props.setId("box_" + index++);
            sb.append("elementProps['").append(box).append("'] = '").append(props.toJSONString()).append("';");
        }
        sb.append("</script>");
        ajaxResponse.addCallbackArgument("message", message);
        ajaxResponse.addCallbackArgument("html", sb.toString());
        ajaxResponse.print();
    }
}

