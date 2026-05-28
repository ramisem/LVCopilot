/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.opal.ajax.sample;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class GetParticipantSamples
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String participantid = ajaxResponse.getRequestParameter("participantid", "");
        String sampleid = "";
        if (participantid.length() > 0) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_sample.s_sampleid from s_sample where s_sample.samplefamilyid in  (select s_samplefamily.s_samplefamilyid from s_samplefamily where participantid = ?) order by s_sample.s_sampleid", (Object[])new String[]{participantid});
            sampleid = ds != null && ds.size() > 0 ? ds.getColumnValues("s_sampleid", ";") : "";
        }
        ajaxResponse.addCallbackArgument("sampleid", sampleid);
        ajaxResponse.addCallbackArgument("isclinical", this.getStudyClinicalFlag(participantid));
        ajaxResponse.print();
    }

    private String getStudyClinicalFlag(String participantid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_study.clinicalflag from s_participant, s_study where s_study.s_studyid = s_participant.sstudyid and s_participant.s_participantid = ?", (Object[])new String[]{participantid});
        if (ds != null && ds.size() > 0) {
            return ds.getString(0, "clinicalflag", "N");
        }
        return "N";
    }
}

