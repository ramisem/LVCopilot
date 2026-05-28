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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetSystemPrinters
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String refreshPrinters = ar.getRequestParameter("refreshprinters", "Y");
        String listPrinters = ar.getRequestParameter("listprinters", "N");
        String msg = "";
        JSONArray printers = new JSONArray();
        String count = "0";
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("refreshprinters", refreshPrinters);
            actionProps.setProperty("listprinters", listPrinters);
            this.getActionProcessor().processAction("FindPrinters", "1", actionProps);
            String retCode = actionProps.getProperty("(return)");
            if (retCode != null && retCode.equals("1")) {
                msg = actionProps.getProperty("msg");
                printers = new JSONArray(actionProps.getProperty("printers"));
                count = actionProps.getProperty("count");
            }
        }
        catch (Exception e) {
            msg = this.getTranslationProcessor().translate("Could not retrieve printers") + ":\n" + e.getMessage();
            throw new ServletException(msg);
        }
        ar.addCallbackArgument("count", count);
        ar.addCallbackArgument("printers", printers);
        ar.addCallbackArgument("msg", msg);
        ar.print();
    }
}

