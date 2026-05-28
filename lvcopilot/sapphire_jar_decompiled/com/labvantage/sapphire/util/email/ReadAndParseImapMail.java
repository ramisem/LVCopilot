/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.email.Attachment
 *  com.aspose.email.ImapClient
 *  com.aspose.email.ImapFolderInfo
 *  com.aspose.email.ImapFolderInfoCollection
 *  com.aspose.email.ImapMessageInfo
 *  com.aspose.email.ImapMessageInfoCollection
 *  com.aspose.email.ImapPageInfo
 *  com.aspose.email.ImapQueryBuilder
 *  com.aspose.email.MailMessage
 *  com.aspose.email.MailQuery
 *  com.aspose.email.MapiAttachment
 *  com.aspose.email.MapiMessage
 *  com.aspose.email.PageInfo
 *  com.aspose.email.PageSettings
 *  com.aspose.email.SaveOptions
 *  org.apache.commons.io.IOUtils
 */
package com.labvantage.sapphire.util.email;

import com.aspose.email.Attachment;
import com.aspose.email.ImapClient;
import com.aspose.email.ImapFolderInfo;
import com.aspose.email.ImapFolderInfoCollection;
import com.aspose.email.ImapMessageInfo;
import com.aspose.email.ImapMessageInfoCollection;
import com.aspose.email.ImapPageInfo;
import com.aspose.email.ImapQueryBuilder;
import com.aspose.email.MailMessage;
import com.aspose.email.MailQuery;
import com.aspose.email.MapiAttachment;
import com.aspose.email.MapiMessage;
import com.aspose.email.PageInfo;
import com.aspose.email.PageSettings;
import com.aspose.email.SaveOptions;
import com.labvantage.sapphire.util.email.EmailCaptureOptions;
import com.labvantage.sapphire.util.email.FetchMail;
import com.labvantage.sapphire.util.email.ReadAndParseMail;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;

public class ReadAndParseImapMail
implements ReadAndParseMail {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String UNIQUE_ID_DELIMETER = "$";
    private ImapClient client;

    @Override
    public FetchMail.Email readEmail(EmailCaptureOptions options) throws Exception {
        System.out.println("Reading mail using IMAP!!!!");
        FetchMail.Email email = null;
        try {
            this.client = this.getIMapClient(options);
            ArrayList<ImapFolderInfo> imapFolderInfos = new ArrayList<ImapFolderInfo>();
            this.getImapFolders(options.getArchiveFolder(), imapFolderInfos, options.getMailboxFolderToRead());
            if (imapFolderInfos.size() > 0) {
                email = this.fetchMessage((ImapFolderInfo)imapFolderInfos.get(0), options);
            }
        }
        catch (Exception e) {
            this.client.dispose();
            throw e;
        }
        this.client.dispose();
        return email;
    }

    private ImapClient getIMapClient(EmailCaptureOptions options) {
        ImapClient client = new ImapClient();
        client.setHost(options.getHost());
        client.setPort(options.getPort());
        client.setUsername(options.getUserId());
        client.setPassword(options.getPassword());
        client.setSecurityOptions(256);
        return client;
    }

    private void getImapFolders(String archivedFolder, List<ImapFolderInfo> exchangeFolderInfos, String folderToSearch) {
        ImapFolderInfoCollection folderInfoColl = null;
        folderInfoColl = this.client.listFolders(false);
        if (folderInfoColl != null) {
            for (ImapFolderInfo folderInfo : folderInfoColl) {
                if (folderInfo.getName().equals(archivedFolder) || !folderInfo.getName().toLowerCase().contains(folderToSearch.toLowerCase())) continue;
                if (this.client.existFolder(folderInfo.getName())) {
                    if (folderInfo.getFolderType() == 2 || folderInfo.getFolderType() == 3 || folderInfo.getFolderType() == 5 || folderInfo.getFolderType() == 6 || folderInfo.getFolderType() == 7) continue;
                    exchangeFolderInfos.add(folderInfo);
                    continue;
                }
                ImapFolderInfoCollection folderInfoCol2 = this.client.listFolders(folderInfo.getName(), false);
                for (ImapFolderInfo folderInfo1 : folderInfoCol2) {
                    if (folderInfo1.getName().equals(archivedFolder) || !folderInfo.getName().toLowerCase().contains(folderToSearch.toLowerCase()) || folderInfo1.getFolderType() == 2 || folderInfo1.getFolderType() == 3 || folderInfo1.getFolderType() == 5 || folderInfo1.getFolderType() == 6 || folderInfo1.getFolderType() == 7) continue;
                    exchangeFolderInfos.add(folderInfo1);
                }
            }
        }
    }

    private String getDeletedItemFolder() {
        String deletedItemFolder = "";
        ImapFolderInfoCollection folderInfoColl = this.client.listFolders(false);
        for (ImapFolderInfo folderInfo1 : folderInfoColl) {
            if (this.client.existFolder(folderInfo1.getName())) {
                if (folderInfo1.getFolderType() != 7) continue;
                deletedItemFolder = folderInfo1.getName();
                break;
            }
            ImapFolderInfoCollection folderInfoCol2 = this.client.listFolders(folderInfo1.getName(), false);
            for (ImapFolderInfo folderInfoCol : folderInfoCol2) {
                if (folderInfoCol.getFolderType() != 7) continue;
                deletedItemFolder = folderInfoCol.getName();
                break;
            }
            if (deletedItemFolder.trim().isEmpty()) continue;
            break;
        }
        return deletedItemFolder;
    }

    private FetchMail.Email fetchMessage(ImapFolderInfo imapFolderInfo, EmailCaptureOptions options) throws Exception {
        String lastReadMessageUniqueId;
        FetchMail.Email email = null;
        boolean hasCriteria = false;
        int itemsPerPage = 150;
        ImapQueryBuilder builder = new ImapQueryBuilder();
        Calendar fetchFromDate = options.getLastReadDateForMails();
        String string = lastReadMessageUniqueId = options.getMessageUniqueId() == null ? "" : options.getMessageUniqueId();
        if (fetchFromDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fetchFromDate.getTime());
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            builder.getInternalDate().since(calendar.getTime(), 1);
        }
        if (options.getSubjectCriteria().trim().length() > 0 && options.getMessageCriteria().trim().length() > 0) {
            builder.or(builder.getSubject().contains(options.getSubjectCriteria(), true), builder.getBody().contains(options.getMessageCriteria(), true));
            hasCriteria = true;
        } else if (options.getSubjectCriteria().trim().length() > 0 && options.getMessageCriteria().trim().length() == 0) {
            builder.getSubject().contains(options.getSubjectCriteria(), true);
            hasCriteria = true;
        } else if (options.getSubjectCriteria().trim().length() == 0 && options.getMessageCriteria().trim().length() > 0) {
            builder.getBody().contains(options.getMessageCriteria(), true);
            hasCriteria = true;
        } else {
            hasCriteria = false;
        }
        ImapMessageInfoCollection msgCollection = null;
        ArrayList<ImapPageInfo> pages = new ArrayList<ImapPageInfo>();
        ImapPageInfo pageInfo = null;
        MailQuery query = null;
        PageSettings pageSettings = new PageSettings();
        pageSettings.setFolderName(imapFolderInfo.getName());
        if (hasCriteria) {
            query = builder.getQuery();
            this.client.selectFolder(imapFolderInfo.getName());
            pageInfo = this.client.listMessagesByPage(query, new PageInfo(itemsPerPage, 0), pageSettings);
        } else {
            this.client.selectFolder(imapFolderInfo.getName());
            pageInfo = this.client.listMessagesByPage(itemsPerPage, 0, pageSettings);
        }
        pages.add(pageInfo);
        while (!pageInfo.getLastPage()) {
            pageInfo = hasCriteria ? this.client.listMessagesByPage(query, pageInfo.getNextPage(), pageSettings) : this.client.listMessagesByPage(itemsPerPage, pageInfo.getNextPage().getPageOffset(), pageSettings);
            pages.add(pageInfo);
        }
        for (ImapPageInfo folderCol : pages) {
            msgCollection = folderCol.getItems();
            boolean continueFetchingMssg = true;
            for (ImapMessageInfo msgInfo : msgCollection) {
                MailMessage msg;
                String uniqueId = msgInfo.getUniqueId();
                String messageId = msgInfo.getMessageId();
                if (lastReadMessageUniqueId != null) {
                    if (lastReadMessageUniqueId.contains(messageId)) {
                        continueFetchingMssg = true;
                        continue;
                    }
                    continueFetchingMssg = false;
                }
                if ((msg = this.client.fetchMessage(uniqueId)) == null || msg.getFrom() == null) continue;
                if (options.getFromMatchingPatterns() != null && !options.getFromMatchingPatterns().isEmpty()) {
                    boolean isMatch = false;
                    for (int x = 0; x < options.getFromMatchingPatterns().size(); ++x) {
                        String globPattern = options.getFromMatchingPatterns().get(x);
                        isMatch = this.checkGlobPattern(msg.getFrom().getAddress(), globPattern);
                        if (isMatch) break;
                    }
                    if (!isMatch) continue;
                    continueFetchingMssg = false;
                } else {
                    continueFetchingMssg = false;
                }
                if (!options.isMustHaveAttachment()) {
                    continueFetchingMssg = false;
                } else {
                    if (msg.getAttachments().size() == 0) {
                        continueFetchingMssg = true;
                        continue;
                    }
                    continueFetchingMssg = false;
                }
                email = new FetchMail().new FetchMail.Email();
                String subject = msg.getSubject();
                Calendar sentDate = Calendar.getInstance();
                sentDate.setTime(msg.getDate());
                String addressFrom = msg.getFrom().getAddress();
                ArrayList<String> addressesTo = new ArrayList<String>();
                msg.getTo().forEach(k -> addressesTo.add(k.getAddress()));
                ArrayList<String> addressesCC = new ArrayList<String>();
                msg.getCC().forEach(k -> addressesCC.add(k.getAddress()));
                ArrayList<String> addressesBcc = new ArrayList<String>();
                msg.getBcc().forEach(k -> addressesBcc.add(k.getAddress()));
                email.setSubject(subject);
                email.setSentDate(sentDate);
                Calendar sentDateCalendar = Calendar.getInstance();
                sentDateCalendar.setTime(sentDate.getTime());
                int sentDateDay = sentDateCalendar.get(5);
                int sentDateMonth = sentDateCalendar.get(2);
                int sentDateYear = sentDateCalendar.get(1);
                String sentDateStr = "" + sentDateDay + sentDateMonth + sentDateYear;
                int fetchedFromDateDay = fetchFromDate.get(5);
                int fetchedFromDateMonth = fetchFromDate.get(2);
                int fetchedFromDateYear = fetchFromDate.get(1);
                String fetchedFromDateStr = "" + fetchedFromDateDay + fetchedFromDateMonth + fetchedFromDateYear;
                if (sentDateStr.equals(fetchedFromDateStr)) {
                    String newUniqueId = lastReadMessageUniqueId + UNIQUE_ID_DELIMETER + messageId + UNIQUE_ID_DELIMETER;
                    email.setUniqueId(newUniqueId);
                    email.setLastReadDateForMails(sentDate.getTime().getTime());
                } else {
                    email.setUniqueId(messageId);
                    email.setLastReadDateForMails(sentDate.getTime().getTime());
                }
                email.setAddressFrom(addressFrom);
                email.setAddressesTo(addressesTo);
                if (addressesCC != null && addressesCC.size() > 0) {
                    email.setAddressesCC(addressesCC);
                }
                if (addressesBcc != null && addressesBcc.size() > 0) {
                    email.setAddressesBcc(addressesBcc);
                }
                email.setProtocol("IMAP");
                boolean storeMessageOption = options.getStoreOption();
                String storeattachmentOption = options.getStoreAttachmentOption();
                List<String> matchingFilePattern = null;
                HashMap<String, Map<String, Object>> attachments = new HashMap<String, Map<String, Object>>();
                if (storeattachmentOption.equals(EmailCaptureOptions.StoreAttachmentOptions.MATCHING.toString())) {
                    matchingFilePattern = options.getMatchingFilenamePatterns();
                }
                Path tempMsgPath = Files.createTempFile(subject.trim().replaceAll(":", "_").replaceAll("\\s+", "_"), ".msg", new FileAttribute[0]);
                msg.save(tempMsgPath.toFile().getAbsolutePath(), (SaveOptions)SaveOptions.getDefaultMsg());
                MapiMessage tempMsg = MapiMessage.fromFile((String)tempMsgPath.toFile().getAbsolutePath());
                this.constructAttachmentMap(tempMsg, attachments);
                this.filterAttachments(attachments, options.isMustHaveAttachment(), storeattachmentOption, matchingFilePattern);
                tempMsg.getAttachments().removeRange(0, tempMsg.getAttachments().size());
                Path tempMsgPath1 = Files.createTempFile(subject.trim().replaceAll(":", "_").replaceAll("\\s+", "_"), ".msg", new FileAttribute[0]);
                tempMsg.save(Files.newOutputStream(tempMsgPath1, new OpenOption[0]), (SaveOptions)SaveOptions.getDefaultMsg());
                boolean doDeleteMsgTempFile = false;
                if (storeMessageOption) {
                    doDeleteMsgTempFile = false;
                    email.setMsgPath(tempMsgPath1);
                    tempMsgPath.toFile().delete();
                    if (!attachments.isEmpty() && attachments.size() > 0) {
                        email.setAttachments(attachments);
                    }
                } else {
                    doDeleteMsgTempFile = true;
                    if (!attachments.isEmpty()) {
                        email.setAttachments(attachments);
                    }
                }
                if (doDeleteMsgTempFile) {
                    Files.delete(tempMsgPath);
                    Files.delete(tempMsgPath1);
                }
                if (continueFetchingMssg) continue;
                if (options.getActionOnOriginalOption().equals(EmailCaptureOptions.ActionOnOriginalOptions.ARCHIVE.toString())) {
                    if (options.getArchiveFolder().trim().length() <= 0) break;
                    if (!this.client.existFolder(options.getArchiveFolder().trim())) {
                        this.client.createFolder(options.getArchiveFolder().trim());
                    }
                    this.client.moveMessage(uniqueId, options.getArchiveFolder().trim());
                    this.client.commitDeletes();
                    break;
                }
                if (options.getActionOnOriginalOption().equals(EmailCaptureOptions.ActionOnOriginalOptions.DELETE_TO_DELETED_ITEMS.toString())) {
                    String deletedItemFolder = this.getDeletedItemFolder();
                    this.client.moveMessage(uniqueId, deletedItemFolder);
                    this.client.commitDeletes();
                    break;
                }
                if (!options.getActionOnOriginalOption().equals(EmailCaptureOptions.ActionOnOriginalOptions.DELETE_PERMANENTLY.toString())) break;
                this.client.deleteMessage(uniqueId);
                this.client.commitDeletes();
                break;
            }
            if (email == null) continue;
            break;
        }
        return email;
    }

    private boolean checkGlobPattern(String toMatch, List<String> globPatterns) {
        boolean globPatternMatched = false;
        if (globPatterns != null && !globPatterns.isEmpty()) {
            for (String globPattern : globPatterns) {
                Path filePath;
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
                if (!pathMatcher.matches(filePath = Paths.get(toMatch, new String[0]))) continue;
                globPatternMatched = true;
                break;
            }
        } else {
            globPatternMatched = true;
        }
        return globPatternMatched;
    }

    private boolean checkGlobPattern(String toMatch, String globPattern) {
        Path filePath;
        PathMatcher pathMatcher;
        boolean globPatternMatched = false;
        toMatch = toMatch.replaceAll(":", "");
        if (globPattern != null && !globPattern.trim().isEmpty() && (pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern)).matches(filePath = Paths.get(toMatch, new String[0]))) {
            globPatternMatched = true;
        }
        return globPatternMatched;
    }

    private void constructAttachmentMap(MapiMessage msg, Map<String, Map<String, Object>> attachments) {
        msg.getAttachments().forEach(k -> {
            MapiAttachment attachment = k;
            String filenameWithExtension = attachment.getDisplayName();
            String filename = filenameWithExtension.split("\\.")[0] + "#";
            String extension = filenameWithExtension.split("\\.")[1];
            Path tempFilePath = null;
            try {
                tempFilePath = Files.createTempFile(filename, "." + extension, new FileAttribute[0]);
                Files.write(tempFilePath, attachment.getBinaryData(), new OpenOption[0]);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            HashMap<String, Path> attachmentMap = new HashMap<String, Path>();
            if (tempFilePath != null) {
                attachmentMap.put("filePath", tempFilePath);
            }
            attachments.put(filenameWithExtension, attachmentMap);
        });
    }

    private void constructAttachmentMap(MailMessage msg, Map<String, Map<String, Object>> attachments) {
        msg.getAttachments().forEach(k -> {
            Attachment attachment = k;
            String contentDispositionType = attachment.getContentDisposition().getDispositionType();
            String filenameWithExtension = attachment.getContentDisposition().getFileName();
            String filename = filenameWithExtension.split("\\.")[0] + "#";
            String extension = filenameWithExtension.split("\\.")[1];
            Path tempFilePath = null;
            try {
                tempFilePath = Files.createTempFile(filename, "." + extension, new FileAttribute[0]);
                Files.write(tempFilePath, IOUtils.toByteArray((InputStream)attachment.getContentStream()), new OpenOption[0]);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
            HashMap<String, Object> attachmentMap = new HashMap<String, Object>();
            attachmentMap.put("contentType", contentDispositionType);
            if (tempFilePath != null) {
                attachmentMap.put("filePath", tempFilePath);
            }
            attachments.put(filenameWithExtension, attachmentMap);
        });
    }

    private void filterAttachments(Map<String, Map<String, Object>> attachments, boolean mustHaveAttachments, String storeAttachments, List<String> globPatterns) {
        if (attachments != null && attachments.size() > 0) {
            ArrayList<String> attachmentsToDelete = new ArrayList<String>();
            if (storeAttachments.equals(EmailCaptureOptions.StoreAttachmentOptions.MATCHING.toString())) {
                if (globPatterns != null && !globPatterns.isEmpty()) {
                    for (Map.Entry<String, Map<String, Object>> entry : attachments.entrySet()) {
                        String filename = entry.getKey();
                        if (this.checkGlobPattern(filename, globPatterns)) continue;
                        attachmentsToDelete.add(filename);
                    }
                    attachmentsToDelete.forEach(k -> {
                        Map cfr_ignored_0 = (Map)attachments.remove(k);
                    });
                }
            } else if (storeAttachments.equals(EmailCaptureOptions.StoreAttachmentOptions.NONE.toString())) {
                for (Map.Entry<String, Map<String, Object>> entry : attachments.entrySet()) {
                    String filename = entry.getKey();
                    attachmentsToDelete.add(filename);
                }
                attachmentsToDelete.forEach(k -> {
                    Map cfr_ignored_0 = (Map)attachments.remove(k);
                });
            }
        }
    }

    private void deleteAttachmentsFromMsg(MapiMessage tempMsg, Path tempMsgPath, Map<String, Map<String, Object>> attachments, List<String> matchingFilePattern) throws IOException {
        ArrayList<MapiAttachment> mapiAttachmentsToDelete = new ArrayList<MapiAttachment>();
        for (MapiAttachment mapiAttachment : tempMsg.getAttachments()) {
            String filename = mapiAttachment.getFileName();
            if (this.checkGlobPattern(filename, matchingFilePattern)) continue;
            mapiAttachmentsToDelete.add(mapiAttachment);
        }
        if (mapiAttachmentsToDelete.size() > 0) {
            for (MapiAttachment mapiAttachment : mapiAttachmentsToDelete) {
                tempMsg.getAttachments().removeMapiAttachment(mapiAttachment);
            }
        }
        tempMsg.save(Files.newOutputStream(tempMsgPath, new OpenOption[0]));
    }

    private void saveAttachmentToMsg(MapiMessage tempMsg, Path tempMsgPath, Map<String, Map<String, Object>> attachments) throws IOException {
        if (attachments != null && attachments.size() > 0) {
            for (Map.Entry<String, Map<String, Object>> entry : attachments.entrySet()) {
                String filename = entry.getKey();
                Path filePath = (Path)entry.getValue().get("filePath");
                tempMsg.getAttachments().add(filename, Files.readAllBytes(filePath));
            }
        }
        tempMsgPath.toFile().delete();
        tempMsg.save(tempMsgPath.toFile().getAbsolutePath(), (SaveOptions)SaveOptions.getDefaultMsg());
    }
}

