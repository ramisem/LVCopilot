/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.logfileviewer;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerUtil;
import com.labvantage.sapphire.admin.system.automation.Server;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.AutomationService;
import com.labvantage.sapphire.services.SapphireConnection;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.servlet.AjaxResponse;
import sapphire.servlet.BaseAjaxRequest;
import sapphire.util.ConnectionInfo;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LogViewerAjaxHandler
extends BaseAjaxRequest {
    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException {
        AjaxResponse ajaxresponse;
        block49: {
            ajaxresponse = new AjaxResponse(request, response);
            ConnectionProcessor cp = this.getConnectionProcessor();
            ConnectionInfo info = cp.getConnectionInfo(cp.getConnectionid());
            try {
                String dothis = ajaxresponse.getRequestParameter("dothis");
                String rolelist = ";" + info.getRoleList() + ";";
                if (!(dothis.equals("GENERATESNAPSHOT") || dothis.equals("GENERATESNAPSHOTTEMPFOLDER") || dothis.equals("GETPROGRESS") || dothis.equals("CANCELPROGRESS") || rolelist.contains(";Administrator;") || rolelist.contains(";IssueSubmitter;"))) {
                    throw new SapphireException("Unable to execute request. You do not have sufficient privileges");
                }
                if (dothis.equals("GETPROGRESS")) {
                    String folder = ajaxresponse.getRequestParameter("folder");
                    try {
                        Path progresFile = Paths.get(LogViewerUtil.getProgressFilename(folder), new String[0]);
                        if (!progresFile.toFile().exists()) {
                            ajaxresponse.addCallbackArgument("message", "END");
                            break block49;
                        }
                        List<String> lines = Files.readAllLines(progresFile, Charset.forName("UTF-8"));
                        if (lines.size() > 0) {
                            String s = lines.get(0);
                            ajaxresponse.addCallbackArgument("message", s);
                            break block49;
                        }
                        ajaxresponse.addCallbackArgument("message", "?");
                    }
                    catch (Exception e) {
                        ajaxresponse.addCallbackArgument("message", "?");
                    }
                    break block49;
                }
                if (dothis.equals("CANCELPROGRESS")) {
                    String folder = ajaxresponse.getRequestParameter("folder");
                    Path cancelFilename = Paths.get(LogViewerUtil.getCancelFilename(folder), new String[0]);
                    Files.write(cancelFilename, "cancel".getBytes(), new OpenOption[0]);
                    break block49;
                }
                if (dothis.equals("STARTANALYSIS")) {
                    String snapshotFilename = ajaxresponse.getRequestParameter("snapshotfilename");
                    LogViewerUtil.setStartAnalysis(true);
                    HashMap<String, Set<String>> sets = LogViewerUtil.preprocessSnapshot(this.getConnectionId(), snapshotFilename);
                    if (sets != null) {
                        JSONObject o = new JSONObject();
                        for (String key : sets.keySet()) {
                            Set<String> set = sets.get(key);
                            o.put(key, LogViewerAjaxHandler.joinSet(set, ";"));
                        }
                        ajaxresponse.addCallbackArgument("sets", o.toString());
                    } else {
                        ajaxresponse.addCallbackArgument("sets", "");
                    }
                    break block49;
                }
                if (dothis.equals("STRIPTHREADS")) {
                    String snapshotFilename = ajaxresponse.getRequestParameter("snapshotfilename");
                    String filename = LogViewerUtil.stripThreads(this.getConnectionId(), snapshotFilename);
                    if (filename != null) {
                        ajaxresponse.addCallbackArgument("filename", filename);
                    } else {
                        ajaxresponse.addCallbackArgument("filename", "");
                    }
                    break block49;
                }
                if (dothis.equals("VIEWREQUEST")) {
                    String snapshotFilename = ajaxresponse.getRequestParameter("snapshotfilename");
                    String index = ajaxresponse.getRequestParameter("index");
                    HashMap<String, String> displayOptions = new HashMap<String, String>();
                    displayOptions.put("showraw", ajaxresponse.getRequestParameter("showraw"));
                    displayOptions.put("hidedebug", ajaxresponse.getRequestParameter("hidedebug"));
                    displayOptions.put("showlevel", ajaxresponse.getRequestParameter("showlevel"));
                    displayOptions.put("showcontext", ajaxresponse.getRequestParameter("showcontext"));
                    displayOptions.put("indenting", ajaxresponse.getRequestParameter("indenting"));
                    displayOptions.put("highlightstepvalue", ajaxresponse.getRequestParameter("highlightstepvalue", "0"));
                    String[] text = new String[]{ajaxresponse.getRequestParameter("texta"), ajaxresponse.getRequestParameter("textb"), ajaxresponse.getRequestParameter("textc")};
                    String textCaseSensitive = ajaxresponse.getRequestParameter("textcasesensitive");
                    String html = LogViewerUtil.getRequestView(this.getConnectionid(), snapshotFilename, index, displayOptions, text, "Y".equals(textCaseSensitive));
                    if (html != null) {
                        ajaxresponse.addCallbackArgument("html", html);
                    } else {
                        ajaxresponse.addCallbackArgument("html", "");
                    }
                    break block49;
                }
                if (dothis.equals("DOWNLOADREQUESTS")) {
                    String snapshotFilename = ajaxresponse.getRequestParameter("snapshotfilename");
                    String requestIndexList = ajaxresponse.getRequestParameter("indexlist");
                    String[] requestedIndexes = StringUtil.split(requestIndexList, ";");
                    HashSet<String> requestIndexSet = new HashSet<String>();
                    for (String index : requestedIndexes) {
                        if (index.length() <= 0) continue;
                        requestIndexSet.add(index);
                    }
                    String filename = LogViewerUtil.prepareRequestDownload(this.getConnectionid(), snapshotFilename, requestIndexSet);
                    if (filename != null) {
                        ajaxresponse.addCallbackArgument("filename", filename);
                    } else {
                        ajaxresponse.addCallbackArgument("filename", "");
                    }
                    break block49;
                }
                if (dothis.equals("EXECUTESEARCH")) {
                    String snapshotFilename = ajaxresponse.getRequestParameter("snapshotfilename");
                    String requesttype = ajaxresponse.getRequestParameter("requesttype");
                    String requestid = ajaxresponse.getRequestParameter("id");
                    String userid = ajaxresponse.getRequestParameter("userid");
                    String dbid = ajaxresponse.getRequestParameter("dbName");
                    String searchconnectionid = ajaxresponse.getRequestParameter("connectionid");
                    String threadid = ajaxresponse.getRequestParameter("threadid");
                    String[] text = new String[]{ajaxresponse.getRequestParameter("texta"), ajaxresponse.getRequestParameter("textb"), ajaxresponse.getRequestParameter("textc")};
                    String textCaseSensitive = ajaxresponse.getRequestParameter("textcasesensitive");
                    String hasException = ajaxresponse.getRequestParameter("hasexception");
                    String exceptiontype = ajaxresponse.getRequestParameter("exceptiontype");
                    String duration = ajaxresponse.getRequestParameter("duration");
                    String stepduration = ajaxresponse.getRequestParameter("stepduration");
                    String fromdate = ajaxresponse.getRequestParameter("fromdate");
                    String todate = ajaxresponse.getRequestParameter("todate");
                    int maxhits = Integer.parseInt(ajaxresponse.getRequestParameter("maxhits", "5000"));
                    List<String> found = LogViewerUtil.executeSearch(this.getConnectionId(), snapshotFilename, requesttype, requestid, userid, searchconnectionid, threadid, text, textCaseSensitive, hasException, exceptiontype, duration, stepduration, fromdate, todate, dbid);
                    JSONArray jsonLines = new JSONArray();
                    for (int i = 0; i < found.size() && i < maxhits; ++i) {
                        jsonLines.put(found.get(i));
                    }
                    String countLabel = "";
                    countLabel = jsonLines.length() == 0 ? "No requests found matching your criteria." : (jsonLines.length() == found.size() ? "Found " + found.size() + " requests matching your criteria." : "Showing the first " + jsonLines.length() + " of " + found.size() + " requests found.");
                    ajaxresponse.addCallbackArgument("lines", jsonLines.toString());
                    ajaxresponse.addCallbackArgument("countlabel", countLabel);
                    break block49;
                }
                if (dothis.equals("GETSTACKTRACES")) {
                    String snapshotFilename = ajaxresponse.getRequestParameter("snapshotfilename");
                    String exceptionType = ajaxresponse.getRequestParameter("exceptiontype");
                    int maxhits = Integer.parseInt(ajaxresponse.getRequestParameter("maxhits", "500"));
                    List<String> found = LogViewerUtil.getStackTraces(this.getConnectionId(), snapshotFilename, exceptionType);
                    JSONArray jsonLines = new JSONArray();
                    for (int i = 0; i < found.size() && i < maxhits; ++i) {
                        jsonLines.put(found.get(i));
                    }
                    String countLabel = "";
                    countLabel = jsonLines.length() == 0 ? "No stacktraces found." : (jsonLines.length() == found.size() ? "Found " + found.size() + " stacktraces." : "Showing the first " + jsonLines.length() + " of " + found.size() + " stacktraces found.");
                    ajaxresponse.addCallbackArgument("lines", jsonLines.toString());
                    ajaxresponse.addCallbackArgument("countlabel", countLabel);
                    break block49;
                }
                if (dothis.equals("EXTRACTSNAPSHOT")) {
                    String snapshotFilename = ajaxresponse.getRequestParameter("snapshotfilename");
                    boolean cancel = LogViewerUtil.extractSnapshot(this.getConnectionid(), snapshotFilename);
                    ajaxresponse.addCallbackArgument("message", cancel ? "cancel" : "finished");
                    break block49;
                }
                if (dothis.equals("DELETESNAPSHOT")) {
                    String requestedFilesList = ajaxresponse.getRequestParameter("files");
                    String[] requestedFiles = StringUtil.split(requestedFilesList, "|");
                    try {
                        String snapshotFolder = LogViewerUtil.getSnapshotFolder(info.getConnectionId());
                        for (String requestedFile : requestedFiles) {
                            String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(this.getConnectionid(), requestedFile);
                            LogViewerUtil.deleteDirectory(snapshotFolderName);
                            File file = new File(snapshotFolder + "/" + requestedFile);
                            Path path = file.toPath();
                            Files.delete(path);
                        }
                        ajaxresponse.addCallbackArgument("message", "ok");
                    }
                    catch (Exception e) {
                        ajaxresponse.setError("Unable to delete snapshots: " + e.getMessage());
                    }
                    break block49;
                }
                if (dothis.equals("GENERATESNAPSHOTTEMPFOLDER")) {
                    String workingFolder = LogViewerUtil.getLogFolder() + "/working";
                    File temp = new File(workingFolder);
                    if (!temp.exists()) {
                        temp.mkdir();
                    }
                    Path generateFolder = Files.createTempDirectory(Paths.get(workingFolder, new String[0]), "generatesnapshot", new FileAttribute[0]);
                    ajaxresponse.addCallbackArgument("tempfolder", generateFolder.toFile().getAbsolutePath());
                    ajaxresponse.addCallbackArgument("description", ajaxresponse.getRequestParameter("description"));
                    ajaxresponse.addCallbackArgument("timemode", ajaxresponse.getRequestParameter("timemode"));
                    ajaxresponse.addCallbackArgument("fromdate", ajaxresponse.getRequestParameter("fromdate"));
                    ajaxresponse.addCallbackArgument("todate", ajaxresponse.getRequestParameter("todate"));
                    ajaxresponse.addCallbackArgument("cluster", ajaxresponse.getRequestParameter("cluster"));
                    ajaxresponse.addCallbackArgument("db", ajaxresponse.getRequestParameter("db"));
                    ajaxresponse.addCallbackArgument("source", ajaxresponse.getRequestParameter("source"));
                    ajaxresponse.addCallbackArgument("logType", ajaxresponse.getRequestParameter("logType"));
                    break block49;
                }
                if (dothis.equals("GENERATESNAPSHOT")) {
                    SequenceProcessor sp = this.getSequenceProcessor();
                    int sequence = sp.getSequence("LogFileViewer", "snapshot", 0, 1);
                    String snapshotid = "LVS" + new DecimalFormat("000000").format(sequence);
                    String tempFolderName = ajaxresponse.getRequestParameter("tempfolder");
                    String description = ajaxresponse.getRequestParameter("description");
                    String timemode = ajaxresponse.getRequestParameter("timemode");
                    String fromdate = ajaxresponse.getRequestParameter("fromdate");
                    String todate = ajaxresponse.getRequestParameter("todate");
                    String cluster = ajaxresponse.getRequestParameter("cluster");
                    String dbid = ajaxresponse.getRequestParameter("db");
                    String source = ajaxresponse.getRequestParameter("source");
                    String logType = ajaxresponse.getRequestParameter("logType");
                    String hostid = Configuration.getInstance().getHostid();
                    SapphireConnection sapphireConnection = this.getConnectionProcessor().getSapphireConnection();
                    String sysuserid = sapphireConnection.getSysuserId();
                    String databaseid = sapphireConnection.getDatabaseId();
                    List<Server> otherServers = AutomationService.getOtherServerList(info.getDatabaseId());
                    boolean snapshotGenerated = false;
                    try {
                        if (LogViewerUtil.getSnapshotFolder(this.getConnectionid()).length() == 0) {
                            throw new SapphireException("Snapshot folder un-defined. Please set the value in System Configuration page.");
                        }
                        if (cluster == null || cluster.length() == 0 || cluster.equalsIgnoreCase("this")) {
                            snapshotGenerated = LogViewerUtil.generateSnapshot(this.getConnectionid(), tempFolderName, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, dbid, source, logType);
                        } else {
                            PropertyList props = new PropertyList();
                            props.setProperty("snapshotid", snapshotid);
                            props.setProperty("description", description);
                            props.setProperty("timemode", timemode);
                            props.setProperty("fromdate", fromdate);
                            props.setProperty("todate", todate);
                            props.setProperty("cluster", cluster);
                            props.setProperty("sysuserid", sysuserid);
                            props.setProperty("db", dbid);
                            props.setProperty("logType", logType);
                            props.setProperty("source", source);
                            if (cluster.equals("all")) {
                                AutomationService.broadcastServerCommand(databaseid, "GenerateSnapshot", props.toXMLString());
                                snapshotGenerated = LogViewerUtil.generateSnapshot(this.getConnectionid(), tempFolderName, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, dbid, source, logType);
                            } else {
                                AutomationService.sendServerCommand(databaseid, cluster, "GenerateSnapshot", props.toXMLString());
                            }
                        }
                        if (!snapshotGenerated && otherServers.size() == 0) {
                            ajaxresponse.addCallbackArgument("message", "Info: No Log File found to generate snapshot");
                            break block49;
                        }
                        ajaxresponse.addCallbackArgument("id", snapshotid);
                    }
                    catch (Exception e) {
                        ajaxresponse.setError("Failed to generate snapshot: " + e.getMessage());
                    }
                    break block49;
                }
                if (dothis.equals("QUICKSEARCH")) {
                    String requestedFilesList = ajaxresponse.getRequestParameter("files");
                    String[] requestedFiles = null;
                    if (requestedFilesList != null && requestedFilesList != "") {
                        requestedFiles = StringUtil.split(requestedFilesList, "|");
                        String searchText = ajaxresponse.getRequestParameter("searchText");
                        LogViewerUtil.quickSearch(this.getConnectionid(), requestedFiles, searchText);
                        ajaxresponse.addCallbackArgument("message", "ok");
                    }
                } else if (dothis.equals("REFRESH")) {
                    LogViewerUtil.clearSelectedZip();
                    ajaxresponse.addCallbackArgument("message", "ok");
                } else if (dothis.equals("SORT")) {
                    String sortedColumn = ajaxresponse.getRequestParameter("sortedColumn");
                    String sortedOrder = ajaxresponse.getRequestParameter("sortedOrder");
                    ajaxresponse.addCallbackArgument("sortedDataset", LogViewerUtil.getSnapshots(this.getConnectionid(), sortedColumn, sortedOrder));
                }
            }
            catch (Exception e) {
                Trace.logError("Unable to execute LogViewer command", e);
                ajaxresponse.setError(e.getMessage());
            }
        }
        ajaxresponse.print();
    }

    private static String joinSet(Set<String> set, String delimeter) {
        if (set.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String s : set) {
            sb.append(delimeter).append(s);
        }
        return sb.toString();
    }
}

