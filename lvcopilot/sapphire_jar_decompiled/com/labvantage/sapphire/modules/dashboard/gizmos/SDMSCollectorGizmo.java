/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.util.http.HttpUtil;
import sapphire.SapphireException;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SDMSCollectorGizmo
extends sapphire.pageelements.BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";

    @Override
    public PropertyList getParameters() {
        PropertyList out = super.getParameters();
        return out;
    }

    @Override
    public String getHtml() {
        String ctarget;
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/sdmscollectorgizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/sdmscollectorgizmo.css", this.pageContext) + "\">");
        String colid = this.element.getProperty("sdmscollectorid", this.getParameter("sdmscollectorid", LABVANTAGE_CVS_ID));
        String url = this.element.getProperty("instrumenturl", LABVANTAGE_CVS_ID);
        String target = this.element.getProperty("instrumenttarget", "_blank");
        html.append("<div class=\"ctg_collector\" data-sdmscollectorid=\"").append(colid).append("\" onclick=\"sdmsCollectorGizmo.clickCollector(this, event);\">");
        html.append("<div class=\"ctg_row\">");
        html.append("<div class=\"ctg_block ctg_collectorid\">");
        if (this.element.getProperty("collectorurl", LABVANTAGE_CVS_ID).length() > 0) {
            String curl = StringUtil.replaceAll(this.element.getProperty("collectorurl", LABVANTAGE_CVS_ID), "[sdmscollectorid]", colid);
            ctarget = this.element.getProperty("collectortarget", "_blank");
            curl = "javascript:sdmsCollectorGizmo.navigate( '" + curl + "', '" + ctarget + "');";
            html.append("<a href=\"").append(curl).append("\">").append(colid).append("</a>");
        } else {
            html.append("<span>").append(colid).append("</span>");
        }
        html.append("</div>");
        html.append("<div class=\"ctg_block ctg_status\">");
        html.append("</div>");
        if (this.element.getProperty("allcollectorsurl", LABVANTAGE_CVS_ID).length() > 0) {
            html.append("<div class=\"ctg_block ctg_allcollectors\">");
            String allcolurl = StringUtil.replaceAll(this.element.getProperty("allcollectorsurl", LABVANTAGE_CVS_ID), "[sdmscollectorid]", colid);
            ctarget = this.element.getProperty("allcollectorstarget", "_blank");
            allcolurl = "javascript:sdmsCollectorGizmo.navigate( '" + allcolurl + "', '" + ctarget + "');";
            html.append("<a href=\"").append(allcolurl).append("\">(").append(this.getTranslationProcessor().translate("show all")).append(")</a>");
            html.append("</div>");
        }
        html.append("<div class=\"ctg_block ctg_upgrade\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("<div class=\"ctg_block ctg_incidents\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        html.append("<div class=\"ctg_row\">");
        html.append("<div class=\"ctg_block ctg_internal\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("<div class=\"ctg_block ctg_direct\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        html.append("<div class=\"ctg_row\">");
        html.append("<div class=\"ctg_block ctg_lastPing\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("<div class=\"ctg_block ctg_hostname\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        html.append("<div class=\"ctg_row\">");
        html.append("<div class=\"ctg_instruments\" data-url=\"").append(url).append("\" data-target=\"").append(target).append("\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        return html.toString();
    }

    @Override
    public String getScript() {
        StringBuilder script = new StringBuilder();
        String colid = this.element.getProperty("sdmscollectorid", this.getParameter("sdmscollectorid", LABVANTAGE_CVS_ID));
        if (colid.length() > 0) {
            script.append("if ( typeof(sdmsCollectorGizmo) != 'undefined'){ sdmsCollectorGizmo.registerCollector('").append(colid).append("', '").append((Object)this.getGizmoStyle()).append("'); }");
        }
        try {
            PropertyList collectorDefaults = this.getConfigurationProcessor().getPolicy("SDMSPolicy", "Sapphire Custom").getPropertyListNotNull("collectordefaults");
            int slowPingSeconds = Integer.parseInt(collectorDefaults.getProperty("serverpinginterval", "2"));
            int fastPingSeconds = Integer.parseInt(collectorDefaults.getProperty("fastserverpinginterval", "10"));
            script.append("sdmsCollectorGizmo.slowPingSeconds = " + slowPingSeconds + ";");
            script.append("sdmsCollectorGizmo.fastPingSeconds = " + fastPingSeconds + ";");
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
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/sdmscollectorgizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/sdmscollectorgizmo.css", this.pageContext) + "\">");
        String colid = this.element.getProperty("sdmscollectorid", this.getParameter("sdmscollectorid", LABVANTAGE_CVS_ID));
        if (this.element.getPropertyList("gizmoprops") != null) {
            this.element.getPropertyList("gizmoprops").setProperty("image", "FlatBlackInboxIn");
        }
        html.append("<div class=\"ctg_collector\" data-sdmscollectorid=\"").append(colid).append("\" onclick=\"sdmsCollectorGizmo.clickCollector(this, event);\">");
        html.append("<div class=\"ctg_upgrade\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        html.append("<div class=\"ctg_incidents\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        BaseGizmo.evaluateExpression(this.getGizmoDefId(), this.element, BaseGizmo.I18NFormat.CLIENT, this.getParameters(), null, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        if (super.getGizmoStyle().showImage) {
            String h = this.getHelpText();
            h = SafeHTML.encodeForHTML(h, true);
            html.append("<span title=\"").append(h).append("\" ").append(super.getGizmoStyle().className.length() > 0 ? " class=\"" + super.getGizmoStyle().className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
            html.append(this.getIcon());
            html.append("</span>");
        }
        html.append("<span id=\"").append(this.elementid).append("_changetext\">").append(colid).append("</span>&nbsp<span class=\"ctg_status\"></span>");
        html.append("</div>");
        return html.toString();
    }
}

