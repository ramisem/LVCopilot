/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.actions.eln;

import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.actions.eln.BaseELNAction;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItem;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemFactory;
import com.labvantage.sapphire.modules.eln.gwt.server.worksheetitem.WorksheetItemOptions;
import com.labvantage.sapphire.services.AuditService;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.services.ServiceException;
import java.util.HashMap;
import sapphire.SapphireException;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;

public class SetWorksheetItemStatus
extends BaseELNAction {
    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String worksheetid = properties.getProperty("worksheetid");
        String worksheetversionid = properties.getProperty("worksheetversionid");
        String worksheetitemid = properties.getProperty("worksheetitemid");
        String worksheetitemversionid = properties.getProperty("worksheetitemversionid", "1");
        String newStatus = properties.getProperty("status");
        DataSet item = this.getWorksheetItem(worksheetitemid, worksheetitemversionid);
        PropertyList wsOptions = new PropertyList();
        PropertyList wssOptions = new PropertyList();
        String worksheetsectionid = item.getValue(0, "worksheetsectionid");
        String worksheetsectionversionid = item.getValue(0, "worksheetsectionversionid");
        PropertyList sdcProps = this.getSDCProcessor().getPropertyList("LV_WorksheetItem");
        AuditService audit = new AuditService(new SapphireConnection(this.database.getConnection(), this.connectionInfo));
        String traceLogId = "";
        boolean traceLogSetToSession = false;
        if (!sdcProps.getProperty("auditedflag").equalsIgnoreCase("N")) {
            String auditReason;
            String traceLogIdStr;
            traceLogId = traceLogIdStr = properties.getProperty("tracelogid", "").trim();
            if (traceLogIdStr.length() == 0 && (auditReason = properties.getProperty("auditreason", "")).length() > 0) {
                this.logger.info("Generate the tracelog records");
                String promptflag = sdcProps.getProperty("auditpromptflag");
                String standard = !promptflag.equalsIgnoreCase("R") && !promptflag.equalsIgnoreCase("S") ? "N" : "Y";
                try {
                    traceLogId = audit.addSDITraceLogEntry("LV_WorksheetItem", worksheetitemid, worksheetitemversionid, "", auditReason, properties.getProperty("auditactivity", ""), properties.getProperty("auditsignedflag", "N"), properties.getProperty("auditdt"), "SetWorksheetItemStatus LV_WorksheetItem, " + worksheetitemid, standard.equals("Y"));
                    properties.setProperty("tracelogid", String.valueOf(traceLogId));
                    audit.setTracelogIdInDBSession(traceLogId + "");
                    traceLogSetToSession = true;
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to add audit records", e);
                }
            }
        }
        try {
            if (!this.sectionInProgress(this.database, worksheetsectionid, worksheetsectionversionid, wsOptions, wssOptions, properties.getProperty("bypassstatuscheck").equals("Y"))) return;
            if (properties.getProperty("bypassavailabilitycheck").equals("Y") || item.getValue(0, "availabilityflag", "Y").equals("Y")) {
                String currentStatus = item.getValue(0, "itemstatus");
                ActionBlock ab = new ActionBlock();
                PropertyList editProps = new PropertyList();
                editProps.setProperty("sdcid", "LV_WorksheetItem");
                editProps.setProperty("keyid1", worksheetitemid);
                editProps.setProperty("keyid2", worksheetitemversionid);
                editProps.setProperty("tracelogid", traceLogId);
                editProps.setProperty("itemstatus", newStatus);
                editProps.setProperty("worksheet_action", "Y");
                ab.setAction("EditItem", "EditSDI", "1", editProps);
                String itemAvailability = wssOptions.getProperty("itemavailability", "A");
                String activitylog = "";
                WorksheetItem worksheetItem = WorksheetItemFactory.getInstance(new SapphireConnection(this.database.getConnection(), this.connectionInfo), (DBUtil)this.database, (HashMap)item.get(0));
                if (newStatus.equals("Complete")) {
                    if (!item.getValue(0, "itemstatus").equals("Complete") || item.getValue(0, "contentcompleteddt").length() == 0) {
                        editProps.setProperty("contentcompletedby", this.connectionInfo.getSysuserId());
                        editProps.setProperty("contentcompleteddt", "n");
                        editProps.setProperty("contentcompletedreason", properties.getProperty("auditreason"));
                        editProps.setProperty("contentcompletedtracelogid", traceLogId);
                    }
                } else {
                    editProps.setProperty("contentcompletedby", "(null)");
                    editProps.setProperty("contentcompleteddt", "(null)");
                    editProps.setProperty("contentcompletedreason", "(null)");
                    editProps.setProperty("contentcompletedtracelogid", "(null)");
                }
                WorksheetItemOptions worksheetItemOptions = worksheetItem.getWorksheetItemOptions();
                if (newStatus.equals("Complete")) {
                    boolean requiresCompleteHTML;
                    int count;
                    boolean itemCompletionRequiresIncidentsClosed;
                    this.logger.info("Setting worksheetitem " + SetWorksheetItemStatus.getIdVersionText(worksheetitemid, worksheetitemversionid) + " status to " + newStatus + " and saving view html");
                    activitylog = "Changed item status from " + currentStatus + " to " + newStatus;
                    String message = worksheetItem.checkCompleteness(currentStatus);
                    if (message.length() > 0) {
                        this.getErrorHandler().addValidationError(message);
                    }
                    if ((itemCompletionRequiresIncidentsClosed = worksheetItemOptions.getOption("itemcompletionrequiresincidentsclosed", "Y").equals("Y")) && (count = this.getQueryProcessor().getPreparedCount("SELECT count(*)  FROM incident i, incidentitem ii  WHERE i.incidentid = ii.incidentid  AND ii.sourcesdcid=? AND ii.sourcekeyid1=? AND ii.sourcekeyid2=? AND ii.causalobjectflag='Y'  AND i.incidentstatus not in( 'Completed','Closed','Cancelled' )", new String[]{"LV_WorksheetItem", worksheetitemid, worksheetitemversionid})) > 0) {
                        this.getErrorHandler().addValidationError("Control has " + count + " active incident" + (count == 1 ? "." : "s."));
                    }
                    if (this.getErrorHandler().size() != 0) throw new SapphireException();
                    boolean hasCompleteHTML = item.getValue(0, "html").length() > 0 && currentStatus.equals("Complete");
                    boolean bl = requiresCompleteHTML = properties.getProperty("savehtml").equals("Y") || wsOptions.getProperty("savehtmloncomplete", "I").equals("I") && !hasCompleteHTML;
                    if (worksheetItem.readyToSaveHtml() && !worksheetItemOptions.getOption("isalwayslive").equals("Y") && requiresCompleteHTML) {
                        editProps.setProperty("html", worksheetItem.getCompleteHTML());
                        if (worksheetItemOptions.getOption("hasexporthtml").equals("Y")) {
                            editProps.setProperty("exporthtml", worksheetItem.getExportHTML(null));
                        }
                    }
                    editProps.setProperty("availabilitystatus", worksheetItem.getAvailability());
                    if (itemAvailability.equals("S")) {
                        this.database.createPreparedResultSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetsectionid = ? AND worksheetsectionversionid = ? AND usersequence > ? ORDER BY usersequence", new Object[]{worksheetsectionid, worksheetsectionversionid, item.getInt(0, "usersequence")});
                        if (this.database.getNext()) {
                            PropertyList nextProps = new PropertyList();
                            nextProps.setProperty("sdcid", "LV_WorksheetItem");
                            nextProps.setProperty("keyid1", this.database.getValue("worksheetitemid"));
                            nextProps.setProperty("keyid2", this.database.getValue("worksheetitemversionid"));
                            nextProps.setProperty("availabilityflag", "Y");
                            nextProps.setProperty("worksheet_action", "Y");
                            ab.setAction("EditNextItem", "EditSDI", "1", nextProps);
                            properties.setProperty("availableworksheetitemid", this.database.getValue("worksheetitemid"));
                            properties.setProperty("availableworksheetitemversionid", this.database.getValue("worksheetitemversionid"));
                            properties.setProperty("availabilityflag", "Y");
                        }
                    }
                } else {
                    if (!newStatus.equals("InProgress")) throw new SapphireException("Invalid item status transition from " + currentStatus + " to " + newStatus);
                    String message = worksheetItem.validateStateChange(currentStatus, newStatus);
                    if (message.length() == 0) {
                        this.logger.info("Setting worksheetitem " + SetWorksheetItemStatus.getIdVersionText(worksheetitemid, worksheetitemversionid) + " status to " + newStatus + " and resetting view html");
                        activitylog = "Changed item status from " + currentStatus + " to " + newStatus;
                        editProps.setProperty("html", "");
                        editProps.setProperty("exporthtml", "");
                        editProps.setProperty("availabilitystatus", "");
                        if (itemAvailability.equals("S")) {
                            this.database.createPreparedResultSet("SELECT worksheetitemid, worksheetitemversionid FROM worksheetitem WHERE worksheetsectionid = ? AND worksheetsectionversionid = ? AND usersequence > ? ORDER BY usersequence", new Object[]{worksheetsectionid, worksheetsectionversionid, item.getInt(0, "usersequence")});
                            StringBuffer availableworksheetitemid = new StringBuffer();
                            StringBuffer availableworksheetitemversionid = new StringBuffer();
                            StringBuffer availabilityflag = new StringBuffer();
                            while (this.database.getNext()) {
                                availableworksheetitemid.append(";").append(this.database.getValue("worksheetitemid"));
                                availableworksheetitemversionid.append(";").append(this.database.getValue("worksheetitemversionid"));
                                availabilityflag.append(";").append("N");
                            }
                            if (availableworksheetitemid.length() > 0) {
                                PropertyList nextProps = new PropertyList();
                                nextProps.setProperty("sdcid", "LV_WorksheetItem");
                                nextProps.setProperty("keyid1", availableworksheetitemid.substring(1));
                                nextProps.setProperty("keyid2", availableworksheetitemversionid.substring(1));
                                nextProps.setProperty("availabilityflag", "N");
                                nextProps.setProperty("worksheet_action", "Y");
                                ab.setAction("EditNextItem", "EditSDI", "1", nextProps);
                                properties.setProperty("availableworksheetitemid", availableworksheetitemid.substring(1));
                                properties.setProperty("availableworksheetitemversionid", availableworksheetitemversionid.substring(1));
                                properties.setProperty("availabilityflag", availabilityflag.substring(1));
                            }
                        }
                    } else {
                        this.getErrorHandler().addValidationError(message);
                        throw new SapphireException();
                    }
                }
                this.getActionProcessor().processActionBlock(ab);
                this.addActivityLog(worksheetid, worksheetversionid, "SetStatus", "LV_WorksheetItem", worksheetitemid, worksheetitemversionid, activitylog);
                return;
            }
            this.getErrorHandler().addValidationError("Not available to be marked as " + newStatus);
            throw new SapphireException();
        }
        catch (Exception e) {
            throw new SapphireException(this.getErrorHandler().getLastErrorMessage(), e);
        }
        finally {
            if (traceLogSetToSession) {
                try {
                    audit.removeTracelogIdFromDBSession();
                }
                catch (ServiceException e) {
                    throw new SapphireException("Failed to clear Tracelog Info from session: ", e);
                }
            }
        }
    }
}

