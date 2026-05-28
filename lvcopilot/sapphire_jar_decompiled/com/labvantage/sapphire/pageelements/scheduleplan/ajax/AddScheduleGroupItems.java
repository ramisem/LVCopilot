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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class AddScheduleGroupItems
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        int i;
        AjaxResponse ar = new AjaxResponse(request, response);
        String scheduleGroupId = ar.getRequestParameter("schedulegroupid");
        if (scheduleGroupId.isEmpty()) {
            throw new ServletException("Schedule group Id is empty");
        }
        if (scheduleGroupId.contains(";")) {
            throw new ServletException("Only one schedule group Id allowed");
        }
        String sdcId = ar.getRequestParameter("sdcid");
        if (sdcId.isEmpty()) {
            throw new ServletException("SDC Id is empty");
        }
        if (sdcId.contains(";")) {
            throw new ServletException("Only one SDC Id allowed");
        }
        String keys = ar.getRequestParameter("keys");
        String keyId1s = ar.getRequestParameter("keyid1");
        String keyId2s = ar.getRequestParameter("keyid2");
        try {
            keys = URLDecoder.decode(keys, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new ServletException("Cannot decode input params");
        }
        HashSet<List<String>> schedulePlanItemSet = new HashSet<List<String>>();
        ArrayList<String> keyId1List = new ArrayList<String>();
        ArrayList<String> keyId2List = new ArrayList<String>();
        if (sdcId.equals("SchedulePlanItem")) {
            this.addKeys(keys, keyId1List, keyId2List);
            if (!keyId1s.isEmpty() && !keyId2s.isEmpty()) {
                keyId1List.addAll(Arrays.asList(keyId1s.split(";")));
                keyId2List.addAll(Arrays.asList(keyId2s.split(";")));
                if (keyId1List.size() != keyId2List.size()) {
                    throw new ServletException("Inconsistent number of keys");
                }
            }
            for (i = 0; i < keyId1List.size(); ++i) {
                String schedulePlanId = (String)keyId1List.get(i);
                String schedulePlanItemId = (String)keyId2List.get(i);
                List<String> schedulePlanItemKeys = new ArrayList<String>();
                schedulePlanItemKeys.add(schedulePlanId);
                schedulePlanItemKeys.add(schedulePlanItemId);
                schedulePlanItemSet.add(schedulePlanItemKeys);
            }
        } else {
            if (!keyId1s.isEmpty()) {
                keyId1List.addAll(Arrays.asList(keyId1s.split(";")));
                if (!keyId2s.isEmpty()) {
                    keyId2List.addAll(Arrays.asList(keyId2s.split(";")));
                }
            }
            this.addKeys(keys, keyId1List, keyId2List);
            if (!keyId1List.isEmpty()) {
                for (i = 0; i < keyId1List.size(); ++i) {
                    ArrayList<String> sqlParams = new ArrayList<String>();
                    Object sql = "SELECT scheduleplanid, scheduleplanitemid FROM scheduleplanitem WHERE linksdcid = ? AND linkkeyid1 = ?";
                    sqlParams.add(sdcId);
                    sqlParams.add((String)keyId1List.get(i));
                    if (keyId2List.isEmpty() && keyId2List.size() > i) {
                        sql = (String)sql + " AND linkkeyid2 = ?";
                        sqlParams.add((String)keyId2List.get(i));
                    }
                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet((String)sql, sqlParams.toArray());
                    for (int j = 0; j < ds.getRowCount(); ++j) {
                        ArrayList<String> schedulePlanItemKeys = new ArrayList<String>();
                        schedulePlanItemKeys.add(ds.getString(j, "scheduleplanid", ""));
                        schedulePlanItemKeys.add(ds.getString(j, "scheduleplanitemid", ""));
                        schedulePlanItemSet.add(schedulePlanItemKeys);
                    }
                }
            }
        }
        StringBuilder schedulePlanIds = new StringBuilder();
        StringBuilder schedulePlanItemIds = new StringBuilder();
        for (List<String> schedulePlanItemKeys : schedulePlanItemSet) {
            String schedulePlanId = (String)schedulePlanItemKeys.get(0);
            String schedulePlanItemId = (String)schedulePlanItemKeys.get(1);
            if (schedulePlanId.isEmpty() || schedulePlanItemId.isEmpty() || !this.isAlreadyAdded(scheduleGroupId, schedulePlanId, schedulePlanItemId)) continue;
            schedulePlanIds.append(";").append(schedulePlanId);
            schedulePlanItemIds.append(";").append(schedulePlanItemId);
        }
        if (schedulePlanIds.length() > 0 && schedulePlanItemIds.length() > 0) {
            PropertyList addSDIDetailProps = new PropertyList();
            addSDIDetailProps.setProperty("sdcid", "LV_ScheduleGroup");
            addSDIDetailProps.setProperty("keyid1", scheduleGroupId);
            addSDIDetailProps.setProperty("linkid", "ScheduleGroup Items");
            addSDIDetailProps.setProperty("scheduleplanid", schedulePlanIds.substring(1));
            addSDIDetailProps.setProperty("scheduleplanitemid", schedulePlanItemIds.substring(1));
            try {
                this.getActionProcessor().processAction("AddSDIDetail", "1", addSDIDetailProps);
            }
            catch (ActionException e) {
                throw new ServletException("Cannot add schedule group items", (Throwable)e);
            }
        }
        ar.print();
    }

    private void addKeys(String keys, List<String> keyId1List, List<String> keyId2List) {
        if (!keys.isEmpty()) {
            for (String key : keys.split(";")) {
                String keyId1 = "";
                String keyId2 = "";
                if (key.contains("|")) {
                    String[] splitKey = key.split("\\|");
                    if (splitKey.length > 0) {
                        keyId1 = splitKey[0];
                        if (splitKey.length > 1) {
                            keyId2 = splitKey[1];
                        }
                    }
                } else {
                    keyId1 = key;
                }
                if (keyId1.isEmpty()) continue;
                keyId1List.add(keyId1);
                if (keyId2.isEmpty()) continue;
                keyId2List.add(keyId2);
            }
        }
    }

    private boolean isAlreadyAdded(String scheduleGroupId, String schedulePlanId, String schedulePlanItemId) {
        String sql = "SELECT sgi.scheduleplanid, sgi.scheduleplanitemid FROM schedulegroupitem sgi WHERE sgi.schedulegroupid = ? AND sgi.scheduleplanid = ? AND sgi.scheduleplanitemid = ?";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{scheduleGroupId, schedulePlanId, schedulePlanItemId});
        return ds.getRowCount() == 0;
    }
}

