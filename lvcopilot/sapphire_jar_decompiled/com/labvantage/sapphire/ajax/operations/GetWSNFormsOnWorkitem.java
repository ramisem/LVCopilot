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
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GetWSNFormsOnWorkitem
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String workitemid = ajaxResponse.getRequestParameter("workitemid").replaceAll("%3B", ";");
        String workitemversionid = ajaxResponse.getRequestParameter("workitemversionid").replaceAll("%3B", ";");
        String sdiworkitemid = ajaxResponse.getRequestParameter("sdiworkitemid").replaceAll("%3B", ";");
        String rsetid = "";
        boolean datasetHasWS = false;
        try {
            rsetid = this.getDAMProcessor().createRSet("SDIWorkItem", sdiworkitemid, null, null);
        }
        catch (SapphireException e) {
            ajaxResponse.setError(e.getMessage());
        }
        SafeSQL safeSQL = new SafeSQL();
        String sql4WS = "SELECT count(d.sdiworkitemid) FROM sdiworkitem d, rsetitems r WHERE r.rsetid in (" + safeSQL.addIn(rsetid.replaceAll(";", "', '")) + ") AND r.sdcid = 'SDIWorkItem' AND  r.keyid1 = d.sdiworkitemid AND d.documentid is not null";
        try {
            int count = this.getQueryProcessor().getPreparedCount(sql4WS, safeSQL.getValues());
            if (count > 0) {
                datasetHasWS = true;
            }
        }
        catch (SapphireException e) {
            ajaxResponse.setError(e.getMessage());
            throw new ServletException((Throwable)e);
        }
        this.getDAMProcessor().clearRSet(rsetid);
        String formid = "";
        String formversionid = "";
        int formsCount = 0;
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("keyid1", workitemid);
        String keyid = "";
        for (int i = 0; i < StringUtil.split(workitemid, ";").length; ++i) {
            keyid = keyid + ";(null)";
        }
        if (workitemversionid.length() > 0) {
            actionProps.setProperty("keyid2", workitemversionid);
        } else {
            actionProps.setProperty("keyid2", keyid.substring(1));
        }
        actionProps.setProperty("keyid3", keyid.substring(1));
        actionProps.setProperty("sdcid", "WorkItem");
        actionProps.setProperty("connectionId", this.getConnectionId());
        actionProps.setProperty("sysUserId", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        try {
            this.getActionProcessor().processActionClass(InvokeGetSDIFormsProc.class.getName(), actionProps);
        }
        catch (ActionException e) {
            throw new ServletException((Throwable)e);
        }
        String formsRsetid = actionProps.getProperty("rsetResult");
        DataSet formsds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1, keyid2, keyid3 FROM rsetitems WHERE rsetid=?", new Object[]{formsRsetid});
        formid = formsds.getColumnValues("keyid1", ";");
        formversionid = formsds.getColumnValues("keyid2", ";");
        formsCount = formsds.size();
        this.getDAMProcessor().clearRSet(formsRsetid);
        ajaxResponse.addCallbackArgument("datasetHasWS", datasetHasWS);
        ajaxResponse.addCallbackArgument("formsCount", formsCount);
        ajaxResponse.addCallbackArgument("formid", formid);
        ajaxResponse.addCallbackArgument("formversionid", formversionid);
        ajaxResponse.print();
    }
}

