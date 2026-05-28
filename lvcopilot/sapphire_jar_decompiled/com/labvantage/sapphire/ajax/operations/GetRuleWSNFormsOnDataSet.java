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

import com.labvantage.sapphire.actions.documents.InvokeGetSDIFormsProc;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;

public class GetRuleWSNFormsOnDataSet
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String sdidataid = ajaxResponse.getRequestParameter("sdidataid").replaceAll("%3B", ";");
        String rsetid = "";
        try {
            rsetid = this.getDAMProcessor().createRSet("DataSet", sdidataid, null, null);
        }
        catch (SapphireException e) {
            ajaxResponse.setError(e.getMessage());
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql4WS = "SELECT d.paramlistid, d.paramlistversionid, d.variantid , d.documentid FROM sdidata d, rsetitems r WHERE r.rsetid in (" + safeSQL.addIn(rsetid, ";") + ") AND r.sdcid = 'DataSet' AND  r.keyid1 = d.sdidataid ";
        DataSet plds = this.getQueryProcessor().getPreparedSqlDataSet(sql4WS, safeSQL.getValues());
        String paramlistid = plds.getColumnValues("paramlistid", ";");
        String paramlistversionid = plds.getColumnValues("paramlistversionid", ";");
        String variantid = plds.getColumnValues("variantid", ";");
        boolean datasetHasWS = false;
        for (int i = 0; i < plds.size(); ++i) {
            if (plds.getValue(i, "documentid").length() <= 0) continue;
            datasetHasWS = true;
            break;
        }
        this.getDAMProcessor().clearRSet(rsetid);
        boolean isRuleNotAssignment = false;
        String rsetidPL = "";
        try {
            rsetidPL = this.getDAMProcessor().createRSet("ParamList", paramlistid, paramlistversionid, variantid);
        }
        catch (SapphireException e) {
            ajaxResponse.setError(e.getMessage());
            throw new ServletException((Throwable)e);
        }
        safeSQL.reset();
        String sql = "SELECT p.createworksheetrule FROM paramlist p, rsetitems r WHERE r.rsetid in (" + safeSQL.addIn(rsetidPL, ";") + ") AND r.sdcid = 'ParamList' AND  r.keyid1 = p.paramlistid AND r.keyid2 = p.paramlistversionid AND r.keyid3 = p.variantid ";
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
        for (int i = 0; i < ds.size(); ++i) {
            if (ds.getValue(i, "createworksheetrule").equals("On Assignment")) continue;
            isRuleNotAssignment = true;
            break;
        }
        this.getDAMProcessor().clearRSet(rsetidPL);
        String formid = "";
        String formversionid = "";
        int formsCount = 0;
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("keyid1", paramlistid.replaceAll("%3B", ";"));
        actionProps.setProperty("keyid2", paramlistversionid.replaceAll("%3B", ";"));
        actionProps.setProperty("keyid3", variantid.replaceAll("%3B", ";"));
        actionProps.setProperty("sdcid", "ParamList");
        actionProps.setProperty("connectionId", this.getConnectionId());
        actionProps.setProperty("sysUserId", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        try {
            this.getActionProcessor().processActionClass(InvokeGetSDIFormsProc.class.getName(), actionProps);
        }
        catch (ActionException e) {
            throw new ServletException((Throwable)e);
        }
        String formsRsetid = actionProps.getProperty("rsetResult");
        safeSQL.reset();
        String sql1 = "SELECT keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=" + safeSQL.addVar(formsRsetid);
        DataSet formsds = this.getQueryProcessor().getPreparedSqlDataSet(sql1, safeSQL.getValues());
        formid = formsds.getColumnValues("keyid1", ";");
        formversionid = formsds.getColumnValues("keyid2", ";");
        formsCount = formsds.size();
        this.getDAMProcessor().clearRSet(formsRsetid);
        ajaxResponse.addCallbackArgument("isRuleNotAssignment", isRuleNotAssignment);
        ajaxResponse.addCallbackArgument("datasetHasWS", datasetHasWS);
        ajaxResponse.addCallbackArgument("formsCount", formsCount);
        ajaxResponse.addCallbackArgument("formid", formid);
        ajaxResponse.addCallbackArgument("formversionid", formversionid);
        ajaxResponse.addCallbackArgument("paramlistid", paramlistid);
        ajaxResponse.addCallbackArgument("paramlistversionid", paramlistversionid);
        ajaxResponse.addCallbackArgument("variantid", variantid);
        ajaxResponse.print();
    }
}

