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

import java.text.DecimalFormat;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class CopySchedulePlanItem
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String schedulePlanIds = ar.getRequestParameter("scheduleplanid");
        String schedulePlanItemIds = ar.getRequestParameter("scheduleplanitemid");
        String scheduleTemplateSdcId = ar.getRequestParameter("scheduletemplatesdcid");
        String scheduleTemplateKeyId1 = ar.getRequestParameter("scheduletemplatekeyid1");
        String scheduleTemplateKeyId2 = ar.getRequestParameter("scheduletemplatekeyid2");
        String scheduleTemplateKeyId3 = ar.getRequestParameter("scheduletemplatekeyid3");
        boolean shareScheduleTemplate = ar.getRequestParameter("sharescheduletemplate", "N").toLowerCase().startsWith("y");
        boolean allowCopyMultiple = ar.getRequestParameter("allowcopymultiple", "N").toLowerCase().startsWith("y");
        if (schedulePlanIds.isEmpty()) {
            throw new ServletException("Schedule plan ID is empty");
        }
        if (schedulePlanItemIds.isEmpty()) {
            throw new ServletException("Schedule plan item ID is empty");
        }
        if (schedulePlanItemIds.contains(";") && !allowCopyMultiple) {
            throw new ServletException("Only single schedule plan item allowed");
        }
        String[] schedulePlanIdArr = StringUtil.split(schedulePlanIds, ";");
        String[] schedulePlanItemIdsArr = StringUtil.split(schedulePlanItemIds, ";");
        if (allowCopyMultiple && schedulePlanIdArr.length != schedulePlanItemIdsArr.length) {
            throw new ServletException("Schedule Plan Id count does not match Schedule Plan Item Id count");
        }
        for (int i = 0; i < schedulePlanIdArr.length; ++i) {
            String schedulePlanId = schedulePlanIdArr[i];
            String schedulePlanItemId = schedulePlanItemIdsArr[i];
            boolean isExclusiveScheduleSampleTemplate = false;
            if (scheduleTemplateSdcId.equals("Sample")) {
                String sql = "select scheduletemplateflag from s_sample where s_sampleid  = ?";
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{scheduleTemplateKeyId1});
                if (ds.getRowCount() == 1) {
                    isExclusiveScheduleSampleTemplate = ds.getValue(0, "scheduletemplateflag", "N").equals("E");
                }
            }
            PropertyList addSDIProps = new PropertyList();
            addSDIProps.setProperty("sdcid", "SchedulePlanItem");
            addSDIProps.setProperty("templatekeyid1", schedulePlanId);
            addSDIProps.setProperty("templatekeyid2", schedulePlanItemId);
            addSDIProps.setProperty("keyid1", schedulePlanId);
            addSDIProps.setProperty("keyid2", new DecimalFormat("00000").format(this.getSequenceProcessor().getSequence("SchedulePlanItem", schedulePlanId)));
            addSDIProps.setProperty("planitemstatus", "X");
            if (!shareScheduleTemplate) {
                addSDIProps.setProperty("scheduletemplatekeyid1", "(null)");
                addSDIProps.setProperty("scheduletemplatekeyid2", "(null)");
                addSDIProps.setProperty("scheduletemplatekeyid3", "(null)");
            }
            try {
                this.getActionProcessor().processAction("AddSDI", "1", addSDIProps);
            }
            catch (ActionException e) {
                throw new ServletException("Cannot add SDI: " + e.getMessage(), (Throwable)e);
            }
            if (!isExclusiveScheduleSampleTemplate || !shareScheduleTemplate) continue;
            if (scheduleTemplateSdcId.isEmpty()) {
                throw new ServletException("Schedule template SDC ID is empty");
            }
            if (scheduleTemplateKeyId1.isEmpty()) {
                throw new ServletException("Schedule template key ID1 is empty");
            }
            PropertyList editSDIProps = new PropertyList();
            editSDIProps.setProperty("sdcid", scheduleTemplateSdcId);
            editSDIProps.setProperty("keyid1", scheduleTemplateKeyId1);
            if (!scheduleTemplateKeyId2.isEmpty()) {
                editSDIProps.setProperty("keyid2", scheduleTemplateKeyId2);
                if (!scheduleTemplateKeyId3.isEmpty()) {
                    editSDIProps.setProperty("keyid3", scheduleTemplateKeyId3);
                }
            }
            editSDIProps.setProperty("scheduletemplateflag", "S");
            try {
                this.getActionProcessor().processAction("EditSDI", "1", editSDIProps);
                continue;
            }
            catch (ActionException e) {
                throw new ServletException("Cannot edit SDI: " + e.getMessage(), (Throwable)e);
            }
        }
        ar.print();
    }
}

