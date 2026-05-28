/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.misc;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.BaseRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.sdc.WebPageRenderer;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.GenericLayoutRO;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

public class GenericLayoutRenderer
extends BaseRenderer {
    private HashMap colors;
    private ArrayList sitemaps;
    private GenericLayoutRO genericLayoutRO;
    private GenericLayoutRO refGenericLayoutRO;
    private boolean sitemapPageButtonRoleMatrixIncluded;
    private PropertyList config;

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO ro, HashMap sdisIncluded, boolean includeSitemapPageButtonRoleMatrix) {
        super.initialize(sapphireConnection, config, ro, sdisIncluded);
        this.genericLayoutRO = (GenericLayoutRO)ro;
        this.sitemapPageButtonRoleMatrixIncluded = includeSitemapPageButtonRoleMatrix;
        this.colors = new HashMap();
        this.colors.put("lightblue", "6A5ACD");
        this.colors.put("darkblue", "4682B4");
        this.colors.put("green", "008000");
        this.colors.put("brown", "9A3F48");
        this.colors.put("lightgreen", "000000");
        this.colors.put("pink", "C71585");
        this.colors.put("red", "DC143C");
        this.colors.put("yellow", "FFA500");
        try {
            this.sitemaps = this.genericLayoutRO.getSitemapList();
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/gif/Confirm.gif"), new File(this.folder + "/images/WEB-CORE/images/gif/Confirm.gif"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
        this.config = config;
    }

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO srcro, BaseRO refRO, HashMap sdisIncluded, boolean includeDiffReport, boolean includeSiteMapPageButtonRoleMatrix) {
        super.initialize(sapphireConnection, config, srcro, refRO, sdisIncluded, includeDiffReport);
        this.genericLayoutRO = (GenericLayoutRO)srcro;
        this.refGenericLayoutRO = (GenericLayoutRO)refRO;
        this.sitemapPageButtonRoleMatrixIncluded = includeSiteMapPageButtonRoleMatrix;
        this.colors = new HashMap();
        this.colors.put("lightblue", "6A5ACD");
        this.colors.put("darkblue", "4682B4");
        this.colors.put("green", "008000");
        this.colors.put("brown", "9A3F48");
        this.colors.put("lightgreen", "000000");
        this.colors.put("pink", "C71585");
        this.colors.put("red", "DC143C");
        this.colors.put("yellow", "FFA500");
        try {
            this.sitemaps = this.genericLayoutRO.getSitemapList();
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
            sectionList = this.genericLayoutRO.getSitemapList();
            if (this.sitemapPageButtonRoleMatrixIncluded) {
                sectionList.add("Sitemap Page Button Role Matrix");
            }
        } else {
            if (this.genericLayoutRO != null) {
                sectionList = this.genericLayoutRO.getSitemapList();
            }
            ArrayList refSectionList = new ArrayList();
            if (this.refGenericLayoutRO != null) {
                refSectionList = this.refGenericLayoutRO.getSitemapList();
            }
            sectionList = this.mergeSectionLists(sectionList, refSectionList);
            if (this.sitemapPageButtonRoleMatrixIncluded) {
                sectionList.add("Sitemap Page Button Role Matrix");
            }
        }
        return sectionList;
    }

    @Override
    public boolean hasChapterChanged() {
        return this.chapterChanged;
    }

    public void reportNoFrames(String chapterNo, OutputStream reportStream) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Generic Layout chapter");
        configReportContent.startChapter(chapterNo, "Generic Layout", "");
        for (int i = 0; i < this.sitemaps.size(); ++i) {
            String currentSiteMap = this.sitemaps.get(i).toString();
            configReportContent.startSection(currentSiteMap);
            this.renderSiteMapSummary(configReportContent, currentSiteMap);
            if (this.includeDiffReport && this.refGenericLayoutRO != null) {
                this.renderDiffSiteMapDetails(configReportContent, currentSiteMap);
            } else {
                this.renderSiteMapDetails(configReportContent, currentSiteMap);
            }
            configReportContent.endSection();
            configReportContent.pageBreak();
            this.updateSectionChangeInfo("Generic Layout", ConfigReportContent.generateSectionTitle(currentSiteMap), configReportContent);
        }
        if (this.sitemapPageButtonRoleMatrixIncluded) {
            configReportContent.clearContent();
            configReportContent.startSection("Sitemap Page Button Role Matrix");
            configReportContent.appendSubSection(this.renderFullRoleMatrix(), "SiteMap Page Button Role Matrix", this.diffOnly);
            configReportContent.endSection();
            this.updateSectionChangeInfo("Generic Layout", ConfigReportContent.generateSectionTitle("Sitemap Page Button Role Matrix"), configReportContent);
            configReportContent.pageBreak();
        }
        configReportContent.endChapter(chapterNo);
        try {
            reportStream.write(configReportContent.toString().getBytes());
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    public void reportWithFrames(String chapterNo) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "generic layout");
        ArrayList sitemaps = this.genericLayoutRO.getSitemapList();
        for (int i = 0; i < sitemaps.size(); ++i) {
            FileOutputStream sitemapFile;
            String currentSiteMap = sitemaps.get(i).toString();
            String sectionFileName = ConfigReportContent.generateSectionFileName("Generic Layout", currentSiteMap);
            try {
                sitemapFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report file " + sectionFileName);
            }
            configReportContent.clearContent();
            configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("Generic Layout", currentSiteMap));
            configReportContent.startSection(currentSiteMap);
            this.renderSiteMapSummary(configReportContent, currentSiteMap);
            if (this.includeDiffReport && this.refGenericLayoutRO != null) {
                this.renderDiffSiteMapDetails(configReportContent, currentSiteMap);
                configReportContent.insertDiffAnchors();
            } else {
                this.renderSiteMapDetails(configReportContent, currentSiteMap);
            }
            configReportContent.endSection();
            this.updateSectionChangeInfo("Generic Layout", ConfigReportContent.generateSectionTitle(currentSiteMap), configReportContent);
            this.createSubSectionInfo("Generic Layout", ConfigReportContent.generateSectionTitle(currentSiteMap), configReportContent.diffInfo);
            configReportContent.endFile();
            try {
                sitemapFile.write(configReportContent.toString().getBytes());
                sitemapFile.close();
                continue;
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
        if (this.sitemapPageButtonRoleMatrixIncluded) {
            FileOutputStream rolematrixFile;
            String sectionFileName = ConfigReportContent.generateSectionFileName("Generic Layout", "Sitemap Page Button Role Matrix");
            try {
                rolematrixFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report file " + sectionFileName);
            }
            configReportContent.clearContent();
            configReportContent.startFile(ConfigReportContent.generateSubSectionFileName("Generic Layout", "Sitemap Page Button Role Matrix"));
            configReportContent.startSection("Sitemap Page Button Role Matrix");
            configReportContent.appendSubSection(this.renderFullRoleMatrix(), "SiteMap Page Button Role Matrix", this.diffOnly);
            configReportContent.insertDiffAnchors();
            configReportContent.endSection();
            this.updateSectionChangeInfo("Generic Layout", ConfigReportContent.generateSectionTitle("Sitemap Page Button Role Matrix"), configReportContent);
            configReportContent.endFile();
            this.createSubSectionInfo("Generic Layout", ConfigReportContent.generateSectionTitle("Sitemap Page Button Role Matrix"), configReportContent.diffInfo);
            try {
                rolematrixFile.write(configReportContent.toString().getBytes());
                rolematrixFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
    }

    public ConfigReportContent renderFullRoleMatrix() throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "generic layout role matrix");
        if (this.includeDiffReport && this.refGenericLayoutRO != null) {
            DataSet rolematrix = this.getRawLayoutRoleMatrix(this.genericLayoutRO);
            DataSet refRoleMatrix = this.getRawLayoutRoleMatrix(this.refGenericLayoutRO);
            String[] keycols = new String[]{"Type", "Name", "Sitemap", "Tab", "Menu"};
            DataSet diff = configReportContent.getMenuMatrixDiffInfo(rolematrix, refRoleMatrix, keycols);
            configReportContent.appendSubSection(this.renderRoleMatrixFromRaw(diff), "SiteMap Page Button Role Matrix", this.diffOnly);
        } else {
            configReportContent.renderRoleMatrix(this.getLayoutRoleMatrix(this.genericLayoutRO), 1);
        }
        return configReportContent;
    }

    public ConfigReportContent renderTabInfo(PropertyList tabPropertyList) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "generic layout tab");
        String roles = tabPropertyList.getAttribute("rolelist");
        String modules = tabPropertyList.getAttribute("modulelist");
        String link = tabPropertyList.getProperty("link");
        boolean available = false;
        if (modules != null && modules.length() > 0) {
            String[] moduleArr = StringUtil.split(modules, ";");
            for (int i = 0; i < moduleArr.length; ++i) {
                available |= this.genericLayoutRO.checkModuleAvailable(moduleArr[i]);
            }
        } else {
            available = true;
        }
        if (available) {
            configReportContent.startTable();
            if (roles.length() > 0) {
                configReportContent.startRow();
                configReportContent.addRowItem("Tramline Available to roles", roles.replaceAll(";", ", "), this.getTranslationProcessor());
                configReportContent.endRow();
            }
            configReportContent.startRow();
            configReportContent.addRowItem("Default link", ConfigReportContent.renderLink(link, this.sdisIncluded, this.frames, this.connection), this.getTranslationProcessor());
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
                    available |= this.genericLayoutRO.checkModuleAvailable(moduleArr[modulenum]);
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

    public void renderSiteMapSummary(ConfigReportContent configReportContent, String sitemap) throws SapphireException {
        PropertyList sitemapPl = this.genericLayoutRO.getSitemapDetails(sitemap);
        PropertyList refSitemapPl = null;
        DataSet ds = this.getTabsRoleMatrix(sitemapPl);
        DataSet refDs = null;
        if (this.includeDiffReport && this.refGenericLayoutRO != null && (refSitemapPl = this.refGenericLayoutRO.getSitemapDetails(sitemap)) != null) {
            refDs = this.getTabsRoleMatrix(refSitemapPl);
        }
        ConfigReportContent roleMatrix = new ConfigReportContent(this.config, "Tabs Summary");
        roleMatrix.startSubSection(sitemap + " Tabs Summary", sitemap + " sitemap has the following tabs/tramlines <P>");
        if (refDs != null) {
            String[] keycols = new String[]{"Tab"};
            roleMatrix.renderDiffRoleMatrix(ds, refDs, keycols);
        } else {
            roleMatrix.renderRoleMatrix(ds, 2);
        }
        configReportContent.appendSubSection(roleMatrix, sitemap + " Tabs Summary", this.diffOnly);
    }

    private DataSet getTabsRoleMatrix(PropertyList currSiteMap) throws SapphireException {
        if (currSiteMap == null) {
            throw new SapphireException("sitemap is null");
        }
        PropertyListCollection tabs = currSiteMap.getCollectionNotNull("tabs");
        SortedSet roles = this.getAllRoles(tabs);
        DataSet ds = new DataSet();
        ds.setColidCaseSensitive(true);
        ds.addColumn("Tab", 0);
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
                    available |= this.genericLayoutRO.checkModuleAvailable(moduleArr[modulenum]);
                }
            } else {
                available = true;
            }
            if (!available || "N".equals(showTab)) continue;
            int currRow = ds.addRow();
            String anchor = ConfigReportContent.convertToID(tabName);
            String hyperlink = "<A HREF=#" + anchor + ">" + tabName + "</A>";
            ds.setString(currRow, "Tab", hyperlink);
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

    public void renderSiteMapDetails(ConfigReportContent configReportContent, String sitemap) throws SapphireException {
        PropertyList sitemapPl = this.genericLayoutRO.getSitemapDetails(sitemap);
        PropertyList refSitemapPl = null;
        configReportContent.append("<P>The following sections enumerate the menu items (tram stops) for each of the tabs(tramlines) in the sitemap: " + sitemap + "<P>");
        PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
        if (this.includeDiffReport && this.refGenericLayoutRO != null) {
            refSitemapPl = this.refGenericLayoutRO.getSitemapDetails(sitemap);
            PropertyListCollection propertyListCollection = refSitemapPl.getCollectionNotNull("tabs");
        }
        for (int i = 0; i < tabs.size(); ++i) {
            PropertyList currPL = tabs.getPropertyList(i);
            String tabName = currPL.getProperty("text", currPL.getId());
            String showTab = currPL.getProperty("show", "Y");
            ConfigReportContent tabContent = new ConfigReportContent(this.config, tabName);
            if (!"N".equals(showTab)) {
                String anchor = ConfigReportContent.convertToID(tabName);
                tabContent.startSubHeading(tabName, "", anchor);
                tabContent.appendSubSection(this.renderTramLine(currPL), tabName, this.diffOnly);
                PropertyListCollection menus = currPL.getCollectionNotNull("menus");
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
                            available |= this.genericLayoutRO.checkModuleAvailable(moduleArr[k]);
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
                tabContent.renderRoleMatrix(ds, 2);
            }
            configReportContent.appendSubSection(tabContent, tabName, this.diffOnly);
        }
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

    public void renderDiffSiteMapDetails(ConfigReportContent configReportContent, String sitemap) throws SapphireException {
        PropertyList sitemapPl = this.genericLayoutRO.getSitemapDetails(sitemap);
        PropertyList refSitemapPl = null;
        configReportContent.append("<P>The following sections enumerate the menu items (tram stops) for each of the tabs(tramlines) in the sitemap: " + sitemap + "<P>");
        PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
        PropertyListCollection reftabs = null;
        if (this.includeDiffReport && this.refGenericLayoutRO != null) {
            refSitemapPl = this.refGenericLayoutRO.getSitemapDetails(sitemap);
            reftabs = refSitemapPl.getCollectionNotNull("tabs");
        }
        for (int i = 0; i < tabs.size(); ++i) {
            PropertyList currPL = tabs.getPropertyList(i);
            String tabName = currPL.getProperty("text", currPL.getId());
            String showTab = currPL.getProperty("show", "Y");
            PropertyList refPL = this.findRefTabProps(tabName, reftabs);
            ConfigReportContent tabContent = new ConfigReportContent(this.config, tabName);
            if (!"N".equals(showTab)) {
                tabContent.startSubSection(tabName, "");
                if (refPL != null && !"N".equals(refPL.getProperty("show", "Y"))) {
                    tabContent.appendSubSection(this.renderDiffTramLine(currPL, refPL), tabName, this.diffOnly);
                } else {
                    tabContent.appendSubSection(this.renderTramLine(currPL), tabName, this.diffOnly);
                }
                DataSet ds = this.getMenusRoleMatrix(this.genericLayoutRO, currPL);
                DataSet refDs = this.getMenusRoleMatrix(this.refGenericLayoutRO, refPL);
                String[] keycols = new String[]{"Menu", "Link"};
                tabContent.renderDiffRoleMatrix(ds, refDs, keycols);
            }
            configReportContent.appendSubSection(tabContent, tabName, this.diffOnly);
        }
    }

    private DataSet getMenusRoleMatrix(GenericLayoutRO ro, PropertyList currTabPl) throws SapphireException {
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
                    available |= this.genericLayoutRO.checkModuleAvailable(moduleArr[k]);
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

    private ConfigReportContent renderTramLine(PropertyList tab) throws SapphireException {
        String tabName = tab.getProperty("text", tab.getId());
        ConfigReportContent buffer = new ConfigReportContent(this.config, "generic layout tab:" + tabName);
        String code = tab.getProperty("colorcode");
        String color = (String)this.colors.get(code);
        int topHeight = 10;
        int startWidth = 94;
        int startHeight = 56;
        int spaceWidth = 10;
        int textWidth = 84;
        int textHeight = 40;
        int terminusWidth = 80;
        int terminusHeight = 37;
        int rightEndWidth = 10;
        int tramlinePos = 21;
        int terminusPos = 16;
        PropertyListCollection menus = tab.getCollectionNotNull("menus");
        buffer.append("<table border=0 cellspacing=20 cellpadding=0 id=\"bodytable\" width=\"100%\"> ");
        buffer.append("<tr id=\"bodyrow\"> ");
        buffer.append("<td id=\"bodycell\" class=\"bodycell\" valign=top> ");
        buffer.append("<table border=0 cellpadding=\"0\" cellspacing=\"0\" id=\"maintable\" style=\"table-layout:auto;\" > ");
        buffer.append("<tr height=\"" + topHeight + "\" id=\"toprow\"> ");
        buffer.append("<td style=\"width:" + spaceWidth + "px\"></td> ");
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/title_top.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/title_top.jpg"));
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        buffer.append("<td valign=\"top\" style=\"width:" + startWidth + "px;background-image: url('../images/WEB-OPAL/pagetypes/sitemap/tramline/images/title_top.jpg');background-position:center center;background-repeat:no-repeat;\" id=\"topcell\" > ");
        buffer.append("</td>");
        buffer.append("<td></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=2 style=\"height:100%;\"> ");
        buffer.append("<table cellpadding=\"0\" cellspacing=\"0\" style=\"height:100%;table-layout:fixed;\" width=\"100%\" border=0> ");
        buffer.append("<tr height=\"" + startHeight + "\">");
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendleft.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendleft.jpg"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/title.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/title.jpg"));
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        buffer.append("<td valign=\"top\" style=\"width:" + spaceWidth + "px;background-image: url('../images/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendleft.jpg');background-position:center center;background-repeat:no-repeat;\" > ");
        buffer.append("</td>");
        buffer.append("<td valign=\"top\" align=\"center\" style=\"padding-top:5px;width: " + startWidth + "px; height:" + startHeight + "px; color:" + color + "; background-image: url('images/WEB-OPAL/pagetypes/sitemap/tramline/images/title.jpg');background-repeat:no-repeat;background-position:center center;\"> ");
        buffer.append("<div height=\"" + textHeight + "\" style=\"vertical-align:middle;width:" + textWidth + "px;height:" + textHeight + "px;overflow-y:hidden;\"> ");
        buffer.append("<table cellpadding=\"0\" cellspacing=\"0\" height=\"100%\" width=\"100%\" border=0 style=\"table-layout:fixed;\"> ");
        buffer.append("<tr>");
        buffer.append("<td valign=\"middle\" align=\"center\" > ");
        buffer.append(tabName);
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</div>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"width:" + spaceWidth + "px;\"></td> ");
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/title_pipe.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/title_pipe.jpg"));
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        buffer.append("<td valign=\"top\" style=\"width:" + startWidth + "px; height:auto;background-image: url('../images/WEB-OPAL/pagetypes/sitemap/tramline/images/title_pipe.jpg');background-repeat:repeat-y;background-position:top center;\">&nbsp;</td> ");
        buffer.append("</tr>");
        buffer.append("</table> ");
        buffer.append("</td>");
        buffer.append("<td style=\"height:100%;border:solid 0 red;width:100%;\" nowrap >");
        buffer.append("<table cellpadding=\"0\" border=0 cellspacing=\"0\" style=\"height:100%;table-layout:fixed;\" id=\"contenttable0\"> ");
        buffer.append("<tr>");
        int itemcount = 0;
        for (int curritem = 0; curritem < menus.size(); ++curritem) {
            String nodeId;
            String alias = nodeId = "tramline0_node" + itemcount;
            PropertyList menuItems = menus.getPropertyList(curritem);
            if (!"Y".equals(menuItems.getProperty("show", "Y"))) continue;
            String menuName = menuItems.getProperty("text", menuItems.getId());
            try {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg"));
            }
            catch (Exception e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            buffer.append("<td valign=\"top\" align=\"center\" width=" + terminusWidth + " style=\"background-image: url( '../images/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg' );background-repeat:repeat-x;background-position:0 " + tramlinePos + "\">\n");
            buffer.append("<table id=" + nodeId + itemcount + " title=\"" + menuName + "\"  cellpadding=\"0\" cellspacing=\"0\" class=\"sitemap_TramStopNorm\" style=\"table-layout:fixed;height:100%;\" border=0 >\n ");
            buffer.append("<tr style=\"height:" + terminusHeight + "px\">\n");
            try {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg"));
            }
            catch (Exception e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            buffer.append("<td  align=center valign=bottom style=\"background-image: url( '../images/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg' );background-repeat:no-repeat;background-position:center " + terminusPos + "\">\n");
            buffer.append("</td>\n");
            buffer.append("</tr>\n");
            buffer.append("<tr>\n");
            buffer.append("<td valign=top align=center>\n");
            String link = menuItems.getProperty("link");
            String pageName = ConfigReportContent.getPageName(link);
            if (pageName.length() > 0 && ConfigReportContent.isFKIncluded("WebPage", pageName, this.sdisIncluded)) {
                String href;
                SDI sdi = new SDI("WebPage", pageName, ConfigReportContent.getPageEdition(this.connection, pageName), "");
                String anchor = ConfigReportContent.generateSDISectionAnchor(sdi);
                if (!this.frames) {
                    href = "#" + anchor;
                } else {
                    String sectionFileName = ConfigReportContent.generateSDISectionFileName(sdi);
                    href = sectionFileName + "#" + anchor;
                }
                buffer.append("<A HREF=\"" + href + "\">" + menuName + "</A>");
            } else {
                buffer.append(menuName);
            }
            buffer.append("</td>");
            buffer.append("</tr>");
            buffer.append("</table>");
            buffer.append("</td>");
            ++itemcount;
        }
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendright.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendright.jpg"));
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        buffer.append("<td valign=\"top\" align=\"center\" style=\"width:" + rightEndWidth + "px;background-image: url( '../images/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendright.jpg' );background-repeat:no-repeat;background-position:0 " + tramlinePos + ";\">");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        return buffer;
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

    private ConfigReportContent renderDiffTramLine(PropertyList tab, PropertyList refTab) throws SapphireException {
        String tabName = tab.getProperty("text", tab.getId());
        ConfigReportContent buffer = new ConfigReportContent(this.config, "generic layout tab:" + tabName);
        String code = tab.getProperty("colorcode");
        String color = (String)this.colors.get(code);
        int topHeight = 10;
        int startWidth = 94;
        int startHeight = 56;
        int spaceWidth = 10;
        int textWidth = 84;
        int textHeight = 40;
        int terminusWidth = 80;
        int terminusHeight = 37;
        int rightEndWidth = 10;
        int tramlinePos = 21;
        int terminusPos = 16;
        PropertyListCollection menus = tab.getCollectionNotNull("menus");
        PropertyListCollection refMenus = refTab.getCollectionNotNull("menus");
        buffer.append("<table border=0 cellspacing=20 cellpadding=0 id=\"bodytable\" width=\"100%\"> ");
        buffer.append("<tr id=\"bodyrow\"> ");
        buffer.append("<td id=\"bodycell\" class=\"bodycell\" valign=top> ");
        buffer.append("<table border=0 cellpadding=\"0\" cellspacing=\"0\" id=\"maintable\" style=\"table-layout:auto;\" > ");
        buffer.append("<tr height=\"" + topHeight + "\" id=\"toprow\"> ");
        buffer.append("<td style=\"width:" + spaceWidth + "px;\"></td> ");
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/title_top.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/title_top.jpg"));
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        buffer.append("<td valign=\"top\" style=\"width:" + startWidth + "px;background-image: url('../images/WEB-OPAL/pagetypes/sitemap/tramline/images/title_top.jpg');background-position:center center;background-repeat:no-repeat;\" id=\"topcell\" > ");
        buffer.append("</td>");
        buffer.append("<td></td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td colspan=2 style=\"height:100%;\"> ");
        buffer.append("<table cellpadding=\"0\" cellspacing=\"0\" style=\"height:100%;table-layout:fixed;\" width=\"100%\" border=0> ");
        buffer.append("<tr height=\"" + startHeight + "\">");
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendleft.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendleft.jpg"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/title.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/title.jpg"));
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        buffer.append("<td valign=\"top\" style=\"width:" + spaceWidth + "px;background-image: url('../images/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendleft.jpg');background-position:center center;background-repeat:no-repeat;\" > ");
        buffer.append("</td>");
        buffer.append("<td valign=\"top\" align=\"center\" style=\"padding-top:5px;width: " + startWidth + "px; height:" + startHeight + "px; color:" + color + "; background-image: url('images/WEB-OPAL/pagetypes/sitemap/tramline/images/title.jpg');background-repeat:no-repeat;background-position:center center;\"> ");
        buffer.append("<div height=\"" + textHeight + "\" style=\"vertical-align:middle;width:" + textWidth + "px;height:" + textHeight + "px;overflow-y:hidden;\"> ");
        buffer.append("<table cellpadding=\"0\" cellspacing=\"0\" height=\"100%\" width=\"100%\" border=0 style=\"table-layout:fixed;\"> ");
        buffer.append("<tr>");
        buffer.append("<td valign=\"middle\" align=\"center\" > ");
        buffer.append(tabName);
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</div>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("<tr>");
        buffer.append("<td style=\"width:" + spaceWidth + "px;\"></td> ");
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/title_pipe.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/title_pipe.jpg"));
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        buffer.append("<td valign=\"top\" style=\"width:" + startWidth + "px; height:auto;background-image: url('../images/WEB-OPAL/pagetypes/sitemap/tramline/images/title_pipe.jpg');background-repeat:repeat-y;background-position:top center;\">&nbsp;</td> ");
        buffer.append("</tr>");
        buffer.append("</table> ");
        buffer.append("</td>");
        buffer.append("<td style=\"height:100%;border:solid 0 red;width:100%;\" nowrap >");
        buffer.append("<table cellpadding=\"0\" border=0 cellspacing=\"0\" style=\"height:100%;table-layout:fixed;\" id=\"contenttable0\"> ");
        buffer.append("<tr>");
        int itemcount = 0;
        for (int curritem = 0; curritem < menus.size(); ++curritem) {
            String nodeId = "tramline0_node" + itemcount;
            PropertyList menuItems = menus.getPropertyList(curritem);
            if (!"Y".equals(menuItems.getProperty("show", "Y"))) continue;
            String menuName = menuItems.getProperty("text", menuItems.getId());
            PropertyList refMenuItemProps = this.findMenuItem(menuName, refMenus);
            try {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg"));
            }
            catch (Exception e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            buffer.append("<td valign=\"top\" align=\"center\" width=" + terminusWidth + " style=\"background-image: url( '../images/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg' );background-repeat:repeat-x;background-position:0 " + tramlinePos + "\">\n");
            buffer.append("<table id=" + nodeId + curritem + " title=\"" + menuName + "\"  cellpadding=\"0\" cellspacing=\"0\" class=\"sitemap_TramStopNorm\" style=\"table-layout:fixed;height:100%;\" border=0 >\n ");
            buffer.append("<tr style=\"height:" + terminusHeight + "px\">\n");
            try {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg"));
            }
            catch (Exception e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            buffer.append("<td  align=center valign=bottom style=\"background-image: url( '../images/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg' );background-repeat:no-repeat;background-position:center " + terminusPos + "\">\n");
            buffer.append("</td>\n");
            buffer.append("</tr>\n");
            buffer.append("<tr>\n");
            buffer.append("<td valign=top align=center>\n");
            String link = menuItems.getProperty("link");
            String pageName = ConfigReportContent.getPageName(link);
            if (pageName.length() > 0 && ConfigReportContent.isFKIncluded("WebPage", pageName, this.sdisIncluded)) {
                String href;
                SDI sdi = new SDI("WebPage", pageName, ConfigReportContent.getPageEdition(this.connection, pageName), "");
                String anchor = ConfigReportContent.generateSDISectionAnchor(sdi);
                if (!this.frames) {
                    href = "#" + anchor;
                } else {
                    String sectionFileName = ConfigReportContent.generateSDISectionFileName(sdi);
                    href = sectionFileName + "#" + anchor;
                }
                if (refMenuItemProps != null && "Y".equals(refMenuItemProps.getProperty("show", "Y"))) {
                    String refLink = refMenuItemProps.getProperty("link");
                    String refPageName = ConfigReportContent.getPageName(refLink);
                    if (pageName.equals(refPageName)) {
                        buffer.append("<A HREF=\"" + href + "\">" + menuName + "</A>");
                    } else {
                        String chg = " ( Link changed )";
                        buffer.append("<A HREF=\"" + href + "\">" + menuName + chg + "</A>");
                    }
                } else {
                    buffer.append("<A HREF=\"" + href + "\">" + ConfigReportContent.getNewString(menuName) + "</A>");
                }
            } else if (refMenuItemProps != null) {
                buffer.append(menuName);
            } else {
                buffer.append(ConfigReportContent.getNewString(menuName));
            }
            buffer.append("</td>");
            buffer.append("</tr>");
            buffer.append("</table>");
            buffer.append("</td>");
            ++itemcount;
        }
        int deletedcount = 0;
        for (int k = 0; k < refMenus.size(); ++k) {
            String nodeId = "tramline0_node" + (itemcount + deletedcount);
            PropertyList refMenuItems = refMenus.getPropertyList(k);
            if (!"Y".equals(refMenuItems.getProperty("show", "Y"))) continue;
            String refMenuName = refMenuItems.getProperty("text", refMenuItems.getId());
            PropertyList origMenuItemProps = this.findMenuItem(refMenuName, menus);
            try {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg"));
            }
            catch (Exception e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            if (origMenuItemProps != null && "Y".equals(origMenuItemProps.getProperty("show", "Y"))) continue;
            buffer.append("<td valign=\"top\" align=\"center\" width=" + terminusWidth + " style=\"background-image: url( '../images/WEB-OPAL/pagetypes/sitemap/tramline/images/track.jpg' );background-repeat:repeat-x;background-position:0 " + tramlinePos + "\">\n");
            buffer.append("<table id=" + nodeId + k + deletedcount + " title=\"" + refMenuName + "\"  cellpadding=\"0\" cellspacing=\"0\" class=\"sitemap_TramStopNorm\" style=\"table-layout:fixed;height:100%;\" border=0 >\n ");
            buffer.append("<tr style=\"height:" + terminusHeight + "px\">\n");
            try {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg"));
            }
            catch (Exception e) {
                throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
            }
            buffer.append("<td  align=center valign=bottom style=\"background-image: url( '../images/WEB-OPAL/pagetypes/sitemap/tramline/images/terminus_" + code + ".jpg' );background-repeat:no-repeat;background-position:center " + terminusPos + "\">\n");
            buffer.append("</td>\n");
            buffer.append("</tr>\n");
            buffer.append("<tr>\n");
            buffer.append("<td valign=top align=center>\n");
            buffer.append(ConfigReportContent.getDeletedString(refMenuName));
            buffer.append("</td>");
            buffer.append("</tr>");
            buffer.append("</table>");
            buffer.append("</td>");
            ++deletedcount;
        }
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendright.jpg"), new File(this.folder + "/images/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendright.jpg"));
        }
        catch (Exception e) {
            throw new SapphireException(ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())));
        }
        buffer.append("<td valign=\"top\" align=\"center\" style=\"width:" + rightEndWidth + "px;background-image: url( '../images/WEB-OPAL/pagetypes/sitemap/tramline/images/trackendright.jpg' );background-repeat:no-repeat;background-position:0 " + tramlinePos + ";\">");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        buffer.append("</td>");
        buffer.append("</tr>");
        buffer.append("</table>");
        return buffer;
    }

    public DataSet getRawLayoutRoleMatrix(GenericLayoutRO ro) throws SapphireException {
        if (ro.dataSource.equals("XMLREPORT")) {
            return ro.getLayoutRoleMatrixFromXMLReport();
        }
        DataSet roleMatrix = new DataSet();
        roleMatrix.setColidCaseSensitive(true);
        roleMatrix.addColumn("Type", 0);
        roleMatrix.addColumn("Name", 0);
        roleMatrix.addColumn("Sitemap", 0);
        roleMatrix.addColumn("Tab", 0);
        roleMatrix.addColumn("Menu", 0);
        roleMatrix.addColumn("Button", 0);
        ArrayList sitemaps = ro.getSitemapList();
        for (int i = 0; i < sitemaps.size(); ++i) {
            String sitemap = (String)sitemaps.get(i);
            PropertyList sitemapPl = ro.getSitemapDetails(sitemap);
            PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
            int currRow = roleMatrix.addRow();
            roleMatrix.setString(currRow, "Name", sitemap);
            roleMatrix.setString(currRow, "Type", "Sitemap");
            roleMatrix.setString(currRow, "Sitemap", "N/A");
            roleMatrix.setString(currRow, "Tab", "N/A");
            roleMatrix.setString(currRow, "Menu", "N/A");
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
                        available |= this.genericLayoutRO.checkModuleAvailable(moduleArr[modulenum]);
                    }
                } else {
                    available = true;
                }
                if (!available || "N".equals(showTab)) continue;
                currRow = roleMatrix.addRow();
                roleMatrix.setString(currRow, "Name", tabName);
                roleMatrix.setString(currRow, "Type", "Tab");
                roleMatrix.setString(currRow, "Sitemap", sitemap);
                roleMatrix.setString(currRow, "Tab", "N/A");
                roleMatrix.setString(currRow, "Menu", "N/A");
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
                            menuavailable |= this.genericLayoutRO.checkModuleAvailable(moduleArr[k]);
                        }
                    } else {
                        menuavailable = true;
                    }
                    if (!menuavailable || !"Y".equals(showMenu)) continue;
                    currRow = roleMatrix.addRow();
                    String link = menuItems.getProperty("link");
                    String pageName = ConfigReportContent.getPageName(link);
                    roleMatrix.setString(currRow, "Type", "Menu");
                    roleMatrix.setString(currRow, "Sitemap", sitemap);
                    roleMatrix.setString(currRow, "Tab", tabName);
                    roleMatrix.setString(currRow, "Name", menuName);
                    roleMatrix.setString(currRow, "Menu", "N/A");
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
                            roleMatrix.setString(currRow, "Sitemap", sitemap);
                            roleMatrix.setString(currRow, "Tab", tabName);
                            roleMatrix.setString(currRow, "Menu", menuName);
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

    public DataSet getLayoutRoleMatrix(GenericLayoutRO currRO) throws SapphireException {
        if (currRO.dataSource.equals("XMLREPORT")) {
            return currRO.getLayoutRoleMatrixFromXMLReport();
        }
        DataSet roleMatrix = new DataSet();
        roleMatrix.setColidCaseSensitive(true);
        roleMatrix.addColumn("Item", 0);
        ArrayList sitemaps = currRO.getSitemapList();
        for (int i = 0; i < sitemaps.size(); ++i) {
            String sitemap = (String)sitemaps.get(i);
            PropertyList sitemapPl = currRO.getSitemapDetails(sitemap);
            PropertyListCollection tabs = sitemapPl.getCollectionNotNull("tabs");
            int currRow = roleMatrix.addRow();
            roleMatrix.setString(currRow, "Item", "Sitemap: " + sitemap);
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
                        available |= this.genericLayoutRO.checkModuleAvailable(moduleArr[modulenum]);
                    }
                } else {
                    available = true;
                }
                if (!available || "N".equals(showTab)) continue;
                currRow = roleMatrix.addRow();
                roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;Tab: " + tabName);
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
                            menuavailable |= this.genericLayoutRO.checkModuleAvailable(moduleArr[k]);
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
                        roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menu: " + menuName + " ( Page: " + pageName + " )");
                    } else if (link.indexOf("command=wizard") > -1) {
                        roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menu: " + menuName + " ( Wizard:" + ConfigReportContent.getWizardName(link) + " )");
                    } else if (link.indexOf("command=file") > -1) {
                        roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menu: " + menuName + " ( File:" + ConfigReportContent.getFileName(link) + " )");
                    } else {
                        roleMatrix.setString(currRow, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menu: " + menuName + " ( URL not valid )");
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
        if (this.genericLayoutRO == null) {
            return;
        }
        String xmlReportContent = this.genericLayoutRO.getGenericLayout().toXMLString();
        String xmlFileName = ConfigReportContent.generateSectionXMLFileName("Generic", "Layout");
        try {
            sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report xml file " + xmlFileName);
        }
        try {
            sdiXMLFile.write(xmlReportContent.getBytes());
            sdiXMLFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create report file");
        }
        ArrayList siteMaps = this.genericLayoutRO.getSitemapList();
        for (int i = 0; i < siteMaps.size(); ++i) {
            FileOutputStream detailsXMLFile;
            String currSiteMap = siteMaps.get(i).toString();
            xmlFileName = ConfigReportContent.generateSectionXMLFileName("GenericLayout", currSiteMap);
            PropertyList siteMapDetails = this.genericLayoutRO.getSitemapDetails(currSiteMap);
            try {
                detailsXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlFileName);
            }
            try {
                detailsXMLFile.write(siteMapDetails.toXMLString().getBytes());
                detailsXMLFile.close();
                continue;
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a profilepros file");
            }
        }
        String smpbrmFileName = ConfigReportContent.generateSectionXMLFileName("GenericLayout", "PageButtonRoleMatrix");
        xmlReportContent = this.getRawLayoutRoleMatrix(this.genericLayoutRO).toXML();
        try {
            xmlFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + smpbrmFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report xml file " + xmlFileName);
        }
        try {
            xmlFile.write(xmlReportContent.getBytes());
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
            formatted.setString(currSite, "Item", "Sitemap: " + this.wrapStatus(sitemaps.getString(s, "Name"), sitemaps.getString(s, "__status", "")));
            HashMap<String, String> tab = new HashMap<String, String>();
            tab.put("Type", "Tab");
            tab.put("Sitemap", sitemaps.getString(s, "Name"));
            DataSet tabs = raw.getFilteredDataSet(tab);
            for (int t = 0; t < tabs.getRowCount(); ++t) {
                int currTab = formatted.addRow();
                formatted.setString(currTab, "Item", "&nbsp;&nbsp;&nbsp;Tab: " + this.wrapStatus(tabs.getString(t, "Name"), tabs.getString(t, "__status", "")));
                for (int col = 0; col < columns.length; ++col) {
                    String currCol = columns[col];
                    if (currCol.equals("Type") || currCol.equals("Name") || currCol.equals("Sitemap") || currCol.equals("Tab") || currCol.equals("Menu") || currCol.equals("Button") || currCol.equals("Link")) continue;
                    formatted.setString(currTab, currCol, tabs.getString(t, currCol));
                }
                HashMap<String, String> menu = new HashMap<String, String>();
                menu.put("Type", "Menu");
                menu.put("Sitemap", sitemaps.getString(s, "Name"));
                menu.put("Tab", tabs.getString(t, "Name"));
                DataSet menus = raw.getFilteredDataSet(menu);
                for (int m = 0; m < menus.getRowCount(); ++m) {
                    int currMenu = formatted.addRow();
                    String link = menus.getString(m, "Link", "");
                    String menuName = menus.getString(m, "Name", "");
                    menuName = this.wrapStatus(menuName, menus.getString(m, "__status", ""));
                    if (link.indexOf("command=page") > -1) {
                        String pageName = ConfigReportContent.getPageName(link);
                        formatted.setString(currMenu, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menu: " + menuName + " ( Page: " + pageName + " )");
                    } else if (link.indexOf("command=wizard") > -1) {
                        formatted.setString(currMenu, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menu: " + menuName + " ( Wizard:" + ConfigReportContent.getWizardName(link) + " )");
                    } else if (link.indexOf("command=file") > -1) {
                        formatted.setString(currMenu, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menu: " + menuName + " ( File:" + ConfigReportContent.getFileName(link) + " )");
                    } else {
                        formatted.setString(currMenu, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Menu: " + menuName + " ( URL not valid )");
                    }
                    for (int col = 0; col < columns.length; ++col) {
                        String currCol = columns[col];
                        if (currCol.equals("Type") || currCol.equals("Name") || currCol.equals("Sitemap") || currCol.equals("Tab") || currCol.equals("Menu") || currCol.equals("Button") || currCol.equals("Link")) continue;
                        formatted.setString(currMenu, currCol, menus.getString(m, currCol));
                    }
                    HashMap<String, String> button = new HashMap<String, String>();
                    button.put("Type", "Button");
                    button.put("Sitemap", sitemaps.getString(s, "Name"));
                    button.put("Tab", tabs.getString(t, "Name"));
                    button.put("Menu", menus.getString(m, "Name"));
                    DataSet buttons = raw.getFilteredDataSet(button);
                    for (int b = 0; b < buttons.getRowCount(); ++b) {
                        int currButton = formatted.addRow();
                        formatted.setString(currButton, "Item", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + this.wrapStatus(buttons.getString(b, "Name", ""), buttons.getString(b, "__status", "")));
                        for (int col = 0; col < columns.length; ++col) {
                            String currCol = columns[col];
                            if (currCol.equals("Type") || currCol.equals("Name") || currCol.equals("Sitemap") || currCol.equals("Tab") || currCol.equals("Menu") || currCol.equals("Button") || currCol.equals("Link")) continue;
                            formatted.setString(currButton, currCol, buttons.getString(b, currCol));
                        }
                    }
                }
            }
        }
        configReportContent.renderRoleMatrix(formatted, 1);
        return configReportContent;
    }
}

