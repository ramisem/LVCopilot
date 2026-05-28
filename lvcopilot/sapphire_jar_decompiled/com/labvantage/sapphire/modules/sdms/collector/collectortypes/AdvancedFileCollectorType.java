/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.io.FileUtils
 *  org.apache.commons.io.FilenameUtils
 */
package com.labvantage.sapphire.modules.sdms.collector.collectortypes;

import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import com.labvantage.sapphire.util.format.DateFormatter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import sapphire.SapphireException;
import sapphire.ext.BaseCollectorType;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AdvancedFileCollectorType
extends BaseCollectorType {
    protected String instrumentRootFolder = null;
    protected boolean isCollectionEnabled = false;
    protected boolean isDeliveryEnbabled = false;
    protected boolean isEmulatorEnabled = false;
    private boolean firstTriggerCheck = true;
    protected int collectorPollInterval;
    protected int emulatorPollInterval;
    protected String triggerType;
    protected Path triggerRoot;
    protected String triggerSubFolderChecking = "Root";
    protected String triggerSubFolderMatcher = "";
    protected String triggerGlobFileMatcher = "";
    protected String triggerGlobFolderMatcher = "";
    protected int triggerCountExceeds;
    protected int triggerFileSize;
    protected String triggerTextInFile;
    protected String waitType = "None";
    protected int waitSeconds;
    protected int waitFileLockInitial;
    protected long waitFileLockTimeout;
    protected Path deliveryRoot;
    protected boolean deliveryFileOverwrite = false;
    PropertyListCollection collectConfigCollection;
    protected String emulatorFilename;
    protected String emulatorMode = "";
    protected String emulatorFileContent = "";
    protected String emulatorDefaultFileContent = "I am some results.";
    protected String emulatorFilenameMask = "";
    protected long emulatorWritingTimeInEachRun;
    protected Path emulatorRoot;
    protected boolean emulatorUseTemplateFile;
    protected boolean emulatorCreateSubFolder;
    protected String emulatorSubfolderNameFormat = "";
    protected Path emulatorTemplateFile;
    protected long emulatorMaxFileSize;
    protected boolean junitMode = false;
    private String lastStoreDescription = "";
    List<Path> currentTriggerSearchFolders = new ArrayList<Path>();
    List<Path> foundTriggerFiles = new ArrayList<Path>();
    List<Path> foundTriggerFolders = new ArrayList<Path>();
    Path lastRunFileDelivered = null;
    Calendar lastTriggerDt = null;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void configure(PropertyList collectorTypeProps) throws SapphireException {
        File check;
        this.junitMode = collectorTypeProps.getProperty("junitmode").equals("Y");
        this.instrumentRootFolder = collectorTypeProps.getProperty("instrumentremoteroot");
        this.instrumentRootFolder = StringUtil.replaceAll(this.instrumentRootFolder, "\\", "/");
        String msg = "";
        this.isCollectionEnabled = collectorTypeProps.getProperty("enablecollection").equals("Y");
        this.isDeliveryEnbabled = collectorTypeProps.getProperty("enablerunfiledelivery").equals("Y");
        this.isEmulatorEnabled = collectorTypeProps.getProperty("enableemulator").equals("Y");
        if (this.instrumentRootFolder.length() > 0 && (this.isCollectionEnabled || this.isDeliveryEnbabled || this.isEmulatorEnabled) && !(check = new File(this.instrumentRootFolder)).exists()) {
            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Root folder could not be located", "Root folder " + this.instrumentRootFolder + " could not be located for " + this.getInstrumentid(), false, true);
        }
        if (this.isCollectionEnabled) {
            PropertyList collectorProps = collectorTypeProps.getPropertyListNotNull("collectorprops");
            PropertyList trigger = collectorProps.getPropertyListNotNull("trigger");
            this.collectorPollInterval = Integer.parseInt(trigger.getProperty("triggerpollintervalseconds", "" + this.getDefaultInstrumentPollInterval()));
            this.triggerType = trigger.getProperty("triggertype", "FileCreated");
            String triggerRootType = trigger.getProperty("triggerroottype", "Instrument");
            try {
                String relativeToRoot = trigger.getProperty("relativetoroot");
                String otherPath = trigger.getProperty("otherrootpath");
                this.triggerRoot = this.getTriggerRoot(triggerRootType, relativeToRoot, otherPath);
            }
            catch (SapphireException e) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure trigger path", "Failed to configure trigger path: " + e.getMessage(), false, true);
            }
            if (this.triggerRoot == null || !this.triggerRoot.toFile().exists()) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to locate trigger root folder", "Failed to locate trigger root folder " + this.triggerRoot == null ? "" : this.triggerRoot.toFile().getAbsolutePath(), false, true);
            }
            this.triggerSubFolderChecking = trigger.getProperty("subfolderchecking", "Root");
            if (!this.triggerSubFolderChecking.equals("Root")) {
                this.triggerSubFolderMatcher = trigger.getProperty("subfoldermatcher", "*");
                this.triggerSubFolderMatcher = StringUtil.replaceAll(this.triggerSubFolderMatcher, "\\", "/");
                if (this.triggerSubFolderMatcher.length() == 0) {
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Sub-Folder Matcher not defined", "Sub-Folder Matcher not defined", false, true);
                }
            }
            if (this.triggerType.equals("FolderCreated")) {
                this.triggerGlobFolderMatcher = trigger.getProperty("globfoldernamematcher");
                this.triggerGlobFolderMatcher = StringUtil.replaceAll(this.triggerGlobFolderMatcher, "\\", "/");
                if (this.triggerGlobFolderMatcher.length() == 0) {
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Folder Matching definition not defined", "Folder Matching definition not defined", false, true);
                }
            } else {
                this.triggerGlobFileMatcher = trigger.getProperty("globfilenamematcher");
                this.triggerGlobFileMatcher = StringUtil.replaceAll(this.triggerGlobFileMatcher, "\\", "/");
                if (this.triggerGlobFileMatcher.length() == 0) {
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "File Matching definition not defined", "File Matching definition not defined", false, true);
                }
            }
            this.triggerCountExceeds = this.triggerType.equalsIgnoreCase("FileCount") ? Integer.parseInt(trigger.getProperty("countexceeds", "0")) : 0;
            this.triggerFileSize = this.triggerType.equalsIgnoreCase("FileSizeAny") || this.triggerType.equalsIgnoreCase("FileSizeTotal") ? Integer.parseInt(trigger.getProperty("filesize", "0")) : 0;
            this.triggerTextInFile = trigger.getProperty("triggertextinfile", "");
            if (this.triggerType.equals("TextAppearInFile") && this.triggerTextInFile.length() == 0) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Missing search text", "No search text has been defined", false, true);
            }
            PropertyList wait = collectorProps.getPropertyListNotNull("wait");
            this.waitType = wait.getProperty("waittype");
            this.waitSeconds = this.waitType.equalsIgnoreCase("Time") ? Integer.parseInt(wait.getProperty("waitseconds", "5")) : 5;
            this.waitFileLockInitial = this.waitType.equalsIgnoreCase("FilesUnlocked") ? Integer.parseInt(wait.getProperty("filewaitinitial", "2")) : 2;
            this.waitFileLockTimeout = Long.parseLong(wait.getProperty("filewaittimeout", "60")) * 1000L;
            this.collectConfigCollection = collectorProps.getCollection("collect");
            for (int i = 0; i < this.collectConfigCollection.size(); ++i) {
                PropertyList collectionConfig = this.collectConfigCollection.getPropertyList(i);
                String collectType = collectionConfig.getProperty("collecttype");
                if (!this.triggerType.equals("FolderCreated") || !collectType.equals("TriggerFile") && !collectType.equals("TriggerFileSiblings")) continue;
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Illegal collect type", "You cannot collect off trigger files when triggering on a folder creation", false, true);
            }
        }
        if (this.isDeliveryEnbabled) {
            boolean created;
            PropertyList deliveryProps = collectorTypeProps.getPropertyListNotNull("deliveryprops");
            String deliveryRootType = deliveryProps.getProperty("deliveryglobpathtype", "Instrument");
            try {
                String relativeToRoot = deliveryProps.getProperty("relativetoroot");
                String otherPath = deliveryProps.getProperty("otherrootpath");
                this.deliveryRoot = this.getTriggerRoot(deliveryRootType, relativeToRoot, otherPath);
            }
            catch (SapphireException e) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure delivery path", "Failed to configure delivery path: " + e.getMessage(), false, true);
            }
            if (this.deliveryRoot == null) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to locate delivery folder", "Failed to locate delivery folder", false, true);
            }
            if (!this.deliveryRoot.toFile().exists() && !(created = this.deliveryRoot.toFile().mkdirs())) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to create delivery folder", "Failed to create delivery folder " + this.deliveryRoot.toFile().getAbsolutePath(), false, true);
            }
            this.logStartup("Delivery RunFile Folder Path = " + this.deliveryRoot.toFile().getAbsolutePath());
            this.deliveryFileOverwrite = deliveryProps.getProperty("fileexistsbehavior").equalsIgnoreCase("Overwrite");
            this.logStartup("Delivery File Overwrite = " + this.deliveryFileOverwrite);
        }
        if (this.isEmulatorEnabled) {
            boolean created;
            PropertyList emulatorProps = collectorTypeProps.getPropertyListNotNull("emulatorprops");
            this.emulatorPollInterval = Integer.parseInt(emulatorProps.getProperty("frequency", "" + this.getDefaultInstrumentPollInterval()));
            int delta = Integer.parseInt(emulatorProps.getProperty("randomdelta", "0"));
            if (delta > 0) {
                int maximum = this.emulatorPollInterval + delta;
                int minimum = this.emulatorPollInterval - delta;
                this.emulatorPollInterval = SDMSUtil.getRandomInteger(maximum, minimum);
            }
            this.emulatorMode = emulatorProps.getProperty("filecreationmode");
            this.emulatorFilename = emulatorProps.getProperty("filename");
            this.emulatorFileContent = emulatorProps.getProperty("filecontent");
            this.emulatorFilenameMask = emulatorProps.getProperty("filenamemask", "count");
            this.emulatorWritingTimeInEachRun = Long.parseLong(emulatorProps.getProperty("writingtimeineachrun", "10")) * 1000L;
            this.emulatorMaxFileSize = Long.parseLong(emulatorProps.getProperty("maxfilesize", "1000"));
            PropertyList outputfolderProps = emulatorProps.getPropertyListNotNull("outputfolder");
            String emulatorRootType = outputfolderProps.getProperty("outputfolderpathtype", "Instrument");
            try {
                String relativeToRoot = outputfolderProps.getProperty("relativetoroot");
                String otherPath = outputfolderProps.getProperty("otherrootpath");
                this.emulatorRoot = this.getTriggerRoot(emulatorRootType, relativeToRoot, otherPath);
            }
            catch (SapphireException e) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure emulator path", "Failed to configure emulator path: " + e.getMessage(), false, true);
            }
            if (this.emulatorRoot == null) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to locate emulator folder", "Failed to locate emulator folder", false, true);
            }
            if (!this.emulatorRoot.toFile().exists() && !(created = this.emulatorRoot.toFile().mkdirs())) {
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to create emulator folder", "Failed to create emulator folder " + this.deliveryRoot.toFile().getAbsolutePath(), false, true);
            }
            this.emulatorCreateSubFolder = "Y".equalsIgnoreCase(outputfolderProps.getProperty("createsubfolder"));
            this.emulatorSubfolderNameFormat = outputfolderProps.getProperty("subfoldernameformat");
            PropertyList templateProps = emulatorProps.getPropertyListNotNull("template");
            this.emulatorUseTemplateFile = "Y".equalsIgnoreCase(templateProps.getProperty("usetemplate"));
            if (this.emulatorUseTemplateFile) {
                Path templateRoot = null;
                String templateRootType = templateProps.getProperty("templatefolderpathtype", "Instrument");
                try {
                    String relativeToRoot = templateProps.getProperty("relativetoroot");
                    String otherPath = templateProps.getProperty("otherrootpath");
                    templateRoot = this.getTriggerRoot(templateRootType, relativeToRoot, otherPath);
                }
                catch (SapphireException e) {
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure template path", "Failed to configure template path: " + e.getMessage(), false, true);
                }
                if (templateRoot == null) {
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to locate emulator folder", "Failed to locate emulator folder", false, true);
                }
                this.emulatorTemplateFile = templateRoot.resolve(templateProps.getProperty("templatefile"));
                if (this.emulatorTemplateFile.toFile().isDirectory() || !this.emulatorTemplateFile.toFile().exists()) {
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to locate template file", "Failed to locate template file " + this.emulatorTemplateFile.toFile().getAbsolutePath(), false, true);
                }
                if ("zip".equalsIgnoreCase(FilenameUtils.getExtension((String)this.emulatorTemplateFile.toFile().getName())) && this.emulatorFilename.length() > 0) {
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Emulator File name " + this.emulatorFilename + " is not required when template file is a zip file", "Emulator File name " + this.emulatorFilename + " is not required when template file is a zip file", false, true);
                }
            }
            this.logStartup("Emulator generating files every " + this.emulatorPollInterval + "s into emulator root " + this.emulatorRoot.toFile().getAbsolutePath());
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private Path getTriggerRoot(String triggerRootType, String relativeToRoot, String otherPath) throws SapphireException {
        Path output = null;
        if (triggerRootType.equals("Instrument")) {
            return Paths.get(this.instrumentRootFolder, new String[0]);
        }
        if (triggerRootType.equals("Relative")) {
            if (relativeToRoot.length() <= 0) throw new SapphireException("Relative to Root folder not defined");
            return Paths.get(this.instrumentRootFolder, new String[0]).resolve(relativeToRoot);
        }
        if (!triggerRootType.equals("Other")) return output;
        if (otherPath.length() <= 0) throw new SapphireException("Other Root folder not defined");
        otherPath = StringUtil.replaceAll(otherPath, "\\", "/");
        return Paths.get(otherPath, new String[0]);
    }

    @Override
    public int getCollectionPollInterval() {
        return this.collectorPollInterval;
    }

    @Override
    public int getEmulatorPollInterval() {
        return this.emulatorPollInterval;
    }

    @Override
    public boolean isCollectionEnabled() {
        return this.isCollectionEnabled;
    }

    @Override
    public boolean isRunfileDeliveryEnabled() {
        return this.isDeliveryEnbabled;
    }

    @Override
    public boolean isEmulatorEnabled() {
        return this.isEmulatorEnabled;
    }

    public List<Path> getCurrentTriggerSearchFolders() {
        return this.currentTriggerSearchFolders;
    }

    public List<Path> getFoundTriggerFiles() {
        return this.foundTriggerFiles;
    }

    public List<Path> getFoundTriggerFolders() {
        return this.foundTriggerFolders;
    }

    @Override
    public boolean doRunCollector(FileSenderFactory fileSenderFactory) throws Exception {
        boolean captureFiles;
        boolean triggered = this.isCollectionTriggered();
        if (triggered && (captureFiles = this.doWait())) {
            this.startCollectionLog("Trigger for " + this.instrumentid + " detected");
            PropertyList captureMetaData = new PropertyList();
            this.lastTriggerDt = Calendar.getInstance();
            BaseFileSender fileSender = fileSenderFactory.getInstance(this.sdmsCollector, this);
            String sendId = fileSender.init(this.lastTriggerDt, captureMetaData);
            this.lastStoreDescription = "";
            this.collectFiles(fileSender, sendId);
        }
        return triggered;
    }

    /*
     * WARNING - void declaration
     */
    protected boolean isCollectionTriggered() throws SapphireException {
        this.foundTriggerFiles.clear();
        this.foundTriggerFolders.clear();
        if (this.triggerRoot == null) {
            return false;
        }
        try {
            this.currentTriggerSearchFolders = this.getTriggerOrCollectSourcePaths(this.triggerRoot, this.triggerSubFolderChecking, this.triggerSubFolderMatcher);
        }
        catch (IOException e) {
            this.raiseInstrumentAlert("SDMS Collection", "Failure", "Unable to find sub-folders.", "Unable to find sub Folders: " + e.getMessage(), false, false);
            return false;
        }
        if (this.firstTriggerCheck) {
            this.logger.log("COLLECTING", "First trigger check details:");
            for (Path path : this.currentTriggerSearchFolders) {
                this.logger.log("COLLECTING", " > Checking in " + path.toFile().getAbsolutePath());
            }
            this.firstTriggerCheck = false;
        }
        boolean isCollectionTriggered = false;
        for (Path triggerPath : this.currentTriggerSearchFolders) {
            String erromsg = "";
            String description = "Failed to determine file size";
            ArrayList<Path> matched = null;
            try {
                matched = this.triggerType.equals("FolderCreated") ? SDMSUtil.getMatchingDirectories(triggerPath, this.triggerGlobFolderMatcher) : SDMSUtil.getMatchingFiles(triggerPath, this.triggerGlobFileMatcher);
            }
            catch (IOException e) {
                this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to get Matching Directories", "Failed to get Matching Directories.Reason:" + e.getMessage(), false, false);
            }
            if (matched == null || matched.size() == 0) continue;
            if (this.triggerType.equals("FileCreated")) {
                this.foundTriggerFiles.addAll(matched);
                isCollectionTriggered = true;
            } else if (this.triggerType.equals("FolderCreated")) {
                this.foundTriggerFolders.addAll(matched);
                isCollectionTriggered = true;
            } else if (this.triggerType.equals("FileCount")) {
                this.foundTriggerFiles.addAll(matched);
            } else if (this.triggerType.equals("FileSizeAny")) {
                for (Path path : matched) {
                    try {
                        long size = Files.size(path) / 1024L;
                        if (size <= (long)this.triggerFileSize) continue;
                        this.foundTriggerFiles.add(path);
                        isCollectionTriggered = true;
                        break;
                    }
                    catch (IOException e) {
                        erromsg = "Failed to determine file size " + path;
                    }
                }
            } else if (this.triggerType.equals("FileSizeTotal")) {
                this.foundTriggerFiles.addAll(matched);
            } else if (this.triggerType.equals("FileSizeChanged")) {
                ArrayList<String> filesizes = new ArrayList<String>();
                PropertyList runtime = this.getRuntimeProperties();
                for (Object k : runtime.keySet()) {
                    if (!((String)k).startsWith("filesize:")) continue;
                    filesizes.add((String)k);
                }
                for (Path path : matched) {
                    filesizes.remove(this.getFilesizeRuntimeKey(path));
                    try {
                        String lastFileSize = this.getRuntimeProperty(this.getFilesizeRuntimeKey(path));
                        if (lastFileSize.length() > 0) {
                            if (Files.size(path) == Long.parseLong(lastFileSize)) continue;
                            this.foundTriggerFiles.add(path);
                            isCollectionTriggered = true;
                            this.setRuntimeProperty(this.getFilesizeRuntimeKey(path), "");
                            continue;
                        }
                        this.setRuntimeProperty(this.getFilesizeRuntimeKey(path), "" + Files.size(path));
                    }
                    catch (IOException e) {
                        erromsg = "Failed to determine file size " + path;
                    }
                }
                for (String string : filesizes) {
                    runtime.remove(string);
                }
            } else if (this.triggerType.equals("FileModified")) {
                Long lastFileModifiedTriggerCheck = Long.parseLong(this.getRuntimeProperty("lastFileModifiedTriggerCheck", "0"));
                Calendar lastFileModifiedTriggerCheckDate = Calendar.getInstance();
                lastFileModifiedTriggerCheckDate.setTimeInMillis(lastFileModifiedTriggerCheck);
                Calendar lastModifiedTime = null;
                String string = this.getRuntimeProperty("filealreadycaptured", "");
                if (lastFileModifiedTriggerCheck == 0L) {
                    this.setRuntimeProperty("lastFileModifiedTriggerCheck", "" + Calendar.getInstance().getTimeInMillis());
                } else {
                    void var10_26;
                    for (Path path : matched) {
                        try {
                            BasicFileAttributeView basicView = Files.getFileAttributeView(path, BasicFileAttributeView.class, new LinkOption[0]);
                            BasicFileAttributes basicAttr = basicView.readAttributes();
                            lastModifiedTime = Calendar.getInstance();
                            lastModifiedTime.setTimeInMillis(basicAttr.lastModifiedTime().toMillis());
                            if (!lastModifiedTime.equals(lastFileModifiedTriggerCheckDate) && !lastModifiedTime.after(lastFileModifiedTriggerCheckDate)) continue;
                            if (lastModifiedTime.equals(lastFileModifiedTriggerCheckDate)) {
                                if (var10_26.contains(path.toString())) continue;
                                String string2 = (String)var10_26 + ";" + path.toString();
                                this.foundTriggerFiles.add(path);
                                isCollectionTriggered = true;
                                continue;
                            }
                            this.foundTriggerFiles.add(path);
                            isCollectionTriggered = true;
                            String string3 = path.toString();
                            lastFileModifiedTriggerCheckDate = lastModifiedTime;
                        }
                        catch (Exception e) {
                            erromsg = "Failed to determine file modified time " + path;
                            description = "Failed to determine file modified time";
                        }
                    }
                    if (isCollectionTriggered) {
                        this.setRuntimeProperty("lastFileModifiedTriggerCheck", "" + lastFileModifiedTriggerCheckDate.getTimeInMillis());
                        this.setRuntimeProperty("filealreadycaptured", "" + (String)var10_26);
                    }
                }
            } else if (this.triggerType.equals("TextAppearInFile")) {
                for (Path path : matched) {
                    try {
                        String content = new String(Files.readAllBytes(path));
                        if (!content.contains(this.triggerTextInFile)) continue;
                        this.foundTriggerFiles.add(path);
                        isCollectionTriggered = true;
                    }
                    catch (Exception e) {
                        this.logger.log("COLLECTING", "Failed to read contents of file " + path.toFile().getAbsolutePath() + ". Skipping this file");
                    }
                }
            }
            if (erromsg.length() <= 0) continue;
            erromsg = erromsg + " for" + this.getInstrumentid();
            this.raiseInstrumentAlert("SDMS Startup", "Failure", description, erromsg, false, true);
        }
        if (this.triggerType.equals("FileCount")) {
            isCollectionTriggered = this.foundTriggerFiles.size() >= this.triggerCountExceeds;
        } else if (this.triggerType.equals("FileSizeTotal")) {
            long l = 0L;
            for (Path path : this.foundTriggerFiles) {
                try {
                    l += Files.size(path);
                }
                catch (IOException e) {
                    this.raiseInstrumentAlert("SDMS Startup", "Warning", "Failed to determine file size", "Failed to determine file size for " + path.toFile().getAbsolutePath() + ". Skipping file.", false, true);
                }
            }
            if (l > (long)(this.triggerFileSize * 1024)) {
                isCollectionTriggered = true;
            }
        }
        if (!isCollectionTriggered) {
            this.foundTriggerFiles.clear();
            this.foundTriggerFolders.clear();
        }
        return isCollectionTriggered;
    }

    protected ArrayList<Path> getTriggerOrCollectSourcePaths(Path triggerRoot, String triggerSubFolderChecking, String triggerSubFolderMatcher) throws IOException {
        ArrayList<Path> searchInTheseFolders = new ArrayList<Path>();
        if (triggerSubFolderChecking.equals("Root") || triggerSubFolderChecking.equals("Both")) {
            searchInTheseFolders.add(triggerRoot);
        }
        if (!triggerSubFolderChecking.equals("Root") && triggerSubFolderMatcher.length() > 0) {
            ArrayList<Path> matching = SDMSUtil.getMatchingDirectories(triggerRoot, triggerSubFolderMatcher);
            if (triggerSubFolderChecking.equals("LeafFolders")) {
                ArrayList<Path> matchingTemp = new ArrayList<Path>();
                matchingTemp.addAll(matching);
                for (Path path : matchingTemp) {
                    matching.remove(path.getParent());
                }
                matchingTemp.clear();
                matchingTemp.addAll(matching);
                for (Path path : matchingTemp) {
                    File[] directories = path.toFile().listFiles(file -> file.isDirectory());
                    if (directories == null || directories.length <= 0) continue;
                    matching.remove(path);
                }
                searchInTheseFolders.addAll(matching);
            } else {
                searchInTheseFolders.addAll(matching);
            }
        }
        return searchInTheseFolders;
    }

    private boolean doWait() throws SapphireException, InterruptedException {
        boolean captureFiles = false;
        if (this.waitType.equals("None")) {
            captureFiles = true;
        } else if (this.waitType.equals("Time")) {
            this.appendCollectionLog("Waiting for " + this.waitSeconds + "s before collecting");
            try {
                Thread.sleep(this.waitSeconds * 1000);
                captureFiles = true;
            }
            catch (InterruptedException e) {
                this.raiseInstrumentAlert("SDMS Collection", "Warning", "Triggered Wait Time Interrupted", "The Waiting thread waiting for " + this.waitSeconds + "s was Interrupted.", false, false);
            }
        } else if (this.waitType.equals("FilesUnlocked")) {
            boolean locked;
            if (this.waitFileLockInitial > 0) {
                this.appendCollectionLog("Waiting for " + this.waitFileLockInitial + " before checking lock status");
                try {
                    Thread.sleep(this.waitFileLockInitial * 1000);
                }
                catch (InterruptedException e) {
                    this.raiseInstrumentAlert("SDMS Collection", "Warning", "Triggered Wait Time Interrupted", "The Waiting thread waiting for " + this.waitSeconds + "s was Interrupted.", false, false);
                }
            }
            boolean firstLocked = true;
            long startTime = Calendar.getInstance().getTimeInMillis();
            do {
                if (!(locked = FileTransfer.isLocked(this.foundTriggerFiles))) continue;
                Thread.sleep(500L);
                if (!firstLocked) continue;
                this.appendCollectionLog("The triggered file is in locked state. Will keep checking every 1/2 second for " + this.waitFileLockTimeout / 1000L + "s");
                firstLocked = false;
            } while (locked && System.currentTimeMillis() - startTime < this.waitFileLockTimeout);
            if (locked) {
                this.raiseInstrumentAlert("SDMS Collection", "Warning", "Failed to collect due to locked files", "One or more trigger files was still locked after the timeout period", false, false);
                captureFiles = false;
            } else {
                captureFiles = true;
            }
        }
        return captureFiles;
    }

    private String getFilesizeRuntimeKey(Path path) {
        return "filesize:" + path.toFile().getName() + "_" + path.toFile().getAbsolutePath().hashCode();
    }

    private void collectFiles(BaseFileSender fileSender, String sendId) throws Exception {
        for (int i = 0; i < this.collectConfigCollection.size(); ++i) {
            PropertyList collectionConfig = this.collectConfigCollection.getPropertyList(i);
            String collectType = collectionConfig.getProperty("collecttype");
            String collectFilenameMatcher = collectionConfig.getProperty("globfilenamematcher");
            String excludeFilenameMatcher = collectionConfig.getProperty("excludefilenamematcher");
            if (excludeFilenameMatcher.length() > 0 && collectFilenameMatcher.length() == 0) {
                collectFilenameMatcher = "*.*";
            }
            String collectModifier = collectionConfig.getProperty("countmodifier", "A");
            BaseFileSender.ActionOnOrginal processOriginalMode = collectionConfig.getProperty("actiononoriginal", "Delete").equals("Delete") ? BaseFileSender.ActionOnOrginal.DELETE : (collectionConfig.getProperty("actiononoriginal", "Delete").equals("Archive") ? BaseFileSender.ActionOnOrginal.ARCHIVE : BaseFileSender.ActionOnOrginal.LEAVE);
            Path archiveFolder = null;
            String archiveFolderAsSeenFromLIMS = null;
            if (processOriginalMode == BaseFileSender.ActionOnOrginal.ARCHIVE) {
                String archiveFolderStr = collectionConfig.getProperty("archivefolder", "");
                archiveFolder = archiveFolderStr.length() > 0 ? Paths.get(archiveFolderStr, new String[0]) : null;
                archiveFolderAsSeenFromLIMS = collectionConfig.getProperty("archivefolderseenfromlims", "");
                if (archiveFolder == null) {
                    this.raiseInstrumentAlert("SDMS Collection", "Failure", "No archive folder provided", "Archive mode selected and no archive folder provided", false, true);
                } else if (!Files.exists(archiveFolder, new LinkOption[0])) {
                    this.raiseInstrumentAlert("SDMS Collection", "Failure", "Archive folder does not exist:" + archiveFolder, "Archive mode selected and archive folder does not exist:" + archiveFolder, false, true);
                }
            }
            String attachmentClass = collectionConfig.getProperty("attachmentclass");
            boolean attachByReference = collectionConfig.getProperty("attachbyreference", "N").equalsIgnoreCase("Y");
            if (attachByReference && processOriginalMode == BaseFileSender.ActionOnOrginal.DELETE) {
                this.raiseInstrumentAlert("SDMS Collection", "Failure", "Reference attachment on Delete action", "Reference attachments cannot be used with Delete action on orginal", false, true);
            }
            String zipFileName = collectionConfig.getProperty("zipfilename");
            boolean zipMultiFiles = "Y".equalsIgnoreCase(collectionConfig.getProperty("zipmultiplefiles", "N"));
            if (collectType.equals("TriggerFile")) {
                if (this.foundTriggerFiles.size() <= 0) continue;
                if (!collectModifier.equals("A")) {
                    this.filterByCountModifier(this.foundTriggerFiles, collectModifier);
                }
                if (zipMultiFiles && this.foundTriggerFiles.size() > 0) {
                    this.appendCollectionLog("Collecting and zipping mulitple trigger files");
                    this.zipMultipleFilesAndStore(fileSender, sendId, this.foundTriggerFiles, zipFileName, attachmentClass, attachByReference, archiveFolder, archiveFolderAsSeenFromLIMS, processOriginalMode);
                    continue;
                }
                for (Path triggerFile : this.foundTriggerFiles) {
                    try {
                        this.appendCollectionLog("Collecting trigger file " + triggerFile.toFile().getCanonicalPath());
                        this.lastStoreDescription = this.lastStoreDescription + fileSender.store(sendId, triggerFile, processOriginalMode, attachmentClass, attachByReference, archiveFolder, archiveFolderAsSeenFromLIMS, new PropertyList());
                    }
                    catch (Exception exception) {
                        this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to store data", "Failed to send the file to LIMS: " + exception.getMessage(), false, false);
                    }
                }
                if (processOriginalMode != BaseFileSender.ActionOnOrginal.DELETE) continue;
                for (Path currentTriggerFolder : this.currentTriggerSearchFolders) {
                    if (currentTriggerFolder.equals(this.triggerRoot)) continue;
                    FileUtils.deleteQuietly((File)currentTriggerFolder.toFile());
                }
                continue;
            }
            if (collectType.equals("TriggerFileSiblings")) {
                HashSet<Path> parentFolders = new HashSet<Path>();
                for (Path path : this.foundTriggerFiles) {
                    parentFolders.add(path.getParent());
                }
                ArrayList<Path> allMatching = new ArrayList<Path>();
                for (Path folder : parentFolders) {
                    try {
                        allMatching.addAll(SDMSUtil.getMatchingFiles(folder, collectFilenameMatcher, excludeFilenameMatcher));
                    }
                    catch (IOException e) {
                        this.raiseInstrumentAlert("SDMS Collection", "Warning", "Failed to get all trigger file siblings", "Failed to get all trigger file siblings: " + e.getMessage(), false, false);
                    }
                }
                if (allMatching.size() <= 0) continue;
                if (!collectModifier.equals("A")) {
                    this.filterByCountModifier(allMatching, collectModifier);
                }
                if (zipMultiFiles && allMatching.size() > 1) {
                    this.appendCollectionLog("Collecting and zipping mulitple trigger sibling files");
                    this.zipMultipleFilesAndStore(fileSender, sendId, allMatching, zipFileName, attachmentClass, attachByReference, archiveFolder, archiveFolderAsSeenFromLIMS, processOriginalMode);
                    continue;
                }
                for (Path matchingFile : allMatching) {
                    try {
                        this.appendCollectionLog("Collecting trigger file sibling " + matchingFile.toFile().getCanonicalPath());
                        this.lastStoreDescription = this.lastStoreDescription + fileSender.store(sendId, matchingFile, processOriginalMode, attachmentClass, attachByReference, archiveFolder, archiveFolderAsSeenFromLIMS, new PropertyList());
                    }
                    catch (Exception e) {
                        this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to store data", "Failed to send the file to LIMS: " + e.getMessage(), false, false);
                    }
                }
                continue;
            }
            if (collectType.equals("TriggerFolder")) {
                HashSet<Path> collectFolders = new HashSet<Path>();
                if (this.foundTriggerFiles.size() > 0) {
                    for (Path path : this.foundTriggerFiles) {
                        collectFolders.add(path.getParent());
                    }
                } else {
                    collectFolders.addAll(this.foundTriggerFolders);
                }
                if (collectFolders.size() <= 0) continue;
                if (zipFileName.length() == 0 || !zipFileName.endsWith(".zip")) {
                    zipFileName = "folder.zip";
                }
                boolean includeSubfolders = "Y".equalsIgnoreCase(collectionConfig.getProperty("includesubfolders"));
                Path path = Files.createTempDirectory("collectorzip", new FileAttribute[0]);
                int count = 0;
                for (Path triggerFolder : collectFolders) {
                    this.appendCollectionLog("Collecting and zipping trigger folder " + triggerFolder.toFile().getCanonicalPath());
                    String workingZipFilename = count == 0 ? zipFileName : StringUtil.replaceAll(zipFileName, ".zip", "_" + count + ".zip");
                    ++count;
                    Path zipFile = path.resolve(workingZipFilename);
                    try {
                        SDMSUtil.folderToZip(triggerFolder.toFile(), includeSubfolders, collectFilenameMatcher, excludeFilenameMatcher, zipFile.toFile());
                        try {
                            this.lastStoreDescription = this.lastStoreDescription + fileSender.store(sendId, zipFile, processOriginalMode, attachmentClass, false, null, null, new PropertyList());
                            if (this.lastStoreDescription != null && this.lastStoreDescription.trim().length() > 0 && processOriginalMode == BaseFileSender.ActionOnOrginal.DELETE) {
                                FileUtils.deleteQuietly((File)triggerFolder.toFile());
                            }
                        }
                        catch (Exception e) {
                            this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to zip and store data", "Failed to zip and send the file to LIMS: " + e.getMessage(), false, false);
                        }
                    }
                    catch (Exception e2) {
                        this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to zip folder", "Failed to zip file prior to storage: " + e2.getMessage(), false, false);
                    }
                    if (this.junitMode) continue;
                    try {
                        zipFile.toFile().delete();
                    }
                    catch (Exception e2) {}
                }
                if (this.junitMode) continue;
                try {
                    path.toFile().delete();
                }
                catch (Exception e) {}
                continue;
            }
            if (collectType.equals("FilesMatching")) {
                boolean doCollection = true;
                Path collectRoot = null;
                String string = collectionConfig.getProperty("collectroottype", "Instrument");
                if (string.equals("Instrument")) {
                    collectRoot = Paths.get(this.instrumentRootFolder, new String[0]);
                } else if (string.equals("Relative")) {
                    String relativeToRoot = collectionConfig.getProperty("relativetoroot");
                    if (relativeToRoot.length() > 0) {
                        collectRoot = Paths.get(this.instrumentRootFolder, new String[0]).resolve(relativeToRoot);
                    } else {
                        this.appendCollectionLog("Relative to Root folder not defined - skipping this collection item");
                        doCollection = false;
                    }
                } else if (string.equals("Other")) {
                    String otherRootPath = collectionConfig.getProperty("otherrootpath");
                    if (otherRootPath.length() > 0) {
                        otherRootPath = StringUtil.replaceAll(otherRootPath, "\\", "/");
                        collectRoot = Paths.get(otherRootPath, new String[0]);
                    } else {
                        this.appendCollectionLog("Other Root folder not defined - skipping this collection item");
                        doCollection = false;
                    }
                }
                String subFolderMatcher = "";
                String subFolderChecking = collectionConfig.getProperty("subfolderchecking", "Root");
                if (!subFolderChecking.equals("Root")) {
                    subFolderMatcher = collectionConfig.getProperty("subfoldermatcher", "*");
                    if ((subFolderMatcher = StringUtil.replaceAll(subFolderMatcher, "\\", "/")).length() == 0) {
                        this.appendCollectionLog("Other Root folder not defined - skipping this collection item");
                        doCollection = false;
                    }
                }
                if (!doCollection || collectRoot == null || !collectRoot.toFile().exists()) continue;
                ArrayList<Path> allMatching = new ArrayList<Path>();
                ArrayList<Path> sourcePaths = this.getTriggerOrCollectSourcePaths(collectRoot, subFolderChecking, subFolderMatcher);
                if (sourcePaths.size() > 0) {
                    for (Path path : sourcePaths) {
                        try {
                            ArrayList<Path> allMatchingFiles = SDMSUtil.getMatchingFiles(path, collectFilenameMatcher, excludeFilenameMatcher);
                            for (Path filePath : allMatchingFiles) {
                                boolean isLocked = FileTransfer.isLocked(filePath.toFile());
                                if (isLocked) continue;
                                allMatching.add(filePath);
                            }
                        }
                        catch (IOException e) {
                            this.appendCollectionLog("Failed to get matching files - skipping this collection item: " + e.getMessage());
                        }
                    }
                }
                if (allMatching.size() <= 0) continue;
                if (!collectModifier.equals("A")) {
                    this.filterByCountModifier(allMatching, collectModifier);
                }
                if (zipMultiFiles && allMatching.size() > 0) {
                    this.appendCollectionLog("Collecting and zipping mulitple matching files");
                    this.zipMultipleFilesAndStore(fileSender, sendId, allMatching, zipFileName, attachmentClass, attachByReference, archiveFolder, archiveFolderAsSeenFromLIMS, processOriginalMode);
                    continue;
                }
                for (Path matchingFile : allMatching) {
                    try {
                        this.appendCollectionLog("Collecting matching file " + matchingFile.toFile().getCanonicalPath());
                        this.lastStoreDescription = this.lastStoreDescription + fileSender.store(sendId, matchingFile, processOriginalMode, attachmentClass, attachByReference, archiveFolder, archiveFolderAsSeenFromLIMS, new PropertyList());
                    }
                    catch (Exception e) {
                        this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to store data", "Failed to send the file to LIMS: " + e.getMessage(), false, false);
                    }
                }
                continue;
            }
            if (!collectType.equals("RunFile") || this.lastRunFileDelivered == null) continue;
            try {
                this.appendCollectionLog("Collecting run file " + this.lastRunFileDelivered.toFile().getCanonicalPath());
                this.lastStoreDescription = this.lastStoreDescription + fileSender.store(sendId, this.lastRunFileDelivered, processOriginalMode, attachmentClass, attachByReference, archiveFolder, archiveFolderAsSeenFromLIMS, new PropertyList());
                continue;
            }
            catch (Exception e) {
                this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to store data", "Failed to send the file to LIMS: " + e.getMessage(), false, false);
            }
        }
        fileSender.complete(sendId);
    }

    private void filterByCountModifier(List<Path> list, String collectModifier) {
        if (collectModifier.equals("O")) {
            list.sort(new Comparator<Path>(){

                @Override
                public int compare(Path o1, Path o2) {
                    return Long.compare(o1.toFile().lastModified(), o2.toFile().lastModified());
                }
            });
        } else if (collectModifier.equals("N")) {
            list.sort(new Comparator<Path>(){

                @Override
                public int compare(Path o2, Path o1) {
                    return Long.compare(o1.toFile().lastModified(), o2.toFile().lastModified());
                }
            });
        }
        int size = list.size();
        for (int i = 1; i < size; ++i) {
            list.remove(1);
        }
    }

    @Override
    public boolean doRunEmulator() throws SapphireException {
        this.startEmulatorLog("");
        boolean emulated = false;
        Path tempDestinationFolder = this.emulatorRoot;
        if (this.emulatorCreateSubFolder) {
            this.appendEmulatorLog("To create subfolder....");
            if (this.emulatorSubfolderNameFormat.length() == 0) {
                this.emulatorSubfolderNameFormat = "dd-mmm-yy";
            }
            Calendar cal = Calendar.getInstance();
            String subfolderName = DateFormatter.formatDateTime(cal, this.emulatorSubfolderNameFormat);
            tempDestinationFolder = this.emulatorRoot.resolve(subfolderName);
            this.appendEmulatorLog("Destination Folder where emulator file to be created " + tempDestinationFolder);
            tempDestinationFolder.toFile().mkdirs();
        }
        Path file = this.getNewFile(tempDestinationFolder);
        if ("Simple".equals(this.emulatorMode) || "AppendToFile".equals(this.emulatorMode)) {
            if (this.emulatorUseTemplateFile && "zip".equalsIgnoreCase(FilenameUtils.getExtension((String)this.emulatorTemplateFile.toFile().getName()))) {
                boolean targetFolderMissingZipContent = FileUtil.isAnyZipFileContentMissingInFolder(this.emulatorTemplateFile.toFile(), file);
                if (targetFolderMissingZipContent) {
                    emulated = true;
                    this.createFileFromTemplateFile(file);
                }
            } else if (!file.toFile().exists()) {
                emulated = true;
                if (this.emulatorUseTemplateFile) {
                    this.createFileFromTemplateFile(file);
                } else {
                    FileTransferOptions fto = new FileTransferOptions();
                    fto.setCloseInputStream(true);
                    fto.setCloseOutputStream(true);
                    fto.setReplaceTarget(true);
                    try {
                        if (this.emulatorFileContent.startsWith("data:") && this.emulatorFileContent.contains(";base64,")) {
                            FileManager.FileData fileData = new FileManager.FileData(this.emulatorFileContent);
                            FileTransfer.safeDataTransfer(fileData.getInputStream(), file.toFile(), fto);
                        } else {
                            FileTransfer.safeDataTransfer((InputStream)new ByteArrayInputStream(this.emulatorFileContent.getBytes("UTF8")), file.toFile(), fto);
                        }
                        this.appendEmulatorLog("File created for instrument " + this.instrumentid + "  is: " + file.toAbsolutePath());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if ("AppendToFile".equals(this.emulatorMode)) {
                emulated = true;
                try {
                    Files.write(file, Arrays.asList(this.emulatorFileContent.length() > 0 ? this.emulatorFileContent : " appended text"), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                }
                catch (IOException e) {
                    this.appendEmulatorLog("Failed to append text to file. Error: " + e.getMessage());
                }
            }
        } else if ("Log".equals(this.emulatorMode)) {
            emulated = true;
            long startTime = Calendar.getInstance().getTimeInMillis();
            long timeElapsed = 0L;
            String content = this.emulatorFileContent.length() > 0 ? this.emulatorFileContent : this.emulatorDefaultFileContent;
            try {
                FileOutputStream objFOS = new FileOutputStream(file.toFile());
                DataOutputStream objDOS = new DataOutputStream(objFOS);
                boolean fileClosed = false;
                do {
                    int fileSize;
                    if (fileClosed) {
                        file = this.getNewFile(tempDestinationFolder);
                        this.appendEmulatorLog("Creating new file:" + file.getFileName());
                        objFOS = new FileOutputStream(file.toFile());
                        objDOS = new DataOutputStream(objFOS);
                        fileClosed = false;
                    }
                    if ((fileSize = objDOS.size()) > 0) {
                        int bytesLentoWrite = new String("\n" + content).getBytes().length;
                        if ((long)(fileSize + bytesLentoWrite) <= this.emulatorMaxFileSize) {
                            objDOS.writeBytes("\n" + content);
                            continue;
                        }
                        objDOS.close();
                        fileClosed = true;
                        this.appendEmulatorLog("Total " + fileSize + " bytes are written to File:" + file.getFileName() + ". ");
                        continue;
                    }
                    objDOS.writeBytes(content);
                } while ((timeElapsed = Calendar.getInstance().getTimeInMillis() - startTime) < this.emulatorWritingTimeInEachRun);
                this.appendEmulatorLog("Log file creation time taken( in seconds ): " + timeElapsed / 1000L);
                objDOS.close();
            }
            catch (Exception e) {
                this.appendEmulatorLog("Exception occurred while creating files in 'Log' mode." + e.getMessage());
            }
        }
        return emulated;
    }

    private void zipMultipleFilesAndStore(BaseFileSender fileSender, String sendId, List<Path> files, String zipFileName, String attachmentClass, boolean byReference, Path archiveFolder, String archiveFolderAsSeenFromLIMS, BaseFileSender.ActionOnOrginal processOriginalMode) throws Exception {
        HashSet<String> names = new HashSet<String>();
        ArrayList<Path> distinctFiles = new ArrayList<Path>();
        for (Path file : files) {
            if (names.contains(file.toFile().getName())) {
                this.appendCollectionLog("Skipped file " + file.toFile().getAbsolutePath() + " because a file with the same name has already been added to the zip");
                continue;
            }
            names.add(file.toFile().getName());
            distinctFiles.add(file);
        }
        if (zipFileName.contains("[foldername]")) {
            String commonFolder = FileUtil.getCommonParentFolder(distinctFiles);
            if (commonFolder.length() > 0) {
                if (!(zipFileName = StringUtil.replaceAll(zipFileName, "[foldername]", commonFolder)).endsWith(".zip")) {
                    zipFileName = zipFileName + ".zip";
                }
            } else {
                zipFileName = "multiplefiles.zip";
            }
        } else if (zipFileName.length() == 0 || !zipFileName.endsWith(".zip")) {
            zipFileName = "multiplefiles.zip";
        }
        Path tempDir = Files.createTempDirectory("collectorzip", new FileAttribute[0]);
        Path zipFile = tempDir.resolve(zipFileName);
        try {
            FileTransferOptions options = new FileTransferOptions();
            options.setReplaceTarget(true);
            int cnt = 0;
            for (Path file : distinctFiles) {
                FileTransfer.safeFileTransfer(file.toFile(), zipFile.toFile(), cnt == 0 ? options : null);
                ++cnt;
            }
            try {
                this.lastStoreDescription = this.lastStoreDescription + fileSender.zipAndStore(sendId, zipFile, processOriginalMode, attachmentClass, byReference, archiveFolder, archiveFolderAsSeenFromLIMS, new PropertyList(), files);
            }
            catch (Exception e) {
                this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to zip and store data", "Failed to zip and send the file to LIMS.", false, false);
            }
        }
        catch (Exception e2) {
            this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to zip files", "Failed to zip file prior to storage.", false, false);
        }
        if (!this.junitMode) {
            try {
                zipFile.toFile().delete();
                tempDir.toFile().delete();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    public Calendar getLastCaptureDt() {
        return this.lastTriggerDt;
    }

    @Override
    public String getLastCaptureDescription() {
        String description = "No recent capture detected";
        if (this.foundTriggerFolders.size() > 0) {
            description = "Triggered by " + this.foundTriggerFolders.get(0).toFile().getAbsolutePath();
            if (this.foundTriggerFolders.size() > 1) {
                description = description + " and " + (this.foundTriggerFolders.size() - 1) + " more.";
            }
        } else if (this.foundTriggerFiles.size() > 0) {
            description = "Triggered by " + this.foundTriggerFiles.get(0).toFile().getAbsolutePath();
            if (this.foundTriggerFiles.size() > 1) {
                description = description + " and " + (this.foundTriggerFiles.size() - 1) + " more.";
            }
        }
        return description;
    }

    @Override
    public String getLastStoreDescription() {
        return this.lastStoreDescription == null ? "None" : this.lastStoreDescription;
    }

    private Path getNewFile(Path dir) {
        String filename = "";
        filename = this.emulatorFilename;
        Path file = dir.resolve(filename);
        int fileCtr = 0;
        if (file.toFile().exists() && "Log".equals(this.emulatorMode)) {
            this.appendEmulatorLog("File name already exists. Resolving file name again using latest file name in the directory and the file name mask.");
            String emuFileNameWithoutExt = this.emulatorFilename.substring(0, this.emulatorFilename.indexOf("."));
            String ext = this.emulatorFilename.substring(this.emulatorFilename.indexOf("."));
            if ("Count".equalsIgnoreCase(this.emulatorFilenameMask)) {
                String latestfileCtr = "";
                File latestFile = SDMSUtil.getLatestFilefromDir(dir.toFile().getAbsolutePath());
                String latestFileName = latestFile.getName();
                if (latestFileName.length() > 0 && latestFileName.indexOf("_") > -1) {
                    latestfileCtr = latestFileName.substring(latestFileName.lastIndexOf("_") + 1, latestFileName.indexOf("."));
                }
                if (latestfileCtr.length() == 0) {
                    fileCtr = 0;
                } else {
                    try {
                        fileCtr = Integer.parseInt(latestfileCtr);
                    }
                    catch (NumberFormatException ne) {
                        fileCtr = 0;
                    }
                }
                filename = emuFileNameWithoutExt + "_" + ++fileCtr + ext;
            } else if ("CurrentTimeStamp".equalsIgnoreCase(this.emulatorFilenameMask)) {
                String timeStamp = "" + Calendar.getInstance().getTimeInMillis();
                filename = emuFileNameWithoutExt + "_" + timeStamp + ext;
            }
            file = dir.resolve(filename);
        }
        return file;
    }

    private void createFileFromTemplateFile(Path file) throws SapphireException {
        try {
            FileTransferOptions options = new FileTransferOptions();
            options.setReplaceTarget(true);
            FileTransfer.safeFileTransfer(this.emulatorTemplateFile.toFile(), file.toFile(), options);
            this.appendEmulatorLog("File  created copying template file " + this.emulatorTemplateFile.toFile().getName() + "  is: " + file.toAbsolutePath());
        }
        catch (Exception e) {
            throw new SapphireException("Failed to copy template file: " + e.getMessage(), e);
        }
    }

    @Override
    public String doDeliverRunFile(String filename, byte[] bytes) throws SapphireException {
        String message = "";
        File targetFile = this.deliveryRoot.resolve(filename).toFile();
        if (targetFile.exists()) {
            if (this.deliveryFileOverwrite) {
                targetFile.delete();
            } else {
                throw new SapphireException("RunFile " + filename + " already exists on the target.");
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(targetFile);
            fos.write(bytes);
            fos.close();
            message = "RunFile " + filename + " has been successfully delivered.";
            this.lastRunFileDelivered = targetFile.toPath();
        }
        catch (Exception e) {
            throw new SapphireException("Failed to write to RunFile " + targetFile.getAbsolutePath() + ": " + e.getMessage());
        }
        finally {
            try {
                fos.close();
            }
            catch (IOException iOException) {}
        }
        return message;
    }

    @Override
    public List<String> getReportsForSDC(PropertyList collectorTypeProps, String sdcid) {
        ArrayList<String> reports = new ArrayList<String>();
        PropertyList deliveryProps = collectorTypeProps.getPropertyListNotNull("deliveryprops");
        PropertyListCollection reportRules = deliveryProps.getCollectionNotNull("reportrules");
        for (int i = 0; i < reportRules.size(); ++i) {
            PropertyList report = reportRules.getPropertyList(i);
            if (!report.getProperty("sdcid", sdcid).equals(sdcid)) continue;
            reports.add(report.getProperty("reportid") + ";" + report.getProperty("reportversionid", "C"));
        }
        return reports;
    }
}

