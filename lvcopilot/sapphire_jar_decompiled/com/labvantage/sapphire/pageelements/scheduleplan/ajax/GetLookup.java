/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.pageelements.scheduleplan.ajax;

import com.labvantage.sapphire.tagext.SDITagUtil;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.tagext.SDITagInfo;
import sapphire.xml.PropertyList;

public class GetLookup
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String datasetName = ar.getRequestParameter("datasetname");
        String sdcId = ar.getRequestParameter("sdcid");
        String javaScriptVarName = ar.getRequestParameter("jsvar");
        PageContext pageContext = ar.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        SDITagInfo sdiInfo = (SDITagInfo)pageContext.getAttribute("sdiinfo");
        ar.addCallbackArgument("lookuphtml", this.getSDCLookupHtml(datasetName, sdcId, javaScriptVarName, pageContext, sdiInfo));
        ar.print();
    }

    public String getSDCLookupHtml(String datasetName, String sdcId, String javaScriptVarName, PageContext pageContext, SDITagInfo sdiInfo) {
        PropertyList inputProps = new PropertyList();
        inputProps.setProperty("sdcid", sdcId);
        inputProps.setProperty("img", "WEB-CORE/imageref/flat/32/flat_black_external_lookup1.svg");
        inputProps.setProperty("data", datasetName);
        inputProps.setProperty("columnid", "linkkeyid1editor" + sdcId);
        inputProps.setProperty("mode", "lookup");
        inputProps.setProperty("validation", "Mandatory;");
        inputProps.setProperty("title", "Source");
        inputProps.setProperty("name", datasetName + "[__rowid]_linkkeyid1editor" + sdcId);
        inputProps.setProperty("lookuplink", new PropertyList());
        inputProps.setProperty("img_cssstyle", "lookup_img");
        inputProps.setProperty("lookupfieldid", datasetName + "[__rowid]_linkkeyid1editor" + sdcId);
        inputProps.setProperty("maxlen", "40");
        inputProps.setProperty("onchange", "sapphire.page.getTop().sapphire.page.maint.getMaintFrame()." + javaScriptVarName + ".sourceLookupOnChange();");
        inputProps.setProperty("rowindex", "[__rowid]");
        inputProps.setProperty("datasetname", datasetName);
        inputProps.setProperty("lookuppagedirectives", "oLUPD_" + datasetName + "_linkkeyid1editor" + sdcId);
        String html = SDITagUtil.getInstance(pageContext).getInputHtml(inputProps, sdiInfo);
        return html;
    }
}

