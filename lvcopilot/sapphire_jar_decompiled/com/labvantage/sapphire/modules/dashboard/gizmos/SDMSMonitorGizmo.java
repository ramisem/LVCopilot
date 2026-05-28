/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.pageelements.controls.Button;
import com.labvantage.sapphire.pageelements.controls.Tab;
import com.labvantage.sapphire.pageelements.controls.TabGroup;
import com.labvantage.sapphire.util.http.HttpUtil;
import java.util.HashSet;
import java.util.Set;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseGizmo;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDMSMonitorGizmo
extends BaseGizmo {
    private static final String LABVANTAGE_CVS_ID = "";

    @Override
    public String getHtml() {
        boolean showIncidents = false;
        String collectorid = this.getElementProperties().getProperty("sdmscollectorid", this.getParameter("sdmscollectorid", LABVANTAGE_CVS_ID));
        String instrumentid = this.getElementProperties().getProperty("instrumentid", this.getParameter("instrumentid", LABVANTAGE_CVS_ID));
        String datacaptureid = this.getElementProperties().getProperty("datacaptureid", this.getParameter("datacaptureid", LABVANTAGE_CVS_ID));
        String sdiattachmentid = this.getElementProperties().getProperty("sdiattachmentid", this.getParameter("sdiattachmentid", LABVANTAGE_CVS_ID));
        Mode mode = Mode.COLLECTOR;
        try {
            mode = Mode.valueOf(this.getElementProperties().getProperty("mode", mode.toString()).toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        boolean defaultView = false;
        if (mode == Mode.COLLECTOR && collectorid.contains(";")) {
            defaultView = true;
        } else if (mode == Mode.INSTRUMENT && instrumentid.contains(";")) {
            defaultView = true;
        } else if (mode == Mode.DATACAPTURE && datacaptureid.contains(";")) {
            defaultView = true;
        }
        StringBuilder html = new StringBuilder();
        html.append("<script src=\"WEB-CORE/modules/dashboard/scripts/sdmsmonitorgizmo.js\"></script>");
        html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + HttpUtil.getCSS("WEB-CORE/modules/dashboard/style/sdmsmonitorgizmo.css", this.pageContext) + "\">");
        if (mode == Mode.COLLECTOR) {
            this.getCollectorHTML(html, collectorid, instrumentid, defaultView, showIncidents);
        } else if (mode == Mode.INSTRUMENT) {
            this.getInstrumentHTML(html, instrumentid, datacaptureid, collectorid, defaultView, showIncidents);
        } else if (mode == Mode.DATACAPTURE) {
            this.getDataCaptureHTML(html, datacaptureid, sdiattachmentid, instrumentid, defaultView, showIncidents);
        } else {
            html.append("Incorrect mode.");
        }
        return html.toString();
    }

    private String renderRoleBasedButtons(String sdmscollectorid, String instrumentid, String datadaptureid, PropertyListCollection buttons, Set<String> buttonFunctions) {
        StringBuilder buttonStateBuilder = new StringBuilder();
        boolean paused = false;
        boolean disabled = false;
        boolean internalflag = false;
        boolean instrumentPaused = false;
        boolean instrumentUnavailable = false;
        boolean collectorPaused = false;
        boolean collectorDisabled = false;
        if (sdmscollectorid != null && sdmscollectorid.length() > 0) {
            DataSet collector = this.getQueryProcessor().getPreparedSqlDataSet("SELECT pausedflag, disabledflag, internalflag FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{sdmscollectorid});
            paused = collector.getValue(0, "pausedflag").equals("Y");
            disabled = collector.getValue(0, "disabledflag").equals("Y");
            internalflag = collector.getValue(0, "internalflag").equals("Y");
        }
        if (instrumentid != null && instrumentid.length() > 0) {
            DataSet instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instr.sdmspausedflag,instr.instrumentstatus,instr.inserviceflag,sdmsc.pausedflag,sdmsc.disabledflag FROM instrument instr left join sdmscollector sdmsc on sdmsc.sdmscollectorid=instr.sdmscollectorid  WHERE instr.instrumentid=?", (Object[])new String[]{instrumentid});
            instrumentPaused = instrument.getValue(0, "sdmspausedflag", "N").equalsIgnoreCase("Y");
            instrumentUnavailable = instrument.getValue(0, "instrumentstatus", LABVANTAGE_CVS_ID).equalsIgnoreCase("Unavailable") || instrument.getValue(0, "inserviceflag", "Y").equalsIgnoreCase("N");
            collectorPaused = instrument.getValue(0, "pausedflag", "N").equalsIgnoreCase("Y");
            collectorDisabled = instrument.getValue(0, "disabledflag", "N").equalsIgnoreCase("Y");
        }
        if (buttons != null && buttons.size() > 0) {
            buttonStateBuilder.append("<div class=\"sdms_btn_actions\" style=\"display:inline\">");
            TranslationProcessor tp = this.getTranslationProcessor();
            for (int i = 0; i < buttons.size(); ++i) {
                String buttonFunction;
                PropertyList buttonPropertyList = buttons.getPropertyList(i);
                boolean buttonShow = buttonPropertyList.getProperty("show", "Y").equalsIgnoreCase("Y");
                if (instrumentid != null && instrumentid.length() > 0) {
                    boolean bl = buttonShow = !instrumentUnavailable && !collectorPaused && !collectorDisabled;
                }
                if (!buttonFunctions.contains(buttonFunction = buttonPropertyList.getProperty("buttonfunction")) || !buttonShow) continue;
                for (BUTTON_PROPERTIES buttonProperties : BUTTON_PROPERTIES.values()) {
                    String function = buttonProperties.getButtonFunction();
                    String buttonText = buttonProperties.getButtonText();
                    String buttonTip = buttonProperties.getButtonTips();
                    String buttonAction = buttonProperties.getButtonAction();
                    String buttonImage = buttonProperties.getButtonImage();
                    if (!buttonFunction.equals(function)) continue;
                    Button button = new Button(this.pageContext);
                    button.setAction(buttonAction);
                    button.setText(tp.translate(buttonText));
                    button.setTip(tp.translate(buttonTip));
                    button.setImg(buttonImage);
                    String displayStyle = "display:inline";
                    String divClass = LABVANTAGE_CVS_ID;
                    if (buttonFunction.equals("PC")) {
                        displayStyle = "display:" + (disabled || paused ? "none" : "inline");
                        divClass = "class=\"sdms_btn_pause\"";
                    } else if (buttonFunction.equals("RC")) {
                        displayStyle = "display:" + (disabled || !paused ? "none" : "inline");
                        divClass = "class=\"sdms_btn_resume\"";
                    } else if (buttonFunction.equals("DC")) {
                        displayStyle = "display:" + (disabled ? "none" : "inline");
                        divClass = "class=\"sdms_btn_disable\"";
                    } else if (buttonFunction.equals("EC")) {
                        displayStyle = "display:" + (!disabled ? "none" : "inline");
                        divClass = "class=\"sdms_btn_enable\"";
                    } else if (buttonFunction.equals("UC")) {
                        displayStyle = "display:" + (internalflag ? "none" : "inline");
                        divClass = "class=\"sdms_btn_upgrade\"";
                    } else if (buttonFunction.equals("IC")) {
                        displayStyle = "display:" + (internalflag ? "none" : "inline");
                        divClass = "class=\"sdms_btn_install\"";
                    } else if (buttonFunction.equals("RI")) {
                        displayStyle = "display:" + (!instrumentPaused ? "none" : "inline");
                        divClass = "class=\"sdms_btn_resume\"";
                    } else if (buttonFunction.equals("PI")) {
                        displayStyle = "display:" + (instrumentPaused ? "none" : "inline");
                        divClass = "class=\"sdms_btn_pause\"";
                    }
                    buttonStateBuilder.append("<div " + divClass + " style=\"" + displayStyle + "\">");
                    buttonStateBuilder.append(button.getHtml());
                    buttonStateBuilder.append("&nbsp;&nbsp;");
                    buttonStateBuilder.append("</div>");
                }
            }
            buttonStateBuilder.append("</div>");
        }
        return buttonStateBuilder.toString();
    }

    public void getCollectorHTML(StringBuilder html, String sdmscollectorid, String instrumentid, boolean defaultView, boolean showIncidents) {
        html.append("<div class=\"sdms_default\" style=\"display:").append(defaultView ? "block" : "none").append("\">");
        boolean hasSDMSAdminRole = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).hasRole("SDMSAdmin");
        TabGroup defaultTabGroup = new TabGroup();
        defaultTabGroup.setPageContext(this.pageContext);
        defaultTabGroup.setId("defaulttabs");
        defaultTabGroup.setAppearance("modern");
        TranslationProcessor tp = this.getTranslationProcessor();
        Tab defaultStateTab = new Tab();
        defaultStateTab.setPageContext(this.pageContext);
        defaultStateTab.setId("default_state");
        defaultStateTab.setText(tp.translate("State"));
        StringBuilder defaultStateBuilder = new StringBuilder();
        defaultStateBuilder.append("<div class=\"sdms_buttons\">");
        PropertyListCollection buttons = this.getElementProperties().getCollection("buttons");
        Button button = new Button(this.pageContext);
        if (buttons != null && buttons.size() > 0 && sdmscollectorid.length() > 0 && !hasSDMSAdminRole) {
            HashSet<String> buttonFunctions = new HashSet<String>();
            buttonFunctions.add("AC");
            defaultStateBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, buttons, buttonFunctions));
        } else if (sdmscollectorid.length() > 0 && hasSDMSAdminRole) {
            button.setAction("sdmsMonitorGizmo.addCollector()");
            button.setText(tp.translate("Add Collector"));
            defaultStateBuilder.append(button.getHtml());
            defaultStateBuilder.append("&nbsp;&nbsp;");
        }
        defaultStateBuilder.append("</div>");
        defaultStateBuilder.append("<div class=\"sdms_content\">");
        defaultStateBuilder.append("<div class=\"sdms_defaultstate\"></div>");
        defaultStateBuilder.append("</div>");
        defaultStateTab.setContent(defaultStateBuilder.toString());
        defaultTabGroup.setTab(defaultStateTab);
        html.append(defaultTabGroup.getHtml());
        html.append("</div>");
        html.append("<div class=\"sdms_collector\" style=\"display:").append(!defaultView && sdmscollectorid.length() > 0 && instrumentid.length() == 0 ? "block" : "none").append("\">");
        TabGroup collectorTabGroup = new TabGroup();
        collectorTabGroup.setPageContext(this.pageContext);
        collectorTabGroup.setId("collectortabs");
        collectorTabGroup.setAppearance("modern");
        Tab collectorStateTab = new Tab();
        collectorStateTab.setPageContext(this.pageContext);
        collectorStateTab.setId("collector_state");
        collectorStateTab.setText(tp.translate("State"));
        StringBuilder collectorStateBuilder = new StringBuilder();
        if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
            HashSet<String> buttonFunctions = new HashSet<String>();
            buttonFunctions.add("CC");
            buttonFunctions.add("PC");
            buttonFunctions.add("RC");
            buttonFunctions.add("DC");
            buttonFunctions.add("EC");
            buttonFunctions.add("RBC");
            buttonFunctions.add("VL");
            buttonFunctions.add("UC");
            buttonFunctions.add("IC");
            collectorStateBuilder.append(this.renderRoleBasedButtons(sdmscollectorid, LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, buttons, buttonFunctions));
        } else if (hasSDMSAdminRole) {
            collectorStateBuilder.append(this.buildCollectorToolbar(sdmscollectorid));
        }
        collectorStateBuilder.append("<div class=\"sdms_content\">");
        collectorStateBuilder.append("<div " + (!hasSDMSAdminRole ? "style=\"top:0\"" : LABVANTAGE_CVS_ID) + " class=\"sdms_collectorstate sdms_subcontent\">Loading...</div>");
        collectorStateBuilder.append("</div>");
        collectorStateTab.setContent(collectorStateBuilder.toString());
        collectorTabGroup.setTab(collectorStateTab);
        Tab foldersTab = new Tab();
        foldersTab.setPageContext(this.pageContext);
        foldersTab.setId("collector_folders");
        foldersTab.setText(tp.translate("Data Capture Folders"));
        StringBuilder foldersBuilder = new StringBuilder();
        foldersBuilder.append("<div class=\"sdms_content\">");
        foldersBuilder.append("<div style=\"top:0\" class=\"sdms_collectorfolder sdms_subcontent\"></div>");
        foldersBuilder.append("</div>");
        foldersTab.setContent(foldersBuilder.toString());
        collectorTabGroup.setTab(foldersTab);
        Tab startupTab = new Tab();
        startupTab.setPageContext(this.pageContext);
        startupTab.setId("collector_startup");
        startupTab.setText(tp.translate("Last Startup Log"));
        StringBuilder startupBuilder = new StringBuilder();
        startupBuilder.append("<div class=\"sdms_content\">");
        startupBuilder.append("<div style=\"top:0\" class=\"sdms_collectorstartup sdms_subcontent\"></div>");
        startupBuilder.append("</div>");
        startupTab.setContent(startupBuilder.toString());
        collectorTabGroup.setTab(startupTab);
        if (showIncidents) {
            Tab collectorIncidentsTab = new Tab();
            collectorIncidentsTab.setPageContext(this.pageContext);
            collectorIncidentsTab.setId("collector_incidents");
            collectorIncidentsTab.setText(tp.translate("Incidents"));
            StringBuilder collectorIncidentsBuilder = new StringBuilder();
            collectorIncidentsBuilder.append("<div class=\"sdms_buttons\" onresize=\"sapphire.logger.debug('pop')\">");
            if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
                HashSet<String> buttonFunctions = new HashSet<String>();
                buttonFunctions.add("CA");
                collectorIncidentsBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, buttons, buttonFunctions));
            } else if (hasSDMSAdminRole) {
                button = new Button(this.pageContext);
                button.setAction("sdmsMonitorGizmo.clearAlerts()");
                button.setText(tp.translate("Clear Alerts"));
                collectorIncidentsBuilder.append(button.getHtml());
                collectorIncidentsBuilder.append("&nbsp;&nbsp;");
            }
            collectorIncidentsBuilder.append("</div>");
            collectorIncidentsBuilder.append("<div class=\"sdms_content\">");
            collectorIncidentsBuilder.append("<div class=\"sdms_collectorincidents sdms_subcontent\"></div>");
            collectorIncidentsBuilder.append("</div>");
            collectorIncidentsTab.setContent(collectorIncidentsBuilder.toString());
            collectorTabGroup.setTab(collectorIncidentsTab);
        }
        html.append(collectorTabGroup.getHtml());
        html.append("</div>");
        html.append("<div class=\"sdms_instrument\" style=\"display:").append(!defaultView && instrumentid.length() > 0 && sdmscollectorid.length() > 0 ? "block" : "none").append("\">");
        this.buildInstrumentTabs(html, instrumentid, showIncidents, hasSDMSAdminRole, true);
        html.append("</div>");
    }

    public void getInstrumentHTML(StringBuilder html, String instrumentid, String datacaptureid, String sdmscollectorid, boolean defaultView, boolean showIncidents) {
        HashSet<String> buttonFunctions;
        html.append("<div class=\"sdms_default\" style=\"display:").append(defaultView && sdmscollectorid.length() == 0 ? "block" : "none").append("\">");
        TranslationProcessor tp = this.getTranslationProcessor();
        TabGroup defaultTabGroup = new TabGroup();
        defaultTabGroup.setPageContext(this.pageContext);
        defaultTabGroup.setId("defaulttabs");
        defaultTabGroup.setAppearance("modern");
        Tab defaultStateTab = new Tab();
        defaultStateTab.setPageContext(this.pageContext);
        defaultStateTab.setId("default_state");
        defaultStateTab.setText(tp.translate("State"));
        StringBuilder defaultStateBuilder = new StringBuilder();
        defaultStateBuilder.append("<div class=\"sdms_content\">");
        defaultStateBuilder.append("<div class=\"sdms_defaultstate sdms_subcontent\"></div>");
        defaultStateBuilder.append("</div>");
        defaultStateTab.setContent(defaultStateBuilder.toString());
        defaultTabGroup.setTab(defaultStateTab);
        html.append(defaultTabGroup.getHtml());
        html.append("</div>");
        html.append("<div class=\"sdms_collector\" style=\"display:").append(defaultView && sdmscollectorid.length() > 0 ? "block" : "none").append("\">");
        TabGroup collectorTabGroup = new TabGroup();
        collectorTabGroup.setPageContext(this.pageContext);
        collectorTabGroup.setId("collectortabs");
        collectorTabGroup.setAppearance("modern");
        Tab collectorStateTab = new Tab();
        collectorStateTab.setPageContext(this.pageContext);
        collectorStateTab.setId("collector_state");
        collectorStateTab.setText(tp.translate("State"));
        boolean hasSDMSAdminRole = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).hasRole("SDMSAdmin");
        StringBuilder collectorStateBuilder = new StringBuilder();
        PropertyListCollection buttons = this.getElementProperties().getCollection("buttons");
        if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
            HashSet<String> buttonFunctions2 = new HashSet<String>();
            buttonFunctions2.add("CC");
            buttonFunctions2.add("PC");
            buttonFunctions2.add("RC");
            buttonFunctions2.add("DC");
            buttonFunctions2.add("EC");
            buttonFunctions2.add("RBC");
            buttonFunctions2.add("VL");
            buttonFunctions2.add("UC");
            buttonFunctions2.add("IC");
            collectorStateBuilder.append(this.renderRoleBasedButtons(sdmscollectorid, LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, buttons, buttonFunctions2));
        } else if (hasSDMSAdminRole) {
            collectorStateBuilder.append(this.buildCollectorToolbar(sdmscollectorid));
        }
        collectorStateBuilder.append("<div class=\"sdms_content\">");
        collectorStateBuilder.append("<div class=\"sdms_collectorstate sdms_subcontent\">Loading...</div>");
        collectorStateBuilder.append("</div>");
        collectorStateTab.setContent(collectorStateBuilder.toString());
        collectorTabGroup.setTab(collectorStateTab);
        Tab foldersTab = new Tab();
        foldersTab.setPageContext(this.pageContext);
        foldersTab.setId("collector_folders");
        foldersTab.setText(tp.translate("Data Capture Folders"));
        StringBuilder foldersBuilder = new StringBuilder();
        foldersBuilder.append("<div class=\"sdms_content\">");
        foldersBuilder.append("<div class=\"sdms_collectorfolder sdms_subcontent\"></div>");
        foldersBuilder.append("</div>");
        foldersTab.setContent(foldersBuilder.toString());
        collectorTabGroup.setTab(foldersTab);
        Tab startupTab = new Tab();
        startupTab.setPageContext(this.pageContext);
        startupTab.setId("collector_startup");
        startupTab.setText(tp.translate("Last Startup Log"));
        StringBuilder startupBuilder = new StringBuilder();
        startupBuilder.append("<div class=\"sdms_content\">");
        startupBuilder.append("<div class=\"sdms_collectorstartup sdms_subcontent\"></div>");
        startupBuilder.append("</div>");
        startupTab.setContent(startupBuilder.toString());
        collectorTabGroup.setTab(startupTab);
        if (showIncidents) {
            Tab collectorIncidentsTab = new Tab();
            collectorIncidentsTab.setPageContext(this.pageContext);
            collectorIncidentsTab.setId("collector_incidents");
            collectorIncidentsTab.setText(tp.translate("Incidents"));
            StringBuilder collectorIncidentsBuilder = new StringBuilder();
            collectorIncidentsBuilder.append("<div class=\"sdms_buttons\">");
            if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
                HashSet<String> buttonFunctions3 = new HashSet<String>();
                buttonFunctions3.add("CA");
                collectorIncidentsBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, buttons, buttonFunctions3));
            } else if (hasSDMSAdminRole) {
                Button button = new Button(this.pageContext);
                button.setAction("sdmsMonitorGizmo.clearAlerts()");
                button.setText(tp.translate("Clear Alerts"));
                collectorIncidentsBuilder.append(button.getHtml());
                collectorIncidentsBuilder.append("&nbsp;&nbsp;");
            }
            collectorIncidentsBuilder.append("</div>");
            collectorIncidentsBuilder.append("<div class=\"sdms_content\">");
            collectorIncidentsBuilder.append("<div class=\"sdms_collectorincidents sdms_subcontent\"></div>");
            collectorIncidentsBuilder.append("</div>");
            collectorIncidentsTab.setContent(collectorIncidentsBuilder.toString());
            collectorTabGroup.setTab(collectorIncidentsTab);
        }
        html.append(collectorTabGroup.getHtml());
        html.append("</div>");
        html.append("<div class=\"sdms_instrument\" style=\"display:").append(instrumentid.length() > 0 && !defaultView ? "block" : "none").append("\">");
        this.buildInstrumentTabs(html, instrumentid, showIncidents, hasSDMSAdminRole, false);
        html.append("</div>");
        html.append("<div class=\"sdms_datacapture\" style=\"display:").append(datacaptureid.length() > 0 && !defaultView ? "block" : "none").append("\">");
        TabGroup datacaptureTabGroup = new TabGroup();
        datacaptureTabGroup.setPageContext(this.pageContext);
        datacaptureTabGroup.setId("datacapturetabs");
        datacaptureTabGroup.setAppearance("modern");
        Tab datacaptureStateTab = new Tab();
        datacaptureStateTab.setPageContext(this.pageContext);
        datacaptureStateTab.setId("datacapture_state");
        datacaptureStateTab.setText(tp.translate("State"));
        StringBuilder datacaptureStateBuilder = new StringBuilder();
        datacaptureStateBuilder.append("<div class=\"sdms_buttons\">");
        Button button = new Button(this.pageContext);
        if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
            buttonFunctions = new HashSet<String>();
            buttonFunctions.add("VDC");
            datacaptureStateBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, datacaptureid, buttons, buttonFunctions));
        } else if (hasSDMSAdminRole) {
            button.setAction("sdmsMonitorGizmo.viewDataCapture()");
            button.setText(tp.translate("View"));
            button.setTip(tp.translate("View Data Capture"));
            button.setImg("rc?command=image&image=FlatBlackEye1");
            datacaptureStateBuilder.append(button.getHtml());
        }
        if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
            buttonFunctions = new HashSet();
            buttonFunctions.add("MDC");
            datacaptureStateBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, datacaptureid, buttons, buttonFunctions));
        } else if (hasSDMSAdminRole) {
            datacaptureStateBuilder.append("&nbsp;&nbsp;");
            button = new Button(this.pageContext);
            button.setAction("sdmsMonitorGizmo.markDataCaptureReady()");
            button.setTip(tp.translate("Mark Ready for Processing if the status is Pending Processing"));
            button.setText(tp.translate("Start Processing"));
            button.setImg("rc?command=image&image=FlatBlackPlay1");
            datacaptureStateBuilder.append(button.getHtml());
            datacaptureStateBuilder.append("&nbsp;&nbsp;");
        }
        datacaptureStateBuilder.append("</div>");
        datacaptureStateBuilder.append("<div class=\"sdms_content\">");
        datacaptureStateBuilder.append("<div class=\"sdms_datacapturestate sdms_subcontent\">Loading...</div>");
        datacaptureStateBuilder.append("</div>");
        datacaptureStateTab.setContent(datacaptureStateBuilder.toString());
        datacaptureTabGroup.setTab(datacaptureStateTab);
        Tab attachmentsStateTab = new Tab();
        attachmentsStateTab.setPageContext(this.pageContext);
        attachmentsStateTab.setId("attachment_state");
        attachmentsStateTab.setText(tp.translate("Attachments"));
        StringBuilder attachmentsStateBuilder = new StringBuilder();
        attachmentsStateBuilder.append("<div class=\"sdms_content\">");
        attachmentsStateBuilder.append("<div class=\"sdms_datacaptureattachments sdms_subcontent\">Loading...</div>");
        attachmentsStateBuilder.append("</div>");
        attachmentsStateTab.setContent(attachmentsStateBuilder.toString());
        datacaptureTabGroup.setTab(attachmentsStateTab);
        Tab executionsTab = new Tab();
        executionsTab.setPageContext(this.pageContext);
        executionsTab.setId("datacapture_executions");
        executionsTab.setText(tp.translate("Executions"));
        StringBuilder executionsBuilder = new StringBuilder();
        executionsBuilder.append("<div class=\"sdms_content\">");
        executionsBuilder.append("<div class=\"sdms_datacaptureexecutions sdms_subcontent\"></div>");
        executionsBuilder.append("</div>");
        executionsTab.setContent(executionsBuilder.toString());
        datacaptureTabGroup.setTab(executionsTab);
        Tab linkedTab = new Tab();
        linkedTab.setPageContext(this.pageContext);
        linkedTab.setId("datacapture_linkedsdi");
        linkedTab.setText(tp.translate("Linked SDIs"));
        StringBuilder linkedBuilder = new StringBuilder();
        linkedBuilder.append("<div class=\"sdms_content\">");
        linkedBuilder.append("<div class=\"sdms_datacapturelinked sdms_subcontent\"></div>");
        linkedBuilder.append("</div>");
        linkedTab.setContent(linkedBuilder.toString());
        datacaptureTabGroup.setTab(linkedTab);
        Tab metaTab = new Tab();
        metaTab.setPageContext(this.pageContext);
        metaTab.setId("datacapture_metadata");
        metaTab.setText(tp.translate("Meta Data"));
        StringBuilder metaBuilder = new StringBuilder();
        metaBuilder.append("<div class=\"sdms_content\">");
        metaBuilder.append("<div class=\"sdms_datacapturemetadata sdms_subcontent\"></div>");
        metaBuilder.append("</div>");
        metaTab.setContent(metaBuilder.toString());
        datacaptureTabGroup.setTab(metaTab);
        if (showIncidents) {
            Tab datacaptureIncidentsTab = new Tab();
            datacaptureIncidentsTab.setPageContext(this.pageContext);
            datacaptureIncidentsTab.setId("datacapture_incidents");
            datacaptureIncidentsTab.setText(tp.translate("Incidents"));
            StringBuilder collectorIncidentsBuilder = new StringBuilder();
            collectorIncidentsBuilder.append("<div class=\"sdms_buttons\">");
            if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
                HashSet<String> buttonFunctions4 = new HashSet<String>();
                buttonFunctions4.add("CA");
                datacaptureStateBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, datacaptureid, buttons, buttonFunctions4));
            } else if (hasSDMSAdminRole) {
                button = new Button(this.pageContext);
                button.setAction("sdmsMonitorGizmo.clearAlerts()");
                button.setText(tp.translate("Clear Alerts"));
                collectorIncidentsBuilder.append(button.getHtml());
                collectorIncidentsBuilder.append("&nbsp;&nbsp;");
            }
            collectorIncidentsBuilder.append("</div>");
            collectorIncidentsBuilder.append("<div class=\"sdms_content\">");
            collectorIncidentsBuilder.append("<div class=\"sdms_datacapturerincidents sdms_subcontent\"></div>");
            collectorIncidentsBuilder.append("</div>");
            datacaptureIncidentsTab.setContent(collectorIncidentsBuilder.toString());
            datacaptureTabGroup.setTab(datacaptureIncidentsTab);
        }
        html.append(datacaptureTabGroup.getHtml());
        html.append("</div>");
    }

    private void buildInstrumentTabs(StringBuilder html, String instrumentid, boolean showIncidents, boolean hasSDMSAdminRole, boolean isPopup) {
        Button button;
        TabGroup instrumentTabGroup = new TabGroup();
        instrumentTabGroup.setPageContext(this.pageContext);
        instrumentTabGroup.setId("instrumenttabs");
        instrumentTabGroup.setAppearance("modern");
        TranslationProcessor tp = this.getTranslationProcessor();
        Tab instrumentStateTab = new Tab();
        instrumentStateTab.setPageContext(this.pageContext);
        instrumentStateTab.setId("instrument_state");
        instrumentStateTab.setText(tp.translate("State"));
        StringBuilder instrumentStateBuilder = new StringBuilder();
        PropertyListCollection buttons = this.getElementProperties().getCollection("buttons");
        instrumentStateBuilder.append("<div class=\"sdms_buttons\">");
        if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
            HashSet<String> buttonFunctions = new HashSet<String>();
            buttonFunctions.add("RI");
            buttonFunctions.add("PI");
            buttonFunctions.add("RBI");
            buttonFunctions.add("VIL");
            instrumentStateBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, instrumentid, LABVANTAGE_CVS_ID, buttons, buttonFunctions));
        } else if (hasSDMSAdminRole) {
            instrumentStateBuilder.append(this.buildInstrumentToolbar(instrumentid));
        }
        instrumentStateBuilder.append("</div>");
        instrumentStateBuilder.append("<div class=\"sdms_content\">");
        instrumentStateBuilder.append("<div class=\"sdms_instrumentstate sdms_subcontent\">Loading...</div>");
        instrumentStateBuilder.append("</div>");
        instrumentStateTab.setContent(instrumentStateBuilder.toString());
        instrumentTabGroup.setTab(instrumentStateTab);
        Tab instrumentCaptureTab = new Tab();
        instrumentCaptureTab.setPageContext(this.pageContext);
        instrumentCaptureTab.setId("instrument_captures");
        instrumentCaptureTab.setText(tp.translate("Data Captures"));
        StringBuilder instrumentCaptureBuilder = new StringBuilder();
        instrumentCaptureBuilder.append("<div class=\"sdms_content\">");
        instrumentCaptureBuilder.append("<div " + (isPopup ? "style=\"top:0\"" : LABVANTAGE_CVS_ID) + " class=\"sdms_instrumentcapture sdms_subcontent\"></div>");
        instrumentCaptureBuilder.append("</div>");
        instrumentCaptureTab.setContent(instrumentCaptureBuilder.toString());
        instrumentTabGroup.setTab(instrumentCaptureTab);
        Tab instrumentEmulatorTab = new Tab();
        instrumentEmulatorTab.setPageContext(this.pageContext);
        instrumentEmulatorTab.setId("emulation_state");
        instrumentEmulatorTab.setText(tp.translate("Emulation"));
        StringBuilder instrumentEmulatorBuilder = new StringBuilder();
        instrumentEmulatorBuilder.append("<div class=\"sdms_buttons\">");
        if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
            HashSet<String> buttonFunctions = new HashSet<String>();
            buttonFunctions.add("SSE");
            buttonFunctions.add("TE");
            instrumentEmulatorBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, buttons, buttonFunctions));
        } else if (hasSDMSAdminRole) {
            button = new Button(this.pageContext);
            button.setAction("sdmsMonitorGizmo.stopstartEmulator()");
            button.setText(tp.translate("Start/Stop Emulator"));
            button.setTip(tp.translate("Start/Stop Emulator"));
            button.setImg("rc?command=image&image=FlatBlackControlResume");
            instrumentEmulatorBuilder.append(button.getHtml());
            instrumentEmulatorBuilder.append("&nbsp;&nbsp;");
            button = new Button(this.pageContext);
            button.setAction("sdmsMonitorGizmo.triggerEmulator()");
            button.setText(tp.translate("Trigger Emulator"));
            button.setImg("rc?command=image&image=FlatBlackControlPlay");
            button.setTip(tp.translate("Trigger Emulator"));
            instrumentEmulatorBuilder.append(button.getHtml());
            instrumentEmulatorBuilder.append("&nbsp;&nbsp;");
        }
        instrumentEmulatorBuilder.append("</div>");
        instrumentEmulatorBuilder.append("<div class=\"sdms_content\">");
        instrumentEmulatorBuilder.append("<div class=\"sdms_emulatorstate sdms_subcontent\">Loading...</div>");
        instrumentEmulatorBuilder.append("</div>");
        instrumentEmulatorTab.setContent(instrumentEmulatorBuilder.toString());
        instrumentTabGroup.setTab(instrumentEmulatorTab);
        if (showIncidents) {
            Tab instrumentIncidentsTab = new Tab();
            instrumentIncidentsTab.setPageContext(this.pageContext);
            instrumentIncidentsTab.setId("instrument_incidents");
            instrumentIncidentsTab.setText(tp.translate("Incidents"));
            StringBuilder instruemntIncidentsBuilder = new StringBuilder();
            instruemntIncidentsBuilder.append("<div class=\"sdms_buttons\">");
            if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
                HashSet<String> buttonFunctions = new HashSet<String>();
                buttonFunctions.add("CA");
                instruemntIncidentsBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, buttons, buttonFunctions));
            } else if (hasSDMSAdminRole) {
                button = new Button(this.pageContext);
                button.setAction("sdmsMonitorGizmo.clearAlerts()");
                button.setText(tp.translate("Clear Alerts"));
                instruemntIncidentsBuilder.append(button.getHtml());
                instruemntIncidentsBuilder.append("&nbsp;&nbsp;");
            }
            instruemntIncidentsBuilder.append("</div>");
            instruemntIncidentsBuilder.append("<div class=\"sdms_content\">");
            instruemntIncidentsBuilder.append("<div class=\"sdms_instrumentincidents sdms_subcontent\"></div>");
            instruemntIncidentsBuilder.append("</div>");
            instrumentIncidentsTab.setContent(instruemntIncidentsBuilder.toString());
            instrumentTabGroup.setTab(instrumentIncidentsTab);
        }
        html.append(instrumentTabGroup.getHtml());
    }

    private String buildCollectorToolbar(String sdmscollectorid) {
        StringBuilder collectorStateBuilder = new StringBuilder();
        DataSet collector = this.getQueryProcessor().getPreparedSqlDataSet("SELECT pausedflag, disabledflag, internalflag FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{sdmscollectorid});
        boolean paused = collector.getValue(0, "pausedflag").equals("Y");
        boolean disabled = collector.getValue(0, "disabledflag").equals("Y");
        boolean internalflag = collector.getValue(0, "internalflag").equals("Y");
        collectorStateBuilder.append("<div class=\"sdms_buttons\" >");
        TranslationProcessor tp = this.getTranslationProcessor();
        Button button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.configureCollector()");
        button.setText(tp.translate("Configure"));
        button.setTip(tp.translate("Configure Collector"));
        button.setImg("rc?command=image&image=FlatBlackCog1");
        collectorStateBuilder.append("<div style=\"display:inline;\">");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        collectorStateBuilder.append("</div>");
        collectorStateBuilder.append("<div class=\"sdms_btn_actions\" style=\"display:inline\">");
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.pauseCollector()");
        button.setText(tp.translate("Pause"));
        button.setTip(tp.translate("Pause Collector"));
        button.setImg("rc?command=image&image=FlatBlackControlPause");
        collectorStateBuilder.append("<div class=\"sdms_btn_pause\" style=\"display:" + (disabled || paused ? "none" : "inline") + "\">");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        collectorStateBuilder.append("</div>");
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.resumeCollector()");
        button.setText(tp.translate("Resume"));
        button.setTip(tp.translate("Resume Collector"));
        button.setImg("rc?command=image&image=FlatBlackControlResume");
        collectorStateBuilder.append("<div class=\"sdms_btn_resume\" style=\"display:" + (disabled || !paused ? "none" : "inline") + "\">");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        collectorStateBuilder.append("</div>");
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.disableCollector()");
        button.setText(tp.translate("Disable"));
        button.setTip(tp.translate("Disable Collector"));
        button.setImg("rc?command=image&image=FlatBlackCancel");
        collectorStateBuilder.append("<div class=\"sdms_btn_disable\" style=\"display:" + (disabled ? "none" : "inline") + "\">");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        collectorStateBuilder.append("</div>");
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.enableCollector()");
        button.setText(tp.translate("Enable"));
        button.setTip(tp.translate("Enable Collector"));
        button.setImg("rc?command=image&image=FlatBlackControlPlay");
        collectorStateBuilder.append("<div class=\"sdms_btn_enable\" style=\"display:" + (!disabled ? "none" : "inline") + "\">");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        collectorStateBuilder.append("</div>");
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.rebootCollector()");
        button.setText(tp.translate("Reboot"));
        button.setTip(tp.translate("Reboot Collector"));
        button.setImg("rc?command=image&image=FlatBlackRefresh");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.viewCollectorLog()");
        button.setText(tp.translate("View Log"));
        button.setTip(tp.translate("View full log since the collector was last restarted."));
        button.setImg("rc?command=image&image=FlatBlackBulletList4");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.upgradeCollector()");
        button.setText(tp.translate("Upgrade"));
        button.setTip(tp.translate("Upgrade Collector"));
        button.setImg("rc?command=image&image=FlatBlackUpload2");
        collectorStateBuilder.append("<div class=\"sdms_btn_upgrade\" style=\"display:" + (internalflag ? "none" : "inline") + "\">");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        collectorStateBuilder.append("</div>");
        collectorStateBuilder.append("</div>");
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.downloadInstaller()");
        button.setText(tp.translate("Install"));
        button.setTip(tp.translate("Download Installer For External Collector"));
        button.setImg("rc?command=image&image=FlatBlackDownload2");
        collectorStateBuilder.append("<div class=\"sdms_btn_install\" style=\"display:" + (internalflag ? "none" : "inline") + "\">");
        collectorStateBuilder.append(button.getHtml());
        collectorStateBuilder.append("&nbsp;&nbsp;");
        collectorStateBuilder.append("</div>");
        collectorStateBuilder.append("</div>");
        return collectorStateBuilder.toString();
    }

    private String buildInstrumentToolbar(String instrumentid) {
        Button button;
        StringBuilder instrumentStateBuilder = new StringBuilder();
        DataSet instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instr.sdmspausedflag,instr.instrumentstatus,instr.inserviceflag,sdmsc.pausedflag,sdmsc.disabledflag FROM instrument instr left join sdmscollector sdmsc on sdmsc.sdmscollectorid=instr.sdmscollectorid  WHERE instr.instrumentid=?", (Object[])new String[]{instrumentid});
        boolean instrumentPaused = instrument.getValue(0, "sdmspausedflag", "N").equalsIgnoreCase("Y");
        boolean instrumentUnavailable = instrument.getValue(0, "instrumentstatus", LABVANTAGE_CVS_ID).equalsIgnoreCase("Unavailable") || instrument.getValue(0, "inserviceflag", "Y").equalsIgnoreCase("N");
        boolean collectorPaused = instrument.getValue(0, "pausedflag", "N").equalsIgnoreCase("Y");
        boolean collectorDisabled = instrument.getValue(0, "disabledflag", "N").equalsIgnoreCase("Y");
        TranslationProcessor tp = this.getTranslationProcessor();
        if (!(instrumentUnavailable || collectorPaused || collectorDisabled)) {
            button = new Button(this.pageContext);
            button.setAction("sdmsMonitorGizmo.resumeInstrument()");
            button.setText(tp.translate("Resume"));
            button.setTip(tp.translate("Resume Instrument"));
            button.setImg("rc?command=image&image=FlatBlackControlResume");
            instrumentStateBuilder.append("<div class=\"sdms_btn_resume\" style=\"display:" + (!instrumentPaused ? "none" : "inline") + "\">");
            instrumentStateBuilder.append(button.getHtml());
            instrumentStateBuilder.append("&nbsp;&nbsp;");
            instrumentStateBuilder.append("</div>");
            button = new Button(this.pageContext);
            button.setAction("sdmsMonitorGizmo.pauseInstrument()");
            button.setText(tp.translate("Pause "));
            button.setTip(tp.translate("Pause Instrument"));
            button.setImg("rc?command=image&image=FlatBlackControlPause");
            instrumentStateBuilder.append("<div class=\"sdms_btn_pause\" style=\"display:" + (instrumentPaused ? "none" : "inline") + "\">");
            instrumentStateBuilder.append(button.getHtml());
            instrumentStateBuilder.append("&nbsp;&nbsp;");
            instrumentStateBuilder.append("</div>");
            button = new Button(this.pageContext);
            button.setAction("sdmsMonitorGizmo.rebootInstrument()");
            button.setText(tp.translate("Reboot"));
            button.setTip(tp.translate("Reboot Instrument"));
            button.setImg("rc?command=image&image=FlatBlackRefresh");
            instrumentStateBuilder.append(button.getHtml());
            instrumentStateBuilder.append("&nbsp;&nbsp;");
        }
        button = new Button(this.pageContext);
        button.setAction("sdmsMonitorGizmo.viewInstrumentLog()");
        button.setText(tp.translate("View Log"));
        button.setTip(tp.translate("View full log since the instrument was last restarted."));
        button.setImg("rc?command=image&image=FlatBlackBulletList4");
        instrumentStateBuilder.append(button.getHtml());
        instrumentStateBuilder.append("&nbsp;&nbsp;");
        return instrumentStateBuilder.toString();
    }

    public void getDataCaptureHTML(StringBuilder html, String datacaptureid, String sdiattachmentid, String instrumentid, boolean defaultView, boolean showIncidents) {
        Button button;
        HashSet<String> buttonFunctions;
        html.append("<div class=\"sdms_default\" style=\"display:").append(defaultView ? "block" : "none").append("\">");
        TabGroup defaultTabGroup = new TabGroup();
        defaultTabGroup.setPageContext(this.pageContext);
        defaultTabGroup.setId("defaulttabs");
        defaultTabGroup.setAppearance("modern");
        TranslationProcessor tp = this.getTranslationProcessor();
        Tab defaultStateTab = new Tab();
        defaultStateTab.setPageContext(this.pageContext);
        defaultStateTab.setId("default_state");
        defaultStateTab.setText(tp.translate("State"));
        StringBuilder defaultStateBuilder = new StringBuilder();
        defaultStateBuilder.append("<div class=\"sdms_content\">");
        defaultStateBuilder.append("<div class=\"sdms_defaultstate\"></div>");
        defaultStateBuilder.append("</div>");
        defaultStateTab.setContent(defaultStateBuilder.toString());
        defaultTabGroup.setTab(defaultStateTab);
        html.append(defaultTabGroup.getHtml());
        html.append("</div>");
        boolean hasSDMSAdminRole = this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()).hasRole("SDMSAdmin");
        html.append("<div class=\"sdms_instrument\" style=\"display:").append(instrumentid.length() > 0 && !defaultView ? "block" : "none").append("\">");
        this.buildInstrumentTabs(html, instrumentid, showIncidents, hasSDMSAdminRole, false);
        html.append("</div>");
        html.append("<div class=\"sdms_datacapture\" style=\"display:").append(datacaptureid.length() > 0 && !defaultView && sdiattachmentid.length() == 0 ? "block" : "none").append("\">");
        TabGroup datacaptureTabGroup = new TabGroup();
        datacaptureTabGroup.setPageContext(this.pageContext);
        datacaptureTabGroup.setId("datacapturetabs");
        datacaptureTabGroup.setAppearance("modern");
        Tab datacaptureStateTab = new Tab();
        datacaptureStateTab.setPageContext(this.pageContext);
        datacaptureStateTab.setId("datacapture_state");
        datacaptureStateTab.setText(tp.translate("State"));
        StringBuilder datacaptureStateBuilder = new StringBuilder();
        datacaptureStateBuilder.append("<div class=\"sdms_buttons\">");
        PropertyListCollection buttons = this.getElementProperties().getCollection("buttons");
        if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
            buttonFunctions = new HashSet<String>();
            buttonFunctions.add("VDC");
            datacaptureStateBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, datacaptureid, buttons, buttonFunctions));
        } else if (hasSDMSAdminRole) {
            button = new Button(this.pageContext);
            button.setAction("sdmsMonitorGizmo.viewDataCapture()");
            button.setText(tp.translate("View"));
            button.setTip(tp.translate("View Data Capture"));
            button.setImg("rc?command=image&image=FlatBlackEye1");
            datacaptureStateBuilder.append(button.getHtml());
        }
        if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
            buttonFunctions = new HashSet();
            buttonFunctions.add("MDC");
            datacaptureStateBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, datacaptureid, buttons, buttonFunctions));
        } else if (hasSDMSAdminRole) {
            datacaptureStateBuilder.append("&nbsp;&nbsp;");
            button = new Button(this.pageContext);
            button.setAction("sdmsMonitorGizmo.markDataCaptureReady()");
            button.setTip(tp.translate("Mark Ready for Processing if the status is Pending Processing"));
            button.setText(tp.translate("Start Processing"));
            button.setImg("rc?command=image&image=FlatBlackPlay1");
            datacaptureStateBuilder.append(button.getHtml());
            datacaptureStateBuilder.append("&nbsp;&nbsp;");
        }
        datacaptureStateBuilder.append("</div>");
        datacaptureStateBuilder.append("<div class=\"sdms_content\">");
        datacaptureStateBuilder.append("<div class=\"sdms_datacapturestate sdms_subcontent\">Loading...</div>");
        datacaptureStateBuilder.append("</div>");
        datacaptureStateTab.setContent(datacaptureStateBuilder.toString());
        datacaptureTabGroup.setTab(datacaptureStateTab);
        Tab attachmentsStateTab = new Tab();
        attachmentsStateTab.setPageContext(this.pageContext);
        attachmentsStateTab.setId("attachment_state");
        attachmentsStateTab.setText(tp.translate("Attachments"));
        StringBuilder attachmentsStateBuilder = new StringBuilder();
        attachmentsStateBuilder.append("<div class=\"sdms_content\">");
        attachmentsStateBuilder.append("<div class=\"sdms_datacaptureattachments sdms_subcontent\">Loading...</div>");
        attachmentsStateBuilder.append("</div>");
        attachmentsStateTab.setContent(attachmentsStateBuilder.toString());
        datacaptureTabGroup.setTab(attachmentsStateTab);
        Tab executionsTab = new Tab();
        executionsTab.setPageContext(this.pageContext);
        executionsTab.setId("datacapture_executions");
        executionsTab.setText(tp.translate("Executions"));
        StringBuilder executionsBuilder = new StringBuilder();
        executionsBuilder.append("<div class=\"sdms_content\">");
        executionsBuilder.append("<div class=\"sdms_datacaptureexecutions sdms_subcontent\"></div>");
        executionsBuilder.append("</div>");
        executionsTab.setContent(executionsBuilder.toString());
        datacaptureTabGroup.setTab(executionsTab);
        Tab linkedTab = new Tab();
        linkedTab.setPageContext(this.pageContext);
        linkedTab.setId("datacapture_linkedsdi");
        linkedTab.setText(tp.translate("Linked SDIs"));
        StringBuilder linkedBuilder = new StringBuilder();
        linkedBuilder.append("<div class=\"sdms_content\">");
        linkedBuilder.append("<div class=\"sdms_datacapturelinked sdms_subcontent\"></div>");
        linkedBuilder.append("</div>");
        linkedTab.setContent(linkedBuilder.toString());
        datacaptureTabGroup.setTab(linkedTab);
        Tab metaTab = new Tab();
        metaTab.setPageContext(this.pageContext);
        metaTab.setId("datacapture_metadata");
        metaTab.setText(tp.translate("Meta Data"));
        StringBuilder metaBuilder = new StringBuilder();
        metaBuilder.append("<div class=\"sdms_content\">");
        metaBuilder.append("<div class=\"sdms_datacapturemetadata sdms_subcontent\"></div>");
        metaBuilder.append("</div>");
        metaTab.setContent(metaBuilder.toString());
        datacaptureTabGroup.setTab(metaTab);
        if (showIncidents) {
            Tab datacaptureIncidentsTab = new Tab();
            datacaptureIncidentsTab.setPageContext(this.pageContext);
            datacaptureIncidentsTab.setId("datacapture_incidents");
            datacaptureIncidentsTab.setText(tp.translate("Incidents"));
            StringBuilder collectorIncidentsBuilder = new StringBuilder();
            collectorIncidentsBuilder.append("<div class=\"sdms_buttons\">");
            if (buttons != null && buttons.size() > 0 && !hasSDMSAdminRole) {
                HashSet<String> buttonFunctions2 = new HashSet<String>();
                buttonFunctions2.add("CA");
                collectorIncidentsBuilder.append(this.renderRoleBasedButtons(LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, LABVANTAGE_CVS_ID, buttons, buttonFunctions2));
            } else if (hasSDMSAdminRole) {
                button = new Button(this.pageContext);
                button.setAction("sdmsMonitorGizmo.clearAlerts()");
                button.setText(tp.translate("Clear Alerts"));
                collectorIncidentsBuilder.append(button.getHtml());
                collectorIncidentsBuilder.append("&nbsp;&nbsp;");
            }
            collectorIncidentsBuilder.append("</div>");
            collectorIncidentsBuilder.append("<div class=\"sdms_content\">");
            collectorIncidentsBuilder.append("<div class=\"sdms_datacapturerincidents sdms_subcontent\"></div>");
            collectorIncidentsBuilder.append("</div>");
            datacaptureIncidentsTab.setContent(collectorIncidentsBuilder.toString());
            datacaptureTabGroup.setTab(datacaptureIncidentsTab);
        }
        html.append(datacaptureTabGroup.getHtml());
        html.append("</div>");
        html.append("<div class=\"sdms_attachment\" style=\"display:").append(!defaultView && datacaptureid.length() > 0 && sdiattachmentid.length() > 0 ? "block" : "none").append("\">");
        TabGroup attachmentTabGroup = new TabGroup();
        attachmentTabGroup.setPageContext(this.pageContext);
        attachmentTabGroup.setId("attachmentttabs");
        attachmentTabGroup.setAppearance("modern");
        Tab attachmentStateTab = new Tab();
        attachmentStateTab.setPageContext(this.pageContext);
        attachmentStateTab.setId("attachment_state");
        attachmentStateTab.setText(tp.translate("State"));
        StringBuilder attachmentStateBuilder = new StringBuilder();
        attachmentStateBuilder.append("<div class=\"sdms_content\">");
        attachmentStateBuilder.append("<div class=\"sdms_attachmentstate sdms_subcontent\">Loading...</div>");
        attachmentStateBuilder.append("</div>");
        attachmentStateTab.setContent(attachmentStateBuilder.toString());
        attachmentTabGroup.setTab(attachmentStateTab);
        Tab metaAttTab = new Tab();
        metaAttTab.setPageContext(this.pageContext);
        metaAttTab.setId("attachment_metadata");
        metaAttTab.setText(tp.translate("Meta Data"));
        StringBuilder metaAttBuilder = new StringBuilder();
        metaAttBuilder.append("<div class=\"sdms_content\">");
        metaAttBuilder.append("<div class=\"sdms_attachmentmetadata sdms_subcontent\"></div>");
        metaAttBuilder.append("</div>");
        metaAttTab.setContent(metaAttBuilder.toString());
        attachmentTabGroup.setTab(metaAttTab);
        html.append(attachmentTabGroup.getHtml());
        html.append("</div>");
    }

    @Override
    public String getScript() {
        StringBuilder script = new StringBuilder();
        Mode mode = Mode.COLLECTOR;
        try {
            mode = Mode.valueOf(this.getElementProperties().getProperty("mode", mode.toString()).toUpperCase());
        }
        catch (Exception exception) {
            // empty catch block
        }
        script.append("if (typeof(sdmsMonitorGizmo) != 'undefined'){sdmsMonitorGizmo.mode = '").append(mode.toString()).append("';}");
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

    public static enum BUTTON_PROPERTIES {
        CONFIGURE_COLLECTOR("CC", "Configure", "Configure Collector", "sdmsMonitorGizmo.configureCollector()", "rc?command=image&image=FlatBlackCog1"),
        PAUSE_COLLECTOR("PC", "Pause", "Pause Collector", "sdmsMonitorGizmo.pauseCollector()", "rc?command=image&image=FlatBlackControlPause"),
        RESUME_COLLECTOR("RC", "Resume", "Resume Collector", "sdmsMonitorGizmo.resumeCollector()", "rc?command=image&image=FlatBlackControlResume"),
        DISABLE_COLLECTOR("DC", "Disable", "Disable Collector", "sdmsMonitorGizmo.disableCollector()", "rc?command=image&image=FlatBlackCancel"),
        ENABLE_COLLECTOR("EC", "Enable", "Enable Collector", "sdmsMonitorGizmo.enableCollector()", "rc?command=image&image=FlatBlackControlPlay"),
        REBOOT_COLLECTOR("RBC", "Reboot", "Reboot Collector", "sdmsMonitorGizmo.rebootCollector()", "rc?command=image&image=FlatBlackRefresh"),
        VIEW_LOG("VL", "View Log", "View full log since the collector was last restarted.", "sdmsMonitorGizmo.viewCollectorLog()", "rc?command=image&image=FlatBlackBulletList4"),
        UPGRADE_COLLECTOR("UC", "Upgrade", "Upgrade Collector", "sdmsMonitorGizmo.upgradeCollector()", "rc?command=image&image=FlatBlackUpload2"),
        INSTALL_COLLECTOR("IC", "Install", "Download Installer For External Collector", "sdmsMonitorGizmo.downloadInstaller()", "rc?command=image&image=FlatBlackDownload2"),
        ADD_COLLECTOR("AC", "Add Collector", "Add Collector", "sdmsMonitorGizmo.addCollector()", ""),
        CLEAR_ALERT("CA", "Clear Alerts", "Clear Alerts", "sdmsMonitorGizmo.clearAlerts()", ""),
        STARTSTOP_EMULATOR("SSE", "Start/Stop Emulator", "Start/Stop Emulator", "sdmsMonitorGizmo.stopstartEmulator()", "rc?command=image&image=FlatBlackControlResume"),
        TRIGGER_EMULATOR("TE", "Trigger Emulator", "Trigger Emulator", "sdmsMonitorGizmo.triggerEmulator()", "rc?command=image&image=FlatBlackControlPlay"),
        RESUME_INSTRUMENT("RI", "Resume", "Resume Instrument", "sdmsMonitorGizmo.resumeInstrument()", "rc?command=image&image=FlatBlackControlResume"),
        PAUSE_INSTRUMENT("PI", "Pause", "Pause Instrument", "sdmsMonitorGizmo.pauseInstrument()", "rc?command=image&image=FlatBlackControlPause"),
        REBOOT_INSTRUMENT("RBI", "Reboot", "Reboot Instrument", "sdmsMonitorGizmo.rebootInstrument()", "rc?command=image&image=FlatBlackRefresh"),
        VIEW_INSTRUMENT_LOG("VIL", "View Log", "View full log since the instrument was last restarted.", "sdmsMonitorGizmo.viewInstrumentLog()", "rc?command=image&image=FlatBlackBulletList4"),
        VIEW_DATA_CAPTURE("VDC", "View", "View Data Capture", "sdmsMonitorGizmo.viewDataCapture()", "rc?command=image&image=FlatBlackEye1"),
        MARK_DATA_CAPTURE("MDC", "Start Processing", "Mark Ready for Processing if the status is Pending Processing", "sdmsMonitorGizmo.markDataCaptureReady()", "rc?command=image&image=FlatBlackPlay1");

        private String buttonFunction;
        private String buttonText;
        private String buttonTips;
        private String buttonAction;
        private String buttonImage;

        private BUTTON_PROPERTIES(String buttonFunction, String buttonText, String buttonTips, String buttonAction, String buttonImage) {
            this.buttonFunction = buttonFunction;
            this.buttonText = buttonText;
            this.buttonTips = buttonTips;
            this.buttonAction = buttonAction;
            this.buttonImage = buttonImage;
        }

        public String getButtonFunction() {
            return this.buttonFunction;
        }

        public String getButtonText() {
            return this.buttonText;
        }

        public String getButtonTips() {
            return this.buttonTips;
        }

        public String getButtonAction() {
            return this.buttonAction;
        }

        public String getButtonImage() {
            return this.buttonImage;
        }
    }

    public static enum Mode {
        COLLECTOR,
        INSTRUMENT,
        DATACAPTURE;

    }
}

