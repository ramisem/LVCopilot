/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.activation.DataHandler
 *  javax.activation.DataSource
 *  javax.activation.FileDataSource
 *  javax.mail.Address
 *  javax.mail.Authenticator
 *  javax.mail.BodyPart
 *  javax.mail.Message
 *  javax.mail.Message$RecipientType
 *  javax.mail.MessagingException
 *  javax.mail.Multipart
 *  javax.mail.PasswordAuthentication
 *  javax.mail.Session
 *  javax.mail.Transport
 *  javax.mail.internet.InternetAddress
 *  javax.mail.internet.MimeBodyPart
 *  javax.mail.internet.MimeMessage
 *  javax.mail.internet.MimeMultipart
 */
package com.labvantage.sapphire.actions.mail;

import com.labvantage.opal.handler.ErrorUtil;
import com.labvantage.opal.util.OpalUtil;
import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.util.file.FileManager;
import java.io.File;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import sapphire.SapphireException;
import sapphire.action.BaseAction;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class SendMail
extends BaseAction
implements sapphire.action.SendMail {
    static final String LABVANTAGE_CVS_ID = "$Revision: 95949 $";

    @Override
    public boolean isDatabaseRequired() {
        return false;
    }

    @Override
    public void processAction(PropertyList properties) throws SapphireException {
        String password;
        String username;
        String from = properties.getProperty("from");
        String to = properties.getProperty("address");
        if (to == null || to.length() == 0) {
            to = properties.getProperty("to");
        }
        String cc = properties.getProperty("cc");
        String bcc = properties.getProperty("bcc");
        String replyTo = properties.getProperty("replyto");
        String subject = properties.getProperty("subject");
        String text = properties.getProperty("message");
        String filename = properties.getProperty("filename");
        String logicalFilename = properties.getProperty("logicalfilename");
        String messageHeader = properties.getProperty("messageheader", "");
        String filelocationpolicynode = properties.getProperty("filelocationpolicynode", "SendMail Custom");
        String mailformat = properties.getProperty("mailformat");
        if (OpalUtil.isEmpty(mailformat)) {
            mailformat = "text";
        }
        String additionalpropStr = "";
        String mailserver = "";
        if (this.isSMTPConfigurationPresentInDB()) {
            mailserver = this.getConnectionProcessor().getSysConfigProperty("smtphost");
            username = this.getConnectionProcessor().getConfigProperty("mailserverusernam").trim();
            password = EncryptDecrypt.decrypt(this.getConnectionProcessor().getConfigProperty("mailserverpassword"), this.connectionInfo.getDatabaseId());
            additionalpropStr = this.getConnectionProcessor().getSysConfigProperty("mailserveradditionalprops");
            if (OpalUtil.isEmpty(from)) {
                from = this.getConnectionProcessor().getSysConfigProperty("emailfromaddress");
            }
        } else {
            mailserver = this.getConnectionProcessor().getConfigProperty("com.labvantage.sapphire.server.smtphost");
            username = this.getConnectionProcessor().getConfigProperty("com.labvantage.sapphire.server.mailserverusernam").trim();
            password = EncryptDecrypt.decrypt(this.getConnectionProcessor().getConfigProperty("com.labvantage.sapphire.server.mailserverpassword"), this.connectionInfo.getDatabaseId());
            additionalpropStr = this.getConnectionProcessor().getConfigProperty("com.labvantage.sapphire.server.mailserveradditionalprops");
            if (OpalUtil.isEmpty(from)) {
                from = this.getConnectionProcessor().getConfigProperty("com.labvantage.sapphire.server.emailfromaddress");
            }
        }
        if (OpalUtil.isEmpty(to) && OpalUtil.isEmpty(cc) && OpalUtil.isEmpty(bcc)) {
            throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("Atleast one of the following properties is required: To/CC/BCC"));
        }
        if (OpalUtil.isEmpty(from)) {
            throw new SapphireException("INVALID_PARAMETERS", this.getTranslationProcessor().translate("'From' address is mandatory."));
        }
        Properties props = new Properties();
        props.put("mail.smtp.host", mailserver);
        if (additionalpropStr.trim().length() > 0) {
            String[] additionalprops = StringUtil.split(additionalpropStr, "\r");
            for (int p = 0; p < additionalprops.length; ++p) {
                if (additionalprops[p].indexOf("=") <= 0) continue;
                String[] propnamevalues = StringUtil.split(additionalprops[p], "=");
                props.put(propnamevalues[0].trim(), propnamevalues[1].trim());
            }
        }
        this.logger.info("Connect to mail server " + (username.length() > 0 && password.length() > 0 ? "with authentication for " + username + " " : "") + "using: " + props);
        Session session = Session.getInstance((Properties)props, username.length() > 0 && password.length() > 0 ? new Authenticator(){

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        } : null);
        session.setDebug(Trace.isDebugEnabled());
        MimeMessage message = new MimeMessage(session);
        try {
            int i;
            if (OpalUtil.isNotEmpty(from)) {
                if (from.substring(0, 1).equals("<")) {
                    message.setFrom((Address)new InternetAddress(from));
                } else {
                    String[] froms = StringUtil.split(from, ";");
                    for (i = 0; i < froms.length; ++i) {
                        message.setFrom((Address)new InternetAddress(froms[i]));
                    }
                }
            }
            if (OpalUtil.isNotEmpty(to)) {
                if (to.substring(0, 1).equals("<")) {
                    message.addRecipient(Message.RecipientType.TO, (Address)new InternetAddress(to));
                } else {
                    String[] tos = StringUtil.split(to, ";");
                    for (i = 0; i < tos.length; ++i) {
                        message.addRecipient(Message.RecipientType.TO, (Address)new InternetAddress(tos[i]));
                    }
                }
            }
            if (OpalUtil.isNotEmpty(cc)) {
                if (cc.substring(0, 1).equals("<")) {
                    message.addRecipient(Message.RecipientType.CC, (Address)new InternetAddress(cc));
                } else {
                    String[] ccs = StringUtil.split(cc, ";");
                    for (i = 0; i < ccs.length; ++i) {
                        message.addRecipient(Message.RecipientType.CC, (Address)new InternetAddress(ccs[i]));
                    }
                }
            }
            if (OpalUtil.isNotEmpty(bcc)) {
                if (bcc.substring(0, 1).equals("<")) {
                    message.addRecipient(Message.RecipientType.BCC, (Address)new InternetAddress(bcc));
                } else {
                    String[] bccs = StringUtil.split(bcc, ";");
                    for (i = 0; i < bccs.length; ++i) {
                        message.addRecipient(Message.RecipientType.BCC, (Address)new InternetAddress(bccs[i]));
                    }
                }
            }
            if (replyTo.length() > 0) {
                String[] replyToArr = StringUtil.split(replyTo, ";");
                InternetAddress[] replyToIAddr = new InternetAddress[replyToArr.length];
                for (int i2 = 0; i2 < replyToArr.length; ++i2) {
                    replyToIAddr[i2] = new InternetAddress(replyToArr[i2]);
                }
                message.setReplyTo((Address[])replyToIAddr);
            }
            message.setSubject(StringUtil.replaceAll(subject, "#semicolon#", ";"), "UTF-8");
            message.setSentDate(new Date());
            if (OpalUtil.isEmpty(filename)) {
                if (mailformat.equals("text")) {
                    message.setText(StringUtil.replaceAll(text, "#semicolon#", ";"), "UTF-8");
                } else if (mailformat.equals("html")) {
                    text = text.replaceAll("\r\n", "<br/>");
                    message.setContent((Object)StringUtil.replaceAll(text, "#semicolon#", ";"), "text/html;charset=UTF-8");
                }
            } else {
                MimeBodyPart textBody = new MimeBodyPart();
                if (mailformat.equals("html")) {
                    text = text.replaceAll("\r\n", "<br/>");
                    textBody.setContent((Object)StringUtil.replaceAll(text, "#semicolon#", ";"), "text/html;charset=UTF-8");
                } else {
                    textBody.setText(StringUtil.replaceAll(text, "#semicolon#", ";"), "UTF-8");
                }
                MimeMultipart mimemultipart = new MimeMultipart();
                mimemultipart.addBodyPart((BodyPart)textBody);
                String[] files = StringUtil.split(filename, ";");
                String[] logicalFilenames = null;
                if (logicalFilename.trim().length() > 1) {
                    logicalFilenames = StringUtil.split(logicalFilename, ";");
                }
                PropertyList filelocationPolicy = null;
                for (int i3 = 0; i3 < files.length; ++i3) {
                    File file = new File(files[i3]);
                    if (file.isFile() && file.exists()) {
                        if (filelocationPolicy == null) {
                            try {
                                filelocationPolicy = this.getConfigurationProcessor().getPolicy("FileLocationPolicy", filelocationpolicynode);
                            }
                            catch (Exception e) {
                                this.logger.warn("Unable to obtain file location policy. Please make sure you have an " + filelocationpolicynode + " node.");
                                filelocationPolicy = new PropertyList();
                            }
                        }
                        if (!FileManager.isValidFileLocation(file.toString(), filelocationPolicy)) {
                            throw new SapphireException("INVALID_PARAMETER", "The file '" + files[i3] + "' is from an invalid path.");
                        }
                    } else {
                        throw new SapphireException("INVALID_PARAMETER", "The file '" + files[i3] + "' could not be found.");
                    }
                    MimeBodyPart attachment = new MimeBodyPart();
                    FileDataSource filedatasource = new FileDataSource(file);
                    attachment.setDataHandler(new DataHandler((DataSource)filedatasource));
                    attachment.setFileName(logicalFilenames != null && logicalFilenames.length > i3 ? logicalFilenames[i3] : file.getName());
                    mimemultipart.addBodyPart((BodyPart)attachment);
                }
                message.setContent((Multipart)mimemultipart);
            }
            if (messageHeader.length() > 0) {
                String[] headers = messageHeader.split(";");
                for (int i4 = 0; i4 < headers.length; ++i4) {
                    String[] nameValue;
                    if (!headers[i4].contains("=") || (nameValue = headers[i4].split("=")).length != 2) continue;
                    message.setHeader(nameValue[0], nameValue[1]);
                }
            }
            Transport.send((Message)message);
        }
        catch (MessagingException e) {
            throw new SapphireException("INVALID_PARAMETER", "Messaging Exception thrown: " + ErrorUtil.extractMessageFromException(e, ErrorUtil.isUserAdmin(this.getConnectionId())), e);
        }
    }

    private boolean isSMTPConfigurationPresentInDB() throws SapphireException {
        boolean presentinDB = true;
        String mailserver = this.getConnectionProcessor().getSysConfigProperty("smtphost");
        if (OpalUtil.isEmpty(mailserver)) {
            presentinDB = false;
            mailserver = this.getConnectionProcessor().getConfigProperty("com.labvantage.sapphire.server.smtphost");
            if (OpalUtil.isEmpty(mailserver)) {
                throw new SapphireException("INVALID_PARAMETER", "No SMTP host configuration property found.");
            }
        }
        return presentinDB;
    }
}

