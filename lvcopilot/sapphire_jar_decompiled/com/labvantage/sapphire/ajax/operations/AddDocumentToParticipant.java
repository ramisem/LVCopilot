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
import com.labvantage.opal.util.OpalUtil;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;

public class AddDocumentToParticipant
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String participantid = ajaxResponse.getRequestParameter("participantid");
        try {
            DataSet ds_participant = this.getQueryProcessor().getPreparedSqlDataSet("select s_participantid, subjectid, clinicalprotocolrevision, cpcohortid from s_participant where s_participantid = ?", new Object[]{participantid});
            if (ds_participant != null && ds_participant.size() > 0) {
                DataSet ds = new DataSet();
                int row = ds.addRow();
                ds.setString(row, "sstudyid", OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "sstudyid", "s_participantid = ?", new String[]{participantid}));
                ds.setString(row, "studysiteid", OpalUtil.getColumnValue(this.getQueryProcessor(), "s_participant", "studysiteid", "s_participantid = ?", new String[]{participantid}));
                ds.setString(row, "s_participantid", participantid);
                ds.setString(row, "subjectid", ds_participant.getString(0, "subjectid"));
                ds.setString(row, "clinicalprotocolrevision", ds_participant.getString(0, "clinicalprotocolrevision"));
                ds.setString(row, "cpcohortid", ds_participant.getString(0, "cpcohortid"));
                ds.setString(row, "formid", ajaxResponse.getRequestParameter("formid"));
                ds.setString(row, "formversionid", ajaxResponse.getRequestParameter("formversionid"));
                FormUtil.addBlankDocument(ds, this.getActionProcessor(), "LV_Participant", "s_participantid");
            }
        }
        catch (Exception e) {
            ajaxResponse.setError("Failed to add SDI. Exception: " + e.getMessage(), e);
        }
        ajaxResponse.print();
    }
}

