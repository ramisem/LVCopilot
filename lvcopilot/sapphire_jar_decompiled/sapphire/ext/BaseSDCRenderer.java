/*
 * Decompiled with CFR 0.152.
 */
package sapphire.ext;

import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.renderer.BaseRenderer;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
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
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseSDCRenderer
extends BaseRenderer {
    protected boolean reportSummary = true;
    protected boolean reportCategories = true;
    protected boolean reportDetails = true;
    protected boolean reportTemplatesOnly = false;
    public static final String OPTION_SDC_SUMMARY = "Include Special Content ";
    public static final String OPTION_SDC_DETAILS = "Include Raw Data";
    public static final String OPTION_TEMPLATES_ONLY = "Include Templates Only";
    public static final String OPTION_SDC_CATEGORIES = "Include Categories";
    protected BaseSDCRO sdcRO;
    protected BaseSDCRO refSdcRO;
    public boolean reportSDIRoleMatrix = true;
    public static final String OPTIONID = "optionid";
    public static final String OPTIONTITLE = "title";

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseSDCRO srcRO, BaseSDCRO refRO, HashMap sdisIncluded, boolean includeDiffReport, boolean includeSDIRoleMatrix) {
        super.initialize(sapphireConnection, config, srcRO, refRO, sdisIncluded, includeDiffReport);
        if (srcRO != null) {
            this.sdcRO = (BaseSDCRO)this.sourceRO;
        }
        if (refRO != null) {
            this.refSdcRO = (BaseSDCRO)this.refRO;
        }
        this.reportSDIRoleMatrix = includeSDIRoleMatrix;
        this.config = config;
    }

    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO srcRO, HashMap sdisIncluded, boolean includeSDIRoleMatrix) {
        super.initialize(sapphireConnection, config, srcRO, sdisIncluded);
        this.sdcRO = (BaseSDCRO)this.sourceRO;
        if (this.refRO != null) {
            this.refSdcRO = (BaseSDCRO)this.refRO;
        }
        this.reportSDIRoleMatrix = includeSDIRoleMatrix;
        this.config = config;
    }

    public void initialize(SapphireConnection sapphireConnection, BaseRO srcRO, BaseRO refRO) {
        super.initialize(sapphireConnection, null, srcRO, refRO, new HashMap(), true);
        this.sdcRO = (BaseSDCRO)this.sourceRO;
        if (refRO != null) {
            this.refSdcRO = (BaseSDCRO)this.refRO;
        }
        this.reportSDIRoleMatrix = false;
        this.config = new PropertyList();
    }

    public BaseSDCRO getSourceRO() {
        return this.sdcRO;
    }

    public BaseSDCRO getReferenceRO() {
        return this.refSdcRO;
    }

    public SDIData getSourceSDIData() {
        return this.sdcRO.currentSDIData;
    }

    public SDIData getReferenceSDIData() {
        return this.refSdcRO.currentSDIData;
    }

    public PropertyListCollection getOptions() {
        PropertyListCollection ret = new PropertyListCollection();
        PropertyList option = new PropertyList();
        option.setProperty(OPTIONID, "IncludeSpecialContent");
        option.setProperty(OPTIONTITLE, "Include Special Content");
        ret.add(option);
        option = new PropertyList();
        option.setProperty(OPTIONID, "IncludeRawContent");
        option.setProperty(OPTIONTITLE, "Include Raw Tables");
        ret.add(option);
        option = new PropertyList();
        option.setProperty(OPTIONID, "IncludeTemplatesOnly");
        option.setProperty(OPTIONTITLE, OPTION_TEMPLATES_ONLY);
        ret.add(option);
        option = new PropertyList();
        option.setProperty(OPTIONID, "IncludeCategories");
        option.setProperty(OPTIONTITLE, OPTION_SDC_CATEGORIES);
        ret.add(option);
        return ret;
    }

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
            if (optionNo != 3) continue;
            selectedOptions.setProperty(OPTION_SDC_CATEGORIES, options.getString(i, "selectedvalue"));
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
    }

    public void setIgnoreDiffs(PropertyListCollection ignorePrimaryDiffs, PropertyListCollection ignoreDetailsDiffs) {
        this.ignorePrimaryDiffs = ignorePrimaryDiffs;
        this.ignoreDetailsDiffs = ignoreDetailsDiffs;
    }

    public void setOptions(PropertyListCollection options) {
        PropertyList selectedOptions = new PropertyList();
        for (int i = 0; i < options.size(); ++i) {
            PropertyList currOptions = options.getPropertyList(i);
            String option = currOptions.getProperty(OPTIONID);
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
            if (!"IncludeCategories".equals(option)) continue;
            selectedOptions.setProperty(OPTION_SDC_CATEGORIES, currOptions.getProperty("optionvalue"));
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
    }

    public boolean checkIfIgnore(BaseSDCRO sdcRO, BaseSDCRO refSdcRO) {
        return this.reportTemplatesOnly && (sdcRO != null && "N".equals(sdcRO.getTemplateFlag()) || refSdcRO != null && "N".equals(refSdcRO.getTemplateFlag()));
    }

    public ConfigReportContent getSectionContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "get section content");
        configReportContent.clearContent();
        BaseSDCRO currRO = sdcRO;
        if (this.includeDiffReport) {
            if (sdcRO == null || sdcRO.currentSDI == null) {
                currRO = refSdcRO;
                configReportContent.startSDISection(refSdcRO.currentSDI, refSdcRO.getDescription());
            } else if (refSdcRO == null || refSdcRO.currentSDI == null) {
                configReportContent.startSDISection(sdcRO.currentSDI, sdcRO.getDescription());
            } else {
                configReportContent.startSDISectionDiff(this.getSDCProcessor(), sdcRO.currentSDIData, sdcRO.currentSDI, sdcRO.getDescription(), refSdcRO.getDescription());
            }
        } else {
            configReportContent.startSDISection(sdcRO.currentSDI, sdcRO.getDescription());
        }
        if (this.reportSummary) {
            if (this.includeDiffReport) {
                if (sdcRO == null || sdcRO.currentSDI == null) {
                    configReportContent.appendSpecialContent(this.getSpecialContent(refSdcRO, translationProcessor), this.diffOnly);
                } else if (refSdcRO == null || refSdcRO.currentSDI == null) {
                    configReportContent.appendSpecialContent(this.getSpecialContent(sdcRO, translationProcessor), this.diffOnly);
                } else {
                    configReportContent.appendSpecialContent(this.getSpecialContent(sdcRO, refSdcRO, translationProcessor), this.diffOnly);
                }
            } else {
                configReportContent.appendSpecialContent(this.getSpecialContent(sdcRO, translationProcessor), this.diffOnly);
            }
        }
        if (this.reportDetails) {
            this.renderDetails(configReportContent, this.reportDetails, translationProcessor);
            this.renderAttributes(configReportContent);
        }
        if (this.reportCategories) {
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
        this.updateSectionChangeInfo(currRO.getSDCSingular(), ConfigReportContent.generateSDISectionTitle(currRO.currentSDI), configReportContent);
        return configReportContent;
    }

    protected ConfigReportContent getSpecialContent(BaseSDCRO sdcRO, BaseSDCRO refSdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        return this.getSpecialContent(sdcRO.currentSDIData, refSdcRO.currentSDIData, translationProcessor);
    }

    protected ConfigReportContent getSpecialContent(SDIData sdiData, SDIData refSDIData, TranslationProcessor translationProcessor) throws SapphireException {
        return new ConfigReportContent(this.config, "get special content");
    }

    protected ConfigReportContent getSpecialContent(BaseSDCRO sdcRO, TranslationProcessor translationProcessor) throws SapphireException {
        return this.getSpecialContent(sdcRO.currentSDIData, translationProcessor);
    }

    protected ConfigReportContent getSpecialContent(SDIData sdiData, TranslationProcessor translationProcessor) throws SapphireException {
        return new ConfigReportContent(this.config, "get special content");
    }

    @Override
    public boolean hasChapterChanged() {
        return this.chapterChanged;
    }

    public void createXMLReport() throws SapphireException {
        FileOutputStream sdiXMLFile;
        OutputStreamWriter writer;
        if (this.sdcRO == null || this.sdcRO.currentSDI == null) {
            return;
        }
        if (this.sdcRO.currentSDIPosition == 0) {
            FileOutputStream colDataXMLFile;
            FileOutputStream sdcpropsXMLFile;
            String xmlSDCProps = this.sdcRO.getCurrentSDCProperties().toXMLString();
            String xmlSDCFileName = ConfigReportContent.generateSectionXMLFileName(this.sdcRO.getSDCName() + "_sdc", "props");
            try {
                sdcpropsXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlSDCFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlSDCFileName);
            }
            try {
                writer = new OutputStreamWriter((OutputStream)sdcpropsXMLFile, "UTF-8");
                writer.write(xmlSDCProps);
                writer.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
            String xmlColumnData = this.sdcRO.getColumnData().toXML();
            String xmlColDataFileName = ConfigReportContent.generateSectionXMLFileName(this.sdcRO.getSDCName() + "_column", "data");
            try {
                colDataXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlColDataFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report xml file " + xmlColDataFileName);
            }
            try {
                OutputStreamWriter writer2 = new OutputStreamWriter((OutputStream)colDataXMLFile, "UTF-8");
                writer2.write(xmlColumnData);
                writer2.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
        }
        if (this.sdcRO.currentSDIData == null) {
            throw new SapphireException("Cannot create xml report for sdcid:" + this.sdcRO.currentSDI.getSdcid() + " keyid1:" + this.sdcRO.currentSDI.getKeyid1());
        }
        String xmlReportContent = "";
        if (this.sdcRO.currentSDI.getSdcid().equals("PropertyTree")) {
            String valuetree = this.sdcRO.getPrimaryValue("valuetree");
            String deftree = this.sdcRO.getPrimaryValue("definitiontree");
            this.sdcRO.currentSDIData.getDataset("primary").setString(0, "valuetree", "");
            this.sdcRO.currentSDIData.getDataset("primary").setString(0, "definitiontree", "");
            xmlReportContent = this.sdcRO.currentSDIData.toXML();
            this.sdcRO.currentSDIData.getDataset("primary").setString(0, "valuetree", valuetree);
            this.sdcRO.currentSDIData.getDataset("primary").setString(0, "definitiontree", deftree);
        } else {
            xmlReportContent = this.sdcRO.currentSDIData.toXML();
        }
        String xmlFileName = ConfigReportContent.generateSDISectionXMLFileName(this.sdcRO.currentSDI);
        try {
            sdiXMLFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + xmlFileName);
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report xml file " + xmlFileName);
        }
        try {
            writer = new OutputStreamWriter((OutputStream)sdiXMLFile, "UTF-8");
            writer.write(xmlReportContent);
            writer.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    public ArrayList getSDIsIncluded() {
        String currSDC = "";
        currSDC = this.sdcRO != null ? this.sdcRO.getSDCName() : this.refSdcRO.getSDCName();
        if (this.sdisIncluded.get(currSDC) == null) {
            Trace.log("No SDIs included for SDC:" + currSDC);
            return null;
        }
        return (ArrayList)this.sdisIncluded.get(currSDC);
    }

    @Override
    public ArrayList getSectionList() {
        ArrayList<String> sectionNames = new ArrayList<String>();
        String currSDC = "";
        currSDC = this.sdcRO != null ? this.sdcRO.getSDCName() : this.refSdcRO.getSDCName();
        if (this.sdisIncluded.get(currSDC) == null) {
            Trace.log("No SDIs included for SDC:" + currSDC);
            return sectionNames;
        }
        ArrayList mergedSdiList = (ArrayList)this.sdisIncluded.get(currSDC);
        for (int i = 0; i < mergedSdiList.size(); ++i) {
            SDI currentSDI = (SDI)mergedSdiList.get(i);
            if (this.sdcRO != null) {
                this.sdcRO.gotoSection(currentSDI);
            }
            if (this.refSdcRO != null) {
                this.refSdcRO.gotoSection(currentSDI);
            }
            if (this.checkIfIgnore(this.sdcRO, this.refSdcRO)) continue;
            String sectionName = ConfigReportContent.generateSDISectionTitle(currentSDI);
            sectionNames.add(sectionName);
        }
        if (this.reportSDIRoleMatrix) {
            sectionNames.add("SDI Role Matrix");
        }
        if (this.hasCustomSections()) {
            ArrayList customSections = this.getCustomSectionNames();
            for (int j = 0; j < customSections.size(); ++j) {
                sectionNames.add(customSections.get(j).toString());
            }
        }
        return sectionNames;
    }

    @Override
    public ArrayList getSectionTitleList() {
        return this.getSectionList();
    }

    public boolean hasCustomSections() {
        return false;
    }

    public ArrayList getCustomSectionNames() {
        return new ArrayList();
    }

    public ConfigReportContent getCustomSectionContent(String customSectionName) {
        return new ConfigReportContent(this.config, "get custom section content: " + customSectionName);
    }

    public static String getPrimaryValue(SDIData sdiData, String columnName) {
        if (sdiData == null || sdiData.getDataset("primary") == null) {
            return "";
        }
        return sdiData.getDataset("primary").getValue(0, columnName);
    }

    protected void renderDetails(ConfigReportContent content, boolean reportDetailsTables, TranslationProcessor translationProcessor) {
        content.startSection("Detailed Definition");
        content.startSubSection("Primary", "");
        content.appendSubSection(this.renderPrimary("", translationProcessor), "Primary", this.diffOnly);
        if (reportDetailsTables) {
            this.renderSDIDetails(content);
        }
    }

    protected ConfigReportContent renderDetails(boolean reportDetailsTables, String hideValueFor, TranslationProcessor translationProcessor) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "render details");
        configReportContent.startSubSection("Primary", "");
        configReportContent.appendSubSection(this.renderPrimary(hideValueFor, translationProcessor), "Primary", this.diffOnly);
        if (reportDetailsTables) {
            this.renderSDIDetails(configReportContent);
        }
        return configReportContent;
    }

    private ConfigReportContent renderPrimary(String hideColumnValue, TranslationProcessor translationProcessor) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "render primary");
        configReportContent.startTable();
        if (this.includeDiffReport) {
            if (this.sdcRO == null || this.sdcRO.currentSDI == null) {
                configReportContent.append(this.renderROPrimary(this.refSdcRO, hideColumnValue, translationProcessor).toString());
            } else if (this.refSdcRO == null || this.refSdcRO.currentSDI == null) {
                configReportContent.append(this.renderROPrimary(this.sdcRO, hideColumnValue, translationProcessor).toString());
            } else {
                configReportContent.append(this.renderROPrimaryDiff(hideColumnValue, translationProcessor).toString());
            }
        } else {
            configReportContent.append(this.renderROPrimary(this.sdcRO, hideColumnValue, translationProcessor).toString());
        }
        configReportContent.endTable();
        return configReportContent;
    }

    private ConfigReportContent renderPrimary(TranslationProcessor translationProcessor) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "");
        configReportContent.startTable();
        if (this.includeDiffReport) {
            if (this.sdcRO == null || this.sdcRO.currentSDI == null) {
                configReportContent.append(this.renderROPrimary(this.refSdcRO, "", translationProcessor).toString());
            } else if (this.refSdcRO == null || this.refSdcRO.currentSDI == null) {
                configReportContent.append(this.renderROPrimary(this.sdcRO, "", translationProcessor).toString());
            } else {
                configReportContent.append(this.renderROPrimaryDiff(translationProcessor).toString());
            }
        } else {
            configReportContent.append(this.renderROPrimary(this.sdcRO, translationProcessor).toString());
        }
        configReportContent.endTable();
        return configReportContent;
    }

    private ConfigReportContent renderROPrimary(BaseSDCRO ro, TranslationProcessor translationProcessor) {
        return this.renderROPrimary(ro, "", translationProcessor);
    }

    private ConfigReportContent getPrimaryValue(BaseSDCRO ro, String column) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "column:" + column);
        String val = ro.getPrimaryValue(column);
        if (val.toLowerCase().startsWith("<propertylist")) {
            try {
                PropertyList pl = new PropertyList();
                pl.setPropertyList(val, false, false);
                configReportContent.renderPropertyList(pl, false).toString();
            }
            catch (SapphireException e) {
                Trace.log("Cannot convert value of " + column + " to propertylist ");
            }
        } else if (val.toLowerCase().startsWith("<dataset")) {
            DataSet ds = new DataSet(val);
            configReportContent.renderListTable(ds, this.getTranslationProcessor());
        } else {
            configReportContent.append(val);
        }
        return configReportContent;
    }

    public String getSDCId() {
        if (this.sdcRO != null && this.sdcRO.currentSDI != null) {
            return this.sdcRO.currentSDI.sdcid;
        }
        if (this.refSdcRO != null && this.refSdcRO.currentSDI != null) {
            return this.refSdcRO.currentSDI.sdcid;
        }
        return "";
    }

    public boolean isClob(String columnName) {
        String type = this.getSDCProcessor().getSDCColumnProperty(this.getSDCId(), columnName, "datatype");
        return type.equals("T");
    }

    private ConfigReportContent renderROPrimary(BaseSDCRO ro, String hideColumnValue, TranslationProcessor translationProcessor) {
        String currlabel;
        String currColumn;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "primary");
        ArrayList columns = ro.getPrimaryColumns();
        ArrayList columnLabels = ro.getPrimaryColumnLabels();
        configReportContent.startTable();
        int i = 0;
        int currrowitems = 0;
        while (i < columns.size()) {
            if (currrowitems == 2) {
                currrowitems = 0;
                configReportContent.startRow();
            }
            currColumn = (String)columns.get(i);
            if (this.isClob(columns.get(i).toString())) {
                ++i;
            } else {
                String value = ro.getPrimaryValue(currColumn);
                if (currColumn.equals(hideColumnValue)) {
                    value = "[Hidden]";
                }
                currlabel = columnLabels.get(i).toString();
                ConfigReportContent rowitem = new ConfigReportContent("rowitem", translationProcessor);
                rowitem.addRowItem(currlabel, value, false);
                configReportContent.append(rowitem.toString());
                ++currrowitems;
                ++i;
            }
            if (currrowitems != 2) continue;
            configReportContent.endRow();
        }
        configReportContent.endTable();
        for (i = 0; i < columns.size(); ++i) {
            currColumn = (String)columns.get(i);
            if (!this.isClob(columns.get(i).toString())) continue;
            String sourcevalue = ro.getPrimaryValue(currColumn);
            currlabel = columnLabels.get(i).toString();
            if (sourcevalue.length() <= 0) continue;
            String diffval = configReportContent.getFormattedDiffVal(currColumn, sourcevalue, sourcevalue, true, translationProcessor);
            if (diffval.length() > 0) {
                configReportContent.startSubSection(currlabel, "");
                configReportContent.append("<table>");
                configReportContent.append("<tr>");
                configReportContent.append("<td>");
                configReportContent.append(diffval);
                configReportContent.append("</td>");
                configReportContent.append("</tr>");
                configReportContent.append("</table>");
                continue;
            }
            configReportContent.startSubSection(currlabel, "");
            configReportContent.startTable();
            configReportContent.append("<tr><td>No contents</td></tr>");
            configReportContent.endTable();
        }
        return configReportContent;
    }

    protected boolean ignoreDiff(String currCol) {
        if (this.ignorePrimaryDiffs == null || this.ignorePrimaryDiffs.size() == 0) {
            return false;
        }
        for (int i = 0; i < this.ignorePrimaryDiffs.size(); ++i) {
            String columnid = this.ignorePrimaryDiffs.getPropertyList(i).getProperty("columnid");
            if (!columnid.equals(currCol)) continue;
            return true;
        }
        return false;
    }

    private ConfigReportContent renderROPrimaryDiff(String hideColumnValue, TranslationProcessor translationProcessor) {
        String currColumn;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "primary");
        ArrayList columns = this.sdcRO.getPrimaryColumns();
        ArrayList columnLabels = this.sdcRO.getPrimaryColumnLabels();
        int i = 0;
        int currrowitems = 0;
        while (i < columns.size()) {
            if (currrowitems == 2) {
                currrowitems = 0;
                configReportContent.startRow();
            }
            currColumn = (String)columns.get(i);
            if (this.isClob(columns.get(i).toString())) {
                ++i;
            } else {
                String value = BaseSDCRenderer.getPrimaryValue(this.sdcRO.currentSDIData, currColumn);
                if (currColumn.equals(hideColumnValue)) {
                    value = "[Hidden]";
                }
                String currlabel = columnLabels.get(i).toString();
                currlabel = translationProcessor.translate(currlabel);
                ConfigReportContent rowitem = new ConfigReportContent("rowitem", translationProcessor);
                rowitem.addDiffRowItem(currlabel, value, BaseSDCRenderer.getPrimaryValue(this.refSdcRO.currentSDIData, currColumn), 1, false, translationProcessor, false);
                configReportContent.append(rowitem.toString());
                ++currrowitems;
                ++i;
            }
            if (currrowitems != 2) continue;
            configReportContent.endRow();
        }
        configReportContent.endTable();
        for (i = 0; i < columns.size(); ++i) {
            currColumn = (String)columns.get(i);
            if (!this.isClob(columns.get(i).toString())) continue;
            String sourcevalue = BaseSDCRenderer.getPrimaryValue(this.sdcRO.currentSDIData, currColumn);
            String refvalue = BaseSDCRenderer.getPrimaryValue(this.refSdcRO.currentSDIData, currColumn);
            String currlabel = columnLabels.get(i).toString();
            if (sourcevalue.length() <= 0 && refvalue.length() <= 0) continue;
            currlabel = translationProcessor.translate(currlabel);
            String diffval = configReportContent.getFormattedDiffVal(currColumn, sourcevalue, refvalue, true, translationProcessor);
            if (diffval.length() > 0) {
                configReportContent.startSubSection(currlabel, "");
                configReportContent.append("<table>");
                configReportContent.append("<tr>");
                configReportContent.append("<td>");
                configReportContent.append(diffval);
                configReportContent.append("</td>");
                configReportContent.append("</tr>");
                configReportContent.append("</table>");
                continue;
            }
            configReportContent.startSubSection(currlabel, "");
            configReportContent.startTable();
            configReportContent.append("<tr><td>No contents</td></tr>");
            configReportContent.endTable();
        }
        return configReportContent;
    }

    private ConfigReportContent renderROPrimaryDiff(TranslationProcessor translationProcessor) {
        return this.renderROPrimaryDiff("", translationProcessor);
    }

    private void renderSDIDetails(ConfigReportContent content) {
        if (this.includeDiffReport) {
            if (this.sdcRO == null || this.sdcRO.currentSDI == null) {
                this.renderRODetails(content, this.refSdcRO);
            } else if (this.refSdcRO == null || this.refSdcRO.currentSDI == null) {
                this.renderRODetails(content, this.sdcRO);
            } else {
                this.renderRODetailsDiff(content);
            }
        } else {
            this.renderRODetails(content, this.sdcRO);
        }
    }

    public DataSet removeAuditColumns(DataSet orig) {
        DataSet clean = new DataSet();
        if (orig == null) {
            orig = new DataSet();
        }
        String[] cols = orig.getColumns();
        for (int i = 0; i < cols.length; ++i) {
            if (cols[i].equals("createdt") || cols[i].equals("moddt") || cols[i].equals("modby") || cols[i].equals("createby") || cols[i].equals("createtool") || cols[i].equals("modtool") || cols[i].equals("auditsequence") || cols[i].equals("usersequence") || cols[i].equals("tracelogid")) continue;
            String values = orig.getColumnValues(cols[i], "|!|");
            clean.addColumn(cols[i], orig.getColumnType(cols[i]));
            clean.addColumnValues(cols[i], orig.getColumnType(cols[i]), values, "|!|");
        }
        return clean;
    }

    private void renderRODetails(ConfigReportContent configReportContent, BaseSDCRO ro) {
        String[] detailTables = ro.getDetailTables();
        if (detailTables != null) {
            for (int i = 0; i < detailTables.length; ++i) {
                if (ro.getDataSet(detailTables[i]) == null || ro.getDataSet(detailTables[i]).getRowCount() <= 0) continue;
                configReportContent.startSubSection(detailTables[i], "There are " + ro.getDataSet(detailTables[i]).getRowCount() + " items. ");
                DataSet clean = this.removeAuditColumns(ro.getDataSet(detailTables[i]));
                ConfigReportContent detailTableContent = new ConfigReportContent(this.config, detailTables[i]);
                detailTableContent.renderListTable(clean, this.getTranslationProcessor());
                String linkid = ro.getLinkid(detailTables[i]);
                PropertyListCollection detailLinkProps = this.getSDCProcessor().getDetailLinks(ro.getSDCName());
                detailTableContent.appendSubSection(this.renderDetailDetails(ro, linkid, detailLinkProps), detailTables[i] + " Raw Details", this.diffOnly);
                configReportContent.appendSubSection(detailTableContent, detailTables[i], this.diffOnly);
            }
        }
    }

    private ConfigReportContent renderDetailDetails(BaseSDCRO ro, String detailtableid, PropertyListCollection detailLinksProps) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "details of details");
        if (detailLinksProps != null && detailLinksProps.size() > 0) {
            for (int i = 0; i < detailLinksProps.size(); ++i) {
                String detailLinkTableId;
                PropertyList detailLinkProp = (PropertyList)detailLinksProps.get(i);
                String linkType = detailLinkProp.getProperty("linktype");
                String parentLinkId = detailLinkProp.getProperty("linkid");
                if (!detailtableid.equalsIgnoreCase(parentLinkId) || !"D".equals(linkType) || ro.getDataSet(detailLinkTableId = detailLinkProp.getProperty("linktableid")).getRowCount() <= 0) continue;
                configReportContent.startSubHeading(detailLinkTableId, "There are " + ro.getDataSet(detailLinkTableId).getRowCount() + " items. ");
                DataSet clean = this.removeAuditColumns(ro.getDataSet(detailLinkTableId));
                configReportContent.renderListTable(clean, this.getTranslationProcessor());
            }
        }
        return configReportContent;
    }

    private ConfigReportContent renderRODetailsDiff(ConfigReportContent configReportContent) {
        String[] detailTables = this.sdcRO.getDetailTables();
        if (detailTables == null) {
            return configReportContent;
        }
        for (int i = 0; i < detailTables.length; ++i) {
            if (this.sdcRO.getDataSet(detailTables[i]) == null || this.sdcRO.getDataSet(detailTables[i]).getRowCount() <= 0) continue;
            ConfigReportContent detailTableContent = new ConfigReportContent(this.config, detailTables[i]);
            detailTableContent.startSubSection(detailTables[i], "There are " + this.sdcRO.getDataSet(detailTables[i]).getRowCount() + " items. ");
            if (!this.includeDiffReport || this.refSdcRO == null || this.refSdcRO.currentSDI == null) {
                detailTableContent.renderListTable(this.sdcRO.getDataSet(detailTables[i]), this.getTranslationProcessor());
            } else {
                String[] keycols = this.sdcRO.getDataSetKeyCols(detailTables[i]);
                DataSet src = this.removeAuditColumns(this.sdcRO.getDataSet(detailTables[i]));
                DataSet ref = this.removeAuditColumns(this.refSdcRO.getDataSet(detailTables[i]));
                PropertyListCollection ignoreDiffCols = this.getIgnoreDetailsDiffCols(detailTables[i]);
                detailTableContent.renderDiffListTable(src, ref, keycols, ignoreDiffCols, this.getTranslationProcessor());
            }
            configReportContent.appendSubSection(detailTableContent, detailTables[i], this.diffOnly);
        }
        return configReportContent;
    }

    public void renderAttributes(ConfigReportContent content) {
        if (this.includeDiffReport) {
            if (this.sdcRO == null || this.sdcRO.currentSDI == null) {
                this.renderROAttributes(content, this.refSdcRO);
            } else if (this.refSdcRO == null || this.refSdcRO.currentSDI == null) {
                this.renderROAttributes(content, this.sdcRO);
            } else {
                this.renderROAttributesDiff(content);
            }
        } else {
            this.renderROAttributes(content, this.sdcRO);
        }
    }

    private void renderROAttributes(ConfigReportContent content, BaseSDCRO ro) {
        ConfigReportContent attributesContent = new ConfigReportContent(this.config, "attributes");
        SDIData cfr_ignored_0 = ro.currentSDIData;
        DataSet attributes = ro.getDataSet("attribute");
        if (attributes == null || attributes.getRowCount() == 0) {
            return;
        }
        attributesContent.startSubSection("Attributes", "There are " + attributes.getRowCount() + " items. ");
        DataSet clean = this.removeAuditColumns(attributes);
        attributesContent.renderListTable(clean, this.getTranslationProcessor());
        content.appendSubSection(attributesContent, "Attributes", this.diffOnly);
    }

    private void renderROAttributesDiff(ConfigReportContent content) {
        ConfigReportContent attributesContent = new ConfigReportContent(this.config, "attributes");
        SDIData cfr_ignored_0 = this.sdcRO.currentSDIData;
        DataSet sdcattrs = this.sdcRO.getDataSet("attribute");
        if (sdcattrs == null || sdcattrs.getRowCount() == 0) {
            return;
        }
        if (sdcattrs.getRowCount() > 0) {
            attributesContent.startSubHeading("Attributes", "There are " + sdcattrs.getRowCount() + " items. ");
            if (!this.includeDiffReport || this.refSdcRO == null || this.refSdcRO.currentSDI == null) {
                SDIData cfr_ignored_1 = this.sdcRO.currentSDIData;
                attributesContent.renderListTable(this.sdcRO.getDataSet("attribute"), this.getTranslationProcessor());
            } else {
                SDIData cfr_ignored_2 = this.sdcRO.currentSDIData;
                String[] keycols = this.sdcRO.currentSDIData.getKeys("attribute");
                SDIData cfr_ignored_3 = this.sdcRO.currentSDIData;
                DataSet src = this.removeAuditColumns(this.sdcRO.getDataSet("attribute"));
                SDIData cfr_ignored_4 = this.sdcRO.currentSDIData;
                DataSet ref = this.removeAuditColumns(this.refSdcRO.getDataSet("attribute"));
                attributesContent.renderDiffListTable(src, ref, keycols);
            }
            content.appendSubSection(attributesContent, "Attributes", this.diffOnly);
        }
    }
}

