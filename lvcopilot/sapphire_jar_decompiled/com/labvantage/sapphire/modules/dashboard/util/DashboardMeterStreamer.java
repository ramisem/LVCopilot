/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.ServletRequest
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.jfree.chart.JFreeChart
 */
package com.labvantage.sapphire.modules.dashboard.util;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.util.DashboardMeter;
import com.labvantage.sapphire.servlet.command.BaseRequest;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jfree.chart.JFreeChart;
import sapphire.util.Logger;
import sapphire.xml.PropertyList;

public class DashboardMeterStreamer
extends BaseRequest {
    private static final String LABVANTAGE_CVS_ID = "$Revision: 50515 $";
    public static final String MEASUREMENT_SDC = "LV_Measurement";
    private String userlanguage = "";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext context) {
        try {
            String elementid = request.getParameter("elementid");
            String pageid = request.getParameter("pageid");
            String meterid = request.getParameter("meterid");
            PropertyList props = BaseGizmo.getElementProperties((ServletRequest)request, pageid, elementid);
            this.setUserlanguage(props.getProperty("userlanguage", ""));
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0L);
            props.setProperty("currentuser", this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).getSysuserId());
            response.setContentType("image/png");
            try (ServletOutputStream ouputStream = response.getOutputStream();){
                int height;
                int width;
                DashboardMeter dm = new DashboardMeter();
                if (meterid == null || meterid.length() == 0) {
                    dm.setSDCProcessor(this.getSDCProcessor());
                    dm.setQp(this.getQueryProcessor());
                    dm.setTranslationProcessor(this.getTranslationProcessor());
                    dm.createMeter(props);
                } else {
                    Object chart = request.getSession().getAttribute(meterid);
                    if (chart != null && chart instanceof JFreeChart) {
                        dm.setChart((JFreeChart)chart);
                        request.getSession().removeAttribute(meterid);
                    } else {
                        dm.setSDCProcessor(this.getSDCProcessor());
                        dm.setQp(this.getQueryProcessor());
                        dm.setTranslationProcessor(this.getTranslationProcessor());
                        dm.createMeter(props);
                    }
                }
                try {
                    width = Integer.parseInt(request.getParameter("width"));
                }
                catch (Exception e) {
                    try {
                        width = Integer.parseInt(props.getProperty("width", "200")) - 15;
                    }
                    catch (Exception e2) {
                        width = 185;
                    }
                }
                try {
                    height = Integer.parseInt(request.getParameter("height"));
                }
                catch (Exception e) {
                    try {
                        height = Integer.parseInt(props.getProperty("height", "200")) - 30;
                    }
                    catch (Exception e2) {
                        height = 170;
                    }
                }
                response.setContentType("image/jpeg");
                int size = dm.streamMeter((OutputStream)ouputStream, dm.getChart(), height, width, false);
                response.setContentLength(size);
            }
        }
        catch (Exception e) {
            Logger.logStackTrace(e);
        }
    }

    public String getUserlanguage() {
        return this.userlanguage;
    }

    public void setUserlanguage(String userlanguage) {
        this.userlanguage = userlanguage;
    }
}

