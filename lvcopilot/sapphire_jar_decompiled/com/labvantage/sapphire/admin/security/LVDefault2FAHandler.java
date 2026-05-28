/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.warrenstrange.googleauth.GoogleAuthenticator
 *  com.warrenstrange.googleauth.GoogleAuthenticatorKey
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.labvantage.sapphire.admin.security;

import com.labvantage.sapphire.EncryptDecrypt;
import com.labvantage.sapphire.Trace;
import com.labvantage.sapphire.admin.system.ConfigurationProcessor;
import com.labvantage.sapphire.platform.Configuration;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import java.net.URLEncoder;
import java.security.Provider;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sapphire.accessor.ActionException;
import sapphire.accessor.ActionProcessor;
import sapphire.accessor.ConnectionProcessor;
import sapphire.accessor.QueryProcessor;
import sapphire.accessor.TranslationProcessor;
import sapphire.ext.BaseWebMFAHandler;
import sapphire.servlet.AjaxResponse;
import sapphire.util.ActionBlock;
import sapphire.util.ConnectionInfo;
import sapphire.util.DataSet;
import sapphire.util.HttpUtil;
import sapphire.util.SafeHTML;
import sapphire.util.StringUtil;
import sapphire.xml.PropertyList;

public class LVDefault2FAHandler
extends BaseWebMFAHandler {
    private String newSecretKey = "";
    private HashMap<String, String> tokenMap = new HashMap();
    private int passcode;

    private static void setupSecureRandomProvider() {
        Provider[] providers = Security.getProviders("SecureRandom.SHA1PRNG");
        if (providers != null && providers.length > 0) {
            System.setProperty("com.warrenstrange.googleauth.rng.algorithmProvider", providers[0].getName());
            Trace.logInfo("SecureRandom.SHA1PRNG Provider system property for GoogleAuth API set to : " + providers[0].getName());
        } else {
            Trace.logInfo("No SecureRandmon.SHA1PRNG Provider found. May cause GoogleAuth API to malfunction.");
        }
    }

    public void setPasscode(int passcode) {
        this.passcode = passcode;
    }

    @Override
    public void renderPrompt(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ConnectionInfo connectionInfo = new ConnectionProcessor(this.connectionid).getConnectionInfo(this.connectionid);
        this.tokenMap.put("databaseid", connectionInfo.getDatabaseId());
        this.tokenMap.put("sysuserid", connectionInfo.getSysuserId());
        this.tokenMap.put("username", connectionInfo.getSysuserName());
        request.getRequestDispatcher("/mfaprompt.jsp").include((ServletRequest)request, (ServletResponse)response);
    }

    public void renderPrompt() throws Exception {
        ConnectionInfo connectionInfo = new ConnectionProcessor(this.connectionid).getConnectionInfo(this.connectionid);
        this.tokenMap.put("databaseid", connectionInfo.getDatabaseId());
        this.tokenMap.put("sysuserid", connectionInfo.getSysuserId());
        this.tokenMap.put("username", connectionInfo.getSysuserName());
    }

    @Override
    public boolean verifyResponse(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Trace.log("Start verify response");
        String passcodeStr = request.getParameter("passcode");
        String rememberdevice = request.getParameter("rememberdevice");
        String errormsg = "Code Incorrect! Please try again";
        boolean isCodeValid = false;
        if (passcodeStr != null) {
            try {
                int passcode = Integer.parseInt(passcodeStr.trim());
                if (passcode == this.passcode) {
                    isCodeValid = true;
                    this.passcode = 0;
                    Trace.log("Verified code sent to email");
                }
                if (!isCodeValid) {
                    GoogleAuthenticator gAuth = new GoogleAuthenticator();
                    if (this.newSecretKey.length() > 0) {
                        this.secretKey = this.newSecretKey;
                    }
                    isCodeValid = gAuth.authorize(this.secretKey, passcode);
                    Trace.log("Verified code from Google Authenticator");
                    if (isCodeValid && this.newSecretKey.length() > 0) {
                        ConnectionInfo connectionInfo = new ConnectionProcessor(this.connectionid).getConnectionInfo(this.connectionid);
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", "User");
                        actionProps.setProperty("keyid1", connectionInfo.getSysuserId());
                        actionProps.setProperty("mfasecretkey", this.newSecretKey);
                        new ActionProcessor(this.connectionid).processAction("EditSDI", "1", actionProps);
                        Trace.log("Google Authenticator secret key saved for user " + connectionInfo.getSysuserId());
                    }
                }
            }
            catch (Throwable t) {
                Trace.log("Code not valid:" + t.getMessage());
            }
            if (isCodeValid && "on".equals(rememberdevice)) {
                String value = this.username + "|%|" + new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                value = EncryptDecrypt.encrypt(value, this.database);
                new HttpUtil(request, response).setCookieHeader("rememberdevice", value, true, true);
            }
        } else {
            errormsg = "You must enter passcode";
        }
        if (!isCodeValid) {
            Trace.log("Code not valid. Send back to mfa prompt page with error message:" + errormsg);
            request.getRequestDispatcher("/mfaprompt.jsp?errormsg=" + errormsg).include((ServletRequest)request, (ServletResponse)response);
        }
        return isCodeValid;
    }

    public String verifyResponse(String passcodeS, boolean isRememberMe, Map<String, Object> sessionProps) throws Exception {
        Trace.log("Start verify response");
        String passcodeStr = passcodeS;
        String errormsg = "Code Incorrect! Please try again";
        boolean isCodeValid = false;
        if (passcodeStr != null) {
            int passcode = 0;
            try {
                passcode = Integer.parseInt(passcodeStr.trim());
                if (passcode == this.passcode) {
                    isCodeValid = true;
                    this.passcode = 0;
                    Trace.log("Verified code sent to email");
                }
                if (!isCodeValid) {
                    GoogleAuthenticator gAuth = new GoogleAuthenticator();
                    if (this.newSecretKey.length() > 0) {
                        this.secretKey = this.newSecretKey;
                    }
                    isCodeValid = gAuth.authorize(this.secretKey, passcode);
                    Trace.log("Verified code from Google Authenticator");
                    if (isCodeValid && this.newSecretKey.length() > 0) {
                        ConnectionInfo connectionInfo = this.getConnectionInfo();
                        PropertyList actionProps = new PropertyList();
                        actionProps.setProperty("sdcid", "User");
                        actionProps.setProperty("keyid1", connectionInfo.getSysuserId());
                        actionProps.setProperty("mfasecretkey", this.newSecretKey);
                        new ActionProcessor(this.connectionid).processAction("EditSDI", "1", actionProps);
                        Trace.log("Google Authenticator secret key saved for user " + connectionInfo.getSysuserId());
                    }
                }
                if (isCodeValid && isRememberMe) {
                    String value = this.username + "|%|" + new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                    value = EncryptDecrypt.encrypt(value, this.database);
                    sessionProps.put("rememberdevice", value);
                }
            }
            catch (NumberFormatException e) {
                isCodeValid = false;
                errormsg = "Invalid Passcode.";
            }
        } else {
            errormsg = "You must enter passcode";
        }
        if (!isCodeValid) {
            Trace.log("Code not valid. Send back to mfa prompt page with error message:" + errormsg);
        }
        return isCodeValid ? "" : errormsg;
    }

    public String sendPasscode(String mode) {
        int passcode = LVDefault2FAHandler.generatePasscode();
        String responsemsg = "";
        TranslationProcessor tp = new TranslationProcessor(this.connectionid);
        String username = this.getUsername();
        if ("email".equals(mode)) {
            String fromEmail = null;
            try {
                fromEmail = new ConfigurationProcessor(this.connectionid).getConfigProperty("com.labvantage.sapphire.server.emailfromaddress", "");
            }
            catch (Exception exception) {
                // empty catch block
            }
            QueryProcessor queryProcessor = new QueryProcessor(this.connectionid);
            DataSet sysuser = queryProcessor.getPreparedSqlDataSet("SELECT sysuserid, email FROM sysuser WHERE lower( sysuserid ) = ? OR logonname = ?", new Object[]{username.toLowerCase(), username});
            String emailto = sysuser.getValue(0, "email");
            HashMap<String, String> sendMailProps = new HashMap<String, String>();
            if (emailto.length() > 0 && fromEmail.length() > 0) {
                sendMailProps.put("to", emailto);
                sendMailProps.put("from", fromEmail);
                sendMailProps.put("subject", this.getEmailSubject());
                sendMailProps.put("mailformat", "html");
                sendMailProps.put("message", this.getEmailMessage(passcode));
                ActionBlock actionBlock = new ActionBlock();
                try {
                    sendMailProps.put("actionid", "SendMail");
                    sendMailProps.put("actionversionid", "1");
                    actionBlock.setAction("sendmail", "AddToDoListEntry", "1", sendMailProps);
                    ActionProcessor actionProcessor = new ActionProcessor(this.connectionid);
                    actionProcessor.processActionBlock(actionBlock);
                    this.setPasscode(passcode);
                    responsemsg = this.getEmailSentMessage(emailto);
                }
                catch (ActionException ae) {
                    responsemsg = tp.translate("Error: Unexpected error while sending email.") + " " + tp.translate("Please contact system administrator.");
                    Trace.logError("Sendmail Failed.", ae);
                }
            } else {
                responsemsg = tp.translate("Error: email address not found for") + " " + username + ".";
            }
        } else {
            responsemsg = tp.translate("Error: Unknown error.") + " " + tp.translate("Please contact system administrator.");
        }
        return responsemsg;
    }

    public void sendPasscode(HttpServletRequest request, HttpServletResponse response) {
        String responsemsg = this.sendPasscode(request.getParameter("mode") != null ? request.getParameter("mode") : "email");
        AjaxResponse ajaxResponse = new AjaxResponse(request, response);
        ajaxResponse.addCallbackArgument("message", SafeHTML.encodeForHTML(responsemsg, true));
        ajaxResponse.print();
    }

    public boolean isAuthenticatorAppEnrolled() {
        return this.secretKey != null && this.secretKey.length() > 0;
    }

    public String getPromptPageTitle() {
        return this.replaceToken(this.getAuthenticationProps().getProperty("promptpagetitle"));
    }

    public String getEnterPasscodePromptText() {
        String prompttext = this.replaceToken(this.isAuthenticatorAppEnrolled() ? this.getAuthenticationProps().getProperty("enterpasscodemsgafterenroll") : this.getAuthenticationProps().getProperty("enterpasscodemsgbeforeenroll"));
        return prompttext;
    }

    public String getPasscodeInputPlaceholder() {
        return this.replaceToken(this.getAuthenticationProps().getProperty("passcodeinputplaceholder"));
    }

    public String getEmailCodeButtonLabel() {
        return this.replaceToken(this.getAuthenticationProps().getProperty("sendemailbuttonlabel"));
    }

    public String getSignInButtonLabel() {
        return this.replaceToken(this.getAuthenticationProps().getProperty("signinbuttonlabel"));
    }

    public String getRememberDeviceLabel() {
        return this.replaceToken(this.getAuthenticationProps().getProperty("rememberdevicelabel"));
    }

    public String getEmailSubject() {
        return this.replaceToken(this.getAuthenticationProps().getPropertyList("emailoptions").getProperty("emailsubject"));
    }

    public String getEmailMessage(int passcode) {
        this.tokenMap.put("passcode", "" + passcode);
        return this.replaceToken(this.getAuthenticationProps().getPropertyList("emailoptions").getProperty("emailmessage"));
    }

    public String getEmailSentMessage(String emailto) {
        this.tokenMap.put("emailto", emailto);
        return this.replaceToken(this.getAuthenticationProps().getPropertyList("emailoptions").getProperty("emailsentmessage"));
    }

    public String getSetupAuthenticatorLinkLabel() {
        return this.replaceToken(this.getAuthenticationProps().getPropertyList("googleauthenticatoroptions").getProperty("setuplinklabel"));
    }

    public String getQRUrl() {
        this.newSecretKey = LVDefault2FAHandler.getSecretKey();
        String accountname = this.replaceToken(this.getAuthenticationProps().getPropertyList("googleauthenticatoroptions").getProperty("accountname"));
        return LVDefault2FAHandler.generateQRUrl(this.username, this.newSecretKey, accountname);
    }

    private String replaceToken(String text) {
        if (this.tokenMap != null) {
            for (String key : this.tokenMap.keySet()) {
                text = StringUtil.replaceAll(text, "[" + key + "]", this.tokenMap.get(key));
            }
        }
        return text;
    }

    public static String getSecretKey() {
        Trace.logInfo("SecureRandmon.SHA1PRNG provider set to: " + System.getProperty("com.warrenstrange.googleauth.rng.algorithmProvider"));
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public static int generatePasscode() {
        Trace.logInfo("SecureRandmon.SHA1PRNG provider set to: " + System.getProperty("com.warrenstrange.googleauth.rng.algorithmProvider"));
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        int code = gAuth.getTotpPassword(gAuth.createCredentials().getKey());
        return code;
    }

    public static String generateQRUrl(String username, String secretKey, String accountname) {
        String qrUrl = "";
        try {
            qrUrl = "/" + Configuration.getInstance().getApplicationid() + "/rc?command=image&qrdata=" + URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", accountname, username, secretKey, accountname), "UTF-8");
        }
        catch (Exception exception) {
            // empty catch block
        }
        return qrUrl;
    }

    static {
        LVDefault2FAHandler.setupSecureRandomProvider();
    }
}

