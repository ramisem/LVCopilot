/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.modules.statsmonitor.gwt.server;

import com.labvantage.sapphire.admin.system.StatusProcessor;
import com.labvantage.sapphire.modules.statsmonitor.MonitorConstants;
import java.util.Calendar;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.SapphireException;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.xml.PropertyList;

public class GetLiveValues
extends BaseAjaxRequest
implements MonitorConstants {
    static final String LABVANTAGE_CVS_ID = ": 1.1 $";

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        try {
            StatusProcessor sp = new StatusProcessor(this.getConnectionid());
            String ygroupid = request.getParameter("ygroupid");
            String yitemid = request.getParameter("yitemid");
            String y2groupid = request.getParameter("y2groupid");
            String y2itemid = request.getParameter("y2itemid");
            boolean first = "true".equals(request.getParameter("first"));
            PropertyList returnProps = new PropertyList();
            if (ygroupid != null && ygroupid.length() > 0) {
                returnProps.setProperty("yvalue", "" + sp.getStatsMonitoringValue(ygroupid, yitemid));
            }
            if (y2groupid != null && y2groupid.length() > 0) {
                returnProps.setProperty("y2value", "" + sp.getStatsMonitoringValue(y2groupid, y2itemid));
            }
            returnProps.setProperty("first", "" + first);
            returnProps.setProperty("capturedt", "" + Calendar.getInstance().getTimeInMillis());
            this.write(returnProps.toJSONString(false));
        }
        catch (SapphireException e) {
            this.logError("Problem loading up stats data: " + e.getMessage(), e);
        }
    }
}

