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

import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetJasperLabelMethodJSON
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        StringBuilder msg = new StringBuilder();
        String json = "";
        List roles = this.getConnectionProcessor().getRoleList();
        if (!roles.contains("Administrator") && !roles.contains(" WebPage-Admin")) {
            msg.append("Administrator or WebPage-Admin role required for testing!");
        } else {
            String keyid1 = ar.getRequestParameter("keyid1", "");
            String labelmethodid = ar.getRequestParameter("labelmethodid", "");
            String labelmethodversionid = ar.getRequestParameter("labelmethodversionid", "");
            if (keyid1.isEmpty() || labelmethodid.isEmpty() || labelmethodversionid.isEmpty()) {
                msg.append(this.getTranslationProcessor().translate("Missing inputs"));
            } else {
                PropertyList props = new PropertyList();
                props.setProperty("keyid1", keyid1);
                props.setProperty("labelmethodid", labelmethodid);
                props.setProperty("labelmethodversionid", labelmethodversionid);
                props.setProperty("jsontest", "Y");
                try {
                    this.getActionProcessor().processAction("GenerateLabel", "1", props);
                    json = props.getProperty("json");
                }
                catch (SapphireException e) {
                    msg.append(e.getMessage());
                }
            }
        }
        ar.addCallbackArgument("json", json);
        ar.addCallbackArgument("msg", msg.toString());
        ar.print();
    }
}

