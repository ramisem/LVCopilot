/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.handlers;

import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.attachmenthandler.HandlerType;
import sapphire.util.ActionBlock;
import sapphire.util.ResultGridOptions;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class TalendJobAttachmentHandler
extends BaseAttachmentHandler {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void runParseProcess(String className, List<Path> dataFiles, PropertyList properties) throws SapphireException {
        Object talendJob = null;
        Method talendJob_getContext = null;
        try {
            Class<?> classToLoad = Class.forName(className, true, this.getHandlerClassLoader());
            talendJob_getContext = classToLoad.getDeclaredMethod("getContext", new Class[0]);
            talendJob = classToLoad.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (Throwable e) {
            if (this.getHandlerClassLoader() != null) {
                if (this.getHandlerClassLoader().getLoadedClasses().contains(className)) {
                    Trace.logError("Failed to load class '" + className + "' (in loaded classes). Make sure the class is attached to the handler, added as an app resource or in the dynamic class libraries area.");
                } else if (this.getHandlerClassLoader().getFailedClasses().contains(className)) {
                    Trace.logError("Failed to load class '" + className + "' (in failed classes). Make sure the class is attached to the handler, added as an app resource or in the dynamic class libraries area.");
                } else {
                    Trace.logError("Failed to load class '" + className + "' (not in failed classes). Make sure the class is attached to the handler, added as an app resource or in the dynamic class libraries area.");
                }
            }
            this.logError("Unable to load class.", e);
        }
        if (talendJob != null) {
            try {
                String[] exec;
                String[][] output = null;
                PrintStream oldOut = System.out;
                boolean errorOccurred = false;
                try {
                    Object v;
                    PrintStream printStream = new PrintStream(new OutputStream(){

                        @Override
                        public void write(int b) throws IOException {
                            TalendJobAttachmentHandler.this.getMessageLog().append(String.valueOf((char)b));
                        }
                    });
                    System.setOut(printStream);
                    ArrayList<String> args = new ArrayList<String>();
                    args.add("--context_param connectionid=" + this.getConnectionId());
                    args.add("--context_param sdcid=" + this.getSDCId());
                    args.add("--context_param keyid1=" + this.getKeyId1());
                    args.add("--context_param keyid2=" + this.getKeyId2());
                    args.add("--context_param keyid3=" + this.getKeyId3());
                    args.add("--context_param attachmenthandlerid=" + this.getAtachmentHandlerId());
                    if (this.isDebugMode()) {
                        args.add("--context_param lvdebug=Y");
                    }
                    if (dataFiles != null && dataFiles.size() > 0) {
                        for (int p = 0; p < dataFiles.size(); ++p) {
                            this.logDebug("File '" + dataFiles.get(p).toString() + "' passed to talend (file " + (Files.exists(dataFiles.get(p), new LinkOption[0]) ? "exists" : "does not exsit") + ")");
                            args.add("--context_param fileName" + (p + 1) + "=" + StringUtil.replaceAll(dataFiles.get(p).toString(), "\\", "\\\\"));
                        }
                    }
                    if (properties != null && properties.size() > 0) {
                        for (Object k : properties.keySet()) {
                            v = properties.get(k);
                            if (!(k instanceof String) || !(v instanceof String) || ((String)v).length() <= 0) continue;
                            if (((String)k).toLowerCase().startsWith("property_")) {
                                args.add("--context_param " + k.toString() + "=" + v.toString());
                                continue;
                            }
                            args.add("--context_param property_" + k.toString() + "=" + v.toString());
                        }
                    }
                    for (Object k : this.getMetaData().keySet()) {
                        v = this.getMetaData().get(k);
                        args.add("--context_param metadata_" + k.toString() + "=" + v.toString());
                    }
                    try {
                        Object v2;
                        Method getException;
                        Class[] cArg = new Class[]{String[].class};
                        Method runJobMethod = talendJob.getClass().getMethod("runJob", cArg);
                        Object outputOb = runJobMethod.invoke(talendJob, new Object[]{args.toArray(new String[0])});
                        if (outputOb instanceof String[][]) {
                            output = (String[][])outputOb;
                        }
                        if ((getException = talendJob.getClass().getMethod("getException", new Class[0])) != null && (v2 = getException.invoke(talendJob, new Object[0])) != null && v2 instanceof Exception) {
                            errorOccurred = true;
                            this.logError("Exception thrown during execution of Talend Job.", (Exception)v2);
                        }
                        this.logDebug("Processed Job.");
                    }
                    catch (Exception e) {
                        try {
                            this.logError("Failed to run job.", e);
                        }
                        finally {
                            if (e.getCause() != null) {
                                this.logWarn("Error caused by: " + e.getCause().getMessage());
                            }
                        }
                    }
                }
                finally {
                    System.setOut(oldOut);
                }
                if (output != null && !errorOccurred) {
                    this.logDebug("Talend RunJob succeeded");
                    if (output.length > 1) {
                        this.logDebug("Talend Job returned data directly. This data will be ignored. Please use LabVantage SDMS Talend Components to return data to LabVantage.");
                    }
                    try {
                        this.logDebug("Talend output returned:");
                        this.logDebug(Arrays.deepToString((Object[])output));
                    }
                    catch (Exception e) {
                        this.logDebug("Failed to print output.");
                    }
                } else {
                    this.logError("Failed to RunJob in Talend");
                }
                Properties contextProperties = null;
                try {
                    Object result = talendJob_getContext != null ? talendJob_getContext.invoke(talendJob, new Object[0]) : null;
                    contextProperties = result != null ? (Properties)result : null;
                }
                catch (Exception e3) {
                    this.logError("Unable to get properties");
                }
                String contextActionBlock = "LabVantageAction_EXEC";
                if (contextProperties != null && contextProperties.containsKey(contextActionBlock)) {
                    String[] exec2 = StringUtil.split(contextProperties.getProperty(contextActionBlock), ";");
                    for (int i = 0; i < exec2.length; ++i) {
                        String action = exec2[i];
                        String blockcontext = StringUtil.replaceAll(contextActionBlock, "_EXEC", "_AB_") + action;
                        if (!contextProperties.containsKey(blockcontext)) continue;
                        ActionBlock ab = this.getActionBlock();
                        try {
                            String outcontext;
                            ab.setJSONObject(new JSONObject(contextProperties.getProperty(blockcontext, "")));
                            String name = ab.getActionName(0);
                            if (name != null && name.length() > 0) {
                                ab.setAction(name, ab.getActionid(0), ab.getVersionid(0), ab.getActionProperties(0));
                            }
                            if (!contextProperties.containsKey(outcontext = StringUtil.replaceAll(blockcontext, "_AB_", "_OUT_"))) continue;
                            PropertyList propertyList = new PropertyList(new JSONObject(contextProperties.getProperty(outcontext, "")));
                            HashMap ap = ab.getActionProperties(name);
                            Iterator pit = propertyList.keySet().iterator();
                            while (pit.hasNext()) {
                                String k = pit.next().toString();
                                String v = propertyList.getProperty(k);
                                if (!v.startsWith("[")) {
                                    v = "[" + v;
                                }
                                if (!v.endsWith("]")) {
                                    v = v + "]";
                                }
                                ap.put(k, v);
                            }
                            continue;
                        }
                        catch (Exception name) {
                            // empty catch block
                        }
                    }
                }
                String contextFiles = "LabVantageFileOutput_EXEC";
                if (contextProperties != null && contextProperties.containsKey(contextFiles)) {
                    String[] exec3 = StringUtil.split(contextProperties.getProperty(contextFiles), ";");
                    for (int i = 0; i < exec3.length; ++i) {
                        String attclasscontext;
                        String file = exec3[i];
                        String filepathcontext = StringUtil.replaceAll(contextFiles, "_EXEC", "_PATH_") + file;
                        if (!contextProperties.containsKey(filepathcontext)) continue;
                        String filepath = contextProperties.getProperty(filepathcontext);
                        String filename = "";
                        String attachmentclass = "";
                        String filenamecontext = StringUtil.replaceAll(contextFiles, "_EXEC", "_NAME_") + file;
                        if (contextProperties.containsKey(filenamecontext)) {
                            filename = contextProperties.getProperty(filenamecontext);
                        }
                        if (contextProperties.containsKey(attclasscontext = StringUtil.replaceAll(contextFiles, "_EXEC", "_CLASS_") + file)) {
                            attachmentclass = contextProperties.getProperty(attclasscontext);
                        }
                        if (filename.length() == 0) {
                            filename = FileManager.getFileName(filepath, true);
                        }
                        this.addFile(filepath, filename, attachmentclass);
                    }
                }
                String contextResultGrid = "LabVantageResultGrid_EXEC";
                if (contextProperties != null && contextProperties.containsKey(contextResultGrid)) {
                    String[] exec4 = StringUtil.split(contextProperties.getProperty(contextResultGrid), ";");
                    for (int i = 0; i < exec4.length; ++i) {
                        String resultgrid = exec4[i];
                        String resultgriddatacontext = StringUtil.replaceAll(contextResultGrid, "_EXEC", "_DATA_") + resultgrid;
                        if (!contextProperties.containsKey(resultgriddatacontext)) continue;
                        ResultDataGrid d = new ResultDataGrid(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                        try {
                            d.getDataSet().setJSONObject(new JSONObject(contextProperties.getProperty(resultgriddatacontext)));
                        }
                        catch (Exception attachmentclass) {
                            // empty catch block
                        }
                        if (d.getRowCount() <= 0) continue;
                        for (int r = 0; r < d.getRowCount(); ++r) {
                            for (int c = 0; c < d.getDataSet().getColumnCount(); ++c) {
                                if (d.getDataSet().getColumnType(d.getDataSet().getColumnId(c)) != 0 || !d.getDataSet().getValue(r, d.getDataSet().getColumnId(c), "").equals("null")) continue;
                                d.getDataSet().setValue(r, d.getDataSet().getColumnId(c), "");
                            }
                        }
                        String resultgridoptionscontext = StringUtil.replaceAll(contextResultGrid, "_EXEC", "_OPTIONS_") + resultgrid;
                        if (contextProperties.containsKey(resultgridoptionscontext)) {
                            try {
                                d.setOptions(new ResultGridOptions(new JSONObject(contextProperties.getProperty(resultgridoptionscontext))));
                            }
                            catch (Exception c) {
                                // empty catch block
                            }
                        }
                        this.setResultGrid(d);
                    }
                }
                String contextMetaData = "LabVantageMetaData_EXEC";
                if (contextProperties != null && contextProperties.containsKey(contextMetaData)) {
                    String[] exec5 = StringUtil.split(contextProperties.getProperty(contextMetaData), ";");
                    for (int i = 0; i < exec5.length; ++i) {
                        String action = exec5[i];
                        String propscontext = StringUtil.replaceAll(contextMetaData, "_EXEC", "_PROPS_") + action;
                        if (!contextProperties.containsKey(propscontext)) continue;
                        try {
                            PropertyList p = new PropertyList(new JSONObject(contextProperties.getProperty(propscontext, "")));
                            Iterator it = p.keySet().iterator();
                            while (it.hasNext()) {
                                String k = it.next().toString();
                                String prp = StringUtil.replaceAll(p.getProperty(k), ";", "#semicolon#");
                                this.addMetaData(k, prp);
                            }
                            continue;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
                if (this.getActionBlock() != null && this.getActionBlock().getActionCount() > 0) {
                    for (int i = 0; i < this.getActionBlock().getActionCount(); ++i) {
                        String sdcid;
                        String aname = this.getActionBlock().getActionid(i);
                        if (!aname.equalsIgnoreCase("AddSDI") || (sdcid = this.getActionBlock().getActionProperty(i, "sdcid")).length() <= 0) continue;
                        this.addLinkSDI(sdcid, "[newkeyid1]", "", "");
                    }
                }
                String contextLinkSDI = "LabVantageLinkSDI_ITEMS";
                if (contextProperties == null || !contextProperties.containsKey(contextLinkSDI)) return;
                for (String e : exec = StringUtil.split(contextProperties.getProperty(contextLinkSDI), "|")) {
                    String[] parts = StringUtil.split(e, ";");
                    if (parts.length <= 1) continue;
                    this.addLinkSDI(parts[0], parts[1], parts.length > 2 && parts[2].length() > 0 && !parts[2].equalsIgnoreCase("null") ? parts[2] : "", parts.length > 3 && parts[3].length() > 0 && !parts[3].equalsIgnoreCase("null") ? parts[3] : "");
                }
                return;
            }
            finally {
                talendJob_getContext = null;
                talendJob = null;
            }
        }
        this.logError("Failed to get talend job");
    }

    @Override
    public void setHandlerClass(String h) {
        super.setHandlerClass(h);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void handleData(List<sapphire.attachment.Attachment> attachments, PropertyList properties) throws SapphireException {
        block15: {
            if (attachments == null || attachments.size() == 0) {
                this.logWarn("No attachments provided to Talend Handler");
            }
            if (this.getHandlerType() != HandlerType.TALENDJOB) {
                this.logError("The handler is not set up as a Talend Job.");
            }
            ArrayList<Path> files = new ArrayList<Path>();
            AttachmentProcessor ap = new AttachmentProcessor(this.getConnectionId());
            for (sapphire.attachment.Attachment attachment : attachments) {
                try {
                    Path file = ap.getSDIAttachmentLocalFile((Attachment)attachment, false);
                    if (file != null) {
                        this.logDebug("File '" + file.toString() + "' obtained from attachment.");
                        this.logDebug("File " + (Files.exists(file, new LinkOption[0]) ? " exists in handler." : " does not exist in handler."));
                        files.add(file);
                        continue;
                    }
                    this.logWarn("File could not be obtained from attachment.");
                }
                catch (Exception e) {
                    throw new SapphireException("Failed to get local file.", e);
                }
            }
            if (files.size() == 0) {
                this.logWarn("No files provided to Talend Handler");
            }
            this.logMessage("Talend handler initiated.");
            try {
                if (this.getHandlerId().length() > 0) {
                    String className = this.getHandlerClass();
                    if (className.length() > 0) {
                        this.logMessage("Talend handler starting...");
                        try {
                            this.runParseProcess(className, files, properties);
                            break block15;
                        }
                        finally {
                            this.logMessage("Talend handler complete...");
                        }
                    }
                    throw new SapphireException("No class name found.");
                }
                throw new SapphireException("No handler id provided.");
            }
            finally {
                this.logMessage("Talend handler finished.");
            }
        }
    }
}

