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

public class GetStudySiteParticipant
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studysiteid = ajaxResponse.getRequestParameter("studysiteid");
        String externalparticipantid = ajaxResponse.getRequestParameter("externalparticipantid");
        String cpcohortid = "";
        String participantid = "";
        String eventdefid = "";
        String eventdeflabel = "";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, cpcohortid, externalparticipantid from s_participant where studysiteid = ? and externalparticipantid = ?", (Object[])new String[]{studysiteid, externalparticipantid});
        if (ds != null && ds.size() > 0) {
            participantid = ds.getString(0, "s_participantid");
            cpcohortid = ds.getString(0, "cpcohortid");
            StringBuilder sql = new StringBuilder();
            sql.append("select ed.s_eventdefid, ed.eventdeflabel");
            sql.append(" from s_eventdef ed, s_clinicalprotocol cp, s_participant p");
            sql.append(" where ed.eventdeftype = 'Visit'");
            sql.append(" and ed.cohortid = p.cpcohortid");
            sql.append(" and ed.clinicalprotocolid = p.clinicalprotocolid");
            sql.append(" and ed.clinicalprotocolrevision = p.clinicalprotocolrevision");
            sql.append(" and ed.CLINICALPROTOCOLVERSIONID = cp.S_CLINICALPROTOCOLVERSIONID");
            sql.append(" and cp.S_CLINICALPROTOCOLID = p.CLINICALPROTOCOLID");
            sql.append(" and cp.S_CLINICALPROTOCOLREVISION = p.CLINICALPROTOCOLREVISION");
            sql.append(" and cp.versionstatus = 'C'");
            sql.append(" and p.s_participantid = ?");
            sql.append(" order by ed.usersequence");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{participantid});
            if (ds != null && ds.size() > 0) {
                eventdefid = ds.getColumnValues("s_eventdefid", ";");
                eventdeflabel = ds.getColumnValues("eventdeflabel", ";");
            }
        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("select co.s_cpcohortid");
            sql.append(" from s_cpcohort co, s_studysite ss, s_clinicalprotocol cp");
            sql.append(" where ss.s_studysiteid = ?");
            sql.append(" and ss.clinicalprotocolid = co.s_clinicalprotocolid");
            sql.append(" and ss.clinicalprotocolrevision = co.s_clinicalprotocolrevision");
            sql.append(" and co.s_clinicalprotocolid = cp.s_clinicalprotocolid");
            sql.append(" and co.s_clinicalprotocolrevision = cp.s_clinicalprotocolrevision");
            sql.append(" and co.s_clinicalprotocolversionid = cp.s_clinicalprotocolversionid");
            sql.append(" and cp.versionstatus = 'C'");
            sql.append(" order by co.s_cpcohortid");
            ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), (Object[])new String[]{studysiteid});
            if (ds != null && ds.size() > 0) {
                cpcohortid = ds.getColumnValues("s_cpcohortid", ";");
            }
        }
        ajaxResponse.addCallbackArgument("rowindex", ajaxResponse.getRequestParameter("rowindex"));
        ajaxResponse.addCallbackArgument("participantid", participantid);
        ajaxResponse.addCallbackArgument("cpcohortid", cpcohortid);
        ajaxResponse.addCallbackArgument("eventdefid", eventdefid);
        ajaxResponse.addCallbackArgument("eventdeflabel", eventdeflabel);
        ajaxResponse.print();
    }
}

