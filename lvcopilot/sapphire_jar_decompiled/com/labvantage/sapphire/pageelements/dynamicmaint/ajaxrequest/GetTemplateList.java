/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.http.HttpSession
 */
package com.labvantage.sapphire.pageelements.dynamicmaint.ajaxrequest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.xml.PropertyList;

public class GetTemplateList
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String pageId = ar.getRequestParameter("pageid", "");
        HttpSession session = req.getSession(true);
        PropertyList pageProps = (PropertyList)session.getAttribute("DYM_" + pageId);
        if (pageProps == null) {
            throw new ServletException("Session is no longer active");
        }
        PropertyList pagetypeConfig = pageProps.getPropertyListNotNull("pagetype");
        String sdcid = pagetypeConfig.getProperty("sdcid", "");
        PropertyList templateSelectorConfig = pagetypeConfig.getPropertyListNotNull("templateselector");
        String extraWhereClause = templateSelectorConfig.getProperty("whereclause", "");
        String whereClause = "templateflag='Y'";
        if (!extraWhereClause.equals("")) {
            whereClause = whereClause + " and " + extraWhereClause;
        }
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setQueryFrom(this.getSDCProcessor().getProperty(sdcid, "tableid"));
        sdiRequest.setQueryWhere(whereClause);
        sdiRequest.setShowTemplates(true);
        sdiRequest.setRetainRsetid(false);
        SDIData data = this.getSDIProcessor().getSDIData(sdiRequest);
        DataSet primary = data != null ? data.getDataset("primary") : new DataSet();
        DataSet templates = new DataSet();
        templates.addColumn("templateid", 0);
        String primaryKey = this.getSDCProcessor().getProperty(sdcid, "keycolid1");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            templates.addRow();
            templates.setValue(i, "templateid", primary.getValue(i, primaryKey));
        }
        ar.addCallbackArgument("templatelist", templates);
        ar.print();
    }
}

