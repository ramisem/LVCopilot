/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.webpage;

import com.labvantage.sapphire.modules.configreport.renderer.webpage.GenericPageRenderer;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintenanceListPageRenderer
extends GenericPageRenderer {
    public MaintenanceListPageRenderer(WebPageRO webpageRO, WebPageRO refWebpageRO, boolean includeDiffReport) {
        super(webpageRO, refWebpageRO, includeDiffReport);
    }

    public MaintenanceListPageRenderer(WebPageRO webpageRO, WebPageRO refWebpageRO) {
        super(webpageRO, refWebpageRO, false);
    }

    @Override
    public ConfigReportContent render(boolean reportAdvancedProperties, boolean reportWebPageCategories, boolean reportHiddenColumns, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "MaintenanceList Page");
        boolean subsection = true;
        configReportContent.startSubSection("PageType Information", "Page type definition for this web page is:");
        configReportContent.appendSubSection(this.renderPageTypeInfo(reportAdvancedProperties, includeDiffReport), "PageType Information", this.diffOnly);
        configReportContent.endSubSection("", "PageType Information");
        configReportContent.appendSubSection(this.renderElement("list", reportAdvancedProperties, reportHiddenColumns, includeDiffReport), "List", this.diffOnly);
        configReportContent.appendSubSection(this.renderElement("advancedtoolbar", reportAdvancedProperties, reportHiddenColumns, includeDiffReport), "Advanced Toolbar", this.diffOnly);
        configReportContent.appendSubSection(this.renderElement("advancedsearch", reportAdvancedProperties, reportHiddenColumns, includeDiffReport), "Advanced Search", this.diffOnly);
        configReportContent.appendSpecialContent(this.renderOtherElements("list,advancedtoolbar,advancedsearch,category", reportAdvancedProperties, reportHiddenColumns, includeDiffReport), this.diffOnly);
        if (reportWebPageCategories) {
            configReportContent.startSubSection("Categories", "");
            ConfigReportContent categories = new ConfigReportContent(this.config, "categories");
            if (!includeDiffReport) {
                if (this.webpageRO != null) {
                    configReportContent.renderCategories(this.webpageRO.getCategories());
                }
            } else if (this.webpageRO == null || this.webpageRO.currentSDI == null) {
                if (this.refWebpageRO != null) {
                    categories.renderCategories(this.refWebpageRO.getCategories());
                }
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            } else if (this.refWebpageRO == null || this.refWebpageRO.currentSDI == null) {
                categories.renderCategories(this.webpageRO.getCategories());
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            } else {
                categories.renderCategoriesDiff(this.webpageRO.getCategories(), this.refWebpageRO.getCategories());
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            }
        }
        return configReportContent;
    }

    public ConfigReportContent renderPageTypeInfo(boolean renderAdvancedProperties, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Page Type Info");
        if (includeDiffReport) {
            if (this.webpageRO == null || this.webpageRO.currentSDI == null) {
                configReportContent.appendSpecialContent(this.renderPageTypeInfoForRO(this.refWebpageRO), this.diffOnly);
            } else if (this.refWebpageRO == null || this.refWebpageRO.currentSDI == null) {
                configReportContent.appendSpecialContent(this.renderPageTypeInfoForRO(this.webpageRO), this.diffOnly);
            } else {
                configReportContent.appendSpecialContent(this.renderPageTypeInfoDiff(), this.diffOnly);
            }
        } else {
            configReportContent.appendSpecialContent(this.renderPageTypeInfoForRO(this.webpageRO), this.diffOnly);
        }
        return configReportContent;
    }

    private ConfigReportContent renderPageTypeInfoForRO(WebPageRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Page Type Info:");
        try {
            PropertyList pageTypeInfo = ro.getPageTypeInfo();
            configReportContent.startTable();
            configReportContent.startRow();
            configReportContent.addRowItem("Title", pageTypeInfo.getProperty("title", "empty"));
            configReportContent.addRowItem("Implemented by", pageTypeInfo.getProperty("objectname", "empty "));
            configReportContent.endRow();
            configReportContent.startRow();
            String includeTemplates = "No";
            if ("Y".equals(pageTypeInfo.getProperty("includetemplates", "N"))) {
                includeTemplates = "Yes";
            }
            configReportContent.addRowItem("Include Templates", includeTemplates);
            configReportContent.addRowItem("Restrictive Where", pageTypeInfo.getProperty("restrictivewhere", ""));
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyList vs = pageTypeInfo.getPropertyList("versionstatus");
            if (vs != null) {
                String provisional = vs.getProperty("provisional", "N");
                String current = vs.getProperty("current", "N");
                String expired = vs.getProperty("expired", "N");
                String active = vs.getProperty("active", "N");
                configReportContent.addRowItem("Version Status", "Provisional:" + provisional + "; Current:" + current + "; Expired: " + expired + "; Active:" + active);
            } else {
                configReportContent.addRowItem("Version Status", "");
            }
            configReportContent.addRowItem("Lookup Callback", pageTypeInfo.getProperty("lookupcallback", ""));
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyListCollection coll = pageTypeInfo.getCollectionNotNull("includes");
            ConfigReportContent subBuffer = new ConfigReportContent(this.config, "Page Type Info:");
            if (coll != null) {
                subBuffer.startTableInner();
                for (int s = 0; s < coll.size(); ++s) {
                    PropertyList pl = coll.getPropertyList(s);
                    if (pl == null) continue;
                    Object[] keyes = pl.keySet().toArray();
                    subBuffer.startTableInner();
                    for (int j = 0; j < keyes.length; ++j) {
                        subBuffer.addRowItem(keyes[j].toString(), pl.getProperty(keyes[j].toString(), ""));
                    }
                    subBuffer.endTable();
                }
                subBuffer.endTable();
                configReportContent.addRowItem("Includes", subBuffer.toString());
            } else {
                configReportContent.addRowItem("Includes", "none");
            }
            configReportContent.endRow();
            configReportContent.endTable();
        }
        catch (SapphireException e) {
            configReportContent.append("<P>Failed to get pagetype information");
        }
        return configReportContent;
    }

    private ConfigReportContent renderPageTypeInfoDiff() {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Page Type Info:");
        try {
            PropertyList pageTypeInfo = this.webpageRO.getPageTypeInfo();
            configReportContent.startTable();
            PropertyList refPageTypeInfo = this.refWebpageRO.getPageTypeInfo();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Title", pageTypeInfo.getProperty("title", "empty"), refPageTypeInfo.getProperty("title", "empty"));
            configReportContent.addDiffRowItem("Implemented by", pageTypeInfo.getProperty("objectname", "empty "), refPageTypeInfo.getProperty("objectname", "empty "));
            configReportContent.endRow();
            configReportContent.startRow();
            String includeTemplates = "No";
            if ("Y".equals(pageTypeInfo.getProperty("includetemplates", "N"))) {
                includeTemplates = "Yes";
            }
            String refIncludeTemplates = "No";
            if ("Y".equals(refPageTypeInfo.getProperty("includetemplates", "N"))) {
                refIncludeTemplates = "Yes";
            }
            configReportContent.addDiffRowItem("Include Templates", includeTemplates, refIncludeTemplates);
            configReportContent.addDiffRowItem("Restrictive Where", pageTypeInfo.getProperty("restrictivewhere", ""), refPageTypeInfo.getProperty("restrictivewhere", ""));
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyList vs = pageTypeInfo.getPropertyListNotNull("versionstatus");
            PropertyList refvs = refPageTypeInfo.getPropertyListNotNull("versionstatus");
            String provisional = vs.getProperty("provisional", "N");
            String refProvisional = refvs.getProperty("provisional", "N");
            String current = vs.getProperty("current", "N");
            String refCurrent = refvs.getProperty("current", "N");
            String expired = vs.getProperty("expired", "N");
            String refExpired = refvs.getProperty("expired", "N");
            String active = vs.getProperty("active", "N");
            String refActive = refvs.getProperty("active", "N");
            provisional = ConfigReportContent.getDiffString(provisional, refProvisional);
            current = ConfigReportContent.getDiffString(current, refCurrent);
            expired = ConfigReportContent.getDiffString(expired, refExpired);
            active = ConfigReportContent.getDiffString(active, refActive);
            configReportContent.addRowItem("Version Status", "Provisional:" + provisional + "; Current:" + current + "; Expired: " + expired + "; Active:" + active);
            configReportContent.addDiffRowItem("Lookup Callback", pageTypeInfo.getProperty("lookupcallback", ""), refPageTypeInfo.getProperty("lookupcallback", ""));
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyListCollection coll = pageTypeInfo.getCollectionNotNull("includes");
            PropertyListCollection refColl = refPageTypeInfo.getCollectionNotNull("includes");
            if (coll != null) {
                ConfigReportContent subBuffer = configReportContent.renderCollectionDiff(coll, refColl, false, this.translationProcessor, true);
                configReportContent.addRowItem("Includes", subBuffer.toString());
            } else {
                configReportContent.addRowItem("Includes", "none");
            }
            configReportContent.endRow();
            configReportContent.endTable();
        }
        catch (SapphireException e) {
            configReportContent.append("<P>Failed to get pagetype information");
        }
        return configReportContent;
    }
}

