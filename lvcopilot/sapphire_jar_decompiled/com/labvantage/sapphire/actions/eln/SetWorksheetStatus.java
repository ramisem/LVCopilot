/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemStatus;
import com.labvantage.sapphire.actions.sdiapproval.ApprovalRuleEvaluator;
import com.labvantage.sapphire.modules.eln.gwt.server.AddWorksheetActivity;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class SetWorksheetStatus
extends BaseELNAction {
    private String boldstart = "";
    private String boldend = "";

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String activityLogPrefixText;
        String currentStatus;
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String newStatus = properties.getProperty("status");
        this.boldstart = properties.getProperty("htmlerror").equals("Y") ? "<b>" : "";
        this.boldend = properties.getProperty("htmlerror").equals("Y") ? "</b>" : "";
        ActionBlock ab = new ActionBlock();
        PropertyList wsProps = new PropertyList();
        wsProps.setProperty("sdcid", "LV_Worksheet");
        wsProps.setProperty("keyid1", worksheetid);
        wsProps.setProperty("keyid2", worksheetversionid);
        String tracelogid = properties.getProperty("tracelogid");
        if (tracelogid.length() == 0 && properties.getProperty("auditreason").length() > 0) {
            try {
                AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                tracelogid = audit.addSDITraceLogEntry("LV_Worksheet", worksheetid, worksheetversionid, "", properties.getProperty("auditreason"), properties.getProperty("auditactivity"), properties.getProperty("auditsignedflag"), "N", "EditSection", true);
            }
            catch (ServiceException audit) {
                // empty catch block
            }
        }
        wsProps.setProperty("tracelogid", tracelogid);
        wsProps.setProperty("worksheetstatus", newStatus);
        wsProps.setProperty("worksheet_action", "Y");
        ab.setAction("EditWorksheet", "EditSDI", "1", wsProps);
        this.database.createPreparedResultSet("SELECT worksheetstatus, options FROM worksheet WHERE worksheetid = ? AND worksheetversionid = ?", new Object[]{worksheetid, worksheetversionid});
        if (this.database.getNext()) {
            currentStatus = this.database.getValue("worksheetstatus");
            if (newStatus.equals("Complete") && !currentStatus.equals("PendingApproval") || newStatus.equals("PendingApproval")) {
                wsProps.setProperty("contentcompletedby", this.connectionInfo.getSysuserId());
                wsProps.setProperty("contentcompleteddt", "n");
                wsProps.setProperty("contentcompletedreason", properties.getProperty("auditreason"));
                wsProps.setProperty("contentcompletedtracelogid", tracelogid);
            } else if (newStatus.equals("InProgress")) {
                wsProps.setProperty("contentcompletedby", "(null)");
                wsProps.setProperty("contentcompleteddt", "(null)");
                wsProps.setProperty("contentcompletedreason", "(null)");
                wsProps.setProperty("contentcompletedtracelogid", "(null)");
            }
            PropertyList wsOptions = new PropertyList();
            wsOptions.setPropertyList(this.database.getClob("options"));
            this.logger.info("Setting worksheet " + SetWorksheetStatus.getIdVersionText(worksheetid, worksheetversionid) + " status to " + newStatus);
            ErrorHandler errorHandler = this.getErrorHandler();
            DataSet sections = null;
            DataSet items = null;
            activityLogPrefixText = "";
            if (!currentStatus.equals("Pending") || !newStatus.equals("InProgress")) {
                SDIData worksheetData = this.loadWorksheet(worksheetid, worksheetversionid, properties);
                SDIData sectionData = worksheetData.getSDIData("sections");
                sections = sectionData.getDataset("primary");
                SDIData itemData = worksheetData.getSDIData("items");
                items = itemData.getDataset("primary");
                this.validateStatusChange(sections, items, currentStatus, newStatus, errorHandler);
                if (errorHandler.size() > 0) {
                    throw new SapphireException();
                }
                if (newStatus.equals("Complete") || newStatus.equals("PendingApproval")) {
                    DataSet notes;
                    String worksheetAttributeMessage;
                    int count;
                    boolean worksheetCompletionRequiresIncidentsClosed = wsOptions.getProperty("worksheetcompletionrequiresincidentsclosed", "Y").equals("Y");
                    boolean worksheetCompletionRequiresAllComplete = wsOptions.getProperty("worksheetcompletionrequiresallcomplete", "N").equals("Y");
                    boolean worksheetCompletionIncludesDisabled = wsOptions.getProperty("worksheetcompletionincludesdisabled", "Y").equals("Y");
                    boolean worksheetCompletionIncludesHidden = wsOptions.getProperty("worksheetcompletionincludeshidden", "N").equals("Y");
                    StringBuilder wssKeyid1 = new StringBuilder();
                    StringBuilder wssKeyid2 = new StringBuilder();
                    StringBuilder wssBehaviorType = new StringBuilder();
                    if (newStatus.equals("Complete") && wsOptions.getProperty("blockflag", "Y").equals("Y")) {
                        String blocksdcid = wsOptions.getProperty("blocksdcid");
                        this.unblockSDIs(worksheetid, worksheetversionid, blocksdcid, ab);
                    }
                    DataSet incidents = null;
                    if (worksheetCompletionRequiresIncidentsClosed) {
                        incidents = this.getQueryProcessor().getPreparedSqlDataSet("SELECT ii.incidentid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2 FROM incident i, incidentitem ii  WHERE i.incidentid=ii.incidentid  AND ii.sourcesdcid in (?,?,? ) AND ii.causalobjectflag='Y'  AND i.incidentstatus not in( 'Completed','Closed','Cancelled' ) AND i.incidentid in  ( SELECT incidentid    FROM incidentitem    WHERE sourcesdcid=? AND sourcekeyid1=? AND sourcekeyid2=? )", (Object[])new String[]{"LV_Worksheet", "LV_WorksheetSection", "LV_WorksheetItem", "LV_Worksheet", worksheetid, worksheetversionid});
                    }
                    if (incidents != null && (count = this.getIncidentsNotClosedCount(incidents, "LV_Worksheet", worksheetid, worksheetversionid)) > 0) {
                        errorHandler.addValidationError(this.boldstart + "Worksheet" + this.boldend + " has " + count + " active incident" + (count == 1 ? "." : "s."));
                    }
                    if ((worksheetAttributeMessage = BaseELNAction.validateMetaData(this.getQueryProcessor(), "LV_Worksheet", worksheetid, worksheetversionid)).length() > 0) {
                        errorHandler.addValidationError(this.boldstart + "Worksheet " + this.boldend + " " + worksheetAttributeMessage);
                    }
                    for (int i = 0; i < sections.size(); ++i) {
                        int count2;
                        String message;
                        boolean checkItemsToo = true;
                        String loopSectionId = sections.getValue(i, "worksheetsectionid");
                        String loopSectionVersionid = sections.getValue(i, "worksheetsectionversionid");
                        String loopSectionStatus = sections.getValue(i, "sectionstatus");
                        boolean includeSection = true;
                        String sectionBehavior = sections.getValue(i, "_behavior");
                        if (sectionBehavior.equals("hide") && !worksheetCompletionIncludesHidden) {
                            includeSection = false;
                        } else if (sectionBehavior.equals("disable") && !worksheetCompletionIncludesDisabled) {
                            includeSection = false;
                        }
                        if (includeSection && (message = BaseELNAction.validateMetaData(this.getQueryProcessor(), "LV_WorksheetSection", loopSectionId, loopSectionVersionid)).length() > 0) {
                            errorHandler.addValidationError(this.boldstart + "Section " + sections.getValue(i, "worksheetsectiondesc") + this.boldend + " " + message);
                            checkItemsToo = false;
                        }
                        if (includeSection && incidents != null && (count2 = this.getIncidentsNotClosedCount(incidents, "LV_WorksheetSection", loopSectionId, loopSectionVersionid)) > 0) {
                            errorHandler.addValidationError(this.boldstart + "Section " + sections.getValue(i, "worksheetsectiondesc") + this.boldend + " has " + count2 + " active incident" + (count2 == 1 ? "." : "s."));
                            checkItemsToo = false;
                        }
                        if (!checkItemsToo) continue;
                        if (includeSection) {
                            PropertyList wssOptions;
                            try {
                                wssOptions = new PropertyList(new JSONObject(sections.getClob(i, "options", "{}")));
                            }
                            catch (JSONException e) {
                                wssOptions = new PropertyList();
                                this.logger.error("Failed to parse section JSON options", e);
                            }
                            String sectionCompletionRule = wssOptions.getProperty("sectioncompletion", "N");
                            if (sectionCompletionRule.equals("A") || worksheetCompletionRequiresAllComplete && sectionCompletionRule.equals("C")) {
                                if (!loopSectionStatus.equals("Complete")) {
                                    errorHandler.addValidationError(this.boldstart + "Section " + sections.getValue(i, "worksheetsectiondesc") + this.boldend + " status needs to be " + "Complete");
                                }
                            } else if (!loopSectionStatus.equals("Complete")) {
                                wssKeyid1.append(";").append(loopSectionId);
                                wssKeyid2.append(";").append(loopSectionVersionid);
                                wssBehaviorType.append(";").append(sectionBehavior);
                            }
                        } else if (!loopSectionStatus.equals("Complete")) {
                            wssKeyid1.append(";").append(loopSectionId);
                            wssKeyid2.append(";").append(loopSectionVersionid);
                            wssBehaviorType.append(";").append(sectionBehavior);
                        }
                        HashMap<String, String> sectionItemsFilter = new HashMap<String, String>();
                        sectionItemsFilter.put("worksheetsectionid", loopSectionId);
                        sectionItemsFilter.put("worksheetsectionversionid", loopSectionVersionid);
                        DataSet sectionitems = items.getFilteredDataSet(sectionItemsFilter);
                        for (int j = 0; j < sectionitems.size(); ++j) {
                            int count3;
                            String worksheetitemid = sectionitems.getValue(j, "worksheetitemid");
                            String worksheetitemversionid = sectionitems.getValue(j, "worksheetitemversionid");
                            String itemStatus = sectionitems.getValue(j, "itemstatus");
                            String itemMessage = "";
                            if (includeSection && incidents != null && (count3 = this.getIncidentsNotClosedCount(incidents, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid)) > 0) {
                                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)sectionitems.get(j));
                                errorHandler.addValidationError(this.boldstart + "Control " + worksheetItem.getName(true) + this.boldend + " has " + count3 + " active incident" + (count3 == 1 ? "." : "s."));
                            }
                            if (includeSection && !itemStatus.equals("Complete")) {
                                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)sectionitems.get(j));
                                WorksheetItemOptions itemOptions = worksheetItem.getWorksheetItemOptions();
                                if (worksheetCompletionRequiresAllComplete && itemOptions.getOption("itemcompletion").equals("C")) {
                                    itemMessage = "status needs to be Complete";
                                }
                                if (itemMessage.length() == 0) {
                                    itemMessage = worksheetItem.checkCompleteness(itemStatus);
                                }
                                if (itemMessage.length() > 0) {
                                    errorHandler.addValidationError(this.boldstart + "Control " + worksheetItem.getName(true) + this.boldend + " " + itemMessage);
                                }
                            }
                            if (itemMessage.length() != 0) continue;
                            PropertyList wsiProps = new PropertyList();
                            wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                            wsiProps.setProperty("status", "Complete");
                            wsiProps.setProperty("tracelogid", tracelogid);
                            wsiProps.setProperty("auditreason", properties.getProperty("auditreason"));
                            wsiProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
                            wsiProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                            wsiProps.setProperty("bypassstatuscheck", "Y");
                            wsiProps.setProperty("bypassavailabilitycheck", "Y");
                            if (newStatus.equals("Complete") && wsOptions.getProperty("savehtmloncomplete", "I").equals("W")) {
                                wsiProps.setProperty("savehtml", "Y");
                            }
                            wsiProps.setProperty("worksheetid", worksheetid);
                            wsiProps.setProperty("worksheetversionid", worksheetversionid);
                            wsiProps.setProperty("worksheetitemid", worksheetitemid);
                            wsiProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                            ab.setActionClass("EditItem_" + worksheetitemid + worksheetitemversionid, SetWorksheetItemStatus.class.getName(), wsiProps);
                        }
                    }
                    if (wssKeyid1.length() > 0) {
                        PropertyList wssProps = new PropertyList();
                        wssProps.setProperty("sdcid", "LV_WorksheetSection");
                        wssProps.setProperty("sectionstatus", "Complete");
                        wssProps.setProperty("worksheet_action", "Y");
                        wssProps.setProperty("tracelogid", tracelogid);
                        wssProps.setProperty("contentcompletedby", this.connectionInfo.getSysuserId());
                        wssProps.setProperty("contentcompleteddt", "n");
                        wssProps.setProperty("contentcompletedreason", properties.getProperty("auditreason"));
                        wssProps.setProperty("contentcompletedtracelogid", tracelogid);
                        wssProps.setProperty("keyid1", wssKeyid1.substring(1));
                        wssProps.setProperty("keyid2", wssKeyid2.substring(1));
                        wssProps.setProperty("behaviortype", wssBehaviorType.substring(1));
                        ab.setAction("EditSections", "EditSDI", "1", wssProps);
                        PropertyList wssActivityProps = new PropertyList();
                        wssActivityProps.setProperty("worksheetid", worksheetid);
                        wssActivityProps.setProperty("worksheetversionid", worksheetversionid);
                        wssActivityProps.setProperty("targetsdcid", "LV_WorksheetSection");
                        wssActivityProps.setProperty("targetkeyid1", wssKeyid1.substring(1));
                        wssActivityProps.setProperty("targetkeyid2", wssKeyid2.substring(1));
                        wssActivityProps.setProperty("activitytype", "SetStatus");
                        wssActivityProps.setProperty("activitylog", "Worksheet completion - Section status set to Complete");
                        ab.setActionClass("EditSectionsActivityLog", AddWorksheetActivity.class.getName(), wssActivityProps);
                    }
                    if (newStatus.equals("Complete") && (notes = SetWorksheetStatus.getWorksheetNotes(this.getQueryProcessor(), worksheetid, worksheetversionid, "followupflag = 'Y' AND resolvedflag = 'N'")).size() > 0) {
                        errorHandler.addValidationError("Followup notes exist that have not been resolved");
                    }
                } else if (newStatus.equals("Approve")) {
                    if (currentStatus.equals("PendingApproval")) {
                        DataSet notes = SetWorksheetStatus.getWorksheetNotes(this.getQueryProcessor(), worksheetid, worksheetversionid, "followupflag = 'Y' AND resolvedflag = 'N'");
                        if (notes.size() == 0) {
                            DataSet approvalsteps = worksheetData.getDataset("approvalstep");
                            DataSet approvaltypes = worksheetData.getDataset("approval");
                            if (approvalsteps.size() > 0) {
                                boolean moreApprovals = false;
                                if (approvaltypes.size() == 1) {
                                    ApprovalRuleEvaluator evaluator = new ApprovalRuleEvaluator();
                                    for (int i = 0; i < approvalsteps.size(); ++i) {
                                        evaluator.addStep(approvalsteps.getValue(i, "stepstatusflag").equals("C") ? "P" : approvalsteps.getValue(i, "approvalflag"), approvalsteps.getValue(i, "mandatoryflag"));
                                    }
                                    moreApprovals = !"P".equals(evaluator.evaluateRule(approvaltypes.getValue(0, "passrule"), true));
                                } else {
                                    for (int i = 0; !moreApprovals && i < approvalsteps.size(); ++i) {
                                        if (approvalsteps.getValue(i, "stepstatusflag").equals("C") || !approvalsteps.getValue(i, "approvalflag").equals("U")) continue;
                                        moreApprovals = true;
                                    }
                                }
                                newStatus = moreApprovals ? "PendingApproval" : "Complete";
                                wsProps.setProperty("worksheetstatus", newStatus);
                                if (!moreApprovals && wsOptions.getProperty("savehtmloncomplete", "I").equals("W")) {
                                    for (int j = 0; j < items.size(); ++j) {
                                        PropertyList wsiProps = new PropertyList();
                                        wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                                        wsiProps.setProperty("status", "Complete");
                                        wsiProps.setProperty("tracelogid", tracelogid);
                                        wsiProps.setProperty("bypassstatuscheck", "Y");
                                        wsiProps.setProperty("bypassavailabilitycheck", "Y");
                                        wsiProps.setProperty("savehtml", "Y");
                                        wsiProps.setProperty("worksheetid", worksheetid);
                                        wsiProps.setProperty("worksheetversionid", worksheetversionid);
                                        wsiProps.setProperty("worksheetitemid", items.getValue(j, "worksheetitemid"));
                                        wsiProps.setProperty("worksheetitemversionid", items.getValue(j, "worksheetitemversionid"));
                                        ab.setActionClass("EditItem_" + items.getValue(j, "worksheetitemid") + items.getValue(j, "worksheetitemversionid"), SetWorksheetItemStatus.class.getName(), wsiProps);
                                    }
                                }
                                StringBuffer approvaltypeList = new StringBuffer();
                                StringBuffer approvalstepList = new StringBuffer();
                                StringBuffer approvalinstList = new StringBuffer();
                                StringBuffer approvalflagList = new StringBuffer();
                                for (int i = 0; i < approvalsteps.size(); ++i) {
                                    if (!approvalsteps.getValue(i, "stepstatusflag").equals("C")) continue;
                                    approvaltypeList.append(";").append(approvalsteps.getValue(i, "approvaltypeid"));
                                    approvalstepList.append(";").append(approvalsteps.getValue(i, "approvalstep"));
                                    approvalinstList.append(";").append(approvalsteps.getValue(i, "approvalstepinstance"));
                                    approvalflagList.append(";").append("P");
                                }
                                if (approvaltypeList.length() > 0) {
                                    HashMap<String, String> approvalProps = new HashMap<String, String>();
                                    approvalProps.put("sdcid", "LV_Worksheet");
                                    approvalProps.put("keyid1", worksheetid);
                                    approvalProps.put("keyid2", worksheetversionid);
                                    approvalProps.put("approvaltypeid", approvaltypeList.substring(1));
                                    approvalProps.put("approvalstep", approvalstepList.substring(1));
                                    approvalProps.put("approvalstepinstance", approvalinstList.substring(1));
                                    approvalProps.put("approvalflag", approvalflagList.substring(1));
                                    ab.setAction("approvesdistep", "ApproveSDIStep", "1", approvalProps);
                                }
                            }
                        } else {
                            errorHandler.addValidationError("Followup notes exist that have not been resolved");
                        }
                    } else {
                        errorHandler.addValidationError("Worksheet is not in a PendingApproval state");
                    }
                } else if (newStatus.equals("Reject")) {
                    if (currentStatus.equals("PendingApproval")) {
                        DataSet notes = SetWorksheetStatus.getWorksheetNotes(this.getQueryProcessor(), worksheetid, worksheetversionid, "followupflag = 'Y' AND resolvedflag = 'N'");
                        if (notes.size() > 0) {
                            newStatus = "InProgress";
                            wsProps.setProperty("worksheetstatus", newStatus);
                            this.addResetRootSection(worksheetid, worksheetversionid, sections, items, ab, errorHandler);
                            this.addResetApproval(ab, "LV_Worksheet", worksheetid, worksheetversionid, worksheetData.getDataset("approvalstep"));
                            activityLogPrefixText = "Worksheet rejected. ";
                        } else {
                            errorHandler.addValidationError("No followup notes have been defined");
                        }
                    } else {
                        errorHandler.addValidationError("Worksheet is not in a PendingApproval state");
                    }
                } else if (newStatus.equals("InProgress")) {
                    this.addResetRootSection(worksheetid, worksheetversionid, sections, items, ab, errorHandler);
                    this.addResetApproval(ab, "LV_Worksheet", worksheetid, worksheetversionid, worksheetData.getDataset("approvalstep"));
                }
            }
            if (errorHandler.size() > 0) {
                throw new SapphireException();
            }
        } else {
            throw new SapphireException("Worksheet " + SetWorksheetStatus.getIdVersionText(worksheetid, worksheetversionid) + " not found");
        }
        this.getActionProcessor().processActionBlock(ab);
        this.addActivityLog(worksheetid, worksheetversionid, "SetStatus", "LV_Worksheet", worksheetid, worksheetversionid, activityLogPrefixText + "Changed worksheet status from " + currentStatus + " to " + newStatus);
    }

    private void validateStatusChange(DataSet sections, DataSet items, String fromStatus, String toStatus, ErrorHandler errorHandler) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        for (int i = 0; i < sections.size(); ++i) {
            filter.put("worksheetsectionid", sections.getValue(i, "worksheetsectionid"));
            filter.put("worksheetsectionversionid", sections.getValue(i, "worksheetsectionversionid"));
            DataSet sectionitems = items.getFilteredDataSet(filter);
            for (int j = 0; j < sectionitems.size(); ++j) {
                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)sectionitems.get(j));
                String message = worksheetItem.validateWorksheetStateChange(fromStatus, toStatus);
                if (message.length() <= 0) continue;
                String itemName = worksheetItem.getName(true);
                if (itemName.equalsIgnoreCase("Reagents")) {
                    itemName = "Consumables";
                }
                errorHandler.addValidationError(this.boldstart + "Control " + itemName + this.boldend + " " + message);
            }
        }
    }

    private void addResetRootSection(String worksheetid, String worksheetversionid, DataSet sections, DataSet items, ActionBlock ab, ErrorHandler errorHandler) throws SapphireException {
        HashMap<String, String> filter = new HashMap<String, String>();
        PropertyList wssProps = new PropertyList();
        wssProps.setProperty("sdcid", "LV_WorksheetSection");
        wssProps.setProperty("keyid1", sections.getValue(0, "worksheetsectionid"));
        wssProps.setProperty("keyid2", sections.getValue(0, "worksheetsectionversionid"));
        wssProps.setProperty("sectionstatus", "InProgress");
        wssProps.setProperty("worksheet_action", "Y");
        ab.setAction("EditSections", "EditSDI", "1", wssProps);
        filter.put("worksheetsectionid", sections.getValue(0, "worksheetsectionid"));
        filter.put("worksheetsectionversionid", sections.getValue(0, "worksheetsectionversionid"));
        DataSet sectionitems = items.getFilteredDataSet(filter);
        for (int i = 0; i < sectionitems.size(); ++i) {
            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)sectionitems.get(i));
            String message = worksheetItem.validateStateChange("Complete", "InProgress");
            if (message.length() == 0) {
                PropertyList wsiProps = new PropertyList();
                wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                wsiProps.setProperty("status", "InProgress");
                wsiProps.setProperty("bypassstatuscheck", "Y");
                wsiProps.setProperty("bypassavailabilitycheck", "Y");
                wsiProps.setProperty("worksheetid", worksheetid);
                wsiProps.setProperty("worksheetversionid", worksheetversionid);
                wsiProps.setProperty("worksheetitemid", sectionitems.getValue(i, "worksheetitemid"));
                wsiProps.setProperty("worksheetitemversionid", sectionitems.getValue(i, "worksheetitemversionid"));
                ab.setActionClass("EditItem_" + sectionitems.getValue(i, "worksheetitemid") + sectionitems.getValue(i, "worksheetitemversionid"), SetWorksheetItemStatus.class.getName(), wsiProps);
                continue;
            }
            errorHandler.addValidationError(this.boldstart + "Control " + worksheetItem.getName(true) + this.boldend + " " + message);
        }
    }

    private void unblockSDIs(String worksheetid, String worksheetversionid, String blocksdcid, ActionBlock ab) throws SapphireException {
        DataSet sdiwids;
        PropertyList props = new PropertyList();
        if (blocksdcid.equalsIgnoreCase("sample")) {
            DataSet sampleds = this.getQueryProcessor().getPreparedSqlDataSet("select keyid1 from worksheetsdi where worksheetid = ? and worksheetversionid = ? and sdcid = 'Sample'", (Object[])new String[]{worksheetid, worksheetversionid});
            if (OpalUtil.isNotEmpty(sampleds)) {
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", sampleds.getColumnValues("keyid1", ";"));
                props.setProperty("bypassworksheetcompletioncheck", "Y");
                ab.setAction("SyncSDIDataSetStatus", "SyncSDIDataSetStatus", "1", props);
                props.clear();
                props.setProperty("sdcid", "Sample");
                props.setProperty("keyid1", sampleds.getColumnValues("keyid1", ";"));
                props.setProperty("bypassworksheetcompletioncheck", "Y");
                ab.setAction("SyncSDIWIStatus", "SyncSDIWIStatus", "1", props);
            }
        } else if (blocksdcid.equalsIgnoreCase("sdiworkitem") && OpalUtil.isNotEmpty(sdiwids = this.getQueryProcessor().getPreparedSqlDataSet("select keyid1 from worksheetsdi where worksheetid = ? and worksheetversionid = ? and sdcid = 'SDIWorkItem'", (Object[])new String[]{worksheetid, worksheetversionid}))) {
            props.clear();
            props.setProperty("sdcid", "SDIWorkItem");
            props.setProperty("keyid1", sdiwids.getColumnValues("keyid1", ";"));
            props.setProperty("bypassworksheetcompletioncheck", "Y");
            ab.setAction("SyncSDIWIStatus", "SyncSDIWIStatus", "1", props);
        }
    }
}

