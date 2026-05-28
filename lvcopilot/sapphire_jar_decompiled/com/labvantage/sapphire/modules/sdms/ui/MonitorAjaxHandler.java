/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  javax.servlet.jsp.PageContext
 */
package com.labvantage.sapphire.modules.sdms.ui;

import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.system.automation.LAM;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.SDMSCommandHandler;
import com.labvantage.sapphire.pageelements.attachment.Files;
import com.labvantage.sapphire.pageelements.maint.MaintAttachmentOperationExecution;
import com.labvantage.sapphire.pageelements.maint.MaintAttribute;
import com.labvantage.sapphire.pageelements.maint.MaintDataCapture;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.util.file.FileType;
import com.labvantage.sapphire.util.format.RelativeDateFormat;
import com.labvantage.sapphire.util.images.ImageRef;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.attachment.Attachment;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class MonitorAjaxHandler
extends BaseAjaxRequest
implements SDMSConstants {
    static long lastMonitorTime = 0L;
    private PageContext pageContext = null;

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxresponse = new AjaxResponse(request, response);
        this.pageContext = ajaxresponse.getPageContext((Servlet)this.getServlet(), servletContext, request, response);
        ConnectionProcessor cp = this.getConnectionProcessor();
        ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
        try {
            String dothis = ajaxresponse.getRequestParameter("dothis");
            if (dothis.equals("AJAX_DRAWCOLLECTORS")) {
                this.drawCollectors(ajaxresponse);
            } else if (dothis.equals("AJAX_UPDATESTATUSES")) {
                this.updateCollectorStatuses(ajaxresponse, null, true);
            } else if (dothis.equals("AJAX_REFRESHCOLLECTORS")) {
                this.updateCollectorStatuses(ajaxresponse, ajaxresponse.getRequestParameter("collectorids").length() > 0 ? StringUtil.split(ajaxresponse.getRequestParameter("collectorids"), ";") : new String[]{}, false);
            } else if (dothis.equals("AJAX_REFRESHDATACAPTURES")) {
                this.updateDataCaptureStatuses(ajaxresponse, ajaxresponse.getRequestParameter("datacaptureids").length() > 0 ? StringUtil.split(ajaxresponse.getRequestParameter("datacaptureids"), ";") : new String[]{});
            } else if (dothis.equals("AJAX_REFRESHDATACAPTURECOUNT")) {
                this.updateDataCaptureCount(ajaxresponse, ajaxresponse.getRequestParameter("properties"));
            } else if (dothis.equals("AJAX_REFRESHINSTRUMENTS")) {
                this.updateInstrumentStatuses(ajaxresponse, ajaxresponse.getRequestParameter("instrumentids").length() > 0 ? StringUtil.split(ajaxresponse.getRequestParameter("instrumentids"), ";") : new String[]{});
            } else if (dothis.equals("AJAX_SHOWCOLLECTORDETAILS")) {
                this.showCollectorDetails(ajaxresponse, info);
            } else if (dothis.equals("AJAX_SHOWDATACAPTUREDETAILS")) {
                this.showDataCaptureDetails(ajaxresponse, info);
            } else if (dothis.equals("AJAX_SHOWATTACHMENTDETAILS")) {
                this.showAttachmentDetails(ajaxresponse, info);
            } else if (dothis.equals("AJAX_SHOWINSTRUMENTDETAILS")) {
                this.showInstrumentDetails(ajaxresponse, info);
            } else if (dothis.equals("AJAX_STOPSTARTEMULATOR")) {
                this.startstopEmulator(ajaxresponse, info);
            } else if (dothis.equals("AJAX_TRIGGEREMULATOR")) {
                this.triggerEmulator(ajaxresponse, info);
            } else if (dothis.equals("AJAX_GETCOLLECTORCOMMANDREPLY")) {
                this.getCollectorCommandReply(ajaxresponse, info);
            } else if (dothis.equals("AJAX_PAUSECOLLECTOR")) {
                this.pauseCollector(ajaxresponse, info, "Y");
            } else if (dothis.equals("AJAX_PAUSEINSTRUMENT")) {
                this.pauseInstrument(ajaxresponse, info, "Y");
            } else if (dothis.equals("AJAX_RESUMEINSTRUMENT")) {
                this.pauseInstrument(ajaxresponse, info, "N");
            } else if (dothis.equals("AJAX_REBOOTINSTRUMENT")) {
                this.rebootInstrument(ajaxresponse, info);
            } else if (dothis.equals("AJAX_MARKREADY")) {
                this.markDataCaptureReady(ajaxresponse, info);
            } else if (dothis.equals("AJAX_RESUMECOLLECTOR")) {
                this.pauseCollector(ajaxresponse, info, "N");
            } else if (dothis.equals("AJAX_DISABLECOLLECTOR")) {
                this.disableCollector(ajaxresponse, info);
            } else if (dothis.equals("AJAX_ENABLECOLLECTOR")) {
                this.enableCollector(ajaxresponse, info);
            } else if (dothis.equals("AJAX_REBOOTCOLLECTOR")) {
                this.rebootCollector(ajaxresponse, info);
            } else if (dothis.equals("AJAX_UPGRADECOLLECTOR")) {
                this.upgradeCollector(ajaxresponse, info);
            } else if (dothis.equals("AJAX_RERUNEXECUTION")) {
                this.rerunExecution(ajaxresponse, info);
            } else if (dothis.equals("AJAX_GETCOLLECTORLOG")) {
                this.getCollectorLog(ajaxresponse, info);
            } else if (dothis.equals("AJAX_GETINSTRUMENTLOG")) {
                this.getInstrumentLog(ajaxresponse, info);
            }
        }
        catch (Exception e) {
            Trace.logError("Unable to execute LogViewer command", e);
            ajaxresponse.setError(e.getMessage());
        }
        ajaxresponse.print();
    }

    private void getCollectorLog(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        String commandid = this.sendCollectorCommand(collectorid, "", "COLLECTORCOMMAND_GETCOLLECTORLOG");
        ajaxresponse.addCallbackArgument("collectorid", collectorid);
        ajaxresponse.addCallbackArgument("collectorcommandid", commandid);
    }

    private void getInstrumentLog(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String instrumentid = ajaxresponse.getRequestParameter("instrumentid");
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        if (collectorid.length() == 0 && instrumentid.length() > 0) {
            collectorid = this.getCollectorIdFromInstrument(instrumentid);
        }
        String commandid = this.sendCollectorCommand(collectorid, instrumentid, "COLLECTORCOMMAND_GETINSTRUMENTLOG");
        ajaxresponse.addCallbackArgument("collectorid", collectorid);
        ajaxresponse.addCallbackArgument("commandid", commandid);
    }

    private void rerunExecution(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String executionId = ajaxresponse.getRequestParameter("executionid");
        if (executionId.length() <= 0) {
            throw new SapphireException("No execution provided");
        }
        PropertyList properties = new PropertyList();
        properties.setProperty("rerunexecutionid", executionId);
        this.getActionProcessor().processAction("AttachmentOperationProcessor", "1", properties, true, true);
    }

    private void upgradeCollector(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        String upgradeMode = ajaxresponse.getRequestParameter("upgrademode");
        this.sendCollectorCommand(collectorid, "", "COLLECTORCOMMAND_UPGRADE", upgradeMode);
    }

    private void markDataCaptureReady(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String datacaptureid = ajaxresponse.getRequestParameter("datacaptureid");
        DataSet dc = this.getQueryProcessor().getPreparedSqlDataSet("SELECT datacapturestatus FROM datacapture WHERE datacaptureid=?", new Object[]{datacaptureid});
        if (dc != null && dc.getRowCount() == 1) {
            String status = dc.getValue(0, "datacapturestatus", "");
            if (status.length() <= 0 || !status.equalsIgnoreCase("Pending Processing")) {
                throw new SapphireException("Manual triggering of Processing can only be done on Captures that are Pending Processing.");
            }
        } else {
            throw new SapphireException("Failed to obtain data capture status.");
        }
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_DataCapture");
        props.setProperty("keyid1", datacaptureid);
        props.setProperty("datacapturestatus", "Ready");
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void pauseCollector(AjaxResponse ajaxresponse, ConnectionInfo info, String pausedflag) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_SDMSCollector");
        props.setProperty("keyid1", collectorid);
        props.setProperty("pausedflag", pausedflag);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void pauseInstrument(AjaxResponse ajaxresponse, ConnectionInfo info, String pausedflag) throws SapphireException {
        String instrumentid = ajaxresponse.getRequestParameter("instrumentid");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "Instrument");
        props.setProperty("keyid1", instrumentid);
        props.setProperty("sdmspausedflag", pausedflag);
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void rebootInstrument(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String instrumentid = ajaxresponse.getRequestParameter("instrumentid");
        String collectorid = this.getCollectorIdFromInstrument(instrumentid);
        PropertyList commandRequest = new PropertyList();
        commandRequest.setProperty("instrumentid", instrumentid);
        try {
            SDMSCommandHandler sdmsCommandHandler = new SDMSCommandHandler();
            sdmsCommandHandler.setConnectionId(this.getConnectionId());
            PropertyList configProps = sdmsCommandHandler.processCommand("COMMAND_GETINSTRUMENTCONFIGPROPS", commandRequest);
            DataSet ds = new DataSet(new JSONObject(configProps.getProperty("instruments_dataset")));
            String configHash = ds.getValue(0, "_confighash");
            PropertyList startupState = SDMSUtil.getCollectorStartupState(this.getQueryProcessor(), collectorid);
            PropertyListCollection instruments = startupState.getCollectionNotNull("instruments");
            PropertyList find = instruments.find("instrumentid", instrumentid);
            if (find != null) {
                find.setProperty("confighash", configHash);
            }
            this.getQueryProcessor().execPreparedUpdate("UPDATE sdmscollector SET startupstate=? WHERE sdmscollectorid=?", new String[]{startupState.toXMLString(), collectorid});
            this.getQueryProcessor().execPreparedUpdate("UPDATE instrument SET rebootrequiredflag = null WHERE instrumentid=?", new String[]{instrumentid});
            this.sendCollectorCommand(collectorid, instrumentid, "COLLECTORCOMMAND_REBOOTINSTRUMENT", configProps.getProperty("instruments_dataset"));
            ajaxresponse.addCallbackArgument("collectorid", collectorid);
        }
        catch (JSONException e) {
            Trace.logError("Unable to reboot instrument:" + e.getMessage(), e);
            ajaxresponse.setError(e.getMessage());
        }
    }

    private void disableCollector(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_SDMSCollector");
        props.setProperty("keyid1", collectorid);
        props.setProperty("disabledflag", "Y");
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void enableCollector(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_SDMSCollector");
        props.setProperty("keyid1", collectorid);
        props.setProperty("disabledflag", "N");
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private void rebootCollector(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        QueryProcessor qp = this.getQueryProcessor();
        DataSet collectors = qp.getPreparedSqlDataSet("SELECT * FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{collectorid}, true);
        boolean internal = collectors.getValue(0, "internalflag").equals("Y");
        LAM lam = AutomationService.getLAM(info.getDatabaseId());
        if (internal && lam.getCollectorHolder(collectorid) != null) {
            lam.stopCollector(collectorid);
        } else {
            this.sendCollectorCommand(collectorid, "", "COLLECTORCOMMAND_REBOOT");
            ajaxresponse.addCallbackArgument("collectorid", collectorid);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    private JSONObject getCollectorCommandReply(String collectorid, String commandid) throws SapphireException {
        JSONObject returnObject;
        block12: {
            block13: {
                returnObject = new JSONObject();
                QueryProcessor qp = this.getQueryProcessor();
                DataSet ds = qp.getPreparedSqlDataSet("SELECT commandtype, commandresponse FROM sdmscollectorcommand WHERE sdmscollectorid=? AND sdmscollectorcommandid=? AND commandstatusflag=?", (Object[])new String[]{collectorid, commandid, "R"}, true);
                if (ds.size() <= 0) break block13;
                String commandtype = ds.getValue(0, "commandtype");
                String commandresponse = ds.getValue(0, "commandresponse");
                try {
                    returnObject.put("commandresponse", commandresponse);
                    returnObject.put("collectorid", collectorid);
                    returnObject.put("commandid", commandid);
                    returnObject.put("commandtype", commandtype);
                }
                catch (Exception exception) {
                    this.getQueryProcessor().execPreparedUpdate("DELETE FROM sdmscollectorcommand WHERE sdmscollectorid=? AND sdmscollectorcommandid=?", new Object[]{collectorid, commandid});
                    break block12;
                    catch (Throwable throwable) {
                        this.getQueryProcessor().execPreparedUpdate("DELETE FROM sdmscollectorcommand WHERE sdmscollectorid=? AND sdmscollectorcommandid=?", new Object[]{collectorid, commandid});
                        throw throwable;
                    }
                }
                this.getQueryProcessor().execPreparedUpdate("DELETE FROM sdmscollectorcommand WHERE sdmscollectorid=? AND sdmscollectorcommandid=?", new Object[]{collectorid, commandid});
                break block12;
            }
            DateTimeUtil dtu = new DateTimeUtil();
            Timestamp deleteCommands = dtu.getTimestamp("now-1m");
            int expired = this.getQueryProcessor().getPreparedCount("SELECT count(*) FROM sdmscollectorcommand WHERE sdmscollectorid=? AND sdmscollectorcommandid=? AND commanddt < ?", new Object[]{collectorid, commandid, deleteCommands});
            if (expired > 0) {
                this.getQueryProcessor().execPreparedUpdate("DELETE FROM sdmscollectorcommand WHERE sdmscollectorid=? AND sdmscollectorcommandid=?", new Object[]{collectorid, commandid});
                try {
                    returnObject.put("commandresponse", "");
                    returnObject.put("collectorid", collectorid);
                    returnObject.put("commandid", commandid);
                    returnObject.put("commandtype", "");
                    returnObject.put("error", collectorid + ";" + commandid);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
        try {
            JSONObject commandRes;
            if (returnObject != null && returnObject.has("commandresponse") && returnObject.getString("commandresponse").length() > 0 && (commandRes = new JSONObject(returnObject.getString("commandresponse"))).has("runnables")) {
                JSONArray runnables = commandRes.getJSONArray("runnables");
                for (int r = 0; r < runnables.length(); ++r) {
                    JSONObject runnable = runnables.getJSONObject(r);
                    if (runnable.has("lastcapturetime") && runnable.getString("lastcapturetime").length() > 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(Long.parseLong(runnable.getString("lastcapturetime")));
                        RelativeDateFormat relativeDateFormat = new RelativeDateFormat(false, this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom", false), this.getTranslationProcessor());
                        String relativedate = relativeDateFormat.format(cal.getTime());
                        runnable.put("lastcapturerel", relativedate);
                        continue;
                    }
                    runnable.put("lastcapturerel", "");
                }
                returnObject.put("commandresponse", commandRes.toString());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return returnObject;
    }

    private void getCollectorCommandReply(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        block18: {
            String collectorid = ajaxresponse.getRequestParameter("collectorid");
            String commandid = ajaxresponse.getRequestParameter("commandid");
            boolean renderhtml = ajaxresponse.getRequestParameter("html").equalsIgnoreCase("Y");
            JSONObject commandReturn = this.getCollectorCommandReply(collectorid, commandid);
            try {
                if (commandReturn.has("error")) {
                    ajaxresponse.setError(commandReturn.getString("error"));
                    break block18;
                }
                String commandresponse = commandReturn.getString("commandresponse");
                String commandtype = commandReturn.getString("commandtype");
                if (commandtype.equals("COLLECTORCOMMAND_GETCOLLECTORLOG")) {
                    try {
                        JSONObject response = new JSONObject(commandresponse);
                        JSONArray log = response.optJSONArray("log");
                        StringBuilder logHTML = this.buildLogHTML(log);
                        commandresponse = logHTML.toString();
                    }
                    catch (JSONException response) {}
                } else if (commandtype.equals("COLLECTORCOMMAND_GETINSTRUMENTLOG")) {
                    try {
                        JSONObject response = new JSONObject(commandresponse);
                        JSONArray log = response.optJSONArray("log");
                        StringBuilder logHTML = this.buildLogHTML(log);
                        commandresponse = logHTML.toString();
                    }
                    catch (JSONException response) {}
                } else if (commandtype.equals("COLLECTORCOMMAND_GETCOLLECTORSTATE") && renderhtml) {
                    StringBuilder stateHTML = new StringBuilder();
                    try {
                        JSONObject jsonState = new JSONObject(commandresponse);
                        this.buildCollectorStateHTML(stateHTML, jsonState);
                        commandresponse = stateHTML.toString();
                    }
                    catch (JSONException jsonState) {}
                } else if (commandtype.equals("COLLECTORCOMMAND_GETINSTRUMENTSTATE") && renderhtml) {
                    StringBuilder instrumentStateHTML = new StringBuilder();
                    StringBuilder emulatorStateHTML = new StringBuilder();
                    try {
                        JSONObject jsonState = new JSONObject(commandresponse);
                        this.buildInstrumentStateHTML(instrumentStateHTML, jsonState);
                        this.buildEmulatorStateHTML(emulatorStateHTML, jsonState);
                        JSONObject jso = new JSONObject();
                        jso.put("instrumentstate", instrumentStateHTML.toString());
                        jso.put("emulatorstate", emulatorStateHTML.toString());
                        commandresponse = jso.toString();
                    }
                    catch (JSONException jSONException) {
                        // empty catch block
                    }
                }
                ajaxresponse.addCallbackArgument("collectorid", collectorid);
                ajaxresponse.addCallbackArgument("commandid", commandid);
                ajaxresponse.addCallbackArgument("commandtype", commandtype);
                ajaxresponse.addCallbackArgument("commandresponse", commandresponse);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    private void showCollectorDetails(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        StringBuilder foldersHTML = new StringBuilder();
        StringBuilder startupHTML = new StringBuilder();
        StringBuilder stateHTML = new StringBuilder();
        String incidentHTML = "";
        String collectorCommandid = "";
        QueryProcessor qp = this.getQueryProcessor();
        boolean requiresSetUp = false;
        boolean paused = false;
        boolean disabled = false;
        boolean internalflag = false;
        if (collectorid.length() == 0 || collectorid.contains(";")) {
            if (collectorid.contains(";")) {
                DataSet collectorDS = qp.getSqlDataSet(this.buildSelect("sdmscollector", "sdmscollectorid", collectorid), true);
                int total = collectorDS.getRowCount();
                if (total > 0) {
                    stateHTML.append("").append(total).append(" Collector").append(total > 1 ? "s" : "").append(" discovered");
                    stateHTML.append("<br>");
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("internalflag", "Y");
                    DataSet filteredDS = collectorDS.getFilteredDataSet(filter);
                    int internal = filteredDS.getRowCount();
                    filter = new HashMap();
                    filter.put("laststartdt", null);
                    filteredDS = collectorDS.getFilteredDataSet(filter);
                    int unsetup = filteredDS.getRowCount();
                    if (internal > 0) {
                        stateHTML.append("").append(internal).append(" Collector").append(internal > 1 ? "s are " : " is ").append("Internal");
                    } else {
                        stateHTML.append("There are no Internal Collectors");
                    }
                    stateHTML.append(" and ");
                    int external = total - internal;
                    if (external > 0) {
                        stateHTML.append("").append(external).append(" Collector").append(external > 1 ? "s are " : " is ").append("External");
                    } else {
                        stateHTML.append("There are no External Collectors");
                    }
                    stateHTML.append("<br>");
                    if (unsetup > 0) {
                        stateHTML.append("There").append(unsetup > 1 ? " are " : " is ").append(unsetup).append(" Collector").append(unsetup > 1 ? "s " : " ").append("to set up");
                    }
                } else {
                    stateHTML.append("No Collectors").append(" discovered");
                    stateHTML.append("<br>");
                    stateHTML.append("Click the 'Add Collector' button to start adding Collectors");
                }
            } else {
                stateHTML.append("No Collector Selected");
            }
        } else {
            DataSet collectorDS = qp.getPreparedSqlDataSet("SELECT * FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{collectorid}, true);
            paused = collectorDS.getValue(0, "pausedflag").equals("Y");
            disabled = collectorDS.getValue(0, "disabledflag").equals("Y");
            internalflag = collectorDS.getValue(0, "internalflag").equals("Y");
            requiresSetUp = !internalflag && collectorDS.getValue(0, "laststartdt", "").length() == 0;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (requiresSetUp) {
                stateHTML.append("Collector ").append(collectorid).append(" has not been set up.<br>Download the installer and continue setting up.");
                startupHTML.append("No startup details found");
            } else {
                int i;
                String storagepath = collectorDS.getValue(0, "storagepathremote");
                if (storagepath.length() > 0) {
                    foldersHTML.append("<table class=\"sdms_table\"><tbody>");
                    foldersHTML.append("<tr><th colspan=\"3\">Folders in " + storagepath + " from today</th></tr>");
                    Path storageRoot = Paths.get(storagepath, new String[0]);
                    File[] files = storageRoot.toFile().listFiles(pathname -> pathname.lastModified() > System.currentTimeMillis() - 86400000L);
                    if (files != null && files.length > 1) {
                        try {
                            Arrays.sort(files, (o1, o2) -> {
                                if (o1.lastModified() > o2.lastModified()) {
                                    return -1;
                                }
                                if (o1.lastModified() < o2.lastModified()) {
                                    return 1;
                                }
                                return 0;
                            });
                        }
                        catch (Exception unsetup) {
                            // empty catch block
                        }
                    }
                    int total = files.length;
                    int remain = 0;
                    if (total > 30) {
                        total = 30;
                        remain = files.length - 50;
                    }
                    for (i = 0; i < total; ++i) {
                        File file = files[i];
                        long filelength = file.length();
                        String size = filelength < 5000L ? file.length() + " bytes" : (filelength < 5000000L ? filelength / 1024L + " KB" : filelength / 0x100000L + " MB");
                        foldersHTML.append("<tr>");
                        foldersHTML.append("<td>").append(file.getName()).append("</td>");
                        foldersHTML.append("<td>").append(sdf.format(file.lastModified())).append("</td>");
                        foldersHTML.append("<td>").append(size).append("</td>");
                        foldersHTML.append("</tr>");
                    }
                    if (total == 0) {
                        foldersHTML.append("<tr><td>No files found</td></tr>");
                    } else if (remain > 0) {
                        foldersHTML.append("<tr><td colspan=\"3\">").append(remain).append(" more folder").append(remain > 1 ? "s" : "").append("...</td></tr>");
                    }
                } else {
                    foldersHTML.append("No folder storage path found");
                }
                String startupState = collectorDS.getValue(0, "startupstate");
                if (startupState.length() > 0) {
                    PropertyList startupPL = new PropertyList();
                    startupPL.setPropertyList(startupState);
                    startupHTML.append("<table>");
                    try {
                        long millis = Long.parseLong(startupPL.getProperty("startupdate", "0"));
                        startupHTML.append("<tr>");
                        startupHTML.append("<td>Last Startup</td>");
                        startupHTML.append("<td>" + sdf.format(new Date(millis)) + "</td>");
                        startupHTML.append("</tr>");
                    }
                    catch (NumberFormatException millis) {
                        // empty catch block
                    }
                    startupHTML.append("<tr>");
                    startupHTML.append("<td>LabVantage Version</td>");
                    startupHTML.append("<td>" + startupPL.getProperty("labvantageversion") + " Build " + startupPL.getProperty("labvantagebuild") + "</td>");
                    startupHTML.append("</tr>");
                    startupHTML.append("<tr>");
                    startupHTML.append("<td>Platform</td>");
                    startupHTML.append("<td>" + startupPL.getProperty("osname") + "</td>");
                    startupHTML.append("</tr>");
                    startupHTML.append("<tr>");
                    startupHTML.append("<td style=\"vertical-align:top\">Startup Log</td>");
                    startupHTML.append("<td>");
                    String log = startupPL.getProperty("startuplog");
                    String[] parts = StringUtil.split(log, "!|!");
                    for (i = 0; i < parts.length; ++i) {
                        startupHTML.append(parts[i] + "<br>");
                    }
                    startupHTML.append("</td>");
                    startupHTML.append("</tr>");
                    startupHTML.append("</table>");
                } else {
                    startupHTML.append("No startup details found");
                }
                boolean showIncidents = ajaxresponse.getRequestParameter("showincidents", "N").equalsIgnoreCase("Y");
                if (showIncidents) {
                    incidentHTML = this.getIncidentHTML(qp, "LV_SDMSCollector", collectorid);
                }
                if (ajaxresponse.getRequestParameter("collectorstate").length() == 0) {
                    collectorCommandid = this.sendCollectorCommand(collectorid, "", "COLLECTORCOMMAND_GETCOLLECTORSTATE");
                } else {
                    try {
                        this.buildCollectorStateHTML(stateHTML, new JSONObject(ajaxresponse.getRequestParameter("collectorstate")));
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        ajaxresponse.addCallbackArgument("collectorid", collectorid);
        ajaxresponse.addCallbackArgument("foldershtml", foldersHTML.toString());
        ajaxresponse.addCallbackArgument("startuphtml", startupHTML.toString());
        ajaxresponse.addCallbackArgument("statehtml", stateHTML.toString());
        ajaxresponse.addCallbackArgument("incidenthtml", incidentHTML);
        ajaxresponse.addCallbackArgument("collectorcommandid", collectorCommandid);
        ajaxresponse.addCallbackArgument("requiressetup", requiresSetUp ? "Y" : "N");
        ajaxresponse.addCallbackArgument("paused", paused ? "Y" : "N");
        ajaxresponse.addCallbackArgument("disabled", disabled ? "Y" : "N");
        ajaxresponse.addCallbackArgument("internalflag", internalflag ? "Y" : "N");
    }

    private void showInstrumentDetails(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        String instrumentid = ajaxresponse.getRequestParameter("instrumentid");
        StringBuilder stateHTML = new StringBuilder();
        StringBuilder emulatorHTML = new StringBuilder();
        QueryProcessor qp = this.getQueryProcessor();
        StringBuilder captureHTML = new StringBuilder();
        String incidentHTML = "";
        String collectorCommandid = "";
        if (collectorid.length() == 0) {
            // empty if block
        }
        boolean instrumentPaused = false;
        if (instrumentid.length() == 0 || instrumentid.contains(";")) {
            if (instrumentid.contains(";")) {
                DataSet instrumentDS = qp.getSqlDataSet(this.buildSelect("instrument", "instrumentid", instrumentid), true);
                int total = instrumentDS.getRowCount();
                if (total > 0) {
                    stateHTML.append("").append(total).append(" Instrument").append(total > 1 ? "s" : "").append(" discovered");
                    stateHTML.append("<br>");
                } else {
                    stateHTML.append("No Instruments").append(" discovered");
                    stateHTML.append("<br>");
                }
            } else {
                stateHTML.append("No Collector Selected");
                stateHTML.append("");
            }
        } else {
            DataSet instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instrumentid, instrumentmodelid, instrumenttype, instrumentdesc,sdmspausedflag,inserviceflag,instrumentstatus, rebootrequiredflag FROM instrument WHERE instrumentid = ?", (Object[])new String[]{instrumentid});
            instrumentPaused = instrument.getValue(0, "sdmspausedflag", "N").equalsIgnoreCase("Y");
            DataSet captures = qp.getPreparedSqlDataSet("SELECT * FROM datacapture WHERE instrumentid=? order by createdt DESC", (Object[])new String[]{instrumentid}, true);
            if (captures.size() > 0) {
                captureHTML.append("<table class=\"sdms_table\"><tbody>");
                captureHTML.append("<tr>");
                captureHTML.append("<th>Id</th><th>Time</th><th>Status</th><th>Log</th>");
                captureHTML.append("</tr>");
                int total = captures.size();
                int remain = 0;
                if (total > 20) {
                    total = 20;
                    remain = captures.size() - 20;
                }
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss ");
                for (int i = 0; i < total; ++i) {
                    String log;
                    captureHTML.append("<tr>");
                    captureHTML.append("<td>" + captures.getValue(i, "datacaptureid") + "</td>");
                    Calendar capturedt = captures.getCalendar(i, "captureddt");
                    if (capturedt == null) {
                        captureHTML.append("<td>").append(df.format(captures.getCalendar(i, "createdt").getTime())).append("</td>");
                    } else {
                        captureHTML.append("<td>").append(df.format(capturedt.getTime())).append("</td>");
                    }
                    captureHTML.append("<td>" + captures.getValue(i, "datacapturestatus").substring(0, 1).toUpperCase() + captures.getValue(i, "datacapturestatus").substring(1) + "</td>");
                    String dis = log = StringUtil.replaceAll(captures.getValue(i, "datacapturelog"), "!|!", "\n");
                    boolean trimmed = false;
                    if (log.contains("\n")) {
                        dis = log.substring(0, log.indexOf("\n"));
                        trimmed = true;
                    }
                    if (dis.length() > 80) {
                        dis = dis.substring(0, 80);
                        trimmed = true;
                    }
                    if (trimmed) {
                        dis = dis + "...";
                    }
                    captureHTML.append("<td>").append("<span onclick=\"sdmsMonitorGizmo.viewLog(this)\" style=\"cursor:pointer;\" title=\"Click to view\" data-log=\"").append(HttpUtil.encodeURIComponent(log)).append("\">").append(dis).append("</span>").append("</td>");
                    captureHTML.append("</tr>");
                }
                if (remain > 0) {
                    captureHTML.append("<tr>");
                    captureHTML.append("<td colspan=\"3\">").append(remain).append(" more data capture").append(remain > 1 ? "s" : "").append("...</td>");
                    captureHTML.append("</tr>");
                }
                captureHTML.append("</tbody></table>");
            } else {
                captureHTML.append("No data captures found");
            }
            boolean showIncidents = ajaxresponse.getRequestParameter("showincidents", "N").equalsIgnoreCase("Y");
            if (showIncidents) {
                incidentHTML = this.getIncidentHTML(qp, "Instrument", instrumentid);
            }
            if (collectorid.length() > 0) {
                collectorCommandid = this.sendCollectorCommand(collectorid, instrumentid, "COLLECTORCOMMAND_GETINSTRUMENTSTATE");
            } else {
                collectorid = this.getCollectorIdFromInstrument(instrumentid);
                collectorCommandid = this.sendCollectorCommand(collectorid, instrumentid, "COLLECTORCOMMAND_GETINSTRUMENTSTATE");
            }
        }
        ajaxresponse.addCallbackArgument("collectorid", collectorid);
        ajaxresponse.addCallbackArgument("instrumentid", instrumentid);
        ajaxresponse.addCallbackArgument("capturehtml", captureHTML.toString());
        ajaxresponse.addCallbackArgument("statehtml", stateHTML.toString());
        ajaxresponse.addCallbackArgument("emulatorhtml", emulatorHTML.toString());
        ajaxresponse.addCallbackArgument("incidenthtml", incidentHTML);
        ajaxresponse.addCallbackArgument("collectorcommandid", collectorCommandid);
        ajaxresponse.addCallbackArgument("paused", instrumentPaused ? "Y" : "N");
    }

    private String buildSelect(String table, String idCol, String ids) {
        StringBuilder out = new StringBuilder();
        out.append("SELECT * FROM ").append(table);
        StringBuilder sql = new StringBuilder();
        String[] dc = StringUtil.split(ids, ";");
        StringBuilder part = new StringBuilder();
        for (int i = 0; i < dc.length; ++i) {
            if (part.length() > 0) {
                part.append(",");
            }
            part.append("'").append(dc[i]).append("'");
            if (i < 900) continue;
            if (sql.length() > 0) {
                sql.append(" OR ");
            }
            sql.append(idCol).append(" IN ");
            sql.append("(").append((CharSequence)part).append(")");
            part = new StringBuilder();
        }
        if (part.length() > 0) {
            if (sql.length() > 0) {
                sql.append(" OR ");
            }
            sql.append(idCol).append(" IN ");
            sql.append("(").append((CharSequence)part).append(")");
        }
        if (sql.length() > 0) {
            out.append(" WHERE ").append((CharSequence)sql);
        }
        return out.toString();
    }

    private void showDataCaptureDetails(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String datacaptureid = ajaxresponse.getRequestParameter("datacaptureid");
        String sdiattachmentid = ajaxresponse.getRequestParameter("sdiattachmentid");
        StringBuilder stateHTML = new StringBuilder();
        StringBuilder executionHTML = new StringBuilder();
        StringBuilder linkedHTML = new StringBuilder();
        StringBuilder metaHTML = new StringBuilder();
        StringBuilder attachmentHTML = new StringBuilder();
        QueryProcessor qp = this.getQueryProcessor();
        String incidentHTML = "";
        if (datacaptureid.length() == 0 || datacaptureid.contains(";")) {
            if (datacaptureid.contains(";")) {
                DataSet datacaptureDS;
                DataSet dataCaptureMore = datacaptureDS = qp.getSqlDataSet(this.buildSelect("datacapture", "datacaptureid", datacaptureid), true);
                int total = datacaptureDS.getRowCount();
                if (total > 0) {
                    stateHTML.append("").append(total).append(" Data Capture").append(total > 1 ? "s" : "").append(" discovered");
                    if (dataCaptureMore.getRowCount() > total) {
                        int t = dataCaptureMore.getRowCount() - total;
                        stateHTML.append(" (<a href=\"javascript:groupGizmo.grid.refreshAll(groupGizmo.parentGrid);\" title=\"Click to refresh\">found ").append(t).append(" more Data Capture").append(t > 1 ? "s" : "").append("...</a>)");
                    }
                    stateHTML.append("<br>");
                    HashMap<String, String> filter = new HashMap<String, String>();
                    filter.put("datacapturestatus", "Captured");
                    DataSet filteredDS = datacaptureDS.getFilteredDataSet(filter);
                    int captured = filteredDS.getRowCount();
                    filter = new HashMap();
                    filter.put("datacapturestatus", "Capturing");
                    filteredDS = datacaptureDS.getFilteredDataSet(filter);
                    int capturing = filteredDS.getRowCount();
                    filter = new HashMap();
                    filter.put("datacapturestatus", "Processing");
                    filteredDS = datacaptureDS.getFilteredDataSet(filter);
                    int processing = filteredDS.getRowCount();
                    filter = new HashMap();
                    filter.put("datacapturestatus", "Processed");
                    filteredDS = datacaptureDS.getFilteredDataSet(filter);
                    int processed = filteredDS.getRowCount();
                    if (capturing > 0) {
                        stateHTML.append("Data Captures ").append("Capturing").append(": ").append(capturing);
                    } else {
                        stateHTML.append("No Data Captures in ").append("Capturing").append(" status.");
                    }
                    stateHTML.append("<br>");
                    if (captured > 0) {
                        stateHTML.append("Data Captures ").append("Captured").append(": ").append(captured);
                    } else {
                        stateHTML.append("No Data Captures in ").append("Captured").append(" status.");
                    }
                    stateHTML.append("<br>");
                    if (processing > 0) {
                        stateHTML.append("Data Captures ").append("Processing").append(": ").append(processing);
                    } else {
                        stateHTML.append("No Data Captures in ").append("Processing").append(" status.");
                    }
                    stateHTML.append("<br>");
                    if (processed > 0) {
                        stateHTML.append("Data Captures ").append("Processed").append(": ").append(processed);
                    } else {
                        stateHTML.append("No Data Captures in ").append("Processed").append(" status.");
                    }
                    stateHTML.append("<br>");
                } else {
                    stateHTML.append("No Data Captures").append(" discovered");
                    if (dataCaptureMore.getRowCount() > total) {
                        int t = dataCaptureMore.getRowCount() - total;
                        stateHTML.append(" (<a href=\"javascript:groupGizmo.grid.refreshAll(groupGizmo.parentGrid);\" title=\"Click to refresh\">").append(t).append(" Data Capture").append(t > 1 ? "s" : "").append(" on server</a>)");
                    }
                    stateHTML.append("<br>");
                }
            } else {
                stateHTML.append("No Data Capture Selected");
                stateHTML.append("");
            }
        } else {
            DataSet executions;
            DataSet capture = qp.getPreparedSqlDataSet("SELECT datacapture.*, instrument.sdmscollectorid FROM datacapture, instrument WHERE instrument.instrumentid=datacapture.instrumentid AND datacaptureid=?", (Object[])new String[]{datacaptureid}, true);
            if (capture.size() > 0) {
                String[] logLines;
                stateHTML.append("<table class=\"sdms_table\">");
                stateHTML.append("<tbody>");
                stateHTML.append("<tr>");
                stateHTML.append("<th colspan=2>").append(datacaptureid).append("</th>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                String status = capture.getValue(0, "datacapturestatus", "(not found)");
                status = status.substring(0, 1).toUpperCase() + status.substring(1);
                stateHTML.append("<td>").append("Status:").append("</td>").append("<td>").append(status).append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Created:").append("</td>").append("<td>").append(capture.getValue(0, "createdt", "")).append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Description:").append("</td>").append("<td>").append(capture.getValue(0, "datacapturedesc", "(no description)")).append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Instrument:").append("</td>").append("<td>").append(capture.getValue(0, "instrumentid", "")).append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Collector:").append("</td>").append("<td>").append(capture.getValue(0, "sdmscollectorid", "")).append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Log:").append("</td>").append("<td style=\"border:solid 1px #DDD;\">");
                for (String line : logLines = StringUtil.split(capture.getValue(0, "datacapturelog", ""), "!|!")) {
                    stateHTML.append(line).append("<br>");
                }
                stateHTML.append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("</tbody>");
                stateHTML.append("</table>");
            }
            if ((executions = qp.getPreparedSqlDataSet("SELECT e.handlerlog, e.executionstatus, e.executionid, e.createdt,  (SELECT distinct s.operationkeyid1 FROM sdiattachmentoperation s WHERE s.attachmentoperationid = e.attachmentoperationid) operationkeyid1 FROM sdiattachmentoperationexec e WHERE e.sdcid=? AND e.keyid1=?", (Object[])new String[]{"LV_DataCapture", datacaptureid}, true)).size() > 0) {
                MaintAttachmentOperationExecution maintAttachmentOperationExecution = new MaintAttachmentOperationExecution(this.pageContext);
                maintAttachmentOperationExecution.setPrimary("LV_DataCapture", datacaptureid, "", "");
                executionHTML.append(maintAttachmentOperationExecution.getHtml());
            } else {
                executionHTML.append("No Executions Found");
            }
            DataSet links = qp.getPreparedSqlDataSet("SELECT * FROM sdidatacapture WHERE datacaptureid=?", (Object[])new String[]{datacaptureid}, true);
            if (links.size() > 0) {
                MaintDataCapture maintDataCapture = new MaintDataCapture(this.pageContext);
                maintDataCapture.setPrimary("LV_DataCapture", datacaptureid, "", "");
                linkedHTML.append(maintDataCapture.getHtml());
            } else {
                linkedHTML.append("No Links Found");
            }
            Files files = new Files(this.pageContext);
            files.setPrimary("LV_DataCapture", datacaptureid, null, null);
            files.setViewOnly(true);
            files.setElementid("datacaptureattachments");
            PropertyList attProps = new PropertyList();
            attProps.setProperty("sdcid", "LV_DataCapture");
            attProps.setProperty("showviewpicker", "N");
            attProps.setProperty("showbuttons", "N");
            files.setElementProperties(attProps);
            files.setAjax(false);
            attachmentHTML.append(files.getHtml());
            DataSet attributes = qp.getPreparedSqlDataSet("SELECT * FROM sdiattribute WHERE sdcid=? AND keyid1=?", (Object[])new String[]{"LV_DataCapture", datacaptureid}, true);
            if (attributes.size() > 0) {
                MaintAttribute.renderSingleSDIAttributes(metaHTML, this.getSDCProcessor().getPropertyList("LV_DataCapture"), attributes, false, attributes.getRowCount(), null, "sdms_dcattributes", "sdms_dcattributes_header", "sdms_dcattributes_value", "sdmsMonitorGizmo", "attributesView", 0, false, null, this.getTranslationProcessor(), this.getQueryProcessor(), this.getConnectionProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
            } else {
                metaHTML.append("No Meta Data Found");
            }
            boolean showIncidents = ajaxresponse.getRequestParameter("showincidents", "N").equalsIgnoreCase("Y");
            if (showIncidents) {
                incidentHTML = this.getIncidentHTML(qp, "LV_DataCapture", datacaptureid);
            }
        }
        ajaxresponse.addCallbackArgument("datacaptureid", datacaptureid);
        ajaxresponse.addCallbackArgument("sdiattachmentid", sdiattachmentid);
        ajaxresponse.addCallbackArgument("executionhtml", executionHTML.toString());
        ajaxresponse.addCallbackArgument("linkedsdi", linkedHTML.toString());
        ajaxresponse.addCallbackArgument("metadata", metaHTML.toString());
        ajaxresponse.addCallbackArgument("statehtml", stateHTML.toString());
        ajaxresponse.addCallbackArgument("attachmentHTML", attachmentHTML);
        ajaxresponse.addCallbackArgument("incidenthtml", incidentHTML);
    }

    private void showAttachmentDetails(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String datacaptureid = ajaxresponse.getRequestParameter("datacaptureid");
        String sdiattachmentid = ajaxresponse.getRequestParameter("sdiattachmentid");
        StringBuilder stateHTML = new StringBuilder();
        StringBuilder executionHTML = new StringBuilder();
        StringBuilder metaHTML = new StringBuilder();
        QueryProcessor qp = this.getQueryProcessor();
        String incidentHTML = "";
        if (datacaptureid.length() != 0 && !datacaptureid.contains(";")) {
            Attachment attachment = new Attachment();
            attachment.setSDIAttachmentId(sdiattachmentid);
            attachment.setSDCId("LV_DataCapture");
            AttachmentProcessor at = new AttachmentProcessor(this.getConnectionid());
            sapphire.attachment.Attachment att = at.getSDIAttachment(attachment, Attachment.ThumbnailGeneration.GENERATEANDSTORE);
            if (att != null) {
                stateHTML.append("<table class=\"sdms_table\">");
                stateHTML.append("<tbody>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Description: ").append("</td>").append("<td>").append(att.getDescription() != null && att.getDescription().length() > 0 ? att.getDescription() : "(no description)").append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Filename: ").append("</td>").append("<td>").append(att.getSourceFilename() != null && att.getSourceFilename().length() > 0 ? att.getSourceFilename() : "(no filename)").append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Attachment Class: ").append("</td>").append("<td>").append(att.getAttachmentClass() != null && att.getAttachmentClass().length() > 0 ? att.getAttachmentClass() : "(no class)").append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                stateHTML.append("<td>").append("Size: ").append("</td>").append("<td>").append(att.getSize() > 0L ? att.getSize() + " bytes" : "(no size)").append("</td>");
                stateHTML.append("</tr>");
                stateHTML.append("<tr>");
                String href = "rc?command=attachment&mode=view&sdcid=" + att.getSDCId() + "&keyid1=" + att.getKeyId1();
                if (att.getKeyId2() != null && att.getKeyId2().length() > 0) {
                    href = href + "&keyid2=" + att.getKeyId2();
                }
                if (att.getKeyId3() != null && att.getKeyId3().length() > 0) {
                    href = href + "&keyid3=" + att.getKeyId3();
                }
                href = href + "&attachmentnum=" + att.getAttachmentNum();
                stateHTML.append("<td colspan=2><div class=\"sdms_thumb\"><a href=\"").append(href).append("\" target=\"_blank\">").append("<img src=\"rc?command=image&attachmentthumb=").append(att.getSDCId()).append(";").append(att.getKeyId1()).append(att.getKeyId2() != null && att.getKeyId2().length() > 0 ? ";" + att.getKeyId2() : ";(null)").append(att.getKeyId3() != null && att.getKeyId3().length() > 0 ? ";" + att.getKeyId3() : ";(null)").append(";").append(att.getAttachmentNum()).append("&thumbnailflag=Y&nocache=Y\">").append("</a></div></td>");
                stateHTML.append("</tr>");
                stateHTML.append("</tbody>");
                stateHTML.append("</table>");
            } else {
                stateHTML.append("Could not find attachment.");
            }
            DataSet attributes = qp.getPreparedSqlDataSet("SELECT * FROM sdiattribute WHERE sdcid=? AND keyid1=?", (Object[])new String[]{"SDIAttachment", sdiattachmentid}, true);
            if (attributes.size() > 0) {
                MaintAttribute.renderSingleSDIAttributes(metaHTML, this.getSDCProcessor().getPropertyList("LV_DataCapture"), attributes, false, attributes.getRowCount(), null, "sdms_dcattributes", "sdms_dcattributes_header", "sdms_dcattributes_value", "sdmsMonitorGizmo", "attributesView", 0, false, null, this.getTranslationProcessor(), this.getQueryProcessor(), this.getConnectionProcessor(), this.getConnectionProcessor().getSapphireConnection(), this.logger);
            } else {
                metaHTML.append("No Meta Data Found");
            }
            boolean showIncidents = ajaxresponse.getRequestParameter("showincidents", "N").equalsIgnoreCase("Y");
            if (showIncidents) {
                incidentHTML = this.getIncidentHTML(qp, "LV_DataCapture", datacaptureid);
            }
        }
        ajaxresponse.addCallbackArgument("datacaptureid", datacaptureid);
        ajaxresponse.addCallbackArgument("sdiattachmentid", sdiattachmentid);
        ajaxresponse.addCallbackArgument("metadata", metaHTML.toString());
        ajaxresponse.addCallbackArgument("statehtml", stateHTML.toString());
        ajaxresponse.addCallbackArgument("incidenthtml", incidentHTML);
        ajaxresponse.addCallbackArgument("collectorcommandid", "");
    }

    private String getIncidentHTML(QueryProcessor qp, String sdcid, String keyid1) {
        StringBuilder incidentHTML = new StringBuilder();
        DataSet incidents = qp.getPreparedSqlDataSet("SELECT i.* FROM incident i, incidentitem ii WHERE i.incidentid=ii.incidentid AND ii.sourcesdcid=? AND ii.sourcekeyid1=? ORDER BY i.incidentdt desc", (Object[])new String[]{sdcid, keyid1}, true);
        if (incidents.size() > 0) {
            incidents.setDateDisplayFormat("logdt", new SimpleDateFormat("HH:mm:ss "));
            incidentHTML.append("<table border=\"1\" class=\"alertstable\" cellpadding=\"0\" cellspacing=\"0\">");
            incidentHTML.append("<tr style=\"background-color:lightgray\">");
            incidentHTML.append("<td>&nbsp;</td>");
            incidentHTML.append("<td>Incident</td>");
            incidentHTML.append("<td>Date</td>");
            incidentHTML.append("<td>Severity</td>");
            incidentHTML.append("<td>Type</td>");
            incidentHTML.append("<td>Description</td>");
            incidentHTML.append("<td>Details</td>");
            incidentHTML.append("</tr>");
            for (int i = 0; i < incidents.size(); ++i) {
                String incidentid = incidents.getValue(i, "incidentid");
                String incidentDt = incidents.getValue(i, "incidentdt");
                String incidentDesc = incidents.getValue(i, "incidentdesc");
                String severity = incidents.getValue(i, "severity");
                String incidentType = incidents.getValue(i, "incidenttype");
                String explanation = incidents.getValue(i, "explanation");
                boolean complete = incidents.getValue(i, "incidentstatus").equals("Completed");
                incidentHTML.append("<tr>");
                if (!complete) {
                    incidentHTML.append("<td><img src=\"rc?command=image&image=FlatBlackBell1&color=red\"/></td>");
                } else {
                    incidentHTML.append("<td>&nbsp;</td>");
                }
                incidentHTML.append("<td>" + incidentid + "</td>");
                incidentHTML.append("<td>" + incidentDt + "</td>");
                incidentHTML.append("<td>" + severity + "</td>");
                incidentHTML.append("<td>" + incidentType + "</td>");
                incidentHTML.append("<td>" + incidentDesc + "</td>");
                incidentHTML.append("<td>" + explanation + "</td>");
                incidentHTML.append("</tr>");
            }
            incidentHTML.append("</table>");
        } else {
            incidentHTML.append("No alerts found");
        }
        return incidentHTML.toString();
    }

    private void buildInstrumentStateHTML(StringBuilder stateHTML, JSONObject instrumentState) {
        boolean isShuttingDown;
        boolean instrumentUnavailable;
        stateHTML.append("<table class=\"sdms_table\">");
        String time = new SimpleDateFormat("HH:mm:ss ").format(Calendar.getInstance().getTime());
        String started = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss");
            long millis = Long.parseLong(instrumentState.optString("starteddt", "0"));
            if (millis > 0L) {
                started = " (Running since " + sdf.format(new Date(millis)) + ")";
            }
        }
        catch (Exception sdf) {
            // empty catch block
        }
        stateHTML.append("<tr><th colspan=\"2\" style=\"text-align:center\">" + instrumentState.optString("instrumentid") + "&nbsp;&nbsp;&nbsp;&nbsp;(Last updated: " + time + ")</th></tr>");
        DataSet instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instrumentid, instrumentmodelid, instrumenttype, instrumentdesc,sdmspausedflag,inserviceflag,instrumentstatus, rebootrequiredflag FROM instrument WHERE instrumentid = ?", (Object[])new String[]{instrumentState.optString("instrumentid")});
        boolean instrumentPaused = instrument.getValue(0, "sdmspausedflag", "N").equalsIgnoreCase("Y");
        boolean rebootRequired = instrument.getValue(0, "rebootrequiredflag").equalsIgnoreCase("Y");
        boolean bl = instrumentUnavailable = instrument.getValue(0, "instrumentstatus", "").equalsIgnoreCase("Unavailable") || instrument.getValue(0, "inserviceflag", "Y").equalsIgnoreCase("N");
        if (rebootRequired) {
            stateHTML.append("<tr><td colspan=\"2\" style=\"color:red\"><b>Configuration Changes Detected. Instrument requires a Reboot</b></td></tr>");
        }
        if (instrumentPaused || instrumentUnavailable) {
            stateHTML.append("<tr><td colspan=\"2\" ><b>Instrument currently " + (instrumentPaused ? "Paused" : "Unavailable") + "</b></td></tr>");
        }
        if (isShuttingDown = instrumentState.optString("shuttingdown", "N").equals("Y")) {
            stateHTML.append("<tr><td colspan=\"2\">Shutting down...</td></tr>");
        } else {
            String storeDetails;
            String captureDetails;
            stateHTML.append("<tr><td>Description</td><td>" + instrument.getValue(0, "instrumentdesc", "(No Description Defined)") + "</td></tr>");
            stateHTML.append("<tr><td>Type / Model</td><td>" + instrument.getValue(0, "instrumenttype", "(No Type Defined)") + " / " + instrument.getValue(0, "instrumentmodelid", "(No Model Defined)") + "</td></tr>");
            stateHTML.append("<tr><td>Collector Type</td><td>" + instrumentState.optString("collectortype") + "</td></tr>");
            String relativedate = "";
            if (instrumentState.has("lastcapturetime") && instrumentState.optString("lastcapturetime").length() > 0) {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(instrumentState.getString("lastcapturetime")));
                    RelativeDateFormat relativeDateFormat = new RelativeDateFormat(false, this.getConfigurationProcessor().getPolicy("DateFormatPolicy", "Sapphire Custom", false), this.getTranslationProcessor());
                    relativedate = relativeDateFormat.format(cal.getTime());
                    instrumentState.put("lastcapturerel", relativedate);
                }
                catch (Exception cal) {
                    // empty catch block
                }
            }
            int lastwait = instrumentState.optInt("lastwait");
            if (instrumentState.optString("continuousoperation").equalsIgnoreCase("Y")) {
                stateHTML.append("<tr><td colspan=2>Continuous Operation " + started + "</td></tr>");
            } else {
                int interval = instrumentState.optInt("collectorinterval");
                stateHTML.append("<tr><td>Poll Every...</td><td>" + interval + "s" + (lastwait > interval && interval > 0 ? " <span style=\"color:red\">(Last wait: " + lastwait + "s)</span>" : "") + " " + started + "</td></tr>");
            }
            String lastCapture = instrumentState.optString("lastcapturedt");
            if (lastCapture.length() > 0) {
                stateHTML.append("<tr><td>Last Collected</td><td>" + lastCapture + " (" + instrumentState.optString("lastcapturerel") + ")</td></tr>");
            }
            if ((captureDetails = instrumentState.optString("lastcapturedesc")).length() > 0) {
                stateHTML.append("<tr><td>Collection Details</td><td>" + captureDetails + "</td></tr>");
            }
            if ((storeDetails = instrumentState.optString("laststoredesc")).length() > 0) {
                stateHTML.append("<tr><td>Store Details</td><td>" + storeDetails + "</td></tr>");
            }
            stateHTML.append("<tr><td >Collection Count</td><td>" + instrumentState.optString("collector.count") + "</td></tr>");
            stateHTML.append("<tr><td nowrap>Min Collection Duration</td><td>" + instrumentState.optString("collector.min") + "s</td></tr>");
            stateHTML.append("<tr><td nowrap>Max Collection Duration</td><td>" + instrumentState.optString("collector.max") + "s</td></tr>");
            stateHTML.append("<tr><td nowrap>Mean Collection Duration</td><td>" + instrumentState.optString("collector.mean") + "s (SD: " + instrumentState.optString("collector.stdev") + "s)</td></tr>");
            stateHTML.append("<tr><td nowrap>50th Percentile (Median)</td><td>" + instrumentState.optString("collector.median") + "s</td></tr>");
            stateHTML.append("<tr><td>75th Percentile</td><td>" + instrumentState.optString("collector.75percentile") + "s</td></tr>");
            stateHTML.append("<tr><td>95th Percentile</td><td>" + instrumentState.optString("collector.95percentile") + "s</td></tr>");
            stateHTML.append("<tr><td>99th Percentile</td><td>" + instrumentState.optString("collector.99percentile") + "s</td></tr>");
            stateHTML.append("<tr><td>99.9th Percentile</td><td>" + instrumentState.optString("collector.999percentile") + "s</td></tr>");
            String xml = instrumentState.optString("runtimeproperties", "");
            if (xml.length() > 0) {
                PropertyList runtimeProps = new PropertyList();
                try {
                    runtimeProps.setPropertyList(xml);
                    if (runtimeProps.keySet().size() > 0) {
                        stateHTML.append("<tr><td>Runtime Properties</td><td>");
                        stateHTML.append("<table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
                        for (Object propertyid : runtimeProps.keySet()) {
                            String value = runtimeProps.getProperty((String)propertyid, "&nbsp;");
                            stateHTML.append("<tr><td>" + propertyid + "</td><td>" + value + "</td></tr>");
                        }
                        stateHTML.append("</table>");
                        stateHTML.append("</td></tr>");
                    }
                }
                catch (SapphireException sapphireException) {
                    // empty catch block
                }
            }
        }
        stateHTML.append("</table>");
    }

    private void buildEmulatorStateHTML(StringBuilder stateHTML, JSONObject instrumentState) {
        boolean isemulator = instrumentState.optString("isemulator", "N").equals("Y");
        boolean isemulatorrunning = instrumentState.optString("isemulatorrunning", "N").equals("Y");
        boolean isShuttingDown = instrumentState.optString("shuttingdown", "N").equals("Y");
        if (isemulator) {
            stateHTML.append("<table class=\"sdms_table\">");
            stateHTML.append("<tr><th colspan=\"2\">" + instrumentState.optString("instrumentid") + "</th></tr>");
            if (isShuttingDown) {
                stateHTML.append("<tr><td colspan=\"2\">Shutting down...</td></tr>");
            } else {
                stateHTML.append("<tr><td>Emulator</td><td>" + (isemulator ? "Yes" : "No") + "</td></tr>");
                if (isemulator) {
                    stateHTML.append("<tr><td>Emulator Status</td><td>" + (isemulatorrunning ? "Running" : "Paused") + "</td></tr>");
                }
                if (instrumentState.optString("isemulator").equals("Y")) {
                    stateHTML.append("<tr><td>Emulate Every...</td><td>" + instrumentState.optInt("emulatorinterval") + "s</td></tr>");
                    stateHTML.append("<tr><td>Last Emulator Log</td><td>");
                    String log = instrumentState.optString("lastemulatorlog");
                    if (log == null || log.length() == 0) {
                        stateHTML.append("No log details available");
                    } else {
                        String[] parts = StringUtil.split(log, "!|!");
                        for (int j = 0; j < parts.length; ++j) {
                            stateHTML.append(parts[j] + "<br>");
                        }
                    }
                    stateHTML.append("</td></tr>");
                    stateHTML.append("<tr><td>Emulator Count</td><td>" + instrumentState.optString("emulator.count") + " emulations</td></tr>");
                    stateHTML.append("<tr><td nowrap>Mean Emulator Duration</td><td>" + instrumentState.optString("emulator.mean") + "s (SD: " + instrumentState.optString("emulator.stdev") + "s)</td></tr>");
                }
            }
            stateHTML.append("</table>");
        }
    }

    private StringBuilder buildLogHTML(JSONArray log) {
        StringBuilder out = new StringBuilder();
        out.append("<table style=\"width:100%\" cellspacing=\"0\" cellpadding=\"1\" class=\"sdms_table\">");
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss");
        out.append("<tr style=\"background-color: #F9F9F9\"><th>Date</th><th>Type</th><th>Entry</th></tr>");
        for (int i = 0; i < log.length(); ++i) {
            try {
                JSONObject row = (JSONObject)log.get(i);
                long time = row.optLong("time", -1L);
                String type = row.optString("type");
                String message = row.optString("message");
                if (time <= 0L || message.trim().length() <= 0) continue;
                out.append("<tr>");
                out.append("<td nowrap style=\"border-bottom:solid 1px #B0C4DE\">").append(sdf.format(new Date(time))).append("</td>");
                out.append("<td style=\"border-bottom:solid 1px #B0C4DE\">").append(type).append("</td>");
                out.append("<td style=\"border-bottom:solid 1px #B0C4DE\">").append(message).append("</td>");
                out.append("</tr>");
                continue;
            }
            catch (JSONException jSONException) {
                // empty catch block
            }
        }
        return out;
    }

    private void buildCollectorStateHTML(StringBuilder stateHTML, JSONObject state) throws JSONException {
        String collectorid;
        String string = collectorid = state == null ? "" : state.optString("collectorid", "");
        if (collectorid.length() == 0) {
            stateHTML.append("Fetching state...");
        } else {
            PropertyList startup = SDMSUtil.getCollectorStartupState(this.getQueryProcessor(), collectorid);
            stateHTML.append("<table class=\"sdms_table\">");
            String time = new SimpleDateFormat("HH:mm:ss ").format(Calendar.getInstance().getTime());
            stateHTML.append("<tr><th colspan=\"2\" style=\"text-align:center\">" + collectorid + "&nbsp;&nbsp;&nbsp;&nbsp;(Last updated: " + time + ")</th></tr>");
            if (state.optString("paused").equals("Y")) {
                stateHTML.append("<tr><td>Paused</td><td>Paused</td></tr>");
            }
            if (state.optString("disabled").equals("Y")) {
                stateHTML.append("<tr><td>State</td><td>Disabled</td></tr>");
            }
            if (!state.optString("paused").equals("Y") && !state.optString("disabled").equals("Y")) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss");
                    long millis = Long.parseLong(startup.getProperty("startupdate", "0"));
                    stateHTML.append("<tr><td>Status</td><td>Running since " + sdf.format(new Date(millis)) + "</td></tr>");
                }
                catch (Exception sdf) {
                    // empty catch block
                }
            }
            JSONArray runnables = state.getJSONArray("runnables");
            DataSet collectors = this.getQueryProcessor().getPreparedSqlDataSet("SELECT * FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{collectorid}, true);
            collectors.setDateDisplayFormat("lastpingdt", new SimpleDateFormat("dd MMM HH:mm:ss "));
            String lastPing = collectors.getValue(0, "lastpingdt");
            if (lastPing.length() > 0) {
                Long lastpingmillis = collectors.getCalendar(0, "lastpingdt").getTimeInMillis();
                int pingrate = state.optInt("pingrate", 0);
                if (pingrate > 0 && lastpingmillis < System.currentTimeMillis() - (long)(2 * pingrate * 1000) - 100L) {
                    stateHTML.append("<tr><td>Last Ping</td><td>" + lastPing + " <span style=\"color:red\">(Ping is late. Rebooting?)</span></td></tr>");
                } else {
                    stateHTML.append("<tr><td>Last Ping</td><td>" + lastPing + " (Pinging every " + pingrate + "s)</td></tr>");
                }
            }
            stateHTML.append("<tr><td>Active Threads</td><td>" + state.optInt("activecount") + " of " + state.optInt("corepoolsize") + " for " + runnables.length() + " Instruments</td></tr>");
            if (runnables.length() > 0) {
                for (int i = 0; i < runnables.length(); ++i) {
                    JSONObject instrumentState = runnables.getJSONObject(i);
                    String instrumentid = instrumentState.optString("instrumentid");
                    String started = "";
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM HH:mm:ss");
                        long millis = Long.parseLong(instrumentState.optString("starteddt", "0"));
                        if (millis > 0L) {
                            started = " (Running since " + sdf.format(new Date(millis)) + ")";
                        }
                    }
                    catch (Exception sdf) {
                        // empty catch block
                    }
                    stateHTML.append("<tr><td>" + instrumentid + "</td><td style=\"padding:0\">");
                    stateHTML.append("<table style=\"border-width:0\" class=\"sdms_runnables\" cellspacing=\"0\" cellpadding=\"0\">");
                    boolean isShuttingDown = instrumentState.optString("shuttingdown", "N").equals("Y");
                    if (isShuttingDown) {
                        stateHTML.append("<tr><td colspan=\"2\">Shutting down...</td></tr>");
                    } else {
                        String storeDetails;
                        int lastwait = instrumentState.optInt("lastwait");
                        if (instrumentState.optString("continuousoperation").equalsIgnoreCase("Y")) {
                            stateHTML.append("<tr><td colspan=2>Continuous Operation " + started + "</td></tr>");
                        } else {
                            int interval = instrumentState.optInt("collectorinterval");
                            stateHTML.append("<tr><td>Poll Every...</td><td>" + interval + "s" + (lastwait > interval && interval > 0 ? " <span style=\"color:red\">(Last wait: " + lastwait + "s)</span>" : "") + " " + started + "</td></tr>");
                        }
                        String lastCapture = instrumentState.optString("lastcapturedt");
                        if (lastCapture.length() > 0) {
                            stateHTML.append("<tr><td>Last Collected</td><td>" + lastCapture + " (" + instrumentState.optString("lastcapturerel") + ")</td></tr>");
                        }
                        if ((storeDetails = instrumentState.optString("laststoredesc")).length() > 0) {
                            stateHTML.append("<tr><td>Store Details</td><td>" + storeDetails + "</td></tr>");
                        }
                        stateHTML.append("<tr><td>Mean Collection Duration</td><td>" + instrumentState.optString("collector.mean") + "s (SD: " + instrumentState.optString("collector.stdev") + "s)</td></tr>");
                    }
                    stateHTML.append("</table></td>");
                }
            } else {
                stateHTML.append("No runnables found");
            }
            stateHTML.append("</td></tr>");
            stateHTML.append("</table>");
        }
    }

    private String getCollectorIdFromInstrument(String instrumentid) {
        DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("SELECT sdmscollectorid FROM instrument WHERE instrumentid = ?", new Object[]{instrumentid});
        if (ds.size() > 0) {
            return ds.getValue(0, "sdmscollectorid", "");
        }
        return "";
    }

    private void startstopEmulator(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        String instrumentid = ajaxresponse.getRequestParameter("instrumentid");
        if (collectorid.length() == 0 && instrumentid.length() > 0) {
            collectorid = this.getCollectorIdFromInstrument(instrumentid);
        }
        this.sendCollectorCommand(collectorid, instrumentid, "COLLECTORCOMMAND_STARTSTOPEMULATOR");
    }

    private void triggerEmulator(AjaxResponse ajaxresponse, ConnectionInfo info) throws SapphireException {
        String collectorid = ajaxresponse.getRequestParameter("collectorid");
        String instrumentid = ajaxresponse.getRequestParameter("instrumentid");
        if (collectorid.length() == 0 && instrumentid.length() > 0) {
            collectorid = this.getCollectorIdFromInstrument(instrumentid);
        }
        this.sendCollectorCommand(collectorid, instrumentid, "COLLECTORCOMMAND_TRIGGEREMULATOR");
        ajaxresponse.addCallbackArgument("collectorid", collectorid);
    }

    private boolean isCollectorInternal(String collectorid) {
        DataSet collectorDS = this.getQueryProcessor().getPreparedSqlDataSet("SELECT internalflag FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{collectorid});
        return collectorDS.getValue(0, "internalflag").equals("Y");
    }

    private String sendCollectorCommand(String collectorid, String instrumentid, String commandType) throws SapphireException {
        return this.sendCollectorCommand(collectorid, instrumentid, commandType, "");
    }

    private String sendCollectorCommand(String collectorid, String instrumentid, String commandType, String commandParams, File file) throws SapphireException {
        return SDMSUtil.sendCollectorCommand(this.getQueryProcessor(), this.getActionProcessor(), collectorid, instrumentid, commandType, commandParams, file);
    }

    private String sendCollectorCommand(String collectorid, String instrumentid, String commandType, String commandParams) throws SapphireException {
        return SDMSUtil.sendCollectorCommand(this.getQueryProcessor(), this.getActionProcessor(), collectorid, instrumentid, commandType, commandParams);
    }

    private void drawCollectors(AjaxResponse ajaxresponse) {
        QueryProcessor qp = this.getQueryProcessor();
        DataSet collectors = qp.getSqlDataSet("SELECT * FROM sdmscollector order by sdmscollectorid", true);
        DataSet instruments = qp.getSqlDataSet("SELECT * FROM instrument WHERE sdmscollectorid is not null order by sdmscollectorid, instrumentid", true);
        StringBuilder out = new StringBuilder();
        out.append("<table>");
        StringBuilder collectorList = new StringBuilder();
        JSONArray jsonCollectors = new JSONArray();
        JSONArray jsonInstruments = new JSONArray();
        for (int i = 0; i < collectors.size(); ++i) {
            String collectorid = collectors.getValue(i, "sdmscollectorid");
            collectorList.append(collectorid).append(";");
            jsonCollectors.put(collectorid);
            out.append("<tr>");
            out.append("<td collectorid=\"" + collectorid + "\" id=\"collector_" + collectorid + "\" style=\"vertical-align:top\" onclick=\"showCollectorDetailsElement( this )\">" + collectorid + "</td>");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sdmscollectorid", collectorid);
            DataSet perinstrument = instruments.getFilteredDataSet(filter);
            out.append("<td style=\"vertical-align:top\">");
            if (perinstrument.size() == 0) {
                out.append("No instruments found");
            } else {
                for (int j = 0; j < perinstrument.size(); ++j) {
                    String instrumentid = perinstrument.getValue(j, "instrumentid");
                    jsonInstruments.put(instrumentid);
                    out.append("<table><tr><td onclick=\"showInstrumentDetailsElement( this )\" style=\"vertical-align:top\" collectorid=\"" + collectorid + "\" instrumentid=\"" + instrumentid + "\" id=\"instrument_" + instrumentid + "\" >" + instrumentid);
                    out.append("</td></tr></table>");
                }
            }
            out.append("</td>");
            out.append("</tr>");
        }
        out.append("</table>");
        ajaxresponse.addCallbackArgument("html", out.toString());
        ajaxresponse.addCallbackArgument("collectors", jsonCollectors.toString());
        ajaxresponse.addCallbackArgument("instruments", jsonInstruments.toString());
        ajaxresponse.addCallbackArgument("collectorlist", collectorList.toString());
    }

    private void updateCollectorStatuses(AjaxResponse ajaxresponse, String[] collectorIds, boolean returnHTML) throws JSONException {
        if ("Y".equals(ajaxresponse.getRequestParameter("fastping"))) {
            this.updateLastMonitorTimeProperty();
        }
        String where = "";
        if (collectorIds != null) {
            where = " sdmscollectorid IN('" + StringUtil.arrayToString(collectorIds, "','") + "') ";
        }
        QueryProcessor qp = this.getQueryProcessor();
        DataSet collectors = qp.getSqlDataSet("SELECT * FROM sdmscollector " + (where.length() > 0 ? "WHERE" + where : "") + "order by sdmscollectorid", true);
        DataSet instruments = qp.getSqlDataSet("SELECT * FROM instrument WHERE ( activeflag = 'Y' OR activeflag IS NULL ) and sdmscollectorid is not null " + (where.length() > 0 ? "AND" + where : "") + " order by sdmscollectorid", true);
        DataSet allCollectorIncidents = qp.getPreparedSqlDataSet("SELECT ii.sourcekeyid1, i.severity FROM incident i, incidentitem ii WHERE i.incidentid=ii.incidentid AND ii.sourcesdcid=? AND i.incidentstatus in ('Initial')", (Object[])new String[]{"LV_SDMSCollector"}, true);
        DataSet allInstrumentIncidents = qp.getPreparedSqlDataSet("SELECT ii.sourcekeyid1, i.severity FROM incident i, incidentitem ii WHERE i.incidentid=ii.incidentid AND ii.sourcesdcid=? AND i.incidentstatus in ('Initial')", (Object[])new String[]{"Instrument"}, true);
        DataSet allDataCaptureIncidents = qp.getPreparedSqlDataSet("SELECT instrumentid, datacaptureid FROM datacapture WHERE datacaptureid IN ( SELECT ii.sourcekeyid1 FROM incident i, incidentitem ii WHERE i.incidentid=ii.incidentid AND ii.sourcesdcid=? AND i.incidentstatus in ('Initial') ) ", (Object[])new String[]{"LV_DataCapture"}, true);
        collectors.setDateDisplayFormat("lastpingdt", new SimpleDateFormat("HH:mm:ss "));
        JSONObject json = new JSONObject();
        StringBuilder collectorList = new StringBuilder();
        JSONObject collectorArray = new JSONObject();
        for (int i = 0; i < collectors.size(); ++i) {
            String color;
            StringBuilder out = new StringBuilder();
            JSONObject collectorOb = new JSONObject();
            String collectorid = collectors.getValue(i, "sdmscollectorid");
            collectorList.append(collectorid).append(";");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sourcekeyid1", collectorid);
            DataSet collectorIncidents = allCollectorIncidents.getFilteredDataSet(filter);
            boolean internal = collectors.getValue(i, "internalflag").equals("Y");
            boolean direct = collectors.getValue(i, "storagemodeflag").equals("D");
            String hostname = collectors.getValue(i, "actualhostname");
            String lastPing = collectors.getValue(i, "lastpingdt");
            String storagepathlocal = collectors.getValue(i, "storagepathlocal");
            String upgradeRequired = "";
            String tip = "";
            String alertSeverity = "";
            boolean disabled = collectors.getValue(i, "disabledflag").equals("Y");
            boolean paused = collectors.getValue(i, "pausedflag").equals("Y");
            String status = "";
            Calendar lastPingDt = collectors.getCalendar(i, "lastpingdt");
            int notFountDuration = 15000;
            boolean setUpRequired = false;
            if (lastPingDt == null || Calendar.getInstance().getTimeInMillis() - lastPingDt.getTimeInMillis() > (long)notFountDuration) {
                setUpRequired = !internal && collectors.getValue(i, "laststartdt", "").length() == 0;
                status = "(Not Found)";
            } else {
                status = disabled ? "Disabled" : (paused ? "Paused" : "Running");
            }
            PropertyList startupState = new PropertyList();
            try {
                startupState.setPropertyList(collectors.getValue(i, "startupstate", "<propertylist/>"));
            }
            catch (SapphireException sapphireException) {
                // empty catch block
            }
            String reboot = collectors.getValue(i, "rebootrequiredflag", "N");
            if (reboot.equalsIgnoreCase("Y")) {
                upgradeRequired = "Collector Reboot required";
                tip = "Configuration change has been detected. Reboot required.";
                alertSeverity = "Warning";
            } else if (!internal) {
                if (reboot.equalsIgnoreCase("C")) {
                    upgradeRequired = "CUSTOM upgrade required";
                    tip = "Custom jars have been updated";
                    alertSeverity = "Warning";
                } else if (reboot.equalsIgnoreCase("R")) {
                    upgradeRequired = "RUNTIME upgrade required";
                    tip = "New build has been installed";
                    alertSeverity = "Warning";
                } else if (reboot.equalsIgnoreCase("F")) {
                    upgradeRequired = "FULL upgrade required";
                    tip = "A full upgrade of the collector is required.";
                    alertSeverity = "Failure";
                }
            }
            if (upgradeRequired.length() == 0) {
                filter = new HashMap();
                filter.put("sdmscollectorid", collectorid);
                filter.put("rebootrequiredflag", "Y");
                DataSet collectorInstruments = instruments.getFilteredDataSet(filter);
                if (collectorInstruments.size() > 0) {
                    upgradeRequired = "Instrument Reboot required";
                    tip = "Instrument configuration changes detected. Instrument or Collector reboot required.";
                    alertSeverity = "Warning";
                }
            }
            String string = alertSeverity.equals("Failure") ? "#CF7B79" : (color = alertSeverity.equals("Warning") ? "#cdeb8e" : "");
            if (returnHTML) {
                out.append("<table title=\"" + tip + "\" style=\"" + (color.length() > 0 ? "background-color:" + color : "") + "\" id=\"collectortable_" + collectorid + "\" class=\"collector\"><tr>");
                out.append("<td><h1>" + collectorid + "</h1></td>");
                out.append("<td>" + status + "</td>");
            } else {
                collectorArray.put(collectorid, collectorOb);
                collectorOb.put("tip", tip);
                collectorOb.put("status", status);
                collectorOb.put("color", color);
                collectorOb.put("className", "collector");
            }
            int colspan = 2;
            if (collectorIncidents.size() > 0) {
                if (returnHTML) {
                    out.append("<td>");
                    out.append("<img src=\"rc?command=image&image=FlatBlackBell1&color=red\"/>");
                    out.append("</td>");
                    ++colspan;
                } else {
                    collectorOb.put("incidents", collectorIncidents.size());
                    HashMap<String, String> filterInc = new HashMap<String, String>();
                    filterInc.put("severity", "Failure");
                    DataSet filterDS = collectorIncidents.getFilteredDataSet(filterInc);
                    collectorOb.put("incidentsFail", filterDS.size());
                    filterInc = new HashMap();
                    filterInc.put("severity", "Warning");
                    filterDS = collectorIncidents.getFilteredDataSet(filterInc);
                    collectorOb.put("incidentsWarn", filterDS.size());
                }
            } else if (!returnHTML) {
                collectorOb.put("incidents", 0);
                collectorOb.put("incidentsFail", 0);
                collectorOb.put("incidentsWarn", 0);
            }
            if (returnHTML) {
                out.append("</tr><tr>");
                out.append("<td colspan=\"" + colspan + "\"" + (!direct ? "title=\"Path: " + storagepathlocal + "\"" : "") + ">" + (internal ? "Internal" : "External") + " " + (direct ? "Direct" : "Indirect") + "</td>");
                out.append("</tr>");
                if (!setUpRequired) {
                    out.append("<tr><td colspan=\"2\">Last Ping: " + lastPing + "<br>On:" + hostname + "</td></tr>");
                }
                if (upgradeRequired.length() > 0) {
                    out.append("<tr><td colspan=\"2\">" + upgradeRequired + "</td></tr>");
                }
                out.append("</table>");
                json.put("collector_" + collectorid, out.toString());
                continue;
            }
            String collectorCommandid = "";
            try {
                collectorCommandid = this.sendCollectorCommand(collectorid, "", "COLLECTORCOMMAND_GETCOLLECTORSTATE");
            }
            catch (Exception exception) {
                // empty catch block
            }
            collectorOb.put("internal", internal);
            collectorOb.put("setupRequired", setUpRequired ? "Y" : "N");
            collectorOb.put("direct", direct);
            collectorOb.put("storagePathLocal", !direct ? storagepathlocal : "");
            collectorOb.put("lastPing", lastPing);
            collectorOb.put("hostname", hostname);
            collectorOb.put("upgradeRequired", upgradeRequired);
            collectorOb.put("upgradeTip", tip);
            collectorOb.put("setupTip", "Set up required");
            collectorOb.put("collectorcommandid", collectorCommandid);
        }
        AttachmentProcessor attachmentProcessor = null;
        HashMap<String, String> modelImages = new HashMap<String, String>();
        for (int j = 0; j < instruments.size(); ++j) {
            String model;
            JSONObject collectorOb;
            String instrumentid = instruments.getValue(j, "instrumentid");
            String instrumentstatus = instruments.getValue(j, "instrumentstatus");
            String inserviceflag = instruments.getValue(j, "inserviceflag");
            String sdmspausedflag = instruments.getValue(j, "sdmspausedflag");
            String rebootrequiredflag = instruments.getValue(j, "rebootrequiredflag");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sourcekeyid1", instrumentid);
            DataSet instrumentAlerts = allInstrumentIncidents.getFilteredDataSet(filter);
            filter = new HashMap();
            filter.put("instrumentid", instrumentid);
            DataSet datacaptureAlerts = allDataCaptureIncidents.getFilteredDataSet(filter);
            if (returnHTML) {
                StringBuilder out = new StringBuilder();
                out.append("<table id=\"instrumenttable_" + instrumentid + "\" class=\"instrument\"><tr>");
                out.append("<td>" + instrumentid + "</td>");
                if (instrumentAlerts.size() > 0) {
                    out.append("<td style=\"text-align:right\">");
                    out.append("<img src=\"rc?command=image&image=FlatBlackBell1&color=red\"/>");
                    out.append("</td>");
                }
                out.append("</tr></table>");
                json.put("instrument_" + instrumentid, out.toString());
                continue;
            }
            String collector = instruments.getValue(j, "sdmscollectorid");
            if (collector.length() <= 0 || (collectorOb = collectorArray.getJSONObject(collector)) == null) continue;
            if (!collectorOb.has("instruments")) {
                collectorOb.put("instruments", new JSONObject());
            }
            JSONObject instrumentsArray = collectorOb.getJSONObject("instruments");
            JSONObject instrumentOb = new JSONObject();
            instrumentsArray.put(instrumentid, instrumentOb);
            instrumentOb.put("status", instrumentstatus);
            instrumentOb.put("inserviceflag", inserviceflag);
            instrumentOb.put("sdmspausedflag", sdmspausedflag);
            if (rebootrequiredflag.equals("Y")) {
                instrumentOb.put("upgradeRequired", "Instrument Reboot required.");
                instrumentOb.put("upgradeTip", "Configuration change has been detected. Reboot required.");
            }
            if ((model = instruments.getValue(j, "instrumentmodelid", "")).length() > 0) {
                if (modelImages.containsKey(model)) {
                    instrumentOb.put("image", modelImages.get(model));
                } else {
                    DataSet a;
                    if (attachmentProcessor == null) {
                        attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
                    }
                    if ((a = qp.getPreparedSqlDataSet("SELECT attachmentnum, keyid2 FROM sdiattachment WHERE sdcid=? AND keyid1=? AND attachmentclass=?", (Object[])new String[]{"LV_InstrumentModel", model, "Icon"})) != null && a.size() > 0) {
                        ImageRef image = new ImageRef(this.getConnectionProcessor().getSapphireConnection());
                        image.setAttachment("LV_InstrumentModel", model, a.getValue(0, "keyid2", "(null)"), "(null)", a.getInt(0, "attachmentnum", 1));
                        image.setDimensions(32, 32);
                        modelImages.put(model, image.getSrc());
                        instrumentOb.put("image", modelImages.get(model));
                    }
                }
            }
            instrumentOb.put("incidents", instrumentAlerts.size());
            instrumentOb.put("datacaptureincidents", datacaptureAlerts.size());
            HashMap<String, String> filterInc = new HashMap<String, String>();
            filterInc.put("severity", "Failure");
            DataSet filterDS = instrumentAlerts.getFilteredDataSet(filterInc);
            instrumentOb.put("incidentsFail", filterDS.size());
            filterInc = new HashMap();
            filterInc.put("severity", "Warning");
            filterDS = instrumentAlerts.getFilteredDataSet(filterInc);
            instrumentOb.put("incidentsWarn", filterDS.size());
        }
        if (returnHTML) {
            json.put("collectorlist", collectorList.toString());
        }
        ajaxresponse.addCallbackArgument("json", returnHTML ? json.toString() : collectorArray.toString());
    }

    private void updateDataCaptureCount(AjaxResponse ajaxresponse, String propertiesJson) throws JSONException {
        PropertyList pl = new PropertyList(new JSONObject(HttpUtil.decodeURIComponent(propertiesJson)));
        int c = -1;
        if (pl.getPropertyListNotNull("defaultquery").getProperty("querywhere").length() > 0) {
            SDIRequest sdiRequest = new SDIRequest();
            sdiRequest.setSDCid("LV_DataCapture");
            sdiRequest.setQueryWhere(pl.getPropertyListNotNull("defaultquery").getProperty("querywhere"));
            sdiRequest.setQueryFrom(pl.getPropertyListNotNull("defaultquery").getProperty("queryfrom"));
            sdiRequest.setRequestItem("primary");
            SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
            if (sdiData != null && sdiData.getDataset("primary") != null) {
                c = sdiData.getDataset("primary").getRowCount();
            }
        }
        ajaxresponse.addCallbackArgument("count", c);
    }

    private void updateDataCaptureStatuses(AjaxResponse ajaxresponse, String[] dataCaptureIds) throws JSONException {
        String datacaptureid;
        if ("Y".equals(ajaxresponse.getRequestParameter("fastping"))) {
            this.updateLastMonitorTimeProperty();
        }
        String whereDC = "";
        String whereAtt = "";
        if (dataCaptureIds != null) {
            whereDC = " datacaptureid IN('" + StringUtil.arrayToString(dataCaptureIds, "','") + "') ";
            whereAtt = " keyid1 IN('" + StringUtil.arrayToString(dataCaptureIds, "','") + "') ";
        }
        QueryProcessor qp = this.getQueryProcessor();
        DataSet datacaptures = qp.getSqlDataSet("SELECT datacapture.*, (instrument.sdmscollectorid ) sdmscollectorid FROM datacapture, instrument WHERE instrument.instrumentid=datacapture.instrumentid " + (whereDC.length() > 0 ? "AND" + whereDC : "") + "order by datacaptureid", true);
        DataSet attachments = qp.getSqlDataSet("SELECT * FROM sdiattachment WHERE sdcid='LV_DataCapture' " + (whereAtt.length() > 0 ? "AND" + whereAtt : "") + " order by sdiattachmentid", true);
        DataSet allDataCaptureIncidents = qp.getPreparedSqlDataSet("SELECT ii.sourcekeyid1, i.severity FROM incident i, incidentitem ii WHERE i.incidentid=ii.incidentid AND ii.sourcesdcid=? AND i.incidentstatus in ('Initial')", (Object[])new String[]{"LV_DataCapture"}, true);
        JSONObject json = new JSONObject();
        JSONObject datacaptureArray = new JSONObject();
        for (int i = 0; i < datacaptures.size(); ++i) {
            JSONObject dataCaptureOb = new JSONObject();
            datacaptureid = datacaptures.getValue(i, "datacaptureid");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sourcekeyid1", datacaptureid);
            DataSet dataCaptureIncidents = allDataCaptureIncidents.getFilteredDataSet(filter);
            String tip = "";
            String alertSeverity = "";
            String status = datacaptures.getValue(i, "datacapturestatus");
            Calendar lastPingDt = Calendar.getInstance();
            String color = alertSeverity.equals("Failure") ? "#CF7B79" : (alertSeverity.equals("Warning") ? "#cdeb8e" : "");
            datacaptureArray.put(datacaptureid, dataCaptureOb);
            dataCaptureOb.put("tip", tip);
            dataCaptureOb.put("status", status);
            dataCaptureOb.put("color", color);
            dataCaptureOb.put("className", "collector");
            if (dataCaptureIncidents.size() > 0) {
                dataCaptureOb.put("incidents", dataCaptureIncidents.size());
                HashMap<String, String> filterInc = new HashMap<String, String>();
                filterInc.put("severity", "Failure");
                DataSet filterDS = dataCaptureIncidents.getFilteredDataSet(filterInc);
                dataCaptureOb.put("incidentsFail", filterDS.size());
                filterInc = new HashMap();
                filterInc.put("severity", "Warning");
                filterDS = dataCaptureIncidents.getFilteredDataSet(filterInc);
                dataCaptureOb.put("incidentsWarn", filterDS.size());
            }
            dataCaptureOb.put("instrumentid", datacaptures.getValue(i, "instrumentid"));
            dataCaptureOb.put("sdmscollectorid", datacaptures.getValue(i, "sdmscollectorid"));
            dataCaptureOb.put("lastPing", new SimpleDateFormat("HH:mm:ss ").format(lastPingDt.getTime()));
        }
        for (int j = 0; j < attachments.size(); ++j) {
            JSONObject dataCaptureOb;
            String attachmentid = attachments.getValue(j, "sdiattachmentid");
            datacaptureid = attachments.getValue(j, "keyid1");
            if (datacaptureid.length() <= 0 || !datacaptureArray.has(datacaptureid) || (dataCaptureOb = datacaptureArray.getJSONObject(datacaptureid)) == null) continue;
            if (!dataCaptureOb.has("attachments")) {
                dataCaptureOb.put("attachments", new JSONObject());
            }
            JSONObject attachmentsArray = dataCaptureOb.getJSONObject("attachments");
            JSONObject attachmentOb = new JSONObject();
            FileType fileType = FileType.getFileTypeByFileName(attachments.getValue(j, "sourcefilename"), this.getConnectionId());
            attachmentOb.put("image", "rc?command=image&image=" + fileType.getImageRefId() + "&size=32");
            attachmentsArray.put(attachmentid, attachmentOb);
            attachmentOb.put("status", "");
            attachmentOb.put("sourcefilename", attachments.getValue(j, "sourcefilename"));
            attachmentOb.put("attachmentdesc", attachments.getValue(j, "attachmentdesc"));
            attachmentOb.put("incidents", 0);
            attachmentOb.put("incidentsFail", 0);
            attachmentOb.put("incidentsWarn", 0);
        }
        ajaxresponse.addCallbackArgument("json", datacaptureArray.toString());
    }

    private void updateInstrumentStatuses(AjaxResponse ajaxresponse, String[] instrumentIds) throws JSONException {
        String instrumentid;
        if ("Y".equals(ajaxresponse.getRequestParameter("fastping"))) {
            this.updateLastMonitorTimeProperty();
        }
        String whereIn = "";
        if (instrumentIds != null) {
            whereIn = " instrumentid IN('" + StringUtil.arrayToString(instrumentIds, "','") + "') ";
        }
        QueryProcessor qp = this.getQueryProcessor();
        DataSet instruments = qp.getSqlDataSet("SELECT instrument.* FROM instrument " + (whereIn.length() > 0 ? "WHERE " + whereIn : "") + "order by instrumentid", true);
        DataSet datacapturesAll = this.getDataCaptureUsingSDIReqquest(whereIn);
        DataSet allInstrumentIncidents = qp.getPreparedSqlDataSet("SELECT ii.sourcekeyid1, i.severity FROM incident i, incidentitem ii WHERE i.incidentid=ii.incidentid AND ii.sourcesdcid=? AND i.incidentstatus in ('Initial')", (Object[])new String[]{"Instrument"}, true);
        DataSet allDataCaptureIncidents = qp.getPreparedSqlDataSet("SELECT ii.sourcekeyid1, i.severity FROM incident i, incidentitem ii WHERE i.incidentid=ii.incidentid AND ii.sourcesdcid=? AND i.incidentstatus in ('Initial')", (Object[])new String[]{"LV_DataCapture"}, true);
        JSONObject json = new JSONObject();
        AttachmentProcessor attachmentProcessor = null;
        HashMap<String, String> modelImages = new HashMap<String, String>();
        JSONObject instrumentArray = new JSONObject();
        for (int i = 0; i < instruments.size(); ++i) {
            JSONObject instrumentOb = new JSONObject();
            instrumentid = instruments.getValue(i, "instrumentid");
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sourcekeyid1", instrumentid);
            DataSet instrumentIncidents = allInstrumentIncidents.getFilteredDataSet(filter);
            String tip = "";
            String alertSeverity = "";
            String status = instruments.getValue(i, "instrumentstatus");
            String inserviceflag = instruments.getValue(i, "inserviceflag");
            String sdmspausedflag = instruments.getValue(i, "sdmspausedflag");
            String rebootrequiredflag = instruments.getValue(i, "rebootrequiredflag");
            String description = instruments.getValue(i, "instrumentdesc");
            String model = instruments.getValue(i, "instrumentmodelid");
            String type = instruments.getValue(i, "instrumenttype");
            String sdmscollectorid = instruments.getValue(i, "sdmscollectorid");
            Calendar lastPingDt = Calendar.getInstance();
            String color = alertSeverity.equals("Failure") ? "#CF7B79" : (alertSeverity.equals("Warning") ? "#cdeb8e" : "");
            instrumentArray.put(instrumentid, instrumentOb);
            instrumentOb.put("tip", tip);
            instrumentOb.put("status", status);
            instrumentOb.put("inserviceflag", inserviceflag);
            instrumentOb.put("sdmspausedflag", sdmspausedflag);
            if (rebootrequiredflag.equals("Y")) {
                instrumentOb.put("upgradeRequired", "Instrument Reboot required.");
                instrumentOb.put("upgradeTip", "Configuration change has been detected. Reboot required.");
            }
            instrumentOb.put("color", color);
            instrumentOb.put("className", "collector");
            if (model.length() > 0) {
                if (modelImages.containsKey(model)) {
                    instrumentOb.put("image", modelImages.get(model));
                } else {
                    DataSet a;
                    if (attachmentProcessor == null) {
                        attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
                    }
                    if ((a = qp.getPreparedSqlDataSet("SELECT attachmentnum, keyid2 FROM sdiattachment WHERE sdcid=? AND keyid1=? AND attachmentclass=?", (Object[])new String[]{"LV_InstrumentModel", model, "Icon"})) != null && a.size() > 0) {
                        ImageRef image = new ImageRef(this.getConnectionProcessor().getSapphireConnection());
                        image.setAttachment("LV_InstrumentModel", model, a.getValue(0, "keyid2", "(null)"), "(null)", a.getInt(0, "attachmentnum", 1));
                        image.setDimensions(32, 32);
                        modelImages.put(model, image.getSrc());
                        instrumentOb.put("image", modelImages.get(model));
                    }
                }
            }
            if (instrumentIncidents.size() > 0) {
                instrumentOb.put("incidents", instrumentIncidents.size());
                HashMap<String, String> filterInc = new HashMap<String, String>();
                filterInc.put("severity", "Failure");
                DataSet filterDS = instrumentIncidents.getFilteredDataSet(filterInc);
                instrumentOb.put("incidentsFail", filterDS.size());
                filterInc = new HashMap();
                filterInc.put("severity", "Warning");
                filterDS = instrumentIncidents.getFilteredDataSet(filterInc);
                instrumentOb.put("incidentsWarn", filterDS.size());
            }
            instrumentOb.put("description", description);
            instrumentOb.put("model", model);
            instrumentOb.put("type", type);
            instrumentOb.put("sdmscollectorid", sdmscollectorid);
            instrumentOb.put("lastPing", new SimpleDateFormat("HH:mm:ss ").format(lastPingDt.getTime()));
        }
        for (int j = 0; j < datacapturesAll.size(); ++j) {
            JSONObject instrumentOb;
            String datacaptureid = datacapturesAll.getValue(j, "datacaptureid");
            instrumentid = datacapturesAll.getValue(j, "instrumentid");
            if (instrumentid.length() <= 0 || !instrumentArray.has(instrumentid) || (instrumentOb = instrumentArray.getJSONObject(instrumentid)) == null) continue;
            int totalDCIncidents = 0;
            if (instrumentOb.has("dcincidentcount")) {
                totalDCIncidents = instrumentOb.getInt("dcincidentcount");
            }
            int count = 0;
            if (instrumentOb.has("dccount")) {
                count = instrumentOb.getInt("dccount");
            }
            HashMap<String, String> filter = new HashMap<String, String>();
            filter.put("sourcekeyid1", datacaptureid);
            DataSet datacaptureAlerts = allDataCaptureIncidents.getFilteredDataSet(filter);
            if (count < 3) {
                if (!instrumentOb.has("datacaptures")) {
                    instrumentOb.put("datacaptures", new JSONObject());
                }
                JSONObject datacapturesArray = instrumentOb.getJSONObject("datacaptures");
                JSONObject datacaptureOb = new JSONObject();
                datacapturesArray.put(datacaptureid, datacaptureOb);
                datacaptureOb.put("status", datacapturesAll.getValue(j, "datacapturestatus", ""));
                datacaptureOb.put("incidents", datacaptureAlerts.size());
                HashMap<String, String> filterInc = new HashMap<String, String>();
                filterInc.put("severity", "Failure");
                DataSet filterDS = datacaptureAlerts.getFilteredDataSet(filterInc);
                datacaptureOb.put("incidentsFail", filterDS.size());
                filterInc = new HashMap();
                filterInc.put("severity", "Warning");
                filterDS = datacaptureAlerts.getFilteredDataSet(filterInc);
                datacaptureOb.put("incidentsWarn", filterDS.size());
            }
            instrumentOb.put("dccount", ++count);
            instrumentOb.put("dcincidentcount", totalDCIncidents += datacaptureAlerts.size());
        }
        ajaxresponse.addCallbackArgument("json", instrumentArray.toString());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void updateLastMonitorTimeProperty() {
        block5: {
            try {
                long now = System.currentTimeMillis();
                if (now - lastMonitorTime <= 10000L) break block5;
                ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionId());
                Class<?> clazz = this.getClass();
                synchronized (clazz) {
                    configurationProcessor.setSysConfigProperty("lastsdmsmonitortime", "" + now);
                    lastMonitorTime = now;
                }
            }
            catch (SapphireException e) {
                this.logger.error("Failed to set the sysconfig last montitor time property");
            }
        }
    }

    private DataSet getDataCaptureUsingSDIReqquest(String whereClause) {
        DataSet ds = new DataSet();
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_DataCapture");
        sdiRequest.setQueryFrom("datacapture");
        sdiRequest.setQueryWhere(whereClause);
        sdiRequest.setRequestItem("primary");
        sdiRequest.setQueryOrderBy("createdt desc");
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        if (sdiData != null && sdiData.getDataset("primary") != null) {
            ds = sdiData.getDataset("primary");
        }
        return ds;
    }
}

