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

import com.labvantage.sapphire.actions.documents.InvokeInstrumentCertProc;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GetInstrumentForQCBatch
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String certflag = ajaxResponse.getRequestParameter("certflag");
        String qcbatchids = ajaxResponse.getRequestParameter("qcbatchid").replaceAll("%3B", ";");
        SafeSQL safeSQL = new SafeSQL();
        String sql = "SELECT DISTINCT instrumenttypeid, instrumentmodelid FROM s_qcbatch WHERE s_qcbatchid in (" + safeSQL.addIn(qcbatchids.replaceAll(";", "','")) + ")";
        DataSet qcbatchDS = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        String error = "";
        String instrumentTypeId = "";
        String instrumentModelId = "";
        if (qcbatchDS.getRowCount() > 1) {
            error = this.getTranslationProcessor().translate("Please select QCBatches of same Instrument Type and Model");
        } else {
            instrumentTypeId = qcbatchDS.getValue(0, "instrumenttypeid");
            instrumentModelId = qcbatchDS.getValue(0, "instrumentmodelid");
            if (instrumentTypeId.length() == 0) {
                error = this.getTranslationProcessor().translate("Instrument Type is not set for the selected QCBatche(s): " + qcbatchids.replaceAll(";", ", "));
            }
        }
        String instrumentid = "";
        if (error.length() == 0) {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("instrumenttypeid", instrumentTypeId);
            actionProps.setProperty("certflag", certflag);
            actionProps.setProperty("connectionId", this.getConnectionId());
            actionProps.setProperty("sysUserId", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
            try {
                this.getActionProcessor().processActionClass(InvokeInstrumentCertProc.class.getName(), actionProps);
            }
            catch (ActionException e) {
                throw new ServletException((Throwable)e);
            }
            String rsetid = actionProps.getProperty("rsetResult");
            if (instrumentModelId.length() > 0) {
                safeSQL.reset();
                sql = "SELECT instrumentid FROM instrument, rsetitems r WHERE r.sdcid = 'Instrument' AND r.rsetid = " + safeSQL.addVar(rsetid) + " AND instrument.instrumentid = r.keyid1  AND instrument.instrumentmodelid = " + safeSQL.addVar(instrumentModelId);
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
                instrumentid = ds.getColumnValues("instrumentid", ";");
            } else {
                DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1 FROM rsetitems WHERE rsetid=?", new Object[]{rsetid});
                instrumentid = ds.getColumnValues("keyid1", ";");
            }
            this.getDAMProcessor().clearRSet(rsetid);
        }
        ajaxResponse.addCallbackArgument("keyid1", instrumentid);
        ajaxResponse.addCallbackArgument("qcbatchid", qcbatchids);
        ajaxResponse.addCallbackArgument("errormsg", error);
        ajaxResponse.print();
    }
}

