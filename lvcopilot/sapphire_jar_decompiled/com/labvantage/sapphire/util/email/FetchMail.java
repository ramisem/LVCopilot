/*
 * Decompiled with CFR 0.152.
 */
package com.labvantage.sapphire.util.email;

import com.labvantage.sapphire.util.email.EmailCaptureOptions;
import com.labvantage.sapphire.util.email.ReadAndParseMail;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FetchMail {
    static final String LABVANTAGE_CVS_ID = "1: 1.1 $";

    public Email readEmail(String protocol, EmailCaptureOptions options) throws Exception {
        return MailProtocol.valueOf(protocol).getInstance().readEmail(options);
    }

    public class Email {
        private String subject;
        private Calendar sentDate;
        private String uniqueId;
        private String addressFrom;
        private List<String> addressesTo;
        private List<String> addressesCC;
        private List<String> addressesBcc;
        private Path msgPath;
        private String protocol;
        private Map<String, Map<String, Object>> attachments = new HashMap<String, Map<String, Object>>();
        private long lastReadDateForMails;
        private String mailServerTimeZoneId;

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public void setSentDate(Calendar sentDate) {
            this.sentDate = sentDate;
        }

        public void setAddressFrom(String addressFrom) {
            this.addressFrom = addressFrom;
        }

        public void setAddressesTo(List<String> addressesTo) {
            this.addressesTo = addressesTo;
        }

        public void setAddressesCC(List<String> addressesCC) {
            this.addressesCC = addressesCC;
        }

        public void setAddressesBcc(List<String> addressesBcc) {
            this.addressesBcc = addressesBcc;
        }

        public void setMsgPath(Path msgPath) {
            this.msgPath = msgPath;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public void setAttachments(Map<String, Map<String, Object>> attachments) {
            this.attachments = attachments;
        }

        public String getSubject() {
            return this.subject;
        }

        public Calendar getSentDate() {
            return this.sentDate;
        }

        public String getAddressFrom() {
            return this.addressFrom;
        }

        public List<String> getAddressesTo() {
            return this.addressesTo;
        }

        public List<String> getAddressesCC() {
            return this.addressesCC;
        }

        public List<String> getAddressesBcc() {
            return this.addressesBcc;
        }

        public Path getMsgPath() {
            return this.msgPath;
        }

        public String getProtocol() {
            return this.protocol;
        }

        public Map<String, Map<String, Object>> getAttachments() {
            return this.attachments;
        }

        public String getUniqueId() {
            return this.uniqueId;
        }

        public void setUniqueId(String uniqueId) {
            this.uniqueId = uniqueId;
        }

        public long getLastReadDateForMails() {
            return this.lastReadDateForMails;
        }

        public void setLastReadDateForMails(long lastReadDateForMails) {
            this.lastReadDateForMails = lastReadDateForMails;
        }

        public String getMailServerTimeZoneId() {
            return this.mailServerTimeZoneId;
        }

        public void setMailServerTimeZoneId(String mailServerTimeZoneId) {
            this.mailServerTimeZoneId = mailServerTimeZoneId;
        }
    }

    static enum MailProtocol {
        Exchange("ReadAndParseExchangeMail"),
        IMAP("ReadAndParseImapMail"),
        POP3("ReadAndParsePop3Mail");

        private String className;

        private MailProtocol(String className) {
            this.className = className;
        }

        public ReadAndParseMail getInstance() {
            ReadAndParseMail readAndParseMail = null;
            String packageName = ((Object)((Object)this)).getClass().getPackage().getName();
            try {
                readAndParseMail = (ReadAndParseMail)Class.forName(packageName + "." + this.className).newInstance();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return readAndParseMail;
        }
    }
}

