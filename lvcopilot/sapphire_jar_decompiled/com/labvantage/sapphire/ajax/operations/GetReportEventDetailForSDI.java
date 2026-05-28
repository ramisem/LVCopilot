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

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.I18nUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetReportEventDetailForSDI
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 91421 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String reportEventId = "No Data Found";
        ConnectionInfo connectionInfo = this.getConnectionProcessor().getConnectionInfo(this.getConnectionid());
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String keyids = ajaxResponse.getRequestParameter("keyid1");
        ArrayList findMapList = this.getFindMapList(keyids);
        String keyid1 = this.getKeyid1(keyids);
        int keyid1Count = StringUtil.split(keyid1, ";").length;
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        String reportid = ajaxResponse.getRequestParameter("reportid");
        String languageid = ajaxResponse.getRequestParameter("languageid");
        languageid = OpalUtil.isNotEmpty(languageid) && !languageid.equalsIgnoreCase("null") && !languageid.equalsIgnoreCase("(null)") ? languageid : connectionInfo.getLanguage();
        String timezone = ajaxResponse.getRequestParameter("timezone");
        timezone = OpalUtil.isNotEmpty(timezone) && !timezone.equalsIgnoreCase("null") && !timezone.equalsIgnoreCase("(null)") ? timezone : (OpalUtil.isNotEmpty(connectionInfo.getTimeZone()) ? connectionInfo.getTimeZone().trim() : this.getSystemTimeZone(connectionInfo));
        SafeSQL safeSQL = new SafeSQL();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT reporteventitem.reporteventid, reporteventitem.itemkeyid1,reporteventitem.itemkeyid2,reporteventitem.itemkeyid3, reportevent.createdt ").append(" FROM reporteventitem , reportevent ").append(" WHERE reportevent.reporteventid IN(SELECT reporteventitem.reporteventid  FROM reporteventitem, reportevent ").append(" WHERE reportevent.reportid=" + safeSQL.addVar(reportid) + " and reporteventitem.itemsdcid=" + safeSQL.addVar(sdcid)).append(" and  reporteventitem.itemkeyid1 in(" + safeSQL.addIn(keyid1, ";") + ")  ").append(" and reporteventitem.reporteventid=reportevent.reporteventid) and ").append(" reportevent.reporteventid=reporteventitem.reporteventid ");
        if (OpalUtil.isNotEmpty(languageid) && !languageid.equalsIgnoreCase("(null)")) {
            sql.append(" and reportevent.languageid = " + safeSQL.addVar(languageid));
        }
        if (OpalUtil.isNotEmpty(timezone) && !timezone.equalsIgnoreCase("(null)")) {
            sql.append(" and reportevent.timezone = " + safeSQL.addVar(timezone));
        }
        sql.append(" order by reportevent.createdt desc");
        DataSet tempDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
        if (OpalUtil.isNotEmpty(tempDataSet)) {
            ArrayList<DataSet> groupDataSet = tempDataSet.getGroupedDataSets("reporteventid");
            for (int i = 0; i < groupDataSet.size(); ++i) {
                DataSet dataSet = groupDataSet.get(i);
                if (dataSet.getRowCount() != keyid1Count) continue;
                boolean isAllKeyMatched = true;
                for (int j = 0; j < findMapList.size(); ++j) {
                    int index = dataSet.findRow((HashMap)findMapList.get(j));
                    if (index >= 0) continue;
                    isAllKeyMatched = false;
                    break;
                }
                if (!isAllKeyMatched) continue;
                reportEventId = dataSet.getValue(0, "reporteventid");
                reportEventId = reportEventId + ";" + dataSet.getValue(0, "createdt");
                break;
            }
        }
        ajaxResponse.addCallbackArgument("data", reportEventId);
        ajaxResponse.print();
    }

    private ArrayList getFindMapList(String keyids) {
        String[] arrKeyids = StringUtil.split(keyids, "%3B");
        ArrayList findMapList = new ArrayList();
        for (int i = 0; i < arrKeyids.length; ++i) {
            String[] arr = StringUtil.split(arrKeyids[i], "|");
            HashMap<String, String> findMap = new HashMap<String, String>();
            if (arr.length == 1) {
                findMap.put("itemkeyid1", arr[0]);
                findMap.put("itemkeyid2", "(null)");
                findMap.put("itemkeyid3", "(null)");
            } else if (arr.length == 2) {
                findMap.put("itemkeyid1", arr[0]);
                findMap.put("itemkeyid2", arr[1]);
                findMap.put("itemkeyid3", "(null)");
            } else if (arr.length == 3) {
                findMap.put("itemkeyid1", arr[0]);
                findMap.put("itemkeyid2", arr[1]);
                findMap.put("itemkeyid3", arr[2]);
            }
            findMapList.add(findMap);
        }
        return findMapList;
    }

    private String getKeyid1(String keyids) {
        String keyid1 = "";
        String[] arrKeyids = StringUtil.split(keyids, "%3B");
        for (int i = 0; i < arrKeyids.length; ++i) {
            String row = arrKeyids[i];
            String[] arr = StringUtil.split(row, "|");
            keyid1 = arr.length == 1 ? keyid1 + ";" + row : keyid1 + ";" + arr[0];
        }
        return keyid1.substring(1);
    }

    private String getSystemTimeZone(ConnectionInfo connInfo) {
        TimeZone tz = I18nUtil.getConnectionTimeZone(connInfo);
        boolean isDayLightSaving = tz.inDaylightTime(new Date());
        String timezoneid = tz.getDisplayName(isDayLightSaving, 0);
        return timezoneid;
    }
}

