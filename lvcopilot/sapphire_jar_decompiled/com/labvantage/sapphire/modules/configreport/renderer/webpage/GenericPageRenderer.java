/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.webpage;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.element.BaseElementRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.element.GenericElementRenderer;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import com.labvantage.sapphire.xml.PropertyDefinitionList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.ConfigReportContent;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class GenericPageRenderer {
    protected WebPageRO webpageRO;
    protected WebPageRO refWebpageRO;
    protected String connectionId;
    protected PropertyList config;
    protected HashMap sdisIncluded;
    protected boolean includeDiffReport;
    protected boolean diffOnly = false;
    protected TranslationProcessor translationProcessor;

    public void initialize(PropertyList config, HashMap sdisIncluded) {
        this.config = config;
        this.sdisIncluded = sdisIncluded;
        this.connectionId = config.getProperty("connection");
        this.translationProcessor = new TranslationProcessor(this.connectionId);
        this.diffOnly = "Y".equals(config.getProperty("diffonlyreport", "N"));
    }

    public GenericPageRenderer(WebPageRO webpageRO, WebPageRO refWebpageRO, boolean includeDiffReport) {
        this.webpageRO = webpageRO;
        this.refWebpageRO = refWebpageRO;
        this.includeDiffReport = includeDiffReport;
    }

    public ConfigReportContent render(boolean reportAdvancedProperties, boolean reportWebPageCategories, boolean reportHiddenColumns, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "GenericPage ");
        configReportContent.appendSpecialContent(this.renderOtherElements("category", reportAdvancedProperties, reportHiddenColumns, includeDiffReport));
        if (reportWebPageCategories) {
            configReportContent.startSubSection("Categories", "");
            ConfigReportContent categories = new ConfigReportContent(this.config, "categories");
            if (!includeDiffReport) {
                if (this.webpageRO != null) {
                    categories.renderCategories(this.webpageRO.getCategories());
                    configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
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

    protected ConfigReportContent renderElement(String elementId, boolean reportAdvancedProperties, boolean renderHiddenColumns, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Element:" + elementId);
        if (includeDiffReport) {
            if (this.webpageRO == null || this.webpageRO.currentSDI == null) {
                configReportContent.appendSpecialContent(this.renderElementForRO(elementId, reportAdvancedProperties, renderHiddenColumns, this.refWebpageRO));
            } else if (this.refWebpageRO == null || this.refWebpageRO.currentSDI == null) {
                configReportContent.appendSpecialContent(this.renderElementForRO(elementId, reportAdvancedProperties, renderHiddenColumns, this.webpageRO));
            } else {
                configReportContent.appendSpecialContent(this.renderElementDiff(elementId, reportAdvancedProperties, renderHiddenColumns));
            }
        } else {
            configReportContent.appendSpecialContent(this.renderElementForRO(elementId, reportAdvancedProperties, renderHiddenColumns, this.webpageRO));
        }
        return configReportContent;
    }

    private ConfigReportContent renderElementForRO(String elementId, boolean reportAdvancedProperties, boolean reportHiddenColumns, WebPageRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Element: " + elementId);
        try {
            BaseElementRenderer renderer;
            PropertyList elementProperties = ro.getElementDetails(elementId);
            String propertyTreeId = elementProperties.getProperty("propertytreeid");
            if (propertyTreeId == null || propertyTreeId.length() == 0) {
                return configReportContent;
            }
            PropertyDefinitionList elementPropertyDefinitionList = ro.getElementPropertyDefinitionList(elementId, propertyTreeId);
            String className = "com.labvantage.sapphire.modules.configreport.renderer.element." + propertyTreeId.substring(0, 1).toUpperCase() + propertyTreeId.substring(1) + "ElementRenderer";
            try {
                renderer = (BaseElementRenderer)Class.forName(className).newInstance();
            }
            catch (ClassNotFoundException e) {
                renderer = new GenericElementRenderer();
            }
            renderer.initialize(this.config, this.sdisIncluded);
            configReportContent.appendSpecialContent(renderer.report(elementId, elementProperties, null, elementPropertyDefinitionList, reportAdvancedProperties, reportHiddenColumns, false), this.diffOnly);
        }
        catch (Exception e) {
            Trace.logError("Failed to render element " + elementId + " page:" + ro.currentSDI.getKeyid1() + " : " + e.getMessage());
        }
        return configReportContent;
    }

    private ConfigReportContent renderElementDiff(String elementId, boolean reportAdvancedProperties, boolean reportHiddenColumns) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Element: " + elementId);
        try {
            BaseElementRenderer renderer;
            String propertyTreeId;
            PropertyList elementProperties = this.webpageRO.getElementDetails(elementId);
            PropertyList refElementProperties = null;
            if (this.includeDiffReport && this.refWebpageRO != null && this.refWebpageRO.currentSDI != null) {
                refElementProperties = this.refWebpageRO.getElementDetails(elementId);
            }
            if ((propertyTreeId = elementProperties.getProperty("propertytreeid")) == null || propertyTreeId.length() == 0) {
                return configReportContent;
            }
            PropertyDefinitionList elementPropertyDefinitionList = this.webpageRO.getElementPropertyDefinitionList(elementId, propertyTreeId);
            String className = "com.labvantage.sapphire.modules.configreport.renderer.element." + propertyTreeId.substring(0, 1).toUpperCase() + propertyTreeId.substring(1) + "ElementRenderer";
            try {
                renderer = (BaseElementRenderer)Class.forName(className).newInstance();
            }
            catch (ClassNotFoundException e) {
                renderer = new GenericElementRenderer();
            }
            renderer.initialize(this.config, this.sdisIncluded);
            configReportContent.appendSpecialContent(renderer.report(elementId, elementProperties, refElementProperties, elementPropertyDefinitionList, reportAdvancedProperties, reportHiddenColumns, this.includeDiffReport), this.diffOnly);
        }
        catch (Exception e) {
            Trace.logError("Failed to render element " + elementId + " page:" + this.webpageRO.currentSDI.getKeyid1() + " : " + e.getMessage());
        }
        return configReportContent;
    }

    protected ConfigReportContent renderOtherElements(String excludedElementsStr, boolean reportAdvancedProperties, boolean reportHiddenColumns, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Element: Others");
        if (includeDiffReport) {
            if (this.webpageRO == null || this.webpageRO.currentSDI == null) {
                configReportContent.appendSpecialContent(this.renderOtherElementsForRO(excludedElementsStr, reportAdvancedProperties, reportHiddenColumns, this.refWebpageRO), this.diffOnly);
            } else if (this.refWebpageRO == null || this.refWebpageRO.currentSDI == null) {
                configReportContent.appendSpecialContent(this.renderOtherElementsForRO(excludedElementsStr, reportAdvancedProperties, reportHiddenColumns, this.webpageRO), this.diffOnly);
            } else {
                configReportContent.appendSpecialContent(this.renderOtherElementsDiff(excludedElementsStr, reportAdvancedProperties, reportHiddenColumns), this.diffOnly);
            }
        } else {
            configReportContent.appendSpecialContent(this.renderOtherElementsForRO(excludedElementsStr, reportAdvancedProperties, reportHiddenColumns, this.webpageRO), this.diffOnly);
        }
        return configReportContent;
    }

    private ConfigReportContent renderOtherElementsForRO(String excludedElementsStr, boolean reportAdvancedProperties, boolean reportHiddenColumns, WebPageRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Element: other ");
        try {
            ArrayList elementList = ro.getWebPageElements();
            String[] excludedElements = StringUtil.split(excludedElementsStr, ",");
            for (int i = 0; i < elementList.size(); ++i) {
                BaseElementRenderer renderer;
                String elementId = (String)elementList.get(i);
                boolean excluded = false;
                for (int j = 0; j < excludedElements.length; ++j) {
                    if (!elementId.equals(excludedElements[j])) continue;
                    excluded = true;
                    break;
                }
                if (excluded) continue;
                PropertyList elementProperties = ro.getElementDetails(elementId);
                PropertyList refElementProperties = null;
                String propertyTreeId = elementProperties.getProperty("propertytreeid");
                PropertyDefinitionList elementPropertyDefinitionList = null;
                try {
                    elementPropertyDefinitionList = ro.getElementPropertyDefinitionList(elementId, propertyTreeId);
                }
                catch (SapphireException e) {
                    Trace.logError("Failed to get propertydefinition for element: " + ro.getWebPageId() + "," + elementId, e);
                    continue;
                }
                String className = "com.labvantage.sapphire.modules.configreport.renderer.element." + propertyTreeId.substring(0, 1).toUpperCase() + propertyTreeId.substring(1) + "ElementRenderer";
                try {
                    renderer = (BaseElementRenderer)Class.forName(className).newInstance();
                }
                catch (ClassNotFoundException e) {
                    renderer = new GenericElementRenderer();
                }
                catch (Exception e) {
                    throw new SapphireException("Rendering element failed" + e.getMessage(), e);
                }
                renderer.initialize(this.config, this.sdisIncluded);
                configReportContent.appendSpecialContent(renderer.report(elementId, elementProperties, refElementProperties, elementPropertyDefinitionList, reportAdvancedProperties, reportHiddenColumns, this.includeDiffReport), this.diffOnly);
            }
        }
        catch (SapphireException e) {
            Trace.logError("Failed to render page elements: " + e.getMessage());
        }
        return configReportContent;
    }

    private ConfigReportContent renderOtherElementsDiff(String excludedElementsStr, boolean reportAdvancedProperties, boolean reportHiddenColumns) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "Element: other");
        try {
            ArrayList elementList = this.webpageRO.getWebPageElements();
            String[] excludedElements = StringUtil.split(excludedElementsStr, ",");
            for (int i = 0; i < elementList.size(); ++i) {
                BaseElementRenderer renderer;
                String elementId = (String)elementList.get(i);
                boolean excluded = false;
                for (int j = 0; j < excludedElements.length; ++j) {
                    if (!elementId.equals(excludedElements[j])) continue;
                    excluded = true;
                    break;
                }
                if (excluded) continue;
                PropertyList elementProperties = this.webpageRO.getElementDetails(elementId);
                PropertyList refElementProperties = null;
                if (this.includeDiffReport && this.refWebpageRO != null && this.refWebpageRO.currentSDI != null) {
                    refElementProperties = this.refWebpageRO.getElementDetails(elementId);
                }
                String propertyTreeId = elementProperties.getProperty("propertytreeid");
                PropertyDefinitionList elementPropertyDefinitionList = null;
                try {
                    elementPropertyDefinitionList = this.webpageRO.getElementPropertyDefinitionList(elementId, propertyTreeId);
                }
                catch (SapphireException e) {
                    Trace.logError("Failed to get propertydefinition for element: " + this.webpageRO.getWebPageId() + "," + elementId, e);
                    continue;
                }
                String className = "com.labvantage.sapphire.modules.configreport.renderer.element." + propertyTreeId.substring(0, 1).toUpperCase() + propertyTreeId.substring(1) + "ElementRenderer";
                try {
                    renderer = (BaseElementRenderer)Class.forName(className).newInstance();
                }
                catch (ClassNotFoundException e) {
                    renderer = new GenericElementRenderer();
                }
                catch (Exception e) {
                    throw new SapphireException("Rendering element failed" + e.getMessage(), e);
                }
                renderer.initialize(this.config, this.sdisIncluded);
                configReportContent.appendSpecialContent(renderer.report(elementId, elementProperties, refElementProperties, elementPropertyDefinitionList, reportAdvancedProperties, reportHiddenColumns, this.includeDiffReport), this.diffOnly);
            }
        }
        catch (SapphireException e) {
            Trace.logError("Failed to render page" + this.webpageRO.currentSDI.getKeyid1() + " elements: " + e.getMessage());
        }
        return configReportContent;
    }

    public ConfigReportContent renderAllElementsDetails(boolean reportAdvancedProperties, boolean includeDiffReport) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "All Elements");
        try {
            configReportContent.startSubSection("PageType Properties", "");
            if (includeDiffReport) {
                if (this.webpageRO == null || this.webpageRO.currentSDI == null) {
                    PropertyList pageTypeInfo = this.refWebpageRO.getPageTypeInfo();
                    configReportContent.appendSubSection(configReportContent.renderPropertyList(pageTypeInfo, true), "PageType Properties", this.diffOnly);
                    configReportContent.appendSpecialContent(this.renderAllElementsForRef(reportAdvancedProperties, this.refWebpageRO), this.diffOnly);
                } else if (this.refWebpageRO == null || this.refWebpageRO.currentSDI == null) {
                    PropertyList pageTypeInfo = this.webpageRO.getPageTypeInfo();
                    configReportContent.appendSubSection(configReportContent.renderPropertyList(pageTypeInfo, true), "PageType Properties", this.diffOnly);
                    configReportContent.appendSpecialContent(this.renderAllElementsForSrc(reportAdvancedProperties, this.webpageRO), this.diffOnly);
                } else {
                    PropertyList pageTypeInfo = this.webpageRO.getPageTypeInfo();
                    PropertyList refPageTypeInfo = this.refWebpageRO.getPageTypeInfo();
                    configReportContent.appendSubSection(configReportContent.renderPropertyListDiff(pageTypeInfo, refPageTypeInfo, true, this.translationProcessor), "PageType Properties", this.diffOnly);
                    configReportContent.appendSpecialContent(this.renderAllElementsDiff(reportAdvancedProperties), this.diffOnly);
                }
            } else {
                configReportContent.appendSubSection(configReportContent.renderPropertyList(this.webpageRO.getPageTypeInfo(), true), "PageType Properties", this.diffOnly);
                configReportContent.appendSpecialContent(this.renderAllElementsForSrc(reportAdvancedProperties, this.webpageRO), this.diffOnly);
            }
        }
        catch (SapphireException e) {
            Trace.logError("Failed to render page elements: " + e.getMessage());
        }
        return configReportContent;
    }

    private ConfigReportContent renderAllElementsForSrc(boolean reportAdvancedProperties, WebPageRO ro) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "All Elements");
        ArrayList elementList = ro.getWebPageElements();
        for (int i = 0; i < elementList.size(); ++i) {
            String elementId = (String)elementList.get(i);
            PropertyList elementProperties = ro.getElementDetails(elementId);
            String propertyTreeId = elementProperties.getProperty("propertytreeid");
            if (propertyTreeId == null || propertyTreeId.length() <= 0) continue;
            PropertyDefinitionList elementPropertyDefinitionList = null;
            try {
                elementPropertyDefinitionList = ro.getElementPropertyDefinitionList(elementId, propertyTreeId);
            }
            catch (SapphireException e) {
                Trace.logError("Failed to get propertydefinition for element: " + this.webpageRO.getWebPageId() + "," + elementId, e);
                continue;
            }
            GenericElementRenderer renderer = new GenericElementRenderer();
            renderer.initialize(this.config, this.sdisIncluded);
            configReportContent.appendSpecialContent(renderer.reportDetails(elementId, elementProperties, elementPropertyDefinitionList, reportAdvancedProperties, this.includeDiffReport));
        }
        return configReportContent;
    }

    private ConfigReportContent renderAllElementsForRef(boolean reportAdvancedProperties, WebPageRO ro) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "All Elements");
        ArrayList elementList = ro.getWebPageElements();
        for (int i = 0; i < elementList.size(); ++i) {
            String elementId = (String)elementList.get(i);
            PropertyList elementProperties = ro.getElementDetails(elementId);
            String propertyTreeId = elementProperties.getProperty("propertytreeid");
            if (propertyTreeId == null || propertyTreeId.length() <= 0) continue;
            PropertyDefinitionList elementPropertyDefinitionList = null;
            try {
                elementPropertyDefinitionList = ro.getElementPropertyDefinitionList(elementId, propertyTreeId);
            }
            catch (SapphireException e) {
                Trace.logError("Failed to get propertydefinition for element: " + this.refWebpageRO.getWebPageId() + "," + elementId, e);
                continue;
            }
            GenericElementRenderer renderer = new GenericElementRenderer();
            renderer.initialize(this.config, this.sdisIncluded);
            configReportContent.appendSpecialContent(renderer.reportDetails(elementId, elementProperties, elementPropertyDefinitionList, reportAdvancedProperties, this.includeDiffReport));
        }
        return configReportContent;
    }

    private ConfigReportContent renderAllElementsDiff(boolean reportAdvancedProperties) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "All Elements");
        ArrayList srcElementList = this.webpageRO.getWebPageElements();
        ArrayList refElementList = this.refWebpageRO.getWebPageElements();
        ArrayList mergedElementList = this.mergeElementLists(srcElementList, refElementList);
        for (int i = 0; i < mergedElementList.size(); ++i) {
            ConfigReportContent elem;
            String elementId = (String)mergedElementList.get(i);
            PropertyList srcElementProperties = null;
            if (this.checkIfExists(srcElementList, elementId)) {
                srcElementProperties = this.webpageRO.getElementDetails(elementId);
            }
            PropertyList refElementProps = null;
            if (this.checkIfExists(refElementList, elementId)) {
                refElementProps = this.refWebpageRO.getElementDetails(elementId);
            }
            if (srcElementProperties == null) {
                elem = this.renderElementInfo(elementId, this.refWebpageRO, refElementProps, reportAdvancedProperties);
                elem.markAsDeleted();
                configReportContent.appendSpecialContent(elem, this.diffOnly);
                continue;
            }
            if (refElementProps == null) {
                elem = this.renderElementInfo(elementId, this.webpageRO, srcElementProperties, reportAdvancedProperties);
                elem.markAsNew();
                configReportContent.appendSpecialContent(elem, this.diffOnly);
                continue;
            }
            elem = this.renderElementDiff(elementId, srcElementProperties, refElementProps, reportAdvancedProperties);
            configReportContent.appendSpecialContent(elem, this.diffOnly);
        }
        return configReportContent;
    }

    private ConfigReportContent renderElementInfo(String elementId, WebPageRO ro, PropertyList elementProps, boolean reportAdvancedProperties) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent(this.config, "Element Info: " + elementId);
        String propertyTreeId = elementProps.getProperty("config");
        if (propertyTreeId != null && propertyTreeId.length() > 0) {
            PropertyDefinitionList elementPropertyDefinitionList = null;
            try {
                elementPropertyDefinitionList = ro.getElementPropertyDefinitionList(elementId, propertyTreeId);
            }
            catch (SapphireException e) {
                Trace.logError("Failed to get propertydefinition for element: " + ro.getWebPageId() + "," + elementId, e);
            }
            GenericElementRenderer renderer = new GenericElementRenderer();
            renderer.initialize(this.config, this.sdisIncluded);
            content.appendSpecialContent(renderer.reportDetails(elementId, elementProps, elementPropertyDefinitionList, reportAdvancedProperties, this.includeDiffReport));
        }
        return content;
    }

    private ConfigReportContent renderElementDiff(String elementId, PropertyList srcElementProps, PropertyList refElementProps, boolean reportAdvancedProperties) throws SapphireException {
        ConfigReportContent content = new ConfigReportContent(this.config, "Element: " + elementId);
        String propertyTreeId = srcElementProps.getProperty("propertytreeid");
        if (propertyTreeId != null && propertyTreeId.length() > 0) {
            PropertyDefinitionList elementPropertyDefinitionList = null;
            try {
                elementPropertyDefinitionList = this.webpageRO.getElementPropertyDefinitionList(elementId, propertyTreeId);
            }
            catch (SapphireException e) {
                Trace.logError("Failed to get propertydefinition for element: " + this.webpageRO.getWebPageId() + "," + elementId, e);
            }
            GenericElementRenderer renderer = new GenericElementRenderer();
            renderer.initialize(this.config, this.sdisIncluded);
            content.appendSpecialContent(renderer.reportDetailsDiff(elementId, srcElementProps, refElementProps, elementPropertyDefinitionList, reportAdvancedProperties, this.includeDiffReport));
        }
        return content;
    }

    private ArrayList mergeElementLists(ArrayList sourceList, ArrayList refList) {
        int i;
        if (refList == null || refList.size() == 0) {
            return sourceList;
        }
        TreeSet merged = new TreeSet();
        for (i = 0; i < sourceList.size(); ++i) {
            merged.add(sourceList.get(i));
        }
        for (i = 0; i < refList.size(); ++i) {
            merged.add(refList.get(i));
        }
        Object[] arr = merged.toArray();
        ArrayList<String> ret = new ArrayList<String>();
        for (int i2 = 0; i2 < arr.length; ++i2) {
            ret.add((String)arr[i2]);
        }
        return ret;
    }

    private boolean checkIfExists(ArrayList list, String element) {
        for (int i = 0; i < list.size(); ++i) {
            String curr = list.get(i).toString();
            if (!curr.equals(element)) continue;
            return true;
        }
        return false;
    }
}

