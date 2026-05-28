/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.email.Attachment
 *  com.aspose.email.DeletionOptions
 *  com.aspose.email.EWSClient
 *  com.aspose.email.ExchangeFolderInfo
 *  com.aspose.email.ExchangeFolderInfoCollection
 *  com.aspose.email.ExchangeMessageInfo
 *  com.aspose.email.ExchangeMessageInfoCollection
 *  com.aspose.email.ExchangeMessagePageInfo
 *  com.aspose.email.ExchangeQueryBuilder
 *  com.aspose.email.IEWSClient
 *  com.aspose.email.MailMessage
 *  com.aspose.email.MailQuery
 *  com.aspose.email.MapiAttachment
 *  com.aspose.email.MapiMessage
 *  com.aspose.email.SaveOptions
 *  org.apache.commons.io.IOUtils
 */
package com.labvantage.sapphire.util.email;

import com.aspose.email.Attachment;
import com.aspose.email.DeletionOptions;
import com.aspose.email.EWSClient;
import com.aspose.email.ExchangeFolderInfo;
import com.aspose.email.ExchangeFolderInfoCollection;
import com.aspose.email.ExchangeMessageInfo;
import com.aspose.email.ExchangeMessageInfoCollection;
import com.aspose.email.ExchangeMessagePageInfo;
import com.aspose.email.ExchangeQueryBuilder;
import com.aspose.email.IEWSClient;
import com.aspose.email.MailMessage;
import com.aspose.email.MailQuery;
import com.aspose.email.MapiAttachment;
import com.aspose.email.MapiMessage;
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

public class ReadAndParseExchangeMail
implements ReadAndParseMail {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String UNIQUE_ID_DELIMETER = "$";
    private IEWSClient client;

    @Override
    public FetchMail.Email readEmail(EmailCaptureOptions options) throws Exception {
        EWSClient.useSAAJAPI((boolean)true);
        this.client = EWSClient.getEWSClient((String)options.getExchangeURI(), (String)options.getUserId(), (String)options.getPassword(), (String)"");
        FetchMail.Email email = null;
        try {
            String rootUri = this.client.getMailboxInfo().getRootUri();
            if (this.client.folderExists(rootUri, options.getMailboxFolderToRead())) {
                ExchangeFolderInfoCollection folderInfoCollection = this.client.listSubFolders(rootUri);
                ExchangeFolderInfo folderInfo = null;
                for (ExchangeFolderInfo exchangeFolderInfo : folderInfoCollection) {
                    if (!exchangeFolderInfo.getDisplayName().toLowerCase().equals(options.getMailboxFolderToRead().toLowerCase())) continue;
                    folderInfo = exchangeFolderInfo;
                    break;
                }
                if (folderInfo != null) {
                    email = this.fetchMessage(folderInfo, options);
                }
            }
        }
        catch (Exception e) {
            this.client.dispose();
            throw e;
        }
        this.client.dispose();
        return email;
    }

    private void getExchangeFolders(String uri, String archivedFolder, List<ExchangeFolderInfo> exchangeFolderInfos, String folderToSearch) {
        ExchangeFolderInfoCollection folderInfoCollection = this.client.listSubFolders(uri);
        for (ExchangeFolderInfo exchangeFolderInfo : folderInfoCollection) {
            if (exchangeFolderInfo.getDisplayName().equals(archivedFolder) || exchangeFolderInfo.getDisplayName().equals("Deleted Items") || exchangeFolderInfo.getDisplayName().equals("Sent Items") || exchangeFolderInfo.getDisplayName().equals("Archive") || exchangeFolderInfo.getDisplayName().equals("Conversation History") || exchangeFolderInfo.getDisplayName().equals("Junk Email") || exchangeFolderInfo.getDisplayName().equals("Outbox") || exchangeFolderInfo.getDisplayName().equals("Drafts") || exchangeFolderInfo.getFolderType() != 1 || !exchangeFolderInfo.getDisplayName().toLowerCase().equalsIgnoreCase(folderToSearch.toLowerCase())) continue;
            exchangeFolderInfos.add(exchangeFolderInfo);
        }
    }

    private FetchMail.Email fetchMessage(ExchangeFolderInfo exchangeFolderInfo, EmailCaptureOptions options) throws Exception {
        String lastReadMessageUniqueId;
        FetchMail.Email email = null;
        boolean hasCriteria = false;
        int itemsPerPage = 150;
        ExchangeQueryBuilder builder = new ExchangeQueryBuilder();
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
            builder.or(builder.getSubject().contains(options.getSubjectCriteria().trim(), true), builder.getBody().contains(options.getMessageCriteria().trim(), true));
            hasCriteria = true;
        } else if (options.getSubjectCriteria().trim().length() > 0 && options.getMessageCriteria().trim().length() == 0) {
            builder.getSubject().contains(options.getSubjectCriteria().trim(), true);
            hasCriteria = true;
        } else if (options.getSubjectCriteria().trim().length() == 0 && options.getMessageCriteria().trim().length() > 0) {
            builder.getBody().contains(options.getMessageCriteria().trim(), true);
            hasCriteria = true;
        } else {
            hasCriteria = false;
        }
        ExchangeMessageInfoCollection msgCollection = null;
        ArrayList<ExchangeMessagePageInfo> pages = new ArrayList<ExchangeMessagePageInfo>();
        ExchangeMessagePageInfo pageInfo = null;
        MailQuery query = null;
        if (hasCriteria) {
            query = builder.getQuery();
            pageInfo = this.client.listMessagesByPage(exchangeFolderInfo.getUri(), query, itemsPerPage);
        } else {
            pageInfo = this.client.listMessagesByPage(exchangeFolderInfo.getUri(), itemsPerPage);
        }
        pages.add(pageInfo);
        while (!pageInfo.getLastPage()) {
            pageInfo = hasCriteria ? this.client.listMessagesByPage(exchangeFolderInfo.getUri(), query, itemsPerPage, pageInfo.getPageOffset() + 1) : this.client.listMessagesByPage(exchangeFolderInfo.getUri(), itemsPerPage, pageInfo.getPageOffset() + 1);
            pages.add(pageInfo);
        }
        for (ExchangeMessagePageInfo pageCol : pages) {
            msgCollection = pageCol.getItems();
            boolean continueFetchingMssg = true;
            for (int i = 0; i < msgCollection.size(); ++i) {
                ExchangeMessageInfo msgInfo = (ExchangeMessageInfo)msgCollection.get_Item(i);
                String uniqueUri = msgInfo.getUniqueUri();
                String messageId = msgInfo.getMessageId();
                if (lastReadMessageUniqueId != null) {
                    if (lastReadMessageUniqueId.contains(messageId)) {
                        continueFetchingMssg = true;
                        continue;
                    }
                    continueFetchingMssg = false;
                }
                if (msgInfo == null || msgInfo.getFrom() == null) continue;
                if (options.getFromMatchingPatterns() != null && !options.getFromMatchingPatterns().isEmpty()) {
                    boolean isMatch = false;
                    for (int x = 0; x < options.getFromMatchingPatterns().size(); ++x) {
                        String globPattern = options.getFromMatchingPatterns().get(x);
                        isMatch = this.checkGlobPattern(msgInfo.getFrom().getAddress(), globPattern);
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
                    if (msgInfo.getAttachments().size() == 0) {
                        continueFetchingMssg = true;
                        continue;
                    }
                    continueFetchingMssg = false;
                }
                MailMessage msg = this.client.fetchMessage(uniqueUri);
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
                email.setProtocol("Exchange");
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
                    ExchangeFolderInfo archivedFolderInfo;
                    if (options.getArchiveFolder().trim().length() <= 0) break;
                    if (!this.client.folderExists(this.client.getMailboxInfo().getRootUri(), options.getArchiveFolder().trim())) {
                        this.client.createFolder(this.client.getMailboxInfo().getRootUri(), options.getArchiveFolder().trim());
                    }
                    if ((archivedFolderInfo = this.getExchangeFolder(options.getArchiveFolder().trim())) == null) break;
                    String uri = this.client.appendMessage(msg);
                    this.client.moveItem(uri, archivedFolderInfo.getUri());
                    this.client.deleteItem(uniqueUri, DeletionOptions.getDeletePermanently());
                    break;
                }
                if (options.getActionOnOriginalOption().equals(EmailCaptureOptions.ActionOnOriginalOptions.DELETE_TO_DELETED_ITEMS.toString())) {
                    this.client.deleteItem(uniqueUri, DeletionOptions.getMoveToDeletedItems());
                    break;
                }
                if (!options.getActionOnOriginalOption().equals(EmailCaptureOptions.ActionOnOriginalOptions.DELETE_PERMANENTLY.toString())) break;
                this.client.deleteItem(uniqueUri, DeletionOptions.getDeletePermanently());
                break;
            }
            if (email == null) continue;
            break;
        }
        return email;
    }

    private static int getHours(String offset) {
        String hour = offset.substring(0, 2);
        String mins = offset.substring(2, 4);
        int time = Integer.parseInt(hour) * 60 * 60 * 1000 + Integer.parseInt(mins) * 60 * 1000;
        return time;
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
            String extension = null;
            try {
                extension = filenameWithExtension.split("\\.")[1];
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
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

    private boolean checkGlobPattern(String toMatch, List<String> globPatterns) {
        boolean globPatternMatched = false;
        toMatch = toMatch.replaceAll(":", "");
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

    private void saveAttachmentToMsg(MapiMessage tempMsg, Path tempMsgPath, Map<String, Map<String, Object>> attachments) throws IOException {
        if (attachments != null && attachments.size() > 0) {
            for (Map.Entry<String, Map<String, Object>> entry : attachments.entrySet()) {
                String filename = entry.getKey();
                Path filePath = (Path)entry.getValue().get("filePath");
                tempMsg.getAttachments().add(filename, Files.readAllBytes(filePath));
            }
        }
        tempMsg.save(tempMsgPath.toFile().getAbsolutePath(), (SaveOptions)SaveOptions.getDefaultMsg());
    }

    private ExchangeFolderInfo getExchangeFolder(String archivedFolder) {
        ExchangeFolderInfoCollection folderInfoCollection = this.client.listSubFolders(this.client.getMailboxInfo().getRootUri());
        ExchangeFolderInfo folderInfo = null;
        for (ExchangeFolderInfo exchangeFolderInfo : folderInfoCollection) {
            if (!exchangeFolderInfo.getDisplayName().equals(archivedFolder)) continue;
            folderInfo = exchangeFolderInfo;
        }
        return folderInfo;
    }
}

