/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.gwt.shared.util.StringUtil;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import java.util.ArrayList;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class WorksheetItemOptions
implements ELNConstants {
    private PropertyList options;
    private ArrayList<String[]> requiresSDI = new ArrayList();
    private ArrayList<String> scriptIncludes = new ArrayList();
    private PropertyListCollection operations = new PropertyListCollection();

    public WorksheetItemOptions(String options) {
        this.options = new PropertyList();
        if (options != null && options.length() > 0) {
            try {
                if (options.trim().startsWith("<propertylist")) {
                    this.options.setPropertyList(options);
                } else {
                    this.options.setJSONString(options);
                }
            }
            catch (Exception e) {
                Trace.logError("Failed to parse worksheetitem options", e);
            }
        }
    }

    public void setName(String name) {
        this.options.setProperty("name", name);
    }

    public void setIsAlwaysLive(boolean alwaysLive) {
        this.options.setProperty("isalwayslive", alwaysLive ? "Y" : "N");
    }

    public void setHasExportHTML(boolean hasExportHTML) {
        this.options.setProperty("hasexporthtml", hasExportHTML ? "Y" : "N");
    }

    public void setAllowCaption(boolean allowCaption) {
        this.options.setProperty("allowcaption", allowCaption ? "Y" : "N");
    }

    public void setSupportsExport(boolean supportsExport) {
        this.options.setProperty("supportsexport", supportsExport ? "Y" : "N");
    }

    public void setSupportsHistory(boolean supportsHistory) {
        this.options.setProperty("supportshistory", supportsHistory ? "Y" : "N");
    }

    public void setSupportsHistoryDiffing(boolean supportsHistoryDiffing) {
        this.options.setProperty("supportshistorydiffing", supportsHistoryDiffing ? "Y" : "N");
    }

    public void setSupportsFields(boolean supportsFields) {
        this.options.setProperty("supportsfields", supportsFields ? "Y" : "N");
    }

    public void setConsumesFields(boolean consumesFields) {
        this.options.setProperty("consumesfields", consumesFields ? "Y" : "N");
    }

    public void setSupportsDataAvailablity(boolean supportsDataAvailablity) {
        this.options.setProperty("supportsdataavailability", supportsDataAvailablity ? "Y" : "N");
    }

    public void setSupportsQuerySDIs(boolean supportsQuerySDIs) {
        this.options.setProperty("supportsquerysdis", supportsQuerySDIs ? "Y" : "N");
    }

    public void setSupportsSDIs(boolean supportsSDIs, String source, String sdcid) {
        this.options.setProperty("supportssdis", supportsSDIs ? "Y" : "N");
        this.options.setProperty("defaultsource", supportsSDIs ? source : "");
        this.options.setProperty("defaultsdcid", supportsSDIs ? sdcid : "");
    }

    public void setDefaultSDCId(String sdcid) {
        this.options.setProperty("defaultsdcid", sdcid);
    }

    public void setViewerHTMLasTOC(boolean viewerHTMLasTOC) {
        this.options.setProperty("viewerhtmlastoc", viewerHTMLasTOC ? "Y" : "N");
    }

    public void setViewOnly(boolean viewOnly) {
        this.options.setProperty("viewonly", viewOnly ? "Y" : "N");
    }

    public void disableAttributes(boolean disable) {
        this.options.setProperty("disableitemattributes", disable ? "Y" : "N");
    }

    public void setRequiresConfig(boolean requiresConfig, String message) {
        this.options.setProperty("requiresconfig", requiresConfig ? "Y" : "N");
        this.options.setProperty("requiresconfigmessage", message);
        this.options.setProperty("viewonly", "Y");
    }

    public void setEditorAttributeId(String editorAttributeId) {
        this.options.setProperty("editorattributeid", editorAttributeId);
    }

    public void setEditorObjectName(String editorObjectName) {
        this.options.setProperty("editorobjectname", editorObjectName);
    }

    public void setEditorMaxSize(boolean maxsize) {
        this.options.setProperty("editormaxsize", maxsize ? "Y" : "N");
    }

    public void setDoNotCloseWithCancel(boolean doNotCloseWithCancel) {
        this.options.setProperty("donotclosewithcancel", doNotCloseWithCancel ? "Y" : "N");
    }

    public void addRequiresSDI(String sdcid, String plural, boolean fromWorksheet, boolean fromExternal, String linkPage, String linkPageMode, String linkPageText, String lookupPage, int min, int max) {
        this.requiresSDI.add(new String[]{sdcid, plural, fromWorksheet ? "Y" : "N", fromExternal ? "Y" : "N", linkPage, linkPageMode, linkPageText, lookupPage, String.valueOf(min), String.valueOf(max)});
    }

    public void setQuery(String queryid, PropertyListCollection params, String queryfrom, String querywhere) {
        this.options.setProperty("queryid", queryid);
        this.options.setProperty("queryparams", params != null ? params : new PropertyListCollection());
        this.options.setProperty("queryfrom", queryfrom);
        this.options.setProperty("querywhere", querywhere);
    }

    public void setIncludes(PropertyListCollection includes) {
        this.options.setProperty("includes", includes);
    }

    public void addOperations(PropertyListCollection operations) {
        if (operations != null && this.options.getProperty("defaultsdcid").length() > 0) {
            for (int i = 0; i < operations.size(); ++i) {
                PropertyList operation = operations.getPropertyList(i);
                if (operation.getProperty("type").equals("Menu")) {
                    PropertyListCollection menuitems = operation.getCollection("menuitems");
                    for (int j = 0; j < menuitems.size(); ++j) {
                        if (menuitems.getPropertyList(j).getProperty("sdcid").length() != 0) continue;
                        menuitems.getPropertyList(j).setProperty("sdcid", this.options.getProperty("defaultsdcid"));
                    }
                    continue;
                }
                if (operation.getProperty("sdcid").length() != 0) continue;
                operation.setProperty("sdcid", this.options.getProperty("defaultsdcid"));
            }
            this.operations = operations;
        }
    }

    public void addScriptInclude(String scriptfile) {
        this.scriptIncludes.add(scriptfile);
    }

    public String getOption(String option) {
        return this.options.getProperty(option);
    }

    public String getOption(String option, String optionDefault) {
        return this.options.getProperty(option, optionDefault);
    }

    public PropertyList toPropertyList() {
        PropertyListCollection requiresSDI = new PropertyListCollection();
        boolean sdiFromWorksheet = false;
        for (int i = 0; i < this.requiresSDI.size(); ++i) {
            PropertyList propertyList = new PropertyList();
            propertyList.setProperty("sdcid", this.requiresSDI.get(i)[0]);
            propertyList.setProperty("plural", StringUtil.initCaps(this.requiresSDI.get(i)[1]));
            propertyList.setProperty("fromworksheet", this.requiresSDI.get(i)[2]);
            propertyList.setProperty("fromexternal", this.requiresSDI.get(i)[3]);
            propertyList.setProperty("linkpage", this.requiresSDI.get(i)[4]);
            propertyList.setProperty("linkpagemode", this.requiresSDI.get(i)[5]);
            propertyList.setProperty("linkpagetext", this.requiresSDI.get(i)[6]);
            propertyList.setProperty("lookuppage", this.requiresSDI.get(i)[7]);
            propertyList.setProperty("min", this.requiresSDI.get(i)[8]);
            propertyList.setProperty("max", this.requiresSDI.get(i)[9]);
            requiresSDI.add(propertyList);
            if (!propertyList.getProperty("fromworksheet").equals("Y")) continue;
            sdiFromWorksheet = true;
        }
        this.options.setProperty("requiressdis", requiresSDI);
        this.options.setProperty("operations", this.operations);
        this.options.setProperty("allowworksheetsdis", sdiFromWorksheet ? "Y" : "N");
        return this.options;
    }

    public String toJSONString() {
        return this.toPropertyList().toJSONString();
    }
}

