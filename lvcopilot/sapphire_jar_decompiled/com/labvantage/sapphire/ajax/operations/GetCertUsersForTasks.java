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

import com.labvantage.sapphire.actions.workflow.InvokeTaskDefUsersCertProc;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class GetCertUsersForTasks
extends BaseAjaxRequest {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        String taskdefid = ajaxResponse.getRequestParameter("taskdefid");
        String taskdefversionid = ajaxResponse.getRequestParameter("taskdefversionid");
        String taskdefvariantid = ajaxResponse.getRequestParameter("taskdefvariantid");
        String departmentid = ajaxResponse.getRequestParameter("departmentid");
        PropertyList actionProps = new PropertyList();
        actionProps.setProperty("taskdefid", taskdefid);
        actionProps.setProperty("taskdefversionid", taskdefversionid);
        actionProps.setProperty("taskdefvariantidd", taskdefvariantid);
        actionProps.setProperty("connectionId", this.getConnectionId());
        actionProps.setProperty("department", departmentid);
        actionProps.setProperty("sysUserId", this.getConnectionProcessor().getSapphireConnection().getSysuserId());
        try {
            this.getActionProcessor().processActionClass(InvokeTaskDefUsersCertProc.class.getName(), actionProps);
        }
        catch (ActionException e) {
            throw new ServletException((Throwable)e);
        }
        String rsetid = actionProps.getProperty("rsetResult");
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT keyid1 FROM rsetitems WHERE rsetid=?", new Object[]{rsetid});
        String userid = ds.getColumnValues("keyid1", ";");
        this.getDAMProcessor().clearRSet(rsetid);
        ajaxResponse.addCallbackArgument("keyid1", userid);
        ajaxResponse.print();
    }
}

