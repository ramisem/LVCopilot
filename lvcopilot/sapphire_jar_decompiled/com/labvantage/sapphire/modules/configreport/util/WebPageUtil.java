/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.cmt.view.SDISnapshotViewer;
import com.labvantage.sapphire.modules.configreport.renderer.webpage.GenericPageRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.webpage.MaintenanceFormPageRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.webpage.MaintenanceListPageRenderer;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import java.io.File;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class WebPageUtil
extends BaseSDCRenderer {
    public ConfigReportContent renderWebPageSummary(WebPageRO webpageRO, WebPageRO refWebpageRO, String webpageId, String productEdition, String desc, boolean reportAdvancedProperties, boolean reportHiddenColumns, String screenshotfolder, boolean hideEmptyColumns) {
        ConfigReportContent screenshot;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "WebPage summary: " + webpageId);
        WebPageRO currRO = null;
        currRO = webpageRO == null || webpageRO.currentSDI == null ? refWebpageRO : webpageRO;
        if (refWebpageRO == null) {
            refWebpageRO = webpageRO;
        }
        if ((screenshot = this.renderScreenShot(webpageId, screenshotfolder)).length() > 0) {
            configReportContent.appendSubSection(screenshot, webpageId + " ScreenShot", this.diffOnly);
        }
        configReportContent.startSubSection("Web Page Info", "");
        configReportContent.appendSubSection(this.renderWebPageInfo(webpageRO.currentSDIData, refWebpageRO.currentSDIData, webpageId, productEdition, hideEmptyColumns), "Web Page Info", this.diffOnly);
        if ("MaintenanceList".equals(currRO.getPageType())) {
            MaintenanceListPageRenderer m = new MaintenanceListPageRenderer(webpageRO, refWebpageRO, this.includeDiffReport);
            m.initialize(this.config, this.sdisIncluded);
            configReportContent.appendSpecialContent(m.render(reportAdvancedProperties, this.reportCategories, reportHiddenColumns, this.includeDiffReport), this.diffOnly);
        } else if ("MaintenanceForm".equals(currRO.getPageType())) {
            MaintenanceFormPageRenderer m = new MaintenanceFormPageRenderer(webpageRO, refWebpageRO, this.includeDiffReport);
            m.initialize(this.config, this.sdisIncluded);
            configReportContent.appendSpecialContent(m.render(reportAdvancedProperties, this.reportCategories, reportHiddenColumns, this.includeDiffReport), this.diffOnly);
        } else {
            GenericPageRenderer m = new GenericPageRenderer(webpageRO, refWebpageRO, this.includeDiffReport);
            m.initialize(this.config, this.sdisIncluded);
            configReportContent.appendSpecialContent(m.render(reportAdvancedProperties, this.reportCategories, reportHiddenColumns, this.includeDiffReport), this.diffOnly);
        }
        return configReportContent;
    }

    public ConfigReportContent renderWebPageSummaryNewMode(WebPageRO webpageRO, WebPageRO refWebpageRO, String webpageId, String productEdition, String desc, boolean reportAdvancedProperties, boolean reportHiddenColumns, String screenshotfolder, boolean hideEmptyColumns, boolean includeInheritedProperties) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "WebPage summary: " + webpageId);
        ConfigReportContent screenshot = this.renderScreenShot(webpageId, screenshotfolder);
        if (screenshot.length() > 0) {
            configReportContent.appendSubSection(screenshot, webpageId + " ScreenShot", this.diffOnly);
        }
        configReportContent.startSubSection("Web Page Info", "");
        if (webpageRO == null) {
            webpageRO = new WebPageRO();
        }
        configReportContent.appendSubSection(this.renderWebPageInfo(webpageRO.currentSDIData, refWebpageRO.currentSDIData, webpageId, productEdition, hideEmptyColumns), "Web Page Info", this.diffOnly);
        if (refWebpageRO.currentSDI == null) {
            configReportContent.appendSpecialContent(this.renderWebPagePropertyTree(webpageId, webpageRO.getDataSet("webpagepropertytree"), !includeInheritedProperties, true, this.getTranslationProcessor()));
        } else if (webpageRO.currentSDI == null) {
            configReportContent.appendSpecialContent(this.renderWebPagePropertyTree(webpageId, refWebpageRO.getDataSet("webpagepropertytree"), !includeInheritedProperties, true, this.getTranslationProcessor()));
        } else {
            configReportContent.appendSpecialContent(this.renderWebPagePropertyTreeDiff(webpageId, webpageRO.getDataSet("webpagepropertytree"), refWebpageRO.getDataSet("webpagepropertytree"), !includeInheritedProperties, true, this.getTranslationProcessor()));
        }
        configReportContent.startSubSection("Categories", "");
        ConfigReportContent categories = new ConfigReportContent(this.config, "categories");
        if (!this.includeDiffReport) {
            if (webpageRO != null) {
                categories.renderCategories(webpageRO.getCategories());
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            }
        } else if (webpageRO == null || webpageRO.currentSDI == null) {
            if (refWebpageRO != null) {
                categories.renderCategories(refWebpageRO.getCategories());
            }
            configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
        } else if (refWebpageRO == null || refWebpageRO.currentSDI == null) {
            categories.renderCategories(webpageRO.getCategories());
            configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
        } else {
            categories.renderCategoriesDiff(webpageRO.getCategories(), refWebpageRO.getCategories());
            configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
        }
        return configReportContent;
    }

    public ConfigReportContent renderWebPageDetails(WebPageRO webpageRO, WebPageRO refWebpageRO, String webpageId, String productEdition, String desc, boolean reportAdvancedProperties) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "WebPage details: " + webpageId);
        GenericPageRenderer m = new GenericPageRenderer(webpageRO, refWebpageRO, this.includeDiffReport);
        m.initialize(this.config, this.sdisIncluded);
        configReportContent.appendSpecialContent(m.renderAllElementsDetails(reportAdvancedProperties, this.includeDiffReport), this.diffOnly);
        return configReportContent;
    }

    public ConfigReportContent renderScreenShot(String webpageid, String screenshotsFolder) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "WebPage screen shot: " + webpageid);
        String screenshotfilejpg = screenshotsFolder + "/" + webpageid + ".jpg";
        String screenshotfilebmp = screenshotsFolder + "/" + webpageid + ".bmp";
        String screenshotfilepng = screenshotsFolder + "/" + webpageid + ".png";
        boolean included = this.insertImageFileIfExists(webpageid, configReportContent, screenshotfilejpg);
        if (!included) {
            included = this.insertImageFileIfExists(webpageid, configReportContent, screenshotfilepng);
        }
        if (!included) {
            included = this.insertImageFileIfExists(webpageid, configReportContent, screenshotfilebmp);
        }
        return configReportContent;
    }

    public boolean insertImageFileIfExists(String webpageid, ConfigReportContent configReportContent, String screenshotfile) {
        if (new File(screenshotfile).exists()) {
            configReportContent.startSubHeading("Example Screen Shot", "");
            try {
                ConfigReportContent.copyFile(new File(screenshotfile), new File(this.folder + "/images/" + webpageid + ".jpg"));
            }
            catch (Exception e) {
                Trace.log("Failed to copy screenshotfile:" + e.getMessage());
            }
            configReportContent.append("<img src=\"../images/" + webpageid + ".jpg\">");
            configReportContent.append("<P><P>");
            return true;
        }
        return false;
    }

    public ConfigReportContent renderWebPageInfo(SDIData srcSDIData, SDIData refSDIData, String webpageId, String webpageEdition, boolean hideEmptyColumns) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "WebPage Info:" + webpageId);
        configReportContent.startTable();
        if (refSDIData == null) {
            configReportContent.startRow();
            configReportContent.addRowItem("Page ID", webpageId);
            configReportContent.addRowItem("Product Edition", webpageEdition);
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Description", WebPageUtil.getPrimaryValue(srcSDIData, "webpagedesc"), 3);
            configReportContent.endRow();
            configReportContent.startRow();
            if (WebPageUtil.getPrimaryValue(srcSDIData, "filename").length() > 0 || !hideEmptyColumns) {
                configReportContent.addRowItem("Filename", WebPageUtil.getPrimaryValue(srcSDIData, "filename"));
                configReportContent.addRowItem("Page Type", WebPageRO.getPageType(srcSDIData));
            } else {
                configReportContent.addRowItem("Page Type", WebPageRO.getPageType(srcSDIData), 3);
            }
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Virtual Page", WebPageRO.getIsVirtualPage(srcSDIData));
            configReportContent.addRowItem("WebPage Type", WebPageRO.getWebPageType(srcSDIData));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Express Page", WebPageRO.getIsExpressPage(srcSDIData));
            configReportContent.endRow();
        } else if (srcSDIData == null) {
            configReportContent.startRow();
            configReportContent.addRowItem("Page ID", WebPageUtil.getPrimaryValue(refSDIData, "webpageid"));
            configReportContent.addRowItem("Product Edition", WebPageUtil.getPrimaryValue(refSDIData, "productedition"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Description", WebPageUtil.getPrimaryValue(refSDIData, "webpagedesc"), 3);
            configReportContent.endRow();
            configReportContent.startRow();
            if (WebPageUtil.getPrimaryValue(refSDIData, "filename").length() > 0 || !hideEmptyColumns) {
                configReportContent.addRowItem("Filename", WebPageUtil.getPrimaryValue(refSDIData, "filename"));
                configReportContent.addRowItem("Page Type", WebPageRO.getPageType(refSDIData));
            } else {
                configReportContent.addRowItem("Page Type", WebPageRO.getPageType(refSDIData), 3);
            }
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Virtual Page", WebPageRO.getIsVirtualPage(refSDIData));
            configReportContent.addRowItem("WebPage Type", WebPageRO.getWebPageType(refSDIData));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Express Page", WebPageRO.getIsExpressPage(refSDIData));
            configReportContent.endRow();
        } else {
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Page ID", WebPageUtil.getPrimaryValue(srcSDIData, "webpageid"), WebPageUtil.getPrimaryValue(refSDIData, "webpageid"));
            configReportContent.addDiffRowItem("Product Edition", WebPageUtil.getPrimaryValue(srcSDIData, "productedition"), WebPageUtil.getPrimaryValue(refSDIData, "productedition"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Description", WebPageUtil.getPrimaryValue(srcSDIData, "webpagedesc"), WebPageUtil.getPrimaryValue(refSDIData, "webpagedesc"), 3, this.getTranslationProcessor());
            configReportContent.endRow();
            configReportContent.startRow();
            if (WebPageUtil.getPrimaryValue(srcSDIData, "filename").length() > 0 || WebPageUtil.getPrimaryValue(refSDIData, "filename").length() > 0 || !hideEmptyColumns) {
                configReportContent.addDiffRowItem("Filename", WebPageUtil.getPrimaryValue(srcSDIData, "filename"), WebPageUtil.getPrimaryValue(refSDIData, "filename"));
                configReportContent.addDiffRowItem("Page Type", WebPageRO.getPageType(srcSDIData), WebPageRO.getPageType(refSDIData));
            } else {
                configReportContent.addDiffRowItem("Page Type", WebPageRO.getPageType(srcSDIData), WebPageRO.getPageType(refSDIData), 3, this.getTranslationProcessor());
            }
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Virtual Page", WebPageRO.getIsVirtualPage(srcSDIData), WebPageRO.getIsVirtualPage(refSDIData));
            configReportContent.addDiffRowItem("WebPage Type", WebPageRO.getWebPageType(srcSDIData), WebPageRO.getWebPageType(refSDIData));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Express Page", WebPageRO.getIsExpressPage(srcSDIData), WebPageRO.getIsExpressPage(refSDIData));
            configReportContent.endRow();
        }
        configReportContent.endTable();
        return configReportContent;
    }

    public ConfigReportContent renderWebPagePropertyTreeDiff(String webpageid, DataSet src, DataSet ref, boolean hideInheritedProperties, boolean hideEmptyColumns, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("WebPage", translationProcessor);
        for (int i = 0; i < src.getRowCount(); ++i) {
            ConfigReportContent plContent;
            String nodeid;
            ConfigReportContent subsection;
            String element;
            block9: {
                String configCompCode;
                element = src.getValue(i, "elementid");
                HashMap<String, PropertyList> srcProps = SDISnapshotViewer.getOverridingPropertyTrees(src, i);
                String sourceextendednodeid = src.getValue(i, "extendnodeid");
                String refextendednodeid = "";
                if (element.length() <= 0) continue;
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("elementid", element);
                HashMap refProps = new PropertyList();
                int row = ref.findRow(filter);
                if (row != -1) {
                    refextendednodeid = ref.getValue(row, "extendnodeid");
                    refProps = SDISnapshotViewer.getOverridingPropertyTrees(ref, row);
                }
                subsection = new ConfigReportContent(element, translationProcessor);
                String extendendednodediffstring = ConfigReportContent.getDiffString(sourceextendednodeid, refextendednodeid);
                String sectiontitle = "Element: " + element + ", PropertyTree: " + src.getValue(i, "propertytreeid", "");
                subsection.startSubSection(sectiontitle, "Extends Node: " + extendendednodediffstring);
                nodeid = "webpagepropertytree_" + i;
                String propertytreeid = src.getValue(i, "propertytreeid", "");
                plContent = new ConfigReportContent("", translationProcessor);
                ConfigurationProcessor config = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
                boolean isDevMode = "Y".equals(config.getSysConfigProperty("devmode"));
                boolean isCompMode = false;
                String string = configCompCode = isDevMode ? "" : config.getSysConfigProperty("compcode");
                if (configCompCode.length() > 0) {
                    String compcode = new QueryProcessor(this.getConnectionId()).getPreparedSqlDataSet("SELECT compcode FROM webpage WHERE webpageid=?", (Object[])new String[]{webpageid}).getValue(0, "compcode");
                    isCompMode = compcode.equals(configCompCode);
                }
                try {
                    plContent = SDISnapshotViewer.renderOverriddenPropertyList(isDevMode, isCompMode, this.sapphireConnection.getConnectionId(), sectiontitle, propertytreeid, sourceextendednodeid, refextendednodeid, srcProps, refProps, hideInheritedProperties, this.getTranslationProcessor(), true);
                }
                catch (Exception e) {
                    PropertyList p = srcProps.get("component");
                    if (!p.isEmpty()) {
                        plContent.appendSpecialContent(plContent.renderPropertyListDiff(p, (PropertyList)refProps.get("component"), true, this.getTranslationProcessor()));
                    }
                    if (!(p = srcProps.get("product")).isEmpty()) {
                        plContent.appendSpecialContent(plContent.renderPropertyListDiff(p, (PropertyList)refProps.get("product"), true, this.getTranslationProcessor()));
                    }
                    if ((p = srcProps.get("custom")).isEmpty()) break block9;
                    plContent.appendSpecialContent(plContent.renderPropertyListDiff(p, (PropertyList)refProps.get("custom"), true, this.getTranslationProcessor()));
                }
            }
            if (plContent.length() > 0) {
                subsection.appendNodeContent(plContent, nodeid, element);
            } else {
                plContent.append("No properties");
                subsection.appendNodeContent(plContent, nodeid, element);
            }
            configReportContent.appendNodeContent(subsection, element, element);
        }
        return configReportContent;
    }

    public ConfigReportContent renderWebPagePropertyTree(String webpageid, DataSet src, boolean hideInheritedProperties, boolean hideEmptyColumns, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent("WebPage", translationProcessor);
        for (int i = 0; i < src.getRowCount(); ++i) {
            ConfigReportContent plContent;
            String nodeid;
            ConfigReportContent subsection;
            String element;
            block8: {
                String configCompCode;
                element = src.getValue(i, "elementid");
                HashMap<String, PropertyList> srcProps = SDISnapshotViewer.getOverridingPropertyTrees(src, i);
                String sourceextendednodeid = src.getValue(i, "extendnodeid");
                if (element.length() <= 0) continue;
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("elementid", element);
                subsection = new ConfigReportContent(element, translationProcessor);
                String sectiontitle = "Element: " + element + ", PropertyTree: " + src.getValue(i, "propertytreeid", "");
                subsection.startSubSection(sectiontitle, "Extends Node: " + sourceextendednodeid);
                nodeid = "webpagepropertytree_" + i;
                String propertytreeid = src.getValue(i, "propertytreeid", "");
                plContent = new ConfigReportContent("", translationProcessor);
                ConfigurationProcessor config = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
                boolean isDevMode = "Y".equals(config.getSysConfigProperty("devmode"));
                boolean isCompMode = false;
                String string = configCompCode = isDevMode ? "" : config.getSysConfigProperty("compcode");
                if (configCompCode.length() > 0) {
                    String compcode = new QueryProcessor(this.getConnectionId()).getPreparedSqlDataSet("SELECT compcode FROM webpage WHERE webpageid=?", (Object[])new String[]{webpageid}).getValue(0, "compcode");
                    isCompMode = compcode.equals(configCompCode);
                }
                try {
                    plContent = SDISnapshotViewer.renderOverriddenPropertyList(isDevMode, isCompMode, this.sapphireConnection.getConnectionId(), sectiontitle, propertytreeid, sourceextendednodeid, sourceextendednodeid, srcProps, srcProps, hideInheritedProperties, this.getTranslationProcessor(), true);
                }
                catch (Exception e) {
                    PropertyList p = srcProps.get("component");
                    if (!p.isEmpty()) {
                        plContent.appendSpecialContent(plContent.renderPropertyList(p, true, this.getTranslationProcessor()));
                    }
                    if (!(p = srcProps.get("product")).isEmpty()) {
                        plContent.appendSpecialContent(plContent.renderPropertyList(p, true, this.getTranslationProcessor()));
                    }
                    if ((p = srcProps.get("custom")).isEmpty()) break block8;
                    plContent.appendSpecialContent(plContent.renderPropertyList(p, true, this.getTranslationProcessor()));
                }
            }
            if (plContent.length() > 0) {
                subsection.appendNodeContent(plContent, nodeid, element);
            } else {
                plContent.append("No properties");
                subsection.appendNodeContent(plContent, nodeid, element);
            }
            configReportContent.appendNodeContent(subsection, element, element);
        }
        return configReportContent;
    }
}

