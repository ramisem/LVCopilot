/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.eln.gwt.server;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.RSet;
import com.labvantage.sapphire.actions.eln.AddWorksheet;
import com.labvantage.sapphire.actions.eln.AddWorksheetContributor;
import com.labvantage.sapphire.actions.eln.AddWorksheetItem;
import com.labvantage.sapphire.actions.eln.AddWorksheetItemRef;
import com.labvantage.sapphire.actions.eln.AddWorksheetItemSDI;
import com.labvantage.sapphire.actions.eln.AddWorksheetSDI;
import com.labvantage.sapphire.actions.eln.AddWorksheetSection;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetContributor;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetItem;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetItemSDI;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetSDI;
import com.labvantage.sapphire.actions.eln.DeleteWorksheetSection;
import com.labvantage.sapphire.actions.eln.EditWorksheet;
import com.labvantage.sapphire.actions.eln.EditWorksheetItem;
import com.labvantage.sapphire.actions.eln.EditWorksheetItemParams;
import com.labvantage.sapphire.actions.eln.EditWorksheetSection;
import com.labvantage.sapphire.actions.eln.GenerateQCBatchWorksheet;
import com.labvantage.sapphire.actions.eln.GenerateReagentWorksheet;
import com.labvantage.sapphire.actions.eln.GenerateSampleWorksheet;
import com.labvantage.sapphire.actions.eln.GenerateTestMethodWorksheet;
import com.labvantage.sapphire.actions.eln.GenerateWorkorderWorksheet;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemConfig;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemContent;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemStatus;
import com.labvantage.sapphire.actions.eln.SetWorksheetSectionStatus;
import com.labvantage.sapphire.actions.eln.SetWorksheetStatus;
import com.labvantage.sapphire.actions.sdi.BaseSDIAction;
import com.labvantage.sapphire.actions.sdi.DeleteSDINote;
import com.labvantage.sapphire.modules.eln.SignWorksheet;
import com.labvantage.sapphire.modules.eln.Worksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.modules.eln.gwt.server.CopyWorksheet;
import com.labvantage.sapphire.modules.eln.gwt.server.LoadSection;
import com.labvantage.sapphire.modules.eln.gwt.server.RemoveIncidentSource;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.Attachment;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFields;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFieldsField;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemTokenResolver;
import com.labvantage.sapphire.modules.search.Indexer;
import com.labvantage.sapphire.modules.search.SearchDocument;
import com.labvantage.sapphire.modules.search.SearchRequest;
import com.labvantage.sapphire.modules.search.SearchResults;
import com.labvantage.sapphire.modules.search.Searcher;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandRequest;
import com.labvantage.sapphire.pageelements.gwt.server.command.CommandResponse;
import com.labvantage.sapphire.pageelements.gwt.server.command.JSONableMap;
import com.labvantage.sapphire.pageelements.gwt.server.command.StandardCommandRequest;
import com.labvantage.sapphire.pageelements.gwt.shared.ELNConstants;
import com.labvantage.sapphire.pageelements.maint.EditorStyleUtil;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.DataAccessService;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import org.json.JSONException;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.SDCProcessor;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.FormatUtil;
import sapphire.util.M18NUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIList;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class ELNRequest
extends StandardCommandRequest
implements ELNConstants {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected boolean processCommand(String command, CommandRequest commandRequest, CommandResponse commandResponse) throws SapphireException {
        super.processCommand(command, commandRequest, commandResponse);
        DBUtil dbu = new DBUtil(this.sapphireConnection.getConnectionId());
        try {
            dbu.setConnection(this.sapphireConnection);
            if (command.equals("lwsm")) {
                this.loadWSM(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwb")) {
                this.loadWorkbook(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwssdis")) {
                this.loadWorksheetSDIs(commandRequest, commandResponse, dbu);
            } else if (command.equals("lcws")) {
                this.loadCopyWorksheet(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwss")) {
                this.loadWorksheets(commandRequest, commandResponse, dbu);
            } else if (command.equals("lc")) {
                this.loadContributors(commandRequest, commandResponse, dbu);
            } else if (command.equals("ac")) {
                this.addContributor(commandRequest, commandResponse, dbu);
            } else if (command.equals("dc")) {
                this.deleteContributor(commandRequest, commandResponse, dbu);
            } else if (command.equals("lf")) {
                this.loadFields(commandRequest, commandResponse, dbu);
            } else if (command.equals("latt")) {
                this.loadAttachments(commandRequest, commandResponse, dbu);
            } else if (command.equals("rsaf")) {
                this.resequenceAttachmentFields(commandRequest, commandResponse, dbu);
            } else if (command.equals("la")) {
                this.loadAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("ladef")) {
                this.loadAttributeDefs(commandRequest, commandResponse, dbu);
            } else if (command.equals("subv")) {
                this.substituteValues(commandRequest, commandResponse, dbu);
            } else if (command.equals("aws")) {
                this.addWorksheet(commandRequest, commandResponse, dbu);
            } else if (command.equals("awsv")) {
                this.addWorksheetVersion(commandRequest, commandResponse, dbu);
            } else if (command.equals("apvwsv")) {
                this.approveWorksheetVersion(commandRequest, commandResponse, dbu);
            } else if (command.equals("dwssdi")) {
                this.deleteWorksheetSDI(commandRequest, commandResponse, dbu);
            } else if (command.equals("wsnc")) {
                this.worksheetNameCheck(commandRequest, commandResponse, dbu);
            } else if (command.equals("ews")) {
                this.editWorksheet(commandRequest, commandResponse, dbu);
            } else if (command.equals("swss")) {
                this.setWorksheetStatus(commandRequest, commandResponse, dbu);
            } else if (command.equals("ltd")) {
                this.loadTemplateDetails(commandRequest, commandResponse, dbu);
            } else if (command.equals("sat")) {
                this.saveAsTemplate(commandRequest, commandResponse, dbu);
            } else if (command.equals("awss")) {
                this.addSection(commandRequest, commandResponse, dbu);
            } else if (command.equals("ewss")) {
                this.editSection(commandRequest, commandResponse, dbu);
            } else if (command.equals("mwss")) {
                this.moveSection(commandRequest, commandResponse, dbu);
            } else if (command.equals("swsss")) {
                this.setSectionStatus(commandRequest, commandResponse, dbu);
            } else if (command.equals("dwss")) {
                this.deleteSection(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwsi")) {
                this.loadWorksheetItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwsid")) {
                this.loadWorksheetItemDiff(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwiinc")) {
                this.loadWorksheetItemIncludes(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwsia")) {
                this.loadItemAudit(commandRequest, commandResponse, dbu);
            } else if (command.equals("lwsisdis")) {
                this.loadItemSDIs(commandRequest, commandResponse, dbu);
            } else if (command.equals("awsi")) {
                this.addItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("awssdi")) {
                this.addSDI(commandRequest, commandResponse, dbu);
            } else if (command.equals("dwsisdi")) {
                this.deleteItemSDI(commandRequest, commandResponse, dbu);
            } else if (command.equals("awsiref")) {
                this.addItemRef(commandRequest, commandResponse, dbu);
            } else if (command.equals("ewsi")) {
                this.editItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("mwsi")) {
                this.moveItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("swsis")) {
                this.setItemStatus(commandRequest, commandResponse, dbu);
            } else if (command.equals("ceo")) {
                this.canExecuteOperation(commandRequest, commandResponse, dbu);
            } else if (command.equals("sewsic")) {
                this.startEditContent(commandRequest, commandResponse, dbu);
            } else if (command.equals("eewsic")) {
                this.endEditContent(commandRequest, commandResponse, dbu);
            } else if (command.equals("rwsic")) {
                this.revertItemContent(commandRequest, commandResponse, dbu);
            } else if (command.equals("addincident")) {
                this.recordIncident(commandRequest, commandResponse, dbu);
            } else if (command.equals("cewsic")) {
                this.cancelEditContent(commandRequest, commandResponse, dbu);
            } else if (command.equals("dwsi")) {
                this.deleteItem(commandRequest, commandResponse, dbu);
            } else if (command.equals("lid")) {
                this.loadItemDetails(commandRequest, commandResponse, dbu);
            } else if (command.equals("lao")) {
                this.loadAddOptions(commandRequest, commandResponse, dbu);
            } else if (command.equals("lr")) {
                this.loadReferences(commandRequest, commandResponse, dbu);
            } else if (command.equals("lal")) {
                this.loadActivityLog(commandRequest, commandResponse, dbu);
            } else if (command.equals("loga")) {
                this.logActivity(commandRequest, commandResponse, dbu);
            } else if (command.equals("lic")) {
                this.loadConfig(commandRequest, commandResponse, dbu);
            } else if (command.equals("loadbehaviors")) {
                this.loadBehaviors(commandRequest, commandResponse, dbu);
            } else if (command.equals("sc")) {
                this.saveConfig(commandRequest, commandResponse, dbu);
            } else if (command.equals("lo")) {
                this.loadOptions(commandRequest, commandResponse, dbu);
            } else if (command.equals("so")) {
                this.saveOptions(commandRequest, commandResponse, dbu);
            } else if (command.equals("sup")) {
                this.saveUserPrivs(commandRequest, commandResponse, dbu);
            } else if (command.equals("asa")) {
                this.addSDIAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("dsa")) {
                this.deleteSDIAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("sp")) {
                this.saveParameters(commandRequest, commandResponse, dbu);
            } else if (command.equals("vssa")) {
                this.validateSDIAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("ssa")) {
                this.saveSDIAttributes(commandRequest, commandResponse, dbu);
            } else if (command.equals("asatt")) {
                this.addSDIAttachment(commandRequest, commandResponse, dbu);
            } else if (command.equals("esatt")) {
                this.editSDIAttachment(commandRequest, commandResponse, dbu);
            } else if (command.equals("dsatt")) {
                this.deleteSDIAttachment(commandRequest, commandResponse, dbu);
            } else if (command.equals("rd")) {
                this.resetDetails(commandRequest, commandResponse, dbu);
            } else if (command.equals("eab")) {
                this.executeActionBlock(commandRequest, commandResponse, dbu);
            } else if (command.equals("chknote")) {
                this.checkFollowupNote(commandRequest, commandResponse, dbu);
            } else if (command.equals("spd")) {
                this.signPdfDocument(commandRequest, commandResponse);
            }
            boolean bl = true;
            return bl;
        }
        finally {
            dbu.releaseConnection();
        }
    }

    private void recordIncident(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        PropertyList props = new PropertyList();
        props.setProperty("templateid", commandRequest.getString("templateid"));
        DataSet items = new DataSet();
        items.addColumn("sdcid", 0);
        items.addColumn("keyid1", 0);
        items.addColumn("keyid2", 0);
        items.addColumn("keyid3", 0);
        items.addColumn("causalobjectflag", 0);
        String sourcesdcid = commandRequest.getString("sourcesdcid");
        String sourcekeyid1 = commandRequest.getString("sourcekeyid1");
        String sourcekeyid2 = commandRequest.getString("sourcekeyid2");
        int sourceRow = items.addRow();
        items.setString(sourceRow, "sdcid", sourcesdcid);
        items.setString(sourceRow, "keyid1", sourcekeyid1);
        items.setString(sourceRow, "keyid2", sourcekeyid2);
        items.setString(sourceRow, "causalobjectflag", "Y");
        if (!sourcesdcid.equals("LV_Worksheet")) {
            int worksheetRow = items.addRow();
            items.setString(worksheetRow, "sdcid", "LV_Worksheet");
            items.setString(worksheetRow, "keyid1", worksheetid);
            items.setString(worksheetRow, "keyid2", worksheetversionid);
            items.setString(worksheetRow, "causalobjectflag", "N");
        }
        if (commandRequest.getString("addworksheetsdis").equalsIgnoreCase("Y")) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3, 'N' causalobjectflag FROM worksheetsdi WHERE worksheetid=? AND worksheetversionid=? ORDER BY keyid1, keyid2, keyid3", (Object[])new String[]{worksheetid, worksheetversionid});
            items.copyRow(ds, -1, 1);
        } else if (commandRequest.getString("additemsdis").equalsIgnoreCase("Y")) {
            DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdcid, keyid1, keyid2, keyid3, 'N' causalobjectflag FROM worksheetitemsdi WHERE worksheetitemid=? AND worksheetitemversionid=? ORDER BY keyid1, keyid2, keyid3", (Object[])new String[]{sourcekeyid1, sourcekeyid2});
            items.copyRow(ds, -1, 1);
        }
        props.setProperty("sourcesdcid", items.getColumnValues("sdcid", ";"));
        props.setProperty("sourcekeyid1", items.getColumnValues("keyid1", ";"));
        props.setProperty("sourcekeyid2", items.getColumnValues("keyid2", ";"));
        props.setProperty("sourcekeyid3", items.getColumnValues("keyid3", ";"));
        props.setProperty("causalobjectflag", items.getColumnValues("causalobjectflag", ";"));
        try {
            this.getActionProcessor().processAction("RecordIncident", "1", props);
            String incidentid = props.getProperty("newkeyid1");
            commandResponse.set("incidentid", incidentid);
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", sourcesdcid);
            activityProps.setProperty("targetkeyid1", sourcekeyid1);
            activityProps.setProperty("targetkeyid2", sourcekeyid2);
            activityProps.setProperty("activitytype", "Add");
            activityProps.setProperty("activitylog", "Added Incident: " + incidentid);
            this.getActionProcessor().processActionClass(AddWorksheetActivity.class.getName(), activityProps);
        }
        catch (ActionException e) {
            commandResponse.setStatusFail("Failed create Incident. Reason: " + e.getMessage(), e);
        }
    }

    private void loadBehaviors(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        boolean refreshItemAvailability = commandRequest.getString("refreshitemavailability").equals("Y");
        boolean processLegacyExcludeRules = commandRequest.getString("processlegacyexcluderules").equals("Y");
        Worksheet worksheet = new Worksheet(this.sapphireConnection);
        try {
            DataSet[] behaviors = worksheet.loadBehaviors(worksheetid, worksheetversionid, refreshItemAvailability, processLegacyExcludeRules);
            commandResponse.set("sections", behaviors[0]);
            if (refreshItemAvailability) {
                commandResponse.set("items", behaviors[1]);
            }
        }
        catch (SapphireException e) {
            commandResponse.setStatusFail("Failed to load Behaviors. Reason: " + e.getMessage(), e);
        }
    }

    private void substituteValues(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil dbu) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        String value = commandRequest.getString("value");
        WorksheetItemTokenResolver resolver = new WorksheetItemTokenResolver(worksheetid, worksheetversionid, this.getQueryProcessor(), this.sapphireConnection);
        value = resolver.resolveTokens(worksheetitemid, worksheetitemversionid, value, null);
        commandResponse.set("value", value);
    }

    private String doSub(String value, String param, String newvalue) {
        value = StringUtil.replaceAll(value, "$S{" + param + "}", newvalue == null ? "" : newvalue);
        value = StringUtil.replaceAll(value, "[" + param + "]", newvalue == null ? "" : newvalue);
        return value;
    }

    private void loadWSM(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            this.getMyWorkbook(commandResponse, database);
            commandResponse.set("workbooksexist", "Y");
            try {
                commandResponse.set("searchenabled", Indexer.getInstance(this.sapphireConnection.getDatabaseId()).isSearching() ? "Y" : "N");
            }
            catch (Exception e) {
                commandResponse.set("searchenabled", "N");
            }
            database.createPreparedResultSet("SELECT count(*) count FROM worksheet WHERE templateflag = 'Y' AND templatetypeflag = ?", new Object[]{"W"});
            database.getNext();
            if (database.getInt("count") == 0) {
                PropertyList templateProps = new PropertyList();
                templateProps.setProperty("worksheetname", "Blank");
                templateProps.setProperty("templateflag", "Y");
                this.getActionProcessor().processActionClass(AddWorksheet.class.getName(), templateProps);
            }
            String queryid = commandRequest.getString("queryid");
            String generatetype = commandRequest.getString("generatetype");
            String worksheetid = commandRequest.getString("worksheetid");
            if (queryid.length() > 0) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_Worksheet");
                sdiRequest.setQueryid(queryid);
                String[] queryparams = new String[12];
                for (int i = 0; i < queryparams.length; ++i) {
                    queryparams[i] = commandRequest.getString("param" + (i + 1));
                }
                sdiRequest.setQueryParams(queryparams);
                sdiRequest.setRequestItem("primary");
                SDIData worksheetData = this.getSDIProcessor().getSDIData(sdiRequest);
                if (worksheetData != null && worksheetData.getDataset("primary") != null) {
                    DataSet worksheets = worksheetData.getDataset("primary");
                    commandResponse.set("worksheetid", worksheets.getColumnValues("worksheetid", ";"));
                    commandResponse.set("worksheetversionid", worksheets.getColumnValues("worksheetversionid", ";"));
                    commandResponse.set("worksheetinputlist", "Y");
                }
            } else if (generatetype.length() > 0) {
                String templateid = commandRequest.getString("generatetemplateid");
                String templateversionid = commandRequest.getString("generatetemplateversionid");
                if (templateid.length() > 0 && templateversionid.length() > 0) {
                    String workbookid = "";
                    String workbookdesc = "WSP-LESPreviews";
                    database.createPreparedResultSet("SELECT workbookid FROM workbook WHERE workbookdesc = ?", new Object[]{workbookdesc});
                    if (database.getNext()) {
                        workbookid = database.getValue("workbookid");
                    }
                    if (workbookid.length() == 0) {
                        PropertyList addWorkbook = new PropertyList();
                        addWorkbook.setProperty("sdcid", "LV_Workbook");
                        addWorkbook.setProperty("keyid2", "1");
                        addWorkbook.setProperty("workbookdesc", workbookdesc);
                        addWorkbook.setProperty("workbookstatus", "InProgress");
                        this.getActionProcessor().processAction("AddSDI", "1", addWorkbook);
                        workbookid = addWorkbook.getProperty("newkeyid1");
                    }
                    StringBuffer wsid = new StringBuffer();
                    StringBuffer wsver = new StringBuffer();
                    database.createPreparedResultSet("SELECT worksheetid, worksheetversionid FROM worksheet WHERE worksheetdesc like 'WSP-%' and createdt < ?", new Object[]{new DateTimeUtil().getTimestamp("now-1d")});
                    while (database.getNext()) {
                        wsid.append(";").append(database.getValue("worksheetid"));
                        wsver.append(";").append(database.getValue("worksheetversionid"));
                    }
                    if (wsid.length() > 0) {
                        PropertyList deleteWS = new PropertyList();
                        deleteWS.setProperty("sdcid", "LV_Worksheet");
                        deleteWS.setProperty("keyid1", wsid.substring(1));
                        deleteWS.setProperty("keyid2", wsver.substring(1));
                        this.getActionProcessor().processAction("DeleteSDI", "1", deleteWS);
                    }
                    PropertyList genWorksheet = new PropertyList();
                    genWorksheet.setProperty("workbookid", workbookid);
                    genWorksheet.setProperty("workbookversionid", "1");
                    genWorksheet.setProperty("templateid", templateid);
                    genWorksheet.setProperty("templateversionid", templateversionid);
                    genWorksheet.setProperty("preview", "Y");
                    if (generatetype.equalsIgnoreCase("WorkItemWorksheet")) {
                        String sampleid = commandRequest.getString("generatepreviewkeyid1");
                        String workitemid = commandRequest.getString("generatekeyid1");
                        SafeSQL safeSQL = new SafeSQL();
                        database.createPreparedResultSet("SELECT sdiworkitemid FROM sdiworkitem WHERE sdcid = 'Sample' AND keyid1 IN (" + safeSQL.addIn(sampleid, ";") + ") AND keyid2 = '(null)' AND keyid3 = '(null)' AND workitemid=" + safeSQL.addVar(workitemid) + " AND workiteminstance = 1", safeSQL.getValues());
                        StringBuffer sdiworkitemid = new StringBuffer();
                        while (database.getNext()) {
                            sdiworkitemid.append(";").append(database.getValue("sdiworkitemid"));
                        }
                        if (sdiworkitemid.length() == 0) {
                            throw new SapphireException("No workitem specified when generating a workitem worksheet.");
                        }
                        genWorksheet.setProperty("sdiworkitemid", sdiworkitemid.substring(1));
                        this.getActionProcessor().processActionClass(GenerateTestMethodWorksheet.class.getName(), genWorksheet);
                    } else if (generatetype.equalsIgnoreCase("SampleWorksheet")) {
                        String sampleid = commandRequest.getString("generatepreviewkeyid1");
                        if (sampleid.length() == 0) {
                            throw new SapphireException("No samples specified when generating a sample worksheet.");
                        }
                        genWorksheet.setProperty("sampleid", sampleid);
                        this.getActionProcessor().processActionClass(GenerateSampleWorksheet.class.getName(), genWorksheet);
                    } else if (generatetype.equalsIgnoreCase("qcbatch")) {
                        String qcbatchid = commandRequest.getString("generatepreviewkeyid1");
                        if (qcbatchid.length() == 0) {
                            throw new SapphireException("No qcbatch specified when generating a qcbatch worksheet.");
                        }
                        genWorksheet.setProperty("qcbatchid", qcbatchid);
                        this.getActionProcessor().processActionClass(GenerateQCBatchWorksheet.class.getName(), genWorksheet);
                    } else if (generatetype.equalsIgnoreCase("consumable")) {
                        String reagentlotid = commandRequest.getString("generatepreviewkeyid1");
                        if (reagentlotid.length() == 0) {
                            throw new SapphireException("No Consumable Lot specified when generating a Consumable worksheet.");
                        }
                        genWorksheet.setProperty("reagentlotid", reagentlotid);
                        this.getActionProcessor().processActionClass(GenerateReagentWorksheet.class.getName(), genWorksheet);
                    } else if (generatetype.equalsIgnoreCase("workorder")) {
                        String workorder = commandRequest.getString("generatepreviewkeyid1");
                        if (workorder.length() == 0) {
                            throw new SapphireException("No WorkOrder specified when generating an Instrument Certification worksheet.");
                        }
                        genWorksheet.setProperty("workorderid", workorder);
                        this.getActionProcessor().processActionClass(GenerateWorkorderWorksheet.class.getName(), genWorksheet);
                    } else {
                        throw new SapphireException("Unrecognized generator type: " + generatetype);
                    }
                    commandResponse.set("worksheetid", genWorksheet.getProperty("worksheetid"));
                    commandResponse.set("worksheetversionid", genWorksheet.getProperty("worksheetversionid"));
                }
            } else if (worksheetid.length() > 0) {
                commandResponse.set("worksheetid", worksheetid);
                commandResponse.set("worksheetversionid", commandRequest.getString("worksheetversionid"));
                commandResponse.set("worksheetinputlist", "Y");
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load Worksheet Manager. Reason: " + e.getMessage(), e);
        }
    }

    private void loadWorkbook(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        block6: {
            try {
                String[] myworkbookid = this.getMyWorkbook(commandResponse, database);
                String workbookid = commandRequest.getString("workbookid", myworkbookid[0]);
                String workbookversionid = commandRequest.getString("workbookversionid", myworkbookid.length > 1 ? myworkbookid[1] : "1");
                commandResponse.set("workbookid", workbookid);
                commandResponse.set("workbookversionid", workbookversionid);
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_Workbook");
                sdiRequest.setKeyid1List(workbookid);
                sdiRequest.setKeyid2List(workbookversionid);
                sdiRequest.setRequestItem("primary");
                SDIData workbookData = this.getSDIProcessor().getSDIData(sdiRequest);
                if (workbookData != null && workbookData.getDataset("primary") != null && workbookData.getDataset("primary").size() == 1) {
                    commandResponse.set("workbook", workbookData.getDataset("primary"));
                    if (workbookData.getDataset("primary").getValue(0, "worksheettemplatesflag", "A").equals("A")) {
                        SDIRequest sdiRequestWorksheets = new SDIRequest();
                        sdiRequestWorksheets.setSDCid("LV_Worksheet");
                        sdiRequestWorksheets.setQueryFrom("worksheet");
                        sdiRequestWorksheets.setQueryWhere("templatetypeflag = 'W' AND ( lesflag = 'N' OR lesflag IS NULL ) AND ( ( templateprivacyflag = 'G' AND versionstatus IN ( " + this.getGlobalVersionStatusList() + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND versionstatus IN ('P', 'A', 'C') AND authorid = '" + this.sapphireConnection.getSysuserId() + "' ) )");
                        sdiRequestWorksheets.setRequestItem("primary");
                        sdiRequestWorksheets.setQueryOrderBy("worksheetname");
                        SDIData worksheetData = this.getSDIProcessor().getSDIData(sdiRequestWorksheets);
                        DataSet templates = worksheetData.getDataset("primary");
                        for (int i = 0; i < templates.size(); ++i) {
                            if (templates.getValue(i, "worksheetversionid").length() != 0) continue;
                            templates.setValue(i, "worksheetversionid", "C");
                            DataSet names = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetversionid, worksheetname FROM worksheet WHERE worksheetid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( worksheetversionid as integer ) DESC", new Object[]{templates.getValue(i, "worksheetid")});
                            if (names.size() <= 0) continue;
                            templates.setValue(i, "worksheetname", names.getValue(0, "worksheetname"));
                        }
                        commandResponse.set("workbooktemplates", templates);
                    } else {
                        commandResponse.set("workbooktemplates", this.loadWorkbookTemplates(workbookData.getDataset("primary").getValue(0, "workbookid"), workbookData.getDataset("primary").getValue(0, "workbookversionid"), "W", ""));
                    }
                    break block6;
                }
                throw new Exception("Workbook not found");
            }
            catch (Exception e) {
                commandResponse.setStatusFail("Failed to load workbook '" + commandRequest.getString("workbookid") + "'. Reason: " + e.getMessage(), e);
            }
        }
    }

    private String[] getMyWorkbook(CommandResponse commandResponse, DBUtil database) throws SapphireException {
        String[] myworkbookid = BaseELNAction.getUserWorkbook(this.sapphireConnection.getSysuserId(), database, this.getActionProcessor(), new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.sapphireConnection.getConnectionId()), true);
        DataSet myworkbook = myworkbookid.length >= 1 ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT workbookdesc FROM workbook WHERE workbookid = ? AND workbookversionid = ?", new Object[]{myworkbookid[0], myworkbookid.length > 1 ? myworkbookid[1] : "1"}) : null;
        commandResponse.set("myworkbookid", myworkbookid[0]);
        commandResponse.set("myworkbookversionid", myworkbookid.length > 1 ? myworkbookid[1] : "1");
        commandResponse.set("myworkbookdesc", myworkbook != null && myworkbook.size() == 1 ? myworkbook.getValue(0, "workbookdesc") : "");
        return myworkbookid;
    }

    private DataSet loadWorkbookTemplates(String workbookid, String workbookversionid, String templatetype, String propertytreeid) {
        DataSet templates = templatetype.equals("I") ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT workbooktemplate.worksheetid, workbooktemplate.worksheetversionid, worksheet.worksheetname, worksheet.templateprivacyflag FROM worksheetitem, workbooktemplate LEFT OUTER JOIN worksheet ON coalesce(worksheet.activeflag,'Y')='Y' AND workbooktemplate.worksheetid = worksheet.worksheetid AND workbooktemplate.worksheetversionid = worksheet.worksheetversionid WHERE workbooktemplate.workbookid = ? AND workbooktemplate.workbookversionid = ? AND workbooktemplate.worksheetid = worksheetitem.worksheetid AND typeflag = ? AND propertytreeid = ? AND versionstatus IN ('P', 'A', 'C') ORDER BY workbooktemplate.usersequence, worksheet.worksheetname", new Object[]{workbookid, workbookversionid, templatetype, propertytreeid}) : this.getQueryProcessor().getPreparedSqlDataSet("SELECT workbooktemplate.worksheetid, workbooktemplate.worksheetversionid, worksheet.worksheetname FROM workbooktemplate LEFT OUTER JOIN worksheet ON coalesce(worksheet.activeflag,'Y')='Y' AND workbooktemplate.worksheetid = worksheet.worksheetid AND workbooktemplate.worksheetversionid = worksheet.worksheetversionid  AND versionstatus IN ('P', 'A', 'C') WHERE workbooktemplate.workbookid = ? AND workbooktemplate.workbookversionid = ? AND typeflag = ? ORDER BY workbooktemplate.usersequence, worksheet.worksheetname", new Object[]{workbookid, workbookversionid, templatetype});
        for (int i = 0; i < templates.size(); ++i) {
            if (templates.getValue(i, "worksheetversionid").length() != 0) continue;
            templates.setValue(i, "worksheetversionid", "C");
            DataSet names = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetversionid, worksheetname FROM worksheet WHERE worksheetid = ? AND ( versionstatus = 'P' OR versionstatus = 'C' ) ORDER BY versionstatus, cast ( worksheetversionid as integer ) DESC", new Object[]{templates.getValue(i, "worksheetid")});
            if (names.size() <= 0) continue;
            templates.setValue(i, "worksheetname", names.getValue(0, "worksheetname"));
        }
        return templates;
    }

    private void loadCopyWorksheet(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            Object ds;
            DataSet worksheet;
            SDIRequest wsRequest = new SDIRequest();
            wsRequest.setSDIList("LV_Worksheet", worksheetid, worksheetversionid, "");
            wsRequest.setRequestItem("primary");
            wsRequest.setExtendedDataTypes(true);
            SDIData worksheetData = this.getSDIProcessor().getSDIData(wsRequest);
            DataSet dataSet = worksheet = worksheetData != null ? worksheetData.getDataset("primary") : null;
            if (worksheet == null || worksheet.size() != 1) {
                throw new SapphireException("Worksheet not found");
            }
            commandResponse.set("worksheet", worksheet);
            DataSet workbook = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM workbook WHERE workbookid = ? AND workbookversionid = ?", new Object[]{worksheet.getValue(0, "workbookid"), worksheet.getValue(0, "workbookversionid")}, true);
            commandResponse.set("workbook", workbook);
            DataSet template = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetname FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheet.getValue(0, "templateid"), worksheet.getValue(0, "templateversionid")}, true);
            commandResponse.set("templatename", template.size() == 1 ? template.getValue(0, "templatename") : "");
            this.getMyWorkbook(commandResponse, database);
            ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
            PropertyList policy = configProcessor.getPolicy("ELNPolicy", "Sapphire Custom");
            commandResponse.set("copyoptions", policy.getPropertyListNotNull("copyoptions"));
            DataSet attributes = ELNRequest.loadAllWorksheetAttributes(this.getQueryProcessor(), new M18NUtil(this.sapphireConnection), worksheetid, worksheetversionid, commandRequest.getBoolean("groupbyitem"));
            String isMandatoryAttributePresent = "";
            Iterator iterator = attributes.iterator();
            while (iterator.hasNext() && !(isMandatoryAttributePresent = ((HashMap)(ds = iterator.next())).containsKey("mandatoryflag") ? ((HashMap)ds).get("mandatoryflag").toString() : "").equals("Y")) {
            }
            commandResponse.set("isMandatoryAttributePresent", isMandatoryAttributePresent);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load copy worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private void loadWorksheets(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String workbookid = commandRequest.getString("workbookid");
        String workbookversionid = commandRequest.getString("workbookversionid");
        String query = StringUtil.replaceAll(commandRequest.getString("query"), "'", "'");
        String like = query.toLowerCase();
        like = query.startsWith("+") ? query.substring(1) : query;
        like = like.endsWith("*") || like.endsWith("~") ? like.substring(0, like.length() - 1) : like;
        int limit = Integer.parseInt(commandRequest.getString("limit"));
        try {
            PropertyListCollection worksheetqueries;
            DataSet worksheets;
            String union = "SELECT worksheetid FROM worksheet WHERE Lower( worksheetname ) like '%" + SafeSQL.encodeForSQL(like, this.sapphireConnection.isOracle()) + "%' UNION SELECT  worksheet.worksheetid FROM worksheet, worksheetsection WHERE worksheet.workbookid = '" + SafeSQL.encodeForSQL(workbookid, this.sapphireConnection.isOracle()) + "' AND worksheet.workbookversionid = '" + SafeSQL.encodeForSQL(workbookversionid, this.sapphireConnection.isOracle()) + "' AND worksheet.worksheetid = worksheetsection.worksheetid AND worksheet.worksheetversionid = worksheetsection.worksheetversionid AND Lower( worksheetsectiondesc ) like '%" + SafeSQL.encodeForSQL(like, this.sapphireConnection.isOracle()) + "%' UNION SELECT  worksheet.worksheetid FROM worksheet, worksheetitem WHERE worksheet.workbookid = '" + SafeSQL.encodeForSQL(workbookid, this.sapphireConnection.isOracle()) + "' AND worksheet.workbookversionid = '" + SafeSQL.encodeForSQL(workbookversionid, this.sapphireConnection.isOracle()) + "' AND worksheet.worksheetid = worksheetitem.worksheetid AND worksheet.worksheetversionid = worksheetitem.worksheetversionid AND Lower( worksheetitemdesc ) like '%" + SafeSQL.encodeForSQL(like, this.sapphireConnection.isOracle()) + "%' ";
            if (commandRequest.getBoolean("loadinputworksheets")) {
                SDIRequest sdiRequest = new SDIRequest();
                sdiRequest.setSDCid("LV_Worksheet");
                sdiRequest.setKeyid1List(commandRequest.getString("inputworksheetid"));
                sdiRequest.setKeyid2List(commandRequest.getString("inputworksheetversionid"));
                sdiRequest.setRequestItem("primary[worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, " + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet", "worksheetid", "worksheetversionid") + "]");
                sdiRequest.setQueryWhere(like.length() > 0 ? "worksheetid IN (" + union + ")" : "");
                SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                if (sdiData != null && sdiData.getDataset("primary") != null) {
                    commandResponse.set("inputworksheets", sdiData.getDataset("primary"));
                } else {
                    commandResponse.set("inputworksheets", new DataSet());
                }
            }
            if (commandRequest.getBoolean("loadworkbookworksheets")) {
                String sql = "SELECT worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, " + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet", "worksheetid", "worksheetversionid") + " FROM worksheet WHERE workbookid = ? AND workbookversionid = ? ";
                if (like.length() > 0) {
                    sql = sql + "AND worksheetid IN (" + union + ")";
                }
                DataSet worksheets2 = this.getQueryProcessor().getPreparedSqlDataSet(database.isOracle() ? "SELECT DISTINCT worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, notestatus FROM (" + sql + " ORDER BY authordt DESC) WHERE rownum < " + (limit + 1) : "SELECT DISTINCT TOP " + limit + " worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, notestatus FROM (" + sql + ") unionview ORDER BY authordt DESC", new Object[]{workbookid, workbookversionid});
                int count = this.getQueryProcessor().getPreparedSqlDataSet("SELECT count(*) \"count\" FROM worksheet WHERE workbookid = ? AND workbookversionid = ? AND ( templateflag = 'N' OR templateflag IS NULL )", new Object[]{workbookid, workbookversionid}).getInt(0, "count");
                commandResponse.set("workbookworksheets", worksheets2);
                commandResponse.set("moreworkbookworksheets", count > limit ? "Y" : "N");
            }
            if (commandRequest.getBoolean("loadrecentworksheets")) {
                worksheets = this.loadMRUList(commandRequest.getString("__hostwebpageid") + "_recent_worksheets", like, limit);
                commandResponse.set("recentworksheets", worksheets);
            }
            if (commandRequest.getBoolean("loadmatchingworksheets")) {
                worksheets = new DataSet();
                if (query.length() > 0) {
                    Searcher searcher = new Searcher(this.sapphireConnection);
                    SearchRequest searchRequest = new SearchRequest(query);
                    searchRequest.setSdcid("LV_Worksheet");
                    searchRequest.setShowTemplates(false);
                    SearchResults results = searcher.getSearchResults(searchRequest);
                    List<SearchDocument> searchDocuments = results.getSearchDocuments();
                    for (int i = 0; i < searchDocuments.size() && i < limit; ++i) {
                        int row;
                        SearchDocument searchDocument = searchDocuments.get(i);
                        if (searchDocument.getType().equals("NOTE") || searchDocument.getType().equals("ATTACHMENT")) {
                            row = worksheets.addRow();
                            if (searchDocument.getParentSdcid().equals("LV_Worksheet")) {
                                worksheets.setString(row, "worksheetid", searchDocument.getParentKeyid1());
                                worksheets.setString(row, "worksheetversionid", searchDocument.getParentKeyid2());
                                worksheets.setString(row, "worksheetname", searchDocument.getValue("worksheetname"));
                                worksheets.setString(row, "worksheetstatus", searchDocument.getValue("worksheetstatus"));
                                worksheets.setString(row, "authorid", searchDocument.getValue("authorid"));
                                worksheets.setString(row, "authordt", searchDocument.getValue("authordt"));
                                continue;
                            }
                            if (!searchDocument.getSdcid().equals("LV_Worksheet")) continue;
                            worksheets.setString(row, "worksheetid", searchDocument.getKeyid1());
                            worksheets.setString(row, "worksheetversionid", searchDocument.getKeyid2());
                            worksheets.setString(row, "worksheetname", searchDocument.getValue("worksheetname"));
                            worksheets.setString(row, "worksheetstatus", searchDocument.getValue("worksheetstatus"));
                            worksheets.setString(row, "authorid", searchDocument.getValue("authorid"));
                            worksheets.setString(row, "authordt", searchDocument.getValue("authordt"));
                            continue;
                        }
                        row = worksheets.addRow();
                        worksheets.setString(row, "worksheetid", searchDocument.getValue("worksheetid"));
                        worksheets.setString(row, "worksheetversionid", searchDocument.getValue("worksheetversionid"));
                        worksheets.setString(row, "worksheetname", searchDocument.getValue("worksheetname"));
                        worksheets.setString(row, "worksheetstatus", searchDocument.getValue("worksheetstatus"));
                        worksheets.setString(row, "authorid", searchDocument.getValue("authorid"));
                        worksheets.setString(row, "authordt", searchDocument.getValue("authordt"));
                    }
                }
                commandResponse.set("matchingworksheets", worksheets);
            }
            if ((worksheetqueries = commandRequest.getCollection("worksheetqueries")) != null) {
                for (int i = 0; i < worksheetqueries.size(); ++i) {
                    PropertyList worksheetquery = worksheetqueries.getPropertyList(i);
                    if (!worksheetquery.getProperty("show").equals("Y")) continue;
                    SDIRequest sdiRequest = new SDIRequest();
                    sdiRequest.setSDCid("LV_Worksheet");
                    sdiRequest.setQueryid(worksheetquery.getProperty("queryid"));
                    PropertyListCollection queryparams = worksheetquery.getCollection("queryparams");
                    if (queryparams != null && queryparams.size() > 0) {
                        String[] params = new String[queryparams.size()];
                        for (int j = 0; j < queryparams.size(); ++j) {
                            params[i] = StringUtil.replaceAll(queryparams.getPropertyList(j).getProperty("paramvalue"), "[currentuser]", this.sapphireConnection.getSysuserId());
                        }
                        sdiRequest.setQueryParams(params);
                    }
                    sdiRequest.setRequestItem("primary[worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, " + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet", "worksheetid", "worksheetversionid") + "]");
                    SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                    if (sdiData != null && sdiData.getDataset("primary") != null) {
                        commandResponse.set(worksheetquery.getProperty("queryid"), sdiData.getDataset("primary"));
                        continue;
                    }
                    commandResponse.set(worksheetquery.getProperty("queryid"), new DataSet());
                }
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheets with query '" + query + "'. Reason: " + e.getMessage(), e);
        }
    }

    private void loadWorksheetSDIs(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            String sdcid;
            int i;
            PropertyListCollection worksheetsdcs = commandRequest.getCollection("worksheetsdcs");
            if (worksheetsdcs == null || worksheetsdcs.size() == 0) {
                worksheetsdcs = new PropertyListCollection();
            }
            SDCProcessor sdcProcessor = this.getSDCProcessor();
            DataSet sdcs = commandRequest.getBoolean("groupbyitem") ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT wsi.wssusersequence, wsi.wsiusersequence, wsi.worksheetitemid, wsi.worksheetitemversionid, worksheetsdi.sdcid  FROM worksheetsdi LEFT OUTER JOIN   ( SELECT worksheetitem.worksheetitemid, worksheetitem.worksheetitemversionid, worksheetitem.worksheetid, worksheetitem.worksheetversionid, sdcid, keyid1, worksheetitem.usersequence wsiusersequence, worksheetsection.usersequence wssusersequence    FROM worksheetitem, worksheetitemsdi, worksheetsection     WHERE worksheetitem.worksheetitemid = worksheetitemsdi.worksheetitemid AND worksheetitem.worksheetitemversionid = worksheetitemsdi.worksheetitemversionid AND worksheetitem.worksheetsectionid = worksheetsection.worksheetsectionid AND worksheetitem.worksheetsectionversionid = worksheetsection.worksheetsectionversionid AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ) wsi     ON worksheetsdi.worksheetid = wsi.worksheetid AND worksheetsdi.worksheetversionid = wsi.worksheetversionid AND        worksheetsdi.sdcid = wsi.sdcid AND worksheetsdi.keyid1 = wsi.keyid1  WHERE worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ?  ORDER BY 1, 2", new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid}) : this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT sdcid FROM worksheetsdi WHERE worksheetid = ? AND worksheetversionid = ? ORDER BY 1", new Object[]{worksheetid, worksheetversionid});
            for (i = 0; i < sdcs.size(); ++i) {
                sdcid = sdcs.getValue(i, "sdcid");
                PropertyList sdc = worksheetsdcs.find("sdcid", sdcid);
                if (sdc == null) {
                    PropertyList worksheetsdc = new PropertyList();
                    try {
                        if (sdcid.equals("SDIWorkItem")) {
                            worksheetsdc.setJSONString("{sdcid: '" + sdcid + "', title: '" + StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "plural")) + "', columns: [   {columnid: 'keyid1', title: 'Sample'},   {columnid: 'workitemid', title: 'Test'}]}");
                        } else if (sdcid.equals("DataSet")) {
                            worksheetsdc.setJSONString("{sdcid: '" + sdcid + "', title: '" + StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "plural")) + "', columns: [   {columnid: 'keyid1', title: 'Sample'},   {columnid: 'paramlistid', title: 'ParamList'}]}");
                        } else if (sdcid.equals("DataItem")) {
                            worksheetsdc.setJSONString("{sdcid: '" + sdcid + "', title: '" + StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "plural")) + "', columns: [   {columnid: 'keyid1', title: 'Sample'},   {columnid: 'paramid', title: 'Param'}]}");
                        } else {
                            int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                            worksheetsdc.setJSONString("{sdcid: '" + sdcid + "', title: '" + StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "plural")) + "', columns: [   {columnid: '" + sdcProcessor.getProperty(sdcid, "keycolid1") + "', title: 'Id'}," + (keycols >= 2 ? "   {columnid: '" + sdcProcessor.getProperty(sdcid, "keycolid2") + "', title: 'Ver'}," : "") + (keycols >= 3 ? "   {columnid: '" + sdcProcessor.getProperty(sdcid, "keycolid3") + "', title: 'Var'}," : "") + "   {columnid: '" + sdcProcessor.getProperty(sdcid, "desccol") + "', title: 'Description'}]}");
                        }
                    }
                    catch (JSONException e) {
                        throw new SapphireException("Failed to create default column list for worksheetsdis", e);
                    }
                    worksheetsdcs.add(worksheetsdc);
                    worksheetsdc.setProperty("singular", StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "singular")));
                    worksheetsdc.setProperty("plural", StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "plural")));
                    continue;
                }
                sdc.setProperty("singular", StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "singular")));
                sdc.setProperty("plural", StringUtil.initCaps(sdcProcessor.getProperty(sdcid, "plural")));
            }
            if (commandRequest.getBoolean("groupbyitem")) {
                commandResponse.set("itemgroups", sdcs);
                for (i = 0; i < sdcs.size(); ++i) {
                    DataSet worksheetsdis;
                    String sql;
                    sdcid = sdcs.getValue(i, "sdcid");
                    String worksheetitemid = sdcs.getValue(i, "worksheetitemid");
                    String worksheetitemversionid = sdcs.getValue(i, "worksheetitemversionid");
                    PropertyList worksheetsdc = worksheetsdcs.find("sdcid", sdcid);
                    PropertyListCollection columns = worksheetsdc.getCollection("columns");
                    String tableid = sdcProcessor.getProperty(sdcid, "tableid");
                    int keycols = Integer.parseInt(sdcProcessor.getProperty(sdcid, "keycolumns"));
                    StringBuffer cols = new StringBuffer();
                    boolean isDTypeSDC = "D".equalsIgnoreCase(sdcProcessor.getProperty(sdcid, "sdctype"));
                    if (columns != null) {
                        for (int j = 0; j < columns.size(); ++j) {
                            cols.append(",").append(tableid).append(".").append(columns.getPropertyList(j).getProperty("columnid"));
                        }
                    }
                    String keycolid1 = sdcProcessor.getProperty(sdcid, "keycolid1");
                    String keycolid2 = sdcProcessor.getProperty(sdcid, "keycolid2");
                    String keycolid3 = sdcProcessor.getProperty(sdcid, "keycolid3");
                    if (worksheetitemid.length() > 0) {
                        sql = "SELECT worksheetitemsdi.worksheetitemid, worksheetitemsdi.worksheetitemversionid, " + tableid + "." + keycolid1 + ", " + tableid + "." + (isDTypeSDC ? "keyid1" : keycolid1 + " keyid1 ") + (keycols >= 2 ? ", " + tableid + "." + keycolid2 + (isDTypeSDC ? "" : " keyid2") : " ") + (keycols >= 3 ? ", " + tableid + "." + keycolid3 + (isDTypeSDC ? "" : " keyid3") : " ") + (cols.length() > 0 ? ", " + cols.substring(1) + " " : "") + "FROM worksheetitemsdi, " + tableid + " WHERE worksheetitemsdi.worksheetitemid = ? AND worksheetitemsdi.worksheetitemversionid = ? AND worksheetitemsdi.sdcid = ? AND worksheetitemsdi.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND worksheetitemsdi.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND worksheetitemsdi.keyid3 = " + tableid + "." + keycolid3 : "") + " ORDER BY 1" + (keycols >= 2 ? ", 2" : "") + (keycols >= 3 ? ", 3" : "");
                        worksheetsdis = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{worksheetitemid, worksheetitemversionid, sdcid});
                        commandResponse.set(worksheetitemid + "_" + sdcid, worksheetsdis);
                        continue;
                    }
                    sql = "SELECT null, null, " + tableid + "." + keycolid1 + ", " + tableid + "." + (isDTypeSDC ? "keyid1" : keycolid1 + " keyid1 ") + (keycols >= 2 ? ", " + tableid + "." + keycolid2 + (isDTypeSDC ? "" : " keyid2") : " ") + (keycols >= 3 ? ", " + tableid + "." + keycolid3 + (isDTypeSDC ? "" : " keyid3") : " ") + (cols.length() > 0 ? ", " + cols.substring(1) + " " : "") + "FROM worksheetsdi, " + tableid + " WHERE worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? AND worksheetsdi.sdcid = ? AND worksheetsdi.keyid1 = " + tableid + "." + keycolid1 + (keycols >= 2 ? " AND worksheetsdi.keyid2 = " + tableid + "." + keycolid2 : "") + (keycols >= 3 ? " AND worksheetsdi.keyid3 = " + tableid + "." + keycolid3 : "") + " AND " + (keycols == 1 ? "worksheetsdi.keyid1" : (keycols == 2 ? "{fn concat( worksheetsdi.keyid1, worksheetsdi.keyid2 )}" : "{fn concat( {fn concat( worksheetsdi.keyid1, worksheetsdi.keyid2 )}, worksheetsdi.keyid3 )}")) + " NOT IN ( SELECT " + (keycols == 1 ? "worksheetitemsdi.keyid1" : (keycols == 2 ? "{fn concat( worksheetitemsdi.keyid1, worksheetitemsdi.keyid2 )}" : "{fn concat( {fn concat( worksheetitemsdi.keyid1, worksheetitemsdi.keyid2 )}, worksheetitemsdi.keyid3 )}")) + " FROM worksheetitem, worksheetitemsdi  WHERE worksheetitem.worksheetitemid = worksheetitemsdi.worksheetitemid AND worksheetitem.worksheetitemversionid = worksheetitemsdi.worksheetitemversionid    AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ) ORDER BY 1" + (keycols >= 2 ? ", 2" : "") + (keycols >= 3 ? ", 3" : "");
                    worksheetsdis = this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{worksheetid, worksheetversionid, sdcid, worksheetid, worksheetversionid});
                    commandResponse.set("null_" + sdcid, worksheetsdis);
                }
            } else {
                for (i = 0; i < worksheetsdcs.size(); ++i) {
                    PropertyList worksheetsdc = worksheetsdcs.getPropertyList(i);
                    String sdcid2 = worksheetsdc.getProperty("sdcid");
                    DataSet worksheetsdis = this.loadWorksheetSDIs(worksheetid, worksheetversionid, sdcid2, worksheetsdc);
                    commandResponse.set(sdcid2 + "_worksheetsdi", worksheetsdis);
                }
            }
            commandResponse.set("worksheetsdcs", worksheetsdcs);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load SDIs for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private DataSet loadWorksheetSDIs(String worksheetid, String worksheetversionid, String sdcid, PropertyList sdc) {
        return this.loadWorksheetSDIs(worksheetid, worksheetversionid, sdcid, sdc, null, null, null, null);
    }

    private DataSet loadWorksheetSDIs(String worksheetid, String worksheetversionid, String sdcid, PropertyList sdc, PropertyList filter, String keyid1, String keyid2, String keyid3) {
        PropertyListCollection columns = sdc != null ? sdc.getCollection("columns") : null;
        PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
        String tableid = sdcProps.getProperty("tableid");
        int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        StringBuffer cols = new StringBuffer();
        if (columns != null) {
            for (int j = 0; j < columns.size(); ++j) {
                cols.append(",").append(tableid).append(".").append(columns.getPropertyList(j).getProperty("columnid"));
            }
        }
        String filterWhere = this.getFilterWhere(tableid, filter);
        String keyidWhere = keyid1 == null || keyid1.length() == 0 ? "" : tableid + "." + sdcProps.getProperty("keycolid1") + "='" + keyid1 + "'";
        keyidWhere = keyidWhere + (keycols >= 2 && keyid2 != null && keyid2.length() > 0 ? " AND " + tableid + "." + sdcProps.getProperty("keycolid2") + "='" + keyid2 + "'" : "");
        keyidWhere = keyidWhere + (keycols >= 3 && keyid3 != null && keyid3.length() > 0 ? " AND " + tableid + "." + sdcProps.getProperty("keycolid3") + "='" + keyid3 + "'" : "");
        String sql = "SELECT " + tableid + "." + sdcProps.getProperty("keycolid1") + ", " + tableid + "." + sdcProps.getProperty("keycolid1") + " keyid1 " + (keycols >= 2 ? ", " + tableid + "." + sdcProps.getProperty("keycolid2") + " keyid2 " : " ") + (keycols >= 3 ? ", " + tableid + "." + sdcProps.getProperty("keycolid3") + " keyid3 " : " ") + (cols.length() > 0 ? ", " + cols.substring(1) + " " : "") + "FROM worksheetsdi, " + tableid + " WHERE worksheetsdi.worksheetid = ? AND worksheetsdi.worksheetversionid = ? AND worksheetsdi.sdcid = ? AND worksheetsdi.keyid1 = " + tableid + "." + sdcProps.getProperty("keycolid1") + (keycols >= 2 ? " AND worksheetsdi.keyid2 = " + tableid + "." + sdcProps.getProperty("keycolid2") : "") + (keycols >= 3 ? " AND worksheetsdi.keyid3 = " + tableid + "." + sdcProps.getProperty("keycolid3") : "") + " " + (filterWhere.length() > 0 ? " AND " + filterWhere : "") + (keyidWhere.length() > 0 ? " AND " + keyidWhere : "") + " ORDER BY 1" + (keycols >= 2 ? ", 2" : "") + (keycols >= 3 ? ", 3" : "");
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{worksheetid, worksheetversionid, sdcid});
    }

    private DataSet loadItemSDIs(String worksheetitemid, String worksheetitemversionid, String sdcid, PropertyList sdc, PropertyList filter, String keyid1, String keyid2, String keyid3) {
        PropertyListCollection columns = sdc != null ? sdc.getCollection("columns") : null;
        PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
        String tableid = sdcProps.getProperty("tableid");
        int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
        StringBuffer cols = new StringBuffer();
        if (columns != null) {
            for (int j = 0; j < columns.size(); ++j) {
                cols.append(",").append(tableid).append(".").append(columns.getPropertyList(j).getProperty("columnid"));
            }
        }
        String filterWhere = this.getFilterWhere(tableid, filter);
        String keyidWhere = keyid1 == null || keyid1.length() == 0 ? "" : tableid + "." + sdcProps.getProperty("keycolid1") + "='" + keyid1 + "'";
        keyidWhere = keyidWhere + (keycols >= 2 && keyid2 != null && keyid2.length() > 0 ? " AND " + tableid + "." + sdcProps.getProperty("keycolid2") + "='" + keyid2 + "'" : "");
        keyidWhere = keyidWhere + (keycols >= 3 && keyid3 != null && keyid3.length() > 0 ? " AND " + tableid + "." + sdcProps.getProperty("keycolid3") + "='" + keyid3 + "'" : "");
        String sql = "SELECT " + (cols.length() > 0 ? cols.substring(1) + ", " : "") + tableid + "." + sdcProps.getProperty("keycolid1") + " keyid1 " + (keycols >= 2 ? ", " + tableid + "." + sdcProps.getProperty("keycolid2") + " keyid2 " : " ") + (keycols >= 3 ? ", " + tableid + "." + sdcProps.getProperty("keycolid3") + " keyid3 " : " ") + "FROM worksheetitemsdi, " + tableid + " WHERE worksheetitemsdi.worksheetitemid = ? AND worksheetitemsdi.worksheetitemversionid = ? AND worksheetitemsdi.sdcid = ? AND worksheetitemsdi.keyid1 = " + tableid + "." + sdcProps.getProperty("keycolid1") + (keycols >= 2 ? " AND worksheetitemsdi.keyid2 = " + tableid + "." + sdcProps.getProperty("keycolid2") : "") + (keycols >= 3 ? " AND worksheetitemsdi.keyid3 = " + tableid + "." + sdcProps.getProperty("keycolid3") : "") + " " + (filterWhere.length() > 0 ? " AND " + filterWhere : "") + (keyidWhere.length() > 0 ? " AND " + keyidWhere : "") + " ORDER BY 1";
        return this.getQueryProcessor().getPreparedSqlDataSet(sql, new Object[]{worksheetitemid, worksheetitemversionid, sdcid});
    }

    private String getFilterWhere(String tableid, PropertyList filter) {
        if (filter == null || filter.size() == 0) {
            return "";
        }
        String columnid = filter.getProperty("columnid");
        boolean in = filter.getProperty("operator", "in").equalsIgnoreCase("in");
        String valuelist = filter.getProperty("valuelist");
        if (columnid.length() == 0 || valuelist.length() == 0) {
            return "";
        }
        if (!valuelist.startsWith("'")) {
            valuelist = valuelist.contains(";") ? "'" + StringUtil.replaceAll(valuelist, ";", "','") + "'" : "'" + valuelist + "'";
        }
        return " " + tableid + "." + columnid + (in ? " in " : " not in ") + "(" + valuelist + ") ";
    }

    private void loadContributors(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            DataSet contributors = this.getQueryProcessor().getPreparedSqlDataSet("SELECT 'A' type, authorid contributorid, sysuserdesc, sysuserid, defaultdepartment departmentid FROM worksheet, sysuser WHERE worksheet.authorid = sysuser.sysuserid AND worksheetid = ? AND worksheetversionid = ? UNION SELECT 'C' type, contributorid, sysuserdesc, sysuserid, defaultdepartment departmentid FROM worksheetcontributor, sysuser WHERE worksheetcontributor.contributorid = sysuser.sysuserid AND worksheetid = ? AND worksheetversionid = ? AND nominatedflag = 'Y' UNION SELECT DISTINCT 'L' type, activityby contributorid, sysuserdesc, sysuser.sysuserid, departmentid FROM worksheetactivitylog, sysuser LEFT OUTER JOIN departmentsysuser ON departmentsysuser.sysuserid = sysuser.sysuserid AND    departmentsysuser.departmentid IN (SELECT departmentid FROM departmentsysuser WHERE sysuserid IN (SELECT authorid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ? ) UNION  SELECT departmentid FROM JOBTYPEDEPARTMENT, SYSUSERJOBTYPE, sysuser         WHERE JOBTYPEDEPARTMENT.JOBTYPEID = SYSUSERJOBTYPE.JOBTYPEID         AND SYSUSERJOBTYPE.SYSUSERID         IN (SELECT authorid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ? )         AND sysuser.SYSUSERID = SYSUSERJOBTYPE.SYSUSERID         AND sysuser.securitytypeflag = 'J' ) WHERE worksheetactivitylog.activityby = sysuser.sysuserid AND worksheetid = ? AND worksheetversionid = ? ORDER BY sysuserdesc, sysuserid, type", new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid, worksheetid, worksheetversionid, worksheetid, worksheetversionid, worksheetid, worksheetversionid});
            for (int i = 0; i < contributors.size(); ++i) {
                String sysuserid = contributors.getValue(i, "sysuserid");
                while (i + 1 < contributors.size() && contributors.getValue(i + 1, "sysuserid").equals(sysuserid)) {
                    contributors.deleteRow(i + 1);
                }
            }
            if (commandRequest.getBoolean("loadhistory")) {
                StringBuffer union = new StringBuffer();
                SafeSQL safeSQL = new SafeSQL();
                for (int i = 0; i < contributors.size(); ++i) {
                    union.append(" UNION ").append("SELECT activityby, activitydt, activitytype, activitylog, targetsdcid, targetkeyid1, targetkeyid2 FROM (SELECT ").append(this.sapphireConnection.isSqlServer() ? "TOP 5" : "").append(" activityby, activitydt, activitytype, activitylog, targetsdcid, targetkeyid1, targetkeyid2 FROM worksheetactivitylog WHERE worksheetid = ").append(safeSQL.addVar(worksheetid)).append(" AND worksheetversionid = ").append(safeSQL.addVar(worksheetversionid)).append(" AND activityby = ").append(safeSQL.addVar(contributors.getValue(i, "contributorid"))).append(" AND activitytype NOT IN (" + safeSQL.addIn("Open") + ") ORDER BY activitydt DESC ) wsal ").append(this.sapphireConnection.isOracle() ? "WHERE rownum < 6" : "");
                }
                if (contributors.size() > 0) {
                    DataSet activity = this.getQueryProcessor().getPreparedSqlDataSet(union.substring(7) + " ORDER BY activityby, activitydt DESC", safeSQL.getValues());
                    commandResponse.set("activity", activity);
                }
            }
            commandResponse.set("contributors", contributors);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load contributors for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private void loadFields(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            boolean groupbyitem = commandRequest.getBoolean("groupbyitem");
            boolean sortbyfieldid = commandRequest.getBoolean("sortbyfieldid", true);
            DataSet fields = groupbyitem ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemdesc, worksheetitemfield.worksheetitemid, worksheetitemfield.worksheetitemversionid, fieldname, fieldinstance, fieldtitle, enteredtext, displayvalue, fieldcontext FROM worksheetsection, worksheetitem, worksheetitemfield WHERE worksheetsection.worksheetsectionid = worksheetitem.worksheetsectionid AND worksheetsection.worksheetsectionversionid = worksheetitem.worksheetsectionversionid   AND worksheetitemfield.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemfield.worksheetitemversionid = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ORDER BY worksheetsection.usersequence, worksheetitem.usersequence, worksheetitemfield.usersequence", new Object[]{worksheetid, worksheetversionid}) : this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemdesc, worksheetitemfield.worksheetitemid, worksheetitemfield.worksheetitemversionid, fieldname, fieldinstance, fieldtitle, enteredtext, displayvalue, fieldcontext FROM worksheetitemfield, worksheetitem WHERE worksheetitemfield.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemfield.worksheetitemversionid = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ORDER BY " + (sortbyfieldid ? "" : "displayvalue, ") + "fieldname, worksheetitemfield.worksheetitemid, fieldinstance", new Object[]{worksheetid, worksheetversionid});
            commandResponse.set("fields", fields);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load fields for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private void loadAttachments(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            boolean groupbyitem = commandRequest.getBoolean("groupbyitem");
            DataSet attachments = this.getQueryProcessor().getPreparedSqlDataSet("SELECT 'W' \"type\", worksheetid \"id\", worksheetversionid \"version\", sdiattachment.sdcid, sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum, sdiattachment.usersequence,        sdiattachment.attachmentdesc, worksheet.worksheetname, -1 \"wssseq\", 0 \"wsiseq\" FROM sdiattachment, worksheet WHERE sdiattachment.sdcid = 'LV_Worksheet'   AND sdiattachment.keyid1 = worksheet.worksheetid AND sdiattachment.keyid2 = worksheet.worksheetversionid  AND worksheet.worksheetid = ? AND worksheet.worksheetversionid = ? UNION SELECT 'S' \"type\", worksheetsection.worksheetid \"id\", worksheetsection.worksheetversionid \"version\", sdiattachment.sdcid, sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum, sdiattachment.usersequence,        sdiattachment.attachmentdesc, worksheetsection.worksheetsectiondesc, worksheetsection.usersequence \"wssseq\", -1 \"wsiseq\" FROM sdiattachment, worksheetsection WHERE sdiattachment.sdcid = 'LV_WorksheetSection'   AND sdiattachment.keyid1 = worksheetsection.worksheetsectionid AND sdiattachment.keyid2 = worksheetsection.worksheetsectionversionid  AND worksheetsection.worksheetid = ? AND worksheetsection.worksheetversionid = ? UNION SELECT 'I' \"type\", worksheetitem.worksheetid \"id\", worksheetitem.worksheetversionid \"version\", sdiattachment.sdcid, sdiattachment.keyid1, sdiattachment.keyid2, sdiattachment.keyid3, sdiattachment.attachmentnum, sdiattachment.usersequence,        sdiattachment.attachmentdesc, worksheetitem.worksheetitemdesc, worksheetsection.usersequence \"wssseq\", worksheetitem.usersequence \"wsiseq\" FROM sdiattachment, worksheetitem, worksheetsection WHERE sdiattachment.sdcid = 'LV_WorksheetItem'   AND sdiattachment.keyid1 = worksheetitem.worksheetitemid AND sdiattachment.keyid2 = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetsectionid = worksheetsection.worksheetsectionid AND worksheetitem.worksheetsectionversionid = worksheetsection.worksheetsectionversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? AND ( attachmentuse IS NULL OR attachmentuse <> 'HTMLEditor' ) ORDER BY \"wssseq\", \"wsiseq\", usersequence, attachmentnum ", new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid, worksheetid, worksheetversionid});
            DataSet fields = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemfield.worksheetitemid, worksheetitemfield.worksheetitemversionid, fieldname, fieldinstance, fieldtitle, enteredtext, displayvalue, fieldcontext FROM worksheetitem, worksheetitemfield WHERE worksheetitemfield.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemfield.worksheetitemversionid = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?  AND worksheetitem.propertytreeid='AttachmentControl'", new Object[]{worksheetid, worksheetversionid});
            for (int i = 0; i < fields.size(); ++i) {
                String[] parts;
                String context = fields.getValue(i, "fieldcontext");
                if (context.length() <= 0 || (parts = StringUtil.split(context, ";")).length <= 5) continue;
                fields.setString(i, "att_sdcid", parts[2]);
                fields.setString(i, "att_keyid1", parts[3]);
                fields.setString(i, "att_keyid2", parts[4]);
                fields.setString(i, "att_keyid3", parts[5]);
                fields.setString(i, "att_attachmentnum", parts[6]);
            }
            if (fields.size() > 0) {
                HashMap<String, String> find = new HashMap<String, String>();
                for (int i = 0; i < attachments.size(); ++i) {
                    String attDesc;
                    find.clear();
                    find.put("att_sdcid", attachments.getString(i, "sdcid"));
                    find.put("att_keyid1", attachments.getString(i, "keyid1", "(null)"));
                    find.put("att_keyid2", attachments.getString(i, "keyid2", "(null)"));
                    find.put("att_keyid3", attachments.getString(i, "keyid3", "(null)"));
                    find.put("att_attachmentnum", attachments.getValue(i, "attachmentnum"));
                    int row = fields.findRow(find);
                    if (row < 0) continue;
                    String fieldDesc = fields.getValue(row, "displayvalue");
                    if (fieldDesc.contains(attDesc = attachments.getValue(i, "attachmentdesc"))) {
                        attachments.setString(i, "attachmentdesc", fieldDesc);
                        continue;
                    }
                    attachments.setString(i, "attachmentdesc", fieldDesc + " - " + attDesc);
                }
            }
            if (!groupbyitem) {
                attachments.sort("attachmentdesc");
            }
            commandResponse.set("attachments", attachments);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load attachments for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private void resequenceAttachmentFields(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            String fieldPrefix;
            boolean generateFields;
            String mode;
            PropertyList config;
            WorksheetItemFields itemFields;
            String key;
            String worksheetitemversionid;
            String worksheetitemid;
            int i;
            ConfigurationProcessor configProcessor = new ConfigurationProcessor(this.sapphireConnection.getConnectionId());
            PropertyList policy = configProcessor.getPolicy("ELNPolicy", "Sapphire Custom");
            DataSet worksheetitems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitem.*  FROM worksheetitem, worksheetsection WHERE worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?  AND worksheetitem.worksheetsectionid = worksheetsection.worksheetsectionid AND worksheetitem.worksheetsectionversionid = worksheetsection.worksheetsectionversionid AND worksheetitem.propertytreeid='AttachmentControl'  ORDER BY worksheetsection.usersequence,worksheetitem.usersequence", new Object[]{worksheetid, worksheetversionid}, true);
            HashMap<String, Integer> counters = new HashMap<String, Integer>();
            HashMap<String, Attachment> allAttachmentControls = new HashMap<String, Attachment>();
            HashMap<String, WorksheetItemFields> allItemFields = new HashMap<String, WorksheetItemFields>();
            for (i = 0; i < worksheetitems.size(); ++i) {
                worksheetitemid = worksheetitems.getString(i, "worksheetitemid");
                worksheetitemversionid = worksheetitems.getString(i, "worksheetitemversionid");
                key = worksheetitemid + ";" + worksheetitemversionid;
                Attachment attachmentControl = (Attachment)WorksheetItemFactory.getInstance(this.sapphireConnection, database, (HashMap)worksheetitems.get(i));
                allAttachmentControls.put(key, attachmentControl);
                itemFields = this.getWorksheetItemFields(worksheetid, worksheetversionid, worksheetitemid, worksheetitemversionid);
                allItemFields.put(key, itemFields);
                config = attachmentControl.getConfig();
                mode = config.getProperty("attachmentmode", "standard");
                generateFields = config.getProperty("generatefields").equals("Y");
                fieldPrefix = config.getProperty("fieldprefix", "Figure");
                if (!mode.equals("worksheet") || !generateFields || !attachmentControl.getStatus().equals("Complete")) continue;
                Iterator<String> iterator = itemFields.iterator();
                while (iterator.hasNext()) {
                    String fieldid = iterator.next();
                    String context = itemFields.getField(fieldid).getFieldContext();
                    if (!context.contains(";")) continue;
                    String[] parts = StringUtil.split(context, ";");
                    try {
                        int sequence = Integer.parseInt(parts[1].trim());
                        if (counters.containsKey(fieldPrefix) && sequence <= (Integer)counters.get(fieldPrefix)) continue;
                        counters.put(fieldPrefix, sequence);
                    }
                    catch (NumberFormatException sequence) {}
                }
            }
            for (i = 0; i < worksheetitems.size(); ++i) {
                worksheetitemid = worksheetitems.getString(i, "worksheetitemid");
                worksheetitemversionid = worksheetitems.getString(i, "worksheetitemversionid");
                key = worksheetitemid + ";" + worksheetitemversionid;
                Attachment attachment = (Attachment)allAttachmentControls.get(key);
                itemFields = (WorksheetItemFields)allItemFields.get(key);
                config = attachment.getConfig();
                mode = config.getProperty("attachmentmode", "standard");
                generateFields = config.getProperty("generatefields").equals("Y");
                if (mode.equals("worksheet") && generateFields && !attachment.getStatus().equals("Complete")) {
                    String id;
                    String sourcesdcid;
                    fieldPrefix = config.getProperty("fieldprefix", "Figure");
                    boolean addDescriptionToFieldValue = config.getProperty("adddescriptiontofield", "Y").equals("Y");
                    String worksheetsource = config.getProperty("worksheetmodesource", "Control");
                    String worksheetsectionid = worksheetitems.getString(i, "worksheetsectionid");
                    String worksheetsectionversionid = worksheetitems.getString(i, "worksheetsectionversionid");
                    String string = worksheetsource.equalsIgnoreCase("worksheet") ? "LV_Worksheet" : (sourcesdcid = worksheetsource.equalsIgnoreCase("section") ? "LV_WorksheetSection" : "LV_WorksheetItem");
                    String string2 = sourcesdcid.equals("LV_Worksheet") ? worksheetid : (id = sourcesdcid.equals("LV_WorksheetSection") ? worksheetsectionid : worksheetitemid);
                    String version = sourcesdcid.equals("LV_Worksheet") ? worksheetversionid : (sourcesdcid.equals("LV_WorksheetSection") ? worksheetsectionversionid : worksheetitemversionid);
                    DataSet attachments = policy != null && policy.getPropertyListNotNull("attachments").getProperty("attachmentclass", "").length() > 0 ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdiattachment.* FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = '(null)' AND attachmentclass = ? ORDER BY usersequence, attachmentnum", new Object[]{sourcesdcid, id, version, policy.getPropertyListNotNull("attachments").getProperty("attachmentclass")}) : this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdiattachment.* FROM sdiattachment WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = '(null)' ORDER BY usersequence, attachmentnum", new Object[]{sourcesdcid, id, version});
                    Attachment.generateFields(fieldPrefix, attachments, itemFields, addDescriptionToFieldValue, counters, true);
                    continue;
                }
                itemFields.deleteAll();
                itemFields.save();
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load attachments for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    protected WorksheetItemFields getWorksheetItemFields(String worksheetid, String worksheetversionid, String worksheetitemid, String worksheetitemversionid) throws SapphireException {
        M18NUtil m18n = new M18NUtil(this.sapphireConnection);
        FormatUtil formatUtil = FormatUtil.getInstance(this.sapphireConnection);
        WorksheetItemFields worksheetItemFields = new WorksheetItemFields(m18n, formatUtil, this.getQueryProcessor(), this.getActionProcessor(), worksheetid, worksheetversionid, worksheetitemid, worksheetitemversionid);
        DataSet fields = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitemfield WHERE worksheetitemid = ? AND worksheetitemversionid = ? ORDER BY usersequence, fieldname, fieldinstance", new Object[]{worksheetitemid, worksheetitemversionid});
        for (int i = 0; i < fields.size(); ++i) {
            String fieldid = fields.getValue(i, "fieldname");
            WorksheetItemFieldsField field = worksheetItemFields.getField(fieldid);
            if (field == null) {
                field = worksheetItemFields.createField(fieldid);
                field.loaded = true;
                field.fieldtitle = fields.getValue(i, "fieldtitle");
                field.datatype = fields.getValue(i, "datatype");
                field.sequence = fields.getInt(i, "usersequence");
                field.fieldContext = fields.getValue(i, "fieldcontext");
                field.fielddef = new PropertyList();
                field.fielddef.setPropertyList(fields.getClob(i, "fielddef", ""));
                worksheetItemFields.addField(field);
            }
            field.setInstance(fields.getInt(i, "fieldinstance"), fields.getValue(i, "enteredtext"), fields.getBigDecimal(i, "numericvalue"), fields.getCalendar(i, "datevalue"), fields.getValue(i, "displayvalue"));
        }
        return worksheetItemFields;
    }

    private void loadAttributeDefs(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet attributeDefs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT attributedefid attributeid, coalesce(attributetitle, attributedefid) attributetitle, basedonid FROM attributedef WHERE basedonid in ( ?,?,?) order by basedonid, attributetitle", (Object[])new String[]{"LV_Worksheet", "LV_WorksheetSection", "LV_WorksheetItem"});
            commandResponse.set("attributedefs", attributeDefs);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load attribute defs. Reason: " + e.getMessage(), e);
        }
    }

    private void loadAttributes(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            DataSet attributes = ELNRequest.loadAllWorksheetAttributes(this.getQueryProcessor(), new M18NUtil(this.sapphireConnection), worksheetid, worksheetversionid, commandRequest.getBoolean("groupbyitem"));
            commandResponse.set("attributes", attributes);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load attributes for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    public static DataSet loadAllWorksheetAttributes(QueryProcessor qp, M18NUtil m18NUtil, String worksheetid, String worksheetversionid, boolean groupbyitem) {
        DataSet attributes = qp.getPreparedSqlDataSet("SELECT 'W' \"type\", -1, -1, worksheetid \"id\", worksheetversionid \"version\", sdiattribute.attributeid, sdiattribute.attributeinstance, sdiattribute.attributesdcid, sdiattribute.editorstyleid, sdiattribute.datatype, sdiattribute.textvalue, sdiattribute.numericvalue, sdiattribute.datevalue, sdiattribute.defaulttextvalue, sdiattribute.defaultnumericvalue, sdiattribute.defaultdatevalue, sdiattribute.mandatoryflag, sdiattribute.updateableflag, attributedef.attributetitle FROM sdiattribute, attributedef, worksheet WHERE sdiattribute.sdcid = attributedef.basedonid AND sdiattribute.attributeid = attributedef.attributedefid AND sdiattribute.sdcid = 'LV_Worksheet'   AND sdiattribute.keyid1 = worksheet.worksheetid AND sdiattribute.keyid2 = worksheet.worksheetversionid   AND worksheet.worksheetid = ? AND worksheet.worksheetversionid = ? UNION SELECT 'S' \"type\", worksheetsection.usersequence, 0, worksheetsectionid \"id\", worksheetsectionversionid \"version\", sdiattribute.attributeid, sdiattribute.attributeinstance, sdiattribute.attributesdcid, sdiattribute.editorstyleid, sdiattribute.datatype, sdiattribute.textvalue, sdiattribute.numericvalue, sdiattribute.datevalue, sdiattribute.defaulttextvalue, sdiattribute.defaultnumericvalue, sdiattribute.defaultdatevalue, sdiattribute.mandatoryflag, sdiattribute.updateableflag, attributedef.attributetitle FROM sdiattribute, attributedef, worksheetsection WHERE sdiattribute.sdcid = attributedef.basedonid AND sdiattribute.attributeid = attributedef.attributedefid AND sdiattribute.sdcid = 'LV_WorksheetSection'   AND sdiattribute.keyid1 = worksheetsection.worksheetsectionid AND sdiattribute.keyid2 = worksheetsection.worksheetsectionversionid   AND worksheetsection.worksheetid = ? AND worksheetsection.worksheetversionid = ? UNION SELECT 'I' \"type\", worksheetsection.usersequence, worksheetitem.usersequence, worksheetitemid \"id\", worksheetitemversionid \"version\", sdiattribute.attributeid, sdiattribute.attributeinstance, sdiattribute.attributesdcid, sdiattribute.editorstyleid, sdiattribute.datatype, sdiattribute.textvalue, sdiattribute.numericvalue, sdiattribute.datevalue, sdiattribute.defaulttextvalue, sdiattribute.defaultnumericvalue, sdiattribute.defaultdatevalue, sdiattribute.mandatoryflag, sdiattribute.updateableflag, attributedef.attributetitle FROM sdiattribute, attributedef, worksheetitem, worksheetsection WHERE sdiattribute.sdcid = attributedef.basedonid AND sdiattribute.attributeid = attributedef.attributedefid AND sdiattribute.sdcid = 'LV_WorksheetItem'   AND sdiattribute.keyid1 = worksheetitem.worksheetitemid AND sdiattribute.keyid2 = worksheetitem.worksheetitemversionid   AND worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ?   AND worksheetitem.worksheetsectionid = worksheetsection.worksheetsectionid   AND worksheetitem.worksheetsectionversionid = worksheetsection.worksheetsectionversionid ORDER BY " + (groupbyitem ? "2, 3, 1 DESC, attributeid" : "attributeid, 2, 3"), new Object[]{worksheetid, worksheetversionid, worksheetid, worksheetversionid, worksheetid, worksheetversionid}, true);
        ELNRequest.formatAttributes(m18NUtil, attributes);
        return attributes;
    }

    private void addWorksheet(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("authorid", this.sapphireConnection.getSysuserId());
            actionProps.setProperty("worksheetstatus", "InProgress");
            DataSet attributes = commandRequest.getDataSet("attributes");
            if (actionProps.getProperty("worksheetname").equals("(Auto)")) {
                DataSet template = this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{actionProps.getProperty("templateid"), actionProps.getProperty("templateversionid")}, true);
                PropertyList options = new PropertyList();
                options.setPropertyList(template.getClob(0, "options", ""));
                actionProps.setProperty("worksheetname", BaseELNAction.resolveWorksheetName(this.sapphireConnection, this.getSequenceProcessor(), options.getProperty("worksheetnametemplate"), actionProps, attributes));
            }
            this.getActionProcessor().processActionClass(AddWorksheet.class.getName(), actionProps);
            String worksheetid = actionProps.getProperty("worksheetid");
            String worksheetversionid = actionProps.getProperty("worksheetversionid");
            if (attributes != null && attributes.size() > 0) {
                this.saveSDIAttributes(worksheetid, worksheetversionid, "", "", "", "", attributes, false);
            }
            if (commandRequest.getBoolean("loadworksheet")) {
                commandRequest.set("worksheetid", worksheetid);
                commandRequest.set("worksheetversionid", worksheetversionid);
                Worksheet worksheet = new Worksheet(this.sapphireConnection);
                worksheet.open(worksheetid, worksheetversionid, commandRequest, commandResponse);
            } else {
                commandResponse.set("worksheetid", worksheetid);
                commandResponse.set("worksheetversionid", worksheetversionid);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to create worksheet. Reason: " + e.getMessage(), e);
        }
    }

    private void addWorksheetVersion(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String templateid = commandRequest.getString("templateid");
            PropertyList actionProps = commandRequest.getStringPropertyList();
            database.createPreparedResultSet("SELECT " + (this.connectionInfo.isOracle() ? " nvl( max( to_number( worksheetversionid ) ), 0 )" : " isnull( max( cast( worksheetversionid AS Integer ) ), 0 )") + " as version FROM worksheet WHERE worksheetid = ?", new Object[]{templateid});
            database.getNext();
            actionProps.setProperty("worksheetversionid", String.valueOf(database.getInt("version") + 1));
            actionProps.setProperty("authorid", this.sapphireConnection.getSysuserId());
            actionProps.setProperty("worksheetstatus", "InProgress");
            actionProps.setProperty("newtemplateid", templateid);
            actionProps.setProperty("newversion", "Y");
            this.getActionProcessor().processActionClass(AddWorksheet.class.getName(), actionProps);
            String worksheetid = actionProps.getProperty("worksheetid");
            String worksheetversionid = actionProps.getProperty("worksheetversionid");
            if (commandRequest.getBoolean("loadworksheet")) {
                commandRequest.set("worksheetid", worksheetid);
                commandRequest.set("worksheetversionid", worksheetversionid);
                Worksheet worksheet = new Worksheet(this.sapphireConnection);
                worksheet.open(worksheetid, worksheetversionid, commandRequest, commandResponse);
            } else {
                commandResponse.set("worksheetid", worksheetid);
                commandResponse.set("worksheetversionid", worksheetversionid);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to create worksheet version. Reason: " + e.getMessage(), e);
        }
    }

    private void approveWorksheetVersion(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_Worksheet");
            props.setProperty("keyid1", worksheetid);
            props.setProperty("keyid2", worksheetversionid);
            props.setProperty("versionstatus", "C");
            props.setProperty("worksheet_action", "Y");
            this.getActionProcessor().processAction("SetSDIVersionStatus", "1", props);
            database.executePreparedUpdate("UPDATE worksheet SET versionstatus = 'A' WHERE worksheetid = ? AND versionstatus = 'C'", new Object[]{worksheetid});
            database.executePreparedUpdate("UPDATE worksheet SET versionstatus = 'C' WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
            commandResponse.set("worksheetid", worksheetid);
            commandResponse.set("worksheetversionid", worksheetversionid);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to approve worksheet version. Reason: " + e.getMessage(), e);
        }
    }

    private void deleteWorksheetSDI(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            SDIList sdiList = commandRequest.getSDIList("sdilist");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            if (commandRequest.getBoolean("deleteunassigned")) {
                actionProps.setProperty("deleteunassigned", "Y");
                actionProps.setProperty("sdcid", commandRequest.getString("sdcid"));
            } else {
                actionProps.setProperty("sdcid", sdiList.getSdcid());
                actionProps.setProperty("keyid1", sdiList.getKeyid1());
                actionProps.setProperty("keyid2", sdiList.getKeyid2());
                actionProps.setProperty("keyid3", sdiList.getKeyid3());
            }
            this.getActionProcessor().processActionClass(DeleteWorksheetSDI.class.getName(), actionProps);
            commandResponse.set("worksheetitemid", actionProps.getProperty("worksheetitemid"));
            commandResponse.set("worksheetitemversionid", actionProps.getProperty("worksheetitemversionid"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to delete worksheet SDIs. Reason: " + e.getMessage(), e);
        }
    }

    private void worksheetNameCheck(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetname = commandRequest.getString("worksheetname");
            String workbookid = commandRequest.getString("workbookid");
            String workbookversionid = commandRequest.getString("workbookversionid");
            String templateid = commandRequest.getString("templateid");
            String templatetype = commandRequest.getString("templatetype");
            String templateprivacyflag = commandRequest.getString("templateprivacyflag");
            String wstemplateid = commandRequest.getString("sourcetemplateid");
            String wstemplateversionid = commandRequest.getString("sourcetemplateversionid");
            if (worksheetname.equals("(Auto)")) {
                commandResponse.set("exists", "N");
            } else if (templatetype.length() == 0) {
                PropertyList policy;
                boolean globalUniqueness;
                String policyNode = "Sapphire Custom";
                if (wstemplateid.length() > 0) {
                    policyNode = Worksheet.getPolicyNode(this.getQueryProcessor(), wstemplateid, wstemplateversionid);
                }
                if (globalUniqueness = "Global".equalsIgnoreCase((policy = this.getConfigurationProcessor().getPolicy("ELNPolicy", policyNode)).getProperty("worksheetnameuniqueness", "Global"))) {
                    database.createPreparedResultSet("SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND ( templateflag = 'N' OR templateflag IS NULL )", new Object[]{worksheetname});
                } else {
                    database.createPreparedResultSet("SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND workbookid = ? and workbookversionid = ? AND ( templateflag = 'N' OR templateflag IS NULL )", new Object[]{worksheetname, workbookid, workbookversionid});
                }
                commandResponse.set("exists", database.getNext() ? "Y" : "N");
                commandResponse.set("existstype", "name");
            } else if (templateprivacyflag.equals("O")) {
                database.createPreparedResultSet("SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND templateflag = 'Y' AND templatetypeflag = ? AND templateprivacyflag = 'O' AND authorid = ?", new Object[]{worksheetname, templatetype, this.sapphireConnection.getSysuserId()});
                commandResponse.set("exists", database.getNext() ? "Y" : "N");
                commandResponse.set("existstype", "name");
            } else if (BaseSDIAction.isValidKey(templateid)) {
                database.createPreparedResultSet("SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetid = ?", new Object[]{templateid + "_" + (Configuration.isDevmode(this.connectionInfo.getDatabaseId()) ? "LV" : "") + templatetype});
                if (database.getNext()) {
                    commandResponse.set("exists", "Y");
                    commandResponse.set("existstype", "id");
                } else {
                    database.createPreparedResultSet("SELECT worksheetid, worksheetversionid, worksheetname FROM worksheet WHERE worksheetname = ? AND templateflag = 'Y' AND templatetypeflag = ? AND ( templateprivacyflag = 'G' OR templateprivacyflag IS NULL )", new Object[]{worksheetname, templatetype});
                    commandResponse.set("exists", database.getNext() ? "Y" : "N");
                    commandResponse.set("existstype", "name");
                }
            } else {
                commandResponse.set("exists", "Y");
                commandResponse.set("existstype", "id");
                commandResponse.set("invalid", "Y");
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to check worksheet name. Reason: " + e.getMessage(), e);
        }
    }

    private void editWorksheet(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList editProps = commandRequest.getStringPropertyList();
            editProps.setProperty("elnrequest", "Y");
            this.getActionProcessor().processActionClass(EditWorksheet.class.getName(), editProps);
            commandResponse.set("worksheetdesc", commandRequest.getString("worksheetdesc"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to edit worksheet. Reason: " + e.getMessage(), e);
        }
    }

    private void setWorksheetStatus(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            if (commandRequest.getString("status").equals("Reject") || commandRequest.getString("status").equals("Approve") || commandRequest.getString("status").equals("Complete")) {
                DataSet notes = BaseELNAction.getWorksheetNotes(this.getQueryProcessor(), commandRequest.getString("worksheetid"), commandRequest.getString("worksheetversionid"), "followupflag = 'Y' AND resolvedflag = 'N'");
                if (commandRequest.getString("status").equals("Reject")) {
                    if (notes.size() == 0) {
                        commandResponse.set("nofollowups", "Y");
                        return;
                    }
                } else if (notes.size() > 0) {
                    commandResponse.set("unresolvedfollowups", "Y");
                    return;
                }
            }
            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("htmlerror", "Y");
            this.getActionProcessor().processActionClass(SetWorksheetStatus.class.getName(), actionProps);
        }
        catch (SapphireException e) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to set worksheet status. Reason: " + e.getMessage(), e);
        }
    }

    private void loadTemplateDetails(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String templateid = commandRequest.getString("templateid");
        String templateversionid = commandRequest.getString("templateversionid");
        try {
            if (templateversionid.length() == 0 || templateversionid.equalsIgnoreCase("C")) {
                templateversionid = BaseELNAction.resolveVersion(this.getQueryProcessor(), templateid, templateversionid, "worksheet");
            }
            DataSet template = this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{templateid, templateversionid}, true);
            PropertyList options = new PropertyList();
            options.setPropertyList(template.getClob(0, "options", ""));
            commandResponse.set("options", options);
            DataSet attributes = this.loadAttributes("LV_Worksheet", templateid, templateversionid);
            commandResponse.set("attributes", attributes);
            commandResponse.set("editorstyles", this.getEditorStyles(attributes.getColumnValues("editorstyleid", ";")));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load details for template '" + BaseELNAction.getIdVersionText(templateid, templateversionid) + "'. Reason: " + e.getMessage(), e);
        }
    }

    private void saveAsTemplate(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("workbookid", "(null)");
            actionProps.setProperty("workbookversionid", "(null)");
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetsectionid", commandRequest.getString("worksheetsectionid"));
            actionProps.setProperty("worksheetsectionversionid", commandRequest.getString("worksheetsectionversionid"));
            actionProps.setProperty("worksheetitemid", commandRequest.getString("worksheetitemid"));
            actionProps.setProperty("worksheetitemversionid", commandRequest.getString("worksheetitemversionid"));
            actionProps.setProperty("newtemplateid", commandRequest.getString("newtemplateid"));
            actionProps.setProperty("worksheetname", commandRequest.getString("worksheetname"));
            actionProps.setProperty("templateflag", "Y");
            actionProps.setProperty("templatetypeflag", commandRequest.getString("templatetypeflag"));
            actionProps.setProperty("templateprivacyflag", commandRequest.getString("templateprivacyflag", "O"));
            actionProps.setProperty("newtemplatemode", commandRequest.getString("newtemplatemode"));
            PropertyList options = commandRequest.getPropertyList("options");
            if (options != null) {
                actionProps.putAll(options);
            }
            this.getActionProcessor().processActionClass(CopyWorksheet.class.getName(), actionProps);
            commandResponse.set("worksheetid", actionProps.getProperty("worksheetid"));
            commandResponse.set("worksheetversionid", actionProps.getProperty("worksheetversionid"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to save worksheet. Reason: " + e.getMessage(), e);
        }
    }

    private void addSection(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("sectiondesc", commandRequest.getString("worksheetsectiondesc"));
            actionProps.setProperty("sectionlevel", commandRequest.getString("sectionlevel"));
            actionProps.setProperty("templateid", commandRequest.getString("templateid"));
            actionProps.setProperty("templateversionid", commandRequest.getString("templateversionid"));
            actionProps.setProperty("sectiontemplateid", commandRequest.getString("sectiontemplateid"));
            actionProps.setProperty("sectiontemplateversionid", commandRequest.getString("sectiontemplateversionid"));
            actionProps.setProperty("fromworksheetid", commandRequest.getString("fromworksheetid"));
            actionProps.setProperty("fromworksheetversionid", commandRequest.getString("fromworksheetversionid"));
            String beforesectionid = commandRequest.getString("beforesectionid");
            int usersequence = -1;
            if (beforesectionid.length() > 0) {
                usersequence = this.getQueryProcessor().getPreparedCount("SELECT usersequence FROM worksheetsection WHERE worksheetid=? AND worksheetsectionid=?", new String[]{commandRequest.getString("worksheetid"), beforesectionid});
            }
            actionProps.setProperty("usersequence", "" + usersequence);
            String aftersectionid = commandRequest.getString("aftersectionid");
            int afterusersequence = -1;
            if (aftersectionid.length() > 0) {
                afterusersequence = this.getQueryProcessor().getPreparedCount("SELECT usersequence FROM worksheetsection WHERE worksheetid=? AND worksheetsectionid=?", new String[]{commandRequest.getString("worksheetid"), aftersectionid});
            }
            actionProps.setProperty("usersequence", "" + usersequence);
            actionProps.setProperty("afterusersequence", "" + afterusersequence);
            this.getActionProcessor().processActionClass(AddWorksheetSection.class.getName(), actionProps);
            String[] sectionid = StringUtil.split(actionProps.getProperty("worksheetsectionid"), ";");
            String[] sectionversion = StringUtil.split(actionProps.getProperty("worksheetsectionversionid"), ";");
            StringBuffer where = new StringBuffer();
            SafeSQL safeSQL = new SafeSQL();
            for (int i = 0; i < sectionid.length; ++i) {
                where.append(" OR ").append("(worksheetsectionid=").append(safeSQL.addVar(sectionid[i])).append(" AND worksheetsectionversionid=").append(safeSQL.addVar(sectionversion[i])).append(")");
            }
            DataSet sections = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetsection WHERE " + where.substring(4) + " ORDER BY usersequence", safeSQL.getValues(), true);
            for (int i = 0; i < sections.size(); ++i) {
                PropertyList options = new PropertyList(sections.getValue(i, "options"));
                sections.setValue(i, "options", options.toJSONString());
                sections.setString(i, "option_itemcompletion", options.getProperty("itemcompletion", "N"));
            }
            commandResponse.set("worksheetsections", sections);
            if (commandRequest.getString("templateid").length() > 0 || commandRequest.getString("sectiontemplateid").length() > 0) {
                DataSet sectionitems = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitem WHERE " + where.substring(4) + " ORDER BY usersequence", safeSQL.getValues(), true);
                for (int i = 0; i < sectionitems.size(); ++i) {
                    WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(this.sapphireConnection, (HashMap)sectionitems.get(i), commandRequest.getString("width"));
                    sectionitems.setString(i, "elementid", worksheetItem.getElementId());
                    sectionitems.addColumn("itemtype", 0);
                    sectionitems.setValue(i, "itemtype", worksheetItem.getClientRenderer());
                    sectionitems.setValue(i, "html", worksheetItem.getViewHTML());
                    sectionitems.setString(i, "availabilitystatus", worksheetItem.getAvailability());
                    PropertyList options = worksheetItem.getWorksheetItemOptions().toPropertyList();
                    sectionitems.setValue(i, "options", options.toJSONString());
                    sectionitems.setString(i, "option_itemcompletion", options.getProperty("itemcompletion", "N"));
                }
                if (commandRequest.getString("templateid").length() > 0) {
                    ELNRequest.updateMRUList(new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.sapphireConnection.getConnectionId()), this.sapphireConnection.getSysuserId(), commandRequest.getString("__hostwebpageid") + "_recent_section_templates", commandRequest.getString("templateid") + ";" + commandRequest.getString("templateversionid"));
                } else if (commandRequest.getString("sectiontemplateid").length() > 0) {
                    ELNRequest.updateMRUList(new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.sapphireConnection.getConnectionId()), this.sapphireConnection.getSysuserId(), commandRequest.getString("__hostwebpageid") + "_recent_copy_worksheets", commandRequest.getString("fromworksheetid") + ";" + commandRequest.getString("fromworksheetversionid"));
                }
                commandResponse.set("worksheetitems", sectionitems);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to create worksheet section. Reason: " + e.getMessage(), e);
        }
    }

    private void editSection(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            this.getActionProcessor().processActionClass(EditWorksheetSection.class.getName(), commandRequest.getStringPropertyList());
            commandResponse.set("worksheetsectiondesc", commandRequest.getString("worksheetsectiondesc"));
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to edit worksheet section. Reason: " + e.getMessage(), e);
        }
    }

    private void moveSection(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            int currseq;
            DataSet sections = commandRequest.getDataSet("worksheetsections");
            int sectionrow = Integer.parseInt(commandRequest.getString("sectionrow"));
            String worksheetid = sections.getValue(sectionrow, "worksheetid");
            String worksheetversionid = sections.getValue(sectionrow, "worksheetversionid");
            String worksheetsectionid = sections.getValue(sectionrow, "worksheetsectionid");
            String worksheetsectionversionid = sections.getValue(sectionrow, "worksheetsectionversionid");
            boolean up = commandRequest.getBoolean("up");
            int newseq = 0;
            if (up && sectionrow > 1) {
                currseq = Integer.parseInt(sections.getValue(sectionrow, "usersequence"));
                newseq = Integer.parseInt(sections.getValue(sectionrow - 1, "usersequence"));
                database.executePreparedUpdate("UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{newseq, worksheetsectionid, worksheetsectionversionid});
                database.executePreparedUpdate("UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{currseq, sections.getValue(sectionrow - 1, "worksheetsectionid"), sections.getValue(sectionrow - 1, "worksheetsectionversionid")});
                sections.setValue(sectionrow - 1, "usersequence", String.valueOf(currseq));
                sections.setValue(sectionrow, "usersequence", String.valueOf(newseq));
                sections.add(sectionrow - 1, sections.remove(sectionrow));
                if (currseq == newseq) {
                    this.checkSectionUserSequence(database, sections);
                }
            } else if (!up && sectionrow < sections.size() - 1) {
                currseq = Integer.parseInt(sections.getValue(sectionrow, "usersequence"));
                newseq = Integer.parseInt(sections.getValue(sectionrow + 1, "usersequence"));
                database.executePreparedUpdate("UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{newseq, worksheetsectionid, worksheetsectionversionid});
                database.executePreparedUpdate("UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{currseq, sections.getValue(sectionrow + 1, "worksheetsectionid"), sections.getValue(sectionrow + 1, "worksheetsectionversionid")});
                sections.setValue(sectionrow + 1, "usersequence", String.valueOf(currseq));
                sections.setValue(sectionrow, "usersequence", String.valueOf(newseq));
                sections.add(sectionrow + 1, sections.remove(sectionrow));
                if (currseq == newseq) {
                    this.checkSectionUserSequence(database, sections);
                }
            }
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", "LV_WorksheetSection");
            activityProps.setProperty("targetkeyid1", worksheetsectionid);
            activityProps.setProperty("targetkeyid2", worksheetsectionversionid);
            activityProps.setProperty("activitytype", "Edit");
            activityProps.setProperty("activitylog", "Moved section " + (up ? "up" : "down") + " to sequence " + newseq);
            this.getActionProcessor().processActionClass(AddWorksheetActivity.class.getName(), activityProps);
            commandResponse.set("worksheetsections", sections);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to move worksheet section. Reason: " + e.getMessage(), e);
        }
    }

    private boolean checkSectionUserSequence(DBUtil database, DataSet sections) throws SapphireException {
        boolean sequencesChanged = false;
        int priorRow = Integer.parseInt(sections.getValue(0, "usersequence", "0"));
        for (int i = 1; i < sections.size(); ++i) {
            int seq = Integer.parseInt(sections.getValue(i, "usersequence", "0"));
            if (seq <= priorRow) {
                seq = priorRow + 1;
                sections.setValue(i, "usersequence", String.valueOf(seq));
                database.executePreparedUpdate("UPDATE worksheetsection SET usersequence = ? WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{seq, sections.getValue(i, "worksheetsectionid"), sections.getValue(i, "worksheetsectionversionid")});
                sequencesChanged = true;
            }
            priorRow = seq;
        }
        return sequencesChanged;
    }

    private void setSectionStatus(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            if (commandRequest.getString("status").equals("Reject") || commandRequest.getString("status").equals("Approve") || commandRequest.getString("status").equals("Complete")) {
                DataSet notes = BaseELNAction.getWorksheetSectionNotes(this.getQueryProcessor(), database, commandRequest.getString("worksheetid"), commandRequest.getString("worksheetversionid"), commandRequest.getString("worksheetsectionid"), commandRequest.getString("worksheetsectionversionid"), "followupflag = 'Y' AND resolvedflag = 'N'", true);
                if (commandRequest.getString("status").equals("Reject")) {
                    if (notes.size() == 0) {
                        commandResponse.set("nofollowups", "Y");
                        return;
                    }
                } else if (notes.size() > 0) {
                    commandResponse.set("unresolvedfollowups", "Y");
                    return;
                }
            }
            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("htmlerror", "Y");
            this.getActionProcessor().processActionClass(SetWorksheetSectionStatus.class.getName(), actionProps);
            commandResponse.set("setstatusworksheetitemid", actionProps.getProperty("setstatusworksheetitemid"));
            commandResponse.set("setstatusworksheetitemversionid", actionProps.getProperty("setstatusworksheetitemversionid"));
            commandResponse.set("setstatusvalue", actionProps.getProperty("setstatusvalue"));
            commandResponse.set("availableworksheetitemid", actionProps.getProperty("availableworksheetitemid"));
            commandResponse.set("availableworksheetitemversionid", actionProps.getProperty("availableworksheetitemversionid"));
            commandResponse.set("availabilityflag", actionProps.getProperty("availabilityflag"));
            PropertyList loadSectionDetails = new PropertyList();
            loadSectionDetails.setProperty("worksheetid", actionProps.getProperty("worksheetid"));
            loadSectionDetails.setProperty("worksheetversionid", actionProps.getProperty("worksheetversionid"));
            loadSectionDetails.setProperty("worksheetsectionid", actionProps.getProperty("worksheetsectionid"));
            loadSectionDetails.setProperty("worksheetsectionversionid", actionProps.getProperty("worksheetsectionversionid"));
            this.getActionProcessor().processActionClass(LoadSection.class.getName(), loadSectionDetails);
            SDIData sectionData = (SDIData)loadSectionDetails.get("section");
            commandResponse.set("sectiondata", sectionData.getDataset("primary"));
        }
        catch (SapphireException e) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to set worksheet section status. Reason: " + e.getMessage(), e);
        }
    }

    private void checkFollowupNote(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet notes;
            if (commandRequest.getString("checkfollowupnote").equals("Y")) {
                DataSet notes2 = BaseELNAction.getWorksheetNotes(this.getQueryProcessor(), commandRequest.getString("worksheetid"), commandRequest.getString("worksheetversionid"), "followupflag = 'Y' AND resolvedflag = 'N'");
                if (notes2.size() == 0) {
                    commandResponse.set("nofollowups", "Y");
                    return;
                }
            } else if (commandRequest.getString("checksectionfollowupnote").equals("Y") && (notes = BaseELNAction.getWorksheetSectionNotes(this.getQueryProcessor(), database, commandRequest.getString("worksheetid"), commandRequest.getString("worksheetversionid"), commandRequest.getString("worksheetsectionid"), commandRequest.getString("worksheetsectionversionid"), "followupflag = 'Y' AND resolvedflag = 'N'", true)).size() == 0) {
                commandResponse.set("nofollowups", "Y");
                return;
            }
        }
        catch (Exception e) {
            commandResponse.addErrorHandler(e.getMessage());
        }
    }

    private void deleteSection(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("sdcid", "LV_WorksheetSection");
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetsectionid", commandRequest.getString("worksheetsectionid"));
            actionProps.setProperty("worksheetsectionversionid", commandRequest.getString("worksheetsectionversionid"));
            this.getActionProcessor().processActionClass(DeleteWorksheetSection.class.getName(), actionProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to delete worksheet section. Reason: " + e.getMessage(), e);
        }
    }

    private void loadWorksheetItem(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet item;
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            boolean dockmode = commandRequest.getString("dockmode").equals("Y");
            int auditsequence = -1;
            try {
                auditsequence = Integer.parseInt(commandRequest.getString("auditsequence", "-1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            DataSet dataSet = item = auditsequence == -1 ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true) : this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM a_worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND auditsequence = ?", new Object[]{worksheetitemid, worksheetitemversionid, auditsequence}, true);
            if (item.size() == 1) {
                WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(this.sapphireConnection, (HashMap)item.get(0), commandRequest.getString("width"));
                worksheetItem.setTemplate(commandRequest.getBoolean("template"));
                item.setString(0, "elementid", worksheetItem.getElementId());
                if (auditsequence > -1 && !worksheetItem.getWorksheetItemOptions().getOption("supportshistory", "N").equals("Y")) {
                    item.setValue(0, "html", this.getTranslationProcessor().translate("Historical data not available for this control"));
                } else if (item.getValue(0, "itemstatus", "InProgress").equals("InProgress") || item.getValue(0, "html").length() == 0) {
                    item.setValue(0, "html", dockmode ? worksheetItem.getDockViewHTML(commandRequest.getString("dockpanelid") + "_") : worksheetItem.getViewHTML());
                    item.setString(0, "availabilitystatus", worksheetItem.getAvailability());
                }
                item.setValue(0, "options", worksheetItem.getWorksheetItemOptions().toPropertyList().toJSONString());
                commandResponse.set("worksheetitem", item);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item. Reason: " + e.getMessage(), e);
        }
    }

    private void loadWorksheetItemDiff(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        String prefix = commandRequest.getString("prefix");
        try {
            int auditsequence = Integer.parseInt(commandRequest.getString("auditsequence", "-1"));
            int priorauditsequence = Integer.parseInt(commandRequest.getString("priorauditsequence", "-1"));
            DataSet current = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM a_worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND auditsequence = ?", new Object[]{worksheetitemid, worksheetitemversionid, auditsequence}, true);
            DataSet prior = this.getQueryProcessor().getPreparedSqlDataSet("SELECT contents FROM a_worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND auditsequence = ?", new Object[]{worksheetitemid, worksheetitemversionid, priorauditsequence}, true);
            if (current.size() == 1 && prior.size() == 1) {
                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(this.sapphireConnection, database, (HashMap)current.get(0));
                String diffhtml = worksheetItem.getDiffHTML(current.getValue(0, "contents"), prior.getValue(0, "contents"), prefix);
                commandResponse.set("diffhtml", diffhtml);
            } else {
                commandResponse.setStatusFail("Failed to load diff information");
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load diff information. Reason: " + e.getMessage(), e);
        }
    }

    private void loadWorksheetItemIncludes(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyListCollection cssincludes = new PropertyListCollection();
            PropertyListCollection scriptincludes = new PropertyListCollection();
            BaseELNAction.getWorksheetItemIncludes(this.sapphireConnection, this.getQueryProcessor(), cssincludes, scriptincludes);
            StringBuffer includes = new StringBuffer();
            for (int i = 0; i < cssincludes.size(); ++i) {
                includes.append("<link rel=\"stylesheet\" href=\"").append(cssincludes.getPropertyList(i).getProperty("cssfile")).append("\" type=\"text/css\"/>");
            }
            commandResponse.set("includes", includes.toString());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item includes. Reason: " + e.getMessage(), e);
        }
    }

    private void addItem(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String propertytreeid = commandRequest.getString("propertytreeid");
            String templateid = commandRequest.getString("templateid");
            String templateversionid = commandRequest.getString("templateversionid");
            String itemtemplateid = commandRequest.getString("itemtemplateid");
            String itemtemplateversionid = commandRequest.getString("itemtemplateversionid");
            DataSet items = commandRequest.getDataSet("worksheetitems");
            int itemrow = Integer.parseInt(commandRequest.getString("itemrow"));
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetsectionid", commandRequest.getString("worksheetsectionid"));
            actionProps.setProperty("worksheetsectionversionid", commandRequest.getString("worksheetsectionversionid"));
            actionProps.setProperty("propertytreeid", propertytreeid);
            actionProps.setProperty("sourcenodeid", commandRequest.getString("sourcenodeid"));
            actionProps.setProperty("sdcid", commandRequest.getString("sdcid"));
            actionProps.setProperty("keyid1", commandRequest.getString("keyid1"));
            actionProps.setProperty("templateid", templateid);
            actionProps.setProperty("templateversionid", templateversionid);
            actionProps.setProperty("fromworksheetid", commandRequest.getString("fromworksheetid"));
            actionProps.setProperty("fromworksheetversionid", commandRequest.getString("fromworksheetversionid"));
            actionProps.setProperty("itemtemplateid", itemtemplateid);
            actionProps.setProperty("itemtemplateversionid", itemtemplateversionid);
            actionProps.setProperty("keyid2", commandRequest.getString("keyid2"));
            actionProps.setProperty("keyid3", commandRequest.getString("keyid3"));
            actionProps.setProperty("usersequence", commandRequest.getString("itemrow"));
            this.getActionProcessor().processActionClass(AddWorksheetItem.class.getName(), actionProps);
            String worksheetitemid = actionProps.getProperty("worksheetitemid");
            String worksheetitemversionid = actionProps.getProperty("worksheetitemversionid");
            items.setValue(itemrow, "worksheetitemid", worksheetitemid);
            items.setValue(itemrow, "worksheetitemversionid", worksheetitemversionid);
            items.setValue(itemrow, "worksheetid", commandRequest.getString("worksheetid"));
            items.setValue(itemrow, "worksheetversionid", commandRequest.getString("worksheetversionid"));
            items.setValue(itemrow, "worksheetsectionid", commandRequest.getString("worksheetsectionid"));
            items.setValue(itemrow, "worksheetsectionversionid", commandRequest.getString("worksheetsectionversionid"));
            items.setValue(itemrow, "propertytreeid", commandRequest.getString("propertytreeid"));
            items.setValue(itemrow, "usersequence", String.valueOf(itemrow));
            items.setValue(itemrow, "createby", this.sapphireConnection.getSysuserId());
            if (actionProps.getProperty("usersequenceupdate", "N").equals("Y")) {
                int seq = itemrow + 1;
                for (int i = itemrow + 1; i < items.size(); ++i) {
                    items.setValue(i, "usersequence", String.valueOf(seq));
                    ++seq;
                }
            }
            DataSet item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
            WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(this.sapphireConnection, (HashMap)item.get(0), commandRequest.getString("width"));
            worksheetItem.setTemplate(commandRequest.getBoolean("template"));
            items.setString(itemrow, "elementid", worksheetItem.getElementId());
            items.setValue(itemrow, "html", templateid.length() == 0 && itemtemplateid.length() == 0 ? worksheetItem.getEditorHTML() : worksheetItem.getViewHTML());
            items.setString(itemrow, "availabilitystatus", worksheetItem.getAvailability());
            items.setValue(itemrow, "contents", worksheetItem.getDefaultContents());
            items.setValue(itemrow, "options", worksheetItem.getWorksheetItemOptions().toPropertyList().toJSONString());
            items.setValue(itemrow, "worksheetitemdesc", item.getValue(0, "worksheetitemdesc"));
            items.setValue(itemrow, "captionflag", item.getValue(0, "captionflag"));
            if (templateid.length() > 0) {
                ELNRequest.updateMRUList(new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.sapphireConnection.getConnectionId()), this.sapphireConnection.getSysuserId(), commandRequest.getString("__hostwebpageid") + "_recent_" + item.getValue(0, "propertytreeid") + "_templates", templateid + ";" + templateversionid);
            } else if (itemtemplateid.length() > 0) {
                ELNRequest.updateMRUList(new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.sapphireConnection.getConnectionId()), this.sapphireConnection.getSysuserId(), commandRequest.getString("__hostwebpageid") + "_recent_copy_worksheets", commandRequest.getString("fromworksheetid") + ";" + commandRequest.getString("fromworksheetversionid"));
            }
            commandResponse.set("worksheetitems", items);
            commandResponse.set("itemrow", String.valueOf(itemrow));
            commandResponse.set("itemtype", worksheetItem.getClientRenderer());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add worksheet item. Reason: " + e.getMessage(), e);
        }
    }

    private void editItem(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            this.getActionProcessor().processActionClass(EditWorksheetItem.class.getName(), commandRequest.getStringPropertyList());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to edit worksheet item. Reason: " + e.getMessage(), e);
        }
    }

    private void moveItem(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet items = commandRequest.getDataSet("worksheetitems");
            int itemrow = Integer.parseInt(commandRequest.getString("itemrow"));
            String worksheetid = items.getValue(itemrow, "worksheetid");
            String worksheetversionid = items.getValue(itemrow, "worksheetversionid");
            String worksheetitemid = items.getValue(itemrow, "worksheetitemid");
            String worksheetitemversionid = items.getValue(itemrow, "worksheetitemversionid");
            String currentsectionid = items.getValue(itemrow, "worksheetsectionid");
            String currentsectionversionid = items.getValue(itemrow, "worksheetsectionversionid");
            boolean up = commandRequest.getBoolean("up");
            int newseq = 0;
            if (up) {
                if (itemrow > 0) {
                    int currseq = Integer.parseInt(items.getValue(itemrow, "usersequence"));
                    newseq = Integer.parseInt(items.getValue(itemrow - 1, "usersequence"));
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{newseq, worksheetitemid, worksheetitemversionid});
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{currseq, items.getValue(itemrow - 1, "worksheetitemid"), items.getValue(itemrow - 1, "worksheetitemversionid")});
                    items.setValue(itemrow - 1, "usersequence", String.valueOf(currseq));
                    items.setValue(itemrow, "usersequence", String.valueOf(newseq));
                    items.add(itemrow - 1, items.remove(itemrow));
                } else {
                    String priorsectionid = commandRequest.getString("priorsectionid");
                    String priorsectionversionid = commandRequest.getString("priorsectionversionid");
                    DataSet prioritems = commandRequest.getDataSet("priorsectionitems");
                    newseq = prioritems.size() == 0 ? 0 : Integer.parseInt(prioritems.getString(prioritems.size() - 1, "usersequence")) + 1;
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence=?, worksheetsectionid=?, worksheetsectionversionid=? WHERE worksheetitemid=? AND worksheetitemversionid=?", new Object[]{newseq, priorsectionid, priorsectionversionid, worksheetitemid, worksheetitemversionid});
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence=usersequence-1 WHERE worksheetsectionid=? AND worksheetsectionversionid=?", new Object[]{currentsectionid, currentsectionversionid});
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence=0 WHERE worksheetsectionid=? AND worksheetsectionversionid=? AND usersequence=-1", new Object[]{currentsectionid, currentsectionversionid});
                    prioritems.copyRow(items, itemrow, 1);
                    int newrow = prioritems.size() - 1;
                    prioritems.setString(newrow, "usersequence", "" + newseq);
                    prioritems.setString(newrow, "worksheetsectionid", priorsectionid);
                    prioritems.setString(newrow, "worksheetsectionversionid", priorsectionversionid);
                    items.deleteRow(itemrow);
                    for (int i = 0; i < items.size(); ++i) {
                        items.setString(i, "usersequence", "" + (Integer.parseInt(items.getString(i, "usersequence")) - 1));
                    }
                    commandResponse.set("priorsectionkey", priorsectionid + ";" + priorsectionversionid);
                    commandResponse.set("priorworksheetitems", prioritems);
                }
            } else if (!up) {
                if (itemrow < items.size() - 1) {
                    int currseq = Integer.parseInt(items.getValue(itemrow, "usersequence"));
                    newseq = Integer.parseInt(items.getValue(itemrow + 1, "usersequence"));
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{newseq, worksheetitemid, worksheetitemversionid});
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence = ? WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{currseq, items.getValue(itemrow + 1, "worksheetitemid"), items.getValue(itemrow + 1, "worksheetitemversionid")});
                    items.setValue(itemrow + 1, "usersequence", String.valueOf(currseq));
                    items.setValue(itemrow, "usersequence", String.valueOf(newseq));
                    items.add(itemrow + 1, items.remove(itemrow));
                } else {
                    String nextsectionid = commandRequest.getString("nextsectionid");
                    String nextsectionversionid = commandRequest.getString("nextsectionversionid");
                    DataSet nextitems = commandRequest.getDataSet("nextsectionitems");
                    newseq = 0;
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence=usersequence+1 WHERE worksheetsectionid=? AND worksheetsectionversionid=?", new Object[]{currentsectionid, currentsectionversionid});
                    database.executePreparedUpdate("UPDATE worksheetitem SET usersequence=0, worksheetsectionid=?, worksheetsectionversionid=? WHERE worksheetitemid=? AND worksheetitemversionid=?", new Object[]{nextsectionid, nextsectionversionid, worksheetitemid, worksheetitemversionid});
                    for (int i = 0; i < nextitems.size(); ++i) {
                        int usersequence = Integer.parseInt(nextitems.getString(i, "usersequence"));
                        usersequence = usersequence == -1 ? 0 : usersequence;
                        nextitems.setString(i, "usersequence", "" + (usersequence + 1));
                    }
                    nextitems.copyRow(items, itemrow, 1);
                    int newrow = nextitems.size() - 1;
                    nextitems.setString(newrow, "usersequence", "0");
                    nextitems.setString(newrow, "worksheetsectionid", nextsectionid);
                    nextitems.setString(newrow, "worksheetsectionversionid", nextsectionversionid);
                    nextitems.sort("usersequence");
                    items.deleteRow(itemrow);
                    commandResponse.set("nextsectionkey", nextsectionid + ";" + nextsectionversionid);
                    commandResponse.set("nextworksheetitems", nextitems);
                }
            }
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", "LV_WorksheetItem");
            activityProps.setProperty("targetkeyid1", worksheetitemid);
            activityProps.setProperty("targetkeyid2", worksheetitemversionid);
            activityProps.setProperty("activitytype", "Edit");
            activityProps.setProperty("activitylog", "Moved item " + (up ? "up" : "down") + " to sequence " + newseq);
            this.getActionProcessor().processActionClass(AddWorksheetActivity.class.getName(), activityProps);
            commandResponse.set("worksheetitems", items);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to move worksheet item. Reason: " + e.getMessage(), e);
        }
    }

    private void setItemStatus(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("htmlerror", "Y");
            this.getActionProcessor().processActionClass(SetWorksheetItemStatus.class.getName(), actionProps);
            commandResponse.set("availableworksheetitemid", actionProps.getProperty("availableworksheetitemid"));
            commandResponse.set("availableworksheetitemversionid", actionProps.getProperty("availableworksheetitemversionid"));
            commandResponse.set("availabilityflag", actionProps.getProperty("availabilityflag"));
        }
        catch (SapphireException e) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to set worksheet item status. Reason: " + e.getMessage(), e);
        }
    }

    private void canExecuteOperation(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            String lockedby = "";
            if (commandRequest.getString("lockworksheet", "N").equals("N")) {
                RSet rset = das.createLockedRSet("LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "(null)", "LA");
                database.createPreparedResultSet("SELECT rsetitems.sysuserid \"__lockedby\" FROM worksheetitem, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = ? AND rsetitems.keyid1 = worksheetitem.worksheetitemid AND rsetitems.keyid2 = worksheetitem.worksheetitemversionid", new Object[]{rset.getRsetid(), "LV_WorksheetItem"});
                if (database.getNext()) {
                    lockedby = database.getValue("__lockedby");
                }
                das.clearRSet(rset);
            } else {
                RSet rset = das.createLockedRSet("LV_Worksheet", worksheetid, worksheetversionid, "(null)", "LA");
                database.createPreparedResultSet("SELECT rsetitems.sysuserid \"__lockedby\" FROM worksheet, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = ? AND rsetitems.keyid1 = worksheet.worksheetid AND rsetitems.keyid2 = worksheet.worksheetversionid", new Object[]{rset.getRsetid(), "LV_Worksheet"});
                if (database.getNext()) {
                    lockedby = database.getValue("__lockedby");
                }
                das.clearRSet(rset);
            }
            commandResponse.set("lockedby", lockedby);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to check if an operation can execute. Reason: " + e.getMessage(), e);
        }
    }

    private void startEditContent(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet item;
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String rsetid = "";
            if (commandRequest.getString("lockworksheet", "N").equals("N")) {
                DataAccessService das = new DataAccessService(this.sapphireConnection);
                RSet rset = das.createLockedRSet("LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "(null)", "LA");
                item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitem.*, rsetitems.lockstate \"__lockstate\", rsetitems.sysuserid \"__lockedby\" FROM worksheetitem, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = ? AND rsetitems.keyid1 = worksheetitem.worksheetitemid AND rsetitems.keyid2 = worksheetitem.worksheetitemversionid", new Object[]{rset.getRsetid(), "LV_WorksheetItem"}, true);
                rsetid = rset.getRsetid();
                commandResponse.set("lockedby", item.getValue(0, "__lockedby"));
            } else {
                item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitem.*, '0' \"__lockstate\", '' \"__lockedby\" FROM worksheetitem WHERE worksheetitem.worksheetitemid = ? AND worksheetitem.worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
            }
            WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(this.sapphireConnection, (HashMap)item.get(0), commandRequest.getString("width"));
            worksheetItem.setTemplate(commandRequest.getBoolean("template"));
            commandResponse.set("html", worksheetItem.getEditorHTML());
            commandResponse.set("contents", worksheetItem.getContentsForEdit());
            commandResponse.set("options", worksheetItem.getWorksheetItemOptions().toPropertyList());
            commandResponse.set("rsetid", rsetid.length() > 0 ? rsetid + ";" + worksheetItem.getEditRSet() : worksheetItem.getEditRSet());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to start editing worksheet item. Reason: " + e.getMessage(), e);
        }
    }

    private void endEditContent(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList actionProps = commandRequest.getStringPropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetitemid", commandRequest.getString("worksheetitemid"));
            actionProps.setProperty("worksheetitemversionid", commandRequest.getString("worksheetitemversionid"));
            actionProps.setProperty("contents", commandRequest.getString("contents"));
            actionProps.setProperty("template", commandRequest.getBoolean("template") ? "Y" : "N");
            actionProps.setProperty("activitylog", commandRequest.getString("activitylog"));
            actionProps.setProperty("auditreason", commandRequest.getString("auditreason"));
            actionProps.setProperty("auditsignedflag", commandRequest.getString("auditsignedflag"));
            actionProps.setProperty("auditactivity", commandRequest.getString("auditactivity"));
            this.getActionProcessor().processActionClass(SetWorksheetItemContent.class.getName(), actionProps);
            WorksheetItem worksheetItem = (WorksheetItem)actionProps.get("worksheetitem");
            if (commandRequest.getBoolean("complete")) {
                actionProps.setProperty("status", "Complete");
                actionProps.setProperty("htmlerror", "Y");
                this.getActionProcessor().processActionClass(SetWorksheetItemStatus.class.getName(), actionProps);
            }
            commandResponse.set("viewerhtml", worksheetItem.getViewHTML());
            String[] worksheetitem = worksheetItem.getDependentWorksheetItems();
            commandResponse.set("dependentworksheetitemid", worksheetitem[0]);
            commandResponse.set("dependentworksheetitemversionid", worksheetitem[1]);
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            String[] rsets = StringUtil.split(commandRequest.getString("rsetid"), ";");
            for (int i = 0; i < rsets.length; ++i) {
                if (rsets[i].length() <= 0) continue;
                das.clearRSet(new RSet(rsets[i]));
            }
        }
        catch (SapphireException e) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to end editing worksheet item. Reason: " + e.getMessage(), e);
        }
    }

    private void revertItemContent(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            int auditsequence = -1;
            try {
                auditsequence = Integer.parseInt(commandRequest.getString("auditsequence", "-1"));
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (auditsequence >= 0) {
                DataSet item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT contents FROM a_worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ? AND auditsequence = ?", new Object[]{commandRequest.getString("worksheetitemid"), commandRequest.getString("worksheetitemversionid"), auditsequence}, true);
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
                actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
                actionProps.setProperty("worksheetitemid", commandRequest.getString("worksheetitemid"));
                actionProps.setProperty("worksheetitemversionid", commandRequest.getString("worksheetitemversionid"));
                actionProps.setProperty("contents", item.getValue(0, "contents"));
                actionProps.setProperty("template", commandRequest.getBoolean("template") ? "Y" : "N");
                actionProps.setProperty("activitylog", "Reverted back to audit-sequence " + auditsequence);
                this.getActionProcessor().processActionClass(SetWorksheetItemContent.class.getName(), actionProps);
            }
        }
        catch (SapphireException e) {
            commandResponse.addErrorHandler(this.getActionProcessor().getErrorHandler());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to revert worksheet item content. Reason: " + e.getMessage(), e);
        }
    }

    private void cancelEditContent(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            boolean initialedit = commandRequest.getBoolean("initialedit");
            if (initialedit) {
                this.deleteItem(commandRequest, commandResponse, database);
            } else {
                DataSet item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
                WorksheetItem worksheetItem = WorksheetItemFactory.getRenderingInstance(this.sapphireConnection, (HashMap)item.get(0), commandRequest.getString("width"));
                commandResponse.set("viewerhtml", worksheetItem.getViewHTML());
            }
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            String[] rsets = StringUtil.split(commandRequest.getString("rsetid"), ";");
            for (int i = 0; i < rsets.length; ++i) {
                if (rsets[i].length() <= 0) continue;
                das.clearRSet(new RSet(rsets[i]));
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to end editing worksheet item. Reason: " + e.getMessage(), e);
        }
    }

    private void deleteItem(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            DataAccessService das = new DataAccessService(this.sapphireConnection);
            RSet rset = das.createLockedRSet("LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "(null)", "LA");
            DataSet item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT rsetitems.lockstate \"__lockstate\", rsetitems.sysuserid \"__lockedby\" FROM worksheetitem, rsetitems WHERE rsetitems.rsetid = ? AND rsetitems.sdcid = ? AND rsetitems.keyid1 = worksheetitem.worksheetitemid AND rsetitems.keyid2 = worksheetitem.worksheetitemversionid", new Object[]{rset.getRsetid(), "LV_WorksheetItem"}, true);
            das.clearRSet(rset);
            if (item.getValue(0, "__lockedby").length() == 0) {
                PropertyList actionProps = new PropertyList();
                actionProps.setProperty("sdcid", "LV_WorksheetItem");
                actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
                actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
                actionProps.setProperty("worksheetitemid", worksheetitemid);
                actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                actionProps.setProperty("auditreason", commandRequest.getString("auditreason"));
                actionProps.setProperty("auditsignedflag", commandRequest.getString("auditsignedflag"));
                actionProps.setProperty("auditactivity", commandRequest.getString("auditactivity"));
                this.getActionProcessor().processActionClass(DeleteWorksheetItem.class.getName(), actionProps);
            } else {
                commandResponse.set("lockedby", item.getValue(0, "__lockedby"));
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to delete worksheet item. Reason: " + e.getMessage(), e);
        }
    }

    private void loadItemDetails(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList options;
            String loadkeyid2;
            String loadkeyid1;
            String loadsdcid;
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            boolean template = commandRequest.getBoolean("template");
            boolean loadAttributes = false;
            boolean loadAttachments = false;
            boolean loadIncidents = false;
            DataSet itemdata = null;
            DataSet attributedata = null;
            DataSet attachmentdata = null;
            DataSet incidentdata = null;
            if (worksheetitemid.length() > 0) {
                PropertyList wsiOptions = null;
                itemdata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitem.*, worksheetsection.options \"wssoptions\", ( SELECT worksheetname FROM worksheet template WHERE template.worksheetid = worksheetitem.templateid AND template.worksheetversionid = worksheetitem.templateversionid ) \"templatename\" FROM worksheetitem, worksheetsection WHERE worksheetitem.worksheetsectionid = worksheetsection.worksheetsectionid AND worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(this.sapphireConnection, database, (HashMap)itemdata.get(0));
                wsiOptions = worksheetItem.getWorksheetItemOptions().toPropertyList();
                itemdata.setString(0, "elementid", worksheetItem.getElementId());
                commandResponse.set("options", wsiOptions);
                commandResponse.set("metadata", this.getMetaData(worksheetItem.getMetaData()));
                itemdata.setValue(0, "options", "");
                itemdata.setValue(0, "config", "");
                itemdata.setValue(0, "html", "");
                PropertyList wssOptions = new PropertyList();
                wssOptions.setPropertyList(itemdata.getClob(0, "wssoptions", ""));
                commandResponse.set("wssoptions", wssOptions);
                loadAttributes = template || wsiOptions.getProperty("allowitemattributes", "Y").equals("Y") && wsiOptions.getProperty("disableitemattributes", "N").equals("N");
                loadAttachments = template || wsiOptions.getProperty("allowitemattachments", "Y").equals("Y") && wsiOptions.getProperty("disableitemattachments", "N").equals("N");
                loadIncidents = template || wsiOptions.getProperty("allowitemincidents", "N").equals("Y");
                loadsdcid = "LV_WorksheetItem";
                loadkeyid1 = worksheetitemid;
                loadkeyid2 = worksheetitemversionid;
                DataSet params = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitemparam WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid});
                commandResponse.set("params", params);
                StringBuffer editorstyleid = new StringBuffer();
                for (int i = 0; i < params.size(); ++i) {
                    editorstyleid.append(";").append(params.getValue(i, "parameditorstyleid"));
                }
                if (editorstyleid.length() > 0) {
                    commandResponse.set("parameditorstyles", this.getEditorStyles(editorstyleid.substring(1)));
                }
            } else if (worksheetsectionid.length() > 0) {
                itemdata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetsection.*, ( SELECT worksheetname FROM worksheet template WHERE template.worksheetid = worksheetsection.templateid AND template.worksheetversionid = worksheetsection.templateversionid ) \"templatename\" FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{worksheetsectionid, worksheetsectionversionid}, true);
                options = new PropertyList();
                options.setPropertyList(itemdata.getClob(0, "options", ""));
                commandResponse.set("options", options);
                itemdata.setValue(0, "options", "");
                loadAttributes = template || options.getProperty("allowsectionattributes", "Y").equals("Y");
                loadAttachments = template || options.getProperty("allowsectionattachments", "Y").equals("Y");
                loadIncidents = template || options.getProperty("allowsectionincidents", "N").equals("Y");
                loadsdcid = "LV_WorksheetSection";
                loadkeyid1 = worksheetsectionid;
                loadkeyid2 = worksheetsectionversionid;
            } else {
                itemdata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheet.*,  ( SELECT worksheetname FROM worksheet template WHERE template.worksheetid = worksheet.templateid AND template.worksheetversionid = worksheet.templateversionid ) \"templatename\" FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid}, true);
                options = new PropertyList();
                options.setPropertyList(itemdata.getClob(0, "options", ""));
                options.setProperty("lesonly", itemdata.getValue(0, "lesflag"));
                options.setProperty("blockflag", itemdata.getValue(0, "blockflag"));
                options.setProperty("blocksdcid", itemdata.getValue(0, "blocksdcid"));
                commandResponse.set("options", options);
                itemdata.setValue(0, "options", "");
                loadAttributes = template || options.getProperty("allowworksheetattributes", "Y").equals("Y");
                loadAttachments = template || options.getProperty("allowworksheetattachments", "Y").equals("Y");
                loadIncidents = template || options.getProperty("allowworksheetincidents", "N").equals("Y");
                loadsdcid = "LV_Worksheet";
                loadkeyid1 = worksheetid;
                loadkeyid2 = worksheetversionid;
            }
            if (loadAttributes) {
                commandResponse.set("allowattributes", "Y");
                attributedata = this.loadAttributes(loadsdcid, loadkeyid1, loadkeyid2);
                JSONableMap editorStyles = this.getEditorStyles(attributedata.getColumnValues("editorstyleid", ";"));
                commandResponse.set("attributeeditorstyles", editorStyles);
                this.preprocessAttributes(attributedata, editorStyles);
            }
            if (loadAttachments) {
                commandResponse.set("allowattachments", "Y");
                attachmentdata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT attachmentdesc, attachmentnum  FROM sdiattachment  WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?   AND ( attachmentuse IS NULL OR attachmentuse <> 'HTMLEditor' ) ORDER BY usersequence, attachmentnum", new Object[]{loadsdcid, loadkeyid1, loadkeyid2});
            }
            if (loadIncidents) {
                commandResponse.set("allowincidents", "Y");
                incidentdata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT i.incidentid, i.incidentdesc, i.incidentstatus, i.incidenttype, i.incidentdt, refvalue.refdisplayicon  FROM incident i left outer join refvalue on refvalue.reftypeid = 'IncidentType' AND i.incidenttype = refvalue.refvalueid, incidentitem ii WHERE i.incidentid=ii.incidentid  AND ii.sourcesdcid = ? AND ii.sourcekeyid1 = ? AND ii.sourcekeyid2 = ? AND causalobjectflag='Y' ORDER BY i.usersequence, i.incidentid", new Object[]{loadsdcid, loadkeyid1, loadkeyid2});
                if (itemdata != null) {
                    itemdata.setNumber(0, "incidentcount", incidentdata.size());
                }
            }
            commandResponse.set("itemdata", itemdata != null ? itemdata : new DataSet());
            commandResponse.set("attributedata", attributedata != null ? attributedata : new DataSet());
            commandResponse.set("attachmentdata", attachmentdata != null ? attachmentdata : new DataSet());
            commandResponse.set("incidentdata", incidentdata != null ? incidentdata : new DataSet());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item data. Reason: " + e.getMessage(), e);
        }
    }

    private void preprocessAttributes(DataSet attributedata, JSONableMap editorStyles) {
        EditorStyleUtil esu = new EditorStyleUtil(this.connectionInfo.getConnectionId());
        if (attributedata != null && attributedata.size() > 0 && editorStyles != null) {
            for (int i = 0; i < attributedata.size(); ++i) {
                String datatype;
                PropertyList def;
                String editorstyleid = attributedata.getValue(i, "editorstyleid");
                String displayvalue = null;
                if (editorstyleid.length() > 0 && (def = (PropertyList)editorStyles.get(editorstyleid)) != null && (datatype = attributedata.getValue(i, "datatype")).equals("S")) {
                    String value = attributedata.getValue(i, "textvalue");
                    displayvalue = esu.getStringDisplayValue(value, def);
                }
                if (displayvalue == null || displayvalue.length() <= 0) continue;
                attributedata.setString(i, "_converteddisplayvalue", displayvalue);
            }
        }
    }

    private DataSet loadAttributes(String sdcid, String keyid1, String keyid2) {
        DataSet attributes = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdiattribute.attributeid, sdiattribute.attributeinstance, sdiattribute.attributesdcid, sdiattribute.editorstyleid, sdiattribute.datatype, sdiattribute.textvalue, sdiattribute.numericvalue, sdiattribute.datevalue, sdiattribute.clobvalue, sdiattribute.defaulttextvalue, sdiattribute.defaultnumericvalue, sdiattribute.defaultdatevalue, sdiattribute.mandatoryflag, sdiattribute.updateableflag, attributedef.attributetitle FROM sdiattribute, attributedef WHERE sdiattribute.sdcid = attributedef.basedonid AND sdiattribute.attributeid = attributedef.attributedefid AND sdiattribute.sdcid = ? AND sdiattribute.keyid1 = ? AND sdiattribute.keyid2 = ?", new Object[]{sdcid, keyid1, keyid2}, true);
        ELNRequest.formatAttributes(new M18NUtil(this.sapphireConnection), attributes);
        return attributes;
    }

    private static void formatAttributes(M18NUtil m18NUtil, DataSet attributes) {
        attributes.addColumn("displayvalue", 0);
        attributes.addColumn("defaultdisplayvalue", 0);
        for (int i = 0; i < attributes.size(); ++i) {
            DateFormat df;
            if (attributes.getValue(i, "datatype").equals("D")) {
                df = m18NUtil.getDefaultDateFormat();
                attributes.setValue(i, "displayvalue", attributes.getCalendar(i, "datevalue") != null ? df.format(attributes.getCalendar(i, "datevalue").getTime()) : "");
                attributes.setValue(i, "defaultdisplayvalue", attributes.getCalendar(i, "defaultdatevalue") != null ? df.format(attributes.getCalendar(i, "defaultdatevalue").getTime()) : "");
                continue;
            }
            if (attributes.getValue(i, "datatype").equals("O")) {
                df = m18NUtil.getDefaultDateOnlyFormat();
                attributes.setValue(i, "displayvalue", attributes.getCalendar(i, "datevalue") != null ? df.format(attributes.getCalendar(i, "datevalue").getTime()) : "");
                attributes.setValue(i, "defaultdisplayvalue", attributes.getCalendar(i, "defaultdatevalue") != null ? df.format(attributes.getCalendar(i, "defaultdatevalue").getTime()) : "");
                continue;
            }
            if (attributes.getValue(i, "datatype").equals("N")) {
                attributes.setValue(i, "displayvalue", attributes.getValue(i, "numericvalue"));
                attributes.setValue(i, "defaultdisplayvalue", attributes.getValue(i, "defaultnumericvalue"));
                continue;
            }
            attributes.setValue(i, "displayvalue", attributes.getValue(i, "textvalue"));
            attributes.setValue(i, "defaultdisplayvalue", attributes.getValue(i, "defaulttextvalue"));
        }
    }

    private void loadItemAudit(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            SafeSQL safeSQL = new SafeSQL();
            String sql = "SELECT wal.activitydt, coalesce( sysuser.initials, sysuser.sysuserdesc ) initials,  sysuser.sysuserdesc, wal.targetauditseq,tracelog.reason, tracelog_deleted.reason reason2 FROM worksheetactivitylog wal LEFT OUTER JOIN sysuser ON sysuser.sysuserid=activityby  LEFT OUTER JOIN a_worksheetitem on activitytype in ('SetStatus','SetContent' ) AND a_worksheetitem.worksheetitemid=targetkeyid1 AND a_worksheetitem.worksheetitemversionid=targetkeyid2 AND a_worksheetitem.auditsequence=targetauditseq LEFT OUTER JOIN tracelog ON a_worksheetitem.tracelogid=tracelog.tracelogid  LEFT OUTER JOIN tracelog tracelog_deleted ON activitytype='Delete Control' AND wal.tracelogid=tracelog_deleted.tracelogid WHERE activitytype in ( 'SetContent', 'SetConfig', 'Delete Control' ) AND wal.worksheetid=" + safeSQL.addVar(worksheetid) + " AND wal.worksheetversionid=" + safeSQL.addVar(worksheetversionid) + " AND targetsdcid=" + safeSQL.addVar("LV_WorksheetItem") + " AND targetkeyid1=" + safeSQL.addVar(worksheetitemid) + " AND targetkeyid2=" + safeSQL.addVar(worksheetitemversionid) + " ORDER BY wal.targetauditseq desc";
            DataSet auditdata = this.getQueryProcessor().getPreparedSqlDataSet(sql, safeSQL.getValues());
            boolean hasReason = false;
            for (int i = 0; i < auditdata.size(); ++i) {
                String reason2 = auditdata.getValue(i, "reason2");
                if (reason2.length() > 0) {
                    auditdata.setValue(i, "reason", reason2);
                }
                hasReason |= auditdata.getValue(i, "reason").length() > 0;
            }
            commandResponse.set("auditdata", auditdata != null ? auditdata : new DataSet());
            commandResponse.set("hasreason", hasReason ? "Y" : "N");
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item audit data. Reason: " + e.getMessage(), e);
        }
    }

    private DataSet getMetaData(LinkedHashMap<String, String> metadata) {
        DataSet dataSet = new DataSet();
        if (metadata != null) {
            for (String title : metadata.keySet()) {
                int row = dataSet.addRow();
                dataSet.setString(row, "title", title);
                dataSet.setString(row, "value", metadata.get(title));
            }
        }
        return dataSet;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void loadItemSDIs(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet sdis;
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String sdcid = commandRequest.getString("sdcid");
            PropertyList operation = commandRequest.getPropertyList("operation");
            String sdcOperation = operation.getProperty("sdcoperation");
            String keyid1 = commandRequest.getString("keyid1");
            String keyid2 = commandRequest.getString("keyid2");
            String keyid3 = commandRequest.getString("keyid3");
            String source = commandRequest.getString("source");
            PropertyList filter = operation.getPropertyListNotNull("sdifilter");
            PropertyList sdcProps = this.getSDCProcessor().getProperties(sdcid);
            String tableid = sdcProps.getProperty("tableid");
            if (source.equalsIgnoreCase("Query")) {
                DataSet item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(this.sapphireConnection, database, (HashMap)item.get(0));
                if (!worksheetItem.getWorksheetItemOptions().getOption("supportsquerysdis").equals("Y")) throw new SapphireException("Cannot derive list of associated SDIs when source type is query");
                String filterWhere = this.getFilterWhere(tableid, filter);
                int keycols = Integer.parseInt(sdcProps.getProperty("keycolumns"));
                String keyidWhere = keyid1 == null || keyid1.length() == 0 ? "" : tableid + "." + sdcProps.getProperty("keycolid1") + "='" + keyid1 + "'";
                keyidWhere = keyidWhere + (keycols >= 2 && keyid2 != null && keyid2.length() > 0 ? " AND " + tableid + "." + sdcProps.getProperty("keycolid2") + "='" + keyid2 + "'" : "");
                if ((keyidWhere = keyidWhere + (keycols >= 3 && keyid3 != null && keyid3.length() > 0 ? " AND " + tableid + "." + sdcProps.getProperty("keycolid3") + "='" + keyid3 + "'" : "")).length() > 0) {
                    filterWhere = filterWhere.length() > 0 ? filterWhere + " AND ( " + keyidWhere + " ) " : keyidWhere;
                }
                sdis = worksheetItem.getQuerySDIs(filterWhere);
            } else {
                DataSet dataSet = sdis = worksheetid.length() > 0 ? this.loadWorksheetSDIs(worksheetid, worksheetversionid, sdcid, sdcProps, filter, keyid1, keyid2, keyid3) : this.loadItemSDIs(worksheetitemid, worksheetitemversionid, sdcid, sdcProps, filter, keyid1, keyid2, keyid3);
                if (sdcOperation.length() > 0 && sdis.size() > 0 && !this.hasSecurityAccess(sdcid, sdis, sdcOperation)) {
                    commandResponse.setStatusFail("This operation is not permitted on the " + this.getSDCProcessor().getProperty(sdcid, "plural") + " in this control");
                }
            }
            commandResponse.set("worksheetitemsdis", sdis);
            commandResponse.set("plural", this.getSDCProcessor().getProperty(sdcid, "plural"));
            return;
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item SDIs. Reason: " + e.getMessage(), e);
        }
    }

    private boolean hasSecurityAccess(String sdcid, DataSet sdis, String sdcOperation) throws SapphireException {
        boolean hasAccess = true;
        try {
            String securityMode = this.getSDCProcessor().getProperty(sdcid, "accesscontrolledflag");
            int keyCount = Integer.parseInt(this.getSDCProcessor().getProperty(sdcid, "keycolumns"));
            PropertyList pl = new PropertyList();
            pl.setProperty("sdcid", sdcid);
            pl.setProperty("operation", sdcOperation);
            pl.setProperty("keyid1", sdis.getColumnValues("keyid1", ";"));
            pl.setProperty("keyid2", keyCount > 1 ? sdis.getColumnValues("keyid2", ";") : "");
            pl.setProperty("keyid3", keyCount > 2 ? sdis.getColumnValues("keyid3", ";") : "");
            boolean check = false;
            if ("D".equalsIgnoreCase(securityMode) || "SDIWorkItem".equalsIgnoreCase(sdcid) || "B".equalsIgnoreCase(securityMode)) {
                this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.ddt.DepartmentalSecurityCheck", pl);
                check = true;
            } else if ("S".equalsIgnoreCase(securityMode)) {
                this.getActionProcessor().processActionClass("com.labvantage.sapphire.actions.ddt.SDISecurityCheck", pl);
                check = true;
            }
            if (check) {
                String outoperation = pl.getProperty("operation");
                String failedsdis = pl.getProperty("failedsdis");
                String passedsdis = pl.getProperty("passedsdis");
                if (failedsdis.length() > 0) {
                    hasAccess = false;
                }
                this.logDebug("outoperation = " + outoperation);
                this.logDebug("failedsdis = " + failedsdis);
                this.logDebug("passedsdis = " + passedsdis);
            }
        }
        catch (Exception e) {
            throw new SapphireException("Failed to check SDC Operation. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.connectionInfo.getConnectionId())), e);
        }
        return hasAccess;
    }

    private void addSDI(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String source = commandRequest.getString("source");
            SDIList sdiList = commandRequest.getSDIList("sdilist");
            if (commandRequest.getBoolean("connectortypeassdc")) {
                database.createPreparedResultSet("SELECT connectortypesdcid FROM connectortype WHERE connectortypeid = ?", new Object[]{sdiList.getSdcid()});
                if (database.getNext()) {
                    sdiList.setSdcid(database.getValue("connectortypesdcid"));
                } else {
                    throw new SapphireException("Failed to resolve SDC from connectortypeid '" + sdiList.getSdcid() + "' when adding SDIs to worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid));
                }
            }
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("sdcid", sdiList.getSdcid());
            actionProps.setProperty("keyid1", sdiList.getKeyid1());
            actionProps.setProperty("keyid2", sdiList.getKeyid2());
            actionProps.setProperty("keyid3", sdiList.getKeyid3());
            if (source.equalsIgnoreCase("Control")) {
                actionProps.setProperty("worksheetitemid", worksheetitemid);
                actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                this.getActionProcessor().processActionClass(AddWorksheetItemSDI.class.getName(), actionProps);
            } else {
                this.getActionProcessor().processActionClass(AddWorksheetSDI.class.getName(), actionProps);
            }
            DataSet dependents = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT worksheetitemid, worksheetitemversionid FROM worksheetitemparam WHERE valuesdcid = ? AND valuekeyid1 = ? AND valuekeyid2 = ? AND valuetype = 'sdilist' AND valuelabel = 'keyid1'", new Object[]{"LV_WorksheetItem", worksheetitemid, worksheetitemversionid});
            if (dependents.size() > 0) {
                commandResponse.set("dependentworksheetitemid", dependents.getColumnValues("worksheetitemid", ";"));
                commandResponse.set("dependentworksheetitemversionid", dependents.getColumnValues("worksheetitemversionid", ";"));
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add worksheet item SDIs. Reason: " + e.getMessage(), e);
        }
    }

    private void deleteItemSDI(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            SDIList sdiList = commandRequest.getSDIList("sdilist");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            if (worksheetitemid.contains(";") && sdiList != null && sdiList.getKeyid1().contains(";")) {
                DataSet props = new DataSet();
                props.addColumnValues("worksheetitemid", 0, worksheetitemid, ";");
                props.addColumnValues("worksheetitemversionid", 0, worksheetitemversionid, ";");
                props.addColumnValues("sdcid", 0, sdiList.getSdcid(), ";");
                props.addColumnValues("keyid1", 0, sdiList.getKeyid1(), ";");
                props.addColumnValues("keyid2", 0, sdiList.getKeyid2(), ";");
                props.addColumnValues("keyid3", 0, sdiList.getKeyid3(), ";");
                props.padColumns();
                ArrayList<DataSet> groupedDataSets = props.getGroupedDataSets("worksheetitemid, worksheetitemversionid");
                for (DataSet groupedDataSet : groupedDataSets) {
                    actionProps.setProperty("worksheetitemid", groupedDataSet.getValue(0, "worksheetitemid"));
                    actionProps.setProperty("worksheetitemversionid", groupedDataSet.getValue(0, "worksheetitemversionid"));
                    actionProps.setProperty("sdcid", groupedDataSet.getValue(0, "sdcid"));
                    actionProps.setProperty("keyid1", groupedDataSet.getColumnValues("keyid1", ";"));
                    actionProps.setProperty("keyid2", groupedDataSet.getColumnValues("keyid2", ";"));
                    actionProps.setProperty("keyid3", groupedDataSet.getColumnValues("keyid3", ";"));
                    this.getActionProcessor().processActionClass(DeleteWorksheetItemSDI.class.getName(), actionProps);
                    commandResponse.set("worksheetitemid", actionProps.getProperty("worksheetitemid"));
                    commandResponse.set("worksheetitemversionid", actionProps.getProperty("worksheetitemversionid"));
                }
            } else {
                actionProps.setProperty("worksheetitemid", worksheetitemid);
                actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                if (sdiList != null && sdiList.getSdcid().length() > 0) {
                    actionProps.setProperty("sdcid", sdiList.getSdcid());
                    if (sdiList.size() > 0) {
                        actionProps.setProperty("keyid1", sdiList.getKeyid1());
                        actionProps.setProperty("keyid2", sdiList.getKeyid2());
                        actionProps.setProperty("keyid3", sdiList.getKeyid3());
                    }
                }
                this.getActionProcessor().processActionClass(DeleteWorksheetItemSDI.class.getName(), actionProps);
            }
            commandResponse.set("worksheetitemid", worksheetitemid);
            commandResponse.set("worksheetitemversionid", worksheetitemversionid);
            DataSet dependents = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT worksheetitemid, worksheetitemversionid FROM worksheetitemparam WHERE valuesdcid = ? AND valuekeyid1 = ? AND valuekeyid2 = ? AND valuetype = 'sdilist' AND valuelabel = 'keyid1'", new Object[]{"LV_WorksheetItem", worksheetitemid, worksheetitemversionid});
            if (dependents.size() > 0) {
                commandResponse.set("dependentworksheetitemid", dependents.getColumnValues("worksheetitemid", ";"));
                commandResponse.set("dependentworksheetitemversionid", dependents.getColumnValues("worksheetitemversionid", ";"));
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to delete worksheet item SDIs. Reason: " + e.getMessage(), e);
        }
    }

    private void addItemRef(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            this.getActionProcessor().processActionClass(AddWorksheetItemRef.class.getName(), commandRequest.getStringPropertyList());
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add worksheet item reference. Reason: " + e.getMessage(), e);
        }
    }

    private void loadAddOptions(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String mruPropertyId;
            DataSet templates;
            String workbookid = commandRequest.getString("workbookid");
            String workbookversionid = commandRequest.getString("workbookversionid");
            String templatetype = commandRequest.getString("templatetype");
            String propertytreeid = commandRequest.getString("propertytreeid");
            String templatecategoryid = commandRequest.getString("templatecategoryid");
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            int limit = 10;
            boolean workbookTemplates = false;
            if (templatetype.equals("C")) {
                templates = new DataSet();
                mruPropertyId = commandRequest.getString("__hostwebpageid") + "_recent_copy_worksheets";
            } else {
                templates = workbookTemplates ? this.loadWorkbookTemplates(workbookid, workbookversionid, templatetype, propertytreeid) : (templatetype.equals("I") ? this.getQueryProcessor().getPreparedSqlDataSet(database.isOracle() ? "SELECT worksheetid, worksheetversionid, worksheetname, templateprivacyflag  FROM ( SELECT worksheet.worksheetid, worksheet.worksheetversionid, worksheet.worksheetname, worksheet.templateprivacyflag FROM worksheet, worksheetitem  WHERE coalesce(worksheet.activeflag,'Y')='Y' AND worksheet.worksheetid = worksheetitem.worksheetid AND worksheet.worksheetversionid = worksheetitem.worksheetversionid AND templatetypeflag = ? AND propertytreeid = ? AND " + (templatecategoryid.length() > 0 ? "worksheet.worksheetid IN (SELECT categoryitem.keyid1 FROM categoryitem WHERE categoryitem.sdcid='LV_Worksheet' AND categoryitem.categoryid='" + templatecategoryid + "') AND " : "") + "( ( templateprivacyflag = 'G' AND worksheet.versionstatus IN (" + this.getGlobalVersionStatusList() + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') AND authorid = ? ) )  ORDER BY 3)  WHERE rownum < " + (limit + 2) : "SELECT TOP " + (limit + 1) + " worksheet.worksheetid, worksheet.worksheetversionid, worksheet.worksheetname, worksheet.templateprivacyflag  FROM worksheet, worksheetitem  WHERE coalesce(worksheet.activeflag,'Y')='Y' AND worksheet.worksheetid = worksheetitem.worksheetid AND worksheet.worksheetversionid = worksheetitem.worksheetversionid AND templatetypeflag = ? AND propertytreeid = ? AND " + (templatecategoryid.length() > 0 ? "worksheet.worksheetid IN (SELECT categoryitem.keyid1 FROM categoryitem WHERE categoryitem.sdcid='LV_Worksheet' AND categoryitem.categoryid='" + templatecategoryid + "') AND " : "") + "( ( templateprivacyflag = 'G' AND worksheet.versionstatus IN (" + this.getGlobalVersionStatusList() + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') AND authorid = ? ) )  ORDER BY 3", new Object[]{templatetype, propertytreeid, this.sapphireConnection.getSysuserId()}) : (templatetype.equals("S") ? this.getQueryProcessor().getPreparedSqlDataSet(database.isOracle() ? "SELECT worksheetid, worksheetversionid, worksheetname, templateprivacyflag FROM  (SELECT worksheetid, worksheetversionid, worksheetname,templateprivacyflag  FROM worksheet WHERE coalesce(worksheet.activeflag,'Y')='Y' AND templatetypeflag = ? AND  ( ( templateprivacyflag = 'G' AND worksheet.versionstatus IN (" + this.getGlobalVersionStatusList() + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') AND authorid = ? ) )  ORDER BY 3)  WHERE rownum < " + (limit + 2) : "SELECT TOP " + (limit + 1) + " worksheetid, worksheetversionid, worksheetname, templateprivacyflag FROM worksheet WHERE coalesce(worksheet.activeflag,'Y')='Y' AND templatetypeflag = ? AND  ( ( templateprivacyflag = 'G' AND worksheet.versionstatus IN ( " + this.getGlobalVersionStatusList() + ") ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') AND authorid = ? ) )  ORDER BY 3", new Object[]{templatetype, this.sapphireConnection.getSysuserId()}) : new DataSet()));
                mruPropertyId = commandRequest.getString("__hostwebpageid") + "_recent_" + (templatetype.equals("S") ? "section" : propertytreeid) + "_templates";
            }
            DataSet recents = this.loadMRUList(mruPropertyId, "", 5);
            commandResponse.set("recentitems", recents);
            commandResponse.set("templates", templates);
            if (templates.size() > limit) {
                commandResponse.set("moretemplates", "Y");
                templates.deleteRow(templates.size() - 1);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item add options. Reason: " + e.getMessage(), e);
        }
    }

    private void loadReferences(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            DataSet externalrefs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheet.worksheetid, worksheet.worksheetversionid, worksheet.worksheetname, refsdcid, refkeyid1, refkeyid2, reffunction, worksheetitemreference.createby, worksheetitemreference.createdt FROM worksheet, worksheetitem, worksheetitemreference WHERE worksheetitemreference.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemreference.worksheetitemversionid = worksheetitem.worksheetitemversionid AND       worksheetitem.worksheetid = worksheet.worksheetid AND worksheetitem.worksheetversionid = worksheet.worksheetversionid AND       refworksheetid = ? AND refworksheetversionid = ? ORDER BY worksheetitemreference.createdt", new Object[]{worksheetid, worksheetversionid}, true);
            commandResponse.set("externalrefs", externalrefs);
            DataSet internalrefs = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitem.worksheetitemid, worksheetitem.worksheetitemversionid, refworksheetid, refworksheetversionid, worksheet.worksheetname, refsdcid, refkeyid1, refkeyid2, reffunction, worksheetitemreference.createby, worksheetitemreference.createdt FROM worksheet, worksheetitem, worksheetitemreference WHERE worksheetitemreference.worksheetitemid = worksheetitem.worksheetitemid AND worksheetitemreference.worksheetitemversionid = worksheetitem.worksheetitemversionid AND       worksheetitemreference.refworksheetid = worksheet.worksheetid AND worksheetitemreference.refworksheetversionid = worksheet.worksheetversionid AND       worksheetitem.worksheetid = ? AND worksheetitem.worksheetversionid = ? ORDER BY worksheetitemreference.createdt", new Object[]{worksheetid, worksheetversionid}, true);
            commandResponse.set("internalrefs", internalrefs);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item references. Reason: " + e.getMessage(), e);
        }
    }

    private void loadActivityLog(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DateTimeUtil dtu = new DateTimeUtil();
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            PropertyList format = this.getConfigurationProcessor().getPolicy("ELNPolicy", "Sapphire Custom").getPropertyListNotNull("exportformat");
            PropertyList digitalsigningProps = format.getPropertyListNotNull("digitalsigning");
            String targetsdcid = commandRequest.getString("targetsdcid", "");
            String targetkeyid1 = commandRequest.getString("targetkeyid1", "");
            String targetkeyid2 = commandRequest.getString("targetkeyid2", "1");
            boolean publishworksheetevent = commandRequest.getString("publishworksheetevent", "N").equals("Y");
            ArrayList<Object> bindVars = new ArrayList<Object>();
            bindVars.add(worksheetid);
            bindVars.add(worksheetversionid);
            StringBuffer sql = new StringBuffer("SELECT worksheetactivitylog.targetauditseq, worksheetactivitylog.activityby, worksheetactivitylog.activitydt, worksheetactivitylog.activitytype, sysuser.sysuserdesc, worksheetactivitylog.targetkeyid1, worksheetactivitylog.targetkeyid2, worksheetactivitylog.reporteventid, CASE WHEN targetsdcid = 'LV_Worksheet' THEN 'Worksheet' WHEN targetsdcid = 'LV_WorksheetSection' THEN 'Section' WHEN targetsdcid = 'LV_WorksheetItem' THEN 'Control' ELSE 'Unknown' END AS targetsdcid, CASE WHEN targetsdcid = 'LV_Worksheet' THEN     (SELECT worksheetname FROM worksheet WHERE worksheetid = targetkeyid1 AND worksheetversionid=targetkeyid2) WHEN targetsdcid = 'LV_WorksheetSection' THEN     (SELECT worksheetsectiondesc FROM worksheetsection WHERE worksheetsectionid = targetkeyid1 AND worksheetsectionversionid=targetkeyid2) WHEN targetsdcid = 'LV_WorksheetItem' THEN     (SELECT Coalesce (NullIf (worksheetitemdesc,''),propertytreeid) FROM worksheetitem WHERE worksheetitemid = targetkeyid1 AND worksheetitemversionid=targetkeyid2)   ELSE 'Unknown' END AS targetname,  worksheetactivitylog.activitylog, tracelog_worksheet.reason reason1, tracelog_section.reason reason2, tracelog.reason reason3, tracelog_deleted.reason reason4 FROM worksheetactivitylog  LEFT OUTER JOIN sysuser ON sysuser.sysuserid=activityby  LEFT OUTER JOIN a_worksheet on activitytype='SetStatus' AND a_worksheet.worksheetid=targetkeyid1 AND a_worksheet.worksheetversionid=targetkeyid2 AND a_worksheet.auditsequence=targetauditseq LEFT OUTER JOIN tracelog tracelog_worksheet ON a_worksheet.tracelogid=tracelog_worksheet.tracelogid   LEFT OUTER JOIN a_worksheetsection on activitytype='SetStatus' AND a_worksheetsection.worksheetsectionid=targetkeyid1 AND a_worksheetsection.worksheetsectionversionid=targetkeyid2 AND a_worksheetsection.auditsequence=targetauditseq LEFT OUTER JOIN tracelog tracelog_section ON a_worksheetsection.tracelogid=tracelog_section.tracelogid  LEFT OUTER JOIN a_worksheetitem on activitytype in ( 'SetStatus','SetContent' ) AND a_worksheetitem.worksheetitemid=targetkeyid1 AND a_worksheetitem.worksheetitemversionid=targetkeyid2 AND a_worksheetitem.auditsequence=targetauditseq LEFT OUTER JOIN tracelog ON a_worksheetitem.tracelogid=tracelog.tracelogid  LEFT OUTER JOIN tracelog tracelog_deleted ON activitytype='Delete Control' AND worksheetactivitylog.tracelogid=tracelog_deleted.tracelogid ");
            sql.append(" WHERE worksheetactivitylog.worksheetid = ? AND worksheetactivitylog.worksheetversionid = ?");
            if (commandRequest.contains("activityby") && commandRequest.getString("activityby").length() > 0) {
                sql.append(" AND worksheetactivitylog.activityby = ? ");
                bindVars.add(commandRequest.getString("activityby"));
            }
            if (commandRequest.contains("activitytype") && commandRequest.getString("activitytype").length() > 0) {
                String activitytype = commandRequest.getString("activitytype").equalsIgnoreCase("Publish") ? "Export" : commandRequest.getString("activitytype");
                sql.append(" AND worksheetactivitylog.activitytype = ? ");
                bindVars.add(activitytype);
            }
            if (commandRequest.contains("activitydt_start") && commandRequest.getString("activitydt_start").length() > 0) {
                sql.append(" AND worksheetactivitylog.activitydt >= ? ");
                bindVars.add(dtu.getTimestamp(commandRequest.getString("activitydt_start")));
            }
            if (commandRequest.contains("activitydt_end") && commandRequest.getString("activitydt_end").length() > 0) {
                sql.append(" AND worksheetactivitylog.activitydt <= ? ");
                bindVars.add(dtu.getTimestamp(commandRequest.getString("activitydt_end")));
            }
            if (targetsdcid.length() > 0) {
                sql.append(" AND targetsdcid = ? ");
                bindVars.add(targetsdcid);
            }
            if (targetkeyid1.length() > 0) {
                sql.append(" AND targetkeyid1 = ? ");
                bindVars.add(targetkeyid1);
            }
            if (targetkeyid2.length() > 0) {
                sql.append(" AND targetkeyid2 = ? ");
                bindVars.add(targetkeyid2);
            }
            if (publishworksheetevent) {
                sql.append(" AND worksheetactivitylog.activitytype ='Export' ");
            }
            DataSet log = this.getQueryProcessor().getPreparedSqlDataSet(sql + " ORDER BY worksheetactivitylog.activitydt DESC, worksheetactivitylog.activitylogid DESC ", bindVars.toArray(new Object[bindVars.size()]), true);
            for (int i = 0; i < log.size(); ++i) {
                log.setString(i, "reason", log.getValue(i, "reason1", log.getValue(i, "reason2", log.getValue(i, "reason3", log.getValue(i, "reason4")))));
            }
            log.removeColumn("reason1");
            log.removeColumn("reason2");
            log.removeColumn("reason3");
            log.removeColumn("reason4");
            log.addColumn("document", 0);
            log.addColumn("alreadysigned", 0);
            log.addColumn("signpdf", 0);
            log.addColumn("documenttype", 0);
            log.addColumn("hasRole", 0);
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("activitytype", "Export");
            DataSet exportDataset = log.getFilteredDataSet(filter);
            for (int i = 0; i < exportDataset.size(); ++i) {
                if (publishworksheetevent) {
                    String reporteventid = exportDataset.getValue(i, "reporteventid");
                    if (OpalUtil.isNotEmpty(reporteventid)) {
                        String imgurl;
                        String displayType = this.getDocumentDisplayType(reporteventid);
                        String string = imgurl = displayType.equalsIgnoreCase("pdf") ? "<a style='border:none' href='rc?command=operation&operationclass=com.labvantage.sapphire.modules.eln.ExportWorksheet&reporteventid=" + reporteventid + "' target='_self'><img style='border:none' title='Click to view the report' src='WEB-OPAL/images/pdf.gif'/></a>" : "<a style='border:none' href='rc?command=operation&operationclass=com.labvantage.sapphire.modules.eln.ExportWorksheet&reporteventid=" + reporteventid + "' target='_self'><img style='border:none' title='Click to view the report' src='WEB-CORE/imageref/basic_application_icons/file_types/16/doc.png'/></a>";
                        String signed = displayType.equalsIgnoreCase("pdf") ? (this.isAlreadySigned(reporteventid) ? "<img title='Digitally signed' src='WEB-CORE/imageref/flat/16/flat_black_draw_pen_reflection.svg'/>" : "") : "";
                        exportDataset.setValue(i, "document", exportDataset.getValue(i, "activitylog") + " " + imgurl);
                        exportDataset.setValue(i, "alreadysigned", signed);
                        exportDataset.setValue(i, "documenttype", displayType);
                        exportDataset.setValue(i, "hasrole", this.sapphireConnection.hasRole(digitalsigningProps.getProperty("confirmationrole")) ? "yes" : "no");
                    } else {
                        exportDataset.setValue(i, "document", exportDataset.getValue(i, "activitylog"));
                        exportDataset.setValue(i, "reporteventid", "No report event generated");
                    }
                }
                exportDataset.setValue(i, "activitytype", "Publish");
            }
            filter.clear();
            filter.put("activitytype", "Delete Control");
            log.addColumn("deletecontrolprops", 0);
            log.addColumn("_donotshowdock", 0);
            DataSet deleteControlDataSet = log.getFilteredDataSet(filter);
            try {
                for (int i = 0; i < deleteControlDataSet.size(); ++i) {
                    String worksheetitemid = deleteControlDataSet.getValue(i, "targetkeyid1");
                    String worksheetitemversionid = deleteControlDataSet.getValue(i, "targetkeyid2");
                    int targetauditseq = deleteControlDataSet.getInt(i, "targetauditseq");
                    if (targetauditseq >= 0) {
                        DataSet deleteAudit = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM a_worksheetitem WHERE worksheetitemid=? AND worksheetitemversionid=? AND auditsequence=?", new Object[]{worksheetitemid, worksheetitemversionid, targetauditseq}, true);
                        if (deleteAudit == null || deleteAudit.size() != 1) continue;
                        WorksheetItem instance = WorksheetItemFactory.getInstance(this.sapphireConnection, database, (HashMap)deleteAudit.get(0));
                        String supportsHistory = instance.getWorksheetItemOptions().getOption("supportshistory");
                        JSONableMap deleteControlProps = new JSONableMap();
                        deleteControlProps.put("supportshistorydiffing", instance.getWorksheetItemOptions().getOption("supportshistorydiffing"));
                        deleteControlProps.put("supportshistory", supportsHistory);
                        deleteControlDataSet.setString(i, "deletecontrolprops", deleteControlProps.toJSONString());
                        if (supportsHistory.equals("Y")) continue;
                        HashMap<String, String> donotshowFilter = new HashMap<String, String>();
                        donotshowFilter.put("targetkeyid1", worksheetitemid);
                        donotshowFilter.put("targetkeyid2", worksheetitemversionid);
                        DataSet donotshows = log.getFilteredDataSet(donotshowFilter);
                        donotshows.setString(-1, "_donotshowdock", "Y");
                        continue;
                    }
                    deleteControlDataSet.setString(i, "activitytype", "Open");
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            commandResponse.set("activitylog", log);
            if (targetsdcid.equals("LV_Worksheet")) {
                commandResponse.set("istemplate", this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM worksheet WHERE worksheetid=? AND worksheetversionid=? AND templateflag='Y'", new String[]{targetkeyid1, targetkeyid2}) > 0 ? "Y" : "N");
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load activity log. Reason: " + e.getMessage(), e);
        }
    }

    private boolean isAlreadySigned(String reporteventid) {
        SafeSQL safeSQL = new SafeSQL();
        safeSQL.addVar(reporteventid);
        DataSet event = this.getQueryProcessor().getPreparedSqlDataSet("Select * from reportevent where reporteventid=?", safeSQL.getValues());
        String digitallysignedflag = event.getValue(0, "digitallysignedflag");
        return digitallysignedflag.equalsIgnoreCase("Y");
    }

    private String getDocumentDisplayType(String reporteventid) {
        SafeSQL safesql = new SafeSQL();
        String sql = "Select displaytype from reportevent where reporteventid = ?";
        safesql.addVar(reporteventid);
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet(sql, safesql.getValues());
        return ds.getValue(0, "displaytype");
    }

    private void logActivity(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            activityProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            activityProps.setProperty("targetsdcid", commandRequest.getString("targetsdcid"));
            activityProps.setProperty("targetkeyid1", commandRequest.getString("targetkeyid1"));
            activityProps.setProperty("targetkeyid2", commandRequest.getString("targetkeyid2"));
            activityProps.setProperty("activitytype", commandRequest.getString("activitytype"));
            activityProps.setProperty("activitylog", commandRequest.getString("activitylog"));
            this.getActionProcessor().processActionClass(AddWorksheetActivity.class.getName(), activityProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to log activity. Reason: " + e.getMessage(), e);
        }
    }

    private void loadConfig(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            DataSet item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT config, propertytreeid FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
            PropertyList config = new PropertyList();
            config.setPropertyList(item.getClob(0, "config"));
            commandResponse.set("config", config);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item config. Reason: " + e.getMessage(), e);
        }
    }

    private void saveConfig(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String propertyid = commandRequest.getString("propertyid");
            String propertyvalue = this.unescapeChars(commandRequest.getString("propertyvalue"));
            PropertyList config = commandRequest.getPropertyList("config");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("templateflag", commandRequest.getString("templateflag"));
            actionProps.setProperty("worksheetitemid", worksheetitemid);
            actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
            actionProps.setProperty("propertyid", propertyid);
            actionProps.setProperty("propertyvalue", propertyvalue);
            actionProps.setProperty("config", config != null ? config.toXMLString() : "");
            this.getActionProcessor().processActionClass(SetWorksheetItemConfig.class.getName(), actionProps);
            if (commandRequest.getBoolean("loadviewer")) {
                WorksheetItem worksheetItem = (WorksheetItem)actionProps.get("worksheetitem");
                commandResponse.set("viewerhtml", worksheetItem.getViewHTML());
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to save worksheet item config. Reason: " + e.getMessage(), e);
        }
    }

    private void loadOptions(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            PropertyList options = new PropertyList();
            if (worksheetitemid.length() > 0) {
                DataSet data = this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
                options.setPropertyList(data.getClob(0, "options"));
            } else if (worksheetsectionid.length() > 0) {
                DataSet data = this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{worksheetsectionid, worksheetsectionversionid}, true);
                options.setPropertyList(data.getClob(0, "options"));
            } else {
                DataSet data = this.getQueryProcessor().getPreparedSqlDataSet("SELECT options, lesflag, blockflag, blocksdcid FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid}, true);
                options.setPropertyList(data.getClob(0, "options"));
                options.setProperty("lesonly", data.getValue(0, "lesflag"));
                options.setProperty("blockflag", data.getValue(0, "blockflag"));
                options.setProperty("blocksdcid", data.getValue(0, "blocksdcid"));
            }
            commandResponse.set("options", options);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to load worksheet item options. Reason: " + e.getMessage(), e);
        }
    }

    private void saveOptions(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            PropertyList options = commandRequest.getPropertyList("options");
            String optionsXML = options.toXMLString();
            boolean apply = commandRequest.getBoolean("apply");
            ActionBlock ab = new ActionBlock();
            if (worksheetitemid.length() > 0) {
                if (apply) {
                    database.createPreparedResultSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
                    while (database.getNext()) {
                        PropertyList editProps = new PropertyList();
                        editProps.setProperty("worksheetid", worksheetid);
                        editProps.setProperty("worksheetversionid", worksheetversionid);
                        editProps.setProperty("worksheetitemid", database.getValue("worksheetitemid"));
                        editProps.setProperty("worksheetitemversionid", database.getValue("worksheetitemversionid"));
                        editProps.setProperty("options", optionsXML);
                        ab.setActionClass("EditItemOptions" + database.getValue("worksheetitemid") + database.getValue("worksheetitemversionid"), EditWorksheetItem.class.getName(), editProps);
                    }
                } else {
                    PropertyList editProps = new PropertyList();
                    editProps.setProperty("worksheetid", worksheetid);
                    editProps.setProperty("worksheetversionid", worksheetversionid);
                    editProps.setProperty("worksheetitemid", worksheetitemid);
                    editProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                    editProps.setProperty("options", optionsXML);
                    ab.setActionClass("EditItemOptions", EditWorksheetItem.class.getName(), editProps);
                }
            } else if (worksheetsectionid.length() > 0) {
                ELNRequest.resolveSectionSDIApprovals(this.getQueryProcessor(), worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid, options, ab);
                if (apply) {
                    boolean firstPass = true;
                    int level = 0;
                    database.createPreparedResultSet("SELECT worksheetsectionid, worksheetsectionversionid, sectionlevel, worksheetsectiondesc FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ?   AND usersequence >= ( SELECT usersequence FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ? ) ORDER BY usersequence", new Object[]{worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid});
                    while (database.getNext()) {
                        if (firstPass || database.getInt("sectionlevel") > level) {
                            PropertyList editProps = new PropertyList();
                            editProps.setProperty("worksheetid", worksheetid);
                            editProps.setProperty("worksheetversionid", worksheetversionid);
                            editProps.setProperty("worksheetsectionid", database.getValue("worksheetsectionid"));
                            editProps.setProperty("worksheetsectionversionid", database.getValue("worksheetsectionversionid"));
                            editProps.setProperty("options", optionsXML);
                            ab.setActionClass("EditSectionOptions" + database.getValue("worksheetsectionid") + database.getValue("worksheetsectionversionid"), EditWorksheetSection.class.getName(), editProps);
                        }
                        if (firstPass) {
                            level = database.getInt("sectionlevel");
                        }
                        firstPass = false;
                    }
                } else {
                    PropertyList editProps = new PropertyList();
                    editProps.setProperty("worksheetid", worksheetid);
                    editProps.setProperty("worksheetversionid", worksheetversionid);
                    editProps.setProperty("worksheetsectionid", worksheetsectionid);
                    editProps.setProperty("worksheetsectionversionid", worksheetsectionversionid);
                    editProps.setProperty("options", optionsXML);
                    ab.setActionClass("EditSectionOptions", EditWorksheetSection.class.getName(), editProps);
                }
            } else {
                PropertyList editProps = new PropertyList();
                editProps.setProperty("worksheetid", worksheetid);
                editProps.setProperty("worksheetversionid", worksheetversionid);
                PropertyListCollection worksheetsdcs = options.getCollection("worksheetsdcs");
                if (worksheetid != null) {
                    for (int i = 0; i < worksheetsdcs.size(); ++i) {
                        PropertyList worksheetsdc = worksheetsdcs.getPropertyList(i);
                        String sdcid = worksheetsdc.getProperty("sdcid");
                        worksheetsdc.setProperty("title", StringUtil.initCaps(this.getSDCProcessor().getProperty(sdcid, "plural")));
                    }
                }
                editProps.setProperty("options", optionsXML);
                editProps.setProperty("lesflag", options.getProperty("lesonly", "N"));
                editProps.setProperty("blockflag", options.getProperty("blockflag", "N"));
                editProps.setProperty("blocksdcid", options.getProperty("blockflag").equals("Y") ? options.getProperty("blocksdcid") : "");
                editProps.setProperty("elnrequest", "Y");
                ab.setActionClass("EditWorksheetOptions", EditWorksheet.class.getName(), editProps);
                ELNRequest.resolveWorksheetSDIApprovals(this.getQueryProcessor(), worksheetid, worksheetversionid, options, ab);
                if (apply) {
                    String sectionOptionsXML = options.getPropertyList("sectiondefaults").toXMLString();
                    database.createPreparedResultSet("SELECT worksheetsectionid, worksheetsectionversionid FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ? ", new Object[]{worksheetid, worksheetversionid});
                    while (database.getNext()) {
                        PropertyList sectionProps = new PropertyList();
                        sectionProps.setProperty("worksheetid", worksheetid);
                        sectionProps.setProperty("worksheetversionid", worksheetversionid);
                        sectionProps.setProperty("worksheetsectionid", database.getValue("worksheetsectionid"));
                        sectionProps.setProperty("worksheetsectionversionid", database.getValue("worksheetsectionversionid"));
                        sectionProps.setProperty("options", sectionOptionsXML);
                        ab.setActionClass("EditSectionOptions" + database.getValue("worksheetsectionid") + database.getValue("worksheetsectionversionid"), EditWorksheetSection.class.getName(), sectionProps);
                    }
                    String itemOptionsXML = options.getPropertyList("itemdefaults").toXMLString();
                    database.createPreparedResultSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
                    while (database.getNext()) {
                        PropertyList itemProps = new PropertyList();
                        itemProps.setProperty("worksheetid", worksheetid);
                        itemProps.setProperty("worksheetversionid", worksheetversionid);
                        itemProps.setProperty("worksheetitemid", database.getValue("worksheetitemid"));
                        itemProps.setProperty("worksheetitemversionid", database.getValue("worksheetitemversionid"));
                        itemProps.setProperty("options", itemOptionsXML);
                        ab.setActionClass("EditItemOptions" + database.getValue("worksheetitemid") + database.getValue("worksheetitemversionid"), EditWorksheetItem.class.getName(), itemProps);
                    }
                }
            }
            this.getActionProcessor().processActionBlock(ab);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to save options. Reason: " + e.getMessage(), e);
        }
    }

    public static void resolveSectionSDIApprovals(QueryProcessor qp, String worksheetid, String worksheetversionid, String worksheetsectionid, String worksheetsectionversionid, PropertyList options, ActionBlock ab) throws SapphireException {
        int deleteApprovalType = 0;
        if (options.getProperty("sectioncompletion").equals("A") && options.getProperty("sectionapprovaltype").length() > 0) {
            ELNRequest.checkNestedSectionApprovalTypes(qp, worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid);
            DataSet approvals = qp.getPreparedSqlDataSet("SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?", new Object[]{"LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid});
            boolean exists = false;
            for (int i = 0; i < approvals.size(); ++i) {
                String approvaltypeid = approvals.getValue(i, "approvaltypeid");
                if (approvaltypeid.equals(options.getProperty("sectionapprovaltype"))) {
                    exists = true;
                    continue;
                }
                PropertyList delApprovalProps = new PropertyList();
                delApprovalProps.put("sdcid", "LV_WorksheetSection");
                delApprovalProps.put("keyid1", worksheetsectionid);
                delApprovalProps.put("keyid2", worksheetsectionversionid);
                delApprovalProps.put("approvaltypeid", approvaltypeid);
                ab.setAction("DeleteSectionApproval" + deleteApprovalType++, "DeleteSDIApproval", "1", delApprovalProps);
            }
            if (!exists) {
                PropertyList addApprovalProps = new PropertyList();
                addApprovalProps.put("sdcid", "LV_WorksheetSection");
                addApprovalProps.put("keyid1", worksheetsectionid);
                addApprovalProps.put("keyid2", worksheetsectionversionid);
                addApprovalProps.put("approvaltypeid", options.getProperty("sectionapprovaltype"));
                ab.setAction("AddSectionApproval", "AddSDIApproval", "1", addApprovalProps);
            }
        } else {
            DataSet approvals = qp.getPreparedSqlDataSet("SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?", new Object[]{"LV_Worksheet", worksheetid, worksheetversionid});
            for (int i = 0; i < approvals.size(); ++i) {
                PropertyList delApprovalProps = new PropertyList();
                delApprovalProps.put("sdcid", "LV_WorksheetSection");
                delApprovalProps.put("keyid1", worksheetsectionid);
                delApprovalProps.put("keyid2", worksheetsectionversionid);
                delApprovalProps.put("approvaltypeid", approvals.getValue(i, "approvaltypeid"));
                ab.setAction("DeleteSectionApproval" + deleteApprovalType++, "DeleteSDIApproval", "1", delApprovalProps);
            }
        }
    }

    public static void resolveWorksheetSDIApprovals(QueryProcessor qp, String worksheetid, String worksheetversionid, PropertyList options, ActionBlock ab) throws SapphireException {
        int deleteApprovalType = 0;
        if (options.getProperty("worksheetcompletion").equals("A") && options.getProperty("worksheetapprovaltype").length() > 0) {
            DataSet approvals = qp.getPreparedSqlDataSet("SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?", new Object[]{"LV_Worksheet", worksheetid, worksheetversionid});
            boolean exists = false;
            for (int i = 0; i < approvals.size(); ++i) {
                String approvaltypeid = approvals.getValue(i, "approvaltypeid");
                if (approvaltypeid.equals(options.getProperty("worksheetapprovaltype"))) {
                    exists = true;
                    continue;
                }
                PropertyList delApprovalProps = new PropertyList();
                delApprovalProps.put("sdcid", "LV_Worksheet");
                delApprovalProps.put("keyid1", worksheetid);
                delApprovalProps.put("keyid2", worksheetversionid);
                delApprovalProps.put("approvaltypeid", approvaltypeid);
                ab.setAction("DeleteWorksheetApproval" + deleteApprovalType++, "DeleteSDIApproval", "1", delApprovalProps);
            }
            if (!exists) {
                PropertyList addApprovalProps = new PropertyList();
                addApprovalProps.put("sdcid", "LV_Worksheet");
                addApprovalProps.put("keyid1", worksheetid);
                addApprovalProps.put("keyid2", worksheetversionid);
                addApprovalProps.put("approvaltypeid", options.getProperty("worksheetapprovaltype"));
                ab.setAction("AddWorksheetApproval", "AddSDIApproval", "1", addApprovalProps);
            }
        } else {
            DataSet approvals = qp.getPreparedSqlDataSet("SELECT approvaltypeid FROM sdiapproval WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?", new Object[]{"LV_Worksheet", worksheetid, worksheetversionid});
            for (int i = 0; i < approvals.size(); ++i) {
                PropertyList delApprovalProps = new PropertyList();
                delApprovalProps.put("sdcid", "LV_Worksheet");
                delApprovalProps.put("keyid1", worksheetid);
                delApprovalProps.put("keyid2", worksheetversionid);
                delApprovalProps.put("approvaltypeid", approvals.getValue(i, "approvaltypeid"));
                ab.setAction("DeleteWorksheetApproval" + deleteApprovalType++, "DeleteSDIApproval", "1", delApprovalProps);
            }
        }
    }

    private static void checkNestedSectionApprovalTypes(QueryProcessor qp, String worksheetid, String worksheetversionid, String worksheetsectionid, String worksheetsectionversionid) throws SapphireException {
        DataSet sections = qp.getPreparedSqlDataSet("SELECT worksheetsectionid, worksheetsectionversionid, sectionlevel, options FROM worksheetsection WHERE worksheetid=? AND worksheetversionid=? ORDER BY usersequence", (Object[])new String[]{worksheetid, worksheetversionid}, true);
        HashMap<String, String> find = new HashMap<String, String>();
        find.put("worksheetsectionid", worksheetsectionid);
        find.put("worksheetsectionversionid", worksheetsectionversionid);
        int row = sections.findRow(find);
        if (row >= 0) {
            PropertyList suboptionsPL;
            String suboptions;
            int level;
            int i;
            int startlevel = sections.getInt(row, "sectionlevel");
            boolean nestedSectionError = false;
            boolean parentSectionError = false;
            for (i = row + 1; i < sections.size() && (level = sections.getInt(i, "sectionlevel")) > startlevel; ++i) {
                suboptions = sections.getValue(i, "options").trim();
                if (!suboptions.startsWith("<propertylist")) continue;
                suboptionsPL = new PropertyList();
                suboptionsPL.setPropertyList(suboptions);
                if (suboptionsPL.getProperty("sectioncompletion").equals("N")) continue;
                nestedSectionError = true;
            }
            for (i = row - 1; i >= 0 && (level = sections.getInt(i, "sectionlevel")) < startlevel; --i) {
                suboptions = sections.getValue(i, "options").trim();
                if (!suboptions.startsWith("<propertylist")) continue;
                suboptionsPL = new PropertyList();
                suboptionsPL.setPropertyList(suboptions);
                if (suboptionsPL.getProperty("sectioncompletion").equals("N")) continue;
                parentSectionError = true;
            }
            if (nestedSectionError) {
                throw new SapphireException("A sub-section has already been marked as requiring Completion");
            }
            if (parentSectionError) {
                throw new SapphireException("A parent section has already been marked as requiring Completion");
            }
        }
    }

    private void saveUserPrivs(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            PropertyList editProps = new PropertyList();
            editProps.setProperty("worksheetid", worksheetid);
            editProps.setProperty("worksheetversionid", worksheetversionid);
            editProps.setProperty("userprivs", commandRequest.getString("userprivs"));
            editProps.setProperty("elnrequest", "Y");
            this.getActionProcessor().processActionClass(EditWorksheet.class.getName(), editProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to save user privs. Reason: " + e.getMessage(), e);
        }
    }

    private void saveParameters(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        try {
            DataSet params = commandRequest.getDataSet("params");
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", commandRequest.getString("worksheetid"));
            actionProps.setProperty("worksheetversionid", commandRequest.getString("worksheetversionid"));
            actionProps.setProperty("worksheetitemid", worksheetitemid);
            actionProps.setProperty("worksheetitemversionid", worksheetitemversionid);
            actionProps.setProperty("paramname", params.getColumnValues("paramname", ";"));
            actionProps.setProperty("paramtitle", params.getColumnValues("paramtitle", ";"));
            actionProps.setProperty("parameditorstyleid", params.getColumnValues("parameditorstyleid", ";"));
            actionProps.setProperty("paramvalue", params.getColumnValues("paramvalue", ";"));
            actionProps.setProperty("valuesdcid", params.getColumnValues("valuesdcid", ";"));
            actionProps.setProperty("valuekeyid1", params.getColumnValues("valuekeyid1", ";"));
            actionProps.setProperty("valuekeyid2", params.getColumnValues("valuekeyid2", ";"));
            actionProps.setProperty("valuetype", params.getColumnValues("valuetype", ";"));
            actionProps.setProperty("valuelabel", params.getColumnValues("valuelabel", ";"));
            this.getActionProcessor().processActionClass(EditWorksheetItemParams.class.getName(), actionProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to save parameters for worksheet item " + BaseELNAction.getIdVersionText(worksheetitemid, worksheetitemversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private void addSDIAttributes(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String attributes = commandRequest.getString("attributes");
            String type = commandRequest.getString("type");
            ActionBlock ab = new ActionBlock();
            PropertyList actionProps = new PropertyList();
            if (worksheetitemid.length() > 0) {
                actionProps.setProperty("sdcid", "LV_WorksheetItem");
                actionProps.setProperty("attributesdcid", "LV_WorksheetItem");
                actionProps.setProperty("keyid1", worksheetitemid);
                actionProps.setProperty("keyid2", worksheetitemversionid);
            } else if (worksheetsectionid.length() > 0) {
                actionProps.setProperty("sdcid", "LV_WorksheetSection");
                actionProps.setProperty("attributesdcid", "LV_WorksheetSection");
                actionProps.setProperty("keyid1", worksheetsectionid);
                actionProps.setProperty("keyid2", worksheetsectionversionid);
            } else {
                actionProps.setProperty("sdcid", "LV_Worksheet");
                actionProps.setProperty("attributesdcid", "LV_Worksheet");
                actionProps.setProperty("keyid1", worksheetid);
                actionProps.setProperty("keyid2", worksheetversionid);
            }
            actionProps.setProperty("type", type);
            actionProps.setProperty("attributeid", attributes);
            ab.setAction("AddAttributes", "AddSDIAttribute", "1", actionProps);
            SafeSQL safeSQL = new SafeSQL();
            StringBuilder message = new StringBuilder();
            DataSet titles = this.getQueryProcessor().getPreparedSqlDataSet("SELECT attributeid, attributetitle  FROM sdcattributedef  WHERE sdcid=" + safeSQL.addVar(actionProps.getProperty("sdcid")) + " AND attributeid in (" + safeSQL.addIn(attributes, ";") + ")", safeSQL.getValues());
            for (int i = 0; i < titles.size(); ++i) {
                message.append(i > 0 ? ", " : "");
                String attributeid = titles.getValue(i, "attributeid");
                String title = titles.getValue(i, "attributetitle", attributeid);
                message.append((title.length() > 0 ? title + " " : "") + " (" + attributeid + ")");
            }
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", actionProps.getProperty("sdcid"));
            activityProps.setProperty("targetkeyid1", actionProps.getProperty("keyid1"));
            activityProps.setProperty("targetkeyid2", actionProps.getProperty("keyid2"));
            activityProps.setProperty("activitytype", "Add");
            activityProps.setProperty("activitylog", "Added Metadata: " + message);
            ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
            this.getActionProcessor().processActionBlock(ab);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add attributes. Reason: " + e.getMessage(), e);
        }
    }

    private void deleteSDIAttributes(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            ActionBlock ab = new ActionBlock();
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("sdcid", commandRequest.getString("sdcid"));
            deleteProps.setProperty("keyid1", commandRequest.getString("keyid1"));
            deleteProps.setProperty("keyid2", commandRequest.getString("keyid2"));
            deleteProps.setProperty("attributeid", commandRequest.getString("attributeid"));
            deleteProps.setProperty("attributesdcid", commandRequest.getString("attributesdcid"));
            deleteProps.setProperty("attributeinstance", commandRequest.getString("attributeinstance"));
            ab.setAction("DeleteAttribute", "DeleteSDIAttribute", "1", deleteProps);
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", commandRequest.getString("sdcid"));
            activityProps.setProperty("targetkeyid1", commandRequest.getString("keyid1"));
            activityProps.setProperty("targetkeyid2", commandRequest.getString("keyid2"));
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog", "Deleted attribute: " + commandRequest.getString("attributeid"));
            ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
            this.getActionProcessor().processActionBlock(ab);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to delete attribute. Reason: " + e.getMessage(), e);
        }
    }

    private void validateSDIAttributes(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet attributes = commandRequest.getDataSet("attributes");
            DateTimeUtil dtu = new DateTimeUtil(this.sapphireConnection);
            for (int i = 0; i < attributes.size(); ++i) {
                Calendar cal;
                HashMap<String, String> trans = new HashMap<String, String>();
                trans.put("title", attributes.getValue(i, "attributetitle"));
                String value = attributes.getValue(i, "value");
                if (value.length() <= 0) continue;
                if (attributes.getValue(i, "datatype").equals("N")) {
                    try {
                        BigDecimal bigDecimal = new BigDecimal(value);
                    }
                    catch (Exception e) {
                        attributes.setString(i, "error", this.getTranslationProcessor().translate("Value for '[title]' is not a valid number", trans));
                    }
                    continue;
                }
                if (!attributes.getValue(i, "datatype").equals("D") && !attributes.getValue(i, "datatype").equals("O") || (cal = dtu.getCalendar(value)) != null) continue;
                attributes.setString(i, "error", this.getTranslationProcessor().translate("Value for '[title]' is not a valid date", trans));
            }
            commandResponse.set("attributes", attributes);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add attributes. Reason: " + e.getMessage(), e);
        }
    }

    private void saveSDIAttributes(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            DataSet deleteAttributes;
            DataSet saveAttributes;
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            DataSet attributes = commandRequest.getDataSet("attributes");
            boolean template = commandRequest.getBoolean("template");
            if (attributes.getColumnType("_delete") == -1) {
                saveAttributes = attributes;
                deleteAttributes = null;
            } else {
                HashMap<String, String> filter = new HashMap<String, String>();
                filter.put("_delete", "Y");
                deleteAttributes = attributes.getFilteredDataSet(filter);
                saveAttributes = attributes.getFilteredDataSet(filter, true);
            }
            String priorOptions = "";
            priorOptions = worksheetitemid.length() > 0 ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheetitem WHERE worksheetitemid=? AND worksheetitemversionid=?", (Object[])new String[]{worksheetitemid, worksheetitemversionid}, true).getValue(0, "options") : (worksheetsectionid.length() > 0 ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheetsection WHERE worksheetsectionid=? AND worksheetsectionversionid=?", (Object[])new String[]{worksheetsectionid, worksheetsectionversionid}, true).getValue(0, "options") : this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheet WHERE worksheetid=? AND worksheetversionid=?", (Object[])new String[]{worksheetid, worksheetversionid}, true).getValue(0, "options"));
            if (saveAttributes.size() > 0) {
                PropertyList actionProps = this.saveSDIAttributes(worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid, worksheetitemid, worksheetitemversionid, saveAttributes, template);
                DataSet dependents = this.getQueryProcessor().getPreparedSqlDataSet("SELECT DISTINCT worksheetitemid, worksheetitemversionid FROM worksheetitemparam WHERE valuesdcid = ? AND valuekeyid1 = ? AND valuekeyid2 = ? AND valuetype = 'metadata' AND valuelabel IN ('" + StringUtil.replaceAll(actionProps.getProperty("attributeid"), ";", "','") + "')", new Object[]{actionProps.getProperty("sdcid"), actionProps.getProperty("keyid1"), actionProps.getProperty("keyid2")});
                if (dependents.size() > 0) {
                    commandResponse.set("dependentworksheetitemid", dependents.getColumnValues("worksheetitemid", ";"));
                    commandResponse.set("dependentworksheetitemversionid", dependents.getColumnValues("worksheetitemversionid", ";"));
                }
            }
            if (deleteAttributes != null && deleteAttributes.size() > 0) {
                ActionBlock ab = new ActionBlock();
                PropertyList deleteProps = new PropertyList();
                if (worksheetitemid.length() > 0) {
                    deleteProps.setProperty("sdcid", "LV_WorksheetItem");
                    deleteProps.setProperty("keyid1", worksheetitemid);
                    deleteProps.setProperty("keyid2", worksheetitemversionid);
                } else if (worksheetsectionid.length() > 0) {
                    deleteProps.setProperty("sdcid", "LV_WorksheetSection");
                    deleteProps.setProperty("keyid1", worksheetsectionid);
                    deleteProps.setProperty("keyid2", worksheetsectionversionid);
                } else {
                    deleteProps.setProperty("sdcid", "LV_Worksheet");
                    deleteProps.setProperty("keyid1", worksheetid);
                    deleteProps.setProperty("keyid2", worksheetversionid);
                }
                deleteProps.setProperty("attributeid", deleteAttributes.getColumnValues("attributeid", ";"));
                deleteProps.setProperty("attributesdcid", deleteAttributes.getColumnValues("attributesdcid", ";"));
                deleteProps.setProperty("attributeinstance", deleteAttributes.getColumnValues("attributeinstance", ";"));
                ab.setAction("DeleteAttribute", "DeleteSDIAttribute", "1", deleteProps);
                PropertyList activityProps = new PropertyList();
                activityProps.setProperty("worksheetid", worksheetid);
                activityProps.setProperty("worksheetversionid", worksheetversionid);
                activityProps.setProperty("targetsdcid", deleteProps.getProperty("sdcid"));
                activityProps.setProperty("targetkeyid1", deleteProps.getProperty("keyid1"));
                activityProps.setProperty("targetkeyid2", deleteProps.getProperty("keyid2"));
                activityProps.setProperty("activitytype", "Delete");
                activityProps.setProperty("activitylog", "Deleted attribute: " + commandRequest.getString("attributeid"));
                ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
                this.getActionProcessor().processActionBlock(ab);
            }
            String afterOptions = "";
            afterOptions = worksheetitemid.length() > 0 ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheetitem WHERE worksheetitemid=? AND worksheetitemversionid=?", (Object[])new String[]{worksheetitemid, worksheetitemversionid}, true).getValue(0, "options") : (worksheetsectionid.length() > 0 ? this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheetsection WHERE worksheetsectionid=? AND worksheetsectionversionid=?", (Object[])new String[]{worksheetsectionid, worksheetsectionversionid}, true).getValue(0, "options") : this.getQueryProcessor().getPreparedSqlDataSet("SELECT options FROM worksheet WHERE worksheetid=? AND worksheetversionid=?", (Object[])new String[]{worksheetid, worksheetversionid}, true).getValue(0, "options"));
            if (!priorOptions.equals(afterOptions)) {
                commandResponse.set("_refreshworksheet", "Y");
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add attributes. Reason: " + e.getMessage(), e);
        }
    }

    private PropertyList saveSDIAttributes(String worksheetid, String worksheetversionid, String worksheetsectionid, String worksheetsectionversionid, String worksheetitemid, String worksheetitemversionid, DataSet attributes, boolean template) throws ActionException {
        ActionBlock ab = new ActionBlock();
        PropertyList actionProps = new PropertyList();
        if (worksheetitemid.length() > 0) {
            actionProps.setProperty("sdcid", "LV_WorksheetItem");
            actionProps.setProperty("keyid1", worksheetitemid);
            actionProps.setProperty("keyid2", worksheetitemversionid);
        } else if (worksheetsectionid.length() > 0) {
            actionProps.setProperty("sdcid", "LV_WorksheetSection");
            actionProps.setProperty("keyid1", worksheetsectionid);
            actionProps.setProperty("keyid2", worksheetsectionversionid);
        } else {
            actionProps.setProperty("sdcid", "LV_Worksheet");
            actionProps.setProperty("keyid1", worksheetid);
            actionProps.setProperty("keyid2", worksheetversionid);
        }
        actionProps.setProperty("attributeid", attributes.getColumnValues("attributeid", ";"));
        actionProps.setProperty("attributeinstance", attributes.getColumnValues("attributeinstance", ";"));
        actionProps.setProperty("attributesdcid", attributes.getColumnValues("attributesdcid", ";"));
        actionProps.setProperty(template ? "defaultvalue" : "value", attributes.getColumnValues("value", ";"));
        actionProps.setProperty("mandatory", attributes.getColumnValues("mandatoryflag", ";"));
        actionProps.setProperty("updatable", attributes.getColumnValues("updateableflag", ";"));
        ab.setAction("EditAttributes", "EditSDIAttribute", "1", actionProps);
        PropertyList activityProps = new PropertyList();
        activityProps.setProperty("worksheetid", worksheetid);
        activityProps.setProperty("worksheetversionid", worksheetversionid);
        activityProps.setProperty("targetsdcid", actionProps.getProperty("sdcid"));
        activityProps.setProperty("targetkeyid1", actionProps.getProperty("keyid1"));
        activityProps.setProperty("targetkeyid2", actionProps.getProperty("keyid2"));
        activityProps.setProperty("activitytype", "Edit");
        StringBuilder log = new StringBuilder();
        for (int i = 0; i < attributes.size(); ++i) {
            String originalValue;
            String attributeid = attributes.getValue(i, "attributeid");
            String title = attributes.getValue(i, "attributetitle", attributeid);
            String instance = attributes.getValue(i, "attributeinstance").equals("1") ? "" : " Inst:" + attributes.getValue(i, "attributeinstance");
            String header = (title.length() > 0 ? title + " " : "") + "(" + attributeid + instance + ")";
            if (attributes.getValue(i, "_delete").equals("Y")) {
                log.append(", ");
                log.append(header + " Deleted");
                continue;
            }
            String string = originalValue = template ? attributes.getValue(i, "defaultdisplayvalue") : attributes.getValue(i, "displayvalue");
            if (attributes.getValue(i, "value").equals(originalValue)) continue;
            log.append(", ");
            log.append(header + ": " + (originalValue.length() > 0 ? originalValue : "(blank)") + " \ud83e\udc1a " + attributes.getValue(i, "value", "(blank)"));
        }
        activityProps.setProperty("activitylog", "Edited Metadata: " + (log.length() > 0 ? log.substring(2) : ""));
        ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
        this.getActionProcessor().processActionBlock(ab);
        return actionProps;
    }

    private void addSDIAttachment(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            String attachmentdesc = commandRequest.getString("attachmentdesc");
            PropertyList activityProps = new PropertyList();
            if (worksheetitemid.length() > 0) {
                activityProps.setProperty("targetsdcid", "LV_WorksheetItem");
                activityProps.setProperty("targetkeyid1", worksheetitemid);
                activityProps.setProperty("targetkeyid2", worksheetitemversionid);
            } else if (worksheetsectionid.length() > 0) {
                activityProps.setProperty("targetsdcid", "LV_WorksheetSection");
                activityProps.setProperty("targetkeyid1", worksheetsectionid);
                activityProps.setProperty("targetkeyid2", worksheetsectionversionid);
            } else {
                activityProps.setProperty("targetsdcid", "LV_Worksheet");
                activityProps.setProperty("targetkeyid1", worksheetid);
                activityProps.setProperty("targetkeyid2", worksheetversionid);
            }
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("activitytype", "Add");
            activityProps.setProperty("activitylog", "Added attachment: " + attachmentdesc);
            this.getActionProcessor().processActionClass(AddWorksheetActivity.class.getName(), activityProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add attachment (log). Reason: " + e.getMessage(), e);
        }
    }

    private void editSDIAttachment(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            Object[] update;
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            String worksheetsectionid = commandRequest.getString("worksheetsectionid");
            String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
            String worksheetitemid = commandRequest.getString("worksheetitemid");
            String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
            int attachmentnum = Integer.parseInt(commandRequest.getString("attachmentnum"));
            String attachmentdesc = commandRequest.getString("attachmentdesc");
            PropertyList activityProps = new PropertyList();
            if (worksheetitemid.length() > 0) {
                activityProps.setProperty("targetsdcid", "LV_WorksheetItem");
                activityProps.setProperty("targetkeyid1", worksheetitemid);
                activityProps.setProperty("targetkeyid2", worksheetitemversionid);
                update = new Object[]{attachmentdesc, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, attachmentnum};
            } else if (worksheetsectionid.length() > 0) {
                activityProps.setProperty("targetsdcid", "LV_WorksheetSection");
                activityProps.setProperty("targetkeyid1", worksheetsectionid);
                activityProps.setProperty("targetkeyid2", worksheetsectionversionid);
                update = new Object[]{attachmentdesc, "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, attachmentnum};
            } else {
                activityProps.setProperty("targetsdcid", "LV_Worksheet");
                activityProps.setProperty("targetkeyid1", worksheetid);
                activityProps.setProperty("targetkeyid2", worksheetversionid);
                update = new Object[]{attachmentdesc, "LV_Worksheet", worksheetid, worksheetversionid, attachmentnum};
            }
            database.executePreparedUpdate("UPDATE sdiattachment SET attachmentdesc = ? WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ? AND keyid3 = '(null)' AND attachmentnum = ?", update);
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("activitytype", "Edit");
            activityProps.setProperty("activitylog", "Edited attachment description: " + attachmentdesc);
            this.getActionProcessor().processActionClass(AddWorksheetActivity.class.getName(), activityProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to edit attachments. Reason: " + e.getMessage(), e);
        }
    }

    private void deleteSDIAttachment(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        try {
            String worksheetid = commandRequest.getString("worksheetid");
            String worksheetversionid = commandRequest.getString("worksheetversionid");
            ActionBlock ab = new ActionBlock();
            PropertyList deleteProps = new PropertyList();
            deleteProps.setProperty("sdcid", commandRequest.getString("sdcid"));
            deleteProps.setProperty("keyid1", commandRequest.getString("keyid1"));
            deleteProps.setProperty("keyid2", commandRequest.getString("keyid2"));
            if (commandRequest.getString("attachmentnum").length() > 0) {
                deleteProps.setProperty("attachmentnum", commandRequest.getString("attachmentnum"));
            }
            ab.setAction("DeleteAttachment", "DeleteSDIAttachment", "1", deleteProps);
            PropertyList activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", commandRequest.getString("sdcid"));
            activityProps.setProperty("targetkeyid1", commandRequest.getString("keyid1"));
            activityProps.setProperty("targetkeyid2", commandRequest.getString("keyid2"));
            activityProps.setProperty("activitytype", "Delete");
            if (commandRequest.getString("attachmentnum").length() > 0) {
                activityProps.setProperty("activitylog", "Deleted attachment: " + commandRequest.getString("attachmentnum"));
            } else {
                activityProps.setProperty("activitylog", "Deleted all attachments");
            }
            ab.setActionClass("ActivityLog", AddWorksheetActivity.class.getName(), activityProps);
            this.getActionProcessor().processActionBlock(ab);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to delete attachments. Reason: " + e.getMessage(), e);
        }
    }

    private void addContributor(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", worksheetid);
            actionProps.setProperty("worksheetversionid", worksheetversionid);
            actionProps.setProperty("contributorid", commandRequest.getString("contributorid"));
            this.getActionProcessor().processActionClass(AddWorksheetContributor.class.getName(), actionProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to add contributor to worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private void deleteContributor(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        try {
            PropertyList actionProps = new PropertyList();
            actionProps.setProperty("worksheetid", worksheetid);
            actionProps.setProperty("worksheetversionid", worksheetversionid);
            actionProps.setProperty("contributorid", commandRequest.getString("contributorid"));
            this.getActionProcessor().processActionClass(DeleteWorksheetContributor.class.getName(), actionProps);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to delete contributor from worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private void resetDetails(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        String worksheetsectionid = commandRequest.getString("worksheetsectionid");
        String worksheetsectionversionid = commandRequest.getString("worksheetsectionversionid");
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        boolean resetMetadata = commandRequest.getBoolean("resetmetadata");
        boolean resetMetadataValues = commandRequest.getBoolean("resetmetadatavalues");
        boolean resetNotes = commandRequest.getBoolean("resetnotes");
        boolean resetAttachments = commandRequest.getBoolean("resetattachments");
        boolean resetIncidents = commandRequest.getBoolean("resetincidents");
        boolean resetLIMSData = commandRequest.getBoolean("resetlimsdata");
        boolean applyAll = commandRequest.getBoolean("applyall");
        boolean applySubsections = commandRequest.getBoolean("applysubsections");
        boolean applyControls = commandRequest.getBoolean("applycontrols");
        boolean template = commandRequest.getBoolean("template");
        boolean controlTemplate = commandRequest.getBoolean("controltemplate");
        try {
            ActionBlock ab = new ActionBlock();
            if (worksheetitemid.length() > 0) {
                this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, resetMetadata, resetMetadataValues, resetNotes, resetAttachments, resetIncidents, resetLIMSData, template, controlTemplate, ab);
            } else if (worksheetsectionid.length() > 0) {
                this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, resetMetadata, resetMetadataValues, resetNotes, resetAttachments, resetIncidents, resetLIMSData, template, controlTemplate, ab);
                if (applyControls) {
                    DataSet items = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{worksheetsectionid, worksheetsectionversionid});
                    for (int i = 0; i < items.size(); ++i) {
                        this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetItem", items.getValue(i, "worksheetitemid"), items.getValue(i, "worksheetitemversionid"), resetMetadata, resetMetadataValues, resetNotes, resetAttachments, resetIncidents, resetLIMSData, template, controlTemplate, ab);
                    }
                }
                if (applySubsections) {
                    int level = Integer.parseInt(commandRequest.getString("sectionlevel", "1"));
                    DataSet sections = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetsectionid, worksheetsectionversionid, sectionlevel FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ? AND usersequence > ( SELECT usersequence FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?) ORDER BY usersequence", new Object[]{worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid});
                    for (int i = 0; i < sections.size() && sections.getInt(i, "sectionlevel") > level; ++i) {
                        this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetSection", sections.getValue(i, "worksheetsectionid"), sections.getValue(i, "worksheetsectionversionid"), resetMetadata, resetMetadataValues, resetNotes, resetAttachments, resetIncidents, resetLIMSData, template, controlTemplate, ab);
                        if (!applyControls) continue;
                        DataSet items = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{sections.getString(i, "worksheetsectionid"), sections.getString(i, "worksheetsectionversionid")});
                        for (int j = 0; j < items.size(); ++j) {
                            this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetItem", items.getValue(j, "worksheetitemid"), items.getValue(j, "worksheetitemversionid"), resetMetadata, resetMetadataValues, resetNotes, resetAttachments, resetIncidents, resetLIMSData, template, controlTemplate, ab);
                        }
                    }
                }
            } else {
                this.resetDetails(worksheetid, worksheetversionid, "LV_Worksheet", worksheetid, worksheetversionid, resetMetadata, resetMetadataValues, resetNotes, resetAttachments, resetIncidents, resetLIMSData, template, controlTemplate, ab);
                if (applyAll) {
                    DataSet sections = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetsectionid, worksheetsectionversionid FROM worksheetsection WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
                    for (int i = 0; i < sections.size(); ++i) {
                        this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetSection", sections.getValue(i, "worksheetsectionid"), sections.getValue(i, "worksheetsectionversionid"), resetMetadata, resetMetadataValues, resetNotes, resetAttachments, resetIncidents, resetLIMSData, template, controlTemplate, ab);
                    }
                    DataSet items = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
                    for (int i = 0; i < items.size(); ++i) {
                        this.resetDetails(worksheetid, worksheetversionid, "LV_WorksheetItem", items.getValue(i, "worksheetitemid"), items.getValue(i, "worksheetitemversionid"), resetMetadata, resetMetadataValues, resetNotes, resetAttachments, resetIncidents, resetLIMSData, template, controlTemplate, ab);
                    }
                }
            }
            this.getActionProcessor().processActionBlock(ab);
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to reset details for worksheet " + BaseELNAction.getIdVersionText(worksheetid, worksheetversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    private void resetDetails(String worksheetid, String worksheetversionid, String itemsdcid, String itemid, String itemversionid, boolean resetMetadata, boolean resetMetadataValues, boolean resetNotes, boolean resetAttachments, boolean resetIncidents, boolean resetLIMSData, boolean template, boolean controlTemplate, ActionBlock ab) throws SapphireException {
        PropertyList activityProps;
        if (resetMetadata || resetMetadataValues) {
            DataSet metadata = this.getQueryProcessor().getPreparedSqlDataSet("SELECT attributeid, attributeinstance FROM sdiattribute WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?", new Object[]{itemsdcid, itemid, itemversionid});
            for (int i = 0; i < metadata.size(); ++i) {
                PropertyList attrProps = new PropertyList();
                attrProps.setProperty("sdcid", itemsdcid);
                attrProps.setProperty("keyid1", itemid);
                attrProps.setProperty("keyid2", itemversionid);
                attrProps.setProperty("attributesdcid", itemsdcid);
                attrProps.setProperty("attributeid", metadata.getValue(i, "attributeid"));
                attrProps.setProperty("attributeinstance", metadata.getValue(i, "attributeinstance"));
                attrProps.setProperty(template ? "defaultvalue" : "value", "");
                ab.setAction("Attributes_" + itemid + metadata.getValue(i, "attributeid"), resetMetadata ? "DeleteSDIAttribute" : "EditSDIAttribute", resetMetadata ? "1" : "1", attrProps);
            }
            activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", itemsdcid);
            activityProps.setProperty("targetkeyid1", itemid);
            activityProps.setProperty("targetkeyid2", itemversionid);
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog", resetMetadata ? "Deleted all metadata" : "Reset all metadata values");
            ab.setActionClass("AttributeActivityLog_" + itemid, AddWorksheetActivity.class.getName(), activityProps);
        }
        if (resetNotes) {
            DataSet notes = this.getQueryProcessor().getPreparedSqlDataSet("SELECT notenum FROM sdinote WHERE sdcid = ? AND keyid1 = ? AND keyid2 = ?", new Object[]{itemsdcid, itemid, itemversionid});
            for (int i = 0; i < notes.size(); ++i) {
                PropertyList noteProps = new PropertyList();
                noteProps.setProperty("sdcid", itemsdcid);
                noteProps.setProperty("keyid1", itemid);
                noteProps.setProperty("keyid2", itemversionid);
                noteProps.setProperty("notenum", notes.getValue(i, "notenum"));
                ab.setActionClass("Notes_" + itemid + notes.getValue(i, "notenum"), DeleteSDINote.class.getName(), noteProps);
            }
            activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", itemsdcid);
            activityProps.setProperty("targetkeyid1", itemid);
            activityProps.setProperty("targetkeyid2", itemversionid);
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog", "Deleted all notes");
            ab.setActionClass("NotesActivityLog_" + itemid, AddWorksheetActivity.class.getName(), activityProps);
        }
        if (resetAttachments) {
            PropertyList attachProps = new PropertyList();
            attachProps.setProperty("sdcid", itemsdcid);
            attachProps.setProperty("keyid1", itemid);
            attachProps.setProperty("keyid2", itemversionid);
            ab.setAction("Attachments_" + itemid, "DeleteSDIAttachment", "1", attachProps);
            activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", itemsdcid);
            activityProps.setProperty("targetkeyid1", itemid);
            activityProps.setProperty("targetkeyid2", itemversionid);
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog", "Deleted all attachments");
            ab.setActionClass("AttachmentsActivityLog_" + itemid, AddWorksheetActivity.class.getName(), activityProps);
        }
        if (resetIncidents) {
            PropertyList incidentProps = new PropertyList();
            incidentProps.setProperty("sourcesdcid", itemsdcid);
            incidentProps.setProperty("sourcekeyid1", itemid);
            incidentProps.setProperty("sourcekeyid2", itemversionid);
            ab.setActionClass("incidents_" + itemid, RemoveIncidentSource.class.getName(), incidentProps);
            activityProps = new PropertyList();
            activityProps.setProperty("worksheetid", worksheetid);
            activityProps.setProperty("worksheetversionid", worksheetversionid);
            activityProps.setProperty("targetsdcid", itemsdcid);
            activityProps.setProperty("targetkeyid1", itemid);
            activityProps.setProperty("targetkeyid2", itemversionid);
            activityProps.setProperty("activitytype", "Delete");
            activityProps.setProperty("activitylog", "Removed From Incident");
            ab.setActionClass("IncidentActivityLog_" + itemid, AddWorksheetActivity.class.getName(), activityProps);
        }
        if (resetLIMSData && !itemsdcid.equals("LV_WorksheetSection")) {
            PropertyList limsProps = new PropertyList();
            limsProps.setProperty("worksheetid", worksheetid);
            limsProps.setProperty("worksheetversionid", worksheetversionid);
            if (controlTemplate || itemsdcid.equals("LV_Worksheet")) {
                ab.setActionClass("LIMSData_" + itemid, DeleteWorksheetSDI.class.getName(), limsProps);
            } else {
                limsProps.setProperty("worksheetitemid", itemid);
                limsProps.setProperty("worksheetitemversionid", itemversionid);
                ab.setActionClass("LIMSData_" + itemid, DeleteWorksheetItemSDI.class.getName(), limsProps);
            }
        }
    }

    private void executeActionBlock(CommandRequest commandRequest, CommandResponse commandResponse, DBUtil database) {
        String worksheetid = commandRequest.getString("worksheetid");
        String worksheetversionid = commandRequest.getString("worksheetversionid");
        String worksheetitemid = commandRequest.getString("worksheetitemid");
        String worksheetitemversionid = commandRequest.getString("worksheetitemversionid");
        String keyid1 = commandRequest.getString("keyid1");
        String keyid2 = commandRequest.getString("keyid2");
        String keyid3 = commandRequest.getString("keyid3");
        PropertyList operation = commandRequest.getPropertyList("operation");
        String sdcOperation = operation.getProperty("sdcoperation");
        PropertyList filter = operation.getPropertyListNotNull("sdifilter");
        try {
            String actionblock = commandRequest.getString("actionblock");
            actionblock = StringUtil.replaceAll(actionblock, "[worksheetid]", worksheetid);
            actionblock = StringUtil.replaceAll(actionblock, "[worksheetversionid]", worksheetversionid);
            actionblock = StringUtil.replaceAll(actionblock, "[worksheetitemid]", worksheetitemid);
            actionblock = StringUtil.replaceAll(actionblock, "[worksheetitemversionid]", worksheetitemversionid);
            DataSet item = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM worksheetitem WHERE worksheetitemid = ? AND worksheetitemversionid = ?", new Object[]{worksheetitemid, worksheetitemversionid}, true);
            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(this.sapphireConnection, database, (HashMap)item.get(0));
            boolean cont = true;
            if (worksheetItem.getWorksheetItemOptions().getOption("supportssdis").equals("Y")) {
                String sdcid = worksheetItem.getWorksheetItemOptions().getOption("defaultsdcid");
                actionblock = StringUtil.replaceAll(actionblock, "[sdcid]", sdcid);
                PropertyList config = worksheetItem.getConfig();
                String source = commandRequest.getString("source", config.getProperty("source", "Control"));
                DataSet sdiList = null;
                sdiList = source.equalsIgnoreCase("worksheet") ? this.loadWorksheetSDIs(worksheetid, worksheetversionid, sdcid, null, filter, keyid1, keyid2, keyid3) : this.loadItemSDIs(worksheetitemid, worksheetitemversionid, sdcid, null, filter, keyid1, keyid2, keyid3);
                if (sdcOperation.length() > 0 && sdiList.size() > 0 && !this.hasSecurityAccess(sdcid, sdiList, sdcOperation)) {
                    commandResponse.setStatusFail("This operation is not permitted on the " + this.getSDCProcessor().getProperty(sdcid, "plural") + " in this control");
                    cont = false;
                }
                actionblock = StringUtil.replaceAll(actionblock, "[keyid1]", sdiList.getColumnValues("keyid1", ";"));
                actionblock = StringUtil.replaceAll(actionblock, "[keyid2]", sdiList.getColumnValues("keyid2", ";"));
                actionblock = StringUtil.replaceAll(actionblock, "[keyid3]", sdiList.getColumnValues("keyid3", ";"));
            }
            if (cont) {
                ActionBlock ab = new ActionBlock(actionblock);
                this.getActionProcessor().processActionBlock(ab);
            }
        }
        catch (Exception e) {
            commandResponse.setStatusFail("Failed to execute action block for worksheet item " + BaseELNAction.getIdVersionText(worksheetitemid, worksheetitemversionid) + ". Reason: " + e.getMessage(), e);
        }
    }

    public static void updateMRUList(com.labvantage.sapphire.admin.system.ConfigurationProcessor configurationProcessor, String sysuserid, String propertyid, String key) throws SapphireException {
        String mruList = configurationProcessor.getProfileProperty(sysuserid, propertyid);
        int pos = mruList.indexOf(key);
        if (pos == -1) {
            mruList = key + (mruList.length() > 0 ? "|" + mruList : "");
        } else if (pos > 0) {
            int pos2 = mruList.indexOf("|", pos + 1);
            mruList = key + "|" + mruList.substring(0, pos - 1) + (pos2 > -1 ? mruList.substring(pos2) : "");
        }
        if (mruList.length() > 200) {
            mruList = mruList.substring(0, mruList.lastIndexOf("|"));
        }
        configurationProcessor.setProfileProperty(sysuserid, propertyid, mruList);
    }

    private DataSet loadMRUList(String propertyid, String query, int limit) throws SapphireException {
        com.labvantage.sapphire.admin.system.ConfigurationProcessor cp = new com.labvantage.sapphire.admin.system.ConfigurationProcessor(this.sapphireConnection.getConnectionId());
        String[] recent = StringUtil.split(cp.getProfileProperty(this.sapphireConnection.getSysuserId(), propertyid), "|");
        DataSet recents = new DataSet();
        StringBuffer where = new StringBuffer();
        SafeSQL safeSQL = new SafeSQL();
        if (recent.length > 0 && recent[0].length() > 0) {
            for (int i = 0; i < recent.length; ++i) {
                String[] parts = StringUtil.split(recent[i], ";");
                if (parts.length != 2) continue;
                where.append(" OR ").append("(worksheetid=").append(safeSQL.addVar(parts[0])).append(" AND worksheetversionid=").append(safeSQL.addVar(parts[1])).append(")");
            }
            DataSet temp = this.getQueryProcessor().getPreparedSqlDataSet("SELECT worksheetid, worksheetversionid, worksheetname, worksheetstatus, authorid, authordt, templateprivacyflag, " + Worksheet.getNoteStatusClause("LV_Worksheet", "worksheet", "worksheetid", "worksheetversionid") + " FROM worksheet WHERE ( ( templateprivacyflag = 'G' AND versionstatus IN (" + this.getGlobalVersionStatusList() + " ) ) OR templateprivacyflag IS NULL OR ( templateprivacyflag = 'O' AND worksheet.versionstatus IN ('P', 'A', 'C') ) )  AND (" + where.substring(4) + ")" + (query.length() > 0 ? " AND Lower( worksheetname ) like " + safeSQL.addVar("'%" + query.toLowerCase() + "%'") : ""), safeSQL.getValues());
            HashMap<String, String> findMap = new HashMap<String, String>();
            for (int i = 0; i < recent.length && recents.size() <= limit; ++i) {
                String[] parts = StringUtil.split(recent[i], ";");
                if (parts.length != 2) continue;
                findMap.put("worksheetid", parts[0]);
                findMap.put("worksheetversionid", parts[1]);
                int findRow = temp.findRow(findMap);
                if (findRow == -1) continue;
                recents.copyRow(temp, findRow, 1);
            }
        }
        return recents;
    }

    private String getGlobalVersionStatusList() {
        try {
            boolean isELNAdmin = this.sapphireConnection.getRoleList().contains("ELNAdmin");
            PropertyList policy = this.getConfigurationProcessor().getPolicy("ELNPolicy", "Sapphire Custom");
            PropertyList globalTemplates = policy.getPropertyListNotNull("globaltemplates");
            boolean requiresApproval = globalTemplates.getProperty("requiresapproval", "N").equals("Y");
            return isELNAdmin || !requiresApproval ? "'C', 'P', 'A'" : "'C'";
        }
        catch (SapphireException e) {
            this.logError("Failed to load ELN policy: " + e.getMessage(), e);
            return "'C', 'P'";
        }
    }

    private void signPdfDocument(CommandRequest commandRequest, CommandResponse commandResponse) {
        try {
            PropertyList actionProps = commandRequest.getStringPropertyList();
            this.getActionProcessor().processActionClass(SignWorksheet.class.getName(), actionProps);
            commandResponse.set("message", "PDF document is signed successfully");
        }
        catch (Exception e) {
            commandResponse.set("message", e.getMessage());
        }
    }
}

