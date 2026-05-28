/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.validation.sample;

import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateMultiSampleReview
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sampleids = ajaxResponse.getRequestParameter("sampleid", "").trim();
        SafeSQL safeSQL = new SafeSQL();
        DataSet sdiApporvalDS = this.getQueryProcessor().getPreparedSqlDataSet("select distinct approvaltypeid,keyid1 from sdiapproval where sdcid='Sample' and keyid1 in (" + safeSQL.addIn(sampleids, "%3B") + ")", safeSQL.getValues());
        String flag = "S";
        if (sdiApporvalDS != null && sdiApporvalDS.getRowCount() > 0) {
            String approvaltypes = sdiApporvalDS.getColumnValues("approvaltypeid", ";");
            String[] approvaltypeidArr = StringUtil.split(approvaltypes, ";");
            String[] sampleidArr = StringUtil.split(sampleids, "%3B");
            HashMap<String, String> hm = new HashMap<String, String>();
            block0: for (String sampleid : sampleidArr) {
                for (String approvaltypeid : approvaltypeidArr) {
                    hm.clear();
                    hm.put("keyid1", sampleid);
                    hm.put("approvaltypeid", approvaltypeid);
                    if (sdiApporvalDS.findRow(hm) >= 0) continue;
                    flag = "D";
                    continue block0;
                }
            }
        }
        ajaxResponse.addCallbackArgument("flag", flag);
        ajaxResponse.print();
    }
}

