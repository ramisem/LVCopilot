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
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetStorageStatusofParentSample
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleid = ajaxResponse.getRequestParameter("sampleid");
        ArrayList<String> childSampleStatusList = new ArrayList<String>();
        if (!OpalUtil.isEmpty(sampleid)) {
            sampleid = StringUtil.replaceAll(sampleid, "%3B", ";");
            List<String> sampleList = OpalUtil.toUniqueList(sampleid, ";");
            for (String sample : sampleList) {
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_samplemap.sourcesampleid, (select s1.storagestatus from s_sample s1 where s1.s_sampleid = s_samplemap.SOURCESAMPLEID) sourcestoragestatus, (select s1.previousstoragestatus from s_sample s1 where s1.s_sampleid = s_samplemap.SOURCESAMPLEID) sourcepreviousstoragestatus, s_samplemap.destsampleid, (select s2.storagestatus from s_sample s2 where s2.s_sampleid = s_samplemap.DESTSAMPLEID) deststoragestatus from s_samplemap where s_samplemap.destsampleid = ?", (Object[])new String[]{sample});
                if (ds == null || ds.size() <= 0) continue;
                String childStorageStatus = "In Circulation";
                for (int row = 0; row < ds.size(); ++row) {
                    String sourcestoragestatus = ds.getString(row, "sourcestoragestatus");
                    if ("Disposed".equals(sourcestoragestatus)) {
                        sourcestoragestatus = ds.getString(row, "sourcepreviousstoragestatus");
                    }
                    if ("Temporary In Lab".equals(sourcestoragestatus)) {
                        childStorageStatus = "Temporary In Lab";
                        continue;
                    }
                    if ("Temporary In Lab".equals(childStorageStatus)) continue;
                    childStorageStatus = "In Circulation";
                }
                childSampleStatusList.add(ds.getString(0, "destsampleid") + ":" + childStorageStatus);
            }
        }
        ajaxResponse.addCallbackArgument("response", OpalUtil.toDelimitedString(childSampleStatusList, ";"));
        ajaxResponse.print();
    }
}

