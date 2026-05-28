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
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;

public class GetParticipantSubjectInfo
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String studysiteid = ajaxResponse.getRequestParameter("studysiteid", "").trim();
        String externalsubject = ajaxResponse.getRequestParameter("externalsubject", "").trim();
        String sequence = ajaxResponse.getRequestParameter("sequence", "").trim();
        String sstudyid = "";
        String subjectid = "";
        String s_participantid = "";
        String cpcohortid = "";
        String participantstatus = "";
        String studysitedesc = "";
        if (studysiteid.length() > 0 && externalsubject.length() > 0) {
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder sql = new StringBuilder();
            sql.append("select s_participantid, sstudyid, studysiteid, (select s.studysitedesc from s_studysite s where s.s_studysiteid = studysiteid) studysitedesc, cpcohortid, subjectid, participantstatus");
            sql.append(" from s_participant");
            sql.append(" where studysiteid = ").append(safeSQL.addVar(studysiteid));
            sql.append(" and externalparticipantid = ").append(safeSQL.addVar(externalsubject));
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql.toString(), safeSQL.getValues());
            if (ds != null && ds.size() > 0) {
                sstudyid = ds.getValue(0, "sstudyid", "");
                studysitedesc = ds.getValue(0, "studysitedesc", "");
                subjectid = ds.getValue(0, "subjectid", "");
                s_participantid = ds.getValue(0, "s_participantid", "");
                cpcohortid = ds.getValue(0, "cpcohortid", "");
                participantstatus = ds.getValue(0, "participantstatus", "");
            }
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("externalsubject", externalsubject);
            jsonObject.put("sstudyid", sstudyid);
            jsonObject.put("studysiteid", studysiteid);
            jsonObject.put("studysitedesc", studysitedesc);
            jsonObject.put("subjectid", subjectid);
            jsonObject.put("s_participantid", s_participantid);
            jsonObject.put("cpcohortid", cpcohortid);
            jsonObject.put("participantstatus", participantstatus);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        ajaxResponse.addCallbackArgument("sequence", sequence);
        ajaxResponse.addCallbackArgument("json", jsonObject.toString());
        ajaxResponse.print();
    }
}

