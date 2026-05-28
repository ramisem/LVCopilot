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

import com.labvantage.opal.util.FormUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class AddDocumentToParticipantEvent
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String participanteventid = ajaxResponse.getRequestParameter("participanteventid");
        try {
            DataSet ds_pe = this.getQueryProcessor().getPreparedSqlDataSet("select p.sstudyid, p.studysiteid, p.s_participantid, p.subjectid, p.clinicalprotocolrevision, p.cpcohortid from s_participantevent pe, s_participant p where pe.s_participanteventid = ? and p.s_participantid = pe.participantid", new Object[]{participanteventid});
            if (ds_pe != null && ds_pe.size() > 0) {
                DataSet ds = new DataSet();
                int row = ds.addRow();
                ds.setString(row, "sstudyid", ds_pe.getString(0, "sstudyid"));
                ds.setString(row, "studysiteid", ds_pe.getString(0, "studysiteid"));
                ds.setString(row, "s_participantid", ds_pe.getString(0, "s_participantid"));
                ds.setString(row, "subjectid", ds_pe.getString(0, "subjectid"));
                ds.setString(row, "clinicalprotocolrevision", ds_pe.getString(0, "clinicalprotocolrevision"));
                ds.setString(row, "cpcohortid", ds_pe.getString(0, "cpcohortid"));
                ds.setString(row, "formid", ajaxResponse.getRequestParameter("formid"));
                ds.setString(row, "formversionid", ajaxResponse.getRequestParameter("formversionid"));
                ds.setString(row, "s_participanteventid", participanteventid);
                FormUtil.addBlankDocument(ds, this.getActionProcessor(), "LV_ParticipantEvent", "s_participanteventid");
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to add SDI. Exception: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

