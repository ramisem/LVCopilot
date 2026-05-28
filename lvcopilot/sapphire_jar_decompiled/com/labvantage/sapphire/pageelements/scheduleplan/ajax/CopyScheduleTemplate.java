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
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class CopyScheduleTemplate
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String sdcId = ar.getRequestParameter("sdcid");
        String keyId1 = ar.getRequestParameter("keyid1");
        String keyId2 = ar.getRequestParameter("keyid2");
        String schedulePlanId = ar.getRequestParameter("scheduleplanid");
        String schedulePlanItemId = ar.getRequestParameter("scheduleplanitemid");
        if (sdcId.isEmpty()) {
            throw new ServletException("SDC ID is empty");
        }
        if (keyId1.isEmpty()) {
            throw new ServletException("Key ID1 is empty");
        }
        if (keyId1.contains(";")) {
            throw new ServletException("Only single SDI is allowed");
        }
        if (schedulePlanId.isEmpty()) {
            throw new ServletException("Schedule plan ID is empty");
        }
        if (schedulePlanItemId.isEmpty()) {
            throw new ServletException("Schedule plan item ID is empty");
        }
        PropertyList addSDIProps = new PropertyList();
        addSDIProps.setProperty("sdcid", sdcId);
        if (!keyId2.isEmpty() && !keyId2.equals("(null)")) {
            addSDIProps.setProperty("templatekeyid1", keyId1);
            addSDIProps.setProperty("templatekeyid2", keyId2);
        } else {
            addSDIProps.setProperty("templateid", keyId1);
        }
        if (sdcId.equals("Sample")) {
            addSDIProps.setProperty("scheduletemplateflag", "E");
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
        addSDIProps.setProperty("overrideautokey", "Y");
        addSDIProps.setProperty("keyid1", newKeyId1);
        if (keyColumn2 != null && !keyColumn2.isEmpty()) {
            addSDIProps.setProperty("keyid2", newKeyId2);
        }
        addSDIProps.setProperty("copies", "1");
        addSDIProps.setProperty("templateflag", "Y");
        try {
            this.getActionProcessor().processAction("AddSDI", "1", addSDIProps);
            newKeyId1 = addSDIProps.getProperty("newkeyid1");
            newKeyId2 = addSDIProps.getProperty("newkeyid2");
        }
        catch (ActionException e) {
            throw new ServletException("Cannot copy schedule template: " + keyId1, (Throwable)e);
        }
        PropertyList editSDIProps = new PropertyList();
        editSDIProps.setProperty("sdcid", "SchedulePlanItem");
        editSDIProps.setProperty("keyid1", schedulePlanId);
        editSDIProps.setProperty("keyid2", schedulePlanItemId);
        editSDIProps.setProperty("scheduletemplatesdcid", sdcId);
        editSDIProps.setProperty("scheduletemplatekeyid1", newKeyId1);
        if (!newKeyId2.isEmpty() && !newKeyId2.equals("(null)")) {
            editSDIProps.setProperty("scheduletemplatekeyid2", newKeyId2);
        }
        try {
            this.getActionProcessor().processAction("EditSDI", "1", editSDIProps);
        }
        catch (ActionException e) {
            throw new ServletException("Cannot set schedule template keys for plan item: " + schedulePlanId + schedulePlanItemId, (Throwable)e);
        }
        ar.addCallbackArgument("sdcid", sdcId);
        ar.addCallbackArgument("newkeyid1", newKeyId1);
        ar.addCallbackArgument("newkeyid2", newKeyId2);
        ar.print();
    }
}

