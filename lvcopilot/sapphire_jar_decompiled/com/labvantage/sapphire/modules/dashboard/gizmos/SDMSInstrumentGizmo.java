/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.util.http.HttpUtil;
import sapphire.SapphireException;
import sapphire.pageelements.BaseGizmo;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDMSInstrumentGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";

    @Override
    public PropertyList getParameters() {
        PropertyList out = super.getParameters();
        return out;
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/sdmsinstrumentgizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/sdmsinstrumentgizmo.css", this.pageContext) + "\">");
        String instrumentid = this.element.getProperty("instrumentid", this.getParameter("instrumentid", LABVANTAGE_CVS_ID));
        html.append("<div class=\"ig_instrument\" data-instrumentid=\"").append(instrumentid).append("\" onclick=\"sdmsInstrumentGizmo.clickInstrument(this, event);\">");
        html.append("<div class=\"ig_row\">");
        html.append("<div class=\"ig_block ig_instrumentid\" data-url=\"").append(this.element.getProperty("instrumentlink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("instrumenttarget", "_blank")).append("\">");
        html.append(instrumentid);
        html.append("</div>");
        html.append("<div class=\"ig_block ig_status\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        int x = 2;
        try {
            x = Integer.parseInt(this.element.getPropertyList("gizmoprops").getProperty("size_x"));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (x > 2) {
            html.append("<div class=\"ig_block ig_collectorid\" data-url=\"").append(this.element.getProperty("collectorslink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("collectorstarget", "_blank")).append("\">");
            html.append(LABVANTAGE_CVS_ID);
            html.append("</div>");
        }
        html.append("<div class=\"ig_block ig_reboot\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("<div class=\"ig_block ig_incidents\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        if (x < 3) {
            html.append("<div class=\"ig_row\">");
            html.append("<div class=\"ig_block ig_collectorid\" data-url=\"").append(this.element.getProperty("collectorslink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("collectorstarget", "_blank")).append("\">");
            html.append(LABVANTAGE_CVS_ID);
            html.append("</div>");
            html.append("</div>");
        }
        html.append("<div class=\"ig_row\">");
        html.append("<div class=\"ig_block ig_modeltype\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        html.append("<div class=\"ig_row\">");
        html.append("<div class=\"ig_block ig_lastPing\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("<div class=\"ig_block ig_running\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        html.append("<div class=\"ig_row\">");
        html.append("<div class=\"ig_block ig_dcsummary\" data-url=\"").append(this.element.getProperty("datacaptureslink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("datacapturestarget", "_blank")).append("\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        html.append("<div class=\"ig_row\">");
        html.append("<div class=\"ig_datacaptures\" data-url=\"").append(this.element.getProperty("datacapturelink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("datacapturetarget", "_blank")).append("\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    @Override
    public String getScript() {
        StringBuilder script = new StringBuilder();
        String instrumentid = this.element.getProperty("instrumentid", this.getParameter("instrumentid", LABVANTAGE_CVS_ID));
        if (instrumentid.length() > 0) {
            script.append("if ( typeof(sdmsInstrumentGizmo) != 'undefined'){ sdmsInstrumentGizmo.registerInstrument('").append(instrumentid).append("','").append(this.getGizmoStyle().toString()).append("'); }");
        }
        try {
            PropertyList collectorDefaults = this.getConfigurationProcessor().getPolicy("SDMSPolicy", "Sapphire Custom").getPropertyListNotNull("collectordefaults");
            int slowPingSeconds = Integer.parseInt(collectorDefaults.getProperty("serverpinginterval", "2"));
            int fastPingSeconds = Integer.parseInt(collectorDefaults.getProperty("fastserverpinginterval", "10"));
            script.append("sdmsInstrumentGizmo.slowPingSeconds = " + slowPingSeconds + ";");
            script.append("sdmsInstrumentGizmo.fastPingSeconds = " + fastPingSeconds + ";");
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        return script.toString();
    }

    @Override
    public String getIconHtml() {
        int size = 16;
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/sdmsinstrumentgizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/sdmsinstrumentgizmo.css", this.pageContext) + "\">");
        String instrumentid = this.element.getProperty("instrumentid", this.getParameter("instrumentid", LABVANTAGE_CVS_ID));
        if (this.element.getPropertyList("gizmoprops") != null) {
            this.element.getPropertyList("gizmoprops").setProperty("image", "FlatBlackMicroscope");
        }
        html.append("<div class=\"ig_instrument\" data-instrumentid=\"").append(instrumentid).append("\" onclick=\"sdmsInstrumentGizmo.clickInstrument(this, event);\">");
        html.append("<div class=\"ig_block ig_reboot\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("<div class=\"ig_incidents\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        if (super.getGizmoStyle().showImage) {
            String h = this.getHelpText();
            h = SafeHTML.encodeForHTML(h, true);
            html.append("<span title=\"").append(h).append("\" ").append(super.getGizmoStyle().className.length() > 0 ? " class=\"" + super.getGizmoStyle().className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
            html.append(this.getIcon());
            html.append("</span>");
        }
        String iurl = StringUtil.replaceAll(this.element.getProperty("instrumentlink", LABVANTAGE_CVS_ID), "[instrumentid]", instrumentid);
        String itarget = this.element.getProperty("instrumenttarget", "_blank");
        iurl = "javascript:sdmsInstrumentGizmo.navigate( '" + iurl + "', '" + itarget + "');";
        if (this.getGizmoStyle() == BaseGizmo.GizmoStyle.LARGETEXT) {
            html.append("<span id=\"").append(this.elementid).append("_changetext\">");
            if (this.element.getProperty("instrumentlink", LABVANTAGE_CVS_ID).length() > 0) {
                html.append("<a href=\"").append(iurl).append("\">");
            }
            html.append(instrumentid);
            if (this.element.getProperty("instrumentlink", LABVANTAGE_CVS_ID).length() > 0) {
                html.append("</a>");
            }
            html.append("</span>&nbsp<span class=\"ig_running\"></span>");
        } else {
            html.append("<span id=\"").append(this.elementid).append("_changetext\">");
            if (this.element.getProperty("instrumentlink", LABVANTAGE_CVS_ID).length() > 0) {
                html.append("<a href=\"").append(iurl).append("\">");
            }
            html.append(instrumentid);
            if (this.element.getProperty("instrumentlink", LABVANTAGE_CVS_ID).length() > 0) {
                html.append("</a>");
            }
            html.append("</span><br><span class=\"ig_running\"></span><br><span class=\"ig_dcsummary\" data-url=\"").append(this.element.getProperty("datacaptureslink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("datacapturestarget", "_blank")).append("\"></span>");
        }
        html.append("</div>");
        return html.toString();
    }
}

