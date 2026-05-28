/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.dashboard;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.services.RequestService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.servlet.RequestProcessor;
import com.labvantage.sapphire.util.cache.CacheUtil;
import com.labvantage.sapphire.xml.PropertyTree;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.servlet.RequestContext;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MenuGizmoPopulator
extends BaseAction {
    private static final String STARTURL = "rc?command=page&page=";
    private static final String SITEMAPJSP = "/WEB-OPAL/pagetypes/sitemap/tramline/sitemap.jsp";
    private HashMap<String, PropertyList> menus;

    private void writeOutSitemap(PropertyList layout, String menuid) {
        PropertyListCollection tabs;
        PropertyListCollection propertyListCollection = tabs = layout != null ? layout.getCollection("tabs") : null;
        if (tabs != null) {
            for (int t = 0; t < tabs.size(); ++t) {
                PropertyList tab = tabs.getPropertyList(t);
                String tabid = tab.getProperty("text", "Submenu " + t);
                String submenu = this.addSubMenu(menuid, tabid, tab.getProperty("show", "Y").equalsIgnoreCase("Y"), tabid, false, tab.getAttribute("rolelist"), tab.getAttribute("modulelist"));
                PropertyListCollection menus = tab.getCollection("menus");
                if (menus == null || menus.size() <= 0) continue;
                for (int m = 0; m < menus.size(); ++m) {
                    PropertyList menu = menus.getPropertyList(m);
                    this.addMenuItem(submenu, menu.getProperty("id", "menuitem" + m), menu.getProperty("show", "Y").equalsIgnoreCase("Y"), menu.getProperty("text", "Menu Item " + m), menu.getProperty("link"), menu.getProperty("releaselocks", "Y").equalsIgnoreCase("Y"), menu.getAttribute("rolelist"), menu.getAttribute("modulelist"));
                }
            }
        }
    }

    private DataSet getGizmoDef(String gizmoDefId) {
        DataSet out = null;
        SDIRequest sdiReq = new SDIRequest();
        sdiReq.setSDCid("LV_GizmoDef");
        sdiReq.setQueryFrom("gizmodef");
        sdiReq.setQueryWhere("gizmodefid='" + SafeSQL.encodeForSQL(gizmoDefId, this.database.isOracle()) + "'");
        sdiReq.setRequestItem("primary");
        sdiReq.setExtendedDataTypes(true);
        SDIData sdi = this.getSDIProcessor().getSDIData(sdiReq);
        if (sdi != null) {
            out = sdi.getDataset("primary");
        }
        return out;
    }

    private boolean containsMenu(String id) {
        boolean found = false;
        for (String key : this.menus.keySet()) {
            if (!key.equalsIgnoreCase(id)) continue;
            found = true;
        }
        return found;
    }

    private String addMenuDefinition(String menuId, boolean topMenu) {
        PropertyList out = new PropertyList();
        String id = menuId + " Menu";
        out.setProperty("menu", new PropertyListCollection());
        int count = 1;
        while (this.containsMenu(id)) {
            id = menuId + " " + count + " Menu";
            ++count;
        }
        DataSet d = this.getGizmoDef(id);
        if (d != null) {
            Random rand = new Random();
            while (d.size() > 0) {
                int n = rand.nextInt(100) + 1;
                id = menuId + " " + n + " Menu";
                d = this.getGizmoDef(id);
                if (d != null) continue;
                break;
            }
            out.setProperty("topmenu", topMenu ? "Y" : "N");
            this.menus.put(id, out);
            this.logger.debug("Menu Definition " + id + " created.");
        }
        return id;
    }

    private PropertyList addMenuItem(String menuId, String itemId, boolean show, String text, String url, boolean releaseLocks, String rolelist, String modulelist) {
        PropertyList menu = this.menus.get(menuId);
        if (menu != null) {
            PropertyList item = new PropertyList();
            item.setProperty("id", itemId);
            item.setProperty("type", "Menu Item");
            item.setProperty("show", show ? "Y" : "N");
            item.setProperty("text", text);
            item.setProperty("link", url);
            item.setProperty("releaselocks", releaseLocks ? "Y" : "N");
            if (rolelist != null && rolelist.length() > 0) {
                item.setAttribute("rolelist", rolelist);
            }
            if (modulelist != null && modulelist.length() > 0) {
                item.setAttribute("modulelist", modulelist);
            }
            menu.getCollection("menu").add(item);
            this.logger.debug("Menu Item " + itemId + " added.");
        }
        return menu;
    }

    private String addSubMenu(String menuId, String submenuId, boolean show, String text, boolean topMenu, String rolelist, String modulelist) {
        PropertyList menu = this.menus.get(menuId);
        String id = this.addMenuDefinition(submenuId, topMenu);
        if (menu != null) {
            PropertyList item = new PropertyList();
            item.setProperty("id", StringUtil.replaceAll(StringUtil.replaceAll(id, " ", "_"), "&", "n").toLowerCase());
            item.setProperty("type", "Submenu");
            item.setProperty("show", show ? "Y" : "N");
            item.setProperty("text", text);
            item.setProperty("submenu", id);
            if (rolelist != null && rolelist.length() > 0) {
                item.setAttribute("rolelist", rolelist);
            }
            if (modulelist != null && modulelist.length() > 0) {
                item.setAttribute("modulelist", modulelist);
            }
            menu.getCollection("menu").add(item);
            this.logger.debug("Submenu " + id + " added.");
        }
        return id;
    }

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        if (properties.getProperty("clearall", "N").equalsIgnoreCase("Y")) {
            try {
                this.database.executeSQL("DELETE categoryitem WHERE sdcid='LV_GizmoDef' AND keyid1 IN (SELECT gizmodefid FROM gizmodef WHERE propertytreeid = 'menugizmo')");
                this.database.executeSQL("DELETE gizmodef WHERE propertytreeid = 'menugizmo'");
            }
            catch (Exception e) {
                throw new SapphireException("Could not clear current menus");
            }
        }
        boolean isDevMode = false;
        try {
            ConfigurationProcessor config = new ConfigurationProcessor(this.getConnectionId());
            String devMode = config.getSysConfigProperty("devmode", "N");
            isDevMode = devMode.equalsIgnoreCase("Y");
        }
        catch (Exception config) {
            // empty catch block
        }
        if (properties.getProperty("generateasdevmode", "N").equalsIgnoreCase("Y")) {
            isDevMode = true;
        }
        this.menus = new HashMap();
        String propertytreeid = properties.getProperty("propertytreeid", "Generic");
        String nodeid = properties.getProperty("nodeid", "Sapphire Custom");
        if (propertytreeid.length() > 0 && nodeid.length() > 0) {
            WebAdminProcessor webAdminProcessor = new WebAdminProcessor(this.getConnectionid());
            try {
                String topMenuId = this.addMenuDefinition("Labvantage", true);
                PropertyTree propertyTree = webAdminProcessor.getPropertyTree(propertytreeid);
                PropertyList mainProps = propertyTree.getNodePropertyList(nodeid, true);
                PropertyListCollection propertyListCollection = mainProps.getCollection("links");
                for (int i = 0; i < propertyListCollection.size(); ++i) {
                    PropertyList layout;
                    PropertyList pagedata;
                    PropertyList link = propertyListCollection.getPropertyList(i);
                    String url = link.getProperty("link", "");
                    if (link.getProperty("show", "Y").equalsIgnoreCase("N") || url.length() <= 0 || !url.startsWith(STARTURL)) continue;
                    String page = url.substring(STARTURL.length());
                    int ia = page.indexOf("&");
                    if (ia > -1) {
                        page = page.substring(0, ia);
                    }
                    PropertyList pageProps = null;
                    if (isDevMode) {
                        SapphireConnection sc = this.getConnectionProcessor().getSapphireConnection();
                        sc.setConnection(this.database.getConnection());
                        RequestService rs = new RequestService(sc);
                        pageProps = rs.getWebPageProperties(page, rs.getDefaultWebPageEdition(page), new PropertyList(), false);
                    } else {
                        RequestProcessor requestProcessor = new RequestProcessor(this.getConnectionid());
                        pageProps = requestProcessor.getWebPageProperties(page, new RequestContext(new PropertyList()));
                    }
                    if (pageProps == null || (pagedata = pageProps.getPropertyList("pagedata")) == null || !pagedata.getProperty("objectname").equals(SITEMAPJSP) || (layout = pageProps.getPropertyList("layout")) == null) continue;
                    String currentLink = link.getProperty("text", "Menu " + i);
                    String submenuid = this.addSubMenu(topMenuId, currentLink, true, currentLink, true, "", "");
                    this.writeOutSitemap(layout, submenuid);
                }
            }
            catch (Exception e) {
                throw new SapphireException("Failed to build menu. " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
            if (this.menus != null && this.menus.size() > 0) {
                this.logger.info("Menu definition built with " + this.menus.size() + " items.");
                DataSet categoryitem = new DataSet();
                categoryitem.addColumn("categoryid", 0);
                categoryitem.addColumn("sdcid", 0);
                categoryitem.addColumn("keyid1", 0);
                DataSet out = new DataSet();
                out.addColumn("gizmodefid", 0);
                out.addColumn("gizmodefdesc", 0);
                out.addColumn("propertytreeid", 0);
                out.addColumn("extendnodeid", 0);
                out.addColumn((isDevMode ? "product" : "") + "valuetree", 3);
                for (Map.Entry entry : this.menus.entrySet()) {
                    String menuid = (String)entry.getKey();
                    PropertyList definition = (PropertyList)entry.getValue();
                    int row = out.addRow();
                    out.setValue(row, "gizmodefid", menuid);
                    out.setValue(row, "gizmodefdesc", menuid);
                    out.setValue(row, "propertytreeid", "menugizmo");
                    out.setValue(row, "extendnodeid", "Sapphire Custom");
                    out.setClob(row, (isDevMode ? "product" : "") + "valuetree", definition.toXMLString());
                    int catrow = categoryitem.addRow();
                    categoryitem.setValue(row, "categoryid", "SAPPHIREConfig");
                    categoryitem.setValue(row, "sdcid", "LV_GizmoDef");
                    categoryitem.setValue(row, "keyid1", menuid);
                }
                try {
                    DataSetUtil.insert(this.database, out, "gizmodef");
                    this.logger.info("Menus created successfully.");
                    if (isDevMode) {
                        try {
                            DataSet testcat = this.getQueryProcessor().getSqlDataSet("SELECT categoryid FROM category WHERE sdcid='LV_GizmoDef' AND categoryid='SAPPHIREConfig'");
                            if (testcat == null || testcat.getRowCount() == 0) {
                                this.database.executeSQL("INSERT INTO category (categoryid, sdcid) VALUES ('SAPPHIREConfig','LV_GizmoDef')");
                            }
                        }
                        catch (Exception e1) {
                            this.logger.warn("Could not test and create SAPPHIREConfig category.");
                        }
                        try {
                            DataSetUtil.insert(this.database, categoryitem, "categoryitem");
                            this.logger.info("Menus Categories created successfully.");
                        }
                        catch (Exception e2) {
                            this.logger.warn("Could not create SAPPHIREConfig category items.");
                        }
                    }
                    if (isDevMode) {
                        // empty if block
                    }
                    CacheUtil.clear(this.getConnectionProcessor().getSapphireConnection().getDatabaseId(), "GizmoDef");
                }
                catch (Exception e) {
                    throw new SapphireException("Could not insert new menus. " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
                }
            }
        } else {
            throw new SapphireException("No layout and/or node provided.");
        }
    }
}

