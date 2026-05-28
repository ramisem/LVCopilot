/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.scheduler.ajax;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;

public class GetDeepCopyInfo
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest req, HttpServletResponse resp, ServletContext sc) throws ServletException {
        AjaxResponse ar = new AjaxResponse(req, resp);
        String sdcId = ar.getRequestParameter("sdcid", "");
        String keyId1 = ar.getRequestParameter("keyid1", "");
        if (sdcId.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Id is missing");
        }
        if (keyId1.isEmpty()) {
            throw new IllegalArgumentException("Schedule Plan Item Id is missing");
        }
        boolean canCopySamplePoint = false;
        String label = "";
        if (sdcId.equals("Location")) {
            String keygenerationrule = this.getSDCProcessor().getProperty("SamplePoint", "keygenerationrule");
            canCopySamplePoint = StringUtil.getLen(keygenerationrule) > 0L && keygenerationrule.charAt(0) == 'A';
            String sql = "select locationlabel from s_location where s_locationid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyId1});
            label = ds.getString(0, "locationlabel", "");
        } else if (sdcId.equals("SamplePoint")) {
            String sql = "select locationlabel from s_samplepoint where s_samplepointid = ?";
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, (Object[])new String[]{keyId1});
            label = ds.getString(0, "locationlabel", "");
        }
        ar.addCallbackArgument("label", label);
        ar.addCallbackArgument("cancopysamplepoint", canCopySamplePoint);
        ar.print();
    }
}

