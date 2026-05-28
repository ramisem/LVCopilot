/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.pagetype.tism;

import java.util.HashSet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class TISMUnload
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String testmode = ajaxResponse.getRequestParameter("testmode");
        String taskexecid = ajaxResponse.getRequestParameter("taskexecid");
        String trackitems = ajaxResponse.getRequestParameter("trackitems");
        String sdcid = ajaxResponse.getRequestParameter("sourcesdcid");
        String keyid1 = "";
        String keyid2 = "";
        String keyid3 = "";
        String storageunitlist = "";
        String status = "OK";
        String statusmessage = "";
        if (!sdcid.equals("StorageUnitSDC")) {
            if (testmode.equals("N")) {
                if (trackitems.length() > 0) {
                    SafeSQL safeSQL = new SafeSQL();
                    String sql = "SELECT trackitem.linksdcid, trackitem.linkkeyid1, trackitem.currentstorageunitid, taskqueue.taskqueueid, storageunit.parentid, storageunit.storageunittype  FROM   taskqueue, trackitem LEFT OUTER JOIN storageunit ON trackitem.currentstorageunitid = storageunit.storageunitid WHERE  trackitem.trackitemid IN (" + safeSQL.addIn(trackitems, ";") + ") AND   trackitem.linksdcid = taskqueue.queuesdcid AND trackitem.linkkeyid1 = taskqueue.queuekeyid1 ";
                    DataSet taskqueueitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                    if (taskqueueitems != null && taskqueueitems.size() > 0) {
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("taskexecid", taskexecid);
                        actionProps.setProperty("taskqueueid", taskqueueitems.getColumnValues("taskqueueid", ";"));
                        try {
                            this.getActionProcessor().processAction("AllocateTaskQueueItems", "1", actionProps);
                            keyid1 = taskqueueitems.getColumnValues("linkkeyid1", ";");
                            keyid2 = taskqueueitems.getColumnValues("linkkeyid2", ";");
                            keyid3 = taskqueueitems.getColumnValues("linkkeyid3", ";");
                            storageunitlist = this.getStorageUnitList(taskqueueitems);
                        }
                        catch (Exception e) {
                            status = "Fail";
                            statusmessage = "Failed to allocate task queue items. Reason: " + e.getMessage();
                            this.logError(statusmessage, e);
                        }
                    }
                }
            } else {
                SafeSQL safeSQL = new SafeSQL();
                String sql = "SELECT trackitem.linksdcid, trackitem.linkkeyid1, trackitem.currentstorageunitid, storageunit.parentid, storageunit.storageunittype  FROM   trackitem LEFT OUTER JOIN storageunit ON trackitem.currentstorageunitid = storageunit.storageunitid  WHERE  trackitem.trackitemid IN (" + safeSQL.addIn(trackitems, ";") + ")";
                DataSet taskqueueitems = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                keyid1 = taskqueueitems.getColumnValues("linkkeyid1", ";");
                keyid2 = taskqueueitems.getColumnValues("linkkeyid2", ";");
                keyid3 = taskqueueitems.getColumnValues("linkkeyid3", ";");
                storageunitlist = this.getStorageUnitList(taskqueueitems);
            }
        } else {
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT trackitem.linksdcid, trackitem.linkkeyid1, trackitem.currentstorageunitid, storageunit.parentid, storageunit.storageunittype  FROM   trackitem LEFT OUTER JOIN storageunit ON trackitem.currentstorageunitid = storageunit.storageunitid  WHERE  trackitem.trackitemid IN (" + safeSQL.addIn(trackitems, ";") + ")";
            DataSet trackitemItems = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            sdcid = trackitemItems.getValue(0, "linksdcid");
            keyid1 = trackitemItems.getColumnValues("linkkeyid1", ";");
            keyid2 = trackitemItems.getColumnValues("linkkeyid2", ";");
            keyid3 = trackitemItems.getColumnValues("linkkeyid3", ";");
            storageunitlist = this.getStorageUnitList(trackitemItems);
        }
        ajaxResponse.addCallbackArgument("sdcid", sdcid);
        ajaxResponse.addCallbackArgument("keyid1", keyid1);
        ajaxResponse.addCallbackArgument("keyid2", keyid2);
        ajaxResponse.addCallbackArgument("keyid3", keyid3);
        ajaxResponse.addCallbackArgument("storageunitlist", storageunitlist);
        ajaxResponse.addCallbackArgument("stepid", ajaxResponse.getRequestParameter("stepid"));
        ajaxResponse.addCallbackArgument("status", status);
        ajaxResponse.addCallbackArgument("statusmessage", statusmessage);
        ajaxResponse.print();
    }

    private String getStorageUnitList(DataSet data) {
        StringBuilder storageunits = new StringBuilder();
        HashSet<String> set = new HashSet<String>();
        for (int i = 0; i < data.size(); ++i) {
            String storageunitid = data.getValue(i, "currentstorageunitid");
            if (data.getValue(i, "storageunittype").equals("BoxPos")) {
                storageunitid = data.getValue(i, "parentid");
            }
            if (set.contains(storageunitid)) continue;
            set.add(storageunitid);
            storageunits.append(";").append(storageunitid);
        }
        return storageunits.length() > 0 ? storageunits.substring(1) : "";
    }
}

