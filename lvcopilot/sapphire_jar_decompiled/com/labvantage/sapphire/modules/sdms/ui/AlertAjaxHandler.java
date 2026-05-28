/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.sdms.ui;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.capa.ClearAlert;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.xml.PropertyList;

public class AlertAjaxHandler
extends BaseAjaxRequest
implements SDMSConstants {
    static Map<String, Map<String, String>> lastKnownCollectorHealthState = new ConcurrentHashMap<String, Map<String, String>>();
    private PageContext pageContext = null;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxresponse = new AjaxResponse(request, response);
        this.pageContext = ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
        try {
            String dothis = ajaxresponse.getRequestParameter("dothis");
            if (dothis.equals("AJAX_CLEARALERT")) {
                this.clearAlert(ajaxresponse, info);
            }
        }
        catch (Exception e) {
            Trace.logError("Unable to execute command", e);
            ajaxresponse.setError(e.getMessage());
        }
        ajaxresponse.print();
    }

    private void clearAlert(AjaxResponse ajaxresponse, ConnectionInfo info) throws ActionException {
        String incidentid = ajaxresponse.getRequestParameter("incidentid");
        String resolution = ajaxresponse.getRequestParameter("resolution");
        String triage = ajaxresponse.getRequestParameter("triage");
        String suppressduration = ajaxresponse.getRequestParameter("suppressduration");
        PropertyList props = new PropertyList();
        props.setProperty("incidentid", incidentid);
        props.setProperty("resolution", resolution);
        props.setProperty("triage", triage);
        props.setProperty("suppressduration", suppressduration);
        this.getActionProcessor().processActionClass(ClearAlert.class.getName(), props);
    }
}

