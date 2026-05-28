/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.actions.eln.SetWorksheetItemStatus;
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
import sapphire.accessor.ActionException;
import sapphire.error.ErrorHandler;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.xml.PropertyList;

public class SetWorksheetSectionStatus
extends BaseELNAction {
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        PropertyList wsOptions;
        String worksheetversionid;
        String worksheetid = properties.getProperty("worksheetid");
        if (this.worksheetInProgress(this.database, worksheetid, worksheetversionid = properties.getProperty("worksheetversionid"), wsOptions = new PropertyList())) {
            String worksheetsectionid = properties.getProperty("worksheetsectionid");
            String worksheetsectionversionid = properties.getProperty("worksheetsectionversionid", "1");
            String newStatus = properties.getProperty("status");
            this.database.createPreparedResultSet("SELECT worksheetsectiondesc, sectionstatus, sectionlevel, usersequence FROM worksheetsection WHERE worksheetsectionid = ? AND worksheetsectionversionid = ?", new Object[]{worksheetsectionid, worksheetsectionversionid});
            if (this.database.getNext()) {
                String activitylog;
                DataSet notes;
                String boldstart = properties.getProperty("htmlerror").equals("Y") ? "<b>" : "";
                String boldend = properties.getProperty("htmlerror").equals("Y") ? "</b>" : "";
                String currentStatus = this.database.getValue("sectionstatus");
                SDIData worksheetSectionData = this.loadSection(worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid, false, true);
                DataSet approvalsteps = worksheetSectionData.getDataset("approvalstep");
                ErrorHandler errorHandler = this.getErrorHandler();
                String tracelogid = properties.getProperty("tracelogid");
                if (tracelogid.length() == 0 && properties.getProperty("auditreason").length() > 0) {
                    try {
                        AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
                        tracelogid = audit.addSDITraceLogEntry("LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, "", properties.getProperty("auditreason"), properties.getProperty("auditactivity"), properties.getProperty("auditsignedflag"), "N", "EditSection", true);
                    }
                    catch (ServiceException audit) {
                        // empty catch block
                    }
                }
                if (newStatus.equals("Approve") && currentStatus.equals("PendingApproval")) {
                    notes = SetWorksheetSectionStatus.getWorksheetSectionNotes(this.getQueryProcessor(), this.database, worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid, "followupflag = 'Y' AND resolvedflag = 'N'", true);
                    if (notes.size() == 0) {
                        if (approvalsteps.size() > 0) {
                            boolean moreApprovals = false;
                            for (int i = 0; !moreApprovals && i < approvalsteps.size(); ++i) {
                                if (approvalsteps.getValue(i, "stepstatusflag").equals("C") || !approvalsteps.getValue(i, "approvalflag").equals("U")) continue;
                                moreApprovals = true;
                            }
                            StringBuilder approvaltypeList = new StringBuilder();
                            StringBuilder approvalstepList = new StringBuilder();
                            StringBuilder approvalinstList = new StringBuilder();
                            StringBuilder approvalflagList = new StringBuilder();
                            for (int i = 0; i < approvalsteps.size(); ++i) {
                                if (!approvalsteps.getValue(i, "stepstatusflag").equals("C")) continue;
                                approvaltypeList.append(";").append(approvalsteps.getValue(i, "approvaltypeid"));
                                approvalstepList.append(";").append(approvalsteps.getValue(i, "approvalstep"));
                                approvalinstList.append(";").append(approvalsteps.getValue(i, "approvalstepinstance"));
                                approvalflagList.append(";").append("P");
                            }
                            if (approvaltypeList.length() > 0) {
                                PropertyList approvalProps = new PropertyList();
                                approvalProps.setProperty("sdcid", "LV_WorksheetSection");
                                approvalProps.setProperty("keyid1", worksheetsectionid);
                                approvalProps.setProperty("keyid2", worksheetsectionversionid);
                                approvalProps.setProperty("approvaltypeid", approvaltypeList.substring(1));
                                approvalProps.setProperty("approvalstep", approvalstepList.substring(1));
                                approvalProps.setProperty("approvalstepinstance", approvalinstList.substring(1));
                                approvalProps.setProperty("approvalflag", approvalflagList.substring(1));
                                this.getActionProcessor().processAction("ApproveSDIStep", "1", approvalProps);
                            }
                            if (!moreApprovals) {
                                properties.setProperty("status", "Complete");
                                this.getActionProcessor().processActionClass(SetWorksheetSectionStatus.class.getName(), properties);
                            } else {
                                PropertyList wssProps = new PropertyList();
                                wssProps.setProperty("sdcid", "LV_WorksheetSection");
                                wssProps.setProperty("tracelogid", tracelogid);
                                wssProps.setProperty("sectionstatus", "PendingApproval");
                                wssProps.setProperty("worksheet_action", "Y");
                                wssProps.setProperty("keyid1", worksheetsectionid);
                                wssProps.setProperty("keyid2", worksheetsectionversionid);
                                this.getActionProcessor().processAction("EditSDI", "1", wssProps);
                                this.addActivityLog(worksheetid, worksheetversionid, "SetStatus", "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, "Intermediate section approval");
                            }
                        }
                    } else {
                        errorHandler.addValidationError("Followup notes exist that have not been resolved");
                    }
                } else if (newStatus.equals("Reject")) {
                    if (currentStatus.equals("PendingApproval")) {
                        notes = SetWorksheetSectionStatus.getWorksheetSectionNotes(this.getQueryProcessor(), this.database, worksheetid, worksheetversionid, worksheetsectionid, worksheetsectionversionid, "followupflag = 'Y' AND resolvedflag = 'N'", true);
                        if (notes.size() > 0) {
                            properties.setProperty("status", "InProgress");
                            this.getActionProcessor().processActionClass(SetWorksheetSectionStatus.class.getName(), properties);
                        } else {
                            errorHandler.addValidationError("No followup notes have been defined");
                        }
                    } else {
                        errorHandler.addValidationError("Section is not in a PendingApproval state");
                    }
                } else if (newStatus.equals("InProgress")) {
                    int i;
                    int i2;
                    activitylog = "";
                    this.logger.info("Setting worksheetsection " + SetWorksheetSectionStatus.getIdVersionText(worksheetsectionid, worksheetsectionversionid) + " status to " + newStatus);
                    activitylog = "Changed section status from " + currentStatus + " to " + newStatus;
                    ActionBlock ab = new ActionBlock();
                    PropertyList wssProps = new PropertyList();
                    wssProps.setProperty("sdcid", "LV_WorksheetSection");
                    wssProps.setProperty("tracelogid", tracelogid);
                    wssProps.setProperty("contentcompletedby", "(null)");
                    wssProps.setProperty("contentcompleteddt", "(null)");
                    wssProps.setProperty("contentcompletedreason", "(null)");
                    wssProps.setProperty("contentcompletedtracelogid", "(null)");
                    wssProps.setProperty("sectionstatus", newStatus);
                    wssProps.setProperty("worksheet_action", "Y");
                    ab.setAction("EditSection", "EditSDI", "1", wssProps);
                    PropertyList loadWorksheetProps = new PropertyList();
                    loadWorksheetProps.setProperty("worksheetid", worksheetid);
                    loadWorksheetProps.setProperty("worksheetversionid", worksheetversionid);
                    loadWorksheetProps.setProperty("loadoptions", "Y");
                    SDIData worksheetData = this.loadWorksheet(worksheetid, worksheetversionid, loadWorksheetProps);
                    SDIData sectionData = worksheetData.getSDIData("sections");
                    DataSet sections = sectionData.getDataset("primary");
                    HashMap<String, String> find = new HashMap<String, String>();
                    find.put("worksheetsectionid", worksheetsectionid);
                    find.put("worksheetsectionversionid", worksheetsectionversionid);
                    int row = sections.findRow(find);
                    for (int i3 = 0; i3 < row; ++i3) {
                        sections.deleteRow(0);
                    }
                    int level = sections.getInt(0, "sectionlevel");
                    int nextfound = -1;
                    for (i2 = 1; i2 < sections.size() && nextfound == -1; ++i2) {
                        if (sections.getInt(i2, "sectionlevel") > level) continue;
                        nextfound = i2;
                    }
                    if (nextfound > -1) {
                        for (i2 = sections.size() - 1; i2 >= nextfound; --i2) {
                            sections.deleteRow(i2);
                        }
                    }
                    SDIData itemData = worksheetData.getSDIData("items");
                    DataSet items = itemData.getDataset("primary");
                    HashMap<String, String> filter = new HashMap<String, String>();
                    StringBuilder wssKeyid1 = new StringBuilder();
                    StringBuilder wssKeyid2 = new StringBuilder();
                    StringBuilder returnStatusValues = new StringBuilder();
                    StringBuilder wssBehaviorType = new StringBuilder();
                    StringBuilder setstatusworksheetitemid = new StringBuilder();
                    StringBuilder setstatusworksheetitemversionid = new StringBuilder();
                    StringBuilder setstatusvalue = new StringBuilder();
                    StringBuilder availableworksheetitemid = new StringBuilder();
                    StringBuilder availableworksheetitemversionid = new StringBuilder();
                    StringBuilder availabilityitemflag = new StringBuilder();
                    for (i = 0; i < sections.size(); ++i) {
                        String loopSectionid = sections.getValue(i, "worksheetsectionid");
                        String loopSectionVersionid = sections.getValue(i, "worksheetsectionversionid");
                        wssKeyid1.append(";").append(loopSectionid);
                        wssKeyid2.append(";").append(loopSectionVersionid);
                        returnStatusValues.append(";").append(newStatus);
                        wssBehaviorType.append(";(null)");
                        filter.put("worksheetsectionid", loopSectionid);
                        filter.put("worksheetsectionversionid", loopSectionVersionid);
                        DataSet sectionitems = items.getFilteredDataSet(filter);
                        for (int j = 0; j < sectionitems.size(); ++j) {
                            String worksheetitemid = sectionitems.getValue(j, "worksheetitemid");
                            String worksheetitemversionid = sectionitems.getValue(j, "worksheetitemversionid");
                            PropertyList wsiProps = new PropertyList();
                            wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                            wsiProps.setProperty("status", "InProgress");
                            wsiProps.setProperty("tracelogid", tracelogid);
                            wsiProps.setProperty("auditreason", properties.getProperty("auditreason"));
                            wsiProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
                            wsiProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                            wsiProps.setProperty("bypassstatuscheck", "Y");
                            wsiProps.setProperty("bypassavailabilitycheck", "Y");
                            wsiProps.setProperty("worksheetid", worksheetid);
                            wsiProps.setProperty("worksheetversionid", worksheetversionid);
                            wsiProps.setProperty("worksheetitemid", worksheetitemid);
                            wsiProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                            ab.setActionClass("EditItem_" + worksheetitemid + worksheetitemversionid, SetWorksheetItemStatus.class.getName(), wsiProps);
                            setstatusworksheetitemid.append(";").append(worksheetitemid);
                            setstatusworksheetitemversionid.append(";").append(worksheetitemversionid);
                            setstatusvalue.append(";").append("InProgress");
                        }
                    }
                    if (errorHandler.size() > 0) {
                        throw new SapphireException();
                    }
                    if (wssKeyid1.length() > 0) {
                        wssProps.setProperty("keyid1", wssKeyid1.substring(1));
                        wssProps.setProperty("keyid2", wssKeyid2.substring(1));
                        wssProps.setProperty("behaviortype", wssBehaviorType.substring(1));
                    }
                    this.addResetApproval(ab, "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, approvalsteps);
                    this.getActionProcessor().processActionBlock(ab);
                    properties.setProperty("setstatusworksheetitemid", wssKeyid1.append((CharSequence)setstatusworksheetitemid).substring(1));
                    properties.setProperty("setstatusworksheetitemversionid", wssKeyid2.append((CharSequence)setstatusworksheetitemversionid).substring(1));
                    properties.setProperty("setstatusvalue", returnStatusValues.append((CharSequence)setstatusvalue).substring(1));
                    for (i = 0; i < ab.getActionCount(); ++i) {
                        HashMap actionProperties;
                        String availableid;
                        if (!ab.getActionName(i).startsWith("EditItem_") || (availableid = (String)(actionProperties = ab.getActionProperties(i)).get("availableworksheetitemid")) == null || availableid.length() <= 0) continue;
                        availableworksheetitemid.append(";").append(availableid);
                        availableworksheetitemversionid.append(";").append((String)actionProperties.get("availableworksheetitemversionid"));
                        availabilityitemflag.append(";").append((String)actionProperties.get("availabilityflag"));
                    }
                    this.addActivityLog(worksheetid, worksheetversionid, "SetStatus", "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, activitylog);
                } else if (newStatus.equals("Complete") || newStatus.equals("PendingApproval")) {
                    int i;
                    activitylog = "";
                    String wiStatus = "Complete";
                    if (newStatus.equals("PendingApproval")) {
                        this.logger.info("Setting worksheetsection " + SetWorksheetSectionStatus.getIdVersionText(worksheetsectionid, worksheetsectionversionid) + " status to " + newStatus);
                        activitylog = "Changed section status from " + currentStatus + " to " + newStatus;
                    } else if (newStatus.equals("Complete")) {
                        this.logger.info("Setting worksheetsection " + SetWorksheetSectionStatus.getIdVersionText(worksheetsectionid, worksheetsectionversionid) + " status to " + newStatus);
                        activitylog = "Changed section status from " + currentStatus + " to " + newStatus;
                    }
                    ActionBlock ab = new ActionBlock();
                    PropertyList wssProps = new PropertyList();
                    wssProps.setProperty("sdcid", "LV_WorksheetSection");
                    wssProps.setProperty("tracelogid", tracelogid);
                    if (newStatus.equals("Complete") && !currentStatus.equals("PendingApproval") || newStatus.equals("PendingApproval")) {
                        wssProps.setProperty("contentcompletedby", this.connectionInfo.getSysuserId());
                        wssProps.setProperty("contentcompleteddt", "n");
                        wssProps.setProperty("contentcompletedreason", properties.getProperty("auditreason"));
                        wssProps.setProperty("contentcompletedtracelogid", tracelogid);
                    }
                    wssProps.setProperty("sectionstatus", newStatus);
                    wssProps.setProperty("worksheet_action", "Y");
                    ab.setAction("EditSection", "EditSDI", "1", wssProps);
                    PropertyList loadWorksheetProps = new PropertyList();
                    loadWorksheetProps.setProperty("worksheetid", worksheetid);
                    loadWorksheetProps.setProperty("worksheetversionid", worksheetversionid);
                    loadWorksheetProps.setProperty("loadoptions", "Y");
                    SDIData worksheetData = this.loadWorksheet(worksheetid, worksheetversionid, loadWorksheetProps);
                    SDIData sectionData = worksheetData.getSDIData("sections");
                    DataSet sections = sectionData.getDataset("primary");
                    HashMap<String, String> find = new HashMap<String, String>();
                    find.put("worksheetsectionid", worksheetsectionid);
                    find.put("worksheetsectionversionid", worksheetsectionversionid);
                    int row = sections.findRow(find);
                    for (int i4 = 0; i4 < row; ++i4) {
                        sections.deleteRow(0);
                    }
                    int level = sections.getInt(0, "sectionlevel");
                    int nextfound = -1;
                    for (i = 1; i < sections.size() && nextfound == -1; ++i) {
                        if (sections.getInt(i, "sectionlevel") > level) continue;
                        nextfound = i;
                    }
                    if (nextfound > -1) {
                        for (i = sections.size() - 1; i >= nextfound; --i) {
                            sections.deleteRow(i);
                        }
                    }
                    SDIData itemData = worksheetData.getSDIData("items");
                    DataSet items = itemData.getDataset("primary");
                    HashMap<String, String> filter = new HashMap<String, String>();
                    StringBuilder wssKeyid1 = new StringBuilder();
                    StringBuilder wssKeyid2 = new StringBuilder();
                    StringBuilder returnStatusValues = new StringBuilder();
                    StringBuilder wssBehaviorType = new StringBuilder();
                    StringBuilder setstatusworksheetitemid = new StringBuilder();
                    StringBuilder setstatusworksheetitemversionid = new StringBuilder();
                    StringBuilder setstatusvalue = new StringBuilder();
                    StringBuilder availableworksheetitemid = new StringBuilder();
                    StringBuilder availableworksheetitemversionid = new StringBuilder();
                    StringBuilder availabilityitemflag = new StringBuilder();
                    boolean sectionCompletionRequiresAllComplete = false;
                    boolean sectionCompletionRequiresIncidentsClosed = false;
                    boolean completionIncludesHidden = false;
                    boolean completionIncludesDisabled = false;
                    for (int i5 = 0; i5 < sections.size(); ++i5) {
                        String loopSectionid = sections.getValue(i5, "worksheetsectionid");
                        String loopSectionVersionid = sections.getValue(i5, "worksheetsectionversionid");
                        String loopSectionStatus = sections.getValue(i5, "sectionstatus");
                        String loopSectionBehavior = sections.getValue(i5, "_behavior");
                        PropertyList wssOptions = null;
                        try {
                            wssOptions = new PropertyList(new JSONObject(sections.getClob(i5, "options", "{}")));
                        }
                        catch (JSONException e) {
                            this.logger.error("Failed to parse section JSON options", e);
                        }
                        boolean includeSection = true;
                        DataSet incidents = null;
                        if (i5 == 0) {
                            int count;
                            sectionCompletionRequiresAllComplete = wssOptions.getProperty("sectioncompletionrequiresallcomplete", "N").equals("Y");
                            sectionCompletionRequiresIncidentsClosed = wssOptions.getProperty("sectionscompletionrequiresincidentsclosed", "Y").equals("Y");
                            completionIncludesHidden = wssOptions.getProperty("sectioncompletionincludeshidden", "N").equals("Y");
                            completionIncludesDisabled = wssOptions.getProperty("sectioncompletionincludesdisabled", "Y").equals("Y");
                            if (sectionCompletionRequiresIncidentsClosed && (count = this.getIncidentsNotClosedCount(incidents = this.getQueryProcessor().getPreparedSqlDataSet("SELECT ii.incidentid, ii.sourcesdcid, ii.sourcekeyid1, ii.sourcekeyid2 FROM incident i, incidentitem ii  WHERE i.incidentid=ii.incidentid  AND ii.sourcesdcid in (?,?,? ) AND ii.causalobjectflag='Y'  AND i.incidentstatus not in( 'Completed','Closed','Cancelled' ) AND i.incidentid in  ( SELECT incidentid    FROM incidentitem    WHERE sourcesdcid=? AND sourcekeyid1=? AND sourcekeyid2=? )", (Object[])new String[]{"LV_Worksheet", "LV_WorksheetSection", "LV_WorksheetItem", "LV_Worksheet", worksheetid, worksheetversionid}), "LV_WorksheetSection", loopSectionid, loopSectionVersionid)) > 0) {
                                errorHandler.addValidationError(boldstart + "Section " + sections.getValue(i5, "worksheetsectiondesc") + boldend + " has " + count + " active incident" + (count == 1 ? "." : "s."));
                            }
                            wssKeyid1.append(";").append(loopSectionid);
                            wssKeyid2.append(";").append(loopSectionVersionid);
                            wssBehaviorType.append(";").append(loopSectionBehavior);
                            returnStatusValues.append(";").append(newStatus);
                            String message = BaseELNAction.validateMetaData(this.getQueryProcessor(), "LV_WorksheetSection", loopSectionid, loopSectionVersionid);
                            if (message.length() > 0) {
                                errorHandler.addValidationError(boldstart + "Section " + sections.getValue(i5, "worksheetsectiondesc") + boldend + " " + message);
                            }
                        } else {
                            int count;
                            String message;
                            if (loopSectionBehavior.equals("hide") && !completionIncludesHidden) {
                                includeSection = false;
                            } else if (loopSectionBehavior.equals("disable") && !completionIncludesDisabled) {
                                includeSection = false;
                            }
                            if (includeSection && (message = BaseELNAction.validateMetaData(this.getQueryProcessor(), "LV_WorksheetSection", loopSectionid, loopSectionVersionid)).length() > 0) {
                                errorHandler.addValidationError(boldstart + "Section " + sections.getValue(i5, "worksheetsectiondesc") + boldend + " " + message);
                            }
                            if (includeSection && incidents != null && (count = this.getIncidentsNotClosedCount(incidents, "LV_WorksheetSection", loopSectionid, loopSectionVersionid)) > 0) {
                                errorHandler.addValidationError(boldstart + "Section " + sections.getValue(i5, "worksheetsectiondesc") + boldend + " has " + count + " active incident" + (count == 1 ? "." : "s."));
                            }
                            if (includeSection) {
                                String sectionCompletion = wssOptions.getProperty("sectioncompletion", "N");
                                if (sectionCompletionRequiresAllComplete && sectionCompletion.equals("C") && !loopSectionStatus.equals("Complete")) {
                                    errorHandler.addValidationError(boldstart + "Section " + sections.getValue(i5, "worksheetsectiondesc") + boldend + " status needs to be " + "Complete");
                                } else if (!sectionCompletion.equals("A") && !loopSectionStatus.equals("Complete")) {
                                    wssKeyid1.append(";").append(loopSectionid);
                                    wssKeyid2.append(";").append(loopSectionVersionid);
                                    wssBehaviorType.append(";").append(loopSectionBehavior);
                                    returnStatusValues.append(";").append("Complete");
                                }
                            } else if (!loopSectionStatus.equals("Complete")) {
                                wssKeyid1.append(";").append(loopSectionid);
                                wssKeyid2.append(";").append(loopSectionVersionid);
                                wssBehaviorType.append(";").append(loopSectionBehavior);
                                returnStatusValues.append(";").append("Complete");
                            }
                        }
                        filter.put("worksheetsectionid", loopSectionid);
                        filter.put("worksheetsectionversionid", loopSectionVersionid);
                        DataSet sectionitems = items.getFilteredDataSet(filter);
                        for (int j = 0; j < sectionitems.size(); ++j) {
                            int count;
                            String worksheetitemid = sectionitems.getValue(j, "worksheetitemid");
                            String worksheetitemversionid = sectionitems.getValue(j, "worksheetitemversionid");
                            String itemMessage = "";
                            if (includeSection && incidents != null && (count = this.getIncidentsNotClosedCount(incidents, "LV_WorksheetItem", worksheetitemid, worksheetitemversionid)) > 0) {
                                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)sectionitems.get(j));
                                errorHandler.addValidationError(boldstart + "Control " + worksheetItem.getName(true) + boldend + " has " + count + " active incident" + (count == 1 ? "." : "s."));
                            }
                            WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)sectionitems.get(j));
                            WorksheetItemOptions itemOptions = worksheetItem.getWorksheetItemOptions();
                            String itemStatus = sectionitems.getValue(j, "itemstatus");
                            if (includeSection && !itemStatus.equals("Complete")) {
                                if (sectionCompletionRequiresAllComplete && itemOptions.getOption("itemcompletion").equals("C")) {
                                    itemMessage = "status needs to be Complete";
                                }
                                if (itemMessage.length() == 0) {
                                    itemMessage = worksheetItem.checkCompleteness(itemStatus);
                                }
                                if (itemMessage.length() > 0) {
                                    errorHandler.addValidationError(boldstart + "Control " + worksheetItem.getName(true) + boldend + "<br> " + itemMessage);
                                }
                            }
                            if (itemMessage.length() != 0) continue;
                            PropertyList wsiProps = new PropertyList();
                            wsiProps.setProperty("sdcid", "LV_WorksheetItem");
                            wsiProps.setProperty("status", wiStatus);
                            wsiProps.setProperty("tracelogid", tracelogid);
                            wsiProps.setProperty("auditreason", properties.getProperty("auditreason"));
                            wsiProps.setProperty("auditactivity", properties.getProperty("auditactivity"));
                            wsiProps.setProperty("auditsignedflag", properties.getProperty("auditsignedflag"));
                            wsiProps.setProperty("bypassstatuscheck", "Y");
                            wsiProps.setProperty("bypassavailabilitycheck", "Y");
                            wsiProps.setProperty("worksheetid", worksheetid);
                            wsiProps.setProperty("worksheetversionid", worksheetversionid);
                            wsiProps.setProperty("worksheetitemid", worksheetitemid);
                            wsiProps.setProperty("worksheetitemversionid", worksheetitemversionid);
                            ab.setActionClass("EditItem_" + worksheetitemid + worksheetitemversionid, SetWorksheetItemStatus.class.getName(), wsiProps);
                            setstatusworksheetitemid.append(";").append(worksheetitemid);
                            setstatusworksheetitemversionid.append(";").append(worksheetitemversionid);
                            setstatusvalue.append(";").append(wiStatus);
                        }
                    }
                    if (errorHandler.size() > 0) {
                        throw new SapphireException();
                    }
                    if (wssKeyid1.length() > 0) {
                        wssProps.setProperty("keyid1", wssKeyid1.substring(1));
                        wssProps.setProperty("keyid2", wssKeyid2.substring(1));
                        wssProps.setProperty("behaviortype", wssBehaviorType.substring(1));
                    }
                    try {
                        this.getActionProcessor().processActionBlock(ab);
                    }
                    catch (ActionException e) {
                        ErrorHandler errorHandler1 = ab.getErrorHandler();
                        errorHandler.addValidationError(errorHandler1.getLastErrorMessage());
                    }
                    if (errorHandler.size() > 0) {
                        throw new SapphireException();
                    }
                    properties.setProperty("setstatusworksheetitemid", wssKeyid1.append((CharSequence)setstatusworksheetitemid).substring(1));
                    properties.setProperty("setstatusworksheetitemversionid", wssKeyid2.append((CharSequence)setstatusworksheetitemversionid).substring(1));
                    properties.setProperty("setstatusvalue", returnStatusValues.append((CharSequence)setstatusvalue).substring(1));
                    for (int i6 = 0; i6 < ab.getActionCount(); ++i6) {
                        HashMap actionProperties;
                        String availableid;
                        if (!ab.getActionName(i6).startsWith("EditItem_") || (availableid = (String)(actionProperties = ab.getActionProperties(i6)).get("availableworksheetitemid")) == null || availableid.length() <= 0) continue;
                        availableworksheetitemid.append(";").append(availableid);
                        availableworksheetitemversionid.append(";").append((String)actionProperties.get("availableworksheetitemversionid"));
                        availabilityitemflag.append(";").append((String)actionProperties.get("availabilityflag"));
                    }
                    this.addActivityLog(worksheetid, worksheetversionid, "SetStatus", "LV_WorksheetSection", worksheetsectionid, worksheetsectionversionid, activitylog);
                }
            } else {
                throw new SapphireException("Worksheet section " + SetWorksheetSectionStatus.getIdVersionText(worksheetsectionid, worksheetsectionversionid) + " not found");
            }
        }
    }
}

