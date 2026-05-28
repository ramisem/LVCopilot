/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.configreport.util;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.modules.configreport.ro.BaseRO;
import com.labvantage.sapphire.modules.configreport.ro.SDCRO;
import com.labvantage.sapphire.modules.configreport.util.DDTLabelsUtil;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.accessor.SDCProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseSDCRO;
import sapphire.ext.BaseSDCRenderer;
import sapphire.ext.ConfigReportContent;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDCUtil
extends BaseSDCRenderer {
    @Override
    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseSDCRO ro, BaseSDCRO refRO, HashMap sdisIncluded, boolean includeDiffReport, boolean includeSDIRoleMatrix) {
        super.initialize(sapphireConnection, config, ro, refRO, sdisIncluded, includeDiffReport, includeSDIRoleMatrix);
        this.sdcRO = (SDCRO)this.sourceRO;
        if (refRO != null) {
            this.refSdcRO = (SDCRO)this.refRO;
        }
        try {
            if (this.folder != null && this.folder.length() > 0) {
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/black_verticalline.gif"), new File(this.folder + "/images/black_verticalline.gif"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/flink_from.GIF"), new File(this.folder + "/images/flink_from.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/flink.GIF"), new File(this.folder + "/images/flink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/dlink.gif"), new File(this.folder + "/images/dlink.gif"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/lastdlink.GIF"), new File(this.folder + "/images/lastdlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/mlink.gif"), new File(this.folder + "/images/mlink.gif"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/redflink_from.GIF"), new File(this.folder + "/images/redflink_from.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/redflink.GIF"), new File(this.folder + "/images/redflink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/reddlink.GIF"), new File(this.folder + "/images/reddlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/redlastdlink.GIF"), new File(this.folder + "/images/redlastdlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/redmlink.GIF"), new File(this.folder + "/images/redmlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenflink_from.GIF"), new File(this.folder + "/images/greenflink_from.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenflink.GIF"), new File(this.folder + "/images/greenflink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greendlink.GIF"), new File(this.folder + "/images/greendlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenlastdlink.GIF"), new File(this.folder + "/images/greenlastdlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenmlink.GIF"), new File(this.folder + "/images/greenmlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangeflink_from.GIF"), new File(this.folder + "/images/orangeflink_from.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangeflink.GIF"), new File(this.folder + "/images/orangeflink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangedlink.GIF"), new File(this.folder + "/images/orangedlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangelastdlink.GIF"), new File(this.folder + "/images/orangelastdlink.GIF"));
                ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangemlink.GIF"), new File(this.folder + "/images/orangemlink.GIF"));
            }
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
    }

    @Override
    public void initialize(SapphireConnection sapphireConnection, PropertyList config, BaseRO ro, HashMap sdisIncluded, boolean includeSDIRoleMatrix) {
        super.initialize(sapphireConnection, config, ro, sdisIncluded, includeSDIRoleMatrix);
        this.sdcRO = (SDCRO)this.sourceRO;
        if (this.refRO != null) {
            this.refSdcRO = (SDCRO)this.refRO;
        }
        try {
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/black_verticalline.gif"), new File(this.folder + "/images/black_verticalline.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/flink_from.GIF"), new File(this.folder + "/images/flink_from.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/flink.GIF"), new File(this.folder + "/images/flink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/dlink.gif"), new File(this.folder + "/images/dlink.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/lastdlink.GIF"), new File(this.folder + "/images/lastdlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/mlink.gif"), new File(this.folder + "/images/mlink.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/redflink_from.GIF"), new File(this.folder + "/images/redflink_from.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/redflink.GIF"), new File(this.folder + "/images/redflink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/reddlink.GIF"), new File(this.folder + "/images/reddlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/redlastdlink.GIF"), new File(this.folder + "/images/redlastdlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/redmlink.GIF"), new File(this.folder + "/images/redmlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenflink_from.GIF"), new File(this.folder + "/images/greenflink_from.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenflink.GIF"), new File(this.folder + "/images/greenflink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greendlink.GIF"), new File(this.folder + "/images/greendlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenlastdlink.GIF"), new File(this.folder + "/images/greenlastdlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenmlink.GIF"), new File(this.folder + "/images/greenmlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangeflink_from.GIF"), new File(this.folder + "/images/orangeflink_from.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangeflink.GIF"), new File(this.folder + "/images/orangeflink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangedlink.GIF"), new File(this.folder + "/images/orangedlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangelastdlink.GIF"), new File(this.folder + "/images/orangelastdlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/orangemlink.GIF"), new File(this.folder + "/images/orangemlink.GIF"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/blackline.gif"), new File(this.folder + "/images/blackline.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/blueline.gif"), new File(this.folder + "/images/blueline.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/greenline.gif"), new File(this.folder + "/images/greenline.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/black_verticalline.gif"), new File(this.folder + "/images/black_verticalline.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/blackLline.gif"), new File(this.folder + "/images/blackLline.gif"));
            ConfigReportContent.copyFile(new File(this.applicationRoot + "/WEB-CORE/images/blackTline.gif"), new File(this.folder + "/images/blackTline.gif"));
        }
        catch (Exception e) {
            Trace.logError("Failed to copy image file", e);
        }
    }

    public static ConfigReportContent renderSDCInfo(SDCRO ro, TranslationProcessor translationProcessor) {
        return SDCUtil.renderSDCInfoDiff(ro.currentSDIData, ro.currentSDIData, false, translationProcessor);
    }

    public static String getCurrentSDCType(SDIData sdiData) {
        String colValue = SDCUtil.getPrimaryValue(sdiData, "sdctype");
        if ("C".equals(colValue)) {
            return "Core";
        }
        if ("S".equals(colValue)) {
            return "System";
        }
        if ("D".equals(colValue)) {
            return "Data";
        }
        return colValue;
    }

    public static ConfigReportContent renderSDCInfoDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns, TranslationProcessor translationProcessor) {
        ConfigReportContent configReportContent = new ConfigReportContent("SDC Info", translationProcessor);
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("SDC", SDCUtil.getPrimaryValue(srcSDIData, "sdcid"), SDCUtil.getPrimaryValue(refSDIData, "sdcid"));
        configReportContent.addDiffRowItem("Type", SDCUtil.getCurrentSDCType(srcSDIData), SDCUtil.getCurrentSDCType(refSDIData));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Table", SDCUtil.getPrimaryValue(srcSDIData, "tableid"), SDCUtil.getPrimaryValue(refSDIData, "tableid"), 3, translationProcessor);
        configReportContent.endRow();
        String srcdesc = SDCUtil.getPrimaryValue(srcSDIData, "sdcdesc");
        String refdesc = SDCUtil.getPrimaryValue(refSDIData, "sdcdesc");
        if (srcdesc.length() > 0 || refdesc.length() > 0 && !hideEmptyColumns) {
            configReportContent.startRow();
            configReportContent.addDiffRowItem("Description", srcdesc, refdesc, 3, translationProcessor);
            configReportContent.endRow();
        }
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Singular", SDCUtil.getPrimaryValue(srcSDIData, "singular"), SDCUtil.getPrimaryValue(refSDIData, "singular"), translationProcessor);
        configReportContent.addDiffRowItem("Plural", SDCUtil.getPrimaryValue(srcSDIData, "plural"), SDCUtil.getPrimaryValue(refSDIData, "plural"), translationProcessor);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Versioned", SDCUtil.getCurrentVersioned(srcSDIData), SDCUtil.getCurrentVersioned(refSDIData));
        configReportContent.addDiffRowItem("Usable Key Size", SDCUtil.getPrimaryValue(srcSDIData, "keyidusablesize"), SDCUtil.getPrimaryValue(refSDIData, "keyidusablesize"), translationProcessor);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Version Approval Type", SDCUtil.getPrimaryValue(srcSDIData, "versionapprovaltypeid"), SDCUtil.getPrimaryValue(refSDIData, "versionapprovaltypeid"), 3, translationProcessor);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Key Generation Rule", SDCUtil.getPrimaryValue(srcSDIData, "keygenerationrule"), SDCUtil.getPrimaryValue(refSDIData, "keygenerationrule"), 3, translationProcessor);
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Notes", SDCUtil.getPrimaryValue(srcSDIData, "notes"), SDCUtil.getPrimaryValue(refSDIData, "notes"), 3, translationProcessor);
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public static String getCurrentVersioned(SDIData sdiData) {
        String flag = SDCUtil.getPrimaryValue(sdiData, "versionedflag");
        if (flag == null) {
            return "";
        }
        if ("N".equals(flag.toString())) {
            return "No";
        }
        if ("Y".equals(flag.toString())) {
            return "Yes";
        }
        return "";
    }

    public ConfigReportContent renderSecurityOptions(SDCRO ro, TranslationProcessor translationProcessor) {
        return SDCUtil.renderSecurityOptionsDiff(ro.currentSDIData, ro.currentSDIData, translationProcessor);
    }

    public static ConfigReportContent renderSecurityOptionsDiff(SDIData srcSDIData, SDIData refSDIData, TranslationProcessor translationProcessor) {
        ConfigReportContent configReportContent = new ConfigReportContent("SDC security options:", translationProcessor);
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Access Control", SDCUtil.getCurrentAccessControl(srcSDIData), SDCUtil.getCurrentAccessControl(refSDIData));
        configReportContent.addDiffRowItem("Allow Chain of Custody", SDCUtil.getCurrentCOC(srcSDIData), SDCUtil.getCurrentCOC(refSDIData));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Audit Method", SDCUtil.getCurrentAuditMethod(srcSDIData), SDCUtil.getCurrentAuditMethod(refSDIData));
        configReportContent.addDiffRowItem("Audit Prompt Options", SDCUtil.getCurrentAuditPromptOptions(srcSDIData), SDCUtil.getCurrentAuditPromptOptions(refSDIData));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Reason Reference Type", SDCUtil.getPrimaryValue(srcSDIData, "reftypeid"), SDCUtil.getPrimaryValue(refSDIData, "reftypeid"));
        configReportContent.addDiffRowItem("Change Control", SDCUtil.getChangeControlledFlag(srcSDIData), SDCUtil.getChangeControlledFlag(refSDIData));
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public static String getCurrentAccessControl(SDIData sdiData) {
        String flag = SDCUtil.getPrimaryValue(sdiData, "accesscontrolledflag");
        if (flag == null) {
            return "";
        }
        if ("D".equals(flag.toString())) {
            return "Departmental";
        }
        if ("Y".equals(flag.toString())) {
            return "Role Level";
        }
        if ("N".equals(flag.toString())) {
            return "Not Implemented";
        }
        return "";
    }

    public static String getCurrentCOC(SDIData sdiData) {
        String flag = SDCUtil.getPrimaryValue(sdiData, "cocableflag");
        if (flag != null) {
            if ("N".equals(flag.toString())) {
                return "No";
            }
            if ("Y".equals(flag.toString())) {
                return "Yes";
            }
        }
        return "";
    }

    public static String getCurrentAuditMethod(SDIData sdiData) {
        String flag = SDCUtil.getPrimaryValue(sdiData, "auditedflag");
        if (flag == null) {
            return "";
        }
        if ("N".equals(flag.toString())) {
            return "None";
        }
        if ("B".equals(flag.toString())) {
            return "Background";
        }
        if ("P".equals(flag.toString())) {
            return "Prompt";
        }
        if ("W".equals(flag.toString())) {
            return "Prompt with Password";
        }
        return flag.toString();
    }

    public static String getChangeControlledFlag(SDIData sdiData) {
        String flag = SDCUtil.getPrimaryValue(sdiData, "changecontrolledflag");
        if (flag == null) {
            return "";
        }
        if ("N".equals(flag.toString())) {
            return "No";
        }
        if ("Y".equals(flag.toString())) {
            return "Yes";
        }
        if ("T".equals(flag.toString())) {
            return "Template Only";
        }
        if ("P".equals(flag.toString())) {
            return "Under Parent Change Control";
        }
        return flag.toString();
    }

    public static String getCurrentAuditPromptOptions(SDIData sdiData) {
        String flag = SDCUtil.getPrimaryValue(sdiData, "auditpromptflag");
        if (flag == null) {
            return "";
        }
        if ("R".equals(flag.toString())) {
            return "Standard reason required";
        }
        if ("F".equals(flag.toString())) {
            return "Free text reason required";
        }
        if ("S".equals(flag.toString())) {
            return "Standard reason optional";
        }
        if ("O".equals(flag.toString())) {
            return "Optional reason";
        }
        if ("B".equals(flag.toString())) {
            return "Free text or Optional reason";
        }
        if ("T".equals(flag.toString())) {
            return "Text or reason";
        }
        return flag.toString();
    }

    public ConfigReportContent renderDefinitionOptions(SDCRO ro, TranslationProcessor translationProcessor) {
        return SDCUtil.renderDefinitionOptionsDiff(ro.currentSDIData, ro.currentSDIData, translationProcessor);
    }

    public static ConfigReportContent renderDefinitionOptionsDiff(SDIData srcSDIData, SDIData refSDIData, TranslationProcessor translationProcessor) {
        ConfigReportContent configReportContent = new ConfigReportContent("SDC security options:", translationProcessor);
        configReportContent.startTable();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Allow categories", SDCUtil.getPrimaryFlag(srcSDIData, "categoriesflag"), SDCUtil.getPrimaryFlag(refSDIData, "categoriesflag"));
        configReportContent.addDiffRowItem("Allow data entry", SDCUtil.getPrimaryFlag(srcSDIData, "dataentryflag"), SDCUtil.getPrimaryFlag(refSDIData, "dataentryflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Allow specifications", SDCUtil.getPrimaryFlag(srcSDIData, "specflag"), SDCUtil.getPrimaryFlag(refSDIData, "specflag"));
        configReportContent.addDiffRowItem("Allow workflows", SDCUtil.getPrimaryFlag(srcSDIData, "workflowflag"), SDCUtil.getPrimaryFlag(refSDIData, "workflowflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Allow workitems", SDCUtil.getPrimaryFlag(srcSDIData, "workitemflag"), SDCUtil.getPrimaryFlag(refSDIData, "workitemflag"));
        configReportContent.addDiffRowItem("Allow contacts", SDCUtil.getPrimaryFlag(srcSDIData, "addressesflag"), SDCUtil.getPrimaryFlag(refSDIData, "addressesflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Allow attachments", SDCUtil.getPrimaryFlag(srcSDIData, "attachmentsflag"), SDCUtil.getPrimaryFlag(refSDIData, "attachmentsflag"));
        configReportContent.addDiffRowItem("Allow notes", SDCUtil.getPrimaryFlag(srcSDIData, "notesflag"), SDCUtil.getPrimaryFlag(refSDIData, "notesflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Allow templates", SDCUtil.getPrimaryFlag(srcSDIData, "templatableflag"), SDCUtil.getPrimaryFlag(refSDIData, "templatableflag"));
        configReportContent.addDiffRowItem("Allow aliases", SDCUtil.getPrimaryFlag(srcSDIData, "aliasableflag"), SDCUtil.getPrimaryFlag(refSDIData, "aliasableflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("May be scheduled", SDCUtil.getPrimaryFlag(srcSDIData, "scheduleableflag"), SDCUtil.getPrimaryFlag(refSDIData, "scheduleableflag"));
        configReportContent.addDiffRowItem("Allow coordinates", SDCUtil.getPrimaryFlag(srcSDIData, "coordinatableflag"), SDCUtil.getPrimaryFlag(refSDIData, "coordinatableflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Allow ad-hoc searching", SDCUtil.getPrimaryFlag(srcSDIData, "searchableflag"), SDCUtil.getPrimaryFlag(refSDIData, "searchableflag"));
        configReportContent.addDiffRowItem("Allow attributes", SDCUtil.getPrimaryFlag(srcSDIData, "allowattributesflag"), SDCUtil.getPrimaryFlag(refSDIData, "allowattributesflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Allow activate/deactivate", SDCUtil.getPrimaryFlag(srcSDIData, "activeableflag"), SDCUtil.getPrimaryFlag(refSDIData, "activeableflag"));
        configReportContent.addDiffRowItem("Use Effective Date", SDCUtil.getPrimaryFlag(srcSDIData, "versionuseeffectivedtflag"), SDCUtil.getPrimaryFlag(refSDIData, "versionuseeffectivedtflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Contains Sensitive Data?", SDCUtil.getPrimaryFlag(srcSDIData, "sensitivedataflag"), SDCUtil.getPrimaryFlag(refSDIData, "sensitivedataflag"));
        configReportContent.addDiffRowItem("Can be Masked?", SDCUtil.getPrimaryFlag(srcSDIData, "maskableflag"), SDCUtil.getPrimaryFlag(refSDIData, "maskableflag"));
        configReportContent.endRow();
        configReportContent.startRow();
        configReportContent.addDiffRowItem("Can be planned?", SDCUtil.getPrimaryFlag(srcSDIData, "plannableflag"), SDCUtil.getPrimaryFlag(refSDIData, "plannableflag"));
        configReportContent.addDiffRowItem("Support UUID", SDCUtil.getPrimaryFlag(srcSDIData, "uuidflag"), SDCUtil.getPrimaryFlag(refSDIData, "uuidflag"));
        configReportContent.endRow();
        configReportContent.endTable();
        return configReportContent;
    }

    public static String getPrimaryFlag(SDIData sdiData, String flagfield) {
        String flag = SDCUtil.getPrimaryValue(sdiData, flagfield);
        if (flag == null) {
            flag = "";
        }
        return SDCUtil.yesorno(flag.toString());
    }

    public static String yesorno(String flag) {
        if ("Y".equals(flag)) {
            return "Yes";
        }
        if ("N".equals(flag)) {
            return "No";
        }
        return "";
    }

    public ConfigReportContent renderTablesInfo(boolean reportSDCRelationshipModelOnly, SDCRO ro) throws SapphireException {
        return this.renderTablesInfoDiff(reportSDCRelationshipModelOnly, ro, ro);
    }

    public static ConfigReportContent renderTablesInfoDiff(SDIData srcSDIData, SDIData refSDIData, boolean hideEmptyColumns, TranslationProcessor translationProcessor, SDCProcessor sdcProcessor) throws SapphireException {
        DataSet match;
        ConfigReportContent configReportContent = new ConfigReportContent("SDC tables info:", translationProcessor);
        DataSet tableinfo = new DataSet();
        tableinfo.setColidCaseSensitive(true);
        tableinfo.addColumn("Table", 0);
        tableinfo.addColumn("Relation", 0);
        tableinfo.addColumn("Parent", 0);
        tableinfo.addColumn("Link id", 0);
        tableinfo.addColumn("Table Label", 0);
        tableinfo.addColumn("Item Display Format", 0);
        tableinfo.addColumn("Documentation", 0);
        String srcTableName = SDCUtil.getPrimaryValue(srcSDIData, "tableid");
        String refTableName = SDCUtil.getPrimaryValue(refSDIData, "tableid");
        HashMap srctableDocHM = SDCUtil.getTableDocHM(srcSDIData);
        HashMap reftableDocHM = SDCUtil.getTableDocHM(refSDIData);
        DataSet srcSysTable = srcSDIData.getDataset("systable");
        DataSet refSysTable = refSDIData.getDataset("systable");
        tableinfo.addRow();
        tableinfo.setString(0, "Table", srcTableName);
        tableinfo.setString(0, "Relation", "Primary");
        tableinfo.setString(0, "Parent", "");
        tableinfo.setString(0, "Link id", "");
        String sourcePrimaryTableLabel = "";
        String sourcePrimaryItemDisplay = "";
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("tableid", srcTableName);
        if (srcSysTable != null && (match = srcSysTable.getFilteredDataSet(filter)) != null && match.getRowCount() > 0) {
            sourcePrimaryTableLabel = match.getValue(0, "tablelabel");
            sourcePrimaryItemDisplay = match.getValue(0, "itemdisplay");
        }
        tableinfo.setString(0, "Table Label", sourcePrimaryTableLabel);
        tableinfo.setString(0, "Item Display Format", sourcePrimaryItemDisplay);
        String documentation = srctableDocHM.get(srcTableName) == null ? "" : srctableDocHM.get(srcTableName).toString();
        tableinfo.setString(0, "Documentation", documentation);
        DataSet srcdtypeLinks = SDCUtil.getDTypeTables(srcSDIData);
        for (int i = 0; i < srcdtypeLinks.size(); ++i) {
            DataSet match2;
            String linktableid = srcdtypeLinks.getString(i, "linktableid");
            tableinfo.addRow();
            tableinfo.setString(i + 1, "Table", linktableid);
            tableinfo.setString(i + 1, "Relation", "Detail");
            tableinfo.setString(i + 1, "Parent", srcTableName);
            tableinfo.setString(i + 1, "Link id", srcdtypeLinks.getString(i, "linkid"));
            filter = new HashMap();
            filter.put("tableid", linktableid);
            String srclinktablelabel = "";
            String srclinkitemdisplay = "";
            if (srcSysTable != null && (match2 = srcSysTable.getFilteredDataSet(filter)) != null && match2.getRowCount() > 0) {
                srclinktablelabel = match2.getString(0, "tablelabel");
                srclinkitemdisplay = match2.getString(0, "itemdisplay");
            }
            tableinfo.setString(i + 1, "Table Label", srclinktablelabel);
            tableinfo.setString(i + 1, "Item Display Format", srclinkitemdisplay);
            tableinfo.setString(i + 1, "Documentation", (String)srctableDocHM.get(linktableid));
        }
        DataSet reftableinfo = new DataSet();
        if (refTableName.length() > 0) {
            reftableinfo.setColidCaseSensitive(true);
            reftableinfo.addRow();
            reftableinfo.setString(0, "Table", refTableName);
            reftableinfo.setString(0, "Relation", "Primary");
            reftableinfo.setString(0, "Parent", "");
            reftableinfo.setString(0, "Link id", "");
            String reftablelabel = "";
            String refitemdisplay = "";
            if (refSysTable != null) {
                filter = new HashMap();
                filter.put("tableid", refTableName);
                DataSet match3 = refSysTable.getFilteredDataSet(filter);
                if (match3 != null && match3.getRowCount() > 0) {
                    reftablelabel = match3.getString(0, "tablelabel");
                    refitemdisplay = match3.getString(0, "itemdisplay");
                }
            }
            reftableinfo.setString(0, "Table Label", reftablelabel);
            reftableinfo.setString(0, "Item Display Format", refitemdisplay);
            reftableinfo.setString(0, "Documentation", (String)reftableDocHM.get(refTableName));
            DataSet refdtypeLinks = SDCUtil.getDTypeTables(refSDIData);
            for (int i = 0; i < refdtypeLinks.size(); ++i) {
                String refLinktableid = refdtypeLinks.getString(i, "linktableid");
                reftableinfo.addRow();
                reftableinfo.setString(i + 1, "Table", refLinktableid);
                reftableinfo.setString(i + 1, "Relation", "Detail");
                reftableinfo.setString(i + 1, "Parent", refTableName);
                reftableinfo.setString(i + 1, "Link id", refdtypeLinks.getString(i, "linkid"));
                String reflinktablelabel = "";
                String reflinkitemdisplay = "";
                if (refSysTable != null) {
                    filter = new HashMap();
                    filter.put("tableid", refLinktableid);
                    DataSet match4 = refSysTable.getFilteredDataSet(filter);
                    if (match4 != null && match4.getRowCount() > 0) {
                        reflinktablelabel = match4.getString(0, "tablelabel");
                        reflinkitemdisplay = match4.getString(0, "itemdisplay");
                    }
                }
                reftableinfo.setString(i + 1, "Table Label", reflinktablelabel);
                reftableinfo.setString(i + 1, "Item Display Format", reflinkitemdisplay);
                reftableinfo.setString(i + 1, "Documentation", (String)reftableDocHM.get(refLinktableid));
            }
        }
        String[] keycols = new String[]{"Table"};
        String tablelabel = "Tables";
        String itemdisplay = "[Table]";
        configReportContent.startSubSection(tablelabel, "");
        HashMap<String, String> columnTitleMap = DDTLabelsUtil.getColumnTitleMap(sdcProcessor, "systable", tableinfo.getColumns());
        configReportContent.renderDetailTablesDiff(columnTitleMap, "systable", tablelabel, itemdisplay, tableinfo, reftableinfo, keycols, translationProcessor, hideEmptyColumns);
        return configReportContent;
    }

    public static DataSet getDTypeTables(SDIData sdiData) {
        DataSet sdclinks = sdiData.getDataset("sdclink");
        if (sdclinks == null) {
            return new DataSet();
        }
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("linktype", "D");
        return sdclinks.getFilteredDataSet(filter);
    }

    public static HashMap getTableDocHM(SDIData sdiData) {
        HashMap<String, String> tableDocHM = new HashMap<String, String>();
        DataSet tableDocDS = SDCUtil.getTableDoc(sdiData);
        if (tableDocDS != null) {
            for (int i = 0; i < tableDocDS.size(); ++i) {
                tableDocHM.put(tableDocDS.getString(i, "tableid"), tableDocDS.getString(i, "tabledoc"));
            }
        }
        return tableDocHM;
    }

    public static DataSet getTableDoc(SDIData sdiData) {
        DataSet dtypeLinks = SDCUtil.getDTypeTables(sdiData);
        String dTypeTables = dtypeLinks.getColumnValues("linktableid", ";");
        String cuurentTablename = SDCUtil.getPrimaryValue(sdiData, "tableid");
        String tableids = dTypeTables + ";" + cuurentTablename;
        String[] tablelist = StringUtil.split(tableids, ";");
        DataSet tabledocDS = new DataSet();
        DataSet systable = sdiData.getDataset("systable");
        if (systable != null && systable.getRowCount() > 0) {
            tabledocDS.addColumn("tableid", 0);
            tabledocDS.addColumn("tabledoc", 0);
            for (int i = 0; i < tablelist.length; ++i) {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("tableid", tablelist[i]);
                DataSet match = systable.getFilteredDataSet(filter);
                tabledocDS.addRow();
                tabledocDS.setString(i, "tableid", tablelist[i]);
                tabledocDS.setString(i, "tabledoc", match.getString(0, "tabledoc", ""));
            }
        }
        return tabledocDS;
    }

    public ConfigReportContent renderTablesInfoDiff(boolean reportSDCRelationshipModelOnly, SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC tables info:");
        DataSet tableinfo = new DataSet();
        tableinfo.setColidCaseSensitive(true);
        tableinfo.addColumn("Table", 0);
        tableinfo.addColumn("Relation", 0);
        if (!reportSDCRelationshipModelOnly) {
            tableinfo.addColumn("Parent", 0);
            tableinfo.addColumn("Link id", 0);
        }
        tableinfo.addColumn("Table Label", 0);
        tableinfo.addColumn("Item Display", 0);
        tableinfo.addColumn("Documentation", 0);
        String currentTableName = sdcRO.getCurrentTableName();
        DataSet tableDoc = sdcRO.getTableDocInfo();
        tableinfo.addRow();
        tableinfo.setString(0, "Table", currentTableName);
        tableinfo.setString(0, "Relation", "Primary");
        if (!reportSDCRelationshipModelOnly) {
            tableinfo.setString(0, "Parent", "");
            tableinfo.setString(0, "Link id", "");
        }
        tableinfo.setString(0, "Table Label", tableDoc.getString(0, "tablelabel"));
        tableinfo.setString(0, "Item Display", tableDoc.getString(0, "itemdisplay"));
        tableinfo.setString(0, "Documentation", tableDoc.getString(0, "tabledoc"));
        DataSet dtypeLinks = sdcRO.getDTypeTables();
        for (int i = 0; i < dtypeLinks.size(); ++i) {
            String linktableid = dtypeLinks.getString(i, "linktableid");
            tableinfo.addRow();
            tableinfo.setString(i + 1, "Table", linktableid);
            tableinfo.setString(i + 1, "Relation", "Detail");
            if (!reportSDCRelationshipModelOnly) {
                tableinfo.setString(i + 1, "Parent", sdcRO.getCurrentTableName());
                tableinfo.setString(i + 1, "Link id", dtypeLinks.getString(i, "linkid"));
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("tableid", linktableid);
            DataSet match = tableDoc.getFilteredDataSet(filter);
            tableinfo.setString(i + 1, "Documentation", match.getString(0, "tabledoc"));
            tableinfo.setString(i + 1, "Table Label", match.getString(0, "tablelabel"));
            tableinfo.setString(i + 1, "Item Display", match.getString(0, "itemdisplay"));
        }
        DataSet reftableinfo = new DataSet();
        reftableinfo.setColidCaseSensitive(true);
        String refTableName = refSdcRO.getCurrentTableName();
        DataSet refTableDoc = refSdcRO.getTableDocInfo();
        reftableinfo.addRow();
        reftableinfo.setString(0, "Table", refTableName);
        reftableinfo.setString(0, "Relation", "Primary");
        if (!reportSDCRelationshipModelOnly) {
            reftableinfo.setString(0, "Parent", "");
            reftableinfo.setString(0, "Link id", "");
        }
        reftableinfo.setString(0, "Table Label", refTableDoc.getString(0, "tablelabel", ""));
        reftableinfo.setString(0, "Item Display", refTableDoc.getString(0, "itemdisplay", ""));
        reftableinfo.setString(0, "Documentation", refTableDoc.getString(0, "tabledoc", ""));
        DataSet refdtypeLinks = refSdcRO.getDTypeTables();
        for (int i = 0; i < refdtypeLinks.size(); ++i) {
            String refLinktableid = refdtypeLinks.getString(i, "linktableid");
            reftableinfo.addRow();
            reftableinfo.setString(i + 1, "Table", refLinktableid);
            reftableinfo.setString(i + 1, "Relation", "Detail");
            if (!reportSDCRelationshipModelOnly) {
                reftableinfo.setString(i + 1, "Parent", refSdcRO.getCurrentTableName());
                reftableinfo.setString(i + 1, "Link id", refdtypeLinks.getString(i, "linkid"));
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("tableid", refLinktableid);
            DataSet match = refTableDoc.getFilteredDataSet(filter);
            reftableinfo.setString(i + 1, "Table Label", match.getString(0, "tablelabel", ""));
            reftableinfo.setString(i + 1, "Item Display", match.getString(0, "itemdisplay", ""));
            reftableinfo.setString(i + 1, "Documentation", match.getString(0, "tabledoc", ""));
        }
        String[] keycols = new String[]{"Table"};
        PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("tables");
        configReportContent.renderDiffListTable(tableinfo, reftableinfo, keycols, ignoreDiffs, this.getTranslationProcessor());
        return configReportContent;
    }

    public ConfigReportContent renderColumnsInfo(boolean reportSDCRelationshipModelOnly, SDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC columns info:");
        String primaryTable = ro.getCurrentTableName();
        ConfigReportContent primary = new ConfigReportContent(this.config, "Primary");
        primary.startSubSection("Columns: " + primaryTable, "");
        primary.renderListTable(ro.getColumnsInfo(primaryTable, reportSDCRelationshipModelOnly), this.getTranslationProcessor());
        configReportContent.appendSubSection(primary, "Columns: " + primaryTable, this.diffOnly);
        DataSet tables = ro.getDTypeTables();
        for (int i = 0; i < tables.getRowCount(); ++i) {
            ConfigReportContent detailTables = new ConfigReportContent(this.config, "Detail Tables");
            String table = tables.getString(i, "linktableid");
            detailTables.startSubSection("Columns: " + table, "");
            detailTables.renderListTable(ro.getDetailsColumnsInfo(table), this.getTranslationProcessor());
            configReportContent.appendSubSection(detailTables, "Columns: " + table, this.diffOnly);
        }
        return configReportContent;
    }

    public ConfigReportContent renderIndexInfo(SDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC columns info:");
        String primaryTable = ro.getCurrentTableName();
        ConfigReportContent primary = new ConfigReportContent(this.config, "Primary");
        primary.startSubSection("Indexes: " + primaryTable, "");
        primary.renderListTable(this.getIndexInfo(ro, primaryTable), this.getTranslationProcessor());
        configReportContent.appendSubSection(primary, "Indexes: " + primaryTable, this.diffOnly);
        DataSet tables = ro.getDTypeTables();
        for (int i = 0; i < tables.getRowCount(); ++i) {
            ConfigReportContent detailTables = new ConfigReportContent(this.config, "Detail Tables");
            String table = tables.getString(i, "linktableid");
            detailTables.startSubSection("Indexes: " + table, "");
            detailTables.renderListTable(this.getIndexInfo(ro, table), this.getTranslationProcessor());
            configReportContent.appendSubSection(detailTables, "Indexes: " + table, this.diffOnly);
        }
        return configReportContent;
    }

    public ConfigReportContent renderAttrsInfo(SDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC attributes:");
        DataSet attrsinfo = this.getAttrsInfoFromRO(ro);
        configReportContent.renderListTable(attrsinfo, this.getTranslationProcessor());
        return configReportContent;
    }

    public ConfigReportContent renderOperationsInfo(SDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC operations:");
        DataSet opInfo = this.getOperationInfoFromRO(ro);
        configReportContent.renderListTable(opInfo, this.getTranslationProcessor());
        return configReportContent;
    }

    public ConfigReportContent renderColumnsInfoDiff(boolean reportSDCRelationshipModelOnly, SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC columns info:");
        String[] keycols = new String[]{"Column Name"};
        PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("syscolumn");
        ConfigReportContent primary = new ConfigReportContent(this.config, "Primary Columns");
        primary.renderDiffListTable(sdcRO.getColumnsInfo(sdcRO.getCurrentTableName(), reportSDCRelationshipModelOnly), refSdcRO.getColumnsInfo(refSdcRO.getCurrentTableName(), reportSDCRelationshipModelOnly), keycols, ignoreDiffs, this.getTranslationProcessor());
        configReportContent.appendSubSection(primary, "Columns: " + sdcRO.getCurrentTableName(), this.diffOnly);
        DataSet tables = sdcRO.getDTypeTables();
        for (int i = 0; i < tables.getRowCount(); ++i) {
            String table = tables.getString(i, "linktableid");
            ConfigReportContent detailTable = new ConfigReportContent(this.config, "Detail columns");
            detailTable.startSubSection("Columns: " + table, "");
            detailTable.renderDiffListTable(sdcRO.getDetailsColumnsInfo(table), refSdcRO.getDetailsColumnsInfo(table), keycols, ignoreDiffs, this.getTranslationProcessor());
            configReportContent.appendSubSection(detailTable, "Columns: " + table, this.diffOnly);
        }
        return configReportContent;
    }

    public ConfigReportContent renderLinksInfo(SDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC links:");
        DataSet linksinfo = this.getLinksInfoFromRO(ro);
        configReportContent.renderListTable(linksinfo, this.getTranslationProcessor());
        return configReportContent;
    }

    public ConfigReportContent renderLinksInfoDiff(SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC links:");
        DataSet linksinfo = this.getLinksInfoFromRO(sdcRO);
        DataSet reflinksinfo = this.getLinksInfoFromRO(refSdcRO);
        String[] keycols = new String[]{"Link Id"};
        PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("sdclink");
        configReportContent.renderDiffListTable(linksinfo, reflinksinfo, keycols, ignoreDiffs, this.getTranslationProcessor());
        return configReportContent;
    }

    public ConfigReportContent renderAttrsInfoDiff(SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC attributes:");
        DataSet attsinfo = this.getAttrsInfoFromRO(sdcRO);
        DataSet refattrsinfo = this.getAttrsInfoFromRO(refSdcRO);
        String[] keycols = new String[]{"Attribute Id"};
        PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("attributes");
        configReportContent.renderDiffListTable(attsinfo, refattrsinfo, keycols, ignoreDiffs, this.getTranslationProcessor());
        return configReportContent;
    }

    public ConfigReportContent renderOperationsDiff(SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC operations:");
        DataSet operInfo = this.getOperationInfoFromRO(sdcRO);
        DataSet refoperInfo = this.getOperationInfoFromRO(refSdcRO);
        String[] keycols = new String[]{"Operation"};
        PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("operations");
        configReportContent.renderDiffListTable(operInfo, refoperInfo, keycols, ignoreDiffs, this.getTranslationProcessor());
        return configReportContent;
    }

    public ConfigReportContent renderIndexInfoDiff(SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC index info:");
        String[] keycols = new String[]{"Name"};
        PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("syscolumn");
        ConfigReportContent primary = new ConfigReportContent(this.config, "Primary indexes");
        primary.renderDiffListTable(this.getIndexInfo(sdcRO, sdcRO.getCurrentTableName()), this.getIndexInfo(refSdcRO, refSdcRO.getCurrentTableName()), keycols, ignoreDiffs, this.getTranslationProcessor());
        configReportContent.appendSubSection(primary, "Indexes: " + sdcRO.getCurrentTableName(), this.diffOnly);
        DataSet tables = sdcRO.getDTypeTables();
        for (int i = 0; i < tables.getRowCount(); ++i) {
            String table = tables.getString(i, "linktableid");
            ConfigReportContent detailTable = new ConfigReportContent(this.config, "Detail indexes");
            detailTable.startSubSection("Indexes: " + table, "");
            detailTable.renderDiffListTable(this.getIndexInfo(sdcRO, table), this.getIndexInfo(refSdcRO, table), keycols, ignoreDiffs, this.getTranslationProcessor());
            configReportContent.appendSubSection(detailTable, "Indexes: " + table, this.diffOnly);
        }
        return configReportContent;
    }

    private String getRefType(String refTypeFlag) {
        if ("P".equals(refTypeFlag)) {
            return "Primary Key Index";
        }
        if ("U".equals(refTypeFlag)) {
            return "Unique Index";
        }
        if ("I".equals(refTypeFlag)) {
            return "Non-unique Index";
        }
        return "Foreign Key";
    }

    private String getDateType(String dataTypeFlag) {
        if ("S".equals(dataTypeFlag)) {
            return "String";
        }
        if ("N".equals(dataTypeFlag)) {
            return "Number";
        }
        if ("D".equals(dataTypeFlag)) {
            return "Date";
        }
        return "Clob";
    }

    private String YesNo(String yesNoFlag) {
        if ("Y".equals(yesNoFlag)) {
            return "Yes";
        }
        return "No";
    }

    protected ConfigReportContent renderSDCSummary(SDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC summary:");
        String sdcid = ro.getCurrentSDCName();
        String sdcType = ro.getCurrentSDCType();
        String versioned = ro.getCurrentVersioned();
        String keygeneration = ro.getKeyGeneration();
        String summary = "";
        summary = sdcid + " is a " + sdcType + " SDC. ";
        summary = versioned.equals("Yes") ? summary + " It is versioned " : summary + " It is not versioned ";
        summary = keygeneration.length() > 0 && keygeneration.startsWith("A") ? summary + " and autokey generation is enabled." : summary + " and autokey generation is not enabled.";
        configReportContent.append("<P>");
        configReportContent.append(summary);
        configReportContent.append("<P>");
        return configReportContent;
    }

    public ConfigReportContent renderSDCSummaryDiff(SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC summary:");
        String sdcid = sdcRO.getCurrentSDCName();
        String sdcType = sdcRO.getCurrentSDCType();
        String versioned = sdcRO.getCurrentVersioned();
        String keygeneration = sdcRO.getKeyGeneration();
        String summary = "";
        String refSdcType = refSdcRO.getCurrentSDCType();
        String refVersioned = refSdcRO.getCurrentVersioned();
        String refKeyGeneration = refSdcRO.getKeyGeneration();
        if (!refSdcType.equals(sdcType)) {
            sdcType = sdcType + " " + ConfigReportContent.getDeletedString(refSdcType);
        }
        summary = sdcid + " is a " + sdcType + " SDC. ";
        summary = !versioned.equals(refVersioned) ? (versioned.equals("Yes") ? summary + " It is versioned " + ConfigReportContent.getDeletedString("not versioned") : summary + " It is not versioned " + ConfigReportContent.getDeletedString("versioned")) : (versioned.equals("Yes") ? summary + " It is versioned " : summary + " It is not versioned ");
        summary = !keygeneration.equals(refKeyGeneration) ? (keygeneration.length() > 0 ? summary + " and autokey generation is enabled " + ConfigReportContent.getDeletedString("not enabled") + "." : summary + " and autokey generation is not enabled " + ConfigReportContent.getDeletedString("enabled") + ".") : (keygeneration.length() > 0 ? summary + " and autokey generation is enabled." : summary + " and autokey generation is not enabled.");
        configReportContent.append("<P>");
        configReportContent.append(summary);
        configReportContent.append("<P>");
        return configReportContent;
    }

    protected ConfigReportContent renderDetails(SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC details");
        if (this.includeDiffReport) {
            if (sdcRO == null || sdcRO.currentSDI == null) {
                configReportContent.startSubSection("Primary", "");
                configReportContent.appendSubSection(this.renderPrimary(refSdcRO), "Primary", this.diffOnly);
                configReportContent.appendSpecialContent(this.renderSDIDetails(refSdcRO), this.diffOnly);
                configReportContent.startSubSection("Indexes", "");
                ConfigReportContent index = new ConfigReportContent(this.config, "Indexes");
                index.renderListTable(refSdcRO.currentIndexInfo, this.getTranslationProcessor());
                configReportContent.appendSubSection(index, "Indexes", this.diffOnly);
            } else if (this.refRO == null || refSdcRO.currentSDI == null) {
                configReportContent.startSubSection("Primary", "");
                configReportContent.appendSubSection(this.renderPrimary(sdcRO), "Primary", this.diffOnly);
                configReportContent.appendSpecialContent(this.renderSDIDetails(sdcRO), this.diffOnly);
                configReportContent.startSubSection("Indexes", "");
                ConfigReportContent index = new ConfigReportContent(this.config, "Indexes");
                index.renderListTable(sdcRO.currentIndexInfo, this.getTranslationProcessor());
                configReportContent.appendSubSection(index, "Indexes", this.diffOnly);
            } else {
                configReportContent.startSubSection("Primary", "");
                configReportContent.appendSubSection(this.renderPrimaryDiff(), "Primary", this.diffOnly);
                configReportContent.appendSpecialContent(this.renderSDIDetailsDiff(sdcRO, refSdcRO), this.diffOnly);
                configReportContent.startSubSection("Indexes", "");
                String[] keycols = new String[]{"tableid", "columnid"};
                ConfigReportContent index = new ConfigReportContent(this.config, "Indexes");
                PropertyListCollection ignoreDiffs = this.getIgnoreDetailsDiffCols("indexes");
                index.renderDiffListTable(sdcRO.currentIndexInfo, refSdcRO.currentIndexInfo, keycols, ignoreDiffs, this.getTranslationProcessor());
                configReportContent.appendSubSection(index, "Indexes", this.diffOnly);
            }
        } else {
            configReportContent.startSubSection("Primary", "");
            configReportContent.appendSubSection(this.renderPrimary(sdcRO), "Primary", this.diffOnly);
            configReportContent.appendSpecialContent(this.renderSDIDetails(sdcRO), this.diffOnly);
            configReportContent.startSubSection("Indexes", "");
            ConfigReportContent index = new ConfigReportContent(this.config, "Indexes");
            index.renderListTable(sdcRO.currentIndexInfo, this.getTranslationProcessor());
            configReportContent.appendSubSection(index, "Indexes", this.diffOnly);
        }
        return configReportContent;
    }

    private ConfigReportContent renderPrimary(SDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC primary:");
        configReportContent.startTable();
        ArrayList columns = ro.getPrimaryColumns();
        for (int i = 0; i < columns.size(); ++i) {
            configReportContent.startRow();
            String currColumn = (String)columns.get(i);
            configReportContent.addRowItem(currColumn, ro.getPrimaryValue(currColumn));
            if (i + 1 < columns.size()) {
                currColumn = (String)columns.get(i + 1);
                configReportContent.addRowItem(currColumn, ro.getPrimaryValue(currColumn));
                ++i;
            }
            configReportContent.endRow();
        }
        configReportContent.endTable();
        return configReportContent;
    }

    private ConfigReportContent renderPrimaryDiff() {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC primary");
        configReportContent.startTable();
        ArrayList columns = this.sdcRO.getPrimaryColumns();
        if (this.includeDiffReport && this.refSdcRO != null && this.refSdcRO.currentSDI != null) {
            for (int i = 0; i < columns.size(); ++i) {
                configReportContent.startRow();
                String currColumn = (String)columns.get(i);
                configReportContent.addDiffRowItem(currColumn, this.sdcRO.getPrimaryValue(currColumn), this.refSdcRO.getPrimaryValue(currColumn), this.ignoreDiff(currColumn), this.getTranslationProcessor());
                if (i + 1 < columns.size()) {
                    currColumn = (String)columns.get(i + 1);
                    configReportContent.addDiffRowItem(currColumn, this.sdcRO.getPrimaryValue(currColumn), this.refSdcRO.getPrimaryValue(currColumn), this.ignoreDiff(currColumn), this.getTranslationProcessor());
                    ++i;
                }
                configReportContent.endRow();
            }
        }
        configReportContent.endTable();
        return configReportContent;
    }

    private ConfigReportContent renderSDIDetails(SDCRO ro) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC details:");
        String[] detailTables = ro.getDetailNames();
        if (detailTables == null) {
            return configReportContent;
        }
        for (int i = 0; i < detailTables.length; ++i) {
            if (ro.getDataSet(detailTables[i]) == null || ro.getDataSet(detailTables[i]).getRowCount() <= 0) continue;
            configReportContent.startSubHeading(detailTables[i], "");
            DataSet clean = this.removeAuditColumns(ro.getDataSet(detailTables[i]));
            configReportContent.renderListTable(clean, this.getTranslationProcessor());
        }
        return configReportContent;
    }

    private ConfigReportContent renderSDIDetailsDiff(SDCRO sdcRO, SDCRO refSdcRO) {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC details");
        String[] detailTables = sdcRO.getDetailNames();
        if (detailTables == null) {
            return configReportContent;
        }
        for (int i = 0; i < detailTables.length; ++i) {
            if (sdcRO.getDataSet(detailTables[i]) == null || sdcRO.getDataSet(detailTables[i]).getRowCount() <= 0) continue;
            configReportContent.startSubSection(detailTables[i], "");
            String[] keycols = sdcRO.getDataSetKeyCols(detailTables[i]);
            DataSet cleanSrc = this.removeAuditColumns(sdcRO.getDataSet(detailTables[i]));
            DataSet cleanRef = this.removeAuditColumns(refSdcRO.getDataSet(detailTables[i]));
            ConfigReportContent detail = new ConfigReportContent(this.config, detailTables[i]);
            PropertyListCollection ignoreDiffCols = this.getIgnoreDetailsDiffCols(detailTables[i]);
            detail.renderDiffListTable(cleanSrc, cleanRef, keycols, ignoreDiffCols, this.getTranslationProcessor());
            configReportContent.appendSubSection(detail, detailTables[i], this.diffOnly);
        }
        return configReportContent;
    }

    public ConfigReportContent renderRelationships(SDCRO ro, boolean configreport) throws SapphireException {
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC relationships:");
        DataSet dtypes = ro.getDTypeTables();
        configReportContent.append(this.drawModel(dtypes, ro.getRefersToSDCs(), ro.getReferredBySDCs(), ro.getReferredBySDCDetails(), ro.getCurrentSDCName(), ro.getCurrentSDCDescription(), configreport).toString());
        return configReportContent;
    }

    public ConfigReportContent renderRelationshipsDiff(SDCRO sdcRO, SDCRO refSdcRO, boolean configreport) {
        return this.drawModel(sdcRO, refSdcRO, configreport);
    }

    private ConfigReportContent drawModel(SDCRO ro, SDCRO refRO, boolean configreport) {
        String col2;
        String linkcolumns;
        int i;
        String fklink;
        String color;
        int i2;
        DataSet details = ro.getDTypeTables();
        DataSet refersTo = ro.getRefersToSDCs();
        DataSet referredBy = ro.getReferredBySDCs();
        DataSet referredByDetails = ro.getReferredBySDCDetails();
        DataSet refDetails = refRO.getDTypeTables();
        DataSet refRefersTo = refRO.getRefersToSDCs();
        DataSet refReferredBy = refRO.getReferredBySDCs();
        DataSet refReferredByDetails = refRO.getReferredBySDCDetails();
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC draw model:");
        details = configReportContent.addDiffInfo(details, refDetails, new String[]{"linktableid"});
        refersTo = configReportContent.addDiffInfo(refersTo, refRefersTo, new String[]{"linkid"});
        referredBy = configReportContent.addDiffInfo(referredBy, refReferredBy, new String[]{"linkid", "sdcid"});
        referredByDetails = configReportContent.addDiffInfo(referredByDetails, refReferredByDetails, new String[]{"detaillinkid", "sdcid"});
        if (refersTo == null) {
            refersTo = new DataSet();
        }
        StringBuffer firstcolumn = new StringBuffer();
        firstcolumn.append("<TABLE class=\"datamodel\">");
        for (i2 = 0; i2 < referredBy.getRowCount(); ++i2) {
            firstcolumn.append("<TR><TD height=\"30\"  style=\"padding:0;border-spacing: 0;\" /></TR>");
            color = "black";
            if (referredBy.getString(i2, "_status").equals("New")) {
                color = "green";
            } else if (referredBy.getString(i2, "_status").equals("Modified")) {
                color = "orange";
            } else if (referredBy.getString(i2, "_status").equals("Deleted")) {
                color = "red";
            }
            firstcolumn.append("<TR><TD height=\"30\" width=\"80\" align=\"center\"  title=\"" + referredBy.getString(i2, "sdcdesc", "TBD") + "\"  class=\"datamodelsdc\" >");
            fklink = ConfigReportContent.createHyperLink("SDC", referredBy.getString(i2, "sdcid") + "(" + referredBy.getString(i2, "tableid") + ")", "", "", this.sdisIncluded, this.frames);
            firstcolumn.append(fklink);
            firstcolumn.append("</TD></TR>");
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\"/></TR>");
        }
        for (i2 = 0; i2 < referredByDetails.getRowCount(); ++i2) {
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
            color = "black";
            if (referredByDetails.getString(i2, "_status").equals("New")) {
                color = "green";
            } else if (referredByDetails.getString(i2, "_status").equals("Modified")) {
                color = "orange";
            } else if (referredByDetails.getString(i2, "_status").equals("Deleted")) {
                color = "red";
            }
            firstcolumn.append("<TR><TD height=\"30\" title=\"" + referredByDetails.getString(i2, "sdcdesc") + "\" class=\"datamodelsdc\">");
            fklink = ConfigReportContent.createHyperLink("SDC", referredByDetails.getString(i2, "sdcid"), "", "", this.sdisIncluded, this.frames);
            firstcolumn.append(fklink + "(" + referredByDetails.getString(i2, "linktableid") + ")");
            firstcolumn.append("</TD></TR>");
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\"/></TR>");
        }
        firstcolumn.append("</TABLE>");
        StringBuffer after1 = new StringBuffer();
        after1.append("<TABLE class=\"datamodel\">");
        for (i = 0; i < referredBy.getRowCount(); ++i) {
            after1.append("<TR>");
            after1.append("<TD height=\"30\" width=\"300\" align=\"center\" style=\"padding:0;border-spacing: 0;\">");
            if (referredBy.getString(i, "_status").equals("New")) {
                after1.append("<font color=\"green\" style=\"background-color:yellow\">");
            } else if (referredBy.getString(i, "_status").equals("Deleted")) {
                after1.append("<font color=\"red\"><strike>");
            } else if (referredBy.getString(i, "_status").equals("Modified")) {
                after1.append("<font color=\"orange\">");
            }
            if ("F".equals(referredBy.getString(i, "linktype", ""))) {
                linkcolumns = referredBy.getString(i, "sdccolumnid", "");
                col2 = referredBy.getString(i, "sdccolumnid2");
                if (col2 != null && col2.length() > 0) {
                    linkcolumns = linkcolumns + ", ";
                    linkcolumns = linkcolumns + col2;
                }
                after1.append(referredBy.getString(i, "linkid") + "(" + linkcolumns + ")");
            } else {
                after1.append(referredBy.getString(i, "linkid"));
            }
            if (referredBy.getString(i, "_status", "").equals("New")) {
                after1.append("</font>");
            } else if (referredBy.getString(i, "_status", "").equals("Deleted")) {
                after1.append("</strike></font>");
            } else if (referredBy.getString(i, "_status", "").equals("Modified")) {
                after1.append("</font>");
            }
            after1.append("</TD>");
            after1.append("</TR>");
            after1.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if (referredBy.getString(i, "linktype", "").equals("F")) {
                if (referredBy.getString(i, "_status", "").equals("New")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\greenflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\greenflink_from.GIF\"/>");
                    }
                } else if (referredBy.getString(i, "_status", "").equals("Deleted")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\redflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\redflink_from.GIF\"/>");
                    }
                } else if (referredBy.getString(i, "_status", "").equals("Modified")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\orangeflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\orangeflink_from.GIF\"/>");
                    }
                } else if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\flink_from.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\flink_from.GIF\"/>");
                }
            } else if (referredBy.getString(i, "_status", "").equals("New")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\greenmlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\greenmlink.GIF\"/>");
                }
            } else if (referredBy.getString(i, "_status", "").equals("Deleted")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\redmlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\redmlink.GIF\"/>");
                }
            } else if (referredBy.getString(i, "_status", "").equals("Modified")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\orangemlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\orangemlink.GIF\"/>");
                }
            } else if (configreport) {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            after1.append("</TD></TR>");
            after1.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
        }
        for (i = 0; i < referredByDetails.getRowCount(); ++i) {
            after1.append("<TR>");
            after1.append("<TD height=\"30\" width=\"300\" align=\"center\" style=\"padding:0;border-spacing: 0;\">");
            if (referredByDetails.getString(i, "_status", "").equals("New")) {
                after1.append("<font color=\"green\"  style=\"background-color:yellow\">");
            } else if (referredByDetails.getString(i, "_status", "").equals("Deleted")) {
                after1.append("<font color=\"red\"><strike>");
            } else if (referredByDetails.getString(i, "_status", "").equals("Modified")) {
                after1.append("<font color=\"orange\">");
            }
            if (referredByDetails.getString(i, "linktype", "").equals("F")) {
                linkcolumns = referredByDetails.getString(i, "sdccolumnid", "");
                col2 = referredByDetails.getString(i, "sdccolumnid2", "");
                if (col2 != null && col2.length() > 0) {
                    linkcolumns = linkcolumns + ", ";
                    linkcolumns = linkcolumns + col2;
                }
                after1.append(referredByDetails.getString(i, "detaillinkid", "") + "(" + linkcolumns + ")");
            } else {
                after1.append(referredByDetails.getString(i, "detaillinkid", ""));
            }
            if (referredByDetails.getString(i, "_status", "").equals("New")) {
                after1.append("</font>");
            } else if (referredByDetails.getString(i, "_status", "").equals("Deleted")) {
                after1.append("</strike></font>");
            } else if (referredByDetails.getString(i, "_status", "").equals("Modified")) {
                after1.append("</font>");
            }
            after1.append("</TD>");
            after1.append("</TR>");
            after1.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if (referredByDetails.getString(i, "linktype", "").equals("F")) {
                if (referredByDetails.getString(i, "_status").equals("New")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\greenflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\greenflink_from.GIF\"/>");
                    }
                } else if (referredByDetails.getString(i, "_status", "").equals("Deleted")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\redflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\redflink_from.GIF\"/>");
                    }
                } else if (referredByDetails.getString(i, "_status", "").equals("Modified")) {
                    if (configreport) {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\orangeflink_from.GIF\"/>");
                    } else {
                        after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\orangeflink_from.GIF\"/>");
                    }
                } else if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\flink_from.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\flink_from.GIF\"/>");
                }
            } else if (referredByDetails.getString(i, "_status", "").equals("New")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\greenmlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\greenmlink.GIF\"/>");
                }
            } else if (referredByDetails.getString(i, "_status", "").equals("Deleted")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\redmlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\redmlink.GIF\"/>");
                }
            } else if (referredByDetails.getString(i, "_status", "").equals("Modified")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\orangemlink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\orangemlink.GIF\"/>");
                }
            } else if (configreport) {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            after1.append("</TD></TR>");
            after1.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
        }
        after1.append("</TABLE>");
        int referredByCount = referredBy.getRowCount() + referredByDetails.getRowCount();
        int secondcolumnlen = referredByCount > refersTo.getRowCount() ? referredByCount : refersTo.getRowCount();
        StringBuffer secondcolumn = new StringBuffer();
        int secondcolumnheight = 90 * secondcolumnlen;
        secondcolumn.append("<TABLE  width=\"250\" class=\"datamodel\">");
        secondcolumn.append("<TR><TD title=\"" + ro.getCurrentSDCName() + "(" + ro.getCurrentSDCDescription() + ")\" align=\"center\" height=\"" + secondcolumnheight + "\"  class=\"datamodelsdc\" >");
        secondcolumn.append(ro.getCurrentSDCName());
        secondcolumn.append("</TD></TR>");
        StringBuffer detailInfo = new StringBuffer();
        detailInfo.append("<TABLE class=\"datamodel\" >");
        for (int i3 = 0; i3 < details.getRowCount(); ++i3) {
            detailInfo.append("<TR>");
            detailInfo.append("<TD NOWRAP style=\"padding:0;border-spacing:0;white-space:nowrap\">");
            if (details.getString(i3, "_status", "").equals("New")) {
                detailInfo.append("<font color=\"green\"  style=\"background-color:yellow\">");
            } else if (details.getString(i3, "_status", "").equals("Deleted")) {
                detailInfo.append("<font color=\"red\"><strike>");
            } else if (details.getString(i3, "_status").equals("Modified")) {
                detailInfo.append("<font color=\"orange\">");
            }
            detailInfo.append(details.getString(i3, "linkid", ""));
            if (details.getString(i3, "_status").equals("New")) {
                detailInfo.append("</font>");
            } else if (details.getString(i3, "_status", "").equals("Deleted")) {
                detailInfo.append("</strike></font>");
            } else if (details.getString(i3, "_status", "").equals("Modified")) {
                detailInfo.append("</font>");
            }
            detailInfo.append("</TD>");
            detailInfo.append("<TD style=\"padding:0;border-spacing:0;\" >");
            if (configreport) {
                detailInfo.append("<IMG SRC=\"..\\images\\black_verticalline.gif\" />");
            } else {
                detailInfo.append("<IMG SRC=\"WEB-CORE\\images\\black_verticalline.gif\" />");
            }
            detailInfo.append("</TD>");
            detailInfo.append("</TR>");
            detailInfo.append("<TR>");
            detailInfo.append("<TD  height=\"30\" style=\"padding:0;border-spacing:0;\">");
            detailInfo.append("");
            detailInfo.append("</TD>");
            detailInfo.append("<TD height=\"30\" style=\"padding:0;border-spacing:0;\" >");
            if (i3 == details.getRowCount() - 1) {
                if (details.getString(i3, "_status", "").equals("Modified")) {
                    if (configreport) {
                        detailInfo.append("<IMG  SRC=\"..\\images\\orangelastdlink.GIF\" />");
                    } else {
                        detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\orangelastdlink.GIF\" />");
                    }
                } else if (details.getString(i3, "_status", "").equals("New")) {
                    if (configreport) {
                        detailInfo.append("<IMG  SRC=\"..\\images\\greenlastdlink.GIF\" />");
                    } else {
                        detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\greenlastdlink.GIF\" />");
                    }
                } else if (details.getString(i3, "_status", "").equals("Deleted")) {
                    if (configreport) {
                        detailInfo.append("<IMG  SRC=\"..\\images\\redlastdlink.GIF\" />");
                    } else {
                        detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\redlastdlink.GIF\" />");
                    }
                } else if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\lastdlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\lastdlink.GIF\" />");
                }
            } else if (details.getString(i3, "_status", "").equals("Modified")) {
                if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\orangedlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\orangedlink.GIF\" />");
                }
            } else if (details.getString(i3, "_status", "").equals("New")) {
                if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\greendlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\greendlink.GIF\" />");
                }
            } else if (details.getString(i3, "_status", "").equals("Deleted")) {
                if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\reddlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\reddlink.GIF\" />");
                }
            } else if (configreport) {
                detailInfo.append("<IMG  SRC=\"..\\images\\dlink.gif\" />");
            } else {
                detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\dlink.gif\" />");
            }
            detailInfo.append("</TD> ");
            detailInfo.append("<TD height=\"30\" class=\"datamodelsdc\">");
            detailInfo.append(details.getString(i3, "linktableid", ""));
            detailInfo.append("</TD></TR>");
        }
        detailInfo.append("</TABLE>");
        secondcolumn.append("</TABLE>");
        StringBuffer beforethird = new StringBuffer();
        beforethird.append("<TABLE  class=\"datamodel\">");
        for (int i4 = 0; i4 < refersTo.getRowCount(); ++i4) {
            beforethird.append("<TR>");
            beforethird.append("<TD height=\"30\" width=\"300\" align=\"center\" style=\"padding:0;border-spacing: 0;\">");
            if (refersTo.getString(i4, "_status", "").equals("New")) {
                beforethird.append("<font color=\"green\"  style=\"background-color:yellow\">");
            } else if (refersTo.getString(i4, "_status", "").equals("Deleted")) {
                beforethird.append("<font color=\"red\"><strike>");
            } else if (refersTo.getString(i4, "_status", "").equals("Modified")) {
                beforethird.append("<font color=\"orange\">");
            }
            if ("F".equals(refersTo.getString(i4, "linktype"))) {
                String linkcolumns2 = refersTo.getString(i4, "sdccolumnid", "");
                String col22 = refersTo.getString(i4, "sdccolumnid2", "");
                if (col22 != null && col22.length() > 0) {
                    linkcolumns2 = linkcolumns2 + ", ";
                    linkcolumns2 = linkcolumns2 + col22;
                }
                beforethird.append(refersTo.getString(i4, "linkid", "") + "(" + linkcolumns2 + ")");
            } else {
                beforethird.append(refersTo.getString(i4, "linkid", ""));
            }
            if ("New".equals(refersTo.getString(i4, "_status", ""))) {
                beforethird.append("</font>");
            } else if ("Deleted".equals(refersTo.getString(i4, "_status", ""))) {
                beforethird.append("</strike></font>");
            } else if ("Modified".equals(refersTo.getString(i4, "_status", ""))) {
                beforethird.append("</font>");
            }
            beforethird.append("</TD>");
            beforethird.append("</TR>");
            beforethird.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if ("F".equals(refersTo.getString(i4, "linktype", ""))) {
                if ("New".equals(refersTo.getString(i4, "_status", ""))) {
                    if (configreport) {
                        beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\greenflink.GIF\"/>");
                    } else {
                        beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\greenflink.GIF\"/>");
                    }
                } else if ("Deleted".equals(refersTo.getString(i4, "_status", ""))) {
                    if (configreport) {
                        beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\redflink.GIF\"/>");
                    } else {
                        beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\redflink.GIF\"/>");
                    }
                } else if ("Modified".equals(refersTo.getString(i4, "_status", ""))) {
                    if (configreport) {
                        beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\orangeflink.GIF\"/>");
                    } else {
                        beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\orangeflink.GIF\"/>");
                    }
                } else if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\flink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\flink.GIF\"/>");
                }
            } else if ("New".equals(refersTo.getString(i4, "_status", ""))) {
                if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\greenmlink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\greenmlink.GIF\"/>");
                }
            } else if ("Deleted".equals(refersTo.getString(i4, "_status", ""))) {
                if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\redmlink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\redmlink.GIF\"/>");
                }
            } else if ("Modified".equals(refersTo.getString(i4, "_status", ""))) {
                if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\orangemlink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\orangemlink.GIF\"/>");
                }
            } else if (configreport) {
                beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            beforethird.append("</TD></TR>");
            beforethird.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\"/></TR>");
        }
        beforethird.append("</TABLE>");
        StringBuffer thirdcolumn = new StringBuffer();
        thirdcolumn.append("<TABLE  class=\"datamodel\">");
        for (int i5 = 0; i5 < refersTo.getRowCount(); ++i5) {
            thirdcolumn.append("<TR><TD height=\"30\"  style=\"padding:0;border-spacing: 0;\" /></TR>");
            String color2 = "black";
            if ("New".equals(refersTo.getString(i5, "_status", ""))) {
                color2 = "green";
            } else if ("Deleted".equals(refersTo.getString(i5, "_status", ""))) {
                color2 = "red";
            } else if ("Modified".equals(refersTo.getString(i5, "_status", ""))) {
                color2 = "orange";
            }
            thirdcolumn.append("<TR><TD height=\"30\"  width=\"80\" title=\"" + refersTo.getString(i5, "sdcdesc") + "\" class=\"datamodelsdc\" >");
            String fklink2 = ConfigReportContent.createHyperLink("SDC", refersTo.getString(i5, "linksdcid", ""), "", "", this.sdisIncluded, this.frames);
            thirdcolumn.append(fklink2);
            thirdcolumn.append("</TD></TR>");
            thirdcolumn.append("<TR><TD height=\"30\" /></TR>");
        }
        thirdcolumn.append("</TABLE>");
        configReportContent.append("<TABLE class=\"datamodel\">");
        configReportContent.append("<TR>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(firstcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(after1);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(secondcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(beforethird);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\" >");
        configReportContent.append(thirdcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("</TR>");
        configReportContent.append("<TR>");
        configReportContent.append("<TD colspan=2 />");
        configReportContent.append("<TD colspan=3 valign=\"Top\" style=\"padding:0;border-spacing: 0;\" >");
        configReportContent.append(detailInfo);
        configReportContent.append("</TD>");
        configReportContent.append("</TABLE>");
        return configReportContent;
    }

    private ConfigReportContent drawModel(DataSet details, DataSet refersTo, DataSet referredBy, DataSet referredByDetails, String sdcName, String sdcDesc, boolean configreport) throws SapphireException {
        String col2;
        String linkcolumndesc;
        String linkcolumns;
        int i;
        String fklink;
        int i2;
        ConfigReportContent configReportContent = new ConfigReportContent(this.config, "SDC draw model");
        StringBuffer firstcolumn = new StringBuffer();
        firstcolumn.append("<TABLE class=\"datamodel\" >");
        if (referredBy == null) {
            referredBy = new DataSet();
        }
        for (i2 = 0; i2 < referredBy.getRowCount(); ++i2) {
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
            firstcolumn.append("<TR><TD height=\"30\" width=\"80\" title=\"" + referredBy.getString(i2, "sdcdesc") + "\" class=\"datamodelsdc\" >");
            fklink = ConfigReportContent.createHyperLink("SDC", referredBy.getString(i2, "sdcid"), "", "", this.sdisIncluded, this.frames) + "(" + referredBy.getString(i2, "tableid") + ")";
            firstcolumn.append(fklink);
            firstcolumn.append("</TD></TR>");
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing:0;\"/></TR>");
        }
        if (referredByDetails == null) {
            referredByDetails = new DataSet();
        }
        for (i2 = 0; i2 < referredByDetails.getRowCount(); ++i2) {
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
            firstcolumn.append("<TR><TD height=\"30\"  title=\"" + referredByDetails.getString(i2, "sdcdesc") + "\"  class=\"datamodelsdc\" >");
            fklink = ConfigReportContent.createHyperLink("SDC", referredByDetails.getString(i2, "sdcid"), "", "", this.sdisIncluded, this.frames);
            firstcolumn.append(fklink + "(" + referredByDetails.getString(i2, "linktableid") + ")");
            firstcolumn.append("</TD></TR>");
            firstcolumn.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\"/></TR>");
        }
        firstcolumn.append("</TABLE>");
        StringBuffer after1 = new StringBuffer();
        after1.append("<TABLE class=\"datamodel\">");
        for (i = 0; i < referredBy.getRowCount(); ++i) {
            after1.append("<TR>");
            if (referredBy.getString(i, "linktype", "").equals("F")) {
                linkcolumns = referredBy.getString(i, "sdccolumnid", "");
                linkcolumndesc = this.getLinkColumnDesc("referredbysdcid", referredBy.getString(i, "sdcid"), linkcolumns);
                after1.append("<TD height=\"30\" width=\"300\" align=\"center\"  title=\"" + linkcolumndesc + "\" class=\"datamodellink\">");
                col2 = referredBy.getString(i, "sdccolumnid2", "");
                if (col2 != null && col2.length() > 0) {
                    linkcolumns = linkcolumns + ", ";
                    linkcolumns = linkcolumns + col2;
                }
                after1.append(referredBy.getString(i, "linkid", "") + "(" + linkcolumns + ")");
            } else {
                after1.append("<TD height=\"30\" width=\"300\" align=\"center\"  class=\"datamodellink\">");
                after1.append(referredBy.getString(i, "linkid", ""));
            }
            after1.append("</TD>");
            after1.append("</TR>");
            after1.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if (referredBy.getString(i, "linktype", "").equals("F")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\flink_from.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\flink_from.GIF\"/>");
                }
            } else if (configreport) {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                after1.append("<IMG width=\"300\" height=\"30\" SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            after1.append("</TD></TR>");
            after1.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
        }
        for (i = 0; i < referredByDetails.getRowCount(); ++i) {
            after1.append("<TR>");
            if (referredByDetails.getString(i, "linktype", "").equals("F")) {
                linkcolumns = referredByDetails.getString(i, "sdccolumnid", "");
                linkcolumndesc = "";
                if (referredBy.getString(i, "linksdcid") != null) {
                    linkcolumndesc = this.getLinkColumnDesc("referredbysdcidf1", referredBy.getString(i, "linksdcid"), linkcolumns);
                } else if (referredBy.getString(i, "sdcid") != null) {
                    linkcolumndesc = this.getLinkColumnDesc("referredbysdcidf2", referredBy.getString(i, "sdcid"), linkcolumns);
                }
                after1.append("<TD height=\"30\" width=\"300\"  title=\"" + linkcolumndesc + "\"  class=\"datamodellink\">");
                col2 = referredByDetails.getString(i, "sdccolumnid2", "");
                if (col2 != null && col2.length() > 0) {
                    linkcolumns = linkcolumns + ", ";
                    linkcolumns = linkcolumns + col2;
                }
                after1.append(referredByDetails.getString(i, "detaillinkid", "") + "(" + linkcolumns + ")");
            } else {
                after1.append("<TD height=\"30\" width=\"300\"  title=\"" + referredByDetails.getString(i, "sdcdesc") + "\"  class=\"datamodellink\">");
                after1.append(referredByDetails.getString(i, "detaillinkid", ""));
            }
            after1.append("</TD>");
            after1.append("</TR>");
            after1.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if (referredByDetails.getString(i, "linktype", "").equals("F")) {
                if (configreport) {
                    after1.append("<IMG width=\"300\"  SRC=\"..\\images\\flink.GIF\"/>");
                } else {
                    after1.append("<IMG width=\"300\"  SRC=\"WEB-CORE\\images\\flink.GIF\"/>");
                }
            } else if (configreport) {
                after1.append("<IMG width=\"300\"  SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                after1.append("<IMG width=\"300\"  SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            after1.append("</TD></TR>");
            after1.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\" /></TR>");
        }
        after1.append("</TABLE>");
        int referredByCount = referredBy.getRowCount() + referredByDetails.getRowCount();
        if (refersTo == null) {
            refersTo = new DataSet();
        }
        int secondcolumnlen = referredByCount > refersTo.getRowCount() ? referredByCount : refersTo.getRowCount();
        StringBuffer secondcolumn = new StringBuffer();
        int secondcolumnheight = 90 * secondcolumnlen;
        secondcolumn.append("<TABLE  width=\"250\" class=\"datamodel\">");
        secondcolumn.append("<TR><TD title=\"" + sdcName + "(" + sdcDesc + ")\" align=\"center\" height=\"" + secondcolumnheight + "\" class=\"datamodelsdc\" >");
        secondcolumn.append(sdcName);
        secondcolumn.append("</TD></TR>");
        StringBuffer detailInfo = new StringBuffer();
        detailInfo.append("<TABLE class=\"datamodel\" >");
        for (int i3 = 0; i3 < details.getRowCount(); ++i3) {
            detailInfo.append("<TR>");
            detailInfo.append("<TD NOWRAP style=\"padding:0;border-spacing:0;white-space:nowrap\">");
            detailInfo.append(details.getString(i3, "linkid", ""));
            detailInfo.append("</TD>");
            detailInfo.append("<TD style=\"padding:0;border-spacing:0;\" >");
            if (configreport) {
                detailInfo.append("<IMG SRC=\"..\\images\\black_verticalline.gif\" />");
            } else {
                detailInfo.append("<IMG SRC=\"WEB-CORE\\images\\black_verticalline.gif\" />");
            }
            detailInfo.append("</TD>");
            detailInfo.append("</TR>");
            detailInfo.append("<TR>");
            detailInfo.append("<TD  height=\"30\" style=\"padding:0;border-spacing:0;\">");
            detailInfo.append("");
            detailInfo.append("</TD>");
            detailInfo.append("<TD height=\"30\" style=\"padding:0;border-spacing:0;\" >");
            if (i3 == details.getRowCount() - 1) {
                if (configreport) {
                    detailInfo.append("<IMG  SRC=\"..\\images\\lastdlink.GIF\" />");
                } else {
                    detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\lastdlink.GIF\" />");
                }
            } else if (configreport) {
                detailInfo.append("<IMG  SRC=\"..\\images\\dlink.gif\" />");
            } else {
                detailInfo.append("<IMG  SRC=\"WEB-CORE\\images\\dlink.gif\" />");
            }
            detailInfo.append("</TD> ");
            detailInfo.append("<TD height=\"30\"  class=\"datamodelsdc\">");
            detailInfo.append(details.getString(i3, "linktableid", ""));
            detailInfo.append("</TD></TR>");
        }
        detailInfo.append("</TABLE>");
        secondcolumn.append("</TABLE>");
        StringBuffer beforethird = new StringBuffer();
        beforethird.append("<TABLE class=\"datamodel\">");
        for (int i4 = 0; i4 < refersTo.getRowCount(); ++i4) {
            beforethird.append("<TR>");
            if (refersTo.getString(i4, "linktype", "").equals("F")) {
                String linkcolumns2 = refersTo.getString(i4, "sdccolumnid", "");
                String linkcolumndesc2 = "";
                if (referredBy.getString(i4, "linksdcid") != null) {
                    linkcolumndesc2 = this.getLinkColumnDesc("referredbysdcidchanged1", referredBy.getString(i4, "linksdcid"), linkcolumns2);
                } else if (referredBy.getString(i4, "sdcid") != null) {
                    linkcolumndesc2 = this.getLinkColumnDesc("referredbysdcidchanged2", referredBy.getString(i4, "sdcid"), linkcolumns2);
                }
                beforethird.append("<TD height=\"30\" width=\"300\"  title=\"" + linkcolumndesc2 + "\" class=\"datamodellink\">");
                String col22 = refersTo.getString(i4, "sdccolumnid2", "");
                if (col22 != null && col22.length() > 0) {
                    linkcolumns2 = linkcolumns2 + ", ";
                    linkcolumns2 = linkcolumns2 + col22;
                }
                beforethird.append(refersTo.getString(i4, "linkid", "") + "(" + linkcolumns2 + ")");
            } else {
                beforethird.append("<TD height=\"30\" width=\"300\"  class=\"datamodellink\">");
                beforethird.append(refersTo.getString(i4, "linkid", ""));
            }
            beforethird.append("</TD>");
            beforethird.append("</TR>");
            beforethird.append("<TR><TD height=\"30\" width=\"300\" style=\"padding:0;border-spacing: 0;\">");
            if (refersTo.getString(i4, "linktype").equals("F")) {
                if (configreport) {
                    beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\flink.GIF\"/>");
                } else {
                    beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\flink.GIF\"/>");
                }
            } else if (configreport) {
                beforethird.append("<IMG width=\"300\" SRC=\"..\\images\\mlink.gif\"/>");
            } else {
                beforethird.append("<IMG width=\"300\" SRC=\"WEB-CORE\\images\\mlink.gif\"/>");
            }
            beforethird.append("</TD></TR>");
            beforethird.append("<TR><TD height=\"30\" style=\"padding:0;border-spacing: 0;\"/></TR>");
        }
        beforethird.append("</TABLE>");
        StringBuffer thirdcolumn = new StringBuffer();
        thirdcolumn.append("<TABLE  class=\"datamodel\">");
        for (int i5 = 0; i5 < refersTo.getRowCount(); ++i5) {
            thirdcolumn.append("<TR><TD height=\"30\"  style=\"padding:0;border-spacing: 0;\" /></TR>");
            thirdcolumn.append("<TR><TD height=\"30\" title=\"" + refersTo.getString(i5, "sdcdesc", "TBD") + "\" width=\"80\"  class=\"datamodelsdc\" >");
            String fklink2 = ConfigReportContent.createHyperLink("SDC", refersTo.getString(i5, "linksdcid", ""), "", "", this.sdisIncluded, this.frames) + "(" + refersTo.getString(i5, "tableid") + ")";
            thirdcolumn.append(fklink2);
            thirdcolumn.append("</TD></TR>");
            thirdcolumn.append("<TR><TD height=\"30\" /></TR>");
        }
        thirdcolumn.append("</TABLE>");
        configReportContent.append("<TABLE  class=\"datamodel\">");
        configReportContent.append("<TR>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(firstcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(after1);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(secondcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\">");
        configReportContent.append(beforethird);
        configReportContent.append("</TD>");
        configReportContent.append("<TD valign=\"Top\" style=\"padding:0;border-spacing: 0;\" >");
        configReportContent.append(thirdcolumn);
        configReportContent.append("</TD>");
        configReportContent.append("</TR>");
        configReportContent.append("<TR>");
        configReportContent.append("<TD colspan=2 />");
        configReportContent.append("<TD colspan=3 valign=\"Top\" style=\"padding:0;border-spacing: 0;\" >");
        configReportContent.append(detailInfo);
        configReportContent.append("</TD>");
        configReportContent.append("</TABLE>");
        return configReportContent;
    }

    private DataSet getLinksInfoFromRO(SDCRO ro) {
        DataSet linksinfo = new DataSet();
        linksinfo.setColidCaseSensitive(true);
        linksinfo.addColumn("Link Id", 0);
        linksinfo.addColumn("Link Type", 0);
        linksinfo.addColumn("Link Col 1", 0);
        linksinfo.addColumn("Link Col 2", 0);
        linksinfo.addColumn("Linked SDC", 0);
        linksinfo.addColumn("Linked RefType", 0);
        linksinfo.addColumn("Link Table", 0);
        linksinfo.addColumn("Delete Rule", 0);
        DataSet links = ro.getLinksInfo();
        if (links == null || links.size() == 0) {
            return new DataSet();
        }
        for (int i = 0; i < links.size(); ++i) {
            String linktype;
            String val = linktype = links.getString(i, "linktype");
            if ("F".equals(linktype)) {
                val = "Foreign Key";
            } else {
                if ("D".equals(linktype)) {
                    val = "Detail";
                    continue;
                }
                if ("V".equals(linktype)) {
                    val = "Reference ( Validated )";
                } else if ("M".equals(linktype)) {
                    val = "Many to Many";
                }
            }
            int currrow = linksinfo.addRow();
            linksinfo.setString(currrow, "Link Id", links.getString(i, "linkid"));
            linksinfo.setString(currrow, "Link Type", val);
            linksinfo.setString(currrow, "Link Col 1", links.getString(i, "sdccolumnid"));
            linksinfo.setString(currrow, "Link Col 2", links.getString(i, "sdccolumnid2"));
            String fklink = ConfigReportContent.createHyperLink("SDC", links.getString(i, "linksdcid"), "", "", this.sdisIncluded, this.frames);
            linksinfo.setString(currrow, "Linked SDC", fklink);
            fklink = ConfigReportContent.createHyperLink("RefType", links.getString(i, "reftypeid"), "", "", this.sdisIncluded, this.frames);
            linksinfo.setString(currrow, "Linked RefType", fklink);
            linksinfo.setString(currrow, "Link Table", links.getString(i, "tableid"));
            linksinfo.setString(currrow, "Delete Rule", links.getString(i, "deleteflag"));
        }
        return linksinfo;
    }

    private DataSet getIndexInfo(SDCRO ro, String tablename) {
        DataSet allindexes = ro.currentIndexInfo;
        HashMap<String, String> indexFilter = new HashMap<String, String>();
        indexFilter.put("tableid", tablename);
        DataSet indexes = allindexes.getFilteredDataSet(indexFilter);
        DataSet renderindexinfo = new DataSet();
        if (indexes != null && indexes.getRowCount() > 0) {
            renderindexinfo.setColidCaseSensitive(true);
            renderindexinfo.addColumn("Type", 0);
            renderindexinfo.addColumn("Name", 0);
            renderindexinfo.addColumn("Columns", 0);
            int i = 0;
            while (i < indexes.size()) {
                String tableid = indexes.getValue(i, "tableid");
                String refid = indexes.getValue(i, "refid");
                String reftypeflag = indexes.getValue(i, "reftypeflag");
                int currrow = renderindexinfo.addRow();
                renderindexinfo.setString(currrow, "Name", refid);
                renderindexinfo.setString(currrow, "Type", this.getRefType(reftypeflag));
                String cols = "";
                while (i < indexes.size() && refid.equals(indexes.getValue(i, "refid"))) {
                    if (cols.length() > 0) {
                        cols = cols + "<br>";
                    }
                    cols = cols + indexes.getValue(i, "columnid");
                    ++i;
                }
                renderindexinfo.setString(currrow, "Columns", cols);
            }
        }
        return renderindexinfo;
    }

    private DataSet getAttrsInfoFromRO(SDCRO ro) {
        DataSet attributes = ro.currentAttributes;
        if (attributes == null || attributes.size() == 0) {
            return new DataSet();
        }
        DataSet renderattrsinfo = new DataSet();
        renderattrsinfo.setColidCaseSensitive(true);
        renderattrsinfo.addColumn("Attribute Id", 0);
        renderattrsinfo.addColumn("Title", 0);
        renderattrsinfo.addColumn("Always Add", 0);
        renderattrsinfo.addColumn("Add Count", 0);
        renderattrsinfo.addColumn("Allow Duplicates", 0);
        renderattrsinfo.addColumn("Data Type", 0);
        renderattrsinfo.addColumn("Editor Style", 0);
        renderattrsinfo.addColumn("Editor SDC ID", 0);
        renderattrsinfo.addColumn("Editor RefType", 0);
        renderattrsinfo.addColumn("Default", 0);
        renderattrsinfo.addColumn("Help", 0);
        for (int i = 0; i < attributes.size(); ++i) {
            String attributeid = attributes.getValue(i, "attributeid");
            String attributeTitle = attributes.getValue(i, "attributetitle");
            String helpText = attributes.getValue(i, "helptext");
            String allowDubFlag = attributes.getValue(i, "allowduplicatesflag");
            String alwaysAddFlag = attributes.getValue(i, "alwaysaddflag");
            String alwaysAddCount = attributes.getValue(i, "alwaysaddcount");
            String dataType = attributes.getValue(i, "datatype");
            String defaultVal = attributes.getValue(i, "defaulttextvalue");
            String editorStyle = attributes.getValue(i, "editorstyleid");
            String editSdcId = attributes.getValue(i, "editsdcid");
            String editorRefType = attributes.getValue(i, "editreftypeid");
            String defaultValue = attributes.getValue(i, "default");
            int currrow = renderattrsinfo.addRow();
            renderattrsinfo.setString(currrow, "Attribute Id", attributeid);
            renderattrsinfo.setString(currrow, "Title", attributeTitle);
            renderattrsinfo.setString(currrow, "Always Add", this.YesNo(alwaysAddFlag));
            renderattrsinfo.setString(currrow, "Add Count", alwaysAddCount);
            renderattrsinfo.setString(currrow, "Allow Duplicates", this.YesNo(allowDubFlag));
            renderattrsinfo.setString(currrow, "Data Type", this.getDateType(dataType));
            renderattrsinfo.setString(currrow, "Editor Style", editorStyle);
            renderattrsinfo.setString(currrow, "Editor SDC ID", editSdcId);
            renderattrsinfo.setString(currrow, "Editor RefType", editorRefType);
            renderattrsinfo.setString(currrow, "Default", defaultValue);
            renderattrsinfo.setString(currrow, "Help", helpText);
        }
        return renderattrsinfo;
    }

    private DataSet getOperationInfoFromRO(SDCRO ro) {
        DataSet operations = ro.currentOperations;
        if (operations == null || operations.size() == 0) {
            return new DataSet();
        }
        DataSet renderopsinfo = new DataSet();
        renderopsinfo.setColidCaseSensitive(true);
        renderopsinfo.addColumn("Operation", 0);
        renderopsinfo.addColumn("Description", 0);
        for (int i = 0; i < operations.size(); ++i) {
            int currrow = renderopsinfo.addRow();
            renderopsinfo.setString(currrow, "Operation", operations.getValue(i, "operationid"));
            renderopsinfo.setString(currrow, "Description", operations.getValue(i, "operationdesc"));
        }
        return renderopsinfo;
    }

    private String getLinkColumnDesc(String context, String sdcid, String columnid) throws SapphireException {
        if (sdcid == null || sdcid.length() == 0) {
            return "";
        }
        try {
            return this.getSDCProcessor().getSDCColumnProperty(sdcid, columnid, "columndoc");
        }
        catch (Exception e) {
            throw new SapphireException("Failed to find linkcolumndesc for:" + sdcid + " columnid:" + columnid);
        }
    }
}

