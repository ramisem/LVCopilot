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

import com.labvantage.sapphire.Trace;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class GetLabelEventDetailForSDI
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 77738 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        String result = "No Data Found";
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String keyids = ajaxResponse.getRequestParameter("keyids", "");
        keyids = StringUtil.replaceAll(keyids, "%3B", ";");
        ArrayList findMapList = this.getFindMapList(keyids);
        String keyid1 = this.getKeyid1(keyids);
        String labelMethodId = ajaxResponse.getRequestParameter("labelmethodid");
        String sdcid = ajaxResponse.getRequestParameter("sdcid");
        String rset_id = "";
        try {
            rset_id = this.getDAMProcessor().createRSet(sdcid, keyid1, null, null);
        }
        catch (SapphireException exp) {
            Trace.logError("Failed to create RSET " + exp.getMessage());
            ajaxResponse.setError(exp.getMessage());
            throw new ServletException((Throwable)exp);
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql = "select labeleventitem.labeleventid, labeleventitem.itemkeyid1,labeleventitem.itemkeyid2,labeleventitem.itemkeyid3  from labeleventitem, labelevent  where labelevent.labeleventid in(select labeleventitem.labeleventid from labeleventitem , labelevent,  rsetitems where labelevent.labelmethodid=" + safeSQL.addVar(labelMethodId) + " and labeleventitem.itemsdcid=" + safeSQL.addVar(sdcid) + " and rsetitems.rsetid = " + safeSQL.addVar(rset_id) + " and rsetitems.sdcid = labeleventitem.itemsdcid and labeleventitem.itemkeyid1 = rsetitems.keyid1  and labeleventitem.labeleventid=labelevent.labeleventid) and labelevent.labeleventid=labeleventitem.labeleventid order by labelevent.createdt desc";
        DataSet tempDataSet = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        if (tempDataSet.getRowCount() > 0) {
            ArrayList<DataSet> groupDataSet = tempDataSet.getGroupedDataSets("labeleventid");
            for (int i = 0; i < groupDataSet.size(); ++i) {
                DataSet dataSet = groupDataSet.get(i);
                boolean isKeyMatched = false;
                for (int j = 0; j < findMapList.size(); ++j) {
                    int index = dataSet.findRow((HashMap)findMapList.get(j));
                    if (index < 0) continue;
                    isKeyMatched = true;
                    break;
                }
                if (!isKeyMatched) continue;
                result = "Data Found";
                break;
            }
        }
        this.getDAMProcessor().clearRSet(rset_id);
        ajaxResponse.addCallbackArgument("data", result);
        ajaxResponse.print();
    }

    private ArrayList getFindMapList(String keyids) {
        String[] arrKeyids = StringUtil.split(keyids, ";");
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
        String[] arrKeyids = StringUtil.split(keyids, ";");
        for (int i = 0; i < arrKeyids.length; ++i) {
            String row = arrKeyids[i];
            String[] arr = StringUtil.split(row, "|");
            keyid1 = arr.length == 1 ? keyid1 + ";" + row : keyid1 + ";" + arr[0];
        }
        return keyid1.substring(1);
    }
}

