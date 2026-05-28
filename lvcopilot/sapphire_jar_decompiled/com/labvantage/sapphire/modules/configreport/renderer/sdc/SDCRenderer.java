/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.renderer.sdc;

import com.labvantage.sapphire.modules.configreport.ro.SDCRO;
import com.labvantage.sapphire.modules.configreport.util.SDCUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import sapphire.SapphireException;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDCRenderer
extends SDCUtil {
    public static final String OPTION_SDC_SUMMARY = "Include Special Content ";
    public static final String OPTION_SDC_DETAILS = "Include Raw Data";
    public static final String OPTION_TEMPLATES_ONLY = "Include Templates Only";
    public static final String OPTION_SDC_CATEGORIES = "Include Categories";
    public static final String OPTION_SDC_RELATIONSHIP_MODEL_ONLY = "Include SDC Relationship Model Only";
    private boolean reportSDCRelationshipModelOnly = false;

    @Override
    public PropertyListCollection getOptions() {
        PropertyListCollection ret = new PropertyListCollection();
        PropertyList option = new PropertyList();
        option.setProperty("optionid", "IncludeSpecialContent");
        option.setProperty("title", "Include Special Content");
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeRawContent");
        option.setProperty("title", "Include Raw Tables");
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeTemplatesOnly");
        option.setProperty("title", OPTION_TEMPLATES_ONLY);
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeCategories");
        option.setProperty("title", OPTION_SDC_CATEGORIES);
        ret.add(option);
        option = new PropertyList();
        option.setProperty("optionid", "IncludeSDCRelationshipModelOnly");
        option.setProperty("title", OPTION_SDC_RELATIONSHIP_MODEL_ONLY);
        ret.add(option);
        return ret;
    }

    @Override
    public void setOptions(PropertyListCollection options) {
        PropertyList selectedOptions = new PropertyList();
        for (int i = 0; i < options.size(); ++i) {
            PropertyList currOptions = options.getPropertyList(i);
            String option = currOptions.getProperty("optionid");
            if ("IncludeSpecialContent".equals(option)) {
                selectedOptions.setProperty(OPTION_SDC_SUMMARY, currOptions.getProperty("optionvalue"));
                continue;
            }
            if ("IncludeRawContent".equals(option)) {
                selectedOptions.setProperty(OPTION_SDC_DETAILS, currOptions.getProperty("optionvalue"));
                continue;
            }
            if ("IncludeTemplatesOnly".equals(option)) {
                selectedOptions.setProperty(OPTION_TEMPLATES_ONLY, currOptions.getProperty("optionvalue"));
                continue;
            }
            if ("IncludeCategories".equals(option)) {
                selectedOptions.setProperty(OPTION_SDC_CATEGORIES, currOptions.getProperty("optionvalue"));
                continue;
            }
            if (!"IncludeSDCRelationshipModelOnly".equals(option)) continue;
            selectedOptions.setProperty(OPTION_SDC_RELATIONSHIP_MODEL_ONLY, currOptions.getProperty("optionvalue"));
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_SDC_SUMMARY))) {
            this.reportSummary = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_SDC_DETAILS))) {
            this.reportDetails = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_SDC_CATEGORIES))) {
            this.reportCategories = false;
        }
        if ("Y".equals(selectedOptions.getProperty(OPTION_TEMPLATES_ONLY))) {
            this.reportTemplatesOnly = true;
        }
        if ("Y".equals(selectedOptions.getProperty(OPTION_SDC_RELATIONSHIP_MODEL_ONLY))) {
            this.reportSDCRelationshipModelOnly = true;
        }
    }

    @Override
    public void setOptions(DataSet options) {
        PropertyList selectedOptions = new PropertyList();
        for (int i = 0; i < options.getRowCount(); ++i) {
            int optionNo = options.getInt(i, "optionno");
            if (optionNo == 0) {
                selectedOptions.setProperty(OPTION_SDC_SUMMARY, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 1) {
                selectedOptions.setProperty(OPTION_SDC_DETAILS, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 2) {
                selectedOptions.setProperty(OPTION_TEMPLATES_ONLY, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo == 3) {
                selectedOptions.setProperty(OPTION_SDC_CATEGORIES, options.getString(i, "selectedvalue"));
                continue;
            }
            if (optionNo != 4) continue;
            selectedOptions.setProperty(OPTION_SDC_RELATIONSHIP_MODEL_ONLY, options.getString(i, "selectedvalue"));
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_SDC_SUMMARY))) {
            this.reportSummary = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_SDC_DETAILS))) {
            this.reportDetails = false;
        }
        if ("N".equals(selectedOptions.getProperty(OPTION_SDC_CATEGORIES))) {
            this.reportCategories = false;
        }
        if ("Y".equals(selectedOptions.getProperty(OPTION_TEMPLATES_ONLY))) {
            this.reportTemplatesOnly = true;
        }
        if ("Y".equals(selectedOptions.getProperty(OPTION_SDC_RELATIONSHIP_MODEL_ONLY))) {
            this.reportSDCRelationshipModelOnly = true;
        }
    }

    @Override
    public void createXMLReport() throws SapphireException {
        super.createXMLReport();
        SDCRO sourceSDCRO = (SDCRO)this.sdcRO;
        SDCRO refSDCRO = (SDCRO)this.refSdcRO;
        if (sourceSDCRO != null && sourceSDCRO.currentSDIData != null) {
            FileOutputStream tableDocFile;
            FileOutputStream attrsDataFile;
            FileOutputStream referredByDetailsFile;
            FileOutputStream referredByFile;
            FileOutputStream refersToFile;
            FileOutputStream indexFile;
            FileOutputStream detailColsFile;
            FileOutputStream colsFile;
            FileOutputStream linksDataFile;
            FileOutputStream sdcPropsFile;
            FileOutputStream sdiXMLFile;
            String xmlSdiData = sourceSDCRO.currentSDIData.toXML();
            String xmlSdcProps = sourceSDCRO.getSDCPropertiesXML();
            String xmlLinksData = sourceSDCRO.getLinksInfo().toXML();
            String xmlCols = sourceSDCRO.currentCols.toXML();
            String xmlDetailsCols = sourceSDCRO.currentDetailsCols.toXML();
            String xmlIndexInfo = sourceSDCRO.currentIndexInfo.toXML();
            String xmlRefersToSDCs = sourceSDCRO.getRefersToSDCs().toXML();
            String xmlReferredBySDCs = sourceSDCRO.getReferredBySDCs().toXML();
            String xmlReferredBySDCDetails = sourceSDCRO.getReferredBySDCDetails().toXML();
            String xmlAttributesData = sourceSDCRO.getAttributesInfo().toXML();
            String xmlTableDoc = sourceSDCRO.getTableDocInfo().toXML();
            String xmlSdiFileName = ConfigReportContent.generateSDISectionXMLFileName(sourceSDCRO.currentSDI);
            String xmlSdcPropsFileName = xmlSdiFileName.replace(".xml", "_sdcprops.xml");
            String xmlLinksDataFileName = xmlSdiFileName.replace(".xml", "_linksdata.xml");
            String xmlColsFileName = xmlSdiFileName.replace(".xml", "_cols.xml");
            String xmlDetailsColsFileName = xmlSdiFileName.replace(".xml", "_detailscols.xml");
            String xmlIndexFileName = xmlSdiFileName.replace(".xml", "_indexinfo.xml");
            String xmlRefersToFileName = xmlSdiFileName.replace(".xml", "_refersto.xml");
            String xmlReferredByFileName = xmlSdiFileName.replace(".xml", "_referredby.xml");
            String xmlReferredByDetailsFileName = xmlSdiFileName.replace(".xml", "_referredbydetails.xml");
            String xmlAttrsDataFileName = xmlSdiFileName.replace(".xml", "_attrsdata.xml");
            String xmlTableDocFileName = xmlSdiFileName.replace(".xml", "_tabledoc.xml");
            try {
                sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlSdiFileName);
                sdcPropsFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlSdcPropsFileName);
                linksDataFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlLinksDataFileName);
                colsFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlColsFileName);
                detailColsFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlDetailsColsFileName);
                indexFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlIndexFileName);
                refersToFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlRefersToFileName);
                referredByFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlReferredByFileName);
                referredByDetailsFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlReferredByDetailsFileName);
                attrsDataFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlAttrsDataFileName);
                tableDocFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlTableDocFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlSdiFileName);
            }
            try {
                sdiXMLFile.write(xmlSdiData.getBytes());
                sdiXMLFile.close();
                sdcPropsFile.write(xmlSdcProps.getBytes());
                sdcPropsFile.close();
                linksDataFile.write(xmlLinksData.getBytes());
                linksDataFile.close();
                colsFile.write(xmlCols.getBytes());
                colsFile.close();
                detailColsFile.write(xmlDetailsCols.getBytes());
                detailColsFile.close();
                indexFile.write(xmlIndexInfo.getBytes());
                indexFile.close();
                refersToFile.write(xmlRefersToSDCs.getBytes());
                refersToFile.close();
                referredByFile.write(xmlReferredBySDCs.getBytes());
                referredByFile.close();
                referredByDetailsFile.write(xmlReferredBySDCDetails.getBytes());
                referredByDetailsFile.close();
                attrsDataFile.write(xmlAttributesData.getBytes());
                attrsDataFile.close();
                tableDocFile.write(xmlTableDoc.getBytes());
                tableDocFile.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
    }

    @Override
    public ConfigReportContent getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC: ");
        this.sdcRO = sdcRO;
        this.refSdcRO = refSdcRO;
        SDCRO currRO = null;
        currRO = sdcRO == null || sdcRO.currentSDI == null ? (SDCRO)refSdcRO : (SDCRO)sdcRO;
        configReportContent.startSDISection(currRO.currentSDI, currRO.getCurrentSDCDescription());
        int subsection = 1;
        if (this.reportSummary) {
            if (this.includeDiffReport) {
                if (sdcRO == null || sdcRO.currentSDI == null) {
                    configReportContent.startSubSection("SDC", "");
                    configReportContent.append(this.renderSDCSummary((SDCRO)refSdcRO).toString());
                    configReportContent.appendSubSection(SDCRenderer.renderSDCInfo((SDCRO)refSdcRO, translationProcessor), "SDC", this.diffOnly);
                    configReportContent.startSubSection("Security/Update Options", "");
                    configReportContent.appendSubSection(this.renderSecurityOptions((SDCRO)refSdcRO, translationProcessor), "Security/Update Options", this.diffOnly);
                    configReportContent.startSubSection("Definition Options", "");
                    configReportContent.appendSubSection(this.renderDefinitionOptions((SDCRO)refSdcRO, translationProcessor), "Definition Options", this.diffOnly);
                    configReportContent.startSubSection("Tables", "");
                    configReportContent.appendSubSection(this.renderTablesInfo(this.reportSDCRelationshipModelOnly, (SDCRO)refSdcRO), "Tables", this.diffOnly);
                    configReportContent.startSubSection("Columns", "");
                    configReportContent.appendSpecialContent(this.renderColumnsInfo(this.reportSDCRelationshipModelOnly, (SDCRO)refSdcRO), this.diffOnly);
                    configReportContent.startSubSection("Links", "");
                    configReportContent.appendSubSection(this.renderLinksInfo((SDCRO)refSdcRO), "Links", this.diffOnly);
                    configReportContent.startSubSection("Operations", "");
                    configReportContent.appendSubSection(this.renderOperationsInfo((SDCRO)refSdcRO), "Operations", this.diffOnly);
                    configReportContent.appendSpecialContent(this.renderIndexInfo((SDCRO)refSdcRO), this.diffOnly);
                    configReportContent.startSubSection("Attributes", "");
                    configReportContent.appendSubSection(this.renderAttrsInfo((SDCRO)refSdcRO), "Attributes", this.diffOnly);
                    configReportContent.startSubSection("SDC Relationship Model", "");
                    configReportContent.appendSubSection(this.renderRelationships((SDCRO)refSdcRO, true), "SDC Relationship Model", this.diffOnly);
                    configReportContent.endSubSection("Summary", "");
                } else if (this.refRO == null || refSdcRO.currentSDI == null) {
                    configReportContent.startSubSection("SDC", "");
                    configReportContent.append(this.renderSDCSummary((SDCRO)sdcRO).toString());
                    configReportContent.appendSubSection(SDCRenderer.renderSDCInfo((SDCRO)sdcRO, this.getTranslationProcessor()), "SDC", this.diffOnly);
                    configReportContent.startSubSection("Security/Update Options", "");
                    configReportContent.appendSubSection(this.renderSecurityOptions((SDCRO)sdcRO, translationProcessor), "Security/Update Options", this.diffOnly);
                    configReportContent.startSubSection("Definition Options", "");
                    configReportContent.appendSubSection(this.renderDefinitionOptions((SDCRO)sdcRO, translationProcessor), "Definition Options", this.diffOnly);
                    configReportContent.startSubSection("Tables", "");
                    configReportContent.appendSubSection(this.renderTablesInfo(this.reportSDCRelationshipModelOnly, (SDCRO)sdcRO), "Tables", this.diffOnly);
                    configReportContent.startSubSection("Columns", "");
                    configReportContent.appendSpecialContent(this.renderColumnsInfo(this.reportSDCRelationshipModelOnly, (SDCRO)sdcRO), this.diffOnly);
                    configReportContent.startSubSection("Links", "");
                    configReportContent.appendSubSection(this.renderLinksInfo((SDCRO)sdcRO), "Links", this.diffOnly);
                    configReportContent.startSubSection("Operations", "");
                    configReportContent.appendSubSection(this.renderOperationsInfo((SDCRO)sdcRO), "Operations", this.diffOnly);
                    configReportContent.appendSpecialContent(this.renderIndexInfo((SDCRO)sdcRO), this.diffOnly);
                    configReportContent.startSubSection("Attributes", "");
                    configReportContent.appendSubSection(this.renderAttrsInfo((SDCRO)sdcRO), "Attributes", this.diffOnly);
                    configReportContent.startSubSection("SDC Relationship Model", "");
                    configReportContent.appendSubSection(this.renderRelationships((SDCRO)sdcRO, true), "SDC Relationship Model", this.diffOnly);
                    configReportContent.endSubSection("SDC", "");
                } else {
                    configReportContent.startSubSection("SDC", "");
                    configReportContent.append(this.renderSDCSummaryDiff((SDCRO)sdcRO, (SDCRO)refSdcRO).toString());
                    configReportContent.appendSubSection(SDCRenderer.renderSDCInfoDiff(sdcRO.currentSDIData, refSdcRO.currentSDIData, false, translationProcessor), "SDC", this.diffOnly);
                    configReportContent.startSubSection("Security/Update Options", "");
                    configReportContent.appendSubSection(SDCRenderer.renderSecurityOptionsDiff(sdcRO.currentSDIData, refSdcRO.currentSDIData, translationProcessor), "Security/Update Options", this.diffOnly);
                    configReportContent.startSubSection("Definition Options", "");
                    configReportContent.appendSubSection(SDCRenderer.renderDefinitionOptionsDiff(sdcRO.currentSDIData, refSdcRO.currentSDIData, translationProcessor), "Definition Options", this.diffOnly);
                    configReportContent.startSubSection("Tables", "");
                    configReportContent.appendSubSection(this.renderTablesInfoDiff(this.reportSDCRelationshipModelOnly, (SDCRO)sdcRO, (SDCRO)refSdcRO), "Tables", this.diffOnly);
                    configReportContent.startSubSection("Columns", "");
                    configReportContent.appendSubSection(this.renderColumnsInfoDiff(this.reportSDCRelationshipModelOnly, (SDCRO)sdcRO, (SDCRO)refSdcRO), "Columns", this.diffOnly);
                    configReportContent.startSubSection("Links", "");
                    configReportContent.appendSubSection(this.renderLinksInfoDiff((SDCRO)sdcRO, (SDCRO)refSdcRO), "Links", this.diffOnly);
                    configReportContent.startSubSection("Operations", "");
                    configReportContent.appendSubSection(this.renderOperationsDiff((SDCRO)sdcRO, (SDCRO)refSdcRO), "Operations", this.diffOnly);
                    configReportContent.appendSpecialContent(this.renderIndexInfoDiff((SDCRO)sdcRO, (SDCRO)refSdcRO), this.diffOnly);
                    configReportContent.startSubSection("Attributes", "");
                    configReportContent.appendSubSection(this.renderAttrsInfoDiff((SDCRO)sdcRO, (SDCRO)refSdcRO), "Attributes", this.diffOnly);
                    configReportContent.startSubSection("SDC Relationship Model", "");
                    configReportContent.appendSubSection(this.renderRelationshipsDiff((SDCRO)sdcRO, (SDCRO)refSdcRO, true), "SDC Relationship Model", this.diffOnly);
                    configReportContent.endSubSection("SDC", "");
                }
            } else {
                if (!this.reportSDCRelationshipModelOnly) {
                    configReportContent.startSubSection("SDC", "");
                    configReportContent.append(this.renderSDCSummary((SDCRO)sdcRO).toString());
                    configReportContent.appendSubSection(SDCRenderer.renderSDCInfo((SDCRO)sdcRO, translationProcessor), "SDC", this.diffOnly);
                    configReportContent.startSubSection("Security/Update Options", "");
                    configReportContent.appendSubSection(this.renderSecurityOptions((SDCRO)sdcRO, translationProcessor), "Security/Update Options", this.diffOnly);
                    configReportContent.startSubSection("Definition Options", "");
                    configReportContent.appendSubSection(this.renderDefinitionOptions((SDCRO)sdcRO, translationProcessor), "Definition Options", this.diffOnly);
                }
                configReportContent.startSubSection("Tables", "");
                configReportContent.appendSubSection(this.renderTablesInfo(this.reportSDCRelationshipModelOnly, (SDCRO)sdcRO), "Tables", this.diffOnly);
                configReportContent.startSubSection("Columns", "");
                configReportContent.appendSpecialContent(this.renderColumnsInfo(this.reportSDCRelationshipModelOnly, (SDCRO)sdcRO), this.diffOnly);
                if (!this.reportSDCRelationshipModelOnly) {
                    configReportContent.startSubSection("Links", "");
                    configReportContent.appendSubSection(this.renderLinksInfo((SDCRO)sdcRO), "Links", this.diffOnly);
                    configReportContent.startSubSection("Operations", "");
                    configReportContent.appendSubSection(this.renderOperationsInfo((SDCRO)sdcRO), "Operations", this.diffOnly);
                }
                configReportContent.appendSpecialContent(this.renderIndexInfo((SDCRO)sdcRO), this.diffOnly);
                configReportContent.startSubSection("Attributes", "");
                configReportContent.appendSubSection(this.renderAttrsInfo((SDCRO)sdcRO), "Attributes", this.diffOnly);
                configReportContent.startSubSection("SDC Relationship Model", "");
                configReportContent.appendSubSection(this.renderRelationships((SDCRO)sdcRO, true), "SDC Relationship Model", this.diffOnly);
                configReportContent.endSubSection("Summary", "");
            }
            ++subsection;
        }
        if (this.reportDetails && !this.reportSDCRelationshipModelOnly) {
            configReportContent.startSection("Detailed Definition");
            configReportContent.appendSpecialContent(this.renderDetails((SDCRO)sdcRO, (SDCRO)refSdcRO), this.diffOnly);
            configReportContent.endSection();
            ++subsection;
        }
        if (this.reportCategories && !this.reportSDCRelationshipModelOnly) {
            configReportContent.startSubSection("Categories", "");
            ConfigReportContent categories = new ConfigReportContent(this.config, "categories");
            if (!this.includeDiffReport) {
                if (sdcRO != null) {
                    configReportContent.renderCategories(sdcRO.getCategories());
                }
            } else if (sdcRO == null || sdcRO.currentSDI == null) {
                if (refSdcRO != null) {
                    categories.renderCategories(refSdcRO.getCategories());
                }
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            } else if (refSdcRO == null || refSdcRO.currentSDI == null) {
                categories.renderCategories(sdcRO.getCategories());
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            } else {
                categories.renderCategoriesDiff(sdcRO.getCategories(), refSdcRO.getCategories());
                configReportContent.appendSubSection(categories, "Categories", this.diffOnly);
            }
        }
        configReportContent.endSection();
        this.updateSectionChangeInfo("SDC", ConfigReportContent.generateSDISectionTitle(currRO.currentSDI), configReportContent);
        return configReportContent;
    }
}

