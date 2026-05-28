/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.dashboard.gizmos;

import com.labvantage.sapphire.modules.dashboard.GizmoTargetList;
import com.labvantage.sapphire.modules.dashboard.gizmos.BaseGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.GroupGizmo;
import com.labvantage.sapphire.modules.dashboard.gizmos.MenuGizmo;
import com.labvantage.sapphire.services.SapphireConnection;
import java.util.Arrays;
import java.util.List;
import javax.servlet.jsp.PageContext;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class GroupPickerGizmo
extends MenuGizmo {
    private static final String LABVANTAGE_CVS_ID = "";
    public static final String IMAGE = "Compass";

    @Override
    public boolean init() {
        this.setRefreshOnResize(false);
        this.setTimeout(-1);
        return true;
    }

    @Override
    protected void setUpProperties() {
        this.imageTitle = this.getGizmoDefId().length() > 0 ? this.getGizmoDefId() : "Group Picker";
        this.setTitle(this.imageTitle);
        if (this.element == null) {
            this.element = new PropertyList();
        }
        this.element.setProperty("image", this.element.getProperty("image", IMAGE));
        String mode = this.element.getProperty("mode", "menu");
        if (mode.equalsIgnoreCase("button")) {
            DataSet groupgizmos;
            SDIRequest sdiReq = new SDIRequest();
            sdiReq.setSDCid("LV_GizmoDef");
            sdiReq.setQueryFrom("gizmodef");
            sdiReq.setQueryWhere("propertytreeid='groupgizmo'");
            sdiReq.setRequestItem("primary");
            SDIData sdi = new SDIProcessor(this.getConnectionId()).getSDIData(sdiReq);
            if (sdi != null && (groupgizmos = sdi.getDataset("primary")) != null) {
                PropertyListCollection links = new PropertyListCollection();
                for (int i = 0; i < groupgizmos.getRowCount(); ++i) {
                    PropertyList link = new PropertyList();
                    String id = this.elementid + "_" + i;
                    link.setProperty("id", id);
                    link.setId(id);
                    link.setProperty("type", MenuGizmo.MenuItemType.MENU_ITEM.toString());
                    link.setProperty("releaselock", "N");
                    String groupgizmoid = groupgizmos.getValue(i, "gizmodefid", LABVANTAGE_CVS_ID);
                    link.setProperty("text", groupgizmoid);
                    link.setProperty("link", "javascript:modernLayout.setGroup('" + groupgizmoid + "')");
                    links.add(link);
                }
                this.element.setProperty("menu", links);
            }
        } else if (mode.equalsIgnoreCase("automatic")) {
            PropertyListCollection menu = new PropertyListCollection();
            GroupPickerGizmo.addSidebarGroups(menu, this.pageContext);
            this.element.setProperty("menu", menu);
        }
    }

    public static void addSidebarGroups(PropertyListCollection menu, PageContext pageContext) {
        GroupPickerGizmo.addSidebarGroups(menu, false, pageContext);
    }

    public static boolean addSidebarGroups(PropertyListCollection menu, boolean addSeparator, PageContext pageContext) {
        return GroupPickerGizmo.addGroups("sidebar", menu, "modernLayout.setGroup", addSeparator, pageContext);
    }

    public static boolean canView(PropertyList groupGizmoProps, List<String> roles, String sysuserid) {
        return GroupPickerGizmo.canView(groupGizmoProps, roles, null, sysuserid);
    }

    public static boolean canView(PropertyList groupGizmoProps, List<String> roles, List<String> modules, String sysuserid) {
        PropertyList options = groupGizmoProps.getPropertyList("groupoptions");
        String gRole = options != null ? options.getProperty("role") : LABVANTAGE_CVS_ID;
        String gOwner = options != null ? options.getProperty("owner") : LABVANTAGE_CVS_ID;
        boolean gPublic = options != null ? options.getProperty("public", LABVANTAGE_CVS_ID).equalsIgnoreCase("Y") : false;
        String gModule = options != null ? options.getProperty("module") : LABVANTAGE_CVS_ID;
        boolean modulePermitted = false;
        if (gModule.length() == 0) {
            modulePermitted = true;
        } else if (modules != null && modules.contains(gModule)) {
            modulePermitted = true;
        }
        boolean canShow = false;
        if (modulePermitted) {
            if (gPublic) {
                canShow = true;
            } else if (gOwner.equalsIgnoreCase(sysuserid)) {
                canShow = true;
            } else if (roles.contains(gRole)) {
                canShow = true;
            }
        }
        return canShow;
    }

    private static boolean addGroups(String property, PropertyListCollection menu, String script, boolean addSep, PageContext pageContext) {
        DataSet groupgizmos = GizmoTargetList.getGizmoTargets(LABVANTAGE_CVS_ID, "groupgizmo", pageContext);
        boolean added = false;
        if (groupgizmos != null) {
            PropertyList guiPolicy = null;
            try {
                guiPolicy = new ConfigurationProcessor(pageContext).getPolicy("GUIPolicy", "Sapphire Custom");
                if (guiPolicy == null) {
                    guiPolicy = new PropertyList();
                }
            }
            catch (Exception e) {
                guiPolicy = new PropertyList();
            }
            SapphireConnection sc = new ConnectionProcessor(pageContext).getSapphireConnection();
            List<String> roles = Arrays.asList(StringUtil.split(sc.getRoleList(), ";"));
            List<String> modules = Arrays.asList(StringUtil.split(sc.getModuleList(), ";"));
            String role = guiPolicy.getProperty("groupadminrole", LABVANTAGE_CVS_ID);
            String user = sc.getSysuserId();
            for (int i = 0; i < groupgizmos.getRowCount(); ++i) {
                String groupgizmodefid = groupgizmos.getValue(i, "gizmodefid");
                GroupGizmo current = (GroupGizmo)BaseGizmo.getInstance(pageContext, groupgizmodefid, null, true, true);
                if (current == null || !current.getElementProperties().getProperty(property, "N").equalsIgnoreCase("Y") || !GroupPickerGizmo.canView(current.getElementProperties(), roles, modules, user)) continue;
                if (addSep) {
                    addSep = false;
                    PropertyList seperator = new PropertyList();
                    seperator.setProperty("text", "-");
                    menu.add(seperator);
                }
                PropertyList menuitem = new PropertyList();
                menuitem.setProperty("link", "javascript:" + script + "('" + groupgizmodefid + "')");
                String title = current.getTitle();
                if (title == null || title.length() == 0) {
                    title = groupgizmos.getValue(i, "gizmodefdesc", groupgizmodefid);
                }
                menuitem.setProperty("text", title);
                menu.add(menuitem);
                added = true;
            }
        }
        return added;
    }

    public static void addDashboardGroups(PropertyListCollection menu, PageContext pageContext) {
        GroupPickerGizmo.addDashboardGroups(menu, false, pageContext);
    }

    public static boolean addDashboardGroups(PropertyListCollection menu, boolean addSeparator, PageContext pageContext) {
        return GroupPickerGizmo.addGroups("dashboard", menu, "groupGizmo.grid.changeGroup", addSeparator, pageContext);
    }

    @Override
    public String getIcon() {
        return this.getImage(this.getTranslationProcessor().translate("Select") + " " + this.element.getProperty("groupgizmo", this.imageTitle), this.getGizmoStyle().size).getHtml();
    }

    @Override
    public String getIconHtml() {
        String mode = this.element.getProperty("mode", "menu");
        if (mode.equalsIgnoreCase("button")) {
            StringBuffer html = new StringBuffer();
            BaseGizmo.GizmoStyle gizmoStyle = this.getGizmoStyle();
            if (gizmoStyle.showImage) {
                html.append("<span id=\"e_").append(this.elementid).append("\" onclick=\"").append("modernLayout.setGroup('").append(this.element.getProperty("groupgizmo", LABVANTAGE_CVS_ID)).append("')\"").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_img\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(this.getImage(this.getTranslationProcessor().translate("Select") + " " + this.element.getProperty("groupgizmo", this.imageTitle), gizmoStyle.size).getHtml());
                html.append("</span>");
            }
            if (gizmoStyle.showTitle) {
                html.append("<span onclick=\"").append("modernLayout.setGroup('").append(this.element.getProperty("groupgizmo", LABVANTAGE_CVS_ID)).append("')\"").append(gizmoStyle.className.length() > 0 ? " class=\"" + gizmoStyle.className + "_txt\"" : LABVANTAGE_CVS_ID).append(">");
                html.append(this.getGizmoDefId().length() > 0 ? this.getGizmoDefId() : "Group Picker");
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

