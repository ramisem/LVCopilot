/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.ListGizmo;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.util.SafeHTML;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDMSDataCaptureGizmo
extends sapphire.pageelements.BaseGizmo
implements SDMSConstants {
    private static final String LABVANTAGE_CVS_ID = "";
    private String datacaptureid = "";
    private ListGizmo listGizmo = null;

    @Override
    public PropertyList getParameters() {
        PropertyList out = super.getParameters();
        return out;
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/sdmsdatacapturegizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/sdmsdatacapturegizmo.css", this.pageContext) + "\">");
        Mode mode = Mode.SINGLE;
        try {
            mode = Mode.valueOf(this.element.getProperty("mode", Mode.SINGLE.toString()).toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (mode == Mode.SINGLE) {
            this.datacaptureid = this.element.getProperty("datacaptureid", LABVANTAGE_CVS_ID);
            html.append("<div class=\"dcg_datacapture\" data-datacaptureid=\"").append(this.datacaptureid).append("\" onclick=\"sdmsDataCaptureGizmo.clickDataCapture(this, event);\">");
            html.append("<div class=\"dcg_row\">");
            html.append("<div class=\"dcg_block dcg_datacaptureid\">");
            html.append(this.datacaptureid);
            html.append("</div>");
            html.append("<div class=\"dcg_block dcg_status\">");
            html.append(LABVANTAGE_CVS_ID);
            html.append("</div>");
            html.append("<div class=\"dcg_block dcg_incidents\">");
            html.append(LABVANTAGE_CVS_ID);
            html.append("</div>");
            html.append("</div>");
            html.append("<div class=\"dcg_row\">");
            html.append("<div class=\"dcg_block dcg_instrumentid\" data-url=\"").append(this.element.getProperty("instrumentlink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("instrumenttarget", "_blank")).append("\">");
            html.append(LABVANTAGE_CVS_ID);
            html.append("</div>");
            html.append("<div class=\"dcg_block dcg_collectorid\" data-url=\"").append(this.element.getProperty("collectorlink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("collectortarget", "_blank")).append("\">");
            html.append(LABVANTAGE_CVS_ID);
            html.append("</div>");
            html.append("</div>");
            html.append("<div class=\"dcg_row\">");
            html.append("<div class=\"ig_block dcg_lastPing\">");
            html.append(LABVANTAGE_CVS_ID);
            html.append("</div>");
            html.append("</div>");
            html.append("<div class=\"dcg_row\">");
            html.append("<div class=\"dcg_attachments\" data-url=\"").append(this.element.getProperty("attachmentlink", LABVANTAGE_CVS_ID)).append("\" data-target=\"").append(this.element.getProperty("attachmenttarget", "_blank")).append("\">");
            html.append(LABVANTAGE_CVS_ID);
            html.append("</div>");
            html.append("</div>");
            html.append("</div>");
        } else if (mode == Mode.MULTIPLE) {
            ArrayList<String> ignore = new ArrayList<String>();
            ignore.add("datacapturelink");
            ignore.add("instrumentlink");
            ignore.add("collectorlink");
            BaseGizmo.evaluateExpression(this.getGizmoDefId(), this.element, BaseGizmo.I18NFormat.CLIENT, this.getParameters(), this.getGroovyBindMap(), this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()), ignore, true);
            String querywhere = this.element.getProperty("querywhere", LABVANTAGE_CVS_ID);
            String queryorderby = this.element.getProperty("queryorderby", LABVANTAGE_CVS_ID);
            this.listGizmo = new ListGizmo();
            this.listGizmo.setElementid(this.getElementid());
            this.listGizmo.setGizmoLocation(this.getGizmoLocation());
            this.listGizmo.setGizmoDefId(this.getGizmoDefId());
            this.listGizmo.setGizmoStyle(this.getGizmoStyle());
            this.listGizmo.setPageContext(this.pageContext);
            PropertyList element = new PropertyList();
            element.setProperty("sdcid", "LV_DataCapture");
            PropertyList defaultquery = new PropertyList();
            defaultquery.setProperty("queryfrom", this.getSDCProcessor().getProperty("LV_DataCapture", "tableid"));
            defaultquery.setProperty("querywhere", querywhere);
            defaultquery.setProperty("queryorderby", queryorderby);
            element.setProperty("defaultquery", defaultquery);
            PropertyListCollection sortbys = new PropertyListCollection();
            PropertyList sortby = new PropertyList();
            sortby.setProperty("id", queryorderby);
            sortby.setProperty("columnid", queryorderby);
            sortbys.add(sortby);
            element.setProperty("sortby", sortbys);
            element.setProperty("selectortype", "checkbox");
            element.setProperty("rowclickselection", "Y");
            element.setProperty("rowsperpage", "50");
            element.setProperty("retrievelimit", "3000");
            element.setProperty("retrievelimit", "3000");
            element.setProperty("selectiontype", "Hidden");
            element.setProperty("hidelistcheckboxes", "Y");
            element.setProperty("disablelistlayout", "Y");
            element.setProperty("iframeload", "if(typeof(sdmsDataCaptureGizmo)!='undefined'){sdmsDataCaptureGizmo.iframeOnload(this)}");
            PropertyListCollection columns = new PropertyListCollection();
            TranslationProcessor tp = this.getTranslationProcessor();
            PropertyList column = new PropertyList();
            column.setProperty("columnid", "datacaptureid");
            column.setProperty("title", tp.translate("Data Capture"));
            PropertyList link = new PropertyList();
            link.setProperty("href", this.getElementProperties().getProperty("datacapturelink", "rc?command=page&page=LV_DataCaptureMaint&sdcid=LV_DataCapture&mode=Edit&keyid1=[columnid=datacaptureid]"));
            String target = this.getElementProperties().getProperty("datacapturetarget", "_blank");
            link.setProperty("target", target);
            column.setProperty("link", link);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "datacapturestatus");
            column.setProperty("title", tp.translate("Status"));
            column.setProperty("width", "100");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "createdt");
            column.setProperty("title", tp.translate("Captured Date"));
            column.setProperty("width", "100");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "instrumentid");
            column.setProperty("title", tp.translate("Instrument"));
            column.setProperty("width", "100");
            link = new PropertyList();
            link.setProperty("href", this.element.getProperty("instrumentlink", LABVANTAGE_CVS_ID));
            target = this.element.getProperty("instrumenttarget", LABVANTAGE_CVS_ID).equalsIgnoreCase("_self") ? "_nav_frame1" : this.element.getProperty("instrumenttarget", LABVANTAGE_CVS_ID);
            link.setProperty("target", target);
            column.setProperty("link", link);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "sdmscollectorid");
            column.setProperty("title", tp.translate("Collector"));
            column.setProperty("width", "100");
            link = new PropertyList();
            link.setProperty("href", this.element.getProperty("collectorlink", LABVANTAGE_CVS_ID));
            target = this.element.getProperty("collectortarget", LABVANTAGE_CVS_ID).equalsIgnoreCase("_self") ? "_nav_frame1" : this.element.getProperty("collectortarget", LABVANTAGE_CVS_ID);
            link.setProperty("target", target);
            column.setProperty("link", link);
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "(SELECT count( sa.sdiattachmentid ) FROM sdiattachment sa WHERE sa.sdcid='LV_DataCapture' AND sa.keyid1=datacapture.datacaptureid) attcount");
            column.setProperty("title", " ");
            column.setProperty("displayvalue", ">0=<img src=\"rc?command=image&image=FlatBlackPaperclip\" border=0 id=\"attachment_[keyid1]\" title=\"[attcount] Attachments\">;0=;");
            column.setProperty("width", "30");
            columns.add(column);
            column = new PropertyList();
            column.setProperty("columnid", "(SELECT count( ii.incidentitemid ) FROM incidentitem ii WHERE ii.sourcesdcid='LV_DataCapture' AND ii.sourcekeyid1=datacapture.datacaptureid) incidents");
            column.setProperty("displayvalue", ">0=<img src=\"rc?command=image&image=FlatBlackExclamation1&color=red\" border=0 id=\"attachment_[keyid1]\" title=\"[incidents] Alerts\">;0=;");
            column.setProperty("title", " ");
            column.setProperty("width", "30");
            columns.add(column);
            element.setProperty("columns", columns);
            this.listGizmo.setElementProperties(element);
            html.append(this.listGizmo.getHtml());
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        StringBuilder script = new StringBuilder();
        Mode mode = Mode.SINGLE;
        try {
            mode = Mode.valueOf(this.element.getProperty("mode", Mode.SINGLE.toString()).toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (mode == Mode.SINGLE) {
            this.datacaptureid = this.element.getProperty("datacaptureid", LABVANTAGE_CVS_ID);
            if (this.datacaptureid.length() > 0) {
                script.append("if ( typeof(sdmsDataCaptureGizmo) != 'undefined'){ sdmsDataCaptureGizmo.registerDataCapture('").append(this.element.getProperty("datacaptureid", LABVANTAGE_CVS_ID)).append("','").append(mode.toString()).append("'); }");
            }
        } else if (this.listGizmo != null) {
            script.append(this.listGizmo.getScript());
            script.append("if ( typeof(sdmsDataCaptureGizmo) != 'undefined'){ sdmsDataCaptureGizmo.register('").append(mode.toString()).append("','").append(this.elementid).append("'); }");
        }
        try {
            PropertyList collectorDefaults = this.getConfigurationProcessor().getPolicy("SDMSPolicy", "Sapphire Custom").getPropertyListNotNull("collectordefaults");
            int slowPingSeconds = Integer.parseInt(collectorDefaults.getProperty("serverpinginterval", "2"));
            int fastPingSeconds = Integer.parseInt(collectorDefaults.getProperty("fastserverpinginterval", "10"));
            script.append("sdmsDataCaptureGizmo.slowPingSeconds = " + slowPingSeconds + ";");
            script.append("sdmsDataCaptureGizmo.fastPingSeconds = " + fastPingSeconds + ";");
        }
        catch (SapphireException sapphireException) {
            // empty catch block
        }
        return script.toString();
    }

    @Override
    public String getIconHtml() {
        int size = 16;
        Mode mode = Mode.SINGLE;
        try {
            mode = Mode.valueOf(this.element.getProperty("mode", Mode.SINGLE.toString()).toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/sdmsdatacapturegizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/sdmsdatacapturegizmo.css", this.pageContext) + "\">");
        String datacaptureid = this.element.getProperty("datacaptureid", LABVANTAGE_CVS_ID);
        if (this.element.getPropertyList("gizmoprops") != null) {
            this.element.getPropertyList("gizmoprops").setProperty("image", "FlatBlackMicroscope");
        }
        if (mode == Mode.SINGLE) {
            html.append("<div class=\"dcg_datacapture\" data-datacaptureid=\"").append(datacaptureid).append("\" onclick=\"sdmsDataCaptureGizmo.clickDataCapture(this, event);\">");
        } else {
            html.append("<div class=\"dcg_datacapture\"\">");
        }
        html.append("<div class=\"dcg_incidents\">");
        html.append(LABVANTAGE_CVS_ID);
        html.append("</div>");
        if (super.getGizmoStyle().showImage) {
            String h = this.getHelpText();
            h = SafeHTML.encodeForHTML(h, true);
            html.append("<span title=\"").append(h).append("\" ").append(super.getGizmoStyle().className.length() > 0 ? " class=\"" + super.getGizmoStyle().className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
            html.append(this.getIcon());
            html.append("</span>");
        }
        if (mode == Mode.SINGLE) {
            if (this.getGizmoStyle() == BaseGizmo.GizmoStyle.LARGETEXT) {
                html.append("<span id=\"").append(this.elementid).append("_changetext\">").append(datacaptureid).append("</span>");
            } else {
                html.append("<span id=\"").append(this.elementid).append("_changetext\">").append(datacaptureid).append("</span>");
            }
        } else {
            html.append("<span id=\"").append(this.elementid).append("_changetext\">").append("Data Captures").append("</span>");
        }
        html.append("</div>");
        return html.toString();
    }

    public static enum Mode {
        SINGLE,
        MULTIPLE;

    }
}

