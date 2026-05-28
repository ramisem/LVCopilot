/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.GizmoTargetList;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.GroupPickerGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.Arrays;
import java.util.List;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.util.DataSet;
import sapphire.util.Logger;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MenuPickerGizmo
extends MenuGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String IMAGE = "Compass";

    @Override
    public boolean init() {
        super.init();
        return true;
    }

    @Override
    protected void setUpProperties() {
        this.imageTitle = this.getGizmoDefId().length() > 0 ? this.getGizmoDefId() : "Menu Picker";
        this.setTitle(this.imageTitle);
        if (this.element == null) {
            this.element = new PropertyList();
        }
        this.element.setProperty("image", this.element.getProperty("image", IMAGE));
        String mode = this.element.getProperty("mode", "menu");
        if (mode.equalsIgnoreCase("button")) {
            if (this.pageContext != null) {
                sapphire.pageelements.BaseGizmo gizmo;
                String rootmenu = this.element.getProperty("rootmenu", LABVANTAGE_CVS_ID);
                if (rootmenu.length() > 0 && (gizmo = BaseGizmo.getInstance(this.pageContext, rootmenu, true)) != null && gizmo instanceof MenuGizmo) {
                    PropertyListCollection links = gizmo.getElementProperties().getCollection("menu");
                    this.element.setProperty("menu", links);
                }
                this.element.setProperty("menuheader", this.getTranslationProcessor().translate("Select Menu"));
                this.element.setProperty("menuheaderclick", "modernLayout.setMenu");
                this.element.setProperty("customclass", "menugizmo_picker");
            }
        } else if (mode.equalsIgnoreCase("automatic")) {
            PropertyListCollection menu = new PropertyListCollection();
            DataSet menugizmos = GizmoTargetList.getGizmoTargets(LABVANTAGE_CVS_ID, "menugizmo", this.pageContext);
            boolean sep = false;
            if (menugizmos != null) {
                for (int i = 0; i < menugizmos.getRowCount(); ++i) {
                    String menugizmodefid = menugizmos.getValue(i, "gizmodefid");
                    MenuGizmo current = (MenuGizmo)BaseGizmo.getInstance(this.pageContext, menugizmodefid, null, true, true);
                    if (current == null) {
                        Logger.logDebug("Null menu found for " + menugizmodefid);
                    }
                    if (current == null || !current.getElementProperties().getProperty("showinpicker", "N").equalsIgnoreCase("Y")) continue;
                    PropertyList menuitem = new PropertyList();
                    menuitem.setProperty("link", "javascript:modernLayout.setMenu('" + menugizmodefid + "'," + this.element.getProperty("fullpage", "Y").equalsIgnoreCase("Y") + ")");
                    menuitem.setProperty("alias", menugizmodefid);
                    String title = current.getTitle();
                    if (title == null || title.length() == 0) {
                        title = menugizmos.getValue(i, "gizmodefdesc", menugizmodefid);
                    }
                    menuitem.setProperty("text", title);
                    menu.add(menuitem);
                    sep = true;
                }
                if (this.element.getProperty("showcurrent", "Y").equalsIgnoreCase("N")) {
                    this.element.setProperty("onshow", "menuGizmo.menuPicker.onShowRemove");
                } else {
                    this.element.setProperty("onshow", "menuGizmo.menuPicker.onShowSelection");
                }
            }
            if (this.element.getProperty("showdashboards", "Y").equalsIgnoreCase("Y")) {
                boolean sep2 = false;
                DataSet groupgizmos = GizmoTargetList.getGizmoTargets(LABVANTAGE_CVS_ID, "groupgizmo", this.pageContext);
                if (groupgizmos != null && GroupPickerGizmo.addDashboardGroups(menu, sep, this.pageContext)) {
                    sep = false;
                    sep2 = true;
                }
                PropertyList guiPolicy = null;
                try {
                    guiPolicy = new ConfigurationProcessor(this.pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
                    if (guiPolicy == null) {
                        guiPolicy = new PropertyList();
                    }
                }
                catch (Exception e) {
                    guiPolicy = new PropertyList();
                }
                if (guiPolicy.getProperty("lockgroups", "N").equalsIgnoreCase("N")) {
                    boolean hasRole;
                    SapphireConnection sc = new ConnectionProcessor(this.pageContext).getSapphireConnection();
                    List<String> roles = Arrays.asList(StringUtil.split(sc.getRoleList(), ";"));
                    String role = guiPolicy.getProperty("groupadminrole", LABVANTAGE_CVS_ID);
                    boolean bl = hasRole = role.length() == 0 || roles.contains(role);
                    if (hasRole) {
                        if (sep2 || sep) {
                            PropertyList seperator = new PropertyList();
                            seperator.setProperty("text", "-");
                            menu.add(seperator);
                        }
                        PropertyList menuitem = new PropertyList();
                        menuitem.setProperty("link", "javascript:modernLayout.gizmos.create('groupgizmo','Dashboard Custom','','Dashboard', sapphire.util.propertyList.create({groupoptions:{owner:sapphire.connection.sysUserId}}), 'modernLayout.gizmos.create_DashboardCallback')");
                        menuitem.setProperty("alias", "CreateDashboard");
                        menuitem.setProperty("text", "Create Dashboard");
                        menu.add(menuitem);
                    }
                }
            }
            this.element.setProperty("menu", menu);
        }
    }

    @Override
    public String getIcon() {
        return this.getImage(this.getTranslationProcessor().translate("Select") + " " + this.element.getProperty("rootmenu", this.imageTitle), this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getIconHtml() {
        String mode = this.element.getProperty("mode", "menu");
        if (mode.equalsIgnoreCase("button")) {
            StringBuffer html = new StringBuffer();
            BaseGizmo.GizmoStyle gizmoStyle = this.getGizmoStyle();
            if (gizmoStyle.showImage) {
                html.append("<span id=\"e_").append(this.elementid).append("\" onclick=\"").append("modernLayout.setMenu('").append(this.element.getProperty("rootmenu", LABVANTAGE_CVS_ID)).append("',").append(this.element.getProperty("fullpage", "Y").equalsIgnoreCase("Y")).append(")\"").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(this.getImage(this.getTranslationProcessor().translate("Select") + " " + this.element.getProperty("rootmenu", this.imageTitle), gizmoStyle.size).getHtml());
                html.append("</span>");
            }
            if (gizmoStyle.showTitle) {
                html.append("<span onclick=\"").append("modernLayout.setMenu('").append(this.element.getProperty("rootmenu", LABVANTAGE_CVS_ID)).append("',").append(this.element.getProperty("fullpage", "Y").equalsIgnoreCase("Y")).append(")\"").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_txt\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(this.getGizmoDefId().length() > 0 ? this.getGizmoDefId() : "Menu Picker");
                html.append("</span>");
            }
            return html.toString();
        }
        if (mode.equalsIgnoreCase("automatic")) {
            return super.getIconHtml();
        }
        return super.getIconHtml();
    }

    @Override
    public String getDefaultImageSrc() {
        return IMAGE;
    }
}

