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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;

public class ValidateEventDefinitionLabel
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = "$Revision: 54068 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String protocolId = ajaxResponse.getRequestParameter("protocolId");
        String revisionId = ajaxResponse.getRequestParameter("revisionId");
        String versionId = ajaxResponse.getRequestParameter("versionId");
        String cohortId = ajaxResponse.getRequestParameter("cohortId");
        String newLabel = ajaxResponse.getRequestParameter("label");
        String eventType = ajaxResponse.getRequestParameter("eventType");
        String mode = ajaxResponse.getRequestParameter("mode");
        String sourceEventId = ajaxResponse.getRequestParameter("sourceEventId");
        boolean labelExists = this.isLabelDuplicate(protocolId, revisionId, versionId, cohortId, newLabel, this.getQueryProcessor(), eventType, mode, sourceEventId);
        ajaxResponse.addCallbackArgument("duplicateLabel", String.valueOf(labelExists));
        ajaxResponse.print();
    }

    private boolean isLabelDuplicate(String protocolId, String revisionId, String versionId, String cohortId, String label, QueryProcessor qp, String eventType, String mode, String sourceEventId) {
        boolean labelExists = false;
        String sql = "";
        String parenteventdefidCondition = "";
        SafeSQL safeSQL = new SafeSQL();
        if (eventType.equalsIgnoreCase("Timepoint")) {
            parenteventdefidCondition = mode.equalsIgnoreCase("copy") ? "parenteventdefid=(select parenteventdefid from s_eventdef where s_eventdefid=" + safeSQL.addVar(sourceEventId) + ")" : "parenteventdefid = " + safeSQL.addVar(sourceEventId);
            sql = "select eventdeflabel from s_eventdef where clinicalprotocolid = " + safeSQL.addVar(protocolId) + " and clinicalprotocolversionid =" + safeSQL.addVar(versionId) + " and clinicalprotocolrevision =" + safeSQL.addVar(revisionId) + " and cohortid=" + safeSQL.addVar(cohortId) + " and " + parenteventdefidCondition + " order by createdt";
        } else {
            sql = "select eventdeflabel from s_eventdef where clinicalprotocolid = " + safeSQL.addVar(protocolId) + " and clinicalprotocolversionid =" + safeSQL.addVar(versionId) + " and clinicalprotocolrevision =" + safeSQL.addVar(revisionId) + " and cohortid IN (" + safeSQL.addIn(cohortId, ";") + ") order by createdt";
        }
        DataSet ds4evts = qp.getPreparedSqlDataSet(sql, safeSQL.getValues());
        String evtlabels = "";
        if (ds4evts.size() > 0) {
            evtlabels = ds4evts.getColumnValues("eventdeflabel", ";");
            String[] labelArr = StringUtil.split(evtlabels, ";");
            for (int i = 0; i < labelArr.length; ++i) {
                if (!labelArr[i].toUpperCase().equals(label.toUpperCase())) continue;
                labelExists = true;
                break;
            }
        }
        return labelExists;
    }
}

