/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.eln.SectionBehaviorResolver;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemIncludes;
import com.labvantage.sapphire.pageelements.gwt.server.ApprovalStepUtil;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.DataAccessService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.format.NumericFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseAction;
import sapphire.attachment.Attachment;
import sapphire.util.ActionBlock;
import sapphire.util.DBAccess;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class BaseELNAction
extends BaseAction
implements ELNConstants {
    public static String TEMPLATE_KEYGENRULE = "internaltemplate;Q^";

    public static void createAttachmentActivityRecord(ActionProcessor ap, Attachment attachment, String mode) throws ActionException {
        int attachmentnum = attachment.getAttachmentNum();
        PropertyList activityProps = new PropertyList();
        activityProps.setProperty("targetsdcid", attachment.getSDCId());
        activityProps.setProperty("targetkeyid1", attachment.getKeyId1());
        activityProps.setProperty("targetkeyid2", attachment.getKeyId2());
        if (mode.equals("I")) {
            activityProps.setProperty("activitytype", "Add");
            activityProps.setProperty("activitylog", "Added new attachment " + attachment.getSourceFilename() + " (num=" + attachmentnum + ")");
        } else if (mode.equals("U")) {
            activityProps.setProperty("activitytype", "Edit");
            activityProps.setProperty("activitylog", "Modified attachment " + attachment.getSourceFilename() + " (num=" + attachmentnum + ")");
        } else {
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog", "Removed attachment " + attachment.getSourceFilename() + " (num=" + attachmentnum + ")");
        }
        ap.processActionClass(AddWorksheetActivity.class.getName(), activityProps);
    }

    protected String getTemplateDetailKey(String worksheetid) {
        return worksheetid + NumericFormatter.formatNumber(this.getSequenceProcessor().getSequence("LV_Worksheet", worksheetid), "00000");
    }

    protected boolean workbookInProgress(DBAccess database, String worksheetid, String worksheetversionid) throws SapphireException {
        database.createPreparedResultSet("worksheet", "SELECT workbook.workbookid, workbook.workbookstatus FROM worksheet LEFT OUTER JOIN workbook ON workbook.workbookid = worksheet.workbookid AND workbook.workbookversionid = worksheet.workbookversionid WHERE worksheet.worksheetid = ? AND worksheet.worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
        if (database.getNext("worksheet")) {
            if (database.getValue("worksheet", "workbookid").length() > 0 && !database.getValue("worksheet", "workbookstatus").equals("InProgress")) {
                throw new SapphireException("Blocked editing worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + " as the workbook status is not " + "InProgress");
            }
            return true;
        }
        throw new SapphireException("Worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + " not found");
    }

    protected boolean worksheetInProgress(DBAccess database, String worksheetid, String worksheetversionid) throws SapphireException {
        return this.worksheetInProgress(database, worksheetid, worksheetversionid, null);
    }

    protected int getIncidentsNotClosedCount(DataSet incidents, String sourcesdcid, String sourcekeyid1, String sourcekeyid2) {
        HashMap<String, String> filter = new HashMap<String, String>();
        filter.put("sourcesdcid", sourcesdcid);
        filter.put("sourcekeyid1", sourcekeyid1);
        filter.put("sourcekeyid2", sourcekeyid2);
        return incidents.getFilteredDataSet(filter).size();
    }

    protected boolean worksheetInProgress(DBAccess database, String worksheetid, String worksheetversionid, PropertyList options) throws SapphireException {
        database.createPreparedResultSet("worksheet", "SELECT workbook.workbookid, workbook.workbookstatus, worksheet.worksheetstatus, worksheet.options FROM worksheet LEFT OUTER JOIN workbook ON workbook.workbookid = worksheet.workbookid AND workbook.workbookversionid = worksheet.workbookversionid WHERE worksheet.worksheetid = ? AND worksheet.worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
        if (database.getNext("worksheet")) {
            if (database.getValue("worksheet", "workbookid").length() > 0 && !database.getValue("worksheet", "workbookstatus").equals("InProgress")) {
                throw new SapphireException("Blocked editing worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + " as the workbook status is not " + "InProgress");
            }
            if (!database.getValue("worksheet", "worksheetstatus").equals("Pending") && !database.getValue("worksheet", "worksheetstatus").equals("InProgress")) {
                throw new SapphireException("Blocked editing worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + " as the status is not " + "InProgress");
            }
            if (options != null) {
                options.setPropertyList(database.getClob("worksheet", "options"));
            }
            return true;
        }
        throw new SapphireException("Worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + " not found");
    }

    protected boolean sectionInProgress(DBAccess database, String worksheetsectionid, String worksheetsectionversionid) throws SapphireException {
        return this.sectionInProgress(database, worksheetsectionid, worksheetsectionversionid, null, null, false);
    }

    protected boolean sectionInProgress(DBAccess database, String worksheetsectionid, String worksheetsectionversionid, PropertyList wsoptions, PropertyList wssoptions, boolean bypassStatusCheck) throws SapphireException {
        database.createPreparedResultSet("section", "SELECT workbook.workbookid, workbook.workbookstatus, worksheet.worksheetid, worksheet.worksheetversionid, worksheet.worksheetstatus, worksheet.options \"wsoptions\", worksheetsection.sectionstatus, worksheetsection.options \"wssoptions\"FROM worksheetsection, worksheet LEFT OUTER JOIN workbook ON workbook.workbookid = worksheet.workbookid AND workbook.workbookversionid = worksheet.workbookversionid WHERE worksheet.worksheetid = worksheetsection.worksheetid AND worksheet.worksheetversionid = worksheetsection.worksheetversionid AND worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{worksheetsectionid, worksheetsectionversionid});
        if (database.getNext("section")) {
            if (database.getValue("section", "workbookid").length() > 0 && !bypassStatusCheck && !database.getValue("section", "workbookstatus").equals("InProgress")) {
                throw new SapphireException("Blocked editing worksheet " + BaseELNAction.getIdVersionText(database.getValue("section", "worksheetid"), database.getValue("section", "worksheetversionid")) + " as the workbook status is not " + "InProgress");
            }
            if (!(bypassStatusCheck || database.getValue("section", "worksheetstatus").equals("Pending") || database.getValue("section", "worksheetstatus").equals("InProgress"))) {
                throw new SapphireException("Blocked editing worksheet " + BaseELNAction.getIdVersionText(database.getValue("section", "worksheetid"), database.getValue("section", "worksheetversionid")) + " as the status is not " + "InProgress");
            }
            if (!bypassStatusCheck && !database.getValue("section", "sectionstatus").equals("InProgress")) {
                throw new SapphireException("Blocked editing worksheet section " + BaseELNAction.getIdVersionText(worksheetsectionid, worksheetsectionversionid) + " as the status is not " + "InProgress");
            }
            if (wsoptions != null) {
                wsoptions.setPropertyList(database.getClob("section", "wsoptions"));
            }
            if (wssoptions != null) {
                wssoptions.setPropertyList(database.getClob("section", "wssoptions"));
            }
            return true;
        }
        throw new SapphireException("Worksheet section " + BaseELNAction.getIdVersionText(worksheetsectionid, worksheetsectionversionid) + " not found");
    }

    protected DataSet getWorksheetItem(String worksheetitemid, String worksheetitemversionid) throws SapphireException {
        DataSet item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
        if (item.size() == 1) {
            return item;
        }
        throw new SapphireException("Worksheet item " + BaseELNAction.getIdVersionText(worksheetitemid, worksheetitemversionid) + " not found");
    }

    protected SDIData loadWorksheet(String worksheetid, String worksheetversionid, PropertyList properties) throws SapphireException {
        Worksheet worksheet = new Worksheet(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        return worksheet.load(worksheetid, worksheetversionid, properties);
    }

    protected SDIData loadSection(String worksheetid, String worksheetversionid, String worksheetsectionid, String worksheetsectionversionid, boolean loadSubsections, boolean loadApprovals) throws SapphireException {
        SDIData itemData;
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        worksheetsectionversionid = BaseELNAction.resolveVersion(this.getQueryProcessor(), worksheetsectionid, worksheetsectionversionid, "worksheetsection");
        SDIRequest wssRequest = new SDIRequest();
        wssRequest.setSDCid("LV_WorksheetSection");
        wssRequest.setRequestItem("primary");
        if (loadApprovals) {
            wssRequest.setRequestItem("approval");
            wssRequest.setRequestItem("approvalstep");
        }
        wssRequest.setExtendedDataTypes(true);
        String sswhere = "";
        if (loadSubsections) {
            sswhere = BaseELNAction.getSubSectionWhere(worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid, this.database);
            wssRequest.setQueryFrom("worksheetsection");
            wssRequest.setQueryWhere(sswhere);
            wssRequest.setQueryOrderBy("usersequence");
        } else {
            wssRequest.setKeyid1List(worksheetsectionid);
            wssRequest.setKeyid2List(worksheetsectionversionid);
        }
        SDIData sectionData = this.getSDIProcessor().getSDIData(wssRequest);
        if (sectionData != null && sectionData.getDataset("primary") != null) {
            DataSet sections = sectionData.getDataset("primary");
            if (loadApprovals) {
                sections.addColumn("approvable", 0);
                sections.addColumn("hasapprovalstarted", 0);
                DataSet wssApproval = sectionData.getDataset("approval");
                DataSet wssApprovalSteps = sectionData.getDataset("approvalstep");
                wssApprovalSteps.addColumn("stepstatusflag", 0);
                HashMap<String, String> findMap = new HashMap<String, String>();
                for (int i = 0; i < sections.size(); ++i) {
                    PropertyList wssOptions = new PropertyList();
                    wssOptions.setPropertyList(sections.getClob(i, "options", ""));
                    sections.setValue(i, "options", wssOptions.toJSONString());
                    if (wssApproval == null || wssApproval.size() <= 0 || wssApprovalSteps == null || wssApprovalSteps.size() <= 0) continue;
                    findMap.put("keyid1", sections.getValue(i, "worksheetsectionid"));
                    findMap.put("keyid2", sections.getValue(i, "worksheetsectionversionid"));
                    int wssApprovalRow = wssApproval.findRow(findMap);
                    if (wssApprovalRow <= -1) continue;
                    DataSet wssApprovalStepsFiltered = wssApprovalSteps.getFilteredDataSet(findMap);
                    DataSet datasettocheck = this.getWorksheetContributors(worksheetid, worksheetversionid);
                    int dummyrow = datasettocheck.addRow();
                    datasettocheck.setValue(dummyrow, "activityby", sections.getValue(i, "createby"));
                    ApprovalStepUtil.checkApprovalSteps(wssApprovalStepsFiltered, sapphireConnection.getRoleList(), sapphireConnection.getSysuserId(), wssApproval.getValue(wssApprovalRow, "sequenceflag"), wssApproval.getValue(wssApprovalRow, "uniquenessflag"), datasettocheck, "activityby", false);
                    for (int j = 0; j < wssApprovalStepsFiltered.size(); ++j) {
                        if (!wssApprovalStepsFiltered.getValue(j, "stepstatusflag").equals("C") || !wssApprovalStepsFiltered.getValue(j, "approvalflag").equals("U")) continue;
                        sections.setValue(i, "approvable", sections.getValue(i, "sectionstatus").equals("PendingApproval") ? "Y" : "N");
                        break;
                    }
                    sections.setString(i, "hasapprovalstarted", ApprovalStepUtil.hasApprovalStarted(wssApprovalStepsFiltered) ? "Y" : "N");
                }
            }
            SDIRequest wsiRequest = new SDIRequest();
            wsiRequest.setSDCid("LV_WorksheetItem");
            wsiRequest.setRequestItem("primary");
            wsiRequest.setExtendedDataTypes(true);
            if (loadSubsections) {
                wsiRequest.setQueryFrom("worksheetitem");
                wsiRequest.setQueryWhere(sswhere);
                wsiRequest.setQueryOrderBy("usersequence");
            } else {
                wsiRequest.setQueryFrom("worksheetitem");
                wsiRequest.setQueryWhere("worksheetsectionid='" + SafeSQL.encodeForSQL(worksheetsectionid, this.database.isOracle()) + "' AND worksheetsectionversionid='" + SafeSQL.encodeForSQL(worksheetsectionversionid, this.database.isOracle()) + "'");
                wsiRequest.setQueryOrderBy("usersequence");
                wsiRequest.setShowTemplates(true);
            }
            itemData = this.getSDIProcessor().getSDIData(wsiRequest);
            if (itemData == null || itemData.getDataset("primary") == null) {
                throw new SapphireException("Failed to load worksheetitem");
            }
        } else {
            throw new SapphireException("Failed to load worksheetsection");
        }
        DataSet items = itemData.getDataset("primary");
        sectionData.setSDIData("items", itemData);
        return sectionData;
    }

    public static String getSubSectionWhere(String worksheetid, String worksheetversionid, String worksheetsectionid, String worksheetsectionversionid, DBAccess database) throws SapphireException {
        StringBuilder sswhere = new StringBuilder();
        database.createPreparedResultSet("SELECT worksheetsectionid, worksheetsectionversionid, usersequence, sectionlevel FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ? ORDER BY usersequence", new Object[]{worksheetid, worksheetversionid});
        boolean found = false;
        while (database.getNext()) {
            if (!database.getValue("worksheetsectionid").equals(worksheetsectionid) || !database.getValue("worksheetsectionversionid").equals(worksheetsectionversionid)) continue;
            found = true;
            sswhere.append(" OR ").append("(worksheetsectionid='").append(SafeSQL.encodeForSQL(worksheetsectionid, database.isOracle())).append("' AND worksheetsectionversionid='").append(SafeSQL.encodeForSQL(worksheetsectionversionid, database.isOracle())).append("')");
            int level = database.getInt("sectionlevel");
            while (database.getNext() && database.getInt("sectionlevel") > level) {
                sswhere.append(" OR ").append("(worksheetsectionid='").append(database.getValue("worksheetsectionid")).append("' AND worksheetsectionversionid='").append(database.getValue("worksheetsectionversionid")).append("')");
            }
            break block0;
        }
        if (!found) {
            throw new SapphireException("Section " + BaseELNAction.getIdVersionText(worksheetsectionid, worksheetsectionversionid) + " not found in worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid));
        }
        return sswhere.substring(4);
    }

    protected String[] copyWorksheet(String worksheetid, String worksheetversionid, String workbookid, String workbookversionid, PropertyList properties) throws SapphireException {
        SectionBehaviorResolver sbr;
        SapphireConnection sapphireConnection = new SapphireConnection(this.database.getConnection(), this.connectionInfo);
        SDIData worksheetData = this.loadWorksheet(worksheetid, worksheetversionid, properties);
        DataSet worksheet = worksheetData.getDataset("primary");
        SDIData sectionData = worksheetData.getSDIData("sections");
        DataSet worksheetsections = sectionData.getDataset("primary");
        SDIData itemData = worksheetData.getSDIData("items");
        DataSet worksheetitems = itemData.getDataset("primary");
        String newworksheetid = "";
        String newworksheetversionid = "";
        String worksheettemplateid = worksheet.getValue(0, "worksheetid");
        String worksheettemplateversionid = worksheet.getValue(0, "worksheetversionid");
        String worksheetsectionid = properties.getProperty("worksheetsectionid");
        String worksheetitemid = properties.getProperty("worksheetitemid");
        String templateflag = properties.getProperty("templateflag", "N");
        String newtemplateid = properties.getProperty("newtemplateid");
        String keygenrule = "";
        String templatetypeflag = properties.getProperty("templatetypeflag");
        String templateprivacyflag = properties.getProperty("templateprivacyflag", "G");
        boolean copyWorksheet = worksheetsectionid.length() == 0 && worksheetitemid.length() == 0;
        boolean copySection = worksheetsectionid.length() > 0;
        boolean copyItem = worksheetitemid.length() > 0;
        boolean newVersion = properties.getProperty("newversion", "N").equals("Y");
        boolean includeSubsections = properties.getProperty("includesubsections", "N").equals("Y");
        String copyWorksheetAttachments = properties.getProperty("copyworksheetattachments", "Y");
        String copySectionAttachments = properties.getProperty("copysectionattachments", "Y");
        String copyItemAttachments = properties.getProperty("copyitemattachments", "Y");
        String copyWorksheetAttributes = properties.getProperty("copyworksheetattributes", "Y");
        String copySectionAttributes = properties.getProperty("copysectionattributes", "Y");
        String copyItemAttributes = properties.getProperty("copyitemattributes", "Y");
        String clearWorksheetAttributes = properties.getProperty("clearworksheetattributes", "N");
        String clearSectionAttributes = properties.getProperty("clearsectionattributes", "N");
        String clearItemAttributes = properties.getProperty("clearitemattributes", "N");
        String setWorksheetAttributesDefaults = properties.getProperty("setworksheetattributedefaults", "N");
        String setSectionAttributesDefaults = properties.getProperty("setsectionattributedefaults", "N");
        String setItemAttributesDefaults = properties.getProperty("setitemattributedefaults", "N");
        String copyWorksheetNotes = properties.getProperty("copyworksheetnotes", "Y");
        String copySectionNotes = properties.getProperty("copysectionnotes", "Y");
        String copyItemNotes = properties.getProperty("copyitemnotes", "Y");
        String excludeWorksheetContributors = properties.getProperty("copyworksheetcontributors", "Y").equals("N") ? "Y" : "N";
        String excludeWorksheetLog = properties.getProperty("copyworksheetlog", "N").equals("N") ? "Y" : "N";
        String excludeWorksheetSDIs = properties.getProperty("copyworksheetsdis", "Y").equals("N") ? "Y" : "N";
        String excludeItemSDIs = excludeWorksheetSDIs.equals("Y") || properties.getProperty("copyitemsdis", "Y").equals("N") ? "Y" : "N";
        String excludeWorksheetApprovals = "Y";
        String excludeSectionApprovals = "Y";
        ActionBlock ab = new ActionBlock();
        if (worksheet != null && (worksheet.size() == 1 || copySection || copyItem)) {
            PropertyList wsProps = new PropertyList();
            wsProps.setProperty("sdcid", "LV_Worksheet");
            wsProps.setProperty("worksheetversionid", properties.getProperty("newtemplatemode").equals("Y") ? "1" : properties.getProperty("worksheetversionid", "1"));
            wsProps.setProperty("versionstatus", properties.getProperty("versionstatus", "P"));
            wsProps.setProperty("worksheetdesc", properties.getProperty("worksheetname"));
            wsProps.setProperty("worksheetname", properties.getProperty("worksheetname"));
            wsProps.setProperty("authorid", properties.getProperty("authorid", sapphireConnection.getSysuserId()));
            wsProps.setProperty("authordt", properties.getProperty("authordt", "now"));
            wsProps.setProperty("workbookid", workbookid);
            wsProps.setProperty("workbookversionid", workbookversionid);
            wsProps.setProperty("worksheetstatus", properties.getProperty("worksheetstatus", "InProgress"));
            wsProps.setProperty("templateprivacyflag", templateprivacyflag);
            wsProps.setProperty("templateflag", templateflag);
            wsProps.setProperty("autokeytemplate", templateflag);
            wsProps.setProperty("templatetypeflag", templateflag.equals("N") ? "(null)" : (templatetypeflag.length() > 0 ? templatetypeflag : (copyWorksheet ? "W" : (copySection ? "S" : "I"))));
            wsProps.setProperty("coreflag", "N");
            PropertyList wsOptions = null;
            try {
                wsOptions = new PropertyList(new JSONObject(worksheet.getValue(0, "options", "{}")));
                wsOptions.remove("keygenrule");
            }
            catch (JSONException e) {
                throw new SapphireException("Failed to parse worksheet options");
            }
            if (newtemplateid.length() > 0) {
                wsProps.setProperty("keyid1", newtemplateid + (newtemplateid.endsWith("_LVW") || newtemplateid.endsWith("_LVS") || newtemplateid.endsWith("_LVI") || newtemplateid.endsWith("_W") || newtemplateid.endsWith("_S") || newtemplateid.endsWith("_I") ? "" : "_" + (Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "LV" : "") + templatetypeflag));
                wsProps.setProperty("coreflag", Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "Y" : "N");
                wsProps.setProperty("overrideautokey", "Y");
                keygenrule = TEMPLATE_KEYGENRULE;
                wsOptions.setProperty("keygenrule", keygenrule);
            }
            wsProps.setProperty("options", wsOptions.toXMLString());
            if (copyWorksheet) {
                wsProps.setProperty("templatekeyid1", worksheettemplateid);
                wsProps.setProperty("templatekeyid2", worksheettemplateversionid);
                if (newVersion || templateflag.equals("Y")) {
                    wsProps.setProperty("forcecopyattributes", copyWorksheetAttributes);
                    wsProps.setProperty("templateid", "(null)");
                    wsProps.setProperty("templateversionid", "(null)");
                } else {
                    wsProps.setProperty("templateid", worksheettemplateid);
                    wsProps.setProperty("templateversionid", worksheettemplateversionid);
                    wsProps.setProperty("forcecopydownattributes", copyWorksheetAttributes);
                    wsProps.setProperty("setattributedefaults", setWorksheetAttributesDefaults);
                    wsProps.setProperty("clearattributevalues", clearWorksheetAttributes);
                }
                wsProps.setProperty("copyattachment", copyWorksheetAttachments);
            }
            wsProps.setProperty("worksheet_action", "Y");
            wsProps.setProperty("copysdinotes", copyWorksheetNotes);
            wsProps.setProperty("excludeattribute", copyWorksheetAttributes.equals("N") ? "Y" : "N");
            wsProps.setProperty("excludeworksheetsdi", excludeWorksheetSDIs);
            wsProps.setProperty("excludeworksheetcontributor", excludeWorksheetContributors);
            wsProps.setProperty("excludeworksheetactivitylog", excludeWorksheetLog);
            wsProps.setProperty("excludeapproval", excludeWorksheetApprovals);
            wsProps.setProperty("excludeapprovalstep", excludeWorksheetApprovals);
            ab.setAction("AddWorksheet", "AddSDI", "1", wsProps);
            if (copyWorksheet && (newtemplateid.length() == 0 || newVersion) && wsOptions.getProperty("worksheetapprovaltype").length() > 0) {
                PropertyList approvalProps = new PropertyList();
                approvalProps.put("sdcid", "LV_Worksheet");
                approvalProps.put("keyid1", "[$G{AddWorksheet.newkeyid1}]");
                approvalProps.put("keyid2", "[$G{AddWorksheet.newkeyid2}]");
                approvalProps.put("approvaltypeid", wsOptions.getProperty("worksheetapprovaltype"));
                ab.setAction("WorksheetSDIApproval", "AddSDIApproval", "1", approvalProps);
            }
            sbr = new SectionBehaviorResolver(this.getQueryProcessor());
            for (int i = 0; i < worksheetsections.size(); ++i) {
                PropertyList wssProps = new PropertyList();
                wssProps.setProperty("sdcid", "LV_WorksheetSection");
                wssProps.setProperty("keygenerationrule", keygenrule);
                wssProps.setProperty("worksheetsectionversionid", properties.getProperty("worksheetversionid", "1"));
                if (wsOptions.getProperty("keygenrule").equals(TEMPLATE_KEYGENRULE)) {
                    wssProps.setProperty("worksheetsectionid", this.getTemplateDetailKey(wsProps.getProperty("keyid1")));
                    wssProps.setProperty("overrideautokey", "Y");
                }
                wssProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
                wssProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
                wssProps.setProperty("sectionstatus", properties.getProperty("sectionstatus", "InProgress"));
                wssProps.setProperty("sectionlevel", worksheetsections.getValue(i, "sectionlevel"));
                wssProps.setProperty("availabilityflag", templateflag.equals("Y") ? "Y" : (i <= 1 ? "Y" : "Y"));
                wssProps.setProperty("templateflag", copyWorksheet ? "N" : "Y");
                wssProps.setProperty("autokeytemplate", copyWorksheet ? "N" : "Y");
                if (copyWorksheet || copySection) {
                    String fromSectionid = worksheetsections.getValue(i, "worksheetsectionid");
                    String fromSectionVersionid = worksheetsections.getValue(i, "worksheetsectionversionid");
                    wssProps.setProperty("templatekeyid1", fromSectionid);
                    wssProps.setProperty("templatekeyid2", fromSectionVersionid);
                    int sectionSequence = worksheetsections.getInt(i, "usersequence");
                    sbr.addSectionToSequence(fromSectionid, fromSectionVersionid, sectionSequence);
                    if (newVersion) {
                        wssProps.setProperty("forcecopyattributes", copySectionAttributes);
                        wssProps.setProperty("templateid", "(null)");
                        wssProps.setProperty("templateversionid", "(null)");
                    } else {
                        wssProps.setProperty("forcecopydownattributes", copySectionAttributes);
                        wssProps.setProperty("setattributedefaults", setSectionAttributesDefaults);
                        wssProps.setProperty("clearattributevalues", clearSectionAttributes);
                        wssProps.setProperty("templateid", worksheettemplateid);
                        wssProps.setProperty("templateversionid", worksheettemplateversionid);
                    }
                    wssProps.setProperty("copyattachment", copySectionAttachments);
                }
                wssProps.setProperty("copysdinotes", copySectionNotes);
                wssProps.setProperty("excludeattribute", copySectionAttributes.equals("N") ? "Y" : "N");
                wssProps.setProperty("excludeapproval", excludeSectionApprovals);
                wssProps.setProperty("excludeapprovalstep", excludeSectionApprovals);
                wssProps.setProperty("worksheet_action", "Y");
                ab.setAction("AddWorksheetSection_" + i, "AddSDI", "1", wssProps);
                if (!copyWorksheet) continue;
                PropertyList wssOptions = null;
                try {
                    wssOptions = new PropertyList(new JSONObject(worksheetsections.getValue(i, "options", "{}")));
                    worksheetsections.setString(i, "__sectionavailabilityflag", wssProps.getProperty("availabilityflag"));
                    worksheetsections.setString(i, "__itemavailability", wssOptions.getProperty("itemavailability", "A"));
                }
                catch (JSONException e) {
                    throw new SapphireException("Failed to parse worksheet section options");
                }
                if (wssOptions.getProperty("sectionapprovaltype").length() <= 0 || newtemplateid.length() != 0 && !newVersion) continue;
                PropertyList approvalProps = new PropertyList();
                approvalProps.put("sdcid", "LV_WorksheetSection");
                approvalProps.put("keyid1", "[$G{AddWorksheetSection_" + i + ".newkeyid1}]");
                approvalProps.put("keyid2", "[$G{AddWorksheetSection_" + i + ".newkeyid2}]");
                approvalProps.put("approvaltypeid", wssOptions.getProperty("sectionapprovaltype"));
                ab.setAction("WorksheetSectionSDIApproval_" + i, "AddSDIApproval", "1", approvalProps);
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            for (int i = 0; i < worksheetsections.size(); ++i) {
                int sectionSequence = worksheetsections.getInt(i, "usersequence");
                filter.put("worksheetsectionid", worksheetsections.getValue(i, "worksheetsectionid"));
                filter.put("worksheetsectionversionid", worksheetsections.getValue(i, "worksheetsectionversionid"));
                DataSet sectionitems = !copyItem ? worksheetitems.getFilteredDataSet(filter) : worksheetitems;
                String sectionAvailabilityFlag = worksheetsections.getValue(i, "__sectionavailabilityflag", "Y");
                String itemAvailability = worksheetsections.getValue(i, "__itemavailability", "A");
                for (int j = 0; j < sectionitems.size(); ++j) {
                    PropertyList wsiProps = new PropertyList();
                    wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                    wsiProps.setProperty("keygenerationrule", keygenrule);
                    wsiProps.setProperty("worksheetitemversionid", properties.getProperty("worksheetversionid", "1"));
                    if (wsOptions.getProperty("keygenrule").equals(TEMPLATE_KEYGENRULE)) {
                        wsiProps.setProperty("worksheetitemid", this.getTemplateDetailKey(wsProps.getProperty("keyid1")));
                        wsiProps.setProperty("overrideautokey", "Y");
                    }
                    wsiProps.setProperty("worksheetid", "[$G{AddWorksheet.newkeyid1}]");
                    wsiProps.setProperty("worksheetversionid", "[$G{AddWorksheet.newkeyid2}]");
                    wsiProps.setProperty("worksheetsectionid", "[$G{AddWorksheetSection_" + i + ".newkeyid1}]");
                    wsiProps.setProperty("worksheetsectionversionid", "[$G{AddWorksheetSection_" + i + ".newkeyid2}]");
                    wsiProps.setProperty("itemstatus", properties.getProperty("itemstatus", "InProgress"));
                    wsiProps.setProperty("html", "(null)");
                    wsiProps.setProperty("exporthtml", "(null)");
                    wsiProps.setProperty("availabilityflag", templateflag.equals("Y") ? "Y" : (j == 0 ? sectionAvailabilityFlag : (itemAvailability.equals("S") ? "N" : (itemAvailability.equals("R") ? "Y" : sectionAvailabilityFlag))));
                    wsiProps.setProperty("templateflag", copyWorksheet ? "N" : "Y");
                    wsiProps.setProperty("autokeytemplate", copyWorksheet ? "N" : "Y");
                    String fromitemid = sectionitems.getValue(j, "worksheetitemid");
                    String fromitemversionid = sectionitems.getValue(j, "worksheetitemversionid");
                    wsiProps.setProperty("templatekeyid1", fromitemid);
                    wsiProps.setProperty("templatekeyid2", fromitemversionid);
                    int itemSequence = sectionitems.getInt(j, "usersequence");
                    sbr.addItemToSequence(fromitemid, fromitemversionid, sectionSequence, itemSequence);
                    if (newVersion) {
                        wsiProps.setProperty("forcecopyattributes", copyItemAttributes);
                        wsiProps.setProperty("templateid", "(null)");
                        wsiProps.setProperty("templateversionid", "(null)");
                    } else {
                        wsiProps.setProperty("forcecopydownattributes", copyItemAttributes);
                        wsiProps.setProperty("setattributedefaults", setItemAttributesDefaults);
                        wsiProps.setProperty("clearattributevalues", clearItemAttributes);
                        wsiProps.setProperty("templateid", worksheettemplateid);
                        wsiProps.setProperty("templateversionid", worksheettemplateversionid);
                    }
                    wsiProps.setProperty("copysdinotes", copyItemNotes);
                    wsiProps.setProperty("excludeattribute", copyItemAttributes.equals("N") ? "Y" : "N");
                    wsiProps.setProperty("excludeworksheetitemsdi", excludeItemSDIs);
                    wsiProps.setProperty("copyattachment", copyItemAttachments);
                    wsiProps.setProperty("worksheet_action", "Y");
                    ab.setBlockProperty("WSI_templateid_" + i + "_" + j, sectionitems.getValue(j, "worksheetitemid"));
                    ab.setAction("AddWorksheetItem_" + i + "_" + j, "AddSDI", "1", wsiProps);
                }
            }
        } else {
            throw new SapphireException("Failed to load worksheet in copy");
        }
        this.getActionProcessor().processActionBlock(ab);
        newworksheetid = ab.getActionProperty("AddWorksheet", "newkeyid1");
        newworksheetversionid = ab.getActionProperty("AddWorksheet", "newkeyid2");
        sbr.resolveBehaviorReferences(newworksheetid, newworksheetversionid);
        return new String[]{newworksheetid, newworksheetversionid};
    }

    public DataSet getWorksheetContributors(String worksheetid, String worksheetversionid) {
        DataSet contributors = this.getQueryProcessor().getPreparedSqlDataSet("SELECT distinct activityby FROM worksheetactivitylog WHERE worksheetid=? AND worksheetversionid=? AND activitytype='SetContent'", (Object[])new String[]{worksheetid, worksheetversionid});
        return contributors;
    }

    public static DataSet getWorksheetNotes(QueryProcessor queryProcessor, String worksheetid, String worksheetversionid, String where) {
        DataSet notes = queryProcessor.getPreparedSqlDataSet("SELECT 'W' \"type\", worksheetid \"id\", worksheetversionid \"version\", sdinote.note, sdinote.ownerid, sdinote.followupflag, sdinote.followupuserid, sdinote.followupnotifyuserflag, sdinote.resolvedflag, sdinote.resolvedby, sdinote.resolveddt, sdinote.resolvednote FROM sdinote, worksheet WHERE sdinote.sdcid = 'LV_Worksheet' AND sdinote.keyid1 = worksheet.worksheetid AND sdinote.keyid2 = worksheet.worksheetversionid   AND worksheet.worksheetid = ? AND worksheet.worksheetversionid = ? " + (where.length() > 0 ? "AND " + where : "") + " UNION ALL SELECT 'S' \"type\", worksheetsectionid \"id\", worksheetsectionversionid \"version\", sdinote.note, sdinote.ownerid, sdinote.followupflag, sdinote.followupuserid, sdinote.followupnotifyuserflag, sdinote.resolvedflag, sdinote.resolvedby, sdinote.resolveddt, sdinote.resolvednote FROM sdinote, worksheetsection WHERE sdinote.sdcid = '" + "LV_WorksheetSection" + "' AND sdinote.keyid1 = worksheetsection.worksheetsectionid AND sdinote.keyid2 = worksheetsection.worksheetsectionversionid   AND worksheetsection.worksheetid = ? AND worksheetsection.worksheetversionid = ? " + (where.length() > 0 ? "AND " + where : "") + " UNION ALL SELECT 'I' \"type\", worksheetitemid \"id\", worksheetitemversionid \"version\", sdinote.note, sdinote.ownerid, sdinote.followupflag, sdinote.followupuserid, sdinote.followupnotifyuserflag, sdinote.resolvedflag, sdinote.resolvedby, sdinote.resolveddt, sdinote.resolvednote FROM sdinote, worksheetitem WHERE sdinote.sdcid = '" + "LV_WorksheetItem" + "' AND sdinote.keyid1 = worksheetitem.worksheetitemid AND sdinote.keyid2 = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? " + (where.length() > 0 ? "AND " + where : ""), new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid, worksheetid, worksheetversionid}, true);
        return notes;
    }

    public static DataSet getWorksheetSectionNotes(QueryProcessor queryProcessor, DBAccess database, String worksheetid, String worksheetversinoid, String worksheetsectionid, String worksheetsectionversionid, String where, boolean subsections) {
        String keywhere = "";
        if (subsections) {
            try {
                keywhere = BaseELNAction.getSubSectionWhere(worksheetid, worksheetversinoid, worksheetsectionid, worksheetsectionversionid, database);
            }
            catch (SapphireException sapphireException) {}
        } else {
            keywhere = " worksheetsectionid = '" + worksheetsectionid + "' AND worksheetsectionversionid = '" + worksheetsectionversionid + "' ";
        }
        DataSet notes = queryProcessor.getSqlDataSet("SELECT 'S' \"type\", worksheetsectionid \"id\", worksheetsectionversionid \"version\", sdinote.note, sdinote.ownerid, sdinote.followupflag, sdinote.followupuserid, sdinote.followupnotifyuserflag, sdinote.resolvedflag, sdinote.resolvedby, sdinote.resolveddt, sdinote.resolvednote FROM sdinote, worksheetsection WHERE sdinote.sdcid = 'LV_WorksheetSection' AND sdinote.keyid1 = worksheetsection.worksheetsectionid AND sdinote.keyid2 = worksheetsection.worksheetsectionversionid " + (where.length() > 0 ? " AND (" + where + ")" : "") + "  AND (" + keywhere + ") UNION ALL SELECT 'I' \"type\", worksheetitemid \"id\", worksheetitemversionid \"version\", sdinote.note, sdinote.ownerid, sdinote.followupflag, sdinote.followupuserid, sdinote.followupnotifyuserflag, sdinote.resolvedflag, sdinote.resolvedby, sdinote.resolveddt, sdinote.resolvednote FROM sdinote, worksheetitem WHERE sdinote.sdcid = '" + "LV_WorksheetItem" + "' AND sdinote.keyid1 = worksheetitem.worksheetitemid AND sdinote.keyid2 = worksheetitem.worksheetitemversionid " + (where.length() > 0 ? " AND (" + where + ")" : "") + " AND (" + keywhere + ")", true);
        return notes;
    }

    protected int addActivityLog(String worksheetid, String worksheetversionid, String activitytype, String targetsdcid, String targetkeyid1, String targetkeyid2, String activitylog) throws SapphireException {
        return this.addActivityLog(worksheetid, worksheetversionid, "", activitytype, targetsdcid, targetkeyid1, targetkeyid2, "", activitylog);
    }

    protected int addActivityLog(String worksheetid, String worksheetversionid, String reporteventid, String activitytype, String targetsdcid, String targetkeyid1, String targetkeyid2, String targetkeyid3, String activitylog) throws SapphireException {
        int activitylogid = this.getSequenceProcessor().getSequence("LV_Worksheet", "activitylog");
        String sysuserid = !this.connectionInfo.getSysuserId().equals("(system)") ? this.connectionInfo.getSysuserId() : "";
        this.database.executePreparedUpdate("INSERT INTO worksheetactivitylog ( worksheetid, worksheetversionid, activitylogid, activityby, activitydt, activitytype, targetsdcid, targetkeyid1, targetkeyid2, targetkeyid3, targetauditseq, activitylog, reporteventid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT auditsequence FROM " + (targetsdcid.equals("LV_Worksheet") ? "worksheet WHERE worksheetid = ? AND worksheetversionid = ?" : (targetsdcid.equals("LV_WorksheetSection") ? "worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?" : "worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?")) + "), ?, ? )", new Object[]{worksheetid, worksheetversionid, activitylogid, sysuserid, DateTimeUtil.getNowTimestamp(), activitytype, targetsdcid, targetkeyid1, targetkeyid2, targetkeyid3, targetkeyid1, targetkeyid2, activitylog.length() > 4000 ? activitylog.substring(0, 4000) : activitylog, reporteventid});
        if (!(activitytype.equals("Open") && targetsdcid.equals("LV_Worksheet") || sysuserid.length() <= 0)) {
            this.database.executePreparedUpdate("INSERT INTO worksheetcontributor ( worksheetid, worksheetversionid, contributorid, nominatedflag ) SELECT ?, ?, ?, 'N' FROM sdc WHERE sdcid = 'SDC' AND NOT EXISTS ( SELECT worksheetid, worksheetversionid, contributorid FROM worksheetcontributor wsc1 WHERE wsc1.worksheetid = ? AND wsc1.worksheetversionid = ? AND wsc1.contributorid = ? )", new Object[]{worksheetid, worksheetversionid, sysuserid, worksheetid, worksheetversionid, sysuserid});
        }
        return activitylogid;
    }

    protected void clearRSet(String rsetid) {
        if (rsetid.length() > 0) {
            this.getDAMProcessor().clearRSet(rsetid);
        }
    }

    protected String[] lockRSet(String worksheetid, String worksheetversionid, String rsetid) throws SapphireException {
        try {
            DataAccessService das = new DataAccessService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
            RSet rset = new RSet(rsetid);
            rset.setRSet(das.lockRSet(rset, "LA", 1, true, true));
            this.database.createPreparedResultSet("SELECT sysuserid, checkedoutbyuserid, checkedoutbydepartmentid FROM rsetitems WHERE rsetid = ? AND sdcid = ? AND keyid1 = ? AND keyid2 = ?", new Object[]{rset.getRsetid(), "LV_Worksheet", worksheetid, worksheetversionid});
            if (this.database.getNext()) {
                return new String[]{rset.getRsetid(), rset.getPrimaryStatus() == 1 ? "" : this.database.getValue("sysuserid"), this.database.getValue("checkedoutbyuserid"), this.database.getValue("checkedoutbydepartmentid")};
            }
            return new String[]{rset.getRsetid(), "Unknown"};
        }
        catch (Exception e) {
            throw new SapphireException("Failed to lock worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid), e);
        }
    }

    protected String propertyValues(PropertyList properties, PropertyList intialOptions, String[] ignore) {
        StringBuffer out = new StringBuffer();
        HashSet<String> ignoreSet = new HashSet<String>(Arrays.asList("worksheetid", "worksheetversionid", "sdcid", "keyid1", "keyid2", "worksheet_action", "bypassavailabilitycheck", "bypassstatuscheck"));
        ignoreSet.addAll(Arrays.asList(ignore));
        for (String propertyid : properties.keySet()) {
            if (ignoreSet.contains(propertyid)) continue;
            String value = properties.getProperty(propertyid);
            if (value.contains("<propertylist")) {
                try {
                    PropertyList props = new PropertyList();
                    props.setPropertyList(value);
                    if (propertyid.equals("options")) {
                        this.processOptions("", intialOptions, out, props);
                        continue;
                    }
                    out.append(", ").append(propertyid).append("=");
                    Set keyset = props.keySet();
                    for (String pid : keyset) {
                        Object v = props.get(pid);
                        if (v instanceof String) {
                            out.append(" " + pid + "=" + v);
                            continue;
                        }
                        out.append(" " + pid + "=...");
                    }
                    continue;
                }
                catch (Exception e) {
                    out.append(", ").append(propertyid).append("=").append(value);
                    continue;
                }
            }
            if (value.equals(intialOptions.getProperty(propertyid))) continue;
            out.append(", ").append(propertyid).append("=").append(value);
        }
        String temp = out.toString().trim();
        if (temp.startsWith(",")) {
            temp = temp.substring(1);
        }
        return temp;
    }

    private void processOptions(String prefix, PropertyList intialOptions, StringBuffer out, PropertyList props) {
        Set keyset = props.keySet();
        for (String pid : keyset) {
            Object v = props.get(pid);
            Object voriginal = intialOptions.get(pid);
            if (v == null && voriginal == null) continue;
            if (v instanceof String) {
                if (intialOptions.getProperty(pid).equals(v)) continue;
                out.append(" " + prefix + pid + "=" + v);
                continue;
            }
            if (v instanceof PropertyList) {
                this.processOptions(pid + ".", intialOptions.getPropertyList(pid), out, (PropertyList)v);
                continue;
            }
            if (v instanceof PropertyListCollection && !(voriginal instanceof PropertyListCollection)) continue;
        }
    }

    public static String getIdVersionText(String id, String version) {
        return "'" + id + " v(" + version + ")'";
    }

    public static String resolveVersion(QueryProcessor queryProcessor, String id, String version, String tableid) throws SapphireException {
        if (version.length() == 0 || version.equalsIgnoreCase("C")) {
            String sql = "SELECT " + tableid + "versionid FROM " + tableid + " WHERE " + tableid + "id = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( " + tableid + "versionid as integer ) DESC";
            DataSet versions = queryProcessor.getPreparedSqlDataSet(sql, new Object[]{id});
            if (versions.size() > 0) {
                return versions.getValue(0, tableid + "versionid");
            }
            throw new SapphireException("Failed to find " + tableid + " '" + id + "' with version '" + version + "'");
        }
        return version;
    }

    public static String resolveWorksheetName(SapphireConnection sapphireConnection, SequenceProcessor sequenceProcessor, String worksheetname, PropertyList substitutions, DataSet attributes) {
        String[] nameparts = StringUtil.getTokens(worksheetname);
        M18NUtil m18n = new M18NUtil(sapphireConnection);
        for (int i = 0; i < nameparts.length; ++i) {
            int row;
            String[] parts;
            String namepart = nameparts[i];
            if (substitutions != null && substitutions.containsKey(namepart)) {
                worksheetname = StringUtil.replaceAll(worksheetname, "[" + namepart + "]", substitutions.getProperty(namepart), false);
                continue;
            }
            if (namepart.equalsIgnoreCase("currentuser")) {
                worksheetname = StringUtil.replaceAll(worksheetname, "[" + namepart + "]", sapphireConnection.getSysuserId(), false);
                continue;
            }
            if (namepart.startsWith("currentuser.")) {
                worksheetname = StringUtil.replaceAll(worksheetname, "[currentuser.name]", sapphireConnection.getSysuserName(), false);
                continue;
            }
            if (namepart.startsWith("currentdatetime") || namepart.startsWith("currentdate")) {
                parts = StringUtil.split(namepart, ";");
                if (parts.length > 1) {
                    SimpleDateFormat df = new SimpleDateFormat(parts[1]);
                    worksheetname = StringUtil.replaceAll(worksheetname, "[" + namepart + "]", df.format(m18n.getNowCalendar()), false);
                    continue;
                }
                if (namepart.startsWith("currentdatetime")) {
                    worksheetname = StringUtil.replaceAll(worksheetname, "[" + namepart + "]", m18n.format(m18n.getNowCalendar()), false);
                    continue;
                }
                if (!namepart.equalsIgnoreCase("currentdate")) continue;
                worksheetname = StringUtil.replaceAll(worksheetname, "[" + namepart + "]", m18n.formatDateOnly(m18n.getNowCalendar()), false);
                continue;
            }
            if (namepart.startsWith("template_seq")) {
                parts = StringUtil.split(namepart, ";");
                int seq = sequenceProcessor.getSequence("LV_Worksheet", substitutions.getProperty("templateid"));
                worksheetname = StringUtil.replaceAll(worksheetname, "[" + namepart + "]", parts.length > 1 ? NumericFormatter.formatNumber(seq, parts[1]) : String.valueOf(seq), false);
                continue;
            }
            if (namepart.startsWith("worksheet_seq")) {
                parts = StringUtil.split(namepart, ";");
                int seq = sequenceProcessor.getSequence("LV_Worksheet", "main");
                worksheetname = StringUtil.replaceAll(worksheetname, "[" + namepart + "]", parts.length > 1 ? NumericFormatter.formatNumber(seq, parts[1]) : String.valueOf(seq), false);
                continue;
            }
            if (attributes == null || attributes.size() <= 0) continue;
            String attributeid = namepart;
            if (namepart.startsWith("metadata.")) {
                attributeid = namepart.substring(9);
            }
            if ((row = attributes.findRow("attributeid", attributeid)) < 0) continue;
            worksheetname = StringUtil.replaceAll(worksheetname, "[" + namepart + "]", attributes.getValue(row, "value"), false);
        }
        return worksheetname;
    }

    public static String validateMetaData(QueryProcessor queryProcessor, String sdcid, String id, String ver) throws SapphireException {
        DataSet metadata = queryProcessor.getPreparedSqlDataSet("SELECT attributeid, datatype, textvalue, numericvalue, datevalue, clobvalue FROM sdiattribute WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND mandatoryflag = 'Y'", new Object[]{sdcid, id, ver}, true);
        StringBuffer message = new StringBuffer();
        for (int i = 0; i < metadata.size(); ++i) {
            String datatype = metadata.getValue(i, "datatype");
            if (datatype.equals("S") && metadata.getValue(i, "textvalue").length() == 0) {
                message.append("<br/>Metadata '").append(metadata.getValue(i, "attributeid")).append("' has not been entered");
                continue;
            }
            if (datatype.equals("C") && metadata.getValue(i, "clobvalue").length() == 0) {
                message.append("<br/>Metadata '").append(metadata.getValue(i, "attributeid")).append("' has not been entered");
                continue;
            }
            if (datatype.equals("N") && metadata.getValue(i, "numericvalue").length() == 0) {
                message.append("<br/>Metadata '").append(metadata.getValue(i, "attributeid")).append("' has not been entered");
                continue;
            }
            if (!datatype.equals("D") && !datatype.equals("O") || metadata.getValue(i, "datevalue").length() != 0) continue;
            message.append("<br/>Metadata '").append(metadata.getValue(i, "attributeid")).append("' has not been entered");
        }
        return message.length() > 0 ? message.substring(5) : "";
    }

    public static String[] getUserWorkbook(String sysuserid, DBAccess database, ActionProcessor actionProcessor, ConfigurationProcessor configurationProcessor, boolean create) throws SapphireException {
        String[] myworkbookid;
        String profileworkbookid = configurationProcessor.getProfileProperty(sysuserid, "defaultworkbook");
        if (profileworkbookid.length() > 0 && database.getPreparedCount("SELECT count(*) FROM workbook WHERE workbookid=? AND workbookversionid=? AND workbookstatus<>'Cancelled'", new String[]{(myworkbookid = StringUtil.split(profileworkbookid.contains(";") ? profileworkbookid : profileworkbookid + ";1", ";"))[0], myworkbookid[1]}) == 0) {
            profileworkbookid = "";
        }
        if (profileworkbookid.length() > 0) {
            return StringUtil.split(profileworkbookid.contains(";") ? profileworkbookid : profileworkbookid + ";1", ";");
        }
        if (create) {
            PropertyList wbProps = new PropertyList();
            wbProps.setProperty("sdcid", "LV_Workbook");
            wbProps.setProperty("keyid2", "1");
            wbProps.setProperty("workbookdesc", sysuserid.equals("(system)") ? "SystemUser Workbook" : sysuserid + " Workbook");
            wbProps.setProperty("ownerid", sysuserid.equals("(system)") ? "" : sysuserid);
            wbProps.setProperty("workbookstatus", "InProgress");
            actionProcessor.processAction("AddSDI", "1", wbProps);
            String[] myworkbookid2 = new String[]{wbProps.getProperty("newkeyid1"), wbProps.getProperty("newkeyid2")};
            configurationProcessor.setProfileProperty(sysuserid, "defaultworkbook", myworkbookid2[0] + ";" + myworkbookid2[1]);
            return myworkbookid2;
        }
        return new String[]{"", ""};
    }

    public static void getWorksheetItemIncludes(SapphireConnection sapphireConnection, QueryProcessor queryProcessor, PropertyListCollection cssIncludes, PropertyListCollection jsIncludes) {
        DataSet worksheetItems = queryProcessor.getPreparedSqlDataSet("SELECT objectname FROM propertytree WHERE propertytreetype = ?", new Object[]{"WorksheetItem"});
        for (int i = 0; i < worksheetItems.size(); ++i) {
            String objectname = worksheetItems.getValue(i, "objectname");
            if (objectname.length() <= 0) continue;
            try {
                WorksheetItem worksheetItem = WorksheetItemFactory.getIncludesInstance(sapphireConnection, objectname);
                WorksheetItemIncludes worksheetItemIncludes = worksheetItem.getWorksheetItemIncludes();
                ArrayList<String> styleIncludes = worksheetItemIncludes.getStyleIncludes();
                Iterator<String> iterator = styleIncludes.iterator();
                while (iterator.hasNext()) {
                    PropertyList cssfile = new PropertyList();
                    cssfile.setProperty("cssfile", iterator.next());
                    cssIncludes.add(cssfile);
                }
                ArrayList<String> scriptIncludes = worksheetItemIncludes.getScriptIncludes();
                Iterator<String> iterator2 = scriptIncludes.iterator();
                while (iterator2.hasNext()) {
                    PropertyList scriptfile = new PropertyList();
                    scriptfile.setProperty("scriptfile", iterator2.next());
                    jsIncludes.add(scriptfile);
                }
                continue;
            }
            catch (Exception e) {
                Trace.logError("Failed to get include files for worksheetitem '" + objectname + "'", e);
            }
        }
    }

    protected PropertyList buildOptionProps(String[] optionProps, String options, PropertyList properties, PropertyList wsProps) throws SapphireException {
        boolean optionChanged = false;
        PropertyList optionPL = new PropertyList();
        if (options.length() > 0) {
            optionPL.setPropertyList(options);
        }
        for (int i = 0; i < optionProps.length; ++i) {
            String value = properties.getProperty(optionProps[i]);
            if (value.length() <= 0) continue;
            optionPL.setProperty(optionProps[i], value.equals("(null)") ? "" : value);
            optionChanged = true;
        }
        if (optionChanged) {
            wsProps.setProperty("options", optionPL.toXMLString());
        }
        return optionPL;
    }

    protected void addResetApproval(ActionBlock ab, String sdcid, String keyid1, String keyid2, DataSet approvalsteps) throws ActionException {
        if (approvalsteps.size() > 0) {
            StringBuffer approvaltypeList = new StringBuffer();
            StringBuffer approvalstepList = new StringBuffer();
            StringBuffer approvalinstList = new StringBuffer();
            StringBuffer approvalflagList = new StringBuffer();
            for (int i = 0; i < approvalsteps.size(); ++i) {
                approvaltypeList.append(";").append(approvalsteps.getValue(i, "approvaltypeid"));
                approvalstepList.append(";").append(approvalsteps.getValue(i, "approvalstep"));
                approvalinstList.append(";").append(approvalsteps.getValue(i, "approvalstepinstance"));
                approvalflagList.append(";").append("U");
            }
            HashMap<String, String> approvalProps = new HashMap<String, String>();
            approvalProps.put("sdcid", sdcid);
            approvalProps.put("keyid1", keyid1);
            approvalProps.put("keyid2", keyid2);
            approvalProps.put("approvaltypeid", approvaltypeList.substring(1));
            approvalProps.put("approvalstep", approvalstepList.substring(1));
            approvalProps.put("approvalstepinstance", approvalinstList.substring(1));
            approvalProps.put("approvalflag", approvalflagList.substring(1));
            ab.setAction("approvesdistep", "ApproveSDIStep", "1", approvalProps);
        }
    }
}

