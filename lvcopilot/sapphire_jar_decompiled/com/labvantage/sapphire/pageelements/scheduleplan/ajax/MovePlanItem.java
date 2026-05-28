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

import java.util.Arrays;
import java.util.HashSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class MovePlanItem
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ar = new AjaxResponse(request, response);
        String schedulePlanId = ar.getRequestParameter("scheduleplanid");
        String schedulePlanItemId = ar.getRequestParameter("scheduleplanitemid");
        String nodeId = ar.getRequestParameter("nodeid");
        if (schedulePlanId.isEmpty()) {
            throw new ServletException("Schedule plan ID is empty");
        }
        if (schedulePlanItemId.isEmpty()) {
            throw new ServletException("Schedule plan Item ID is empty");
        }
        if (nodeId.isEmpty()) {
            throw new ServletException("Node ID is empty");
        }
        if (nodeId.contains(";")) {
            throw new ServletException("Moving to multiple nodes not supported!");
        }
        HashSet<String> planIds = new HashSet<String>(Arrays.asList(StringUtil.split(schedulePlanId, ";")));
        String mainSchedulePlanId = "";
        if (planIds.size() != 1) {
            throw new ServletException("Cannot move cross Schedule Plans!");
        }
        mainSchedulePlanId = (String)planIds.stream().findFirst().get();
        PropertyList editSdiProps = new PropertyList();
        editSdiProps.setProperty("sdcid", "SchedulePlanItem");
        editSdiProps.setProperty("keyid1", schedulePlanId);
        editSdiProps.setProperty("keyid2", schedulePlanItemId);
        editSdiProps.setProperty("scheduleplannodeid", nodeId);
        String sql = "select * from scheduleplannode where scheduleplanid = ? and scheduleplannodeid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{mainSchedulePlanId, nodeId});
        if (ds.getRowCount() == 1) {
            String sourceSdc = ds.getValue(0, "refsdcid");
            String sourceKeyid1 = ds.getValue(0, "refkeyid1");
            String sourceKeyid2 = ds.getValue(0, "refkeyid2");
            String sourceKeyid3 = ds.getValue(0, "refkeyid3");
            if (!sourceSdc.isEmpty() && !sourceKeyid1.isEmpty()) {
                editSdiProps.setProperty("linksdcid", sourceSdc);
                editSdiProps.setProperty("linkkeyid1", sourceKeyid1);
                editSdiProps.setProperty("linkkeyid2", sourceKeyid2);
                editSdiProps.setProperty("linkkeyid3", sourceKeyid3);
            }
        } else {
            throw new ServletException("Schedule Plan Node not found!");
        }
        try {
            this.getActionProcessor().processAction("EditSDI", "1", editSdiProps);
        }
        catch (Exception e) {
            throw new ServletException("Cannot move item to another folder", (Throwable)e);
        }
        ar.print();
    }
}

