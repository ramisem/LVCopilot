/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.scheduler.ajax;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.action.ExecuteNowForSDI;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class ExecuteNowForSDIAjax
extends BaseAjaxRequest
implements ExecuteNowForSDI {
    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String sdcid = ar.getRequestParameter("sdcid");
        String keyid1 = ar.getRequestParameter("keyid1");
        if (sdcid.isEmpty()) {
            throw new IllegalArgumentException("SDC id not given!");
        }
        if (keyid1.isEmpty()) {
            throw new IllegalArgumentException("KeyID 1 not given!");
        }
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("sdcid", sdcid);
        actionProps.setProperty("keyid1", keyid1);
        actionProps.setProperty("keyid2", ar.getRequestParameter("keyid2"));
        actionProps.setProperty("keyid3", ar.getRequestParameter("keyid3"));
        actionProps.setProperty("honorplanitemstatus", ar.getRequestParameter("honorplanitemstatus"));
        actionProps.setProperty("monitorgroupflag", ar.getRequestParameter("monitorgroupflag"));
        actionProps.setProperty("monitorgrouplabel", ar.getRequestParameter("monitorgrouplabel"));
        actionProps.setProperty("monitorgrouptype", ar.getRequestParameter("monitorgrouptype"));
        actionProps.setProperty("monitorgroupid", ar.getRequestParameter("monitorgroupid"));
        actionProps.setProperty("eventdt", ar.getRequestParameter("eventdt"));
        actionProps.setProperty("instancecount", ar.getRequestParameter("instancecount"));
        actionProps.setProperty("schedulegroupid", ar.getRequestParameter("schedulegroupid"));
        String msg = "";
        boolean status = true;
        try {
            this.getActionProcessor().processAction("ExecuteNowForSDI", "1", actionProps);
            msg = actionProps.getProperty("msg");
        }
        catch (SapphireException e) {
            msg = this.getTranslationProcessor().translate("Could not Execute Schedules. Error: ") + e.getMessage();
            status = false;
        }
        ar.addCallbackArgument("status", status);
        ar.addCallbackArgument("msg", msg);
        ar.print();
    }
}

