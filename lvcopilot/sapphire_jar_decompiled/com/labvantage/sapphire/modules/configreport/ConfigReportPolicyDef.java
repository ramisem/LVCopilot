/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport;

import java.io.Serializable;
import sapphire.SapphireException;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ConfigReportPolicyDef
implements Serializable {
    public static final String SELECTION_TYPE_CATEGORIES = "Categories";
    public static final String SELECTION_TYPE_SDCLIST = "SelectedSdcs";
    private String title = "";
    private boolean show = true;
    private String categorylist = "";
    private String excludeSDCList = "";
    private boolean includeGenericLayout = false;
    private boolean includeSiteMapPageButtonRoleMatrix = false;
    private boolean includeSysConfig = false;
    private boolean includeSDIRoleMatrix = false;
    private boolean includeWebPagePageButtonRoleMatrix = false;
    private String customRendererPackage = "";
    private String selectionType = "";
    private PropertyListCollection selectedSdcs;
    private PropertyListCollection rendererOptions;
    private PropertyList tocRendererOptions;
    private PropertyList policy;

    public ConfigReportPolicyDef(PropertyList policy) throws SapphireException {
        if (policy == null) {
            throw new SapphireException("Policy is null.");
        }
        this.policy = policy;
        this.title = policy.getProperty("title", "Missing Title");
        this.show = !"N".equals(policy.getProperty("show"));
        this.selectionType = policy.getProperty("selectiontype", "");
        if (this.selectionType.equals(SELECTION_TYPE_CATEGORIES)) {
            this.determineCategoryList(policy);
        } else {
            this.selectedSdcs = policy.getCollectionNotNull("selectedsdcs");
        }
        this.customRendererPackage = policy.getProperty("customrendererpackage", "");
        PropertyList contentOptions = policy.getPropertyList("contentoptions");
        this.rendererOptions = policy.getCollectionNotNull("chapterrendereroptions");
        this.tocRendererOptions = policy.getPropertyListNotNull("tocrendereroptions");
        this.includeGenericLayout = !"N".equals(contentOptions.getProperty("includegenericlayout", "N"));
        this.includeSiteMapPageButtonRoleMatrix = !"N".equals(contentOptions.getProperty("sitemappagebuttonrolematrix", "N"));
        this.includeSysConfig = !"N".equals(contentOptions.getProperty("includesysconfig", "N"));
        this.includeSDIRoleMatrix = !"N".equals(contentOptions.getProperty("includesdirolematrix", "N"));
        this.includeWebPagePageButtonRoleMatrix = !"N".equals(contentOptions.getProperty("includewebpagepagebuttonrolematrix", "N"));
    }

    public String getRuleTitle() {
        return this.title;
    }

    public boolean show() {
        return this.show;
    }

    public String getCategoryList() {
        return this.categorylist;
    }

    public String getExcludeSDCList() {
        return this.excludeSDCList;
    }

    public String getSelectionType() {
        return this.selectionType;
    }

    public PropertyList getPolicy() {
        return this.policy;
    }

    public String getCustomRendererPackage() {
        return this.customRendererPackage;
    }

    public boolean getIncludeGenericLayout() {
        return this.includeGenericLayout;
    }

    public boolean getIncludeSiteMapPageButtonRoleMatrix() {
        return this.includeSiteMapPageButtonRoleMatrix;
    }

    public boolean getIncludeWebPagePageButtonRoleMatrix() {
        return this.includeWebPagePageButtonRoleMatrix;
    }

    public boolean getIncludeSysConfig() {
        return this.includeSysConfig;
    }

    public boolean getIncludeSDIRoleMatrix() {
        return this.includeSDIRoleMatrix;
    }

    public PropertyListCollection getSelectedSdcs() {
        return this.selectedSdcs;
    }

    private void determineCategoryList(PropertyList policy) {
        int i;
        PropertyList categories = policy.getPropertyListNotNull("categories");
        PropertyListCollection categorylistcoll = categories.getCollectionNotNull("categorylist");
        PropertyListCollection excludesdccoll = categories.getCollectionNotNull("excludesdcs");
        for (i = 0; i < categorylistcoll.size(); ++i) {
            PropertyList currCategoryPL = categorylistcoll.getPropertyList(i);
            String currCategory = currCategoryPL.getProperty("category");
            if (currCategory == null || currCategory.length() <= 0) continue;
            if (this.categorylist.length() > 0) {
                this.categorylist = this.categorylist + ";";
            }
            this.categorylist = this.categorylist + currCategory;
        }
        for (i = 0; i < excludesdccoll.size(); ++i) {
            String currExcludeSdc;
            PropertyList currExcludePL = excludesdccoll.getPropertyList(i);
            if (currExcludePL == null || (currExcludeSdc = currExcludePL.getProperty("sdcid")) == null || currExcludeSdc.length() <= 0) continue;
            if (this.excludeSDCList.length() > 0) {
                this.excludeSDCList = this.excludeSDCList + ";";
            }
            this.excludeSDCList = this.excludeSDCList + currExcludeSdc;
        }
    }

    public PropertyListCollection getRendererOptions(String chapterName) {
        if (this.rendererOptions != null) {
            PropertyList currChaptersOptions;
            int i;
            for (i = 0; i < this.rendererOptions.size(); ++i) {
                currChaptersOptions = this.rendererOptions.getPropertyList(i);
                if (!chapterName.equals(currChaptersOptions.getProperty("chapter"))) continue;
                return currChaptersOptions.getCollectionNotNull("optionlist");
            }
            chapterName = "Others";
            for (i = 0; i < this.rendererOptions.size(); ++i) {
                currChaptersOptions = this.rendererOptions.getPropertyList(i);
                if (!chapterName.equals(currChaptersOptions.getProperty("chapter"))) continue;
                return currChaptersOptions.getCollectionNotNull("optionlist");
            }
        }
        return new PropertyListCollection();
    }

    public PropertyList getTOCRendererOptions() {
        return this.tocRendererOptions;
    }

    public PropertyListCollection getIgnorePrimaryDiffs(String chapterName) {
        if (this.rendererOptions != null) {
            PropertyList currChaptersOptions;
            int i;
            for (i = 0; i < this.rendererOptions.size(); ++i) {
                currChaptersOptions = this.rendererOptions.getPropertyList(i);
                if (!chapterName.equals(currChaptersOptions.getProperty("chapter"))) continue;
                return currChaptersOptions.getPropertyList("ignorediffs").getCollectionNotNull("primarycolumns");
            }
            chapterName = "Others";
            for (i = 0; i < this.rendererOptions.size(); ++i) {
                currChaptersOptions = this.rendererOptions.getPropertyList(i);
                if (!chapterName.equals(currChaptersOptions.getProperty("chapter"))) continue;
                return currChaptersOptions.getPropertyList("ignorediffs").getCollectionNotNull("primarycolumns");
            }
        }
        return new PropertyListCollection();
    }

    public PropertyListCollection getIgnoreDetailsDiffs(String chapterName) {
        if (this.rendererOptions != null) {
            PropertyList currChaptersOptions;
            int i;
            for (i = 0; i < this.rendererOptions.size(); ++i) {
                currChaptersOptions = this.rendererOptions.getPropertyList(i);
                if (!chapterName.equals(currChaptersOptions.getProperty("chapter"))) continue;
                return currChaptersOptions.getPropertyList("ignorediffs").getCollectionNotNull("detailtablecolumns");
            }
            chapterName = "Others";
            for (i = 0; i < this.rendererOptions.size(); ++i) {
                currChaptersOptions = this.rendererOptions.getPropertyList(i);
                if (!chapterName.equals(currChaptersOptions.getProperty("chapter"))) continue;
                return currChaptersOptions.getPropertyList("ignorediffs").getCollectionNotNull("detailtablecolumns");
            }
        }
        return new PropertyListCollection();
    }
}

