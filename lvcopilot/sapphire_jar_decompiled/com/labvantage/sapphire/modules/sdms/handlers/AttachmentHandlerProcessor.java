/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.handlers;

import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.actions.AddSDIDataCapture;
import com.labvantage.sapphire.modules.sdms.actions.ManageAttachmentOperationExecution;
import com.labvantage.sapphire.modules.sdms.handlers.BaseAttachmentHandler;
import com.labvantage.sapphire.modules.sdms.handlers.TalendJobAttachmentHandler;
import com.labvantage.sapphire.platform.Configuration;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.policy.SecurityPolicyUtil;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.file.Path;
import java.security.AccessControlContext;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.SDIProcessor;
import sapphire.action.BaseAction;
import sapphire.attachmenthandler.HandlerType;
import sapphire.attachmenthandler.SDILink;
import sapphire.attachmenthandler.SDILinks;
import sapphire.util.ActionBlock;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.util.ResultDataGrid;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class AttachmentHandlerProcessor
extends BaseAction
implements SDMSConstants {
    public static final String ID = "AttachmentHandlerProcessor";
    public static final String PROPERTY_ATTACHMENTHANDLERID = "attachmenthandlerid";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_EXECUTIONID = "execid";
    public static final String PROPERTY_ATTACHMENTNUM = "attachmentnum";
    public static final String PROPERTY_ATTACHMENTCLASS = "attachmentclass";
    public static final String PROPERTY_FILENAME = "filename";
    public static final String PROPERTY_MODE = "mode";
    public static final String PROPERTY_HANDLERRESULT = "handlerresult";
    public static final String PROPERTY_RETURNEXECUTIONLOG = "returnexecutionlog";
    public static final String RETURN_FIELDCOUNT = "fieldcount";
    private int result = 1;
    private StringBuilder executionLog = new StringBuilder();
    private Mode mode = Mode.PROCESS;

    protected void logMessage(String msg) {
        this.logger.info(msg);
        this.executionLog.append(msg).append("\n");
    }

    protected void logWarn(String msg) {
        this.logger.warn(msg);
        this.executionLog.append("WARN: " + msg).append("\n");
    }

    protected void logDebug(String msg) {
        if (this.mode == Mode.DEBUG) {
            this.logger.debug(msg);
            this.executionLog.append("DEBUG: " + msg).append("\n");
        }
    }

    @Override
    protected int setError(String msg) {
        this.logger.error(msg);
        this.executionLog.append("ERROR: ").append(msg).append("\n");
        this.result = 2;
        return this.result;
    }

    @Override
    protected int setError(String msg, Exception e) {
        return this.setError(msg, (Throwable)e);
    }

    protected int setError(String msg, Throwable e) {
        this.logger.error(msg, e);
        this.result = 2;
        this.executionLog.append("ERROR: ").append(msg).append("\n");
        if (e != null) {
            this.executionLog.append("EXCEPTION: ").append(e.toString()).append("\n");
        }
        return this.result;
    }

    public void updateSDIAttachmentOperationExecution(String sdcid, String keyid1, String keyid2, String keyid3, String executionid, String log, ActionBlock actionBlock, PropertyList fields, PropertyList handlerProps, SDILinks sdilist) throws SapphireException {
        if (sdcid.length() > 0 && keyid1.length() > 0 && executionid.length() > 0) {
            HashMap<String, Object> props = new HashMap<String, Object>();
            if (actionBlock != null && actionBlock.getActionCount() > 0) {
                props.put("actionblock", actionBlock.toJSONString());
            }
            if (fields != null && fields.size() > 0) {
                props.put("fields", fields.toJSONString());
            }
            if (handlerProps != null && handlerProps.size() > 0) {
                props.put("handlerprops", handlerProps.toJSONString());
            }
            if (sdilist != null && sdilist.size() > 0) {
                props.put("sdilist", sdilist.toJSONString());
            }
            props.put("log", log);
            props.put(PROPERTY_MODE, (Object)ManageAttachmentOperationExecution.Mode.EDIT);
            props.put(PROPERTY_SDCID, sdcid);
            props.put(PROPERTY_KEYID1, keyid1);
            props.put(PROPERTY_KEYID2, keyid2);
            props.put(PROPERTY_KEYID3, keyid3);
            props.put("executionid", executionid);
            this.getActionProcessor().processActionClass(ManageAttachmentOperationExecution.class.getName(), props, true);
        }
    }

    private String evaluate(String input, BaseAttachmentHandler bdh) {
        String output = input;
        if (bdh.getActionBlock() != null && bdh.getActionBlock().getActionCount() > 0 && input.length() > 0 && input.contains("[") && input.contains("]")) {
            String[] ts;
            for (String t : ts = StringUtil.getExpressionTokens(output)) {
                if (bdh.getActionBlock().getBlockProperties().containsKey(t)) {
                    output = StringUtil.replaceAll(output, "[" + t + "]", bdh.getActionBlock().getBlockProperty(t));
                    continue;
                }
                for (int i = 0; i < bdh.getActionBlock().getActionCount(); ++i) {
                    try {
                        HashMap ap = bdh.getActionBlock().getActionProperties(i);
                        if (!ap.containsKey(t)) continue;
                        output = StringUtil.replaceAll(output, "[" + t + "]", ap.get(t).toString());
                        continue;
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            }
        }
        return output;
    }

    private Path getAsFile(Attachment attachment) throws SapphireException {
        Path file = null;
        try {
            AttachmentProcessor ap = new AttachmentProcessor(this.getConnectionId());
            file = ap.getSDIAttachmentLocalFile(attachment, false);
        }
        catch (Exception e) {
            throw new SapphireException("Failed to get local file.", e);
        }
        return file;
    }

    public static SDIData getAttachmentHandlerData(String attachmenthandlerid, SDIProcessor sdiProcessor) {
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid("LV_AttachmentHandler");
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("attachment");
        sdiRequest.setExtendedDataTypes(true);
        sdiRequest.setKeyid1List(attachmenthandlerid);
        return sdiProcessor.getSDIData(sdiRequest);
    }

    private boolean isValidAttachment(String sdcid, String keyid1, String keyid2, String keyid3, int attachmentnum, DataSet attachments) {
        HashMap<String, BigDecimal> filter = new HashMap<String, BigDecimal>();
        filter.put(PROPERTY_ATTACHMENTNUM, new BigDecimal(attachmentnum));
        DataSet out = attachments.getFilteredDataSet(filter);
        return out.getRowCount() == 1;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     */
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        block129: {
            try {
                block132: {
                    block131: {
                        String keyid3;
                        String keyid2;
                        this.executionLog = new StringBuilder();
                        String attachmenthandlerid = propertyList.getProperty(PROPERTY_ATTACHMENTHANDLERID);
                        if (attachmenthandlerid.length() <= 0) break block132;
                        propertyList.remove(PROPERTY_ATTACHMENTHANDLERID);
                        this.mode = Mode.getMode(propertyList.getProperty(PROPERTY_MODE));
                        propertyList.remove(PROPERTY_MODE);
                        this.logDebug("Running Process Data Handler In Test mode for data handler " + attachmenthandlerid + ".");
                        String sdcid = propertyList.getProperty(PROPERTY_SDCID);
                        propertyList.remove(PROPERTY_SDCID);
                        String keyid1 = propertyList.getProperty(PROPERTY_KEYID1);
                        propertyList.remove(PROPERTY_KEYID1);
                        AttachmentProcessor attachmentProcessor = null;
                        SDIData attachmentHandlerData = null;
                        if (sdcid.length() > 0 && keyid1.length() > 0) {
                            if (propertyList.containsKey(PROPERTY_KEYID2)) {
                                keyid2 = propertyList.getProperty(PROPERTY_KEYID2);
                                propertyList.remove(PROPERTY_KEYID2);
                            } else {
                                keyid2 = null;
                            }
                            if (propertyList.containsKey(PROPERTY_KEYID3)) {
                                keyid3 = propertyList.getProperty(PROPERTY_KEYID3);
                                propertyList.remove(PROPERTY_KEYID3);
                            } else {
                                keyid3 = null;
                            }
                        } else {
                            keyid2 = null;
                            keyid3 = null;
                            this.setError("Cannot Process Data Handler as no keyid1 and/or sdcid provided.");
                        }
                        String execid = propertyList.getProperty(PROPERTY_EXECUTIONID);
                        propertyList.remove(PROPERTY_EXECUTIONID);
                        if (this.result != 1) break block129;
                        BaseAttachmentHandler bdhSetup = null;
                        try {
                            block135: {
                                block133: {
                                    block134: {
                                        LabVantageClassLoader labVantageClassLoader;
                                        SDIRequest sdiRequest = new SDIRequest();
                                        sdiRequest.setSDCid(sdcid);
                                        sdiRequest.setRequestItem("primary");
                                        sdiRequest.setRequestItem("attachment");
                                        sdiRequest.setExtendedDataTypes(true);
                                        sdiRequest.setKeyid1List(keyid1);
                                        if (keyid2 != null && keyid2.length() > 0) {
                                            sdiRequest.setKeyid2List(keyid2);
                                        }
                                        if (keyid3 != null && keyid3.length() > 0) {
                                            sdiRequest.setKeyid3List(keyid3);
                                        }
                                        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
                                        ArrayList<Attachment> attachments = new ArrayList<Attachment>();
                                        if (sdiData != null && sdiData.getDataset("primary") != null && sdiData.getDataset("primary").getRowCount() > 0) {
                                            if (sdiData.getDataset("attachment") != null && sdiData.getDataset("attachment").getRowCount() > 0) {
                                                if (propertyList.getProperty(PROPERTY_ATTACHMENTNUM).length() > 0 || propertyList.getProperty(PROPERTY_FILENAME).length() > 0 || propertyList.getProperty(PROPERTY_ATTACHMENTCLASS).length() > 0) {
                                                    String[] atnum = propertyList.getProperty(PROPERTY_ATTACHMENTNUM).length() > 0 ? StringUtil.split(propertyList.getProperty(PROPERTY_ATTACHMENTNUM), ";") : new String[]{};
                                                    propertyList.remove(PROPERTY_ATTACHMENTNUM);
                                                    for (String att : atnum) {
                                                        if (this.isValidAttachment(sdcid, keyid1, keyid2, keyid3, Integer.parseInt(att), sdiData.getDataset("attachment"))) {
                                                            Attachment attachment;
                                                            if (attachmentProcessor == null) {
                                                                attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
                                                            }
                                                            if ((attachment = attachmentProcessor.getSDIAttachment(sdcid, keyid1, keyid2 == null || keyid2.length() == 0 ? "(null)" : keyid2, keyid3 == null || keyid3.length() == 0 ? "(null)" : keyid3, Integer.parseInt(att))) == null) continue;
                                                            attachments.add(attachment);
                                                            continue;
                                                        }
                                                        this.logWarn("Failed to find an attachment for handler (1).");
                                                    }
                                                    String[] attclass = propertyList.getProperty(PROPERTY_ATTACHMENTCLASS).length() > 0 ? StringUtil.split(propertyList.getProperty(PROPERTY_ATTACHMENTCLASS), ";") : new String[]{};
                                                    propertyList.remove(PROPERTY_ATTACHMENTCLASS);
                                                    if (attclass.length > 0) {
                                                        for (String att : attclass) {
                                                            for (int a = 0; a < sdiData.getDataset("attachment").getRowCount(); ++a) {
                                                                Attachment attachment;
                                                                if (!att.equalsIgnoreCase(sdiData.getDataset("attachment").getValue(a, PROPERTY_ATTACHMENTCLASS))) continue;
                                                                int attachmentNum = sdiData.getDataset("attachment").getInt(a, PROPERTY_ATTACHMENTNUM);
                                                                if (attachmentProcessor == null) {
                                                                    attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
                                                                }
                                                                if ((attachment = attachmentProcessor.getSDIAttachment(sdcid, keyid1, keyid2 == null || keyid2.length() == 0 ? "(null)" : keyid2, keyid3 == null || keyid3.length() == 0 ? "(null)" : keyid3, attachmentNum)) != null) {
                                                                    attachments.add(attachment);
                                                                    continue;
                                                                }
                                                                this.logWarn("Failed to find an attachment for handler (2).");
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    propertyList.remove(PROPERTY_ATTACHMENTCLASS);
                                                    propertyList.remove(PROPERTY_ATTACHMENTNUM);
                                                    this.logDebug("No attachments or files provided so take all files on capture.");
                                                    for (int a = 0; a < sdiData.getDataset("attachment").getRowCount(); ++a) {
                                                        Attachment attachment;
                                                        if (attachmentProcessor == null) {
                                                            attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
                                                        }
                                                        if ((attachment = attachmentProcessor.getSDIAttachment(sdcid, keyid1, keyid2 == null || keyid2.length() == 0 ? "(null)" : keyid2, keyid3 == null || keyid3.length() == 0 ? "(null)" : keyid3, Integer.parseInt(sdiData.getDataset("attachment").getValue(a, PROPERTY_ATTACHMENTNUM, "")))) == null) continue;
                                                        attachments.add(attachment);
                                                    }
                                                }
                                            } else {
                                                this.logWarn("No files found on data capture.");
                                            }
                                        } else {
                                            this.logWarn("No data capture could be obtained to take files from.");
                                        }
                                        if (attachments == null || attachments.size() <= 0) break block133;
                                        this.logDebug("There are " + attachments.size() + " attachment(s)");
                                        if (attachmentHandlerData == null) {
                                            attachmentHandlerData = AttachmentHandlerProcessor.getAttachmentHandlerData(attachmenthandlerid, this.getSDIProcessor());
                                        }
                                        if (attachmentHandlerData == null || attachmentHandlerData.getDataset("primary") == null || attachmentHandlerData.getDataset("primary").getRowCount() <= 0) break block134;
                                        String handlerclass = attachmentHandlerData.getDataset("primary").getValue(0, "handlerclass", "");
                                        HandlerType handlerType = HandlerType.getHandlerType(attachmentHandlerData.getDataset("primary").getValue(0, "typeflag", ""));
                                        if (handlerclass.length() == 0) {
                                            this.logWarn("No handler class provided.");
                                        }
                                        String[] excludedJars = new String[]{"sapphire"};
                                        if (SecurityPolicyUtil.isJavaAttachmentsPermitted(this.getConnectionId(), LabVantageClassLoader.ClassLoaderType.ATTACHMENTHANDLER.getArea())) {
                                            labVantageClassLoader = LabVantageClassLoader.getClassLoader(LabVantageClassLoader.ClassLoaderType.ATTACHMENTHANDLER, attachmenthandlerid, attachmentHandlerData.getDataset("primary").getValue(0, "appresourceid", ""), attachmentHandlerData.getDataset("attachment"), "HandlerLibrary", excludedJars, this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                                            this.logMessage("AttachmentHandler loaded.");
                                            try {
                                                this.logMessage("Running on: " + InetAddress.getLocalHost());
                                            }
                                            catch (Exception att) {}
                                        } else {
                                            labVantageClassLoader = null;
                                            this.logDebug("Class loaders disabled in security policy.");
                                        }
                                        Configuration cf = Configuration.getInstance();
                                        if (cf != null) {
                                            this.logDebug("Attachment Handler executed by server: " + cf.getServerHostName() + "");
                                        }
                                        if (handlerType == HandlerType.TALENDJOB) {
                                            TalendJobAttachmentHandler t = null;
                                            try {
                                                Class<?> c = labVantageClassLoader != null ? labVantageClassLoader.loadClass(TalendJobAttachmentHandler.class.getName()) : Class.forName(TalendJobAttachmentHandler.class.getName());
                                                t = (TalendJobAttachmentHandler)c.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                                                t.setHandlerClass(handlerclass);
                                                bdhSetup = t;
                                            }
                                            catch (Throwable e) {
                                                this.setError("Could not load Talend Job Handler.", e);
                                                this.logger.error("Could not load talend handler class. Make sure class '" + handlerclass + "' is either attached to the handler, is on a related application resource or is in the dynamic class libaries area.", e);
                                            }
                                        } else if (handlerType == HandlerType.HANDLERCLASS) {
                                            try {
                                                Class<?> c = labVantageClassLoader != null ? labVantageClassLoader.loadClass(handlerclass) : Class.forName(handlerclass);
                                                bdhSetup = (BaseAttachmentHandler)c.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                                            }
                                            catch (Throwable e) {
                                                this.setError("Invalid class provided.", e);
                                                this.logger.error("Could not load handler class. Make sure class '" + handlerclass + "' is either attached to the handler, is on a related application resource or is in the dynamic class libaries area.", e);
                                            }
                                        } else {
                                            bdhSetup = null;
                                        }
                                        if (bdhSetup == null) {
                                            throw new SapphireException("Could not load handler.");
                                        }
                                        if (this.mode == Mode.DEBUG) {
                                            bdhSetup.setDebugMode(true);
                                        }
                                        bdhSetup.setMessageLog(this.executionLog);
                                        BaseAttachmentHandler bdh = bdhSetup;
                                        LabVantageClassLoader.executeCode(labVantageClassLoader != null ? labVantageClassLoader : this.getClass().getClassLoader(), () -> bdh.startHandle(attachmenthandlerid, sdcid, keyid1, keyid2, keyid3, execid != null && execid.length() > 0 ? execid : null, handlerType, labVantageClassLoader, this.getConnectionProcessor().getSapphireConnection()), false);
                                        try {
                                            Object return_keyid3;
                                            Object return_keyid2;
                                            Object return_keyid1;
                                            String return_sdcid;
                                            PropertyList variables;
                                            block130: {
                                                variables = new PropertyList();
                                                if (this.mode == Mode.DEBUG) {
                                                    DataSet setupVars;
                                                    DataSet dataSet = setupVars = attachmentHandlerData != null ? attachmentHandlerData.getDataset("primary") : this.getQueryProcessor().getPreparedSqlDataSet("SELECT propertyclob FROM attachmenthandler WHERE attachmenthandlerid=?", new Object[]{attachmenthandlerid});
                                                    if (setupVars != null && setupVars.size() > 0) {
                                                        try {
                                                            JSONObject properties = new JSONObject(setupVars.getClob(0, "propertyclob", "{}"));
                                                            if (!properties.has("variables")) break block130;
                                                            JSONArray jay = properties.getJSONArray("variables");
                                                            if (jay != null && jay.length() > 0) {
                                                                for (int i = 0; i < jay.length(); ++i) {
                                                                    JSONObject var = jay.getJSONObject(i);
                                                                    if (!var.has("variableid")) continue;
                                                                    String variableid = var.optString("variableid");
                                                                    String value = var.optString("value");
                                                                    variables.setProperty(variableid, value);
                                                                    this.logDebug("Setup Variable Found (from handler): " + variableid + " = " + value);
                                                                }
                                                                break block130;
                                                            }
                                                            this.logDebug("No attachment handler variables could be found in debug mode.");
                                                        }
                                                        catch (Exception e) {
                                                            this.logDebug("Warning - Failed to obtain attachment handler variables in debug mode.");
                                                        }
                                                    } else {
                                                        this.logDebug("No attachment handler variables could be obtained in debug mode.");
                                                    }
                                                }
                                            }
                                            Iterator it = propertyList.keySet().iterator();
                                            while (it.hasNext()) {
                                                String k = it.next().toString();
                                                if (!k.toLowerCase().startsWith("property_")) continue;
                                                String var = k.substring("property_".length());
                                                variables.setProperty(var, propertyList.getProperty(k, ""));
                                                this.logDebug("Setup Variable Found (passed in): " + var + " = " + propertyList.getProperty(k, ""));
                                            }
                                            if (variables.containsKey("forcelvdebug") && variables.getProperty("forcelvdebug").equalsIgnoreCase("Y")) {
                                                variables.remove("forcelvdebug");
                                                bdh.setDebugMode(true);
                                            }
                                            this.logDebug("About To Handle Data...");
                                            Permissions allowedPermissionsColl = new Permissions();
                                            ((PermissionCollection)allowedPermissionsColl).add(new RuntimePermission("accessDeclaredMembers"));
                                            AccessControlContext allowedPermissions = new AccessControlContext(new ProtectionDomain[]{new ProtectionDomain(null, allowedPermissionsColl)});
                                            LabVantageClassLoader.executeCode(labVantageClassLoader != null ? labVantageClassLoader : this.getClass().getClassLoader(), () -> bdh.handleData(attachments, variables), false);
                                            this.logDebug("...Data Handled.");
                                            if (bdh.getActionBlock() != null && bdh.getActionBlock().getActionCount() > 0) {
                                                this.logDebug("About to process " + bdh.getActionBlock().getActionCount() + " action(s).");
                                                try {
                                                    this.logDebug("About To Process Action Block...");
                                                    this.getActionProcessor().processActionBlock(bdh.getActionBlock());
                                                    this.logDebug("...Action Block Processed.");
                                                }
                                                catch (Throwable e) {
                                                    this.setError("Failed to process action block returned from handler.", e);
                                                    this.logDebug("----------------------");
                                                    this.logDebug("Failed Action Block Contents:" + bdh.getActionBlock().toJSONString());
                                                    this.logDebug("----------------------");
                                                }
                                            } else if (this.mode == Mode.DEBUG) {
                                                this.logDebug("No action block returned from Attachment handler.");
                                            }
                                            if (bdh.getResultResultGridCount() > 0) {
                                                this.logDebug("About to process " + bdh.getResultResultGridCount() + " result grid(s).");
                                                String instrumentId = "";
                                                if (sdcid.equalsIgnoreCase("LV_DataCapture")) {
                                                    DataSet ds = this.getQueryProcessor().getPreparedSqlDataSet("select instrumentid from datacapture where datacaptureid = ?", (Object[])new String[]{keyid1});
                                                    if (ds.getRowCount() > 0) {
                                                        instrumentId = ds.getValue(0, "instrumentid");
                                                    } else {
                                                        this.logDebug("DataCapture SDI cannot be retrieved.");
                                                    }
                                                }
                                                for (int i = 0; i < bdh.getResultResultGridCount(); ++i) {
                                                    if (bdh.getResultGrid(i).getRowCount() <= 0) continue;
                                                    try {
                                                        bdh.getResultGrid(i).getOptions().setApplyLock(false);
                                                        if (sdcid.equalsIgnoreCase("LV_DataCapture")) {
                                                            bdh.getResultGrid(i).setString(-1, "datacaptureid", keyid1);
                                                            if (instrumentId.length() > 0) {
                                                                bdh.getResultGrid(i).setString(-1, "instrumentid", instrumentId);
                                                            } else {
                                                                this.logDebug("DataCapture SDI does not have an Instrument.");
                                                            }
                                                        }
                                                        this.logDebug("About To Save Result Grid...");
                                                        bdh.getResultGrid(i).save();
                                                        String el = bdh.getResultGrid(i).getExecutionLog();
                                                        if (el.length() > 0) {
                                                            this.logMessage("ResultGrid Processing Log:");
                                                            this.logMessage(el);
                                                            this.logMessage("--------------------------");
                                                        }
                                                        this.logDebug("...Result Grid Saved.");
                                                        for (int r = 0; r < bdh.getResultGrid(i).getRowCount(); ++r) {
                                                            String return_sdidataitemid;
                                                            String return_sdidataid;
                                                            return_sdcid = bdh.getResultGrid(i).getValue(r, ResultDataGrid.CoreColumns.SDCID, bdh.getResultGrid(i).getOptions().getSdcId());
                                                            return_keyid1 = bdh.getResultGrid(i).getValue(r, ResultDataGrid.CoreColumns.NEWKEYID1, bdh.getResultGrid(i).getValue(r, ResultDataGrid.CoreColumns.KEYID1, ""));
                                                            return_keyid2 = "";
                                                            return_keyid3 = "";
                                                            if (return_sdcid.length() > 0 && ((String)return_keyid1).length() > 0) {
                                                                return_keyid2 = bdh.getResultGrid(i).getValue(r, ResultDataGrid.CoreColumns.KEYID2, "");
                                                                if (((String)return_keyid2).length() > 0) {
                                                                    return_keyid3 = bdh.getResultGrid(i).getValue(r, ResultDataGrid.CoreColumns.KEYID3, "");
                                                                }
                                                                bdh.addLinkSDI(return_sdcid, (String)return_keyid1, (String)return_keyid2, (String)return_keyid3);
                                                            }
                                                            if ((return_sdidataid = bdh.getResultGrid(i).getValue(r, ResultDataGrid.CoreColumns.SDIDATAID, "")).length() > 0) {
                                                                bdh.addLinkSDI("DataSet", return_sdidataid, "", "");
                                                            }
                                                            if ((return_sdidataitemid = bdh.getResultGrid(i).getValue(r, ResultDataGrid.CoreColumns.SDIDATAIIEMID, "")).length() <= 0) continue;
                                                            bdh.addLinkSDI("DataItem", return_sdidataitemid, "", "");
                                                        }
                                                        continue;
                                                    }
                                                    catch (Throwable e) {
                                                        this.setError("Failed to process returned result grid.", e);
                                                        this.logDebug("----------------------");
                                                        this.logDebug("Failed Result Grid Contents:" + bdh.getResultGrid(i).getDataSet().toJSONString());
                                                        this.logDebug("----------------------");
                                                    }
                                                }
                                            } else if (this.mode == Mode.DEBUG) {
                                                this.logDebug("No result grids returned from Attachment handler.");
                                            }
                                            if (sdcid.equalsIgnoreCase("LV_DataCapture") && (bdh.getMetaDataToUpdate() != null && bdh.getMetaDataToUpdate().size() > 0 || bdh.getLinkSDI() != null && bdh.getLinkSDI().size() > 0 && keyid1.length() > 0)) {
                                                if (bdh.getMetaDataToUpdate() != null && bdh.getMetaDataToUpdate().size() > 0) {
                                                    this.logDebug("About To Update Meta Data...");
                                                    BaseSDIAttributeAction.addMetaData(bdh.getMetaDataToUpdate(), sdcid, keyid1, keyid2, keyid3, this.getSDCProcessor(), this.getActionProcessor());
                                                    this.logDebug("...Meta Data Updated");
                                                }
                                                if (bdh.getLinkSDI() != null && bdh.getLinkSDI().size() > 0) {
                                                    M18NUtil m18n = new M18NUtil(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
                                                    ArrayList<String> sdidataitems = new ArrayList<String>();
                                                    for (SDILink sdi : bdh.getLinkSDI()) {
                                                        try {
                                                            return_sdcid = this.evaluate(sdi.getSDCId(), bdh);
                                                            return_keyid1 = sdi.getKeyId1() != null && sdi.getKeyId1().length() > 0 ? StringUtil.split(this.evaluate(sdi.getKeyId1(), bdh), ";") : new String[]{};
                                                            return_keyid2 = sdi.getKeyId2() != null && sdi.getKeyId2().length() > 0 ? StringUtil.split(this.evaluate(sdi.getKeyId2(), bdh), ";") : new String[]{};
                                                            return_keyid3 = sdi.getKeyId3() != null && sdi.getKeyId3().length() > 0 ? StringUtil.split(this.evaluate(sdi.getKeyId3(), bdh), ";") : new String[]{};
                                                            for (int i = 0; i < ((String[])return_keyid1).length; ++i) {
                                                                if (return_sdcid.equalsIgnoreCase("DataItem")) {
                                                                    sdidataitems.add(return_keyid1[i]);
                                                                    continue;
                                                                }
                                                                this.logDebug("About To Link SDI " + return_sdcid + " " + return_keyid1[i] + "...");
                                                                AddSDIDataCapture.linkSDI(return_sdcid, (String)return_keyid1[i], (String)(((String[])return_keyid2).length > i ? return_keyid2[i] : null), ((String[])return_keyid3).length > i ? return_keyid3[i] : null, keyid1, m18n, this.database, this.logger);
                                                                this.logDebug("...SDI Linked.");
                                                            }
                                                        }
                                                        catch (Exception e) {
                                                            this.setError("Failed to link sdi.", e);
                                                        }
                                                    }
                                                }
                                            }
                                            if (keyid1.length() > 0 && bdh.getFiles() != null && bdh.getFiles().size() > 0) {
                                                this.logDebug("About to process " + bdh.getFiles().size() + " returned file(s).");
                                                if (attachmentProcessor == null) {
                                                    attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
                                                }
                                                for (sapphire.attachment.Attachment a : bdh.getFiles()) {
                                                    Attachment attachment = (Attachment)a;
                                                    attachment.setSDCId("LV_DataCapture");
                                                    attachment.setKeyId1(keyid1);
                                                    if (keyid2 != null && keyid2.length() > 0) {
                                                        attachment.setKeyId2(keyid2);
                                                    }
                                                    if (keyid3 != null && keyid3.length() > 0) {
                                                        attachment.setKeyId3(keyid3);
                                                    }
                                                    try {
                                                        this.logDebug("Adding attachment '" + attachment.getSourceFilename() + "'...");
                                                        attachmentProcessor.addSDIAttachment(attachment);
                                                        this.logDebug("...Attachment added.");
                                                    }
                                                    catch (Exception exception) {}
                                                }
                                            } else if (this.mode == Mode.DEBUG) {
                                                if (keyid1.length() > 0) {
                                                    this.logDebug("No files returned from Attachment handler.");
                                                } else {
                                                    this.logDebug("No primary key used, so cannot process files.");
                                                }
                                            }
                                        }
                                        catch (Throwable e) {
                                            this.setError("Failed to process operation.", e);
                                        }
                                        try {
                                            LabVantageClassLoader.executeCode(labVantageClassLoader != null ? labVantageClassLoader : this.getClass().getClassLoader(), bdh::endHandle, false);
                                        }
                                        catch (Exception e) {
                                            this.setError("Failed to end operation.", e);
                                        }
                                        if (labVantageClassLoader != null && labVantageClassLoader instanceof LabVantageClassLoader && labVantageClassLoader.hasFailed()) {
                                            boolean error;
                                            boolean bl = error = this.result == 2;
                                            if (error) {
                                                this.setError("The ClassLoader failed to load " + labVantageClassLoader.getFailedClasses().size() + " class" + (labVantageClassLoader.getFailedClasses().size() > 1 ? "s" : "") + " required for the Attachment Handler.");
                                            } else {
                                                this.logWarn("The ClassLoader failed to load " + labVantageClassLoader.getFailedClasses().size() + " class" + (labVantageClassLoader.getFailedClasses().size() > 1 ? "s" : "") + " but was able to continue processing the Attachment Handler.");
                                            }
                                            if (error) {
                                                this.setError("Please make sure you either attach or include all the following resources:");
                                                for (String classname : labVantageClassLoader.getFailedClasses()) {
                                                    this.logMessage("\tClass: " + classname);
                                                }
                                            } else if (this.mode == Mode.DEBUG) {
                                                this.logWarn("The following missing resources caused this warning:");
                                                for (String classname : labVantageClassLoader.getFailedClasses()) {
                                                    this.logMessage("\tClass: " + classname);
                                                }
                                            }
                                        }
                                        break block135;
                                        catch (Throwable throwable) {
                                            try {
                                                LabVantageClassLoader.executeCode(labVantageClassLoader != null ? labVantageClassLoader : this.getClass().getClassLoader(), bdh::endHandle, false);
                                            }
                                            catch (Exception e) {
                                                this.setError("Failed to end operation.", e);
                                            }
                                            if (labVantageClassLoader != null && labVantageClassLoader instanceof LabVantageClassLoader && labVantageClassLoader.hasFailed()) {
                                                boolean error2;
                                                boolean bl = error2 = this.result == 2;
                                                if (error2) {
                                                    this.setError("The ClassLoader failed to load " + labVantageClassLoader.getFailedClasses().size() + " class" + (labVantageClassLoader.getFailedClasses().size() > 1 ? "s" : "") + " required for the Attachment Handler.");
                                                } else {
                                                    this.logWarn("The ClassLoader failed to load " + labVantageClassLoader.getFailedClasses().size() + " class" + (labVantageClassLoader.getFailedClasses().size() > 1 ? "s" : "") + " but was able to continue processing the Attachment Handler.");
                                                }
                                                if (error2) {
                                                    this.setError("Please make sure you either attach or include all the following resources:");
                                                    for (String classname : labVantageClassLoader.getFailedClasses()) {
                                                        this.logMessage("\tClass: " + classname);
                                                    }
                                                } else if (this.mode == Mode.DEBUG) {
                                                    this.logWarn("The following missing resources caused this warning:");
                                                    for (String classname : labVantageClassLoader.getFailedClasses()) {
                                                        this.logMessage("\tClass: " + classname);
                                                    }
                                                }
                                            }
                                            throw throwable;
                                        }
                                    }
                                    this.setError("Could not obtain data handler sdi.");
                                    break block135;
                                }
                                this.logMessage("No files found on data capture so nothing to process.");
                            }
                            if (propertyList.getProperty(PROPERTY_RETURNEXECUTIONLOG, "N").equalsIgnoreCase("Y")) {
                                propertyList.setProperty(PROPERTY_RETURNEXECUTIONLOG, this.executionLog.toString());
                                break block129;
                            }
                            if (execid.length() <= 0) break block131;
                            this.updateSDIAttachmentOperationExecution(sdcid, keyid1, keyid2, keyid3, execid, this.executionLog.toString(), bdhSetup != null ? bdhSetup.getActionBlock() : null, null, propertyList, bdhSetup != null ? bdhSetup.getLinkSDI() : null);
                        }
                        catch (Throwable throwable) {
                            if (propertyList.getProperty(PROPERTY_RETURNEXECUTIONLOG, "N").equalsIgnoreCase("Y")) {
                                propertyList.setProperty(PROPERTY_RETURNEXECUTIONLOG, this.executionLog.toString());
                            } else if (execid.length() > 0) {
                                this.updateSDIAttachmentOperationExecution(sdcid, keyid1, keyid2, keyid3, execid, this.executionLog.toString(), bdhSetup != null ? bdhSetup.getActionBlock() : null, null, propertyList, bdhSetup != null ? bdhSetup.getLinkSDI() : null);
                            } else {
                                this.logger.warn("Unable to update execution as no execution id provided.");
                            }
                            throw throwable;
                        }
                        break block129;
                    }
                    this.logger.warn("Unable to update execution as no execution id provided.");
                    break block129;
                }
                this.setError("No data handler provided.");
            }
            finally {
                try {
                    propertyList.setProperty(PROPERTY_HANDLERRESULT, "" + this.result);
                }
                catch (Exception exception) {}
            }
        }
    }

    public static enum Mode {
        PROCESS,
        DEBUG;


        public static Mode getMode(String property) {
            if (property.length() == 0) {
                return PROCESS;
            }
            if (property.length() == 1) {
                if (property.equalsIgnoreCase("D")) {
                    return DEBUG;
                }
                if (property.equalsIgnoreCase("H")) {
                    return PROCESS;
                }
                return PROCESS;
            }
            try {
                return Mode.valueOf(property.toUpperCase());
            }
            catch (Exception e) {
                return PROCESS;
            }
        }
    }
}

