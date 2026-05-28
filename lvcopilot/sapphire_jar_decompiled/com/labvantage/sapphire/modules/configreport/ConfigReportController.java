/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.SDI;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.ConfigReportPolicyDef;
import com.labvantage.sapphire.modules.configreport.renderer.BaseRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.misc.CoverPageRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.misc.MenuSystemRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.misc.SysConfigRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.misc.TOCRenderer;
import com.labvantage.sapphire.modules.configreport.renderer.sdc.WebPageRenderer;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.CoverPageRO;
import com.labvantage.sapphire.modules.configreport.ro.MenuSystemRO;
import com.labvantage.sapphire.modules.configreport.ro.SysConfigRO;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.SDIProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ConfigReportController {
    public static final String PROPERTY_SDC = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_POLICYNODE = "policynode";
    public static final String PROPERTY_REPORT_FILENAME = "filename";
    public static final String PROPERTY_REPORT_FOLDER = "folder";
    public static final String PROPERTY_FRAMES = "frames";
    public static final String PROPERTY_CREATEDBY = "createdby";
    public static final String PROPERTY_CREATE_DIFF_REPORT = "includediffreport";
    public static final String PROPERTY_GENERATE_DIFFONLY_REPORT = "diffonlyreport";
    public static final String PROPERTY_REFERENCE_REPORT_FOLDER = "refreportfolder";
    public static final String PROPERTY_CUSTOM_RENDERER_PACKAGE = "customrendererpackage";
    public static final String PROPERTY_MENUSYSTEM_INCLUDED = "includegenericlayout";
    public static final String PROPERTY_SYSCONFIG_INCLUDED = "includesysconfig";
    public static final String PROPERTY_CATEGORY = "category";
    public static final String PROPERTY_EXCLUDE_SDC = "excludesdcid";
    public static final String PROPERTY_SDIROLEMATRIX_INCLUDED = "includesdirolematrix";
    public static final String PROPERTY_PAGEBUTTON_ROLEMATRIX_INCLUDED = "includepagebuttonrolematrix";
    public static final String PROPERTY_SITEMAP_PAGEBUTTONROLEMATRIX = "includesitemappagebuttonrolematrix";
    private String reportFileName;
    private String folder;
    private boolean frames;
    private ConfigReportPolicyDef policyDef;
    private boolean sourceMenuSystemIncluded;
    private boolean sourceSysconfigIncluded;
    private boolean refMenuSystemIncluded;
    private boolean refSysconfigIncluded;
    private String createdBy;
    private String customRendererPackage;
    private PropertyList config;
    private ConfigReportContent configReportContent;
    private SapphireConnection sapphireConnection;
    private boolean includeXMLReport;
    private boolean includeDiffReport;
    private boolean fromListPage;
    private boolean diffOnly;
    private String refReportFolder;
    private QueryProcessor queryProcessor;
    private SDCProcessor sdcProcessor;
    private SDIProcessor sdiProcessor;
    private TranslationProcessor translationProcessor;
    private ArrayList sourceSDCList;
    private ArrayList refSDCList;
    private ArrayList mergedSdcList;
    private HashMap sourceSdisIncluded;
    private HashMap refSdisIncluded;
    private HashMap allSdis;
    private ArrayList reportRendererList;
    private ArrayList sourceROList;
    private ArrayList refROList;
    private boolean includeSDIRoleMatrix;
    private boolean includeSiteMapPageButtonRoleMatrix;
    private boolean includeWebPagePageButtonRoleMatrix;
    private PropertyList tocRendererOptions;

    public ConfigReportController(PropertyList config, SapphireConnection sapphireConnection) throws SapphireException {
        this.sapphireConnection = sapphireConnection;
        this.queryProcessor = new QueryProcessor(sapphireConnection.getConnectionId());
        this.sdcProcessor = new SDCProcessor(sapphireConnection.getConnectionId());
        this.sdiProcessor = new SDIProcessor(sapphireConnection.getConnectionId());
        this.translationProcessor = new TranslationProcessor(sapphireConnection.getConnectionId());
        this.reportFileName = config.getProperty(PROPERTY_REPORT_FILENAME);
        this.folder = config.getProperty(PROPERTY_REPORT_FOLDER);
        this.frames = "Y".equals(config.getProperty(PROPERTY_FRAMES, "Y"));
        this.customRendererPackage = config.getProperty(PROPERTY_CUSTOM_RENDERER_PACKAGE);
        this.createdBy = config.getProperty(PROPERTY_CREATEDBY);
        this.config = config;
        this.includeXMLReport = true;
        this.includeDiffReport = "Y".equals(config.getProperty(PROPERTY_CREATE_DIFF_REPORT, "N"));
        this.diffOnly = this.includeDiffReport ? "Y".equals(config.getProperty(PROPERTY_GENERATE_DIFFONLY_REPORT, "N")) : false;
        boolean bl = this.fromListPage = config.getProperty(PROPERTY_SDC, "").length() > 0;
        if (this.includeDiffReport) {
            this.refReportFolder = config.getProperty(PROPERTY_REFERENCE_REPORT_FOLDER, "");
        }
        this.sourceSdisIncluded = new HashMap();
        if (this.fromListPage) {
            this.sourceSDCList = new ArrayList();
            String[] sdcs = StringUtil.split(config.getProperty(PROPERTY_SDC, ""), ";");
            for (int i = 0; i < sdcs.length; ++i) {
                this.sourceSDCList.add(sdcs[i]);
            }
            DataSet ds = new DataSet();
            ds.addColumnValues(PROPERTY_KEYID1, 0, config.getProperty(PROPERTY_KEYID1, ""), ";");
            ds.addColumnValues(PROPERTY_KEYID2, 0, config.getProperty(PROPERTY_KEYID2, ""), ";");
            ds.addColumnValues(PROPERTY_KEYID3, 0, config.getProperty(PROPERTY_KEYID3, ""), ";");
            ds.addColumnValues(PROPERTY_SDC, 0, this.sourceSDCList.get(0).toString(), ";");
            ds.padColumn(PROPERTY_SDC);
            this.sourceMenuSystemIncluded = false;
            this.sourceSysconfigIncluded = false;
            this.sourceSdisIncluded.put(config.getProperty(PROPERTY_SDC, ""), this.convertToSDIList(ds, false));
            this.mergedSdcList = this.sourceSDCList;
            this.allSdis = this.sourceSdisIncluded;
            this.includeSDIRoleMatrix = true;
            this.includeSiteMapPageButtonRoleMatrix = false;
            this.includeWebPagePageButtonRoleMatrix = true;
        } else {
            String policyNode = config.getProperty(PROPERTY_POLICYNODE, "");
            if (policyNode == null || policyNode.length() == 0) {
                throw new SapphireException("Please choose the policy node to execute the report");
            }
            this.policyDef = new ConfigReportPolicyDef(new ConfigurationProcessor(sapphireConnection.getConnectionId()).getPolicy("ConfigReportPolicy", policyNode));
            this.tocRendererOptions = this.policyDef.getTOCRendererOptions();
            if (!this.includeDiffReport) {
                this.sourceMenuSystemIncluded = this.policyDef.getIncludeGenericLayout();
                this.sourceSysconfigIncluded = this.policyDef.getIncludeSysConfig();
                this.determineSdcSdiList();
                this.includeSDIRoleMatrix = this.policyDef.getIncludeSDIRoleMatrix();
                this.includeSiteMapPageButtonRoleMatrix = this.policyDef.getIncludeSiteMapPageButtonRoleMatrix();
                this.includeWebPagePageButtonRoleMatrix = this.policyDef.getIncludeWebPagePageButtonRoleMatrix();
            } else {
                PropertyList refPolicy = this.loadReferencePolicyDetailsIfExists(this.refReportFolder);
                if (refPolicy != null) {
                    ConfigReportPolicyDef refPolicyDef = new ConfigReportPolicyDef(refPolicy);
                    if (!this.checkCompatibility(this.policyDef, refPolicyDef)) {
                        throw new SapphireException("The policy node chosen and the reference report are not compatible");
                    }
                    this.sourceMenuSystemIncluded = this.policyDef.getIncludeGenericLayout();
                    this.sourceSysconfigIncluded = this.policyDef.getIncludeSysConfig();
                    this.determineSdcSdiList();
                    this.includeSDIRoleMatrix = this.policyDef.getIncludeSDIRoleMatrix();
                    this.includeSiteMapPageButtonRoleMatrix = this.policyDef.getIncludeSiteMapPageButtonRoleMatrix();
                    this.includeWebPagePageButtonRoleMatrix = this.policyDef.getIncludeWebPagePageButtonRoleMatrix();
                } else {
                    String excludeSdc;
                    PropertyList refConfig = this.loadConfigDetails(this.refReportFolder);
                    PropertyList refReportOptions = this.loadReportOptions(this.refReportFolder);
                    String[] categoryList = null;
                    String[] excludeSdcList = null;
                    String category = refConfig.getProperty(PROPERTY_CATEGORY, "");
                    if (category.length() > 0) {
                        categoryList = StringUtil.split(category, ";");
                    }
                    if ((excludeSdc = refConfig.getProperty(PROPERTY_EXCLUDE_SDC, "")).length() > 0) {
                        excludeSdcList = StringUtil.split(excludeSdc, ";");
                    }
                    if (!this.checkCompatibility(this.policyDef, category, excludeSdc, refConfig)) {
                        throw new SapphireException("The policy node chosen and the reference report are not compatible");
                    }
                    this.sourceSdisIncluded = this.determineSDIHashMap(excludeSdcList, categoryList, refReportOptions);
                    this.sourceSDCList = this.getDistinctSDCList(this.sourceSdisIncluded);
                    this.sourceMenuSystemIncluded = "Y".equals(refConfig.getProperty(PROPERTY_MENUSYSTEM_INCLUDED, "N"));
                    this.sourceSysconfigIncluded = "Y".equals(refConfig.getProperty(PROPERTY_SYSCONFIG_INCLUDED, "N"));
                    this.includeSDIRoleMatrix = "Y".equals(refConfig.getProperty(PROPERTY_SDIROLEMATRIX_INCLUDED, "N"));
                    this.includeSiteMapPageButtonRoleMatrix = "Y".equals(refConfig.getProperty(PROPERTY_SITEMAP_PAGEBUTTONROLEMATRIX, "N"));
                    this.includeWebPagePageButtonRoleMatrix = "Y".equals(refConfig.getProperty(PROPERTY_PAGEBUTTON_ROLEMATRIX_INCLUDED, "N"));
                    if (this.includeDiffReport) {
                        this.refSDCList = this.determineSDCList(excludeSdcList, this.refReportFolder);
                        this.refSdisIncluded = this.determineSDIHashMap(excludeSdcList, this.refSDCList, this.refReportFolder);
                        this.refMenuSystemIncluded = this.checkIfExistsInTOC("Menu System", this.refReportFolder);
                        this.refSysconfigIncluded = this.checkIfExistsInTOC("System Configuration", this.refReportFolder);
                        this.mergedSdcList = this.mergeSDCLists(this.sourceSDCList, this.refSDCList);
                        this.allSdis = this.mergeSdis(this.mergedSdcList, this.sourceSdisIncluded, this.refSdisIncluded);
                    } else {
                        this.mergedSdcList = this.sourceSDCList;
                        this.allSdis = this.sourceSdisIncluded;
                    }
                }
            }
        }
        this.configReportContent = new ConfigReportContent(config, "Controller");
        this.sourceROList = this.getSourceROList();
        this.refROList = null;
        if (this.includeDiffReport) {
            this.refROList = this.getRefROList();
        }
        this.reportRendererList = this.getRendererList(this.sourceROList, this.refROList);
    }

    private boolean checkCompatibility(ConfigReportPolicyDef sourcePolicyDef, String refCategoryList, String refExcludeSdcList, PropertyList refConfig) {
        if (sourcePolicyDef.getIncludeGenericLayout() && !"Y".equals(refConfig.getProperty(PROPERTY_MENUSYSTEM_INCLUDED, "N"))) {
            Trace.log("Menu System chapter not included in reference report");
            return false;
        }
        if (sourcePolicyDef.getIncludeSiteMapPageButtonRoleMatrix() && !"Y".equals(refConfig.getProperty(PROPERTY_SITEMAP_PAGEBUTTONROLEMATRIX, "N"))) {
            Trace.log("Sitemap Page Button Role Matrix not included in reference report");
            return false;
        }
        if (sourcePolicyDef.getIncludeSysConfig() && !"Y".equals(refConfig.getProperty(PROPERTY_SYSCONFIG_INCLUDED, "N"))) {
            Trace.log("System Configuration chapter not included in reference report");
            return false;
        }
        if (sourcePolicyDef.getIncludeWebPagePageButtonRoleMatrix() && !"Y".equals(refConfig.getProperty(PROPERTY_PAGEBUTTON_ROLEMATRIX_INCLUDED, "N"))) {
            Trace.log("WebPage Page Button Role Matrix not included in reference report");
            return false;
        }
        if (refCategoryList != null && refCategoryList.length() > 0) {
            String selectionType = sourcePolicyDef.getSelectionType();
            if (!selectionType.equals("Categories")) {
                Trace.log("The categories used by the source and reference reports do not match.");
                return false;
            }
            String sourceCategoryList = sourcePolicyDef.getCategoryList();
            if (!this.compareCategoryLists(sourceCategoryList, refCategoryList)) {
                Trace.log("The categories used by the source and reference reports do not match.");
                return false;
            }
        }
        return true;
    }

    private boolean checkCompatibility(ConfigReportPolicyDef sourcePolicyDef, ConfigReportPolicyDef refPolicyDef) throws SapphireException {
        if (sourcePolicyDef.getIncludeGenericLayout() && !refPolicyDef.getIncludeGenericLayout()) {
            Trace.log("Menu System chapter not included in reference report");
            return false;
        }
        if (sourcePolicyDef.getIncludeSiteMapPageButtonRoleMatrix() && !refPolicyDef.getIncludeSiteMapPageButtonRoleMatrix()) {
            Trace.log("Sitemap Page Button Role Matrix not included in reference report");
            return false;
        }
        if (sourcePolicyDef.getIncludeSysConfig() && !refPolicyDef.getIncludeSysConfig()) {
            Trace.log("System Configuration chapter not included in reference report");
            return false;
        }
        if (sourcePolicyDef.getIncludeWebPagePageButtonRoleMatrix() && !refPolicyDef.getIncludeWebPagePageButtonRoleMatrix()) {
            Trace.log("WebPage Page Button Role Matrix not included in reference report");
            return false;
        }
        if (sourcePolicyDef.getSelectionType().equals("Categories")) {
            String refCategoryList;
            if (!refPolicyDef.getSelectionType().equals("Categories")) {
                Trace.log("The selection type of the source and ref reports does not match.");
                return false;
            }
            String sourceCategoryList = sourcePolicyDef.getCategoryList();
            if (!this.compareCategoryLists(sourceCategoryList, refCategoryList = refPolicyDef.getCategoryList())) {
                Trace.log("The categories used by the source and reference reports do not match.");
                return false;
            }
            return true;
        }
        String refSelType = refPolicyDef.getSelectionType();
        if (!refSelType.equals("Categories") && !refSelType.equals("SDCs")) {
            Trace.log("The source selectiontype does not match with ref selection type");
            return false;
        }
        if (refSelType.equals("Categories")) {
            PropertyListCollection selectedSdcs = sourcePolicyDef.getSelectedSdcs();
            String refCategoryList = refPolicyDef.getCategoryList();
            for (int i = 0; i < selectedSdcs.size(); ++i) {
                PropertyList currSdcOptions = selectedSdcs.getPropertyList(i);
                String selectionType = currSdcOptions.getProperty("selectiontype");
                if (!selectionType.equals("Categories")) {
                    Trace.log("The categories used by the source and reference reports do not match.");
                    return false;
                }
                String sourceCategoryList = this.getCategoryListFromSDCOptions(currSdcOptions);
                if (this.compareCategoryLists(sourceCategoryList, refCategoryList)) continue;
                Trace.log("The categories used by the source and reference reports do not match.");
                return false;
            }
            return true;
        }
        if (refSelType.equals("SDCs")) {
            PropertyListCollection selectedSdcs = sourcePolicyDef.getSelectedSdcs();
            for (int i = 0; i < selectedSdcs.size(); ++i) {
                PropertyList currSdcOptions = selectedSdcs.getPropertyList(i);
                String sdcSelectionType = currSdcOptions.getProperty("selectiontype");
                PropertyList currRefSdcOptions = this.findSDCOptions(currSdcOptions.getProperty("SDC"), refPolicyDef.getSelectedSdcs());
                if (currRefSdcOptions == null) {
                    Trace.log("The SDC options in reference report are null");
                    return false;
                }
                String refSdcSelectionType = currRefSdcOptions.getProperty("selectiontype");
                if (!sdcSelectionType.equals(refSdcSelectionType)) {
                    Trace.log("The selection types for sdc:" + currSdcOptions.getProperty("chapter") + " do not match.");
                    return false;
                }
                if (sdcSelectionType.equals("Categories")) {
                    String refCategoryList;
                    String sourceCategoryList = this.getCategoryListFromSDCOptions(currSdcOptions);
                    if (this.compareCategoryLists(sourceCategoryList, refCategoryList = this.getCategoryListForSdc(currSdcOptions.getProperty("chapter"), refPolicyDef.getSelectedSdcs()))) continue;
                    Trace.log("The categories used by the source and reference reports do not match.");
                    return false;
                }
                if (sdcSelectionType.equals("Where Clause")) {
                    String refWhere;
                    String srcwhere = currSdcOptions.getProperty("whereclause");
                    if (srcwhere.equals(refWhere = currRefSdcOptions.getProperty("whereclause"))) continue;
                    return false;
                }
                if (sdcSelectionType.equals("SDIs")) {
                    PropertyListCollection refSDIList;
                    PropertyListCollection srcSDIList = currSdcOptions.getPropertyListNotNull("sdilist").getCollectionNotNull("sdi");
                    if (!this.compareSDIList(srcSDIList, refSDIList = currRefSdcOptions.getPropertyListNotNull("sdilist").getCollectionNotNull("sdi"))) {
                        Trace.log("SDIlists do not match for sdc:" + currSdcOptions.getProperty("chapter"));
                        return false;
                    }
                    return true;
                }
                if (!sdcSelectionType.equals("All")) continue;
                return true;
            }
            return true;
        }
        return false;
    }

    private boolean compareSDIList(PropertyListCollection srcSDIList, PropertyListCollection refSDIList) {
        if (srcSDIList.size() != refSDIList.size()) {
            return false;
        }
        for (int i = 0; i < srcSDIList.size(); ++i) {
            PropertyList currSrcSDI = srcSDIList.getPropertyList(i);
            boolean found = false;
            for (int j = 0; j < refSDIList.size(); ++j) {
                PropertyList currRefSDI = refSDIList.getPropertyList(j);
                if (!currSrcSDI.getProperty(PROPERTY_KEYID1).equals(currRefSDI.getProperty(PROPERTY_KEYID1)) || !currSrcSDI.getProperty(PROPERTY_KEYID2).equals(currRefSDI.getProperty(PROPERTY_KEYID2)) || !currSrcSDI.getProperty(PROPERTY_KEYID3).equals(currRefSDI.getProperty(PROPERTY_KEYID3))) continue;
                found = true;
                break;
            }
            if (found) continue;
            return false;
        }
        return true;
    }

    private String getCategoryListFromSDCOptions(PropertyList currSdcOptions) {
        PropertyListCollection categoryColl = currSdcOptions.getCollection("categories");
        String categories = "";
        for (int c = 0; c < categoryColl.size(); ++c) {
            String cat = categoryColl.getPropertyList(c).getProperty(PROPERTY_CATEGORY);
            if (cat == null || cat.length() <= 0) continue;
            if (categories.length() > 0) {
                categories = categories + ";";
            }
            categories = categories + cat;
        }
        return categories;
    }

    private String getCategoryListForSdc(String sdcid, PropertyListCollection selectedSdcs) {
        for (int i = 0; i < selectedSdcs.size(); ++i) {
            PropertyList currPropList = selectedSdcs.getPropertyList(i);
            if (!currPropList.getProperty("chapter").equals(sdcid)) continue;
            if (!currPropList.getProperty("selectiontype").equals("Categories")) {
                return "";
            }
            PropertyListCollection categoryColl = currPropList.getCollection("categories");
            String categories = "";
            for (int c = 0; c < categoryColl.size(); ++c) {
                String cat = categoryColl.getPropertyList(c).getProperty(PROPERTY_CATEGORY);
                if (cat == null || cat.length() <= 0) continue;
                if (categories.length() > 0) {
                    categories = categories + ";";
                }
                categories = categories + cat;
            }
            return categories;
        }
        return "";
    }

    private PropertyList findSDCOptions(String sdcid, PropertyListCollection selectedSdcs) {
        for (int i = 0; i < selectedSdcs.size(); ++i) {
            PropertyList currPropList = selectedSdcs.getPropertyList(i);
            if (!currPropList.getProperty("SDC").equals(sdcid)) continue;
            return currPropList;
        }
        return null;
    }

    private boolean compareCategoryLists(String sourceCategoryList, String refCategoryList) {
        if (sourceCategoryList.length() == 0 && refCategoryList.length() == 0) {
            return true;
        }
        if (sourceCategoryList.length() == 0 || refCategoryList.length() == 0) {
            return false;
        }
        String[] srcCategories = StringUtil.split(sourceCategoryList, ";");
        String[] refCategories = StringUtil.split(refCategoryList, ";");
        for (int i = 0; i < srcCategories.length; ++i) {
            String srcCat = srcCategories[i];
            boolean found = false;
            for (int j = 0; j < refCategories.length; ++j) {
                if (!srcCat.equals(refCategories[j])) continue;
                found = true;
                break;
            }
            if (found) continue;
            return false;
        }
        return true;
    }

    private void determineSdcSdiList() throws SapphireException {
        String selectionType = this.policyDef.getSelectionType();
        if (selectionType.equals("Categories")) {
            String[] categoryList = null;
            String[] excludeSdcList = null;
            String category = this.policyDef.getCategoryList();
            if (category.length() <= 0) {
                throw new SapphireException("Categories not specified in the policy to determine the SDI list");
            }
            categoryList = StringUtil.split(category, ";");
            String excludeSdc = this.policyDef.getExcludeSDCList();
            if (excludeSdc.length() > 0) {
                excludeSdcList = StringUtil.split(excludeSdc, ";");
            }
            this.sourceSdisIncluded = this.determineSDIList(this.policyDef, excludeSdcList, categoryList);
            this.sourceSDCList = this.getDistinctSDCList(this.sourceSdisIncluded);
            if (this.includeDiffReport) {
                this.refSDCList = this.determineSDCList(excludeSdcList, this.refReportFolder);
                this.refSdisIncluded = this.determineSDIHashMap(excludeSdcList, this.refSDCList, this.refReportFolder);
                this.refMenuSystemIncluded = this.checkIfExistsInTOC("Menu System", this.refReportFolder);
                this.refSysconfigIncluded = this.checkIfExistsInTOC("System Configuration", this.refReportFolder);
                this.mergedSdcList = this.mergeSDCLists(this.sourceSDCList, this.refSDCList);
                this.allSdis = this.mergeSdis(this.mergedSdcList, this.sourceSdisIncluded, this.refSdisIncluded);
            } else {
                this.mergedSdcList = this.sourceSDCList;
                this.allSdis = this.sourceSdisIncluded;
            }
        } else {
            PropertyListCollection selectedSdcs = this.policyDef.getSelectedSdcs();
            HashMap<String, ArrayList> sdisIncludedHM = new HashMap<String, ArrayList>();
            this.sourceSDCList = new ArrayList();
            for (int i = 0; i < selectedSdcs.size(); ++i) {
                PropertyList currSdcOptions = selectedSdcs.getPropertyList(i);
                ArrayList sdiList = this.getSdisIncludedForSDC(currSdcOptions);
                if (sdiList.size() == 0) continue;
                sdisIncludedHM.put(currSdcOptions.getProperty("SDC"), sdiList);
                this.sourceSDCList.add(currSdcOptions.getProperty("SDC"));
            }
            this.sourceSdisIncluded = sdisIncludedHM;
            if (this.includeDiffReport) {
                this.refSDCList = this.determineSDCList(null, this.refReportFolder);
                this.refSdisIncluded = this.determineSDIHashMap(null, this.refSDCList, this.refReportFolder);
                this.refMenuSystemIncluded = this.checkIfExistsInTOC("Menu System", this.refReportFolder);
                this.refSysconfigIncluded = this.checkIfExistsInTOC("System Configuration", this.refReportFolder);
                this.mergedSdcList = this.mergeSDCLists(this.sourceSDCList, this.refSDCList);
                this.allSdis = this.mergeSdis(this.mergedSdcList, this.sourceSdisIncluded, this.refSdisIncluded);
            } else {
                this.mergedSdcList = this.sourceSDCList;
                this.allSdis = this.sourceSdisIncluded;
            }
        }
    }

    public HashMap determineSDIHashMap(String[] excludeSdcList, String[] categories, PropertyList reportOptions) {
        HashMap<String, ArrayList> sdisIncludedHM = new HashMap<String, ArrayList>();
        String inClause = "( '" + categories[0] + "'";
        for (int categoryItem = 1; categoryItem < categories.length; ++categoryItem) {
            inClause = inClause + ", '" + categories[categoryItem] + "'";
        }
        inClause = inClause + ")";
        String sdcsql = "SELECT distinct sdcid FROM categoryitem WHERE categoryid IN " + inClause + " order by sdcid";
        DataSet allSdcListDS = this.queryProcessor.getSqlDataSet(sdcsql);
        String[] allSdcList = StringUtil.split(allSdcListDS.getColumnValues(PROPERTY_SDC, ";"), ";");
        for (int i = 0; i < allSdcList.length; ++i) {
            String currSDCId = allSdcList[i];
            boolean include = true;
            if (excludeSdcList != null) {
                for (int j = 0; j < excludeSdcList.length; ++j) {
                    if (!currSDCId.equals(excludeSdcList[j])) continue;
                    include = false;
                    break;
                }
                if (!include) continue;
            }
            DataSet sdisIncluded = new DataSet();
            sdisIncluded.addColumn(PROPERTY_SDC, 0);
            sdisIncluded.addColumn(PROPERTY_KEYID1, 0);
            sdisIncluded.addColumn(PROPERTY_KEYID2, 0);
            sdisIncluded.addColumn(PROPERTY_KEYID3, 0);
            String sql = "SELECT keyid1 FROM categoryitem WHERE sdcid='" + currSDCId + "'";
            sql = sql + " and categoryid IN " + inClause + "order by keyid1";
            DataSet results = this.queryProcessor.getSqlDataSet(sql);
            if (results.getRowCount() == 0) continue;
            String[] keyid1ListArr = StringUtil.split(results.getColumnValues(PROPERTY_KEYID1, ";"), ";");
            String table = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("tableid");
            String sdiArr = "";
            int keyCount = Integer.parseInt((String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolumns"));
            for (int j = 0; j < keyid1ListArr.length; ++j) {
                String keycolid2;
                if (keyCount == 1) {
                    int currRow = sdisIncluded.addRow();
                    sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                    sdisIncluded.setString(currRow, PROPERTY_KEYID1, keyid1ListArr[j].trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID2, "");
                    sdisIncluded.setString(currRow, PROPERTY_KEYID3, "");
                    continue;
                }
                if (keyCount == 2) {
                    String keycolid1 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid1");
                    keycolid2 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid2");
                    String multi = "SELECT " + keycolid1 + "," + keycolid2 + " FROM " + table + " WHERE " + keycolid1 + " = '" + keyid1ListArr[j] + "'";
                    DataSet ds = this.queryProcessor.getSqlDataSet(multi);
                    for (int d = 0; d < ds.size(); ++d) {
                        int currRow = sdisIncluded.addRow();
                        sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                        sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, keycolid1).trim());
                        sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, keycolid2).trim());
                        sdisIncluded.setString(currRow, PROPERTY_KEYID3, "");
                    }
                    continue;
                }
                if (keyCount != 3) continue;
                String keycolid1 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid1");
                keycolid2 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid2");
                String keycolid3 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid3");
                String multi = "SELECT " + keycolid1 + "," + keycolid2 + "," + keycolid3 + " FROM " + table + " WHERE " + keycolid1 + " = '" + keyid1ListArr[j] + "'";
                DataSet ds = this.queryProcessor.getSqlDataSet(multi);
                for (int d = 0; d < ds.size(); ++d) {
                    int currRow = sdisIncluded.addRow();
                    sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                    sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, keycolid1).trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, keycolid2).trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID3, ds.getString(d, keycolid3).trim());
                }
            }
            sdisIncludedHM.put(currSDCId, this.convertToSDIList(sdisIncluded, false));
        }
        return sdisIncludedHM;
    }

    private ArrayList getSdisIncludedForSDC(PropertyList currSdcOptions) {
        String sdcid = currSdcOptions.getProperty("SDC");
        String show = currSdcOptions.getProperty("show");
        if ("N".equals(show)) {
            return new ArrayList();
        }
        String sdiSelectionType = currSdcOptions.getProperty("selectiontype");
        boolean templatesOnly = "Y".equals(currSdcOptions.getProperty("templatesonly"));
        if ("SDIs".equals(sdiSelectionType)) {
            ArrayList<SDI> list = new ArrayList<SDI>();
            PropertyList sdiListPL = currSdcOptions.getPropertyList("sdilist");
            PropertyListCollection sdicoll = sdiListPL.getCollection("sdi");
            for (int i = 0; i < sdicoll.size(); ++i) {
                PropertyList keys = sdicoll.getPropertyList(i);
                String keyid1 = keys.getProperty(PROPERTY_KEYID1);
                String keyid2 = keys.getProperty(PROPERTY_KEYID2);
                String keyid3 = keys.getProperty(PROPERTY_KEYID3);
                SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
                list.add(sdi);
            }
            return list;
        }
        if ("All".equals(sdiSelectionType)) {
            DataSet sdisIncluded = new DataSet();
            int keyCount = Integer.parseInt((String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolumns"));
            String sql = "SELECT ";
            String orderby = " ORDER BY ";
            if (keyCount == 1) {
                sql = sql + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1");
                orderby = orderby + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1");
            } else if (keyCount == 2) {
                sql = sql + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2");
                orderby = orderby + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2");
            } else if (keyCount == 3) {
                sql = sql + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2") + "," + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3");
                orderby = orderby + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2") + "," + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3");
            }
            sql = sql + " FROM " + this.sdcProcessor.getSDCProperties(sdcid).get("tableid");
            if (templatesOnly) {
                sql = sql + " WHERE templateflag= 'Y' ";
            }
            sql = sql + orderby;
            DataSet ds = this.queryProcessor.getSqlDataSet(sql);
            for (int d = 0; d < ds.size(); ++d) {
                int currRow = sdisIncluded.addRow();
                sdisIncluded.setString(currRow, PROPERTY_SDC, sdcid);
                sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1").toString()).trim());
                if (keyCount > 1) {
                    sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2").toString()).trim());
                }
                if (keyCount <= 2) continue;
                sdisIncluded.setString(currRow, PROPERTY_KEYID3, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3").toString()).trim());
            }
            ArrayList sdiList = this.convertToSDIList(sdisIncluded, false);
            return sdiList;
        }
        if ("Where Clause".equals(sdiSelectionType)) {
            DataSet ds;
            DataSet sdisIncluded = new DataSet();
            String whereclause = currSdcOptions.getProperty("whereclause");
            int keyCount = Integer.parseInt((String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolumns"));
            String sql = "SELECT ";
            String orderby = " ORDER BY ";
            if (keyCount == 1) {
                sql = sql + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1");
                orderby = orderby + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1");
            } else if (keyCount == 2) {
                sql = sql + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2");
                orderby = orderby + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2");
            } else if (keyCount == 3) {
                sql = sql + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2") + "," + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3");
                orderby = orderby + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2") + "," + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3");
            }
            sql = sql + " FROM " + this.sdcProcessor.getSDCProperties(sdcid).get("tableid");
            if (whereclause.length() > 0) {
                sql = sql + " WHERE (" + whereclause + ")";
                if (templatesOnly) {
                    sql = sql + " AND templateflag= 'Y' ";
                }
            } else if (templatesOnly) {
                sql = sql + " WHERE templateflag= 'Y' ";
            }
            if ((ds = this.queryProcessor.getSqlDataSet(sql = sql + orderby)) != null) {
                for (int d = 0; d < ds.size(); ++d) {
                    int currRow = sdisIncluded.addRow();
                    sdisIncluded.setString(currRow, PROPERTY_SDC, sdcid);
                    sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1").toString()).trim());
                    if (keyCount >= 2) {
                        sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2").toString()).trim());
                    }
                    if (keyCount != 3) continue;
                    sdisIncluded.setString(currRow, PROPERTY_KEYID3, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3").toString()).trim());
                }
            }
            ArrayList sdiList = this.convertToSDIList(sdisIncluded, false);
            return sdiList;
        }
        if ("Categories".equals(sdiSelectionType)) {
            DataSet sdisIncluded = new DataSet();
            PropertyListCollection categoryColl = currSdcOptions.getCollection("categories");
            String categories = "";
            for (int i = 0; i < categoryColl.size(); ++i) {
                String cat = categoryColl.getPropertyList(i).getProperty(PROPERTY_CATEGORY);
                if (cat == null || cat.length() <= 0) continue;
                if (categories.length() > 0) {
                    categories = categories + ",";
                }
                categories = categories + "'" + cat + "'";
            }
            String whereclause = this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + " IN ( SELECT keyid1 FROM CATEGORYITEM WHERE categoryid IN ( " + categories + ") AND sdcid = '" + sdcid + "' ) ";
            int keyCount = Integer.parseInt((String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolumns"));
            String sql = "SELECT ";
            String orderby = " ORDER BY ";
            if (keyCount == 1) {
                sql = sql + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1");
                orderby = orderby + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1");
            } else if (keyCount == 2) {
                sql = sql + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2");
                orderby = orderby + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2");
            } else if (keyCount == 3) {
                sql = sql + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2") + "," + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3");
                orderby = orderby + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1") + "," + this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2") + "," + (String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3");
            }
            sql = sql + " FROM " + this.sdcProcessor.getSDCProperties(sdcid).get("tableid");
            if (whereclause.length() > 0) {
                sql = sql + " WHERE (" + whereclause + ")";
                if (templatesOnly) {
                    sql = sql + " AND templateflag= 'Y' ";
                }
            } else if (templatesOnly) {
                sql = sql + " WHERE templateflag= 'Y' ";
            }
            sql = sql + orderby;
            DataSet ds = this.queryProcessor.getSqlDataSet(sql);
            for (int d = 0; d < ds.size(); ++d) {
                int currRow = sdisIncluded.addRow();
                sdisIncluded.setString(currRow, PROPERTY_SDC, sdcid);
                sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1").toString()).trim());
                if (this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2") != null) {
                    sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2").toString()).trim());
                }
                if (this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3") == null) continue;
                sdisIncluded.setString(currRow, PROPERTY_KEYID3, ds.getString(d, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3").toString()).trim());
            }
            ArrayList sdiList = this.convertToSDIList(sdisIncluded, false);
            return sdiList;
        }
        return new ArrayList();
    }

    private ArrayList mergeSDCLists(ArrayList sourceSDCList, ArrayList refSDCList) {
        int i;
        if (refSDCList == null || refSDCList.size() == 0) {
            return sourceSDCList;
        }
        TreeSet merged = new TreeSet();
        for (i = 0; i < sourceSDCList.size(); ++i) {
            merged.add(sourceSDCList.get(i));
        }
        for (i = 0; i < refSDCList.size(); ++i) {
            merged.add(refSDCList.get(i));
        }
        Object[] arr = merged.toArray();
        ArrayList<Object> ret = new ArrayList<Object>();
        for (int i2 = 0; i2 < arr.length; ++i2) {
            ret.add(arr[i2]);
        }
        return ret;
    }

    public DataSet determineSDIList(String[] categories) {
        DataSet sdisIncluded = new DataSet();
        sdisIncluded.addColumn(PROPERTY_SDC, 0);
        sdisIncluded.addColumn(PROPERTY_KEYID1, 0);
        sdisIncluded.addColumn(PROPERTY_KEYID2, 0);
        sdisIncluded.addColumn(PROPERTY_KEYID3, 0);
        String inClause = "( '" + categories[0] + "'";
        for (int categoryItem = 1; categoryItem < categories.length; ++categoryItem) {
            inClause = inClause + ", '" + categories[categoryItem] + "'";
        }
        inClause = inClause + ")";
        String sdcsql = "SELECT distinct sdcid FROM categoryitem WHERE categoryid IN " + inClause + " order by sdcid";
        DataSet sdcListDS = this.queryProcessor.getSqlDataSet(sdcsql);
        String[] sdcList = StringUtil.split(sdcListDS.getColumnValues(PROPERTY_SDC, ";"), ";");
        for (int i = 0; i < sdcList.length; ++i) {
            String currSDCId = sdcList[i];
            String sql = "SELECT keyid1 FROM categoryitem WHERE sdcid='" + currSDCId + "'";
            DataSet results = this.queryProcessor.getSqlDataSet(sql = sql + " and categoryid IN " + inClause + "order by keyid1");
            if (results.getRowCount() == 0) continue;
            String[] keyid1ListArr = StringUtil.split(results.getColumnValues(PROPERTY_KEYID1, ";"), ";");
            String table = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("tableid");
            String sdiArr = "";
            int keyCount = Integer.parseInt((String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolumns"));
            for (int j = 0; j < keyid1ListArr.length; ++j) {
                String keycolid2;
                if (keyCount == 1) {
                    int currRow = sdisIncluded.addRow();
                    sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                    sdisIncluded.setString(currRow, PROPERTY_KEYID1, keyid1ListArr[j].trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID2, "");
                    sdisIncluded.setString(currRow, PROPERTY_KEYID3, "");
                    continue;
                }
                if (keyCount == 2) {
                    String keycolid1 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid1");
                    keycolid2 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid2");
                    String multi = "SELECT " + keycolid1 + "," + keycolid2 + " FROM " + table + " WHERE " + keycolid1 + " = '" + keyid1ListArr[j] + "'";
                    DataSet ds = this.queryProcessor.getSqlDataSet(multi);
                    for (int d = 0; d < ds.size(); ++d) {
                        int currRow = sdisIncluded.addRow();
                        sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                        sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, keycolid1).trim());
                        sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, keycolid2).trim());
                        sdisIncluded.setString(currRow, PROPERTY_KEYID3, "");
                    }
                    continue;
                }
                if (keyCount != 3) continue;
                String keycolid1 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid1");
                keycolid2 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid2");
                String keycolid3 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid3");
                String multi = "SELECT " + keycolid1 + "," + keycolid2 + "," + keycolid3 + " FROM " + table + " WHERE " + keycolid1 + " = '" + keyid1ListArr[j] + "'";
                DataSet ds = this.queryProcessor.getSqlDataSet(multi);
                for (int d = 0; d < ds.size(); ++d) {
                    int currRow = sdisIncluded.addRow();
                    sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                    sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, keycolid1).trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, keycolid2).trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID3, ds.getString(d, keycolid3).trim());
                }
            }
        }
        return sdisIncluded;
    }

    public HashMap determineSDIList(ConfigReportPolicyDef policyDef, String[] excludeSdcList, String[] categories) {
        HashMap<String, ArrayList> sdisIncludedHM = new HashMap<String, ArrayList>();
        String inClause = "( '" + categories[0] + "'";
        for (int categoryItem = 1; categoryItem < categories.length; ++categoryItem) {
            inClause = inClause + ", '" + categories[categoryItem] + "'";
        }
        inClause = inClause + ")";
        String sdcsql = "SELECT distinct sdcid FROM categoryitem WHERE categoryid IN " + inClause + " order by sdcid";
        DataSet allSdcListDS = this.queryProcessor.getSqlDataSet(sdcsql);
        String[] allSdcList = StringUtil.split(allSdcListDS.getColumnValues(PROPERTY_SDC, ";"), ";");
        for (int i = 0; i < allSdcList.length; ++i) {
            String currSDCId = allSdcList[i];
            boolean include = true;
            if (excludeSdcList != null) {
                for (int j = 0; j < excludeSdcList.length; ++j) {
                    if (!currSDCId.equals(excludeSdcList[j])) continue;
                    include = false;
                    break;
                }
                if (!include) continue;
            }
            DataSet sdisIncluded = new DataSet();
            sdisIncluded.addColumn(PROPERTY_SDC, 0);
            sdisIncluded.addColumn(PROPERTY_KEYID1, 0);
            sdisIncluded.addColumn(PROPERTY_KEYID2, 0);
            sdisIncluded.addColumn(PROPERTY_KEYID3, 0);
            String sql = "SELECT keyid1 FROM categoryitem WHERE sdcid='" + currSDCId + "'";
            sql = sql + " and categoryid IN " + inClause + "order by keyid1";
            DataSet results = this.queryProcessor.getSqlDataSet(sql);
            if (results.getRowCount() == 0) continue;
            String[] keyid1ListArr = StringUtil.split(results.getColumnValues(PROPERTY_KEYID1, ";"), ";");
            String table = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("tableid");
            int keyCount = Integer.parseInt((String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolumns"));
            for (int j = 0; j < keyid1ListArr.length; ++j) {
                String keycolid2;
                if (keyCount == 1) {
                    int currRow = sdisIncluded.addRow();
                    sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                    sdisIncluded.setString(currRow, PROPERTY_KEYID1, keyid1ListArr[j].trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID2, "");
                    sdisIncluded.setString(currRow, PROPERTY_KEYID3, "");
                    continue;
                }
                if (keyCount == 2) {
                    String keycolid1 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid1");
                    keycolid2 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid2");
                    String multi = "SELECT " + keycolid1 + "," + keycolid2 + " FROM " + table + " WHERE " + keycolid1 + " = '" + keyid1ListArr[j] + "'";
                    DataSet ds = this.queryProcessor.getSqlDataSet(multi);
                    for (int d = 0; d < ds.size(); ++d) {
                        int currRow = sdisIncluded.addRow();
                        sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                        sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, keycolid1).trim());
                        sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, keycolid2).trim());
                        sdisIncluded.setString(currRow, PROPERTY_KEYID3, "");
                    }
                    continue;
                }
                if (keyCount != 3) continue;
                String keycolid1 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid1");
                keycolid2 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid2");
                String keycolid3 = (String)this.sdcProcessor.getSDCProperties(currSDCId).get("keycolid3");
                String multi = "SELECT " + keycolid1 + "," + keycolid2 + "," + keycolid3 + " FROM " + table + " WHERE " + keycolid1 + " = '" + keyid1ListArr[j] + "'";
                DataSet ds = this.queryProcessor.getSqlDataSet(multi);
                for (int d = 0; d < ds.size(); ++d) {
                    int currRow = sdisIncluded.addRow();
                    sdisIncluded.setString(currRow, PROPERTY_SDC, currSDCId);
                    sdisIncluded.setString(currRow, PROPERTY_KEYID1, ds.getString(d, keycolid1).trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID2, ds.getString(d, keycolid2).trim());
                    sdisIncluded.setString(currRow, PROPERTY_KEYID3, ds.getString(d, keycolid3).trim());
                }
            }
            ArrayList sdiList = this.convertToSDIList(sdisIncluded, false);
            if (sdiList.size() == 0) continue;
            sdisIncludedHM.put(currSDCId, sdiList);
        }
        return sdisIncludedHM;
    }

    private ArrayList getDistinctSDCList(HashMap sdisIncludedDS) {
        Set s = sdisIncludedDS.keySet();
        ArrayList<String> sdcList = new ArrayList<String>();
        Object[] o = s.toArray();
        for (int i = 0; i < o.length; ++i) {
            sdcList.add(o[i].toString());
        }
        Collections.sort(sdcList);
        return sdcList;
    }

    private ArrayList getMergedChapterList() {
        ArrayList<String> ret = new ArrayList<String>();
        if (this.sourceMenuSystemIncluded) {
            ret.add("Menu System");
        }
        for (int i = 0; i < this.mergedSdcList.size(); ++i) {
            ret.add(this.mergedSdcList.get(i).toString());
        }
        if (this.sourceSysconfigIncluded) {
            ret.add("System Configuration");
        }
        return ret;
    }

    public void createReport() throws SapphireException {
        FileOutputStream fos = this.createReportFile();
        ArrayList mergedSDCChapterNames = this.getMergedChapterList();
        this.startReport(fos, this.reportRendererList, mergedSDCChapterNames);
        for (int i = 0; i < mergedSDCChapterNames.size(); ++i) {
            BaseRenderer renderer = (BaseRenderer)this.reportRendererList.get(i);
            String currChapter = mergedSDCChapterNames.get(i).toString();
            this.reportChapter(fos, i + 1 + "", currChapter, renderer);
        }
        this.generateTOC(fos, mergedSDCChapterNames, this.reportRendererList, this.sourceROList, this.refROList, this.includeDiffReport, this.tocRendererOptions);
        this.endReport(fos);
        if (this.includeXMLReport) {
            if (this.policyDef != null) {
                this.saveConfigReportPolicy(this.policyDef);
            }
            this.saveSdisIncluded(new PropertyList(this.sourceSdisIncluded));
        }
    }

    private ArrayList getRendererList(ArrayList sourceROList, ArrayList refROList) throws SapphireException {
        BaseRO refRO;
        ArrayList<BaseRenderer> rendererList = new ArrayList<BaseRenderer>();
        int currChapter = 0;
        if (this.sourceMenuSystemIncluded) {
            MenuSystemRenderer renderer = new MenuSystemRenderer();
            if (this.includeDiffReport) {
                refRO = this.findRefMenuSystemRO("Menu System", refROList);
                if (refRO != null) {
                    renderer.initialize(this.sapphireConnection, this.config, (BaseRO)sourceROList.get(currChapter), refRO, this.allSdis, this.includeDiffReport, this.includeSiteMapPageButtonRoleMatrix);
                } else {
                    renderer.initialize(this.sapphireConnection, this.config, (BaseRO)sourceROList.get(currChapter), this.sourceSdisIncluded, this.includeSiteMapPageButtonRoleMatrix);
                }
            } else {
                renderer.initialize(this.sapphireConnection, this.config, (BaseRO)sourceROList.get(currChapter), this.sourceSdisIncluded, this.includeSiteMapPageButtonRoleMatrix);
            }
            rendererList.add(renderer);
            ++currChapter;
        }
        for (int i = 0; i < this.mergedSdcList.size(); ++i) {
            BaseSDCRenderer renderer = ConfigReportController.createSDCRenderer(this.customRendererPackage, this.mergedSdcList.get(i).toString());
            if (this.includeDiffReport) {
                BaseSDCRO refRO2 = this.findRO(this.mergedSdcList.get(i).toString(), refROList);
                BaseSDCRO srcRO = this.findRO(this.mergedSdcList.get(i).toString(), sourceROList);
                if (renderer instanceof WebPageRenderer) {
                    ((WebPageRenderer)renderer).initialize(this.sapphireConnection, this.config, srcRO, refRO2, this.allSdis, this.includeDiffReport, this.includeSDIRoleMatrix, this.includeWebPagePageButtonRoleMatrix);
                } else {
                    renderer.initialize(this.sapphireConnection, this.config, srcRO, refRO2, this.allSdis, this.includeDiffReport, this.includeSDIRoleMatrix);
                }
            } else if (renderer instanceof WebPageRenderer) {
                ((WebPageRenderer)renderer).initialize(this.sapphireConnection, this.config, (BaseRO)((BaseSDCRO)sourceROList.get(currChapter)), this.sourceSdisIncluded, this.includeSDIRoleMatrix, this.includeWebPagePageButtonRoleMatrix);
            } else {
                renderer.initialize(this.sapphireConnection, this.config, (BaseSDCRO)sourceROList.get(currChapter), this.sourceSdisIncluded, this.includeSDIRoleMatrix);
            }
            if (this.policyDef != null) {
                renderer.setOptions(this.policyDef.getRendererOptions((String)this.mergedSdcList.get(i)));
                renderer.setIgnoreDiffs(this.policyDef.getIgnorePrimaryDiffs((String)this.mergedSdcList.get(i)), this.policyDef.getIgnoreDetailsDiffs((String)this.mergedSdcList.get(i)));
            } else if (!this.fromListPage) {
                PropertyList refReportOptions = this.loadReportOptions(this.refReportFolder);
                String currSDCOptions = refReportOptions.getProperty(this.mergedSdcList.get(i).toString());
                DataSet currSDCOptionList = new DataSet(currSDCOptions);
                renderer.setOptions(currSDCOptionList);
            } else {
                String reportOptionsStr = this.config.getProperty("options");
                if (reportOptionsStr != null && reportOptionsStr.length() > 0) {
                    String rhs = reportOptionsStr.substring(reportOptionsStr.indexOf("=") + 1, reportOptionsStr.indexOf(";"));
                    DataSet opt = new DataSet();
                    if (rhs != null && rhs.length() > 0) {
                        String[] options = StringUtil.split(rhs, ",");
                        for (int optno = 0; optno < options.length; ++optno) {
                            String[] tokens = StringUtil.split(options[optno], ":");
                            if (tokens.length != 2) continue;
                            int row = opt.addRow();
                            opt.setNumber(row, "optionno", Integer.parseInt(tokens[0]));
                            opt.setString(row, "selectedvalue", tokens[1]);
                        }
                    }
                    renderer.setOptions(opt);
                }
            }
            rendererList.add(renderer);
            ++currChapter;
        }
        if (this.sourceSysconfigIncluded) {
            SysConfigRenderer renderer = new SysConfigRenderer();
            if (this.includeDiffReport) {
                refRO = this.findRefSysConfigRO("System Configuration", refROList);
                SysConfigRO srcRO = this.findRefSysConfigRO("System Configuration", sourceROList);
                renderer.initialize(this.sapphireConnection, this.config, srcRO, refRO, this.allSdis, this.includeDiffReport);
            } else {
                renderer.initialize(this.sapphireConnection, this.config, (BaseRO)sourceROList.get(currChapter), this.sourceSdisIncluded);
            }
            if (this.policyDef != null) {
                renderer.setOptions(this.policyDef.getRendererOptions("System Configuration"));
            }
            rendererList.add(renderer);
        }
        return rendererList;
    }

    private ArrayList getSourceChapterList() {
        ArrayList<String> chapterList = new ArrayList<String>();
        if (this.sourceMenuSystemIncluded) {
            chapterList.add("Menu System");
        }
        for (int i = 0; i < this.sourceSDCList.size(); ++i) {
            chapterList.add(this.sourceSDCList.get(i).toString());
        }
        if (this.sourceSysconfigIncluded) {
            chapterList.add("System Configuration");
        }
        return chapterList;
    }

    private ArrayList getRefChapterList() {
        ArrayList<String> chapterList = new ArrayList<String>();
        if (this.refMenuSystemIncluded) {
            chapterList.add("Menu System");
        }
        for (int i = 0; i < this.refSDCList.size(); ++i) {
            chapterList.add(this.refSDCList.get(i).toString());
        }
        if (this.refSysconfigIncluded) {
            chapterList.add("System Configuration");
        }
        return chapterList;
    }

    private void reportChapter(OutputStream fos, String chapterNo, String chapterName, BaseRenderer renderer) throws SapphireException {
        if (chapterName.equals("Menu System")) {
            this.reportMenuSystem(fos, chapterNo, (MenuSystemRenderer)renderer);
        } else if (chapterName.equals("System Configuration")) {
            this.reportSysConfig(fos, chapterNo, (SysConfigRenderer)renderer);
        } else {
            this.reportSDCChapter(fos, chapterNo, chapterName, (BaseSDCRenderer)renderer);
        }
    }

    private ArrayList getSourceROList() throws SapphireException {
        ArrayList<BaseRO> roList = new ArrayList<BaseRO>();
        if (this.sourceMenuSystemIncluded) {
            MenuSystemRO ro = new MenuSystemRO();
            ro.initialize("Menu System", this.sapphireConnection, this.folder, this.createdBy);
            ro.startChapter();
            roList.add(ro);
        }
        for (int i = 0; i < this.sourceSDCList.size(); ++i) {
            String currSDC = this.sourceSDCList.get(i).toString();
            BaseSDCRO ro = this.createSDCRO(currSDC);
            ro.initialize(this.sourceSDCList.get(i).toString(), this.sapphireConnection, this.folder, this.createdBy);
            ArrayList sdiList = (ArrayList)this.sourceSdisIncluded.get(currSDC);
            ro.setSDIList(sdiList);
            ro.startChapter();
            roList.add(ro);
        }
        if (this.sourceSysconfigIncluded) {
            SysConfigRO ro = new SysConfigRO();
            ro.initialize("System Configuration", this.sapphireConnection, this.folder, this.createdBy);
            ro.startChapter();
            roList.add(ro);
        }
        return roList;
    }

    private ArrayList getRefROList() throws SapphireException {
        ArrayList<BaseRO> roList = new ArrayList<BaseRO>();
        if (this.refMenuSystemIncluded) {
            MenuSystemRO ro = new MenuSystemRO();
            ro.initialize("Menu System", this.folder, this.createdBy, this.refReportFolder, this.sapphireConnection);
            ro.startChapter();
            roList.add(ro);
        }
        for (int i = 0; i < this.refSDCList.size(); ++i) {
            String currSDC = this.refSDCList.get(i).toString();
            BaseSDCRO ro = this.createSDCRO(currSDC);
            ro.initialize(this.refSDCList.get(i).toString(), this.folder, this.createdBy, this.refReportFolder, this.sapphireConnection);
            ArrayList sdiList = (ArrayList)this.refSdisIncluded.get(currSDC);
            ro.setSDIList(sdiList);
            ro.startChapter();
            roList.add(ro);
        }
        if (this.refSysconfigIncluded) {
            SysConfigRO ro = new SysConfigRO();
            ro.initialize("System Configuration", this.folder, this.createdBy, this.refReportFolder, this.sapphireConnection);
            ro.startChapter();
            roList.add(ro);
        }
        return roList;
    }

    private boolean findChapter(String chapterName, ArrayList chapterList) {
        for (int i = 0; i < chapterList.size(); ++i) {
            String currchap = chapterList.get(i).toString();
            if (!currchap.equals(chapterName)) continue;
            return true;
        }
        return false;
    }

    private ArrayList getRefChapterROs(ArrayList refSdcList, HashMap refSdisIncluded, String refReportFolder) throws SapphireException {
        ArrayList<BaseRO> roList = new ArrayList<BaseRO>();
        ArrayList chapters = this.determineChapterList(refReportFolder);
        if (this.findChapter("Menu System", chapters)) {
            MenuSystemRO ro = new MenuSystemRO();
            ro.initialize("Menu System", this.folder, this.createdBy, refReportFolder, this.sapphireConnection);
            ro.startChapter();
            roList.add(ro);
        }
        for (int i = 0; i < refSdcList.size(); ++i) {
            ArrayList currentSDIList = (ArrayList)refSdisIncluded.get(refSdcList.get(i).toString());
            BaseSDCRO ro = this.createSDCRO(refSdcList.get(i).toString());
            ro.initialize(refSdcList.get(i).toString(), this.folder, this.createdBy, refReportFolder, this.sapphireConnection);
            ro.setSDIList(currentSDIList);
            ro.startChapter();
            roList.add(ro);
        }
        if (this.findChapter("System Configuration", chapters)) {
            SysConfigRO ro = new SysConfigRO();
            ro.initialize("System Configuration", this.folder, this.createdBy, refReportFolder, this.sapphireConnection);
            ro.startChapter();
            roList.add(ro);
        }
        return roList;
    }

    private BaseSDCRO findRO(String chapterName, ArrayList roList) {
        for (int i = 0; i < roList.size(); ++i) {
            try {
                BaseSDCRO ro = (BaseSDCRO)roList.get(i);
                if (ro == null) {
                    Trace.log("RO is null for chapter:" + chapterName);
                    return null;
                }
                if (ro.getSDCProperties() == null || !chapterName.equals(ro.getSDCName())) continue;
                return ro;
            }
            catch (ClassCastException classCastException) {
                // empty catch block
            }
        }
        return null;
    }

    private SysConfigRO findRefSysConfigRO(String chapterName, ArrayList refROList) {
        for (int i = 0; i < refROList.size(); ++i) {
            try {
                SysConfigRO ro = (SysConfigRO)refROList.get(i);
                return ro;
            }
            catch (ClassCastException classCastException) {
                continue;
            }
        }
        return null;
    }

    private MenuSystemRO findRefMenuSystemRO(String chapterName, ArrayList refROList) {
        for (int i = 0; i < refROList.size(); ++i) {
            try {
                MenuSystemRO ro = (MenuSystemRO)refROList.get(i);
                return ro;
            }
            catch (ClassCastException classCastException) {
                continue;
            }
        }
        return null;
    }

    private BaseSDCRO createSDCRO(String sdcId) throws SapphireException {
        String roClassName = "com.labvantage.sapphire.modules.configreport.ro." + sdcId.substring(0, 1).toUpperCase() + sdcId.substring(1) + "RO";
        String userROClassName = this.customRendererPackage + "." + sdcId.substring(0, 1).toUpperCase() + sdcId.substring(1) + "RO";
        BaseSDCRO ro = null;
        try {
            ro = (BaseSDCRO)Class.forName(userROClassName).newInstance();
        }
        catch (ClassNotFoundException e) {
            ro = null;
        }
        catch (Exception e) {
            throw new SapphireException("create sdc RO failed" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
        }
        if (ro == null) {
            try {
                ro = (BaseSDCRO)Class.forName(roClassName).newInstance();
            }
            catch (ClassNotFoundException e) {
                ro = new BaseSDCRO();
            }
            catch (Exception e) {
                throw new SapphireException("create sdc RO failed" + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())), e);
            }
        }
        return ro;
    }

    public static BaseSDCRenderer createSDCRenderer(String customRendererPackage, String sdcId) throws SapphireException {
        BaseSDCRenderer renderer;
        String rendererClassName = "com.labvantage.sapphire.modules.configreport.renderer.sdc." + sdcId.substring(0, 1).toUpperCase() + sdcId.substring(1) + "Renderer";
        String userRendererClassName = customRendererPackage + "." + sdcId.substring(0, 1).toUpperCase() + sdcId.substring(1) + "Renderer";
        try {
            renderer = (BaseSDCRenderer)Class.forName(userRendererClassName).newInstance();
        }
        catch (ClassNotFoundException e) {
            renderer = null;
        }
        catch (Exception e) {
            throw new SapphireException("create sdc RO failed" + e.getMessage(), e);
        }
        if (renderer == null) {
            try {
                renderer = (BaseSDCRenderer)Class.forName(rendererClassName).newInstance();
            }
            catch (ClassNotFoundException e) {
                renderer = new BaseSDCRenderer();
            }
            catch (Exception e) {
                throw new SapphireException("create sdc Renderer failed" + e.getMessage(), e);
            }
        }
        return renderer;
    }

    private FileOutputStream createReportFile() throws SapphireException {
        FileOutputStream fos;
        String filePath = this.folder + File.separator + this.reportFileName;
        try {
            String xmlReportFolder;
            String htmlReportFolder;
            fos = new FileOutputStream(filePath);
            String imagesfolder = this.folder + File.separator + "images";
            if (!new File(imagesfolder).exists()) {
                new File(imagesfolder).mkdir();
            }
            if (!new File(htmlReportFolder = this.folder + File.separator + "html").exists()) {
                new File(htmlReportFolder).mkdir();
            }
            if (this.includeXMLReport && !new File(xmlReportFolder = this.folder + File.separator + "xmlreport").exists()) {
                new File(xmlReportFolder).mkdir();
            }
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create the report file: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
        }
        return fos;
    }

    private void startReport(FileOutputStream fos, ArrayList rendererList, ArrayList chapterNames) throws SapphireException {
        boolean hideSubsections = false;
        if (this.tocRendererOptions != null) {
            hideSubsections = this.tocRendererOptions.getProperty("hidesubsections", "N").equals("Y");
        }
        if (this.tocRendererOptions != null && this.tocRendererOptions.getProperty("chapterrendering").equals("CategoryList")) {
            String firstChapter = TOCRenderer.getCategorizedChapterList(this.tocRendererOptions.getCollectionNotNull("categorylist")).getString(0, PROPERTY_CATEGORY);
            this.configReportContent.startReport(rendererList.size(), firstChapter, false, hideSubsections);
        } else {
            this.configReportContent.startReport(rendererList.size(), chapterNames.get(0).toString(), true, hideSubsections);
        }
        this.appendCoverPage(fos, "1", this.allSdis);
        try {
            fos.write(this.configReportContent.toString().getBytes());
        }
        catch (IOException e) {
            throw new SapphireException("Failed to start report", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
        }
    }

    private void endReport(FileOutputStream fos) throws SapphireException {
        if (!this.frames) {
            StringBuffer buffer = new StringBuffer();
            this.configReportContent.endReport(buffer);
            try {
                fos.write(buffer.toString().getBytes());
                fos.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to write report", ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
            }
            try {
                File coverp = new File(this.folder + File.separator + "html" + File.separator + "cover.html");
                StringBuffer coverpage = new StringBuffer(FileUtil.getFileString(coverp));
                coverp.delete();
                File tocp = new File(this.folder + File.separator + "html" + File.separator + "toc.html");
                StringBuffer toc = new StringBuffer(FileUtil.getFileString(tocp));
                tocp.delete();
                StringBuffer content = new StringBuffer(FileUtil.getFileString(new File(this.folder + File.separator + this.reportFileName)));
                File merged = new File(this.folder + File.separator + this.reportFileName);
                FileOutputStream s = new FileOutputStream(merged);
                s.write(coverpage.toString().getBytes());
                s.write(toc.toString().getBytes());
                s.write(content.toString().getBytes());
                s.close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to merge report files.");
            }
        }
    }

    private void generateTOC(OutputStream reportStream, ArrayList chapterList, ArrayList rendererList, ArrayList sourceROList, ArrayList refROList, boolean includediffreport, PropertyList tocRendererOptions) throws SapphireException {
        TOCRenderer tocRenderer = new TOCRenderer(this.mergedSdcList);
        tocRenderer.setOptions(tocRendererOptions);
        tocRenderer.initialize(this.sapphireConnection, this.config, this.allSdis, includediffreport);
        if (!this.frames) {
            String filePath = this.folder + File.separator + "html" + File.separator + "toc.html";
            try {
                FileOutputStream tocFile = new FileOutputStream(filePath);
                tocRenderer.reportNoFrames(tocFile, chapterList);
                tocFile.close();
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create the report file: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
            }
            catch (IOException e) {
                throw new SapphireException("Cannot close the report file: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
            }
        } else {
            tocRenderer.reportWithFrames(chapterList, rendererList);
            tocRenderer.generateChapterTOC(chapterList, rendererList);
        }
        if (this.includeXMLReport) {
            tocRenderer.createXMLReport(chapterList, sourceROList, rendererList);
        }
    }

    private void reportMenuSystem(OutputStream outStream, String chapterNo, MenuSystemRenderer renderer) throws SapphireException {
        if (this.includeXMLReport) {
            renderer.createXMLReport();
        }
        if (!this.frames) {
            renderer.reportNoFrames(chapterNo, outStream);
        } else {
            renderer.reportWithFrames(chapterNo);
        }
    }

    private void reportSysConfig(OutputStream noFramesOutStream, String chapterNo, SysConfigRenderer renderer) throws SapphireException {
        if (this.includeXMLReport) {
            renderer.createXMLReport();
        }
        if (!this.frames) {
            renderer.reportNoFrames(chapterNo, noFramesOutStream);
        } else {
            renderer.reportWithFrames(chapterNo);
        }
    }

    private void appendCoverPage(OutputStream noFramesOutStream, String chapterNo, HashMap sdisIncluded) throws SapphireException {
        CoverPageRO coverpageRO = new CoverPageRO();
        coverpageRO.initialize("Cover Page", this.sapphireConnection, this.folder, this.createdBy);
        coverpageRO.startChapter();
        CoverPageRenderer renderer = null;
        if (!this.includeDiffReport) {
            renderer = new CoverPageRenderer();
            renderer.initialize(this.sapphireConnection, this.config, coverpageRO, sdisIncluded);
        } else {
            CoverPageRO refCoverPageRO = new CoverPageRO();
            refCoverPageRO.initialize("Cover Page", this.folder, this.createdBy, this.refReportFolder, this.sapphireConnection);
            refCoverPageRO.startChapter();
            renderer = new CoverPageRenderer();
            renderer.initialize(this.sapphireConnection, this.config, coverpageRO, refCoverPageRO, sdisIncluded, this.includeDiffReport);
        }
        if (!this.frames) {
            String filePath = this.folder + File.separator + "html" + File.separator + "cover.html";
            try {
                FileOutputStream cpFile = new FileOutputStream(filePath);
                renderer.reportNoFrames(cpFile);
                cpFile.close();
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create the report file: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
            }
            catch (IOException e) {
                throw new SapphireException("Cannot close the report file: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
            }
        } else {
            renderer.reportWithFrames();
        }
        if (this.includeXMLReport) {
            renderer.createXMLReport();
        }
    }

    private void reportSDCChapter(OutputStream noFramesOutStream, String chapterNo, String sdcid, BaseSDCRenderer renderer) throws SapphireException {
        try {
            if (!this.frames) {
                this.reportNoFrames(renderer, chapterNo, sdcid, noFramesOutStream);
            } else {
                this.reportWithFrames(renderer, chapterNo, sdcid);
            }
        }
        catch (SapphireException e) {
            Trace.logError("Failed to create chapter: " + e.getMessage());
            throw new SapphireException(e);
        }
    }

    public void reportNoFrames(BaseSDCRenderer renderer, String chapterNo, String sdcid, OutputStream reportStream) throws SapphireException {
        BaseSDCRO ro = renderer.getSourceRO();
        BaseSDCRO refRO = renderer.getReferenceRO();
        this.configReportContent.startChapter(chapterNo, ro.getSDCName(), ro.getSDCDescription());
        int offset = 0;
        ArrayList mergedSdiList = (ArrayList)this.allSdis.get(sdcid);
        for (int section = 0; section < mergedSdiList.size(); ++section) {
            SDI currSDI = (SDI)mergedSdiList.get(section);
            if (ro != null && ro.gotoSection(currSDI) != -1) {
                ro.startSection();
            }
            if (refRO != null && refRO.gotoSection(currSDI) != -1) {
                refRO.startSection();
            }
            try {
                ConfigReportContent content = renderer.getSectionContent(renderer.getSourceRO(), renderer.getReferenceRO(), this.translationProcessor);
                this.configReportContent.append(content.toString());
                renderer.createSubSectionInfo(ro.getSDCName(), ConfigReportContent.generateSDISectionTitle(currSDI), content.diffInfo);
            }
            catch (Exception e) {
                Trace.log("Error: " + e.getMessage());
                throw new SapphireException("Failed to get section content:", e);
            }
            if (this.includeXMLReport) {
                renderer.createXMLReport();
            }
            ++offset;
        }
        if (renderer.reportSDIRoleMatrix) {
            this.configReportContent.setFoundDiff(false);
            String sdcname = "";
            sdcname = ro != null ? ro.getSDCName() : refRO.getSDCName();
            String sectionNo = chapterNo + "." + offset++;
            this.configReportContent.startSection("SDI Role Matrix");
            String accessControl = "";
            accessControl = ro != null ? ro.getAccessControl() : refRO.getAccessControl();
            if ("Y".equals(accessControl)) {
                this.configReportContent.append("<P>Access Control is set to 'Restrictive Role'. Note that the SDIs without roles will not be accessible to any user.<P>");
            } else if ("L".equals(accessControl)) {
                this.configReportContent.append("<P>Access Control is set to 'Open Role'. Note that the SDIs without roles will be accessible to all users.<P>");
            } else if ("D".equals(accessControl)) {
                this.configReportContent.append("<P>Access Control is set to 'Departmental'. Note that the roles assigned to SDIs will not used.<P>");
            } else if ("N".equals(accessControl)) {
                this.configReportContent.append("<P>Access Control is set to 'Not Implemented'. Note that the roles assigned to SDIs will not used.<P>");
            }
            if (ro == null) {
                this.configReportContent.renderRoleMatrix(refRO.getRoleMatrix(), refRO.getKeyColCount());
            } else if (refRO == null) {
                this.configReportContent.renderRoleMatrix(ro.getRoleMatrix(), ro.getKeyColCount());
            } else {
                String[] keycols = new String[ro.getKeyColCount()];
                for (int i = 0; i < ro.getKeyColCount(); ++i) {
                    keycols[i] = i == 0 ? ro.getKeyColId1() : (i == 1 ? ro.getKeyColId2() : ro.getKeyColId3());
                }
                this.configReportContent.renderDiffRoleMatrix(ro.getRoleMatrix(), refRO.getRoleMatrix(), keycols);
            }
            if (this.includeXMLReport && ro != null) {
                this.saveRoleMatrix(sdcname, ro.getRoleMatrix());
            }
            this.configReportContent.endSection();
            renderer.updateSectionChangeInfo(sdcname, ConfigReportContent.generateSectionTitle("SDI Role Matrix"), this.configReportContent);
            this.configReportContent.pageBreak();
        }
        if (renderer.hasCustomSections()) {
            ArrayList customSectionNames = renderer.getCustomSectionNames();
            for (int i = 0; i < customSectionNames.size(); ++i) {
                String currSectionName = customSectionNames.get(i).toString();
                this.configReportContent.appendSubSection(renderer.getCustomSectionContent(currSectionName), currSectionName, this.diffOnly);
            }
        }
        try {
            this.configReportContent.endChapter(chapterNo);
            reportStream.write(this.configReportContent.toString().getBytes());
        }
        catch (IOException e) {
            throw new SapphireException("Failed to append to report " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.sapphireConnection.getConnectionId())));
        }
    }

    public void reportWithFrames(BaseSDCRenderer renderer, String chapterNo, String sdcid) throws SapphireException {
        BaseSDCRO ro = renderer.getSourceRO();
        BaseSDCRO refRO = renderer.getReferenceRO();
        ArrayList mergedSdiList = (ArrayList)this.allSdis.get(sdcid);
        for (int section = 0; section < mergedSdiList.size(); ++section) {
            ConfigReportContent content;
            FileOutputStream sdiFile;
            SDI currSDI = (SDI)mergedSdiList.get(section);
            if (ro != null) {
                int ret = ro.gotoSection(currSDI);
                if (ret == -2) {
                    throw new SapphireException("Error initializing SDI:" + currSDI.getSdcid() + ":" + currSDI.getKeyid1());
                }
                if (ro.gotoSection(currSDI) != -1) {
                    ro.startSection();
                }
            }
            if (refRO != null && refRO.gotoSection(currSDI) != -1) {
                refRO.startSection();
            }
            if (renderer.checkIfIgnore(ro, refRO)) continue;
            String sectionFileName = ConfigReportContent.generateSDISectionFileName(currSDI);
            try {
                sdiFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report file " + sectionFileName);
            }
            this.configReportContent.clearContent();
            this.configReportContent.startFile(ConfigReportContent.generateSDISubSectionFileName(currSDI));
            try {
                content = renderer.getSectionContent(renderer.getSourceRO(), renderer.getReferenceRO(), this.translationProcessor);
            }
            catch (Exception e) {
                Trace.log("Failed to create section content for  " + sdcid + " " + e.getMessage());
                throw new SapphireException("Failed to create chapter for:" + sdcid + " ", e);
            }
            this.configReportContent.append(content.toString());
            renderer.createSubSectionInfo(sdcid, ConfigReportContent.generateSDISectionTitle(currSDI), content.diffInfo);
            this.configReportContent.endFile();
            try {
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter((OutputStream)sdiFile, "UTF8"));
                out.write(this.configReportContent.toString());
                ((Writer)out).flush();
                ((Writer)out).close();
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
            this.configReportContent.clearContent();
            if (!this.includeXMLReport) continue;
            renderer.createXMLReport();
        }
        if (renderer.reportSDIRoleMatrix) {
            FileOutputStream sdiFile;
            this.configReportContent.setFoundDiff(false);
            String sdcname = "";
            sdcname = ro != null ? ro.getSDCName() : refRO.getSDCName();
            String sectionFileName = ConfigReportContent.generateSectionFileName(sdcname, "SDI Role Matrix");
            try {
                sdiFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
            }
            catch (FileNotFoundException e) {
                throw new SapphireException("Cannot create report file " + sectionFileName);
            }
            this.configReportContent.startFile(ConfigReportContent.generateSubSectionFileName(sdcname, "SDI Role Matrix"));
            this.configReportContent.startSection("SDI Role Matrix");
            String accessControl = "";
            accessControl = ro != null ? ro.getAccessControl() : refRO.getAccessControl();
            if ("Y".equals(accessControl)) {
                this.configReportContent.append("<P>Access Control is set to 'Restrictive Role'. Note that the SDIs without roles will not be accessible to any user.<P>");
            } else if ("L".equals(accessControl)) {
                this.configReportContent.append("<P>Access Control is set to 'Open Role'. Note that the SDIs without roles will be accessible to all users.<P>");
            } else if ("D".equals(accessControl)) {
                this.configReportContent.append("<P>Access Control is set to 'Departmental'. Note that the roles assigned to SDIs will not used.<P>");
            } else if ("N".equals(accessControl)) {
                this.configReportContent.append("<P>Access Control is set to 'Not Implemented'. Note that the roles assigned to SDIs will not used.<P>");
            }
            if (ro == null) {
                this.configReportContent.renderRoleMatrix(refRO.getRoleMatrix(), refRO.getKeyColCount());
            } else if (refRO == null) {
                this.configReportContent.renderRoleMatrix(ro.getRoleMatrix(), ro.getKeyColCount());
            } else {
                String[] keycols = new String[ro.getKeyColCount()];
                for (int i = 0; i < ro.getKeyColCount(); ++i) {
                    keycols[i] = i == 0 ? ro.getKeyColId1() : (i == 1 ? ro.getKeyColId2() : ro.getKeyColId3());
                }
                this.configReportContent.renderDiffRoleMatrix(ro.getRoleMatrix(), refRO.getRoleMatrix(), keycols);
            }
            if (this.includeXMLReport && ro != null) {
                this.saveRoleMatrix(sdcname, ro.getRoleMatrix());
            }
            this.configReportContent.endSection();
            this.configReportContent.endFile();
            renderer.createSubSectionInfo(sdcid, ConfigReportContent.generateSectionTitle("SDI Role Matrix"), this.configReportContent.diffInfo);
            try {
                sdiFile.write(this.configReportContent.toString().getBytes());
            }
            catch (IOException e) {
                throw new SapphireException("Failed to create a section file");
            }
            renderer.updateSectionChangeInfo(sdcid, ConfigReportContent.generateSectionTitle("SDI Role Matrix"), this.configReportContent);
        }
        if (renderer.hasCustomSections()) {
            ArrayList customSectionNames = renderer.getCustomSectionNames();
            for (int i = 0; i < customSectionNames.size(); ++i) {
                FileOutputStream sdiFile;
                String currSectionName = customSectionNames.get(i).toString();
                String sectionFileName = ConfigReportContent.generateSectionFileName(sdcid, currSectionName);
                try {
                    sdiFile = new FileOutputStream(this.folder + File.separator + "html" + File.separator + sectionFileName);
                }
                catch (FileNotFoundException e) {
                    throw new SapphireException("Cannot create report file " + sectionFileName);
                }
                this.configReportContent.startFile(ConfigReportContent.generateSubSectionFileName(sdcid, currSectionName));
                this.configReportContent.append(renderer.getCustomSectionContent(currSectionName).toString());
                this.configReportContent.endFile();
                renderer.createSubSectionInfo(sdcid, currSectionName, this.configReportContent.diffInfo);
                try {
                    sdiFile.write(this.configReportContent.toString().getBytes());
                    continue;
                }
                catch (IOException e) {
                    throw new SapphireException("Failed to create a section file");
                }
            }
        }
    }

    public ArrayList determineSDCList(String[] excludeSdcList, String refReportFolder) throws SapphireException {
        String xmlReportFolder = refReportFolder + "/xmlreport/";
        String tocFileName = xmlReportFolder + "/toc.xml";
        File f = new File(tocFileName);
        try {
            String xml = FileUtil.getFileString(f);
            DataSet chapterDS = new DataSet(xml);
            String chapterNames = chapterDS.getColumnValues("chapter", ";");
            String[] list = StringUtil.split(chapterNames, ";");
            ArrayList<String> ret = new ArrayList<String>();
            for (int i = 0; i < list.length; ++i) {
                if (list[i].equals("Menu System") || list[i].equals("System Configuration")) continue;
                boolean exclude = false;
                if (list[i].equals("Generic Layout")) {
                    exclude = true;
                }
                if (excludeSdcList != null) {
                    for (int x = 0; x < excludeSdcList.length; ++x) {
                        if (!list[i].equals(excludeSdcList[x])) continue;
                        exclude = true;
                        break;
                    }
                }
                if (exclude) continue;
                ret.add(list[i]);
            }
            return ret;
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
            throw new SapphireException("SDI does not exist in the ref report ");
        }
    }

    public ArrayList determineChapterList(String refReportFolder) {
        String xmlReportFolder = refReportFolder + "/xmlreport/";
        String tocFileName = xmlReportFolder + "/toc.xml";
        File f = new File(tocFileName);
        try {
            String xml = FileUtil.getFileString(f);
            DataSet chapterDS = new DataSet(xml);
            String chapterNames = chapterDS.getColumnValues("chapter", ";");
            String[] list = StringUtil.split(chapterNames, ";");
            ArrayList<String> ret = new ArrayList<String>();
            for (int i = 0; i < list.length; ++i) {
                ret.add(list[i]);
            }
            return ret;
        }
        catch (IOException e) {
            Trace.log("SDI does not exist in the ref report");
            return null;
        }
    }

    public HashMap determineSDIHashMap(String[] excludeSdcList, ArrayList sdcList, String reportFolder) throws SapphireException {
        String filename = reportFolder + "/xmlreport/sdisincluded.xml";
        HashMap ret = null;
        File f = new File(filename);
        try {
            String xml = FileUtil.getFileString(f);
            DataSet sdisDS = new DataSet(xml);
            ArrayList finalSdcList = new ArrayList();
            for (int i = 0; i < sdcList.size(); ++i) {
                boolean exclude = false;
                if (excludeSdcList != null) {
                    for (int x = 0; x < excludeSdcList.length; ++x) {
                        if (!excludeSdcList[x].equals(sdcList.get(i).toString())) continue;
                        exclude = true;
                        break;
                    }
                }
                if (exclude) continue;
                finalSdcList.add(sdcList.get(i));
            }
            ret = this.convertToSDIHashMap(sdcList, sdisDS);
        }
        catch (IOException e) {
            Trace.log("failed to detemine sdi list" + e.getMessage());
            throw new SapphireException("failed to determine sdi list");
        }
        return ret;
    }

    private ArrayList convertToSDIList(DataSet sdisIncluded, boolean templatesonly) {
        ArrayList<SDI> sdiList = new ArrayList<SDI>();
        if (sdisIncluded == null || sdisIncluded.getRowCount() == 0) {
            return sdiList;
        }
        SDIRequest primaryRequest = new SDIRequest();
        primaryRequest.setRequestItem("primary");
        String sdcid = sdisIncluded.getString(0, PROPERTY_SDC);
        int keyCount = Integer.parseInt((String)this.sdcProcessor.getSDCProperties(sdcid).get("keycolumns"));
        primaryRequest.setSDCid(sdisIncluded.getString(0, PROPERTY_SDC));
        primaryRequest.setKeyid1List(sdisIncluded.getColumnValues(PROPERTY_KEYID1, ";"));
        if (keyCount > 1) {
            primaryRequest.setKeyid2List(sdisIncluded.getColumnValues(PROPERTY_KEYID2, ";"));
        }
        if (keyCount > 2) {
            primaryRequest.setKeyid3List(sdisIncluded.getColumnValues(PROPERTY_KEYID3, ";"));
        }
        SDIData data = this.sdiProcessor.getSDIData(primaryRequest);
        DataSet primary = data.getDataset("primary");
        for (int i = 0; i < primary.getRowCount(); ++i) {
            String templateFlag = primary.getString(i, "templateflag", "N");
            if (templatesonly && "N".equals(templateFlag)) continue;
            String keyid1 = primary.getString(i, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid1").toString());
            String keyid2 = "";
            if (keyCount > 1) {
                keyid2 = primary.getString(i, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid2").toString());
            }
            String keyid3 = "";
            if (keyCount > 2) {
                keyid3 = primary.getString(i, this.sdcProcessor.getSDCProperties(sdcid).get("keycolid3").toString());
            }
            SDI sdi = new SDI(sdcid, keyid1, keyid2, keyid3);
            sdiList.add(sdi);
        }
        return this.sortSDIList(sdiList, keyCount);
    }

    private void saveConfigReportPolicy(ConfigReportPolicyDef policyDef) throws SapphireException {
        try {
            PropertyList policy = policyDef.getPolicy();
            FileOutputStream optionsFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + "ConfigReportPolicy.xml");
            optionsFile.write(policy.toXMLString().getBytes());
            optionsFile.close();
            optionsFile = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + "config.xml");
            optionsFile.write(this.config.toXMLString().getBytes());
            optionsFile.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to write reportOptions file", e);
        }
    }

    private ArrayList sortSDIList(ArrayList sdiList, final int keyCount) {
        Collections.sort(sdiList, new Comparator(){

            public int compare(Object o1, Object o2) {
                SDI p1 = (SDI)o1;
                SDI p2 = (SDI)o2;
                String s1 = p1.getKeyid1();
                String s2 = p2.getKeyid1();
                if (keyCount > 1) {
                    s1 = p1.getKeyid1() + "," + p1.getKeyid2();
                    s2 = p2.getKeyid1() + "," + p2.getKeyid2();
                }
                if (keyCount > 2) {
                    s1 = p1.getKeyid1() + "," + p1.getKeyid2() + "," + p1.getKeyid3();
                    s2 = p2.getKeyid1() + "," + p2.getKeyid2() + "," + p2.getKeyid3();
                }
                return s1.compareToIgnoreCase(s2);
            }
        });
        return sdiList;
    }

    private boolean checkIfExistsInTOC(String chapterName, String reportFolder) {
        String tocFileName = reportFolder + "/xmlreport/toc.xml";
        File f = new File(tocFileName);
        try {
            String xml = FileUtil.getFileString(f);
            return xml.indexOf(chapterName) > -1;
        }
        catch (IOException e) {
            Trace.logError("Could not read TOC file:" + e.getMessage());
            return false;
        }
    }

    private HashMap mergeSdis(ArrayList allSdcs, HashMap sourceSdis, HashMap refSdis) {
        HashMap<String, ArrayList> merged = new HashMap<String, ArrayList>();
        for (int i = 0; i < allSdcs.size(); ++i) {
            String sdc = (String)allSdcs.get(i);
            ArrayList sourceSdisForSdc = null;
            if (sourceSdis.get(sdc) != null) {
                sourceSdisForSdc = (ArrayList)sourceSdis.get(sdc);
            }
            ArrayList refSdisForSdc = null;
            if (refSdis.get(sdc) != null) {
                refSdisForSdc = (ArrayList)refSdis.get(sdc);
            }
            if (sourceSdisForSdc == null) {
                merged.put(sdc, refSdisForSdc);
                continue;
            }
            if (refSdisForSdc == null) {
                merged.put(sdc, sourceSdisForSdc);
                continue;
            }
            merged.put(sdc, this.mergeSDILists(sourceSdisForSdc, refSdisForSdc));
        }
        return merged;
    }

    private ArrayList mergeSDILists(ArrayList source, ArrayList ref) {
        ArrayList merged = (ArrayList)source.clone();
        for (int i = 0; i < ref.size(); ++i) {
            SDI insertSDI = (SDI)ref.get(i);
            boolean exists = false;
            for (int j = 0; j < merged.size(); ++j) {
                SDI currSDI = (SDI)merged.get(j);
                if (!currSDI.getKeyid1().equals(insertSDI.getKeyid1()) || !currSDI.getKeyid2().equals(insertSDI.getKeyid2()) || !currSDI.getKeyid3().equals(insertSDI.getKeyid3())) continue;
                exists = true;
                break;
            }
            if (exists) continue;
            int findpos = -1;
            for (int j = 0; j < merged.size(); ++j) {
                SDI currSDI = (SDI)merged.get(j);
                if (currSDI.toString().toLowerCase().compareTo(insertSDI.toString().toLowerCase()) <= 0) continue;
                findpos = j;
                break;
            }
            if (findpos == -1) {
                merged.add(insertSDI);
                continue;
            }
            merged.add(findpos, insertSDI);
        }
        return merged;
    }

    private void saveRoleMatrix(String sdcid, DataSet roleMatrix) throws SapphireException {
        FileOutputStream file;
        String xml = roleMatrix.toXML();
        try {
            file = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + sdcid + "_sdirolematrix.xml");
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report xml file " + sdcid + "_sdirolematrix.xml");
        }
        try {
            file.write(xml.getBytes());
            file.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    private void saveSdisIncluded(HashMap sdisIncluded) throws SapphireException {
        FileOutputStream file;
        DataSet ds = this.convertToDataSet(sdisIncluded);
        try {
            file = new FileOutputStream(this.folder + File.separator + "xmlreport" + File.separator + "sdisincluded.xml");
        }
        catch (FileNotFoundException e) {
            throw new SapphireException("Cannot create report xml file sdisincluded");
        }
        try {
            file.write(ds.toXML().getBytes());
            file.close();
        }
        catch (IOException e) {
            throw new SapphireException("Failed to create a section file");
        }
    }

    private DataSet convertToDataSet(HashMap sdisIncluded) {
        Set sdis = sdisIncluded.keySet();
        Iterator iter = sdis.iterator();
        DataSet ret = new DataSet();
        while (iter.hasNext()) {
            String currsdc = iter.next().toString();
            ArrayList sdilist = (ArrayList)sdisIncluded.get(currsdc);
            for (int i = 0; i < sdilist.size(); ++i) {
                SDI curr = (SDI)sdilist.get(i);
                int currrow = ret.addRow();
                ret.setString(currrow, PROPERTY_SDC, curr.getSdcid());
                ret.setString(currrow, PROPERTY_KEYID1, curr.getKeyid1());
                ret.setString(currrow, PROPERTY_KEYID2, curr.getKeyid2());
                ret.setString(currrow, PROPERTY_KEYID3, curr.getKeyid3());
            }
        }
        return ret;
    }

    private HashMap convertToSDIHashMap(ArrayList sdcList, DataSet sdisIncludedDS) {
        HashMap sdisIncluded = new HashMap();
        for (int i = 0; i < sdcList.size(); ++i) {
            String currsdc = sdcList.get(i).toString();
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put(PROPERTY_SDC, currsdc);
            DataSet currSdiList = sdisIncludedDS.getFilteredDataSet(filter);
            ArrayList<SDI> arr = new ArrayList<SDI>();
            for (int j = 0; j < currSdiList.size(); ++j) {
                SDI curr = new SDI();
                curr.sdcid = currsdc;
                curr.keyid1 = currSdiList.getString(j, PROPERTY_KEYID1);
                curr.keyid2 = currSdiList.getString(j, PROPERTY_KEYID2);
                curr.keyid3 = currSdiList.getString(j, PROPERTY_KEYID3);
                arr.add(curr);
            }
            sdisIncluded.put(currsdc, arr);
        }
        return sdisIncluded;
    }

    private PropertyList loadReferencePolicyDetailsIfExists(String refReportFolder) throws SapphireException {
        String policyFileName = refReportFolder + File.separator + "xmlreport" + File.separator + "ConfigReportPolicy.xml";
        File f = new File(policyFileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList policy = new PropertyList();
                policy.setPropertyList(xml);
                return policy;
            }
        }
        catch (IOException e) {
            Trace.log("policy file content cannot be read");
        }
        return null;
    }

    private PropertyList loadConfigDetails(String refReportFolder) throws SapphireException {
        String fileName = refReportFolder + File.separator + "xmlreport" + File.separator + "config.xml";
        File f = new File(fileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList config = new PropertyList();
                config.setPropertyList(xml);
                return config;
            }
        }
        catch (IOException e) {
            Trace.log("config file content cannot be read");
        }
        return null;
    }

    private PropertyList loadReportOptions(String refReportFolder) throws SapphireException {
        String fileName = refReportFolder + File.separator + "xmlreport" + File.separator + "reportOptions.xml";
        File f = new File(fileName);
        try {
            if (f.exists()) {
                String xml = FileUtil.getFileString(f);
                PropertyList rep = new PropertyList();
                rep.setPropertyList(xml);
                return rep;
            }
        }
        catch (IOException e) {
            Trace.log("reportOptions file content cannot be read");
        }
        return null;
    }
}

