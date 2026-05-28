/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.webpage;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.webpage.GenericPageRenderer;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import sapphire.SapphireException;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MaintenanceFormPageRenderer
extends GenericPageRenderer {
    public MaintenanceFormPageRenderer(WebPageRO webpageRO, WebPageRO refWebpageRO) {
        super(webpageRO, refWebpageRO, false);
    }

    public MaintenanceFormPageRenderer(WebPageRO webpageRO, WebPageRO refWebpageRO, boolean includeDiffReport) {
        super(webpageRO, refWebpageRO, includeDiffReport);
    }

    @Override
    public ConfigReportContent render(boolean reportAdvancedProperties, boolean reportWebPageCategories, boolean reportHiddenColumns, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "MaintenanceForm");
        boolean subsection = true;
        try {
            configReportContent.startSubSection("PageType Information", "Page type definition for this web page is:");
            configReportContent.appendSubSection(this.renderPageTypeInfo(reportAdvancedProperties, includeDiffReport), "PageType Information", this.diffOnly);
            configReportContent.endSubSection("", "PageType Information");
            configReportContent.appendSubSection(this.renderElement("maint", reportAdvancedProperties, reportHiddenColumns, includeDiffReport), "Maint", this.diffOnly);
            configReportContent.appendSubSection(this.renderElement("advancedtoolbar", reportAdvancedProperties, reportHiddenColumns, includeDiffReport), "Advanced Toolbar", this.diffOnly);
            configReportContent.appendSpecialContent(this.renderOtherElements("maint,advancedtoolbar,category", reportAdvancedProperties, reportHiddenColumns, includeDiffReport), this.diffOnly);
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
        }
        catch (SapphireException e) {
            Trace.logError("Failed to create report " + e.getMessage());
        }
        return configReportContent;
    }

    public ConfigReportContent renderPageTypeInfo(boolean renderAdvancedProperties, boolean includeDiffReport) throws SapphireException {
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
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addRowItem("Style", pageTypeInfo.getProperty("maintstyle", "empty "));
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyListCollection postSaveActionColl = pageTypeInfo.getCollectionNotNull("actions");
            configReportContent.addRowItem("Post Save Action", this.getPostSaveActionInfo(postSaveActionColl));
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyList postSaveActionProperties = pageTypeInfo.getPropertyList("postsaveactionprops");
            if (postSaveActionProperties != null) {
                ConfigReportContent value = new ConfigReportContent(this.config, "Page Type Info:");
                value.startTableInner();
                value.startRow();
                value.addRowItem("Success Message", postSaveActionProperties.getProperty("successmsg", ""));
                value.endRow();
                value.startRow();
                value.addRowItem("Fail Message", postSaveActionProperties.getProperty("failmsg", ""));
                value.endRow();
                value.endTable();
                configReportContent.addRowItem("Post Save Action Properties", value.toString());
                configReportContent.endRow();
            }
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyListCollection coll = pageTypeInfo.getCollectionNotNull("includes");
            DataSet incls = new DataSet();
            if (coll != null) {
                for (int s = 0; s < coll.size(); ++s) {
                    PropertyList pl = coll.getPropertyList(s);
                    if (pl == null) continue;
                    Object[] keyes = pl.keySet().toArray();
                    int currRow = incls.addRow();
                    for (int j = 0; j < keyes.length; ++j) {
                        incls.setString(currRow, keyes[j].toString(), pl.getProperty(keyes[j].toString(), ""));
                    }
                }
                ConfigReportContent subbuffer = new ConfigReportContent(this.config, "Page Type Info:");
                subbuffer.renderListTable(incls, this.translationProcessor);
                configReportContent.addRowItem("Includes", subbuffer.toString());
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
            PropertyList refPageTypeInfo = null;
            refPageTypeInfo = this.refWebpageRO.getPageTypeInfo();
            configReportContent.startTable();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Title", pageTypeInfo.getProperty("title", "empty"), refPageTypeInfo.getProperty("title", "empty"));
            configReportContent.endRow();
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Style", pageTypeInfo.getProperty("maintstyle", "empty "), refPageTypeInfo.getProperty("maintstyle", "empty "));
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyListCollection postSaveActionColl = pageTypeInfo.getCollectionNotNull("actions");
            PropertyListCollection refPostSaveActionColl = refPageTypeInfo.getCollectionNotNull("actions");
            ConfigReportContent st = configReportContent.renderCollectionDiff(postSaveActionColl, refPostSaveActionColl, false, this.translationProcessor, true);
            configReportContent.addRowItem("Post Save Action", st.toString());
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyList postSaveActionProperties = pageTypeInfo.getPropertyList("postsaveactionprops");
            PropertyList refPostSaveActionProperties = refPageTypeInfo.getPropertyList("postsaveactionprops");
            ConfigReportContent value = configReportContent.renderPropertyListDiff(postSaveActionProperties, refPostSaveActionProperties, false, this.translationProcessor);
            configReportContent.addRowItem("Post Save Action Properties", value.toString());
            configReportContent.endRow();
            configReportContent.startRow();
            PropertyListCollection coll = pageTypeInfo.getCollectionNotNull("includes");
            PropertyListCollection refColl = refPageTypeInfo.getCollectionNotNull("includes");
            ConfigReportContent buff = configReportContent.renderCollectionDiff(coll, refColl, false, this.translationProcessor, true);
            configReportContent.addRowItem("Includes", buff.toString());
            configReportContent.endRow();
            configReportContent.endTable();
        }
        catch (SapphireException e) {
            configReportContent.append("<P>Failed to get pagetype information");
        }
        return configReportContent;
    }

    private String getPostSaveActionInfo(PropertyListCollection coll) {
        ConfigReportContent buffer = new ConfigReportContent(this.config, "Page Type Info");
        Object[] list = coll.toArray();
        buffer.startTableInner();
        for (int i = 0; i < list.length; ++i) {
            buffer.startRow();
            PropertyList pl = (PropertyList)list[i];
            String actionName = pl.getProperty("actionid", "");
            String version = pl.getProperty("versionid", "");
            String async = pl.getProperty("asynchronous", "");
            ConfigReportContent value = new ConfigReportContent(this.config, "Page Type Info");
            value.startTableInner();
            value.startRow();
            value.addRowItem("Action", actionName);
            value.endRow();
            value.startRow();
            value.addRowItem("Version", version);
            value.endRow();
            value.startRow();
            value.addRowItem("Asynchronous", async);
            value.endRow();
            value.endTable();
            buffer.addRowItem(pl.getProperty("id", pl.getId()), value.toString());
            buffer.endRow();
        }
        buffer.endTable();
        return buffer.toString();
    }
}

