/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.email;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;

public class EmailCaptureOptions {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";
    private String host;
    private int port;
    private String exchangeURI;
    private String mailboxFolderToRead;
    private String userId;
    private String password;
    private boolean doReadFromAllFolders;
    private List<String> fromMatchingPatterns;
    private String subjectCriteria;
    private String messageCriteria;
    private boolean mustHaveAttachment;
    private boolean storeOption;
    private String storeAttachmentOption;
    private List<String> matchingFilenamePatterns;
    private String actionOnOriginalOption;
    private String actionOnOriginalOptionForPop3;
    private String archiveFolder;
    private Path storageLocalPath;
    private Calendar lastReadDateForMails;
    private String messageUniqueId;
    private String mailServerTimeZoneId;

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getExchangeURI() {
        return this.exchangeURI;
    }

    public void setExchangeURI(String exchangeURI) {
        this.exchangeURI = exchangeURI;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMailboxFolderToRead() {
        return this.mailboxFolderToRead;
    }

    public void setMailboxFolderToRead(String mailboxFolderToRead) {
        this.mailboxFolderToRead = mailboxFolderToRead;
    }

    public List<String> getFromMatchingPatterns() {
        return this.fromMatchingPatterns;
    }

    public void setFromMatchingPatterns(List<String> fromMatchingPatterns) {
        this.fromMatchingPatterns = fromMatchingPatterns;
    }

    public String getSubjectCriteria() {
        return this.subjectCriteria;
    }

    public void setSubjectCriteria(String subjectCriteria) {
        this.subjectCriteria = subjectCriteria;
    }

    public String getMessageCriteria() {
        return this.messageCriteria;
    }

    public void setMessageCriteria(String messageCriteria) {
        this.messageCriteria = messageCriteria;
    }

    public boolean isMustHaveAttachment() {
        return this.mustHaveAttachment;
    }

    public void setMustHaveAttachment(boolean mustHaveAttachment) {
        this.mustHaveAttachment = mustHaveAttachment;
    }

    public boolean getStoreOption() {
        return this.storeOption;
    }

    public void setStoreOption(boolean storeOption) {
        this.storeOption = storeOption;
    }

    public String getStoreAttachmentOption() {
        return this.storeAttachmentOption;
    }

    public void setStoreAttachmentOption(String storeAttachmentOption) {
        for (StoreAttachmentOptions value : StoreAttachmentOptions.values()) {
            if (!value.toString().equalsIgnoreCase(storeAttachmentOption)) continue;
            this.storeAttachmentOption = value.toString();
            break;
        }
    }

    public List<String> getMatchingFilenamePatterns() {
        return this.matchingFilenamePatterns;
    }

    public void setMatchingFilenamePatterns(List<String> matchingFilenamePatterns) {
        this.matchingFilenamePatterns = matchingFilenamePatterns;
    }

    public String getActionOnOriginalOption() {
        return this.actionOnOriginalOption;
    }

    public void setActionOnOriginalOption(String actionOnOriginalOption) {
        for (ActionOnOriginalOptions value : ActionOnOriginalOptions.values()) {
            if (!value.getOption().trim().equalsIgnoreCase(actionOnOriginalOption)) continue;
            this.actionOnOriginalOption = value.toString();
            break;
        }
    }

    public String getActionOnOriginalOptionForPop3() {
        return this.actionOnOriginalOptionForPop3;
    }

    public void setActionOnOriginalOptionForPop3(String actionOnOriginalOptionForPop3) {
        for (ActionOnOriginalOptionsForPop3 value : ActionOnOriginalOptionsForPop3.values()) {
            if (!value.getOption().trim().equalsIgnoreCase(actionOnOriginalOptionForPop3)) continue;
            this.actionOnOriginalOptionForPop3 = value.toString();
            break;
        }
    }

    public String getArchiveFolder() {
        return this.archiveFolder;
    }

    public void setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
    }

    public Path getStorageLocalPath() {
        return this.storageLocalPath;
    }

    public void setStorageLocalPath(Path storageLocalPath) {
        this.storageLocalPath = storageLocalPath;
    }

    public Calendar getLastReadDateForMails() {
        return this.lastReadDateForMails;
    }

    public void setLastReadDateForMails(Calendar lastReadDateForMails) {
        this.lastReadDateForMails = lastReadDateForMails;
    }

    public String getMessageUniqueId() {
        return this.messageUniqueId;
    }

    public void setMessageUniqueId(String messageUniqueId) {
        this.messageUniqueId = messageUniqueId;
    }

    public String getMailServerTimeZoneId() {
        return this.mailServerTimeZoneId;
    }

    public void setMailServerTimeZoneId(String mailServerTimeZoneId) {
        this.mailServerTimeZoneId = mailServerTimeZoneId;
    }

    public static enum ActionOnOriginalOptionsForPop3 {
        LEAVE("Leave"),
        DELETE_TO_DELETED_ITEMS("Move To Deleted Items");

        private String option;

        private ActionOnOriginalOptionsForPop3(String option) {
            this.option = option;
        }

        public String getOption() {
            return this.option;
        }
    }

    public static enum ActionOnOriginalOptions {
        ARCHIVE("Archive"),
        DELETE_PERMANENTLY("Delete Permanently"),
        DELETE_TO_DELETED_ITEMS("Move To Deleted Items"),
        LEAVE("Leave");

        private String option;

        private ActionOnOriginalOptions(String option) {
            this.option = option;
        }

        public String getOption() {
            return this.option;
        }
    }

    public static enum StoreAttachmentOptions {
        ALL,
        MATCHING,
        NONE;

    }
}

