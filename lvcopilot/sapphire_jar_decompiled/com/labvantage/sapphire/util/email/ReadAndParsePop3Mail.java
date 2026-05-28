/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.aspose.email.Attachment
 *  com.aspose.email.MailMessage
 *  com.aspose.email.MailQuery
 *  com.aspose.email.MailQueryBuilder
 *  com.aspose.email.MapiAttachment
 *  com.aspose.email.MapiMessage
 *  com.aspose.email.Pop3Client
 *  com.aspose.email.Pop3MessageInfo
 *  com.aspose.email.Pop3MessageInfoCollection
 *  com.aspose.email.SaveOptions
 *  org.apache.commons.io.IOUtils
 */
package com.labvantage.sapphire.util.email;

import com.aspose.email.Attachment;
import com.aspose.email.MailMessage;
import com.aspose.email.MailQuery;
import com.aspose.email.MailQueryBuilder;
import com.aspose.email.MapiAttachment;
import com.aspose.email.MapiMessage;
import com.aspose.email.Pop3Client;
import com.aspose.email.Pop3MessageInfo;
import com.aspose.email.Pop3MessageInfoCollection;
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

public class ReadAndParsePop3Mail
implements ReadAndParseMail {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private static final String UNIQUE_ID_DELIMETER = "$";
    private Pop3Client client;

    @Override
    public FetchMail.Email readEmail(EmailCaptureOptions options) throws Exception {
        System.out.println("Reading mail using POP3!!!!");
        FetchMail.Email email = null;
        try {
            this.client = this.getPop3Client(options);
            email = this.fetchMessage(options);
        }
        catch (Exception e) {
            this.client.dispose();
            throw e;
        }
        this.client.dispose();
        return email;
    }

    private Pop3Client getPop3Client(EmailCaptureOptions options) {
        Pop3Client client = new Pop3Client();
        client.setHost(options.getHost());
        client.setPort(options.getPort());
        client.setUsername(options.getUserId());
        client.setPassword(options.getPassword());
        client.setSecurityOptions(256);
        client.setTimeout(1000000);
        return client;
    }

    private FetchMail.Email fetchMessage(EmailCaptureOptions options) throws IOException {
        FetchMail.Email email = null;
        boolean hasCriteria = false;
        MailQueryBuilder builder = new MailQueryBuilder();
        Calendar fetchFromDate = options.getLastReadDateForMails();
        String lastReadMessageUniqueId = options.getMessageUniqueId() == null ? "" : options.getMessageUniqueId();
        Pop3MessageInfoCollection msgCollection = null;
        if (fetchFromDate != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fetchFromDate.getTime());
            calendar.set(11, 0);
            calendar.set(12, 0);
            calendar.set(13, 0);
            builder.getInternalDate().since(calendar.getTime(), 1);
        }
        if (options.getSubjectCriteria().trim().length() > 0) {
            builder.getSubject().contains(options.getSubjectCriteria(), true);
            hasCriteria = true;
        }
        if (hasCriteria) {
            MailQuery query = builder.getQuery();
            msgCollection = this.client.listMessages(query);
        } else {
            msgCollection = this.client.listMessages();
        }
        boolean continueFetchingMssg = true;
        boolean count = false;
        for (Pop3MessageInfo msgInfo : msgCollection) {
            MailMessage msg;
            String uniqueId = msgInfo.getUniqueId();
            if (lastReadMessageUniqueId != null) {
                if (lastReadMessageUniqueId.contains(uniqueId)) {
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
                String newUniqueId = lastReadMessageUniqueId + UNIQUE_ID_DELIMETER + uniqueId + UNIQUE_ID_DELIMETER;
                email.setUniqueId(newUniqueId);
                email.setLastReadDateForMails(sentDate.getTime().getTime());
            } else {
                email.setUniqueId(uniqueId);
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
            email.setProtocol("POP3");
            boolean storeMessageOption = options.getStoreOption();
            String storeattachmentOption = options.getStoreAttachmentOption();
            List<String> matchingFilePattern = null;
            HashMap<String, Map<String, Object>> attachments = new HashMap<String, Map<String, Object>>();
            ArrayList attachmentList = new ArrayList();
            if (storeattachmentOption != null && storeattachmentOption.equals(EmailCaptureOptions.StoreAttachmentOptions.MATCHING.toString())) {
                matchingFilePattern = options.getMatchingFilenamePatterns();
            }
            Path tempMsgPath = Files.createTempFile(subject.trim().replaceAll(":", "_").replaceAll("\\s+", "_"), ".msg", new FileAttribute[0]);
            msg.save(Files.newOutputStream(tempMsgPath, new OpenOption[0]), (SaveOptions)SaveOptions.getDefaultMsg());
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
            if (!options.getActionOnOriginalOptionForPop3().equals(EmailCaptureOptions.ActionOnOriginalOptionsForPop3.DELETE_TO_DELETED_ITEMS.toString())) break;
            this.client.deleteMessage(uniqueId);
            this.client.commitDeletes();
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

    private void saveAttachmentToMsg(MapiMessage tempMsg, Path tempMsgPath, Map<String, Map<String, Object>> attachments) throws IOException {
        if (attachments != null && attachments.size() > 0) {
            for (Map.Entry<String, Map<String, Object>> entry : attachments.entrySet()) {
                String filename = entry.getKey();
                Path filePath = (Path)entry.getValue().get("filePath");
                tempMsg.getAttachments().add(filename, Files.readAllBytes(filePath));
            }
        }
        tempMsg.save(Files.newOutputStream(tempMsgPath, new OpenOption[0]));
    }
}

