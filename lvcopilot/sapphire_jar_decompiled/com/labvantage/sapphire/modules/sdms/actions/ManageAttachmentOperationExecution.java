/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.actions;

import com.labvantage.sapphire.DataSetUtil;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import java.util.Calendar;
import sapphire.SapphireException;
import sapphire.accessor.ConfigurationProcessor;
import sapphire.accessor.SequenceProcessor;
import sapphire.action.BaseAction;
import sapphire.util.DataSet;
import sapphire.util.M18NUtil;
import sapphire.xml.PropertyList;

public class ManageAttachmentOperationExecution
extends BaseAction
implements SDMSConstants {
    private void logExecutionFailure(String sdcid, String keyid1, String keyid2, String keyid3, String executionid, String msg, boolean raiseAlert) throws SapphireException {
        if (sdcid.length() > 0 && keyid1.length() > 0 && executionid.length() > 0) {
            String instrumentid;
            DataSet instrument;
            ConfigurationProcessor cp;
            this.logger.error(msg);
            DataSet update = new DataSet();
            update.setM18NUtil(new M18NUtil(this.connectionInfo));
            update.addColumn("sdcid", 0);
            update.addColumn("keyid1", 0);
            update.addColumn("keyid2", 0);
            update.addColumn("keyid3", 0);
            update.addColumn("executionstatus", 0);
            update.addColumn("executionid", 0);
            update.addColumn("moddt", 2);
            update.addColumn("modby", 0);
            update.addRow();
            update.setValue(0, "sdcid", sdcid);
            update.setValue(0, "keyid1", keyid1);
            if (keyid2 != null && keyid2.length() > 0) {
                update.setValue(0, "keyid2", keyid2);
            } else {
                update.setValue(0, "keyid2", "(null)");
            }
            if (keyid3 != null && keyid3.length() > 0) {
                update.setValue(0, "keyid3", keyid3);
            } else {
                update.setValue(0, "keyid3", "(null)");
            }
            update.setValue(0, "executionid", executionid);
            update.setValue(0, "executionstatus", "fail");
            update.setValue(0, "modby", this.connectionInfo.getSysuserId());
            update.setDate(0, "moddt", Calendar.getInstance());
            try {
                DataSetUtil.update(this.database, update, "sdiattachmentoperationexec", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "executionid"});
            }
            catch (Exception e) {
                this.logger.error("Failed to update execution log.", e);
            }
            if (raiseAlert) {
                SDMSUtil.raiseSDMSAlert(this.getActionProcessor(), sdcid, keyid1, keyid2, keyid3, "SDMS Processing", "Failure", false, true, "Attachment Operation Execution Failure", msg);
            }
            if (sdcid.equalsIgnoreCase("LV_DataCapture") && (cp = this.getConfigurationProcessor()).getPolicy("SDMSPolicy", "Sapphire Custom").getPropertyListNotNull("errorhandling").getProperty("onexecutionfailure").equals("P") && (instrument = this.getQueryProcessor().getPreparedSqlDataSet("SELECT instrumentid FROM datacapture WHERE datacaptureid = ?", new Object[]{keyid1})).size() == 1 && (instrumentid = instrument.getValue(0, "instrumentid", "")).length() > 0) {
                this.pauseInstrument(instrumentid);
            }
        }
    }

    private void pauseInstrument(String instrumentid) {
        try {
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "Instrument");
            props.setProperty("keyid1", instrumentid);
            props.setProperty("sdmspausedflag", "Y");
            this.getActionProcessor().processAction("EditSDI", "1", props);
        }
        catch (Exception e) {
            this.logger.error("Failed to update instrument status to paused.", e);
        }
    }

    private void logExecutionSuccess(String sdcid, String keyid1, String keyid2, String keyid3, String executionid) throws SapphireException {
        if (sdcid.length() > 0 && keyid1.length() > 0 && executionid.length() > 0) {
            DataSet update = new DataSet();
            update.setM18NUtil(new M18NUtil(this.connectionInfo));
            update.addColumn("sdcid", 0);
            update.addColumn("keyid1", 0);
            update.addColumn("keyid2", 0);
            update.addColumn("keyid3", 0);
            update.addColumn("executionstatus", 0);
            update.addColumn("executionid", 0);
            update.addColumn("moddt", 2);
            update.addColumn("modby", 0);
            update.addRow();
            update.setValue(0, "sdcid", sdcid);
            update.setValue(0, "keyid1", keyid1);
            if (keyid2 != null && keyid2.length() > 0) {
                update.setValue(0, "keyid2", keyid2);
            } else {
                update.setValue(0, "keyid2", "(null)");
            }
            if (keyid3 != null && keyid3.length() > 0) {
                update.setValue(0, "keyid3", keyid3);
            } else {
                update.setValue(0, "keyid3", "(null)");
            }
            update.setValue(0, "executionid", executionid);
            update.setValue(0, "executionstatus", "success");
            update.setValue(0, "modby", this.connectionInfo.getSysuserId());
            update.setDate(0, "moddt", Calendar.getInstance());
            DataSetUtil.update(this.database, update, "sdiattachmentoperationexec", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "executionid"});
        }
    }

    private String addSDIAttachmentOperationExecution(String sdcid, String keyid1, String keyid2, String keyid3, String attachmentoperationid) throws SapphireException {
        SequenceProcessor sequenceProcessor = this.getSequenceProcessor();
        int seq = sequenceProcessor.getSequence("LV_DataCapture", "sdiattachmentoperationexecution");
        String executionid = "exec-" + seq;
        DataSet insert = new DataSet();
        insert.setM18NUtil(new M18NUtil(this.connectionInfo));
        insert.addColumn("sdcid", 0);
        insert.addColumn("keyid1", 0);
        insert.addColumn("keyid2", 0);
        insert.addColumn("keyid3", 0);
        insert.addColumn("attachmentoperationid", 0);
        insert.addColumn("executionid", 0);
        insert.addColumn("createdt", 2);
        insert.addColumn("createby", 0);
        insert.addRow();
        insert.setValue(0, "sdcid", sdcid);
        insert.setValue(0, "keyid1", keyid1);
        if (keyid2 != null && keyid2.length() > 0) {
            insert.setValue(0, "keyid2", keyid2);
        } else {
            insert.setValue(0, "keyid2", "(null)");
        }
        if (keyid3 != null && keyid3.length() > 0) {
            insert.setValue(0, "keyid3", keyid3);
        } else {
            insert.setValue(0, "keyid3", "(null)");
        }
        insert.setValue(0, "attachmentoperationid", attachmentoperationid);
        insert.setValue(0, "executionid", executionid);
        insert.setValue(0, "createby", this.connectionInfo.getSysuserId());
        insert.setDate(0, "createdt", Calendar.getInstance());
        DataSetUtil.insert(this.database, insert, "sdiattachmentoperationexec");
        return executionid;
    }

    private void updateSDIAttachmentOperationExecution(String sdcid, String keyid1, String keyid2, String keyid3, String executionid, String log, String actionBlock, String fields, String handlerProps, String sdilist) throws SapphireException {
        if (sdcid.length() > 0 && keyid1.length() > 0 && executionid.length() > 0) {
            DataSet update = new DataSet();
            update.setM18NUtil(new M18NUtil(this.connectionInfo));
            update.addColumn("executionid", 0);
            update.addColumn("handlerlog", 0);
            update.addColumn("handleractionblock", 3);
            update.addColumn("handlerfields", 3);
            update.addColumn("handlerproperties", 3);
            update.addColumn("handlersdilist", 3);
            update.addColumn("moddt", 2);
            update.addColumn("modby", 0);
            update.addColumn("sdcid", 0);
            update.addColumn("keyid1", 0);
            update.addColumn("keyid2", 0);
            update.addColumn("keyid3", 0);
            update.addRow();
            update.setValue(0, "sdcid", sdcid);
            update.setValue(0, "keyid1", keyid1);
            if (keyid2 != null && keyid2.length() > 0) {
                update.setValue(0, "keyid2", keyid2);
            } else {
                update.setValue(0, "keyid2", "(null)");
            }
            if (keyid3 != null && keyid3.length() > 0) {
                update.setValue(0, "keyid3", keyid3);
            } else {
                update.setValue(0, "keyid3", "(null)");
            }
            update.setValue(0, "executionid", executionid);
            if (log != null) {
                update.setValue(0, "handlerlog", log);
            }
            if (actionBlock != null && actionBlock.length() > 0) {
                update.setValue(0, "handleractionblock", actionBlock);
            }
            if (fields != null && fields.length() > 0) {
                update.setValue(0, "handlerfields", fields);
            }
            if (handlerProps != null && handlerProps.length() > 0) {
                update.setValue(0, "handlerproperties", handlerProps);
            }
            if (sdilist != null && sdilist.length() > 0) {
                update.setValue(0, "handlersdilist", sdilist);
            }
            update.setValue(0, "modby", this.connectionInfo.getSysuserId());
            update.setDate(0, "moddt", Calendar.getInstance());
            DataSetUtil.update(this.database, update, "sdiattachmentoperationexec", new String[]{"sdcid", "keyid1", "keyid2", "keyid3", "executionid"});
        }
    }

    @Override
    public void processAction(PropertyList propertyList) throws SapphireException {
        String sdcid = propertyList.getProperty("sdcid");
        String keyid1 = propertyList.getProperty("keyid1");
        String keyid2 = propertyList.getProperty("keyid2");
        String keyid3 = propertyList.getProperty("keyid3");
        if (sdcid.length() > 0 && keyid1.length() > 0) {
            Mode mode = Mode.ADD;
            try {
                mode = Mode.valueOf(propertyList.getProperty("mode", "ADD").toUpperCase());
            }
            catch (Exception exception) {
                // empty catch block
            }
            String executionid = propertyList.getProperty("executionid");
            switch (mode) {
                case ADD: {
                    String attachmentoperationid = propertyList.getProperty("attachmentoperationid");
                    if (attachmentoperationid.length() > 0) {
                        propertyList.setProperty("executionid", this.addSDIAttachmentOperationExecution(sdcid, keyid1, keyid2, keyid3, attachmentoperationid));
                        break;
                    }
                    this.logger.warn("No attachment operation id provided for execution creation");
                    break;
                }
                case EDIT: {
                    if (executionid.length() > 0) {
                        this.updateSDIAttachmentOperationExecution(sdcid, keyid1, keyid2, keyid3, executionid, propertyList.getProperty("log"), propertyList.getProperty("actionblock"), propertyList.getProperty("fields"), propertyList.getProperty("handlerprops"), propertyList.getProperty("sdilist"));
                        break;
                    }
                    this.logger.warn("No execution id provided for update");
                    break;
                }
                case FAILURE: {
                    if (executionid.length() > 0) {
                        this.logExecutionFailure(sdcid, keyid1, keyid2, keyid3, executionid, propertyList.getProperty("message"), propertyList.getProperty("raisealert").equalsIgnoreCase("Y"));
                        break;
                    }
                    this.logger.warn("No execution id provided for fail");
                    break;
                }
                case SUCCESS: {
                    if (executionid.length() > 0) {
                        this.logExecutionSuccess(sdcid, keyid1, keyid2, keyid3, executionid);
                        break;
                    }
                    this.logger.warn("No execution id provided for success");
                }
            }
        } else {
            this.logger.warn("No sdcid or keyid1 provided to work with execution");
        }
    }

    public static enum Mode {
        ADD,
        EDIT,
        SUCCESS,
        FAILURE;

    }
}

