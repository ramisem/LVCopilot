/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  org.apache.commons.io.FilenameUtils
 */
package com.labvantage.sapphire.modules.sdms.collector;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.sapphire.Build;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.FileUtil;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.admin.webadmin.WebAdminProcessor;
import com.labvantage.sapphire.modules.sdms.SDMSConstants;
import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.actions.CreateDataCapture;
import com.labvantage.sapphire.modules.sdms.actions.UpdateDataCapture;
import com.labvantage.sapphire.modules.sdms.collector.SDMSFullUpgradeCounter;
import com.labvantage.sapphire.modules.sdms.ui.SDMSInstallDownloadHandler;
import com.labvantage.sapphire.services.Attachment;
import com.labvantage.sapphire.util.file.FileManager;
import com.labvantage.sapphire.util.file.FileTransfer;
import com.labvantage.sapphire.util.file.FileTransferOptions;
import com.labvantage.sapphire.xml.PropertyTree;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.servlet.ServletException;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.accessor.ActionException;
import sapphire.accessor.AttachmentProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.servlet.BaseExternalHandler;
import sapphire.util.DataSet;
import sapphire.xml.PropertyList;
import sapphire.xml.PropertyListCollection;

public class SDMSCommandHandler
extends BaseExternalHandler
implements SDMSConstants {
    @Override
    public PropertyList processCommand(String command, PropertyList commandRequest) throws SapphireException {
        PropertyList commandResponse = new PropertyList();
        if (command.equals("COMMAND_REGISTERCOLLECTOR")) {
            this.registerCollector(commandRequest, commandResponse);
        } else if (command.equals("COMMAND_GETCONFIGPROPS")) {
            String collectorid = commandRequest.getProperty("collectorid");
            commandResponse = this.getConfigProps(collectorid);
        } else if (command.equals("COMMAND_GETINSTRUMENTCONFIGPROPS")) {
            String instrumentid = commandRequest.getProperty("instrumentid");
            commandResponse = this.getInstrumentConfigProps(instrumentid);
        } else if (command.equals("COMMAND_PING")) {
            this.ping(commandRequest, commandResponse);
        } else if (command.equals("COMMAND_COLLECTORCOMMAND_REPLY")) {
            this.collectorCommandReply(commandRequest, commandResponse);
        } else if (command.equals("COMMAND_SAVERUNTIMEPROPERTIES")) {
            this.saveRuntimeProperties(commandRequest, commandResponse);
        } else if (command.equals("COMMAND_CREATEDATACAPTURE")) {
            commandResponse = this.createDataCapture(commandRequest);
        } else if (command.equals("COMMAND_COMPLETEDATACAPTURE")) {
            commandResponse = this.completeDataCapture(commandRequest);
        } else if (command.equals("COMMAND_RAISEALERT")) {
            this.raiseAlert(commandRequest, commandResponse);
        } else if (command.equals("COMMAND_SENDMAIL")) {
            JSONObject jsonRequest = new JSONObject(commandRequest);
            this.sendEmail(null, jsonRequest);
        } else if (command.equals("COMMAND_ADDDATACAPTUREATTACHMENT")) {
            commandResponse = this.addDataCaptureAttachment(commandRequest);
        }
        return commandResponse;
    }

    @Override
    public File processFileDownloadCommand(String command, JSONObject commandRequest) throws SapphireException {
        if (command.equals("COMMAND_DOWNLOADUPGRADEZIP")) {
            return this.getUpgradeZip(commandRequest);
        }
        return null;
    }

    private File getUpgradeZip(JSONObject commandRequest) throws SapphireException {
        SDMSInstallDownloadHandler downloadHandler = new SDMSInstallDownloadHandler();
        downloadHandler.setConnectionId(this.getConnectionId());
        String collectorid = commandRequest.optString("collectorid");
        JSONObject downloadProps = null;
        try {
            downloadProps = new JSONObject();
            downloadProps.put("platform", commandRequest.optString("platform"));
            downloadProps.put("upgrademode", commandRequest.optString("upgrademode"));
        }
        catch (JSONException jSONException) {
            // empty catch block
        }
        Path file = null;
        try {
            file = downloadHandler.getUpgradeZip(collectorid, downloadProps, true);
        }
        catch (ServletException e) {
            throw new SapphireException("Failed to build upgrade zip: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
        return file.toFile();
    }

    private void raiseAlert(PropertyList commandRequest, PropertyList commandResponse) {
        String sdcid = commandRequest.getProperty("sdcid");
        String keyid1 = commandRequest.getProperty("keyid1");
        String alertType = commandRequest.getProperty("alerttype");
        String alertSeverity = commandRequest.getProperty("alertseverity");
        String forceNew = commandRequest.getProperty("forcenew", "N");
        String matchDescription = commandRequest.getProperty("matchdescription", "Y");
        String description = commandRequest.getProperty("description");
        String message = commandRequest.getProperty("message");
        String newIncidentid = SDMSUtil.raiseSDMSAlert(this.getActionProcessor(), sdcid, keyid1, alertType, alertSeverity, forceNew.equals("Y"), matchDescription.equals("Y"), description, message);
        commandResponse.setProperty("newkeyid1", newIncidentid);
    }

    @Override
    public JSONObject processFileCommand(String command, Path file, JSONObject commandRequest) throws SapphireException {
        JSONObject commandResponse = null;
        if (command.equals("COMMAND_ADDDATACAPTUREATTACHMENT")) {
            commandResponse = this.addDataCaptureAttachment(file, commandRequest);
        } else if (command.equals("COMMAND_SENDMAIL")) {
            this.sendEmail(file, commandRequest);
        } else if (command.equalsIgnoreCase("COMMAND_COMPLETEDATACAPTURE")) {
            this.completeDataCapture(file, commandRequest);
        }
        return commandResponse;
    }

    @Override
    public JSONObject processFileCommand(String command, String filename, InputStream inputStream, JSONObject commandRequest) throws SapphireException {
        JSONObject commandResponse = new JSONObject();
        if (command.equals("COMMAND_ADDDATACAPTUREATTACHMENT")) {
            try {
                commandResponse = this.addDataCaptureAttachment(filename, inputStream, commandRequest);
            }
            catch (Exception e) {
                throw new SapphireException("Process file command failed.", e);
            }
        } else if (command.equals("COMMAND_SENDMAIL")) {
            this.sendEmail(filename, inputStream, commandRequest);
        } else if (command.equalsIgnoreCase("COMMAND_COMPLETEDATACAPTURE")) {
            this.completeDataCapture(filename, inputStream, commandRequest);
        }
        return commandResponse;
    }

    private void saveRuntimeProperties(PropertyList commandRequest, PropertyList commandResponse) throws ActionException {
        String collectorid = commandRequest.getProperty("collectorid");
        PropertyList runtimeProperties = commandRequest.getPropertyList("runtimeproperties");
        PropertyList props = new PropertyList();
        props.setProperty("sdcid", "LV_SDMSCollector");
        props.setProperty("keyid1", collectorid);
        props.setProperty("runtimeproperties", runtimeProperties.toXMLString());
        props.setProperty("skipconfighash", "Y");
        this.getActionProcessor().processAction("EditSDI", "1", props);
    }

    private JSONObject addDataCaptureAttachment(Path file, JSONObject commandRequest) throws SapphireException {
        JSONObject out = new JSONObject();
        if (file.toFile().exists()) {
            PropertyList metaData;
            String dataCaptureId = commandRequest.optString("datacaptureid");
            if (commandRequest.has("filemetadata")) {
                try {
                    metaData = new PropertyList(new JSONObject(commandRequest.getString("filemetadata")));
                }
                catch (Exception e) {
                    metaData = new PropertyList();
                }
            } else {
                metaData = new PropertyList();
            }
            AttachmentProcessor ap = this.getRakFile() != null ? new AttachmentProcessor(this.getRakFile(), this.getConnectionid()) : new AttachmentProcessor(this.getConnectionid());
            sapphire.attachment.Attachment attachment = FileManager.addFileAttachment("LV_DataCapture", dataCaptureId, "", "", file, "", false, metaData, this.getActionProcessor(), this.getQueryProcessor(), ap, this.getSDCProcessor(), this.getConnectionId());
            try {
                Attachment att = Attachment.getAttachment((Attachment)attachment, this.getQueryProcessor(), this.getConnectionId());
                long h = att.getDataHash();
                out.put("attachmentdatahash", h + "");
            }
            catch (Exception exception) {}
        } else {
            this.logger.warn("File " + file.toString() + " does not exist.");
        }
        return out;
    }

    private PropertyList addDataCaptureAttachment(PropertyList commandRequest) throws SapphireException {
        PropertyList out = new PropertyList();
        String filepath = commandRequest.getProperty("filepath", "");
        if (filepath.length() > 0) {
            Path fp = Paths.get(filepath, new String[0]);
            if (Files.exists(fp, new LinkOption[0])) {
                String dataCaptureId = commandRequest.getProperty("datacaptureid");
                PropertyList metaData = commandRequest.getPropertyListNotNull("filemetadata");
                AttachmentProcessor ap = this.getRakFile() != null ? new AttachmentProcessor(this.getRakFile(), this.getConnectionid()) : new AttachmentProcessor(this.getConnectionid());
                sapphire.attachment.Attachment attachment = FileManager.addFileReferenceAttachment("LV_DataCapture", dataCaptureId, "", "", fp, "", metaData, this.getActionProcessor(), this.getQueryProcessor(), ap, this.getSDCProcessor(), this.getConnectionId());
            } else {
                this.logger.warn("File " + filepath + " does not exist.");
            }
        } else {
            this.logger.warn("File not provided.");
        }
        return out;
    }

    private JSONObject addDataCaptureAttachment(String filename, InputStream inputStream, JSONObject commandRequest) throws SapphireException {
        JSONObject out = new JSONObject();
        boolean avail = false;
        try {
            avail = inputStream != null && inputStream.available() > 0;
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (avail) {
            PropertyList metaData;
            String dataCaptureId = commandRequest.optString("datacaptureid");
            if (commandRequest.has("filemetadata")) {
                try {
                    metaData = new PropertyList(new JSONObject(commandRequest.getString("filemetadata")));
                }
                catch (Exception e) {
                    metaData = new PropertyList();
                }
            } else {
                metaData = new PropertyList();
            }
            AttachmentProcessor ap = this.getRakFile() != null ? new AttachmentProcessor(this.getRakFile(), this.getConnectionid()) : new AttachmentProcessor(this.getConnectionid());
            String attachmentClass = null;
            if (commandRequest.has("attachmentclass")) {
                try {
                    attachmentClass = commandRequest.getString("attachmentclass");
                }
                catch (Exception e) {
                    attachmentClass = "";
                }
            }
            sapphire.attachment.Attachment attachment = FileManager.addFileAttachment("LV_DataCapture", dataCaptureId, "", "", filename, attachmentClass, inputStream, metaData, this.getActionProcessor(), this.getQueryProcessor(), ap, this.getSDCProcessor(), this.getConnectionId());
            try {
                Attachment att = Attachment.getAttachment((Attachment)attachment, this.getQueryProcessor(), this.getConnectionId());
                long h = att.getDataHash();
                out.put("attachmentdatahash", h + "");
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return out;
    }

    private void sendEmail(Path file, JSONObject commandRequest) throws SapphireException {
        String emailTo = commandRequest.optString("mailid");
        String emailFrom = commandRequest.optString("emailfrom");
        String instrumentId = commandRequest.optString("instrumentid");
        String emailsubject = commandRequest.optString("subject");
        String emailmessage = commandRequest.optString("contentbody");
        PropertyList actionProps = new PropertyList();
        actionProps.put("from", emailFrom);
        actionProps.put("to", emailTo);
        actionProps.put("subject", emailsubject);
        actionProps.put("message", emailmessage);
        if (file != null && file.toFile().exists()) {
            actionProps.put("filename", file.toAbsolutePath());
        }
        this.getActionProcessor().processAction("SendMail", "1", actionProps);
    }

    private void sendEmail(String filename, InputStream inputStream, JSONObject commandRequest) throws SapphireException {
        String emailTo = commandRequest.optString("mailid");
        String emailFrom = commandRequest.optString("emailfrom");
        String instrumentId = commandRequest.optString("instrumentid");
        String emailsubject = commandRequest.optString("subject");
        String emailmessage = commandRequest.optString("contentbody");
        PropertyList actionProps = new PropertyList();
        actionProps.put("from", emailFrom);
        actionProps.put("to", emailTo);
        actionProps.put("subject", emailsubject);
        actionProps.put("message", emailmessage);
        try {
            String baseName = FilenameUtils.getBaseName((String)filename);
            String fileExtension = FilenameUtils.getExtension((String)filename);
            File tempFile = FileUtil.createTempFile(baseName, "." + fileExtension).toFile();
            tempFile.delete();
            FileTransfer.safeDataTransfer(inputStream, tempFile, null);
            if (tempFile != null && tempFile.exists()) {
                actionProps.put("filename", tempFile.toPath().toAbsolutePath());
            }
            this.getActionProcessor().processAction("SendMail", "1", actionProps);
        }
        catch (Exception e) {
            throw new SapphireException(e);
        }
    }

    private void collectorCommandReply(PropertyList commandRequest, PropertyList commandResponse) {
        String collectorid = commandRequest.getProperty("collectorid");
        String commandid = commandRequest.getProperty("commandid");
        String commandreply = commandRequest.getProperty("commandreply");
        this.getQueryProcessor().execPreparedUpdate("UPDATE sdmscollectorcommand SET commandresponse=?, commandstatusflag=? WHERE sdmscollectorid=? AND sdmscollectorcommandid=?", new Object[]{commandreply, "R", collectorid, commandid});
    }

    private PropertyList createDataCapture(PropertyList commandRequest) throws ActionException {
        PropertyList commandResponse = new PropertyList();
        String instrumentid = commandRequest.getProperty("instrumentid");
        String sdmscollectorid = commandRequest.getProperty("sdmscollectorid");
        PropertyList props = new PropertyList();
        props.setProperty("instrumentid", instrumentid);
        if (sdmscollectorid.length() > 0) {
            props.setProperty("sdmscollectorid", sdmscollectorid);
        }
        this.getActionProcessor().processActionClass(CreateDataCapture.class.getName(), props);
        String dataCaptureId = props.getProperty("datacaptureid");
        commandResponse.setProperty("datacaptureid", dataCaptureId);
        return commandResponse;
    }

    private JSONObject completeDataCapture(String filename, InputStream is, JSONObject commandRequest) throws SapphireException {
        File mFile = null;
        try {
            mFile = FileUtil.createTempFile("manifest", "mf").toFile();
            FileTransferOptions fto = new FileTransferOptions();
            fto.setCloseInputStream(true);
            fto.setCloseInputStream(true);
            fto.setReplaceTarget(true);
            FileTransfer.safeDataTransfer(is, mFile, fto);
        }
        catch (Exception e) {
            throw new SapphireException(e.getMessage(), e);
        }
        if (mFile != null) {
            return this.completeDataCapture(mFile.toPath(), commandRequest);
        }
        return null;
    }

    private JSONObject completeDataCapture(Path file, JSONObject commandRequest) throws SapphireException {
        PropertyList manifest = new PropertyList();
        manifest.setPropertyList(file.toFile());
        PropertyList captureMetaData = manifest.getPropertyListNotNull("capturemetadata");
        if (captureMetaData.size() > 0) {
            UpdateDataCapture.addMetaData(captureMetaData, new String[]{"labvantageversion", "labvantagebuild", "instrumentid", "collectortypeid"}, commandRequest.optString("datacaptureid"), false, this.getSDCProcessor(), this.getActionProcessor());
        }
        PropertyList out = this.completeDataCapture(new PropertyList(commandRequest));
        return out.toJSONObject(false, false);
    }

    private PropertyList completeDataCapture(PropertyList commandRequest) throws SapphireException {
        PropertyList commandResponse = new PropertyList();
        String dataCaptureId = commandRequest.getProperty("datacaptureid");
        String log = commandRequest.getProperty("datacapturelog");
        if (commandRequest.getProperty("failure", "N").equalsIgnoreCase("Y")) {
            UpdateDataCapture.markFailure(dataCaptureId, log, this.getActionProcessor());
        } else {
            UpdateDataCapture.markCaptured(dataCaptureId, log, "", this.getActionProcessor());
        }
        return commandResponse;
    }

    private void ping(PropertyList commandRequest, PropertyList commandResponse) throws ActionException {
        String collectorid = commandRequest.getProperty("collectorid");
        if (collectorid.length() > 0) {
            DataSet commands;
            long lastMonitorTime;
            PropertyList props = new PropertyList();
            props.setProperty("sdcid", "LV_SDMSCollector");
            props.setProperty("keyid1", collectorid);
            props.setProperty("lastpingdt", "now");
            props.setProperty("skipconfighash", "Y");
            props.setProperty("_nolog", "Y");
            if (commandRequest.getProperty("firstping").equals("Y")) {
                PropertyList startupState = commandRequest.getPropertyList("startupstate");
                props.setProperty("laststartdt", "n");
                props.setProperty("actualhostname", commandRequest.getProperty("actualhostname"));
                props.setProperty("startupstate", commandRequest.getPropertyList("startupstate").toXMLString());
                boolean internalflag = startupState.getProperty("internalflag").equals("Y");
                props.setProperty("rebootrequiredflag", "N");
                if (!internalflag) {
                    String build = startupState.getProperty("labvantagebuild");
                    String version = startupState.getProperty("labvantageversion");
                    String upgradeCounter = startupState.getProperty("upgradecounter");
                    String collectorLibCustomHash = startupState.getProperty("libcustomhash");
                    props.setProperty("rebootrequiredflag", "N");
                    if (!version.equals(Build.getVersion())) {
                        props.setProperty("rebootrequiredflag", "F");
                    } else if (!upgradeCounter.equals(SDMSFullUpgradeCounter.getCounter())) {
                        props.setProperty("rebootrequiredflag", "F");
                    } else if (!build.equals(Build.getBuild())) {
                        props.setProperty("rebootrequiredflag", "R");
                    } else {
                        String libCustomHash = SDMSFullUpgradeCounter.getLibCustomHash(this.getConfigurationProcessor());
                        if (libCustomHash != null && libCustomHash.length() > 0 && collectorLibCustomHash.length() > 0 && !collectorLibCustomHash.equals(libCustomHash)) {
                            props.setProperty("rebootrequiredflag", "C");
                        }
                    }
                }
                this.getQueryProcessor().execPreparedUpdate("UPDATE instrument SET rebootrequiredflag=null WHERE sdmscollectorid=?", new String[]{collectorid});
            }
            this.getActionProcessor().processAction("EditSDI", "1", props);
            QueryProcessor qp = this.getQueryProcessor();
            DataSet collectorDS = qp.getPreparedSqlDataSet("collectorstate_nolog", "SELECT disabledflag, pausedflag FROM sdmscollector WHERE sdmscollectorid=?", new String[]{collectorid});
            commandResponse.setProperty("disabledflag", collectorDS.getValue(0, "disabledflag", "N"));
            commandResponse.setProperty("pausedflag", collectorDS.getValue(0, "pausedflag", "N"));
            PropertyListCollection collectorinstruments = new PropertyListCollection();
            DataSet instrumentDS = qp.getPreparedSqlDataSet("instrumentstate_nolog", "SELECT instrumentid,sdmspausedflag FROM instrument WHERE sdmscollectorid=?", new String[]{collectorid});
            for (int i = 0; i < instrumentDS.size(); ++i) {
                PropertyList nextInstr = new PropertyList();
                nextInstr.setProperty("instrumentid", instrumentDS.getString(i, "instrumentid", ""));
                nextInstr.setProperty("sdmspausedflag", instrumentDS.getString(i, "sdmspausedflag", "N"));
                collectorinstruments.add(nextInstr);
            }
            commandResponse.setProperty("collectorinstruments", collectorinstruments);
            ConfigurationProcessor configurationProcessor = new ConfigurationProcessor(this.getConnectionid());
            try {
                lastMonitorTime = Long.parseLong(configurationProcessor.getSysConfigProperty("lastsdmsmonitortime", "0"));
            }
            catch (SapphireException e) {
                lastMonitorTime = 0L;
            }
            long now = System.currentTimeMillis();
            if (now - lastMonitorTime < 30000L) {
                commandResponse.setProperty("fastping", "Y");
            }
            if ((commands = this.getQueryProcessor().getPreparedSqlDataSet("collectorping_nolog", "SELECT sdmscollectorcommandid, commandtype, commandparams, commandfilecontents, instrumentid FROM sdmscollectorcommand WHERE sdmscollectorid=? AND commandstatusflag=? ORDER BY sdmscollectorcommandid", new String[]{collectorid, "P"}, true)).size() > 0) {
                PropertyListCollection collectorCommands = new PropertyListCollection();
                commandResponse.setProperty("collectorcommands", collectorCommands);
                for (int i = 0; i < commands.size(); ++i) {
                    PropertyList nextCommand = new PropertyList();
                    String commandid = commands.getValue(i, "sdmscollectorcommandid");
                    nextCommand.setProperty("collectorid", collectorid);
                    nextCommand.setProperty("commandid", commandid);
                    nextCommand.setProperty("commandtype", commands.getValue(i, "commandtype"));
                    nextCommand.setProperty("commandparams", commands.getValue(i, "commandparams"));
                    nextCommand.setProperty("commandfilecontents", commands.getValue(i, "commandfilecontents"));
                    nextCommand.setProperty("instrumentid", commands.getValue(i, "instrumentid"));
                    collectorCommands.add(nextCommand);
                    this.getQueryProcessor().execPreparedUpdate("UPDATE sdmscollectorcommand SET commandstatusflag=? WHERE sdmscollectorid=? AND sdmscollectorcommandid=?", new Object[]{"S", collectorid, commandid});
                }
            }
        }
    }

    private void registerCollector(PropertyList commandRequest, PropertyList commandResponse) throws SapphireException {
        String collectorid = commandRequest.getProperty("collectorid");
        if (collectorid.length() == 0) {
            throw new SapphireException("Missing collectorid");
        }
        DataSet collector = this.getQueryProcessor().getPreparedSqlDataSet("SELECT internalflag, disabledflag FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{collectorid});
        if (collector.size() == 0) {
            throw new SapphireException("Collector has not been configured in the LIMS.");
        }
        if (collector.getValue(0, "internalflag").equals("Y")) {
            throw new SapphireException("Collector has been configured for INTERNAL deployment only");
        }
        this.getQueryProcessor().execPreparedUpdate("DELETE FROM sdmscollectorcommand WHERE sdmscollectorid=?", new String[]{collectorid});
    }

    private PropertyList getConfigProps(String collectorid) throws SapphireException {
        PropertyList commandResponse = new PropertyList();
        QueryProcessor qp = this.getQueryProcessor();
        StringBuilder collectorHashBuilder = new StringBuilder();
        if (collectorid.length() > 0) {
            DataSet collectorDS = qp.getPreparedSqlDataSet("SELECT * FROM sdmscollector WHERE sdmscollectorid=?", (Object[])new String[]{collectorid}, true);
            if (collectorDS == null || collectorDS.size() == 0) {
                throw new SapphireException("Unrecognized collector: " + collectorid);
            }
            PropertyList runtimeProperties = new PropertyList();
            String rp = collectorDS.getValue(0, "runtimeproperties");
            if (rp.length() > 0) {
                runtimeProperties.setPropertyList(rp);
            }
            collectorDS.setValue(0, "runtimeproperties", "");
            commandResponse.setProperty("collector_dataset", collectorDS.toJSONString());
            commandResponse.setProperty("runtimeproperties", runtimeProperties);
            collectorHashBuilder.append(collectorDS.getValue(0, "internalflag"));
            collectorHashBuilder.append(collectorDS.getValue(0, "storagepathlocal"));
            collectorHashBuilder.append(collectorDS.getValue(0, "storagemodeflag"));
            collectorHashBuilder.append(collectorDS.getValue(0, "corepoolsize"));
            collectorHashBuilder.append(collectorDS.getValue(0, "mindiskspace"));
        }
        try {
            PropertyList policy = this.getConfigurationProcessor().getPolicy("SDMSPolicy", "Sapphire Custom");
            PropertyList collectorDefaults = policy.getPropertyListNotNull("collectordefaults");
            commandResponse.setProperty("collectordefaults", collectorDefaults.toJSONString());
        }
        catch (SapphireException e) {
            throw new SapphireException("Unable to locate SDMS Policy");
        }
        DataSet instrumentDS = this.getInstrumentDataSet(qp, " WHERE inst.sdmscollectorid=? AND instrumentstatus!='Unavailable' AND ( inst.inserviceflag = 'Y' OR inst.inserviceflag IS NULL ) AND ( inst.activeflag = 'Y' OR inst.activeflag IS NULL )", collectorid);
        commandResponse.setProperty("instruments_dataset", instrumentDS.toJSONString(true, false));
        collectorHashBuilder.append(instrumentDS.getColumnValues("instrumentid", ";"));
        commandResponse.setProperty("confighash", "" + collectorHashBuilder.toString().hashCode());
        return commandResponse;
    }

    private PropertyList getInstrumentConfigProps(String instrumentid) throws SapphireException {
        PropertyList commandResponse = new PropertyList();
        QueryProcessor qp = this.getQueryProcessor();
        DataSet instrumentDS = this.getInstrumentDataSet(qp, " WHERE inst.instrumentid=? AND ( inst.activeflag = 'Y' OR inst.activeflag IS NULL )", instrumentid);
        commandResponse.setProperty("instruments_dataset", instrumentDS.toJSONString(true, false));
        return commandResponse;
    }

    public void sendCollectorCommand(String collectorid, String command, String commanddetails) {
        int commandnum = this.getSequenceProcessor().getSequence("LV_SDMSCollector", collectorid);
        this.getQueryProcessor().execPreparedUpdate("INSERT INTO sdmscollectorcommand( sdmscollectorid, commandnum, command, commanddetails ) values( ?, ?, ?, ? )", new Object[]{collectorid, commandnum, command, commanddetails});
    }

    private void setDecryptedValue(PropertyList config) throws Exception {
        for (Map.Entry entry : config.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                if (!value.toString().startsWith("__!ENC!__")) continue;
                String encryptedValue = value.toString().substring("__!ENC!__".length());
                String decryptedValue = EncryptDecrypt.decrypt(encryptedValue, this.getConnectionProcessor().getConnectionInfo(this.getQueryProcessor().getConnectionid()).getDatabaseId());
                entry.setValue(decryptedValue);
                continue;
            }
            if (!(value instanceof PropertyList)) continue;
            this.setDecryptedValue((PropertyList)value);
        }
    }

    private DataSet getInstrumentDataSet(QueryProcessor qp, String where, String arg) throws SapphireException {
        WebAdminProcessor webAdminProcessor = new WebAdminProcessor(this.getConnectionId());
        DataSet instrumentDS = qp.getPreparedSqlDataSet("SELECT inst.instrumentid, inst.instrumentdesc, inst.instrumenttype, inst.instrumentmodelid, inst.instrumentstatus,inst.inserviceflag,inst.activeflag,inst.sdmspausedflag, inst.collectorpropertytreeid, inst.collectorextendnodeid, inst.sdmscollectorid, inst.collectorvaluetree, inst.notes, instmodel.collectorvaluetree modelvaluetree FROM instrument inst LEFT OUTER JOIN instrumentmodel instmodel ON inst.instrumenttype=instmodel.instrumenttypeid and inst.instrumentmodelid=instmodel.instrumentmodelid " + where + " ORDER BY instrumentid", new Object[]{arg}, true);
        for (int i = 0; i < instrumentDS.size(); ++i) {
            String propertytreeid = instrumentDS.getValue(i, "collectorpropertytreeid");
            String extendnodeid = instrumentDS.getValue(i, "collectorextendnodeid");
            String valueTree = instrumentDS.getValue(i, "collectorvaluetree");
            String modelValueTree = instrumentDS.getValue(i, "modelvaluetree");
            instrumentDS.setString(i, "collectorpropertytreeid", null);
            instrumentDS.setString(i, "collectorextendnodeid", null);
            instrumentDS.setString(i, "collectorvaluetree", null);
            instrumentDS.setString(i, "modelvaluetree", null);
            PropertyList config = null;
            if (propertytreeid.length() <= 0) continue;
            try {
                PropertyTree propertyTree = webAdminProcessor.getPropertyTree(propertytreeid);
                String objectname = webAdminProcessor.getPropertyTreeObject(propertytreeid);
                config = propertyTree.getNodePropertyList(extendnodeid, true);
                if (modelValueTree.length() > 0) {
                    config.setPropertyList(modelValueTree, true, true);
                }
                if (valueTree.length() > 0) {
                    config.setPropertyList(valueTree, true, true);
                }
                this.setDecryptedValue(config);
                instrumentDS.setString(i, "_collectorrules", config.toXMLString());
                instrumentDS.setString(i, "_objectname", objectname);
                instrumentDS.setString(i, "_collectortype", propertytreeid);
                StringBuilder instrumentHashBuilder = new StringBuilder();
                instrumentHashBuilder.append(config.toXMLString());
                instrumentHashBuilder.append(objectname);
                instrumentHashBuilder.append(propertytreeid);
                instrumentDS.setString(i, "_confighash", "" + instrumentHashBuilder.toString().hashCode());
                continue;
            }
            catch (Exception e) {
                throw new SapphireException("Failed to get propertytree for control '" + propertytreeid + "'. Reason: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
            }
        }
        return instrumentDS;
    }

    public static SDMSCommandHandler getInstance(String connectionid, String databaseid) {
        SDMSCommandHandler handler = new SDMSCommandHandler();
        handler.setConnectionId(connectionid);
        handler.setDatabaseId(databaseid);
        return handler;
    }
}

