/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.pageelements.scheduleplan.ajax;

import com.labvantage.sapphire.scheduler.SchedulerUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetNewEmbeddedTemplateId
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String sdcId = ar.getRequestParameter("sdcid");
        if (sdcId.isEmpty()) {
            throw new ServletException("SDC id is empty");
        }
        PropertyList templateKeyidProps = new PropertyList();
        templateKeyidProps.setProperty("sdcid", sdcId);
        SchedulerUtil schedulerUtil = new SchedulerUtil(this.getConnectionId());
        try {
            schedulerUtil.getNewEmbeddedTemplateId(templateKeyidProps);
        }
        catch (SapphireException e) {
            throw new ServletException(e.getMessage());
        }
        String keyColumn1 = templateKeyidProps.getProperty("keycolumn1");
        String keyColumn2 = templateKeyidProps.getProperty("keycolumn2");
        String newKeyId1 = templateKeyidProps.getProperty("newkeyid1");
        String newKeyId2 = templateKeyidProps.getProperty("newkeyid2");
        ar.addCallbackArgument("keycolid1", keyColumn1);
        ar.addCallbackArgument("keycolid2", keyColumn2);
        ar.addCallbackArgument("newkeyid1", newKeyId1);
        ar.addCallbackArgument("newkeyid2", newKeyId2);
        ar.print();
    }
}

