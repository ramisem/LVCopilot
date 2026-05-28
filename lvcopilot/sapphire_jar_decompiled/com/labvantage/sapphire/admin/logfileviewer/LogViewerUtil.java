/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FileDeleteStrategy
 */
package com.labvantage.sapphire.admin.logfileviewer;

import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.DateTimeUtil;
import com.labvantage.sapphire.admin.logfileviewer.LogFileProcessor;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerConnection;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerException;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerFile;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerParser;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerParser_Default;
import com.labvantage.sapphire.admin.logfileviewer.LogViewerRequest;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.SecurityService;
import com.labvantage.sapphire.util.file.ZipFileUtil;
import com.labvantage.sapphire.util.http.HttpUtil;
import com.labvantage.sapphire.util.logger.LogConfig;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileDeleteStrategy;
import sapphire.SapphireException;
import sapphire.util.DataSet;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class LogViewerUtil {
    public static final String indentString = "                                                                                                                              ";
    public static final List<String> zipName = new ArrayList<String>();
    public static final List<String> quickSearchList = new ArrayList<String>();
    public static Boolean startAnalysis = new Boolean(false);

    public static String getSnapshotFolder(String connectionid) throws SapphireException {
        ConfigurationProcessor configuration = new ConfigurationProcessor(connectionid);
        return configuration.getConfigProperty("com.labvantage.sapphire.server.snapshotfolder");
    }

    public static HashMap<String, Set<String>> preprocessSnapshot(String connectionid, String snapshotFilename) throws SapphireException, IOException {
        final ArrayList requests = new ArrayList();
        final ArrayList exceptions = new ArrayList();
        final ArrayList connections = new ArrayList();
        final TreeSet threads = new TreeSet();
        final ArrayList files = new ArrayList();
        final HashMap threadRequest = new HashMap();
        String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(connectionid, snapshotFilename);
        final String progressFilename = LogViewerUtil.getProgressFilename(snapshotFolderName);
        final File cancelFile = new File(LogViewerUtil.getCancelFilename(snapshotFolderName));
        LogViewerUtil.updateProgress(progressFilename, "Indexing: 0%");
        final int[] totalRowNum = new int[]{0};
        final boolean[] cancel = new boolean[]{false};
        final int[] requestIndex = new int[]{0};
        final int[] exceptionIndex = new int[]{0};
        final String[] lastContentLine = new String[]{""};
        final LogViewerException[] insideException = new LogViewerException[]{null};
        LogFileProcessor.processFiles(connectionid, snapshotFilename, new LogFileProcessor(){

            @Override
            public void processFile(LogViewerFile logViewerFile, BufferedReader in) throws IOException {
                if (cancelFile.exists()) {
                    cancel[0] = true;
                    cancelFile.delete();
                }
                if (!cancel[0]) {
                    String line;
                    LogViewerUtil.updateProgress(progressFilename, "Indexing: " + 100 * logViewerFile.filenumber / logViewerFile.filecount + "%");
                    files.add(logViewerFile);
                    logViewerFile.startTotalRow = totalRowNum[0] + 1;
                    int lineNum = 0;
                    LogViewerParser parser = LogViewerUtil.getParser();
                    int nonSapphireLineCount = 0;
                    while ((line = in.readLine()) != null) {
                        ++lineNum;
                        if (parser.isContentLine(line)) {
                            String connectionid;
                            int pos2;
                            int pos;
                            String thread;
                            ArrayList requestStack;
                            String content = parser.getLineContent(line);
                            lastContentLine[0] = line;
                            LogViewerRequest request = null;
                            if (parser.isContentRequestStart(content)) {
                                int pos3 = content.indexOf("command=");
                                int pos22 = content.indexOf("&", pos3);
                                if (pos22 == -1) {
                                    pos22 = content.indexOf(" ", pos3);
                                }
                                if (pos3 >= 0 && pos22 >= 0) {
                                    request = new LogViewerRequest();
                                    request.type = content.substring(pos3 + 8, pos22);
                                    request.url = content;
                                    int pos32 = content.indexOf("=", pos22);
                                    int pos4a = content.indexOf(" ", pos32);
                                    int pos4b = content.indexOf("&", pos32);
                                    if (pos4a >= 0 || pos4b >= 0) {
                                        int pos4 = Math.min(pos4a == -1 ? 1000000000 : pos4a, pos4b == -1 ? 100000000 : pos4b);
                                        if (pos32 >= 0 && pos4 >= 0) {
                                            request.targetid = content.substring(pos32 + 1, pos4);
                                        }
                                    }
                                }
                            } else if (parser.isContentActionStart(content)) {
                                request = new LogViewerRequest();
                                request.type = "action";
                                int pos4 = line.indexOf("Processing Action ") + 18;
                                request.targetid = line.substring(pos4, line.indexOf(" ", pos4));
                            } else if (parser.isContentStartupStart(content)) {
                                request = new LogViewerRequest();
                                request.type = "startup";
                                request.targetid = null;
                            } else if (parser.isContentRequestEnd(content)) {
                                String thread2 = parser.getLineThread(line);
                                ArrayList requestStack2 = (ArrayList)threadRequest.get(thread2);
                                if (requestStack2 != null && requestStack2.size() > 0) {
                                    LogViewerUtil.endRequest(parser, logViewerFile, line, lineNum, requestStack2);
                                }
                            } else if (parser.isContentActionEnd(content)) {
                                String thread3 = parser.getLineThread(line);
                                ArrayList requestStack3 = (ArrayList)threadRequest.get(thread3);
                                if (requestStack3 != null && requestStack3.size() > 0) {
                                    LogViewerUtil.endRequest(parser, logViewerFile, line, lineNum, requestStack3);
                                }
                            } else if (parser.isContentStartupEnd(content) && (requestStack = (ArrayList)threadRequest.get(thread = parser.getLineThread(line))) != null && requestStack.size() > 0) {
                                LogViewerUtil.endRequest(parser, logViewerFile, line, lineNum, requestStack);
                            }
                            if (request != null) {
                                requestIndex[0] = requestIndex[0] + 1;
                                request.index = request.index;
                                request.startFilename = logViewerFile.filename;
                                request.startRow = lineNum;
                                request.startTotalRow = logViewerFile.startTotalRow + lineNum;
                                request.startDate = parser.getLineDateString(line);
                                request.startDateDate = parser.getLineDate(line);
                                request.connectionid = LogViewerUtil.getRequestConnectionid(content);
                                request.userid = LogViewerUtil.getUserFromConnection(request.connectionid);
                                request.thread = parser.getLineThread(line);
                                threads.add(request.thread);
                                requests.add(request);
                                ArrayList<LogViewerRequest> requestStack4 = (ArrayList<LogViewerRequest>)threadRequest.get(request.thread);
                                if (requestStack4 == null) {
                                    requestStack4 = new ArrayList<LogViewerRequest>();
                                    threadRequest.put(request.thread, requestStack4);
                                }
                                request.indent = requestStack4.size();
                                requestStack4.add(request);
                            }
                            if (content.startsWith("++")) {
                                String connectionid2;
                                int pos23;
                                int pos5 = content.indexOf("for [");
                                if (pos5 > 0 && (pos23 = content.indexOf("]", pos5)) > 0 && ((connectionid2 = content.substring(pos5 + 5, pos23)).contains("|") || connectionid2.startsWith(SecurityService.ENCRYPTEDCONNECTIONID_MARKER))) {
                                    LogViewerUtil.addConnection(connectionid2, connections);
                                }
                            } else if (content.startsWith("New Connection: ") && (pos = content.indexOf("new connectionid [")) > 0 && (pos2 = content.indexOf("]", pos)) > 0 && ((connectionid = content.substring(pos + 18, pos2)).contains("|") || connectionid.startsWith(SecurityService.ENCRYPTEDCONNECTIONID_MARKER))) {
                                LogViewerUtil.addConnection(connectionid, connections);
                            }
                            insideException[0] = null;
                            continue;
                        }
                        if (insideException[0] != null) {
                            String temp;
                            if (insideException[0].firstAt == null && line.trim().startsWith("at ")) {
                                insideException[0].firstAt = line;
                            }
                            if ((temp = line).contains("sapphire.")) {
                                temp = "<span style=\"color:red\">" + temp + "</span>";
                                nonSapphireLineCount = 0;
                            } else {
                                ++nonSapphireLineCount;
                            }
                            if (nonSapphireLineCount == 3) {
                                insideException[0].content.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;...");
                                continue;
                            }
                            if (nonSapphireLineCount >= 3) continue;
                            insideException[0].content.append("<br>&nbsp;&nbsp;&nbsp;&nbsp;" + temp);
                            continue;
                        }
                        if (!line.contains("Exception:") || exceptions.size() >= 10000) continue;
                        LogViewerException exception = new LogViewerException();
                        exceptionIndex[0] = exceptionIndex[0] + 1;
                        exception.index = exception.index;
                        exception.filename = logViewerFile.filename;
                        exception.startRow = lineNum;
                        exception.startTotalRow = logViewerFile.startTotalRow + lineNum;
                        exception.exception = line.trim().substring(0, line.trim().indexOf(":"));
                        exception.startDate = parser.getLineDateString(lastContentLine[0]);
                        exception.content.append(line.trim());
                        exceptions.add(exception);
                        insideException[0] = exception;
                        exception.thread = parser.getLineThread(lastContentLine[0]);
                        ArrayList requestStack = (ArrayList)threadRequest.get(exception.thread);
                        if (requestStack == null) continue;
                        for (LogViewerRequest r : requestStack) {
                            r.hasException = true;
                            if (!r.exceptionType.contains(";?ex" + exception.exception)) {
                                r.exceptionType = r.exceptionType + ";?ex" + exception.exception;
                            }
                            exception.requestIndex = r.index;
                        }
                    }
                    totalRowNum[0] = totalRowNum[0] + lineNum;
                    logViewerFile.endTotalRow = totalRowNum[0];
                }
            }
        });
        HashMap<String, AbstractSet> sets = null;
        if (!cancel[0]) {
            PropertyList manifest = LogViewerUtil.getManifestPropertyList(snapshotFolderName);
            sets = new HashMap<String, AbstractSet>();
            TreeSet<String> requestTypes = new TreeSet<String>();
            TreeSet<String> exceptionTypes = new TreeSet<String>();
            TreeSet<String> users = new TreeSet<String>();
            TreeSet<String> connectionids = new TreeSet<String>();
            StringBuilder fileOutString = new StringBuilder();
            Date minDateDate = null;
            Date maxDateDate = null;
            for (LogViewerRequest request : requests) {
                String[] types;
                if (request.startDateDate == null || request.endDateDate == null) continue;
                if (minDateDate == null || request.startDateDate.before(minDateDate)) {
                    minDateDate = request.startDateDate;
                }
                if (maxDateDate == null || request.endDateDate.after(maxDateDate)) {
                    maxDateDate = request.endDateDate;
                }
                fileOutString.append("#|#index=").append(request.index);
                fileOutString.append("#|#type=").append(request.type);
                fileOutString.append("#|#targetid=").append(request.targetid);
                fileOutString.append("#|#startfile=").append(request.startFilename);
                fileOutString.append("#|#startrow=").append(request.startRow);
                fileOutString.append("#|#userid=").append(request.userid);
                fileOutString.append("#|#dbid=").append(LogViewerUtil.fetchDBid(request.connectionid));
                fileOutString.append("#|#connectionid=").append(request.connectionid);
                fileOutString.append("#|#hasexception=").append(request.hasException ? "Y" : "N");
                fileOutString.append("#|#exceptiontype=").append(request.exceptionType);
                fileOutString.append("#|#indent=").append(request.indent);
                fileOutString.append("#|#startdate=").append(request.startDate);
                fileOutString.append("#|#startdatemillis=").append(request.startDateDate.getTime());
                fileOutString.append("#|#enddatemillis=").append(request.endDateDate.getTime());
                fileOutString.append("#|#took=").append(request.took);
                fileOutString.append("#|#endfile=").append(request.endFilename);
                fileOutString.append("#|#endrow=").append(request.endRow);
                fileOutString.append("#|#threadid=").append(request.thread);
                fileOutString.append("#|#starttotalrow=").append(request.startTotalRow);
                fileOutString.append("#|#endtotalrow=").append(request.endTotalRow);
                fileOutString.append("#|#");
                fileOutString.append("\n");
                requestTypes.add(request.type);
                if (request.targetid != null) {
                    TreeSet<String> targetidSet = (TreeSet<String>)sets.get(request.type);
                    if (targetidSet == null) {
                        targetidSet = new TreeSet<String>();
                        sets.put(request.type, targetidSet);
                    }
                    targetidSet.add(request.targetid);
                }
                for (String type : types = StringUtil.split(request.exceptionType, ";?ex")) {
                    exceptionTypes.add(type);
                }
                connectionids.add(request.connectionid);
                users.add(request.userid);
            }
            Files.write(Paths.get(snapshotFolderName + "/requests.txt", new String[0]), fileOutString.toString().getBytes(), new OpenOption[0]);
            fileOutString.setLength(0);
            for (LogViewerException exception : exceptions) {
                fileOutString.append("#|#" + exception.exception + "#|#");
                fileOutString.append(exception.index + "#|#");
                fileOutString.append(exception.filename + "#|#");
                fileOutString.append(exception.startRow + "#|#");
                fileOutString.append(exception.startTotalRow + "#|#");
                fileOutString.append(exception.thread + "#|#");
                fileOutString.append(exception.startDate + "#|#");
                fileOutString.append(exception.requestIndex + "#|#");
                fileOutString.append(exception.firstAt + "#|#");
                fileOutString.append(StringUtil.replaceAll(exception.content.toString(), "#|#", "") + "#|#");
                fileOutString.append("\n");
            }
            Files.write(Paths.get(snapshotFolderName + "/exceptions.txt", new String[0]), fileOutString.toString().getBytes(), new OpenOption[0]);
            sets.put("requesttypes", requestTypes);
            sets.put("exceptiontypes", exceptionTypes);
            sets.put("userids", users);
            sets.put("connectionids", connectionids);
            sets.put("threads", threads);
            HashSet<String> minDateSet = new HashSet<String>();
            minDateSet.add("" + (minDateDate != null ? Long.valueOf(minDateDate.getTime()) : ""));
            sets.put("mindate", minDateSet);
            HashSet<String> maxDateSet = new HashSet<String>();
            maxDateSet.add("" + (maxDateDate != null ? Long.valueOf(maxDateDate.getTime()) : ""));
            sets.put("maxdate", maxDateSet);
            try {
                String shapshotbyconnectionid;
                String snapshotby;
                HashSet<String> tempSet;
                String logToDate;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String logFromDate = manifest.getProperty("logfromdate");
                if (logFromDate.length() > 0) {
                    Date logfrom = sdf.parse(logFromDate);
                    HashSet<String> tempSet2 = new HashSet<String>();
                    tempSet2.add("" + logfrom.getTime());
                    sets.put("logfromdate", tempSet2);
                }
                if ((logToDate = manifest.getProperty("logtodate")).length() >= 0) {
                    Date logto = sdf.parse(logToDate);
                    tempSet = new HashSet<String>();
                    tempSet.add("" + (logto.getTime() + 1000L));
                    sets.put("logtodate", tempSet);
                }
                if ((snapshotby = manifest.getProperty("shapshotby")).length() >= 0) {
                    tempSet = new HashSet();
                    tempSet.add(snapshotby);
                    sets.put("shapshotby", tempSet);
                }
                if ((shapshotbyconnectionid = manifest.getProperty("shapshotbyconnectionid")).length() >= 0) {
                    HashSet<String> tempSet3 = new HashSet<String>();
                    tempSet3.add(shapshotbyconnectionid);
                    sets.put("shapshotbyconnectionid", tempSet3);
                }
                sets.put("dbids", LogViewerUtil.getDBList(connectionids));
                LogViewerUtil.deleteProgressFile(progressFilename);
            }
            catch (Exception sdf) {
                // empty catch block
            }
            PropertyListCollection filesCollection = manifest.getCollection("filelist");
            for (LogViewerFile file : files) {
                PropertyList filepl = filesCollection.find("filename", file.filename);
                filepl.setProperty("starttotalrow", "" + file.startTotalRow);
                filepl.setProperty("endtotalrow", "" + file.endTotalRow);
            }
            Files.write(Paths.get(snapshotFolderName + "/snapshot.mf", new String[0]), manifest.toXMLString().getBytes(), new OpenOption[0]);
        }
        return sets;
    }

    private static HashSet getDBList(TreeSet<String> connectionids) {
        HashSet<String> tempSet = new HashSet<String>();
        Iterator<String> connectionid = connectionids.iterator();
        while (connectionid.hasNext()) {
            String val = SecurityService.decryptConnectionId(connectionid.next().toString());
            if (val == null || val.isEmpty() || !val.contains("|")) continue;
            tempSet.add(val.split("\\|")[0]);
        }
        return tempSet;
    }

    public static String fetchDBid(String connectionid) {
        String dbName = "";
        if (connectionid != null && !connectionid.isEmpty() && (connectionid.contains("|") || connectionid.startsWith(SecurityService.ENCRYPTEDCONNECTIONID_MARKER))) {
            dbName = SecurityService.decryptConnectionId(connectionid).split("\\|")[0];
        }
        return dbName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String stripThreads(String connectionid, String snapshotFilename) throws SapphireException, IOException {
        String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(connectionid, snapshotFilename);
        final String progressFilename = LogViewerUtil.getProgressFilename(snapshotFolderName);
        final File cancelFile = new File(LogViewerUtil.getCancelFilename(snapshotFolderName));
        LogViewerUtil.updateProgress(progressFilename, "Stripping threads: 0%");
        final HashMap tempFiles = new HashMap();
        final HashMap bufferedWriters = new HashMap();
        final HashMap fileOutputStreams = new HashMap();
        final HashMap outputStreamWriters = new HashMap();
        final HashMap indents = new HashMap();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        final boolean[] cancel = new boolean[]{false};
        final String tempFolderName = snapshotFolderName + "/temp";
        try {
            LogViewerUtil.deleteDirectory(tempFolderName);
        }
        catch (IOException e) {
            throw new SapphireException("Unable to delete the temp folder in the snapshot folder");
        }
        File tempFolder = new File(tempFolderName);
        if (tempFolder.exists()) {
            throw new SapphireException("Unable to delete the temporary folder " + tempFolderName);
        }
        if (!tempFolder.mkdir()) {
            throw new SapphireException("Unable to create the temporary folder " + tempFolderName);
        }
        try {
            LogFileProcessor.processFiles(connectionid, snapshotFilename, new LogFileProcessor(){

                @Override
                public void processFile(LogViewerFile logViewerFile, BufferedReader in) throws IOException {
                    if (cancelFile.exists()) {
                        cancel[0] = true;
                        cancelFile.delete();
                    }
                    if (!cancel[0]) {
                        String line;
                        String lastThreadid = "";
                        LogViewerParser parser = LogViewerUtil.getParser();
                        while ((line = in.readLine()) != null) {
                            String threadid = parser.isContentLine(line) ? parser.getLineThread(line) : lastThreadid;
                            if (threadid.length() > 0) {
                                BufferedWriter bw = (BufferedWriter)bufferedWriters.get(threadid);
                                if (bw == null) {
                                    File threadFile = new File(tempFolderName + "/thread" + threadid + ".log");
                                    FileOutputStream fos = new FileOutputStream(threadFile);
                                    OutputStreamWriter osw = new OutputStreamWriter((OutputStream)fos, "UTF-8");
                                    bw = new BufferedWriter(osw);
                                    tempFiles.put(threadid, threadFile);
                                    fileOutputStreams.put(threadid, fos);
                                    outputStreamWriters.put(threadid, osw);
                                    bufferedWriters.put(threadid, bw);
                                    indents.put(threadid, 0);
                                }
                                StringBuilder out = LogViewerUtil.getFormattedLineOut(parser, line, threadid, indents, true);
                                bw.write(out.toString());
                            }
                            lastThreadid = threadid;
                        }
                        LogViewerUtil.updateProgress(progressFilename, "Stripping threads: " + 100 * logViewerFile.filenumber / logViewerFile.filecount + "%");
                    }
                }
            });
        }
        finally {
            try {
                for (String threadid : bufferedWriters.keySet()) {
                    FileOutputStream fos = (FileOutputStream)fileOutputStreams.get(threadid);
                    OutputStreamWriter osw = (OutputStreamWriter)outputStreamWriters.get(threadid);
                    BufferedWriter bw = (BufferedWriter)bufferedWriters.get(threadid);
                    if (bw != null) {
                        bw.close();
                    }
                    if (fos != null) {
                        fos.close();
                    }
                    if (osw == null) continue;
                    osw.close();
                }
                if (in != null) {
                    in.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
            catch (Exception exception) {}
        }
        if (tempFiles.size() > 0) {
            String zipFilename = snapshotFolderName + "/threads.zip";
            LogViewerUtil.createZipFile(tempFolder.getAbsolutePath(), zipFilename, progressFilename, cancelFile);
            LogViewerUtil.deleteDirectory(tempFolderName);
            return "threads.zip";
        }
        return "";
    }

    private static void deleteProgressFile(String progressFilename) {
        try {
            Files.delete(Paths.get(progressFilename, new String[0]));
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static void updateProgress(String progressFilename, String message) {
        try {
            Files.write(Paths.get(progressFilename, new String[0]), message.getBytes(), new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static List<String> executeSearch(String connectionid, String snapshotFilename, String requesttype, String targetid, String userid, String searchConnectionid, String threadid, String[] text, String textCaseSensitive, String hasexception, String exceptiontype, String duration, String stepduration, String fromdate, String todate, String dbid) throws SapphireException, IOException {
        boolean hasStepDuration;
        String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(connectionid, snapshotFilename);
        final String progressFilename = LogViewerUtil.getProgressFilename(snapshotFolderName);
        final File cancelFile = new File(LogViewerUtil.getCancelFilename(snapshotFolderName));
        LogViewerUtil.updateProgress(progressFilename, "Searching: 0%");
        ArrayList<String> found = new ArrayList<String>();
        List<String> requestLines = Files.readAllLines(Paths.get(snapshotFolderName + "/requests.txt", new String[0]), Charset.forName("UTF-8"));
        double durationTarget = duration.length() > 0 ? Double.parseDouble(duration) : -1.0;
        double startDateTarget = fromdate.length() > 0 ? (double)Long.parseLong(fromdate) : -1.0;
        double endDateTarget = todate.length() > 0 ? (double)Long.parseLong(todate) : -1.0;
        for (String requestLine : requestLines) {
            boolean match = true;
            if (match && requesttype.length() > 0 && (match = requestLine.contains("#|#type=" + requesttype + "#")) && targetid.length() > 0) {
                match = requestLine.contains("#|#targetid=" + targetid + "#");
            }
            if (match && "Y".equals(hasexception) && (match = requestLine.contains("#|#hasexception=Y#")) && exceptiontype.length() > 0) {
                match = requestLine.contains(";?ex" + exceptiontype + "#");
            }
            if (match && userid.length() > 0) {
                match = requestLine.contains("#|#userid=" + userid + "#");
            }
            if (match && dbid.length() > 0) {
                match = requestLine.contains("#|#dbid=" + dbid + "#");
            }
            if (match && searchConnectionid.length() > 0) {
                match = requestLine.contains("#|#connectionid=" + searchConnectionid + "#");
            }
            if (match && threadid.length() > 0) {
                match = requestLine.contains("#|#threadid=" + threadid + "#");
            }
            if (match && (durationTarget > 0.0 || fromdate.length() > 0 || todate.length() > 0)) {
                HashMap<String, String> map = LogViewerUtil.getRequestLineMap(requestLine);
                if (match && duration.length() > 0 && map.get("took").length() > 0) {
                    double actual = Double.parseDouble(map.get("took"));
                    boolean bl = match = actual > durationTarget;
                }
                if (match && startDateTarget > 0.0) {
                    long actual = Long.parseLong(map.get("startdatemillis"));
                    boolean bl = match = (double)actual > startDateTarget;
                }
                if (match && endDateTarget > 0.0) {
                    long actual = Long.parseLong(map.get("enddatemillis"));
                    boolean bl = match = (double)actual < endDateTarget;
                }
            }
            if (!match) continue;
            found.add(requestLine);
        }
        final boolean hasFindText = text[0].length() > 0 || text[1].length() > 0 || text[2].length() > 0;
        boolean bl = hasStepDuration = stepduration.length() > 0;
        if (hasFindText || hasStepDuration) {
            final boolean caseSensitive = "Y".equals(textCaseSensitive);
            final String[] findText = new String[]{caseSensitive ? text[0] : text[0].toLowerCase(), caseSensitive ? text[1] : text[1].toLowerCase(), caseSensitive ? text[2] : text[2].toLowerCase()};
            final ArrayList<RequestRange> toFind = new ArrayList<RequestRange>();
            final int step = stepduration.length() > 0 ? Integer.parseInt(stepduration) : 0;
            int total = found.size();
            int maxTotalRow = 0;
            for (int i = total - 1; i >= 0; --i) {
                String requestLine = (String)found.get(i);
                try {
                    HashMap<String, String> map = LogViewerUtil.getRequestLineMap(requestLine);
                    RequestRange range = LogViewerUtil.getRequestRange(map);
                    if (range.endTotalRow > maxTotalRow) {
                        maxTotalRow = range.endTotalRow;
                    }
                    range.index = i;
                    if (range.startTotalRow >= 0 && range.endTotalRow > range.startTotalRow) {
                        toFind.add(range);
                        continue;
                    }
                    found.remove(i);
                    continue;
                }
                catch (NumberFormatException map) {
                    // empty catch block
                }
            }
            final BitSet b = new BitSet(maxTotalRow + 100);
            for (RequestRange range : toFind) {
                b.set(range.startTotalRow, range.endTotalRow - 1);
            }
            final HashMap lastThreadDate = new HashMap();
            final boolean[] cancel = new boolean[]{false};
            LogFileProcessor.processFiles(connectionid, snapshotFilename, new LogFileProcessor(){

                @Override
                public void processFile(LogViewerFile logViewerFile, BufferedReader in) throws IOException {
                    if (cancelFile.exists()) {
                        cancel[0] = true;
                        cancelFile.delete();
                    }
                    if (!cancel[0]) {
                        String line;
                        LogViewerUtil.updateProgress(progressFilename, "Searching: " + 100 * logViewerFile.filenumber / logViewerFile.filecount + "%");
                        LogViewerParser parser = LogViewerUtil.getParser();
                        int lineNum = 0;
                        while ((line = in.readLine()) != null) {
                            long millis;
                            Date contentDate;
                            String findLine;
                            int totalLineNumber;
                            if ((totalLineNumber = logViewerFile.startTotalRow + ++lineNum) >= b.size() || !b.get(totalLineNumber)) continue;
                            String string = findLine = caseSensitive ? line : line.toLowerCase();
                            if (hasFindText) {
                                for (int i = 0; i < findText.length; ++i) {
                                    if (findText[i].length() <= 0 || !findLine.contains(findText[i])) continue;
                                    for (RequestRange range : toFind) {
                                        if (range.textFound[i] || totalLineNumber < range.startTotalRow || totalLineNumber > range.endTotalRow || !range.threadid.equals(parser.getLineThread(line))) continue;
                                        range.textFound[i] = true;
                                    }
                                }
                            }
                            if (!hasStepDuration || !parser.isContentLine(line) || (contentDate = parser.getLineDate(line)) == null) continue;
                            String threadid = parser.getLineThread(line);
                            String content = parser.getLineContent(line);
                            Date lastDate = null;
                            if (!parser.isContentActionStart(content) && !parser.isContentRequestStart(content)) {
                                lastDate = (Date)lastThreadDate.get(threadid);
                            }
                            if (lastDate != null && (millis = contentDate.getTime() - lastDate.getTime()) > (long)step) {
                                for (RequestRange range : toFind) {
                                    if (range.stepDurationFound || totalLineNumber < range.startTotalRow || totalLineNumber >= range.endTotalRow || !range.threadid.equals(parser.getLineThread(line))) continue;
                                    range.stepDurationFound = true;
                                }
                            }
                            lastThreadDate.put(threadid, contentDate);
                        }
                    }
                }
            });
            for (RequestRange range : toFind) {
                if (!(hasStepDuration && !range.stepDurationFound || text[0].length() > 0 && !range.textFound[0] || text[1].length() > 0 && !range.textFound[1]) && (text[2].length() <= 0 || range.textFound[2])) continue;
                found.remove(range.index);
            }
            LogViewerUtil.deleteProgressFile(progressFilename);
        }
        return found;
    }

    public static RequestRange getRequestRange(HashMap<String, String> map) {
        RequestRange range = new RequestRange();
        range.requestIndex = Integer.parseInt(map.get("index"));
        range.startFile = map.get("startfile");
        range.startRow = Integer.parseInt(map.get("startrow"));
        range.endFile = map.get("endfile");
        range.endRow = Integer.parseInt(map.get("endrow"));
        range.threadid = map.get("threadid");
        range.startTotalRow = Integer.parseInt(map.get("starttotalrow"));
        range.endTotalRow = Integer.parseInt(map.get("endtotalrow"));
        return range;
    }

    public static String getLogFolder() throws SapphireException {
        Configuration config = Configuration.getInstance();
        return config.getApplicationHome() + "/logs";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean generateSnapshot(String connectionid, String tempFolderName, String sysuserid, String snapshotid, String description, String timemode, String fromdate, String todate, String hostid, String selectedDBId, String source, String logType) throws SapphireException, IOException {
        String progressFilename = LogViewerUtil.getProgressFilename(tempFolderName);
        File cancelFile = new File(LogViewerUtil.getCancelFilename(tempFolderName));
        boolean snapshotGenerated = false;
        try {
            File tempFolder = new File(tempFolderName);
            tempFolder.deleteOnExit();
            Calendar fromCal = null;
            Calendar toCal = null;
            DateTimeUtil dtu = new DateTimeUtil();
            if (timemode.equals("last2m")) {
                fromCal = dtu.getCalendar("now-2m");
            } else if (timemode.equals("last10m")) {
                fromCal = dtu.getCalendar("now-10m");
            } else if (timemode.equals("last1h")) {
                fromCal = dtu.getCalendar("now-1h");
            } else if (timemode.equals("last1d")) {
                fromCal = dtu.getCalendar("now-1d");
            } else {
                if (fromdate.length() > 0 && (fromCal = dtu.getCalendar(fromdate)) == null) {
                    throw new SapphireException("Unable to determine a from-date using " + fromdate);
                }
                if (todate.length() > 0 && (toCal = dtu.getCalendar(todate)) == null) {
                    throw new SapphireException("Unable to determine a to-date using " + todate);
                }
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fromDateString = fromCal == null ? null : sdf.format(fromCal.getTime());
            String toDateString = toCal == null ? null : sdf.format(toCal.getTime());
            String logFileName = "";
            if (source != null && source.equalsIgnoreCase("Issue")) {
                snapshotGenerated = LogViewerUtil.generateLogForCurrentDB(connectionid, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, progressFilename, cancelFile, tempFolder, sdf, fromDateString, toDateString, logType);
            } else if (selectedDBId == null || selectedDBId.length() == 0) {
                logFileName = LogViewerUtil.isDiagnosticLoggingSelected(logType) ? "labvantage.diag.log" : "labvantage.log";
                snapshotGenerated = LogViewerUtil.createSnapShot(connectionid, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, progressFilename, cancelFile, tempFolder, sdf, fromDateString, toDateString, logFileName, null);
            } else if (selectedDBId.equalsIgnoreCase("All")) {
                String[] dbids = LogViewerUtil.getConfiguredDBList();
                for (int len = 0; len < dbids.length; ++len) {
                    String dbID = dbids[len];
                    dbID = dbID.replaceAll("_", ".").trim();
                    logFileName = LogViewerUtil.isDiagnosticLoggingSelected(logType) ? dbID + ".diag.log" : dbID + ".log";
                    snapshotGenerated = LogViewerUtil.createSnapShot(connectionid, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, progressFilename, cancelFile, tempFolder, sdf, fromDateString, toDateString, logFileName, dbID);
                }
            } else if (selectedDBId.equalsIgnoreCase("This")) {
                snapshotGenerated = LogViewerUtil.generateLogForCurrentDB(connectionid, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, progressFilename, cancelFile, tempFolder, sdf, fromDateString, toDateString, logType);
            } else {
                selectedDBId = selectedDBId.replaceAll("_", ".");
                logFileName = LogViewerUtil.isDiagnosticLoggingSelected(logType) ? selectedDBId + ".diag.log" : selectedDBId + ".log";
                snapshotGenerated = LogViewerUtil.createSnapShot(connectionid, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, progressFilename, cancelFile, tempFolder, sdf, fromDateString, toDateString, logFileName, selectedDBId);
            }
        }
        finally {
            LogViewerUtil.deleteProgressFile(progressFilename);
            LogViewerUtil.deleteDirectory(tempFolderName);
        }
        return snapshotGenerated;
    }

    private static boolean generateLogForCurrentDB(String connectionid, String sysuserid, String snapshotid, String description, String timemode, String fromdate, String todate, String hostid, String progressFilename, File cancelFile, File tempFolder, SimpleDateFormat sdf, String fromDateString, String toDateString, String logType) throws SapphireException, IOException {
        String[] dbids = LogViewerUtil.getConfiguredDBList();
        String loogedInDB = LogViewerUtil.fetchDBid(connectionid);
        boolean snapshotGenerated = false;
        boolean found = false;
        for (int len = 0; len < dbids.length; ++len) {
            String dbID = dbids[len];
            if (!loogedInDB.equalsIgnoreCase(dbID.trim())) continue;
            found = true;
            break;
        }
        if (found) {
            loogedInDB = loogedInDB.replaceAll("_", ".").trim();
            String logFileName = LogViewerUtil.isDiagnosticLoggingSelected(logType) ? loogedInDB + ".diag.log" : loogedInDB + ".log";
            snapshotGenerated = LogViewerUtil.createSnapShot(connectionid, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, progressFilename, cancelFile, tempFolder, sdf, fromDateString, toDateString, logFileName, loogedInDB);
        } else {
            String logFileName = LogViewerUtil.isDiagnosticLoggingSelected(logType) ? "labvantage.diag.log" : "labvantage.log";
            snapshotGenerated = LogViewerUtil.createSnapShot(connectionid, sysuserid, snapshotid, description, timemode, fromdate, todate, hostid, progressFilename, cancelFile, tempFolder, sdf, fromDateString, toDateString, logFileName, null);
        }
        return snapshotGenerated;
    }

    public static String[] getConfiguredDBList() throws SapphireException {
        Configuration configuration = Configuration.getInstance();
        LogConfig logConfig = new LogConfig(configuration.getApplicationid(), configuration.getLogConfigFile());
        HashSet tempSet = new HashSet();
        String dbList = logConfig.getSplitByDatabaseList();
        String[] dbids = dbList.split(",");
        return dbids;
    }

    public static boolean isDiagnosticLoggingSelected(String logType) throws SapphireException {
        return logType != null && logType != "" && logType.equalsIgnoreCase("diagnosticlog");
    }

    public static boolean isLogSplittedByDB() throws SapphireException {
        String[] dbids = LogViewerUtil.getConfiguredDBList();
        boolean isConfigured = true;
        if (dbids.length == 1 && dbids[0].equalsIgnoreCase("")) {
            isConfigured = false;
        }
        return isConfigured;
    }

    private static boolean createSnapShot(String connectionid, String sysuserid, String snapshotid, String description, String timemode, String fromdate, String todate, String hostid, String progressFilename, File cancelFile, File tempFolder, SimpleDateFormat sdf, String fromDateString, String toDateString, String logFileName, String dbid) throws SapphireException, IOException {
        File[] files = LogViewerUtil.getAllLogFiles(logFileName);
        boolean snapshotGenerated = false;
        ArrayList<String> firstEntryDate = new ArrayList<String>();
        for (File file : files) {
            firstEntryDate.add(LogViewerUtil.getFirstDate(file));
        }
        String lastEntryDate = LogViewerUtil.getLastDate(files[0]);
        int MODE_WAITING = 0;
        int MODE_STARTED = 1;
        int MODE_STOPPING = 2;
        int mode = fromDateString == null ? MODE_STARTED : MODE_WAITING;
        String logStart = "";
        String logEnd = lastEntryDate;
        PropertyListCollection filelist = new PropertyListCollection();
        boolean cancel = false;
        for (int i = firstEntryDate.size() - 1; i >= 0 && !cancel; --i) {
            if (mode == MODE_STARTED && i > 0 && toDateString != null && ((String)firstEntryDate.get(i)).compareTo(toDateString) > 0) {
                mode = MODE_STOPPING;
                String string = logEnd = i > 0 ? LogViewerUtil.getLastDate(files[i - 1]) : "";
            }
            if (mode == MODE_WAITING) {
                if (i == 0) {
                    if (lastEntryDate.compareTo(fromDateString) > 0) {
                        mode = MODE_STARTED;
                    }
                } else if (((String)firstEntryDate.get(i - 1)).compareTo(fromDateString) > 0) {
                    mode = MODE_STARTED;
                }
            }
            if (mode != MODE_STARTED) continue;
            if (logStart.length() == 0) {
                logStart = (String)firstEntryDate.get(i);
            }
            PropertyList filepl = new PropertyList();
            filepl.setProperty("filename", files[i].getName());
            filelist.add(filepl);
            File newFile = new File(tempFolder + "/" + files[i].getName());
            LogViewerUtil.updateProgress(progressFilename, "Copying: " + files[i].getName());
            Files.copy(files[i].toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            snapshotGenerated = true;
            if (!cancelFile.exists()) continue;
            cancel = true;
            cancelFile.delete();
        }
        if (!cancel && snapshotGenerated) {
            PropertyList manifest = new PropertyList();
            manifest.setProperty("version", Build.getVersion());
            manifest.setProperty("id", snapshotid);
            manifest.setProperty("description", description);
            manifest.setProperty("shapshotdt", sdf.format(Calendar.getInstance().getTime()));
            manifest.setProperty("shapshotby", sysuserid);
            manifest.setProperty("shapshotbyconnectionid", connectionid);
            manifest.setProperty("timemode", timemode);
            manifest.setProperty("fromdate", fromdate);
            manifest.setProperty("todate", todate);
            manifest.setProperty("logfromdate", fromDateString == null || fromDateString.length() == 0 ? logStart.substring(0, 19) : fromDateString);
            manifest.setProperty("logtodate", toDateString == null || toDateString.length() == 0 ? (logEnd == null || logEnd.equals("") ? "" : logEnd.substring(0, 19)) : toDateString);
            manifest.setProperty("hostid", hostid);
            manifest.setProperty("dbid", dbid);
            manifest.setProperty("filelist", filelist);
            Files.write(Paths.get(tempFolder.getAbsolutePath() + "/snapshot.mf", new String[0]), manifest.toXMLString().getBytes(), new OpenOption[0]);
            String snapshotFileName = "";
            snapshotFileName = dbid == null || dbid.length() == 0 ? snapshotid + "-" + hostid + ".zip" : snapshotid + "-" + hostid + "-" + dbid + ".zip";
            File check = new File(LogViewerUtil.getSnapshotFolder(connectionid) + "/" + snapshotFileName);
            if (check.exists()) {
                throw new SapphireException("Snapshot file " + snapshotFileName + " already exists.");
            }
            LogViewerUtil.createZipFile(tempFolder.getAbsolutePath(), LogViewerUtil.getSnapshotFolder(connectionid) + "/" + snapshotFileName, progressFilename, cancelFile);
        }
        LogViewerUtil.deleteFile(tempFolder.getAbsolutePath());
        return snapshotGenerated;
    }

    private static void deleteFile(String directoryName) throws SapphireException {
        File[] files;
        File directory = new File(directoryName);
        for (File file : files = directory.listFiles()) {
            if (file.delete()) continue;
            throw new SapphireException("File not deleted " + file);
        }
    }

    public static File[] getAllLogFiles() throws SapphireException {
        File logFolder = new File(LogViewerUtil.getLogFolder());
        File[] allFiles = logFolder.listFiles(new FilenameFilter(){

            @Override
            public boolean accept(File dir, String name) {
                return name.contains("labvantage.log");
            }
        });
        Arrays.sort(allFiles, new Comparator<File>(){

            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
        return allFiles;
    }

    public static File[] getAllLogFiles(final String logfileName) throws SapphireException {
        File logFolder = new File(LogViewerUtil.getLogFolder());
        File[] allFiles = logFolder.listFiles(new FilenameFilter(){

            @Override
            public boolean accept(File dir, String name) {
                return name.contains(logfileName);
            }
        });
        Arrays.sort(allFiles, new Comparator<File>(){

            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
        return allFiles;
    }

    public static File[] sortFiles(File[] allFiles) throws SapphireException {
        Arrays.sort(allFiles, new Comparator<File>(){

            @Override
            public int compare(File f1, File f2) {
                return Long.compare(f2.lastModified(), f1.lastModified());
            }
        });
        return allFiles;
    }

    public static String getFirstDate(File file) throws SapphireException {
        LogViewerParser parser = LogViewerUtil.getParser();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        String firstDate = "";
        try {
            String line;
            fis = new FileInputStream(file);
            isr = new InputStreamReader((InputStream)fis, "UTF-8");
            in = new BufferedReader(isr);
            while ((line = in.readLine()) != null & firstDate.length() == 0) {
                if (!parser.isContentLine(line)) continue;
                firstDate = parser.getLineDateString(line);
            }
            String string = firstDate;
            return string;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
    }

    public static String getLastDate(File file) throws SapphireException {
        LogViewerParser parser = LogViewerUtil.getParser();
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader in = null;
        String lastContentLine = "";
        try {
            String line;
            fis = new FileInputStream(file);
            isr = new InputStreamReader((InputStream)fis, "UTF-8");
            in = new BufferedReader(isr);
            while ((line = in.readLine()) != null) {
                if (!parser.isContentLine(line)) continue;
                lastContentLine = line;
            }
            String string = lastContentLine.length() > 0 ? parser.getLineDateString(lastContentLine) : "";
            return string;
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (isr != null) {
                    isr.close();
                }
                if (fis != null) {
                    fis.close();
                }
            }
            catch (Exception e) {
                throw new SapphireException(e);
            }
        }
    }

    public static void deleteDirectory(String directoryFilePath) throws IOException {
        Path directory = Paths.get(directoryFilePath, new String[0]);
        if (Files.exists(directory, new LinkOption[0])) {
            Files.walkFileTree(directory, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
                    FileDeleteStrategy.FORCE.delete(path.toFile());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException {
                    Files.delete(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public static boolean createZipFile(String sourceDirPath, String zipFilePath, final String progressFilename, final File cancelFile) throws IOException {
        final boolean[] cancel = new boolean[]{false};
        File zipFile = new File(zipFilePath);
        FileOutputStream fos = new FileOutputStream(zipFile);
        final ZipOutputStream zos = new ZipOutputStream(fos);
        final Path folder = new File(sourceDirPath).toPath();
        Files.walkFileTree(folder, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (!progressFilename.endsWith(file.toFile().getName())) {
                    if (progressFilename.length() > 0) {
                        LogViewerUtil.updateProgress(progressFilename, "Zipping " + file.toFile().getName());
                    }
                    zos.putNextEntry(new ZipEntry(folder.relativize(file).toString()));
                    Files.copy(file, zos);
                    zos.closeEntry();
                    if (cancelFile.exists()) {
                        cancel[0] = true;
                        cancelFile.delete();
                    }
                }
                return cancel[0] ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
            }
        });
        zos.close();
        fos.close();
        if (cancel[0] && zipFile.exists()) {
            zipFile.delete();
        }
        return cancel[0];
    }

    public static DataSet getSnapshots(String connectionid, String sortedColumn, String sortedOrder) throws SapphireException {
        DataSet ds = LogViewerUtil.getSnapshots(connectionid, "");
        ds.sort(sortedColumn + " " + sortedOrder);
        return ds;
    }

    public static DataSet getSnapshots(String connectionid, String filename) throws SapphireException {
        String snapshotFolderName = LogViewerUtil.getSnapshotFolder(connectionid);
        File snapshots = new File(snapshotFolderName);
        File[] files = snapshots.listFiles();
        DataSet ds = new DataSet();
        for (int i = 0; i < files.length; ++i) {
            File file = files[i];
            if (!file.getName().startsWith("LVS") || file.isDirectory() || !file.getName().endsWith(".zip") || filename.length() != 0 && !file.getName().equalsIgnoreCase(filename)) continue;
            try {
                PropertyList props = LogViewerUtil.getManifestFile(file);
                if (props.size() <= 0) continue;
                long kb = file.length() / 1024L;
                DecimalFormat formatter = new DecimalFormat("#,###");
                String size = formatter.format(kb) + " KB";
                int row = ds.addRow();
                ds.setString(row, "type", "folder");
                ds.setString(row, "filename", file.getName());
                ds.setString(row, "absolutefilename", file.getAbsolutePath());
                ds.setString(row, "id", props.getProperty("id"));
                ds.setString(row, "description", props.getProperty("description"));
                ds.setString(row, "shapshotdt", props.getProperty("shapshotdt"));
                ds.setString(row, "shapshotby", props.getProperty("shapshotby"));
                ds.setString(row, "fromdate", props.getProperty("fromdate"));
                ds.setString(row, "todate", props.getProperty("todate"));
                ds.setString(row, "logfromdate", props.getProperty("logfromdate").length() > 16 ? props.getProperty("logfromdate").substring(0, 16) : props.getProperty("logfromdate"));
                ds.setString(row, "logtodate", props.getProperty("logtodate").length() > 16 ? props.getProperty("logtodate").substring(0, 16) : props.getProperty("logtodate"));
                ds.setString(row, "hostid", props.getProperty("hostid"));
                ds.setString(row, "dbid", props.getProperty("dbid"));
                ds.setString(row, "size", size);
                ds.setString(row, "selected", LogViewerUtil.snapshotSelected(file.getName()).toString());
                continue;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        LogViewerUtil.setStartAnalysis(false);
        zipName.clear();
        return ds;
    }

    private static Boolean snapshotSelected(String snapshotID) {
        Boolean found = false;
        if (LogViewerUtil.getStartAnalysis().booleanValue()) {
            for (String val : quickSearchList) {
                if (!val.equalsIgnoreCase(snapshotID)) continue;
                found = true;
            }
        } else {
            for (String val : zipName) {
                if (!val.equalsIgnoreCase(snapshotID)) continue;
                found = true;
            }
        }
        return found;
    }

    public static PropertyList getManifestFile(File snapshotFile) throws IOException, SapphireException {
        ZipFile zipFile = new ZipFile(snapshotFile);
        PropertyList props = new PropertyList();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!"snapshot.mf".equals(entry.getName())) continue;
            InputStream in = zipFile.getInputStream(entry);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuffer out = new StringBuffer();
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            props.setPropertyList(out.toString());
            reader.close();
            in.close();
        }
        zipFile.close();
        return props;
    }

    public static boolean extractSnapshot(String connectionid, String snapshotFilename) throws SapphireException, IOException {
        String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(connectionid, snapshotFilename);
        try {
            LogViewerUtil.deleteDirectory(snapshotFolderName);
        }
        catch (IOException e) {
            throw new SapphireException("Unable to delete the temp folder inside the snapshot folder.\nA file in that folder may be locked.");
        }
        File tempFolder = new File(snapshotFolderName);
        if (tempFolder.exists()) {
            throw new SapphireException("Unable to delete the temp folder inside the snapshot folder.\nA file in that folder may be locked.");
        }
        tempFolder.mkdir();
        String progressFilename = LogViewerUtil.getProgressFilename(snapshotFolderName);
        File cancelFile = new File(LogViewerUtil.getCancelFilename(snapshotFolderName));
        boolean cancel = false;
        byte[] buffer = new byte[1024];
        try {
            FileInputStream fis = new FileInputStream(LogViewerUtil.getSnapshotFolder(connectionid) + "/" + snapshotFilename);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null && !cancel) {
                int len;
                if (cancelFile.exists()) {
                    cancel = true;
                    cancelFile.delete();
                }
                String fileName = ze.getName();
                LogViewerUtil.updateProgress(progressFilename, "Extracting " + fileName);
                File newFile = new File(snapshotFolderName + "/" + fileName);
                FileOutputStream fos = new FileOutputStream(newFile);
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fis.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if (cancel) {
            tempFolder.delete();
        }
        return cancel;
    }

    public static String getSnapshotFolderName(String connectionid, String snapshotFilename) throws SapphireException {
        return LogViewerUtil.getSnapshotFolder(connectionid) + "/" + snapshotFilename.substring(0, snapshotFilename.length() - 4);
    }

    public static void endRequest(LogViewerParser parser, LogViewerFile logViewerFile, String line, int lineNum, ArrayList<LogViewerRequest> requestStack) {
        LogViewerRequest removed = requestStack.remove(requestStack.size() - 1);
        removed.endDate = parser.getLineDateString(line);
        removed.endFilename = logViewerFile.filename;
        removed.endRow = lineNum;
        removed.endTotalRow = logViewerFile.startTotalRow + lineNum;
        if (removed.startDateDate != null) {
            removed.endDateDate = parser.getLineDate(line);
            removed.took = removed.endDateDate.getTime() - removed.startDateDate.getTime();
        }
    }

    private static String getUserFromConnection(String connectionid) {
        if (connectionid.length() == 0) {
            return "";
        }
        connectionid = SecurityService.decryptConnectionId(connectionid);
        String userid = "";
        String temp = connectionid.substring(connectionid.indexOf("|") + 1);
        int pos = temp.lastIndexOf(45);
        if (pos > 0) {
            userid = temp.substring(0, pos);
        } else {
            int count = 0;
            while (count < temp.length()) {
                if (!Character.isDigit(temp.charAt(count++))) continue;
                userid = temp.substring(0, count - 1);
                break;
            }
        }
        return userid;
    }

    private static String getRequestConnectionid(String content) {
        int pos2;
        String connectionid = "";
        int pos = content.indexOf("for [");
        if (pos > 0 && (pos2 = content.indexOf("]", pos)) > 0) {
            connectionid = content.substring(pos + 5, pos2);
        }
        return connectionid.contains("|") || connectionid.startsWith(SecurityService.ENCRYPTEDCONNECTIONID_MARKER) ? connectionid : "";
    }

    public static String getCancelFilename(String folderName) {
        return folderName + "/cancel.txt";
    }

    public static String getProgressFilename(String folderName) {
        return folderName + "/progress.txt";
    }

    public static LogViewerParser getParser() {
        return new LogViewerParser_Default();
    }

    public static void addConnection(String connectionid, ArrayList<LogViewerConnection> connections) {
        LogViewerConnection connection = new LogViewerConnection();
        connection.connectionid = connectionid;
        if (!connections.contains(connection)) {
            connection.userid = LogViewerUtil.getUserFromConnection(connectionid);
            connections.add(connection);
        }
    }

    public static PropertyList getManifestPropertyList(String tempFolderName) throws SapphireException, IOException {
        File manifest = new File(tempFolderName + "/snapshot.mf");
        FileInputStream in = new FileInputStream(manifest);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuffer out = new StringBuffer();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        PropertyList props = new PropertyList();
        props.setPropertyList(out.toString());
        reader.close();
        ((InputStream)in).close();
        return props;
    }

    public static HashMap<String, String> getRequestLineMap(String line) {
        String[] parts = StringUtil.split(line, "#|#");
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < parts.length; ++i) {
            if (parts[i].length() <= 0) continue;
            int pos = parts[i].indexOf("=");
            String key = parts[i].substring(0, pos);
            String value = parts[i].substring(pos + 1);
            map.put(key, value);
        }
        return map;
    }

    public static String prepareRequestDownload(String connectionid, String snapshotFilename, Set<String> requestIndexSet) throws SapphireException, IOException {
        String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(connectionid, snapshotFilename);
        final String progressFilename = LogViewerUtil.getProgressFilename(snapshotFolderName);
        LogViewerUtil.updateProgress(progressFilename, "Preparing: 0%");
        List<String> requestLines = Files.readAllLines(Paths.get(snapshotFolderName + "/requests.txt", new String[0]), Charset.forName("UTF-8"));
        File snapshotFolder = new File(snapshotFolderName);
        File fileout = File.createTempFile("searchresults", "log", snapshotFolder);
        FileOutputStream fos = new FileOutputStream(fileout);
        OutputStreamWriter osw = new OutputStreamWriter((OutputStream)fos, "UTF-8");
        final BufferedWriter bw = new BufferedWriter(osw);
        int maxTotalRow = 0;
        final ArrayList<RequestRange> toExport = new ArrayList<RequestRange>();
        for (String string : requestLines) {
            HashMap<String, String> map = LogViewerUtil.getRequestLineMap(string);
            String index = map.get("index");
            if (!requestIndexSet.contains(index)) continue;
            RequestRange range = LogViewerUtil.getRequestRange(map);
            toExport.add(range);
        }
        final BitSet b = new BitSet(maxTotalRow + 100);
        for (RequestRange range : toExport) {
            b.set(range.startTotalRow, range.endTotalRow + 1);
        }
        final HashMap hashMap = new HashMap();
        final HashMap nextThreadRequest = new HashMap();
        final HashMap indents = new HashMap();
        LogFileProcessor.processFiles(connectionid, snapshotFilename, new LogFileProcessor(){

            @Override
            public void processFile(LogViewerFile logViewerFile, BufferedReader in) throws IOException {
                String line;
                LogViewerUtil.updateProgress(progressFilename, "Preparing: " + 100 * logViewerFile.filenumber / logViewerFile.filecount + "%");
                LogViewerParser parser = LogViewerUtil.getParser();
                int lineNum = 0;
                String lastContentThread = "";
                while ((line = in.readLine()) != null) {
                    int totalLineNumber;
                    if ((totalLineNumber = logViewerFile.startTotalRow + ++lineNum) >= b.size() || !b.get(totalLineNumber)) continue;
                    String threadid = parser.getLineThread(line);
                    if (threadid.length() == 0) {
                        threadid = lastContentThread;
                    }
                    lastContentThread = threadid;
                    RequestRange currentRange = (RequestRange)hashMap.get(threadid);
                    if (currentRange == null) {
                        RequestRange nextRange = (RequestRange)nextThreadRequest.get(threadid);
                        if (nextRange == null) {
                            for (RequestRange r : toExport) {
                                if (nextRange != null || !r.threadid.equals(threadid) || r.startTotalRow < totalLineNumber) continue;
                                nextRange = r;
                            }
                        }
                        if (nextRange != null && nextRange.startTotalRow == totalLineNumber) {
                            currentRange = nextRange;
                            currentRange.out = new StringBuffer();
                            hashMap.put(threadid, currentRange);
                            nextThreadRequest.put(threadid, null);
                        }
                    }
                    if (currentRange == null) continue;
                    StringBuilder out = LogViewerUtil.getFormattedLineOut(parser, line, threadid, indents, false);
                    currentRange.out.append((CharSequence)out);
                    if (totalLineNumber != currentRange.endTotalRow) continue;
                    bw.write(currentRange.out.toString());
                    bw.newLine();
                    bw.newLine();
                    currentRange.out.setLength(0);
                    hashMap.put(threadid, null);
                }
            }
        });
        bw.close();
        osw.close();
        fos.close();
        LogViewerUtil.deleteProgressFile(progressFilename);
        return fileout.getName();
    }

    public static String getRequestView(String connectionid, String snapshotFilename, String requestIndex, HashMap<String, String> displayOptions, String[] text, final boolean textCaseSensitive) throws SapphireException, IOException {
        String textc;
        String textb;
        String texta;
        String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(connectionid, snapshotFilename);
        List<String> requestLines = Files.readAllLines(Paths.get(snapshotFolderName + "/requests.txt", new String[0]), Charset.forName("UTF-8"));
        RequestRange range = null;
        for (String requestLine : requestLines) {
            HashMap<String, String> map = LogViewerUtil.getRequestLineMap(requestLine);
            String index = map.get("index");
            if (!requestIndex.equals(index)) continue;
            range = LogViewerUtil.getRequestRange(map);
        }
        final HashMap indents = new HashMap();
        final StringBuilder out = new StringBuilder();
        final long[] start = new long[]{0L};
        final long[] lastline = new long[]{0L};
        out.append("<table style=\"font-family:courier\">");
        final boolean hideDebug = "Y".equals(displayOptions.get("hidedebug"));
        final boolean showLevel = "Y".equals(displayOptions.get("showlevel"));
        final boolean showRaw = "Y".equals(displayOptions.get("showraw"));
        final boolean showContext = "Y".equals(displayOptions.get("showcontext"));
        final boolean indenting = "Y".equals(displayOptions.get("indenting"));
        int highlightStepValue = 0;
        try {
            highlightStepValue = Integer.parseInt(displayOptions.get("highlightstepvalue"));
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        String string = text[0] == null || text[0].length() == 0 ? null : (texta = textCaseSensitive ? HttpUtil.htmlEncode(text[0]) : HttpUtil.htmlEncode(text[0].toUpperCase()));
        String string2 = text[1] == null || text[1].length() == 0 ? null : (textb = textCaseSensitive ? HttpUtil.htmlEncode(text[1]) : HttpUtil.htmlEncode(text[1].toUpperCase()));
        String string3 = text[2] == null || text[2].length() == 0 ? null : (textc = textCaseSensitive ? HttpUtil.htmlEncode(text[2]) : HttpUtil.htmlEncode(text[2].toUpperCase()));
        if (range != null) {
            final RequestRange finalRange = range;
            final int finalHighlightStepValue = highlightStepValue;
            LogFileProcessor.processFiles(connectionid, snapshotFilename, new LogFileProcessor(){

                @Override
                public void processFile(LogViewerFile logViewerFile, BufferedReader in) throws IOException {
                    String line;
                    LogViewerParser parser = LogViewerUtil.getParser();
                    int lineNum = 0;
                    String lastContentThread = "";
                    StringBuilder writeLine = new StringBuilder();
                    boolean lastLineBlank = false;
                    while ((line = in.readLine()) != null) {
                        String threadid;
                        int totalLineNumber;
                        if ((totalLineNumber = logViewerFile.startTotalRow + ++lineNum) < finalRange.startTotalRow || totalLineNumber > finalRange.endTotalRow) continue;
                        if (!showRaw && totalLineNumber == finalRange.startTotalRow) {
                            start[0] = parser.getLineDate(line).getTime();
                            out.append("<tr><td colspan=\"5\">" + parser.getLineDateString(line) + "</td></tr>");
                        }
                        if ((threadid = parser.getLineThread(line)).length() == 0) {
                            threadid = lastContentThread;
                        }
                        lastContentThread = threadid;
                        if (!threadid.equals(finalRange.threadid)) continue;
                        writeLine.setLength(0);
                        boolean blankBefore = false;
                        boolean blankAfter = false;
                        int indent = 0;
                        if (!showRaw && parser.isContentLine(line)) {
                            String level;
                            String string = level = hideDebug || showLevel ? parser.getLineLevel(line) : "";
                            if (!hideDebug || !"DEBUG".equals(level)) {
                                writeLine.append("<tr>");
                                Date d = parser.getLineDate(line);
                                long fromlast = lastline[0] > 0L ? d.getTime() - lastline[0] : 0L;
                                writeLine.append("<td nowrap " + (fromlast > (long)finalHighlightStepValue ? "class=\"preview_time_red\"" : "class=\"preview_time\"") + ">");
                                writeLine.append(d.getTime() - start[0]);
                                writeLine.append("</td>");
                                lastline[0] = d.getTime();
                                if (showLevel) {
                                    String displayLevel = LogViewerUtil.highlightText(level, texta, textb, textc, textCaseSensitive);
                                    writeLine.append("<td class=\"preview_level\">" + (level.length() == 0 ? "&nbsp;" : displayLevel) + "</td>");
                                }
                                if (showContext) {
                                    String context = parser.getLineContext(line);
                                    context = LogViewerUtil.highlightText(context, texta, textb, textc, textCaseSensitive);
                                    writeLine.append("<td class=\"preview_context\">" + (context.length() == 0 ? "&nbsp;" : context) + "</td>");
                                }
                                if (indenting) {
                                    Integer ii = (Integer)indents.get(threadid);
                                    indent = ii == null ? 0 : ii;
                                }
                                String content = parser.getLineContent(line);
                                if (indenting && (content.startsWith("END:") || parser.isContentActionEnd(content))) {
                                    indents.put(threadid, indent -= 2);
                                    blankAfter = true;
                                }
                                String prefix = "";
                                if (indenting && indent > 0 && indent < 100) {
                                    prefix = StringUtil.replaceAll(LogViewerUtil.indentString.substring(0, indent), " ", "&nbsp;");
                                }
                                writeLine.append("<td class=\"preview_content\" nowrap>");
                                String displayContent = HttpUtil.htmlEncode(content);
                                displayContent = LogViewerUtil.highlightText(displayContent, texta, textb, textc, textCaseSensitive);
                                writeLine.append(prefix + displayContent);
                                writeLine.append("</td>");
                                if (indenting && (content.startsWith("START:") || parser.isContentActionStart(content))) {
                                    Integer iii = (Integer)indents.get(threadid);
                                    indent = iii == null ? 0 : iii;
                                    indents.put(threadid, indent += 2);
                                    blankBefore = true;
                                }
                                writeLine.append("</tr>");
                            }
                        } else {
                            writeLine.append("</tr>");
                            line = HttpUtil.htmlEncode(line);
                            line = LogViewerUtil.highlightText(line, texta, textb, textc, textCaseSensitive);
                            writeLine.append("<td nowrap colspan=\"5\">" + line + "</td>");
                            writeLine.append("</tr>");
                        }
                        String blankLine = "<tr height=\"5px\"><td colspan=\"5\"></td></tr>";
                        out.append(blankBefore && !lastLineBlank ? blankLine : "").append((CharSequence)writeLine).append(blankAfter ? blankLine : "");
                        lastLineBlank = blankAfter;
                    }
                }
            });
        }
        out.append("</table>");
        return out.toString();
    }

    private static String highlightText(String line, String texta, String textb, String textc, boolean textCaseSensitive) {
        int pos;
        if (texta == null && textb == null && textc == null) {
            return line;
        }
        String searchLine = line;
        if (!textCaseSensitive) {
            searchLine = line.toUpperCase();
        }
        if (texta != null && (pos = searchLine.indexOf(texta)) >= 0) {
            line = line.substring(0, pos) + "<span id=\"hh\" class=\"highlighta\">" + line.substring(pos, pos + texta.length()) + "</span>" + line.substring(pos + texta.length());
            searchLine = searchLine.substring(0, pos) + "<span id=\"hh\" class=\"highlighta\">" + searchLine.substring(pos, pos + texta.length()) + "</span>" + searchLine.substring(pos + texta.length());
        }
        if (textb != null && (pos = searchLine.indexOf(textb)) >= 0) {
            line = line.substring(0, pos) + "<span id=\"hh\" class=\"highlightb\">" + line.substring(pos, pos + textb.length()) + "</span>" + line.substring(pos + textb.length());
            searchLine = searchLine.substring(0, pos) + "<span id=\"hh\" class=\"highlighta\">" + searchLine.substring(pos, pos + texta.length()) + "</span>" + searchLine.substring(pos + texta.length());
        }
        if (textc != null && (pos = searchLine.indexOf(textc)) >= 0) {
            line = line.substring(0, pos) + "<span id=\"hh\" class=\"highlightc\">" + line.substring(pos, pos + textc.length()) + "</span>" + line.substring(pos + textc.length());
        }
        return line;
    }

    public static StringBuilder getFormattedLineOut(LogViewerParser parser, String line, String threadid, HashMap<String, Integer> indents, boolean checkRequestStartEnd) {
        String content;
        StringBuilder out = new StringBuilder();
        Integer ii = indents.get(threadid);
        int indent = ii == null ? 0 : ii;
        String string = content = parser.isContentLine(line) ? parser.getLineContent(line) : line;
        if (content.startsWith("END:")) {
            indents.put(threadid, indent -= 2);
        }
        if (indent > 0 && indent < 100) {
            int pos = parser.getLineContentPos(line);
            String string2 = line = pos >= 0 ? line.substring(0, pos) + indentString.substring(0, indent) + line.substring(pos) : line;
        }
        if (checkRequestStartEnd && (parser.isContentRequestStart(content) || parser.isContentActionStart(content))) {
            out.append("\n\n");
        }
        out.append(line + "\n");
        if (checkRequestStartEnd && (parser.isContentRequestEnd(content) || parser.isContentActionEnd(content))) {
            out.append("\n");
        }
        if (content.startsWith("START:")) {
            Integer iii = indents.get(threadid);
            indent = iii == null ? 0 : iii;
            indents.put(threadid, indent += 2);
        }
        return out;
    }

    public static List<String> getStackTraces(String connectionid, String snapshotFilename, String exceptiontype) throws SapphireException, IOException {
        String snapshotFolderName = LogViewerUtil.getSnapshotFolderName(connectionid, snapshotFilename);
        ArrayList<String> found = new ArrayList<String>();
        List<String> requestLines = Files.readAllLines(Paths.get(snapshotFolderName + "/exceptions.txt", new String[0]), Charset.forName("UTF-8"));
        for (String requestLine : requestLines) {
            boolean match = true;
            if (match && exceptiontype.length() > 0) {
                match = requestLine.startsWith("#|#" + exceptiontype + "#");
            }
            if (!match) continue;
            found.add(requestLine);
        }
        return found;
    }

    public static void quickSearch(String connectionID, String[] requestedFiles, String searchText) throws SapphireException, IOException {
        zipName.clear();
        for (String requestedFile : requestedFiles) {
            try (ZipFile zipFile = new ZipFile(LogViewerUtil.getSnapshotFolder(connectionID) + File.separator + requestedFile);){
                boolean found = LogViewerUtil.extractZipFileAndDoSearch(zipFile, searchText);
                if (!found) continue;
                String fileSep = Pattern.quote(System.getProperty("file.separator"));
                zipName.add(zipFile.getName().split(fileSep)[zipFile.getName().split(fileSep).length - 1]);
            }
        }
        quickSearchList.addAll(zipName);
    }

    private static String[] getZipFiles(String folderPath) throws SapphireException {
        File[] allFiles;
        ArrayList<String> requestedFiles = new ArrayList<String>();
        File folder = new File(folderPath);
        for (File f : allFiles = folder.listFiles()) {
            if (!ZipFileUtil.isArchive(f)) continue;
            requestedFiles.add(f.getName());
        }
        return (String[])requestedFiles.toArray();
    }

    private static boolean extractZipFileAndDoSearch(ZipFile zipFile, String searchText) throws SapphireException, IOException {
        boolean found = false;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            String extractedString;
            ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().contains(".mf") || !LogViewerUtil.doSearch(extractedString = ZipFileUtil.extractString(zipFile, zipEntry), searchText)) continue;
            found = true;
            break;
        }
        return found;
    }

    private static boolean doSearch(String extractedString, String searchText) {
        return extractedString.contains(searchText);
    }

    public static void clearSelectedZip() {
        zipName.clear();
        quickSearchList.clear();
    }

    public static Boolean getStartAnalysis() {
        return startAnalysis;
    }

    public static void setStartAnalysis(Boolean startAnalysis) {
        LogViewerUtil.startAnalysis = startAnalysis;
    }

    public static class RequestRange {
        public String startFile;
        public int startRow;
        public String endFile;
        public int endRow;
        public String threadid;
        public int startTotalRow;
        public int endTotalRow;
        public int index;
        public int requestIndex;
        public boolean[] textFound = new boolean[3];
        public boolean stepDurationFound;
        public StringBuffer out;
    }
}

