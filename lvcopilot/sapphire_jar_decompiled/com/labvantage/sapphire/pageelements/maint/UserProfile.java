/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.pageelements.maint;

import com.labvantage.sapphire.admin.system.SysToolsPropertyHandler;
import java.util.ArrayList;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.pageelements.BaseElement;
import sapphire.servlet.RequestContext;
import sapphire.util.Browser;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class UserProfile
extends BaseElement {
    static final String LABVANTAGE_CVS_ID = "$Revision: 97164 $";

    private String getLogonRows(Browser.GUIMode guiMode, String tabMode, String startup, String userid, boolean viewMode, com.labvantage.sapphire.admin.system.ConfigurationProcessor configuration, TranslationProcessor tp) throws SapphireException {
        StringBuffer html = new StringBuffer();
        String id = (guiMode == null ? "" : guiMode.getId()) + "logonpageurl";
        String sysvalue = guiMode != null ? guiMode.getStartupUrl() : configuration.getProfileProperty("(system)", id);
        String uservalue = configuration.getProfileProperty(userid, id, sysvalue);
        if (startup == null || startup.length() == 0 || startup.equalsIgnoreCase("User")) {
            html.append("<tr id=\"").append(id).append("_row\" class=\"logoninforow\">");
        } else if (startup.equalsIgnoreCase("Last")) {
            html.append("<tr id=\"").append(id).append("_row\" style=\"display: none\" class=\"logoninforow\">");
        }
        if (tabMode.equals("User")) {
            html.append("<td class=\"maintform_fieldtitle\">" + (guiMode != null ? guiMode.getTitle() + " " : "") + tp.translate("Logon URL") + ":</td>");
            html.append("<td nowrap class=\"maintform_field\"><input onchange=\"setChangesMade()\"").append(" readonly").append(" id =\"").append(id).append("\" type = \"text\" name = \"").append(id).append("\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append(">");
            if (!startup.equalsIgnoreCase("Fixed")) {
                html.append("<button type=\"button\" onclick=\"savePageAsStartup('").append(guiMode != null ? guiMode.getId() : "").append("', true, false, false); \">" + tp.translate("Choose Current") + "</button>");
            }
            html.append("</td>");
            html.append("</tr>");
        } else {
            html.append("<td class=\"maintform_fieldtitle\">" + (guiMode != null ? guiMode.getTitle() + " " : "") + tp.translate("Logon URL") + ":</td>");
            html.append("<td nowrap class=\"maintform_field\"><input onchange=\"setChangesMade()\" id=\"").append(id).append("\" type = \"text\" name = \"").append(id).append("\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append(">");
            html.append("<button type=\"button\" onclick=\"browsePage('").append(id).append("'); \">...</button>");
            html.append("</td>");
            html.append("</tr>");
        }
        PropertyList guiPolicy = null;
        try {
            guiPolicy = new ConfigurationProcessor(this.pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
        }
        catch (Exception e) {
            guiPolicy = null;
        }
        id = (guiMode == null ? "" : guiMode.getId()) + "logonmenu";
        sysvalue = guiMode != null ? guiMode.getStartupMenuGizmo() : (guiPolicy != null ? guiPolicy.getProperty("defaultmenugizmo", "") : "");
        uservalue = configuration.getProfileProperty(userid, id, sysvalue);
        if (startup == null || startup.length() == 0 || startup.equalsIgnoreCase("User")) {
            html.append("<tr id=\"").append(id).append("_row\" class=\"logoninforow\">");
        } else if (startup.equalsIgnoreCase("Last")) {
            html.append("<tr id=\"").append(id).append("_row\" style=\"display: none\" class=\"logoninforow\">");
        }
        if (tabMode.equals("User")) {
            html.append("<td class=\"maintform_fieldtitle\">" + (guiMode != null ? guiMode.getTitle() + " " : "") + tp.translate("Start Menu") + ":</td>");
            html.append("<td nowrap class=\"maintform_field\"><input onchange=\"setChangesMade()\"").append(" readonly").append(" id =\"").append(id).append("\" type = \"text\" name = \"").append(id).append("\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append(">");
            if (!startup.equalsIgnoreCase("Fixed")) {
                html.append("<button type=\"button\" onclick=\"browseMenu('").append(id).append("'); \">...</button>");
                html.append("<button type=\"button\" onclick=\"savePageAsStartup('").append(guiMode != null ? guiMode.getId() : "").append("',false, true, false); \">" + tp.translate("Choose Current") + "</button>");
            }
            html.append("</td>");
            html.append("</tr>");
        } else {
            html.append("<td class=\"maintform_fieldtitle\">" + (guiMode != null ? guiMode.getTitle() + " " : "") + tp.translate("Start Menu") + ":</td>");
            html.append("<td nowrap class=\"maintform_field\"><input onchange=\"setChangesMade()\" id=\"").append(id).append("\" type = \"text\" name = \"").append(id).append("\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append(">");
            html.append("<button type=\"button\" onclick=\"browseMenu('").append(id).append("'); \">...</button>");
            html.append("</td>");
            html.append("</tr>");
        }
        id = (guiMode == null ? "" : guiMode.getId()) + "logongroup";
        sysvalue = guiMode != null ? guiMode.getStartupGroupGizmo() : (guiPolicy != null ? guiPolicy.getProperty("defaultgroupgizmo", "") : "");
        uservalue = configuration.getProfileProperty(userid, id, sysvalue);
        if (startup == null || startup.length() == 0 || startup.equalsIgnoreCase("User")) {
            html.append("<tr id=\"").append(id).append("_row\" class=\"logoninforow\">");
        } else if (startup.equalsIgnoreCase("Last")) {
            html.append("<tr id=\"").append(id).append("_row\" style=\"display: none\" class=\"logoninforow\">");
        }
        if (tabMode.equals("User")) {
            html.append("<td class=\"maintform_fieldtitle\">" + (guiMode != null ? guiMode.getTitle() + " " : "") + tp.translate("Start Sidebar") + ":</td>");
            html.append("<td nowrap class=\"maintform_field\"><input onchange=\"setChangesMade()\"").append(" readonly").append(" id =\"").append(id).append("\" type = \"text\" name = \"").append(id).append("\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append(">");
            if (!startup.equalsIgnoreCase("Fixed")) {
                html.append("<button type=\"button\" onclick=\"browseGroup('").append(id).append("'); \">...</button>");
                html.append("<button type=\"button\" onclick=\"savePageAsStartup('").append(guiMode != null ? guiMode.getId() : "").append("',false, false, true); \">" + tp.translate("Choose Current") + "</button>");
            }
            html.append("</td>");
            html.append("</tr>");
        } else {
            html.append("<td class=\"maintform_fieldtitle\">" + (guiMode != null ? guiMode.getTitle() + " " : "") + tp.translate("Start Sidebar") + ":</td>");
            html.append("<td nowrap class=\"maintform_field\"><input onchange=\"setChangesMade()\" id=\"").append(id).append("\" type = \"text\" name = \"").append(id).append("\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append(">");
            html.append("<button type=\"button\" onclick=\"browseGroup('").append(id).append("'); \">...</button>");
            html.append("</td>");
            html.append("</tr>");
        }
        return html.toString();
    }

    @Override
    public String getHtml() {
        RequestContext rc;
        String mode;
        TranslationProcessor tp = this.getTranslationProcessor();
        StringBuffer html = new StringBuffer();
        com.labvantage.sapphire.admin.system.ConfigurationProcessor configuration = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.pageContext);
        PropertyList tabPropertyList = null;
        if (this.pageContext.getAttribute("tabPropertyList") != null) {
            tabPropertyList = (PropertyList)this.pageContext.getAttribute("tabPropertyList");
        }
        String tabMode = "User";
        if (tabPropertyList != null) {
            tabMode = tabPropertyList.getProperty("mode", "User");
        }
        boolean viewMode = (mode = (rc = (RequestContext)this.pageContext.getRequest().getAttribute("RequestContext")).getProperty("mode")) != null && mode.equalsIgnoreCase("view");
        DataSet primary = this.sdiInfo.getDataSet("primary");
        String userid = primary.getString(0, "sysuserid");
        html.append("<script>");
        html.append("var el = '';");
        html.append("function browsePage(id) {");
        html.append("el = id;");
        html.append("sapphire.lookup.link.open(id);");
        html.append("}");
        html.append("function browseMenu(id) {");
        html.append("el = id;");
        html.append("sapphire.lookup.gizmo.open('browseMenu_Callback','menugizmo');");
        html.append("}");
        html.append("function browseMenu_Callback(sItem) {");
        html.append("var o = document.getElementById(el);");
        html.append("o.value=sItem;");
        html.append("sapphire.events.fireEvent(o,'onchange');");
        html.append("}");
        html.append("function browseGroup(id) {");
        html.append("el = id;");
        html.append("sapphire.lookup.gizmo.open('browseGroup_Callback','groupgizmo');");
        html.append("}");
        html.append("function browseGroup_Callback(sItem) {");
        html.append("var o = document.getElementById(el);");
        html.append("o.value=sItem;");
        html.append("sapphire.events.fireEvent(o,'onchange');");
        html.append("}");
        html.append("function changeLogonType(type) {");
        html.append("$('.logoninforow').css('display',(type=='Last'?'none':'table-row'));");
        html.append("}");
        html.append("function savePageAsStartup(guimode, page, menu, group) {");
        html.append("sapphire.ajax.callClass( \"com.labvantage.sapphire.ajax.operations.SavePageAsStartup\", \"handleSavePageAsStartup\", {page:(page?'Y':'N'),menu:(menu?'Y':'N'),group:(group?'Y':'N'),guimode:guimode});");
        html.append("}");
        html.append("function handleSavePageAsStartup( message, startupurl, menu, group, data ) { ");
        html.append("sapphire.ui.dialog.alert( message );");
        html.append("if (startupurl.length > 0){");
        html.append("document.getElementById( data.guimode + \"logonpageurl\" ).value = startupurl;");
        html.append("}");
        html.append("if (group.length > 0){");
        html.append("document.getElementById( data.guimode + \"logongroup\" ).value = group;");
        html.append("}");
        html.append("if (menu.length > 0){");
        html.append("document.getElementById( data.guimode + \"logonmenu\" ).value = menu;");
        html.append("}");
        html.append("}");
        html.append("</script>");
        try {
            html.append("<table class=\"maintform_table\" cellspacing = \"0\" cellpadding = \"3\" width=\"100%\">");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\" colspan=\"2\">" + tp.translate("Logon Options") + "</td>");
            html.append("</tr>");
            String startup = configuration.getProfileProperty(userid, "startuptype");
            String sysvalue = "";
            String uservalue = "";
            if (!tabMode.equals("Admin")) {
                if (!startup.equals("Fixed")) {
                    html.append("<tr>");
                    html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("Startup Type") + ":</td>");
                    html.append("<td><select onchange=\"setChangesMade();changeLogonType(this.value);\"  name=\"startuptype\" style=\"width:400px;").append("\">");
                    if (startup == null || startup.length() == 0 || startup.equalsIgnoreCase("User")) {
                        html.append("<option value=\"User\" selected>").append(tp.translate("Nominate a startup page, menu and sidebar")).append("</option>");
                        html.append("<option value=\"Last\">").append(tp.translate("Continue where I left off")).append("</option>");
                    } else if (startup.equalsIgnoreCase("Last")) {
                        html.append("<option value=\"User\">").append(tp.translate("Nominate a startup page, menu and sidebar")).append("</option>");
                        html.append("<option value=\"Last\" selected>").append(tp.translate("Continue where I left off")).append("</option>");
                    }
                    html.append("</select></td></tr>");
                } else {
                    html.append("<input type=\"hidden\" name=\"startuptype\" value=\"Fixed\"/>");
                }
            } else {
                html.append("<tr>");
                html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("Startup Type") + ":</td>");
                html.append("<td><select onchange=\"setChangesMade();changeLogonType(this.value);\"  name=\"startuptype\" style=\"width:400px;").append("\">");
                if (startup == null || startup.length() == 0 || startup.equalsIgnoreCase("User")) {
                    html.append("<option value=\"Fixed\">" + tp.translate("Assign a startup page, menu & sidebar") + " </option>");
                    html.append("<option value=\"User\" selected>" + tp.translate("User nominated startup page, menu & sidebar") + "</option>");
                    html.append("<option value=\"Last\">" + tp.translate("Continue where user left off") + "</option>");
                } else if (startup.equalsIgnoreCase("Last")) {
                    html.append("<option value=\"Fixed\">" + tp.translate("Assign a startup page, menu & sidebar") + " </option>");
                    html.append("<option value=\"User\" >" + tp.translate("User nominated startup page, menu & sidebar") + "</option>");
                    html.append("<option value=\"Last\" selected>" + tp.translate("Continue where user left off") + "</option>");
                } else if (startup.equalsIgnoreCase("Fixed")) {
                    html.append("<option value=\"Fixed\" selected>" + tp.translate("Assign a startup page, menu & sidebar") + "</option>");
                    html.append("<option value=\"User\" >" + tp.translate("User nominated startup page, menu & sidebar") + "</option>");
                    html.append("<option value=\"Last\">" + tp.translate("Continue where user left off") + "</option>");
                }
                html.append("</select></td></tr>");
            }
            Browser browser = new Browser(this.pageContext);
            ArrayList<Browser.GUIMode> guiModes = browser.getGUIModes();
            for (int i = 0; i < guiModes.size(); ++i) {
                html.append(this.getLogonRows(guiModes.get(i), tabMode, startup, userid, viewMode, configuration, tp));
            }
            uservalue = configuration.getProfileProperty(userid, "defaultworkbook");
            sysvalue = configuration.getProfileProperty("(system)", "defaultworkbook");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("Default Workbook") + ":</td>");
            html.append("<td nowrap class=\"maintform_field\"><input onchange=\"if ( this.value&&this.value.indexOf( '|' )>=0)this.value=this.value.replace('|',';');setChangesMade()\" id=\"defaultworkbook\" type = \"text\" name = \"defaultworkbook\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append(">");
            html.append("<button type=\"button\" onclick=\"lookupfield( 'defaultworkbook', 'LV_Workbook', '', 'N', '', '', '', '', '', '', '', 'LV_WorkbookLookup', '', true ); \">...</button>");
            html.append("</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td colspan=\"2\">&nbsp;</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\" colspan=\"2\">" + tp.translate("BO Options") + "</td>");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bourl");
            sysvalue = configuration.getProfileProperty("(system)", "bourl");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO URL") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bourl\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bodocumentdomain");
            sysvalue = configuration.getProfileProperty("(system)", "bodocumentdomain");
            html.append("<tr style=\"display:none\">");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Document Domain") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bodocumentdomain\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bodomain");
            sysvalue = configuration.getProfileProperty("(system)", "bodomain");
            html.append("<tr style=\"display:none\">");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Domain") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bodomain\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "boexchangemode");
            sysvalue = configuration.getProfileProperty("(system)", "boexchangemode");
            html.append("<tr style=\"display:none\">");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Exchange Mode") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"boexchangemode\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bouniverse");
            sysvalue = configuration.getProfileProperty("(system)", "bouniverse");
            html.append("<tr style=\"display:none\">");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Universe") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bouniverse\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bocmsname");
            sysvalue = configuration.getProfileProperty("(system)", "bocmsname");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO CMS Name") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bocmsname\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bousername");
            sysvalue = configuration.getProfileProperty("(system)", "bousername");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Username") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bousername\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bopassword");
            sysvalue = configuration.getProfileProperty("(system)", "bopassword");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Password") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade();checkBOPasswords();\" type = \"password\" name = \"bopassword\" value = \"").append(uservalue.length() > 0 ? "(storedpassword)" : "").append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "borootfoldername");
            sysvalue = configuration.getProfileProperty("(system)", "borootfoldername");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Root Folder Name") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"borootfoldername\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "boauthenticationtype");
            sysvalue = configuration.getProfileProperty("(system)", "boauthenticationtype");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Authentication Type") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"boauthenticationtype\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "boconnectiontimeout");
            sysvalue = configuration.getProfileProperty("(system)", "boconnectiontimeout");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Connection TimeOut(milliseconds)") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"boconnectiontimeout\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bocrystaldateformat");
            sysvalue = configuration.getProfileProperty("(system)", "bocrystaldateformat");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Crystal Date Format") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bocrystaldateformat\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bodeskidateformat");
            sysvalue = configuration.getProfileProperty("(system)", "bodeskidateformat");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Desktop Date Format") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bodeskidateformat\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "bowebidateformat");
            sysvalue = configuration.getProfileProperty("(system)", "bowebidateformat");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("BO Webi Date Format") + ":</td>");
            html.append("<td class=\"maintform_field\"><input onchange=\"setChangesMade()\" type = \"text\" name = \"bowebidateformat\" value = \"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\"").append(viewMode ? " readonly" : "").append("></td >");
            html.append("<td class=\"maintform_field\"><input onClick=\"testBOXIConnection()\" type = \"button\" name = \"TestConnection\" value = '" + tp.translate("Test Connection") + "' style=\"width:100px;\")</td >");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td colspan=\"2\">&nbsp;</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\" colspan=\"2\">" + tp.translate("Animation Options") + "</td>");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "menuanimation");
            sysvalue = configuration.getProfileProperty("(system)", "menuanimation");
            if (uservalue == null || uservalue.length() == 0) {
                uservalue = "Y";
            }
            if (sysvalue == null || sysvalue.length() == 0) {
                sysvalue = "Y";
            }
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("Enable Menu Animations") + ":</td>");
            html.append("<td class=\"maintform_field\">");
            if (viewMode) {
                html.append("<input readonly value=\"").append(uservalue.equalsIgnoreCase("Y") ? "Yes" : "No").append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\">");
            } else {
                html.append("<select onchange=\"setChangesMade()\" name=\"menuanimation\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\">");
                if (uservalue.equalsIgnoreCase("Y")) {
                    html.append("<option value=\"Y\" selected>Yes</option>");
                    html.append("<option value=\"N\">No</option>");
                } else {
                    html.append("<option value=\"Y\">Yes</option>");
                    html.append("<option value=\"N\" selected>No</option>");
                }
            }
            html.append("</td>");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "fadeanimation");
            sysvalue = configuration.getProfileProperty("(system)", "fadeanimation");
            if (uservalue == null || uservalue.length() == 0) {
                uservalue = "Y";
            }
            if (sysvalue == null || sysvalue.length() == 0) {
                sysvalue = "Y";
            }
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("Enable Fade-in") + " &amp; " + tp.translate("Fade-out Animations") + ":</td>");
            html.append("<td class=\"maintform_field\">");
            if (viewMode) {
                html.append("<input readonly value=\"").append(uservalue.equalsIgnoreCase("Y") ? "Yes" : "No").append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\">");
            } else {
                html.append("<select onchange=\"setChangesMade()\" name=\"fadeanimation\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\">");
                if (uservalue.equalsIgnoreCase("Y")) {
                    html.append("<option value=\"Y\" selected>Yes</option>");
                    html.append("<option value=\"N\">No</option>");
                } else {
                    html.append("<option value=\"Y\">Yes</option>");
                    html.append("<option value=\"N\" selected>No</option>");
                }
            }
            html.append("</td>");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "resizeanimation");
            sysvalue = configuration.getProfileProperty("(system)", "resizeanimation");
            if (uservalue == null || uservalue.length() == 0) {
                uservalue = "Y";
            }
            if (sysvalue == null || sysvalue.length() == 0) {
                sysvalue = "Y";
            }
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("Enable Resize-in") + " &amp; " + tp.translate("Resize-out Animations") + ":</td>");
            html.append("<td class=\"maintform_field\">");
            if (viewMode) {
                html.append("<input readonly value=\"").append(uservalue.equalsIgnoreCase("Y") ? "Yes" : "No").append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\">");
            } else {
                html.append("<select onchange=\"setChangesMade()\" name=\"resizeanimation\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\">");
                if (uservalue.equalsIgnoreCase("Y")) {
                    html.append("<option value=\"Y\" selected>Yes</option>");
                    html.append("<option value=\"N\">No</option>");
                } else {
                    html.append("<option value=\"Y\">Yes</option>");
                    html.append("<option value=\"N\" selected>No</option>");
                }
            }
            html.append("</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td colspan=\"2\">&nbsp;</td>");
            html.append("</tr>");
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\" colspan=\"2\">" + tp.translate("Notification Options") + "</td>");
            html.append("</tr>");
            uservalue = configuration.getProfileProperty(userid, "notificationformat");
            sysvalue = configuration.getProfileProperty("(system)", "notificationformat");
            if (uservalue == null || uservalue.length() == 0) {
                uservalue = "Bulletin";
            }
            if (sysvalue == null || sysvalue.length() == 0) {
                sysvalue = "Bulletin";
            }
            html.append("<tr>");
            html.append("<td class=\"maintform_fieldtitle\">" + tp.translate("Notifications Format") + ":</td>");
            html.append("<td class=\"maintform_field\">");
            if (viewMode) {
                html.append("<input readonly value=\"").append(uservalue).append("\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\">");
            } else {
                html.append("<select onchange=\"setChangesMade()\" name=\"notificationformat\" style=\"width:400px;").append(uservalue.equals(sysvalue) ? "color:Blue" : "").append("\">");
                if (uservalue.equalsIgnoreCase("Bulletin")) {
                    html.append("<option value=\"Bulletin\" selected>").append(tp.translate("Bulletin")).append("</option>");
                    html.append("<option value=\"Email\">").append(tp.translate("Email")).append("</option>");
                } else {
                    html.append("<option value=\"Bulletin\">").append(tp.translate("Bulletin")).append("</option>");
                    html.append("<option value=\"Email\" selected>").append(tp.translate("Email")).append("</option>");
                }
            }
            html.append("</td>");
            html.append("</tr>");
            if (!viewMode) {
                html.append("<tr>");
                html.append("<td  class=\"maintform_field\" colspan=\"2\"><input onchange=\"setChangesMade()\" type = \"checkbox\" name = \"__userprofilereset\">&nbsp;" + tp.translate("Reset to system defaults") + "</td >");
                html.append("</tr>");
            }
            html.append("</table>");
            html.append("<input type=\"hidden\" name=\"profileuserid\" value=\"").append(userid).append("\">");
            html.append("<input type=\"hidden\" name=\"systoolsmode\" value=\"userprofilesave\">");
            html.append("<input type=\"hidden\" name=\"__propertyhandler_").append(this.elementid).append("\" value=\"").append(SysToolsPropertyHandler.class.getName()).append("\"/>");
        }
        catch (SapphireException e) {
            this.logger.warn(e.getMessage());
        }
        return html.toString();
    }
}

