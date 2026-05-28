/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.modules.sdms.collector.collectortypes;

import com.labvantage.sapphire.modules.sdms.SDMSUtil;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.BaseFileSender;
import com.labvantage.sapphire.modules.sdms.collector.storagemodes.FileSenderFactory;
import com.labvantage.sapphire.util.email.EmailCaptureOptions;
import com.labvantage.sapphire.util.email.FetchMail;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import sapphire.SapphireException;
import sapphire.ext.BaseCollectorType;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class EmailCollectorType
extends BaseCollectorType {
    boolean isCollectionEnabled = false;
    boolean isEmulatorEnabled = false;
    protected int collectorPollInterval;
    protected int emulatorPollInterval;
    String actionOnOriginal;
    String emulatorMailBox = "";
    String emulatorMailSubject = "";
    String emulatorMailContent = "";
    boolean emulatorAttachment = false;
    String emulatorAttachmentFile = "";
    Path lastTriggerFile = null;
    Calendar lastTriggerDt = null;
    String lastStoreDescription = null;
    PropertyList emulatorProps = null;
    private PropertyList triggerPropertyList;
    private PropertyList credentialsPropertyList;
    private PropertyList collectPropertyList;
    private String emailProtocol;
    private String exchangeUri;
    private String mailbox;
    private String emailHost;
    private int emailPort;
    private String userId;
    private String password;
    private String emailFromCriteria;
    private String emailSubjectCriteria;
    private String emailMessageCriteria;
    private boolean emailMustHaveAttachments;
    private boolean storeMessage;
    private String storeAttachments;
    private String matchFileName;
    private String archiveFolder;
    private String actionOnOriginalForPop3;
    private String attachmentClassForMsg;
    private String attachmentClassForAttachment;

    @Override
    public void configure(PropertyList collectorTypeProps) throws SapphireException {
        String msg = "";
        this.isCollectionEnabled = collectorTypeProps.getProperty("enablecollection").equals("Y");
        this.isEmulatorEnabled = collectorTypeProps.getProperty("enableemulator").equals("Y");
        if (this.isCollectionEnabled) {
            block13: {
                PropertyList collectorProps = collectorTypeProps.getPropertyList("collectorprops");
                this.triggerPropertyList = collectorProps.getPropertyListNotNull("trigger");
                this.credentialsPropertyList = collectorProps.getPropertyListNotNull("credentials");
                this.collectPropertyList = collectorProps.getPropertyListNotNull("collect");
                this.emailProtocol = collectorProps.getProperty("emailprotocol", "Exchange");
                if (this.emailProtocol.trim().equalsIgnoreCase("exchange")) {
                    this.exchangeUri = collectorProps.getProperty("exchangeuri");
                }
                this.mailbox = collectorProps.getProperty("mailbox", "inbox");
                this.emailHost = collectorProps.getProperty("emailhost");
                if (this.emailHost.trim().isEmpty() && !this.emailProtocol.trim().equalsIgnoreCase("exchange")) {
                    msg = "Mail host can not be blank";
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure email collector", msg, false, true);
                }
                try {
                    this.emailPort = Integer.parseInt(collectorProps.getProperty("emailport"));
                }
                catch (Exception err) {
                    this.emailPort = 0;
                    if (this.emailPort != 0 || this.emailProtocol.trim().equalsIgnoreCase("exchange")) break block13;
                    msg = "Email port should be a numeric value";
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure email collector", msg, false, true);
                }
            }
            this.userId = this.credentialsPropertyList.getProperty("userid");
            if (this.userId.trim().isEmpty()) {
                msg = "User Id can not be blank";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure email collector", msg, false, true);
            }
            this.password = this.credentialsPropertyList.getProperty("password");
            if (this.password.trim().isEmpty()) {
                msg = "Password can not be blank";
                this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure email collector", msg, false, true);
            }
            this.emailFromCriteria = this.triggerPropertyList.getProperty("from");
            this.emailSubjectCriteria = this.triggerPropertyList.getProperty("subjectcontains");
            this.emailMessageCriteria = this.triggerPropertyList.getProperty("messagecontains");
            this.collectorPollInterval = Integer.parseInt(this.triggerPropertyList.getProperty("triggerpollintervalseconds", "" + this.sdmsCollector.getDefaultInstrumentPollInterval()));
            this.emailMustHaveAttachments = this.triggerPropertyList.getProperty("withattachment").equals("Y");
            this.storeMessage = this.collectPropertyList.getProperty("store", "Y").equals("Y");
            String string = this.storeAttachments = this.collectPropertyList.getProperty("storeattachments").trim().equals("") ? "No" : this.collectPropertyList.getProperty("storeattachments");
            if (this.storeAttachments.trim().equals("Matching")) {
                this.matchFileName = this.collectPropertyList.getProperty("matchfilename");
            }
            this.actionOnOriginal = this.collectPropertyList.getProperty("actiononoriginal");
            if (this.actionOnOriginal.equals("Archive")) {
                this.archiveFolder = this.collectPropertyList.getProperty("archivefolder");
                if (this.archiveFolder.trim().isEmpty()) {
                    msg = "Archive folder can not be empty";
                    this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure email collector", msg, false, true);
                }
            } else {
                this.archiveFolder = "";
            }
            this.actionOnOriginalForPop3 = this.collectPropertyList.getProperty("actiononoriginalpop3");
            this.attachmentClassForMsg = this.collectPropertyList.getProperty("attachmentclassformsg");
            this.attachmentClassForAttachment = this.collectPropertyList.getProperty("attachmentclassforattachment");
            this.logStartup("Looking for emails in " + this.userId + " every " + this.collectorPollInterval + "seconds using " + this.emailProtocol + " protocol");
        }
        if (this.isEmulatorEnabled) {
            this.emulatorProps = collectorTypeProps.getPropertyList("emulatorprops");
            this.emulatorPollInterval = Integer.parseInt(this.emulatorProps.getProperty("frequency", "" + this.sdmsCollector.getDefaultInstrumentPollInterval()));
            int delta = Integer.parseInt(this.emulatorProps.getProperty("randomdelta", "0"));
            if (delta > 0) {
                int maximum = this.emulatorPollInterval + delta;
                int minimum = this.emulatorPollInterval - delta;
                this.emulatorPollInterval = SDMSUtil.getRandomInteger(maximum, minimum);
            }
            this.emulatorMailBox = this.emulatorProps.getProperty("mailbox");
            PropertyList mailDetails = this.emulatorProps.getPropertyList("maildetails");
            this.emulatorMailSubject = mailDetails.getProperty("subject", "Test mail");
            this.emulatorMailContent = mailDetails.getProperty("contentbody", "This is some content");
            this.emulatorAttachment = "Y".equalsIgnoreCase(mailDetails.getProperty("attachment"));
            this.emulatorAttachmentFile = mailDetails.getProperty("attachmentfile");
            this.logStartup("Emulating by generating email at the mailbox " + this.emulatorMailBox + " for the instrument " + this.instrumentid + " every " + this.emulatorPollInterval + "s.");
        }
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
        return false;
    }

    @Override
    public boolean isEmulatorEnabled() {
        return this.isEmulatorEnabled;
    }

    @Override
    public Calendar getLastCaptureDt() {
        return this.lastTriggerDt;
    }

    @Override
    public String getLastCaptureDescription() {
        return this.lastTriggerFile == null ? "None" : this.lastTriggerFile.toFile().getAbsolutePath();
    }

    @Override
    public String getLastStoreDescription() {
        return this.lastStoreDescription == null ? "None" : this.lastStoreDescription;
    }

    @Override
    public boolean doRunCollector(FileSenderFactory fileSenderFactory) throws SapphireException, IOException {
        boolean collected = false;
        FetchMail fetchMail = new FetchMail();
        ArrayList<String> globMatchingFilePatterns = new ArrayList();
        if (this.storeAttachments.trim().equalsIgnoreCase("matching") && this.matchFileName != null && !this.matchFileName.trim().equals("")) {
            String[] patternsArr = this.matchFileName.split(";");
            globMatchingFilePatterns = Arrays.asList(patternsArr);
        }
        ArrayList<String> fromCriterias = new ArrayList();
        if (this.emailFromCriteria != null && this.emailFromCriteria.trim().length() > 0) {
            String[] fromPatterns = this.emailFromCriteria.split(";");
            fromCriterias = Arrays.asList(fromPatterns);
        }
        EmailCaptureOptions emailCaptureOptions = new EmailCaptureOptions();
        emailCaptureOptions.setHost(this.emailHost);
        emailCaptureOptions.setPort(this.emailPort);
        emailCaptureOptions.setExchangeURI(this.exchangeUri);
        emailCaptureOptions.setUserId(this.userId);
        emailCaptureOptions.setPassword(this.password);
        emailCaptureOptions.setMailboxFolderToRead(this.mailbox);
        emailCaptureOptions.setFromMatchingPatterns(fromCriterias);
        emailCaptureOptions.setSubjectCriteria(this.emailSubjectCriteria);
        emailCaptureOptions.setMessageCriteria(this.emailMessageCriteria);
        emailCaptureOptions.setMustHaveAttachment(this.emailMustHaveAttachments);
        emailCaptureOptions.setStoreOption(this.storeMessage);
        emailCaptureOptions.setStoreAttachmentOption(this.storeAttachments);
        emailCaptureOptions.setMatchingFilenamePatterns(globMatchingFilePatterns);
        emailCaptureOptions.setActionOnOriginalOption(this.actionOnOriginal);
        emailCaptureOptions.setActionOnOriginalOptionForPop3(this.actionOnOriginalForPop3);
        emailCaptureOptions.setArchiveFolder(this.archiveFolder);
        emailCaptureOptions.setStorageLocalPath(this.sdmsCollector.getStoragePathLocal());
        Calendar fetchFromDate = Calendar.getInstance();
        String lastReadDateForMails = this.getRuntimeProperty("lastreaddateformails");
        if (!lastReadDateForMails.trim().equals("")) {
            fetchFromDate.setTimeInMillis(Long.parseLong(lastReadDateForMails));
            emailCaptureOptions.setLastReadDateForMails(fetchFromDate);
        } else {
            fetchFromDate.set(11, 0);
            fetchFromDate.set(12, 0);
            fetchFromDate.set(13, 0);
            emailCaptureOptions.setLastReadDateForMails(fetchFromDate);
        }
        String messageUniqueId = this.getRuntimeProperty("lastreadmessageid");
        if (!messageUniqueId.trim().equals("")) {
            emailCaptureOptions.setMessageUniqueId(messageUniqueId.trim());
        }
        FetchMail.Email email = null;
        try {
            email = fetchMail.readEmail(this.emailProtocol, emailCaptureOptions);
        }
        catch (Exception e) {
            String msg = e.getMessage();
            this.raiseInstrumentAlert("SDMS Startup", "Failure", "Failed to configure Collector", msg, false, true);
        }
        if (email != null) {
            collected = true;
            this.startCollectionLog("Trigger for " + this.instrumentid + " detected: 1 email");
            PropertyList captureMetaData = new PropertyList();
            Calendar lastTriggerDt = Calendar.getInstance();
            BaseFileSender fileSender = fileSenderFactory.getInstance(this.sdmsCollector, this);
            String sendId = null;
            try {
                sendId = fileSender.init(lastTriggerDt, captureMetaData);
            }
            catch (Exception e) {
                this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to initialise sender", "Failed to initialise the sender.", false, false);
            }
            if (sendId != null && sendId.length() > 0) {
                if (email.getSubject() != null && email.getSubject().trim().length() > 0) {
                    captureMetaData.put("subject", email.getSubject());
                }
                if (email.getSentDate() != null) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
                    String sentDateStr = simpleDateFormat.format(email.getSentDate().getTime());
                    captureMetaData.put("sentdate", sentDateStr);
                }
                if (email.getAddressFrom() != null) {
                    captureMetaData.put("from", email.getAddressFrom());
                }
                if (email.getAddressesTo() != null && email.getAddressesTo().size() > 0) {
                    captureMetaData.put("to", email.getAddressesTo().toString().replace("[", "").replace("]", ""));
                }
                if (email.getAddressesBcc() != null && email.getAddressesBcc().size() > 0) {
                    captureMetaData.put("bcc", email.getAddressesBcc().toString().replace("[", "").replace("]", ""));
                }
                if (email.getAddressesCC() != null && email.getAddressesCC().size() > 0) {
                    captureMetaData.put("cc", email.getAddressesCC().toString().replace("[", "").replace("]", ""));
                }
                if (email.getProtocol() != null) {
                    captureMetaData.put("protocol", email.getProtocol());
                }
                if (email.getMsgPath() != null) {
                    PropertyList fileMetaData = new PropertyList();
                    fileMetaData.setProperty("filename", email.getMsgPath().getFileName().toString());
                    try {
                        fileSender.store(sendId, email.getMsgPath(), BaseFileSender.ActionOnOrginal.DELETE, this.attachmentClassForMsg, false, null, null, fileMetaData);
                    }
                    catch (Exception e) {
                        this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to store data", "Failed to send the file to LIMS.", false, false);
                    }
                }
                if (email.getAttachments() != null && email.getAttachments().size() > 0) {
                    for (Map.Entry<String, Map<String, Object>> entry : email.getAttachments().entrySet()) {
                        PropertyList fileMetaData = new PropertyList();
                        String filename = entry.getKey();
                        Map<String, Object> value = entry.getValue();
                        Path filePath = (Path)value.get("filePath");
                        Path newPath = filePath.getParent().resolve(filename);
                        Files.move(filePath, newPath, StandardCopyOption.REPLACE_EXISTING);
                        fileMetaData.setProperty("filename", filename);
                        try {
                            fileSender.store(sendId, newPath, BaseFileSender.ActionOnOrginal.DELETE, this.attachmentClassForAttachment, false, null, null, fileMetaData);
                        }
                        catch (Exception e) {
                            this.raiseInstrumentAlert("SDMS Collection", "Failure", "Failed to store data", "Failed to send the file to LIMS.", false, false);
                        }
                    }
                }
                this.setRuntimeProperty("lastreaddateformails", Long.toString(email.getLastReadDateForMails()));
                this.setRuntimeProperty("lastreadmessageid", email.getUniqueId().trim());
                fileSender.complete(sendId);
            }
        }
        return collected;
    }

    @Override
    public boolean doRunEmulator() throws SapphireException {
        PropertyList mailDetails = this.emulatorProps.getPropertyList("maildetails");
        if (!this.emulatorMailBox.contains("@")) {
            throw new SapphireException("No Email Id or invalid Email Id found in Emulator property list");
        }
        String attachmentFile = mailDetails.getProperty("attachmentfile");
        Path[] attachmentFilePaths = null;
        boolean attachmentsPresent = "Y".equalsIgnoreCase(mailDetails.getProperty("attachment"));
        mailDetails.setProperty("instrumentid", this.instrumentid);
        mailDetails.setProperty("mailid", this.emulatorMailBox);
        JSONObject jsonRequest = new JSONObject(mailDetails);
        if (attachmentsPresent && attachmentFile.length() > 0) {
            String[] attachmentFiles = StringUtil.split(attachmentFile, ";");
            attachmentFilePaths = new Path[attachmentFiles.length];
            attachmentFilePaths[0] = Paths.get(attachmentFiles[0], new String[0]);
            this.sdmsCollector.container.sendFileCommandToLIMS("", "COMMAND_SENDMAIL", attachmentFilePaths[0], jsonRequest);
        } else {
            this.sdmsCollector.container.sendCommandToLIMS("COMMAND_SENDMAIL", mailDetails);
        }
        return true;
    }

    @Override
    public List<String> getReportsForSDC(PropertyList collectorTypeProps, String sdcid) {
        return null;
    }
}

