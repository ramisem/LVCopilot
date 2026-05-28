/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.misc;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.BaseRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.sdc.WebPageRenderer;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.MenuSystemRO;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MenuSystemRenderer
extends BaseRenderer {
    private MenuSystemRO menuSystemRO;
    private MenuSystemRO refMenuSystemRO;
    private boolean menuPageButtonRoleMatrixIncluded;
    private PropertyList config;
    private ArrayList menuNameList;

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO ro, HashMap sdisIncluded, boolean includeMenuPageButtonRoleMatrix) {
        super.initialize(sapphireConnection, config, ro, sdisIncluded);
        this.menuSystemRO = (MenuSystemRO)ro;
        this.menuPageButtonRoleMatrixIncluded = includeMenuPageButtonRoleMatrix;
        try {
            this.menuNameList = this.menuSystemRO.getMenuNameList();
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Confirm.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Confirm.gif"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
        this.config = config;
    }

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO srcro, BaseRO refRO, HashMap sdisIncluded, boolean includeDiffReport, boolean includeSiteMapPageButtonRoleMatrix) {
        super.initialize(sapphireConnection, config, srcro, refRO, sdisIncluded, includeDiffReport);
        this.menuSystemRO = (MenuSystemRO)srcro;
        this.refMenuSystemRO = (MenuSystemRO)refRO;
        this.menuPageButtonRoleMatrixIncluded = includeSiteMapPageButtonRoleMatrix;
        try {
            this.menuNameList = this.menuSystemRO.getMenuNameList();
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Confirm.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Confirm.gif"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
        this.config = config;
    }

    private ArrayList mergeSectionLists(ArrayList sectionList, ArrayList refSectionList) {
        ArrayList merged = new ArrayList();
        merged = (ArrayList)sectionList.clone();
        if (refSectionList.size() > 0) {
            for (int i = 0; i < refSectionList.size(); ++i) {
                String currRefSec = refSectionList.get(i).toString();
                boolean found = false;
                for (int j = 0; j < sectionList.size(); ++j) {
                    if (!currRefSec.equals(sectionList.get(j))) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                merged.add(currRefSec);
            }
        }
        return merged;
    }

    @Override
    public ArrayList getSectionList() throws SapphireException {
        ArrayList sectionList = new ArrayList();
        if (!this.includeDiffReport) {
            sectionList = this.menuSystemRO.getMenuNameList();
            if (this.menuPageButtonRoleMatrixIncluded && !sectionList.contains("Menu Driven Page Button Role Matrix")) {
                sectionList.add("Menu Driven Page Button Role Matrix");
            }
        } else {
            if (this.menuSystemRO != null) {
                sectionList = this.menuSystemRO.getMenuNameList();
            }
            ArrayList refSectionList = new ArrayList();
            if (this.refMenuSystemRO != null) {
                refSectionList = this.refMenuSystemRO.getMenuNameList();
            }
            sectionList = this.mergeSectionLists(sectionList, refSectionList);
            if (this.menuPageButtonRoleMatrixIncluded && !sectionList.contains("Menu Driven Page Button Role Matrix")) {
                sectionList.add("Menu Driven Page Button Role Matrix");
            }
        }
        return sectionList;
    }

    @Override
    public boolean hasChapterChanged() {
        return this.chapterChanged;
    }

    public void reportNoFrames(String chapterNo, OutputStream reportStream) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Menu System chapter");
        configReportContent.startChapter(chapterNo, "Menu System", "");
        for (int i = 0; i < this.menuNameList.size(); ++i) {
            String currentSiteMap = this.menuNameList.get(i).toString();
            configReportContent.startSection(currentSiteMap);
            this.renderMenuSummary(configReportContent, currentSiteMap);
            if (this.includeDiffReport && this.refMenuSystemRO != null) {
                this.renderDiffMenuDetails(configReportContent, currentSiteMap);
            } else {
                this.renderMenuDetails(configReportContent, currentSiteMap);
            }
            configReportContent.endSection();
            configReportContent.pageBreak();
            this.updateSectionChangeInfo("Menu System", ConfigReportContent.generateSectionTitle(currentSiteMap), configReportContent);
        }
        if (this.menuPageButtonRoleMatrixIncluded) {
            configReportContent.clearContent();
            configReportContent.startSection("Menu Driven Page Button Role Matrix");
            configReportContent.appendSubSection(this.renderFullRoleMatrix(), "Menu Driven Page Button Role Matrix", this.diffOnly);
            configReportContent.endSection();
            this.updateSectionChangeInfo("Menu System", ConfigReportContent.generateSectionTitle("Menu Driven Page Button Role Matrix"), configReportContent);
            configReportContent.pageBreak();
        }
        configReportContent.endChapter(chapterNo);
        try {
            reportStream.write(configReportContent.toString().getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    public void reportWithFrames(String chapterNo) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Menu System");
        ArrayList sitemaps = this.menuSystemRO.getMenuNameList();
        for (int i = 0; i < sitemaps.size(); ++i) {
            FileOutputStream sitemapFile;
            String currentSiteMap = sitemaps.get(i).toString();
            String sectionFileName = ConfigReportContent.generateSectionFileName("Menu System", currentSiteMap);
            try {
                sitemapFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report file " + sectionFileName);
            }
            configReportContent.clearContent();
            configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("Menu System", currentSiteMap));
            configReportContent.startSection(currentSiteMap);
            this.renderMenuSummary(configReportContent, currentSiteMap);
            if (this.includeDiffReport && this.refMenuSystemRO != null) {
                this.renderDiffMenuDetails(configReportContent, currentSiteMap);
            } else {
                this.renderMenuDetails(configReportContent, currentSiteMap);
            }
            configReportContent.endSection();
            this.updateSectionChangeInfo("Menu System", ConfigReportContent.generateSectionTitle(currentSiteMap), configReportContent);
            this.createSubSectionInfo("Menu System", ConfigReportContent.generateSectionTitle(currentSiteMap), configReportContent.diffInfo);
            configReportContent.endFile();
            try {
                sitemapFile.write(configReportContent.toString().getBytes(StandardCharsets.UTF_8));
                sitemapFile.close();
                continue;
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
        if (this.menuPageButtonRoleMatrixIncluded) {
            FileOutputStream rolematrixFile;
            String sectionFileName = ConfigReportContent.generateSectionFileName("Menu System", "Menu Driven Page Button Role Matrix");
            try {
                rolematrixFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report file " + sectionFileName);
            }
            configReportContent.clearContent();
            configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("Menu System", "Menu Driven Page Button Role Matrix"));
            configReportContent.startSection("Menu Driven Page Button Role Matrix");
            configReportContent.appendSubSection(this.renderFullRoleMatrix(), "Menu Driven Page Button Role Matrix", this.diffOnly);
            configReportContent.endSection();
            this.updateSectionChangeInfo("Menu System", ConfigReportContent.generateSectionTitle("Menu Driven Page Button Role Matrix"), configReportContent);
            configReportContent.endFile();
            this.createSubSectionInfo("Menu System", ConfigReportContent.generateSectionTitle("Menu Driven Page Button Role Matrix"), configReportContent.diffInfo);
            try {
                rolematrixFile.write(configReportContent.toString().getBytes(StandardCharsets.UTF_8));
                rolematrixFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
    }

    public ConfigReportContent renderFullRoleMatrix() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "menu system role matrix");
        if (this.includeDiffReport && this.refMenuSystemRO != null) {
            DataSet rolematrix = this.getRawLayoutRoleMatrix(this.menuSystemRO);
            DataSet refRoleMatrix = this.getRawLayoutRoleMatrix(this.refMenuSystemRO);
            String[] keycols = new String[]{"Type", "Name", "Menu", "Menuitem", "Menulink"};
            DataSet diff = configReportContent.getMenuMatrixDiffInfo(rolematrix, refRoleMatrix, keycols);
            configReportContent.appendSubSection(this.renderRoleMatrixFromRaw(diff), "Menu Driven Page Button Role Matrix", this.diffOnly);
            configReportContent.startSubHeading("Source", "");
            configReportContent.renderRoleMatrix(rolematrix, 1);
            configReportContent.startSubHeading("Target", "");
            configReportContent.renderRoleMatrix(refRoleMatrix, 1);
        } else {
            configReportContent.renderRoleMatrix(this.getLayoutRoleMatrix(this.menuSystemRO), 1);
        }
        return configReportContent;
    }

    public ConfigReportContent renderTabInfo(PropertyList tabPropertyList) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "menu system");
        String roles = tabPropertyList.getAttribute("rolelist");
        String modules = tabPropertyList.getAttribute("modulelist");
        String link = tabPropertyList.getProperty("link");
        boolean available = false;
        if (modules != null && modules.length() > 0) {
            String[] moduleArr = StringUtil.split(modules, ";");
            for (int i = 0; i < moduleArr.length; ++i) {
                available |= this.menuSystemRO.checkModuleAvailable(moduleArr[i]);
            }
        } else {
            available = true;
        }
        if (available) {
            configReportContent.startTable();
            if (roles.length() > 0) {
                configReportContent.startRow();
                configReportContent.addRowItem("Tramline Available to roles", roles.replaceAll(";", ", "));
                configReportContent.endRow();
            }
            configReportContent.startRow();
            configReportContent.addRowItem("Default link", ConfigReportContent.renderLink(link, this.sdisIncluded, this.frames, this.connection));
            configReportContent.endRow();
            configReportContent.endTable();
        }
        return configReportContent;
    }

    private SortedSet getAllRoles(PropertyListCollection collection) throws SapphireException {
        TreeSet<String> s = new TreeSet<String>();
        for (int i = 0; i < collection.size(); ++i) {
            String rolelist;
            PropertyList currPL = collection.getPropertyList(i);
            String show = currPL.getProperty("show", "Y");
            String modules = currPL.getAttribute("modulelist");
            boolean available = false;
            if (modules != null && modules.length() > 0) {
                String[] moduleArr = StringUtil.split(modules, ";");
                for (int modulenum = 0; modulenum < moduleArr.length; ++modulenum) {
                    available |= this.menuSystemRO.checkModuleAvailable(moduleArr[modulenum]);
                }
            } else {
                available = true;
            }
            if (!available || "N".equals(show) || (rolelist = currPL.getAttribute("rolelist")) == null || rolelist.length() <= 0) continue;
            String[] roles = StringUtil.split(rolelist, ";");
            for (int j = 0; j < roles.length; ++j) {
                if (roles[j].trim().length() <= 0) continue;
                s.add(roles[j]);
            }
        }
        return s;
    }

    public void renderMenuSummary(ConfigReportContent configReportContent, String menu) throws SapphireException {
        PropertyList menuGizmoPL = this.menuSystemRO.getMenuDetails(menu);
        DataSet ds = this.getMenuItemsRoleMatrix(menuGizmoPL);
        DataSet refDs = null;
        ConfigReportContent summary = new ConfigReportContent(this.config, "MenuItemSummary");
        summary.startSubSection(menu + " Summary", menu + "  has the following Menu Items <P>");
        if (refDs != null) {
            summary.appendSubSection(this.renderDiffMenuItems(menu), "MenuItemSummary", this.diffOnly);
            summary.startSubHeading("Menu Item Links", "");
            String[] keycols = new String[]{"Menuitem"};
            summary.renderDiffRoleMatrix(ds, refDs, keycols);
        } else {
            summary.appendSubSection(this.renderMenuItems(menu), "MenuItemSummary", this.diffOnly);
            summary.startSubHeading("Menu Item Links", "");
            summary.renderRoleMatrix(ds, 2);
        }
        configReportContent.appendSubSection(summary, menu + " Summary", this.diffOnly);
    }

    private DataSet getMenuItemsRoleMatrix(PropertyList currMenu) throws SapphireException {
        if (currMenu == null) {
            throw new SapphireException("Menu is null");
        }
        PropertyListCollection tabs = currMenu.getCollectionNotNull("tabs");
        SortedSet roles = this.getAllRoles(tabs);
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        ds.addColumn("Menuitem", 0);
        ds.addColumn("Link", 0);
        for (int roleno = 0; roleno < roles.size(); ++roleno) {
            ds.addColumn(roles.toArray()[roleno].toString(), 0);
        }
        for (int i = 0; i < tabs.size(); ++i) {
            PropertyList currPL = tabs.getPropertyList(i);
            String tabName = currPL.getProperty("text", currPL.getId());
            String showTab = currPL.getProperty("show", "Y");
            String modules = currPL.getAttribute("modulelist");
            boolean available = false;
            if (modules != null && modules.length() > 0) {
                String[] moduleArr = StringUtil.split(modules, ";");
                for (int modulenum = 0; modulenum < moduleArr.length; ++modulenum) {
                    available |= this.menuSystemRO.checkModuleAvailable(moduleArr[modulenum]);
                }
            } else {
                available = true;
            }
            if (!available || "N".equals(showTab)) continue;
            int currRow = ds.addRow();
            String anchor = ConfigReportContent.convertToID(tabName);
            String hyperlink = "<A HREF=#" + anchor + ">" + tabName + "</A>";
            ds.setString(currRow, "Menuitem", hyperlink);
            ds.setString(currRow, "Link", ConfigReportContent.renderLink(currPL.getProperty("link"), this.sdisIncluded, this.frames, this.connection));
            String rolelist = currPL.getAttribute("rolelist");
            if (rolelist == null || rolelist.length() <= 0) continue;
            String[] roleArr = StringUtil.split(rolelist, ";");
            for (int j = 0; j < roleArr.length; ++j) {
                String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roleArr[j] + "\" title=\"" + roleArr[j] + "\">";
                ds.setString(currRow, roleArr[j], includeImg);
            }
        }
        return ds;
    }

    public void renderMenuDetails(ConfigReportContent configReportContent, String menu) throws SapphireException {
        PropertyList sitemapPl = this.menuSystemRO.getMenuDetails(menu);
        PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
        for (int i = 0; i < tabs.size(); ++i) {
            PropertyList currPL = tabs.getPropertyList(i);
            String tabName = currPL.getProperty("text", currPL.getId());
            String showTab = currPL.getProperty("show", "Y");
            ConfigReportContent tabContent = new ConfigReportContent(this.config, tabName);
            if (!"N".equals(showTab)) {
                String anchor = ConfigReportContent.convertToID(tabName);
                tabContent.startSubHeading(tabName, "", anchor);
                PropertyListCollection menus = currPL.getCollectionNotNull("menus");
                SortedSet roles = this.getAllRoles(menus);
                DataSet ds = new DataSet();
                ds.setColidCaseSensitive(true);
                ds.addColumn("Menuitem", 0);
                ds.addColumn("Link", 0);
                for (int roleno = 0; roleno < roles.size(); ++roleno) {
                    ds.addColumn(roles.toArray()[roleno].toString(), 0);
                }
                for (int j = 0; j < menus.size(); ++j) {
                    PropertyList menuItems = menus.getPropertyList(j);
                    String menuName = menuItems.getProperty("text", menuItems.getId());
                    String showMenu = menuItems.getProperty("show", "Y");
                    String modules = menuItems.getAttribute("modulelist");
                    boolean available = false;
                    if (modules != null && modules.length() > 0) {
                        String[] moduleArr = StringUtil.split(modules, ";");
                        for (int k = 0; k < moduleArr.length; ++k) {
                            available |= this.menuSystemRO.checkModuleAvailable(moduleArr[k]);
                        }
                    } else {
                        available = true;
                    }
                    if (!available || !"Y".equals(showMenu)) continue;
                    int currRow = ds.addRow();
                    ds.setString(currRow, "Menuitem", menuName);
                    ds.setString(currRow, "Link", ConfigReportContent.renderLink(menuItems.getProperty("link"), this.sdisIncluded, this.frames, this.connection));
                    String rolelist = menuItems.getAttribute("rolelist");
                    if (rolelist == null || rolelist.length() <= 0) continue;
                    String[] roleArr = StringUtil.split(rolelist, ";");
                    for (int k = 0; k < roleArr.length; ++k) {
                        String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roleArr[k] + "\" title=\"" + roleArr[k] + "\">";
                        ds.setString(currRow, roleArr[k], includeImg);
                    }
                }
                tabContent.renderRoleMatrix(ds, 2);
            }
            configReportContent.appendSubSection(tabContent, tabName, this.diffOnly);
        }
    }

    public ConfigReportContent renderMenuItems(String menu) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, menu);
        PropertyList sitemapPl = this.menuSystemRO.getMenuDetails(menu);
        configReportContent.append("<TABLE class=\"\">");
        configReportContent.append("<TR valign=\"bottom\">");
        PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
        int count = 0;
        for (int i = 0; i < tabs.size(); ++i) {
            PropertyList currPL = tabs.getPropertyList(i);
            String tabName = currPL.getProperty("text", currPL.getId());
            String showTab = currPL.getProperty("show", "Y");
            ConfigReportContent tabContent = new ConfigReportContent(this.config, tabName);
            if (!"N".equals(showTab)) {
                String anchor = ConfigReportContent.convertToID(tabName);
                PropertyListCollection menus = currPL.getCollectionNotNull("menus");
                DataSet ds = new DataSet();
                ds.setColidCaseSensitive(true);
                tabName = ConfigReportContent.createHyperLink(tabName, anchor);
                ds.addColumn(tabName, 0);
                for (int j = 0; j < menus.size(); ++j) {
                    PropertyList menuItems = menus.getPropertyList(j);
                    String menuName = menuItems.getProperty("text", menuItems.getId());
                    String showMenu = menuItems.getProperty("show", "Y");
                    String modules = menuItems.getAttribute("modulelist");
                    boolean available = false;
                    if (modules != null && modules.length() > 0) {
                        String[] moduleArr = StringUtil.split(modules, ";");
                        for (int k = 0; k < moduleArr.length; ++k) {
                            available |= this.menuSystemRO.checkModuleAvailable(moduleArr[k]);
                        }
                    } else {
                        available = true;
                    }
                    if (!available || !"Y".equals(showMenu)) continue;
                    int currRow = ds.addRow();
                    ds.setString(currRow, tabName, menuName);
                }
                this.renderMenuCard(tabContent, ds);
            }
            if (tabContent.length() <= 0) continue;
            configReportContent.append("<TD style=\"border-spacing:0;padding:0;border-collapse:collapse;\" width=250px valign=\"bottom\">");
            configReportContent.append(tabContent.toString());
            configReportContent.append("</TD>");
            if (++count % 6 != 0) continue;
            configReportContent.append("</TR><TR valign=\"bottom\">");
        }
        configReportContent.append("</TR>");
        configReportContent.append("</Table>");
        return configReportContent;
    }

    private PropertyList findRefTabProps(String tabname, PropertyListCollection refTabs) {
        for (int i = 0; i < refTabs.size(); ++i) {
            PropertyList pl = refTabs.getPropertyList(i);
            String tname = pl.getProperty("text", pl.getId());
            if (!tname.equals(tabname)) continue;
            return pl;
        }
        return null;
    }

    public ConfigReportContent renderDiffMenuItems(String menu) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, menu);
        PropertyList sitemapPl = this.menuSystemRO.getMenuDetails(menu);
        PropertyList refSitemapPl = null;
        PropertyListCollection srctabs = sitemapPl.getCollectionNotNull("tabs");
        if (this.includeDiffReport && this.refMenuSystemRO != null) {
            refSitemapPl = this.refMenuSystemRO.getMenuDetails(menu);
            PropertyListCollection reftabs = refSitemapPl.getCollectionNotNull("tabs");
            for (int i = 0; i < srctabs.size(); ++i) {
                PropertyList currPL = srctabs.getPropertyList(i);
                String tabName = currPL.getProperty("text", currPL.getId());
                String showTab = currPL.getProperty("show", "Y");
                PropertyList refcurrPL = this.findRefTabProps(tabName, reftabs);
                ConfigReportContent tabContent = new ConfigReportContent(this.config, tabName);
                if (!"N".equals(showTab)) {
                    int k;
                    boolean available;
                    String modules;
                    String showMenu;
                    String menuName;
                    PropertyList menuItems;
                    int j;
                    String anchor = ConfigReportContent.convertToID(tabName);
                    PropertyListCollection srcmenus = currPL.getCollectionNotNull("menus");
                    PropertyListCollection refmenus = refcurrPL.getCollectionNotNull("menus");
                    DataSet srcds = new DataSet();
                    srcds.setColidCaseSensitive(true);
                    tabName = ConfigReportContent.createHyperLink(tabName, anchor);
                    srcds.addColumn(tabName, 0);
                    DataSet refds = new DataSet();
                    refds.setColidCaseSensitive(true);
                    refds.addColumn(tabName, 0);
                    for (j = 0; j < srcmenus.size(); ++j) {
                        menuItems = srcmenus.getPropertyList(j);
                        menuName = menuItems.getProperty("text", menuItems.getId());
                        showMenu = menuItems.getProperty("show", "Y");
                        modules = menuItems.getAttribute("modulelist");
                        available = false;
                        if (modules != null && modules.length() > 0) {
                            String[] moduleArr = StringUtil.split(modules, ";");
                            for (k = 0; k < moduleArr.length; ++k) {
                                available |= this.menuSystemRO.checkModuleAvailable(moduleArr[k]);
                            }
                        } else {
                            available = true;
                        }
                        if (!available || !"Y".equals(showMenu)) continue;
                        int currRow = srcds.addRow();
                        srcds.setString(currRow, tabName, menuName);
                    }
                    for (j = 0; j < refmenus.size(); ++j) {
                        menuItems = refmenus.getPropertyList(j);
                        menuName = menuItems.getProperty("text", menuItems.getId());
                        showMenu = menuItems.getProperty("show", "Y");
                        modules = menuItems.getAttribute("modulelist");
                        available = false;
                        if (modules != null && modules.length() > 0) {
                            String[] moduleArr = StringUtil.split(modules, ";");
                            for (k = 0; k < moduleArr.length; ++k) {
                                available |= this.refMenuSystemRO.checkModuleAvailable(moduleArr[k]);
                            }
                        } else {
                            available = true;
                        }
                        if (!available || !"Y".equals(showMenu)) continue;
                        int currRow = refds.addRow();
                        refds.setString(currRow, tabName, menuName);
                    }
                    String[] keycols = new String[]{tabName};
                    tabContent.renderDiffListTable(srcds, refds, keycols);
                }
                configReportContent.append("<TD valign=\"bottom\">");
                configReportContent.append(tabContent.toString());
                configReportContent.append("</TD>");
            }
            configReportContent.append("</TR>");
            configReportContent.append("</Table>");
        }
        return configReportContent;
    }

    public void renderDiffMenuDetails(ConfigReportContent configReportContent, String menu) throws SapphireException {
        PropertyList sitemapPl = this.menuSystemRO.getMenuDetails(menu);
        PropertyList refSitemapPl = null;
        PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
        if (this.includeDiffReport && this.refMenuSystemRO != null && (refSitemapPl = this.refMenuSystemRO.getMenuDetails(menu)) != null) {
            PropertyListCollection reftabs = refSitemapPl.getCollectionNotNull("tabs");
            for (int i = 0; i < tabs.size(); ++i) {
                PropertyList currPL = tabs.getPropertyList(i);
                String tabName = currPL.getProperty("text", currPL.getId());
                String showTab = currPL.getProperty("show", "Y");
                PropertyList refPL = this.findRefTabProps(tabName, reftabs);
                ConfigReportContent tabContent = new ConfigReportContent(this.config, tabName);
                if (!"N".equals(showTab)) {
                    tabContent.startSubSection(tabName, "");
                    DataSet ds = this.getMenusRoleMatrix(this.menuSystemRO, currPL);
                    DataSet refDs = this.getMenusRoleMatrix(this.refMenuSystemRO, refPL);
                    String[] keycols = new String[]{"Menu", "Link"};
                    tabContent.renderDiffRoleMatrix(ds, refDs, keycols);
                }
                configReportContent.appendSubSection(tabContent, tabName, this.diffOnly);
            }
        }
    }

    private DataSet getMenusRoleMatrix(MenuSystemRO ro, PropertyList currTabPl) throws SapphireException {
        if (currTabPl == null) {
            currTabPl = new PropertyList();
        }
        PropertyListCollection menus = currTabPl.getCollectionNotNull("menus");
        SortedSet roles = this.getAllRoles(menus);
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        ds.addColumn("Menu", 0);
        ds.addColumn("Link", 0);
        for (int roleno = 0; roleno < roles.size(); ++roleno) {
            ds.addColumn(roles.toArray()[roleno].toString(), 0);
        }
        for (int j = 0; j < menus.size(); ++j) {
            PropertyList menuItems = menus.getPropertyList(j);
            String menuName = menuItems.getProperty("text", menuItems.getId());
            String showMenu = menuItems.getProperty("show", "Y");
            String modules = menuItems.getAttribute("modulelist");
            boolean available = false;
            if (modules != null && modules.length() > 0) {
                String[] moduleArr = StringUtil.split(modules, ";");
                for (int k = 0; k < moduleArr.length; ++k) {
                    available |= this.menuSystemRO.checkModuleAvailable(moduleArr[k]);
                }
            } else {
                available = true;
            }
            if (!available || !"Y".equals(showMenu)) continue;
            int currRow = ds.addRow();
            ds.setString(currRow, "Menu", menuName);
            ds.setString(currRow, "Link", ConfigReportContent.renderLink(menuItems.getProperty("link"), this.sdisIncluded, this.frames, this.connection));
            String rolelist = menuItems.getAttribute("rolelist");
            if (rolelist == null || rolelist.length() <= 0) continue;
            String[] roleArr = StringUtil.split(rolelist, ";");
            for (int k = 0; k < roleArr.length; ++k) {
                String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roleArr[k] + "\" title=\"" + roleArr[k] + "\">";
                ds.setString(currRow, roleArr[k], includeImg);
            }
        }
        return ds;
    }

    private PropertyList findMenuItem(String menuname, PropertyListCollection menus) {
        for (int i = 0; i < menus.size(); ++i) {
            PropertyList menuItems = menus.getPropertyList(i);
            String currMenuName = menuItems.getProperty("text", menuItems.getId());
            if (!currMenuName.equals(menuname) || !"Y".equals(menuItems.getProperty("show", "Y"))) continue;
            return menuItems;
        }
        return null;
    }

    public DataSet getRawLayoutRoleMatrix(MenuSystemRO ro) throws SapphireException {
        if (ro.dataSource.equals("XMLREPORT")) {
            return ro.getMenuDrivenRoleMatrixFromXMLReport();
        }
        DataSet roleMatrix = new DataSet();
        roleMatrix.setColidCaseSensitive(true);
        roleMatrix.addColumn("Type", 0);
        roleMatrix.addColumn("Name", 0);
        roleMatrix.addColumn("Menu", 0);
        roleMatrix.addColumn("Menuitem", 0);
        roleMatrix.addColumn("Menulink", 0);
        roleMatrix.addColumn("Button", 0);
        ArrayList sitemaps = ro.getMenuNameList();
        for (int i = 0; i < sitemaps.size(); ++i) {
            String sitemap = (String)sitemaps.get(i);
            PropertyList sitemapPl = ro.getMenuDetails(sitemap);
            PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
            int currRow = roleMatrix.addRow();
            roleMatrix.setString(currRow, "Name", sitemap);
            roleMatrix.setString(currRow, "Type", "Sitemap");
            roleMatrix.setString(currRow, "Menu", "N/A");
            roleMatrix.setString(currRow, "Menuitem", "N/A");
            roleMatrix.setString(currRow, "Menulink", "N/A");
            roleMatrix.setString(currRow, "Button", "N/A");
            for (int tab = 0; tab < tabs.size(); ++tab) {
                int j;
                PropertyList tabPropertyList = tabs.getPropertyList(tab);
                String tabName = tabPropertyList.getProperty("text", tabPropertyList.getId());
                String showTab = tabPropertyList.getProperty("show", "Y");
                String modules = tabPropertyList.getAttribute("modulelist");
                boolean available = false;
                if (modules != null && modules.length() > 0) {
                    String[] moduleArr = StringUtil.split(modules, ";");
                    for (int modulenum = 0; modulenum < moduleArr.length; ++modulenum) {
                        available |= this.menuSystemRO.checkModuleAvailable(moduleArr[modulenum]);
                    }
                } else {
                    available = true;
                }
                if (!available || "N".equals(showTab)) continue;
                currRow = roleMatrix.addRow();
                roleMatrix.setString(currRow, "Name", tabName);
                roleMatrix.setString(currRow, "Type", "Tab");
                roleMatrix.setString(currRow, "Menu", sitemap);
                roleMatrix.setString(currRow, "Menuitem", "N/A");
                roleMatrix.setString(currRow, "Menulink", "N/A");
                String rolelist = tabPropertyList.getAttribute("rolelist");
                if (rolelist != null && rolelist.length() > 0) {
                    String[] roleArr = StringUtil.split(rolelist, ";");
                    for (j = 0; j < roleArr.length; ++j) {
                        String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roleArr[j] + "\" title=\"" + roleArr[j] + "\">";
                        roleMatrix.setString(currRow, roleArr[j], includeImg);
                    }
                }
                PropertyListCollection menus = tabPropertyList.getCollectionNotNull("menus");
                for (j = 0; j < menus.size(); ++j) {
                    PropertyList menuItems = menus.getPropertyList(j);
                    String menuName = menuItems.getProperty("text", menuItems.getId());
                    String showMenu = menuItems.getProperty("show", "Y");
                    String menumodules = menuItems.getAttribute("modulelist");
                    boolean menuavailable = false;
                    if (menumodules != null && menumodules.length() > 0) {
                        String[] moduleArr = StringUtil.split(menumodules, ";");
                        for (int k = 0; k < moduleArr.length; ++k) {
                            menuavailable |= this.menuSystemRO.checkModuleAvailable(moduleArr[k]);
                        }
                    } else {
                        menuavailable = true;
                    }
                    if (!menuavailable || !"Y".equals(showMenu)) continue;
                    currRow = roleMatrix.addRow();
                    String link = menuItems.getProperty("link");
                    String pageName = ConfigReportContent.getPageName(link);
                    roleMatrix.setString(currRow, "Type", "Menu");
                    roleMatrix.setString(currRow, "Menu", sitemap);
                    roleMatrix.setString(currRow, "Menuitem", tabName);
                    roleMatrix.setString(currRow, "Name", menuName);
                    roleMatrix.setString(currRow, "Menulink", "N/A");
                    roleMatrix.setString(currRow, "Link", link);
                    String menurolelist = menuItems.getAttribute("rolelist");
                    if (menurolelist != null && menurolelist.length() > 0) {
                        String[] roleArr = StringUtil.split(menurolelist, ";");
                        for (int k = 0; k < roleArr.length; ++k) {
                            String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roleArr[k] + "\" title=\"" + roleArr[k] + "\">";
                            roleMatrix.setString(currRow, roleArr[k], includeImg);
                        }
                    }
                    if (pageName == null || pageName.length() <= 0) continue;
                    ArrayList<SDI> sdiList = new ArrayList<SDI>();
                    SDI sdi = new SDI("WebPage", pageName, ConfigReportContent.getPageEdition(this.connection, pageName), "");
                    sdiList.add(sdi);
                    WebPageRO webpageRO = ro.getWebPageRO();
                    webpageRO.setSDIList(sdiList);
                    webpageRO.startChapter();
                    WebPageRenderer webpageRenderer = new WebPageRenderer();
                    webpageRenderer.initialize(this.sapphireConnection, this.config, (BaseRO)webpageRO, this.sdisIncluded, false, true);
                    try {
                        webpageRO.nextSection();
                        webpageRO.startSection();
                        DataSet buttonRoles = webpageRenderer.getCurrentPageButtonRoles(webpageRO);
                        if (buttonRoles == null) continue;
                        String[] cols = buttonRoles.getColumns();
                        for (int buttonrole = 0; buttonrole < buttonRoles.getRowCount(); ++buttonrole) {
                            currRow = roleMatrix.addRow();
                            roleMatrix.setString(currRow, "Type", "Button");
                            roleMatrix.setString(currRow, "Menu", sitemap);
                            roleMatrix.setString(currRow, "Menuitem", tabName);
                            roleMatrix.setString(currRow, "Menulink", menuName);
                            for (int colnum = 0; colnum < cols.length; ++colnum) {
                                if (cols[colnum].equals("Button/Operation")) {
                                    roleMatrix.setString(currRow, "Name", buttonRoles.getString(buttonrole, cols[colnum]));
                                    continue;
                                }
                                roleMatrix.setString(currRow, cols[colnum], buttonRoles.getString(buttonrole, cols[colnum]));
                            }
                        }
                        continue;
                    }
                    catch (SapphireException e) {
                        this.logger.error("Error in role matrix creation", e);
                    }
                }
            }
        }
        return roleMatrix;
    }

    public DataSet getLayoutRoleMatrix(MenuSystemRO currRO) throws SapphireException {
        if (currRO.dataSource.equals("XMLREPORT")) {
            return currRO.getMenuDrivenRoleMatrixFromXMLReport();
        }
        DataSet roleMatrix = new DataSet();
        roleMatrix.setColidCaseSensitive(true);
        roleMatrix.addColumn("Item", 0);
        ArrayList sitemaps = currRO.getMenuNameList();
        for (int i = 0; i < sitemaps.size(); ++i) {
            String sitemap = (String)sitemaps.get(i);
            PropertyList sitemapPl = currRO.getMenuDetails(sitemap);
            PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
            int currRow = roleMatrix.addRow();
            roleMatrix.setString(currRow, "Item", "Menu: " + sitemap);
            for (int tab = 0; tab < tabs.size(); ++tab) {
                int j;
                PropertyList tabPropertyList = tabs.getPropertyList(tab);
                String tabName = tabPropertyList.getProperty("text", tabPropertyList.getId());
                String showTab = tabPropertyList.getProperty("show", "Y");
                String modules = tabPropertyList.getAttribute("modulelist");
                boolean available = false;
                if (modules != null && modules.length() > 0) {
                    String[] moduleArr = StringUtil.split(modules, ";");
                    for (int modulenum = 0; modulenum < moduleArr.length; ++modulenum) {
                        available |= this.menuSystemRO.checkModuleAvailable(moduleArr[modulenum]);
                    }
                } else {
                    available = true;
                }
                if (!available || "N".equals(showTab)) continue;
                currRow = roleMatrix.addRow();
                roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;Menuitem: " + tabName);
                String rolelist = tabPropertyList.getAttribute("rolelist");
                if (rolelist != null && rolelist.length() > 0) {
                    String[] roleArr = StringUtil.split(rolelist, ";");
                    for (j = 0; j < roleArr.length; ++j) {
                        String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roleArr[j] + "\" title=\"" + roleArr[j] + "\">";
                        roleMatrix.setString(currRow, roleArr[j], includeImg);
                    }
                }
                PropertyListCollection menus = tabPropertyList.getCollectionNotNull("menus");
                for (j = 0; j < menus.size(); ++j) {
                    PropertyList menuItems = menus.getPropertyList(j);
                    String menuName = menuItems.getProperty("text", menuItems.getId());
                    String showMenu = menuItems.getProperty("show", "Y");
                    String menumodules = menuItems.getAttribute("modulelist");
                    boolean menuavailable = false;
                    if (menumodules != null && menumodules.length() > 0) {
                        String[] moduleArr = StringUtil.split(menumodules, ";");
                        for (int k = 0; k < moduleArr.length; ++k) {
                            menuavailable |= this.menuSystemRO.checkModuleAvailable(moduleArr[k]);
                        }
                    } else {
                        menuavailable = true;
                    }
                    if (!menuavailable || !"Y".equals(showMenu)) continue;
                    currRow = roleMatrix.addRow();
                    String link = menuItems.getProperty("link");
                    String pageName = "";
                    if (link.indexOf("command=page") > -1) {
                        pageName = ConfigReportContent.getPageName(link);
                        roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menulink: " + menuName + " ( Page: " + pageName + " )");
                    } else if (link.indexOf("command=wizard") > -1) {
                        roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menulink: " + menuName + " ( Wizard:" + ConfigReportContent.getWizardName(link) + " )");
                    } else if (link.indexOf("command=file") > -1) {
                        roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menulink: " + menuName + " ( File:" + ConfigReportContent.getFileName(link) + " )");
                    } else {
                        roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menulink: " + menuName + " ( URL not valid )");
                    }
                    String menurolelist = menuItems.getAttribute("rolelist");
                    if (menurolelist != null && menurolelist.length() > 0) {
                        String[] roleArr = StringUtil.split(menurolelist, ";");
                        for (int k = 0; k < roleArr.length; ++k) {
                            String includeImg = "<img src=\"../images/WEB-CORE/images/gif/Confirm.gif\" alt=\"" + roleArr[k] + "\" title=\"" + roleArr[k] + "\">";
                            roleMatrix.setString(currRow, roleArr[k], includeImg);
                        }
                    }
                    if (pageName == null || pageName.length() <= 0) continue;
                    ArrayList<SDI> sdiList = new ArrayList<SDI>();
                    SDI sdi = new SDI("WebPage", pageName, ConfigReportContent.getPageEdition(this.connection, pageName), "");
                    sdiList.add(sdi);
                    WebPageRO webpageRO = currRO.getWebPageRO();
                    webpageRO.setSDIList(sdiList);
                    webpageRO.startChapter();
                    WebPageRenderer webpageRenderer = new WebPageRenderer();
                    webpageRenderer.initialize(this.sapphireConnection, this.config, (BaseRO)webpageRO, this.sdisIncluded, false, true);
                    try {
                        webpageRO.nextSection();
                        webpageRO.startSection();
                        DataSet buttonRoles = webpageRenderer.getCurrentPageButtonRoles(webpageRO);
                        if (buttonRoles == null) continue;
                        String[] cols = buttonRoles.getColumns();
                        for (int buttonrole = 0; buttonrole < buttonRoles.getRowCount(); ++buttonrole) {
                            currRow = roleMatrix.addRow();
                            for (int colnum = 0; colnum < cols.length; ++colnum) {
                                if (cols[colnum].equals("Button/Operation")) {
                                    roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + buttonRoles.getString(buttonrole, cols[colnum]));
                                    continue;
                                }
                                roleMatrix.setString(currRow, cols[colnum], buttonRoles.getString(buttonrole, cols[colnum]));
                            }
                        }
                        continue;
                    }
                    catch (SapphireException e) {
                        this.logger.error("Error in role matrix creation", e);
                    }
                }
            }
        }
        return roleMatrix;
    }

    public void createXMLReport() throws SapphireException {
        FileOutputStream xmlFile;
        FileOutputStream sdiXMLFile;
        if (this.menuSystemRO == null) {
            return;
        }
        String xmlReportContent = this.menuSystemRO.getMenuDetailsXML().toXMLString();
        String xmlFileName = ConfigReportContent.generateSectionXMLFileName("Menu", "System");
        try {
            sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report xml file " + xmlFileName);
        }
        try {
            sdiXMLFile.write(xmlReportContent.getBytes(StandardCharsets.UTF_8));
            sdiXMLFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create report file");
        }
        String smpbrmFileName = ConfigReportContent.generateSectionXMLFileName("MenuDriven", "PageButtonRoleMatrix");
        xmlReportContent = this.getRawLayoutRoleMatrix(this.menuSystemRO).toXML();
        try {
            xmlFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + smpbrmFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report xml file " + xmlFileName);
        }
        try {
            xmlFile.write(xmlReportContent.getBytes(StandardCharsets.UTF_8));
            xmlFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create report file");
        }
    }

    private String wrapStatus(String input, String s) {
        if ("D".equals(s)) {
            return ConfigReportContent.getDeletedString(input);
        }
        if ("N".equals(s)) {
            return ConfigReportContent.getNewString(input);
        }
        return input;
    }

    private ConfigReportContent renderRoleMatrixFromRaw(DataSet raw) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "generic layout role matrix");
        DataSet formatted = new DataSet();
        formatted.setColidCaseSensitive(true);
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("Type", "Sitemap");
        DataSet sitemaps = raw.getFilteredDataSet(filter);
        String[] columns = raw.getColumns();
        for (int s = 0; s < sitemaps.getRowCount(); ++s) {
            int currSite = formatted.addRow();
            formatted.setString(currSite, "Item", "Menu: " + this.wrapStatus(sitemaps.getString(s, "Name"), sitemaps.getString(s, "__status", "")));
            HashMap<String, String> tab = new HashMap<String, String>();
            tab.put("Type", "Tab");
            tab.put("Menu", sitemaps.getString(s, "Name"));
            DataSet tabs = raw.getFilteredDataSet(tab);
            for (int t = 0; t < tabs.getRowCount(); ++t) {
                int currTab = formatted.addRow();
                formatted.setString(currTab, "Item", "&nbsp;&nbsp;&nbsp;Menuitem: " + this.wrapStatus(tabs.getString(t, "Name"), tabs.getString(t, "__status", "")));
                for (int col = 0; col < columns.length; ++col) {
                    String currCol = columns[col];
                    if (currCol.equals("Type") || currCol.equals("Name") || currCol.equals("Menu") || currCol.equals("Menuitem") || currCol.equals("Menulink") || currCol.equals("Button") || currCol.equals("Link")) continue;
                    formatted.setString(currTab, currCol, tabs.getString(t, currCol));
                }
                HashMap<String, String> menu = new HashMap<String, String>();
                menu.put("Type", "Menu");
                menu.put("Menu", sitemaps.getString(s, "Name"));
                menu.put("Menuitem", tabs.getString(t, "Name"));
                DataSet menus = raw.getFilteredDataSet(menu);
                for (int m = 0; m < menus.getRowCount(); ++m) {
                    int currMenu = formatted.addRow();
                    String link = menus.getString(m, "Link", "");
                    String menuName = menus.getString(m, "Name", "");
                    menuName = this.wrapStatus(menuName, menus.getString(m, "__status", ""));
                    if (link.indexOf("command=page") > -1) {
                        String pageName = ConfigReportContent.getPageName(link);
                        formatted.setString(currMenu, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menulink: " + menuName + " ( Page: " + pageName + " )");
                    } else if (link.indexOf("command=wizard") > -1) {
                        formatted.setString(currMenu, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menulink: " + menuName + " ( Wizard:" + ConfigReportContent.getWizardName(link) + " )");
                    } else if (link.indexOf("command=file") > -1) {
                        formatted.setString(currMenu, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menulink: " + menuName + " ( File:" + ConfigReportContent.getFileName(link) + " )");
                    } else {
                        formatted.setString(currMenu, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menulink: " + menuName + " ( URL not valid )");
                    }
                    for (int col = 0; col < columns.length; ++col) {
                        String currCol = columns[col];
                        if (currCol.equals("Type") || currCol.equals("Name") || currCol.equals("Menu") || currCol.equals("Menuitem") || currCol.equals("Menulink") || currCol.equals("Button") || currCol.equals("Link")) continue;
                        formatted.setString(currMenu, currCol, menus.getString(m, currCol));
                    }
                    HashMap<String, String> button = new HashMap<String, String>();
                    button.put("Type", "Button");
                    button.put("Menu", sitemaps.getString(s, "Name"));
                    button.put("Menuitem", tabs.getString(t, "Name"));
                    button.put("Menulink", menus.getString(m, "Name"));
                    DataSet buttons = raw.getFilteredDataSet(button);
                    for (int b = 0; b < buttons.getRowCount(); ++b) {
                        int currButton = formatted.addRow();
                        formatted.setString(currButton, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + this.wrapStatus(buttons.getString(b, "Name", ""), buttons.getString(b, "__status", "")));
                        for (int col = 0; col < columns.length; ++col) {
                            String currCol = columns[col];
                            if (currCol.equals("Type") || currCol.equals("Name") || currCol.equals("Menu") || currCol.equals("Menuitem") || currCol.equals("Menulink") || currCol.equals("Button") || currCol.equals("Link")) continue;
                            formatted.setString(currButton, currCol, buttons.getString(b, currCol));
                        }
                    }
                }
            }
        }
        configReportContent.renderRoleMatrix(formatted, 1);
        return configReportContent;
    }

    public void renderMenuCard(ConfigReportContent content, DataSet listItems) {
        if (listItems != null && listItems.getRowCount() > 0) {
            int i;
            content.append("<TABLE width=\"100%\"  style=\"border: 1px solid #333; border-radius: 10px 10px 10px 10px\">\n");
            int rows = listItems.getRowCount();
            int cols = listItems.getColumnCount();
            String[] colList = listItems.getColumns();
            content.startHeader();
            for (i = 0; i < colList.length; ++i) {
                if (colList[i].startsWith("__")) continue;
                String modTitle = colList[i];
                if (colList[i].length() > 1) {
                    modTitle = colList[i].substring(0, 1).toUpperCase() + colList[i].substring(1);
                }
                String str = "<th width=250px style=\"background: gainsboro;border: 1px solid #333; border-radius: 10px 10px 0px 0px;  font: Arial;\n    font-size: 8pt;\n    padding: 0;\n    border-spacing: 0;\n    border-collapse: collapse\" >" + modTitle + "</th>\n";
                content.append(str);
            }
            content.endHeader();
            for (i = 0; i < rows; ++i) {
                content.append("<TR VALIGN=TOP  style=\"border: 1px solid #333; border-radius: 0px 0px 0px 0px;padding:0;border-spacing: 0;border-collapse: collapse\" >\n");
                for (int j = 0; j < cols; ++j) {
                    if (colList[j].startsWith("__")) continue;
                    String columnValue = listItems.getValue(i, colList[j]);
                    if (i != rows - 1) {
                        content.append("<td align=\"center\" width=250px  style=\"border: 1px solid #333; border-radius: 0px 0px 0px 0px;padding:0;border-spacing: 0;border-collapse: collapse\" >\n");
                    } else {
                        content.append("<td align=\"center\" width=250px style=\"border: 1px solid #333; border-radius: 0px 0px 10px 10px;padding:0;border-spacing: 0;border-collapse: collapse\" >\n");
                    }
                    content.append(columnValue);
                    content.append("</td>\n");
                }
                content.append("</TR>\n");
            }
            content.append("</TABLE>\n");
        } else {
            content.append("<P>No entries.");
        }
    }
}

