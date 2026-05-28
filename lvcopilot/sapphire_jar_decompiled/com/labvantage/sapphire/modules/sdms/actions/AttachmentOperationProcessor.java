/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.actions.automation.AddToDoListEntry;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.actions.ManageAttachmentOperationExecution;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.QueryProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.SafeSQL;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class AttachmentOperationProcessor
extends BaseAction
implements SDMSConstants {
    public static final String ID = "AttachmentOperationProcessor";
    public static final String PROPERTY_SDCID = "sdcid";
    public static final String PROPERTY_KEYID1 = "keyid1";
    public static final String PROPERTY_KEYID2 = "keyid2";
    public static final String PROPERTY_KEYID3 = "keyid3";
    public static final String PROPERTY_ATTACHMENTOPERATIONID = "attachmentoperationid";
    public static final String PROPERTY_STATUSCOLUMN = "statuscolumn";
    public static final String PROPERTY_STATUSEND = "statusend";
    public static final String PROPERTY_RERUNEXECUTIONID = "rerunexecutionid";
    public static final String PROPERTY_MODE = "mode";

    public String addSDIAttachmentOperationExecution(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentoperationid) throws SapphireException {
        return this.addSDIAttachmentOperationExecution(sdcid, keyid1, keyid2, keyid3, attachmentoperationid);
    }

    public String addSDIAttachmentOperationExecution(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentoperationid, PropertyList handlerProps) throws SapphireException {
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(PROPERTY_MODE, (Object)ManageAttachmentOperationExecution.Mode.ADD);
        props.put(PROPERTY_SDCID, sdcid);
        props.put(PROPERTY_KEYID1, keyid1);
        props.put(PROPERTY_KEYID2, keyid2);
        props.put(PROPERTY_KEYID3, keyid3);
        props.put(PROPERTY_ATTACHMENTOPERATIONID, attachmentoperationid);
        if (handlerProps != null && handlerProps.size() > 0) {
            props.put("handlerprops", handlerProps.toJSONString());
        }
        this.getActionProcessor().processActionClass(ManageAttachmentOperationExecution.class.getName(), props, true);
        return props.containsKey("executionid") ? props.get("executionid").toString() : "";
    }

    public void logExecutionFailure(String sdcid, String keyid1, String keyid2, String keyid3, String executionid, String msg, boolean raiseAlert) throws SapphireException {
        if (sdcid.length() > 0 && keyid1.length() > 0 && executionid.length() > 0) {
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put(PROPERTY_MODE, (Object)ManageAttachmentOperationExecution.Mode.FAILURE);
            props.put(PROPERTY_SDCID, sdcid);
            props.put(PROPERTY_KEYID1, keyid1);
            props.put(PROPERTY_KEYID2, keyid2);
            props.put(PROPERTY_KEYID3, keyid3);
            props.put("executionid", executionid);
            props.put("message", msg);
            props.put("raisealert", raiseAlert ? "Y" : "N");
            this.getActionProcessor().processActionClass(ManageAttachmentOperationExecution.class.getName(), props, true);
        }
    }

    public void logExecutionSuccess(String sdcid, String keyid1, String keyid2, String keyid3, String executionid) throws SapphireException {
        if (sdcid.length() > 0 && keyid1.length() > 0 && executionid.length() > 0) {
            HashMap<String, Object> props = new HashMap<String, Object>();
            props.put(PROPERTY_MODE, (Object)ManageAttachmentOperationExecution.Mode.SUCCESS);
            props.put(PROPERTY_SDCID, sdcid);
            props.put(PROPERTY_KEYID1, keyid1);
            props.put(PROPERTY_KEYID2, keyid2);
            props.put(PROPERTY_KEYID3, keyid3);
            props.put("executionid", executionid);
            this.getActionProcessor().processActionClass(ManageAttachmentOperationExecution.class.getName(), props, true);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void rerunExecution(String executionId, Mode mode) throws SapphireException {
        QueryProcessor queryProcessor = this.getQueryProcessor();
        DataSet execution = queryProcessor.getPreparedSqlDataSet("SELECT * FROM sdiattachmentoperationexec WHERE executionid=?", new Object[]{executionId}, true);
        if (execution == null || execution.size() <= 0) throw new SapphireException("Could not find single operation row.");
        String operationid = execution.getValue(0, PROPERTY_ATTACHMENTOPERATIONID, "");
        PropertyList handlerProps = null;
        try {
            handlerProps = new PropertyList(new JSONObject(execution.getClob(0, "handlerproperties", "")));
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (handlerProps == null) throw new SapphireException("Failed to find previous handler properties");
        DataSet captureops = queryProcessor.getPreparedSqlDataSet("SELECT * FROM sdiattachmentoperation WHERE attachmentoperationid=? and sdcid=? AND keyid1=? AND keyid2=? AND keyid3=?", new Object[]{operationid, execution.getValue(0, PROPERTY_SDCID), execution.getValue(0, PROPERTY_KEYID1), execution.getValue(0, PROPERTY_KEYID2), execution.getValue(0, PROPERTY_KEYID3)}, true);
        if (captureops != null && captureops.size() == 1) {
            String sdcid = captureops.getValue(0, PROPERTY_SDCID, "");
            String keyid1 = captureops.getValue(0, PROPERTY_KEYID1, "");
            String keyid2 = captureops.getValue(0, PROPERTY_KEYID2, "");
            String keyid3 = captureops.getValue(0, PROPERTY_KEYID3, "");
            String update = "";
            SafeSQL safeSQLU = new SafeSQL();
            update = keyid2 != null && keyid2.length() > 0 ? (keyid3 != null && keyid3.length() > 0 ? "UPDATE sdiattachmentoperation SET operationstatus=" + safeSQLU.addVar("Processing") + " WHERE attachmentoperationid=" + safeSQLU.addVar(operationid) + " AND sdcid=" + safeSQLU.addVar("LV_DataCapture") + " AND keyid1=" + safeSQLU.addVar(keyid1) + "AND keyid2=" + safeSQLU.addVar(keyid2) + " AND keyid3=" + safeSQLU.addVar(keyid3) : "UPDATE sdiattachmentoperation SET operationstatus=" + safeSQLU.addVar("Processing") + " WHERE attachmentoperationid=" + safeSQLU.addVar(operationid) + " AND sdcid=" + safeSQLU.addVar("LV_DataCapture") + " AND keyid1=" + safeSQLU.addVar(keyid1) + " AND keyid2=" + safeSQLU.addVar(keyid2)) : "UPDATE sdiattachmentoperation SET operationstatus=" + safeSQLU.addVar("Processing") + " WHERE attachmentoperationid=" + safeSQLU.addVar(operationid) + " AND sdcid=" + safeSQLU.addVar("LV_DataCapture") + " AND keyid1=" + safeSQLU.addVar(keyid1);
            this.processOperation(sdcid, keyid1, keyid2, keyid3, operationid, captureops, update, safeSQLU, handlerProps, mode);
            return;
        } else {
            this.logger.info("Operation " + operationid + " skipped as not ready.");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processOperation(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentoperationid, DataSet captureops, String updateSql, SafeSQL safeSQLU, PropertyList baseProps, Mode mode) throws SapphireException {
        block19: {
            block18: {
                SafeSQL safeSQL;
                Mode retM;
                String attachmenthandlerid = captureops.getValue(0, "operationkeyid1", "");
                if (attachmenthandlerid.length() <= 0) break block18;
                String attachmentclass = captureops.getValue(0, "attachmentclass", "");
                try {
                    this.database.executePreparedUpdate(updateSql, safeSQLU.getValues());
                }
                catch (Exception e) {
                    this.logger.error("Failed to update operation status to processing.");
                }
                PropertyList properties = baseProps == null ? new PropertyList() : baseProps;
                String xmlprops = captureops.getClob(0, "propertyclob");
                if (xmlprops != null && xmlprops.length() > 0 && (retM = this.mergeAttachmentOperationsProperties(properties, xmlprops)) == Mode.DEBUG && mode != Mode.DEBUG) {
                    mode = retM;
                }
                properties.setProperty(PROPERTY_SDCID, sdcid);
                properties.setProperty(PROPERTY_KEYID1, keyid1);
                if (keyid2 != null && keyid2.length() > 0) {
                    properties.setProperty(PROPERTY_KEYID2, keyid2);
                }
                if (keyid3 != null && keyid3.length() > 0) {
                    properties.setProperty(PROPERTY_KEYID3, keyid3);
                }
                properties.setProperty("attachmenthandlerid", attachmenthandlerid);
                properties.setProperty("attachmentclass", "" + attachmentclass);
                properties.setProperty(PROPERTY_MODE, mode.toString());
                String execid = this.addSDIAttachmentOperationExecution(sdcid, keyid1, keyid2, keyid3, attachmentoperationid, properties);
                properties.setProperty("execid", execid);
                try {
                    try {
                        this.getActionProcessor().processAction("AttachmentHandlerProcessor", "1", properties, false, false);
                        if (properties.getProperty("handlerresult", "1").equalsIgnoreCase("2")) {
                            throw new SapphireException("Attachment Handler failed.");
                        }
                        this.logExecutionSuccess(sdcid, keyid1, keyid2, keyid3, execid);
                    }
                    catch (Exception e) {
                        this.logExecutionFailure(sdcid, keyid1, keyid2, keyid3, execid, e.getMessage(), true);
                    }
                    safeSQL = new SafeSQL();
                }
                catch (Throwable throwable) {
                    SafeSQL safeSQL2 = new SafeSQL();
                    try {
                        this.database.executePreparedUpdate("UPDATE sdiattachmentoperation SET operationstatus=" + safeSQL2.addVar("Processed") + " WHERE attachmentoperationid=" + safeSQL2.addVar(attachmentoperationid) + " AND sdcid=" + safeSQL2.addVar("LV_DataCapture") + " AND keyid1=" + safeSQL2.addVar(keyid1) + (keyid2 != null && keyid2.length() > 0 ? " AND keyid2=" + safeSQL2.addVar(keyid2) : "") + (keyid3 != null && keyid3.length() > 0 ? " AND keyid3=" + safeSQL2.addVar(keyid3) : ""), safeSQL2.getValues());
                    }
                    catch (Exception e) {
                        this.logger.error("Failed to update operation status to processed.");
                    }
                    PropertyList props = new PropertyList();
                    props.setProperty("actionid", "CheckAttachmentOperations");
                    props.setProperty("actionversionid", "1");
                    props.setProperty("processname", sdcid + ";" + keyid1 + (keyid2 != null && keyid2.length() > 0 ? ";" + keyid2 : "") + (keyid3 != null && keyid3.length() > 0 ? ";" + keyid3 : ""));
                    props.setProperty(PROPERTY_SDCID, sdcid);
                    props.setProperty(PROPERTY_KEYID1, keyid1);
                    if (keyid2 != null && keyid2.length() > 0) {
                        props.setProperty(PROPERTY_KEYID2, keyid2);
                    }
                    if (keyid3 != null && keyid3.length() > 0) {
                        props.setProperty(PROPERTY_KEYID3, keyid3);
                    }
                    props.setProperty(PROPERTY_STATUSEND, baseProps != null ? baseProps.getProperty(PROPERTY_STATUSEND) : "");
                    props.setProperty(PROPERTY_STATUSCOLUMN, baseProps != null ? baseProps.getProperty(PROPERTY_STATUSCOLUMN) : "");
                    this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props, false);
                    throw throwable;
                }
                try {
                    this.database.executePreparedUpdate("UPDATE sdiattachmentoperation SET operationstatus=" + safeSQL.addVar("Processed") + " WHERE attachmentoperationid=" + safeSQL.addVar(attachmentoperationid) + " AND sdcid=" + safeSQL.addVar("LV_DataCapture") + " AND keyid1=" + safeSQL.addVar(keyid1) + (keyid2 != null && keyid2.length() > 0 ? " AND keyid2=" + safeSQL.addVar(keyid2) : "") + (keyid3 != null && keyid3.length() > 0 ? " AND keyid3=" + safeSQL.addVar(keyid3) : ""), safeSQL.getValues());
                }
                catch (Exception e) {
                    this.logger.error("Failed to update operation status to processed.");
                }
                PropertyList props = new PropertyList();
                props.setProperty("actionid", "CheckAttachmentOperations");
                props.setProperty("actionversionid", "1");
                props.setProperty("processname", sdcid + ";" + keyid1 + (keyid2 != null && keyid2.length() > 0 ? ";" + keyid2 : "") + (keyid3 != null && keyid3.length() > 0 ? ";" + keyid3 : ""));
                props.setProperty(PROPERTY_SDCID, sdcid);
                props.setProperty(PROPERTY_KEYID1, keyid1);
                if (keyid2 != null && keyid2.length() > 0) {
                    props.setProperty(PROPERTY_KEYID2, keyid2);
                }
                if (keyid3 != null && keyid3.length() > 0) {
                    props.setProperty(PROPERTY_KEYID3, keyid3);
                }
                props.setProperty(PROPERTY_STATUSEND, baseProps != null ? baseProps.getProperty(PROPERTY_STATUSEND) : "");
                props.setProperty(PROPERTY_STATUSCOLUMN, baseProps != null ? baseProps.getProperty(PROPERTY_STATUSCOLUMN) : "");
                this.getActionProcessor().processActionClass(AddToDoListEntry.class.getName(), props, false);
                break block19;
            }
            this.logger.warn("No data handler found for operation " + attachmentoperationid + ".");
        }
    }

    private Mode mergeAttachmentOperationsProperties(PropertyList properties, String jsonStr) {
        Mode mode = Mode.PROCESS;
        try {
            if (jsonStr != null && jsonStr.length() > 0) {
                PropertyListCollection variables;
                PropertyList props = new PropertyList(new JSONObject(jsonStr));
                if (props.containsKey("debugmode") && props.getProperty("debugmode").equalsIgnoreCase("Y")) {
                    mode = Mode.DEBUG;
                }
                if ((variables = props.getCollection("variables")) != null) {
                    for (int i = 0; i < variables.size(); ++i) {
                        PropertyList var = variables.getPropertyList(i);
                        String varId = var.getProperty("variableid", "");
                        String value = var.getProperty("value", "");
                        properties.put("property_" + varId, value);
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return mode;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        Mode mode = Mode.getMode(propertyList.getProperty(PROPERTY_MODE));
        propertyList.remove(PROPERTY_MODE);
        String rerunexecution = propertyList.getProperty(PROPERTY_RERUNEXECUTIONID, "");
        if (rerunexecution.length() > 0) {
            this.rerunExecution(rerunexecution, mode);
            return;
        } else {
            String sdcid = propertyList.getProperty(PROPERTY_SDCID);
            String keyid1 = propertyList.getProperty(PROPERTY_KEYID1);
            String keyid2 = null;
            if (propertyList.containsKey(PROPERTY_KEYID2)) {
                keyid2 = propertyList.getProperty(PROPERTY_KEYID2);
            }
            String keyid3 = null;
            if (propertyList.containsKey(PROPERTY_KEYID3)) {
                keyid3 = propertyList.getProperty(PROPERTY_KEYID3);
            }
            if (keyid1.length() <= 0 || sdcid.length() <= 0) throw new SapphireException("No sdcid or keyid1 provided.");
            String attachmentoperationid = propertyList.getProperty(PROPERTY_ATTACHMENTOPERATIONID);
            if (attachmentoperationid.length() <= 0) throw new SapphireException("No capture operation provided.");
            SafeSQL safeSQL = new SafeSQL();
            SafeSQL safeSQLU = new SafeSQL();
            String select = "";
            String update = "";
            if (keyid2 != null && keyid2.length() > 0) {
                if (keyid3 != null && keyid3.length() > 0) {
                    select = "SELECT operationkeyid1, attachmentclass, operationstatus, propertyclob FROM sdiattachmentoperation WHERE attachmentoperationid=" + safeSQL.addVar(attachmentoperationid) + " AND sdcid=" + safeSQL.addVar("LV_DataCapture") + " AND keyid1=" + safeSQL.addVar(keyid1) + " AND keyid2=" + safeSQL.addVar(keyid2) + " AND keyid3=" + safeSQL.addVar(keyid3);
                    update = "UPDATE sdiattachmentoperation SET operationstatus=" + safeSQLU.addVar("Processing") + " WHERE attachmentoperationid=" + safeSQLU.addVar(attachmentoperationid) + " AND sdcid=" + safeSQLU.addVar("LV_DataCapture") + " AND keyid1=" + safeSQLU.addVar(keyid1) + "AND keyid2=" + safeSQLU.addVar(keyid2) + " AND keyid3=" + safeSQLU.addVar(keyid3);
                } else {
                    select = "SELECT operationkeyid1, attachmentclass, operationstatus, propertyclob FROM sdiattachmentoperation WHERE attachmentoperationid=" + safeSQL.addVar(attachmentoperationid) + " AND sdcid=" + safeSQL.addVar("LV_DataCapture") + " AND keyid1=" + safeSQL.addVar(keyid1) + " AND keyid2=" + safeSQL.addVar(keyid2);
                    update = "UPDATE sdiattachmentoperation SET operationstatus=" + safeSQLU.addVar("Processing") + " WHERE attachmentoperationid=" + safeSQLU.addVar(attachmentoperationid) + " AND sdcid=" + safeSQLU.addVar("LV_DataCapture") + " AND keyid1=" + safeSQLU.addVar(keyid1) + " AND keyid2=" + safeSQLU.addVar(keyid2);
                }
            } else {
                select = "SELECT operationkeyid1, attachmentclass, operationstatus, propertyclob FROM sdiattachmentoperation WHERE attachmentoperationid=" + safeSQL.addVar(attachmentoperationid) + " AND sdcid=" + safeSQL.addVar("LV_DataCapture") + " AND keyid1=" + safeSQL.addVar(keyid1);
                update = "UPDATE sdiattachmentoperation SET operationstatus=" + safeSQLU.addVar("Processing") + " WHERE attachmentoperationid=" + safeSQLU.addVar(attachmentoperationid) + " AND sdcid=" + safeSQLU.addVar("LV_DataCapture") + " AND keyid1=" + safeSQLU.addVar(keyid1);
            }
            DataSet captureops = this.getQueryProcessor().getPreparedSqlDataSet(select, safeSQL.getValues(), true);
            if (captureops == null || captureops.getRowCount() != 1) throw new SapphireException("Could not find single operation row.");
            String status = captureops.getValue(0, "operationstatus", "ready");
            if (status.equalsIgnoreCase("ready")) {
                PropertyList baseprops = new PropertyList();
                baseprops.setProperty(PROPERTY_STATUSEND, propertyList.getProperty(PROPERTY_STATUSEND));
                baseprops.setProperty(PROPERTY_STATUSCOLUMN, propertyList.getProperty(PROPERTY_STATUSCOLUMN));
                this.processOperation(sdcid, keyid1, keyid2, keyid3, attachmentoperationid, captureops, update, safeSQLU, baseprops, mode);
                return;
            } else {
                this.logger.info("Operation " + attachmentoperationid + " skipped as not ready.");
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

