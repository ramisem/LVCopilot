/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import com.labvantage.sapphire.admin.system.AttachmentProcessor;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.actions.CreateDataCapture;
import com.labvantage.sapphire.modules.sdms.actions.UpdateDataCapture;
import com.labvantage.sapphire.modules.sdms.handlers.AttachmentHandlerProcessor;
import com.labvantage.sapphire.services.Attachment;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SDIData;
import sapphire.util.SDIRequest;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AttachmentOperationsProcessor
extends BaseAction
implements SDMSConstants {
    public static final String ID = "AttachmentOperationsProcessor";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_STATUSCOLUMN = "statuscolumn";
    public static final String PROPERTY_STATUSSTART = "statusstart";
    public static final String PROPERTY_STATUSEND = "statusend";
    public static final String PROPERTY_MODE = "mode";
    public static final String PROPERTY_ATTACHMENTHANDLERID = "attachmenthandlerid";
    public static final String PROPERTY_ATTACHMENTCLASS = "attachmentclass";

    private void addAttachmentOperations(String attachmentHandlerId, String datacaptureid, String attachmentclass, String props) throws SapphireException {
        PropertyList actionprops = new PropertyList();
        actionprops.setProperty(PROPERTY_SDCID, "LV_DataCapture");
        actionprops.setProperty(PROPERTY_KEYID1, datacaptureid);
        actionprops.setProperty(PROPERTY_ATTACHMENTHANDLERID, attachmentHandlerId);
        actionprops.setProperty("synchronousflag", "Y");
        actionprops.setProperty("propertyclob", props);
        actionprops.setProperty(PROPERTY_ATTACHMENTCLASS, attachmentclass);
        try {
            this.getActionProcessor().processActionClass("com.labvantage.sapphire.modules.sdms.actions.AddSDIAttachmentOperation", actionprops);
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage(), e);
        }
        this.logger.info("Capture operations created on Data Capture.");
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String sdcid = propertyList.getProperty(PROPERTY_SDCID);
        String keyid1 = propertyList.getProperty(PROPERTY_KEYID1);
        String keyid2 = propertyList.getProperty(PROPERTY_KEYID2);
        String keyid3 = propertyList.getProperty(PROPERTY_KEYID3);
        Mode mode = Mode.getMode(propertyList.getProperty(PROPERTY_MODE));
        propertyList.remove(PROPERTY_MODE);
        if (mode == Mode.DEBUG) {
            String attachmenthandlerid = propertyList.getProperty(PROPERTY_ATTACHMENTHANDLERID);
            if (attachmenthandlerid.length() <= 0) throw new SapphireException("Debug mode selected but no attachment handler provided");
            this.logger.info("Running Attachment Operations Processor In Test mode for attachment handler " + attachmenthandlerid + ".");
            PropertyList props = new PropertyList();
            props.setProperty("datacapturedesc", "Created from handler " + attachmenthandlerid);
            this.getActionProcessor().processActionClass(CreateDataCapture.class.getName(), props);
            keyid1 = props.getProperty("datacaptureid");
            keyid2 = "";
            keyid3 = "";
            sdcid = "LV_DataCapture";
            propertyList.setProperty(PROPERTY_SDCID, sdcid);
            propertyList.setProperty(PROPERTY_KEYID1, keyid1);
            String attClass = propertyList.getProperty(PROPERTY_ATTACHMENTCLASS, "");
            AttachmentProcessor attachmentProcessor = new AttachmentProcessor(this.getConnectionId());
            SDIData attachmentHandlerData = AttachmentHandlerProcessor.getAttachmentHandlerData(attachmenthandlerid, this.getSDIProcessor());
            if (attachmentHandlerData != null && attachmentHandlerData.getDataset("attachment") != null) {
                for (int a = 0; a < attachmentHandlerData.getDataset("attachment").getRowCount(); ++a) {
                    Attachment handlerExampleFile;
                    if (!attachmentHandlerData.getDataset("attachment").getValue(a, PROPERTY_ATTACHMENTCLASS, "").equalsIgnoreCase("HandlerExampleFile") || (handlerExampleFile = attachmentProcessor.getSDIAttachment("LV_AttachmentHandler", attachmenthandlerid, "(null)", "(null)", attachmentHandlerData.getDataset("attachment").getInt(a, "attachmentnum", 1))) == null) continue;
                    handlerExampleFile.setSDCId("LV_DataCapture");
                    handlerExampleFile.setKeyId1(keyid1);
                    handlerExampleFile.setAttachmentClass(attClass);
                    attachmentProcessor.addSDIAttachment(handlerExampleFile);
                }
            }
            PropertyList prop = new PropertyList();
            if (mode == Mode.DEBUG) {
                prop.setProperty("debugmode", "Y");
            }
            prop.setProperty("variables", new PropertyListCollection());
            this.addAttachmentOperations(attachmenthandlerid, keyid1, attClass, prop.toJSONString());
            UpdateDataCapture.markCaptured(keyid1, "Created capture from testing handler " + attachmenthandlerid + ".", "", this.getActionProcessor());
            return;
        }
        if (sdcid.length() <= 0 || keyid1.length() <= 0) throw new SapphireException("No sdc id or keyid1 provided.");
        SDIRequest sdiRequest = new SDIRequest();
        sdiRequest.setSDCid(sdcid);
        sdiRequest.setQueryFrom(this.getSDCProcessor().getProperty(sdcid, "tableid"));
        sdiRequest.setRequestItem("primary");
        sdiRequest.setRequestItem("attachmentoperation");
        sdiRequest.setExtendedDataTypes(true);
        StringBuilder qw = new StringBuilder();
        qw.append(this.getSDCProcessor().getProperty(sdcid, "keycolid1")).append(" = '").append(keyid1).append("'");
        if (keyid2 != null && keyid2.length() > 0) {
            qw.append(this.getSDCProcessor().getProperty(sdcid, "keycolid2")).append(" = '").append(keyid2).append("'");
        }
        if (keyid3 != null && keyid3.length() > 0) {
            qw.append(this.getSDCProcessor().getProperty(sdcid, "keycolid3")).append(" = '").append(keyid3).append("'");
        }
        sdiRequest.setQueryWhere(qw.toString());
        SDIData sdiData = this.getSDIProcessor().getSDIData(sdiRequest);
        if (sdiData == null || sdiData.getDataset("primary") == null || sdiData.getDataset("primary").getRowCount() <= 0) throw new SapphireException("Could not obtain primary record.");
        DataSet captureops = sdiData.getDataset("attachmentoperation");
        if (captureops != null && captureops.getRowCount() > 0) {
            String status = propertyList.getProperty(PROPERTY_STATUSSTART);
            String statuscolumn = propertyList.getProperty(PROPERTY_STATUSCOLUMN);
            if (status.length() > 0 && statuscolumn.length() > 0) {
                PropertyList edit = new PropertyList();
                edit.setProperty(PROPERTY_SDCID, sdcid);
                edit.setProperty(PROPERTY_KEYID1, keyid1);
                if (keyid2.length() > 0) {
                    edit.setProperty(PROPERTY_KEYID2, keyid2);
                }
                if (keyid3.length() > 0) {
                    edit.setProperty(PROPERTY_KEYID3, keyid3);
                }
                edit.setProperty(statuscolumn, status);
                this.getActionProcessor().processAction("EditSDI", "1", edit, true);
            }
            captureops.sort("usersequence");
            SafeSQL safeSQL = new SafeSQL();
            try {
                this.database.executePreparedUpdate("UPDATE sdiattachmentoperation SET operationstatus=" + safeSQL.addVar("ready") + " WHERE sdcid=" + safeSQL.addVar(sdcid) + " AND keyid1=" + safeSQL.addVar(keyid1) + (keyid2.length() > 0 ? " AND keyid2=" + safeSQL.addVar(keyid2) : "") + (keyid3.length() > 0 ? " AND keyid3=" + safeSQL.addVar(keyid3) : ""), safeSQL.getValues());
            }
            catch (Exception e) {
                this.logger.error("Failed to update operation status to ready.");
                throw new SapphireException(e);
            }
            for (int r = 0; r < captureops.getRowCount(); ++r) {
                String attachmentoperationid = captureops.getValue(r, "attachmentoperationid", "");
                if (attachmentoperationid.length() > 0) {
                    boolean sync = !captureops.getValue(r, "synchronousflag", "Y").equalsIgnoreCase("N");
                    String processinggroupname = captureops.getValue(r, "processinggroupname", "");
                    try {
                        if (processinggroupname.length() > 0) {
                            PropertyList props = new PropertyList();
                            props.setProperty("actionid", "AttachmentOperationProcessor");
                            props.setProperty("actionversionid", "1");
                            props.setProperty("groupname", processinggroupname);
                            props.setProperty("grouperrorrule", sync ? "S" : "C");
                            props.setProperty(PROPERTY_SDCID, sdcid);
                            props.setProperty(PROPERTY_KEYID1, keyid1);
                            props.setProperty(PROPERTY_KEYID2, keyid2);
                            props.setProperty(PROPERTY_KEYID3, keyid3);
                            props.setProperty("attachmentoperationid", attachmentoperationid);
                            props.setProperty(PROPERTY_STATUSCOLUMN, statuscolumn);
                            props.setProperty(PROPERTY_STATUSEND, propertyList.getProperty(PROPERTY_STATUSEND));
                            this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props, false);
                            continue;
                        }
                        PropertyList properties = new PropertyList();
                        properties.setProperty(PROPERTY_SDCID, sdcid);
                        properties.setProperty(PROPERTY_KEYID1, keyid1);
                        properties.setProperty(PROPERTY_KEYID2, keyid2);
                        properties.setProperty(PROPERTY_KEYID3, keyid3);
                        properties.setProperty("attachmentoperationid", attachmentoperationid);
                        properties.setProperty(PROPERTY_STATUSCOLUMN, statuscolumn);
                        properties.setProperty(PROPERTY_STATUSEND, propertyList.getProperty(PROPERTY_STATUSEND));
                        this.getActionProcessor().processAction("AttachmentOperationProcessor", "1", properties, false, !sync);
                        continue;
                    }
                    catch (Exception e) {
                        throw new SapphireException("Handler failed", e);
                    }
                }
                this.logger.warn("No data handler found for operation " + r + ".");
            }
            return;
        }
        this.logger.info("No operations to process.");
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

