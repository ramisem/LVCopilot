/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.element.AdvancedtoolbarElementRenderer;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.WebPageRO;
import com.labvantage.sapphire.modules.configreport.util.WebPageUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WebPageRenderer
extends WebPageUtil {
    private boolean reportSummary = true;
    private boolean reportDetails = true;
    private boolean reportCategories = true;
    private boolean reportHiddenColumns = false;
    private boolean reportInheritdProperties = true;
    private boolean reportAdvancedProperties = true;
    private boolean pageButtonRoleMatrixIncluded = true;
    boolean backwardCompatibilityMode = false;
    private WebPageRO webpageRO;
    private WebPageRO refWebpageRO;
    private PropertyList config;
    private String screenshotsFolder;
    public static final String OPTION_DISPLAY_SUMMARY = "Include Special View";
    public static final String OPTION_DISPLAY_DETAILS = "Include Raw Data";
    public static final String OPTION_DISPLAY_CATEGORIES = "Include Categories";
    public static final String OPTION_ADVANCED_PROPERTIES = "Include Advanced Properties";
    public static final String OPTION_SCREENSHOTS_FOLDER = "Folder Containing Screenshots";
    public static final String OPTION_HIDDEN_COLUMNS = "Include Hidden Columns";
    public static final String OPTION_INHERITED_PROPERTIES = "Include Inherited Properties";
    public static final String OPTION_BACKWARD_COMPATIBILITY_MODE = "Backward Compatibility Mode";

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseSDCRO srcRO, BaseSDCRO refRO, HashMap sdisIncluded, boolean includeDiffReport, boolean includeSDIRoleMatrix, boolean includeWebPagePageButtonRoleMatrix) {
        super.initialize(sapphireConnection, config, srcRO, refRO, sdisIncluded, includeDiffReport, includeSDIRoleMatrix);
        this.webpageRO = (WebPageRO)srcRO;
        if (refRO != null) {
            this.refWebpageRO = (WebPageRO)refRO;
        }
        this.config = config;
        this.pageButtonRoleMatrixIncluded = includeWebPagePageButtonRoleMatrix;
    }

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO ro, HashMap sdisIncluded, boolean includeSDIRoleMatrix, boolean includeWebPagePageButtonRoleMatrix) {
        super.initialize(sapphireConnection, config, ro, sdisIncluded, includeSDIRoleMatrix);
        this.webpageRO = (WebPageRO)ro;
        if (this.refRO != null) {
            this.refWebpageRO = (WebPageRO)this.refRO;
        }
        this.config = config;
        this.pageButtonRoleMatrixIncluded = includeWebPagePageButtonRoleMatrix;
    }

    @Override
    public PropertyListCollection getOptions() {
        PropertyListCollection ret = new PropertyListCollection();
        PropertyList option = new PropertyList();
        option.setProperty("optionid", "IncludeSpecialContent");
        option.setProperty("title", "Include Special Content");
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeRawContent");
        option.setProperty("title", "Include Raw Content");
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeCategories");
        option.setProperty("title", OPTION_DISPLAY_CATEGORIES);
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeAdvanced");
        option.setProperty("title", OPTION_ADVANCED_PROPERTIES);
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "ScreenShotsFolder");
        option.setProperty("title", "Screen Shots Folder");
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeHiddenColumns");
        option.setProperty("title", OPTION_HIDDEN_COLUMNS);
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "BackwardCompatibilityMode");
        option.setProperty("title", OPTION_BACKWARD_COMPATIBILITY_MODE);
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeInheritedProperties");
        option.setProperty("title", OPTION_INHERITED_PROPERTIES);
        ret.add(option);
        return ret;
    }

    @Override
    public void setOptions(DataSet options) {
        PropertyList selectedOptions = new PropertyList();
        for (int i = 0; i < options.getRowCount(); ++i) {
            int optionNo = options.getInt(i, "optionno");
            if (optionNo == 0) {
                selectedOptions.setProperty(OPTION_DISPLAY_SUMMARY, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 1) {
                selectedOptions.setProperty(OPTION_DISPLAY_DETAILS, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 2) {
                selectedOptions.setProperty(OPTION_DISPLAY_CATEGORIES, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 3) {
                selectedOptions.setProperty(OPTION_ADVANCED_PROPERTIES, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 4) {
                selectedOptions.setProperty(OPTION_SCREENSHOTS_FOLDER, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 5) {
                selectedOptions.setProperty(OPTION_HIDDEN_COLUMNS, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 6) {
                selectedOptions.setProperty(OPTION_INHERITED_PROPERTIES, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo != 7) continue;
            selectedOptions.setProperty(OPTION_BACKWARD_COMPATIBILITY_MODE, options.getString(i, "selectedvalue"));
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_DISPLAY_SUMMARY))) {
            this.reportSummary = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_DISPLAY_DETAILS))) {
            this.reportDetails = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_ADVANCED_PROPERTIES))) {
            this.reportAdvancedProperties = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_DISPLAY_CATEGORIES))) {
            this.reportCategories = false;
        }
        if ("Y".equals(selectedOptions.getProperty(OPTION_HIDDEN_COLUMNS))) {
            this.reportHiddenColumns = true;
        }
        if ("Y".equals(selectedOptions.getProperty(OPTION_BACKWARD_COMPATIBILITY_MODE))) {
            this.backwardCompatibilityMode = true;
        }
        this.screenshotsFolder = selectedOptions.getProperty(OPTION_SCREENSHOTS_FOLDER);
    }

    @Override
    public void setOptions(PropertyListCollection options) {
        PropertyList selectedOptions = new PropertyList();
        for (int i = 0; i < options.size(); ++i) {
            PropertyList currOptions = options.getPropertyList(i);
            String option = currOptions.getProperty("optionid");
            if ("IncludeSpecialContent".equals(option)) {
                selectedOptions.setProperty("Include Special Content ", currOptions.getProperty("optionvalue"));
                continue;
            }
            if ("IncludeRawContent".equals(option)) {
                selectedOptions.setProperty(OPTION_DISPLAY_DETAILS, currOptions.getProperty("optionvalue"));
                continue;
            }
            if ("IncludeCategories".equals(option)) {
                selectedOptions.setProperty(OPTION_DISPLAY_CATEGORIES, currOptions.getProperty("optionvalue"));
                continue;
            }
            if ("IncludeAdvanced".equals(option)) {
                selectedOptions.setProperty(OPTION_ADVANCED_PROPERTIES, currOptions.getProperty("optionvalue"));
                continue;
            }
            if ("IncludeHiddenColumns".equals(option)) {
                selectedOptions.setProperty(OPTION_HIDDEN_COLUMNS, currOptions.getProperty("optionvalue"));
                continue;
            }
            if ("IncludeInheritedProperties".equals(option)) {
                selectedOptions.setProperty(OPTION_INHERITED_PROPERTIES, currOptions.getProperty("optionvalue"));
                continue;
            }
            if (!"BackwardCompatibilityMode".equals(option)) continue;
            selectedOptions.setProperty(OPTION_BACKWARD_COMPATIBILITY_MODE, currOptions.getProperty("optionvalue"));
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_DISPLAY_SUMMARY))) {
            this.reportSummary = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_DISPLAY_DETAILS))) {
            this.reportDetails = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_ADVANCED_PROPERTIES))) {
            this.reportAdvancedProperties = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_DISPLAY_CATEGORIES))) {
            this.reportCategories = false;
        }
        if ("Y".equals(selectedOptions.getProperty(OPTION_HIDDEN_COLUMNS))) {
            this.reportHiddenColumns = true;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_INHERITED_PROPERTIES))) {
            this.reportInheritdProperties = false;
        }
        if ("Y".equals(selectedOptions.getProperty(OPTION_BACKWARD_COMPATIBILITY_MODE))) {
            this.backwardCompatibilityMode = true;
        }
        this.screenshotsFolder = selectedOptions.getProperty(OPTION_SCREENSHOTS_FOLDER);
    }

    @Override
    public ConfigReportContent getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent details;
        ConfigReportContent configReportContent = new ConfigReportContent("WebPage", translationProcessor);
        configReportContent.setFoundDiff(false);
        WebPageRO currRO = (WebPageRO)sdcRO;
        if (this.includeDiffReport) {
            if (sdcRO == null || sdcRO.currentSDI == null) {
                currRO = (WebPageRO)refSdcRO;
                configReportContent.startSDISection(refSdcRO.currentSDI, refSdcRO.getDescription());
            } else if (refSdcRO == null || refSdcRO.currentSDI == null) {
                configReportContent.startSDISection(sdcRO.currentSDI, sdcRO.getDescription());
            } else {
                configReportContent.startSDISectionDiff(sdcRO.currentSDI, sdcRO.getDescription(), refSdcRO.getDescription());
            }
        } else {
            configReportContent.startSDISection(sdcRO.currentSDI, sdcRO.getDescription());
        }
        String currWebPageId = currRO.getWebPageId();
        String currEdition = currRO.getWebPageProductEdition();
        String currDesc = currRO.getWebPageDesc();
        if (this.reportSummary) {
            configReportContent.startSection("WebPage " + currWebPageId + "," + currEdition);
            if (this.backwardCompatibilityMode) {
                configReportContent.appendSpecialContent(this.renderWebPageSummary(this.webpageRO, this.refWebpageRO, currWebPageId, currEdition, currDesc, this.reportAdvancedProperties, this.reportHiddenColumns, this.screenshotsFolder, false), this.diffOnly);
            } else if (this.includeDiffReport) {
                configReportContent.appendSpecialContent(this.renderWebPageSummaryNewMode(this.webpageRO, this.refWebpageRO, currWebPageId, currEdition, currDesc, this.reportAdvancedProperties, this.reportHiddenColumns, this.screenshotsFolder, false, this.reportInheritdProperties), this.diffOnly);
            } else {
                configReportContent.appendSpecialContent(this.renderWebPageSummaryNewMode(this.webpageRO, this.webpageRO, currWebPageId, currEdition, currDesc, this.reportAdvancedProperties, this.reportHiddenColumns, this.screenshotsFolder, false, this.reportInheritdProperties), this.diffOnly);
            }
            configReportContent.endSection();
        }
        if (this.backwardCompatibilityMode && this.reportDetails && (details = this.renderWebPageDetails(this.webpageRO, this.refWebpageRO, currWebPageId, currEdition, currDesc, this.reportAdvancedProperties)).toString().trim().length() > 0) {
            configReportContent.startSection("Detailed Definition");
            configReportContent.appendSpecialContent(details, this.diffOnly);
            configReportContent.endSection();
        }
        configReportContent.endSection();
        this.updateSectionChangeInfo("WebPage", ConfigReportContent.generateSDISectionTitle(currRO.currentSDI), configReportContent);
        return configReportContent;
    }

    @Override
    public boolean hasCustomSections() {
        return this.pageButtonRoleMatrixIncluded;
    }

    @Override
    public ArrayList getCustomSectionNames() {
        ArrayList<String> ret = new ArrayList<String>();
        ret.add("Page Button Role Matrix");
        return ret;
    }

    @Override
    public ConfigReportContent getCustomSectionContent(String currSection) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "WebPage Page Button Role Matrix: ");
        if (currSection.equals("Page Button Role Matrix")) {
            if (this.includeDiffReport) {
                if (this.webpageRO == null) {
                    configReportContent.appendSubSection(this.createPageButtonRoleMatrix(this.refWebpageRO), "WebPage Page Button Role Matrix", this.diffOnly);
                } else if (this.refWebpageRO == null) {
                    configReportContent.appendSubSection(this.createPageButtonRoleMatrix(this.webpageRO), "WebPage Page Button Role Matrix", this.diffOnly);
                } else {
                    configReportContent.appendSubSection(this.createPageButtonRoleMatrixDiff(), "WebPage Page Button Role Matrix", this.diffOnly);
                }
                configReportContent.endSection();
                this.updateSectionChangeInfo("WebPage", ConfigReportContent.generateSectionTitle("Page Button Role Matrix"), configReportContent);
            } else {
                configReportContent.appendSubSection(this.createPageButtonRoleMatrix(this.webpageRO), "WebPage Page Button Role Matrix", this.diffOnly);
            }
        }
        return configReportContent;
    }

    public ConfigReportContent createPageButtonRoleMatrix(WebPageRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "WebPage Page Button Role Matrix");
        DataSet roleMatrix = this.getRoleMatrix(ro);
        configReportContent.startSection("Page Button Role Matrix ");
        configReportContent.renderRoleMatrix(roleMatrix, 2);
        configReportContent.endSection();
        return configReportContent;
    }

    public ConfigReportContent createPageButtonRoleMatrixDiff() {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "WebPage Page Button Role Matrix");
        DataSet roleMatrix = this.getRoleMatrix(this.webpageRO);
        configReportContent.startSection("Page Button Role Matrix ");
        DataSet refRoleMatrix = this.getRoleMatrix(this.refWebpageRO);
        String[] keycols = new String[]{"PageId", "Edition", "Button/Operation"};
        configReportContent.renderDiffRoleMatrix(roleMatrix, refRoleMatrix, keycols, true);
        configReportContent.endSection();
        return configReportContent;
    }

    private DataSet getRoleMatrix(WebPageRO webpageRO) {
        DataSet roleMatrix = new DataSet();
        roleMatrix.setColidCaseSensitive(true);
        try {
            webpageRO.reset();
            webpageRO.startChapter();
            boolean i = false;
            while (webpageRO.hasNextSection()) {
                webpageRO.nextSection();
                webpageRO.startSection();
                DataSet currentPageButtonRoleMatrix = this.getCurrentPageButtonRoles(webpageRO);
                if (currentPageButtonRoleMatrix == null || currentPageButtonRoleMatrix.getRowCount() <= 0) continue;
                String[] currPageRoleMatrixColumns = currentPageButtonRoleMatrix.getColumns();
                for (int row = 0; row < currentPageButtonRoleMatrix.getRowCount(); ++row) {
                    int currRow = roleMatrix.addRow();
                    roleMatrix.setString(currRow, "PageId", webpageRO.getWebPageId());
                    roleMatrix.setString(currRow, "Edition", webpageRO.getKeyid2());
                    for (int role = 0; role < currPageRoleMatrixColumns.length; ++role) {
                        String val = currentPageButtonRoleMatrix.getString(row, currPageRoleMatrixColumns[role], "");
                        roleMatrix.setString(currRow, currPageRoleMatrixColumns[role], val);
                    }
                }
            }
        }
        catch (SapphireException e) {
            Trace.logError("Failed to create page/button role matrix:" + e.getMessage());
        }
        return roleMatrix;
    }

    private DataSet getRefRoleMatrix(WebPageRO refwebpageRO) {
        DataSet roleMatrix = new DataSet();
        roleMatrix.setColidCaseSensitive(true);
        try {
            refwebpageRO.reset();
            refwebpageRO.startChapter();
            boolean i = false;
            while (refwebpageRO.hasNextSection()) {
                refwebpageRO.nextSection();
                DataSet currentPageButtonRoleMatrix = this.getCurrentPageButtonRoles(refwebpageRO);
                if (currentPageButtonRoleMatrix == null || currentPageButtonRoleMatrix.getRowCount() <= 0) continue;
                String[] currPageRoleMatrixColumns = currentPageButtonRoleMatrix.getColumns();
                for (int row = 0; row < currentPageButtonRoleMatrix.getRowCount(); ++row) {
                    int currRow = roleMatrix.addRow();
                    roleMatrix.setString(currRow, "PageId", refwebpageRO.getWebPageId());
                    for (int role = 0; role < currPageRoleMatrixColumns.length; ++role) {
                        String val = currentPageButtonRoleMatrix.getString(row, currPageRoleMatrixColumns[role], "");
                        roleMatrix.setString(currRow, currPageRoleMatrixColumns[role], val);
                    }
                }
            }
        }
        catch (SapphireException e) {
            Trace.logError("Failed to create page/button role matrix:" + e.getMessage());
        }
        return roleMatrix;
    }

    public DataSet getCurrentPageButtonRoles(WebPageRO webpageRO) throws SapphireException {
        PropertyList toolbarProperties = webpageRO.getElementDetails("advancedtoolbar");
        if (toolbarProperties.isEmpty()) {
            return null;
        }
        AdvancedtoolbarElementRenderer renderer = new AdvancedtoolbarElementRenderer();
        renderer.initialize(this.config, this.sdisIncluded);
        return renderer.getButtonRoleMatrix(toolbarProperties);
    }

    @Override
    public void createXMLReport() throws SapphireException {
        if (this.sdcRO != null && this.sdcRO.currentSDI != null) {
            FileOutputStream sdiXMLFile;
            super.createXMLReport();
            String xmlReportContent = this.webpageRO.pageProps.toXMLString();
            String xmlFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
            xmlFileName = xmlFileName.replace(".xml", "_pageprops.xml");
            try {
                sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlFileName);
            }
            try {
                OutputStreamWriter writer = new OutputStreamWriter((OutputStream)sdiXMLFile, "UTF-8");
                writer.write(xmlReportContent);
                writer.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
    }
}

