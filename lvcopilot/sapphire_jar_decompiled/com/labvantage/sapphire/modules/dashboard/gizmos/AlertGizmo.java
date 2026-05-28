/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AlertGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";

    @Override
    public PropertyList getParameters() {
        PropertyList out = super.getParameters();
        return out;
    }

    private String buildIN(String idCol, String ids) {
        StringBuilder out = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        String[] dc = StringUtil.split(ids, ";");
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < dc.length; ++i) {
            if (part.length() > 0) {
                part.append(",");
            }
            part.append("'").append(dc[i]).append("'");
            if (i < 900) continue;
            if (sql.length() > 0) {
                sql.append(" OR ");
            }
            sql.append(idCol).append(" IN ");
            sql.append("(").append((CharSequence)part).append(")");
            part = new StringBuilder();
        }
        if (part.length() > 0) {
            if (sql.length() > 0) {
                sql.append(" OR ");
            }
            sql.append(idCol).append(" IN ");
            sql.append("(").append((CharSequence)part).append(")");
        }
        return sql.toString();
    }

    @Override
    public String getHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/alertgizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/alertgizmo.css", this.pageContext) + "\">");
        String error = LABVANTAGE_CVS_ID;
        String[] sdcids = StringUtil.split(this.element.getProperty("sdcid", LABVANTAGE_CVS_ID), ";");
        String[] parameterids = StringUtil.split(this.element.getProperty("parameterid", LABVANTAGE_CVS_ID), ";");
        boolean hasSDMSAdminRole = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).hasRole("SDMSAdmin");
        if (sdcids.length != parameterids.length) {
            error = "Invalid sdc to parameter number.";
        } else {
            String sourcekeyid1 = LABVANTAGE_CVS_ID;
            String sdcid = LABVANTAGE_CVS_ID;
            for (int i = 0; i < parameterids.length; ++i) {
                String p = parameterids[i];
                String v = this.getParameter(p, LABVANTAGE_CVS_ID);
                this.subscribeToParameter(p);
                if (v.length() <= 0) continue;
                sourcekeyid1 = v;
                sdcid = sdcids[i];
            }
            if (sourcekeyid1.length() > 0) {
                DataSet incidents = null;
                String sqlWhere = " incident.incidentid = incidentitem.incidentid AND incident.incidentstatus in ('Initial') AND incidentitem.sourcesdcid = '" + sdcid + "'";
                sqlWhere = sourcekeyid1.contains(";") ? sqlWhere + "  AND " + this.buildIN("incidentitem.sourcekeyid1", sourcekeyid1) : sqlWhere + "  AND incidentitem.sourcekeyid1 = '" + sourcekeyid1 + "'";
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_Incdt");
                sdiRequest.setQueryFrom("incident, incidentitem");
                sdiRequest.setQueryWhere(sqlWhere);
                sdiRequest.setRequestItem("primary");
                sdiRequest.setQueryOrderBy("incident.incidentdt desc");
                SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                incidents = sdiData.getDataset("primary");
                HashMap<String, String> filterInc = new HashMap<String, String>();
                filterInc.put("severity", "Failure");
                DataSet failures = incidents.getFilteredDataSet(filterInc);
                filterInc = new HashMap();
                filterInc.put("severity", "Warning");
                DataSet warnings = incidents.getFilteredDataSet(filterInc);
                TabGroup tabGroup = new TabGroup();
                tabGroup.setId("alerts");
                tabGroup.setPageContext(this.pageContext);
                tabGroup.setAppearance("modern");
                RequestContext requestContext = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext");
                PropertyList userConfig = requestContext.getPropertyList("userconfig");
                try {
                    tabGroup.setSelectedTab(Integer.parseInt(userConfig.getProperty("alertgizmotab", "0")));
                }
                catch (Exception exception) {
                    // empty catch block
                }
                TranslationProcessor tp = this.getTranslationProcessor();
                Tab tab = new Tab();
                tab.setPageContext(this.pageContext);
                tab.setId("meter");
                tab.setText(tp.translate("Current Alerts"));
                tab.setAction("alertGizmo.changeTab(0)");
                StringBuilder meterHTML = new StringBuilder();
                meterHTML.append("<div class=\"alert_metercontainer\">");
                if (warnings.size() > 0 && failures.size() > 0) {
                    meterHTML.append("<div class=\"alert_meter alert_fail alert_big\" data-elementid=\"").append(this.elementid).append("\" title=\"").append(failures.size() + " Failure" + (failures.size() > 1 ? "s" : LABVANTAGE_CVS_ID)).append("\">");
                    meterHTML.append(failures.size());
                    meterHTML.append("</div>");
                    meterHTML.append("<div class=\"alert_meter alert_warn alert_small\" data-elementid=\"").append(this.elementid).append("\" title=\"").append(warnings.size() + " Warning" + (warnings.size() > 1 ? "s" : LABVANTAGE_CVS_ID)).append("\">");
                    meterHTML.append(warnings.size());
                    meterHTML.append("</div>");
                } else if (failures.size() > 0) {
                    meterHTML.append("<div class=\"alert_meter alert_fail alert_big\" data-elementid=\"").append(this.elementid).append("\" title=\"").append(failures.size() + " Failure" + (failures.size() > 1 ? "s" : LABVANTAGE_CVS_ID)).append("\">");
                    meterHTML.append(failures.size());
                    meterHTML.append("</div>");
                } else if (warnings.size() > 0) {
                    meterHTML.append("<div class=\"alert_meter alert_warn alert_big\" data-elementid=\"").append(this.elementid).append("\" title=\"").append(warnings.size() + " Warning" + (warnings.size() > 1 ? "s" : LABVANTAGE_CVS_ID)).append("\">");
                    meterHTML.append(warnings.size());
                    meterHTML.append("</div>");
                } else {
                    meterHTML.append("<div class=\"alert_meter alert_ok alert_big\" data-elementid=\"").append(this.elementid).append("\" title=\"").append("No alerts").append("\">");
                    meterHTML.append(0);
                    meterHTML.append("</div>");
                }
                meterHTML.append("</div>");
                tab.setContent(meterHTML.toString());
                tabGroup.setTab(tab);
                tab = new Tab();
                tab.setPageContext(this.pageContext);
                tab.setId("list");
                tab.setText(tp.translate("All Alerts"));
                tab.setAction("alertGizmo.changeTab(1)");
                StringBuilder listHTML = new StringBuilder();
                if (incidents.size() > 0) {
                    int i;
                    PropertyListCollection buttons = this.getElementProperties().getCollection("buttons");
                    if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
                        for (i = 0; i < buttons.size(); ++i) {
                            PropertyList buttonPropertyList = buttons.getPropertyList(i);
                            boolean buttonShow = buttonPropertyList.getProperty("show", "Y").equalsIgnoreCase("Y");
                            String buttonFunction = buttonPropertyList.getProperty("buttonfunction");
                            if (!buttonShow || !buttonFunction.equals("CA")) continue;
                            listHTML.append("<div class=\"alert_buttons\">");
                            Button button = new Button(this.pageContext);
                            button.setAction("alertGizmo.clearAlerts( '" + sdcid + "','" + sourcekeyid1 + "')");
                            button.setText(tp.translate("Clear Alerts"));
                            listHTML.append(button.getHtml());
                            listHTML.append("&nbsp;&nbsp;");
                            listHTML.append("</div>");
                        }
                    } else if (hasSDMSAdminRole) {
                        listHTML.append("<div class=\"alert_buttons\">");
                        Button button = new Button(this.pageContext);
                        button.setAction("alertGizmo.clearAlerts( '" + sdcid + "','" + sourcekeyid1 + "')");
                        button.setText(tp.translate("Clear Alerts"));
                        listHTML.append(button.getHtml());
                        listHTML.append("&nbsp;&nbsp;");
                        listHTML.append("</div>");
                    }
                    listHTML.append("<div class=\"alert_content\">");
                    listHTML.append("<div class=\"alert_incidents alert_subcontent\">");
                    incidents.setDateDisplayFormat("logdt", new SimpleDateFormat("HH:mm:ss "));
                    listHTML.append("<table border=\"1\" class=\"alert_table\" cellpadding=\"2\" cellspacing=\"0\">");
                    listHTML.append("<tr>");
                    listHTML.append("<td>&nbsp;</td>");
                    listHTML.append("<td>").append(tp.translate("Incident")).append("</td>");
                    listHTML.append("<td>").append(tp.translate("Date")).append("</td>");
                    listHTML.append("<td>").append(tp.translate("Severity")).append("</td>");
                    listHTML.append("<td>").append(tp.translate("Type")).append("</td>");
                    listHTML.append("<td>").append(tp.translate("Description")).append("</td>");
                    listHTML.append("<td>").append(tp.translate("Details")).append("</td>");
                    listHTML.append("</tr>");
                    for (i = 0; i < incidents.size(); ++i) {
                        String incidentid = incidents.getValue(i, "incidentid");
                        String incidentDt = incidents.getValue(i, "incidentdt");
                        String incidentDesc = incidents.getValue(i, "incidentdesc");
                        String severity = incidents.getValue(i, "severity");
                        String incidentType = incidents.getValue(i, "incidenttype");
                        String explanation = incidents.getValue(i, "explanation");
                        boolean complete = incidents.getValue(i, "incidentstatus").equals("Completed");
                        listHTML.append("<tr>");
                        if (!complete) {
                            if (severity.equalsIgnoreCase("warning")) {
                                listHTML.append("<td><img src=\"rc?command=image&image=FlatBlackWarning&color=orange\"/></td>");
                            } else if (severity.equalsIgnoreCase("failure")) {
                                listHTML.append("<td><img src=\"rc?command=image&image=FlatBlackExclamation1&color=red\"/></td>");
                            } else {
                                listHTML.append("<td><img src=\"rc?command=image&image=FlatBlackInformationCircle&color=black\"/></td>");
                            }
                        } else {
                            listHTML.append("<td>&nbsp;</td>");
                        }
                        listHTML.append("<td>" + incidentid + "</td>");
                        listHTML.append("<td>" + incidentDt + "</td>");
                        listHTML.append("<td>" + severity + "</td>");
                        listHTML.append("<td>" + incidentType + "</td>");
                        listHTML.append("<td>" + incidentDesc + "</td>");
                        listHTML.append("<td>" + explanation + "</td>");
                        listHTML.append("</tr>");
                    }
                    listHTML.append("<table>");
                } else {
                    listHTML.append("No alerts found");
                }
                listHTML.append("</div>");
                listHTML.append("</div>");
                tab.setContent(listHTML.toString());
                tabGroup.setTab(tab);
                html.append(tabGroup.getHtml());
            } else {
                html.append("No data provided for alerts.");
            }
        }
        if (error.length() > 0) {
            html.append("<div style=\"color:red;\">").append(error).append("</div>");
        }
        return html.toString();
    }

    @Override
    public String getScript() {
        StringBuilder script = new StringBuilder();
        String paramid = LABVANTAGE_CVS_ID;
        String[] parameterids = StringUtil.split(this.element.getProperty("parameterid", LABVANTAGE_CVS_ID), ";");
        for (int i = 0; i < parameterids.length; ++i) {
            String p = parameterids[i];
            String v = this.getParameter(p, LABVANTAGE_CVS_ID);
            if (v.length() <= 0) continue;
            paramid = p;
        }
        script.append("if ( typeof(alertGizmo) != 'undefined'){ alertGizmo.registerAlertGizmo('").append(this.elementid).append("','").append(paramid).append("'); }");
        return script.toString();
    }

    @Override
    public String getIconHtml() {
        StringBuilder s = new StringBuilder();
        s.append("<div style=\"height:100%;display: flex;justify-content: center; align-items: center;\">");
        s.append("<div style=\"flex: 0 0 auto;\">");
        s.append(this.getTranslationProcessor().translate("Enlarge To View Details"));
        s.append("</div>");
        s.append("</div>");
        return s.toString();
    }
}

