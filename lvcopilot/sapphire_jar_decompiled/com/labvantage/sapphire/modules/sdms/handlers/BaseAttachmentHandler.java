/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.handlers;

import com.labvantage.sapphire.BaseCustom;
import com.labvantage.sapphire.DBUtil;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.actions.sdi.BaseSDIAttributeAction;
import com.labvantage.sapphire.modules.sdms.util.ResultDataGrid;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.services.ConnectionInfo;
import com.labvantage.sapphire.services.SapphireConnection;
import com.labvantage.sapphire.util.LabVantageClassLoader;
import com.labvantage.sapphire.util.file.FileManager;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import sapphire.SapphireException;
import sapphire.attachmenthandler.HandlerType;
import sapphire.attachmenthandler.SDILink;
import sapphire.attachmenthandler.SDILinks;
import sapphire.util.ActionBlock;
import sapphire.util.DBAccess;
import sapphire.xml.PropertyList;

public abstract class BaseAttachmentHandler
extends BaseCustom {
    protected DBAccess database;
    protected ConnectionInfo connectionInfo;
    private PropertyList fields = new PropertyList();
    private ActionBlock actionBlock = null;
    private SDILinks sdilist = new SDILinks();
    private PropertyList metadataCombined = new PropertyList();
    private PropertyList metadataNew = new PropertyList();
    private List<ResultDataGrid> resultdatagrids = new ArrayList<ResultDataGrid>();
    private StringBuilder messageLog = new StringBuilder();
    private LabVantageClassLoader classLoader = null;
    private List<sapphire.attachment.Attachment> files = new ArrayList<sapphire.attachment.Attachment>();
    private long startTime = 0L;
    private DBUtil dbUtil;
    private String handlerid = null;
    private String executionId = null;
    private String sdcid = null;
    private String keyid1 = null;
    private String keyid2 = null;
    private String keyid3 = null;
    private HandlerType handlertype = HandlerType.HANDLERCLASS;
    private String handlerClassName = null;
    private boolean debugMode = false;

    protected void setDebugMode(boolean d) {
        this.debugMode = d;
    }

    protected boolean isDebugMode() {
        return this.debugMode;
    }

    protected String getExecutionId() {
        return this.executionId;
    }

    protected String getSDCId() {
        return this.sdcid;
    }

    protected String getKeyId1() {
        return this.keyid1;
    }

    protected String getKeyId2() {
        return this.keyid2;
    }

    protected String getKeyId3() {
        return this.keyid3;
    }

    protected String getAtachmentHandlerId() {
        return this.handlerid;
    }

    protected void logMessage(String message) {
        Trace.log(message);
        this.messageLog.append(message).append("\n");
    }

    protected void logWarn(String msg) {
        Trace.logWarn(msg);
        this.messageLog.append("WARN: " + msg).append("\n");
    }

    protected void logError(String msg, Throwable e) throws SapphireException {
        Trace.logError(msg);
        Trace.logError(e.getMessage(), e);
        this.messageLog.append("ERROR: " + msg).append("\n");
        this.messageLog.append("EXCEPTION: " + e.getMessage()).append("\n");
        throw new SapphireException(msg, e);
    }

    protected void logError(String msg) throws SapphireException {
        Trace.logError(msg);
        this.messageLog.append("ERROR: " + msg).append("\n");
        throw new SapphireException(msg);
    }

    protected void logDebug(String msg) {
        if (this.debugMode) {
            Trace.logDebug(msg);
            this.messageLog.append("DEBUG: " + msg).append("\n");
        }
    }

    protected void addFile(String filepath, String aliasFileName, String attachmentClass) {
        if (filepath != null && filepath.length() > 0) {
            Attachment a = new Attachment();
            if (aliasFileName == null || aliasFileName.length() == 0) {
                aliasFileName = FileManager.getFileName(filepath, true);
            }
            a.setSourceFilename(aliasFileName);
            a.setFilename(aliasFileName);
            try {
                a.setInputStream(Files.newInputStream(Paths.get(filepath, new String[0]), new OpenOption[0]));
                if (attachmentClass != null && attachmentClass.length() > 0) {
                    ((sapphire.attachment.Attachment)a).setAttachmentClass(attachmentClass);
                }
                if (this.files == null) {
                    this.files = new ArrayList<sapphire.attachment.Attachment>();
                }
                this.files.add(a);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    protected void setResultGrid(ResultDataGrid resultGrid) {
        this.resultdatagrids.add(resultGrid);
    }

    protected int getResultResultGridCount() {
        return this.resultdatagrids.size();
    }

    protected ResultDataGrid getResultGrid(int resultgridindex) {
        if (this.resultdatagrids.size() > 0 && resultgridindex < this.resultdatagrids.size()) {
            return this.resultdatagrids.get(resultgridindex);
        }
        return null;
    }

    protected ResultDataGrid getResultGrid() {
        return this.getResultGrid(0);
    }

    protected void addMetaData(String name, String value) {
        this.metadataNew.setProperty(name, value);
        this.metadataCombined.setProperty(name, value);
    }

    protected void addFileMetaData(PropertyList propertyList, sapphire.attachment.Attachment attachment) {
        try {
            BaseSDIAttributeAction.addAttachmentMetaData(propertyList, attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum(), this.getQueryProcessor(), this.getSDCProcessor(), this.getActionProcessor());
        }
        catch (Exception e) {
            this.logger.error("Failed to add attachment meta data.", e);
        }
    }

    protected void setActionBlock(ActionBlock actionBlock) {
        this.actionBlock = actionBlock;
    }

    protected void addLinkSDI(String sdcid, String keyid1, String keyid2, String keyid3) {
        if (this.sdilist.size() == 0) {
            this.sdilist.add(new SDILink(sdcid, keyid1, keyid2, keyid3));
        } else {
            boolean found = false;
            for (SDILink link : this.sdilist) {
                if (!sdcid.equalsIgnoreCase(link.getSDCId()) || !keyid1.equalsIgnoreCase(link.getKeyId1())) continue;
                if (keyid2 != null && link.getKeyId2() != null) {
                    found = true;
                    break;
                }
                if (!keyid2.equalsIgnoreCase(link.getKeyId2())) continue;
                if (keyid3 != null && link.getKeyId3() != null) {
                    found = true;
                    break;
                }
                if (!keyid3.equalsIgnoreCase(link.getKeyId3())) continue;
                found = true;
                break;
            }
            if (!found) {
                this.sdilist.add(new SDILink(sdcid, keyid1, keyid2, keyid3));
            }
        }
    }

    protected boolean isDatabaseRequired() {
        return true;
    }

    protected PropertyList getFileMetaData(sapphire.attachment.Attachment attachment) {
        try {
            return BaseSDIAttributeAction.getAttachmentMetaData(attachment.getSDCId(), attachment.getKeyId1(), attachment.getKeyId2(), attachment.getKeyId3(), attachment.getAttachmentNum(), this.getQueryProcessor(), this.getActionProcessor());
        }
        catch (Exception e) {
            this.logger.error("Failed to get attributes on attachment.\n" + e.getMessage(), e);
            return new PropertyList();
        }
    }

    protected SDILinks getLinkSDI() {
        return this.sdilist;
    }

    protected String getHandlerId() {
        return this.handlerid;
    }

    protected PropertyList getMetaData() {
        return this.metadataCombined;
    }

    protected abstract void handleData(List<sapphire.attachment.Attachment> var1, PropertyList var2) throws SapphireException;

    protected HandlerType getHandlerType() {
        return this.handlertype;
    }

    public ResultDataGrid addResultGrid() {
        ResultDataGrid g = new ResultDataGrid(this.getConnectionProcessor().getConnectionInfo(this.getConnectionId()));
        this.resultdatagrids.add(g);
        return g;
    }

    void setMessageLog(StringBuilder executionLog) {
        this.messageLog = executionLog;
    }

    StringBuilder getMessageLog() {
        return this.messageLog;
    }

    LabVantageClassLoader getHandlerClassLoader() {
        return this.classLoader;
    }

    List<sapphire.attachment.Attachment> getFiles() {
        return this.files;
    }

    ActionBlock getActionBlock() {
        if (this.actionBlock == null) {
            this.actionBlock = new ActionBlock();
        }
        return this.actionBlock;
    }

    PropertyList getMetaDataToUpdate() {
        return this.metadataNew;
    }

    void startHandle(String handlerId, String sdcid, String keyid1, String keyid2, String keyid3, HandlerType handlerType, LabVantageClassLoader classLoader, SapphireConnection sapphireConnection) {
        this.startHandle(handlerId, sdcid, keyid1, keyid2, keyid3, null, handlerType, classLoader, sapphireConnection);
    }

    void startHandle(String handlerId, String sdcid, String keyid1, String keyid2, String keyid3, String executionId, HandlerType handlerType, LabVantageClassLoader classLoader, SapphireConnection sapphireConnection) {
        this.setConnectionId(sapphireConnection.getConnectionId());
        this.classLoader = classLoader;
        this.connectionInfo = sapphireConnection;
        this.handlerid = handlerId;
        this.setHandlerType(handlerType);
        this.executionId = executionId;
        this.sdcid = sdcid;
        this.keyid1 = keyid1;
        this.keyid2 = keyid2;
        this.keyid3 = keyid3;
        if (this.isDatabaseRequired()) {
            this.dbUtil = new DBUtil(sapphireConnection.getConnectionId());
            this.dbUtil.setConnection(sapphireConnection);
            this.database = this.dbUtil;
        }
        this.logger.setLoggerName(this.getHandlerId().toUpperCase());
        this.startTime = System.currentTimeMillis();
        try {
            this.metadataCombined = BaseSDIAttributeAction.getMetaData(sdcid, keyid1, keyid2, keyid3, this.getActionProcessor());
        }
        catch (Exception e) {
            this.logger.error("Failed to load up existing meta data");
        }
    }

    void endHandle() {
        this.logger.info(new StringBuffer("End data handler: ").append(this.getHandlerId()).append(". Took ").append(System.currentTimeMillis() - this.startTime).append(" ms").toString());
        this.classLoader = null;
    }

    void setHandlerType(HandlerType handlerType) {
        this.handlertype = handlerType;
    }

    void setHandlerClass(String classname) {
        this.handlerClassName = classname;
    }

    String getHandlerClass() {
        return this.handlerClassName == null ? this.getClass().getName() : this.handlerClassName;
    }

    public String getHelperURL() {
        return "";
    }
}

